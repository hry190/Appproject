from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Header, Query, status

from app.api.dependencies import get_current_user, get_learning_service
from app.domains.learning.contracts import (
    EvidenceAwardPublic,
    EvidenceCategory,
    EvidenceListPublic,
    LearningStatsPublic,
    ManualLearningHistoryPublic,
    TrialAttemptAccepted,
    TrialAttemptCreate,
    TrialPublic,
)
from app.domains.learning.service import LearningService
from app.models import User


router = APIRouter(prefix="/v1", tags=["learning"])


@router.get("/trials/{trial_id}", response_model=TrialPublic)
def get_trial(
    trial_id: uuid.UUID,
    _user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> TrialPublic:
    return service.get_trial(trial_id)


@router.post(
    "/trials/{trial_id}/attempts",
    response_model=TrialAttemptAccepted,
    status_code=status.HTTP_201_CREATED,
)
def submit_trial_attempt(
    trial_id: uuid.UUID,
    payload: TrialAttemptCreate,
    idempotency_key: str = Header(
        alias="Idempotency-Key",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9._:-]+$",
    ),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> TrialAttemptAccepted:
    return service.submit_attempt(user, trial_id, payload, idempotency_key)


@router.get("/me/learning-stats", response_model=LearningStatsPublic)
def get_learning_stats(
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> LearningStatsPublic:
    return service.get_stats(user)


@router.get("/me/learning-evidence", response_model=EvidenceListPublic)
def list_learning_evidence(
    category: EvidenceCategory | None = None,
    week_only: bool = False,
    cursor: str | None = Query(default=None, max_length=120),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> EvidenceListPublic:
    return service.list_user_evidence(
        user,
        category=category,
        week_only=week_only,
        cursor=cursor,
        limit=limit,
    )


@router.get(
    "/manuals/{manual_page_id}/evidence",
    response_model=list[EvidenceAwardPublic],
)
def list_manual_evidence(
    manual_page_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> list[EvidenceAwardPublic]:
    return service.list_evidence(user, manual_page_id)


@router.get(
    "/manuals/{manual_page_id}/learning-history",
    response_model=ManualLearningHistoryPublic,
)
def get_manual_learning_history(
    manual_page_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: LearningService = Depends(get_learning_service),
) -> ManualLearningHistoryPublic:
    return service.get_learning_history(user, manual_page_id)
