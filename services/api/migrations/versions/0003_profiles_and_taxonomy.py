"""Add profiles, titles, badges, manual taxonomy, and favorites.

Revision ID: 0003_profiles_and_taxonomy
Revises: 0002_user_settings
"""
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0003_profiles_and_taxonomy"
down_revision: str | None = "0002_user_settings"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    profile_visibility = sa.Enum(
        "PRIVATE",
        "GUARDIAN_ONLY",
        "CLASSROOM",
        "COMMUNITY",
        name="profilevisibility",
        native_enum=False,
        length=20,
    )
    manual_content_status = sa.Enum(
        "OUTLINE",
        "READY",
        "ARCHIVED",
        name="manualcontentstatus",
        native_enum=False,
        length=16,
    )

    op.create_table(
        "title_definitions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("code", sa.String(length=40), nullable=False),
        sa.Column("name", sa.String(length=40), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("unlock_rule_version", sa.String(length=32), nullable=False),
        sa.Column("is_active", sa.Boolean(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_title_definitions")),
        sa.UniqueConstraint("code", name=op.f("uq_title_definitions_code")),
    )
    op.create_table(
        "badge_definitions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("code", sa.String(length=40), nullable=False),
        sa.Column("name", sa.String(length=40), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("unlock_rule_version", sa.String(length=32), nullable=False),
        sa.Column("is_active", sa.Boolean(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_badge_definitions")),
        sa.UniqueConstraint("code", name=op.f("uq_badge_definitions_code")),
    )
    op.create_table(
        "manual_volumes",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("number", sa.Integer(), nullable=False),
        sa.Column("code", sa.String(length=40), nullable=False),
        sa.Column("title", sa.String(length=80), nullable=False),
        sa.Column("core_domain", sa.String(length=100), nullable=False),
        sa.Column("art_style", sa.String(length=40), nullable=False),
        sa.Column("start_page", sa.Integer(), nullable=False),
        sa.Column("end_page", sa.Integer(), nullable=False),
        sa.Column("is_listed", sa.Boolean(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "number BETWEEN 1 AND 10",
            name=op.f("ck_manual_volumes_number_range"),
        ),
        sa.CheckConstraint(
            "start_page <= end_page",
            name=op.f("ck_manual_volumes_page_range_order"),
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_manual_volumes")),
        sa.UniqueConstraint("code", name=op.f("uq_manual_volumes_code")),
        sa.UniqueConstraint("number", name=op.f("uq_manual_volumes_number")),
    )
    op.create_table(
        "manual_pages",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("volume_id", sa.Uuid(), nullable=False),
        sa.Column("page_no", sa.Integer(), nullable=False),
        sa.Column("style_no", sa.Integer(), nullable=False),
        sa.Column("slug", sa.String(length=80), nullable=False),
        sa.Column("title", sa.String(length=80), nullable=False),
        sa.Column("core_logic", sa.Text(), nullable=False),
        sa.Column("life_hook", sa.Text(), nullable=False),
        sa.Column("interaction_evidence", sa.Text(), nullable=False),
        sa.Column("content_version", sa.String(length=32), nullable=False),
        sa.Column("content_status", manual_content_status, nullable=False),
        sa.Column("is_listed", sa.Boolean(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "page_no BETWEEN 1 AND 50",
            name=op.f("ck_manual_pages_page_no_range"),
        ),
        sa.CheckConstraint(
            "style_no BETWEEN 1 AND 5",
            name=op.f("ck_manual_pages_style_no_range"),
        ),
        sa.ForeignKeyConstraint(
            ["volume_id"],
            ["manual_volumes.id"],
            name=op.f("fk_manual_pages_volume_id_manual_volumes"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_manual_pages")),
        sa.UniqueConstraint("page_no", name=op.f("uq_manual_pages_page_no")),
        sa.UniqueConstraint("slug", name=op.f("uq_manual_pages_slug")),
        sa.UniqueConstraint(
            "volume_id",
            "style_no",
            name="uq_manual_pages_volume_style",
        ),
    )
    op.create_index(
        op.f("ix_manual_pages_volume_id"),
        "manual_pages",
        ["volume_id"],
        unique=False,
    )
    op.create_table(
        "user_profiles",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("anonymous_id", sa.String(length=16), nullable=False),
        sa.Column("avatar_asset_id", sa.Uuid()),
        sa.Column("class_label", sa.String(length=20)),
        sa.Column("current_title_id", sa.Uuid()),
        sa.Column("profile_visibility", profile_visibility, nullable=False),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["current_title_id"],
            ["title_definitions.id"],
            name=op.f("fk_user_profiles_current_title_id_title_definitions"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_profiles_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("user_id", name=op.f("pk_user_profiles")),
        sa.UniqueConstraint(
            "anonymous_id", name=op.f("uq_user_profiles_anonymous_id")
        ),
    )
    op.create_index(
        op.f("ix_user_profiles_avatar_asset_id"),
        "user_profiles",
        ["avatar_asset_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_user_profiles_current_title_id"),
        "user_profiles",
        ["current_title_id"],
        unique=False,
    )
    op.create_table(
        "user_titles",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("title_id", sa.Uuid(), nullable=False),
        sa.Column("evidence_ref", sa.String(length=80)),
        sa.Column("earned_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["title_id"],
            ["title_definitions.id"],
            name=op.f("fk_user_titles_title_id_title_definitions"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_titles_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_user_titles")),
        sa.UniqueConstraint(
            "user_id", "title_id", name="uq_user_titles_user_title"
        ),
    )
    op.create_index(
        op.f("ix_user_titles_title_id"), "user_titles", ["title_id"], unique=False
    )
    op.create_index(
        op.f("ix_user_titles_user_id"), "user_titles", ["user_id"], unique=False
    )
    op.create_index(
        "ix_user_titles_user_earned",
        "user_titles",
        ["user_id", "earned_at"],
        unique=False,
    )
    op.create_table(
        "user_badges",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("badge_id", sa.Uuid(), nullable=False),
        sa.Column("evidence_ref", sa.String(length=80)),
        sa.Column("earned_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["badge_id"],
            ["badge_definitions.id"],
            name=op.f("fk_user_badges_badge_id_badge_definitions"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_badges_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_user_badges")),
        sa.UniqueConstraint(
            "user_id", "badge_id", name="uq_user_badges_user_badge"
        ),
    )
    op.create_index(
        op.f("ix_user_badges_badge_id"), "user_badges", ["badge_id"], unique=False
    )
    op.create_index(
        op.f("ix_user_badges_user_id"), "user_badges", ["user_id"], unique=False
    )
    op.create_index(
        "ix_user_badges_user_earned",
        "user_badges",
        ["user_id", "earned_at"],
        unique=False,
    )
    op.create_table(
        "user_manual_favorites",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("manual_page_id", sa.Uuid(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["manual_page_id"],
            ["manual_pages.id"],
            name=op.f("fk_user_manual_favorites_manual_page_id_manual_pages"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_manual_favorites_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_user_manual_favorites")),
        sa.UniqueConstraint(
            "user_id",
            "manual_page_id",
            name="uq_user_manual_favorites_user_page",
        ),
    )
    op.create_index(
        op.f("ix_user_manual_favorites_manual_page_id"),
        "user_manual_favorites",
        ["manual_page_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_user_manual_favorites_user_id"),
        "user_manual_favorites",
        ["user_id"],
        unique=False,
    )
    op.create_index(
        "ix_user_manual_favorites_user_created",
        "user_manual_favorites",
        ["user_id", "created_at"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_table("user_manual_favorites")
    op.drop_table("user_badges")
    op.drop_table("user_titles")
    op.drop_table("user_profiles")
    op.drop_table("manual_pages")
    op.drop_table("manual_volumes")
    op.drop_table("badge_definitions")
    op.drop_table("title_definitions")
