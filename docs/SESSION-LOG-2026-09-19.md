# SESSION-LOG-2026-09-19

> 昨日: [SESSION-LOG-2026-09-18.md](SESSION-LOG-2026-09-18.md)(1434 行,§1~§16,顶部有 TL;DR)
> 今日工作: **09-19 开始 — 工作区扫描 + SESSION-LOG 建档**
> 状态: 09-18 的 **10 个 commit 已全部推送**;工作区干净;`adb reverse` 昨夜**未被清空**

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | `zzz` |
| 最近 commit | `935c19e` docs(session-log): 09-18 加 TL;DR 总结 + 更新过期快照 |
| 远端 HEAD | 已同步(本地 = 远端)|
| Tracked 文件总数 | 1247 |
| 工作区未 commit | **0**(干净)|
| 09-18 SESSION-LOG | 1434 行(§1~§16,完整)+ 顶部 TL;DR |
| 09-19 SESSION-LOG | **本文件**(新建,刚开工)|

> ⚠️ 上表是 **09-19 开工时**的快照。后续工作会让"未 commit"/"远端 HEAD"变化,**别再当现状用**。
> 看真实状态:`git log --oneline -1` + `git status -sb`。

---

## 📌 昨日(09-18)遗留待办

> 来源:[SESSION-LOG-2026-09-18.md](SESSION-LOG-2026-09-18.md) 顶部 TL;DR 的「🔔 未完成 / 待办」段。

| # | 事项 | 昨日收工状态 | 今日开工状态 |
|---|---|---|---|
| 1 | **`听言解意篇` / `正心守道录` 缺"Y 最大页面"** | ⏳ 等用户创建(用户原话"先不要跳转,**之后提醒我**")| ⏳ 仍未创建 → 后山8/9 的 4 个标签保持死区 |
| 2 | **`adb reverse` 的 IDE Before launch 绑定** | ⏳ 用户明确决定**不做**(改为需要时手动跑脚本)| ✅ 已按"手动跑"执行;`reverse` 昨夜未被清空 |
| 3 | **`main` 分支落后** | ⏳ 未处理(纯 fast-forward)| ⏳ 仍落后 |

> 📌 09-18 的其余 16 段工作**全部完成并推送**(当日 10 个 commit:`d144407` → `935c19e`)。

---

## §1 09-19 开工 — 工作区扫描 + 建 SESSION-LOG

### 扫描结果

| 检查 | 结果 |
|---|---|
| 日期 | **2026-09-19 00:46 星期六** |
| 分支 + 同步 | `## zzz...remotes/origin/zzz`(**已同步**,无 ahead/behind)|
| 最近 commit | `935c19e`(09-18 加 TL;DR)|
| Tracked 文件数 | **1247** |
| 工作区改动 | **0**(干净,昨日收工状态完好)|
| 今日日志 | 不存在 → **新建本文件** |

### 环境快检(开工体检)

| 项 | 结果 | 判定 |
|---|---|---|
| `docker compose version` | ✅ `v5.4.0` | ✅ **继续扛住**(09-17 §34 的 `cliPluginsExtraDirs` 修复持续有效)|
| `adb devices` | ✅ `21908b7a device` | ✅ |
| **`adb reverse --list`** | ✅ `UsbFfs tcp:8010 tcp:8010` | ✅ **昨夜未被清空**(见下方对比)|
| API 8010 · 主机侧 `/docs` | ✅ **200** | ✅ 后端正常 |
| API 8010 · 设备侧 `/docs` | ✅ **200** | ✅ 端口转发有效 |
| `git push` | ✅ 可用 | ✅ 代理正常 |

### ✅ 两个改善点(对比 09-18 开工)

1. **`adb reverse` 没有在隔夜/重启后被清空** —— 这是前 4 次都踩的问题
   (09-17 §23 · §34,09-18 §1 · §6)。本次开工直接就是好的。
   > 注:这不代表问题根治了(§7 的脚本仍是兜底),但说明**昨夜环境稳定**(无重启、无 USB 重插)。
2. **`git push` 可用** —— 09-18 早上代理不通(§1 记录),当晚恢复并推送了 §34 + 当日 10 个 commit。

### 今日起点:后山1~9 的导航现状

> 这是今天最可能动到的部分,先把现状钉在这里(取自 09-18 §15/§16)。

| 导航方式 | 当前规则 |
|---|---|
| 标签(**本页 Y 最大**)| → 其文本对应的**卷的第一页** |
| 标签(**其他**)| → **该文本是 Y 最大标签的那个页面** |
| 标签(死区)| 后山8/9 的 `听言解意篇`、`正心守道录`(见待办 1)|
| 返回键 / 返回按钮 | → 上一页 |
| 整屏点击 | 后山2~9 **已取消**;后山1 **→ 后山2**(09-18 §16)|

> 📌 上表是 **今日开工(§1)快照**,记录的是开工那一刻的状态。当日后续变化见:
> §2(创建后山10)、§3(创建后山11 + 三页「正心守道录」接 dolly)、§4(main 同步 + 修仓库配置)。
> 收工现状:**后山 2~11;标签死区已全部清零**(上表"死区"一行的两处在 §2/§3 接完)。

### 待办

- [x] 等用户指示今日工作内容 → 已收到:创建后山10(§2)、后山11(§3)
- [x] 🔔 **(提醒项)**`听言解意篇` / `正心守道录` 的"Y 最大页面"创建后,接上后山8/9 的 4 个标签 → **后山10(§2)/ 后山11(§3)已创建**,相关死区全部接完
- [x] `main` 分支同步 → **已完成**(§4):4 个 ref 全部到 `97b55ff`;顺带修复 `.git/config` 的非法 `pull.twohead`
- [x] §2 + §3 的改动已提交并推送(`97b55ff`)

**A 模式**:执行但不 commit,等用户说"commit"

---

<!-- 以下为今日各段工作记录(§2 起) -->

## §2 创建后山10 页面 + 后山9 升格为过场页 + 后山8 接线

### 用户指令

> 继续创建后山10页面,那种山峰拉近的动画也要在跳转到后山10页面时出现,后山10页面复用后山8页面的素材和动画,在后山10页面,把标签'千层观心镜'的文本改成'听言解意篇',把标签'赏罚驭灵诀'的文本改成'正心守道录',删掉'听言解意篇'标签和'赏罚驭灵诀'标签,并且应用之前说的Y值最大的标签可以跳转到对应的卷-1

### 指令矛盾点与澄清

指令里"把 A 改成 B"和"删掉 B"同时出现,存在两种读法:

| 读法 | 结果 |
|---|---|
| 先把标签3/4 改名,再删掉同名标签 | 等于全删,页面无标签 —— 显然不是意图 |
| **先删掉原标签3/4,再把剩下的标签1/2 改名** | 页面保留 2 个标签,与原页面对应关系延续 |

采用第二种读法,并用 AskUserQuestion 确认了 4 个点:

| # | 问题 | 用户决定 |
|---|---|---|
| 1 | 删除哪两个标签 | 删原**标签3(听言解意篇 Y=322)** 和**标签4(正心守道录 Y=248)**,保留 2 个 |
| 2 | 后山9 → 后山10 的触发方式 | **点击「赏罚驭灵诀」**(非整屏 dolly) |
| 3 | 后山8/9 那 4 个死区标签 | **接到后山10** |
| 4 | 后山10 的「正心守道录」 | **保持死区**(不跳转) |

### 最终方案

后山10 的两标签(复用后山8 素材 + dolly 参数):

| 标签 | 原页文本 | 后山10 文本 | Y | 点击行为 |
|---|---|---|---|---|
| 标签1 | 千层观心镜 | **听言解意篇** | 570 | → `Volume9Part1` |
| 标签2 | 赏罚驭灵诀 | **正心守道录** | 345 | **死区** `{}` |

Y 最大值核验(§15 规则 1):

| 文本 | 后山8 | 后山9 | 后山10 | Y 最大页 |
|---|---|---|---|---|
| 听言解意篇 | 322 | 390 | **570** | 后山10 ✓ |
| 正心守道录 | 248 | 295 | **345** | 后山10 ✓ |

→ 🔔 **昨日遗留的提醒项(后山8 §2026-09-18.x 起挂着的「听言解意篇/正心守道录 缺 Y 最大页面」)至此关闭。**

### 改动清单

| 文件 | 改动 |
|---|---|
| `nav/Routes.kt` | 新增 `const val Shilian10 = "shilian10"` |
| `ui/screens/houshan10/Houshan10Screen.kt` | **新建**(512 行):包名 `...screens.houshan10`;`data class Houshan10Actions(onBack, onOpenVolume9Part1)`;删标签3/4;标签1/2 改名;KDoc 重写;dolly 代码保留为模板 |
| `ui/screens/houshan9/Houshan9Screen.kt` | **终点页 → 过场页**:新增 `onOpenHoushan10` 字段;`BackHandler(enabled = !isTransitioning)`;`startDollyIn` 末尾改调 `actions.onOpenHoushan10()`;3 个标签 `onClick = { startDollyIn() }` 且 `enabled = !isTransitioning`;返回按钮加 `graphicsLayer { alpha = chromeFade }` |
| `ui/screens/houshan8/Houshan8Screen.kt` | 新增 `onOpenHoushan10` 字段;标签3(听言解意篇)、标签4(正心守道录)由死区改为 `actions.onOpenHoushan10` |
| `nav/JianghuNavHost.kt` | 新增 2 个 import;后山8/后山9 区块各接 `onOpenHoushan10 = { navController.navigate(Routes.Shilian10) }`;后山9 的 3 个卷回调置 `{}`;新增 `composable(Routes.Shilian10)`(enterTransition 同 Shilian5~9,`scaleIn` + `fadeIn`) |

### ⚠️ 副作用(已向用户披露)

后山9 的「赏罚驭灵诀」Y=521 是**其本页 Y 最大标签** → 按 §15 规则 1 本应跳「卷8」。按用户选定的触发方式,它现在改为触发 dolly → 后山10。

**结果:后山9 不再有「→ 卷8」的入口。**(后山6/7/8 的「赏罚驭灵诀」仅仅是导航到后山9,不直接跳卷8。)

### 踩坑:锚点文本在多个区块重复 → 全局误替换

用 `onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },   // §15 赏罚驭灵诀 → 后山9` 作锚点追加 `onOpenHoushan10` 时,该锚点文本在 **后山6 / 后山7 / 后山8 三个区块**都出现,替换把 3 处全改了:

```
error: No parameter with name 'onOpenHoushan10' found   (JianghuNavHost.kt:748, :780)
```

修复:删掉后山6/后山7 里误加的字段,只保留后山8(L808)与后山9(L838)。**复现了本项目的既有教训 —— 锚点替换前必须先确认锚点文本唯一,或用行号范围限定;替换后必须核验"范围外未被改动"。**

### 验证

| 项 | 结果 |
|---|---|
| 后山8 四标签 | 千层观心镜→卷7、赏罚驭灵诀→后山9、听言解意篇→后山10、正心守道录→后山10 ✓ |
| 后山9 三标签 | 全部 `{ startDollyIn() }`;`BackHandler(enabled = !isTransitioning)` L149;`actions.onOpenHoushan10()` L207;返回按钮 `chromeFade` L530 ✓ |
| 后山10 两标签 | 听言解意篇→`actions.onOpenVolume9Part1`;正心守道录→`{}` ✓ |
| NavHost | `Shilian10` 路由 + 调用点齐备 ✓ |
| 构建 / 安装 | `compileDebugKotlin` BUILD SUCCESSFUL;设备 `21908b7a` 安装成功 ✓ |

### 沉淀

1. **指令自相矛盾时先澄清,不要猜**:本例"改名为 X"与"删掉 X"并存,两种读法结果完全不同(留 2 标签 vs 留 0 标签)。用 AskUserQuestion 一次性问清 4 个决策点,比改完再返工便宜得多。
2. **锚点替换的唯一性是硬约束**:同一段代码块在 NavHost 里会被复制到多个页面区块,文本锚点天然不唯一。规则:行号限定 → 替换 → 核验范围外。
3. **过场页 vs 终点页的差异要显式改造**:后山9 从"终点页"变"过场页"时,不能只加一个跳转 —— 还要 `BackHandler(enabled = !isTransitioning)` 防止动画期间返回键乱跳,以及返回按钮随 `chromeFade` 淡出与 chrome 层保持一致。

---

## §3 创建后山11 页面 + 三页「正心守道录」接 dolly 推进

### 用户指令

> 继续创建后山11页面，那种山峰拉近的动画也要在跳转到后山11页面时出现，后山11页面复用后山9页面的素材和动画，在后山11页面，把标签”赏罚驭灵诀“的文本改成”正心守道录“，删掉”听言解意篇“标签和页面原有的”正心守道录“标签，并且应用之前说的Y值最大的标签可以跳转到对应的卷-1

### 澄清(AskUserQuestion)

| # | 问题 | 用户决定 |
|---|---|---|
| 1 | 后山10 → 后山11 的 dolly 触发点 | **"点击任意页面的'正心守道录'标签"**(自定义回答)|
| 2 | 后山8(248)/ 后山9(295)的「正心守道录」是否改指后山11 | **改为直指后山11** |

→ 两条合起来 = **后山8 / 后山9 / 后山10 三页的「正心守道录」全部改为 dolly 推进 → 后山11**。

### 最终方案

**后山11(复用后山9 素材/动画,标签 3 → 1)**

| 标签 | 后山9 原文本 | 后山11 新文本 | X | Y | 点击行为 |
|---|---|---|---|---|---|
| 标签2 | 赏罚驭灵诀 | **正心守道录** | 124 | **521** | → **第十卷-1** |
| ~~标签3~~ | ~~听言解意篇~~ | **已删除** | 43 | 390 | 用户指令删除 |
| ~~标签4~~ | ~~正心守道录~~ | **已删除** | 105 | 295 | 用户指令删除(页面原有的)|

字号沿用原槽位 14sp / 行距 14sp(5×14=70dp < 容器 80dp)—— "赏罚驭灵诀"与"正心守道录"同为 5 字,无需调整。

**Y 最大值核验**

| 文本 | 后山8 | 后山9 | 后山10 | 后山11 | Y 最大页 |
|---|---|---|---|---|---|
| 听言解意篇 | 322 | 390 | **570** | — | 后山10(§2 已接)|
| 正心守道录 | 248 | 295 | 345 | **521** | **后山11**(§3 起)|

→ 「正心守道录」的 Y 最大页从后山10 **让位**给后山11;后山10 的该标签因此不再跳卷,改为 dolly 推进。

### 关键技术点:startDollyIn 参数化

后山9 原来的 `startDollyIn: () -> Unit` **硬编码**调 `actions.onOpenHoushan10()`,无法让同页不同标签去往不同目标。三页统一改为**接收跳转目标**:

```kotlin
val startDollyIn: (() -> Unit) -> Unit = { onComplete ->
    if (!isTransitioning) {
        isTransitioning = true
        scope.launch { dolly.animateTo(1f, tween(DOLLY_DURATION_MS, FastOutSlowInEasing)) }
        scope.launch { delay(DOLLY_HANDOFF_MS); onComplete() }
    }
}
```

调用点写作 `startDollyIn(actions.onOpenHoushan11)` —— 这是 lambda **捕获**,不是 composable 的参数传递,**不触发 §3 的 slot 0 null bug**(那个 bug 只在"多个同类型 lambda 直接作 composable 参数"时出现)。

### 改动清单

| 文件 | 改动 |
|---|---|
| `nav/Routes.kt` | 新增 `const val Shilian11 = "shilian11"` |
| `ui/screens/houshan11/Houshan11Screen.kt` | **新建**:复用后山9 全套素材 / 云雾 / dolly 参数;`data class Houshan11Actions(onBack, onOpenVolume10Part1)`;标签 3→1;dolly 代码保留为模板(未启用)|
| `ui/screens/houshan8/Houshan8Screen.kt` | Actions 加 `onOpenHoushan11`;`startDollyIn` 参数化;标签4(正心守道录)由"直接 navigate 后山10"改为 `{ startDollyIn(actions.onOpenHoushan11) }`;3 个卷字段标注为预留 |
| `ui/screens/houshan9/Houshan9Screen.kt` | Actions 加 `onOpenHoushan11`;`startDollyIn` 参数化;标签4(正心)→ 后山11,标签2(赏罚)/标签3(听言)显式传 → 后山10 |
| `ui/screens/houshan10/Houshan10Screen.kt` | Actions 加 `onOpenHoushan11`;`startDollyIn` 参数化并**启用**;标签2 由死区 `{}` 改为 → 后山11 |
| `nav/JianghuNavHost.kt` | 2 个 import;后山8/9/10 三区块各接 `onOpenHoushan11`;新增 `composable(Routes.Shilian11)`(enterTransition 同 Shilian5~10)|

### ⚠️ 副作用(已向用户披露)

1. **后山10 的「正心守道录」不再是死区** —— 这也推翻了 §2 时"保持死区"的决定(那时后山11 尚未创建,该标签无处可去;现在它有了 Y 最大页)。
2. **后山8 的「正心守道录」不再跳后山10** —— 改为 dolly → 后山11。
3. 后山8 有 4 个标签,**只有标签4 走 dolly**,其余 3 个仍是直接 navigate(无动画)。这是刻意的:用户只要求「正心守道录」的跳转带山峰拉近动画。

### 验证

| 项 | 结果 |
|---|---|
| 后山8 标签4 | `onClick = { startDollyIn(actions.onOpenHoushan11) }`(L547)✓ |
| 后山9 三标签 | 听言(L437)/ 赏罚(L502)→ `onOpenHoushan10`;正心(L469)→ `onOpenHoushan11` ✓ |
| 后山10 两标签 | 听言 → `onOpenVolume9Part1`;正心(L465)→ `startDollyIn(onOpenHoushan11)` ✓ |
| 后山11 | 仅 1 个标签 `contentDescription = "标签2"`,文本 `正\n心\n守\n道\n录`;无"听言解意篇"残留 ✓ |
| NavHost | L811 / L842 / L871 三处 `onOpenHoushan11` + L879 `route = Routes.Shilian11` ✓ |
| 构建 / 安装 | `compileDebugKotlin` + `assembleDebug` BUILD SUCCESSFUL;设备 `21908b7a` Streamed Install **Success** ✓ |

### 沉淀

1. **"让动画能去往不同目标" = 把目标作为参数传入**:原 `startDollyIn` 硬编码单一目标,一旦同页出现"两个标签去往不同页面"就必须参数化。参数化版本同时兼容模板用途(无调用点时保留,如后山11)。
2. **§15 规则的"Y 最大页"是动态的**:每新建一页,都可能让旧页某文本失去 Y 最大地位。后山11 创建后,后山10 的「正心守道录」立即从"该跳卷"变成"该推进"。**每次加页面都要重算全链 Y 值**,并回头检查旧页的接线是否仍然成立。
3. **同文件多处相似锚点必须带坐标**:后山9 的标签3 与标签4 的 `.clickable(...)` 行**逐字相同**(连注释也一样),只用该行作锚点会命中两处。加上上一行 `.offset(x = ..., y = ...)` 即可唯一 —— 本次三页的标签替换全部使用带坐标的锚点(延续 §2 踩坑的教训)。

---

## §4 main 分支同步 + 修复仓库非法配置

### 用户指令

> 同步到main

### 结果

| Ref | Commit |
|---|---|
| `local main` | `97b55ff` |
| `origin/main` | `97b55ff` |
| `local zzz` | `97b55ff` |
| `origin/zzz` | `97b55ff` |

推送结果:`9a30078..97b55ff  zzz -> main`(fast-forward)✓ 当前仍停在 `zzz` 分支,工作树干净。

### ⚠️ 踩坑 1:`origin/zzz` ref 歧义导致差点误判

最初用简写判断,得到**"双向分歧、不可快进"**的错误结论:

```
git rev-list --left-right --count origin/main...origin/zzz   →  21   1
git merge-base --is-ancestor origin/main origin/zzz          →  exit 1
stderr: warning: refname 'origin/zzz' is ambiguous.
```

改用**完整 refname** 后结论完全反转:

```
git rev-list --left-right --count refs/remotes/origin/main...refs/remotes/origin/zzz  →  0   21
git merge-base --is-ancestor refs/remotes/origin/main refs/remotes/origin/zzz         →  exit 0 ✓
```

→ **教训:脚本里判断分支关系一律用 `refs/remotes/origin/xxx` 全名,不要用简写。** git 自己会打印 ambiguous 警告,但**很容易被忽略**。

### ⚠️ 踩坑 2:`.git/config` 里的非法合并策略(真实 bug)

原计划 `git checkout main && git merge --ff-only zzz` —— **失败**:

```
Could not find merge strategy 'theirs'.
Available strategies are: octopus ours recursive resolve subtree
```

根因在 `.git/config`:

```ini
[pull]
	twohead = theirs     ← 非法:该键要的是**策略名**(ort / recursive / resolve / subtree / octopus),
	                       而 theirs 是 `-X theirs` 的**选项值**
[merge]
	ff = false
```

Git 2.55 会让 `git merge` 读取 `pull.twohead` 作为默认策略 → **每次 merge 都直接报错终止**。

→ **这很可能就是 main 长期无法同步的真正原因**:不是"忘了合并",而是合并命令**根本跑不起来**。

**绕行方案**(完全不需要 merge):

```
git push origin zzz:main                                          # 远程 main 快进
git fetch origin
git branch -f main refs/remotes/origin/main                       # 本地 main 跟进
```

**修复**(用户选择"删掉 pull.twohead"):

```
git config --unset pull.twohead
```

验证:`git merge --ff-only zzz` → `Already up to date.`,**exit=0** ✓ merge 功能恢复正常。

> **遗留**:`merge.ff = false` 保持未改。它本身合法,但效果是"merge 默认总是创建合并提交,即使可以快进"。
> 若日后希望 `git merge` 能直接快进 main,需另行改为 `true`,或在命令里显式加 `--ff-only`。

### 沉淀

1. **判断分支关系用全名 refname**:简写 `origin/zzz` 存在歧义风险,会**静默给出错误结论** —— 本次险些因此放弃一次完全安全的快进。
2. **多工具一致 ≠ 结论正确**:`--left-right --count` 与 `is-ancestor` 双双指向"分歧",但换成全名后两者同时反转,说明错误来自 **ref 解析**,而非仓库状态。当多个证据共享同一个错误输入时,它们的一致没有说服力。
3. **`.git/config` 也会藏 bug,且报错信息会误导**:它不随仓库分发,容易在换机器/换工具后被写入非法值;而 `Could not find merge strategy` 指向的是"策略",不是配置键。排查时应先跑 **`git config --list --show-origin`** 看全量配置。

---

## §5 首页1 第五个主入口「闯荡江湖」+ 五个入口文字样式统一

### 用户指令(连续四条)

1. `D:\图\未标题-151.png` 作为首页1 第五个入口「闯荡江湖」的图像,放 **X286 Y227**,尺寸与其它四个标签一致,文本字号 14、白色、regular
2. 「我希望文本'闯荡江湖'的行间距和文本'作品创作'的行间距一致」
3. (真机反馈)「为什么我调整文本'闯荡江湖'的行间距,但是似乎没有效果」
4. 「先把五个入口的字重统一,把'闯荡江湖'文本和其他四个标签统一起来」→ 追问颜色 → 选 **A(完全交给默认)**

### 最终落点

| 项 | 结果 |
|---|---|
| 素材 | `res/drawable/img_home1_btn5.png`(138×299,比例 0.462,与 `img_home1_btn3` 完全相同)|
| 位置 / 尺寸 | X286 Y227 · 55×90(与其它四个一致)|
| 字体 | 沿用 `YaHei` 变量 = **Noto Sans SC**,14sp |
| 字重 | `SemiBold`(与四个**同源**,不再各自覆盖)|
| 颜色 | 默认暖白 `#F4E6CF` ↔ 悬停/按下 `#FFF7DC`(同源)|
| 行高 | 显式 18.82sp(= 13sp × 1.448)|
| 入场动效 | 加入 `PrimaryHomeAction` 枚举,自动纳入随机顺序淡入 |

### ⚠️ 踩坑 1:逐字 Text 的 lineHeight 不生效(用户"没效果"的根因)

`DecorButton` 原本把**每个字符渲染成独立的 Text**(Column 逐字堆叠)。每个 Text 只含 1 个字 = **单行文本**,而 Compose 对单行文本的 `lineHeight` 处理不可靠 —— 数值怎么调都没有视觉变化。

**项目内已验证有效的做法**是「单 Text + `\n` 硬换行」:后山标签 `Text("听\n言\n解\n意\n篇", lineHeight = 9.sp)` 的注释明确标着 `5×9=45dp`,且能压到小于字号。

**修复**:`textLineHeight != null` 时改走单 Text 多行渲染;未指定时保持原逐字实现(四个旧入口**零变化**)。

### ⚠️ 踩坑 2:字体家族没有 SemiBold 档,实际渲染是 Bold

`Type.kt` 的 `YaHei` 家族只注册三档:`Normal(400)→regular 文件`、`Medium(500)→regular 文件(疑似笔误)`、`Bold(700)→bold 文件`。四个入口请求的 `SemiBold(600)` **不存在**。

读 Compose 1.7.6 源码 `FontMatcher.kt` 确认匹配规则:

```kotlin
fontWeight > FontWeight.W500 -> fontsToSearch.filterByClosestWeight(fontWeight, preferBelow = false)
```

即"优先取更重的" → `SemiBold(600)` 命中 **700 Bold**。**四个入口实际是粗体。**

→ 所以「真正的一致」不是把新入口改成 `Normal`,而是**两边都不覆盖、走同一个默认值**。

### 字体行高系数 1.448 的来源

解析 `noto_sans_sc_regular.otf` 的 hhea 表:unitsPerEm=1000 / asc=1160 / desc=-288 / gap=0 → **1.448**;`USE_TYPO_METRICS=False`,故 Android 走 hhea 而非 typo(typo 系数 1.0,差异极大)。
→ 「作品创作」13sp × 1.448 = **18.82sp**;「闯荡江湖」14sp 原本 20.27sp(偏大 7.7%)。
三个字重文件的 hhea 度量**完全相同**,故该值与字重无关。

### 顺带发现(已向用户指出,尚未修)

- `noto_sans_sc_medium.otf`(8.5 MB)**完全没被引用**,白打进 APK
- `FontWeight.Medium` 指向的是 regular 文件 —— 几乎肯定是笔误
- 三个字体合计 **25.7 MB**,建议子集化

### 沉淀

1. **Compose 的 lineHeight 要配多行文本**:逐字独立 Text 的写法下它不生效。
2. **"看起来没变"先怀疑渲染路径,而不是数值**:用户把 18.82 手改成 17 也没变化 —— 问题根本不在数值。
3. **字体家族缺档位会静默降级**:`SemiBold`→`Bold`、`Medium`→`regular`。要看真实渲染效果,得读匹配算法或实测,不能只看声明。

---

## §6 闯荡江湖五关(纯 Android 端实现)

### 用户指令

> 「现在请查看文档然后实现入口'闯荡江湖'的功能,重在实现功能,用的素材随便你使用」

澄清回答:**「先做页面出来,先不接后端」** + Boss 评审用**本地规则评分**。
后续追加:「虽然只做'识机真诀'的五个关卡,但是还要留下可以选择其他书本的选项」。

### 交付(5 个新文件,2124 行)

| 文件 | 职责 |
|---|---|
| `ChuangdangData.kt` | 模型 + 五关题库(16 题)+ 规则常量 + **十本秘籍目录** |
| `ChuangdangStore.kt` | 进程内进度(闯荡令 / 已通关 / 出发状态)|
| `ChuangdangMapScreen.kt` | 地图:五节点 + 闯荡令 + 出发 + **秘籍切换面板** |
| `ChuangdangBattleScreen.kt` | 战斗:三心攻防 |
| `ChuangdangBossScreen.kt` | Boss:制作任务 + 本地五维规则评分 |

接线:`Routes.kt`(3 条路由,战斗关号走 `NavType.IntType`)、`JianghuNavHost.kt`(3 个 composable + 首页1 入口)。

### 对照策划方案 v2

| 文档条款 | 实现 |
|---|---|
| §2 地图五节点 / 印记 / 当前关 / 闯荡令余额 | ✓ |
| §3.1 左右站位、双方三心、每关重置、生命不带入 | ✓ |
| §3.2 回合判定顺序(答对→攻击;答错→防御机会;解释后置) | ✓ 状态机 |
| §3.3 格挡递减 30/20/10/0%,防御答对不计入 | ✓ `cdBlockRate()` |
| §4.1 任务卡提前公开交付形式与评分标准 | ✓ |
| §4.3 五维量表 + 门槛 70/18/12 + **必要条件** | ✓ `cdScoreBoss()`,必要条件作硬门槛 |
| §5 五关剧情与敌人 | ✓ 按原文写进题库 |
| §6.1 出发扣 1 枚、连推不扣令;§6.2 上限 3 枚 | ✓ |
| §12 十本秘籍,其余九本标"后续开放" | ✓ 点击只给提示不跳转 |

### Boss 本地规则评分要点

五维全部是**关键词 + 结构检查**:知识 30(能力边界词 ∧ 人工确认词)、完成度 25(五项要求各 5 分)、解释 20(10 个决策词每种 +5)、测试修正 15(失败情形 +8 / 验证办法 +7)、表达 10(字数分档 + 编号分段)。

通关 = 总分≥70 ∧ 知识≥18 ∧ 解释≥12 ∧ **`boundary && humanCheck`**。建议最多 3 条(对应文档"最多三项优先修改建议")。

**诚实标注的弱点**:能被堆砌关键词骗过;措辞稍变就失分;把"感知/行动"说反了照样得分;字数敏感。文档自己写了"具体分数需结合样例校准",故明确标注为**演示级替代**。替换成本低 —— 逻辑全在 `cdScoreBoss()` 一个私有函数里。

### ⚠️ 修掉一个初始化顺序隐患

地图页的节点列表依赖**同包另一文件**的顶层 `CD_STAGES`。Kotlin 跨文件顶层属性的初始化顺序**没有保证**,直接引用可能拿到尚未初始化的空列表 → 已改为 `by lazy`。

### 未做(按"先做页面"裁剪)

后端(进度不落库,杀进程即重置)、AI 评审、修为/境界/配饰、补修与错题联动、断网恢复、防重提交、战利品池、完整皮肤。

### 验证状态

`assembleDebug` BUILD SUCCESSFUL + 真机安装成功。
⚠️ **完整流程未在真机走通** —— 开发机侧卡在登录页(无凭据),需用户自行体验。

---

## §7 给已移除 RN 前端的残留加废弃标记

### 背景

调研技术栈时发现项目**早期是 React Native(Expo SDK 57)应用** —— `android/README.md` 明确写着"从 React Native (Expo SDK 57) 项目 `../mobile/` 改写而来"。该前端已移除,留下三处残留。

### 改动(只加标记,不改任何逻辑)

| 目标 | 处理 |
|---|---|
| `scripts/` 下 10 个 Playwright 脚本(`capture-*.py` ×9 + `debug-forgot.py`)| 文件头插入废弃注释块,说明 `localhost:8081` 是 Metro 端口,并给出替代做法(adb screencap + pull);`py_compile` 语法自检通过 |
| `packages/learning-contracts` | 新增 `README.md`(废弃原因 + "现在该看哪里"对照表);`package.json` 的 description 加 `[已废弃]` 前缀并补标准 `deprecated` 字段 |
| `Routes.kt` KDoc | 原文 "Mirrors the React Native expo-router route names" 易被误读为"两边需同步",补写命名沿革,明确它只描述**命名来源** |

**未改**:`android/README.md` 那句沿革说明是**准确的**,不是残留。

### ⚠️ 自己的误判(值得记)

我在对话中一度断言 `learning-contracts/package.json` 的 description 是乱码 `淇偧椤靛悗绔?` —— **实为 PowerShell 终端把 UTF-8 显示成 GBK**,用 read 工具看原文是正常中文「修炼页后端 API contracts and fetch client」。

**同一天栽了两次**(另一次是给脚本加标记后的验证)。教训:**判断文件内容只看 read 工具输出,不用终端 `Get-Content` 的显示结果下结论。**

---

## §8 注释占比度量

用 Python 脚本统计(区分 KDoc / 行注释 / docstring / 行尾注释),并对可疑结果做朴素方法交叉验证。

### 结果

| 范围 | 文件 | 总行 | 纯注释行 | 空行 | 注释率(含空行)| 注释率(不含空行)|
|---|---|---|---|---|---|---|
| Android / Kotlin | 271 | 64,344 | 7,699 | 3,268 | 12.0% | **12.6%** |
| 后端 / Python | 106 | 23,273 | 222 | 2,510 | 1.0% | **1.1%** |
| 合计 | 377 | 87,617 | 7,921 | 5,778 | 9.0% | **9.7%** |

Kotlin 的 7,699 行中:**KDoc 4,826 行(63%)**、行注释 2,873 行、行尾注释 336 行。
Python 的 222 行中:docstring 62 行、`#` 注释 160 行、行尾 17 行。

### 交叉验证(Python 数字低得可疑,故复核)

| 文件 | 注释行 / 总行 |
|---|---|
| `services/api/app/main.py` | **0 / 163** |
| `services/api/app/api/routes/auth.py` | **0 / 140** |
| `services/api/app/core/security.py` | **0 / 184** |
| `android/.../nav/Routes.kt` | 9 / 258 |
| `android/.../home/Home1Screen.kt` | 56 / 646 |

→ **确认不是统计错误**:后端确实几乎零注释。

### 解读

- **Android 侧 12.6% 属健康区间**,且注释以 KDoc 为主(63%),与项目"§号 + 日期"的注释规范一致。
- **后端 1.1% 显著偏低**。`main.py`(163 行)、`security.py`(184 行)、`auth.py`(140 行)全零注释 —— 而这几个恰恰是**入口与安全相关**代码,最需要说明"为什么这么做"。
- 后端的说明主要沉在 `services/api/docs/*.md`(14 篇)+ OpenAPI,属"文档外置"策略;但这不能替代安全逻辑旁的意图说明。

### 沉淀

1. **先怀疑统计口径,再下结论**:Python 只 1.0% 时我没有直接汇报,而是换一套朴素方法复核 —— 结果证明数字真实,避免了一次错误结论。
2. **注释率要分层看**:两侧差 11 倍,不是"谁更规范",而是规范与文档策略不同。
3. **注释的价值在"为什么"**:后端那三个零注释文件若要补,该补的是决策理由(为什么这样校验、为什么这样限流),而不是复述代码。
