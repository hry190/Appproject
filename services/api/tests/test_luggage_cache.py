from __future__ import annotations

import uuid
from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from redis.exceptions import ConnectionError as RedisConnectionError
from sqlalchemy import event

from app.domains.catalog.seed import seed_catalog_data
from app.domains.creations.models import (
    CreationMediaType,
    CreationProject,
    CreationVersion,
    CreationVisibility,
)
from app.domains.luggage.cache import InMemoryLuggageCache, RedisLuggageCache


@pytest.fixture
def seeded_client(app: FastAPI) -> Iterator[TestClient]:
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    with TestClient(app) as client:
        yield client


def register(client: TestClient, phone: str) -> tuple[dict[str, str], str]:
    assert client.post(
        "/v1/auth/verification-codes",
        json={"phone": phone, "purpose": "REGISTER"},
    ).status_code == 202
    response = client.post(
        "/v1/auth/register",
        json={
            "phone": phone,
            "verification_code": "123456",
            "password": "StrongPass!8",
            "age_band": "AGE_14_TO_17",
            "terms_version": "2026-08",
            "privacy_version": "2026-08",
        },
    )
    assert response.status_code == 201, response.text
    body = response.json()
    return (
        {"Authorization": f"Bearer {body['tokens']['access_token']}"},
        body["user"]["id"],
    )


def count_selects(app: FastAPI, operation) -> tuple[int, object]:
    statements: list[str] = []

    def capture(
        _connection,
        _cursor,
        statement: str,
        _parameters,
        _context,
        _executemany,
    ) -> None:
        if statement.lstrip().upper().startswith("SELECT"):
            statements.append(statement)

    event.listen(app.state.engine, "before_cursor_execute", capture)
    try:
        result = operation()
    finally:
        event.remove(app.state.engine, "before_cursor_execute", capture)
    return len(statements), result


def test_warm_luggage_uses_one_auth_query_and_preserves_etag(
    seeded_client: TestClient,
) -> None:
    headers, _user_id = register(seeded_client, "13970000001")
    cache = seeded_client.app.state.luggage_cache
    assert isinstance(cache, InMemoryLuggageCache)

    cold_selects, cold = count_selects(
        seeded_client.app,
        lambda: seeded_client.get("/v1/me/luggage", headers=headers),
    )
    warm_selects, warm = count_selects(
        seeded_client.app,
        lambda: seeded_client.get("/v1/me/luggage", headers=headers),
    )

    assert cold.status_code == warm.status_code == 200
    assert warm_selects == 1
    assert cold_selects > warm_selects
    assert warm.json()["meta"] == cold.json()["meta"]
    assert warm.headers["cache-control"] == "no-store"
    assert warm.headers["vary"] == "Authorization"
    assert cache.stats.hits >= 1

    not_modified = seeded_client.get(
        "/v1/me/luggage",
        headers={**headers, "If-None-Match": warm.headers["etag"]},
    )
    assert not_modified.status_code == 304
    assert not_modified.headers["etag"] == warm.headers["etag"]


def test_commit_invalidates_only_the_affected_users_snapshot(
    seeded_client: TestClient,
) -> None:
    first_headers, _ = register(seeded_client, "13970000002")
    second_headers, _ = register(seeded_client, "13970000003")
    first_before = seeded_client.get("/v1/me/luggage", headers=first_headers)
    second_before = seeded_client.get("/v1/me/luggage", headers=second_headers)
    profile = seeded_client.get("/v1/profile", headers=first_headers).json()

    updated = seeded_client.patch(
        "/v1/profile",
        headers=first_headers,
        json={"nickname": "缓存已失效", "row_version": profile["row_version"]},
    )
    assert updated.status_code == 200, updated.text

    second_after = seeded_client.get("/v1/me/luggage", headers=second_headers)
    first_after = seeded_client.get("/v1/me/luggage", headers=first_headers)
    assert second_after.json()["meta"] == second_before.json()["meta"]
    assert first_after.json()["data"]["profile"]["nickname"] == "缓存已失效"
    assert first_after.headers["etag"] != first_before.headers["etag"]


class FailingLuggageCache:
    def get(self, _user_id):
        raise RuntimeError("cache unavailable")

    def set(self, _user_id, _value) -> None:
        raise RuntimeError("cache unavailable")

    def invalidate_many(self, _user_ids) -> None:
        raise RuntimeError("cache unavailable")

    def invalidate_all(self) -> None:
        raise RuntimeError("cache unavailable")


class DisconnectedRedis:
    def get(self, _key):
        raise RedisConnectionError("redis unavailable")

    def setex(self, _key, _ttl, _value):
        raise RedisConnectionError("redis unavailable")

    def delete(self, *_keys):
        raise RedisConnectionError("redis unavailable")

    def scan_iter(self, **_kwargs):
        raise RedisConnectionError("redis unavailable")


def test_cache_failure_does_not_break_reads_or_committed_writes(
    seeded_client: TestClient,
) -> None:
    headers, _ = register(seeded_client, "13970000004")
    seeded_client.app.state.luggage_cache = FailingLuggageCache()

    luggage = seeded_client.get("/v1/me/luggage", headers=headers)
    assert luggage.status_code == 200, luggage.text
    profile = seeded_client.get("/v1/profile", headers=headers).json()
    updated = seeded_client.patch(
        "/v1/profile",
        headers=headers,
        json={"nickname": "无缓存也可用", "row_version": profile["row_version"]},
    )
    assert updated.status_code == 200, updated.text
    assert seeded_client.get("/v1/me/luggage", headers=headers).json()["data"][
        "profile"
    ]["nickname"] == "无缓存也可用"


def test_redis_cache_adapter_fails_open_when_redis_is_disconnected() -> None:
    cache = RedisLuggageCache(
        DisconnectedRedis(),  # type: ignore[arg-type]
        prefix="test",
        ttl_seconds=30,
    )
    user_id = uuid.uuid4()

    assert cache.get(user_id) is None
    cache.invalidate_many([user_id])
    cache.invalidate_all()


def test_creation_query_count_does_not_grow_with_project_count(
    seeded_client: TestClient,
) -> None:
    headers, raw_user_id = register(seeded_client, "13970000005")
    user_id = uuid.UUID(raw_user_id)
    assert seeded_client.get("/v1/profile", headers=headers).status_code == 200

    def add_projects(count: int) -> None:
        with seeded_client.app.state.session_factory() as db:
            for index in range(count):
                project_id = uuid.uuid4()
                version_id = uuid.uuid4()
                db.add(
                    CreationProject(
                        id=project_id,
                        owner_user_id=user_id,
                        title=f"查询定界作品 {index}",
                        media_type=CreationMediaType.ILLUSTRATION,
                        default_visibility=CreationVisibility.PRIVATE,
                        current_version_number=1,
                    )
                )
                db.add(
                    CreationVersion(
                        id=version_id,
                        project_id=project_id,
                        version_number=1,
                        parent_version_id=None,
                        created_by_user_id=user_id,
                        layer_manifest=[
                            {
                                "layer_id": "text-1",
                                "kind": "TEXT",
                                "name": "题字",
                                "z_index": 0,
                                "visible": True,
                                "text_content": "竹影",
                            }
                        ],
                        layer_count=1,
                        canvas_width=800,
                        canvas_height=600,
                        preview_asset_id=None,
                        change_summary="查询定界",
                    )
                )
            db.commit()

    add_projects(1)
    one_project_selects, first = count_selects(
        seeded_client.app,
        lambda: seeded_client.get("/v1/me/luggage", headers=headers),
    )
    assert first.status_code == 200

    add_projects(11)
    twelve_project_selects, second = count_selects(
        seeded_client.app,
        lambda: seeded_client.get("/v1/me/luggage", headers=headers),
    )
    assert second.status_code == 200
    assert twelve_project_selects == one_project_selects
    assert sum(second.json()["data"]["creations"]["counts_by_status"].values()) == 12
