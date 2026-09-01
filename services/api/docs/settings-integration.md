# 设置页接口接入说明

设置页采用“本机立即生效、登录后同步云端”的方式。Android 端已经接入当前页面所需接口；本文同时记录后续前端新增页面时可直接使用的服务端能力。

## 通用约定

- API 基地址：本地 Android 模拟器使用 `http://10.0.2.2:8010`，电脑本机使用 `http://127.0.0.1:8010`。
- 除登录、注册等公开接口外，下列请求都要携带 `Authorization: Bearer <access_token>`。
- `PATCH` 只提交发生变化的字段；空对象会返回 `422`。
- 访问令牌过期时沿用现有认证模块刷新令牌，再重试一次原请求。
- 设置开关应先写本机缓存并立即刷新界面；网络失败时保留“待同步”标记，不回滚用户刚才的操作。

## 当前设置页已接入

### 偏好设置

`GET /v1/settings/preferences` 获取全部设置，`PATCH /v1/settings/preferences` 更新一项或多项设置。

```json
{
  "message_enabled": true,
  "learning_reminder": true,
  "work_updates": true,
  "service_messages": true,
  "quiet_hours": false,
  "auto_save": true,
  "wifi_only": true,
  "haptic_feedback": true,
  "large_text": false,
  "sound_enabled": true,
  "music_volume": 0.6,
  "effect_volume": 0.7
}
```

Android 端当前行为：

- 消息总开关、学习提醒、夜间免打扰会重新安排或取消每天 20:00 的本地提醒。
- 大字号即时应用到全应用。
- 自动保存用于生图、图片和要素输入页的本机草稿。
- 触感、声音总开关及音效音量用于设置控件的反馈预览。
- 仅 Wi-Fi 与背景音乐音量已保存并同步；素材下载器和背景音乐引擎接入后读取对应字段即可。

响应还包含预留字段 `high_contrast`、`read_aloud`、`subtitles_enabled`、`personalization_enabled`、`rest_reminder`，供后续增加无障碍、个性化和健康提醒页面时使用。

### 账号资料与登录设备

- `GET /v1/auth/me`：刷新昵称、脱敏手机号、年龄身份和账号状态。
- `GET /v1/account/sessions`：列出有效登录设备。
- `DELETE /v1/account/sessions/{session_id}`：撤销指定设备会话，成功返回 `204`。
- `POST /v1/auth/logout`：退出当前设备并撤销当前刷新令牌。
- `POST /v1/auth/logout-all`：撤销账号全部会话。

当前账号管理页已经展示真实资料和有效设备数量；后续如果设计“设备管理”子页，可直接使用会话列表和单设备撤销接口。

### 黑名单

- `GET /v1/settings/blacklist`：获取黑名单。
- `POST /v1/settings/blacklist`：请求体为 `{"blocked_user_id":"用户 UUID"}`。
- `DELETE /v1/settings/blacklist/{blocked_user_id}`：移出黑名单，成功返回 `204`。

当前页面已接入列表和移除操作；“加入黑名单”的入口应放在用户主页、互动评论或举报流程中。

### 帮助与反馈

`POST /v1/support/feedback`

```json
{
  "category": "GENERAL",
  "message": "反馈正文，去除首尾空格后需要 10 至 1000 个字符"
}
```

当前帮助中心已提供反馈输入弹窗、提交中状态和成功或失败提示。

### 数据恢复

当前“数据恢复”只恢复偏好设置默认值，并同步云端；不会删除账号、作品、进度或创作草稿。若后续要做云端作品恢复，应另建恢复任务接口，不要复用偏好设置接口。

## 已准备、待前端页面接入

### 家长控制与青少年模式

- `GET /v1/settings/guardian-controls`
- `PATCH /v1/settings/guardian-controls`

可更新字段：

```json
{
  "daily_limit_minutes": 60,
  "creation_allowed": true,
  "content_level": "TEEN",
  "minor_mode": true
}
```

`daily_limit_minutes` 范围为 15 至 240；`content_level` 可选值以 OpenAPI 文档为准。成年人请求此能力时服务端会拒绝。

### 数据导出与权利请求

- `GET /v1/account/export`：返回账号资料、设置、监护设置、同意记录和有效会话的结构化导出。
- `POST /v1/account/data-rights-requests`：创建数据权利请求。
- `GET /v1/account/data-rights-requests`：查询当前账号提交过的请求及处理状态。

创建请求示例：

```json
{
  "request_type": "ACCOUNT_DELETION",
  "reason": "不再使用该账号"
}
```

这组接口适合后续“隐私管理 / 下载我的数据 / 注销账号”页面。注销请求只是进入处理流程，不应在客户端直接删除本地以外的数据。

## 接入顺序建议

1. 先完成新增页面的视觉稿与页面路由。
2. 复用现有 `AuthRepository` 的鉴权、令牌刷新和统一错误解析。
3. 偏好类页面复用 `SettingsRepository`；账号会话、数据权利请求可在其上扩充，或按模块拆出仓库。
4. 显示服务端提交中、成功、失败状态；删除或撤销操作必须二次确认。
5. 接入后至少验证：离线修改、本机重启、重新登录后的云端同步、令牌过期重试、空列表和接口错误状态。

本地可在 API 启动后访问 `/docs` 查看实时 OpenAPI 页面。
