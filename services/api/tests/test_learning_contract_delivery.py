from __future__ import annotations

import json
from pathlib import Path

from fastapi.testclient import TestClient

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
from scripts.mock_learning_server import app as mock_app


FIXTURES = Path(__file__).parents[1] / "docs" / "api" / "fixtures"


def load(name: str) -> dict:
    return json.loads((FIXTURES / name).read_text(encoding="utf-8"))


def test_openapi_contains_learning_page_routes() -> None:
    paths = app.openapi()["paths"]
    assert "/v1/learning/overview" in paths
    assert "/v1/learning/route" in paths
    assert "/v1/lessons/{lesson_id}/migration-evidence" in paths


def test_fixtures_validate_against_public_contracts() -> None:
    LearningOverviewPublic.model_validate(load("learning-overview-content.json"))
    LearningOverviewPublic.model_validate(load("learning-overview-empty.json"))
    LearningRoutePublic.model_validate(load("learning-route.json"))
    ManualPageDetailPublic.model_validate(load("lesson-detail.json"))
    TrialAttemptAccepted.model_validate(load("trial-attempt-passed.json"))
    TrialAttemptAccepted.model_validate(load("trial-attempt-failed.json"))
    LessonReadEventAccepted.model_validate(load("lesson-read-event.json"))
    MigrationEvidenceSubmitted.model_validate(load("migration-pending.json"))
    TeachingEvidenceAccepted.model_validate(load("teaching-accepted.json"))


def test_mock_server_exposes_empty_and_pending_states() -> None:
    client = TestClient(mock_app)
    assert client.get("/v1/learning/overview?mode=empty").json()["books"] == []
    response = client.post("/v1/lessons/lesson-1/migration-evidence")
    assert response.status_code == 201
    assert response.json()["validation_status"] == "PENDING_REVIEW"
