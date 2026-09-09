"""Export or validate the versioned 50-page trial content manifest.

Examples (from services/api):
    python scripts/export_trial_manifest.py
    python scripts/export_trial_manifest.py --input docs/content/trial-manifest.json
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from app.domains.catalog.content_schema import (
    TrialManifestEntry,
    build_trial_manifest,
    validate_trial_manifest,
)


ROOT = Path(__file__).parents[1]
DEFAULT_OUTPUT = ROOT / "docs" / "content" / "trial-manifest.json"


def main() -> None:
    parser = argparse.ArgumentParser(description="Export or validate trial manifest")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--input", type=Path, help="validate an existing JSON manifest")
    args = parser.parse_args()

    if args.input:
        raw = json.loads(args.input.read_text(encoding="utf-8"))
        entries = [TrialManifestEntry.model_validate(item) for item in raw]
        validate_trial_manifest(entries)
        print(f"trial manifest valid: {args.input}")
        return

    entries = build_trial_manifest()
    validate_trial_manifest(entries)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps([entry.model_dump(mode="json") for entry in entries], ensure_ascii=False, indent=2)
        + "\n",
        encoding="utf-8",
    )
    print(f"trial manifest exported: {args.output}")


if __name__ == "__main__":
    main()
