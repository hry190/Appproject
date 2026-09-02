# 学习事件、试炼与错题接口（阶段 2）

基线日期：2026-09-01。Android 当前可以继续不接这些入口；后端会保留接口与数据结构，页面更新后直接联调。

## 1. 当前完成范围

后端通用能力已经完成：版本化试炼、服务端判分、幂等尝试、不可变学习事件、秘籍进度、悟性/匠心/侠义证据、有效修炼统计、错题聚合、重练和延迟巩固。

当前只为第 1 页“会动未必会思”配置了一个正式可运行的纵向试炼。其余 49 页没有伪造正确答案，后续只需增加版本化内容配置，不需要修改状态机或统计接口。

## 2. 获取与提交试炼

- `GET /v1/trials/{trial_id}`：返回题目、预测提示、答案 JSON Schema、分值和解释要求；不会返回判分配置或正确答案。
- `POST /v1/trials/{trial_id}/attempts`：提交预测、答案和解释。

提交必须携带 8-64 字符的 `Idempotency-Key`：

```http
Idempotency-Key: attempt-android-20260901-0001
```

```json
{
  "trial_version_id": "试炼详情返回的版本 UUID",
  "prediction_payload": {"key_feature": "能否从样本中改进"},
  "answer_payload": {"choice": "LEARNS_FROM_DATA"},
  "explanation": "机器学习系统能够根据样本经验改进后续判断。",
  "client_request_id": "android-learning-0001"
}
```

相同幂等键和相同请求会返回第一次结果，不会重复增加修炼、证据或错题。相同键提交不同内容返回 `IDEMPOTENCY_KEY_REUSED`。

客户端不能提交 `score`、`passed`、`progress_state`、悟性数值或错题状态，多余字段返回 422。

## 3. 服务端事务

一次新尝试在同一事务中完成：

1. 校验试炼和当前版本。
2. 使用版本锁定的判分规则计算分数和原因码。
3. 写入有效修炼会话与 `TRIAL_GRADED` 事件。
4. 有效预测产生 `PREDICTION_COMPLETED`，只允许 `UNSEEN -> DISCOVERED`。
5. 首次通过产生 `TRIAL_PASSED`、悟性证据和“初试锋芒”勋章，只允许 `DISCOVERED -> LEARNED`。
6. 更新累计修炼、不同试炼通关数和证据投影。
7. 失败时创建或更新错题。

空对象、空数组和空字符串不算有效预测。重复通过同一试炼可以计为一次新修炼，但不会反复刷“首次通过”证据、不同通关数或勋章。

## 4. 进度与学习证据

- `GET /v1/me/learning-stats`：本周有效修炼、累计修炼、不同试炼通关数和三类有效证据。
- `GET /v1/manuals?state=...`：按真实秘籍状态筛选。
- `GET /v1/manuals/{manual_page_id}`：状态、达成条件和最近证据。
- `GET /v1/manuals/{manual_page_id}/evidence`：完整证据列表。
- `GET /v1/manuals/{manual_page_id}/learning-history`：每次晋级的前态、后态、触发事件、规则版本、证据摘要和时间。

本周使用 `Asia/Shanghai` 周一 00:00 至下周一 00:00。登录、打开页面和媒体上传不计修炼；失败但完成判分的试炼属于有效修炼。

当前公开试炼只能推进至 `LEARNED/习得`。`MASTERED/悟得` 等待作品迁移证据审核，`TEACHING/传习` 等待结构化评招，客户端没有直接修改接口。

## 5. 错题与重练

- `GET /v1/mistakes?status=&manual_page_id=&cursor=&limit=`：本人错题列表。
- `GET /v1/mistakes/{mistake_id}`：原答案、错误原因、关联秘籍、重练记录和下次复习时间。
- `POST /v1/mistakes/{mistake_id}/retry-sessions`：生成有效 15 分钟且只能使用一次的重练上下文。

重练仍提交到统一试炼接口，并在请求中加入：

```json
{"remediation_context_id": "retry-sessions 返回的 UUID"}
```

错题状态由后端管理：

- `TO_REVIEW`：待巩固。
- 首次重练通过后为 `PRACTICING`，并安排至少 24 小时后的复习。
- 到期后再次通过才成为 `CONSOLIDATED`。

过期、已使用、属于其他用户或试炼不匹配的上下文统一返回 `REMEDIATION_CONTEXT_INVALID`。普通通过不能伪装成重练，也不能让前端直接勾选“已巩固”。

## 6. 行囊同步

`GET /v1/me/luggage` 现在读取真实学习和错题投影：本周修炼、累计修炼、通关数、三类证据、各秘籍状态、最近获得秘籍、待巩固错题与重练地址都会随事务提交立即更新。`ETag` 同时包含资料、学习和错题投影版本。

作品、来源谱、媒体和审核仍返回明确空状态，对应能力开关保持 `false`。
