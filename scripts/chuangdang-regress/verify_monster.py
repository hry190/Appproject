# -*- coding: utf-8 -*-
"""看第 1 关的铜齿门卫素材效果:进关后连拍(0 命中 / 命中后带伤痕)。

打法:进第 1 关 → 关掉剧情浮层 → 每次答对后截图,直到敌人掉 2 颗心。
素材是实图,所以这里主要靠**看图**,只用 dump 确认题目与血量文案。
"""
import os
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from drive import ADB, OUT, SERIAL, dump_raw, find
from qa import BY_PROMPT
import play as P

STAGE = int(sys.argv[1]) if len(sys.argv) > 1 else 1
D = os.path.join(OUT, "monster%d" % STAGE)
os.makedirs(D, exist_ok=True)
LOG = []


def adb(*a):
    subprocess.run([ADB, "-s", SERIAL] + list(a), capture_output=True)


def note(m):
    LOG.append(m)
    print(m.encode("ascii", "replace").decode("ascii"))


def cap(name):
    adb("shell", "screencap", "-p", "/sdcard/s.png")
    adb("pull", "/sdcard/s.png", os.path.join(D, name + ".png"))
    return name


def tap_node(n, wait=1.0):
    adb("shell", "input", "tap", str((n[2] + n[4]) // 2), str((n[3] + n[5]) // 2))
    time.sleep(wait)


def find_prompt(nodes):
    for n in nodes:
        if n[0] in BY_PROMPT:
            return n[0]
    return None


# 到地图 → 进目标关
#   注意要处理"战斗中途"这种情况:上一轮可能停在别的关卡里,
#   那时页面上没有「返回地图」,只有撤退箭头(desc=撤退)→ 弹框选「暂离」才回得到地图。
for _ in range(16):
    nodes = dump_raw()[1]
    if find(nodes, r"^通关印记"):
        t = find(nodes, r"^第 %d 关" % STAGE)
        if t:
            tap_node(t[0], 1.8)
            break
        note("!! 地图上没有第 %d 关" % STAGE)
        break
    b = find(nodes, r"^返回地图$") or find(nodes, r"^返回$")
    if b:
        tap_node(b[0], 1.5)
        continue
    r = find(nodes, r"^撤退$")
    if r:
        tap_node(r[0], 1.2)
        z = find(dump_raw()[1], r"^暂离$")
        if z:
            tap_node(z[0], 1.5)
        continue
    e = find(nodes, r"^闯荡江湖$")
    if e:
        tap_node(e[0], 2.0)
        continue
    en = find(nodes, r"^点击进入$")
    if en:
        tap_node(en[0], 5.0)
        continue
    note("!! 导航失败")
    for n in nodes:
        if n[0]:
            note("      " + n[0][:50])
    break

shot = 0
answered = 0
for step in range(60):
    nodes = dump_raw()[1]
    # 每帧都拍:血条是画出来的(无文字),dump 读不到,只能**事后按红色像素数挑**有伤害的那帧
    cap("f%02d" % step)

    if find(nodes, r"^(进入战斗|开始练习)$"):
        tap_node(find(nodes, r"^(进入战斗|开始练习)$")[0], 1.2)
        continue

    if find(nodes, r"^返回地图$"):
        note("[%02d] 战斗结束(通关)" % step)
        break

    cont = find(nodes, r"^(继续|迎击.*)$")
    if cont:
        tap_node(cont[0], 0.9)
        continue

    prompt = find_prompt(nodes)
    if prompt:
        kind, payload = BY_PROMPT[prompt]
        tap_node(P.do_answer(kind, payload, prompt, nodes, step), 1.0)
        answered += 1
        continue

    note("[%02d] 认不出的界面,停" % step)
    for n in nodes:
        if n[0]:
            note("      " + n[0][:50])
    break

note("共作答 %d 次" % answered)

with open(os.path.join(D, "monster.log"), "w", encoding="utf-8") as f:
    f.write("\n".join(LOG))
