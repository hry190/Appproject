"""Add conversation-driven creation records.

Revision ID: 0023_creation_conversations
Revises: 0022_manual_prerequisites
"""

from __future__ import annotations

import sqlalchemy as sa
from alembic import op


revision: str = "0023_creation_conversations"
down_revision: str | None = "0022_manual_prerequisites"
branch_labels: str | None = None
depends_on: str | None = None


def upgrade() -> None:
    op.create_table(
        "creation_conversations",
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column(
            "status",
            sa.Enum(
                "DIALOGUE",
                "GENERATING",
                "RESULT_READY",
                "SAVED",
                "GENERATION_FAILED",
                name="creationconversationstatus",
                native_enum=False,
                length=24,
            ),
            nullable=False,
        ),
        sa.Column("initial_idea", sa.Text(), nullable=False),
        sa.Column("attachment_asset_ids", sa.JSON(), nullable=False),
        sa.Column("manual_page_ids", sa.JSON(), nullable=False),
        sa.Column("plan_summary", sa.Text(), nullable=True),
        sa.Column("active_suggestion_id", sa.Uuid(), nullable=True),
        sa.Column("draft_version_ids", sa.JSON(), nullable=False),
        sa.Column("saved_version_ids", sa.JSON(), nullable=False),
        sa.Column("active_generation_job_id", sa.Uuid(), nullable=True),
        sa.Column("result_version_id", sa.Uuid(), nullable=True),
        sa.Column("last_generation_idempotency_key", sa.String(length=64), nullable=True),
        sa.Column("last_generation_request_fingerprint", sa.String(length=64), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["active_generation_job_id"],
            ["image_generation_jobs.id"],
            name=op.f("fk_creation_conversations_active_generation_job_id_image_generation_jobs"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_conversations_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_conversations_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["result_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_creation_conversations_result_version_id_creation_versions"),
            ondelete="SET NULL",
        ),
        sa.PrimaryKeyConstraint("project_id", name=op.f("pk_creation_conversations")),
    )
    op.create_index(
        op.f("ix_creation_conversations_owner_user_id"),
        "creation_conversations",
        ["owner_user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_conversations_active_suggestion_id"),
        "creation_conversations",
        ["active_suggestion_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_conversations_active_generation_job_id"),
        "creation_conversations",
        ["active_generation_job_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_conversations_result_version_id"),
        "creation_conversations",
        ["result_version_id"],
        unique=False,
    )

    op.create_table(
        "creation_conversation_messages",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column(
            "role",
            sa.Enum(
                "STUDENT",
                "COACH",
                name="creationconversationrole",
                native_enum=False,
                length=16,
            ),
            nullable=False,
        ),
        sa.Column(
            "kind",
            sa.Enum(
                "IDEA",
                "MESSAGE",
                "SUGGESTION",
                name="creationconversationmessagekind",
                native_enum=False,
                length=16,
            ),
            nullable=False,
        ),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column(
            "decision",
            sa.Enum(
                "NONE",
                "PENDING",
                "ACCEPTED",
                "REPLACED",
                name="creationsuggestiondecision",
                native_enum=False,
                length=16,
            ),
            nullable=False,
        ),
        sa.Column("in_reply_to_id", sa.Uuid(), nullable=True),
        sa.Column("client_idempotency_key", sa.String(length=64), nullable=True),
        sa.Column("request_fingerprint", sa.String(length=64), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["in_reply_to_id"],
            ["creation_conversation_messages.id"],
            name=op.f(
                "fk_creation_conversation_messages_in_reply_to_id_creation_conversation_messages"
            ),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_conversation_messages_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_conversations.project_id"],
            name=op.f("fk_creation_conversation_messages_project_id_creation_conversations"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_conversation_messages")),
        sa.UniqueConstraint(
            "owner_user_id",
            "client_idempotency_key",
            name="uq_creation_conversation_messages_owner_idempotency",
        ),
    )
    op.create_index(
        op.f("ix_creation_conversation_messages_project_id"),
        "creation_conversation_messages",
        ["project_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_conversation_messages_owner_user_id"),
        "creation_conversation_messages",
        ["owner_user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_conversation_messages_in_reply_to_id"),
        "creation_conversation_messages",
        ["in_reply_to_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(
        op.f("ix_creation_conversation_messages_in_reply_to_id"),
        table_name="creation_conversation_messages",
    )
    op.drop_index(
        op.f("ix_creation_conversation_messages_owner_user_id"),
        table_name="creation_conversation_messages",
    )
    op.drop_index(
        op.f("ix_creation_conversation_messages_project_id"),
        table_name="creation_conversation_messages",
    )
    op.drop_table("creation_conversation_messages")
    op.drop_index(
        op.f("ix_creation_conversations_result_version_id"),
        table_name="creation_conversations",
    )
    op.drop_index(
        op.f("ix_creation_conversations_active_generation_job_id"),
        table_name="creation_conversations",
    )
    op.drop_index(
        op.f("ix_creation_conversations_active_suggestion_id"),
        table_name="creation_conversations",
    )
    op.drop_index(
        op.f("ix_creation_conversations_owner_user_id"),
        table_name="creation_conversations",
    )
    op.drop_table("creation_conversations")
