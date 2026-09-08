"""Add conference review reporting, audit-ready moderation, and queue expiry.

Revision ID: 0018_conference_write_safety
Revises: 0017_derivative_project_link
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0018_conference_write_safety"
down_revision: str | None = "0017_derivative_project_link"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


review_moderation_status = sa.Enum(
    "VISIBLE",
    "REMOVED",
    name="conferencereviewmoderationstatus",
    native_enum=False,
    length=16,
)
review_report_reason = sa.Enum(
    "PERSONAL_INFO",
    "HARASSMENT",
    "INAPPROPRIATE_CONTENT",
    "OTHER",
    name="conferencereviewreportreason",
    native_enum=False,
    length=24,
)
review_report_status = sa.Enum(
    "PENDING",
    "RESOLVED",
    "DISMISSED",
    name="conferencereviewreportstatus",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    op.add_column(
        "conference_reviews",
        sa.Column(
            "moderation_status",
            review_moderation_status,
            nullable=False,
            server_default="VISIBLE",
        ),
    )
    op.alter_column("conference_reviews", "moderation_status", server_default=None)
    op.create_index(
        "ix_conference_reviews_publication_moderation_created",
        "conference_reviews",
        ["publication_id", "moderation_status", "created_at"],
    )

    op.create_table(
        "conference_review_reports",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("review_id", sa.Uuid(), nullable=False),
        sa.Column("reporter_user_id", sa.Uuid(), nullable=False),
        sa.Column("reason", review_report_reason, nullable=False),
        sa.Column("details", sa.Text(), nullable=True),
        sa.Column("status", review_report_status, nullable=False),
        sa.Column("reviewer_reference", sa.String(length=80), nullable=True),
        sa.Column("resolution_summary", sa.String(length=500), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("resolved_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["review_id"], ["conference_reviews.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["reporter_user_id"], ["users.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "review_id",
            "reporter_user_id",
            name="uq_conference_review_reports_review_reporter",
        ),
    )
    op.create_index(
        op.f("ix_conference_review_reports_review_id"),
        "conference_review_reports",
        ["review_id"],
    )
    op.create_index(
        op.f("ix_conference_review_reports_reporter_user_id"),
        "conference_review_reports",
        ["reporter_user_id"],
    )
    op.create_index(
        "ix_conference_review_reports_status_created",
        "conference_review_reports",
        ["status", "created_at"],
    )

    op.add_column(
        "conference_match_queue",
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=True),
    )
    op.create_index(
        "ix_conference_match_queue_user_status_joined",
        "conference_match_queue",
        ["user_id", "status", "joined_at"],
    )
    op.create_index(
        "ix_conference_derivative_authorizations_source_status",
        "conference_derivative_authorizations",
        ["source_publication_id", "status"],
    )


def downgrade() -> None:
    op.drop_index(
        "ix_conference_derivative_authorizations_source_status",
        table_name="conference_derivative_authorizations",
    )
    op.drop_index(
        "ix_conference_match_queue_user_status_joined",
        table_name="conference_match_queue",
    )
    op.drop_column("conference_match_queue", "expires_at")

    op.drop_index(
        "ix_conference_review_reports_status_created",
        table_name="conference_review_reports",
    )
    op.drop_index(
        op.f("ix_conference_review_reports_reporter_user_id"),
        table_name="conference_review_reports",
    )
    op.drop_index(
        op.f("ix_conference_review_reports_review_id"),
        table_name="conference_review_reports",
    )
    op.drop_table("conference_review_reports")

    op.drop_index(
        "ix_conference_reviews_publication_moderation_created",
        table_name="conference_reviews",
    )
    op.drop_column("conference_reviews", "moderation_status")
