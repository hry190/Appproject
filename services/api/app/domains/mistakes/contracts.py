from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import Field, JsonValue

from app.domains.learning.contracts import (
    ContractModel,
    MistakeStatus,
    TrialAttemptResult,
)


class MistakeListItemPublic(ContractModel):
    id: uuid.UUID
    trial_id: uuid.UUID
    manual_page_id: uuid.UUID
    manual_title: str
    knowledge_point_code: str
    error_reason_code: str
    error_reason_summary: str
    status: MistakeStatus
    failure_count: int = Field(ge=1)
    successful_retries: int = Field(ge=0)
    next_review_at: datetime | None
    updated_at: datetime
    retry_url: str


class MistakeListPublic(ContractModel):
    total: int = Field(ge=0)
    items: list[MistakeListItemPublic]
    next_cursor: str | None


class RemediationRecordPublic(ContractModel):
    id: uuid.UUID
    attempt_id: uuid.UUID
    result: TrialAttemptResult
    reflection: str | None
    occurred_at: datetime


class MistakeDetailPublic(MistakeListItemPublic):
    first_attempt_id: uuid.UUID
    latest_attempt_id: uuid.UUID
    original_answer_payload: JsonValue
    consolidated_at: datetime | None
    remediation_records: list[RemediationRecordPublic]


class RetrySessionPublic(ContractModel):
    id: uuid.UUID
    mistake_id: uuid.UUID
    trial_id: uuid.UUID
    trial_version_id: uuid.UUID
    expires_at: datetime
    submit_url: str
