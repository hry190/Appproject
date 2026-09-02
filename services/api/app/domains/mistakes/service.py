from __future__ import annotations

import base64
import binascii
import uuid
from datetime import datetime, timedelta

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.catalog.models import ManualPage
from app.domains.learning.contracts import (
    MistakeStatus,
    TrialAttemptResult,
    TrialMistakePublic,
)
from app.domains.learning.grading import GradeResult
from app.domains.learning.models import Trial, TrialAttempt, TrialStatus, TrialVersion
from app.domains.mistakes.contracts import (
    MistakeDetailPublic,
    MistakeListItemPublic,
    MistakeListPublic,
    RemediationRecordPublic,
    RetrySessionPublic,
)
from app.domains.mistakes.models import (
    MistakeItem,
    RemediationContext,
    RemediationRecord,
)
from app.models import User


REMEDIATION_RULE_VERSION = "delayed-review-24h-v1"
REMEDIATION_DELAY = timedelta(hours=24)
CONTEXT_TTL = timedelta(minutes=15)

REASON_SUMMARIES = {
    "CONFUSED_AUTOMATION_WITH_LEARNING": (
        "把自动运行误认为机器学习；关键是系统是否会从样本经验中改进。"
    ),
    "PREDICTION_REQUIRED": "缺少作答前预测，尚不能形成完整学习证据。",
    "EXPLANATION_REQUIRED": "解释过短或缺失，请说明判断依据。",
    "ANSWER_INCORRECT": "答案与当前试炼规则不一致，请回顾关联秘籍。",
}


def _encode_cursor(offset: int) -> str:
    return base64.urlsafe_b64encode(f"v1:{offset}".encode()).decode().rstrip("=")


def _decode_cursor(cursor: str) -> int:
    try:
        padded = cursor + "=" * (-len(cursor) % 4)
        version, raw_offset = base64.urlsafe_b64decode(padded).decode().split(":", 1)
        offset = int(raw_offset)
        if version != "v1" or offset < 0:
            raise ValueError
        return offset
    except (ValueError, UnicodeDecodeError, binascii.Error) as exc:
        raise ApiError(400, "INVALID_CURSOR", "分页游标无效，请从第一页重新加载") from exc


class MistakeService:
    def __init__(self, *, db: Session) -> None:
        self.db = db

    def validate_context(
        self,
        user: User,
        context_id: uuid.UUID | None,
        *,
        trial_id: uuid.UUID,
        trial_version_id: uuid.UUID,
        now: datetime,
    ) -> tuple[RemediationContext, MistakeItem] | None:
        if context_id is None:
            return None
        row = self.db.execute(
            select(RemediationContext, MistakeItem)
            .join(MistakeItem, MistakeItem.id == RemediationContext.mistake_id)
            .where(
                RemediationContext.id == context_id,
                RemediationContext.user_id == user.id,
                RemediationContext.trial_version_id == trial_version_id,
                RemediationContext.used_at.is_(None),
                RemediationContext.expires_at > now,
                MistakeItem.trial_id == trial_id,
            )
            .with_for_update()
        ).one_or_none()
        if row is None:
            raise ApiError(
                409,
                "REMEDIATION_CONTEXT_INVALID",
                "重练上下文无效、已使用或已过期，请重新点击“再试一次”",
            )
        return row

    def record_attempt(
        self,
        *,
        user: User,
        trial: Trial,
        attempt: TrialAttempt,
        grade: GradeResult,
        answer_payload: object,
        explanation: str | None,
        context_row: tuple[RemediationContext, MistakeItem] | None,
        occurred_at: datetime,
    ) -> TrialMistakePublic | None:
        if context_row is not None:
            context, mistake = context_row
            context.used_at = occurred_at
            mistake.latest_attempt_id = attempt.id
            mistake.row_version += 1
            mistake.updated_at = occurred_at
            self.db.add(
                RemediationRecord(
                    id=uuid.uuid4(),
                    mistake_id=mistake.id,
                    attempt_id=attempt.id,
                    result=grade.result,
                    reflection=explanation,
                    occurred_at=occurred_at,
                )
            )
            if grade.passed:
                mistake.successful_retries += 1
                if (
                    mistake.status == MistakeStatus.PRACTICING
                    and mistake.next_review_at is not None
                    and self._is_due(mistake.next_review_at, occurred_at)
                ):
                    mistake.status = MistakeStatus.CONSOLIDATED
                    mistake.consolidated_at = occurred_at
                else:
                    mistake.status = MistakeStatus.PRACTICING
                    if mistake.next_review_at is None:
                        mistake.next_review_at = occurred_at + REMEDIATION_DELAY
                return None

            reason_code = self._primary_reason(grade.feedback_codes)
            mistake.failure_count += 1
            mistake.status = MistakeStatus.PRACTICING
            mistake.error_reason_code = reason_code
            mistake.error_reason_summary = self._reason_summary(reason_code)
            return self._trial_mistake(mistake)

        if grade.passed:
            return None
        mistake = self.db.scalar(
            select(MistakeItem)
            .where(
                MistakeItem.user_id == user.id,
                MistakeItem.trial_id == trial.id,
                MistakeItem.knowledge_point_code == trial.knowledge_point_code,
            )
            .with_for_update()
        )
        reason_code = self._primary_reason(grade.feedback_codes)
        if mistake is None:
            mistake = MistakeItem(
                id=uuid.uuid4(),
                user_id=user.id,
                trial_id=trial.id,
                manual_page_id=trial.manual_page_id,
                knowledge_point_code=trial.knowledge_point_code,
                first_attempt_id=attempt.id,
                latest_attempt_id=attempt.id,
                original_answer_payload=answer_payload,
                error_reason_code=reason_code,
                error_reason_summary=self._reason_summary(reason_code),
                status=MistakeStatus.TO_REVIEW,
                failure_count=1,
                successful_retries=0,
                rule_version=REMEDIATION_RULE_VERSION,
                row_version=1,
                created_at=occurred_at,
                updated_at=occurred_at,
            )
            self.db.add(mistake)
        else:
            mistake.latest_attempt_id = attempt.id
            mistake.error_reason_code = reason_code
            mistake.error_reason_summary = self._reason_summary(reason_code)
            mistake.status = MistakeStatus.TO_REVIEW
            mistake.failure_count += 1
            mistake.next_review_at = None
            mistake.consolidated_at = None
            mistake.row_version += 1
            mistake.updated_at = occurred_at
        return self._trial_mistake(mistake)

    def list_mistakes(
        self,
        user: User,
        *,
        status: MistakeStatus | None,
        manual_page_id: uuid.UUID | None,
        cursor: str | None,
        limit: int,
    ) -> MistakeListPublic:
        filters = [MistakeItem.user_id == user.id]
        if status is not None:
            filters.append(MistakeItem.status == status)
        if manual_page_id is not None:
            filters.append(MistakeItem.manual_page_id == manual_page_id)
        total = self.db.scalar(select(func.count(MistakeItem.id)).where(*filters)) or 0
        offset = _decode_cursor(cursor) if cursor else 0
        rows = self.db.execute(
            select(MistakeItem, ManualPage)
            .join(ManualPage, ManualPage.id == MistakeItem.manual_page_id)
            .where(*filters)
            .order_by(MistakeItem.updated_at.desc(), MistakeItem.id)
            .offset(offset)
            .limit(limit + 1)
        ).all()
        has_more = len(rows) > limit
        items = [self._list_item(item, page) for item, page in rows[:limit]]
        return MistakeListPublic(
            total=total,
            items=items,
            next_cursor=_encode_cursor(offset + limit) if has_more else None,
        )

    def get_mistake(self, user: User, mistake_id: uuid.UUID) -> MistakeDetailPublic:
        row = self.db.execute(
            select(MistakeItem, ManualPage)
            .join(ManualPage, ManualPage.id == MistakeItem.manual_page_id)
            .where(MistakeItem.id == mistake_id, MistakeItem.user_id == user.id)
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MISTAKE_NOT_FOUND", "错题不存在")
        mistake, page = row
        records = self.db.scalars(
            select(RemediationRecord)
            .where(RemediationRecord.mistake_id == mistake.id)
            .order_by(RemediationRecord.occurred_at)
        ).all()
        return MistakeDetailPublic(
            **self._list_item(mistake, page).model_dump(),
            first_attempt_id=mistake.first_attempt_id,
            latest_attempt_id=mistake.latest_attempt_id,
            original_answer_payload=mistake.original_answer_payload,
            consolidated_at=mistake.consolidated_at,
            remediation_records=[
                RemediationRecordPublic(
                    id=record.id,
                    attempt_id=record.attempt_id,
                    result=record.result,
                    reflection=record.reflection,
                    occurred_at=record.occurred_at,
                )
                for record in records
            ],
        )

    def create_retry_session(
        self,
        user: User,
        mistake_id: uuid.UUID,
    ) -> RetrySessionPublic:
        mistake = self.db.scalar(
            select(MistakeItem)
            .where(MistakeItem.id == mistake_id, MistakeItem.user_id == user.id)
            .with_for_update()
        )
        if mistake is None:
            raise ApiError(404, "MISTAKE_NOT_FOUND", "错题不存在")
        if mistake.status == MistakeStatus.CONSOLIDATED:
            raise ApiError(409, "MISTAKE_ALREADY_CONSOLIDATED", "该错题已经巩固")
        version = self.db.scalar(
            select(TrialVersion)
            .join(Trial, Trial.id == TrialVersion.trial_id)
            .where(
                Trial.id == mistake.trial_id,
                Trial.status == TrialStatus.ACTIVE,
                TrialVersion.is_active.is_(True),
            )
            .order_by(TrialVersion.version.desc())
            .limit(1)
        )
        if version is None:
            raise ApiError(409, "TRIAL_VERSION_STALE", "关联试炼暂不可用")
        now = utcnow()
        context = RemediationContext(
            id=uuid.uuid4(),
            user_id=user.id,
            mistake_id=mistake.id,
            trial_version_id=version.id,
            expires_at=now + CONTEXT_TTL,
            created_at=now,
        )
        self.db.add(context)
        self.db.commit()
        return RetrySessionPublic(
            id=context.id,
            mistake_id=mistake.id,
            trial_id=mistake.trial_id,
            trial_version_id=version.id,
            expires_at=context.expires_at,
            submit_url=f"/v1/trials/{mistake.trial_id}/attempts",
        )

    @staticmethod
    def _is_due(next_review_at: datetime, occurred_at: datetime) -> bool:
        if next_review_at.tzinfo is None:
            next_review_at = next_review_at.replace(tzinfo=occurred_at.tzinfo)
        return next_review_at <= occurred_at

    @staticmethod
    def _primary_reason(feedback_codes: list[str]) -> str:
        return feedback_codes[0] if feedback_codes else "ANSWER_INCORRECT"

    @staticmethod
    def _reason_summary(reason_code: str) -> str:
        return REASON_SUMMARIES.get(reason_code, REASON_SUMMARIES["ANSWER_INCORRECT"])

    @staticmethod
    def _trial_mistake(mistake: MistakeItem) -> TrialMistakePublic:
        return TrialMistakePublic(
            id=mistake.id,
            knowledge_point_code=mistake.knowledge_point_code,
            reason_code=mistake.error_reason_code,
            status=mistake.status,
        )

    @staticmethod
    def _list_item(mistake: MistakeItem, page: ManualPage) -> MistakeListItemPublic:
        return MistakeListItemPublic(
            id=mistake.id,
            trial_id=mistake.trial_id,
            manual_page_id=mistake.manual_page_id,
            manual_title=page.title,
            knowledge_point_code=mistake.knowledge_point_code,
            error_reason_code=mistake.error_reason_code,
            error_reason_summary=mistake.error_reason_summary,
            status=mistake.status,
            failure_count=mistake.failure_count,
            successful_retries=mistake.successful_retries,
            next_review_at=mistake.next_review_at,
            updated_at=mistake.updated_at,
            retry_url=f"/v1/mistakes/{mistake.id}/retry-sessions",
        )
