from __future__ import annotations

from datetime import timedelta

from app.core.config import Settings
from app.domains.media.storage import MinioObjectStore


class FakeMinio:
    def __init__(
        self,
        endpoint: str,
        *,
        access_key: str,
        secret_key: str,
        secure: bool,
        region: str,
    ) -> None:
        del access_key, secret_key
        self.endpoint = endpoint
        self.scheme = "https" if secure else "http"
        self.region = region

    def bucket_exists(self, _bucket: str) -> bool:
        return True

    def presigned_put_object(self, bucket: str, key: str, *, expires) -> str:
        del expires
        return f"{self.scheme}://{self.endpoint}/{bucket}/{key}?signed=put"

    def presigned_get_object(self, bucket: str, key: str, *, expires) -> str:
        del expires
        return f"{self.scheme}://{self.endpoint}/{bucket}/{key}?signed=get"


def test_minio_uses_internal_endpoint_for_io_and_public_endpoint_for_signing(
    monkeypatch,
) -> None:
    monkeypatch.setattr("minio.Minio", FakeMinio)
    settings = Settings(
        _env_file=None,
        environment="test",
        media_storage_provider="minio",
        minio_endpoint="minio:9000",
        minio_public_endpoint="10.0.2.2:19000",
        minio_public_secure=False,
    )

    store = MinioObjectStore(settings)
    upload = store.presign_upload(
        "users/u/uploads/image.png",
        content_type="image/png",
        expires=timedelta(minutes=15),
    )
    download = store.presign_private_download(
        "users/u/private/image.png",
        expires=timedelta(minutes=5),
    )

    assert store.client.endpoint == "minio:9000"
    assert upload.url.startswith("http://10.0.2.2:19000/")
    assert download.startswith("http://10.0.2.2:19000/")
