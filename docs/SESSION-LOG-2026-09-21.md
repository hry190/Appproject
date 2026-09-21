# SESSION-LOG-2026-09-21

> 昨日: [SESSION-LOG-2026-09-20.md](./SESSION-LOG-2026-09-20.md)(861 行,§1~§12,顶部有 TL;DR)
> **创建于 2026-09-21**,起因:今日分两段 —— 上午是闯荡江湖「胜利结算」收尾(`d420c26` / `5a1eaf2`),
> 下午用户要求「跑一次后山板块的测试」,查完发现**后山没有任何现成测试**,遂新建一套,
> 随后把 `zzz` 同步到 `main`。**傍晚到夜间转入另一条线**:用户要评估 `feature/creation-contest-demo`
> 能不能合进来 —— 于是建沙箱分支真跑了一遍合并、装机做两版对照、重建后端、定位 503 根因,
> 最后正式合并进 `zzz` 并同步到 `main`(§5~§9)。
> 今日工作: **后山 1~11 页跳转逻辑回归单测(新建)+ 合并 SOP 偏差自查 + 文档对账
> + 沙箱验证 creation-contest-demo 合并 + 真机两版对照 + 后端重建与 503 根因 + 正式合并进 zzz/main + 一处命名纠正**
> 状态: 工作区干净。`zzz` = `main` = `origin/zzz` = `origin/main`(同一提交,SOP 4.6 已执行)。

> 📌 **归属说明**:09-21 的 commit 分三类 ——
> ① 上午两笔(`d420c26` / `5a1eaf2`,闯荡江湖胜利结算)是**本会话之前**的提交,本日志只登记、不重述;
> ② 本会话的实质产出:`719e278`(后山单测)、`16057cf`(本日志+对账)、`cf6a0ba`(合并进 zzz)、`43bff43`(合并进 main);
> ③ 合并带进来的 `beefab2` / `a4b3741` 属 `feature/creation-contest-demo` 原作者 Zqw66666666,细节看那两个 commit。
> **要看做了什么请直接看 commit / 代码,别在文档里替它编理由。**

## 🎯 今日 TL;DR

**一句话**:前半段用户要「跑一次后山板块的测试」,查下来后山**一条测试都没有**,
于是建了一套纯 JVM 的后山跳转逻辑单测并同步 `zzz`→`main`;
后半段评估 `feature/creation-contest-demo` 能否合并 —— **建沙箱分支真跑一遍、装机对照、重建后端、
端到端跑通对话流,再正式合并**,顺带纠正了一个我自己臆造的页面中文名。

| 段 | 做了什么 | 关键结论 |
|---|---|---|
| §1 | **后山板块跳转逻辑单测**(新建 `HoushanNavigationTest`,5 条)| 后山此前 **0 条测试**;新测试纯 JVM、不动依赖、不改任何源码 |
| §2 | `zzz` → `main` 同步(第一次) | `719e278` → merge `325b827` → 推 `main` + 推 `zzz` |
| §3 | **合并 SOP 偏差自查 + 补验** | 我没读 `MERGE-WORKFLOW.md` 就合了;漏了 **Step 4.2「推送前跑编译+测试」**。已补 |
| §4 | 文档对账 | 查出并修掉 **4 处**漂移(2 处行数 + 1 处索引 + **1 条跑不通的命令**) |
| §5 | **沙箱分支真跑合并**(`test/merge-creation-contest-demo`)| 1 处真冲突;⚠️ `JianghuNavHost.kt` **无冲突警告却被静默改写**(3041→2865) |
| §6 | **真机两版对照**(装合并版 + 装 zzz 版)| 主界面不变;工坊文案变;**生图对话页结构完全不同**(「下一步」→「保存上传草稿」)|
| §7 | **后端重建 + 503 根因** | 容器跑的是 **09-09 镜像**且无挂载;重建后接口从 404→503→**200**;根因是 `conversation_coach_provider` 默认 `disabled` |
| §8 | **正式合并进 `zzz` 并同步 `main`** | 树与沙箱**逐字节相同**;`main` 侧零冲突;SOP 4.6 已完成(`zzz` = `main`)|
| §9 | **命名纠正:`shengtu` = 生图,不是「圣途」** | 我凭读音臆造的中文名;仓库里有权威对照表(`ENVIRONMENT.md`)我一直没查 |

## 快速参考(收工时刷新)

| 项 | 值 |
|---|---|
| 工作分支 | `zzz` |
| 最近 commit | `43bff43` merge zzz → main。**现行哈希用 `git log -1` 取,别照抄这里** |
| 分支同步 | ✅ **`zzz` = `main` = `origin/zzz` = `origin/main` = 同一提交** —— SOP 4.6 已执行(`reset --hard main` + `push --force`)|
| 今日 commit(09-21)| 本会话之前:`d420c26`(09:19)· `5a1eaf2`(09:34)—— 闯荡江湖胜利结算<br>本会话:`719e278` 后山单测(15:32)· `16057cf` 09-21 日志+对账 · `cf6a0ba` merge zzz ← creation-contest-demo · `43bff43` merge zzz → main<br>合并带入:`beefab2` / `a4b3741`(原作者 Zqw66666666)|
| 工作区 | 干净 |
| 后山测试 | ✅ **新增 5 条**(`HoushanNavigationTest`);改动前项目仅 4 个测试文件,后山 0 条 |
| 单测总数 | **19 条全过**(`./gradlew :app:testDebugUnitTest`;zzz 16 + 合并带入 3)|
| 后端测试 | ✅ **123 passed**(`services/api/.venv` + `pytest`;21 文件 / 内存 SQLite,不需 Postgres)|
| 设计稿目录 | `D:\图` —— 仍处于已删除状态(2026-09-20 删);与今日无关 |
| 真机 | ✅ **接过了**(`21908b7a` / 2201123C / Android 15)。做了**合并版 vs zzz 版**两轮装机对照;`adb reverse` 过程中**又被清空 2 次**,均当场重建 |
| `feature/*` 分支 | `feature/creation-contest-demo` 已并入 `main`;`feature/authentication-foundation` 早已并入。两个都 `ahead=0`,可删 |
| 沙箱分支 | `test/merge-creation-contest-demo` —— **已删除**(本地 + 远端);它只是验证用,内容已由 `cf6a0ba` 承载 |

> ⚠️ 上表是**收工时(2026-09-21)**刷新的快照;**本文自身的提交会让 HEAD 再前进一格**。
> 查真实状态:`git log --oneline -1` + `git status -sb` + `git for-each-ref --format='%(refname:short) %(objectname:short)' refs/heads/main refs/heads/zzz`。
> ⚠️ **别用 `git rev-parse --short main zzz`** —— 多 ref + `--short` 会报 `fatal: Needed a single revision`(2026-09-21 实测)。
> 要么每个 ref 单独来一次(`git rev-parse --short main`),要么用上面的 `for-each-ref`。

---

## §1 后山板块跳转逻辑单测(新建)

### §1.1 需求与摸底

用户原话:「帮我跑一次后山板块的测试」→ 追问后:「**测试一下后山几个页面的跳转逻辑**」。

摸底结论(**先查清再动手**):

| 问题 | 结论 |
|---|---|
| 「后山」在哪 | `android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan{1..11}/` —— **11 个页面** |
| 入口 | `Gunlun1Screen.kt` 的「后山」按钮(`onOpenHoushan1` → `Houshan1Screen`);滚轮1 页由修炼页「前往后院」进入 |
| 有现成测试吗 | ❌ **没有**。`app/src/test/` 下只有 4 个不相关的测试文件;`androidTest/` 目录**不存在** |
| 能直接跑吗 | ❌ `scripts/chuangdang-regress/` 那 17 个脚本是**闯荡江湖五关**的真机回归,与后山无关 |
| 依赖具备吗 | ❌ 只引了 `junit4`;**没有** `androidx.navigation.testing`、没有 Compose UI 测试框架 |

> 📌 **命名陷阱(§1.4 沉淀 1 的由来)**:仓库里搜「后山」**一个字都搜不到** —— 屏目录一律用**拼音**
> (`houshan1..11`),中文只出现在**注释 / KDoc / 文档**里。我第一轮按中文搜全仓库得到 0 结果,
> 一度误判「功能不存在」。

### §1.2 跳转逻辑的真相:两套命名并存

这是读代码时最容易绊倒的一处,先钉在这里:

| 层 | 命名 | 例 |
|---|---|---|
| 路由常量(`Routes.kt`)| **`Shilian*`** | `Routes.Shilian` = `"shilian"`;`Routes.Shilian2..11` = `"shilian2".."shilian11"` |
| Composable / Actions | **`Houshan*`** | `Houshan1Screen(actions = Houshan1Actions(...))` |
| NavHost 接线 | 两层对接 | `composable(Routes.Shilian) { Houshan1Screen(actions = Houshan1Actions(onOpenHoushan2 = { navController.navigate(Routes.Shilian2) })) }` |

`Routes.kt` 顶部 KDoc 已写明沿革:项目早期是 **React Native / Expo(expo-router)**,路由名沿用当时命名;
**RN 前端已移除**,所以这只描述命名来源,不存在「两边要保持同步」的关系。**别为了「统一」去改名。**

### §1.3 新增的 5 条测试

**文件**:`android/app/src/test/java/com/jueqiao/jianghu/nav/HoushanNavigationTest.kt`(126 行)
**改了什么**:❌ 零 —— 没动 `build.gradle.kts`、没动 `libs.versions.toml`、没动任何 `Houshan*Screen.kt` / `Routes.kt` / `JianghuNavHost.kt`

| # | 测试 | 断言什么 | 防的是哪类回归 |
|---|---|---|---|
| 1 | `houshanRouteConstantsHaveExpectedStringValues` | `Shilian` / `Shilian2..11` 的**字符串值** | 路由字符串被改错 |
| 2 | `navHostRegistersComposableForEveryHoushanPage` | NavHost 里 11 条 `composable(Routes.ShilianN)` 都在 | **漏注册**导致某页不可达 |
| 3 | `everyOnOpenHoushanNavPointsAtMatchingShilianRoute` | 每条 `onOpenHoushanN = navigate(Routes.ShilianM)` 满足 **N == M** | 字段接到**错的路由**(最像手滑的一类) |
| 4 | `everyShilianRouteIsReachableByAtLeastOneHoushanPage` | `Shilian2..11` 至少被一条 `navigate()` 引用 | **死页**(没有任何上游会跳到它) |
| 5 | `everyHoushanActionsHasOnBackField` | 反射验 11 个 `HoushanNActions` 都含 `onBack` | `BackHandler` 依赖它,缺了会崩 |

### §1.4 写这套测试时踩的 4 个坑

1. **搜代码要搜拼音,搜文档才搜中文** —— 见 §1.1 的命名陷阱。中文只存在于注释/KDoc/文档。
2. **正则抓 Kotlin `data class` 字段别用 `\((.*?)\)`** —— `val onBack: () -> Unit = {}` 的**字段类型里有括号**,
   最小匹配会提前在那儿闭合,结果 11 个类各只抓到 1 个字段(`onBack`)。**必须锚到行尾的 `\n)\n`** 才收得住。
3. **Kotlin 字符串拼接把常量当字面量**:`if (n == 1) Routes.Shilian else "Routes.Shilian$n"` ——
   前一支取的是**常量的值**(`"shilian"`),拼进正则后成了 `Routes.Shilianshilian`,测试假失败。
   两条支路都得是**字面量** `"Routes.Shilian"`。
4. **`cls.kotlin.constructors...[].parameters` 需要 `kotlin-reflect`**,项目没引。
   改用 **Java 反射 `cls.declaredFields`** 即可(Kotlin 的构造器 `val` 会编译成同名字段),不引入依赖。

### §1.5 设计取舍:为什么没上 `TestNavHostController`

- **能测「点按钮真的 navigate 到哪」** 的写法需要 `androidx.navigation:navigation-testing`(提供 `TestNavHostController`),
  代价是**动 `libs.versions.toml`**。
- 本次先做**不依赖新依赖**的一层:断言"跳转声明写对了"(测试 2/3/4 本质是**文本级静态校验**)。
- **边界要说清**:这套测**不会**发现「点了按钮但 `onOpenHoushan2` 闭包忘了调 `navigate`」——
  它只看 NavHost 源码里那句赋值长什么样。要覆盖到那一层,得加依赖 + 写 `TestNavHostController`
  (用户当时选的是"按推荐的来",推荐即先做无依赖版)。

### §1.6 验证

```
cd android && ./gradlew :app:testDebugUnitTest
→ BUILD SUCCESSFUL;新增 5 条 + 原有 11 条 = 16 条全过
```

---

## §2 `zzz` → `main` 同步

用户指令:「同步到main」。

### §2.1 执行

| 步骤 | 命令 / 结果 |
|---|---|
| 1 | 先把后山单测提交到 `zzz`:**`719e278`**(提交前重跑过 `testDebugUnitTest` → BUILD SUCCESSFUL) |
| 2 | `git checkout main`;确认 `main` == `origin/main` == `c6dc4b2`,工作区干净 |
| 3 | `git merge --no-ff zzz -F <msg>` → **`325b827`**,ort 策略,19 笔入 main |
| 4 | `git push origin main` → `c6dc4b2..325b827` ✅ |
| 5 | `git push origin zzz` → `5a1eaf2..719e278` ✅ |

为什么用 `--no-ff`:仓库历史上 `main` 一直接 **merge commit**(`Merge branch 'zzz'`、
`merge zzz → main: ...`),不是 fast-forward。本次沿用。

### §2.2 验证(独立回读,不看本地缓存)

| 检查 | 结果 |
|---|---|
| `git ls-remote origin refs/heads/main refs/heads/zzz` | `main` = `325b827` = 本地;`zzz` = `719e278` = 本地 ✅ |
| `git diff --stat main zzz` | **空**(两分支内容逐字节相同)✅ |
| `git merge-base --is-ancestor zzz main` | ✅ `zzz` 是 `main` 的祖先 |
| `git rev-list --count origin/main..main` | `0` ✅ |

### §2.3 过程中的网络问题(老毛病,第 N 次)

`git fetch` 直接失败:

```
fatal: unable to access 'https://github.com/hry190/Appproject.git/':
  Failed to connect to github.com:443 over proxy 127.0.0.1 after 2058 ms
```

诊断结果:git 配的代理 `http.proxy = http://127.0.0.1:65532`(**用户梯子的本地端口**)——
该端口**无监听**;直连 `github.com:443` 也超时(`curl exit=28`);`7890 / 10809 / 1080` 都试过,同样不通。
**用户开启代理后** `code=200`,一次成功。

> 📌 与 [09-18 日志](./SESSION-LOG-2026-09-18.md) 记的是同一件事(「等用户开代理」)。
> 这是**环境问题,不是仓库问题**;判断方法:`netstat -ano | grep :65532` 看有没有监听。

---

## §3 合并 SOP 偏差自查(今日最该记的一条)

同步**做完之后**我才读到 `docs/MERGE-WORKFLOW.md` —— 它的第一行写着「**合并任何分支前必读**」,4 步严走。

### §3.1 我偏离了什么

| SOP 步骤 | 我做了吗 | 说明 |
|---|---|---|
| Step 1 侦察(commits / diff --stat) | ✅ | 看了 ahead/behind、`diff --stat`、`git log` |
| Step 2 评估(`merge-base` + `comm -12` 求两边都改的文件)| ⚠️ **部分** | 只看了 ahead/behind 与文件范围,**没做正式的"两边都改"交集计算**,也没给风险评级 |
| Step 3 建议(SOP 规定格式的报告 + 明确推荐)| ❌ | 没出「领先 N commit / M 文件 / 重叠文件 / 冲突风险 / 推荐」那份报告 |
| Step 4.1 试合并 `--no-commit --no-ff` | ❌ | 直接 `--no-ff` 一次性合了 |
| **Step 4.2 推送前跑 `compileDebugKotlin` + `testDebugUnitTest`** | ❌ | **最实质的一条** —— 没在合并结果上验就跑推了 |
| Step 4.3 查 conflict markers / `.orig` | ❌→✅ | 当时没查,**事后补查:0 marker、0 个 `.orig`** |
| Step 4.6 `zzz` reset --hard main + force push | ❌ | 见 §3.4,未执行(破坏性操作,已问用户) |

### §3.2 为什么这次实际是低风险(但不构成免责)

- `main` **严格落后** `zzz`(**合并当时** `git rev-list --count main..zzz` = **19**,`zzz..main` = **0**)
  —— 合并等价于 fast-forward;
- 合并后 `git diff --stat main zzz` **为空** → 合并结果与 `zzz` 的树**逐字节相同**;
- 因此"没验"这次**没有酿成后果**。

**但**:SOP 存在的理由正是"看起来没事"的合并也可能出事(它自己引的 2026-09-10 教训就是
「没有冲突但实际有 1 个文件被 auto-merge,git 不报错不警告」)。**判断低风险 ≠ 验证过。**

### §3.3 已补做的验证(合并**之后**补跑)

| 检查 | 结果 |
|---|---|
| `grep -nE '^(<<<<<<<\|=======\|>>>>>>>)'`(全 `main` 树)| ✅ 0 个 marker |
| `find . -name '*.orig'` | ✅ 0 个 |
| `./gradlew :app:compileDebugKotlin`(on `main`)| ✅ BUILD SUCCESSFUL |
| `./gradlew :app:testDebugUnitTest --rerun`(on `main`)| ✅ **16/16 通过**(报告时间戳确认是本次新生成的) |

### §3.4 SOP Step 4.6 的 `zzz` 对齐 —— ✅ 当晚已执行

SOP 写的是合并后 `git checkout zzz; git reset --hard main; git push --force origin zzz`,
目的是让 **`zzz` 与 `main` 指向同一个 commit**。

当时状态是「内容相同、hash 不同」,`reset --hard` + `--force` push 属**破坏性**操作,故先搁置等用户点头。

**2026-09-21 夜间已执行完毕**(见 §8.3):执行前核验 `main..zzz` = **0**(`zzz` 无任何独有提交),
所以强推不丢东西;结果 `zzz` = `main` = `43bff43`,本地与远端四方一致。
强推第一次因网络抖动失败(`Recv failure: Connection was reset`),代理确认正常后重试即成功。

> 📌 另记一条**待复核**:SOP 的「Windows bash git 已知 bug」一节说 `git merge --no-ff`
> 会被误解析成策略名 `theirs` 而报错、必须显式加 `--strategy=recursive`。
> **本次 `git merge --no-ff zzz -F <file>` 一次成功、没有复现**(日志显示用的是 `ort` 策略)。
> 可能是 git 版本已修,或触发条件比 SOP 写的更窄。**下次合并前留意,别照抄 SOP 的结论**。

---

## §4 文档对账(新建本日志时顺手查出并修掉)

按仓库 §9.2「新建 docs 文件后要更新 `docs/README.md` 的『当前文件清单』表」办手续时,顺带核了几个数字,**发现 4 处没跟上**:

| # | 位置 | 原文 | 实际 | 处理 |
|---|---|---|---|---|
| 1 | `docs/README.md` 清单 / 「最新一天」注 | 最新一天 = `09-20` | 有了 `09-21` | ✅ 已补索引行 + 改注 |
| 2 | `docs/README.md`「最新一天」注 | `09-19`(**1931 行**) | `wc -l` = **1944** | ✅ 已订正 |
| 3 | `SESSION-LOG-2026-09-20.md` 头部 | 昨日 `09-19`(**1940 行**) | `wc -l` = **1944** | ✅ 已订正 |
| 4 | 两份日志的收工快照说明 | `git rev-parse --short main zzz` | **这条命令本身是坏的** —— `fatal: Needed a single revision`(多 ref 不能用 `--short`,2026-09-21 实测)| ✅ 两处都换成可用的 `git for-each-ref --format='%(refname:short) %(objectname:short)' ...` |

> 第 4 条值得单独说:它是我**从 09-20 日志照抄**下来的 —— 也就是说 09-20 **写的时候就没跑过这条命令**。
> **写进文档的命令必须当场跑一遍**,否则就是在传抄一个假的"怎么查"。

**另修一处直接被我这次改动影响的**(不是历史漂移,是我加测试导致过期):

- `docs/ONBOARDING.md` §10.1「单元测试文件数 = **3**」→ 实际**已是 5**(`CreationToolCallModelsTest` 与本次的
  `HoushanNavigationTest` 都在那 3 个之后新增)。已更新为 5 个文件 / 16 条,并加两行说明:
  - `androidTest/` **目录不存在**、未引 Compose UI 测试依赖;
  - ⚠️ **`HoushanNavigationTest` 不是 click 测试**(详见 §1.5),别把它当成 ONBOARDING §10.2 第 1 条那类。

**核对过、确认无问题的**:`09-18`(1434 行)、`09-20`(861 行)、`09-16` 的行数标注与 `wc -l` 一致。

> 📌 教训与 [09-20 §10](./SESSION-LOG-2026-09-20.md) 同源:**行数 / 条数这类"随手写死的数字"必然漂移**。
> 09-20 §10 的结论是「涉及自身的计数写成命令」;这次查出的 3 处属于**"引用别的文件的数字"**,
> 同样会漂 —— **写的时候宁可用命令,或至少写清"截至某日"**。

---

## §5 沙箱分支:把 `feature/creation-contest-demo` 真跑了一遍合并

用户想知道「如果拉取,会不会有冲突或其他问题」。做法是**建一个沙箱分支真合一遍**,不碰 `zzz` / `main`。

分支:`test/merge-creation-contest-demo`(基于 `zzz`)。

### §5.1 先摸底:这个分支里有什么

| 部分 | 文件 | 行数 | 说明 |
|---|---|---|---|
| `services/api` | 36 | +4342 / −559 | 新增 `conversation_coach.py` + 迁移 `0023`~`0026` + 2 个测试 + 素材 |
| `android/app` | 24 | +5330 / −4341 | 新增 `dahui/ConferenceArenaDesign.kt`(2228 行)、`ConferenceWorksNavigationBar.kt` |
| docs + scripts | 3 | +647 | 含 `creation-conversation-simplification-20260910.md`(设计说明)|

### §5.2 冲突:1 个文件 / 1 个冲突块

`merge-tree` 预判与真合并**结论一致**:

```
CONFLICT (content): shengtu/ShengtuScreen.kt
Auto-merging: JianghuNavHost.kt / Routes.kt / RoutesTest.kt
```

**性质不是手滑,是重设计撞车**:

| | base(09-11)| `zzz` | 分支 |
|---|---|---|---|
| `ShengtuScreen.kt` 行数 | 2223 | 2223 | **625** |
| 组件集 | `ChatBubble`/`AgentThinkingPanel`/`CoachProposalCard` | 同 base | 换成 `ConversationBubble`/`CoachBubble`/`GenerationBubble`/`ConversationInput` |

`zzz` 侧**只在那个区块改过注释**(`920368a`),分支整段删掉 → 三方合并判「修改/删除冲突」。
**解法(本次采用):接受分支版**(`git checkout --theirs`)。

### §5.3 ⚠️ 无冲突警告、却被静默改写的文件(本次最大的坑)

`JianghuNavHost.kt` 报的是「Auto-merging」,**零冲突提示**,但行数 **3041 → 2865**:

| | `zzz` | 合并后 |
|---|---|---|
| `onOpenCollections` | 1 | **0** |
| `analysisNotice`(创作安全提示)| 2 | **0** |
| `navigateConferenceRoot` | 0 | **13** |
| `DahuiRecords` | 0 | 5 |
| NavHost 引用的 `Routes.*` | — | ✅ **一条没少**(231→232,只多 `DahuiRecords`)|

机制:这些行 **base 有、`zzz` 保留、分支删掉** → 三方合并**忠实执行了分支的删除**,换成分支自己那套接线
(`navigateConferenceRoot` + 不同回调名)。**只看 git 的冲突提示会完全漏掉它** ——
这正是 `MERGE-WORKFLOW.md` 开头警告的「Automatic merge went well ≠ 没冲突」。

### §5.4 验证结果(全过)

| 闸门 | 结果 |
|---|---|
| 冲突标记 / `.orig` | **0 处 / 0 个** |
| `compileDebugKotlin` | ✅ BUILD SUCCESSFUL(1m25s)|
| `testDebugUnitTest` | ✅ **19/19**(zzz 16 + 分支新 3)|
| 后端 `pytest` | ✅ **123 passed**(40s)|
| 迁移链 | ✅ **线性无分叉**:`0022` → `0023` → `0024` → `0025` → `0026` |

---

## §6 真机两版对照(合并版 vs `zzz` 版)

### §6.1 前置:`adb reverse` 又被清空

`adb -s 21908b7a reverse --list` **一开始就是空的**,设备侧 `/docs` 回 `000`。按 `ONBOARDING §3.4` 重建后:
设备侧 `/docs` 与 `/openapi.json` 都回 **200**。**这一晚又被清空了 2 次**,均当场重建。

### §6.2 装机

`assembleDebug` → `adb install -r`。**第一次被 MIUI 挡下**:

```
INSTALL_FAILED_USER_RESTRICTED: Install canceled by user
```

这是**设备侧授权**(文档 09-16 / 09-18 都记过),不是包的问题;重试并在手机上确认后 `Success`。
用 `dumpsys package ... | grep lastUpdateTime` 核对换包时间。

### §6.3 同一路径走两遍,肉眼差异

路径:`修炼页 → 作品创作 → 工坊 → 继续沟通 → 生图对话页`。

| 页面 | 差异 |
|---|---|
| **修炼页(主界面)** | **完全没变** —— 文字节点逐条相同,坐标都没变 |
| **工坊 · 创作台** | 教练台词:「我会先**帮你理清步骤**」→「我会先**听懂你的想法,再和你一起商量**」;作品卡按钮:「**制作**」→「**继续沟通**」;骨架相同 |
| **生图对话页** | **结构完全不同**:`zzz` 底部是「**下一步**」(多步流程)+ 中间「创作教练建议,请确认」+「暂不采用 / 采用建议」;合并版是**单一对话页** + 唯一主动作「**保存上传草稿**」|

这正是 §5.2 里 `CreationWorkflowDialog` / `ProductionWorkflowContent` / `SealWorkflowContent`
那套五阶段 UI 与分支对话式 UI 的分野,**真机上肉眼可见**。

---

## §7 后端重建 + 503 根因(本节的技术含量最高)

### §7.1 发现:后端根本没跟着换

合并带了 36 个后端文件,但**运行中的后端一行新代码都没有**:

| 检查 | 结果 |
|---|---|
| 后端形态 | **Docker 容器** `jianghu-dev-api-1`(PID 9904 是 Docker Desktop 后台,不是 uvicorn)|
| 容器挂载 | **无挂载 —— 代码打进镜像** |
| 镜像构建时间 | **2026-09-09 13:56**(12 天前);容器当天重启过但没重建镜像 |
| 运行中 openapi | 115 条路径,**conversation 相关 0 条** |
| 直探新接口 | `POST /v1/creation-conversations:start` → **404** |
| 测试分支工作区 | ✅ 确实有(`creations.py:106`)|

### §7.2 重建

compose 的 `api` 服务 `build.context: ../services/api` → 直接吃工作区代码,所以重建即可。
`docker compose up -d --build api worker`(**exit=0**,两个镜像 Built)。
启动命令自带 `alembic upgrade head` → **迁移 `0023`~`0026` 自动跑完**,`alembic current` = `0026_video_media (head)`。

重建后:openapi **127** 条路径 / conversation **8 条**;`/creation-conversations:start` → **404 变 401**(需鉴权 = 路由已存在)。

### §7.3 端到端:503 → 200

重建后重走对话流,**接口通了但报 503**:

```
GET  /v1/creation-projects/{id}                      → 200 OK
POST /v1/creation-projects/{id}/conversation:resume  → 503 Service Unavailable
```

**根因(源码 `conversation_coach.py:244`)**:

```python
def build_conversation_coach(settings):
    if settings.environment == "test":
        return ScriptedTestConversationCoach()
    if settings.conversation_coach_provider == "disabled":
        return None                    # ← 当前走这里
    return OpenAIConversationCoach(settings)
```

`None` → `service.py:2526` 抛 `ApiError(503, "COACH_SERVICE_UNAVAILABLE", "教练暂时没连上,请稍后再试。")`

**容器实际配置**:`JIANGHU_ENVIRONMENT=development`、`JIANGHU_CONVERSATION_COACH_PROVIDER` **未设置**(默认 `disabled`)、
`JIANGHU_OPENAI_API_KEY` **空**、`JIANGHU_DEEPSEEK_API_KEY` **未设置**。

**修法(本次采用 A)**:改 `services/api/.env` 第 4 行 `JIANGHU_ENVIRONMENT=development → test`,
走 `ScriptedTestConversationCoach()`(**确定性实现,不需要任何 API key**),重启 api 容器。

**结果**:`conversation:resume` → **200 OK**,生图对话页出现:

- 学生气泡:`yth`
- **教练气泡**:「我理解你希望"yth"。这次会以你的新要求为准,同时让主体和背景更清楚。你觉得这样可以吗?」
  (与 `conversation_coach.py:237~241` 的脚本实现**逐字一致**,确认走的是 test 实现)
- 「**采纳并继续**」按钮 + 输入框「继续和教练商量」+ ↑ 发送 + 底部「保存上传草稿」

> 事后已把 `.env` **还原为 `development`** 并与备份逐行比对确认一致。

### §7.4 顺带查出的缺口 —— ⚠️ 初次结论有误,当场订正

**初次结论(错)**:以为是 `infra/docker-compose.yml` 没配 conversation coach,导致"任何人拉下来都是空白"。

**实际(订正)**:查证后发现 ——

| 检查 | 事实 |
|---|---|
| `infra/docker-compose.yml` 是否入库 | ✅ **入库**,`zzz`/`main`/两个 `feature` 分支都有,未被 ignore,历史上没删过 |
| coach 变量从哪来 | compose 用 `env_file: ../services/api/.env` → **真正的来源是 `.env`**,不是 compose |
| `.env` 从哪来 | `services/api/README.md:7` —— 「将 `.env.example` 复制为 `.env`」 |
| **`.env.example` 里有 coach 配置吗** | ✅ **有**,而且是**本次合并的 `beefab2` 加进去的**:<br>`JIANGHU_CONVERSATION_COACH_PROVIDER=deepseek` / `..._MODEL=deepseek-v4-flash-vision-exp` / `..._TIMEOUT_SECONDS=60` / `JIANGHU_DEEPSEEK_API_KEY=***`(占位符)|
| 我的本地 `.env` 里有吗 | ❌ **0 行** —— 它是**合并之前**从旧模板复制来的 |

**⇒ 真正的因果**:`env_file` 只在**容器创建时**读 `.env`,**拉取代码不会更新别人的 `.env`**。
我的 `.env` 建于合并前,缺那几行 → 落到默认 `disabled` → 503。
**这是「老 `.env` 不自动跟上」的迁移问题,不是仓库缺口。**

**⇒ 因此**:`infra/docker-compose.yml` **不需要补** —— coach 变量本来就不该写在 compose 里(它走 `env_file`)。

**真正值得做的两条**:
1. **文档补一句**:拉取后若 `.env` 早于本次合并,需把 `.env.example` 的新增段手动并进 `.env`
   (否则会撞上"功能静默不可用",正是本次的坑);
2. **本地无密钥想跑通对话流**:把 `JIANGHU_ENVIRONMENT` 设为 `test`(走
   `ScriptedTestConversationCoach()`,确定性实现,不需要 key)。注意 `.env.example` 默认给的是
   `PROVIDER=deepseek` + **占位符 key** —— 那样会去调真实 DeepSeek 而失败,**不是一条能直接跑通的路**。

> 📌 **这条纠正本身也是一个教训**:我先前只看了 compose 文件就下了结论,
> 没顺着 `env_file` → `.env` → `.env.example` 这条链查下去。**"配置从哪来"要追到源头再断言。**

### §7.5 另一处观察(未断言为缺陷)

教练 503 时,**app 侧不显示任何错误提示** —— 生图对话页 UI 框架完整加载,卷轴区空白,
但那个 503 的用户文案「教练暂时没连上,请稍后再试。」**在界面上没看到**。仅记录观察。

---

## §8 正式合并进 `zzz` 并同步 `main`

### §8.1 `zzz` ← `feature/creation-contest-demo`

按 SOP Step 4 走(`--no-commit --no-ff --strategy=recursive`)。**冲突与沙箱完全一致**(同文件、同 1 块),
用**同一种解法**(取分支版)。

🎯 **关键一步:验证这次真合并的树与已验证的沙箱逐字节相同**

```
git diff --stat test/merge-creation-contest-demo   →  空
```

→ 沙箱上跑过的 `compileDebugKotlin` / `19/19` / 后端 `123 passed` **可直接沿用**。
随后仍按 SOP 4.2 强制重跑 `testDebugUnitTest --rerun` → **19/19 通过**;0 标记 / 0 `.orig`。
提交 **`cf6a0ba`**(两父:`16057cf` + `a4b3741`),推 `origin/zzz`。

### §8.2 `main` ← `zzz`(第二次同步)

侦察发现 `main` 与 `zzz` **已分叉**:`main..zzz` = 4,`zzz..main` = 1,merge-base = `719e278`。

> 📌 顺带**纠正一处我先前的说法**:`main` 里**其实没有**那笔 09-21 文档提交 ——
> 因为第一次「同步到 main」是在我**建日志之前**做的,`325b827` 的父是 `c6dc4b2` + `719e278`,不含 `16057cf`。

`merge-tree` 试合并 → **exit=0,零冲突**;真合并同样零冲突。合并后
**`main` 的树 == `zzz` 的树**(`fdf336c4`),推 `325b827..43bff43`。

### §8.3 SOP 4.6:让 `zzz` 与 `main` 同一提交

执行前核验 **`main..zzz` = 0**(`zzz` **无任何独有提交**),所以强推不丢东西。

```bash
git checkout zzz && git reset --hard main && git push --force origin zzz
```

第一次强推**网络抖动失败**(`Recv failure: Connection was reset`);代理端口确认在听、`curl` 回 200 后重试成功:
`cf6a0ba..43bff43  zzz -> zzz`。

**结果**:`zzz` = `main` = `origin/zzz` = `origin/main` = **`43bff43`**,四方一致。

### §8.4 沙箱分支与已并入分支

- `test/merge-creation-contest-demo`:**已删**(本地 + 远端)。它的 tip 不是 `zzz` 的祖先(是另一个合并 commit),
  故用 `-D`;但**内容与 `zzz` 逐字节相同、两个父都在 `zzz` 历史里**,只有那个 commit 对象变不可达,不丢内容。
- `feature/authentication-foundation`:早已并入,可删。
- `feature/creation-contest-demo`:刚并入,`ahead=0`,可删。

---

## §9 命名纠正:`shengtu` = **生图**,不是「圣途」

本条是**我自己的错误**,记下来防止重犯。

整场会话里我一直把 `ui/screens/shengtu/` 那个页面叫「**圣途**页」。用户指出应当是「**生图**」。核实:

| 检查 | 结果 |
|---|---|
| 「圣途」在仓库里出现次数 | **0**(源码 / 文档 / 提交信息 / 全部分支历史都搜不到)|
| 权威出处 | `ENVIRONMENT.md` 第 199~206 行**本来就有一张 Screen → 中文名 对照表** |
| 该表怎么写的 | `ShengtuScreen | ui/screens/shengtu/ | **生图页**` |
| 旁证 | `CONTRIBUTING.md` 写作 `img_shengtu_bg.png`(**生图页**背景);`settings-integration.md`、`creation-workflow-integration.md` 同样写「生图」|

**已处理**:本地截图文件名 `30-圣途对话页…` → `30-生图对话页…`;
已核实**未污染仓库**(提交信息与所有分支历史里都搜不到「圣途」)。

**教训**:这不是"表达随性",而是**有权威来源我没查** —— `ENVIRONMENT.md` 那张表就是为这件事准备的,
我却在第一次提到该页时按读音臆造了一个中文名,然后一路用了十几轮。**涉及页面命名,先查表。**

---

## 沉淀(§1~§9)

1. **搜代码用拼音,搜文档用中文。** 本项目的屏目录一律拼音(`houshan1..11` / `gunlun1..16` / `volumeNpartM`),
   中文只出现在注释、KDoc、docs 里。**先按中文搜会得到 0 结果,从而误判"这个功能不存在"。**
2. **SOP 存在但没读 = 没有 SOP。** 我按"看起来是简单同步"直接做了,
   事后再读 `MERGE-WORKFLOW.md` 才发现漏了正式评估和推送前验证。
   **收到"合并/推 main"指令时,第一件事应该是查有没有 MERGE-WORKFLOW.md 这类文档。**
3. **"低风险"是判断,不是验证。** 判断(严格落后 → 等价 ff)这次恰好对,
   但 SOP 要的是**跑过的证据**。补跑后 16/16 才叫验证 —— **判断与验证不能互相替代。**
4. **写测试前先确认"有没有现成的"。** 用户说"跑一次测试",实际仓库里那条测试**根本不存在**。
   先摸底再动手,省掉了"跑一个不存在的任务然后报失败"这一轮。
5. **写进文档的命令必须当场跑一遍。** `git rev-parse --short main zzz` 这条**跑不通的命令**
   在 09-20 日志里就躺着,我又照抄进了 09-21 —— **两边都在传抄一个假的"怎么查"**。
   文档里越像"标准操作"的句子,越要动手验一次。
6. **接受用户的一句"跑个测试"之前,先分清它问的是哪种测试。** 本项目同时存在
   **JVM 单测**(`app/src/test/`,16 条)与**真机回归**(`scripts/chuangdang-regress/`,只覆盖闯荡江湖)——
   用户说"后山板块的测试",两者**都不适用**(后山连单测都没有)。**先报现状、再让用户选方向**,
   比闷头造一套更省事。
7. **页面中文名有权威表,不要按读音臆造。** `ENVIRONMENT.md` §结构里有现成的
   `Screen 文件 → 目录 → 中文名` 对照表(`ShengtuScreen → 生图页`)。我凭读音造了个「圣途」,
   一路用了十几轮才被用户纠正(§9)。**涉及页面命名,先查表。**
8. **「Automatic merge went well」≠「内容正确」。** 本次 `JianghuNavHost.kt` 报 Auto-merging、
   **零冲突警告**,但被静默改写掉 176 行(含 `onOpenCollections` / `analysisNotice`),§5.3。
   合并后**必须 diff 关键文件**,不能只看 git 的冲突提示。SOP 自己警告过这一点,这次真撞上了。
9. **合并前把 SOP 读一遍,是能省掉事后补验的。** §3 那次我没读就合了,漏了 Step 4.2;
   §8 这次读了 SOP 再走,一次做全(侦察 → 评估 → 试合并 → 验证 → 提交 → 推送 → 4.6 对齐)。
   **同一份 SOP,读与不读的差别就是"补做"与"一次做对"。**
10. **后端容器里的代码是「构建时快照」。** compose 里 `api` 无挂载、代码打进镜像,
   所以**切分支不会让容器跟着变** —— 换了源码必须 `--build` 重建,否则 app 在跑新代码、
    后端还是十几天前的(§7.1 就是这样:镜像停在 09-09,新接口一路 404)。
11. **端到端不通时,先分清"代码问题 / 配置没开 / 代码没上机"。** 本次三步都不是 bug:
   ① 后端 404 = **代码没重建**;② 404→503 = **provider 默认 `disabled`**(配置);③ 页面空白 =
    上面两步的后果。**在"这是 bug"之前,先把这三层排掉。**
12. **沙箱分支验证法值得复用。** 建一个临时分支真合一遍 → 跑完全部闸门 → **正式合并时验证
    「树与沙箱逐字节相同」**,那么沙箱上的验证结论就能**直接沿用**,无需重复跑。
    本次靠这一条,正式合并只需重跑一次 `testDebugUnitTest` 即可确认(`git diff --stat` 为空是硬证据)。
13. **断言"配置缺了"之前,要顺着来源链追到源头。** 我先只看了 `infra/docker-compose.yml` 就断定
    "compose 没配 coach → 任何人拉下来都空白";实际上 compose 用 `env_file`,变量来自 `.env`,
    而 `.env` 由 `.env.example` 复制而来 —— **`.env.example` 里明明有**(还是本次合并加的)。
    真实原因是"我的 `.env` 建于合并前、拉取不会更新它"。**追链一步,结论就反了**(§7.4)。

---

## 待办

| # | 事项 | 状态 |
|---|---|---|
| 1 | ~~`zzz` 是否 `reset --hard main` + force push~~ | ✅ **已执行**(§8.3):`zzz` = `main` = `43bff43` |
| 2 | 合并 SOP 的「`--no-ff` 被误解析为 `theirs`」是否已失效 | ⏳ 待复核(两次都没复现)|
| 3 | 后山跳转是否要升级到 `TestNavHostController`(需加 `androidx.navigation.testing` 依赖)| ⏳ 等用户决定;加依赖前先问 |
| 4 | 后山尚有 6 页未纳入任何自动化(本次只测**跳转逻辑**,不测渲染/素材/动画)| ⏳ 如需覆盖,参照 [CHUANGDANG-REGRESSION-PLAYBOOK.md](./CHUANGDANG-REGRESSION-PLAYBOOK.md) |
| 5 | 09-21 上午两笔(`d420c26` / `5a1eaf2`)的技术细节 | ⏳ 无归属日志;**要看直接看代码**,别替它编理由 |
| 6 | ~~`infra/docker-compose.yml` 补 conversation coach 配置~~ → **订正**:compose **不需要补**(coach 变量走 `env_file`);真正该做的是**文档补一句「拉取后需把 `.env.example` 的新增段并进本地 `.env`」**(§7.4)| ⏳ 待办;另注:coach **没有 `development` 值**,本地无密钥只能靠 `JIANGHU_ENVIRONMENT=test` |
| 7 | app 在教练 503 时**不显示错误提示**(§7.5)| ⏳ 仅观察,未断言为缺陷;值得看一眼 |
| 8 | **演武场·视频 / 大会·竞技场两屏从未肉眼验证** | ⏳ 改动第二/第三大(`+818/−503`、全新 2228 行),只过了编译与单测 |
| 9 | `services/api/.venv`(为跑后端测试创建)| ⏳ 已在本 `git/info/exclude` 排除,不入库;可随时删 |
| 10 | `feature/authentication-foundation` / `feature/creation-contest-demo` 已并入,可删 | ⏳ 两者 `ahead=0` |
| 11 | 真机对照截图 8 张在 `D:\hermes\cache\merge-test-20260921\` | ⏳ 留作对照或清理 |

---

> **收工快照**(按 09-20 §10 习惯,**数字一律走命令现算**):
> - 今日 commit 笔数:`git rev-list --count e55e547..zzz`
> - `main` 落后笔数:`git rev-list --count main..zzz`
> - HEAD:`git log --oneline -1`
> - 工作区状态:`git status -sb`
> - 本日志行数:`wc -l docs/SESSION-LOG-2026-09-21.md`
