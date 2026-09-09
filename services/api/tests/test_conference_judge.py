from __future__ import annotations

import json
import uuid
from unittest.mock import patch

from app.core.config import Settings
from app.core.security import utcnow
from app.domains.conference.contracts import (
    ConferenceMatchAnswerInternalPublic,
    ConferenceMatchJudgmentCandidatePublic,
    ConferenceMatchQuestionPublic,
)
from app.domains.conference.judge import (
    DevelopmentConferenceJudge,
    WebhookConferenceJudge,
)
from app.domains.conference.models import ConferenceMatchQuestionKind


def candidate() -> ConferenceMatchJudgmentCandidatePublic:
    first_user_id = uuid.uuid4()
    second_user_id = uuid.uuid4()
    questions = [
        ConferenceMatchQuestionPublic(
            id=uuid.uuid4(),
            position=position,
            kind=kind,
            prompt=f"第 {position} 题",
        )
        for position, kind in enumerate(
            (
                ConferenceMatchQuestionKind.CORE_LOGIC,
                ConferenceMatchQuestionKind.CASE_ANALYSIS,
                ConferenceMatchQuestionKind.TRANSFER,
            ),
            start=1,
        )
    ]
    answers = [
        ConferenceMatchAnswerInternalPublic(
            id=uuid.uuid4(),
            question_id=question.id,
            participant_user_id=user_id,
            answer="我会先解释核心概念，再把方案拆成可以验证的步骤。",
            reason="这样能够同时说明判断依据、执行过程和必要的安全边界。",
            submitted_at=utcnow(),
        )
        for user_id in (first_user_id, second_user_id)
        for question in questions
    ]
    return ConferenceMatchJudgmentCandidatePublic(
        match_id=uuid.uuid4(),
        manual_page_id=uuid.uuid4(),
        participant_user_ids=[first_user_id, second_user_id],
        questions=questions,
        rubrics={str(question.id): "关注概念、理由、方案与边界" for question in questions},
        answers=answers,
        matched_at=utcnow(),
    )


def test_development_conference_judge_is_deterministic_and_transparent() -> None:
    match = candidate()
    judge = DevelopmentConferenceJudge()

    first = judge.judge(match)
    second = judge.judge(match)

    assert first == second
    assert first.evaluator_reference == "development:deterministic-learning-rubric-v1"
    assert {item.user_id for item in first.participants} == set(
        match.participant_user_ids
    )
    assert all(0 <= item.score <= 100 for item in first.participants)
    assert all("开发环境透明规则" in item.summary for item in first.participants)


def test_webhook_conference_judge_uses_anonymous_labels_and_validates_response() -> None:
    match = candidate()
    settings = Settings(
        _env_file=None,
        conference_judge_provider="webhook",
        conference_judge_model="judge-model-2026-09",
        conference_judge_webhook_url="https://judge.example.test/v1/evaluate",
        conference_judge_webhook_token="judge-secret-token",
    )
    judge = WebhookConferenceJudge(settings)
    captured: dict[str, object] = {}

    class Response:
        def __enter__(self) -> "Response":
            return self

        def __exit__(self, *_args: object) -> None:
            return None

        @staticmethod
        def read(_limit: int) -> bytes:
            return json.dumps(
                {
                    "evaluator_reference": "gateway:judge-model-2026-09",
                    "participants": [
                        {
                            "participant": "A",
                            "score": 90,
                            "dimension_scores": {"理解": 91},
                            "summary": "概念理解准确。",
                            "strengths": ["理由清楚"],
                            "improvements": ["补充验证方法"],
                        },
                        {
                            "participant": "B",
                            "score": 85,
                            "dimension_scores": {"理解": 86},
                            "summary": "方案具有可执行性。",
                            "strengths": ["步骤完整"],
                            "improvements": ["补充安全边界"],
                        },
                    ],
                },
                ensure_ascii=False,
            ).encode()

    def urlopen(request: object, *, timeout: int) -> Response:
        captured["request"] = request
        captured["timeout"] = timeout
        return Response()

    with patch("urllib.request.urlopen", side_effect=urlopen):
        result = judge.judge(match)

    request = captured["request"]
    body = request.data.decode()
    assert captured["timeout"] == 30
    assert all(str(user_id) not in body for user_id in match.participant_user_ids)
    assert {item["participant"] for item in json.loads(body)["answers"]} == {"A", "B"}
    assert result.evaluator_reference == "gateway:judge-model-2026-09"
    assert [item.user_id for item in result.participants] == match.participant_user_ids
