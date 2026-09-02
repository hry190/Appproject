from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import Field, field_validator, model_validator

from app.domains.creations.models import (
    CreationChangeAction,
    CreationMediaType,
    CreationProjectStatus,
    CreationVisibility,
    LayerKind,
    LearningCardStatus,
    MaterialLicenseType,
    ProvenanceItemType,
    ProvenanceStatus,
    PublicationStatus,
)
from app.domains.learning.contracts import ContractModel


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
    media_type: CreationMediaType
    default_visibility: CreationVisibility = CreationVisibility.PRIVATE

    @field_validator("title")
    @classmethod
    def title_must_not_be_blank(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("title must not be blank")
        return value


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
    display_status: CreationDisplayStatus
    latest_publication: PublicationPublic | None
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime


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


class CreationChangeLogPublic(ContractModel):
    id: uuid.UUID
    version_id: uuid.UUID | None
    action: CreationChangeAction
    summary: str
    details: dict
    created_at: datetime


class CreationChangeLogListPublic(ContractModel):
    items: list[CreationChangeLogPublic]
