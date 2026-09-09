from __future__ import annotations

import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    CheckConstraint,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Index,
    Integer,
    JSON,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.core.security import utcnow
from app.db import Base
from app.models import AgeBand


class ConferenceReviewTemplate(str, enum.Enum):
    OBSERVATION = "OBSERVATION"
    LEARNING = "LEARNING"
    EVIDENCE = "EVIDENCE"
    SUGGESTION = "SUGGESTION"
    QUESTION = "QUESTION"


class ConferenceReviewStatus(str, enum.Enum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    THINKING = "THINKING"
    REPLIED = "REPLIED"


class ConferenceReviewModerationStatus(str, enum.Enum):
    VISIBLE = "VISIBLE"
    REMOVED = "REMOVED"


class ConferenceReviewReportStatus(str, enum.Enum):
    PENDING = "PENDING"
    RESOLVED = "RESOLVED"
    DISMISSED = "DISMISSED"


class ConferenceReviewReportReason(str, enum.Enum):
    PERSONAL_INFO = "PERSONAL_INFO"
    HARASSMENT = "HARASSMENT"
    INAPPROPRIATE_CONTENT = "INAPPROPRIATE_CONTENT"
    OTHER = "OTHER"


class DerivativeRequestStatus(str, enum.Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"


class DerivativeAuthorizationStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    REVOKED = "REVOKED"


class ConferenceMatchQueueStatus(str, enum.Enum):
    WAITING = "WAITING"
    MATCHED = "MATCHED"
    EXITED = "EXITED"


class ConferenceMatchStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    AWAITING_JUDGMENT = "AWAITING_JUDGMENT"
    ENDED = "ENDED"


class ConferenceMatchEndReason(str, enum.Enum):
    COMPLETED = "COMPLETED"
    EXITED = "EXITED"
    REPORTED = "REPORTED"


class ConferenceMatchQuestionKind(str, enum.Enum):
    CORE_LOGIC = "CORE_LOGIC"
    CASE_ANALYSIS = "CASE_ANALYSIS"
    TRANSFER = "TRANSFER"


class ConferenceMatchEvaluationKind(str, enum.Enum):
    AI = "AI"
    SELF = "SELF"
    PEER = "PEER"
    TEACHER = "TEACHER"


class ConferenceLetterCategory(str, enum.Enum):
    REVIEW = "REVIEW"
    DERIVATIVE = "DERIVATIVE"
    MATCH = "MATCH"
    SYSTEM = "SYSTEM"


class ConferenceMatchReportReason(str, enum.Enum):
    INAPPROPRIATE_CONTENT = "INAPPROPRIATE_CONTENT"
    HARASSMENT = "HARASSMENT"
    SAFETY_CONCERN = "SAFETY_CONCERN"
    OTHER = "OTHER"


class ConferenceMatchReportStatus(str, enum.Enum):
    PENDING = "PENDING"
    RESOLVED = "RESOLVED"
    DISMISSED = "DISMISSED"


class ConferenceReview(Base):
    __tablename__ = "conference_reviews"
    __table_args__ = (
        UniqueConstraint(
            "publication_id",
            "reviewer_user_id",
            "template",
            name="uq_conference_reviews_publication_reviewer_template",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="CASCADE"), index=True
    )
    reviewer_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    template: Mapped[ConferenceReviewTemplate] = mapped_column(
        Enum(ConferenceReviewTemplate, native_enum=False, length=16), nullable=False
    )
    content: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[ConferenceReviewStatus] = mapped_column(
        Enum(ConferenceReviewStatus, native_enum=False, length=16),
        default=ConferenceReviewStatus.PENDING,
        nullable=False,
    )
    moderation_status: Mapped[ConferenceReviewModerationStatus] = mapped_column(
        Enum(ConferenceReviewModerationStatus, native_enum=False, length=16),
        default=ConferenceReviewModerationStatus.VISIBLE,
        nullable=False,
    )
    author_reply: Mapped[str | None] = mapped_column(Text)
    handled_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    adopted_in_creation_version_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="SET NULL"), index=True
    )
    adoption_summary: Mapped[str | None] = mapped_column(String(500))
    adopted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    row_version: Mapped[int] = mapped_column(default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ConferenceReviewReport(Base):
    __tablename__ = "conference_review_reports"
    __table_args__ = (
        UniqueConstraint(
            "review_id",
            "reporter_user_id",
            name="uq_conference_review_reports_review_reporter",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    review_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_reviews.id", ondelete="CASCADE"), index=True
    )
    reporter_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    reason: Mapped[ConferenceReviewReportReason] = mapped_column(
        Enum(ConferenceReviewReportReason, native_enum=False, length=24), nullable=False
    )
    details: Mapped[str | None] = mapped_column(Text)
    status: Mapped[ConferenceReviewReportStatus] = mapped_column(
        Enum(ConferenceReviewReportStatus, native_enum=False, length=16),
        default=ConferenceReviewReportStatus.PENDING,
        nullable=False,
    )
    reviewer_reference: Mapped[str | None] = mapped_column(String(80))
    resolution_summary: Mapped[str | None] = mapped_column(String(500))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    resolved_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ConferenceCollection(Base):
    __tablename__ = "conference_collections"
    __table_args__ = (
        UniqueConstraint(
            "user_id", "publication_id", name="uq_conference_collections_user_publication"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="CASCADE"), index=True
    )
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ConferenceDerivativeRequest(Base):
    __tablename__ = "conference_derivative_requests"
    __table_args__ = (
        UniqueConstraint(
            "requester_user_id",
            "source_publication_id",
            name="uq_conference_derivative_request_requester_source",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    source_publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="RESTRICT"), index=True
    )
    source_creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), index=True
    )
    source_author_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    requester_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    source_title: Mapped[str] = mapped_column(String(100), nullable=False)
    source_author_nickname: Mapped[str] = mapped_column(String(40), nullable=False)
    requested_use: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[DerivativeRequestStatus] = mapped_column(
        Enum(DerivativeRequestStatus, native_enum=False, length=16),
        default=DerivativeRequestStatus.PENDING,
        nullable=False,
    )
    author_decision_note: Mapped[str | None] = mapped_column(Text)
    decided_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ConferenceDerivativeAuthorization(Base):
    __tablename__ = "conference_derivative_authorizations"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    request_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_derivative_requests.id", ondelete="CASCADE"),
        unique=True,
        index=True,
    )
    source_publication_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("publications.id", ondelete="RESTRICT"), index=True
    )
    source_creation_version_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("creation_versions.id", ondelete="RESTRICT"), index=True
    )
    source_author_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    requester_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    authorization_version: Mapped[str] = mapped_column(String(32), nullable=False)
    status: Mapped[DerivativeAuthorizationStatus] = mapped_column(
        Enum(DerivativeAuthorizationStatus, native_enum=False, length=16),
        default=DerivativeAuthorizationStatus.ACTIVE,
        nullable=False,
    )
    granted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ConferenceMatch(Base):
    __tablename__ = "conference_matches"
    __table_args__ = (
        CheckConstraint("question_count BETWEEN 3 AND 5", name="question_count_range"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="RESTRICT"), index=True
    )
    age_band: Mapped[AgeBand] = mapped_column(
        Enum(AgeBand, native_enum=False, length=20), nullable=False
    )
    participant_a_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    participant_b_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    status: Mapped[ConferenceMatchStatus] = mapped_column(
        Enum(ConferenceMatchStatus, native_enum=False, length=24),
        default=ConferenceMatchStatus.ACTIVE,
        nullable=False,
    )
    end_reason: Mapped[ConferenceMatchEndReason | None] = mapped_column(
        Enum(ConferenceMatchEndReason, native_enum=False, length=16)
    )
    winner_user_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("users.id", ondelete="SET NULL"), index=True
    )
    question_count: Mapped[int] = mapped_column(Integer, default=3, nullable=False)
    matched_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    ended_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ConferenceMatchQuestion(Base):
    __tablename__ = "conference_match_questions"
    __table_args__ = (
        UniqueConstraint(
            "match_id", "position", name="uq_conference_match_questions_match_position"
        ),
        CheckConstraint("position BETWEEN 1 AND 5", name="position_range"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    match_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="CASCADE"), index=True
    )
    position: Mapped[int] = mapped_column(Integer, nullable=False)
    kind: Mapped[ConferenceMatchQuestionKind] = mapped_column(
        Enum(ConferenceMatchQuestionKind, native_enum=False, length=20), nullable=False
    )
    prompt: Mapped[str] = mapped_column(Text, nullable=False)
    rubric: Mapped[str] = mapped_column(Text, nullable=False)
    content_version: Mapped[str] = mapped_column(String(32), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ConferenceMatchAnswer(Base):
    __tablename__ = "conference_match_answers"
    __table_args__ = (
        UniqueConstraint(
            "question_id",
            "participant_user_id",
            name="uq_conference_match_answers_question_participant",
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    match_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="CASCADE"), index=True
    )
    question_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_match_questions.id", ondelete="CASCADE"), index=True
    )
    participant_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    answer: Mapped[str] = mapped_column(Text, nullable=False)
    reason: Mapped[str] = mapped_column(Text, nullable=False)
    submitted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ConferenceMatchEvaluation(Base):
    __tablename__ = "conference_match_evaluations"
    __table_args__ = (
        UniqueConstraint(
            "match_id",
            "subject_user_id",
            "kind",
            name="uq_conference_match_evaluations_match_subject_kind",
        ),
        CheckConstraint("score >= 0 AND score <= 100", name="score_range"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    match_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="CASCADE"), index=True
    )
    subject_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    evaluator_user_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("users.id", ondelete="SET NULL"), index=True
    )
    kind: Mapped[ConferenceMatchEvaluationKind] = mapped_column(
        Enum(ConferenceMatchEvaluationKind, native_enum=False, length=16), nullable=False
    )
    score: Mapped[float] = mapped_column(Float, nullable=False)
    dimension_scores: Mapped[dict] = mapped_column(JSON, nullable=False)
    summary: Mapped[str] = mapped_column(Text, nullable=False)
    strengths: Mapped[list[str]] = mapped_column(JSON, nullable=False)
    improvements: Mapped[list[str]] = mapped_column(JSON, nullable=False)
    evaluator_reference: Mapped[str | None] = mapped_column(String(120))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ConferenceMatchReflection(Base):
    __tablename__ = "conference_match_reflections"
    __table_args__ = (
        UniqueConstraint(
            "match_id", "user_id", name="uq_conference_match_reflections_match_user"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    match_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="CASCADE"), index=True
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    learned: Mapped[str] = mapped_column(Text, nullable=False)
    next_improvement: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


class ConferenceMatchQueue(Base):
    __tablename__ = "conference_match_queue"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    manual_page_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("manual_pages.id", ondelete="RESTRICT"), index=True
    )
    age_band: Mapped[AgeBand] = mapped_column(
        Enum(AgeBand, native_enum=False, length=20), nullable=False
    )
    status: Mapped[ConferenceMatchQueueStatus] = mapped_column(
        Enum(ConferenceMatchQueueStatus, native_enum=False, length=16), nullable=False
    )
    match_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="SET NULL"), index=True
    )
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    expires_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow, onupdate=utcnow
    )


class ConferenceMatchReport(Base):
    __tablename__ = "conference_match_reports"
    __table_args__ = (
        UniqueConstraint(
            "match_id", "reporter_user_id", name="uq_conference_match_reports_match_reporter"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    match_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("conference_matches.id", ondelete="CASCADE"), index=True
    )
    reporter_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    reported_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="RESTRICT"), index=True
    )
    reason: Mapped[ConferenceMatchReportReason] = mapped_column(
        Enum(ConferenceMatchReportReason, native_enum=False, length=24), nullable=False
    )
    details: Mapped[str | None] = mapped_column(Text)
    status: Mapped[ConferenceMatchReportStatus] = mapped_column(
        Enum(ConferenceMatchReportStatus, native_enum=False, length=16),
        default=ConferenceMatchReportStatus.PENDING,
        nullable=False,
    )
    reviewer_reference: Mapped[str | None] = mapped_column(String(80))
    resolution_summary: Mapped[str | None] = mapped_column(String(500))
    row_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)
    resolved_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class ConferenceLetter(Base):
    __tablename__ = "conference_letters"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    recipient_user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    category: Mapped[ConferenceLetterCategory] = mapped_column(
        Enum(ConferenceLetterCategory, native_enum=False, length=16), nullable=False
    )
    title: Mapped[str] = mapped_column(String(80), nullable=False)
    body: Mapped[str] = mapped_column(String(500), nullable=False)
    action_type: Mapped[str | None] = mapped_column(String(40))
    action_id: Mapped[uuid.UUID | None] = mapped_column()
    read_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utcnow)


Index(
    "ix_conference_reviews_publication_status_created",
    ConferenceReview.publication_id,
    ConferenceReview.status,
    ConferenceReview.created_at,
)
Index(
    "ix_conference_reviews_publication_moderation_created",
    ConferenceReview.publication_id,
    ConferenceReview.moderation_status,
    ConferenceReview.created_at,
)
Index(
    "ix_conference_review_reports_status_created",
    ConferenceReviewReport.status,
    ConferenceReviewReport.created_at,
)
Index(
    "ix_conference_collections_user_created",
    ConferenceCollection.user_id,
    ConferenceCollection.created_at,
)
Index(
    "ix_conference_derivative_requests_author_status",
    ConferenceDerivativeRequest.source_author_user_id,
    ConferenceDerivativeRequest.status,
)
Index(
    "ix_conference_match_queue_scope_status_joined",
    ConferenceMatchQueue.manual_page_id,
    ConferenceMatchQueue.age_band,
    ConferenceMatchQueue.status,
    ConferenceMatchQueue.joined_at,
)
Index(
    "ix_conference_match_queue_user_status_joined",
    ConferenceMatchQueue.user_id,
    ConferenceMatchQueue.status,
    ConferenceMatchQueue.joined_at,
)
Index(
    "ix_conference_match_answers_match_participant",
    ConferenceMatchAnswer.match_id,
    ConferenceMatchAnswer.participant_user_id,
)
Index(
    "ix_conference_match_evaluations_match_subject",
    ConferenceMatchEvaluation.match_id,
    ConferenceMatchEvaluation.subject_user_id,
)
Index(
    "ix_conference_match_reports_status_created",
    ConferenceMatchReport.status,
    ConferenceMatchReport.created_at,
)
Index(
    "ix_conference_letters_recipient_read_created",
    ConferenceLetter.recipient_user_id,
    ConferenceLetter.read_at,
    ConferenceLetter.created_at,
)
Index(
    "ix_conference_derivative_authorizations_source_status",
    ConferenceDerivativeAuthorization.source_publication_id,
    ConferenceDerivativeAuthorization.status,
)
