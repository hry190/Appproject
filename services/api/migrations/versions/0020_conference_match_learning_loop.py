"""Add conference match learning loop, evaluations, and letters.

Revision ID: 0020_conference_match_learning_loop
Revises: 0019_schema_metadata_alignment
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0020_conference_match_learning_loop"
down_revision: str | None = "0019_schema_metadata_alignment"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


match_status = sa.Enum(
    "ACTIVE",
    "AWAITING_JUDGMENT",
    "ENDED",
    name="conferencematchstatus",
    native_enum=False,
    length=24,
)
match_status_previous = sa.Enum(
    "ACTIVE",
    "ENDED",
    name="conferencematchstatus",
    native_enum=False,
    length=16,
)
match_end_reason = sa.Enum(
    "COMPLETED",
    "EXITED",
    "REPORTED",
    name="conferencematchendreason",
    native_enum=False,
    length=16,
)
question_kind = sa.Enum(
    "CORE_LOGIC",
    "CASE_ANALYSIS",
    "TRANSFER",
    name="conferencematchquestionkind",
    native_enum=False,
    length=20,
)
evaluation_kind = sa.Enum(
    "AI",
    "SELF",
    "PEER",
    "TEACHER",
    name="conferencematchevaluationkind",
    native_enum=False,
    length=16,
)
letter_category = sa.Enum(
    "REVIEW",
    "DERIVATIVE",
    "MATCH",
    "SYSTEM",
    name="conferencelettercategory",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    op.alter_column(
        "conference_matches",
        "status",
        existing_type=sa.String(length=16),
        type_=match_status,
        existing_nullable=False,
    )
    op.add_column(
        "conference_matches",
        sa.Column("end_reason", match_end_reason, nullable=True),
    )
    op.add_column(
        "conference_matches", sa.Column("winner_user_id", sa.Uuid(), nullable=True)
    )
    op.add_column(
        "conference_matches",
        sa.Column("question_count", sa.Integer(), server_default="3", nullable=False),
    )
    op.alter_column("conference_matches", "question_count", server_default=None)
    op.create_foreign_key(
        op.f("fk_conference_matches_winner_user_id_users"),
        "conference_matches",
        "users",
        ["winner_user_id"],
        ["id"],
        ondelete="SET NULL",
    )
    op.create_index(
        op.f("ix_conference_matches_winner_user_id"),
        "conference_matches",
        ["winner_user_id"],
    )
    op.create_check_constraint(
        op.f("ck_conference_matches_question_count_range"),
        "conference_matches",
        "question_count BETWEEN 3 AND 5",
    )
    # Existing matches predate question snapshots and cannot safely continue in the
    # new answering flow. Close them without inventing answers or evaluation data.
    op.execute(
        "UPDATE conference_matches SET status = 'ENDED', end_reason = 'EXITED', "
        "ended_at = COALESCE(ended_at, matched_at) WHERE status = 'ACTIVE'"
    )
    op.execute(
        "UPDATE conference_match_queue SET status = 'EXITED', expires_at = NULL "
        "WHERE status = 'MATCHED'"
    )

    op.create_table(
        "conference_match_questions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=False),
        sa.Column("position", sa.Integer(), nullable=False),
        sa.Column("kind", question_kind, nullable=False),
        sa.Column("prompt", sa.Text(), nullable=False),
        sa.Column("rubric", sa.Text(), nullable=False),
        sa.Column("content_version", sa.String(length=32), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "position BETWEEN 1 AND 5",
            name=op.f("ck_conference_match_questions_position_range"),
        ),
        sa.ForeignKeyConstraint(
            ["match_id"], ["conference_matches.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "match_id",
            "position",
            name="uq_conference_match_questions_match_position",
        ),
    )
    op.create_index(
        op.f("ix_conference_match_questions_match_id"),
        "conference_match_questions",
        ["match_id"],
    )

    op.create_table(
        "conference_match_answers",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=False),
        sa.Column("question_id", sa.Uuid(), nullable=False),
        sa.Column("participant_user_id", sa.Uuid(), nullable=False),
        sa.Column("answer", sa.Text(), nullable=False),
        sa.Column("reason", sa.Text(), nullable=False),
        sa.Column("submitted_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["match_id"], ["conference_matches.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["participant_user_id"], ["users.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["question_id"], ["conference_match_questions.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "question_id",
            "participant_user_id",
            name="uq_conference_match_answers_question_participant",
        ),
    )
    for column in ("match_id", "question_id", "participant_user_id"):
        op.create_index(
            op.f(f"ix_conference_match_answers_{column}"),
            "conference_match_answers",
            [column],
        )
    op.create_index(
        "ix_conference_match_answers_match_participant",
        "conference_match_answers",
        ["match_id", "participant_user_id"],
    )

    op.create_table(
        "conference_match_evaluations",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=False),
        sa.Column("subject_user_id", sa.Uuid(), nullable=False),
        sa.Column("evaluator_user_id", sa.Uuid(), nullable=True),
        sa.Column("kind", evaluation_kind, nullable=False),
        sa.Column("score", sa.Float(), nullable=False),
        sa.Column("dimension_scores", sa.JSON(), nullable=False),
        sa.Column("summary", sa.Text(), nullable=False),
        sa.Column("strengths", sa.JSON(), nullable=False),
        sa.Column("improvements", sa.JSON(), nullable=False),
        sa.Column("evaluator_reference", sa.String(length=120), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "score >= 0 AND score <= 100",
            name=op.f("ck_conference_match_evaluations_score_range"),
        ),
        sa.ForeignKeyConstraint(
            ["evaluator_user_id"], ["users.id"], ondelete="SET NULL"
        ),
        sa.ForeignKeyConstraint(
            ["match_id"], ["conference_matches.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["subject_user_id"], ["users.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "match_id",
            "subject_user_id",
            "kind",
            name="uq_conference_match_evaluations_match_subject_kind",
        ),
    )
    for column in ("match_id", "subject_user_id", "evaluator_user_id"):
        op.create_index(
            op.f(f"ix_conference_match_evaluations_{column}"),
            "conference_match_evaluations",
            [column],
        )
    op.create_index(
        "ix_conference_match_evaluations_match_subject",
        "conference_match_evaluations",
        ["match_id", "subject_user_id"],
    )

    op.create_table(
        "conference_match_reflections",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("match_id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("learned", sa.Text(), nullable=False),
        sa.Column("next_improvement", sa.Text(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["match_id"], ["conference_matches.id"], ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint(
            "match_id",
            "user_id",
            name="uq_conference_match_reflections_match_user",
        ),
    )
    op.create_index(
        op.f("ix_conference_match_reflections_match_id"),
        "conference_match_reflections",
        ["match_id"],
    )
    op.create_index(
        op.f("ix_conference_match_reflections_user_id"),
        "conference_match_reflections",
        ["user_id"],
    )

    op.create_table(
        "conference_letters",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("recipient_user_id", sa.Uuid(), nullable=False),
        sa.Column("category", letter_category, nullable=False),
        sa.Column("title", sa.String(length=80), nullable=False),
        sa.Column("body", sa.String(length=500), nullable=False),
        sa.Column("action_type", sa.String(length=40), nullable=True),
        sa.Column("action_id", sa.Uuid(), nullable=True),
        sa.Column("read_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["recipient_user_id"], ["users.id"], ondelete="CASCADE"
        ),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(
        op.f("ix_conference_letters_recipient_user_id"),
        "conference_letters",
        ["recipient_user_id"],
    )
    op.create_index(
        "ix_conference_letters_recipient_read_created",
        "conference_letters",
        ["recipient_user_id", "read_at", "created_at"],
    )


def downgrade() -> None:
    op.drop_index(
        "ix_conference_letters_recipient_read_created",
        table_name="conference_letters",
    )
    op.drop_index(
        op.f("ix_conference_letters_recipient_user_id"),
        table_name="conference_letters",
    )
    op.drop_table("conference_letters")
    op.drop_table("conference_match_reflections")
    op.drop_index(
        "ix_conference_match_evaluations_match_subject",
        table_name="conference_match_evaluations",
    )
    op.drop_table("conference_match_evaluations")
    op.drop_index(
        "ix_conference_match_answers_match_participant",
        table_name="conference_match_answers",
    )
    op.drop_table("conference_match_answers")
    op.drop_table("conference_match_questions")

    op.execute(
        "UPDATE conference_matches SET status = 'ENDED', "
        "end_reason = COALESCE(end_reason, 'EXITED'), "
        "ended_at = COALESCE(ended_at, matched_at) "
        "WHERE status = 'AWAITING_JUDGMENT'"
    )
    op.drop_constraint(
        op.f("ck_conference_matches_question_count_range"),
        "conference_matches",
        type_="check",
    )
    op.drop_index(
        op.f("ix_conference_matches_winner_user_id"),
        table_name="conference_matches",
    )
    op.drop_constraint(
        op.f("fk_conference_matches_winner_user_id_users"),
        "conference_matches",
        type_="foreignkey",
    )
    op.drop_column("conference_matches", "question_count")
    op.drop_column("conference_matches", "winner_user_id")
    op.drop_column("conference_matches", "end_reason")
    op.alter_column(
        "conference_matches",
        "status",
        existing_type=sa.String(length=24),
        type_=match_status_previous,
        existing_nullable=False,
    )
