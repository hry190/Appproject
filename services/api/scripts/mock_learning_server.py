"""Dependency-free mock server for frontend contract integration.

Run from ``services/api`` with ``python scripts/mock_learning_server.py``.
The mock deliberately serves static, valid contract examples and does not
connect to PostgreSQL, Redis, or authentication providers.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException, Query


FIXTURE_DIR = Path(__file__).parents[1] / "docs" / "api" / "fixtures"
app = FastAPI(title="机巧江湖 修炼页 Mock API", version="1.0.0-mock")


def fixture(name: str) -> Any:
    return json.loads((FIXTURE_DIR / name).read_text(encoding="utf-8"))


@app.get("/v1/learning/overview")
def overview(mode: str = Query(default="content", pattern="^(empty|content)$")) -> Any:
    return fixture(f"learning-overview-{mode}.json")


@app.get("/v1/learning/route")
def route(q: str = Query(min_length=1, max_length=80)) -> Any:
    payload = fixture("learning-route.json")
    payload["query"] = q
    return payload


@app.get("/v1/lessons/{lesson_id}")
def lesson(lesson_id: str) -> Any:
    payload = fixture("lesson-detail.json")
    payload["id"] = lesson_id
    return payload


@app.post("/v1/trials/{trial_id}/attempts", status_code=201)
def trial_attempt(trial_id: str, failed: bool = False) -> Any:
    payload = fixture("trial-attempt-failed.json" if failed else "trial-attempt-passed.json")
    payload["trial_id"] = trial_id
    return payload


@app.post("/v1/lessons/{lesson_id}/read-events", status_code=201)
def read_event(lesson_id: str) -> Any:
    payload = fixture("lesson-read-event.json")
    payload["lesson_id"] = lesson_id
    return payload


@app.post("/v1/lessons/{lesson_id}/migration-evidence", status_code=201)
def migration(lesson_id: str, pending: bool = True) -> Any:
    if not pending:
        raise HTTPException(status_code=400, detail="mock supports pending review flow only")
    payload = fixture("migration-pending.json")
    payload["lesson_id"] = lesson_id
    return payload


@app.post("/v1/lessons/{lesson_id}/teaching-evidence", status_code=201)
def teaching(lesson_id: str) -> Any:
    payload = fixture("teaching-accepted.json")
    payload["lesson_id"] = lesson_id
    return payload


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8099)
