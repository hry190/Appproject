# -*- coding: utf-8 -*-
"""§24 的 A/B 取证(修正版:用**固定槽位**,不做模板匹配)。

⚠️ 第一版踩的坑:拿 `locate()`(模板匹配)在这张**通关帧**上找位置,它返回了
   背景上的一个点(匹配误差 2572.9,而真槽位处是 3546)—— 通关态下战斗区的画面与
   干净素材差得较多(有伤痕叠加),于是"最不像背景"的点反而更"像"模板。
   结论:同一台设备、同一版式,**槽位坐标是固定的**,直接用它,不要每次重新匹配。

槽位坐标来自 verify_render 在**题目帧**上量的结果(题目帧没有伤痕叠加,匹配可靠):
   第 1 关 (718,330) 264×264 · 第 4 关 (694,330) 310×264
"""
import os
import sys

import numpy as np
from PIL import Image

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

OUT = os.environ.get("CD_TEST_DIR") or os.path.join(os.environ["TEMP"], "cd-test")
RUN = os.path.join(OUT, "run")
CROPS = os.path.join(OUT, "crops")
PAD = 14
SLOT = {1: ("盾", 718, 330, 264, 264), 4: ("鹤", 694, 330, 310, 264)}


def crop2x(img, x, y, w, h, path):
    c = img[max(0, y - PAD):y + h + PAD, max(0, x - PAD):x + w + PAD]
    im = Image.fromarray(c)
    im = im.resize((im.width * 2, im.height * 2), Image.LANCZOS)
    im.save(path)
    return np.array(im).astype(np.int16)


def counts(a):
    r, g, b = a[:, :, 0], a[:, :, 1], a[:, :, 2]
    red = int(((r - g > 55) & (r - b > 45) & (r < 240)).sum())
    strong = int(((r - g > 85) & (r - b > 70)).sum())
    # 平均"红度":半透明红仍然算红,但 (R-G) 会明显变小 —— 这是本次改动最直接的读数
    redness = float((r - g)[(r - g) > 40].mean()) if ((r - g) > 40).any() else 0.0
    return red, strong, redness


print("=" * 88)
print("§24 贯穿痕半透明化 · 改前/改后(同一槽位、同一裁法、均为通关帧满三心)")
print("=" * 88)
for f in sorted(os.listdir(RUN)):
    pass

rows = []
for stage, (glyph, x, y, w, h) in SLOT.items():
    vic = sorted(n for n in os.listdir(RUN)
                 if n.startswith("s%d-victory" % stage) and n.endswith(".png"))
    if not vic:
        print("!! 第 %d 关没有通关帧" % stage)
        continue
    after_img = np.array(Image.open(os.path.join(RUN, vic[-1])).convert("RGB"))
    after = crop2x(after_img, x, y, w, h, os.path.join(CROPS, "after_hit3_s%d.png" % stage))
    before = np.array(Image.open(os.path.join(CROPS, "hit3_s%d.png" % stage)).convert("RGB")).astype(np.int16)
    mh, mw = min(before.shape[0], after.shape[0]), min(before.shape[1], after.shape[1])
    before, after = before[:mh, :mw], after[:mh, :mw]

    rb, sb, mb = counts(before)
    ra, sa, ma = counts(after)
    print("")
    print("【第 %d 关 %s】裁自 %s" % (stage, glyph, vic[-1]))
    print("   改前:偏红 %6d  浓红 %6d  平均红度(R-G) %.1f" % (rb, sb, mb))
    print("   改后:偏红 %6d  浓红 %6d  平均红度(R-G) %.1f" % (ra, sa, ma))
    print("   Δ   :偏红 %+6d  浓红 %+6d  平均红度 %+.1f" % (ra - rb, sa - sb, ma - mb))
    if glyph == "鹤":
        ok = sa < sb * 0.95 or ma < mb - 2
        print("   → %s(贯穿痕改半透明:浓红/平均红度应下降,偏红仍在)"
              % ("✅ 如预期" if ok else "❌ 没看出变化"))
    else:
        print("   → 对照项(盾那一路没改):%s"
              % ("✅ 逐值一致" if (ra == rb and sa == sb) else "❌ 动了,说明改错了范围"))
    rows.append((stage, glyph, rb, sb, mb, ra, sa, ma))

    # 并排图:左=改前 右=改后
    strip = np.concatenate([before.astype(np.uint8), np.full((mh, 8, 3), 30, np.uint8),
                            after.astype(np.uint8)], axis=1)
    Image.fromarray(strip).save(os.path.join(CROPS, "ab_s%d.png" % stage))
print("\n→ crops/ab_s1.png(对照) / crops/ab_s4.png(改动)")
