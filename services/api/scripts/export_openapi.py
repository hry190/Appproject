"""Export the FastAPI contract used by the 修炼页 integration.

Usage (from services/api):
    .venv/Scripts/python scripts/export_openapi.py
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from app.main import app


def export(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(app.openapi(), ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Export OpenAPI JSON")
    parser.add_argument(
        "--output",
        type=Path,
        default=Path(__file__).parents[1] / "docs" / "api" / "openapi.json",
    )
    args = parser.parse_args()
    export(args.output)
    print(f"openapi exported: {args.output}")


if __name__ == "__main__":
    main()
