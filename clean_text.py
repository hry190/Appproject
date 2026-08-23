"""
精准清除 PNG 里烧进去的文字,目标颜色:
- 米黄填充: (244, 230, 207) ± 容差
- 深绿阴影: (20, 30, 20) 偏移 1px 后的合成色

用严格阈值匹配 + 邻域采样还原,只动文字像素,不动竹叶。
"""
from PIL import Image
import statistics

# 写字脚本里用的颜色
TEXT_FILL = (244, 230, 207)   # 米黄
TEXT_SHADOW = (20, 30, 20)    # 深绿

# 容差:RGB 距离
TOL = 30


def color_dist(c1, c2):
    return sum(abs(a - b) for a, b in zip(c1, c2))


def is_text_pixel(rgb):
    return (
        color_dist(rgb, TEXT_FILL) <= TOL
        or color_dist(rgb, TEXT_SHADOW) <= TOL
    )


def clean_image(src_path: str) -> None:
    img = Image.open(src_path).convert("RGBA")
    w, h = img.size
    px = img.load()

    # 第一次:估算背景色(取所有非文字像素的中位 RGB)
    bg_samples = []
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a > 200 and not is_text_pixel((r, g, b)):
                bg_samples.append((r, g, b))
    if not bg_samples:
        print(f"  {src_path}: 找不到背景,跳过")
        return
    bg_r = int(statistics.median(s[0] for s in bg_samples))
    bg_g = int(statistics.median(s[1] for s in bg_samples))
    bg_b = int(statistics.median(s[2] for s in bg_samples))
    print(f"  背景典型色 rgb({bg_r},{bg_g},{bg_b})")

    # 第二次:替换文字像素 — 用 5x5 邻域非文字像素中位色
    cleaned = 0
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            if not is_text_pixel((r, g, b)):
                continue
            neigh = []
            for dy in range(-2, 3):
                for dx in range(-2, 3):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h:
                        nr, ng, nb, na = px[nx, ny]
                        if na > 200 and not is_text_pixel((nr, ng, nb)):
                            neigh.append((nr, ng, nb))
            if neigh:
                nr = int(statistics.median(s[0] for s in neigh))
                ng = int(statistics.median(s[1] for s in neigh))
                nb = int(statistics.median(s[2] for s in neigh))
            else:
                nr, ng, nb = bg_r, bg_g, bg_b
            px[x, y] = (nr, ng, nb, a)
            cleaned += 1

    img.save(src_path)
    print(f"  {src_path}: 清理 {cleaned} 像素")


def main() -> None:
    paths = [
        r"D:\App\Appproject\mobile\assets\images\home\1.png",
        r"D:\App\Appproject\mobile\assets\images\home\2.png",
        r"D:\App\Appproject\mobile\assets\images\home\3.png",
        r"D:\App\Appproject\mobile\assets\images\home\4.png",
    ]
    print("=== 清理文字像素(严格阈值) ===")
    for p in paths:
        print(f"- {p}")
        clean_image(p)


if __name__ == "__main__":
    main()