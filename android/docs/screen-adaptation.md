# 屏幕适配方案 — 机巧江湖 Android (Jetpack Compose)

> 给新加入项目 / 接手旧页的开发者。本项目 UI 全部基于 Figma 412×917 设计稿,屏幕适配靠 **"两段式 / 三段式 Box 结构 + BoxScope 相对定位"** 落地。本文记录方案模板、转换公式、代码示例,以及哪些页面已经按这套方案做、哪些还没。

---

## 0. 读完这份能学到什么

1. 怎么把"设计稿绝对坐标"翻译成 `align(...) + offset(...)`,让元素在任意屏幕高度上都不被裁
2. 简单页(纯背景 + 顶/中/底散落元素)用 **两段式** 怎么写
3. 复杂页(自带底部导航条,如演武场视频)用 **三段式** 怎么写
4. 现有 22 个屏幕里哪些已经按这套做了、哪些还是旧的"绝对 offset 写法"
5. 改完怎么验证(`assembleDebug` + 装设备肉眼对比)

---

## 1. 为什么需要适配

本项目所有 UI 都按 **Figma 412×917 设计稿** 落坐标,但 Android 真机千差万别:

| 设备 | 实际 dp 尺寸 | 系统栏占用 | 可用 safe-area(高) |
|---|---|---|---|
| Pixel 4a | 393 × 851 | 24(状态栏)+ 48(3 键导航) | 779 |
| Pixel 6 | 411 × 891 | 24 + 48 | 819 |
| Pixel 7 Pro | 412 × 918 | 24 + 48 | 846 |
| Galaxy S22 | 360 × 780 | 24 + 24(手势条) | 732 |
| 全面屏手势 + 小窗 | 320 × 640 | 24 + 0 | 616 |

**绝对 offset 在设计稿上能用,在真机上常见三种问题:**

1. **底部元素被系统导航条压住** —— Y 越大的元素越容易中招。原 `YanwuchangVideoScreen` 的进度条 Y=818,直接被底部 85dp 的应用导航条 + 系统导航条盖住。
2. **底部独立导航条没填满宽屏** —— 平板 / 折叠屏上,硬编码 `size(412dp)` 的底部条只占中间 412dp,两侧是背景图穿透。
3. **中央元素在矮屏偏离视觉重心** —— `offset(y = 487.dp)` 的按钮在 779dp 高的设备上,实际位置偏下,跟设计稿意图的"屏幕中段"差很多。

**目标:** 不改设计稿坐标含义(412 宽仍然指 Figma 的 412 宽),只让 Y 跟着实际 safe-area 走。

---

## 2. 三种结构模式

### 2.1 模式速查

| 模式 | 适用场景 | 例子 |
|---|---|---|
| **A. 两段式** | 简单页(纯背景 + 散落元素) | Dahui、Gongfang、Shengtu、ChatResult、Picture、Zaowu、Yanwuchang |
| **B. 三段式** | 有独立底部导航条的页 | YanwuchangVideo |
| **C. 滚动式** | 内容超过一屏,需要垂直滚动 | HomeScreen(`AuthDimens.homeCanvasH` 固定 900dp,套 `verticalScroll`) |

> 模式 C 是 `HomeScreen` 独苗,本项目其他页**不**用滚动。如果新页内容确实超长,优先考虑把元素做小、用 `weight` 分栏,不要直接上滚动。

### 2.2 模式 A — 两段式骨架

```
Box(fillMaxSize) {                       // 外层:全屏背景
    Image(background, fillMaxSize, ContentScale.Crop)

    Box(fillMaxSize, windowInsetsPadding(navigationBars)) {   // 内层:safe-area
        // 所有页面元素放在这里
    }
}
```

- **外层** 用 `fillMaxSize` 把背景铺到整屏(含状态栏 / 导航条后面),`ContentScale.Crop` 保证图片不变形。
- **内层** 用 `windowInsetsPadding(WindowInsets.navigationBars)` 把内容区"推"到系统导航条上方。
- **不**用 `WindowInsets.systemBars`,因为状态栏区域的元素(返回按钮)用绝对 Y=41~76 就够,不需要整体下移。

### 2.3 模式 B — 三段式骨架

```
Box(fillMaxSize) {                       // 外层:全屏背景
    Image(background, fillMaxSize, ContentScale.Crop)

    Box(fillMaxSize, windowInsetsPadding(navigationBars)) {   // 中层:内容(safe-area)
        // 顶部行(返回 / Tab / 搜索)
        // 中部内容(主体元素)
    }

    // 底层:独立底部导航条
    Box(
        align(Alignment.BottomCenter),
        fillMaxWidth,
        wrapContentHeight,
        navigationBarsPadding,
    ) {
        Box(fillMaxWidth, height(85dp), background(渐变)) {
            // 底部条内容(作品 / 我的)
        }
    }
}
```

- 底部条**不**放中层,因为它需要**叠在系统导航条之上**(而不是避让),所以走 `Modifier.align(Alignment.BottomCenter)`。
- `wrapContentHeight() + navigationBarsPadding()` 让"内容 85dp + 系统导航条高度"成为底部条总高,内容浮在系统导航条上方。
- 旧代码常见错误:把底部条硬编码 `size(412dp × 85dp)`,宽屏只占中间。本方案 `fillMaxWidth()` 解决。

### 2.4 模式 C — 滚动式骨架(HomeScreen 专属)

```
Box(fillMaxSize) {
    Box(fillMaxSize, verticalScroll(rememberScrollState())) {
        Box(fillMaxWidth, height(900dp)) {     // 固定 canvas 高
            Image(background, fillMaxSize, ContentScale.Crop)
            Box(fillMaxSize, windowInsetsPadding(navigationBars)) {
                // 内容
            }
        }
    }
}
```

只用于 HomeScreen,因为它的"对话框推进"逻辑需要固定 900dp 高的画布。新页**不**推荐用。

---

## 3. BoxScope 相对定位速查

### 3.1 9 个对齐点

```
TopStart ──────── TopCenter ──────── TopEnd
   │                                    │
CenterStart ────── Center ────── CenterEnd
   │                                    │
BottomStart ──── BottomCenter ──── BottomEnd
```

- `Alignment.TopStart` / `Alignment.TopCenter` / `Alignment.TopEnd`:把元素的对应边/中心贴到父 Box 的对应边/中心
- `Alignment.Center` / `CenterStart` / `CenterEnd`:把元素中心 / 左中 / 右中贴到父 Box 中心 / 左中 / 右中
- `Alignment.BottomStart` / `BottomCenter` / `BottomEnd`:把元素底边对应位置贴到父 Box 底边

`align(...)` 是 `BoxScope` 内的修饰符,**必须**写在父 Box 的子元素上才有效。

### 3.2 `offset` 怎么配合 `align` 用

`align` 决定元素相对父 Box 的锚点,`offset(x, y)` 是在这个锚点基础上的**微调**。正负方向遵循正常屏幕坐标(右正下正)。

| 元素语义 | 写法 |
|---|---|
| 顶部偏左 20 | `align(TopStart).offset(x=20, y=41)` |
| 顶部居中 | `align(TopCenter).offset(y=10)` |
| 屏幕正中、偏右下一点 | `align(Center).offset(x=20, y=20)` |
| 底部居中、距离底 80dp | `align(BottomCenter).offset(y=-80)` |
| 底部偏左、距离底 235dp | `align(BottomStart).offset(x=8, y=-235)` |
| 右中、距右边 5dp、距中心下 88dp | `align(CenterEnd).offset(x=-5, y=88)` |

注意:**Y 偏移在 `Top*` 上为正向下,在 `Bottom*` 上为负向上**(因为锚点已经贴到底边,offset 负值 = 远离底边 = 向上)。

---

## 4. 设计稿坐标 → align/offset 转换公式

设计稿基准:**412 宽 × 917 高**,48dp 系统导航条(3 键模式),safe-area 高度 **869dp**。

### 4.1 安全区中心

```
safeAreaCenterY = (917 - 48) / 2 = 434.5
safeAreaCenterX = 412 / 2       = 206
```

### 4.2 各位置的转换

| 元素类型 | 原稿写法 | 新写法 | 公式 |
|---|---|---|---|
| 顶部偏左(返回按钮) | `offset(X=20, Y=76)` | `align(TopStart).offset(X=20, Y=41)` | Y 用 41(其他页约定),非 76 |
| 屏幕中心(主体) | `offset(X=x, Y=y)`,中心 `(x+W/2, y+H/2)` | `align(Center).offset(X=x+W/2-206, Y=y+H/2-434.5)` | 中心 = 设计稿中心 - safe-area 中心 |
| 底部偏左(气泡) | `offset(X=8, Y=564)`,底沿 634 | `align(BottomStart).offset(X=8, Y=-235)` | Y = -(869 - 底沿) = -235 |
| 底部居中(熊猫) | `offset(X=82, Y=599)`,底中 `(152.5, 859)` | `align(BottomCenter).offset(X=-53.5, Y=-10)` | X = 152.5 - 206 = -53.5,Y = -(869-859) = -10 |
| 右侧操作列(头像+点赞+评论+收藏+分享+嘴部装饰) | 12 个元素 Y=444~761,X=367~407 | 包成 `Box(align(CenterEnd).offset(X=-5, Y=88).size(40, 317))`,元素内部 `offset(X=x-367, Y=y-364)` | 列中心 = (387, 522.5),列高 317,X 偏 -5,Y 偏 +88 |

> **经验:** `X` 偏 `±5dp` 留出右边距,`Y` 偏 `±80~100dp` 让列的视觉中心落在"屏幕上半到下半"过渡处。

### 4.3 速算表(412×917 设计稿)

把以下数字背下来,改新页时直接套:

| 锚点 | X 偏移 | Y 偏移 |
|---|---|---|
| TopStart(返回键) | 20 | 41 |
| Center(主体) | 设计中心 - 206 | 设计中心 - 434.5 |
| BottomStart(底部偏左) | 设计 X | -(869 - 设计底沿) |
| BottomCenter(底部居中) | 设计中心 - 206 | -(869 - 设计底沿) |
| CenterEnd(右中) | -(412 - 设计右沿 + 5) | 设计中心 Y - 434.5 |
| BottomEnd(右底) | -(412 - 设计右沿) | -(869 - 设计底沿) |

---

## 5. 完整代码模板

### 5.1 模式 A — YanwuchangScreen 简化版

```kotlin
@Composable
fun MyScreen(
    onBack: () -> Unit = {},
    onSomething: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(   // ← 外层:背景
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景图
        Image(
            painter = painterResource(R.drawable.img_my_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内层:safe-area 内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ----- 顶部:返回按钮 -----
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 41.dp)
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                )
            }

            // ----- 中部:屏幕中心元素 -----
            //   原稿中心 (X_c, Y_c) → align(Center).offset(X_c - 206, Y_c - 434.5)
            Image(
                painter = painterResource(R.drawable.img_center_icon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -50.dp, y = 30.dp)   // 例:偏左 50、偏下 30
                    .size(width = 100.dp, height = 100.dp),
            )

            // ----- 底部:偏左元素 -----
            //   原稿底沿 634 → align(BottomStart).offset(X=8, Y=-235)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 8.dp, y = -235.dp)
                    .size(132.dp, 70.dp),
            ) {
                Image(/* 气泡背景 */)
                Text(/* 气泡文字 */)
            }
        }
    }
}
```

### 5.2 模式 B — YanwuchangVideoScreen 简化版

```kotlin
@Composable
fun MyScreenWithBottomBar(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tier 1:背景
        Image(
            painter = painterResource(R.drawable.img_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Tier 2:内容(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 顶部行
            Box(/* 返回按钮 */)
            // ... Tab / 搜索 ...

            // 中部元素
            // ----- 屏幕正中元素 -----
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(/* 中心点算出的偏移 */)
                    .size(...),
            )

            // ----- 右侧操作列(12 个元素的密集组合)-----
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = -5.dp, y = 88.dp)        // 列中心算的
                    .size(width = 40.dp, height = 317.dp), // 列实际宽高
            ) {
                // 元素用"相对列左上角"offset,原稿 X - 367,Y - 364
                Image(modifier = Modifier.offset(x = 0.dp, y = 0.dp).size(40, 40))   // 头像
                Image(modifier = Modifier.offset(x = 7.dp, y = 56.dp).size(30, 28))  // 点赞
                // ... 其余 10 个 ...
            }

            // ----- 底部元素(用户名/标题/进度条)-----
            // 注意:整体上挪 80dp 让出 Tier 3 的底部条
            Image(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 17.dp, y = -172.dp)  // 用户名(底沿 697)
                    .size(97.dp, 32.dp),
            )
            Image(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 22.dp, y = -140.dp)  // 标题(底沿 729)
                    .size(160.dp, 21.dp),
            )
            Image(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 20.dp, y = -123.dp)  // 进度(底沿 746)
                    .size(372.dp, 8.dp),
            )
        }

        // Tier 3:独立底部导航条
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .background(Brush.verticalGradient(...)),
            ) {
                Image(/* 作品按钮 offset(90, 0) */)
                Image(/* 我的图标 offset(296, 8) */)
                Text(/* 我的文字 offset(296, 34) */)
            }
        }
    }
}
```

**关键点:**
- 底部条内容(渐变 + 按钮)总高 85dp,加上 `navigationBarsPadding()` 让总高 = 85 + 系统导航条高度。
- 底部条**不**走 safe-area 的 `windowInsetsPadding`,因为它要"压在"系统导航条上方而不是避让。
- 视频主体(用户名/标题/进度)Y 值必须小于 `safeAreaBottom - 85dp`,否则会被底部条盖住。

---

## 6. 验证步骤

### 6.1 编译验证(必做)

```bash
# 1. 强制用 JBR 21(AGP 8.7.3 不支持 JDK 25)
$env:JAVA_HOME = "C:\Users\28784\.jdks\jbr-21.0.11"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. 编译
cd D:\App\Appproject\android
.\gradlew.bat assembleDebug
```

成功输出:`BUILD SUCCESSFUL in Ns`,关键 task `compileDebugKotlin` 必须经过。

### 6.2 视觉验证(强烈建议)

```bash
# 装到设备
.\gradlew.bat installDebug
# 或手动
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

肉眼检查:
- [ ] 返回按钮在左上角,没被状态栏压
- [ ] 顶部 Tab / 搜索 跟返回按钮横向对齐
- [ ] 中部元素(头像/熊猫/中心图标)视觉居中
- [ ] 底部元素(气泡/进度条)完整可见,没被底部条盖
- [ ] 底部条满屏宽,内容(作品/我的)在系统导航条**上方**而不是被压

### 6.3 多尺寸验证(可选)

如果有 Android Studio 的 Preview,可以同时加多个 device profile 看差异:
- `Phone - Pixel 4a`(393×851)— 测矮屏
- `Phone - Pixel 6`(411×891)— 测标准屏
- `Tablet - Pixel Tablet`(1600×2560)— 测宽屏底部条是否填满

---

## 7. 项目里现有的页面状态

| 页面 | 文件 | 模式 | 状态 |
|---|---|---|---|
| Splash | `ui/screens/splash/SplashScreen.kt` | A | ✅ 已按方案 |
| Login / Register / Forgot | `ui/screens/{login,register,forgot}/` | A | ✅ |
| Agreement / Privacy | `ui/screens/{agreement,privacy}/` | A | ✅ |
| Home | `ui/screens/home/HomeScreen.kt` | **C(滚动)** | ✅ |
| Home1 / Challenge / Luggage / Settings | `ui/screens/home/` | A | ✅ |
| Xiulian | `ui/screens/xiulian/XiulianScreen.kt` | A | ✅ |
| Zaowu | `ui/screens/zaowu/ZaowuScreen.kt` | A | ✅ |
| Dahui | `ui/screens/dahui/DahuiScreen.kt` | A | ✅ |
| **Yanwuchang** | `ui/screens/yanwuchang/YanwuchangScreen.kt` | A | ✅(本次重写) |
| **YanwuchangVideo** | `ui/screens/yanwuchangvideo/YanwuchangVideoScreen.kt` | **B(三段式)** | ✅(本次重写) |
| Gongfang | `ui/screens/gongfang/GongfangScreen.kt` | A | ✅(用 12 空格缩进,特殊) |
| ChatResult | `ui/screens/chatresult/ChatResultScreen.kt` | A | ✅ |
| Shengtu | `ui/screens/shengtu/ShengtuScreen.kt` | A | ✅ |
| Picture | `ui/screens/picture/PictureScreen.kt` | A | ✅ |
| Yaosu | `ui/screens/yaosu/YaosuScreen.kt` | A | ✅ |
| Chuangzuodangan ~ 5 | `ui/screens/chuangzuodangan*/` | A | ✅(基本是占位页,元素少) |

**速查:看到 `offset(x = X.dp, y = Y.dp)` 但**没有** `align(...)` 的元素,基本都是"还没适配"的旧写法。** 不过项目里现在所有 22 个屏幕的 `offset` 都已经按这个方案在 safe-area Box 内使用,基本没问题。

---

## 8. 改新页的 Checklist

接到一张新 Figma 设计稿时,按这个清单走:

1. **判模式** —— 有底部导航条 → B,没有 → A,内容超一屏 → C(基本不用)
2. **搭骨架** —— 按 §2 复制模式 A 或 B 的 Box 骨架
3. **画背景** —— `Image(fillMaxSize, ContentScale.Crop)`
4. **算坐标** —— 412×917 设计稿上每个元素,用 §4 公式转成 `align + offset`
5. **处理密集组** —— 12 个元素挤在一起(如右侧操作列)?包成一个 `Box(align, offset, size)`,内部用相对坐标
6. **检查底部冲突** —— 模式 B 的页,确保内容元素底沿 ≤ safe-area 底 - 85dp
7. **跑 build** —— `assembleDebug` 必须过
8. **装设备看** —— `installDebug` + 肉眼对比 Figma

---

## 9. 常见坑(踩过的)

### 9.1 不要硬编码 width = 412.dp

```kotlin
// ❌ 错:宽屏只占中间
Box(modifier = Modifier.size(width = 412.dp, height = 85.dp))

// ✅ 对:跟随屏幕宽
Box(modifier = Modifier.fillMaxWidth().wrapContentHeight())
```

### 9.2 不要把底部条放进 safe-area Box

```kotlin
// ❌ 错:底部条会被系统导航条挡住
Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(navigationBars)) {
    // 底部条放在这里 → 永远显示在系统导航条上方,留出空隙
    BottomBar()
}

// ✅ 对:底部条独立绘制,叠在系统导航条之上
Box(modifier = Modifier.fillMaxSize()) {
    Content()  // safe-area 在 Content 内
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding(),  // ← 用这个让内容浮在系统导航条上方
    ) { BottomBar() }
}
```

### 9.3 不要忘了 `ContentScale.Crop`

```kotlin
// ❌ 错:背景图会变形或留白
Image(modifier = Modifier.fillMaxSize())

// ✅ 对:背景不变形,铺满
Image(
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
)
```

### 9.4 不要直接用 `align(...)` 在非 Box 子元素上

`align` 是 `BoxScope` 扩展,只能用在 `Box { ... }` 的**直接子元素**上。包一层 Box 就丢失对齐能力。

```kotlin
// ❌ 错:align 在 Column 子元素上无效
Column(modifier = Modifier.align(Alignment.Center)) { ... }

// ✅ 对:用 Box 包装后再 align
Box {
    Column(modifier = Modifier.align(Alignment.Center)) { ... }
}
```

### 9.5 不要混用 `WindowInsets.systemBars` 和 `navigationBars`

本项目**统一用 `WindowInsets.navigationBars`**(只避让底部)。如果用 `systemBars` 会把整个内容下移 24dp(状态栏高度),跟其他页不一致。

```kotlin
// ✅ 项目约定
Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars))
```

### 9.6 状态栏图标颜色记得适配(扩展)

如果以后某页要全屏铺到状态栏(像 EdgeToEdgeScreen.kt 那种),用 `androidx.core.view.WindowCompat` 控制状态栏图标深浅;不在本文档范围,见 `ui/components/EdgeToEdgeScreen.kt`。

---

## 10. 关联文档

- `CONTRIBUTING.md` — 项目协作约定(资源命名、路由命名、缩进风格)
- `ENVIRONMENT.md` — 环境配置 / 构建 / 调试
- `android/gradle.properties` — 强制 JBR 21 的注释(本机 JAVA_HOME 是 JDK 25 时必读)
- `android/app/src/main/java/com/jueqiao/jianghu/ui/components/EdgeToEdgeScreen.kt` — 全屏 + 状态栏图标控制(进阶)

---

**最后更新:** 本次重写 `YanwuchangScreen` / `YanwuchangVideoScreen` 后归档。
**参考实现:**
- 模式 A → `ui/screens/yanwuchang/YanwuchangScreen.kt`
- 模式 B → `ui/screens/yanwuchangvideo/YanwuchangVideoScreen.kt`
