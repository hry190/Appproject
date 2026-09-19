# -*- coding: utf-8 -*-
"""Boss 评审结果面板取证:连拍 + 同时刻 dump(成对),供 clip2.py 与人工复核。

用法(人在设备上手动填完中文草稿并点「提交评审」之后跑):
    python scripts/chuangdang-regress/boss_capture.py pass
参数是标签:pass(通过态)/ fail(未通过态)/ top(提交前)。

为什么这一步必须**手动填草稿**:`adb shell input text` 输不进中文
(MIUI 上 NPE;拼音输入法会把注入的 ASCII 吞成「啧啧啧」),见 §11 坑 4 与 §26。
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from drive import OUT, dump_raw, find, sh  # noqa: E402

TAG = sys.argv[1] if len(sys.argv) > 1 else "pass"
D = os.path.join(OUT, "boss_" + TAG)
os.makedirs(D, exist_ok=True)


def cap(name, nodes):
    png, xml = os.path.join(D, name + ".png"), os.path.join(D, name + ".xml")
    sh("shell", "screencap", "-p", "/sdcard/s.png")
    sh("pull", "/sdcard/s.png", png)
    sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
    sh("pull", "/sdcard/u.xml", xml)
    print("%-26s %d 个文本节点" % (name, sum(1 for n in nodes if n[0])))
    for n in nodes:
        if n[0]:
            print("    %s" % n[0][:70])


nodes = dump_raw()[1]
cap("01-" + TAG, nodes)

# 往下滚,把结果面板的其余部分也拍下来(面板很长)
for k in range(3):
    sh("shell", "input", "swipe", "540", "1700", "540", "900", "300")
    nodes = dump_raw()[1]
    cap("%02d-scroll%d" % (k + 2, k), nodes)

print("")
print("-> %s" % D)
print("   接着跑: python scripts/chuangdang-regress/clip2.py \"%s\" \"%s\""
      % (D, os.path.join(D, "clip_report.txt")))
