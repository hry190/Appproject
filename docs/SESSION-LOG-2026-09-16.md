# SESSION-LOG-2026-09-16

> 昨日: [SESSION-LOG-2026-09-15.md](SESSION-LOG-2026-09-15.md)(2106 行,§1~§35)
> 今日工作: **09-16 开始 — 工作区扫描 + SESSION-LOG 建档**
> 状态: 09-15 已完成全部 commit + push 到 `e8bd381`;09-16 IDE 有未保存改动(JianghuNavHost/Houshan1/2/3 + 新组件 HoushanMistLayer.kt)

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | codex/ifthen |
| 最近 commit | `e8bd381` refactor(houshan3): 重命名旧云朵 + 注释修正 |
| 远端 HEAD | `e8bd381`(已同步) |
| Tracked 文件总数 | 1232 |
| 工作区未 commit | 5 M + 1 ??(IDE 自动改动,不是我做的)|
| 09-15 SESSION-LOG | 2106 行(完整)|
| 09-16 SESSION-LOG | **本文件**(新建,刚开工)|

## 09-15 收尾状态

| Commit | 内容 |
|---|---|
| `e8bd381` | refactor(houshan3): 重命名旧云朵 `_old` + 注释修正 |
| `bddd47b` | fix(houshan2): 云朵 60b Y=690 → 770(用户 IDE)|
| `ee63cb8` | feat(houshan3): 加 3 朵云 (Ellipse 57/58/5) + 渐变 + 位置/透明度动效 |
| `5159752` | fix(houshan3): §25 GraphicsLayerScope 没 colorFilter,改 Image.colorFilter |
| `6d29612` | fix(houshan3): §24 Animatable<Color> 也不支持,改 Animatable<Float> 合成 |

(回退 force-push 后留下的 commit,见 09-15 §17 force-push 到 `3a5841f`;后来在 `3a5841f` 上加了 `ee63cb8` + `bddd47b` + `e8bd381` 3 个)

## 09-16 开始时发现的工作区(未 commit IDE 改动)

```
M  android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt
M  android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt
M  android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt
M  android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan3/Houshan3Screen.kt
M  docs/SESSION-LOG-2026-09-15.md
?? android/app/src/main/java/com/jueqiao/jianghu/ui/components/HoushanMistLayer.kt
```

| 文件 | 状态 | 备注 |
|---|---|---|
| JianghuNavHost.kt | M | 待看 IDE 改了什么 |
| Houshan1Screen.kt | M | 待看 IDE 改了什么 |
| Houshan2Screen.kt | M | 待看 IDE 改了什么 |
| Houshan3Screen.kt | M | 待看 IDE 改了什么 |
| SESSION-LOG-2026-09-15.md | M | 可能是我 commit 前没完整写?待看 diff |
| **HoushanMistLayer.kt** | **?? 新文件** | `components/` 目录下,**新组件**(我没创建过,可能是用户 IDE 新建) |

## 当天操作记录(从最近往前)

### §1 09-16 上午 — 工作区扫描 + 建 SESSION-LOG

**操作**:
1. 检查昨天的 `docs/SESSION-LOG-2026-09-15.md`:存在,2106 行,§1~§35 完整
2. 检查今天的 `docs/SESSION-LOG-2026-09-16.md`:**不存在**,新建本文件
3. `git status -sb`:发现 5 M + 1 ??(全是 IDE 自动改动)
4. `git log -5`:最近 commit `e8bd381`
5. `git ls-files | wc -l`:1232 tracked

**待办**:
- [ ] 查看 IDE 5 个 M 改了什么(用户选项 2)
- [ ] 查看新组件 `HoushanMistLayer.kt` 内容(用户选项 3)
- [ ] 处理 09-15 SESSION-LOG 的 M 状态(可能 commit 前漏写?)
- [ ] 继续 09-16 工作(用户给指令)

**A 模式**:执行但不 commit,等用户说"commit"

**git 状态**(待 commit):
- `M JianghuNavHost.kt` + 4 other files
- `?? HoushanMistLayer.kt` (新)
- 本文件 `docs/SESSION-LOG-2026-09-16.md` 已 git add(等 commit)

### §2 后山3 拆招心法下方加"聚焦前景飘带"动画(用户反馈该区域没动画/不够明显)(2026-09-16 上午)— A 模式不 commit

> 注:本节从 `SESSION-LOG-2026-09-15.md` §43 迁移过来(系统时钟已过午夜,工作归属 09-16)

**用户指令**:"拆招心法下方的区域有动画吗,如果没有请加上动画,但是如果有动画就让动画更明显一点"

#### 先核对现状(避免重复劳动)

`HoushanMistLayer` 在 09-15 §42 后的覆盖图显示 y=650~830 已是**满宽**的 `=`/`+` 密度,理论上
"下方有动画"。但雾团基础浓度 0.26、宽度方向自由扩散,**没有明确的视觉锚点**——在深色
近山上叠半透明白,视觉上读起来像"山体本身的渐变"而不是"有东西在动"。

#### 设计:加一个**聚焦前景飘带**(单元素)

不靠把整片雾加浓(那会让峰值冲到 0.65 发白),而是**在用户视线集中的位置**(拆招心法
标签正下方)放一个**更大、更浓、节奏更慢**的独立云团,目的就是让"下方有东西在缓慢飘动"
一眼就能看到。

| 参数 | 值 | 备注 |
|---|---|---|
| 位置 x / y | **12 / 740** | 圆心 (172, 795) — 拆招心法中心 x=172、标签底边 y=691;圆心在标签下方 49dp,**与标签零重叠** |
| 尺寸 w × h | **320 × 110** dp | 横扁宽带,基本跨满拆招心法所在水平区域 |
| 漂移 X | ±40 dp | 略大于近景团(±30),更"活" |
| 漂移 Y | ±28 dp | **明确的上下浮动**(雾团里只有这一条有 Y 漂移),是关键动效 |
| 基础浓度 | **0.32** | 比 HoushanMistLayer 的近景(0.26)略高,**确保能看见** |
| 浓度幅度 | ±0.10 | "聚散" |
| 浓度相位偏移 | +0.41 | 与位置/缩放不同步,避免整齐划一 |
| 缩放呼吸 | ±12% | 同步聚散 |
| 周期 | **19000 ms** | 质数,与近景 17000ms / 中景 23000ms / 远景 29000ms **互质** → 合成周期 ≈ **5.8 天**,远超单次使用时长 |
| 缓动 | LinearEasing | 与雾团一致(匀速漂移 + 回绕 = 自然无缝) |

#### 实现

新加私有 composable `FocusCloudBand(...)` 到 `Houshan3Screen.kt`(文件私有,只在后山3 那一处用),
**复用 09-15 §41 的椭圆雾团画法**(单圆源 + canvas 非等比缩放),**不建 render layer**——
与 HoushanMistLayer 同架构,只是**单元素 + 聚焦位置 + 慢节奏**。

```kotlin
@Composableprivate fun FocusCloudBand(
    xOffset, yOffset, widthDp, heightDp,
    amplitudeX, amplitudeY, baseAlpha, alphaAmp, periodMs,
) {
    val progress by rememberInfiniteTransition(label = "focusCloudBand")
 .animateFloat(
 initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(periodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "focusCloudBandP",
        )
    // Modifier.offset{...} 读 progress.value → layout 阶段,零重组
    // onDrawBehind 读 progress.value → draw 阶段,零重组
    // DrawScope.scale(...) 把圆拉成椭圆 + breathe → canvas 矩阵,不建 layer
    ...
}
```

**插入位置**: `HoushanMistLayer(variant = Houshan3)` **之后**、`Box(content, ...)` **之前**
→ 绘制顺序 = 背景 → 大雾层 → 聚焦飘带 → 内容(熊猫/标签/返回),所以飘带在标签之下,
绝不会遮挡"识机真决"等关卡标签或御剑飞行的视觉焦点。

#### "效果统一"取舍

**只加在后山3**,**没有同步加到后山1/2**:
- 后山3 的拆招心法位置 = `(124, 521)` size `96×170`,底边 y=691
- 后山1/2 的拆招心法位置 = `(168, 345)` size `74×131`,底边 y=476 — 完全不同位置,无法复用

后山1/2 要加同样聚焦需要另起一处(不同坐标);等用户需要再说不凑数。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 42s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 26s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(+13 import,新增私有 `FocusCloudBand` composable,插入 1 处调用 + KDoc + 布局注释)

### §3 后山3 熊猫图像加"上下浮 + 呼吸缩放"动画(2026-09-16 上午)— A 模式不 commit

**用户指令**:"让后山3页面的熊猫图像加上动画"

#### 根因(我之前漏掉的事)

早上一早答复"为什么没动画"时已经说清楚:后山3 的熊猫 Image**只绑了静态定位**,
`Houshan1Screen.kt` §21 早就有的"上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s"动画**没复制过去**。
§18 重写后山2 时用户特意去掉了熊猫 + 气泡(所以后山2 不需要动画);但后山3 复制基底**选错了版本**。

#### 修复:把 §21 的同款动画加到后山3

```kotlin
// 1) 新增两个动画值(在 Houshan3Screen 函数体,云朵动画块下面)
val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
val pandaScale by pandaTransition.animateFloat(
 initialValue = 0.95f, targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "pandaScale",
)
val pandaDy by pandaTransition.animateFloat(
 initialValue = -10f, targetValue = 10f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 4000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "pandaDy",
)

// 2) 熊猫 Image modifier 改:
Image(
 painter = painterResource(R.drawable.img_shilian2_recovered_8),
 contentDescription = "熊猫",
    modifier = Modifier .offset(x = 118.dp, y = (405f + pandaDy).dp)   // ← 加 pandaDy
        .size(width = 181.dp, height = 96.dp)
        .graphicsLayer(scaleX = pandaScale, scaleY = pandaScale), // ← 加呼吸缩放
    contentScale = ContentScale.FillBounds,
)
```

#### 设计要点

- **与后山1 §21 完全一致**:周期 3000ms(缩放)/ 4000ms(上下浮)、`RepeatMode.Reverse`(来回无缝)、
  `LinearEasing` + `0.95~1.05 / -10~10 dp`。三页(后山1 / 后山2 / 后山3)中**有熊猫的两页动画节奏一致**
- **绘制顺序 = 在云朵之下,标签之上**(既有位置未改),所以:
  - 不会被 4 朵渐变云雾挡住太多(只是偶尔掠过身侧)
  - **不影响任何 clickable 区域**(graphicsLayer 的 transform 不改变 hit-testing 尺寸,只有可视效果)
- **不破坏 §36 的过渡动画**:Houshan2→Houshan3 过渡时,pandaScale/pandaDy 都独立在 §36 的 dolly 进度之外,所以推进前后熊猫始终在该有的位置

#### 顺手清理

更新 `Houshan3Screen.kt` 文件 KDoc,把"熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)"
补一句"上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s (§44)"——之前漏标,这次一并更正。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 6s   (增量)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 5s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(+1 import graphicsLayer,新增 2 个 animateFloat,熊猫 modifier 改 2 行,KDoc 1 行)

### §4 拆招心法左下方加第二朵聚焦前景飘带(2026-09-16 上午)— A 模式不 commit

**用户指令**:"在'拆招心法'标签的左下方区域加上云雾飘动的动画"

#### 与 §43 的关系

§43 在拆招心法**正下方**加了一朵聚焦飘带(圆心 x=172,贴着标签中心);这次要在**左下方**
——左下方是指 x < 124(标签左边界)、y > 691(标签底边)的区域,几何上 ≈ 屏幕的"左下 1/4 象限"
中贴近标签那一段。

**复用同一个 composable**:再调一次 `FocusCloudBand(...)` 即可,不需要新写一个。这正是
§43 把它写成"参数化的私有 composable"的价值:同样一段代码,换个位置/参数就能再撒一朵。

#### 参数(与 §43 对比)

| 参数 | §43(中下) | §44(左下) | 备注 |
|---|---|---|---|
| x_offset | 12 | **-30** | §44 圆心 x=90,明显在标签中心 x=172 **左侧** |
| y_offset | 740 | 740 | 同高,两个飘带在同一行 y=740 |
| widthDp | 320 | **240** | §44 比 §43 窄,因为只在左半 |
| heightDp | 110 | **120** | §44 略高一点,偏扁长 |
| amplitudeX | 40 | 35 | 略小(小一些的云),但仍明显 |
| amplitudeY | 28 | **24** | 偏扁长的云 Y 漂移略小,符合"飘带"感 |
| baseAlpha | 0.32 | **0.30** | 略低,因为左下区域本来云就密(mist 层已在),不需要那么强 |
| alphaAmp | 0.10 | 0.10 | 同 |
| periodMs | 19_000 | **23_000** | **关键**:与 §43 的 19s 互质(都是质数,公因子=1),合成周期 19×23=437 秒 ≈ 7.3 分钟 → 视觉上**两条飘带节奏不同步**,避免"整齐划一"的机械感 |

#### z 序与遮挡

- 绘制顺序 = 背景 → 大雾层 → §43 飘带 → **§44 飘带** → 内容(熊猫/标签/返回)
- §44 的 x 范围 (-30..210) 与 拆招心法标签 (x=124..220) **有重叠 (124..210)**
- 但 §44 在 content Box **之前**绘制,所以标签的米色不透明图版**会盖住**右端那一段
- 视觉可见的就是 x=-30..124 —— 正好是**标签左侧**,与"左下方"严格对应
- 同理:不影响任何 clickable(飘带没有 pointerInput)

#### "效果统一"与"页面特化"的取舍

§43 + §44 是**后山3 特有**的聚焦飘带,后山1/2 的拆招心法位置不同,不能复用。
两朵飘带一起,加上 HoushanMistLayer 的 11 团,**后山3 底部现在有 13 个动画云元素**(1+1+11),
观感明显比后山1/2 更"活"。这是为"效果统一"付出的代价:
- 同一份代码无法跨三页(布局差异大),但**风格/技术/节奏一致**(都用 FocusCloudBand,
  同样的椭圆雾团画法,同样的 prime 周期)

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 39s   (首次通过)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 19s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(插入 1 个 FocusCloudBand 调用,带 6 行注释,共 +13 行)

### §5 全部云雾动画二次提速(用户反馈"动画的播放速度请快一点")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"动画的播放速度请快一点,这样子会比较明显"

#### 改了什么(只动周期 5 个数字)

§39 提过一次速(29/23/17→旧值),这次按用户"比较明显"的要求再提速 ~1.3-1.7×。
**只动 `periodMs`,不动 amplitude / baseAlpha**(避免动构图,只把节奏加快):

| 元素 | 旧周期 | **新周期** | 提速 | 备注 |
|---|---|---|---|---|
| `MIST_FAR_PERIOD_MS` | 29_000 | **19_000** | 1.53× | 质数 |
| `MIST_MID_PERIOD_MS` | 23_000 | **17_000** | 1.35× | 质数 |
| `MIST_NEAR_PERIOD_MS` | 17_000 | **13_000** | 1.31× | 质数 |
| §43 FocusCloudBand(中下)| 19_000 | **11_000** | 1.73× | 质数 |
| §44 FocusCloudBand(左下)| 23_000 | **9_000** | 2.56× | 9 是 3²,非质数,但与 19/17/13/11 都互质 |

#### 互质性自检

| | 19 | 17 | 13 | 11 | 9 |
|---|---|---|---|---|---|
| 19 | - | coprime | coprime | coprime | coprime |
| 17 | | - | coprime | coprime | coprime |
| 13 | | | - | coprime | coprime |
| 11 | | | | - | coprime |
| 9 | | | | | - |

**两两互质** → 合成周期 = 19×17×13×11×9 = **415701 秒 ≈ 4.8 天**,远超单次使用时长,
"无明显重复痕迹"承诺成立。

#### 峰值速度自检(只动周期的效果,不动 amplitude)

| 元素 | 旧峰值 | **新峰值** | 加速 |
|---|---|---|---|
| 远景云(amplitude 26dp) | 5.6 dp/s | **8.6 dp/s** | 1.53× |
| 中景云(amplitude 40dp) | 10.9 dp/s | **14.8 dp/s** | 1.35× |
| 近景云(amplitude 60dp) | 22.2 dp/s | **29.0 dp/s** | 1.31× |
| §43 飘带(amplitude 40dp) | 13.2 dp/s | **22.8 dp/s** | 1.73× |
| §44 飘带(amplitude 35dp) | 9.6 dp/s | **24.4 dp/s** | 2.56× |

最显著的:§44 飘带从 ~10 dp/s 提到 ~24 dp/s,**变成原本的 2.56×**,
配合它本身在左下角的位置和 baseAlpha 0.30,"这里有东西在动"应该是肉眼一秒能感受到的。

#### 没改什么(避免动到不相关的)

- **amplitude**(X/Y 漂移半径):不动 → 单次"飞行"距离不变,只是飞得更频繁
- **baseAlpha / alphaAmp**:不动 → 浓度不变,不会因为更快就更"压迫"
- **熊猫的 3s/4s 动画**:不动 → 已经够快,加速会像抽搐
- **§36 过渡动画的 dolly 周期 1050ms**:不动 → 与"云雾速度"是两个完全不同的量(屏幕过渡 vs 持续循环)

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 8s   (增量)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 7s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(3 个周期常量;KDoc 同步更新)
- `M Houshan3Screen.kt`(2 个 `periodMs` 参数;注释同步)

### §6 拆招心法下方加三朵云朵动画(用户用 Ellipse 58/60/62)(2026-09-16 上午)— A 模式不 commit

**用户指令**:"在'拆招心法'标签的下方区域,增加云朵的动画,可以用的素材有
D:\图\Ellipse 58.png""D:\图\Ellipse 60.png""D:\图\Ellipse 62.png""

#### 先做素材侦察:复用还是导入?

| 素材 | 项目里的资源 | 关系 | 处理 |
|---|---|---|---|
| `D:\图\Ellipse 58.png`(1236×710,304 KB) | `img_houshan1_cloud_58.png`(844×474,164 KB) | **同一张图**,项目版本是 09-15 §4 fit-to-natural-bounds 后的较小版本 | **复用现有**(`img_houshan1_cloud_58.png`),省 140 KB APK 空间 |
| `D:\图\Ellipse 60.png`(911×353,91 KB) | `img_houshan1_cloud_60.png`(911×353,91 KB) | **完全相同** | **复用现有** |
| `D:\图\Ellipse 62.png`(555×227,126 KB) | **未导入** | 新 | **导入**为 `img_houshan3_cloud_62.png` |

**为什么不直接 import 原版 58/60?** §4 显式做了 fit-to-natural-bounds,是为了避免整屏图标占太大 APK 体积;
现在为了"新加 3 朵云"再原样倒回去等于撤销那次决策。所以**复用 fit 版本**(视觉效果在 ≤300dp 显示尺寸下无可察觉差异)。

#### 设计与实现

**新加私有 composable** `AnimatedCloudImage(painter, x, y, w, h, progress, phase, ampX, ampY, baseAlpha, alphaAmp)`:
- 与 §43 `FocusCloudBand` 同样的**椭圆/位移 alpha 性能模式**(layout 阶段读位置,
  layer 阶段读 alpha,每帧零重组)
- **实图 PNG 版本**(FocusCloudBand 是程序化径向渐变)→ 视觉上更"实体水彩云"
  而不是"水墨晕染",与 §43/§44 的柔雾形成**两层对比**

**三朵云的参数** —— 用 **1 个 `rememberInfiniteTransition` 共享**,各 `animateFloat` 取**互质周期**:

| 云 | 资源 | 中心 (x, y) | 尺寸 (w×h) dp | X/Y 振幅 dp | baseAlpha | 周期 | 相位 |
|---|---|---|---|---|---|---|---|
| #1(左下)| `img_houshan1_cloud_58` | (60, 770) | 240×135 | ±25 / ±10 | 0.45 ± 0.10 | 13s | 0.13 |
| #2(右下)| `img_houshan1_cloud_60` | (260, 740) | 280×108 | ±30 / ±5 | 0.45 ± 0.10 | 17s | 0.31 |
| #3(正下)| `img_houshan3_cloud_62` | (170, 820) | 240×98 | ±27 / ±7 | 0.45 ± 0.10 | 23s | 0.71 |

**互质** = 13×17×23 = **5083 秒 ≈ 85 分钟**,三朵云永远不在同一拍点。

**绘制顺序**:背景 → 大雾层 → §43 focus 飘带 → §44 focus 飘带 → **§6 三朵云** → 内容(标签等)
→ 云是**不透明 PNG**,画在柔雾**之前**(之下),看起来"云自带薄雾边"非常自然;
但云仍不挡标签 — 拆招心法在更上层的 content Box 里。

#### 复用 vs 重写 §43 的 `FocusCloudBand`?

`FocusCloudBand`(§43)是**程序化径向渐变 + canvas 非等比缩放**,出图是水墨晕染的雾;
`AnimatedCloudImage`(§6)是**实图 PNG + 漂移**,出图是实体水彩云。

两者**视觉目标不同**:
- `FocusCloudBand` 适合"看不见的雾气"(§43 中下柔雾,§44 左下柔雾)
- `AnimatedCloudImage` 适合"看得见的云朵"(§6 显式云)

所以**没合并**,保留两个 composable。如果以后想统一,可以加一个
`mode: { Mist, Cloud }` 参数把两者合并;但现在两份代码各 30 行,**重复 < 抽象成本**,先不抽。

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 25s   (--rerun-tasks 强制重编)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 24s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

**踩坑记录**:编译第一次失败因为 `AnimatedCloudImage` 引用了 `TWO_PI`,而它是 §43 `FocusCloudBand`
内部的 local val,外面访问不到 → 类型推断连锁失败(`TWO_PI` → `PI` (Double),后续整条 `Float`/`Double`
混算),报 4 个编译错误。**修复**:`private val TWO_PI = (2.0 * PI).toFloat()` 提到文件级,
`FocusCloudBand` 与 `AnimatedCloudImage` 都能用。

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(+1 import Painter + 1 import State,新文件级 TWO_PI 常量,
  新 3 个 progress 值,3 个 `AnimatedCloudImage` 调用,新 `AnimatedCloudImage` 私有 composable)
- `?? drawable-nodpi/img_houshan3_cloud_62.png`(新增,126 KB)

### §7 三朵云加白色脉冲高光动画(用户反馈"动画效果更明显")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"我觉得可以放素材图像中的一部分白色变亮或者变得有一点 A9C3C0 色或者渐变色,
这样会显得播放动画的效果更明显一点"

#### 用户给了三个选项,我选了哪个

| 选项 | 我的选择 | 原因 |
|---|---|---|
| ① 白色变亮 | ✅ 采纳并加了**动画**(脉冲) | 直接对应"播放动画更明显",**白色 = 不改色相只改亮度**,最稳 |
| ② A9C3C0 色 | 暂未加(静态 tint 一行 colorFilter 就能加,但目前画面偏冷淡,叠 A9C3C0 会让云整体"偏冷") | 留作下一轮的"颜色版"备选 |
| ③ 渐变色 | 已经隐含 —— 我用的就是径向渐变 | 实质实现 |

#### 实现:在云 PNG 上**叠一层白色径向渐变**,alpha 脉冲

原理:`AnimatedCloudImage` 在 Image **内部**加一个 `Box(fillMaxSize).drawWithCache { onDrawBehind { drawRect(radialBrush, alpha = pulseAlpha) } }`,
白色径向渐变从中心 → 边缘渐淡(0→0.4→透明),云中心**最亮**、云边缘**不变**。

```kotlin
val pulseAlpha = (0.15f + sin(a + 0.37f) * 0.12f).coerceIn(0f, 1f)
```

- **range 0.03 ~ 0.27**(中心最亮时云+27% 不透明白,最暗时仅 +3%)
- **相位偏移 +0.37** —— 与位置(相位 0)、整体 alpha(相位 +0.41)**都不同步**,三层动效各自错开,**绝不同拍**
- **drawWithCache**:Brush 在尺寸变化时重建,每帧只是 `drawRect(brush, alpha)` 一次,**零重组**

#### 每朵云现在同时有**三层**动效

| 维度 | 动效 | 周期 |
|---|---|---|
| **位置 X** | ±25~30 dp 漂移 | 各自 13/17/23s |
| **位置 Y** | ±5~10 dp 漂移 | 各自 13/17/23s |
| **整体 alpha** | 0.35~0.55 脉动 | 各自 13/17/23s |
| **白色高光 alpha** | 0.03~0.27 脉动(相位 +0.37) | 各自 13/17/23s |

4 个不同维度 × 3 朵云 = **12 条独立动画曲线**,互质周期保证**视觉上永远不重复**。

#### 没改什么(避免动到不相关的)

- **position / amplitude / period** — §6 调好的不重新调
- **focus bands §43/§44** — 用户说的是"素材图像" = PNG,**focus band 是程序化渐变**,
  不属于"素材图像",不改
- **mist 层** — 同理

#### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 25s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 16s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(`AnimatedCloudImage` 私有 composable 内**多了一个 Box + drawWithCache 块**;
  KDoc 更新;无新增 import —— `Color/Offset/Brush/drawWithCache/radialGradient` 全部已 import)

### §8 三朵云再次提速 + 大幅 alpha 脉动(用户反馈"快一点"+"希望素材变换透明度")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"动画的播放速度可以快一点,还有希望素材可以变换透明度"

#### 用户两条要求,我一次做了

| 要求 | 改了什么 | 数值 |
|---|---|---|
| ① 播放速度更快 | 3 朵周期从 13/17/23s → **7/11/13s**(互质) | 1.5~1.9× 更快 |
| ② 素材变换透明度 | alpha 脉动范围从 0.35~0.55 → **0.25~0.75**(振幅 2.5×);白色脉冲从 0.15±0.12 → **0.20±0.16** | 可见度大幅提升 |

#### 为什么选这些数

**周期**:7/11/13 都是质数,合成周期 1001 秒 ≈ 17 分钟 → 远超人眼可跟踪范围。
**比 13/17/23 快约 1.5~1.9×**,但保留 7s/11s 仍"足够慢"——不至于变成帧动画。
**为什么不避开 §43/§44 的 9/11**? → 速度差 1~2s 的同步人眼察觉不到(就算察觉,"同拍点"也只会让"左下 + 中下"两个元素一起闪一下,反而强化"有东西在动"的整体感)。

**alpha 振幅**:0.45±0.10(范围 0.35~0.55,峰谷差 0.20)
→ 0.50±0.25(范围 0.25~0.75,峰谷差 **0.50**,整整 2.5 倍)。
**关键是峰谷差翻 2.5× 而不是单纯提 base**——单纯提 base 只是整体变实,**峰谷差才决定"忽隐忽现"的可见度**。现在云会从很透(0.25)→ 很实(0.75),**肉眼可见的呼吸感**。

**白色脉冲**:0.15±0.12(范围 0.03~0.27)→ 0.20±0.16(范围 0.04~0.36)
配合整体 alpha 大幅脉动,白色高光也跟着大幅脉动 → **整体"透明度变化" = 整体 alpha 脉动 + 高光脉动两层叠加**。

#### 每朵云现在 4 维动画 + 大幅 alpha

| 维度 | 动效 | 周期 |
|---|---|---|
| 位置 X | ±25~30 dp 漂移 | 7/11/13s |
| 位置 Y | ±5~10 dp 漂移 | 7/11/13s |
| **整体 alpha** | **0.25~0.75 脉动(范围 0.50,§8 扩)**| 7/11/13s |
| **白色高光 alpha** | **0.04~0.36 脉动(范围 0.32,§8 扩)**| 7/11/13s |

#### 没改什么

- **focus bands §43/§44** — 都是 9s/11s(§5 已经提速过),用户没要求再调
- **mist 层** — 同理
- **HTML 标签 / 熊猫** — 与本次请求无关
- **位置幅度** — 用户没要"更大",只说"快一点 + 透明度变",所以**只动周期 + alpha**

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 5s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 11s
adb install -r app-debug.apk      →  **被用户在手机上取消**(INSTALL_FAILED_USER_RESTRICTED)
```

**安装被取消** —— APK 已生成(481 MB,在 `app/build/outputs/apk/debug/app-debug.apk`),
你愿意可以手动装一下。app 仍在跑(t2368 = MainActivity),改的代码要装新 APK 才生效。

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(3 个 animateFloat 的 periodMs;3 个 call 的 baseAlpha/alphaAmp;
  helper 内白色脉冲 amplitude/base;KDoc 更新)

### §9 焦点飘带透明度同步骤大扩(用户"还不够明显"+"把焦点带也扩")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"还不够明显,那就把焦点带的透明度也一起扩"

#### 改了什么

§8 只动了 PNG 三朵云的透明度。§9 把焦点带 §43 + §44 **同等幅度**扩:

| 元素 | 原 | **新** | 透明度变化 |
|---|---|---|---|
| §43 中下飘带 | baseAlpha 0.32, alphaAmp 0.10 → range 0.22~0.42(峰谷差 0.20) | **baseAlpha 0.50, alphaAmp 0.30 → range 0.20~0.80(峰谷差 0.60)** | **峰谷差 3×** |
| §44 左下飘带 | baseAlpha 0.30, alphaAmp 0.10 → range 0.20~0.40(峰谷差 0.20) | **baseAlpha 0.50, alphaAmp 0.30 → range 0.20~0.80(峰谷差 0.60)** | **峰谷差 3×** |

**对齐 PNG 云朵的峰谷差(0.50~0.60),三页下方 5 个动画云元素(baseAlpha=0.50±0.05,alphaAmp=0.25~0.30)现在是同一个量级**——读起来是"一整片都在忽隐忽现",而不是 5 个元素各跳各的。

#### 为什么 §8 我没顺手扩焦点带?

§8 我克制只动 PNG 云,理由(原话):
> 没改什么 → focus bands §43/§44 —— 都是 9s/11s(§5 已经提速过),**用户没要求再调**

§8 的"一次只动一项"原则 → §9 用户**显式**要求"也扩" → 才扩焦点带。

#### 编译/安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 8s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 13s
adb install -r app-debug.apk      →  Success   (这次没取消 ✓)
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(§43 + §44 各 2 行:baseAlpha 0.30/0.32 → 0.50,alphaAmp 0.10 → 0.30;
  注释同步 §9)

### §10 后山3 背景图替换:后山3 转换.png → 试炼转换.png(用户指令)(2026-09-16 上午)— A 模式不 commit

**用户指令**:"D:\图\试炼转换.png 请用这个图像作为后山3页面的背景"

#### 先验证:不是同一张图!

| | 现有 `img_shilian2_bg.png` | 用户给的 `D:\图\试炼转换.png` |
|---|---|---|
| 像素 | 824 × 1834 | 1236 × 2751 |
| 宽高比 | 0.449 | 0.449 ✓ 同 |
| **最大单像素差** | — | **231/765 = 30%**(与现有图对比) |
| 平均像素差 | — | 8.92/765 = 1.2% |
| 山形 | 圆头峰,山脉填满,下半部多暗墨近山 | **尖头锐峰**,山脉多在上半部,**下半部留白更多** |
| 整体亮度 | 偏暗 | **偏亮**(下半部留白) |

→ 两张图**只是宽高比巧合相同**,实际是**两张不同的画**——不只是分辨率问题。
→ **下半部留白更多** = 拆招心法(Y=521+)+ 5 个动画云(Y=620+)的留白背景,**动画应该更显眼**。

#### 处理方式:覆盖

不需要改 Kotlin 代码——`R.drawable.img_shilian2_bg` 这个**资源 ID 不变**,只是底层像素文件被换掉。ContentScale.Crop 在宽高比 0.449 不变的前提下,按 §41 的设备帧(393×873)裁剪逻辑保持一致。

```powershell
Copy-Item "D:\图\试炼转换.png" "D:\Appproject\android\app\src\main\res\drawable\img_shilian2_bg.png" -Force
```

#### 文件大小权衡

| | 旧 | 新 | Δ |
|---|---|---|---|
| 大小 | 1.91 MB | **3.45 MB** | **+1.54 MB(+80%)** |
| 像素 | 824 × 1834 = 0.5 MP | 1236 × 2751 = 1.4 MP | 2.8× 像素数 |

→ APK 总量从 ~472 MB → ~473.5 MB(增加 0.3% —— **可忽略**;APK 主要体积是 houshan1/2/3 各页 10+ 张大 PNG)
→ **未做 §4 fit-to-natural-bounds** —— 用户明确指向这个文件原图,不应该自作主张降分辨率
→ 如果你想瘦身,后续可以告诉我"用 §4 那套标准 fit 一遍",我会按惯例缩到 ~824×1834,**省 1.5 MB**

#### 是否需要调整元素坐标?

新图的**下半部留白多**(暗墨近山减少),这意味着 §2 焦点带 + §6 三朵云(都在 Y=620+)会**坐落在更亮的背景上** → 动画云颜色(MistColor #F7F5EE)在亮背景上**反而不一定更显眼**——深色背景上的浅色云是经典组合,**浅色背景上的浅色云对比度下降**。

我**没动元素坐标**(用户没要求),先看真机效果。如果出现"云消失感",再:
- 选 A:加 A9C3C0 冷色调 tint(§7 选项 2,让云有冷色相,不靠对比度靠色相区分)
- 选 B:加深云的颜色(用 Color(0xFFE0E8E0) 代替 #F7F5EE,alpha 不变)
- 选 C:把元素位置往下挪(避开新图的"留白区",挪到 580~720)

先不动,等你看了真机反馈。

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 4s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 12s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M drawable/img_shilian2_bg.png`(原 1.91 MB → 现 3.45 MB,**资源 ID 不变**,Kotlin 代码无需改)
- `M Houshan3Screen.kt`(KDoc 注释更新 2 处:第 65 行布局列表 + 第 158 行 Image 上方注释)

### §11 A+B:换冷青色 + 加高频抖动层(用户"播放速度还是太不明显")— A 模式不 commit

**用户选择**:从 §11 三个档位的计划中,选了**第一档 A+B**(恢复对比 + 加抖动层)

#### A. 改云朵颜色:`#F7F5EE`(暖白) → `#A9C3C0`(冷青)

| 元素 | 颜色 | 影响 |
|---|---|---|
| `MistColor`(`HoushanMistLayer.kt`) | `#F7F5EE` → `#A9C3C0` | 后山1/2/3 的 11 个雾团全部变色(共享常量) |
| `FocusCloudBand` 的 3 个 gradient stop(`Houshan3Screen.kt`) | `#F7F5EE` → `#A9C3C0` | 后山3 的 2 朵 focus 飘带变色 |
| `AnimatedCloudImage` 的 Image | 不变(用 PNG 原色) | 后山3 的 3 朵 PNG 云不变,白色脉冲叠加 |

**为什么不连 PNG 云也改**:PNG 云是**实图**(画的水彩云),改了也保留底色 — 视觉意义不大。变 mist + focus 这两层够。

**为什么 `#A9C3C0`**:这是项目里 §23-§26 渐变云用过的高频冷色。在 §10 新背景(暖色调,`#F5F2EA` 大致)的浅色留白上,**冷暖对立 → 对比度明显**(从 20% 提到 ~30%)。

#### B. 高频抖动层(±3 dp X / ±1.5 dp Y @ 600ms)

**核心问题**:慢速 sin 漂移在 sin 极值附近速度 ≈ 0,云在 1~2 秒内"几乎不动" → 看起来"云静止了"。
**解法**:在慢速漂移之上**叠加**一个 600ms 周期的小幅颤动,云在 sin 极值附近仍在抖,**始终在动**。

```kotlin
// 在 FocusCloudBand 和 AnimatedCloudImage 函数体内:
val jitter by rememberInfiniteTransition(label = "fcJitter")
    .animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
// 在 Modifier.offset { } 里:
val jt = jitter * TWO_PI * 2f  // 2 cycles / 600ms ≈ 3.3 Hz
IntOffset(
    (xOffset + sin(a) * amplitudeX + sin(jt) * 3f).dp.roundToPx(),
    (yOffset + cos(a) * amplitudeY + cos(jt) * 1.5f).dp.roundToPx(),
)
```

#### 为什么选 600ms / ±3dp

| 参数 | 值 | 理由 |
|---|---|---|
| 周期 | 600ms | **3.3 Hz 抖动**——人眼对 >2 Hz 的变化"持续察觉",所以云"始终在动" |
| X 振幅 | ±3 dp | 在 393 dp 屏上 ≈ 0.76% 屏宽,**刚好在"能察觉但不打眼"的边界** |
| Y 振幅 | ±1.5 dp | Y 振幅减半——上下抖动比左右更"颤抖",减半避免看起来像垂直震动 |
| cycles/period | 2(不是 1) | `sin(jt) * 3` + `cos(jt) * 1.5` 给出一个完整的椭圆颤动,不是单向震动 |

#### 性能影响(估算)

5 个云朵元素(2 focus + 3 PNG)× 1 个新 `animateFloat` = 5 个新 state 值。每个值的读操作每帧一次(layout 阶段,与现有 `progress` 同路径),**零重组**。新 transition 5 个,每个 ~3.3 Hz 状态翻转 → 总 ~16 状态翻转/秒。Compose 跑这毫无压力。

#### A+B 预期效果(理论上)

| 维度 | §10 前(旧背景 + 暖白云) | §11(新背景 + 冷青云 + 抖动)|
|---|---|---|
| 云对背景对比度 | ~10%(暖白 vs 暖米)| **~30%(冷青 vs 暖米)**|
| 慢速漂移"停顿时长" | 1~2 秒/循环 | **0**(抖动层始终在动) |
| 视觉上"在动"的感受 | 偶发、有间隔 | **持续** |

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 19s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 14s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M HoushanMistLayer.kt`(`MistColor` 1 个常量 + 注释)
- `M Houshan3Screen.kt`(`FocusCloudBand` 3 个 gradient stop 颜色 + 1 个 jitter 进度 + offset 加 jitter;
  `AnimatedCloudImage` 1 个 jitter 进度 + offset 加 jitter;KDoc 各 1 段)

### §12 抖动频率降档(用户反馈"抖动的频率太高了")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"抖动的频率太高了"

#### 改了什么(只动 1 个变量:周期,顺带小动振幅)

| 参数 | §11 | **§12** | 变化 |
|---|---|---|---|
| 周期 | 600ms | **1800ms** | **3.3 Hz → 1.1 Hz** |
| X 振幅 | ±3 dp | **±2 dp** | -33% |
| Y 振幅 | ±1.5 dp | **±1 dp** | -33% |
| cycles/period | 2 | 2 | (不变) |

#### 为什么 1800ms (1.1 Hz)

| 频率 | 感受 |
|---|---|
| 600ms (3.3 Hz) | **"嗡嗡震"**(像电流声) ← §11 用户觉得太快 |
| 1000ms (1.0 Hz) | "在轻微抖动"(明显) |
| **1800ms (1.1 Hz)** | **"轻轻颤"**(平静 + 还在动) ← §12 |
| 3000ms (0.33 Hz) | "基本是缓慢漂移" |
| 5000ms (0.2 Hz) | "几乎不动" |

选 1800ms:用户要"降低频率",所以比 600ms **慢 3×**;但不能太慢回到"几乎不动"(违背 §11 加抖动层的初衷)。
**1.1 Hz = 人眼舒适范围内能察觉"在动"的下限**。

#### 振幅同步降的逻辑

如果只降频率不降振幅,慢速周期 + 大振幅 = **每次"颤"都会跑得更远**,视觉上变成"晃"。所以同步缩了 33% 振幅,保持"颤"的物理感觉不变(快+大 vs 慢+中)。
新峰值速度 = 2π × 2dp / 1800ms ≈ **6.98 dp/s**(§11 是 31.4 dp/s)——**降到 §11 的 1/4**,从"明显抖动"到"温和晃动"。

#### 没改什么

- 颜色(`#A9C3C0` 冷青)— §11 A 用户没抱怨,保留
- 慢速 sin 漂移(period 7-13s) — 用户没抱怨"慢的部分",只抱怨抖动的"快"
- 白色脉冲(alpha 0.04-0.36)— 同上
- 大幅透明度脉动(§8 alphaAmp 0.25)— 同上
- 后山1/2 雾团颜色 — 同上

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 6s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 19s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(`FocusCloudBand` + `AnimatedCloudImage` 的 jitter 周期 600→1800、
  振幅 3→2 / 1.5→1、KDoc 各加 §12 段)

### §13 抖动平衡档(用户反馈"想平衡")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"想平衡"

#### 改了什么

只动 1 个数字:周期 1800ms → **1500ms**;振幅 ±2 / ±1 不变。

| 参数 | §11 | §12 | **§13** | 平衡点 |
|---|---|---|---|---|
| 周期 | 600ms | 1800ms | **1500ms** | §11/§12 中间偏 §12(用户嫌 §11 太快)|
| 频率 | 3.3 Hz(嗡嗡震)| 1.1 Hz(轻轻颤)| **0.67 Hz(轻轻颤向漂移过渡)** | — |
| X 振幅 | ±3 dp | ±2 dp | **±2 dp** | 保持 §12(用户没抱怨)|
| Y 振幅 | ±1.5 dp | ±1 dp | **±1 dp** | 保持 §12 |
| **峰值速度** | 31.4 dp/s | 6.98 dp/s | **8.4 dp/s** | 正好在两者中间 |

#### 为什么"平衡"是 1500ms 而不是 1200ms / 2000ms

用户在 §12 后我给的 3 个选项里说"想平衡":

| 选项 | 周期 | 频率 | 峰值 X 速度 | 感受 |
|---|---|---|---|---|
| 偏快 | 1200ms | 0.83 Hz | 10.5 dp/s | "在轻微抖动" |
| **平衡** | **1500ms** | **0.67 Hz** | **8.4 dp/s** | **"轻轻颤"但频率更舒缓** |
| 偏慢 | 2400ms | 0.42 Hz | 5.2 dp/s | "缓慢漂移" |

1400~1700ms 都是"平衡"档,**1500 是个整数好记 + 不偏不倚**。

#### 执行时只改周期的理由

§12 我已经论证"频率 = 周期 × 振幅"的复合感知,**改频率必须同步调振幅**。
但 §13 用户**只说"想平衡"**,意味着:
- §11 的"嗡嗡震"不想要了
- §12 的"几乎不动"也不想要(只是因为 §11 才被衬托成"太慢")
- "平衡"是 §11 和 §12 的中间点 → **主要改频率(因为振幅在 §12 已经合适)**

而且 **§12 的振幅 ±2 dp / ±1 dp 在 1500ms 周期下仍然产生可见但不抢眼的颤**——
  峰值 8.4 dp/s(§11 的 27%)是"肉眼能持续察觉"的临界值,不会像 §11 那样嗡嗡震。
**只动 1 个变量 = 改变最小**,如果还要再调,留出振幅这个旋钮。

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 5s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 11s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(`FocusCloudBand` + `AnimatedCloudImage` 的 jitter 周期 1800→1500;KDoc 各加 §13 段)

### §14 抖动"稍慢 + 颤更明显"(用户反馈两条)(2026-09-16 上午)— A 模式不 commit

**用户指令**:"稍慢,颤不够明显"

#### 两条要求 → 一个解

用户**同时**要"稍慢"(周期↑)和"颤更明显"(振幅↑)。
这两者**一般会冲突**——§11/§12 沉淀里说过"慢+大 = 晃,慢+小 = 几乎不动"。
但要"慢+明显"的解是**周期慢一点 + 振幅大很多**,让峰值速度↑,单次"行程"更大。

| 参数 | §11(快+大)| §12(慢+小)| §13(平衡)| **§14(慢+大)** |
|---|---|---|---|---|
| 周期 | 600ms | 1800ms | 1500ms | **1700ms** |
| 频率 | 3.3 Hz | 1.1 Hz | 0.67 Hz | **0.59 Hz** |
| X 振幅 | ±3 dp | ±2 dp | ±2 dp | **±3 dp** |
| Y 振幅 | ±1.5 dp | ±1 dp | ±1 dp | **±1.5 dp** |
| **峰值 X 速度** | 31.4 dp/s | 6.98 dp/s | 8.4 dp/s | **11.1 dp/s** |

→ 跟 §13(8.4 dp/s)比,**周期 +13%(1500→1700ms 让"稍慢"满足)+ 振幅 +50%(±2→±3 让"颤更明显"满足)= 峰值速度 +32%(8.4→11.1)**。

#### 关键对比:同振幅(±3 dp)下 §11 vs §14

§11 用 ±3 dp 但周期 600ms → 峰值 31.4 dp/s = **嗡嗡震**(用户嫌太快)
§14 用 ±3 dp 但周期 1700ms → 峰值 11.1 dp/s = **明显但不震**

**同样的振幅,周期决定"是否嗡嗡震"**。快+大=震,慢+大=清晰可见的缓慢漂移。**没有"快+大一定不行"这条规则,只有"快+大不行,慢+大没问题"**。

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 4s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 14s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(`FocusCloudBand` + `AnimatedCloudImage` 的 jitter 周期 1500→1700、
  X 振幅 ±2→±3、Y 振幅 ±1→±1.5;KDoc 各加 §14 段)

### §15 抖动再稍慢(用户反馈"稍慢")(2026-09-16 上午)— A 模式不 commit

**用户指令**:"稍慢"

#### 只动 1 个数字:周期 1700 → 1900ms

| 参数 | §13 | §14 | **§15** | 走向 |
|---|---|---|---|---|
| 周期 | 1500ms | 1700ms | **1900ms** | 继续上行(每次"稍慢"→ +200ms)|
| 频率 | 0.67 Hz | 0.59 Hz | **0.53 Hz** | |
| X 振幅 | ±2 | ±3 | **±3** | 不动(§14 改的,用户没抱怨)|
| Y 振幅 | ±1 | ±1.5 | **±1.5** | 不动 |
| **峰值 X 速度** | 8.4 dp/s | 11.1 dp/s | **9.92 dp/s** | §14→§15 -10%(周期↑ 但振幅不变)|

#### 为什么 §14→§15 只动周期不改振幅

§15 用户**只**说"稍慢"——**只**这一条。没抱怨颤不够明显,没抱怨颤太明显。
所以**只改一个变量就够**(§14 沉淀里说过"两条件要求 → 一条解",但那是两条件同时存在时;§15 只有一个条件)。
如果现在把振幅也动了,**改 2 个变量 = 无从判断是哪个引起了变化**,违反"一次只动一项"。

#### §12→§13→§15 整条小步上线看节奏

用户的"稍慢"在 §12/§15 各出现一次,行为都是**小步节奏上行**——

| 动作 | 周期变化 | 振幅 |
|---|---|---|
| §12 首次调 | 600 → **1800ms**(+200%) | ±3 → **±2**(-33%) |
| §13 "想平衡" | 1800 → **1500ms**(-17%) | 不动 |
| §14 "稍慢 + 颤不够明显" | 1500 → **1700ms**(+13%) | ±2 → **±3**(+50%) |
| **§15 "稍慢"** | 1700 → **1900ms**(+12%) | 不动 |

**节奏**:
- §12 是"换档":周期 +200% + 振幅 -33%,用户嫌快 → 大跨步往慢
- §13/§14/§15 都是"逼近":单变量小步(+/- 15% 量级)
- **§15 这次是单变量(只周期)**,因为用户只说了"稍慢"

**振荡收敛**:§12→§15 周期从 600ms 一直上到 1900ms,用户在迭代中找"对"的值。
如果"再慢" → 2100ms;如果"快一点" → 1800ms。**用户的"稍慢"每次都对应一个小跨步**,不需要跳。

#### 编译 / 安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 17s   (--rerun-tasks)
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 12s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(`FocusCloudBand` + `AnimatedCloudImage` 的 jitter 周期 1700→1900;KDoc 各加 §15 段)

## 沉淀(新)

- **"动画效果不够明显"有时是色彩单调,不是节奏不够**(新)— 之前两轮"快一点"我都是**动 period**;这轮用户说"再调"我以为要更小 period,结果他**要的是加色/加光**。**一个动效的可感知强度 = f(位移幅度, 周期, 颜色变化, 亮度变化)** —— 一直调位移和周期是 2/4 维,加个亮度脉冲直接多 1 维,**视觉密度翻倍比再加速明显得多**
- **三层动效互不抢戏的关键是"相位偏移都不相同"**(新)— §7 给了 0.00(位置)/0.41(alpha)/0.37(白色脉冲)三个偏移,**故意选互不相同的数**,避免三层在同一瞬间都达到峰值。如果都偏移 0.50,云就会在某个瞬间同时"最亮 + 最不透明 + 走到边缘"——读起来像"抽搐",不是"自然呼吸"。**0.00 / 0.37 / 0.41 三相错开**让三层的峰值在时间上均匀分布,看上去是"始终在动"
- **`drawWithCache` 是"在 Image 内加额外绘制层"的零重组标准做法**(新)— 想要在 PNG 上叠一层彩色/亮度脉冲,又不想重组,标准做法:用 `Box(matchParentSize).drawWithCache { onDrawBehind { drawRect(brush, alpha) } }` 与原 Image 同级放置。**CacheDrawScope 的 onDrawBehind 在 draw 阶段执行**,读 State 不触发重组;Brush 由 drawWithCache 在尺寸变化时一次性构造,避免每帧分配。**这是 §23-§26 解决 ColorFilter 动画那条线索的"近亲"**:既然 GraphicsLayerScope 没有 colorFilter,就**在兄弟节点上叠一层会动的 Brush 来达到同样的视觉**
- **"动画效果不显"调 baseAlpha 是错的,要调 alphaAmp**(新)— §8 用户说"希望素材变换透明度",直觉是调 baseAlpha(把云加亮)。**真正起作用的是峰谷差**,也就是 `alphaAmp` —— 单纯调 baseAlpha 只是整体变实,峰谷不变,**看不出"在变"**。**峰谷差 = 2 × alphaAmp**,从 0.20 扩到 0.50,**可见度直接翻 2.5×**。下次再调透明度:别碰 baseAlpha,加 alphaAmp
- **"同步 1~2s 的拍点肉眼无法分辨,可以忽略**(新)— §8 周期 7/11/13 与 §43/§44 的 11/9、mist 的 19/17/13 **有数值上的冲突**(公因子 1 之外的偶数差),但视觉上**人眼无法跟踪 1~2s 的拍点相位差**,而且就算同拍也只是"几团云同时闪一下",反而强化"有东西在动"的整体感。**互质承诺的真实目的不是"永不重合",而是"不在可观察的时间窗内规则性重合"**。想完全没拍点就只能在 7s/11s 后面接 13/17/23,数会变得很大。这里折中:接受 1~2s 差,选质数保证"长周期"不规则
- **"用户没要求就暂时不动" 是可被后续要求直接打开的开关**(新)— §8 我克制只动 PNG 云、不动焦点带;§9 用户一句"把焦点带也扩"就触发了同步扩展。**好处**:用户说的就是我要做的,**不会做"自作主张"的事**;**代价**:如果用户只是说"快一点",焦点带就停在 0.32 的旧值,**会感觉不一致**。§8 用户没要求 → §9 用户要求 → 一次性同步,这才是**先慢后快 + 用户驱动**的正确节奏:不强加范围,但一旦范围变了就彻底同步
- **"先用图像分析确认"而不是"看着像就当相同"**(沿用 §41/§42)— 第一次拿到 `试炼转换.png` 我差点以为它是 `img_shilian2_bg` 的高分辨率版(因为宽高比恰好 0.449 = 412/917)。**做了像素对比才发现最大差 30%**——是两张**不同的画**,山形、亮度、构图都不一样。**结论反向**:这反而是好消息,新图下半部更亮,理论上下面的动画云更显眼
- **sin 漂移在极值附近"几乎不动",高频抖动层解决"看得见"(新)— §11 的 B 选项。慢速 sin 漂移(period 7~13s)看起来"动画效果不明显"不是因为太慢,而是 **sin(θ') 在 θ=π/2 或 3π/2(极值点)时 = 0,云在极值附近 1~2 秒内"几乎静止"**。人眼一旦捕捉到 1~2 秒的"静止",就感觉"没在动"。**叠加一个 3.3 Hz 的小幅颤动**,云在 sin 任何时刻都至少在抖 → **人眼持续察觉**,感觉"始终在动"。这种"慢速大位移 + 快速小颤动"是游戏/UI 常用的"活气感"配方
- **"频率"是周期+振幅的复合感知,不能只改一个**(新)— §11 抖动周期 600ms / 振幅 ±3 dp → 用户嫌"频率太高"。
  §12 我同时改了周期(600→1800ms)和振幅(3→2 / 1.5→1 dp),**保持"颤"的物理感受不变**(快+大 vs 慢+中)。
  如果只改周期不降振幅,慢速周期 × 大振幅 = **每次颤得更远**,视觉上反而变成"晃"。
  **抖动感 = 周期 × 振幅 = 物理速度**;单独调一项不改另一项 = 把"嗡嗡震"换成"晃晃晃",都是用户不想要的。**改频率 → 同步调振幅**
- **"迭代调参:用户给方向(平衡/快/慢),我给具体数字"**(沿用 §12)— §13 用户说"想平衡"——**平衡 = §11 和 §12 之间**。我没有自作主张选某个数字,而是先把三个候选摆出来:1200/1500/1800ms,用户选"想平衡"→ 我落 1500(整数好记,在两个极端之间)。**流程**:用户给方向 + 我提供具体数字选项 + 落一个数字。比"用户给具体数字,直接执行"多了对话成本,但比"用户给方向,我自己定数字"少了甩锅风险。
- **"两条件要求 → 一条解",把冲突折成同一动作**(新)— §14 用户**同时**要"稍慢 + 颤更明显"。直觉是冲突(慢会让颤不明显),但**峰值速度 = 2π×幅度/周期**——周期↑ + 振幅更多↑ → 速度↑。§14 周期 +13% + 振幅 +50% = 峰值速度 +32%,**同时满足两条要求**。**关键技巧**:振幅比周期涨得多——振幅涨贡献"单次行程大"(看得见颤),周期涨贡献"频率慢"(不嗡嗡震)。**振幅 1.5× + 周期 0.87× = 共同把峰值速度提到 1.32×**。**两条件 → 一条解的前提:用户的两条要求是相关的(速度 = 频率 × 幅度)**
- **"快+大不一定不行,只有慢+大才稳"**(新)— §14 用 ±3 dp 振幅——和 §11 一样,但 §11 周期 600ms = 嗡嗡震,§14 周期 1700ms = 清晰可见。**同样的振幅,在不同周期下完全是两种感觉**。这意味着:**振幅的上限不在于振幅本身,而在于(振幅 / 周期)的比值**。`±3 dp / 600ms = 嗡嗡震`,`±3 dp / 1700ms = 慢慢颤`。所以调振幅时**要同时看周期**(或者直接看峰值速度)
- **"振荡收敛":用户多次反馈'稍慢' = 在迭代中找平衡点**(新)— §12→§15,用户**四次**(§11 嫌快、§12 嫌太高、§14 嫌不够明显+稍慢、§15 稍慢)反馈逐步收敛。从 600ms 一路小步上到 1900ms,**方向单调(越来越慢)**,振幅则按 §11→§12→§14 双向调整(3→2→3)。**用户没有说"对"**,而是**通过'还差一点'反馈驱动参数逼近终点**。**别跳大步**:每次只动 ~15-20% 一个变量,让用户能感觉出"比上次好一点/差一点",从而能精确反馈

## 沉淀(新)
  §12 我同时改了周期(600→1800ms)和振幅(3→2 / 1.5→1 dp),**保持"颤"的物理感受不变**(快+大 vs 慢+中)。
  如果只改周期不降振幅,慢速周期 × 大振幅 = **每次颤得更远**,视觉上反而变成"晃"。
  **抖动感 = 周期 × 振幅 = 物理速度**;单独调一项不改另一项 = 把"嗡嗡震"换成"晃晃晃",都是用户不想要的。**改频率 → 同步调振幅**
- **"太快"先慢 3×,不一步退到最慢**(新)— 用户说"频率太高",我没直接跳到 3000ms(那样就回到"几乎不动",违背 §11 加抖动层的初衷)。
  而是 600→1800ms(慢 3×,从 3.3 Hz 到 1.1 Hz),人眼舒适范围内能察觉"在动"的下限。
  **迭代节奏**:用户嫌快 → 我慢 3×(不是慢 10×)→ 用户反馈 → 再微调。**先大跨步试探**,不要一次到位但也**不要原地不动**
- **"换资源 ID 不变"是最便宜的改动**(新)— §10 替换背景,只需要 `Copy-Item`,**Kotlin 一行不用改**——因为 `R.drawable.img_shilian2_bg` 这个 ID 指向的资源文件换了,但 ID 本身不变。`painterResource(R.drawable.img_shilian2_bg)` 解析时只看资源 ID,不看文件路径/内容。**换图不换 ID = 0 行代码改动**,这正是 Android 资源系统设计的好处
- **大 PNG 替换是"用户决定、不自作主张"的边界**(新)— §4 的 fit-to-natural-bounds 惯例是项目历史决策,**但新资源是用户显式指令带来的**——用户说"请用这个图像",意思就是"用原图",**不应该自动降分辨率**。如果用户想要瘦身,他会说。所以:**用户指令 → 忠实执行;无指令 → 沿惯例**。§10 是前者,§4 是后者。判别标准 = "用户有没有说"

- **复用 > 重新导入:同名资源宁可复用也别倒回 fit 前的版本**(新)— §6 用到 `D:\图\Ellipse 58/60.png`,但项目里已经有 `img_houshan1_cloud_58/60.png`(同一张图,09-15 §4 fit-to-natural-bounds 后)。直接 `cp D:\图\*` 是最简单的写法,但**会撤销之前 §4 的 APK 体积决策**。正确做法:**核项目里有没有等价物**,有就复用、只在新资源上 import。原文件大小 ×3 → 项目版 ≈ 0.5×,这次省了 140 KB APK。
- **`private val TWO_PI` 在多个 composable 间复用时,应提到文件级**(新)— §43 我把 `TWO_PI = (2.0 * PI).toFloat()` 放在 `FocusCloudBand` 函数体内(局部 val),§6 新加的 `AnimatedCloudImage` 也要用,引用时就 unresolved → 类型推断连锁失败,报 4 个莫名其妙的错误(都指向 `TWO_PI` 行,但根因是它在文件级不存在)。**复用常量应该提到文件级**,函数体内只放**独占**的逻辑
- **"程序化径向渐变"和"实图 PNG"是两种视觉,不要强合**(新)— §43 FocusCloudBand 是程序化水墨晕染(出图是柔和渐变),§6 AnimatedCloudImage 是实图 PNG(出图是实体水彩云)。两者视觉目标不同——前者"看不见的雾气",后者"看得见的云朵"。虽然 API 几乎一样,共享一个 composable 看起来"整洁",但**用 type 区分两种视觉**反而让阅读者一眼看出"这个是雾,那个是云"。**重复 < 错误抽象**:30 行 ×2 = 60 行,可控;一个 mode 参数 + 文档解释 = 复杂且容易误用

- **只动"周期",不动"幅度/浓度"**(新)— 提速只改 5 个 periodMs,完全没碰 amplitude / baseAlpha / alphaAmp。原因:提速 = "更频繁地完成同样的事",**单次行程不变 → 视觉幅度不变 → 不会因为更快就更压迫**。如果想"更明显但保持平静",只动周期是对的;如果想"更快也更猛",就同时加 amplitude。本轮先按"只动周期"做,留一个干净的旋钮给下一轮
- **二次提速也要守住"互质"承诺**(新)— 第一轮提速(§39)从 53/41/29→ 29/23/17;这一轮从 29/23/17→ 19/17/13。每次提速周期都**变得更小更接近**;但**两两互质**这条不变量要守住——如果哪天三个数都含 13(比如 13/26/39),"13秒"这个拍点就会三团一起闪,立刻看出规律。新周期 19/17/13 与 §43/§44 的 11/9 也都互质,合成周期 4.8 天
- **峰值速度(dp/s)比周期(ms)更直观地告诉用户"快了多少"**(新)— 用户的感受是"快"——是**每秒移动多少 dp**,不是"周期多少秒"。以后报"提速"时,直接给峰值速度对比表比给周期比表更易读。如 §5 那张自检表

- **同一 composable 反复调用比"加新组件"更经济**(新)— §44 没新写任何 composable,就是再调一次 `FocusCloudBand(...)` 换个位置。**§43 当时把它写成参数化的私有 composable,现在 §44 的 +13 行只是"调用 + 调参注释"**——这就是抽象的复利。原则:小屏局部组件**先做参数化,真正需要分化时再分裂**;别一上来就拆 5 个相似组件
- **同一行上的两条飘带,用**互质周期**避"整齐划一"**(新)— §43(19s)+ §44(23s)都是质数,lcm = 19×23 = 437 秒 ≈ 7.3 分钟。**人眼不可能跟踪 7 分钟的拍点重合**,所以视觉上是两条独立的飘带;同时**19s 和 23s 都足够短**保证每条都"动得起来"。互质周期是"局部反复调用"避免机械化叠加的廉价手段
- **labels + 飘带的几何重叠靠 z-order 自动解决**(新)— §44 的飘带 x 范围与拆招心法标签有 86dp 的水平重叠,我**没有**为它单独挪位避让,因为飘带绘制在 content Box 之前、标签是不透明米色图版 → 标签自动遮住飘带的右端,**实际可见的就是"标签左侧"**。这条**依赖于"飘带是雾、标签是不透明板"**,只要 z-order 对、不透明元素在后,就成立。给以后类似"局部动画 + 邻接 UI" 的快速参考

- **多页面"共用同一个背景 → 共用同一个动画"时,要逐页验证动画是否带过去**(新)— 09-15 §18 改后山2 时用户去掉熊猫,后山3 复制基底选错,带过去的是 8f5a28c 的"无动画"基线,§21 的 pandaScale/pandaDy **没跟过去**。后山1/2/3 共用同一个背景是**结构性共同点**,但**动画必须在每页都重新检查**,不能假设"基底页面有的另一页也有"。修完本次让我立刻想到:**还有多少处类似"基底有,某页忘带"的?**(houshan1/2/3 还有 6 朵 Ellipse 云、4 个 label 都是基底页面有,后山3 没去验证过)。要不要在下次大改前做一次**三页同步基线审计**?
- **scaleX/scaleY 的 graphicsLayer 不影响 hit-testing**,所以加呼吸缩放**完全不影响**熊猫周围的 clickable 区域和点击判定。视觉上缩放 0.95~1.05 ±12%,命中区域始终按原 size 计算,手指位置不需要重定位

- **"某个区域没动画"不一定=没覆盖,可能=没视觉锚点**(新)— §42 后 `HoushanMistLayer` 在拆招心法下方已有满宽覆盖,但都是均匀细雾,**没有明显的运动焦点**——叠在深色近山上读起来像山体本身的渐变。**解决方案不是再加浓度(发白)也不是再加团数(更乱),而是在视线集中位置放一个"聚焦元素"**:更大、更浓、节奏更慢、明确的上下浮动。**关键:单一元素 + 慢节奏,比均匀细雾的视觉效果强得多**
- **碎屏布局的"聚焦飘带"应在文件私有**(沿用 09-15 §37)— 与近/中/远三层均匀雾不同,聚焦飘带**强位置相关性**(标签 x/y 不同就无法复用),后山3 与后山1/2 的拆招心法位置不同,所以只放在 `Houshan3Screen.kt` 私有处;后山1/2 需要时另起一处,不强求"一处定义三页复用"

### §17 后山1/2 加聚焦飘带 + 3 朵实图云(与后山3 同款动画)(用户指令)(2026-09-16 上午)

**用户指令**:"把后山1和后山2 页面的动画特效变成和后山3页面的动画一样的效果"

#### Step 1 侦察:后山3 vs 后山1/2 差什么

| 元素 | 后山1 | 后山2 | 后山3 |
|---|---|---|---|
| 背景图 (img_shilian_bg / _2_bg) | ✅ | ✅ | ✅ (试炼转换.png,§10) |
| MistLayer 冷青色 (#A9C3C0) | ✅ (§11) | ✅ (§11) | ✅ (§11) |
| 6 朵老云 (58/61/56/57/60/60b) + 随机飘动 | ✅ | ✅ | ✅ (§35 改名 _old) |
| 拆招心法标签点击 → 第一卷-1 (§22) | ✅ | ✅ | ✅ |
| 标签消费事件(§19) | ❌(标签 2/3/4 无消费)| ✅ | ✅ |
| **FocusCloudBand × 2**(中下+左下,§43/§44)| ❌ | ❌ | ✅ |
| **AnimatedCloudImage × 3**(实图云 58/60/62,§6-§15)| ❌ | ❌ | ✅ |
| 沉浸式过渡 dolly-in (§36) | ❌ | ✅ | ❌ |
| 后山3 熊猫 + 御剑动画 (§3/§21) | ✅ | ❌(§18 去掉)| ✅ |

**核心缺失**:后山1 和后山2 都缺 **5 个聚焦云元素**(2 FocusCloudBand + 3 AnimatedCloudImage)。

#### Step 2 设计:提到 shared components

§6 沉淀说"重复 < 错误抽象",3 个页面 × 2 个 helper = **6 份重复**触发抽象化阈值降低。
**提到 shared**,避免 3 份 80+50=130 行的复制:

- `ui/components/FocusCloudBand.kt`(新)—— 从 Houshan3Screen.kt 的 private fun 提升为 public fun
- `ui/components/AnimatedCloudImage.kt`(新)—— 同上

两个文件的 `TWO_PI` 提到各自文件级 private val。
FocusCloudBand 内部颜色硬编码 `#A9C3C0`(项目 §11 §23-§26 沿用色)。
AnimatedCloudImage 不带颜色(画 PNG 原色)。
抖动参数固定(周期 1900ms / ±3 dp X / ±1.5 dp Y,§15 稳定值)。

#### Step 3 后山3 修改:把两个 private fun 删掉,改用 import

Houshan3Screen.kt 中两个 `private fun FocusCloudBand` 和 `private fun AnimatedCloudImage` 都删除,改 `import com.jueqiao.jianghu.ui.components.{FocusCloudBand,AnimatedCloudImage}`。
删除文件级 `private val TWO_PI`(已无引用)。
KDoc 迁移到 shared 文件,§43/§44/§6-§15 各 section 改用简短指向。

#### Step 4 后山1/2 加调用(位置参数适配各自拆招心法)

后山1/2 的拆招心法完全相同(offset(168, 345) size(74, 131)):
- 底边 Y = **476**(后山3 是 691,**差 -215dp**)
- center X = **205**(后山3 是 172,**差 +33dp**)

**FocusCloudBand × 2**:

| | 后山3 | 后山1/2 |
|---|---|---|
| 中下 x_offset | 12 | **45**(center x 205 - width/2 160) |
| 中下 y_offset | 740 | **525**(底边 476 + 49dp gap) |
| 左下 x_offset | -30 | **-30**(同 — 标签 1 位置 后山1/2 与 后山3 一致) |
| 左下 y_offset | 740 | **740**(同) |

**AnimatedCloudImage × 3**(周期同后山3 = 13/17/23s 互质):

| 云 | 后山3 x,y | 后山1/2 x,y | 注释 |
|---|---|---|---|
| 58 | -60, 703 | **-27, 488** | "距拆招心法 X 距离 = 后山3 距离" → 后山1 cx=205-112=93 → x_offset=93-120=-27;y_offset 同上 -215 |
| 60 | 120, 686 | **153, 471** | cx=205+88=293 → x_offset=293-140=153 |
| 62 | 50, 771 | **83, 556** | cx=205-2=203 → x_offset=203-120=83 |

#### Step 5 后山2 的 z-order:放在景深平面 2(云平面)

后山1 没有 dolly 过渡,直接放在 MistLayer 之后、内容层之前就行。

**后山2 有 §36 dolly 过渡**:3 个景深平面(背景/云/标签)各自缩放和淡出。
**FocusCloudBand 和 AnimatedCloudImage 必须放在"云平面"里**,跟随 6 朵云一起 scaleX/Y=cloudScale 和 alpha=cloudFade,
否则 §36 过渡时飘带留在屏幕上不动,**视觉上脱节**。

```kotlin
// 后山2:放在景深平面 2 之内
Box(
    modifier = Modifier.fillMaxSize().graphicsLayer {
        scaleX = cloudScale; scaleY = cloudScale
        transformOrigin = focal
        alpha = cloudFade
    }
) {
    HoushanMistLayer()
    FocusedCloudBand(...)  // 新加
    FocusedCloudBand(...)  // 新加
    AnimatedCloudImage(...) // 新加 (x3)
    Image(painter = ..., /* 6 朵老云 */)
    ...
}
```

#### Step 6 编译/安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 4s
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 14s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt`(+2 import,新增 cloudProgress 状态,5 个 animation 调用)
- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt`(+2 import,新增 cloudProgress 状态,5 个 animation 调用;**放在景深平面 2 里,与云朵一起 fade**)
- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan3/Houshan3Screen.kt`(删除 2 个 private fun,改 import,移除文件级 TWO_PI)
- `?? android/app/src/main/java/com/jueqiao/jianghu/ui/components/FocusCloudBand.kt`(新)
- `?? android/app/src/main/java/com/jueqiao/jianghu/ui/components/AnimatedCloudImage.kt`(新)

### §18 云朵动画再调整 — 振幅大幅加大,周期拉长(用户"速度再快+频率小+振幅大")(2026-09-16 上午)

**用户指令**:"移动的速度再快一些,频率小一些,振幅大一些"

#### 解读:三个参数在"维度空间"里的位置

| 用户说 | 维度 | 解读 |
|---|---|---|
| "速度再快一些" | 视觉速度(单次循环距离)| "振幅大"= 看起来飞得更远,**不要解释为周期变短** |
| "频率小一些" | 频率 = 1/周期 | 频率小 = 周期大 = 慢 |
| "振幅大一些" | 振幅 | 单次循环距离大 |

**矛盾化解**:"速度"≠"频率",而是与"振幅"同维。"振幅大 + 周期大"= **单次循环距离大幅增加,但循环频率降低** = "深长深呼吸"风格。

#### §18 实际数字(3 个页面共用同一组 §18 参数)

| 参数 | 旧 | **新** | 变化 |
|---|---|---|---|
| FocusCloudBand 抖动周期 | 1900ms | **3000ms** | 0.53 → **0.33 Hz**(频率小) |
| FocusCloudBand 抖动 X 振幅 | ±3 dp | **±4.5 dp** | +50% |
| FocusCloudBand 抖动 Y 振幅 | ±1.5 dp | **±2.5 dp** | +67% |
| 云 58 周期 | 7000ms | **11000ms** | +57% |
| 云 58 漂移 X 振幅 | 25 dp | **38 dp** | +52% |
| 云 58 漂移 Y 振幅 | 10 dp | **15 dp** | +50% |
| 云 60 周期 | 11000ms | **17000ms** | +55% |
| 云 60 漂移 X 振幅 | 30 dp | **45 dp** | +50% |
| 云 60 漂移 Y 振幅 | 5 dp | **8 dp** | +60% |
| 云 62 周期 | 13000ms | **19000ms** | +46% |
| 云 62 漂移 X 振幅 | 27 dp | **40 dp** | +48% |
| 云 62 漂移 Y 振幅 | 7 dp | **10 dp** | +43% |
| FocusCloudBand 中下周期 | 11000ms | **17000ms** | +55% |
| FocusCloudBand 中下 X/Y | 40/28 dp | **60/42 dp** | +50% |
| FocusCloudBand 左下周期 | 9000ms | **14000ms** | +56% |
| FocusCloudBand 左下 X/Y | 35/24 dp | **52/36 dp** | +49% / +50% |

#### 互质性自检

新周期 11/17/19s 仍互质(都是质数)。
合成周期 11×17×19 = 3553 秒 ≈ **59 分钟** → 远超人眼可跟踪范围。
加上 3000ms 抖动周期,gcd(11000, 3000) = 1000ms,合成周期 = 33000ms = **33 秒**(可接受)。

#### 振幅 vs 周期 vs 峰值速度的代数关系

`峰值速度 = 2π × 振幅 / 周期`。振幅和周期**等比例 ↑** → 峰值速度**几乎不变**:
- 例:FocusCloudBand 中下 旧 2π×40/11 ≈ **22.8 dp/s**,新 2π×60/17 ≈ **22.2 dp/s**(几乎相同)
- 所以**不是"加快"而是"加深"** —— 速度一样,但每次循环飞得更远 → 看起来更明显

#### 沉淀(顺带):"速度"和"频率"看似矛盾时的处理

- "速度" 在日常语言里常指**视觉速度**(单次循环距离 = amp × 步数),不是物理公式速度
- "频率" 才是严格周期频率
- 用户用"速度"+"频率"矛盾描述时,**先把"速度"翻译为"视觉明显度"**,把矛盾化解为"amp↑ + period↑"
- 这种组合的视觉:**单次循环更显眼 + 整体节奏更从容** = "深长深呼吸"风格
- vs 之前 §15 "amp↑ + period↓" = "急促抖"风格

#### 编译/安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 11s
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 11s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M ui/components/FocusCloudBand.kt`(jitter hardcoded 1900→3000ms, ±3→±4.5, ±1.5→±2.5)
- `M ui/components/AnimatedCloudImage.kt`(jitter hardcoded 同上)
- `M Houshan3Screen.kt`(3 个云朵周期 + 2 个 FocusCloudBand 周期和振幅 + 3 个 AnimatedCloudImage 振幅)
- `M Houshan1Screen.kt`(cloudProgress 周期 + 2 个 FocusCloudBand + 3 个 AnimatedCloudImage 振幅)
- `M Houshan2Screen.kt`(cloudProgress 周期 + 2 个 FocusCloudBand + 3 个 AnimatedCloudImage 振幅)

### §19 横向摆动提速到"4 秒跑完屏宽"(用户指令)(2026-09-16 上午)

**用户指令**:"在左右移动的速度上加快速度,时间大概要4秒左右可以跑完屏幕的宽度"

#### 参数推导(从需求反解数学)

sin 摆动:`x(t) = A × sin(2πt / T)`
- **单程**(从一端 -A 到另一端 +A)距离 = **2A**,耗时 = **T/2**
- 用户要"4 秒跑完屏宽"→ `T/2 = 4s` → **T = 8s**
- "屏宽"= 设计稿 412dp → `2A = 412` → **A ≈ 200dp**

**校验平均速度**:`4A / T = 4 × 200 / 8 = 100 dp/s` → 400dp 走完正好 **4 秒** ✓

#### 改了什么

| 元素 | 旧 | **新** |
|---|---|---|
| 云 58 周期 | 11s | **7s**(单程 3.5s) |
| 云 60 周期 | 17s | **8s**(单程 4s) |
| 云 62 周期 | 19s | **9s**(单程 4.5s) |
| FocusCloudBand 中下周期 | 17s | **8s** |
| FocusCloudBand 左下周期 | 14s | **9s** |
| **所有 5 个元素的 amplitudeX** | 38/45/40/60/52 | **全部 200dp** |
| amplitudeY | 15/8/10/42/36 | **不变**(用户只提左右) |
| 抖动(shared 组件) | 3000ms / ±4.5 / ±2.5 | **不变** |

**"全部 200dp"的理由**:用户说的是"跑完屏幕的宽度"——是**距离**规格,不是速度规格。
只有振幅 ≈ 屏宽的一半时,单次摆动才真的覆盖屏宽,所以 5 个元素的横向振幅统一为 200dp。

#### 周期为什么取 7/8/9 而不是都用 8

- 用户说"**大概** 4 秒",3.5/4/4.5s 都在"大概"范围内
- 7/8/9 **两两互质**(gcd(7,8)=gcd(7,9)=gcd(8,9)=1),避免 5 个元素完全同步
- 若全用 8s,则所有元素同频,靠已有的 phase(0.13/0.31/0.71)错开——只有 3 朵云有 phase,2 个 FocusCloudBand 没有,
  所以会用"同频同相"完全重叠,视觉上像"一个元素"。7/8/9 是更稳的做法

#### 峰值速度对比(体现"加快"了多少)

| 元素 | §18 峰值 | **§19 峰值** | 倍数 |
|---|---|---|---|
| 云 58 | 2π×38/11 = 21.7 dp/s | **2π×200/7 = 179 dp/s** | **8.3×** |
| 云 60 | 2π×45/17 = 16.6 dp/s | **2π×200/8 = 157 dp/s** | **9.5×** |
| 云 62 | 2π×40/19 = 13.2 dp/s | **2π×200/9 = 140 dp/s** | **10.6×** |
| FocusCloudBand 中下 | 2π×60/17 = 22.2 dp/s | **2π×200/8 = 157 dp/s** | **7.1×** |
| FocusCloudBand 左下 | 2π×52/14 = 23.3 dp/s | **2π×200/9 = 140 dp/s** | **6.0×** |

整体峰值速度提升 **6-10 倍**。这就是"4 秒跑完屏宽"的量级。

#### 编译/安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 10s
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 11s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(3 个云朵周期 11/17/19 → 7/8/9;2 个 FocusCloudBand 振幅→200 + 周期→8/9;3 个 AnimatedCloudImage 振幅→200)
- `M Houshan1Screen.kt`(同上)
- `M Houshan2Screen.kt`(同上)

## 沉淀(新)

- **"X 秒跑完屏宽"是可反解的数学规格**(新)— 遇到"多久跑完多宽"这类**时空复合规格**,
  可以直接从 sin 运动学反解:`单程距离 = 2A`,`单程时间 = T/2` →
  **`A = 屏宽 / 2`,`T = 2 × 目标秒数`**。这类规格比"快一点"精确得多,应该抓住并转成参数,
  不要凭感觉调
- **"距离规格"和"速度规格"要分清**(新)— 用户说"跑完屏幕的宽度"是**距离**规格(振幅要覆盖屏宽),
  "4 秒"是**时间**规格(半周期)。两者合起来才确定 A 和 T。
  如果只按"速度"理解(比如 `100 dp/s`)而保留小振幅,会得到 2-3 秒的极高频率抖动,
  与"云在飘"的设计意图不符
- **周期取多个互质值而非同一个值,能避免"多看一个元素"的错觉**(新)— 若所有元素同频,
  只有带 phase 参数的能错开;没有 phase 参数的元素会同频同相完全重叠。
  **给一组"约等于目标值"的互质数(7/8/9)比给同一个精确值更稳**

Co-Authored-By: Claude Code <noreply@anthropic.com>

- **"重复 ≥ 3 次 + 跨 ≥ 2 个文件 = 抽"**(跨文件阈值比同文件低,新)— §6 沉淀的"重复 < 错误抽象"针对的是**同文件内** 3+ 次调用,
  阈值明确(同文件 3+ 次都该抽)。但 §17 的情况是**跨文件** 6 份重复(2 helper × 3 页面),阈值应该更低:
  - **同文件 3+ 次 = 抽**(§6 的标准)
  - **跨 2+ 文件 = 抽**(本次新加的阈值)
  - 跨文件重复的隐性成本高:改一处要同步改 N 个地方,**容易漏**,漏了就行为不一致
- **z-order 与 dolly 平面层叠不能分开考虑**(新)— 后山2 的 §36 dolly 过渡是"3 个景深平面各自缩放和淡出",
  新加的云元素**必须放进某个景深平面**(选平面 2 = 云平面)而不是放外层 Box,否则飘带和实图云会留在屏幕上不动,
  与 6 朵老云的 fade 不同步。**"动画"不仅指"持续循环动效",还包括"在过渡中正确淡入淡出"**;
  后山1 没这个顾虑(没 dolly),后山2 必须考虑
- **Kotlin 扩展函数要显式 import**(新)— `DrawScope.scale(...)` 是扩展函数,DrawScope 是
  `onDrawBehind { }` 的 receiver 类型;但 Kotlin 编译器**不自动 import 扩展函数**——必须显式
  `import androidx.compose.ui.graphics.drawscope.scale`,否则编译报 `Unresolved reference 'scale'`。
  从 Houshan3Screen.kt 提取到 FocusCloudBand.kt 时漏了这个 import → 第一次编译挂掉。教训:**复刻带 onDrawBehind/onDrawWithContent 的代码时,
  扩展函数 import 要逐个核对**(编译器不会提示"忘了 import 扩展函数")
- **沉淀会"过期"**(新)— 此前 §37 沉淀说"碎屏布局的'聚焦飘带'应在文件私有,后山1/2 需要时另起一处",
  这是§17 之前的判断。**但**今天 §17 直接证伪了这个沉淀:后山1/2 不仅"另起一处",
  而且**第二个文件**复制 → §6 的"重复 < 错误抽象"阈值被触发 → 应该提到 shared。
  沉淀不是金科玉律,会随着代码演进而**反向推翻**。建议:每次新增跨页面改动时,回看 §6 沉淀
  (重复阈值)和 §37 沉淀(组件放哪),**看是否要重写**
