from __future__ import annotations

import io
from dataclasses import dataclass
from datetime import timedelta
from typing import Protocol

from app.core.config import Settings


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

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None: ...

    def presign_private_download(self, object_key: str, *, expires: timedelta) -> str: ...

    def delete_quarantine(self, object_key: str) -> None: ...

    def delete_private(self, object_key: str) -> None: ...


class InMemoryObjectStore:
    """Test/development store. Production settings reject this provider."""

    def __init__(self) -> None:
        self.quarantine: dict[str, tuple[bytes, str]] = {}
        self.private: dict[str, tuple[bytes, str]] = {}

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

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None:
        self.private[object_key] = (data, content_type)

    def presign_private_download(self, object_key: str, *, expires: timedelta) -> str:
        if object_key not in self.private:
            raise ObjectNotFoundError(object_key)
        seconds = int(expires.total_seconds())
        return f"memory://private/{object_key}?expires_in={seconds}"

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

    def write_private(self, object_key: str, data: bytes, *, content_type: str) -> None:
        self.client.put_object(
            self.private_bucket,
            object_key,
            io.BytesIO(data),
            len(data),
            content_type=content_type,
        )

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
    return InMemoryObjectStore()
