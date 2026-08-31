from __future__ import annotations

import re
import uuid
from enum import Enum

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from app.models import AgeBand, GuardianStatus, UserStatus


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
