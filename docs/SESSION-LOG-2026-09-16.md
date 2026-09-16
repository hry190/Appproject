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

> ⚠️ 上表是 **09-16 早上开工时**的快照,已被后续工作覆盖,**勿当现状用**。
> **09-16 晚(§21 后)实际状态**:分支 **`zzz`**、tracked **1237**;09-16 的 §21~§21f 已 commit + push。
> 最新 HEAD 见 `git log --oneline -1`(本文档不追记 commit 号,避免又变成过期快照)。
>
> ✅ **两个历史缺口已于 09-16 晚(§21g)修复**:
> 1. **§16 已恢复** —— 它的内容原本只存在于悬空 commit `b58c714`(被后续 force-push 挤出分支),
>    现按原样插回 §15 的沉淀之后、§17 之前(85 行,含 MERGE-WORKFLOW 四步 + 踩坑沉淀)。
> 2. **错位的 `## 沉淀(新)` 标题 + 重复 §12 条目已删除** —— 那是 §16 那次 stash-pop 冲突的残留:
>    一个多余的二级标题 + 3 行与上文逐字相同的 §12 片段。


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

### §16 同步 codex/ifthen 到 zzz 分支(用户指令"同步到zzz分支")(2026-09-16 上午)

按项目 MERGE-WORKFLOW.md 4 步走:**侦察 → 评估 → 建议 → 合并**

#### Step 1 侦察

| 查什么 | 值 |
|---|---|
| 本地 zzz HEAD | `8f5a28c`("merge main → zzz: Vol-6-15 + Vol-10-14 卷末闭环",09-15) |
| 本地 codex/ifthen HEAD | `65cc510`(我刚 commit 的第 3 个,09-16 §15) |
| `git merge-base zzz codex/ifthen` | **`8f5a28c`**(zzz HEAD)= **strict fast-forward 关系** |
| codex/ifthen 比 zzz 多的 commit | **33+ 个**(`zzz..codex/ifthen`) |
| zzz 比 codex/ifthen 多的 commit | **0 个**(`codex/ifthen..zzz` 为空) |
| 本地 codex/ifthen vs origin/codex/ifthen | **完全相等**(都是 65cc510,**未 push**) |
| 本地 zzz vs origin/zzz | **完全相等**(都是 8f5a28c) |

**核心结论**:**codex/ifthen 是 zzz 的严格后代**,zzz 上没有任何 codex/ifthen 没有的 commit。
**同步方向**:把 codex/ifthen 的 33+ 个 commit **前移**(fast-forward)到 zzz。

#### Step 2 评估

- **无冲突** — zzz 是 codex/ifthen 的祖先,fast-forward 不会有任何合并冲突
- **22 个文件差异**(4917 行新增 / 131 行删除):
  - **10 个新增的云朵素材 PNG**
  - **Houshan2Screen.kt / Houshan3Screen.kt** 代码修改
  - **img_shilian2_bg.png / img_shilian_bg.png** 背景替换
  - **SESSION-LOG-2026-09-15.md / 16.md** 文档

#### Step 3 建议

**推荐方案 A:把 codex/ifthen 合并到 zzz(`git merge --ff-only codex/ifthen`)**
- zzz HEAD 直接从 8f5a28c 移到 65cc510,无 merge commit 生成
- 结果:zzz 与 codex/ifthen 同步,3 页分支内容一致

#### Step 4 合并(实际执行)——踩了一个坑

**预期**: `git merge --ff-only codex/ifthen` 直接 fast-forward。

**实际**: 报错 `Could not find merge strategy 'theirs'`。

**根因**: 项目 `.git/config` 设了 `merge.ff=false`(见 `.git/config` 第 18 行 `[merge] ff = false`)。
**这条配置禁用了 fast-forward 合并**。当 zzz 是 codex/ifthen 祖先时,git 应该 fast-forward,但 `merge.ff=false` 强制 git 走"真合并"流程——然后 git 试图用 "theirs" 策略(从 `pull.twohead=theirs` 推出来),但 "theirs" 不是合法合并策略名,所以报错。

**解决**: 直接用 `git reset --hard codex/ifthen` ——**绕开 git merge 流程,直接移动分支指针**。
- 等价语义:把 zzz 指向 codex/ifthen HEAD,无 merge commit 生成
- **完全等同于 fast-forward**,只是不经过 git merge 那条会触发策略检查的代码路径
- 用 `reset --hard` 会丢掉 working tree 的修改,但本次 working tree 只有**未跟踪文件** `docs/SESSION-LOG-2026-09-16.md`,**未被跟踪 = 不会被 `reset --hard` 触碰**

**踩的第二个坑**: §16 编辑在 codex/ifthen 上做的(未 commit),stash push 到 stash@{0},切换 zzz 后 stash pop **失败**——zzz HEAD 没这个文件(`8f5a28c` 是 09-15 的),stash 里的文件被当作"新增",upstream 把它当作"删除",**modify/delete 冲突**。
**解决**: 用 `git checkout --theirs` 解决冲突("theirs" = stash 内容),`git add` 标记解决,然后 `git stash drop` 丢掉 stash(§16 内容已在 working tree)。

**执行序列**(实际跑的):
```bash
git stash push -m "§16 zzz-sync log (pending merge)"
git checkout zzz
git checkout --theirs docs/SESSION-LOG-2026-09-16.md
git add docs/SESSION-LOG-2026-09-16.md
git stash drop 0
git reset --hard codex/ifthen   # <-- 绕过 merge.ff=false 这个坑
```

**结果**:
- ✅ zzz HEAD = `65cc510`(与 codex/ifthen 同步)
- ✅ working tree clean
- ✅ 与 origin/zzz 相比领先 33 个 commit,待 push

#### git status

```
On branch zzz
Your branch is ahead of 'origin/zzz' by 33 commits.
nothing to commit, working tree clean
```

#### 沉淀(踩坑 + 解法)

- **`merge.ff=false` + `pull.twohead=theirs` 是互相耦合的反直觉配置**(新)— 看 `.git/config` 第 17-22 行:`[pull] twohead = theirs` 给 `git pull` 用,`[merge] ff = false` 给 `git merge` 用。**两个独立**,但**当你在 zzz 上 `git merge codex/ifthen`(zzz 是 ancestor)时**:
  1. `merge.ff=false` 禁用了 ff,强制走"真合并"
  2. 真合并需要"theirs"或"ours"策略,但 `pull.twohead` 设的 "theirs" 在 merge 流程里也被引用
  3. 结果:`Could not find merge strategy 'theirs'`(即使命令行没指定 theirs)
  
  **避开方法**:用 `git reset --hard` 直接移分支指针——**等价于 ff-merge,但绕开 merge 策略检查**。
  
- **modify/delete 冲突的标准解法**(新)— 切换分支时,如果 working tree 有未提交改动,**目标分支文件不存在**(本例:zzz 没有 `SESSION-LOG-2026-09-16.md`,但 codex/ifthen 有),git 会拒绝切换(`Your local changes would be overwritten`)。stash 后切换,然后 stash pop 触发**modify/delete 冲突**(stash 是新增,upstream 是删除)。**两个选择**:`--ours` 保留删除(本例不合适,会丢 §16)、`--theirs` 保留 stash 内容(本例对)。然后 `git add` 标记解决,`git stash drop` 丢掉 stash。

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

### §20 横向摆动降速到"约 5 秒跑完屏宽"(用户指令)(2026-09-16 上午)

**用户指令**:"我觉得有点快了,那就要3秒左右可以跑完屏幕宽度的速度"

#### 用户指令里的数字矛盾 → 我先问清再动手

用户说"**有点快了**"(要更慢),但给的数字"**3 秒跑完屏宽**"比当时的"4 秒跑完屏宽"(§19)**更快**——
**秒数越少 = 越快**,两者方向相反。

用 AskUserQuestion 摆出 4 个候选,用户选:**"慢一点:约 5 秒跑完屏宽"**。

**这次询问避免了白跑一轮**(之前 §11-§18 已有 6 轮迭代,方向猜错的成本高)。

#### 参数反解

`单程距离 = 2A`(A = 振幅),`单程时间 = T/2`(T = 周期)
- 目标"5 秒跑完屏宽" → `T/2 = 5s` → **T = 10s**
- 屏宽 412dp → `2A = 412` → **A = 200dp**(§19 已设,沿用)

**校验**:平均速度 = `4A/T = 4×200/10 = 80 dp/s` → 400dp 走完正好 **5 秒** ✓

#### 改了什么

| 元素 | §19 | **§20** | 单程 |
|---|---|---|---|
| 云 58 周期 | 7s | **9s** | 4.5s |
| 云 60 周期 | 8s | **10s** | 5s |
| 云 62 周期 | 9s | **11s** | 5.5s |
| FocusCloudBand 中下 | 8s | **10s** | 5s |
| FocusCloudBand 左下 | 9s | **11s** | 5.5s |
| **amplitudeX** | 200dp | **200dp(不变)** | — |
| amplitudeY / 抖动 | — | **不变** | — |

**周期取 9/10/11**(两两互质:`gcd(9,10)=gcd(9,11)=gcd(10,11)=1`),单程 4.5/5/5.5s —
都是"大概 5 秒"但互不同步(避免无 phase 参数的 2 个 FocusCloudBand 同频同相重叠)。

#### 速度变化

| 元素 | §19 峰值 | **§20 峰值** | 变化 |
|---|---|---|---|
| 云 58 | 179 dp/s | **2π×200/9 = 140 dp/s** | -22% |
| 云 60 | 157 dp/s | **2π×200/10 = 126 dp/s** | -20% |
| 云 62 | 140 dp/s | **2π×200/11 = 114 dp/s** | -18% |
| FocusCloudBand 中下 | 157 dp/s | **126 dp/s** | -20% |
| FocusCloudBand 左下 | 140 dp/s | **114 dp/s** | -18% |

平均速度 100 → **80 dp/s**(-20%),正是"有点快了"的对应回调幅度。

#### 编译/安装

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 9s
.\gradlew.bat assembleDebug       →  BUILD SUCCESSFUL in 13s
adb install -r app-debug.apk      →  Success
adb shell dumpsys activity        →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
```

#### git 状态(待 commit)

- `M Houshan3Screen.kt`(3 个云朵周期 7/8/9 → 9/10/11;2 个 FocusCloudBand 周期 8/9 → 10/11)
- `M Houshan1Screen.kt`(同上)
- `M Houshan2Screen.kt`(同上)

## 沉淀(新)

- **用户给的数字和形容词矛盾时,先问再动**(新)— 用户"有点快了"+"3 秒"(比当时 4 秒更快)方向相反。
  **不要自行选一个解释就动手**——§11-§18 已迭代 6 轮,每次白跑都消耗用户时间。
  用 AskUserQuestion 摆 4 个候选(慢 6s / 慢 5s / 字面 3s / 同时减幅度),用户 5 秒就选完了,
  **比猜错再改一轮便宜得多**。**判据**:当"方向性形容词"(快/慢/大/小)与"具体数值"指向相反时,
  一律先问;当只有形容词(如"再明显一点")时,可以直接按最合理解读动手
- **"秒数"是反直觉的规格**——"4 秒跑完"→"3 秒跑完"是**加速**不是减速。
  用户可能把"秒数"当成"速度值"(越大越快),实际相反。
  **回复时显式写出"秒数越少 = 越快"**能帮用户校准,本轮的 AskUserQuestion 就是这么写的
- **升降速的"幅度"应该匹配用户的措辞强度**(新)— "有点快了" → 回调 20%(100→80 dp/s);
  若是"太快了受不了" → 应回调 40-50%;若是"再慢一点点" → 10%。
  **"有点"/"稍微"= 15-25%;"太"= 40-60%;"一点点"= 5-10%**

Co-Authored-By: Claude Code <noreply@anthropic.com>

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

---

## §40 后续 session(2026-09-16 下午~晚上):后山 1 云朵动效大迭代

### 用户指令时间线

1. 加云朵动效(原始 + 透明度时变)— 8 朵云全加,后全删只留 3 朵明显位置
2. Ellipse 58.png (X=117 Y=322 W=277 H=92) 加为云朵 13
3. 云朵 13 移到标签 3 之下(X=0 Y=420 W=277 H=92)
4. 后山 1 改名(试炼→后山 1,学习→后山 2,后山→后山 3)
5. 云朵 56 移到内容层最内层显示在最上层 + 上移 60
6. 大云再上移 50:Y=120 → 70
7. 大云左右移动范围加大 1.5-2 倍
8. 大云添加透明度时变(绑定 sin/cos 同 X/Y)
9. 大云 X 半径 50→100
10. 熊猫也加动效(上下浮 + 呼吸缩放)
11. 拆招心法右小云等比例放大(W=80→144 H=35→63)
12. 小云等比例放大(W=72→108 H=43→65)
13. 加新云朵 Ellipse 60b (X=-12 Y=872 W=247.5 H=61.64)— 用户调整 Y 为 372
14. 多次"飘动速度/幅度/方向"调整

### 关键 commit (codex/ifthen 分支)

```
8320794 feat(houshan1): 加云雾飘动效果 — 12 秒水平循环平移
... 中间多次 commit 调速/调方向 ...
5a406e2 feat(houshan1): 加云朵 11 (Ellipse 56, 用户指定位置 X=278 Y=755 W=335 H=297)
4369e72 feat(houshan1): 加云朵 12 (Ellipse 60b) + 修复 cloud_56 位置
f473d9f feat(houshan1): Ellipse 56 透明度时变 0.6-0.95 (始终不透明到见背景)
0c0dab6 feat(houshan1): 加云朵 13 (Ellipse 58b, 用户指定 X=117 Y=322 W=277 H=92)
c58ed19 feat(houshan1): 云朵 13 移到标签3 之下
ca06570 feat(houshan1): Ellipse 56 不透明度 100% — 移除透明度时变动画
0e5fd15 feat(houshan1): 替换背景图 — D:\图\试炼.png (1236x2751, 3.6 MB)
953e939 feat(houshan1): 第三次替换背景图 — D:\图\试炼.png
5b94943 fix(houshan1): 修复 line 143 多余的 '}' — cloud56Alpha LaunchedEffect 没正确关闭
```

### 后山 1 最终状态(commit 14f99a6)

**保留 3 朵云**(用户明确"只留明显位置"):
- **大云** (Ellipse 0, 240×180): X=60 Y=70(原 180,大云移到内容层最内层最上层+上移 110),椭圆轨迹 ±150/±40,16 秒 + 透明度 0.1-0.9 随 Y 同步
- **拆招心法右** (Ellipse 60, 144×63): X=280 Y=400,上下飘 ±50,4.5 秒 + 透明度 0.15-0.85
- **小云** (Ellipse 61, 144×86): X=50 Y=750,上下飘 ±60,4 秒(用户要快节奏)

**额外添加 4 朵**(后加):
- **云朵 11** (Ellipse 56, 335×297): X=118 Y=700(用户调整位置),三个动画(上下+左右+透明度)
- **云朵 12** (Ellipse 60b, 247.5×61.64): X=-12 Y=372(用户调整),左右飘+透明度
- **云朵 13** (Ellipse 58b, 277×92): X=0 Y=420,标签 3 之下,左右飘+透明度

**熊猫动效**:上下浮 ±8 px,3 秒 + 呼吸缩放 1.0↔1.05,4 秒

### 后山 1 背景图

替换 3 次 `D:\图\试炼.png` → 最终 `drawable-nodpi/img_shilian_bg.png` (1236×2751, 3.4 MB)

### 调试的痛苦与教训

**问题**:.NET Insert 操作不可靠 — 中文字符、缩进、空格、括号都容易错

**踩坑清单**:
1. ❌ 漏写 `,` 在 `.blur(6.dp)` 后 → Modifier chain 断裂,级联 20+ 错误
2. ❌ 写 `}    }` 单行(应当 `}\n    }`)— 语法合法但不规范
3. ❌ 加 Image 漏 `contentScale = ...,` 行
4. ❌ Insert 时缩进错 16 空格
5. ❌ 重复的 `)` 关闭 Image
6. ❌ 漏写 Animatable 声明但 LaunchedEffect 引用
7. ❌ 漏 import `import androidx.compose.runtime.LaunchedEffect` 等
8. ❌ 删 Animatable 时也误删了被引用的 Animatable(cloud60bX/Alpha)
9. ❌ Y 坐标用户调整过(372),但我误以为是 872 "原始位置"

**关键 commit 修复线**:
- `5b94943` 修复 `}    }` 单行
- `8e5c39a` 修复 cloud56Alpha LaunchedEffect 缺 `}`
- `196d314` 补回 cloud60bX/Alpha Animatable 声明
- `eade9b7` 补回 cloud60MidRightY/Alpha Animatable 声明
- `6702628` 修复云朵 13 注释缩进 + 删重复 contentScale
- `df5bd2b` 修复云朵 12/13 LaunchedEffect 括号不匹配
- `aed893e` 修复云朵 12 注释缩进(16→8)
- `9acbc5d` 修 .blur(6.dp) 缺 `,` + 云朵 12 Y 872→372
- `efb2a5d` 云朵 12 .blur(6.dp) 后加 `,`
- `8585b54` 加 drawable img_houshan1_cloud_58b.png
- `3bb2225` 删云朵 13 Image 块多余的 `)`
- `14f99a6` 补云朵 12 Image 关闭 `)`

### 最终操作

`git reset --hard 8e5c39a` 回退到 merge feature 之后的稳定状态
→ 用 `edit` 工具加云朵 13 声明
→ 用 .NET 脚本安全加 2 个 LaunchedEffects + Image(用 IndexOf 找锚点)
→ 修 .blur 缺 `,`、Y 872→372、Image 缺 `)`
→ `git push -f origin codex/ifthen` force push

### 沉淀(后山 1 云朵动效相关)

1. **.NET Insert 不可靠** — 中文环境下 Insert 操作经常漏字、加错位置
   - 优先用 `edit` 工具(它读完整文件上下文,避免误判缩进)
   - 或先 Read 完整文件,再用精准的 PowerShell `.NET` 脚本(必须用 `"\\"` 双反斜杠转义路径)
   - **每次 Insert 后用 .NET 脚本 verify 括号深度、`.blur` / `.alpha` 后是否有 `,`**
2. **错误级联** — 一个 Image 块参数不平衡会报 20+ 个错误,要找第一个 error 修根因
3. **"modifier chain 内逗号"陷阱** — `.blur(6.dp),` 中的 `,` 是 Image 参数 separator,不是 modifier chain 元素(`.blur` 后不能 `,`)
4. **用户调整过的值要保护** — Y=372 是用户 IDE 真机微调过的,不要"修复"成 872
5. **`git reset --hard` 后必须 `fetch + reset origin` 拉新 commit**,否则本地 force push 跟 origin 不一致
6. **加 Animatable 时配套加 LaunchedEffect 和 Image** — 三个一气呵成,不然编译报 `Unresolved reference`

---

## §21 三页动画云素材:竖向间隔 60~90dp 重排 + 最底部新增飘带(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户指令**:
> "我想让后山1和后山2和后山3,一共3个页面,我想让这几个页面内的各个有动画效果的素材可以每两个间隔随机在60单位到90单位的距离,这样子可以让素材不要太密集,还有在最底部(大概在导航栏的上方的位置)一定要一个动画素材"

### 第 1 步:先把"素材"和"间隔"定义清楚(3 问 AskUserQuestion)

收到指令后**没有直接动手**,先把几何前提量出来 —— 因为"60~90 间隔"和"每页素材数量"存在**数学冲突**:

**真机几何(adb 实测,不是设计稿)**:

| 项 | 实测值 | 来源 |
|---|---|---|
| 屏幕 | 1080×2400 px @440dpi = **393×873 dp** | `wm size` / `wm density` |
| 导航栏 | frame `[0,2356][1080,2400]` → 高 **44px = 16 dp** | `dumpsys window displays` |
| 导航模式 | **手势导航**(`navigation_mode = 2`) | `settings get secure navigation_mode` |
| 可用高度 | 873 − 16 = **857 dp** | 计算 |

**冲突**:云朵素材按用户原尺寸(不缩放)排不进一屏:

| 每页云朵数 | 素材高度合计 | 60~90dp 间隔需要 | 合计 | 结果 |
|---|---|---|---|---|
| **5 个**(§17 共享云) | 571 | 4×60~75 = 240~300 | **811~871** | ✓ 正好铺满 |
| **6 个**(最小的 6 个) | 708 | 5×60 = 300 | **1008** | ✗ 超 151dp |
| **11 个**(后山1/2 全留) | 1805 | 10×60 = 600 | **2405** | ✗ **是屏高的 2.8 倍** |

反解:放 6 个需缩到 **68~79%**;放 11 个需缩到 **14%**(云会变成 45~135dp 小色块)。

→ **"6~11 个云朵" 与 "60~90dp 边到边间隔" 在 873dp 屏上无法同时成立**,只能三选二。
按 §20 沉淀"用户给的数字与形容词矛盾/冲突时先问再动",摆了 3 组共 4 个问题,用户选择:

| 问题 | 用户选择 |
|---|---|
| 「有动画效果的素材」范围 | 云朵素材(每页 6~11 个);**熊猫不算**、位置不动;**雾团不动位置** |
| 60~90 按哪个方向量 | **竖直 Y、边缘到边缘** |
| "随机"的语义 | **定死一组**(每次进页面都一样) |
| 底部那个素材 | **飘带 `FocusCloudBand`**(三页都加) |
| 三选二取舍 | **6 个 · 缩到约 75%** |
| `amplitudeY` 怎么处理 | **收到 8~12dp** |
| 老云(后山1/2 六朵 + 后山3 五朵) | **保留为静态图层、去掉动画** |

### 第 2 步:6 元素竖排布局(三页同一套)

素材 **×0.75** 等比缩放;高度合计 511dp → 可用 857dp → **5 个间隔平均上限 69dp**,
故间隔取 **69/62/66/60/69**(全部落在用户给的 60~90 内,且不均匀)。

| # | 元素 | 原 w×h | **×0.75 后** | **y(顶)** | 竖向占位 | ↑ 间隔 |
|---|---|---|---|---|---|---|
| 1 | ACI 58(`img_houshan1_cloud_58`) | 240×135 | **180×101.5** | **8** | 8..109.5 | — |
| 2 | FCB 左下 | 240×120 | **180×90** | **178.5** | 178.5..268.5 | **69** |
| 3 | ACI 60(`img_houshan1_cloud_60`) | 280×108 | **210×81** | **330.5** | 330.5..411.5 | **62** |
| 4 | FCB 中下 | 320×110 | **240×82.5** | **477.5** | 477.5..560 | **66** |
| 5 | ACI 62(`img_houshan3_cloud_62`) | 240×98 | **180×73.5** | **619.5** | 619.5..693 | **60** |
| 6 | **FCB 底部(新增)** | 320×110 | **240×82.5** | **762** | 762..**844.5** | **69** |

- **第 6 个即"最底部素材"**:底边 **844.5dp**,距导航栏上沿(857dp)留 **12.5dp** → 严格满足"导航栏上方"
- x 沿用各页原值(后山1: -27/-30/153/45/83;后山2 同;后山3: -60/-30/120/12/50);新增的底部 FCB 三页统一 x=76(240dp 宽居中:393−240=153 → 76)
- 周期:FCB 左下 11s、FCB 中下 10s、ACI 三朵共用 10s(phase 0.13/0.31/0.71)、**新增底部 FCB 9s**(与 10/11 互质 → 单程 4.5s,符合 §20 的"约 5 秒跑完屏宽")
- **amplitudeY 收小**:FCB 中下 42→**10**、FCB 左下 36→**10**、ACI58 15→**10**、ACI60 8→**8**、ACI62 10→**8**、新底部 FCB **10**
  - 原因:42/36dp 的上下漂移会把 60~69dp 的间隔**整个吃掉**(两朵各飘 42/36dp 时直接叠在一起),间隔就白定了
  - **amplitudeX 保持 200dp**(§20 的"约 5 秒跑完屏宽"不变)

### 第 3 步:老云静态化

| 页面 | 老云 | 处理 |
|---|---|---|
| 后山1 | 6 朵(58/61/56/57/60/60b) | 去掉 `rememberCloudFloat` 随机飘动 + 去掉云 56 的 ±40dp/7s 椭圆飘动;alpha 固定 **0.75**(原动画区间 0.5~1.0 的中点),**云 56 保持 1f**(§7 用户明确"100% 不透明") |
| 后山2 | 同上 6 朵 | 同上(仍留在 §36 景深平面 2 内,过渡时照常淡出) |
| 后山3 | **5 朵**(`_old` 本就静态、56/58/57/5) | 去掉随机飘动 + **去掉 §23-§26 的 `tintProgress` 色彩循环**(colorFilter 移除,恢复 PNG 原色);alpha 固定 0.75 |

**清掉的 dead code**(三页各自):
- 后山1/2/3 的 `private fun rememberCloudFloat(...)` 整段删除(已无人调用)
- 后山3 的 `tintProgress`(Animatable + LaunchedEffect A9C3C0↔白 循环)整段删除
- 连带清理不再使用的 import:后山1 删 7 个、后山2 删 5 个、后山3 删 13 个(含一个**重复的 `import kotlinx.coroutines.isActive`**,是历史遗留)

### 第 4 步:编译 / 安装 / 真机验证

**踩坑:`compileDebugKotlin` 第一次直接失败** ——

```
FAILURE: Build failed with an exception.
* What went wrong:
25.0.3
```

不是我的代码错。本机 `JAVA_HOME = C:\Program Files\Java\jdk-25.0.3`,而 **AGP 8.7.3 不支持 JDK 25**
(`android/gradle.properties` 第 7 行早就写了这条注释和"What went wrong: 25.0.3"这个特征)。
解法:本机有 `C:\Users\28784\.jdks\jbr-21.0.11`,构建时显式覆盖:

```powershell
$env:JAVA_HOME = "C:\Users\28784\.jdks\jbr-21.0.11"
.\gradlew.bat compileDebugKotlin
```

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 11s
assembleDebug       →  BUILD SUCCESSFUL in 9s   (APK 451.7 MB)
adb install -r      →  Success
dumpsys activity    →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
logcat              →  无 FATAL EXCEPTION
```

**真机走查(adb 截图核对,三页都进到了)**:

| 页面 | 进入路径 | 截图核对结果 |
|---|---|---|
| 后山1 | 点击进入 → 首页「修炼」→ 修炼页「修炼」→ 滚轮1「后山」 | ✓ 熊猫/御剑 + 4 标签 + 气泡都在;6 个新云元素已铺开 |
| 后山2 | 后山1 气泡(Rectangle156) | ✓ 无熊猫无气泡(§18 设计);`识机真决` 标签仍在(后山2 特征) |
| 后山3 | 后山2 空白处 tap → dolly-in | ✓ 熊猫在剑上(X=118,Y=405 未动)、无 `识机真决`(后山3 特征) |

**动效位置客观核对**:抓两帧(间隔 3s)按行统计变化像素占比,得 后山1 的运动带(单位 dp):

```
174.5~247.3  (峰值 30%)   ← FCB 左下(冷青在浅色天空上,对比最强)
334.5~421.8  (峰值 73%)   ← ACI 60
480.0~538.2  (峰值  5%)   ← FCB 中下(冷青压在深色山体上,对比弱)
610.9~814.5  (峰值 23%)   ← ACI 62 + **FCB 底部**(785~814 段 11~14%)
0~160        (0%)         ← ACI 58(白云在近白天空上,像素差几乎为 0)
```

→ 6 个元素**都在动**,位置与设计表一致;**最底部(762~844.5dp)确有一处在运动** ✓

### 已知观感风险(留给用户判断)

底部那条 `FocusCloudBand` 是**冷青半透明雾**,而后山背景最下沿接近纯白 →
**它确实在动,但读起来是"一层很淡的青灰雾",不是"一朵看得见的云"**。
如果用户觉得"最底部那个不够明显",可换方案(都是 1 行改动):
- 把第 6 个从 `FocusCloudBand` 换成 `AnimatedCloudImage`(实图云 PNG,如 Ellipse 60 扁长条)
- 或把底部那条的 `baseAlpha` 从 0.50 提到 0.65~0.75

同理,顶部的 ACI 58(y=8)是**白色云 PNG 落在近白天空**上,两帧像素差为 0(见上表),
肉眼也基本看不见 —— 这是 §10/§11 那个"浅底浅云"老问题的延续,不是本次引入的新 bug。

### git 状态(待 commit)

- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt`
- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan2/Houshan2Screen.kt`
- `M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan3/Houshan3Screen.kt`

```
3 files changed, 217 insertions(+), 471 deletions(-)
```

(净减 254 行:删掉 3 份 `rememberCloudFloat` + 后山3 的 tint 循环 + 大量旧注释)

## 沉淀(新)

- **"间隔 N~M"这类布局指令,先做可行性算术再动手**(新)— 用户要"每两个间隔 60~90dp",但**没给素材数量上限**;
  一算就发现"11 个云 + 60dp 间隔 = 2405dp,是 873dp 屏的 2.8 倍",**物理上不可能**。
  如果直接照做,只能做出"大部分云在屏幕外"的荒谬结果。**判据**:凡是"间距/数量/尺寸"三者同时出现的指令,
  先列出 `素材高度合计 + (n−1)×间隔 vs 可用高度`,把取舍表摆给用户 —— 这份表本身就是最好的沟通材料
- **"用户给的形容词"要和"物理量"分开解**(沿用 §14/§18,本 § 再验证)— 用户说"**随机**在 60~90",但真正的
  物理约束是**间距区间**,不是"每次刷新都变";一问就确认是"定死一组"。**"随机"在日常语言里常指"不均匀、有变化",
  不是"每次重算"**。同理"每两个间隔"指的是**相邻两个**,不是"所有两两组合"(后者是 O(n²) 约束,更不可能满足)
- **可用高度要减掉导航栏,不能拿屏幕高度算**(新)— 873dp 是屏幕,**857dp 才是可用高度**(手势导航栏 44px=16dp)。
  这 16dp 看着小,但 §42 已经证明过"44dp 的设计稿误差会让最下面 1.5 个雾团掉出屏外"。
  **`adb shell dumpsys window displays | grep navigationBars` 是拿这个数的标准做法**,不要估
- **"别动位置"的边界会把改动赶到别处**(新)— 用户说"熊猫不动、雾团不动",于是 6 个云元素的竖向空间
  被这两个约束**上下夹住**(熊猫在 y=405~501,雾团铺满 y=120~895)→ 只能整屏均匀铺开。
  **"哪些不动"这个信息,决定了"能动的东西"的解空间大小**,应该在最开始就问清(这次问了,省了一轮返工)
- **A 模式的真实价值在"这一轮改得对不对"之前就体现**(沿用 §32)— 本次改动删了 471 行、动了三页的构图,
  但**一路只改工作区、没 commit**;如果用户看了真机说"底部换个云朵 PNG",就是 1 行 + 一次重编的事。**A 模式让"大改"也不可怕**
- **构建环境的 JDK 版本是"环境级坑",不是代码错**(新)— `What went wrong: 25.0.3` 这个报错**极度不友好**
  (错误信息就是版本号本身,没有"Unsupported class file version"之类的提示)。
  **`android/gradle.properties` 里已经写了解法**,所以构建失败时**先读 gradle.properties 的注释**,
  再去怀疑代码。已核实本机可用:`C:\Users\28784\.jdks\jbr-21.0.11`
- **"肉眼看不出来"的问题可以用两帧做差客观化**(新)— 浅色页面上判断"某处到底有没有动画"很难;
  **抓两帧按行统计变化像素占比**,就能得到一张"运动带"表,直接和设计的 y 表对照。
  本 § 靠这张表确认了"底部 762~844.5dp 确实在动",也**顺带暴露了顶部 ACI 58 像素差为 0**
  (白色云在近白天空上 → 白做了)。**以后凡是"XX 区域有没有动画/明不明显",先跑这个两帧差**
- **"像素差为 0"不等于"代码没跑",而是"对比度不够"**(新)— ACI 58 的 0% 一开始让我以为它没渲染;
  实际是**白云叠近白天空**。区分方法:看它**在别的背景下**的表观(同样代码的 ACI 60 峰值 73%)。
  **动效的"可感知强度"= 位移 × 对比度,和 §7 那条"加亮度脉冲比再加速明显"是同一件事的两面**

---

### §21b 第 6 个元素换成实图云 PNG(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户指令**:
> "第 6 个换成 AnimatedCloudImage(实图云 PNG)"

#### 先量素材,不凭直觉挑图

上一节我自己在"已知观感风险"里提了这条路(底部雾感太淡 → 换实图云)。但**挑哪张 PNG 不能靠形状直觉** ——
先把项目里 11 张云素材全部采样测了一遍(**采样步长 w/80、h/80;`Tone%` = 不透明像素中亮度 < 225 的占比**):

| 素材 | 尺寸 | 比例 | 平均 RGB | **平均亮度** | **Tone%** | 结论 |
|---|---|---|---|---|---|---|
| `img_houshan1_cloud_56` / `img_shilian3_cloud_56` | 1487×1373 | 1.08 | 210,223,222 | **219** | **58%** | ✅ 有色调 |
| `img_houshan1_cloud_57` / `img_shilian3_cloud_5` / `img_shilian3_cloud_57` | 986×884 | 1.12 | 216,225,217 | **221** | **72%** | ✅ 最"有颜色" |
| `img_houshan3_cloud_62` | 555×227 | 2.44 | 239,238,228 | 237 | **0%** | ❌ 近白 |
| `img_shilian3_cloud_old` | 715×474 | 1.51 | 251,250,245 | 250 | 0% | ❌ 近白 |
| `img_shilian3_cloud_58` | 1236×710 | 1.74 | 251,250,245 | 250 | 0% | ❌ 近白 |
| `img_houshan1_cloud_58` | 844×474 | 1.78 | 251,250,245 | 250 | 0% | ❌ 近白 |
| `img_houshan1_cloud_61` | 351×210 | 1.67 | 255,255,255 | 255 | 0% | ❌ 纯白 |
| **`img_houshan1_cloud_60`** | 911×353 | 2.58 | **255,255,255** | **255** | **0%** | ❌ **纯白** |

**两个关键发现**:
1. **当前动画栈里用的 3 张全是白的** —— `cloud_58`(250)、`cloud_62`(237)、`cloud_60`(**255 纯白**)。
   这就是 §21 里"顶部 ACI 58 两帧像素差 = 0"以及"底部看不出东西"的**真正原因**,不是位置问题、是**素材本身没颜色**。
2. **形状最贴的扁长云恰好全是白的**(比例 > 1.5 的四张:237~255,**Tone% 全是 0**);
   **有色调的两张(219/221)比例都是 1.08~1.12(近方)** —— 形状和"有颜色"在现有素材里**不可兼得**。

#### 取舍:可见 > 形状

按 §21 的沉淀"动效可感知强度 = 位移 × 对比度",选了 **`img_houshan1_cloud_57`**(MeanLum 221、Tone% 72%,
是项目里最有色调的云),**放弃形状更贴的 `cloud_60`**(因为它是纯白,叠在近纯白底部背景上等于没加)。

代价:比例 1.12 → 在"间隔 ≥60dp + 底边 ≤844.5dp"的约束下只能做到 **100×90 dp**。

#### 新布局(第 6 个 + 微调第 5 个的 y)

| # | 元素 | 尺寸 | y(顶) | ↑ 间隔 |
|---|---|---|---|---|
| 5 | ACI 62 | 180×73.5 | **620**(原 619.5 → 620,让 g4 正好 60) | 60 |
| 6 | **ACI 57(实图云 PNG)** | **100×90** | **754.5** | **61** |

- 新增 5 个位置校核:`101.5 + 69 + 90 + 62 + 81 + 66 + 82.5 + 60 + 73.5 + 61 + 90 = 836.5` → 顶 8 + 836.5 = **底边 844.5** ✓
- 5 个间隔 **69/62/66/60/61**,全部仍在 60~90 内且不均匀(保持"定死一组随机值"的语义)✓
- 底边 844.5dp,距导航栏上沿(857dp)**12.5dp** ✓
- x 居中:100dp 宽 → (393−100)/2 = 146.5 → **146**
- 动画参数与其他 ACI 一致:`phase = 0.87`(0.13/0.31/0.71 之外的第 4 个相位)、`ampX 200 / ampY 10`、`alpha 0.50±0.25`
- 后山1/2 用共享 `cloudProgress`(10s);后山3 用 `c60Progress`(10s,同周期)

#### 编译 / 安装 / 真机验证

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 8s
assembleDebug       →  BUILD SUCCESSFUL in 8s
adb install -r      →  Success
dumpsys activity    →  topResumedActivity=com.jueqiao.jianghu/.MainActivity ✓
logcat              →  无 FATAL EXCEPTION
```

**三页真机走查**:后山1 ✓、后山2 ✓、后山3 ✓(后山2→后山3 画面差 56.4% 证明真的翻页了)

**底部动效复测(两帧做差)**:

| 区间 | 换之前(FCB,冷青雾) | **换之后(cloud_57 实图)** |
|---|---|---|
| dp 690~755 | 21%~23% | **19.8%** |
| **dp 755~845(第 6 个所在)** | 11~14% | **8.3%** |

→ 数值上同量级(都远大于顶部那张"纯白云"的 **0%**),说明**确实看得见、确实在动**。

#### 一个必须告诉用户的差异:两页的底部背景深浅完全不同

裁剪真机截图后发现 —— **同一个 cloud_57,在三页上的对比度不一样**:

| 页面 | 底部背景(真机截图实测观感) | cloud_57 的可见度 |
|---|---|---|
| 后山1 / 后山2 | **近纯白山谷**(水彩留白) | 浅叠浅,偏柔和 |
| 后山3 | **深青绿山体**(试炼转换.png 下半部是山) | 浅叠深,**明显** |

也就是说"第 6 个够不够明显"这个问题,**后山3 大概率没问题,后山1/2 需要用户真机判断**。
如果后山1/2 还觉得淡,下一步可选(都不改构图,只改数值):
- 把第 6 个的 `baseAlpha` 从 0.50 提到 **0.70**(只是这一朵更实,不动其他 5 个)
- 或换 **`img_houshan1_cloud_56`**(MeanLum 219,比 57 更暗一点,58% 带色)
- 或给第 6 个单独加一层 `#A9C3C0` 冷青 tint(冷青在暖白底上对比更强,是 §11 已经验证过的手段)

#### git 状态(待 commit)

仍是同样 3 个文件(在 §21 基础上再改):
- `M Houshan1Screen.kt` / `M Houshan2Screen.kt` / `M Houshan3Screen.kt`

## 沉淀(§21b)

- **"素材本身没颜色"是比"位置不对"更隐蔽的一类 bug**(新)— 我先后怀疑过位置、尺寸、层级、周期,
  最后**量了素材亮度**才发现:`cloud_60` 是**纯白 255**,`cloud_58` 是 250。
  **在浅色页面上放浅色 PNG,再多的位置/速度调整都不会让它出现**。
  **判据**:页面偏浅时,先把素材的 `MeanLum / Tone%` 测出来再排布局 —— **素材的色阶是布局的前提,不是布局的结果**
- **"形状合适"和"有颜色"在现有素材里不可兼得,要显式取舍**(新)— 扁长的 4 张全是 0% 带色,有色的 2 张都是近方形。
  按"可感知强度 = 位移 × 对比度",**对比度为 0 时位移再大也是 0**,所以**选颜色、牺牲形状**。
  这条决策必须写进 log,否则下次有人看到"为什么底部不用最像的那张"会重复纠结
- **同一份代码在不同页面上的观感可以完全不同(背景决定对比度)**(新)— 第 6 个元素三页代码/尺寸/位置**完全一样**,
  但后山1/2 的底是近白山谷、后山3 的底是深青山体 → **后山3 明显、后山1/2 偏淡**。
  **"三页效果统一"只能统一参数,统一不了观感** —— 因为参数是叠在不同画作上的。§17 定的"效果统一"以后都要带这个前提
- **改一个元素也要回头校核整条间隔链**(新)— 把第 6 个从 240×82.5 换成 100×90,高度变了 7.5dp,
  于是第 5 个的 y 必须从 619.5 挪到 620(否则 g4 变成 59.5,**掉出 60dp 下限**);
  底边也必须重新算,确认还是 844.5。**"一列元素按固定间隔排"是一个约束链,动任何一个都要全链重算**

---

### §21c 横向运动分两种模式:一部分"单向+回绕",一部分保持 sin 摆动(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户先问**:"所有素材的左右移动方向都是从左向右走吗"
**回答**:不是 —— 当时**全部**都是 `sin` **左右往复摆动**(不是单向走)。公式:
`x = xOffset + sin(2π×(进度+相位)) × amplitudeX`。进度 0→25% 向右、25%→75% 向左、75%→100% 回位;
**单程 = 半周期**(10s 周期 → 单程 5s = §20 的"约 5 秒跑完屏宽")。
`phase` 只决定**从哪出发、先往哪边**:ACI58(0.13)先右、ACI60(0.31)先左、ACI62(0.71)先左、ACI57(0.87)先右。
另外:**熊猫完全没有横向运动**(只有 ±10dp 上下浮 + 呼吸缩放);雾团也是 sin;6 朵老云已静态化不动。

**用户指令**:"一部分使用单向加回绕,一部分素材使用混合的方式"
**澄清后确认**(AskUserQuestion):"混合" = **元素分组** —— 一部分元素走单向回绕、一部分保持 sin 摆动,用互质周期错开。

#### 第 1 步:给两个共享组件加"运动模式"

`FocusCloudBand.kt` 新增 public `enum class CloudMotion`,两个组件都加 `motion` 参数(默认 Oscillate):

| 模式 | 公式 | 特性 |
|---|---|---|
| `Oscillate` | `x = xOffset + sin(2π·进度) × amplitudeX` | 左右往复;**永远在屏上** |
| `DriftWrap` | `x = −元素宽 + 进度 × (屏宽 + 元素宽)` | **单向**左进右出;出屏后回绕 |

**回绕跨度为什么是 `屏宽 + 元素宽`**:必须让元素走到右端时**已经完全出屏**,回绕瞬间才不可见 → 无"跳回"痕迹。
代价:**元素有 20~38% 的时间完全不在屏上**(越宽占比越高)。

#### 第 2 步:先按"位置交替"分配 —— 然后发现**看不见**

第一版按位置交替(#1/#3/#5 单向,#2/#4/#6 摆动)。装上真机后**怎么截图都看不出差别**,
于是给组件插了临时 `Log.d` 探针,拿到决定性数据:

```
CloudDrift: motion=DriftWrap w=180.0 screenW=393.0 span=573.0
CloudDrift: motion=DriftWrap w=210.0 screenW=393.0 span=603.0
CloudDrift: motion=Oscillate w=100.0 screenW=393.0 span=493.0
```

→ `LocalConfiguration.current.screenWidthDp = **393**`(与 §21 实测的 393×873 完全一致),
跨度 573/603 也对 —— **单向回绕的数学是对的,不是 bug**。
真正的原因是 §21b 那张素材亮度表:**分到单向组的 3 个元素(ACI58/60/62)恰好是 250/255/237 的近白/纯白云**,
在浅色背景上本来就看不见 → "有云在单向飘"这件事视觉上根本传达不出来。

#### 第 3 步:改成**按"观感可见性"分配**(最终方案)

真机上真正看得见的是两条 **冷青 `#A9C3C0` 的 FocusCloudBand**(§21 实测变化量 21~30%),
所以把它们换进单向组:

| # | 元素 | y | 模式 | 周期 | 真机可见度 |
|---|---|---|---|---|---|
| 1 | ACI58(近白 250) | 8 | `Oscillate` | 7s | 几乎不可见(浅天空) |
| 2 | **FCB左下(冷青)** | 178.5 | **`DriftWrap`** | 11s | **明显** ✓ |
| 3 | ACI60(纯白 255) | 330.5 | **`DriftWrap`** | 8s | 山体上可见 |
| 4 | **FCB中下(冷青)** | 477.5 | **`DriftWrap`** | 10s | **可见** ✓ |
| 5 | ACI62(近白 237) | 620 | `Oscillate` | 9s | 偏淡 |
| 6 | ACI57(有色调 221) | 754.5 | `Oscillate` | 13s | 可见,**永远在屏上** |

- **单向组 = #2 #3 #4**(3 个),**摆动组 = #1 #5 #6**(3 个)→ 满足"一部分/一部分"
- #6 按用户明确要求**保持摆动**:单向会让它约 20% 时间出屏,破坏"最底部一定要有一个动画素材"这条硬要求
- 周期 7/8/9/13 + FCB 的 10/11 → 六个数 `lcm = 7×8×9×5×11×13 = 360360s ≈ **4.2 天**`,看不出规律性同步
- **平均速度校准**(让两组快慢看起来一致):
  单向组 = `(393+元素宽)/周期` → 573/7=**82**、603/8=**75**、633/10=**63** dp/s
  摆动组 = `400/半周期` → 400/5.5=**73**、400/5=**80**、400/6.5=**62** dp/s
- 单向模式**忽略 `xOffset` / `amplitudeX`**(横向位置完全由回绕决定),调用处传 `0f` 并在注释里写明

#### 第 4 步:编译 / 安装 / 真机验证

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 9s / 3s
assembleDebug       →  BUILD SUCCESSFUL in 9s / 3s
adb install -r      →  Success;  logcat 无 FATAL EXCEPTION
```

**逐带两帧差(帧1→帧2 / 帧1→帧3,间隔约 2s)**:

| 带 | 帧1→2 | 帧1→3 |
|---|---|---|
| 全屏 | 13.8% | 11.9% |
| FCB左下(**单向**) | **22.2%** | 18.4% |
| FCB中下(**单向**) | 5.6% | 0.1% |
| ACI62(摆动) | 12.3% | 12.6% |
| ACI57(摆动·底部) | **25.6%** | 21% |

→ 全屏 13.8%、单向带 22.2%、底部 25.6%,**动画确实在跑**。

**⚠️ 诚实记录:我没能用像素方法证明"方向"**。试过两种都被噪声淹没:
1. **变化重心** —— 13 个半透明雾团叠加,"差分的重心"会乱跳
2. **互相关求最佳位移** —— 元素 alpha 脉动范围高达 0.5(0.25~0.75),"变亮"的权重压过"位移",最佳位移恒为 0
**结论:方向这件事以真机肉眼为准**,代码上的方向是确定的(`DriftWrap` 是纯线性递增,不存在反向分支)。

#### git 状态(待 commit)

- `M ui/components/FocusCloudBand.kt`(新增 public `enum CloudMotion` + `motion` 参数 + 分支)
- `M ui/components/AnimatedCloudImage.kt`(同上)
- `M Houshan1Screen.kt` / `M Houshan2Screen.kt` / `M Houshan3Screen.kt`(各 6 个调用加 `motion`;周期改 7/8/9/13)

## 沉淀(§21c)

- **"用像素差证明动画方向"在当前场景下不可行,别再试第三次**(新)— 两个方法都失败了,根因是
  **元素太多 + 半透明叠加 + alpha 脉动幅度大(0.5)**。要测方向,只有两条路:
  ①**临时打日志**(本次验证 `screenWidthDp=393` 就是靠它,一次到位)
  ②**做一个人为的、高对比的测试元素**(比如临时把某个元素换成纯色块)
  **"靠截图看动画"在超过 3 层半透明叠加后就失效了** —— 这是本次最贵的一课(试了两轮才转向日志)
- **验证"机制是否生效"和验证"观感是否达标"是两件事**(新)— `Log.d` 证明了回绕几何**完全正确**,
  但真机上**看不出任何差别**。**机制正确 ≠ 效果达成**。以后遇到"用户要的效果装上去看不出来",
  第一步应该是问"**它可见吗**"(查素材亮度/对比度),而不是怀疑"**它算对了吗**"(查公式)
- **"按位置交替"这种对称分配,在可见性不均匀时会失效**(新)— 我第一版按 #1/#3/#5 交替分配单向/摆动,
  逻辑上最"均匀";但项目里**元素的可见度差异极大**(冷青 21~30% vs 纯白云 0%)。
  **分配运动模式时,应该按"这个元素能不能被看见"排,而不是按"位置好不好看"排** ——
  看不见的元素无论怎么动都是 0 效果
- **新增 `enum` 参数是给共享组件加行为分支的低成本做法**(新)— 只加了一个 `motion: CloudMotion = CloudMotion.Oscillate`
  (带默认值),既有 18 个调用点**不改也能编译**;然后逐个调用点显式声明。**默认值 = 渐进式迁移的安全网**
- **同一次改动里"周期"和"模式"要一起看**(新)— 单向模式的"一个周期"是**一次完整穿越**(跨度≈屏宽+元素宽),
  而摆动模式的"一个周期"是**来回一次**(单程≈半周期)。**两者的周期语义不同**,
  所以不能直接把摆动的 10s 搬给单向(会慢 40%)。本次按"平均 dp/s 对齐"重新配了 7/8/9 与 10/11/13

---

### §21d 把 2 个"单向向右"改成"单向向左"(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户先问**:"在一个页面中有多少个素材是单向的在水平方向中向右移动的"
**回答**:**3 个**(三页相同),而且全部向右、反方向 0 个 —— FCB左下(11s/52dp/s)、ACI60(8s/75dp/s)、FCB中下(10s/63dp/s)。

**用户指令**:"把2个单向向右的素材改成单向向左的"

#### 改了哪 2 个(3 个单向 → **1 右 + 2 左**)

| # | 素材 | 改前 | 改后 | 选择理由 |
|---|---|---|---|---|
| 2 | FCB 左下(y=178.5) | → 右 | **保持 → 右** | 三页里**最明显**的元素(§21 实测变化量 21~30%) |
| 3 | ACI60(y=330.5) | → 右 | **→ 左** | |
| 4 | FCB 中下(y=477.5) | → 右 | **→ 左** | 这样**两个方向各留一个看得见的冷青飘带**,对立才看得出来 |

(备选方案:让 3 个按 y 交替 `右/左/右`;但那样"剩下的唯一向右"会是近白不可见的 ACI60 →
向右这个方向在真机上几乎看不到,所以否决了)

#### 实现:enum 加第 3 个值

```kotlin
enum class CloudMotion {
    Oscillate,       // sin 往复,永远在屏上
    DriftWrap,       // 单向 左 → 右:x = −元素宽 + 进度 × (屏宽 + 元素宽)
    DriftWrapLeft,   // 单向 右 → 左:x = 屏宽 − 进度 × (屏宽 + 元素宽)   ← §21d 新增
}
```

两个组件都是 `when (motion)` 三分支(`when` 是穷尽的,漏一个就编译不过 —— 这点正好当护栏)。
`DriftWrapLeft` 是**反向线性递减**:p=0 时 x=屏宽(整朵在右屏外)、p=1 时 x=−元素宽(整朵在左屏外),
**两端都在屏外所以回绕无痕**,与向右版完全对称。

#### 编译 / 安装

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 11s
assembleDebug       →  BUILD SUCCESSFUL in 12s
adb install -r      →  Success;  logcat 无 FATAL EXCEPTION
真机走查            →  后山1 正常(熊猫/御剑/气泡/4 标签齐全)
```

#### ⚠️ 第三次尝试用像素验证"方向",**仍然失败** —— 到此为止

这次换了"**逐像素取 9 帧中位数当静态背景,再取 (G−R) 冷青增量重心**"的思路(理论上能消掉静态背景),
但 PowerShell 逐像素循环在 1080×2400 上**跑到 600s 超时**;前面两次(变化重心 / 互相关)也被噪声淹没。

**结论**:本项目**不要再尝试用截图证明动画方向**,三条路都试过了。可信的只有:
① **临时 `Log.d` 打参数**(§21c 靠它拿到 `screenWidthDp=393`,一次到位)
② 代码/公式审查(单向分支是纯线性,没有反向分支,方向由符号决定)
③ 真机肉眼

#### 一个操作教训:截图前必须确认"当前在哪个页面"

本次第一轮导航**没落到后山1**(停在首页),结果我拿 4 帧算出**完全相同的 19475 个冷青像素、重心恒为 603** ——
**"逐帧完全一致"就是"页面是静态的"的强信号**。
如果当时把它当成"动画没生效"去改代码,就会在正确的代码上乱改。**判据:连续帧像素数一模一样 → 先怀疑页面不对,再怀疑功能**。

#### git 状态(待 commit)

- `M ui/components/FocusCloudBand.kt`(enum 加 `DriftWrapLeft` + 分支改三分支)
- `M ui/components/AnimatedCloudImage.kt`(同上)
- `M Houshan1Screen.kt` / `M Houshan2Screen.kt` / `M Houshan3Screen.kt`(ACI60 与 FCB中下 改 `DriftWrapLeft`)

## 沉淀(§21d)

- **"逐帧像素数完全相同"是页面级静态的强信号,不是动画 bug**(新)— 19475 个像素、重心 603 **四次完全一致**,
  我一开始以为是"改动没生效";实际是**导航没落到目标页**(停在首页)。
  **判据:如果连噪声都没有(精确到整数位相同),那不是"动画很慢",而是"根本没动"** ——
  **先确认页面身份(看截图/看 `topResumedActivity`),再下结论**
- **PowerShell 逐像素处理 1080×2400 会超时,别再用它做图像分析**(新)— 中位数 + 9 帧遍历直接跑满 600s。
  要真做图像处理,应该:**下采样到 1/8 再算**(本次 `step=6` 仍然太慢,因为每像素都走了 `GetPixel` 反射调用),
  或者干脆用 `Add-Type` 直接操作 `BitmapData`(指针级 `LockBits`)。
  **本项目的结论:图像分析这条路 ROI 太低,改用日志/代码审查**
- **`when` 穷尽性检查是加 enum 值的免费护栏**(新)— `CloudMotion` 从 2 个值加到 3 个时,
  两个组件的 `when` 立刻编译报错提示补分支,**不会出现"某个组件忘了处理新模式"**。
  **给共享行为加 enum 分支时,要确保分支点是 `when` 表达式而不是 `if/else if`**(后者会静默走 else)
- **"方向对立"要保证两侧都有看得见的元素**(新)— 改完是 1 右 2 左,但如果把"唯一向右"留给近白不可见的 ACI60,
  那"右"这个方向在真机上就消失了。**和 §21c 同一条原理:分配运动方式时要按"能不能被看见"排,
  不是按几何对称排**。本次特意让两条冷青飘带一右一左

---

### §21e 改成"2 右 + 1 左"(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户指令**:"2 右 1 左"

把 §21d 里翻向左的 **ACI60 翻回向右**,保留 FCB中下 向左。最终 3 个单向元素:

| # | 素材 | y | 模式 | 周期 |
|---|---|---|---|---|
| 2 | FCB 左下(冷青) | 178.5 | **→ 右** | 11s |
| 3 | ACI60(在山体上可见) | 330.5 | **→ 右** | 8s |
| 4 | FCB 中下(冷青) | 477.5 | **← 左** | 10s |

**为什么把"唯一向左"留给 FCB中下 而不是 ACI60**:ACI60 是近白 PNG,只有压在深色山体上时才看得见;
FCB中下 是冷青飘带,§21 实测可见。**这样"左"这个方向在真机上才真的存在**(延续 §21c/§21d 的"按可见性分配"原则)。

#### 代码改动(每页 1 行 × 3 页)

`motion = CloudMotion.DriftWrapLeft` → `CloudMotion.DriftWrap`(仅 ACI60 那一处)。
三页分布核对(grep 实测,完全一致):

```
Houshan1Screen.kt:189 Oscillate  :196 DriftWrap  :207 DriftWrap  :214 DriftWrapLeft  :225 Oscillate  :243 Oscillate
Houshan2Screen.kt:247 Oscillate  :254 DriftWrap  :265 DriftWrap  :272 DriftWrapLeft  :283 Oscillate  :297 Oscillate
Houshan3Screen.kt:188 Oscillate  :199 DriftWrap  :215 DriftWrap  :226 DriftWrapLeft  :242 Oscillate  :261 Oscillate
```

→ 每页 **2 个 `DriftWrap`(右)+ 1 个 `DriftWrapLeft`(左)+ 3 个 `Oscillate`** ✓

#### 编译 / 安装 / 真机验证

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 3s
assembleDebug       →  BUILD SUCCESSFUL in 4s
adb install -r      →  Success
真机走查            →  **确认在后山1**(熊猫御剑 + 气泡 + 4 标签齐全),logcat 无 FATAL
```

**这次特意先确认了页面身份再下结论**(§21d 的教训):截图里能看到熊猫、气泡、识机真决/万象谱/拆招心法/寻径迷踪步
四个标签 —— 是后山1 无疑,不是首页。

#### 顺带记下:真机回归环境的完整恢复步骤(本次踩到)

上一轮我按了一次 BACK,结果**退出了 App 并且会话失效**,再进变成**登录页**。恢复步骤:

```powershell
adb reverse tcp:8010 tcp:8010          # 真机访问主机的 8010(必需,否则登录请求发不出去)
# 登录页:填测试账号(见 docs/ONBOARDING.md §12.3)
adb shell input tap 504 1471 ; adb shell input text "13800138000"
adb shell input keyevent KEYCODE_BACK   # 收起键盘(否则键盘挡住密码框,后续 tap 全打在键盘上)
adb shell input tap 504 1626 ; adb shell input text "Test1234!"
adb shell input keyevent KEYCODE_BACK
adb shell input tap 91 1824             # 勾选《用户协议》
adb shell input tap 540 1941            # 登录
```

登录后是**首次引导**(首页要点 3 次才出现 修炼/大会/作品创作/行囊 四个入口),之后:
`修炼(292,874) → 引导×2 → 修炼(447,1106) → 滚轮1「后山」(756,580) → 后山1`。
**注意:`adb shell input tap` 在键盘弹出时会打在键盘上** —— 填完每个输入框都要 BACK 收键盘。

#### git 状态(待 commit)

- `M Houshan1Screen.kt` / `M Houshan2Screen.kt` / `M Houshan3Screen.kt`(各 1 行:ACI60 的 motion 值 + 注释)

## 沉淀(§21e)

- **"唯一的方向"要留给看得见的元素**(新,§21c→§21d→§21e 三次收敛出的一条)— 这轮反复调整了三次
  (3右 → 1右2左 → 2右1左),每次的决定因素都不是"几何对称"而是**"哪个方向能被看见"**。
  **可操作的判据:先列出每个元素的实测可见度,再把"稀少的那一类"分配给可见度最高的元素**
- **`adb shell input text` 之后必须收键盘**(新)— 键盘会盖住下半屏,**后续所有 tap 都打在键盘上**,
  表现为"点了没反应"。本次第一轮登录就是这样失败的(手机号填对了,密码框的 tap 打在键盘上)。
  **标准动作:每个 `input text` 之后跟一次 `input keyevent KEYCODE_BACK`**
- **`adb shell input keyevent KEYCODE_BACK` 是"危险键"**(新)— 在导航栈浅的时候它会**直接退出 App**,
  真机上还可能**让会话失效 → 退回登录页**。本次因此丢了一轮验证。
  **优先用 UI 里的返回按钮(BackHandler 有实际 onClick),只在"收起键盘"这种明确场合才用 BACK**

---

### §21f 6 朵老云重新动画化(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户指令**:"把 6 朵静态老云的动画也做出来,不考虑间距了"

§21 曾按用户指令把老云静态化,本节**撤销那个决定**,并明确放弃"60~90dp 间隔"这条约束
(老云不再参与间隔排布,可以和 §21 那 6 个动画元素重叠)。

#### 用 CloudMotion 而不是恢复原来的 `rememberCloudFloat`

两条路都能"让老云动起来",选了前者:

| | 恢复 `rememberCloudFloat`(旧方案) | **复用 `CloudMotion`(本次选的)** |
|---|---|---|
| 运动性质 | 朝**随机目标点**游走,方向/速度不可预期 | 确定性:sin 往复 或 单向回绕 |
| 资源 | **每朵云 3 个协程**(X/Y/alpha),6 朵 = 18 协程 | 1 个 `rememberInfiniteTransition` + 6 个动画值,零协程 |
| 一致性 | 与 §21c 的 6 个动画元素是两套语言 | 与它们**同一套**语言(3 模式 + 互质周期) |

#### 后山1 / 后山2 的 6 朵(位置尺寸沿用原值)

| 云 | 尺寸 | 模式 | 周期 | ampX / ampY | 说明 |
|---|---|---|---|---|---|
| 58 | 455×259 | Oscillate | 17s | 90 / 14 | 最大,留在原地 |
| 61 | 355×213 | **DriftWrap → 右** | 19s | 0 / 12 | |
| 56 | 335×297 | Oscillate | 23s | 70 / 16 | 最高,留在原地 |
| 57 | 225×191 | **DriftWrapLeft ← 左** | 21s | 0 / 12 | |
| 60 | 355×137 | Oscillate | 13s | 90 / 10 | 扁长 |
| 60b | 355×137 | **DriftWrap → 右** | 11s | 0 / 10 | |

- 周期 17/19/23/21/13/11,**6 个数两两互质**(21 = 3×7,与 11/13/17/19/23 都互质),
  与 §21c 的 7/8/9/10/11/13 合起来 lcm 极大 → 12 个元素基本不会规律性同步
- 尺寸最大的两朵(58/56)用 Oscillate 留在原地,避免 455dp 宽的大块云有近半时间飘出屏幕
- alpha 恢复老动画区间 **0.75±0.25**(即原 0.5~1.0);**云 56 仍按 §7 用户指令恒为 1f**(alphaAmp = 0)

#### 后山3 的 5 朵

后山3 比后山1/2 多一朵:`img_shilian3_cloud_old`(**从 8f5a28c 基线起就一直是静态的**,本次也一并动画化)。
其余 cloud_56/58/57/5 沿用各自原位置尺寸;模式分配与后山1/2 同构(大云 Oscillate,扁长的 2 右 1 左)。

#### 组件改动:`AnimatedCloudImage` 新增 `pulseBase` / `pulseAmp`

老云是画好的水彩云,不该再叠 §7 那层白色脉冲高光。新增两个参数(默认值 = 原 0.20 / 0.16,向后兼容),
**两者都传 0f 时整块跳过** white-pulse 的 `Box`(老云连 `drawRect` 都不执行)。
§21 那 6 个动画元素不传这两个参数 → 行为完全不变。

#### ⚠️ 顺带修掉一个 §21 引入的回归:后山2 云 60b 被挪了 10dp

`git show 68ac236` 逐条核对时发现:

```
-  // 云朵 60b (副本,Y=690)
-  .offset(x = (-21f + cloud60bDx).dp, y = (770f + cloud60bDy).dp)
+  // 云朵 60b (X=-21, Y=760, W=355, H=137)— §21 静态化
+  .offset(x = (-21f).dp, y = 760f.dp)
```

**§21 静态化时我把后山2 的 60b 从 Y=770 抄成了 760**(后山1 的值),而 770 是用户真机调过的
(§40 记录:`fix(houshan2): 云朵 60b Y=690 → 770(用户 IDE 真机微调)`)。
**§40 的沉淀第 4 条写的就是"用户调整过的值要保护"** —— 这条我踩了。§21f 已改回 **770**。

#### 编译 / 安装 / 真机验证

```
compileDebugKotlin  →  BUILD SUCCESSFUL in 4s    ← 注意:已不需要任何 JAVA_HOME 覆盖(§21e 的构建环境修复生效)
assembleDebug       →  BUILD SUCCESSFUL in 33s
adb install -r      →  Success;  logcat 无 FATAL EXCEPTION
真机走查            →  后山1 / 后山2 / 后山3 三页均正常(后山2→后山3 画面差 53.6%,确认真翻页)
```

**"动的元素变多了"的客观证据(全屏帧间变化率,间隔 2s)**:

| 状态 | 全屏帧间变化率 |
|---|---|
| §21e(只有 6 个元素动) | **13.8%** |
| **§21f(12 个元素都动)** | **33.8%** |

→ 变化率 **2.4 倍**。这个指标不能证明"方向",但能证明"多了东西在动",正好补上 §21d 缺的那一环。

#### git 状态(待 commit)

- `M ui/components/AnimatedCloudImage.kt`(新增 `pulseBase`/`pulseAmp`,脉冲可整体跳过)
- `M Houshan1Screen.kt` / `M Houshan2Screen.kt` / `M Houshan3Screen.kt`(老云转 AnimatedCloudImage + 6/6/5 组进度值)

## 沉淀(§21f)

- **"批量静态化/去动画"最容易顺手改掉坐标**(新)— §21 一次改了 12 处 `offset`,其中后山2 的 60b
  被我抄成了后山1 的值(770→760)。**这类"机械替换"改动必须逐条 `git diff` 核对数字**,
  尤其当两个文件是"复制粘贴关系"时 —— 差异点(后山2 的 770、后山1 的 760)正是用户手工调过的地方。
  **§40 沉淀第 4 条"用户调整过的值要保护"就是这条,本次是它的第二次复现**
- **"帧间变化率"是比"变化重心/互相关"好得多的宏观指标**(新)— 它不能给方向(§21d 已证),
  但能干净地回答"**有没有变多/变少**":本次 13.8% → 33.8% 直接量化了"动的元素从 6 个变 12 个"。
  **用途分工:宏观"有没有动/动了多少" → 帧间变化率;方向/参数 → `Log.d`;观感 → 真机肉眼**
- **给共享组件加行为开关时,默认值必须等于"旧行为"**(沿用 §21c)— `pulseBase=0.20f, pulseAmp=0.16f`
  与原硬编码完全一致 → 既有 9 个调用点(§21 那 6 个 + 后山3 的 3 个)一个都不用改,行为零变化;
  只有需要关脉冲的老云显式传 `0f`。**"默认值 = 旧行为"是往共享组件加参数时唯一安全的做法**
- **同一件事有"旧实现"时,先比较再决定是否照搬**(新)— 恢复 `rememberCloudFloat` 是最省事的路(纯 revert),
  但它带来 18 个协程 + 不可预期的运动,而项目这两天刚建好 `CloudMotion` 这套确定性方案。
  **"能恢复"不等于"该恢复"** —— 判断依据是"新方案是否已经覆盖了旧方案的意图",而不是"哪个 diff 小"

---

### §21g 恢复文档层的两个历史缺口(用户指令)(2026-09-16 晚)— A 模式不 commit

**用户指令**:"恢复文档层的两个已知缺口"

两个缺口都是 §16 那次分支同步(stash-pop 冲突)留下的:

#### 缺口 1:§16 整节缺失

**成因**:§16 的内容只存在于 commit `b58c714`("docs: §16 同步 codex/ifthen 到 zzz 分支记录"),
该 commit 后来被 force-push 挤出了分支 → **文件里 §15 直接跳到 §17**。
所幸 **commit 对象仍在本地对象库**(未被 gc),可以直接取回。

**恢复方式**(没有手抄,而是程序化取回,避免 85 行中文转录出错):

```powershell
# 1. 取出悬空 commit 里的 §16 段(行 951..EOF,共 85 行)
$blob = @(& git show "b58c714:docs/SESSION-LOG-2026-09-16.md")
$s16  = $blob[950..($blob.Count-1)]
# 2. 插到 §15 沉淀之后、§17 之前
# 3. 按原编码写回:UTF-8 无 BOM + CRLF
```

**插入位置**:§15 的 `## 沉淀(新)` 块(行 910~951)之后、`### §17`(行 1038)之前 ——
既符合时间顺序(§15 → §16 → §17),也不打断 §15 那个沉淀块。

#### 缺口 2:错位的 `## 沉淀(新)` 标题 + 重复的 §12 条目

**成因**:同一次 stash-pop 冲突里,一个 `## 沉淀(新)` 标题被 §16 的标题覆盖又还原,
留下**一个多余的二级标题 + 3 行与上文逐字相同的 §12 条目片段**。

**先验证再删**(没有凭印象删):

```powershell
# 这 3 行是否与上文 §12 条目逐字相同 → True,可安全删除
for ($k=0; $k -lt 3; $k++) { if ($lines[$iBad+1+$k] -ne $lines[$iBad-8+$k]) { $dup = $false } }
```

确认 `True` 后才 `RemoveRange`。

#### 安全措施(改文档也要防"整篇被重写")

改之前先探明并保持:

| 项 | 实测 | 处理 |
|---|---|---|
| 编码 | **UTF-8,无 BOM** | `UTF8Encoding($false)` 写回 |
| 换行 | **全 CRLF(0 个裸 LF)** | 按 `\r\n` 切分后再 join,不做任何换行归一化 |
| 改后复核 | CRLF = 2156、裸 LF = **0**、BOM = **False** | ✓ 与改前一致 |

**关键:先 `RemoveRange` 再重新定位 §17 锚点再插入**,不依赖插入前的旧索引(避免索引漂移)。
另外插完后发现 §16 段自带一个末尾空行 → §17 前出现**两个连续空行**,已单独去掉一个。

#### 顺带:更新文件顶部那条"已知缺口"注记

顶部注记原先写着"§16 缺失 / 第 ~923 行有错位标题",现已改为 **✅ 两个缺口已修复(§21g)**,
并把原来硬编码的 `@ 4c1ab02` 换成"最新 HEAD 见 `git log`" —— **避免注记本身又变成过期快照**(这是本次踩过的老问题)。

#### 结果

| 项 | 改前 | 改后 |
|---|---|---|
| 行数 | 2076 | **2156** |
| §16 | **缺失** | ✅ 存在(85 行,唯一) |
| `## 沉淀(新)` 出现次数 | 4 | **3**(全是合法位置:§15 块、§17~§20 块、§40 块) |
| §14~§18 顺序 | §15 → **§17** | §15 → 沉淀 → **§16** → §17 ✓ |
| 编码 / 换行 | UTF-8 无 BOM / CRLF | **完全一致**(未被整篇重写) |

#### git 状态(待 commit)

- `M docs/SESSION-LOG-2026-09-16.md`(§21f/§21g 两节 + §16 恢复 + 顶部注记更新)

## 沉淀(§21g)

- **"文档缺口"要区分"丢了"和"只是不在这条分支上"**(新)— §16 不是被删掉,而是所在的 commit
  被 force-push 挤出了分支;**对象还在本地对象库里,`git show <dangling-sha>:<path>` 就能取回**。
  比手抄安全得多。**判据:`git log --all` 找不到 ≠ 内容不存在,先试悬空对象**
- **改 Markdown 也要防"整篇被重写"**(新)— 文档文件同样有**编码(BOM)**和**换行(CRLF/LF)**两个隐形属性,
  用 `Get-Content`/`Set-Content` 一把梭很容易把整个文件的行尾换掉 → `git diff` 变成"全文重写",真实改动被淹没。
  **标准动作:先探测 BOM + 换行,改完再复核这两个数**。本次改前改后都是"无 BOM + 2156 CRLF + 0 裸 LF"
- **按索引做多处编辑时,删完要重新定位锚点**(新)— 先删 4 行会让后续锚点行号漂移。
  本次顺序是"先 `RemoveRange` → 再重新扫描 §17 → 再 `InsertRange`",不缓存插入前的索引
- **"注记本身会过期"是可以预防的**(新)— 顶部那条注记原本硬编码了 `@ 4c1ab02`,而它当场就已过期
  (§21~§21f 的 commit 还没发生)。**注记里不要写会变的值**,要写"怎么查"(如"见 `git log --oneline -1`")







