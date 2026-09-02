from __future__ import annotations

import hmac
from collections.abc import Iterator

from fastapi import Depends, Header, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import PasswordService, PhoneProtector, TokenService, VerificationCodeDigester
from app.db import get_db
from app.domains.catalog.service import CatalogService
from app.domains.creations.service import CreationService
from app.domains.learning.service import LearningService
from app.domains.luggage.service import LuggageService
from app.domains.media.service import MediaService
from app.domains.moderation.service import ModerationService
from app.domains.mistakes.service import MistakeService
from app.domains.profiles.service import ProfileService
from app.domains.privacy.service import PrivacyService
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


def get_profile_service(db: Session = Depends(get_db)) -> Iterator[ProfileService]:
    yield ProfileService(db=db)


def get_catalog_service(db: Session = Depends(get_db)) -> Iterator[CatalogService]:
    yield CatalogService(db=db)


def get_creation_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[CreationService]:
    yield CreationService(
        db=db,
        request_id=getattr(request.state, "request_id", "unknown"),
    )


def get_luggage_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[LuggageService]:
    yield LuggageService(
        db=db,
        settings=request.app.state.settings,
        store=request.app.state.object_store,
        cache=request.app.state.luggage_cache,
    )


def get_media_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[MediaService]:
    yield MediaService(
        db=db,
        settings=request.app.state.settings,
        store=request.app.state.object_store,
        virus_scanner=request.app.state.virus_scanner,
        request_id=getattr(request.state, "request_id", "unknown"),
    )


def get_moderation_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[ModerationService]:
    yield ModerationService(
        db=db,
        request_id=getattr(request.state, "request_id", "unknown"),
    )


def get_privacy_service(
    request: Request, db: Session = Depends(get_db)
) -> Iterator[PrivacyService]:
    yield PrivacyService(
        db=db,
        request_id=getattr(request.state, "request_id", "unknown"),
    )


def require_internal_worker(
    request: Request,
    supplied_token: str | None = Header(default=None, alias="X-Internal-Token"),
) -> None:
    expected = request.app.state.settings.internal_worker_token.get_secret_value()
    if supplied_token is None or not hmac.compare_digest(supplied_token, expected):
        raise ApiError(404, "NOT_FOUND", "接口不存在")


def get_learning_service(db: Session = Depends(get_db)) -> Iterator[LearningService]:
    yield LearningService(db=db)


def get_mistake_service(db: Session = Depends(get_db)) -> Iterator[MistakeService]:
    yield MistakeService(db=db)
