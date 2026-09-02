from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import DateTime, Enum, ForeignKey, Index, Integer, JSON, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base


class ModerationCaseStatus(str, enum.Enum):
    AUTO_CHECK = "AUTO_CHECK"
    HUMAN_REVIEW = "HUMAN_REVIEW"
    RESOLVED = "RESOLVED"


class ModerationRiskLevel(str, enum.Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class ModerationDecision(str, enum.Enum):
    PUBLISH = "PUBLISH"
    RETURN = "RETURN"
    RESTRICT = "RESTRICT"


class AppealStatus(str, enum.Enum):
    PENDING = "PENDING"
    UPHELD = "UPHELD"
    OVERTURNED = "OVERTURNED"


class ModerationCase(Base):
    __tablename__ = "moderation_cases"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="CASCADE"), unique=True, index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    status: Mapped[ModerationCaseStatus] = mapped_column(
        Enum(ModerationCaseStatus, native_enum=False, length=20), nullable=False
    )
    risk_level: Mapped[ModerationRiskLevel | None] = mapped_column(
        Enum(ModerationRiskLevel, native_enum=False, length=12)
    )
    automatic_reason_codes: Mapped[list[str]] = mapped_column(JSON, nullable=False)
    detector_version: Mapped[str | None] = mapped_column(String(80))
    decision: Mapped[ModerationDecision | None] = mapped_column(
        Enum(ModerationDecision, native_enum=False, length=12)
    )
    public_reason_code: Mapped[str | None] = mapped_column(String(80))
    public_reason_summary: Mapped[str | None] = mapped_column(String(500))
    revision_suggestion: Mapped[str | None] = mapped_column(String(500))
    minimal_evidence: Mapped[dict] = mapped_column(JSON, nullable=False)
    reviewer_reference: Mapped[str | None] = mapped_column(String(80))
    reviewed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ModerationAppeal(Base):
    __tablename__ = "moderation_appeals"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    moderation_case_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("moderation_cases.id", ondelete="CASCADE"), index=True
    )
    appellant_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    reason: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[AppealStatus] = mapped_column(
        Enum(AppealStatus, native_enum=False, length=16), nullable=False
    )
    reviewer_reference: Mapped[str | None] = mapped_column(String(80))
    resolution_summary: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    resolved_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class DomainAuditEvent(Base):
    __tablename__ = "domain_audit_events"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    actor_user_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("users.id", ondelete="SET NULL"), index=True
    )
    actor_type: Mapped[str] = mapped_column(String(24), nullable=False)
    action: Mapped[str] = mapped_column(String(60), nullable=False, index=True)
    target_type: Mapped[str] = mapped_column(String(40), nullable=False)
    target_id: Mapped[uuid.UUID] = mapped_column(index=True)
    result: Mapped[str] = mapped_column(String(20), nullable=False)
    request_id: Mapped[str] = mapped_column(String(64), nullable=False)
    safe_diff: Mapped[dict] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index(
    "ix_moderation_cases_owner_status_updated",
    ModerationCase.owner_user_id,
    ModerationCase.status,
    ModerationCase.updated_at,
)
Index(
    "ix_moderation_appeals_case_status_created",
    ModerationAppeal.moderation_case_id,
    ModerationAppeal.status,
    ModerationAppeal.created_at,
)
