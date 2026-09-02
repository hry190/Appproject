from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.dependencies import get_current_user, get_privacy_service
from app.domains.privacy.contracts import (
    PrivacySettingsPatch,
    PrivacySettingsPublic,
)
from app.domains.privacy.service import PrivacyService
from app.models import User


router = APIRouter(prefix="/v1/me/privacy-settings", tags=["privacy"])


@router.get("", response_model=PrivacySettingsPublic)
def get_privacy_settings(
    user: User = Depends(get_current_user),
    service: PrivacyService = Depends(get_privacy_service),
) -> PrivacySettingsPublic:
    return service.get_settings(user)


@router.patch("", response_model=PrivacySettingsPublic)
def update_privacy_settings(
    payload: PrivacySettingsPatch,
    user: User = Depends(get_current_user),
    service: PrivacyService = Depends(get_privacy_service),
) -> PrivacySettingsPublic:
    return service.update_settings(user, payload)
