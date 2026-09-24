from __future__ import annotations

import uuid

import pytest
from fastapi.testclient import TestClient

from app.domains.catalog.seed import stable_id
from app.domains.creations.models import (
    CreationVisibility,
    Publication,
    PublicationStatus,
)
from app.domains.learning.contracts import (
    EvidenceCategory,
    EvidenceValidationStatus,
    ManualProgressState,
)
from app.domains.learning.models import LearningEvidence, ManualProgress
from app.domains.moderation.models import ModerationCase, ModerationCaseStatus
from test_learning_api import bearer, register, seeded_client
from test_creations_api import (
    advance_existing_project_to_seal,
    create_project,
    create_text_version,
)
from test_conference_api import (
    INTERNAL_TOKEN,
    create_follow_up_version,
    create_published_community_work,
    publish,
)


def stats(client: TestClient, headers: dict) -> dict:
    response = client.get("/v1/me/learning-stats", headers=headers)
    assert response.status_code == 200, response.text
    return response.json()["evidence"]


def learn(client: TestClient, user_id: str, *page_numbers: int) -> list[str]:
    ids = [stable_id("manual_page", str(number)) for number in page_numbers]
    with client.app.state.session_factory() as db:
        db.add_all([
            ManualProgress(
                user_id=uuid.UUID(user_id), manual_page_id=page_id,
                state=ManualProgressState.LEARNED,
            ) for page_id in ids
        ])
        db.commit()
    return [str(page_id) for page_id in ids]


def put_card(client: TestClient, headers: dict, version_id: str, ids: list[str], revision=None):
    response = client.put(
        f"/v1/creation-versions/{version_id}/learning-card", headers=headers,
        json={
            "manual_page_ids": ids,
            "method_summary": "运用已学方法完成作品",
            "unresolved_questions": [],
            "questions_confirmed": True,
            "row_version": revision,
        },
    )
    assert response.status_code == 200, response.text
    return response.json()


def test_craft_counts_finished_works_not_manuals_or_repeated_saves(seeded_client):
    auth = register(seeded_client, "13930000801")
    headers = bearer(auth)
    ids = learn(seeded_client, auth["user"]["id"], 1, 5)
    for expected in (1, 2):
        project = create_project(seeded_client, headers)
        version = create_text_version(seeded_client, headers, project["id"])
        card = put_card(seeded_client, headers, version["id"], ids)
        assert stats(seeded_client, headers)["craft"]["count"] == expected - 1
        advance_existing_project_to_seal(seeded_client, headers, project["id"], version["id"])
        assert stats(seeded_client, headers)["craft"]["count"] == expected
        for _ in range(2):
            card = put_card(seeded_client, headers, version["id"], ids, card["row_version"])
        assert stats(seeded_client, headers)["craft"]["count"] == expected

    evidence = seeded_client.get(
        "/v1/me/learning-evidence?category=CRAFT&week_only=true", headers=headers
    ).json()
    assert evidence["total"] == 2
    assert all(item["validation_status"] == "VALID" for item in evidence["items"])
    assert all("完成作品" in item["summary"] for item in evidence["items"])
    other = bearer(register(seeded_client, "13930000802"))
    assert stats(seeded_client, other)["craft"]["count"] == 0


def test_craft_requires_learned_manual_and_can_link_it_after_completion(seeded_client):
    auth = register(seeded_client, "13930000803")
    headers = bearer(auth)
    ids = learn(seeded_client, auth["user"]["id"], 1)
    project = create_project(seeded_client, headers)
    version = create_text_version(seeded_client, headers, project["id"])
    advance_existing_project_to_seal(seeded_client, headers, project["id"], version["id"])
    assert stats(seeded_client, headers)["craft"]["count"] == 0
    card = put_card(seeded_client, headers, version["id"], [str(stable_id("manual_page", "5"))])
    assert stats(seeded_client, headers)["craft"]["count"] == 0
    put_card(seeded_client, headers, version["id"], ids, card["row_version"])
    assert stats(seeded_client, headers)["craft"]["count"] == 1


@pytest.mark.parametrize("visibility,decision,expected", [
    ("COMMUNITY", "PUBLISH", 1),
    ("PRIVATE", "PUBLISH", 0),
    ("COMMUNITY", "RETURN", 0),
])
def test_chivalry_requires_successful_conference_publication(
    seeded_client, visibility, decision, expected,
):
    auth = register(seeded_client, "13930000804")
    headers = bearer(auth)
    publication_id, case_id = create_published_community_work(
        seeded_client.app, auth["user"]["id"], title="大会分享作品"
    )
    with seeded_client.app.state.session_factory() as db:
        publication = db.get(Publication, uuid.UUID(publication_id))
        publication.visibility = CreationVisibility(visibility)
        db.commit()
    # Prime the cached luggage snapshot while the upload is still pending.
    before = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert before["data"]["stats"]["evidence"]["chivalry"]["count"] == 0
    response = seeded_client.post(
        f"/v1/internal/moderation-cases/{case_id}/decision",
        headers={"X-Internal-Token": INTERNAL_TOKEN},
        json={
            "decision": decision, "risk_level": "LOW", "row_version": 1,
            "reviewer_reference": "growth-test",
            **({"reason_code": "REVISION_REQUIRED", "reason_summary": "作品需要修改。",
                "revision_suggestion": "请完善作品内容后再投稿。"}
               if decision == "RETURN" else {}),
        },
    )
    assert response.status_code == 200, response.text
    after = seeded_client.get("/v1/me/luggage", headers=headers).json()
    assert after["data"]["stats"]["evidence"]["chivalry"]["count"] == expected
    assert stats(seeded_client, headers)["chivalry"]["count"] == expected
    evidence = seeded_client.get(
        "/v1/me/learning-evidence?category=CHIVALRY", headers=headers
    ).json()
    assert evidence["total"] == expected
    if expected:
        with seeded_client.app.state.session_factory() as db:
            record = db.get(LearningEvidence, uuid.UUID(evidence["items"][0]["id"]))
            assert record.source_id == uuid.UUID(publication_id)
        assert "大会分享作品" in evidence["items"][0]["summary"]


def test_republishing_a_new_version_does_not_duplicate_chivalry(seeded_client):
    auth = register(seeded_client, "13930000805")
    headers = bearer(auth)
    user_id = auth["user"]["id"]
    publication_id, case_id = create_published_community_work(seeded_client.app, user_id, title="反复完善")
    publish(seeded_client, case_id)
    version_id = create_follow_up_version(seeded_client.app, publication_id, user_id)
    with seeded_client.app.state.session_factory() as db:
        parent = db.get(Publication, uuid.UUID(publication_id))
        publication = Publication(
            project_id=parent.project_id, creation_version_id=uuid.UUID(version_id),
            owner_user_id=parent.owner_user_id,
            status=PublicationStatus.PENDING_HUMAN_REVIEW,
            visibility=CreationVisibility.COMMUNITY,
            conference_category=parent.conference_category,
            idempotency_key="new-version-publication", request_fingerprint="b" * 64,
        )
        db.add(publication)
        db.flush()
        case = ModerationCase(
            publication_id=publication.id, owner_user_id=parent.owner_user_id,
            status=ModerationCaseStatus.HUMAN_REVIEW,
            automatic_reason_codes=[], minimal_evidence={},
        )
        db.add(case)
        db.commit()
        new_case_id = str(case.id)
    publish(seeded_client, new_case_id)
    assert stats(seeded_client, headers)["chivalry"]["count"] == 1
    _, another_case = create_published_community_work(seeded_client.app, user_id, title="另一件作品")
    publish(seeded_client, another_case)
    assert stats(seeded_client, headers)["chivalry"]["count"] == 2


def test_legacy_migration_and_teaching_are_not_new_growth_awards(seeded_client):
    auth = register(seeded_client, "13930000806")
    headers = bearer(auth)
    with seeded_client.app.state.session_factory() as db:
        for category, kind in [
            (EvidenceCategory.CRAFT, "MIGRATION_SUBMITTED"),
            (EvidenceCategory.CHIVALRY, "STRUCTURED_REVIEW_ACCEPTED"),
        ]:
            db.add(LearningEvidence(
                user_id=uuid.UUID(auth["user"]["id"]), category=category,
                evidence_type=kind, source_type="LEGACY", source_id=uuid.uuid4(),
                manual_page_id=stable_id("manual_page", "1"),
                summary="原有秘籍阶段记录", rule_version="legacy-v1",
                validation_status=EvidenceValidationStatus.VALID,
            ))
        db.commit()
    growth = stats(seeded_client, headers)
    assert growth["craft"]["count"] == growth["chivalry"]["count"] == 0
    evidence = seeded_client.get("/v1/me/learning-evidence", headers=headers).json()
    assert evidence["total"] == 0
    history = seeded_client.get(
        f"/v1/manuals/{stable_id('manual_page', '1')}/evidence", headers=headers
    )
    assert history.status_code == 200, history.text
    assert len(history.json()) == 2
