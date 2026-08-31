"""Create authentication tables.

Revision ID: 0001_auth
Revises:
"""
from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0001_auth"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    age_band = sa.Enum("UNDER_14", "AGE_14_TO_17", "ADULT", name="ageband", native_enum=False)
    user_status = sa.Enum("ACTIVE", "LOCKED", "DELETED", name="userstatus", native_enum=False)
    guardian_status = sa.Enum("NOT_REQUIRED", "VERIFIED", name="guardianstatus", native_enum=False)
    consent_type = sa.Enum("TERMS", "PRIVACY", "GUARDIAN", name="consenttype", native_enum=False)
    consent_subject = sa.Enum("SELF", "GUARDIAN", name="consentsubject", native_enum=False)

    op.create_table(
        "users",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("phone_ciphertext", sa.Text(), nullable=False),
        sa.Column("phone_lookup_hash", sa.String(length=64), nullable=False),
        sa.Column("nickname", sa.String(length=40), nullable=False),
        sa.Column("status", user_status, nullable=False),
        sa.Column("age_band", age_band, nullable=False),
        sa.Column("guardian_status", guardian_status, nullable=False),
        sa.Column("token_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("password_changed_at", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_users")),
    )
    op.create_index(op.f("ix_users_phone_lookup_hash"), "users", ["phone_lookup_hash"], unique=True)

    op.create_table(
        "credentials",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("password_hash", sa.Text(), nullable=False),
        sa.Column("failed_attempts", sa.Integer(), nullable=False),
        sa.Column("locked_until", sa.DateTime(timezone=True)),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], name=op.f("fk_credentials_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("user_id", name=op.f("pk_credentials")),
    )

    op.create_table(
        "auth_sessions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("family_id", sa.Uuid(), nullable=False),
        sa.Column("parent_session_id", sa.Uuid()),
        sa.Column("refresh_token_hash", sa.String(length=64), nullable=False),
        sa.Column("device_name", sa.String(length=80)),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("last_seen_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("revoked_at", sa.DateTime(timezone=True)),
        sa.ForeignKeyConstraint(["parent_session_id"], ["auth_sessions.id"], name=op.f("fk_auth_sessions_parent_session_id_auth_sessions"), ondelete="SET NULL"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], name=op.f("fk_auth_sessions_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_auth_sessions")),
    )
    op.create_index(op.f("ix_auth_sessions_family_id"), "auth_sessions", ["family_id"], unique=False)
    op.create_index(op.f("ix_auth_sessions_refresh_token_hash"), "auth_sessions", ["refresh_token_hash"], unique=True)
    op.create_index(op.f("ix_auth_sessions_user_id"), "auth_sessions", ["user_id"], unique=False)

    op.create_table(
        "consent_records",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("consent_type", consent_type, nullable=False),
        sa.Column("document_version", sa.String(length=32), nullable=False),
        sa.Column("subject", consent_subject, nullable=False),
        sa.Column("evidence_id", sa.String(length=64), nullable=False),
        sa.Column("agreed_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], name=op.f("fk_consent_records_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_consent_records")),
    )
    op.create_index(op.f("ix_consent_records_user_id"), "consent_records", ["user_id"], unique=False)

    op.create_table(
        "guardian_links",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("child_user_id", sa.Uuid(), nullable=False),
        sa.Column("guardian_phone_hash", sa.String(length=64), nullable=False),
        sa.Column("consent_evidence_id", sa.String(length=64), nullable=False),
        sa.Column("verified_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["child_user_id"], ["users.id"], name=op.f("fk_guardian_links_child_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_guardian_links")),
        sa.UniqueConstraint("child_user_id", name=op.f("uq_guardian_links_child_user_id")),
    )
    op.create_index(op.f("ix_guardian_links_guardian_phone_hash"), "guardian_links", ["guardian_phone_hash"], unique=False)

    op.create_table(
        "auth_audit_events",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid()),
        sa.Column("phone_lookup_hash", sa.String(length=64)),
        sa.Column("event_type", sa.String(length=40), nullable=False),
        sa.Column("result", sa.String(length=20), nullable=False),
        sa.Column("request_id", sa.String(length=64), nullable=False),
        sa.Column("network_key", sa.String(length=64)),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], name=op.f("fk_auth_audit_events_user_id_users"), ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_auth_audit_events")),
    )
    op.create_index("ix_auth_audit_event_type_created", "auth_audit_events", ["event_type", "created_at"], unique=False)
    op.create_index(op.f("ix_auth_audit_events_phone_lookup_hash"), "auth_audit_events", ["phone_lookup_hash"], unique=False)
    op.create_index(op.f("ix_auth_audit_events_user_id"), "auth_audit_events", ["user_id"], unique=False)


def downgrade() -> None:
    op.drop_table("auth_audit_events")
    op.drop_table("guardian_links")
    op.drop_table("consent_records")
    op.drop_table("auth_sessions")
    op.drop_table("credentials")
    op.drop_table("users")
