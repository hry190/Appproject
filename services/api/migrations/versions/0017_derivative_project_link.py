"""Link derivative projects to their approved authorization.

Revision ID: 0017_derivative_project_link
Revises: 0016_conference
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0017_derivative_project_link"
down_revision: str | None = "0016_conference"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "creation_projects",
        sa.Column("derivative_authorization_id", sa.Uuid(), nullable=True),
    )
    op.create_foreign_key(
        "fk_creation_projects_derivative_authorization_id",
        "creation_projects",
        "conference_derivative_authorizations",
        ["derivative_authorization_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        op.f("ix_creation_projects_derivative_authorization_id"),
        "creation_projects",
        ["derivative_authorization_id"],
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_creation_projects_derivative_authorization_id"), table_name="creation_projects")
    op.drop_constraint("fk_creation_projects_derivative_authorization_id", "creation_projects", type_="foreignkey")
    op.drop_column("creation_projects", "derivative_authorization_id")
