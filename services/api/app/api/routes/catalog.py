from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Query, Response, status

from app.api.dependencies import get_catalog_service, get_current_user
from app.domains.catalog.contracts import (
    ManualFavoritePublic,
    ManualPageDetailPublic,
    ManualPageListPublic,
)
from app.domains.catalog.service import CatalogService
from app.domains.learning.contracts import ManualProgressState
from app.models import User


router = APIRouter(prefix="/v1/manuals", tags=["manuals"])


@router.get("", response_model=ManualPageListPublic)
def list_manuals(
    volume: int | None = Query(default=None, ge=1, le=10),
    q: str | None = Query(default=None, max_length=80),
    state: ManualProgressState | None = None,
    favorites_only: bool = False,
    cursor: str | None = Query(default=None, max_length=120),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: CatalogService = Depends(get_catalog_service),
) -> ManualPageListPublic:
    return service.list_pages(
        user,
        volume=volume,
        query=q,
        state=state,
        favorites_only=favorites_only,
        cursor=cursor,
        limit=limit,
    )


@router.get("/{manual_page_id}", response_model=ManualPageDetailPublic)
def get_manual(
    manual_page_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CatalogService = Depends(get_catalog_service),
) -> ManualPageDetailPublic:
    return service.get_page(user, manual_page_id)


@router.put("/{manual_page_id}/favorite", response_model=ManualFavoritePublic)
def add_manual_favorite(
    manual_page_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CatalogService = Depends(get_catalog_service),
) -> ManualFavoritePublic:
    return service.add_favorite(user, manual_page_id)


@router.delete(
    "/{manual_page_id}/favorite",
    status_code=status.HTTP_204_NO_CONTENT,
)
def remove_manual_favorite(
    manual_page_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CatalogService = Depends(get_catalog_service),
) -> Response:
    service.remove_favorite(user, manual_page_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
