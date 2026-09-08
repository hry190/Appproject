from __future__ import annotations

from typing import Literal

from pydantic import Field

from app.domains.learning.contracts import ContractModel


# 首批比赛展示路线的结构化试炼定义。答案只用于服务端判分，
# 不会通过公开的试炼详情接口返回 grader_config。
TRIAL_DEFINITIONS: dict[int, dict] = {
    1: {
        "trial_type": "exact_json",
        "prompt": "哪一项最能说明一个系统使用了机器学习？",
        "prediction_prompt": "作答前先预测：你认为哪一个特征最关键？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": [
                        "FOLLOWS_FIXED_RULES",
                        "LEARNS_FROM_DATA",
                        "MOVES_AUTOMATICALLY",
                    ],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": "EXACT_JSON",
        "grader_config": {
            "expected_answer": {"choice": "LEARNS_FROM_DATA"},
            "failure_code": "CONFUSED_AUTOMATION_WITH_LEARNING",
        },
        "rubric_version": "manual-01-exact-v1",
        "status": "ACTIVE",
    },
    5: {
        "trial_type": "exact_json",
        "prompt": "验毒机关只有七成把握时，高风险场景最合适的下一步是什么？",
        "prediction_prompt": "作答前先预测：高风险任务应该怎样处理不确定结果？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": ["AUTO_ACCEPT", "LOWER_THRESHOLD", "HUMAN_REVIEW"],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": "EXACT_JSON",
        "grader_config": {
            "expected_answer": {"choice": "HUMAN_REVIEW"},
            "failure_code": "IGNORED_HIGH_RISK_UNCERTAINTY",
        },
        "rubric_version": "manual-05-exact-v1",
        "status": "ACTIVE",
    },
    23: {
        "trial_type": "exact_json",
        "prompt": "为什么测试集不能在训练过程中反复查看？",
        "prediction_prompt": "作答前先预测：测试集在学习流程中应该保持什么状态？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": ["REUSE_TEST_SET", "UNSEEN_TEST_SET", "TRAINING_ONLY"],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": "EXACT_JSON",
        "grader_config": {
            "expected_answer": {"choice": "UNSEEN_TEST_SET"},
            "failure_code": "LEAKED_TEST_SET",
        },
        "rubric_version": "manual-23-exact-v1",
        "status": "ACTIVE",
    },
    29: {
        "trial_type": "exact_json",
        "prompt": "危险物识别属于高风险任务时，阈值调整应优先避免哪类错误？",
        "prediction_prompt": "作答前先预测：高风险识别应优先减少误报还是漏报？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": ["HIGH_PRECISION", "HIGH_RECALL", "NO_TRADEOFF"],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": "EXACT_JSON",
        "grader_config": {
            "expected_answer": {"choice": "HIGH_RECALL"},
            "failure_code": "MISSED_HIGH_RISK_TRADEOFF",
        },
        "rubric_version": "manual-29-exact-v1",
        "status": "ACTIVE",
    },
    45: {
        "trial_type": "exact_json",
        "prompt": "面对一段流畅但可能出错的 AI 生成内容，最可靠的下一步是什么？",
        "prediction_prompt": "作答前先预测：怎样区分听起来合理和有证据支持？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": ["TRUST_FLUENCY", "VERIFY_SOURCES", "SHARE_FIRST"],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": "EXACT_JSON",
        "grader_config": {
            "expected_answer": {"choice": "VERIFY_SOURCES"},
            "failure_code": "TRUSTED_UNVERIFIED_GENERATION",
        },
        "rubric_version": "manual-45-exact-v1",
        "status": "ACTIVE",
    },
}


class TrialManifestEntry(ContractModel):
    page_no: int = Field(ge=1, le=50)
    trial_type: Literal["exact_json", "choice", "slider", "sort", "classification", "pending"]
    prompt: str = Field(min_length=1, max_length=500)
    prediction_prompt: str = Field(min_length=1, max_length=300)
    answer_schema: dict
    grader_kind: str | None = None
    grader_config: dict | None = None
    rubric_version: str = Field(min_length=1, max_length=32)
    status: Literal["ACTIVE", "DRAFT"]

    @property
    def ready_for_activation(self) -> bool:
        return (
            self.status == "ACTIVE"
            and self.trial_type != "pending"
            and self.grader_kind is not None
            and self.grader_config is not None
        )


def build_trial_manifest() -> list[TrialManifestEntry]:
    """Build the content checklist without inventing answers for unfinished trials."""
    from app.domains.catalog.seed import PAGES

    entries: list[TrialManifestEntry] = []
    for page_no, _title, _logic, _hook, interaction in PAGES:
        definition = TRIAL_DEFINITIONS.get(page_no)
        if definition is not None:
            entries.append(TrialManifestEntry(page_no=page_no, **definition))
        else:
            entries.append(
                TrialManifestEntry(
                    page_no=page_no,
                    trial_type="pending",
                    prompt=interaction,
                    prediction_prompt=f"学习“{interaction[:40]}”前，先写下你的预测。",
                    answer_schema={"type": "object"},
                    rubric_version="pending",
                    status="DRAFT",
                )
            )
    return entries


def validate_trial_manifest(entries: list[TrialManifestEntry]) -> None:
    page_numbers = [entry.page_no for entry in entries]
    if page_numbers != list(range(1, 51)):
        raise ValueError("trial manifest must contain pages 1 through 50 exactly once")
    active = [entry for entry in entries if entry.status == "ACTIVE"]
    if any(not entry.ready_for_activation for entry in active):
        raise ValueError("active trial entries must have a grader and grading config")
    if any(entry.status == "DRAFT" and entry.ready_for_activation for entry in entries):
        raise ValueError("draft trial entries cannot be marked ready for activation")
