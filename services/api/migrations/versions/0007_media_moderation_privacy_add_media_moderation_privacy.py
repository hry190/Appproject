"""Add isolated media processing, moderation, appeals, audit, and privacy.

Revision ID: 0007_media_moderation_privacy
Revises: 0006_creations_and_provenance
"""
from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa


revision: str = "0007_media_moderation_privacy"
down_revision: str | None = "0006_creations_and_provenance"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table('outbox_events',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('aggregate_type', sa.String(length=40), nullable=False),
    sa.Column('aggregate_id', sa.Uuid(), nullable=False),
    sa.Column('event_type', sa.String(length=60), nullable=False),
    sa.Column('payload', sa.JSON(), nullable=False),
    sa.Column('deduplication_key', sa.String(length=120), nullable=False),
    sa.Column('status', sa.Enum('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', name='outboxstatus', native_enum=False, length=16), nullable=False),
    sa.Column('attempts', sa.Integer(), nullable=False),
    sa.Column('available_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('locked_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('processed_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('last_error_code', sa.String(length=80), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_outbox_events')),
    sa.UniqueConstraint('deduplication_key', name='uq_outbox_events_deduplication')
    )
    op.create_index(op.f('ix_outbox_events_aggregate_id'), 'outbox_events', ['aggregate_id'], unique=False)
    op.create_index(op.f('ix_outbox_events_event_type'), 'outbox_events', ['event_type'], unique=False)
    op.create_index('ix_outbox_events_status_available', 'outbox_events', ['status', 'available_at'], unique=False)
    op.create_table('domain_audit_events',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('actor_user_id', sa.Uuid(), nullable=True),
    sa.Column('actor_type', sa.String(length=24), nullable=False),
    sa.Column('action', sa.String(length=60), nullable=False),
    sa.Column('target_type', sa.String(length=40), nullable=False),
    sa.Column('target_id', sa.Uuid(), nullable=False),
    sa.Column('result', sa.String(length=20), nullable=False),
    sa.Column('request_id', sa.String(length=64), nullable=False),
    sa.Column('safe_diff', sa.JSON(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['actor_user_id'], ['users.id'], name=op.f('fk_domain_audit_events_actor_user_id_users'), ondelete='SET NULL'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_domain_audit_events'))
    )
    op.create_index(op.f('ix_domain_audit_events_action'), 'domain_audit_events', ['action'], unique=False)
    op.create_index(op.f('ix_domain_audit_events_actor_user_id'), 'domain_audit_events', ['actor_user_id'], unique=False)
    op.create_index(op.f('ix_domain_audit_events_target_id'), 'domain_audit_events', ['target_id'], unique=False)
    op.create_table('privacy_settings',
    sa.Column('user_id', sa.Uuid(), nullable=False),
    sa.Column('default_work_visibility', sa.Enum('PRIVATE', 'GUARDIAN_ONLY', 'CLASSROOM', 'COMMUNITY', name='creationvisibility', native_enum=False, length=20), nullable=False),
    sa.Column('learning_card_public', sa.Boolean(), nullable=False),
    sa.Column('aigc_export_mark_enabled', sa.Boolean(), nullable=False),
    sa.Column('profile_discovery_enabled', sa.Boolean(), nullable=False),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['user_id'], ['users.id'], name=op.f('fk_privacy_settings_user_id_users'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('user_id', name=op.f('pk_privacy_settings'))
    )
    op.create_table('upload_sessions',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('owner_user_id', sa.Uuid(), nullable=False),
    sa.Column('purpose', sa.Enum('CREATION_LAYER', 'CREATION_PREVIEW', 'PROVENANCE_PROOF', 'AIGC_OUTPUT', 'AVATAR', name='uploadpurpose', native_enum=False, length=24), nullable=False),
    sa.Column('original_filename', sa.String(length=180), nullable=False),
    sa.Column('declared_mime', sa.String(length=80), nullable=False),
    sa.Column('expected_bytes', sa.Integer(), nullable=False),
    sa.Column('client_sha256', sa.String(length=64), nullable=False),
    sa.Column('object_key', sa.String(length=300), nullable=False),
    sa.Column('status', sa.Enum('ISSUED', 'COMPLETED', 'EXPIRED', 'REJECTED', name='uploadsessionstatus', native_enum=False, length=16), nullable=False),
    sa.Column('complete_idempotency_key', sa.String(length=64), nullable=True),
    sa.Column('complete_fingerprint', sa.String(length=64), nullable=True),
    sa.Column('expires_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('completed_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.CheckConstraint('expected_bytes > 0', name=op.f('ck_upload_sessions_expected_bytes_positive')),
    sa.ForeignKeyConstraint(['owner_user_id'], ['users.id'], name=op.f('fk_upload_sessions_owner_user_id_users'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_upload_sessions')),
    sa.UniqueConstraint('owner_user_id', 'object_key', name='uq_upload_sessions_owner_object')
    )
    op.create_index('ix_upload_sessions_owner_status_created', 'upload_sessions', ['owner_user_id', 'status', 'created_at'], unique=False)
    op.create_index(op.f('ix_upload_sessions_owner_user_id'), 'upload_sessions', ['owner_user_id'], unique=False)
    op.create_table('media_assets',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('owner_user_id', sa.Uuid(), nullable=False),
    sa.Column('upload_session_id', sa.Uuid(), nullable=False),
    sa.Column('purpose', sa.Enum('CREATION_LAYER', 'CREATION_PREVIEW', 'PROVENANCE_PROOF', 'AIGC_OUTPUT', 'AVATAR', name='uploadpurpose', native_enum=False, length=24), nullable=False),
    sa.Column('original_filename', sa.String(length=180), nullable=False),
    sa.Column('declared_mime', sa.String(length=80), nullable=False),
    sa.Column('actual_mime', sa.String(length=80), nullable=True),
    sa.Column('byte_size', sa.Integer(), nullable=False),
    sa.Column('sha256', sa.String(length=64), nullable=False),
    sa.Column('quarantine_object_key', sa.String(length=300), nullable=False),
    sa.Column('private_object_key', sa.String(length=300), nullable=True),
    sa.Column('status', sa.Enum('QUARANTINED', 'PROCESSING', 'READY', 'REJECTED', 'DELETION_PENDING', 'DELETED', name='mediaassetstatus', native_enum=False, length=24), nullable=False),
    sa.Column('width', sa.Integer(), nullable=True),
    sa.Column('height', sa.Integer(), nullable=True),
    sa.Column('metadata_stripped', sa.Boolean(), nullable=True),
    sa.Column('aigc_detected', sa.Boolean(), nullable=True),
    sa.Column('rejection_code', sa.String(length=80), nullable=True),
    sa.Column('rejection_summary', sa.String(length=500), nullable=True),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('ready_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.CheckConstraint('byte_size > 0', name=op.f('ck_media_assets_byte_size_positive')),
    sa.ForeignKeyConstraint(['owner_user_id'], ['users.id'], name=op.f('fk_media_assets_owner_user_id_users'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['upload_session_id'], ['upload_sessions.id'], name=op.f('fk_media_assets_upload_session_id_upload_sessions'), ondelete='RESTRICT'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_media_assets'))
    )
    op.create_index('ix_media_assets_owner_status_updated', 'media_assets', ['owner_user_id', 'status', 'updated_at'], unique=False)
    op.create_index(op.f('ix_media_assets_owner_user_id'), 'media_assets', ['owner_user_id'], unique=False)
    op.create_index(op.f('ix_media_assets_upload_session_id'), 'media_assets', ['upload_session_id'], unique=True)
    op.create_table('media_derivatives',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('asset_id', sa.Uuid(), nullable=False),
    sa.Column('kind', sa.Enum('SANITIZED_ORIGINAL', 'THUMBNAIL_320', 'THUMBNAIL_640', name='mediaderivativekind', native_enum=False, length=24), nullable=False),
    sa.Column('mime_type', sa.String(length=80), nullable=False),
    sa.Column('byte_size', sa.Integer(), nullable=False),
    sa.Column('storage_key', sa.String(length=300), nullable=False),
    sa.Column('width', sa.Integer(), nullable=False),
    sa.Column('height', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.CheckConstraint('byte_size > 0', name=op.f('ck_media_derivatives_derivative_byte_size_positive')),
    sa.ForeignKeyConstraint(['asset_id'], ['media_assets.id'], name=op.f('fk_media_derivatives_asset_id_media_assets'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_media_derivatives')),
    sa.UniqueConstraint('asset_id', 'kind', name='uq_media_derivatives_asset_kind'),
    sa.UniqueConstraint('storage_key', name=op.f('uq_media_derivatives_storage_key'))
    )
    op.create_index(op.f('ix_media_derivatives_asset_id'), 'media_derivatives', ['asset_id'], unique=False)
    op.create_table('media_scan_results',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('asset_id', sa.Uuid(), nullable=False),
    sa.Column('scan_kind', sa.Enum('SIGNATURE', 'HASH', 'VIRUS', 'DECODE', 'PIXEL_LIMIT', 'METADATA', 'CONTENT_SAFETY', 'AIGC', name='mediascankind', native_enum=False, length=24), nullable=False),
    sa.Column('outcome', sa.Enum('PASSED', 'FAILED', 'REVIEW', 'NOT_RUN', name='mediascanoutcome', native_enum=False, length=16), nullable=False),
    sa.Column('reason_code', sa.String(length=80), nullable=True),
    sa.Column('detector_version', sa.String(length=80), nullable=False),
    sa.Column('details', sa.JSON(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['asset_id'], ['media_assets.id'], name=op.f('fk_media_scan_results_asset_id_media_assets'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_media_scan_results'))
    )
    op.create_index(op.f('ix_media_scan_results_asset_id'), 'media_scan_results', ['asset_id'], unique=False)
    op.create_table('moderation_cases',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('publication_id', sa.Uuid(), nullable=False),
    sa.Column('owner_user_id', sa.Uuid(), nullable=False),
    sa.Column('status', sa.Enum('AUTO_CHECK', 'HUMAN_REVIEW', 'RESOLVED', name='moderationcasestatus', native_enum=False, length=20), nullable=False),
    sa.Column('risk_level', sa.Enum('LOW', 'MEDIUM', 'HIGH', name='moderationrisklevel', native_enum=False, length=12), nullable=True),
    sa.Column('automatic_reason_codes', sa.JSON(), nullable=False),
    sa.Column('detector_version', sa.String(length=80), nullable=True),
    sa.Column('decision', sa.Enum('PUBLISH', 'RETURN', 'RESTRICT', name='moderationdecision', native_enum=False, length=12), nullable=True),
    sa.Column('public_reason_code', sa.String(length=80), nullable=True),
    sa.Column('public_reason_summary', sa.String(length=500), nullable=True),
    sa.Column('revision_suggestion', sa.String(length=500), nullable=True),
    sa.Column('minimal_evidence', sa.JSON(), nullable=False),
    sa.Column('reviewer_reference', sa.String(length=80), nullable=True),
    sa.Column('reviewed_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('row_version', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False),
    sa.ForeignKeyConstraint(['owner_user_id'], ['users.id'], name=op.f('fk_moderation_cases_owner_user_id_users'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['publication_id'], ['publications.id'], name=op.f('fk_moderation_cases_publication_id_publications'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_moderation_cases'))
    )
    op.create_index('ix_moderation_cases_owner_status_updated', 'moderation_cases', ['owner_user_id', 'status', 'updated_at'], unique=False)
    op.create_index(op.f('ix_moderation_cases_owner_user_id'), 'moderation_cases', ['owner_user_id'], unique=False)
    op.create_index(op.f('ix_moderation_cases_publication_id'), 'moderation_cases', ['publication_id'], unique=True)
    op.create_table('moderation_appeals',
    sa.Column('id', sa.Uuid(), nullable=False),
    sa.Column('moderation_case_id', sa.Uuid(), nullable=False),
    sa.Column('appellant_user_id', sa.Uuid(), nullable=False),
    sa.Column('reason', sa.Text(), nullable=False),
    sa.Column('status', sa.Enum('PENDING', 'UPHELD', 'OVERTURNED', name='appealstatus', native_enum=False, length=16), nullable=False),
    sa.Column('reviewer_reference', sa.String(length=80), nullable=True),
    sa.Column('resolution_summary', sa.String(length=500), nullable=True),
    sa.Column('created_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('resolved_at', sa.DateTime(timezone=True), nullable=True),
    sa.ForeignKeyConstraint(['appellant_user_id'], ['users.id'], name=op.f('fk_moderation_appeals_appellant_user_id_users'), ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['moderation_case_id'], ['moderation_cases.id'], name=op.f('fk_moderation_appeals_moderation_case_id_moderation_cases'), ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id', name=op.f('pk_moderation_appeals'))
    )
    op.create_index(op.f('ix_moderation_appeals_appellant_user_id'), 'moderation_appeals', ['appellant_user_id'], unique=False)
    op.create_index('ix_moderation_appeals_case_status_created', 'moderation_appeals', ['moderation_case_id', 'status', 'created_at'], unique=False)
    op.create_index(op.f('ix_moderation_appeals_moderation_case_id'), 'moderation_appeals', ['moderation_case_id'], unique=False)


def downgrade() -> None:
    op.drop_index(op.f('ix_moderation_appeals_moderation_case_id'), table_name='moderation_appeals')
    op.drop_index('ix_moderation_appeals_case_status_created', table_name='moderation_appeals')
    op.drop_index(op.f('ix_moderation_appeals_appellant_user_id'), table_name='moderation_appeals')
    op.drop_table('moderation_appeals')
    op.drop_index(op.f('ix_moderation_cases_publication_id'), table_name='moderation_cases')
    op.drop_index(op.f('ix_moderation_cases_owner_user_id'), table_name='moderation_cases')
    op.drop_index('ix_moderation_cases_owner_status_updated', table_name='moderation_cases')
    op.drop_table('moderation_cases')
    op.drop_index(op.f('ix_media_scan_results_asset_id'), table_name='media_scan_results')
    op.drop_table('media_scan_results')
    op.drop_index(op.f('ix_media_derivatives_asset_id'), table_name='media_derivatives')
    op.drop_table('media_derivatives')
    op.drop_index(op.f('ix_media_assets_upload_session_id'), table_name='media_assets')
    op.drop_index(op.f('ix_media_assets_owner_user_id'), table_name='media_assets')
    op.drop_index('ix_media_assets_owner_status_updated', table_name='media_assets')
    op.drop_table('media_assets')
    op.drop_index(op.f('ix_upload_sessions_owner_user_id'), table_name='upload_sessions')
    op.drop_index('ix_upload_sessions_owner_status_created', table_name='upload_sessions')
    op.drop_table('upload_sessions')
    op.drop_table('privacy_settings')
    op.drop_index(op.f('ix_domain_audit_events_target_id'), table_name='domain_audit_events')
    op.drop_index(op.f('ix_domain_audit_events_actor_user_id'), table_name='domain_audit_events')
    op.drop_index(op.f('ix_domain_audit_events_action'), table_name='domain_audit_events')
    op.drop_table('domain_audit_events')
    op.drop_index('ix_outbox_events_status_available', table_name='outbox_events')
    op.drop_index(op.f('ix_outbox_events_event_type'), table_name='outbox_events')
    op.drop_index(op.f('ix_outbox_events_aggregate_id'), table_name='outbox_events')
    op.drop_table('outbox_events')
