# 机巧江湖“行囊”后端完整实现方案

版本：v1.0  
基线日期：2026-09-01  
适用代码库：`Appproject/services/api`（FastAPI + SQLAlchemy + Alembic + PostgreSQL + Redis）

## 1. 结论与优化建议

现有意见方向正确，覆盖了行囊页面真正需要的后端能力。为了避免后续出现“数据能展示，但不可证明、不可审核、不可回滚”的问题，建议补充以下设计原则。

### 1.1 “四态”应定义为“四个已获得阶段 + 未闻初始态”

策划书中的收集阶段实际包含五个内部状态：`UNSEEN`（未闻）、`DISCOVERED`（偶得）、`LEARNED`（习得）、`MASTERED`（悟得）、`TEACHING`（传习）。产品文案仍可称“四态收集”，因为“未闻”表示尚未获得，不属于已获得的四个成长阶段。

状态只能由可验证学习事件推进。前端不能提交目标状态，也不能直接提交悟性、匠心、侠义数值。

### 1.2 不做“连续签到”，改做“有效修炼次数”

“本周修炼”应统计有效修炼会话，不统计登录、打开页面、停留时长或连续天数。一个会话只有完成漫画预测、有效试炼、迁移创作、结构化评招或作品修订中的至少一项，才计为一次修炼。

默认周区间采用 `Asia/Shanghai` 时区，按星期一 00:00 至下一星期一 00:00 的左闭右开区间计算。累计修行也以有效修炼会话为单位，前端统一显示“次”，不引入断签惩罚、连续签到压力或公开排名。

### 1.3 成长数据应展示“证据”，而不是只展示分数

“悟性、匠心、侠义”推荐返回证据数量、最近证据、证据来源和发生时间。若以后需要内部评分，评分也必须由版本化规则计算，不对外形成公开榜单。

- 悟性：预测、试炼解释、因果判断、核验选择。
- 匠心：迁移创作、版本迭代、修改理由、保留原创线稿。
- 侠义：隐私检查、素材授权、AIGC 标识、有效且有礼的结构化评招。

### 1.4 作品编辑状态与发布审核状态必须分开

`CreationProject` 表示创作项目，`CreationVersion` 表示不可变版本；`Publication` 表示某个版本的发布与审核过程。不能把草稿、版本和审核状态全部塞进一列，否则发布后的旧版本会被新编辑覆盖，审核证据也会失真。

行囊页面可以返回一个统一的 `display_status`，但数据库内应保留项目状态、媒体状态、发布状态和审核状态各自的事实。

### 1.5 行囊聚合接口应读取“投影”，不能在请求内串调多个业务 API

`GET /v1/me/luggage` 应在一次数据库事务内读取面向行囊的投影表，并配合短时缓存与 ETag。它不是一个在服务器内部依次调用资料、统计、秘籍、错题、作品五个 HTTP 接口的“拼接器”。

### 1.6 前端暂无入口不影响后端先行

未接入前端的接口仍应完成数据库迁移、OpenAPI 契约、权限检查、服务层、测试和 Swagger 调试。通过 `/v1/meta/capabilities` 或服务端功能开关控制是否在客户端展示入口，不使用虚假数据填充生产账户。

## 2. 当前项目基线

### 2.1 已有能力

- FastAPI 应用工厂、统一错误响应、请求 ID、中间件和 Trusted Host。
- PostgreSQL/SQLite 数据层、SQLAlchemy 2.x、Alembic 迁移。
- Redis 验证码和限流基础。
- 手机号注册登录、JWT 访问令牌和刷新令牌、会话撤销。
- 年龄层、监护同意记录、偏好设置、黑名单、反馈、数据导出和数据权利申请。
- 当前后端测试 45 项全部通过，总覆盖率约 90%；阶段 0-2 均有契约、接口和迁移回归测试。

### 2.2 当前缺口

- 后端没有秘籍、试炼、学习事件、进度、错题、作品、版本、来源谱、媒体、审核、申诉或行囊聚合模型。
- `LuggageScreen.kt` 的昵称、班级、匿名 ID、称号、勋章、统计、秘籍和作品均为静态值。
- 当前行囊文案仍为“签到：3天”，与策划书的无连续签到设计冲突。
- 当前监护设置由未成年人自己的访问令牌即可修改。产品化前必须改为监护角色写入、学生角色只读。
- Android 构建仍可把 MiniMax 密钥放入 `BuildConfig`，发布包中可被提取。模型供应商调用必须迁移到后端网关。
- Docker Compose 对外 API 端口为 `8010`，而 Android `build.gradle.kts` 的默认地址为 `8000`；接入前应统一环境配置。
- 当前账户导出只覆盖账户、设置、同意和会话，后续需纳入学习证据、作品、来源谱和审核申诉记录。

## 3. 推荐总体架构

第一阶段采用“模块化单体 + 独立媒体 Worker”，不拆成大量微服务。

### 3.1 同步请求链

Android 客户端调用 FastAPI。路由只负责认证、参数校验和响应映射；领域服务完成权限、事务和状态机；PostgreSQL 保存业务事实、不可变事件和行囊投影；Redis只用于限流、短时缓存和幂等辅助，不作为学习事实来源。

### 3.2 异步任务链

业务事务在 PostgreSQL 同时写入 `outbox_events`。媒体 Worker 使用 `FOR UPDATE SKIP LOCKED` 领取任务，完成查毒、解码、缩略图、OCR/隐私线索检测、内容审核和删除。这样即使 Redis 或 Worker 临时故障，任务也不会因消息丢失而永久消失。

### 3.3 对象存储

开发环境使用 S3 兼容的 MinIO；演示或生产环境替换为合规对象存储。至少划分三个逻辑区域：

- `quarantine`：客户端直传的隔离区，永不直接挂公开 CDN。
- `private`：通过检查的用户私有原图和版本资源。
- `published`：已通过发布审核的派生文件，不包含不必要的原始元数据。

所有桶默认私有。客户端只获取短时签名 URL。

### 3.4 后端模块边界

- `profiles`：用户资料、匿名 ID、头像、称号和勋章。
- `learning`：内容版本、试炼、尝试、学习事件、证据和统计。
- `manuals`：十卷五十页、进度、收藏、搜索和筛选。
- `mistakes`：错题聚合、重练和巩固规则。
- `creations`：项目、不可变版本、图层清单和修改记录。
- `provenance`：学习卡、人机分工、素材授权和 AIGC 证据。
- `media`：上传会话、媒体资产、派生文件和安全检查。
- `moderation`：自动审核、人工复核、原因码、申诉和处置。
- `privacy`：可见范围、监护授权、撤回、删除和审计。
- `luggage`：面向行囊页面的只读聚合模型。

现有单文件 `models.py` 和 `schemas.py` 会随着本功能快速膨胀。建议保留兼容导入，同时逐步拆到 `app/domains/<domain>/models.py`、`schemas.py`、`service.py` 和 `router.py`。

## 4. 核心数据模型

以下字段为建议的最小生产模型。所有主键使用 UUID；所有时间使用带时区 UTC 存储，响应中使用 ISO 8601。

### 4.1 用户资料、称号与勋章

`user_profiles`

- `user_id`：关联现有 `users.id`，一对一。
- `anonymous_id`：随机生成、稳定、唯一的公开 ID，不暴露数据库 UUID，也不使用自增序号。
- `avatar_asset_id`：只能引用已通过检查的私有媒体资产。
- `class_label`：可空，仅保存“五（三）班”这类班级标签，不保存学校名称。
- `current_title_id`：只能从用户已解锁称号中选择。
- `profile_visibility`：默认 `PRIVATE`。
- `created_at`、`updated_at`、`row_version`。

年龄层继续使用现有 `users.age_band`。年龄层变更单独走受控流程并写审计，不放入普通资料 PATCH。

`title_definitions` 保存称号配置与解锁规则版本；`user_titles` 保存解锁证据和时间。`badge_definitions` 与 `user_badges` 同理。前端可以选择当前称号，但不能自行声明已解锁称号或勋章。

### 4.2 十卷五十页和收藏

`manual_volumes` 保存十卷信息，`manual_pages` 保存五十页内容元数据：卷号、式号、标题、关键词、前置页、关联试炼、审核版本、发布状态和搜索文本。

`user_manual_favorites` 使用 `(user_id, manual_page_id)` 唯一约束。收藏是用户主动行为，不影响秘籍成长状态。

“未闻”不必为每位用户预先插入 50 行。列表查询用五十页内容左连接用户进度，缺失记录即返回 `UNSEEN`，避免注册时批量写入。

### 4.3 试炼和服务端判分

`trials` 保存试炼定义、内容版本、交互类型、关联秘籍和启用状态。`trial_versions` 保存不可变规则快照，包括输入 JSON Schema、判分策略、通过阈值、错误原因码和证据映射。

`trial_attempts`

- `user_id`、`trial_id`、`trial_version_id`。
- `practice_session_id`。
- `prediction_payload`、`answer_payload`、`explanation_payload`。
- `server_score`、`result`、`error_codes`。
- `started_at`、`submitted_at`、`graded_at`。
- `idempotency_key`，与用户形成唯一约束。
- `remediation_of_mistake_id`，重练时填写。

请求模型禁止出现 `score`、`passed`、`progress_state`、`evidence_count` 等服务端字段。即使恶意客户端提交这些字段，也应因 Pydantic `extra="forbid"` 返回 422。

### 4.4 不可变学习事件

`learning_events` 是学习事实来源，只追加、不原地修改。

- `event_type`：如 `PREDICTION_COMPLETED`、`TRIAL_GRADED`、`TRANSFER_EVIDENCE_APPROVED`、`STRUCTURED_REVIEW_ACCEPTED`、`PROJECT_REVISION_COMPLETED`。
- `user_id`、`actor_id`、`source_type`、`source_id`。
- `rule_version`、`payload`、`occurred_at`。
- `idempotency_key` 和 `request_id`。
- `revoked_at`、`revocation_reason`：只用于证据被审核判定无效，不删除原事实。

写入事件和更新同步投影必须处于同一数据库事务，保证用户提交试炼后立即读取行囊时能看到新状态。异步 Worker 只处理缩略图、审核、通知等非即时任务。

### 4.5 秘籍进度与证据

`manual_progress` 是可重建投影，唯一键为 `(user_id, manual_page_id)`。

- `state`：五个内部状态之一。
- `discovered_at`、`learned_at`、`mastered_at`、`teaching_at`。
- `latest_evidence_id`、`updated_at`、`projection_version`。

`progress_transitions` 保存每次状态变化的前态、后态、规则版本、触发事件和时间，用于“为什么升级”的学习证据时间线。

`learning_evidence`

- `category`：`WISDOM`、`CRAFT`、`CHIVALRY`。
- `evidence_type`、`source_type`、`source_id`。
- `manual_page_id`、`summary`、`rule_version`。
- `validation_status`：`VALID`、`PENDING_REVIEW`、`REVOKED`。
- `created_at`、`validated_at`。

### 4.6 修炼统计

`practice_sessions` 保存会话开始、最后活动、首次有效事件和结束时间。只有出现合格事件才写 `qualified_at`，同一会话只计一次。

`user_learning_stats` 是累计投影：有效修炼总次数、已通过不同试炼数、三类有效证据数、最后修炼时间和投影版本。

本周修炼可以由 `practice_sessions` 按周查询；数据量增加后再增加 `user_weekly_learning_stats`。不存“连续签到天数”。

### 4.7 错题与巩固

`mistake_items` 是对失败尝试的用户级聚合，不简单复制每次 Attempt。

- `user_id`、`trial_id`、`knowledge_point_code`。
- `first_attempt_id`、`latest_attempt_id`。
- `original_answer_payload`。
- `error_reason_code`、`error_reason_summary`。
- `manual_page_id`。
- `status`：`TO_REVIEW`、`PRACTICING`、`CONSOLIDATED`。
- `next_review_at`、`consolidated_at`。

`remediation_records` 关联错题和新的 `trial_attempts`，保存重练结果与学习者反思。

默认巩固规则建议为：重练通过后，再在至少 24 小时后的复习中通过一次。只有服务端满足规则后才把状态改为 `CONSOLIDATED`。规则应版本化，比赛阶段可通过配置降低时间门槛，但不能由前端直接勾选“已巩固”。

### 4.8 作品、版本与修改记录

`creation_projects`

- 所有者、主题、媒体类型、当前版本 ID。
- 项目状态：`DRAFT`、`ACTIVE`、`ARCHIVED`、`DELETED`。
- 默认可见范围和乐观锁版本。

`creation_versions` 每次保存都新建不可变行：版本号、父版本、版本说明、图层清单、画布信息、预览资产、Agent 会话、创建人和创建时间。已发布版本永不覆盖。

图层可先以经过 JSON Schema 校验的 `layer_manifest` JSONB 快照保存，每个图层只引用 `media_assets`。若以后需要多人协作或图层级查询，再拆为 `creation_layers`。

`creation_change_logs` 保存动作类别、版本差异摘要和修改理由。日志不保存无关触摸轨迹或键盘记录。

### 4.9 学习卡与来源谱

`learning_cards` 与某个 `creation_version` 一对一：

- 至少一条 `manual_page_id` 关联。
- `method_summary`：使用了什么原理和方法。
- `unresolved_questions`：仍不确定的问题，可为空但必须由用户确认。
- 完成状态和校验结果。

`provenance_manifests` 是版本化清单，提交审核时锁定。`provenance_items` 逐项记录：

- 人工贡献：本人绘制、本人编剧、本人修改。
- AI 贡献：供应商、模型、工具动作、提示摘要、输出资产和人工修改。
- 外部素材：来源、作者、授权类型、授权证明资产、使用范围。
- AIGC 标识：用户声明、系统观察和导出文件标识状态。

不要记录“人创 70%”这类无法验证的比例。系统只记录可追溯动作和资产关系。

### 4.10 媒体、安全检查与缩略图

`upload_sessions` 保存上传意图、允许的 MIME、大小上限、对象键、过期时间、客户端哈希和完成状态。

`media_assets` 保存所有者、原始文件名的净化版本、实际 MIME、字节数、SHA-256、隔离对象键、私有对象键、状态和删除时间。

`media_derivatives` 保存 320px、640px 等缩略图或安全转码版本。客户端不能提交最终缩略图 URL。

`media_scan_results` 逐项保存文件签名、病毒、解码、超大像素、EXIF/定位、人脸/校服/地址线索、内容安全和 AIGC 标识检测结果，以及检测器版本。

首期建议只开放 JPEG、PNG、WebP，拒绝 SVG 和未知容器；图片上限建议 20 MB，并额外限制解码后的像素数。视频在完成图片链路后再开放。

### 4.11 发布、审核、申诉和审计

`publications` 固定引用一个 `creation_version`、一个完整学习卡和一个锁定来源谱。

发布状态：`DRAFT`、`PENDING_CHECK`、`PENDING_HUMAN_REVIEW`、`PUBLISHED`、`RETURNED`、`RESTRICTED`、`WITHDRAWN`、`DELETED_EVIDENCE_ONLY`。

`moderation_cases` 保存自动检测原因码、风险等级、模型版本、人工处置和最小必要证据。`moderation_appeals` 保存申诉、复核人和结果。审核员只看完成判断所需内容，不显示手机号、学校或精确身份资料。

`audit_events` 只追加，保存操作者、动作、目标、结果、请求 ID 和净化后的差异摘要。不得在普通日志或审计表中保存访问令牌、手机号明文、完整提示词中的个人信息或对象存储永久 URL。

`outbox_events` 支持媒体处理、审核、删除和投影重建任务的可靠执行。

## 5. 关键状态机

### 5.1 秘籍成长状态机

1. `UNSEEN -> DISCOVERED`：完成漫画阅读并提交有效预测。
2. `DISCOVERED -> LEARNED`：通过服务端判分的原理试炼，并满足解释要求。
3. `LEARNED -> MASTERED`：在新情境创作、讲解或案例中正确应用原理，证据自动验证或人工通过。
4. `MASTERED -> TEACHING`：给非本人作品提供一条有效、有礼、可执行的结构化评招，并通过规则检查。

状态通常单向前进。若证据因作弊、误判或审核被撤销，重算器可以把投影回退，但必须保留原事件、撤销原因和审计记录。

### 5.2 作品发布状态机

1. 项目在 `DRAFT` 中可反复新建版本。
2. 用户提交发布时，服务端检查当前版本、学习卡、来源谱、可见范围和媒体状态；缺项返回明确原因码，不进入审核。
3. 完整后进入 `PENDING_CHECK`，媒体与内容审核异步执行。
4. 自动审核低风险且高置信时可进入 `PUBLISHED`；低置信或高风险进入 `PENDING_HUMAN_REVIEW`。
5. 可修改问题进入 `RETURNED`，必须给出原因码、儿童易懂说明和修改建议。
6. 用户基于退回版本创建新版本后重新提交，不能覆盖旧审核记录。
7. 已发布作品可由本人或合法监护角色撤回为 `WITHDRAWN`。
8. 严重违规进入 `RESTRICTED` 或删除流程，对外立即不可见，仅按经法务确认的期限保留最小审计证据。

## 6. API 设计

所有路径沿用现有 `/v1` 前缀和 Bearer 鉴权。创建尝试、版本、上传完成和发布提交必须支持 `Idempotency-Key`。更新资料和项目元数据使用 `If-Match` 或 `row_version` 做乐观并发控制。

### 6.1 资料、称号和勋章

- `GET /v1/me/profile`：读取本人资料。
- `PATCH /v1/me/profile`：只允许昵称、班级标签、已通过检查的头像和已解锁称号。
- `POST /v1/me/profile/age-band-change-requests`：受控年龄层变更。
- `GET /v1/me/titles`：已解锁称号与证据。
- `GET /v1/me/badges`：勋章列表与获得时间。

### 6.2 秘籍、收藏、证据和统计

- `GET /v1/manuals?state=&volume=&q=&favorites_only=&cursor=&limit=`：搜索、十卷筛选、状态筛选、收藏筛选和游标分页。
- `GET /v1/manuals/{manual_page_id}`：秘籍详情、达成条件和最近证据摘要。
- `GET /v1/manuals/{manual_page_id}/evidence`：完整学习证据。
- `GET /v1/manuals/{manual_page_id}/learning-history`：状态时间线、触发事件与规则版本。
- `PUT /v1/manuals/{manual_page_id}/favorite`：幂等收藏。
- `DELETE /v1/manuals/{manual_page_id}/favorite`：取消收藏。
- `GET /v1/me/learning-stats`：本周、累计、试炼通关和三类证据。
- `POST /v1/trials/{trial_id}/attempts`：提交预测、答案和解释，由服务端判分并生成事件。

不存在 `PATCH /progress`、`PATCH /stats` 或“增加悟性”接口。

### 6.3 错题与重练

- `GET /v1/mistakes?status=&manual_page_id=&cursor=&limit=`：错题列表。
- `GET /v1/mistakes/{mistake_id}`：原答案、原因、关联秘籍、重练历史和下一次复习时间。
- `POST /v1/mistakes/{mistake_id}/retry-sessions`：创建短时重练上下文，返回试炼版本和提交地址。
- 重练结果仍提交到统一的 `POST /v1/trials/{id}/attempts`，并携带服务端签发的重练上下文 ID。

### 6.4 作品、版本、学习卡和来源谱

- `POST /v1/creation-projects`：创建草稿项目。
- `GET /v1/me/creation-projects?status=&cursor=&limit=`：本人作品列表。
- `GET /v1/creation-projects/{id}`：项目详情与当前版本。
- `PATCH /v1/creation-projects/{id}`：修改项目标题、默认可见范围等可变元数据。
- `POST /v1/creation-projects/{id}/versions`：创建不可变版本。
- `GET /v1/creation-projects/{id}/versions?cursor=`：版本和修改记录。
- `PUT /v1/creation-versions/{id}/learning-card`：保存并校验学习卡。
- `PUT /v1/creation-versions/{id}/provenance-manifest`：保存来源谱草稿。
- `POST /v1/creation-projects/{id}/submissions`：锁定当前版本、学习卡和来源谱并提交审核。
- `POST /v1/publications/{id}/withdraw`：撤回已发布作品。
- `DELETE /v1/creation-projects/{id}`：发起删除；先不可见，再异步清理对象。
- `POST /v1/moderation-cases/{id}/appeals`：提交审核申诉。

### 6.5 媒体

- `POST /v1/uploads/intents`：校验用途、格式和大小后签发隔离区上传地址。
- 客户端直接上传对象存储，不把二进制文件穿过 FastAPI。
- `POST /v1/uploads/{upload_id}/complete`：提交字节数和 SHA-256，服务端 HEAD 校验并排队处理。
- `GET /v1/media-assets/{asset_id}`：读取处理状态和短时私有 URL；只有资源所有者或授权角色可用。
- `DELETE /v1/media-assets/{asset_id}`：仅未被锁定版本引用时可直接删除；否则走引用检查和延迟删除。

### 6.6 隐私、监护和数据权利

- `GET /v1/me/privacy-settings`：本人可见范围、AIGC 导出设置和监护状态。
- `PATCH /v1/me/privacy-settings`：本人可修改的隐私选项。
- `GET /v1/guardian/children/{child_id}/controls`：监护角色读取。
- `PATCH /v1/guardian/children/{child_id}/controls`：仅已验证监护角色可写。
- 现有 `/v1/settings/guardian-controls` 可暂时保留兼容读取，但应停止让学生令牌写入。
- 现有 `/v1/account/data-rights-requests` 扩展作品撤回、数据删除和同意撤回的处理进度。
- 现有 `/v1/account/export` 改为小数据同步、大数据异步导出，并包含学习、作品和来源谱。

### 6.7 行囊聚合接口

`GET /v1/me/luggage?manual_limit=3&mistake_limit=3&creation_limit=4`

响应应包含：

- `profile`：昵称、头像短时 URL、年龄层、班级标签、匿名 ID、当前称号和勋章摘要。
- `stats`：本周修炼次数、累计有效修炼、不同试炼通关数、悟性/匠心/侠义证据摘要。
- `manuals`：总页数、已获得数、各状态数量、最近秘籍和“查看全部”链接。
- `mistakes`：待巩固数量、最近错题、明确的空状态标识和“再试一次”目标。
- `creations`：各发布状态数量、最近作品、缩略图、版本、退回原因摘要和修订入口能力。
- `privacy`：监护是否生效、待处理申诉数量和隐私入口能力。
- `meta`：`generated_at`、`snapshot_version`、`etag` 和各完整列表的链接。

无数据时返回 200、计数为 0、列表为空，并提供稳定的 `empty_reason`；不要返回 404。审核或媒体仍在处理时返回明确状态，不把“处理中”伪装成网络失败。

聚合响应只返回首屏摘要。搜索、筛选和分页继续走各领域列表接口，避免一个响应无限增长。

### 6.8 行囊响应示例

以下示例只展示稳定契约，不代表用假数据初始化真实用户：

```json
{
  "data": {
    "profile": {
      "nickname": "阿砚",
      "avatar": {
        "asset_id": "8a799d72-5557-4ab2-995c-51373f376452",
        "url": "https://signed.example/private/avatar",
        "expires_at": "2026-09-01T08:05:00Z"
      },
      "age_band": "UNDER_14",
      "class_label": "五（三）班",
      "anonymous_id": "JH-7M4K9Q2X",
      "current_title": {
        "code": "APPRENTICE",
        "name": "见习弟子"
      },
      "badges": [
        {
          "code": "FIRST_TRIAL",
          "name": "初试锋芒",
          "earned_at": "2026-08-31T03:20:00Z"
        }
      ]
    },
    "stats": {
      "week": {
        "timezone": "Asia/Shanghai",
        "starts_at": "2026-08-30T16:00:00Z",
        "ends_at_exclusive": "2026-09-06T16:00:00Z",
        "practice_count": 3
      },
      "lifetime_practice_count": 7,
      "distinct_trials_passed": 5,
      "evidence": {
        "wisdom": {"count": 6, "latest_at": "2026-09-01T02:10:00Z"},
        "craft": {"count": 2, "latest_at": "2026-08-31T10:30:00Z"},
        "chivalry": {"count": 1, "latest_at": "2026-08-30T09:00:00Z"}
      }
    },
    "manuals": {
      "total": 50,
      "obtained": 3,
      "counts_by_state": {
        "UNSEEN": 47,
        "DISCOVERED": 1,
        "LEARNED": 1,
        "MASTERED": 1,
        "TEACHING": 0
      },
      "items": [
        {
          "id": "60b98362-a0d2-4c8c-9286-f56960096608",
          "volume": 5,
          "title": "百炼识物诀",
          "state": "MASTERED",
          "state_label": "悟得",
          "latest_evidence_summary": "在校园分类机关兽中正确使用标签与人工复核",
          "updated_at": "2026-09-01T02:10:00Z"
        }
      ],
      "detail_url": "/v1/me/manuals"
    },
    "mistakes": {
      "pending_count": 1,
      "empty_reason": null,
      "items": [
        {
          "id": "c317f3ae-3272-42f9-8b44-d192cff8bd0a",
          "knowledge_point": "训练数据与测试数据不可混用",
          "status": "TO_REVIEW",
          "manual_page_id": "60b98362-a0d2-4c8c-9286-f56960096608",
          "retry_url": "/v1/mistakes/c317f3ae-3272-42f9-8b44-d192cff8bd0a/retry-sessions"
        }
      ],
      "detail_url": "/v1/mistakes"
    },
    "creations": {
      "counts_by_status": {
        "DRAFT": 1,
        "PENDING_CHECK": 0,
        "PENDING_HUMAN_REVIEW": 0,
        "PUBLISHED": 1,
        "RETURNED": 0
      },
      "items": [
        {
          "project_id": "9fb9679d-9614-4204-ac4f-e243e2ed174a",
          "title": "校园分类机关兽",
          "display_status": "PUBLISHED",
          "current_version": 2,
          "thumbnail": {
            "asset_id": "bd7bbdd8-88aa-482c-9f66-00d64989c387",
            "url": "https://signed.example/private/thumbnail",
            "expires_at": "2026-09-01T08:05:00Z"
          },
          "can_revise": true,
          "return_reason": null,
          "updated_at": "2026-08-31T10:30:00Z"
        }
      ],
      "detail_url": "/v1/me/creation-projects"
    },
    "privacy": {
      "guardian_controls_active": true,
      "pending_appeal_count": 0,
      "privacy_settings_url": "/v1/me/privacy-settings"
    }
  },
  "meta": {
    "generated_at": "2026-09-01T08:00:00Z",
    "snapshot_version": 42,
    "etag": "W/\"luggage-user-version-42\""
  }
}
```

签名 URL 仅为传输便利，不是媒体身份。客户端应持久化 `asset_id`，不能持久化 URL。

### 6.9 聚合缓存与失效

- PostgreSQL 投影为权威数据；Redis 缓存建议 15-30 秒。
- 缓存键包含用户 ID、请求的三个 limit 和 `luggage_projection_version`。
- 资料、试炼、秘籍收藏、错题、作品、媒体或审核事务更新成功后递增该用户的投影版本并删除旧缓存。
- 生成响应时返回弱 ETag；客户端使用 `If-None-Match`，未变化返回 304。
- Redis 故障时直接读取 PostgreSQL，不影响正确性。
- 不把短时签名 URL 放入长缓存；可在组装响应的最后一步批量签名，或缓存不含 URL 的结构化快照。

## 7. 服务端计算规则

### 7.1 一次试炼提交的事务步骤

1. 认证用户并校验年龄、试炼可见性、内容版本和限流。
2. 以 `(user_id, idempotency_key)` 查询重复提交；重复请求返回第一次结果。
3. 按 `trial_version` 的 JSON Schema 校验输入。
4. 使用版本化判分策略计算分数、通过结果和错误原因码。
5. 插入不可变 `trial_attempt`。
6. 生成一个或多个 `learning_events`。
7. 同步更新修炼会话、秘籍进度、学习证据、累计统计和错题投影。
8. 写入审计摘要和必要的 outbox 事件。
9. 一次提交事务成功后统一返回；任何步骤失败则全部回滚。

### 7.2 统计口径

- 本周修炼：本周 `qualified_at` 不为空的不同修炼会话数。
- 累计修行：历史有效修炼会话总数。
- 试炼通关数：至少通过一次的不同 `trial_id` 数，不是通过尝试次数。
- 悟性/匠心/侠义：状态为 `VALID` 的不同证据数；已撤销证据不计入。
- 最近秘籍：按最近一次有效进度变化排序，而不是按内容 ID。
- 最近作品：按用户最后一次版本或审核状态变化排序。

### 7.3 幂等和并发

- 试炼提交、版本创建、上传完成、提交审核必须有数据库唯一幂等键。
- 同一秘籍投影更新使用行锁或原子 UPSERT，防止并发尝试重复晋级。
- 版本号使用 `(project_id, version_number)` 唯一约束并由数据库事务分配。
- 发布提交锁定明确版本 ID；之后即使项目继续编辑，也不改变正在审核的内容。

## 8. 媒体处理流水线

### 8.1 上传前

- 校验用途、账户权限、监护限制、声明 MIME、扩展名、大小和每日配额。
- 生成不可预测对象键，禁止使用原始文件名作为路径。
- 签名只允许单对象、指定大小范围和短时有效。

### 8.2 上传后

1. 校验对象真实存在、大小和 SHA-256。
2. 通过 magic bytes 判断真实类型，扩展名不作为依据。
3. 病毒扫描、图片完整解码和超大像素/压缩炸弹检查。
4. 清除 EXIF、GPS 和非必要元数据；保留原始文件仅在私有区。
5. 生成安全预览和 320px/640px 缩略图。
6. 执行 OCR、可能的人脸/校服/地址/二维码线索检查。
7. 执行图像内容安全和 AIGC 标识检查，保存检测器版本与置信度。
8. 自动低风险通过；低置信或高风险进入人工复核；失败则给出可修正原因。

任何一步失败都不能把文件复制到 `published`。Worker 重试必须幂等，重试不会生成重复派生文件。

## 9. 权限与隐私模型

### 9.1 角色

- `STUDENT`：只管理本人资料、学习、草稿和可撤回作品。
- `GUARDIAN`：只管理已验证绑定儿童的监护控制和依法允许的撤回/删除操作。
- `REVIEWER`：只查看待审核内容和必要原因，不查看账户敏感信息。
- `ADMIN`：配置内容和处置重大案件，关键动作需更高权限并审计。
- `SERVICE`：媒体 Worker 等机器身份，只能访问规定任务。

### 9.2 可见范围

建议枚举为 `PRIVATE`、`GUARDIAN_ONLY`、`CLASSROOM`、`COMMUNITY`。默认草稿为 `PRIVATE`。`CLASSROOM` 在教师/班级能力正式完成前可保留后端枚举但不对前端开放。

公开发布仅暴露匿名昵称、公开头像、年龄层级和作品学习卡。班级标签、内部用户 ID、手机号、监护关系和审核备注不得进入公共响应。

### 9.3 撤回、删除与留证

- 撤回：立即从推荐和公开访问中移除，可保留本人私有版本。
- 删除：立即禁用访问和签名 URL，后台检查引用后删除对象及派生文件。
- 审计留证：只保留政策要求的最小事件摘要和哈希，不保留不必要的完整内容。
- 具体保留期限必须配置化并由法务确认，不能在代码中随意写死。

## 10. 非正常状态与错误契约

后端需要为前端提供可稳定映射的机器错误码和儿童易懂提示。

- `LUGGAGE_NOT_READY`：极短暂的初始化投影未完成；正常注册流程应主动初始化，尽量不出现。
- `TRIAL_VERSION_STALE`：客户端使用旧试炼版本，返回可重载的当前版本。
- `ATTEMPT_ALREADY_PROCESSED`：幂等重放，返回原结果而不是报错。
- `LEARNING_CARD_INCOMPLETE`：列出缺少的字段。
- `PROVENANCE_INCOMPLETE`：列出未声明的人机分工或授权项。
- `MEDIA_PROCESSING`：媒体仍在检查，不允许提交发布。
- `MEDIA_REJECTED`：返回原因码和可修正建议。
- `MODERATION_RETURNED`：返回退回原因、可修改字段和申诉能力。
- `VISIBILITY_NOT_ALLOWED_FOR_AGE`：年龄或监护限制不允许该可见范围。
- `FORBIDDEN_RESOURCE`：统一资源越权响应，不泄露资源是否存在。
- `VERSION_CONFLICT`：乐观锁冲突，提示刷新后合并。

加载骨架由前端实现，但后端应支持快速超时、ETag、明确处理状态和安全重试。网络错误时客户端可展示本地缓存，任何缓存数据都不能反向覆盖服务端统计。

## 11. 数据库迁移与种子顺序

不要把全部能力放进一个巨大迁移。建议按以下顺序：

1. `0003_profiles_and_taxonomy`：资料、匿名 ID、称号、勋章、十卷五十页元数据和收藏。
2. `0004_learning_events_and_progress`：试炼版本、尝试、会话、事件、证据、进度和统计。
3. `0005_mistakes_and_remediation`：错题聚合和重练。
4. `0006_creations_and_provenance`：项目、版本、学习卡、来源谱和修改记录。
5. `0007_media_moderation_privacy`：上传、媒体、派生文件、审核、申诉、隐私和发布。
6. `0008_luggage_projection_and_outbox`：聚合投影、审计和 outbox。

十卷五十页使用可重复执行的版本化 seed 命令，不在 Alembic 中塞大段可变正文。种子必须校验页数、卷号、唯一 slug、关联试炼和审核版本。

### 11.1 必要索引和约束

- `user_profiles.anonymous_id` 唯一索引。
- `manual_pages(volume_no, page_no)` 与 `manual_pages.slug` 唯一约束。
- `manual_progress(user_id, state, updated_at DESC)`，支持行囊计数和最近秘籍。
- `trial_attempts(user_id, idempotency_key)` 唯一约束；另建 `(user_id, trial_id, submitted_at DESC)`。
- `learning_events(user_id, occurred_at DESC)` 与 `(source_type, source_id)`。
- `learning_evidence(user_id, category, validation_status, created_at DESC)`。
- `mistake_items(user_id, status, updated_at DESC)`；同一打开错题的聚合键使用部分唯一索引。
- `creation_versions(project_id, version_number)` 唯一约束。
- `creation_projects(owner_user_id, updated_at DESC)`。
- `publications(owner_user_id, status, updated_at DESC)` 与待人工审核部分索引。
- `media_assets(owner_user_id, status, created_at DESC)` 和 `media_assets.sha256` 非唯一检索索引；不同用户之间不能仅凭哈希共享私有对象权限。
- `outbox_events(status, available_at)` 的待处理部分索引，Worker 领取时使用跳过锁定。

不要无差别给所有 JSONB 建 GIN 索引。只有确认存在稳定 JSON 路径查询后再增加表达式索引；行囊常用字段应进入普通列或投影表。

## 12. 代码实施结构

建议新增：

```text
services/api/app/domains/
  profiles/
  learning/
  manuals/
  mistakes/
  creations/
  provenance/
  media/
  moderation/
  privacy/
  luggage/
services/api/app/workers/
  outbox.py
  media_pipeline.py
services/api/app/api/routes/
  profile.py
  learning.py
  manuals.py
  mistakes.py
  creations.py
  uploads.py
  moderation.py
  luggage.py
services/api/tests/
  test_luggage.py
  test_progress_rules.py
  test_trial_attempts.py
  test_mistakes.py
  test_creation_versions.py
  test_media_pipeline.py
  test_moderation_permissions.py
  test_privacy_and_deletion.py
```

业务枚举和事件契约应集中管理并进入 OpenAPI/JSON Schema。移动端模型由契约生成或严格对齐，禁止口头约定字段。

### 12.1 环境与可观测性

Docker Compose 至少增加 MinIO、媒体 Worker 和病毒扫描服务；API 进程只处理 JSON 与签名，不执行 FFmpeg、OCR 或大图解码。开发、测试、演示环境使用不同桶、密钥和数据库。

建议新增配置：对象存储端点与桶名、签名有效期、图片大小/像素上限、Worker 并发、审核阈值、错题巩固规则版本、媒体与审计保留策略。秘密只从环境或秘密管理服务读取。

关键指标：

- 行囊接口 p50/p95、缓存命中率和数据库查询数。
- 试炼提交量、判分失败率、幂等重放量和状态晋级量。
- 投影版本落后、投影重建失败和 outbox 最老任务年龄。
- 隔离区文件数、扫描耗时、拒绝原因分布和缩略图失败率。
- 待人工审核数量、最老案件年龄、退回率和申诉改判率。
- 撤回到公共不可见的延迟、对象删除失败和签名 URL 越权拒绝量。

日志只记录 request ID、用户内部 ID 的受控表示、资源 ID、状态变化和错误码。不得记录完整答案正文、提示词、手机号、访问令牌或对象存储签名参数。

## 13. 分期实施计划

### 阶段 0：契约和安全基线，1-2 个开发日

- 冻结字段命名、五个内部秘籍状态、证据类型、错误码和可见范围。
- 输出 OpenAPI 草案和状态机单元测试样例。
- 修复监护设置写权限设计；确定对象存储和审核供应商边界。
- 把模型供应商密钥从 Android 迁往后端网关列为上线阻断项。

验收：契约评审通过，客户端无需猜字段，所有敏感写操作都有明确角色。

### 阶段 1：资料、内容目录和行囊骨架，3-4 个开发日

实施状态（2026-09-01）：已完成 `0003_profiles_and_taxonomy`、版本化 10 卷 50 页种子、资料接口、目录搜索/按卷/按状态筛选、游标分页、私人收藏、能力开关，以及带 ETag 的明确空状态行囊骨架。学习进度仍只返回事实默认值 `UNSEEN`，不会制造假修炼数据；真实事件、统计和进度进入阶段 2。

- 迁移用户资料、匿名 ID、称号、勋章、十卷五十页和收藏。
- 实现资料接口、秘籍列表筛选和空的行囊聚合骨架。
- 增加 ETag、游标分页和能力开关。

验收：新旧用户都能返回稳定行囊响应；50 页可搜索、按卷和状态筛选。

### 阶段 2：试炼、事件、进度、统计和错题，5-7 个开发日

实施状态（2026-09-01）：已完成 `0004_learning_events_and_progress` 与 `0005_mistakes_and_remediation`。首个纵向试炼使用版本化精确 JSON 判分；同一事务写入尝试、修炼会话、事件、悟性证据、进度、统计和错题。幂等键防止重复计数；失败产生错题，一次性重练上下文与 24 小时延迟复习共同决定巩固。目录和行囊已读取真实投影。其余 49 页的试炼属于内容配置工作，不生成虚假答案。

- 实现试炼版本、服务端判分、幂等尝试。
- 同事务生成学习事件、证据、进度和统计投影。
- 实现错题生成、重练和巩固规则。
- 补齐并发、回放和投影重建测试。

验收：客户端伪造统计或状态均失败；四次状态晋级都有可追溯证据；“本周修炼”不计算登录和页面打开。

### 阶段 3：作品、版本、学习卡和来源谱，5-7 个开发日

实施状态（2026-09-01）：已完成 `0006_creations_and_provenance`。项目元数据使用乐观锁，版本与图层快照不可变；学习卡和来源谱可先保存草稿，提交前统一校验秘籍、方法、人机分工、素材授权和 AIGC 声明；提交使用幂等键并锁定相关证明。真实媒体处理和发布状态流转属于阶段 4。

- 实现项目和不可变版本。
- 实现学习卡、人机分工、素材授权、AIGC 声明和修改记录。
- 实现提交前完整性校验和退回后新版本修订。

验收：已发布版本不可覆盖；缺学习卡或来源谱无法进入审核；版本差异和修改理由可追溯。

### 阶段 4：图片媒体、审核、隐私和申诉，5-7 个开发日

实施状态（2026-09-01）：已完成 `0007_media_moderation_privacy`。客户端只获得隔离区预签名 PUT；完成接口以 HEAD 校验对象后排队。Worker 校验 SHA-256、文件签名、ClamAV、Pillow 解码和像素限制，重新编码以移除元数据并生成 320/640 缩略图。生产配置强制 MinIO 与 ClamAV；没有可靠自动内容结论时进入人工复核。退回、申诉、复核、发布、撤回、项目删除、隐私设置、账户导出和只追加审计均已接入。

- 增加 MinIO/对象存储、隔离上传和 outbox Worker。
- 实现查毒、真实类型、解码、元数据清理、缩略图和审核结果。
- 实现发布状态机、人工复核接口、撤回、删除和申诉。
- 扩展监护、数据导出和数据权利处理。

验收：未检查文件无法公开；越权访问、永久 URL、路径穿越和重复任务测试通过；撤回立即生效。

### 阶段 5：聚合优化、前端联调和质量门禁，3-4 个开发日

实施状态（2026-09-01）：后端优化、完整 Docker 联调和接入设计已完成。`GET /v1/me/luggage` 使用 30 秒 Redis 短缓存，SQLAlchemy 事务提交后按用户精确失效，内容目录/称号/勋章定义变更时全量失效；Redis 故障自动回源 PostgreSQL。作品当前版本与缩略图使用批量查询，不随作品数量产生 N+1。已增加缓存命中查询数、跨用户精确失效、Redis 故障、ETag/304 和 1/12 件作品查询定界测试。真实双 API Worker 环境下，200 路径 p95 为 133.1 ms，304 路径 p95 为 78.1 ms；真实 MinIO、ClamAV、缩略图和签名下载链路通过。联调同时修复了 Alembic 长 revision 列宽、Worker 迁移抢跑和 MinIO 内外端点混用。Android 代码依照最初边界未改动，完整接入清单已输出，等页面更新后连接。

- 完成行囊投影、Redis 短缓存和精确失效。
- 输出 Android DTO、Repository 和 ViewModel 接入说明。
- 加入契约、权限、负载、故障注入和端到端测试。
- 更新 README、环境变量、Docker Compose 和演示数据 seed。

验收：典型演示数据下，行囊接口 p95 小于 300 ms；一次请求返回首屏所需内容；各详情列表可以独立分页。

总体工作量约 22-31 个后端开发日。两名熟悉现有项目的开发者并行时，可按 4-5 周完成；单人应优先完成阶段 0-3 和图片上传最小链路，再扩展人工审核后台。

## 14. 测试与验收清单

### 14.1 学习可信性

- 前端提交 `score`、`passed`、`state` 或证据数返回 422。
- 同一个幂等键重复提交只产生一条 Attempt 和一组事件。
- 并发两次通过不会重复晋级或重复增加统计。
- 事件回放可重建相同进度和累计统计。
- 试炼版本变更不改变历史判分和历史证据。

### 14.2 错题

- 失败尝试生成或更新正确知识点的错题。
- 原答案和错误原因来自服务端判分结果。
- 重练历史完整，提前重复提交不会直接变成已巩固。
- 无错题返回 200 和空列表。

### 14.3 作品与来源

- 每个版本不可变，父子版本链完整。
- 缺少秘籍关联、方法、人机分工或素材授权时不能发布。
- AI 图层必须有完整来源项和 AIGC 声明，已提交版本的来源证据不可修改。
- 退回原因可见但审核员内部备注不泄露。

### 14.4 媒体与安全

- 扩展名伪装、SVG、超大像素、损坏图片、病毒样本和哈希不一致被拒绝。
- 私有对象 URL 短时有效且不能跨用户访问。
- 只有通过检查的派生文件能进入 published 区域。
- Worker 重试、进程中断和重复领取不生成重复资源。

### 14.5 权限与隐私

- 学生不能修改服务端统计、审核结果或监护限制。
- 监护人只能访问已验证绑定的儿童。
- 审核员看不到手机号和不必要的资料。
- 作品撤回后公共 URL、推荐流和收藏入口立即失效。
- 导出包含用户本人数据但不包含令牌、密钥、内部风控特征和他人隐私。

### 14.6 聚合接口

- 首次注册、全空、部分数据、媒体处理中、审核退回和网络重试场景都有固定契约。
- 聚合值与领域明细一致。
- ETag 未变化返回 304；数据变化后立即失效。
- 50 页、数百次尝试和数十个作品条件下无 N+1 查询。

## 15. 前端后续接入方式

后端可以先完成全部能力。Android 更新时只需增加一个行囊数据层：

1. `LuggageApi` 调用 `GET /v1/me/luggage`。
2. `LuggageRepository` 保存最近成功快照和 ETag。
3. `LuggageViewModel` 暴露 `Loading`、`Content`、`Empty`、`NetworkError` 和 `PartialProcessing` UI 状态。
4. 首屏只使用聚合响应；“查看全部”、筛选、搜索、分页、证据详情和版本修订再调用领域接口。
5. 图片使用 Coil 加载短时签名 URL；URL 过期时刷新媒体详情，不把 URL 持久化为资源身份。
6. 把静态“签到：3天”替换为服务端 `weekly_practice_count`，文案显示“本周修炼：3次”。

前端尚未制作的“学习证据”“隐私与安全”“审核申诉”“版本修订”入口可以暂不展示，但对应 API、权限和测试应提前完成。

## 16. 上线前必须解决的阻断项

- 禁止客户端直改统计、进度、巩固和审核状态。
- 禁止把 MiniMax 或其他模型供应商密钥打包进 APK。
- 禁止未审核媒体进入公开对象存储或 CDN。
- 修复监护设置由学生账号自行写入的问题。
- 统一 Android 与 Docker 的 API 基础地址和环境配置。
- 建立匿名 ID，公共接口不暴露内部用户 UUID、手机号、班级或监护关系。
- 明确保留期限、删除流程、监护同意和人工审核责任人。
- 所有发布版本必须具有完整学习卡、来源谱和 AIGC 标识结果。

## 17. 最终建议的首个可交付纵切

第一条纵向链路建议选择《百炼识物诀》：

1. 用户完成漫画预测，秘籍从未闻进入偶得。
2. 用户提交分类试炼，服务端判分并生成错误或习得证据。
3. 错误尝试进入错题，用户通过“再试一次”完成重练。
4. 用户创建“校园垃圾分类机关兽”作品，保存两个不可变版本。
5. 学习卡关联《百炼识物诀》，来源谱记录本人绘制、AI 局部润色和授权素材。
6. 图片上传隔离区，完成安全检查和缩略图。
7. 作品提交审核、发布或退回；退回后创建新版本再次提交。
8. 行囊一次返回资料、本周修炼、秘籍状态、错题状态和作品审核状态。

这条链可以同时验证学习可信性、错题闭环、作品版本、来源谱、媒体安全、审核状态机和行囊聚合，是比“先把所有空接口铺完”更有效的实施顺序。
