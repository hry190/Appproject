"""Add creation projects, immutable versions, learning cards, and provenance.

Revision ID: 0006_creations_and_provenance
Revises: 0005_mistakes_and_remediation
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0006_creations_and_provenance"
down_revision: str | None = "0005_mistakes_and_remediation"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table('creation_projects',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('owner_user_id', sa.Uuid(), nullable=False),
    sa.Column('title', sa.String(length=100), nullable=False),
    sa.Column('description', sa.Text(), nullable=True),
    sa.Column('media_type', sa.Enum('ILLUSTRATION', 'COMIC', 'MIXED_MEDIA', name='creationmediatype', native_enum=False, length=20), nullable=False),
    sa.Column('status', sa.Enum('ACTIVE', 'ARCHIVED', 'DELETED', name='creationprojectstatus', native_enum=False, length=16), nullable=False),
    sa.Column('default_visibility', sa.Enum('PRIVATE', 'GUARDIAN_ONLY', 'CLASSROOM', 'COMMUNITY', name='creationvisibility', native_enum=False, length=20), nullable=False),
    sa.Column('current_version_number', sa.Integer(), nullable=True),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.CheckConstraint('current_version_number IS NULL OR current_version_number >= 1', name=op.f('ck_creation_projects_current_version_number_positive')),
    sa.ForeignKeyConstraint(['owner_user_id'], ['users.id'], name=op.f('fk_creation_projects_owner_user_id_users'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_creation_projects'))
    )
    op.create_index('ix_creation_projects_owner_updated', 'creation_projects', ['owner_user_id', 'updated_at'], unique=False)
    op.create_index(op.f('ix_creation_projects_owner_user_id'), 'creation_projects', ['owner_user_id'], unique=False)
    op.create_table('creation_versions',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('project_id', sa.Uuid(), nullable=False),
    sa.Column('version_number', sa.Integer(), nullable=False),
    sa.Column('parent_version_id', sa.Uuid(), nullable=True),
    sa.Column('created_by_user_id', sa.Uuid(), nullable=False),
    sa.Column('layer_manifest', sa.JSON(), nullable=False),
    sa.Column('layer_count', sa.Integer(), nullable=False),
    sa.Column('canvas_width', sa.Integer(), nullable=False),
    sa.Column('canvas_height', sa.Integer(), nullable=False),
    sa.Column('preview_asset_id', sa.Uuid(), nullable=True),
    sa.Column('change_summary', sa.String(length=500), nullable=False),
    sa.Column('modification_reason', sa.String(length=500), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.CheckConstraint('canvas_height > 0', name=op.f('ck_creation_versions_canvas_height_positive')),
    sa.CheckConstraint('canvas_width > 0', name=op.f('ck_creation_versions_canvas_width_positive')),
    sa.CheckConstraint('layer_count >= 1', name=op.f('ck_creation_versions_layer_count_positive')),
    sa.CheckConstraint('version_number >= 1', name=op.f('ck_creation_versions_version_number_positive')),
    sa.ForeignKeyConstraint(['created_by_user_id'], ['users.id'], name=op.f('fk_creation_versions_created_by_user_id_users'), ondelete='RESTRICT'),
    sa.ForeignKeyConstraint(['parent_version_id'], ['creation_versions.id'], name=op.f('fk_creation_versions_parent_version_id_creation_versions'), ondelete='RESTRICT'),
    sa.ForeignKeyConstraint(['project_id'], ['creation_projects.id'], name=op.f('fk_creation_versions_project_id_creation_projects'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_creation_versions')),
    sa.UniqueConstraint('project_id', 'version_number', name='uq_creation_versions_project_version')
    )
    op.create_index(op.f('ix_creation_versions_created_by_user_id'), 'creation_versions', ['created_by_user_id'], unique=False)
    op.create_index(op.f('ix_creation_versions_parent_version_id'), 'creation_versions', ['parent_version_id'], unique=False)
    op.create_index(op.f('ix_creation_versions_preview_asset_id'), 'creation_versions', ['preview_asset_id'], unique=False)
    op.create_index('ix_creation_versions_project_created', 'creation_versions', ['project_id', 'created_at'], unique=False)
    op.create_index(op.f('ix_creation_versions_project_id'), 'creation_versions', ['project_id'], unique=False)
    op.create_table('creation_change_logs',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('project_id', sa.Uuid(), nullable=False),
    sa.Column('version_id', sa.Uuid(), nullable=True),
    sa.Column('actor_user_id', sa.Uuid(), nullable=False),
    sa.Column('action', sa.Enum('PROJECT_CREATED', 'PROJECT_METADATA_UPDATED', 'VERSION_CREATED', 'LEARNING_CARD_UPDATED', 'PROVENANCE_UPDATED', 'SUBMITTED', name='creationchangeaction', native_enum=False, length=32), nullable=False),
    sa.Column('summary', sa.String(length=500), nullable=False),
    sa.Column('details', sa.JSON(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['actor_user_id'], ['users.id'], name=op.f('fk_creation_change_logs_actor_user_id_users'), ondelete='RESTRICT'),
    sa.ForeignKeyConstraint(['project_id'], ['creation_projects.id'], name=op.f('fk_creation_change_logs_project_id_creation_projects'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['version_id'], ['creation_versions.id'], name=op.f('fk_creation_change_logs_version_id_creation_versions'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_creation_change_logs'))
    )
    op.create_index(op.f('ix_creation_change_logs_actor_user_id'), 'creation_change_logs', ['actor_user_id'], unique=False)
    op.create_index('ix_creation_change_logs_project_created', 'creation_change_logs', ['project_id', 'created_at'], unique=False)
    op.create_index(op.f('ix_creation_change_logs_project_id'), 'creation_change_logs', ['project_id'], unique=False)
    op.create_index(op.f('ix_creation_change_logs_version_id'), 'creation_change_logs', ['version_id'], unique=False)
    op.create_table('learning_cards',
    sa.Column('creation_version_id', sa.Uuid(), nullable=False),
    sa.Column('method_summary', sa.Text(), nullable=False),
    sa.Column('unresolved_questions', sa.JSON(), nullable=False),
    sa.Column('questions_confirmed', sa.Boolean(), nullable=False),
    sa.Column('status', sa.Enum('DRAFT', 'COMPLETE', 'LOCKED', name='learningcardstatus', native_enum=False, length=16), nullable=False),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('locked_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['creation_version_id'], ['creation_versions.id'], name=op.f('fk_learning_cards_creation_version_id_creation_versions'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('creation_version_id', name=op.f('pk_learning_cards'))
    )
    op.create_table('provenance_manifests',
    sa.Column('creation_version_id', sa.Uuid(), nullable=False),
    sa.Column('human_contribution_summary', sa.Text(), nullable=False),
    sa.Column('ai_assistance_used', sa.Boolean(), nullable=False),
    sa.Column('ai_contribution_summary', sa.Text(), nullable=True),
    sa.Column('aigc_label_declared', sa.Boolean(), nullable=False),
    sa.Column('unresolved_rights', sa.Boolean(), nullable=False),
    sa.Column('status', sa.Enum('DRAFT', 'COMPLETE', 'LOCKED', name='provenancestatus', native_enum=False, length=16), nullable=False),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('locked_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['creation_version_id'], ['creation_versions.id'], name=op.f('fk_provenance_manifests_creation_version_id_creation_versions'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('creation_version_id', name=op.f('pk_provenance_manifests'))
    )
    op.create_table('publications',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('project_id', sa.Uuid(), nullable=False),
    sa.Column('creation_version_id', sa.Uuid(), nullable=False),
    sa.Column('owner_user_id', sa.Uuid(), nullable=False),
    sa.Column('status', sa.Enum('PENDING_CHECK', 'PENDING_HUMAN_REVIEW', 'PUBLISHED', 'RETURNED', 'RESTRICTED', 'WITHDRAWN', name='publicationstatus', native_enum=False, length=24), nullable=False),
    sa.Column('visibility', sa.Enum('PRIVATE', 'GUARDIAN_ONLY', 'CLASSROOM', 'COMMUNITY', name='creationvisibility', native_enum=False, length=20), nullable=False),
    sa.Column('idempotency_key', sa.String(length=64), nullable=False),
    sa.Column('request_fingerprint', sa.String(length=64), nullable=False),
    sa.Column('return_reason_code', sa.String(length=80), nullable=True),
    sa.Column('return_reason_summary', sa.String(length=500), nullable=True),
    sa.Column('submitted_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('published_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('returned_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('withdrawn_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['creation_version_id'], ['creation_versions.id'], name=op.f('fk_publications_creation_version_id_creation_versions'), ondelete='RESTRICT'),
    sa.ForeignKeyConstraint(['owner_user_id'], ['users.id'], name=op.f('fk_publications_owner_user_id_users'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['project_id'], ['creation_projects.id'], name=op.f('fk_publications_project_id_creation_projects'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_publications')),
    sa.UniqueConstraint('owner_user_id', 'idempotency_key', name='uq_publications_owner_idempotency')
    )
    op.create_index(op.f('ix_publications_creation_version_id'), 'publications', ['creation_version_id'], unique=True)
    op.create_index('ix_publications_owner_status_updated', 'publications', ['owner_user_id', 'status', 'updated_at'], unique=False)
    op.create_index(op.f('ix_publications_owner_user_id'), 'publications', ['owner_user_id'], unique=False)
    op.create_index(op.f('ix_publications_project_id'), 'publications', ['project_id'], unique=False)
    op.create_table('learning_card_manuals',
    sa.Column('creation_version_id', sa.Uuid(), nullable=False),
    sa.Column('manual_page_id', sa.Uuid(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['creation_version_id'], ['learning_cards.creation_version_id'], name=op.f('fk_learning_card_manuals_creation_version_id_learning_cards'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['manual_page_id'], ['manual_pages.id'], name=op.f('fk_learning_card_manuals_manual_page_id_manual_pages'), ondelete='RESTRICT'),
    sa.PrimaryKeyConstraint('creation_version_id', 'manual_page_id', name=op.f('pk_learning_card_manuals'))
    )
    op.create_table('provenance_items',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('creation_version_id', sa.Uuid(), nullable=False),
    sa.Column('item_type', sa.Enum('HUMAN_CONTRIBUTION', 'AI_CONTRIBUTION', 'EXTERNAL_MATERIAL', name='provenanceitemtype', native_enum=False, length=24), nullable=False),
    sa.Column('contribution_type', sa.String(length=80), nullable=False),
    sa.Column('description', sa.String(length=500), nullable=False),
    sa.Column('source_url', sa.String(length=500), nullable=True),
    sa.Column('source_author', sa.String(length=100), nullable=True),
    sa.Column('license_type', sa.Enum('ORIGINAL', 'CC0', 'CC_BY', 'CC_BY_SA', 'PUBLIC_DOMAIN', 'AUTHORIZED', 'UNKNOWN', 'NOT_APPLICABLE', name='materiallicensetype', native_enum=False, length=24), nullable=False),
    sa.Column('authorization_asset_id', sa.Uuid(), nullable=True),
    sa.Column('ai_provider', sa.String(length=80), nullable=True),
    sa.Column('ai_model', sa.String(length=120), nullable=True),
    sa.Column('ai_tool_action', sa.String(length=120), nullable=True),
    sa.Column('prompt_summary', sa.String(length=500), nullable=True),
    sa.Column('output_asset_id', sa.Uuid(), nullable=True),
    sa.Column('user_modified', sa.Boolean(), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['creation_version_id'], ['provenance_manifests.creation_version_id'], name=op.f('fk_provenance_items_creation_version_id_provenance_manifests'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_provenance_items'))
    )
    op.create_index(op.f('ix_provenance_items_creation_version_id'), 'provenance_items', ['creation_version_id'], unique=False)


def downgrade() -> None:
    op.drop_index(op.f('ix_provenance_items_creation_version_id'), table_name='provenance_items')
    op.drop_table('provenance_items')
    op.drop_table('learning_card_manuals')
    op.drop_index(op.f('ix_publications_project_id'), table_name='publications')
    op.drop_index(op.f('ix_publications_owner_user_id'), table_name='publications')
    op.drop_index('ix_publications_owner_status_updated', table_name='publications')
    op.drop_index(op.f('ix_publications_creation_version_id'), table_name='publications')
    op.drop_table('publications')
    op.drop_table('provenance_manifests')
    op.drop_table('learning_cards')
    op.drop_index(op.f('ix_creation_change_logs_version_id'), table_name='creation_change_logs')
    op.drop_index(op.f('ix_creation_change_logs_project_id'), table_name='creation_change_logs')
    op.drop_index('ix_creation_change_logs_project_created', table_name='creation_change_logs')
    op.drop_index(op.f('ix_creation_change_logs_actor_user_id'), table_name='creation_change_logs')
    op.drop_table('creation_change_logs')
    op.drop_index(op.f('ix_creation_versions_project_id'), table_name='creation_versions')
    op.drop_index('ix_creation_versions_project_created', table_name='creation_versions')
    op.drop_index(op.f('ix_creation_versions_preview_asset_id'), table_name='creation_versions')
    op.drop_index(op.f('ix_creation_versions_parent_version_id'), table_name='creation_versions')
    op.drop_index(op.f('ix_creation_versions_created_by_user_id'), table_name='creation_versions')
    op.drop_table('creation_versions')
    op.drop_index(op.f('ix_creation_projects_owner_user_id'), table_name='creation_projects')
    op.drop_index('ix_creation_projects_owner_updated', table_name='creation_projects')
    op.drop_table('creation_projects')
