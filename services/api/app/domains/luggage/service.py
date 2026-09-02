from __future__ import annotations

import hashlib
import logging
import uuid
from datetime import datetime, timedelta

from sqlalchemy import func, select, tuple_
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.core.security import utcnow
from app.domains.catalog.contracts import STATE_LABELS
from app.domains.catalog.models import ManualPage, ManualVolume
from app.domains.creations.models import (
    CreationProject,
    CreationProjectStatus,
    CreationVersion,
    Publication,
    PublicationStatus,
)
from app.domains.learning.contracts import ManualProgressState, MistakeStatus
from app.domains.learning.models import (
    LearningEvidence,
    ManualProgress,
    UserLearningStats,
)
from app.domains.learning.service import LearningService
from app.domains.luggage.cache import LuggageCache
from app.domains.luggage.contracts import (
    BadgeSummaryPublic,
    CreationDisplayStatus,
    CurrentTitlePublic,
    LuggageCreationSectionPublic,
    LuggageCreationItemPublic,
    LuggageDataPublic,
    LuggageEmptyReason,
    LuggageManualItemPublic,
    LuggageManualSectionPublic,
    LuggageMetaPublic,
    LuggageMistakeItemPublic,
    LuggageMistakeSectionPublic,
    LuggagePrivacyPublic,
    LuggageProfilePublic,
    LuggageResponse,
    SignedMediaPublic,
)
from app.domains.media.models import (
    MediaAsset,
    MediaAssetStatus,
    MediaDerivative,
    MediaDerivativeKind,
)
from app.domains.media.storage import ObjectNotFoundError, ObjectStore
from app.domains.mistakes.models import MistakeItem
from app.domains.moderation.models import AppealStatus, ModerationAppeal
from app.domains.profiles.service import ProfileService
from app.models import AgeBand, User


logger = logging.getLogger(__name__)


class LuggageService:
    """Build a consistent personal snapshot from current domain projections."""

    def __init__(
        self,
        *,
        db: Session,
        settings: Settings,
        store: ObjectStore,
        cache: LuggageCache,
    ) -> None:
        self.db = db
        self.settings = settings
        self.store = store
        self.cache = cache
        self.profiles = ProfileService(db=db)
        self.learning = LearningService(db=db)

    def get_snapshot(self, user: User) -> LuggageResponse:
        try:
            cached = self.cache.get(user.id)
        except Exception as exc:
            logger.warning("luggage cache lookup failed: %s", type(exc).__name__)
            cached = None
        if cached is not None:
            return cached

        now = utcnow()
        profile = self.profiles.get_profile(user)
        learning_stats = self.learning.get_stats(user, reference=now)
        total_manuals = self.db.scalar(
            select(func.count(ManualPage.id))
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .where(
                ManualPage.is_listed.is_(True),
                ManualVolume.is_listed.is_(True),
            )
        ) or 0
        state_counts = {state: 0 for state in ManualProgressState}
        progress_counts = self.db.execute(
            select(ManualProgress.state, func.count(ManualProgress.manual_page_id))
            .join(ManualPage, ManualPage.id == ManualProgress.manual_page_id)
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .where(
                ManualProgress.user_id == user.id,
                ManualPage.is_listed.is_(True),
                ManualVolume.is_listed.is_(True),
            )
            .group_by(ManualProgress.state)
        ).all()
        for state, count in progress_counts:
            state_counts[state] = count
        obtained = sum(
            count
            for state, count in state_counts.items()
            if state != ManualProgressState.UNSEEN
        )
        state_counts[ManualProgressState.UNSEEN] = total_manuals - obtained
        recent_progress = self.db.execute(
            select(ManualProgress, ManualPage, ManualVolume, LearningEvidence)
            .join(ManualPage, ManualPage.id == ManualProgress.manual_page_id)
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .outerjoin(
                LearningEvidence,
                LearningEvidence.id == ManualProgress.latest_evidence_id,
            )
            .where(
                ManualProgress.user_id == user.id,
                ManualProgress.state != ManualProgressState.UNSEEN,
                ManualPage.is_listed.is_(True),
                ManualVolume.is_listed.is_(True),
            )
            .order_by(ManualProgress.updated_at.desc())
            .limit(3)
        ).all()
        manual_items = [
            LuggageManualItemPublic(
                id=page.id,
                volume=volume.number,
                style_no=page.style_no,
                title=page.title,
                state=progress.state,
                state_label=STATE_LABELS[progress.state],
                latest_evidence_summary=evidence.summary if evidence else None,
                updated_at=progress.updated_at,
            )
            for progress, page, volume, evidence in recent_progress
        ]
        pending_mistake_filters = (
            MistakeItem.user_id == user.id,
            MistakeItem.status != MistakeStatus.CONSOLIDATED,
        )
        pending_mistakes = self.db.scalar(
            select(func.count(MistakeItem.id)).where(*pending_mistake_filters)
        ) or 0
        recent_mistakes = self.db.execute(
            select(MistakeItem, ManualPage)
            .join(ManualPage, ManualPage.id == MistakeItem.manual_page_id)
            .where(*pending_mistake_filters)
            .order_by(MistakeItem.updated_at.desc())
            .limit(3)
        ).all()
        mistake_items = [
            LuggageMistakeItemPublic(
                id=mistake.id,
                knowledge_point=page.title,
                status=mistake.status,
                manual_page_id=mistake.manual_page_id,
                retry_url=f"/v1/mistakes/{mistake.id}/retry-sessions",
            )
            for mistake, page in recent_mistakes
        ]
        creation_projects = self.db.scalars(
            select(CreationProject)
            .where(
                CreationProject.owner_user_id == user.id,
                CreationProject.status != CreationProjectStatus.DELETED,
            )
            .order_by(CreationProject.updated_at.desc())
        ).all()
        project_ids = [project.id for project in creation_projects]
        publication_by_project: dict = {}
        if project_ids:
            publications = self.db.scalars(
                select(Publication)
                .where(Publication.project_id.in_(project_ids))
                .order_by(Publication.submitted_at.desc())
            ).all()
            for publication in publications:
                publication_by_project.setdefault(
                    publication.project_id, publication
                )

        recent_versioned_projects = [
            project
            for project in creation_projects
            if project.current_version_number is not None
        ][:3]
        version_pairs = [
            (project.id, project.current_version_number)
            for project in recent_versioned_projects
        ]
        version_by_project: dict[uuid.UUID, CreationVersion] = {}
        if version_pairs:
            versions = self.db.scalars(
                select(CreationVersion).where(
                    tuple_(
                        CreationVersion.project_id,
                        CreationVersion.version_number,
                    ).in_(version_pairs)
                )
            ).all()
            version_by_project = {version.project_id: version for version in versions}

        thumbnail_asset_by_project: dict[uuid.UUID, uuid.UUID] = {}
        for project in recent_versioned_projects:
            version = version_by_project.get(project.id)
            asset_id = self._version_thumbnail_asset_id(version) if version else None
            if asset_id is not None:
                thumbnail_asset_by_project[project.id] = asset_id

        thumbnail_asset_ids = set(thumbnail_asset_by_project.values())
        if profile.avatar_asset_id is not None:
            thumbnail_asset_ids.add(profile.avatar_asset_id)
        signed_thumbnail_by_asset = self._sign_thumbnails(thumbnail_asset_ids, now)

        status_counts = {status: 0 for status in CreationDisplayStatus}
        creation_items: list[LuggageCreationItemPublic] = []
        for project in creation_projects:
            publication = publication_by_project.get(project.id)
            display_status = (
                CreationDisplayStatus(publication.status.value)
                if publication
                else CreationDisplayStatus.DRAFT
            )
            status_counts[display_status] += 1
            if project.current_version_number is None or len(creation_items) >= 3:
                continue
            creation_items.append(
                LuggageCreationItemPublic(
                    project_id=project.id,
                    title=project.title,
                    display_status=display_status,
                    current_version=project.current_version_number,
                    thumbnail=signed_thumbnail_by_asset.get(
                        thumbnail_asset_by_project.get(project.id)
                    ),
                    can_revise=(
                        project.status == CreationProjectStatus.ACTIVE
                        and display_status
                        in {
                            CreationDisplayStatus.DRAFT,
                            CreationDisplayStatus.RETURNED,
                            CreationDisplayStatus.PUBLISHED,
                        }
                    ),
                    return_reason=(
                        publication.return_reason_summary
                        if publication
                        and publication.status == PublicationStatus.RETURNED
                        else None
                    ),
                    updated_at=project.updated_at,
                )
            )

        learning_projection = self.db.get(UserLearningStats, user.id)
        learning_version = learning_projection.projection_version if learning_projection else 0
        mistake_version = self.db.scalar(
            select(func.coalesce(func.sum(MistakeItem.row_version), 0)).where(
                MistakeItem.user_id == user.id
            )
        ) or 0
        creation_version = sum(project.row_version for project in creation_projects)
        creation_version += sum(
            publication.row_version for publication in publication_by_project.values()
        )
        pending_appeals = self.db.scalar(
            select(func.count(ModerationAppeal.id)).where(
                ModerationAppeal.appellant_user_id == user.id,
                ModerationAppeal.status == AppealStatus.PENDING,
            )
        ) or 0
        luggage_data = LuggageDataPublic(
            profile=LuggageProfilePublic(
                nickname=profile.nickname,
                avatar=signed_thumbnail_by_asset.get(profile.avatar_asset_id),
                age_band=profile.age_band,
                class_label=profile.class_label,
                anonymous_id=profile.anonymous_id,
                current_title=(
                    CurrentTitlePublic(
                        code=profile.current_title.code,
                        name=profile.current_title.name,
                    )
                    if profile.current_title
                    else None
                ),
                badges=[
                    BadgeSummaryPublic(
                        code=badge.code,
                        name=badge.name,
                        earned_at=badge.earned_at,
                    )
                    for badge in profile.badges
                ],
            ),
            stats=learning_stats,
            manuals=LuggageManualSectionPublic(
                total=total_manuals,
                obtained=obtained,
                counts_by_state=state_counts,
                items=manual_items,
                empty_reason=(
                    LuggageEmptyReason.NO_OBTAINED_MANUALS
                    if obtained == 0
                    else None
                ),
                detail_url="/v1/manuals",
            ),
            mistakes=LuggageMistakeSectionPublic(
                pending_count=pending_mistakes,
                items=mistake_items,
                empty_reason=(
                    LuggageEmptyReason.NO_MISTAKES
                    if pending_mistakes == 0
                    else None
                ),
                detail_url="/v1/mistakes",
            ),
            creations=LuggageCreationSectionPublic(
                counts_by_status=status_counts,
                items=creation_items,
                empty_reason=(
                    LuggageEmptyReason.NO_CREATIONS
                    if not creation_projects
                    else None
                ),
                detail_url="/v1/me/creation-projects",
            ),
            privacy=LuggagePrivacyPublic(
                guardian_controls_active=user.age_band != AgeBand.ADULT,
                pending_appeal_count=pending_appeals,
                privacy_settings_url="/v1/me/privacy-settings",
            ),
        )
        # Hash the actual client-visible projection. This also changes when media
        # becomes READY or a catalog label changes without a profile row update.
        etag_value = hashlib.sha256(luggage_data.model_dump_json().encode()).hexdigest()[
            :24
        ]
        etag = f'W/"{etag_value}"'
        snapshot = LuggageResponse(
            data=luggage_data,
            meta=LuggageMetaPublic(
                generated_at=now,
                snapshot_version=(
                    profile.row_version
                    + learning_version
                    + mistake_version
                    + creation_version
                    + pending_appeals
                ),
                etag=etag,
            ),
        )
        try:
            self.cache.set(user.id, snapshot)
        except Exception as exc:
            logger.warning("luggage cache population failed: %s", type(exc).__name__)
        return snapshot

    @staticmethod
    def _version_thumbnail_asset_id(version: CreationVersion) -> uuid.UUID | None:
        if version.preview_asset_id is not None:
            return version.preview_asset_id
        for layer in reversed(version.layer_manifest):
            raw_asset_id = layer.get("asset_id") if layer.get("visible", True) else None
            if not raw_asset_id:
                continue
            try:
                return uuid.UUID(str(raw_asset_id))
            except ValueError:
                continue
        return None

    def _sign_thumbnails(
        self,
        asset_ids: set[uuid.UUID],
        now: datetime,
    ) -> dict[uuid.UUID, SignedMediaPublic]:
        if not asset_ids:
            return {}
        rows = self.db.execute(
            select(MediaAsset, MediaDerivative)
            .join(MediaDerivative, MediaDerivative.asset_id == MediaAsset.id)
            .where(
                MediaAsset.id.in_(asset_ids),
                MediaAsset.status == MediaAssetStatus.READY,
                MediaDerivative.kind == MediaDerivativeKind.THUMBNAIL_320,
            )
        ).all()
        expires_delta = timedelta(minutes=self.settings.media_download_ttl_minutes)
        result: dict[uuid.UUID, SignedMediaPublic] = {}
        for asset, derivative in rows:
            try:
                url = self.store.presign_private_download(
                    derivative.storage_key,
                    expires=expires_delta,
                )
            except ObjectNotFoundError:
                continue
            result[asset.id] = SignedMediaPublic(
                asset_id=asset.id,
                url=url,
                expires_at=now + expires_delta,
            )
        return result
