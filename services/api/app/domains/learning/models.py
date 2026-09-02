from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    Boolean,
    CheckConstraint,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Index,
    Integer,
    JSON,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.security import utcnow
from app.db import Base
from app.domains.learning.contracts import (
    EvidenceCategory,
    EvidenceValidationStatus,
    LearningEventType,
    ManualProgressState,
    TrialAttemptResult,
)


class TrialStatus(str, enum.Enum):
    DRAFT = "DRAFT"
    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"


class TrialGraderKind(str, enum.Enum):
    EXACT_JSON = "EXACT_JSON"


class Trial(Base):
    __tablename__ = "trials"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="CASCADE"), index=True
    )
    code: Mapped[str] = mapped_column(String(80), unique=True, nullable=False)
    title: Mapped[str] = mapped_column(String(100), nullable=False)
    knowledge_point_code: Mapped[str] = mapped_column(String(80), nullable=False)
    status: Mapped[TrialStatus] = mapped_column(
        Enum(TrialStatus, native_enum=False, length=16),
        default=TrialStatus.DRAFT,
        nullable=False,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    versions: Mapped[list[TrialVersion]] = relationship(
        back_populates="trial", cascade="all, delete-orphan"
    )


class TrialVersion(Base):
    __tablename__ = "trial_versions"
    __table_args__ = (
        UniqueConstraint("trial_id", "version", name="uq_trial_versions_trial_version"),
        CheckConstraint("version >= 1", name="version_positive"),
        CheckConstraint("max_score > 0", name="max_score_positive"),
        CheckConstraint(
            "pass_score >= 0 AND pass_score <= max_score",
            name="pass_score_range",
        ),
        CheckConstraint(
            "min_explanation_length >= 0", name="min_explanation_length_non_negative"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    trial_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trials.id", ondelete="CASCADE"), index=True
    )
    version: Mapped[int] = mapped_column(Integer, nullable=False)
    prompt: Mapped[str] = mapped_column(Text, nullable=False)
    prediction_prompt: Mapped[str] = mapped_column(Text, nullable=False)
    answer_schema: Mapped[dict] = mapped_column(JSON, nullable=False)
    grader_kind: Mapped[TrialGraderKind] = mapped_column(
        Enum(TrialGraderKind, native_enum=False, length=20), nullable=False
    )
    grader_config: Mapped[dict] = mapped_column(JSON, nullable=False)
    max_score: Mapped[float] = mapped_column(Float, nullable=False)
    pass_score: Mapped[float] = mapped_column(Float, nullable=False)
    prediction_required: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    explanation_required: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    min_explanation_length: Mapped[int] = mapped_column(Integer, default=10, nullable=False)
    evidence_category: Mapped[EvidenceCategory] = mapped_column(
        Enum(EvidenceCategory, native_enum=False, length=16), nullable=False
    )
    rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    published_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)

    trial: Mapped[Trial] = relationship(back_populates="versions")


class PracticeSession(Base):
    __tablename__ = "practice_sessions"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    source_type: Mapped[str] = mapped_column(String(40), nullable=False)
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    qualified_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    ended_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class TrialAttempt(Base):
    __tablename__ = "trial_attempts"
    __table_args__ = (
        UniqueConstraint(
            "user_id", "idempotency_key", name="uq_trial_attempts_user_idempotency"
        ),
        CheckConstraint("server_score >= 0", name="server_score_non_negative"),
        CheckConstraint("max_score > 0", name="max_score_positive"),
        CheckConstraint("server_score <= max_score", name="server_score_not_over_max"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    trial_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trials.id", ondelete="RESTRICT"), index=True
    )
    trial_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trial_versions.id", ondelete="RESTRICT"), index=True
    )
    practice_session_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("practice_sessions.id", ondelete="RESTRICT"), index=True
    )
    prediction_payload: Mapped[object | None] = mapped_column(JSON)
    answer_payload: Mapped[object] = mapped_column(JSON, nullable=False)
    explanation: Mapped[str | None] = mapped_column(Text)
    server_score: Mapped[float] = mapped_column(Float, nullable=False)
    max_score: Mapped[float] = mapped_column(Float, nullable=False)
    result: Mapped[TrialAttemptResult] = mapped_column(
        Enum(TrialAttemptResult, native_enum=False, length=16), nullable=False
    )
    feedback_codes: Mapped[list[str]] = mapped_column(JSON, nullable=False)
    idempotency_key: Mapped[str] = mapped_column(String(64), nullable=False)
    request_fingerprint: Mapped[str] = mapped_column(String(64), nullable=False)
    client_request_id: Mapped[str | None] = mapped_column(String(64))
    response_payload: Mapped[dict] = mapped_column(JSON, nullable=False)
    submitted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    graded_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class LearningEvent(Base):
    __tablename__ = "learning_events"
    __table_args__ = (
        UniqueConstraint(
            "user_id", "idempotency_key", name="uq_learning_events_user_idempotency"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    event_type: Mapped[LearningEventType] = mapped_column(
        Enum(LearningEventType, native_enum=False, length=40), nullable=False
    )
    source_type: Mapped[str] = mapped_column(String(40), nullable=False)
    source_id: Mapped[uuid.UUID] = mapped_column(index=True)
    manual_page_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="SET NULL"), index=True
    )
    practice_session_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("practice_sessions.id", ondelete="SET NULL"), index=True
    )
    rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    payload: Mapped[dict] = mapped_column(JSON, nullable=False)
    idempotency_key: Mapped[str] = mapped_column(String(96), nullable=False)
    occurred_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    revocation_reason: Mapped[str | None] = mapped_column(String(160))


class LearningEvidence(Base):
    __tablename__ = "learning_evidence"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    category: Mapped[EvidenceCategory] = mapped_column(
        Enum(EvidenceCategory, native_enum=False, length=16), nullable=False
    )
    evidence_type: Mapped[str] = mapped_column(String(60), nullable=False)
    source_type: Mapped[str] = mapped_column(String(40), nullable=False)
    source_id: Mapped[uuid.UUID] = mapped_column(index=True)
    manual_page_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="SET NULL"), index=True
    )
    summary: Mapped[str] = mapped_column(String(240), nullable=False)
    rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    validation_status: Mapped[EvidenceValidationStatus] = mapped_column(
        Enum(EvidenceValidationStatus, native_enum=False, length=20), nullable=False
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    validated_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ManualProgress(Base):
    __tablename__ = "manual_progress"

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="CASCADE"), primary_key=True
    )
    state: Mapped[ManualProgressState] = mapped_column(
        Enum(ManualProgressState, native_enum=False, length=16),
        default=ManualProgressState.UNSEEN,
        nullable=False,
    )
    discovered_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    learned_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    mastered_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    teaching_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    latest_evidence_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("learning_evidence.id", ondelete="SET NULL"), index=True
    )
    projection_version: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ProgressTransition(Base):
    __tablename__ = "progress_transitions"
    __table_args__ = (
        CheckConstraint("projection_version >= 1", name="projection_version_positive"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="CASCADE"), index=True
    )
    previous_state: Mapped[ManualProgressState] = mapped_column(
        Enum(ManualProgressState, native_enum=False, length=16), nullable=False
    )
    current_state: Mapped[ManualProgressState] = mapped_column(
        Enum(ManualProgressState, native_enum=False, length=16), nullable=False
    )
    event_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("learning_events.id", ondelete="RESTRICT"), index=True
    )
    evidence_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("learning_evidence.id", ondelete="SET NULL"), index=True
    )
    rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    projection_version: Mapped[int] = mapped_column(Integer, nullable=False)
    occurred_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class UserLearningStats(Base):
    __tablename__ = "user_learning_stats"
    __table_args__ = (
        CheckConstraint("lifetime_practice_count >= 0", name="practice_count_non_negative"),
        CheckConstraint("lifetime_practice_days >= 0", name="practice_days_non_negative"),
        CheckConstraint("distinct_trials_passed >= 0", name="trials_passed_non_negative"),
        CheckConstraint("wisdom_count >= 0", name="wisdom_count_non_negative"),
        CheckConstraint("craft_count >= 0", name="craft_count_non_negative"),
        CheckConstraint("chivalry_count >= 0", name="chivalry_count_non_negative"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    lifetime_practice_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    lifetime_practice_days: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    distinct_trials_passed: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    wisdom_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    craft_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    chivalry_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    wisdom_latest_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    craft_latest_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    chivalry_latest_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    last_practice_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    projection_version: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


Index(
    "ix_practice_sessions_user_qualified",
    PracticeSession.user_id,
    PracticeSession.qualified_at,
)
Index(
    "ix_trial_attempts_user_trial_submitted",
    TrialAttempt.user_id,
    TrialAttempt.trial_id,
    TrialAttempt.submitted_at,
)
Index("ix_learning_events_user_occurred", LearningEvent.user_id, LearningEvent.occurred_at)
Index("ix_learning_events_source", LearningEvent.source_type, LearningEvent.source_id)
Index(
    "ix_learning_evidence_user_category_status_created",
    LearningEvidence.user_id,
    LearningEvidence.category,
    LearningEvidence.validation_status,
    LearningEvidence.created_at,
)
Index(
    "ix_manual_progress_user_state_updated",
    ManualProgress.user_id,
    ManualProgress.state,
    ManualProgress.updated_at,
)
Index(
    "ix_progress_transitions_user_page_occurred",
    ProgressTransition.user_id,
    ProgressTransition.manual_page_id,
    ProgressTransition.occurred_at,
)
