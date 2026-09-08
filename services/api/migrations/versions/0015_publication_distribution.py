"""Add classroom membership and publication delivery snapshots.

Revision ID: 0015_publication_distribution
Revises: 0014_creation_export_jobs
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0015_publication_distribution"
down_revision: str | None = "0014_creation_export_jobs"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "classrooms",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("owner_teacher_user_id", sa.Uuid(), nullable=False),
        sa.Column("name", sa.String(length=80), nullable=False),
        sa.Column("join_code_hash", sa.String(length=64), nullable=False),
        sa.Column("status", sa.Enum("ACTIVE", "ARCHIVED", name="classroomstatus", native_enum=False, length=16), nullable=False),
        sa.Column("row_version", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["owner_teacher_user_id"], ["users.id"], name=op.f("fk_classrooms_owner_teacher_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_classrooms")),
    )
    op.create_index(op.f("ix_classrooms_owner_teacher_user_id"), "classrooms", ["owner_teacher_user_id"])
    op.create_index(op.f("ix_classrooms_join_code_hash"), "classrooms", ["join_code_hash"], unique=True)

    op.create_table(
        "classroom_memberships",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("classroom_id", sa.Uuid(), nullable=False),
        sa.Column("student_user_id", sa.Uuid(), nullable=False),
        sa.Column("status", sa.Enum("ACTIVE", "LEFT", name="classroommembershipstatus", native_enum=False, length=16), nullable=False),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("left_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["classroom_id"], ["classrooms.id"], name=op.f("fk_classroom_memberships_classroom_id_classrooms"), ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["student_user_id"], ["users.id"], name=op.f("fk_classroom_memberships_student_user_id_users"), ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_classroom_memberships")),
        sa.UniqueConstraint("classroom_id", "student_user_id", name="uq_classroom_memberships_classroom_student"),
    )
    op.create_index(op.f("ix_classroom_memberships_classroom_id"), "classroom_memberships", ["classroom_id"])
    op.create_index(op.f("ix_classroom_memberships_student_user_id"), "classroom_memberships", ["student_user_id"])
    op.create_index("ix_classroom_memberships_student_status", "classroom_memberships", ["student_user_id", "status"])

    op.add_column("publications", sa.Column("classroom_id", sa.Uuid(), nullable=True))
    op.create_foreign_key(op.f("fk_publications_classroom_id_classrooms"), "publications", "classrooms", ["classroom_id"], ["id"], ondelete="SET NULL")
    op.create_index(op.f("ix_publications_classroom_id"), "publications", ["classroom_id"])

    op.create_table(
        "publication_deliveries",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("publication_id", sa.Uuid(), nullable=False),
        sa.Column("channel", sa.Enum("GUARDIAN", "CLASSROOM", name="publicationdeliverychannel", native_enum=False, length=16), nullable=False),
        sa.Column("recipient_user_id", sa.Uuid(), nullable=True),
        sa.Column("recipient_lookup_hash", sa.String(length=64), nullable=True),
        sa.Column("classroom_id", sa.Uuid(), nullable=True),
        sa.Column("status", sa.Enum("ACTIVE", "REVOKED", name="publicationdeliverystatus", native_enum=False, length=16), nullable=False),
        sa.Column("delivered_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True),
        sa.CheckConstraint("(channel = 'GUARDIAN' AND recipient_lookup_hash IS NOT NULL AND classroom_id IS NULL) OR (channel = 'CLASSROOM' AND recipient_lookup_hash IS NULL AND classroom_id IS NOT NULL)", name=op.f("ck_publication_deliveries_valid_delivery_target")),
        sa.ForeignKeyConstraint(["publication_id"], ["publications.id"], name=op.f("fk_publication_deliveries_publication_id_publications"), ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["recipient_user_id"], ["users.id"], name=op.f("fk_publication_deliveries_recipient_user_id_users"), ondelete="SET NULL"),
        sa.ForeignKeyConstraint(["classroom_id"], ["classrooms.id"], name=op.f("fk_publication_deliveries_classroom_id_classrooms"), ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_publication_deliveries")),
        sa.UniqueConstraint("publication_id", "channel", "recipient_lookup_hash", "classroom_id", name="uq_publication_deliveries_target"),
    )
    for column in ("publication_id", "recipient_user_id", "recipient_lookup_hash", "classroom_id"):
        op.create_index(op.f(f"ix_publication_deliveries_{column}"), "publication_deliveries", [column])
    op.create_index("ix_publication_deliveries_recipient_status", "publication_deliveries", ["recipient_user_id", "status"])
    op.create_index("ix_publication_deliveries_lookup_status", "publication_deliveries", ["recipient_lookup_hash", "status"])


def downgrade() -> None:
    op.drop_table("publication_deliveries")
    op.drop_index(op.f("ix_publications_classroom_id"), table_name="publications")
    op.drop_constraint(op.f("fk_publications_classroom_id_classrooms"), "publications", type_="foreignkey")
    op.drop_column("publications", "classroom_id")
    op.drop_table("classroom_memberships")
    op.drop_table("classrooms")
