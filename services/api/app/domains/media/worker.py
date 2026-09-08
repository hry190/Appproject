from __future__ import annotations

import time
from datetime import timedelta

from redis import Redis
from sqlalchemy import and_, or_, select

from app.core.config import get_settings
from app.core.security import utcnow
from app.db import build_engine, build_session_factory
from app.domains.luggage.cache import RedisLuggageCache
from app.domains.conference.judge import (
    ConferenceJudgeProviderError,
    build_conference_judge,
)
from app.domains.conference.service import ConferenceService
from app.domains.creations.image_generation import build_image_generator
from app.domains.creations.image_generation_service import ImageGenerationService
from app.domains.creations.export_service import CreationExportService
from app.domains.distribution.service import DistributionService
from app.domains.media.contracts import InternalMediaProcessRequest
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.media.service import MediaService
from app.domains.media.storage import build_object_store
from app.domains.media.virus import build_virus_scanner
from app.domains.moderation.service import ModerationService
from app.core.security import PhoneProtector
from app.services.user_settings import UserSettingsService


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
        self.image_generator = build_image_generator(self.settings)
        self.conference_judge = build_conference_judge(self.settings)
        self.phone_protector = PhoneProtector(self.settings)

    def run_once(self) -> bool:
        with self.session_factory() as db:
            now = utcnow()
            stale_before = now - timedelta(seconds=self.settings.outbox_lease_seconds)
            event = db.scalar(
                select(OutboxEvent)
                .where(
                    or_(
                        and_(
                            OutboxEvent.status == OutboxStatus.PENDING,
                            OutboxEvent.available_at <= now,
                        ),
                        and_(
                            OutboxEvent.status == OutboxStatus.PROCESSING,
                            OutboxEvent.locked_at.is_not(None),
                            OutboxEvent.locked_at <= stale_before,
                        ),
                    )
                )
                .order_by(OutboxEvent.created_at)
                .with_for_update(skip_locked=True)
                .limit(1)
            )
            if event is None:
                return False
            if event.status == OutboxStatus.PROCESSING:
                event.attempts += 1
                event.last_error_code = "WORKER_LEASE_EXPIRED"
                if event.attempts >= self.settings.outbox_max_attempts:
                    event.status = OutboxStatus.FAILED
                    event.processed_at = now
                    db.commit()
                    return True
            event.status = OutboxStatus.PROCESSING
            event.locked_at = now
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
                elif event_type == "IMAGE_GENERATION_REQUESTED":
                    media_service = MediaService(
                        db=db,
                        settings=self.settings,
                        store=self.store,
                        virus_scanner=self.virus_scanner,
                        request_id=request_id,
                    )
                    ImageGenerationService(
                        db=db,
                        settings=self.settings,
                        generator=self.image_generator,
                        media_service=media_service,
                        request_id=request_id,
                    ).process_job(aggregate_id)
                elif event_type == "CREATION_EXPORT_REQUESTED":
                    media_service = MediaService(
                        db=db,
                        settings=self.settings,
                        store=self.store,
                        virus_scanner=self.virus_scanner,
                        request_id=request_id,
                    )
                    CreationExportService(
                        db=db,
                        store=self.store,
                        media_service=media_service,
                        request_id=request_id,
                    ).process_job(aggregate_id)
                elif event_type == "CONFERENCE_MATCH_JUDGMENT_REQUESTED":
                    if self.conference_judge is None:
                        raise ConferenceJudgeProviderError(
                            "CONFERENCE_JUDGE_DISABLED",
                            "大会切磋自动评审未启用",
                            retryable=False,
                        )
                    ConferenceService(
                        db=db,
                        distribution=DistributionService(
                            db=db,
                            settings=self.settings,
                            store=self.store,
                        ),
                        request_id=request_id,
                    ).process_match_judgment(aggregate_id, self.conference_judge)
                elif event_type == "ACCOUNT_DELETION_REQUESTED":
                    user_id = UserSettingsService(
                        db=db,
                        phone_protector=self.phone_protector,
                    ).process_account_deletion(aggregate_id)
                    if user_id is not None:
                        self.luggage_cache.invalidate_many([user_id])
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
                    event.last_error_code = str(
                        getattr(exc, "code", type(exc).__name__)
                    )[:80]
                    retryable = bool(getattr(exc, "retryable", True))
                    if (
                        not retryable
                        or event.attempts >= self.settings.outbox_max_attempts
                    ):
                        event.status = OutboxStatus.FAILED
                        event.processed_at = utcnow()
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
