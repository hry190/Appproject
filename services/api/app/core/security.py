from __future__ import annotations

import hashlib
import hmac
import re
import secrets
import uuid
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
from typing import Any

import jwt
from cryptography.fernet import Fernet, InvalidToken
from pwdlib import PasswordHash

from app.core.config import Settings
from app.core.errors import ApiError


PHONE_RE = re.compile(r"^1[3-9]\d{9}$")


def utcnow() -> datetime:
    return datetime.now(UTC)


class PhoneProtector:
    def __init__(self, settings: Settings) -> None:
        self._cipher = Fernet(settings.phone_encryption_key.get_secret_value().encode())
        self._lookup_key = settings.phone_lookup_key.get_secret_value().encode()

    @staticmethod
    def normalize(raw: str) -> str:
        compact = re.sub(r"[\s-]", "", raw)
        if compact.startswith("+86"):
            compact = compact[3:]
        elif compact.startswith("86") and len(compact) == 13:
            compact = compact[2:]
        if not PHONE_RE.fullmatch(compact):
            raise ApiError(422, "INVALID_PHONE", "请输入有效的中国大陆手机号")
        return f"+86{compact}"

    def lookup_hash(self, normalized_phone: str) -> str:
        return hmac.new(
            self._lookup_key, normalized_phone.encode(), hashlib.sha256
        ).hexdigest()

    def encrypt(self, normalized_phone: str) -> str:
        return self._cipher.encrypt(normalized_phone.encode()).decode()

    def decrypt(self, ciphertext: str) -> str:
        try:
            return self._cipher.decrypt(ciphertext.encode()).decode()
        except InvalidToken as exc:
            raise RuntimeError("phone ciphertext cannot be decrypted") from exc

    @staticmethod
    def mask(normalized_phone: str) -> str:
        national = normalized_phone[-11:]
        return f"{national[:3]}****{national[-4:]}"

    def opaque_network_key(self, raw_value: str) -> str:
        return hmac.new(self._lookup_key, raw_value.encode(), hashlib.sha256).hexdigest()


class PasswordService:
    def __init__(self) -> None:
        self._hasher = PasswordHash.recommended()
        self._dummy_hash = self._hasher.hash(secrets.token_urlsafe(24))

    def hash(self, password: str) -> str:
        return self._hasher.hash(password)

    def verify(self, password: str, password_hash: str) -> bool:
        return self._hasher.verify(password, password_hash)

    def burn_dummy_verify(self, password: str) -> None:
        self._hasher.verify(password, self._dummy_hash)


@dataclass(frozen=True, slots=True)
class AccessClaims:
    user_id: uuid.UUID
    token_version: int
    token_id: str


class TokenService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._secret = settings.jwt_secret.get_secret_value()

    def create_access_token(self, user_id: uuid.UUID, token_version: int) -> tuple[str, int]:
        now = utcnow()
        expires = now + timedelta(minutes=self._settings.access_token_minutes)
        payload = {
            "iss": self._settings.jwt_issuer,
            "aud": self._settings.jwt_audience,
            "sub": str(user_id),
            "typ": "access",
            "ver": token_version,
            "jti": str(uuid.uuid4()),
            "iat": now,
            "nbf": now,
            "exp": expires,
        }
        token = jwt.encode(payload, self._secret, algorithm=self._settings.jwt_algorithm)
        return token, int((expires - now).total_seconds())

    def decode_access_token(self, token: str) -> AccessClaims:
        payload = self._decode(token, expected_type="access")
        try:
            return AccessClaims(
                user_id=uuid.UUID(payload["sub"]),
                token_version=int(payload["ver"]),
                token_id=str(payload["jti"]),
            )
        except (KeyError, TypeError, ValueError) as exc:
            raise ApiError(401, "INVALID_ACCESS_TOKEN", "登录状态无效或已过期") from exc

    def create_guardian_consent_token(
        self,
        *,
        child_phone_hash: str,
        guardian_phone_hash: str,
        terms_version: str,
        privacy_version: str,
    ) -> tuple[str, int]:
        now = utcnow()
        expires = now + timedelta(minutes=self._settings.guardian_token_minutes)
        payload = {
            "iss": self._settings.jwt_issuer,
            "aud": self._settings.jwt_audience,
            "sub": child_phone_hash,
            "typ": "guardian_consent",
            "guardian": guardian_phone_hash,
            "terms": terms_version,
            "privacy": privacy_version,
            "jti": str(uuid.uuid4()),
            "iat": now,
            "nbf": now,
            "exp": expires,
        }
        token = jwt.encode(payload, self._secret, algorithm=self._settings.jwt_algorithm)
        return token, int((expires - now).total_seconds())

    def decode_guardian_consent_token(self, token: str) -> dict[str, Any]:
        return self._decode(token, expected_type="guardian_consent")

    @staticmethod
    def new_refresh_token() -> str:
        return secrets.token_urlsafe(48)

    @staticmethod
    def refresh_token_hash(refresh_token: str) -> str:
        return hashlib.sha256(refresh_token.encode()).hexdigest()

    def _decode(self, token: str, *, expected_type: str) -> dict[str, Any]:
        try:
            payload = jwt.decode(
                token,
                self._secret,
                algorithms=[self._settings.jwt_algorithm],
                audience=self._settings.jwt_audience,
                issuer=self._settings.jwt_issuer,
                options={
                    "require": ["iss", "aud", "sub", "typ", "jti", "iat", "nbf", "exp"]
                },
            )
        except jwt.PyJWTError as exc:
            raise ApiError(401, "INVALID_TOKEN", "凭证无效或已过期") from exc
        if payload.get("typ") != expected_type:
            raise ApiError(401, "INVALID_TOKEN_TYPE", "凭证类型不正确")
        return payload


class VerificationCodeDigester:
    def __init__(self, settings: Settings) -> None:
        self._key = settings.verification_code_key.get_secret_value().encode()

    def digest(self, *, phone_hash: str, purpose: str, code: str) -> str:
        message = f"{purpose}:{phone_hash}:{code}".encode()
        return hmac.new(self._key, message, hashlib.sha256).hexdigest()
