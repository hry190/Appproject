from __future__ import annotations

import re
import uuid
from datetime import datetime
from enum import Enum

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from app.models import (
    AgeBand,
    ContentLevel,
    DataRequestStatus,
    DataRequestType,
    FeedbackCategory,
    GuardianStatus,
    TicketStatus,
    UserStatus,
)


PHONE_INPUT_RE = re.compile(r"^(?:\+?86)?1[3-9]\d{9}$")


class StrictModel(BaseModel):
    # Passwords and opaque tokens must never be silently changed. Individual
    # human-readable fields normalize themselves explicitly where appropriate.
    model_config = ConfigDict(extra="forbid")


class VerificationPurpose(str, Enum):
    REGISTER = "REGISTER"
    RESET_PASSWORD = "RESET_PASSWORD"
    GUARDIAN_CONSENT = "GUARDIAN_CONSENT"


def validate_phone_input(value: str) -> str:
    compact = re.sub(r"[\s-]", "", value)
    if not PHONE_INPUT_RE.fullmatch(compact):
        raise ValueError("请输入有效的中国大陆手机号")
    return compact


class VerificationCodeRequest(StrictModel):
    phone: str = Field(min_length=11, max_length=16)
    purpose: VerificationPurpose
    client_request_id: str | None = Field(default=None, min_length=8, max_length=64)

    _validate_phone = field_validator("phone")(validate_phone_input)


class VerificationCodeAccepted(StrictModel):
    accepted: bool = True
    expires_in: int
    retry_after: int
    request_id: str


class GuardianConsentRequest(StrictModel):
    child_phone: str = Field(min_length=11, max_length=16)
    guardian_phone: str = Field(min_length=11, max_length=16)
    verification_code: str = Field(pattern=r"^\d{6}$")
    terms_version: str = Field(min_length=1, max_length=32)
    privacy_version: str = Field(min_length=1, max_length=32)

    _validate_child_phone = field_validator("child_phone")(validate_phone_input)
    _validate_guardian_phone = field_validator("guardian_phone")(validate_phone_input)

    @model_validator(mode="after")
    def phones_must_differ(self) -> "GuardianConsentRequest":
        if re.sub(r"\D", "", self.child_phone)[-11:] == re.sub(
            r"\D", "", self.guardian_phone
        )[-11:]:
            raise ValueError("监护人手机号不能与学生手机号相同")
        return self


class GuardianConsentResponse(StrictModel):
    guardian_consent_token: str
    expires_in: int


class RegisterRequest(StrictModel):
    phone: str = Field(min_length=11, max_length=16)
    verification_code: str = Field(pattern=r"^\d{6}$")
    password: str = Field(min_length=8, max_length=64)
    age_band: AgeBand
    terms_version: str = Field(min_length=1, max_length=32)
    privacy_version: str = Field(min_length=1, max_length=32)
    guardian_consent_token: str | None = Field(default=None, min_length=32, max_length=2048)
    device_name: str | None = Field(default=None, min_length=1, max_length=80)
    client_request_id: str | None = Field(default=None, min_length=8, max_length=64)

    _validate_phone = field_validator("phone")(validate_phone_input)


class PasswordLoginRequest(StrictModel):
    phone: str = Field(min_length=11, max_length=16)
    password: str = Field(min_length=8, max_length=64)
    device_name: str | None = Field(default=None, min_length=1, max_length=80)

    _validate_phone = field_validator("phone")(validate_phone_input)


class RefreshRequest(StrictModel):
    refresh_token: str = Field(min_length=32, max_length=512)
    device_name: str | None = Field(default=None, min_length=1, max_length=80)


class LogoutRequest(StrictModel):
    refresh_token: str = Field(min_length=32, max_length=512)


class PasswordResetRequest(StrictModel):
    phone: str = Field(min_length=11, max_length=16)
    verification_code: str = Field(pattern=r"^\d{6}$")
    new_password: str = Field(min_length=8, max_length=64)

    _validate_phone = field_validator("phone")(validate_phone_input)


class UserPublic(StrictModel):
    id: uuid.UUID
    nickname: str
    phone_masked: str
    status: UserStatus
    age_band: AgeBand
    guardian_status: GuardianStatus


class TokenPair(StrictModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int
    refresh_expires_in: int


class AuthResponse(StrictModel):
    user: UserPublic
    tokens: TokenPair
    next_action: str = "ENTER_APP"


class PasswordResetResponse(StrictModel):
    status: str = "PASSWORD_RESET_SUCCESS"


class StatusResponse(StrictModel):
    status: str


class UserPreferencesPublic(StrictModel):
    message_enabled: bool
    learning_reminder: bool
    work_updates: bool
    service_messages: bool
    quiet_hours: bool
    auto_save: bool
    wifi_only: bool
    haptic_feedback: bool
    large_text: bool
    sound_enabled: bool
    music_volume: float
    effect_volume: float
    high_contrast: bool
    read_aloud: bool
    subtitles_enabled: bool
    personalization_enabled: bool
    rest_reminder: bool
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True, extra="forbid")


class UserPreferencesPatch(StrictModel):
    message_enabled: bool | None = None
    learning_reminder: bool | None = None
    work_updates: bool | None = None
    service_messages: bool | None = None
    quiet_hours: bool | None = None
    auto_save: bool | None = None
    wifi_only: bool | None = None
    haptic_feedback: bool | None = None
    large_text: bool | None = None
    sound_enabled: bool | None = None
    music_volume: float | None = Field(default=None, ge=0, le=1)
    effect_volume: float | None = Field(default=None, ge=0, le=1)
    high_contrast: bool | None = None
    read_aloud: bool | None = None
    subtitles_enabled: bool | None = None
    personalization_enabled: bool | None = None
    rest_reminder: bool | None = None

    @model_validator(mode="after")
    def at_least_one_field(self) -> "UserPreferencesPatch":
        if not self.model_fields_set:
            raise ValueError("请至少提交一项设置")
        return self


class GuardianControlsPublic(StrictModel):
    daily_limit_minutes: int
    creation_allowed: bool
    content_level: ContentLevel
    minor_mode: bool
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True, extra="forbid")


class GuardianControlsPatch(StrictModel):
    daily_limit_minutes: int | None = Field(default=None, ge=15, le=240)
    creation_allowed: bool | None = None
    content_level: ContentLevel | None = None
    minor_mode: bool | None = None

    @model_validator(mode="after")
    def at_least_one_field(self) -> "GuardianControlsPatch":
        if not self.model_fields_set:
            raise ValueError("请至少提交一项监护设置")
        return self


class FeedbackCreate(StrictModel):
    category: FeedbackCategory = FeedbackCategory.GENERAL
    message: str = Field(min_length=10, max_length=1000)

    @field_validator("message")
    @classmethod
    def normalize_message(cls, value: str) -> str:
        normalized = value.strip()
        if len(normalized) < 10:
            raise ValueError("反馈内容至少需要10个字")
        return normalized


class FeedbackPublic(StrictModel):
    id: uuid.UUID
    category: FeedbackCategory
    message: str
    status: TicketStatus
    created_at: datetime

    model_config = ConfigDict(from_attributes=True, extra="forbid")


class BlacklistCreate(StrictModel):
    blocked_user_id: uuid.UUID


class BlacklistEntryPublic(StrictModel):
    user_id: uuid.UUID
    nickname: str
    blocked_at: datetime


class SessionPublic(StrictModel):
    id: uuid.UUID
    device_name: str
    created_at: datetime
    last_seen_at: datetime
    expires_at: datetime


class DataRightsRequestCreate(StrictModel):
    request_type: DataRequestType
    reason: str | None = Field(default=None, max_length=500)

    @field_validator("reason")
    @classmethod
    def normalize_reason(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None


class DataRightsRequestPublic(StrictModel):
    id: uuid.UUID
    request_type: DataRequestType
    status: DataRequestStatus
    reason: str | None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True, extra="forbid")


class ConsentRecordPublic(StrictModel):
    consent_type: str
    document_version: str
    subject: str
    agreed_at: datetime


class AccountExport(StrictModel):
    generated_at: datetime
    user: UserPublic
    preferences: UserPreferencesPublic
    guardian_controls: GuardianControlsPublic | None
    consents: list[ConsentRecordPublic]
    active_sessions: list[SessionPublic]
