# IMAGE-COORDINATE-VERIFICATION — 2026-09-11

> 创建/修改 Vol-2/Vol-3 系列页面时,**怎么决定图片 offset/size**(坐标)的标准方法。背景:本项目 9+ 屏是 copy-paste 出来的(见 `CODE-AUDIT-2026-09-11.md` H6),KDoc/行内注释与代码长期漂移,图片坐标经常要么越界要么严重畸变。这份文档沉淀 2026-09-11 总结出的 3 件套:PNG 像素校验 + AskUserQuestion 二步决策 + KDoc 真机调整留痕。

---

## 为什么需要这份文档

用户每次创建新屏的指令形如:
> "创建'第三卷-X'页面,点击'第三卷-X-1'标题时可跳转,复制'第一卷-N'页面的背景和标题和书框这些素材到'第三卷-X'页面,图1 image 320.png X28Y155W352H203,标题文本改成'...'"

表面看指令完整,**实际里有 3 个独立陷阱**:
1. "复制 X 页素材" ≠ "照抄 X 页数值"(素材可复制,布局数值要按本屏语境重定)
2. 用户给的坐标**本身就可能是漂移值**(历史 KDoc 模板错或设计稿未同步)
3. 不验证就写 → 图片被 `ContentScale.FillBounds` 无条件拉伸,要么越界要么畸变

每次都要分别处理。**不校验就动手 = 把 bug 复制到新屏**。

---

## 核心方法:3 件套

### 1. PNG 真实像素校验(决定性证据)

读 PNG 头部 16-24 字节拿原图宽高,算原图宽高比,和拟渲染尺寸的 W/H 比对比:

```bash
python -c "
import struct
f=open(r'D:/图/image 320.png','rb').read(33)
w,h=struct.unpack('>II', f[16:24])
print(f'{w}x{h}  ratio={w/h:.3f}')
"
```

判据:
- 渲染比 / 原图比 < 1.05 → 几乎完美(可接受)
- 1.05 ~ 1.40 → 可接受(有轻微畸变)
- > 1.40 → 严重畸变,通常用户给的 H 值过小(图被横向拉宽)

**为什么廉价**:不需要 Android Studio / 不需要构建,Python 一行就能在 2 秒内回答"用户给的尺寸和原图差多远"。

**关键例子**(2026-09-11 实操):
- image 320 实测 1.137,用户给 `W=352 H=203` 渲染比 1.734 → **严重拉宽 35%**,需修订
- image 339 实测 0.877(近正方),用户给 `W=356 H=300` 渲染比 1.187 → **拉宽 35%**,需修订
- image 380 实测 0.809(纵向矩形),用户给 `W=356 H=320` 渲染比 1.113 → **拉宽 38%**,需修订
- image 333 实测 0.965,用户给 `W=355 H=369` 渲染比 0.962 → **几乎完美**,直接采用

### 2. AskUserQuestion 二步决策套路

PNG 校验**发现问题时**,不要默改、不要猜,要把数据+选项摆给用户。2026-09-11 反复用的 3 问模板:

| 问 | 问什么 | 选项范例(基于实测数据) |
|---|---|---|
| **Q1:图坐标** | 渲染畸变 < 阈值? | (1) 套原图比例 / (2) 限高保安全 / (3) 完全按字面值 |
| **Q2:书框素材** | "复制第一卷-1" 还是 "复制第一卷-2"? | (1) Group 255 / (2) Group 256 + 是否与交替一致 |
| **Q3:标题宽度** | 7 字 vs 5 字文本,W 多大? | W=192(7-8 字)/ W=213(5 字) |

**关键原则**:每个选项都带**实测数据**(原图比例 + 渲染比 + 是否越界),让用户基于事实决策,不靠感觉。

**反例**(禁止):
- 默默改成"看起来合理"的值(违背代码以用户/设计为准原则)
- 直接说"按用户给的字面值"(已知有畸变还照搬)
- 一次性问 5+ 个问题(用户疲劳)

### 3. KDoc 真机调整留痕(防漂移复发)

每次代码后续在真机上被微调(用户/IDE 改 offset/size),**KDoc「坐标说明」段必须同步补一句**"代码后续真机上调过:...见行内注释"。模板:

```kotlin
/**
 * 布局(z-order 由下到上):
 *   - 图1(image 320.png,X=18, Y=135, W=350, H=312)— 上部
 *   ...
 *
 * 坐标说明:
 *   - 用户原始设计稿给的是 X=28/Y=155/W=352/H=203
 *   - image 320 实测 696×612(比率 1.137);用户给 W=352/H=203 渲染比 1.734,偏宽 35%
 *   - AskUserQuestion 后改为:图1 W=352→350 H=203→312(图2 W=356→353 H=214→314)
 *   - 代码后续真机上调过:图1 H=312→322;图2 Y=351→461(见行内注释)
 */
```

为什么:
- 防止后人/自己看 KDoc 时困惑"为什么 KDoc 数值 ≠ 用户指令"
- 防止下次 commit 前"注意注释"轮再来一遍
- 留痕"哪次是真机微调、哪次是原设计、哪次是 AskUserQuestion 修订"

---

## 4 个典型案例

### 案例 A:image 320 漂移值(典型正向纠错)

- 用户指令:图1 `X28 Y155 W352 H203`
- PNG 实测:696×612,比率 1.137
- 渲染比 1.734,**严重拉宽 35%**(横向拉 1.73×,纵向压到原高 28%)
- 决策:AskUserQuestion → 用户选"套用 Vol-3-1 实调尺寸 352×327"
- 结果:Vol-3-2 用 `18/135/352/327`,渲染比 1.076 vs 1.137(可接受)

### 案例 B:image 339 越界(典型限高保安全)

- 用户指令:图2 `X18 Y351 W356 H342`
- PNG 实测:710×810,比率 0.877(近正方)
- 渲染比 1.187 vs 0.877,**严重拉宽 35%**
- Y=576+342=918,**越出书框底 872 共 46dp**
- 决策:AskUserQuestion → 用户选"限高保安全 356×296"
- 结果:Vol-3-10 用 `18/471/356/356`(正方形),Y=827 安全,14% 拉高可接受

### 案例 C:image 333 几乎完美(无需决策)

- 用户指令:图1 `X18 Y135 W355 H369`
- PNG 实测:708×734,比率 0.965
- 渲染比 0.962 vs 0.965,**0.3% 差距**
- 决策:**无需 AskUserQuestion,直接采用**
- 结果:Vol-3-7 用 `18/135/355/369`,零修订

### 案例 D:Vol-3-6/7/8 注释漂移(真机调整后未同步)

- 现象:用户多次说"注意注释"
- 根因:用户/IDE 在真机上微调 offset/size,KDoc/行内注释没同步
- 处理:**以代码为准修注释**,共修复 12+ 处
- KDoc 补:每屏「坐标说明」段加"代码后续真机上调过:..."句

### 案例 E:全卷批量"按宽度调整"消畸变(2026-09-12)

- **触发**:用户口头指示"排查一下有没有畸变超过5%的图像,按照宽度去调整图像"
- **范围**:144 个内容图像(volumeNpartN_image_* / learningN_image_*)全卷扫描
- **发现**:43 项畸变 >5%(分布 Vol-1/2/3/4/5/6 + Learning-4),最大 18.6%(Vol-2-9 image 293)
- **决策**:用户给出**新规则**——所有内容图像一律 `H = round(W / 原图比例)`,消除 `ContentScale.FillBounds` 拉伸
- **结果**:42 项自动 + 1 项手动修复,**剩余 0 项 >5%**
- **KDoc 留痕**:每处加"用户 2026-09-12 按宽度调整消除畸变,H=X→Y 自然高度 W/比例"
- **沉淀**:规则写入 [[image-size-by-width-default]](~/.claude/projects/d--Appproject/memory/image-size-by-width-default.md)(跨会话生效)

---

## 全卷畸变审计脚本(案例 E 沉淀)

### 使用方法

```bash
PYTHONIOENCODING=utf-8 python audit_distortion.py
```

输出格式:
```
=== > 5% content images ===
  Volume4Part7Screen  img_volume4part7_image_366  W=355 H=311  diff 15.5%  -> new H=263
  ...
```

阈值 5% 是用户规则定的(image-size-by-width-default memory)。

### 脚本实现要点(供以后移植到 `tools/audit_distortion.py`)

```python
import struct, os, re, glob

DRAW_DIR = "android/app/src/main/res/drawable-nodpi"
SCREEN_DIR = "android/app/src/main/java/com/jueqiao/jianghu/ui/screens"

def png_dims(name):
    """读 PNG 头 16-24 字节拿宽高"""
    p = os.path.join(DRAW_DIR, name + ".png")
    with open(p, 'rb') as f: data = f.read(24)
    if data[:8] != b'\x89PNG\r\n\x1a\n': return None
    w, h = struct.unpack('>II', data[16:24])
    return (w, h)

def is_content_image(name):
    """只审计 Vol-N + Learning 系列内容图,跳过 UI/书框/背景"""
    return bool(re.match(r'^img_(volume\d+part\d+_image_|learning\d+_image_)', name))

results = []
for kt_path in glob.glob(f'{SCREEN_DIR}/**/*Screen.kt', recursive=True):
    screen = os.path.basename(kt_path).replace('.kt', '')
    with open(kt_path, encoding='utf-8') as f: content = f.read()
    # 每个 painterResource 后向前找最近的 .size()
    for m in re.finditer(r'painterResource\(R\.drawable\.(\w+)\)', content):
        img = m.group(1)
        if not is_content_image(img): continue
        start = max(0, m.start() - 600)
        snip = content[start:m.start()]
        sizes = list(re.finditer(r'\.size\(width\s*=\s*([\d.]+)\.dp,\s*height\s*=\s*([\d.]+)\.dp\)', snip))
        if not sizes: continue
        w, h = float(sizes[-1].group(1)), float(sizes[-1].group(2))
        d = png_dims(img)
        if not d: continue
        ow, oh = d
        ratio_o = ow / oh
        ratio_r = w / h
        diff = abs(ratio_o - ratio_r) / ratio_o * 100
        if diff > 5:
            results.append((screen, img, w, h, diff, round(w / ratio_o)))
```

### 已知 bug:多图像屏的 backward search

**问题**:屏有 2 张图(image_364 + image_366),`backward search` 找 `.size()` 时,会抓到**前一张图**的 .size() 而不是当前的。
**修复**:改成从 inline comment (`// 图N(image X.png,...)`) 前向找 .size(),或按 modifier 嵌套结构解析。

实际 2026-09-12 案例 E 中 Vol-4-7 image 366 因此漏改,需手动 Edit 修正。

---

## 按宽度调整 H 的标准公式(2026-09-12 新规则)

```
H_natural = round(W_given / 原图_width_height_ratio)
```

| H偏离原图比例 | 处理 |
|---|---|
| ≤ 5% | 用用户 H(容差)|
| > 5% | **自动按比例重算 H**,KDoc 留痕"用户字面 H=X→自然 H=Y 消除畸变"|
| 用户显式说"按 H=X"或真机已调过 | 尊重用户 H,留痕 |

详见 [[image-size-by-width-default memory]](~/.claude/projects/d--Appproject/memory/image-size-by-width-default.md)。

---

## 决策树(快速参考)

```
收到"创建新屏"指令
   │
   ├─ 1. grep 同卷兄弟屏的实际 offset/size(不要看用户给的字面值)
   │
   ├─ 2. PNG 像素校验用户给的字面值
   │     │
   │     ├─ 渲染比 vs 原图比 差 < 5% → 直接采用,无需问
   │     │
   │     └─ 差 > 5% → AskUserQuestion
   │            │
   │            ├─ Q1:图坐标怎么改?(给 3 选项:套原图/限高/字面)
   │            ├─ Q2:书框素材?(给 2 选项:255/256 + 交替性)
   │            └─ Q3:标题宽度?(给 2 选项:W=192 / W=213)
   │
   ├─ 3. 按用户确认值写代码 + KDoc + 行内注释
   │
   ├─ 4. KDoc「坐标说明」段必写:
   │     - 用户原始值
   │     - PNG 实测比例
   │     - 渲染比差距 + 是否修订
   │     - (如有)AskUserQuestion 修订说明
   │
   └─ 5. 接线 Routes/RoutesTest/JianghuNavHost + 上屏 callback
```

---

## 不要做什么

- ❌ 默写用户给的值,不校验就动手
- ❌ 默默改成"看起来合理"的值(违背"代码以用户/设计为准")
- ❌ 直接照搬同屏字面(不问"是否按交替")
- ❌ 真机调整 offset 后不补 KDoc 留痕(导致下次又"注意注释")
- ❌ 跳过 PNG 校验步骤(2 秒就能做完)

---

## 已知陷阱

| 陷阱 | 表现 | 解决 |
|---|---|---|
| **图片族混淆** | image 320/321/316/319 都是 696×612 同族横向矩形;用户给 `W=352 H=229` 是另一族扁图的尺寸,会拉宽 35% | PNG 校验 + AskUserQuestion 修订 |
| **图3 越界** | Y=576 H=342 → 底 Y=918 > 书框底 872(46dp 溢出) | 限高保安全 |
| **正方图按矩形渲染** | 近正方图(0.87 / 0.96 比例)被配横向矩形尺寸,严重拉宽 | 套原图比例 |
| **标题宽度文本长度不匹配** | 7 字文本配 W=192 / 5 字文本配 W=213 / W=302 是 3 屏真机调过的值 | 按字数选 W |
| **真机调整未同步注释** | 用户/IDE 改 offset 后 KDoc/行内没跟 | KDoc 必补"代码后续真机调整"留痕 |

---

## 沉淀索引

- **沉淀出处**:2026-09-11 一天 11 屏 Vol-3 创建批 + 3 轮"注意注释"修复;**2026-09-12 增量**:案例 E 全卷批量"按宽度调整"消畸变 + 4 个新 memory/image rules
- **配套 memory**:
  - `~/.claude/projects/d--Appproject/memory/screen-copy-verify-coordinates.md` — 跨会话生效
  - `~/.claude/projects/d--Appproject/memory/image-size-by-width-default.md` — 2026-09-12 新增,H = W / 比例
- **相关文档**:
  - [docs/CODE-AUDIT-2026-09-11.md](./CODE-AUDIT-2026-09-11.md) — Vol-2 doc 批审计,本方法源自该审计的 H2-H4 + M6
  - [docs/SESSION-LOG-2026-09-11.md § 19-27](./SESSION-LOG-2026-09-11.md) — 每天操作记录,含本方法所有使用实例
  - [docs/SESSION-LOG-2026-09-12.md § 31-33](./SESSION-LOG-2026-09-12.md) — Vol-5/Vol-6 创建批 + 注释审计 + 全卷畸变排查
- **沉淀时间**:2026-09-11(初版)+ 2026-09-12(增量)