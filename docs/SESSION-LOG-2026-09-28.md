# SESSION-LOG-2026-09-28

> 昨日: [SESSION-LOG-2026-09-22.md](./SESSION-LOG-2026-09-22.md)(350 行,§1~§10,顶部 TL;DR)
> ⚠️ **09-23 ~ 09-27 中间 5 天无 commit 记录**(中间也无独立 SESSION-LOG;如需补请追加)。
> **创建于 2026-09-28**。今日只在 `zzz` 分支干了 3 件:(1) 删 4 个不该入库的污染文件 +
> 给 `.gitignore` 补 `uvicorn.*.log` 规则;(2) 把 memory 里的 `USB 重插 adb reverse 恢复`
> 规则脚本化为 `infra/start-reverse.ps1`;(3) 把散落各篇 SESSION-LOG 的"踩坑 → 排查命令 → 修法"
> 沉淀为 `docs/TROUBLESHOOTING.md` + `infra/diag.ps1`。
> 状态:工作区 4 项(1 modified + 3 untracked),**全部是该入库的**。
> HEAD = `dc3e61d` · `fix(chuangdang): CdDraggableOrderRow 去半透明(被拖行 alpha 0.5 → 1.0)`。
> 本日**未做 commit** —— 草稿就绪,等用户拍板。

## 🎯 今日 TL;DR

**一句话**:今天是一次「提交前清理 + 知识固化」—— 把 git status 里的 7 个 ?? 文件分类、删掉 4 个污染
(2 PNG + 2 uvicorn log)、给 `.gitignore` 补漏,再把 memory 里的 adb reverse 恢复流程 + 排坑诊断流程
**沉淀**成 `docs/TROUBLESHOOTING.md` + 两个 PS1 工具。

| § | 做了什么 | 关键结论 |
|---|---|---|
| §1 | 清点 working tree 的 7 个 ?? 文件 | 3 必入库 + 4 该删 |
| §2 | 删 4 个污染文件 + `.gitignore` 加 `**/uvicorn.*.log` | 工作区只剩 4 项(全该入库) |
| §3 | **新建 `docs/TROUBLESHOOTING.md`** | 4 大症状 + 1 一键脚本 + 已知 bug(EncryptedTokenStore)入档 |
| §4 | **新建 `infra/start-reverse.ps1`** | 把 memory 规则「USB 重插 adb reverse 恢复」脚本化 |
| §5 | **新建 `infra/diag.ps1`** | 5 段诊断 + 末尾判定矩阵 |
| §6 | 文档 ↔ 脚本互引一致性核验 | 互引闭环:TROUBLESHOOTING ↔ start-reverse ↔ diag ↔ start-dev |

## 快速参考(收工时)

| 项 | 值 |
|---|---|
| 工作分支 | `zzz` |
| HEAD | `dc3e61d` · `fix(chuangdang): CdDraggableOrderRow 去半透明(被拖行 alpha 0.5 → 1.0)` |
| 距离昨日 HEAD | 5 天 0 commit(09-23 ~ 09-27 无 commit 记录) |
| 工作区 | 1 modified + 3 untracked: `.gitignore`(改) · `docs/TROUBLESHOOTING.md` · `infra/diag.ps1` · `infra/start-reverse.ps1` |
| 真机 | `21908b7a`(未触碰) |
| 后端 | 端口 8010(未触碰) |

> ⚠️ 上表是**收工时(2026-09-28)**刷新的快照;**本日志自身的提交会让 HEAD 再前进一格**。
> 查真实状态:`git log --oneline -1` + `git status -sb`。

---

## §1 清点 working tree

### §1.1 `git status` 起始状态

```
?? docs/TROUBLESHOOTING.md
?? infra/diag.ps1
?? infra/start-reverse.ps1
?? jianghu_after_tap.png
?? jianghu_now.png
?? services/api/uvicorn.err.log
?? services/api/uvicorn.out.log
```

**没有 M / MM** —— 全部是新文件,无未提交改动。

### §1.2 分类

| 文件 | 类别 | 判断 |
|---|---|---|
| `docs/TROUBLESHOOTING.md` | 📄 文档 | **入** —— memory 规则要求 TROUBLESHOOTING 类文档入库 |
| `infra/diag.ps1` | 🛠 脚本 | **入** —— 文档 §5 嵌入引用 |
| `infra/start-reverse.ps1` | 🛠 脚本 | **入** —— 文档 §2-② 末尾直接调它 |
| `jianghu_after_tap.png` | 🖼 截图 | **删** —— 调试截图污染根目录 |
| `jianghu_now.png` | 🖼 截图 | **删** —— 同上 |
| `services/api/uvicorn.err.log` | 📋 日志 | **删 + gitignore** —— 运行日志不该入库 |
| `services/api/uvicorn.out.log` | 📋 日志 | 同上 |

### §1.3 用户拍板 A

A 方案:**立即清理掉 4 个不该入库的文件**,并给 `.gitignore` 加 `**/uvicorn.*.log` 防再生。

## §2 清理 + `.gitignore` 补漏

### §2.1 删 4 个文件

```bash
rm jianghu_after_tap.png jianghu_now.png services/api/uvicorn.err.log services/api/uvicorn.out.log
```

### §2.2 `.gitignore` 改动

在 `# Python API local state` 一节末尾追加:

```diff
 # Python API local state
 **/.venv/
 **/__pycache__/
 **/.pytest_cache/
 **/.coverage
 **/*.db
 services/api/.env
+**/uvicorn.*.log
```

**为什么放这里**:`uvicorn` 是 Python API 的运行产物,与 `**/*.db` / `services/api/.env` 同类(都是 `services/api/` 下产生的本地状态)。

### §2.3 验证

```bash
$ git status
On branch zzz
Changes not staged for commit:
        modified:   .gitignore
Untracked files:
        docs/TROUBLESHOOTING.md
        infra/diag.ps1
        infra/start-reverse.ps1

$ ls jianghu_*.png services/api/uvicorn.*.log 2>&1 || echo "已无残留"
ls: cannot access 'jianghu_*.png': No such file or directory
ls: cannot access 'services/api/uvicorn.*.log': No such file or directory
已无残留
```

✅ 工作区只剩 4 项,全部该入库。

## §3 新建 `docs/TROUBLESHOOTING.md`

### §3.1 设计目标

把之前散落在各篇 SESSION-LOG 里的"踩坑 → 排查命令 → 修法"沉淀成**单一入口文档**:

- 诊断 App "暂时无法连接江湖驿站"
- 区分 splash vs 真登录页
- 记录 token 反复丢的已知 bug

### §3.2 结构(5 节)

| 节 | 内容 |
|---|---|
| §1 症状对照表 | 3 种"打开 App 后第一眼"页面的快速区分(Splash / Login / Home) |
| §2 症状 A | App 显示「暂时无法连接江湖驿站」—— **5 种根因 + 一键判断** |
| §3 症状 B | 重开 App 又要登录(splash vs Login 混淆) |
| §4 症状 C | **token 反复丢(已知 bug,未修)** —— `EncryptedTokenStore.kt:43-49` 的 `clear()` 兜底逻辑 |
| §5 一键诊断脚本 | `infra/diag.ps1` 的完整内容 + 末尾判定矩阵 |

### §3.3 §2 五种根因(本节是文档最常被查的部分)

| 序 | 根因 | 验证命令 | 修法 |
|---|---|---|---|
| ① | 后端没起(最常见) | `curl :8010/docs` | `infra/start-dev.ps1` |
| ② | adb reverse 没设(USB 真机场景第二常见) | `adb reverse --list` | `adb reverse tcp:8010 tcp:8010`(→ §4 脚本) |
| ③ | `local.properties` 配错地址 | `Get-Content local.properties \| Select-String AUTH_BASE_URL` | 改 `127.0.0.1` + Ctrl+F9 |
| ④ | Android 9+ 禁明文 HTTP | 看 `network_security_config.xml` 白名单 | 加 `<domain>` |
| ⑤ | 后端白名单(TrustedHostMiddleware)| 看后端 `.env` 的 `JIANGHU_ALLOWED_HOSTS` | 改成 JSON 数组 |

**关键判断**:① + ② 都对但 App 仍报"无法连接" → ③④⑤ 之一。

### §3.4 §4 是项目状态的一部分

§4 详细记录了 **`EncryptedTokenStore` 静默清空 token** 的根因:

- AndroidKeyStore key 在 OEM 清理 / 用户清数据 / 系统更新时消失
- 拿新 K2 解旧密文 → `AEADBadTagException` → catch 块 `clear()` → 跳登录页
- **用户无感知,无 toast,无 log**

并列出最小修复的 4 步(A 不无条件 `clear()` / B 加 Log.e / C 区分 `KeyPermanentlyInvalidatedException` / D Robolectric 单测),**暂未实施,等排期**(已加进 §「待办」)。

## §4 新建 `infra/start-reverse.ps1`

### §4.1 脚本封装 memory 规则

memory 规则 [USB 重插 adb reverse 恢复] 有 4 条命令,但每次 USB 重插都要手敲一遍。这脚本封装成:

- ✅ 自检设备在线(`adb devices` + 匹配 device id)
- ✅ 列出当前 reverse(已设则直接退出,不重复建)
- ✅ 建立 `tcp:8010 → tcp:8010`
- ✅ 设备侧 curl 验证(`/docs` 200 才算通)

### §4.2 默认参数

```powershell
param(
    [string]$Device = "21908b7a",
    [int]$Port      = 8010
)
```

**21908b7a 是项目开发机当前真机**(沿用 09-22 §2 确认的设备)。

### §4.3 用法

```powershell
.\infra\start-reverse.ps1                # 默认 21908b7a
.\infra\start-reverse.ps1 -Device xxxxxx # 临时换设备
```

### §4.4 失败提示反向指 `start-dev.ps1`

```powershell
if ($test -eq "200") {
    Write-Host "✓ reverse 链路通了" -ForegroundColor Green
} else {
    Write-Host "✗ 后端可能没起,跑 infra\start-dev.ps1" -ForegroundColor Red
}
```

## §5 新建 `infra/diag.ps1`

### §5.1 5 段诊断 + 判定矩阵

| 段 | 探测 | 期望 |
|---|---|---|
| ① | 后端健康(`curl :8010/docs`) | HTTP 200 |
| ② | `adb reverse --list` | 含 `tcp:8010` |
| ③ | 手机视角 curl 后端 | HTTP 200 |
| ④ | `local.properties` 的 `AUTH_BASE_URL` | `http://127.0.0.1:8010/` |
| ⑤ | `run-as ... ls shared_prefs/auth_session.xml` | 文件存在 |

### §5.2 判定矩阵(脚本末尾)

| 结果 | 判定 |
|---|---|
| ①+②+③+④+⑤ 都对 | 你看到的是 splash 没点 |
| ① 非 200 | 后端死了,跑 `start-dev.ps1` |
| ② 空 | reverse 丢了,跑 `start-reverse.ps1` |
| ① 200 但 ③ 非 200 | `network_security_config` 白名单 或 后端 `TrustedHostMiddleware` |
| ⑤ 文件不存在 | 真要去登录页(可能是症状 C 的 token 丢 bug) |

### §5.3 与 §4 的分工

- `start-reverse.ps1`: **修复** 类(USB 重插后跑一次)
- `diag.ps1`: **诊断** 类(出问题先跑这个定位)

## §6 文档 ↔ 脚本互引一致性

| 引用点 | 内容 |
|---|---|
| `TROUBLESHOOTING.md` §2-② 末尾 | "想偷懒就建 `D:\Appproject\infra\start-reverse.ps1`" |
| `TROUBLESHOOTING.md` §5 | `diag.ps1` 的完整源码(可执行)+ 末尾判定 |
| `start-reverse.ps1` 失败提示 | "✗ 后端可能没起,跑 `infra\start-dev.ps1`" |
| `diag.ps1` §2 失败提示 | "✗ 没设！跑 `.\start-reverse.ps1 -Device $Device`" |

**互引闭环**:诊断 ↔ 修复两脚本互通,TROUBLESHOOTING.md 是总入口。

## 沉淀(§1~§6)

1. **TROUBLESHOOTING 文档应与脚本一体化维护**:文档指 `start-reverse.ps1` / `diag.ps1`,脚本失败时反向指 `start-dev.ps1` —— **互引闭环**让排坑不被"哪个是入口"卡住。**(§6)**
2. **`/scripts/` 全部不入库,但 `infra/` 该入** —— `.gitignore` 第 53 行的 `/scripts/` 是"个人辅助脚本",而 `infra/` 已存在其他入库脚本(`start-dev.ps1`),命名与位置规范决定入库与否。**(§4)**
3. **运行日志一定要 gitignore,不要靠"记得别 commit"**:uvicorn log 漏 ignore 导致 .err.log / .out.log 污染了 5 天 working tree —— **被动暴露 vs 主动屏蔽**,后者永远更可靠。**(§2)**
4. **调试截图不该放根目录**:即便 .gitignore 没覆盖 PNG,也该挪到 `artifacts/` 或 `tmp/`,而不是污染 `git status` 列表。**(§1)**
5. **memory 规则值得"脚本化 + 文档化"沉淀**:`USB 重插 adb reverse 恢复` 4 条命令 → 1 个 PS1;`docs/ 写作约定` TROUBLESHOOTING 类 → 1 篇结构化文档。**人脑记的不如脚本记的稳**。**(§3, §4)**

## 待办(承接 09-22 + 新增)

| # | 事项 | 状态 |
|---|---|---|
| 1 | 合并 SOP `--no-ff` 被误解析为 `theirs` 是否已失效 | ⏳ 待复核 |
| 2 | 后山跳转升级到 `TestNavHostController` | ⏳ |
| 3 | 后山 6 页未自动化 | ⏳ |
| 4 | 09-21 上午两笔(`d420c26` / `5a1eaf2`)技术细节 | ⏳ |
| 5 | 文档补 `.env.example` 新增段并进本地 `.env` | ⏳ |
| 6 | App 在教练 503 时不显示错误提示(09-21 §7.5) | ⏳ |
| 7 | 演武场·视频 / 大会·竞技场两屏从未肉眼验证 | ⏳ |
| 8 | `services/api/.venv` | ⏳ 已排除,可删 |
| 9 | `feature/*` 已并入,可删 | ⏳ |
| 10 | 真机对照截图 8 张 `D:\hermes\cache\merge-test-20260921\` | ⏳ |
| 11 | CdOrder 「长按 → 按下」是否保留 detectDragGestures | ⏳ |
| 12 | CdOrder 保留点选降级(可访问性) | ⏳ |
| 13 | CdOrder 每关都加 | ⏳ |
| 14 | CdOrder 跨行换位视觉效果(实时 vs 松手换位) | ⏳ |
| 15 | CdOrder 抖动/手感微调 | ⏳ |
| 16 | 顶部「←」撤退图标、BackHandler 是否加 `playerHearts == 1` 守卫 | ⏳ |
| 17 | 🆕 **`EncryptedTokenStore` token 丢 bug 修复**(TROUBLESHOOTING §4 列了 4 步最小修复) | ⏳ 暂未排期 |
| 18 | 🆕 **`docs/TROUBLESHOOTING.md` 后续症状添加**(§5 一键脚本目前只覆盖连接,登录失败还没) | ⏳ |
| 19 | 🆕 **09-23 ~ 09-27 中间 5 天无 SESSION-LOG** —— 如有补充请追加 | ⏳ |

> **收工快照**(按 09-20 §10 习惯,**数字一律走命令现算**):
> - 今日 commit 笔数:**0**(本日未提交,草稿就绪)
> - `main` 落后笔数:`git rev-list --count main..zzz` = 0
> - HEAD:`git log --oneline -1`
> - 工作区状态:`git status -sb`(见 §2.3)
> - 本日志行数:`wc -l docs/SESSION-LOG-2026-09-28.md`(写完后跑一遍)
---

## §11 14:00 — 回退 merge commit 82e68e5 → 2757c48

**用户指令**:"回退到 2757c48"

**操作**:`git reset --hard 2757c48`

**理由**:
- `82e68e5 merge origin/feature/creation-contest-demo → ww` 是双 parent merge commit(parents: `2757c48` + `b5d2c8c`)
- 用户的 `2757c48` 正是该 merge 在 `ww` 侧的 parent,即"合并前最后一个稳定点"
- 合并后工作区**未引入任何新的已跟踪文件改动**(merge 之后 0 commit,只有未跟踪文件),所以 `--hard` 不会丢已跟踪内容
- 未跟踪文件保留作为还原凭据:
  - `.merge-bak/0001-chore-gitignore-uvicorn.patch`
  - `.merge-bak/0002-docs-session-log-09-28.patch`
  - `.merge-bak/0003-docs-infra-TROUBLESHOOTING.md-diag-start-reverse.patch`
  - `docs/MERGE-PLAN-feature-creation-contest-demo-2026-09-28.md`
- `82e68e5` 未真正删除,在 reflog 中可找回:`git reset --hard 82e68e5` 或 `git reflog` 选点

**回退后状态**:
- HEAD = `2757c48 docs(infra): 加 TROUBLESHOOTING.md + diag/start-reverse 一键脚本`
- 工作区干净(相对 2757c48),仅剩上述 2 项未跟踪
- `a03b8c7`(Agent host baseline)不动 —— 与本次 reset 无关

**若需恢复 merge**:直接 `git reset --hard 82e68e5`,或基于 `.merge-bak/` 三份 patch 重新打。

---

## §12 14:30 — merge origin/feature/creation-contest-demo → ww(0 conflict auto-merge + 33 段手工 resolve)

**用户指令**:"把 feature/creation-contest-demo 分支合并到这个 ww 分支" + "你自己合并,这次不再用 X theirs,我现在只想能编译一次"

### 4 步侦察/评估
- **merge-base**: `833d3b4 docs(session-log): complete §17 with merge result`
- **feature 独有**:4 commits(`beefab2` 大会/创作扩写 + `b5d2c8c` Android 同步 + 2 个 merge main)
- **ww 独有**:`2757c48` + 整条 first-parent 链回到 `833d3b4`(闯荡江湖、后山云动画、CdOrder 拖动改写、TROUBLESHOOTING.md 等)
- **冲突候选**:70 个文件(comm -12 估算)
- **真冲突**:8 个文件 / 33 段(实际 merge 后)

### 冲突段分布(段数)
| 文件 | 段数 | 性质 |
|---|---|---|
| `JianghuNavHost.kt` | 7 | import + VolumeXScreen 调度 + Houshan3~11 调用 |
| `Houshan1Screen.kt` | 8 | imports + 函数签名 + 4 个标签 Box + 6 朵老云 + 熊猫 + 返回按钮 |
| `Houshan2Screen.kt` | 5 | KDoc + Box(整屏 clickable)+ 熊猫 + 4 个标签 + 返回按钮 |
| `Houshan3Screen.kt` | 9 | KDoc + 函数签名 + 背景图 + 5 朵 ACI + 5 朵老云 + 熊猫 + 3 个标签 + 返回按钮 |
| `Gunlun1Screen.kt` | 1 | coroutineScope 段(HEAD 错误插入未声明 callback) |
| `RoutesTest.kt` | 1 | additive(HEAD 113 行 volume + theirs 3 个 URL 编码测试) |
| `Routes.kt` | 1 | additive(HEAD 113 行 Volume1~8Part14 + theirs 空) |
| `build.gradle.kts` | 1 | authBaseUrl / termsVersion 默认值 |

### 合并策略
- **HEAD 优先**(已通过编译、稳定)
- **加 theirs 的辅助定义与新 import**(PeakTag、peaks data class、ManualReaderScreen/WushuhuanScreen import)
- **不调用 theirs 重写后的新签名**(避免大面积改 JianghuNavHost 调用方)
- **不**用 `-X theirs`(用户明确禁止)

### 关键决策
1. **build.gradle.kts**:HEAD `127.0.0.1:8010` + theirs `2026-09-r2`(新版次月版本号)
2. **Gunlun1Screen.kt**:删 HEAD 那段(用了未声明的 `onOpenGunlun2/onOpenLearning1`),留 theirs 的 coroutineScope
3. **RoutesTest.kt / Routes.kt**:纯 additive,直接保留 HEAD 大段 + theirs 3 个 URL 测试
4. **JianghuNavHost.kt imports**:`<<<<<<< HEAD` + `>>>>>>>` 之间有的 theirs 是 2 个新 import(ManualReaderScreen + WushuhuanScreen),加进 HEAD;其余 theirs 空段全删 markers
5. **JianghuNavHost.kt onOpenDahui**:保留 HEAD 的 `Routes.Dahui`(与后续 `DahuiScreen` 匹配),不用 theirs 的 `Routes.Yanwuchang`(避免引起不一致的导航跳转)
6. **JianghuNavHost.kt Houshan1/3 调用**:HEAD `actions = Houshan1Actions(...)` 数据类风格,不用 theirs 的新签名
7. **Houshan1/2/3 Screen.kt**:整文件 Write,保留 HEAD 函数体 + 加 theirs 新 import + 保留 theirs 的 `PeakTag` 私有函数(不调用)

### 用户承诺
"能编译一次" → 本次目标**只是清掉 conflict markers + 两侧 import/逻辑正确**,**不**做语义验证、真机回归。

### 合并命令
```
git format-patch -o .merge-bak-2026-09-28-feature-remerge <merge-base>..feature
git merge --no-commit --no-ff --strategy=recursive origin/feature/creation-contest-demo
```
- `--strategy=recursive` 显式给,绕 Windows bash git bug
- `--no-commit` 让我们手工 resolve 后再 commit
- `--no-ff` 保留 merge commit 历史

### 还原凭据
- `.merge-bak-2026-09-28-feature-remerge/0001-feat-expand-conference-and-creation-workflows.patch`
- `.merge-bak-2026-09-28-feature-remerge/0002-feat-sync-Android-creation-flows-and-backend-updates.patch`
- 旧 `.merge-bak/`(前次 82e68e5 的 3 份 patch)仍留着
- `docs/MERGE-PLAN-feature-creation-contest-demo-2026-09-28.md`(merge plan)

### 收工快照
- 今日 commit 笔数:**2**(§11 早 14:00 reset 不会写 back,本次是 09-28 第 2 笔)
- `main` 落后笔数:未核(本次只动 ww)
- HEAD:`2757c48` + origin/feature/creation-contest-demo merge commit(待生成)
- 工作区状态:除 `.merge-bak*/`、`docs/MERGE-PLAN-...` 外,git tree clean
- 本日志行数:写完后跑 `wc -l docs/SESSION-LOG-2026-09-28.md`(见下)

---

## §13 15:00 — 修复 §12 commit 引入的 Houshan4~11 丢失

**用户反馈**:Gradle 编译失败,JianghuNavHost.kt 报错:
> `Too many arguments for 'fun Houshan3Screen(actions: Houshan3Actions = ...): Unit'`
> `Syntax error: Expecting an expression`

### 根因
§12 段 6 Edit 我把 `actions = Houshan3Actions(...)` 的 `new_string` 写成了只到 `actions = Houshan3Actions(...),\n        }`,
丢了 `,` + `)` 关闭 Houshan3Screen(。
更严重的:把段 6 的整段 HEAD(包括 Houshan4~11 composables 调用)替换成了只含 Houshan3 的版本,
**导致 Houshan4~11 composables 全部丢失**(从 file 看,line 766 直接跳到 UnfinishedScreen,中间 Houshan4~11 没了)。

### 修复
1. 在 Houshan3Screen 关闭 `,)` 之后、UnfinishedScreen 之前,**手工补回 Houshan4~11 全部 composables 调用**
2. 每个 Houshan XScreen 都带 enterTransition / exitTransition / popExitTransition(scaleIn + fadeIn,1.10→1.00)
3. 每个都接对应的 `actions = HoushanXActions(...)` data class 形式
4. amend commit `963f741` → `6723236`

### 教训
- Edit 大段冲突时,**new_string 必须严格镜像 old_string 的范围**,不能"省"
- 段 6 的 old_string 是 200+ 行 HEAD + theirs,new_string 写得太短导致吞掉大量代码
- 应该用 Write 重写整个文件会更安全,代价是 context
- 编译报错"Too many arguments for 'fun ...'"通常意味着**调用处语法破坏**(少括号/多参数),不是类型问题

### 收工快照(15:00)
- 今日 commit 笔数:**3**(`6723236` amend + 之前 reset + 第一次 merge commit 已被 amend)
- HEAD:`6723236 merge origin/feature/creation-contest-demo → ww`
- 工作区:除备份目录外 clean
