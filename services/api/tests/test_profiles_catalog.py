from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.domains.catalog.seed import seed_catalog_data


OTP = "123456"
TERMS_VERSION = "2026-08"
PRIVACY_VERSION = "2026-08"


@pytest.fixture
def seeded_client(app: FastAPI) -> Iterator[TestClient]:
    with app.state.session_factory() as db:
        seed_catalog_data(db)
        seed_catalog_data(db)
    with TestClient(app) as client:
        yield client


def register(client: TestClient, phone: str) -> dict:
    code = client.post(
        "/v1/auth/verification-codes",
        json={"phone": phone, "purpose": "REGISTER"},
    )
    assert code.status_code == 202, code.text
    response = client.post(
        "/v1/auth/register",
        json={
            "phone": phone,
            "verification_code": OTP,
            "password": "StrongPass!8",
            "age_band": "AGE_14_TO_17",
            "terms_version": TERMS_VERSION,
            "privacy_version": PRIVACY_VERSION,
            "device_name": "pytest-catalog",
        },
    )
    assert response.status_code == 201, response.text
    return response.json()


def bearer(auth: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {auth['tokens']['access_token']}"}


def test_profile_is_created_once_and_uses_optimistic_locking(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13920000001")
    headers = bearer(auth)

    first = seeded_client.get("/v1/profile", headers=headers)
    second = seeded_client.get("/v1/profile", headers=headers)
    assert first.status_code == second.status_code == 200
    profile = first.json()
    assert second.json()["anonymous_id"] == profile["anonymous_id"]
    assert profile["anonymous_id"].startswith("JH-")
    assert profile["profile_visibility"] == "PRIVATE"
    assert profile["current_title"]["code"] == "APPRENTICE"
    assert profile["current_title"]["selected"] is True
    assert seeded_client.get("/v1/profile/titles", headers=headers).json()[0][
        "code"
    ] == "APPRENTICE"
    assert seeded_client.get("/v1/profile/badges", headers=headers).json() == []

    updated = seeded_client.patch(
        "/v1/profile",
        headers=headers,
        json={
            "nickname": "墨竹少侠",
            "class_label": "五（三）班",
            "profile_visibility": "GUARDIAN_ONLY",
            "row_version": profile["row_version"],
        },
    )
    assert updated.status_code == 200, updated.text
    assert updated.json()["nickname"] == "墨竹少侠"
    assert updated.json()["class_label"] == "五（三）班"
    assert updated.json()["row_version"] == profile["row_version"] + 1

    stale = seeded_client.patch(
        "/v1/profile",
        headers=headers,
        json={"nickname": "过期资料", "row_version": profile["row_version"]},
    )
    assert stale.status_code == 409
    assert stale.json()["error"]["code"] == "VERSION_CONFLICT"


def test_profile_rejects_invalid_class_and_unowned_title(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13920000002")
    headers = bearer(auth)
    profile = seeded_client.get("/v1/profile", headers=headers).json()

    invalid_class = seeded_client.patch(
        "/v1/profile",
        headers=headers,
        json={"class_label": "某某学校<script>", "row_version": profile["row_version"]},
    )
    assert invalid_class.status_code == 422

    unknown_title = seeded_client.patch(
        "/v1/profile",
        headers=headers,
        json={
            "current_title_id": "00000000-0000-0000-0000-000000000001",
            "row_version": profile["row_version"],
        },
    )
    assert unknown_title.status_code == 403
    assert unknown_title.json()["error"]["code"] == "TITLE_NOT_UNLOCKED"


def test_manual_catalog_supports_detail_search_volume_and_cursor(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13920000003")
    headers = bearer(auth)

    first_page = seeded_client.get("/v1/manuals?limit=7", headers=headers)
    assert first_page.status_code == 200, first_page.text
    body = first_page.json()
    assert body["total"] == 50
    assert [item["page_no"] for item in body["items"]] == list(range(1, 8))
    assert all(item["progress_state"] == "UNSEEN" for item in body["items"])
    assert all(item["progress_label"] == "未闻" for item in body["items"])

    next_page = seeded_client.get(
        "/v1/manuals",
        headers=headers,
        params={"limit": 7, "cursor": body["next_cursor"]},
    )
    assert [item["page_no"] for item in next_page.json()["items"]] == list(
        range(8, 15)
    )

    volume = seeded_client.get("/v1/manuals?volume=10", headers=headers)
    assert volume.status_code == 200
    assert [item["page_no"] for item in volume.json()["items"]] == list(
        range(46, 51)
    )

    searched = seeded_client.get(
        "/v1/manuals", headers=headers, params={"q": "幻觉"}
    )
    assert searched.status_code == 200
    assert [item["page_no"] for item in searched.json()["items"]] == [45]

    manual_id = body["items"][0]["id"]
    detail = seeded_client.get(f"/v1/manuals/{manual_id}", headers=headers)
    assert detail.status_code == 200
    assert detail.json()["title"] == "会动未必会思"
    assert detail.json()["content_status"] == "OUTLINE"
    assert [item["label"] for item in detail.json()["progress_requirements"]] == [
        "偶得",
        "习得",
        "悟得",
        "传习",
    ]
    assert detail.json()["evidence"] == []

    invalid_cursor = seeded_client.get(
        "/v1/manuals?cursor=broken", headers=headers
    )
    assert invalid_cursor.status_code == 400
    assert invalid_cursor.json()["error"]["code"] == "INVALID_CURSOR"

    not_yet_obtained = seeded_client.get(
        "/v1/manuals?state=LEARNED", headers=headers
    )
    assert not_yet_obtained.status_code == 200
    assert not_yet_obtained.json() == {
        "total": 0,
        "items": [],
        "next_cursor": None,
    }


def test_manual_favorites_are_idempotent_and_private(
    seeded_client: TestClient,
) -> None:
    owner = register(seeded_client, "13920000004")
    other = register(seeded_client, "13920000005")
    owner_headers = bearer(owner)
    other_headers = bearer(other)
    manual_id = seeded_client.get("/v1/manuals?limit=1", headers=owner_headers).json()[
        "items"
    ][0]["id"]

    first = seeded_client.put(
        f"/v1/manuals/{manual_id}/favorite", headers=owner_headers
    )
    second = seeded_client.put(
        f"/v1/manuals/{manual_id}/favorite", headers=owner_headers
    )
    assert first.status_code == second.status_code == 200
    assert first.json()["created_at"] == second.json()["created_at"]

    favorites = seeded_client.get(
        "/v1/manuals?favorites_only=true", headers=owner_headers
    )
    assert favorites.json()["total"] == 1
    assert favorites.json()["items"][0]["is_favorite"] is True
    assert seeded_client.get(
        "/v1/manuals?favorites_only=true", headers=other_headers
    ).json()["total"] == 0

    removed = seeded_client.delete(
        f"/v1/manuals/{manual_id}/favorite", headers=owner_headers
    )
    assert removed.status_code == 204
    assert seeded_client.get(
        "/v1/manuals?favorites_only=true", headers=owner_headers
    ).json()["total"] == 0


def test_luggage_skeleton_has_explicit_empty_states_and_etag(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13920000006")
    headers = bearer(auth)

    response = seeded_client.get("/v1/me/luggage", headers=headers)
    assert response.status_code == 200, response.text
    body = response.json()
    assert body["data"]["manuals"]["total"] == 50
    assert body["data"]["manuals"]["obtained"] == 0
    assert body["data"]["manuals"]["counts_by_state"]["UNSEEN"] == 50
    assert body["data"]["manuals"]["empty_reason"] == "NO_OBTAINED_MANUALS"
    assert body["data"]["mistakes"]["empty_reason"] == "NO_MISTAKES"
    assert body["data"]["creations"]["empty_reason"] == "NO_CREATIONS"
    assert body["data"]["stats"]["week"]["practice_count"] == 0
    assert body["meta"]["etag"] == response.headers["etag"]

    unchanged = seeded_client.get(
        "/v1/me/luggage",
        headers={**headers, "If-None-Match": response.headers["etag"]},
    )
    assert unchanged.status_code == 304
    assert unchanged.content == b""

    capabilities = seeded_client.get("/v1/meta/capabilities")
    assert capabilities.status_code == 200
    assert capabilities.json()["manual_catalog"] is True
    assert capabilities.json()["luggage_snapshot"] == "LIVE_MEDIA_REVIEW"
    assert capabilities.json()["learning_progress"] is True
    assert capabilities.json()["mistakes"] is True
    assert capabilities.json()["creations"] is True
    assert capabilities.json()["media_uploads"] is True
