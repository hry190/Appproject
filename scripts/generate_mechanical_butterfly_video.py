from __future__ import annotations

import argparse
import math
import random
import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageOps


REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE = (
    REPO_ROOT
    / "android/app/src/main/res/drawable-nodpi/img_yanwuchang_video_mechanical_butterfly.png"
)
DEFAULT_OUTPUT = REPO_ROOT / "services/api/seed_assets/mechanical_butterfly.mp4"
DEFAULT_COVER = REPO_ROOT / "services/api/seed_assets/mechanical_butterfly_cover.jpg"


def render_video(
    source: Path,
    output: Path,
    cover: Path,
    *,
    duration_seconds: int = 8,
    fps: int = 15,
    width: int = 720,
    height: int = 1280,
) -> None:
    if not source.is_file():
        raise FileNotFoundError(source)
    ffmpeg = shutil.which("ffmpeg")
    if ffmpeg is None:
        raise RuntimeError("ffmpeg is required to encode the seed video")

    with Image.open(source) as opened:
        base = ImageOps.fit(
            opened.convert("RGB"),
            (width, height),
            method=Image.Resampling.LANCZOS,
            centering=(0.5, 0.38),
        )
    output.parent.mkdir(parents=True, exist_ok=True)
    cover.parent.mkdir(parents=True, exist_ok=True)
    base.save(cover, "JPEG", quality=93, optimize=True)

    rng = random.Random(20260911)
    particles = [
        (
            rng.uniform(0.08, 0.92),
            rng.uniform(0.12, 0.78),
            rng.uniform(0.025, 0.075),
            rng.uniform(0.6, 1.45),
            rng.uniform(0, math.tau),
        )
        for _ in range(24)
    ]
    frame_count = duration_seconds * fps

    with tempfile.TemporaryDirectory(prefix="jianghu-butterfly-") as temp_dir:
        frame_dir = Path(temp_dir)
        for index in range(frame_count):
            phase = math.tau * index / frame_count
            zoom = 1.0 + 0.018 * (0.5 - 0.5 * math.cos(phase))
            scaled = base.resize(
                (round(width * zoom), round(height * zoom)),
                Image.Resampling.LANCZOS,
            )
            left = (scaled.width - width) // 2 + round(3 * math.sin(phase))
            top = (scaled.height - height) // 2 + round(2 * math.sin(phase * 2))
            frame = scaled.crop((left, top, left + width, top + height))
            brightness = 1.0 + 0.018 * math.sin(phase)
            frame = ImageEnhance.Brightness(frame).enhance(brightness).convert("RGBA")

            glow = Image.new("RGBA", frame.size, (0, 0, 0, 0))
            glow_draw = ImageDraw.Draw(glow)
            pulse = 0.5 + 0.5 * math.sin(phase * 2 - 0.7)
            radius_x = round(width * (0.19 + 0.012 * pulse))
            radius_y = round(height * (0.12 + 0.008 * pulse))
            center_x, center_y = round(width * 0.50), round(height * 0.36)
            glow_draw.ellipse(
                (
                    center_x - radius_x,
                    center_y - radius_y,
                    center_x + radius_x,
                    center_y + radius_y,
                ),
                fill=(255, 205, 86, round(18 + 18 * pulse)),
            )
            glow = glow.filter(ImageFilter.GaussianBlur(round(width * 0.055)))
            frame = Image.alpha_composite(frame, glow)

            sparks = Image.new("RGBA", frame.size, (0, 0, 0, 0))
            sparks_draw = ImageDraw.Draw(sparks)
            for px, py, size, speed, offset in particles:
                progress = (index / frame_count * speed + offset / math.tau) % 1.0
                x = width * (px + 0.018 * math.sin(phase * speed + offset))
                y = height * (py - 0.23 * progress)
                alpha = round(150 * math.sin(math.pi * progress) ** 2)
                radius = max(1, round(width * size * 0.055))
                sparks_draw.ellipse(
                    (x - radius, y - radius, x + radius, y + radius),
                    fill=(255, 218, 112, alpha),
                )
            sparks = sparks.filter(ImageFilter.GaussianBlur(1.2))
            frame = Image.alpha_composite(frame, sparks).convert("RGB")
            frame.save(frame_dir / f"frame-{index:04d}.png", "PNG", optimize=False)

        command = [
            ffmpeg,
            "-hide_banner",
            "-loglevel",
            "error",
            "-y",
            "-framerate",
            str(fps),
            "-i",
            str(frame_dir / "frame-%04d.png"),
            "-an",
            "-c:v",
            "libx264",
            "-preset",
            "medium",
            "-crf",
            "22",
            "-pix_fmt",
            "yuv420p",
            "-movflags",
            "+faststart",
            str(output),
        ]
        subprocess.run(command, check=True)


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate the first conference video asset")
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--cover", type=Path, default=DEFAULT_COVER)
    args = parser.parse_args()
    render_video(args.source, args.output, args.cover)
    print(args.output.resolve())
    print(args.cover.resolve())


if __name__ == "__main__":
    main()
