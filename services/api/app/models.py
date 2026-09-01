from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import Boolean, DateTime, Enum, Float, ForeignKey, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.security import utcnow
from app.db import Base


class AgeBand(str, enum.Enum):
    UNDER_14 = "UNDER_14"
    AGE_14_TO_17 = "AGE_14_TO_17"
    ADULT = "ADULT"


class UserStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    LOCKED = "LOCKED"
    DELETED = "DELETED"


class GuardianStatus(str, enum.Enum):
    NOT_REQUIRED = "NOT_REQUIRED"
    VERIFIED = "VERIFIED"


class ConsentType(str, enum.Enum):
    TERMS = "TERMS"
    PRIVACY = "PRIVACY"
    GUARDIAN = "GUARDIAN"


class ConsentSubject(str, enum.Enum):
    SELF = "SELF"
    GUARDIAN = "GUARDIAN"


class ContentLevel(str, enum.Enum):
    CHILD = "CHILD"
    TEEN = "TEEN"
    STANDARD = "STANDARD"


class FeedbackCategory(str, enum.Enum):
    GENERAL = "GENERAL"
    BUG = "BUG"
    CONTENT_SAFETY = "CONTENT_SAFETY"
    ACCOUNT = "ACCOUNT"


class TicketStatus(str, enum.Enum):
    OPEN = "OPEN"
    PROCESSING = "PROCESSING"
    RESOLVED = "RESOLVED"


class DataRequestType(str, enum.Enum):
    ACCOUNT_DELETION = "ACCOUNT_DELETION"
    CONSENT_WITHDRAWAL = "CONSENT_WITHDRAWAL"


class DataRequestStatus(str, enum.Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    REJECTED = "REJECTED"


class User(Base):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    phone_ciphertext: Mapped[str] = mapped_column(Text, nullable=False)
    phone_lookup_hash: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    nickname: Mapped[str] = mapped_column(String(40), nullable=False)
    status: Mapped[UserStatus] = mapped_column(
        Enum(UserStatus, native_enum=False, length=16), default=UserStatus.ACTIVE, nullable=False
    )
    age_band: Mapped[AgeBand] = mapped_column(
        Enum(AgeBand, native_enum=False, length=20), nullable=False
    )
    guardian_status: Mapped[GuardianStatus] = mapped_column(
        Enum(GuardianStatus, native_enum=False, length=20), nullable=False
    )
    token_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )
    password_changed_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)

    credential: Mapped[Credential] = relationship(
        back_populates="user", cascade="all, delete-orphan", uselist=False
    )
    sessions: Mapped[list[AuthSession]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    preferences: Mapped[UserPreference | None] = relationship(
        back_populates="user", cascade="all, delete-orphan", uselist=False
    )
    guardian_controls: Mapped[GuardianControl | None] = relationship(
        back_populates="child", cascade="all, delete-orphan", uselist=False
    )


class Credential(Base):
    __tablename__ = "credentials"

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    password_hash: Mapped[str] = mapped_column(Text, nullable=False)
    failed_attempts: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    locked_until: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    user: Mapped[User] = relationship(back_populates="credential")


class AuthSession(Base):
    __tablename__ = "auth_sessions"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    family_id: Mapped[uuid.UUID] = mapped_column(index=True)
    parent_session_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("auth_sessions.id", ondelete="SET NULL")
    )
    refresh_token_hash: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    device_name: Mapped[str | None] = mapped_column(String(80))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    last_seen_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

    user: Mapped[User] = relationship(back_populates="sessions")


class ConsentRecord(Base):
    __tablename__ = "consent_records"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    consent_type: Mapped[ConsentType] = mapped_column(
        Enum(ConsentType, native_enum=False, length=16), nullable=False
    )
    document_version: Mapped[str] = mapped_column(String(32), nullable=False)
    subject: Mapped[ConsentSubject] = mapped_column(
        Enum(ConsentSubject, native_enum=False, length=16), nullable=False
    )
    evidence_id: Mapped[str] = mapped_column(String(64), nullable=False)
    agreed_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class GuardianLink(Base):
    __tablename__ = "guardian_links"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    child_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), unique=True
    )
    guardian_phone_hash: Mapped[str] = mapped_column(String(64), index=True)
    consent_evidence_id: Mapped[str] = mapped_column(String(64), nullable=False)
    verified_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class AuthAuditEvent(Base):
    __tablename__ = "auth_audit_events"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("users.id", ondelete="SET NULL"), index=True
    )
    phone_lookup_hash: Mapped[str | None] = mapped_column(String(64), index=True)
    event_type: Mapped[str] = mapped_column(String(40), nullable=False)
    result: Mapped[str] = mapped_column(String(20), nullable=False)
    request_id: Mapped[str] = mapped_column(String(64), nullable=False)
    network_key: Mapped[str | None] = mapped_column(String(64))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class UserPreference(Base):
    __tablename__ = "user_preferences"

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    message_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    learning_reminder: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    work_updates: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    service_messages: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    quiet_hours: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    auto_save: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    wifi_only: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    haptic_feedback: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    large_text: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    sound_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    music_volume: Mapped[float] = mapped_column(Float, default=0.65, nullable=False)
    effect_volume: Mapped[float] = mapped_column(Float, default=0.8, nullable=False)

    # Reserved for the settings entries that will be added by the frontend later.
    high_contrast: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    read_aloud: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    subtitles_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    personalization_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    rest_reminder: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    user: Mapped[User] = relationship(back_populates="preferences")


class GuardianControl(Base):
    __tablename__ = "guardian_controls"

    child_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    daily_limit_minutes: Mapped[int] = mapped_column(Integer, default=60, nullable=False)
    creation_allowed: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    content_level: Mapped[ContentLevel] = mapped_column(
        Enum(ContentLevel, native_enum=False, length=16),
        default=ContentLevel.CHILD,
        nullable=False,
    )
    minor_mode: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    child: Mapped[User] = relationship(back_populates="guardian_controls")


class FeedbackTicket(Base):
    __tablename__ = "feedback_tickets"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    category: Mapped[FeedbackCategory] = mapped_column(
        Enum(FeedbackCategory, native_enum=False, length=24), nullable=False
    )
    message: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[TicketStatus] = mapped_column(
        Enum(TicketStatus, native_enum=False, length=16),
        default=TicketStatus.OPEN,
        nullable=False,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class DataRightsRequest(Base):
    __tablename__ = "data_rights_requests"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    request_type: Mapped[DataRequestType] = mapped_column(
        Enum(DataRequestType, native_enum=False, length=24), nullable=False
    )
    reason: Mapped[str | None] = mapped_column(Text)
    status: Mapped[DataRequestStatus] = mapped_column(
        Enum(DataRequestStatus, native_enum=False, length=16),
        default=DataRequestStatus.PENDING,
        nullable=False,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class BlacklistEntry(Base):
    __tablename__ = "blacklist_entries"
    __table_args__ = (
        UniqueConstraint("owner_user_id", "blocked_user_id", name="uq_blacklist_owner_blocked"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    blocked_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index("ix_auth_audit_event_type_created", AuthAuditEvent.event_type, AuthAuditEvent.created_at)
