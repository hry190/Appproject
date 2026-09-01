from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Response, status

from app.api.dependencies import get_current_user, get_user_settings_service
from app.models import User
from app.schemas import (
    BlacklistCreate,
    BlacklistEntryPublic,
    GuardianControlsPatch,
    GuardianControlsPublic,
    UserPreferencesPatch,
    UserPreferencesPublic,
)
from app.services.user_settings import UserSettingsService


router = APIRouter(prefix="/v1/settings", tags=["settings"])


@router.get("/preferences", response_model=UserPreferencesPublic)
def get_preferences(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> UserPreferencesPublic:
    return UserPreferencesPublic.model_validate(service.get_preferences(user))


@router.patch("/preferences", response_model=UserPreferencesPublic)
def update_preferences(
    payload: UserPreferencesPatch,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> UserPreferencesPublic:
    return UserPreferencesPublic.model_validate(
        service.update_preferences(user, payload)
    )


@router.get("/guardian-controls", response_model=GuardianControlsPublic)
def get_guardian_controls(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> GuardianControlsPublic:
    return GuardianControlsPublic.model_validate(service.get_guardian_controls(user))


@router.patch("/guardian-controls", response_model=GuardianControlsPublic)
def update_guardian_controls(
    payload: GuardianControlsPatch,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> GuardianControlsPublic:
    return GuardianControlsPublic.model_validate(
        service.update_guardian_controls(user, payload)
    )


@router.get("/blacklist", response_model=list[BlacklistEntryPublic])
def get_blacklist(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> list[BlacklistEntryPublic]:
    return service.list_blacklist(user)


@router.post(
    "/blacklist",
    response_model=BlacklistEntryPublic,
    status_code=status.HTTP_201_CREATED,
)
def add_to_blacklist(
    payload: BlacklistCreate,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> BlacklistEntryPublic:
    return service.add_to_blacklist(user, payload.blocked_user_id)


@router.delete("/blacklist/{blocked_user_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_from_blacklist(
    blocked_user_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> Response:
    service.remove_from_blacklist(user, blocked_user_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
