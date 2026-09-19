# -*- coding: utf-8 -*-
"""把 Boss 评分函数(`cdScoreBoss`,ChuangdangBossScreen.kt L474~536)原样搬到 Python。

为什么搬:要通过态面板就得先知道"什么草稿能过关"。规则评分是**关键词 + 结构检查**,
确定性很强 —— 在 Python 里跑一遍,就能在人去打字之前把分数算准,避免"填完才发现差一点"。

⚠️ 两边必须**逐条对齐**:这份代码是 Kotlin 的镜像,不是"大概意思"。
   一旦 Kotlin 那边的关键词表变了,这里要同步改(反之亦然)。
"""
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass


def cd_score_boss(raw):
    t = raw.strip()
    n = len(t)

    def has(*keys):
        return any(k in t for k in keys)

    boundary = has("不能保证", "不确定", "会出错", "可能出错", "能力边界", "边界", "局限", "做不到")
    human = has("人工确认", "交给人", "由人", "人来判断", "人工", "确认", "复核", "人工复核")
    knowledge = 30 if (boundary and human) else 18 if (boundary or human) else 8 if n > 0 else 0

    points = [
        has("能做", "可以识别", "能识别", "能做什么", "能认") and has("不能", "保证"),
        has("感知") and has("判断", "推理") and has("行动"),
        has("样本", "数据", "样例", "例子"),
        has("不确定", "看不清", "没学过", "陌生", "模糊", "拿不准") and human,
        has("失败", "出错") and has("验证", "测试", "修正", "改进", "办法"),
    ]
    completeness = sum(1 for x in points if x) * 5

    hits = [w for w in ("因为", "所以", "我选择", "考虑到", "取舍", "优先", "原因", "权衡", "设计成", "决定")
            if w in t]
    explanation = min(len(hits) * 5, 20)

    failure = has("失败", "出错", "错误的情况", "错误情形", "误判")
    fix = has("验证", "测试", "修正", "改进", "调整", "迭代")
    testfix = (8 if failure else 0) + (7 if fix else 0)

    clarity = min((5 if n >= 120 else 3 if n >= 60 else 0)
                  + (3 if n >= 260 else 0)
                  + (2 if (t.count("\n") >= 3 or has("1.", "一、", "①", "第一")) else 0), 10)

    total = knowledge + completeness + explanation + testfix + clarity
    passed = total >= 70 and knowledge >= 18 and explanation >= 12 and boundary and human
    return dict(len=n, knowledge=knowledge, completeness=completeness, explanation=explanation,
                testfix=testfix, clarity=clarity, total=total, passed=passed,
                hits=hits, points=points, boundary=boundary, human=human)


DRAFT = """1. 它能做什么:能识别水壶、书本等常见物品并给出名称。
2. 它不能保证什么:照片模糊、物品被遮挡或不在样本范围内时会出错,不能保证每次都正确 —— 这是它的能力边界。
3. 分工:感知负责从照片提取形状与颜色,判断负责与样本比对后给出结论,行动负责输出名称或提示。
4. 样本:每个类别准备 20 张有代表性的样本,覆盖不同角度、光照和模糊程度。
5. 不确定时怎么办:看不清或没学过时不猜答案,直接提示「拿不准」并交给人确认,由人工复核后决定。
6. 可能失败与验证:我选择先小范围测试,因为失败最常出现在阴天逆光,所以给出修正办法 —— 收集出错例子补样本、重新调整阈值。"""

if __name__ == "__main__":
    texts = {"本次要填的草稿": DRAFT,
             "上次那次的乱码草稿": "啧啧啧——ABC——"}
    for name, txt in texts.items():
        r = cd_score_boss(txt)
        print("=" * 76)
        print("【%s】长度 %d 字符" % (name, r["len"]))
        print("  知识 %2d/30 · 任务 %2d/25 · 个人解释 %2d/20 · 测试修正 %2d/15 · 表达 %2d/10  =  %d/100"
              % (r["knowledge"], r["completeness"], r["explanation"], r["testfix"], r["clarity"], r["total"]))
        print("  五项要点命中: %s" % r["points"])
        print("  因果词命中: %s" % r["hits"])
        print("  必要条件: 能力边界=%s 人工确认=%s" % (r["boundary"], r["human"]))
        print("  → %s" % ("✅ 通过(会走「机枢恢复清明」那条分支)" if r["passed"] else "❌ 未通过"))
