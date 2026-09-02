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
    JSON,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base


class CreationProjectStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"
    DELETED = "DELETED"


class CreationVisibility(str, enum.Enum):
    PRIVATE = "PRIVATE"
    GUARDIAN_ONLY = "GUARDIAN_ONLY"
    CLASSROOM = "CLASSROOM"
    COMMUNITY = "COMMUNITY"


class CreationMediaType(str, enum.Enum):
    ILLUSTRATION = "ILLUSTRATION"
    COMIC = "COMIC"
    MIXED_MEDIA = "MIXED_MEDIA"


class LayerKind(str, enum.Enum):
    DRAWING = "DRAWING"
    TEXT = "TEXT"
    IMAGE = "IMAGE"
    AI_GENERATED = "AI_GENERATED"
    REFERENCE = "REFERENCE"


class LearningCardStatus(str, enum.Enum):
    DRAFT = "DRAFT"
    COMPLETE = "COMPLETE"
    LOCKED = "LOCKED"


class ProvenanceStatus(str, enum.Enum):
    DRAFT = "DRAFT"
    COMPLETE = "COMPLETE"
    LOCKED = "LOCKED"


class ProvenanceItemType(str, enum.Enum):
    HUMAN_CONTRIBUTION = "HUMAN_CONTRIBUTION"
    AI_CONTRIBUTION = "AI_CONTRIBUTION"
    EXTERNAL_MATERIAL = "EXTERNAL_MATERIAL"


class MaterialLicenseType(str, enum.Enum):
    ORIGINAL = "ORIGINAL"
    CC0 = "CC0"
    CC_BY = "CC_BY"
    CC_BY_SA = "CC_BY_SA"
    PUBLIC_DOMAIN = "PUBLIC_DOMAIN"
    AUTHORIZED = "AUTHORIZED"
    UNKNOWN = "UNKNOWN"
    NOT_APPLICABLE = "NOT_APPLICABLE"


class PublicationStatus(str, enum.Enum):
    PENDING_CHECK = "PENDING_CHECK"
    PENDING_HUMAN_REVIEW = "PENDING_HUMAN_REVIEW"
    PUBLISHED = "PUBLISHED"
    RETURNED = "RETURNED"
    RESTRICTED = "RESTRICTED"
    WITHDRAWN = "WITHDRAWN"


class CreationChangeAction(str, enum.Enum):
    PROJECT_CREATED = "PROJECT_CREATED"
    PROJECT_METADATA_UPDATED = "PROJECT_METADATA_UPDATED"
    VERSION_CREATED = "VERSION_CREATED"
    LEARNING_CARD_UPDATED = "LEARNING_CARD_UPDATED"
    PROVENANCE_UPDATED = "PROVENANCE_UPDATED"
    SUBMITTED = "SUBMITTED"


class CreationProject(Base):
    __tablename__ = "creation_projects"
    __table_args__ = (
        CheckConstraint(
            "current_version_number IS NULL OR current_version_number >= 1",
            name="current_version_number_positive",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    title: Mapped[str] = mapped_column(String(100), nullable=False)
    description: Mapped[str | None] = mapped_column(Text)
    media_type: Mapped[CreationMediaType] = mapped_column(
        Enum(CreationMediaType, native_enum=False, length=20), nullable=False
    )
    status: Mapped[CreationProjectStatus] = mapped_column(
        Enum(CreationProjectStatus, native_enum=False, length=16),
        default=CreationProjectStatus.ACTIVE,
        nullable=False,
    )
    default_visibility: Mapped[CreationVisibility] = mapped_column(
        Enum(CreationVisibility, native_enum=False, length=20),
        default=CreationVisibility.PRIVATE,
        nullable=False,
    )
    current_version_number: Mapped[int | None] = mapped_column(Integer)
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class CreationVersion(Base):
    __tablename__ = "creation_versions"
    __table_args__ = (
        UniqueConstraint(
            "project_id", "version_number", name="uq_creation_versions_project_version"
        ),
        CheckConstraint("version_number >= 1", name="version_number_positive"),
        CheckConstraint("layer_count >= 1", name="layer_count_positive"),
        CheckConstraint("canvas_width > 0", name="canvas_width_positive"),
        CheckConstraint("canvas_height > 0", name="canvas_height_positive"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    version_number: Mapped[int] = mapped_column(Integer, nullable=False)
    parent_version_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), index=True
    )
    created_by_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    layer_manifest: Mapped[list[dict]] = mapped_column(JSON, nullable=False)
    layer_count: Mapped[int] = mapped_column(Integer, nullable=False)
    canvas_width: Mapped[int] = mapped_column(Integer, nullable=False)
    canvas_height: Mapped[int] = mapped_column(Integer, nullable=False)
    preview_asset_id: Mapped[uuid.UUID | None] = mapped_column(index=True)
    change_summary: Mapped[str] = mapped_column(String(500), nullable=False)
    modification_reason: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class CreationChangeLog(Base):
    __tablename__ = "creation_change_logs"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    version_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), index=True
    )
    actor_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    action: Mapped[CreationChangeAction] = mapped_column(
        Enum(CreationChangeAction, native_enum=False, length=32), nullable=False
    )
    summary: Mapped[str] = mapped_column(String(500), nullable=False)
    details: Mapped[dict] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class LearningCard(Base):
    __tablename__ = "learning_cards"

    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), primary_key=True
    )
    method_summary: Mapped[str] = mapped_column(Text, nullable=False)
    unresolved_questions: Mapped[list[str]] = mapped_column(JSON, nullable=False)
    questions_confirmed: Mapped[bool] = mapped_column(Boolean, nullable=False)
    status: Mapped[LearningCardStatus] = mapped_column(
        Enum(LearningCardStatus, native_enum=False, length=16), nullable=False
    )
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class LearningCardManual(Base):
    __tablename__ = "learning_card_manuals"

    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("learning_cards.creation_version_id", ondelete="CASCADE"),
        primary_key=True,
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="RESTRICT"), primary_key=True
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ProvenanceManifest(Base):
    __tablename__ = "provenance_manifests"

    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), primary_key=True
    )
    human_contribution_summary: Mapped[str] = mapped_column(Text, nullable=False)
    ai_assistance_used: Mapped[bool] = mapped_column(Boolean, nullable=False)
    ai_contribution_summary: Mapped[str | None] = mapped_column(Text)
    aigc_label_declared: Mapped[bool] = mapped_column(Boolean, nullable=False)
    unresolved_rights: Mapped[bool] = mapped_column(Boolean, nullable=False)
    status: Mapped[ProvenanceStatus] = mapped_column(
        Enum(ProvenanceStatus, native_enum=False, length=16), nullable=False
    )
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ProvenanceItem(Base):
    __tablename__ = "provenance_items"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("provenance_manifests.creation_version_id", ondelete="CASCADE"),
        index=True,
    )
    item_type: Mapped[ProvenanceItemType] = mapped_column(
        Enum(ProvenanceItemType, native_enum=False, length=24), nullable=False
    )
    contribution_type: Mapped[str] = mapped_column(String(80), nullable=False)
    description: Mapped[str] = mapped_column(String(500), nullable=False)
    source_url: Mapped[str | None] = mapped_column(String(500))
    source_author: Mapped[str | None] = mapped_column(String(100))
    license_type: Mapped[MaterialLicenseType] = mapped_column(
        Enum(MaterialLicenseType, native_enum=False, length=24), nullable=False
    )
    authorization_asset_id: Mapped[uuid.UUID | None] = mapped_column()
    ai_provider: Mapped[str | None] = mapped_column(String(80))
    ai_model: Mapped[str | None] = mapped_column(String(120))
    ai_tool_action: Mapped[str | None] = mapped_column(String(120))
    prompt_summary: Mapped[str | None] = mapped_column(String(500))
    output_asset_id: Mapped[uuid.UUID | None] = mapped_column()
    user_modified: Mapped[bool | None] = mapped_column(Boolean)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class Publication(Base):
    __tablename__ = "publications"
    __table_args__ = (
        UniqueConstraint(
            "owner_user_id",
            "idempotency_key",
            name="uq_publications_owner_idempotency",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), unique=True, index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    status: Mapped[PublicationStatus] = mapped_column(
        Enum(PublicationStatus, native_enum=False, length=24), nullable=False
    )
    visibility: Mapped[CreationVisibility] = mapped_column(
        Enum(CreationVisibility, native_enum=False, length=20), nullable=False
    )
    idempotency_key: Mapped[str] = mapped_column(String(64), nullable=False)
    request_fingerprint: Mapped[str] = mapped_column(String(64), nullable=False)
    return_reason_code: Mapped[str | None] = mapped_column(String(80))
    return_reason_summary: Mapped[str | None] = mapped_column(String(500))
    submitted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    published_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    returned_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    withdrawn_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


Index(
    "ix_creation_projects_owner_updated",
    CreationProject.owner_user_id,
    CreationProject.updated_at,
)
Index(
    "ix_creation_versions_project_created",
    CreationVersion.project_id,
    CreationVersion.created_at,
)
Index(
    "ix_creation_change_logs_project_created",
    CreationChangeLog.project_id,
    CreationChangeLog.created_at,
)
Index(
    "ix_publications_owner_status_updated",
    Publication.owner_user_id,
    Publication.status,
    Publication.updated_at,
)
