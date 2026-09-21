"""Disposable local API for the Android acceptance build. All data is lost on exit."""
import argparse
from pathlib import Path
import tempfile
import threading
import time

from sqlalchemy import create_engine, event
from sqlalchemy.engine import URL
from sqlalchemy import select
import uvicorn

from app.core.config import Settings
from app.db import Base
from app.domains.catalog.seed import seed_catalog_data
from app.domains.creations.image_generation_service import ImageGenerationService
from app.domains.media.contracts import InternalMediaProcessRequest
from app.domains.media.models import OutboxEvent, OutboxStatus
from app.domains.media.models import MediaScanOutcome
from app.domains.media.service import MediaService
from app.main import create_app
from app.sms import NoopSmsProvider
from app.stores import InMemoryRateLimiter, InMemoryVerificationStore


def serve(database_path: Path, *, live_coach: bool = False) -> None:
    settings = Settings(
        _env_file=None if not live_coach else ".env",
        environment="development" if live_coach else "test",
        database_url="sqlite+pysqlite://",
        media_storage_provider="memory",
        media_virus_scanner="development",
        media_memory_public_base_url="http://10.0.2.2:8011",
        sms_provider="noop",
        fixed_verification_code="123456",
    )
    if live_coach and settings.conversation_coach_provider == "disabled":
        raise RuntimeError("Live coach mode requires a configured model provider")
    if live_coach and settings.image_generation_provider == "development":
        # A real-model demo must never present the deterministic test renderer as
        # an AI-created work. Keep dialogue available, but require a separately
        # configured external image provider before the creation action can run.
        settings = settings.model_copy(update={"image_generation_provider": "disabled"})
    # Each request needs its own connection: StaticPool shares one connection across
    # concurrent Android reads and can corrupt an in-flight cursor's result shape.
    engine = create_engine(
        URL.create("sqlite+pysqlite", database=str(database_path)),
        connect_args={"check_same_thread": False, "timeout": 30},
        pool_pre_ping=True,
    )

    @event.listens_for(engine, "connect")
    def configure_sqlite(connection, _record) -> None:
        cursor = connection.cursor()
        cursor.execute("PRAGMA journal_mode=WAL")
        cursor.execute("PRAGMA synchronous=NORMAL")
        cursor.execute("PRAGMA busy_timeout=30000")
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()
    Base.metadata.create_all(engine)
    app = create_app(settings=settings, engine=engine, sms_provider=NoopSmsProvider(),
                     verification_store=InMemoryVerificationStore(), rate_limiter=InMemoryRateLimiter())
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    stop_worker = threading.Event()

    def run_local_worker() -> None:
        """Process image jobs without Redis or direct business-row edits."""
        while not stop_worker.is_set():
            with app.state.session_factory() as db:
                event = db.scalar(
                    select(OutboxEvent)
                    .where(
                        OutboxEvent.event_type.in_([
                            "MEDIA_PROCESS_REQUESTED",
                            "IMAGE_GENERATION_REQUESTED",
                        ]),
                        OutboxEvent.status == OutboxStatus.PENDING,
                    )
                    .order_by(OutboxEvent.created_at)
                    .limit(1)
                )
                if event is not None:
                    media = MediaService(
                        db=db,
                        settings=settings,
                        store=app.state.object_store,
                        virus_scanner=app.state.virus_scanner,
                        request_id=f"acceptance:{event.id}",
                    )
                    if event.event_type == "MEDIA_PROCESS_REQUESTED":
                        media.process_asset(
                            event.aggregate_id,
                            InternalMediaProcessRequest(
                                content_safety_outcome=MediaScanOutcome.PASSED,
                            ),
                        )
                    else:
                        ImageGenerationService(
                            db=db,
                            settings=settings,
                            generator=app.state.image_generator,
                            media_service=media,
                            request_id=f"acceptance:{event.id}",
                        ).process_job(event.aggregate_id)
                    continue
            time.sleep(0.15)

    worker = threading.Thread(target=run_local_worker, name="acceptance-image-worker", daemon=True)
    worker.start()
    coach_label = "configured live conversation coach" if live_coach else "test conversation coach"
    image_label = (
        "configured external image provider"
        if settings.image_generation_provider in {"openai", "volcengine"}
        else "external image provider not configured"
        if live_coach
        else "deterministic test image worker"
    )
    print(
        "Disposable acceptance API: 127.0.0.1:8011; temporary SQLite, memory media, "
        f"noop SMS, {image_label}, {coach_label}",
        flush=True,
    )
    try:
        uvicorn.run(
            app,
            host="127.0.0.1",
            port=8011,
            log_level="warning",
            timeout_keep_alive=30,
        )
    finally:
        stop_worker.set()
        worker.join(timeout=3)
        engine.dispose()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--live-coach",
        "--live-models",
        dest="live_coach",
        action="store_true",
        help=(
            "Load real model providers from services/api/.env; the local test image "
            "renderer is disabled unless an external image provider is configured"
        ),
    )
    args = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix="jianghu-acceptance-") as directory:
        target = Path(directory).resolve()
        assert target.parent == Path(tempfile.gettempdir()).resolve()
        assert target.name.startswith("jianghu-acceptance-")
        serve(target / "acceptance.db", live_coach=args.live_coach)


if __name__ == "__main__":
    main()
