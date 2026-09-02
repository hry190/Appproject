from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Query, status

from app.api.dependencies import get_current_user, get_mistake_service
from app.domains.learning.contracts import MistakeStatus
from app.domains.mistakes.contracts import (
    MistakeDetailPublic,
    MistakeListPublic,
    RetrySessionPublic,
)
from app.domains.mistakes.service import MistakeService
from app.models import User


router = APIRouter(prefix="/v1/mistakes", tags=["mistakes"])


@router.get("", response_model=MistakeListPublic)
def list_mistakes(
    mistake_status: MistakeStatus | None = Query(default=None, alias="status"),
    manual_page_id: uuid.UUID | None = None,
    cursor: str | None = Query(default=None, max_length=120),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: MistakeService = Depends(get_mistake_service),
) -> MistakeListPublic:
    return service.list_mistakes(
        user,
        status=mistake_status,
        manual_page_id=manual_page_id,
        cursor=cursor,
        limit=limit,
    )


@router.get("/{mistake_id}", response_model=MistakeDetailPublic)
def get_mistake(
    mistake_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: MistakeService = Depends(get_mistake_service),
) -> MistakeDetailPublic:
    return service.get_mistake(user, mistake_id)


@router.post(
    "/{mistake_id}/retry-sessions",
    response_model=RetrySessionPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_retry_session(
    mistake_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: MistakeService = Depends(get_mistake_service),
) -> RetrySessionPublic:
    return service.create_retry_session(user, mistake_id)
