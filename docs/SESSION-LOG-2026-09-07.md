# SESSION-LOG — 2026-09-07

> 当天开发会话记录。从"什么都没配置好"到"app 登录测试成功"的完整过程。

---

## 快速重启指南(关机后再开,做这 5 步)

```powershell
# 1. 启动后端(后台跑,保持开)
cd D:\App\Appproject\infra
.\start-dev.ps1

# 2. 设 adb reverse(后台跑,保持开;USB 每次插拔要重设)
adb -s 21908b7a reverse tcp:8010 tcp:8010
# 或用脚本:.\infra\adb-reverse.ps1(自动找小米设备)

# 3. AS 打开项目,Build → Make Project

# 4. AS Run,选小米 K50 Pro(传输 ID 21908b7a)

# 5. app 里用 13800138000 / Test1234! 登录(已注册的测试账号)
```

---

## 当天操作(按时间顺序)

### 1. 配置 JDK

- **问题**: AS Gradle 报错"找不到有效 Gradle JDK 配置"
- **原因**: 系统 `JAVA_HOME=jdk-25.0.3`,但 AGP 8.7.3 不支持 JDK 25
- **修法**: AS 弹窗里选 **"Use JDK jbr-21 (C:/Users/28784/.jdks/jbr-21.0.11)"**
- **注意**: `D:\Android\Studio\jbr` 是 AS 自带的 JBR,**但当前是 25**(跟着 AS 升级),不是项目要的 21

### 2. 配置 local.properties

```properties
# C:\Users\28784\AppData\Local\Android\Sdk
sdk.dir=D\:\\Android\\Sdk
AUTH_BASE_URL=http://127.0.0.1:8010/   # USB reverse 场景
```

> ⚠️ `local.properties` 在 `.gitignore` 里(**不提交**,个人配置)

### 3. 启动 uvicorn(直接跑,后来改成 docker)

```powershell
# 第一次是直接 uvicorn — 后来发现 python 走 Docker Hub 拉基础镜像卡住
$env:JIANGHU_ALLOWED_HOSTS = '["127.0.0.1","localhost","10.80.15.108","10.0.2.2"]'
cd D:\App\Appproject\services\api
uvicorn app.main:app --reload --port 8010 --host 0.0.0.0
```

后来发现 `.env` 文件更稳,改了 docker 方案。

### 4. uvicorn 启动时遇到的 3 个错误

- **JSONDecodeError: Extra data**: pydantic-settings 用 `json.loads` 解析 list 类型 env var,字符串 `"a,b,c"` 不是合法 JSON
  - **修法**: env var 用 JSON 数组格式 `["a","b","c"]`
- **配置无效 / API 不响应**: uvicorn 只绑 `127.0.0.1`,真机走 LAN IP 访问不到
  - **修法**: 加 `--host 0.0.0.0`
- **TrustedHost middleware 拒绝**: `allowed_hosts` 不含手机访问来源
  - **修法**: 加 `127.0.0.1` / `localhost` / 手机 LAN IP
  - **注意**: 项目 `env_prefix="JIANGHU_"`(不是默认的 `APP_`),所以 env var 名是 `JIANGHU_ALLOWED_HOSTS` 不是 `ALLOWED_HOSTS`

### 5. AS 改 cleartext 配置

- **问题**: app 显示"暂时无法连接江湖驿站"
- **原因**: Android 9+ 默认禁明文 HTTP(`usesCleartextTraffic="false"` 默认)
- **修法 1(临时)**: `AndroidManifest.xml` 改 `usesCleartextTraffic="true"`(全局放行,生产不安全)
- **修法 2(最终)**: 用 `network_security_config.xml` 精确白名单 `127.0.0.1` / `localhost` / `10.0.2.2`(模拟器)

### 6. adb 配置

- **adb 不在 PATH**: 加 `D:\Android\Sdk\platform-tools` 到用户 PATH
- **USB 真机调试**: 设置 → 开发者选项 → USB 调试(开),小米 K50 Pro 出现 `21908b7a device`
- **adb reverse**: `adb -s 21908b7a reverse tcp:8010 tcp:8010` 把手机的 `127.0.0.1:8010` 转到电脑
  - 每次 USB 插拔会丢,要重设 → 创建了 `infra/adb-reverse.ps1`(自动找小米设备 product=`cupid`)
- **GitHub 推不上去**: 网络问题 → 设 git proxy `http://127.0.0.1:65532`(用户的梯子本地端口)
  ```powershell
  git config --global http.proxy http://127.0.0.1:65532
  git config --global https.proxy http://127.0.0.1:65532
  ```

### 7. Docker Hub 卡住,切 DaoCloud

- **问题**: `docker compose build` 卡在 `auth.docker.io:443`(网络阻断了 Docker Hub)
- **用户环境**: 中国大陆,VPN 开了但 Docker daemon 不走 VPN 流量
- **修法**:
  - 改 `services/api/Dockerfile`: 用 `ARG BASE_REGISTRY=docker.io/library/` + `FROM ${BASE_REGISTRY}python:3.13-slim`(标准版)
  - 新增 `infra/docker-compose.override.yml`: 设 `args: { BASE_REGISTRY: docker.m.daocloud.io/library/ }`(CN 专用覆盖)
  - 启动方式:
    ```bash
    # 标准(Docker Hub 通的用户):
    docker compose -f docker-compose.yml up -d
    # CN(DaoCloud):
    docker compose -f docker-compose.yml -f docker-compose.override.yml up -d
    ```

### 8. 创建测试账号

```powershell
# 注册(分两步:先验证,再注册)
& "C:\Windows\System32\curl.exe" -X POST http://127.0.0.1:8010/v1/auth/verification-codes `
  -H "Content-Type: application/json" `
  -d "{\"phone\":\"13800138000\",\"purpose\":\"REGISTER\"}"
# ↑ fixed_verification_code="123456" 在 config.py L61(dev 模式固定)

& "C:\Windows\System32\curl.exe" -X POST http://127.0.0.1:8010/v1/auth/register `
  -H "Content-Type: application/json" `
  -d "@register.json"   # 文件传 body 避免 PowerShell `!` 转义问题
```

> 💡 **PowerShell 陷阱**: `"..."Test1234!..."` 里的 `!` 会被 PowerShell 当历史扩展处理
> - **修法**: JSON body 存到 `.json` 文件,curl 用 `-d "@file.json"`

### 9. APK 打包(用户没 SIM 卡不能 USB 安装)

- **问题**: 小米 "USB 安装" 默认禁,且需 Mi Account 验证,用户没 SIM 卡
- **用户选择**: 我用 Gradle 直接 build:
  ```powershell
  $env:JAVA_HOME = "C:\Users\28784\.jdks\jbr-21.0.11"
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
  cd android
  .\gradlew.bat assembleDebug
  ```
- **输出**: `android\app\build\outputs\apk\debug\app-debug.apk`(109 MB)
- **传输给手机**: 微信文件传输助手 / 蓝牙 / SMB / 文件管理手动安装
- **手动安装问题**: 小米病毒扫描拦截,设置 → 隐私保护 → 关病毒扫描

### 10. 代码重构(滚轮1-5 共用脚手架)

- **新建** `ui/components/StandardGunlunScaffold.kt`(107 行)
- **抽象**: 背景 + 熊猫打坐(可点击参数) + 返回按钮 + BackHandler
- **重构** 5 个 Screen,代码减少约 270 行(-25%)
- **修了一个 bug**: Gunlun5 重构后少导入了 `fillMaxSize`,编译报错,补上

---

## 当天创建的交付物(都在 git 里了)

| 文件 | 用途 |
|---|---|
| `infra/start-dev.ps1` | 一行启动 docker compose |
| `infra/stop-dev.ps1` | 一行停止 |
| `infra/adb-reverse.ps1` | 自动找小米设备并设端口转发 |
| `infra/docker-compose.override.yml` | CN 网络下用 DaoCloud 镜像 |
| `services/api/.env` | 个人配置(gitignore) |
| `services/api/.env.example` | 模板文件(提交到 git) |
| `android/app/src/main/res/xml/network_security_config.xml` | cleartext 精确白名单 |
| `android/app/src/main/AndroidManifest.xml` | 加 networkSecurityConfig 引用,关 cleartext 全局 |
| `DEV-SETUP.md` | 完整 dev 流程文档 |
| `docs/SESSION-LOG-2026-09-07.md` | 本文件 |

## 当天修改的源代码文件

| 文件 | 改动 |
|---|---|
| `services/api/Dockerfile` | 加 `ARG BASE_REGISTRY` 参数化 FROM |
| `android/app/src/main/AndroidManifest.xml` | cleartext→false + 加 networkSecurityConfig |
| `android/app/src/main/res/xml/network_security_config.xml` | 新建(见上) |
| 5 个 gunlun Screen | 用 StandardGunlunScaffold 重构 |

---

## 当天学到的"踩坑经验"

1. **JBR vs Embedded JDK**: `D:\Android\Studio\jbr` 是 AS 自带 JBR,但**跟着 AS 版本升级**,不一定符合项目要求。AS 装好用户级 JBR 21 是更可控的选择。
2. **Docker Hub 在国内会被阻断**,但 Docker daemon **不走系统 VPN 流量**。解决方案:DaoCloud 等国内镜像 + override 文件。
3. **adb reverse 每次 USB 插拔会丢** — 用脚本自动化最稳。
4. **`env_prefix="JIANGHU_"`** 在 `services/api/app/core/config.py` 里 — env var 不是 `ALLOWED_HOSTS`,是 `JIANGHU_ALLOWED_HOSTS`。
5. **pydantic-settings list env var 必须是 JSON 数组** `["a","b"]`,不是 `a,b`。
6. **Android 9+ cleartext 默认禁**,需要 `usesCleartextTraffic="true"`(临时)或 `network_security_config.xml`(白名单)。
7. **小米 K50 Pro 不允许无 SIM 卡开 "USB 安装"** — 变通是 wifi 调试(无线 ADB)或 Gradle build APK 手动传。
8. **PowerShell `"...!..."` 的 `!` 是历史扩展** — JSON body 用文件传。
9. **`ANDROID_HOME` / `JAVA_HOME` 设了不当的 JDK 会让 AS 报"找不到 Gradle JDK"** — AS 弹窗里手动选 JBR 21 是最干净的解法。
10. **git push 走代理** 是 `git config --global http.proxy`,不是 `git config --local`。

---

## 后续会话的文档约定(建议)

```
docs/
├── README.md                          ← 索引(推荐读这个)
├── SESSION-LOG-2026-09-07.md          ← 本文件
├── DECISIONS.md                        ← 架构决策(为什么要这样设计)
├── TROUBLESHOOTING.md                  ← 问题-解决方案对(可搜索)
├── SPRINTS.md                          ← 功能 sprint 计划(可选)
```

每次新会话(尤其是调试长跑),产生一个 `docs/SESSION-LOG-YYYY-MM-DD.md`,记录:
- 当天做的(commit hash 引用)
- 踩的坑(怎么修)
- 重要决策(为什么这么做)
- 链接到产生的文件 / PR / 文档

AI agent 在新会话开始时读 `docs/README.md` 即可快速了解项目状态。

---

## 相关链接

- [DEV-SETUP.md](../../DEV-SETUP.md) — 完整 dev 流程指南(8 个坑 + 排查清单)
- [docs/README.md](./README.md) — 文档索引
- GitHub: <https://github.com/hry190/Appproject>
- `ww` 分支历史:`git log --oneline ww -10`(待合并到 main 的实验分支)
