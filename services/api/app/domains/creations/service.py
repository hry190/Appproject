from __future__ import annotations

import base64
import binascii
import hashlib
import json
import uuid
from datetime import UTC, datetime

from sqlalchemy import delete, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.catalog.models import ManualPage, ManualVolume
from app.domains.creations.contracts import (
    CreationChangeLogListPublic,
    CreationChangeLogPublic,
    CreationDisplayStatus,
    CreationProjectCreate,
    CreationProjectListPublic,
    CreationProjectPatch,
    CreationProjectPublic,
    CreationSubmissionCreate,
    CreationVersionCreate,
    CreationVersionListPublic,
    CreationVersionPublic,
    LearningCardPublic,
    LearningCardPut,
    ProvenanceItemInput,
    ProvenanceItemPublic,
    ProvenanceManifestPublic,
    ProvenanceManifestPut,
    PublicationPublic,
)
from app.domains.creations.models import (
    CreationChangeAction,
    CreationChangeLog,
    CreationProject,
    CreationProjectStatus,
    CreationVersion,
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
from app.domains.moderation.audit import add_audit_event
from app.domains.moderation.service import queue_moderation_case
from app.domains.privacy.models import PrivacySetting
from app.models import GuardianControl, User


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


def _fingerprint(payload: CreationSubmissionCreate) -> str:
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
        self, user: User, payload: CreationProjectCreate
    ) -> CreationProjectPublic:
        guardian_controls = self.db.get(GuardianControl, user.id)
        if guardian_controls is not None and not guardian_controls.creation_allowed:
            raise ApiError(403, "CREATION_DISABLED_BY_GUARDIAN", "监护设置暂未允许创作")
        privacy = self.db.get(PrivacySetting, user.id)
        default_visibility = payload.default_visibility
        if "default_visibility" not in payload.model_fields_set and privacy is not None:
            default_visibility = privacy.default_work_visibility
        project = CreationProject(
            owner_user_id=user.id,
            title=payload.title,
            description=payload.description,
            media_type=payload.media_type,
            default_visibility=default_visibility,
        )
        self.db.add(project)
        self.db.flush()
        self._log(
            project,
            user,
            CreationChangeAction.PROJECT_CREATED,
            "创建作品项目",
            {"media_type": payload.media_type.value},
        )
        self.db.commit()
        return self._project_public(project, None)

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

    def create_version(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: CreationVersionCreate,
    ) -> CreationVersionPublic:
        project = self._require_project(user, project_id, for_update=True)
        self._require_active(project)
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
        )
        self.db.add(version)
        self.db.flush()
        project.current_version_number = version_number
        project.row_version += 1
        project.updated_at = utcnow()
        self._log(
            project,
            user,
            CreationChangeAction.VERSION_CREATED,
            f"创建第 {version_number} 版",
            {"layer_count": len(payload.layers)},
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
            if payload.manual_page_ids
            and card.method_summary
            and payload.questions_confirmed
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
        _, version = self._require_version(user, payload.creation_version_id)
        if version.project_id != project.id:
            raise ApiError(404, "CREATION_VERSION_NOT_FOUND", "作品版本不存在")
        if version.version_number != project.current_version_number:
            raise ApiError(409, "STALE_CREATION_VERSION", "只能提交当前作品版本")
        already_submitted = self.db.scalar(
            select(Publication).where(Publication.creation_version_id == version.id)
        )
        if already_submitted is not None:
            raise ApiError(409, "VERSION_ALREADY_SUBMITTED", "该作品版本已经提交")

        card = self.db.get(LearningCard, version.id)
        manifest = self.db.get(ProvenanceManifest, version.id)
        issues = self._submission_issues(version, card, manifest)
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
        if (
            guardian_controls is not None
            and guardian_controls.minor_mode
            and visibility.value == "COMMUNITY"
        ):
            raise ApiError(
                403,
                "GUARDIAN_VISIBILITY_RESTRICTED",
                "当前监护设置不允许发布到社区",
            )
        publication = Publication(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=user.id,
            status=PublicationStatus.PENDING_CHECK,
            visibility=visibility,
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
        assert card is not None and manifest is not None
        card.status = LearningCardStatus.LOCKED
        card.locked_at = now
        card.updated_at = now
        card.row_version += 1
        manifest.status = ProvenanceStatus.LOCKED
        manifest.locked_at = now
        manifest.updated_at = now
        manifest.row_version += 1
        project.row_version += 1
        project.updated_at = now
        self._log(
            project,
            user,
            CreationChangeAction.SUBMITTED,
            f"提交第 {version.version_number} 版审核",
            {"visibility": publication.visibility.value},
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
    ) -> list[dict[str, str]]:
        issues: list[dict[str, str]] = []
        if card is None:
            issues.append({"code": "LEARNING_CARD_REQUIRED", "field": "learning_card", "message": "请先填写学习卡"})
        elif card.status != LearningCardStatus.COMPLETE:
            if not self._card_manual_ids(card.creation_version_id):
                issues.append({"code": "MANUAL_REQUIRED", "field": "learning_card.manual_page_ids", "message": "至少关联一本使用过的秘籍"})
            if not card.method_summary.strip():
                issues.append({"code": "METHOD_REQUIRED", "field": "learning_card.method_summary", "message": "请填写创作方法"})
            if not card.questions_confirmed:
                issues.append({"code": "QUESTIONS_NOT_CONFIRMED", "field": "learning_card.questions_confirmed", "message": "请确认未解决问题已经如实记录"})
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
