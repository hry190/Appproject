from __future__ import annotations

from fastapi import APIRouter, Depends, Request, Response, status

from app.api.dependencies import get_auth_service, get_current_user
from app.models import User
from app.schemas import (
    AuthResponse,
    GuardianConsentRequest,
    GuardianConsentResponse,
    LogoutRequest,
    PasswordLoginRequest,
    PasswordResetRequest,
    PasswordResetResponse,
    RefreshRequest,
    RegisterRequest,
    StatusResponse,
    TokenPair,
    UserPublic,
    VerificationCodeAccepted,
    VerificationCodeRequest,
)
from app.services.auth import AuthService


router = APIRouter(prefix="/v1/auth", tags=["auth"])


def _request_id(request: Request) -> str:
    return request.state.request_id


def _client_ip(request: Request) -> str:
    return request.client.host if request.client else "unknown"


@router.post(
    "/verification-codes",
    response_model=VerificationCodeAccepted,
    status_code=status.HTTP_202_ACCEPTED,
)
def request_verification_code(
    payload: VerificationCodeRequest,
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> VerificationCodeAccepted:
    return service.request_verification_code(
        payload, request_id=_request_id(request), client_ip=_client_ip(request)
    )


@router.post(
    "/guardian-consents/verify", response_model=GuardianConsentResponse
)
def verify_guardian_consent(
    payload: GuardianConsentRequest,
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> GuardianConsentResponse:
    return service.verify_guardian_consent(
        payload, request_id=_request_id(request), client_ip=_client_ip(request)
    )


@router.post(
    "/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED
)
def register(
    payload: RegisterRequest,
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> AuthResponse:
    return service.register(
        payload, request_id=_request_id(request), client_ip=_client_ip(request)
    )


@router.post("/login/password", response_model=AuthResponse)
def password_login(
    payload: PasswordLoginRequest,
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> AuthResponse:
    return service.login(
        payload, request_id=_request_id(request), client_ip=_client_ip(request)
    )


@router.post("/token/refresh", response_model=TokenPair)
def refresh_token(
    payload: RefreshRequest,
    service: AuthService = Depends(get_auth_service),
) -> TokenPair:
    return service.refresh(payload.refresh_token, device_name=payload.device_name)


@router.post("/password/reset", response_model=PasswordResetResponse)
def reset_password(
    payload: PasswordResetRequest,
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> PasswordResetResponse:
    return service.reset_password(
        payload, request_id=_request_id(request), client_ip=_client_ip(request)
    )


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(
    payload: LogoutRequest,
    service: AuthService = Depends(get_auth_service),
) -> Response:
    service.logout(payload.refresh_token)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post("/logout-all", response_model=StatusResponse)
def logout_all(
    user: User = Depends(get_current_user),
    service: AuthService = Depends(get_auth_service),
) -> StatusResponse:
    service.logout_all(user)
    return StatusResponse(status="LOGGED_OUT_ALL_SESSIONS")


@router.get("/me", response_model=UserPublic)
def me(
    request: Request,
    user: User = Depends(get_current_user),
) -> UserPublic:
    normalized_phone = request.app.state.phone_protector.decrypt(user.phone_ciphertext)
    return UserPublic(
        id=user.id,
        nickname=user.nickname,
        phone_masked=request.app.state.phone_protector.mask(normalized_phone),
        status=user.status,
        age_band=user.age_band,
        guardian_status=user.guardian_status,
    )
