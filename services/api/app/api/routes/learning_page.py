from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Header, Query, status

from app.api.dependencies import get_catalog_service, get_current_user, get_learning_service
from app.domains.catalog.contracts import ManualPageDetailPublic
from app.domains.catalog.service import CatalogService
from app.domains.learning.page_contracts import (
    LearningOverviewPublic,
    LearningRoutePublic,
    LessonReadEventAccepted,
    MigrationEvidenceApproved,
    MigrationEvidenceCreate,
    MigrationEvidenceSubmitted,
    TeachingEvidenceAccepted,
    TeachingEvidenceCreate,
)
from app.domains.learning.service import LearningService
from app.models import User
from app.api.dependencies import require_internal_worker


router = APIRouter(prefix="/v1", tags=["learning-page"])


@router.get("/learning/overview", response_model=LearningOverviewPublic)
def get_learning_overview(
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> LearningOverviewPublic:
    return service.get_learning_overview(user)


@router.get("/learning/route", response_model=LearningRoutePublic)
def route_learning_question(
    q: str = Query(min_length=1, max_length=80),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> LearningRoutePublic:
    return service.route_learning_question(user, q)


@router.get("/lessons/{lesson_id}", response_model=ManualPageDetailPublic)
def get_lesson(
    lesson_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: CatalogService = Depends(get_catalog_service),
) -> ManualPageDetailPublic:
    """Compatibility alias for the lesson terminology used by the new frontend."""
    return service.get_page(user, lesson_id)


@router.post(
    "/lessons/{lesson_id}/read-events",
    response_model=LessonReadEventAccepted,
    status_code=status.HTTP_201_CREATED,
)
def record_lesson_read(
    lesson_id: uuid.UUID,
    idempotency_key: str = Header(alias="Idempotency-Key", min_length=8, max_length=64),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> LessonReadEventAccepted:
    return service.record_lesson_read(user, lesson_id, idempotency_key)


@router.post(
    "/lessons/{lesson_id}/migration-evidence",
    response_model=MigrationEvidenceSubmitted,
    status_code=status.HTTP_201_CREATED,
)
def submit_migration_evidence(
    lesson_id: uuid.UUID,
    payload: MigrationEvidenceCreate,
    idempotency_key: str = Header(alias="Idempotency-Key", min_length=8, max_length=64),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> MigrationEvidenceSubmitted:
    return service.submit_migration_evidence(user, lesson_id, payload, idempotency_key)


@router.post(
    "/internal/learning/evidence/{evidence_id}/approve",
    response_model=MigrationEvidenceApproved,
)
def approve_migration_evidence(
    evidence_id: uuid.UUID,
    _: None = Depends(require_internal_worker),
    service: LearningService = Depends(get_learning_service),
) -> MigrationEvidenceApproved:
    return service.approve_migration_evidence(evidence_id)


@router.post(
    "/lessons/{lesson_id}/teaching-evidence",
    response_model=TeachingEvidenceAccepted,
    status_code=status.HTTP_201_CREATED,
)
def submit_teaching_evidence(
    lesson_id: uuid.UUID,
    payload: TeachingEvidenceCreate,
    idempotency_key: str = Header(alias="Idempotency-Key", min_length=8, max_length=64),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> TeachingEvidenceAccepted:
    return service.submit_teaching_evidence(user, lesson_id, payload, idempotency_key)
