from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Header, Query, Response, status

from app.api.dependencies import get_creation_service, get_current_user
from app.domains.creations.contracts import (
    CreationChangeLogListPublic,
    CreationProjectCreate,
    CreationProjectListPublic,
    CreationProjectPatch,
    CreationProjectPublic,
    CreationSubmissionCreate,
    CreationVersionCreate,
    CreationVersionListPublic,
    CreationVersionPublic,
    LearningCardPublic,
    LearningCardPut,
    ProvenanceManifestPublic,
    ProvenanceManifestPut,
    PublicationPublic,
)
from app.domains.creations.models import CreationProjectStatus
from app.domains.creations.service import CreationService
from app.models import User


router = APIRouter(prefix="/v1", tags=["creations"])


@router.post(
    "/creation-projects",
    response_model=CreationProjectPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_project(
    payload: CreationProjectCreate,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.create_project(user, payload)


@router.get("/me/creation-projects", response_model=CreationProjectListPublic)
def list_projects(
    project_status: CreationProjectStatus | None = Query(default=None, alias="status"),
    cursor: str | None = Query(default=None, max_length=120),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectListPublic:
    return service.list_projects(
        user,
        status=project_status,
        cursor=cursor,
        limit=limit,
    )


@router.get(
    "/creation-projects/{project_id}", response_model=CreationProjectPublic
)
def get_project(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.get_project(user, project_id)


@router.patch(
    "/creation-projects/{project_id}", response_model=CreationProjectPublic
)
def update_project(
    project_id: uuid.UUID,
    payload: CreationProjectPatch,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationProjectPublic:
    return service.update_project(user, project_id, payload)


@router.delete(
    "/creation-projects/{project_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_project(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> Response:
    service.delete_project(user, project_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/creation-projects/{project_id}/versions",
    response_model=CreationVersionPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_version(
    project_id: uuid.UUID,
    payload: CreationVersionCreate,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionPublic:
    return service.create_version(user, project_id, payload)


@router.get(
    "/creation-projects/{project_id}/versions",
    response_model=CreationVersionListPublic,
)
def list_versions(
    project_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionListPublic:
    return service.list_versions(user, project_id)


@router.get(
    "/creation-projects/{project_id}/change-logs",
    response_model=CreationChangeLogListPublic,
)
def list_change_logs(
    project_id: uuid.UUID,
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationChangeLogListPublic:
    return service.list_change_logs(user, project_id, limit=limit)


@router.get("/creation-versions/{version_id}", response_model=CreationVersionPublic)
def get_version(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> CreationVersionPublic:
    return service.get_version(user, version_id)


@router.put(
    "/creation-versions/{version_id}/learning-card",
    response_model=LearningCardPublic,
)
def put_learning_card(
    version_id: uuid.UUID,
    payload: LearningCardPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> LearningCardPublic:
    return service.put_learning_card(user, version_id, payload)


@router.get(
    "/creation-versions/{version_id}/learning-card",
    response_model=LearningCardPublic,
)
def get_learning_card(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> LearningCardPublic:
    return service.get_learning_card(user, version_id)


@router.put(
    "/creation-versions/{version_id}/provenance-manifest",
    response_model=ProvenanceManifestPublic,
)
def put_provenance_manifest(
    version_id: uuid.UUID,
    payload: ProvenanceManifestPut,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> ProvenanceManifestPublic:
    return service.put_provenance_manifest(user, version_id, payload)


@router.get(
    "/creation-versions/{version_id}/provenance-manifest",
    response_model=ProvenanceManifestPublic,
)
def get_provenance_manifest(
    version_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> ProvenanceManifestPublic:
    return service.get_provenance_manifest(user, version_id)


@router.post(
    "/creation-projects/{project_id}/submissions",
    response_model=PublicationPublic,
    status_code=status.HTTP_201_CREATED,
)
def submit_creation(
    project_id: uuid.UUID,
    payload: CreationSubmissionCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: CreationService = Depends(get_creation_service),
) -> PublicationPublic:
    return service.submit(user, project_id, payload, idempotency_key)
