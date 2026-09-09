from __future__ import annotations

import hashlib
import io
import json
import math
import uuid
from pathlib import Path

from PIL import Image, ImageColor, ImageDraw, ImageFont, ImageOps, UnidentifiedImageError
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.creations.contracts import (
    CreationExportCreate,
    CreationExportJobListPublic,
    CreationExportJobPublic,
    LayerSnapshot,
)
from app.domains.creations.models import (
    CreationChangeAction,
    CreationChangeLog,
    CreationExportFormat,
    CreationExportJob,
    CreationExportJobStatus,
    CreationProject,
    CreationProjectStatus,
    CreationVersion,
    LayerKind,
)
from app.domains.media.models import MediaAsset, MediaAssetStatus, OutboxEvent, OutboxStatus
from app.domains.media.service import MediaService
from app.domains.media.storage import ObjectNotFoundError, ObjectStore
from app.domains.privacy.models import PrivacySetting
from app.models import User


ACTIVE_STATUSES = {
    CreationExportJobStatus.QUEUED,
    CreationExportJobStatus.RENDERING,
    CreationExportJobStatus.SAFETY_CHECK,
}
MAX_EXPORT_PIXELS = 25_000_000


class ExportRenderError(Exception):
    def __init__(self, code: str, summary: str) -> None:
        super().__init__(summary)
        self.code = code
        self.summary = summary


def _fingerprint(payload: CreationExportCreate) -> str:
    canonical = json.dumps(
        payload.model_dump(mode="json"),
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(canonical.encode()).hexdigest()


def _font_path() -> str | None:
    module_path = Path(__file__).resolve()
    candidates = [
        Path("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"),
        Path("C:/Windows/Fonts/msyh.ttc"),
        Path("C:/Windows/Fonts/simhei.ttf"),
    ]
    candidates.extend(
        parent / "android/app/src/main/res/font/noto_sans_sc_regular.otf"
        for parent in module_path.parents
    )
    return next((str(path) for path in candidates if path.exists()), None)


def _font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    path = _font_path()
    if path:
        return ImageFont.truetype(path, size=max(8, size))
    return ImageFont.load_default(size=max(8, size))


def _affine_layer(
    layer_image: Image.Image,
    *,
    scale: float,
    rotation_degrees: float,
    translation_x: float,
    translation_y: float,
) -> Image.Image:
    width, height = layer_image.size
    center_x, center_y = width / 2, height / 2
    radians = math.radians(rotation_degrees)
    cosine = math.cos(radians) / scale
    sine = math.sin(radians) / scale
    a = cosine
    b = sine
    d = -sine
    e = cosine
    c = center_x - a * (center_x + translation_x) - b * (center_y + translation_y)
    f = center_y - d * (center_x + translation_x) - e * (center_y + translation_y)
    return layer_image.transform(
        (width, height),
        Image.Transform.AFFINE,
        (a, b, c, d, e, f),
        resample=Image.Resampling.BICUBIC,
    )


def render_creation_version(
    *,
    version: CreationVersion,
    assets: dict[uuid.UUID, tuple[MediaAsset, bytes]],
    export_format: CreationExportFormat,
    output_scale: int,
    add_aigc_mark: bool,
) -> tuple[bytes, str]:
    width = version.canvas_width * output_scale
    height = version.canvas_height * output_scale
    if width * height > MAX_EXPORT_PIXELS:
        raise ExportRenderError("EXPORT_PIXEL_LIMIT", "导出尺寸超过 2500 万像素限制")

    canvas = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    layers = [LayerSnapshot.model_validate(item) for item in version.layer_manifest]
    for layer in sorted(layers, key=lambda item: item.z_index):
        if not layer.visible or layer.opacity <= 0:
            continue
        surface = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        if layer.kind == LayerKind.TEXT:
            draw = ImageDraw.Draw(surface)
            font = _font((layer.font_size or 64) * output_scale)
            color = ImageColor.getrgb(layer.text_color or "#294B35") + (255,)
            text = layer.text_content or ""
            box = draw.multiline_textbbox((0, 0), text, font=font, align="center", spacing=4)
            text_width = box[2] - box[0]
            text_height = box[3] - box[1]
            draw.multiline_text(
                ((width - text_width) / 2 - box[0], (height - text_height) / 2 - box[1]),
                text,
                font=font,
                fill=color,
                align="center",
                spacing=4,
            )
        else:
            assert layer.asset_id is not None
            source_entry = assets.get(layer.asset_id)
            if source_entry is None:
                raise ExportRenderError("EXPORT_SOURCE_NOT_READY", f"图层“{layer.name}”的素材不可用")
            _, raw = source_entry
            try:
                with Image.open(io.BytesIO(raw)) as opened:
                    source = ImageOps.exif_transpose(opened).convert("RGBA")
                    source.load()
            except (UnidentifiedImageError, OSError, SyntaxError) as exc:
                raise ExportRenderError(
                    "EXPORT_SOURCE_DECODE_FAILED", f"图层“{layer.name}”无法解码"
                ) from exc
            source.thumbnail((width, height), Image.Resampling.LANCZOS)
            surface.alpha_composite(source, ((width - source.width) // 2, (height - source.height) // 2))

        effective_scale = layer.scale / (1 - 2 * layer.crop_inset)
        transformed = _affine_layer(
            surface,
            scale=effective_scale,
            rotation_degrees=layer.rotation_degrees,
            translation_x=layer.offset_x * output_scale,
            translation_y=layer.offset_y * output_scale,
        )
        if layer.opacity < 1:
            alpha = transformed.getchannel("A").point(lambda value: round(value * layer.opacity))
            transformed.putalpha(alpha)
        canvas.alpha_composite(transformed)

    if add_aigc_mark and any(layer.aigc and layer.visible for layer in layers):
        draw = ImageDraw.Draw(canvas)
        font = _font(18 * output_scale)
        label = "AI辅助"
        box = draw.textbbox((0, 0), label, font=font)
        padding = 8 * output_scale
        label_width = box[2] - box[0]
        label_height = box[3] - box[1]
        left = width - label_width - padding * 3
        top = height - label_height - padding * 3
        draw.rounded_rectangle(
            (left, top, width - padding, height - padding),
            radius=padding,
            fill=(255, 255, 255, 205),
        )
        draw.text((left + padding, top + padding - box[1]), label, font=font, fill=(41, 75, 53, 255))

    output = io.BytesIO()
    if export_format == CreationExportFormat.JPEG:
        flattened = Image.new("RGB", canvas.size, (255, 253, 244))
        flattened.paste(canvas, mask=canvas.getchannel("A"))
        flattened.save(output, format="JPEG", quality=92, optimize=True, exif=b"")
        return output.getvalue(), "image/jpeg"
    canvas.save(output, format="PNG", optimize=True)
    return output.getvalue(), "image/png"


class CreationExportService:
    def __init__(
        self,
        *,
        db: Session,
        store: ObjectStore,
        media_service: MediaService,
        request_id: str,
    ) -> None:
        self.db = db
        self.store = store
        self.media_service = media_service
        self.request_id = request_id

    def create_job(
        self,
        user: User,
        version_id: uuid.UUID,
        payload: CreationExportCreate,
        idempotency_key: str,
    ) -> CreationExportJobPublic:
        fingerprint = _fingerprint(payload)
        replay = self.db.scalar(
            select(CreationExportJob).where(
                CreationExportJob.owner_user_id == user.id,
                CreationExportJob.idempotency_key == idempotency_key,
            )
        )
        if replay is not None:
            if replay.creation_version_id != version_id or replay.request_fingerprint != fingerprint:
                raise ApiError(409, "IDEMPOTENCY_CONFLICT", "同一请求标识不能用于不同导出任务")
            return self._public(user, replay)

        version, project = self._require_version(user, version_id)
        if project.status == CreationProjectStatus.DELETED:
            raise ApiError(409, "CREATION_DELETED", "已删除作品不能导出")
        if version.canvas_width * version.canvas_height * payload.output_scale**2 > MAX_EXPORT_PIXELS:
            raise ApiError(422, "EXPORT_PIXEL_LIMIT", "导出尺寸超过 2500 万像素限制")
        active = self.db.scalar(
            select(CreationExportJob.id).where(
                CreationExportJob.creation_version_id == version.id,
                CreationExportJob.status.in_(ACTIVE_STATUSES),
            ).limit(1)
        )
        if active is not None:
            raise ApiError(409, "CREATION_EXPORT_IN_PROGRESS", "该版本已有导出任务进行中")

        now = utcnow()
        job = CreationExportJob(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=user.id,
            format=payload.format,
            output_scale=payload.output_scale,
            status=CreationExportJobStatus.QUEUED,
            progress_percent=0,
            idempotency_key=idempotency_key,
            request_fingerprint=fingerprint,
            row_version=1,
            created_at=now,
            updated_at=now,
        )
        self.db.add(job)
        self.db.flush()
        self._queue(job)
        self._log(job, CreationChangeAction.EXPORT_REQUESTED, "创建作品导出任务")
        self.db.commit()
        return self._public(user, job)

    def list_jobs(self, user: User, version_id: uuid.UUID) -> CreationExportJobListPublic:
        self._require_version(user, version_id)
        jobs = self.db.scalars(
            select(CreationExportJob)
            .where(CreationExportJob.creation_version_id == version_id)
            .order_by(CreationExportJob.created_at.desc(), CreationExportJob.id.desc())
            .limit(20)
        ).all()
        return CreationExportJobListPublic(items=[self._public(user, job) for job in jobs])

    def get_job(self, user: User, job_id: uuid.UUID) -> CreationExportJobPublic:
        return self._public(user, self._require_job(user, job_id))

    def process_job(self, job_id: uuid.UUID) -> None:
        job = self.db.scalar(
            select(CreationExportJob).where(CreationExportJob.id == job_id).with_for_update()
        )
        if job is None:
            self._complete_outbox(job_id, failed=True, error="CREATION_EXPORT_NOT_FOUND")
            self.db.commit()
            return
        if job.status not in ACTIVE_STATUSES:
            self._complete_outbox(job.id)
            self.db.commit()
            return
        if job.output_asset_id is not None:
            asset = self.db.get(MediaAsset, job.output_asset_id)
            if asset is not None and asset.status == MediaAssetStatus.READY:
                self._complete(job, asset.id)
                return

        version = self.db.get(CreationVersion, job.creation_version_id)
        project = self.db.get(CreationProject, job.project_id)
        if version is None or project is None or project.status == CreationProjectStatus.DELETED:
            self._fail(job, "CREATION_EXPORT_SOURCE_GONE", "作品版本已不存在")
            return

        job.status = CreationExportJobStatus.RENDERING
        job.progress_percent = 20
        job.started_at = job.started_at or utcnow()
        job.updated_at = utcnow()
        job.row_version += 1
        self.db.commit()
        try:
            asset_ids = {
                layer.asset_id
                for layer in (LayerSnapshot.model_validate(item) for item in version.layer_manifest)
                if layer.visible and layer.kind != LayerKind.TEXT and layer.asset_id is not None
            }
            media_rows = self.db.scalars(
                select(MediaAsset).where(
                    MediaAsset.id.in_(asset_ids),
                    MediaAsset.owner_user_id == job.owner_user_id,
                    MediaAsset.status == MediaAssetStatus.READY,
                )
            ).all() if asset_ids else []
            if len(media_rows) != len(asset_ids):
                raise ExportRenderError("EXPORT_SOURCE_NOT_READY", "一个或多个图层素材尚未通过安全检查")
            assets: dict[uuid.UUID, tuple[MediaAsset, bytes]] = {}
            for asset in media_rows:
                if not asset.private_object_key:
                    raise ExportRenderError("EXPORT_SOURCE_NOT_READY", "图层素材缺少安全存储文件")
                try:
                    raw = self.store.read_private(asset.private_object_key)
                except ObjectNotFoundError as exc:
                    raise ExportRenderError("EXPORT_SOURCE_MISSING", "图层素材文件不存在") from exc
                assets[asset.id] = (asset, raw)
            privacy = self.db.get(PrivacySetting, job.owner_user_id)
            data, content_type = render_creation_version(
                version=version,
                assets=assets,
                export_format=job.format,
                output_scale=job.output_scale,
                add_aigc_mark=privacy is None or privacy.aigc_export_mark_enabled,
            )
            job = self.db.get(CreationExportJob, job.id)
            assert job is not None
            job.status = CreationExportJobStatus.SAFETY_CHECK
            job.progress_percent = 70
            job.updated_at = utcnow()
            job.row_version += 1
            self.db.commit()
            media = self.media_service.ingest_creation_export(
                owner_user_id=job.owner_user_id,
                export_job_id=job.id,
                data=data,
                content_type=content_type,
                aigc_detected=any(bool(item.get("aigc")) for item in version.layer_manifest),
            )
            job = self.db.get(CreationExportJob, job.id)
            assert job is not None
            job.output_asset_id = media.id
            if media.status == MediaAssetStatus.REJECTED:
                job.status = CreationExportJobStatus.REJECTED
                job.error_code = media.rejection_code or "EXPORT_MEDIA_REJECTED"
                job.error_summary = media.rejection_summary or "导出文件未通过安全检查"
                job.progress_percent = 100
                job.completed_at = utcnow()
                job.updated_at = job.completed_at
                job.row_version += 1
                self._complete_outbox(job.id, failed=True, error=job.error_code)
                self.db.commit()
                return
            if media.status != MediaAssetStatus.READY:
                raise ExportRenderError("EXPORT_MEDIA_NOT_READY", "导出文件安全处理未完成")
            self._complete(job, media.id)
        except ExportRenderError as exc:
            job = self.db.get(CreationExportJob, job.id)
            assert job is not None
            self._fail(job, exc.code, exc.summary)
        except (OSError, ValueError) as exc:
            job = self.db.get(CreationExportJob, job.id)
            assert job is not None
            self._fail(job, "EXPORT_RENDER_FAILED", f"作品合成失败：{type(exc).__name__}")
        except Exception as exc:
            job = self.db.get(CreationExportJob, job.id)
            assert job is not None
            self._fail(job, "EXPORT_INTERNAL_ERROR", f"作品导出服务异常：{type(exc).__name__}")

    def _complete(self, job: CreationExportJob, asset_id: uuid.UUID) -> None:
        job.output_asset_id = asset_id
        job.status = CreationExportJobStatus.COMPLETED
        job.progress_percent = 100
        job.error_code = None
        job.error_summary = None
        job.completed_at = utcnow()
        job.updated_at = job.completed_at
        job.row_version += 1
        self._log(job, CreationChangeAction.EXPORT_COMPLETED, "作品导出完成")
        self._complete_outbox(job.id)
        self.db.commit()

    def _fail(self, job: CreationExportJob, code: str, summary: str) -> None:
        job.status = CreationExportJobStatus.FAILED
        job.progress_percent = 100
        job.error_code = code[:80]
        job.error_summary = summary[:500]
        job.completed_at = utcnow()
        job.updated_at = job.completed_at
        job.row_version += 1
        self._log(job, CreationChangeAction.EXPORT_FAILED, "作品导出失败")
        self._complete_outbox(job.id, failed=True, error=job.error_code)
        self.db.commit()

    def _require_version(self, user: User, version_id: uuid.UUID) -> tuple[CreationVersion, CreationProject]:
        row = self.db.execute(
            select(CreationVersion, CreationProject)
            .join(CreationProject, CreationProject.id == CreationVersion.project_id)
            .where(CreationVersion.id == version_id, CreationProject.owner_user_id == user.id)
        ).one_or_none()
        if row is None:
            raise ApiError(404, "CREATION_VERSION_NOT_FOUND", "作品版本不存在")
        return row

    def _require_job(self, user: User, job_id: uuid.UUID) -> CreationExportJob:
        job = self.db.scalar(
            select(CreationExportJob).where(
                CreationExportJob.id == job_id,
                CreationExportJob.owner_user_id == user.id,
            )
        )
        if job is None:
            raise ApiError(404, "CREATION_EXPORT_NOT_FOUND", "作品导出任务不存在")
        return job

    def _public(self, user: User, job: CreationExportJob) -> CreationExportJobPublic:
        version = self.db.get(CreationVersion, job.creation_version_id)
        output = self.media_service.get_asset(user, job.output_asset_id) if job.output_asset_id else None
        return CreationExportJobPublic(
            id=job.id,
            project_id=job.project_id,
            creation_version_id=job.creation_version_id,
            version_number=version.version_number if version else 1,
            format=job.format,
            output_scale=job.output_scale,
            status=job.status,
            progress_percent=job.progress_percent,
            output_asset=output,
            error_code=job.error_code,
            error_summary=job.error_summary,
            row_version=job.row_version,
            started_at=job.started_at,
            completed_at=job.completed_at,
            created_at=job.created_at,
            updated_at=job.updated_at,
        )

    def _queue(self, job: CreationExportJob) -> None:
        self.db.add(
            OutboxEvent(
                aggregate_type="CREATION_EXPORT_JOB",
                aggregate_id=job.id,
                event_type="CREATION_EXPORT_REQUESTED",
                payload={"job_id": str(job.id)},
                deduplication_key=f"creation-export:{job.id}",
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
                OutboxEvent.event_type == "CREATION_EXPORT_REQUESTED",
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

    def _log(
        self, job: CreationExportJob, action: CreationChangeAction, summary: str
    ) -> None:
        self.db.add(
            CreationChangeLog(
                project_id=job.project_id,
                version_id=job.creation_version_id,
                actor_user_id=job.owner_user_id,
                action=action,
                summary=summary,
                details={
                    "export_job_id": str(job.id),
                    "format": job.format.value,
                    "output_scale": job.output_scale,
                },
            )
        )
