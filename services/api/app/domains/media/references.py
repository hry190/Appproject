from __future__ import annotations

import uuid

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.domains.creations.models import (
    CreationProject,
    CreationProjectStatus,
    CreationVersion,
    ProvenanceItem,
)
from app.domains.profiles.models import UserProfile


def collect_version_asset_ids(db: Session, version: CreationVersion) -> set[uuid.UUID]:
    asset_ids: set[uuid.UUID] = set()
    if version.preview_asset_id is not None:
        asset_ids.add(version.preview_asset_id)
    for layer in version.layer_manifest:
        raw = layer.get("asset_id")
        if raw:
            asset_ids.add(uuid.UUID(str(raw)))
    rows = db.execute(
        select(
            ProvenanceItem.authorization_asset_id,
            ProvenanceItem.output_asset_id,
        ).where(ProvenanceItem.creation_version_id == version.id)
    ).all()
    for authorization_id, output_id in rows:
        if authorization_id is not None:
            asset_ids.add(authorization_id)
        if output_id is not None:
            asset_ids.add(output_id)
    return asset_ids


def is_asset_referenced_by_live_data(
    db: Session,
    *,
    owner_user_id: uuid.UUID,
    asset_id: uuid.UUID,
) -> bool:
    avatar_reference = db.scalar(
        select(UserProfile.user_id).where(
            UserProfile.user_id == owner_user_id,
            UserProfile.avatar_asset_id == asset_id,
        )
    )
    if avatar_reference is not None:
        return True
    versions = db.scalars(
        select(CreationVersion)
        .join(CreationProject, CreationProject.id == CreationVersion.project_id)
        .where(
            CreationProject.owner_user_id == owner_user_id,
            CreationProject.status != CreationProjectStatus.DELETED,
        )
    ).all()
    return any(asset_id in collect_version_asset_ids(db, version) for version in versions)
