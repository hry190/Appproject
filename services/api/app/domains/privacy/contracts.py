from __future__ import annotations

from datetime import datetime

from pydantic import Field, model_validator

from app.domains.creations.models import CreationVisibility
from app.domains.learning.contracts import ContractModel


class PrivacySettingsPublic(ContractModel):
    default_work_visibility: CreationVisibility
    learning_card_public: bool
    aigc_export_mark_enabled: bool
    profile_discovery_enabled: bool
    guardian_controls_active: bool
    row_version: int = Field(ge=1)
    created_at: datetime
    updated_at: datetime


class PrivacySettingsPatch(ContractModel):
    default_work_visibility: CreationVisibility | None = None
    learning_card_public: bool | None = None
    aigc_export_mark_enabled: bool | None = None
    profile_discovery_enabled: bool | None = None
    row_version: int = Field(ge=1)

    @model_validator(mode="after")
    def needs_update(self) -> "PrivacySettingsPatch":
        if not (self.model_fields_set - {"row_version"}):
            raise ValueError("at least one privacy field is required")
        return self
