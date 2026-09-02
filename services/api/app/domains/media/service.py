from __future__ import annotations

import hashlib
import io
import json
import re
import uuid
import warnings
from datetime import UTC, datetime, timedelta
from pathlib import PurePath

from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.media.contracts import (
    InternalMediaProcessRequest,
    MediaAssetPublic,
    MediaDeleteAccepted,
    MediaDerivativePublic,
    UploadComplete,
    UploadIntentCreate,
    UploadIntentPublic,
)
from app.domains.media.models import (
    MediaAsset,
    MediaAssetStatus,
    MediaDerivative,
    MediaDerivativeKind,
    MediaScanKind,
    MediaScanOutcome,
    MediaScanResult,
    OutboxEvent,
    OutboxStatus,
    UploadSession,
    UploadSessionStatus,
)
from app.domains.media.references import is_asset_referenced_by_live_data
from app.domains.media.storage import ObjectNotFoundError, ObjectStore
from app.domains.media.virus import VirusScanner
from app.domains.moderation.audit import add_audit_event
from app.models import User


ALLOWED_IMAGE_MIMES = {"image/jpeg", "image/png", "image/webp"}
FORMAT_TO_MIME = {"JPEG": "image/jpeg", "PNG": "image/png", "WEBP": "image/webp"}
MIME_TO_EXTENSION = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}


def _as_utc(value: datetime) -> datetime:
    return value if value.tzinfo is not None else value.replace(tzinfo=UTC)


def _completion_fingerprint(payload: UploadComplete) -> str:
    encoded = json.dumps(
        payload.model_dump(mode="json"), sort_keys=True, separators=(",", ":")
    ).encode()
    return hashlib.sha256(encoded).hexdigest()


def _sanitize_filename(value: str) -> str:
    leaf = PurePath(value.replace("\\", "/")).name
    cleaned = re.sub(r"[\x00-\x1f\x7f]", "", leaf).strip()
    cleaned = re.sub(r"[^\w.()\-\u4e00-\u9fff]", "_", cleaned)
    return cleaned[:180] or "upload"


class MediaService:
    def __init__(
        self,
        *,
        db: Session,
        settings: Settings,
        store: ObjectStore,
        virus_scanner: VirusScanner,
        request_id: str,
    ) -> None:
        self.db = db
        self.settings = settings
        self.store = store
        self.virus_scanner = virus_scanner
        self.request_id = request_id

    def create_upload_intent(
        self, user: User, payload: UploadIntentCreate
    ) -> UploadIntentPublic:
        mime = payload.declared_mime.lower()
        if mime not in ALLOWED_IMAGE_MIMES:
            raise ApiError(
                422,
                "MEDIA_TYPE_NOT_ALLOWED",
                "目前只支持 JPEG、PNG 和 WebP 图片，不支持 SVG 或未知格式",
            )
        if payload.byte_size > self.settings.media_max_upload_bytes:
            raise ApiError(413, "MEDIA_TOO_LARGE", "图片超过允许的大小上限")
        upload_id = uuid.uuid4()
        extension = MIME_TO_EXTENSION[mime]
        object_key = f"users/{user.id}/uploads/{upload_id}.{extension}"
        expires_at = utcnow() + timedelta(
            minutes=self.settings.media_upload_ttl_minutes
        )
        session = UploadSession(
            id=upload_id,
            owner_user_id=user.id,
            purpose=payload.purpose,
            original_filename=_sanitize_filename(payload.filename),
            declared_mime=mime,
            expected_bytes=payload.byte_size,
            client_sha256=payload.sha256,
            object_key=object_key,
            status=UploadSessionStatus.ISSUED,
            expires_at=expires_at,
        )
        presigned = self.store.presign_upload(
            object_key,
            content_type=mime,
            expires=timedelta(minutes=self.settings.media_upload_ttl_minutes),
        )
        self.db.add(session)
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="UPLOAD_INTENT_CREATED",
            target_type="UPLOAD_SESSION",
            target_id=session.id,
            result="SUCCESS",
            request_id=self.request_id,
            safe_diff={"purpose": payload.purpose.value, "byte_size": payload.byte_size},
        )
        self.db.commit()
        return UploadIntentPublic(
            id=session.id,
            status=session.status,
            purpose=session.purpose,
            upload_url=presigned.url,
            required_headers=presigned.required_headers,
            expires_at=session.expires_at,
        )

    def complete_upload(
        self,
        user: User,
        upload_id: uuid.UUID,
        payload: UploadComplete,
        idempotency_key: str,
    ) -> MediaAssetPublic:
        session = self.db.scalar(
            select(UploadSession)
            .where(
                UploadSession.id == upload_id,
                UploadSession.owner_user_id == user.id,
            )
            .with_for_update()
        )
        if session is None:
            raise ApiError(404, "UPLOAD_NOT_FOUND", "上传任务不存在")
        fingerprint = _completion_fingerprint(payload)
        if session.status == UploadSessionStatus.COMPLETED:
            if (
                session.complete_idempotency_key != idempotency_key
                or session.complete_fingerprint != fingerprint
            ):
                raise ApiError(409, "UPLOAD_ALREADY_COMPLETED", "上传任务已经完成")
            asset = self.db.scalar(
                select(MediaAsset).where(MediaAsset.upload_session_id == session.id)
            )
            assert asset is not None
            return self._asset_public(asset)
        if session.status != UploadSessionStatus.ISSUED:
            raise ApiError(409, "UPLOAD_NOT_COMPLETABLE", "上传任务当前不可完成")
        if utcnow() >= _as_utc(session.expires_at):
            session.status = UploadSessionStatus.EXPIRED
            self.store.delete_quarantine(session.object_key)
            self.db.commit()
            raise ApiError(410, "UPLOAD_EXPIRED", "上传地址已过期，请重新申请")
        if payload.byte_size != session.expected_bytes or payload.sha256 != session.client_sha256:
            raise ApiError(409, "UPLOAD_METADATA_MISMATCH", "文件大小或哈希与上传意图不一致")
        try:
            stored = self.store.head_quarantine(session.object_key)
        except ObjectNotFoundError as exc:
            raise ApiError(409, "UPLOAD_OBJECT_MISSING", "隔离区尚未收到文件") from exc
        if stored.byte_size != session.expected_bytes:
            session.status = UploadSessionStatus.REJECTED
            self.store.delete_quarantine(session.object_key)
            self.db.commit()
            raise ApiError(409, "UPLOAD_SIZE_MISMATCH", "对象存储中的文件大小不一致")
        if stored.content_type and stored.content_type.lower() != session.declared_mime:
            session.status = UploadSessionStatus.REJECTED
            self.store.delete_quarantine(session.object_key)
            self.db.commit()
            raise ApiError(409, "UPLOAD_CONTENT_TYPE_MISMATCH", "上传对象的 Content-Type 不一致")

        now = utcnow()
        asset = MediaAsset(
            owner_user_id=user.id,
            upload_session_id=session.id,
            purpose=session.purpose,
            original_filename=session.original_filename,
            declared_mime=session.declared_mime,
            byte_size=session.expected_bytes,
            sha256=session.client_sha256,
            quarantine_object_key=session.object_key,
            status=MediaAssetStatus.PROCESSING,
        )
        self.db.add(asset)
        self.db.flush()
        session.status = UploadSessionStatus.COMPLETED
        session.complete_idempotency_key = idempotency_key
        session.complete_fingerprint = fingerprint
        session.completed_at = now
        self.db.add(
            OutboxEvent(
                aggregate_type="MEDIA_ASSET",
                aggregate_id=asset.id,
                event_type="MEDIA_PROCESS_REQUESTED",
                payload={"asset_id": str(asset.id)},
                deduplication_key=f"media-process:{asset.id}:v1",
                status=OutboxStatus.PENDING,
                available_at=now,
            )
        )
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="UPLOAD_COMPLETED",
            target_type="MEDIA_ASSET",
            target_id=asset.id,
            result="QUEUED",
            request_id=self.request_id,
            safe_diff={"byte_size": asset.byte_size, "purpose": asset.purpose.value},
        )
        self.db.commit()
        return self._asset_public(asset)

    def get_asset(self, user: User, asset_id: uuid.UUID) -> MediaAssetPublic:
        asset = self._require_asset(user, asset_id)
        return self._asset_public(asset)

    def request_delete(self, user: User, asset_id: uuid.UUID) -> MediaDeleteAccepted:
        asset = self._require_asset(user, asset_id, for_update=True)
        if asset.status == MediaAssetStatus.DELETED:
            return MediaDeleteAccepted(id=asset.id, status=asset.status)
        if is_asset_referenced_by_live_data(
            self.db,
            owner_user_id=user.id,
            asset_id=asset.id,
        ):
            raise ApiError(409, "MEDIA_ASSET_IN_USE", "文件仍被作品版本或来源谱引用，不能删除")
        asset.status = MediaAssetStatus.DELETION_PENDING
        asset.row_version += 1
        asset.updated_at = utcnow()
        self.db.add(
            OutboxEvent(
                aggregate_type="MEDIA_ASSET",
                aggregate_id=asset.id,
                event_type="MEDIA_DELETE_REQUESTED",
                payload={"asset_id": str(asset.id)},
                deduplication_key=f"media-delete:{asset.id}:v{asset.row_version}",
                status=OutboxStatus.PENDING,
                available_at=utcnow(),
            )
        )
        add_audit_event(
            self.db,
            actor_user_id=user.id,
            actor_type="USER",
            action="MEDIA_DELETE_REQUESTED",
            target_type="MEDIA_ASSET",
            target_id=asset.id,
            result="QUEUED",
            request_id=self.request_id,
            safe_diff={},
        )
        self.db.commit()
        return MediaDeleteAccepted(id=asset.id, status=asset.status)

    def process_asset(
        self, asset_id: uuid.UUID, payload: InternalMediaProcessRequest
    ) -> MediaAssetPublic:
        asset = self.db.scalar(
            select(MediaAsset).where(MediaAsset.id == asset_id).with_for_update()
        )
        if asset is None:
            raise ApiError(404, "MEDIA_ASSET_NOT_FOUND", "媒体文件不存在")
        if asset.status in {MediaAssetStatus.READY, MediaAssetStatus.REJECTED}:
            return self._asset_public(asset)
        if asset.status != MediaAssetStatus.PROCESSING:
            raise ApiError(409, "MEDIA_NOT_PROCESSABLE", "媒体文件当前不可处理")
        try:
            raw = self.store.read_quarantine(asset.quarantine_object_key)
        except ObjectNotFoundError as exc:
            return self._reject_asset(asset, "QUARANTINE_OBJECT_MISSING", "隔离区文件不存在", exc)

        actual_hash = hashlib.sha256(raw).hexdigest()
        if actual_hash != asset.sha256 or len(raw) != asset.byte_size:
            self._scan(asset, MediaScanKind.HASH, MediaScanOutcome.FAILED, "HASH_MISMATCH", "sha256-v1")
            return self._reject_asset(asset, "HASH_MISMATCH", "文件哈希校验失败")
        self._scan(asset, MediaScanKind.HASH, MediaScanOutcome.PASSED, None, "sha256-v1")

        actual_mime = self._detect_mime(raw)
        if actual_mime is None or actual_mime != asset.declared_mime:
            self._scan(asset, MediaScanKind.SIGNATURE, MediaScanOutcome.FAILED, "SIGNATURE_MISMATCH", "magic-signature-v1")
            return self._reject_asset(asset, "SIGNATURE_MISMATCH", "文件真实格式与声明不一致")
        self._scan(asset, MediaScanKind.SIGNATURE, MediaScanOutcome.PASSED, None, "magic-signature-v1")

        try:
            virus = self.virus_scanner.scan(raw)
        except (OSError, RuntimeError) as exc:
            raise ApiError(503, "VIRUS_SCANNER_UNAVAILABLE", "文件安全检查暂时不可用") from exc
        if not virus.clean:
            self._scan(asset, MediaScanKind.VIRUS, MediaScanOutcome.FAILED, "MALWARE_DETECTED", virus.detector_version, {"signature": virus.signature})
            return self._reject_asset(asset, "MALWARE_DETECTED", "文件未通过安全检查")
        self._scan(asset, MediaScanKind.VIRUS, MediaScanOutcome.PASSED, None, virus.detector_version)

        try:
            sanitized, width, height, thumbnails = self._sanitize_image(raw, actual_mime)
        except ValueError as exc:
            code = str(exc)
            self._scan(asset, MediaScanKind.DECODE, MediaScanOutcome.FAILED, code, "pillow-12")
            return self._reject_asset(asset, code, "图片无法安全解码或像素尺寸过大")
        self._scan(asset, MediaScanKind.DECODE, MediaScanOutcome.PASSED, None, "pillow-12")
        self._scan(asset, MediaScanKind.PIXEL_LIMIT, MediaScanOutcome.PASSED, None, "pixel-limit-v1", {"width": width, "height": height})
        self._scan(asset, MediaScanKind.METADATA, MediaScanOutcome.PASSED, None, "metadata-strip-v1")

        if payload.content_safety_outcome == MediaScanOutcome.FAILED:
            self._scan(asset, MediaScanKind.CONTENT_SAFETY, MediaScanOutcome.FAILED, payload.content_reason_code or "CONTENT_UNSAFE", "external-content-review")
            return self._reject_asset(asset, payload.content_reason_code or "CONTENT_UNSAFE", "图片内容未通过安全检查")
        content_outcome = (
            payload.content_safety_outcome
            if payload.content_safety_outcome != MediaScanOutcome.NOT_RUN
            else MediaScanOutcome.REVIEW
        )
        self._scan(asset, MediaScanKind.CONTENT_SAFETY, content_outcome, payload.content_reason_code, "external-content-review")
        self._scan(asset, MediaScanKind.AIGC, MediaScanOutcome.PASSED, None, "aigc-observation-v1", {"detected": payload.aigc_detected})

        extension = MIME_TO_EXTENSION[actual_mime]
        original_key = f"users/{asset.owner_user_id}/assets/{asset.id}/sanitized.{extension}"
        self.store.write_private(original_key, sanitized, content_type=actual_mime)
        self.db.execute(delete(MediaDerivative).where(MediaDerivative.asset_id == asset.id))
        for size, (thumbnail_data, thumb_width, thumb_height) in thumbnails.items():
            kind = MediaDerivativeKind.THUMBNAIL_320 if size == 320 else MediaDerivativeKind.THUMBNAIL_640
            thumb_key = f"users/{asset.owner_user_id}/assets/{asset.id}/thumb-{size}.{extension}"
            self.store.write_private(thumb_key, thumbnail_data, content_type=actual_mime)
            self.db.add(
                MediaDerivative(
                    asset_id=asset.id,
                    kind=kind,
                    mime_type=actual_mime,
                    byte_size=len(thumbnail_data),
                    storage_key=thumb_key,
                    width=thumb_width,
                    height=thumb_height,
                )
            )
        asset.actual_mime = actual_mime
        asset.private_object_key = original_key
        asset.width = width
        asset.height = height
        asset.metadata_stripped = True
        asset.aigc_detected = payload.aigc_detected
        asset.status = MediaAssetStatus.READY
        asset.ready_at = utcnow()
        asset.row_version += 1
        asset.updated_at = utcnow()
        self.store.delete_quarantine(asset.quarantine_object_key)
        self._complete_outbox(asset.id, "MEDIA_PROCESS_REQUESTED")
        add_audit_event(
            self.db,
            actor_user_id=None,
            actor_type="SYSTEM",
            action="MEDIA_PROCESS_COMPLETED",
            target_type="MEDIA_ASSET",
            target_id=asset.id,
            result="READY",
            request_id=self.request_id,
            safe_diff={"actual_mime": actual_mime, "width": width, "height": height},
        )
        self.db.commit()
        return self._asset_public(asset)

    def process_deletion(self, asset_id: uuid.UUID) -> MediaDeleteAccepted:
        asset = self.db.scalar(
            select(MediaAsset).where(MediaAsset.id == asset_id).with_for_update()
        )
        if asset is None:
            raise ApiError(404, "MEDIA_ASSET_NOT_FOUND", "媒体文件不存在")
        if asset.status == MediaAssetStatus.DELETED:
            return MediaDeleteAccepted(id=asset.id, status=asset.status)
        if asset.status != MediaAssetStatus.DELETION_PENDING:
            raise ApiError(409, "MEDIA_NOT_DELETABLE", "媒体文件没有等待删除")
        if asset.private_object_key:
            self.store.delete_private(asset.private_object_key)
        derivatives = self.db.scalars(
            select(MediaDerivative).where(MediaDerivative.asset_id == asset.id)
        ).all()
        for derivative in derivatives:
            self.store.delete_private(derivative.storage_key)
        self.store.delete_quarantine(asset.quarantine_object_key)
        asset.status = MediaAssetStatus.DELETED
        asset.deleted_at = utcnow()
        asset.row_version += 1
        self._complete_outbox(asset.id, "MEDIA_DELETE_REQUESTED")
        self.db.commit()
        return MediaDeleteAccepted(id=asset.id, status=asset.status)

    def _require_asset(
        self, user: User, asset_id: uuid.UUID, *, for_update: bool = False
    ) -> MediaAsset:
        statement = select(MediaAsset).where(
            MediaAsset.id == asset_id,
            MediaAsset.owner_user_id == user.id,
        )
        if for_update:
            statement = statement.with_for_update()
        asset = self.db.scalar(statement)
        if asset is None:
            raise ApiError(404, "MEDIA_ASSET_NOT_FOUND", "媒体文件不存在")
        return asset

    def _asset_public(self, asset: MediaAsset) -> MediaAssetPublic:
        derivatives = self.db.scalars(
            select(MediaDerivative)
            .where(MediaDerivative.asset_id == asset.id)
            .order_by(MediaDerivative.kind)
        ).all()
        expires_at = utcnow() + timedelta(
            minutes=self.settings.media_download_ttl_minutes
        )
        original_url = None
        derivative_public: list[MediaDerivativePublic] = []
        if asset.status == MediaAssetStatus.READY and asset.private_object_key:
            original_url = self.store.presign_private_download(
                asset.private_object_key,
                expires=timedelta(minutes=self.settings.media_download_ttl_minutes),
            )
            derivative_public = [
                MediaDerivativePublic(
                    kind=item.kind,
                    width=item.width,
                    height=item.height,
                    mime_type=item.mime_type,
                    byte_size=item.byte_size,
                    url=self.store.presign_private_download(
                        item.storage_key,
                        expires=timedelta(
                            minutes=self.settings.media_download_ttl_minutes
                        ),
                    ),
                    expires_at=expires_at,
                )
                for item in derivatives
            ]
        return MediaAssetPublic(
            id=asset.id,
            purpose=asset.purpose,
            original_filename=asset.original_filename,
            status=asset.status,
            actual_mime=asset.actual_mime,
            byte_size=asset.byte_size,
            sha256=asset.sha256,
            width=asset.width,
            height=asset.height,
            metadata_stripped=asset.metadata_stripped,
            aigc_detected=asset.aigc_detected,
            rejection_code=asset.rejection_code,
            rejection_summary=asset.rejection_summary,
            original_url=original_url,
            url_expires_at=expires_at if original_url else None,
            derivatives=derivative_public,
            row_version=asset.row_version,
            created_at=asset.created_at,
            updated_at=asset.updated_at,
        )

    @staticmethod
    def _detect_mime(data: bytes) -> str | None:
        if data.startswith(b"\xff\xd8\xff"):
            return "image/jpeg"
        if data.startswith(b"\x89PNG\r\n\x1a\n"):
            return "image/png"
        if len(data) >= 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP":
            return "image/webp"
        return None

    def _sanitize_image(
        self, data: bytes, mime_type: str
    ) -> tuple[bytes, int, int, dict[int, tuple[bytes, int, int]]]:
        from PIL import Image, ImageOps, UnidentifiedImageError

        expected_format = next(
            name for name, mime in FORMAT_TO_MIME.items() if mime == mime_type
        )
        try:
            with warnings.catch_warnings():
                warnings.simplefilter("error", Image.DecompressionBombWarning)
                with Image.open(io.BytesIO(data), formats=[expected_format]) as opened:
                    width, height = opened.size
                    if width <= 0 or height <= 0:
                        raise ValueError("INVALID_DIMENSIONS")
                    if width * height > self.settings.media_max_image_pixels:
                        raise ValueError("PIXEL_LIMIT_EXCEEDED")
                    opened.load()
                    normalized = ImageOps.exif_transpose(opened)
                    if mime_type == "image/jpeg":
                        normalized = normalized.convert("RGB")
                    elif normalized.mode not in {"RGB", "RGBA", "L", "LA"}:
                        normalized = normalized.convert("RGBA")
                    sanitized = self._encode_image(normalized, expected_format)
                    thumbnails: dict[int, tuple[bytes, int, int]] = {}
                    for size in (320, 640):
                        thumb = normalized.copy()
                        thumb.thumbnail((size, size), Image.Resampling.LANCZOS)
                        encoded = self._encode_image(thumb, expected_format)
                        thumbnails[size] = (encoded, thumb.width, thumb.height)
                    return sanitized, width, height, thumbnails
        except (Image.DecompressionBombWarning, Image.DecompressionBombError):
            raise ValueError("PIXEL_LIMIT_EXCEEDED") from None
        except (UnidentifiedImageError, OSError, SyntaxError):
            raise ValueError("IMAGE_DECODE_FAILED") from None

    @staticmethod
    def _encode_image(image: object, image_format: str) -> bytes:
        output = io.BytesIO()
        kwargs: dict[str, object] = {}
        if image_format == "JPEG":
            kwargs = {"quality": 85, "exif": b"", "icc_profile": None}
        elif image_format == "WEBP":
            kwargs = {"quality": 85, "exif": b"", "icc_profile": None}
        image.save(output, format=image_format, **kwargs)  # type: ignore[attr-defined]
        return output.getvalue()

    def _scan(
        self,
        asset: MediaAsset,
        kind: MediaScanKind,
        outcome: MediaScanOutcome,
        reason_code: str | None,
        detector_version: str,
        details: dict | None = None,
    ) -> None:
        self.db.add(
            MediaScanResult(
                asset_id=asset.id,
                scan_kind=kind,
                outcome=outcome,
                reason_code=reason_code,
                detector_version=detector_version,
                details=details or {},
            )
        )

    def _reject_asset(
        self,
        asset: MediaAsset,
        code: str,
        summary: str,
        cause: Exception | None = None,
    ) -> MediaAssetPublic:
        del cause
        asset.status = MediaAssetStatus.REJECTED
        asset.rejection_code = code
        asset.rejection_summary = summary
        asset.row_version += 1
        asset.updated_at = utcnow()
        self.store.delete_quarantine(asset.quarantine_object_key)
        self._complete_outbox(asset.id, "MEDIA_PROCESS_REQUESTED", failed=True, error=code)
        add_audit_event(
            self.db,
            actor_user_id=None,
            actor_type="SYSTEM",
            action="MEDIA_PROCESS_REJECTED",
            target_type="MEDIA_ASSET",
            target_id=asset.id,
            result="REJECTED",
            request_id=self.request_id,
            safe_diff={"reason_code": code},
        )
        self.db.commit()
        return self._asset_public(asset)

    def _complete_outbox(
        self,
        aggregate_id: uuid.UUID,
        event_type: str,
        *,
        failed: bool = False,
        error: str | None = None,
    ) -> None:
        event = self.db.scalar(
            select(OutboxEvent)
            .where(
                OutboxEvent.aggregate_id == aggregate_id,
                OutboxEvent.event_type == event_type,
                OutboxEvent.status.in_([OutboxStatus.PENDING, OutboxStatus.PROCESSING]),
            )
            .order_by(OutboxEvent.created_at)
            .limit(1)
        )
        if event is not None:
            event.status = OutboxStatus.FAILED if failed else OutboxStatus.COMPLETED
            event.last_error_code = error
            event.processed_at = utcnow()
            event.attempts += 1
