# 修炼页接口契约 v1

状态：已实现，前端可直接对接。

## 1. 悟书环概览

`GET /v1/learning/overview`

返回 50 个秘籍的当前状态、证据数量、待温习标识，以及后山下一步推荐。
状态由服务端学习事件计算，客户端不能提交或覆盖。

```json
{
  "recommended_lesson_id": "uuid",
  "books": [
    {
      "manual_page_id": "uuid",
      "page_no": 1,
      "style_no": 1,
      "title": "会动未必会思",
      "volume_no": 1,
      "volume_title": "卷一《识机真诀》",
      "state": "UNSEEN",
      "state_label": "未闻",
      "evidence_count": 0,
      "review_due": false,
      "updated_at": null
    }
  ],
  "back_mountain": {
    "volume_no": 1,
    "lesson_id": "uuid",
    "reason": "沿主线学习下一招，完成漫画、预测与试炼",
    "available": true
  }
}
```

## 2. 问题召回

`GET /v1/learning/route?q=AI%20为什么会说错话`

第一版使用审核目录中的可解释关键词和主题别名，不访问外部网页。每个结果包含匹配来源和推荐理由，后续可替换为向量检索而不改变响应结构。
`prerequisites` 返回真实的前置秘籍 ID；`available=false` 时仍可展示推荐，但客户端应先引导用户完成前置招式。

## 3. 秘籍详情兼容入口

`GET /v1/lessons/{lesson_id}` 与 `GET /v1/manuals/{manual_page_id}` 返回相同内容，便于前端使用 lesson 术语而不影响现有客户端。

## 4. 状态边界

有效状态为 `UNSEEN`、`DISCOVERED`、`LEARNED`、`MASTERED`、`TEACHING`。状态只能由阅读/预测、试炼、迁移和结构化评招事件逐级推进；长期未学习不会降级。

## 5. 对接规则

- 客户端使用 `recommended_lesson_id` 和 `nextActions` 决定下一步入口。
- 客户端不得根据标题猜测卷、样式或状态。
- 所有写请求继续使用现有 `Idempotency-Key` 规则。
- 错误响应沿用统一 `{ "error": { "code", "message" } }` 结构。

## 6. 阅读与迁移证据

`POST /v1/lessons/{lesson_id}/read-events` 只记录漫画阅读事实，不推进秘籍状态；请求必须携带 `Idempotency-Key`。

`POST /v1/lessons/{lesson_id}/migration-evidence` 用于提交作品版本、使用的秘籍和修改理由。证据初始为 `PENDING_REVIEW`，只有内部审核接口 `POST /v1/internal/learning/evidence/{evidence_id}/approve` 批准后，才会产生 `TRANSFER_EVIDENCE_APPROVED` 事件并把状态从 `LEARNED` 推进到 `MASTERED`。

`POST /v1/lessons/{lesson_id}/teaching-evidence` 用于提交结构化讲解、应用示例和学习者反馈。服务端要求当前状态至少为 `MASTERED`，写入 `STRUCTURED_REVIEW_ACCEPTED` 事件和有效侠义证据，并将状态推进到 `TEACHING`；接口同样使用 `Idempotency-Key`，重复请求不会重复晋级或重复计数。

## 7. 试炼内容清单

运行 `python scripts/validate_learning_content.py` 会同时校验 10 卷 50 页目录和试炼清单。清单要求 50 页都有试炼元数据；只有具备明确判分器和判分配置的条目才能标记为 `ACTIVE`，未完成内容保持 `DRAFT`，不会进入可作答接口。
