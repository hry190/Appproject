from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import Field, field_validator, model_validator

from app.domains.learning.contracts import (
    ContractModel,
    LearningStatsPublic,
    ManualProgressState,
    MistakeStatus,
)
from app.models import AgeBand


class LuggageEmptyReason(str, Enum):
    NO_OBTAINED_MANUALS = "NO_OBTAINED_MANUALS"
    NO_MISTAKES = "NO_MISTAKES"
    NO_CREATIONS = "NO_CREATIONS"


class CreationDisplayStatus(str, Enum):
    DRAFT = "DRAFT"
    PENDING_CHECK = "PENDING_CHECK"
    PENDING_HUMAN_REVIEW = "PENDING_HUMAN_REVIEW"
    PUBLISHED = "PUBLISHED"
    RETURNED = "RETURNED"
    RESTRICTED = "RESTRICTED"
    WITHDRAWN = "WITHDRAWN"


class SignedMediaPublic(ContractModel):
    asset_id: uuid.UUID
    url: str
    expires_at: datetime


class CurrentTitlePublic(ContractModel):
    code: str
    name: str


class BadgeSummaryPublic(ContractModel):
    code: str
    name: str
    earned_at: datetime


class LuggageProfilePublic(ContractModel):
    nickname: str
    avatar: SignedMediaPublic | None = None
    age_band: AgeBand
    class_label: str | None = None
    anonymous_id: str
    current_title: CurrentTitlePublic | None = None
    badges: list[BadgeSummaryPublic]


class LuggageManualItemPublic(ContractModel):
    id: uuid.UUID
    volume: int = Field(ge=1, le=10)
    style_no: int = Field(ge=1, le=5)
    title: str
    state: ManualProgressState
    state_label: str
    latest_evidence_summary: str | None = None
    updated_at: datetime | None = None


class LuggageManualSectionPublic(ContractModel):
    total: int = Field(ge=0)
    obtained: int = Field(ge=0)
    counts_by_state: dict[ManualProgressState, int]
    items: list[LuggageManualItemPublic]
    empty_reason: LuggageEmptyReason | None = None
    detail_url: str = Field(pattern=r"^/v1/")

    @field_validator("counts_by_state")
    @classmethod
    def state_counts_must_be_complete(
        cls,
        value: dict[ManualProgressState, int],
    ) -> dict[ManualProgressState, int]:
        if set(value) != set(ManualProgressState):
            raise ValueError("counts_by_state must include every progress state")
        if any(count < 0 for count in value.values()):
            raise ValueError("counts_by_state values must be non-negative")
        return value

    @model_validator(mode="after")
    def totals_must_be_consistent(self) -> "LuggageManualSectionPublic":
        if sum(self.counts_by_state.values()) != self.total:
            raise ValueError("manual state counts must add up to total")
        expected_obtained = self.total - self.counts_by_state[ManualProgressState.UNSEEN]
        if self.obtained != expected_obtained:
            raise ValueError("obtained must exclude only UNSEEN manuals")
        if self.obtained == 0 and self.empty_reason is None:
            raise ValueError("empty_reason is required when no manuals are obtained")
        if self.obtained > 0 and self.empty_reason is not None:
            raise ValueError("empty_reason must be null when manuals are obtained")
        return self


class LuggageMistakeItemPublic(ContractModel):
    id: uuid.UUID
    knowledge_point: str
    status: MistakeStatus
    manual_page_id: uuid.UUID
    retry_url: str | None = Field(default=None, pattern=r"^/v1/")


class LuggageMistakeSectionPublic(ContractModel):
    pending_count: int = Field(ge=0)
    items: list[LuggageMistakeItemPublic]
    empty_reason: LuggageEmptyReason | None = None
    detail_url: str = Field(pattern=r"^/v1/")

    @model_validator(mode="after")
    def empty_state_must_be_explicit(self) -> "LuggageMistakeSectionPublic":
        if self.pending_count == 0 and self.empty_reason != LuggageEmptyReason.NO_MISTAKES:
            raise ValueError("NO_MISTAKES is required when pending_count is zero")
        if self.pending_count > 0 and self.empty_reason is not None:
            raise ValueError("empty_reason must be null when pending mistakes exist")
        return self


class LuggageCreationItemPublic(ContractModel):
    project_id: uuid.UUID
    title: str
    display_status: CreationDisplayStatus
    current_version: int = Field(ge=1)
    thumbnail: SignedMediaPublic | None = None
    can_revise: bool
    return_reason: str | None = None
    updated_at: datetime


class LuggageCreationSectionPublic(ContractModel):
    counts_by_status: dict[CreationDisplayStatus, int]
    items: list[LuggageCreationItemPublic]
    empty_reason: LuggageEmptyReason | None = None
    detail_url: str = Field(pattern=r"^/v1/")

    @field_validator("counts_by_status")
    @classmethod
    def status_counts_must_be_non_negative(
        cls,
        value: dict[CreationDisplayStatus, int],
    ) -> dict[CreationDisplayStatus, int]:
        if any(count < 0 for count in value.values()):
            raise ValueError("counts_by_status values must be non-negative")
        return value

    @model_validator(mode="after")
    def creation_empty_state_must_be_explicit(self) -> "LuggageCreationSectionPublic":
        total = sum(self.counts_by_status.values())
        if total == 0 and self.empty_reason != LuggageEmptyReason.NO_CREATIONS:
            raise ValueError("NO_CREATIONS is required when there are no creations")
        if total > 0 and self.empty_reason is not None:
            raise ValueError("empty_reason must be null when creations exist")
        return self


class LuggagePrivacyPublic(ContractModel):
    guardian_controls_active: bool
    pending_appeal_count: int = Field(ge=0)
    privacy_settings_url: str = Field(pattern=r"^/v1/")


class LuggageDataPublic(ContractModel):
    profile: LuggageProfilePublic
    stats: LearningStatsPublic
    manuals: LuggageManualSectionPublic
    mistakes: LuggageMistakeSectionPublic
    creations: LuggageCreationSectionPublic
    privacy: LuggagePrivacyPublic


class LuggageMetaPublic(ContractModel):
    generated_at: datetime
    snapshot_version: int = Field(ge=0)
    etag: str


class LuggageResponse(ContractModel):
    data: LuggageDataPublic
    meta: LuggageMetaPublic
