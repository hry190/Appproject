from __future__ import annotations

import hashlib
import io

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from PIL import Image

from app.domains.catalog.seed import seed_catalog_data
from app.domains.media.storage import InMemoryObjectStore


OTP = "123456"
INTERNAL_TOKEN = "dev-internal-worker-token-change-me-123456"


@pytest.fixture(autouse=True)
def seed_catalog(app: FastAPI) -> None:
    with app.state.session_factory() as db:
        seed_catalog_data(db)


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


def png_bytes(width: int = 80, height: int = 40) -> bytes:
    output = io.BytesIO()
    Image.new("RGB", (width, height), (70, 130, 90)).save(
        output,
        format="PNG",
        pnginfo=None,
    )
    return output.getvalue()


def upload_ready_image(
    client: TestClient,
    headers: dict[str, str],
    *,
    data: bytes | None = None,
    purpose: str = "CREATION_LAYER",
) -> dict:
    image_data = data or png_bytes()
    digest = hashlib.sha256(image_data).hexdigest()
    intent = client.post(
        "/v1/uploads/intents",
        headers=headers,
        json={
            "purpose": purpose,
            "filename": "../../竹林.png",
            "declared_mime": "image/png",
            "byte_size": len(image_data),
            "sha256": digest,
        },
    )
    assert intent.status_code == 201, intent.text
    body = intent.json()
    assert body["upload_url"].startswith("memory://quarantine/users/")
    store = client.app.state.object_store
    assert isinstance(store, InMemoryObjectStore)
    object_key = body["upload_url"].removeprefix("memory://quarantine/")
    store.put_test_object(object_key, image_data, "image/png")
    completed = client.post(
        f"/v1/uploads/{body['id']}/complete",
        headers={**headers, "Idempotency-Key": f"complete-{body['id']}"},
        json={"byte_size": len(image_data), "sha256": digest},
    )
    assert completed.status_code == 202, completed.text
    assert completed.json()["status"] == "PROCESSING"
    processed = client.post(
        f"/v1/internal/media-assets/{completed.json()['id']}/process",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={"content_safety_outcome": "REVIEW", "aigc_detected": False},
    )
    assert processed.status_code == 200, processed.text
    assert processed.json()["status"] == "READY"
    return processed.json()


def create_project_version(
    client: TestClient,
    headers: dict[str, str],
    asset_id: str,
) -> tuple[dict, dict]:
    project_response = client.post(
        "/v1/creation-projects",
        headers=headers,
        json={"title": "竹影机关图", "media_type": "ILLUSTRATION"},
    )
    assert project_response.status_code == 201
    project = project_response.json()
    version_response = client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=headers,
        json={
            "layers": [
                {
                    "layer_id": "image-1",
                    "kind": "IMAGE",
                    "name": "底图",
                    "z_index": 0,
                    "asset_id": asset_id,
                }
            ],
            "canvas_width": 800,
            "canvas_height": 600,
            "preview_asset_id": asset_id,
            "change_summary": "完成底图",
        },
    )
    assert version_response.status_code == 201, version_response.text
    return project, version_response.json()


def complete_creation_evidence(
    client: TestClient,
    headers: dict[str, str],
    version_id: str,
) -> None:
    manual = client.get("/v1/manuals?limit=1", headers=headers).json()["items"][0]
    card = client.put(
        f"/v1/creation-versions/{version_id}/learning-card",
        headers=headers,
        json={
            "manual_page_ids": [manual["id"]],
            "method_summary": "先观察结构再组合。",
            "unresolved_questions": [],
            "questions_confirmed": True,
        },
    )
    assert card.status_code == 200, card.text
    manifest = client.put(
        f"/v1/creation-versions/{version_id}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "构图与绘制由本人完成。",
            "ai_assistance_used": False,
            "aigc_label_declared": False,
            "unresolved_rights": False,
            "items": [
                {
                    "item_type": "HUMAN_CONTRIBUTION",
                    "contribution_type": "绘制",
                    "description": "本人绘制",
                    "license_type": "ORIGINAL",
                }
            ],
        },
    )
    assert manifest.status_code == 200, manifest.text


def test_media_upload_is_private_scanned_and_generates_signed_thumbnails(
    client: TestClient,
) -> None:
    owner = register(client, "13950000001")
    other = register(client, "13950000002")
    asset = upload_ready_image(client, owner)
    assert asset["actual_mime"] == "image/png"
    assert asset["width"] == 80
    assert asset["height"] == 40
    assert asset["metadata_stripped"] is True
    assert asset["original_filename"] == "竹林.png"
    assert asset["original_url"].startswith("memory://private/")
    assert {item["kind"] for item in asset["derivatives"]} == {
        "THUMBNAIL_320",
        "THUMBNAIL_640",
    }
    hidden = client.get(f"/v1/media-assets/{asset['id']}", headers=other)
    assert hidden.status_code == 404
    internal_hidden = client.post(
        f"/v1/internal/media-assets/{asset['id']}/process", json={}
    )
    assert internal_hidden.status_code == 404

    project, _ = create_project_version(client, owner, asset["id"])
    luggage = client.get("/v1/me/luggage", headers=owner)
    assert luggage.status_code == 200, luggage.text
    thumbnail = luggage.json()["data"]["creations"]["items"][0]["thumbnail"]
    assert thumbnail["asset_id"] == asset["id"]
    assert thumbnail["url"].startswith("memory://private/")
    in_use = client.delete(f"/v1/media-assets/{asset['id']}", headers=owner)
    assert in_use.status_code == 409
    assert in_use.json()["error"]["code"] == "MEDIA_ASSET_IN_USE"
    assert client.delete(
        f"/v1/creation-projects/{project['id']}", headers=owner
    ).status_code == 204
    pending_delete = client.get(
        f"/v1/media-assets/{asset['id']}", headers=owner
    ).json()
    assert pending_delete["status"] == "DELETION_PENDING"
    deleted = client.post(
        f"/v1/internal/media-assets/{asset['id']}/process-deletion",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
    )
    assert deleted.status_code == 200
    assert deleted.json()["status"] == "DELETED"


def test_media_rejects_disallowed_type_hash_mismatch_and_eicar(
    client: TestClient,
) -> None:
    headers = register(client, "13950000003")
    svg = b"<svg xmlns='http://www.w3.org/2000/svg'></svg>"
    rejected_intent = client.post(
        "/v1/uploads/intents",
        headers=headers,
        json={
            "purpose": "CREATION_LAYER",
            "filename": "bad.svg",
            "declared_mime": "image/svg+xml",
            "byte_size": len(svg),
            "sha256": hashlib.sha256(svg).hexdigest(),
        },
    )
    assert rejected_intent.status_code == 422
    assert rejected_intent.json()["error"]["code"] == "MEDIA_TYPE_NOT_ALLOWED"

    original = png_bytes()
    claimed_hash = hashlib.sha256(original).hexdigest()
    intent = client.post(
        "/v1/uploads/intents",
        headers=headers,
        json={
            "purpose": "CREATION_LAYER",
            "filename": "hash.png",
            "declared_mime": "image/png",
            "byte_size": len(original),
            "sha256": claimed_hash,
        },
    ).json()
    corrupted = bytearray(original)
    corrupted[-1] ^= 1
    store = client.app.state.object_store
    key = intent["upload_url"].removeprefix("memory://quarantine/")
    store.put_test_object(key, bytes(corrupted), "image/png")
    completed = client.post(
        f"/v1/uploads/{intent['id']}/complete",
        headers={**headers, "Idempotency-Key": "complete-hash-mismatch"},
        json={"byte_size": len(original), "sha256": claimed_hash},
    ).json()
    processed = client.post(
        f"/v1/internal/media-assets/{completed['id']}/process",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={},
    )
    assert processed.json()["status"] == "REJECTED"
    assert processed.json()["rejection_code"] == "HASH_MISMATCH"

    eicar_png = png_bytes() + b"EICAR-STANDARD-ANTIVIRUS-TEST-FILE"
    digest = hashlib.sha256(eicar_png).hexdigest()
    virus_intent = client.post(
        "/v1/uploads/intents",
        headers=headers,
        json={
            "purpose": "CREATION_LAYER",
            "filename": "eicar.png",
            "declared_mime": "image/png",
            "byte_size": len(eicar_png),
            "sha256": digest,
        },
    ).json()
    virus_key = virus_intent["upload_url"].removeprefix("memory://quarantine/")
    store.put_test_object(virus_key, eicar_png, "image/png")
    virus_asset = client.post(
        f"/v1/uploads/{virus_intent['id']}/complete",
        headers={**headers, "Idempotency-Key": "complete-eicar-file"},
        json={"byte_size": len(eicar_png), "sha256": digest},
    ).json()
    virus_result = client.post(
        f"/v1/internal/media-assets/{virus_asset['id']}/process",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={},
    ).json()
    assert virus_result["status"] == "REJECTED"
    assert virus_result["rejection_code"] == "MALWARE_DETECTED"


def test_ready_media_is_required_before_submission(
    client: TestClient,
) -> None:
    headers = register(client, "13950000004")
    missing_asset = "00000000-0000-0000-0000-000000000777"
    project, version = create_project_version(client, headers, missing_asset)
    complete_creation_evidence(client, headers, version["id"])
    submission = client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "missing-media-submit"},
        json={"creation_version_id": version["id"]},
    )
    assert submission.status_code == 409
    codes = {item["code"] for item in submission.json()["error"]["details"]}
    assert "MEDIA_ASSET_INVALID" in codes


def test_moderation_return_appeal_publish_and_withdraw_flow(
    client: TestClient,
) -> None:
    headers = register(client, "13950000005")
    other = register(client, "13950000006")
    asset = upload_ready_image(client, headers)
    project, version = create_project_version(client, headers, asset["id"])
    complete_creation_evidence(client, headers, version["id"])
    publication = client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "moderation-submit-01"},
        json={"creation_version_id": version["id"], "visibility": "CLASSROOM"},
    )
    assert publication.status_code == 201, publication.text
    assert publication.json()["status"] == "PENDING_CHECK"
    publication_id = publication.json()["id"]
    case = client.get(
        f"/v1/publications/{publication_id}/moderation-case", headers=headers
    )
    assert case.status_code == 200
    case_body = case.json()
    assert case_body["status"] == "AUTO_CHECK"
    assert client.get(
        f"/v1/moderation-cases/{case_body['id']}", headers=other
    ).status_code == 404

    routed = client.post(
        f"/v1/internal/moderation-cases/{case_body['id']}/route",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
    )
    assert routed.json()["publication_status"] == "PENDING_HUMAN_REVIEW"
    returned = client.post(
        f"/v1/internal/moderation-cases/{case_body['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "RETURN",
            "risk_level": "MEDIUM",
            "reason_code": "PRIVACY_CLUE",
            "reason_summary": "画面中可能包含个人信息。",
            "revision_suggestion": "请遮挡可识别的信息后重新提交。",
            "reviewer_reference": "reviewer-01",
            "row_version": routed.json()["row_version"],
        },
    )
    assert returned.status_code == 200, returned.text
    assert returned.json()["publication_status"] == "RETURNED"
    assert returned.json()["can_appeal"] is True

    appeal = client.post(
        f"/v1/moderation-cases/{case_body['id']}/appeals",
        headers=headers,
        json={"reason": "该内容是虚构编号，不包含真实个人信息，请复核。"},
    )
    assert appeal.status_code == 201, appeal.text
    luggage = client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["privacy"]["pending_appeal_count"] == 1
    overturned = client.post(
        f"/v1/internal/moderation-appeals/{appeal.json()['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "status": "OVERTURNED",
            "resolution_summary": "同意重新复核。",
            "reviewer_reference": "reviewer-02",
        },
    )
    assert overturned.status_code == 200
    assert overturned.json()["status"] == "OVERTURNED"

    reopened = client.get(
        f"/v1/moderation-cases/{case_body['id']}", headers=headers
    ).json()
    published = client.post(
        f"/v1/internal/moderation-cases/{case_body['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "PUBLISH",
            "risk_level": "LOW",
            "reviewer_reference": "reviewer-03",
            "row_version": reopened["row_version"],
        },
    )
    assert published.status_code == 200, published.text
    assert published.json()["publication_status"] == "PUBLISHED"
    current_project = client.get(
        f"/v1/creation-projects/{project['id']}", headers=headers
    ).json()
    withdrawn = client.post(
        f"/v1/publications/{publication_id}/withdraw",
        headers=headers,
        json={"row_version": current_project["latest_publication"]["row_version"]},
    )
    assert withdrawn.status_code == 200, withdrawn.text
    assert withdrawn.json()["publication_status"] == "WITHDRAWN"


def test_privacy_settings_use_optimistic_lock_and_project_delete_hides_work(
    client: TestClient,
) -> None:
    headers = register(client, "13950000007")
    initial = client.get("/v1/me/privacy-settings", headers=headers)
    assert initial.status_code == 200
    assert initial.json()["default_work_visibility"] == "PRIVATE"
    assert initial.json()["guardian_controls_active"] is True
    updated = client.patch(
        "/v1/me/privacy-settings",
        headers=headers,
        json={
            "default_work_visibility": "CLASSROOM",
            "aigc_export_mark_enabled": True,
            "row_version": initial.json()["row_version"],
        },
    )
    assert updated.status_code == 200
    stale = client.patch(
        "/v1/me/privacy-settings",
        headers=headers,
        json={
            "profile_discovery_enabled": True,
            "row_version": initial.json()["row_version"],
        },
    )
    assert stale.status_code == 409

    project = client.post(
        "/v1/creation-projects",
        headers=headers,
        json={"title": "待删除作品", "media_type": "ILLUSTRATION"},
    ).json()
    assert project["default_visibility"] == "CLASSROOM"
    removed = client.delete(
        f"/v1/creation-projects/{project['id']}", headers=headers
    )
    assert removed.status_code == 204
    assert client.get(
        f"/v1/creation-projects/{project['id']}", headers=headers
    ).status_code == 404
    assert client.get("/v1/me/creation-projects", headers=headers).json()["total"] == 0


def test_only_ready_avatar_assets_can_be_attached_to_profile(
    client: TestClient,
) -> None:
    headers = register(client, "13950000008")
    avatar = upload_ready_image(client, headers, purpose="AVATAR")
    profile = client.get("/v1/profile", headers=headers).json()
    attached = client.patch(
        "/v1/profile",
        headers=headers,
        json={
            "avatar_asset_id": avatar["id"],
            "row_version": profile["row_version"],
        },
    )
    assert attached.status_code == 200, attached.text
    assert attached.json()["avatar_asset_id"] == avatar["id"]
    luggage = client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["profile"]["avatar"]["asset_id"] == avatar["id"]
