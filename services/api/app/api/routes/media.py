from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Header, status

from app.api.dependencies import (
    get_current_user,
    get_media_service,
    require_internal_worker,
)
from app.domains.media.contracts import (
    InternalMediaProcessRequest,
    MediaAssetPublic,
    MediaDeleteAccepted,
    UploadComplete,
    UploadIntentCreate,
    UploadIntentPublic,
)
from app.domains.media.service import MediaService
from app.models import User


router = APIRouter(prefix="/v1", tags=["media"])


@router.post(
    "/uploads/intents",
    response_model=UploadIntentPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_upload_intent(
    payload: UploadIntentCreate,
    user: User = Depends(get_current_user),
    service: MediaService = Depends(get_media_service),
) -> UploadIntentPublic:
    return service.create_upload_intent(user, payload)


@router.post(
    "/uploads/{upload_id}/complete",
    response_model=MediaAssetPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def complete_upload(
    upload_id: uuid.UUID,
    payload: UploadComplete,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: MediaService = Depends(get_media_service),
) -> MediaAssetPublic:
    return service.complete_upload(user, upload_id, payload, idempotency_key)


@router.get("/media-assets/{asset_id}", response_model=MediaAssetPublic)
def get_media_asset(
    asset_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: MediaService = Depends(get_media_service),
) -> MediaAssetPublic:
    return service.get_asset(user, asset_id)


@router.delete(
    "/media-assets/{asset_id}",
    response_model=MediaDeleteAccepted,
    status_code=status.HTTP_202_ACCEPTED,
)
def delete_media_asset(
    asset_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: MediaService = Depends(get_media_service),
) -> MediaDeleteAccepted:
    return service.request_delete(user, asset_id)


@router.post(
    "/internal/media-assets/{asset_id}/process",
    response_model=MediaAssetPublic,
    dependencies=[Depends(require_internal_worker)],
)
def process_media_asset(
    asset_id: uuid.UUID,
    payload: InternalMediaProcessRequest,
    service: MediaService = Depends(get_media_service),
) -> MediaAssetPublic:
    return service.process_asset(asset_id, payload)


@router.post(
    "/internal/media-assets/{asset_id}/process-deletion",
    response_model=MediaDeleteAccepted,
    dependencies=[Depends(require_internal_worker)],
)
def process_media_deletion(
    asset_id: uuid.UUID,
    service: MediaService = Depends(get_media_service),
) -> MediaDeleteAccepted:
    return service.process_deletion(asset_id)
