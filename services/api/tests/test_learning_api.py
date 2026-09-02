from __future__ import annotations

from collections.abc import Iterator
from datetime import datetime, timezone

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.domains.catalog.seed import seed_catalog_data, stable_id


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
            "device_name": "pytest-learning",
        },
    )
    assert response.status_code == 201, response.text
    return response.json()


def bearer(auth: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {auth['tokens']['access_token']}"}


def correct_payload(version_id: str) -> dict:
    return {
        "trial_version_id": version_id,
        "prediction_payload": {"key_feature": "能从样本改进"},
        "answer_payload": {"choice": "LEARNS_FROM_DATA"},
        "explanation": "机器学习系统能够根据样本经验改进后续判断。",
        "client_request_id": "android-learning-0001",
    }


def test_trial_is_server_versioned_and_correct_attempt_updates_all_projections(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000001")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers)
    assert trial.status_code == 200, trial.text
    trial_body = trial.json()
    assert trial_body["knowledge_point_code"] == "AI_CAPABILITY_BOUNDARY"
    assert trial_body["current_version"]["version"] == 1
    assert "grader_config" not in trial.text
    assert "expected_answer" not in trial.text

    attempt_headers = {**headers, "Idempotency-Key": "attempt-correct-0001"}
    attempt = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers=attempt_headers,
        json=correct_payload(trial_body["current_version"]["id"]),
    )
    assert attempt.status_code == 201, attempt.text
    result = attempt.json()
    assert result["passed"] is True
    assert result["score"] == 100
    assert [change["current_state"] for change in result["progress_changes"]] == [
        "DISCOVERED",
        "LEARNED",
    ]
    assert result["evidence_awards"][0]["category"] == "WISDOM"

    stats = seeded_client.get("/v1/me/learning-stats", headers=headers)
    assert stats.status_code == 200
    assert stats.json()["week"]["practice_count"] == 1
    assert stats.json()["lifetime_practice_count"] == 1
    assert stats.json()["lifetime_practice_days"] == 1
    assert stats.json()["distinct_trials_passed"] == 1
    assert stats.json()["evidence"]["wisdom"]["count"] == 1
    assert stats.json()["evidence"]["wisdom"]["display_summary"] == (
        "积累1条悟性证据"
    )
    badges = seeded_client.get("/v1/profile/badges", headers=headers)
    assert badges.status_code == 200
    assert [item["code"] for item in badges.json()] == ["FIRST_TRIAL"]

    learned = seeded_client.get("/v1/manuals?state=LEARNED", headers=headers)
    assert learned.status_code == 200
    assert learned.json()["total"] == 1
    assert learned.json()["items"][0]["progress_label"] == "习得"
    unseen = seeded_client.get("/v1/manuals?state=UNSEEN", headers=headers)
    assert unseen.json()["total"] == 49

    manual_id = learned.json()["items"][0]["id"]
    detail = seeded_client.get(f"/v1/manuals/{manual_id}", headers=headers)
    assert detail.json()["progress_state"] == "LEARNED"
    assert len(detail.json()["evidence"]) == 1
    evidence = seeded_client.get(
        f"/v1/manuals/{manual_id}/evidence", headers=headers
    )
    assert evidence.status_code == 200
    assert evidence.json()[0]["evidence_type"] == "TRIAL_EXPLANATION_ACCEPTED"
    all_evidence = seeded_client.get(
        "/v1/me/learning-evidence?week_only=true&category=WISDOM",
        headers=headers,
    )
    assert all_evidence.status_code == 200
    assert all_evidence.json()["total"] == 1
    assert all_evidence.json()["items"][0]["manual_title"] is not None
    history = seeded_client.get(
        f"/v1/manuals/{manual_id}/learning-history", headers=headers
    )
    assert history.status_code == 200
    assert history.json()["current_state"] == "LEARNED"
    assert [item["trigger_event"] for item in history.json()["transitions"]] == [
        "PREDICTION_COMPLETED",
        "TRIAL_PASSED",
    ]
    assert history.json()["transitions"][1]["evidence_summary"] is not None

    luggage = seeded_client.get("/v1/me/luggage", headers=headers)
    assert luggage.status_code == 200, luggage.text
    assert luggage.json()["data"]["stats"]["week"]["practice_count"] == 1
    assert luggage.json()["data"]["stats"]["lifetime_practice_days"] == 1
    assert luggage.json()["data"]["manuals"]["obtained"] == 1
    assert luggage.json()["data"]["manuals"]["items"][0]["style_no"] == 1
    assert luggage.json()["data"]["manuals"]["counts_by_state"]["LEARNED"] == 1
    assert luggage.json()["data"]["manuals"]["empty_reason"] is None


def test_attempt_idempotency_prevents_duplicate_stats_and_evidence(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000002")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    payload = correct_payload(trial["current_version"]["id"])
    attempt_headers = {**headers, "Idempotency-Key": "attempt-replay-0001"}

    first = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers=attempt_headers,
        json=payload,
    )
    replay = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers=attempt_headers,
        json=payload,
    )
    assert first.status_code == replay.status_code == 201
    assert first.json() == replay.json()

    stats = seeded_client.get("/v1/me/learning-stats", headers=headers).json()
    assert stats["lifetime_practice_count"] == 1
    assert stats["lifetime_practice_days"] == 1
    assert stats["evidence"]["wisdom"]["count"] == 1

    changed = dict(payload)
    changed["answer_payload"] = {"choice": "FOLLOWS_FIXED_RULES"}
    conflict = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers=attempt_headers,
        json=changed,
    )
    assert conflict.status_code == 409
    assert conflict.json()["error"]["code"] == "IDEMPOTENCY_KEY_REUSED"


def test_lifetime_practice_days_use_shanghai_calendar_days(
    seeded_client: TestClient,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    auth = register(seeded_client, "13930000012")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    payload = correct_payload(trial["current_version"]["id"])
    current = [datetime(2026, 9, 1, 15, 30, tzinfo=timezone.utc)]
    monkeypatch.setattr("app.domains.learning.service.utcnow", lambda: current[0])

    first = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "practice-day-0001"},
        json=payload,
    )
    assert first.status_code == 201, first.text

    # 16:00 UTC is midnight in Shanghai, so this is the next learner-facing day.
    current[0] = datetime(2026, 9, 1, 16, 30, tzinfo=timezone.utc)
    second = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "practice-day-0002"},
        json=payload,
    )
    assert second.status_code == 201, second.text

    stats = seeded_client.get("/v1/me/learning-stats", headers=headers).json()
    assert stats["lifetime_practice_count"] == 2
    assert stats["lifetime_practice_days"] == 2


def test_learning_evidence_rejects_invalid_cursor(seeded_client: TestClient) -> None:
    auth = register(seeded_client, "13930000013")
    response = seeded_client.get(
        "/v1/me/learning-evidence?cursor=not-a-cursor",
        headers=bearer(auth),
    )
    assert response.status_code == 400
    assert response.json()["error"]["code"] == "INVALID_CURSOR"


def test_failed_attempt_counts_as_practice_but_does_not_forge_learning(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000003")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    payload = correct_payload(trial["current_version"]["id"])
    payload["answer_payload"] = {"choice": "MOVES_AUTOMATICALLY"}

    failed = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "attempt-failed-0001"},
        json=payload,
    )
    assert failed.status_code == 201, failed.text
    assert failed.json()["passed"] is False
    assert failed.json()["feedback_codes"] == [
        "CONFUSED_AUTOMATION_WITH_LEARNING"
    ]
    assert [item["current_state"] for item in failed.json()["progress_changes"]] == [
        "DISCOVERED"
    ]
    assert failed.json()["evidence_awards"] == []
    assert failed.json()["mistake"]["status"] == "TO_REVIEW"
    assert failed.json()["mistake"]["reason_code"] == (
        "CONFUSED_AUTOMATION_WITH_LEARNING"
    )

    stats = seeded_client.get("/v1/me/learning-stats", headers=headers).json()
    assert stats["lifetime_practice_count"] == 1
    assert stats["distinct_trials_passed"] == 0
    assert stats["evidence"]["wisdom"]["count"] == 0
    discovered = seeded_client.get(
        "/v1/manuals?state=DISCOVERED", headers=headers
    ).json()
    assert discovered["total"] == 1


def test_attempt_requires_idempotency_key_and_current_version(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000004")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    payload = correct_payload(trial["current_version"]["id"])

    missing_key = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts", headers=headers, json=payload
    )
    assert missing_key.status_code == 422

    payload["trial_version_id"] = "00000000-0000-0000-0000-000000000001"
    stale = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "attempt-stale-0001"},
        json=payload,
    )
    assert stale.status_code == 404
    assert stale.json()["error"]["code"] == "TRIAL_NOT_FOUND"


def test_empty_prediction_cannot_advance_progress(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000005")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()
    payload = correct_payload(trial["current_version"]["id"])
    payload["prediction_payload"] = {}

    response = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "empty-prediction-0001"},
        json=payload,
    )
    assert response.status_code == 201
    assert response.json()["passed"] is False
    assert response.json()["feedback_codes"] == ["PREDICTION_REQUIRED"]
    assert response.json()["progress_changes"] == []
    assert seeded_client.get(
        "/v1/manuals?state=UNSEEN", headers=headers
    ).json()["total"] == 50
