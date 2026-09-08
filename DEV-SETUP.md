# DEV-SETUP.md

> 本文档记录"江湖(Jianghu)"Android 项目在本地跑通真机调试的完整流程,以及踩过的坑。
>
> 适用读者:本项目开发者 / AI agent(本人 6 个月后看回来也能用)。
>
> **本文档因实战而产生**(2026 年 9 月那次调试会话)。

---

## 目录

1. [前置依赖](#前置依赖)
2. [一次性配置](#一次性配置)
3. [每日开发流程](#每日开发流程)
4. [常见坑 + 修复](#常见坑--修复)
5. [测试 API 是否通](#测试-api-是否通)
6. [创建第一个测试账号](#创建第一个测试账号)
7. [架构速览](#架构速览)

---

## 前置依赖

| 工具 | 版本要求 | 用途 |
|---|---|---|
| **JDK / JBR** | **21**(必须 21,不能用 25) | Gradle 运行时 + AS 项目 SDK |
| **Docker Desktop** | 最新版 | 后端 + 数据库 + 缓存 + 对象存储 |
| **Android Studio** | Koala 2024.1.1+ | IDE |
| **Android SDK** | platform-tools + build-tools | adb 工具 + 编译 |
| **真机** | 任意(测试用 USB 连电脑) | 跑 app |

---

## 一次性配置

### 1. JDK 21

```powershell
# 验证 JDK 版本
java -version
# 必须 21.x(不要 25!AGP 8.7.3 不支持)
```

**踩坑**:本机 `JAVA_HOME=C:\Program Files\Java\jdk-25.0.3`,AGP 8.7.3 不支持 → AS 报 "What went wrong: 25.0.3"。

**修复**:AS 里 `Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK` 选 **Embedded JBR**(项目 `gradle.properties` 配的就是这个)。**不要**改 `JAVA_HOME` 系统变量。

### 2. adb 加到 PATH

```powershell
# 永久加用户级 PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$currentPath;D:\Android\Sdk\platform-tools", "User")
```
**关掉 PowerShell 重开生效**。

---

## 每日开发流程

### Step 1: 启动后端(新 PowerShell 窗口,跑后保持打开)

```powershell
cd D:\App\Appproject\infra
.\start-dev.ps1
```

**等 30-60 秒**让 docker 拉镜像 + 跑 alembic 迁移。**首次启动 1-3 分钟**(拉镜像)。

`start-dev.ps1` 实际跑的是:
```powershell
docker compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

> 国外/Docker Hub 通的用户:把 `-f docker-compose.override.yml` 删掉就行。

**验证后端**:
```powershell
& "C:\Windows\System32\curl.exe" -s -o /dev/null -w "HTTP %{http_code}`n" http://127.0.0.1:8010/docs
# 期望:HTTP 200
```

### Step 2: 手机 USB 连电脑 + 转发端口(新 PowerShell 窗口,跑后保持打开)

1. 手机开"开发者选项" + "USB 调试",插电脑
2. 第一次会弹"允许 USB 调试",点同意
3. 在电脑 PowerShell 跑:
   ```powershell
   adb devices
   # 期望:List of devices attached + 一行 xxxxxxxx    device
   ```
4. 转发端口:
   ```powershell
   adb reverse tcp:8010 tcp:8010
   # 期望:8010(端口号,无错误)
   ```

**原理**:手机 `127.0.0.1:8010` 通过 USB 数据线 → 电脑 `127.0.0.1:8010`(android app 用这个 URL)。

### Step 3: AS 配置 + build

**一次性配置** `android/local.properties`(gitignore,不入库):
```properties
sdk.dir=D\:\\Android\\Sdk
AUTH_BASE_URL=http://127.0.0.1:8010/
```

**说明**:
- `sdk.dir`:你的 Android SDK 位置
- `AUTH_BASE_URL`:真机走 adb reverse,所以用 `127.0.0.1`(localhost)
- 局域网真机调试(非 USB)改成电脑 LAN IP,例如 `http://<你的电脑IP>:8010/`

### Step 4: AS Build → Make Project

`Ctrl+F9` 重新生成 `BuildConfig.AUTH_BASE_URL`。

### Step 5: 手机登录

1. 打开 app
2. 输入手机号 `13800138000`,密码 `Test1234!`(或者你自己注册的)
3. 登录 → 应该进首页

---

## 常见坑 + 修复

### ❌ 坑 1:Gradle 报 "What went wrong: 25.0.3"
- **原因**:系统 `JAVA_HOME=jdk-25.0.3`,AGP 8.7.3 不支持
- **修法**:AS 里 Gradle JDK 选 Embedded JBR,**不要改 JAVA_HOME**

### ❌ 坑 2:app 报"暂时无法连接江湖驿站"
- **原因 A**:后端没起(最常见)— 跑 `curl http://127.0.0.1:8010/docs`
- **原因 B**:`local.properties` 用了模拟器 IP `10.0.2.2`,真机访问不到 — 改成电脑 LAN IP 或 `127.0.0.1`(USB reverse 场景)
- **原因 C**:`adb reverse` 没设(USB 场景)— 跑 `adb reverse tcp:8010 tcp:8010`
- **原因 D**:Android 9+ 默认禁明文 HTTP — 见坑 4

### ❌ 坑 3:Swagger UI 能开,但 app 调 API 报"Invalid host header"
- **原因**:后端 `TrustedHostMiddleware` 白名单不含你的访问来源
- **白名单的 env var**:`JIANGHU_ALLOWED_HOSTS`(注意 `JIANGHU_` 前缀 + JSON 数组格式)
- **`.env` 文件**:
  ```
  JIANGHU_ALLOWED_HOSTS=["<你的电脑IP>","127.0.0.1","localhost","10.0.2.2"]
  ```
- ⚠️ **JSON 数组格式**必须是 `["a","b","c"]`,不能是 `a,b,c`(后者会被 pydantic-settings 当 JSON 解析失败)

### ❌ 坑 4:Android 9+ 阻止明文 HTTP
- **修法**:精确白名单(开发用)— 创建 `android/app/src/main/res/xml/network_security_config.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <network-security-config>
      <domain-config cleartextTrafficPermitted="true">
          <domain includeSubdomains="false">127.0.0.1</domain>
          <domain includeSubdomains="false"><你的电脑IP></domain>
      </domain-config>
  </network-security-config>
  ```
  `AndroidManifest.xml` 加引用:
  ```xml
  <application
      ...
      android:networkSecurityConfig="@xml/network_security_config">
  ```
- ⚠️ **不要**简单设 `usesCleartextTraffic="true"`(全局放行,生产不安全)

### ❌ 坑 5:uvicorn 启动报 `JSONDecodeError: Extra data`
- **原因**:pydantic-settings 用 `json.loads` 解析 list 类型 env var,字符串格式 `a,b,c` 不是合法 JSON
- **修法**:用 JSON 数组格式 `["a","b","c"]`

### ❌ 坑 6:`docker compose up` 卡在 `auth.docker.io:443`
- **原因**:Docker Hub 在国内连不上
- **修法 1(临时)**:改 Dockerfile 的 `FROM docker.m.daocloud.io/library/python:3.13-slim`
- **修法 2(优雅,推荐)**:用 override 文件(本项目用此)
  - Dockerfile: `ARG BASE_REGISTRY=docker.io/library/` + `FROM ${BASE_REGISTRY}python:3.13-slim`
  - `infra/docker-compose.override.yml`: `args: { BASE_REGISTRY: docker.m.daocloud.io/library/ }`
  - 启动用 `-f docker-compose.yml -f docker-compose.override.yml`

### ❌ 坑 7:ADT / AS 重启后 uvicorn 没自动起来
- **原因**:docker compose 容器默认 `restart: unless-stopped`,**电脑重启后才会自启**
- **避免**:`docker compose down` 别带 `--remove-orphans` 之类,数据卷会保留

---

## 测试 API 是否通

```powershell
# Swagger UI(GET)
& "C:\Windows\System32\curl.exe" -s -o /dev/null -w "HTTP %{http_code}`n" http://127.0.0.1:8010/docs

# Health check
& "C:\Windows\System32\curl.exe" -s http://127.0.0.1:8010/healthz
```

---

## 创建第一个测试账号

### dev 验证码固定 `123456`(见 `services/api/app/core/config.py` L61)

### 步骤

1. **生成验证码**(并存入 store):
   ```powershell
   # verify_code.json 内容:
   # {"phone": "13800138000", "purpose": "REGISTER"}
   & "C:\Windows\System32\curl.exe" -X POST http://127.0.0.1:8010/v1/auth/verification-codes -H "Content-Type: application/json" -d "@verify_code.json"
   # 期望:HTTP 202,body {"accepted": true, ...}
   ```

2. **注册**:
   ```powershell
   # register.json 内容:
   # {"phone": "13800138000", "password": "Test1234!", "verification_code": "123456", "age_band": "ADULT", "terms_version": "2026-08", "privacy_version": "2026-08"}
   & "C:\Windows\System32\curl.exe" -X POST http://127.0.0.1:8010/v1/auth/register -H "Content-Type: application/json" -d "@register.json"
   # 期望:HTTP 201,body 含 user + tokens
   ```

3. **删临时文件**(含明文密码):
   ```powershell
   Remove-Item verify_code.json, register.json
   ```

4. 在 app 用 `13800138000 / Test1234!` 登录

---

## 架构速览

```
docker compose (infra/docker-compose.yml)
├── postgres     (55432)  主数据库,alembic 迁移自动跑
├── redis        (16379)  缓存 + 限流 + 验证码
├── minio        (19000)  对象存储(图片、文件)
├── clamav       (内)      病毒扫描
├── api          (8010)   FastAPI 主服务
└── worker       (内)      后台任务(媒体处理、扫描)

Android app (com.jueqiao.jianghu)
├── 本地资源:图片、Compose UI
├── 网络:OkHttp → AUTH_BASE_URL(默认 http://127.0.0.1:8010/)
└── 调试:USB 连电脑 → adb reverse tcp:8010 tcp:8010
```

### 后端 dev 模块
- `services/api/app/api/routes/auth.py` — 注册/登录
- `services/api/app/core/config.py` — `.env` 读 + `allowed_hosts` 等
- `services/api/alembic/` — 数据库迁移
- `services/api/app/domains/catalog/seed.py` — catalog 种子

### Android 模块
- `android/app/build.gradle.kts` — BuildConfig 生成
- `android/app/src/main/java/com/jueqiao/jianghu/auth/` — 鉴权
- `android/app/src/main/java/com/jueqiao/jianghu/network/` — OkHttp + Retrofit

---

## 文件清单(本项目 dev 相关)

| 路径 | 用途 | 入库 |
|---|---|---|
| `infra/docker-compose.yml` | 完整 dev 编排 | ✅ |
| `infra/docker-compose.override.yml` | CN 网络镜像覆盖 | ✅(本 PR 加) |
| `infra/start-dev.ps1` | 一行启动 | ✅(本 PR 加) |
| `infra/stop-dev.ps1` | 一行停 | ✅(本 PR 加) |
| `services/api/Dockerfile` | 标准 FROM + ARG 模板 | ✅ |
| `services/api/.env` | 个人配置(白名单 IP)| ❌(gitignore) |
| `services/api/.env.example` | 模板文件 | 建议加 |
| `android/local.properties` | AS 个人配置 | ❌(gitignore) |
| `android/app/src/main/res/xml/network_security_config.xml` | cleartext 白名单 | ✅(本 PR 加) |
| `android/app/src/main/AndroidManifest.xml` | `networkSecurityConfig` 引用 | 修改 `usesCleartextTraffic` |

---

## 排查清单(checklist)

遇到"无法连接"按这个顺序查:
1. ✅ Docker 跑着? `docker ps`
2. ✅ API 在 8010? `curl http://127.0.0.1:8010/docs`
3. ✅ adb reverse 设了? `adb reverse --list`
4. ✅ 手机连同 USB? `adb devices`
5. ✅ local.properties URL 对? `cat android/local.properties`
6. ✅ BuildConfig 用新 URL? `android/app/build/generated/source/buildConfig/.../BuildConfig.java`
7. ✅ cleartext 白名单包含你的来源 IP / localhost?
8. ✅ .env JSON 数组格式正确?
9. ✅ AS Make Project 后 buildConfig 才更新
10. ✅ Logcat 看具体错误

---

## 备注

- 本文因 2026-09 那次调试会话而产生
- 如果某个步骤不再适用(比如 AS 版本变化),更新本文
- 新人 onboarding:先看这份文档再上手
