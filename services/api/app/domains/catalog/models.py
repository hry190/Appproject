from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    Boolean,
    CheckConstraint,
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


class ManualContentStatus(str, enum.Enum):
    OUTLINE = "OUTLINE"
    READY = "READY"
    ARCHIVED = "ARCHIVED"


class ManualVolume(Base):
    __tablename__ = "manual_volumes"
    __table_args__ = (
        CheckConstraint("number BETWEEN 1 AND 10", name="number_range"),
        CheckConstraint("start_page <= end_page", name="page_range_order"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    number: Mapped[int] = mapped_column(Integer, unique=True, nullable=False)
    code: Mapped[str] = mapped_column(String(40), unique=True, nullable=False)
    title: Mapped[str] = mapped_column(String(80), nullable=False)
    core_domain: Mapped[str] = mapped_column(String(100), nullable=False)
    art_style: Mapped[str] = mapped_column(String(40), nullable=False)
    start_page: Mapped[int] = mapped_column(Integer, nullable=False)
    end_page: Mapped[int] = mapped_column(Integer, nullable=False)
    is_listed: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    pages: Mapped[list[ManualPage]] = relationship(
        back_populates="volume", cascade="all, delete-orphan"
    )


class ManualPage(Base):
    __tablename__ = "manual_pages"
    __table_args__ = (
        CheckConstraint("page_no BETWEEN 1 AND 50", name="page_no_range"),
        CheckConstraint("style_no BETWEEN 1 AND 5", name="style_no_range"),
        UniqueConstraint("volume_id", "style_no", name="uq_manual_pages_volume_style"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    volume_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_volumes.id", ondelete="CASCADE"), index=True
    )
    page_no: Mapped[int] = mapped_column(Integer, unique=True, nullable=False)
    style_no: Mapped[int] = mapped_column(Integer, nullable=False)
    slug: Mapped[str] = mapped_column(String(80), unique=True, nullable=False)
    title: Mapped[str] = mapped_column(String(80), nullable=False)
    core_logic: Mapped[str] = mapped_column(Text, nullable=False)
    life_hook: Mapped[str] = mapped_column(Text, nullable=False)
    interaction_evidence: Mapped[str] = mapped_column(Text, nullable=False)
    content_version: Mapped[str] = mapped_column(String(32), nullable=False)
    content_status: Mapped[ManualContentStatus] = mapped_column(
        Enum(ManualContentStatus, native_enum=False, length=16),
        default=ManualContentStatus.OUTLINE,
        nullable=False,
    )
    is_listed: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )

    volume: Mapped[ManualVolume] = relationship(back_populates="pages")


class UserManualFavorite(Base):
    __tablename__ = "user_manual_favorites"
    __table_args__ = (
        UniqueConstraint(
            "user_id", "manual_page_id", name="uq_user_manual_favorites_user_page"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="CASCADE"), index=True
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index(
    "ix_user_manual_favorites_user_created",
    UserManualFavorite.user_id,
    UserManualFavorite.created_at,
)
