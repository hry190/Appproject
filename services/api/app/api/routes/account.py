from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Response, status

from app.api.dependencies import get_current_user, get_user_settings_service
from app.models import User
from app.schemas import (
    AccountExport,
    DataRightsRequestCreate,
    DataRightsRequestPublic,
    SessionPublic,
)
from app.services.user_settings import UserSettingsService


router = APIRouter(prefix="/v1/account", tags=["account"])


@router.get("/sessions", response_model=list[SessionPublic])
def list_sessions(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> list[SessionPublic]:
    return service.list_sessions(user)


@router.delete("/sessions/{session_id}", status_code=status.HTTP_204_NO_CONTENT)
def revoke_session(
    session_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> Response:
    service.revoke_session(user, session_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/export", response_model=AccountExport)
def export_account(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> AccountExport:
    return service.export_account(user)


@router.post(
    "/data-rights-requests",
    response_model=DataRightsRequestPublic,
    status_code=status.HTTP_202_ACCEPTED,
)
def create_data_rights_request(
    payload: DataRightsRequestCreate,
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> DataRightsRequestPublic:
    return DataRightsRequestPublic.model_validate(
        service.create_data_request(user, payload)
    )


@router.get("/data-rights-requests", response_model=list[DataRightsRequestPublic])
def list_data_rights_requests(
    user: User = Depends(get_current_user),
    service: UserSettingsService = Depends(get_user_settings_service),
) -> list[DataRightsRequestPublic]:
    return [
        DataRightsRequestPublic.model_validate(item)
        for item in service.list_data_requests(user)
    ]
