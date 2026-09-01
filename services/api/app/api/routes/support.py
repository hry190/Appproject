from __future__ import annotations

from fastapi import APIRouter, Depends, status

from app.api.dependencies import get_current_user, get_user_settings_service
from app.models import User
from app.schemas import FeedbackCreate, FeedbackPublic
from app.services.user_settings import UserSettingsService


router = APIRouter(prefix="/v1/support", tags=["support"])


@router.post(
    "/feedback",
    response_model=FeedbackPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_feedback(
    payload: FeedbackCreate,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> FeedbackPublic:
    return FeedbackPublic.model_validate(service.create_feedback(user, payload))
