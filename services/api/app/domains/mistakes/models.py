from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import (
    CheckConstraint,
    DateTime,
    Enum,
    ForeignKey,
    Index,
    Integer,
    JSON,
    String,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base
from app.domains.learning.contracts import MistakeStatus, TrialAttemptResult


class MistakeItem(Base):
    __tablename__ = "mistake_items"
    __table_args__ = (
        UniqueConstraint(
            "user_id",
            "trial_id",
            "knowledge_point_code",
            name="uq_mistake_items_user_trial_knowledge",
        ),
        CheckConstraint("failure_count >= 1", name="failure_count_positive"),
        CheckConstraint(
            "successful_retries >= 0", name="successful_retries_non_negative"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    trial_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trials.id", ondelete="RESTRICT"), index=True
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="RESTRICT"), index=True
    )
    knowledge_point_code: Mapped[str] = mapped_column(String(80), nullable=False)
    first_attempt_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trial_attempts.id", ondelete="RESTRICT"), index=True
    )
    latest_attempt_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trial_attempts.id", ondelete="RESTRICT"), index=True
    )
    original_answer_payload: Mapped[object] = mapped_column(JSON, nullable=False)
    error_reason_code: Mapped[str] = mapped_column(String(80), nullable=False)
    error_reason_summary: Mapped[str] = mapped_column(String(240), nullable=False)
    status: Mapped[MistakeStatus] = mapped_column(
        Enum(MistakeStatus, native_enum=False, length=16),
        default=MistakeStatus.TO_REVIEW,
        nullable=False,
    )
    failure_count: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    successful_retries: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    next_review_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    consolidated_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class RemediationContext(Base):
    __tablename__ = "remediation_contexts"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    mistake_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("mistake_items.id", ondelete="CASCADE"), index=True
    )
    trial_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trial_versions.id", ondelete="RESTRICT"), index=True
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    used_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class RemediationRecord(Base):
    __tablename__ = "remediation_records"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    mistake_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("mistake_items.id", ondelete="CASCADE"), index=True
    )
    attempt_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("trial_attempts.id", ondelete="RESTRICT"), unique=True, index=True
    )
    result: Mapped[TrialAttemptResult] = mapped_column(
        Enum(TrialAttemptResult, native_enum=False, length=16), nullable=False
    )
    reflection: Mapped[str | None] = mapped_column(String(1000))
    occurred_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index(
    "ix_mistake_items_user_status_updated",
    MistakeItem.user_id,
    MistakeItem.status,
    MistakeItem.updated_at,
)
Index(
    "ix_remediation_contexts_user_expires",
    RemediationContext.user_id,
    RemediationContext.expires_at,
)
Index(
    "ix_remediation_records_mistake_occurred",
    RemediationRecord.mistake_id,
    RemediationRecord.occurred_at,
)
