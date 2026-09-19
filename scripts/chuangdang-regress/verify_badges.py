# -*- coding: utf-8 -*-
"""地图页徽记校验(§23:徽记由字形文字换成素材图)。

三件事各配一个判据:
  ① 徽记几何没变 —— 圆仍是 40dp(110px)、仍在卡片左端的同一位置;
  ② 主体没被圆切 —— 素材着墨的最远角必须落在圆内;
  ③ 五个徽记各画的是**各自那关**的素材 —— 五个模板在窗口内各匹配一遍(负对照)。

⚠️ 两个踩过的坑:
  · 窗口若从 x=10 起,会把**卡片左缘/竖装饰线**算进去 → 圆被判成 150px 宽(实际 110)。
    故窗口取 x∈[80,230]:卡片左缘在 60、标题文字从 236 起,中间只有徽记。
  · "着墨"判据不能用"与卡片白差 > 阈值":圆本身的 **tint**(差 10~27)会先被判成着墨;
    改用"素材自身 alpha>200"的掩膜(core),并按真机合成顺序(叠圆底 → 状态压暗)造模板。
"""
import os
import re
import sys

import numpy as np
from PIL import Image
from scipy import ndimage

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import verify_render as VR  # noqa: E402

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

DENSITY = 2.75
BADGE = 40 * DENSITY            # 110px
PAD = 5 * DENSITY               # 内缩 5dp
BOX = BADGE - 2 * PAD           # 素材可用方框 ≈ 82.5px
WX1, WX2 = 80, 230              # 徽记专用窗口(排除卡片左缘与标题文字)
CARDS = [("盾", "第 1 关"), ("蝠", "第 2 关"), ("棋", "第 3 关"), ("鹤", "第 4 关"), ("枢", "终局")]


def nodes_of(xml_path):
    xml = open(xml_path, encoding="utf-8").read()
    out = []
    for m in re.finditer(r"<node[^>]*>", xml):
        tag = m.group(0)
        t = re.search(r'text="([^"]*)"', tag)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
        if t and b and t.group(1).strip():
            out.append((t.group(1), *[int(g) for g in b.groups()]))
    return out


def build(fn, bg, state_alpha):
    """真机合成顺序:素材(带 alpha)叠圆底 → 再按状态压暗。core = 素材 alpha>200。"""
    w, h = 0, 0
    tpl = VR.load_asset(fn)
    w, h = tpl.shape[1], tpl.shape[0]
    s = min(BOX / w, BOX / h)
    im = Image.fromarray(tpl, "RGBA").resize((max(1, int(round(w * s))), max(1, int(round(h * s)))),
                                             Image.LANCZOS)
    t = np.array(im).astype(np.float32)
    a = t[:, :, 3:4] / 255.0
    over = t[:, :, :3] * a + bg[None, None, :] * (1 - a)
    rgb = state_alpha * over + (1 - state_alpha) * bg[None, None, :]
    return rgb, (t[:, :, 3] > 200)


def model_clip(fn, box, radius):
    """模型法(不依赖截图):素材按 Fit 放进徽记方框再居中,**主体最远点会不会出圆**。

    为什么还要这一步:截图法的着墨掩膜必须排除 1dp 描边,于是"最远"被人为截在 R-2,
    量不出"到底切没切"。模型法直接把源素材的每个不透明像素映射到徽记坐标,
    得到 max r 与半径比较 —— 这是**确定性的**,不受阈值与描边干扰。
    另返回会被切掉的不透明像素占比(0 才是真的没切)。
    """
    a = VR.load_asset(fn)
    h, w = a.shape[0], a.shape[1]
    s = min(box / w, box / h)
    ys, xs = np.where(a[:, :, 3] > 8)
    dx = (xs - w / 2.0) * s
    dy = (ys - h / 2.0) * s
    r = np.hypot(dx, dy)
    return float(r.max()), float((r > radius).mean()), int((r > radius).sum()), int(len(r))


def main():
    tag = sys.argv[1] if len(sys.argv) > 1 else "alpha50"
    d = os.path.join(VR.OUT, "mapcheck2")
    png, xml = os.path.join(d, tag + ".png"), os.path.join(d, tag + ".xml")
    img = np.array(Image.open(png).convert("RGB")).astype(np.float32)
    nodes = nodes_of(xml)
    titles = [(t, y1) for (t, _x1, y1, _x2, _y2) in nodes if re.match(r"^(第 \d 关|终局)", t)]
    labels = [(t, y1) for (t, _x1, y1, _x2, _y2) in nodes
              if t in ("可挑战", "继续", "已通关", "未解锁")]

    print("=" * 100)
    print("地图页徽记校验 · %s" % os.path.basename(png))
    print("=" * 100)
    rows, okall = [], True

    for (glyph, prefix), (title, ty1) in zip(CARDS, titles):
        # ── 定位徽记圆 ────────────────────────────────────────────────────────
        # 三次踩坑之后的判据:**在窗口里找"形状像这个圆"的连通域**,而不是先算卡片边界。
        #   ① 固定窗口会把卡片上下的水彩背景含进来 → 掩膜铺满窗口(圆算成 150×340);
        #   ② 沿单列走会撞上右对齐的状态标签文字;
        #   ③ 用"整行白像素数"切卡片边界,又会被**两行副标题**或**卡片间隙里偏亮的背景**带偏
        #      (第 2 关的窗口把邻卡一起吞了 → 141×106)。
        #   这里改成一个**自校验**的判据:40dp 徽记在 2.75 密度下必然是 110×110px 的方形 bbox,
        #   所以直接挑 bbox 落在 [95,130] 且近方形的那个连通域 —— 背景/文字都长不出这个形状。
        wy1, wy2 = ty1 - 140, ty1 + 200
        win = img[wy1:wy2, WX1:WX2]
        mn = win.min(axis=2)
        mx = win.max(axis=2)
        neutral = (mn >= 240) & ((mx - mn) <= 4)
        if neutral.sum() < 500:
            print("!! %s 窗口内没有卡片白" % prefix)
            okall = False
            continue
        white = win[neutral].mean(axis=0).astype(np.float32)
        diff = np.abs(win - white[None, None, :]).max(axis=2)
        lab, n = ndimage.label(diff > 6)
        cxy, radius, circle_w, circle_h = None, None, None, None
        for i, sl in enumerate(ndimage.find_objects(lab), 1):
            if sl is None:
                continue
            h, w = sl[0].stop - sl[0].start, sl[1].stop - sl[1].start
            if 95 <= w <= 130 and 95 <= h <= 130 and 0.85 <= w / h <= 1.18:
                cxy = (WX1 + (sl[1].start + sl[1].stop) / 2, wy1 + (sl[0].start + sl[0].stop) / 2)
                circle_w, circle_h = w, h
                radius = max(w, h) / 2
                break
        if cxy is None:
            print("!! %s 窗口内找不到 110px 的圆形徽记" % prefix)
            okall = False
            continue
        ccx, ccy = cxy

        # 着墨:与卡片白差 >40(圆底 tint 最多差 27、1dp 描边另作排除)
        yy, xx = np.mgrid[0:win.shape[0], 0:win.shape[1]]
        r2 = (WX1 + xx - ccx) ** 2 + (wy1 + yy - ccy) ** 2
        ink = (diff > 40) & (r2 <= (radius - 2) ** 2)   # 排除 1dp 描边(它就在 r≈R 上)
        if not ink.any():
            print("!! %s 圆内没有着墨" % prefix)
            okall = False
            continue
        ys, xs = np.where(ink)
        ink_box = (WX1 + xs.min(), wy1 + ys.min(), WX1 + xs.max(), wy1 + ys.max())
        # ⚠️ 判"有没有被圆切",量的是**墨迹像素到圆心的最大距离**,不是 bbox 的角 ——
        #     bbox 四角是矩形的角,没有任何墨迹落在那里,拿它比半径会虚报出圈(踩过)。
        ink_rmax = float(np.sqrt(r2[ink].max()))

        label = next((t for (t, ly) in labels if abs(ly - ty1) < 80), "?")
        sa = 0.5 if label == "未解锁" else 1.0
        # 圆底颜色直接从截图取:圆心附近、但避开主体的一小块(圆心上方 0.8R 处)
        bg = img[int(ccy - radius * 0.8), int(ccx)].astype(np.float32)

        scores = {}
        for g2, (_nm, fn, _slot) in VR.ASSETS.items():
            tpl, core = build(fn, bg, sa)
            h, w = tpl.shape[:2]
            best = None
            for dy in range(0, win.shape[0] - h + 1):
                for dx in range(0, win.shape[1] - w + 1):
                    v = float(np.abs(win[dy:dy + h, dx:dx + w] - tpl).max(axis=2)[core].mean())
                    if best is None or v < best:
                        best = v
            scores[g2] = best
        order = sorted(scores.items(), key=lambda kv: kv[1])
        winner, margin = order[0][0], order[1][1] / max(order[0][1], 1e-6)

        good_geo = abs(circle_w - BADGE) <= 4 and abs(circle_h - BADGE) <= 4
        m_rmax, m_share, m_n, m_tot = model_clip(VR.ASSETS[glyph][1], BOX, radius)
        good_ink = m_n == 0        # 判"切没切"用模型法,不用被描边截断的截图量
        good_id = winner == glyph
        okall &= good_geo and good_ink and good_id
        rows.append((prefix, glyph, label, circle_w, ink_rmax, radius, winner, margin,
                     good_geo, good_ink, good_id))

        print("")
        print("%s「%s」 状态=%s" % (prefix, glyph, label))
        print("   圆:%dx%dpx(%.1fdp,期望 40dp)  圆心=(%.0f,%.0f)  %s"
              % (circle_w, circle_h, circle_w / DENSITY, ccx, ccy, "✅" if good_geo else "❌"))
        print("   着墨 bbox=x[%d,%d] y[%d,%d](%dx%dpx)  墨迹最远(截图量,≤R-2) %.1fpx / 半径 %.1fpx"
              % (ink_box[0], ink_box[2], ink_box[1], ink_box[3],
                 ink_box[2] - ink_box[0] + 1, ink_box[3] - ink_box[1] + 1, ink_rmax, radius))
        print("   模型法(源素材逐像素映射进徽记):主体最远 %.1fpx / 半径 %.1fpx,"
              "出圆像素 %d/%d  → %s"
              % (m_rmax, radius, m_n, m_tot,
                 "✅ 一点没切(离边 %.1fdp)" % ((radius - m_rmax) / DENSITY) if m_n == 0
                 else "❌ 有 %.2f%% 会被圆切掉" % (m_share * 100)))
        print("   素材识别:" + "  ".join("%s=%.1f" % (g2, v) for g2, v in order))
        print("   → 判为「%s」%s(次优差 %.2f×)" % (winner, "✅" if good_id else "❌ 认错了", margin))

    print("")
    print("=" * 100)
    print("%-8s %-4s %-8s %-8s %-10s %-10s %s" % ("关", "素材", "状态", "圆(px)", "墨迹最远", "识别", "判定"))
    for (prefix, glyph, label, cw, rmax, radius, winner, margin, g1, g2_, g3) in rows:
        print("%-8s %-4s %-8s %-8d %-10.1f %-10s %s"
              % (prefix, glyph, label, cw, rmax, winner, "✅" if (g1 and g2_ and g3) else "❌"))
    print("结论:%s" % ("✅ 五项全过(几何未变 / 主体未出圆 / 认得出是各自那关的素材)" if okall else "❌ 有项未过"))


if __name__ == "__main__":
    main()
