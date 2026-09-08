"""Persist verified sketch and learned-manual creation sources.

Revision ID: 0010_creation_sources
Revises: 0009_creation_workflow
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0010_creation_sources"
down_revision: str | None = "0009_creation_workflow"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    with op.batch_alter_table("creation_intents") as batch_op:
        batch_op.add_column(
            sa.Column(
                "attachment_asset_ids",
                sa.JSON(),
                nullable=False,
                server_default="[]",
            )
        )
        batch_op.add_column(
            sa.Column(
                "manual_page_ids",
                sa.JSON(),
                nullable=False,
                server_default="[]",
            )
        )
    with op.batch_alter_table("creation_methods") as batch_op:
        batch_op.add_column(
            sa.Column(
                "source_asset_ids",
                sa.JSON(),
                nullable=False,
                server_default="[]",
            )
        )
        batch_op.add_column(
            sa.Column(
                "manual_page_ids",
                sa.JSON(),
                nullable=False,
                server_default="[]",
            )
        )


def downgrade() -> None:
    with op.batch_alter_table("creation_methods") as batch_op:
        batch_op.drop_column("manual_page_ids")
        batch_op.drop_column("source_asset_ids")
    with op.batch_alter_table("creation_intents") as batch_op:
        batch_op.drop_column("manual_page_ids")
        batch_op.drop_column("attachment_asset_ids")
