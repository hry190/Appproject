from __future__ import annotations

import uuid
from datetime import timedelta

from sqlalchemy import create_engine, select
from sqlalchemy.pool import StaticPool

from app.core.config import Settings
from app.core.security import utcnow
from app.db import Base, build_session_factory
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.media.worker import OutboxWorker


def worker_with_memory_database() -> tuple[OutboxWorker, object]:
    engine = create_engine(
        "sqlite+pysqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(engine)
    session_factory = build_session_factory(engine)
    worker = OutboxWorker.__new__(OutboxWorker)
    worker.settings = Settings(
        _env_file=None,
        environment="test",
        database_url="sqlite+pysqlite://",
        outbox_max_attempts=3,
        outbox_lease_seconds=30,
    )
    worker.session_factory = session_factory
    worker.conference_judge = None
    return worker, session_factory


def test_outbox_worker_reclaims_an_expired_processing_lease() -> None:
    worker, session_factory = worker_with_memory_database()
    event_id = uuid.uuid4()
    with session_factory() as db:
        db.add(
            OutboxEvent(
                id=event_id,
                aggregate_type="TEST",
                aggregate_id=uuid.uuid4(),
                event_type="TEST_UNKNOWN_EVENT",
                payload={},
                deduplication_key=f"test-stale:{event_id}",
                status=OutboxStatus.PROCESSING,
                attempts=0,
                available_at=utcnow(),
                locked_at=utcnow() - timedelta(seconds=60),
            )
        )
        db.commit()

    assert worker.run_once() is True

    with session_factory() as db:
        event = db.get(OutboxEvent, event_id)
        assert event is not None
        assert event.status == OutboxStatus.COMPLETED
        assert event.attempts == 2
        assert event.last_error_code == "WORKER_LEASE_EXPIRED"


def test_outbox_worker_does_not_retry_a_disabled_conference_judge() -> None:
    worker, session_factory = worker_with_memory_database()
    event_id = uuid.uuid4()
    with session_factory() as db:
        db.add(
            OutboxEvent(
                id=event_id,
                aggregate_type="CONFERENCE_MATCH",
                aggregate_id=uuid.uuid4(),
                event_type="CONFERENCE_MATCH_JUDGMENT_REQUESTED",
                payload={},
                deduplication_key=f"test-disabled-judge:{event_id}",
                status=OutboxStatus.PENDING,
                attempts=0,
                available_at=utcnow(),
            )
        )
        db.commit()

    assert worker.run_once() is True

    with session_factory() as db:
        event = db.scalar(select(OutboxEvent).where(OutboxEvent.id == event_id))
        assert event is not None
        assert event.status == OutboxStatus.FAILED
        assert event.attempts == 1
        assert event.last_error_code == "CONFERENCE_JUDGE_DISABLED"
        assert event.processed_at is not None
