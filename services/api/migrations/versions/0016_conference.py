"""Add conference interactions for community publications.

Revision ID: 0016_conference
Revises: 0015_publication_distribution
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0016_conference"
down_revision: str | None = "0015_publication_distribution"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


review_template = sa.Enum(
    "OBSERVATION", "LEARNING", "EVIDENCE", "QUESTION",
    name="conferencereviewtemplate", native_enum=False, length=16,
)
review_status = sa.Enum(
    "PENDING", "ACCEPTED", "THINKING", "REPLIED",
    name="conferencereviewstatus", native_enum=False, length=16,
)
derivative_request_status = sa.Enum(
    "PENDING", "APPROVED", "REJECTED",
    name="derivativerequeststatus", native_enum=False, length=16,
)
derivative_authorization_status = sa.Enum(
    "ACTIVE", "REVOKED",
    name="derivativeauthorizationstatus", native_enum=False, length=16,
)
match_queue_status = sa.Enum(
    "WAITING", "MATCHED", "EXITED",
    name="conferencematchqueuestatus", native_enum=False, length=16,
)
match_status = sa.Enum(
    "ACTIVE", "ENDED",
    name="conferencematchstatus", native_enum=False, length=16,
)
match_report_reason = sa.Enum(
    "INAPPROPRIATE_CONTENT", "HARASSMENT", "SAFETY_CONCERN", "OTHER",
    name="conferencematchreportreason", native_enum=False, length=24,
)
age_band = sa.Enum(
    "UNDER_14", "AGE_14_TO_17", "ADULT",
    name="ageband", native_enum=False, length=20,
)


def upgrade() -> None:
    op.create_table(
        "conference_reviews",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("publication_id", sa.Uuid(), nullable=False),
        sa.Column("reviewer_user_id", sa.Uuid(), nullable=False),
        sa.Column("template", review_template, nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("status", review_status, nullable=False),
        sa.Column("author_reply", sa.Text(), nullable=True),
        sa.Column("handled_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["publication_id"], ["publications.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["reviewer_user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("publication_id", "reviewer_user_id", "template", name="uq_conference_reviews_publication_reviewer_template"),
    )
    op.create_index(op.f("ix_conference_reviews_publication_id"), "conference_reviews", ["publication_id"])
    op.create_index(op.f("ix_conference_reviews_reviewer_user_id"), "conference_reviews", ["reviewer_user_id"])
    op.create_index("ix_conference_reviews_publication_status_created", "conference_reviews", ["publication_id", "status", "created_at"])

    op.create_table(
        "conference_collections",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("publication_id", sa.Uuid(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["publication_id"], ["publications.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("user_id", "publication_id", name="uq_conference_collections_user_publication"),
    )
    op.create_index(op.f("ix_conference_collections_user_id"), "conference_collections", ["user_id"])
    op.create_index(op.f("ix_conference_collections_publication_id"), "conference_collections", ["publication_id"])
    op.create_index("ix_conference_collections_user_created", "conference_collections", ["user_id", "created_at"])

    op.create_table(
        "conference_derivative_requests",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("source_publication_id", sa.Uuid(), nullable=False),
        sa.Column("source_creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("source_author_user_id", sa.Uuid(), nullable=False),
        sa.Column("requester_user_id", sa.Uuid(), nullable=False),
        sa.Column("source_title", sa.String(length=100), nullable=False),
        sa.Column("source_author_nickname", sa.String(length=40), nullable=False),
        sa.Column("requested_use", sa.Text(), nullable=False),
        sa.Column("status", derivative_request_status, nullable=False),
        sa.Column("author_decision_note", sa.Text(), nullable=True),
        sa.Column("decided_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["source_publication_id"], ["publications.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["source_creation_version_id"], ["creation_versions.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["source_author_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["requester_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("requester_user_id", "source_publication_id", name="uq_conference_derivative_request_requester_source"),
    )
    for column in ("source_publication_id", "source_creation_version_id", "source_author_user_id", "requester_user_id"):
        op.create_index(op.f(f"ix_conference_derivative_requests_{column}"), "conference_derivative_requests", [column])
    op.create_index("ix_conference_derivative_requests_author_status", "conference_derivative_requests", ["source_author_user_id", "status"])

    op.create_table(
        "conference_derivative_authorizations",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("request_id", sa.Uuid(), nullable=False),
        sa.Column("source_publication_id", sa.Uuid(), nullable=False),
        sa.Column("source_creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("source_author_user_id", sa.Uuid(), nullable=False),
        sa.Column("requester_user_id", sa.Uuid(), nullable=False),
        sa.Column("authorization_version", sa.String(length=32), nullable=False),
        sa.Column("status", derivative_authorization_status, nullable=False),
        sa.Column("granted_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["request_id"], ["conference_derivative_requests.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["source_publication_id"], ["publications.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["source_creation_version_id"], ["creation_versions.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["source_author_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["requester_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.PrimaryKeyConstraint("id"),
    )
    for column in ("request_id", "source_publication_id", "source_creation_version_id", "source_author_user_id", "requester_user_id"):
        op.create_index(op.f(f"ix_conference_derivative_authorizations_{column}"), "conference_derivative_authorizations", [column], unique=column == "request_id")

    op.create_table(
        "conference_matches",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("age_band", age_band, nullable=False),
        sa.Column("participant_a_user_id", sa.Uuid(), nullable=False),
        sa.Column("participant_b_user_id", sa.Uuid(), nullable=False),
        sa.Column("status", match_status, nullable=False),
        sa.Column("matched_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("ended_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["manual_page_id"], ["manual_pages.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["participant_a_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["participant_b_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.PrimaryKeyConstraint("id"),
    )
    for column in ("manual_page_id", "participant_a_user_id", "participant_b_user_id"):
        op.create_index(op.f(f"ix_conference_matches_{column}"), "conference_matches", [column])

    op.create_table(
        "conference_match_queue",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("age_band", age_band, nullable=False),
        sa.Column("status", match_queue_status, nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=True),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["manual_page_id"], ["manual_pages.id"], ondelete="RESTRICT"),
        sa.ForeignKeyConstraint(["match_id"], ["conference_matches.id"], ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id"),
    )
    for column in ("user_id", "manual_page_id", "match_id"):
        op.create_index(op.f(f"ix_conference_match_queue_{column}"), "conference_match_queue", [column])
    op.create_index("ix_conference_match_queue_scope_status_joined", "conference_match_queue", ["manual_page_id", "age_band", "status", "joined_at"])

    op.create_table(
        "conference_match_reports",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=False),
        sa.Column("reporter_user_id", sa.Uuid(), nullable=False),
        sa.Column("reported_user_id", sa.Uuid(), nullable=False),
        sa.Column("reason", match_report_reason, nullable=False),
        sa.Column("details", sa.Text(), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["match_id"], ["conference_matches.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["reporter_user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["reported_user_id"], ["users.id"], ondelete="RESTRICT"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("match_id", "reporter_user_id", name="uq_conference_match_reports_match_reporter"),
    )
    for column in ("match_id", "reporter_user_id", "reported_user_id"):
        op.create_index(op.f(f"ix_conference_match_reports_{column}"), "conference_match_reports", [column])


def downgrade() -> None:
    op.drop_table("conference_match_reports")
    op.drop_table("conference_match_queue")
    op.drop_table("conference_matches")
    op.drop_table("conference_derivative_authorizations")
    op.drop_table("conference_derivative_requests")
    op.drop_table("conference_collections")
    op.drop_table("conference_reviews")
