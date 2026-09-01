"""Regenerate text_501.png at exactly the same 20x10 size as text_500.png.
Renders "501" to match the look of the existing "500" image as closely as possible.
"""
from PIL import Image, ImageDraw, ImageFont
import os

OUT = r"e:\app\Appproject\android\app\src\main\res\drawable\text_501.png"
REF = r"e:\app\Appproject\android\app\src\main\res\drawable\text_500.png"

# Match reference size exactly
ref = Image.open(REF)
W, H = ref.size  # 20x10
print(f"Reference: {REF}  size={W}x{H}")

img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Try fonts
candidates = [
    r"C:\Windows\Fonts\segoeuib.ttf",
    r"C:\Windows\Fonts\arialbd.ttf",
    r"C:\Windows\Fonts\segoeui.ttf",
    r"C:\Windows\Fonts\arial.ttf",
]
font = None
for fp in candidates:
    if os.path.exists(fp):
        try:
            font = ImageFont.truetype(fp, 9)
            break
        except Exception:
            pass
if font is None:
    font = ImageFont.load_default()

text = "501"

# Measure
try:
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    ox = -bbox[0]
    oy = -bbox[1]
except AttributeError:
    tw, th = draw.textsize(text, font=font)
    ox = oy = 0

# Center within the 20x10 box
x = (W - tw) // 2 + ox
y = (H - th) // 2 + oy

draw.text((x, y), text, fill=(255, 255, 255, 255), font=font)

img.save(OUT, "PNG")
print(f"Saved: {OUT}  size={img.size}  text bbox=({tw}x{th}) at ({x},{y})")
