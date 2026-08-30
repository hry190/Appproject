# 项目环境说明

> 给新加入项目的协作者阅读。本文帮你快速了解技术栈、本地环境配置、跑通构建,以及容易踩的坑。

## 1. 项目概况

**应用名**: 江湖 (Jianghu)
**包名**: `com.jueqiao.jianghu`
**类型**: Android 原生应用(单 Module,纯 Kotlin + Jetpack Compose)
**当前状态**: 还在功能开发阶段,不是成品

主要功能:古风主题的"创作辅助"类 App,包含登录、首页、聊天结果、生图、保存作品等模块。

## 2. 技术栈

| 类别 | 工具 / 版本 |
|---|---|
| **编译目标** | Java 17 (`VERSION_17`) |
| **Gradle 运行时** | JBR 21.0.11(`org.gradle.java.home` 强制指定) |
| **Gradle** | 8.10.2(通过 wrapper) |
| **Android Gradle Plugin** | 8.7.3 |
| **Kotlin** | 2.0.21(含 Compose Compiler Plugin) |
| **Jetpack Compose BOM** | 2024.12.01 |
| **Navigation Compose** | 2.8.4 |
| **Coil** | 2.7.0(图片加载) |
| **OkHttp** | 4.12.0(网络) |
| **Gson** | 2.11.0(JSON 解析) |
| **compileSdk / targetSdk** | 35(Android 15) |
| **minSdk** | 24(Android 7.0) |

## 3. 本地环境要求

### 3.1 JDK(必须装两个)

| 用途 | 推荐 | 来源 |
|---|---|---|
| **Gradle 跑构建** | **JBR 21.0.11** | Android Studio 自带的 JetBrains Runtime,**不要用本机 JAVA_HOME 指向的 JDK 25** |
| **编译目标** | Java 17 字节码 | `compileOptions` 设定的,不需要本地装 17 |

**为什么不能直接用 JDK 25?**
- AGP 8.7.3 **不支持 JDK 25**(`gradle.properties` 里的注释说明)
- 项目已经在 `gradle.properties` 强制指定了 JBR 21(`org.gradle.java.home`)
- 如果你本机 `JAVA_HOME` 是 JDK 25,**别改它**,把 JBR 21 留给 Gradle 自己用即可

**JBR 21 装在哪?**
- 默认路径:`C:\Users\<用户>\.jdks\jbr-21.0.11\`
- Android Studio → Settings → Build, Execution, Deployment → Build Tools → Gradle → "Gradle JDK" 里可以重新下载 / 选择

### 3.2 Android SDK

需要装:
- Android SDK Platform 35(`compileSdk = 35`)
- Build-Tools 35.0.0
- Platform-Tools
- (可选) Android 15 系统镜像(用模拟器时)

`local.properties` 里指定 SDK 路径:
```properties
sdk.dir=D\:\\Android\\Sdk
```

### 3.3 IDE

推荐 **Android Studio Koala (2024.1.1)** 或更新版本,因为:
- Compose 编译器插件需要 IDE 支持 Kotlin 2.0+
- Android SDK Manager 内置,装 SDK 方便

## 4. 跑通构建

### 4.1 第一次克隆项目

```bash
# 1. 克隆
git clone <repo-url>
cd Appproject

# 2. 创建 local.properties(这个文件**不要提交**)
# Windows:
copy NUL android\local.properties
# macOS/Linux:
touch android/local.properties
```

`local.properties` 最小内容:
```properties
sdk.dir=<你的 Android SDK 路径>
# 可选:Minimax API 配置
MINIMAX_BASE_URL=https://api.MiniMax.cn/v1
MINIMAX_API_KEY=<你的 key>
```

### 4.2 构建命令

```bash
cd android

# Debug 构建(本地开发)
./gradlew assembleDebug

# Release 构建(生产)
./gradlew assembleRelease

# 清理
./gradlew clean

# 完整重新构建(改了 build.gradle.kts 或 libs.versions.toml 后)
./gradlew clean assembleDebug
```

构建产物在 `android/app/build/outputs/apk/` 下。

### 4.3 安装到设备

```bash
# 用 Gradle 直接装
./gradlew installDebug

# 或者手动装生成的 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 5. 项目结构

```
Appproject/
├── android/                           # Android 项目根
│   ├── app/
│   │   ├── build.gradle.kts          # 模块级 Gradle 配置
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/jueqiao/jianghu/
│   │   │   │   ├── JianghuApp.kt           # Application 类
│   │   │   │   ├── MainActivity.kt         # 启动 Activity
│   │   │   │   ├── data/                   # 静态数据 / 校验
│   │   │   │   ├── nav/                    # Navigation Compose 配置
│   │   │   │   │   ├── JianghuNavHost.kt
│   │   │   │   │   └── Routes.kt
│   │   │   │   ├── network/                # API 客户端
│   │   │   │   │   └── MiniMaxApi.kt
│   │   │   │   └── ui/
│   │   │   │       ├── components/          # 通用 UI 组件
│   │   │   │       ├── screens/             # 各页面
│   │   │   │       └── theme/               # 主题/颜色/字体
│   │   │   └── res/                         # 资源文件
│   │   │       ├── drawable/                # 图片、矢量图(@1x)
│   │   │       ├── drawable-xxhdpi/         # 高清图(@2x)
│   │   │       ├── values/                  # strings/colors/themes
│   │   │       └── mipmap-*/                # 应用图标
│   ├── build.gradle.kts              # 项目级 Gradle
│   ├── gradle/
│   │   ├── libs.versions.toml        # 版本目录(单一来源)
│   │   └── wrapper/
│   ├── gradle.properties            # 关键配置:强制 JBR 21、代理等
│   ├── settings.gradle.kts
│   ├── local.properties              # 本地配置(**不进 git**)
│   ├── gradlew / gradlew.bat
│   └── README.md
├── clean_text.py                     # ⚠ 根目录脚本(项目无关,见 §8)
├── write_vertical_text.py            # ⚠ 同上
├── hs_err_pid63956.log              # ⚠ JVM 崩溃 dump(应清理)
├── replay_pid63956.log              # ⚠ JVM 崩溃 dump(应清理)
└── README.md
```

## 6. 核心约定

### 6.1 路由结构

所有路由常量定义在 `android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt`:

```kotlin
object Routes {
    const val Home1 = "home1"
    const val ChatResult = "chat_result"
    fun chatResult(query: String): String = "chat_result/$encodedQuery"
    // ...
}
```

**当前启动页** = `Routes.ChatResultPattern`(带 query 参数),从 ChatResult 进入 Shengtu → Picture 的链路。

### 6.2 Compose 命名 / 资源

- **Screen 文件**:`<PageName>Screen.kt`,放在 `ui/screens/<page>/`
- **组件文件**:`<ComponentName>.kt`,放在 `ui/components/`
- **drawable 命名**:`img_<page>_<source>_<id>.png`(例如 `img_shengtu_group253.png`)
  - 中文文件名(如 "Rectangle 227.png")先转拼音 / 简化,再用 `_` 分隔
  - `@1x` 放 `drawable/`,`@2x` 放 `drawable-xxhdpi/`

### 6.3 主题 / 字体

字体统一用 `ui/theme/Type.kt` 里定义的 `YaHei` 单例,所有 Text/TextField 都用它:
```kotlin
style = TextStyle(fontFamily = YaHei, ...)
```

### 6.4 关键页面

| 页面 | 路径 | 用途 |
|---|---|---|
| ChatResultScreen | `ui/screens/chatresult/` | 聊天结果(启动页) |
| ShengtuScreen | `ui/screens/shengtu/` | 生图页 |
| PictureScreen | `ui/screens/picture/` | 图片结果页 |
| GongfangScreen | `ui/screens/gongfang/` | 工坊页 |
| ZaowuScreen | `ui/screens/zaowu/` | 作品创作页 |
| Home1Screen | `ui/screens/home/` | 首页 1 |

## 7. 常见操作

### 7.1 添加新页面

```kotlin
// 1. 在 Routes.kt 加常量
object Routes {
    const val NewPage = "new_page"
}

// 2. 创建 ui/screens/newpage/NewPageScreen.kt
@Composable
fun NewPageScreen(onBack: () -> Unit = {}) {
    // ...
}

// 3. 在 JianghuNavHost.kt 注册 composable
composable(Routes.NewPage) {
    NewPageScreen(
        onBack = { navController.popBackStack() },
    )
}
```

### 7.2 加新的依赖

去 `android/gradle/libs.versions.toml` 加版本号,然后在 `app/build.gradle.kts` 里 `implementation(libs.xxx)`。

### 7.3 改启动页

`android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt`:
```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.<路由名>,
    ...
)
```

## 8. ⚠ 容易踩的坑

### 8.1 不要直接用 JAVA_HOME 配的 JDK 跑 Gradle

如果有人改了 `gradle.properties` 删了 `org.gradle.java.home` 那行,build 会失败并报 "What went wrong: 25.0.3"。**别删那行**。

### 8.2 不要 commit `local.properties`

这个文件含 SDK 路径和 API Key。已经在 `.gitignore` 里,别 force-add 进去。

### 8.3 根目录的 Python 脚本和 JVM 崩溃文件**不属于项目**

- `clean_text.py`、`write_vertical_text.py` —— 临时工具脚本,跟 Android 项目无关
- `hs_err_pid*.log`、`replay_pid*.log` —— JVM 崩溃 dump,不应该提交

新人别被这些文件误导,以为它们是项目一部分。

### 8.4 AndroidManifest 缺 `INTERNET` 权限

当前 manifest 里**没有声明** `<uses-permission android:name="android.permission.INTERNET" />`。如果遇到 `MiniMaxApi` 调不通的问题,先检查这个。

### 8.5 Compose 中绝对定位元素很多

绝大多数页面用 `Box + offset/size` 绝对定位,**没用 LazyColumn / Column**。这意味着:
- 长内容会超出屏幕(底部元素可能被裁)
- 状态栏 / 导航栏适配是手算的(没加 statusBarPadding)

### 8.6 输入框可能渲染异常

`ShengtuScreen` 和 `PictureScreen` 的"输入框"用了 **BasicTextField + Text 叠加**的非标准结构(为了绕过 Material3 TextField 的 placeholder 显示问题)。如果换 Material3 版本,这块要重测。

### 8.7 resources 中部分 drawable 密度放错

历史原因,部分 `@1x` 素材放在了 `drawable-xxhdpi/`,渲染时会**放大 3 倍变模糊**。修复前先和设计确认正确密度版。

## 9. 调试 / 排查

### 9.1 Gradle 报 "What went wrong: 25.0.3"

JDK 版本太新。检查:
1. `gradle.properties` 的 `org.gradle.java.home` 是否还指向 JBR 21
2. 本机有没有人改了 JAVA_HOME → 还原成 JDK 25 即可(JDK 25 是给别处用的,别动它)

### 9.2 build 报 "Unresolved reference X"

大概率是缺 import。Compose 的 modifier / 函数很多,IDE 通常会标红提示,**照着加 import 就行**。

### 9.3 APK 装到设备上白屏

- 看 logcat 过滤项目包名,找 `FATAL EXCEPTION`
- 大概率是 `BackHandler`、`FocusRequester`、网络权限等问题

### 9.4 启动直接崩

- AndroidManifest 没注册 Activity → 检查 manifest
- Application 类名写错 → 检查 `android:name=".JianghuApp"`

## 10. 联系 / 文档

代码内的中文注释是主要的设计文档。每个 Screen 文件顶部都有 KDoc 简要说明用途。
