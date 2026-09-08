from __future__ import annotations

import uuid

from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import PhoneProtector, utcnow
from app.domains.creations.models import (
    CreationIntent,
    CreationExportJob,
    CreationMethod,
    CreationProject,
    CreationSealCheck,
    CreationStageEvent,
    CreationTestIssue,
    CreationTestRecord,
    CreationToolCall,
    CreationVersion,
    ImageGenerationJob,
    IntentAnalysis,
    LearningCard,
    LearningCardManual,
    ProvenanceItem,
    ProvenanceManifest,
    Publication,
)
from app.domains.conference.models import (
    ConferenceCollection,
    ConferenceDerivativeAuthorization,
    ConferenceDerivativeRequest,
    ConferenceLetter,
    ConferenceMatch,
    ConferenceMatchAnswer,
    ConferenceMatchEvaluation,
    ConferenceMatchQueue,
    ConferenceMatchReflection,
    ConferenceMatchReport,
    ConferenceReview,
    ConferenceReviewReport,
)
from app.domains.media.models import MediaAsset, OutboxEvent, OutboxStatus
from app.domains.moderation.models import DomainAuditEvent, ModerationAppeal
from app.domains.privacy.models import PrivacySetting
from app.domains.profiles.models import ProfileVisibility, UserProfile
from app.models import (
    AgeBand,
    AuthSession,
    BlacklistEntry,
    ConsentRecord,
    ContentLevel,
    DataRequestStatus,
    DataRightsRequest,
    FeedbackTicket,
    GuardianControl,
    TicketStatus,
    User,
    UserPreference,
    UserStatus,
)
from app.schemas import (
    AccountExport,
    BlacklistEntryPublic,
    ConsentRecordPublic,
    DataRightsRequestCreate,
    DataRightsRequestPublic,
    FeedbackCreate,
    FeedbackPublic,
    GuardianControlsPatch,
    GuardianControlsPublic,
    SessionPublic,
    UserPreferencesPatch,
    UserPreferencesPublic,
    UserPublic,
)


class UserSettingsService:
    def __init__(
        self,
        *,
        db: Session,
        phone_protector: PhoneProtector,
    ) -> None:
        self.db = db
        self.phone = phone_protector

    def get_preferences(self, user: User) -> UserPreference:
        preferences = self.db.get(UserPreference, user.id)
        if preferences is None:
            preferences = UserPreference(user_id=user.id)
            self.db.add(preferences)
            self.db.commit()
            self.db.refresh(preferences)
        return preferences

    def update_preferences(
        self,
        user: User,
        payload: UserPreferencesPatch,
    ) -> UserPreference:
        preferences = self.get_preferences(user)
        updates = payload.model_dump(exclude_unset=True, exclude_none=True)
        if not updates:
            raise ApiError(422, "VALIDATION_ERROR", "请至少提交一项有效设置")
        for name, value in updates.items():
            setattr(preferences, name, value)
        preferences.updated_at = utcnow()
        self.db.commit()
        self.db.refresh(preferences)
        return preferences

    def get_guardian_controls(self, user: User) -> GuardianControl:
        self._require_minor(user)
        controls = self.db.get(GuardianControl, user.id)
        if controls is None:
            controls = GuardianControl(
                child_user_id=user.id,
                content_level=(
                    ContentLevel.CHILD
                    if user.age_band == AgeBand.UNDER_14
                    else ContentLevel.TEEN
                ),
            )
            self.db.add(controls)
            self.db.commit()
            self.db.refresh(controls)
        return controls

    def update_guardian_controls(
        self,
        user: User,
        payload: GuardianControlsPatch,
    ) -> GuardianControl:
        controls = self.get_guardian_controls(user)
        updates = payload.model_dump(exclude_unset=True, exclude_none=True)
        if not updates:
            raise ApiError(422, "VALIDATION_ERROR", "请至少提交一项有效监护设置")
        for name, value in updates.items():
            setattr(controls, name, value)
        controls.updated_at = utcnow()
        self.db.commit()
        self.db.refresh(controls)
        return controls

    def create_feedback(self, user: User, payload: FeedbackCreate) -> FeedbackTicket:
        ticket = FeedbackTicket(
            user_id=user.id,
            category=payload.category,
            message=payload.message,
            status=TicketStatus.OPEN,
        )
        self.db.add(ticket)
        self.db.commit()
        self.db.refresh(ticket)
        return ticket

    def list_blacklist(self, user: User) -> list[BlacklistEntryPublic]:
        rows = self.db.execute(
            select(BlacklistEntry, User)
            .join(User, User.id == BlacklistEntry.blocked_user_id)
            .where(BlacklistEntry.owner_user_id == user.id)
            .order_by(BlacklistEntry.created_at.desc())
        ).all()
        return [
            BlacklistEntryPublic(
                user_id=blocked.id,
                nickname=blocked.nickname,
                blocked_at=entry.created_at,
            )
            for entry, blocked in rows
        ]

    def add_to_blacklist(
        self,
        user: User,
        blocked_user_id: uuid.UUID,
    ) -> BlacklistEntryPublic:
        if blocked_user_id == user.id:
            raise ApiError(400, "CANNOT_BLOCK_SELF", "不能将自己加入黑名单")
        blocked = self.db.scalar(
            select(User).where(
                User.id == blocked_user_id,
                User.status == UserStatus.ACTIVE,
            )
        )
        if blocked is None:
            raise ApiError(404, "USER_NOT_FOUND", "未找到该用户")
        entry = self.db.scalar(
            select(BlacklistEntry).where(
                BlacklistEntry.owner_user_id == user.id,
                BlacklistEntry.blocked_user_id == blocked_user_id,
            )
        )
        if entry is None:
            entry = BlacklistEntry(
                owner_user_id=user.id,
                blocked_user_id=blocked_user_id,
            )
            self.db.add(entry)
            self.db.commit()
            self.db.refresh(entry)
        return BlacklistEntryPublic(
            user_id=blocked.id,
            nickname=blocked.nickname,
            blocked_at=entry.created_at,
        )

    def remove_from_blacklist(self, user: User, blocked_user_id: uuid.UUID) -> None:
        entry = self.db.scalar(
            select(BlacklistEntry).where(
                BlacklistEntry.owner_user_id == user.id,
                BlacklistEntry.blocked_user_id == blocked_user_id,
            )
        )
        if entry is not None:
            self.db.delete(entry)
            self.db.commit()

    def list_sessions(self, user: User) -> list[SessionPublic]:
        now = utcnow()
        sessions = self.db.scalars(
            select(AuthSession)
            .where(
                AuthSession.user_id == user.id,
                AuthSession.revoked_at.is_(None),
                AuthSession.expires_at > now,
            )
            .order_by(AuthSession.last_seen_at.desc())
        ).all()
        return [self._session_public(session) for session in sessions]

    def revoke_session(self, user: User, session_id: uuid.UUID) -> None:
        session = self.db.scalar(
            select(AuthSession).where(
                AuthSession.id == session_id,
                AuthSession.user_id == user.id,
            )
        )
        if session is None:
            raise ApiError(404, "SESSION_NOT_FOUND", "未找到该登录设备")
        if session.revoked_at is None:
            session.revoked_at = utcnow()
            self.db.commit()

    def create_data_request(
        self,
        user: User,
        payload: DataRightsRequestCreate,
    ) -> DataRightsRequest:
        existing = self.db.scalar(
            select(DataRightsRequest).where(
                DataRightsRequest.user_id == user.id,
                DataRightsRequest.request_type == payload.request_type,
                DataRightsRequest.status.in_(
                    [DataRequestStatus.PENDING, DataRequestStatus.PROCESSING]
                ),
            )
        )
        if existing is not None:
            return existing
        request = DataRightsRequest(
            user_id=user.id,
            request_type=payload.request_type,
            reason=payload.reason,
            status=DataRequestStatus.PENDING,
        )
        self.db.add(request)
        self.db.flush()
        if payload.request_type.value == "ACCOUNT_DELETION":
            self.db.add(
                OutboxEvent(
                    aggregate_type="data_rights_request",
                    aggregate_id=request.id,
                    event_type="ACCOUNT_DELETION_REQUESTED",
                    payload={"request_id": str(request.id), "user_id": str(user.id)},
                    deduplication_key=f"account-deletion:{request.id}",
                    status=OutboxStatus.PENDING,
                    available_at=utcnow(),
                )
            )
        self.db.commit()
        self.db.refresh(request)
        return request

    def process_account_deletion(self, request_id: uuid.UUID) -> uuid.UUID | None:
        request = self.db.scalar(
            select(DataRightsRequest).where(DataRightsRequest.id == request_id)
        )
        if request is None or request.request_type.value != "ACCOUNT_DELETION":
            return None
        user = self.db.scalar(select(User).where(User.id == request.user_id))
        if user is None:
            request.status = DataRequestStatus.COMPLETED
            self.db.commit()
            return None
        if request.status == DataRequestStatus.COMPLETED:
            return user.id

        request.status = DataRequestStatus.PROCESSING
        profile = self.db.scalar(select(UserProfile).where(UserProfile.user_id == user.id))
        opaque_phone = "+86" + "199" + str(uuid.uuid4().int)[-8:]
        user.phone_ciphertext = self.phone.encrypt(opaque_phone)
        user.phone_lookup_hash = self.phone.lookup_hash(opaque_phone)
        user.nickname = f"已注销用户-{str(user.id).split('-')[0]}"
        user.status = UserStatus.DELETED
        user.token_version += 1
        for session in self.db.scalars(
            select(AuthSession).where(
                AuthSession.user_id == user.id,
                AuthSession.revoked_at.is_(None),
            )
        ).all():
            session.revoked_at = utcnow()
        if profile is not None:
            profile.anonymous_id = f"JH-{str(uuid.uuid4()).replace('-', '').upper()[:8]}"
            profile.class_label = None
            profile.avatar_asset_id = None
            profile.profile_visibility = ProfileVisibility.PRIVATE
            profile.row_version += 1
        request.status = DataRequestStatus.COMPLETED
        self.db.commit()
        return user.id

    def list_data_requests(self, user: User) -> list[DataRightsRequest]:
        return list(
            self.db.scalars(
                select(DataRightsRequest)
                .where(DataRightsRequest.user_id == user.id)
                .order_by(DataRightsRequest.created_at.desc())
            ).all()
        )

    def export_account(self, user: User) -> AccountExport:
        normalized_phone = self.phone.decrypt(user.phone_ciphertext)
        controls = None
        if user.age_band != AgeBand.ADULT:
            controls = GuardianControlsPublic.model_validate(
                self.get_guardian_controls(user)
            )
        consents = self.db.scalars(
            select(ConsentRecord)
            .where(ConsentRecord.user_id == user.id)
            .order_by(ConsentRecord.agreed_at.asc())
        ).all()
        domain_data = self._export_domain_data(user)
        return AccountExport(
            generated_at=utcnow(),
            user=UserPublic(
                id=user.id,
                nickname=user.nickname,
                phone_masked=self.phone.mask(normalized_phone),
                status=user.status,
                age_band=user.age_band,
                guardian_status=user.guardian_status,
            ),
            preferences=UserPreferencesPublic.model_validate(
                self.get_preferences(user)
            ),
            guardian_controls=controls,
            consents=[
                ConsentRecordPublic(
                    consent_type=record.consent_type.value,
                    document_version=record.document_version,
                    subject=record.subject.value,
                    agreed_at=record.agreed_at,
                )
                for record in consents
            ],
            active_sessions=self.list_sessions(user),
            **domain_data,
        )

    def _export_domain_data(self, user: User) -> dict:
        projects = self.db.scalars(
            select(CreationProject)
            .where(CreationProject.owner_user_id == user.id)
            .order_by(CreationProject.created_at)
        ).all()
        intents = self.db.scalars(
            select(CreationIntent)
            .where(CreationIntent.owner_user_id == user.id)
            .order_by(CreationIntent.created_at)
        ).all()
        intent_ids = [intent.id for intent in intents]
        analyses = (
            self.db.scalars(
                select(IntentAnalysis)
                .where(IntentAnalysis.intent_id.in_(intent_ids))
                .order_by(IntentAnalysis.created_at)
            ).all()
            if intent_ids
            else []
        )
        analyses_by_intent: dict[uuid.UUID, list[IntentAnalysis]] = {}
        for analysis in analyses:
            analyses_by_intent.setdefault(analysis.intent_id, []).append(analysis)
        creations: list[dict] = []
        for project in projects:
            versions = self.db.scalars(
                select(CreationVersion)
                .where(CreationVersion.project_id == project.id)
                .order_by(CreationVersion.version_number)
            ).all()
            version_exports = [self._export_creation_version(version) for version in versions]
            publications = self.db.scalars(
                select(Publication)
                .where(Publication.project_id == project.id)
                .order_by(Publication.submitted_at)
            ).all()
            methods = self.db.scalars(
                select(CreationMethod)
                .where(CreationMethod.project_id == project.id)
                .order_by(CreationMethod.version_number)
            ).all()
            stage_events = self.db.scalars(
                select(CreationStageEvent)
                .where(CreationStageEvent.project_id == project.id)
                .order_by(CreationStageEvent.created_at)
            ).all()
            tool_calls = self.db.scalars(
                select(CreationToolCall)
                .where(CreationToolCall.project_id == project.id)
                .order_by(CreationToolCall.proposed_at)
            ).all()
            image_generation_jobs = self.db.scalars(
                select(ImageGenerationJob)
                .where(ImageGenerationJob.project_id == project.id)
                .order_by(ImageGenerationJob.created_at)
            ).all()
            creation_export_jobs = self.db.scalars(
                select(CreationExportJob)
                .where(CreationExportJob.project_id == project.id)
                .order_by(CreationExportJob.created_at)
            ).all()
            test_records = self.db.scalars(
                select(CreationTestRecord)
                .where(CreationTestRecord.project_id == project.id)
                .order_by(CreationTestRecord.created_at)
            ).all()
            test_record_ids = [item.id for item in test_records]
            test_issues = (
                self.db.scalars(
                    select(CreationTestIssue)
                    .where(CreationTestIssue.test_record_id.in_(test_record_ids))
                    .order_by(CreationTestIssue.created_at)
                ).all()
                if test_record_ids
                else []
            )
            issues_by_record: dict[uuid.UUID, list[CreationTestIssue]] = {}
            for issue in test_issues:
                issues_by_record.setdefault(issue.test_record_id, []).append(issue)
            creations.append(
                {
                    "id": project.id,
                    "title": project.title,
                    "description": project.description,
                    "media_type": project.media_type,
                    "status": project.status,
                    "default_visibility": project.default_visibility,
                    "source_intent_id": project.source_intent_id,
                    "current_stage": project.current_stage,
                    "created_at": project.created_at,
                    "methods": [
                        {
                            "version_number": item.version_number,
                            "name": item.name,
                            "goal": item.goal,
                            "audience": item.audience,
                            "format": item.format,
                            "steps": item.steps,
                            "resource_links": item.resource_links,
                            "source_asset_ids": item.source_asset_ids,
                            "manual_page_ids": item.manual_page_ids,
                            "created_at": item.created_at,
                        }
                        for item in methods
                    ],
                    "stage_events": [
                        {
                            "from_stage": item.from_stage,
                            "to_stage": item.to_stage,
                            "reason": item.reason,
                            "created_at": item.created_at,
                        }
                        for item in stage_events
                    ],
                    "tool_calls": [
                        {
                            "id": item.id,
                            "creation_version_id": item.creation_version_id,
                            "kind": item.kind,
                            "status": item.status,
                            "input_snapshot": item.input_snapshot,
                            "prompt_summary": item.prompt_summary,
                            "effect_summary": item.effect_summary,
                            "external_data_shared": item.external_data_shared,
                            "output_snapshot": item.output_snapshot,
                            "executor_ref": item.executor_ref,
                            "row_version": item.row_version,
                            "proposed_at": item.proposed_at,
                            "expires_at": item.expires_at,
                            "decided_at": item.decided_at,
                            "completed_at": item.completed_at,
                        }
                        for item in tool_calls
                    ],
                    "image_generation_jobs": [
                        {
                            "id": item.id,
                            "parent_version_id": item.parent_version_id,
                            "prompt": item.prompt,
                            "size": item.size,
                            "quality": item.quality,
                            "status": item.status,
                            "progress_percent": item.progress_percent,
                            "provider_ref": item.provider_ref,
                            "model_ref": item.model_ref,
                            "external_data_shared": item.external_data_shared,
                            "output_asset_id": item.output_asset_id,
                            "output_version_id": item.output_version_id,
                            "error_code": item.error_code,
                            "error_summary": item.error_summary,
                            "retry_count": item.retry_count,
                            "created_at": item.created_at,
                            "completed_at": item.completed_at,
                        }
                        for item in image_generation_jobs
                    ],
                    "creation_export_jobs": [
                        {
                            "id": item.id,
                            "creation_version_id": item.creation_version_id,
                            "format": item.format,
                            "output_scale": item.output_scale,
                            "status": item.status,
                            "progress_percent": item.progress_percent,
                            "output_asset_id": item.output_asset_id,
                            "error_code": item.error_code,
                            "error_summary": item.error_summary,
                            "created_at": item.created_at,
                            "completed_at": item.completed_at,
                        }
                        for item in creation_export_jobs
                    ],
                    "test_records": [
                        {
                            "id": item.id,
                            "creation_version_id": item.creation_version_id,
                            "scenario": item.scenario,
                            "result": item.result,
                            "notes": item.notes,
                            "created_at": item.created_at,
                            "issues": [
                                {
                                    "id": issue.id,
                                    "severity": issue.severity,
                                    "description": issue.description,
                                    "status": issue.status,
                                    "resolution_summary": issue.resolution_summary,
                                    "row_version": issue.row_version,
                                    "created_at": issue.created_at,
                                    "resolved_at": issue.resolved_at,
                                }
                                for issue in issues_by_record.get(item.id, [])
                            ],
                        }
                        for item in test_records
                    ],
                    "versions": version_exports,
                    "publications": [
                        {
                            "id": item.id,
                            "creation_version_id": item.creation_version_id,
                            "status": item.status,
                            "visibility": item.visibility,
                            "submitted_at": item.submitted_at,
                            "published_at": item.published_at,
                            "withdrawn_at": item.withdrawn_at,
                        }
                        for item in publications
                    ],
                }
            )
        assets = self.db.scalars(
            select(MediaAsset)
            .where(MediaAsset.owner_user_id == user.id)
            .order_by(MediaAsset.created_at)
        ).all()
        appeals = self.db.scalars(
            select(ModerationAppeal)
            .where(ModerationAppeal.appellant_user_id == user.id)
            .order_by(ModerationAppeal.created_at)
        ).all()
        privacy = self.db.get(PrivacySetting, user.id)
        audit_events = self.db.scalars(
            select(DomainAuditEvent)
            .where(DomainAuditEvent.actor_user_id == user.id)
            .order_by(DomainAuditEvent.created_at)
        ).all()
        conference_activity = self._export_conference_activity(user)
        return {
            "creation_intents": [
                {
                    "id": intent.id,
                    "text": intent.text,
                    "attachment_refs": intent.attachment_refs,
                    "attachment_asset_ids": intent.attachment_asset_ids,
                    "manual_page_ids": intent.manual_page_ids,
                    "resource_links": intent.resource_links,
                    "status": intent.status,
                    "created_at": intent.created_at,
                    "expires_at": intent.expires_at,
                    "analyses": [
                        {
                            "schema_version": item.schema_version,
                            "suggestion": item.suggestion,
                            "confidence": item.confidence,
                            "safety_flags": item.safety_flags,
                            "model_ref": item.model_ref,
                            "created_at": item.created_at,
                        }
                        for item in analyses_by_intent.get(intent.id, [])
                    ],
                }
                for intent in intents
            ],
            "creations": creations,
            "media_assets": [
                {
                    "id": item.id,
                    "purpose": item.purpose,
                    "original_filename": item.original_filename,
                    "actual_mime": item.actual_mime,
                    "byte_size": item.byte_size,
                    "sha256": item.sha256,
                    "status": item.status,
                    "width": item.width,
                    "height": item.height,
                    "aigc_detected": item.aigc_detected,
                    "created_at": item.created_at,
                    "deleted_at": item.deleted_at,
                }
                for item in assets
            ],
            "moderation_appeals": [
                {
                    "id": item.id,
                    "moderation_case_id": item.moderation_case_id,
                    "reason": item.reason,
                    "status": item.status,
                    "resolution_summary": item.resolution_summary,
                    "created_at": item.created_at,
                    "resolved_at": item.resolved_at,
                }
                for item in appeals
            ],
            "conference_activity": conference_activity,
            "privacy_settings": (
                {
                    "default_work_visibility": privacy.default_work_visibility,
                    "learning_card_public": privacy.learning_card_public,
                    "aigc_export_mark_enabled": privacy.aigc_export_mark_enabled,
                    "profile_discovery_enabled": privacy.profile_discovery_enabled,
                    "updated_at": privacy.updated_at,
                }
                if privacy
                else {}
            ),
            "domain_audit_events": [
                {
                    "action": item.action,
                    "target_type": item.target_type,
                    "target_id": item.target_id,
                    "result": item.result,
                    "safe_diff": item.safe_diff,
                    "created_at": item.created_at,
                }
                for item in audit_events
            ],
        }

    def _export_conference_activity(self, user: User) -> dict:
        reviews_authored = self.db.scalars(
            select(ConferenceReview)
            .where(ConferenceReview.reviewer_user_id == user.id)
            .order_by(ConferenceReview.created_at)
        ).all()
        reviews_received = self.db.scalars(
            select(ConferenceReview)
            .join(Publication, Publication.id == ConferenceReview.publication_id)
            .where(Publication.owner_user_id == user.id)
            .order_by(ConferenceReview.created_at)
        ).all()
        collections = self.db.scalars(
            select(ConferenceCollection)
            .where(ConferenceCollection.user_id == user.id)
            .order_by(ConferenceCollection.created_at)
        ).all()
        derivative_requests = self.db.scalars(
            select(ConferenceDerivativeRequest)
            .where(
                or_(
                    ConferenceDerivativeRequest.requester_user_id == user.id,
                    ConferenceDerivativeRequest.source_author_user_id == user.id,
                )
            )
            .order_by(ConferenceDerivativeRequest.created_at)
        ).all()
        derivative_request_ids = [item.id for item in derivative_requests]
        authorizations = (
            self.db.scalars(
                select(ConferenceDerivativeAuthorization).where(
                    ConferenceDerivativeAuthorization.request_id.in_(
                        derivative_request_ids
                    )
                )
            ).all()
            if derivative_request_ids
            else []
        )
        authorizations_by_request = {item.request_id: item for item in authorizations}
        matches = self.db.scalars(
            select(ConferenceMatch)
            .where(
                or_(
                    ConferenceMatch.participant_a_user_id == user.id,
                    ConferenceMatch.participant_b_user_id == user.id,
                )
            )
            .order_by(ConferenceMatch.matched_at)
        ).all()
        answers = self.db.scalars(
            select(ConferenceMatchAnswer)
            .where(ConferenceMatchAnswer.participant_user_id == user.id)
            .order_by(ConferenceMatchAnswer.submitted_at)
        ).all()
        evaluations = self.db.scalars(
            select(ConferenceMatchEvaluation)
            .where(
                or_(
                    ConferenceMatchEvaluation.subject_user_id == user.id,
                    ConferenceMatchEvaluation.evaluator_user_id == user.id,
                )
            )
            .order_by(ConferenceMatchEvaluation.created_at)
        ).all()
        reflections = self.db.scalars(
            select(ConferenceMatchReflection)
            .where(ConferenceMatchReflection.user_id == user.id)
            .order_by(ConferenceMatchReflection.created_at)
        ).all()
        queue_entries = self.db.scalars(
            select(ConferenceMatchQueue)
            .where(ConferenceMatchQueue.user_id == user.id)
            .order_by(ConferenceMatchQueue.joined_at)
        ).all()
        review_reports = self.db.scalars(
            select(ConferenceReviewReport)
            .where(ConferenceReviewReport.reporter_user_id == user.id)
            .order_by(ConferenceReviewReport.created_at)
        ).all()
        match_reports = self.db.scalars(
            select(ConferenceMatchReport)
            .where(ConferenceMatchReport.reporter_user_id == user.id)
            .order_by(ConferenceMatchReport.created_at)
        ).all()
        letters = self.db.scalars(
            select(ConferenceLetter)
            .where(ConferenceLetter.recipient_user_id == user.id)
            .order_by(ConferenceLetter.created_at)
        ).all()

        def review_export(item: ConferenceReview) -> dict:
            return {
                "id": item.id,
                "publication_id": item.publication_id,
                "template": item.template,
                "content": item.content,
                "status": item.status,
                "moderation_status": item.moderation_status,
                "author_reply": item.author_reply,
                "handled_at": item.handled_at,
                "adopted_in_creation_version_id": item.adopted_in_creation_version_id,
                "adoption_summary": item.adoption_summary,
                "adopted_at": item.adopted_at,
                "created_at": item.created_at,
            }

        return {
            "reviews_authored": [review_export(item) for item in reviews_authored],
            # Reviewer identities are deliberately omitted from received-review exports.
            "reviews_received": [review_export(item) for item in reviews_received],
            "collections": [
                {
                    "publication_id": item.publication_id,
                    "saved_at": item.created_at,
                }
                for item in collections
            ],
            "derivative_requests": [
                {
                    "id": item.id,
                    "role": (
                        "REQUESTER"
                        if item.requester_user_id == user.id
                        else "SOURCE_AUTHOR"
                    ),
                    "source_publication_id": item.source_publication_id,
                    "source_creation_version_id": item.source_creation_version_id,
                    "source_title": item.source_title,
                    "requested_use": item.requested_use,
                    "status": item.status,
                    "author_decision_note": item.author_decision_note,
                    "decided_at": item.decided_at,
                    "created_at": item.created_at,
                    "authorization": (
                        {
                            "id": authorization.id,
                            "authorization_version": authorization.authorization_version,
                            "status": authorization.status,
                            "granted_at": authorization.granted_at,
                            "revoked_at": authorization.revoked_at,
                        }
                        if (
                            authorization := authorizations_by_request.get(item.id)
                        )
                        else None
                    ),
                }
                for item in derivative_requests
            ],
            "matches": [
                {
                    "id": item.id,
                    "manual_page_id": item.manual_page_id,
                    "age_band": item.age_band,
                    "status": item.status,
                    "end_reason": item.end_reason,
                    "outcome": (
                        "PENDING"
                        if item.status.value != "ENDED"
                        else "WIN"
                        if item.winner_user_id == user.id
                        else "LOSE"
                        if item.winner_user_id is not None
                        else "TIE"
                        if item.end_reason is not None
                        and item.end_reason.value == "COMPLETED"
                        else "ENDED_WITHOUT_RESULT"
                    ),
                    "question_count": item.question_count,
                    "matched_at": item.matched_at,
                    "ended_at": item.ended_at,
                }
                for item in matches
            ],
            "match_answers": [
                {
                    "id": item.id,
                    "match_id": item.match_id,
                    "question_id": item.question_id,
                    "answer": item.answer,
                    "reason": item.reason,
                    "submitted_at": item.submitted_at,
                }
                for item in answers
            ],
            "match_evaluations": [
                {
                    "id": item.id,
                    "match_id": item.match_id,
                    "relation": (
                        "ABOUT_ME"
                        if item.subject_user_id == user.id
                        else "BY_ME"
                    ),
                    "kind": item.kind,
                    "score": item.score,
                    "dimension_scores": item.dimension_scores,
                    "summary": item.summary,
                    "strengths": item.strengths,
                    "improvements": item.improvements,
                    "created_at": item.created_at,
                }
                for item in evaluations
            ],
            "match_reflections": [
                {
                    "id": item.id,
                    "match_id": item.match_id,
                    "learned": item.learned,
                    "next_improvement": item.next_improvement,
                    "created_at": item.created_at,
                }
                for item in reflections
            ],
            "match_queue_history": [
                {
                    "id": item.id,
                    "manual_page_id": item.manual_page_id,
                    "status": item.status,
                    "match_id": item.match_id,
                    "joined_at": item.joined_at,
                    "expires_at": item.expires_at,
                }
                for item in queue_entries
            ],
            "review_reports_submitted": [
                {
                    "id": item.id,
                    "review_id": item.review_id,
                    "reason": item.reason,
                    "details": item.details,
                    "status": item.status,
                    "resolution_summary": item.resolution_summary,
                    "created_at": item.created_at,
                    "resolved_at": item.resolved_at,
                }
                for item in review_reports
            ],
            "match_reports_submitted": [
                {
                    "id": item.id,
                    "match_id": item.match_id,
                    "reason": item.reason,
                    "details": item.details,
                    "status": item.status,
                    "resolution_summary": item.resolution_summary,
                    "created_at": item.created_at,
                    "resolved_at": item.resolved_at,
                }
                for item in match_reports
            ],
            "letters": [
                {
                    "id": item.id,
                    "category": item.category,
                    "title": item.title,
                    "body": item.body,
                    "action_type": item.action_type,
                    "action_id": item.action_id,
                    "read_at": item.read_at,
                    "created_at": item.created_at,
                }
                for item in letters
            ],
        }

    def _export_creation_version(self, version: CreationVersion) -> dict:
        learning_card = self.db.get(LearningCard, version.id)
        manual_ids = self.db.scalars(
            select(LearningCardManual.manual_page_id).where(
                LearningCardManual.creation_version_id == version.id
            )
        ).all()
        manifest = self.db.get(ProvenanceManifest, version.id)
        seal_check = self.db.get(CreationSealCheck, version.id)
        provenance_items = self.db.scalars(
            select(ProvenanceItem).where(
                ProvenanceItem.creation_version_id == version.id
            )
        ).all()
        return {
            "id": version.id,
            "version_number": version.version_number,
            "parent_version_id": version.parent_version_id,
            "layers": version.layer_manifest,
            "change_summary": version.change_summary,
            "modification_reason": version.modification_reason,
            "created_at": version.created_at,
            "learning_card": (
                {
                    "manual_page_ids": list(manual_ids),
                    "method_summary": learning_card.method_summary,
                    "unresolved_questions": learning_card.unresolved_questions,
                    "questions_confirmed": learning_card.questions_confirmed,
                    "status": learning_card.status,
                }
                if learning_card
                else None
            ),
            "provenance": (
                {
                    "human_contribution_summary": manifest.human_contribution_summary,
                    "ai_assistance_used": manifest.ai_assistance_used,
                    "ai_contribution_summary": manifest.ai_contribution_summary,
                    "aigc_label_declared": manifest.aigc_label_declared,
                    "unresolved_rights": manifest.unresolved_rights,
                    "items": [
                        {
                            "item_type": item.item_type,
                            "contribution_type": item.contribution_type,
                            "description": item.description,
                            "source_url": item.source_url,
                            "source_author": item.source_author,
                            "license_type": item.license_type,
                            "ai_provider": item.ai_provider,
                            "ai_model": item.ai_model,
                            "ai_tool_action": item.ai_tool_action,
                            "prompt_summary": item.prompt_summary,
                        }
                        for item in provenance_items
                    ],
                }
                if manifest
                else None
            ),
            "seal_check": (
                {
                    "work_description": seal_check.work_description,
                    "learning_reflection": seal_check.learning_reflection,
                    "next_improvement": seal_check.next_improvement,
                    "identity_privacy_confirmed": seal_check.identity_privacy_confirmed,
                    "contact_privacy_confirmed": seal_check.contact_privacy_confirmed,
                    "portrait_rights_confirmed": seal_check.portrait_rights_confirmed,
                    "status": seal_check.status,
                    "row_version": seal_check.row_version,
                    "locked_at": seal_check.locked_at,
                    "created_at": seal_check.created_at,
                    "updated_at": seal_check.updated_at,
                }
                if seal_check
                else None
            ),
        }

    @staticmethod
    def _session_public(session: AuthSession) -> SessionPublic:
        return SessionPublic(
            id=session.id,
            device_name=session.device_name or "未知设备",
            created_at=session.created_at,
            last_seen_at=session.last_seen_at,
            expires_at=session.expires_at,
        )

    @staticmethod
    def _require_minor(user: User) -> None:
        if user.age_band == AgeBand.ADULT:
            raise ApiError(
                409,
                "GUARDIAN_CONTROLS_NOT_APPLICABLE",
                "成年人账号不适用监护设置",
            )
