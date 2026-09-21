from __future__ import annotations

import base64
import hashlib
import json
import secrets
import uuid
from datetime import timedelta

from sqlalchemy import and_, func, or_, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.creations.models import (
    ConferenceCategory,
    CreationExportJob,
    CreationExportJobStatus,
    CreationProject,
    CreationProjectStatus,
    CreationVersion,
    CreationVisibility,
    LearningCard,
    LearningCardManual,
    ProvenanceManifest,
    ProvenanceItem,
    Publication,
    PublicationStatus,
)
from app.domains.conference.models import (
    ConferenceCollection,
    ConferenceDerivativeRequest,
    ConferenceLike,
    ConferenceReview,
    ConferenceReviewModerationStatus,
)
from app.domains.distribution.contracts import (
    ClassroomCreate,
    ClassroomCreatedPublic,
    ClassroomJoin,
    ClassroomListPublic,
    ClassroomPublic,
    ClassroomRole,
    PublicationFeedItemPublic,
    PublicationLearningCardSummaryPublic,
    PublicationProvenanceSummaryPublic,
    PublicationRelatedManualPublic,
    PublicationFeedPagePublic,
)
from app.domains.catalog.models import ManualPage
from app.domains.distribution.models import (
    Classroom,
    ClassroomMembership,
    ClassroomMembershipStatus,
    ClassroomStatus,
    PublicationDelivery,
    PublicationDeliveryChannel,
    PublicationDeliveryStatus,
)
from app.domains.media.models import MediaAsset, MediaAssetStatus, MediaDerivative, MediaDerivativeKind
from app.domains.media.storage import ObjectStore
from app.domains.privacy.models import PrivacySetting
from app.models import AgeBand, BlacklistEntry, GuardianLink, User, UserStatus


JOIN_CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"


def _hash_join_code(code: str) -> str:
    return hashlib.sha256(code.encode("ascii")).hexdigest()


def _encode_cursor(publication: Publication) -> str:
    assert publication.published_at is not None
    raw = json.dumps(
        {"v": 1, "at": publication.published_at.isoformat(), "id": str(publication.id)},
        separators=(",", ":"),
    ).encode()
    return base64.urlsafe_b64encode(raw).decode().rstrip("=")


def _decode_cursor(value: str) -> tuple[object, uuid.UUID]:
    from datetime import datetime

    try:
        padded = value + "=" * (-len(value) % 4)
        payload = json.loads(base64.urlsafe_b64decode(padded).decode())
        if payload.get("v") != 1:
            raise ValueError
        return datetime.fromisoformat(payload["at"]), uuid.UUID(payload["id"])
    except (ValueError, TypeError, KeyError, json.JSONDecodeError) as exc:
        raise ApiError(422, "INVALID_CURSOR", "分页游标无效") from exc


def activate_publication_delivery(db: Session, publication: Publication) -> None:
    """Create or reactivate a recipient snapshot in the same moderation transaction."""
    now = utcnow()
    target: dict[str, object] | None = None
    if publication.visibility == CreationVisibility.GUARDIAN_ONLY:
        link = db.scalar(
            select(GuardianLink).where(GuardianLink.child_user_id == publication.owner_user_id)
        )
        if link is None:
            return
        guardian = db.scalar(
            select(User).where(
                User.phone_lookup_hash == link.guardian_phone_hash,
                User.age_band == AgeBand.ADULT,
                User.status == UserStatus.ACTIVE,
            )
        )
        target = {
            "channel": PublicationDeliveryChannel.GUARDIAN,
            "recipient_user_id": guardian.id if guardian else None,
            "recipient_lookup_hash": link.guardian_phone_hash,
            "classroom_id": None,
        }
    elif publication.visibility == CreationVisibility.CLASSROOM and publication.classroom_id:
        classroom = db.scalar(
            select(Classroom).where(
                Classroom.id == publication.classroom_id,
                Classroom.status == ClassroomStatus.ACTIVE,
            )
        )
        if classroom is None:
            return
        target = {
            "channel": PublicationDeliveryChannel.CLASSROOM,
            "recipient_user_id": classroom.owner_teacher_user_id,
            "recipient_lookup_hash": None,
            "classroom_id": classroom.id,
        }
    if target is None:
        return
    existing = db.scalar(
        select(PublicationDelivery).where(
            PublicationDelivery.publication_id == publication.id,
            PublicationDelivery.channel == target["channel"],
        )
    )
    if existing is None:
        db.add(
            PublicationDelivery(
                publication_id=publication.id,
                status=PublicationDeliveryStatus.ACTIVE,
                delivered_at=now,
                **target,
            )
        )
    else:
        existing.recipient_user_id = target["recipient_user_id"]
        existing.recipient_lookup_hash = target["recipient_lookup_hash"]
        existing.classroom_id = target["classroom_id"]
        existing.status = PublicationDeliveryStatus.ACTIVE
        existing.delivered_at = now
        existing.revoked_at = None


def revoke_publication_deliveries(db: Session, publication_id: uuid.UUID) -> None:
    now = utcnow()
    deliveries = db.scalars(
        select(PublicationDelivery).where(
            PublicationDelivery.publication_id == publication_id,
            PublicationDelivery.status == PublicationDeliveryStatus.ACTIVE,
        )
    ).all()
    for delivery in deliveries:
        delivery.status = PublicationDeliveryStatus.REVOKED
        delivery.revoked_at = now


class DistributionService:
    def __init__(self, db: Session, settings: Settings, store: ObjectStore) -> None:
        self.db = db
        self.settings = settings
        self.store = store

    def create_classroom(self, user: User, payload: ClassroomCreate) -> ClassroomCreatedPublic:
        if user.age_band != AgeBand.ADULT:
            raise ApiError(403, "ADULT_ACCOUNT_REQUIRED", "只有成人账户可以创建班级")
        for _ in range(8):
            code = "".join(secrets.choice(JOIN_CODE_ALPHABET) for _ in range(8))
            classroom = Classroom(
                owner_teacher_user_id=user.id,
                name=payload.name,
                join_code_hash=_hash_join_code(code),
                status=ClassroomStatus.ACTIVE,
            )
            self.db.add(classroom)
            try:
                self.db.commit()
            except IntegrityError:
                self.db.rollback()
                continue
            return ClassroomCreatedPublic(
                id=classroom.id,
                name=classroom.name,
                join_code=code,
                member_count=0,
                created_at=classroom.created_at,
            )
        raise ApiError(503, "JOIN_CODE_UNAVAILABLE", "暂时无法生成班级邀请码，请稍后重试")

    def join_classroom(self, user: User, payload: ClassroomJoin) -> ClassroomPublic:
        classroom = self.db.scalar(
            select(Classroom).where(
                Classroom.join_code_hash == _hash_join_code(payload.join_code),
                Classroom.status == ClassroomStatus.ACTIVE,
            )
        )
        if classroom is None:
            raise ApiError(404, "CLASSROOM_NOT_FOUND", "班级邀请码无效或已失效")
        if classroom.owner_teacher_user_id == user.id:
            raise ApiError(409, "CLASSROOM_OWNER_CANNOT_JOIN", "班级创建者无需加入自己的班级")
        membership = self.db.scalar(
            select(ClassroomMembership).where(
                ClassroomMembership.classroom_id == classroom.id,
                ClassroomMembership.student_user_id == user.id,
            )
        )
        now = utcnow()
        if membership is None:
            membership = ClassroomMembership(
                classroom_id=classroom.id,
                student_user_id=user.id,
                status=ClassroomMembershipStatus.ACTIVE,
                joined_at=now,
            )
            self.db.add(membership)
        elif membership.status != ClassroomMembershipStatus.ACTIVE:
            membership.status = ClassroomMembershipStatus.ACTIVE
            membership.joined_at = now
            membership.left_at = None
        self.db.commit()
        teacher = self.db.get(User, classroom.owner_teacher_user_id)
        return self._classroom_public(classroom, ClassroomRole.MEMBER, teacher, membership.joined_at)

    def list_classrooms(self, user: User) -> ClassroomListPublic:
        owned = self.db.scalars(
            select(Classroom)
            .where(
                Classroom.owner_teacher_user_id == user.id,
                Classroom.status == ClassroomStatus.ACTIVE,
            )
            .order_by(Classroom.created_at.desc())
        ).all()
        joined = self.db.execute(
            select(Classroom, ClassroomMembership)
            .join(ClassroomMembership, ClassroomMembership.classroom_id == Classroom.id)
            .where(
                ClassroomMembership.student_user_id == user.id,
                ClassroomMembership.status == ClassroomMembershipStatus.ACTIVE,
                Classroom.status == ClassroomStatus.ACTIVE,
            )
            .order_by(ClassroomMembership.joined_at.desc())
        ).all()
        items: list[ClassroomPublic] = []
        for classroom in owned:
            items.append(self._classroom_public(classroom, ClassroomRole.OWNER, user, None))
        for classroom, membership in joined:
            items.append(
                self._classroom_public(
                    classroom,
                    ClassroomRole.MEMBER,
                    self.db.get(User, classroom.owner_teacher_user_id),
                    membership.joined_at,
                )
            )
        return ClassroomListPublic(items=items)

    def community_feed(
        self,
        user: User,
        *,
        cursor: str | None,
        limit: int,
        category: ConferenceCategory | None = None,
    ) -> PublicationFeedPagePublic:
        statement = self._community_statement(user)
        if category is not None:
            statement = statement.where(Publication.conference_category == category)
        statement = self._apply_cursor(statement, cursor)
        rows = self.db.execute(
            statement.order_by(Publication.published_at.desc(), Publication.id.desc()).limit(limit + 1)
        ).all()
        return self._feed_page(rows, limit, is_inbox=False, viewer=user)

    def owned_community_feed(
        self, user: User, *, limit: int
    ) -> PublicationFeedPagePublic:
        rows = self.db.execute(
            self._community_statement(user)
            .where(Publication.owner_user_id == user.id)
            .order_by(Publication.published_at.desc(), Publication.id.desc())
            .limit(limit)
        ).all()
        return self._feed_page(rows, limit, is_inbox=False, viewer=user)

    def community_item(
        self, user: User, publication_id: uuid.UUID
    ) -> PublicationFeedItemPublic | None:
        row = self.db.execute(
            self._community_statement(user).where(Publication.id == publication_id)
        ).one_or_none()
        if row is None:
            return None
        publication, project, version, author = row
        return self._feed_item(publication, project, version, author, None, viewer=user)

    @staticmethod
    def _community_statement(user: User) -> object:
        blocked_ids = select(BlacklistEntry.blocked_user_id).where(
            BlacklistEntry.owner_user_id == user.id
        )
        blocking_ids = select(BlacklistEntry.owner_user_id).where(
            BlacklistEntry.blocked_user_id == user.id
        )
        statement = (
            select(Publication, CreationProject, CreationVersion, User)
            .join(CreationProject, CreationProject.id == Publication.project_id)
            .join(CreationVersion, CreationVersion.id == Publication.creation_version_id)
            .join(User, User.id == Publication.owner_user_id)
            .where(
                Publication.status == PublicationStatus.PUBLISHED,
                Publication.visibility == CreationVisibility.COMMUNITY,
                Publication.published_at.is_not(None),
                CreationProject.status == CreationProjectStatus.ACTIVE,
                User.status == UserStatus.ACTIVE,
                Publication.owner_user_id.not_in(blocked_ids),
                Publication.owner_user_id.not_in(blocking_ids),
            )
        )
        return statement

    def inbox(
        self, user: User, *, cursor: str | None, limit: int
    ) -> PublicationFeedPagePublic:
        recipient = PublicationDelivery.recipient_user_id == user.id
        if user.age_band == AgeBand.ADULT:
            recipient = or_(recipient, PublicationDelivery.recipient_lookup_hash == user.phone_lookup_hash)
        statement = (
            select(Publication, CreationProject, CreationVersion, User, PublicationDelivery)
            .join(PublicationDelivery, PublicationDelivery.publication_id == Publication.id)
            .join(CreationProject, CreationProject.id == Publication.project_id)
            .join(CreationVersion, CreationVersion.id == Publication.creation_version_id)
            .join(User, User.id == Publication.owner_user_id)
            .where(
                recipient,
                PublicationDelivery.status == PublicationDeliveryStatus.ACTIVE,
                Publication.status == PublicationStatus.PUBLISHED,
                Publication.published_at.is_not(None),
                CreationProject.status == CreationProjectStatus.ACTIVE,
                User.status == UserStatus.ACTIVE,
            )
        )
        statement = self._apply_cursor(statement, cursor)
        rows = self.db.execute(
            statement.order_by(Publication.published_at.desc(), Publication.id.desc()).limit(limit + 1)
        ).all()
        return self._feed_page(rows, limit, is_inbox=True, viewer=user)

    @staticmethod
    def _apply_cursor(statement: object, cursor: str | None) -> object:
        if not cursor:
            return statement
        published_at, publication_id = _decode_cursor(cursor)
        return statement.where(
            or_(
                Publication.published_at < published_at,
                and_(Publication.published_at == published_at, Publication.id < publication_id),
            )
        )

    def _feed_page(
        self, rows: list[object], limit: int, *, is_inbox: bool, viewer: User
    ) -> PublicationFeedPagePublic:
        visible_rows = rows[:limit]
        items: list[PublicationFeedItemPublic] = []
        for row in visible_rows:
            publication, project, version, author = row[:4]
            delivery = row[4] if is_inbox else None
            items.append(
                self._feed_item(
                    publication,
                    project,
                    version,
                    author,
                    delivery.channel if delivery else None,
                    viewer=viewer,
                )
            )
        next_cursor = _encode_cursor(visible_rows[-1][0]) if len(rows) > limit and visible_rows else None
        return PublicationFeedPagePublic(items=items, next_cursor=next_cursor)

    def _feed_item(
        self,
        publication: Publication,
        project: CreationProject,
        version: CreationVersion,
        author: User,
        channel: PublicationDeliveryChannel | None,
        *,
        viewer: User,
    ) -> PublicationFeedItemPublic:
        asset = self._preview_asset(version)
        preview_url = None
        mime_type = None
        width = None
        height = None
        duration_ms = None
        expires_at = None
        if asset and asset.private_object_key:
            expires_at = utcnow() + timedelta(minutes=self.settings.media_download_ttl_minutes)
            derivative = self.db.scalar(
                select(MediaDerivative).where(
                    MediaDerivative.asset_id == asset.id,
                    MediaDerivative.kind == MediaDerivativeKind.THUMBNAIL_640,
                )
            )
            if derivative:
                preview_url = self.store.presign_private_download(
                    derivative.storage_key,
                    expires=timedelta(minutes=self.settings.media_download_ttl_minutes),
                )
                mime_type, width, height = derivative.mime_type, derivative.width, derivative.height
            else:
                preview_url = self.store.presign_private_download(
                    asset.private_object_key,
                    expires=timedelta(minutes=self.settings.media_download_ttl_minutes),
                )
                mime_type, width, height = asset.actual_mime, asset.width, asset.height
                duration_ms = asset.duration_ms
        manifest = self.db.get(ProvenanceManifest, version.id)
        card = self.db.get(LearningCard, version.id)
        privacy = self.db.get(PrivacySetting, author.id)
        include_learning = publication.visibility != CreationVisibility.COMMUNITY or bool(
            privacy and privacy.learning_card_public
        )
        classroom = self.db.get(Classroom, publication.classroom_id) if publication.classroom_id else None
        manual_rows = self.db.execute(
            select(ManualPage.id, ManualPage.page_no, ManualPage.title)
            .join(
                LearningCardManual,
                LearningCardManual.manual_page_id == ManualPage.id,
            )
            .where(LearningCardManual.creation_version_id == version.id)
            .order_by(ManualPage.page_no)
        ).all()
        source_count = self.db.scalar(
            select(func.count())
            .select_from(ProvenanceItem)
            .where(ProvenanceItem.creation_version_id == version.id)
        ) or 0
        is_collected = self.db.scalar(
            select(ConferenceCollection.id).where(
                ConferenceCollection.user_id == viewer.id,
                ConferenceCollection.publication_id == publication.id,
            )
        ) is not None
        collection_count = self.db.scalar(
            select(func.count())
            .select_from(ConferenceCollection)
            .where(ConferenceCollection.publication_id == publication.id)
        ) or 0
        is_liked = self.db.scalar(
            select(ConferenceLike.id).where(
                ConferenceLike.user_id == viewer.id,
                ConferenceLike.publication_id == publication.id,
            )
        ) is not None
        like_count = self.db.scalar(
            select(func.count())
            .select_from(ConferenceLike)
            .where(ConferenceLike.publication_id == publication.id)
        ) or 0
        review_count = self.db.scalar(
            select(func.count())
            .select_from(ConferenceReview)
            .where(
                ConferenceReview.publication_id == publication.id,
                ConferenceReview.moderation_status
                == ConferenceReviewModerationStatus.VISIBLE,
            )
        ) or 0
        co_create_request_status = self.db.scalar(
            select(ConferenceDerivativeRequest.status)
            .where(
                ConferenceDerivativeRequest.requester_user_id == viewer.id,
                ConferenceDerivativeRequest.source_publication_id == publication.id,
            )
            .order_by(ConferenceDerivativeRequest.created_at.desc())
            .limit(1)
        )
        assert publication.published_at is not None
        return PublicationFeedItemPublic(
            publication_id=publication.id,
            project_id=project.id,
            creation_version_id=version.id,
            title=project.title,
            description=project.description,
            media_type=project.media_type,
            visibility=publication.visibility,
            conference_category=publication.conference_category,
            channel=channel,
            classroom_id=publication.classroom_id,
            classroom_name=classroom.name if classroom else None,
            author_nickname=author.nickname,
            is_owner=publication.owner_user_id == viewer.id,
            version_number=version.version_number,
            published_at=publication.published_at,
            preview_url=preview_url,
            preview_mime_type=mime_type,
            preview_width=width,
            preview_height=height,
            preview_duration_ms=duration_ms,
            preview_url_expires_at=expires_at if preview_url else None,
            ai_assisted=bool(manifest and manifest.ai_assistance_used),
            learning_summary=card.method_summary if card and include_learning else None,
            related_manuals=[
                PublicationRelatedManualPublic(
                    manual_page_id=manual_page_id,
                    page_no=page_no,
                    title=title,
                )
                for manual_page_id, page_no, title in manual_rows
            ],
            learning_card=(
                PublicationLearningCardSummaryPublic(
                    method_summary=card.method_summary,
                    unresolved_questions=card.unresolved_questions,
                )
                if card and include_learning
                else None
            ),
            provenance=(
                PublicationProvenanceSummaryPublic(
                    human_contribution_summary=manifest.human_contribution_summary,
                    ai_assistance_used=manifest.ai_assistance_used,
                    ai_contribution_summary=manifest.ai_contribution_summary,
                    aigc_label_declared=manifest.aigc_label_declared,
                    source_count=source_count,
                )
                if manifest
                else None
            ),
            is_liked=is_liked,
            like_count=like_count,
            is_collected=is_collected,
            collection_count=collection_count,
            review_count=review_count,
            co_create_request_status=(
                co_create_request_status.value
                if co_create_request_status is not None
                else None
            ),
        )

    def _preview_asset(self, version: CreationVersion) -> MediaAsset | None:
        asset_id = version.preview_asset_id
        if asset_id is None:
            asset_id = self.db.scalar(
                select(CreationExportJob.output_asset_id)
                .where(
                    CreationExportJob.creation_version_id == version.id,
                    CreationExportJob.status == CreationExportJobStatus.COMPLETED,
                    CreationExportJob.output_asset_id.is_not(None),
                )
                .order_by(CreationExportJob.completed_at.desc())
                .limit(1)
            )
        if asset_id is None:
            for layer in reversed(version.layer_manifest):
                if layer.get("asset_id"):
                    try:
                        asset_id = uuid.UUID(layer["asset_id"])
                    except (ValueError, TypeError):
                        continue
                    break
        if asset_id is None:
            return None
        return self.db.scalar(
            select(MediaAsset).where(
                MediaAsset.id == asset_id,
                MediaAsset.status == MediaAssetStatus.READY,
            )
        )

    def _classroom_public(
        self,
        classroom: Classroom,
        role: ClassroomRole,
        teacher: User | None,
        joined_at: object | None,
    ) -> ClassroomPublic:
        member_count = self.db.scalar(
            select(func.count(ClassroomMembership.id)).where(
                ClassroomMembership.classroom_id == classroom.id,
                ClassroomMembership.status == ClassroomMembershipStatus.ACTIVE,
            )
        ) or 0
        return ClassroomPublic(
            id=classroom.id,
            name=classroom.name,
            role=role,
            teacher_nickname=teacher.nickname if teacher else "班级创建者",
            member_count=member_count,
            can_submit=role == ClassroomRole.MEMBER,
            joined_at=joined_at,
            created_at=classroom.created_at,
        )
