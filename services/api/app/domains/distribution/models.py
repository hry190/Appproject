from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import CheckConstraint, DateTime, Enum, ForeignKey, Index, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base


class ClassroomStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"


class ClassroomMembershipStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    LEFT = "LEFT"


class PublicationDeliveryChannel(str, enum.Enum):
    GUARDIAN = "GUARDIAN"
    CLASSROOM = "CLASSROOM"


class PublicationDeliveryStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    REVOKED = "REVOKED"


class Classroom(Base):
    __tablename__ = "classrooms"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_teacher_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    name: Mapped[str] = mapped_column(String(80), nullable=False)
    join_code_hash: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    status: Mapped[ClassroomStatus] = mapped_column(
        Enum(ClassroomStatus, native_enum=False, length=16), nullable=False
    )
    row_version: Mapped[int] = mapped_column(default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ClassroomMembership(Base):
    __tablename__ = "classroom_memberships"
    __table_args__ = (
        UniqueConstraint(
            "classroom_id", "student_user_id", name="uq_classroom_memberships_classroom_student"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    classroom_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("classrooms.id", ondelete="CASCADE"), index=True
    )
    student_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    status: Mapped[ClassroomMembershipStatus] = mapped_column(
        Enum(ClassroomMembershipStatus, native_enum=False, length=16), nullable=False
    )
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    left_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class PublicationDelivery(Base):
    __tablename__ = "publication_deliveries"
    __table_args__ = (
        CheckConstraint(
            "(channel = 'GUARDIAN' AND recipient_lookup_hash IS NOT NULL AND classroom_id IS NULL) "
            "OR (channel = 'CLASSROOM' AND recipient_lookup_hash IS NULL AND classroom_id IS NOT NULL)",
            name="valid_delivery_target",
        ),
        UniqueConstraint(
            "publication_id", "channel", "recipient_lookup_hash", "classroom_id",
            name="uq_publication_deliveries_target",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="CASCADE"), index=True
    )
    channel: Mapped[PublicationDeliveryChannel] = mapped_column(
        Enum(PublicationDeliveryChannel, native_enum=False, length=16), nullable=False
    )
    recipient_user_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("users.id", ondelete="SET NULL"), index=True
    )
    recipient_lookup_hash: Mapped[str | None] = mapped_column(String(64), index=True)
    classroom_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("classrooms.id", ondelete="SET NULL"), index=True
    )
    status: Mapped[PublicationDeliveryStatus] = mapped_column(
        Enum(PublicationDeliveryStatus, native_enum=False, length=16), nullable=False
    )
    delivered_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


Index(
    "ix_classroom_memberships_student_status",
    ClassroomMembership.student_user_id,
    ClassroomMembership.status,
)
Index(
    "ix_publication_deliveries_recipient_status",
    PublicationDelivery.recipient_user_id,
    PublicationDelivery.status,
)
Index(
    "ix_publication_deliveries_lookup_status",
    PublicationDelivery.recipient_lookup_hash,
    PublicationDelivery.status,
)
