from __future__ import annotations

import json
import urllib.error

import pytest

from app.core.config import Settings
from app.domains.creations.conversation_coach import (
    CoachImage,
    CoachManual,
    CoachTurn,
    ConversationCoachContext,
    ConversationCoachError,
    OpenAIConversationCoach,
)


class _Response:
    def __init__(self, payload: dict) -> None:
        self.payload = json.dumps(payload, ensure_ascii=False).encode("utf-8")

    def __enter__(self) -> "_Response":
        return self

    def __exit__(self, *_args) -> None:
        return None

    def read(self, _limit: int) -> bytes:
        return self.payload


def _settings() -> Settings:
    return Settings(
        _env_file=None,
        environment="development",
        openai_api_key="unit-test-key",
        openai_base_url="https://model.example.test/v1",
        conversation_coach_provider="openai",
        conversation_coach_model="coach-model",
    )


def _context() -> ConversationCoachContext:
    return ConversationCoachContext(
        initial_idea="画一只修理木鸟的小熊猫",
        turns=(
            CoachTurn(role="STUDENT", content="画一只修理木鸟的小熊猫"),
            CoachTurn(role="COACH", content="先确定场景。", decision="ACCEPTED"),
            CoachTurn(role="STUDENT", content="背景换成傍晚"),
        ),
        attachment_names=("草图.png",),
        images=(CoachImage("草图.png", "image/png", b"png-bytes"),),
        manuals=(CoachManual("构图秘籍", "主体突出", "观察生活", "完成练习"),),
        derivative_source_title="同门机关鸟",
    )


def _deepseek_settings() -> Settings:
    return Settings(
        _env_file=None,
        environment="development",
        deepseek_api_key="deepseek-unit-test-key",
        deepseek_base_url="https://api.deepseek.example.test",
        conversation_coach_provider="deepseek",
        conversation_coach_model="deepseek-vision-model",
    )


def test_openai_conversation_coach_sends_history_references_and_image(monkeypatch) -> None:
    captured: dict = {}

    def fake_urlopen(request, timeout):
        captured["url"] = request.full_url
        captured["headers"] = dict(request.headers)
        captured["body"] = json.loads(request.data)
        captured["timeout"] = timeout
        return _Response(
            {"choices": [{"message": {"content": "我理解你想换成傍晚。\n可以先用暖紫色天空。"}}]}
        )

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    reply = OpenAIConversationCoach(_settings()).reply(_context(), action="student_message")

    assert reply == "我理解你想换成傍晚。\n可以先用暖紫色天空。"
    assert captured["url"] == "https://model.example.test/v1/chat/completions"
    assert captured["headers"]["Authorization"] == "Bearer unit-test-key"
    assert captured["body"]["model"] == "coach-model"
    serialized = json.dumps(captured["body"], ensure_ascii=False)
    assert "背景换成傍晚" in serialized
    assert "构图秘籍" in serialized
    assert "同门机关鸟" in serialized
    assert "data:image/png;base64," in serialized


def test_openai_conversation_coach_returns_retryable_friendly_network_error(monkeypatch) -> None:
    def fail_urlopen(_request, timeout):
        del timeout
        raise urllib.error.URLError("offline")

    monkeypatch.setattr("urllib.request.urlopen", fail_urlopen)

    with pytest.raises(ConversationCoachError) as captured:
        OpenAIConversationCoach(_settings()).reply(_context(), action="student_message")

    assert captured.value.retryable is True
    assert captured.value.user_message == "教练暂时没连上，请检查网络后重试。"


def test_deepseek_conversation_coach_uses_compatible_chat_api(monkeypatch) -> None:
    captured: dict = {}

    def fake_urlopen(request, timeout):
        captured["url"] = request.full_url
        captured["headers"] = dict(request.headers)
        captured["body"] = json.loads(request.data)
        captured["timeout"] = timeout
        return _Response(
            {"choices": [{"message": {"content": "我看到你的草图了，可以把天空改亮一些。"}}]}
        )

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    coach = OpenAIConversationCoach(_deepseek_settings())
    reply = coach.reply(_context(), action="student_message")

    assert coach.provider_ref == "deepseek"
    assert reply == "我看到你的草图了，可以把天空改亮一些。"
    assert captured["url"] == "https://api.deepseek.example.test/chat/completions"
    assert captured["headers"]["Authorization"] == "Bearer deepseek-unit-test-key"
    assert captured["body"]["model"] == "deepseek-vision-model"
    assert captured["body"]["max_tokens"] == 700
    assert captured["body"]["thinking"] == {"type": "disabled"}
    assert "max_completion_tokens" not in captured["body"]
    serialized = json.dumps(captured["body"], ensure_ascii=False)
    assert "背景换成傍晚" in serialized
    assert "data:image/png;base64," in serialized
