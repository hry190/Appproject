from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import Field

from app.domains.learning.contracts import ContractModel, ManualProgressState


class LearningBookPublic(ContractModel):
    manual_page_id: uuid.UUID
    page_no: int = Field(ge=1, le=50)
    style_no: int = Field(ge=1, le=5)
    title: str
    volume_no: int = Field(ge=1, le=10)
    volume_title: str
    state: ManualProgressState
    state_label: str
    evidence_count: int = Field(ge=0)
    review_due: bool
    updated_at: datetime | None = None


class BackMountainRecommendationPublic(ContractModel):
    volume_no: int = Field(ge=1, le=10)
    lesson_id: uuid.UUID
    reason: str
    available: bool = True


class LearningOverviewPublic(ContractModel):
    recommended_lesson_id: uuid.UUID | None = None
    books: list[LearningBookPublic]
    back_mountain: BackMountainRecommendationPublic | None = None


class LearningRouteMatchPublic(ContractModel):
    lesson_id: uuid.UUID
    volume_no: int = Field(ge=1, le=10)
    volume_title: str
    title: str
    state: ManualProgressState
    available: bool
    prerequisites: list[uuid.UUID] = Field(default_factory=list)
    recommended_reason: str
    match_source: str


class LearningRoutePublic(ContractModel):
    query: str = Field(max_length=80)
    matches: list[LearningRouteMatchPublic]


class LessonReadEventAccepted(ContractModel):
    event_id: uuid.UUID
    lesson_id: uuid.UUID
    state: ManualProgressState
    changed: bool = False
    processed_at: datetime


class MigrationEvidenceCreate(ContractModel):
    creation_version_id: uuid.UUID
    used_lessons: list[uuid.UUID] = Field(min_length=1, max_length=10)
    revision_reason: str = Field(min_length=1, max_length=500)


class MigrationEvidenceSubmitted(ContractModel):
    evidence_id: uuid.UUID
    lesson_id: uuid.UUID
    creation_version_id: uuid.UUID
    validation_status: str
    current_state: ManualProgressState
    processed_at: datetime


class MigrationEvidenceApproved(ContractModel):
    evidence_id: uuid.UUID
    lesson_id: uuid.UUID
    state: ManualProgressState
    changed: bool
    processed_at: datetime


class TeachingEvidenceCreate(ContractModel):
    """User-authored structured explanation used to demonstrate teaching."""

    explanation: str = Field(min_length=20, max_length=1000)
    application_example: str = Field(min_length=10, max_length=1000)
    learner_feedback: str = Field(min_length=10, max_length=500)


class TeachingEvidenceAccepted(ContractModel):
    evidence_id: uuid.UUID
    lesson_id: uuid.UUID
    validation_status: str
    current_state: ManualProgressState
    changed: bool
    processed_at: datetime
