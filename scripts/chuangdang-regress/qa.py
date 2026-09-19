# -*- coding: utf-8 -*-
"""从 ChuangdangData.kt 解析三种题型的题目与正确答案(供自动游玩使用)。

2026-09-19 §15:引入排序 / 分类后,答案不再只是"第几个选项"。
统一返回 {题干: (类型, 载荷)},载荷含义:
  choice → (选项列表, 正确下标)
  order  → 正确顺序的步骤列表
  sort   → (类别名列表, [(条目, 正确类别下标), …])

关键教训(2026-09-19 §9):答案一律从源码解析,**不手打** ——
手打会因为错一个字而匹配失败,并让脚本在"找不到正确项"处静默卡住。
"""
import os
import re

# 题库源文件:从本脚本位置往上找到仓库根(scripts/chuangdang-regress/ → 仓库根),
# 不写死绝对路径 —— 换机器 / 换克隆目录都不用改。
_REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(_REPO, "android", "app", "src", "main", "java", "com", "jueqiao",
                   "jianghu", "ui", "screens", "chuangdang", "ChuangdangData.kt")

_STR = r'"((?:[^"\\]|\\.)*)"'


def _prompt(chunk):
    m = re.search(r'prompt = ' + _STR, chunk)
    return m.group(1) if m else None


def load():
    text = open(SRC, encoding="utf-8").read()
    qs = {}

    # 点选:data class 的声明也含 "CdChoice(",解析不到 prompt 会被跳过
    for chunk in text.split("CdChoice(")[1:]:
        p = _prompt(chunk)
        om = re.search(r"options = listOf\((.*?)\n\s*\)", chunk, re.S)
        ci = re.search(r"correctIndex = (\d+)", chunk)
        if p and om and ci:
            opts = re.findall(_STR, om.group(1))
            qs[p] = ("choice", (opts, int(ci.group(1))))

    # 排序:steps 存的是**正确顺序**
    for chunk in text.split("CdOrder(")[1:]:
        p = _prompt(chunk)
        sm = re.search(r"steps = listOf\((.*?)\n\s*\)", chunk, re.S)
        if p and sm:
            qs[p] = ("order", re.findall(_STR, sm.group(1)))

    # 分类:buckets + items(条目, 正确类别下标)
    for chunk in text.split("CdSort(")[1:]:
        p = _prompt(chunk)
        bm = re.search(r"buckets = listOf\((.*?)\n\s*\)", chunk, re.S)
        im = re.search(r"items = listOf\((.*?)\n\s*\)", chunk, re.S)
        if p and bm and im:
            buckets = re.findall(_STR, bm.group(1))
            items = re.findall(r'CdSortItem\(' + _STR + r",\s*(\d+)\)", im.group(1))
            qs[p] = ("sort", (buckets, [(t, int(b)) for t, b in items]))

    return qs


BY_PROMPT = load()


def kind_of(prompt):
    v = BY_PROMPT.get(prompt)
    return v[0] if v else None


if __name__ == "__main__":
    import collections
    c = collections.Counter(k for k, _ in BY_PROMPT.values())
    out = ["解析到 %d 道题:%s" % (len(BY_PROMPT), dict(c)), ""]
    for p, (k, payload) in BY_PROMPT.items():
        out.append("[%s] %s" % (k, p))
        if k == "choice":
            opts, i = payload
            for j, o in enumerate(opts):
                out.append("      %s %s" % ("[V]" if j == i else "[ ]", o))
        elif k == "order":
            for j, s in enumerate(payload, 1):
                out.append("      %d. %s" % (j, s))
        else:
            buckets, items = payload
            out.append("      类别: %s" % " / ".join(buckets))
            for t, b in items:
                out.append("      [%s] %s" % (buckets[b], t))
        out.append("")
    out_path = os.path.join(os.environ.get("CD_TEST_DIR")
                            or os.path.join(os.environ["TEMP"], "cd-test"), "questions.txt")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    open(out_path, "w", encoding="utf-8").write("\n".join(out))
    print("questions=%d types=%s" % (len(BY_PROMPT), dict(c)))
