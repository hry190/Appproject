from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.dependencies import get_current_user, get_profile_service
from app.domains.profiles.contracts import (
    BadgePublic,
    ProfilePatch,
    ProfilePublic,
    TitlePublic,
)
from app.domains.profiles.service import ProfileService
from app.models import User


router = APIRouter(prefix="/v1/profile", tags=["profile"])


@router.get("", response_model=ProfilePublic)
def get_profile(
    user: User = Depends(get_current_user),
    service: ProfileService = Depends(get_profile_service),
) -> ProfilePublic:
    return service.get_profile(user)


@router.patch("", response_model=ProfilePublic)
def update_profile(
    payload: ProfilePatch,
    user: User = Depends(get_current_user),
    service: ProfileService = Depends(get_profile_service),
) -> ProfilePublic:
    return service.update_profile(user, payload)


@router.get("/titles", response_model=list[TitlePublic])
def list_titles(
    user: User = Depends(get_current_user),
    service: ProfileService = Depends(get_profile_service),
) -> list[TitlePublic]:
    return service.list_titles(user)


@router.get("/badges", response_model=list[BadgePublic])
def list_badges(
    user: User = Depends(get_current_user),
    service: ProfileService = Depends(get_profile_service),
) -> list[BadgePublic]:
    return service.list_badges(user)
