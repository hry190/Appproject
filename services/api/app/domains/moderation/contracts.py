from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import Field, model_validator

from app.domains.creations.models import PublicationStatus
from app.domains.learning.contracts import ContractModel
from app.domains.moderation.models import (
    AppealStatus,
    ModerationCaseStatus,
    ModerationDecision,
    ModerationRiskLevel,
)


class ModerationCasePublic(ContractModel):
    id: uuid.UUID
    publication_id: uuid.UUID
    publication_status: PublicationStatus
    status: ModerationCaseStatus
    risk_level: ModerationRiskLevel | None
    public_reason_code: str | None
    public_reason_summary: str | None
    revision_suggestion: str | None
    can_appeal: bool
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime


class InternalModerationDecision(ContractModel):
    decision: ModerationDecision
    risk_level: ModerationRiskLevel
    reason_code: str | None = Field(default=None, max_length=80)
    reason_summary: str | None = Field(default=None, max_length=500)
    revision_suggestion: str | None = Field(default=None, max_length=500)
    reviewer_reference: str = Field(min_length=3, max_length=80)
    minimal_evidence: dict = Field(default_factory=dict)
    row_version: int = Field(ge=1)

    @model_validator(mode="after")
    def rejection_must_explain_next_step(self) -> "InternalModerationDecision":
        if self.decision in {ModerationDecision.RETURN, ModerationDecision.RESTRICT}:
            if not self.reason_code or not self.reason_summary:
                raise ValueError("return/restrict decisions require a public reason")
        if self.decision == ModerationDecision.RETURN and not self.revision_suggestion:
            raise ValueError("return decisions require a revision suggestion")
        return self


class WithdrawPublication(ContractModel):
    row_version: int = Field(ge=1)


class ModerationAppealCreate(ContractModel):
    reason: str = Field(min_length=10, max_length=1000)


class ModerationAppealPublic(ContractModel):
    id: uuid.UUID
    moderation_case_id: uuid.UUID
    reason: str
    status: AppealStatus
    resolution_summary: str | None
    created_at: datetime
    resolved_at: datetime | None


class InternalAppealDecision(ContractModel):
    status: AppealStatus
    resolution_summary: str = Field(min_length=5, max_length=500)
    reviewer_reference: str = Field(min_length=3, max_length=80)

    @model_validator(mode="after")
    def must_be_final(self) -> "InternalAppealDecision":
        if self.status == AppealStatus.PENDING:
            raise ValueError("appeal decision must be final")
        return self
