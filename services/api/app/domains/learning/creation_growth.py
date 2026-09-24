from __future__ import annotations

import uuid

from sqlalchemy import and_, or_, select
from sqlalchemy.orm import Session

from app.core.security import utcnow
from app.domains.creations.models import (
    CreationConversation,
    CreationConversationStatus,
    CreationMethod,
    CreationProject,
    CreationStage,
    CreationVersion,
    CreationVisibility,
    ImageGenerationJob,
    ImageGenerationJobStatus,
    LearningCardManual,
    Publication,
    PublicationStatus,
)
from app.domains.learning.contracts import (
    EvidenceCategory,
    EvidenceValidationStatus,
    ManualProgressState,
)
from app.domains.learning.models import LearningEvidence, ManualProgress


CRAFT_EVIDENCE_TYPE = "LEARNED_MANUAL_CREATION_COMPLETED"
CHIVALRY_EVIDENCE_TYPE = "CONFERENCE_WORK_PUBLISHED"


def growth_evidence_filter():
    """Legacy mastery/teaching proofs remain in lesson history, not growth totals."""
    return or_(
        LearningEvidence.category == EvidenceCategory.WISDOM,
        and_(
            LearningEvidence.category == EvidenceCategory.CRAFT,
            LearningEvidence.evidence_type == CRAFT_EVIDENCE_TYPE,
        ),
        and_(
            LearningEvidence.category == EvidenceCategory.CHIVALRY,
            LearningEvidence.evidence_type == CHIVALRY_EVIDENCE_TYPE,
        ),
    )


def award_creation_growth(
    db: Session, project: CreationProject, version: CreationVersion
) -> LearningEvidence | None:
    """Award once per work when a finished result uses at least one learned manual."""
    db.flush()
    if version.project_id != project.id:
        return None
    conversation = db.get(CreationConversation, project.id)
    if conversation is not None:
        complete = (
            conversation.result_version_id == version.id
            and conversation.status in {
                CreationConversationStatus.RESULT_READY,
                CreationConversationStatus.SAVED,
            }
        )
    else:
        complete = (
            project.current_stage == CreationStage.SEAL
            and project.current_version_number == version.version_number
        ) or db.scalar(
            select(ImageGenerationJob.id).where(
                ImageGenerationJob.output_version_id == version.id,
                ImageGenerationJob.status == ImageGenerationJobStatus.COMPLETED,
            ).limit(1)
        ) is not None
    if not complete:
        return None

    method = db.scalar(
        select(CreationMethod)
        .where(CreationMethod.project_id == project.id)
        .order_by(CreationMethod.version_number.desc())
        .limit(1)
    )
    manual_ids = set(db.scalars(
        select(LearningCardManual.manual_page_id)
        .where(LearningCardManual.creation_version_id == version.id)
    ))
    manual_ids.update(uuid.UUID(item) for item in (method.manual_page_ids if method else []))
    learned_id = db.scalar(
        select(ManualProgress.manual_page_id)
        .where(
            ManualProgress.user_id == project.owner_user_id,
            ManualProgress.manual_page_id.in_(manual_ids),
            ManualProgress.state.in_({
                ManualProgressState.LEARNED,
                ManualProgressState.MASTERED,
                ManualProgressState.TEACHING,
            }),
        )
        .order_by(ManualProgress.manual_page_id)
        .limit(1)
    )
    if learned_id is None:
        return None
    return _award(
        db, project,
        category=EvidenceCategory.CRAFT,
        evidence_type=CRAFT_EVIDENCE_TYPE,
        source_type="CREATION_VERSION",
        source_id=version.id,
        manual_page_id=learned_id,
        summary=f"运用已学秘籍完成作品《{project.title}》",
    )


def award_conference_growth(db: Session, publication: Publication) -> LearningEvidence | None:
    if (
        publication.status != PublicationStatus.PUBLISHED
        or publication.visibility != CreationVisibility.COMMUNITY
    ):
        return None
    project = db.get(CreationProject, publication.project_id)
    if project is None:
        return None
    return _award(
        db, project,
        category=EvidenceCategory.CHIVALRY,
        evidence_type=CHIVALRY_EVIDENCE_TYPE,
        source_type="PUBLICATION",
        source_id=publication.id,
        manual_page_id=None,
        summary=f"将完成的作品《{project.title}》发布到大会作品页",
    )


def _award(
    db: Session,
    project: CreationProject,
    *,
    category: EvidenceCategory,
    evidence_type: str,
    source_type: str,
    source_id: uuid.UUID,
    manual_page_id: uuid.UUID | None,
    summary: str,
) -> LearningEvidence:
    # Serialize different versions of the same work; the stable primary key also
    # prevents duplicate awards if a request/worker is retried after a commit.
    db.scalar(select(CreationProject.id).where(CreationProject.id == project.id).with_for_update())
    evidence_id = uuid.uuid5(project.id, f"growth:{category.value}")
    existing = db.get(LearningEvidence, evidence_id)
    if existing is not None:
        return existing
    now = utcnow()
    evidence = LearningEvidence(
        id=evidence_id,
        user_id=project.owner_user_id,
        category=category,
        evidence_type=evidence_type,
        source_type=source_type,
        source_id=source_id,
        manual_page_id=manual_page_id,
        summary=summary,
        rule_version="creation-growth-v1",
        validation_status=EvidenceValidationStatus.VALID,
        created_at=now,
        validated_at=now,
    )
    db.add(evidence)
    db.flush()
    return evidence
