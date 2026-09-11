"""Add video metadata to media assets.

Revision ID: 0026_video_media
Revises: 0025_conference_likes
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op


revision: str = "0026_video_media"
down_revision: str | None = "0025_conference_likes"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    op.add_column("media_assets", sa.Column("duration_ms", sa.Integer(), nullable=True))


def downgrade() -> None:
    op.drop_column("media_assets", "duration_ms")
