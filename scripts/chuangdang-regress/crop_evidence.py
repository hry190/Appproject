# -*- coding: utf-8 -*-
"""把每关素材的"真机裁片 / 期望模板 / 差异"三张图并排落盘 —— 给人眼看的证据。

数字只说"哪里不对",要判断"这算不算问题"必须看图(§11/§12 的老规矩)。
判据:差异如果集中在**边界**(1px 抗锯齿)或**被别的 UI 压住的带**(如血条),
那就不算渲染问题;如果差异落在素材主体内部,才要查。
"""
import json
import os
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import verify_render as VR  # noqa: E402

D = os.path.join(VR.RUN, "..", "crops")
os.makedirs(D, exist_ok=True)

STAGE = {1: ("盾", VR.BATTLE_SLOT), 2: ("蝠", VR.BATTLE_SLOT), 3: ("棋", VR.BATTLE_SLOT),
         4: ("鹤", VR.BATTLE_SLOT), 5: ("枢", VR.BOSS_SLOT)}

frames = json.load(open(os.path.join(VR.RUN, "frames.json"), encoding="utf-8"))
picks = {}
for f in frames:
    st = f["stage"]
    if st in (0, None):
        continue
    if "-q01-" in f["name"] or f["name"].startswith("s5-boss-top"):
        picks.setdefault(st, f)

lines = []
for st in (1, 2, 3, 4, 5):
    glyph, slot = STAGE[st]
    f = picks.get(st)
    png = os.path.join(VR.RUN, f["name"] + ".png")
    img = np.array(Image.open(png).convert("RGB"))
    small = img[::2, ::2].astype(np.float32)
    tpl_s, core_s, _ = VR.render_template(VR.load_asset(VR.ASSETS[glyph][1]), slot, 1.0)
    _, (sx, sy) = VR.masked_ssd_mean(small, tpl_s, core_s)
    x, y = sx * 2, sy * 2
    tpl, core, _ = VR.render_template(VR.load_asset(VR.ASSETS[glyph][1]), slot, 1.0, div=1)
    h, w = tpl.shape[:2]

    crop = img[y:y + h, x:x + w].astype(np.int16)
    d = np.abs(crop - tpl.astype(np.int16)).max(axis=2)
    sel = core > 0

    # 三张并排(crop / 模板合成到灰底 / 差异放大 4×)
    tpl_vis = np.where(core[:, :, None] > 0, tpl, 40).astype(np.uint8)
    diff_vis = np.clip(d * 4, 0, 255).astype(np.uint8)
    strip = np.concatenate([crop.astype(np.uint8), tpl_vis,
                            np.repeat(diff_vis[:, :, None], 3, axis=2)], axis=1)
    Image.fromarray(strip).save(os.path.join(D, "s%d_%s.png" % (st, glyph)))

    # 差异的空间分布:按"离素材边界的距离"分带(0-2px 边界 / 内部)
    from scipy import ndimage
    inner = ndimage.binary_erosion(sel, iterations=3)
    border = sel & ~inner
    lines.append("第 %d 关 %s:矩形 (%d,%d) %dx%d;差异 边界带 %.1f / 内部 %.1f;内部差≤12 占 %.1f%%"
                 % (st, VR.ASSETS[glyph][0], x, y, w, h,
                    d[border].mean(), d[inner].mean() if inner.any() else -1,
                    (d[inner] <= 12).mean() * 100 if inner.any() else -1))
    # 内部差异最大的行(看是不是被 UI 压住的一条带)
    if inner.any():
        rowmean = np.where(inner, d, 0).mean(axis=1)
        top = np.argsort(rowmean)[-3:][::-1]
        lines.append("     内部差异最大的行: %s"
                     % ", ".join("y=%d(%.0f%%,均值%.0f)" % (r, 100.0 * r / h, rowmean[r]) for r in top))

open(os.path.join(D, "crop_report.txt"), "w", encoding="utf-8").write("\n".join(lines))
print("\n".join(lines).encode("ascii", "replace").decode("ascii"))
print("-> " + D)
