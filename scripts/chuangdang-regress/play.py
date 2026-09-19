# -*- coding: utf-8 -*-
"""闯荡江湖自动游玩:v0 一路打到 Boss,并在每个界面留截图。

设计要点(来自 2026-09-19 的教训):
  · 答案**从源码解析**(qa.py),不手打 —— 手打错一个字就会让脚本静默卡住。
  · 题干从 dump 里认,再按题干反查答案;选项顺序是打散的,只能按**文字**点。
  · 三种题型(§15 起)各有作答方式:
      choice —— 找到正确选项文字,点它(一次)
      order  —— 按正确顺序依次点;点过的行会被加上 "N. " 前缀,据此判断还剩哪步没点
      sort   —— 逐条把类别按钮点上(每条在**它自己那一行**里找类别按钮,靠 y 相邻判定),
                全归完后点「确认归类」
  · 结算面板**仍然显示题干**,所以必须先判「继续/返回地图」再判题干。
  · 地图页有两条路径:未出发时"选关 → 点出发";出发中点当前关直接进战斗。
  · 关前剧情浮层是模态,会遮住整页,必须优先关掉。
  · 日志写 UTF-8 文件,不看 GBK 终端。
用法: python play.py <最多步数>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from drive import OUT, dump, find, shot, tap
from qa import BY_PROMPT

LOG = os.path.join(OUT, "play.log")
_log = []


def log(msg):
    _log.append(msg)
    print(msg.encode("ascii", "replace").decode("ascii"))


def flush():
    with open(LOG, "w", encoding="utf-8") as f:
        f.write("\n".join(_log))


def tap_node(node, wait=0.7):
    cx, cy = (node[2] + node[4]) // 2, (node[3] + node[5]) // 2
    log("    tap 「%s」@(%d,%d)" % (node[0][:34], cx, cy))
    tap(cx, cy, wait)


def find_prompt(nodes):
    for n in nodes:
        if n[0] in BY_PROMPT:
            return n[0]
    return None


def stage_of(nodes):
    for n in nodes:
        if re.match(r"^(第 \d 关|终局)", n[0]) and n[3] < 400:
            return n[0]
    return "?"


# 分类题:题干 → 已归类的条目数(循环本身无状态,进度存在这里)
SORT_DONE = {}


def main(max_steps=500):
    qno = 0
    shotno = 0
    last_key = None
    stall = 0
    for step in range(max_steps):
        nodes = dump()
        stage = stage_of(nodes)

        # ⓪ 关前剧情浮层(§14 新增):模态遮住整页,必须先关掉
        intro = find(nodes, r"^(进入战斗|开始练习)$")
        if intro:
            log("[%02d] 关前剧情浮层 -> 点「%s」" % (step, intro[0][0]))
            tap_node(intro[0], wait=1.2)
            continue

        # ① 结算 / 胜利 / 战败面板(必须先判 —— 它们也带着题干)
        back = find(nodes, r"^返回地图$")
        if back:
            shotno += 1
            shot("s%02d-panel-%s" % (shotno, stage.replace(" ", "").replace("·", "")))
            log("[%02d] %s 结局面板 -> 截图 s%02d" % (step, stage, shotno))
            tap_node(back[0], wait=1.2)
            continue

        cont = find(nodes, r"^(继续|迎击.*)$")
        if cont:
            shotno += 1
            shot("s%02d-resolve-%s" % (shotno, stage.replace(" ", "").replace("·", "")))
            log("[%02d] %s 结算文案 -> 「%s」(截图 s%02d)" % (step, stage, cont[0][0], shotno))
            tap_node(cont[0], wait=0.8)
            continue

        # ② 地图页
        if find(nodes, r"^通关印记"):
            target = None
            for h in find(nodes, r"^(第 \d 关|终局)"):
                for n in nodes:
                    if n[0] in ("可挑战", "继续") and abs(n[3] - h[3]) < 60:
                        target = h
            if not target:
                log("[%02d] 地图页:没有「可挑战/继续」的关" % step)
                for n in nodes:
                    if n[0]:
                        log("      %s %s" % (n[0][:40], n[3]))
                flush()
                return
            log("[%02d] 地图页 -> 选「%s」" % (step, target[0]))
            tap_node(target, wait=1.2)
            nodes2 = dump()
            if find_prompt(nodes2):
                log("[%02d]   -> 直接进入战斗" % step)
                continue
            if find(nodes2, r"^(进入战斗|开始练习)$"):
                log("[%02d]   -> 进入关卡(关前剧情浮层,下一轮关掉)" % step)
                continue
            s2 = find(nodes2, r"出发$|继续挑战")
            if s2:
                log("[%02d]   -> 点「%s」进入战斗" % (step, s2[0][0]))
                tap_node(s2[0], wait=1.5)
                continue
            log("[%02d] 地图页:选了关但进不去。当前文本:" % step)
            for n in nodes2:
                if n[0]:
                    log("      %s" % n[0][:60])
            flush()
            return

        # ②-b 首页:点第五个主入口进地图(冷启动/装机后需要)
        home = find(nodes, r"^闯荡江湖$")
        if home:
            log("[%02d] 首页 -> 点「闯荡江湖」" % step)
            tap_node(home[0], wait=2.0)
            continue
        enter = find(nodes, r"^点击进入$")
        if enter:
            log("[%02d] 入口页 -> 点「点击进入」" % step)
            tap_node(enter[0], wait=5.0)
            continue

        # ③ 战斗中:按题型作答
        prompt = find_prompt(nodes)
        if prompt:
            kind, payload = BY_PROMPT[prompt]
            if prompt != last_key:
                qno += 1
                shotno += 1
                last_key = prompt
                name = "q%03d-%s-%s" % (qno, kind, stage.replace(" ", "").replace("·", ""))
                shot(name)
                # 2026-09-19 §15:顺带存一份**同时刻的 dump**,供 clip2.py 做裁切回归
                #   (drive.dump_raw 每次都把 xml 写到 OUT/u.xml,本轮刚拉过)
                try:
                    import shutil
                    shutil.copyfile(os.path.join(OUT, "u.xml"),
                                    os.path.join(OUT, "shots", name + ".xml"))
                except Exception:
                    pass
                log("[%02d] Q%d %s [%s] 题干=%s" % (step, qno, stage, kind, prompt))
            tap_node(do_answer(kind, payload, prompt, nodes, step), wait=0.85)
            stall = 0
            continue

        stall += 1
        if stall > 3:
            log("[%02d] 认不出的界面,停止。文本:" % step)
            for n in nodes:
                if n[0]:
                    log("      %s" % n[0][:60])
            flush()
            return

    flush()


def do_answer(kind, payload, prompt, nodes, step):
    """按题型挑出本轮要点的那一个节点。"""
    if kind == "choice":
        opts, ci = payload
        ans = opts[ci]
        shown = [n for n in nodes if n[0] in opts]
        log("      正确项=「%s」 屏上=%d 项" % (ans, len(shown)))
        tgt = next((n for n in shown if n[0] == ans), None)
        if tgt is None:
            raise SystemExit("[%02d] !! 屏上找不到正确项: %s" % (step, [n[0] for n in shown]))
        return tgt

    if kind == "order":
        raw = set(n[0] for n in nodes)
        nxt = next((s for s in payload if s in raw), None)
        if nxt is None:
            raise SystemExit("[%02d] !! 排序题:正确步骤都不在屏上了 %s" % (step, payload))
        log("      下一个应点: 「%s」" % nxt[:30])
        return next(n for n in nodes if n[0] == nxt)

    if kind == "sort":
        buckets, items = payload
        done = SORT_DONE.get(prompt, 0)
        if done < len(items):
            text_i, b = items[done]
            row = next((n for n in nodes if n[0] == text_i), None)
            if row is None:
                raise SystemExit("[%02d] !! 分类题:找不到条目「%s」" % (step, text_i[:30]))
            # 该条目**自己那一行**里的类别按钮:位置在条目文字下方、且垂直距离很近
            chip = None
            for n in nodes:
                if n[0] != buckets[b]:
                    continue
                if n[3] >= row[5] and (n[3] - row[5]) < 120:
                    chip = n
                    break
            if chip is None:
                raise SystemExit("[%02d] !! 分类题:找不到类别按钮「%s」" % (step, buckets[b]))
            SORT_DONE[prompt] = done + 1
            log("      第 %d/%d 条归入「%s」" % (done + 1, len(items), buckets[b]))
            return chip
        btn = find(nodes, r"^确认归类$")
        if not btn:
            raise SystemExit("[%02d] !! 分类题:归完了但找不到「确认归类」" % step)
        log("      全部归完 -> 确认")
        SORT_DONE.pop(prompt, None)
        return btn[0]

    raise SystemExit("未知题型 %s" % kind)


if __name__ == "__main__":
    main(int(sys.argv[1]) if len(sys.argv) > 1 else 500)
    flush()
    print("log -> " + LOG)
