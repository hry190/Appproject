"""Add idempotent likes for conference works.

Revision ID: 0025_conference_likes
Revises: 0024_conference_work_categories
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op


revision: str = "0025_conference_likes"
down_revision: str | None = "0024_conference_work_categories"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    op.create_table(
        "conference_likes",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("publication_id", sa.Uuid(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["publication_id"], ["publications.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "user_id",
            "publication_id",
            name="uq_conference_likes_user_publication",
        ),
    )
    op.create_index(
        op.f("ix_conference_likes_user_id"),
        "conference_likes",
        ["user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_conference_likes_publication_id"),
        "conference_likes",
        ["publication_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_conference_likes_publication_id"), table_name="conference_likes")
    op.drop_index(op.f("ix_conference_likes_user_id"), table_name="conference_likes")
    op.drop_table("conference_likes")
