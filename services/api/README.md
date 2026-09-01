# 机巧江湖 API

第一阶段实现手机号验证码、注册、密码登录、监护人同意、令牌刷新、退出登录和密码重置；设置模块已补充偏好同步、黑名单、反馈、登录设备、家长控制与数据权利接口。

## 本地运行

1. 将 `.env.example` 复制为 `.env`，并替换其中的密钥。
2. 在项目根目录启动 PostgreSQL 与 Redis：

   ```powershell
   docker compose -f infra/docker-compose.yml up -d postgres redis
   ```

   本地映射端口为 PostgreSQL `55432`、Redis `16379`、API `8010`，用于避开其他开发项目常用的默认端口。

3. 创建虚拟环境并安装依赖：

   ```powershell
   cd services/api
   py -3.13 -m venv .venv
   .\.venv\Scripts\python -m pip install -e ".[dev]"
   ```

4. 执行迁移并启动：

   ```powershell
   .\.venv\Scripts\alembic upgrade head
   .\.venv\Scripts\uvicorn app.main:app --reload --port 8010
   ```

开发环境固定验证码默认为 `123456`，不会通过接口返回，也不会写入日志。生产环境会拒绝以固定验证码或 `noop` 短信提供方启动。

## 测试

```powershell
.\.venv\Scripts\python -m pytest --cov=app --cov-report=term-missing
```

测试使用内存 SQLite、内存验证码仓库与限流器，不依赖外部服务。

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

前端字段、成功响应、错误码和未满 14 周岁注册分支见
[`docs/auth-integration.md`](docs/auth-integration.md)；设置页当前接入状态、字段和后续页面接入方式见
[`docs/settings-integration.md`](docs/settings-integration.md)。
