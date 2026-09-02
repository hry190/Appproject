# 用户资料与秘籍目录接口（阶段 1）

基线日期：2026-09-01。所有个人接口均需 `Authorization: Bearer <access_token>`；能力接口无需登录。Android 当前无需修改，可在后续页面按钮就绪后按本文接入。

## 1. 启动前初始化

数据库迁移和目录种子必须依次执行。种子命令可重复运行，会按稳定 UUID 和版本号更新，不会重复插入：

```powershell
.\.venv\Scripts\alembic upgrade head
.\.venv\Scripts\python -m app.domains.catalog.seed
```

Docker Compose 的 API 启动命令已自动执行这两步。

## 2. 用户资料

- `GET /v1/profile`：返回昵称、年龄层、班级、匿名 ID、头像资产 ID、可见范围、当前称号、勋章和 `row_version`。
- `PATCH /v1/profile`：当前允许修改昵称、班级、已获得的当前称号和资料可见范围。
- `GET /v1/profile/titles`：已获得称号。
- `GET /v1/profile/badges`：已获得勋章。

首次读取资料时后端会生成稳定的 `JH-XXXXXXXX` 匿名 ID，并自动授予基础称号“机巧学徒”。年龄层不能通过普通资料接口修改；头像上传需等媒体服务完成后再接入。

更新资料必须回传最近一次读取到的 `row_version`。版本过期返回：

```json
{
  "error": {
    "code": "VERSION_CONFLICT",
    "message": "资料已在其他设备修改，请刷新后重试"
  }
}
```

班级只接受“五（三）班”这类标签，不保存学校名称。当前称号只能从用户已获得且仍启用的称号中选择。

## 3. 十卷五十页

`GET /v1/manuals` 支持：

- `volume=1..10`：按十卷筛选。
- `state=UNSEEN|DISCOVERED|LEARNED|MASTERED|TEACHING`：按状态筛选。
- `q=<关键词>`：搜索页名、核心逻辑、卷名和核心领域。
- `favorites_only=true`：只看本人收藏。
- `limit=1..50` 与 `cursor=<游标>`：游标分页。

详情接口 `GET /v1/manuals/{manual_page_id}` 额外返回生活钩子、互动证据设计、四个已获得阶段的达成条件和学习证据列表。

新用户的真实默认状态是 `UNSEEN/未闻`。客户端不能提交状态；后端现已根据有效页前预测、服务端试炼通过、未来的迁移证明审核和结构化讲解事件逐级推进。

收藏接口是幂等的，重复收藏或重复取消都安全：

- `PUT /v1/manuals/{manual_page_id}/favorite`
- `DELETE /v1/manuals/{manual_page_id}/favorite`

50 页正文元数据来自策划书第 5.3 节十卷表，当前 `content_status=OUTLINE`。正式漫画和试炼完成审核后再将对应内容版本切到 `READY`。

## 4. 行囊单请求骨架

`GET /v1/me/luggage` 已一次返回真实个人资料、50 页秘籍计数、本周/累计修炼、三类证据、最近秘籍、待巩固错题、作品空状态和隐私入口。未实现的作品阶段不会制造假数据：

- 新用户的修炼、通关和三类证据均为 0，提交试炼后读取真实投影。
- 新用户为 50 个 `UNSEEN`；取得秘籍后返回最近进度与证据摘要。
- 无错题返回 `NO_MISTAKES`；失败试炼后返回真实待巩固错题。
- 作品返回 `NO_CREATIONS`。

响应包含 `ETag`。客户端可在刷新时发送 `If-None-Match`，数据未变则返回 `304`。`snapshot_version` 组合资料、学习和错题投影版本。

## 5. 能力开关

`GET /v1/meta/capabilities` 用于前端决定是否展示尚未接线的入口。当前资料、目录、收藏、学习进度、错题、作品和媒体均为 `true`，行囊状态为 `LIVE_MEDIA_REVIEW`。不要依据页面上是否已有按钮决定后端是否保存事实；按钮上线后只需要接现有接口。

## 6. 主要错误码

- `AUTHENTICATION_REQUIRED` / `INVALID_ACCESS_TOKEN`：需要重新登录。
- `VERSION_CONFLICT`：资料乐观锁冲突。
- `TITLE_NOT_UNLOCKED`：试图选择未获得称号。
- `MANUAL_NOT_FOUND`：秘籍不存在或未开放。
- `INVALID_CURSOR`：分页游标损坏，回到第一页加载。
- `VALIDATION_ERROR`：字段、卷号、状态或班级格式不合法。
