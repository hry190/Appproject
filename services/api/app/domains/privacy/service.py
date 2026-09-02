from __future__ import annotations

import uuid

from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.moderation.audit import add_audit_event
from app.domains.privacy.contracts import (
    PrivacySettingsPatch,
    PrivacySettingsPublic,
)
from app.domains.privacy.models import PrivacySetting
from app.models import AgeBand, User


class PrivacyService:
    def __init__(self, *, db: Session, request_id: str) -> None:
        self.db = db
        self.request_id = request_id

    def get_settings(self, user: User) -> PrivacySettingsPublic:
        settings = self._ensure(user.id)
        return self._public(user, settings)

    def update_settings(
        self, user: User, payload: PrivacySettingsPatch
    ) -> PrivacySettingsPublic:
        settings = self._ensure(user.id)
        if settings.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "隐私设置已更新，请刷新后重试")
        updates = payload.model_dump(exclude_unset=True, exclude={"row_version"})
        for name, value in updates.items():
            setattr(settings, name, value)
        settings.row_version += 1
        settings.updated_at = utcnow()
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="PRIVACY_SETTINGS_UPDATED",
            target_type="PRIVACY_SETTINGS",
            target_id=user.id,
            result="SUCCESS",
            request_id=self.request_id,
            safe_diff={
                key: value.value if hasattr(value, "value") else value
                for key, value in updates.items()
            },
        )
        self.db.commit()
        return self._public(user, settings)

    def _ensure(self, user_id: uuid.UUID) -> PrivacySetting:
        settings = self.db.get(PrivacySetting, user_id)
        if settings is None:
            settings = PrivacySetting(user_id=user_id)
            self.db.add(settings)
            self.db.commit()
            self.db.refresh(settings)
        return settings

    @staticmethod
    def _public(user: User, settings: PrivacySetting) -> PrivacySettingsPublic:
        return PrivacySettingsPublic(
            default_work_visibility=settings.default_work_visibility,
            learning_card_public=settings.learning_card_public,
            aigc_export_mark_enabled=settings.aigc_export_mark_enabled,
            profile_discovery_enabled=settings.profile_discovery_enabled,
            guardian_controls_active=user.age_band != AgeBand.ADULT,
            row_version=settings.row_version,
            created_at=settings.created_at,
            updated_at=settings.updated_at,
        )
