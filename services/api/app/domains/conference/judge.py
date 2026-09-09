from __future__ import annotations

import json
import urllib.error
import urllib.request
from typing import Literal, Protocol

from pydantic import Field, ValidationError, field_validator, model_validator

from app.core.config import Settings
from app.domains.conference.contracts import (
    ConferenceMatchJudgmentCandidatePublic,
    ConferenceMatchJudgmentCreate,
    ConferenceMatchJudgmentParticipant,
)
from app.domains.learning.contracts import ContractModel


class ConferenceJudgeProviderError(Exception):
    def __init__(self, code: str, summary: str, *, retryable: bool) -> None:
        super().__init__(summary)
        self.code = code
        self.summary = summary
        self.retryable = retryable


class ConferenceJudge(Protocol):
    provider_ref: str
    model_ref: str
    external_data_shared: bool

    def judge(
        self, candidate: ConferenceMatchJudgmentCandidatePublic
    ) -> ConferenceMatchJudgmentCreate: ...


class DevelopmentConferenceJudge:
    """Transparent local scorer for development; it is deliberately not called AI."""

    provider_ref = "development"
    model_ref = "deterministic-learning-rubric-v1"
    external_data_shared = False

    def judge(
        self, candidate: ConferenceMatchJudgmentCandidatePublic
    ) -> ConferenceMatchJudgmentCreate:
        participants: list[ConferenceMatchJudgmentParticipant] = []
        question_count = max(1, len(candidate.questions))
        for user_id in candidate.participant_user_ids:
            answers = [
                answer
                for answer in candidate.answers
                if answer.participant_user_id == user_id
            ]
            completion = min(1.0, len(answers) / question_count) * 30
            answer_depth = (
                min(1.0, sum(len(item.answer) for item in answers) / (question_count * 120))
                * 30
            )
            reasoning = (
                min(1.0, sum(len(item.reason) for item in answers) / (question_count * 80))
                * 30
            )
            structured = 10 if len(answers) == question_count else 0
            score = round(completion + answer_depth + reasoning + structured, 1)
            strengths = ["已完成全部题目并为答案提供理由"] if completion == 30 else []
            improvements: list[str] = []
            if answer_depth < 24:
                improvements.append("答案可以补充更具体的方案或例子")
            if reasoning < 24:
                improvements.append("理由可以更明确地连接秘籍核心逻辑")
            if not improvements:
                improvements.append("继续补充边界条件和验证方法")
            participants.append(
                ConferenceMatchJudgmentParticipant(
                    user_id=user_id,
                    score=score,
                    dimension_scores={
                        "完成度": round(completion / 30 * 100, 1),
                        "表达充分度": round(answer_depth / 30 * 100, 1),
                        "论证充分度": round(reasoning / 30 * 100, 1),
                    },
                    summary="本结果由开发环境透明规则评分，用于验证流程，不代表模型判断。",
                    strengths=strengths,
                    improvements=improvements,
                )
            )
        return ConferenceMatchJudgmentCreate(
            evaluator_reference=f"{self.provider_ref}:{self.model_ref}",
            participants=participants,
        )


class _WebhookParticipant(ContractModel):
    participant: Literal["A", "B"]
    score: float = Field(ge=0, le=100)
    dimension_scores: dict[str, float] = Field(default_factory=dict)
    summary: str = Field(min_length=1, max_length=1000)
    strengths: list[str] = Field(default_factory=list, max_length=5)
    improvements: list[str] = Field(default_factory=list, max_length=5)

    @field_validator("dimension_scores")
    @classmethod
    def validate_dimensions(cls, value: dict[str, float]) -> dict[str, float]:
        return ConferenceMatchJudgmentParticipant.validate_dimension_scores(value)

    @field_validator("summary")
    @classmethod
    def normalize_summary(cls, value: str) -> str:
        return ConferenceMatchJudgmentParticipant.normalize_judgment_summary(value)

    @field_validator("strengths", "improvements")
    @classmethod
    def normalize_points(cls, value: list[str]) -> list[str]:
        return ConferenceMatchJudgmentParticipant.normalize_judgment_points(value)


class _WebhookResponse(ContractModel):
    evaluator_reference: str = Field(min_length=3, max_length=120)
    participants: list[_WebhookParticipant] = Field(min_length=2, max_length=2)

    @model_validator(mode="after")
    def participant_labels_must_be_complete(self) -> "_WebhookResponse":
        if {item.participant for item in self.participants} != {"A", "B"}:
            raise ValueError("response must contain participants A and B exactly once")
        return self


class WebhookConferenceJudge:
    provider_ref = "webhook"
    external_data_shared = True

    def __init__(self, settings: Settings) -> None:
        if (
            settings.conference_judge_webhook_url is None
            or settings.conference_judge_webhook_token is None
        ):
            raise ValueError("conference judge webhook requires URL and token")
        self.model_ref = settings.conference_judge_model
        self.endpoint = settings.conference_judge_webhook_url
        self.token = settings.conference_judge_webhook_token.get_secret_value()
        self.timeout = settings.conference_judge_timeout_seconds

    def judge(
        self, candidate: ConferenceMatchJudgmentCandidatePublic
    ) -> ConferenceMatchJudgmentCreate:
        labels = {
            candidate.participant_user_ids[0]: "A",
            candidate.participant_user_ids[1]: "B",
        }
        body = json.dumps(
            {
                "schema_version": "conference-judgment-v1",
                "model": self.model_ref,
                "questions": [
                    {
                        "id": str(question.id),
                        "position": question.position,
                        "kind": question.kind.value,
                        "prompt": question.prompt,
                        "rubric": candidate.rubrics[str(question.id)],
                    }
                    for question in candidate.questions
                ],
                "answers": [
                    {
                        "participant": labels[answer.participant_user_id],
                        "question_id": str(answer.question_id),
                        "answer": answer.answer,
                        "reason": answer.reason,
                    }
                    for answer in candidate.answers
                ],
                "response_schema": {
                    "evaluator_reference": "string",
                    "participants": [
                        {
                            "participant": "A or B",
                            "score": "number 0-100",
                            "dimension_scores": {"dimension": "number 0-100"},
                            "summary": "string",
                            "strengths": ["string"],
                            "improvements": ["string"],
                        }
                    ],
                },
            },
            ensure_ascii=False,
            separators=(",", ":"),
        ).encode()
        request = urllib.request.Request(
            self.endpoint,
            data=body,
            method="POST",
            headers={
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                raw = response.read(1_000_001)
        except urllib.error.HTTPError as exc:
            raise ConferenceJudgeProviderError(
                "CONFERENCE_JUDGE_HTTP_ERROR",
                "评审服务暂时不可用",
                retryable=exc.code == 429 or exc.code >= 500,
            ) from exc
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            raise ConferenceJudgeProviderError(
                "CONFERENCE_JUDGE_UNAVAILABLE",
                "评审服务连接失败",
                retryable=True,
            ) from exc
        if len(raw) > 1_000_000:
            raise ConferenceJudgeProviderError(
                "CONFERENCE_JUDGE_RESPONSE_TOO_LARGE",
                "评审服务返回内容过大",
                retryable=False,
            )
        try:
            parsed = _WebhookResponse.model_validate_json(raw)
        except (ValidationError, ValueError) as exc:
            raise ConferenceJudgeProviderError(
                "CONFERENCE_JUDGE_INVALID_RESPONSE",
                "评审服务返回结构不符合约定",
                retryable=False,
            ) from exc
        user_by_label = {label: user_id for user_id, label in labels.items()}
        return ConferenceMatchJudgmentCreate(
            evaluator_reference=parsed.evaluator_reference,
            participants=[
                ConferenceMatchJudgmentParticipant(
                    user_id=user_by_label[item.participant],
                    score=item.score,
                    dimension_scores=item.dimension_scores,
                    summary=item.summary,
                    strengths=item.strengths,
                    improvements=item.improvements,
                )
                for item in parsed.participants
            ],
        )


def build_conference_judge(settings: Settings) -> ConferenceJudge | None:
    if settings.conference_judge_provider == "disabled":
        return None
    if settings.conference_judge_provider == "webhook":
        return WebhookConferenceJudge(settings)
    return DevelopmentConferenceJudge()
