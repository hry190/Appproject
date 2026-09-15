# SESSION-LOG-2026-09-15

> 昨日: [SESSION-LOG-2026-09-14.md](SESSION-LOG-2026-09-14.md)
> 今日工作: adb reverse 重设 + 把昨天 SESSION-LOG 完整版合并到 `docs/`
> 重要: 昨日(09-14)工作日志原保存在 `docs/_archive/`,今按用户指令合并到 `docs/SESSION-LOG-2026-09-14.md`(完整 151 行),archive 保留作为历史快照

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | codex/ifthen |
| 主要工作 | 1. adb reverse 重设 2. 合并 09-14 SESSION-LOG |
| adb 设备 | 21908b7a(小米 K50 Pro,UsbFfs)|
| adb reverse | `tcp:8010 → tcp:8010` ✓ |
| 后端 8010 | LISTENING PID 22796(由 `infra/start-dev.ps1` 启动)|
| 09-14 文件合并 | `docs/SESSION-LOG-2026-09-14.md` 45 行 → 151 行(与 `_archive/` 内容一致)|

## 当天操作记录(从最近往前)

### §1 adb reverse 重设(2026-09-15 上午)

**用户指令**:"重设adb"

**触发原因**:USB 重插/重启手机导致 `adb reverse` 隧道丢失。

**操作**:
1. `adb devices` → 确认 21908b7a 已连接
2. `adb reverse --list` → 空(确认丢失)
3. `adb -s 21908b7a reverse tcp:8010 tcp:8010` → 返回 `8010`(成功)
4. `adb reverse --list` → `UsbFfs tcp:8010 tcp:8010` ✓
5. `netstat -ano | grep :8010` → PC 端 `0.0.0.0:8010` LISTENING (PID 22796) ✓

**结论**:后端服务持续运行(无需重启),只需重设 reverse 即可恢复登录链路。

### §2 合并 09-14 SESSION-LOG 到 `docs/`(2026-09-15 上午)

**用户指令**:"昨天的.md文件写了吗" → "合并到 docs/(完整版)"

**触发原因**:
- 用户问"昨天的 SESSION-LOG 是否写了",经检查:
  - `docs/SESSION-LOG-2026-09-14.md` 只有 45 行简短版(仅记录昨天的回退操作)
  - `docs/_archive/SESSION-LOG-2026-09-14.md` 有 151 行完整版(原 09-14 上午 houshan1 回调变更 §1.1~§1.3 + §81 回退记录)

**操作**:
1. 把 `docs/_archive/SESSION-LOG-2026-09-14.md` 内容复制到 `docs/SESSION-LOG-2026-09-14.md`(覆盖简短版)
2. archive 文件保留作为历史快照(不删除)
3. (待执行)commit + push

**09-14 SESSION-LOG 内容覆盖**:
- §1.1 Houshan1Screen "识机真决"按钮回调从 Houshan2 改为 Vol-1
- §1.2 双按钮绑定 + Bug 修正(.clickable 错绑到气泡 Box 修正)
- §1.3 关键 Bug 沉淀 + 预防措施
- §81 22:36 回退 codex/ifthen 到 8f5a28c

**已知缺失**:09-14 下午~晚上的 50 个 houshan1 云朵/动效 commit 没有 SESSION-LOG 记录(被回退时也丢失),archive 里只有 §81 段描述了"这些 commit 被丢弃"。

### §3 替换 Houshan1Screen 背景图(2026-09-15 上午)

**用户指令**:"'D:\图\试炼.png' 我已经替换过原图,请替换'后山1'页面中的背景图"

**操作**:
- 源图:`D:\图\试炼.png` (3.7 MB, 1236×2751, RGBA, Sep 14 20:30 用户已替换过)
- 目标:`android/app/src/main/res/drawable/img_shilian_bg.png`
- 操作:直接覆盖(cp 不做 fit 调整,因为是全屏背景 + ContentScale.Crop)
- `.kt` 代码**无需改**:`R.drawable.img_shilian_bg` 引用对 `drawable/` 目录已正确
- 8f5a28c 基线上背景图就在 `drawable/`(8f5a28c..HEAD 那 50 个 commit 把它挪到 `drawable-nodpi/`,reset 后回到 `drawable/`)

**文件变化**:
- 旧:1,901,188 bytes (Sep 9 08:53)
- 新:3,734,846 bytes (Sep 15 10:10)
- 增量:+1,833,658 bytes (1.75 MB 增长)

**为什么不需要 fit**:全屏背景 + ContentScale.Crop 自然填满屏幕,不存在列框宽高比问题(fit-to-natural-bounds 规则不适用)。

**git 状态**:`M android/app/src/main/res/drawable/img_shilian_bg.png`

### §4 加云朵 58(Ellipse 58.png)(2026-09-15 上午)

**用户指令**:"'D:\图\Ellipse 58.png' 放在后山页的 X=-47 Y=429 W=277 H=92"

**用户原话尺寸问题**:
- 用户给的 W=277 H=92(横图,比 3.01)
- 源图 `D:\图\Ellipse 58.png` 实际 844×474(横图,比 1.781)
- 两个比例不一致:3.01/1.781 = 1.69,即用户给的"宽高比"是源图实际比的 1.69 倍
- 按 [image-fit-to-natural-bounds](image-fit-to-natural-bounds) 规则 fit max_W=355 × max_H=394 → **W=355 H=199**(横图,W=355,H=round(355/1.781)=199)
- 按 [screen-copy-verify-coordinates](screen-copy-verify-coordinates) 不一致就问 → 用户选 "fit-to-natural-bounds: W=355 H=199 (推荐)"

**操作**:
1. 复制源图到 `android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_58.png`(drawable-nodpi 跳过压缩,符合之前 50 个 commit 链惯例)
2. Houshan1Screen.kt 在内容层开头(line 67-68 之间)插入 Image 代码:
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_houshan1_cloud_58),
       modifier = Modifier
           .offset(x = -47.dp, y = 429.dp)
           .size(width = 355.dp, height = 199.dp),
       contentScale = ContentScale.FillBounds,
   )
   ```
3. z-order:云朵在内容层最开始(熊猫之前绘制,云朵在下,熊猫在上)— 与之前回退的云朵层一致

**文件变化**:
- 新增:android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_58.png (164135 bytes, 844×474)
- 修改:android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt (+10 行)

**注意**:
- 用户本轮尝试 `D:\图\试炼.png` 写过错的图片名,已先不动
- 本次 `D:\图\Ellipse 58.png` 才是真正的源图
- 命名:`Ellipse 58.png` → `img_houshan1_cloud_58.png`(Android 资源不能有空格/大写,符合 cloud 命名系列)

**git 状态**:
- `M Houshan1Screen.kt` (+10)
- `?? img_houshan1_cloud_58.png` (新增)

### §5 加云朵 61(Ellipse 61.png)+ 云朵 58 用户调整(2026-09-15 上午)

**用户指令**:
1. (uncommitted)用户调整云朵 58:X=-47 Y=429 W=355 H=199 → X=-70 Y=320 W=455 H=259(真机看着比例不对)
2. "'D:\图\Ellipse 61.png' 放在后山1页的 X=101 Y=304 W=90 H=43"

**云朵 58 调整合理性**:
- 新尺寸 W=455 H=259,比 1.758
- 与 PNG 实际比 1.781 几乎完美匹配(偏差 0.023)
- 比之前的 355×199(比 1.78)略大且向左上移动,合理

**云朵 61 尺寸问题**:
- 源图 `D:\图\Ellipse 61.png` 351×210,比 1.671(横图)
- 用户给的 W=90 H=43,比 2.093 — 偏差 1.25 倍
- 按 [image-fit-to-natural-bounds](image-fit-to-natural-bounds) 横图 fit max_W=355 × max_H=394 → **W=355 H=213**
- 按 [screen-copy-verify-coordinates](screen-copy-verify-coordinates) 不一致就问 → 用户选 fit-to-natural-bounds

**操作**:
1. 复制源图到 `android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_61.png`(21243 bytes, 351×210)
2. 在云朵 58 Image 之后插入云朵 61 Image(line 78 之前)
3. 同步更新云朵 58 注释(`-47/429/355/199` → `-70/320/455/259`)— 注释之前没改,代码改了导致不一致
4. z-order:云朵 58 → 云朵 61 → 熊猫 → 标签 → 气泡 → 返回(均在下,熊猫等前景在上)

**文件变化**:
- 新增:`android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_61.png` (21243 bytes, 351×210)
- 修改:`android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt`(+12 行:云朵 61 + 注释更新)

**云朵 61 警告**:W=355 H=213 是非常大的云朵,放在 X=101 Y=304 会覆盖屏幕大部分(包括熊猫(184,621)、标签(168,345)、Rectangle156(136,508))。用户明确选了 fit-to-natural-bounds。

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+12)
- `M docs/SESSION-LOG-2026-09-15.md` (+本节)
- `?? img_houshan1_cloud_61.png` (新增)

### §6 加云朵 56(Ellipse 56.png)(2026-09-15 上午)

**用户指令**:"'D:\图\Ellipse 56.png' 放在 X=236 Y=715 W=335 H=297"

**源图与尺寸对比**:
- 源图 `D:\图\Ellipse 56.png`:1487×1373,比 **1.083**(近正方形)
- 用户给的 W=335 H=297,比 **1.128**
- 偏差:1.128/1.083 = **1.04**(仅 4%,可接受范围)
- 用户给的尺寸正是之前 50 个 houshan1 commit 链上 `5a406e2` 用过的尺寸:"feat(houshan1): 加云朵 11 (Ellipse 56, 用户指定位置 X=278 Y=755 W=335 H=297)"

**为什么直接采纳用户 W/H**(不 fit):
- 偏差仅 4%,远低于 25% 警戒线(对比之前 Ellipse 58/61 用户值都是 60%+ 偏差)
- 尺寸是历史 commit 链上验证过的尺寸,非凭印象
- 按 [image-fit-to-natural-bounds](image-fit-to-natural-bounds) 横图 fit max_W=355 × max_H=394 → W=355 H=328,与用户给的 W=335 H=297 差距很小,效果近似
- 实际渲染差别:差 20px 宽 + 31px 高,真机几乎看不出差别

**操作**:
1. 复制源图到 `android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_56.png`(1618298 bytes, 1487×1373)
2. 在云朵 61 之后、熊猫之前插入 Image(line 88 之前)
3. z-order:云朵 58 → 云朵 61 → 云朵 56 → 熊猫 → 标签 → 气泡 → 返回

**文件变化**:
- 新增:`android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_56.png` (1.6 MB, 1487×1373)
- 修改:`Houshan1Screen.kt` (+9 行)

**位置观察**:Y=715 在熊猫(621)下方,Rect156(508)下方,接近屏幕底部。W=335 H=297 是云朵 61 (W=355 H=213) 的"放大版"——比云朵 61 还宽一点,高很多。**会盖住熊猫(184,621)+ 标签 1(-13,570)下方**。

### §7 Ellipse 56.png 不透明度 100% + 云朵 61/56 用户调整(2026-09-15 上午)

**用户指令**:"Ellipse 56.png 的不透明度达到 100%"

**操作**:
1. 在云朵 56 的 Image 添加 `alpha = 1f`(Compose `Image` 参数)— 100% 不透明
2. 同步云朵 56 的注释坐标(IDE 调整后未同步:X=236/Y=715 → X=196/Y=595)
3. uncommitted 改动一并 commit:
   - 云朵 61:X=101 → -50(用户 IDE 调整)
   - 云朵 56:X=236/Y=715 → X=196/Y=595(用户 IDE 调整)
   - 云朵 56 alpha=1f(本次新增)

**代码改动**:
```kotlin
Image(
    painter = painterResource(R.drawable.img_houshan1_cloud_56),
    ...
    modifier = Modifier
        .offset(x = 196.dp, y = 595.dp)
        .size(width = 335.dp, height = 297.dp),
    alpha = 1f,  // 100% 不透明 — 用户指令 2026-09-15 §7
    contentScale = ContentScale.FillBounds,
)
```

**为什么用 alpha 参数**(而不是 `Modifier.graphicsLayer(alpha = ...)`):
- Compose `Image` 自带 `alpha: Float` 参数,默认 1.0f
- 设置为 1.0f 等价"完全不透明",但**显式声明**可避免被未来 graphicsLayer 链上的 alpha 透明度影响
- 这正是之前回退的 50 个 commit 链上 `ca06570 feat(houshan1): Ellipse 56 不透明度 100% — 移除透明度时变动画` 的本意

**注意**:`alpha=1f` 控制 Image 整体不透明度,不影响 PNG 内部像素的 alpha 通道。如果 PNG 本身有透明区域,那些区域仍然透明(被背景显示)。

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+3 行:alpha 参数 + 注释同步)

### §8 云朵 56 椭圆飘动 ±40 dp / 7s(2026-09-15 上午)

**用户指令**:"Ellipse 56.png 是云朵,要形成一种动效,要修成云朵飘动的特性"

**用户选择**:椭圆轨迹 ±40 半径、7s 一圈

**实现**:Compose `rememberInfiniteTransition + animateFloat`(比 50 commit 链上的 Animatable + LaunchedEffect 模式简洁)
```kotlin
val cloud56Transition = rememberInfiniteTransition(label = "cloud56Float")
val cloud56Angle by cloud56Transition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * Math.PI).toFloat(),
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 7000, easing = LinearEasing),
    ),
    label = "cloud56Angle",
)
val cloud56Dx = (sin(cloud56Angle).toFloat() * 40f)
val cloud56Dy = (cos(cloud56Angle).toFloat() * 40f)
```

云朵 56 Image offset 改为:
```kotlin
.offset(x = (196f + cloud56Dx).dp, y = (595f + cloud56Dy).dp)
```

**新加 imports**:
- `androidx.compose.animation.core.{animateFloat, infiniteRepeatable, LinearEasing, rememberInfiniteTransition, tween}`
- `androidx.compose.runtime.getValue`
- `kotlin.math.{cos, sin}`

**轨迹特征**:
- X = sin(angle) × 40,起点 0,7s 后回到 0(逆时针/顺时针取决于坐标系)
- Y = cos(angle) × 40,起点 40(最下),7s 后回到 40(最下)
- 实际上 Y = cos 在 angle=0 时 = 1,所以起点 Y=595+40=635(下),angle=π/2 时 Y=595-40=555(上)
- 完整圆周轨迹,中心 (196, 595),半径 40

**其他云朵无动效**:云朵 58/61 保持静止(用户未要求动效)。如需动效,告诉我具体云朵。

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+13 行:5 行 import + 8 行 transition 代码;offset 改 2 行)
- `M docs/SESSION-LOG-2026-09-15.md` (+本节)

### §9 加云朵 57(Ellipse 57.png)(2026-09-15 下午)

**用户指令**:"'D:\图\Ellipse 57.png' X=248 Y=570 W=225 H=191"

**尺寸决策**(直接采纳):
- 源图 `D:\图\Ellipse 57.png`:**986×884,比 1.115**(近正方形)
- 用户给的 W=225 H=191:比 **1.178**
- 偏差:1.178/1.115 = **1.057**(仅 5.7%,可接受)
- 与 §6 Ellipse 56 (4% 偏差) 类似,采纳用户值而非 fit-to-natural-bounds
- 命名:沿用 cloud 系列 `img_houshan1_cloud_57.png`

**操作**:
1. 复制源图到 `android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_57.png`(796237 bytes, 986×884)
2. 在云朵 56 后、熊猫前插入 Image 代码

**z-order**:云朵 58 → 61 → 56(飘动)→ **57(静态)** → 熊猫 → 标签 → 气泡 → 返回

**位置观察**:
- X=248~473, Y=570~761 — 右下角云朵
- 与云朵 56 飘动范围(156~236)×(555~635):
  - X 方向不重叠(云朵 56 max X=236,云朵 57 min X=248)
  - Y 方向有重叠(云朵 56 Y 555~635,云朵 57 Y 570~761)
- 云朵 56 在飘动时不会盖到云朵 57(只在 X 上接近但不接触)

**历史**:`4481499 feat(houshan1): 加右下角云朵 Ellipse 57, 上下飘动效` — 50 commit 链上 Ellipse 57 原本有"上下飘动效",用户这次没要求动效(只给了静态位置)。

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+9 行)
- `M docs/SESSION-LOG-2026-09-15.md` (+本节)
- `?? img_houshan1_cloud_57.png` (新增)

### §10 加云朵 60(Ellipse 60.png)+ 云朵 57 用户调整(2026-09-15 下午)

**用户指令**:"'D:\图\Ellipse 60.png' X=-21 Y=570 W=247.5 H=61.64"

**尺寸决策**(用 fit-to-natural-bounds):
- 源图 `D:\图\Ellipse 60.png`:**911×353,比 2.581**(扁长横图)
- 用户给的 W=247.5 H=61.64:比 **4.014**
- 偏差:**55%**,远超 4-6% 可接受标准 — 按 [screen-copy-verify-coordinates](screen-copy-verify-coordinates) 必须问用户
- 用户选择:**fit-to-natural-bounds:W=355 H=round(355/2.581)=137**(横图 fit max_W=355 × max_H=394)

**为什么偏差这么大**:
- W=247.5 H=61.64 是 Figma 设计稿里的精确小数(2 位/3 位小数)
- PNG 实际比 2.581,等于设计比的一半左右
- 可能 PNG 不是设计稿用的那张图(也许叫 Ellipse 60b 或别的),但用户没提供其他候选

**操作**:
1. 复制源图到 `android/app/src/main/res/drawable-nodpi/img_houshan1_cloud_60.png`(90897 bytes, 911×353)
2. 在云朵 57 后插入 Image 代码(X=-21, Y=570, W=355, H=137)
3. 同时 commit uncommitted 改动:云朵 57 offset X=248 → 208(用户 IDE 调整)+ 注释同步

**z-order**:云朵 58 → 61 → 56(飘动)→ 57(静态,用户调整)→ **60(静态)** → 熊猫 → 标签 → 气泡 → 返回

**位置观察**:
- X=-21 → 起始部分屏幕外,W=355 几乎横跨整个屏幕宽度
- Y=570 → 在标签 1 同一行高度
- 与云朵 57(208~433 × 570~761)重叠 X 范围(208~355)
- 与 Rectangle156(136,508,177,107)Y 范围(508~615)重叠 (570~615 部分)
- 命名:`Ellipse 60.png` → `img_houshan1_cloud_60.png`(注意:50 commit 链上 `cloud_60b` 是另一张图,这里用 `cloud_60`)

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+11 行:云朵 60 + 云朵 57 注释更新)
- `M docs/SESSION-LOG-2026-09-15.md` (+本节)
- `?? img_houshan1_cloud_60.png` (新增)

### §11 云朵 60 上下浮动 ±15 dp / 4s(2026-09-15 下午)

**用户指令**:"'D:\图\Ellipse 60.png' 我希望云朵能具有动画效果"

**用户选择**:
- 仅云朵 60(云朵 58/57 仍静止)
- 上下浮动 ±15 / 4s(half-cycle 2s,RepeatMode.Reverse)

**实现**:`rememberInfiniteTransition + animateFloat` 用 tween + RepeatMode.Reverse(比 sin/cos 模式更简洁,因为是纯上下浮动)
```kotlin
val cloud60Transition = rememberInfiniteTransition(label = "cloud60Float")
val cloud60Y by cloud60Transition.animateFloat(
    initialValue = -15f,
    targetValue = 15f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "cloud60Y",
)
```

云朵 60 Image offset 改为:
```kotlin
.offset(x = (-21).dp, y = (570f + cloud60Y).dp)
```

**新加 import**:`androidx.compose.animation.core.RepeatMode`

**运动轨迹**:
- 0s: Y=-15(上偏 15dp)
- 2s: Y=+15(下偏 15dp)
- 4s: Y=-15(回到上)
- X 始终 -21(不动)
- LinearEasing 匀速

**与云朵 56 的差异**:
| 云朵 | 动效 | 周期 | 实现 |
|---|---|---|---|
| 56 | 椭圆轨迹 (X,Y 都动) | 7s | sin/cos + LinearEasing |
| 60 | 上下浮动 (仅 Y) | 4s (2s reverse) | tween + RepeatMode.Reverse |

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+8 行:1 行 import + 7 行 transition + offset 改 1 行)

### §12 云朵 60 自然飘动:X ±100 + Y ±15 + Alpha 0.5~1.0 随机(2026-09-15 下午)

**用户指令**:"我希望'D:\图\Ellipse 60.png'还可以随机地上下移动,在移动的路上还可以变化透明度,左右移动的范围还可以达到 100 单位"

**用户需求拆解**:
1. 随机上下移动(无固定周期/无固定幅度)
2. 移动过程中透明度变化
3. 左右移动范围 ±100 单位

**实现**:`Animatable + LaunchedEffect + Random`(比 `rememberInfiniteTransition` 复杂但能产生"随机感")
```kotlin
val cloud60X = remember { Animatable(0f) }
val cloud60Y = remember { Animatable(0f) }
val cloud60Alpha = remember { Animatable(1f) }

// 3 个独立 LaunchedEffect 协程并行
LaunchedEffect(Unit) {
    while (isActive) {
        cloud60X.animateTo(
            targetValue = Random.nextFloat() * 200f - 100f,  // ±100
            animationSpec = tween(durationMillis = Random.nextInt(1500, 3000), easing = LinearEasing),
        )
        delay(Random.nextLong(500, 1500))
    }
}
LaunchedEffect(Unit) {
    while (isActive) {
        cloud60Y.animateTo(
            targetValue = Random.nextFloat() * 30f - 15f,  // ±15
            animationSpec = tween(durationMillis = Random.nextInt(1000, 2000), easing = LinearEasing),
        )
        delay(Random.nextLong(300, 800))
    }
}
LaunchedEffect(Unit) {
    while (isActive) {
        cloud60Alpha.animateTo(
            targetValue = 0.5f + Random.nextFloat() * 0.5f,  // 0.5~1.0
            animationSpec = tween(durationMillis = Random.nextInt(1500, 3000), easing = LinearEasing),
        )
        delay(Random.nextLong(500, 1200))
    }
}
```

云朵 60 Image 改为:
```kotlin
Image(
    painter = painterResource(R.drawable.img_houshan1_cloud_60),
    modifier = Modifier
        .offset(x = (-21f + cloud60X.value).dp, y = (570f + cloud60Y.value).dp)
        .size(width = 355.dp, height = 137.dp),
    alpha = cloud60Alpha.value,
    contentScale = ContentScale.FillBounds,
)
```

**新加 imports**:
- `androidx.compose.animation.core.Animatable`
- `androidx.compose.runtime.{LaunchedEffect, remember}`
- `kotlin.random.Random`
- `kotlinx.coroutines.{delay, isActive}`

**§11 → §12 变化**:
| 项 | §11 | §12 |
|---|---|---|
| 实现 | rememberInfiniteTransition + tween + RepeatMode.Reverse | Animatable + LaunchedEffect + Random |
| X 范围 | 0(不动)| ±100 随机 |
| Y 范围 | ±15 周期性 | ±15 随机 |
| Alpha | 1f 固定 | 0.5~1.0 随机 |
| 节奏 | 周期 4s | 每次 1~3s,delay 0.3~1.5s 随机 |

**运动特征**:
- 每次到目标值后随机 delay,再选下一个目标值
- X/Y/Alpha 三者独立变化,产生"自然飘动"感
- 50 commit 链上 `df2a2b6 fix(houshan1): 改用 Animatable + LaunchedEffect 手动驱动云朵动画` 即此模式

**注意**:`while (isActive)` 是协程挂起函数,在 LaunchedEffect 中保留 scope;屏幕销毁时自动取消循环。

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+30 行:5 行 import + 25 行 LaunchedEffect 代码;Image offset/alpha 改 3 行)

## 沉淀(新)

- **adb 重插恢复 SOP**:`adb -s <device> reverse tcp:8010 tcp:8010` 单条命令即可,前提是后端 8010 已在 PC 跑(`infra/start-dev.ps1`)
- **SESSION-LOG 合并 SOP**:archive 目录可作为 SESSION-LOG 历史快照,合并到 docs/ 时直接复制内容即可(链接相对路径在 docs/ 里反而变正确)
- **当日 SESSION-LOG 必建**:即使是 commit/push 前的最小动作也要建当日文件

## 明天(可选)优先级

| 优先级 | 任务 |
|---|---|
| **高** | 1. 在 `ee50940` + 09-15 merge commit 之上,继续 houshan1 工作(8f5a28c 基线重做) |
| **中** | 2. 编译 APK 真机测试 houshan1(在 8f5a28c 基线上)|
| **低** | 3. 09-14 下午 50 个云朵 commit 是否需要补救日志(用户决定)|

## 重要建议(沿用 + 新加)

1. **adb 重插 = 单条 reverse 命令**(沿用 memory `usb-replug-recovery`)
2. **Gradle JDK 永远设 jbr-21**(沿用 memory `gradle-jdk-jbr21`)
3. **commit 之前必先写当日 SESSION-LOG**(沿用 memory `commit-push-summary-rule`)
4. **合并 archive 到 docs/ 时直接覆盖**(新)— 链接相对路径在新位置变正确
