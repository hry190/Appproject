from __future__ import annotations

import secrets
import uuid
from datetime import UTC, datetime, timedelta

from sqlalchemy import select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, joinedload

from app.core.config import Settings
from app.core.errors import ApiError
from app.core.security import (
    PasswordService,
    PhoneProtector,
    TokenService,
    VerificationCodeDigester,
    utcnow,
)
from app.models import (
    AgeBand,
    AuthAuditEvent,
    AuthSession,
    ConsentRecord,
    ConsentSubject,
    ConsentType,
    Credential,
    GuardianLink,
    GuardianStatus,
    User,
    UserStatus,
)
from app.schemas import (
    AuthResponse,
    GuardianConsentRequest,
    GuardianConsentResponse,
    PasswordLoginRequest,
    PasswordResetRequest,
    PasswordResetResponse,
    RegisterRequest,
    TokenPair,
    UserPublic,
    VerificationCodeAccepted,
    VerificationCodeRequest,
    VerificationPurpose,
)
from app.sms import SmsProvider
from app.stores import RateLimiter, VerificationStore


GENERIC_LOGIN_ERROR = ApiError(
    401, "INVALID_CREDENTIALS", "手机号或密码不正确"
)


def _aware(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value.replace(tzinfo=UTC)
    return value.astimezone(UTC)


class AuthService:
    def __init__(
        self,
        *,
        db: Session,
        settings: Settings,
        verification_store: VerificationStore,
        rate_limiter: RateLimiter,
        sms_provider: SmsProvider,
        phone_protector: PhoneProtector,
        password_service: PasswordService,
        token_service: TokenService,
        code_digester: VerificationCodeDigester,
    ) -> None:
        self.db = db
        self.settings = settings
        self.verification_store = verification_store
        self.rate_limiter = rate_limiter
        self.sms_provider = sms_provider
        self.phone = phone_protector
        self.passwords = password_service
        self.tokens = token_service
        self.code_digester = code_digester

    def request_verification_code(
        self,
        payload: VerificationCodeRequest,
        *,
        request_id: str,
        client_ip: str,
    ) -> VerificationCodeAccepted:
        normalized = self.phone.normalize(payload.phone)
        phone_hash = self.phone.lookup_hash(normalized)
        purpose = payload.purpose.value
        network_key = self.phone.opaque_network_key(client_ip)

        self._enforce_limit(
            f"otp:cooldown:{purpose}:{phone_hash}",
            limit=1,
            window=self.settings.verification_code_cooldown_seconds,
        )
        self._enforce_limit(
            f"otp:daily:{purpose}:{phone_hash}",
            limit=self.settings.verification_code_daily_limit,
            window=24 * 60 * 60,
        )
        self._enforce_limit(
            f"otp:network:{network_key}",
            limit=50,
            window=60 * 60,
        )

        code = self._new_verification_code()
        key = self._verification_key(payload.purpose, phone_hash)
        digest = self.code_digester.digest(
            phone_hash=phone_hash, purpose=purpose, code=code
        )
        self.verification_store.issue(
            key,
            digest,
            ttl_seconds=self.settings.verification_code_ttl_seconds,
            attempts=self.settings.verification_code_attempts,
        )
        try:
            self.sms_provider.send_code(phone=normalized, code=code, purpose=purpose)
        except Exception as exc:
            self.verification_store.delete(key)
            raise ApiError(503, "SMS_DELIVERY_FAILED", "验证码暂时无法发送，请稍后再试") from exc

        self._audit(
            event_type="VERIFICATION_CODE_REQUESTED",
            result="ACCEPTED",
            request_id=request_id,
            phone_hash=phone_hash,
            network_key=network_key,
        )
        self.db.commit()
        return VerificationCodeAccepted(
            expires_in=self.settings.verification_code_ttl_seconds,
            retry_after=self.settings.verification_code_cooldown_seconds,
            request_id=request_id,
        )

    def verify_guardian_consent(
        self,
        payload: GuardianConsentRequest,
        *,
        request_id: str,
        client_ip: str,
    ) -> GuardianConsentResponse:
        self._require_current_consent_versions(
            payload.terms_version, payload.privacy_version
        )
        child_phone = self.phone.normalize(payload.child_phone)
        guardian_phone = self.phone.normalize(payload.guardian_phone)
        child_hash = self.phone.lookup_hash(child_phone)
        guardian_hash = self.phone.lookup_hash(guardian_phone)
        self._consume_verification_code(
            phone_hash=guardian_hash,
            purpose=VerificationPurpose.GUARDIAN_CONSENT,
            code=payload.verification_code,
        )
        token, expires_in = self.tokens.create_guardian_consent_token(
            child_phone_hash=child_hash,
            guardian_phone_hash=guardian_hash,
            terms_version=payload.terms_version,
            privacy_version=payload.privacy_version,
        )
        self._audit(
            event_type="GUARDIAN_CONSENT_VERIFIED",
            result="SUCCESS",
            request_id=request_id,
            phone_hash=child_hash,
            network_key=self.phone.opaque_network_key(client_ip),
        )
        self.db.commit()
        return GuardianConsentResponse(
            guardian_consent_token=token, expires_in=expires_in
        )

    def register(
        self,
        payload: RegisterRequest,
        *,
        request_id: str,
        client_ip: str,
    ) -> AuthResponse:
        self._require_current_consent_versions(
            payload.terms_version, payload.privacy_version
        )
        normalized = self.phone.normalize(payload.phone)
        phone_hash = self.phone.lookup_hash(normalized)

        guardian_hash: str | None = None
        guardian_evidence_id: str | None = None
        consent_subject = ConsentSubject.SELF
        if payload.age_band == AgeBand.UNDER_14:
            if not payload.guardian_consent_token:
                raise ApiError(
                    403,
                    "GUARDIAN_CONSENT_REQUIRED",
                    "不满十四周岁的用户需要先完成监护人同意",
                )
            guardian_payload = self.tokens.decode_guardian_consent_token(
                payload.guardian_consent_token
            )
            if guardian_payload.get("sub") != phone_hash:
                raise ApiError(403, "GUARDIAN_CONSENT_MISMATCH", "监护人同意与当前账号不匹配")
            if guardian_payload.get("terms") != payload.terms_version or guardian_payload.get(
                "privacy"
            ) != payload.privacy_version:
                raise ApiError(403, "CONSENT_VERSION_MISMATCH", "协议版本已更新，请重新确认")
            guardian_hash = str(guardian_payload.get("guardian", ""))
            guardian_evidence_id = str(guardian_payload.get("jti", ""))
            if len(guardian_hash) != 64 or not guardian_evidence_id:
                raise ApiError(403, "INVALID_GUARDIAN_CONSENT", "监护人同意凭证无效")
            consent_subject = ConsentSubject.GUARDIAN
        elif payload.guardian_consent_token is not None:
            raise ApiError(422, "UNEXPECTED_GUARDIAN_CONSENT", "当前年龄段不需要监护人凭证")

        self._consume_verification_code(
            phone_hash=phone_hash,
            purpose=VerificationPurpose.REGISTER,
            code=payload.verification_code,
        )

        existing = self.db.scalar(select(User.id).where(User.phone_lookup_hash == phone_hash))
        if existing is not None:
            raise ApiError(409, "ACCOUNT_NOT_AVAILABLE", "该手机号暂不能用于注册，请直接登录")

        user = User(
            phone_ciphertext=self.phone.encrypt(normalized),
            phone_lookup_hash=phone_hash,
            nickname=f"少侠{secrets.randbelow(10000):04d}",
            age_band=payload.age_band,
            guardian_status=(
                GuardianStatus.VERIFIED
                if payload.age_band == AgeBand.UNDER_14
                else GuardianStatus.NOT_REQUIRED
            ),
            status=UserStatus.ACTIVE,
        )
        user.credential = Credential(password_hash=self.passwords.hash(payload.password))
        self.db.add(user)
        self.db.flush()

        consent_evidence = guardian_evidence_id or request_id
        self.db.add_all(
            [
                ConsentRecord(
                    user_id=user.id,
                    consent_type=ConsentType.TERMS,
                    document_version=payload.terms_version,
                    subject=consent_subject,
                    evidence_id=consent_evidence,
                ),
                ConsentRecord(
                    user_id=user.id,
                    consent_type=ConsentType.PRIVACY,
                    document_version=payload.privacy_version,
                    subject=consent_subject,
                    evidence_id=consent_evidence,
                ),
            ]
        )
        if guardian_hash and guardian_evidence_id:
            self.db.add(
                GuardianLink(
                    child_user_id=user.id,
                    guardian_phone_hash=guardian_hash,
                    consent_evidence_id=guardian_evidence_id,
                )
            )
            self.db.add(
                ConsentRecord(
                    user_id=user.id,
                    consent_type=ConsentType.GUARDIAN,
                    document_version=payload.privacy_version,
                    subject=ConsentSubject.GUARDIAN,
                    evidence_id=guardian_evidence_id,
                )
            )

        tokens = self._new_session(user, device_name=payload.device_name)
        self._audit(
            event_type="REGISTER",
            result="SUCCESS",
            request_id=request_id,
            user_id=user.id,
            phone_hash=phone_hash,
            network_key=self.phone.opaque_network_key(client_ip),
        )
        try:
            self.db.commit()
        except IntegrityError as exc:
            self.db.rollback()
            raise ApiError(409, "ACCOUNT_NOT_AVAILABLE", "该手机号暂不能用于注册，请直接登录") from exc
        return self._auth_response(user, tokens)

    def login(
        self,
        payload: PasswordLoginRequest,
        *,
        request_id: str,
        client_ip: str,
    ) -> AuthResponse:
        normalized = self.phone.normalize(payload.phone)
        phone_hash = self.phone.lookup_hash(normalized)
        network_key = self.phone.opaque_network_key(client_ip)
        self._enforce_limit(
            f"login:network:{network_key}", limit=30, window=15 * 60
        )
        self._enforce_limit(
            f"login:phone:{phone_hash}", limit=20, window=15 * 60
        )

        user = self.db.scalar(
            select(User)
            .options(joinedload(User.credential))
            .where(User.phone_lookup_hash == phone_hash)
        )
        if user is None or user.credential is None:
            self.passwords.burn_dummy_verify(payload.password)
            self._audit_failed_login(request_id, phone_hash, network_key)
            raise GENERIC_LOGIN_ERROR

        credential = user.credential
        now = utcnow()
        if credential.locked_until is not None and _aware(credential.locked_until) > now:
            self._audit_failed_login(request_id, phone_hash, network_key, user.id)
            raise GENERIC_LOGIN_ERROR

        if not self.passwords.verify(payload.password, credential.password_hash):
            credential.failed_attempts += 1
            if credential.failed_attempts >= self.settings.login_failure_limit:
                credential.locked_until = now + timedelta(minutes=self.settings.login_lock_minutes)
                credential.failed_attempts = 0
            self._audit_failed_login(request_id, phone_hash, network_key, user.id)
            self.db.commit()
            raise GENERIC_LOGIN_ERROR

        if user.status != UserStatus.ACTIVE:
            self._audit_failed_login(request_id, phone_hash, network_key, user.id)
            self.db.commit()
            raise GENERIC_LOGIN_ERROR
        if user.age_band == AgeBand.UNDER_14 and user.guardian_status != GuardianStatus.VERIFIED:
            raise ApiError(403, "GUARDIAN_CONSENT_REQUIRED", "账号需要先完成监护人同意")

        credential.failed_attempts = 0
        credential.locked_until = None
        tokens = self._new_session(user, device_name=payload.device_name)
        self._audit(
            event_type="LOGIN",
            result="SUCCESS",
            request_id=request_id,
            user_id=user.id,
            phone_hash=phone_hash,
            network_key=network_key,
        )
        self.db.commit()
        return self._auth_response(user, tokens)

    def refresh(self, refresh_token: str, *, device_name: str | None) -> TokenPair:
        token_hash = self.tokens.refresh_token_hash(refresh_token)
        session = self.db.scalar(
            select(AuthSession)
            .options(joinedload(AuthSession.user))
            .where(AuthSession.refresh_token_hash == token_hash)
        )
        if session is None:
            raise ApiError(401, "INVALID_REFRESH_TOKEN", "刷新凭证无效或已过期")
        if session.revoked_at is not None:
            self._revoke_all_sessions(session.user)
            self.db.commit()
            raise ApiError(401, "REFRESH_TOKEN_REUSED", "登录状态已失效，请重新登录")
        if _aware(session.expires_at) <= utcnow() or session.user.status != UserStatus.ACTIVE:
            session.revoked_at = utcnow()
            self.db.commit()
            raise ApiError(401, "INVALID_REFRESH_TOKEN", "刷新凭证无效或已过期")

        session.revoked_at = utcnow()
        session.last_seen_at = utcnow()
        tokens = self._new_session(
            session.user,
            device_name=device_name or session.device_name,
            family_id=session.family_id,
            parent_session_id=session.id,
        )
        self.db.commit()
        return tokens

    def logout(self, refresh_token: str) -> None:
        token_hash = self.tokens.refresh_token_hash(refresh_token)
        session = self.db.scalar(
            select(AuthSession).where(AuthSession.refresh_token_hash == token_hash)
        )
        if session is not None and session.revoked_at is None:
            session.revoked_at = utcnow()
            self.db.commit()

    def logout_all(self, user: User) -> None:
        self._revoke_all_sessions(user)
        self.db.commit()

    def reset_password(
        self,
        payload: PasswordResetRequest,
        *,
        request_id: str,
        client_ip: str,
    ) -> PasswordResetResponse:
        normalized = self.phone.normalize(payload.phone)
        phone_hash = self.phone.lookup_hash(normalized)
        self._consume_verification_code(
            phone_hash=phone_hash,
            purpose=VerificationPurpose.RESET_PASSWORD,
            code=payload.verification_code,
        )
        user = self.db.scalar(
            select(User)
            .options(joinedload(User.credential))
            .where(User.phone_lookup_hash == phone_hash)
        )
        if user is None or user.credential is None or user.status != UserStatus.ACTIVE:
            raise ApiError(400, "PASSWORD_RESET_NOT_AVAILABLE", "无法完成密码修改，请重新获取验证码")

        user.credential.password_hash = self.passwords.hash(payload.new_password)
        user.credential.failed_attempts = 0
        user.credential.locked_until = None
        user.password_changed_at = utcnow()
        self._revoke_all_sessions(user)
        self._audit(
            event_type="PASSWORD_RESET",
            result="SUCCESS",
            request_id=request_id,
            user_id=user.id,
            phone_hash=phone_hash,
            network_key=self.phone.opaque_network_key(client_ip),
        )
        self.db.commit()
        try:
            self.sms_provider.send_security_notice(
                phone=normalized, event="PASSWORD_RESET"
            )
        except Exception:
            # The credential change is complete; notification failure must not roll it back.
            pass
        return PasswordResetResponse()

    def _new_verification_code(self) -> str:
        if self.settings.fixed_verification_code is not None:
            return self.settings.fixed_verification_code
        return f"{secrets.randbelow(1_000_000):06d}"

    @staticmethod
    def _verification_key(purpose: VerificationPurpose, phone_hash: str) -> str:
        return f"{purpose.value}:{phone_hash}"

    def _consume_verification_code(
        self, *, phone_hash: str, purpose: VerificationPurpose, code: str
    ) -> None:
        digest = self.code_digester.digest(
            phone_hash=phone_hash, purpose=purpose.value, code=code
        )
        if not self.verification_store.verify_and_consume(
            self._verification_key(purpose, phone_hash), digest
        ):
            raise ApiError(
                400,
                "VERIFICATION_CODE_INVALID_OR_EXPIRED",
                "验证码错误或已过期，请重新获取",
            )

    def _enforce_limit(self, key: str, *, limit: int, window: int) -> None:
        allowed, retry_after = self.rate_limiter.hit(
            key, limit=limit, window_seconds=window
        )
        if not allowed:
            raise ApiError(
                429,
                "RATE_LIMITED",
                "操作过于频繁，请稍后再试",
                retry_after=retry_after,
            )

    def _require_current_consent_versions(
        self, terms_version: str, privacy_version: str
    ) -> None:
        if (
            terms_version != self.settings.current_terms_version
            or privacy_version != self.settings.current_privacy_version
        ):
            raise ApiError(
                409,
                "CONSENT_VERSION_OUTDATED",
                "协议版本已更新，请重新阅读并确认",
            )

    def _new_session(
        self,
        user: User,
        *,
        device_name: str | None,
        family_id: uuid.UUID | None = None,
        parent_session_id: uuid.UUID | None = None,
    ) -> TokenPair:
        refresh_token = self.tokens.new_refresh_token()
        now = utcnow()
        refresh_expires = now + timedelta(days=self.settings.refresh_token_days)
        session = AuthSession(
            user_id=user.id,
            family_id=family_id or uuid.uuid4(),
            parent_session_id=parent_session_id,
            refresh_token_hash=self.tokens.refresh_token_hash(refresh_token),
            device_name=device_name,
            expires_at=refresh_expires,
        )
        self.db.add(session)
        access_token, access_expires_in = self.tokens.create_access_token(
            user.id, user.token_version
        )
        return TokenPair(
            access_token=access_token,
            refresh_token=refresh_token,
            expires_in=access_expires_in,
            refresh_expires_in=int((refresh_expires - now).total_seconds()),
        )

    def _auth_response(self, user: User, tokens: TokenPair) -> AuthResponse:
        normalized_phone = self.phone.decrypt(user.phone_ciphertext)
        return AuthResponse(
            user=UserPublic(
                id=user.id,
                nickname=user.nickname,
                phone_masked=self.phone.mask(normalized_phone),
                status=user.status,
                age_band=user.age_band,
                guardian_status=user.guardian_status,
            ),
            tokens=tokens,
        )

    def _revoke_all_sessions(self, user: User) -> None:
        now = utcnow()
        self.db.execute(
            update(AuthSession)
            .where(AuthSession.user_id == user.id, AuthSession.revoked_at.is_(None))
            .values(revoked_at=now)
        )
        user.token_version += 1

    def _audit_failed_login(
        self,
        request_id: str,
        phone_hash: str,
        network_key: str,
        user_id: uuid.UUID | None = None,
    ) -> None:
        self._audit(
            event_type="LOGIN",
            result="FAILED",
            request_id=request_id,
            user_id=user_id,
            phone_hash=phone_hash,
            network_key=network_key,
        )
        if user_id is None:
            self.db.commit()

    def _audit(
        self,
        *,
        event_type: str,
        result: str,
        request_id: str,
        user_id: uuid.UUID | None = None,
        phone_hash: str | None = None,
        network_key: str | None = None,
    ) -> None:
        self.db.add(
            AuthAuditEvent(
                user_id=user_id,
                phone_lookup_hash=phone_hash,
                event_type=event_type,
                result=result,
                request_id=request_id,
                network_key=network_key,
            )
        )
