from __future__ import annotations

import json
from functools import lru_cache
from typing import Literal

from cryptography.fernet import Fernet
from pydantic import Field, SecretStr, field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


DEV_JWT_SECRET = "dev-only-jwt-secret-change-before-shared-deployment-1234567890"
DEV_PHONE_LOOKUP_KEY = "dev-only-phone-lookup-key-change-before-deployment"
DEV_CODE_KEY = "dev-only-verification-code-key-change-before-deployment"
DEV_PHONE_ENCRYPTION_KEY = "MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA="


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_prefix="JIANGHU_",
        extra="ignore",
        case_sensitive=False,
    )

    app_name: str = "机巧江湖 API"
    environment: Literal["development", "test", "production"] = "development"
    database_url: str = "sqlite+pysqlite:///./jianghu.db"
    redis_url: str = "redis://127.0.0.1:6379/0"
    redis_prefix: str = "jianghu"
    luggage_cache_ttl_seconds: int = Field(default=30, ge=5, le=120)

    allowed_hosts: list[str] = Field(
        default_factory=lambda: ["127.0.0.1", "localhost", "10.0.2.2", "testserver"]
    )
    max_request_bytes: int = Field(default=64 * 1024, ge=1024, le=1024 * 1024)

    jwt_secret: SecretStr = Field(default=SecretStr(DEV_JWT_SECRET), min_length=32)
    jwt_algorithm: Literal["HS256"] = "HS256"
    jwt_issuer: str = "jiqiao-jianghu-api"
    jwt_audience: str = "jiqiao-jianghu-app"
    access_token_minutes: int = Field(default=15, ge=5, le=60)
    refresh_token_days: int = Field(default=30, ge=1, le=90)
    guardian_token_minutes: int = Field(default=10, ge=5, le=30)

    phone_encryption_key: SecretStr = SecretStr(DEV_PHONE_ENCRYPTION_KEY)
    phone_lookup_key: SecretStr = Field(
        default=SecretStr(DEV_PHONE_LOOKUP_KEY), min_length=32
    )
    verification_code_key: SecretStr = Field(
        default=SecretStr(DEV_CODE_KEY), min_length=32
    )

    current_terms_version: str = Field(default="2026-08", min_length=1, max_length=32)
    current_privacy_version: str = Field(default="2026-08", min_length=1, max_length=32)

    verification_code_ttl_seconds: int = Field(default=300, ge=120, le=900)
    verification_code_attempts: int = Field(default=5, ge=3, le=10)
    verification_code_cooldown_seconds: int = Field(default=60, ge=30, le=300)
    verification_code_daily_limit: int = Field(default=10, ge=3, le=30)
    fixed_verification_code: str | None = "123456"
    sms_provider: Literal["noop", "aliyun", "tencent"] = "noop"

    media_storage_provider: Literal["memory", "minio"] = "memory"
    media_max_upload_bytes: int = Field(
        default=20 * 1024 * 1024,
        ge=1024,
        le=50 * 1024 * 1024,
    )
    media_max_image_pixels: int = Field(
        default=40_000_000,
        ge=1_000_000,
        le=100_000_000,
    )
    media_upload_ttl_minutes: int = Field(default=15, ge=5, le=60)
    media_download_ttl_minutes: int = Field(default=5, ge=1, le=30)
    minio_endpoint: str = "127.0.0.1:19000"
    minio_public_endpoint: str | None = None
    minio_region: str = "us-east-1"
    minio_access_key: str = "jianghu-local"
    minio_secret_key: SecretStr = SecretStr("jianghu-local-secret-change-me")
    minio_secure: bool = False
    minio_public_secure: bool = False
    minio_quarantine_bucket: str = "jianghu-quarantine"
    minio_private_bucket: str = "jianghu-private"
    media_virus_scanner: Literal["development", "clamav"] = "development"
    clamav_host: str = "127.0.0.1"
    clamav_port: int = Field(default=3310, ge=1, le=65535)
    internal_worker_token: SecretStr = Field(
        default=SecretStr("dev-internal-worker-token-change-me-123456"),
        min_length=32,
    )

    login_failure_limit: int = Field(default=5, ge=3, le=10)
    login_lock_minutes: int = Field(default=15, ge=5, le=60)

    @field_validator("allowed_hosts", mode="before")
    @classmethod
    def parse_allowed_hosts(cls, value: object) -> object:
        if isinstance(value, str):
            stripped = value.strip()
            if stripped.startswith("["):
                return json.loads(stripped)
            return [item.strip() for item in stripped.split(",") if item.strip()]
        return value

    @field_validator("fixed_verification_code")
    @classmethod
    def validate_fixed_code(cls, value: str | None) -> str | None:
        if value is not None and (len(value) != 6 or not value.isdigit()):
            raise ValueError("fixed_verification_code must contain exactly six digits")
        return value

    @field_validator("phone_encryption_key")
    @classmethod
    def validate_phone_encryption_key(cls, value: SecretStr) -> SecretStr:
        try:
            Fernet(value.get_secret_value().encode())
        except (TypeError, ValueError) as exc:
            raise ValueError("phone_encryption_key must be a valid Fernet key") from exc
        return value

    @model_validator(mode="after")
    def validate_secure_production_settings(self) -> "Settings":
        if self.luggage_cache_ttl_seconds >= self.media_download_ttl_minutes * 60:
            raise ValueError(
                "luggage cache TTL must be shorter than signed media URL lifetime"
            )
        if self.environment != "production":
            return self

        insecure_values = {
            self.jwt_secret.get_secret_value(): DEV_JWT_SECRET,
            self.phone_lookup_key.get_secret_value(): DEV_PHONE_LOOKUP_KEY,
            self.verification_code_key.get_secret_value(): DEV_CODE_KEY,
            self.phone_encryption_key.get_secret_value(): DEV_PHONE_ENCRYPTION_KEY,
        }
        if any(actual == default for actual, default in insecure_values.items()):
            raise ValueError("production secrets must be explicitly configured")
        if self.fixed_verification_code is not None:
            raise ValueError("fixed verification codes are forbidden in production")
        if self.sms_provider == "noop":
            raise ValueError("a real SMS provider is required in production")
        if not self.database_url.startswith("postgresql"):
            raise ValueError("production must use PostgreSQL")
        if "*" in self.allowed_hosts:
            raise ValueError("wildcard hosts are forbidden in production")
        if self.media_storage_provider != "minio":
            raise ValueError("production media storage must use MinIO/S3")
        if self.media_virus_scanner != "clamav":
            raise ValueError("production media scanning must use ClamAV")
        if self.minio_secret_key.get_secret_value() == "jianghu-local-secret-change-me":
            raise ValueError("production MinIO credentials must be replaced")
        if (
            self.internal_worker_token.get_secret_value()
            == "dev-internal-worker-token-change-me-123456"
        ):
            raise ValueError("production internal worker token must be replaced")
        return self


@lru_cache
def get_settings() -> Settings:
    return Settings()
