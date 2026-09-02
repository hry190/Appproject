from __future__ import annotations

import uuid
from datetime import datetime, timezone

import pytest
from pydantic import ValidationError

from app.domains.learning.contracts import (
    LearningEventType,
    ManualProgressState,
    TrialAttemptAccepted,
    TrialAttemptCreate,
)
from app.domains.learning.rules import (
    advance_progress,
    is_qualifying_practice_event,
    shanghai_week_window,
)
from app.domains.luggage.contracts import LuggageResponse


UTC = timezone.utc


def trial_attempt_payload() -> dict:
    return {
        "trial_version_id": str(uuid.uuid4()),
        "prediction_payload": {"direction": "increase"},
        "answer_payload": {"labels": ["cat", "dog"]},
        "explanation": "  标签错误会让模型学到错误关系。  ",
        "client_request_id": "android-0001",
    }


def luggage_payload() -> dict:
    manual_page_id = uuid.uuid4()
    mistake_id = uuid.uuid4()
    project_id = uuid.uuid4()
    now = datetime(2026, 9, 1, 8, tzinfo=UTC)
    return {
        "data": {
            "profile": {
                "nickname": "阿砚",
                "avatar": None,
                "age_band": "UNDER_14",
                "class_label": "五（三）班",
                "anonymous_id": "JH-7M4K9Q2X",
                "current_title": {"code": "APPRENTICE", "name": "见习弟子"},
                "badges": [],
            },
            "stats": {
                "week": {
                    "timezone": "Asia/Shanghai",
                    "starts_at": "2026-08-30T16:00:00Z",
                    "ends_at_exclusive": "2026-09-06T16:00:00Z",
                    "practice_count": 3,
                },
                "lifetime_practice_count": 7,
                "lifetime_practice_days": 4,
                "distinct_trials_passed": 5,
                "evidence": {
                    "wisdom": {
                        "count": 2,
                        "latest_at": now,
                        "display_summary": "积累2条悟性证据",
                    },
                    "craft": {
                        "count": 1,
                        "latest_at": now,
                        "display_summary": "积累1条匠心证据",
                    },
                    "chivalry": {
                        "count": 0,
                        "latest_at": None,
                        "display_summary": "尚无侠义证据",
                    },
                },
            },
            "manuals": {
                "total": 50,
                "obtained": 3,
                "counts_by_state": {
                    "UNSEEN": 47,
                    "DISCOVERED": 1,
                    "LEARNED": 1,
                    "MASTERED": 1,
                    "TEACHING": 0,
                },
                "items": [
                    {
                        "id": manual_page_id,
                        "volume": 5,
                        "style_no": 3,
                        "title": "百炼识物诀",
                        "state": "MASTERED",
                        "state_label": "悟得",
                        "latest_evidence_summary": "在新任务中正确使用标签",
                        "updated_at": now,
                    }
                ],
                "empty_reason": None,
                "detail_url": "/v1/me/manuals",
            },
            "mistakes": {
                "pending_count": 1,
                "items": [
                    {
                        "id": mistake_id,
                        "knowledge_point": "训练集和测试集不可混用",
                        "status": "TO_REVIEW",
                        "manual_page_id": manual_page_id,
                        "retry_url": f"/v1/me/mistakes/{mistake_id}/retry-sessions",
                    }
                ],
                "empty_reason": None,
                "detail_url": "/v1/me/mistakes",
            },
            "creations": {
                "counts_by_status": {
                    "DRAFT": 1,
                    "PUBLISHED": 1,
                },
                "items": [
                    {
                        "project_id": project_id,
                        "title": "校园分类机关兽",
                        "display_status": "PUBLISHED",
                        "current_version": 2,
                        "thumbnail": None,
                        "can_revise": True,
                        "return_reason": None,
                        "updated_at": now,
                    }
                ],
                "empty_reason": None,
                "detail_url": "/v1/me/creation-projects",
            },
            "privacy": {
                "guardian_controls_active": True,
                "pending_appeal_count": 0,
                "privacy_settings_url": "/v1/me/privacy-settings",
            },
        },
        "meta": {
            "generated_at": now,
            "snapshot_version": 42,
            "etag": 'W/"luggage-user-version-42"',
        },
    }


@pytest.mark.parametrize(
    ("field", "value"),
    [
        ("score", 100),
        ("passed", True),
        ("progress_state", "TEACHING"),
        ("wisdom", 999),
        ("evidence_count", 99),
    ],
)
def test_trial_attempt_rejects_client_authored_derived_fields(
    field: str,
    value: object,
) -> None:
    payload = trial_attempt_payload()
    payload[field] = value

    with pytest.raises(ValidationError) as error:
        TrialAttemptCreate.model_validate(payload)

    assert error.value.errors()[0]["type"] == "extra_forbidden"


def test_trial_attempt_contract_normalizes_explanation_and_forbids_null_answer() -> None:
    attempt = TrialAttemptCreate.model_validate(trial_attempt_payload())
    assert attempt.explanation == "标签错误会让模型学到错误关系。"

    payload = trial_attempt_payload()
    payload["answer_payload"] = None
    with pytest.raises(ValidationError):
        TrialAttemptCreate.model_validate(payload)


def test_trial_attempt_json_schema_contains_only_client_fields() -> None:
    properties = TrialAttemptCreate.model_json_schema()["properties"]
    assert set(properties) == {
        "trial_version_id",
        "prediction_payload",
        "answer_payload",
        "explanation",
        "remediation_context_id",
        "client_request_id",
    }
    assert TrialAttemptCreate.model_json_schema()["additionalProperties"] is False


def test_server_grading_response_rejects_inconsistent_results() -> None:
    payload = {
        "attempt_id": uuid.uuid4(),
        "trial_id": uuid.uuid4(),
        "trial_version_id": uuid.uuid4(),
        "result": "PASSED",
        "score": 80,
        "max_score": 100,
        "passed": False,
        "feedback_codes": [],
        "progress_changes": [],
        "evidence_awards": [],
        "mistake": None,
        "processed_at": datetime(2026, 9, 1, 8, tzinfo=UTC),
    }

    with pytest.raises(ValidationError, match="passed must match result"):
        TrialAttemptAccepted.model_validate(payload)

    payload["passed"] = True
    payload["score"] = 101
    with pytest.raises(ValidationError, match="score must not exceed"):
        TrialAttemptAccepted.model_validate(payload)


def test_progress_advances_in_order_and_does_not_skip_prerequisites() -> None:
    state = ManualProgressState.UNSEEN

    assert advance_progress(state, LearningEventType.TRIAL_PASSED) == state
    assert advance_progress(state, LearningEventType.TRIAL_GRADED) == state

    state = advance_progress(state, LearningEventType.PREDICTION_COMPLETED)
    assert state == ManualProgressState.DISCOVERED
    assert advance_progress(state, LearningEventType.PREDICTION_COMPLETED) == state

    state = advance_progress(state, LearningEventType.TRIAL_PASSED)
    assert state == ManualProgressState.LEARNED
    state = advance_progress(state, LearningEventType.TRANSFER_EVIDENCE_APPROVED)
    assert state == ManualProgressState.MASTERED
    state = advance_progress(state, LearningEventType.STRUCTURED_REVIEW_ACCEPTED)
    assert state == ManualProgressState.TEACHING


def test_only_meaningful_learning_events_qualify_a_practice_session() -> None:
    assert is_qualifying_practice_event(LearningEventType.TRIAL_GRADED)
    assert is_qualifying_practice_event(LearningEventType.PROJECT_REVISION_COMPLETED)
    assert not is_qualifying_practice_event(LearningEventType.COMIC_PAGE_OPENED)
    assert not is_qualifying_practice_event(LearningEventType.MEDIA_UPLOAD_COMPLETED)


def test_shanghai_week_window_is_monday_based_and_uses_utc_storage() -> None:
    window = shanghai_week_window(datetime(2026, 9, 1, 12, tzinfo=UTC))
    assert window.starts_at == datetime(2026, 8, 30, 16, tzinfo=UTC)
    assert window.ends_at_exclusive == datetime(2026, 9, 6, 16, tzinfo=UTC)

    with pytest.raises(ValueError, match="timezone-aware"):
        shanghai_week_window(datetime(2026, 9, 1, 12))


def test_luggage_contract_accepts_a_consistent_snapshot() -> None:
    response = LuggageResponse.model_validate(luggage_payload())
    assert response.data.stats.week.practice_count == 3
    assert response.data.manuals.obtained == 3
    assert response.data.manuals.items[0].state == ManualProgressState.MASTERED
    assert response.meta.snapshot_version == 42


def test_luggage_contract_rejects_inconsistent_manual_counts() -> None:
    payload = luggage_payload()
    payload["data"]["manuals"]["counts_by_state"]["UNSEEN"] = 46

    with pytest.raises(ValidationError, match="add up to total"):
        LuggageResponse.model_validate(payload)


def test_luggage_contract_requires_explicit_empty_states() -> None:
    payload = luggage_payload()
    payload["data"]["mistakes"] = {
        "pending_count": 0,
        "items": [],
        "empty_reason": None,
        "detail_url": "/v1/me/mistakes",
    }

    with pytest.raises(ValidationError, match="NO_MISTAKES"):
        LuggageResponse.model_validate(payload)

    payload["data"]["mistakes"]["empty_reason"] = "NO_MISTAKES"
    response = LuggageResponse.model_validate(payload)
    assert response.data.mistakes.items == []


def test_luggage_contract_rejects_unknown_response_fields() -> None:
    payload = luggage_payload()
    payload["data"]["stats"]["streak_days"] = 365

    with pytest.raises(ValidationError) as error:
        LuggageResponse.model_validate(payload)

    assert error.value.errors()[0]["type"] == "extra_forbidden"
