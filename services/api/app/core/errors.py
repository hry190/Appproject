from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from fastapi import Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse


@dataclass(slots=True)
class ApiError(Exception):
    status_code: int
    code: str
    message: str
    retry_after: int | None = None
    details: Any | None = None


def _request_id(request: Request) -> str:
    return getattr(request.state, "request_id", "unknown")


async def api_error_handler(request: Request, exc: ApiError) -> JSONResponse:
    error: dict[str, Any] = {
        "code": exc.code,
        "message": exc.message,
        "request_id": _request_id(request),
    }
    headers: dict[str, str] = {}
    if exc.retry_after is not None:
        error["retry_after"] = exc.retry_after
        headers["Retry-After"] = str(exc.retry_after)
    if exc.details is not None:
        error["details"] = exc.details
    return JSONResponse(status_code=exc.status_code, content={"error": error}, headers=headers)


async def validation_error_handler(
    request: Request, exc: RequestValidationError
) -> JSONResponse:
    details = [
        {
            "field": ".".join(str(part) for part in error["loc"] if part != "body"),
            "message": error["msg"],
        }
        for error in exc.errors()
    ]
    return JSONResponse(
        status_code=422,
        content={
            "error": {
                "code": "VALIDATION_ERROR",
                "message": "请求参数不符合要求",
                "request_id": _request_id(request),
                "details": details,
            }
        },
    )
