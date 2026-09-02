from __future__ import annotations

from fastapi.testclient import TestClient


TERMS_VERSION = "2026-08"
PRIVACY_VERSION = "2026-08"
OTP = "123456"


def request_code(client: TestClient, phone: str, purpose: str) -> None:
    response = client.post(
        "/v1/auth/verification-codes",
        json={"phone": phone, "purpose": purpose},
    )
    assert response.status_code == 202, response.text


def register(
    client: TestClient,
    phone: str,
    *,
    password: str = "StrongPass!8",
    age_band: str = "AGE_14_TO_17",
    guardian_consent_token: str | None = None,
):
    request_code(client, phone, "REGISTER")
    payload = {
        "phone": phone,
        "verification_code": OTP,
        "password": password,
        "age_band": age_band,
        "terms_version": TERMS_VERSION,
        "privacy_version": PRIVACY_VERSION,
        "device_name": "pytest",
    }
    if guardian_consent_token:
        payload["guardian_consent_token"] = guardian_consent_token
    return client.post("/v1/auth/register", json=payload)


def bearer(access_token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {access_token}"}


def test_health_and_security_headers(client: TestClient) -> None:
    response = client.get("/healthz", headers={"X-Request-ID": "test-request-123"})

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
    assert response.headers["x-request-id"] == "test-request-123"
    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["cache-control"] == "no-store"
    assert response.headers["referrer-policy"] == "no-referrer"


def test_register_login_and_current_user(client: TestClient) -> None:
    response = register(client, "138 0013-8000")

    assert response.status_code == 201, response.text
    auth = response.json()
    assert auth["user"]["phone_masked"] == "138****8000"
    assert auth["user"]["guardian_status"] == "NOT_REQUIRED"
    assert auth["tokens"]["token_type"] == "bearer"
    assert auth["next_action"] == "SHOW_GUIDE"

    current = client.get(
        "/v1/auth/me", headers=bearer(auth["tokens"]["access_token"])
    )
    assert current.status_code == 200
    assert current.json()["id"] == auth["user"]["id"]

    login = client.post(
        "/v1/auth/login/password",
        json={"phone": "+8613800138000", "password": "StrongPass!8"},
    )
    assert login.status_code == 200, login.text
    assert login.json()["user"]["id"] == auth["user"]["id"]
    assert login.json()["next_action"] == "ENTER_APP"


def test_login_does_not_disclose_account_existence(client: TestClient) -> None:
    registered = register(client, "13900139001")
    assert registered.status_code == 201

    wrong_password = client.post(
        "/v1/auth/login/password",
        json={"phone": "13900139001", "password": "WrongPass!8"},
    )
    nonexistent = client.post(
        "/v1/auth/login/password",
        json={"phone": "13900139002", "password": "WrongPass!8"},
    )

    assert wrong_password.status_code == nonexistent.status_code == 401
    assert wrong_password.json()["error"]["code"] == "INVALID_CREDENTIALS"
    assert nonexistent.json()["error"]["code"] == "INVALID_CREDENTIALS"
    assert wrong_password.json()["error"]["message"] == nonexistent.json()["error"]["message"]


def test_verification_code_is_purpose_bound_and_single_use(client: TestClient) -> None:
    phone = "13700137001"
    request_code(client, phone, "RESET_PASSWORD")

    wrong_purpose = client.post(
        "/v1/auth/register",
        json={
            "phone": phone,
            "verification_code": OTP,
            "password": "StrongPass!8",
            "age_band": "AGE_14_TO_17",
            "terms_version": TERMS_VERSION,
            "privacy_version": PRIVACY_VERSION,
        },
    )
    assert wrong_purpose.status_code == 400
    assert wrong_purpose.json()["error"]["code"] == "VERIFICATION_CODE_INVALID_OR_EXPIRED"

    request_code(client, phone, "REGISTER")
    payload = {
        "phone": phone,
        "verification_code": OTP,
        "password": "StrongPass!8",
        "age_band": "AGE_14_TO_17",
        "terms_version": TERMS_VERSION,
        "privacy_version": PRIVACY_VERSION,
    }
    first = client.post("/v1/auth/register", json=payload)
    second = client.post("/v1/auth/register", json=payload)
    assert first.status_code == 201
    assert second.status_code == 400
    assert second.json()["error"]["code"] == "VERIFICATION_CODE_INVALID_OR_EXPIRED"


def test_refresh_rotation_detects_replay_and_revokes_family(client: TestClient) -> None:
    auth = register(client, "13600136001").json()
    old_refresh = auth["tokens"]["refresh_token"]

    rotated = client.post(
        "/v1/auth/token/refresh", json={"refresh_token": old_refresh}
    )
    assert rotated.status_code == 200, rotated.text
    new_tokens = rotated.json()
    assert new_tokens["refresh_token"] != old_refresh

    replay = client.post(
        "/v1/auth/token/refresh", json={"refresh_token": old_refresh}
    )
    assert replay.status_code == 401
    assert replay.json()["error"]["code"] == "REFRESH_TOKEN_REUSED"

    invalidated = client.get(
        "/v1/auth/me", headers=bearer(new_tokens["access_token"])
    )
    assert invalidated.status_code == 401
    assert invalidated.json()["error"]["code"] == "INVALID_ACCESS_TOKEN"


def test_password_reset_revokes_sessions_and_changes_password(client: TestClient) -> None:
    phone = "13500135001"
    auth = register(client, phone, password="OldPass!88").json()
    request_code(client, phone, "RESET_PASSWORD")

    reset = client.post(
        "/v1/auth/password/reset",
        json={
            "phone": phone,
            "verification_code": OTP,
            "new_password": "NewPass!99",
        },
    )
    assert reset.status_code == 200, reset.text
    assert reset.json()["status"] == "PASSWORD_RESET_SUCCESS"
    assert client.get(
        "/v1/auth/me", headers=bearer(auth["tokens"]["access_token"])
    ).status_code == 401
    assert client.post(
        "/v1/auth/token/refresh",
        json={"refresh_token": auth["tokens"]["refresh_token"]},
    ).status_code == 401
    assert client.post(
        "/v1/auth/login/password",
        json={"phone": phone, "password": "OldPass!88"},
    ).status_code == 401
    assert client.post(
        "/v1/auth/login/password",
        json={"phone": phone, "password": "NewPass!99"},
    ).status_code == 200


def test_under_14_registration_requires_verified_guardian(client: TestClient) -> None:
    child_phone = "13400134001"
    guardian_phone = "13300133001"
    request_code(client, child_phone, "REGISTER")

    child_payload = {
        "phone": child_phone,
        "verification_code": OTP,
        "password": "StrongPass!8",
        "age_band": "UNDER_14",
        "terms_version": TERMS_VERSION,
        "privacy_version": PRIVACY_VERSION,
    }
    blocked = client.post("/v1/auth/register", json=child_payload)
    assert blocked.status_code == 403
    assert blocked.json()["error"]["code"] == "GUARDIAN_CONSENT_REQUIRED"

    request_code(client, guardian_phone, "GUARDIAN_CONSENT")
    consent = client.post(
        "/v1/auth/guardian-consents/verify",
        json={
            "child_phone": child_phone,
            "guardian_phone": guardian_phone,
            "verification_code": OTP,
            "terms_version": TERMS_VERSION,
            "privacy_version": PRIVACY_VERSION,
        },
    )
    assert consent.status_code == 200, consent.text

    child_payload["guardian_consent_token"] = consent.json()["guardian_consent_token"]
    created = client.post("/v1/auth/register", json=child_payload)
    assert created.status_code == 201, created.text
    assert created.json()["user"]["guardian_status"] == "VERIFIED"


def test_registration_rejects_outdated_consent_versions(client: TestClient) -> None:
    phone = "13300133009"
    request_code(client, phone, "REGISTER")

    response = client.post(
        "/v1/auth/register",
        json={
            "phone": phone,
            "verification_code": OTP,
            "password": "StrongPass!8",
            "age_band": "AGE_14_TO_17",
            "terms_version": "old-version",
            "privacy_version": PRIVACY_VERSION,
        },
    )
    assert response.status_code == 409
    assert response.json()["error"]["code"] == "CONSENT_VERSION_OUTDATED"


def test_rate_limit_returns_retry_after(client: TestClient) -> None:
    payload = {"phone": "13200132001", "purpose": "REGISTER"}
    first = client.post("/v1/auth/verification-codes", json=payload)
    second = client.post("/v1/auth/verification-codes", json=payload)

    assert first.status_code == 202
    assert second.status_code == 429
    assert int(second.headers["retry-after"]) >= 1
    assert second.json()["error"]["code"] == "RATE_LIMITED"


def test_unknown_fields_and_large_requests_are_rejected(client: TestClient) -> None:
    extra = client.post(
        "/v1/auth/login/password",
        json={
            "phone": "13100131001",
            "password": "StrongPass!8",
            "is_admin": True,
        },
    )
    assert extra.status_code == 422
    assert extra.json()["error"]["code"] == "VALIDATION_ERROR"

    oversized = client.post(
        "/v1/auth/login/password",
        content='{"padding":"' + ("x" * 2048) + '"}',
        headers={"Content-Type": "application/json"},
    )
    assert oversized.status_code == 413
    assert oversized.json()["error"]["code"] == "REQUEST_TOO_LARGE"


def test_password_whitespace_is_not_silently_trimmed(client: TestClient) -> None:
    phone = "13000130001"
    created = register(client, phone, password=" Passphrase!9 ")
    assert created.status_code == 201, created.text

    exact = client.post(
        "/v1/auth/login/password",
        json={"phone": phone, "password": " Passphrase!9 "},
    )
    trimmed = client.post(
        "/v1/auth/login/password",
        json={"phone": phone, "password": "Passphrase!9"},
    )
    assert exact.status_code == 200
    assert trimmed.status_code == 401
