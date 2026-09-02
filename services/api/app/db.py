from __future__ import annotations

import logging
import uuid
from collections.abc import Iterator

from fastapi import Request
from sqlalchemy import MetaData, create_engine, event
from sqlalchemy.engine import Engine
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker


NAMING_CONVENTION = {
    "ix": "ix_%(column_0_label)s",
    "uq": "uq_%(table_name)s_%(column_0_name)s",
    "ck": "ck_%(table_name)s_%(constraint_name)s",
    "fk": "fk_%(table_name)s_%(column_0_name)s_%(referred_table_name)s",
    "pk": "pk_%(table_name)s",
}

logger = logging.getLogger(__name__)

_CACHE_INFO_KEY = "luggage_cache"
_PENDING_USERS_KEY = "luggage_cache_pending_user_ids"
_CLEAR_ALL_KEY = "luggage_cache_clear_all"

# Only tables whose committed state is represented by GET /v1/me/luggage belong here.
_USER_FIELD_BY_TABLE = {
    "users": "id",
    "user_profiles": "user_id",
    "user_titles": "user_id",
    "user_badges": "user_id",
    "guardian_controls": "child_user_id",
    "practice_sessions": "user_id",
    "trial_attempts": "user_id",
    "learning_events": "user_id",
    "learning_evidence": "user_id",
    "manual_progress": "user_id",
    "progress_transitions": "user_id",
    "user_learning_stats": "user_id",
    "mistake_items": "user_id",
    "remediation_contexts": "user_id",
    "creation_projects": "owner_user_id",
    "creation_versions": "created_by_user_id",
    "publications": "owner_user_id",
    "upload_sessions": "owner_user_id",
    "media_assets": "owner_user_id",
    "moderation_cases": "owner_user_id",
    "moderation_appeals": "appellant_user_id",
    "privacy_settings": "user_id",
}
_GLOBAL_TABLES = {
    "manual_volumes",
    "manual_pages",
    "title_definitions",
    "badge_definitions",
}


class Base(DeclarativeBase):
    metadata = MetaData(naming_convention=NAMING_CONVENTION)


def build_engine(database_url: str) -> Engine:
    connect_args = {"check_same_thread": False} if database_url.startswith("sqlite") else {}
    return create_engine(database_url, pool_pre_ping=True, connect_args=connect_args)


def build_session_factory(
    engine: Engine,
    *,
    luggage_cache: object | None = None,
) -> sessionmaker[Session]:
    info = {_CACHE_INFO_KEY: luggage_cache} if luggage_cache is not None else None
    return sessionmaker(
        bind=engine,
        expire_on_commit=False,
        autoflush=False,
        info=info,
    )


def get_db(request: Request) -> Iterator[Session]:
    session: Session = request.app.state.session_factory()
    session.info[_CACHE_INFO_KEY] = request.app.state.luggage_cache
    try:
        yield session
    finally:
        session.close()


@event.listens_for(Session, "before_flush")
def collect_luggage_cache_invalidations(
    session: Session,
    _flush_context: object,
    _instances: object,
) -> None:
    if session.info.get(_CACHE_INFO_KEY) is None:
        return
    pending: set[uuid.UUID] = session.info.setdefault(_PENDING_USERS_KEY, set())
    for instance in session.new | session.dirty | session.deleted:
        table = getattr(instance, "__tablename__", None)
        if table in _GLOBAL_TABLES:
            session.info[_CLEAR_ALL_KEY] = True
            continue
        field = _USER_FIELD_BY_TABLE.get(table)
        if field is None:
            continue
        user_id = getattr(instance, field, None)
        if isinstance(user_id, uuid.UUID):
            pending.add(user_id)


@event.listens_for(Session, "after_commit")
def invalidate_luggage_cache_after_commit(session: Session) -> None:
    cache = session.info.get(_CACHE_INFO_KEY)
    pending = session.info.pop(_PENDING_USERS_KEY, set())
    clear_all = session.info.pop(_CLEAR_ALL_KEY, False)
    if cache is None:
        return
    try:
        if clear_all:
            cache.invalidate_all()
        elif pending:
            cache.invalidate_many(pending)
    except Exception as exc:  # Cache availability must never change commit semantics.
        logger.warning("luggage cache post-commit invalidation failed: %s", type(exc).__name__)


@event.listens_for(Session, "after_rollback")
def discard_luggage_cache_invalidations(session: Session) -> None:
    session.info.pop(_PENDING_USERS_KEY, None)
    session.info.pop(_CLEAR_ALL_KEY, None)
