from __future__ import annotations

import base64
import binascii
import uuid

from sqlalchemy import Select, and_, exists, func, or_, select
from sqlalchemy.orm import Session

from app.core.errors import ApiError
from app.domains.catalog.contracts import (
    STATE_LABELS,
    ManualFavoritePublic,
    ManualPageDetailPublic,
    ManualPageListPublic,
    ManualPagePublic,
    ManualProgressRequirementPublic,
)
from app.domains.catalog.models import ManualPage, ManualVolume, UserManualFavorite
from app.domains.learning.contracts import EvidenceAwardPublic, ManualProgressState
from app.domains.learning.models import LearningEvidence, ManualProgress
from app.models import User


PROGRESS_REQUIREMENTS = [
    ManualProgressRequirementPublic(
        state=ManualProgressState.DISCOVERED,
        label=STATE_LABELS[ManualProgressState.DISCOVERED],
        requirement="完成本页的页前预测或首次探索任务",
    ),
    ManualProgressRequirementPublic(
        state=ManualProgressState.LEARNED,
        label=STATE_LABELS[ManualProgressState.LEARNED],
        requirement="通过本页关联试炼",
    ),
    ManualProgressRequirementPublic(
        state=ManualProgressState.MASTERED,
        label=STATE_LABELS[ManualProgressState.MASTERED],
        requirement="提交迁移应用证明并通过审核",
    ),
    ManualProgressRequirementPublic(
        state=ManualProgressState.TEACHING,
        label=STATE_LABELS[ManualProgressState.TEACHING],
        requirement="完成结构化讲解或有效互评",
    ),
]


def _encode_cursor(page_no: int) -> str:
    raw = f"v1:{page_no}".encode()
    return base64.urlsafe_b64encode(raw).decode().rstrip("=")


def _decode_cursor(cursor: str) -> int:
    try:
        padded = cursor + "=" * (-len(cursor) % 4)
        raw = base64.urlsafe_b64decode(padded).decode()
        version, value = raw.split(":", maxsplit=1)
        if version != "v1":
            raise ValueError
        page_no = int(value)
        if not 0 <= page_no <= 50:
            raise ValueError
        return page_no
    except (ValueError, UnicodeDecodeError, binascii.Error) as exc:
        raise ApiError(400, "INVALID_CURSOR", "分页游标无效，请从第一页重新加载") from exc


class CatalogService:
    def __init__(self, *, db: Session) -> None:
        self.db = db

    def list_pages(
        self,
        user: User,
        *,
        volume: int | None,
        query: str | None,
        state: ManualProgressState | None,
        favorites_only: bool,
        cursor: str | None,
        limit: int,
    ) -> ManualPageListPublic:
        filters = [ManualPage.is_listed.is_(True), ManualVolume.is_listed.is_(True)]
        if volume is not None:
            filters.append(ManualVolume.number == volume)
        normalized_query = query.strip() if query else None
        if normalized_query:
            pattern = f"%{normalized_query.lower()}%"
            filters.append(
                or_(
                    func.lower(ManualPage.title).like(pattern),
                    func.lower(ManualPage.core_logic).like(pattern),
                    func.lower(ManualVolume.title).like(pattern),
                    func.lower(ManualVolume.core_domain).like(pattern),
                )
            )
        favorite_exists = exists().where(
            UserManualFavorite.user_id == user.id,
            UserManualFavorite.manual_page_id == ManualPage.id,
        )
        if favorites_only:
            filters.append(favorite_exists)
        progress_join = and_(
            ManualProgress.user_id == user.id,
            ManualProgress.manual_page_id == ManualPage.id,
        )
        if state == ManualProgressState.UNSEEN:
            filters.append(
                or_(
                    ManualProgress.user_id.is_(None),
                    ManualProgress.state == ManualProgressState.UNSEEN,
                )
            )
        elif state is not None:
            filters.append(ManualProgress.state == state)

        total = self.db.scalar(
            select(func.count(ManualPage.id))
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .outerjoin(ManualProgress, progress_join)
            .where(*filters)
        ) or 0

        after_page = _decode_cursor(cursor) if cursor else 0
        statement: Select[
            tuple[ManualPage, ManualVolume, bool, ManualProgress | None]
        ] = (
            select(
                ManualPage,
                ManualVolume,
                favorite_exists.label("is_favorite"),
                ManualProgress,
            )
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .outerjoin(ManualProgress, progress_join)
            .where(*filters, ManualPage.page_no > after_page)
            .order_by(ManualPage.page_no)
            .limit(limit + 1)
        )
        rows = self.db.execute(statement).all()
        has_more = len(rows) > limit
        selected_rows = rows[:limit]
        items = [
            self._to_public(page, volume_row, bool(is_favorite), progress)
            for page, volume_row, is_favorite, progress in selected_rows
        ]
        next_cursor = (
            _encode_cursor(selected_rows[-1][0].page_no)
            if has_more and selected_rows
            else None
        )
        return ManualPageListPublic(
            total=total,
            items=items,
            next_cursor=next_cursor,
        )

    def get_page(self, user: User, manual_page_id: uuid.UUID) -> ManualPageDetailPublic:
        favorite_exists = exists().where(
            UserManualFavorite.user_id == user.id,
            UserManualFavorite.manual_page_id == ManualPage.id,
        )
        progress_join = and_(
            ManualProgress.user_id == user.id,
            ManualProgress.manual_page_id == ManualPage.id,
        )
        row = self.db.execute(
            select(
                ManualPage,
                ManualVolume,
                favorite_exists.label("is_favorite"),
                ManualProgress,
            )
            .join(ManualVolume, ManualVolume.id == ManualPage.volume_id)
            .outerjoin(ManualProgress, progress_join)
            .where(
                ManualPage.id == manual_page_id,
                ManualPage.is_listed.is_(True),
                ManualVolume.is_listed.is_(True),
            )
        ).one_or_none()
        if row is None:
            raise ApiError(404, "MANUAL_NOT_FOUND", "秘籍不存在或暂未开放")
        page, volume, is_favorite, progress = row
        public = self._to_public(page, volume, bool(is_favorite), progress)
        evidence_rows = self.db.scalars(
            select(LearningEvidence)
            .where(
                LearningEvidence.user_id == user.id,
                LearningEvidence.manual_page_id == page.id,
            )
            .order_by(LearningEvidence.created_at.desc())
            .limit(10)
        ).all()
        return ManualPageDetailPublic(
            **public.model_dump(),
            life_hook=page.life_hook,
            interaction_evidence=page.interaction_evidence,
            progress_requirements=PROGRESS_REQUIREMENTS,
            evidence=[
                EvidenceAwardPublic(
                    id=evidence.id,
                    category=evidence.category,
                    evidence_type=evidence.evidence_type,
                    manual_page_id=evidence.manual_page_id,
                    summary=evidence.summary,
                    validation_status=evidence.validation_status,
                    created_at=evidence.created_at,
                )
                for evidence in evidence_rows
            ],
        )

    def add_favorite(
        self, user: User, manual_page_id: uuid.UUID
    ) -> ManualFavoritePublic:
        self._require_page(manual_page_id)
        favorite = self.db.scalar(
            select(UserManualFavorite).where(
                UserManualFavorite.user_id == user.id,
                UserManualFavorite.manual_page_id == manual_page_id,
            )
        )
        if favorite is None:
            favorite = UserManualFavorite(
                user_id=user.id,
                manual_page_id=manual_page_id,
            )
            self.db.add(favorite)
            self.db.commit()
            self.db.refresh(favorite)
        return ManualFavoritePublic(
            manual_page_id=manual_page_id,
            is_favorite=True,
            created_at=favorite.created_at,
        )

    def remove_favorite(self, user: User, manual_page_id: uuid.UUID) -> None:
        self._require_page(manual_page_id)
        favorite = self.db.scalar(
            select(UserManualFavorite).where(
                UserManualFavorite.user_id == user.id,
                UserManualFavorite.manual_page_id == manual_page_id,
            )
        )
        if favorite is not None:
            self.db.delete(favorite)
            self.db.commit()

    def _require_page(self, manual_page_id: uuid.UUID) -> ManualPage:
        page = self.db.scalar(
            select(ManualPage).where(
                ManualPage.id == manual_page_id,
                ManualPage.is_listed.is_(True),
            )
        )
        if page is None:
            raise ApiError(404, "MANUAL_NOT_FOUND", "秘籍不存在或暂未开放")
        return page

    @staticmethod
    def _to_public(
        page: ManualPage,
        volume: ManualVolume,
        is_favorite: bool,
        progress: ManualProgress | None,
    ) -> ManualPagePublic:
        state = progress.state if progress else ManualProgressState.UNSEEN
        return ManualPagePublic(
            id=page.id,
            page_no=page.page_no,
            style_no=page.style_no,
            title=page.title,
            volume_no=volume.number,
            volume_title=volume.title,
            core_logic=page.core_logic,
            content_version=page.content_version,
            content_status=page.content_status,
            progress_state=state,
            progress_label=STATE_LABELS[state],
            is_favorite=is_favorite,
        )
