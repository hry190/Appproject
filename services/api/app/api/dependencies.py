from __future__ import annotations

from collections.abc import Iterator

from fastapi import Depends, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import PasswordService, PhoneProtector, TokenService, VerificationCodeDigester
from app.db import get_db
from app.models import User, UserStatus
from app.services.auth import AuthService
from app.services.user_settings import UserSettingsService


bearer_scheme = HTTPBearer(auto_error=False)


def get_auth_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[AuthService]:
    yield AuthService(
        db=db,
        settings=request.app.state.settings,
        verification_store=request.app.state.verification_store,
        rate_limiter=request.app.state.rate_limiter,
        sms_provider=request.app.state.sms_provider,
        phone_protector=request.app.state.phone_protector,
        password_service=request.app.state.password_service,
        token_service=request.app.state.token_service,
        code_digester=request.app.state.code_digester,
    )


def get_current_user(
    request: Request,
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
    db: Session = Depends(get_db),
) -> User:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise ApiError(401, "AUTHENTICATION_REQUIRED", "请先登录")
    claims = request.app.state.token_service.decode_access_token(credentials.credentials)
    user = db.scalar(select(User).where(User.id == claims.user_id))
    if (
        user is None
        or user.status != UserStatus.ACTIVE
        or user.token_version != claims.token_version
    ):
        raise ApiError(401, "INVALID_ACCESS_TOKEN", "登录状态无效或已过期")
    return user


def get_user_settings_service(
    request: Request,
    db: Session = Depends(get_db),
) -> Iterator[UserSettingsService]:
    yield UserSettingsService(
        db=db,
        phone_protector=request.app.state.phone_protector,
    )
