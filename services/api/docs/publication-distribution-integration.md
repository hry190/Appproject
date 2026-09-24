# 作品发布与社区接入说明

作品发布只保留两种范围：私密保存和社区公开。班级、教师收件箱、监护人投递及相关管理入口已经取消；历史数据表继续保留，仅用于兼容既有记录。

## 发布链路

1. 保存到创作档案不会公开作品。
2. 用户在封卷页选择社区分类并点击“确认投稿”，调用 `POST /v1/creation-projects/{project_id}/submissions`，`visibility` 为 `COMMUNITY`。
3. 服务端检查作品说明、学习回顾、改进计划、素材状态、来源记录及必要的隐私确认；缺项通过 `SUBMISSION_INCOMPLETE` 的 `details` 返回，客户端应逐项展示。
4. 资料完整后进入审核状态机。审核通过的作品由 `GET /v1/community/feed` 和大会作品页展示。
5. 作者撤回作品或审核结论撤销后，社区流立即不再展示该作品。

## 接口与权限

- `POST /v1/creation-projects/{project_id}/submissions`：仅作品所有者可提交；可见范围只接受 `PRIVATE` 或 `COMMUNITY`。
- `GET /v1/community/feed?cursor=&limit=20`：返回仍在发布中的社区作品，并应用双向黑名单过滤。
- `GET /v1/conference/feed`：大会作品页使用的社区作品流。
- `POST /v1/publications/{publication_id}/withdraw`：作品所有者撤回。

社区投稿不受账号年龄段或历史监护字段限制。年龄段只用于适龄内容、安全提示与审核规则。AI 辅助作品按用户协议和投稿规范自动保留“AI 辅助”标识，无需在投稿页重复确认。

## 已取消接口

以下接口不再暴露，调用返回 404：

- `POST /v1/classrooms`
- `POST /v1/classrooms:join`
- `GET /v1/me/classrooms`
- `GET /v1/me/publication-inbox`
- `GET|PATCH /v1/settings/guardian-controls`
- `POST /v1/auth/guardian-consents/verify`
