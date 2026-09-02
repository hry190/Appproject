"""Add trials, immutable learning events, evidence, progress, and stats.

Revision ID: 0004_learning_events_and_progress
Revises: 0003_profiles_and_taxonomy
"""
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0004_learning_events_and_progress"
down_revision: str | None = "0003_profiles_and_taxonomy"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    # Alembic creates version_num as VARCHAR(32) by default. Our descriptive
    # revision identifiers are longer, so widen it before Alembic records this
    # revision. SQLite does not enforce the declared VARCHAR length and cannot
    # alter it directly; PostgreSQL is the production path that needs this DDL.
    if op.get_bind().dialect.name == "postgresql":
        op.alter_column(
            "alembic_version",
            "version_num",
            existing_type=sa.String(length=32),
            type_=sa.String(length=128),
            existing_nullable=False,
        )

    trial_status = sa.Enum(
        "DRAFT", "ACTIVE", "ARCHIVED", name="trialstatus", native_enum=False, length=16
    )
    grader_kind = sa.Enum(
        "EXACT_JSON", name="trialgraderkind", native_enum=False, length=20
    )
    evidence_category = sa.Enum(
        "WISDOM", "CRAFT", "CHIVALRY", name="evidencecategory", native_enum=False, length=16
    )
    attempt_result = sa.Enum(
        "PASSED", "FAILED", name="trialattemptresult", native_enum=False, length=16
    )
    event_type = sa.Enum(
        "COMIC_PAGE_OPENED",
        "PREDICTION_COMPLETED",
        "TRIAL_GRADED",
        "TRIAL_PASSED",
        "TRANSFER_EVIDENCE_APPROVED",
        "STRUCTURED_REVIEW_ACCEPTED",
        "PROJECT_REVISION_COMPLETED",
        "MEDIA_UPLOAD_COMPLETED",
        name="learningeventtype",
        native_enum=False,
        length=40,
    )
    validation_status = sa.Enum(
        "VALID",
        "PENDING_REVIEW",
        "REVOKED",
        name="evidencevalidationstatus",
        native_enum=False,
        length=20,
    )
    progress_state = sa.Enum(
        "UNSEEN",
        "DISCOVERED",
        "LEARNED",
        "MASTERED",
        "TEACHING",
        name="manualprogressstate",
        native_enum=False,
        length=16,
    )

    op.create_table(
        "trials",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("code", sa.String(length=80), nullable=False),
        sa.Column("title", sa.String(length=100), nullable=False),
        sa.Column("knowledge_point_code", sa.String(length=80), nullable=False),
        sa.Column("status", trial_status, nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_trials_manual_page_id_manual_pages"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_trials")),
        sa.UniqueConstraint("code", name=op.f("uq_trials_code")),
    )
    op.create_index(
        op.f("ix_trials_manual_page_id"), "trials", ["manual_page_id"], unique=False
    )
    op.create_table(
        "trial_versions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("trial_id", sa.Uuid(), nullable=False),
        sa.Column("version", sa.Integer(), nullable=False),
        sa.Column("prompt", sa.Text(), nullable=False),
        sa.Column("prediction_prompt", sa.Text(), nullable=False),
        sa.Column("answer_schema", sa.JSON(), nullable=False),
        sa.Column("grader_kind", grader_kind, nullable=False),
        sa.Column("grader_config", sa.JSON(), nullable=False),
        sa.Column("max_score", sa.Float(), nullable=False),
        sa.Column("pass_score", sa.Float(), nullable=False),
        sa.Column("prediction_required", sa.Boolean(), nullable=False),
        sa.Column("explanation_required", sa.Boolean(), nullable=False),
        sa.Column("min_explanation_length", sa.Integer(), nullable=False),
        sa.Column("evidence_category", evidence_category, nullable=False),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("is_active", sa.Boolean(), nullable=False),
        sa.Column("published_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "min_explanation_length >= 0",
            name=op.f("ck_trial_versions_min_explanation_length_non_negative"),
        ),
        sa.CheckConstraint(
            "max_score > 0",
            name=op.f("ck_trial_versions_max_score_positive"),
        ),
        sa.CheckConstraint(
            "pass_score >= 0 AND pass_score <= max_score",
            name=op.f("ck_trial_versions_pass_score_range"),
        ),
        sa.CheckConstraint(
            "version >= 1",
            name=op.f("ck_trial_versions_version_positive"),
        ),
        sa.ForeignKeyConstraint(
            ["trial_id"],
            ["trials.id"],
            name=op.f("fk_trial_versions_trial_id_trials"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_trial_versions")),
        sa.UniqueConstraint(
            "trial_id", "version", name="uq_trial_versions_trial_version"
        ),
    )
    op.create_index(
        op.f("ix_trial_versions_trial_id"),
        "trial_versions",
        ["trial_id"],
        unique=False,
    )
    op.create_table(
        "practice_sessions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("source_type", sa.String(length=40), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("qualified_at", sa.DateTime(timezone=True)),
        sa.Column("ended_at", sa.DateTime(timezone=True)),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_practice_sessions_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_practice_sessions")),
    )
    op.create_index(
        op.f("ix_practice_sessions_user_id"),
        "practice_sessions",
        ["user_id"],
        unique=False,
    )
    op.create_index(
        "ix_practice_sessions_user_qualified",
        "practice_sessions",
        ["user_id", "qualified_at"],
        unique=False,
    )
    op.create_table(
        "trial_attempts",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("trial_id", sa.Uuid(), nullable=False),
        sa.Column("trial_version_id", sa.Uuid(), nullable=False),
        sa.Column("practice_session_id", sa.Uuid(), nullable=False),
        sa.Column("prediction_payload", sa.JSON()),
        sa.Column("answer_payload", sa.JSON(), nullable=False),
        sa.Column("explanation", sa.Text()),
        sa.Column("server_score", sa.Float(), nullable=False),
        sa.Column("max_score", sa.Float(), nullable=False),
        sa.Column("result", attempt_result, nullable=False),
        sa.Column("feedback_codes", sa.JSON(), nullable=False),
        sa.Column("idempotency_key", sa.String(length=64), nullable=False),
        sa.Column("request_fingerprint", sa.String(length=64), nullable=False),
        sa.Column("client_request_id", sa.String(length=64)),
        sa.Column("response_payload", sa.JSON(), nullable=False),
        sa.Column("submitted_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("graded_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "max_score > 0", name=op.f("ck_trial_attempts_max_score_positive")
        ),
        sa.CheckConstraint(
            "server_score <= max_score",
            name=op.f("ck_trial_attempts_server_score_not_over_max"),
        ),
        sa.CheckConstraint(
            "server_score >= 0",
            name=op.f("ck_trial_attempts_server_score_non_negative"),
        ),
        sa.ForeignKeyConstraint(
            ["practice_session_id"],
            ["practice_sessions.id"],
            name=op.f("fk_trial_attempts_practice_session_id_practice_sessions"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["trial_id"],
            ["trials.id"],
            name=op.f("fk_trial_attempts_trial_id_trials"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["trial_version_id"],
            ["trial_versions.id"],
            name=op.f("fk_trial_attempts_trial_version_id_trial_versions"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_trial_attempts_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_trial_attempts")),
        sa.UniqueConstraint(
            "user_id",
            "idempotency_key",
            name="uq_trial_attempts_user_idempotency",
        ),
    )
    for column in ("practice_session_id", "trial_id", "trial_version_id", "user_id"):
        op.create_index(
            op.f(f"ix_trial_attempts_{column}"),
            "trial_attempts",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_trial_attempts_user_trial_submitted",
        "trial_attempts",
        ["user_id", "trial_id", "submitted_at"],
        unique=False,
    )
    op.create_table(
        "learning_events",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("event_type", event_type, nullable=False),
        sa.Column("source_type", sa.String(length=40), nullable=False),
        sa.Column("source_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid()),
        sa.Column("practice_session_id", sa.Uuid()),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("payload", sa.JSON(), nullable=False),
        sa.Column("idempotency_key", sa.String(length=96), nullable=False),
        sa.Column("occurred_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("revoked_at", sa.DateTime(timezone=True)),
        sa.Column("revocation_reason", sa.String(length=160)),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_learning_events_manual_page_id_manual_pages"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["practice_session_id"],
            ["practice_sessions.id"],
            name=op.f("fk_learning_events_practice_session_id_practice_sessions"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_learning_events_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_learning_events")),
        sa.UniqueConstraint(
            "user_id",
            "idempotency_key",
            name="uq_learning_events_user_idempotency",
        ),
    )
    for column in ("manual_page_id", "practice_session_id", "source_id", "user_id"):
        op.create_index(
            op.f(f"ix_learning_events_{column}"),
            "learning_events",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_learning_events_user_occurred",
        "learning_events",
        ["user_id", "occurred_at"],
        unique=False,
    )
    op.create_index(
        "ix_learning_events_source",
        "learning_events",
        ["source_type", "source_id"],
        unique=False,
    )
    op.create_table(
        "learning_evidence",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("category", evidence_category, nullable=False),
        sa.Column("evidence_type", sa.String(length=60), nullable=False),
        sa.Column("source_type", sa.String(length=40), nullable=False),
        sa.Column("source_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid()),
        sa.Column("summary", sa.String(length=240), nullable=False),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("validation_status", validation_status, nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("validated_at", sa.DateTime(timezone=True)),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_learning_evidence_manual_page_id_manual_pages"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_learning_evidence_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_learning_evidence")),
    )
    for column in ("manual_page_id", "source_id", "user_id"):
        op.create_index(
            op.f(f"ix_learning_evidence_{column}"),
            "learning_evidence",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_learning_evidence_user_category_status_created",
        "learning_evidence",
        ["user_id", "category", "validation_status", "created_at"],
        unique=False,
    )
    op.create_table(
        "manual_progress",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("state", progress_state, nullable=False),
        sa.Column("discovered_at", sa.DateTime(timezone=True)),
        sa.Column("learned_at", sa.DateTime(timezone=True)),
        sa.Column("mastered_at", sa.DateTime(timezone=True)),
        sa.Column("teaching_at", sa.DateTime(timezone=True)),
        sa.Column("latest_evidence_id", sa.Uuid()),
        sa.Column("projection_version", sa.Integer(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["latest_evidence_id"],
            ["learning_evidence.id"],
            name=op.f("fk_manual_progress_latest_evidence_id_learning_evidence"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_manual_progress_manual_page_id_manual_pages"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_manual_progress_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint(
            "user_id", "manual_page_id", name=op.f("pk_manual_progress")
        ),
    )
    op.create_index(
        op.f("ix_manual_progress_latest_evidence_id"),
        "manual_progress",
        ["latest_evidence_id"],
        unique=False,
    )
    op.create_index(
        "ix_manual_progress_user_state_updated",
        "manual_progress",
        ["user_id", "state", "updated_at"],
        unique=False,
    )
    op.create_table(
        "progress_transitions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("previous_state", progress_state, nullable=False),
        sa.Column("current_state", progress_state, nullable=False),
        sa.Column("event_id", sa.Uuid(), nullable=False),
        sa.Column("evidence_id", sa.Uuid()),
        sa.Column("rule_version", sa.String(length=32), nullable=False),
        sa.Column("projection_version", sa.Integer(), nullable=False),
        sa.Column("occurred_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "projection_version >= 1",
            name=op.f("ck_progress_transitions_projection_version_positive"),
        ),
        sa.ForeignKeyConstraint(
            ["event_id"],
            ["learning_events.id"],
            name=op.f("fk_progress_transitions_event_id_learning_events"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["evidence_id"],
            ["learning_evidence.id"],
            name=op.f("fk_progress_transitions_evidence_id_learning_evidence"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_progress_transitions_manual_page_id_manual_pages"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_progress_transitions_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_progress_transitions")),
    )
    for column in ("event_id", "evidence_id", "manual_page_id", "user_id"):
        op.create_index(
            op.f(f"ix_progress_transitions_{column}"),
            "progress_transitions",
            [column],
            unique=False,
        )
    op.create_index(
        "ix_progress_transitions_user_page_occurred",
        "progress_transitions",
        ["user_id", "manual_page_id", "occurred_at"],
        unique=False,
    )
    op.create_table(
        "user_learning_stats",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("lifetime_practice_count", sa.Integer(), nullable=False),
        sa.Column("distinct_trials_passed", sa.Integer(), nullable=False),
        sa.Column("wisdom_count", sa.Integer(), nullable=False),
        sa.Column("craft_count", sa.Integer(), nullable=False),
        sa.Column("chivalry_count", sa.Integer(), nullable=False),
        sa.Column("wisdom_latest_at", sa.DateTime(timezone=True)),
        sa.Column("craft_latest_at", sa.DateTime(timezone=True)),
        sa.Column("chivalry_latest_at", sa.DateTime(timezone=True)),
        sa.Column("last_practice_at", sa.DateTime(timezone=True)),
        sa.Column("projection_version", sa.Integer(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "chivalry_count >= 0",
            name=op.f("ck_user_learning_stats_chivalry_count_non_negative"),
        ),
        sa.CheckConstraint(
            "craft_count >= 0",
            name=op.f("ck_user_learning_stats_craft_count_non_negative"),
        ),
        sa.CheckConstraint(
            "distinct_trials_passed >= 0",
            name=op.f("ck_user_learning_stats_trials_passed_non_negative"),
        ),
        sa.CheckConstraint(
            "lifetime_practice_count >= 0",
            name=op.f("ck_user_learning_stats_practice_count_non_negative"),
        ),
        sa.CheckConstraint(
            "wisdom_count >= 0",
            name=op.f("ck_user_learning_stats_wisdom_count_non_negative"),
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_learning_stats_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("user_id", name=op.f("pk_user_learning_stats")),
    )


def downgrade() -> None:
    op.drop_table("user_learning_stats")
    op.drop_table("progress_transitions")
    op.drop_table("manual_progress")
    op.drop_table("learning_evidence")
    op.drop_table("learning_events")
    op.drop_table("trial_attempts")
    op.drop_table("practice_sessions")
    op.drop_table("trial_versions")
    op.drop_table("trials")
