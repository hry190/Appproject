from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.pool import StaticPool

from app.core.config import Settings
from app.db import Base
from app.main import create_app
from app.sms import NoopSmsProvider
from app.stores import InMemoryRateLimiter, InMemoryVerificationStore


@pytest.fixture
def settings() -> Settings:
    return Settings(
        _env_file=None,
        environment="test",
        database_url="sqlite+pysqlite://",
        allowed_hosts=["testserver"],
        max_request_bytes=1024,
        fixed_verification_code="123456",
    )


@pytest.fixture
def app(settings: Settings) -> Iterator[FastAPI]:
    engine = create_engine(
        "sqlite+pysqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(engine)
    application = create_app(
        settings=settings,
        engine=engine,
        verification_store=InMemoryVerificationStore(),
        rate_limiter=InMemoryRateLimiter(),
        sms_provider=NoopSmsProvider(),
    )
    yield application
    Base.metadata.drop_all(engine)
    engine.dispose()


@pytest.fixture
def client(app: FastAPI) -> Iterator[TestClient]:
    with TestClient(app) as test_client:
        yield test_client
