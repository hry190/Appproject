"""Add the server-derived lifetime practice-day projection.

Revision ID: 0008_luggage_ui_contract
Revises: 0007_media_moderation_privacy
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0008_luggage_ui_contract"
down_revision: str | None = "0007_media_moderation_privacy"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "user_learning_stats",
        sa.Column(
            "lifetime_practice_days",
            sa.Integer(),
            nullable=False,
            server_default="0",
        ),
    )
    op.create_check_constraint(
        "ck_user_learning_stats_practice_days_non_negative",
        "user_learning_stats",
        "lifetime_practice_days >= 0",
    )

    bind = op.get_bind()
    if bind.dialect.name == "postgresql":
        op.execute(
            sa.text(
                """
                UPDATE user_learning_stats AS stats
                SET lifetime_practice_days = COALESCE((
                    SELECT COUNT(DISTINCT (sessions.qualified_at AT TIME ZONE
                        'Asia/Shanghai')::date)
                    FROM practice_sessions AS sessions
                    WHERE sessions.user_id = stats.user_id
                      AND sessions.qualified_at IS NOT NULL
                ), 0)
                """
            )
        )
    else:
        op.execute(
            sa.text(
                """
                UPDATE user_learning_stats
                SET lifetime_practice_days = COALESCE((
                    SELECT COUNT(DISTINCT date(datetime(
                        practice_sessions.qualified_at, '+8 hours'
                    )))
                    FROM practice_sessions
                    WHERE practice_sessions.user_id = user_learning_stats.user_id
                      AND practice_sessions.qualified_at IS NOT NULL
                ), 0)
                """
            )
        )

    op.alter_column(
        "user_learning_stats",
        "lifetime_practice_days",
        server_default=None,
    )


def downgrade() -> None:
    op.drop_constraint(
        "ck_user_learning_stats_practice_days_non_negative",
        "user_learning_stats",
        type_="check",
    )
    op.drop_column("user_learning_stats", "lifetime_practice_days")
