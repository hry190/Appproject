from __future__ import annotations

import base64
import binascii
import hashlib
import json
import uuid
from datetime import datetime

from sqlalchemy import func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.catalog.models import ManualPage
from app.domains.learning.contracts import (
    EvidenceAwardPublic,
    EvidenceCategory,
    EvidenceCounterPublic,
    EvidenceCountersPublic,
    EvidenceListItemPublic,
    EvidenceListPublic,
    EvidenceValidationStatus,
    LearningEventType,
    LearningStatsPublic,
    LearningWeekPublic,
    ManualProgressState,
    ManualLearningHistoryPublic,
    ProgressChangePublic,
    ProgressTransitionPublic,
    TrialAttemptAccepted,
    TrialAttemptCreate,
    TrialAttemptResult,
    TrialPublic,
    TrialVersionPublic,
)
from app.domains.learning.grading import (
    GradeResult,
    grade_attempt,
    has_meaningful_value,
)
from app.domains.learning.models import (
    LearningEvent,
    LearningEvidence,
    ManualProgress,
    PracticeSession,
    ProgressTransition,
    Trial,
    TrialAttempt,
    TrialStatus,
    TrialVersion,
    UserLearningStats,
)
from app.domains.learning.rules import (
    advance_progress,
    shanghai_calendar_date,
    shanghai_week_window,
)
from app.domains.mistakes.service import MistakeService
from app.domains.profiles.models import BadgeDefinition, UserBadge
from app.models import User


EVIDENCE_CATEGORY_LABELS = {
    EvidenceCategory.WISDOM: "悟性",
    EvidenceCategory.CRAFT: "匠心",
    EvidenceCategory.CHIVALRY: "侠义",
}


def _evidence_display_summary(category: EvidenceCategory, count: int) -> str:
    label = EVIDENCE_CATEGORY_LABELS[category]
    if count == 0:
        return f"尚无{label}证据"
    return f"积累{count}条{label}证据"


def _encode_evidence_cursor(offset: int) -> str:
    return base64.urlsafe_b64encode(f"ev1:{offset}".encode()).decode().rstrip("=")


def _decode_evidence_cursor(cursor: str) -> int:
    try:
        padded = cursor + "=" * (-len(cursor) % 4)
        version, raw_offset = base64.urlsafe_b64decode(padded).decode().split(":", 1)
        offset = int(raw_offset)
        if version != "ev1" or offset < 0:
            raise ValueError
        return offset
    except (ValueError, UnicodeDecodeError, binascii.Error) as exc:
        raise ApiError(400, "INVALID_CURSOR", "分页游标无效，请从第一页重新加载") from exc


def _request_fingerprint(
    trial_id: uuid.UUID,
    payload: TrialAttemptCreate,
) -> str:
    serialized = json.dumps(
        {
            "trial_id": str(trial_id),
            "payload": payload.model_dump(mode="json"),
        },
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(serialized.encode()).hexdigest()


class LearningService:
    def __init__(self, *, db: Session) -> None:
        self.db = db
        self.mistakes = MistakeService(db=db)

    def get_trial(self, trial_id: uuid.UUID) -> TrialPublic:
        row = self.db.execute(
            select(Trial, TrialVersion)
            .join(TrialVersion, TrialVersion.trial_id == Trial.id)
            .where(
                Trial.id == trial_id,
                Trial.status == TrialStatus.ACTIVE,
                TrialVersion.is_active.is_(True),
            )
            .order_by(TrialVersion.version.desc())
            .limit(1)
        ).one_or_none()
        if row is None:
            raise ApiError(404, "TRIAL_NOT_FOUND", "试炼不存在或暂未开放")
        trial, version = row
        return TrialPublic(
            id=trial.id,
            code=trial.code,
            title=trial.title,
            knowledge_point_code=trial.knowledge_point_code,
            manual_page_id=trial.manual_page_id,
            current_version=self._version_public(version),
        )

    def submit_attempt(
        self,
        user: User,
        trial_id: uuid.UUID,
        payload: TrialAttemptCreate,
        idempotency_key: str,
    ) -> TrialAttemptAccepted:
        fingerprint = _request_fingerprint(trial_id, payload)
        existing = self.db.scalar(
            select(TrialAttempt).where(
                TrialAttempt.user_id == user.id,
                TrialAttempt.idempotency_key == idempotency_key,
            )
        )
        if existing is not None:
            if existing.request_fingerprint != fingerprint:
                raise ApiError(
                    409,
                    "IDEMPOTENCY_KEY_REUSED",
                    "该幂等键已用于不同请求，请生成新键后重试",
                )
            return TrialAttemptAccepted.model_validate(existing.response_payload)

        # Serialize projection updates per learner so two different attempts cannot
        # race while creating the first stats or manual-progress row.
        self.db.scalar(select(User.id).where(User.id == user.id).with_for_update())
        trial, version = self._load_active_trial_version(
            trial_id,
            payload.trial_version_id,
        )
        grade = grade_attempt(version, payload)
        now = utcnow()
        remediation_context = self.mistakes.validate_context(
            user,
            payload.remediation_context_id,
            trial_id=trial.id,
            trial_version_id=version.id,
            now=now,
        )
        had_passed_before = self.db.scalar(
            select(func.count(TrialAttempt.id)).where(
                TrialAttempt.user_id == user.id,
                TrialAttempt.trial_id == trial.id,
                TrialAttempt.result == TrialAttemptResult.PASSED,
            )
        )
        practice = PracticeSession(
            id=uuid.uuid4(),
            user_id=user.id,
            source_type="TRIAL_ATTEMPT",
            started_at=now,
            qualified_at=now,
            ended_at=now,
        )
        attempt = TrialAttempt(
            id=uuid.uuid4(),
            user_id=user.id,
            trial_id=trial.id,
            trial_version_id=version.id,
            practice_session_id=practice.id,
            prediction_payload=payload.prediction_payload,
            answer_payload=payload.answer_payload,
            explanation=payload.explanation,
            server_score=grade.score,
            max_score=grade.max_score,
            result=grade.result,
            feedback_codes=grade.feedback_codes,
            idempotency_key=idempotency_key,
            request_fingerprint=fingerprint,
            client_request_id=payload.client_request_id,
            response_payload={},
            submitted_at=now,
            graded_at=now,
        )
        self.db.add_all([practice, attempt])
        # Flush the parent session/attempt before progress projection creates
        # LearningEvent rows. PostgreSQL enforces the practice-session FK
        # immediately; SQLite tests do not unless foreign keys are enabled.
        self.db.flush([practice, attempt])

        progress_changes: list[ProgressChangePublic] = []
        if has_meaningful_value(payload.prediction_payload):
            prediction_event = self._create_event(
                user=user,
                event_type=LearningEventType.PREDICTION_COMPLETED,
                attempt=attempt,
                trial=trial,
                version=version,
                practice=practice,
                idempotency_key=f"{idempotency_key}:prediction",
                payload={"prediction_recorded": True},
                occurred_at=now,
            )
            progress_changes.append(
                self._apply_progress(
                    user=user,
                    manual_page_id=trial.manual_page_id,
                    event=prediction_event,
                    evidence=None,
                    occurred_at=now,
                )
            )

        self._create_event(
            user=user,
            event_type=LearningEventType.TRIAL_GRADED,
            attempt=attempt,
            trial=trial,
            version=version,
            practice=practice,
            idempotency_key=f"{idempotency_key}:graded",
            payload={
                "result": grade.result.value,
                "score": grade.score,
                "max_score": grade.max_score,
                "feedback_codes": grade.feedback_codes,
            },
            occurred_at=now,
        )

        evidence_awards: list[EvidenceAwardPublic] = []
        evidence: LearningEvidence | None = None
        if grade.passed:
            passed_event = self._create_event(
                user=user,
                event_type=LearningEventType.TRIAL_PASSED,
                attempt=attempt,
                trial=trial,
                version=version,
                practice=practice,
                idempotency_key=f"{idempotency_key}:passed",
                payload={"knowledge_point_code": trial.knowledge_point_code},
                occurred_at=now,
            )
            if not had_passed_before:
                evidence = LearningEvidence(
                    id=uuid.uuid4(),
                    user_id=user.id,
                    category=version.evidence_category,
                    evidence_type="TRIAL_EXPLANATION_ACCEPTED",
                    source_type="TRIAL_ATTEMPT",
                    source_id=attempt.id,
                    manual_page_id=trial.manual_page_id,
                    summary=f"通过“{trial.title}”并提交了符合要求的解释",
                    rule_version=version.rule_version,
                    validation_status=EvidenceValidationStatus.VALID,
                    created_at=now,
                    validated_at=now,
                )
                self.db.add(evidence)
            progress_changes.append(
                self._apply_progress(
                    user=user,
                    manual_page_id=trial.manual_page_id,
                    event=passed_event,
                    evidence=evidence,
                    occurred_at=now,
                )
            )
            if evidence is not None:
                evidence_awards.append(self._evidence_public(evidence))

        self._update_stats(
            user=user,
            grade=grade,
            evidence=evidence,
            first_pass=grade.passed and not bool(had_passed_before),
            occurred_at=now,
        )
        if grade.passed and not had_passed_before:
            self._award_first_trial_badge(user, attempt, now)
        mistake = self.mistakes.record_attempt(
            user=user,
            trial=trial,
            attempt=attempt,
            grade=grade,
            answer_payload=payload.answer_payload,
            explanation=payload.explanation,
            context_row=remediation_context,
            occurred_at=now,
        )
        response = TrialAttemptAccepted(
            attempt_id=attempt.id,
            trial_id=trial.id,
            trial_version_id=version.id,
            result=grade.result,
            score=grade.score,
            max_score=grade.max_score,
            passed=grade.passed,
            feedback_codes=grade.feedback_codes,
            progress_changes=progress_changes,
            evidence_awards=evidence_awards,
            mistake=mistake,
            processed_at=now,
        )
        attempt.response_payload = response.model_dump(mode="json")
        try:
            self.db.commit()
        except IntegrityError:
            self.db.rollback()
            replay = self.db.scalar(
                select(TrialAttempt).where(
                    TrialAttempt.user_id == user.id,
                    TrialAttempt.idempotency_key == idempotency_key,
                )
            )
            if replay is None:
                raise
            if replay.request_fingerprint != fingerprint:
                raise ApiError(
                    409,
                    "IDEMPOTENCY_KEY_REUSED",
                    "该幂等键已用于不同请求，请生成新键后重试",
                )
            return TrialAttemptAccepted.model_validate(replay.response_payload)
        return response

    def get_stats(self, user: User, *, reference: datetime | None = None) -> LearningStatsPublic:
        now = reference or utcnow()
        week = shanghai_week_window(now)
        stats = self.db.get(UserLearningStats, user.id)
        weekly_count = self.db.scalar(
            select(func.count(PracticeSession.id)).where(
                PracticeSession.user_id == user.id,
                PracticeSession.qualified_at >= week.starts_at,
                PracticeSession.qualified_at < week.ends_at_exclusive,
            )
        ) or 0
        return LearningStatsPublic(
            week=LearningWeekPublic(
                starts_at=week.starts_at,
                ends_at_exclusive=week.ends_at_exclusive,
                practice_count=weekly_count,
            ),
            lifetime_practice_count=stats.lifetime_practice_count if stats else 0,
            lifetime_practice_days=stats.lifetime_practice_days if stats else 0,
            distinct_trials_passed=stats.distinct_trials_passed if stats else 0,
            evidence=EvidenceCountersPublic(
                wisdom=EvidenceCounterPublic(
                    count=stats.wisdom_count if stats else 0,
                    latest_at=stats.wisdom_latest_at if stats else None,
                    display_summary=_evidence_display_summary(
                        EvidenceCategory.WISDOM,
                        stats.wisdom_count if stats else 0,
                    ),
                ),
                craft=EvidenceCounterPublic(
                    count=stats.craft_count if stats else 0,
                    latest_at=stats.craft_latest_at if stats else None,
                    display_summary=_evidence_display_summary(
                        EvidenceCategory.CRAFT,
                        stats.craft_count if stats else 0,
                    ),
                ),
                chivalry=EvidenceCounterPublic(
                    count=stats.chivalry_count if stats else 0,
                    latest_at=stats.chivalry_latest_at if stats else None,
                    display_summary=_evidence_display_summary(
                        EvidenceCategory.CHIVALRY,
                        stats.chivalry_count if stats else 0,
                    ),
                ),
            ),
        )

    def list_user_evidence(
        self,
        user: User,
        *,
        category: EvidenceCategory | None,
        week_only: bool,
        cursor: str | None,
        limit: int,
        reference: datetime | None = None,
    ) -> EvidenceListPublic:
        filters = [LearningEvidence.user_id == user.id]
        if category is not None:
            filters.append(LearningEvidence.category == category)
        if week_only:
            week = shanghai_week_window(reference or utcnow())
            filters.extend(
                [
                    LearningEvidence.created_at >= week.starts_at,
                    LearningEvidence.created_at < week.ends_at_exclusive,
                ]
            )

        total = self.db.scalar(
            select(func.count(LearningEvidence.id)).where(*filters)
        ) or 0
        offset = _decode_evidence_cursor(cursor) if cursor else 0
        rows = self.db.execute(
            select(LearningEvidence, ManualPage.title)
            .outerjoin(ManualPage, ManualPage.id == LearningEvidence.manual_page_id)
            .where(*filters)
            .order_by(LearningEvidence.created_at.desc(), LearningEvidence.id.desc())
            .offset(offset)
            .limit(limit + 1)
        ).all()
        has_more = len(rows) > limit
        return EvidenceListPublic(
            total=total,
            items=[
                EvidenceListItemPublic(
                    **self._evidence_public(evidence).model_dump(),
                    manual_title=manual_title,
                )
                for evidence, manual_title in rows[:limit]
            ],
            next_cursor=(
                _encode_evidence_cursor(offset + limit) if has_more else None
            ),
        )

    def list_evidence(
        self,
        user: User,
        manual_page_id: uuid.UUID,
    ) -> list[EvidenceAwardPublic]:
        page_exists = self.db.scalar(
            select(ManualPage.id).where(
                ManualPage.id == manual_page_id,
                ManualPage.is_listed.is_(True),
            )
        )
        if page_exists is None:
            raise ApiError(404, "MANUAL_NOT_FOUND", "秘籍不存在或暂未开放")
        rows = self.db.scalars(
            select(LearningEvidence)
            .where(
                LearningEvidence.user_id == user.id,
                LearningEvidence.manual_page_id == manual_page_id,
            )
            .order_by(LearningEvidence.created_at.desc())
        ).all()
        return [self._evidence_public(item) for item in rows]

    def get_learning_history(
        self,
        user: User,
        manual_page_id: uuid.UUID,
    ) -> ManualLearningHistoryPublic:
        page_exists = self.db.scalar(
            select(ManualPage.id).where(
                ManualPage.id == manual_page_id,
                ManualPage.is_listed.is_(True),
            )
        )
        if page_exists is None:
            raise ApiError(404, "MANUAL_NOT_FOUND", "秘籍不存在或暂未开放")
        progress = self.db.scalar(
            select(ManualProgress).where(
                ManualProgress.user_id == user.id,
                ManualProgress.manual_page_id == manual_page_id,
            )
        )
        transition_rows = self.db.execute(
            select(ProgressTransition, LearningEvent, LearningEvidence)
            .join(LearningEvent, LearningEvent.id == ProgressTransition.event_id)
            .outerjoin(
                LearningEvidence,
                LearningEvidence.id == ProgressTransition.evidence_id,
            )
            .where(
                ProgressTransition.user_id == user.id,
                ProgressTransition.manual_page_id == manual_page_id,
            )
            .order_by(
                ProgressTransition.projection_version,
                ProgressTransition.occurred_at,
            )
        ).all()
        evidence = self.list_evidence(user, manual_page_id)
        return ManualLearningHistoryPublic(
            manual_page_id=manual_page_id,
            current_state=(
                progress.state if progress else ManualProgressState.UNSEEN
            ),
            discovered_at=progress.discovered_at if progress else None,
            learned_at=progress.learned_at if progress else None,
            mastered_at=progress.mastered_at if progress else None,
            teaching_at=progress.teaching_at if progress else None,
            transitions=[
                ProgressTransitionPublic(
                    id=transition.id,
                    previous_state=transition.previous_state,
                    current_state=transition.current_state,
                    trigger_event=event.event_type,
                    rule_version=transition.rule_version,
                    evidence_id=transition.evidence_id,
                    evidence_summary=evidence_row.summary if evidence_row else None,
                    occurred_at=transition.occurred_at,
                )
                for transition, event, evidence_row in transition_rows
            ],
            evidence=evidence,
        )

    def _load_active_trial_version(
        self,
        trial_id: uuid.UUID,
        version_id: uuid.UUID,
    ) -> tuple[Trial, TrialVersion]:
        row = self.db.execute(
            select(Trial, TrialVersion)
            .join(TrialVersion, TrialVersion.trial_id == Trial.id)
            .where(Trial.id == trial_id, TrialVersion.id == version_id)
        ).one_or_none()
        if row is None:
            raise ApiError(404, "TRIAL_NOT_FOUND", "试炼或试炼版本不存在")
        trial, version = row
        latest_active_id = self.db.scalar(
            select(TrialVersion.id)
            .where(
                TrialVersion.trial_id == trial.id,
                TrialVersion.is_active.is_(True),
            )
            .order_by(TrialVersion.version.desc())
            .limit(1)
        )
        if (
            trial.status != TrialStatus.ACTIVE
            or not version.is_active
            or latest_active_id != version.id
        ):
            raise ApiError(409, "TRIAL_VERSION_STALE", "试炼版本已更新，请重新加载")
        return trial, version

    def _create_event(
        self,
        *,
        user: User,
        event_type: LearningEventType,
        attempt: TrialAttempt,
        trial: Trial,
        version: TrialVersion,
        practice: PracticeSession,
        idempotency_key: str,
        payload: dict,
        occurred_at: datetime,
    ) -> LearningEvent:
        event = LearningEvent(
            id=uuid.uuid4(),
            user_id=user.id,
            event_type=event_type,
            source_type="TRIAL_ATTEMPT",
            source_id=attempt.id,
            manual_page_id=trial.manual_page_id,
            practice_session_id=practice.id,
            rule_version=version.rule_version,
            payload=payload,
            idempotency_key=idempotency_key,
            occurred_at=occurred_at,
        )
        self.db.add(event)
        return event

    def _apply_progress(
        self,
        *,
        user: User,
        manual_page_id: uuid.UUID,
        event: LearningEvent,
        evidence: LearningEvidence | None,
        occurred_at: datetime,
    ) -> ProgressChangePublic:
        progress = self.db.scalar(
            select(ManualProgress)
            .where(
                ManualProgress.user_id == user.id,
                ManualProgress.manual_page_id == manual_page_id,
            )
            .with_for_update()
        )
        previous = progress.state if progress else ManualProgressState.UNSEEN
        current = advance_progress(previous, event.event_type)
        if current == previous:
            return ProgressChangePublic(
                manual_page_id=manual_page_id,
                previous_state=previous,
                current_state=current,
                changed=False,
                evidence_id=evidence.id if evidence else None,
            )
        if progress is None:
            progress = ManualProgress(
                user_id=user.id,
                manual_page_id=manual_page_id,
                state=previous,
                projection_version=0,
                updated_at=occurred_at,
            )
            self.db.add(progress)
        progress.state = current
        progress.updated_at = occurred_at
        progress.projection_version += 1
        if evidence is not None:
            progress.latest_evidence_id = evidence.id
        timestamp_field = {
            ManualProgressState.DISCOVERED: "discovered_at",
            ManualProgressState.LEARNED: "learned_at",
            ManualProgressState.MASTERED: "mastered_at",
            ManualProgressState.TEACHING: "teaching_at",
        }[current]
        setattr(progress, timestamp_field, occurred_at)
        self.db.add(
            ProgressTransition(
                id=uuid.uuid4(),
                user_id=user.id,
                manual_page_id=manual_page_id,
                previous_state=previous,
                current_state=current,
                event_id=event.id,
                evidence_id=evidence.id if evidence else None,
                rule_version=event.rule_version,
                projection_version=progress.projection_version,
                occurred_at=occurred_at,
            )
        )
        # Session autoflush is disabled; persist the transition so a second event
        # in the same attempt observes the newly advanced state.
        self.db.flush()
        return ProgressChangePublic(
            manual_page_id=manual_page_id,
            previous_state=previous,
            current_state=current,
            changed=True,
            evidence_id=evidence.id if evidence else None,
        )

    def _update_stats(
        self,
        *,
        user: User,
        grade: GradeResult,
        evidence: LearningEvidence | None,
        first_pass: bool,
        occurred_at: datetime,
    ) -> None:
        stats = self.db.scalar(
            select(UserLearningStats)
            .where(UserLearningStats.user_id == user.id)
            .with_for_update()
        )
        if stats is None:
            stats = UserLearningStats(
                user_id=user.id,
                lifetime_practice_count=0,
                lifetime_practice_days=0,
                distinct_trials_passed=0,
                wisdom_count=0,
                craft_count=0,
                chivalry_count=0,
                projection_version=0,
                updated_at=occurred_at,
            )
            self.db.add(stats)
        if (
            stats.last_practice_at is None
            or shanghai_calendar_date(stats.last_practice_at)
            != shanghai_calendar_date(occurred_at)
        ):
            stats.lifetime_practice_days += 1
        stats.lifetime_practice_count += 1
        stats.last_practice_at = occurred_at
        if first_pass:
            stats.distinct_trials_passed += 1
        if grade.passed and evidence is not None:
            count_field = {
                EvidenceCategory.WISDOM: "wisdom_count",
                EvidenceCategory.CRAFT: "craft_count",
                EvidenceCategory.CHIVALRY: "chivalry_count",
            }[evidence.category]
            latest_field = {
                EvidenceCategory.WISDOM: "wisdom_latest_at",
                EvidenceCategory.CRAFT: "craft_latest_at",
                EvidenceCategory.CHIVALRY: "chivalry_latest_at",
            }[evidence.category]
            setattr(stats, count_field, getattr(stats, count_field) + 1)
            setattr(stats, latest_field, occurred_at)
        stats.projection_version += 1
        stats.updated_at = occurred_at

    def _award_first_trial_badge(
        self,
        user: User,
        attempt: TrialAttempt,
        occurred_at: datetime,
    ) -> None:
        badge = self.db.scalar(
            select(BadgeDefinition).where(
                BadgeDefinition.code == "FIRST_TRIAL",
                BadgeDefinition.is_active.is_(True),
            )
        )
        if badge is None:
            return
        already_owned = self.db.scalar(
            select(UserBadge.id).where(
                UserBadge.user_id == user.id,
                UserBadge.badge_id == badge.id,
            )
        )
        if already_owned is None:
            self.db.add(
                UserBadge(
                    user_id=user.id,
                    badge_id=badge.id,
                    evidence_ref=f"TRIAL_ATTEMPT:{attempt.id}",
                    earned_at=occurred_at,
                )
            )

    @staticmethod
    def _version_public(version: TrialVersion) -> TrialVersionPublic:
        return TrialVersionPublic(
            id=version.id,
            version=version.version,
            prompt=version.prompt,
            prediction_prompt=version.prediction_prompt,
            answer_schema=version.answer_schema,
            max_score=version.max_score,
            pass_score=version.pass_score,
            prediction_required=version.prediction_required,
            explanation_required=version.explanation_required,
            min_explanation_length=version.min_explanation_length,
        )

    @staticmethod
    def _evidence_public(evidence: LearningEvidence) -> EvidenceAwardPublic:
        return EvidenceAwardPublic(
            id=evidence.id,
            category=evidence.category,
            evidence_type=evidence.evidence_type,
            manual_page_id=evidence.manual_page_id,
            summary=evidence.summary,
            validation_status=evidence.validation_status,
            created_at=evidence.created_at,
        )
