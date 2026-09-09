"""Run the 修炼页 backend quality gates without a database or frontend build."""

from __future__ import annotations

import json
import sys
from pathlib import Path

from app.domains.catalog.content_schema import build_trial_manifest, validate_trial_manifest
from app.domains.catalog.contracts import ManualPageDetailPublic
from app.domains.learning.contracts import TrialAttemptAccepted
from app.domains.learning.page_contracts import (
    LearningOverviewPublic,
    LearningRoutePublic,
    LessonReadEventAccepted,
    MigrationEvidenceSubmitted,
    TeachingEvidenceAccepted,
)
from app.main import app


ROOT = Path(__file__).parents[1]
OPENAPI_PATH = ROOT / "docs" / "api" / "openapi.json"
FIXTURES = ROOT / "docs" / "api" / "fixtures"


def _load(name: str) -> dict:
    return json.loads((FIXTURES / name).read_text(encoding="utf-8"))


def check_openapi_sync() -> None:
    expected = json.dumps(app.openapi(), ensure_ascii=False, indent=2, sort_keys=True) + "\n"
    actual = OPENAPI_PATH.read_text(encoding="utf-8")
    if actual != expected:
        raise AssertionError("docs/api/openapi.json is stale; run scripts/export_openapi.py")


def check_fixtures() -> None:
    LearningOverviewPublic.model_validate(_load("learning-overview-content.json"))
    LearningOverviewPublic.model_validate(_load("learning-overview-empty.json"))
    LearningRoutePublic.model_validate(_load("learning-route.json"))
    ManualPageDetailPublic.model_validate(_load("lesson-detail.json"))
    TrialAttemptAccepted.model_validate(_load("trial-attempt-passed.json"))
    TrialAttemptAccepted.model_validate(_load("trial-attempt-failed.json"))
    LessonReadEventAccepted.model_validate(_load("lesson-read-event.json"))
    MigrationEvidenceSubmitted.model_validate(_load("migration-pending.json"))
    TeachingEvidenceAccepted.model_validate(_load("teaching-accepted.json"))


def main() -> int:
    validate_trial_manifest(build_trial_manifest())
    check_openapi_sync()
    check_fixtures()
    print("learning backend quality gates passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
