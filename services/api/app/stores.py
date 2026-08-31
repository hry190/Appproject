from __future__ import annotations

import hmac
import json
import threading
import time
from dataclasses import dataclass
from typing import Protocol

from redis import Redis
from redis.exceptions import WatchError


@dataclass(slots=True)
class VerificationRecord:
    digest: str
    attempts_remaining: int


class VerificationStore(Protocol):
    def issue(self, key: str, digest: str, *, ttl_seconds: int, attempts: int) -> None: ...

    def verify_and_consume(self, key: str, digest: str) -> bool: ...

    def delete(self, key: str) -> None: ...


class RateLimiter(Protocol):
    def hit(self, key: str, *, limit: int, window_seconds: int) -> tuple[bool, int]: ...


class InMemoryVerificationStore:
    def __init__(self) -> None:
        self._records: dict[str, tuple[VerificationRecord, float]] = {}
        self._lock = threading.RLock()

    def issue(self, key: str, digest: str, *, ttl_seconds: int, attempts: int) -> None:
        with self._lock:
            self._records[key] = (
                VerificationRecord(digest=digest, attempts_remaining=attempts),
                time.monotonic() + ttl_seconds,
            )

    def verify_and_consume(self, key: str, digest: str) -> bool:
        with self._lock:
            item = self._records.get(key)
            if item is None:
                return False
            record, expires_at = item
            if time.monotonic() >= expires_at:
                self._records.pop(key, None)
                return False
            if hmac.compare_digest(record.digest, digest):
                self._records.pop(key, None)
                return True
            record.attempts_remaining -= 1
            if record.attempts_remaining <= 0:
                self._records.pop(key, None)
            return False

    def delete(self, key: str) -> None:
        with self._lock:
            self._records.pop(key, None)


class InMemoryRateLimiter:
    def __init__(self) -> None:
        self._counters: dict[str, tuple[int, float]] = {}
        self._lock = threading.RLock()

    def hit(self, key: str, *, limit: int, window_seconds: int) -> tuple[bool, int]:
        now = time.monotonic()
        with self._lock:
            count, expires_at = self._counters.get(key, (0, now + window_seconds))
            if now >= expires_at:
                count, expires_at = 0, now + window_seconds
            count += 1
            self._counters[key] = (count, expires_at)
            retry_after = max(1, int(expires_at - now))
            return count <= limit, retry_after


class RedisVerificationStore:
    def __init__(self, client: Redis, *, prefix: str) -> None:
        self._client = client
        self._prefix = prefix

    def _key(self, key: str) -> str:
        return f"{self._prefix}:verification:{key}"

    def issue(self, key: str, digest: str, *, ttl_seconds: int, attempts: int) -> None:
        payload = json.dumps({"digest": digest, "attempts": attempts})
        self._client.setex(self._key(key), ttl_seconds, payload)

    def verify_and_consume(self, key: str, digest: str) -> bool:
        redis_key = self._key(key)
        for _ in range(5):
            with self._client.pipeline() as pipe:
                try:
                    pipe.watch(redis_key)
                    raw = pipe.get(redis_key)
                    if raw is None:
                        return False
                    ttl = pipe.ttl(redis_key)
                    data = json.loads(raw)
                    pipe.multi()
                    if hmac.compare_digest(data["digest"], digest):
                        pipe.delete(redis_key)
                        pipe.execute()
                        return True
                    attempts = int(data["attempts"]) - 1
                    if attempts <= 0 or ttl <= 0:
                        pipe.delete(redis_key)
                    else:
                        pipe.setex(
                            redis_key,
                            ttl,
                            json.dumps({"digest": data["digest"], "attempts": attempts}),
                        )
                    pipe.execute()
                    return False
                except WatchError:
                    continue
        return False

    def delete(self, key: str) -> None:
        self._client.delete(self._key(key))


class RedisRateLimiter:
    def __init__(self, client: Redis, *, prefix: str) -> None:
        self._client = client
        self._prefix = prefix

    def hit(self, key: str, *, limit: int, window_seconds: int) -> tuple[bool, int]:
        redis_key = f"{self._prefix}:limit:{key}"
        with self._client.pipeline() as pipe:
            pipe.incr(redis_key)
            pipe.ttl(redis_key)
            count, ttl = pipe.execute()
        if count == 1 or ttl < 0:
            self._client.expire(redis_key, window_seconds)
            ttl = window_seconds
        return int(count) <= limit, max(1, int(ttl))
