from __future__ import annotations

import uuid

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.core.security import PhoneProtector, utcnow
from app.models import (
    AgeBand,
    AuthSession,
    BlacklistEntry,
    ConsentRecord,
    ContentLevel,
    DataRequestStatus,
    DataRightsRequest,
    FeedbackTicket,
    GuardianControl,
    TicketStatus,
    User,
    UserPreference,
    UserStatus,
)
from app.schemas import (
    AccountExport,
    BlacklistEntryPublic,
    ConsentRecordPublic,
    DataRightsRequestCreate,
    DataRightsRequestPublic,
    FeedbackCreate,
    FeedbackPublic,
    GuardianControlsPatch,
    GuardianControlsPublic,
    SessionPublic,
    UserPreferencesPatch,
    UserPreferencesPublic,
    UserPublic,
)


class UserSettingsService:
    def __init__(
        self,
        *,
        db: Session,
        phone_protector: PhoneProtector,
    ) -> None:
        self.db = db
        self.phone = phone_protector

    def get_preferences(self, user: User) -> UserPreference:
        preferences = self.db.get(UserPreference, user.id)
        if preferences is None:
            preferences = UserPreference(user_id=user.id)
            self.db.add(preferences)
            self.db.commit()
            self.db.refresh(preferences)
        return preferences

    def update_preferences(
        self,
        user: User,
        payload: UserPreferencesPatch,
    ) -> UserPreference:
        preferences = self.get_preferences(user)
        updates = payload.model_dump(exclude_unset=True, exclude_none=True)
        if not updates:
            raise ApiError(422, "VALIDATION_ERROR", "请至少提交一项有效设置")
        for name, value in updates.items():
            setattr(preferences, name, value)
        preferences.updated_at = utcnow()
        self.db.commit()
        self.db.refresh(preferences)
        return preferences

    def get_guardian_controls(self, user: User) -> GuardianControl:
        self._require_minor(user)
        controls = self.db.get(GuardianControl, user.id)
        if controls is None:
            controls = GuardianControl(
                child_user_id=user.id,
                content_level=(
                    ContentLevel.CHILD
                    if user.age_band == AgeBand.UNDER_14
                    else ContentLevel.TEEN
                ),
            )
            self.db.add(controls)
            self.db.commit()
            self.db.refresh(controls)
        return controls

    def update_guardian_controls(
        self,
        user: User,
        payload: GuardianControlsPatch,
    ) -> GuardianControl:
        controls = self.get_guardian_controls(user)
        updates = payload.model_dump(exclude_unset=True, exclude_none=True)
        if not updates:
            raise ApiError(422, "VALIDATION_ERROR", "请至少提交一项有效监护设置")
        for name, value in updates.items():
            setattr(controls, name, value)
        controls.updated_at = utcnow()
        self.db.commit()
        self.db.refresh(controls)
        return controls

    def create_feedback(self, user: User, payload: FeedbackCreate) -> FeedbackTicket:
        ticket = FeedbackTicket(
            user_id=user.id,
            category=payload.category,
            message=payload.message,
            status=TicketStatus.OPEN,
        )
        self.db.add(ticket)
        self.db.commit()
        self.db.refresh(ticket)
        return ticket

    def list_blacklist(self, user: User) -> list[BlacklistEntryPublic]:
        rows = self.db.execute(
            select(BlacklistEntry, User)
            .join(User, User.id == BlacklistEntry.blocked_user_id)
            .where(BlacklistEntry.owner_user_id == user.id)
            .order_by(BlacklistEntry.created_at.desc())
        ).all()
        return [
            BlacklistEntryPublic(
                user_id=blocked.id,
                nickname=blocked.nickname,
                blocked_at=entry.created_at,
            )
            for entry, blocked in rows
        ]

    def add_to_blacklist(
        self,
        user: User,
        blocked_user_id: uuid.UUID,
    ) -> BlacklistEntryPublic:
        if blocked_user_id == user.id:
            raise ApiError(400, "CANNOT_BLOCK_SELF", "不能将自己加入黑名单")
        blocked = self.db.scalar(
            select(User).where(
                User.id == blocked_user_id,
                User.status == UserStatus.ACTIVE,
            )
        )
        if blocked is None:
            raise ApiError(404, "USER_NOT_FOUND", "未找到该用户")
        entry = self.db.scalar(
            select(BlacklistEntry).where(
                BlacklistEntry.owner_user_id == user.id,
                BlacklistEntry.blocked_user_id == blocked_user_id,
            )
        )
        if entry is None:
            entry = BlacklistEntry(
                owner_user_id=user.id,
                blocked_user_id=blocked_user_id,
            )
            self.db.add(entry)
            self.db.commit()
            self.db.refresh(entry)
        return BlacklistEntryPublic(
            user_id=blocked.id,
            nickname=blocked.nickname,
            blocked_at=entry.created_at,
        )

    def remove_from_blacklist(self, user: User, blocked_user_id: uuid.UUID) -> None:
        entry = self.db.scalar(
            select(BlacklistEntry).where(
                BlacklistEntry.owner_user_id == user.id,
                BlacklistEntry.blocked_user_id == blocked_user_id,
            )
        )
        if entry is not None:
            self.db.delete(entry)
            self.db.commit()

    def list_sessions(self, user: User) -> list[SessionPublic]:
        now = utcnow()
        sessions = self.db.scalars(
            select(AuthSession)
            .where(
                AuthSession.user_id == user.id,
                AuthSession.revoked_at.is_(None),
                AuthSession.expires_at > now,
            )
            .order_by(AuthSession.last_seen_at.desc())
        ).all()
        return [self._session_public(session) for session in sessions]

    def revoke_session(self, user: User, session_id: uuid.UUID) -> None:
        session = self.db.scalar(
            select(AuthSession).where(
                AuthSession.id == session_id,
                AuthSession.user_id == user.id,
            )
        )
        if session is None:
            raise ApiError(404, "SESSION_NOT_FOUND", "未找到该登录设备")
        if session.revoked_at is None:
            session.revoked_at = utcnow()
            self.db.commit()

    def create_data_request(
        self,
        user: User,
        payload: DataRightsRequestCreate,
    ) -> DataRightsRequest:
        existing = self.db.scalar(
            select(DataRightsRequest).where(
                DataRightsRequest.user_id == user.id,
                DataRightsRequest.request_type == payload.request_type,
                DataRightsRequest.status.in_(
                    [DataRequestStatus.PENDING, DataRequestStatus.PROCESSING]
                ),
            )
        )
        if existing is not None:
            return existing
        request = DataRightsRequest(
            user_id=user.id,
            request_type=payload.request_type,
            reason=payload.reason,
            status=DataRequestStatus.PENDING,
        )
        self.db.add(request)
        self.db.commit()
        self.db.refresh(request)
        return request

    def list_data_requests(self, user: User) -> list[DataRightsRequest]:
        return list(
            self.db.scalars(
                select(DataRightsRequest)
                .where(DataRightsRequest.user_id == user.id)
                .order_by(DataRightsRequest.created_at.desc())
            ).all()
        )

    def export_account(self, user: User) -> AccountExport:
        normalized_phone = self.phone.decrypt(user.phone_ciphertext)
        controls = None
        if user.age_band != AgeBand.ADULT:
            controls = GuardianControlsPublic.model_validate(
                self.get_guardian_controls(user)
            )
        consents = self.db.scalars(
            select(ConsentRecord)
            .where(ConsentRecord.user_id == user.id)
            .order_by(ConsentRecord.agreed_at.asc())
        ).all()
        return AccountExport(
            generated_at=utcnow(),
            user=UserPublic(
                id=user.id,
                nickname=user.nickname,
                phone_masked=self.phone.mask(normalized_phone),
                status=user.status,
                age_band=user.age_band,
                guardian_status=user.guardian_status,
            ),
            preferences=UserPreferencesPublic.model_validate(
                self.get_preferences(user)
            ),
            guardian_controls=controls,
            consents=[
                ConsentRecordPublic(
                    consent_type=record.consent_type.value,
                    document_version=record.document_version,
                    subject=record.subject.value,
                    agreed_at=record.agreed_at,
                )
                for record in consents
            ],
            active_sessions=self.list_sessions(user),
        )

    @staticmethod
    def _session_public(session: AuthSession) -> SessionPublic:
        return SessionPublic(
            id=session.id,
            device_name=session.device_name or "未知设备",
            created_at=session.created_at,
            last_seen_at=session.last_seen_at,
            expires_at=session.expires_at,
        )

    @staticmethod
    def _require_minor(user: User) -> None:
        if user.age_band == AgeBand.ADULT:
            raise ApiError(
                409,
                "GUARDIAN_CONTROLS_NOT_APPLICABLE",
                "成年人账号不适用监护设置",
            )
