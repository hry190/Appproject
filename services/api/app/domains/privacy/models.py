from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import Boolean, DateTime, Enum, ForeignKey, Integer
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base
from app.domains.creations.models import CreationVisibility


class PrivacySetting(Base):
    __tablename__ = "privacy_settings"

    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    default_work_visibility: Mapped[CreationVisibility] = mapped_column(
        Enum(CreationVisibility, native_enum=False, length=20),
        default=CreationVisibility.PRIVATE,
        nullable=False,
    )
    learning_card_public: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False
    )
    aigc_export_mark_enabled: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False
    )
    profile_discovery_enabled: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False
    )
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )
