from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, status

from app.api.dependencies import (
    get_current_user,
    get_moderation_service,
    require_internal_worker,
)
from app.domains.moderation.contracts import (
    InternalAppealDecision,
    InternalModerationDecision,
    ModerationAppealCreate,
    ModerationAppealPublic,
    ModerationCasePublic,
    WithdrawPublication,
)
from app.domains.moderation.service import ModerationService
from app.models import User


router = APIRouter(prefix="/v1", tags=["moderation"])


@router.get("/moderation-cases/{case_id}", response_model=ModerationCasePublic)
def get_moderation_case(
    case_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationCasePublic:
    return service.get_case(user, case_id)


@router.get(
    "/publications/{publication_id}/moderation-case",
    response_model=ModerationCasePublic,
)
def get_publication_moderation_case(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationCasePublic:
    return service.get_publication_case(user, publication_id)


@router.post(
    "/internal/moderation-cases/{case_id}/route",
    response_model=ModerationCasePublic,
    dependencies=[Depends(require_internal_worker)],
)
def route_moderation_case(
    case_id: uuid.UUID,
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationCasePublic:
    return service.route_to_human_review(case_id)


@router.post(
    "/internal/moderation-cases/{case_id}/decision",
    response_model=ModerationCasePublic,
    dependencies=[Depends(require_internal_worker)],
)
def decide_moderation_case(
    case_id: uuid.UUID,
    payload: InternalModerationDecision,
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationCasePublic:
    return service.decide(case_id, payload)


@router.post(
    "/publications/{publication_id}/withdraw",
    response_model=ModerationCasePublic,
)
def withdraw_publication(
    publication_id: uuid.UUID,
    payload: WithdrawPublication,
    user: User = Depends(get_current_user),
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationCasePublic:
    return service.withdraw(user, publication_id, payload)


@router.post(
    "/moderation-cases/{case_id}/appeals",
    response_model=ModerationAppealPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_moderation_appeal(
    case_id: uuid.UUID,
    payload: ModerationAppealCreate,
    user: User = Depends(get_current_user),
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationAppealPublic:
    return service.create_appeal(user, case_id, payload)


@router.get("/me/moderation-appeals", response_model=list[ModerationAppealPublic])
def list_moderation_appeals(
    user: User = Depends(get_current_user),
    service: ModerationService = Depends(get_moderation_service),
) -> list[ModerationAppealPublic]:
    return service.list_appeals(user)


@router.post(
    "/internal/moderation-appeals/{appeal_id}/decision",
    response_model=ModerationAppealPublic,
    dependencies=[Depends(require_internal_worker)],
)
def decide_moderation_appeal(
    appeal_id: uuid.UUID,
    payload: InternalAppealDecision,
    service: ModerationService = Depends(get_moderation_service),
) -> ModerationAppealPublic:
    return service.decide_appeal(appeal_id, payload)
