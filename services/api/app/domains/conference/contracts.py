from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import Field, field_validator, model_validator

from app.domains.conference.models import (
    ConferenceLetterCategory,
    ConferenceMatchEndReason,
    ConferenceMatchEvaluationKind,
    ConferenceMatchQuestionKind,
    ConferenceMatchReportReason,
    ConferenceMatchReportStatus,
    ConferenceMatchStatus,
    ConferenceReviewModerationStatus,
    ConferenceReviewReportReason,
    ConferenceReviewReportStatus,
    ConferenceReviewStatus,
    ConferenceReviewTemplate,
    DerivativeAuthorizationStatus,
    DerivativeRequestStatus,
)
from app.domains.distribution.contracts import PublicationFeedItemPublic
from app.domains.learning.contracts import ContractModel


class ReviewAuthorAction(str, Enum):
    ACCEPT = "ACCEPT"
    THINK = "THINK"
    REPLY = "REPLY"


class ReviewReportDecision(str, Enum):
    REMOVE = "REMOVE"
    KEEP = "KEEP"


class MatchReportDecision(str, Enum):
    CONFIRM = "CONFIRM"
    DISMISS = "DISMISS"


class DerivativeAuthorDecision(str, Enum):
    APPROVE = "APPROVE"
    REJECT = "REJECT"


class DerivativeRequestScope(str, Enum):
    REQUESTED = "REQUESTED"
    RECEIVED = "RECEIVED"


class ConferenceMatchQueueState(str, Enum):
    IDLE = "IDLE"
    WAITING = "WAITING"
    MATCHED = "MATCHED"
    EXITED = "EXITED"


class ConferenceUserEvaluationKind(str, Enum):
    SELF = "SELF"
    PEER = "PEER"


class ConferenceMatchPerspective(str, Enum):
    SELF = "SELF"
    OPPONENT = "OPPONENT"


class ConferenceMatchOutcome(str, Enum):
    PENDING = "PENDING"
    WIN = "WIN"
    LOSE = "LOSE"
    TIE = "TIE"
    ENDED_WITHOUT_RESULT = "ENDED_WITHOUT_RESULT"


class ConferenceReviewCreate(ContractModel):
    template: ConferenceReviewTemplate
    content: str = Field(min_length=1, max_length=500)

    @field_validator("content")
    @classmethod
    def normalize_content(cls, value: str) -> str:
        normalized = value.strip()
        if not normalized:
            raise ValueError("content must not be blank")
        return normalized


class ConferenceReviewDecision(ContractModel):
    action: ReviewAuthorAction
    reply: str | None = Field(default=None, max_length=500)
    row_version: int = Field(ge=1)

    @field_validator("reply")
    @classmethod
    def normalize_reply(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None

    @model_validator(mode="after")
    def reply_must_match_action(self) -> "ConferenceReviewDecision":
        if self.action == ReviewAuthorAction.REPLY and self.reply is None:
            raise ValueError("reply is required when action is REPLY")
        if self.action != ReviewAuthorAction.REPLY and self.reply is not None:
            raise ValueError("reply is only allowed when action is REPLY")
        return self


class ConferenceReviewAdoptionCreate(ContractModel):
    creation_version_id: uuid.UUID
    summary: str = Field(min_length=1, max_length=500)
    row_version: int = Field(ge=1)

    @field_validator("summary")
    @classmethod
    def normalize_summary(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("summary must not be blank")
        return normalized


class ConferenceReviewPublic(ContractModel):
    id: uuid.UUID
    publication_id: uuid.UUID
    reviewer_nickname: str
    template: ConferenceReviewTemplate
    content: str
    status: ConferenceReviewStatus
    moderation_status: ConferenceReviewModerationStatus
    author_reply: str | None
    handled_at: datetime | None
    adopted_in_creation_version_id: uuid.UUID | None
    adoption_summary: str | None
    adopted_at: datetime | None
    row_version: int = Field(ge=1)
    created_at: datetime


class ConferenceReviewListPublic(ContractModel):
    items: list[ConferenceReviewPublic]


class ConferenceReviewReportCreate(ContractModel):
    reason: ConferenceReviewReportReason
    details: str | None = Field(default=None, max_length=500)

    @field_validator("details")
    @classmethod
    def normalize_details(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None


class ConferenceReviewReportDecision(ContractModel):
    decision: ReviewReportDecision
    reviewer_reference: str = Field(min_length=3, max_length=80)
    resolution_summary: str = Field(min_length=1, max_length=500)
    row_version: int = Field(ge=1)

    @field_validator("reviewer_reference", "resolution_summary")
    @classmethod
    def normalize_required_text(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class ConferenceReviewReportPublic(ContractModel):
    id: uuid.UUID
    review_id: uuid.UUID
    reason: ConferenceReviewReportReason
    status: ConferenceReviewReportStatus
    resolution_summary: str | None
    row_version: int = Field(ge=1)
    created_at: datetime
    resolved_at: datetime | None


class ConferenceReviewReportInternalPublic(ConferenceReviewReportPublic):
    publication_id: uuid.UUID
    review_author_user_id: uuid.UUID
    review_template: ConferenceReviewTemplate
    review_content: str
    report_details: str | None


class ConferenceReviewReportListPublic(ContractModel):
    items: list[ConferenceReviewReportInternalPublic]


class ConferenceCollectionPublic(ContractModel):
    publication_id: uuid.UUID
    saved_at: datetime
    work: PublicationFeedItemPublic


class ConferenceCollectionListPublic(ContractModel):
    items: list[ConferenceCollectionPublic]


class ConferenceDerivativeRequestCreate(ContractModel):
    source_publication_id: uuid.UUID
    requested_use: str = Field(min_length=1, max_length=1000)

    @field_validator("requested_use")
    @classmethod
    def normalize_requested_use(cls, value: str) -> str:
        normalized = value.strip()
        if not normalized:
            raise ValueError("requested_use must not be blank")
        return normalized


class ConferenceDerivativeDecision(ContractModel):
    decision: DerivativeAuthorDecision
    note: str | None = Field(default=None, max_length=500)

    @field_validator("note")
    @classmethod
    def normalize_note(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None

    @model_validator(mode="after")
    def rejection_requires_note(self) -> "ConferenceDerivativeDecision":
        if self.decision == DerivativeAuthorDecision.REJECT and self.note is None:
            raise ValueError("note is required when decision is REJECT")
        return self


class ConferenceDerivativeAuthorizationPublic(ContractModel):
    id: uuid.UUID
    request_id: uuid.UUID
    source_publication_id: uuid.UUID
    source_creation_version_id: uuid.UUID
    authorization_version: str
    status: DerivativeAuthorizationStatus
    granted_at: datetime
    revoked_at: datetime | None


class ConferenceDerivativeRequestPublic(ContractModel):
    id: uuid.UUID
    source_publication_id: uuid.UUID
    source_creation_version_id: uuid.UUID
    source_title: str
    source_author_nickname: str
    requested_use: str
    status: DerivativeRequestStatus
    author_decision_note: str | None
    decided_at: datetime | None
    authorization: ConferenceDerivativeAuthorizationPublic | None
    created_at: datetime


class ConferenceDerivativeRequestListPublic(ContractModel):
    items: list[ConferenceDerivativeRequestPublic]


class ConferenceMatchJoin(ContractModel):
    manual_page_id: uuid.UUID


class ConferenceMatchQueuePublic(ContractModel):
    queue_id: uuid.UUID | None
    status: ConferenceMatchQueueState
    manual_page_id: uuid.UUID | None
    match_id: uuid.UUID | None
    joined_at: datetime | None
    expires_at: datetime | None
    updated_at: datetime | None


class ConferenceMatchQuestionPublic(ContractModel):
    id: uuid.UUID
    position: int = Field(ge=1, le=5)
    kind: ConferenceMatchQuestionKind
    prompt: str


class ConferenceMatchAnswerCreate(ContractModel):
    question_id: uuid.UUID
    answer: str = Field(min_length=1, max_length=2000)
    reason: str = Field(min_length=1, max_length=1000)

    @field_validator("answer", "reason")
    @classmethod
    def normalize_answer_text(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class ConferenceMatchAnswerPublic(ContractModel):
    id: uuid.UUID
    question_id: uuid.UUID
    answer: str
    reason: str
    submitted_at: datetime


class ConferenceMatchProgressPublic(ContractModel):
    answered: int = Field(ge=0)
    total: int = Field(ge=3, le=5)
    complete: bool


class ConferenceMatchDetailPublic(ContractModel):
    match_id: uuid.UUID
    manual_page_id: uuid.UUID
    status: ConferenceMatchStatus
    questions: list[ConferenceMatchQuestionPublic]
    my_answers: list[ConferenceMatchAnswerPublic]
    my_progress: ConferenceMatchProgressPublic
    opponent_progress: ConferenceMatchProgressPublic
    matched_at: datetime


class ConferenceMatchAnswerSubmittedPublic(ContractModel):
    answer: ConferenceMatchAnswerPublic
    my_progress: ConferenceMatchProgressPublic
    opponent_progress: ConferenceMatchProgressPublic
    match_status: ConferenceMatchStatus


class ConferenceMatchAnswerInternalPublic(ConferenceMatchAnswerPublic):
    participant_user_id: uuid.UUID


class ConferenceMatchJudgmentCandidatePublic(ContractModel):
    match_id: uuid.UUID
    manual_page_id: uuid.UUID
    participant_user_ids: list[uuid.UUID]
    questions: list[ConferenceMatchQuestionPublic]
    rubrics: dict[str, str]
    answers: list[ConferenceMatchAnswerInternalPublic]
    matched_at: datetime


class ConferenceMatchJudgmentQueuePublic(ContractModel):
    items: list[ConferenceMatchJudgmentCandidatePublic]


class ConferenceMatchJudgmentParticipant(ContractModel):
    user_id: uuid.UUID
    score: float = Field(ge=0, le=100)
    dimension_scores: dict[str, float] = Field(default_factory=dict)
    summary: str = Field(min_length=1, max_length=1000)
    strengths: list[str] = Field(default_factory=list, max_length=5)
    improvements: list[str] = Field(default_factory=list, max_length=5)

    @field_validator("summary")
    @classmethod
    def normalize_judgment_summary(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("summary must not be blank")
        return normalized

    @field_validator("strengths", "improvements")
    @classmethod
    def normalize_judgment_points(cls, value: list[str]) -> list[str]:
        normalized = [" ".join(item.strip().split()) for item in value]
        if any(not item or len(item) > 300 for item in normalized):
            raise ValueError("each point must contain 1-300 characters")
        return normalized

    @field_validator("dimension_scores")
    @classmethod
    def validate_dimension_scores(cls, value: dict[str, float]) -> dict[str, float]:
        if len(value) > 10 or any(not key.strip() or score < 0 or score > 100 for key, score in value.items()):
            raise ValueError("dimension scores must use 1-10 named dimensions scored 0-100")
        return {key.strip(): score for key, score in value.items()}


class ConferenceMatchJudgmentCreate(ContractModel):
    evaluator_reference: str = Field(min_length=3, max_length=120)
    participants: list[ConferenceMatchJudgmentParticipant] = Field(min_length=2, max_length=2)

    @field_validator("evaluator_reference")
    @classmethod
    def normalize_evaluator_reference(cls, value: str) -> str:
        return " ".join(value.strip().split())

    @model_validator(mode="after")
    def participants_must_be_unique(self) -> "ConferenceMatchJudgmentCreate":
        if len({item.user_id for item in self.participants}) != 2:
            raise ValueError("participants must identify two different users")
        return self


class ConferenceMatchJudgmentPublic(ContractModel):
    match_id: uuid.UUID
    status: ConferenceMatchStatus
    winner_user_id: uuid.UUID | None


class ConferenceMatchEvaluationCreate(ContractModel):
    kind: ConferenceUserEvaluationKind
    score: float = Field(ge=0, le=100)
    dimension_scores: dict[str, float] = Field(default_factory=dict)
    summary: str = Field(min_length=1, max_length=1000)
    strengths: list[str] = Field(default_factory=list, max_length=5)
    improvements: list[str] = Field(default_factory=list, max_length=5)

    @field_validator("summary")
    @classmethod
    def normalize_evaluation_summary(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("summary must not be blank")
        return normalized

    @field_validator("strengths", "improvements")
    @classmethod
    def normalize_evaluation_points(cls, value: list[str]) -> list[str]:
        return ConferenceMatchJudgmentParticipant.normalize_judgment_points(value)

    @field_validator("dimension_scores")
    @classmethod
    def validate_evaluation_dimensions(cls, value: dict[str, float]) -> dict[str, float]:
        return ConferenceMatchJudgmentParticipant.validate_dimension_scores(value)


class ConferenceMatchTeacherEvaluationCreate(ContractModel):
    subject_user_id: uuid.UUID
    evaluator_reference: str = Field(min_length=3, max_length=120)
    score: float = Field(ge=0, le=100)
    dimension_scores: dict[str, float] = Field(default_factory=dict)
    summary: str = Field(min_length=1, max_length=1000)
    strengths: list[str] = Field(default_factory=list, max_length=5)
    improvements: list[str] = Field(default_factory=list, max_length=5)

    @field_validator("evaluator_reference", "summary")
    @classmethod
    def normalize_teacher_text(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized

    @field_validator("strengths", "improvements")
    @classmethod
    def normalize_teacher_points(cls, value: list[str]) -> list[str]:
        return ConferenceMatchJudgmentParticipant.normalize_judgment_points(value)

    @field_validator("dimension_scores")
    @classmethod
    def validate_teacher_dimensions(cls, value: dict[str, float]) -> dict[str, float]:
        return ConferenceMatchJudgmentParticipant.validate_dimension_scores(value)


class ConferenceMatchEvaluationPublic(ContractModel):
    id: uuid.UUID
    kind: ConferenceMatchEvaluationKind
    score: float = Field(ge=0, le=100)
    dimension_scores: dict[str, float]
    summary: str
    strengths: list[str]
    improvements: list[str]
    evaluator_reference: str | None
    created_at: datetime


class ConferenceMatchReflectionCreate(ContractModel):
    learned: str = Field(min_length=1, max_length=1000)
    next_improvement: str = Field(min_length=1, max_length=1000)

    @field_validator("learned", "next_improvement")
    @classmethod
    def normalize_reflection_text(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class ConferenceMatchReflectionPublic(ContractModel):
    id: uuid.UUID
    match_id: uuid.UUID
    learned: str
    next_improvement: str
    created_at: datetime


class ConferenceMatchParticipantResultPublic(ContractModel):
    perspective: ConferenceMatchPerspective
    score: float | None
    evaluations: list[ConferenceMatchEvaluationPublic]


class ConferenceMatchResultPublic(ContractModel):
    match_id: uuid.UUID
    status: ConferenceMatchStatus
    end_reason: ConferenceMatchEndReason | None
    outcome: ConferenceMatchOutcome
    participants: list[ConferenceMatchParticipantResultPublic]
    my_reflection: ConferenceMatchReflectionPublic | None
    ended_at: datetime | None


class ConferenceLetterPublic(ContractModel):
    id: uuid.UUID
    category: ConferenceLetterCategory
    title: str
    body: str
    action_type: str | None
    action_id: uuid.UUID | None
    navigation_target: str | None
    navigation_id: uuid.UUID | None
    is_read: bool
    read_at: datetime | None
    created_at: datetime


class ConferenceLetterListPublic(ContractModel):
    items: list[ConferenceLetterPublic]
    unread_count: int = Field(ge=0)


class ConferenceMatchReportCreate(ContractModel):
    reason: ConferenceMatchReportReason
    details: str | None = Field(default=None, max_length=500)

    @field_validator("details")
    @classmethod
    def normalize_details(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None


class ConferenceMatchReportPublic(ContractModel):
    id: uuid.UUID
    match_id: uuid.UUID
    reason: ConferenceMatchReportReason
    status: ConferenceMatchReportStatus
    resolution_summary: str | None
    row_version: int = Field(ge=1)
    created_at: datetime
    resolved_at: datetime | None


class ConferenceMatchReportDecision(ContractModel):
    decision: MatchReportDecision
    reviewer_reference: str = Field(min_length=3, max_length=80)
    resolution_summary: str = Field(min_length=1, max_length=500)
    row_version: int = Field(ge=1)

    @field_validator("reviewer_reference", "resolution_summary")
    @classmethod
    def normalize_required_text(cls, value: str) -> str:
        normalized = " ".join(value.strip().split())
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class ConferenceMatchReportInternalPublic(ConferenceMatchReportPublic):
    reporter_user_id: uuid.UUID
    reported_user_id: uuid.UUID
    report_details: str | None


class ConferenceMatchReportListPublic(ContractModel):
    items: list[ConferenceMatchReportInternalPublic]
