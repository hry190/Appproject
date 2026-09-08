"""Add creation intent, method, and stage workflow persistence.

Revision ID: 0009_creation_workflow
Revises: 0008_luggage_ui_contract
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0009_creation_workflow"
down_revision: str | None = "0008_luggage_ui_contract"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


creation_stage = sa.Enum(
    "IDEATION",
    "DRAFT",
    "PRODUCTION",
    "TEST",
    "SEAL",
    name="creationstage",
    native_enum=False,
    length=20,
)
intent_status = sa.Enum(
    "ANALYZED",
    "CONVERTED",
    "EXPIRED",
    name="creationintentstatus",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    with op.batch_alter_table("creation_projects") as batch_op:
        batch_op.add_column(
            sa.Column(
                "current_stage",
                creation_stage,
                nullable=False,
                server_default="IDEATION",
            )
        )
        batch_op.add_column(
            sa.Column(
                "stage_updated_at",
                sa.DateTime(timezone=True),
                nullable=False,
                server_default=sa.func.now(),
            )
        )
        batch_op.add_column(
            sa.Column("create_idempotency_key", sa.String(length=64), nullable=True)
        )
        batch_op.add_column(
            sa.Column("create_request_fingerprint", sa.String(length=64), nullable=True)
        )
        batch_op.create_unique_constraint(
            "uq_creation_projects_owner_create_idempotency",
            ["owner_user_id", "create_idempotency_key"],
        )

    # Preserve a useful resume point for projects created before this workflow
    # existed. Existing saved versions belong in production; submitted work is
    # already at the final explanation/sealing step.
    op.execute(
        sa.text(
            """
            UPDATE creation_projects
            SET current_stage = 'PRODUCTION'
            WHERE current_version_number IS NOT NULL
            """
        )
    )
    op.execute(
        sa.text(
            """
            UPDATE creation_projects
            SET current_stage = 'SEAL'
            WHERE EXISTS (
                SELECT 1
                FROM publications
                WHERE publications.project_id = creation_projects.id
            )
            """
        )
    )

    op.create_table(
        "creation_intents",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("text", sa.Text(), nullable=False),
        sa.Column("attachment_refs", sa.JSON(), nullable=False),
        sa.Column("resource_links", sa.JSON(), nullable=False),
        sa.Column("status", intent_status, nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_intents_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_intents")),
    )
    op.create_index(
        op.f("ix_creation_intents_owner_user_id"),
        "creation_intents",
        ["owner_user_id"],
        unique=False,
    )

    with op.batch_alter_table("creation_projects") as batch_op:
        batch_op.add_column(sa.Column("source_intent_id", sa.Uuid(), nullable=True))
        batch_op.create_foreign_key(
            op.f("fk_creation_projects_source_intent_id_creation_intents"),
            "creation_intents",
            ["source_intent_id"],
            ["id"],
            ondelete="SET NULL",
        )
        batch_op.create_unique_constraint(
            "uq_creation_projects_source_intent",
            ["source_intent_id"],
        )
        batch_op.create_index(
            op.f("ix_creation_projects_source_intent_id"),
            ["source_intent_id"],
            unique=False,
        )

    op.create_table(
        "creation_intent_analyses",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("intent_id", sa.Uuid(), nullable=False),
        sa.Column("schema_version", sa.String(length=20), nullable=False),
        sa.Column("suggestion", sa.JSON(), nullable=False),
        sa.Column("confidence", sa.String(length=12), nullable=False),
        sa.Column("safety_flags", sa.JSON(), nullable=False),
        sa.Column("model_ref", sa.String(length=80), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["intent_id"],
            ["creation_intents.id"],
            name=op.f("fk_creation_intent_analyses_intent_id_creation_intents"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_intent_analyses")),
    )
    op.create_index(
        op.f("ix_creation_intent_analyses_intent_id"),
        "creation_intent_analyses",
        ["intent_id"],
        unique=False,
    )

    op.create_table(
        "creation_methods",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("version_number", sa.Integer(), nullable=False),
        sa.Column("created_by_user_id", sa.Uuid(), nullable=False),
        sa.Column("name", sa.String(length=80), nullable=False),
        sa.Column("goal", sa.Text(), nullable=False),
        sa.Column("audience", sa.JSON(), nullable=False),
        sa.Column("format", sa.String(length=40), nullable=False),
        sa.Column("steps", sa.JSON(), nullable=False),
        sa.Column("resource_links", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "version_number >= 1",
            name=op.f("ck_creation_methods_version_number_positive"),
        ),
        sa.ForeignKeyConstraint(
            ["created_by_user_id"],
            ["users.id"],
            name=op.f("fk_creation_methods_created_by_user_id_users"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_methods_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_methods")),
        sa.UniqueConstraint(
            "project_id",
            "version_number",
            name="uq_creation_methods_project_version",
        ),
    )
    op.create_index(
        op.f("ix_creation_methods_created_by_user_id"),
        "creation_methods",
        ["created_by_user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_methods_project_id"),
        "creation_methods",
        ["project_id"],
        unique=False,
    )

    op.create_table(
        "creation_stage_events",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("actor_user_id", sa.Uuid(), nullable=False),
        sa.Column("from_stage", creation_stage, nullable=False),
        sa.Column("to_stage", creation_stage, nullable=False),
        sa.Column("reason", sa.String(length=500), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["actor_user_id"],
            ["users.id"],
            name=op.f("fk_creation_stage_events_actor_user_id_users"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_stage_events_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_stage_events")),
    )
    op.create_index(
        op.f("ix_creation_stage_events_actor_user_id"),
        "creation_stage_events",
        ["actor_user_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_creation_stage_events_project_id"),
        "creation_stage_events",
        ["project_id"],
        unique=False,
    )

    with op.batch_alter_table("creation_versions") as batch_op:
        batch_op.add_column(
            sa.Column("create_idempotency_key", sa.String(length=64), nullable=True)
        )
        batch_op.add_column(
            sa.Column("create_request_fingerprint", sa.String(length=64), nullable=True)
        )
        batch_op.create_unique_constraint(
            "uq_creation_versions_project_create_idempotency",
            ["project_id", "create_idempotency_key"],
        )


def downgrade() -> None:
    with op.batch_alter_table("creation_versions") as batch_op:
        batch_op.drop_constraint(
            "uq_creation_versions_project_create_idempotency",
            type_="unique",
        )
        batch_op.drop_column("create_request_fingerprint")
        batch_op.drop_column("create_idempotency_key")

    op.drop_index(
        op.f("ix_creation_stage_events_project_id"),
        table_name="creation_stage_events",
    )
    op.drop_index(
        op.f("ix_creation_stage_events_actor_user_id"),
        table_name="creation_stage_events",
    )
    op.drop_table("creation_stage_events")
    op.drop_index(
        op.f("ix_creation_methods_project_id"),
        table_name="creation_methods",
    )
    op.drop_index(
        op.f("ix_creation_methods_created_by_user_id"),
        table_name="creation_methods",
    )
    op.drop_table("creation_methods")
    op.drop_index(
        op.f("ix_creation_intent_analyses_intent_id"),
        table_name="creation_intent_analyses",
    )
    op.drop_table("creation_intent_analyses")
    with op.batch_alter_table("creation_projects") as batch_op:
        batch_op.drop_index(op.f("ix_creation_projects_source_intent_id"))
        batch_op.drop_constraint(
            op.f("fk_creation_projects_source_intent_id_creation_intents"),
            type_="foreignkey",
        )
        batch_op.drop_constraint(
            "uq_creation_projects_source_intent",
            type_="unique",
        )
        batch_op.drop_column("source_intent_id")
    op.drop_index(
        op.f("ix_creation_intents_owner_user_id"),
        table_name="creation_intents",
    )
    op.drop_table("creation_intents")

    with op.batch_alter_table("creation_projects") as batch_op:
        batch_op.drop_constraint(
            "uq_creation_projects_owner_create_idempotency",
            type_="unique",
        )
        batch_op.drop_column("create_request_fingerprint")
        batch_op.drop_column("create_idempotency_key")
        batch_op.drop_column("stage_updated_at")
        batch_op.drop_column("current_stage")
