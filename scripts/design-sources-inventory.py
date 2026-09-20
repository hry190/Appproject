# -*- coding: utf-8 -*-
"""导出「设计稿来源」清单 —— 把代码注释里对设计稿的引用整理成一份进仓库的文档。

为什么需要:`D:\\图\\` 这个目录**可能被删除**。删掉之后,
  · 注释里的**文件名**还能读,但没人能再确认它是哪张图;
  · 目录里**代码没引用过**的那些文件,连名字都不会留在任何地方。
所以清单必须在删之前导一次。

用法:
    python scripts/design-sources-inventory.py            # 打印统计,不写文件
    python scripts/design-sources-inventory.py --write    # 写 docs/DESIGN-SOURCES.md
"""
import hashlib
import os
import re
import sys
from datetime import datetime

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(REPO, "android", "app", "src", "main", "java", "com", "jueqiao", "jianghu")
DESIGN_DIR = r"D:\图"
OUT = os.path.join(REPO, "docs", "DESIGN-SOURCES.md")

# 设计稿名里可能带空格("image 64.png"),故优先按扩展名收尾。
# ⚠️ 两种写法都要认:`D:\图\<名>`(2026-09-20 迁移前)与 `设计稿 <名>`(迁移后)——
#    否则迁移一做完,本脚本就再也扫不出东西了。
NAME_EXT = re.compile(r"(?:D:\\图\\|设计稿\s*)([^\\\r\n]*?\.(?:png|jpg|jpeg|webp|gif))", re.IGNORECASE)
# ⚠️ 兜底只给老写法用:迁移后注释里出现「设计稿」这两个字的地方不止文件名
#    (还有"设计稿源""设计稿尺寸"之类的散文),不加扩展名限制会把它们误当成素材名
#    —— 实测:放开后"名字"从 295 涨到 306,多出来的 11 个在磁盘上根本不存在。
NAME_FALLBACK = re.compile(r"D:\\图\\([^\\\r\n()（）,;、]{2,40})")
RES = re.compile(r"\bimg_[a-z0-9_]+")

def _plausible(name):
    """把散文误判挡掉:`设计稿 412×917 基准,参考 演武场视频评论.png` 这种不是素材名。

    判据:不含中文标点(说明它是句子的一部分),且长度不超过 40。
    """
    return len(name) <= 40 and not any(ch in name for ch in ",、。;:：")


records = []          # (设计稿名, 文件, 行号, [资源名...], 该行原文)
for dirpath, _dirs, files in os.walk(SRC):
    for fn in sorted(files):
        if not fn.endswith(".kt"):
            continue
        p = os.path.join(dirpath, fn)
        for i, line in enumerate(open(p, encoding="utf-8").read().splitlines(), 1):
            names = [n for n in NAME_EXT.findall(line) if _plausible(n.strip())]
            if not names:
                names = [m.strip() for m in NAME_FALLBACK.findall(line)]
            for name in names:
                records.append((name.strip(), fn, i, RES.findall(line), line.strip()))

# 目录里真实存在的文件(名 → 体积)
on_disk = {}
disk_hash = {}
if os.path.isdir(DESIGN_DIR):
    for f in os.listdir(DESIGN_DIR):
        fp = os.path.join(DESIGN_DIR, f)
        if os.path.isfile(fp):
            on_disk[f] = os.path.getsize(fp)

referenced = {}
for name, fn, line_no, res, text in records:
    referenced.setdefault(name, []).append((fn, line_no, res, text))

# 别名:注释里的写法可能不带扩展名或带 " (已复制为…" 之类尾巴 —— 尽量对到磁盘上的真实文件名
def match_disk(name):
    if name in on_disk:
        return name
    stem = os.path.splitext(name)[0]
    for f in on_disk:
        if os.path.splitext(f)[0] == stem:
            return f
    return None

matched = {match_disk(n) or n: v for n, v in referenced.items()}
no_res = [(n, v) for n, v in matched.items()
          if not any(r for _fn, _ln, r, _t in v)]
unreferenced = sorted(set(on_disk) - {n for n in matched if n in on_disk})

# ── 内容比对:注释查不到出处 ≠ 没用过 ────────────────────────────────────────
# 反例(实测):五个敌人素材在 res/ 里叫 img_chuangdang_*,注释里查不到 `铜齿门卫.png` 这种写法,
# 于是它们会被列进"未引用" —— 但内容明明就在仓库里。故按**文件内容**再判一次:
#   · 与仓库内某资源逐字节一致 → 已导入,删设计稿无影响
#   · 找不到任何同内容文件     → 才需要人过一眼(可能是被裁/压过的,也可能真没用过)
def _sha(path):
    h = hashlib.sha256()
    with open(path, "rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


res_by_hash = {}
res_root = os.path.join(REPO, "android", "app", "src", "main", "res")
for dirpath, _dirs, files in os.walk(res_root):
    for fn in files:
        fp = os.path.join(dirpath, fn)
        try:
            res_by_hash.setdefault(_sha(fp), os.path.basename(fp))
        except Exception:
            pass

in_repo, orphan = [], []
for f in unreferenced:
    try:
        h = _sha(os.path.join(DESIGN_DIR, f))
    except Exception:
        orphan.append(f)
        continue
    disk_hash[f] = h
    (in_repo if h in res_by_hash else orphan).append(f)

# 整个目录里"内容已在仓库"的比例(不只未引用的那些)—— 说明这批设计稿大部分已经进过代码
whole_in_repo = 0
for f in on_disk:
    h = disk_hash.get(f) or _sha(os.path.join(DESIGN_DIR, f))
    if h in res_by_hash:
        whole_in_repo += 1

print("注释引用行 %d / 设计稿名(去重) %d" % (len(records), len(referenced)))
print("目录文件 %d 个(%.1f MB);其中内容与仓库内资源逐字节一致的 %d 个"
      % (len(on_disk), sum(on_disk.values()) / 1048576.0, whole_in_repo))
print("对得上磁盘文件名的 %d 个;对不上的 %d 个"
      % (len([n for n in matched if n in on_disk]), len([n for n in matched if n not in on_disk])))
print("同行缺资源名的设计稿 %d 个" % len(no_res))
print("注释查不到出处 %d 个 → 其中内容已在仓库里 %d 个,真正找不到对应 %d 个"
      % (len(unreferenced), len(in_repo), len(orphan)))

if "--write" not in sys.argv:
    print("\n(只统计,未写文件;加 --write 才写 %s)" % OUT)
    sys.exit(0)

lines = []
A = lines.append
A("# 设计稿来源清单(DESIGN-SOURCES)")
A("")
A("> **创建于 %s**。起因:`D:\\图\\`(设计稿源目录,本地、不在 git)**计划可能被删除**,"
  % datetime.now().strftime("%Y-%m-%d"))
A("> 删掉之后注释里的文件名就「无人可证」了,目录里没被引用的文件更是连名字都不剩 ——")
A("> 所以这里把「代码用过哪些设计稿、对应仓库里哪个资源」固化成一份可查的清单。")
A("")
A("## 怎么用 / 怎么维护")
A("")
A("- **查来源**:想知道 `img_xxx.png` 从哪张设计稿来 → 在下表搜资源名,或搜设计稿名。")
A("- **加素材**:导入新图时,顺手在表里补一行(`设计稿名 → img_资源名`);**代码注释里不要再写盘符路径**"
  "(规则见 [CONTRIBUTING.md](../CONTRIBUTING.md) §9)。")
A("- **复现**:`python scripts/design-sources-inventory.py --write`(从代码注释 + 目录实时重算)。")
A("")
A("## 统计(导出时快照)")
A("")
A("| 项 | 值 |")
A("|---|---|")
A("| 导出时间 | %s |" % datetime.now().strftime("%Y-%m-%d %H:%M"))
A("| 代码注释里的引用行 | **%d** 行 |" % len(records))
A("| 设计稿源目录 | `%s` —— **%d 个文件 / %.1f MB**%s |"
  % (DESIGN_DIR, len(on_disk), sum(on_disk.values()) / 1048576.0,
     "" if on_disk else "(导出时该目录已不存在)"))
A("| 被代码引用到的设计稿 | **%d** 个 |" % len(matched))
A("| 其中同行已写项目内资源名 | %d 个 |" % (len(matched) - len(no_res)))
A("| 目录里**内容已在仓库**(逐字节一致)| **%d** / %d 个 |" % (whole_in_repo, len(on_disk)))
A("| 注释里**查不到出处**的目录文件 | **%d** 个 → 其中 %d 个内容已在仓库、**%d 个真正找不到对应**"
  % (len(unreferenced), len(in_repo), len(orphan)))
A("")
A("## ① 代码引用过的设计稿 → 项目内资源")
A("")
A("| 设计稿文件名 | 项目内资源名 | 引用处(最多 3 个)|")
A("|---|---|---|")
for name in sorted(matched):
    v = matched[name]
    res = sorted({r for _fn, _ln, rr, _t in v for r in rr})
    where = " · ".join("%s:%d" % (fn, ln) for fn, ln, _rr, _t in v[:3])
    if len(v) > 3:
        where += " 等 %d 处" % len(v)
    A("| `%s` | %s | %s |" % (name, ("`" + "`, `".join(res) + "`") if res else "**未标注**", where))
A("")
if orphan:
    A("## ②-a 注释查不到出处、**内容也找不到对应** —— 删除前值得过一眼(%d 个)" % len(orphan))
    A("")
    A("> ⚠️ 别把这一节读成「没用的文件」:它只说明**仓库里没有逐字节相同的副本**。")
    A("> 已知的两类正常情况:")
    A(">   · **被处理过的素材** —— 例如三个敌人原图(棋冠石像 / 百声纸鹤 / 百面机枢)在 §20~§22 按用户要求")
    A(">     **裁掉水印**后才入库,内容自然对不上;删目录会失去**未裁的原始版本**;")
    A(">   · 过度导出/压缩过的图,或确实没进过项目。")
    A("")
    for f in sorted(orphan, key=lambda x: -on_disk.get(x, 0)):
        A("- `%s` —— %.2f MB" % (f, on_disk.get(f, 0) / 1048576.0))
    A("")
if in_repo:
    A("## ②-b 注释查不到出处,但**内容已在仓库里**(%d 个,删除无影响)" % len(in_repo))
    A("")
    A("> 这些文件在注释里查不到,但按**内容**比对,仓库 `res/` 里有一份逐字节相同的副本 —— 可以放心删。")
    A("> (反例提醒:五个敌人素材在 `res/` 里叫 `img_chuangdang_*`,注释里查不到 `铜齿门卫.png` 这种写法,")
    A(">  只按注释判断会误报成「未引用」。)")
    A("")
    for i in range(0, len(in_repo), 6):
        A("- " + " · ".join("`%s`" % x for x in sorted(in_repo)[i:i + 6]))
    A("")

# ③ 已删除记录(git 里留档,脚本重建时不会被抹掉)
DELETED_LOG = os.path.join(REPO, "docs", "design-sources-deleted.txt")
if os.path.exists(DELETED_LOG):
    rows = [ln.rstrip("\n").split("\t") for ln in open(DELETED_LOG, encoding="utf-8")
            if ln.strip() and not ln.startswith("文件\t")]
    if rows:
        A("## ③ 已从设计稿目录删除的文件(%d 个)" % len(rows))
        A("")
        A("> 判据:它们的**内容逐字节存在于 `res/`**,且那份副本是 **git 跟踪**的文件 ——")
        A("> 所以删除是**可逆**的(随时能从仓库复制回来),删的只是「设计稿目录里的那一份」。")
        A("> 记录本身留在这里,是为了以后有人问「这张图当初是不是有原图」时能查到。")
        A("> 数据源:[`docs/design-sources-deleted.txt`](./design-sources-deleted.txt)(删除时由脚本追加,一张一行)。")
        A("")
        A("| 设计稿文件名 | 体积 | 仓库内副本 |")
        A("|---|---|---|")
        for r in rows:
            name = r[0]
            size = ("%.2f MB" % (int(r[1]) / 1048576.0)) if len(r) > 1 and r[1].isdigit() else "-"
            peer = r[2] if len(r) > 2 else "-"
            A("| `%s` | %s | `%s` |" % (name, size, peer))
        A("")

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("\n已写入 %s(%d 行)" % (OUT, len(lines)))
