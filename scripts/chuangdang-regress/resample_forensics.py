# -*- coding: utf-8 -*-
"""差异溯源:那 8~40 的像素差,是"渲染错了"还是"重采样滤波器不同"?

背景:素材在槽位里被缩小 1.7~7.5 倍。Android Compose 默认 `FilterQuality.Low`
(双线性)与 PIL 的 LANCZOS 在大幅缩小时**本来就给出不同像素** —— 尤其对这种
细节密集的水墨笔触。所以"逐像素差"不能直接当成"渲染不对"。

本脚本做三件事:
  1. 用 NEAREST/BILINEAR/BICUBIC/LANCZOS 四种滤波器各建一份模板,看哪种最像真机;
  2. 把双方都降到 1/4、1/8 再比(把滤波差异平均掉,只留结构);
  3. 给一个**负对照**:故意用错素材/错缩放,看指标会不会变差 ——
     如果指标对"错素材"都不敏感,那它证明不了任何事(§11 的 v1 就是这么废掉的)。
"""
import os
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import verify_render as VR  # noqa: E402

FILTERS = {"NEAREST": Image.NEAREST, "BILINEAR": Image.BILINEAR,
           "BICUBIC": Image.BICUBIC, "LANCZOS": Image.LANCZOS}
LINES = []


def p(s=""):
    LINES.append(s)
    print(s.encode("ascii", "replace").decode("ascii"))


def tpl_with(asset, slot, flt):
    w, h = asset.shape[1], asset.shape[0]
    s = VR.fit_scale((w, h), slot)
    tw, th = int(round(w * s)), int(round(h * s))
    im = Image.fromarray(asset, "RGBA").resize((tw, th), flt)
    a = np.array(im).astype(np.float32)
    return a[:, :, :3], (a[:, :, 3] >= 250)


def box_down(a, k):
    h, w = a.shape[:2]
    return a[: (h // k) * k, : (w // k) * k].reshape(h // k, k, w // k, k, -1).mean(axis=(1, 3))


def refine(img, tpl, core, pos, r=6):
    """全分辨率下精修位置。

    ⚠️ 这一步不能省:粗匹配在 ÷2 图上做,位置天生有 ±1px 量化误差。
       而 (a,b) 回归对**边缘很密**的图片极敏感 —— 差半个像素就会把斜率稀释到 0.8、
       截距抬到 +20,看起来像"整体变淡了"。那是**回归稀释**,不是渲染变淡。
    """
    h, w = tpl.shape[:2]
    best = None
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            x, y = pos[0] + dx, pos[1] + dy
            if x < 0 or y < 0 or y + h > img.shape[0] or x + w > img.shape[1]:
                continue
            sub = img[y:y + h, x:x + w].astype(np.float32)
            v = float(np.abs(sub - tpl).max(axis=2)[core > 0].mean())
            if best is None or v < best[0]:
                best = (v, (x, y))
    return best[1], best[0]


def compare(img, tpl, core, pos, k=1):
    x, y = pos
    h = min(tpl.shape[0], img.shape[0] - y)
    w = min(tpl.shape[1], img.shape[1] - x)
    tpl = tpl[:h, :w]
    core = core[:h, :w]
    sub = img[y:y + h, x:x + w].astype(np.float32)
    if k > 1:
        sub, tpl2 = box_down(sub, k), box_down(tpl, k)
        m = box_down(core[:, :, None].astype(np.float32), k)[:, :, 0] > 0.6
    else:
        tpl2, m = tpl, core > 0
    d = np.abs(sub - tpl2).max(axis=2)
    return dict(mean=float(d[m].mean()), ok=float((d[m] <= 12).mean()))


def bias_and_shift(img, tpl, core, pos):
    """把"系统偏差"和"滤波噪声"分开。

    如果只是重采样不同,那么 渲染 ≈ a×素材 + b 里应有 a≈1、b≈0,残差即为噪声;
    如果 a、b 偏离 1/0(例如整体变亮、变淡),那是**渲染语义**变了,不是滤波差异。
    顺带扫一次**亚像素位移** —— 半像素错位也能造出"到处是边缘差"的假象。
    """
    from scipy import ndimage
    x, y = pos
    h = min(tpl.shape[0], img.shape[0] - y)
    w = min(tpl.shape[1], img.shape[1] - x)
    sub = img[y:y + h, x:x + w].astype(np.float32)
    t, c = tpl[:h, :w], core[:h, :w] > 0
    ab = []
    for ch in range(3):
        A = np.stack([t[:, :, ch][c], np.ones(int(c.sum()), np.float32)], axis=1)
        sol, *_ = np.linalg.lstsq(A, sub[:, :, ch][c], rcond=None)
        resid = sub[:, :, ch][c] - A @ sol
        ab.append((float(sol[0]), float(sol[1]), float(np.abs(resid).mean())))
    best = None
    for dy in np.arange(-2.0, 2.01, 0.25):
        for dx in np.arange(-2.0, 2.01, 0.25):
            s = np.stack([ndimage.shift(t[:, :, ch], (dy, dx), order=1, mode="nearest")
                          for ch in range(3)], axis=2)
            v = float(np.abs(s - sub).max(axis=2)[c].mean())
            if best is None or v < best[0]:
                best = (v, dx, dy)
    return ab, best


frames = __import__("json").load(open(os.path.join(VR.RUN, "frames.json"), encoding="utf-8"))
picks = {}
for f in frames:
    if f["stage"] in (1, 2, 3, 4, 5) and ("-q01-" in f["name"] or f["name"].startswith("s5-boss-top")):
        picks.setdefault(f["stage"], f)

STAGE = {1: ("盾", VR.BATTLE_SLOT), 2: ("蝠", VR.BATTLE_SLOT), 3: ("棋", VR.BATTLE_SLOT),
         4: ("鹤", VR.BATTLE_SLOT), 5: ("枢", VR.BOSS_SLOT)}

p("=" * 104)
p("差异溯源:同分辨率(受滤波影响)/ 1-4 降采样(去滤波)/ 滤波器指纹 / 负对照")
p("=" * 104)
for st in (1, 2, 3, 4, 5):
    glyph, slot = STAGE[st]
    f = picks[st]
    img = np.array(Image.open(os.path.join(VR.RUN, f["name"] + ".png")).convert("RGB"))
    asset = VR.load_asset(VR.ASSETS[glyph][1])
    small = img[::2, ::2].astype(np.float32)
    t0, c0 = tpl_with(asset, slot, Image.LANCZOS)
    t0s = box_down(t0, 2)
    _, (sx, sy) = VR.masked_ssd_mean(small, t0s, box_down(c0[:, :, None].astype(np.float32), 2)[:, :, 0])
    pos, refined = refine(img, t0, c0, (sx * 2, sy * 2))

    p("")
    p("【第 %d 关 %s】越界比 %.2f×" % (st, VR.ASSETS[glyph][0],
                                    max(asset.shape[0], asset.shape[1]) / max(t0.shape[:2])))
    p("  滤波器指纹(1× 全分辨率,越小越像该滤波器的产物):")
    best = None
    for nm, flt in FILTERS.items():
        t, c = tpl_with(asset, slot, flt)
        r = compare(img, t, c, pos)
        p("     %-8s 平均差 %6.2f  差≤12 占 %5.1f%%" % (nm, r["mean"], r["ok"] * 100))
        if best is None or r["mean"] < best[1]:
            best = (nm, r["mean"])
    p("     → 真机最接近 **%s**(说明差异确实来自重采样,不是内容不同)" % best[0])

    t, c = tpl_with(asset, slot, Image.LANCZOS)
    p("  随降采样倍数的收敛(把滤波差异平均掉,只看结构):")
    for k in (1, 2, 4, 8):
        r = compare(img, t, c, pos, k=k)
        p("     ÷%-2d  平均差 %6.2f  差≤12 占 %5.1f%%" % (k, r["mean"], r["ok"] * 100))

    # 负对照:错素材 / 错缩放
    wrong = "鹤" if glyph != "鹤" else "盾"
    tw, cw = tpl_with(VR.load_asset(VR.ASSETS[wrong][1]), slot, Image.LANCZOS)
    hh, ww = min(t.shape[0], tw.shape[0]), min(t.shape[1], tw.shape[1])
    p("  负对照(结构指标对「错东西」敏感吗):")
    p("     正确素材 ÷4 : %.2f / 差≤12 占 %.1f%%"
      % (compare(img, t, c, pos, k=4)["mean"], compare(img, t, c, pos, k=4)["ok"] * 100))
    rc = compare(img, tw, cw, pos, k=4)
    p("     换成 %s 素材 ÷4 : %.2f / 差≤12 占 %.1f%%  ← 应明显更差"
      % (VR.ASSETS[wrong][0], rc["mean"], rc["ok"] * 100))
    big = np.array(Image.fromarray(asset, "RGBA").resize(
        (int(t.shape[1] * 1.2), int(t.shape[0] * 1.2)), Image.LANCZOS)).astype(np.float32)
    rb = compare(img, big[:, :, :3], big[:, :, 3] >= 250, pos, k=4)
    p("     缩放 ×1.2 ÷4 : %.2f / 差≤12 占 %.1f%%  ← 应明显更差" % (rb["mean"], rb["ok"] * 100))

    ab, shift = bias_and_shift(img, t, c, pos)
    p("  系统偏差(渲染 ≈ a×素材 + b):" + "  ".join(
        "ch%d a=%.3f b=%+.1f 残差%.1f" % (i, a, b, r) for i, (a, b, r) in enumerate(ab)))
    p("     → %s" % ("a≈1、b≈0 ⇒ 没有整体变亮/变淡/变色" if all(
        abs(a - 1) < 0.03 and abs(b) < 6 for a, b, _ in ab) else "⚠️ 有系统性颜色/透明度偏差"))
    p("  亚像素位移扫描:最优 dx=%.2f dy=%.2f → 平均差 %.2f(不移位 %.2f)"
      % (shift[1], shift[2], shift[0], compare(img, t, c, pos)["mean"]))

open(os.path.join(VR.OUT, "resample_forensics.txt"), "w", encoding="utf-8").write("\n".join(LINES))
