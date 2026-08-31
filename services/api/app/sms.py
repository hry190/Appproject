from __future__ import annotations

import uuid
from typing import Protocol

from app.core.config import Settings


class SmsProvider(Protocol):
    def send_code(self, *, phone: str, code: str, purpose: str) -> str: ...

    def send_security_notice(self, *, phone: str, event: str) -> str: ...


class NoopSmsProvider:
    """Local/test provider. It deliberately does not print or return the code."""

    def send_code(self, *, phone: str, code: str, purpose: str) -> str:
        del phone, code, purpose
        return f"noop-{uuid.uuid4()}"

    def send_security_notice(self, *, phone: str, event: str) -> str:
        del phone, event
        return f"noop-{uuid.uuid4()}"


def build_sms_provider(settings: Settings) -> SmsProvider:
    if settings.sms_provider == "noop":
        return NoopSmsProvider()
    raise RuntimeError(
        f"SMS provider '{settings.sms_provider}' was selected but its adapter is not configured"
    )
