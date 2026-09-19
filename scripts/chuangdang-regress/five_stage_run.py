# -*- coding: utf-8 -*-
"""完整五关回归 · 自动过关 + 逐帧采集。

跑一遍"从头到尾":关 1 → 关 2 → 关 3 → 关 4 → 终局(Boss 页),
每一帧都落一份 **png + 同时刻 xml**,并记录当帧的全部文本 —— 后面三层校验都吃这份语料:
  · verify_render.py —— 素材在屏上的位置/尺寸/像素一致性
  · verify_hits.py   —— 命中效果随命中数变化
  · clip2.py         —— 文字贴边/裁剪(§12/§19 回归)

前提:进程必须是**刚起的**(进度在进程内,装机/强停即重置)。⚠️ 中途不要 force-stop(§17 踩过的坑)。
用法: python regress/five_stage_run.py
"""
import json
import os
import re
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from drive import OUT, dump_raw, find, sh  # noqa: E402
from qa import BY_PROMPT  # noqa: E402
import play as P  # noqa: E402

D = os.path.join(OUT, "run")
os.makedirs(D, exist_ok=True)
LOG = []
FRAMES = []
_frame_no = 0
_t0 = time.time()


def note(m):
    LOG.append("[%6.1fs] %s" % (time.time() - _t0, m))
    print(m.encode("ascii", "replace").decode("ascii"))


def capture(tag, nodes, stage):
    """截图 + 同时刻 dump(成对,供 clip2 用),并记下本帧全部文本。"""
    global _frame_no
    _frame_no += 1
    name = "%s_f%03d" % (tag, _frame_no)
    png = os.path.join(D, name + ".png")
    xml = os.path.join(D, name + ".xml")
    sh("shell", "screencap", "-p", "/sdcard/s.png")
    sh("pull", "/sdcard/s.png", png)
    try:
        sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
        sh("pull", "/sdcard/u.xml", xml)
    except Exception as e:
        note("  xml 落盘失败: %s" % e)
    texts = [n[0] for n in nodes if n[0]]
    FRAMES.append(dict(
        name=name, stage=stage, t=round(time.time() - _t0, 1),
        texts=texts,
        stage_label=next((n[0] for n in nodes
                          if re.match(r"^(第 \d 关|终局)", n[0]) and n[3] < 400), None),
    ))
    return name


def tap_node(n, wait=0.9):
    cx, cy = (n[2] + n[4]) // 2, (n[3] + n[5]) // 2
    sh("shell", "input", "tap", str(cx), str(cy))
    time.sleep(wait)


def find_prompt(nodes):
    for n in nodes:
        if n[0] in BY_PROMPT:
            return n[0]
    return None


def to_map(max_tries=18):
    """无论现在在哪,回到地图页(有「通关印记」即是)。"""
    for _ in range(max_tries):
        nodes = dump_raw()[1]
        if find(nodes, r"^通关印记"):
            return nodes
        b = find(nodes, r"^返回地图$") or find(nodes, r"^返回$")
        if b:
            tap_node(b[0], 1.5)
            continue
        r = find(nodes, r"^撤退$")
        if r:
            tap_node(r[0], 1.0)
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
        note("  to_map: 认不出界面 → 上滑")
        sh("shell", "input", "swipe", "540", "1700", "540", "600", "300")
        time.sleep(0.7)
    return None


def run_stage(stage_no):
    """打一关,直到出现「返回地图」。返回本关帧列表。"""
    tag = "s%d" % stage_no
    mine = []
    answered = 0
    for _ in range(80):
        nodes = dump_raw()[1]

        intro = find(nodes, r"^(进入战斗|开始练习)$")
        if intro:
            mine.append(capture("%s-intro" % tag, nodes, stage_no))
            note("  关前剧情 → 点「%s」" % intro[0][0])
            tap_node(intro[0], 1.3)
            continue

        back = find(nodes, r"^返回地图$")
        if back:
            mine.append(capture("%s-victory" % tag, nodes, stage_no))
            note("  ★ 通关面板(本关作答 %d 次)" % answered)
            tap_node(back[0], 1.4)
            return mine

        cont = find(nodes, r"^(继续|迎击.*)$")
        if cont:
            mine.append(capture("%s-resolve" % tag, nodes, stage_no))
            note("  结算 → 「%s」" % cont[0][0])
            tap_node(cont[0], 0.9)
            continue

        prompt = find_prompt(nodes)
        if prompt:
            kind, payload = BY_PROMPT[prompt]
            name = capture("%s-q%02d-%s" % (tag, answered + 1, kind), nodes, stage_no)
            mine.append(name)
            answered += 1
            note("  Q%d [%s] %s" % (answered, kind, prompt[:34]))
            tap_node(P.do_answer(kind, payload, prompt, nodes, 0), 1.0)
            continue

        note("  !! 认不出的界面,停。文本:")
        for n in nodes[:40]:
            if n[0]:
                note("      " + n[0][:60])
        mine.append(capture("%s-stuck" % tag, nodes, stage_no))
        return mine

    note("  !! 第 %d 关 80 步未结束" % stage_no)
    return mine


def main():
    # ── 起点:地图页 ───────────────────────────────────────────────────────
    nodes = to_map()
    if nodes is None:
        note("!! 到不了地图页,停")
        return _flush()
    capture("s0-map-start", nodes, 0)
    toks = [n[0] for n in nodes if "枚" in n[0] or "恢复" in n[0]]
    note("地图页(起始):%s" % toks)
    for n in nodes:
        if re.match(r"^(第 \d 关|终局)", n[0]) or n[0] in ("可挑战", "继续", "已通关", "未解锁"):
            note("    %-10s y=%d" % (n[0][:10], n[3]))

    for stage_no in (1, 2, 3, 4):
        nodes = to_map()
        if nodes is None:
            note("!! 回不到地图,第 %d 关放弃" % stage_no)
            break
        tgt = find(nodes, r"^第 %d 关" % stage_no)
        if not tgt:
            note("!! 地图上没有第 %d 关" % stage_no)
            break
        note("═══ 第 %d 关:点「%s」 ═══" % (stage_no, tgt[0][0][:12]))
        tap_node(tgt[0], 1.8)

        # 选关后可能要先点「出发」才进战斗
        n2 = dump_raw()[1]
        if not (find_prompt(n2) or find(n2, r"^(进入战斗|开始练习)$")):
            s = find(n2, r"出发$|继续挑战")
            if s:
                note("  地图 → 点「%s」" % s[0][0])
                tap_node(s[0], 1.8)
        run_stage(stage_no)

        nodes = to_map()
        if nodes:
            capture("s%d-map-after" % stage_no, nodes, stage_no)
            st = [n[0] for n in nodes if n[0] in ("可挑战", "继续", "已通关")]
            note("  地图:关卡状态 = %s" % st)

    # ── 终局:Boss 页(百面机枢)────────────────────────────────────────────
    nodes = to_map()
    if nodes:
        t = find(nodes, r"^终局")
        if t:
            note("═══ 终局:点「%s」 ═══" % t[0][0][:12])
            tap_node(t[0], 2.4)
            for i in range(10):
                nodes = dump_raw()[1]
                if find(nodes, r"^(制作任务|提交评审|我的小机关设计卡)$"):
                    break
                b = find(nodes, r"^返回$")
                if b:
                    break
                time.sleep(1.0)
            capture("s5-boss-top", nodes, 5)
            note("  Boss 页已截:含「允许的辅助工具」=%s"
                 % any("允许的辅助工具" in n[0] for n in nodes))
            # 往下滚一点,拍评审标准区(顺带覆盖 clip2 的"视口截断"分支)
            for k in range(2):
                sh("shell", "input", "swipe", "540", "1700", "540", "900", "300")
                time.sleep(0.8)
                nodes = dump_raw()[1]
                capture("s5-boss-scroll%d" % (k + 1), nodes, 5)

    note("总帧数 %d" % len(FRAMES))
    return _flush()


def _flush():
    with open(os.path.join(D, "run.log"), "w", encoding="utf-8") as f:
        f.write("\n".join(LOG))
    with open(os.path.join(D, "frames.json"), "w", encoding="utf-8") as f:
        json.dump(FRAMES, f, ensure_ascii=False, indent=1)
    print("frames=%d -> %s" % (len(FRAMES), D))


if __name__ == "__main__":
    main()
