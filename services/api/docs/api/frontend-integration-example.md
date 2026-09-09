# 前端对接顺序示例

前端只需要按以下顺序调用后端，页面状态由响应驱动：

1. `GET /v1/learning/overview`：绘制悟书环和后山推荐。`books=[]` 是新用户空态，`review_due=true` 只显示复习提示。
2. `GET /v1/lessons/{lesson_id}`：打开漫画详情。打开动作完成后调用 `POST /v1/lessons/{lesson_id}/read-events`，携带稳定的 `Idempotency-Key`；该事件不会直接把状态推进到“习得”。
3. `POST /v1/trials/{trial_id}/attempts`：提交预测和答案。根据 `result` 显示成功或错误反馈；失败响应里的 `mistake` 可直接进入错题入口。
4. `POST /v1/lessons/{lesson_id}/migration-evidence`：提交真实创作中的迁移证据。响应为 `PENDING_REVIEW` 时显示“待审核”，不要在客户端提前把状态改成“悟得”。
5. 审核通过后再次 `GET /v1/learning/overview`：以后端返回的 `state` 和 `recommended_lesson_id` 刷新地图。
6. 当页面需要验证“传习”时，调用 `POST /v1/lessons/{lesson_id}/teaching-evidence`，提交结构化讲解、应用示例和学习者反馈。该接口只接受 `MASTERED`（或重复提交时的 `TEACHING`）秘籍，服务端成功后推进到 `TEACHING`。

所有写请求都应复用同一个业务操作的幂等键；网络重试不会重复产生事件或证据。网络错误通过 TypeScript 客户端的 `LearningApiError` 统一处理，HTTP 4xx/5xx 不应被当作学习状态。
