"""Close conference review adoption and match-report moderation loops.

Revision ID: 0021_conference_feedback_closure
Revises: 0020_conference_match_learning_loop
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0021_conference_feedback_closure"
down_revision: str | None = "0020_conference_match_learning_loop"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


match_report_status = sa.Enum(
    "PENDING",
    "RESOLVED",
    "DISMISSED",
    name="conferencematchreportstatus",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    with op.batch_alter_table("conference_reviews") as batch_op:
        batch_op.add_column(
            sa.Column("adopted_in_creation_version_id", sa.Uuid(), nullable=True)
        )
        batch_op.add_column(
            sa.Column("adoption_summary", sa.String(length=500), nullable=True)
        )
        batch_op.add_column(
            sa.Column("adopted_at", sa.DateTime(timezone=True), nullable=True)
        )
        batch_op.create_foreign_key(
            op.f(
                "fk_conference_reviews_adopted_in_creation_version_id_creation_versions"
            ),
            "creation_versions",
            ["adopted_in_creation_version_id"],
            ["id"],
            ondelete="SET NULL",
        )
        batch_op.create_index(
            op.f("ix_conference_reviews_adopted_in_creation_version_id"),
            ["adopted_in_creation_version_id"],
        )

    with op.batch_alter_table("conference_match_reports") as batch_op:
        batch_op.add_column(
            sa.Column(
                "status",
                match_report_status,
                server_default="PENDING",
                nullable=False,
            )
        )
        batch_op.add_column(
            sa.Column("reviewer_reference", sa.String(length=80), nullable=True)
        )
        batch_op.add_column(
            sa.Column("resolution_summary", sa.String(length=500), nullable=True)
        )
        batch_op.add_column(
            sa.Column("row_version", sa.Integer(), server_default="1", nullable=False)
        )
        batch_op.add_column(
            sa.Column("resolved_at", sa.DateTime(timezone=True), nullable=True)
        )
        batch_op.alter_column("status", server_default=None)
        batch_op.alter_column("row_version", server_default=None)
        batch_op.create_index(
            "ix_conference_match_reports_status_created",
            ["status", "created_at"],
        )


def downgrade() -> None:
    with op.batch_alter_table("conference_match_reports") as batch_op:
        batch_op.drop_index("ix_conference_match_reports_status_created")
        batch_op.drop_column("resolved_at")
        batch_op.drop_column("row_version")
        batch_op.drop_column("resolution_summary")
        batch_op.drop_column("reviewer_reference")
        batch_op.drop_column("status")

    with op.batch_alter_table("conference_reviews") as batch_op:
        batch_op.drop_index(
            op.f("ix_conference_reviews_adopted_in_creation_version_id")
        )
        batch_op.drop_constraint(
            op.f(
                "fk_conference_reviews_adopted_in_creation_version_id_creation_versions"
            ),
            type_="foreignkey",
        )
        batch_op.drop_column("adopted_at")
        batch_op.drop_column("adoption_summary")
        batch_op.drop_column("adopted_in_creation_version_id")
