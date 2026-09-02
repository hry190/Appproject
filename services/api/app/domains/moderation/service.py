from __future__ import annotations

import uuid

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.creations.models import Publication, PublicationStatus
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.moderation.audit import add_audit_event
from app.domains.moderation.contracts import (
    InternalAppealDecision,
    InternalModerationDecision,
    ModerationAppealCreate,
    ModerationAppealPublic,
    ModerationCasePublic,
    WithdrawPublication,
)
from app.domains.moderation.models import (
    AppealStatus,
    ModerationAppeal,
    ModerationCase,
    ModerationCaseStatus,
    ModerationDecision,
)
from app.models import User


def queue_moderation_case(
    db: Session,
    *,
    publication: Publication,
    owner_user_id: uuid.UUID,
    request_id: str,
) -> ModerationCase:
    case = ModerationCase(
        publication_id=publication.id,
        owner_user_id=owner_user_id,
        status=ModerationCaseStatus.AUTO_CHECK,
        automatic_reason_codes=[],
        minimal_evidence={},
    )
    db.add(case)
    db.flush()
    db.add(
        OutboxEvent(
            aggregate_type="MODERATION_CASE",
            aggregate_id=case.id,
            event_type="MODERATION_REQUESTED",
            payload={"case_id": str(case.id)},
            deduplication_key=f"moderation:{case.id}:v1",
            status=OutboxStatus.PENDING,
            available_at=utcnow(),
        )
    )
    add_audit_event(
        db,
        actor_user_id=owner_user_id,
        actor_type="USER",
        action="PUBLICATION_SUBMITTED",
        target_type="PUBLICATION",
        target_id=publication.id,
        result="PENDING_CHECK",
        request_id=request_id,
        safe_diff={"visibility": publication.visibility.value},
    )
    return case


class ModerationService:
    def __init__(self, *, db: Session, request_id: str) -> None:
        self.db = db
        self.request_id = request_id

    def get_case(self, user: User, case_id: uuid.UUID) -> ModerationCasePublic:
        case, publication = self._require_case(user, case_id)
        return self._case_public(case, publication)

    def get_publication_case(
        self, user: User, publication_id: uuid.UUID
    ) -> ModerationCasePublic:
        row = self.db.execute(
            select(ModerationCase, Publication)
            .join(Publication, Publication.id == ModerationCase.publication_id)
            .where(
                ModerationCase.publication_id == publication_id,
                ModerationCase.owner_user_id == user.id,
            )
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MODERATION_CASE_NOT_FOUND", "审核记录不存在")
        return self._case_public(row[0], row[1])

    def route_to_human_review(self, case_id: uuid.UUID) -> ModerationCasePublic:
        row = self.db.execute(
            select(ModerationCase, Publication)
            .join(Publication, Publication.id == ModerationCase.publication_id)
            .where(ModerationCase.id == case_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MODERATION_CASE_NOT_FOUND", "审核记录不存在")
        case, publication = row
        if case.status == ModerationCaseStatus.RESOLVED:
            return self._case_public(case, publication)
        case.status = ModerationCaseStatus.HUMAN_REVIEW
        case.automatic_reason_codes = ["HUMAN_CONTENT_REVIEW_REQUIRED"]
        case.detector_version = "safe-default-human-review-v1"
        case.row_version += 1
        case.updated_at = utcnow()
        publication.status = PublicationStatus.PENDING_HUMAN_REVIEW
        publication.row_version += 1
        publication.updated_at = utcnow()
        self._complete_outbox(case.id)
        add_audit_event(
            self.db,
            actor_user_id=None,
            actor_type="SYSTEM",
            action="MODERATION_ROUTED_TO_HUMAN",
            target_type="MODERATION_CASE",
            target_id=case.id,
            result="PENDING_HUMAN_REVIEW",
            request_id=self.request_id,
            safe_diff={"reason_codes": case.automatic_reason_codes},
        )
        self.db.commit()
        return self._case_public(case, publication)

    def decide(
        self, case_id: uuid.UUID, payload: InternalModerationDecision
    ) -> ModerationCasePublic:
        row = self.db.execute(
            select(ModerationCase, Publication)
            .join(Publication, Publication.id == ModerationCase.publication_id)
            .where(ModerationCase.id == case_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MODERATION_CASE_NOT_FOUND", "审核记录不存在")
        case, publication = row
        if case.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "审核记录已更新，请刷新后重试")
        if case.status != ModerationCaseStatus.HUMAN_REVIEW:
            raise ApiError(409, "MODERATION_NOT_REVIEWABLE", "审核记录当前不可人工处置")
        now = utcnow()
        case.status = ModerationCaseStatus.RESOLVED
        case.risk_level = payload.risk_level
        case.decision = payload.decision
        case.public_reason_code = payload.reason_code
        case.public_reason_summary = payload.reason_summary
        case.revision_suggestion = payload.revision_suggestion
        case.minimal_evidence = payload.minimal_evidence
        case.reviewer_reference = payload.reviewer_reference
        case.reviewed_at = now
        case.row_version += 1
        case.updated_at = now
        publication.row_version += 1
        publication.updated_at = now
        if payload.decision == ModerationDecision.PUBLISH:
            publication.status = PublicationStatus.PUBLISHED
            publication.published_at = now
            publication.return_reason_code = None
            publication.return_reason_summary = None
        elif payload.decision == ModerationDecision.RETURN:
            publication.status = PublicationStatus.RETURNED
            publication.returned_at = now
            publication.return_reason_code = payload.reason_code
            publication.return_reason_summary = payload.reason_summary
        else:
            publication.status = PublicationStatus.RESTRICTED
            publication.return_reason_code = payload.reason_code
            publication.return_reason_summary = payload.reason_summary
        add_audit_event(
            self.db,
            actor_user_id=None,
            actor_type="REVIEWER",
            action="MODERATION_DECIDED",
            target_type="MODERATION_CASE",
            target_id=case.id,
            result=payload.decision.value,
            request_id=self.request_id,
            safe_diff={
                "risk_level": payload.risk_level.value,
                "reason_code": payload.reason_code,
                "reviewer_reference": payload.reviewer_reference,
            },
        )
        self.db.commit()
        return self._case_public(case, publication)

    def withdraw(
        self,
        user: User,
        publication_id: uuid.UUID,
        payload: WithdrawPublication,
    ) -> ModerationCasePublic:
        row = self.db.execute(
            select(Publication, ModerationCase)
            .join(ModerationCase, ModerationCase.publication_id == Publication.id)
            .where(
                Publication.id == publication_id,
                Publication.owner_user_id == user.id,
            )
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "PUBLICATION_NOT_FOUND", "发布记录不存在")
        publication, case = row
        if publication.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "作品状态已更新，请刷新后重试")
        if publication.status != PublicationStatus.PUBLISHED:
            raise ApiError(409, "PUBLICATION_NOT_WITHDRAWABLE", "只有已发布作品可以撤回")
        now = utcnow()
        publication.status = PublicationStatus.WITHDRAWN
        publication.withdrawn_at = now
        publication.row_version += 1
        publication.updated_at = now
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="PUBLICATION_WITHDRAWN",
            target_type="PUBLICATION",
            target_id=publication.id,
            result="WITHDRAWN",
            request_id=self.request_id,
            safe_diff={},
        )
        self.db.commit()
        return self._case_public(case, publication)

    def create_appeal(
        self,
        user: User,
        case_id: uuid.UUID,
        payload: ModerationAppealCreate,
    ) -> ModerationAppealPublic:
        case, publication = self._require_case(user, case_id)
        if publication.status not in {
            PublicationStatus.RETURNED,
            PublicationStatus.RESTRICTED,
        }:
            raise ApiError(409, "MODERATION_NOT_APPEALABLE", "当前审核结果不可申诉")
        pending = self.db.scalar(
            select(ModerationAppeal).where(
                ModerationAppeal.moderation_case_id == case.id,
                ModerationAppeal.appellant_user_id == user.id,
                ModerationAppeal.status == AppealStatus.PENDING,
            )
        )
        if pending is not None:
            return self._appeal_public(pending)
        appeal = ModerationAppeal(
            moderation_case_id=case.id,
            appellant_user_id=user.id,
            reason=payload.reason.strip(),
            status=AppealStatus.PENDING,
        )
        self.db.add(appeal)
        self.db.flush()
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="MODERATION_APPEALED",
            target_type="MODERATION_APPEAL",
            target_id=appeal.id,
            result="PENDING",
            request_id=self.request_id,
            safe_diff={},
        )
        self.db.commit()
        return self._appeal_public(appeal)

    def list_appeals(self, user: User) -> list[ModerationAppealPublic]:
        appeals = self.db.scalars(
            select(ModerationAppeal)
            .where(ModerationAppeal.appellant_user_id == user.id)
            .order_by(ModerationAppeal.created_at.desc())
        ).all()
        return [self._appeal_public(appeal) for appeal in appeals]

    def decide_appeal(
        self, appeal_id: uuid.UUID, payload: InternalAppealDecision
    ) -> ModerationAppealPublic:
        row = self.db.execute(
            select(ModerationAppeal, ModerationCase, Publication)
            .join(ModerationCase, ModerationCase.id == ModerationAppeal.moderation_case_id)
            .join(Publication, Publication.id == ModerationCase.publication_id)
            .where(ModerationAppeal.id == appeal_id)
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MODERATION_APPEAL_NOT_FOUND", "申诉记录不存在")
        appeal, case, publication = row
        if appeal.status != AppealStatus.PENDING:
            raise ApiError(409, "APPEAL_ALREADY_RESOLVED", "申诉已经处理")
        now = utcnow()
        appeal.status = payload.status
        appeal.resolution_summary = payload.resolution_summary
        appeal.reviewer_reference = payload.reviewer_reference
        appeal.resolved_at = now
        if payload.status == AppealStatus.OVERTURNED:
            case.status = ModerationCaseStatus.HUMAN_REVIEW
            case.decision = None
            case.row_version += 1
            case.updated_at = now
            publication.status = PublicationStatus.PENDING_HUMAN_REVIEW
            publication.return_reason_code = None
            publication.return_reason_summary = None
            publication.row_version += 1
            publication.updated_at = now
        add_audit_event(
            self.db,
            actor_user_id=None,
            actor_type="REVIEWER",
            action="MODERATION_APPEAL_DECIDED",
            target_type="MODERATION_APPEAL",
            target_id=appeal.id,
            result=payload.status.value,
            request_id=self.request_id,
            safe_diff={"reviewer_reference": payload.reviewer_reference},
        )
        self.db.commit()
        return self._appeal_public(appeal)

    def _require_case(
        self, user: User, case_id: uuid.UUID
    ) -> tuple[ModerationCase, Publication]:
        row = self.db.execute(
            select(ModerationCase, Publication)
            .join(Publication, Publication.id == ModerationCase.publication_id)
            .where(
                ModerationCase.id == case_id,
                ModerationCase.owner_user_id == user.id,
            )
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MODERATION_CASE_NOT_FOUND", "审核记录不存在")
        return row[0], row[1]

    @staticmethod
    def _case_public(
        case: ModerationCase, publication: Publication
    ) -> ModerationCasePublic:
        return ModerationCasePublic(
            id=case.id,
            publication_id=case.publication_id,
            publication_status=publication.status,
            status=case.status,
            risk_level=case.risk_level,
            public_reason_code=case.public_reason_code,
            public_reason_summary=case.public_reason_summary,
            revision_suggestion=case.revision_suggestion,
            can_appeal=publication.status
            in {PublicationStatus.RETURNED, PublicationStatus.RESTRICTED},
            row_version=case.row_version,
            created_at=case.created_at,
            updated_at=case.updated_at,
        )

    @staticmethod
    def _appeal_public(appeal: ModerationAppeal) -> ModerationAppealPublic:
        return ModerationAppealPublic(
            id=appeal.id,
            moderation_case_id=appeal.moderation_case_id,
            reason=appeal.reason,
            status=appeal.status,
            resolution_summary=appeal.resolution_summary,
            created_at=appeal.created_at,
            resolved_at=appeal.resolved_at,
        )

    def _complete_outbox(self, case_id: uuid.UUID) -> None:
        event = self.db.scalar(
            select(OutboxEvent).where(
                OutboxEvent.aggregate_id == case_id,
                OutboxEvent.event_type == "MODERATION_REQUESTED",
                OutboxEvent.status.in_([OutboxStatus.PENDING, OutboxStatus.PROCESSING]),
            )
        )
        if event is not None:
            event.status = OutboxStatus.COMPLETED
            event.processed_at = utcnow()
            event.attempts += 1
