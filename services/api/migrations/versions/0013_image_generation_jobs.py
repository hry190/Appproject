"""Add explicit image generation jobs and immutable output links.

Revision ID: 0013_image_generation_jobs
Revises: 0012_creation_seal_check
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0013_image_generation_jobs"
down_revision: str | None = "0012_creation_seal_check"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


job_status = sa.Enum(
    "QUEUED",
    "RUNNING",
    "SAFETY_CHECK",
    "VERSIONING",
    "COMPLETED",
    "FAILED",
    "REJECTED",
    name="imagegenerationjobstatus",
    native_enum=False,
    length=20,
)
generation_size = sa.Enum(
    "SQUARE",
    "PORTRAIT",
    "LANDSCAPE",
    name="imagegenerationsize",
    native_enum=False,
    length=16,
)
generation_quality = sa.Enum(
    "LOW",
    "MEDIUM",
    "HIGH",
    name="imagegenerationquality",
    native_enum=False,
    length=16,
)


def upgrade() -> None:
    op.create_table(
        "image_generation_jobs",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("project_id", sa.Uuid(), nullable=False),
        sa.Column("parent_version_id", sa.Uuid(), nullable=False),
        sa.Column("owner_user_id", sa.Uuid(), nullable=False),
        sa.Column("prompt", sa.Text(), nullable=False),
        sa.Column("size", generation_size, nullable=False),
        sa.Column("quality", generation_quality, nullable=False),
        sa.Column("status", job_status, nullable=False),
        sa.Column("progress_percent", sa.Integer(), nullable=False),
        sa.Column("provider_ref", sa.String(length=40), nullable=False),
        sa.Column("model_ref", sa.String(length=120), nullable=False),
        sa.Column("external_data_shared", sa.Boolean(), nullable=False),
        sa.Column("output_asset_id", sa.Uuid(), nullable=True),
        sa.Column("output_version_id", sa.Uuid(), nullable=True),
        sa.Column("error_code", sa.String(length=80), nullable=True),
        sa.Column("error_summary", sa.String(length=500), nullable=True),
        sa.Column("retryable", sa.Boolean(), nullable=False),
        sa.Column("quota_charged", sa.Boolean(), nullable=False),
        sa.Column("retry_count", sa.Integer(), nullable=False),
        sa.Column("idempotency_key", sa.String(length=64), nullable=False),
        sa.Column("request_fingerprint", sa.String(length=64), nullable=False),
        sa.Column("last_retry_idempotency_key", sa.String(length=64), nullable=True),
        sa.Column("last_retry_fingerprint", sa.String(length=64), nullable=True),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "progress_percent >= 0 AND progress_percent <= 100",
            name=op.f("ck_image_generation_jobs_progress_percent_range"),
        ),
        sa.CheckConstraint(
            "retry_count >= 0",
            name=op.f("ck_image_generation_jobs_retry_count_nonnegative"),
        ),
        sa.ForeignKeyConstraint(
            ["owner_user_id"],
            ["users.id"],
            name=op.f("fk_image_generation_jobs_owner_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["parent_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_image_generation_jobs_parent_version_id_creation_versions"),
            ondelete="RESTRICT",
        ),
        sa.ForeignKeyConstraint(
            ["project_id"],
            ["creation_projects.id"],
            name=op.f("fk_image_generation_jobs_project_id_creation_projects"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["output_asset_id"],
            ["media_assets.id"],
            name=op.f("fk_image_generation_jobs_output_asset_id_media_assets"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["output_version_id"],
            ["creation_versions.id"],
            name=op.f("fk_image_generation_jobs_output_version_id_creation_versions"),
            ondelete="SET NULL",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_image_generation_jobs")),
        sa.UniqueConstraint(
            "owner_user_id",
            "idempotency_key",
            name="uq_image_generation_jobs_owner_idempotency",
        ),
    )
    op.create_index(
        "ix_image_generation_jobs_owner_user_id",
        "image_generation_jobs",
        ["owner_user_id"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_project_id",
        "image_generation_jobs",
        ["project_id"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_parent_version_id",
        "image_generation_jobs",
        ["parent_version_id"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_output_asset_id",
        "image_generation_jobs",
        ["output_asset_id"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_output_version_id",
        "image_generation_jobs",
        ["output_version_id"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_owner_created",
        "image_generation_jobs",
        ["owner_user_id", "created_at"],
        unique=False,
    )
    op.create_index(
        "ix_image_generation_jobs_project_status",
        "image_generation_jobs",
        ["project_id", "status"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index("ix_image_generation_jobs_project_status", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_owner_created", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_output_version_id", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_output_asset_id", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_parent_version_id", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_project_id", table_name="image_generation_jobs")
    op.drop_index("ix_image_generation_jobs_owner_user_id", table_name="image_generation_jobs")
    op.drop_table("image_generation_jobs")
