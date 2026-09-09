"""Add the version-pinned creation seal self-check.

Revision ID: 0012_creation_seal_check
Revises: 0011_creation_tools_tests
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0012_creation_seal_check"
down_revision: str | None = "0011_creation_tools_tests"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


seal_status = sa.Enum(
    "DRAFT",
    "COMPLETE",
    "LOCKED",
    name="creationsealstatus",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    op.create_table(
        "creation_seal_checks",
        sa.Column("creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("work_description", sa.Text(), nullable=False),
        sa.Column("learning_reflection", sa.Text(), nullable=False),
        sa.Column("next_improvement", sa.Text(), nullable=False),
        sa.Column("identity_privacy_confirmed", sa.Boolean(), nullable=False),
        sa.Column("contact_privacy_confirmed", sa.Boolean(), nullable=False),
        sa.Column("portrait_rights_confirmed", sa.Boolean(), nullable=False),
        sa.Column("status", seal_status, nullable=False),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("locked_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["creation_version_id"],
            ["creation_versions.id"],
            name=op.f(
                "fk_creation_seal_checks_creation_version_id_creation_versions"
            ),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint(
            "creation_version_id",
            name=op.f("pk_creation_seal_checks"),
        ),
    )


def downgrade() -> None:
    op.drop_table("creation_seal_checks")
