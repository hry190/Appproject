# 修炼页契约交付包

这一目录是前后端分离对接时的唯一契约来源：`openapi.json` 描述线上 FastAPI 路由，`fixtures/` 提供空态、内容态、试炼成功/失败、迁移证据待审核和传习证据成功样例。

## 导出 OpenAPI

在 `services/api` 目录执行：

```powershell
.venv\Scripts\python.exe scripts\export_openapi.py
```

生成文件为 `docs/api/openapi.json`。后端路由或响应模型改变后重新执行，并在评审中一并检查契约差异。

部署或提交前可执行统一质量门禁：

```powershell
.venv\Scripts\python.exe scripts\verify_learning_backend.py
```

该命令会检查 50 页内容清单、OpenAPI 文件是否与当前路由同步，以及所有状态夹具能否被公开响应模型解析。

## 试炼内容生产

导出当前 50 页清单（未完成页保持 `DRAFT`，不含伪造答案）：

```powershell
.venv\Scripts\python.exe scripts\export_trial_manifest.py
```

内容团队补齐 JSON 后，用 `--input` 校验；只有同时具备判分器、判分配置和正式版本号的条目才能切为 `ACTIVE`。

上线前可查看逐页阻断报告：

```powershell
.venv\Scripts\python.exe scripts\report_learning_readiness.py
```

## 启动 Mock Server

```powershell
.venv\Scripts\python.exe scripts\mock_learning_server.py
```

服务地址为 `http://127.0.0.1:8099`。可用 `GET /v1/learning/overview?mode=empty` 切换空态，默认返回内容态；`POST /v1/trials/{id}/attempts?failed=true` 返回失败态。Mock 不连接数据库，也不修改前端。

## TypeScript 客户端

`Appproject/packages/learning-contracts` 提供与响应模型同步的类型和原生 `fetch` 客户端。前端接入时只需实例化：

```ts
const api = new LearningApiClient(import.meta.env.VITE_API_URL);
const overview = await api.getOverview();
```

请求失败统一抛出 `LearningApiError`，便于前端将网络失败、空内容和待审核状态分别映射到页面状态。
