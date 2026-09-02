from __future__ import annotations

from collections.abc import Iterator
from datetime import timedelta
import uuid

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.core.security import utcnow
from app.domains.catalog.seed import seed_catalog_data, stable_id
from app.domains.mistakes.models import MistakeItem


OTP = "123456"
TERMS_VERSION = "2026-08"
PRIVACY_VERSION = "2026-08"
TRIAL_ID = stable_id("trial", "manual-01-ai-boundary")


@pytest.fixture
def seeded_client(app: FastAPI) -> Iterator[TestClient]:
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    with TestClient(app) as client:
        yield client


def register(client: TestClient, phone: str) -> dict:
    assert client.post(
        "/v1/auth/verification-codes",
        json={"phone": phone, "purpose": "REGISTER"},
    ).status_code == 202
    response = client.post(
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
    assert response.status_code == 201, response.text
    return response.json()


def bearer(auth: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {auth['tokens']['access_token']}"}


def answer_payload(version_id: str, *, correct: bool) -> dict:
    return {
        "trial_version_id": version_id,
        "prediction_payload": {"key_feature": "是否会从样本改进"},
        "answer_payload": {
            "choice": "LEARNS_FROM_DATA" if correct else "MOVES_AUTOMATICALLY"
        },
        "explanation": "系统是否能够从样本经验中改进，是判断的关键依据。",
        "client_request_id": "android-mistake-0001",
    }


def submit(
    client: TestClient,
    headers: dict[str, str],
    payload: dict,
    key: str,
):
    return client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": key},
        json=payload,
    )


def test_failure_creates_private_mistake_and_retry_enters_practicing(
    seeded_client: TestClient,
) -> None:
    owner = register(seeded_client, "13940000001")
    other = register(seeded_client, "13940000002")
    headers = bearer(owner)
    other_headers = bearer(other)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    version_id = trial["current_version"]["id"]

    failed = submit(
        seeded_client,
        headers,
        answer_payload(version_id, correct=False),
        "mistake-failed-0001",
    )
    assert failed.status_code == 201, failed.text
    mistake_id = failed.json()["mistake"]["id"]

    listed = seeded_client.get("/v1/mistakes", headers=headers)
    assert listed.status_code == 200
    assert listed.json()["total"] == 1
    assert listed.json()["items"][0]["status"] == "TO_REVIEW"
    assert listed.json()["items"][0]["manual_title"] == "会动未必会思"
    assert seeded_client.get("/v1/mistakes", headers=other_headers).json()["total"] == 0
    invalid_cursor = seeded_client.get(
        "/v1/mistakes?cursor=broken", headers=headers
    )
    assert invalid_cursor.status_code == 400
    assert invalid_cursor.json()["error"]["code"] == "INVALID_CURSOR"
    assert seeded_client.get(
        f"/v1/mistakes/{mistake_id}", headers=other_headers
    ).status_code == 404

    detail = seeded_client.get(f"/v1/mistakes/{mistake_id}", headers=headers)
    assert detail.status_code == 200
    assert detail.json()["original_answer_payload"] == {
        "choice": "MOVES_AUTOMATICALLY"
    }
    assert detail.json()["remediation_records"] == []

    retry = seeded_client.post(
        f"/v1/mistakes/{mistake_id}/retry-sessions", headers=headers
    )
    assert retry.status_code == 201, retry.text
    corrected_payload = answer_payload(version_id, correct=True)
    corrected_payload["remediation_context_id"] = retry.json()["id"]
    corrected = submit(
        seeded_client,
        headers,
        corrected_payload,
        "mistake-retry-0001",
    )
    assert corrected.status_code == 201, corrected.text
    assert corrected.json()["passed"] is True
    assert corrected.json()["mistake"] is None

    practicing = seeded_client.get(
        f"/v1/mistakes/{mistake_id}", headers=headers
    ).json()
    assert practicing["status"] == "PRACTICING"
    assert practicing["successful_retries"] == 1
    assert practicing["next_review_at"] is not None
    assert len(practicing["remediation_records"]) == 1

    reused = submit(
        seeded_client,
        headers,
        corrected_payload,
        "mistake-retry-reused-context",
    )
    assert reused.status_code == 409
    assert reused.json()["error"]["code"] == "REMEDIATION_CONTEXT_INVALID"

    luggage = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["mistakes"]["pending_count"] == 1
    assert luggage["data"]["mistakes"]["items"][0]["status"] == "PRACTICING"
    assert luggage["data"]["mistakes"]["empty_reason"] is None


def test_second_due_retry_consolidates_mistake(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13940000003")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    version_id = trial["current_version"]["id"]
    failed = submit(
        seeded_client,
        headers,
        answer_payload(version_id, correct=False),
        "consolidate-failed-0001",
    ).json()
    mistake_id = failed["mistake"]["id"]

    first_context = seeded_client.post(
        f"/v1/mistakes/{mistake_id}/retry-sessions", headers=headers
    ).json()
    first_retry = answer_payload(version_id, correct=True)
    first_retry["remediation_context_id"] = first_context["id"]
    assert submit(
        seeded_client, headers, first_retry, "consolidate-retry-0001"
    ).status_code == 201

    with seeded_client.app.state.session_factory() as db:
        mistake = db.get(MistakeItem, uuid.UUID(mistake_id))
        assert mistake is not None
        mistake.next_review_at = utcnow() - timedelta(minutes=1)
        db.commit()

    second_context = seeded_client.post(
        f"/v1/mistakes/{mistake_id}/retry-sessions", headers=headers
    ).json()
    second_retry = answer_payload(version_id, correct=True)
    second_retry["remediation_context_id"] = second_context["id"]
    consolidated = submit(
        seeded_client,
        headers,
        second_retry,
        "consolidate-retry-0002",
    )
    assert consolidated.status_code == 201, consolidated.text

    detail = seeded_client.get(f"/v1/mistakes/{mistake_id}", headers=headers).json()
    assert detail["status"] == "CONSOLIDATED"
    assert detail["successful_retries"] == 2
    assert detail["consolidated_at"] is not None
    assert len(detail["remediation_records"]) == 2
    blocked_retry = seeded_client.post(
        f"/v1/mistakes/{mistake_id}/retry-sessions", headers=headers
    )
    assert blocked_retry.status_code == 409
    assert blocked_retry.json()["error"]["code"] == "MISTAKE_ALREADY_CONSOLIDATED"

    luggage = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["mistakes"]["pending_count"] == 0
    assert luggage["data"]["mistakes"]["items"] == []
    assert luggage["data"]["mistakes"]["empty_reason"] == "NO_MISTAKES"
    stats = seeded_client.get("/v1/me/learning-stats", headers=headers).json()
    assert stats["lifetime_practice_count"] == 3
    assert stats["distinct_trials_passed"] == 1
    assert stats["evidence"]["wisdom"]["count"] == 1
