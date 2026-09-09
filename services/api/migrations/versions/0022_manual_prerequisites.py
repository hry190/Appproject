"""Add versioned prerequisite edges for the 后山 route planner.

Revision ID: 0022_manual_prerequisites
Revises: 0021_conference_feedback_closure
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0022_manual_prerequisites"
down_revision: str | None = "0021_conference_feedback_closure"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "manual_prerequisites",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("prerequisite_page_id", sa.Uuid(), nullable=False),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "manual_page_id <> prerequisite_page_id",
            name="manual_prerequisite_not_self",
        ),
        sa.ForeignKeyConstraint(
            ["manual_page_id"], ["manual_pages.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["prerequisite_page_id"], ["manual_pages.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "manual_page_id",
            "prerequisite_page_id",
            name="uq_manual_prerequisites_edge",
        ),
    )
    op.create_index(
        "ix_manual_prerequisites_manual_page_id",
        "manual_prerequisites",
        ["manual_page_id"],
    )
    op.create_index(
        "ix_manual_prerequisites_prerequisite_page_id",
        "manual_prerequisites",
        ["prerequisite_page_id"],
    )


def downgrade() -> None:
    op.drop_index(
        "ix_manual_prerequisites_prerequisite_page_id",
        table_name="manual_prerequisites",
    )
    op.drop_index(
        "ix_manual_prerequisites_manual_page_id",
        table_name="manual_prerequisites",
    )
    op.drop_table("manual_prerequisites")
