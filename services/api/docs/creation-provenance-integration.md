# 作品、版本、学习卡与来源谱接入说明

本阶段后端已经可独立使用；前端暂时没有入口时不影响部署。数据库迁移为 `0006_creations_and_provenance`，后续 `0007_media_moderation_privacy` 已把真实媒体与审核链接入，因此能力开关现在返回 `creations=true`、`media_uploads=true`。

## 1. 已实现边界

- 作品项目保存标题、说明、作品类型、默认可见范围和当前版本号。
- 项目元数据可修改并使用 `row_version` 乐观锁；其他用户访问统一返回 404。
- 每次保存生成一个不可变 `CreationVersion`，包含父版本、画布、图层快照、修改摘要和修改原因。没有版本覆盖接口。
- 每个版本分别保存学习卡和来源谱；提交前可反复修订，提交后永久锁定。
- 提交记录固定引用一个版本，初始状态为 `PENDING_CHECK`，并使用 `Idempotency-Key` 防止网络重试造成重复提交。
- 行囊返回真实作品状态计数和最近三个已有版本的作品；缩略图在媒体阶段完成前为 `null`。

提交时现在会校验所有资产 UUID 是否属于本人且处于 `READY`；处理中的文件会返回 `MEDIA_NOT_READY`，不存在或越权引用会返回 `MEDIA_ASSET_INVALID`。`PENDING_CHECK` 由 outbox Worker 安全转入人工复核或后续审核决定。

## 2. 推荐前端调用顺序

1. `POST /v1/creation-projects` 创建项目。
2. `POST /v1/creation-projects/{project_id}/versions` 保存首版；修订版必须把当前版本 ID 作为 `parent_version_id`。
3. `PUT /v1/creation-versions/{version_id}/learning-card` 保存学习卡。
4. `PUT /v1/creation-versions/{version_id}/provenance-manifest` 保存人机分工、素材授权与 AIGC 声明。
5. `POST /v1/creation-projects/{project_id}/submissions` 提交当前版本。
6. 从 `GET /v1/me/creation-projects`、项目详情或 `GET /v1/me/luggage` 刷新状态。

学习卡或来源谱第一次保存时不传 `row_version`；再次保存时必须回传上次响应的 `row_version`。项目修改同样必须回传项目的 `row_version`。

## 3. 图层快照

`layers` 为 1-200 项，`layer_id` 和 `z_index` 在一个版本内不能重复。支持：

- `TEXT`：必须提供非空 `text_content`。
- `DRAWING`、`IMAGE`、`REFERENCE`：必须提供 `asset_id`。
- `AI_GENERATED`：必须同时提供 `asset_id` 和 `aigc=true`。

示例：

```json
{
  "parent_version_id": null,
  "layers": [
    {
      "layer_id": "title-1",
      "kind": "TEXT",
      "name": "题字",
      "z_index": 0,
      "visible": true,
      "text_content": "竹影机关"
    }
  ],
  "canvas_width": 1080,
  "canvas_height": 1440,
  "change_summary": "完成首版构图"
}
```

父版本不是当前版本时返回 `STALE_VERSION_PARENT`，前端应重新获取项目和版本列表，不应静默覆盖。

## 4. 学习卡完整条件

学习卡至少关联一本已开放秘籍，填写创作方法，并将 `questions_confirmed` 设为 `true`。`unresolved_questions` 可以为空；其含义是“已确认目前没有未解决问题”，不是跳过确认。

未满足条件时仍可保存，响应状态为 `DRAFT`；全部满足后为 `COMPLETE`；提交成功后为 `LOCKED`。

## 5. 来源谱完整条件

- 必须填写本人贡献摘要，并至少有一个 `HUMAN_CONTRIBUTION` 项。
- 本人贡献的授权类型只能是 `ORIGINAL` 或 `NOT_APPLICABLE`。
- 外部素材必须填写来源，并选择明确授权；`AUTHORIZED` 还必须关联授权证明资产 ID。
- `unresolved_rights=true` 时不能提交。
- 作品含 AI 图层或 `ai_assistance_used=true` 时，必须填写 AI 贡献摘要、确认 AIGC 标识，并至少保存一条 `AI_CONTRIBUTION`。
- 每条 AI 贡献必须填写提供方、模型、工具动作和提示词摘要，授权类型使用 `NOT_APPLICABLE`。

来源谱可保存不完整草稿；响应状态会保持 `DRAFT`。这样前端可以在上传证明或补充素材信息前安全退出。

## 6. 提交与错误处理

提交请求必须带 8-64 位 `Idempotency-Key`。相同键和相同请求可安全重试并返回同一发布记录；相同键换了版本或可见范围会返回 `IDEMPOTENCY_KEY_REUSED`。

资料不完整时返回 409：

```json
{
  "error": {
    "code": "SUBMISSION_INCOMPLETE",
    "message": "作品资料尚未完整，暂不能提交审核",
    "request_id": "...",
    "details": [
      {
        "code": "AIGC_LABEL_REQUIRED",
        "field": "provenance.aigc_label_declared",
        "message": "使用 AI 时必须确认 AIGC 标识"
      }
    ]
  }
}
```

前端应按 `details[].field` 定位到具体表单，不要只显示通用失败提示。常见冲突还包括：

- `VERSION_CONFLICT`：刷新项目、学习卡或来源谱后重试。
- `VERSION_ALREADY_SUBMITTED`：该版本已有提交，刷新项目状态。
- `STALE_CREATION_VERSION`：只能提交当前版本。
- `LEARNING_CARD_LOCKED` / `PROVENANCE_LOCKED`：已提交证明不可修改，应创建新作品版本。
- `MANUAL_REFERENCE_INVALID`：学习卡引用的秘籍不存在或未开放。

## 7. 查询接口

- `GET /v1/me/creation-projects?status=&cursor=&limit=`：本人作品分页列表。
- `GET /v1/creation-projects/{id}`：项目和最近提交状态。
- `GET /v1/creation-projects/{id}/versions`：全部不可变版本，按版本号倒序。
- `GET /v1/creation-versions/{id}`：单个版本快照。
- `GET /v1/creation-versions/{id}/learning-card`：学习卡。
- `GET /v1/creation-versions/{id}/provenance-manifest`：来源谱及来源项。
- `GET /v1/creation-projects/{id}/change-logs`：项目、版本、学习卡、来源谱和提交修改记录。

项目列表中的 `display_status` 为 `DRAFT` 或最近一次提交状态。完整发布状态集为 `PENDING_CHECK`、`PENDING_HUMAN_REVIEW`、`PUBLISHED`、`RETURNED`、`RESTRICTED`、`WITHDRAWN`；后五种状态的流转、退回原因写入和撤回接口将在审核阶段实现。
