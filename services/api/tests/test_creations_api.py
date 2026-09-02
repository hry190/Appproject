from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.domains.catalog.seed import seed_catalog_data


OTP = "123456"


@pytest.fixture
def seeded_client(app: FastAPI) -> Iterator[TestClient]:
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    with TestClient(app) as client:
        yield client


def register(client: TestClient, phone: str) -> dict[str, str]:
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
            "terms_version": "2026-08",
            "privacy_version": "2026-08",
        },
    )
    assert response.status_code == 201, response.text
    return {"Authorization": f"Bearer {response.json()['tokens']['access_token']}"}


def create_project(client: TestClient, headers: dict[str, str]) -> dict:
    response = client.post(
        "/v1/creation-projects",
        headers=headers,
        json={
            "title": "竹影机关图",
            "media_type": "ILLUSTRATION",
            "default_visibility": "PRIVATE",
        },
    )
    assert response.status_code == 201, response.text
    return response.json()


def create_text_version(
    client: TestClient,
    headers: dict[str, str],
    project_id: str,
    parent_version_id: str | None = None,
) -> dict:
    response = client.post(
        f"/v1/creation-projects/{project_id}/versions",
        headers=headers,
        json={
            "parent_version_id": parent_version_id,
            "layers": [
                {
                    "layer_id": "text-1",
                    "kind": "TEXT",
                    "name": "题字",
                    "z_index": 0,
                    "text_content": "竹影",
                }
            ],
            "canvas_width": 800,
            "canvas_height": 600,
            "change_summary": "保存构图",
        },
    )
    assert response.status_code == 201, response.text
    return response.json()


def test_projects_are_private_versioned_and_optimistically_locked(
    seeded_client: TestClient,
) -> None:
    owner = register(seeded_client, "13940000001")
    other = register(seeded_client, "13940000002")
    project = create_project(seeded_client, owner)

    hidden = seeded_client.get(
        f"/v1/creation-projects/{project['id']}", headers=other
    )
    assert hidden.status_code == 404
    assert hidden.json()["error"]["code"] == "CREATION_NOT_FOUND"

    version1 = create_text_version(seeded_client, owner, project["id"])
    assert version1["version_number"] == 1
    assert version1["layers"][0]["text_content"] == "竹影"
    stale_parent = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=owner,
        json={
            "layers": [{"layer_id": "x", "kind": "TEXT", "name": "x", "z_index": 0, "text_content": "x"}],
            "canvas_width": 10,
            "canvas_height": 10,
            "change_summary": "错误父版本",
        },
    )
    assert stale_parent.status_code == 409
    assert stale_parent.json()["error"]["code"] == "STALE_VERSION_PARENT"

    version2 = create_text_version(
        seeded_client, owner, project["id"], version1["id"]
    )
    assert version2["version_number"] == 2
    versions = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/versions", headers=owner
    ).json()
    assert [item["version_number"] for item in versions["items"]] == [2, 1]
    assert versions["items"][1]["layers"][0]["text_content"] == "竹影"

    current = seeded_client.get(
        f"/v1/creation-projects/{project['id']}", headers=owner
    ).json()
    updated = seeded_client.patch(
        f"/v1/creation-projects/{project['id']}",
        headers=owner,
        json={"title": "竹影机关图·修订", "row_version": current["row_version"]},
    )
    assert updated.status_code == 200
    stale = seeded_client.patch(
        f"/v1/creation-projects/{project['id']}",
        headers=owner,
        json={"title": "过期修改", "row_version": current["row_version"]},
    )
    assert stale.status_code == 409
    assert stale.json()["error"]["code"] == "VERSION_CONFLICT"


def test_submission_requires_learning_card_and_provenance_then_locks_them(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000003")
    project = create_project(seeded_client, headers)
    version = create_text_version(seeded_client, headers, project["id"])

    incomplete = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "creation-submit-01"},
        json={"creation_version_id": version["id"]},
    )
    assert incomplete.status_code == 409
    error = incomplete.json()["error"]
    assert error["code"] == "SUBMISSION_INCOMPLETE"
    assert {item["code"] for item in error["details"]} == {
        "LEARNING_CARD_REQUIRED",
        "PROVENANCE_REQUIRED",
    }

    manual_id = seeded_client.get(
        "/v1/manuals?limit=1", headers=headers
    ).json()["items"][0]["id"]
    card = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/learning-card",
        headers=headers,
        json={
            "manual_page_ids": [manual_id],
            "method_summary": "先拆形，再组合。",
            "unresolved_questions": [],
            "questions_confirmed": True,
        },
    )
    assert card.status_code == 200, card.text
    assert card.json()["status"] == "COMPLETE"
    manifest = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "构思与题字均由本人完成。",
            "ai_assistance_used": False,
            "aigc_label_declared": False,
            "unresolved_rights": False,
            "items": [
                {
                    "item_type": "HUMAN_CONTRIBUTION",
                    "contribution_type": "构思",
                    "description": "本人构图",
                    "license_type": "ORIGINAL",
                }
            ],
        },
    )
    assert manifest.status_code == 200, manifest.text
    assert manifest.json()["status"] == "COMPLETE"

    submit_headers = {**headers, "Idempotency-Key": "creation-submit-02"}
    submitted = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers=submit_headers,
        json={"creation_version_id": version["id"], "visibility": "CLASSROOM"},
    )
    replay = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers=submit_headers,
        json={"creation_version_id": version["id"], "visibility": "CLASSROOM"},
    )
    assert submitted.status_code == replay.status_code == 201
    assert submitted.json() == replay.json()
    assert submitted.json()["status"] == "PENDING_CHECK"

    locked_card = seeded_client.get(
        f"/v1/creation-versions/{version['id']}/learning-card", headers=headers
    ).json()
    assert locked_card["status"] == "LOCKED"
    change_locked = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/learning-card",
        headers=headers,
        json={
            "manual_page_ids": [manual_id],
            "method_summary": "试图篡改",
            "unresolved_questions": [],
            "questions_confirmed": True,
            "row_version": locked_card["row_version"],
        },
    )
    assert change_locked.status_code == 409
    assert change_locked.json()["error"]["code"] == "LEARNING_CARD_LOCKED"

    luggage = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["creations"]["counts_by_status"]["PENDING_CHECK"] == 1
    assert luggage["data"]["creations"]["items"][0]["current_version"] == 1
    assert luggage["data"]["creations"]["empty_reason"] is None


def test_ai_layers_require_aigc_disclosure_and_complete_ai_trace(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000004")
    project = create_project(seeded_client, headers)
    asset_id = "00000000-0000-0000-0000-000000000123"
    version_response = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=headers,
        json={
            "layers": [{"layer_id": "ai", "kind": "AI_GENERATED", "name": "底图", "z_index": 0, "asset_id": asset_id, "aigc": True}],
            "canvas_width": 256,
            "canvas_height": 256,
            "change_summary": "生成底图",
        },
    )
    assert version_response.status_code == 201, version_response.text
    version = version_response.json()
    draft = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "本人选择主题。",
            "ai_assistance_used": True,
            "aigc_label_declared": False,
            "unresolved_rights": False,
            "items": [{"item_type": "HUMAN_CONTRIBUTION", "contribution_type": "构思", "description": "选题", "license_type": "ORIGINAL"}],
        },
    )
    assert draft.status_code == 200, draft.text
    assert draft.json()["status"] == "DRAFT"

    manifest = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "本人选择主题。",
            "ai_assistance_used": True,
            "ai_contribution_summary": "AI 生成底图。",
            "aigc_label_declared": True,
            "unresolved_rights": False,
            "row_version": draft.json()["row_version"],
            "items": [
                {"item_type": "HUMAN_CONTRIBUTION", "contribution_type": "构思", "description": "选题", "license_type": "ORIGINAL"},
                {"item_type": "AI_CONTRIBUTION", "contribution_type": "生成", "description": "底图", "license_type": "NOT_APPLICABLE", "ai_provider": "OpenAI", "ai_model": "image", "ai_tool_action": "生成", "prompt_summary": "竹林机关", "output_asset_id": asset_id, "user_modified": True},
            ],
        },
    )
    assert manifest.status_code == 200, manifest.text
    assert manifest.json()["status"] == "COMPLETE"


def test_layer_and_external_material_validation_is_explicit(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000005")
    project = create_project(seeded_client, headers)
    invalid_layer = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=headers,
        json={
            "layers": [{"layer_id": "ai", "kind": "AI_GENERATED", "name": "底图", "z_index": 0, "aigc": True}],
            "canvas_width": 100,
            "canvas_height": 100,
            "change_summary": "缺少资源",
        },
    )
    assert invalid_layer.status_code == 422

    version = create_text_version(seeded_client, headers, project["id"])
    draft = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "本人完成排版。",
            "ai_assistance_used": False,
            "aigc_label_declared": False,
            "unresolved_rights": True,
            "items": [
                {"item_type": "HUMAN_CONTRIBUTION", "contribution_type": "排版", "description": "本人排版", "license_type": "ORIGINAL"},
                {"item_type": "EXTERNAL_MATERIAL", "contribution_type": "参考", "description": "参考图", "license_type": "UNKNOWN"},
            ],
        },
    )
    assert draft.status_code == 200, draft.text
    assert draft.json()["status"] == "DRAFT"

    logs = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/change-logs", headers=headers
    )
    assert logs.status_code == 200
    assert {item["action"] for item in logs.json()["items"]} >= {
        "PROJECT_CREATED",
        "VERSION_CREATED",
        "PROVENANCE_UPDATED",
    }
