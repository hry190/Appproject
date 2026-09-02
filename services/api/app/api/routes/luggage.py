from __future__ import annotations

from fastapi import APIRouter, Depends, Request, Response

from app.api.dependencies import get_current_user, get_luggage_service
from app.domains.luggage.contracts import LuggageResponse
from app.domains.luggage.service import LuggageService
from app.models import User


router = APIRouter(prefix="/v1/me", tags=["luggage"])
PRIVATE_CACHE_HEADERS = {
    "Cache-Control": "no-store",
    "Vary": "Authorization",
}


@router.get("/luggage", response_model=LuggageResponse)
def get_luggage(
    request: Request,
    response: Response,
    user: User = Depends(get_current_user),
    service: LuggageService = Depends(get_luggage_service),
) -> LuggageResponse | Response:
    snapshot = service.get_snapshot(user)
    if request.headers.get("If-None-Match") == snapshot.meta.etag:
        return Response(
            status_code=304,
            headers={**PRIVATE_CACHE_HEADERS, "ETag": snapshot.meta.etag},
        )
    response.headers.update(PRIVATE_CACHE_HEADERS)
    response.headers["ETag"] = snapshot.meta.etag
    return snapshot
