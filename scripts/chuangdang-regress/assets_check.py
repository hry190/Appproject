# -*- coding: utf-8 -*-
"""五关素材 · 离线体检(回归第一层)。

为什么先做离线:屏幕像素只能证明"渲染成什么样",证明不了"部署的那张 PNG 干不干净"。
水印 / 白底 / 缺 alpha 都是**源文件属性** —— 在真机上量它既贵又间接。
所以两层分开:
  · assets_check.py —— 源文件属性(本文件)
  · verify_render.py —— 真机上"渲染出来的就是这张源文件"

判据来源:2026-09-19 §20/§21/§22 的实测值(裁水印后的尺寸 / 主体包围盒),
写成期望表 —— 一旦有人把带水印的原图放回来,这里第一个报警。
"""
import os
import sys

import numpy as np
from PIL import Image
from scipy import ndimage

_REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RES = os.path.join(_REPO, "android", "app", "src", "main", "res", "drawable-nodpi")
# 报告写在产物目录(默认 %TEMP%\cd-test),**不落仓库**
OUTDIR = os.environ.get("CD_TEST_DIR") or os.path.join(os.environ["TEMP"], "cd-test")

# 关 → (字形, 文件名, 期望尺寸, 期望主体 bbox 宽高, 是否必须已有 alpha)
EXPECT = [
    ("第1关 铜齿门卫", "盾", "img_chuangdang_tongchimenwei.png", (1254, 1254), None, True),
    ("第2关 断目机关蝠", "蝠", "img_chuangdang_duanmujiguanfu.png", (1536, 1024), None, True),
    ("第3关 棋冠石将", "棋", "img_chuangdang_qiguanshixiang.png", (1346, 1739), (1126, 1519), True),
    ("第4关 百声纸鹤", "鹤", "img_chuangdang_baishengzhihe.png", (1267, 1077), (1048, 858), True),
    ("终局 百面机枢", "枢", "img_chuangdang_baimianjishu.png", (1219, 1295), (999, 1075), True),
]

OUT = []


def p(s=""):
    OUT.append(s)
    print(s.encode("ascii", "replace").decode("ascii"))


def analyze(path):
    im = Image.open(path)
    mode = im.mode
    a = np.array(im.convert("RGBA"))
    alpha = a[:, :, 3]

    # 前景掩膜:有 alpha 的用 alpha>0;没有 alpha 的(RGB)用"非近白"
    if mode in ("RGBA", "LA", "P") and im.convert("RGBA").getextrema()[3][1] > 0:
        fg = alpha > 8
        has_alpha = True
    else:
        rgb = a[:, :, :3].astype(np.int16)
        fg = rgb.min(axis=2) < 248          # 非近白
        has_alpha = False

    lab, n = ndimage.label(fg)
    if n == 0:
        return dict(mode=mode, size=im.size, has_alpha=has_alpha, n=0)
    sizes = ndimage.sum(fg, lab, range(1, n + 1))
    main = int(np.argmax(sizes)) + 1
    sl = ndimage.find_objects(lab)
    body = sl[main - 1]
    bx = (body[1].start, body[0].start, body[1].stop, body[0].stop)
    body_area = sizes[main - 1]

    # 水印判据(§21):落在**主体包围盒之外**的碎片。
    #   主体之外的碎片数 = 连通域总数 - 1(再排除贴边噪声面积太小的)
    frag = [i for i in range(1, n + 1) if i != main and sizes[i - 1] >= 8]
    outside = []
    for i in frag:
        s = sl[i - 1]
        fx1, fy1, fx2, fy2 = s[1].start, s[0].start, s[1].stop, s[0].stop
        if fx2 <= bx[0] or fx1 >= bx[2] or fy2 <= bx[1] or fy1 >= bx[3]:
            outside.append((fx1, fy1, fx2, fy2, int(sizes[i - 1])))

    opaque_share = float(fg.mean())
    alpha_zero = float((alpha == 0).mean()) if has_alpha else None
    return dict(mode=mode, size=im.size, has_alpha=has_alpha, n=n,
                body=(bx[2] - bx[0], bx[3] - bx[1]), body_box=bx,
                body_share=float(body_area / fg.size), opaque_share=opaque_share,
                alpha_zero=alpha_zero, fragments=len(frag), outside=outside)


def main():
    fails = []
    p("=" * 100)
    p("五关素材 · 离线体检(源文件属性)")
    p("=" * 100)
    for label, glyph, fn, exp_size, exp_body, need_alpha in EXPECT:
        path = os.path.join(RES, fn)
        r = analyze(path)
        feats = []
        if r["size"] != exp_size:
            feats.append("尺寸 %s != 期望 %s" % (r["size"], exp_size))
        if need_alpha and not r["has_alpha"]:
            feats.append("缺 alpha 通道")
        if r["has_alpha"] and r["alpha_zero"] is not None and r["alpha_zero"] < 0.02:
            feats.append("alpha 全不透明(等于没抠底)")
        if any(o[4] >= 40 for o in r["outside"]):
            feats.append("主体之外有 %d 处碎片(水印嫌疑)" % len([o for o in r["outside"] if o[4] >= 40]))
        note_body = ""
        if exp_body and r.get("body"):
            # ⚠️ 容差 ±6px:期望值来自 §20/§21 的实测(alpha>0 口径),本脚本用 alpha>8,
            #    边缘抗锯齿像素的归属差 1~2px 属**口径差异**,不是素材变了。
            note_body = "(主体与 §20/§21 记录相差 %+d/%+dpx,在 ±6px 口径容差内)" % (
                r["body"][0] - exp_body[0], r["body"][1] - exp_body[1])
            if (abs(r["body"][0] - exp_body[0]) > 6) or (abs(r["body"][1] - exp_body[1]) > 6):
                feats.append("主体 %s 与期望 %s 相差超过 6px" % (r.get("body"), exp_body))

        p("")
        p("【%s】%s  (%s)" % (label, glyph, fn))
        p("  模式=%-5s 尺寸=%dx%d  连通域=%d" % (r["mode"], r["size"][0], r["size"][1], r["n"]))
        p("  有 alpha=%s  全透明占比=%s  前景占比=%.1f%%"
          % (r["has_alpha"],
             ("%.1f%%" % (r["alpha_zero"] * 100)) if r["alpha_zero"] is not None else "-",
             r["opaque_share"] * 100))
        if r.get("body"):
            p("  主体 bbox = x[%d,%d] y[%d,%d]  →  %dx%d"
              % (r["body_box"][0], r["body_box"][2], r["body_box"][1], r["body_box"][3],
                 r["body"][0], r["body"][1]))
        p("  主体之外的碎片 = %d %s" % (r["fragments"], r["outside"] if r["outside"] else ""))
        if note_body:
            p("  " + note_body)
        p("  → %s" % ("✅ 通过" if not feats else "❌ " + "; ".join(feats)))
        if feats:
            fails.append((label, feats))

    p("")
    p("=" * 100)
    p("结论:%d/%d 通过" % (len(EXPECT) - len(fails), len(EXPECT)))
    for label, feats in fails:
        p("  ❌ %s: %s" % (label, "; ".join(feats)))
    os.makedirs(OUTDIR, exist_ok=True)
    path = os.path.join(OUTDIR, "assets_check.txt")
    open(path, "w", encoding="utf-8").write("\n".join(OUT))
    print("-> " + path)


if __name__ == "__main__":
    sys.exit(main())
