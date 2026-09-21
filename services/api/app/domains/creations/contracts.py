from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import Field, field_validator, model_validator

from app.domains.creations.models import (
    ConferenceCategory,
    CreationChangeAction,
    CreationConversationMessageKind,
    CreationConversationRole,
    CreationConversationStatus,
    CreationExportFormat,
    CreationExportJobStatus,
    CreationIssueSeverity,
    CreationIssueStatus,
    CreationMediaType,
    CreationProjectStatus,
    CreationSealStatus,
    CreationStage,
    CreationSuggestionDecision,
    CreationTestResult,
    CreationToolCallStatus,
    CreationToolKind,
    CreationVisibility,
    ImageGenerationJobStatus,
    ImageGenerationQuality,
    ImageGenerationSize,
    LayerKind,
    LearningCardStatus,
    MaterialLicenseType,
    ProvenanceItemType,
    ProvenanceStatus,
    PublicationStatus,
)
from app.domains.learning.contracts import ContractModel
from app.domains.media.contracts import MediaAssetPublic


class CreationDisplayStatus(str, Enum):
    DRAFT = "DRAFT"
    PENDING_CHECK = "PENDING_CHECK"
    PENDING_HUMAN_REVIEW = "PENDING_HUMAN_REVIEW"
    PUBLISHED = "PUBLISHED"
    RETURNED = "RETURNED"
    RESTRICTED = "RESTRICTED"
    WITHDRAWN = "WITHDRAWN"


class LayerSnapshot(ContractModel):
    layer_id: str = Field(min_length=1, max_length=64, pattern=r"^[A-Za-z0-9._:-]+$")
    kind: LayerKind
    name: str = Field(min_length=1, max_length=80)
    z_index: int = Field(ge=0, le=999)
    visible: bool = True
    asset_id: uuid.UUID | None = None
    text_content: str | None = Field(default=None, max_length=5000)
    aigc: bool = False
    offset_x: float = Field(default=0, ge=-10000, le=10000)
    offset_y: float = Field(default=0, ge=-10000, le=10000)
    scale: float = Field(default=1, ge=0.1, le=10)
    rotation_degrees: float = Field(default=0, ge=-3600, le=3600)
    opacity: float = Field(default=1, ge=0, le=1)
    crop_inset: float = Field(default=0, ge=0, le=0.45)
    font_size: int | None = Field(default=None, ge=8, le=512)
    text_color: str | None = Field(default=None, pattern=r"^#[0-9A-Fa-f]{6}$")

    @model_validator(mode="after")
    def validate_layer_source(self) -> "LayerSnapshot":
        if self.kind == LayerKind.TEXT and not (self.text_content or "").strip():
            raise ValueError("TEXT layer requires text_content")
        if self.kind in {
            LayerKind.DRAWING,
            LayerKind.IMAGE,
            LayerKind.AI_GENERATED,
            LayerKind.REFERENCE,
        } and self.asset_id is None:
            raise ValueError(f"{self.kind.value} layer requires asset_id")
        if self.kind == LayerKind.AI_GENERATED and not self.aigc:
            raise ValueError("AI_GENERATED layer must set aigc=true")
        return self


class CreationProjectCreate(ContractModel):
    title: str = Field(min_length=1, max_length=100)
    description: str | None = Field(default=None, max_length=2000)
    intent_id: uuid.UUID | None = None
    derivative_authorization_id: uuid.UUID | None = None
    media_type: CreationMediaType
    default_visibility: CreationVisibility = CreationVisibility.PRIVATE

    @field_validator("title")
    @classmethod
    def title_must_not_be_blank(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("title must not be blank")
        return value


class CreationIntentAnalyze(ContractModel):
    text: str = Field(min_length=2, max_length=500)
    attachment_refs: list[str] = Field(default_factory=list, max_length=10)
    attachment_asset_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    manual_page_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    resource_links: list[str] = Field(default_factory=list, max_length=20)

    @field_validator("text")
    @classmethod
    def intent_must_not_be_blank(cls, value: str) -> str:
        value = value.strip()
        if len(value) < 2:
            raise ValueError("text must contain at least two non-space characters")
        return value

    @field_validator("attachment_asset_ids", "manual_page_ids")
    @classmethod
    def source_ids_must_be_unique(cls, value: list[uuid.UUID]) -> list[uuid.UUID]:
        if len(value) != len(set(value)):
            raise ValueError("source IDs must be unique")
        return value


class CreationMethodDraft(ContractModel):
    name: str
    goal: str
    audience: list[str]
    format: str
    steps: list[str]
    resource_links: list[str]
    source_asset_ids: list[uuid.UUID]
    manual_page_ids: list[uuid.UUID]
    recommended_media_type: CreationMediaType


class CreationIntentAnalysisPublic(ContractModel):
    intent_id: uuid.UUID
    analysis_id: uuid.UUID
    schema_version: str
    method_draft: CreationMethodDraft
    questions: list[str]
    safety_flags: list[str]
    confidence: str
    expires_at: datetime


class CreationConversationStart(ContractModel):
    idea: str = Field(min_length=2, max_length=500)
    title: str | None = Field(default=None, max_length=100)
    attachment_asset_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    manual_page_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    resource_links: list[str] = Field(default_factory=list, max_length=20)
    derivative_authorization_id: uuid.UUID | None = None

    @field_validator("idea")
    @classmethod
    def conversation_idea_must_not_be_blank(cls, value: str) -> str:
        value = " ".join(value.split())
        if len(value) < 2:
            raise ValueError("idea must contain at least two characters")
        return value

    @field_validator("attachment_asset_ids", "manual_page_ids")
    @classmethod
    def conversation_source_ids_must_be_unique(
        cls, value: list[uuid.UUID]
    ) -> list[uuid.UUID]:
        if len(value) != len(set(value)):
            raise ValueError("source IDs must be unique")
        return value


class CreationConversationMessageCreate(ContractModel):
    text: str = Field(min_length=2, max_length=1000)

    @field_validator("text")
    @classmethod
    def conversation_message_must_not_be_blank(cls, value: str) -> str:
        value = " ".join(value.split())
        if len(value) < 2:
            raise ValueError("message must contain at least two characters")
        return value


class CreationConversationMessagePublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    role: CreationConversationRole
    kind: CreationConversationMessageKind
    content: str
    decision: CreationSuggestionDecision
    in_reply_to_id: uuid.UUID | None
    created_at: datetime


class CreationConversationGenerate(ContractModel):
    expected_revision: int = Field(ge=1)
    user_confirmed_generation: bool

    @model_validator(mode="after")
    def conversation_generation_requires_confirmation(
        self,
    ) -> "CreationConversationGenerate":
        if not self.user_confirmed_generation:
            raise ValueError("generation requires explicit user confirmation")
        return self


class CreationConversationResultAction(ContractModel):
    expected_revision: int = Field(ge=1)


class CreationProjectPatch(ContractModel):
    title: str | None = Field(default=None, min_length=1, max_length=100)
    description: str | None = Field(default=None, max_length=2000)
    default_visibility: CreationVisibility | None = None
    row_version: int = Field(ge=1)

    @model_validator(mode="after")
    def at_least_one_change(self) -> "CreationProjectPatch":
        changed = self.model_fields_set - {"row_version"}
        if not changed:
            raise ValueError("at least one project field must be supplied")
        if self.title is not None:
            self.title = self.title.strip()
            if not self.title:
                raise ValueError("title must not be blank")
        return self


class CreationVersionCreate(ContractModel):
    parent_version_id: uuid.UUID | None = None
    layers: list[LayerSnapshot] = Field(min_length=1, max_length=200)
    canvas_width: int = Field(ge=1, le=10000)
    canvas_height: int = Field(ge=1, le=10000)
    preview_asset_id: uuid.UUID | None = None
    change_summary: str = Field(min_length=1, max_length=500)
    modification_reason: str | None = Field(default=None, max_length=500)

    @field_validator("layers")
    @classmethod
    def layer_ids_and_z_indexes_must_be_unique(
        cls, value: list[LayerSnapshot]
    ) -> list[LayerSnapshot]:
        layer_ids = [layer.layer_id for layer in value]
        if len(layer_ids) != len(set(layer_ids)):
            raise ValueError("layer_id values must be unique")
        z_indexes = [layer.z_index for layer in value]
        if len(z_indexes) != len(set(z_indexes)):
            raise ValueError("z_index values must be unique")
        return value


class PublicationPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    creation_version_id: uuid.UUID
    status: PublicationStatus
    visibility: CreationVisibility
    conference_category: ConferenceCategory | None
    classroom_id: uuid.UUID | None = None
    return_reason_code: str | None
    return_reason_summary: str | None
    submitted_at: datetime
    published_at: datetime | None
    returned_at: datetime | None
    withdrawn_at: datetime | None
    row_version: int = Field(ge=1)
    updated_at: datetime


class CreationProjectPublic(ContractModel):
    id: uuid.UUID
    title: str
    description: str | None
    media_type: CreationMediaType
    status: CreationProjectStatus
    default_visibility: CreationVisibility
    current_version_number: int | None
    source_intent_id: uuid.UUID | None
    derivative_authorization_id: uuid.UUID | None
    current_stage: CreationStage
    display_status: CreationDisplayStatus
    latest_publication: PublicationPublic | None
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime


class CreationMethodPut(ContractModel):
    name: str = Field(min_length=2, max_length=80)
    goal: str = Field(min_length=2, max_length=1000)
    audience: list[str] = Field(min_length=1, max_length=10)
    format: str = Field(min_length=2, max_length=40)
    steps: list[str] = Field(min_length=1, max_length=20)
    resource_links: list[str] = Field(default_factory=list, max_length=20)
    source_asset_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    manual_page_ids: list[uuid.UUID] = Field(default_factory=list, max_length=10)
    expected_revision: int = Field(ge=1)

    @field_validator("source_asset_ids", "manual_page_ids")
    @classmethod
    def method_source_ids_must_be_unique(
        cls, value: list[uuid.UUID]
    ) -> list[uuid.UUID]:
        if len(value) != len(set(value)):
            raise ValueError("source IDs must be unique")
        return value


class CreationMethodPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    version_number: int = Field(ge=1)
    name: str
    goal: str
    audience: list[str]
    format: str
    steps: list[str]
    resource_links: list[str]
    source_asset_ids: list[uuid.UUID]
    manual_page_ids: list[uuid.UUID]
    project_revision: int = Field(ge=1)
    created_at: datetime


class CreationStageTransition(ContractModel):
    from_stage: CreationStage
    to_stage: CreationStage
    reason: str = Field(min_length=2, max_length=500)
    expected_revision: int = Field(ge=1)


class CreationStageEventPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    from_stage: CreationStage
    to_stage: CreationStage
    reason: str
    actor_user_id: uuid.UUID
    created_at: datetime


class CreationStageTransitionPublic(ContractModel):
    current_stage: CreationStage
    project_revision: int = Field(ge=1)
    event: CreationStageEventPublic


class CreationStageEventListPublic(ContractModel):
    items: list[CreationStageEventPublic]


class CreationToolCallPropose(ContractModel):
    kind: CreationToolKind = CreationToolKind.COACH_REVIEW
    prompt: str = Field(min_length=2, max_length=1000)

    @field_validator("prompt")
    @classmethod
    def tool_prompt_must_not_be_blank(cls, value: str) -> str:
        value = " ".join(value.split())
        if len(value) < 2:
            raise ValueError("prompt must contain at least two characters")
        return value


class CreationToolCallDecision(ContractModel):
    approve: bool
    reason: str | None = Field(default=None, max_length=500)
    expected_revision: int = Field(ge=1)

    @model_validator(mode="after")
    def rejected_call_requires_reason(self) -> "CreationToolCallDecision":
        if not self.approve and len((self.reason or "").strip()) < 2:
            raise ValueError("rejection reason is required")
        return self


class CreationToolCallPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    creation_version_id: uuid.UUID
    kind: CreationToolKind
    status: CreationToolCallStatus
    input_snapshot: dict
    prompt_summary: str
    effect_summary: str
    external_data_shared: bool
    output_snapshot: dict | None
    executor_ref: str | None
    row_version: int = Field(ge=1)
    proposed_at: datetime
    expires_at: datetime
    decided_at: datetime | None
    completed_at: datetime | None


class CreationToolCallListPublic(ContractModel):
    items: list[CreationToolCallPublic]


class CreationTestFindingInput(ContractModel):
    severity: CreationIssueSeverity
    description: str = Field(min_length=2, max_length=500)


class CreationTestRecordCreate(ContractModel):
    creation_version_id: uuid.UUID
    scenario: str = Field(min_length=2, max_length=500)
    result: CreationTestResult
    notes: str = Field(default="", max_length=2000)
    findings: list[CreationTestFindingInput] = Field(default_factory=list, max_length=10)

    @model_validator(mode="after")
    def passed_test_cannot_have_blocking_findings(self) -> "CreationTestRecordCreate":
        if self.result == CreationTestResult.PASSED and any(
            item.severity == CreationIssueSeverity.BLOCKING for item in self.findings
        ):
            raise ValueError("passed test cannot contain blocking findings")
        return self


class CreationTestIssuePublic(ContractModel):
    id: uuid.UUID
    test_record_id: uuid.UUID
    project_id: uuid.UUID
    severity: CreationIssueSeverity
    description: str
    status: CreationIssueStatus
    resolution_summary: str | None
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime
    resolved_at: datetime | None


class CreationTestRecordPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    creation_version_id: uuid.UUID
    scenario: str
    result: CreationTestResult
    notes: str
    issues: list[CreationTestIssuePublic]
    created_at: datetime


class CreationTestRecordListPublic(ContractModel):
    items: list[CreationTestRecordPublic]


class CreationTestIssueResolve(ContractModel):
    resolution_summary: str = Field(min_length=2, max_length=500)
    expected_revision: int = Field(ge=1)


class CreationProjectListPublic(ContractModel):
    total: int = Field(ge=0)
    items: list[CreationProjectPublic]
    next_cursor: str | None


class CreationVersionPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    version_number: int = Field(ge=1)
    parent_version_id: uuid.UUID | None
    layers: list[LayerSnapshot]
    canvas_width: int = Field(ge=1)
    canvas_height: int = Field(ge=1)
    preview_asset_id: uuid.UUID | None
    change_summary: str
    modification_reason: str | None
    created_at: datetime


class CreationVersionListPublic(ContractModel):
    items: list[CreationVersionPublic]


class CreationLayerDiffPublic(ContractModel):
    layer_id: str
    name: str
    kind: LayerKind
    changed_fields: list[str]


class CreationVersionDiffPublic(ContractModel):
    base_version_id: uuid.UUID
    base_version_number: int = Field(ge=1)
    target_version_id: uuid.UUID
    target_version_number: int = Field(ge=1)
    canvas_changed: bool
    added_layers: list[CreationLayerDiffPublic]
    removed_layers: list[CreationLayerDiffPublic]
    modified_layers: list[CreationLayerDiffPublic]


class ImageGenerationCreate(ContractModel):
    parent_version_id: uuid.UUID
    prompt: str = Field(min_length=2, max_length=2000)
    size: ImageGenerationSize = ImageGenerationSize.SQUARE
    quality: ImageGenerationQuality = ImageGenerationQuality.MEDIUM
    expected_project_revision: int = Field(ge=1)
    user_confirmed_generation: bool

    @field_validator("prompt")
    @classmethod
    def generation_prompt_must_be_meaningful(cls, value: str) -> str:
        value = " ".join(value.split())
        if len(value) < 2:
            raise ValueError("prompt must contain at least two characters")
        return value

    @model_validator(mode="after")
    def generation_requires_confirmation(self) -> "ImageGenerationCreate":
        if not self.user_confirmed_generation:
            raise ValueError("image generation requires explicit user confirmation")
        return self


class ImageGenerationRetry(ContractModel):
    expected_revision: int = Field(ge=1)


class ImageGenerationJobPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    parent_version_id: uuid.UUID
    prompt_summary: str
    size: ImageGenerationSize
    quality: ImageGenerationQuality
    status: ImageGenerationJobStatus
    progress_percent: int = Field(ge=0, le=100)
    provider_ref: str
    model_ref: str
    external_data_shared: bool
    output_asset: MediaAssetPublic | None
    output_version_id: uuid.UUID | None
    error_code: str | None
    error_summary: str | None
    retryable: bool
    retry_count: int = Field(ge=0)
    row_version: int = Field(ge=1)
    started_at: datetime | None
    completed_at: datetime | None
    created_at: datetime
    updated_at: datetime


class CreationConversationPublic(ContractModel):
    project: CreationProjectPublic
    status: CreationConversationStatus
    initial_idea: str
    attachment_asset_ids: list[uuid.UUID]
    attachment_names: list[str]
    manual_page_ids: list[uuid.UUID]
    manual_titles: list[str]
    derivative_source_title: str | None
    plan_summary: str | None
    messages: list[CreationConversationMessagePublic]
    draft_version_ids: list[uuid.UUID]
    saved_version_ids: list[uuid.UUID]
    active_generation_job_id: uuid.UUID | None
    result_version_id: uuid.UUID | None
    row_version: int = Field(ge=1)
    started_at: datetime
    updated_at: datetime


class CreationConversationGenerationPublic(ContractModel):
    conversation: CreationConversationPublic
    generation: ImageGenerationJobPublic


class ImageGenerationJobListPublic(ContractModel):
    enabled: bool
    provider_ref: str
    model_ref: str
    external_data_shared: bool
    daily_limit: int = Field(ge=1)
    daily_used: int = Field(ge=0)
    daily_remaining: int = Field(ge=0)
    items: list[ImageGenerationJobPublic]


class CreationExportCreate(ContractModel):
    format: CreationExportFormat = CreationExportFormat.PNG
    output_scale: int = Field(default=1, ge=1, le=2)
    user_confirmed_export: bool

    @model_validator(mode="after")
    def export_requires_confirmation(self) -> "CreationExportCreate":
        if not self.user_confirmed_export:
            raise ValueError("creation export requires explicit user confirmation")
        return self


class CreationExportJobPublic(ContractModel):
    id: uuid.UUID
    project_id: uuid.UUID
    creation_version_id: uuid.UUID
    version_number: int = Field(ge=1)
    format: CreationExportFormat
    output_scale: int = Field(ge=1, le=2)
    status: CreationExportJobStatus
    progress_percent: int = Field(ge=0, le=100)
    output_asset: MediaAssetPublic | None
    error_code: str | None
    error_summary: str | None
    row_version: int = Field(ge=1)
    started_at: datetime | None
    completed_at: datetime | None
    created_at: datetime
    updated_at: datetime


class CreationExportJobListPublic(ContractModel):
    items: list[CreationExportJobPublic]


class LearningCardPut(ContractModel):
    manual_page_ids: list[uuid.UUID] = Field(max_length=50)
    method_summary: str = Field(max_length=3000)
    unresolved_questions: list[str] = Field(max_length=20)
    questions_confirmed: bool
    row_version: int | None = Field(default=None, ge=1)

    @field_validator("manual_page_ids")
    @classmethod
    def manual_ids_must_be_unique(cls, value: list[uuid.UUID]) -> list[uuid.UUID]:
        if len(value) != len(set(value)):
            raise ValueError("manual_page_ids must be unique")
        return value

    @field_validator("unresolved_questions")
    @classmethod
    def questions_must_be_meaningful(cls, value: list[str]) -> list[str]:
        normalized = [question.strip() for question in value]
        if any(not question or len(question) > 300 for question in normalized):
            raise ValueError("each unresolved question must contain 1-300 characters")
        return normalized


class LearningCardPublic(ContractModel):
    creation_version_id: uuid.UUID
    manual_page_ids: list[uuid.UUID]
    method_summary: str
    unresolved_questions: list[str]
    questions_confirmed: bool
    status: LearningCardStatus
    row_version: int = Field(ge=1)
    locked_at: datetime | None
    created_at: datetime
    updated_at: datetime


class CreationSealCheckPut(ContractModel):
    work_description: str = Field(max_length=3000)
    learning_reflection: str = Field(max_length=3000)
    next_improvement: str = Field(max_length=3000)
    identity_privacy_confirmed: bool
    contact_privacy_confirmed: bool
    portrait_rights_confirmed: bool
    row_version: int | None = Field(default=None, ge=1)


class CreationSealCheckPublic(ContractModel):
    creation_version_id: uuid.UUID
    work_description: str
    learning_reflection: str
    next_improvement: str
    identity_privacy_confirmed: bool
    contact_privacy_confirmed: bool
    portrait_rights_confirmed: bool
    status: CreationSealStatus
    row_version: int = Field(ge=1)
    locked_at: datetime | None
    created_at: datetime
    updated_at: datetime


class ProvenanceItemInput(ContractModel):
    item_type: ProvenanceItemType
    contribution_type: str = Field(min_length=1, max_length=80)
    description: str = Field(min_length=1, max_length=500)
    source_url: str | None = Field(
        default=None,
        max_length=500,
        pattern=r"^https?://",
    )
    source_author: str | None = Field(default=None, max_length=100)
    license_type: MaterialLicenseType
    authorization_asset_id: uuid.UUID | None = None
    ai_provider: str | None = Field(default=None, max_length=80)
    ai_model: str | None = Field(default=None, max_length=120)
    ai_tool_action: str | None = Field(default=None, max_length=120)
    prompt_summary: str | None = Field(default=None, max_length=500)
    output_asset_id: uuid.UUID | None = None
    user_modified: bool | None = None


class ProvenanceItemPublic(ProvenanceItemInput):
    id: uuid.UUID
    created_at: datetime


class ProvenanceManifestPut(ContractModel):
    human_contribution_summary: str = Field(max_length=3000)
    ai_assistance_used: bool
    ai_contribution_summary: str | None = Field(default=None, max_length=3000)
    aigc_label_declared: bool
    unresolved_rights: bool
    items: list[ProvenanceItemInput] = Field(max_length=200)
    row_version: int | None = Field(default=None, ge=1)


class ProvenanceManifestPublic(ContractModel):
    creation_version_id: uuid.UUID
    human_contribution_summary: str
    ai_assistance_used: bool
    ai_contribution_summary: str | None
    aigc_label_declared: bool
    unresolved_rights: bool
    status: ProvenanceStatus
    items: list[ProvenanceItemPublic]
    row_version: int = Field(ge=1)
    locked_at: datetime | None
    created_at: datetime
    updated_at: datetime


class CreationSubmissionCreate(ContractModel):
    creation_version_id: uuid.UUID
    visibility: CreationVisibility | None = None
    target_classroom_id: uuid.UUID | None = None
    conference_category: ConferenceCategory | None = None


class ConferenceCategorySuggestionPublic(ContractModel):
    category: ConferenceCategory
    confidence: float = Field(ge=0, le=1)
    reason: str


class ConferenceCategorySuggestionListPublic(ContractModel):
    items: list[ConferenceCategorySuggestionPublic]


class CreationChangeLogPublic(ContractModel):
    id: uuid.UUID
    version_id: uuid.UUID | None
    action: CreationChangeAction
    summary: str
    details: dict
    created_at: datetime


class CreationChangeLogListPublic(ContractModel):
    items: list[CreationChangeLogPublic]
