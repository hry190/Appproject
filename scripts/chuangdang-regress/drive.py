# -*- coding: utf-8 -*-
"""闯荡江湖自动游玩 / 截图驱动。

用途:把 app 打到指定关卡与界面,采集截图供像素检测。
所有输出落在 `%TEMP%\\cd-test`(可用 `CD_TEST_DIR` 改),**故意不落仓库**。
"""
import os
import re
import subprocess
import sys
import time

# ── 可配置项(默认沿用本机历史值;换设备 / 换目录用环境变量)──────────────────
#   CD_SERIAL   :目标设备序列号(默认本机真机 21908b7a)。换机后先 `adb devices` 拿新号
#   CD_ADB      :adb.exe 完整路径(默认从 ANDROID_HOME / ANDROID_SDK_ROOT 拼出来)
#   CD_TEST_DIR :产物根目录(默认 %TEMP%\cd-test)
ADB = os.environ.get("CD_ADB") or os.path.join(
    os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or r"D:\Android\Sdk",
    "platform-tools", "adb.exe",
)
SERIAL = os.environ.get("CD_SERIAL", "21908b7a")
# 2026-09-19:除真机外还有模拟器同时在线 → 所有 adb 调用**必须显式指定 -s**,
# 否则报 "more than one device/emulator",整条自动化链一起失灵。
OUT = os.environ.get("CD_TEST_DIR") or os.path.join(os.environ["TEMP"], "cd-test")
SHOTS = os.path.join(OUT, "shots")
os.makedirs(SHOTS, exist_ok=True)

# 控制台按 UTF-8 输出,避免 GBK 乱码(§7 教训:别让终端显示影响判断)
try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass


def sh(*args, binary=False):
    r = subprocess.run([ADB, "-s", SERIAL] + list(args), capture_output=True)
    if r.returncode != 0:
        raise RuntimeError("adb %s failed: %s" % (" ".join(args), r.stderr.decode("utf-8", "replace")))
    return r.stdout if binary else r.stdout.decode("utf-8", "replace")


def dump():
    """UI 层级 dump,返回 [(text, desc, x1, y1, x2, y2, clickable)]。"""
    return dump_raw()[1]


def dump_raw():
    """返回 (xml 文本, 节点列表)。

    `uiautomator dump` 在界面动画期间会偶发失败("could not get idle state"),
    属瞬时错误 —— 重试几次即可,不要让它把长流程打断。
    """
    local = os.path.join(OUT, "u.xml")
    last = None
    for attempt in range(4):
        try:
            sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
            sh("pull", "/sdcard/u.xml", local)
            xml = open(local, encoding="utf-8").read()
            if xml.strip():
                break
        except Exception as e:
            last = e
            time.sleep(1.2)
    else:
        raise last if last else RuntimeError("dump 连续失败")
    out = []
    for m in re.finditer(r"<node[^>]*>", xml):
        tag = m.group(0)
        t = re.search(r'text="([^"]*)"', tag)
        d = re.search(r'content-desc="([^"]*)"', tag)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
        c = re.search(r'clickable="(\w+)"', tag)
        if not b:
            continue
        x1, y1, x2, y2 = (int(g) for g in b.groups())
        out.append(((t.group(1) if t else ""), (d.group(1) if d else ""),
                    x1, y1, x2, y2, (c.group(1) == "true" if c else False)))
    return xml, out


def shot(name):
    """截图并落盘,返回路径(同样对瞬时失败做重试)。"""
    path = os.path.join(SHOTS, name + ".png")
    last = None
    for attempt in range(4):
        try:
            sh("shell", "screencap", "-p", "/sdcard/s.png")
            sh("pull", "/sdcard/s.png", path)
            if os.path.exists(path) and os.path.getsize(path) > 0:
                return path
        except Exception as e:
            last = e
            time.sleep(1.2)
    raise last if last else RuntimeError("截图连续失败")


def tap(x, y, wait=0.6):
    sh("shell", "input", "tap", str(int(x)), str(int(y)))
    time.sleep(wait)


def find(nodes, pattern, min_h=0):
    """按正则找节点(匹配 text 或 content-desc),返回列表。"""
    rx = re.compile(pattern)
    hits = []
    for t, d, x1, y1, x2, y2, c in nodes:
        if rx.search(t) or rx.search(d):
            if (y2 - y1) >= min_h:
                hits.append((t, d, x1, y1, x2, y2, c))
    return hits


def show(nodes, ymin=0):
    print("%-40s %-22s %s" % ("text", "bounds", "clickable"))
    print("-" * 88)
    for t, d, x1, y1, x2, y2, c in nodes:
        if y2 < ymin or not (t or d):
            continue
        print("%-40s %-22s %s" % ((t or d)[:38], "[%d,%d][%d,%d]" % (x1, y1, x2, y2), "T" if c else ""))


if __name__ == "__main__":
    nodes = dump()
    print("共 %d 个节点" % len(nodes))
    show(nodes)
