# 机巧江湖 — Native Android

机巧江湖的原生 Android 实现。从 React Native (Expo SDK 57) 项目 `../mobile/` 改写而来。

技术栈：
- **Kotlin 2.0.21** + **Jetpack Compose** + **Material 3**
- **Navigation Compose** 路由
- **Lifecycle ViewModel + StateFlow** 认证状态管理
- **OkHttp + Gson** 对接 FastAPI 认证接口
- **Android Keystore + AES/GCM** 加密保存刷新令牌
- **Coil** 异步图片
- **Noto Sans SC** 中文字体

## 目录结构

```
android/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/jueqiao/jianghu/
│   │   │   ├── JianghuApp.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── auth/             # API、Repository、ViewModel、令牌加密存储、认证模型
│   │   │   ├── nav/
│   │   │   │   ├── Routes.kt
│   │   │   │   └── JianghuNavHost.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/        # 7 个文件（Color / Type / Shape / Dimens / AuthDimens / AuthPalette / Theme）
│   │   │   │   ├── components/   # 认证表单、状态反馈、江湖风图标、按钮与卡片
│   │   │   │   └── screens/
│   │   │   │       ├── splash/SplashScreen.kt
│   │   │   │       ├── login/LoginScreen.kt
│   │   │   │       ├── register/RegisterScreen.kt
│   │   │   │       ├── forgot/ForgotScreen.kt
│   │   │   │       ├── agreement/AgreementScreen.kt
│   │   │   │       ├── privacy/PrivacyScreen.kt
│   │   │   │       ├── home/{HomeScreen, HomeOverlays}.kt
│   │   │   │       ├── xiulian/XiulianScreen.kt
│   │   │   │       ├── xingnang/XingnangScreen.kt
│   │   │   │       ├── zaowu/ZaowuScreen.kt
│   │   │   │       └── dahui/DahuiScreen.kt
│   │   │   └── data/{Validators, StaticData}.kt
│   │   └── res/
│   │       ├── drawable/         # img_*.png + ic_*.xml
│   │       ├── font/             # noto_sans_sc_*.otf
│   │       ├── mipmap-anydpi-v26/  # 启动器图标
│   │       └── values/           # strings / colors / themes
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
└── gradle/wrapper/gradle-wrapper.properties
```

## 构建

### 前置条件

- **JDK 17**（`JAVA_HOME` 指向 JDK 17）
- **Android SDK 35**（已安装 platform 35、build-tools 35.0.x）
- **Gradle 8.10.2**（项目自带 wrapper）

### 第一次构建

需要先生成 `gradle-wrapper.jar`。**Android Studio 打开项目时会自动生成**，命令行请运行：

```bash
# 选项 A：用 Android Studio 打开 android/，等 Gradle Sync 完成
# 选项 B：本地有 gradle 时执行
gradle wrapper --gradle-version 8.10.2
```

### 构建 Debug APK

```bash
cd android
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

### 认证接口配置

在 `android/local.properties` 中按运行环境覆盖以下值：

```properties
# Android 模拟器访问电脑本机 FastAPI
AUTH_BASE_URL=http://10.0.2.2:8010/
TERMS_VERSION=2026-08
PRIVACY_VERSION=2026-08
```

Debug 构建允许访问本机 HTTP 服务；Release 构建关闭明文网络，正式地址必须使用 HTTPS。
后端启动、迁移和固定开发验证码说明见 `../services/api/README.md`。

### 安装到设备/模拟器

```bash
./gradlew installDebug
# 或手动
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 启动应用

```bash
adb shell am start -n com.jueqiao.jianghu/.MainActivity
```

### Lint 检查

```bash
./gradlew lint
```

### 发行包（Release）

```bash
./gradlew assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

Release 已启用 R8 (`isMinifyEnabled = true` + `isShrinkResources = true`)。

## 主题

主题名称"水墨江湖"，见 `ui/theme/`：

| 文件 | 用途 |
|---|---|
| `Color.kt` | 9 个 Ink 浅色调 + 9 个 InkDark 深色调 |
| `AuthPalette.kt` | 认证页独立调色板（米黄背景 + 橄榄绿按钮） |
| `Dimens.kt` | 通用间距 2/4/8/12/16/24/32/64 dp |
| `AuthDimens.kt` | 认证页 412×800 设计画布尺寸 + 圆角 + 输入框高度 |
| `Shape.kt` | Material3 Shapes（radiusField=5、radiusButton=10、radiusCard=25） |
| `Type.kt` | Material3 Typography + Noto Sans SC 字体族 |
| `Theme.kt` | `JianghuTheme` 入口；按系统暗色自动切换 |

## 与 RN 原项目的差异

- **无 iOS 支持** — 仅 Android（RN 原项目 iOS + Web 同步支持）
- **无热更新 / OTA** — 通过 Play Store / 手动 APK 分发
- **状态管理** — 认证流程使用 `ViewModel + StateFlow`，页面内短生命周期输入使用 `rememberSaveable`
- **数据** — 认证页已接入 FastAPI；其他业务模块仍使用 `data/StaticData.kt` 中的演示数据

## 字体说明

`res/font/noto_sans_sc_*.otf` 来自 Windows 系统 `C:\Windows\Fonts\Noto Sans SC*.otf`。若需替换为 Microsoft YaHei：

1. 把 `msyh.ttc` 用 FontForge / ttc2ttf 工具转单字重 `.ttf`
2. 重命名为 `font_yahei.ttf` 放到 `res/font/`
3. 修改 `Type.kt` 中 `YaHei` 的引用为 `R.font.font_yahei`

## 后续可优化

- 启用 `Compose Compiler Metrics` 查看重组次数
- 添加 `Baseline Profiles` 提升冷启动性能
- 将作品、秘籍、短视频和 Agent 模块从 `StaticData.kt` 迁移到真实 API
- 添加单元测试与 Compose UI 测试
