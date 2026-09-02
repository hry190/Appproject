from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    JsonValue,
    field_validator,
    model_validator,
)


class ContractModel(BaseModel):
    """Strict base for frozen public API contracts."""

    model_config = ConfigDict(extra="forbid")


class ManualProgressState(str, Enum):
    """Four collected stages plus the uncollected initial state."""

    UNSEEN = "UNSEEN"
    DISCOVERED = "DISCOVERED"
    LEARNED = "LEARNED"
    MASTERED = "MASTERED"
    TEACHING = "TEACHING"


class EvidenceCategory(str, Enum):
    WISDOM = "WISDOM"
    CRAFT = "CRAFT"
    CHIVALRY = "CHIVALRY"


class EvidenceValidationStatus(str, Enum):
    VALID = "VALID"
    PENDING_REVIEW = "PENDING_REVIEW"
    REVOKED = "REVOKED"


class TrialAttemptResult(str, Enum):
    PASSED = "PASSED"
    FAILED = "FAILED"


class MistakeStatus(str, Enum):
    TO_REVIEW = "TO_REVIEW"
    PRACTICING = "PRACTICING"
    CONSOLIDATED = "CONSOLIDATED"


class LearningEventType(str, Enum):
    COMIC_PAGE_OPENED = "COMIC_PAGE_OPENED"
    PREDICTION_COMPLETED = "PREDICTION_COMPLETED"
    TRIAL_GRADED = "TRIAL_GRADED"
    TRIAL_PASSED = "TRIAL_PASSED"
    TRANSFER_EVIDENCE_APPROVED = "TRANSFER_EVIDENCE_APPROVED"
    STRUCTURED_REVIEW_ACCEPTED = "STRUCTURED_REVIEW_ACCEPTED"
    PROJECT_REVISION_COMPLETED = "PROJECT_REVISION_COMPLETED"
    MEDIA_UPLOAD_COMPLETED = "MEDIA_UPLOAD_COMPLETED"


class TrialAttemptCreate(ContractModel):
    """Client-authored trial input. Derived learning fields are intentionally absent."""

    trial_version_id: uuid.UUID
    prediction_payload: JsonValue | None = None
    answer_payload: JsonValue
    explanation: str | None = Field(default=None, max_length=1000)
    remediation_context_id: uuid.UUID | None = None
    client_request_id: str | None = Field(default=None, min_length=8, max_length=64)

    @field_validator("answer_payload")
    @classmethod
    def answer_must_not_be_null(cls, value: JsonValue) -> JsonValue:
        if value is None:
            raise ValueError("answer_payload must not be null")
        return value

    @field_validator("explanation")
    @classmethod
    def normalize_explanation(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None


class TrialVersionPublic(ContractModel):
    id: uuid.UUID
    version: int = Field(ge=1)
    prompt: str
    prediction_prompt: str
    answer_schema: JsonValue
    max_score: float = Field(gt=0)
    pass_score: float = Field(ge=0)
    prediction_required: bool
    explanation_required: bool
    min_explanation_length: int = Field(ge=0)


class TrialPublic(ContractModel):
    id: uuid.UUID
    code: str
    title: str
    knowledge_point_code: str
    manual_page_id: uuid.UUID
    current_version: TrialVersionPublic


class EvidenceAwardPublic(ContractModel):
    id: uuid.UUID
    category: EvidenceCategory
    evidence_type: str
    manual_page_id: uuid.UUID | None = None
    summary: str
    validation_status: EvidenceValidationStatus
    created_at: datetime


class ProgressChangePublic(ContractModel):
    manual_page_id: uuid.UUID
    previous_state: ManualProgressState
    current_state: ManualProgressState
    changed: bool
    evidence_id: uuid.UUID | None = None


class ProgressTransitionPublic(ContractModel):
    id: uuid.UUID
    previous_state: ManualProgressState
    current_state: ManualProgressState
    trigger_event: LearningEventType
    rule_version: str
    evidence_id: uuid.UUID | None
    evidence_summary: str | None
    occurred_at: datetime


class ManualLearningHistoryPublic(ContractModel):
    manual_page_id: uuid.UUID
    current_state: ManualProgressState
    discovered_at: datetime | None
    learned_at: datetime | None
    mastered_at: datetime | None
    teaching_at: datetime | None
    transitions: list[ProgressTransitionPublic]
    evidence: list[EvidenceAwardPublic]


class TrialMistakePublic(ContractModel):
    id: uuid.UUID
    knowledge_point_code: str
    reason_code: str
    status: MistakeStatus


class TrialAttemptAccepted(ContractModel):
    """Server-authored grading result returned after the transaction commits."""

    attempt_id: uuid.UUID
    trial_id: uuid.UUID
    trial_version_id: uuid.UUID
    result: TrialAttemptResult
    score: float = Field(ge=0)
    max_score: float = Field(gt=0)
    passed: bool
    feedback_codes: list[str]
    progress_changes: list[ProgressChangePublic]
    evidence_awards: list[EvidenceAwardPublic]
    mistake: TrialMistakePublic | None = None
    processed_at: datetime

    @model_validator(mode="after")
    def grading_result_must_be_consistent(self) -> "TrialAttemptAccepted":
        if self.score > self.max_score:
            raise ValueError("score must not exceed max_score")
        if self.passed != (self.result == TrialAttemptResult.PASSED):
            raise ValueError("passed must match result")
        if self.passed and self.mistake is not None:
            raise ValueError("a passed attempt cannot create a mistake")
        return self


class EvidenceCounterPublic(ContractModel):
    count: int = Field(ge=0)
    latest_at: datetime | None = None
    display_summary: str


class EvidenceCountersPublic(ContractModel):
    wisdom: EvidenceCounterPublic
    craft: EvidenceCounterPublic
    chivalry: EvidenceCounterPublic


class LearningWeekPublic(ContractModel):
    timezone: str = "Asia/Shanghai"
    starts_at: datetime
    ends_at_exclusive: datetime
    practice_count: int = Field(ge=0)


class LearningStatsPublic(ContractModel):
    week: LearningWeekPublic
    lifetime_practice_count: int = Field(ge=0)
    lifetime_practice_days: int = Field(ge=0)
    distinct_trials_passed: int = Field(ge=0)
    evidence: EvidenceCountersPublic


class EvidenceListItemPublic(EvidenceAwardPublic):
    manual_title: str | None = None


class EvidenceListPublic(ContractModel):
    total: int = Field(ge=0)
    items: list[EvidenceListItemPublic]
    next_cursor: str | None = None
