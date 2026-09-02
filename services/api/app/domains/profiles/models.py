from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    Boolean,
    DateTime,
    Enum,
    ForeignKey,
    Index,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.security import utcnow
from app.db import Base


class ProfileVisibility(str, enum.Enum):
    PRIVATE = "PRIVATE"
    GUARDIAN_ONLY = "GUARDIAN_ONLY"
    CLASSROOM = "CLASSROOM"
    COMMUNITY = "COMMUNITY"


class TitleDefinition(Base):
    __tablename__ = "title_definitions"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(40), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(40), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    unlock_rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class BadgeDefinition(Base):
    __tablename__ = "badge_definitions"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(40), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(40), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    unlock_rule_version: Mapped[str] = mapped_column(String(32), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class UserProfile(Base):
    __tablename__ = "user_profiles"

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    anonymous_id: Mapped[str] = mapped_column(String(16), unique=True, nullable=False)
    avatar_asset_id: Mapped[uuid.UUID | None] = mapped_column(index=True)
    class_label: Mapped[str | None] = mapped_column(String(20))
    current_title_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("title_definitions.id", ondelete="SET NULL"), index=True
    )
    profile_visibility: Mapped[ProfileVisibility] = mapped_column(
        Enum(ProfileVisibility, native_enum=False, length=20),
        default=ProfileVisibility.PRIVATE,
        nullable=False,
    )
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    current_title: Mapped[TitleDefinition | None] = relationship()


class UserTitle(Base):
    __tablename__ = "user_titles"
    __table_args__ = (
        UniqueConstraint("user_id", "title_id", name="uq_user_titles_user_title"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    title_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("title_definitions.id", ondelete="CASCADE"), index=True
    )
    evidence_ref: Mapped[str | None] = mapped_column(String(80))
    earned_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)

    title: Mapped[TitleDefinition] = relationship()


class UserBadge(Base):
    __tablename__ = "user_badges"
    __table_args__ = (
        UniqueConstraint("user_id", "badge_id", name="uq_user_badges_user_badge"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    badge_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("badge_definitions.id", ondelete="CASCADE"), index=True
    )
    evidence_ref: Mapped[str | None] = mapped_column(String(80))
    earned_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)

    badge: Mapped[BadgeDefinition] = relationship()


Index("ix_user_titles_user_earned", UserTitle.user_id, UserTitle.earned_at)
Index("ix_user_badges_user_earned", UserBadge.user_id, UserBadge.earned_at)
