from __future__ import annotations

import uuid

from fastapi import APIRouter, Depends, Query, Response, status

from app.api.dependencies import (
    get_conference_service,
    get_current_user,
    require_internal_worker,
)
from app.domains.conference.contracts import (
    ConferenceCollectionListPublic,
    ConferenceCollectionPublic,
    ConferenceLikePublic,
    ConferenceDerivativeAuthorizationPublic,
    ConferenceDerivativeDecision,
    ConferenceDerivativeRequestCreate,
    ConferenceDerivativeRequestListPublic,
    ConferenceDerivativeRequestPublic,
    ConferenceLetterListPublic,
    ConferenceLetterPublic,
    ConferenceMatchAnswerCreate,
    ConferenceMatchAnswerSubmittedPublic,
    ConferenceMatchDetailPublic,
    ConferenceMatchEvaluationCreate,
    ConferenceMatchEvaluationPublic,
    ConferenceMatchJoin,
    ConferenceMatchJudgmentCreate,
    ConferenceMatchJudgmentPublic,
    ConferenceMatchJudgmentQueuePublic,
    ConferenceMatchHistoryOutcome,
    ConferenceMatchQueuePublic,
    ConferenceMatchRecordListPublic,
    ConferenceMatchReflectionCreate,
    ConferenceMatchReflectionPublic,
    ConferenceMatchReflectionStatus,
    ConferenceMatchReportCreate,
    ConferenceMatchReportDecision,
    ConferenceMatchReportListPublic,
    ConferenceMatchReportPublic,
    ConferenceMatchResultPublic,
    ConferenceMatchTeacherEvaluationCreate,
    ConferenceReviewCreate,
    ConferenceReviewAdoptionCreate,
    ConferenceReviewDecision,
    ConferenceReviewListPublic,
    ConferenceReviewPublic,
    ConferenceReviewReportCreate,
    ConferenceReviewReportDecision,
    ConferenceReviewReportListPublic,
    ConferenceReviewReportPublic,
    DerivativeRequestScope,
)
from app.domains.conference.models import (
    ConferenceMatchReportStatus,
    ConferenceReviewReportStatus,
)
from app.domains.conference.service import ConferenceService
from app.domains.distribution.contracts import (
    PublicationFeedItemPublic,
    PublicationFeedPagePublic,
)
from app.domains.creations.models import ConferenceCategory
from app.models import User


router = APIRouter(prefix="/v1/conference", tags=["conference"])
internal_router = APIRouter(
    prefix="/v1/internal/conference",
    tags=["conference-internal"],
    dependencies=[Depends(require_internal_worker)],
)


@router.get("/feed", response_model=PublicationFeedPagePublic)
def conference_feed(
    cursor: str | None = Query(default=None, max_length=300),
    category: ConferenceCategory | None = Query(default=None),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> PublicationFeedPagePublic:
    return service.distribution.community_feed(
        user, cursor=cursor, limit=5, category=category
    )


@router.get("/me/works", response_model=PublicationFeedPagePublic)
def list_my_conference_works(
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> PublicationFeedPagePublic:
    return service.distribution.owned_community_feed(user, limit=limit)


@router.get(
    "/publications/{publication_id}", response_model=PublicationFeedItemPublic
)
def get_conference_work(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> PublicationFeedItemPublic:
    return service.get_work(user, publication_id)


@router.put("/likes/{publication_id}", response_model=ConferenceLikePublic)
def add_conference_like(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceLikePublic:
    return service.add_like(user, publication_id)


@router.delete("/likes/{publication_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_conference_like(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> Response:
    service.remove_like(user, publication_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/publications/{publication_id}/reviews",
    response_model=ConferenceReviewPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_conference_review(
    publication_id: uuid.UUID,
    payload: ConferenceReviewCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewPublic:
    return service.create_review(user, publication_id, payload)


@router.get(
    "/publications/{publication_id}/reviews",
    response_model=ConferenceReviewListPublic,
)
def list_conference_reviews(
    publication_id: uuid.UUID,
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewListPublic:
    return service.list_reviews(user, publication_id, limit=limit)


@router.post("/reviews/{review_id}/decision", response_model=ConferenceReviewPublic)
def decide_conference_review(
    review_id: uuid.UUID,
    payload: ConferenceReviewDecision,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewPublic:
    return service.decide_review(user, review_id, payload)


@router.post("/reviews/{review_id}/adoption", response_model=ConferenceReviewPublic)
def link_conference_review_adoption(
    review_id: uuid.UUID,
    payload: ConferenceReviewAdoptionCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewPublic:
    return service.link_review_adoption(user, review_id, payload)


@router.post(
    "/reviews/{review_id}/reports",
    response_model=ConferenceReviewReportPublic,
    status_code=status.HTTP_201_CREATED,
)
def report_conference_review(
    review_id: uuid.UUID,
    payload: ConferenceReviewReportCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewReportPublic:
    return service.report_review(user, review_id, payload)


@internal_router.post(
    "/review-reports/{report_id}/decision",
    response_model=ConferenceReviewReportPublic,
)
def decide_conference_review_report(
    report_id: uuid.UUID,
    payload: ConferenceReviewReportDecision,
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewReportPublic:
    return service.decide_review_report(report_id, payload)


@internal_router.get(
    "/review-reports",
    response_model=ConferenceReviewReportListPublic,
)
def list_conference_review_reports(
    report_status: ConferenceReviewReportStatus = ConferenceReviewReportStatus.PENDING,
    limit: int = Query(default=50, ge=1, le=100),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceReviewReportListPublic:
    return service.list_review_reports(report_status=report_status, limit=limit)


@router.put(
    "/collections/{publication_id}", response_model=ConferenceCollectionPublic
)
def add_conference_collection(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceCollectionPublic:
    return service.add_collection(user, publication_id)


@router.get("/me/collections", response_model=ConferenceCollectionListPublic)
def list_conference_collections(
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceCollectionListPublic:
    return service.list_collections(user, limit=limit)


@router.delete(
    "/collections/{publication_id}", status_code=status.HTTP_204_NO_CONTENT
)
def remove_conference_collection(
    publication_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> Response:
    service.remove_collection(user, publication_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/derivative-requests",
    response_model=ConferenceDerivativeRequestPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_conference_derivative_request(
    payload: ConferenceDerivativeRequestCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceDerivativeRequestPublic:
    return service.create_derivative_request(user, payload)


@router.get(
    "/me/derivative-requests", response_model=ConferenceDerivativeRequestListPublic
)
def list_conference_derivative_requests(
    scope: DerivativeRequestScope = DerivativeRequestScope.REQUESTED,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceDerivativeRequestListPublic:
    return service.list_derivative_requests(user, scope=scope)


@router.post(
    "/derivative-requests/{request_id}/decision",
    response_model=ConferenceDerivativeRequestPublic,
)
def decide_conference_derivative_request(
    request_id: uuid.UUID,
    payload: ConferenceDerivativeDecision,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceDerivativeRequestPublic:
    return service.decide_derivative_request(user, request_id, payload)


@router.post(
    "/derivative-authorizations/{authorization_id}/revoke",
    response_model=ConferenceDerivativeAuthorizationPublic,
)
def revoke_conference_derivative_authorization(
    authorization_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceDerivativeAuthorizationPublic:
    return service.revoke_derivative_authorization(user, authorization_id)


@router.get("/match-queue", response_model=ConferenceMatchQueuePublic)
def get_conference_match_queue(
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchQueuePublic:
    return service.get_match_queue(user)


@router.post("/match-queue", response_model=ConferenceMatchQueuePublic)
def join_conference_match_queue(
    payload: ConferenceMatchJoin,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchQueuePublic:
    return service.join_match_queue(user, payload)


@router.delete("/match-queue", response_model=ConferenceMatchQueuePublic)
def exit_conference_match_queue(
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchQueuePublic:
    return service.exit_match_queue(user)


@router.get("/matches", response_model=ConferenceMatchRecordListPublic)
def list_conference_match_records(
    outcome: ConferenceMatchHistoryOutcome | None = Query(default=None),
    reflection_status: ConferenceMatchReflectionStatus | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=50),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchRecordListPublic:
    return service.list_match_records(
        user,
        outcome=outcome,
        reflection_status=reflection_status,
        page=page,
        limit=limit,
    )


@router.get("/matches/{match_id}", response_model=ConferenceMatchDetailPublic)
def get_conference_match(
    match_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchDetailPublic:
    return service.get_match(user, match_id)


@router.post(
    "/matches/{match_id}/answers",
    response_model=ConferenceMatchAnswerSubmittedPublic,
    status_code=status.HTTP_201_CREATED,
)
def answer_conference_match_question(
    match_id: uuid.UUID,
    payload: ConferenceMatchAnswerCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchAnswerSubmittedPublic:
    return service.answer_match_question(user, match_id, payload)


@router.get(
    "/matches/{match_id}/result", response_model=ConferenceMatchResultPublic
)
def get_conference_match_result(
    match_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchResultPublic:
    return service.get_match_result(user, match_id)


@router.post(
    "/matches/{match_id}/reflections",
    response_model=ConferenceMatchReflectionPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_conference_match_reflection(
    match_id: uuid.UUID,
    payload: ConferenceMatchReflectionCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchReflectionPublic:
    return service.create_match_reflection(user, match_id, payload)


@router.post(
    "/matches/{match_id}/evaluations",
    response_model=ConferenceMatchEvaluationPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_conference_match_evaluation(
    match_id: uuid.UUID,
    payload: ConferenceMatchEvaluationCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchEvaluationPublic:
    return service.create_match_evaluation(user, match_id, payload)


@router.post(
    "/matches/{match_id}/reports",
    response_model=ConferenceMatchReportPublic,
    status_code=status.HTTP_201_CREATED,
)
def report_conference_match(
    match_id: uuid.UUID,
    payload: ConferenceMatchReportCreate,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchReportPublic:
    return service.report_match(user, match_id, payload)


@internal_router.get(
    "/match-reports",
    response_model=ConferenceMatchReportListPublic,
)
def list_conference_match_reports(
    report_status: ConferenceMatchReportStatus = ConferenceMatchReportStatus.PENDING,
    limit: int = Query(default=50, ge=1, le=100),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchReportListPublic:
    return service.list_match_reports(report_status=report_status, limit=limit)


@internal_router.post(
    "/match-reports/{report_id}/decision",
    response_model=ConferenceMatchReportPublic,
)
def decide_conference_match_report(
    report_id: uuid.UUID,
    payload: ConferenceMatchReportDecision,
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchReportPublic:
    return service.decide_match_report(report_id, payload)


@router.get("/letters", response_model=ConferenceLetterListPublic)
def list_conference_letters(
    limit: int = Query(default=50, ge=1, le=100),
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceLetterListPublic:
    return service.list_letters(user, limit=limit)


@router.put("/letters/{letter_id}/read", response_model=ConferenceLetterPublic)
def read_conference_letter(
    letter_id: uuid.UUID,
    user: User = Depends(get_current_user),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceLetterPublic:
    return service.read_letter(user, letter_id)


@internal_router.get(
    "/matches/pending-judgment",
    response_model=ConferenceMatchJudgmentQueuePublic,
)
def list_conference_matches_pending_judgment(
    limit: int = Query(default=20, ge=1, le=100),
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchJudgmentQueuePublic:
    return service.list_matches_pending_judgment(limit=limit)


@internal_router.post(
    "/matches/{match_id}/judgment",
    response_model=ConferenceMatchJudgmentPublic,
)
def judge_conference_match(
    match_id: uuid.UUID,
    payload: ConferenceMatchJudgmentCreate,
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchJudgmentPublic:
    return service.judge_match(match_id, payload)


@internal_router.post(
    "/matches/{match_id}/teacher-evaluations",
    response_model=ConferenceMatchEvaluationPublic,
    status_code=status.HTTP_201_CREATED,
)
def create_conference_match_teacher_evaluation(
    match_id: uuid.UUID,
    payload: ConferenceMatchTeacherEvaluationCreate,
    service: ConferenceService = Depends(get_conference_service),
) -> ConferenceMatchEvaluationPublic:
    return service.create_teacher_evaluation(match_id, payload)
