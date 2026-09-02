from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, time, timedelta, timezone
from zoneinfo import ZoneInfo

from app.domains.learning.contracts import LearningEventType, ManualProgressState


SHANGHAI_TIMEZONE = ZoneInfo("Asia/Shanghai")

PROGRESS_ORDER = (
    ManualProgressState.UNSEEN,
    ManualProgressState.DISCOVERED,
    ManualProgressState.LEARNED,
    ManualProgressState.MASTERED,
    ManualProgressState.TEACHING,
)

TARGET_STATE_BY_EVENT = {
    LearningEventType.PREDICTION_COMPLETED: ManualProgressState.DISCOVERED,
    LearningEventType.TRIAL_PASSED: ManualProgressState.LEARNED,
    LearningEventType.TRANSFER_EVIDENCE_APPROVED: ManualProgressState.MASTERED,
    LearningEventType.STRUCTURED_REVIEW_ACCEPTED: ManualProgressState.TEACHING,
}

QUALIFYING_PRACTICE_EVENTS = frozenset(
    {
        LearningEventType.PREDICTION_COMPLETED,
        LearningEventType.TRIAL_GRADED,
        LearningEventType.TRIAL_PASSED,
        LearningEventType.TRANSFER_EVIDENCE_APPROVED,
        LearningEventType.STRUCTURED_REVIEW_ACCEPTED,
        LearningEventType.PROJECT_REVISION_COMPLETED,
    }
)


@dataclass(frozen=True, slots=True)
class WeekWindow:
    starts_at: datetime
    ends_at_exclusive: datetime


def advance_progress(
    current: ManualProgressState,
    event_type: LearningEventType,
) -> ManualProgressState:
    """Advance one stage, while treating repeats and out-of-order events as no-ops."""

    target = TARGET_STATE_BY_EVENT.get(event_type)
    if target is None:
        return current

    current_index = PROGRESS_ORDER.index(current)
    target_index = PROGRESS_ORDER.index(target)
    if target_index == current_index + 1:
        return target
    return current


def is_qualifying_practice_event(event_type: LearningEventType) -> bool:
    return event_type in QUALIFYING_PRACTICE_EVENTS


def shanghai_week_window(reference: datetime) -> WeekWindow:
    """Return the Monday-based Shanghai week as a half-open UTC interval."""

    if reference.tzinfo is None or reference.utcoffset() is None:
        raise ValueError("reference must be timezone-aware")

    local_reference = reference.astimezone(SHANGHAI_TIMEZONE)
    monday = local_reference.date() - timedelta(days=local_reference.weekday())
    local_start = datetime.combine(monday, time.min, tzinfo=SHANGHAI_TIMEZONE)
    local_end = local_start + timedelta(days=7)
    return WeekWindow(
        starts_at=local_start.astimezone(timezone.utc),
        ends_at_exclusive=local_end.astimezone(timezone.utc),
    )


def shanghai_calendar_date(reference: datetime):
    """Return the learner-facing Shanghai date for an application timestamp.

    SQLite drops timezone metadata when reading DateTime values in tests, so a
    naive value is interpreted as UTC, matching how application timestamps are
    persisted.
    """

    if reference.tzinfo is None or reference.utcoffset() is None:
        reference = reference.replace(tzinfo=timezone.utc)
    return reference.astimezone(SHANGHAI_TIMEZONE).date()
