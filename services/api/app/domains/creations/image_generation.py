from __future__ import annotations

import base64
import hashlib
import io
import json
import random
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Protocol

from app.core.config import Settings
from app.domains.creations.models import ImageGenerationQuality, ImageGenerationSize


SIZE_PIXELS = {
    ImageGenerationSize.SQUARE: (1024, 1024),
    ImageGenerationSize.PORTRAIT: (1024, 1536),
    ImageGenerationSize.LANDSCAPE: (1536, 1024),
}
SIZE_API_VALUES = {
    ImageGenerationSize.SQUARE: "1024x1024",
    ImageGenerationSize.PORTRAIT: "1024x1536",
    ImageGenerationSize.LANDSCAPE: "1536x1024",
}


@dataclass(frozen=True, slots=True)
class GeneratedImage:
    data: bytes
    content_type: str


class ImageGenerationProviderError(Exception):
    def __init__(
        self,
        code: str,
        summary: str,
        *,
        retryable: bool,
        rejected: bool = False,
    ) -> None:
        super().__init__(summary)
        self.code = code
        self.summary = summary
        self.retryable = retryable
        self.rejected = rejected


class ImageGenerator(Protocol):
    provider_ref: str
    model_ref: str
    external_data_shared: bool

    def generate(
        self,
        *,
        prompt: str,
        size: ImageGenerationSize,
        quality: ImageGenerationQuality,
        user_ref: str,
    ) -> GeneratedImage: ...


class DevelopmentImageGenerator:
    """Deterministic local preview for end-to-end development and acceptance tests."""

    provider_ref = "development"
    model_ref = "local-composition-v1"
    external_data_shared = False

    def generate(
        self,
        *,
        prompt: str,
        size: ImageGenerationSize,
        quality: ImageGenerationQuality,
        user_ref: str,
    ) -> GeneratedImage:
        del quality, user_ref
        from PIL import Image, ImageDraw, ImageFilter

        width, height = SIZE_PIXELS[size]
        seed = int.from_bytes(hashlib.sha256(prompt.encode()).digest()[:8], "big")
        rng = random.Random(seed)
        top = (24 + rng.randrange(20), 65 + rng.randrange(35), 48 + rng.randrange(24))
        bottom = (138 + rng.randrange(35), 192 + rng.randrange(32), 149 + rng.randrange(28))
        image = Image.new("RGB", (width, height))
        pixels = image.load()
        for y in range(height):
            ratio = y / max(1, height - 1)
            color = tuple(int(top[i] * (1 - ratio) + bottom[i] * ratio) for i in range(3))
            for x in range(width):
                pixels[x, y] = color

        draw = ImageDraw.Draw(image, "RGBA")
        for _ in range(34):
            x = rng.randint(-width // 10, width)
            stem_width = rng.randint(max(5, width // 170), max(9, width // 90))
            shade = rng.choice([(26, 78, 44, 110), (54, 102, 57, 95), (190, 220, 150, 45)])
            draw.rounded_rectangle(
                (x, -height // 10, x + stem_width, height + height // 10),
                radius=max(2, stem_width // 3),
                fill=shade,
            )
            for y in range(rng.randint(0, height // 8), height, rng.randint(95, 180)):
                leaf_width = rng.randint(width // 30, width // 12)
                leaf_height = rng.randint(height // 65, height // 32)
                direction = rng.choice((-1, 1))
                leaf_end = x + direction * leaf_width
                draw.ellipse(
                    (min(x, leaf_end), y, max(x, leaf_end), y + leaf_height),
                    fill=(75, 126, 65, rng.randint(65, 130)),
                )
        for _ in range(10):
            radius = rng.randint(width // 16, width // 5)
            cx = rng.randint(0, width)
            cy = rng.randint(height // 3, height)
            draw.ellipse(
                (cx - radius, cy - radius, cx + radius, cy + radius),
                fill=(220, 239, 184, rng.randint(12, 35)),
            )
        image = image.filter(ImageFilter.GaussianBlur(radius=max(1, width // 900)))
        output = io.BytesIO()
        image.save(output, format="PNG", optimize=True)
        return GeneratedImage(data=output.getvalue(), content_type="image/png")


class OpenAIImageGenerator:
    provider_ref = "openai"
    external_data_shared = True

    def __init__(self, settings: Settings) -> None:
        if settings.openai_api_key is None:
            raise ValueError("OpenAI API key is required")
        self.model_ref = settings.image_generation_model
        self.api_key = settings.openai_api_key.get_secret_value()
        self.endpoint = f"{settings.openai_base_url.rstrip('/')}/images/generations"
        self.timeout = settings.image_generation_timeout_seconds

    def generate(
        self,
        *,
        prompt: str,
        size: ImageGenerationSize,
        quality: ImageGenerationQuality,
        user_ref: str,
    ) -> GeneratedImage:
        body = json.dumps(
            {
                "model": self.model_ref,
                "prompt": prompt,
                "size": SIZE_API_VALUES[size],
                "quality": quality.value.lower(),
                "output_format": "png",
                "moderation": "auto",
                "user": user_ref,
            }
        ).encode()
        request = urllib.request.Request(
            self.endpoint,
            data=body,
            method="POST",
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                payload = json.loads(response.read())
        except urllib.error.HTTPError as exc:
            raw = exc.read().decode(errors="replace")
            provider_code = ""
            provider_message = ""
            try:
                error = json.loads(raw).get("error", {})
                provider_code = str(error.get("code") or error.get("type") or "")
                provider_message = str(error.get("message") or "")
            except json.JSONDecodeError:
                pass
            rejected = "policy" in provider_code.lower() or "safety" in provider_message.lower()
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_REJECTED" if rejected else "IMAGE_PROVIDER_HTTP_ERROR",
                "生成内容未通过模型安全策略" if rejected else "图片生成服务暂时不可用",
                retryable=exc.code == 429 or exc.code >= 500,
                rejected=rejected,
            ) from exc
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_UNAVAILABLE",
                "图片生成服务连接失败，请稍后重试",
                retryable=True,
            ) from exc
        try:
            encoded = payload["data"][0]["b64_json"]
            data = base64.b64decode(encoded, validate=True)
        except (KeyError, IndexError, TypeError, ValueError) as exc:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_INVALID_RESPONSE",
                "图片生成服务返回了无法识别的结果",
                retryable=True,
            ) from exc
        return GeneratedImage(data=data, content_type="image/png")


class VolcengineImageGenerator:
    """Server-side adapter for Volcengine Ark Seedream image generation."""

    provider_ref = "volcengine"
    external_data_shared = True
    _allowed_result_host_suffixes = (
        ".volces.com",
        ".volcengine.com",
        ".byteimg.com",
        ".bytepluses.com",
    )

    def __init__(self, settings: Settings) -> None:
        if settings.volcengine_ark_api_key is None:
            raise ValueError("Volcengine Ark API key is required")
        self.model_ref = settings.image_generation_model
        self.api_key = settings.volcengine_ark_api_key.get_secret_value()
        self.endpoint = (
            f"{settings.volcengine_ark_base_url.rstrip('/')}/images/generations"
        )
        self.output_size = settings.volcengine_image_size
        self.watermark = settings.volcengine_image_watermark
        self.timeout = settings.image_generation_timeout_seconds
        self.max_download_bytes = settings.media_max_upload_bytes

    def generate(
        self,
        *,
        prompt: str,
        size: ImageGenerationSize,
        quality: ImageGenerationQuality,
        user_ref: str,
    ) -> GeneratedImage:
        del quality, user_ref
        composition = {
            ImageGenerationSize.SQUARE: "1:1 方形构图",
            ImageGenerationSize.PORTRAIT: "3:4 竖版构图",
            ImageGenerationSize.LANDSCAPE: "4:3 横版构图",
        }[size]
        body = json.dumps(
            {
                "model": self.model_ref,
                "prompt": f"{prompt}\n画面比例：{composition}。",
                "response_format": "url",
                "size": self.output_size,
                "stream": False,
                "watermark": self.watermark,
            },
            ensure_ascii=False,
            separators=(",", ":"),
        ).encode("utf-8")
        request = urllib.request.Request(
            self.endpoint,
            data=body,
            method="POST",
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                raw = response.read(1_000_001)
        except urllib.error.HTTPError as exc:
            raw_error = exc.read().decode(errors="replace")
            provider_code = ""
            provider_message = ""
            try:
                error = json.loads(raw_error).get("error", {})
                provider_code = str(error.get("code") or error.get("type") or "")
                provider_message = str(error.get("message") or "")
            except json.JSONDecodeError:
                pass
            provider_error = f"{provider_code} {provider_message}".lower()
            rejected = any(
                marker in provider_error
                for marker in ("safety", "policy", "sensitive", "moderation")
            )
            quota_exhausted = any(
                marker in provider_error
                for marker in (
                    "accountoverdue",
                    "overdue balance",
                    "insufficient balance",
                    "quotaexceeded",
                    "quota exceeded",
                )
            )
            authentication_failed = exc.code in {401, 403} and not quota_exhausted
            if rejected:
                code = "IMAGE_PROVIDER_REJECTED"
                summary = "生成内容未通过模型安全策略"
                retryable = False
            elif quota_exhausted:
                code = "IMAGE_PROVIDER_QUOTA_EXHAUSTED"
                summary = "图片创作额度不足，请联系老师"
                retryable = False
            elif authentication_failed:
                code = "IMAGE_PROVIDER_NOT_READY"
                summary = "图片创作服务尚未准备好，请联系老师"
                retryable = False
            elif exc.code == 429:
                code = "IMAGE_PROVIDER_BUSY"
                summary = "现在创作的人有点多，请稍后再试"
                retryable = True
            else:
                code = "IMAGE_PROVIDER_HTTP_ERROR"
                summary = "图片生成服务暂时不可用"
                retryable = exc.code >= 500
            raise ImageGenerationProviderError(
                code,
                summary,
                retryable=retryable,
                rejected=rejected,
            ) from exc
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_UNAVAILABLE",
                "图片生成服务连接失败，请稍后重试",
                retryable=True,
            ) from exc
        if len(raw) > 1_000_000:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_INVALID_RESPONSE",
                "图片生成服务返回了无法识别的结果",
                retryable=True,
            )
        try:
            payload = json.loads(raw)
            item = payload["data"][0]
            result_url = str(item["url"])
        except (json.JSONDecodeError, KeyError, IndexError, TypeError) as exc:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_INVALID_RESPONSE",
                "图片生成服务返回了无法识别的结果",
                retryable=True,
            ) from exc
        self._validate_result_url(result_url)
        try:
            image_request = urllib.request.Request(
                result_url,
                headers={"User-Agent": "JiqiaoJianghu-ImageFetcher/1.0"},
            )
            with urllib.request.urlopen(image_request, timeout=self.timeout) as response:
                data = response.read(self.max_download_bytes + 1)
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError, OSError) as exc:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_RESULT_UNAVAILABLE",
                "图片已经生成，但暂时无法取回，请稍后重试",
                retryable=True,
            ) from exc
        if not data or len(data) > self.max_download_bytes:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_RESULT_INVALID",
                "生成图片大小不符合要求，请重新创作",
                retryable=True,
            )
        content_type = self._image_content_type(data)
        if content_type is None:
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_RESULT_INVALID",
                "生成结果不是可识别的图片，请重新创作",
                retryable=True,
            )
        return GeneratedImage(data=data, content_type=content_type)

    @classmethod
    def _validate_result_url(cls, value: str) -> None:
        parsed = urllib.parse.urlsplit(value)
        hostname = (parsed.hostname or "").lower()
        if parsed.scheme != "https" or not any(
            hostname.endswith(suffix) for suffix in cls._allowed_result_host_suffixes
        ):
            raise ImageGenerationProviderError(
                "IMAGE_PROVIDER_INVALID_RESPONSE",
                "图片生成服务返回了不安全的结果地址",
                retryable=False,
            )

    @staticmethod
    def _image_content_type(data: bytes) -> str | None:
        if data.startswith(b"\x89PNG\r\n\x1a\n"):
            return "image/png"
        if data.startswith(b"\xff\xd8\xff"):
            return "image/jpeg"
        if data.startswith((b"GIF87a", b"GIF89a")):
            return "image/gif"
        if data.startswith(b"RIFF") and data[8:12] == b"WEBP":
            return "image/webp"
        return None


def build_image_generator(settings: Settings) -> ImageGenerator | None:
    if settings.image_generation_provider == "disabled":
        return None
    if settings.image_generation_provider == "openai":
        return OpenAIImageGenerator(settings)
    if settings.image_generation_provider == "volcengine":
        return VolcengineImageGenerator(settings)
    return DevelopmentImageGenerator()
