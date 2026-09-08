"""Add confirmed tool calls and version-pinned creation tests.

Revision ID: 0011_creation_tools_tests
Revises: 0010_creation_sources
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0011_creation_tools_tests"
down_revision: str | None = "0010_creation_sources"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


tool_kind = sa.Enum(
    "COACH_REVIEW",
    name="creationtoolkind",
    native_enum=False,
    length=24,
)
tool_status = sa.Enum(
    "PROPOSED",
    "COMPLETED",
    "REJECTED",
    "EXPIRED",
    "FAILED",
    name="creationtoolcallstatus",
    native_enum=False,
    length=16,
)
test_result = sa.Enum(
    "PASSED",
    "NEEDS_REVISION",
    "BLOCKED",
    name="creationtestresult",
    native_enum=False,
    length=24,
)
issue_severity = sa.Enum(
    "NOTE",
    "IMPORTANT",
    "BLOCKING",
    name="creationissueseverity",
    native_enum=False,
    length=16,
)
issue_status = sa.Enum(
    "OPEN",
    "RESOLVED",
    name="creationissuestatus",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    op.create_table(
        "creation_tool_calls",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("kind", tool_kind, nullable=False),
        sa.Column("status", tool_status, nullable=False),
        sa.Column("input_snapshot", sa.JSON(), nullable=False),
        sa.Column("prompt_summary", sa.String(length=500), nullable=False),
        sa.Column("effect_summary", sa.String(length=500), nullable=False),
        sa.Column("external_data_shared", sa.Boolean(), nullable=False),
        sa.Column("output_snapshot", sa.JSON(), nullable=True),
        sa.Column("executor_ref", sa.String(length=80), nullable=True),
        sa.Column("proposal_idempotency_key", sa.String(length=64), nullable=False),
        sa.Column("proposal_fingerprint", sa.String(length=64), nullable=False),
        sa.Column("decision_idempotency_key", sa.String(length=64), nullable=True),
        sa.Column("decision_fingerprint", sa.String(length=64), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("proposed_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("decided_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["creation_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_creation_tool_calls_creation_version_id_creation_versions"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_tool_calls_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_tool_calls_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_tool_calls")),
        sa.UniqueConstraint(
            "owner_user_id",
            "proposal_idempotency_key",
            name="uq_creation_tool_calls_owner_proposal_idempotency",
        ),
    )
    op.create_index(
        op.f("ix_creation_tool_calls_project_id"),
        "creation_tool_calls",
        ["project_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_tool_calls_creation_version_id"),
        "creation_tool_calls",
        ["creation_version_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_tool_calls_owner_user_id"),
        "creation_tool_calls",
        ["owner_user_id"],
        unique=False,
    )

    op.create_table(
        "creation_test_records",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("scenario", sa.String(length=500), nullable=False),
        sa.Column("result", test_result, nullable=False),
        sa.Column("notes", sa.Text(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["creation_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_creation_test_records_creation_version_id_creation_versions"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_test_records_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_test_records_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_test_records")),
    )
    op.create_index(
        op.f("ix_creation_test_records_project_id"),
        "creation_test_records",
        ["project_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_test_records_creation_version_id"),
        "creation_test_records",
        ["creation_version_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_test_records_owner_user_id"),
        "creation_test_records",
        ["owner_user_id"],
        unique=False,
    )

    op.create_table(
        "creation_test_issues",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("test_record_id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("severity", issue_severity, nullable=False),
        sa.Column("description", sa.String(length=500), nullable=False),
        sa.Column("status", issue_status, nullable=False),
        sa.Column("resolution_summary", sa.String(length=500), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("resolved_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_test_issues_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["test_record_id"],
            ["creation_test_records.id"],
            name=op.f("fk_creation_test_issues_test_record_id_creation_test_records"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_test_issues")),
    )
    op.create_index(
        op.f("ix_creation_test_issues_project_id"),
        "creation_test_issues",
        ["project_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_test_issues_test_record_id"),
        "creation_test_issues",
        ["test_record_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(
        op.f("ix_creation_test_issues_test_record_id"),
        table_name="creation_test_issues",
    )
    op.drop_index(
        op.f("ix_creation_test_issues_project_id"),
        table_name="creation_test_issues",
    )
    op.drop_table("creation_test_issues")
    op.drop_index(
        op.f("ix_creation_test_records_owner_user_id"),
        table_name="creation_test_records",
    )
    op.drop_index(
        op.f("ix_creation_test_records_creation_version_id"),
        table_name="creation_test_records",
    )
    op.drop_index(
        op.f("ix_creation_test_records_project_id"),
        table_name="creation_test_records",
    )
    op.drop_table("creation_test_records")
    op.drop_index(
        op.f("ix_creation_tool_calls_owner_user_id"),
        table_name="creation_tool_calls",
    )
    op.drop_index(
        op.f("ix_creation_tool_calls_creation_version_id"),
        table_name="creation_tool_calls",
    )
    op.drop_index(
        op.f("ix_creation_tool_calls_project_id"),
        table_name="creation_tool_calls",
    )
    op.drop_table("creation_tool_calls")
