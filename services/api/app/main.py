from __future__ import annotations

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from redis import Redis
from sqlalchemy.engine import Engine
from starlette.middleware.trustedhost import TrustedHostMiddleware

from app.api.routes.account import router as account_router
from app.api.routes.auth import router as auth_router
from app.api.routes.health import router as health_router
from app.api.routes.settings import router as settings_router
from app.api.routes.support import router as support_router
from app.core.config import Settings, get_settings
from app.core.errors import ApiError, api_error_handler, validation_error_handler
from app.core.middleware import RequestContextMiddleware, RequestSizeLimitMiddleware
from app.core.security import (
    PasswordService,
    PhoneProtector,
    TokenService,
    VerificationCodeDigester,
)
from app.db import build_engine, build_session_factory
from app.sms import SmsProvider, build_sms_provider
from app.stores import (
    RateLimiter,
    RedisRateLimiter,
    RedisVerificationStore,
    VerificationStore,
)


def create_app(
    *,
    settings: Settings | None = None,
    engine: Engine | None = None,
    verification_store: VerificationStore | None = None,
    rate_limiter: RateLimiter | None = None,
    sms_provider: SmsProvider | None = None,
) -> FastAPI:
    resolved = settings or get_settings()
    docs_enabled = resolved.environment != "production"
    application = FastAPI(
        title=resolved.app_name,
        version="0.1.0",
        debug=False,
        docs_url="/docs" if docs_enabled else None,
        redoc_url="/redoc" if docs_enabled else None,
        openapi_url="/openapi.json" if docs_enabled else None,
    )

    resolved_engine = engine or build_engine(resolved.database_url)
    application.state.settings = resolved
    application.state.engine = resolved_engine
    application.state.session_factory = build_session_factory(resolved_engine)
    application.state.phone_protector = PhoneProtector(resolved)
    application.state.password_service = PasswordService()
    application.state.token_service = TokenService(resolved)
    application.state.code_digester = VerificationCodeDigester(resolved)
    application.state.sms_provider = sms_provider or build_sms_provider(resolved)

    if verification_store is None or rate_limiter is None:
        redis_client = Redis.from_url(resolved.redis_url, decode_responses=True)
        application.state.redis_client = redis_client
        verification_store = verification_store or RedisVerificationStore(
            redis_client, prefix=resolved.redis_prefix
        )
        rate_limiter = rate_limiter or RedisRateLimiter(
            redis_client, prefix=resolved.redis_prefix
        )
    application.state.verification_store = verification_store
    application.state.rate_limiter = rate_limiter

    application.add_exception_handler(ApiError, api_error_handler)
    application.add_exception_handler(RequestValidationError, validation_error_handler)

    application.include_router(health_router)
    application.include_router(auth_router)
    application.include_router(settings_router)
    application.include_router(support_router)
    application.include_router(account_router)

    application.add_middleware(
        RequestSizeLimitMiddleware, max_bytes=resolved.max_request_bytes
    )
    application.add_middleware(RequestContextMiddleware)
    application.add_middleware(
        TrustedHostMiddleware, allowed_hosts=resolved.allowed_hosts
    )
    return application


app = create_app()
