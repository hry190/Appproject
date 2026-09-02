from __future__ import annotations

from fastapi import APIRouter

from app.domains.learning.contracts import ContractModel


router = APIRouter(prefix="/v1/meta", tags=["meta"])


class CapabilitiesPublic(ContractModel):
    profile: bool
    manual_catalog: bool
    manual_favorites: bool
    luggage_snapshot: str
    learning_progress: bool
    mistakes: bool
    creations: bool
    media_uploads: bool


@router.get("/capabilities", response_model=CapabilitiesPublic)
def get_capabilities() -> CapabilitiesPublic:
    return CapabilitiesPublic(
        profile=True,
        manual_catalog=True,
        manual_favorites=True,
        luggage_snapshot="LIVE_MEDIA_REVIEW",
        learning_progress=True,
        mistakes=True,
        creations=True,
        media_uploads=True,
    )
