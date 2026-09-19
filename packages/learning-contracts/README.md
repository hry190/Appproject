# @jiqiao-jianghu/learning-contracts

> ⚠️ **已废弃(DEPRECATED)2026-09-19**

本包是 **React Native / Expo 前端**时代的产物,为「修炼」域提供 TypeScript 类型定义
(`src/index.ts`)与 fetch 客户端(`src/client.ts`)。

## 为什么废弃

**RN 前端已从本仓库移除**,当前客户端只有 Android(Jetpack Compose)。本包在全仓库
**没有任何引用**,保留它只是为了查阅当时的前端契约形状。

## 现在该看哪里

| 需求 | 去处 |
|---|---|
| 后端接口契约 | `services/api/docs/api/openapi.json`(FastAPI 自动生成)|
| 请求/响应模型 | `services/api/app/schemas.py` |
| Android 侧调用方式 | `android/app/src/main/java/com/jueqiao/jianghu/` 下各域自己的 `Repository` 与 `*Api` 类 |
| 后端接入说明 | `services/api/docs/*.md`(14 篇,按域划分)|

**新代码请勿依赖本包。** 若确实需要 TypeScript 侧的契约,应以 OpenAPI 文档为准重新生成,
而不是继续使用这里的旧类型。
