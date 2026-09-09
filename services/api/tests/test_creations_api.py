from __future__ import annotations

from collections.abc import Iterator
from datetime import timedelta
import io
import uuid

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from PIL import Image
from sqlalchemy import select

from app.core.security import utcnow
from app.domains.catalog.seed import seed_catalog_data
from app.domains.learning.contracts import ManualProgressState
from app.domains.learning.models import ManualProgress
from app.domains.media.models import (
    MediaAsset,
    MediaAssetStatus,
    UploadPurpose,
    UploadSession,
    UploadSessionStatus,
)
from app.domains.creations.image_generation import (
    DevelopmentImageGenerator,
    ImageGenerationProviderError,
)
from app.domains.creations.image_generation_service import ImageGenerationService
from app.domains.creations.export_service import CreationExportService
from app.domains.media.service import MediaService
from app.models import User


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


def prepare_project_for_production(
    client: TestClient,
    headers: dict[str, str],
) -> tuple[dict, dict, dict]:
    project = create_project(client, headers)
    version = create_text_version(client, headers, project["id"])
    current = client.get(
        f"/v1/creation-projects/{project['id']}", headers=headers
    ).json()
    method = client.put(
        f"/v1/creation-projects/{project['id']}/method",
        headers=headers,
        json={
            "name": "图文创作工法",
            "goal": "让竹影机关图的主题更清楚",
            "audience": ["同学与老师"],
            "format": "图文画面",
            "steps": ["构思", "草图", "制作", "测试"],
            "expected_revision": current["row_version"],
        },
    )
    assert method.status_code == 200, method.text
    draft = client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "IDEATION",
            "to_stage": "DRAFT",
            "reason": "工法与步骤已经确认",
            "expected_revision": method.json()["project_revision"],
        },
    )
    assert draft.status_code == 201, draft.text
    production = client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "DRAFT",
            "to_stage": "PRODUCTION",
            "reason": "开始制作当前版本",
            "expected_revision": draft.json()["project_revision"],
        },
    )
    assert production.status_code == 201, production.text
    return project, version, production.json()


def advance_existing_project_to_seal(
    client: TestClient,
    headers: dict[str, str],
    project_id: str,
    version_id: str,
) -> dict:
    current = client.get(f"/v1/creation-projects/{project_id}", headers=headers).json()
    method = client.put(
        f"/v1/creation-projects/{project_id}/method",
        headers=headers,
        json={
            "name": "图文创作工法",
            "goal": "完成作品并说明学习过程",
            "audience": ["同学与老师"],
            "format": "图文画面",
            "steps": ["构思", "草图", "制作", "测试", "说明"],
            "expected_revision": current["row_version"],
        },
    )
    assert method.status_code == 200, method.text
    revision = method.json()["project_revision"]
    for from_stage, to_stage, reason in [
        ("IDEATION", "DRAFT", "工法已经确认"),
        ("DRAFT", "PRODUCTION", "开始制作当前版本"),
        ("PRODUCTION", "TEST", "当前版本制作完成，开始测试"),
    ]:
        transition = client.post(
            f"/v1/creation-projects/{project_id}/stage-transitions",
            headers=headers,
            json={
                "from_stage": from_stage,
                "to_stage": to_stage,
                "reason": reason,
                "expected_revision": revision,
            },
        )
        assert transition.status_code == 201, transition.text
        revision = transition.json()["project_revision"]
    passed = client.post(
        f"/v1/creation-projects/{project_id}/test-records",
        headers=headers,
        json={
            "creation_version_id": version_id,
            "scenario": "请同学辨认作品主题并检查表达",
            "result": "PASSED",
            "notes": "主题与操作说明均能被准确理解。",
            "findings": [],
        },
    )
    assert passed.status_code == 201, passed.text
    sealed = client.post(
        f"/v1/creation-projects/{project_id}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "TEST",
            "to_stage": "SEAL",
            "reason": "当前版本复测通过，进入作品说明",
            "expected_revision": revision,
        },
    )
    assert sealed.status_code == 201, sealed.text
    return client.get(f"/v1/creation-projects/{project_id}", headers=headers).json()


def latest_user_id(client: TestClient) -> uuid.UUID:
    with client.app.state.session_factory() as db:
        user_id = db.scalar(select(User.id).order_by(User.created_at.desc()).limit(1))
        assert user_id is not None
        return user_id


def add_ready_creation_asset(client: TestClient, user_id: uuid.UUID) -> uuid.UUID:
    upload_id = uuid.uuid4()
    asset_id = uuid.uuid4()
    with client.app.state.session_factory() as db:
        db.add(
            UploadSession(
                id=upload_id,
                owner_user_id=user_id,
                purpose=UploadPurpose.CREATION_LAYER,
                original_filename="竹影草图.png",
                declared_mime="image/png",
                expected_bytes=8,
                client_sha256="0" * 64,
                object_key=f"users/{user_id}/uploads/{upload_id}.png",
                status=UploadSessionStatus.COMPLETED,
                expires_at=utcnow() + timedelta(minutes=15),
                completed_at=utcnow(),
            )
        )
        db.flush()
        db.add(
            MediaAsset(
                id=asset_id,
                owner_user_id=user_id,
                upload_session_id=upload_id,
                purpose=UploadPurpose.CREATION_LAYER,
                original_filename="竹影草图.png",
                declared_mime="image/png",
                actual_mime="image/png",
                byte_size=8,
                sha256="0" * 64,
                quarantine_object_key=f"users/{user_id}/uploads/{upload_id}.png",
                private_object_key=f"users/{user_id}/assets/{asset_id}/sanitized.png",
                status=MediaAssetStatus.READY,
                width=1,
                height=1,
                metadata_stripped=True,
                row_version=2,
                ready_at=utcnow(),
            )
        )
        db.commit()
    return asset_id


def process_image_generation(client: TestClient, job_id: str) -> None:
    with client.app.state.session_factory() as db:
        media_service = MediaService(
            db=db,
            settings=client.app.state.settings,
            store=client.app.state.object_store,
            virus_scanner=client.app.state.virus_scanner,
            request_id=f"test-generation:{job_id}",
        )
        ImageGenerationService(
            db=db,
            settings=client.app.state.settings,
            generator=client.app.state.image_generator,
            media_service=media_service,
            request_id=f"test-generation:{job_id}",
        ).process_job(uuid.UUID(job_id))


def process_creation_export(client: TestClient, job_id: str) -> None:
    with client.app.state.session_factory() as db:
        media_service = MediaService(
            db=db,
            settings=client.app.state.settings,
            store=client.app.state.object_store,
            virus_scanner=client.app.state.virus_scanner,
            request_id=f"test-export:{job_id}",
        )
        CreationExportService(
            db=db,
            store=client.app.state.object_store,
            media_service=media_service,
            request_id=f"test-export:{job_id}",
        ).process_job(uuid.UUID(job_id))


def test_creation_export_flattens_immutable_version_and_returns_safe_download(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000035")
    project = create_project(seeded_client, headers)
    version = create_text_version(seeded_client, headers, project["id"])
    endpoint = f"/v1/creation-versions/{version['id']}/exports"

    unconfirmed = seeded_client.post(
        endpoint,
        headers={**headers, "Idempotency-Key": "export-unconfirmed-01"},
        json={"format": "PNG", "output_scale": 1, "user_confirmed_export": False},
    )
    assert unconfirmed.status_code == 422

    request_headers = {**headers, "Idempotency-Key": "export-version-one-png"}
    payload = {"format": "PNG", "output_scale": 2, "user_confirmed_export": True}
    queued = seeded_client.post(endpoint, headers=request_headers, json=payload)
    replay = seeded_client.post(endpoint, headers=request_headers, json=payload)
    assert queued.status_code == replay.status_code == 202
    assert queued.json()["id"] == replay.json()["id"]
    assert queued.json()["status"] == "QUEUED"

    process_creation_export(seeded_client, queued.json()["id"])
    completed = seeded_client.get(
        f"/v1/creation-export-jobs/{queued.json()['id']}", headers=headers
    )
    assert completed.status_code == 200, completed.text
    result = completed.json()
    assert result["status"] == "COMPLETED"
    assert result["version_number"] == 1
    assert result["output_asset"]["status"] == "READY"
    assert result["output_asset"]["purpose"] == "CREATION_PREVIEW"
    assert result["output_asset"]["width"] == 1600
    assert result["output_asset"]["height"] == 1200
    assert result["output_asset"]["original_url"].startswith("memory://private/")

    with seeded_client.app.state.session_factory() as db:
        asset = db.get(MediaAsset, uuid.UUID(result["output_asset"]["id"]))
        assert asset is not None and asset.private_object_key
        rendered = seeded_client.app.state.object_store.read_private(asset.private_object_key)
    with Image.open(io.BytesIO(rendered)) as image:
        assert image.format == "PNG"
        assert image.size == (1600, 1200)
        assert image.getbbox() is not None

    listed = seeded_client.get(endpoint, headers=headers)
    assert listed.status_code == 200
    assert listed.json()["items"][0]["id"] == result["id"]
    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    exported_project = next(item for item in exported["creations"] if item["id"] == project["id"])
    assert exported_project["creation_export_jobs"][0]["status"] == "COMPLETED"


def test_image_generation_requires_confirmation_and_writes_safe_version(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000031")
    project, version, production = prepare_project_for_production(seeded_client, headers)
    endpoint = f"/v1/creation-projects/{project['id']}/image-generations"
    base_payload = {
        "parent_version_id": version["id"],
        "prompt": "竹林中的木制机关鸟，主体清楚，留出题字空间",
        "size": "LANDSCAPE",
        "quality": "MEDIUM",
        "expected_project_revision": production["project_revision"],
    }
    unconfirmed = seeded_client.post(
        endpoint,
        headers={**headers, "Idempotency-Key": "generation-unconfirmed-01"},
        json={**base_payload, "user_confirmed_generation": False},
    )
    assert unconfirmed.status_code == 422

    private_prompt = seeded_client.post(
        endpoint,
        headers={**headers, "Idempotency-Key": "generation-private-01"},
        json={
            **base_payload,
            "prompt": "请画出机关鸟，并写上邮箱 student@example.com",
            "user_confirmed_generation": True,
        },
    )
    assert private_prompt.status_code == 422
    assert private_prompt.json()["error"]["code"] == "GENERATION_PERSONAL_DATA"

    request_headers = {**headers, "Idempotency-Key": "generation-safe-0001"}
    queued = seeded_client.post(
        endpoint,
        headers=request_headers,
        json={**base_payload, "user_confirmed_generation": True},
    )
    replay = seeded_client.post(
        endpoint,
        headers=request_headers,
        json={**base_payload, "user_confirmed_generation": True},
    )
    assert queued.status_code == replay.status_code == 202
    assert queued.json()["id"] == replay.json()["id"]
    assert queued.json()["status"] == "QUEUED"
    assert queued.json()["external_data_shared"] is False

    process_image_generation(seeded_client, queued.json()["id"])
    completed = seeded_client.get(
        f"/v1/image-generation-jobs/{queued.json()['id']}", headers=headers
    )
    assert completed.status_code == 200, completed.text
    result = completed.json()
    assert result["status"] == "COMPLETED", (result["error_code"], result["error_summary"])
    assert result["progress_percent"] == 100
    assert result["output_asset"]["status"] == "READY"
    assert result["output_asset"]["aigc_detected"] is True
    assert result["output_version_id"] is not None

    versions = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/versions", headers=headers
    ).json()["items"]
    assert [item["version_number"] for item in versions] == [2, 1]
    assert versions[0]["preview_asset_id"] == result["output_asset"]["id"]
    generated_layer = versions[0]["layers"][-1]
    assert generated_layer["kind"] == "AI_GENERATED"
    assert generated_layer["aigc"] is True

    provenance = seeded_client.get(
        f"/v1/creation-versions/{result['output_version_id']}/provenance-manifest",
        headers=headers,
    ).json()
    assert provenance["status"] == "DRAFT"
    assert provenance["ai_assistance_used"] is True
    assert provenance["aigc_label_declared"] is True
    assert provenance["items"][0]["output_asset_id"] == result["output_asset"]["id"]

    jobs = seeded_client.get(endpoint, headers=headers).json()
    assert jobs["daily_used"] == 1
    assert jobs["daily_remaining"] == jobs["daily_limit"] - 1
    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    exported_project = next(item for item in exported["creations"] if item["id"] == project["id"])
    assert exported_project["image_generation_jobs"][0]["status"] == "COMPLETED"


def test_retryable_generation_failure_refunds_quota_and_can_retry(
    seeded_client: TestClient,
) -> None:
    class FailingGenerator:
        provider_ref = "test-provider"
        model_ref = "failure-v1"
        external_data_shared = False

        def generate(self, **_: object) -> object:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_UNAVAILABLE",
                "temporary failure",
                retryable=True,
            )

    headers = register(seeded_client, "13940000032")
    project, version, production = prepare_project_for_production(seeded_client, headers)
    seeded_client.app.state.image_generator = FailingGenerator()
    endpoint = f"/v1/creation-projects/{project['id']}/image-generations"
    queued = seeded_client.post(
        endpoint,
        headers={**headers, "Idempotency-Key": "generation-retry-0001"},
        json={
            "parent_version_id": version["id"],
            "prompt": "一只木制机关鸟在竹林中展开翅膀",
            "size": "SQUARE",
            "quality": "LOW",
            "expected_project_revision": production["project_revision"],
            "user_confirmed_generation": True,
        },
    )
    assert queued.status_code == 202, queued.text
    process_image_generation(seeded_client, queued.json()["id"])
    failed = seeded_client.get(
        f"/v1/image-generation-jobs/{queued.json()['id']}", headers=headers
    ).json()
    assert failed["status"] == "FAILED"
    assert failed["retryable"] is True
    assert seeded_client.get(endpoint, headers=headers).json()["daily_used"] == 0

    seeded_client.app.state.image_generator = DevelopmentImageGenerator()
    retried = seeded_client.post(
        f"/v1/image-generation-jobs/{failed['id']}/retry",
        headers={**headers, "Idempotency-Key": "generation-retry-call-01"},
        json={"expected_revision": failed["row_version"]},
    )
    assert retried.status_code == 202, retried.text
    assert retried.json()["status"] == "QUEUED"
    assert retried.json()["retry_count"] == 1
    process_image_generation(seeded_client, failed["id"])
    completed = seeded_client.get(
        f"/v1/image-generation-jobs/{failed['id']}", headers=headers
    ).json()
    assert completed["status"] == "COMPLETED"
    assert seeded_client.get(endpoint, headers=headers).json()["daily_used"] == 1


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

    premature = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "creation-submit-premature"},
        json={"creation_version_id": version["id"]},
    )
    assert premature.status_code == 409
    assert premature.json()["error"]["code"] == "SUBMISSION_STAGE_INVALID"
    pre_seal_check = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/seal-check",
        headers=headers,
        json={
            "work_description": "提前填写",
            "learning_reflection": "提前填写",
            "next_improvement": "提前填写",
            "identity_privacy_confirmed": True,
            "contact_privacy_confirmed": True,
            "portrait_rights_confirmed": True,
        },
    )
    assert pre_seal_check.status_code == 409
    assert pre_seal_check.json()["error"]["code"] == "SEAL_STAGE_REQUIRED"

    advance_existing_project_to_seal(
        seeded_client, headers, project["id"], version["id"]
    )
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
        "SEAL_CHECK_REQUIRED",
    }

    card = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/learning-card",
        headers=headers,
        json={
            "manual_page_ids": [],
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
    seal_draft = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/seal-check",
        headers=headers,
        json={
            "work_description": "",
            "learning_reflection": "",
            "next_improvement": "",
            "identity_privacy_confirmed": False,
            "contact_privacy_confirmed": False,
            "portrait_rights_confirmed": False,
        },
    )
    assert seal_draft.status_code == 200, seal_draft.text
    assert seal_draft.json()["status"] == "DRAFT"
    incomplete_seal = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "creation-submit-partial"},
        json={"creation_version_id": version["id"], "visibility": "CLASSROOM"},
    )
    assert incomplete_seal.status_code == 409
    assert {item["code"] for item in incomplete_seal.json()["error"]["details"]} == {
        "WORK_DESCRIPTION_REQUIRED",
        "LEARNING_REFLECTION_REQUIRED",
        "NEXT_IMPROVEMENT_REQUIRED",
        "PRIVACY_SELF_CHECK_REQUIRED",
    }
    seal_check = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/seal-check",
        headers=headers,
        json={
            "work_description": "一幅帮助同学辨认竹林机关结构的图文作品。",
            "learning_reflection": "我学会了用明度差突出关键结构。",
            "next_improvement": "下一版会增加更简洁的操作提示。",
            "identity_privacy_confirmed": True,
            "contact_privacy_confirmed": True,
            "portrait_rights_confirmed": True,
            "row_version": seal_draft.json()["row_version"],
        },
    )
    assert seal_check.status_code == 200, seal_check.text
    assert seal_check.json()["status"] == "COMPLETE"

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
            "manual_page_ids": [],
            "method_summary": "试图篡改",
            "unresolved_questions": [],
            "questions_confirmed": True,
            "row_version": locked_card["row_version"],
        },
    )
    assert change_locked.status_code == 409
    assert change_locked.json()["error"]["code"] == "LEARNING_CARD_LOCKED"

    locked_seal_check = seeded_client.get(
        f"/v1/creation-versions/{version['id']}/seal-check", headers=headers
    ).json()
    assert locked_seal_check["status"] == "LOCKED"
    change_locked_seal = seeded_client.put(
        f"/v1/creation-versions/{version['id']}/seal-check",
        headers=headers,
        json={
            "work_description": "试图修改",
            "learning_reflection": "试图修改",
            "next_improvement": "试图修改",
            "identity_privacy_confirmed": True,
            "contact_privacy_confirmed": True,
            "portrait_rights_confirmed": True,
            "row_version": locked_seal_check["row_version"],
        },
    )
    assert change_locked_seal.status_code == 409
    assert change_locked_seal.json()["error"]["code"] == "SEAL_CHECK_LOCKED"

    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    exported_project = next(
        item for item in exported["creations"] if item["id"] == project["id"]
    )
    assert exported_project["versions"][0]["seal_check"]["status"] == "LOCKED"

    luggage = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert luggage["data"]["creations"]["counts_by_status"]["PENDING_CHECK"] == 1
    assert luggage["data"]["creations"]["items"][0]["current_version"] == 1
    assert luggage["data"]["creations"]["empty_reason"] is None

    revised = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=headers,
        json={
            "parent_version_id": version["id"],
            "layers": [
                {
                    "layer_id": "text-1",
                    "kind": "TEXT",
                    "name": "题字",
                    "z_index": 0,
                    "text_content": "竹影·修订",
                }
            ],
            "canvas_width": 800,
            "canvas_height": 600,
            "change_summary": "封卷后修订说明文字",
            "modification_reason": "根据说明检查结果调整题字",
        },
    )
    assert revised.status_code == 201, revised.text
    assert revised.json()["version_number"] == 2
    reopened = seeded_client.get(
        f"/v1/creation-projects/{project['id']}", headers=headers
    ).json()
    assert reopened["current_stage"] == "PRODUCTION"
    events = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/stage-events", headers=headers
    ).json()["items"]
    assert events[-1]["from_stage"] == "SEAL"
    assert events[-1]["to_stage"] == "PRODUCTION"
    resubmit_without_retest = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/submissions",
        headers={**headers, "Idempotency-Key": "creation-submit-revised"},
        json={"creation_version_id": revised.json()["id"], "visibility": "PRIVATE"},
    )
    assert resubmit_without_retest.status_code == 409
    assert resubmit_without_retest.json()["error"]["code"] == (
        "SUBMISSION_STAGE_INVALID"
    )


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


def test_creation_intent_analysis_returns_a_persisted_method_draft(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000006")
    response = seeded_client.post(
        "/v1/creation-intents:analyze",
        headers=headers,
        json={
            "text": "做一个会互动的竹林环保小游戏",
            "attachment_refs": ["local-sketch-01"],
            "resource_links": ["https://example.com/reference"],
        },
    )

    assert response.status_code == 201, response.text
    analysis = response.json()
    assert analysis["intent_id"]
    assert analysis["analysis_id"]
    assert analysis["schema_version"] == "1.0"
    assert analysis["method_draft"]["goal"] == "做一个会互动的竹林环保小游戏"
    assert analysis["method_draft"]["recommended_media_type"] == "ILLUSTRATION"
    assert analysis["method_draft"]["resource_links"] == [
        "https://example.com/reference"
    ]
    assert "MVP_MEDIA_FALLBACK" in analysis["safety_flags"]
    project_payload = {
        "title": "竹林环保小游戏",
        "description": analysis["method_draft"]["goal"],
        "intent_id": analysis["intent_id"],
        "media_type": analysis["method_draft"]["recommended_media_type"],
    }
    create_headers = {**headers, "Idempotency-Key": "intent-project-001"}
    created = seeded_client.post(
        "/v1/creation-projects", headers=create_headers, json=project_payload
    )
    replay = seeded_client.post(
        "/v1/creation-projects", headers=create_headers, json=project_payload
    )
    duplicate_conversion = seeded_client.post(
        "/v1/creation-projects",
        headers={**headers, "Idempotency-Key": "intent-project-002"},
        json=project_payload,
    )
    assert created.status_code == replay.status_code == 201
    assert created.json()["id"] == replay.json()["id"]
    assert created.json()["source_intent_id"] == analysis["intent_id"]
    assert duplicate_conversion.status_code == 409
    assert duplicate_conversion.json()["error"]["code"] == (
        "CREATION_INTENT_ALREADY_CONVERTED"
    )

    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    exported_intent = exported["creation_intents"][0]
    assert exported_intent["text"] == "做一个会互动的竹林环保小游戏"
    assert exported_intent["status"] == "CONVERTED"
    assert exported_intent["analyses"][0]["model_ref"] == "rules-v1"


def test_verified_sketch_and_learned_manual_flow_into_method_and_export(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000012")
    user_id = latest_user_id(seeded_client)
    manual_id = uuid.UUID(
        seeded_client.get("/v1/manuals?limit=1", headers=headers).json()["items"][0]["id"]
    )
    with seeded_client.app.state.session_factory() as db:
        db.add(
            ManualProgress(
                user_id=user_id,
                manual_page_id=manual_id,
                state=ManualProgressState.LEARNED,
                learned_at=utcnow(),
            )
        )
        db.commit()
    asset_id = add_ready_creation_asset(seeded_client, user_id)

    analyzed = seeded_client.post(
        "/v1/creation-intents:analyze",
        headers=headers,
        json={
            "text": "参考草图做一张竹影机关图",
            "attachment_asset_ids": [str(asset_id)],
            "manual_page_ids": [str(manual_id)],
        },
    )
    assert analyzed.status_code == 201, analyzed.text
    draft = analyzed.json()["method_draft"]
    assert draft["source_asset_ids"] == [str(asset_id)]
    assert draft["manual_page_ids"] == [str(manual_id)]
    referenced_delete = seeded_client.delete(
        f"/v1/media-assets/{asset_id}", headers=headers
    )
    assert referenced_delete.status_code == 409
    assert referenced_delete.json()["error"]["code"] == "MEDIA_ASSET_IN_USE"

    project = seeded_client.post(
        "/v1/creation-projects",
        headers={**headers, "Idempotency-Key": "verified-sources-project"},
        json={
            "title": "竹影机关图",
            "intent_id": analyzed.json()["intent_id"],
            "media_type": draft["recommended_media_type"],
        },
    ).json()
    method = seeded_client.put(
        f"/v1/creation-projects/{project['id']}/method",
        headers=headers,
        json={
            "name": draft["name"],
            "goal": draft["goal"],
            "audience": draft["audience"],
            "format": draft["format"],
            "steps": draft["steps"],
            "source_asset_ids": draft["source_asset_ids"],
            "manual_page_ids": draft["manual_page_ids"],
            "expected_revision": project["row_version"],
        },
    )
    assert method.status_code == 200, method.text
    assert method.json()["source_asset_ids"] == [str(asset_id)]
    assert method.json()["manual_page_ids"] == [str(manual_id)]

    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    assert exported["creation_intents"][0]["attachment_asset_ids"] == [str(asset_id)]
    assert exported["creation_intents"][0]["manual_page_ids"] == [str(manual_id)]
    exported_method = next(
        item for item in exported["creations"] if item["id"] == project["id"]
    )["methods"][0]
    assert exported_method["source_asset_ids"] == [str(asset_id)]
    assert exported_method["manual_page_ids"] == [str(manual_id)]


def test_creation_sources_reject_unlearned_manuals_and_foreign_assets(
    seeded_client: TestClient,
) -> None:
    owner = register(seeded_client, "13940000013")
    owner_id = latest_user_id(seeded_client)
    other = register(seeded_client, "13940000014")
    other_id = latest_user_id(seeded_client)
    foreign_asset_id = add_ready_creation_asset(seeded_client, other_id)
    manual_id = seeded_client.get(
        "/v1/manuals?limit=1", headers=owner
    ).json()["items"][0]["id"]

    response = seeded_client.post(
        "/v1/creation-intents:analyze",
        headers=owner,
        json={
            "text": "带入来源制作机关图",
            "attachment_asset_ids": [str(foreign_asset_id)],
            "manual_page_ids": [manual_id],
        },
    )
    assert owner_id != other_id
    assert response.status_code == 422
    assert response.json()["error"]["code"] == "CREATION_SOURCE_INVALID"

    manual_only = seeded_client.post(
        "/v1/creation-intents:analyze",
        headers=owner,
        json={"text": "带入秘籍制作机关图", "manual_page_ids": [manual_id]},
    )
    assert manual_only.status_code == 422
    assert manual_only.json()["error"]["code"] == "MANUAL_NOT_LEARNED"


def test_guardian_creation_control_blocks_analysis_and_project_creation(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000011")
    controls = seeded_client.patch(
        "/v1/settings/guardian-controls",
        headers=headers,
        json={"creation_allowed": False},
    )
    assert controls.status_code == 200, controls.text

    analysis = seeded_client.post(
        "/v1/creation-intents:analyze",
        headers=headers,
        json={"text": "画一幅竹林机关图"},
    )
    project = seeded_client.post(
        "/v1/creation-projects",
        headers=headers,
        json={"title": "竹林机关图", "media_type": "ILLUSTRATION"},
    )

    assert analysis.status_code == project.status_code == 403
    assert analysis.json()["error"]["code"] == "CREATION_DISABLED_BY_GUARDIAN"
    assert project.json()["error"]["code"] == "CREATION_DISABLED_BY_GUARDIAN"


def test_project_creation_is_idempotent_and_rejects_key_reuse(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000007")
    request_headers = {**headers, "Idempotency-Key": "creation-project-001"}
    payload = {
        "title": "竹影机关图",
        "media_type": "ILLUSTRATION",
        "default_visibility": "PRIVATE",
    }

    first = seeded_client.post(
        "/v1/creation-projects", headers=request_headers, json=payload
    )
    replay = seeded_client.post(
        "/v1/creation-projects", headers=request_headers, json=payload
    )
    conflict = seeded_client.post(
        "/v1/creation-projects",
        headers=request_headers,
        json={**payload, "title": "另一个作品"},
    )

    assert first.status_code == replay.status_code == 201
    assert first.json()["id"] == replay.json()["id"]
    assert first.json()["current_stage"] == "IDEATION"
    assert conflict.status_code == 409
    assert conflict.json()["error"]["code"] == "IDEMPOTENCY_CONFLICT"


def test_creation_version_save_is_idempotent(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000010")
    project = create_project(seeded_client, headers)
    request_headers = {**headers, "Idempotency-Key": "creation-version-001"}
    payload = {
        "layers": [
            {
                "layer_id": "script-1",
                "kind": "TEXT",
                "name": "草图脚本",
                "z_index": 0,
                "text_content": "一只熊猫在竹林里制作机关钟",
            }
        ],
        "canvas_width": 1080,
        "canvas_height": 1920,
        "change_summary": "保存第一版草图脚本",
    }

    first = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=request_headers,
        json=payload,
    )
    replay = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=request_headers,
        json=payload,
    )
    conflict = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=request_headers,
        json={
            **payload,
            "layers": [
                {
                    **payload["layers"][0],
                    "text_content": "不同的草图脚本",
                }
            ],
        },
    )

    assert first.status_code == replay.status_code == 201
    assert first.json()["id"] == replay.json()["id"]
    assert conflict.status_code == 409
    assert conflict.json()["error"]["code"] == "IDEMPOTENCY_CONFLICT"
    versions = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/versions", headers=headers
    ).json()["items"]
    assert len(versions) == 1


def test_method_confirmation_and_stage_transitions_are_revision_guarded(
    seeded_client: TestClient,
) -> None:
    owner = register(seeded_client, "13940000008")
    other = register(seeded_client, "13940000009")
    project = create_project(seeded_client, owner)

    missing = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/method", headers=owner
    )
    assert missing.status_code == 404
    method = seeded_client.put(
        f"/v1/creation-projects/{project['id']}/method",
        headers=owner,
        json={
            "name": "图文创作工法",
            "goal": "画出竹林中的机关装置",
            "audience": ["同学与老师"],
            "format": "图文画面",
            "steps": ["构思", "草图", "制作", "测试"],
            "resource_links": [],
            "expected_revision": project["row_version"],
        },
    )
    assert method.status_code == 200, method.text
    assert method.json()["version_number"] == 1
    method_revision = method.json()["project_revision"]

    hidden = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/method", headers=other
    )
    assert hidden.status_code == 404

    draft = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=owner,
        json={
            "from_stage": "IDEATION",
            "to_stage": "DRAFT",
            "reason": "工法已确认",
            "expected_revision": method_revision,
        },
    )
    assert draft.status_code == 201, draft.text
    assert draft.json()["current_stage"] == "DRAFT"
    assert draft.json()["event"]["from_stage"] == "IDEATION"
    draft_revision = draft.json()["project_revision"]

    stale = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=owner,
        json={
            "from_stage": "IDEATION",
            "to_stage": "DRAFT",
            "reason": "重复提交",
            "expected_revision": method_revision,
        },
    )
    assert stale.status_code == 409
    assert stale.json()["error"]["code"] == "VERSION_CONFLICT"

    skipped = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=owner,
        json={
            "from_stage": "DRAFT",
            "to_stage": "TEST",
            "reason": "尝试跳过制作",
            "expected_revision": draft_revision,
        },
    )
    assert skipped.status_code == 409
    assert skipped.json()["error"]["code"] == "INVALID_STAGE_TRANSITION"

    production = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=owner,
        json={
            "from_stage": "DRAFT",
            "to_stage": "PRODUCTION",
            "reason": "草图已经完成",
            "expected_revision": draft_revision,
        },
    )
    assert production.status_code == 201, production.text
    test_without_version = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=owner,
        json={
            "from_stage": "PRODUCTION",
            "to_stage": "TEST",
            "reason": "准备测试",
            "expected_revision": production.json()["project_revision"],
        },
    )
    assert test_without_version.status_code == 409
    assert test_without_version.json()["error"]["code"] == "VERSION_REQUIRED"

    events = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/stage-events", headers=owner
    )
    assert events.status_code == 200
    assert [item["to_stage"] for item in events.json()["items"]] == [
        "DRAFT",
        "PRODUCTION",
    ]
    exported = seeded_client.get("/v1/account/export", headers=owner).json()
    exported_project = next(
        item for item in exported["creations"] if item["id"] == project["id"]
    )
    assert exported_project["current_stage"] == "PRODUCTION"
    assert exported_project["methods"][0]["name"] == "图文创作工法"
    assert [item["to_stage"] for item in exported_project["stage_events"]] == [
        "DRAFT",
        "PRODUCTION",
    ]


def test_confirmed_coach_call_and_test_issues_gate_seal(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000015")
    project, version, production = prepare_project_for_production(
        seeded_client, headers
    )

    proposal_headers = {**headers, "Idempotency-Key": "coach-proposal-001"}
    proposal_payload = {
        "kind": "COACH_REVIEW",
        "prompt": "请检查主题是否清楚，并给出不改动画面的文字建议",
    }
    proposed = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/tool-calls",
        headers=proposal_headers,
        json=proposal_payload,
    )
    replayed_proposal = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/tool-calls",
        headers=proposal_headers,
        json=proposal_payload,
    )
    assert proposed.status_code == replayed_proposal.status_code == 201
    assert proposed.json() == replayed_proposal.json()
    call = proposed.json()
    assert call["status"] == "PROPOSED"
    assert call["input_snapshot"]["prompt"] == proposal_payload["prompt"]
    assert call["external_data_shared"] is False
    assert "不修改作品" in call["effect_summary"]

    decision_headers = {**headers, "Idempotency-Key": "coach-decision-001"}
    decision_payload = {
        "approve": True,
        "expected_revision": call["row_version"],
    }
    approved = seeded_client.post(
        f"/v1/creation-tool-calls/{call['id']}/decision",
        headers=decision_headers,
        json=decision_payload,
    )
    replayed_decision = seeded_client.post(
        f"/v1/creation-tool-calls/{call['id']}/decision",
        headers=decision_headers,
        json=decision_payload,
    )
    assert approved.status_code == replayed_decision.status_code == 200
    assert approved.json() == replayed_decision.json()
    completed_call = approved.json()
    assert completed_call["status"] == "COMPLETED"
    assert completed_call["executor_ref"] == "rules-coach-v1"
    assert completed_call["external_data_shared"] is False
    assert completed_call["output_snapshot"]["checklist"]

    entered_test = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "PRODUCTION",
            "to_stage": "TEST",
            "reason": "当前版本已制作完成，开始检查",
            "expected_revision": production["project_revision"],
        },
    )
    assert entered_test.status_code == 201, entered_test.text
    test_revision = entered_test.json()["project_revision"]

    needs_revision = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/test-records",
        headers=headers,
        json={
            "creation_version_id": version["id"],
            "scenario": "让同学在不看说明的情况下说出画面主题",
            "result": "NEEDS_REVISION",
            "notes": "同学能看到竹林，但没有注意到机关结构。",
            "findings": [
                {
                    "severity": "IMPORTANT",
                    "description": "机关结构与竹叶颜色太接近，需要提高区分度",
                }
            ],
        },
    )
    assert needs_revision.status_code == 201, needs_revision.text
    issue = needs_revision.json()["issues"][0]
    assert issue["status"] == "OPEN"

    blocked_seal = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "TEST",
            "to_stage": "SEAL",
            "reason": "尝试进入作品说明",
            "expected_revision": test_revision,
        },
    )
    assert blocked_seal.status_code == 409
    assert blocked_seal.json()["error"]["code"] == "TEST_NOT_PASSED"

    resolved = seeded_client.post(
        f"/v1/creation-test-issues/{issue['id']}/resolve",
        headers=headers,
        json={
            "resolution_summary": "提高机关线条明度，并请同学重新辨认",
            "expected_revision": issue["row_version"],
        },
    )
    assert resolved.status_code == 200, resolved.text
    assert resolved.json()["status"] == "RESOLVED"

    still_needs_retest = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "TEST",
            "to_stage": "SEAL",
            "reason": "问题已处理，尝试跳过复测",
            "expected_revision": test_revision,
        },
    )
    assert still_needs_retest.status_code == 409
    assert still_needs_retest.json()["error"]["code"] == "TEST_NOT_PASSED"

    passed = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/test-records",
        headers=headers,
        json={
            "creation_version_id": version["id"],
            "scenario": "按相同场景进行第二次辨认测试",
            "result": "PASSED",
            "notes": "同学能够指出机关结构并准确说出主题。",
            "findings": [
                {
                    "severity": "NOTE",
                    "description": "说明页需要补充机关结构的操作提示",
                }
            ],
        },
    )
    assert passed.status_code == 201, passed.text
    passed_issue = passed.json()["issues"][0]
    open_issue_gate = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "TEST",
            "to_stage": "SEAL",
            "reason": "复测通过但仍有说明问题",
            "expected_revision": test_revision,
        },
    )
    assert open_issue_gate.status_code == 409
    assert open_issue_gate.json()["error"]["code"] == "TEST_ISSUES_OPEN"
    close_passed_issue = seeded_client.post(
        f"/v1/creation-test-issues/{passed_issue['id']}/resolve",
        headers=headers,
        json={
            "resolution_summary": "已在说明计划中补充机关结构操作提示",
            "expected_revision": passed_issue["row_version"],
        },
    )
    assert close_passed_issue.status_code == 200, close_passed_issue.text
    sealed = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/stage-transitions",
        headers=headers,
        json={
            "from_stage": "TEST",
            "to_stage": "SEAL",
            "reason": "复测通过且问题已经关闭",
            "expected_revision": test_revision,
        },
    )
    assert sealed.status_code == 201, sealed.text
    assert sealed.json()["current_stage"] == "SEAL"

    tool_calls = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/tool-calls", headers=headers
    ).json()["items"]
    test_records = seeded_client.get(
        f"/v1/creation-projects/{project['id']}/test-records", headers=headers
    ).json()["items"]
    assert tool_calls[0]["status"] == "COMPLETED"
    assert [item["result"] for item in test_records] == ["PASSED", "NEEDS_REVISION"]
    assert test_records[1]["issues"][0]["status"] == "RESOLVED"

    exported = seeded_client.get("/v1/account/export", headers=headers).json()
    exported_project = next(
        item for item in exported["creations"] if item["id"] == project["id"]
    )
    assert exported_project["tool_calls"][0]["executor_ref"] == "rules-coach-v1"
    assert exported_project["test_records"][0]["issues"][0]["status"] == "RESOLVED"


def test_editor_revision_persists_transforms_compares_versions_and_marks_ai_modified(
    seeded_client: TestClient,
) -> None:
    headers = register(seeded_client, "13940000088")
    project = create_project(seeded_client, headers)
    asset_id = str(uuid.uuid4())
    first = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers=headers,
        json={
            "layers": [
                {
                    "layer_id": "ai-background",
                    "kind": "AI_GENERATED",
                    "name": "机关熊猫底图",
                    "z_index": 0,
                    "asset_id": asset_id,
                    "aigc": True,
                }
            ],
            "canvas_width": 1024,
            "canvas_height": 1024,
            "change_summary": "保存生成底图",
        },
    )
    assert first.status_code == 201, first.text
    first_version = first.json()
    assert first_version["layers"][0]["scale"] == 1
    assert first_version["layers"][0]["crop_inset"] == 0

    provenance = seeded_client.put(
        f"/v1/creation-versions/{first_version['id']}/provenance-manifest",
        headers=headers,
        json={
            "human_contribution_summary": "本人完成构思与后续排版。",
            "ai_assistance_used": True,
            "ai_contribution_summary": "AI 生成画面底稿。",
            "aigc_label_declared": True,
            "unresolved_rights": False,
            "items": [
                {
                    "item_type": "HUMAN_CONTRIBUTION",
                    "contribution_type": "构思",
                    "description": "本人确定主题。",
                    "license_type": "ORIGINAL",
                },
                {
                    "item_type": "AI_CONTRIBUTION",
                    "contribution_type": "图片生成",
                    "description": "AI 生成底图。",
                    "license_type": "NOT_APPLICABLE",
                    "ai_provider": "test-provider",
                    "ai_model": "test-model",
                    "ai_tool_action": "IMAGE_GENERATION",
                    "prompt_summary": "机关熊猫",
                    "output_asset_id": asset_id,
                    "user_modified": False,
                },
            ],
        },
    )
    assert provenance.status_code == 200, provenance.text

    second = seeded_client.post(
        f"/v1/creation-projects/{project['id']}/versions",
        headers={**headers, "Idempotency-Key": "editor-revision-0001"},
        json={
            "parent_version_id": first_version["id"],
            "layers": [
                {
                    **first_version["layers"][0],
                    "offset_x": 36,
                    "offset_y": -18,
                    "scale": 1.25,
                    "rotation_degrees": 5,
                    "opacity": 0.9,
                    "crop_inset": 0.12,
                },
                {
                    "layer_id": "caption-1",
                    "kind": "TEXT",
                    "name": "标题",
                    "z_index": 1,
                    "text_content": "机巧江湖",
                    "font_size": 72,
                    "text_color": "#315B3A",
                },
            ],
            "canvas_width": 1024,
            "canvas_height": 1024,
            "preview_asset_id": asset_id,
            "change_summary": "调整构图并添加标题",
            "modification_reason": "在画布编辑器中完成排版",
        },
    )
    assert second.status_code == 201, second.text
    second_version = second.json()
    assert second_version["layers"][0]["offset_x"] == 36
    assert second_version["layers"][0]["scale"] == 1.25

    diff = seeded_client.get(
        f"/v1/creation-versions/{second_version['id']}/diff",
        headers=headers,
    )
    assert diff.status_code == 200, diff.text
    comparison = diff.json()
    assert [item["layer_id"] for item in comparison["added_layers"]] == ["caption-1"]
    assert comparison["modified_layers"][0]["layer_id"] == "ai-background"
    assert "crop_inset" in comparison["modified_layers"][0]["changed_fields"]

    inherited = seeded_client.get(
        f"/v1/creation-versions/{second_version['id']}/provenance-manifest",
        headers=headers,
    )
    assert inherited.status_code == 200, inherited.text
    inherited_manifest = inherited.json()
    assert inherited_manifest["status"] == "DRAFT"
    ai_item = next(
        item for item in inherited_manifest["items"]
        if item["item_type"] == "AI_CONTRIBUTION"
    )
    assert ai_item["user_modified"] is True
