from __future__ import annotations

import base64
import hashlib
import hmac
import io
import json
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from typing import Protocol

from app.core.config import DEV_JWT_SECRET, Settings


class ObjectNotFoundError(Exception):
    pass


@dataclass(frozen=True, slots=True)
class StoredObjectInfo:
    byte_size: int
    content_type: str | None


@dataclass(frozen=True, slots=True)
class PresignedUrl:
    url: str
    required_headers: dict[str, str]


class ObjectStore(Protocol):
    def presign_upload(
        self, object_key: str, *, content_type: str, expires: timedelta
    ) -> PresignedUrl: ...

    def head_quarantine(self, object_key: str) -> StoredObjectInfo: ...

    def read_quarantine(self, object_key: str) -> bytes: ...

    def write_quarantine(
        self, object_key: str, data: bytes, *, content_type: str
    ) -> None: ...

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None: ...

    def read_private(self, object_key: str) -> bytes: ...

    def read_private_with_type(self, object_key: str) -> tuple[bytes, str]: ...

    def presign_private_download(self, object_key: str, *, expires: timedelta) -> str: ...

    def delete_quarantine(self, object_key: str) -> None: ...

    def delete_private(self, object_key: str) -> None: ...


class InMemoryObjectStore:
    """Test/development store. Production settings reject this provider."""

    def __init__(
        self,
        public_base_url: str = "http://127.0.0.1:8010",
        signing_secret: str = DEV_JWT_SECRET,
    ) -> None:
        self.quarantine: dict[str, tuple[bytes, str]] = {}
        self.private: dict[str, tuple[bytes, str]] = {}
        self.public_base_url = public_base_url.rstrip("/")
        self.signing_secret = signing_secret

    def presign_upload(
        self, object_key: str, *, content_type: str, expires: timedelta
    ) -> PresignedUrl:
        del expires
        return PresignedUrl(
            url=f"memory://quarantine/{object_key}",
            required_headers={"Content-Type": content_type},
        )

    def put_test_object(self, object_key: str, data: bytes, content_type: str) -> None:
        self.quarantine[object_key] = (data, content_type)

    def head_quarantine(self, object_key: str) -> StoredObjectInfo:
        try:
            data, content_type = self.quarantine[object_key]
        except KeyError as exc:
            raise ObjectNotFoundError(object_key) from exc
        return StoredObjectInfo(byte_size=len(data), content_type=content_type)

    def read_quarantine(self, object_key: str) -> bytes:
        try:
            return self.quarantine[object_key][0]
        except KeyError as exc:
            raise ObjectNotFoundError(object_key) from exc

    def write_quarantine(
        self, object_key: str, data: bytes, *, content_type: str
    ) -> None:
        self.quarantine[object_key] = (data, content_type)

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None:
        self.private[object_key] = (data, content_type)

    def read_private(self, object_key: str) -> bytes:
        try:
            return self.private[object_key][0]
        except KeyError as exc:
            raise ObjectNotFoundError(object_key) from exc

    def read_private_with_type(self, object_key: str) -> tuple[bytes, str]:
        try:
            return self.private[object_key]
        except KeyError as exc:
            raise ObjectNotFoundError(object_key) from exc

    def presign_private_download(self, object_key: str, *, expires: timedelta) -> str:
        if object_key not in self.private:
            raise ObjectNotFoundError(object_key)
        payload = json.dumps(
            {
                "key": object_key,
                "expires_at": int(datetime.now(UTC).timestamp() + expires.total_seconds()),
            },
            separators=(",", ":"),
            sort_keys=True,
        ).encode()
        encoded = base64.urlsafe_b64encode(payload).decode().rstrip("=")
        signature = hmac.new(
            self.signing_secret.encode(), payload, hashlib.sha256
        ).hexdigest()
        return f"{self.public_base_url}/v1/media-downloads/{encoded}.{signature}"

    def read_signed_private(self, token: str) -> tuple[bytes, str, int]:
        try:
            encoded, supplied_signature = token.split(".", 1)
            payload_bytes = base64.urlsafe_b64decode(encoded + "=" * (-len(encoded) % 4))
            expected_signature = hmac.new(
                self.signing_secret.encode(), payload_bytes, hashlib.sha256
            ).hexdigest()
            if not hmac.compare_digest(supplied_signature, expected_signature):
                raise ValueError("signature")
            payload = json.loads(payload_bytes)
            remaining_seconds = int(payload["expires_at"]) - int(datetime.now(UTC).timestamp())
            if remaining_seconds <= 0:
                raise ValueError("expired")
            data, content_type = self.read_private_with_type(str(payload["key"]))
            return data, content_type, remaining_seconds
        except (KeyError, TypeError, ValueError, json.JSONDecodeError, ObjectNotFoundError):
            raise ObjectNotFoundError("signed download") from None

    def delete_quarantine(self, object_key: str) -> None:
        self.quarantine.pop(object_key, None)

    def delete_private(self, object_key: str) -> None:
        self.private.pop(object_key, None)


class MinioObjectStore:
    def __init__(self, settings: Settings) -> None:
        from minio import Minio

        credentials = {
            "access_key": settings.minio_access_key,
            "secret_key": settings.minio_secret_key.get_secret_value(),
            "region": settings.minio_region,
        }
        self.client = Minio(
            settings.minio_endpoint,
            secure=settings.minio_secure,
            **credentials,
        )
        public_endpoint = settings.minio_public_endpoint or settings.minio_endpoint
        self.signing_client = Minio(
            public_endpoint,
            secure=(
                settings.minio_public_secure
                if settings.minio_public_endpoint
                else settings.minio_secure
            ),
            **credentials,
        )
        self.quarantine_bucket = settings.minio_quarantine_bucket
        self.private_bucket = settings.minio_private_bucket
        from minio.error import S3Error

        for bucket in (self.quarantine_bucket, self.private_bucket):
            try:
                if not self.client.bucket_exists(bucket):
                    self.client.make_bucket(bucket)
            except S3Error as exc:
                if exc.code not in {"BucketAlreadyOwnedByYou", "BucketAlreadyExists"}:
                    raise

    def presign_upload(
        self, object_key: str, *, content_type: str, expires: timedelta
    ) -> PresignedUrl:
        return PresignedUrl(
            url=self.signing_client.presigned_put_object(
                self.quarantine_bucket,
                object_key,
                expires=expires,
            ),
            required_headers={"Content-Type": content_type},
        )

    def head_quarantine(self, object_key: str) -> StoredObjectInfo:
        from minio.error import S3Error

        try:
            result = self.client.stat_object(self.quarantine_bucket, object_key)
        except S3Error as exc:
            if exc.code in {"NoSuchKey", "NoSuchObject", "NotFound"}:
                raise ObjectNotFoundError(object_key) from exc
            raise
        return StoredObjectInfo(
            byte_size=result.size,
            content_type=result.content_type,
        )

    def read_quarantine(self, object_key: str) -> bytes:
        from minio.error import S3Error

        response = None
        try:
            response = self.client.get_object(self.quarantine_bucket, object_key)
            return response.read()
        except S3Error as exc:
            if exc.code in {"NoSuchKey", "NoSuchObject", "NotFound"}:
                raise ObjectNotFoundError(object_key) from exc
            raise
        finally:
            if response is not None:
                response.close()
                response.release_conn()

    def write_quarantine(
        self, object_key: str, data: bytes, *, content_type: str
    ) -> None:
        self.client.put_object(
            self.quarantine_bucket,
            object_key,
            io.BytesIO(data),
            len(data),
            content_type=content_type,
        )

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None:
        self.client.put_object(
            self.private_bucket,
            object_key,
            io.BytesIO(data),
            len(data),
            content_type=content_type,
        )

    def read_private(self, object_key: str) -> bytes:
        from minio.error import S3Error

        response = None
        try:
            response = self.client.get_object(self.private_bucket, object_key)
            return response.read()
        except S3Error as exc:
            if exc.code in {"NoSuchKey", "NoSuchObject", "NotFound"}:
                raise ObjectNotFoundError(object_key) from exc
            raise
        finally:
            if response is not None:
                response.close()
                response.release_conn()

    def read_private_with_type(self, object_key: str) -> tuple[bytes, str]:
        from minio.error import S3Error

        response = None
        try:
            response = self.client.get_object(self.private_bucket, object_key)
            content_type = response.headers.get("Content-Type") or "application/octet-stream"
            return response.read(), content_type
        except S3Error as exc:
            if exc.code in {"NoSuchKey", "NoSuchObject", "NotFound"}:
                raise ObjectNotFoundError(object_key) from exc
            raise
        finally:
            if response is not None:
                response.close()
                response.release_conn()

    def presign_private_download(self, object_key: str, *, expires: timedelta) -> str:
        return self.signing_client.presigned_get_object(
            self.private_bucket,
            object_key,
            expires=expires,
        )

    def delete_quarantine(self, object_key: str) -> None:
        self.client.remove_object(self.quarantine_bucket, object_key)

    def delete_private(self, object_key: str) -> None:
        self.client.remove_object(self.private_bucket, object_key)


def build_object_store(settings: Settings) -> ObjectStore:
    if settings.media_storage_provider == "minio":
        return MinioObjectStore(settings)
    return InMemoryObjectStore(
        public_base_url=settings.media_memory_public_base_url,
        signing_secret=settings.jwt_secret.get_secret_value(),
    )
