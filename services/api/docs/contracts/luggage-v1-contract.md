# 行囊与学习契约 v1

状态：契约已冻结；阶段 1-2 的资料、目录、试炼、学习进度、统计、错题和聚合已实现。  
基线日期：2026-09-01。

本文件约束第一条《百炼识物诀》纵向链。Pydantic 源码位于：

- `app/domains/learning/contracts.py`
- `app/domains/learning/rules.py`
- `app/domains/luggage/contracts.py`

## 1. 不可信客户端边界

客户端只能提交试炼版本、预测、答案、解释和服务端签发的重练上下文。

客户端不得提交或修改：

- `score`
- `passed`
- `progress_state`
- `evidence_count`
- 悟性、匠心、侠义数值
- 错题巩固状态
- 审核状态

所有契约使用 `extra="forbid"`。出现未声明字段时返回 422，不能静默忽略伪造字段。

## 2. 秘籍状态

内部状态固定为：

1. `UNSEEN`：未闻，尚未获得。
2. `DISCOVERED`：偶得，完成漫画与有效预测。
3. `LEARNED`：习得，服务端确认试炼通过。
4. `MASTERED`：悟得，迁移证据通过。
5. `TEACHING`：传习，结构化评招通过。

晋级只能逐级发生。重复事件、倒序事件和缺少前置条件的越级事件都是幂等 no-op。失败试炼产生 `TRIAL_GRADED`，可计为有效修炼但不晋级；通过试炼额外产生 `TRIAL_PASSED`，用于推进到习得。

## 3. 成长证据

证据类别固定为：

- `WISDOM`：悟性。
- `CRAFT`：匠心。
- `CHIVALRY`：侠义。

证据验证状态固定为 `VALID`、`PENDING_REVIEW`、`REVOKED`。只有 `VALID` 证据进入行囊统计。

## 4. 有效修炼和周区间

以下事件可使修炼会话成为有效会话：

- `PREDICTION_COMPLETED`
- `TRIAL_GRADED`
- `TRIAL_PASSED`
- `TRANSFER_EVIDENCE_APPROVED`
- `STRUCTURED_REVIEW_ACCEPTED`
- `PROJECT_REVISION_COMPLETED`

打开漫画和完成媒体上传不计为修炼。

周区间使用 `Asia/Shanghai`，从本周一 00:00 到下周一 00:00，内部和 API 都采用左闭右开的 UTC 区间：`starts_at <= occurred_at < ends_at_exclusive`。

## 5. 试炼提交契约

实现路径：`POST /v1/trials/{trial_id}/attempts`。

请求体 `TrialAttemptCreate`：

- `trial_version_id`：必填 UUID。
- `prediction_payload`：可空 JSON。
- `answer_payload`：必填且不能为 null 的 JSON。
- `explanation`：可空，最大 1000 字符，服务端去除首尾空白。
- `remediation_context_id`：可空 UUID，只能引用当前用户的有效重练上下文。
- `client_request_id`：可空，8-64 字符，用于客户端排查。

HTTP 请求还必须携带 `Idempotency-Key`。幂等约束最终由 `(user_id, idempotency_key)` 数据库唯一键保证。

成功响应 `TrialAttemptAccepted` 由服务端填写：尝试 ID、试炼版本、分数、最大分、通过结果、反馈原因码、进度变化、证据奖励、错题和处理时间。

响应模型会拒绝以下不一致：

- `score > max_score`。
- `passed` 与 `result` 不一致。
- 已通过的尝试同时生成错题。

## 6. 行囊聚合契约

实现路径：`GET /v1/me/luggage`，支持 `If-None-Match` 和 304。

`LuggageResponse` 固定包含：

- `profile`：昵称、短时头像、年龄层、班级标签、匿名 ID、当前称号、勋章。
- `stats`：本周区间、本周有效修炼次数、累计有效修炼次数、累计修炼自然日、不同试炼
  通关数、三类证据计数和服务端生成的展示摘要。
- `manuals`：总数、已获得数、五态计数、最近秘籍、明确空状态和详情路径。
- `mistakes`：待巩固数、最近错题、重练路径、明确空状态和详情路径。
- `creations`：状态计数、最近作品、版本、缩略图、退回原因和详情路径。
- `privacy`：监护设置是否生效、待处理申诉数和隐私入口。
- `meta`：生成时间、投影版本和 ETag。

秘籍五态计数必须完整且总和等于 50 页内容总数；`obtained` 必须等于总数减 `UNSEEN`。错题、秘籍和作品为空时必须返回对应 `empty_reason`，不能只返回空标题。

最近秘籍返回稳定 `style_no`（1-5），客户端只据此选择书封样式，不根据标题猜测。
全局学习证据使用 `GET /v1/me/learning-evidence`，支持类别、本周范围、游标和分页大小；
证据摘要仍完全由学习事件计算，客户端没有写入成长数值的接口。

## 7. 空状态

固定空状态原因：

- `NO_OBTAINED_MANUALS`
- `NO_MISTAKES`
- `NO_CREATIONS`

无数据返回 200 和空列表。`empty_reason` 用于前端选择儿童易懂文案，不用于表示网络错误。

## 8. 作品首屏状态

行囊首屏状态固定为：

- `DRAFT`
- `PENDING_CHECK`
- `PENDING_HUMAN_REVIEW`
- `PUBLISHED`
- `RETURNED`
- `RESTRICTED`
- `WITHDRAWN`

这只是行囊展示状态。后续数据库仍会分别保存项目、版本、媒体、Publication 和 ModerationCase 的事实状态。

## 9. 当前自动化门禁

`tests/test_learning_luggage_contracts.py` 已覆盖：

- 客户端伪造五类派生字段全部被拒绝。
- 试炼输入归一化和 null 答案拒绝。
- JSON Schema 不包含任何服务端派生输入字段。
- 秘籍只能逐级晋级，失败和越级不会晋级。
- 打开页面和上传媒体不计修炼。
- 上海时区周区间计算正确。
- 行囊一致快照可验证。
- 不一致秘籍计数和缺失空状态会被拒绝。

`tests/test_learning_api.py` 与 `tests/test_mistakes_api.py` 另覆盖：

- 判分配置和正确答案不会通过试炼详情泄露。
- 正确尝试同事务写入事件、偶得/习得变化、悟性证据、统计和勋章。
- 空预测不能晋级，失败判分只计修炼、不伪造通过证据。
- 相同幂等请求不重复计数，相同键不同请求会冲突。
- 学习历史能解释每次晋级的触发事件、规则版本与证据。
- 错题只能由失败尝试生成，并按用户隔离。
- 重练上下文一次性使用，首次通过进入巩固中，到期再次通过才巩固。

## 10. 当前实现与下一步

`0003_profiles_and_taxonomy` 已建立用户资料、匿名 ID、称号、勋章、十卷五十页目录和收藏；`GET /v1/me/luggage` 已返回真实资料、50 页未闻计数以及统计、错题和作品的明确空状态。

`0004_learning_events_and_progress` 已建立版本化试炼、服务端判分、幂等尝试、修炼会话、不可变事件、成长证据、进度变化记录和统计投影。`0005_mistakes_and_remediation` 已建立错题聚合、一次性重练上下文、重练记录和延迟巩固规则。行囊已读取真实学习与错题投影。

当前只为第 1 页配置了一个可运行的纵向试炼样例；其余 49 页等待内容团队按同一版本化结构补齐，不使用占位正确答案。

`0006_creations_and_provenance` 已建立作品项目、不可变版本、学习卡、人机分工、素材授权、AIGC 声明、修改记录和幂等提交。提交时锁定学习卡与来源谱；缺项会返回可定位到字段的明细。行囊已返回真实作品状态计数与最近作品。

`0007_media_moderation_privacy` 已完成隔离上传、MinIO 适配、真实类型/哈希/病毒/解码/像素检查、去元数据重编码、缩略图、outbox Worker、人工复核、退回、申诉、发布、撤回和隐私设置。行囊会返回短时缩略图 URL 和真实待处理申诉数。

阶段 5 已完成后端部分：聚合结果使用短时 Redis 缓存，事务提交后只失效受影响用户；目录、称号或勋章定义变化时执行全量行囊缓存失效。缓存不可用会自动回源数据库，不能改变业务事务结果。作品缩略图改为批量读取，数据库查询数不随作品数量增长；ETag 由真实客户端可见投影计算，媒体转为可用或目录文案变化后也会改变。

`0008_luggage_ui_contract` 增加累计修炼自然日投影，并扩展行囊展示摘要、秘籍封面样式号和
全局学习证据接口。Android 已接入 DTO、Repository、ViewModel、ETag、空/错/加载状态和
所有行囊详情入口，具体见 `docs/android-luggage-integration.md`。
