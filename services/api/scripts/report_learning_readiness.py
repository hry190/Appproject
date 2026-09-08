"""Print a concise readiness report for all 50 修炼页 trial configurations."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from app.domains.catalog.content_schema import TrialManifestEntry, validate_trial_manifest


ROOT = Path(__file__).parents[1]
DEFAULT_INPUT = ROOT / "docs" / "content" / "trial-manifest.json"


def main() -> None:
    parser = argparse.ArgumentParser(description="Report learning content readiness")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--json", action="store_true", dest="as_json")
    args = parser.parse_args()
    raw = json.loads(args.input.read_text(encoding="utf-8"))
    entries = [TrialManifestEntry.model_validate(item) for item in raw]
    validate_trial_manifest(entries)
    report = [
        {
            "page_no": item.page_no,
            "status": item.status,
            "trial_type": item.trial_type,
            "ready_for_activation": item.ready_for_activation,
            "blocker": None
            if item.ready_for_activation
            else "等待正式题目、答案规则和服务端判分配置",
        }
        for item in entries
    ]
    if args.as_json:
        print(json.dumps(report, ensure_ascii=False, indent=2))
        return
    active = sum(item["ready_for_activation"] for item in report)
    print(
        "trial configuration readiness: "
        f"ready={active} blocked={len(report) - active} total={len(report)}"
    )
    for item in report:
        if not item["ready_for_activation"]:
            print(f"page {item['page_no']:02d}: {item['blocker']}")


if __name__ == "__main__":
    main()
