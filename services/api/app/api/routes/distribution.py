from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from app.api.dependencies import get_current_user, get_distribution_service
from app.domains.distribution.contracts import (
    PublicationFeedPagePublic,
)
from app.domains.distribution.service import DistributionService
from app.models import User


router = APIRouter(prefix="/v1", tags=["distribution"])


@router.get("/community/feed", response_model=PublicationFeedPagePublic)
def community_feed(
    cursor: str | None = Query(default=None, max_length=300),
    limit: int = Query(default=20, ge=1, le=30),
    user: User = Depends(get_current_user),
    service: DistributionService = Depends(get_distribution_service),
) -> PublicationFeedPagePublic:
    return service.community_feed(user, cursor=cursor, limit=limit)
