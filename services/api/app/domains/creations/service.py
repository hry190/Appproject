from __future__ import annotations

import base64
import binascii
import hashlib
import json
import uuid
from datetime import UTC, datetime, timedelta

from sqlalchemy import delete, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.catalog.models import ManualPage, ManualVolume
from app.domains.learning.contracts import ManualProgressState
from app.domains.learning.models import ManualProgress
from app.domains.creations.contracts import (
    CreationChangeLogListPublic,
    CreationChangeLogPublic,
    CreationDisplayStatus,
    CreationIntentAnalysisPublic,
    CreationIntentAnalyze,
    CreationMethodDraft,
    CreationMethodPublic,
    CreationMethodPut,
    CreationProjectCreate,
    CreationProjectListPublic,
    CreationProjectPatch,
    CreationProjectPublic,
    CreationSealCheckPublic,
    CreationSealCheckPut,
    CreationStageEventListPublic,
    CreationStageEventPublic,
    CreationStageTransition,
    CreationStageTransitionPublic,
    CreationSubmissionCreate,
    CreationTestIssuePublic,
    CreationTestIssueResolve,
    CreationTestRecordCreate,
    CreationTestRecordListPublic,
    CreationTestRecordPublic,
    CreationToolCallDecision,
    CreationToolCallListPublic,
    CreationToolCallPropose,
    CreationToolCallPublic,
    CreationVersionCreate,
    CreationVersionDiffPublic,
    CreationLayerDiffPublic,
    CreationVersionListPublic,
    CreationVersionPublic,
    LearningCardPublic,
    LearningCardPut,
    LayerSnapshot,
    ProvenanceItemInput,
    ProvenanceItemPublic,
    ProvenanceManifestPublic,
    ProvenanceManifestPut,
    PublicationPublic,
)
from app.domains.creations.models import (
    CreationChangeAction,
    CreationChangeLog,
    CreationExportJob,
    CreationIntent,
    CreationIntentStatus,
    CreationMediaType,
    CreationMethod,
    CreationProject,
    CreationProjectStatus,
    CreationSealCheck,
    CreationSealStatus,
    CreationStage,
    CreationStageEvent,
    CreationTestIssue,
    CreationTestRecord,
    CreationTestResult,
    CreationIssueStatus,
    CreationToolCall,
    CreationToolCallStatus,
    CreationVersion,
    CreationVisibility,
    IntentAnalysis,
    LayerKind,
    LearningCard,
    LearningCardManual,
    LearningCardStatus,
    MaterialLicenseType,
    ProvenanceItem,
    ProvenanceItemType,
    ProvenanceManifest,
    ProvenanceStatus,
    Publication,
    PublicationStatus,
)
from app.domains.media.models import (
    MediaAsset,
    MediaAssetStatus,
    OutboxEvent,
    OutboxStatus,
)
from app.domains.media.references import (
    collect_version_asset_ids,
    is_asset_referenced_by_live_data,
)
from app.domains.distribution.models import (
    Classroom,
    ClassroomMembership,
    ClassroomMembershipStatus,
    ClassroomStatus,
)
from app.domains.distribution.service import revoke_publication_deliveries
from app.domains.conference.models import (
    ConferenceDerivativeAuthorization,
    DerivativeAuthorizationStatus,
)
from app.domains.moderation.audit import add_audit_event
from app.domains.moderation.service import queue_moderation_case
from app.domains.privacy.models import PrivacySetting
from app.models import AgeBand, GuardianControl, GuardianLink, User


def _encode_cursor(offset: int) -> str:
    return base64.urlsafe_b64encode(f"v1:{offset}".encode()).decode().rstrip("=")


def _decode_cursor(cursor: str) -> int:
    try:
        padded = cursor + "=" * (-len(cursor) % 4)
        version, raw_offset = base64.urlsafe_b64decode(padded).decode().split(":", 1)
        offset = int(raw_offset)
        if version != "v1" or offset < 0:
            raise ValueError
        return offset
    except (ValueError, UnicodeDecodeError, binascii.Error) as exc:
        raise ApiError(400, "INVALID_CURSOR", "分页游标无效，请从第一页重新加载") from exc


def _fingerprint(
    payload: (
        CreationProjectCreate
        | CreationVersionCreate
        | CreationSubmissionCreate
        | CreationToolCallPropose
        | CreationToolCallDecision
    ),
) -> str:
    canonical = json.dumps(
        payload.model_dump(mode="json"),
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(canonical.encode()).hexdigest()


def _as_utc(value: datetime | None) -> datetime | None:
    if value is None or value.tzinfo is not None:
        return value
    return value.replace(tzinfo=UTC)


class CreationService:
    def __init__(self, *, db: Session, request_id: str = "unknown") -> None:
        self.db = db
        self.request_id = request_id

    def create_project(
        self,
        user: User,
        payload: CreationProjectCreate,
        idempotency_key: str | None = None,
    ) -> CreationProjectPublic:
        self._require_creation_allowed(user)
        request_fingerprint = _fingerprint(payload)
        if idempotency_key is not None:
            existing = self.db.scalar(
                select(CreationProject).where(
                    CreationProject.owner_user_id == user.id,
                    CreationProject.create_idempotency_key == idempotency_key,
                )
            )
            if existing is not None:
                if existing.create_request_fingerprint != request_fingerprint:
                    raise ApiError(
                        409,
                        "IDEMPOTENCY_CONFLICT",
                        "同一创建请求标识不能用于不同作品",
                    )
                return self._project_public(
                    existing, self._latest_publication(existing.id)
                )
        privacy = self.db.get(PrivacySetting, user.id)
        derivative_authorization_id = payload.derivative_authorization_id
        if derivative_authorization_id is not None:
            self._require_active_derivative_authorization(user, derivative_authorization_id)
        source_intent: CreationIntent | None = None
        if payload.intent_id is not None:
            source_intent = self.db.scalar(
                select(CreationIntent)
                .where(
                    CreationIntent.id == payload.intent_id,
                    CreationIntent.owner_user_id == user.id,
                )
                .with_for_update()
            )
            if source_intent is None:
                raise ApiError(422, "CREATION_INTENT_INVALID", "创作意图不存在或不属于本人")
            intent_expires_at = _as_utc(source_intent.expires_at)
            if intent_expires_at is None or intent_expires_at <= utcnow():
                raise ApiError(409, "CREATION_INTENT_EXPIRED", "创作工法已过期，请重新整理")
            if source_intent.status == CreationIntentStatus.CONVERTED:
                replay = self.db.scalar(
                    select(CreationProject).where(
                        CreationProject.source_intent_id == source_intent.id
                    )
                )
                if (
                    replay is not None
                    and replay.create_idempotency_key == idempotency_key
                    and replay.create_request_fingerprint == request_fingerprint
                ):
                    return self._project_public(
                        replay, self._latest_publication(replay.id)
                    )
                raise ApiError(
                    409,
                    "CREATION_INTENT_ALREADY_CONVERTED",
                    "该创作意图已生成作品",
                )
        default_visibility = payload.default_visibility
        if "default_visibility" not in payload.model_fields_set and privacy is not None:
            default_visibility = privacy.default_work_visibility
        project = CreationProject(
            owner_user_id=user.id,
            title=payload.title,
            description=payload.description,
            media_type=payload.media_type,
            source_intent_id=payload.intent_id,
            derivative_authorization_id=derivative_authorization_id,
            default_visibility=default_visibility,
            create_idempotency_key=idempotency_key,
            create_request_fingerprint=(
                request_fingerprint if idempotency_key is not None else None
            ),
        )
        self.db.add(project)
        try:
            self.db.flush()
        except IntegrityError as exc:
            self.db.rollback()
            replay = (
                self.db.scalar(
                    select(CreationProject).where(
                        CreationProject.owner_user_id == user.id,
                        CreationProject.create_idempotency_key == idempotency_key,
                    )
                )
                if idempotency_key is not None
                else None
            )
            if replay is not None:
                if replay.create_request_fingerprint == request_fingerprint:
                    return self._project_public(
                        replay, self._latest_publication(replay.id)
                    )
                raise ApiError(
                    409,
                    "IDEMPOTENCY_CONFLICT",
                    "同一创建请求标识不能用于不同作品",
                ) from exc
            if payload.intent_id is not None:
                converted = self.db.scalar(
                    select(CreationProject.id).where(
                        CreationProject.source_intent_id == payload.intent_id
                    )
                )
                if converted is not None:
                    raise ApiError(
                        409,
                        "CREATION_INTENT_ALREADY_CONVERTED",
                        "该创作意图已生成作品",
                    ) from exc
            raise ApiError(409, "CREATION_CONFLICT", "作品创建发生冲突，请重试") from exc
        if source_intent is not None:
            source_intent.status = CreationIntentStatus.CONVERTED
        self._log(
            project,
            user,
            CreationChangeAction.PROJECT_CREATED,
            "创建作品项目",
            {"media_type": payload.media_type.value},
        )
        self.db.commit()
        return self._project_public(project, None)

    def analyze_intent(
        self,
        user: User,
        payload: CreationIntentAnalyze,
    ) -> CreationIntentAnalysisPublic:
        self._require_creation_allowed(user)
        self._validate_source_assets(user, payload.attachment_asset_ids)
        self._validate_learned_manuals(user, payload.manual_page_ids)
        text = " ".join(payload.text.split())
        now = utcnow()
        expires_at = now + timedelta(hours=24)

        safety_flags: list[str] = []
        if any(word in text for word in ("电话", "手机号", "住址", "学校全名")):
            safety_flags.append("POSSIBLE_PERSONAL_INFORMATION")
        unsupported = any(
            word in text for word in ("视频", "小游戏", "程序", "配音", "互动故事")
        )
        if unsupported:
            safety_flags.append("MVP_MEDIA_FALLBACK")

        media_type = (
            CreationMediaType.COMIC if "漫画" in text else CreationMediaType.ILLUSTRATION
        )
        format_name = "漫画分镜" if media_type == CreationMediaType.COMIC else "图文画面"
        method_name = (
            "漫画分镜工法" if media_type == CreationMediaType.COMIC else "图文创作工法"
        )
        questions = [] if len(text) >= 6 else ["你最想让观看者记住什么？"]
        confidence = "LOW" if questions else "MEDIUM"
        draft = CreationMethodDraft(
            name=method_name,
            goal=text[:200],
            audience=["同学与老师"],
            format=format_name,
            steps=["明确主题与受众", "完成草图或脚本", "制作并保存版本", "测试并补充说明"],
            resource_links=payload.resource_links,
            source_asset_ids=payload.attachment_asset_ids,
            manual_page_ids=payload.manual_page_ids,
            recommended_media_type=media_type,
        )
        intent = CreationIntent(
            owner_user_id=user.id,
            text=text,
            attachment_refs=payload.attachment_refs,
            attachment_asset_ids=[str(item) for item in payload.attachment_asset_ids],
            manual_page_ids=[str(item) for item in payload.manual_page_ids],
            resource_links=payload.resource_links,
            expires_at=expires_at,
        )
        self.db.add(intent)
        self.db.flush()
        analysis = IntentAnalysis(
            intent_id=intent.id,
            schema_version="1.0",
            suggestion={
                "method_draft": draft.model_dump(mode="json"),
                "questions": questions,
            },
            confidence=confidence,
            safety_flags=safety_flags,
            model_ref="rules-v1",
        )
        self.db.add(analysis)
        self.db.commit()
        return CreationIntentAnalysisPublic(
            intent_id=intent.id,
            analysis_id=analysis.id,
            schema_version=analysis.schema_version,
            method_draft=draft,
            questions=questions,
            safety_flags=safety_flags,
            confidence=confidence,
            expires_at=expires_at,
        )

    def list_projects(
        self,
        user: User,
        *,
        status: CreationProjectStatus | None,
        cursor: str | None,
        limit: int,
    ) -> CreationProjectListPublic:
        filters = [
            CreationProject.owner_user_id == user.id,
            CreationProject.status != CreationProjectStatus.DELETED,
        ]
        if status is not None:
            filters.append(CreationProject.status == status)
        total = self.db.scalar(
            select(func.count(CreationProject.id)).where(*filters)
        ) or 0
        offset = _decode_cursor(cursor) if cursor else 0
        projects = self.db.scalars(
            select(CreationProject)
            .where(*filters)
            .order_by(CreationProject.updated_at.desc(), CreationProject.id.desc())
            .offset(offset)
            .limit(limit + 1)
        ).all()
        has_more = len(projects) > limit
        selected = projects[:limit]
        publication_by_project = self._latest_publications(
            [project.id for project in selected]
        )
        return CreationProjectListPublic(
            total=total,
            items=[
                self._project_public(project, publication_by_project.get(project.id))
                for project in selected
            ],
            next_cursor=_encode_cursor(offset + limit) if has_more else None,
        )

    def get_project(self, user: User, project_id: uuid.UUID) -> CreationProjectPublic:
        project = self._require_project(user, project_id)
        return self._project_public(project, self._latest_publication(project.id))

    def update_project(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationProjectPatch,
    ) -> CreationProjectPublic:
        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
        if project.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "作品资料已更新，请刷新后重试")
        updates = payload.model_dump(exclude_unset=True, exclude={"row_version"})
        changes: dict[str, object] = {}
        for name, value in updates.items():
            old_value = getattr(project, name)
            if old_value != value:
                setattr(project, name, value)
                changes[name] = value.value if hasattr(value, "value") else value
        if not changes:
            return self._project_public(project, self._latest_publication(project.id))
        project.row_version += 1
        project.updated_at = utcnow()
        self._log(
            project,
            user,
            CreationChangeAction.PROJECT_METADATA_UPDATED,
            "更新作品资料",
            changes,
        )
        self.db.commit()
        return self._project_public(project, self._latest_publication(project.id))

    def get_current_method(
        self,
        user: User,
        project_id: uuid.UUID,
    ) -> CreationMethodPublic:
        project = self._require_project(user, project_id)
        method = self.db.scalar(
            select(CreationMethod)
            .where(CreationMethod.project_id == project.id)
            .order_by(CreationMethod.version_number.desc())
            .limit(1)
        )
        if method is None:
            raise ApiError(404, "CREATION_METHOD_NOT_FOUND", "尚未确认创作工法")
        return self._method_public(method, project.row_version)

    def put_method(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationMethodPut,
    ) -> CreationMethodPublic:
        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
        if project.row_version != payload.expected_revision:
            raise ApiError(409, "VERSION_CONFLICT", "创作工法已更新，请刷新后重试")
        self._validate_source_assets(user, payload.source_asset_ids)
        self._validate_learned_manuals(user, payload.manual_page_ids)
        current_number = self.db.scalar(
            select(func.max(CreationMethod.version_number)).where(
                CreationMethod.project_id == project.id
            )
        ) or 0
        method = CreationMethod(
            project_id=project.id,
            version_number=current_number + 1,
            created_by_user_id=user.id,
            name=payload.name.strip(),
            goal=payload.goal.strip(),
            audience=payload.audience,
            format=payload.format.strip(),
            steps=payload.steps,
            resource_links=payload.resource_links,
            source_asset_ids=[str(item) for item in payload.source_asset_ids],
            manual_page_ids=[str(item) for item in payload.manual_page_ids],
        )
        self.db.add(method)
        self.db.flush()
        project.row_version += 1
        project.updated_at = utcnow()
        self._log(
            project,
            user,
            CreationChangeAction.METHOD_UPDATED,
            f"确认第 {method.version_number} 版创作工法",
            {
                "method_version": method.version_number,
                "format": method.format,
                "source_asset_count": len(method.source_asset_ids),
                "manual_count": len(method.manual_page_ids),
            },
        )
        self.db.commit()
        return self._method_public(method, project.row_version)

    def transition_stage(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationStageTransition,
    ) -> CreationStageTransitionPublic:
        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
        if project.row_version != payload.expected_revision:
            raise ApiError(409, "VERSION_CONFLICT", "创作阶段已更新，请刷新后重试")
        if project.current_stage != payload.from_stage:
            raise ApiError(409, "STAGE_CONFLICT", "当前创作阶段已变化，请刷新后重试")

        order = list(CreationStage)
        current_index = order.index(payload.from_stage)
        target_index = order.index(payload.to_stage)
        if target_index == current_index or target_index > current_index + 1:
            raise ApiError(409, "INVALID_STAGE_TRANSITION", "不能跳过创作阶段")
        if target_index < current_index and len(payload.reason.strip()) < 3:
            raise ApiError(422, "STAGE_REASON_REQUIRED", "回到前一阶段时请说明原因")
        if payload.to_stage == CreationStage.DRAFT:
            method_exists = self.db.scalar(
                select(func.count(CreationMethod.id)).where(
                    CreationMethod.project_id == project.id
                )
            )
            if not method_exists:
                raise ApiError(409, "METHOD_REQUIRED", "请先确认创作工法")
        if payload.to_stage in {CreationStage.TEST, CreationStage.SEAL}:
            if project.current_version_number is None:
                raise ApiError(409, "VERSION_REQUIRED", "请先保存至少一个作品版本")
        if payload.to_stage == CreationStage.SEAL:
            current_version = self.db.scalar(
                select(CreationVersion).where(
                    CreationVersion.project_id == project.id,
                    CreationVersion.version_number == project.current_version_number,
                )
            )
            assert current_version is not None
            self._require_version_ready_for_seal(project, current_version)

        event = CreationStageEvent(
            project_id=project.id,
            actor_user_id=user.id,
            from_stage=payload.from_stage,
            to_stage=payload.to_stage,
            reason=payload.reason.strip(),
        )
        self.db.add(event)
        project.current_stage = payload.to_stage
        project.stage_updated_at = utcnow()
        project.updated_at = project.stage_updated_at
        project.row_version += 1
        self._log(
            project,
            user,
            CreationChangeAction.STAGE_TRANSITIONED,
            f"创作阶段从 {payload.from_stage.value} 调整为 {payload.to_stage.value}",
            {
                "from_stage": payload.from_stage.value,
                "to_stage": payload.to_stage.value,
            },
        )
        self.db.commit()
        return CreationStageTransitionPublic(
            current_stage=project.current_stage,
            project_revision=project.row_version,
            event=self._stage_event_public(event),
        )

    def list_stage_events(
        self,
        user: User,
        project_id: uuid.UUID,
    ) -> CreationStageEventListPublic:
        project = self._require_project(user, project_id)
        events = self.db.scalars(
            select(CreationStageEvent)
            .where(CreationStageEvent.project_id == project.id)
            .order_by(CreationStageEvent.created_at, CreationStageEvent.id)
        ).all()
        return CreationStageEventListPublic(
            items=[self._stage_event_public(event) for event in events]
        )

    def propose_tool_call(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationToolCallPropose,
        idempotency_key: str,
    ) -> CreationToolCallPublic:
        project = self._require_project(user, project_id)
        self._require_active(project)
        fingerprint = _fingerprint(payload)
        replay = self.db.scalar(
            select(CreationToolCall).where(
                CreationToolCall.owner_user_id == user.id,
                CreationToolCall.proposal_idempotency_key == idempotency_key,
            )
        )
        if replay is not None:
            if replay.project_id != project.id or replay.proposal_fingerprint != fingerprint:
                raise ApiError(409, "IDEMPOTENCY_CONFLICT", "同一请求标识不能用于不同工具建议")
            return self._tool_call_public(replay)
        if project.current_stage != CreationStage.PRODUCTION:
            raise ApiError(409, "TOOL_STAGE_INVALID", "创作教练工具只能在制作阶段使用")
        version = self._current_version(project)
        if version is None:
            raise ApiError(409, "VERSION_REQUIRED", "请先保存一个草图或脚本版本")
        now = utcnow()
        call = CreationToolCall(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=user.id,
            kind=payload.kind,
            status=CreationToolCallStatus.PROPOSED,
            input_snapshot={
                "prompt": payload.prompt,
                "version_number": version.version_number,
            },
            prompt_summary=payload.prompt[:500],
            effect_summary="仅分析当前文字草稿并返回修改建议，不生成图片、不修改作品",
            external_data_shared=False,
            proposal_idempotency_key=idempotency_key,
            proposal_fingerprint=fingerprint,
            row_version=1,
            proposed_at=now,
            expires_at=now + timedelta(minutes=30),
        )
        self.db.add(call)
        self.db.flush()
        self._log(
            project,
            user,
            CreationChangeAction.TOOL_CALL_RECORDED,
            "提出创作教练调用，等待用户确认",
            {"tool_call_id": str(call.id), "status": call.status.value},
            version=version,
        )
        self.db.commit()
        return self._tool_call_public(call)

    def decide_tool_call(
        self,
        user: User,
        tool_call_id: uuid.UUID,
        payload: CreationToolCallDecision,
        idempotency_key: str,
    ) -> CreationToolCallPublic:
        row = self.db.execute(
            select(CreationToolCall, CreationProject)
            .join(CreationProject, CreationProject.id == CreationToolCall.project_id)
            .where(
                CreationToolCall.id == tool_call_id,
                CreationToolCall.owner_user_id == user.id,
                CreationProject.owner_user_id == user.id,
                CreationProject.status != CreationProjectStatus.DELETED,
            )
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "TOOL_CALL_NOT_FOUND", "工具调用建议不存在")
        call, project = row
        fingerprint = _fingerprint(payload)
        if call.decision_idempotency_key is not None:
            if (
                call.decision_idempotency_key == idempotency_key
                and call.decision_fingerprint == fingerprint
            ):
                return self._tool_call_public(call)
            raise ApiError(409, "TOOL_CALL_ALREADY_DECIDED", "该工具调用已经处理")
        if call.status != CreationToolCallStatus.PROPOSED:
            raise ApiError(409, "TOOL_CALL_NOT_DECIDABLE", "该工具调用当前不能确认")
        if utcnow() >= _as_utc(call.expires_at):
            call.status = CreationToolCallStatus.EXPIRED
            call.row_version += 1
            self.db.commit()
            raise ApiError(410, "TOOL_CALL_EXPIRED", "工具调用建议已过期，请重新发起")
        if call.row_version != payload.expected_revision:
            raise ApiError(409, "VERSION_CONFLICT", "工具调用状态已变化，请刷新后重试")

        now = utcnow()
        call.decision_idempotency_key = idempotency_key
        call.decision_fingerprint = fingerprint
        call.decided_at = now
        call.row_version += 1
        if payload.approve:
            prompt = str(call.input_snapshot.get("prompt", ""))
            call.status = CreationToolCallStatus.COMPLETED
            call.executor_ref = "rules-coach-v1"
            call.output_snapshot = self._coach_review_output(prompt)
            call.completed_at = now
        else:
            call.status = CreationToolCallStatus.REJECTED
            call.output_snapshot = {
                "rejection_reason": (payload.reason or "").strip(),
            }
        version = self.db.get(CreationVersion, call.creation_version_id)
        self._log(
            project,
            user,
            CreationChangeAction.TOOL_CALL_RECORDED,
            "确认并完成创作教练调用" if payload.approve else "拒绝创作教练调用",
            {
                "tool_call_id": str(call.id),
                "approved": payload.approve,
                "executor_ref": call.executor_ref,
            },
            version=version,
        )
        self.db.commit()
        return self._tool_call_public(call)

    def list_tool_calls(
        self,
        user: User,
        project_id: uuid.UUID,
    ) -> CreationToolCallListPublic:
        project = self._require_project(user, project_id)
        calls = self.db.scalars(
            select(CreationToolCall)
            .where(CreationToolCall.project_id == project.id)
            .order_by(CreationToolCall.proposed_at.desc(), CreationToolCall.id.desc())
        ).all()
        return CreationToolCallListPublic(
            items=[self._tool_call_public(call) for call in calls]
        )

    def create_test_record(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationTestRecordCreate,
    ) -> CreationTestRecordPublic:
        project = self._require_project(user, project_id)
        self._require_active(project)
        if project.current_stage != CreationStage.TEST:
            raise ApiError(409, "TEST_STAGE_REQUIRED", "请先进入测试阶段")
        version = self._current_version(project)
        if version is None or version.id != payload.creation_version_id:
            raise ApiError(409, "TEST_VERSION_STALE", "只能测试作品的当前版本")
        record = CreationTestRecord(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=user.id,
            scenario=payload.scenario.strip(),
            result=payload.result,
            notes=payload.notes.strip(),
        )
        self.db.add(record)
        self.db.flush()
        issues = [
            CreationTestIssue(
                test_record_id=record.id,
                project_id=project.id,
                severity=finding.severity,
                description=finding.description.strip(),
                status=CreationIssueStatus.OPEN,
                row_version=1,
            )
            for finding in payload.findings
        ]
        self.db.add_all(issues)
        self._log(
            project,
            user,
            CreationChangeAction.TEST_RECORDED,
            "记录当前版本测试结果",
            {
                "test_record_id": str(record.id),
                "result": record.result.value,
                "issue_count": len(issues),
            },
            version=version,
        )
        self.db.commit()
        return self._test_record_public(record, issues)

    def list_test_records(
        self,
        user: User,
        project_id: uuid.UUID,
    ) -> CreationTestRecordListPublic:
        project = self._require_project(user, project_id)
        records = self.db.scalars(
            select(CreationTestRecord)
            .where(CreationTestRecord.project_id == project.id)
            .order_by(CreationTestRecord.created_at.desc(), CreationTestRecord.id.desc())
        ).all()
        issues = self.db.scalars(
            select(CreationTestIssue)
            .where(CreationTestIssue.project_id == project.id)
            .order_by(CreationTestIssue.created_at, CreationTestIssue.id)
        ).all()
        issues_by_record: dict[uuid.UUID, list[CreationTestIssue]] = {}
        for issue in issues:
            issues_by_record.setdefault(issue.test_record_id, []).append(issue)
        return CreationTestRecordListPublic(
            items=[
                self._test_record_public(record, issues_by_record.get(record.id, []))
                for record in records
            ]
        )

    def resolve_test_issue(
        self,
        user: User,
        issue_id: uuid.UUID,
        payload: CreationTestIssueResolve,
    ) -> CreationTestIssuePublic:
        row = self.db.execute(
            select(CreationTestIssue, CreationProject)
            .join(CreationProject, CreationProject.id == CreationTestIssue.project_id)
            .where(
                CreationTestIssue.id == issue_id,
                CreationProject.owner_user_id == user.id,
                CreationProject.status != CreationProjectStatus.DELETED,
            )
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(404, "TEST_ISSUE_NOT_FOUND", "测试问题不存在")
        issue, project = row
        self._require_active(project)
        if project.current_stage != CreationStage.TEST:
            raise ApiError(409, "TEST_STAGE_REQUIRED", "只能在测试阶段处理问题")
        if issue.status == CreationIssueStatus.RESOLVED:
            raise ApiError(409, "TEST_ISSUE_ALREADY_RESOLVED", "测试问题已经处理")
        if issue.row_version != payload.expected_revision:
            raise ApiError(409, "VERSION_CONFLICT", "测试问题已更新，请刷新后重试")
        now = utcnow()
        issue.status = CreationIssueStatus.RESOLVED
        issue.resolution_summary = payload.resolution_summary.strip()
        issue.resolved_at = now
        issue.updated_at = now
        issue.row_version += 1
        record = self.db.get(CreationTestRecord, issue.test_record_id)
        assert record is not None
        version = self.db.get(CreationVersion, record.creation_version_id)
        self._log(
            project,
            user,
            CreationChangeAction.TEST_ISSUE_UPDATED,
            "解决测试问题",
            {"issue_id": str(issue.id), "status": issue.status.value},
            version=version,
        )
        self.db.commit()
        return self._test_issue_public(issue)

    def create_version(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationVersionCreate,
        idempotency_key: str | None = None,
    ) -> CreationVersionPublic:
        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
        request_fingerprint = _fingerprint(payload)
        if idempotency_key is not None:
            existing = self.db.scalar(
                select(CreationVersion).where(
                    CreationVersion.project_id == project.id,
                    CreationVersion.create_idempotency_key == idempotency_key,
                )
            )
            if existing is not None:
                if existing.create_request_fingerprint != request_fingerprint:
                    raise ApiError(
                        409,
                        "IDEMPOTENCY_CONFLICT",
                        "同一版本请求标识不能用于不同内容",
                    )
                return self._version_public(existing)
        parent: CreationVersion | None = None
        if project.current_version_number is None:
            if payload.parent_version_id is not None:
                raise ApiError(409, "INVALID_VERSION_PARENT", "首个版本不能指定父版本")
            version_number = 1
        else:
            parent = self.db.scalar(
                select(CreationVersion).where(
                    CreationVersion.project_id == project.id,
                    CreationVersion.version_number == project.current_version_number,
                )
            )
            if parent is None or payload.parent_version_id != parent.id:
                raise ApiError(
                    409,
                    "STALE_VERSION_PARENT",
                    "父版本不是当前版本，请刷新作品后重新修订",
                )
            version_number = project.current_version_number + 1
        version = CreationVersion(
            project_id=project.id,
            version_number=version_number,
            parent_version_id=parent.id if parent else None,
            created_by_user_id=user.id,
            layer_manifest=[layer.model_dump(mode="json") for layer in payload.layers],
            layer_count=len(payload.layers),
            canvas_width=payload.canvas_width,
            canvas_height=payload.canvas_height,
            preview_asset_id=payload.preview_asset_id,
            change_summary=payload.change_summary.strip(),
            modification_reason=(
                payload.modification_reason.strip()
                if payload.modification_reason
                else None
            ),
            create_idempotency_key=idempotency_key,
            create_request_fingerprint=(
                request_fingerprint if idempotency_key is not None else None
            ),
        )
        self.db.add(version)
        try:
            self.db.flush()
        except IntegrityError as exc:
            self.db.rollback()
            replay = (
                self.db.scalar(
                    select(CreationVersion).where(
                        CreationVersion.project_id == project_id,
                        CreationVersion.create_idempotency_key == idempotency_key,
                    )
                )
                if idempotency_key is not None
                else None
            )
            if (
                replay is not None
                and replay.create_request_fingerprint == request_fingerprint
            ):
                return self._version_public(replay)
            raise ApiError(
                409,
                "VERSION_CONFLICT",
                "作品版本保存发生冲突，请刷新后重试",
            ) from exc
        changed_layer_ids, modified_ai_asset_ids = self._changed_layers(parent, payload)
        if parent is not None:
            self._inherit_provenance(
                parent=parent,
                version=version,
                modified_ai_asset_ids=modified_ai_asset_ids,
            )
        project.current_version_number = version_number
        if project.current_stage == CreationStage.SEAL:
            rollback_reason = (
                payload.modification_reason.strip()
                if payload.modification_reason
                else "封卷后创建新版本，返回制作并重新测试"
            )
            self.db.add(
                CreationStageEvent(
                    project_id=project.id,
                    actor_user_id=user.id,
                    from_stage=CreationStage.SEAL,
                    to_stage=CreationStage.PRODUCTION,
                    reason=rollback_reason,
                )
            )
            project.current_stage = CreationStage.PRODUCTION
            project.stage_updated_at = utcnow()
            self._log(
                project,
                user,
                CreationChangeAction.STAGE_TRANSITIONED,
                "封卷版本发生修改，自动返回制作阶段",
                {
                    "from_stage": CreationStage.SEAL.value,
                    "to_stage": CreationStage.PRODUCTION.value,
                    "reason": rollback_reason,
                },
                version=version,
            )
        project.row_version += 1
        project.updated_at = utcnow()
        self._log(
            project,
            user,
            CreationChangeAction.VERSION_CREATED,
            f"创建第 {version_number} 版",
            {
                "layer_count": len(payload.layers),
                "modified_layer_ids": sorted(changed_layer_ids),
                "ai_layers_user_modified": len(modified_ai_asset_ids),
            },
            version=version,
        )
        self.db.commit()
        return self._version_public(version)

    def list_versions(
        self, user: User, project_id: uuid.UUID
    ) -> CreationVersionListPublic:
        project = self._require_project(user, project_id)
        versions = self.db.scalars(
            select(CreationVersion)
            .where(CreationVersion.project_id == project.id)
            .order_by(CreationVersion.version_number.desc())
        ).all()
        return CreationVersionListPublic(
            items=[self._version_public(version) for version in versions]
        )

    def get_version(self, user: User, version_id: uuid.UUID) -> CreationVersionPublic:
        _, version = self._require_version(user, version_id)
        return self._version_public(version)

    def compare_versions(
        self,
        user: User,
        version_id: uuid.UUID,
        base_version_id: uuid.UUID | None = None,
    ) -> CreationVersionDiffPublic:
        project, target = self._require_version(user, version_id)
        resolved_base_id = base_version_id or target.parent_version_id
        if resolved_base_id is None:
            raise ApiError(409, "VERSION_HAS_NO_BASE", "首个版本没有可比较的上一版")
        base = self.db.get(CreationVersion, resolved_base_id)
        if base is None or base.project_id != project.id:
            raise ApiError(404, "CREATION_VERSION_NOT_FOUND", "未找到可比较的作品版本")

        base_layers = self._normalized_layers(base.layer_manifest)
        target_layers = self._normalized_layers(target.layer_manifest)
        base_by_id = {layer["layer_id"]: layer for layer in base_layers}
        target_by_id = {layer["layer_id"]: layer for layer in target_layers}

        def diff_item(layer: dict, changed_fields: list[str]) -> CreationLayerDiffPublic:
            return CreationLayerDiffPublic(
                layer_id=layer["layer_id"],
                name=layer["name"],
                kind=layer["kind"],
                changed_fields=changed_fields,
            )

        added = [
            diff_item(target_by_id[layer_id], ["added"])
            for layer_id in sorted(target_by_id.keys() - base_by_id.keys())
        ]
        removed = [
            diff_item(base_by_id[layer_id], ["removed"])
            for layer_id in sorted(base_by_id.keys() - target_by_id.keys())
        ]
        modified: list[CreationLayerDiffPublic] = []
        for layer_id in sorted(base_by_id.keys() & target_by_id.keys()):
            before = base_by_id[layer_id]
            after = target_by_id[layer_id]
            fields = sorted(
                key for key in before.keys() | after.keys() if before.get(key) != after.get(key)
            )
            if fields:
                modified.append(diff_item(after, fields))
        return CreationVersionDiffPublic(
            base_version_id=base.id,
            base_version_number=base.version_number,
            target_version_id=target.id,
            target_version_number=target.version_number,
            canvas_changed=(
                base.canvas_width != target.canvas_width
                or base.canvas_height != target.canvas_height
            ),
            added_layers=added,
            removed_layers=removed,
            modified_layers=modified,
        )

    def put_learning_card(
        self,
        user: User,
        version_id: uuid.UUID,
        payload: LearningCardPut,
    ) -> LearningCardPublic:
        project, version = self._require_version(user, version_id)
        self._require_active(project)
        self._validate_manuals(payload.manual_page_ids)
        card = self.db.get(LearningCard, version.id)
        now = utcnow()
        if card is not None:
            if card.status == LearningCardStatus.LOCKED:
                raise ApiError(409, "LEARNING_CARD_LOCKED", "该版本已提交，学习卡不可修改")
            if payload.row_version is None or payload.row_version != card.row_version:
                raise ApiError(409, "VERSION_CONFLICT", "学习卡已更新，请刷新后重试")
            card.row_version += 1
            card.updated_at = now
        else:
            if payload.row_version is not None:
                raise ApiError(409, "VERSION_CONFLICT", "学习卡尚未创建，请刷新后重试")
            card = LearningCard(creation_version_id=version.id, row_version=1)
            self.db.add(card)
        card.method_summary = payload.method_summary.strip()
        card.unresolved_questions = payload.unresolved_questions
        card.questions_confirmed = payload.questions_confirmed
        card.status = (
            LearningCardStatus.COMPLETE
            if card.method_summary and payload.questions_confirmed
            else LearningCardStatus.DRAFT
        )
        self.db.execute(
            delete(LearningCardManual).where(
                LearningCardManual.creation_version_id == version.id
            )
        )
        self.db.add_all(
            [
                LearningCardManual(
                    creation_version_id=version.id,
                    manual_page_id=manual_page_id,
                )
                for manual_page_id in payload.manual_page_ids
            ]
        )
        self._log(
            project,
            user,
            CreationChangeAction.LEARNING_CARD_UPDATED,
            "更新学习卡",
            {
                "status": card.status.value,
                "manual_count": len(payload.manual_page_ids),
            },
            version=version,
        )
        project.row_version += 1
        project.updated_at = now
        self.db.commit()
        return self._learning_card_public(card)

    def get_learning_card(
        self, user: User, version_id: uuid.UUID
    ) -> LearningCardPublic:
        _, version = self._require_version(user, version_id)
        card = self.db.get(LearningCard, version.id)
        if card is None:
            raise ApiError(404, "LEARNING_CARD_NOT_FOUND", "该版本尚未填写学习卡")
        return self._learning_card_public(card)

    def put_seal_check(
        self,
        user: User,
        version_id: uuid.UUID,
        payload: CreationSealCheckPut,
    ) -> CreationSealCheckPublic:
        project, version = self._require_version(user, version_id)
        self._require_active(project)
        if project.current_stage != CreationStage.SEAL:
            raise ApiError(409, "SEAL_STAGE_REQUIRED", "请先完成测试并进入作品说明")
        if version.version_number != project.current_version_number:
            raise ApiError(409, "STALE_CREATION_VERSION", "只能填写当前版本的封卷检查")
        check = self.db.get(CreationSealCheck, version.id)
        now = utcnow()
        if check is not None:
            if check.status == CreationSealStatus.LOCKED:
                raise ApiError(409, "SEAL_CHECK_LOCKED", "该版本已提交，封卷检查不可修改")
            if payload.row_version is None or payload.row_version != check.row_version:
                raise ApiError(409, "VERSION_CONFLICT", "封卷检查已更新，请刷新后重试")
            check.row_version += 1
            check.updated_at = now
        else:
            if payload.row_version is not None:
                raise ApiError(409, "VERSION_CONFLICT", "封卷检查尚未创建，请刷新后重试")
            check = CreationSealCheck(creation_version_id=version.id, row_version=1)
            self.db.add(check)
        check.work_description = payload.work_description.strip()
        check.learning_reflection = payload.learning_reflection.strip()
        check.next_improvement = payload.next_improvement.strip()
        check.identity_privacy_confirmed = payload.identity_privacy_confirmed
        check.contact_privacy_confirmed = payload.contact_privacy_confirmed
        check.portrait_rights_confirmed = payload.portrait_rights_confirmed
        check.status = (
            CreationSealStatus.COMPLETE
            if check.work_description
            and check.learning_reflection
            and check.next_improvement
            and check.identity_privacy_confirmed
            and check.contact_privacy_confirmed
            and check.portrait_rights_confirmed
            else CreationSealStatus.DRAFT
        )
        self._log(
            project,
            user,
            CreationChangeAction.SEAL_CHECK_UPDATED,
            "更新作品说明与隐私自查",
            {"status": check.status.value},
            version=version,
        )
        project.row_version += 1
        project.updated_at = now
        self.db.commit()
        return self._seal_check_public(check)

    def get_seal_check(
        self,
        user: User,
        version_id: uuid.UUID,
    ) -> CreationSealCheckPublic:
        _, version = self._require_version(user, version_id)
        check = self.db.get(CreationSealCheck, version.id)
        if check is None:
            raise ApiError(404, "SEAL_CHECK_NOT_FOUND", "该版本尚未填写封卷检查")
        return self._seal_check_public(check)

    def put_provenance_manifest(
        self,
        user: User,
        version_id: uuid.UUID,
        payload: ProvenanceManifestPut,
    ) -> ProvenanceManifestPublic:
        project, version = self._require_version(user, version_id)
        self._require_active(project)
        manifest = self.db.get(ProvenanceManifest, version.id)
        now = utcnow()
        if manifest is not None:
            if manifest.status == ProvenanceStatus.LOCKED:
                raise ApiError(409, "PROVENANCE_LOCKED", "该版本已提交，来源谱不可修改")
            if payload.row_version is None or payload.row_version != manifest.row_version:
                raise ApiError(409, "VERSION_CONFLICT", "来源谱已更新，请刷新后重试")
            manifest.row_version += 1
            manifest.updated_at = now
        else:
            if payload.row_version is not None:
                raise ApiError(409, "VERSION_CONFLICT", "来源谱尚未创建，请刷新后重试")
            manifest = ProvenanceManifest(creation_version_id=version.id, row_version=1)
            self.db.add(manifest)
        manifest.human_contribution_summary = payload.human_contribution_summary.strip()
        manifest.ai_assistance_used = payload.ai_assistance_used
        manifest.ai_contribution_summary = (
            payload.ai_contribution_summary.strip()
            if payload.ai_contribution_summary
            else None
        )
        manifest.aigc_label_declared = payload.aigc_label_declared
        manifest.unresolved_rights = payload.unresolved_rights
        issues = self._provenance_issues(version.layer_manifest, payload, payload.items)
        manifest.status = ProvenanceStatus.COMPLETE if not issues else ProvenanceStatus.DRAFT
        # The items reference the manifest's primary key directly. With autoflush off,
        # PostgreSQL needs the new manifest row persisted before its item rows.
        self.db.flush()
        self.db.execute(
            delete(ProvenanceItem).where(
                ProvenanceItem.creation_version_id == version.id
            )
        )
        self.db.add_all(
            [
                ProvenanceItem(
                    creation_version_id=version.id,
                    **item.model_dump(),
                )
                for item in payload.items
            ]
        )
        self._log(
            project,
            user,
            CreationChangeAction.PROVENANCE_UPDATED,
            "更新人机分工与来源谱",
            {"status": manifest.status.value, "item_count": len(payload.items)},
            version=version,
        )
        project.row_version += 1
        project.updated_at = now
        self.db.commit()
        return self._manifest_public(manifest)

    def get_provenance_manifest(
        self, user: User, version_id: uuid.UUID
    ) -> ProvenanceManifestPublic:
        _, version = self._require_version(user, version_id)
        manifest = self.db.get(ProvenanceManifest, version.id)
        if manifest is None:
            raise ApiError(404, "PROVENANCE_NOT_FOUND", "该版本尚未填写来源谱")
        return self._manifest_public(manifest)

    def submit(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationSubmissionCreate,
        idempotency_key: str,
    ) -> PublicationPublic:
        request_fingerprint = _fingerprint(payload)
        replay = self.db.scalar(
            select(Publication).where(
                Publication.owner_user_id == user.id,
                Publication.idempotency_key == idempotency_key,
            )
        )
        if replay is not None:
            if replay.request_fingerprint != request_fingerprint:
                raise ApiError(409, "IDEMPOTENCY_KEY_REUSED", "幂等键已用于不同的提交请求")
            return self._publication_public(replay)

        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
        if project.derivative_authorization_id is not None:
            self._require_active_derivative_authorization(
                user, project.derivative_authorization_id
            )
        _, version = self._require_version(user, payload.creation_version_id)
        if version.project_id != project.id:
            raise ApiError(404, "CREATION_VERSION_NOT_FOUND", "作品版本不存在")
        if version.version_number != project.current_version_number:
            raise ApiError(409, "STALE_CREATION_VERSION", "只能提交当前作品版本")
        if project.current_stage != CreationStage.SEAL:
            raise ApiError(409, "SUBMISSION_STAGE_INVALID", "请先完成测试并通过封卷检查")
        self._require_version_ready_for_seal(project, version)
        already_submitted = self.db.scalar(
            select(Publication).where(Publication.creation_version_id == version.id)
        )
        if already_submitted is not None:
            raise ApiError(409, "VERSION_ALREADY_SUBMITTED", "该作品版本已经提交")

        card = self.db.get(LearningCard, version.id)
        manifest = self.db.get(ProvenanceManifest, version.id)
        seal_check = self.db.get(CreationSealCheck, version.id)
        issues = self._submission_issues(version, card, manifest, seal_check)
        if issues:
            raise ApiError(
                409,
                "SUBMISSION_INCOMPLETE",
                "作品资料尚未完整，暂不能提交审核",
                details=issues,
            )

        now = utcnow()
        visibility = payload.visibility or project.default_visibility
        guardian_controls = self.db.get(GuardianControl, user.id)
        # Missing settings must use the same minor-mode default as the settings API.
        # Publication permissions cannot depend on whether a user has opened that page.
        minor_mode = (
            guardian_controls.minor_mode
            if guardian_controls is not None
            else user.age_band != AgeBand.ADULT
        )
        if (
            minor_mode
            and visibility.value == "COMMUNITY"
        ):
            raise ApiError(
                403,
                "GUARDIAN_VISIBILITY_RESTRICTED",
                "当前监护设置不允许发布到社区",
            )
        target_classroom_id = payload.target_classroom_id
        if visibility == CreationVisibility.GUARDIAN_ONLY:
            if target_classroom_id is not None:
                raise ApiError(422, "CLASSROOM_TARGET_INVALID", "家长可见作品不能指定班级")
            guardian_link = self.db.scalar(
                select(GuardianLink).where(GuardianLink.child_user_id == user.id)
            )
            if guardian_link is None:
                raise ApiError(409, "GUARDIAN_LINK_REQUIRED", "请先完成监护人验证再提交")
        elif visibility == CreationVisibility.CLASSROOM:
            # Legacy clients may still submit an unassigned classroom publication.
            # New clients always send a target, which is strictly membership-checked.
            if target_classroom_id is not None:
                membership = self.db.scalar(
                    select(ClassroomMembership)
                    .join(Classroom, Classroom.id == ClassroomMembership.classroom_id)
                    .where(
                        ClassroomMembership.student_user_id == user.id,
                        ClassroomMembership.classroom_id == target_classroom_id,
                        ClassroomMembership.status == ClassroomMembershipStatus.ACTIVE,
                        Classroom.status == ClassroomStatus.ACTIVE,
                    )
                )
                if membership is None:
                    raise ApiError(409, "CLASSROOM_MEMBERSHIP_REQUIRED", "请先加入目标班级再提交")
        elif target_classroom_id is not None:
            raise ApiError(422, "CLASSROOM_TARGET_INVALID", "当前可见范围不能指定班级")
        publication = Publication(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=user.id,
            status=PublicationStatus.PENDING_CHECK,
            visibility=visibility,
            classroom_id=target_classroom_id,
            idempotency_key=idempotency_key,
            request_fingerprint=request_fingerprint,
            submitted_at=now,
        )
        self.db.add(publication)
        self.db.flush()
        queue_moderation_case(
            self.db,
            publication=publication,
            owner_user_id=user.id,
            request_id=self.request_id,
        )
        assert card is not None and manifest is not None and seal_check is not None
        card.status = LearningCardStatus.LOCKED
        card.locked_at = now
        card.updated_at = now
        card.row_version += 1
        manifest.status = ProvenanceStatus.LOCKED
        manifest.locked_at = now
        manifest.updated_at = now
        manifest.row_version += 1
        seal_check.status = CreationSealStatus.LOCKED
        seal_check.locked_at = now
        seal_check.updated_at = now
        seal_check.row_version += 1
        project.row_version += 1
        project.updated_at = now
        self._log(
            project,
            user,
            CreationChangeAction.SUBMITTED,
            f"提交第 {version.version_number} 版审核",
            {
                "visibility": publication.visibility.value,
                "classroom_id": str(publication.classroom_id) if publication.classroom_id else None,
            },
            version=version,
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            replay = self.db.scalar(
                select(Publication).where(
                    Publication.owner_user_id == user.id,
                    Publication.idempotency_key == idempotency_key,
                )
            )
            if replay is not None and replay.request_fingerprint == request_fingerprint:
                return self._publication_public(replay)
            raise ApiError(409, "SUBMISSION_CONFLICT", "作品提交发生冲突，请刷新后重试") from exc
        return self._publication_public(publication)

    def delete_project(self, user: User, project_id: uuid.UUID) -> None:
        project = self._require_project(user, project_id, for_update=True)
        now = utcnow()
        versions = self.db.scalars(
            select(CreationVersion).where(CreationVersion.project_id == project.id)
        ).all()
        project_asset_ids: set[uuid.UUID] = set()
        for version in versions:
            project_asset_ids.update(collect_version_asset_ids(self.db, version))
        export_asset_ids = self.db.scalars(
            select(CreationExportJob.output_asset_id).where(
                CreationExportJob.project_id == project.id,
                CreationExportJob.output_asset_id.is_not(None),
            )
        ).all()
        project_asset_ids.update(asset_id for asset_id in export_asset_ids if asset_id is not None)
        project.status = CreationProjectStatus.DELETED
        project.row_version += 1
        project.updated_at = now
        publications = self.db.scalars(
            select(Publication).where(Publication.project_id == project.id)
        ).all()
        for publication in publications:
            if publication.status != PublicationStatus.WITHDRAWN:
                publication.status = PublicationStatus.WITHDRAWN
                publication.withdrawn_at = now
                publication.row_version += 1
                publication.updated_at = now
            revoke_publication_deliveries(self.db, publication.id)
            authorizations = self.db.scalars(
                select(ConferenceDerivativeAuthorization).where(
                    ConferenceDerivativeAuthorization.source_publication_id == publication.id,
                    ConferenceDerivativeAuthorization.status == DerivativeAuthorizationStatus.ACTIVE,
                )
            ).all()
            for authorization in authorizations:
                authorization.status = DerivativeAuthorizationStatus.REVOKED
                authorization.revoked_at = now
        self.db.flush()
        if project_asset_ids:
            assets = self.db.scalars(
                select(MediaAsset).where(
                    MediaAsset.id.in_(project_asset_ids),
                    MediaAsset.owner_user_id == user.id,
                )
            ).all()
            for asset in assets:
                if asset.status in {
                    MediaAssetStatus.DELETED,
                    MediaAssetStatus.DELETION_PENDING,
                } or is_asset_referenced_by_live_data(
                    self.db,
                    owner_user_id=user.id,
                    asset_id=asset.id,
                ):
                    continue
                asset.status = MediaAssetStatus.DELETION_PENDING
                asset.row_version += 1
                asset.updated_at = now
                self.db.add(
                    OutboxEvent(
                        aggregate_type="MEDIA_ASSET",
                        aggregate_id=asset.id,
                        event_type="MEDIA_DELETE_REQUESTED",
                        payload={"asset_id": str(asset.id)},
                        deduplication_key=f"media-delete:{asset.id}:v{asset.row_version}",
                        status=OutboxStatus.PENDING,
                        available_at=now,
                    )
                )
        self.db.add(
            OutboxEvent(
                aggregate_type="CREATION_PROJECT",
                aggregate_id=project.id,
                event_type="PROJECT_DELETE_REQUESTED",
                payload={"project_id": str(project.id)},
                deduplication_key=f"project-delete:{project.id}:v{project.row_version}",
                status=OutboxStatus.PENDING,
                available_at=now,
            )
        )
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="CREATION_PROJECT_DELETED",
            target_type="CREATION_PROJECT",
            target_id=project.id,
            result="HIDDEN",
            request_id=self.request_id,
            safe_diff={"withdrawn_publication_count": len(publications)},
        )
        self.db.commit()

    def list_change_logs(
        self, user: User, project_id: uuid.UUID, *, limit: int
    ) -> CreationChangeLogListPublic:
        project = self._require_project(user, project_id)
        logs = self.db.scalars(
            select(CreationChangeLog)
            .where(CreationChangeLog.project_id == project.id)
            .order_by(CreationChangeLog.created_at.desc())
            .limit(limit)
        ).all()
        return CreationChangeLogListPublic(
            items=[
                CreationChangeLogPublic(
                    id=log.id,
                    version_id=log.version_id,
                    action=log.action,
                    summary=log.summary,
                    details=log.details,
                    created_at=log.created_at,
                )
                for log in logs
            ]
        )

    def _require_project(
        self, user: User, project_id: uuid.UUID, *, for_update: bool = False
    ) -> CreationProject:
        statement = select(CreationProject).where(
            CreationProject.id == project_id,
            CreationProject.owner_user_id == user.id,
            CreationProject.status != CreationProjectStatus.DELETED,
        )
        if for_update:
            statement = statement.with_for_update()
        project = self.db.scalar(statement)
        if project is None:
            raise ApiError(404, "CREATION_NOT_FOUND", "作品不存在")
        return project

    def _require_active_derivative_authorization(
        self, user: User, authorization_id: uuid.UUID
    ) -> ConferenceDerivativeAuthorization:
        authorization = self.db.scalar(
            select(ConferenceDerivativeAuthorization)
            .where(ConferenceDerivativeAuthorization.id == authorization_id)
            .with_for_update()
        )
        if authorization is None or authorization.requester_user_id != user.id:
            raise ApiError(403, "DERIVATIVE_AUTHORIZATION_INVALID", "改造授权不存在或不属于本人")
        if authorization.status != DerivativeAuthorizationStatus.ACTIVE:
            raise ApiError(409, "DERIVATIVE_AUTHORIZATION_INACTIVE", "该改造授权已失效")
        source = self.db.get(Publication, authorization.source_publication_id)
        if (
            source is None
            or source.status != PublicationStatus.PUBLISHED
            or source.visibility != CreationVisibility.COMMUNITY
        ):
            raise ApiError(409, "DERIVATIVE_SOURCE_UNAVAILABLE", "原作品当前不可用于改造")
        return authorization

    def _require_version(
        self, user: User, version_id: uuid.UUID
    ) -> tuple[CreationProject, CreationVersion]:
        row = self.db.execute(
            select(CreationProject, CreationVersion)
            .join(CreationVersion, CreationVersion.project_id == CreationProject.id)
            .where(
                CreationVersion.id == version_id,
                CreationProject.owner_user_id == user.id,
                CreationProject.status != CreationProjectStatus.DELETED,
            )
        ).one_or_none()
        if row is None:
            raise ApiError(404, "CREATION_VERSION_NOT_FOUND", "作品版本不存在")
        return row[0], row[1]

    @staticmethod
    def _require_active(project: CreationProject) -> None:
        if project.status != CreationProjectStatus.ACTIVE:
            raise ApiError(409, "CREATION_NOT_EDITABLE", "归档作品不可修改")

    def _require_creation_allowed(self, user: User) -> None:
        controls = self.db.get(GuardianControl, user.id)
        if controls is not None and not controls.creation_allowed:
            raise ApiError(
                403,
                "CREATION_DISABLED_BY_GUARDIAN",
                "监护设置暂未允许创作",
            )

    def _validate_manuals(self, manual_page_ids: list[uuid.UUID]) -> None:
        if not manual_page_ids:
            return
        valid_ids = set(
            self.db.scalars(
                select(ManualPage.id)
                .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
                .where(
                    ManualPage.id.in_(manual_page_ids),
                    ManualPage.is_listed.is_(True),
                    ManualVolume.is_listed.is_(True),
                )
            ).all()
        )
        missing = [str(manual_id) for manual_id in manual_page_ids if manual_id not in valid_ids]
        if missing:
            raise ApiError(
                422,
                "MANUAL_REFERENCE_INVALID",
                "学习卡引用了不存在或未开放的秘籍",
                details=[{"field": "manual_page_ids", "value": value} for value in missing],
            )

    def _validate_source_assets(
        self,
        user: User,
        asset_ids: list[uuid.UUID],
    ) -> None:
        if not asset_ids:
            return
        assets = {
            asset.id: asset
            for asset in self.db.scalars(
                select(MediaAsset).where(
                    MediaAsset.id.in_(asset_ids),
                    MediaAsset.owner_user_id == user.id,
                )
            ).all()
        }
        issues: list[dict[str, str]] = []
        for asset_id in asset_ids:
            asset = assets.get(asset_id)
            if asset is None:
                issues.append(
                    {
                        "field": "attachment_asset_ids",
                        "value": str(asset_id),
                        "code": "MEDIA_ASSET_INVALID",
                    }
                )
            elif asset.status != MediaAssetStatus.READY:
                issues.append(
                    {
                        "field": "attachment_asset_ids",
                        "value": str(asset_id),
                        "code": "MEDIA_NOT_READY",
                    }
                )
            elif asset.purpose.value != "CREATION_LAYER":
                issues.append(
                    {
                        "field": "attachment_asset_ids",
                        "value": str(asset_id),
                        "code": "MEDIA_PURPOSE_INVALID",
                    }
                )
        if issues:
            raise ApiError(
                422,
                "CREATION_SOURCE_INVALID",
                "草图不存在、尚未通过安全检查，或不是创作素材",
                details=issues,
            )

    def _validate_learned_manuals(
        self,
        user: User,
        manual_page_ids: list[uuid.UUID],
    ) -> None:
        if not manual_page_ids:
            return
        allowed_states = {
            ManualProgressState.LEARNED,
            ManualProgressState.MASTERED,
            ManualProgressState.TEACHING,
        }
        learned_ids = set(
            self.db.scalars(
                select(ManualProgress.manual_page_id)
                .join(ManualPage, ManualPage.id == ManualProgress.manual_page_id)
                .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
                .where(
                    ManualProgress.user_id == user.id,
                    ManualProgress.manual_page_id.in_(manual_page_ids),
                    ManualProgress.state.in_(allowed_states),
                    ManualPage.is_listed.is_(True),
                    ManualVolume.is_listed.is_(True),
                )
            ).all()
        )
        missing = [
            str(manual_id)
            for manual_id in manual_page_ids
            if manual_id not in learned_ids
        ]
        if missing:
            raise ApiError(
                422,
                "MANUAL_NOT_LEARNED",
                "只能带入已达到习得、悟得或传习阶段的秘籍",
                details=[
                    {"field": "manual_page_ids", "value": value}
                    for value in missing
                ],
            )

    @staticmethod
    def _normalized_layers(layers: list[dict]) -> list[dict]:
        return [
            LayerSnapshot.model_validate(layer).model_dump(mode="json")
            for layer in layers
        ]

    def _changed_layers(
        self,
        parent: CreationVersion | None,
        payload: CreationVersionCreate,
    ) -> tuple[set[str], set[uuid.UUID]]:
        if parent is None:
            return {layer.layer_id for layer in payload.layers}, set()
        before = {
            layer["layer_id"]: layer
            for layer in self._normalized_layers(parent.layer_manifest)
        }
        after = {
            layer.layer_id: layer.model_dump(mode="json")
            for layer in payload.layers
        }
        changed_ids = {
            layer_id
            for layer_id in before.keys() | after.keys()
            if before.get(layer_id) != after.get(layer_id)
        }
        modified_ai_assets: set[uuid.UUID] = set()
        for layer_id in changed_ids:
            for layer in (before.get(layer_id), after.get(layer_id)):
                if layer is None:
                    continue
                if layer.get("aigc") is True or layer.get("kind") == LayerKind.AI_GENERATED.value:
                    asset_id = layer.get("asset_id")
                    if asset_id:
                        modified_ai_assets.add(uuid.UUID(str(asset_id)))
        return changed_ids, modified_ai_assets

    def _inherit_provenance(
        self,
        *,
        parent: CreationVersion,
        version: CreationVersion,
        modified_ai_asset_ids: set[uuid.UUID],
    ) -> None:
        source = self.db.get(ProvenanceManifest, parent.id)
        if source is None:
            return
        inherited = ProvenanceManifest(
            creation_version_id=version.id,
            human_contribution_summary=source.human_contribution_summary,
            ai_assistance_used=source.ai_assistance_used,
            ai_contribution_summary=source.ai_contribution_summary,
            aigc_label_declared=source.aigc_label_declared,
            unresolved_rights=source.unresolved_rights,
            status=ProvenanceStatus.DRAFT,
            row_version=1,
            locked_at=None,
        )
        self.db.add(inherited)
        self.db.flush()
        source_items = self.db.scalars(
            select(ProvenanceItem)
            .where(ProvenanceItem.creation_version_id == parent.id)
            .order_by(ProvenanceItem.created_at, ProvenanceItem.id)
        ).all()
        self.db.add_all(
            [
                ProvenanceItem(
                    creation_version_id=version.id,
                    item_type=item.item_type,
                    contribution_type=item.contribution_type,
                    description=item.description,
                    source_url=item.source_url,
                    source_author=item.source_author,
                    license_type=item.license_type,
                    authorization_asset_id=item.authorization_asset_id,
                    ai_provider=item.ai_provider,
                    ai_model=item.ai_model,
                    ai_tool_action=item.ai_tool_action,
                    prompt_summary=item.prompt_summary,
                    output_asset_id=item.output_asset_id,
                    user_modified=(
                        True
                        if item.output_asset_id in modified_ai_asset_ids
                        else item.user_modified
                    ),
                )
                for item in source_items
            ]
        )

    @staticmethod
    def _provenance_issues(
        layers: list[dict],
        manifest: ProvenanceManifestPut | ProvenanceManifest,
        items: list[ProvenanceItemInput] | list[ProvenanceItem],
    ) -> list[dict[str, str]]:
        issues: list[dict[str, str]] = []

        def add(code: str, field: str, message: str) -> None:
            issues.append({"code": code, "field": field, "message": message})

        if not manifest.human_contribution_summary.strip():
            add("HUMAN_SUMMARY_REQUIRED", "provenance.human_contribution_summary", "请说明本人完成的部分")
        human_items = [item for item in items if item.item_type == ProvenanceItemType.HUMAN_CONTRIBUTION]
        ai_items = [item for item in items if item.item_type == ProvenanceItemType.AI_CONTRIBUTION]
        external_items = [item for item in items if item.item_type == ProvenanceItemType.EXTERNAL_MATERIAL]
        if not human_items:
            add("HUMAN_ITEM_REQUIRED", "provenance.items", "至少记录一项本人贡献")
        for index, item in enumerate(human_items):
            if item.license_type not in {MaterialLicenseType.ORIGINAL, MaterialLicenseType.NOT_APPLICABLE}:
                add("HUMAN_LICENSE_INVALID", f"provenance.human_items.{index}.license_type", "本人贡献应标记为原创或不适用")

        layer_uses_ai = any(
            layer.get("aigc") is True or layer.get("kind") == LayerKind.AI_GENERATED.value
            for layer in layers
        )
        ai_required = bool(manifest.ai_assistance_used or layer_uses_ai)
        if ai_required:
            if not (manifest.ai_contribution_summary or "").strip():
                add("AI_SUMMARY_REQUIRED", "provenance.ai_contribution_summary", "请说明 AI 参与了哪些工作")
            if not manifest.aigc_label_declared:
                add("AIGC_LABEL_REQUIRED", "provenance.aigc_label_declared", "使用 AI 时必须确认 AIGC 标识")
            if not ai_items:
                add("AI_ITEM_REQUIRED", "provenance.items", "使用 AI 时至少记录一项 AI 贡献")
        elif ai_items:
            add("AI_USAGE_INCONSISTENT", "provenance.ai_assistance_used", "已记录 AI 贡献，请将 AI 协助设为 true")
        for index, item in enumerate(ai_items):
            missing_fields = [
                name
                for name in ("ai_provider", "ai_model", "ai_tool_action", "prompt_summary")
                if not (getattr(item, name) or "").strip()
            ]
            for name in missing_fields:
                add("AI_DETAIL_REQUIRED", f"provenance.ai_items.{index}.{name}", "AI 贡献记录缺少必要信息")
            if item.license_type != MaterialLicenseType.NOT_APPLICABLE:
                add("AI_LICENSE_INVALID", f"provenance.ai_items.{index}.license_type", "AI 贡献的授权类型应为不适用")
        for index, item in enumerate(external_items):
            if not (item.source_url or "").strip():
                add("MATERIAL_SOURCE_REQUIRED", f"provenance.external_items.{index}.source_url", "外部素材必须填写来源")
            if item.license_type in {MaterialLicenseType.UNKNOWN, MaterialLicenseType.NOT_APPLICABLE}:
                add("MATERIAL_LICENSE_REQUIRED", f"provenance.external_items.{index}.license_type", "外部素材必须选择明确授权类型")
            if item.license_type == MaterialLicenseType.AUTHORIZED and item.authorization_asset_id is None:
                add("AUTHORIZATION_PROOF_REQUIRED", f"provenance.external_items.{index}.authorization_asset_id", "已授权素材必须关联授权证明")
        if manifest.unresolved_rights:
            add("UNRESOLVED_RIGHTS", "provenance.unresolved_rights", "仍有未解决的素材授权问题")
        return issues

    def _submission_issues(
        self,
        version: CreationVersion,
        card: LearningCard | None,
        manifest: ProvenanceManifest | None,
        seal_check: CreationSealCheck | None,
    ) -> list[dict[str, str]]:
        issues: list[dict[str, str]] = []
        if card is None:
            issues.append({"code": "LEARNING_CARD_REQUIRED", "field": "learning_card", "message": "请先填写学习卡"})
        elif card.status != LearningCardStatus.COMPLETE:
            if not card.method_summary.strip():
                issues.append({"code": "METHOD_REQUIRED", "field": "learning_card.method_summary", "message": "请填写创作方法"})
            if not card.questions_confirmed:
                issues.append({"code": "QUESTIONS_NOT_CONFIRMED", "field": "learning_card.questions_confirmed", "message": "请确认未解决问题已经如实记录"})
        if seal_check is None:
            issues.append({"code": "SEAL_CHECK_REQUIRED", "field": "seal_check", "message": "请先填写作品说明、学习复盘与隐私自查"})
        elif seal_check.status != CreationSealStatus.COMPLETE:
            if not seal_check.work_description.strip():
                issues.append({"code": "WORK_DESCRIPTION_REQUIRED", "field": "seal_check.work_description", "message": "请填写作品说明"})
            if not seal_check.learning_reflection.strip():
                issues.append({"code": "LEARNING_REFLECTION_REQUIRED", "field": "seal_check.learning_reflection", "message": "请填写学习复盘"})
            if not seal_check.next_improvement.strip():
                issues.append({"code": "NEXT_IMPROVEMENT_REQUIRED", "field": "seal_check.next_improvement", "message": "请写下下一次想改什么"})
            if not (
                seal_check.identity_privacy_confirmed
                and seal_check.contact_privacy_confirmed
                and seal_check.portrait_rights_confirmed
            ):
                issues.append({"code": "PRIVACY_SELF_CHECK_REQUIRED", "field": "seal_check.privacy", "message": "请完成身份、联系方式和肖像权限自查"})
        if manifest is None:
            issues.append({"code": "PROVENANCE_REQUIRED", "field": "provenance", "message": "请先填写人机分工与来源谱"})
        else:
            items = self.db.scalars(
                select(ProvenanceItem).where(
                    ProvenanceItem.creation_version_id == version.id
                )
            ).all()
            issues.extend(self._provenance_issues(version.layer_manifest, manifest, items))
        asset_ids = collect_version_asset_ids(self.db, version)
        if asset_ids:
            assets = {
                asset.id: asset
                for asset in self.db.scalars(
                    select(MediaAsset).where(
                        MediaAsset.id.in_(asset_ids),
                    )
                ).all()
            }
            for asset_id in sorted(asset_ids, key=str):
                asset = assets.get(asset_id)
                if asset is None or asset.owner_user_id != version.created_by_user_id:
                    issues.append(
                        {
                            "code": "MEDIA_ASSET_INVALID",
                            "field": "version.layers",
                            "message": f"媒体文件 {asset_id} 不存在或不属于本人",
                        }
                    )
                elif asset.status != MediaAssetStatus.READY:
                    issues.append(
                        {
                            "code": "MEDIA_NOT_READY",
                            "field": "version.layers",
                            "message": f"媒体文件 {asset_id} 尚未完成安全检查",
                        }
                    )
        return issues

    def _require_version_ready_for_seal(
        self,
        project: CreationProject,
        version: CreationVersion,
    ) -> None:
        latest_test = self.db.scalar(
            select(CreationTestRecord)
            .where(
                CreationTestRecord.project_id == project.id,
                CreationTestRecord.creation_version_id == version.id,
            )
            .order_by(CreationTestRecord.created_at.desc(), CreationTestRecord.id.desc())
            .limit(1)
        )
        if latest_test is None:
            raise ApiError(409, "TEST_RECORD_REQUIRED", "请先记录当前版本的测试结果")
        if latest_test.result != CreationTestResult.PASSED:
            raise ApiError(409, "TEST_NOT_PASSED", "当前版本需要通过一次复测后才能进入说明")
        open_issue_count = self.db.scalar(
            select(func.count(CreationTestIssue.id))
            .join(
                CreationTestRecord,
                CreationTestRecord.id == CreationTestIssue.test_record_id,
            )
            .where(
                CreationTestIssue.project_id == project.id,
                CreationTestRecord.creation_version_id == version.id,
                CreationTestIssue.status == CreationIssueStatus.OPEN,
            )
        ) or 0
        if open_issue_count:
            raise ApiError(
                409,
                "TEST_ISSUES_OPEN",
                "仍有测试问题未处理，不能进入作品说明",
                details=[{"open_issue_count": open_issue_count}],
            )

    def _current_version(self, project: CreationProject) -> CreationVersion | None:
        if project.current_version_number is None:
            return None
        return self.db.scalar(
            select(CreationVersion).where(
                CreationVersion.project_id == project.id,
                CreationVersion.version_number == project.current_version_number,
            )
        )

    @staticmethod
    def _coach_review_output(prompt: str) -> dict:
        normalized = " ".join(prompt.split())
        focus = normalized[:160]
        return {
            "summary": "先把主体、动作和环境拆清楚，再决定画面细节。",
            "suggested_prompt": (
                f"{focus}；主体清晰，动作明确，前中后景分层，并保留一个可验证的视觉重点。"
            ),
            "checklist": [
                "主体是否一眼可辨",
                "动作与场景是否互相支持",
                "画面是否保留本人可继续修改的空间",
            ],
        }

    @staticmethod
    def _tool_call_public(call: CreationToolCall) -> CreationToolCallPublic:
        return CreationToolCallPublic(
            id=call.id,
            project_id=call.project_id,
            creation_version_id=call.creation_version_id,
            kind=call.kind,
            status=call.status,
            input_snapshot=call.input_snapshot,
            prompt_summary=call.prompt_summary,
            effect_summary=call.effect_summary,
            external_data_shared=call.external_data_shared,
            output_snapshot=call.output_snapshot,
            executor_ref=call.executor_ref,
            row_version=call.row_version,
            proposed_at=_as_utc(call.proposed_at),
            expires_at=_as_utc(call.expires_at),
            decided_at=_as_utc(call.decided_at) if call.decided_at else None,
            completed_at=_as_utc(call.completed_at) if call.completed_at else None,
        )

    @classmethod
    def _test_record_public(
        cls,
        record: CreationTestRecord,
        issues: list[CreationTestIssue],
    ) -> CreationTestRecordPublic:
        return CreationTestRecordPublic(
            id=record.id,
            project_id=record.project_id,
            creation_version_id=record.creation_version_id,
            scenario=record.scenario,
            result=record.result,
            notes=record.notes,
            issues=[cls._test_issue_public(issue) for issue in issues],
            created_at=_as_utc(record.created_at),
        )

    @staticmethod
    def _test_issue_public(issue: CreationTestIssue) -> CreationTestIssuePublic:
        return CreationTestIssuePublic(
            id=issue.id,
            test_record_id=issue.test_record_id,
            project_id=issue.project_id,
            severity=issue.severity,
            description=issue.description,
            status=issue.status,
            resolution_summary=issue.resolution_summary,
            row_version=issue.row_version,
            created_at=_as_utc(issue.created_at),
            updated_at=_as_utc(issue.updated_at),
            resolved_at=_as_utc(issue.resolved_at) if issue.resolved_at else None,
        )

    def _project_public(
        self, project: CreationProject, publication: Publication | None
    ) -> CreationProjectPublic:
        return CreationProjectPublic(
            id=project.id,
            title=project.title,
            description=project.description,
            media_type=project.media_type,
            status=project.status,
            default_visibility=project.default_visibility,
            current_version_number=project.current_version_number,
            source_intent_id=project.source_intent_id,
            derivative_authorization_id=project.derivative_authorization_id,
            current_stage=project.current_stage,
            display_status=(
                CreationDisplayStatus(publication.status.value)
                if publication
                else CreationDisplayStatus.DRAFT
            ),
            latest_publication=(
                self._publication_public(publication) if publication else None
            ),
            row_version=project.row_version,
            created_at=project.created_at,
            updated_at=project.updated_at,
        )

    @staticmethod
    def _method_public(
        method: CreationMethod,
        project_revision: int,
    ) -> CreationMethodPublic:
        return CreationMethodPublic(
            id=method.id,
            project_id=method.project_id,
            version_number=method.version_number,
            name=method.name,
            goal=method.goal,
            audience=method.audience,
            format=method.format,
            steps=method.steps,
            resource_links=method.resource_links,
            source_asset_ids=method.source_asset_ids,
            manual_page_ids=method.manual_page_ids,
            project_revision=project_revision,
            created_at=method.created_at,
        )

    @staticmethod
    def _stage_event_public(event: CreationStageEvent) -> CreationStageEventPublic:
        return CreationStageEventPublic(
            id=event.id,
            project_id=event.project_id,
            from_stage=event.from_stage,
            to_stage=event.to_stage,
            reason=event.reason,
            actor_user_id=event.actor_user_id,
            created_at=event.created_at,
        )

    @staticmethod
    def _version_public(version: CreationVersion) -> CreationVersionPublic:
        return CreationVersionPublic(
            id=version.id,
            project_id=version.project_id,
            version_number=version.version_number,
            parent_version_id=version.parent_version_id,
            layers=version.layer_manifest,
            canvas_width=version.canvas_width,
            canvas_height=version.canvas_height,
            preview_asset_id=version.preview_asset_id,
            change_summary=version.change_summary,
            modification_reason=version.modification_reason,
            created_at=version.created_at,
        )

    def _learning_card_public(self, card: LearningCard) -> LearningCardPublic:
        return LearningCardPublic(
            creation_version_id=card.creation_version_id,
            manual_page_ids=self._card_manual_ids(card.creation_version_id),
            method_summary=card.method_summary,
            unresolved_questions=card.unresolved_questions,
            questions_confirmed=card.questions_confirmed,
            status=card.status,
            row_version=card.row_version,
            locked_at=card.locked_at,
            created_at=card.created_at,
            updated_at=card.updated_at,
        )

    def _card_manual_ids(self, version_id: uuid.UUID) -> list[uuid.UUID]:
        return list(
            self.db.scalars(
                select(LearningCardManual.manual_page_id)
                .where(LearningCardManual.creation_version_id == version_id)
                .order_by(LearningCardManual.created_at, LearningCardManual.manual_page_id)
            ).all()
        )

    @staticmethod
    def _seal_check_public(check: CreationSealCheck) -> CreationSealCheckPublic:
        return CreationSealCheckPublic(
            creation_version_id=check.creation_version_id,
            work_description=check.work_description,
            learning_reflection=check.learning_reflection,
            next_improvement=check.next_improvement,
            identity_privacy_confirmed=check.identity_privacy_confirmed,
            contact_privacy_confirmed=check.contact_privacy_confirmed,
            portrait_rights_confirmed=check.portrait_rights_confirmed,
            status=check.status,
            row_version=check.row_version,
            locked_at=check.locked_at,
            created_at=check.created_at,
            updated_at=check.updated_at,
        )

    def _manifest_public(
        self, manifest: ProvenanceManifest
    ) -> ProvenanceManifestPublic:
        items = self.db.scalars(
            select(ProvenanceItem)
            .where(ProvenanceItem.creation_version_id == manifest.creation_version_id)
            .order_by(ProvenanceItem.created_at, ProvenanceItem.id)
        ).all()
        return ProvenanceManifestPublic(
            creation_version_id=manifest.creation_version_id,
            human_contribution_summary=manifest.human_contribution_summary,
            ai_assistance_used=manifest.ai_assistance_used,
            ai_contribution_summary=manifest.ai_contribution_summary,
            aigc_label_declared=manifest.aigc_label_declared,
            unresolved_rights=manifest.unresolved_rights,
            status=manifest.status,
            items=[
                ProvenanceItemPublic(
                    id=item.id,
                    item_type=item.item_type,
                    contribution_type=item.contribution_type,
                    description=item.description,
                    source_url=item.source_url,
                    source_author=item.source_author,
                    license_type=item.license_type,
                    authorization_asset_id=item.authorization_asset_id,
                    ai_provider=item.ai_provider,
                    ai_model=item.ai_model,
                    ai_tool_action=item.ai_tool_action,
                    prompt_summary=item.prompt_summary,
                    output_asset_id=item.output_asset_id,
                    user_modified=item.user_modified,
                    created_at=item.created_at,
                )
                for item in items
            ],
            row_version=manifest.row_version,
            locked_at=manifest.locked_at,
            created_at=manifest.created_at,
            updated_at=manifest.updated_at,
        )

    @staticmethod
    def _publication_public(publication: Publication) -> PublicationPublic:
        return PublicationPublic(
            id=publication.id,
            project_id=publication.project_id,
            creation_version_id=publication.creation_version_id,
            status=publication.status,
            visibility=publication.visibility,
            classroom_id=publication.classroom_id,
            return_reason_code=publication.return_reason_code,
            return_reason_summary=publication.return_reason_summary,
            submitted_at=_as_utc(publication.submitted_at),
            published_at=_as_utc(publication.published_at),
            returned_at=_as_utc(publication.returned_at),
            withdrawn_at=_as_utc(publication.withdrawn_at),
            row_version=publication.row_version,
            updated_at=_as_utc(publication.updated_at),
        )

    def _latest_publication(self, project_id: uuid.UUID) -> Publication | None:
        return self.db.scalar(
            select(Publication)
            .where(Publication.project_id == project_id)
            .order_by(Publication.submitted_at.desc())
            .limit(1)
        )

    def _latest_publications(
        self, project_ids: list[uuid.UUID]
    ) -> dict[uuid.UUID, Publication]:
        result: dict[uuid.UUID, Publication] = {}
        for project_id in project_ids:
            publication = self._latest_publication(project_id)
            if publication is not None:
                result[project_id] = publication
        return result

    def _log(
        self,
        project: CreationProject,
        user: User,
        action: CreationChangeAction,
        summary: str,
        details: dict,
        *,
        version: CreationVersion | None = None,
    ) -> None:
        self.db.add(
            CreationChangeLog(
                project_id=project.id,
                version_id=version.id if version else None,
                actor_user_id=user.id,
                action=action,
                summary=summary,
                details=details,
            )
        )
