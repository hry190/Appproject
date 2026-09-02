from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import Field, field_validator

from app.domains.learning.contracts import ContractModel
from app.domains.media.models import (
    MediaAssetStatus,
    MediaDerivativeKind,
    MediaScanOutcome,
    UploadPurpose,
    UploadSessionStatus,
)


class UploadIntentCreate(ContractModel):
    purpose: UploadPurpose
    filename: str = Field(min_length=1, max_length=180)
    declared_mime: str = Field(min_length=1, max_length=80)
    byte_size: int = Field(ge=1)
    sha256: str = Field(pattern=r"^[a-fA-F0-9]{64}$")

    @field_validator("sha256")
    @classmethod
    def normalize_hash(cls, value: str) -> str:
        return value.lower()


class UploadIntentPublic(ContractModel):
    id: uuid.UUID
    status: UploadSessionStatus
    purpose: UploadPurpose
    upload_url: str
    method: str = "PUT"
    required_headers: dict[str, str]
    expires_at: datetime


class UploadComplete(ContractModel):
    byte_size: int = Field(ge=1)
    sha256: str = Field(pattern=r"^[a-fA-F0-9]{64}$")

    @field_validator("sha256")
    @classmethod
    def normalize_hash(cls, value: str) -> str:
        return value.lower()


class MediaDerivativePublic(ContractModel):
    kind: MediaDerivativeKind
    width: int = Field(ge=1)
    height: int = Field(ge=1)
    mime_type: str
    byte_size: int = Field(ge=1)
    url: str
    expires_at: datetime


class MediaAssetPublic(ContractModel):
    id: uuid.UUID
    purpose: UploadPurpose
    original_filename: str
    status: MediaAssetStatus
    actual_mime: str | None
    byte_size: int = Field(ge=1)
    sha256: str
    width: int | None
    height: int | None
    metadata_stripped: bool | None
    aigc_detected: bool | None
    rejection_code: str | None
    rejection_summary: str | None
    original_url: str | None
    url_expires_at: datetime | None
    derivatives: list[MediaDerivativePublic]
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime


class InternalMediaProcessRequest(ContractModel):
    content_safety_outcome: MediaScanOutcome = MediaScanOutcome.REVIEW
    content_reason_code: str | None = Field(default=None, max_length=80)
    aigc_detected: bool | None = None


class MediaDeleteAccepted(ContractModel):
    id: uuid.UUID
    status: MediaAssetStatus
