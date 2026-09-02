from __future__ import annotations

import time
from datetime import timedelta

from redis import Redis
from sqlalchemy import select

from app.core.config import get_settings
from app.core.security import utcnow
from app.db import build_engine, build_session_factory
from app.domains.luggage.cache import RedisLuggageCache
from app.domains.media.contracts import InternalMediaProcessRequest
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.media.service import MediaService
from app.domains.media.storage import build_object_store
from app.domains.media.virus import build_virus_scanner
from app.domains.moderation.service import ModerationService


class OutboxWorker:
    def __init__(self) -> None:
        self.settings = get_settings()
        redis_client = Redis.from_url(self.settings.redis_url, decode_responses=True)
        self.luggage_cache = RedisLuggageCache(
            redis_client,
            prefix=self.settings.redis_prefix,
            ttl_seconds=self.settings.luggage_cache_ttl_seconds,
        )
        self.session_factory = build_session_factory(
            build_engine(self.settings.database_url),
            luggage_cache=self.luggage_cache,
        )
        self.store = build_object_store(self.settings)
        self.virus_scanner = build_virus_scanner(self.settings)

    def run_once(self) -> bool:
        with self.session_factory() as db:
            event = db.scalar(
                select(OutboxEvent)
                .where(
                    OutboxEvent.status == OutboxStatus.PENDING,
                    OutboxEvent.available_at <= utcnow(),
                )
                .order_by(OutboxEvent.created_at)
                .with_for_update(skip_locked=True)
                .limit(1)
            )
            if event is None:
                return False
            event.status = OutboxStatus.PROCESSING
            event.locked_at = utcnow()
            event_id = event.id
            event_type = event.event_type
            aggregate_id = event.aggregate_id
            db.commit()

        request_id = f"outbox:{event_id}"
        try:
            with self.session_factory() as db:
                if event_type == "MEDIA_PROCESS_REQUESTED":
                    MediaService(
                        db=db,
                        settings=self.settings,
                        store=self.store,
                        virus_scanner=self.virus_scanner,
                        request_id=request_id,
                    ).process_asset(
                        aggregate_id,
                        InternalMediaProcessRequest(),
                    )
                elif event_type == "MEDIA_DELETE_REQUESTED":
                    MediaService(
                        db=db,
                        settings=self.settings,
                        store=self.store,
                        virus_scanner=self.virus_scanner,
                        request_id=request_id,
                    ).process_deletion(aggregate_id)
                elif event_type == "MODERATION_REQUESTED":
                    ModerationService(
                        db=db,
                        request_id=request_id,
                    ).route_to_human_review(aggregate_id)
                else:
                    event = db.get(OutboxEvent, event_id)
                    if event is not None:
                        event.status = OutboxStatus.COMPLETED
                        event.processed_at = utcnow()
                        event.attempts += 1
                        db.commit()
            return True
        except Exception as exc:
            with self.session_factory() as db:
                event = db.get(OutboxEvent, event_id)
                if event is not None and event.status == OutboxStatus.PROCESSING:
                    event.attempts += 1
                    event.last_error_code = type(exc).__name__[:80]
                    if event.attempts >= 5:
                        event.status = OutboxStatus.FAILED
                    else:
                        event.status = OutboxStatus.PENDING
                        event.available_at = utcnow() + timedelta(
                            seconds=min(300, 2**event.attempts)
                        )
                    db.commit()
            return True

    def run_forever(self) -> None:
        while True:
            if not self.run_once():
                time.sleep(1)


def main() -> None:
    OutboxWorker().run_forever()


if __name__ == "__main__":
    main()
