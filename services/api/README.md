# 机巧江湖 API

当前已实现账户与设置基础能力，以及行囊阶段 1-5：用户资料、十卷五十页目录、收藏、版本化试炼、学习证据、秘籍进度、修炼统计、错题重练、作品不可变版本、学习卡、来源谱、隔离上传、媒体安全处理、缩略图、审核、申诉、撤回、隐私设置和真实行囊聚合。行囊聚合使用 30 秒 Redis 短缓存，领域事务提交后按用户精确失效；Redis 故障时自动回源 PostgreSQL。

## 本地运行

1. 将 `.env.example` 复制为 `.env`，并替换其中的密钥。
2. 在项目根目录启动 PostgreSQL、Redis、MinIO、ClamAV、API 与 Worker：

   ```powershell
   docker compose -f infra/docker-compose.yml up -d
   ```

   本地映射端口为 PostgreSQL `55432`、Redis `16379`、MinIO `19000/19001`、API `8010`，用于避开其他开发项目常用的默认端口。宿主机测试的 `MINIO_PUBLIC_ENDPOINT` 使用 `127.0.0.1:19000`；Android 模拟器改为 `10.0.2.2:19000`，服务端内部地址仍为 `minio:9000`。

3. 创建虚拟环境并安装依赖：

   ```powershell
   cd services/api
   py -3.13 -m venv .venv
   .\.venv\Scripts\python -m pip install -e ".[dev]"
   ```

4. 执行迁移并启动：

   ```powershell
   .\.venv\Scripts\alembic upgrade head
   .\.venv\Scripts\python -m app.domains.catalog.seed
   .\.venv\Scripts\uvicorn app.main:app --reload --port 8010
   ```

开发环境固定验证码默认为 `123456`，不会通过接口返回，也不会写入日志。生产环境会拒绝以固定验证码或 `noop` 短信提供方启动。

## 测试

```powershell
.\.venv\Scripts\python -m pytest --cov=app --cov-report=term-missing
```

测试使用内存 SQLite、内存验证码仓库与限流器，不依赖外部服务。

运行中的本地栈可执行有界负载门禁（令牌必须来自专用测试账户）：

```powershell
.\.venv\Scripts\python scripts\load_luggage.py --token <ACCESS_TOKEN> --requests 200 --concurrency 20
```

脚本默认要求全部响应为 200 且 p95 不超过 300 ms；增加 `--conditional` 可单独验证 ETag/304 热路径。缓存 TTL 必须短于媒体签名 URL 有效期，启动配置会校验这一约束。

## 主要路由

- `POST /v1/auth/verification-codes`
- `POST /v1/auth/guardian-consents/verify`
- `POST /v1/auth/register`
- `POST /v1/auth/login/password`
- `POST /v1/auth/token/refresh`
- `POST /v1/auth/password/reset`
- `POST /v1/auth/logout`
- `POST /v1/auth/logout-all`
- `GET /v1/auth/me`
- `GET|PATCH /v1/settings/preferences`
- `GET|PATCH /v1/settings/guardian-controls`
- `GET|POST|DELETE /v1/settings/blacklist`
- `POST /v1/support/feedback`
- `GET|DELETE /v1/account/sessions`
- `GET /v1/account/export`
- `GET|POST /v1/account/data-rights-requests`
- `GET|PATCH /v1/profile`
- `GET /v1/profile/titles`
- `GET /v1/profile/badges`
- `GET /v1/manuals`
- `GET /v1/manuals/{manual_page_id}`
- `PUT|DELETE /v1/manuals/{manual_page_id}/favorite`
- `GET /v1/me/luggage`
- `GET /v1/meta/capabilities`
- `GET /v1/trials/{trial_id}`
- `POST /v1/trials/{trial_id}/attempts`
- `GET /v1/me/learning-stats`
- `GET /v1/manuals/{manual_page_id}/evidence`
- `GET /v1/manuals/{manual_page_id}/learning-history`
- `GET /v1/mistakes`
- `GET /v1/mistakes/{mistake_id}`
- `POST /v1/mistakes/{mistake_id}/retry-sessions`
- `POST /v1/creation-projects`
- `GET /v1/me/creation-projects`
- `GET|PATCH /v1/creation-projects/{project_id}`
- `DELETE /v1/creation-projects/{project_id}`
- `GET|POST /v1/creation-projects/{project_id}/versions`
- `GET /v1/creation-projects/{project_id}/change-logs`
- `GET /v1/creation-versions/{version_id}`
- `GET|PUT /v1/creation-versions/{version_id}/learning-card`
- `GET|PUT /v1/creation-versions/{version_id}/provenance-manifest`
- `POST /v1/creation-projects/{project_id}/submissions`
- `POST /v1/uploads/intents`
- `POST /v1/uploads/{upload_id}/complete`
- `GET|DELETE /v1/media-assets/{asset_id}`
- `GET /v1/publications/{publication_id}/moderation-case`
- `POST /v1/publications/{publication_id}/withdraw`
- `GET /v1/moderation-cases/{case_id}`
- `POST /v1/moderation-cases/{case_id}/appeals`
- `GET /v1/me/moderation-appeals`
- `GET|PATCH /v1/me/privacy-settings`

前端字段、成功响应、错误码和未满 14 周岁注册分支见
[`docs/auth-integration.md`](docs/auth-integration.md)；设置页当前接入状态、字段和后续页面接入方式见
[`docs/settings-integration.md`](docs/settings-integration.md)。行囊与学习的第一版冻结契约见
[`docs/contracts/luggage-v1-contract.md`](docs/contracts/luggage-v1-contract.md)，完整实施方案见
[`docs/luggage-backend-implementation-plan.md`](docs/luggage-backend-implementation-plan.md)。阶段 1
接口字段和前端预留接入方式见
[`docs/profile-manual-integration.md`](docs/profile-manual-integration.md)。
试炼、学习证据、真实统计和错题重练接入见
[`docs/learning-mistakes-integration.md`](docs/learning-mistakes-integration.md)。
作品版本、学习卡、来源谱和提交审核接入见
[`docs/creation-provenance-integration.md`](docs/creation-provenance-integration.md)。
媒体、审核、申诉、隐私和部署接入见
[`docs/media-moderation-privacy-integration.md`](docs/media-moderation-privacy-integration.md)。
阶段 5 的缓存、失效、负载门禁和 Android 预留接入见
[`docs/luggage-stage5-integration.md`](docs/luggage-stage5-integration.md) 与
[`docs/android-luggage-integration.md`](docs/android-luggage-integration.md)。
