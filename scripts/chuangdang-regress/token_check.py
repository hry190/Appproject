# -*- coding: utf-8 -*-
"""闯荡令余额的像素证据(功能回归)。

为什么不能只靠文字:三个令图标**永远**都渲染「令」字,点亮与否是**背景色**
(§6 的 CdGold vs 0x33FFFFFF)—— 文字读不出余额。故直接采样图标颜色。
地图页 `ChuangdangMapScreen` 里:`background(if (i < tokens) CdGold else Color(0x33FFFFFF))`。
"""
import os
import re
import sys

import numpy as np
from PIL import Image

RUN = os.path.join(os.environ.get("CD_TEST_DIR")
                   or os.path.join(os.environ["TEMP"], "cd-test"), "run")
GOLD = np.array([201, 162, 77])   # 参考值;脚本按实测打印,不强绑


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


for name in ("s0-map-start_f001", "s1-map-after_f011", "s4-map-after_f039"):
    png = os.path.join(RUN, name + ".png")
    xml = os.path.join(RUN, name + ".xml")
    img = np.array(Image.open(png).convert("RGB")).astype(np.int16)
    hits = [(t, x1, y1, x2, y2) for (t, x1, y1, x2, y2) in nodes_of(xml) if t == "令"]
    print("\n%s:找到 %d 个「令」节点" % (name, len(hits)))
    lit = 0
    for t, x1, y1, x2, y2 in sorted(hits, key=lambda h: h[1]):
        cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
        # 取图标圆面(排除「令」字本身):环形采样
        patch = img[cy - 26:cy + 27, cx - 26:cx + 27].reshape(-1, 3)
        bright = patch[patch.mean(axis=1) > 60]      # 去掉深色文字
        mean = bright.mean(axis=0) if len(bright) else patch.mean(axis=0)
        is_gold = bool((mean[0] - mean[2] > 25) and mean[0] > 120)
        lit += is_gold
        print("   令 #%d @(%d,%d) 圆面均色=%s → %s"
              % (len(hits) - lit if not is_gold else lit, cx, cy, mean.round(0),
                 "点亮(金)" if is_gold else "熄灭(灰)"))
    txt = [t for (t, *_r) in nodes_of(xml) if "枚" in t]
    print("   点亮数 = %d / 3;说明文字 = %s" % (lit, txt))
