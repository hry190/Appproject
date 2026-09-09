from __future__ import annotations

from app.domains.catalog.content_schema import build_trial_manifest, validate_trial_manifest
from app.domains.catalog.seed import validate_seed_source


def main() -> None:
    validate_seed_source()
    manifest = build_trial_manifest()
    validate_trial_manifest(manifest)
    active = sum(item.status == "ACTIVE" for item in manifest)
    draft = sum(item.status == "DRAFT" for item in manifest)
    print(f"learning content valid: pages={len(manifest)} active_trials={active} draft_trials={draft}")


if __name__ == "__main__":
    main()
