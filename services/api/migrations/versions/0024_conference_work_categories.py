"""Add author-selected categories for conference works.

Revision ID: 0024_conference_work_categories
Revises: 0023_creation_conversations
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op


revision: str = "0024_conference_work_categories"
down_revision: str | None = "0023_creation_conversations"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    op.add_column(
        "publications",
        sa.Column(
            "conference_category",
            sa.Enum(
                "ART",
                "SCIENCE",
                "MATH",
                "LANGUAGE",
                name="conferencecategory",
                native_enum=False,
                length=16,
            ),
            nullable=True,
        ),
    )
    op.create_index(
        op.f("ix_publications_conference_category"),
        "publications",
        ["conference_category"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_publications_conference_category"), table_name="publications")
    op.drop_column("publications", "conference_category")
