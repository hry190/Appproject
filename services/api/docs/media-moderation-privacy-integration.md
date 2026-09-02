# 媒体、审核、申诉与隐私接入说明

数据库迁移为 `0007_media_moderation_privacy`。前端能力开关返回 `media_uploads=true`，行囊状态为 `LIVE_MEDIA_REVIEW`。前端按钮尚未完成时后端可以先部署，所有资源默认私有。

## 1. 上传链路

前端不要把图片二进制 POST 到 FastAPI，也不能提交最终缩略图 URL。标准流程：

1. 计算原文件字节数与 SHA-256。
2. `POST /v1/uploads/intents`，提交用途、净化前文件名、声明 MIME、字节数和 SHA-256。
3. 按响应的 `method`、`upload_url` 和 `required_headers` 直接 PUT 到隔离对象存储。
4. `POST /v1/uploads/{upload_id}/complete`，带独立的 `Idempotency-Key`，再次提交字节数和 SHA-256。
5. 轮询 `GET /v1/media-assets/{asset_id}`，只有 `READY` 才能写入待发布作品。

首期只允许 `image/jpeg`、`image/png`、`image/webp`。SVG、未知容器、超过配置上限的文件在申请阶段拒绝。完成接口只说明对象已进入处理队列，响应状态通常为 `PROCESSING`，不代表安全。

用途包括 `CREATION_LAYER`、`CREATION_PREVIEW`、`PROVENANCE_PROOF`、`AIGC_OUTPUT` 和 `AVATAR`。头像上传处理完成后，通过 `PATCH /v1/profile` 写入 `avatar_asset_id`；后端只接受本人、`READY` 且用途为 `AVATAR` 的资产。

## 2. 安全处理

Worker 依次执行：

- 对象 HEAD 与完成请求的字节数一致性检查。
- 实际 SHA-256 校验，不能只相信客户端哈希。
- JPEG/PNG/WebP 文件签名与声明 MIME 一致性检查。
- ClamAV INSTREAM 病毒检查；生产环境无法连接扫描器时保持处理中并重试，不会放行。
- Pillow 限格式解码、损坏图片检查和解码像素上限。
- EXIF 方向归一化，重新编码且不复制 EXIF、XMP、ICC 和 PNG 文本元数据。
- 生成最长边 320 与 640 的私有缩略图。
- 保存内容安全和 AIGC 观察结果及检测器版本。

生产环境强制 `minio` 和 `clamav`；测试/开发的内存存储与 EICAR 检查器不能通过生产配置校验。没有可靠内容安全结论时记录 `REVIEW`，资产可以用于本人草稿，但作品必须进入人工内容复核。

媒体状态：`PROCESSING`、`READY`、`REJECTED`、`DELETION_PENDING`、`DELETED`。拒绝响应提供稳定的 `rejection_code`，例如 `HASH_MISMATCH`、`SIGNATURE_MISMATCH`、`MALWARE_DETECTED`、`IMAGE_DECODE_FAILED`、`PIXEL_LIMIT_EXCEEDED`。

## 3. 私有访问与删除

`GET /v1/media-assets/{id}` 仅限所有者。`READY` 响应中的原图和缩略图都是短时签名 URL，不保存永久公开 URL。其他用户访问统一返回 404。

`DELETE /v1/media-assets/{id}` 返回 `DELETION_PENDING` 并排队删除。被任何不可变作品版本、预览图或来源谱引用时返回 `MEDIA_ASSET_IN_USE`；不能破坏历史证据。项目删除使用 `DELETE /v1/creation-projects/{id}`，会立即从本人列表隐藏并撤回相关发布，保留最小审计证据等待保留策略清理。

## 4. 作品提交与审核

提交会额外验证版本引用的每个媒体资产：

- 资产不存在或不属于本人：`MEDIA_ASSET_INVALID`。
- 仍在处理、已拒绝或等待删除：`MEDIA_NOT_READY`。

完整提交进入 `PENDING_CHECK` 并创建审核案件。outbox Worker 在没有可信自动发布条件时转为 `PENDING_HUMAN_REVIEW`。审核决定只有三种：

- `PUBLISH`：发布记录变为 `PUBLISHED`。
- `RETURN`：变为 `RETURNED`，必须给用户可理解原因和修改建议。
- `RESTRICT`：变为 `RESTRICTED`，对外不可见。

前端使用 `GET /v1/publications/{id}/moderation-case` 或 `GET /v1/moderation-cases/{id}` 获取本人可见结果。响应不包含内部备注、检测原始证据、手机号或审核员身份，只返回公开原因与修改建议。

## 5. 申诉、复核和撤回

- `POST /v1/moderation-cases/{id}/appeals`：仅 `RETURNED` 或 `RESTRICTED` 可申诉；重复提交未处理申诉会返回同一记录。
- `GET /v1/me/moderation-appeals`：本人申诉列表。
- 申诉推翻后作品回到 `PENDING_HUMAN_REVIEW`，不会直接发布。
- `POST /v1/publications/{id}/withdraw`：本人撤回已发布作品，必须提交发布记录 `row_version` 防止覆盖新状态。

行囊 `privacy.pending_appeal_count` 返回真实待处理申诉数，退回作品同时返回面向用户的退回原因。

## 6. 隐私和账户数据

`GET|PATCH /v1/me/privacy-settings` 提供默认作品可见范围、学习卡是否公开、AIGC 导出标识和资料发现开关，并使用 `row_version` 乐观锁。未成年人响应会标记监护控制生效。

`GET /v1/account/export` 已包含作品版本、来源谱、发布记录、媒体元数据、申诉、隐私设置和本人触发的审计事件；不包含访问令牌、对象存储永久地址或内部审核证据。

## 7. 内部接口

`/v1/internal/*` 仅供 Worker 和审核后台，要求 `X-Internal-Token`。令牌错误统一表现为 404，不能放进 Android 包或前端环境变量。生产部署必须使用独立高熵令牌并限制内部网络来源。

主要内部动作：媒体处理、媒体物理删除、审核转人工、审核决定和申诉复核。所有决定写入只追加审计表，审计差异只保存必要的原因码、风险级别和引用，不保存手机号、Token、永久对象 URL 或完整敏感提示词。

## 8. 部署配置

Docker Compose 已加入固定版本 MinIO、ClamAV、API 和独立 outbox Worker。关键变量：

- `JIANGHU_MEDIA_STORAGE_PROVIDER=minio`
- `JIANGHU_MINIO_ENDPOINT=minio:9000`
- `JIANGHU_MINIO_PUBLIC_ENDPOINT=127.0.0.1:19000`
- `JIANGHU_MINIO_REGION=us-east-1`
- `JIANGHU_MINIO_ACCESS_KEY` / `JIANGHU_MINIO_SECRET_KEY`
- `JIANGHU_MEDIA_VIRUS_SCANNER=clamav`
- `JIANGHU_CLAMAV_HOST=clamav`
- `JIANGHU_INTERNAL_WORKER_TOKEN`
- `JIANGHU_MEDIA_MAX_UPLOAD_BYTES`
- `JIANGHU_MEDIA_MAX_IMAGE_PIXELS`

`MINIO_ENDPOINT` 是 API/Worker 在容器网络内访问对象存储的地址；`MINIO_PUBLIC_ENDPOINT`
只用于生成客户端可访问的预签名 URL。两者不能混用，否则 Android 会收到无法解析的
`minio:9000`。Windows 本机联调用 `127.0.0.1:19000`；Android 模拟器用
`10.0.2.2:19000`；生产环境使用对象存储的正式外部域名并按需启用
`JIANGHU_MINIO_PUBLIC_SECURE=true`。

ClamAV 首次下载病毒库可能需要较长时间。Compose 会等待 API 完成迁移并健康、MinIO
和 ClamAV 健康后再启动 Worker，避免 Worker 在 `outbox_events` 建表前抢跑。后续扫描
失败仍由 outbox 保留事件并退避重试，不会误放行文件。
