# 协同工作注意事项

本项目是 Android 端 Jetpack Compose 应用,使用 Navigation Compose 路由。本文件记录**未在代码里固化、但实际在用的协作约定**,新加入的人(或 AI 助手)请先读完再动代码。

---

## 1. 图片资源命名

项目里 drawable 资源实际遵循的命名格式是:

```
img_<页面拼音>_<Figma 节点名>.png
```

例:
- `img_shengtu_bg.png`(生图页背景)
- `img_shengtu_group196.png`(生图页 Group 196 节点)
- `img_gongfang_return.png`(返回箭头,任何页面都引用)
- `img_picture_vector611.png`(图片页 Vector 611 节点,要素页也复用同一文件)

**没写在任何文件里**——是 Figma 导出时的命名习惯,后来大家都照着走。新增资源时按这个格式,不要自己起新前缀。

---

## 2. 资源存放目录

| 目录 | 用途 |
|---|---|
| `res/drawable/` | 普通密度,放通用元素(背景、装饰图、按钮) |
| `res/drawable-xxhdpi/` | `@2x` 高密度资源(像素 184×286 这种) |

不确定就放 `drawable/`,与 Shengtu 的背景图同目录。

---

## 3. 新建页面

从**最相似的现有页面复制**起步(项目里 Shengtu / Picture / Yaoosu / ChatResult 几乎是一份模板)。不要做过度抽象,不要抽公共 Composable——用户明确偏好直接复制。

**标准目录结构**:
```
android/app/src/main/java/com/jueqiao/jianghu/ui/screens/<拼音>/
    <拼音首字母大写>Screen.kt
```

**复制时改三处**:
1. `package com.jueqiao.jianghu.ui.screens.<拼音>`
2. `fun <拼音首字母大写>Screen(...)`
3. 文件顶部 KDoc:标注复制来源 + "后续会按需修改"

---

## 4. 路由命名(`Routes.kt`)

- 命名风格:页面名拼音,2-4 字,全部小写,无下划线
  - 例: `gongfang`、`shengtu`、`picture`、`chuangzuodangan`
- **不**用驼峰、不用连字符、不带 `Page` 后缀
- 参数化路由三件套:
  ```kotlin
  const val Xxx         = "xxx"          // 普通跳转用
  const val XxxPattern  = "xxx/{param}"  // NavHost composable 用
  fun xxx(param: String): String = "xxx/$param"  // 代码里 navigate 用
  ```
  中文/特殊字符走 `URLEncoder.encode`。

---

## 5. 通用页面元素(各页面顶部复制粘贴区)

下面这几组在不同页面**位置/尺寸都一致**,新建页面时直接拷,不要重新设计:

| 元素 | 位置 / 尺寸 | 资源 |
|---|---|---|
| 全屏背景 | `fillMaxSize` + `ContentScale.Crop` | 各页自定 |
| 返回按钮 | `(20, 41)` `32×32`,内图 `24×24` | `img_gongfang_return` |
| 教练辅助装饰 | `(57, 29)` `160×58` | `img_gongfang_23` |
| "教练辅助" 文字 | `(93, 46)` `71×18`,14sp 黑字 | — |
| 创作档案装饰 | `(265, 35)` `127×46` | `img_gongfang_24` |
| "创作档案" 文字 | `(287, 46)` `76×25`,14sp 黑字 | — |

**可点击规则**:
- 创作档案组(图 + 文字)在**大多数页面**都加 `.clickable(onClick = onOpenChuangzuodangan)` → 跳 `Routes.Chuangzuodangan`
- **教练辅助组全部不可点击**(用户明确偏好)
- **ChuangzuodanganScreen 自身的创作档案组不可点击**(避免自跳死循环)
- 屏幕参数统一加 `onOpenChuangzuodangan: () -> Unit = {}`,在 `JianghuNavHost.kt` 里统一接 `navigate(Routes.Chuangzuodangan)`

---

## 6. 屏幕回调约定

```kotlin
@Composable
fun XxxScreen(
    onBack: () -> Unit = {},            // 返回,默认 popBackStack
    onCreateWork: () -> Unit = {},      // 跳下一个流程页
    onOpenXxx: () -> Unit = {},         // 跳指定页面,默认空实现
)
```

**默认值要带**( `= {}` ),这样不接回调也不会编译报错**。新增的跳转能力按这个模式加,不要直接改既有签名。

`BackHandler` 每个屏幕都要写,行为与左上角返回按钮一致。

---

## 7. 注释里的坐标

每个元素上方常有一行 `// 原文件名(X=?, Y=?, W=?, H=?)` 注释,作为快速参考。

**改了 Modifier 里的 offset/size 后,务必同步注释里的坐标**——不然下次有人查"Vector 611 在哪",会被旧注释误导。

---

## 8. NavHost 接线

`JianghuNavHost.kt` 里每个 `composable(Routes.X)` 块:

1. 在文件顶部加 `import com.jueqiao.jianghu.ui.screens.xxx.XxxScreen`
2. 在 `NavHost` 里加:
   ```kotlin
   composable(Routes.Xxx) {
       XxxScreen(
           onBack = { navController.popBackStack() },
           onOpenXxx = { navController.navigate(Routes.Xxx) },
           // ...
       )
   }
   ```
3. **不**用 `popUpTo` 除非有特殊需求(默认 navigate 会保留来源页在栈里)

---

## 9. 资源导入流程

设计稿源文件都在 `D:\图\` 下。复制流程:

1. `cp "D:\图\<file>.png" android/app/src/main/res/drawable/img_<page>_<node>.png`
2. 在屏幕代码里 `painterResource(R.drawable.img_<page>_<node>)`
3. 注释里**不要**写 `D:\图\...` 磁盘路径(那是临时来源,不是项目资产)

**大背景图特别提醒**:`D:\图\` 下动辄 4-5 MB 的 PNG,搬进项目会显著拖大 APK。**搬之前问一下要不要先压缩**(用户那次把 4.9 MB 压到 2.16 MB,效果立竿见影)。

---

## 10. 不要做的事

- ❌ 不要抽公共 Composable(`PictureContent()` 之类的)——用户明确偏好直接复制
- ❌ 不要给教练辅助组加 `.clickable`(用户已说"暂时保持不可点击")
- ❌ 不要让 `ChuangzuodanganScreen` 自身的创作档案组可点击(自跳死循环)
- ❌ 不要修改"复制来源"KDoc,除非改的是自己写的
- ❌ 不要在 NavHost 里随便加 `popUpTo`(会破坏返回栈)

---

## 11. 缩进注意

多数文件用 16 空格缩进,**`GongfangScreen.kt` 是 12 空格**(少一层嵌套)。改 GongfangScreen 时别把别的文件的缩进风格带过去。

---

## 12. 文件顶部清单

新建/修改屏幕后,在项目根的 `MEMORY.md`(若有)或 PR 描述里说明:
- 改动文件清单(相对路径)
- 是否新建了 drawable 资源
- 是否改了 NavHost
- 是否加了新的 Route 常量

便于 Code Review 快速对账。