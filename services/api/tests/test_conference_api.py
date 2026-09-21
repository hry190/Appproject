from __future__ import annotations

import uuid

from fastapi import FastAPI
from fastapi.testclient import TestClient
from sqlalchemy import select

from app.domains.catalog.models import ManualPage, ManualVolume
from app.domains.creations.models import (
    ConferenceCategory,
    CreationMediaType,
    CreationProject,
    CreationStage,
    CreationVersion,
    CreationVisibility,
    Publication,
    PublicationStatus,
)
from app.domains.moderation.models import (
    DomainAuditEvent,
    ModerationCase,
    ModerationCaseStatus,
)
from app.core.security import utcnow
from app.domains.conference.models import (
    ConferenceMatch,
    ConferenceMatchEndReason,
    ConferenceMatchEvaluation,
    ConferenceMatchEvaluationKind,
    ConferenceMatchReflection,
    ConferenceMatchStatus,
)
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.conference.judge import DevelopmentConferenceJudge
from app.domains.conference.service import ConferenceService
from app.domains.distribution.service import DistributionService


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
            "terms_version": "2026-08",
            "privacy_version": "2026-08",
        },
    )
    assert response.status_code == 201, response.text
    body = response.json()
    return body, {"Authorization": f"Bearer {body['tokens']['access_token']}"}


def create_published_community_work(
    app: FastAPI,
    owner_id: str,
    *,
    title: str,
    category: ConferenceCategory = ConferenceCategory.ART,
) -> tuple[str, str]:
    with app.state.session_factory() as db:
        project = CreationProject(
            owner_user_id=uuid.UUID(owner_id),
            title=title,
            media_type=CreationMediaType.ILLUSTRATION,
            status="ACTIVE",
            default_visibility=CreationVisibility.COMMUNITY,
            current_version_number=1,
            current_stage=CreationStage.SEAL,
        )
        db.add(project)
        db.flush()
        version = CreationVersion(
            project_id=project.id,
            version_number=1,
            created_by_user_id=uuid.UUID(owner_id),
            layer_manifest=[{"layer_id": "text-1", "kind": "TEXT", "name": "题字", "z_index": 0}],
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
            status=PublicationStatus.PENDING_HUMAN_REVIEW,
            visibility=CreationVisibility.COMMUNITY,
            conference_category=category,
            idempotency_key=f"conference-{uuid.uuid4()}",
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
            "reviewer_reference": "conference-test",
            "row_version": 1,
        },
    )
    assert response.status_code == 200, response.text
    return response.json()


def create_follow_up_version(app: FastAPI, publication_id: str, owner_id: str) -> str:
    with app.state.session_factory() as db:
        publication = db.get(Publication, uuid.UUID(publication_id))
        assert publication is not None
        parent = db.get(CreationVersion, publication.creation_version_id)
        assert parent is not None
        project = db.get(CreationProject, publication.project_id)
        assert project is not None
        version = CreationVersion(
            project_id=project.id,
            version_number=parent.version_number + 1,
            parent_version_id=parent.id,
            created_by_user_id=uuid.UUID(owner_id),
            layer_manifest=parent.layer_manifest,
            layer_count=parent.layer_count,
            canvas_width=parent.canvas_width,
            canvas_height=parent.canvas_height,
            change_summary="根据大会建议缩短传动轴",
            modification_reason="采纳结构化建议",
        )
        db.add(version)
        db.flush()
        project.current_version_number = version.version_number
        db.commit()
        return str(version.id)


def create_manual_page(app: FastAPI) -> str:
    with app.state.session_factory() as db:
        volume = ManualVolume(
            number=1,
            code="conference-test-volume",
            title="大会测试卷",
            core_domain="测试",
            art_style="测试",
            start_page=1,
            end_page=1,
            is_listed=True,
        )
        db.add(volume)
        db.flush()
        page = ManualPage(
            volume_id=volume.id,
            page_no=1,
            style_no=1,
            slug="conference-test-page",
            title="同门机关",
            core_logic="同一知识点",
            life_hook="测试",
            interaction_evidence="测试",
            content_version="1.0",
            content_status="READY",
            is_listed=True,
        )
        db.add(page)
        db.commit()
        return str(page.id)


def test_conference_feed_is_hard_capped_at_five(client: TestClient, app: FastAPI) -> None:
    author, author_headers = register(client, "13981000001")
    _, viewer_headers = register(client, "13981000002")
    for index in range(6):
        _, case_id = create_published_community_work(
            app, author["user"]["id"], title=f"大会作品 {index}"
        )
        publish(client, case_id)

    feed = client.get("/v1/conference/feed", headers=viewer_headers)
    assert feed.status_code == 200, feed.text
    assert len(feed.json()["items"]) == 5
    assert feed.json()["next_cursor"] is not None
    detail = client.get(
        f"/v1/conference/publications/{feed.json()['items'][0]['publication_id']}",
        headers=viewer_headers,
    )
    assert detail.status_code == 200, detail.text
    assert detail.json()["project_id"] == feed.json()["items"][0]["project_id"]
    assert detail.json()["is_owner"] is False
    assert detail.json()["conference_category"] == "ART"
    assert detail.json()["is_collected"] is False
    assert detail.json()["review_count"] == 0
    assert client.get("/v1/conference/feed", headers=author_headers).json()["items"][0]["is_owner"] is True
    assert len(
        client.get("/v1/conference/feed?limit=99", headers=viewer_headers).json()["items"]
    ) == 5

    mine = client.get("/v1/conference/me/works", headers=author_headers)
    assert mine.status_code == 200, mine.text
    assert len(mine.json()["items"]) == 6
    assert all(item["is_owner"] for item in mine.json()["items"])


def test_conference_feed_filters_author_selected_category(
    client: TestClient, app: FastAPI
) -> None:
    author, _ = register(client, "13981500001")
    _, viewer_headers = register(client, "13981500002")
    _, art_case = create_published_community_work(
        app,
        author["user"]["id"],
        title="光影插画",
        category=ConferenceCategory.ART,
    )
    _, science_case = create_published_community_work(
        app,
        author["user"]["id"],
        title="机关结构实验",
        category=ConferenceCategory.SCIENCE,
    )
    publish(client, art_case)
    publish(client, science_case)

    filtered = client.get("/v1/conference/feed?category=SCIENCE", headers=viewer_headers)
    assert filtered.status_code == 200, filtered.text
    assert [item["title"] for item in filtered.json()["items"]] == ["机关结构实验"]


def test_conference_review_collection_and_derivative_withdrawal(
    client: TestClient, app: FastAPI
) -> None:
    author, author_headers = register(client, "13982000001")
    reviewer, reviewer_headers = register(client, "13982000002")
    _, stranger_headers = register(client, "13982000003")
    publication_id, case_id = create_published_community_work(
        app, author["user"]["id"], title="可传习的机关图"
    )
    moderation = publish(client, case_id)

    review = client.post(
        f"/v1/conference/publications/{publication_id}/reviews",
        headers=reviewer_headers,
        json={"template": "OBSERVATION", "content": "我看到了齿轮的传动关系。"},
    )
    assert review.status_code == 201, review.text
    assert review.json()["status"] == "PENDING"
    assert client.post(
        f"/v1/conference/reviews/{review.json()['id']}/decision",
        headers=stranger_headers,
        json={"action": "ACCEPT", "row_version": 1},
    ).status_code == 403
    handled = client.post(
        f"/v1/conference/reviews/{review.json()['id']}/decision",
        headers=author_headers,
        json={"action": "REPLY", "reply": "你观察得很准确。", "row_version": 1},
    )
    assert handled.status_code == 200, handled.text
    assert handled.json()["status"] == "REPLIED"

    saved = client.put(
        f"/v1/conference/collections/{publication_id}", headers=reviewer_headers
    )
    assert saved.status_code == 200, saved.text
    liked = client.put(
        f"/v1/conference/likes/{publication_id}", headers=reviewer_headers
    )
    assert liked.status_code == 200, liked.text
    assert liked.json()["publication_id"] == publication_id
    assert client.put(
        f"/v1/conference/likes/{publication_id}", headers=reviewer_headers
    ).status_code == 200
    assert client.get("/v1/conference/me/collections", headers=author_headers).json()["items"] == []
    assert len(client.get("/v1/conference/me/collections", headers=reviewer_headers).json()["items"]) == 1

    live_detail = client.get(
        f"/v1/conference/publications/{publication_id}", headers=reviewer_headers
    )
    assert live_detail.status_code == 200, live_detail.text
    assert live_detail.json()["is_liked"] is True
    assert live_detail.json()["like_count"] == 1
    assert live_detail.json()["is_collected"] is True
    assert live_detail.json()["collection_count"] == 1
    assert live_detail.json()["review_count"] == 1

    assert client.delete(
        f"/v1/conference/likes/{publication_id}", headers=reviewer_headers
    ).status_code == 204
    after_unlike = client.get(
        f"/v1/conference/publications/{publication_id}", headers=reviewer_headers
    ).json()
    assert after_unlike["is_liked"] is False
    assert after_unlike["like_count"] == 0

    requested = client.post(
        "/v1/conference/derivative-requests",
        headers=reviewer_headers,
        json={"source_publication_id": publication_id, "requested_use": "用于重做机关结构练习"},
    )
    assert requested.status_code == 201, requested.text
    assert "preview_url" not in requested.json()
    approved = client.post(
        f"/v1/conference/derivative-requests/{requested.json()['id']}/decision",
        headers=author_headers,
        json={"decision": "APPROVE"},
    )
    assert approved.status_code == 200, approved.text
    authorization = approved.json()["authorization"]
    assert authorization["status"] == "ACTIVE"
    assert set(authorization) == {
        "id", "request_id", "source_publication_id", "source_creation_version_id",
        "authorization_version", "status", "granted_at", "revoked_at",
    }
    derivative_project = client.post(
        "/v1/creation-projects",
        headers=reviewer_headers,
        json={
            "title": "齿轮机关改造稿",
            "media_type": "ILLUSTRATION",
            "derivative_authorization_id": authorization["id"],
        },
    )
    assert derivative_project.status_code == 201, derivative_project.text
    assert derivative_project.json()["derivative_authorization_id"] == authorization["id"]

    withdrawn = client.post(
        f"/v1/publications/{publication_id}/withdraw",
        headers=author_headers,
        json={"row_version": moderation["row_version"]},
    )
    assert withdrawn.status_code == 200, withdrawn.text
    requester_records = client.get(
        "/v1/conference/me/derivative-requests", headers=reviewer_headers
    )
    assert requester_records.status_code == 200
    assert requester_records.json()["items"][0]["authorization"]["status"] == "REVOKED"
    blocked_submission = client.post(
        f"/v1/creation-projects/{derivative_project.json()['id']}/submissions",
        headers={**reviewer_headers, "Idempotency-Key": "revoked-derivative-submit"},
        json={"creation_version_id": str(uuid.uuid4())},
    )
    assert blocked_submission.status_code == 409
    assert blocked_submission.json()["error"]["code"] == "DERIVATIVE_AUTHORIZATION_INACTIVE"
    inactive = client.post(
        "/v1/creation-projects",
        headers=reviewer_headers,
        json={
            "title": "失效授权改造稿",
            "media_type": "ILLUSTRATION",
            "derivative_authorization_id": authorization["id"],
        },
    )
    assert inactive.status_code == 409
    assert inactive.json()["error"]["code"] == "DERIVATIVE_AUTHORIZATION_INACTIVE"


def test_conference_review_privacy_screen_report_and_internal_removal(
    client: TestClient, app: FastAPI
) -> None:
    author, author_headers = register(client, "13982500001")
    reviewer, reviewer_headers = register(client, "13982500002")
    publication_id, case_id = create_published_community_work(
        app, author["user"]["id"], title="可安全评招的机关图"
    )
    publish(client, case_id)

    unsafe = client.post(
        f"/v1/conference/publications/{publication_id}/reviews",
        headers=reviewer_headers,
        json={
            "template": "QUESTION",
            "content": "请联系我 13800138000 继续讨论",
        },
    )
    assert unsafe.status_code == 422
    assert unsafe.json()["error"]["code"] == "INTERACTION_TEXT_PRIVACY_RISK"

    created = client.post(
        f"/v1/conference/publications/{publication_id}/reviews",
        headers=reviewer_headers,
        json={"template": "SUGGESTION", "content": "可以尝试缩短传动轴。"},
    )
    assert created.status_code == 201, created.text
    assert created.json()["moderation_status"] == "VISIBLE"

    report = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/reports",
        headers=author_headers,
        json={"reason": "INAPPROPRIATE_CONTENT", "details": "需要人工复核语境"},
    )
    assert report.status_code == 201, report.text
    assert report.json()["status"] == "PENDING"
    duplicate = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/reports",
        headers=author_headers,
        json={"reason": "OTHER"},
    )
    assert duplicate.status_code == 201
    assert duplicate.json()["id"] == report.json()["id"]

    pending_reports = client.get(
        "/v1/internal/conference/review-reports",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        params={"report_status": "PENDING"},
    )
    assert pending_reports.status_code == 200, pending_reports.text
    assert len(pending_reports.json()["items"]) == 1
    pending_report = pending_reports.json()["items"][0]
    assert pending_report["id"] == report.json()["id"]
    assert pending_report["publication_id"] == publication_id
    assert pending_report["review_author_user_id"] == reviewer["user"]["id"]
    assert pending_report["review_template"] == "SUGGESTION"
    assert pending_report["review_content"] == "可以尝试缩短传动轴。"
    assert pending_report["report_details"] == "需要人工复核语境"

    decided = client.post(
        f"/v1/internal/conference/review-reports/{report.json()['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "REMOVE",
            "reviewer_reference": "conference-reviewer",
            "resolution_summary": "评语不符合大会交流规范",
            "row_version": 1,
        },
    )
    assert decided.status_code == 200, decided.text
    assert decided.json()["status"] == "RESOLVED"
    assert decided.json()["row_version"] == 2
    visible = client.get(
        f"/v1/conference/publications/{publication_id}/reviews",
        headers=author_headers,
    )
    assert visible.status_code == 200
    assert visible.json()["items"] == []

    with app.state.session_factory() as db:
        actions = set(
            db.scalars(
                select(DomainAuditEvent.action).where(
                    DomainAuditEvent.action.like("CONFERENCE_%")
                )
            ).all()
        )
    assert {
        "CONFERENCE_REVIEW_CREATED",
        "CONFERENCE_REVIEW_REPORTED",
        "CONFERENCE_REVIEW_REPORT_DECIDED",
    }.issubset(actions)


def test_conference_review_adoption_requires_a_new_version_and_is_exported(
    client: TestClient, app: FastAPI
) -> None:
    author, author_headers = register(client, "13982700001")
    reviewer, reviewer_headers = register(client, "13982700002")
    publication_id, case_id = create_published_community_work(
        app, author["user"]["id"], title="可持续改进的机关图"
    )
    publish(client, case_id)
    created = client.post(
        f"/v1/conference/publications/{publication_id}/reviews",
        headers=reviewer_headers,
        json={"template": "SUGGESTION", "content": "建议缩短传动轴以减少晃动。"},
    )
    assert created.status_code == 201, created.text
    accepted = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/decision",
        headers=author_headers,
        json={"action": "ACCEPT", "row_version": 1},
    )
    assert accepted.status_code == 200, accepted.text
    assert accepted.json()["row_version"] == 2

    with app.state.session_factory() as db:
        publication = db.get(Publication, uuid.UUID(publication_id))
        assert publication is not None
        published_version_id = str(publication.creation_version_id)
    not_new = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/adoption",
        headers=author_headers,
        json={
            "creation_version_id": published_version_id,
            "summary": "仍然指向参会旧版本",
            "row_version": 2,
        },
    )
    assert not_new.status_code == 409
    assert not_new.json()["error"]["code"] == "INVALID_REVIEW_ADOPTION_VERSION"

    follow_up_version_id = create_follow_up_version(
        app, publication_id, author["user"]["id"]
    )
    unsafe = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/adoption",
        headers=author_headers,
        json={
            "creation_version_id": follow_up_version_id,
            "summary": "详情请联系 13800138000",
            "row_version": 2,
        },
    )
    assert unsafe.status_code == 422
    linked = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/adoption",
        headers=author_headers,
        json={
            "creation_version_id": follow_up_version_id,
            "summary": "缩短传动轴，并重新验证了稳定性。",
            "row_version": 2,
        },
    )
    assert linked.status_code == 200, linked.text
    assert linked.json()["adopted_in_creation_version_id"] == follow_up_version_id
    assert linked.json()["adoption_summary"] == "缩短传动轴，并重新验证了稳定性。"
    assert linked.json()["adopted_at"] is not None
    assert linked.json()["row_version"] == 3
    replay = client.post(
        f"/v1/conference/reviews/{created.json()['id']}/adoption",
        headers=author_headers,
        json={
            "creation_version_id": follow_up_version_id,
            "summary": "缩短传动轴，并重新验证了稳定性。",
            "row_version": 2,
        },
    )
    assert replay.status_code == 200
    assert replay.json()["row_version"] == 3
    letters = client.get("/v1/conference/letters", headers=reviewer_headers)
    adoption_letter = next(
        item
        for item in letters.json()["items"]
        if item["title"] == "你的建议已形成新版本"
    )
    assert adoption_letter["navigation_target"] == "CONFERENCE_WORK"
    assert adoption_letter["navigation_id"] == publication_id
    author_export = client.get("/v1/account/export", headers=author_headers)
    reviewer_export = client.get("/v1/account/export", headers=reviewer_headers)
    assert author_export.status_code == reviewer_export.status_code == 200
    received = author_export.json()["conference_activity"]["reviews_received"]
    authored = reviewer_export.json()["conference_activity"]["reviews_authored"]
    assert received[0]["adopted_in_creation_version_id"] == follow_up_version_id
    assert authored[0]["adoption_summary"] == "缩短传动轴，并重新验证了稳定性。"
    assert "reviewer_user_id" not in author_export.text


def test_conference_match_is_anonymous_and_scoped_to_age_and_manual(
    client: TestClient, app: FastAPI
) -> None:
    first, first_headers = register(client, "13983000001")
    _, second_headers = register(client, "13983000002")
    _, adult_headers = register(client, "13983000003", "ADULT")
    manual_page_id = create_manual_page(app)

    waiting = client.post(
        "/v1/conference/match-queue",
        headers=first_headers,
        json={"manual_page_id": manual_page_id},
    )
    assert waiting.status_code == 200, waiting.text
    assert waiting.json()["status"] == "WAITING"
    assert waiting.json()["phase"] == "FILTERING"
    assert waiting.json()["pool_size"] == 1
    assert waiting.json()["wait_seconds"] >= 0
    assert waiting.json()["manual_title"]
    assert waiting.json()["manual_page_no"] == 1
    assert waiting.json()["anonymous_opponent"] is None
    assert waiting.json()["expires_at"] is not None
    adult_waiting = client.post(
        "/v1/conference/match-queue",
        headers=adult_headers,
        json={"manual_page_id": manual_page_id},
    )
    assert adult_waiting.json()["status"] == "WAITING"

    matched = client.post(
        "/v1/conference/match-queue",
        headers=second_headers,
        json={"manual_page_id": manual_page_id},
    )
    assert matched.status_code == 200, matched.text
    assert matched.json()["status"] == "MATCHED"
    assert matched.json()["phase"] == "LOCKED"
    assert matched.json()["match_id"]
    assert matched.json()["match_code"].startswith("M-")
    assert matched.json()["anonymous_opponent"] == {
        "alias": "竹影同门",
        "avatar_key": "PANDA_BAMBOO",
        "age_band_label": "同龄",
        "stage_label": "同阶段",
    }
    assert "nickname" not in matched.json()
    assert "user_id" not in matched.json()["anonymous_opponent"]

    report = client.post(
        f"/v1/conference/matches/{matched.json()['match_id']}/reports",
        headers=first_headers,
        json={"reason": "SAFETY_CONCERN", "details": "测试举报"},
    )
    assert report.status_code == 201, report.text
    assert report.json()["status"] == "PENDING"
    assert report.json()["row_version"] == 1
    assert client.get("/v1/conference/match-queue", headers=second_headers).json()["status"] == "IDLE"
    pending = client.get(
        "/v1/internal/conference/match-reports",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        params={"report_status": "PENDING"},
    )
    assert pending.status_code == 200, pending.text
    pending_item = pending.json()["items"][0]
    assert pending_item["id"] == report.json()["id"]
    assert pending_item["reporter_user_id"] == first["user"]["id"]
    assert pending_item["report_details"] == "测试举报"
    decided = client.post(
        f"/v1/internal/conference/match-reports/{report.json()['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "CONFIRM",
            "reviewer_reference": "conference-safety-reviewer",
            "resolution_summary": "已核查并完成安全处置",
            "row_version": 1,
        },
    )
    assert decided.status_code == 200, decided.text
    assert decided.json()["status"] == "RESOLVED"
    assert decided.json()["resolution_summary"] == "已核查并完成安全处置"
    assert decided.json()["row_version"] == 2
    repeated = client.post(
        f"/v1/internal/conference/match-reports/{report.json()['id']}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": "DISMISS",
            "reviewer_reference": "conference-safety-reviewer",
            "resolution_summary": "重复处理",
            "row_version": 2,
        },
    )
    assert repeated.status_code == 409
    assert repeated.json()["error"]["code"] == "MATCH_REPORT_ALREADY_DECIDED"


def test_conference_match_records_are_private_filterable_and_paginated(
    client: TestClient, app: FastAPI
) -> None:
    first, first_headers = register(client, "13983500001")
    second, _ = register(client, "13983500002")
    _, outsider_headers = register(client, "13983500003")
    manual_page_id = uuid.UUID(create_manual_page(app))
    first_id = uuid.UUID(first["user"]["id"])
    second_id = uuid.UUID(second["user"]["id"])
    now = utcnow()

    with app.state.session_factory() as db:
        matches = [
            ConferenceMatch(
                id=uuid.uuid4(),
                manual_page_id=manual_page_id,
                age_band="AGE_14_TO_17",
                participant_a_user_id=first_id,
                participant_b_user_id=second_id,
                status=ConferenceMatchStatus.ENDED,
                end_reason=ConferenceMatchEndReason.COMPLETED,
                winner_user_id=first_id,
                question_count=3,
                matched_at=now,
                ended_at=now,
            ),
            ConferenceMatch(
                id=uuid.uuid4(),
                manual_page_id=manual_page_id,
                age_band="AGE_14_TO_17",
                participant_a_user_id=first_id,
                participant_b_user_id=second_id,
                status=ConferenceMatchStatus.ENDED,
                end_reason=ConferenceMatchEndReason.COMPLETED,
                winner_user_id=None,
                question_count=3,
                matched_at=now,
                ended_at=now,
            ),
            ConferenceMatch(
                id=uuid.uuid4(),
                manual_page_id=manual_page_id,
                age_band="AGE_14_TO_17",
                participant_a_user_id=first_id,
                participant_b_user_id=second_id,
                status=ConferenceMatchStatus.ENDED,
                end_reason=ConferenceMatchEndReason.COMPLETED,
                winner_user_id=second_id,
                question_count=3,
                matched_at=now,
                ended_at=now,
            ),
        ]
        db.add_all(matches)
        db.flush()
        scores = ((90.0, 80.0), (88.0, 88.0), (70.0, 95.0))
        for match, (first_score, second_score) in zip(matches, scores, strict=True):
            for subject_id, score in ((first_id, first_score), (second_id, second_score)):
                db.add(
                    ConferenceMatchEvaluation(
                        id=uuid.uuid4(),
                        match_id=match.id,
                        subject_user_id=subject_id,
                        evaluator_user_id=None,
                        kind=ConferenceMatchEvaluationKind.AI,
                        score=score,
                        dimension_scores={"理解": score},
                        summary="大会匿名评审已经完成。",
                        strengths=["理由清楚"],
                        improvements=["补充边界"],
                        evaluator_reference="records-test",
                    )
                )
        db.add(
            ConferenceMatchReflection(
                id=uuid.uuid4(),
                match_id=matches[0].id,
                user_id=first_id,
                learned="学会了复盘。",
                next_improvement="下次补充依据。",
            )
        )
        db.commit()

    first_page = client.get(
        "/v1/conference/matches",
        headers=first_headers,
        params={"page": 1, "limit": 2},
    )
    assert first_page.status_code == 200, first_page.text
    payload = first_page.json()
    assert len(payload["items"]) == 2
    assert payload["page"] == 1
    assert payload["limit"] == 2
    assert payload["total"] == 3
    assert payload["has_more"] is True
    assert payload["summary"] == {
        "total": 3,
        "wins": 1,
        "ties": 1,
        "pending_reflections": 2,
    }
    assert all(item["anonymous_opponent"]["alias"] == "竹影同门" for item in payload["items"])
    assert "participant_a_user_id" not in first_page.text
    assert first["user"]["id"] not in first_page.text
    assert second["user"]["id"] not in first_page.text

    second_page = client.get(
        "/v1/conference/matches",
        headers=first_headers,
        params={"page": 2, "limit": 2},
    ).json()
    assert len(second_page["items"]) == 1
    assert second_page["has_more"] is False

    wins = client.get(
        "/v1/conference/matches",
        headers=first_headers,
        params={"outcome": "WIN"},
    ).json()
    assert [item["outcome"] for item in wins["items"]] == ["WIN"]
    completed_reflections = client.get(
        "/v1/conference/matches",
        headers=first_headers,
        params={"reflection_status": "COMPLETED"},
    ).json()
    assert len(completed_reflections["items"]) == 1
    assert completed_reflections["items"][0]["reflection_status"] == "COMPLETED"
    pending_reflections = client.get(
        "/v1/conference/matches",
        headers=first_headers,
        params={"reflection_status": "PENDING"},
    ).json()
    assert len(pending_reflections["items"]) == 2
    outsider = client.get("/v1/conference/matches", headers=outsider_headers).json()
    assert outsider["items"] == []
    assert outsider["summary"]["total"] == 0


def test_conference_match_learning_loop_evaluations_reflection_and_letters(
    client: TestClient, app: FastAPI
) -> None:
    first, first_headers = register(client, "13984000001")
    second, second_headers = register(client, "13984000002")
    manual_page_id = create_manual_page(app)

    waiting = client.post(
        "/v1/conference/match-queue",
        headers=first_headers,
        json={"manual_page_id": manual_page_id},
    )
    assert waiting.status_code == 200
    matched = client.post(
        "/v1/conference/match-queue",
        headers=second_headers,
        json={"manual_page_id": manual_page_id},
    )
    assert matched.status_code == 200, matched.text
    match_id = matched.json()["match_id"]

    detail = client.get(f"/v1/conference/matches/{match_id}", headers=first_headers)
    assert detail.status_code == 200, detail.text
    questions = detail.json()["questions"]
    assert [item["position"] for item in questions] == [1, 2, 3]
    assert detail.json()["my_progress"] == {
        "answered": 0,
        "total": 3,
        "complete": False,
    }
    assert "participant" not in detail.text

    unsafe = client.post(
        f"/v1/conference/matches/{match_id}/answers",
        headers=first_headers,
        json={
            "question_id": questions[0]["id"],
            "answer": "联系我 13800138000",
            "reason": "继续讨论",
        },
    )
    assert unsafe.status_code == 422
    assert unsafe.json()["error"]["code"] == "INTERACTION_TEXT_PRIVACY_RISK"

    for question in questions:
        answered = client.post(
            f"/v1/conference/matches/{match_id}/answers",
            headers=first_headers,
            json={
                "question_id": question["id"],
                "answer": f"第一位少侠对第 {question['position']} 题的方案",
                "reason": "依据秘籍核心逻辑逐步判断",
            },
        )
        assert answered.status_code == 201, answered.text
    opponent_view = client.get(
        f"/v1/conference/matches/{match_id}", headers=second_headers
    )
    assert opponent_view.json()["opponent_progress"]["answered"] == 3
    assert opponent_view.json()["my_answers"] == []

    for question in questions:
        answered = client.post(
            f"/v1/conference/matches/{match_id}/answers",
            headers=second_headers,
            json={
                "question_id": question["id"],
                "answer": f"第二位少侠对第 {question['position']} 题的方案",
                "reason": "结合公案和迁移任务说明理由",
            },
        )
        assert answered.status_code == 201, answered.text
    assert answered.json()["match_status"] == "AWAITING_JUDGMENT"

    with app.state.session_factory() as db:
        judgment_event = db.scalar(
            select(OutboxEvent).where(
                OutboxEvent.aggregate_id == uuid.UUID(match_id),
                OutboxEvent.event_type == "CONFERENCE_MATCH_JUDGMENT_REQUESTED",
            )
        )
        assert judgment_event is not None
        assert judgment_event.status == OutboxStatus.PENDING

    pending = client.get(
        "/v1/internal/conference/matches/pending-judgment",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
    )
    assert pending.status_code == 200, pending.text
    candidate = pending.json()["items"][0]
    assert candidate["match_id"] == match_id
    assert len(candidate["questions"]) == 3
    assert len(candidate["rubrics"]) == 3
    assert len(candidate["answers"]) == 6

    judgment = client.post(
        f"/v1/internal/conference/matches/{match_id}/judgment",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "evaluator_reference": "conference-ai-judge-v1",
            "participants": [
                {
                    "user_id": first["user"]["id"],
                    "score": 91,
                    "dimension_scores": {"理解": 92, "迁移": 90},
                    "summary": "核心概念清楚，理由完整。",
                    "strengths": ["能够连接核心逻辑与实际方案"],
                    "improvements": ["可以进一步补充风险边界"],
                },
                {
                    "user_id": second["user"]["id"],
                    "score": 84,
                    "dimension_scores": {"理解": 85, "迁移": 83},
                    "summary": "回答完整，迁移方案可执行。",
                    "strengths": ["方案步骤清晰"],
                    "improvements": ["判断依据还可以更具体"],
                },
            ],
        },
    )
    assert judgment.status_code == 200, judgment.text
    assert judgment.json()["status"] == "ENDED"
    assert judgment.json()["winner_user_id"] == first["user"]["id"]
    with app.state.session_factory() as db:
        completed_event = db.scalar(
            select(OutboxEvent).where(
                OutboxEvent.aggregate_id == uuid.UUID(match_id),
                OutboxEvent.event_type == "CONFERENCE_MATCH_JUDGMENT_REQUESTED",
            )
        )
        assert completed_event is not None
        assert completed_event.status == OutboxStatus.COMPLETED

    repeated_last_answer = client.post(
        f"/v1/conference/matches/{match_id}/answers",
        headers=second_headers,
        json={
            "question_id": questions[-1]["id"],
            "answer": "第二位少侠对第 3 题的方案",
            "reason": "结合公案和迁移任务说明理由",
        },
    )
    assert repeated_last_answer.status_code == 201
    assert repeated_last_answer.json()["match_status"] == "ENDED"

    first_result = client.get(
        f"/v1/conference/matches/{match_id}/result", headers=first_headers
    )
    second_result = client.get(
        f"/v1/conference/matches/{match_id}/result", headers=second_headers
    )
    assert first_result.json()["outcome"] == "WIN"
    assert second_result.json()["outcome"] == "LOSE"
    assert [item["score"] for item in first_result.json()["participants"]] == [91, 84]

    self_evaluation = client.post(
        f"/v1/conference/matches/{match_id}/evaluations",
        headers=first_headers,
        json={
            "kind": "SELF",
            "score": 88,
            "dimension_scores": {"投入": 90},
            "summary": "我能讲清核心概念，但风险分析还不够。",
            "strengths": ["理由完整"],
            "improvements": ["补充边界条件"],
        },
    )
    assert self_evaluation.status_code == 201, self_evaluation.text
    peer_evaluation = client.post(
        f"/v1/conference/matches/{match_id}/evaluations",
        headers=first_headers,
        json={
            "kind": "PEER",
            "score": 86,
            "summary": "对方的方案清楚，也能结合公案。",
            "strengths": ["表达有条理"],
            "improvements": ["可以加入更多验证步骤"],
        },
    )
    assert peer_evaluation.status_code == 201, peer_evaluation.text
    teacher_evaluation = client.post(
        f"/v1/internal/conference/matches/{match_id}/teacher-evaluations",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "subject_user_id": first["user"]["id"],
            "evaluator_reference": "teacher-classroom-01",
            "score": 93,
            "dimension_scores": {"论证": 93},
            "summary": "能够从概念走向实际设计。",
            "strengths": ["迁移能力突出"],
            "improvements": ["继续关注公平与安全"],
        },
    )
    assert teacher_evaluation.status_code == 201, teacher_evaluation.text

    reflection = client.post(
        f"/v1/conference/matches/{match_id}/reflections",
        headers=first_headers,
        json={
            "learned": "我学会了把核心概念用于真实场景。",
            "next_improvement": "下一次先列出风险再作答。",
        },
    )
    assert reflection.status_code == 201, reflection.text
    refreshed = client.get(
        f"/v1/conference/matches/{match_id}/result", headers=first_headers
    ).json()
    assert refreshed["my_reflection"]["id"] == reflection.json()["id"]
    assert {item["kind"] for item in refreshed["participants"][0]["evaluations"]} == {
        "AI",
        "SELF",
        "TEACHER",
    }
    assert {item["kind"] for item in refreshed["participants"][1]["evaluations"]} == {
        "AI",
        "PEER",
    }

    letters = client.get("/v1/conference/letters", headers=first_headers)
    assert letters.status_code == 200, letters.text
    assert letters.json()["unread_count"] >= 3
    result_letter = next(
        item
        for item in letters.json()["items"]
        if item["title"] == "切磋评审结果已出"
    )
    assert result_letter["navigation_target"] == "CONFERENCE_MATCH"
    assert result_letter["navigation_id"] == match_id
    read = client.put(
        f"/v1/conference/letters/{result_letter['id']}/read",
        headers=first_headers,
    )
    assert read.status_code == 200, read.text
    assert read.json()["is_read"] is True
    assert read.json()["navigation_id"] == match_id


def test_queued_conference_match_is_processed_by_configured_judge(
    client: TestClient, app: FastAPI
) -> None:
    _, first_headers = register(client, "13985000001")
    _, second_headers = register(client, "13985000002")
    manual_page_id = create_manual_page(app)
    assert client.post(
        "/v1/conference/match-queue",
        headers=first_headers,
        json={"manual_page_id": manual_page_id},
    ).status_code == 200
    matched = client.post(
        "/v1/conference/match-queue",
        headers=second_headers,
        json={"manual_page_id": manual_page_id},
    )
    match_id = matched.json()["match_id"]
    questions = client.get(
        f"/v1/conference/matches/{match_id}", headers=first_headers
    ).json()["questions"]
    for headers in (first_headers, second_headers):
        for question in questions:
            response = client.post(
                f"/v1/conference/matches/{match_id}/answers",
                headers=headers,
                json={
                    "question_id": question["id"],
                    "answer": "我会解释概念并给出可以验证的具体实施方案。",
                    "reason": "理由会连接秘籍、公案、现实应用和安全边界。",
                },
            )
            assert response.status_code == 201, response.text

    with app.state.session_factory() as db:
        processed = ConferenceService(
            db=db,
            distribution=DistributionService(
                db=db,
                settings=app.state.settings,
                store=app.state.object_store,
            ),
            request_id="test:conference-judge-worker",
        ).process_match_judgment(
            uuid.UUID(match_id), DevelopmentConferenceJudge()
        )
        assert processed.status.value == "ENDED"

    result = client.get(
        f"/v1/conference/matches/{match_id}/result", headers=first_headers
    )
    assert result.status_code == 200, result.text
    assert result.json()["outcome"] == "TIE"
    assert all(
        participant["evaluations"][0]["evaluator_reference"]
        == "development:deterministic-learning-rubric-v1"
        for participant in result.json()["participants"]
    )
    with app.state.session_factory() as db:
        event = db.scalar(
            select(OutboxEvent).where(
                OutboxEvent.aggregate_id == uuid.UUID(match_id),
                OutboxEvent.event_type == "CONFERENCE_MATCH_JUDGMENT_REQUESTED",
            )
        )
        assert event is not None
        assert event.status == OutboxStatus.COMPLETED
