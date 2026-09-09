"""Align historical varchar lengths and one generated constraint name.

Revision ID: 0019_schema_metadata_alignment
Revises: 0018_conference_write_safety
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0019_schema_metadata_alignment"
down_revision: str | None = "0018_conference_write_safety"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


_VARCHAR_EXPANSIONS: tuple[tuple[str, str, int, int], ...] = (
    ("users", "status", 7, 16),
    ("users", "age_band", 12, 20),
    ("users", "guardian_status", 12, 20),
    ("consent_records", "consent_type", 8, 16),
    ("consent_records", "subject", 8, 16),
    ("guardian_controls", "content_level", 8, 16),
    ("feedback_tickets", "category", 14, 24),
    ("feedback_tickets", "status", 10, 16),
    ("data_rights_requests", "request_type", 18, 24),
    ("data_rights_requests", "status", 10, 16),
)


def _replace_practice_days_constraint(target_name: str) -> None:
    bind = op.get_bind()
    current_name = None
    for constraint in sa.inspect(bind).get_check_constraints("user_learning_stats"):
        sqltext = "".join((constraint.get("sqltext") or "").lower().split())
        if "lifetime_practice_days>=0" in sqltext:
            current_name = constraint.get("name")
            break
    if current_name == target_name:
        return
    with op.batch_alter_table("user_learning_stats") as batch_op:
        if current_name:
            batch_op.drop_constraint(op.f(current_name), type_="check")
        batch_op.create_check_constraint(
            op.f(target_name), "lifetime_practice_days >= 0"
        )


def upgrade() -> None:
    for table_name, column_name, old_length, new_length in _VARCHAR_EXPANSIONS:
        with op.batch_alter_table(table_name) as batch_op:
            batch_op.alter_column(
                column_name,
                existing_type=sa.String(length=old_length),
                type_=sa.String(length=new_length),
                existing_nullable=False,
            )
    _replace_practice_days_constraint(
        "ck_user_learning_stats_practice_days_non_negative"
    )


def downgrade() -> None:
    _replace_practice_days_constraint(
        "ck_user_learning_stats_ck_user_learning_stats_practice__1140"
    )
    for table_name, column_name, old_length, new_length in reversed(_VARCHAR_EXPANSIONS):
        with op.batch_alter_table(table_name) as batch_op:
            batch_op.alter_column(
                column_name,
                existing_type=sa.String(length=new_length),
                type_=sa.String(length=old_length),
                existing_nullable=False,
            )
