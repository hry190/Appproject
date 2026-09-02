from __future__ import annotations

import secrets

from sqlalchemy import select
from sqlalchemy.orm import Session, joinedload

from app.core.errors import ApiError
from app.core.security import utcnow
from app.domains.profiles.contracts import (
    BadgePublic,
    ProfilePatch,
    ProfilePublic,
    TitlePublic,
)
from app.domains.profiles.models import (
    BadgeDefinition,
    TitleDefinition,
    UserBadge,
    UserProfile,
    UserTitle,
)
from app.domains.media.models import MediaAsset, MediaAssetStatus, UploadPurpose
from app.models import User


ANONYMOUS_ID_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"


def generate_anonymous_id() -> str:
    suffix = "".join(secrets.choice(ANONYMOUS_ID_ALPHABET) for _ in range(8))
    return f"JH-{suffix}"


class ProfileService:
    def __init__(self, *, db: Session) -> None:
        self.db = db

    def get_profile(self, user: User) -> ProfilePublic:
        profile = self._ensure_profile(user)
        return self._build_profile(user, profile)

    def update_profile(self, user: User, payload: ProfilePatch) -> ProfilePublic:
        profile = self._ensure_profile(user)
        if profile.row_version != payload.row_version:
            raise ApiError(409, "VERSION_CONFLICT", "资料已在其他设备修改，请刷新后重试")

        updates = payload.model_dump(
            exclude_unset=True,
            exclude={"row_version"},
        )
        if "nickname" in updates:
            user.nickname = updates.pop("nickname")
        if "avatar_asset_id" in updates:
            avatar_asset_id = updates.pop("avatar_asset_id")
            if avatar_asset_id is not None:
                asset = self.db.scalar(
                    select(MediaAsset).where(
                        MediaAsset.id == avatar_asset_id,
                        MediaAsset.owner_user_id == user.id,
                        MediaAsset.status == MediaAssetStatus.READY,
                        MediaAsset.purpose == UploadPurpose.AVATAR,
                    )
                )
                if asset is None:
                    raise ApiError(
                        422,
                        "AVATAR_ASSET_INVALID",
                        "头像必须使用本人已通过安全检查的头像文件",
                    )
            profile.avatar_asset_id = avatar_asset_id
        if "current_title_id" in updates:
            title_id = updates.pop("current_title_id")
            if title_id is not None:
                unlocked = self.db.scalar(
                    select(UserTitle.id)
                    .join(TitleDefinition, TitleDefinition.id == UserTitle.title_id)
                    .where(
                        UserTitle.user_id == user.id,
                        UserTitle.title_id == title_id,
                        TitleDefinition.is_active.is_(True),
                    )
                )
                if unlocked is None:
                    raise ApiError(403, "TITLE_NOT_UNLOCKED", "只能选择已经获得的称号")
            profile.current_title_id = title_id
        for name, value in updates.items():
            setattr(profile, name, value)
        profile.row_version += 1
        profile.updated_at = utcnow()
        self.db.commit()
        return self._build_profile(user, profile)

    def list_titles(self, user: User) -> list[TitlePublic]:
        profile = self._ensure_profile(user)
        rows = self.db.execute(
            select(UserTitle, TitleDefinition)
            .join(TitleDefinition, TitleDefinition.id == UserTitle.title_id)
            .where(
                UserTitle.user_id == user.id,
                TitleDefinition.is_active.is_(True),
            )
            .order_by(UserTitle.earned_at.desc())
        ).all()
        return [
            TitlePublic(
                id=title.id,
                code=title.code,
                name=title.name,
                description=title.description,
                earned_at=owned.earned_at,
                selected=profile.current_title_id == title.id,
            )
            for owned, title in rows
        ]

    def list_badges(self, user: User) -> list[BadgePublic]:
        self._ensure_profile(user)
        rows = self.db.execute(
            select(UserBadge, BadgeDefinition)
            .join(BadgeDefinition, BadgeDefinition.id == UserBadge.badge_id)
            .where(
                UserBadge.user_id == user.id,
                BadgeDefinition.is_active.is_(True),
            )
            .order_by(UserBadge.earned_at.desc())
        ).all()
        return [
            BadgePublic(
                id=badge.id,
                code=badge.code,
                name=badge.name,
                description=badge.description,
                earned_at=owned.earned_at,
            )
            for owned, badge in rows
        ]

    def _ensure_profile(self, user: User) -> UserProfile:
        profile = self.db.scalar(
            select(UserProfile)
            .options(joinedload(UserProfile.current_title))
            .where(UserProfile.user_id == user.id)
        )
        if profile is not None:
            return profile

        profile = UserProfile(user_id=user.id, anonymous_id=generate_anonymous_id())
        apprentice = self.db.scalar(
            select(TitleDefinition).where(
                TitleDefinition.code == "APPRENTICE",
                TitleDefinition.is_active.is_(True),
            )
        )
        if apprentice is not None:
            profile.current_title_id = apprentice.id
            self.db.add(
                UserTitle(
                    user_id=user.id,
                    title_id=apprentice.id,
                    evidence_ref="ACCOUNT_CREATED",
                )
            )
        self.db.add(profile)
        self.db.commit()
        self.db.refresh(profile)
        return profile

    def _build_profile(self, user: User, profile: UserProfile) -> ProfilePublic:
        titles = self.list_titles_without_ensuring(user, profile)
        selected = next((item for item in titles if item.selected), None)
        return ProfilePublic(
            nickname=user.nickname,
            age_band=user.age_band,
            class_label=profile.class_label,
            anonymous_id=profile.anonymous_id,
            avatar_asset_id=profile.avatar_asset_id,
            profile_visibility=profile.profile_visibility,
            current_title=selected,
            badges=self.list_badges_without_ensuring(user),
            row_version=profile.row_version,
            updated_at=profile.updated_at,
        )

    def list_titles_without_ensuring(
        self,
        user: User,
        profile: UserProfile,
    ) -> list[TitlePublic]:
        rows = self.db.execute(
            select(UserTitle, TitleDefinition)
            .join(TitleDefinition, TitleDefinition.id == UserTitle.title_id)
            .where(UserTitle.user_id == user.id, TitleDefinition.is_active.is_(True))
            .order_by(UserTitle.earned_at.desc())
        ).all()
        return [
            TitlePublic(
                id=title.id,
                code=title.code,
                name=title.name,
                description=title.description,
                earned_at=owned.earned_at,
                selected=profile.current_title_id == title.id,
            )
            for owned, title in rows
        ]

    def list_badges_without_ensuring(self, user: User) -> list[BadgePublic]:
        rows = self.db.execute(
            select(UserBadge, BadgeDefinition)
            .join(BadgeDefinition, BadgeDefinition.id == UserBadge.badge_id)
            .where(UserBadge.user_id == user.id, BadgeDefinition.is_active.is_(True))
            .order_by(UserBadge.earned_at.desc())
        ).all()
        return [
            BadgePublic(
                id=badge.id,
                code=badge.code,
                name=badge.name,
                description=badge.description,
                earned_at=owned.earned_at,
            )
            for owned, badge in rows
        ]
