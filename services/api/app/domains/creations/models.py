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


class ConferenceCategory(str, enum.Enum):
    ART = "ART"
    SCIENCE = "SCIENCE"
    MATH = "MATH"
    LANGUAGE = "LANGUAGE"


class CreationMediaType(str, enum.Enum):
    ILLUSTRATION = "ILLUSTRATION"
    COMIC = "COMIC"
    MIXED_MEDIA = "MIXED_MEDIA"
    VIDEO = "VIDEO"


class CreationStage(str, enum.Enum):
    IDEATION = "IDEATION"
    DRAFT = "DRAFT"
    PRODUCTION = "PRODUCTION"
    TEST = "TEST"
    SEAL = "SEAL"


class CreationIntentStatus(str, enum.Enum):
    ANALYZED = "ANALYZED"
    CONVERTED = "CONVERTED"
    EXPIRED = "EXPIRED"


class CreationToolKind(str, enum.Enum):
    COACH_REVIEW = "COACH_REVIEW"


class CreationToolCallStatus(str, enum.Enum):
    PROPOSED = "PROPOSED"
    COMPLETED = "COMPLETED"
    REJECTED = "REJECTED"
    EXPIRED = "EXPIRED"
    FAILED = "FAILED"


class CreationConversationStatus(str, enum.Enum):
    DIALOGUE = "DIALOGUE"
    GENERATING = "GENERATING"
    RESULT_READY = "RESULT_READY"
    SAVED = "SAVED"
    GENERATION_FAILED = "GENERATION_FAILED"


class CreationConversationRole(str, enum.Enum):
    STUDENT = "STUDENT"
    COACH = "COACH"


class CreationConversationMessageKind(str, enum.Enum):
    IDEA = "IDEA"
    MESSAGE = "MESSAGE"
    SUGGESTION = "SUGGESTION"


class CreationSuggestionDecision(str, enum.Enum):
    NONE = "NONE"
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REPLACED = "REPLACED"


class CreationTestResult(str, enum.Enum):
    PASSED = "PASSED"
    NEEDS_REVISION = "NEEDS_REVISION"
    BLOCKED = "BLOCKED"


class CreationIssueSeverity(str, enum.Enum):
    NOTE = "NOTE"
    IMPORTANT = "IMPORTANT"
    BLOCKING = "BLOCKING"


class CreationIssueStatus(str, enum.Enum):
    OPEN = "OPEN"
    RESOLVED = "RESOLVED"


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


class CreationSealStatus(str, enum.Enum):
    DRAFT = "DRAFT"
    COMPLETE = "COMPLETE"
    LOCKED = "LOCKED"


class ImageGenerationJobStatus(str, enum.Enum):
    QUEUED = "QUEUED"
    RUNNING = "RUNNING"
    SAFETY_CHECK = "SAFETY_CHECK"
    VERSIONING = "VERSIONING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    REJECTED = "REJECTED"


class ImageGenerationSize(str, enum.Enum):
    SQUARE = "SQUARE"
    PORTRAIT = "PORTRAIT"
    LANDSCAPE = "LANDSCAPE"


class ImageGenerationQuality(str, enum.Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class CreationExportFormat(str, enum.Enum):
    PNG = "PNG"
    JPEG = "JPEG"


class CreationExportJobStatus(str, enum.Enum):
    QUEUED = "QUEUED"
    RENDERING = "RENDERING"
    SAFETY_CHECK = "SAFETY_CHECK"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    REJECTED = "REJECTED"


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
    METHOD_UPDATED = "METHOD_UPDATED"
    STAGE_TRANSITIONED = "STAGE_TRANSITIONED"
    TOOL_CALL_RECORDED = "TOOL_CALL_RECORDED"
    TEST_RECORDED = "TEST_RECORDED"
    TEST_ISSUE_UPDATED = "TEST_ISSUE_UPDATED"
    SEAL_CHECK_UPDATED = "SEAL_CHECK_UPDATED"
    IMAGE_GENERATION_REQUESTED = "IMAGE_GENERATION_REQUESTED"
    IMAGE_GENERATION_COMPLETED = "IMAGE_GENERATION_COMPLETED"
    IMAGE_GENERATION_FAILED = "IMAGE_GENERATION_FAILED"
    EXPORT_REQUESTED = "EXPORT_REQUESTED"
    EXPORT_COMPLETED = "EXPORT_COMPLETED"
    EXPORT_FAILED = "EXPORT_FAILED"
    CONVERSATION_STARTED = "CONVERSATION_STARTED"
    CONVERSATION_MESSAGE_ADDED = "CONVERSATION_MESSAGE_ADDED"
    CONVERSATION_SUGGESTION_ACCEPTED = "CONVERSATION_SUGGESTION_ACCEPTED"
    CONVERSATION_RESULT_SAVED = "CONVERSATION_RESULT_SAVED"


class CreationProject(Base):
    __tablename__ = "creation_projects"
    __table_args__ = (
        CheckConstraint(
            "current_version_number IS NULL OR current_version_number >= 1",
            name="current_version_number_positive",
        ),
        UniqueConstraint(
            "owner_user_id",
            "create_idempotency_key",
            name="uq_creation_projects_owner_create_idempotency",
        ),
        UniqueConstraint(
            "source_intent_id",
            name="uq_creation_projects_source_intent",
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
    source_intent_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_intents.id", ondelete="SET NULL"), index=True
    )
    derivative_authorization_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey(
            "conference_derivative_authorizations.id",
            name="fk_creation_projects_derivative_authorization_id",
            ondelete="SET NULL",
            use_alter=True,
        ),
        index=True,
    )
    current_stage: Mapped[CreationStage] = mapped_column(
        Enum(CreationStage, native_enum=False, length=20),
        default=CreationStage.IDEATION,
        nullable=False,
    )
    stage_updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, nullable=False
    )
    create_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    create_request_fingerprint: Mapped[str | None] = mapped_column(String(64))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class CreationIntent(Base):
    __tablename__ = "creation_intents"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    text: Mapped[str] = mapped_column(Text, nullable=False)
    attachment_refs: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    attachment_asset_ids: Mapped[list[str]] = mapped_column(
        JSON, default=list, nullable=False
    )
    manual_page_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    resource_links: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    status: Mapped[CreationIntentStatus] = mapped_column(
        Enum(CreationIntentStatus, native_enum=False, length=16),
        default=CreationIntentStatus.ANALYZED,
        nullable=False,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)


class IntentAnalysis(Base):
    __tablename__ = "creation_intent_analyses"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    intent_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_intents.id", ondelete="CASCADE"), index=True
    )
    schema_version: Mapped[str] = mapped_column(String(20), nullable=False)
    suggestion: Mapped[dict] = mapped_column(JSON, nullable=False)
    confidence: Mapped[str] = mapped_column(String(12), nullable=False)
    safety_flags: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    model_ref: Mapped[str] = mapped_column(String(80), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class CreationMethod(Base):
    __tablename__ = "creation_methods"
    __table_args__ = (
        UniqueConstraint(
            "project_id", "version_number", name="uq_creation_methods_project_version"
        ),
        CheckConstraint("version_number >= 1", name="version_number_positive"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    version_number: Mapped[int] = mapped_column(Integer, nullable=False)
    created_by_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    name: Mapped[str] = mapped_column(String(80), nullable=False)
    goal: Mapped[str] = mapped_column(Text, nullable=False)
    audience: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    format: Mapped[str] = mapped_column(String(40), nullable=False)
    steps: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    resource_links: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    source_asset_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    manual_page_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class CreationStageEvent(Base):
    __tablename__ = "creation_stage_events"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    actor_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    from_stage: Mapped[CreationStage] = mapped_column(
        Enum(CreationStage, native_enum=False, length=20), nullable=False
    )
    to_stage: Mapped[CreationStage] = mapped_column(
        Enum(CreationStage, native_enum=False, length=20), nullable=False
    )
    reason: Mapped[str] = mapped_column(String(500), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class CreationToolCall(Base):
    __tablename__ = "creation_tool_calls"
    __table_args__ = (
        UniqueConstraint(
            "owner_user_id",
            "proposal_idempotency_key",
            name="uq_creation_tool_calls_owner_proposal_idempotency",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    kind: Mapped[CreationToolKind] = mapped_column(
        Enum(CreationToolKind, native_enum=False, length=24), nullable=False
    )
    status: Mapped[CreationToolCallStatus] = mapped_column(
        Enum(CreationToolCallStatus, native_enum=False, length=16), nullable=False
    )
    input_snapshot: Mapped[dict] = mapped_column(JSON, nullable=False)
    prompt_summary: Mapped[str] = mapped_column(String(500), nullable=False)
    effect_summary: Mapped[str] = mapped_column(String(500), nullable=False)
    external_data_shared: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False
    )
    output_snapshot: Mapped[dict | None] = mapped_column(JSON)
    executor_ref: Mapped[str | None] = mapped_column(String(80))
    proposal_idempotency_key: Mapped[str] = mapped_column(String(64), nullable=False)
    proposal_fingerprint: Mapped[str] = mapped_column(String(64), nullable=False)
    decision_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    decision_fingerprint: Mapped[str | None] = mapped_column(String(64))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    proposed_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, nullable=False
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    decided_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class CreationTestRecord(Base):
    __tablename__ = "creation_test_records"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    scenario: Mapped[str] = mapped_column(String(500), nullable=False)
    result: Mapped[CreationTestResult] = mapped_column(
        Enum(CreationTestResult, native_enum=False, length=24), nullable=False
    )
    notes: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class CreationTestIssue(Base):
    __tablename__ = "creation_test_issues"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    test_record_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_test_records.id", ondelete="CASCADE"), index=True
    )
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    severity: Mapped[CreationIssueSeverity] = mapped_column(
        Enum(CreationIssueSeverity, native_enum=False, length=16), nullable=False
    )
    description: Mapped[str] = mapped_column(String(500), nullable=False)
    status: Mapped[CreationIssueStatus] = mapped_column(
        Enum(CreationIssueStatus, native_enum=False, length=16), nullable=False
    )
    resolution_summary: Mapped[str | None] = mapped_column(String(500))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )
    resolved_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class CreationVersion(Base):
    __tablename__ = "creation_versions"
    __table_args__ = (
        UniqueConstraint(
            "project_id", "version_number", name="uq_creation_versions_project_version"
        ),
        UniqueConstraint(
            "project_id",
            "create_idempotency_key",
            name="uq_creation_versions_project_create_idempotency",
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
    create_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    create_request_fingerprint: Mapped[str | None] = mapped_column(String(64))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ImageGenerationJob(Base):
    __tablename__ = "image_generation_jobs"
    __table_args__ = (
        UniqueConstraint(
            "owner_user_id",
            "idempotency_key",
            name="uq_image_generation_jobs_owner_idempotency",
        ),
        CheckConstraint(
            "progress_percent >= 0 AND progress_percent <= 100",
            name="progress_percent_range",
        ),
        CheckConstraint("retry_count >= 0", name="retry_count_nonnegative"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    parent_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    prompt: Mapped[str] = mapped_column(Text, nullable=False)
    size: Mapped[ImageGenerationSize] = mapped_column(
        Enum(ImageGenerationSize, native_enum=False, length=16), nullable=False
    )
    quality: Mapped[ImageGenerationQuality] = mapped_column(
        Enum(ImageGenerationQuality, native_enum=False, length=16), nullable=False
    )
    status: Mapped[ImageGenerationJobStatus] = mapped_column(
        Enum(ImageGenerationJobStatus, native_enum=False, length=20), nullable=False
    )
    progress_percent: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    provider_ref: Mapped[str] = mapped_column(String(40), nullable=False)
    model_ref: Mapped[str] = mapped_column(String(120), nullable=False)
    external_data_shared: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False
    )
    output_asset_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("media_assets.id", ondelete="SET NULL"), index=True
    )
    output_version_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="SET NULL"), index=True
    )
    error_code: Mapped[str | None] = mapped_column(String(80))
    error_summary: Mapped[str | None] = mapped_column(String(500))
    retryable: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    quota_charged: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    retry_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    idempotency_key: Mapped[str] = mapped_column(String(64), nullable=False)
    request_fingerprint: Mapped[str] = mapped_column(String(64), nullable=False)
    last_retry_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    last_retry_fingerprint: Mapped[str | None] = mapped_column(String(64))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class CreationConversation(Base):
    __tablename__ = "creation_conversations"

    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), primary_key=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    status: Mapped[CreationConversationStatus] = mapped_column(
        Enum(CreationConversationStatus, native_enum=False, length=24),
        default=CreationConversationStatus.DIALOGUE,
        nullable=False,
    )
    initial_idea: Mapped[str] = mapped_column(Text, nullable=False)
    attachment_asset_ids: Mapped[list[str]] = mapped_column(
        JSON, default=list, nullable=False
    )
    manual_page_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    plan_summary: Mapped[str | None] = mapped_column(Text)
    active_suggestion_id: Mapped[uuid.UUID | None] = mapped_column(index=True)
    draft_version_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    saved_version_ids: Mapped[list[str]] = mapped_column(JSON, default=list, nullable=False)
    active_generation_job_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("image_generation_jobs.id", ondelete="SET NULL"), index=True
    )
    result_version_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="SET NULL"), index=True
    )
    last_generation_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    last_generation_request_fingerprint: Mapped[str | None] = mapped_column(String(64))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    started_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow, nullable=False
    )


class CreationConversationMessage(Base):
    __tablename__ = "creation_conversation_messages"
    __table_args__ = (
        UniqueConstraint(
            "owner_user_id",
            "client_idempotency_key",
            name="uq_creation_conversation_messages_owner_idempotency",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_conversations.project_id", ondelete="CASCADE"), index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    role: Mapped[CreationConversationRole] = mapped_column(
        Enum(CreationConversationRole, native_enum=False, length=16), nullable=False
    )
    kind: Mapped[CreationConversationMessageKind] = mapped_column(
        Enum(CreationConversationMessageKind, native_enum=False, length=16),
        nullable=False,
    )
    content: Mapped[str] = mapped_column(Text, nullable=False)
    decision: Mapped[CreationSuggestionDecision] = mapped_column(
        Enum(CreationSuggestionDecision, native_enum=False, length=16),
        default=CreationSuggestionDecision.NONE,
        nullable=False,
    )
    in_reply_to_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_conversation_messages.id", ondelete="SET NULL"), index=True
    )
    client_idempotency_key: Mapped[str | None] = mapped_column(String(64))
    request_fingerprint: Mapped[str | None] = mapped_column(String(64))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, nullable=False
    )


class CreationExportJob(Base):
    __tablename__ = "creation_export_jobs"
    __table_args__ = (
        UniqueConstraint(
            "owner_user_id",
            "idempotency_key",
            name="uq_creation_export_jobs_owner_idempotency",
        ),
        CheckConstraint(
            "progress_percent >= 0 AND progress_percent <= 100",
            name="progress_percent_range",
        ),
        CheckConstraint("output_scale >= 1 AND output_scale <= 2", name="output_scale_range"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_projects.id", ondelete="CASCADE"), index=True
    )
    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), index=True
    )
    owner_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    format: Mapped[CreationExportFormat] = mapped_column(
        Enum(CreationExportFormat, native_enum=False, length=8), nullable=False
    )
    output_scale: Mapped[int] = mapped_column(Integer, nullable=False)
    status: Mapped[CreationExportJobStatus] = mapped_column(
        Enum(CreationExportJobStatus, native_enum=False, length=20), nullable=False
    )
    progress_percent: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    output_asset_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("media_assets.id", ondelete="SET NULL"), index=True
    )
    error_code: Mapped[str | None] = mapped_column(String(80))
    error_summary: Mapped[str | None] = mapped_column(String(500))
    idempotency_key: Mapped[str] = mapped_column(String(64), nullable=False)
    request_fingerprint: Mapped[str] = mapped_column(String(64), nullable=False)
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


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


class CreationSealCheck(Base):
    __tablename__ = "creation_seal_checks"

    creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="CASCADE"), primary_key=True
    )
    work_description: Mapped[str] = mapped_column(Text, nullable=False)
    learning_reflection: Mapped[str] = mapped_column(Text, nullable=False)
    next_improvement: Mapped[str] = mapped_column(Text, nullable=False)
    identity_privacy_confirmed: Mapped[bool] = mapped_column(Boolean, nullable=False)
    contact_privacy_confirmed: Mapped[bool] = mapped_column(Boolean, nullable=False)
    portrait_rights_confirmed: Mapped[bool] = mapped_column(Boolean, nullable=False)
    status: Mapped[CreationSealStatus] = mapped_column(
        Enum(CreationSealStatus, native_enum=False, length=16), nullable=False
    )
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


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
    classroom_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("classrooms.id", ondelete="SET NULL"), index=True
    )
    status: Mapped[PublicationStatus] = mapped_column(
        Enum(PublicationStatus, native_enum=False, length=24), nullable=False
    )
    visibility: Mapped[CreationVisibility] = mapped_column(
        Enum(CreationVisibility, native_enum=False, length=20), nullable=False
    )
    conference_category: Mapped[ConferenceCategory | None] = mapped_column(
        Enum(ConferenceCategory, native_enum=False, length=16), index=True
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
Index(
    "ix_image_generation_jobs_owner_created",
    ImageGenerationJob.owner_user_id,
    ImageGenerationJob.created_at,
)
Index(
    "ix_image_generation_jobs_project_status",
    ImageGenerationJob.project_id,
    ImageGenerationJob.status,
)
Index(
    "ix_creation_export_jobs_owner_created",
    CreationExportJob.owner_user_id,
    CreationExportJob.created_at,
)
Index(
    "ix_creation_export_jobs_version_status",
    CreationExportJob.creation_version_id,
    CreationExportJob.status,
)
