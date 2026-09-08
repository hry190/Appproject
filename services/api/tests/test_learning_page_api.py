from __future__ import annotations

import uuid
from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.domains.catalog.seed import seed_catalog_data, stable_id
from app.domains.catalog.content_schema import (
    TRIAL_DEFINITIONS,
    build_trial_manifest,
    validate_trial_manifest,
)
from app.domains.learning.contracts import ManualProgressState
from app.domains.learning.models import ManualProgress
from test_learning_api import TRIAL_ID, bearer, correct_payload, register


@pytest.fixture
def seeded_client(app: FastAPI) -> Iterator[TestClient]:
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    with TestClient(app) as client:
        yield client


def test_learning_overview_returns_circle_and_next_mountain(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000101")
    headers = bearer(auth)

    response = seeded_client.get("/v1/learning/overview", headers=headers)

    assert response.status_code == 200, response.text
    body = response.json()
    assert len(body["books"]) == 50
    assert body["books"][0]["state"] == "UNSEEN"
    assert body["recommended_lesson_id"] == body["books"][0]["manual_page_id"]
    assert body["back_mountain"]["lesson_id"] == body["books"][0]["manual_page_id"]


def test_learning_overview_moves_recommendation_after_trial(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000102")
    headers = bearer(auth)
    trial = seeded_client.get(f"/v1/trials/{TRIAL_ID}", headers=headers).json()

    attempt = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "overview-attempt-0001"},
        json=correct_payload(trial["current_version"]["id"]),
    )
    assert attempt.status_code == 201, attempt.text

    overview = seeded_client.get("/v1/learning/overview", headers=headers)
    assert overview.status_code == 200
    body = overview.json()
    assert body["books"][0]["state"] == "LEARNED"
    assert body["recommended_lesson_id"] == body["books"][1]["manual_page_id"]


def test_learning_route_and_lesson_compatibility_alias(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000103")
    headers = bearer(auth)

    route = seeded_client.get(
        "/v1/learning/route",
        params={"q": "AI 为什么会说错话"},
        headers=headers,
    )
    assert route.status_code == 200, route.text
    matches = route.json()["matches"]
    assert matches
    assert matches[0]["match_source"] == "catalog_keyword"
    assert any("幻觉" in item["recommended_reason"] or item["title"] == "会说不等于知道" for item in matches)

    gated = seeded_client.get(
        "/v1/learning/route",
        params={"q": "感知 推理 行动"},
        headers=headers,
    )
    assert gated.status_code == 200
    assert gated.json()["matches"][0]["available"] is False
    assert gated.json()["matches"][0]["prerequisites"]

    lesson_id = stable_id("manual_page", "1")
    alias = seeded_client.get(f"/v1/lessons/{lesson_id}", headers=headers)
    manual = seeded_client.get(f"/v1/manuals/{lesson_id}", headers=headers)
    assert alias.status_code == manual.status_code == 200
    assert alias.json() == manual.json()
    assert manual.json()["trial_id"] is not None

    page_two = seeded_client.get(
        f"/v1/manuals/{stable_id('manual_page', '2')}", headers=headers
    )
    assert page_two.status_code == 200
    assert page_two.json()["trial_id"] is None


def test_learning_route_rejects_blank_question(seeded_client: TestClient) -> None:
    auth = register(seeded_client, "13930000104")
    response = seeded_client.get(
        "/v1/learning/route",
        params={"q": "   "},
        headers=bearer(auth),
    )
    assert response.status_code == 400
    assert response.json()["error"]["code"] == "QUERY_REQUIRED"


def test_lesson_read_event_is_idempotent_and_does_not_award_state(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000105")
    headers = bearer(auth)
    lesson_id = stable_id("manual_page", "1")
    request_headers = {**headers, "Idempotency-Key": "read-event-0001"}

    first = seeded_client.post(
        f"/v1/lessons/{lesson_id}/read-events",
        headers=request_headers,
    )
    replay = seeded_client.post(
        f"/v1/lessons/{lesson_id}/read-events",
        headers=request_headers,
    )

    assert first.status_code == replay.status_code == 201
    assert first.json() == replay.json()
    assert first.json()["changed"] is False
    overview = seeded_client.get("/v1/learning/overview", headers=headers).json()
    assert overview["books"][0]["state"] == "UNSEEN"


def test_migration_evidence_requires_an_owned_creation_version(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000106")
    headers = bearer(auth)
    lesson_id = stable_id("manual_page", "1")
    payload = {
        "creation_version_id": str(stable_id("creation_version", "missing")),
        "used_lessons": [str(lesson_id)],
        "revision_reason": "把样本标签原则用于作品分类。",
    }
    response = seeded_client.post(
        f"/v1/lessons/{lesson_id}/migration-evidence",
        headers={**headers, "Idempotency-Key": "migration-0001"},
        json=payload,
    )
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "CREATION_VERSION_NOT_FOUND"


def test_migration_evidence_is_reviewed_before_mastery(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000109")
    headers = bearer(auth)
    lesson_id = stable_id("manual_page", "1")
    trial = seeded_client.get(
        f"/v1/trials/{TRIAL_ID}", headers=headers
    ).json()
    passed = seeded_client.post(
        f"/v1/trials/{TRIAL_ID}/attempts",
        headers={**headers, "Idempotency-Key": "migration-trial-0001"},
        json=correct_payload(trial["current_version"]["id"]),
    )
    assert passed.status_code == 201, passed.text

    project = seeded_client.post(
        "/v1/creation-projects",
        headers={**headers, "Idempotency-Key": "migration-project-0001"},
        json={
            "title": "证据核验练习图",
            "media_type": "ILLUSTRATION",
            "default_visibility": "PRIVATE",
        },
    )
    assert project.status_code == 201, project.text
    version = seeded_client.post(
        f"/v1/creation-projects/{project.json()['id']}/versions",
        headers={**headers, "Idempotency-Key": "migration-version-0001"},
        json={
            "layers": [
                {
                    "layer_id": "migration-text",
                    "kind": "TEXT",
                    "name": "核验步骤",
                    "z_index": 0,
                    "text_content": "先找证据，再下结论",
                }
            ],
            "canvas_width": 800,
            "canvas_height": 600,
            "change_summary": "记录迁移练习",
        },
    )
    assert version.status_code == 201, version.text
    payload = {
        "creation_version_id": version.json()["id"],
        "used_lessons": [str(lesson_id)],
        "revision_reason": "把先找证据再下结论的方法用于作品说明。",
    }
    request_headers = {**headers, "Idempotency-Key": "migration-evidence-0001"}
    first = seeded_client.post(
        f"/v1/lessons/{lesson_id}/migration-evidence",
        headers=request_headers,
        json=payload,
    )
    replay = seeded_client.post(
        f"/v1/lessons/{lesson_id}/migration-evidence",
        headers=request_headers,
        json=payload,
    )
    assert first.status_code == replay.status_code == 201, first.text
    assert first.json()["validation_status"] == "PENDING_REVIEW"
    assert first.json()["evidence_id"] == replay.json()["evidence_id"]

    approved = seeded_client.post(
        f"/v1/internal/learning/evidence/{first.json()['evidence_id']}/approve",
        headers={
            "X-Internal-Token": seeded_client.app.state.settings.internal_worker_token.get_secret_value()
        },
    )
    assert approved.status_code == 200, approved.text
    assert approved.json()["state"] == "MASTERED"


def test_teaching_evidence_advances_mastered_state_and_is_idempotent(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000107")
    headers = bearer(auth)
    lesson_id = stable_id("manual_page", "1")
    user_id = auth["user"]["id"]
    with seeded_client.app.state.session_factory() as db:
        db.add(
            ManualProgress(
                user_id=uuid.UUID(user_id),
                manual_page_id=lesson_id,
                state=ManualProgressState.MASTERED,
                projection_version=1,
            )
        )
        db.commit()
    payload = {
        "explanation": "我能用自己的话解释为什么流畅输出仍需要证据核验。",
        "application_example": "在作品说明中加入来源核验步骤并记录修改前后差异。",
        "learner_feedback": "学习者复述后能指出一个没有证据支撑的结论。",
    }
    request_headers = {**headers, "Idempotency-Key": "teaching-evidence-0001"}
    first = seeded_client.post(
        f"/v1/lessons/{lesson_id}/teaching-evidence",
        headers=request_headers,
        json=payload,
    )
    replay = seeded_client.post(
        f"/v1/lessons/{lesson_id}/teaching-evidence",
        headers=request_headers,
        json=payload,
    )
    assert first.status_code == replay.status_code == 201, first.text
    assert first.json()["current_state"] == "TEACHING"
    assert first.json()["changed"] is True
    assert replay.json()["changed"] is False


def test_trial_manifest_covers_all_pages_without_fake_answers() -> None:
    manifest = build_trial_manifest()
    validate_trial_manifest(manifest)
    assert len(manifest) == 50
    assert sum(item.status == "ACTIVE" for item in manifest) == 5
    assert sum(item.status == "DRAFT" for item in manifest) == 45
    assert [item.page_no for item in manifest if item.status == "ACTIVE"] == [1, 5, 23, 29, 45]
    assert all(item.trial_type == "pending" for item in manifest if item.status == "DRAFT")
    assert all(not item.ready_for_activation for item in manifest if item.status == "DRAFT")


def test_core_route_trials_are_seeded_and_server_graded(
    seeded_client: TestClient,
) -> None:
    auth = register(seeded_client, "13930000108")
    headers = bearer(auth)
    for page_no in (5, 23, 29, 45):
        trial_id = stable_id("trial", f"manual-{page_no:02d}-core-route")
        response = seeded_client.get(f"/v1/trials/{trial_id}", headers=headers)
        assert response.status_code == 200, response.text
        body = response.json()
        definition = TRIAL_DEFINITIONS[page_no]
        answer = definition["grader_config"]["expected_answer"]
        attempt = seeded_client.post(
            f"/v1/trials/{trial_id}/attempts",
            headers={**headers, "Idempotency-Key": f"core-route-{page_no:02d}"},
            json={
                "trial_version_id": body["current_version"]["id"],
                "prediction_payload": {"choice": "先考虑风险与证据"},
                "answer_payload": answer,
                "explanation": "我根据题目中的风险和证据要求作出这个选择。",
                "client_request_id": f"android-core-route-{page_no:02d}",
            },
        )
        assert attempt.status_code == 201, attempt.text
        assert attempt.json()["passed"] is True
