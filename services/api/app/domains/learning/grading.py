from __future__ import annotations

from dataclasses import dataclass

from app.domains.learning.contracts import TrialAttemptCreate, TrialAttemptResult
from app.domains.learning.models import TrialGraderKind, TrialVersion


@dataclass(frozen=True, slots=True)
class GradeResult:
    score: float
    max_score: float
    result: TrialAttemptResult
    feedback_codes: list[str]

    @property
    def passed(self) -> bool:
        return self.result == TrialAttemptResult.PASSED


def has_meaningful_value(value: object | None) -> bool:
    if value is None:
        return False
    if isinstance(value, str):
        return bool(value.strip())
    if isinstance(value, (dict, list)):
        return bool(value)
    return True


def grade_attempt(version: TrialVersion, payload: TrialAttemptCreate) -> GradeResult:
    if version.grader_kind != TrialGraderKind.EXACT_JSON:
        raise RuntimeError(f"unsupported grader kind: {version.grader_kind}")

    expected_answer = version.grader_config.get("expected_answer")
    answer_correct = payload.answer_payload == expected_answer
    score = version.max_score if answer_correct else 0.0
    feedback_codes: list[str] = []
    if not answer_correct:
        feedback_codes.append(
            str(version.grader_config.get("failure_code", "ANSWER_INCORRECT"))
        )

    prediction_valid = has_meaningful_value(payload.prediction_payload)
    if version.prediction_required and not prediction_valid:
        feedback_codes.append("PREDICTION_REQUIRED")

    explanation_length = len(payload.explanation or "")
    explanation_valid = (
        not version.explanation_required
        or explanation_length >= version.min_explanation_length
    )
    if not explanation_valid:
        feedback_codes.append("EXPLANATION_REQUIRED")

    passed = (
        score >= version.pass_score
        and (prediction_valid or not version.prediction_required)
        and explanation_valid
    )
    if passed:
        feedback_codes = ["CORRECT"]
    return GradeResult(
        score=score,
        max_score=version.max_score,
        result=TrialAttemptResult.PASSED if passed else TrialAttemptResult.FAILED,
        feedback_codes=feedback_codes,
    )
