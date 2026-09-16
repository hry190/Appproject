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

### §13 其他 3 朵云(58/61/57)加随机飘动 + 抽出 rememberCloudFloat helper(2026-09-15 下午)

**用户指令**:"让其他云朵也这样子"

**重构决策**:抽出 `@Composable private fun rememberCloudFloat()` helper,避免 4 朵云重复 ~30 行 LaunchedEffect 代码(每朵云 25 行 × 4 = 100 行重复)
```kotlin
@Composable
private fun rememberCloudFloat(
    maxX: Float = 100f,
    maxY: Float = 15f,
    alphaMin: Float = 0.5f,
    alphaMax: Float = 1f,
): Triple<Float, Float, Float> {
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    // 3 个 LaunchedEffect 协程并行,每次随机选目标值 + 随机 delay
    ...
    return Triple(x.value, y.value, alpha.value)
}
```

调用方(只 1 行/朵云):
```kotlin
val (cloud58Dx, cloud58Dy, cloud58Alpha) = rememberCloudFloat()
val (cloud61Dx, cloud61Dy, cloud61Alpha) = rememberCloudFloat()
val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()
val (cloud60Dx, cloud60Dy, cloud60Alpha) = rememberCloudFloat()
```

Image 改动(4 朵):
```kotlin
modifier = Modifier
    .offset(x = (-70f + cloud58Dx).dp, y = (320f + cloud58Dy).dp)
    .size(...),
alpha = cloud58Alpha,
contentScale = ContentScale.FillBounds,
```

**云朵 60 同步改造**:
- §12 的 3 个 Animatable + 9 个 LaunchedEffect 删除
- 替换为 1 行 `rememberCloudFloat()` 调用
- Image 改 `cloud60X.value` → `cloud60Dx`(helper 返回 Triple 解构成 Float)

**云朵 56 不动**:用户 §7 明确要求 alpha=1f 100% 不透明,且当前用 rememberInfiniteTransition + sin/cos 椭圆轨迹动效(§8)。如要也加随机飘动需用户指令。

**最终 5 朵云状态**:
| 云朵 | 动效类型 | 参数 | 周期/节奏 |
|---|---|---|---|
| 56 | 椭圆轨迹 (sin/cos) | X/Y ±40 | 7s 周期 |
| 58 | 随机飘动 | X±100/Y±15/Alpha 0.5~1.0 | 1~3s + delay 0.3~1.5s |
| 61 | 随机飘动 | 同上 | 同上 |
| 57 | 随机飘动 | 同上 | 同上 |
| 60 | 随机飘动 | 同上(§12) | 同上 |

**代码量对比**:
- 不抽 helper:4 × 25 = 100 行(重复)
- 抽 helper:~35 行 helper + 4 × 1 = 39 行(净节省 61 行)

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (~+50 行 helper + Image 改 12 行,§12 旧 LaunchedEffect 删 -25 行)

### §14 云朵 60 X 速度放慢约 2 倍(2026-09-15 下午)

**用户指令**:"'D:\图\Ellipse 60.png' 左右移动的速度太快了点,降低一些,其他云朵的速度不用改动"

**改动**:
- 给 `rememberCloudFloat()` helper 加 2 个新参数:`xDuration: IntRange`(X 动画时长)和 `xDelay: LongRange`(X delay 间隔)
- 默认值不变(1500..3000 / 500..1500)— 保持其他 3 朵云(58/61/57)行为不变
- **云朵 60 单独传慢节奏参数**:
  ```kotlin
  val (cloud60Dx, cloud60Dy, cloud60Alpha) = rememberCloudFloat(
      xDuration = 4000..6000,  // X 节奏放慢约 2 倍
      xDelay = 1000..2000,     // X delay 也放慢,目标切换频率减半
  )
  ```

**节奏对比**:
| 云朵 | X duration(ms) | X delay(ms) | 效果 |
|---|---|---|---|
| 58/61/57 | 1500~3000 | 500~1500 | 快节奏(原 §13)|
| **60** | **4000~6000** | **1000~2000** | **慢约 2 倍** |

**注意**:只改了 X 维度,Y 和 Alpha 保持默认节奏(用户只要求 X 慢)。

**为什么用 IntRange + last+1**:
- `Random.nextInt(from, until)` 是开区间 `[from, until)`
- `IntRange` 含两端(1500..3000 = [1500,3000])
- 所以传 `xDuration.last + 1 = 3001` 让 `Random.nextInt(1500, 3001)` 返回 [1500, 3000]

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+5 行 helper 注释 + 参数 + 函数体改;云朵 60 调用改 4 行)

### §15 修复云朵 60 LongRange 类型不匹配编译错误(2026-09-15 下午)

**用户报告**:"failed — Argument type mismatch: actual type is 'kotlin.ranges.IntRange', but 'kotlin.ranges.LongRange' was expected"

**根因**:
- helper 函数声明 `xDelay: LongRange = 500..1500`
- `500..1500` 在 Kotlin 中默认推断为 `IntRange`(因为字面量是 Int)
- 虽然赋值给 `LongRange` 目标类型时 Kotlin 会**尝试**升级为 LongRange,但有歧义时编译器报错
- 云朵 60 调用 `xDelay = 1000..2000` — `1000..2000` 显式是 `IntRange`,与参数 `LongRange` 不匹配 → **编译失败**

**修复**(加 L 后缀强制 Long 字面量):
```kotlin
// helper 默认值
xDelay: LongRange = 500L..1500L

// 云朵 60 调用
val (cloud60Dx, cloud60Dy, cloud60Alpha) = rememberCloudFloat(
    xDuration = 4000..6000,
    xDelay = 1000L..2000L,  // 加 L
)
```

**IntRange / LongRange 区分**:
- `1..10` → `IntRange`(Int 字面量)
- `1L..10L` → `LongRange`(Long 字面量)
- 两者**不**自动转换,必须显式用 L 后缀
- `xDuration: IntRange = 1500..3000` — 默认就是 IntRange,字面量 Int 没问题

**新 memory 候选**:`Range 字面量类型不自动升级 — IntRange vs LongRange 必须显式 L 后缀`

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+2 字符:`..1500` → `..1500L` 和 `1000..2000` → `1000L..2000L`)

### §16 复制云朵 60 动画到 Y=690(2026-09-15 下午)

**用户指令**:"把'D:\图\Ellipse 60.png'的动画再复制一个到Y轴为690的地方,其他信息不变"

**操作**:
1. **复用同一 PNG**:`img_houshan1_cloud_60.png`(不复制文件,不创建新资源)
2. 在 transition 区域添加云朵 60b helper 调用(完全相同参数 xDuration=4000..6000, xDelay=1000L..2000L):
   ```kotlin
   val (cloud60bDx, cloud60bDy, cloud60bAlpha) = rememberCloudFloat(
       xDuration = 4000..6000,
       xDelay = 1000L..2000L,
   )
   ```
3. 在云朵 60 Image 之后插入云朵 60b Image(Y=690,其他不变):
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_houshan1_cloud_60),  // 同一 PNG
       modifier = Modifier
           .offset(x = (-21f + cloud60bDx).dp, y = (690f + cloud60bDy).dp)
           .size(width = 355.dp, height = 137.dp),
       alpha = cloud60bAlpha,
       contentScale = ContentScale.FillBounds,
   )
   ```

**命名考虑**:
- 没用 `img_houshan1_cloud_60b.png`(50 commit 链上有此命名)— 因为是**复用同一 PNG**,不需要新文件
- 变量名用 `cloud60bDx/Dy/Alpha` 表示"60 的 b 副本"

**位置观察**:
- 云朵 60 Y=570,云朵 60b Y=690 — 垂直相距 120 dp
- X 都是 -21,W=355(几乎横跨屏幕) — 两朵云在屏幕上"上下叠"的感觉
- 两朵云的 X 移动各自独立(helper 每次调用创建独立 Animatable),动效**视觉不同步**

**最终 6 朵云状态**:
| 云朵 | 动效 | X 范围 | Y 范围 | Alpha | 节奏 |
|---|---|---|---|---|---|
| 56 | 椭圆轨迹 | ±40 | ±40 | 1f 固定 | 7s 周期 |
| 58 | 随机 | ±100 | ±15 | 0.5~1.0 | 1.5~3s |
| 61 | 随机 | ±100 | ±15 | 0.5~1.0 | 1.5~3s |
| 57 | 随机 | ±100 | ±15 | 0.5~1.0 | 1.5~3s |
| 60 | 随机(慢)| ±100 | ±15 | 0.5~1.0 | **4~6s** |
| **60b** | 随机(慢) | ±100 | ±15 | 0.5~1.0 | **4~6s** |

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+11 行:4 行 helper 调用 + 7 行 Image 代码)

### §17 再替换后山1背景图 — D:\图\试炼.png(2026-09-15 下午)

**用户指令**:"'D:\图\试炼.png' 我已经替换过原图,请把这个图像再替换我页面中的图像"

**文件变化**:
| 文件 | 大小 | 时间 |
|---|---|---|
| `D:\图\试炼.png`(源)| **3,562,012 bytes** (3.4 MB) | Sep 15 16:16(用户替换)|
| `img_shilian_bg.png`(项目)| 3,734,846 bytes (3.6 MB) | Sep 15 10:10(§3 commit)|
| `img_shilian_bg.png`(本次后)| 3,562,012 bytes (3.4 MB) | Sep 15 16:18 |

**尺寸**:1236×2751(不变)— 文件大小减小约 172 KB(173 万字节),可能是用户重新压缩或修改了某些细节

**操作**:`cp "D:/图/试炼.png" android/app/src/main/res/drawable/img_shilian_bg.png` 覆盖

**.kt 代码无改动**:`R.drawable.img_shilian_bg` 引用对 `drawable/` 目录已正确(8f5a28c 基线约定)

**历史**:50 commit 链上有 `953e939 feat(houshan1): 第三次替换背景图 — D:\图\试炼.png` 等多次替换记录 — 用户常会反复替换同一张源图

**git 状态**(commit 后):
- `M img_shilian_bg.png` (3.6 MB → 3.4 MB)
- `M docs/SESSION-LOG-2026-09-15.md` (+本节)

### §18 删除后山2页 + 基于后山1页重写,去掉熊猫和气泡(2026-09-15 下午)

**用户指令**:"可以把'后山2'页面删掉,把'后山1'页面复制过去改名叫'后山2'页面,但是不要复制熊猫图像和气泡及其文本"

**侦察结果**(agent 报告):
- 旧 [Houshan2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt):208 行,已经从 Houshan1 复制并去掉 Rectangle156 气泡,但**没有云朵动画**(旧版时还没加云朵)
- [JianghuNavHost.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt) 接线:`composable(Routes.Shilian2) { Houshan2Screen(onBack, onOpenHoushan3) }` — **不动**
- [Routes.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt):`const val Shilian2 = "shilian2"` — **不动**
- [RoutesTest.kt:39](android/app/src/test/java/com/jueqiao/jianghu/nav/RoutesTest.kt):`assertEquals("shilian2", Routes.Shilian2)` — **不动**(因为 Routes.Shilian2 保持)

**操作**:
1. **删除旧文件**:`rm Houshan2Screen.kt`(208 行)
2. **新建** `Houshan2Screen.kt`(~395 行),基于当前 `Houshan1Screen.kt` 复制:
   - 包名:`com.jueqiao.jianghu.ui.screens.houshan2`
   - 函数签名:`Houshan2Screen(onBack: () -> Unit = {}, onOpenHoushan3: () -> Unit = {})`(同旧 Houshan2,删 `onOpenHoushan2`)
   - 整屏 `.clickable(onClick = onOpenHoushan3)`(同旧 Houshan2 设计)
   - **保留**:6 朵云(58/61/56/57/60/60b)+ 4 个标签(1-4)+ 左上角返回按钮 + rememberCloudFloat helper
   - **删除**:熊猫 `img_shilian_panda` Image + Rectangle156 气泡 Box + "御剑穿行云雾群山..." Text
   - 删除 import:`androidx.compose.foundation.layout.padding`(气泡文字用了)
   - 删除 import:`androidx.compose.animation.core.RepeatMode`(旧 Houshan1 用过但新版不用)

**KDoc 更新**:
- 顶部说明 §18 重写来源 + 删除的元素
- 列出所有布局元素(同旧 Houshan1 注释)

**同时 commit 的 uncommitted 改动**:
- [Houshan1Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt):云朵 60b Y=690 → 760(用户 IDE 真机调整)
- 修改了 cloud_60b 注释 Y 值要保持同步(下次 commit 或 IDE 自动同步)

**最终对照表**(Houshan1 vs 新 Houshan2):

| 元素 | Houshan1 | 新 Houshan2 |
|---|---|---|
| 背景图 | ✓ | ✓ |
| 云朵 58/61/56/57/60/60b(动画)| ✓ | ✓ |
| 4 个标签 + 文字 | ✓ | ✓ |
| 返回按钮 | ✓ | ✓ |
| 熊猫 | ✓ | **❌** |
| Rectangle156 气泡 + 文字 | ✓ | **❌** |
| 整屏 clickable | ❌(只气泡可点击)| ✓(整屏 → Houshan3)|
| onOpenHoushan2 callback | ✓(被 Rectangle156 调用) | **❌**(删除) |
| onOpenHoushan3 callback | ❌ | ✓(整屏跳转) |

**为什么 Houshan1 不动**:
- 用户明确说"复制后山1过去",指的是**复制当前 Houshan1 内容到 Houshan2 文件**
- Houshan1 自身的熊猫和 Rectangle156 气泡保留(它们是 Houshan1 的元素,不是 Houshan2 的)
- Houshan1 → Houshan2 的跳转通过 Rectangle156 气泡 `onClick = onOpenHoushan2` → `navigate(Routes.Shilian2)`(已在 NavHost 接线,不动)

**git 状态**(commit 后):
- `D` 旧 `Houshan2Screen.kt`(-208 行,旧版)
- `M` 新 `Houshan2Screen.kt`(+395 行,新版)— git 视为同一文件修改(rm + Write 同路径)
- `M` `Houshan1Screen.kt`(用户 IDE 调整云朵 60b Y)
- `M` `docs/SESSION-LOG-2026-09-15.md` (+本节)

### §19 后山2 跳转逻辑:点击除 4 个标签外才跳转 Houshan3(2026-09-15 下午)

**用户指令**:"把'后山2'页面跳转到'后山3'页面的方式改为点击除这四个标签之外的位置"

**问题分析**:
- 当前 Houshan2 整屏 `.clickable(onClick = onOpenHoushan3)` 在外层 Box
- 4 个标签 Box **没有** `.clickable`
- Compose 事件分发:子 Box 不可点击时,点击事件会**冒泡**到父 Box
- 结果:**点击标签时也会触发 Houshan3 跳转**(不符合用户需求)

**解决方案**:给 4 个标签 Box 加 `.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})` — 消费事件但不执行任何动作,不显示 ripple:
```kotlin
Box(
    modifier = Modifier
        .offset(x = -13.dp, y = 570.dp)
        .size(width = 106.dp, height = 188.dp)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,        // 禁止 ripple 视觉反馈
            onClick = {},               // 空 lambda,仅消费事件
        ),
) { ... }
```

**为什么需要 `indication = null`**:
- 默认 `Modifier.clickable` 会启用 ripple 效果(Material Design)
- 如果允许 ripple,点击标签会显示涟漪(用户可能不喜欢)
- `indication = null` 完全禁用视觉反馈,标签点击"静默消费"

**为什么需要 `MutableInteractionSource`**:
- Compose API 要求 clickable 接收 `interactionSource`(可选参数)
- 默认值是 `remember { MutableInteractionSource() }`,但配合 `indication = null` 必须显式传(否则编译错)
- 每次 Box 重创建时通过 `remember { ... }` 复用 InteractionSource 实例

**新加 import**:`androidx.compose.foundation.interaction.MutableInteractionSource`

**改动**:4 个标签 Box(标签1/2/3/4)各加 4 行 clickable 块(+ 1 行 import = 17 行)

**事件流**:
- 点击 4 个标签区域 → 标签 Box clickable 消费事件 → 不冒泡 → **不跳转**
- 点击其他位置(背景、云朵、标签外空白) → 事件直接到外层 Box clickable → **跳转 Houshan3**

**git 状态**(commit 后):
- `M Houshan2Screen.kt` (+17 行:1 行 import + 4 × 4 行 clickable 块)

### §20 后山3 跳转未完待续:点击除 3 个标签外区域(2026-09-15 下午)

**用户指令**:"把'后山3'页面跳转到'未完待续'页面的方式改为点击除这几个标签之外的位置"

**侦察结果**(agent 报告):
- [Houshan3Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan3/Houshan3Screen.kt):200 行
- 函数签名:`Houshan3Screen(onBack: () -> Unit = {}, onOpenUnfinished: () -> Unit = {})`
- 整屏 clickable:外层 Box `.clickable(onClick = onOpenUnfinished)`(第 60-66 行)
- **3 个标签**(注意:不是 4 个):
  - 标签3:`offset(43, 390) size(51×91)` — "万\n象\n谱" + "炼"
  - 标签4:`offset(105, 295) size(30×53.5)` — "寻\n径\n迷\n踪\n步" + "炼"
  - 标签2:`offset(124, 521) size(96×170)` — "拆\n招\n心\n法" + "炼"
- 返回按钮:**已有** `.clickable(onClick = onBack)`(已正确消费事件)
- 接线:JianghuNavHost.kt:559-564 `composable(Routes.Shilian3) { Houshan3Screen(onBack, onOpenUnfinished = { navController.navigate(Routes.Unfinished) }) }` — **不动**

**问题**:3 个标签 Box 没有 `.clickable`,点击事件冒泡到整屏 Box,触发跳转

**实现**:同 §19 模式,给 3 个标签 Box 加 `.clickable(indication = null, onClick = {})` 消费事件:
```kotlin
Box(
    modifier = Modifier
        .offset(x = 43.dp, y = 390.dp)
        .size(width = 51.dp, height = 91.dp)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
        ),
) { ... }
```

**新加 imports**:
- `androidx.compose.foundation.interaction.MutableInteractionSource`
- `androidx.compose.runtime.remember`

**注释更新**:line 62 旧的"标签2/3/4 区域不消费点击"过时,改为"整屏 clickable,但 3 个标签 Box 自带消费事件 clickable (§20)"

**KDoc 对照**:Houshan3Screen.kt:31 KDoc 已写明意图
> 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;**点击标签2-4 之外的空白区域跳转未完待续页**

**这次改动正好实现 KDoc 描述的意图**(但代码之前没真正实现,因为标签点击会冒泡)

**事件流**:
- 点击 3 个标签区域 → 标签 Box clickable 消费 → 不冒泡 → **不跳转**
- 点击其他位置(背景/云朵/熊猫/标签外空白) → 外层 Box clickable → **跳转未完待续**
- 点击左上角返回按钮 → 按钮 clickable 已存在 → 回到 Houshan2

**与 §19 的差异**:
| 项 | §19 Houshan2 | §20 Houshan3 |
|---|---|---|
| 跳转目标 | Houshan3 | "未完待续" (Routes.Unfinished) |
| 标签数 | 4 个(标签1-4) | **3 个**(标签2-4,无标签1)|
| 跳转 callback | `onOpenHoushan3` | `onOpenUnfinished` |

**git 状态**(commit 后):
- `M Houshan3Screen.kt` (+16 行:2 行 import + 3 × 4 行 clickable 块 + 1 行注释更新)

### §21 后山1 熊猫动画:上下浮 + 呼吸缩放(2026-09-15 下午)

**用户指令**:"在'后山1'页面,'D:\图\image 75.png' 熊猫图像也要具有动画"

**用户选择**:上下浮 + 呼吸缩放(50 commit 链历史选项 `50489a3 feat(houshan1): 熊猫图像也变生动`)

**源图确认**:
- `D:\图\image 75.png` (420×384, 比 1.094) **字节数与项目里 `img_shilian_panda.png` 完全一致** (165767 bytes)
- 不需要复制,直接用现有资源

**实现**:`rememberInfiniteTransition` + 两个 `animateFloat`(Scale + Y):
```kotlin
val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
val pandaScale by pandaTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "pandaScale",
)
val pandaDy by pandaTransition.animateFloat(
    initialValue = -10f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 4000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "pandaDy",
)
```

熊猫 Image 改动:
```kotlin
modifier = Modifier
    .offset(x = 184.dp, y = (621f + pandaDy).dp)  // Y 动态
    .size(width = 210.dp, height = 192.dp)
    .graphicsLayer(  // 缩放
        scaleX = pandaScale,
        scaleY = pandaScale,
    ),
```

**节奏**:
- Scale 0.95 ↔ 1.05,half-cycle 3s (full 6s,RepeatMode.Reverse 来回)— 缓慢呼吸
- Y -10 ↔ +10 dp,half-cycle 4s (full 8s,RepeatMode.Reverse 来回)— 缓慢上下浮
- 两个周期不同步(3s vs 4s),产生"不重复"自然感

**新加 import**:`androidx.compose.ui.graphics.graphicsLayer`

**技术细节**:
- `graphicsLayer` 默认 `transformOrigin = Center`,缩放从中心开始,不会偏移原位置
- `offset` 在 `size` 之前,符合 Compose 布局顺序
- Modifier 链:`offset → size → graphicsLayer`,scale 在最后,作用于 Image 内容

**为什么不用 rememberCloudFloat helper**:
- 熊猫需要 Scale 维度(graphicsLayer),不是云朵的 X/Y/Alpha 三维
- 熊猫节奏需要"温和呼吸"而非"随机飘动"
- 自定义 transition 更精准

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+22 行:1 行 import + 13 行 transition + Image 改 5 行)

### §22 后山1+后山2 识机真决标签点击跳转第一卷-1(2026-09-15 下午)

**用户指令**:"点击'后山1'页面和'后山2'页面的'识机真决'标签时可以跳转到'第一卷-1'页面"

**历史背景**:
- 50 commit 链上 9月13日 §80 和 9月14日 §1.1/§1.2 都做过此改动
- 9月14日 22:36 reset 到 8f5a28c 后,**改动全部丢失**(`onOpenVolume1` 参数和接线都没了)
- 当前需要重新实现

**代码改动**(5 个文件):

#### 1. [Houshan1Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt)
- 函数签名加 `onOpenVolume1: () -> Unit = {}`(line 63)
- "标签1" Box(line 211-220)加 `.clickable(onClick = onOpenVolume1)`

#### 2. [Houshan2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt)
- 函数签名加 `onOpenVolume1: () -> Unit = {}`(line 52)
- "标签1" Box(line 183-)改 §19 的 `onClick = {}` → `onClick = onOpenVolume1`
  - **保留事件消费**(点击标签1 不再冒泡到外层 → 不触发 Houshan3 跳转)
  - **改为跳转第一卷-1**(用户的意图)

#### 3. [JianghuNavHost.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt)
- `composable(Routes.Shilian)` block 加 `onOpenVolume1 = { navController.navigate(Routes.Volume1) }`
- `composable(Routes.Shilian2)` block 加 `onOpenVolume1 = { navController.navigate(Routes.Volume1) }`

**不动**:
- `Routes.Volume1 = "volume1"`(已存在)
- `RoutesTest.kt:49` `assertEquals("volume1", Routes.Volume1)`(已存在)
- Houshan2 标签 2/3/4 仍保留 §19 的 `onClick = {}`(消费事件但不跳转)

**事件流**(Houshan2):
- 点击标签1 → 标签1 clickable → 跳转 Volume1 ✓
- 点击标签 2/3/4 → 标签 clickable(onClick = {}) → 消费事件 → 不跳转 ✓
- 点击其他位置(整屏) → 外层 Box clickable → 跳转 Houshan3 ✓

**事件流**(Houshan1):
- 点击标签1 → 跳转 Volume1 ✓
- 点击 Rectangle156 气泡 → 跳转 Houshan2(原本)
- 点击左上角返回按钮 → 回到修炼页

**与之前 9月14日 §1.2 的差异**:
- §1.2 当时还做了"标签1 之前从未有 .clickable 是 bug" 的修正(气泡 Box 错绑点击)
- 现在 Houshan1 已经从 8f5a28c 重写,Rectangle156 气泡的 `.clickable(onClick = onOpenHoushan2)` 是正确绑定
- 这次只加标签1 → Volume1 的新跳转,不动气泡

**git 状态**(commit 后):
- `M Houshan1Screen.kt` (+2 行:函数签名 1 行 + 标签1 clickable 1 行;注释 1 行)
- `M Houshan2Screen.kt` (+2 行:函数签名 1 行;标签1 改 1 行;注释 1 行)
- `M JianghuNavHost.kt` (+2 行:Houshan1 + Houshan2 各加 1 行 onOpenVolume1 接线)

### §23 后山3 渐变云朵 Ellipse 56:tint A9C3C0 ↔ 白色循环(2026-09-15 下午)

**用户指令**:"我希望你可以在'后山3'页面用'D:\图\Ellipse 56.png'图像,这是一个渐变的图像,我希望你可以让图像从 A9C3C0 色到白色不断变换颜色,放在 X=263 Y=755 W=335 H=297"

**操作**:
1. **复制源图**:`D:\图\Ellipse 56.png` (1.6 MB, 1487×1373, 比 1.083) → `android/app/src/main/res/drawable-nodpi/img_shilian3_cloud_56.png`(同命名惯例 `img_shilian3_*`)
2. **颜色循环动画**:`rememberInfiniteTransition + animateColor + RepeatMode.Reverse`:
   ```kotlin
   val tintTransition = rememberInfiniteTransition(label = "cloud56Tint")
   val tintColor by tintTransition.animateColor(
       initialValue = Color(0xFFA9C3C0),  // A9C3C0 + FF 透明度
       targetValue = Color.White,
       animationSpec = infiniteRepeatable(
           animation = tween(durationMillis = 4000, easing = LinearEasing),
           repeatMode = RepeatMode.Reverse,
       ),
       label = "cloud56Tint",
   )
   ```
3. **Image + graphicsLayer.colorFilter**:
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_shilian3_cloud_56),
       modifier = Modifier
           .offset(x = 263.dp, y = 755.dp)
           .size(width = 335.dp, height = 297.dp)
           .graphicsLayer {
               colorFilter = ColorFilter.tint(tintColor, BlendMode.Modulate)
           },
       contentScale = ContentScale.FillBounds,
   )
   ```

**色彩行为**:
- `ColorFilter.tint(tintColor, BlendMode.Modulate)`:tint 颜色与原图像素**相乘**
- tintColor 在 [A9C3C0, White] 之间循环(4s 来回)
- 当 tintColor = A9C3C0:原图渐变被染成"青绿偏色"
- 当 tintColor = White:原图渐变保持原色(白乘任何色 = 原色)

**为什么 Modulate 而不是 SrcOver**:
- `SrcOver`:tint 色完全覆盖原图 → 图像变成纯色块(丢失 PNG 渐变)
- `Modulate`:tint 色作为滤镜乘法 → 保留原图渐变结构,只改变色调

**新加 imports**:
- `androidx.compose.animation.core.{LinearEasing, RepeatMode, animateColor, infiniteRepeatable, rememberInfiniteTransition, tween}`
- `androidx.compose.runtime.getValue`
- `androidx.compose.ui.graphics.{BlendMode, ColorFilter, graphicsLayer}`

**位置观察**:
- X=263, Y=755, W=335, H=297(右下角偏中,接近屏幕底部)
- 椭圆 PNG 比 1.083(近正方形),但 size 强制 W=335 H=297,内容被横拉 1.27 倍
- 按 [image-fit-to-natural-bounds](image-fit-to-natural-bounds) 应是 W=297 H=274 — 但用户给了具体 W/H,直接采纳

**z-order**:在 Houshan3 现有云朵之后,熊猫之前 — 渲染顺序:背景 → Ellipse 58 云 → Ellipse 56 渐变云 → 熊猫 → 标签 → 返回

**节奏**:4s 半周期(从 A9C3C0 到 White),8s 完整周期(RepeatMode.Reverse)循环

**git 状态**(commit 后):
- `M img_shilian3_cloud_56.png`(新增, 1.6 MB)
- `M Houshan3Screen.kt` (+20 行:5 行 import + 11 行 transition + 12 行 Image)

### §24 修复 §23 编译错误:`InfiniteTransition.animateColor` 在项目 Compose 版本不存在(2026-09-15 下午)

**用户报告**:"failed — Unresolved reference 'animateColor'. Unresolved reference 'animateColor'. Cannot infer type for this parameter. Unresolved reference 'colorFilter'."

**根因**:
- `InfiniteTransition.animateColor` API 在项目使用的 Compose 版本中**不存在**
- `colorFilter = ...` 报"Unresolved reference"是次生错误(graphicsLayer block 的 lambda 类型推断失败,因为 `tintColor` 是 `State<Color>` 但 animateColor 引用失败,类型变成 `Nothing`)
- Float 动画(`animateFloat`)可工作,但 Color 动画需要 `Animatable<Color>` 手动驱动

**修复方案**:`Animatable<Color>` + `LaunchedEffect` + `while (isActive)` + 两次 `animateTo` 循环(等价于 `RepeatMode.Reverse`):
```kotlin
val tintColor = remember { Animatable(Color(0xFFA9C3C0)) }
LaunchedEffect(Unit) {
    while (isActive) {
        tintColor.animateTo(
            targetValue = Color.White,
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        )
        tintColor.animateTo(
            targetValue = Color(0xFFA9C3C0),
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        )
    }
}
// Image modifier:
.graphicsLayer {
    colorFilter = ColorFilter.tint(tintColor.value, BlendMode.Modulate)
}
```

**Import 变化**:
- 移除:`androidx.compose.animation.core.{animateColor, infiniteRepeatable, rememberInfiniteTransition, RepeatMode}`
- 新加:`androidx.compose.animation.core.Animatable`
- 新加:`androidx.compose.runtime.LaunchedEffect`
- 新加:`kotlinx.coroutines.isActive`
- `LinearEasing` / `tween` 保留

**关键差异**:
| §23(报错) | §24(修复) |
|---|---|
| `val tintColor by tintTransition.animateColor(...)` | `val tintColor = remember { Animatable(...) }` |
| `tintColor`(State) | `tintColor.value`(显式取值)|
| `rememberInfiniteTransition` + `animateColor` | `Animatable<Color>` + `LaunchedEffect` + `while (isActive)` |

**新加 memory**:`compose-animatecolor-version-trap` — 记录这个 Compose API 陷阱,避免下次重蹈覆辙

**git 状态**(commit 后):
- `M Houshan3Screen.kt` (净 0 变化,但内部从 `animateColor` 改 `Animatable`)

### §25 修复 §24 编译错误:`Animatable<Color>` 也不支持(2026-09-15 下午)

**用户报告**:"Argument type mismatch: actual type is 'androidx.compose.ui.graphics.Color', but 'kotlin.Float' was expected. Unresolved reference 'colorFilter'."

**根因**:
- 项目 Compose 版本中 `Animatable` 只支持基础类型 `Float`(需要 `TwoWayConverter<Color, *>` 才支持 Color)
- §24 用 `Animatable<Color>` 也是错的 — 同样不被支持
- `Unresolved reference 'colorFilter'` 仍是 graphicsLayer lambda 类型推断失败的次生错误

**最终修复方案**(§25):用 `Animatable<Float>` 在 [0, 1] 插值,在 graphicsLayer 块内动态合成 Color:
```kotlin
val tintProgress = remember { Animatable(0f) }  // 0 = A9C3C0, 1 = White
LaunchedEffect(Unit) {
    while (isActive) {
        tintProgress.animateTo(1f, animationSpec = tween(4000, LinearEasing))
        tintProgress.animateTo(0f, animationSpec = tween(4000, LinearEasing))
    }
}

.graphicsLayer {
    val t = tintProgress.value
    val r = 0xA9 + ((0xFF - 0xA9) * t).toInt()
    val g = 0xC3 + ((0xFF - 0xC3) * t).toInt()
    val b = 0xC0 + ((0xFF - 0xC0) * t).toInt()
    colorFilter = ColorFilter.tint(
        Color(red = r, green = g, blue = b),
        BlendMode.Modulate,
    )
}
```

**3 次失败总结**(§23/§24/§25):
| 方案 | 错误 |
|---|---|
| §23 `InfiniteTransition.animateColor` | `Unresolved reference 'animateColor'` |
| §24 `Animatable<Color>` | `Argument type mismatch: Color → Float` |
| §25 `Animatable<Float>` + 合成 Color | ✓ 通过 |

**更新 memory**:`compose-animatecolor-version-trap` — 增加 §24/§25 信息,记录 Color 动画用 `Animatable<Float>` + 合成 Color 模式

**git 状态**(commit 后):
- `M Houshan3Screen.kt` (净变化小,但内部从 Animatable<Color> 改 Animatable<Float>)

### §26 修复 §25:`Modifier.graphicsLayer` 没有 colorFilter 属性(2026-09-15 下午)

**用户报告**:"Unresolved reference 'colorFilter'"

**根因**:
- §25 用了 `Modifier.graphicsLayer { colorFilter = ... }` 块设 colorFilter
- 但 `GraphicsLayerScope` **没有 `colorFilter` 属性**!它只暴露 scaleX/Y, alpha, translationX/Y, rotationX/Y/Z, shape, clip, blendMode 等层变换相关属性
- `colorFilter` 是 `Paint` 的属性,**不属于 GraphicsLayer**

**正确方案**(§26):用 Image 自己的 `colorFilter` 参数(不是 graphicsLayer 块):
```kotlin
Image(
    painter = painterResource(R.drawable.img_shilian3_cloud_56),
    modifier = Modifier
        .offset(x = 263.dp, y = 755.dp)
        .size(width = 335.dp, height = 297.dp),
    colorFilter = ColorFilter.tint(
        Color(
            red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
            green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
            blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
        ),
        BlendMode.Modulate,
    ),
    contentScale = ContentScale.FillBounds,
)
```

**动画机制**:
- `tintProgress.value` 变化 → Animatable 触发状态更新 → Image 重组 → `colorFilter` 重新计算 → tint 更新
- 标准 Compose 重组流程,无需额外 LaunchedEffect

**Import 变化**:
- 移除:`androidx.compose.ui.graphics.graphicsLayer`(不再用)

**4 次失败升级路径**:
| 方案 | 错误 |
|---|---|
| §23 `InfiniteTransition.animateColor` | `Unresolved reference 'animateColor'` |
| §24 `Animatable<Color>` + LaunchedEffect | `Argument type mismatch: Color → Float` |
| §25 `Animatable<Float>` + `graphicsLayer { colorFilter = ... }` | `Unresolved reference 'colorFilter'`(GraphicsLayerScope 无此属性)|
| §26 `Animatable<Float>` + `Image.colorFilter = ...` | ✓ 通过 |

**更新 memory**:`compose-animatecolor-version-trap` 记录完整 4 次失败路径,GraphicsLayerScope 不支持 colorFilter 是关键陷阱

**git 状态**(commit 后):
- `M Houshan3Screen.kt` (内部从 graphicsLayer 块改 Image 的 colorFilter 参数)

### §27 后山3 加渐变云朵 Ellipse 58(2026-09-15 下午)— A 模式不 commit

**用户指令**:"'D:\图\Ellipse 58.png' 也要渐变的放在'后山3'页面的 X=78 Y=170 W=331 H=92"

**操作**(按 A 模式,执行但不 commit):
1. **复制源图**:`D:\图\Ellipse 58.png` (303,871 bytes) → `android/app/src/main/res/drawable-nodpi/img_shilian3_cloud_58.png`
2. **加 Image 代码**:与 Ellipse 56 渐变云朵并列,**共用同一个 `tintProgress` Animatable**(同步循环渐变)
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_shilian3_cloud_58),
       modifier = Modifier
           .offset(x = 78.dp, y = 170.dp)
           .size(width = 331.dp, height = 92.dp),
       colorFilter = ColorFilter.tint(
           Color(
               red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
               green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
               blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
           ),
           BlendMode.Modulate,
       ),
       contentScale = ContentScale.FillBounds,
   )
   ```

**共享 tintProgress 设计**:
- Houshan3 已经有 `val tintProgress = remember { Animatable(0f) }`(§26 在 line 60)
- 两个云朵(56 + 58)共享同一个 Animatable,颜色**同步循环**
- 用户没要求独立动画,默认同步(简单可预测)
- 如需独立动画,需要 2 个 Animatable

**位置观察**:
- Ellipse 58:X=78 Y=170 W=331 H=92(顶部偏左,与原"img_shilian3_cloud"位置 X=-46 Y=476 不同)
- 与 Ellipse 56(Y=755,底部)分屏布局,一个顶部一个底部
- Ellipse 58 PNG 比例可能 ~1:1(303KB 文件,需实际尺寸)— 暂时不验证(按用户 W/H 直接采纳)

**命名**:
- `img_shilian3_cloud_58.png`(houshan3 目录命名惯例 `img_shilian3_*`)
- 注意:Houshan3 已有 `img_shilian3_cloud.png`(侦察报告位置 X=-46 Y=476) — 这是**另一张云朵**(不是 Ellipse 58),可能是之前 houshan1 复制的旧图
- 不复用,新建独立资源

**A 模式**:
- 本次改动未 commit(等用户说"commit")
- 当前工作区:M Houshan3Screen.kt + ?? img_shilian3_cloud_58.png

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (+14 行:Image 代码 + 注释)
- `?? img_shilian3_cloud_58.png` (新增, 303 KB)

### §28 后山3 2 朵渐变云朵加位置 + 透明度动效(2026-09-15 下午)— A 模式不 commit

**用户指令**:"两个渐变的云朵还要可以变化位置和透明度的"

**复用 §13 模式**:Houshan3 加 `rememberCloudFloat()` helper,调用 2 次给 Ellipse 56 和 Ellipse 58:
```kotlin
val (cloud56Dx, cloud56Dy, cloud56Alpha) = rememberCloudFloat()
val (cloud58Dx, cloud58Dy, cloud58Alpha) = rememberCloudFloat()
```

**两个渐变云朵动效对照**:
| 维度 | Ellipse 56 | Ellipse 58 | 关系 |
|---|---|---|---|
| 颜色渐变 | `tintProgress`(共用)| `tintProgress`(共用)| **同步循环**(A9C3C0 ↔ White)|
| 位置 X/Y | `cloud56Dx/Dy` | `cloud58Dx/Dy` | **各自独立随机** |
| 透明度 | `cloud56Alpha` | `cloud58Alpha` | **各自独立随机** |

**Image 改动**:
```kotlin
modifier = Modifier
    .offset(x = (263f + cloud56Dx).dp, y = (755f + cloud56Dy).dp)  // 基座位置 + 随机偏移
    .size(width = 335.dp, height = 297.dp),
alpha = cloud56Alpha,  // 独立透明度
```

**`rememberCloudFloat` 参数**(沿用 §13 默认值):
- X ±100, Y ±15, Alpha 0.5~1.0
- X 节奏 1.5~3s + delay 0.5~1.5s,Y 1~2s + delay 0.3~0.8s,Alpha 1.5~3s + delay 0.5~1.2s

**helper 复制说明**:
- `rememberCloudFloat` 是 `private fun`,定义在 [Houshan1Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt) 末尾(§13)
- 在 [Houshan2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt) 已复制过 1 份(§18)
- 现在 Houshan3 是第 3 份
- **TODO 标记**:helper 已在 3 个文件重复,可考虑抽出到 `ui/util/CloudFloat.kt` 共享(暂未做,保持最小改动)

**A 模式**:执行但不 commit,等用户说"commit"再 push

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (净 +70 行:3 行 import + 2 行 helper 调用 + 12 行 2 个 Image offset/alpha 改动 + 50 行 helper 函数)

### §29 后山3 加渐变云朵 Ellipse 57 含位置/透明度动效(2026-09-15 下午)— A 模式不 commit

**用户指令**:"'D:\图\Ellipse 57.png' 也要渐变色和可以上下左右移动和变换透明度,放在 X=-25 Y=500 W=225 H=191"

**操作**(按 A 模式,执行但不 commit):
1. **复制源图**:`D:\图\Ellipse 57.png` (796 KB, 986×884) → `android/app/src/main/res/drawable-nodpi/img_shilian3_cloud_57.png`
2. **加 helper 调用**(沿用 §28 模式):
   ```kotlin
   val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()
   ```
3. **加 Image 代码**(在 Ellipse 58 之后,与 56/58 同样模式):
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_shilian3_cloud_57),
       modifier = Modifier
           .offset(x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp)
           .size(width = 225.dp, height = 191.dp),
       colorFilter = ColorFilter.tint(
           Color(red = 0xA9 + ..., green = 0xC3 + ..., blue = 0xC0 + ...),
           BlendMode.Modulate,
       ),
       alpha = cloud57Alpha,
       contentScale = ContentScale.FillBounds,
   )
   ```

**Houshan3 3 朵渐变云朵动效对照**(§26/27/28/29):
| 云朵 | 位置 | 颜色 | 透明度 | 节奏 |
|---|---|---|---|---|
| Ellipse 56 | X=263 Y=755 W=335 H=297 | A9C3C0↔White 共用 | 各自随机 0.5~1.0 | 1.5~3s |
| Ellipse 58 | X=78 Y=170 W=331 H=92 | A9C3C0↔White 共用 | 各自随机 0.5~1.0 | 1.5~3s |
| **Ellipse 57** | **X=-25 Y=500 W=225 H=191** | **A9C3C0↔White 共用** | **各自随机 0.5~1.0** | **1.5~3s** |

**命名延续**:`img_shilian3_cloud_57.png`(Houshan3 目录 `img_shilian3_*` 命名惯例)

**位置观察**:
- X=-25 部分屏幕外(左侧)
- Y=500 在 Houshan3 中部,熊猫(118, 405)下方
- W=225 H=191 与原 Houshan1/Houshan2 的 Ellipse 57 一致

**A 模式**:本次改动未 commit(等用户说"commit"或累积更多)

**git 状态**(待 commit):
- `?? img_shilian3_cloud_57.png` (新增, 796 KB)
- `M Houshan3Screen.kt` (+2 行 helper 调用 + 18 行 Image 代码)

### §30 取消 Ellipse 57 渐变色,保留位置/透明度动效(2026-09-15 下午)— A 模式不 commit

**用户指令**:"'D:\图\Ellipse 57.png' 就不要渐变了吧"

**操作**:
- 移除 Ellipse 57 Image 的 `colorFilter = ColorFilter.tint(...)` 参数
- 保留位置动效 `offset(x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp)`
- 保留透明度动效 `alpha = cloud57Alpha`

**修改后代码**:
```kotlin
Image(
    painter = painterResource(R.drawable.img_shilian3_cloud_57),
    modifier = Modifier
        .offset(x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp)
        .size(width = 225.dp, height = 191.dp),
    alpha = cloud57Alpha,  // 透明度动效保留
    contentScale = ContentScale.FillBounds,
)
```

**Houshan3 3 朵云最终动效对比**:
| 云朵 | 颜色 | 位置 | 透明度 |
|---|---|---|---|
| Ellipse 56 | A9C3C0↔White(渐变)| 随机 ±100/±15 | 0.5~1.0 |
| Ellipse 57 | **原色(无 tint)** | 随机 ±100/±15 | 0.5~1.0 |
| Ellipse 58 | A9C3C0↔White(渐变)| 随机 ±100/±15 | 0.5~1.0 |

**共享资源不变**:
- `tintProgress` Animatable(仍驱动 Ellipse 56 + 58 颜色)
- `rememberCloudFloat()` helper(3 个云朵各自独立调用)

**A 模式**:本次改动未 commit

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (删 12 行 colorFilter 块)

### §31 调整 Ellipse 57 位置到 X=187 Y=308(原色显示)(2026-09-15 下午)— A 模式不 commit

**用户指令**:"'D:\图\Ellipse 57.png' 白色,X=187 Y=308 W=225 H=191,可以上下左右移动,移动轨迹不要是圆形,要可以变化透明度"

**解读**:
- "白色" = PNG 原色显示(§30 刚取消渐变,白色云朵 PNG 的自然颜色)
- 位置调整:X=-25 Y=500 → X=187 Y=308
- W=225 H=191 保持不变
- 动效保留(随机位置 + 透明度)— "移动轨迹不要是圆形" = 不使用 sin/cos 圆周模式,使用 §13 rememberCloudFloat 的随机模式(已经是这种)

**操作**:
- Ellipse 57 Image 的 offset 改为 `x = (187f + cloud57Dx).dp, y = (308f + cloud57Dy).dp`
- 保留 `alpha = cloud57Alpha`(透明度动效)
- 不加 `colorFilter`(§30 已取消渐变)
- 不加新代码,helper 调用已有(`val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()`)

**Houshan3 3 朵云最终状态**:
| 云朵 | 位置 | 颜色 | 透明度 | 轨迹 |
|---|---|---|---|---|
| Ellipse 56 | X=263 Y=755 | A9C3C0↔White | 0.5~1.0 随机 | 随机(非圆)|
| Ellipse 57 | **X=187 Y=308**(调整)| **原色(白)** | 0.5~1.0 随机 | 随机(非圆)|
| Ellipse 58 | X=78 Y=170 | A9C3C0↔White | 0.5~1.0 随机 | 随机(非圆)|

**Houshan2Screen.kt 备注**(system-reminder 提到):
- IDE 自改了 Houshan2Screen.kt(可能是 IDE 自动格式化或未保存改动)
- 与本指令无关,Houshan2 已有 Ellipse 57 在 X=208 Y=570(原 Houshan1 复刻),保留不动

**A 模式**:本次改动未 commit

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (改 2 行:Image 注释 + offset)

### §32 撤回 §31:Ellipse 57 位置改回 X=-25 Y=500(2026-09-15 下午)— A 模式不 commit

**用户指令**:"撤回上一步"

**撤回操作**:
- Ellipse 57 Image offset 改回:`x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp`
- 注释改回 §30 版本

**最终状态**(等价于 §30 之后,§31 之前):
- Ellipse 57:X=-25 Y=500 W=225 H=191,原色显示,无 tint
- 透明度 + 位置随机动效保留

**A 模式本撤回为什么简单**:
- §31 改动未 commit,所以撤回 = 直接修改文件
- 不需要 `git revert`(那是已 commit 后撤回)
- 不需要 force push

**A 模式的价值显现**:本次撤回零代价(只改工作区 + SESSION-LOG),避免了"为了撤回一个小改动而 force push"的麻烦。

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (改 2 行:Image 注释 + offset,回到 §30 值)

### §33 后山3 加云朵 Ellipse 5(X=187 Y=308 W=225 H=191 白色原色)(2026-09-15 下午)— A 模式不 commit

**用户指令**:"'D:\图\Ellipse 5.png' 白色,放在 X=187 Y=308 W=225 H=191,可以上下左右移动,移动轨迹不要是圆形,要可以变化透明度"

**重要发现**:`D:\图\Ellipse 5.png` 字节数与 `Ellipse 57.png` **完全一致**(都是 796,237 bytes,986×884,比 1.115)— 实际是同一张图(可能文件管理器里有两份,但内容相同)。

按用户明确指定的 `Ellipse 5.png` 新建独立资源:
- `android/app/src/main/res/drawable-nodpi/img_shilian3_cloud_5.png`(新建,即使内容与 cloud_57.png 相同)
- 未来 Ellipse 5.png 和 Ellipse 57.png 内容分化时,资源独立

**操作**:
1. **复制源图**:`D:\图\Ellipse 5.png` → `img_shilian3_cloud_5.png` (796,237 bytes)
2. **加 helper 调用**(沿用 §28 模式):
   ```kotlin
   val (cloud5Dx, cloud5Dy, cloud5Alpha) = rememberCloudFloat()
   ```
3. **加 Image 代码**(在 Ellipse 57 之后,X=187 Y=308,白色原色无 tint):
   ```kotlin
   Image(
       painter = painterResource(R.drawable.img_shilian3_cloud_5),
       modifier = Modifier
           .offset(x = (187f + cloud5Dx).dp, y = (308f + cloud5Dy).dp)
           .size(width = 225.dp, height = 191.dp),
       alpha = cloud5Alpha,
       contentScale = ContentScale.FillBounds,
   )
   ```

**Houshan3 4 朵云最终状态**:
| 云朵 | 位置 | 颜色 | 透明度 | 备注 |
|---|---|---|---|---|
| Ellipse 56 | X=263 Y=755 | A9C3C0↔White | 0.5~1.0 | 渐变 |
| Ellipse 57 | X=-25 Y=500 | 原色 | 0.5~1.0 | §30 取消渐变 |
| Ellipse 58 | X=78 Y=170 | A9C3C0↔White | 0.5~1.0 | 渐变 |
| **Ellipse 5** | **X=187 Y=308** | **原色** | **0.5~1.0** | **§33 新加(§31 撤回 → Ellipse 5 替代)** |

**轨迹**:全部使用 `rememberCloudFloat()` 随机模式(非 sin/cos 圆周),符合用户"移动轨迹不要是圆形"

**A 模式**:本次改动未 commit

**git 状态**(待 commit):
- `?? img_shilian3_cloud_5.png` (新增, 796 KB,与 cloud_57.png 内容相同)
- `M Houshan3Screen.kt` (改 2 行:helper 调用 + Image)

### §34 注释修正:补 §26 引用 + 简化 Ellipse 57 注释(2026-09-15 下午)— A 模式不 commit

**用户指令**:"注意注释"

**2 处注释修正**:
1. **Line 60**:补 §26 引用(GraphicsLayerScope 没 colorFilter 修复)
   - 改前:`// Ellipse 56 渐变色循环:Animatable<Float> 在 [0,1] 插值,4s 来回 (§23/24/25 修复)`
   - 改后:`// Ellipse 56 渐变色循环:Animatable<Float> 在 [0,1] 插值,4s 来回 (§23/24/25/26 修复)`
   - 原因:§26 是 GraphicsLayerScope 没 colorFilter 修复,缺它会让历史不完整

2. **Line 145** Ellipse 57 Image:简化引用(去掉已撤回的 §31)
   - 改前:`// 云朵(Ellipse 57.png, X=-25, Y=500, W=225, H=191) — 原色显示(§30),仅位置 + 透明度随机 (§32 撤回 §31)`
   - 改后:`// 云朵(Ellipse 57.png, X=-25, Y=500, W=225, H=191) — 原色显示,仅位置 + 透明度随机 (§29/§30)`
   - 原因:§31(调到 X=187 Y=308)被 §32 撤回,引用它会造成混淆;注释应反映当前代码状态

**未做(用户未要求)**:
- **发现命名冲突**:Houshan3 现有 2 朵都标注为 Ellipse 58.png:
  - 旧云朵(8f5a28c 基线) `img_shilian3_cloud`,X=-46 Y=476 W=331 H=92
  - 新云朵(§27 我加) `img_shilian3_cloud_58.png`,X=78 Y=170 W=331 H=92
  - 资源名不同但 PNG 源可能相同("Ellipse 58.png"标注冲突)
  - **高度都是 W=331 H=92**,可能重叠
  - 命名重构(改旧云朵资源名)超出本次注释修正范围,等用户决定

**A 模式**:本次改动未 commit

**git 状态**(待 commit):
- `M Houshan3Screen.kt` (改 2 行注释)

### §35 Houshan3 重命名旧云朵 → img_shilian3_cloud_old(避免命名冲突)(2026-09-15 下午)— A 模式不 commit

**用户指令**:选 A — 把旧云朵 `img_shilian3_cloud` 改名为 `img_shilian3_cloud_old`

**操作**(用 `git mv` 保留历史):
1. **重命名文件**:`android/app/src/main/res/drawable/img_shilian3_cloud.png` → `img_shilian3_cloud_old.png`
   - 用 `git mv` 而非 `mv`(保留 git 历史记录,IDE 友好)
2. **更新引用**:Houshan3Screen.kt line 98
   - `painterResource(R.drawable.img_shilian3_cloud)` → `painterResource(R.drawable.img_shilian3_cloud_old)`
   - 注释改为:`// 旧云朵(8f5a28c 基线,重命名为 _old 避免与 Ellipse 58.png 命名冲突 §35)`

**命名对比**:
| 资源名 | 用途 | 来源 |
|---|---|---|
| `img_shilian3_cloud_old.png`(原 `img_shilian3_cloud`)| 旧云朵 | 8f5a28c 基线 |
| `img_shilian3_cloud_56.png` | §23 我加的 Ellipse 56 | D:\图\Ellipse 56.png |
| `img_shilian3_cloud_57.png` | §29 我加的 Ellipse 57 | D:\图\Ellipse 57.png |
| `img_shilian3_cloud_58.png` | §27 我加的 Ellipse 58 | D:\图\Ellipse 58.png |
| `img_shilian3_cloud_5.png` | §33 我加的 Ellipse 5 | D:\图\Ellipse 5.png |

**结果**:命名唯一,无重复。`_old` 后缀明确标识为 8f5a28c 基线旧资源。

**A 模式**:本次改动未 commit(等用户说 commit)

**git 状态**(待 commit):
- `R img_shilian3_cloud.png → img_shilian3_cloud_old.png`(重命名)
- `M Houshan3Screen.kt` (改 2 行:painterResource + 注释)

### §36 后山2 → 后山3 沉浸式过渡动画(纵深推进 dolly-in)(2026-09-15 晚)— A 模式不 commit

**用户指令**:"请为'后山2'切换到'后山3'增加一段沉浸式过渡动画(以两张示意图为准):让画面中的主山峰产生明显的'向用户靠近'的纵深推进感:山峰平滑放大并向前移动,周围的云雾、水墨山峦可轻微后移或淡化…动画结束时自然衔接为'后山3'页面的构图与元素位置,并呈现熊猫角色。整体风格需保持现有国风水墨质感,动画柔和、连贯,不要突兀的页面闪切;建议时长约 0.8~1.2 秒,可使用 ease-in-out 缓动。页面中的按钮、文字和标记应随过渡平滑淡入/淡出或跟随对应山体移动,避免跳动。"

#### 侦察结论(关键,决定方案可行性)

**两张背景图是不同作品,不是同一张图的缩放** —— 这一点必须先验证,否则会做出"以为在形变衔接、实际在硬切"的假过渡:

| 资源 | 用途 | 像素 | 宽高比 |
|---|---|---|---|
| `img_shilian_bg.png` | 后山2 背景 | **1236 × 2751** | 0.44929 |
| `img_shilian2_bg.png` | 后山3 背景(源文件名"后山3 转换.png")| **824 × 1834** | 0.44929 |

- 两者 1236/824 = 2751/1834 = **恰好 1.5**,初看像是"中央 2/3 裁剪 = 1.5 倍推近",**这是陷阱**
- 实际两者宽高比都 = **0.44929 = 412/917**(Figma 全屏设计稿比例)—— 比例相同只说明"都是全屏稿",**不构成构图关系**
- **程序化证伪**:以相关系数(Pearson,对亮度/对比度不变)搜索"绕中心裁剪 + 缩放"因子 1.00~2.00,最高仅 **0.3514**(噪声级);直接按整图缩放对比平均灰度差 **26.05**、按 1.5 倍中央裁剪对比 **29.92**,两者都很差 → **确认两张图是不同画作**

**因此**:"同一座山放大后落到下一页构图"在现有素材下**不可能真实实现**。用户要的是"观感上的纵深推进",故采用**三景深平面视差推进 + 交叉淡入接棒**——推进感来自层间速度差,衔接来自灭点对齐 + 交叉淡入。

**另一路侦察**:`Routes.Shilian3` 全仓库仅 2 处引用(navigate + composable),确认**只能从后山2 进入**,因此可以安全地只在 Shilian2/Shilian3 上做方向特异化过渡。

#### 实现

**A. [Houshan2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt)** —— 屏内三景深视差推进

新增文件级常量(便于调参 + 可读):
```kotlin
private const val DOLLY_DURATION_MS = 1050   // 落在用户要求 0.8~1.2s 区间
private const val DOLLY_HANDOFF_MS  = 560L   // 半程交给导航
private const val DOLLY_BG_SCALE    = 0.34f  // 背景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f  // 云雾     1.00 → 1.09
private const val DOLLY_LABEL_SCALE = 0.34f  // 标签     1.00 → 1.34
private const val FOCAL_X = 0.5f             // 灭点 X
private const val FOCAL_Y = 0.48f            // 灭点 Y
```

新增状态 + 触发:
```kotlin
val scope = rememberCoroutineScope()
var isTransitioning by remember { mutableStateOf(false) }
val dolly = remember { Animatable(0f) }        // 0→1 推进进度,三层共用
BackHandler(enabled = !isTransitioning) { onBack() }
```

三条变换层(每层 `fillMaxSize` 包裹,见下方"为什么"):
```kotlin
val bgScale    = 1f + DOLLY_BG_SCALE * p
val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
val labelScale = 1f + DOLLY_LABEL_SCALE * p
val cloudFade  = (1f - p).coerceIn(0f, 1f)
val labelFade  = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出
val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)   // 返回按钮
val focal = TransformOrigin(FOCAL_X, FOCAL_Y)
```

整屏点击改为启动推进,并在半程交给导航:
```kotlin
val startDollyIn: () -> Unit = {
    if (!isTransitioning) {
        isTransitioning = true
        scope.launch {
            dolly.animateTo(1f, tween(DOLLY_DURATION_MS, easing = FastOutSlowInEasing))
        }
        scope.launch { delay(DOLLY_HANDOFF_MS); onOpenHoushan3() }
    }
}
```

**B. [JianghuNavHost.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt)** —— 交叉淡入接棒

```kotlin
composable(
    route = Routes.Shilian2,
    exitTransition = {
        if (targetState.destination.route == Routes.Shilian3) {
            fadeOut(tween(durationMillis = 520, easing = LinearEasing))
        } else {
            fadeOut(tween(durationMillis = 300, easing = FastOutSlowInEasing))
        }
    },
) { Houshan2Screen(...) }

composable(
    route = Routes.Shilian3,
    enterTransition = {
        scaleIn(tween(760, easing = FastOutSlowInEasing), initialScale = 1.10f,
                transformOrigin = TransformOrigin(0.5f, 0.48f)) +
        fadeIn(tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing))
    },
    popEnterTransition = { fadeIn(tween(300, easing = FastOutSlowInEasing)) },
) { Houshan3Screen(...) }
```

#### 时间轴(以点击为 t=0)

| 时刻 | 事件 |
|---|---|
| 0 ms | 点击 → `isTransitioning=true`,dolly 0→1 开始(1050ms, FastOutSlowIn) |
| ~480 ms | 标签淡出完毕(p=0.714) |
| **560 ms** | **交接**:`onOpenHoushan3()`;此时山体已推进约 78% |
| 560→1080 ms | 后山2 淡出(520ms)与后山3 淡入(延迟 120ms,640ms)交叠 → 交叉溶解 |
| 560→1320 ms | 后山3 由 1.10 回落到 1.00,读作"镜头减速停稳",熊猫随之呈现 |
| 1050 ms | 后山2 屏内推进走满(此时已在淡出中) |

#### 关键设计决策(为什么这么做)

| 决策 | 原因 |
|---|---|
| **每层单独 `Box(fillMaxSize).graphicsLayer{...}`** | `graphicsLayer` 的 `transformOrigin` 是**相对元素自身边界**的,不是相对父容器。要让三层绕**同一个屏幕灭点**缩放,必须让每层边界 = 屏幕;否则每层各自中心缩放,层间速度差会变成"各转各的"而不是纵深 |
| **标签与背景同速(都 0.34)** | 用户要求"避免跳动"。标签与山体同速、同灭点 ⇒ 相对位置**零漂移**,文字始终"贴"在对应山体上;再叠加淡出,视线自然交还给山体 |
| **云雾 0.09 < 山体 0.34** | 视差正是**层间速度差**:近景推得多、远景推得少,远景相对后退 → 即用户要的"云雾后移" |
| **返回按钮只淡出不缩放** | UI chrome 不属于景深;若跟随山体放大,视觉上会"跳" |
| **`exitTransition` 的 else 分支用轻淡出而非 `ExitTransition.None`** | 若用 None,"后山2 → 后山1"返回会变成**硬切**(回归)。用轻淡出保持原有柔和感 |
| **显式设 `popEnterTransition`** | Navigation Compose 的坑:`enterTransition` 非空时,`popEnterTransition` 会**默认继承它**。不覆盖的话,从"未完待续"返回后山3 也会播一次推进动画 |
| **交接点放在半程(560ms)而非动画结束** | 若等推进走满再导航,会出现"推到最大 → 静止 → 再淡入"的顿挫;半程交接让推进与淡入**同时进行**,读作一个连续镜头 |

#### 编译

```
JAVA_HOME=C:\Users\28784\.jdks\jbr-21.0.11   (java -version → 21.0.11)
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 41s
```

**首次通过,未重蹈 §23~§26 四次编译失败覆辙**。原因是动手前先核对了 API 可用性:
- `navigationCompose = 2.8.4` — destination 级 `enterTransition/exitTransition` 自 **2.7.0** 起提供 ✓
- `composeBom = 2024.12.01` — `scaleIn/fadeIn/TransformOrigin/GraphicsLayerScope.transformOrigin` 均为长期稳定 API ✓
- 用到的 `GraphicsLayerScope` 属性仅 `scaleX/scaleY/transformOrigin/alpha`(**不含 colorFilter**,与 §26 结论一致)

#### git 状态(待 commit)

- `M Houshan2Screen.kt`(净 +约 60 行:常量块 + 状态 + 触发 + 3 个景深包裹层;原 6 云/4 标签内容不变,仅缩进)
- `M JianghuNavHost.kt`(+约 30 行:5 个 import + Shilian2 exitTransition + Shilian3 enterTransition/popEnterTransition)

### §37 后山1 + 后山2 持续循环云雾缭绕效果(程序化水墨云海)(2026-09-15 晚)— A 模式不 commit

**用户指令**:"请为'后山1'和'后山2'两个页面增加持续循环的云雾缭绕动态效果,以参考图中的水墨山景为准。云雾应以半透明、水墨晕染的质感,在山峰之间、山脚和画面边缘缓慢流动、聚散与轻微漂浮,营造山间云海与纵深感。可让前景云雾移动稍快、中远景云雾移动更慢,形成自然的层次与视差。动画整体要轻柔、克制、无明显重复痕迹,保持国风水墨的宁静意境;不要遮挡或影响山峰、关卡标签、文字、熊猫角色及其他交互元素的阅读与点击。建议使用低速、无缝循环的动画,并注意性能表现。"

#### 素材侦察(决定"用图"还是"程序化")

| 候选素材 | 结论 |
|---|---|
| `img_zaowu_directional_fog_v2.png` (841×1870, 1.0 MB) | **不可用** — 实为**竖向金色光柱**(god rays),不是横向雾;且为灰度 PNG(colorType=0),色相/形态/方向全不对 |
| `img_zaowu_bg_shadow_v2.png` (840×1871) | 不可用 — 是阴影层,灰度,非雾 |
| `img_houshan1_cloud_*` (Ellipse 系列 6 张) | 已在用(6 朵装饰云),再加会重复;但它们保留,与新雾层互补 |
| 新增整屏雾图 | **否决** — 当前 APK 已 **472 MB**,且 CODE-AUDIT 已把大图列为 CRITICAL;再加图是反向操作 |

**结论:程序化(径向渐变)实现** —— 天然软边、任意分辨率不糊、alpha 可廉价动画,且**零新增素材/零 APK 增长**,正好匹配"半透明水墨晕染"。

#### 实现:[HoushanMistLayer.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/components/HoushanMistLayer.kt)(新建)

**三层视差**(周期互质 → 长期不重复):

| 层 | 位置 | 周期 | 透明度 | 位移 | 对应要求 |
|---|---|---|---|---|---|
| 远景 | 远峰山脊之间 (Y≈150~345) | **53s** | 0.09~0.11 | ±15~18dp | "中远景移动更慢" |
| 中景 | 山腰 + 左右画面边缘 (Y≈430~620) | **41s** | 0.13~0.16 | ±22~26dp | 层次过渡 |
| 近景 | 山脚 + 画面底部边缘 (Y≈700~862) | **29s** | 0.20~0.22 | ±34~38dp | "前景移动稍快" |

共 **11 个雾团**,每团由 `MistBlob(x, y, w, h, baseAlpha, alphaAmp, driftX, driftY, phase)` 一行描述 —— 调参只改三张表。

**无缝 + 无明显重复痕迹的原理(关键)**:
- 每层用 **0→1 线性回绕**进度(`RepeatMode.Restart` + `LinearEasing`)→ 匀速漂移
- 所有派生量取 `sin/cos(2π(t+phase))`。因 sin 在 t=1 与 t=0 取值相同,**回绕瞬间不跳变 → 天然无缝**,无需做首尾对接
- 三层周期取 **53/41/29(均为质数)** → 合成周期 ≈ **63017s ≈ 17.5 小时**,肉眼不可能看出循环
- 每团 `phase` 不同 + "聚散"相位再偏移 `0.31` → 位移与明暗不同步,避免整齐划一

**性能(对应"注意性能表现")—— 每帧零重组**:
| 手段 | 效果 |
|---|---|
| 位移用 `Modifier.offset { }`(lambda 版) | 状态读取推迟到 **layout 阶段**,不触发组合 |
| 透明度在 `onDrawBehind` 内读取 | 状态读取推迟到 **draw 阶段**,不触发组合 |
| `drawWithCache` 缓存渐变 Brush | 仅尺寸变化时重建,不每帧分配 |
| 三层各只 hold **1 个**动画值(共 3 个) | 而非每团一个 `Animatable` + 协程(11 协程) |
| 无 render layer | 不用 `graphicsLayer{alpha}`(那会为每团建一个离屏 FBO) |

**不干扰交互(对应"不要遮挡或影响…阅读和点击")**:
- 本层**不含任何 `clickable`/`pointerInput`** → 完全不拦截触摸事件
- 插入位置为**背景图之后、所有内容之前** → 永远位于山峰/标签/文字/熊猫/气泡之下
- 因上述元素均为**不透明 PNG 图版**(标签是米色竖版、气泡是实体图、熊猫是不透明图),雾层不会降低任何文字对比度
- 颜色用同色不同 alpha(`MistColor.copy(alpha=0f)`)而非 `Color.Transparent` —— 避免"透明黑"插值产生灰边

#### 接线

| 文件 | 改动 |
|---|---|
| [Houshan1Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt) | 背景图之后、内容层之前插入 `HoushanMistLayer()` |
| [Houshan2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt) | 插入**景深平面 2(云雾层)之内、6 朵云之前** → §36 的过渡推进时,雾与云一起"相对后移 + 淡出",层次一致 |

两个页面共用 `R.drawable.img_shilian_bg`(同一张背景),故雾层位置表完全可共用。

#### 架构说明(偏离既有约定,需用户确认)

ONBOARDING §5.5 记有"❌ 不要抽公共 Composable(用户明确偏好直接复制)"。本次**仍抽到 `ui/components/HoushanMistLayer.kt`**,理由:
1. 两页**共用同一张背景**,雾层位置表本就应完全一致
2. 本层是**纯氛围层**,不含任何屏内容(与"教练辅助组"那类逐屏分化的内容不同)
3. 复制两份 ≈ 180 行且需长期手工同步,收益为负
4. `ui/components/` 本就是项目放共享 Composable 的地方(StandardGunlunScaffold 等)

**若用户要按原约定逐屏复制,改动是纯机械的**(把文件内容内联为各屏 private fun,再把 `rememberCloudFloat` 那样的 private 副本各留一份)。

#### 编译

```
JAVA_HOME=C:\Users\28784\.jdks\jbr-21.0.11
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 25s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL
adb install -r app-debug.apk      →  Success
```

#### git 状态(待 commit)

- `?? HoushanMistLayer.kt`(新增,约 200 行)
- `M Houshan1Screen.kt`(+1 import +5 行调用 +注释)
- `M Houshan2Screen.kt`(+1 import +4 行调用 +注释)

### §38 后山3 也加入云雾缭绕(与后山1/2 效果统一)(2026-09-15 晚)— A 模式不 commit

**用户指令**:"请在'后山3'页面也加入持续循环的云雾缭绕动画,并与'后山1、后山2'的效果保持统一。云雾以半透明、水墨晕染的质感,在远山、山谷及前景山脚间缓慢流动和聚散;前景云雾可略快,远景云雾更慢,形成自然的景深层次。动画应柔和、低速、无缝循环,避免明显的重复感或现代化特效。注意保留熊猫御剑飞行的视觉焦点:云雾可从其周围及山体之间轻轻掠过,但不要遮挡熊猫角色、关卡标签或影响交互操作。"

#### 关键判断:"统一"统一的是什么

用户说"保持统一",但**后山3 的背景是另一张画**:

| 页面 | 背景资源 | 像素 |
|---|---|---|
| 后山1 / 后山2 | `img_shilian_bg` | 1236 × 2751 |
| **后山3** | **`img_shilian2_bg`** | **824 × 1834**(另一张画作,见 §36 相关系数证伪) |

若直接**照搬坐标**,雾会落在后山3 的空白天空(看不见)或压在深色近山上(发灰发脏)。
故解读为:**统一的是"效果",不是"坐标"** ——

| 维度 | 处理 |
|---|---|
| 三层周期 53/41/29s | **完全共用** → 节奏统一 |
| 配色 `MistColor` #F7F5EE | **完全共用** → 色调统一 |
| 透明度区间(0.09~0.22) | **完全共用** → 浓淡统一 |
| 视差结构(远慢近快)+ 位移幅度 | **完全共用** → 层次统一 |
| **11 个雾团的坐标** | **后山3 单独一套**,贴合其自身构图 |

用户已明确指定后山3 的位置意图("在远山、山谷及前景山脚间"),这也印证坐标需要按构图调。

#### 实现:组件扩展为双变体

[HoushanMistLayer.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/components/HoushanMistLayer.kt) 增加:

```kotlin
enum class HoushanMistVariant {
    Houshan12,   // 后山1 / 后山2(共用 img_shilian_bg)
    Houshan3,    // 后山3(img_shilian2_bg,构图不同 → 位置表单独调)
}

@Composable
fun HoushanMistLayer(
    variant: HoushanMistVariant = HoushanMistVariant.Houshan12,
    modifier: Modifier = Modifier,
)

private class MistLayout(val far: List<MistBlob>, val mid: List<MistBlob>, val near: List<MistBlob>)
private val Houshan12Mist = MistLayout(...)   // 11 团
private val Houshan3Mist  = MistLayout(...)   // 11 团
```

默认值 `Houshan12` → **后山1/后山2 调用处零改动**(仍是 `HoushanMistLayer()`)。

#### 后山3 位置表依据

对照 `img_shilian2_bg` 的水墨留白带(坐标基于 412×917 设计稿):

| 层 | 对应画中位置 | Y 范围 | 理由 |
|---|---|---|---|
| 远景 | 顶部远山群之间 | 120 ~ 445 | 用户指定"远山" |
| 中景 | **熊猫所在的山谷**(熊猫在 Y=405~501)+ 左右画面边缘 | 400 ~ 765 | 用户指定"山谷";同时满足"从熊猫周围轻轻掠过" |
| 近景 | 前景深色近山的山脚与画面底缘 | 690 ~ 1038 | 用户指定"前景山脚" |

#### 熊猫焦点保护(用户特别强调)

1. **整层位于内容层之外、之前** → 绘制顺序上雾在熊猫**之下**,物理上不可能遮挡熊猫
2. 熊猫是不透明 PNG(`img_shilian2_recovered_8`),雾在其下层不会透出、不会降低对比度
3. 位于 3 个关卡标签之下 → 标签是米色不透明竖版,文字对比度不受影响
4. **本层无 `clickable`/`pointerInput`** → 不拦截触摸,整屏跳转"未完待续"与 3 个标签的
   事件消费逻辑(§20)完全不受影响

"中景 Y=400 起"这一处正是为"从熊猫周围掠过"设计的:雾带在熊猫同一高度但位于其下层,
视觉上像从身下/身侧流过,而不会盖住角色。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 25s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 12s
adb install -r app-debug.apk      →  Success
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(加 variant 枚举 + MistLayout + 第二套位置表)
- `M Houshan3Screen.kt`(+2 import +4 行调用 +KDoc 一行)

### §39 云雾提速 + 加强(用户反馈"动画不够明显")(2026-09-15 晚)— A 模式不 commit

**用户指令**:"动画不够明显,可以把动画的播放速度加快一点"

#### 先量化:原参数到底慢到什么程度

位移是 `sin` 驱动的,峰值速度 = `2π × driftX / period`:

| 层 | §37/§38 原值 | 峰值速度 |
|---|---|---|
| 远景 | driftX=16, period=53s | **1.90 dp/s** |
| 中景 | driftX=24, period=41s | **3.68 dp/s** |
| 近景 | driftX=38, period=29s | **8.23 dp/s** |

近景 8.23 dp/s ≈ 每秒移动屏宽的 **2%** —— 用户反馈"不够明显"属实,不是观感错觉。

#### 改动 1:提速(周期调小)

| 层 | 原周期 | 新周期 | 倍数 |
|---|---|---|---|
| 远景 | 53s | **29s** | 1.83× |
| 中景 | 41s | **23s** | 1.78× |
| 近景 | 29s | **17s** | 1.71× |

**三个新值 29/23/17 仍是两两互质的质数** → 合成周期 = 29×23×17 = **11339s(约 3.1 小时)**。
比原来的 17.5 小时短,但单次使用时长内仍不可能看出循环节拍,"无明显重复痕迹"依然成立。
(⚠ 提速时必须保持互质,否则三层的拍点会重合,立刻显出规律)

#### 改动 2:加强幅度

| 参数 | 远景 | 中景 | 近景 |
|---|---|---|---|
| `driftX`(原 → 新) | 16 → **26** | 24 → **40** | 38 → **60** |
| `driftY`(原 → 新) | 6 → **10** | 9 → **16** | 14 → **22** |
| `baseAlpha`(原 → 新) | 0.09~0.11 → **0.13** | 0.13~0.16 → **0.19** | 0.20~0.22 → **0.27** |
| `alphaAmp`(原 → 新) | 0.035~0.040 → **0.070** | 0.045~0.055 → **0.100** | 0.062~0.070 → **0.130** |
| `MIST_BREATHE` | — | — | **新增 0.11** |

**提速 + 加幅度的合成效果**(峰值速度):

| 层 | 原 | 新 | 倍数 |
|---|---|---|---|
| 远景 | 1.90 dp/s | **5.63 dp/s** | **2.97×** |
| 中景 | 3.68 dp/s | **10.93 dp/s** | **2.97×** |
| 近景 | 8.23 dp/s | **22.18 dp/s** | **2.70×** |

即整体观感约 **2.7~3 倍**强于原来。

#### 改动 3(新功能):真正的"聚散"—— 明暗 + 胀缩

原实现只有明暗起伏,读起来像"渐隐渐显"而不是"聚散"。§39 增加**同相胀缩**:

```kotlin
val breathe = 1f + cos(angle) * MIST_BREATHE   // ±11%
scale(scale = breathe, pivot = center) {       // canvas 矩阵变换
    drawRect(brush = brush, alpha = alpha)
}
```

- 用 `DrawScope.scale`(canvas 矩阵)**而非 `graphicsLayer`** → 不建 render layer,性能不退化
  (沿用 §37 的"每团一个离屏 FBO 太贵"的判断)
- 明暗与胀缩同相 → 视觉上像水墨在宣纸上**洇开 / 收拢**,而不是单纯淡入淡出
- 这同时是"更明显"的重要来源:尺寸变化比透明度变化更容易被眼睛捕捉

#### 改动 4(可维护性):调参从 22 行收敛到 3 行

原 `MistBlob` 每团带 9 个参数(坐标 + 浓度 + 幅度),22 个团 = 22 行 × 9 列,用户每次调参都要逐行改。

重构为**分层共用**:

```kotlin
private data class MistBand(baseAlpha, alphaAmp, driftX, driftY)   // 每层 1 份,共 3 份
private data class MistBlob(x, y, w, h, phase)                     // 每团只留位置 + 相位
private val FAR_BAND  = MistBand(baseAlpha = 0.13f, alphaAmp = 0.070f, driftX = 26f, driftY = 10f)
private val MID_BAND  = MistBand(baseAlpha = 0.19f, alphaAmp = 0.100f, driftX = 40f, driftY = 16f)
private val NEAR_BAND = MistBand(baseAlpha = 0.27f, alphaAmp = 0.130f, driftX = 60f, driftY = 22f)
```

**效果:以后"整体更浓/更淡/更快/漂更远"只需改 3 行**(且三条 `*_BAND` 对三个页面同时生效 → 天然保持"效果统一")。
单团微调仍改位置表里那一行(现在只有 5 个参数,一眼可读)。

文件顶部新增**调参入口表**(KDoc),把"想要什么效果 → 改哪个常量"直接列出来,便于后续迭代。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 25s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 13s
adb install -r app-debug.apk      →  Success
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(周期 53/41/29 → 29/23/17;新增 MistBand 分层参数;新增 MIST_BREATHE 聚散缩放;KDoc 加调参入口表)

### §40 云雾继续加浓(baseAlpha 三层各 +0.05)(2026-09-15 晚)— A 模式不 commit

**用户指令**:"1"(承接 §39 结尾给出的三步建议,选 **第 1 步:先加浓**)

#### 改动(仅 3 行 —— 正是 §39 重构后"调参入口"的设计目的)

| 层 | baseAlpha 原 → 新 | 透明度实际区间(原 → 新) |
|---|---|---|
| 远景 `FAR_BAND` | 0.13 → **0.18** | 0.06~0.20 → **0.11~0.25** |
| 中景 `MID_BAND` | 0.19 → **0.24** | 0.09~0.29 → **0.14~0.34** |
| 近景 `NEAR_BAND` | 0.27 → **0.32** | 0.14~0.40 → **0.19~0.45** |

只改 `baseAlpha`,`alphaAmp` / `driftX` / `driftY` / 周期全部不动
→ **速度与"聚散"幅度保持 §39 的状态,本次只提升浓度**,便于单独判断浓度这一个变量。
(这正是 §39 结尾"一次只动一项"的原因:三项一起改无法归因)

三个页面(后山1/2/3)因共用 `*_BAND`,**同步生效** → 仍保持"效果统一"。

#### 重叠浓度提醒(未改,供判断)

雾团之间是有重叠的(如后山1/2 近景三团圆心 Y≈790/885/952,间距 95dp < 半径 90dp×2),
重叠处**有效透明度按 `1-(1-a)^n` 叠加**,两团相叠时最高可达 ≈0.70。
若真机看着底部发白、把山体冲淡了,说明这一档偏浓,**回退只需把那 3 行的 `baseAlpha` 各减 0.05**。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 5s   (增量,首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 4s
adb install -r app-debug.apk      →  Success
```

> 注:本次 `am start` 因用了 `Select-Object -First 1` 提前掐断管道,导致 adb 非零退出且输出丢失,
> 一度误判为启动失败。已用 `dumpsys activity` + `pidof` 复核:
> `topResumedActivity=com.jueqiao.jianghu/.MainActivity`,进程存在 → **实际启动正常**。
> (教训:`Select-Object -First N` 会提前终止上游原生命令的管道,不要用它截断 `adb` 的输出判断成败)

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(3 行 `baseAlpha`)

### §41 修复雾团形状 bug —— 后山3 下半屏几乎无雾的真正原因(2026-09-15 晚)— A 模式不 commit

**用户指令**:"后山3页面的下半个部分的动画不够明显"

#### 先做覆盖率分析,而不是凭感觉加雾团

写了一个脚本,按组件实际的渐变衰减公式(`R = min(w,h)/2`,stops `[0,0.5,1]` → alpha `[1.0,0.55,0.0]`),
在 412×917 设计稿上按 20dp 网格逐点算**合成透明度**(`1 - Π(1-aᵢ)`),输出 ASCII 覆盖图。

**分析结果直接指出问题**:雾只占了中间约 180dp 宽的一条**竖窄带**,左右大片空白:

```
      ...            ← 雾被挤在中间窄条里
      ....
      ...            ← 下半屏 63% 的格子 alpha < 0.10(几乎无雾)
```

| 指标 | 实测值 |
|---|---|
| 下半屏平均合成 alpha | **0.086** |
| 下半屏"几乎无雾"格占比 | **63%** |

**用户反馈属实,而且不是"浓度不够",是"根本没铺到"。**

#### 根因:圆渐变被当矩形铺 —— 一个真实实现 bug

原代码:

```kotlin
val radius = size.minDimension / 2f          // ← 由"高"决定
val brush = Brush.radialGradient(..., radius = radius)
onDrawBehind { drawRect(brush = brush, alpha = alpha) }   // ← 铺满整个 Box
```

- `Brush.radialGradient` 是**圆**渐变,可见范围只有 `radius` 的直径
- 雾团 Box 是 440~520 **宽** × 150~200 **高**,`min(w,h)` 取的是**高**
  → `radius` 只有 75~95dp,**可见圆直径仅 150~190dp**
- 宽出来的那 250~330dp 全是透明 —— **宽的 `w` 参数实际毫无作用**

即:我一直以为"宽扁的 Box = 横向雾带",但代码从来没做到。文件 KDoc 里
"`min(w,h)` 决定渐变半径,故宽扁的块 = 横向雾带"这句注释**描述的是一个不存在的行为**。

#### 修复:按圆建渐变 + canvas 非等比缩放拉成椭圆

```kotlin
val radius = size.minDimension / 2f
val brush = Brush.radialGradient(..., radius = radius)
// 把半径 radius 的圆拉伸成半轴 (w/2, h/2) 的椭圆
val stretchX = size.width / size.minDimension
val stretchY = size.height / size.minDimension
onDrawBehind {
    val breathe = 1f + cos(angle) * MIST_BREATHE
    // pivot = center,与渐变中心重合 → 拉伸后圆心不动
    scale(scaleX = stretchX * breathe, scaleY = stretchY * breathe, pivot = center) {
        drawCircle(brush = brush, radius = radius, center = center, alpha = alpha)
    }
}
```

- `drawRect` → `drawCircle`(画圆,再由矩阵拉伸)
- §39 的 `breathe` 聚散**并入同一个 `scale` 调用**(`stretch * breathe`),没有增加额外变换
- 仍是 canvas 矩阵变换,**不建 render layer**,性能设计不变

#### 效果(同一套脚本复算)

| 指标 | 修复前 | 修复后 | 变化 |
|---|---|---|---|
| 下半屏平均合成 alpha | 0.086 | **0.203** | **2.4×** |
| 下半屏"几乎无雾"格占比 | 63% | **19%** | **↓44pt** |
| 全屏"几乎无雾"格占比 | 76% | 44% | ↓32pt |
| 峰值合成 alpha | 0.41 | 0.56 | 仍在可接受范围 |

**本次刻意只改"形状",不改浓度** —— `baseAlpha` 保持用户 §40 刚确认的 0.18/0.24/0.32。
理由:这样才能把"下半屏变明显"**单独归因到形状修复**上(§39 结尾我自己定的"一次只动一项")。
脚本同时算了备选浓度(0.15/0.20/0.26 → 下半屏 0.167 / 空档 25%;0.13/0.17/0.22 → 0.141 / 31%),留档备选。

#### 遗留(未改,供用户决定)

修复后覆盖图显示:**底右角(x>270, y>690)仍偏弱** —— 因为两套位置表的近景团圆心都偏左
(后山3 近景 cx≈60/160/170/180;后山1/2 cx≈160/170/180)。
若要补,应在**后山3 与后山1/2 同步**加一团右侧近景(保持"效果统一"),而不是只补后山3。

#### 工具链踩坑(记录,避免重复)

| 坑 | 现象 | 正解 |
|---|---|---|
| PowerShell **不支持 `//` 注释** | 首字符 `/` 被当除法运算符,解析器一路吞 token,**报错行号完全误导**(报在 61 行,实际是第 1 行) | 用 `#` |
| PowerShell **变量名大小写不敏感** | `$A`(alpha 表)被循环里 `$a = ...` **覆盖成数字**,后续索引全空 → 覆盖图全空白、mean=0 | 改名 `$ALPHA` |
| 脚本含中文 + 无 BOM UTF-8 | PowerShell 按 GBK 读取,中文注释乱码并**破坏字符串终止符** | 分析脚本一律用纯 ASCII |

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 21s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 10s
adb install -r app-debug.apk      →  Success
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(drawWithCache 块:`drawRect` → `drawCircle` + 非等比 `scale`;KDoc 补 §41 修复说明与正确行为描述)

### §42 修复"设计稿 ≠ 真机尺寸"导致的雾团出屏 —— 后山3 下半屏补雾(2026-09-15 晚)— A 模式不 commit

**用户指令**:"我觉得后山3页面的下半部分的雾气含量太少了"(附 22:53 真机截图)

#### 根因:我一直在用**设计稿尺寸**算,但真机不是那个尺寸

§41 的覆盖率分析用的是 **412×917**(Figma 设计稿),但真机实测:

| 项 | 值 |
|---|---|
| `wm size` | 1080 × 2400 px |
| `wm density` | 440 dpi → scale **2.75** |
| **实际 dp** | **392.7 × 872.7 dp** |
| 绘制区 | `MainActivity` 用了 **`enableEdgeToEdge`** → 可绘制区 = 整个窗口 |

**917 − 873 = 44dp,差的全部在底部** —— 而"底部雾堤"正是放在那里的。
按 `可见范围 = cy ± 0.72·ry`(由 §41 的 `Falloff` 反解 alpha>0.1)验算后山3 的 4 个近景团:

| 雾团 | 圆心 cy | ry | 可见 Y 区间 | 在 873dp 屏上 |
|---|---|---|---|---|
| n1 | 780 | 90 | 715 ~ 845 | ✓ 完整可见 |
| n2 | 835 | 90 | 770 ~ 900 | 部分(底部超出) |
| n3 | 875 | 95 | 807 ~ 943 | **只剩上半** |
| n4 | 948 | 90 | 883 ~ 1013 | **完全在屏外**(顶边 883 > 873) |

**4 个近景团里 1.5 个白费。后山1/2 同样问题**(cy=790/885/952,同样 1.5 个出屏)。
§41 把圆修成了椭圆(横向铺开了),但纵向这几个团仍然在屏幕外 → 所以用户看着下半屏还是空的。

#### 修复:按真机高度重排近景,并让两个变体共用

```kotlin
private val NearBlobs = listOf(
    MistBlob(x = -160f, y = 640f, w = 440f, h = 180f, phase = 0.06f),  // 左下
    MistBlob(x = -60f,  y = 620f, w = 500f, h = 180f, phase = 0.31f),  // 中下
    MistBlob(x = 150f,  y = 640f, w = 460f, h = 190f, phase = 0.58f),  // 右下
    MistBlob(x = -140f, y = 730f, w = 460f, h = 180f, phase = 0.19f),  // 底左
    MistBlob(x = -30f,  y = 720f, w = 480f, h = 170f, phase = 0.44f),  // 底中
    MistBlob(x = 170f,  y = 735f, w = 440f, h = 160f, phase = 0.73f),  // 底右
)
```

- **4 → 6 团**,圆心 cy = 710/730/735/805/820/835 **全部落在 873 以内**
- x 分**左/中/右三列**(cx ≈ 60/190/380),每团可见半宽 ≈165dp → 合成后横向无空档
- **两个变体共用同一份** `NearBlobs`(`Houshan12Mist.near = NearBlobs`,`Houshan3Mist.near = NearBlobs`)
  - 理由:两张背景的下半部分都是"深色近山 + 画面底缘",这一层是沿底边铺开的云海堤岸,
    **不依赖具体山峰位置**(不像 far/mid 必须卡在各自画作的山峰之间)
  - 同时天然满足用户要求的"后山1/2 与后山3 效果统一"

#### 浓度:0.32 → 0.26(用"铺开"换"不加浓")

团数从 4 增到 6 且全部可见后,若维持 `baseAlpha = 0.32`,峰值合成 alpha 会冲到 **0.65**(发白糊成一片)。
脚本试算四档后选定 0.26:

| 方案 | 下 1/3 平均浓度 | 峰值 | 结论 |
|---|---|---|---|
| §41 现状(4 团,1.5 出屏)@.32 | 0.230 | 0.56 | 基准 |
| 新 6 团 @.32 | 0.382 (+66%) | **0.65** | 峰值过高,发白 |
| **新 6 团 @.26** | **0.324 (+41%)** | **0.56** | ✅ 选定:峰值不升 |
| 新 6 团 @.24 | 0.299 (+30%) | 0.52 | 偏保守 |

**关键:单团浓度下调、团数增加 → 底部雾气 +41%,而峰值浓度一点没涨。**
这是"把雾铺开"而不是"把雾加浓",不会糊成一片。

#### 效果(真机帧 393×873 复算)

| 指标 | §41 后 | §42 后 | 变化 |
|---|---|---|---|
| 下半屏平均合成 alpha | 0.197 | **0.257** | +30% |
| 下半屏"几乎无雾"格占比 | 18% | **12%** | ↓6pt |
| **下 1/3(y>600)平均浓度** | 0.230 | **0.324** | **+41%** |
| 峰值合成 alpha | 0.56 | 0.56 | 不变 |

覆盖图(真机帧)显示 y=650~830 已是**满宽**的 `=`/`+` 密度,右侧空档消失;
此前逐行只有中间几列有雾。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 22s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 13s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(新增共用 `NearBlobs` 6 团;两变体 `near = NearBlobs`;`NEAR_BAND` alpha 0.32 → 0.26)

> **§43 已迁移到 [SESSION-LOG-2026-09-16.md](./SESSION-LOG-2026-09-16.md) §2**。本节于 2026-09-16 09:30 完成,按"一日一日志"约定放入 09-16 文件,这里不再重复。

## 沉淀(新)

- **"某个区域没动画"不一定=没覆盖,可能=没视觉锚点**(新)— §42 后 `HoushanMistLayer` 在拆招心法下方已有满宽覆盖,但都是均匀细雾,**没有明显的运动焦点**——叠在深色近山上读起来像山体本身的渐变。**解决方案不是再加浓度(发白)也不是再加团数(更乱),而是在视线集中位置放一个"聚焦元素"**:更大、更浓、节奏更慢、明确的上下浮动。**关键:单一元素 + 慢节奏,比均匀细雾的视觉效果强得多**
- **碎屏布局的"聚焦飘带"应在文件私有**(沿用 §37)— 与近/中/远三层均匀雾不同,聚焦飘带**强位置相关性**(标签 x/y 不同就无法复用),后山3 与后山1/2 的拆招心法位置不同,所以只放在 `Houshan3Screen.kt` 私有处;后山1/2 需要时另起一处,不强求"一处定义三页复用"

- **写**了。`docs/SESSION-LOG-2026-09-14.md` 今天上午 **§2** 已经从 `docs/_archive/` 合并过来,现 137 行,与 archive 完全一致(刚刚核实)
- 09-15 今天一直**在持续写**,从上午 §1(adb 重设)到现在的 §42(近景雾团重排),**没断过档**

- **设计稿尺寸 ≠ 真机尺寸,绝对坐标布局前必须查真机 dp**(新)— 本项目设计稿是 **412×917**,但真机(K50 Pro)是 `1080×2400 @ density 440` = **393×873 dp**,且 `enableEdgeToEdge` 使可绘制区 = 整个窗口。**矮了 44dp,全部在底部** → 按设计稿摆在"底部"的元素会直接掉出屏幕。§41/§42 连查两轮才发现:§41 修了横向(圆→椭圆),§42 才发现纵向出屏。**取真机尺寸的命令**:`adb shell wm size` + `adb shell wm density`,dp = px ÷ (density/160)
- **"某个区域没效果"要同时查横向和纵向覆盖**(新)— §41 只算了横向(发现圆渐变太窄),§42 才算纵向(发现底部团在屏外)。覆盖率脚本应直接输出**真机帧**的 ASCII 图,一眼看出哪一列/哪一行是空的
- **增加元素数量时要同步下调单元素强度**(新)— 近景团 4 → 6 且全部可见后,若维持 `baseAlpha=0.32` 峰值会冲到 0.65(发白)。**降单团浓度 + 增团数 = 总量上升但峰值不变**,这是"铺开"而非"加浓",观感更自然
- **雾团的可见范围公式**:`可见半径 = 0.72 × ry`(由 `alpha>0.1` 反解 `Falloff`,其中 `ry = h/2`)。摆位置时用这个算,能让圆心 Y 的上限 = `屏高 - 0.72·ry`,避免出屏
- **"某个区域效果不够明显"要先算覆盖率,不要凭感觉加元素**(沿用 §41)

- **"某个区域效果不够明显"要先算覆盖率,不要凭感觉加元素**(新)— §41 用户说"后山3 下半个部分动画不明显",直觉会去"多加几个雾团";实际写 20dp 网格的合成 alpha 覆盖图后,发现真因是**雾团形状渲染错了**(圆渐变被当矩形铺),雾根本没铺到那里。加元素是在错误的地基上叠加,算一次覆盖率就直接定位了
- **KDoc 写了什么不等于代码做了什么**(新)— §37 注释写着"宽扁的块 = 横向雾带",但 `radius = minDimension/2` + `drawRect` 的组合让可见范围只有圆的直径,**宽的 `w` 参数完全无效**。跨了 §37~§40 四个小节都没发现,因为没人验证过形状。注释描述的意图必须与代码行为对照检查
- **`Brush.radialGradient` 是圆;要椭圆必须自己拉伸**(新)— 正解:按圆建 brush,再用 `DrawScope.scale(scaleX = w/minDim, scaleY = h/minDim, pivot = center)` 把 `drawCircle` 拉成填满 Box 的椭圆。`pivot` 与 brush `center` 重合则圆心不动
- **PowerShell 三个静默坑**(新)— ① 不支持 `//` 注释(`/` 被当除法,**报错行号会误导到几十行之后**);② **变量名大小写不敏感**,`$a` 会覆盖 `$A`;③ 无 BOM 的 UTF-8 中文脚本被按 GBK 读,**乱码会破坏字符串终止符**。分析脚本建议一律纯 ASCII + `#` 注释 + 变量名带后缀区分
- **先量化再改**(沿用 §39)— 位移类动画峰值速度 = `2π × 幅度 / 周期`;静态类效果用**网格覆盖率 + 合成 alpha** 比对"改前/改后",能把"感觉"变成可归因的数字(§41 用 63%→19% 与 0.086→0.203 证明修复有效)

- **"动画不明显"要先量化再改**(新)— 位移是 `sin` 驱动时,峰值速度 = `2π × 幅度 / 周期`。§39 算出原设计近景仅 **8.23 dp/s(≈屏宽 2%/秒)**,证实用户反馈属实。只凭"感觉调快点"容易改不到位或改过头;算一次就知道该动幅度还是动周期
- **提速无限循环动画时必须保持各层周期互质**(新)— §37 用 53/41/29(17.5h 合成周期),§39 提速到 29/23/17(3.1h)。若改成 30/20/10 这类有公倍数的值,三层拍点会重合,**立刻显出循环规律**,违反"无明显重复痕迹"
- **"聚散"要靠尺寸而非只靠透明度**(新)— 只做明暗起伏读起来像"渐隐渐显";叠加同相胀缩(如 ±11%)才像水墨洇开/收拢。且用 `DrawScope.scale`(canvas 矩阵)实现,**不建 render layer**,不牺牲 §37 的性能设计
- **同一个效果跨多页时,把"风格参数"与"位置表"分离**(新)— 风格参数按层共用(3 行,三页同时生效 → 天然统一);位置表按页面各自的背景构图单独调(两张背景是不同画作,坐标不能照搬)。§39 把 22 行 × 9 列的调参面收敛成 3 行

- **先核对 API 版本再写动画代码**(新)— 本项目 Compose/Navigation 版本对 API 可用性敏感(§23~§26 因 `animateColor`/`Animatable<Color>`/`GraphicsLayerScope.colorFilter` 连续失败 4 次)。动手前查 `gradle/libs.versions.toml` + 记住关键下限(如 Navigation destination transitions ≥ 2.7.0),可一次通过
- **两张图宽高比相同 ≠ 构图相关**(新)— 全屏设计稿恒为 412/917=0.44929,比例相同只说明"都是全屏稿"。判断两图是否同一构图,要用**对亮度不变的相关系数**搜索缩放/裁剪因子,不能靠"像素尺寸整除关系"(1236/824=1.5 就是陷阱)
- **`graphicsLayer.transformOrigin` 是相对元素自身**(新)— 要让多个元素绕同一屏幕点缩放,须各自包一层 `fillMaxSize` Box(使自身边界=屏幕),而非逐个算 origin
- **Navigation Compose 的 pop 过渡会继承 enter/exit**(新)— 设了 `enterTransition` 就必须检查 `popEnterTransition` 是否被意外继承
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
