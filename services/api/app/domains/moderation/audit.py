from __future__ import annotations

import uuid

from sqlalchemy.orm import Session

from app.domains.moderation.models import DomainAuditEvent


def add_audit_event(
    db: Session,
    *,
    actor_user_id: uuid.UUID | None,
    actor_type: str,
    action: str,
    target_type: str,
    target_id: uuid.UUID,
    result: str,
    request_id: str,
    safe_diff: dict,
) -> None:
    db.add(
        DomainAuditEvent(
            actor_user_id=actor_user_id,
            actor_type=actor_type,
            action=action,
            target_type=target_type,
            target_id=target_id,
            result=result,
            request_id=request_id,
            safe_diff=safe_diff,
        )
    )
