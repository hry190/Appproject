"""Disposable local API for the Android acceptance build. All data is lost on exit."""
from pathlib import Path
import tempfile

from sqlalchemy import create_engine
from sqlalchemy.engine import URL
import uvicorn

from app.core.config import Settings
from app.db import Base
from app.domains.catalog.seed import seed_catalog_data
from app.main import create_app
from app.sms import NoopSmsProvider
from app.stores import InMemoryRateLimiter, InMemoryVerificationStore


def serve(database_path: Path):
    settings = Settings(_env_file=None, environment="test", database_url="sqlite+pysqlite://",
                        media_storage_provider="memory", media_virus_scanner="development",
                        sms_provider="noop", fixed_verification_code="123456")
    # Each request needs its own connection: StaticPool shares one connection across
    # concurrent Android reads and can corrupt an in-flight cursor's result shape.
    engine = create_engine(URL.create("sqlite+pysqlite", database=str(database_path)),
                           connect_args={"check_same_thread": False, "timeout": 30})
    Base.metadata.create_all(engine)
    app = create_app(settings=settings, engine=engine, sms_provider=NoopSmsProvider(),
                     verification_store=InMemoryVerificationStore(), rate_limiter=InMemoryRateLimiter())
    with app.state.session_factory() as db:
        seed_catalog_data(db)
    print("Disposable acceptance API: 127.0.0.1:8011; SQLite/memory, noop SMS, no worker", flush=True)
    try:
        uvicorn.run(app, host="127.0.0.1", port=8011, log_level="warning")
    finally:
        engine.dispose()


def main():
    with tempfile.TemporaryDirectory(prefix="jianghu-acceptance-") as directory:
        target = Path(directory).resolve()
        assert target.parent == Path(tempfile.gettempdir()).resolve()
        assert target.name.startswith("jianghu-acceptance-")
        serve(target / "acceptance.db")


if __name__ == "__main__":
    main()
