from __future__ import annotations

import logging
import threading
import time
import uuid
from collections.abc import Iterable
from dataclasses import dataclass
from typing import Protocol

from redis import Redis
from redis.exceptions import RedisError

from app.domains.luggage.contracts import LuggageResponse


logger = logging.getLogger(__name__)


class LuggageCache(Protocol):
    """Short-lived cache for the complete private luggage snapshot."""

    def get(self, user_id: uuid.UUID) -> LuggageResponse | None: ...

    def set(self, user_id: uuid.UUID, value: LuggageResponse) -> None: ...

    def invalidate_many(self, user_ids: Iterable[uuid.UUID]) -> None: ...

    def invalidate_all(self) -> None: ...


@dataclass(frozen=True)
class LuggageCacheStats:
    hits: int
    misses: int
    writes: int
    invalidations: int


class InMemoryLuggageCache:
    """Thread-safe test cache with the same TTL semantics as Redis."""

    def __init__(self, *, ttl_seconds: int) -> None:
        self.ttl_seconds = ttl_seconds
        self._entries: dict[uuid.UUID, tuple[float, str]] = {}
        self._lock = threading.Lock()
        self._hits = 0
        self._misses = 0
        self._writes = 0
        self._invalidations = 0

    @property
    def stats(self) -> LuggageCacheStats:
        with self._lock:
            return LuggageCacheStats(
                hits=self._hits,
                misses=self._misses,
                writes=self._writes,
                invalidations=self._invalidations,
            )

    def get(self, user_id: uuid.UUID) -> LuggageResponse | None:
        now = time.monotonic()
        with self._lock:
            entry = self._entries.get(user_id)
            if entry is None or entry[0] <= now:
                if entry is not None:
                    self._entries.pop(user_id, None)
                self._misses += 1
                return None
            self._hits += 1
            payload = entry[1]
        return LuggageResponse.model_validate_json(payload)

    def set(self, user_id: uuid.UUID, value: LuggageResponse) -> None:
        payload = value.model_dump_json()
        with self._lock:
            self._entries[user_id] = (
                time.monotonic() + self.ttl_seconds,
                payload,
            )
            self._writes += 1

    def invalidate_many(self, user_ids: Iterable[uuid.UUID]) -> None:
        unique_ids = set(user_ids)
        with self._lock:
            for user_id in unique_ids:
                self._entries.pop(user_id, None)
            self._invalidations += len(unique_ids)

    def invalidate_all(self) -> None:
        with self._lock:
            count = len(self._entries)
            self._entries.clear()
            self._invalidations += count


class RedisLuggageCache:
    """Redis-backed cache that always fails open when Redis is unavailable."""

    def __init__(
        self,
        client: Redis,
        *,
        prefix: str,
        ttl_seconds: int,
    ) -> None:
        self.client = client
        self.key_prefix = f"{prefix}:luggage:v1:"
        self.ttl_seconds = ttl_seconds

    def get(self, user_id: uuid.UUID) -> LuggageResponse | None:
        try:
            payload = self.client.get(self._key(user_id))
            if payload is None:
                return None
            return LuggageResponse.model_validate_json(payload)
        except (RedisError, ValueError, TypeError) as exc:
            logger.warning("luggage cache read failed: %s", type(exc).__name__)
            return None

    def set(self, user_id: uuid.UUID, value: LuggageResponse) -> None:
        try:
            self.client.setex(
                self._key(user_id),
                self.ttl_seconds,
                value.model_dump_json(),
            )
        except RedisError as exc:
            logger.warning("luggage cache write failed: %s", type(exc).__name__)

    def invalidate_many(self, user_ids: Iterable[uuid.UUID]) -> None:
        keys = [self._key(user_id) for user_id in set(user_ids)]
        if not keys:
            return
        try:
            self.client.delete(*keys)
        except RedisError as exc:
            logger.warning("luggage cache invalidation failed: %s", type(exc).__name__)

    def invalidate_all(self) -> None:
        try:
            batch: list[str] = []
            for key in self.client.scan_iter(match=f"{self.key_prefix}*", count=200):
                batch.append(key)
                if len(batch) >= 200:
                    self.client.delete(*batch)
                    batch.clear()
            if batch:
                self.client.delete(*batch)
        except RedisError as exc:
            logger.warning("luggage cache global invalidation failed: %s", type(exc).__name__)

    def _key(self, user_id: uuid.UUID) -> str:
        return f"{self.key_prefix}{user_id}"
