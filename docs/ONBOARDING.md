# 新人入门指南 — Appproject (江湖驿站)

> **写给:** 新加入这个项目的人(或 AI 助手)。**先读完这一篇**,再去读 `CONTRIBUTING.md`(协作约定)和 `docs/SESSION-LOG-2026-09-09.md`(项目历史)。
>
> 这是一份"上手即用"指南 — 环境怎么搭、命令怎么敲、文件怎么命名、坑在哪里。

---

## 0. TL;DR(60 秒)

- **项目**:Android 端 Jetpack Compose 应用,主题"江湖"教学游戏;配套 FastAPI 后端
- **设备**:USB 真机(小米 K50 Pro `21908b7a`)**不是模拟器**
- **JDK**:`jbr-21` 在 `C:/Users/28784/.jdks/jbr-21.0.11`,**不是**系统 Embedded JDK(系统是 jdk-25,跑不起来)
- **核心命令**:
  ```bash
  export JAVA_HOME="C:/Users/28784/.jdks/jbr-21.0.11"
  cd d:/Appproject/android
  ./gradlew.bat compileDebugKotlin                       # 编译
  ./gradlew.bat testDebugUnitTest                        # 跑单元测试
  ./gradlew.bat assembleDebug                           # 出 APK
  adb -s 21908b7a reverse tcp:8010 tcp:8010             # 端口转发(每次插 USB/重启都要重设)
  adb -s 21908b7a install -r app/build/outputs/apk/debug/app-debug.apk
  ```

---

## 1. 技术栈

### 1.1 软件(必装)

| 软件 | 版本/位置 | 用途 |
|---|---|---|
| **JDK (jbr-21)** | `C:/Users/28784/.jdks/jbr-21.0.11` | 编译 Kotlin / Gradle |
| **Android Studio** | Hedgehog 或更新 | IDE(集成调试) |
| **Git for Windows** | 2.40+ | 版本控制(配 LF line-ending) |
| **Docker Desktop** | 4.x | 跑后端 6 个容器 |
| **小米手机 USB 驱动** | — | adb 识别小米 K50 Pro |

### 1.2 不需要装的

- ❌ **Embedded JDK (D:\Android\Studio\jbr)**:那是 Java 25,Gradle 8.10.2 只支持 Java 8-23,会直接报 "25.0.3" 然后 BUILD FAILED
- ❌ **Android Emulator**:本项目用 USB 真机,模拟器访问不到本地后端
- ❌ **JDK 17 系统环境变量**:设了反而会被 Gradle 误用,要用 jbr-21

### 1.3 编程语言

| 语言 | 用途 | 文件位置 |
|---|---|---|
| **Kotlin** (JVM 17) | Android UI + 业务逻辑 | `android/app/src/main/java/` |
| **Python** | FastAPI 后端 + 工具脚本 | `services/api/app/` |
| **TypeScript** | 暂未大量使用(将来可能要补 e2e) | — |
| **Gradle DSL (Kotlin)** | 构建脚本 | `*.gradle.kts` |

### 1.4 主要依赖

**Android 端:**
- Jetpack Compose + Material3
- AndroidX Navigation
- Coil(图片加载)
- OkHttp 4.x(网络)
- AndroidX Security Crypto(EncryptedSharedPreferences for tokens)
- 暂未引 Hilt/Koin — DI 是手写的 `JianghuApp.lateinit`

**后端:**
- FastAPI + SQLAlchemy + Redis
- pwdlib(Argon2id 哈希)
- AndroidKeyStore + GCM token 加密

---

## 2. 项目结构

```
D:\Appproject\
├── android/                    # Android 主项目
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── AndroidManifest.xml
│   │   │   │   ├── java/com/jueqiao/jianghu/
│   │   │   │   │   ├── auth/                       # 鉴权(AuthApi, AuthRepository, EncryptedTokenStore, ...)
│   │   │   │   │   ├── data/                       # Validators, StaticData
│   │   │   │   │   ├── creation/                   # 创作 ViewModel + 状态
│   │   │   │   │   ├── conference/                 # 大会 ViewModel
│   │   │   │   │   ├── distribution/               # 分发 ViewModel
│   │   │   │   │   ├── luggage/                    # 行囊 ViewModel + LuggageApi/Repository
│   │   │   │   │   ├── nav/                        # Routes + JianghuNavHost
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── components/              # 共享 Composable(StandardGunlunScaffold, ...HexagonShape)
│   │   │   │   │   │   ├── screens/<拼音>/
│   │   │   │   │   │   │   └── <拼音首字母大写>Screen.kt
│   │   │   │   │   │   └── theme/                  # 颜色、字体(YaHei)
│   │   │   │   │   └── JianghuApp.kt                # Application 单例,手写 DI 仓库
│   │   │   │   └── res/
│   │   │   │       ├── drawable/                   # mdpi 通用(背景、装饰)
│   │   │   │       ├── drawable-nodpi/             # **新建资源放这里**(默认)
│   │   │   │       └── xml/                        # network_security_config, backup_rules
│   │   │   └── test/                               # 单元测试(目前只有 3 个)
│   │   └── build.gradle.kts                        # 模块构建脚本
│   └── build.gradle.kts                            # 项目根脚本
├── services/
│   └── api/                    # FastAPI 后端
│       ├── app/
│       │   ├── core/           # config, security, middleware
│       │   ├── domains/        # 业务域(auth, learning, conference, ...)
│       │   ├── api/routes/     # HTTP 路由
│       │   └── main.py
│       ├── scripts/             # 运维脚本
│       └── tests/
├── packages/
│   └── learning-contracts/    # 跨端共享类型/合约(暂小)
├── infra/                      # Docker compose、PowerShell 启动脚本
│   └── start-dev.ps1           # **启动后端用这个**
├── docs/                      # 项目文档(本文件所在目录)
│   ├── README.md                # 索引
│   ├── SESSION-LOG-YYYY-MM-DD.md
│   ├── CODE-AUDIT-YYYY-MM-DD.md
│   ├── SUMMARY-YYYY-MM-DD-to-YYYY-MM-DD.md
│   └── ONBOARDING.md           # 本文件
├── CONTRIBUTING.md             # 协作约定(与本文件互补)
├── DEV-SETUP.md                # 详细环境配置步骤
├── ENVIRONMENT.md              # 项目根的 ENV 文件
└── D:\图\                      # ⚠ 设计稿源文件,**不在 git 里**,本地临时目录
```

---

## 3. 开发环境搭建(60 分钟首次配置)

### 3.1 Windows / Git 配置

`.gitattributes` 在项目根设了 LF line-ending,Windows 上配:
```bash
git config --global core.autocrlf input
git config --global core.eol lf
```

### 3.2 JDK

**正确做法** — 用 jbr-21,不要用系统的 jdk-25:

```bash
# 一次性检查
java -version
#  如果显示 25.0.3 或更新版本,会失败:
#  Gradle 报 "25.0.3" 然后 BUILD FAILED

# 修复:在 bash 里 export JAVA_HOME
export JAVA_HOME="C:/Users/28784/.jdks/jbr-21.0.11"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
#  应显示 "21.0.x"
```

**Android Studio 内**:Settings → Build → Build Tools → Gradle → Gradle JDK = `jbr-21`(嵌入 jbr 不是这个,**用我们指定的 jbr-21 路径**)。

### 3.3 Android Studio 配置

- File → Settings → Build, Execution, Deployment → Build Tools → Gradle
  - Gradle JDK: `C:\Users\28784\.jdks\jbr-21.0.11`
- File → Settings → Editor → Code Style → Kotlin
  - Continuation indent: 4 spaces
  - Use tab character: ❌ 不勾(用空格)

### 3.4 USB 真机连接

```bash
# 1. 手机开"开发者选项" → "USB 调试" → "USB 安装"
# 2. USB 线连 PC
# 3. adb 应识别
adb devices
#  应显示: 21908b7a    device

# 4. 端口转发 — 每次插拔/重启都重设
adb -s 21908b7a reverse tcp:8010 tcp:8010
adb -  应  回: 8010
adb -s 21908b7a reverse --list
#  应显示: UsbFfs tcp:8010 tcp:8010
```

### 3.5 后端启动

```powershell
# PowerShell 里跑(Windows 系统里)
cd D:\Appproject\infra
.\start-dev.ps1
```

启动后验证:
```bash
curl http://127.0.0.1:8010/healthz
#  应返回: {"status":"ok"}
docker ps --filter "name=jianghu"
#  应显示 6 个容器(jihu-dev-api-1, jianghu-dev-postgres-1, ...) 都是 healthy
```

### 3.6 验证 App 能跑通

```bash
# 1. 装 APK
adb -s 21908b7a install -r app/build/outputs/apk/debug/app-debug.apk

# 2. 启动 App(从 launcher)
adb -s 21908b7a shell am start -n com.jueqiao.jianghu/.MainActivity

# 3. 测试账号
#  手机: 13800138000 / Test1234!
#  curl 验证后端:
curl -X POST http://127.0.0.1:8010/v1/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"Test1234!"}'
#  应返回 200 + user info
```

### 3.7 项目内 `local.properties`

在 `android/local.properties` 设:
```properties
AUTH_BASE_URL=http://127.0.0.1:8010/
```

`local.properties` **不 commit**(.gitignore 已加),每人本地独立配置。

---

## 4. 构建命令速查

| 命令 | 作用 |
|---|---|
| `./gradlew.bat compileDebugKotlin` | 编译 Kotlin 源码(快,无 lint) |
| `./gradlew.bat testDebugUnitTest` | 跑单元测试 |
| `./gradlew.bat assembleDebug` | 出 debug APK(输出在 `app/build/outputs/apk/debug/`) |
| `./gradlew.bat installDebug` | 编译 + 安装 + 启动 |
| `./gradlew.bat lint` | Android Lint(慢,可选) |

**镜像**:目前 Gradle 跑 `services.gradle.org`,首次构建 5-10 分钟下载依赖,之后增量秒级。

---

## 5. 代码规范

### 5.1 文件命名(Android)

| 类型 | 格式 | 例 |
|---|---|---|
| Composable 屏幕 | `<拼音首字母大写>Screen.kt` | `Learning2Screen.kt`、`Gunlun14Screen.kt` |
| 屏幕包 | `com.jueqiao.jianghu.ui.screens.<拼音>` | `package com.jueqiao.jianghu.ui.screens.learning2` |
| 共享 Composable | `<描述><类型>.kt` | `StandardGunlunScaffold.kt` |
| 共享形状 | `<描述>Shape.kt` | `WideHexagonShape.kt` |
| ViewModel | `<功能><ViewModel>.kt` | `LuggageViewModel.kt` |
| Repository | `<功能><Repository>.kt` | `LuggageRepository.kt` |
| API | `<功能><Api>.kt` | `LuggageApi.kt` |
| 测试 | `<被测类>Test.kt` | `RoutesTest.kt` |

### 5.2 Drawable 资源命名

```
img_<页面拼音>_<Figma 节点名>.png
```

- 例:`img_shengtu_group196.png`、`img_learning_image_174.png`、`img_houshan_bg.png`
- **没写在任何文件里** — 是从 Figma 导出习惯沿用的,新增资源请照这个格式
- 详见 `CONTRIBUTING.md §1`

### 5.3 资源存放

| 目录 | 用途 |
|---|---|
| `res/drawable-nodpi/` | **新建资源放这里**(默认) |
| `res/drawable/` | mdpi 通用元素(背景、装饰) |
| `res/drawable-xxhdpi/` | @2x 高密度资源 |

⚠ **V2 审计发现**:`res/drawable/` 里有 4 张大背景 PNG(`img_gunlun1_bg.png` 等),xxhdpi 上每张解码 ~54 MB,**`git mv` 到 `drawable-nodpi/` 零代码改动,能省大量内存**。

### 5.4 屏幕适配(必读)

所有 UI 按 Figma 412×917 设计稿落坐标,**真机千差万别**(Pixel 4a 393×851 / Galaxy S22 360×780 / 折叠屏 320×640 …)。常见 3 类坑:
1. **底部元素被系统导航条压住** — Y 越大的元素越惨
2. **底部独立导航条没填满宽屏** — 硬编码 `size(412dp)` 在折叠屏只占中间
3. **中央元素在矮屏偏离视觉重心** — 绝对 Y 在矮屏看起来偏下

**项目用方案:两段式 / 三段式 Box 结构 + BoxScope 相对定位**。详情、转换公式、5 种真机 dp 表、22 屏迁移清单看 [android/docs/screen-adaptation.md](../android/docs/screen-adaptation.md)。

新建页时:
- ✅ 默认采用"两段式":外层 `Box(fillMaxSize)` 放背景,内层 `Box(fillMaxSize, windowInsetsPadding(navigationBars))` 放内容
- ✅ 元素位置用 `align(Alignment.X) + offset(x, y)` 表达,不直接算绝对坐标
- ✅ 改元素 offset/size 时,**同步 inline 注释里的 `// 元素(X=?, Y=?, W=?, H=?)`**

### 5.5 Compose 模式

**屏幕骨架**:
```kotlin
@Composable
fun XxxScreen(
    onBack: () -> Unit = {},          // 返回,默认空实现
    onOpenYyy: () -> Unit = {},        // 跳转回调,默认空实现
) {
    BackHandler(enabled = true) { onBack() }
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Image(                          // 全屏背景
            painter = painterResource(R.drawable.img_xxx_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            // 内容层
        }
    }
}
```

**约定**:
- ✅ 每个屏幕参数都带 `= {}` 默认值(允许暂不接回调)
- ✅ 元素坐标 inline 注释里写 `// 元素名(X=?, Y=?, W=?, H=?)`,改 Modifier 后**必须同步**
- ✅ 图像元素 `contentDescription` 给 TalkBack 有意义文本(`null` 仅装饰图)
- ✅ 可点击元素 `.clickable(onClick = ...)` 直接附在 Modifier 链上
- ❌ 不要抽公共 Composable(用户明确偏好直接复制)
- ❌ 不要给"教练辅助"组加 clickable

### 5.5 NavHost 路由

**Routes 命名**:
```kotlin
const val Splash    = "splash"
const val Gunlun1  = "gunlun1"
const val LuggageCreations = "luggage/creations"
const val LuggageManualsPattern = "luggage/manuals/{state}"  // 参数化
fun luggageManuals(state: String?): String = "luggage/manuals/${state ?: "ALL"}"  // 调用
```

**NavHost 注册**(`JianghuNavHost.kt`):
```kotlin
import com.jueqiao.jianghu.ui.screens.xxx.XxxScreen

composable(Routes.Xxx) {
    XxxScreen(
        onBack = { navController.popBackStack() },
        onOpenYyy = { navController.navigate(Routes.Yyy) },
    )
}
```

⚠ **V2 审计发现**:
- 路由仍是字符串 + `orEmpty()` 提取参数,容易拼写错误时编译通过运行时崩溃
- 改进方向:Navigation Compose 2.8+ 的 type-safe routes(用 kotlinx-serialization)

### 5.6 RoutesTest 模式

每个新路由加一个 assertion 到 `learningDestinationsIncludeWheelTrialAndBackMountainFlow` 测试函数:
```kotlin
fun learningDestinationsIncludeWheelTrialAndBackMountainFlow() {
    assertEquals("xiulian", Routes.Xiulian)
    // ...
    assertEquals("learning2", Routes.Learning2)
    assertEquals("learning3", Routes.Learning3)
    assertEquals("learning4", Routes.Learning4)  // 新加
}
```

⚠ **V2 审计发现**:RoutesTest 只断言字符串常量,**没有行为测试**(没有 click → navigation 测试)。这就是为什么之前 PendingUnlock"前往解锁"按钮做成 dead affordance 没人发现 — 加 `testTag` + `createComposeRule()` 测 click。

---

## 6. DI(依赖注入)

⚠ **当前是手写 DI**,**不要**改用 Hilt/Koin 除非整个项目决定迁移。

```kotlin
// JianghuApp.kt — Application 单例持有所有仓库
class JianghuApp : Application() {
    lateinit var authRepository: AuthRepository
    lateinit var settingsRepository: SettingsRepository
    lateinit var luggageRepository: LuggageRepository
    
    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(api = AuthApi(BuildConfig.AUTH_BASE_URL), tokenStore = EncryptedTokenStore(this))
        settingsRepository = SettingsRepository(...)
        luggageRepository = LuggageRepository(api = LuggageApi(...), ...)
    }
}

// JianghuNavHost.kt — ViewModelFactory 在 nav 内手动构造
val authFactory = remember(application) {
    AuthViewModelFactory(application.authRepository)
}
val authViewModel: AuthViewModel = viewModel(factory = authFactory)
```

**ViewModel 创建模板**(`ViewModelFactory`):
```kotlin
class XxxViewModelFactory(private val repo: XxxRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return XxxViewModel(repo) as T
    }
}
```

⚠ **V2 审计发现**:
- 5 个 ViewModelFactory 复制粘贴(LuggageViewModel.kt:62 那种样板)
- 5 个 ViewModel 都重复 try/CancellationException/Exception/UserMessage 样板 → 抽 `runOp(operation, fallback, block)`
- 这些抽出来后,可减少 ~150 行重复

---

## 7. 安全(必读)

### 7.1 Token 存储

- Refresh token 用 `EncryptedSharedPreferences` + AndroidKeyStore
- 私钥在 `getOrCreateKey` 里懒加载
- KeyStore 失败时通过 `KeyPermanentlyInvalidatedException` 触发清理 — **必须 catch 这个异常**(V2 审计 HIGH)

### 7.2 网络

- HTTPS 优先,cleartext 仅 10.0.2.2 / 127.0.0.1 / localhost 白名单(`network_security_config.xml`)
- Cert pinning:**未实现**(V2 审计建议加)
- 不要在 Log.* 输出 token / 用户密码 / 手机号

### 7.3 后端密钥

⚠ **V2 审计发现**:`services/api/app/core/config.py` 里有 dev 密钥作为默认值:
- `DEV_JWT_SECRET = "dev-only-jwt-secret-change-before-shared-deployment-1234567890"`
- `internal_worker_token` 默认 `"dev-internal-worker-token-change-me-123456"`

**生产部署前必须删默认值 + 轮换。**

---

## 8. Git 工作流

### 8.1 当前流程(2026-09-10 起简化)

**直接在 main 上工作**(zzz 分支已于 2026-09-10 删除):
```bash
git checkout main
# 改代码、add、commit、push
git add <files>
git commit -m "<conventional-commit-style message>"
git push origin main
```

### 8.2 Commit message 格式

用 [Conventional Commits](https://www.conventionalcommits.org/):
```
feat(<scope>): <description>
fix(<scope>): <description>
refactor(<scope>): <description>
docs(<scope>): <description>
chore(<scope>): <description>
```

例:
- `feat(learning4): add Learning4 page with content image`
- `fix(pendingunlock): make 前往解锁 button clickable (v2 audit HIGH #11)`
- `refactor(gunlun5): clean up magic numbers`
- `docs: add CODE-AUDIT-2026-09-10`

### 8.3 何时 commit

**频繁**。每个功能点结束就 commit + push。原因参见 `docs/SESSION-LOG-2026-09-09.md §"备份策略:频繁 commit + push"`:
> 9月8日文件丢失是因为有修改从未 commit

---

## 9. 文档规范

### 9.1 写什么 / 何时写

| 类型 | 文件名 | 何时建 |
|---|---|---|
| 会话记录 | `docs/SESSION-LOG-YYYY-MM-DD.md` | **每次长跑调试结束(≥30 分钟)** |
| 决策记录 | `docs/DECISIONS.md` | 做了重要的架构 / 设计决策 |
| 故障排查 | `docs/TROUBLESHOOTING.md` | 解决了一个反复出现的问题 |
| **代码审计** | **`docs/CODE-AUDIT-YYYY-MM-DD.md`** | **多维度扫描结果(每次扫描另起一份)** |
| Sprint 计划 | `docs/SPRINT-YYYY-MM-DD.md` | 多步骤功能开发 |
| 跨天摘要 | `docs/SUMMARY-YYYY-MM-DD-to-YYYY-MM-DD.md` | 跨天高层 TL;DR |

**每类文档第一行写明标题 + 一句话说明 + 创建日期/原因。**

### 9.2 索引更新

新建/删除 docs文件后,**更新 [docs/README.md](docs/README.md) 的"当前文件清单"表**。

### 9.3 git 政策

- **应该 commit**:`SESSION-LOG-*.md`、`CODE-AUDIT-*.md`、`DECISIONS.md`、`TROUBLESHOOTING.md`、`SUMMARY-*.md`
- **不应该 commit**:个人笔记、未结论的临时排查

---

## 10. 测试

### 10.1 现状(2026-09-10)

| 维度 | 状态 |
| |---|
| 单元测试文件数 | 3(`ConferenceModelsTest.kt`、`RoutesTest.kt`、`ResponsiveDesignCanvasTest.kt`) |
| 测试覆盖 | ~0% |

⚠ **V2 审计重大发现**:**新加的整条学习/滚轮导航链路 0 行为测试**。一个 click → navigation 测试就能逮到 PendingUnlock dead button 这种 bug。

### 10.2 必须测的事(优先级)

1. **行为测试** — 每个 `clickable` 元素至少 1 个 click → navigation 测试
2. **ViewModel** — 状态机 + error path(用 `runTest` + `TestScope`)
3. **校验** — `Validators.isPassword` / `isPhone` 等边界用例
4. **API** — AuthRepository 的 logout cleanup(LuggageApi.kt:121 SSRF 那种)

### 10.3 工具

- JUnit 4 + Truth(已在 `android/app/build.gradle.kts`)
- Compose 测试:`createComposeRule()`,`onNodeWithText().assertIsDisplayed().performClick()`
- Coroutines 测试:`runTest { ... }`,`TestDispatcher`

---

## 11. 常见陷阱(V2 审计高频)

### 11.1 错误操作

| ❌ 错误 | ✅ 正确 |
|---|---|
| 用系统 JDK 25 跑 gradle | `export JAVA_HOME=C:/Users/28784/.jdks/jbr-21.0.11` |
| `adb install` 不加 `-r` | `adb install -r app-debug.apk` |
| `git push` 前没看 `git status` | 永远先 `git status` 看 staged + working tree |
| 改 Modifier 链里的 offset/size 但不更新 inline 注释 | 一起改(`// 元素名(X=?, Y=?, W=?, H=?)` 必须同步) |
| 路由写成 `Routes.Learing2`(拼错)| 编译器抓不到,N点击用户被空 `orEmpty()` 漏掉 |
| 在屏幕内嵌业务逻辑 | ViewModel 拿 state,Composable 只 `collectAsStateWithLifecycle` |
| 抽公共 Composable("教练辅助组")| 用户明确偏好直接复制 |
| 在 Log.* 输出 token | 用 BuildConfig.DEBUG 包起来或干脆不打 |
| 抽 helper 时一次性 refactor 10 个屏 | 一次 1 屏,跑测试 + commit,再下一个 |
| `popUpTo` 随便加 | 默认 navigate 保留来源页;除非特殊需求 |

### 11.2 真实存在的高危问题(2026-09-10 仍未修)

来自 [CODE-AUDIT-2026-09-10.md](docs/CODE-AUDIT-2026-09-10.md),**接手的第 1 周应该先修这些**:

1. **CRITICAL · [LuggageApi.kt:309](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L309)** — SSRF(直接传到任意 host URL)。删 `isDirectUpload` 分支,统一走后端 `/v1/uploads/{id}/object`。
2. **CRITICAL · 4 张大背景 PNG** — `git mv res/drawable/img_gunlun1_bg.png`(及 houshan_bg、unfinished_image134、unfinished_compact124)→ `drawable-nodpi/`。零代码改动,xxhdpi 上每张省 ~50 MB 解码内存。
3. **HIGH · [AuthRepository.kt:121](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L121)** — `readRefreshToken()` 移入 try 块。
4. **HIGH · [build.gradle.kts:14, :50](android/app/build.gradle.kts#L14)** — release/acceptance 强制 https,删除 debug signing 复用。
5. **HIGH · [config.py:89](services/api/app/core/config.py#L89)** — 删 dev 密钥默认 + 轮换 `JIANGHU_INTERNAL_WORKER_TOKEN`。
6. **HIGH · 抽 `BookShelfScaffold` + `LearningPageChrome` + `BookFrameScaffold`** — Gunlun13/14/15、Learning2/3、PendingUnlock/Unfinished 全部塌缩到 1 行声明。
7. **MEDIUM · [LuggageRepository.kt:81](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L81)** — `manualDetail` 3 调用并行。
8. **MEDIUM · 抽 `runOp` 帮助函数** — 消除 5 个 ViewModel 的 try/CancellationException 样板。

---

## 12. Quick Reference 速查表

### 12.1 关键文件位置

| 用途 | 文件 |
| |---|
| 路由表 | [android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt) |
| 导航图 | [android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt) |
| Application 单例(DI) | [android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt](android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt) |
| 共享 Composable | [android/app/src/main/java/com/jueqiao/jianghu/ui/components/](android/app/src/main/java/com/jueqiao/jianghu/ui/components/) |
| 主题(颜色 + YaHei) | [android/app/src/main/java/com/jueqiao/jianghu/ui/theme/](android/app/src/main/java/com/jueqiao/jianghu/ui/theme/) |
| 后端 config | [services/api/app/core/config.py](services/api/app/core/config.py) |
| 后端路由 | [services/api/app/api/routes/](services/api/app/api/routes/) |
| 后端业务域 | [services/api/app/domains/](services/api/app/domains/) |
| 安全配置 | [android/app/src/main/res/xml/network_security_config.xml](android/app/src/main/res/xml/network_security_config.xml) |
| 备份规则 | [android/app/src/main/res/xml/backup_rules.xml](android/app/src/main/res/xml/backup_rules.xml) |
| **屏幕适配方案** | **[android/docs/screen-adaptation.md](../android/docs/screen-adaptation.md)** | **必读 — 两段式/三段式 + 真机 dp 表** |
| AndroidManifest | [android/app/src/main/AndroidManifest.xml](android/app/src/main/AndroidManifest.xml) |
| 模块构建脚本 | [android/app/build.gradle.kts](android/app/build.gradle.kts) |
| 后端启动 | [infra/start-dev.ps1](infra/start-dev.ps1) |

### 12.2 关键路径

| 用途 | 路径 |
| |---|
| 设计稿源 | `D:\图\`(本地,**不在 git**) |
| APK 输出 | `android/app/build/outputs/apk/debug/app-debug.apk` |
| Gradle 用户缓存 | `C:\Users\28784\.gradle\caches` |

### 12.3 测试账号

```
13800138000 / Test1234!
```

### 12.4 急救命令

| 现象 | 命令 |
| |---|
| 手机没反应(USB 刚插) | `adb -s 21908b7a reverse tcp:8010 tcp:8010` |
| 后端容器挂了 | `cd D:\Appproject\infra; .\start-dev.ps1` |
| Gradle 报 25.0.3 | `export JAVA_HOME=C:/Users/28784/.jdks/jbr-21.0.11` |
| Android Studio 索引错乱 | File → Invalidate Caches → Restart |
| NTFS 文件系统诡异 | 重启电脑(90% 解决);最差 `chkdsk D: /f /r /x` |
| Conflicting overloads | 删 `android/.gradle`、`android/app/build`、`C:\Users\28784\.gradle\caches`,重启 AS |

---

## 13. 必读文档清单(按优先级)

新人进来前 30 天**必读**:

1. **本文件**(你正在读)— 上手指南
2. [CONTRIBUTING.md](../CONTRIBUTING.md) — 协作约定(命名、复制、NavHost)
3. [docs/README.md](../docs/README.md) — docs/ 索引
4. [docs/SESSION-LOG-2026-09-09.md](../docs/SESSION-LOG-2026-09-09.md) + [2026-09-10.md](../docs/SESSION-LOG-2026-09-10.md) — 知道"为什么会变成现在这样"
5. [docs/CODE-AUDIT-2026-09-10.md](../docs/CODE-AUDIT-2026-09-10.md) — 知道现在**还有哪些坑要修**
6. [DEV-SETUP.md](../DEV-SETUP.md) — 环境配置细节
7. [ENVIRONMENT.md](../ENVIRONMENT.md) — ENV 文件说明

如果只能读 1 篇:读本文件。如果还能读 2 篇:加上 `CODE-AUDIT-2026-09-10.md`(知道坑在哪)。