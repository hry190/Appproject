from __future__ import annotations

import json
from io import BytesIO
import urllib.error

import pytest

from app.core.config import Settings
from app.domains.creations.image_generation import (
    ImageGenerationProviderError,
    VolcengineImageGenerator,
    build_image_generator,
)
from app.domains.creations.models import ImageGenerationQuality, ImageGenerationSize


class _Response:
    def __init__(self, payload: bytes) -> None:
        self.payload = payload

    def __enter__(self) -> "_Response":
        return self

    def __exit__(self, *_args) -> None:
        return None

    def read(self, limit: int) -> bytes:
        return self.payload[:limit]


def _settings() -> Settings:
    return Settings(
        _env_file=None,
        environment="development",
        image_generation_provider="volcengine",
        image_generation_model="seedream-test-model",
        volcengine_ark_api_key="ark-unit-test-key",
        volcengine_ark_base_url="https://ark.example.test/api/v3",
        volcengine_image_size="2K",
        volcengine_image_watermark=True,
    )


def test_volcengine_image_generator_calls_ark_and_downloads_result(monkeypatch) -> None:
    captured: dict = {}

    def fake_urlopen(request, timeout):
        if request.full_url.endswith("/images/generations"):
            captured["url"] = request.full_url
            captured["headers"] = dict(request.headers)
            captured["body"] = json.loads(request.data)
            captured["timeout"] = timeout
            return _Response(
                json.dumps(
                    {"data": [{"url": "https://result.volces.com/generated/work.png"}]}
                ).encode()
            )
        captured["download_url"] = request.full_url
        return _Response(b"\x89PNG\r\n\x1a\nimage-bytes")

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    generator = VolcengineImageGenerator(_settings())
    result = generator.generate(
        prompt="一幅清晨荷花图",
        size=ImageGenerationSize.PORTRAIT,
        quality=ImageGenerationQuality.MEDIUM,
        user_ref="student-reference",
    )

    assert captured["url"] == "https://ark.example.test/api/v3/images/generations"
    assert captured["headers"]["Authorization"] == "Bearer ark-unit-test-key"
    assert captured["body"] == {
        "model": "seedream-test-model",
        "prompt": "一幅清晨荷花图\n画面比例：3:4 竖版构图。",
        "response_format": "url",
        "size": "2K",
        "stream": False,
        "watermark": True,
    }
    assert captured["download_url"] == "https://result.volces.com/generated/work.png"
    assert result.content_type == "image/png"
    assert result.data.startswith(b"\x89PNG")
    assert isinstance(build_image_generator(_settings()), VolcengineImageGenerator)


def test_volcengine_image_generator_rejects_untrusted_result_url(monkeypatch) -> None:
    def fake_urlopen(_request, timeout):
        del timeout
        return _Response(
            json.dumps({"data": [{"url": "http://127.0.0.1/private"}]}).encode()
        )

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    with pytest.raises(ImageGenerationProviderError) as captured:
        VolcengineImageGenerator(_settings()).generate(
            prompt="安全测试",
            size=ImageGenerationSize.SQUARE,
            quality=ImageGenerationQuality.MEDIUM,
            user_ref="student-reference",
        )

    assert captured.value.retryable is False
    assert captured.value.code == "IMAGE_PROVIDER_INVALID_RESPONSE"


def test_volcengine_image_generator_returns_friendly_network_error(monkeypatch) -> None:
    def fail_urlopen(_request, timeout):
        del timeout
        raise urllib.error.URLError("offline")

    monkeypatch.setattr("urllib.request.urlopen", fail_urlopen)

    with pytest.raises(ImageGenerationProviderError) as captured:
        VolcengineImageGenerator(_settings()).generate(
            prompt="网络测试",
            size=ImageGenerationSize.SQUARE,
            quality=ImageGenerationQuality.MEDIUM,
            user_ref="student-reference",
        )

    assert captured.value.retryable is True
    assert captured.value.summary == "图片生成服务连接失败，请稍后重试"


def test_volcengine_image_generator_maps_exhausted_balance_to_student_safe_error(
    monkeypatch,
) -> None:
    def fail_urlopen(request, timeout):
        del timeout
        body = json.dumps(
            {
                "error": {
                    "code": "AccountOverdueError",
                    "type": "Forbidden",
                    "message": "The account has an overdue balance.",
                }
            }
        ).encode()
        raise urllib.error.HTTPError(
            request.full_url,
            403,
            "Forbidden",
            hdrs=None,
            fp=BytesIO(body),
        )

    monkeypatch.setattr("urllib.request.urlopen", fail_urlopen)

    with pytest.raises(ImageGenerationProviderError) as captured:
        VolcengineImageGenerator(_settings()).generate(
            prompt="额度错误测试",
            size=ImageGenerationSize.PORTRAIT,
            quality=ImageGenerationQuality.MEDIUM,
            user_ref="student-reference",
        )

    assert captured.value.code == "IMAGE_PROVIDER_QUOTA_EXHAUSTED"
    assert captured.value.summary == "图片创作额度不足，请联系老师"
    assert captured.value.retryable is False
