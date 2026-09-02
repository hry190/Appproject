from __future__ import annotations

import enum
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


class UploadPurpose(str, enum.Enum):
    CREATION_LAYER = "CREATION_LAYER"
    CREATION_PREVIEW = "CREATION_PREVIEW"
    PROVENANCE_PROOF = "PROVENANCE_PROOF"
    AIGC_OUTPUT = "AIGC_OUTPUT"
    AVATAR = "AVATAR"


class UploadSessionStatus(str, enum.Enum):
    ISSUED = "ISSUED"
    COMPLETED = "COMPLETED"
    EXPIRED = "EXPIRED"
    REJECTED = "REJECTED"


class MediaAssetStatus(str, enum.Enum):
    QUARANTINED = "QUARANTINED"
    PROCESSING = "PROCESSING"
    READY = "READY"
    REJECTED = "REJECTED"
    DELETION_PENDING = "DELETION_PENDING"
    DELETED = "DELETED"


class MediaDerivativeKind(str, enum.Enum):
    SANITIZED_ORIGINAL = "SANITIZED_ORIGINAL"
    THUMBNAIL_320 = "THUMBNAIL_320"
    THUMBNAIL_640 = "THUMBNAIL_640"


class MediaScanKind(str, enum.Enum):
    SIGNATURE = "SIGNATURE"
    HASH = "HASH"
    VIRUS = "VIRUS"
    DECODE = "DECODE"
    PIXEL_LIMIT = "PIXEL_LIMIT"
    METADATA = "METADATA"
    CONTENT_SAFETY = "CONTENT_SAFETY"
    AIGC = "AIGC"


class MediaScanOutcome(str, enum.Enum):
    PASSED = "PASSED"
    FAILED = "FAILED"
    REVIEW = "REVIEW"
    NOT_RUN = "NOT_RUN"


class OutboxStatus(str, enum.Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


class UploadSession(Base):
    __tablename__ = "upload_sessions"
    __table_args__ = (
        CheckConstraint("expected_bytes > 0", name="expected_bytes_positive"),
        UniqueConstraint(
            "owner_user_id", "object_key", name="uq_upload_sessions_owner_object"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    purpose: Mapped[UploadPurpose] = mapped_column(
        Enum(UploadPurpose, native_enum=False, length=24), nullable=False
    )
    original_filename: Mapped[str] = mapped_column(String(180), nullable=False)
    declared_mime: Mapped[str] = mapped_column(String(80), nullable=False)
    expected_bytes: Mapped[int] = mapped_column(Integer, nullable=False)
    client_sha256: Mapped[str] = mapped_column(String(64), nullable=False)
    object_key: Mapped[str] = mapped_column(String(300), nullable=False)
    status: Mapped[UploadSessionStatus] = mapped_column(
        Enum(UploadSessionStatus, native_enum=False, length=16), nullable=False
    )
    complete_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    complete_fingerprint: Mapped[str | None] = mapped_column(String(64))
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class MediaAsset(Base):
    __tablename__ = "media_assets"
    __table_args__ = (
        CheckConstraint("byte_size > 0", name="byte_size_positive"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    upload_session_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("upload_sessions.id", ondelete="RESTRICT"), unique=True, index=True
    )
    purpose: Mapped[UploadPurpose] = mapped_column(
        Enum(UploadPurpose, native_enum=False, length=24), nullable=False
    )
    original_filename: Mapped[str] = mapped_column(String(180), nullable=False)
    declared_mime: Mapped[str] = mapped_column(String(80), nullable=False)
    actual_mime: Mapped[str | None] = mapped_column(String(80))
    byte_size: Mapped[int] = mapped_column(Integer, nullable=False)
    sha256: Mapped[str] = mapped_column(String(64), nullable=False)
    quarantine_object_key: Mapped[str] = mapped_column(String(300), nullable=False)
    private_object_key: Mapped[str | None] = mapped_column(String(300))
    status: Mapped[MediaAssetStatus] = mapped_column(
        Enum(MediaAssetStatus, native_enum=False, length=24), nullable=False
    )
    width: Mapped[int | None] = mapped_column(Integer)
    height: Mapped[int | None] = mapped_column(Integer)
    metadata_stripped: Mapped[bool | None] = mapped_column()
    aigc_detected: Mapped[bool | None] = mapped_column()
    rejection_code: Mapped[str | None] = mapped_column(String(80))
    rejection_summary: Mapped[str | None] = mapped_column(String(500))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    ready_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class MediaDerivative(Base):
    __tablename__ = "media_derivatives"
    __table_args__ = (
        UniqueConstraint(
            "asset_id", "kind", name="uq_media_derivatives_asset_kind"
        ),
        CheckConstraint("byte_size > 0", name="derivative_byte_size_positive"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    asset_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("media_assets.id", ondelete="CASCADE"), index=True
    )
    kind: Mapped[MediaDerivativeKind] = mapped_column(
        Enum(MediaDerivativeKind, native_enum=False, length=24), nullable=False
    )
    mime_type: Mapped[str] = mapped_column(String(80), nullable=False)
    byte_size: Mapped[int] = mapped_column(Integer, nullable=False)
    storage_key: Mapped[str] = mapped_column(String(300), nullable=False, unique=True)
    width: Mapped[int] = mapped_column(Integer, nullable=False)
    height: Mapped[int] = mapped_column(Integer, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class MediaScanResult(Base):
    __tablename__ = "media_scan_results"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    asset_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("media_assets.id", ondelete="CASCADE"), index=True
    )
    scan_kind: Mapped[MediaScanKind] = mapped_column(
        Enum(MediaScanKind, native_enum=False, length=24), nullable=False
    )
    outcome: Mapped[MediaScanOutcome] = mapped_column(
        Enum(MediaScanOutcome, native_enum=False, length=16), nullable=False
    )
    reason_code: Mapped[str | None] = mapped_column(String(80))
    detector_version: Mapped[str] = mapped_column(String(80), nullable=False)
    details: Mapped[dict] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class OutboxEvent(Base):
    __tablename__ = "outbox_events"
    __table_args__ = (
        UniqueConstraint("deduplication_key", name="uq_outbox_events_deduplication"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    aggregate_type: Mapped[str] = mapped_column(String(40), nullable=False)
    aggregate_id: Mapped[uuid.UUID] = mapped_column(index=True)
    event_type: Mapped[str] = mapped_column(String(60), nullable=False, index=True)
    payload: Mapped[dict] = mapped_column(JSON, nullable=False)
    deduplication_key: Mapped[str] = mapped_column(String(120), nullable=False)
    status: Mapped[OutboxStatus] = mapped_column(
        Enum(OutboxStatus, native_enum=False, length=16), nullable=False
    )
    attempts: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    available_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    processed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    last_error_code: Mapped[str | None] = mapped_column(String(80))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index(
    "ix_upload_sessions_owner_status_created",
    UploadSession.owner_user_id,
    UploadSession.status,
    UploadSession.created_at,
)
Index(
    "ix_media_assets_owner_status_updated",
    MediaAsset.owner_user_id,
    MediaAsset.status,
    MediaAsset.updated_at,
)
Index(
    "ix_outbox_events_status_available",
    OutboxEvent.status,
    OutboxEvent.available_at,
)
