from __future__ import annotations

from fastapi import APIRouter, Depends, Query, status

from app.api.dependencies import get_current_user, get_distribution_service
from app.domains.distribution.contracts import (
    ClassroomCreate,
    ClassroomCreatedPublic,
    ClassroomJoin,
    ClassroomListPublic,
    ClassroomPublic,
    PublicationFeedPagePublic,
)
from app.domains.distribution.service import DistributionService
from app.models import User


router = APIRouter(prefix="/v1", tags=["distribution"])


@router.post("/classrooms", response_model=ClassroomCreatedPublic, status_code=status.HTTP_201_CREATED)
def create_classroom(
    payload: ClassroomCreate,
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> ClassroomCreatedPublic:
    return service.create_classroom(user, payload)


@router.post("/classrooms:join", response_model=ClassroomPublic)
def join_classroom(
    payload: ClassroomJoin,
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> ClassroomPublic:
    return service.join_classroom(user, payload)


@router.get("/me/classrooms", response_model=ClassroomListPublic)
def list_classrooms(
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> ClassroomListPublic:
    return service.list_classrooms(user)


@router.get("/community/feed", response_model=PublicationFeedPagePublic)
def community_feed(
    cursor: str | None = Query(default=None, max_length=300),
    limit: int = Query(default=20, ge=1, le=30),
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> PublicationFeedPagePublic:
    return service.community_feed(user, cursor=cursor, limit=limit)


@router.get("/me/publication-inbox", response_model=PublicationFeedPagePublic)
def publication_inbox(
    cursor: str | None = Query(default=None, max_length=300),
    limit: int = Query(default=20, ge=1, le=30),
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> PublicationFeedPagePublic:
    return service.inbox(user, cursor=cursor, limit=limit)
