from __future__ import annotations

from fastapi.testclient import TestClient


TERMS_VERSION = "2026-08"
PRIVACY_VERSION = "2026-08"
OTP = "123456"


def register(
    client: TestClient,
    phone: str,
    *,
    age_band: str = "AGE_14_TO_17",
) -> dict:
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
            "age_band": age_band,
            "terms_version": TERMS_VERSION,
            "privacy_version": PRIVACY_VERSION,
            "device_name": "pytest-primary",
        },
    )
    assert response.status_code == 201, response.text
    return response.json()


def bearer(auth: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {auth['tokens']['access_token']}"}


def test_preferences_have_defaults_and_accept_partial_updates(client: TestClient) -> None:
    auth = register(client, "13910000001")

    initial = client.get("/v1/settings/preferences", headers=bearer(auth))
    assert initial.status_code == 200, initial.text
    assert initial.json()["large_text"] is False
    assert initial.json()["subtitles_enabled"] is True
    assert initial.json()["music_volume"] == 0.65

    updated = client.patch(
        "/v1/settings/preferences",
        headers=bearer(auth),
        json={"large_text": True, "music_volume": 0.25},
    )
    assert updated.status_code == 200, updated.text
    assert updated.json()["large_text"] is True
    assert updated.json()["music_volume"] == 0.25
    assert updated.json()["message_enabled"] is True

    invalid = client.patch(
        "/v1/settings/preferences",
        headers=bearer(auth),
        json={"effect_volume": 2},
    )
    assert invalid.status_code == 422


def test_guardian_controls_are_available_only_for_minor_accounts(client: TestClient) -> None:
    minor = register(client, "13910000002")
    controls = client.patch(
        "/v1/settings/guardian-controls",
        headers=bearer(minor),
        json={
            "daily_limit_minutes": 90,
            "creation_allowed": False,
            "content_level": "TEEN",
        },
    )
    assert controls.status_code == 200, controls.text
    assert controls.json()["daily_limit_minutes"] == 90
    assert controls.json()["creation_allowed"] is False

    adult = register(client, "13910000003", age_band="ADULT")
    unavailable = client.get(
        "/v1/settings/guardian-controls", headers=bearer(adult)
    )
    assert unavailable.status_code == 409
    assert unavailable.json()["error"]["code"] == "GUARDIAN_CONTROLS_NOT_APPLICABLE"


def test_feedback_blacklist_and_removal(client: TestClient) -> None:
    owner = register(client, "13910000004")
    blocked = register(client, "13910000005")

    feedback = client.post(
        "/v1/support/feedback",
        headers=bearer(owner),
        json={"category": "BUG", "message": "消息提醒开关无法正常保存，请协助检查。"},
    )
    assert feedback.status_code == 201, feedback.text
    assert feedback.json()["status"] == "OPEN"

    blocked_user_id = blocked["user"]["id"]
    added = client.post(
        "/v1/settings/blacklist",
        headers=bearer(owner),
        json={"blocked_user_id": blocked_user_id},
    )
    assert added.status_code == 201, added.text
    assert added.json()["user_id"] == blocked_user_id

    entries = client.get("/v1/settings/blacklist", headers=bearer(owner))
    assert entries.status_code == 200
    assert [entry["user_id"] for entry in entries.json()] == [blocked_user_id]

    removed = client.delete(
        f"/v1/settings/blacklist/{blocked_user_id}", headers=bearer(owner)
    )
    assert removed.status_code == 204
    assert client.get(
        "/v1/settings/blacklist", headers=bearer(owner)
    ).json() == []


def test_sessions_export_and_data_rights_requests(client: TestClient) -> None:
    auth = register(client, "13910000006")
    second_login = client.post(
        "/v1/auth/login/password",
        json={
            "phone": "13910000006",
            "password": "StrongPass!8",
            "device_name": "pytest-secondary",
        },
    )
    assert second_login.status_code == 200, second_login.text

    sessions = client.get("/v1/account/sessions", headers=bearer(auth))
    assert sessions.status_code == 200, sessions.text
    assert {item["device_name"] for item in sessions.json()} == {
        "pytest-primary",
        "pytest-secondary",
    }

    secondary = next(
        item for item in sessions.json() if item["device_name"] == "pytest-secondary"
    )
    revoked = client.delete(
        f"/v1/account/sessions/{secondary['id']}", headers=bearer(auth)
    )
    assert revoked.status_code == 204
    assert len(client.get("/v1/account/sessions", headers=bearer(auth)).json()) == 1

    exported = client.get("/v1/account/export", headers=bearer(auth))
    assert exported.status_code == 200, exported.text
    assert exported.json()["user"]["phone_masked"] == "139****0006"
    assert "refresh_token" not in exported.text
    assert len(exported.json()["consents"]) == 2

    request_payload = {
        "request_type": "ACCOUNT_DELETION",
        "reason": "不再使用该账号，希望删除账号数据。",
    }
    first = client.post(
        "/v1/account/data-rights-requests",
        headers=bearer(auth),
        json=request_payload,
    )
    duplicate = client.post(
        "/v1/account/data-rights-requests",
        headers=bearer(auth),
        json=request_payload,
    )
    assert first.status_code == duplicate.status_code == 202
    assert first.json()["id"] == duplicate.json()["id"]
    assert first.json()["status"] == "PENDING"

    requests = client.get(
        "/v1/account/data-rights-requests", headers=bearer(auth)
    )
    assert requests.status_code == 200
    assert len(requests.json()) == 1
