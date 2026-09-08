"""Add asynchronous flattened creation export jobs.

Revision ID: 0014_creation_export_jobs
Revises: 0013_image_generation_jobs
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0014_creation_export_jobs"
down_revision: str | None = "0013_image_generation_jobs"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


export_format = sa.Enum(
    "PNG",
    "JPEG",
    name="creationexportformat",
    native_enum=False,
    length=8,
)
export_status = sa.Enum(
    "QUEUED",
    "RENDERING",
    "SAFETY_CHECK",
    "COMPLETED",
    "FAILED",
    "REJECTED",
    name="creationexportjobstatus",
    native_enum=False,
    length=20,
)


def upgrade() -> None:
    op.create_table(
        "creation_export_jobs",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("creation_version_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("format", export_format, nullable=False),
        sa.Column("output_scale", sa.Integer(), nullable=False),
        sa.Column("status", export_status, nullable=False),
        sa.Column("progress_percent", sa.Integer(), nullable=False),
        sa.Column("output_asset_id", sa.Uuid(), nullable=True),
        sa.Column("error_code", sa.String(length=80), nullable=True),
        sa.Column("error_summary", sa.String(length=500), nullable=True),
        sa.Column("idempotency_key", sa.String(length=64), nullable=False),
        sa.Column("request_fingerprint", sa.String(length=64), nullable=False),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "progress_percent >= 0 AND progress_percent <= 100",
            name=op.f("ck_creation_export_jobs_progress_percent_range"),
        ),
        sa.CheckConstraint(
            "output_scale >= 1 AND output_scale <= 2",
            name=op.f("ck_creation_export_jobs_output_scale_range"),
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_creation_export_jobs_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["creation_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_creation_export_jobs_creation_version_id_creation_versions"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_creation_export_jobs_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["output_asset_id"],
            ["media_assets.id"],
            name=op.f("fk_creation_export_jobs_output_asset_id_media_assets"),
            ondelete="SET NULL",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_creation_export_jobs")),
        sa.UniqueConstraint(
            "owner_user_id",
            "idempotency_key",
            name="uq_creation_export_jobs_owner_idempotency",
        ),
    )
    op.create_index(op.f("ix_creation_export_jobs_project_id"), "creation_export_jobs", ["project_id"])
    op.create_index(
        op.f("ix_creation_export_jobs_creation_version_id"),
        "creation_export_jobs",
        ["creation_version_id"],
    )
    op.create_index(
        op.f("ix_creation_export_jobs_owner_user_id"),
        "creation_export_jobs",
        ["owner_user_id"],
    )
    op.create_index(
        op.f("ix_creation_export_jobs_output_asset_id"),
        "creation_export_jobs",
        ["output_asset_id"],
    )
    op.create_index(
        "ix_creation_export_jobs_owner_created",
        "creation_export_jobs",
        ["owner_user_id", "created_at"],
    )
    op.create_index(
        "ix_creation_export_jobs_version_status",
        "creation_export_jobs",
        ["creation_version_id", "status"],
    )


def downgrade() -> None:
    op.drop_index("ix_creation_export_jobs_version_status", table_name="creation_export_jobs")
    op.drop_index("ix_creation_export_jobs_owner_created", table_name="creation_export_jobs")
    op.drop_index(op.f("ix_creation_export_jobs_output_asset_id"), table_name="creation_export_jobs")
    op.drop_index(op.f("ix_creation_export_jobs_owner_user_id"), table_name="creation_export_jobs")
    op.drop_index(op.f("ix_creation_export_jobs_creation_version_id"), table_name="creation_export_jobs")
    op.drop_index(op.f("ix_creation_export_jobs_project_id"), table_name="creation_export_jobs")
    op.drop_table("creation_export_jobs")
