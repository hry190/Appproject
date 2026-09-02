from __future__ import annotations

import re
import uuid
from datetime import datetime

from pydantic import ConfigDict, Field, field_validator, model_validator

from app.domains.learning.contracts import ContractModel
from app.domains.profiles.models import ProfileVisibility
from app.models import AgeBand


CLASS_LABEL_RE = re.compile(r"^[一二三四五六七八九十0-9A-Za-z（）()级班\- ]{1,20}$")


class TitlePublic(ContractModel):
    id: uuid.UUID
    code: str
    name: str
    description: str
    earned_at: datetime
    selected: bool


class BadgePublic(ContractModel):
    id: uuid.UUID
    code: str
    name: str
    description: str
    earned_at: datetime


class ProfilePublic(ContractModel):
    nickname: str
    age_band: AgeBand
    class_label: str | None
    anonymous_id: str
    avatar_asset_id: uuid.UUID | None
    profile_visibility: ProfileVisibility
    current_title: TitlePublic | None
    badges: list[BadgePublic]
    row_version: int
    updated_at: datetime


class ProfilePatch(ContractModel):
    nickname: str | None = Field(default=None, min_length=2, max_length=20)
    avatar_asset_id: uuid.UUID | None = None
    class_label: str | None = Field(default=None, max_length=20)
    current_title_id: uuid.UUID | None = None
    profile_visibility: ProfileVisibility | None = None
    row_version: int = Field(ge=1)

    @field_validator("nickname")
    @classmethod
    def normalize_nickname(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        if len(normalized) < 2:
            raise ValueError("昵称至少需要2个字符")
        if any(ord(character) < 32 for character in normalized):
            raise ValueError("昵称不能包含控制字符")
        return normalized

    @field_validator("class_label")
    @classmethod
    def validate_class_label(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        if not normalized:
            return None
        if not CLASS_LABEL_RE.fullmatch(normalized):
            raise ValueError("班级仅填写年级和班级，例如“五（三）班”")
        return normalized

    @model_validator(mode="after")
    def at_least_one_mutable_field(self) -> "ProfilePatch":
        mutable_fields = self.model_fields_set - {"row_version"}
        if not mutable_fields:
            raise ValueError("请至少提交一项资料修改")
        return self

    model_config = ConfigDict(extra="forbid")
