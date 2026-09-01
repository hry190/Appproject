"""Add user settings, guardian controls, support, and data-rights tables.

Revision ID: 0002_user_settings
Revises: 0001_auth
"""
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0002_user_settings"
down_revision: str | None = "0001_auth"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    content_level = sa.Enum(
        "CHILD", "TEEN", "STANDARD", name="contentlevel", native_enum=False
    )
    feedback_category = sa.Enum(
        "GENERAL", "BUG", "CONTENT_SAFETY", "ACCOUNT",
        name="feedbackcategory",
        native_enum=False,
    )
    ticket_status = sa.Enum(
        "OPEN", "PROCESSING", "RESOLVED", name="ticketstatus", native_enum=False
    )
    data_request_type = sa.Enum(
        "ACCOUNT_DELETION", "CONSENT_WITHDRAWAL",
        name="datarequesttype",
        native_enum=False,
    )
    data_request_status = sa.Enum(
        "PENDING", "PROCESSING", "COMPLETED", "REJECTED",
        name="datarequeststatus",
        native_enum=False,
    )

    op.create_table(
        "user_preferences",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("message_enabled", sa.Boolean(), nullable=False),
        sa.Column("learning_reminder", sa.Boolean(), nullable=False),
        sa.Column("work_updates", sa.Boolean(), nullable=False),
        sa.Column("service_messages", sa.Boolean(), nullable=False),
        sa.Column("quiet_hours", sa.Boolean(), nullable=False),
        sa.Column("auto_save", sa.Boolean(), nullable=False),
        sa.Column("wifi_only", sa.Boolean(), nullable=False),
        sa.Column("haptic_feedback", sa.Boolean(), nullable=False),
        sa.Column("large_text", sa.Boolean(), nullable=False),
        sa.Column("sound_enabled", sa.Boolean(), nullable=False),
        sa.Column("music_volume", sa.Float(), nullable=False),
        sa.Column("effect_volume", sa.Float(), nullable=False),
        sa.Column("high_contrast", sa.Boolean(), nullable=False),
        sa.Column("read_aloud", sa.Boolean(), nullable=False),
        sa.Column("subtitles_enabled", sa.Boolean(), nullable=False),
        sa.Column("personalization_enabled", sa.Boolean(), nullable=False),
        sa.Column("rest_reminder", sa.Boolean(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"],
            name=op.f("fk_user_preferences_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("user_id", name=op.f("pk_user_preferences")),
    )

    op.create_table(
        "guardian_controls",
        sa.Column("child_user_id", sa.Uuid(), nullable=False),
        sa.Column("daily_limit_minutes", sa.Integer(), nullable=False),
        sa.Column("creation_allowed", sa.Boolean(), nullable=False),
        sa.Column("content_level", content_level, nullable=False),
        sa.Column("minor_mode", sa.Boolean(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["child_user_id"], ["users.id"],
            name=op.f("fk_guardian_controls_child_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint(
            "child_user_id", name=op.f("pk_guardian_controls")
        ),
    )

    op.create_table(
        "feedback_tickets",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("category", feedback_category, nullable=False),
        sa.Column("message", sa.Text(), nullable=False),
        sa.Column("status", ticket_status, nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"],
            name=op.f("fk_feedback_tickets_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_feedback_tickets")),
    )
    op.create_index(
        op.f("ix_feedback_tickets_user_id"),
        "feedback_tickets",
        ["user_id"],
        unique=False,
    )

    op.create_table(
        "data_rights_requests",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("request_type", data_request_type, nullable=False),
        sa.Column("reason", sa.Text()),
        sa.Column("status", data_request_status, nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"],
            name=op.f("fk_data_rights_requests_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_data_rights_requests")),
    )
    op.create_index(
        op.f("ix_data_rights_requests_user_id"),
        "data_rights_requests",
        ["user_id"],
        unique=False,
    )

    op.create_table(
        "blacklist_entries",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("blocked_user_id", sa.Uuid(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["blocked_user_id"], ["users.id"],
            name=op.f("fk_blacklist_entries_blocked_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"], ["users.id"],
            name=op.f("fk_blacklist_entries_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_blacklist_entries")),
        sa.UniqueConstraint(
            "owner_user_id", "blocked_user_id", name="uq_blacklist_owner_blocked"
        ),
    )
    op.create_index(
        op.f("ix_blacklist_entries_blocked_user_id"),
        "blacklist_entries",
        ["blocked_user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_blacklist_entries_owner_user_id"),
        "blacklist_entries",
        ["owner_user_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_table("blacklist_entries")
    op.drop_table("data_rights_requests")
    op.drop_table("feedback_tickets")
    op.drop_table("guardian_controls")
    op.drop_table("user_preferences")
