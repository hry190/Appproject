# -*- coding: utf-8 -*-
"""五关素材 · 命中效果校验(回归第三层)。

策划 §5 的战斗表现,五个敌人现在是"素材 + 叠加层"(§16/§17/§18/§20):
  盾 爪痕(每命中一道,满三心再加两道贯穿痕)
  蝠 声波环(每命中一圈)
  棋 石台崩落(每命中一块楔形缺口,且**必须落在石台上**)—— §21 重算过归一化坐标
  鹤 通用受损标记
  枢 无(终局页没有三心)

判据是**差分**,不是绝对值:命中标记是叠加层,素材本身的暖色也会"偏红",
所以先量 hit0 作基线,再看每一级命中的**增量** —— 这样阈值不用猜,也不会被素材自身颜色骗到。
（§16 的教训:换素材后"红"的定义会变,所以判据应该建立在"同一张素材前后比"上。）
"""
import json
import os
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import verify_render as VR  # noqa: E402

RUN = VR.RUN
GLYPH = {1: "盾", 2: "蝠", 3: "棋", 4: "鹤"}
OUTL = []


def p(s=""):
    OUTL.append(s)
    print(s.encode("ascii", "replace").decode("ascii"))


def locate(png, glyph):
    """返回 (x, y, w, h) 全分辨率下素材的渲染矩形(Fit 理论尺寸)。"""
    slot = VR.BATTLE_SLOT
    img = np.array(Image.open(png).convert("RGB"))[::2, ::2].astype(np.float32)
    rgb, core, _ = VR.render_template(VR.load_asset(VR.ASSETS[glyph][1]), slot, 1.0)
    score, (x, y) = VR.masked_ssd_mean(img, rgb, core)
    return x * 2, y * 2, rgb.shape[1] * 2, rgb.shape[0] * 2, score


def red_mask(a):
    """偏红掩膜:命中标记用 Danger(155,59,46) 与 DamageFaint(0.7 叠底)。"""
    r = a[:, :, 0].astype(np.int16)
    g = a[:, :, 1].astype(np.int16)
    b = a[:, :, 2].astype(np.int16)
    return (r - g > 55) & (r - b > 45) & (r < 240)


def hit_levels(frames, stage):
    """从结算文案推出**每帧自身的命中数**(含本帧刚造成的那一下)。

    ⚠️ 两个口径错误都踩过,记下来:
      1. 关前剧情浮层(`sN-intro_fNNN`)是模态遮住整页的,拿去当 hit0 基线会让匹配落到背景上;
      2. 结算面板与战斗画面**同屏**(advance() 的副作用,§11 记过)——所以"标题写着命中!"
         的那一帧里,怪物**已经**带着这道伤痕了。命中数必须**含本帧**,不能取"进这一帧之前"。
    """
    lv = 0
    out = []
    for f in frames:
        if f["stage"] != stage:
            continue
        if "-intro" in f["name"] or "boss" in f["name"] or "map" in f["name"]:
            continue
        text = " | ".join(f["texts"])
        dmg = 1 if (("-1 心" in text) and ("识破一式" in text or "命中!" in text)) else 0
        lv += dmg
        out.append(dict(name=f["name"], level=lv, gain=dmg, t=f["t"],
                        blocked=("被格挡了" in text)))
    return out, lv


def main():
    frames = json.load(open(os.path.join(RUN, "frames.json"), encoding="utf-8"))
    p("=" * 100)
    p("五关素材 · 命中效果校验(各级命中的红色增量落在素材上、且位置符合预期)")
    p("=" * 100)

    pos_cache = {}
    results = []
    for stage in (1, 2, 3, 4):
        glyph = GLYPH[stage]
        levels, total = hit_levels(frames, stage)
        p("")
        p("─" * 100)
        p("【第 %d 关 %s】共挨 %d 下(从结算文案数出来)" % (stage, VR.ASSETS[glyph][0], total))
        if total == 0:
            p("  !! 本关没有任何命中,无法验证命中效果")
            continue

        # 每个等级取"该等级最后一帧"(叠加层已画上,后续帧才开始变);
        # hit0 的基线用**该关第一张题目帧**(那时还没挨过打,怪物槽位与结算帧完全相同)。
        reps = {}
        for e in levels:
            reps[e["level"]] = e["name"]
        base = next((f["name"] for f in frames
                     if f["stage"] == stage and "-q01-" in f["name"]), None)
        reps[0] = base
        if base is None:
            p("  !! 没有 hit0 帧作基线")
            continue
        x, y, w, h, score = locate(os.path.join(RUN, base + ".png"), glyph)
        pos_cache[stage] = dict(x=x, y=y, w=w, h=h, match=score)
        p("  素材渲染矩形 = (%d,%d) %dx%dpx  [匹配误差 %.2f]" % (x, y, w, h, score))

        base_img = np.array(Image.open(os.path.join(RUN, base + ".png")).convert("RGB"))[y:y + h, x:x + w]
        base_red = int(red_mask(base_img).sum())
        p("  基线(hit0):矩形内偏红像素 = %d(素材自身颜色)" % base_red)
        p("  %-6s %-26s %-10s %-10s %-12s %s" % ("命中", "帧", "红像素", "相对基线", "变化像素", "变化区域(占素材)"))
        for lv in sorted(reps):
            img = np.array(Image.open(os.path.join(RUN, reps[lv] + ".png")).convert("RGB"))[y:y + h, x:x + w]
            red = int(red_mask(img).sum())
            d = np.abs(img.astype(np.int16) - base_img.astype(np.int16)).max(axis=2)
            changed = d > 12
            n = int(changed.sum())
            box = "-"
            if n > 0:
                ys, xs = np.where(changed)
                box = "x%.2f~%.2f y%.2f~%.2f" % (xs.min() / w, xs.max() / w, ys.min() / h, ys.max() / h)
            p("  %-6d %-26s %-10d %-10s %-12d %s"
              % (lv, reps[lv], red, "%+d" % (red - base_red), n, box))
            results.append(dict(stage=stage, glyph=glyph, level=lv, red=red, base_red=base_red,
                                changed=n, box=box))

    p("")
    p("=" * 100)
    p("判定")
    p("─" * 100)
    for stage in (1, 2, 3, 4):
        rs = [r for r in results if r["stage"] == stage]
        if not rs:
            continue
        g = GLYPH[stage]
        reds = {r["level"]: r["red"] for r in rs}
        lv = sorted(reds)
        mono = all(reds[lv[i]] <= reds[lv[i + 1]] for i in range(len(lv) - 1))
        grew = reds[lv[-1]] > reds.get(0, 0)
        extra = ""
        if g == "棋":
            # 石台在素材高度的 66%~91%(§21 实测);变化区域必须落在这条带里
            last = [r for r in rs if r["level"] == lv[-1]][0]
            try:
                y0 = float(last["box"].split("y")[1].split("~")[0])
                y1 = float(last["box"].split("y")[1].split("~")[1].split(" ")[0])
                inside = y0 >= 0.55 and y1 <= 0.98
                extra = ";崩落带 y%.2f~%.2f %s(§21 期望 0.66~0.91)"
                extra = extra % (y0, y1, "✅ 在石台上" if inside else "❌ 偏离石台")
            except Exception:
                extra = ""
        p("  第 %d 关 %s:偏红 %s  %s%s"
          % (stage, VR.ASSETS[g][0],
             "→".join(str(reds[k]) for k in lv),
             "✅ 随命中单调增加" if (mono and grew) else "❌ 没有随命中增加", extra))

    open(os.path.join(VR.OUT, "hits_check.txt"), "w", encoding="utf-8").write("\n".join(OUTL))
    json.dump(pos_cache, open(os.path.join(VR.OUT, "slot_pos.json"), "w"), indent=1)


if __name__ == "__main__":
    main()
