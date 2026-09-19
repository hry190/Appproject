# -*- coding: utf-8 -*-
"""五关素材 · 渲染层校验(回归第二层):真机上"渲染出来的就是这张源文件",且尺寸=Fit 理论值。

判据与正负对照(照 2026-09-19 §11 的规矩:没有正/负对照的"通过"不算数):
  · 正向匹配:把源素材按理论 Fit 尺寸缩放成模板,带 **alpha 掩膜**在整屏上做 SSD 匹配,
    取最优位置。核心像素(alpha≥250)处应与截图**逐像素相同**(合成只在半透明边上发生)。
  · 负对照:同一屏用**另外四个素材**的模板各匹配一遍 —— 正确的那张必须明显胜出。
    若五张分数接近,说明这个判据分辨不出东西(和 §11 的 v1 一样是废判据)。
  · 尺寸正对照:对获胜素材扫 0.90/0.95/1.00/1.05/1.10 × Fit 五档,
    **最小值必须落在 1.00** —— 否则说明真实渲染尺寸不是 Fit 理论值。

密度 2.75(440dpi):战斗页槽位 132×96dp = 363×264px;Boss 页 64dp = 176×176px。
"""
import json
import os
import re
import sys

import numpy as np
from PIL import Image
from scipy.signal import fftconvolve

HERE = os.path.dirname(os.path.abspath(__file__))
_REPO = os.path.dirname(os.path.dirname(HERE))
OUT = os.environ.get("CD_TEST_DIR") or os.path.join(os.environ["TEMP"], "cd-test")
RUN = os.path.join(OUT, "run")
RES = os.path.join(_REPO, "android", "app", "src", "main", "res", "drawable-nodpi")
DENSITY = 2.75
BATTLE_SLOT = (132 * DENSITY, 96 * DENSITY)   # 363 × 264
BOSS_SLOT = (64 * DENSITY, 64 * DENSITY)      # 176 × 176

ASSETS = {
    "盾": ("第1关 铜齿门卫", "img_chuangdang_tongchimenwei.png", BATTLE_SLOT),
    "蝠": ("第2关 断目机关蝠", "img_chuangdang_duanmujiguanfu.png", BATTLE_SLOT),
    "棋": ("第3关 棋冠石将", "img_chuangdang_qiguanshixiang.png", BATTLE_SLOT),
    "鹤": ("第4关 百声纸鹤", "img_chuangdang_baishengzhihe.png", BATTLE_SLOT),
    "枢": ("终局 百面机枢", "img_chuangdang_baimianjishu.png", BOSS_SLOT),
}

OUTL = []


def p(s=""):
    OUTL.append(s)
    print(s.encode("ascii", "replace").decode("ascii"))


def load_asset(fn):
    im = Image.open(os.path.join(RES, fn)).convert("RGBA")
    return np.array(im)


def fit_scale(asset_wh, slot):
    w, h = asset_wh
    return min(slot[0] / w, slot[1] / h)


def render_template(asset, slot, k=1.0, div=2):
    """按 Fit × k 缩放素材,返回 (rgb, mask_core) —— 直接在 ÷div 分辨率上做。"""
    w, h = asset.shape[1], asset.shape[0]
    s = fit_scale((w, h), slot) * k / div
    tw, th = max(2, int(round(w * s))), max(2, int(round(h * s)))
    im = Image.fromarray(asset, "RGBA").resize((tw, th), Image.LANCZOS)
    a = np.array(im).astype(np.float32)
    rgb = a[:, :, :3]
    core = (a[:, :, 3] >= 250).astype(np.float32)
    return rgb, core, s


def masked_ssd_mean(img, tpl, core):
    """掩膜 SSD 的均值:返回 (best_mean_absdiff, (x, y))。img/tpl 同分辨率。"""
    m = core
    n = float(m.sum())
    if n < 100:
        return None
    t = tpl * m[:, :, None]
    i2 = (img ** 2).sum(axis=2)
    # 交叉项:I 与 (m*T) 的相关
    cross = np.zeros((img.shape[0] - tpl.shape[0] + 1, img.shape[1] - tpl.shape[1] + 1), np.float32)
    for c in range(3):
        cross += fftconvolve(img[:, :, c], t[::-1, ::-1, c], mode="valid")
    # I² 与 m 的相关
    sq = fftconvolve(i2, m[::-1, ::-1], mode="valid")
    t2 = float((t ** 2).sum())
    ssd = t2 - 2 * cross + sq
    idx = int(np.argmin(ssd))
    yy, xx = np.unravel_index(idx, ssd.shape)
    return float(ssd[yy, xx]) / n, (int(xx), int(yy))


def diff_stats(img, tpl, core, pos):
    x, y = pos
    h, w = tpl.shape[:2]
    sub = img[y:y + h, x:x + w]
    d = np.abs(sub - tpl).max(axis=2)
    sel = core > 0
    return dict(mean=float(d[sel].mean()), p99=float(np.percentile(d[sel], 99)),
                ok_share=float((d[sel] <= 8).mean()), n=int(sel.sum()))


def structural_diff(img, tpl, core, pos, k=2):
    """结构一致性:双方都做 k× 盒式降采样再比。

    为什么要这一步(而不是只报"逐像素差"):素材在 96dp 槽位里被**大幅缩小**
    (如 1254→264px,4.7 倍),Android 的线性滤波与 PIL 的 LANCZOS 在高频细节上
    本来就会给出不同的值 —— 那是**重采样差异**,不是"渲染错了"。
    降采样后比,能把"是不是这张图、结构对不对"和"用哪种滤波"分开。
    """
    x, y = pos
    h, w = tpl.shape[:2]
    sub = img[y:y + h, x:x + w].astype(np.float32)
    a = sub[: (h // k) * k, : (w // k) * k].reshape(h // k, k, w // k, k, 3).mean(axis=(1, 3))
    b = tpl[: (h // k) * k, : (w // k) * k].reshape(h // k, k, w // k, k, 3).mean(axis=(1, 3))
    m = (core[: (h // k) * k, : (w // k) * k]
         .reshape(h // k, k, w // k, k).mean(axis=(1, 3)) > 0.6)
    d = np.abs(a - b).max(axis=2)[m]
    return dict(mean=float(d.mean()), p99=float(np.percentile(d, 99)),
                ok=float((d <= 12).mean()))


def analyze_frame(png, asset_glyph, slot, label):
    img_full = np.array(Image.open(png).convert("RGB"))
    img = img_full[::2, ::2].astype(np.float32)

    results = {}
    for g, (nm, fn, gslot) in ASSETS.items():
        # 负对照刻意**不排除异槽位素材**:把每张都按当前槽位 Fit 一遍再比,
        # 五张里必须只有本关那张胜出 —— 排除掉就少了一半的对照强度。
        rgb, core, s = render_template(load_asset(fn), slot, 1.0)
        r = masked_ssd_mean(img, rgb, core)
        if r:
            results[g] = dict(score=r[0], pos=r[1], scale=s)

    order = sorted(results.items(), key=lambda kv: kv[1]["score"])
    p("")
    p("─" * 104)
    p("【%s】%s" % (label, os.path.basename(png)))
    p("  负对照(同一屏,五个模板各自的最优掩膜平均误差 —— 越小越像;正确的那张必须明显胜出):")
    for g, r in order:
        mark = " ← 本关素材" if g == asset_glyph else ""
        p("     %s %-18s 误差 %6.2f   @(%4d,%4d)%s"
          % ("★" if g == asset_glyph else " ", ASSETS[g][0], r["score"], r["pos"][0] * 2, r["pos"][1] * 2, mark))

    if asset_glyph not in results:
        p("  !! 本关素材不在该槽位体系里")
        return None
    best = order[0][0]
    own = results[asset_glyph]
    others = [r["score"] for g, r in results.items() if g != asset_glyph]
    verdict_neg = "✅ 正确素材胜出" if best == asset_glyph else "❌ 判据失效:胜出的是 %s" % best
    p("  → %s(本关 %.2f vs 他关最小 %.2f,倍数 %.1f×)"
      % (verdict_neg, own["score"], min(others) if others else -1,
         (min(others) / own["score"]) if others and own["score"] > 0 else -1))

    # 尺寸正对照
    curve = {}
    for k in (0.90, 0.95, 1.00, 1.05, 1.10):
        rgb, core, s = render_template(load_asset(ASSETS[asset_glyph][1]), slot, k)
        r = masked_ssd_mean(img, rgb, core)
        if r:
            curve[k] = r[0]
    best_k = min(curve, key=curve.get)
    p("  尺寸正对照(×Fit):" + "  ".join("%.2f→%.2f" % (k, v) for k, v in sorted(curve.items())))
    p("  → 最优缩放 %.2f  %s" % (best_k, "✅ =Fit(尺寸正确)" if best_k == 1.00 else "❌ 偏离 Fit"))

    # 全分辨率精修 + 逐像素
    rgb_f, core_f, s_full = render_template(load_asset(ASSETS[asset_glyph][1]), slot, 1.0, div=1)
    cx, cy = own["pos"][0] * 2, own["pos"][1] * 2
    best = (1e18, None)
    for dy in range(-6, 7):
        for dx in range(-6, 7):
            y, x = cy + dy, cx + dx
            if y < 0 or x < 0 or y + rgb_f.shape[0] > img_full.shape[0] or x + rgb_f.shape[1] > img_full.shape[1]:
                continue
            sub = img_full[y:y + rgb_f.shape[0], x:x + rgb_f.shape[1]].astype(np.float32)
            d = np.abs(sub - rgb_f).max(axis=2)
            sel = core_f > 0
            v = float(d[sel].mean())
            if v < best[0]:
                best = (v, (x, y), d, sel)
    v, (x, y), d, sel = best
    aw = load_asset(ASSETS[asset_glyph][1])
    op = aw[:, :, 3] > 8
    ys, xs = np.where(op)
    bw, bh = xs.max() - xs.min() + 1, ys.max() - ys.min() + 1
    scale_px = fit_scale((aw.shape[1], aw.shape[0]), slot)
    st = structural_diff(img_full.astype(np.float32), rgb_f, core_f, (x, y))
    p("  精确匹配:位置=(%d,%d) 全尺寸=%dx%dpx(%.1f×%.1fdp)"
      % (x, y, rgb_f.shape[1], rgb_f.shape[0],
         rgb_f.shape[1] / DENSITY, rgb_f.shape[0] / DENSITY))
    p("  源素材主体 %dx%dpx → 按 Fit 应显示 %dx%dpx = %.1f×%.1fdp"
      % (bw, bh, round(bw * scale_px), round(bh * scale_px),
         bw * scale_px / DENSITY, bh * scale_px / DENSITY))
    p("  结构一致性(2× 降采样,排除重采样滤波差异):平均差 %.2f,p99 %.1f,差≤12 占 %.2f%%  %s"
      % (st["mean"], st["p99"], st["ok"] * 100,
         "✅ 结构就是这张源文件" if st["ok"] > 0.97 else "⚠️ 结构有差异"))
    p("  逐像素(同分辨率,受滤波差异影响,仅供参考):平均差 %.2f,差≤8 占 %.2f%%"
      % (d[sel].mean(), (d[sel] <= 8).mean() * 100))
    return dict(glyph=asset_glyph, pos=(x, y), size=(int(rgb_f.shape[1]), int(rgb_f.shape[0])),
                body_px=(round(bw * scale_px), round(bh * scale_px)),
                body_dp=(round(bw * scale_px / DENSITY, 1), round(bh * scale_px / DENSITY, 1)),
                mean=float(d[sel].mean()), ok_share=float(st["ok"]),
                neg_margin=float(min(others)) if others else None,
                neg_winner=best, scale_best=best_k, curve=curve)


def main():
    frames = json.load(open(os.path.join(RUN, "frames.json"), encoding="utf-8"))
    # 每关取**第一张真实战斗/页面帧**作为渲染基准。
    #   ⚠️ 踩坑:关前剧情浮层是模态,盖住整页 —— 拿它当基准会匹配到背景上去
    #   (帧名形如 `s1-intro_f002`,所以判据要用正则找 `-qNN-`,不能按前缀排除)。
    picks = {}
    for f in frames:
        st = f["stage"]
        if st in (0, None):
            continue
        if re.search(r"-q\d\d-", f["name"]) or re.match(r"^s5-boss-top", f["name"]):
            picks.setdefault(st, f)

    p("=" * 104)
    p("五关素材 · 渲染层校验(真机截图 vs 源文件)")
    p("=" * 104)
    p("设备密度 2.75(440dpi);战斗页槽位 132×96dp=363×264px;Boss 页 64dp=176×176px")
    summary = []
    for st in (1, 2, 3, 4, 5):
        glyph = {1: "盾", 2: "蝠", 3: "棋", 4: "鹤", 5: "枢"}[st]
        f = picks.get(st)
        if not f:
            p("")
            p("!! 第 %d 关没有采到战斗帧" % st)
            continue
        slot = BOSS_SLOT if st == 5 else BATTLE_SLOT
        r = analyze_frame(os.path.join(RUN, f["name"] + ".png"), glyph, slot,
                          "第 %d 关 / %s(帧 %s)" % (st, ASSETS[glyph][0], f["name"]))
        if r:
            summary.append((st, r))

    p("")
    p("=" * 104)
    p("汇总:实测渲染尺寸 vs §22 理论表")
    p("─" * 104)
    theory = {1: ("96×96dp", "96 × 96"), 2: ("132×88dp", "132 × 88"), 3: ("74×96dp", "74 × 96"),
              4: ("96×82dp", "96 × 82"), 5: ("60×64dp", "60 × 64")}
    p("%-6s %-14s %-16s %-16s %-10s %s" % ("关", "素材", "实测(源主体换算)", "§22 理论(fit 后整图)", "像素一致", "判定"))
    for st, r in summary:
        tw, t = theory[st]
        p("%-6d %-14s %-16s %-16s %-10s %s" % (
            st, ASSETS[r["glyph"]][0][:12],
            "%.1f×%.1fdp" % r["body_dp"], t + "(整图)", "%.2f%%" % (r["ok_share"] * 100),
            ("✅" if r["ok_share"] > 0.97 and r["scale_best"] == 1.0 else "⚠️")))

    open(os.path.join(OUT, "render_check.txt"), "w", encoding="utf-8").write("\n".join(OUTL))
    print("-> " + os.path.join(OUT, "render_check.txt"))


if __name__ == "__main__":
    main()
