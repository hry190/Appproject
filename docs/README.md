# docs/ 文档索引

> 本目录是项目文档的索引。每次会话开始,先读 `README.md` 再读 `SESSION-LOG-最新一天.md`,快速了解项目状态。

---

## 怎么用这个目录

| 文件 | 何时读 |
|---|---|
| `README.md`(本文件) | 新会话 / 接手项目时 |
| `SESSION-LOG-YYYY-MM-DD.md` | 想了解某一天做了什么 |
| `DEV-SETUP.md`(在项目根) | 第一次配置 dev 环境 |
| `TROUBLESHOOTING.md`(计划中) | 遇到具体问题想查"怎么修" |
| `DECISIONS.md`(计划中) | 想了解架构决策的原因 |

---

## 当前文件清单

| 文件 | 用途 | 创建日期 |
|---|---|---|
| [README.md](./README.md) | 本索引 | - |
| [ONBOARDING.md](./ONBOARDING.md) | 新人入门指南 — 环境/规范/陷阱/必读 | 2026-09-10 |
| [SESSION-LOG-2026-09-07.md](./SESSION-LOG-2026-09-07.md) | 2026-09-07 dev 环境调试会话 | 2026-09-07 |
| [SESSION-LOG-2026-09-08.md](./SESSION-LOG-2026-09-08.md) | 2026-09-08 滚轮1-12 + 试炼1-3 + 未完待续/后山页 | 2026-09-08 |
| [SESSION-LOG-2026-09-09.md](./SESSION-LOG-2026-09-09.md) | 2026-09-09 环境清理 + 登录连不上根因 + 多维度扫描 + 学习2 页面 | 2026-09-09 |
| [SESSION-LOG-2026-09-10.md](./SESSION-LOG-2026-09-10.md) | 2026-09-10 学习/滚轮 6 新屏 + 修 dead button + v2 审计 | 2026-09-10 |
| [CODE-AUDIT-2026-09-09.md](./CODE-AUDIT-2026-09-09.md) | v1 代码审计 — 103 个 finding + 8 个优先行动 | 2026-09-09 |
| [CODE-AUDIT-2026-09-10.md](./CODE-AUDIT-2026-09-10.md) | v2 代码审计 — 68 个 finding(含 NEW/PERSISTED 标注) | 2026-09-10 |
| [SUMMARY-2026-09-09-to-2026-09-10.md](./SUMMARY-2026-09-09-to-2026-09-10.md) | 跨两天高层 TL;DR + 8 优先行动 | 2026-09-10 |

**项目根目录的文档**(不属于 docs/ 但常参考):
- [DEV-SETUP.md](../DEV-SETUP.md) — dev 环境完整配置流程
- [CONTRIBUTING.md](../CONTRIBUTING.md) — 协作约定
- [android/docs/screen-adaptation.md](../android/docs/screen-adaptation.md) — 屏幕适配方案(两段式/三段式 + 真机 dp 表)

---

## 新会话流程(建议)

1. AI agent 在新会话开始时,先读这个 README
2. 读最近一份 `SESSION-LOG-YYYY-MM-DD.md`(最新日期)
3. 看 `git status` 和 `git log --oneline -10` 了解当前状态
4. 问用户今天想做什么

---

## 添加新文档的约定

| 文档类型 | 命名 | 何时建 |
|---|---|---|
| 新人入门指南 | `ONBOARDING.md` | 项目首建,新人加入时 |
| 会话记录 | `SESSION-LOG-YYYY-MM-DD.md` | 每次长跑调试结束(≥30 分钟) |
| 决策记录 | `DECISIONS.md` | 做了重要的架构 / 设计决策 |
| 故障排查 | `TROUBLESHOOTING.md` | 解决了一个反复出现的问题,值得记录 |
| 代码审计 | `CODE-AUDIT-YYYY-MM-DD.md` | 多维度代码扫描结果快照(下次扫描另起一份,可用 diff 看趋势) |
| Sprint 计划 | `SPRINT-YYYY-MM-DD.md` | 启动一个多步骤功能开发 |
| 分支合并 SOP | `MERGE-WORKFLOW.md` | 合并任何分支前必读(侦察 → 评估 → 建议 → 合并)|

每类文档第一行写明:
```markdown
# 标题

> 一句话说明本文档是什么,什么时候创建,为什么需要它。
```

---

## 文档 git 政策

**应该 commit 到 git 的**:
- `SESSION-LOG-*.md`(团队历史)
- `DECISIONS.md`(架构决策)
- `TROUBLESHOOTING.md`(解决方案)

**不应该 commit**(`docs/.gitignore` 或项目根 `.gitignore`):
- 个人笔记(不是知识沉淀)
- 临时排查记录(没结论的)
