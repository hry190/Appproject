"""
给 4 张竖向装饰图添加竖排中文标题。
- 1.png (30x65): 行囊
- 2.png (30x65): 修炼
- 3.png (33x69): 大会
- 4.png (33x69): 作品创作
竖排 = 一个字一行,自上而下。
"""
from PIL import Image, ImageDraw, ImageFont

# 配色 —— 跟首页气泡 #F4E6CF 一致,在深绿叶子上读得清
TEXT_COLOR = (244, 230, 207, 255)  # 米黄 / 牙色(跟气泡同色)
SHADOW_COLOR = (20, 30, 20, 200)   # 极深绿阴影(跟竹叶同色系)

# 字体:微软雅黑 (首页文字样式)
FONT_PATH = r"C:\Windows\Fonts\msyh.ttc"


def vertical_text_image(
    src_path: str,
    text: str,
    out_path: str,
    font_size: int,
) -> None:
    img = Image.open(src_path).convert("RGBA")
    w, h = img.size
    font = ImageFont.truetype(FONT_PATH, font_size)

    # 用临时图层画文字,后面 paste 回去(原图保留装饰)
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)

    n = len(text)
    # 整体竖排块高: n*font_size, 居中放在画布
    block_h = n * font_size
    top = max(2, (h - block_h) // 2)
    x_center = w // 2

    # 测一下字宽,居中
    bbox = font.getbbox("行")
    char_w = bbox[2] - bbox[0]
    x = x_center - char_w // 2

    for i, ch in enumerate(text):
        y = top + i * font_size
        # 阴影(向右下偏移 1 像素)
        draw.text((x + 1, y + 1), ch, font=font, fill=SHADOW_COLOR)
        # 正文
        draw.text((x, y), ch, font=font, fill=TEXT_COLOR)

    out = Image.alpha_composite(img, layer)
    out.save(out_path)
    print(f"  wrote {out_path} ({w}x{h}, '{text}' x{n}, font={font_size})")


def main() -> None:
    cases = [
        (r"D:\App\Appproject\mobile\assets\images\home\1.png", "行囊"),
        (r"D:\App\Appproject\mobile\assets\images\home\2.png", "修炼"),
        (r"D:\App\Appproject\mobile\assets\images\home\3.png", "大会"),
        (r"D:\App\Appproject\mobile\assets\images\home\4.png", "作品创作"),
    ]
    # 统一字号:13px(用户指定)
    UNIFIED_FONT_SIZE = 13
    for src, text in cases:
        print(f"- {src}")
        vertical_text_image(src, text, src, UNIFIED_FONT_SIZE)  # 覆盖原图


if __name__ == "__main__":
    main()