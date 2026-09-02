from __future__ import annotations

import pytest
from cryptography.fernet import Fernet
from pydantic import ValidationError

from app.core.config import Settings


def test_luggage_cache_must_expire_before_signed_media_urls() -> None:
    with pytest.raises(ValidationError, match="cache TTL"):
        Settings(
            _env_file=None,
            environment="test",
            luggage_cache_ttl_seconds=60,
            media_download_ttl_minutes=1,
        )


def test_production_rejects_development_security_defaults() -> None:
    with pytest.raises(ValidationError, match="production secrets"):
        Settings(_env_file=None, environment="production")


def test_production_rejects_fixed_code_and_noop_sms() -> None:
    common = {
        "environment": "production",
        "database_url": "postgresql+psycopg://app:secret@db/app",
        "allowed_hosts": ["api.example.test"],
        "jwt_secret": "production-jwt-secret-that-is-long-and-random-enough",
        "phone_encryption_key": Fernet.generate_key().decode(),
        "phone_lookup_key": "production-phone-lookup-key-at-least-32-characters",
        "verification_code_key": "production-code-digest-key-at-least-32-characters",
    }
    with pytest.raises(ValidationError, match="fixed verification codes"):
        Settings(
            _env_file=None,
            **common,
            fixed_verification_code="123456",
            sms_provider="aliyun",
        )

    with pytest.raises(ValidationError, match="real SMS provider"):
        Settings(
            _env_file=None,
            **common,
            fixed_verification_code=None,
            sms_provider="noop",
        )


def test_production_configuration_accepts_explicit_secure_values() -> None:
    settings = Settings(
        _env_file=None,
        environment="production",
        database_url="postgresql+psycopg://app:secret@db/app",
        allowed_hosts=["api.example.test"],
        jwt_secret="production-jwt-secret-that-is-long-and-random-enough",
        phone_encryption_key=Fernet.generate_key().decode(),
        phone_lookup_key="production-phone-lookup-key-at-least-32-characters",
        verification_code_key="production-code-digest-key-at-least-32-characters",
        fixed_verification_code=None,
        sms_provider="tencent",
        media_storage_provider="minio",
        media_virus_scanner="clamav",
        minio_secret_key="production-minio-secret-value",
        internal_worker_token="production-internal-worker-token-long-enough-123",
    )
    assert settings.environment == "production"
