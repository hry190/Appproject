# -*- coding: utf-8 -*-
"""地图页徽记截图:导航到地图页 → 截图 + dump,并按徽记几何裁出人眼可看的裁片。

用法: python regress/map_shot.py <标签>
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import five_stage_run as R  # noqa: E402
from drive import OUT, find, sh  # noqa: E402

TAG = sys.argv[1] if len(sys.argv) > 1 else "map"
D = os.path.join(OUT, "mapcheck2")
os.makedirs(D, exist_ok=True)

nodes = R.to_map()
if nodes is None:
    print("!! 到不了地图页")
    sys.exit(1)

png = os.path.join(D, TAG + ".png")
xml = os.path.join(D, TAG + ".xml")
sh("shell", "screencap", "-p", "/sdcard/s.png")
sh("pull", "/sdcard/s.png", png)
sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
sh("pull", "/sdcard/u.xml", xml)
print("-> %s" % png)

for n in nodes:
    if n[0] in ("可挑战", "继续", "已通关", "未解锁") or n[0].startswith("第 ") or n[0].startswith("终局"):
        print("   %-24s y=%d" % (n[0][:24], n[3]))

# 徽记在每张卡左端:卡片的 y 区间由「第 N 关 · …」标题行给出,徽记与该标题同高
print("\n徽记位置(据标题行推算,徽记 40dp=110px,与标题行垂直居中):")
for h in find(nodes, r"^(第 \d 关|终局)"):
    print("   %-22s 标题 y=[%d,%d] → 徽记约 x=[24,134] y=[%d,%d]"
          % (h[0][:22], h[3], h[5], h[3] - 20, h[3] + 90))
