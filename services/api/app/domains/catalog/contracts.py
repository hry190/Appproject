from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import Field

from app.domains.catalog.models import ManualContentStatus
from app.domains.learning.contracts import (
    ContractModel,
    EvidenceAwardPublic,
    ManualProgressState,
)


STATE_LABELS = {
    ManualProgressState.UNSEEN: "未闻",
    ManualProgressState.DISCOVERED: "偶得",
    ManualProgressState.LEARNED: "习得",
    ManualProgressState.MASTERED: "悟得",
    ManualProgressState.TEACHING: "传习",
}


class ManualProgressRequirementPublic(ContractModel):
    state: ManualProgressState
    label: str
    requirement: str


class ManualPagePublic(ContractModel):
    id: uuid.UUID
    page_no: int = Field(ge=1, le=50)
    style_no: int = Field(ge=1, le=5)
    title: str
    volume_no: int = Field(ge=1, le=10)
    volume_title: str
    core_logic: str
    content_version: str
    content_status: ManualContentStatus
    progress_state: ManualProgressState
    progress_label: str
    is_favorite: bool


class ManualPageDetailPublic(ManualPagePublic):
    life_hook: str
    interaction_evidence: str
    progress_requirements: list[ManualProgressRequirementPublic]
    evidence: list[EvidenceAwardPublic]


class ManualPageListPublic(ContractModel):
    total: int = Field(ge=0)
    items: list[ManualPagePublic]
    next_cursor: str | None


class ManualFavoritePublic(ContractModel):
    manual_page_id: uuid.UUID
    is_favorite: bool
    created_at: datetime
