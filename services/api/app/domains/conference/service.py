from __future__ import annotations

import uuid
from datetime import UTC, datetime, timedelta

from sqlalchemy import and_, func, or_, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.catalog.models import ManualPage, ManualVolume
from app.domains.conference.contracts import (
    ConferenceCollectionListPublic,
    ConferenceCollectionPublic,
    ConferenceLikePublic,
    ConferenceDerivativeAuthorizationPublic,
    ConferenceDerivativeDecision,
    ConferenceDerivativeRequestCreate,
    ConferenceDerivativeRequestListPublic,
    ConferenceDerivativeRequestPublic,
    ConferenceLetterListPublic,
    ConferenceLetterPublic,
    ConferenceMatchAnswerCreate,
    ConferenceMatchAnswerPublic,
    ConferenceMatchAnswerSubmittedPublic,
    ConferenceMatchDetailPublic,
    ConferenceMatchEvaluationCreate,
    ConferenceMatchEvaluationPublic,
    ConferenceMatchJoin,
    ConferenceMatchJudgmentCandidatePublic,
    ConferenceMatchJudgmentCreate,
    ConferenceMatchJudgmentPublic,
    ConferenceMatchJudgmentQueuePublic,
    ConferenceMatchJudgmentStatus,
    ConferenceMatchHistoryOutcome,
    ConferenceMatchOutcome,
    ConferenceMatchParticipantResultPublic,
    ConferenceMatchPerspective,
    ConferenceMatchOpponentPublic,
    ConferenceMatchQueuePhase,
    ConferenceMatchQueuePublic,
    ConferenceMatchQueueState,
    ConferenceMatchQuestionPublic,
    ConferenceMatchProgressPublic,
    ConferenceMatchReflectionCreate,
    ConferenceMatchReflectionPublic,
    ConferenceMatchReflectionStatus,
    ConferenceMatchRecordListPublic,
    ConferenceMatchRecordPublic,
    ConferenceMatchRecordSummaryPublic,
    ConferenceMatchReportCreate,
    ConferenceMatchReportDecision,
    ConferenceMatchReportInternalPublic,
    ConferenceMatchReportListPublic,
    ConferenceMatchReportPublic,
    ConferenceMatchResultPublic,
    ConferenceMatchTeacherEvaluationCreate,
    ConferenceReviewCreate,
    ConferenceReviewAdoptionCreate,
    ConferenceReviewDecision,
    ConferenceReviewListPublic,
    ConferenceReviewPublic,
    ConferenceReviewReportCreate,
    ConferenceReviewReportDecision,
    ConferenceReviewReportInternalPublic,
    ConferenceReviewReportListPublic,
    ConferenceReviewReportPublic,
    DerivativeAuthorDecision,
    DerivativeRequestScope,
    MatchReportDecision,
    ReviewAuthorAction,
    ReviewReportDecision,
)
from app.domains.conference.models import (
    ConferenceCollection,
    ConferenceDerivativeAuthorization,
    ConferenceDerivativeRequest,
    ConferenceLetter,
    ConferenceLetterCategory,
    ConferenceLike,
    ConferenceMatch,
    ConferenceMatchAnswer,
    ConferenceMatchEndReason,
    ConferenceMatchEvaluation,
    ConferenceMatchEvaluationKind,
    ConferenceMatchQuestion,
    ConferenceMatchQuestionKind,
    ConferenceMatchQueue,
    ConferenceMatchQueueStatus,
    ConferenceMatchReport,
    ConferenceMatchReportStatus,
    ConferenceMatchReflection,
    ConferenceMatchStatus,
    ConferenceReview,
    ConferenceReviewModerationStatus,
    ConferenceReviewReport,
    ConferenceReviewReportStatus,
    ConferenceReviewStatus,
    ConferenceReviewTemplate,
    DerivativeAuthorizationStatus,
    DerivativeRequestStatus,
)
from app.domains.conference.judge import ConferenceJudge
from app.domains.creations.models import (
    CreationVersion,
    CreationVisibility,
    Publication,
    PublicationStatus,
)
from app.domains.distribution.contracts import PublicationFeedItemPublic
from app.domains.distribution.service import DistributionService
from app.domains.moderation.audit import add_audit_event
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.conference.safety import ensure_public_interaction_text
from app.models import BlacklistEntry, User, UserPreference


AUTHORIZATION_VERSION = "conference-derivative-v1"
MATCH_QUEUE_TTL = timedelta(minutes=5)


class ConferenceService:
    def __init__(
        self, *, db: Session, distribution: DistributionService, request_id: str
    ) -> None:
        self.db = db
        self.distribution = distribution
        self.request_id = request_id

    def create_review(
        self,
        user: User,
        publication_id: uuid.UUID,
        payload: ConferenceReviewCreate,
    ) -> ConferenceReviewPublic:
        publication = self._require_visible_community_publication(user, publication_id)
        if publication.owner_user_id == user.id:
            raise ApiError(403, "SELF_REVIEW_FORBIDDEN", "不能给自己的作品留言")
        ensure_public_interaction_text(field="content", value=payload.content)
        review = ConferenceReview(
            id=uuid.uuid4(),
            publication_id=publication.id,
            reviewer_user_id=user.id,
            template=payload.template,
            content=payload.content,
            status=ConferenceReviewStatus.PENDING,
        )
        self.db.add(review)
        self._audit(
            user=user,
            action="CONFERENCE_REVIEW_CREATED",
            target_type="CONFERENCE_REVIEW",
            target_id=review.id,
            result="PENDING",
            safe_diff={
                "publication_id": str(publication.id),
                "template": payload.template.value,
            },
        )
        self._create_letter(
            recipient_user_id=publication.owner_user_id,
            category=ConferenceLetterCategory.REVIEW,
            title="作品收到新评语",
            body="你的大会作品收到一条结构化评语，去看看对方发现了什么。",
            action_type="CONFERENCE_REVIEW",
            action_id=review.id,
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            existing = self.db.scalar(
                select(ConferenceReview).where(
                    ConferenceReview.publication_id == publication.id,
                    ConferenceReview.reviewer_user_id == user.id,
                    ConferenceReview.template == payload.template,
                )
            )
            if existing is not None:
                raise ApiError(
                    409, "REVIEW_TEMPLATE_EXISTS", "该作品的同类评语已提交"
                ) from exc
            raise ApiError(409, "REVIEW_WRITE_CONFLICT", "评语写入失败，请重试") from exc
        return self._review_public(review, user)

    def get_work(
        self, user: User, publication_id: uuid.UUID
    ) -> PublicationFeedItemPublic:
        return self._require_visible_community_item(user, publication_id)

    def add_like(self, user: User, publication_id: uuid.UUID) -> ConferenceLikePublic:
        self._require_visible_community_publication(user, publication_id)
        existing = self.db.scalar(
            select(ConferenceLike).where(
                ConferenceLike.user_id == user.id,
                ConferenceLike.publication_id == publication_id,
            )
        )
        if existing is not None:
            return ConferenceLikePublic(
                publication_id=existing.publication_id,
                liked_at=existing.created_at,
            )
        like = ConferenceLike(
            user_id=user.id,
            publication_id=publication_id,
        )
        self.db.add(like)
        try:
            self.db.commit()
        except IntegrityError:
            self.db.rollback()
            existing = self.db.scalar(
                select(ConferenceLike).where(
                    ConferenceLike.user_id == user.id,
                    ConferenceLike.publication_id == publication_id,
                )
            )
            if existing is None:
                raise ApiError(409, "LIKE_WRITE_CONFLICT", "点赞失败，请重试")
            like = existing
        return ConferenceLikePublic(
            publication_id=like.publication_id,
            liked_at=like.created_at,
        )

    def remove_like(self, user: User, publication_id: uuid.UUID) -> None:
        like = self.db.scalar(
            select(ConferenceLike).where(
                ConferenceLike.user_id == user.id,
                ConferenceLike.publication_id == publication_id,
            )
        )
        if like is not None:
            self.db.delete(like)
            self.db.commit()

    def list_reviews(
        self, user: User, publication_id: uuid.UUID, *, limit: int
    ) -> ConferenceReviewListPublic:
        self._require_visible_community_publication(user, publication_id)
        blocked_ids, blocking_ids = self._block_lists(user.id)
        rows = self.db.execute(
            select(ConferenceReview, User)
            .join(User, User.id == ConferenceReview.reviewer_user_id)
            .where(
                ConferenceReview.publication_id == publication_id,
                ConferenceReview.moderation_status
                == ConferenceReviewModerationStatus.VISIBLE,
                ConferenceReview.reviewer_user_id.not_in(blocked_ids),
                ConferenceReview.reviewer_user_id.not_in(blocking_ids),
            )
            .order_by(ConferenceReview.created_at.desc(), ConferenceReview.id.desc())
            .limit(limit)
        ).all()
        return ConferenceReviewListPublic(
            items=[self._review_public(review, reviewer) for review, reviewer in rows]
        )

    def decide_review(
        self,
        user: User,
        review_id: uuid.UUID,
        payload: ConferenceReviewDecision,
    ) -> ConferenceReviewPublic:
        row = self.db.execute(
            select(ConferenceReview, Publication)
            .join(Publication, Publication.id == ConferenceReview.publication_id)
            .where(ConferenceReview.id == review_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "REVIEW_NOT_FOUND", "评语不存在")
        review, publication = row
        if publication.owner_user_id != user.id:
            raise ApiError(403, "REVIEW_AUTHOR_REQUIRED", "只有作品作者可以处理评语")
        if review.status != ConferenceReviewStatus.PENDING:
            raise ApiError(409, "REVIEW_ALREADY_HANDLED", "该评语已处理")
        if review.moderation_status != ConferenceReviewModerationStatus.VISIBLE:
            raise ApiError(409, "REVIEW_UNAVAILABLE", "该评语当前不可处理")
        if review.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "评语已更新，请刷新后重试")
        ensure_public_interaction_text(field="reply", value=payload.reply)
        review.status = {
            ReviewAuthorAction.ACCEPT: ConferenceReviewStatus.ACCEPTED,
            ReviewAuthorAction.THINK: ConferenceReviewStatus.THINKING,
            ReviewAuthorAction.REPLY: ConferenceReviewStatus.REPLIED,
        }[payload.action]
        review.author_reply = payload.reply
        review.handled_at = utcnow()
        review.row_version += 1
        self._audit(
            user=user,
            action="CONFERENCE_REVIEW_DECIDED",
            target_type="CONFERENCE_REVIEW",
            target_id=review.id,
            result=review.status.value,
            safe_diff={"row_version": review.row_version},
        )
        self._create_letter(
            recipient_user_id=review.reviewer_user_id,
            category=ConferenceLetterCategory.REVIEW,
            title="评语有了新回应",
            body="作品作者已经处理了你的结构化评语。",
            action_type="CONFERENCE_REVIEW",
            action_id=review.id,
        )
        self.db.commit()
        reviewer = self.db.get(User, review.reviewer_user_id)
        assert reviewer is not None
        return self._review_public(review, reviewer)

    def link_review_adoption(
        self,
        user: User,
        review_id: uuid.UUID,
        payload: ConferenceReviewAdoptionCreate,
    ) -> ConferenceReviewPublic:
        row = self.db.execute(
            select(ConferenceReview, Publication)
            .join(Publication, Publication.id == ConferenceReview.publication_id)
            .where(ConferenceReview.id == review_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "REVIEW_NOT_FOUND", "评语不存在")
        review, publication = row
        if publication.owner_user_id != user.id:
            raise ApiError(403, "REVIEW_AUTHOR_REQUIRED", "只有作品作者可以登记采纳结果")
        if review.template != ConferenceReviewTemplate.SUGGESTION:
            raise ApiError(409, "REVIEW_ADOPTION_TEMPLATE_REQUIRED", "只有建议类评语可以登记采纳版本")
        if review.status != ConferenceReviewStatus.ACCEPTED:
            raise ApiError(409, "REVIEW_NOT_ACCEPTED", "请先采纳建议，再登记改进版本")
        if review.moderation_status != ConferenceReviewModerationStatus.VISIBLE:
            raise ApiError(409, "REVIEW_UNAVAILABLE", "该评语当前不可登记采纳版本")
        ensure_public_interaction_text(field="summary", value=payload.summary)
        if review.adopted_in_creation_version_id is not None:
            if (
                review.adopted_in_creation_version_id == payload.creation_version_id
                and review.adoption_summary == payload.summary
            ):
                reviewer = self.db.get(User, review.reviewer_user_id)
                assert reviewer is not None
                return self._review_public(review, reviewer)
            raise ApiError(409, "REVIEW_ADOPTION_ALREADY_LINKED", "该建议已关联改进版本")
        if review.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "评语已更新，请刷新后重试")
        version = self.db.get(CreationVersion, payload.creation_version_id)
        published_version = self.db.get(CreationVersion, publication.creation_version_id)
        if (
            version is None
            or published_version is None
            or version.project_id != publication.project_id
            or version.version_number <= published_version.version_number
        ):
            raise ApiError(
                409,
                "INVALID_REVIEW_ADOPTION_VERSION",
                "采纳结果必须关联同一作品中晚于参会版本的新版本",
            )
        review.adopted_in_creation_version_id = version.id
        review.adoption_summary = payload.summary
        review.adopted_at = utcnow()
        review.row_version += 1
        self._audit(
            user=user,
            action="CONFERENCE_REVIEW_ADOPTION_LINKED",
            target_type="CONFERENCE_REVIEW",
            target_id=review.id,
            result="LINKED",
            safe_diff={
                "creation_version_id": str(version.id),
                "version_number": version.version_number,
                "row_version": review.row_version,
            },
        )
        self._create_letter(
            recipient_user_id=review.reviewer_user_id,
            category=ConferenceLetterCategory.REVIEW,
            title="你的建议已形成新版本",
            body="作品作者采纳了你的建议，并关联了改进后的作品版本。",
            action_type="CONFERENCE_REVIEW",
            action_id=review.id,
        )
        self.db.commit()
        reviewer = self.db.get(User, review.reviewer_user_id)
        assert reviewer is not None
        return self._review_public(review, reviewer)

    def report_review(
        self,
        user: User,
        review_id: uuid.UUID,
        payload: ConferenceReviewReportCreate,
    ) -> ConferenceReviewReportPublic:
        row = self.db.execute(
            select(ConferenceReview, Publication)
            .join(Publication, Publication.id == ConferenceReview.publication_id)
            .where(ConferenceReview.id == review_id)
        ).one_or_none()
        if row is None:
            raise ApiError(404, "REVIEW_NOT_FOUND", "评语不存在")
        review, publication = row
        self._require_visible_community_publication(user, publication.id)
        if review.moderation_status != ConferenceReviewModerationStatus.VISIBLE:
            raise ApiError(409, "REVIEW_UNAVAILABLE", "该评语当前不可举报")
        if review.reviewer_user_id == user.id:
            raise ApiError(403, "SELF_REPORT_FORBIDDEN", "不能举报自己的评语")
        existing = self.db.scalar(
            select(ConferenceReviewReport).where(
                ConferenceReviewReport.review_id == review.id,
                ConferenceReviewReport.reporter_user_id == user.id,
            )
        )
        if existing is not None:
            return self._review_report_public(existing)
        report = ConferenceReviewReport(
            id=uuid.uuid4(),
            review_id=review.id,
            reporter_user_id=user.id,
            reason=payload.reason,
            details=payload.details,
            status=ConferenceReviewReportStatus.PENDING,
        )
        self.db.add(report)
        self._audit(
            user=user,
            action="CONFERENCE_REVIEW_REPORTED",
            target_type="CONFERENCE_REVIEW_REPORT",
            target_id=report.id,
            result="PENDING",
            safe_diff={
                "review_id": str(review.id),
                "reason": payload.reason.value,
            },
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            existing = self.db.scalar(
                select(ConferenceReviewReport).where(
                    ConferenceReviewReport.review_id == review.id,
                    ConferenceReviewReport.reporter_user_id == user.id,
                )
            )
            if existing is not None:
                return self._review_report_public(existing)
            raise ApiError(409, "REVIEW_REPORT_CONFLICT", "举报提交发生冲突，请重试") from exc
        return self._review_report_public(report)

    def decide_review_report(
        self,
        report_id: uuid.UUID,
        payload: ConferenceReviewReportDecision,
    ) -> ConferenceReviewReportPublic:
        row = self.db.execute(
            select(ConferenceReviewReport, ConferenceReview)
            .join(ConferenceReview, ConferenceReview.id == ConferenceReviewReport.review_id)
            .where(ConferenceReviewReport.id == report_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "REVIEW_REPORT_NOT_FOUND", "评语举报不存在")
        report, review = row
        if report.status != ConferenceReviewReportStatus.PENDING:
            raise ApiError(409, "REVIEW_REPORT_ALREADY_DECIDED", "该举报已处理")
        if report.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "举报记录已更新，请刷新后重试")
        now = utcnow()
        if payload.decision == ReviewReportDecision.REMOVE:
            report.status = ConferenceReviewReportStatus.RESOLVED
            review.moderation_status = ConferenceReviewModerationStatus.REMOVED
        else:
            report.status = ConferenceReviewReportStatus.DISMISSED
        report.reviewer_reference = payload.reviewer_reference
        report.resolution_summary = payload.resolution_summary
        report.resolved_at = now
        report.row_version += 1
        self._audit(
            user=None,
            action="CONFERENCE_REVIEW_REPORT_DECIDED",
            target_type="CONFERENCE_REVIEW_REPORT",
            target_id=report.id,
            result=report.status.value,
            safe_diff={
                "review_id": str(review.id),
                "decision": payload.decision.value,
                "reviewer_reference": payload.reviewer_reference,
                "row_version": report.row_version,
            },
            actor_type="INTERNAL_WORKER",
        )
        self.db.commit()
        return self._review_report_public(report)

    def list_review_reports(
        self, *, report_status: ConferenceReviewReportStatus, limit: int
    ) -> ConferenceReviewReportListPublic:
        rows = self.db.execute(
            select(ConferenceReviewReport, ConferenceReview)
            .join(ConferenceReview, ConferenceReview.id == ConferenceReviewReport.review_id)
            .where(ConferenceReviewReport.status == report_status)
            .order_by(
                ConferenceReviewReport.created_at,
                ConferenceReviewReport.id,
            )
            .limit(limit)
        ).all()
        return ConferenceReviewReportListPublic(
            items=[
                ConferenceReviewReportInternalPublic(
                    **self._review_report_public(report).model_dump(),
                    publication_id=review.publication_id,
                    review_author_user_id=review.reviewer_user_id,
                    review_template=review.template,
                    review_content=review.content,
                    report_details=report.details,
                )
                for report, review in rows
            ]
        )

    def add_collection(
        self, user: User, publication_id: uuid.UUID
    ) -> ConferenceCollectionPublic:
        item = self._require_visible_community_item(user, publication_id)
        collection = self.db.scalar(
            select(ConferenceCollection).where(
                ConferenceCollection.user_id == user.id,
                ConferenceCollection.publication_id == publication_id,
            )
        )
        if collection is None:
            collection = ConferenceCollection(
                id=uuid.uuid4(), user_id=user.id, publication_id=publication_id
            )
            self.db.add(collection)
            self._audit(
                user=user,
                action="CONFERENCE_COLLECTION_ADDED",
                target_type="CONFERENCE_COLLECTION",
                target_id=collection.id,
                result="SUCCESS",
                safe_diff={"publication_id": str(publication_id)},
            )
            try:
                self.db.commit()
            except IntegrityError as exc:
                self.db.rollback()
                collection = self.db.scalar(
                    select(ConferenceCollection).where(
                        ConferenceCollection.user_id == user.id,
                        ConferenceCollection.publication_id == publication_id,
                    )
                )
                if collection is None:
                    raise ApiError(409, "COLLECTION_CONFLICT", "收藏发生冲突，请重试") from exc
        return ConferenceCollectionPublic(
            publication_id=collection.publication_id,
            saved_at=collection.created_at,
            work=item,
        )

    def list_collections(self, user: User, *, limit: int) -> ConferenceCollectionListPublic:
        collections = self.db.scalars(
            select(ConferenceCollection)
            .where(ConferenceCollection.user_id == user.id)
            .order_by(ConferenceCollection.created_at.desc(), ConferenceCollection.id.desc())
            .limit(limit)
        ).all()
        items: list[ConferenceCollectionPublic] = []
        for collection in collections:
            work = self.distribution.community_item(user, collection.publication_id)
            if work is not None:
                items.append(
                    ConferenceCollectionPublic(
                        publication_id=collection.publication_id,
                        saved_at=collection.created_at,
                        work=work,
                    )
                )
        return ConferenceCollectionListPublic(items=items)

    def remove_collection(self, user: User, publication_id: uuid.UUID) -> None:
        collection = self.db.scalar(
            select(ConferenceCollection).where(
                ConferenceCollection.user_id == user.id,
                ConferenceCollection.publication_id == publication_id,
            )
        )
        if collection is not None:
            self._audit(
                user=user,
                action="CONFERENCE_COLLECTION_REMOVED",
                target_type="CONFERENCE_COLLECTION",
                target_id=collection.id,
                result="SUCCESS",
                safe_diff={"publication_id": str(publication_id)},
            )
            self.db.delete(collection)
            self.db.commit()

    def create_derivative_request(
        self, user: User, payload: ConferenceDerivativeRequestCreate
    ) -> ConferenceDerivativeRequestPublic:
        item = self._require_visible_community_item(user, payload.source_publication_id)
        source = self.db.scalar(
            select(Publication)
            .where(Publication.id == payload.source_publication_id)
            .with_for_update()
        )
        assert source is not None
        if (
            source.status != PublicationStatus.PUBLISHED
            or source.visibility != CreationVisibility.COMMUNITY
        ):
            raise ApiError(404, "COMMUNITY_WORK_NOT_FOUND", "公开作品不存在或暂不可见")
        if source.owner_user_id == user.id:
            raise ApiError(403, "SELF_DERIVATIVE_FORBIDDEN", "不能向自己申请同门改造授权")
        ensure_public_interaction_text(
            field="requested_use", value=payload.requested_use
        )
        source_author = self.db.get(User, source.owner_user_id)
        assert source_author is not None
        request = ConferenceDerivativeRequest(
            id=uuid.uuid4(),
            source_publication_id=source.id,
            source_creation_version_id=source.creation_version_id,
            source_author_user_id=source.owner_user_id,
            requester_user_id=user.id,
            source_title=item.title,
            source_author_nickname=source_author.nickname,
            requested_use=payload.requested_use,
            status=DerivativeRequestStatus.PENDING,
        )
        self.db.add(request)
        self._audit(
            user=user,
            action="CONFERENCE_DERIVATIVE_REQUEST_CREATED",
            target_type="CONFERENCE_DERIVATIVE_REQUEST",
            target_id=request.id,
            result="PENDING",
            safe_diff={
                "source_publication_id": str(source.id),
                "source_creation_version_id": str(source.creation_version_id),
            },
        )
        self._create_letter(
            recipient_user_id=source.owner_user_id,
            category=ConferenceLetterCategory.DERIVATIVE,
            title="收到同门改造申请",
            body="有人希望基于你的大会作品继续创作，请查看用途说明并决定是否授权。",
            action_type="DERIVATIVE_REQUEST",
            action_id=request.id,
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            existing = self.db.scalar(
                select(ConferenceDerivativeRequest).where(
                    ConferenceDerivativeRequest.requester_user_id == user.id,
                    ConferenceDerivativeRequest.source_publication_id == source.id,
                )
            )
            if existing is not None:
                raise ApiError(
                    409, "DERIVATIVE_REQUEST_EXISTS", "已向该作品申请过授权"
                ) from exc
            raise ApiError(
                409, "DERIVATIVE_REQUEST_WRITE_CONFLICT", "授权申请写入失败，请重试"
            ) from exc
        return self._derivative_request_public(request)

    def list_derivative_requests(
        self, user: User, *, scope: DerivativeRequestScope
    ) -> ConferenceDerivativeRequestListPublic:
        owner_column = (
            ConferenceDerivativeRequest.requester_user_id
            if scope == DerivativeRequestScope.REQUESTED
            else ConferenceDerivativeRequest.source_author_user_id
        )
        requests = self.db.scalars(
            select(ConferenceDerivativeRequest)
            .where(owner_column == user.id)
            .order_by(
                ConferenceDerivativeRequest.created_at.desc(),
                ConferenceDerivativeRequest.id.desc(),
            )
        ).all()
        return ConferenceDerivativeRequestListPublic(
            items=[self._derivative_request_public(request) for request in requests]
        )

    def decide_derivative_request(
        self,
        user: User,
        request_id: uuid.UUID,
        payload: ConferenceDerivativeDecision,
    ) -> ConferenceDerivativeRequestPublic:
        request = self.db.scalar(
            select(ConferenceDerivativeRequest)
            .where(ConferenceDerivativeRequest.id == request_id)
            .with_for_update()
        )
        if request is None:
            raise ApiError(404, "DERIVATIVE_REQUEST_NOT_FOUND", "授权申请不存在")
        if request.source_author_user_id != user.id:
            raise ApiError(403, "DERIVATIVE_AUTHOR_REQUIRED", "只有原作者可以处理授权申请")
        if request.status != DerivativeRequestStatus.PENDING:
            raise ApiError(409, "DERIVATIVE_REQUEST_ALREADY_DECIDED", "该授权申请已处理")
        source = self.db.scalar(
            select(Publication)
            .where(Publication.id == request.source_publication_id)
            .with_for_update()
        )
        if (
            source is None
            or source.status != PublicationStatus.PUBLISHED
            or source.visibility != CreationVisibility.COMMUNITY
        ):
            raise ApiError(409, "DERIVATIVE_SOURCE_UNAVAILABLE", "原作品已撤回或不再公开")
        ensure_public_interaction_text(field="note", value=payload.note)
        now = utcnow()
        request.status = (
            DerivativeRequestStatus.APPROVED
            if payload.decision == DerivativeAuthorDecision.APPROVE
            else DerivativeRequestStatus.REJECTED
        )
        request.author_decision_note = payload.note
        request.decided_at = now
        if payload.decision == DerivativeAuthorDecision.APPROVE:
            self.db.add(
                ConferenceDerivativeAuthorization(
                    id=uuid.uuid4(),
                    request_id=request.id,
                    source_publication_id=request.source_publication_id,
                    source_creation_version_id=request.source_creation_version_id,
                    source_author_user_id=request.source_author_user_id,
                    requester_user_id=request.requester_user_id,
                    authorization_version=AUTHORIZATION_VERSION,
                    status=DerivativeAuthorizationStatus.ACTIVE,
                    granted_at=now,
                )
            )
        self._audit(
            user=user,
            action="CONFERENCE_DERIVATIVE_REQUEST_DECIDED",
            target_type="CONFERENCE_DERIVATIVE_REQUEST",
            target_id=request.id,
            result=request.status.value,
            safe_diff={"decision": payload.decision.value},
        )
        self._create_letter(
            recipient_user_id=request.requester_user_id,
            category=ConferenceLetterCategory.DERIVATIVE,
            title="同门改造申请已处理",
            body=(
                "原作者已同意你的改造申请，可以按授权范围开始创作。"
                if request.status == DerivativeRequestStatus.APPROVED
                else "原作者暂未同意你的改造申请，可调整思路后再创作原创版本。"
            ),
            action_type="DERIVATIVE_REQUEST",
            action_id=request.id,
        )
        self.db.commit()
        return self._derivative_request_public(request)

    def revoke_derivative_authorization(
        self, user: User, authorization_id: uuid.UUID
    ) -> ConferenceDerivativeAuthorizationPublic:
        authorization = self.db.scalar(
            select(ConferenceDerivativeAuthorization)
            .where(ConferenceDerivativeAuthorization.id == authorization_id)
            .with_for_update()
        )
        if authorization is None:
            raise ApiError(404, "DERIVATIVE_AUTHORIZATION_NOT_FOUND", "授权记录不存在")
        if authorization.source_author_user_id != user.id:
            raise ApiError(403, "DERIVATIVE_AUTHOR_REQUIRED", "只有原作者可以撤回授权")
        if authorization.status != DerivativeAuthorizationStatus.ACTIVE:
            raise ApiError(409, "DERIVATIVE_AUTHORIZATION_REVOKED", "该授权已撤回")
        authorization.status = DerivativeAuthorizationStatus.REVOKED
        authorization.revoked_at = utcnow()
        self._audit(
            user=user,
            action="CONFERENCE_DERIVATIVE_AUTHORIZATION_REVOKED",
            target_type="CONFERENCE_DERIVATIVE_AUTHORIZATION",
            target_id=authorization.id,
            result="REVOKED",
            safe_diff={
                "source_publication_id": str(authorization.source_publication_id)
            },
        )
        self._create_letter(
            recipient_user_id=authorization.requester_user_id,
            category=ConferenceLetterCategory.DERIVATIVE,
            title="同门改造授权已撤回",
            body="原作者已撤回该作品的改造授权，未发布版本不能再引用这份授权。",
            action_type="DERIVATIVE_AUTHORIZATION",
            action_id=authorization.id,
        )
        self.db.commit()
        return self._authorization_public(authorization)

    def get_match_queue(self, user: User) -> ConferenceMatchQueuePublic:
        entry = self._active_queue_entry(user.id)
        return self._queue_public(entry)

    def join_match_queue(
        self, user: User, payload: ConferenceMatchJoin
    ) -> ConferenceMatchQueuePublic:
        self.db.scalar(select(User.id).where(User.id == user.id).with_for_update())
        entry = self._active_queue_entry(user.id)
        if entry is not None:
            return self._queue_public(entry)
        manual_page = self._require_manual_page(payload.manual_page_id)
        blocked_ids, blocking_ids = self._block_lists(user.id)
        now = utcnow()
        candidate = self.db.scalar(
            select(ConferenceMatchQueue)
            .where(
                ConferenceMatchQueue.manual_page_id == payload.manual_page_id,
                ConferenceMatchQueue.age_band == user.age_band,
                ConferenceMatchQueue.status == ConferenceMatchQueueStatus.WAITING,
                ConferenceMatchQueue.expires_at.is_not(None),
                ConferenceMatchQueue.expires_at > now,
                ConferenceMatchQueue.user_id != user.id,
                ConferenceMatchQueue.user_id.not_in(blocked_ids),
                ConferenceMatchQueue.user_id.not_in(blocking_ids),
            )
            .order_by(ConferenceMatchQueue.joined_at, ConferenceMatchQueue.id)
            .with_for_update()
            .limit(1)
        )
        entry = ConferenceMatchQueue(
            id=uuid.uuid4(),
            user_id=user.id,
            manual_page_id=payload.manual_page_id,
            age_band=user.age_band,
            status=ConferenceMatchQueueStatus.WAITING,
            expires_at=now + MATCH_QUEUE_TTL,
        )
        self.db.add(entry)
        if candidate is not None:
            match = ConferenceMatch(
                id=uuid.uuid4(),
                manual_page_id=payload.manual_page_id,
                age_band=user.age_band,
                participant_a_user_id=candidate.user_id,
                participant_b_user_id=user.id,
                status=ConferenceMatchStatus.ACTIVE,
                question_count=3,
                matched_at=now,
            )
            self.db.add(match)
            self._snapshot_match_questions(match, manual_page)
            candidate.status = ConferenceMatchQueueStatus.MATCHED
            candidate.match_id = match.id
            candidate.expires_at = None
            candidate.updated_at = now
            entry.status = ConferenceMatchQueueStatus.MATCHED
            entry.match_id = match.id
            entry.expires_at = None
            entry.updated_at = now
            for participant_user_id in (
                match.participant_a_user_id,
                match.participant_b_user_id,
            ):
                self._create_letter(
                    recipient_user_id=participant_user_id,
                    category=ConferenceLetterCategory.MATCH,
                    title="切磋已经开始",
                    body="已为你匹配同龄对手，本局共有 3 道题，请完成答案和理由。",
                    action_type="CONFERENCE_MATCH",
                    action_id=match.id,
                )
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_QUEUE_JOINED",
            target_type="CONFERENCE_MATCH_QUEUE",
            target_id=entry.id,
            result=entry.status.value,
            safe_diff={
                "manual_page_id": str(payload.manual_page_id),
                "match_id": str(entry.match_id) if entry.match_id else None,
            },
        )
        self.db.commit()
        return self._queue_public(entry)

    def get_match(
        self, user: User, match_id: uuid.UUID
    ) -> ConferenceMatchDetailPublic:
        match = self._require_match_participant(user, match_id)
        return self._match_detail_public(match, user.id)

    def answer_match_question(
        self,
        user: User,
        match_id: uuid.UUID,
        payload: ConferenceMatchAnswerCreate,
    ) -> ConferenceMatchAnswerSubmittedPublic:
        match = self.db.scalar(
            select(ConferenceMatch)
            .where(ConferenceMatch.id == match_id)
            .with_for_update()
        )
        if match is None or user.id not in self._participant_ids(match):
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        question = self.db.scalar(
            select(ConferenceMatchQuestion).where(
                ConferenceMatchQuestion.id == payload.question_id,
                ConferenceMatchQuestion.match_id == match.id,
            )
        )
        if question is None:
            raise ApiError(404, "MATCH_QUESTION_NOT_FOUND", "切磋题目不存在")
        existing = self.db.scalar(
            select(ConferenceMatchAnswer).where(
                ConferenceMatchAnswer.question_id == question.id,
                ConferenceMatchAnswer.participant_user_id == user.id,
            )
        )
        if existing is not None:
            if existing.answer != payload.answer or existing.reason != payload.reason:
                raise ApiError(409, "MATCH_ANSWER_EXISTS", "该题已经作答，不能覆盖")
            return self._answer_submission_public(match, existing, user.id)
        if match.status != ConferenceMatchStatus.ACTIVE:
            raise ApiError(409, "MATCH_ANSWERING_CLOSED", "本局已停止作答")
        ensure_public_interaction_text(field="answer", value=payload.answer)
        ensure_public_interaction_text(field="reason", value=payload.reason)
        answer = ConferenceMatchAnswer(
            id=uuid.uuid4(),
            match_id=match.id,
            question_id=question.id,
            participant_user_id=user.id,
            answer=payload.answer,
            reason=payload.reason,
        )
        self.db.add(answer)
        self.db.flush()
        progress = self._match_progress(match)
        if all(progress[participant_id].complete for participant_id in self._participant_ids(match)):
            match.status = ConferenceMatchStatus.AWAITING_JUDGMENT
            self._queue_match_judgment(match.id)
            for participant_user_id in self._participant_ids(match):
                self._create_letter(
                    recipient_user_id=participant_user_id,
                    category=ConferenceLetterCategory.MATCH,
                    title="切磋作答已经完成",
                    body="双方均已完成本局题目，正在等待 AI 评审。",
                    action_type="CONFERENCE_MATCH",
                    action_id=match.id,
                )
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_ANSWER_SUBMITTED",
            target_type="CONFERENCE_MATCH_ANSWER",
            target_id=answer.id,
            result=match.status.value,
            safe_diff={
                "match_id": str(match.id),
                "question_id": str(question.id),
                "answered": progress[user.id].answered,
                "total": match.question_count,
            },
        )
        self.db.commit()
        return self._answer_submission_public(match, answer, user.id)

    def list_matches_pending_judgment(
        self, *, limit: int
    ) -> ConferenceMatchJudgmentQueuePublic:
        matches = self.db.scalars(
            select(ConferenceMatch)
            .where(ConferenceMatch.status == ConferenceMatchStatus.AWAITING_JUDGMENT)
            .order_by(ConferenceMatch.matched_at, ConferenceMatch.id)
            .limit(limit)
        ).all()
        return ConferenceMatchJudgmentQueuePublic(
            items=[self._judgment_candidate_public(match) for match in matches]
        )

    def process_match_judgment(
        self, match_id: uuid.UUID, judge: ConferenceJudge
    ) -> ConferenceMatchJudgmentPublic:
        match = self.db.get(ConferenceMatch, match_id)
        if match is None:
            self._complete_judgment_outbox(match_id, error="MATCH_NOT_FOUND")
            self.db.commit()
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        if match.status == ConferenceMatchStatus.ENDED:
            self._complete_judgment_outbox(match.id)
            self.db.commit()
            return ConferenceMatchJudgmentPublic(
                match_id=match.id,
                status=match.status,
                winner_user_id=match.winner_user_id,
            )
        if match.status != ConferenceMatchStatus.AWAITING_JUDGMENT:
            raise ApiError(409, "MATCH_NOT_READY_FOR_JUDGMENT", "本局尚未进入评审阶段")
        candidate = self._judgment_candidate_public(match)
        self.db.rollback()
        payload = judge.judge(candidate)
        try:
            return self.judge_match(match_id, payload)
        except ApiError as exc:
            if exc.code != "MATCH_NOT_READY_FOR_JUDGMENT":
                raise
            refreshed = self.db.get(ConferenceMatch, match_id)
            if refreshed is None or refreshed.status != ConferenceMatchStatus.ENDED:
                raise
            self._complete_judgment_outbox(match_id)
            self.db.commit()
            return ConferenceMatchJudgmentPublic(
                match_id=refreshed.id,
                status=refreshed.status,
                winner_user_id=refreshed.winner_user_id,
            )

    def judge_match(
        self, match_id: uuid.UUID, payload: ConferenceMatchJudgmentCreate
    ) -> ConferenceMatchJudgmentPublic:
        match = self.db.scalar(
            select(ConferenceMatch)
            .where(ConferenceMatch.id == match_id)
            .with_for_update()
        )
        if match is None:
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        if match.status != ConferenceMatchStatus.AWAITING_JUDGMENT:
            raise ApiError(409, "MATCH_NOT_READY_FOR_JUDGMENT", "本局尚未进入评审阶段")
        participant_ids = set(self._participant_ids(match))
        if {item.user_id for item in payload.participants} != participant_ids:
            raise ApiError(422, "MATCH_PARTICIPANTS_MISMATCH", "评审对象必须是本局双方")
        score_by_user: dict[uuid.UUID, float] = {}
        for item in payload.participants:
            self._ensure_evaluation_text_safe(
                summary=item.summary,
                strengths=item.strengths,
                improvements=item.improvements,
            )
            evaluation = ConferenceMatchEvaluation(
                id=uuid.uuid4(),
                match_id=match.id,
                subject_user_id=item.user_id,
                evaluator_user_id=None,
                kind=ConferenceMatchEvaluationKind.AI,
                score=item.score,
                dimension_scores=item.dimension_scores,
                summary=item.summary,
                strengths=item.strengths,
                improvements=item.improvements,
                evaluator_reference=payload.evaluator_reference,
            )
            self.db.add(evaluation)
            score_by_user[item.user_id] = item.score
        participant_a_score = score_by_user[match.participant_a_user_id]
        participant_b_score = score_by_user[match.participant_b_user_id]
        winner_user_id = None
        if participant_a_score > participant_b_score:
            winner_user_id = match.participant_a_user_id
        elif participant_b_score > participant_a_score:
            winner_user_id = match.participant_b_user_id
        self._end_match(
            match,
            ConferenceMatchEndReason.COMPLETED,
            winner_user_id=winner_user_id,
        )
        for participant_user_id in participant_ids:
            self._create_letter(
                recipient_user_id=participant_user_id,
                category=ConferenceLetterCategory.MATCH,
                title="切磋评审结果已出",
                body="本局 AI 评审已经完成，可以查看得分、建议并写下复盘。",
                action_type="CONFERENCE_MATCH",
                action_id=match.id,
            )
        self._audit(
            user=None,
            action="CONFERENCE_MATCH_JUDGED",
            target_type="CONFERENCE_MATCH",
            target_id=match.id,
            result="COMPLETED",
            safe_diff={
                "evaluator_reference": payload.evaluator_reference,
                "winner_user_id": str(winner_user_id) if winner_user_id else None,
            },
            actor_type="INTERNAL_WORKER",
        )
        self._complete_judgment_outbox(match.id)
        self.db.commit()
        return ConferenceMatchJudgmentPublic(
            match_id=match.id,
            status=match.status,
            winner_user_id=match.winner_user_id,
        )

    def create_match_evaluation(
        self,
        user: User,
        match_id: uuid.UUID,
        payload: ConferenceMatchEvaluationCreate,
    ) -> ConferenceMatchEvaluationPublic:
        match = self._require_match_participant(user, match_id)
        self._require_completed_match(match)
        kind = ConferenceMatchEvaluationKind(payload.kind.value)
        subject_user_id = (
            user.id if kind == ConferenceMatchEvaluationKind.SELF else self._opponent_id(match, user.id)
        )
        self._ensure_evaluation_text_safe(
            summary=payload.summary,
            strengths=payload.strengths,
            improvements=payload.improvements,
        )
        existing = self.db.scalar(
            select(ConferenceMatchEvaluation).where(
                ConferenceMatchEvaluation.match_id == match.id,
                ConferenceMatchEvaluation.subject_user_id == subject_user_id,
                ConferenceMatchEvaluation.kind == kind,
            )
        )
        if existing is not None:
            raise ApiError(409, "MATCH_EVALUATION_EXISTS", "本类评价已经提交")
        evaluation = ConferenceMatchEvaluation(
            id=uuid.uuid4(),
            match_id=match.id,
            subject_user_id=subject_user_id,
            evaluator_user_id=user.id,
            kind=kind,
            score=payload.score,
            dimension_scores=payload.dimension_scores,
            summary=payload.summary,
            strengths=payload.strengths,
            improvements=payload.improvements,
            evaluator_reference=None,
        )
        self.db.add(evaluation)
        if kind == ConferenceMatchEvaluationKind.PEER:
            self._create_letter(
                recipient_user_id=subject_user_id,
                category=ConferenceLetterCategory.MATCH,
                title="收到切磋互评",
                body="本局同伴已经提交互评，你可以在结果页查看。",
                action_type="CONFERENCE_MATCH",
                action_id=match.id,
            )
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_EVALUATION_CREATED",
            target_type="CONFERENCE_MATCH_EVALUATION",
            target_id=evaluation.id,
            result=kind.value,
            safe_diff={"match_id": str(match.id), "kind": kind.value},
        )
        self.db.commit()
        return self._evaluation_public(evaluation)

    def create_teacher_evaluation(
        self,
        match_id: uuid.UUID,
        payload: ConferenceMatchTeacherEvaluationCreate,
    ) -> ConferenceMatchEvaluationPublic:
        match = self.db.get(ConferenceMatch, match_id)
        if match is None:
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        self._require_completed_match(match)
        if payload.subject_user_id not in self._participant_ids(match):
            raise ApiError(422, "MATCH_PARTICIPANT_REQUIRED", "师评对象必须是本局参与者")
        self._ensure_evaluation_text_safe(
            summary=payload.summary,
            strengths=payload.strengths,
            improvements=payload.improvements,
        )
        existing = self.db.scalar(
            select(ConferenceMatchEvaluation).where(
                ConferenceMatchEvaluation.match_id == match.id,
                ConferenceMatchEvaluation.subject_user_id == payload.subject_user_id,
                ConferenceMatchEvaluation.kind == ConferenceMatchEvaluationKind.TEACHER,
            )
        )
        if existing is not None:
            raise ApiError(409, "MATCH_TEACHER_EVALUATION_EXISTS", "本局师评已经提交")
        evaluation = ConferenceMatchEvaluation(
            id=uuid.uuid4(),
            match_id=match.id,
            subject_user_id=payload.subject_user_id,
            evaluator_user_id=None,
            kind=ConferenceMatchEvaluationKind.TEACHER,
            score=payload.score,
            dimension_scores=payload.dimension_scores,
            summary=payload.summary,
            strengths=payload.strengths,
            improvements=payload.improvements,
            evaluator_reference=payload.evaluator_reference,
        )
        self.db.add(evaluation)
        self._create_letter(
            recipient_user_id=payload.subject_user_id,
            category=ConferenceLetterCategory.MATCH,
            title="收到切磋师评",
            body="老师已经为本局留下评价，可以在结果页查看。",
            action_type="CONFERENCE_MATCH",
            action_id=match.id,
        )
        self._audit(
            user=None,
            action="CONFERENCE_MATCH_TEACHER_EVALUATION_CREATED",
            target_type="CONFERENCE_MATCH_EVALUATION",
            target_id=evaluation.id,
            result="TEACHER",
            safe_diff={
                "match_id": str(match.id),
                "evaluator_reference": payload.evaluator_reference,
            },
            actor_type="INTERNAL_WORKER",
        )
        self.db.commit()
        return self._evaluation_public(evaluation)

    def get_match_result(
        self, user: User, match_id: uuid.UUID
    ) -> ConferenceMatchResultPublic:
        match = self._require_match_participant(user, match_id)
        evaluations = self.db.scalars(
            select(ConferenceMatchEvaluation)
            .where(ConferenceMatchEvaluation.match_id == match.id)
            .order_by(
                ConferenceMatchEvaluation.subject_user_id,
                ConferenceMatchEvaluation.created_at,
            )
        ).all()
        by_subject: dict[uuid.UUID, list[ConferenceMatchEvaluation]] = {
            participant_id: [] for participant_id in self._participant_ids(match)
        }
        for evaluation in evaluations:
            by_subject[evaluation.subject_user_id].append(evaluation)
        if match.status != ConferenceMatchStatus.ENDED:
            outcome = ConferenceMatchOutcome.PENDING
        elif match.end_reason != ConferenceMatchEndReason.COMPLETED:
            outcome = ConferenceMatchOutcome.ENDED_WITHOUT_RESULT
        elif match.winner_user_id is None:
            outcome = ConferenceMatchOutcome.TIE
        elif match.winner_user_id == user.id:
            outcome = ConferenceMatchOutcome.WIN
        else:
            outcome = ConferenceMatchOutcome.LOSE
        reflection = self.db.scalar(
            select(ConferenceMatchReflection).where(
                ConferenceMatchReflection.match_id == match.id,
                ConferenceMatchReflection.user_id == user.id,
            )
        )
        opponent_user_id = self._opponent_id(match, user.id)
        participants = []
        for subject_user_id, perspective in (
            (user.id, ConferenceMatchPerspective.SELF),
            (opponent_user_id, ConferenceMatchPerspective.OPPONENT),
        ):
            subject_evaluations = by_subject[subject_user_id]
            ai_evaluation = next(
                (
                    evaluation
                    for evaluation in subject_evaluations
                    if evaluation.kind == ConferenceMatchEvaluationKind.AI
                ),
                None,
            )
            participants.append(
                ConferenceMatchParticipantResultPublic(
                    perspective=perspective,
                    score=ai_evaluation.score if ai_evaluation else None,
                    evaluations=[
                        self._evaluation_public(evaluation)
                        for evaluation in subject_evaluations
                    ],
                )
            )
        return ConferenceMatchResultPublic(
            match_id=match.id,
            status=match.status,
            end_reason=match.end_reason,
            outcome=outcome,
            participants=participants,
            my_reflection=(
                self._reflection_public(reflection) if reflection is not None else None
            ),
            ended_at=match.ended_at,
        )

    def list_match_records(
        self,
        user: User,
        *,
        outcome: ConferenceMatchHistoryOutcome | None,
        reflection_status: ConferenceMatchReflectionStatus | None,
        page: int,
        limit: int,
    ) -> ConferenceMatchRecordListPublic:
        participant_filter = or_(
            ConferenceMatch.participant_a_user_id == user.id,
            ConferenceMatch.participant_b_user_id == user.id,
        )
        completed_filter = ConferenceMatch.status == ConferenceMatchStatus.ENDED
        reflection_exists = (
            select(ConferenceMatchReflection.id)
            .where(
                ConferenceMatchReflection.match_id == ConferenceMatch.id,
                ConferenceMatchReflection.user_id == user.id,
            )
            .exists()
        )

        query = select(ConferenceMatch).where(participant_filter, completed_filter)
        if outcome == ConferenceMatchHistoryOutcome.WIN:
            query = query.where(
                ConferenceMatch.end_reason == ConferenceMatchEndReason.COMPLETED,
                ConferenceMatch.winner_user_id == user.id,
            )
        elif outcome == ConferenceMatchHistoryOutcome.LOSE:
            query = query.where(
                ConferenceMatch.end_reason == ConferenceMatchEndReason.COMPLETED,
                ConferenceMatch.winner_user_id.is_not(None),
                ConferenceMatch.winner_user_id != user.id,
            )
        elif outcome == ConferenceMatchHistoryOutcome.TIE:
            query = query.where(
                ConferenceMatch.end_reason == ConferenceMatchEndReason.COMPLETED,
                ConferenceMatch.winner_user_id.is_(None),
            )
        if reflection_status == ConferenceMatchReflectionStatus.COMPLETED:
            query = query.where(reflection_exists)
        elif reflection_status == ConferenceMatchReflectionStatus.PENDING:
            query = query.where(
                ConferenceMatch.end_reason == ConferenceMatchEndReason.COMPLETED,
                ~reflection_exists,
            )

        filtered_count = int(
            self.db.scalar(select(func.count()).select_from(query.subquery())) or 0
        )
        matches = list(
            self.db.scalars(
                query.order_by(
                    ConferenceMatch.ended_at.desc(),
                    ConferenceMatch.id.desc(),
                )
                .offset((page - 1) * limit)
                .limit(limit)
            ).all()
        )

        all_completed = list(
            self.db.scalars(
                select(ConferenceMatch).where(participant_filter, completed_filter)
            ).all()
        )
        reflected_match_ids = set(
            self.db.scalars(
                select(ConferenceMatchReflection.match_id).where(
                    ConferenceMatchReflection.user_id == user.id
                )
            ).all()
        )
        summary = ConferenceMatchRecordSummaryPublic(
            total=len(all_completed),
            wins=sum(
                1
                for match in all_completed
                if self._match_outcome(match, user.id) == ConferenceMatchOutcome.WIN
            ),
            ties=sum(
                1
                for match in all_completed
                if self._match_outcome(match, user.id) == ConferenceMatchOutcome.TIE
            ),
            pending_reflections=sum(
                1
                for match in all_completed
                if match.end_reason == ConferenceMatchEndReason.COMPLETED
                and match.id not in reflected_match_ids
            ),
        )
        return ConferenceMatchRecordListPublic(
            items=[self._match_record_public(match, user.id) for match in matches],
            summary=summary,
            page=page,
            limit=limit,
            total=filtered_count,
            has_more=page * limit < filtered_count,
        )

    def create_match_reflection(
        self,
        user: User,
        match_id: uuid.UUID,
        payload: ConferenceMatchReflectionCreate,
    ) -> ConferenceMatchReflectionPublic:
        match = self._require_match_participant(user, match_id)
        self._require_completed_match(match)
        ensure_public_interaction_text(field="learned", value=payload.learned)
        ensure_public_interaction_text(
            field="next_improvement", value=payload.next_improvement
        )
        existing = self.db.scalar(
            select(ConferenceMatchReflection).where(
                ConferenceMatchReflection.match_id == match.id,
                ConferenceMatchReflection.user_id == user.id,
            )
        )
        if existing is not None:
            if (
                existing.learned != payload.learned
                or existing.next_improvement != payload.next_improvement
            ):
                raise ApiError(409, "MATCH_REFLECTION_EXISTS", "本局复盘已经提交")
            return self._reflection_public(existing)
        reflection = ConferenceMatchReflection(
            id=uuid.uuid4(),
            match_id=match.id,
            user_id=user.id,
            learned=payload.learned,
            next_improvement=payload.next_improvement,
        )
        self.db.add(reflection)
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_REFLECTION_CREATED",
            target_type="CONFERENCE_MATCH_REFLECTION",
            target_id=reflection.id,
            result="SUCCESS",
            safe_diff={"match_id": str(match.id)},
        )
        self.db.commit()
        return self._reflection_public(reflection)

    def list_letters(self, user: User, *, limit: int) -> ConferenceLetterListPublic:
        letters = self.db.scalars(
            select(ConferenceLetter)
            .where(ConferenceLetter.recipient_user_id == user.id)
            .order_by(ConferenceLetter.created_at.desc(), ConferenceLetter.id.desc())
            .limit(limit)
        ).all()
        unread_count = self.db.scalar(
            select(func.count())
            .select_from(ConferenceLetter)
            .where(
                ConferenceLetter.recipient_user_id == user.id,
                ConferenceLetter.read_at.is_(None),
            )
        ) or 0
        navigations = self._letter_navigations(letters)
        return ConferenceLetterListPublic(
            items=[
                self._letter_public(letter, *navigations[letter.id])
                for letter in letters
            ],
            unread_count=unread_count,
        )

    def read_letter(self, user: User, letter_id: uuid.UUID) -> ConferenceLetterPublic:
        letter = self.db.scalar(
            select(ConferenceLetter)
            .where(
                ConferenceLetter.id == letter_id,
                ConferenceLetter.recipient_user_id == user.id,
            )
            .with_for_update()
        )
        if letter is None:
            raise ApiError(404, "CONFERENCE_LETTER_NOT_FOUND", "书信不存在")
        if letter.read_at is None:
            letter.read_at = utcnow()
            self._audit(
                user=user,
                action="CONFERENCE_LETTER_READ",
                target_type="CONFERENCE_LETTER",
                target_id=letter.id,
                result="SUCCESS",
                safe_diff={"category": letter.category.value},
            )
            self.db.commit()
        navigation = self._letter_navigations([letter])[letter.id]
        return self._letter_public(letter, *navigation)

    def exit_match_queue(self, user: User) -> ConferenceMatchQueuePublic:
        entry = self._active_queue_entry(user.id)
        if entry is None:
            return self._queue_public(None)
        entry.status = ConferenceMatchQueueStatus.EXITED
        entry.expires_at = None
        if entry.match_id is not None:
            match = self.db.get(ConferenceMatch, entry.match_id)
            if match is not None and match.status != ConferenceMatchStatus.ENDED:
                self._end_match(match, ConferenceMatchEndReason.EXITED)
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_QUEUE_EXITED",
            target_type="CONFERENCE_MATCH_QUEUE",
            target_id=entry.id,
            result="EXITED",
            safe_diff={"match_id": str(entry.match_id) if entry.match_id else None},
        )
        self.db.commit()
        return self._queue_public(entry)

    def report_match(
        self,
        user: User,
        match_id: uuid.UUID,
        payload: ConferenceMatchReportCreate,
    ) -> ConferenceMatchReportPublic:
        match = self.db.get(ConferenceMatch, match_id)
        if match is None or user.id not in {
            match.participant_a_user_id,
            match.participant_b_user_id,
        }:
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        existing = self.db.scalar(
            select(ConferenceMatchReport).where(
                ConferenceMatchReport.match_id == match.id,
                ConferenceMatchReport.reporter_user_id == user.id,
            )
        )
        if existing is not None:
            return self._report_public(existing)
        reported_user_id = (
            match.participant_b_user_id
            if match.participant_a_user_id == user.id
            else match.participant_a_user_id
        )
        report = ConferenceMatchReport(
            id=uuid.uuid4(),
            match_id=match.id,
            reporter_user_id=user.id,
            reported_user_id=reported_user_id,
            reason=payload.reason,
            details=payload.details,
            status=ConferenceMatchReportStatus.PENDING,
        )
        self.db.add(report)
        if match.status != ConferenceMatchStatus.ENDED:
            self._end_match(match, ConferenceMatchEndReason.REPORTED)
        self._audit(
            user=user,
            action="CONFERENCE_MATCH_REPORTED",
            target_type="CONFERENCE_MATCH_REPORT",
            target_id=report.id,
            result="PENDING_REVIEW",
            safe_diff={
                "match_id": str(match.id),
                "reason": payload.reason.value,
            },
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            existing = self.db.scalar(
                select(ConferenceMatchReport).where(
                    ConferenceMatchReport.match_id == match.id,
                    ConferenceMatchReport.reporter_user_id == user.id,
                )
            )
            if existing is not None:
                return self._report_public(existing)
            raise ApiError(409, "MATCH_REPORT_CONFLICT", "举报提交发生冲突，请重试") from exc
        return self._report_public(report)

    def list_match_reports(
        self, *, report_status: ConferenceMatchReportStatus, limit: int
    ) -> ConferenceMatchReportListPublic:
        reports = self.db.scalars(
            select(ConferenceMatchReport)
            .where(ConferenceMatchReport.status == report_status)
            .order_by(ConferenceMatchReport.created_at, ConferenceMatchReport.id)
            .limit(limit)
        ).all()
        return ConferenceMatchReportListPublic(
            items=[
                ConferenceMatchReportInternalPublic(
                    **self._report_public(report).model_dump(),
                    reporter_user_id=report.reporter_user_id,
                    reported_user_id=report.reported_user_id,
                    report_details=report.details,
                )
                for report in reports
            ]
        )

    def decide_match_report(
        self,
        report_id: uuid.UUID,
        payload: ConferenceMatchReportDecision,
    ) -> ConferenceMatchReportPublic:
        report = self.db.scalar(
            select(ConferenceMatchReport)
            .where(ConferenceMatchReport.id == report_id)
            .with_for_update()
        )
        if report is None:
            raise ApiError(404, "MATCH_REPORT_NOT_FOUND", "切磋举报不存在")
        if report.status != ConferenceMatchReportStatus.PENDING:
            raise ApiError(409, "MATCH_REPORT_ALREADY_DECIDED", "该举报已处理")
        if report.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "举报记录已更新，请刷新后重试")
        report.status = (
            ConferenceMatchReportStatus.RESOLVED
            if payload.decision == MatchReportDecision.CONFIRM
            else ConferenceMatchReportStatus.DISMISSED
        )
        report.reviewer_reference = payload.reviewer_reference
        report.resolution_summary = payload.resolution_summary
        report.resolved_at = utcnow()
        report.row_version += 1
        self._audit(
            user=None,
            action="CONFERENCE_MATCH_REPORT_DECIDED",
            target_type="CONFERENCE_MATCH_REPORT",
            target_id=report.id,
            result=report.status.value,
            safe_diff={
                "match_id": str(report.match_id),
                "decision": payload.decision.value,
                "reviewer_reference": payload.reviewer_reference,
                "row_version": report.row_version,
            },
            actor_type="INTERNAL_WORKER",
        )
        self._create_letter(
            recipient_user_id=report.reporter_user_id,
            category=ConferenceLetterCategory.SYSTEM,
            title="切磋举报已处理",
            body=payload.resolution_summary,
            action_type="CONFERENCE_MATCH_REPORT",
            action_id=report.id,
        )
        self.db.commit()
        return self._report_public(report)

    def _require_visible_community_item(
        self, user: User, publication_id: uuid.UUID
    ) -> PublicationFeedItemPublic:
        item = self.distribution.community_item(user, publication_id)
        if item is None:
            raise ApiError(404, "COMMUNITY_WORK_NOT_FOUND", "公开作品不存在或暂不可见")
        return item

    def _require_visible_community_publication(
        self, user: User, publication_id: uuid.UUID
    ) -> Publication:
        self._require_visible_community_item(user, publication_id)
        publication = self.db.get(Publication, publication_id)
        assert publication is not None
        return publication

    @staticmethod
    def _block_lists(user_id: uuid.UUID) -> tuple[object, object]:
        return (
            select(BlacklistEntry.blocked_user_id).where(
                BlacklistEntry.owner_user_id == user_id
            ),
            select(BlacklistEntry.owner_user_id).where(
                BlacklistEntry.blocked_user_id == user_id
            ),
        )

    def _review_public(self, review: ConferenceReview, reviewer: User) -> ConferenceReviewPublic:
        def as_utc(value: datetime | None) -> datetime | None:
            # SQLite drops timezone information when a row is reloaded on replay.
            return value.replace(tzinfo=UTC) if value is not None and value.tzinfo is None else value

        return ConferenceReviewPublic(
            id=review.id,
            publication_id=review.publication_id,
            reviewer_nickname=reviewer.nickname,
            template=review.template,
            content=review.content,
            status=review.status,
            moderation_status=review.moderation_status,
            author_reply=review.author_reply,
            handled_at=as_utc(review.handled_at),
            adopted_in_creation_version_id=review.adopted_in_creation_version_id,
            adoption_summary=review.adoption_summary,
            adopted_at=as_utc(review.adopted_at),
            row_version=review.row_version,
            created_at=as_utc(review.created_at),
        )

    def _derivative_request_public(
        self, request: ConferenceDerivativeRequest
    ) -> ConferenceDerivativeRequestPublic:
        authorization = self.db.scalar(
            select(ConferenceDerivativeAuthorization).where(
                ConferenceDerivativeAuthorization.request_id == request.id
            )
        )
        return ConferenceDerivativeRequestPublic(
            id=request.id,
            source_publication_id=request.source_publication_id,
            source_creation_version_id=request.source_creation_version_id,
            source_title=request.source_title,
            source_author_nickname=request.source_author_nickname,
            requested_use=request.requested_use,
            status=request.status,
            author_decision_note=request.author_decision_note,
            decided_at=request.decided_at,
            authorization=self._authorization_public(authorization) if authorization else None,
            created_at=request.created_at,
        )

    @staticmethod
    def _authorization_public(
        authorization: ConferenceDerivativeAuthorization,
    ) -> ConferenceDerivativeAuthorizationPublic:
        return ConferenceDerivativeAuthorizationPublic(
            id=authorization.id,
            request_id=authorization.request_id,
            source_publication_id=authorization.source_publication_id,
            source_creation_version_id=authorization.source_creation_version_id,
            authorization_version=authorization.authorization_version,
            status=authorization.status,
            granted_at=authorization.granted_at,
            revoked_at=authorization.revoked_at,
        )

    def _active_queue_entry(self, user_id: uuid.UUID) -> ConferenceMatchQueue | None:
        now = utcnow()
        return self.db.scalar(
            select(ConferenceMatchQueue)
            .where(
                ConferenceMatchQueue.user_id == user_id,
                or_(
                    ConferenceMatchQueue.status == ConferenceMatchQueueStatus.MATCHED,
                    and_(
                        ConferenceMatchQueue.status == ConferenceMatchQueueStatus.WAITING,
                        ConferenceMatchQueue.expires_at.is_not(None),
                        ConferenceMatchQueue.expires_at > now,
                    ),
                ),
            )
            .order_by(ConferenceMatchQueue.joined_at.desc(), ConferenceMatchQueue.id.desc())
            .limit(1)
        )

    def _queue_public(
        self, entry: ConferenceMatchQueue | None
    ) -> ConferenceMatchQueuePublic:
        now = utcnow()
        if entry is None:
            return ConferenceMatchQueuePublic(
                queue_id=None,
                status=ConferenceMatchQueueState.IDLE,
                phase=ConferenceMatchQueuePhase.IDLE,
                manual_page_id=None,
                manual_title=None,
                manual_page_no=None,
                match_id=None,
                match_code=None,
                pool_size=0,
                wait_seconds=0,
                server_time=now,
                anonymous_opponent=None,
                joined_at=None,
                expires_at=None,
                updated_at=None,
            )

        manual_page = self.db.get(ManualPage, entry.manual_page_id)
        pool_size = 0
        if entry.status == ConferenceMatchQueueStatus.WAITING:
            pool_size = int(
                self.db.scalar(
                    select(func.count(ConferenceMatchQueue.id)).where(
                        ConferenceMatchQueue.manual_page_id == entry.manual_page_id,
                        ConferenceMatchQueue.age_band == entry.age_band,
                        ConferenceMatchQueue.status == ConferenceMatchQueueStatus.WAITING,
                        ConferenceMatchQueue.expires_at.is_not(None),
                        ConferenceMatchQueue.expires_at > now,
                    )
                )
                or 0
            )

        wait_ended_at = (
            entry.updated_at
            if entry.status == ConferenceMatchQueueStatus.MATCHED
            else now
        )
        joined_at = entry.joined_at
        if joined_at.tzinfo is None:
            joined_at = joined_at.replace(tzinfo=UTC)
        if wait_ended_at.tzinfo is None:
            wait_ended_at = wait_ended_at.replace(tzinfo=UTC)
        wait_seconds = max(0, int((wait_ended_at - joined_at).total_seconds()))

        phase = {
            ConferenceMatchQueueStatus.WAITING: ConferenceMatchQueuePhase.FILTERING,
            ConferenceMatchQueueStatus.MATCHED: ConferenceMatchQueuePhase.LOCKED,
            ConferenceMatchQueueStatus.EXITED: ConferenceMatchQueuePhase.IDLE,
        }[entry.status]
        opponent = None
        match_code = None
        if entry.status == ConferenceMatchQueueStatus.MATCHED and entry.match_id is not None:
            match_code = f"M-{entry.match_id.hex[:5].upper()}"
            opponent = ConferenceMatchOpponentPublic(
                alias="竹影同门",
                avatar_key="PANDA_BAMBOO",
                age_band_label="同龄",
                stage_label="同阶段",
            )
        return ConferenceMatchQueuePublic(
            queue_id=entry.id,
            status=ConferenceMatchQueueState(entry.status.value),
            phase=phase,
            manual_page_id=entry.manual_page_id,
            manual_title=manual_page.title if manual_page else None,
            manual_page_no=manual_page.page_no if manual_page else None,
            match_id=entry.match_id,
            match_code=match_code,
            pool_size=pool_size,
            wait_seconds=wait_seconds,
            server_time=now,
            anonymous_opponent=opponent,
            joined_at=entry.joined_at,
            expires_at=entry.expires_at,
            updated_at=entry.updated_at,
        )

    @staticmethod
    def _match_outcome(
        match: ConferenceMatch, user_id: uuid.UUID
    ) -> ConferenceMatchOutcome:
        if match.status != ConferenceMatchStatus.ENDED:
            return ConferenceMatchOutcome.PENDING
        if match.end_reason != ConferenceMatchEndReason.COMPLETED:
            return ConferenceMatchOutcome.ENDED_WITHOUT_RESULT
        if match.winner_user_id is None:
            return ConferenceMatchOutcome.TIE
        if match.winner_user_id == user_id:
            return ConferenceMatchOutcome.WIN
        return ConferenceMatchOutcome.LOSE

    def _match_record_public(
        self, match: ConferenceMatch, user_id: uuid.UUID
    ) -> ConferenceMatchRecordPublic:
        manual_page = self.db.get(ManualPage, match.manual_page_id)
        if manual_page is None:
            raise ApiError(500, "MATCH_MANUAL_NOT_FOUND", "切磋秘籍不存在")
        opponent_user_id = self._opponent_id(match, user_id)
        ai_evaluations = list(
            self.db.scalars(
                select(ConferenceMatchEvaluation).where(
                    ConferenceMatchEvaluation.match_id == match.id,
                    ConferenceMatchEvaluation.kind == ConferenceMatchEvaluationKind.AI,
                )
            ).all()
        )
        score_by_user = {
            evaluation.subject_user_id: evaluation.score
            for evaluation in ai_evaluations
        }
        has_reflection = self.db.scalar(
            select(ConferenceMatchReflection.id).where(
                ConferenceMatchReflection.match_id == match.id,
                ConferenceMatchReflection.user_id == user_id,
            )
        ) is not None
        judgment_status = (
            ConferenceMatchJudgmentStatus.COMPLETED
            if ai_evaluations
            else (
                ConferenceMatchJudgmentStatus.NOT_REQUIRED
                if match.end_reason != ConferenceMatchEndReason.COMPLETED
                else ConferenceMatchJudgmentStatus.PENDING
            )
        )
        return ConferenceMatchRecordPublic(
            match_id=match.id,
            manual_id=match.manual_page_id,
            manual_title=manual_page.title,
            manual_page_no=manual_page.page_no,
            status=match.status,
            outcome=self._match_outcome(match, user_id),
            my_score=score_by_user.get(user_id),
            opponent_score=score_by_user.get(opponent_user_id),
            anonymous_opponent=ConferenceMatchOpponentPublic(
                alias="竹影同门",
                avatar_key="PANDA_BAMBOO",
                age_band_label="同龄",
                stage_label="同阶段",
            ),
            judgment_status=judgment_status,
            reflection_status=(
                ConferenceMatchReflectionStatus.COMPLETED
                if has_reflection
                else ConferenceMatchReflectionStatus.PENDING
            ),
            created_at=match.matched_at,
            ended_at=match.ended_at,
        )

    def _queue_match_judgment(self, match_id: uuid.UUID) -> None:
        self.db.add(
            OutboxEvent(
                id=uuid.uuid4(),
                aggregate_type="CONFERENCE_MATCH",
                aggregate_id=match_id,
                event_type="CONFERENCE_MATCH_JUDGMENT_REQUESTED",
                payload={"match_id": str(match_id)},
                deduplication_key=f"conference-match-judgment:{match_id}",
                status=OutboxStatus.PENDING,
                available_at=utcnow(),
            )
        )

    def _complete_judgment_outbox(
        self, match_id: uuid.UUID, *, error: str | None = None
    ) -> None:
        event = self.db.scalar(
            select(OutboxEvent)
            .where(
                OutboxEvent.aggregate_type == "CONFERENCE_MATCH",
                OutboxEvent.aggregate_id == match_id,
                OutboxEvent.event_type == "CONFERENCE_MATCH_JUDGMENT_REQUESTED",
                OutboxEvent.status.in_([OutboxStatus.PENDING, OutboxStatus.PROCESSING]),
            )
            .order_by(OutboxEvent.created_at.desc())
            .limit(1)
        )
        if event is None:
            return
        event.status = OutboxStatus.FAILED if error else OutboxStatus.COMPLETED
        event.last_error_code = error
        event.processed_at = utcnow()
        event.attempts += 1

    def _judgment_candidate_public(
        self, match: ConferenceMatch
    ) -> ConferenceMatchJudgmentCandidatePublic:
        questions = self._match_questions(match.id)
        answers = self.db.scalars(
            select(ConferenceMatchAnswer)
            .where(ConferenceMatchAnswer.match_id == match.id)
            .order_by(
                ConferenceMatchAnswer.participant_user_id,
                ConferenceMatchAnswer.submitted_at,
            )
        ).all()
        return ConferenceMatchJudgmentCandidatePublic(
            match_id=match.id,
            manual_page_id=match.manual_page_id,
            participant_user_ids=list(self._participant_ids(match)),
            questions=[self._question_public(question) for question in questions],
            rubrics={str(question.id): question.rubric for question in questions},
            answers=[
                {
                    **self._answer_public(answer).model_dump(),
                    "participant_user_id": answer.participant_user_id,
                }
                for answer in answers
            ],
            matched_at=match.matched_at,
        )

    @staticmethod
    def _participant_ids(match: ConferenceMatch) -> tuple[uuid.UUID, uuid.UUID]:
        return match.participant_a_user_id, match.participant_b_user_id

    def _require_match_participant(
        self, user: User, match_id: uuid.UUID
    ) -> ConferenceMatch:
        match = self.db.get(ConferenceMatch, match_id)
        if match is None or user.id not in self._participant_ids(match):
            raise ApiError(404, "MATCH_NOT_FOUND", "切磋记录不存在")
        return match

    @staticmethod
    def _opponent_id(match: ConferenceMatch, user_id: uuid.UUID) -> uuid.UUID:
        return (
            match.participant_b_user_id
            if match.participant_a_user_id == user_id
            else match.participant_a_user_id
        )

    def _snapshot_match_questions(
        self, match: ConferenceMatch, manual_page: ManualPage
    ) -> None:
        snapshots = (
            (
                ConferenceMatchQuestionKind.CORE_LOGIC,
                f"请用自己的话解释「{manual_page.title}」的核心道理，并写出判断依据。",
                f"回答应准确围绕核心逻辑：{manual_page.core_logic}",
            ),
            (
                ConferenceMatchQuestionKind.CASE_ANALYSIS,
                f"阅读公案：{manual_page.life_hook} 请指出其中的关键问题，并说明理由。",
                "回答应识别公案中的关键问题，并将判断与本页核心逻辑建立联系。",
            ),
            (
                ConferenceMatchQuestionKind.TRANSFER,
                f"迁移任务：{manual_page.interaction_evidence} 请给出你的方案和理由。",
                "回答应包含可执行方案、选择理由，以及必要的安全或公平边界。",
            ),
        )
        for position, (kind, prompt, rubric) in enumerate(snapshots, start=1):
            self.db.add(
                ConferenceMatchQuestion(
                    id=uuid.uuid4(),
                    match_id=match.id,
                    position=position,
                    kind=kind,
                    prompt=prompt,
                    rubric=rubric,
                    content_version=manual_page.content_version,
                )
            )

    def _match_questions(self, match_id: uuid.UUID) -> list[ConferenceMatchQuestion]:
        return list(
            self.db.scalars(
                select(ConferenceMatchQuestion)
                .where(ConferenceMatchQuestion.match_id == match_id)
                .order_by(ConferenceMatchQuestion.position)
            ).all()
        )

    @staticmethod
    def _question_public(
        question: ConferenceMatchQuestion,
    ) -> ConferenceMatchQuestionPublic:
        return ConferenceMatchQuestionPublic(
            id=question.id,
            position=question.position,
            kind=question.kind,
            prompt=question.prompt,
        )

    @staticmethod
    def _answer_public(answer: ConferenceMatchAnswer) -> ConferenceMatchAnswerPublic:
        return ConferenceMatchAnswerPublic(
            id=answer.id,
            question_id=answer.question_id,
            answer=answer.answer,
            reason=answer.reason,
            submitted_at=answer.submitted_at,
        )

    def _match_progress(
        self, match: ConferenceMatch
    ) -> dict[uuid.UUID, ConferenceMatchProgressPublic]:
        counts = dict(
            self.db.execute(
                select(
                    ConferenceMatchAnswer.participant_user_id,
                    func.count(ConferenceMatchAnswer.id),
                )
                .where(ConferenceMatchAnswer.match_id == match.id)
                .group_by(ConferenceMatchAnswer.participant_user_id)
            ).all()
        )
        return {
            participant_id: ConferenceMatchProgressPublic(
                answered=int(counts.get(participant_id, 0)),
                total=match.question_count,
                complete=int(counts.get(participant_id, 0)) >= match.question_count,
            )
            for participant_id in self._participant_ids(match)
        }

    def _match_detail_public(
        self, match: ConferenceMatch, user_id: uuid.UUID
    ) -> ConferenceMatchDetailPublic:
        questions = self._match_questions(match.id)
        answers = self.db.scalars(
            select(ConferenceMatchAnswer)
            .join(
                ConferenceMatchQuestion,
                ConferenceMatchQuestion.id == ConferenceMatchAnswer.question_id,
            )
            .where(
                ConferenceMatchAnswer.match_id == match.id,
                ConferenceMatchAnswer.participant_user_id == user_id,
            )
            .order_by(ConferenceMatchQuestion.position)
        ).all()
        progress = self._match_progress(match)
        opponent_user_id = self._opponent_id(match, user_id)
        return ConferenceMatchDetailPublic(
            match_id=match.id,
            manual_page_id=match.manual_page_id,
            status=match.status,
            questions=[self._question_public(question) for question in questions],
            my_answers=[self._answer_public(answer) for answer in answers],
            my_progress=progress[user_id],
            opponent_progress=progress[opponent_user_id],
            matched_at=match.matched_at,
        )

    def _answer_submission_public(
        self,
        match: ConferenceMatch,
        answer: ConferenceMatchAnswer,
        user_id: uuid.UUID,
    ) -> ConferenceMatchAnswerSubmittedPublic:
        progress = self._match_progress(match)
        return ConferenceMatchAnswerSubmittedPublic(
            answer=self._answer_public(answer),
            my_progress=progress[user_id],
            opponent_progress=progress[self._opponent_id(match, user_id)],
            match_status=match.status,
        )

    @staticmethod
    def _require_completed_match(match: ConferenceMatch) -> None:
        if (
            match.status != ConferenceMatchStatus.ENDED
            or match.end_reason != ConferenceMatchEndReason.COMPLETED
        ):
            raise ApiError(409, "MATCH_RESULT_NOT_READY", "本局尚未完成评审")

    @staticmethod
    def _ensure_evaluation_text_safe(
        *, summary: str, strengths: list[str], improvements: list[str]
    ) -> None:
        ensure_public_interaction_text(field="summary", value=summary)
        for index, point in enumerate(strengths):
            ensure_public_interaction_text(field=f"strengths.{index}", value=point)
        for index, point in enumerate(improvements):
            ensure_public_interaction_text(field=f"improvements.{index}", value=point)

    @staticmethod
    def _evaluation_public(
        evaluation: ConferenceMatchEvaluation,
    ) -> ConferenceMatchEvaluationPublic:
        return ConferenceMatchEvaluationPublic(
            id=evaluation.id,
            kind=evaluation.kind,
            score=evaluation.score,
            dimension_scores=evaluation.dimension_scores,
            summary=evaluation.summary,
            strengths=evaluation.strengths,
            improvements=evaluation.improvements,
            evaluator_reference=evaluation.evaluator_reference,
            created_at=evaluation.created_at,
        )

    @staticmethod
    def _reflection_public(
        reflection: ConferenceMatchReflection,
    ) -> ConferenceMatchReflectionPublic:
        return ConferenceMatchReflectionPublic(
            id=reflection.id,
            match_id=reflection.match_id,
            learned=reflection.learned,
            next_improvement=reflection.next_improvement,
            created_at=reflection.created_at,
        )

    def _create_letter(
        self,
        *,
        recipient_user_id: uuid.UUID,
        category: ConferenceLetterCategory,
        title: str,
        body: str,
        action_type: str | None,
        action_id: uuid.UUID | None,
    ) -> None:
        preference = self.db.get(UserPreference, recipient_user_id)
        if preference is not None and (
            not preference.message_enabled or not preference.work_updates
        ):
            return
        self.db.add(
            ConferenceLetter(
                id=uuid.uuid4(),
                recipient_user_id=recipient_user_id,
                category=category,
                title=title,
                body=body,
                action_type=action_type,
                action_id=action_id,
            )
        )

    def _letter_navigations(
        self,
        letters: list[ConferenceLetter],
    ) -> dict[uuid.UUID, tuple[str | None, uuid.UUID | None]]:
        review_ids = {
            letter.action_id
            for letter in letters
            if letter.action_type == "CONFERENCE_REVIEW" and letter.action_id is not None
        }
        report_ids = {
            letter.action_id
            for letter in letters
            if letter.action_type == "CONFERENCE_MATCH_REPORT" and letter.action_id is not None
        }
        review_publications = {
            review.id: review.publication_id
            for review in self.db.scalars(
                select(ConferenceReview).where(ConferenceReview.id.in_(review_ids))
            ).all()
        } if review_ids else {}
        report_matches = {
            report.id: report.match_id
            for report in self.db.scalars(
                select(ConferenceMatchReport).where(ConferenceMatchReport.id.in_(report_ids))
            ).all()
        } if report_ids else {}

        result: dict[uuid.UUID, tuple[str | None, uuid.UUID | None]] = {}
        for letter in letters:
            if letter.action_type == "CONFERENCE_REVIEW":
                publication_id = review_publications.get(letter.action_id)
                result[letter.id] = (
                    ("CONFERENCE_WORK", publication_id) if publication_id else (None, None)
                )
            elif letter.action_type in {
                "DERIVATIVE_REQUEST",
                "DERIVATIVE_AUTHORIZATION",
            }:
                result[letter.id] = ("DERIVATIVE_REQUESTS", None)
            elif letter.action_type == "CONFERENCE_MATCH" and letter.action_id is not None:
                result[letter.id] = ("CONFERENCE_MATCH", letter.action_id)
            elif letter.action_type == "CONFERENCE_MATCH_REPORT":
                match_id = report_matches.get(letter.action_id)
                result[letter.id] = (
                    ("CONFERENCE_MATCH", match_id) if match_id else (None, None)
                )
            else:
                result[letter.id] = (None, None)
        return result

    @staticmethod
    def _letter_public(
        letter: ConferenceLetter,
        navigation_target: str | None,
        navigation_id: uuid.UUID | None,
    ) -> ConferenceLetterPublic:
        return ConferenceLetterPublic(
            id=letter.id,
            category=letter.category,
            title=letter.title,
            body=letter.body,
            action_type=letter.action_type,
            action_id=letter.action_id,
            navigation_target=navigation_target,
            navigation_id=navigation_id,
            is_read=letter.read_at is not None,
            read_at=letter.read_at,
            created_at=letter.created_at,
        )

    def _require_manual_page(self, manual_page_id: uuid.UUID) -> ManualPage:
        page = self.db.scalar(
            select(ManualPage)
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .where(
                ManualPage.id == manual_page_id,
                ManualPage.is_listed.is_(True),
                ManualVolume.is_listed.is_(True),
            )
        )
        if page is None:
            raise ApiError(404, "MANUAL_NOT_FOUND", "秘籍不存在或暂未开放")
        return page

    def _end_match(
        self,
        match: ConferenceMatch,
        reason: ConferenceMatchEndReason,
        *,
        winner_user_id: uuid.UUID | None = None,
    ) -> None:
        now = utcnow()
        match.status = ConferenceMatchStatus.ENDED
        match.end_reason = reason
        match.winner_user_id = winner_user_id
        match.ended_at = now
        entries = self.db.scalars(
            select(ConferenceMatchQueue).where(
                ConferenceMatchQueue.match_id == match.id,
                ConferenceMatchQueue.status == ConferenceMatchQueueStatus.MATCHED,
            )
        ).all()
        for entry in entries:
            entry.status = ConferenceMatchQueueStatus.EXITED
            entry.expires_at = None
            entry.updated_at = now

    @staticmethod
    def _review_report_public(
        report: ConferenceReviewReport,
    ) -> ConferenceReviewReportPublic:
        return ConferenceReviewReportPublic(
            id=report.id,
            review_id=report.review_id,
            reason=report.reason,
            status=report.status,
            resolution_summary=report.resolution_summary,
            row_version=report.row_version,
            created_at=report.created_at,
            resolved_at=report.resolved_at,
        )

    def _audit(
        self,
        *,
        user: User | None,
        action: str,
        target_type: str,
        target_id: uuid.UUID,
        result: str,
        safe_diff: dict,
        actor_type: str = "USER",
    ) -> None:
        add_audit_event(
            self.db,
            actor_user_id=user.id if user is not None else None,
            actor_type=actor_type,
            action=action,
            target_type=target_type,
            target_id=target_id,
            result=result,
            request_id=self.request_id,
            safe_diff=safe_diff,
        )

    @staticmethod
    def _report_public(report: ConferenceMatchReport) -> ConferenceMatchReportPublic:
        return ConferenceMatchReportPublic(
            id=report.id,
            match_id=report.match_id,
            reason=report.reason,
            status=report.status,
            resolution_summary=report.resolution_summary,
            row_version=report.row_version,
            created_at=report.created_at,
            resolved_at=report.resolved_at,
        )
