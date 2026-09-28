# 江湖 Android App — 调试排坑速查

> 本文档记录开发期反复踩过的连接 / 登录类问题及**可在终端验证**的诊断步骤。
> 适用读者：项目开发者 / AI agent。
> 与 `DEV-SETUP.md` 互补——那份讲"日常怎么跑起来"，这份讲"跑起来后报错怎么定位"。

---

## 目录

1. [症状对照表](#1-症状对照表)
2. [症状 A：App 显示「暂时无法连接江湖驿站」](#2-症状-aapp-显示暂时无法连接江湖驿站)
3. [症状 B：重开 App 又要登录（splash vs 真登录页）](#3-症状-b重开-app-又要登录splash-vs-真登录页)
4. [症状 C：token 反复丢（已知 bug，未修）](#4-症状-ctoken-反复丢已知-bug-未修)
5. [一键诊断脚本](#5-一键诊断脚本)

---

## 1. 症状对照表

| 你看到的画面 | 真实页面 | 原因 |
|---|---|---|
| 熊猫 + "点击进入" | `SplashScreen` (`Routes.Splash`) | 冷启动必经页，**必须手动点**才会触发 bootstrap |
| 手机号+密码输入框 + 蓝色登录按钮 | `LoginScreen` (`Routes.Login`) | `restoreSession()` 返回 false |
| 红色弹窗「暂时无法连接江湖驿站，请检查网络后重试」 | `AuthApiException` | API 调用 `IOException`（网络层） |

**混淆点**：Splash 和 Login 都是「打开 App 后第一眼看到的页面」，但它们**触发原因完全不同**。判断标准：

- 有没有手机号输入框？→ Login
- 只有一个熊猫 + "点击进入"？→ Splash，**点一下**才会真正决定去哪

---

## 2. 症状 A：App 显示「暂时无法连接江湖驿站」

### 触发点（错误信息源码）

错误文本固定在三个地方，都是捕获 `IOException` 后抛出 `AuthApiException`：

```
android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt:194, 238
android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt:1287
```

### 根因排序 + 验证

按 `DEV-SETUP.md` 坑 2 的四种原因**逐条验证**：

#### ① 后端没起（最常见）

```powershell
& "C:\Windows\System32\curl.exe" -s -o /dev/null -w "HTTP %{http_code}`n" http://127.0.0.1:8010/docs
# 期望:HTTP 200；若是连不上/超时/Connection refused → 后端死了
```

修法：`D:\Appproject\infra\start-dev.ps1`

#### ② adb reverse 没设（USB 真机场景，第二常见）

```powershell
adb reverse --list
# 期望:UsbFfs tcp:8010 tcp:8010
# 空 → 手机的 127.0.0.1:8010 找不到电脑的后端
```

修法：

```powershell
adb reverse tcp:8010 tcp:8010
```

> **USB 每次重插 / adb server 重启都会丢这条**，要重新跑。
> 想偷懒就建 `D:\Appproject\infra\start-reverse.ps1` 每次启动顺手跑一遍。

#### ③ local.properties 配错地址

```powershell
Get-Content D:\Appproject\android\local.properties | Select-String AUTH_BASE_URL
```

期望（USB 真机 + adb reverse 场景）：
```
AUTH_BASE_URL=http://127.0.0.1:8010/
```

- 用了 `10.0.2.2`（模拟器地址）但跑在真机 → 真机访问不到
- 想用 LAN 真机 → 改成电脑 IP，例如 `http://192.168.1.100:8010/`

改完必须 **Ctrl+F9 重新生成 BuildConfig**。

#### ④ Android 9+ 禁明文 HTTP

确认 `android/app/src/main/res/xml/network_security_config.xml` 的白名单包含当前 URL：

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">127.0.0.1</domain>
    <domain includeSubdomains="false">localhost</domain>
    <domain includeSubdomains="false">10.0.2.2</domain>
</domain-config>
```

如果用了电脑 LAN IP，**必须把这个 IP 加进去**，否则会被静默拦截、错误信息仍是"无法连接"。

#### ⑤ 后端白名单 (TrustedHostMiddleware)

如果 Swagger 能打开但 App 报"Invalid host header"：

```powershell
# 看后端 .env
Get-Content D:\Appproject\services\*\*.env   # 路径按项目实际位置
```

确认 `JIANGHU_ALLOWED_HOSTS` 是 **JSON 数组** 格式：

```
JIANGHU_ALLOWED_HOSTS=["<你的电脑IP>","127.0.0.1","localhost","10.0.2.2"]
```

⚠️ 字符串 `a,b,c` 格式会被 pydantic-settings 解析失败，**不是合法 JSON**。

### 一键判断在哪个环节

```powershell
# 从手机视角 curl 后端 —— 这一步过了说明 reverse + network_security + 后端白名单都没问题
adb -s <device> shell "curl -s -o /dev/null -w 'HTTP %{http_code}\n' http://127.0.0.1:8010/docs"
```

期望：`HTTP 200`。这条过了，App 仍报"无法连接"才考虑 OkHttp 拦截器 / 客户端 bug。

---

## 3. 症状 B：重开 App 又要登录（splash vs 真登录页）

### 关键认知

```
打开 App
   │
   ▼
[ Routes.Splash ]  ← 必经页，必须点 "点击进入"
   │
   │  onTap → authViewModel.bootstrap { authenticated ->
   │      if (authenticated) Routes.Home1 else Routes.Login
   │  }
   │
   ├── 成功 → 首页 (闯荡江湖)
   └── 失败 → 登录页
```

**"打开 App 又要登录"实际有 3 种可能**，先截图确认是哪一种：

| 你看到的 | 真实页面 | 真原因 |
|---|---|---|
| 熊猫 + "点击进入" | Splash | 冷启动必经页，**只是你还没点**——还没跑 bootstrap |
| 手机号+密码输入框 | Login | `restoreSession()` 返回 false（见症状 C） |
| 首页（闯荡江湖） | Home | 已经登录了，**你误判了** |

### 怎么快速判断

**截图**：

```powershell
adb -s <device> shell "screencap -p /sdcard/jianghu.png"
adb -s <device> pull /sdcard/jianghu.png D:\Appproject\
```

或者 **直接 tap 一下屏幕中央**（splash 全屏可点击）：

```powershell
adb -s <device> shell "input tap 540 1200"
```

- 5 秒后还是 splash → bootstrap 卡死，看 logcat
- 跳到首页 → 本来就登录着，是误判
- 跳到登录页 → 见症状 C

### 看 SharedPreferences 判断 token 状态

```powershell
adb -s <device> shell "run-as com.jueqiao.jianghu ls -la shared_prefs/"
adb -s <device> shell "run-as com.jueqiao.jianghu cat shared_prefs/auth_session.xml"
```

| 现象 | 含义 |
|---|---|
| 文件不存在 | 从未成功登录 / token 已被 `clear()` 清掉 |
| 文件存在，时间戳是几分钟前 | 当前 bootstrap 还没跑（停在 splash） |
| 文件存在，时间戳 = 现在 | bootstrap 跑完，**刚 `saveTokens()` 了新 token**，意味着 `restoreSession()` 成功 → 应该跳首页 |

> ⚠️ 文件 timestamp 变化 ≠ 一定是 `clear()`，可能是 `saveTokens()` 写新 token。
> 看**文件大小**区分：被 `clear()` 清空的 SharedPreferences ≈ 80~120 字节（空 XML），有内容 ≈ 280 字节。

---

## 4. 症状 C：token 反复丢（已知 bug，未修）

### 触发条件

代码：`android/app/src/main/java/com/jueqiao/jianghu/auth/EncryptedTokenStore.kt:43-49`

```kotlin
} catch (error: GeneralSecurityException) {
    clear()    // ← 静默清空 SharedPreferences
    null
}
```

密文存 SharedPreferences，密钥存 AndroidKeyStore。**AndroidKeyStore 里的 key 在某些情况下会消失**：

| 场景 | 概率 |
|---|---|
| OEM 厂商清理（小米/华为/OPPO/vivo 内存压力或"自启动管理"） | 高 |
| 用户手动"清除应用数据" | 中 |
| 安装/卸载过同包名的其他构建（debug/release 签名不同） | 中 |
| 系统更新 | 低 |
| `KeyPermanentlyInvalidatedException`（用户改锁屏 / 删指纹） | 低，本应用未启用 `setUserAuthenticationRequired` |

### 症状

- 登录成功 → token 存好
- 某次冷启动（间隔可能几小时/几天）→ keystore key 没了
- 启动 → `readRefreshToken()` 拿新 K2 解旧密文 → `AEADBadTagException` → catch → `clear()` → 跳登录页
- 用户毫无感知（无 toast / 无 log）

### 当前没有的兜底

- ❌ 没有 toast 告诉用户"系统凭据失效，请重新登录"
- ❌ 没有 `Log.e` 记录这次失败
- ❌ `clear()` 触发后没有 `setUnexpectedError` 等用户可见提示
- ❌ keystore key 失效 vs 用户主动登出，两条路径走同一个 catch，混淆

### 临时缓解（不修代码的情况）

只能**接受这个 bug**，养成习惯：

1. 重启 App 后看到登录页 → 重输密码登一次
2. 如果连接也断了 → 先看 [症状 A](#2-症状-aapp-显示暂时无法连接江湖驿站)

### 计划修复（写代码）

最小修复见 `EncryptedTokenStore.kt`：
- **A. 不要无条件 `clear()`**：catch 里只 log，不清。让 `restoreSession()` 走"无 session"分支，user 输密码后 `saveTokens()` 自然会盖掉。
- **B. 加 Log.e**：方便下次定位
- **C. 区分 `KeyPermanentlyInvalidatedException`**：明确告诉用户"系统安全凭据变化，需要重新登录"
- **D. 单测**：用 Robolectric + 模拟 keystore 失效场景验证

（暂未实施，等排期。）

---

## 5. 一键诊断脚本

保存为 `D:\Appproject\infra\diag.ps1`，出问题时跑一遍：

```powershell
# diag.ps1 —— 江湖 App 连接 / 登录问题一键诊断
param([string]$Device = "21908b7a")

Write-Host "=== [1/5] 后端健康 ==="
$code = & curl.exe -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8010/docs
Write-Host "  127.0.0.1:8010/docs → HTTP $code"

Write-Host ""
Write-Host "=== [2/5] adb reverse ==="
$reverse = & adb -s $Device reverse --list
if ($reverse -match "tcp:8010") { Write-Host "  ✓ reverse 已设" } else { Write-Host "  ✗ 没设！adb -s $Device reverse tcp:8010 tcp:8010" -ForegroundColor Red }

Write-Host ""
Write-Host "=== [3/5] 手机视角访问后端 ==="
$fromPhone = & adb -s $Device shell "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8010/docs"
Write-Host "  device → 127.0.0.1:8010/docs → HTTP $fromPhone"

Write-Host ""
Write-Host "=== [4/5] local.properties ==="
Get-Content D:\Appproject\android\local.properties | Select-String AUTH_BASE_URL

Write-Host ""
Write-Host "=== [5/5] token 文件状态 ==="
& adb -s $Device shell "run-as com.jueqiao.jianghu ls -la shared_prefs/auth_session.xml 2>/dev/null"
if ($LASTEXITCODE -eq 0) {
    & adb -s $Device shell "run-as com.jueqiao.jianghu cat shared_prefs/auth_session.xml 2>/dev/null"
} else {
    Write-Host "  ✗ 没有 auth_session.xml（从未登录 / 已被清掉）" -ForegroundColor Yellow
}
```

跑完看 5 个 section：

- ① 200 + ②有 + ③200 + ④`127.0.0.1:8010` + ⑤文件存在 → **一切正常，你看到的 splash/登录页是 splash 没点**
- ①非 200 → 后端死了，起后端
- ②空 → reverse 丢了，重设
- ①200 但 ③非 200 → network_security_config 白名单 / 客户端反向映射 / 后端 `TrustedHostMiddleware` 的问题
- ⑤文件不存在 → 真要去登录页（可能是症状 C 的 bug）
