from __future__ import annotations

import re

from app.core.errors import ApiError


_PUBLIC_TEXT_PATTERNS: tuple[tuple[str, re.Pattern[str]], ...] = (
    ("PHONE_NUMBER", re.compile(r"(?<!\d)1[3-9]\d{9}(?!\d)")),
    (
        "EMAIL_ADDRESS",
        re.compile(r"(?i)(?<![\w.+-])[\w.+-]+@[\w-]+(?:\.[\w-]+)+"),
    ),
    ("EXTERNAL_URL", re.compile(r"(?i)\b(?:https?://|www\.)\S+")),
    (
        "DIRECT_CONTACT_HANDLE",
        re.compile(r"(?i)(?:微信|微\s*信|v\s*x|qq)\s*[:：号]?\s*[a-z0-9_-]{5,}"),
    ),
)


def ensure_public_interaction_text(*, field: str, value: str | None) -> None:
    """Reject direct-contact details before public or peer-visible text is stored."""
    if value is None:
        return
    reasons = [code for code, pattern in _PUBLIC_TEXT_PATTERNS if pattern.search(value)]
    if not reasons:
        return
    raise ApiError(
        422,
        "INTERACTION_TEXT_PRIVACY_RISK",
        "内容可能包含联系方式或外部链接，请删除后重试",
        details=[{"field": field, "reason": reason} for reason in reasons],
    )
