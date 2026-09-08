from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import Field, field_validator

from app.domains.creations.models import CreationMediaType, CreationVisibility
from app.domains.distribution.models import PublicationDeliveryChannel
from app.domains.learning.contracts import ContractModel


class ClassroomRole(str, Enum):
    OWNER = "OWNER"
    MEMBER = "MEMBER"


class ClassroomCreate(ContractModel):
    name: str = Field(min_length=1, max_length=80)

    @field_validator("name")
    @classmethod
    def normalize_name(cls, value: str) -> str:
        return " ".join(value.strip().split())


class ClassroomCreatedPublic(ContractModel):
    id: uuid.UUID
    name: str
    join_code: str
    role: ClassroomRole = ClassroomRole.OWNER
    member_count: int = 0
    created_at: datetime


class ClassroomJoin(ContractModel):
    join_code: str = Field(min_length=6, max_length=12)

    @field_validator("join_code")
    @classmethod
    def normalize_join_code(cls, value: str) -> str:
        return value.replace("-", "").replace(" ", "").upper()


class ClassroomPublic(ContractModel):
    id: uuid.UUID
    name: str
    role: ClassroomRole
    teacher_nickname: str
    member_count: int = Field(ge=0)
    can_submit: bool
    joined_at: datetime | None = None
    created_at: datetime


class ClassroomListPublic(ContractModel):
    items: list[ClassroomPublic]


class PublicationRelatedManualPublic(ContractModel):
    manual_page_id: uuid.UUID
    page_no: int = Field(ge=1, le=50)
    title: str


class PublicationLearningCardSummaryPublic(ContractModel):
    method_summary: str
    unresolved_questions: list[str]


class PublicationProvenanceSummaryPublic(ContractModel):
    human_contribution_summary: str
    ai_assistance_used: bool
    ai_contribution_summary: str | None
    aigc_label_declared: bool
    source_count: int = Field(ge=0)


class PublicationFeedItemPublic(ContractModel):
    publication_id: uuid.UUID
    project_id: uuid.UUID
    creation_version_id: uuid.UUID
    title: str
    description: str | None
    media_type: CreationMediaType
    visibility: CreationVisibility
    channel: PublicationDeliveryChannel | None
    classroom_id: uuid.UUID | None
    classroom_name: str | None
    author_nickname: str
    is_owner: bool
    version_number: int = Field(ge=1)
    published_at: datetime
    preview_url: str | None
    preview_mime_type: str | None
    preview_width: int | None
    preview_height: int | None
    preview_url_expires_at: datetime | None
    ai_assisted: bool
    learning_summary: str | None
    related_manuals: list[PublicationRelatedManualPublic]
    learning_card: PublicationLearningCardSummaryPublic | None
    provenance: PublicationProvenanceSummaryPublic | None


class PublicationFeedPagePublic(ContractModel):
    items: list[PublicationFeedItemPublic]
    next_cursor: str | None
