# 大会后端接入说明

大会接口已经独立可用，页面尚未接入时不影响部署。所有接口都要求 `Authorization: Bearer <access_token>`；客户端先请求 `GET /v1/meta/capabilities`，仅在 `conference=true` 时显示大会入口。

## 1. 作品流

`GET /v1/conference/feed?cursor=` 返回已发布的社区作品，响应沿用 `PublicationFeedPagePublic` 的 `items` 与 `next_cursor`。每页固定最多 5 项，即使传入 `limit` 也不会放大；继续翻页只传回上次的 `next_cursor`。

作品流自动排除已撤回、非社区可见、被任一方拉黑的作品。`preview_url` 是短时效签名地址，过期后重新请求作品流，不应在客户端长期缓存。`is_owner=true` 表示当前登录用户是该作品作者，客户端据此显示评语处理入口。

每个作品卡同时返回 `version_number`、`related_manuals`、`learning_card` 和 `provenance`。`related_manuals` 包含关联秘籍页 ID、页码和标题；作者允许公开学习卡时，`learning_card` 返回方法总结和未解决问题，否则为 `null`，旧字段 `learning_summary` 继续兼容。`provenance` 返回人的贡献、AI 协助内容、AIGC 声明及来源条目数量，不返回可能包含外部地址的完整来源明细；旧字段 `ai_assisted` 继续兼容。

`GET /v1/conference/publications/{publication_id}` 返回单件仍可见的社区作品，用于作品详情路由恢复；不可见作品统一返回 `COMMUNITY_WORK_NOT_FOUND`。

## 2. 评语与收藏

评语创建接口为 `POST /v1/conference/publications/{publication_id}/reviews`：

```json
{
  "template": "OBSERVATION",
  "content": "我看到了齿轮的传动关系。"
}
```

`template` 可为 `OBSERVATION`、`LEARNING`、`SUGGESTION`、`QUESTION`；`EVIDENCE` 为旧版“依据”模板，继续兼容已有客户端。同一用户对同一作品每种模板只能提交一次，且不能评价自己的作品。通过 `GET` 同路径取得仍可见的评语。

作品作者使用 `POST /v1/conference/reviews/{review_id}/decision` 处理待处理评语：

```json
{
  "action": "REPLY",
  "reply": "你观察得很准确。",
  "row_version": 1
}
```

`action` 为 `ACCEPT`、`THINK` 或 `REPLY`；只有 `REPLY` 能带 `reply`。响应状态依次为 `ACCEPTED`、`THINKING`、`REPLIED`。处理必须回传当前 `row_version`，冲突时刷新评语再重试。

对于已经 `ACCEPTED` 的 `SUGGESTION`，作者在完成改稿并创建新作品版本后，调用 `POST /v1/conference/reviews/{review_id}/adoption`：

```json
{
  "creation_version_id": "<uuid>",
  "summary": "缩短传动轴，并重新验证了稳定性。",
  "row_version": 2
}
```

目标版本必须属于同一作品，且版本号晚于该评语对应的参会发布版本。成功后返回 `adopted_in_creation_version_id`、`adoption_summary`、`adopted_at` 和递增后的 `row_version`，同时写入审计并通知评语作者。同内容重试返回原结果，不重复写入；换版本再次关联返回冲突。

评语、作者回复、二创用途和授权处理备注会在写入前检查手机号、邮箱、外部链接与直接联系方式；命中时返回 `INTERACTION_TEXT_PRIVACY_RISK`。用户可通过 `POST /v1/conference/reviews/{review_id}/reports` 举报他人的可见评语，`reason` 可为 `PERSONAL_INFO`、`HARASSMENT`、`INAPPROPRIATE_CONTENT`、`OTHER`。同一用户重复举报同一评语时返回原记录。

内部审核员先使用 `GET /v1/internal/conference/review-reports?report_status=PENDING&limit=50` 拉取待处理举报，再使用 `POST /v1/internal/conference/review-reports/{report_id}/decision` 处理，并携带 `X-Internal-Token`。`decision` 为 `REMOVE` 或 `KEEP`，同时提交 `reviewer_reference`、`resolution_summary` 和当前 `row_version`；被移除的评语不再出现在公开列表。

`PUT /v1/conference/collections/{publication_id}` 收藏公开作品，可重复调用；`DELETE` 取消收藏，可重复调用；`GET /v1/conference/me/collections` 返回本人仍可见的收藏。被撤回、隐藏或拉黑后，收藏记录不会再出现在列表中。

## 3. 同门改造授权

申请接口为 `POST /v1/conference/derivative-requests`：

```json
{
  "source_publication_id": "<uuid>",
  "requested_use": "用于重做机关结构练习"
}
```

申请人用 `GET /v1/conference/me/derivative-requests?scope=REQUESTED` 查询进度；原作者传 `scope=RECEIVED` 处理收到的申请。原作者通过 `POST /v1/conference/derivative-requests/{request_id}/decision` 批准或拒绝；拒绝必须说明 `note`。批准会返回一个状态为 `ACTIVE` 的 `authorization`，其 `id`、`source_creation_version_id` 和 `authorization_version` 是后续作品来源记录应保存的凭据。

原作者可调用 `POST /v1/conference/derivative-authorizations/{authorization_id}/revoke` 撤回有效授权。源作品被撤回、下架或删除时，相关有效授权也会自动变为 `REVOKED`；前端不得把已撤回授权用于新的改造作品。

## 4. 匿名切磋

`GET /v1/conference/match-queue` 获取当前状态。使用 `POST /v1/conference/match-queue` 并传入 `{ "manual_page_id": "<uuid>" }` 进入队列；服务仅按相同秘籍页与年龄段匹配，并排除任一方拉黑的用户。等待记录有效期为 5 分钟，响应中的 `expires_at` 供客户端显示和刷新等待状态。匹配响应只包含队列、匹配和秘籍 ID，不暴露对手昵称、手机号或个人资料。

匹配成功后，`GET /v1/conference/matches/{match_id}` 返回根据秘籍内容固化的 3 道题、本人答案以及双方完成进度。题目分别覆盖核心概念、公案分析与迁移应用；响应只显示对方完成数量，不显示身份或答案。使用 `POST /v1/conference/matches/{match_id}/answers` 逐题提交：

```json
{
  "question_id": "<uuid>",
  "answer": "我的方案",
  "reason": "我的判断依据"
}
```

答案与理由会做联系方式和外链检查。每人每题只能提交一次；同内容重试会返回原答案，不允许覆盖已经提交的内容。双方完成全部题目后，对局进入 `AWAITING_JUDGMENT`。

双方作答完成时，服务会在同一数据库事务中写入 `CONFERENCE_MATCH_JUDGMENT_REQUESTED` 任务。现有 `python -m app.domains.media.worker` 工作进程会抢占任务、调用评审器并写回双方 0–100 分总分、分项分、总结、优点及改进项。任务失败按指数退避重试，默认最多 5 次；处理进程异常退出后，超过默认 300 秒租约的任务可被其他进程重新领取。

`JIANGHU_CONFERENCE_JUDGE_PROVIDER=development` 使用明确标注的本地透明规则评分，只用于开发验收，不冒充模型判断。生产可配置 `webhook`，并提供 `JIANGHU_CONFERENCE_JUDGE_WEBHOOK_URL`、`JIANGHU_CONFERENCE_JUDGE_WEBHOOK_TOKEN` 和模型标识。发往网关的参与者只标记为 `A`、`B`，不会包含用户 ID、昵称、手机号或其他资料；网关响应必须包含 A、B 各一项，并通过分数范围、字段长度和结构校验。生产地址必须使用 HTTPS。

内部运维仍可携带 `X-Internal-Token`，通过 `GET /v1/internal/conference/matches/pending-judgment` 查看待评任务，或调用 `POST /v1/internal/conference/matches/{match_id}/judgment` 人工补写合法的结构化结果。评审完成后对局变为 `ENDED`，并计算 `WIN`、`LOSE` 或 `TIE`。

用户通过 `GET /v1/conference/matches/{match_id}/result` 查看本人视角结果；使用 `POST /v1/conference/matches/{match_id}/evaluations` 提交一次 `SELF` 自评或 `PEER` 互评，使用 `POST /v1/conference/matches/{match_id}/reflections` 保存“学到什么”和“下次改进”。内部教学服务可通过 `POST /v1/internal/conference/matches/{match_id}/teacher-evaluations` 写入一次师评。所有评价均保存分数、分项、总结、优点和改进项，并进入审计记录。

`DELETE /v1/conference/match-queue` 退出等待或结束当前切磋。对局内可调用 `POST /v1/conference/matches/{match_id}/reports` 举报：

```json
{
  "reason": "SAFETY_CONCERN",
  "details": "可选，最多 500 字"
}
```

`reason` 可为 `INAPPROPRIATE_CONTENT`、`HARASSMENT`、`SAFETY_CONCERN`、`OTHER`。同一用户对同一对局重复举报会返回已有记录，首次举报会立即结束该对局。

内部审核员使用 `GET /v1/internal/conference/match-reports?report_status=PENDING&limit=50` 拉取待处理举报，再调用 `POST /v1/internal/conference/match-reports/{report_id}/decision`。`decision` 为 `CONFIRM` 或 `DISMISS`，同时提交 `reviewer_reference`、`resolution_summary` 和当前 `row_version`。处理成功后状态为 `RESOLVED` 或 `DISMISSED`，并向举报者发送结案书信；确认举报本身不会自动封禁被举报账号。

## 5. 大会书信

`GET /v1/conference/letters?limit=50` 返回大会书信和总未读数，`PUT /v1/conference/letters/{letter_id}/read` 标记已读。新评语与作者回应、建议形成新版本、改造申请及处理结果、切磋开始、双方完成、AI 评审完成、互评、师评和举报结案都会生成书信。书信正文只写事件摘要，不复制评语、回答或举报内容；用户关闭“消息”或“作品动态”后不再生成新书信。

## 6. 账号导出

`GET /v1/account/export` 的 `conference_activity` 包含本人创建或收到的评语与采纳记录、收藏、改造申请及授权、切磋、本人作答、与本人相关的评价、本人复盘、排队历史、本人提交的举报和收到的书信。导出不包含匿名对手的用户 ID；收到的评语也不输出评语作者 ID。

账号注销仍通过现有异步数据权利申请进入待处理状态；当前代码库尚未实现删除/匿名化执行 Worker，客户端不能把申请已受理显示为数据已经物理删除。

## 7. 能力边界与错误

当前仍不包含 AI 自动评语裁定或匹配后的实时聊天，客户端不应假设这两项服务存在。大会的创建、处理、撤销、作答、评审、复盘和举报操作会写入领域审计事件，审计差异中不保存评语正文、答案正文或举报详情。全部错误继续使用通用 `{ "error": { "code", "message", "request_id" } }` 结构；参数错误为 422，权限不足为 403，资源不可见为 404，重复提交或版本冲突为 409。

本轮数据库迁移为 `0021_conference_feedback_closure`。完整写入检查、风险分级和上线建议见 [`conference-backend-review-report.md`](conference-backend-review-report.md)。
