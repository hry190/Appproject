# 行囊阶段 5：缓存、失效与质量门禁

基线日期：2026-09-01  
范围：后端缓存优化、Android 聚合接入与详情入口实现。

## 1. 已实现结果

`GET /v1/me/luggage` 仍返回冻结的 v1 契约，但执行路径改为：

1. JWT 鉴权并确认用户仍有效。
2. 按用户 UUID 读取 Redis 完整快照。
3. 命中时直接返回快照；`If-None-Match` 相同则返回 304。
4. 未命中时从 PostgreSQL 读取资料、统计、秘籍、错题、作品和申诉。
5. 批量读取最近作品当前版本与缩略图，不逐作品查询。
6. 由实际客户端可见 `data` 计算 ETag，写入短缓存并返回。

默认缓存键为 `jianghu:luggage:v1:<user_id>`，TTL 为 30 秒，可通过
`JIANGHU_LUGGAGE_CACHE_TTL_SECONDS` 配置为 5-120 秒。配置必须短于媒体签名 URL
有效期；否则应用拒绝启动，避免返回已过期的图片地址。

Redis 只保存可重建的私人响应副本，不是学习事实来源。Redis 读取、写入或删除失败
都自动回源/继续提交，不能使资料修改、试炼提交、作品审核等业务事务失败。

## 2. 提交后失效

SQLAlchemy 在 flush 前收集受影响用户，在事务成功提交后删除对应缓存键；回滚时丢弃
待失效集合。当前覆盖：

- 用户、资料、称号、勋章和监护控制。
- 修炼会话、尝试、事件、证据、秘籍进度、统计和错题。
- 作品项目、版本、发布状态。
- 上传会话、媒体资产、审核案件、申诉和隐私设置。

秘籍目录、称号定义或勋章定义属于共享配置，变更后使用 Redis `SCAN` 分批清理所有
行囊键。Worker 使用同一 Redis 缓存适配器，因此媒体从隔离中变为 `READY`、审核状态
变化或删除任务完成后也会失效。

若某条新领域事实未来会出现在行囊中，必须同时把对应表和用户字段加入
`app/db.py` 的失效白名单；短 TTL 只是漏配时的安全兜底。

## 3. 查询边界和 ETag

热缓存请求只执行一次用户鉴权 SELECT，不再执行聚合查询。冷请求中，最近三个作品的
当前版本使用一条复合键查询，头像与作品 320px 缩略图使用一条批量查询；测试保证
作品从 1 件增长到 12 件时 SELECT 数不增长。

ETag 不再只拼接少量行版本，而是哈希真实 `data` 投影。因此以下变化都会产生新 ETag：

- 昵称、资料、统计、秘籍、错题或作品状态变化。
- 媒体从处理中变为可用，或缩略图被删除。
- 最近秘籍标题、当前称号名称或勋章名称变化。

`generated_at` 不参与 ETag；业务数据相同但缓存自然过期时，客户端仍可获得 304。

## 4. 自动化门禁

`tests/test_luggage_cache.py` 覆盖：

- 冷请求后写缓存，热请求只有一次鉴权查询。
- 缓存命中时 ETag 与 `generated_at` 保持一致，条件请求返回 304。
- 修改用户 A 只失效 A，用户 B 的快照继续命中。
- 缓存读、写和提交后删除全部抛错时，读取与已提交写入仍成功。
- Redis 连接异常时 Redis 适配器 fail-open。
- 1 件与 12 件作品的冷聚合 SELECT 数相同。

完整回归：

```powershell
.\.venv\Scripts\python -m pytest --cov=app --cov-report=term-missing
```

本地 Docker 栈负载门禁：

```powershell
.\.venv\Scripts\python scripts\load_luggage.py `
  --base-url http://127.0.0.1:8010 `
  --token <专用测试账号访问令牌> `
  --requests 200 `
  --concurrency 20 `
  --p95-budget-ms 300
```

增加 `--conditional` 会先预热并携带 ETag，要求后续全部返回 304。脚本输出状态分布、
p50、p95、平均耗时和吞吐量；出现网络错误、非预期状态或 p95 超预算时以非零状态退出。

300 ms 是运行环境验收门槛，不能用内存 SQLite 单元测试的耗时代替。正式演示前应在
与部署同规格的 PostgreSQL、Redis、MinIO 和 API 双 Worker 环境运行，并保存结果。

## 5. 运维检查

- API 与媒体 Worker 必须指向相同 `JIANGHU_REDIS_URL` 和 `JIANGHU_REDIS_PREFIX`。
- Redis 可以重启或清空；只会造成短暂缓存未命中，不会丢失学习事实。
- 不记录缓存 JSON、访问令牌或签名 URL 查询参数。
- Redis 内存策略应允许这些短 TTL 键被淘汰；不要为行囊缓存启用永久保留。
- 发布前同时验证 200 热路径与 304 条件路径，不能只测其中一种。

Android 已完成接入，字段、页面状态、接口与验收记录见
`docs/android-luggage-integration.md`。

## 6. 2026-09-01 本地完整验收记录

验收环境：Docker Desktop，PostgreSQL 16、Redis 7、MinIO、ClamAV、两个 Uvicorn API
Worker 和一个 outbox Worker，接口地址 `http://127.0.0.1:8010`。

- PostgreSQL 从已有 0002 成功升级到 `0008_luggage_ui_contract`。
- 独立临时 PostgreSQL 数据库完成 `base -> head -> base -> head`，随后已删除。
- 发现并修复 Alembic 默认 `version_num VARCHAR(32)` 无法保存长 revision ID 的问题。
- Compose API 健康检查使用 `/healthz`；Worker 等待 API、MinIO、ClamAV 健康后启动。
- 真实账号验证 200、304、30 秒 Redis TTL、资料提交后精确失效和 ETag 更新。
- 真实 PNG 完成预签名 PUT、HEAD、SHA-256、文件签名、ClamAV、Pillow 解码、像素检查、
  元数据移除、320/640 缩略图和头像签名下载；最终状态 `READY`。
- 发现并修复预签名 URL 返回容器内部 `minio:9000` 的问题，拆分内部与公开端点；公开
  签名下载返回 200、`image/png`。

负载结果（各 200 请求，并发 20）：

- 200 热缓存路径：p50 64.3 ms，p95 133.1 ms，平均 71.7 ms，259.0 req/s。
- 304 条件路径：p50 62.5 ms，p95 78.1 ms，平均 62.6 ms，295.3 req/s。

两组均为零错误并通过 300 ms p95 门禁。Redis 断连的 fail-open 行为由自动化故障注入
测试覆盖；学习事实仍只存储在 PostgreSQL。
