"""Add mistake aggregation, retry contexts, and remediation records.

Revision ID: 0005_mistakes_and_remediation
Revises: 0004_learning_events_and_progress
"""
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0005_mistakes_and_remediation"
down_revision: str | None = "0004_learning_events_and_progress"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    mistake_status = sa.Enum(
        "TO_REVIEW",
        "PRACTICING",
        "CONSOLIDATED",
        name="mistakestatus",
        native_enum=False,
        length=16,
    )
    attempt_result = sa.Enum(
        "PASSED",
        "FAILED",
        name="trialattemptresult",
        native_enum=False,
        length=16,
    )

    op.create_table(
        "mistake_items",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("trial_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("knowledge_point_code", sa.String(length=80), nullable=False),
        sa.Column("first_attempt_id", sa.Uuid(), nullable=False),
        sa.Column("latest_attempt_id", sa.Uuid(), nullable=False),
        sa.Column("original_answer_payload", sa.JSON(), nullable=False),
        sa.Column("error_reason_code", sa.String(length=80), nullable=False),
        sa.Column("error_reason_summary", sa.String(length=240), nullable=False),
        sa.Column("status", mistake_status, nullable=False),
        sa.Column("failure_count", sa.Integer(), nullable=False),
        sa.Column("successful_retries", sa.Integer(), nullable=False),
        sa.Column("next_review_at", sa.DateTime(timezone=True)),
        sa.Column("consolidated_at", sa.DateTime(timezone=True)),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "failure_count >= 1",
            name=op.f("ck_mistake_items_failure_count_positive"),
        ),
        sa.CheckConstraint(
            "successful_retries >= 0",
            name=op.f("ck_mistake_items_successful_retries_non_negative"),
        ),
        sa.ForeignKeyConstraint(
            ["first_attempt_id"],
            ["trial_attempts.id"],
            name=op.f("fk_mistake_items_first_attempt_id_trial_attempts"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["latest_attempt_id"],
            ["trial_attempts.id"],
            name=op.f("fk_mistake_items_latest_attempt_id_trial_attempts"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_mistake_items_manual_page_id_manual_pages"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["trial_id"],
            ["trials.id"],
            name=op.f("fk_mistake_items_trial_id_trials"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_mistake_items_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_mistake_items")),
        sa.UniqueConstraint(
            "user_id",
            "trial_id",
            "knowledge_point_code",
            name="uq_mistake_items_user_trial_knowledge",
        ),
    )
    for column in (
        "first_attempt_id",
        "latest_attempt_id",
        "manual_page_id",
        "trial_id",
        "user_id",
    ):
        op.create_index(
            op.f(f"ix_mistake_items_{column}"),
            "mistake_items",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_mistake_items_user_status_updated",
        "mistake_items",
        ["user_id", "status", "updated_at"],
        unique=False,
    )

    op.create_table(
        "remediation_contexts",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("mistake_id", sa.Uuid(), nullable=False),
        sa.Column("trial_version_id", sa.Uuid(), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("used_at", sa.DateTime(timezone=True)),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["mistake_id"],
            ["mistake_items.id"],
            name=op.f("fk_remediation_contexts_mistake_id_mistake_items"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["trial_version_id"],
            ["trial_versions.id"],
            name=op.f("fk_remediation_contexts_trial_version_id_trial_versions"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_remediation_contexts_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_remediation_contexts")),
    )
    for column in ("mistake_id", "trial_version_id", "user_id"):
        op.create_index(
            op.f(f"ix_remediation_contexts_{column}"),
            "remediation_contexts",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_remediation_contexts_user_expires",
        "remediation_contexts",
        ["user_id", "expires_at"],
        unique=False,
    )

    op.create_table(
        "remediation_records",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("mistake_id", sa.Uuid(), nullable=False),
        sa.Column("attempt_id", sa.Uuid(), nullable=False),
        sa.Column("result", attempt_result, nullable=False),
        sa.Column("reflection", sa.String(length=1000)),
        sa.Column("occurred_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["attempt_id"],
            ["trial_attempts.id"],
            name=op.f("fk_remediation_records_attempt_id_trial_attempts"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["mistake_id"],
            ["mistake_items.id"],
            name=op.f("fk_remediation_records_mistake_id_mistake_items"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_remediation_records")),
    )
    op.create_index(
        op.f("ix_remediation_records_attempt_id"),
        "remediation_records",
        ["attempt_id"],
        unique=True,
    )
    op.create_index(
        op.f("ix_remediation_records_mistake_id"),
        "remediation_records",
        ["mistake_id"],
        unique=False,
    )
    op.create_index(
        "ix_remediation_records_mistake_occurred",
        "remediation_records",
        ["mistake_id", "occurred_at"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_table("remediation_records")
    op.drop_table("remediation_contexts")
    op.drop_table("mistake_items")
