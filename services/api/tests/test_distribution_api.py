from __future__ import annotations

import uuid

from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.domains.catalog.models import ManualContentStatus, ManualPage, ManualVolume
from app.domains.creations.models import (
    CreationMediaType,
    CreationProject,
    CreationStage,
    CreationVersion,
    CreationVisibility,
    LearningCard,
    LearningCardManual,
    LearningCardStatus,
    MaterialLicenseType,
    ProvenanceItem,
    ProvenanceItemType,
    ProvenanceManifest,
    ProvenanceStatus,
    Publication,
    PublicationStatus,
)
from app.domains.moderation.models import ModerationCase, ModerationCaseStatus


OTP = "123456"
INTERNAL_TOKEN = "dev-internal-worker-token-change-me-123456"


def register(client: TestClient, phone: str, age_band: str = "AGE_14_TO_17") -> tuple[dict, dict[str, str]]:
    assert client.post(
        "/v1/auth/verification-codes", json={"phone": phone, "purpose": "REGISTER"}
    ).status_code == 202
    response = client.post(
        "/v1/auth/register",
        json={
            "phone": phone,
            "verification_code": OTP,
            "password": "StrongPass!8",
            "age_band": age_band,
            "terms_version": "2026-09-r2",
            "privacy_version": "2026-08",
        },
    )
    assert response.status_code == 201, response.text
    body = response.json()
    return body, {"Authorization": f"Bearer {body['tokens']['access_token']}"}


def create_review_case(
    app: FastAPI,
    owner_id: str,
    visibility: CreationVisibility,
    classroom_id: str | None = None,
    *,
    title: str = "竹影机关图",
) -> tuple[str, str]:
    with app.state.session_factory() as db:
        project = CreationProject(
            owner_user_id=uuid.UUID(owner_id),
            title=title,
            media_type=CreationMediaType.ILLUSTRATION,
            status="ACTIVE",
            default_visibility=visibility,
            current_version_number=1,
            current_stage=CreationStage.SEAL,
        )
        db.add(project)
        db.flush()
        version = CreationVersion(
            project_id=project.id,
            version_number=1,
            created_by_user_id=uuid.UUID(owner_id),
            layer_manifest=[
                {"layer_id": "text-1", "kind": "TEXT", "name": "题字", "z_index": 0}
            ],
            layer_count=1,
            canvas_width=800,
            canvas_height=600,
            change_summary="完成画面",
        )
        db.add(version)
        db.flush()
        publication = Publication(
            project_id=project.id,
            creation_version_id=version.id,
            owner_user_id=uuid.UUID(owner_id),
            classroom_id=uuid.UUID(classroom_id) if classroom_id else None,
            status=PublicationStatus.PENDING_HUMAN_REVIEW,
            visibility=visibility,
            idempotency_key=f"test-{uuid.uuid4()}",
            request_fingerprint="a" * 64,
        )
        db.add(publication)
        db.flush()
        case = ModerationCase(
            publication_id=publication.id,
            owner_user_id=uuid.UUID(owner_id),
            status=ModerationCaseStatus.HUMAN_REVIEW,
            automatic_reason_codes=[],
            minimal_evidence={},
        )
        db.add(case)
        db.commit()
        return str(publication.id), str(case.id)


def publish(client: TestClient, case_id: str) -> dict:
    response = client.post(
        f"/v1/internal/moderation-cases/{case_id}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "PUBLISH",
            "risk_level": "LOW",
            "reviewer_reference": "distribution-test",
            "row_version": 1,
        },
    )
    assert response.status_code == 200, response.text
    return response.json()


def attach_public_learning_and_provenance(
    app: FastAPI, publication_id: str
) -> str:
    with app.state.session_factory() as db:
        publication = db.get(Publication, uuid.UUID(publication_id))
        assert publication is not None
        volume = ManualVolume(
            id=uuid.uuid4(),
            number=1,
            code="conference-card-volume",
            title="大会作品卡卷",
            core_domain="作品公开信息",
            art_style="水墨",
            start_page=1,
            end_page=5,
            is_listed=True,
        )
        db.add(volume)
        db.flush()
        page = ManualPage(
            id=uuid.uuid4(),
            volume_id=volume.id,
            page_no=1,
            style_no=1,
            slug="conference-card-manual",
            title="会动未必会思",
            core_logic="自动规则和机器学习并不相同。",
            life_hook="通过例子辨认能力边界。",
            interaction_evidence="设计一张能力边界牌。",
            content_version="test-v1",
            content_status=ManualContentStatus.READY,
            is_listed=True,
        )
        db.add_all(
            [
                page,
                LearningCard(
                    creation_version_id=publication.creation_version_id,
                    method_summary="先拆解结构，再比较不同传动方案。",
                    unresolved_questions=["怎样减少传动损耗？"],
                    questions_confirmed=True,
                    status=LearningCardStatus.LOCKED,
                    row_version=1,
                ),
                ProvenanceManifest(
                    creation_version_id=publication.creation_version_id,
                    human_contribution_summary="本人完成构思、结构设计和最终修改。",
                    ai_assistance_used=True,
                    ai_contribution_summary="AI 协助生成初步构图参考。",
                    aigc_label_declared=True,
                    unresolved_rights=False,
                    status=ProvenanceStatus.LOCKED,
                    row_version=1,
                ),
            ]
        )
        db.flush()
        db.add(
            LearningCardManual(
                creation_version_id=publication.creation_version_id,
                manual_page_id=page.id,
            )
        )
        db.add_all(
            [
                ProvenanceItem(
                    id=uuid.uuid4(),
                    creation_version_id=publication.creation_version_id,
                    item_type=ProvenanceItemType.HUMAN_CONTRIBUTION,
                    contribution_type="CONCEPT",
                    description="原创机关结构构思",
                    license_type=MaterialLicenseType.ORIGINAL,
                    user_modified=True,
                ),
                ProvenanceItem(
                    id=uuid.uuid4(),
                    creation_version_id=publication.creation_version_id,
                    item_type=ProvenanceItemType.AI_CONTRIBUTION,
                    contribution_type="REFERENCE",
                    description="构图参考",
                    license_type=MaterialLicenseType.NOT_APPLICABLE,
                    ai_provider="test-provider",
                    ai_model="test-model",
                    ai_tool_action="IMAGE_REFERENCE",
                    prompt_summary="机关结构构图参考",
                    user_modified=True,
                ),
            ]
        )
        db.commit()
        return str(page.id)


def test_classroom_and_supervisor_inbox_endpoints_are_removed(
    client: TestClient,
) -> None:
    _, headers = register(client, "13961000001", "ADULT")
    assert client.post(
        "/v1/classrooms", headers=headers, json={"name": "墨竹一班"}
    ).status_code == 404
    assert client.post(
        "/v1/classrooms:join",
        headers=headers,
        json={"join_code": "ABCDEFGH"},
    ).status_code == 404
    assert client.get("/v1/me/classrooms", headers=headers).status_code == 404
    assert client.get("/v1/me/publication-inbox", headers=headers).status_code == 404


def test_community_feed_contains_only_live_community_publications(
    client: TestClient, app: FastAPI
) -> None:
    author, author_headers = register(client, "13963000001")
    _, viewer_headers = register(client, "13963000002")
    publication_id, case_id = create_review_case(
        app, author["user"]["id"], CreationVisibility.COMMUNITY, title="知行流公开作品"
    )
    manual_page_id = attach_public_learning_and_provenance(app, publication_id)
    privacy = client.get("/v1/me/privacy-settings", headers=author_headers)
    assert privacy.status_code == 200
    assert client.patch(
        "/v1/me/privacy-settings",
        headers=author_headers,
        json={
            "learning_card_public": True,
            "row_version": privacy.json()["row_version"],
        },
    ).status_code == 200
    decision = publish(client, case_id)

    feed = client.get("/v1/community/feed?limit=1", headers=viewer_headers)
    assert feed.status_code == 200, feed.text
    assert feed.json()["items"][0]["publication_id"] == publication_id
    assert feed.json()["items"][0]["channel"] is None
    item = feed.json()["items"][0]
    assert item["version_number"] == 1
    assert item["learning_summary"] == "先拆解结构，再比较不同传动方案。"
    assert item["learning_card"]["unresolved_questions"] == ["怎样减少传动损耗？"]
    assert item["related_manuals"] == [
        {
            "manual_page_id": manual_page_id,
            "page_no": 1,
            "title": "会动未必会思",
        }
    ]
    assert item["provenance"] == {
        "human_contribution_summary": "本人完成构思、结构设计和最终修改。",
        "ai_assistance_used": True,
        "ai_contribution_summary": "AI 协助生成初步构图参考。",
        "aigc_label_declared": True,
        "source_count": 2,
    }

    assert client.post(
        f"/v1/publications/{publication_id}/withdraw",
        headers=author_headers,
        json={"row_version": decision["row_version"]},
    ).status_code == 200
    assert client.get("/v1/community/feed", headers=viewer_headers).json()["items"] == []
