# -*- coding: utf-8 -*-
"""像素级"文字是否贴边/被裁"检测 v2 —— 先定位卡片矩形,再量墨迹到卡片边的留白。

为什么换掉 v1(沿亮度突变外扫):
  v1 从「文本节点框外」起扫。可**真实裁剪**恰恰发生在节点框之外 ——
  文字排版框比卡片高时,框的上边已经在卡片外面,那里根本扫不到卡片边。
  实测:人工造出"文字顶到卡片上边"的图,v1 仍报 ok —— 它测不出它要测的东西。正对照失败。

v2 的判据(实测得来,不是猜的):
  · 卡片底色 BCardBg = Color(0xF2FFFFFF),95% 不透明白,叠在背景上后
    渲染为**恒定的中性近白** —— 实测卡片内部 rgb=(254,254,254),描边 rgb=(212,211,210),
    而页面背景是偏色水彩(rgb 如 234,235,227)。
  · 故 card_mask = 中性且 ≥250 的像素;连通域即一张卡片,其 bbox 就是卡片矩形。
  · 留白 = 墨迹包围盒到卡片 bbox 的距离。真实裁剪时墨迹从卡片边开始 ⇒ 留白 0。
"""
import os
import re
import sys

import numpy as np
from PIL import Image
from scipy import ndimage

DARK = 150        # 墨迹阈值
MIN_GAP = 6       # 留白小于此值报警
MIN_AREA = 20000  # 卡片连通域最小面积


def parse_nodes(xml_path):
    xml = open(xml_path, encoding="utf-8").read()
    out = []
    for m in re.finditer(r"<node[^>]*>", xml):
        tag = m.group(0)
        t = re.search(r'text="([^"]*)"', tag)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
        if not (t and b) or not t.group(1).strip():
            continue
        x1, y1, x2, y2 = (int(g) for g in b.groups())
        out.append((t.group(1), x1, y1, x2, y2))
    return out


def card_boxes(rgb):
    """卡片连通域 → 卡片矩形列表。

    卡片底色是 BCardBg = Color(0xF2FFFFFF)(alpha=242/255≈0.949)叠在背景之上,
    所以渲染值 = 242 + 0.051 × 背景值,且**通道差被压到 1/20**:
        bg(234,235,227) → (254,254,254)   bg(150,155,140) → (250,250,249)
    ⚠️ 踩过的坑:最初判据写成 `r >= 250`,结果卡片上渲染成 249 的地方被挖掉,
       一张卡片被切成两半 —— 于是"卡片宽 800"而实际是 974,右留白被算成 23px(真实 197px)。
       教训:阈值必须覆盖渲染值的**整个可能区间**,不能按某个样本的观测值定。
    现在的判据:够亮(min ≥ 240)且**近乎中性**(通道极差 ≤ 3)。
    水彩背景是偏色的(通道差 5~20),卡片的 1dp 描边是 (212,211,210) —— 两者都会被排除,
    所以各卡片互不连通、也不会和背景粘连。
    """
    r, g, b = (rgb[:, :, i].astype(np.int16) for i in range(3))
    mx = np.maximum(np.maximum(r, g), b)
    mn = np.minimum(np.minimum(r, g), b)
    mask = (mn >= 240) & ((mx - mn) <= 3)
    lab, n = ndimage.label(mask)
    boxes = []
    sl = ndimage.find_objects(lab)
    for i, s in enumerate(sl, 1):
        if s is None:
            continue
        h = s[0].stop - s[0].start
        w = s[1].stop - s[1].start
        if h * w < MIN_AREA:
            continue
        boxes.append((s[1].start, s[0].start, s[1].stop, s[0].stop))
    return boxes


def measure(png, xml_path):
    rgb = np.array(Image.open(png).convert("RGB"))
    gray = np.array(Image.open(png).convert("L")).astype(np.int16)
    H, W = gray.shape
    boxes = card_boxes(rgb)
    rows = []
    for text, x1, y1, x2, y2 in parse_nodes(xml_path):
        bx1, by1 = max(0, x1), max(0, y1)
        bx2, by2 = min(W, x2), min(H, y2)
        box = gray[by1:by2, bx1:bx2]
        if box.size == 0:
            continue
        m = box < DARK
        if not m.any():
            continue
        ys, xs = np.where(m)
        at, ab = by1 + int(ys.min()), by1 + int(ys.max())
        al, ar = bx1 + int(xs.min()), bx1 + int(xs.max())
        cx, cy = (al + ar) // 2, (at + ab) // 2

        card = None
        for (cx1, cy1, cx2, cy2) in boxes:
            if cx1 <= cx <= cx2 and cy1 <= cy <= cy2:
                card = (cx1, cy1, cx2, cy2)
                break
        if card is None:
            rows.append((text, (x1, y1, x2, y2), None, None, "不在卡片内"))
            continue

        # 护栏 1:卡片被滚动视口截断 —— 可见部分的上/下边就是屏幕边,
        #   文字"贴着"它是滚动所致,不是裁切。实测:结算后向下滚动,制作任务卡
        #   只剩 134px 露在屏幕顶端,于是"评分标准"那行被算成上留白 0。
        if card[1] <= 1 or card[3] >= H - 1:
            rows.append((text, (x1, y1, x2, y2), card, None, "卡片被视口截断"))
            continue

        # 护栏 2:容器节点 —— bounds 与卡片几乎重合的节点不是"一行文字",
        #   而是把文字当属性的容器(如多行输入框)。对它量"墨迹到卡片边的留白"没有意义。
        if (abs(x1 - card[0]) <= 6 and abs(x2 - card[2]) <= 6
                and abs(y1 - card[1]) <= 6 and abs(y2 - card[3]) <= 6):
            rows.append((text, (x1, y1, x2, y2), card, None, "容器节点"))
            continue

        # 分类 3:被**滚动视口**截断 —— 版面框高度远小于同行文本的正常行高
        #   (正常单行节点框 ≈ 44~52px)。实测:结算后向下滚,「评分标准」那行的
        #   节点框只剩 20px、墨迹只剩 13px,上留白 0。
        #   这是滚动内容的正常表现,不是卡片把文字裁了 —— 故**单独归类**,不当成缺陷。
        if (y2 - y1) < 30:
            rows.append((text, (x1, y1, x2, y2), card, None,
                         "视口截断(滚动),节点框仅 %dpx" % (y2 - y1)))
            continue

        g = dict(top=at - card[1], bot=card[3] - ab,
                 left=al - card[0], right=card[2] - ar)
        rows.append((text, (x1, y1, x2, y2), card, g, ""))
    return rows


def run(shots_dir, out_path):
    names = sorted(f[:-4] for f in os.listdir(shots_dir) if f.endswith(".png"))
    L, tot, bad, skip, flagged = [], 0, 0, 0, []
    for name in names:
        xml = os.path.join(shots_dir, name + ".xml")
        if not os.path.exists(xml):
            continue
        rows = measure(os.path.join(shots_dir, name + ".png"), xml)
        L.append("\n### %s  (文本 %d)" % (name, len(rows)))
        L.append("%-46s %5s %5s %5s %5s  %-16s %s" % ("文字", "上", "下", "左", "右", "卡片", "判定"))
        L.append("-" * 112)
        for text, box, card, g, why in rows:
            if g is None:
                skip += 1
                L.append("%-46s %5s %5s %5s %5s  %-16s %s" % (
                    text[:44], "-", "-", "-", "-",
                    ("%dx%d" % (card[2] - card[0], card[3] - card[1])) if card else "-",
                    "跳过:" + why))
                continue
            vals = [g["top"], g["bot"], g["left"], g["right"]]
            worst = min(vals)
            tot += 1
            v = "ok"
            if worst < MIN_GAP:
                v = "!! 贴边"
                bad += 1
                flagged.append((name, text, worst))
            L.append("%-46s %5d %5d %5d %5d  %-16s %s" % (
                text[:44], *vals, "%dx%d" % (card[2] - card[0], card[3] - card[1]), v))
    L.append("\n" + "=" * 112)
    L.append("可测量文本 %d 行,留白 < %dpx 的 %d 行;不适用(跳过) %d 行" % (tot, MIN_GAP, bad, skip))
    for nm, t, w in flagged:
        L.append("  !! %s 「%s」worst=%dpx" % (nm, t[:40], w))
    open(out_path, "w", encoding="utf-8").write("\n".join(L))
    print("measurable=%d bad=%d skipped=%d -> %s" % (tot, bad, skip, out_path))


if __name__ == "__main__":
    run(sys.argv[1], sys.argv[2])
