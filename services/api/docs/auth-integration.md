# 认证模块前后端交接说明

本文档对应登录、注册、忘记密码三个页面。接口基址为 `http://10.0.2.2:8010`（Android 模拟器本地调试）或正式环境的 HTTPS API 域名，所有业务接口使用 `/v1` 版本前缀。

## 页面与接口映射

| 页面动作 | 方法与路径 | 说明 |
| --- | --- | --- |
| 登录 | `POST /v1/auth/login/password` | 手机号与密码登录 |
| 注册页获取验证码 | `POST /v1/auth/verification-codes` | `purpose` 固定为 `REGISTER` |
| 注册 | `POST /v1/auth/register` | 验证码、密码、年龄段、协议版本一并提交 |
| 忘记密码页获取验证码 | `POST /v1/auth/verification-codes` | `purpose` 固定为 `RESET_PASSWORD` |
| 确定修改密码 | `POST /v1/auth/password/reset` | 成功后所有旧登录状态失效 |
| 刷新登录状态 | `POST /v1/auth/token/refresh` | 每次刷新都会轮换 refresh token |
| 当前用户 | `GET /v1/auth/me` | 请求头携带 access token |
| 当前设备退出 | `POST /v1/auth/logout` | 提交当前 refresh token |
| 全部设备退出 | `POST /v1/auth/logout-all` | 请求头携带 access token |

## 三个页面需要提交的字段

### 登录页

```json
{
  "phone": "13800138000",
  "password": "用户输入的原始密码",
  "device_name": "Android"
}
```

登录按钮仅在手机号格式正确、密码为 8–64 个字符且用户勾选协议后可用。密码不得在客户端自动去除首尾空格。

### 注册页

先请求验证码：

```json
{
  "phone": "13800138000",
  "purpose": "REGISTER",
  "client_request_id": "客户端生成的唯一请求号"
}
```

然后注册：

```json
{
  "phone": "13800138000",
  "verification_code": "123456",
  "password": "用户输入的原始密码",
  "age_band": "AGE_14_TO_17",
  "terms_version": "2026-08",
  "privacy_version": "2026-08",
  "device_name": "Android"
}
```

`age_band` 只允许 `UNDER_14`、`AGE_14_TO_17`、`ADULT`。不得依据年级或设备信息偷偷推断年龄；注册流程必须让用户明确选择。不满 14 周岁时，客户端必须先进入监护人同意分支，获取 `guardian_consent_token` 后再提交注册。

监护人同意分支：

1. 使用监护人手机号请求验证码，`purpose` 为 `GUARDIAN_CONSENT`。
2. 调用 `POST /v1/auth/guardian-consents/verify`：

   ```json
   {
     "child_phone": "13800138000",
     "guardian_phone": "13900139000",
     "verification_code": "123456",
     "terms_version": "2026-08",
     "privacy_version": "2026-08"
   }
   ```

3. 将响应中的 `guardian_consent_token` 放入注册请求。

当前注册设计图尚无年龄选择与监护人分支，这是 Android 接入注册接口前必须补齐的产品项。

### 忘记密码页

获取验证码时 `purpose` 为 `RESET_PASSWORD`，修改密码请求为：

```json
{
  "phone": "13800138000",
  "verification_code": "123456",
  "new_password": "新的原始密码"
}
```

忘记密码是账号安全操作，不应再次要求勾选用户协议；设计稿中的协议勾选可在下一次 UI 修订时移除。

## 成功响应与令牌保存

注册和登录返回同一结构：

```json
{
  "user": {
    "id": "UUID",
    "nickname": "少侠0001",
    "phone_masked": "138****8000",
    "status": "ACTIVE",
    "age_band": "AGE_14_TO_17",
    "guardian_status": "NOT_REQUIRED"
  },
  "tokens": {
    "access_token": "JWT",
    "refresh_token": "opaque-token",
    "token_type": "bearer",
    "expires_in": 900,
    "refresh_expires_in": 2592000
  },
  "next_action": "ENTER_APP"
}
```

- access token 仅放在内存，用于 `Authorization: Bearer <token>`。
- refresh token 使用 Android Keystore 加密后落盘，不写日志、不进入云备份。
- 刷新成功后必须以新 refresh token 原子替换旧值；旧 token 再次出现时服务端会视为重放并撤销该用户的登录状态。

## 错误处理约定

业务错误统一为：

```json
{
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "手机号或密码不正确",
    "request_id": "请求追踪号"
  }
}
```

客户端展示 `message`，日志只记录 `code` 与 `request_id`，不得记录手机号、密码、验证码或令牌。重点错误码：

| 错误码 | 客户端行为 |
| --- | --- |
| `VALIDATION_ERROR` | 标记相关输入框，不发起自动重试 |
| `INVALID_CREDENTIALS` | 显示统一登录错误，不区分账号是否存在 |
| `VERIFICATION_CODE_INVALID_OR_EXPIRED` | 提示重新输入或获取验证码 |
| `RATE_LIMITED` | 按响应头 `Retry-After` 禁用按钮并倒计时 |
| `CONSENT_VERSION_OUTDATED` | 重新打开最新协议并要求确认 |
| `GUARDIAN_CONSENT_REQUIRED` | 进入监护人同意流程 |
| `INVALID_ACCESS_TOKEN` / `INVALID_REFRESH_TOKEN` | 清除本地令牌并返回登录页 |
| `REFRESH_TOKEN_REUSED` | 清除全部本地登录状态并提示重新登录 |

## 本地联调

开发环境固定验证码为 `123456`，接口不会返回或记录验证码。执行：

```powershell
docker compose -f infra/docker-compose.yml up -d postgres redis
cd services/api
.\.venv\Scripts\alembic upgrade head
.\.venv\Scripts\uvicorn app.main:app --reload --port 8010
```

正式环境必须使用 HTTPS、PostgreSQL、Redis、随机独立密钥和真实短信适配器；固定验证码与 `noop` 短信配置会在启动时被拒绝。
