from __future__ import annotations

import hashlib
import json
import logging
import re
import uuid
from datetime import UTC, datetime, time

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.creations.contracts import (
    CreationStageTransition,
    CreationVersionCreate,
    ImageGenerationCreate,
    ImageGenerationJobListPublic,
    ImageGenerationJobPublic,
    ImageGenerationRetry,
    LayerSnapshot,
    ProvenanceItemInput,
    ProvenanceManifestPut,
)
from app.domains.creations.image_generation import (
    ImageGenerationProviderError,
    ImageGenerator,
    SIZE_PIXELS,
)
from app.domains.creations.models import (
    CreationChangeAction,
    CreationChangeLog,
    CreationProject,
    CreationProjectStatus,
    CreationStage,
    CreationVersion,
    ImageGenerationJob,
    ImageGenerationJobStatus,
    LayerKind,
    MaterialLicenseType,
    ProvenanceItem,
    ProvenanceManifest,
    ProvenanceItemType,
    ProvenanceStatus,
)
from app.domains.creations.service import CreationService
from app.domains.media.models import MediaAssetStatus, OutboxEvent, OutboxStatus
from app.domains.media.service import MediaService
from app.models import GuardianControl, User


ACTIVE_STATUSES = {
    ImageGenerationJobStatus.QUEUED,
    ImageGenerationJobStatus.RUNNING,
    ImageGenerationJobStatus.SAFETY_CHECK,
    ImageGenerationJobStatus.VERSIONING,
}
PERSONAL_DATA_PATTERNS = (
    re.compile(r"(?<!\d)1[3-9]\d{9}(?!\d)"),
    re.compile(r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b", re.IGNORECASE),
    re.compile(r"https?://|www\.", re.IGNORECASE),
    re.compile(r"(?<!\d)\d{17}[\dXx](?!\d)"),
)
logger = logging.getLogger(__name__)


def _fingerprint(payload: object) -> str:
    dumped = payload.model_dump(mode="json")  # type: ignore[attr-defined]
    canonical = json.dumps(dumped, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    return hashlib.sha256(canonical.encode()).hexdigest()


def _as_utc(value: datetime | None) -> datetime | None:
    if value is None or value.tzinfo is not None:
        return value
    return value.replace(tzinfo=UTC)


class ImageGenerationService:
    def __init__(
        self,
        *,
        db: Session,
        settings: Settings,
        generator: ImageGenerator | None,
        media_service: MediaService,
        request_id: str,
    ) -> None:
        self.db = db
        self.settings = settings
        self.generator = generator
        self.media_service = media_service
        self.request_id = request_id

    def create_job(
        self,
        user: User,
        project_id: uuid.UUID,
        payload: ImageGenerationCreate,
        idempotency_key: str,
    ) -> ImageGenerationJobPublic:
        fingerprint = _fingerprint(payload)
        replay = self.db.scalar(
            select(ImageGenerationJob).where(
                ImageGenerationJob.owner_user_id == user.id,
                ImageGenerationJob.idempotency_key == idempotency_key,
            )
        )
        if replay is not None:
            if replay.project_id != project_id or replay.request_fingerprint != fingerprint:
                raise ApiError(409, "IDEMPOTENCY_CONFLICT", "同一请求标识不能用于不同生成任务")
            return self._job_public(user, replay)
        if self.generator is None:
            raise ApiError(503, "IMAGE_GENERATION_DISABLED", "图片生成功能暂未开放")
        self._require_creation_allowed(user)
        project = self._require_project(user, project_id, for_update=True)
        if project.status != CreationProjectStatus.ACTIVE:
            raise ApiError(409, "CREATION_NOT_EDITABLE", "归档作品不可生成图片")
        if project.current_stage not in {CreationStage.DRAFT, CreationStage.PRODUCTION}:
            raise ApiError(409, "IMAGE_GENERATION_STAGE_INVALID", "只能在草图或制作阶段生成画面")
        if project.row_version != payload.expected_project_revision:
            raise ApiError(409, "VERSION_CONFLICT", "作品状态已变化，请刷新后重试")
        parent = self.db.get(CreationVersion, payload.parent_version_id)
        if (
            parent is None
            or parent.project_id != project.id
            or parent.version_number != project.current_version_number
        ):
            raise ApiError(409, "STALE_VERSION_PARENT", "只能基于当前作品版本生成")
        if any(pattern.search(payload.prompt) for pattern in PERSONAL_DATA_PATTERNS):
            raise ApiError(
                422,
                "GENERATION_PERSONAL_DATA",
                "提示词疑似包含电话、邮箱、网址或证件号，请删除个人信息后再生成",
            )
        active = self.db.scalar(
            select(ImageGenerationJob.id).where(
                ImageGenerationJob.project_id == project.id,
                ImageGenerationJob.status.in_(ACTIVE_STATUSES),
            ).limit(1)
        )
        if active is not None:
            raise ApiError(409, "IMAGE_GENERATION_IN_PROGRESS", "当前作品已有生成任务进行中")
        self._require_quota(user.id)

        now = utcnow()
        job = ImageGenerationJob(
            project_id=project.id,
            parent_version_id=parent.id,
            owner_user_id=user.id,
            prompt=payload.prompt,
            size=payload.size,
            quality=payload.quality,
            status=ImageGenerationJobStatus.QUEUED,
            progress_percent=0,
            provider_ref=self.generator.provider_ref,
            model_ref=self.generator.model_ref,
            external_data_shared=self.generator.external_data_shared,
            quota_charged=True,
            retry_count=0,
            idempotency_key=idempotency_key,
            request_fingerprint=fingerprint,
            row_version=1,
            created_at=now,
            updated_at=now,
        )
        self.db.add(job)
        self.db.flush()
        self._queue(job)
        self._log(
            job,
            CreationChangeAction.IMAGE_GENERATION_REQUESTED,
            "确认并提交图片生成任务",
            {"size": job.size.value, "quality": job.quality.value, "provider": job.provider_ref},
        )
        self.db.commit()
        return self._job_public(user, job)

    def list_jobs(
        self, user: User, project_id: uuid.UUID
    ) -> ImageGenerationJobListPublic:
        self._require_project(user, project_id)
        jobs = self.db.scalars(
            select(ImageGenerationJob)
            .where(ImageGenerationJob.project_id == project_id)
            .order_by(ImageGenerationJob.created_at.desc(), ImageGenerationJob.id.desc())
            .limit(20)
        ).all()
        used = self._daily_used(user.id)
        generator = self.generator
        return ImageGenerationJobListPublic(
            enabled=generator is not None,
            provider_ref=generator.provider_ref if generator else "disabled",
            model_ref=generator.model_ref if generator else "disabled",
            external_data_shared=generator.external_data_shared if generator else False,
            daily_limit=self.settings.image_generation_daily_limit,
            daily_used=used,
            daily_remaining=max(0, self.settings.image_generation_daily_limit - used),
            items=[self._job_public(user, job) for job in jobs],
        )

    def get_job(self, user: User, job_id: uuid.UUID) -> ImageGenerationJobPublic:
        job = self._require_job(user, job_id)
        return self._job_public(user, job)

    def retry_job(
        self,
        user: User,
        job_id: uuid.UUID,
        payload: ImageGenerationRetry,
        idempotency_key: str,
    ) -> ImageGenerationJobPublic:
        job = self._require_job(user, job_id, for_update=True)
        fingerprint = _fingerprint(payload)
        if job.last_retry_idempotency_key is not None:
            if (
                job.last_retry_idempotency_key == idempotency_key
                and job.last_retry_fingerprint == fingerprint
            ):
                return self._job_public(user, job)
            if job.status in ACTIVE_STATUSES:
                raise ApiError(409, "IMAGE_GENERATION_IN_PROGRESS", "生成任务已经重新排队")
        if job.row_version != payload.expected_revision:
            raise ApiError(409, "VERSION_CONFLICT", "生成任务状态已变化，请刷新后重试")
        if job.status != ImageGenerationJobStatus.FAILED or not job.retryable:
            raise ApiError(409, "IMAGE_GENERATION_NOT_RETRYABLE", "该生成任务当前不能重试")
        if job.retry_count >= self.settings.image_generation_max_retries:
            raise ApiError(409, "IMAGE_GENERATION_RETRY_LIMIT", "该生成任务已达到重试上限")
        project = self._require_project(user, job.project_id, for_update=True)
        parent = self.db.get(CreationVersion, job.parent_version_id)
        existing_version = self._existing_output_version(job)
        if (
            parent is None
            or (
                parent.version_number != project.current_version_number
                and existing_version is None
            )
        ):
            raise ApiError(409, "STALE_VERSION_PARENT", "作品已有新版本，请重新发起生成")
        if not job.quota_charged:
            self._require_quota(user.id)
            job.quota_charged = True
        job.status = ImageGenerationJobStatus.QUEUED
        job.progress_percent = 0
        job.error_code = None
        job.error_summary = None
        job.retryable = False
        job.retry_count += 1
        job.last_retry_idempotency_key = idempotency_key
        job.last_retry_fingerprint = fingerprint
        job.row_version += 1
        job.updated_at = utcnow()
        self._queue(job)
        self.db.commit()
        return self._job_public(user, job)

    def process_job(self, job_id: uuid.UUID) -> None:
        job = self.db.scalar(
            select(ImageGenerationJob).where(ImageGenerationJob.id == job_id).with_for_update()
        )
        if job is None:
            self._complete_outbox(job_id, failed=True, error="IMAGE_GENERATION_JOB_NOT_FOUND")
            self.db.commit()
            return
        if job.status != ImageGenerationJobStatus.QUEUED:
            self._complete_outbox(job.id)
            self.db.commit()
            return
        if self.generator is None:
            self._fail(job, "IMAGE_GENERATION_DISABLED", "图片生成功能暂未开放", retryable=False)
            return
        project = self.db.get(CreationProject, job.project_id)
        parent = self.db.get(CreationVersion, job.parent_version_id)
        existing_version = self._existing_output_version(job)
        if (
            project is None
            or parent is None
            or project.status != CreationProjectStatus.ACTIVE
            or (
                parent.version_number != project.current_version_number
                and existing_version is None
            )
        ):
            self._fail(job, "STALE_VERSION_PARENT", "作品版本已变化，未执行本次生成", retryable=False)
            return

        job.status = ImageGenerationJobStatus.RUNNING
        job.progress_percent = 15
        job.started_at = job.started_at or utcnow()
        job.updated_at = utcnow()
        job.row_version += 1
        self.db.commit()
        try:
            if existing_version is not None and job.output_asset_id is not None:
                job.status = ImageGenerationJobStatus.VERSIONING
                job.progress_percent = 80
                job.updated_at = utcnow()
                job.row_version += 1
                self.db.commit()
                self._create_output_version(job, job.output_asset_id)
                return
            generated = self.generator.generate(
                prompt=job.prompt,
                size=job.size,
                quality=job.quality,
                user_ref=hashlib.sha256(
                    f"jianghu:image-user:{job.owner_user_id}".encode()
                ).hexdigest()[:32],
            )
            job.status = ImageGenerationJobStatus.SAFETY_CHECK
            job.progress_percent = 55
            job.updated_at = utcnow()
            job.row_version += 1
            self.db.commit()
            media = self.media_service.ingest_generated_image(
                owner_user_id=job.owner_user_id,
                generation_job_id=job.id,
                data=generated.data,
                content_type=generated.content_type,
            )
            if media.status != MediaAssetStatus.READY:
                self.db.refresh(job)
                job.output_asset_id = media.id
                self._reject(
                    job,
                    media.rejection_code or "GENERATED_MEDIA_REJECTED",
                    media.rejection_summary or "生成结果未通过媒体安全检查",
                )
                return
            self.db.refresh(job)
            job.output_asset_id = media.id
            job.status = ImageGenerationJobStatus.VERSIONING
            job.progress_percent = 80
            job.updated_at = utcnow()
            job.row_version += 1
            self.db.commit()
            self._create_output_version(job, media.id)
        except ImageGenerationProviderError as exc:
            self.db.rollback()
            job = self.db.get(ImageGenerationJob, job_id)
            assert job is not None
            if exc.rejected:
                self._reject(job, exc.code, exc.summary)
            else:
                self._fail(job, exc.code, exc.summary, retryable=exc.retryable)
        except ApiError as exc:
            self.db.rollback()
            job = self.db.get(ImageGenerationJob, job_id)
            assert job is not None
            stale = exc.code in {"STALE_VERSION_PARENT", "VERSION_CONFLICT"}
            self._fail(job, exc.code, exc.message, retryable=not stale)
        except Exception:
            logger.exception("image generation job processing failed", extra={"job_id": str(job_id)})
            self.db.rollback()
            job = self.db.get(ImageGenerationJob, job_id)
            assert job is not None
            self._fail(
                job,
                "IMAGE_GENERATION_INTERNAL_ERROR",
                "生成结果处理失败，请稍后重试",
                retryable=True,
            )

    def _create_output_version(self, job: ImageGenerationJob, asset_id: uuid.UUID) -> None:
        user = self.db.get(User, job.owner_user_id)
        project = self.db.get(CreationProject, job.project_id)
        parent = self.db.get(CreationVersion, job.parent_version_id)
        assert user is not None and project is not None and parent is not None
        existing_version = self._existing_output_version(job)
        if existing_version is None and parent.version_number != project.current_version_number:
            raise ApiError(409, "STALE_VERSION_PARENT", "生成期间作品已产生新版本")
        if len(parent.layer_manifest) >= 200:
            raise ApiError(409, "CREATION_LAYER_LIMIT", "作品图层已达到上限")
        max_z = max(int(layer.get("z_index", 0)) for layer in parent.layer_manifest)
        if max_z >= 999:
            raise ApiError(409, "CREATION_LAYER_LIMIT", "作品图层顺序已达到上限")
        layers = [LayerSnapshot.model_validate(layer) for layer in parent.layer_manifest]
        layers.append(
            LayerSnapshot(
                layer_id=f"generation:{job.id}",
                kind=LayerKind.AI_GENERATED,
                name=f"AI 生成画面 {job.retry_count + 1}",
                z_index=max_z + 1,
                visible=True,
                asset_id=asset_id,
                aigc=True,
            )
        )
        width, height = SIZE_PIXELS[job.size]
        creation = CreationService(db=self.db, request_id=self.request_id)
        version = (
            CreationService._version_public(existing_version)
            if existing_version is not None
            else creation.create_version(
                user,
                project.id,
                CreationVersionCreate(
                    parent_version_id=parent.id,
                    layers=layers,
                    canvas_width=width,
                    canvas_height=height,
                    preview_asset_id=asset_id,
                    change_summary="添加经用户确认生成的 AI 画面",
                    modification_reason="图片生成任务自动写入新版本",
                ),
                idempotency_key=f"generation:{job.id}",
            )
        )
        project = self.db.get(CreationProject, project.id)
        assert project is not None
        if project.current_stage == CreationStage.DRAFT:
            creation.transition_stage(
                user,
                project.id,
                CreationStageTransition(
                    from_stage=CreationStage.DRAFT,
                    to_stage=CreationStage.PRODUCTION,
                    reason="生成画面已写入作品版本，进入制作阶段",
                    expected_revision=project.row_version,
                ),
            )
        manifest = self.db.get(ProvenanceManifest, version.id)
        if manifest is None:
            creation.put_provenance_manifest(
                user,
                version.id,
                ProvenanceManifestPut(
                    human_contribution_summary="",
                    ai_assistance_used=True,
                    ai_contribution_summary="使用图片生成工具形成画面底稿，等待补充本人修改说明。",
                    aigc_label_declared=True,
                    unresolved_rights=False,
                    items=[
                        ProvenanceItemInput(
                            item_type=ProvenanceItemType.AI_CONTRIBUTION,
                            contribution_type="图片生成",
                            description="经用户确认后生成，并通过媒体安全检查写入新版本。",
                            license_type=MaterialLicenseType.NOT_APPLICABLE,
                            ai_provider=job.provider_ref,
                            ai_model=job.model_ref,
                            ai_tool_action="IMAGE_GENERATION",
                            prompt_summary=job.prompt[:500],
                            output_asset_id=asset_id,
                            user_modified=False,
                        )
                    ],
                ),
            )
        else:
            already_recorded = self.db.scalar(
                select(ProvenanceItem.id).where(
                    ProvenanceItem.creation_version_id == version.id,
                    ProvenanceItem.output_asset_id == asset_id,
                )
            )
            if already_recorded is None:
                manifest.ai_assistance_used = True
                manifest.aigc_label_declared = True
                manifest.status = ProvenanceStatus.DRAFT
                manifest.locked_at = None
                manifest.ai_contribution_summary = (
                    "使用图片生成工具形成画面底稿，等待补充本人修改说明。"
                )
                manifest.updated_at = utcnow()
                manifest.row_version += 1
                self.db.add(
                    ProvenanceItem(
                        creation_version_id=version.id,
                        item_type=ProvenanceItemType.AI_CONTRIBUTION,
                        contribution_type="图片生成",
                        description="经用户确认后生成，并通过媒体安全检查写入新版本。",
                        license_type=MaterialLicenseType.NOT_APPLICABLE,
                        ai_provider=job.provider_ref,
                        ai_model=job.model_ref,
                        ai_tool_action="IMAGE_GENERATION",
                        prompt_summary=job.prompt[:500],
                        output_asset_id=asset_id,
                        user_modified=False,
                    )
                )
        job = self.db.get(ImageGenerationJob, job.id)
        assert job is not None
        job.output_version_id = version.id
        job.status = ImageGenerationJobStatus.COMPLETED
        job.progress_percent = 100
        job.retryable = False
        job.error_code = None
        job.error_summary = None
        job.completed_at = utcnow()
        job.updated_at = job.completed_at
        job.row_version += 1
        self._log(
            job,
            CreationChangeAction.IMAGE_GENERATION_COMPLETED,
            f"图片生成完成并写入第 {version.version_number} 版",
            {"asset_id": str(asset_id), "version_id": str(version.id)},
            version_id=version.id,
        )
        self._complete_outbox(job.id)
        self.db.commit()

    def _fail(
        self,
        job: ImageGenerationJob,
        code: str,
        summary: str,
        *,
        retryable: bool,
    ) -> None:
        job.status = ImageGenerationJobStatus.FAILED
        job.error_code = code[:80]
        job.error_summary = summary[:500]
        job.retryable = retryable and job.retry_count < self.settings.image_generation_max_retries
        job.quota_charged = False
        job.completed_at = utcnow()
        job.updated_at = job.completed_at
        job.row_version += 1
        self._log(
            job,
            CreationChangeAction.IMAGE_GENERATION_FAILED,
            "图片生成任务失败",
            {"error_code": job.error_code, "retryable": job.retryable},
        )
        self._complete_outbox(job.id, error=job.error_code)
        self.db.commit()

    def _reject(self, job: ImageGenerationJob, code: str, summary: str) -> None:
        job.status = ImageGenerationJobStatus.REJECTED
        job.error_code = code[:80]
        job.error_summary = summary[:500]
        job.retryable = False
        job.completed_at = utcnow()
        job.updated_at = job.completed_at
        job.row_version += 1
        self._log(
            job,
            CreationChangeAction.IMAGE_GENERATION_FAILED,
            "生成内容未通过安全检查",
            {"error_code": job.error_code, "retryable": False},
        )
        self._complete_outbox(job.id, error=job.error_code)
        self.db.commit()

    def _queue(self, job: ImageGenerationJob) -> None:
        self.db.add(
            OutboxEvent(
                aggregate_type="IMAGE_GENERATION_JOB",
                aggregate_id=job.id,
                event_type="IMAGE_GENERATION_REQUESTED",
                payload={"job_id": str(job.id)},
                deduplication_key=f"image-generation:{job.id}:attempt:{job.retry_count}",
                status=OutboxStatus.PENDING,
                available_at=utcnow(),
            )
        )

    def _complete_outbox(
        self, job_id: uuid.UUID, *, failed: bool = False, error: str | None = None
    ) -> None:
        event = self.db.scalar(
            select(OutboxEvent)
            .where(
                OutboxEvent.aggregate_id == job_id,
                OutboxEvent.event_type == "IMAGE_GENERATION_REQUESTED",
                OutboxEvent.status.in_([OutboxStatus.PENDING, OutboxStatus.PROCESSING]),
            )
            .order_by(OutboxEvent.created_at.desc())
            .limit(1)
        )
        if event is not None:
            event.status = OutboxStatus.FAILED if failed else OutboxStatus.COMPLETED
            event.last_error_code = error
            event.processed_at = utcnow()
            event.attempts += 1

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

    def _require_job(
        self, user: User, job_id: uuid.UUID, *, for_update: bool = False
    ) -> ImageGenerationJob:
        statement = select(ImageGenerationJob).where(
            ImageGenerationJob.id == job_id,
            ImageGenerationJob.owner_user_id == user.id,
        )
        if for_update:
            statement = statement.with_for_update()
        job = self.db.scalar(statement)
        if job is None:
            raise ApiError(404, "IMAGE_GENERATION_NOT_FOUND", "图片生成任务不存在")
        return job

    def _require_creation_allowed(self, user: User) -> None:
        controls = self.db.get(GuardianControl, user.id)
        if controls is not None and not controls.creation_allowed:
            raise ApiError(403, "CREATION_DISABLED_BY_GUARDIAN", "监护设置暂未允许创作")

    def _day_start(self) -> datetime:
        now = utcnow()
        return datetime.combine(now.date(), time.min, tzinfo=UTC)

    def _daily_used(self, user_id: uuid.UUID) -> int:
        return self.db.scalar(
            select(func.count(ImageGenerationJob.id)).where(
                ImageGenerationJob.owner_user_id == user_id,
                ImageGenerationJob.quota_charged.is_(True),
                ImageGenerationJob.created_at >= self._day_start(),
            )
        ) or 0

    def _require_quota(self, user_id: uuid.UUID) -> None:
        if self._daily_used(user_id) >= self.settings.image_generation_daily_limit:
            raise ApiError(429, "IMAGE_GENERATION_QUOTA_EXCEEDED", "今日图片生成次数已用完")

    def _existing_output_version(
        self, job: ImageGenerationJob
    ) -> CreationVersion | None:
        return self.db.scalar(
            select(CreationVersion).where(
                CreationVersion.project_id == job.project_id,
                CreationVersion.create_idempotency_key == f"generation:{job.id}",
            )
        )

    def _job_public(self, user: User, job: ImageGenerationJob) -> ImageGenerationJobPublic:
        output_asset = (
            self.media_service.get_asset(user, job.output_asset_id)
            if job.output_asset_id is not None
            else None
        )
        return ImageGenerationJobPublic(
            id=job.id,
            project_id=job.project_id,
            parent_version_id=job.parent_version_id,
            prompt_summary=job.prompt[:500],
            size=job.size,
            quality=job.quality,
            status=job.status,
            progress_percent=job.progress_percent,
            provider_ref=job.provider_ref,
            model_ref=job.model_ref,
            external_data_shared=job.external_data_shared,
            output_asset=output_asset,
            output_version_id=job.output_version_id,
            error_code=job.error_code,
            error_summary=job.error_summary,
            retryable=job.retryable,
            retry_count=job.retry_count,
            row_version=job.row_version,
            started_at=_as_utc(job.started_at),
            completed_at=_as_utc(job.completed_at),
            created_at=_as_utc(job.created_at),
            updated_at=_as_utc(job.updated_at),
        )

    def _log(
        self,
        job: ImageGenerationJob,
        action: CreationChangeAction,
        summary: str,
        details: dict,
        *,
        version_id: uuid.UUID | None = None,
    ) -> None:
        self.db.add(
            CreationChangeLog(
                project_id=job.project_id,
                version_id=version_id or job.parent_version_id,
                actor_user_id=job.owner_user_id,
                action=action,
                summary=summary,
                details={"generation_job_id": str(job.id), **details},
            )
        )
