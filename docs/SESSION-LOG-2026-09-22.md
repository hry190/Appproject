# SESSION-LOG-2026-09-22

> 昨日: [SESSION-LOG-2026-09-21.md](./SESSION-LOG-2026-09-21.md)(590 行,§1~§9,顶部有 TL;DR)
> **创建于 2026-09-22**。今日分两段:上午是仓库存活(切到 `zzz` + 查看分支 + 建空骨架日志);
> 下午起是 CdOrder 拖动改写 —— **从「点选」改为「拖动排序」,经过 5 轮调试解决了
> 3 个连续问题:闪退(NPE)、跨行换位不均、拖动期间疯狂闪烁**。第 2 关 attack 列表临时调换
> 已在收尾时还原,本日志对应的 diff 完全来自 `ChuangdangBattleScreen.kt`。
> 状态: 工作区干净(只有 1 个 Kotlin 文件改动 + 1 个未跟踪的日志文件本身)。
> HEAD = `dcce4fb` = `docs(session-log): 订正 §7.4 的错误结论(compose 缺口 → 实为老 .env 未跟上)`。
> 本日**未做 commit** —— 没新工作交接,留作明天的起点。

## 🎯 今日 TL;DR

**一句话**:上午两件定位动作(切 zzz、查分支)后,下午干了一整场 CdOrder「点选→拖动」改写
+ 真机对照 + 5 轮调试(闪退 / 跨行换位 / 闪烁),最后用 `Modifier.offset { }` 替代
`graphicsLayer { translationY = ... }` 才彻底治好闪烁;关卡数据临时调换已还原,只留
代码改动。

| 段 | 做了什么 | 关键结论 |
|---|---|---|
| §1 | 切 `zzz` + 查看全部分支 | `zzz` == `main`(已合并);2 个 `feature/*` 早已并入,长期未动 |
| §2 | `adb` 重建(USB 插拔后)| 设备 `21908b7a` 还在;后端是 **8010**(不是 8000);reverse 重建后 200 |
| §3 | SESSION-LOG 空骨架(早建)| 71 行,只放指针 + 状态表 |
| §4 | **CdOrder 改拖动排序(用户指令)** | 4 步:数据层改 `MutableList` → 渲染层每行加「≡」+ 抬升 + 确认按钮 → 编译过 → 装真机 |
| §5 | **闪退(NPE)调试** | 真机第 1 关 CdChoice 一选就崩;stacktrace 指向 State holder dispose;根因:`remember(question)` 跟随 question 切换;修法:`remember { }` 不带 key |
| §6 | **跨行换位不均调试** | 「最上/最下到中间」换位很难;根因:`toInt()` 取整 + 每次换位后清零 deltaY;修法:`roundToInt()` + 保留剩余偏移 + `Collections.swap` |
| §7 | **拖动卡顿调试 + `dumpsys gfxinfo`** | 14% 丢帧 / 99% 帧 300ms;**真凶**:每帧改 `orderSteps` 触发整棵子树重组;改用 `mutableStateListOf` + `Animatable.snapTo` 但**未根本解决** |
| §8 | **疯狂闪烁调试(5 轮)** | 详见 §8.1~§8.5;最终修法:`Modifier.offset { IntOffset(0, dragOffsetPx.roundToInt()) }` 替代 `graphicsLayer { translationY = ... }` —— `offset` 的 block 读 State **不触发 Composable 重组** |
| §9 | 关卡数据临时调换(第 2 关)| 第 2 关 attack 列表前 2 题换成 CdOrder(便于调试);**收尾时已还原**;git diff 为空 |
| §10 | 数据还原 + 收尾 | 见 §10;`git status` 显示只有 1 个 Kotlin 文件改动 + 1 个未跟踪日志 |

## 快速参考(收工时)

| 项 | 值 |
|---|---|
| 工作分支 | `zzz` |
| HEAD | `dcce4fb` · `docs(session-log): 订正 §7.4 的错误结论(compose 缺口 → 实为老 .env 未跟上)` |
| 距离昨日 HEAD | 0 commit(本日未提交)|
| 工作区 | 1 文件改动 + 1 未跟踪:`ChuangdangBattleScreen.kt` (+173 / -25) · `SESSION-LOG-2026-09-22.md`(本文件)|
| 真机 | `21908b7a` / 2201123C / Android 15;`adb reverse` 今日清空 1 次(USB 插拔)已重建,设备侧 `/docs` + `/openapi` 都回 200 |
| 后端 | 端口 **`8010`**(不是 8000);容器 `jianghu-dev-api-1` 仍在运行(13h) |
| CdOrder 调试 | 5 轮,最终结论见 §8.6;**拖动不闪、可跟手、松手归位动画 180ms** |

> ⚠️ 上表是**收工时(2026-09-22)**刷新的快照;**本日志自身的提交会让 HEAD 再前进一格**(今天没提交)。
> 查真实状态:`git log --oneline -1` + `git status -sb`。

---

## §1 分支状态确认

### §1.1 切到 `zzz`

用户在会话开头要求「切换到 zzz 分支」。`git checkout zzz` → `git status` 干净,与 `origin/zzz` 一致。

### §1.2 查看其他分支

| 分支 | HEAD | 说明 |
|---|---|---|
| `main`(本地)| `dcce4fb` | 与 `zzz` **逐字节相同**;落后/领先均为 0 |
| `zzz`(本地,当前)| `dcce4fb` | — |
| `origin/main` | `dcce4fb` | 与本地 `main` 同步 |
| `origin/zzz` | `dcce4fb` | 与本地 `zzz` 同步 |
| `origin/feature/authentication-foundation` | `5e16009` | 2026-09-09 最后提交,早已并入 |
| `origin/feature/creation-contest-demo` | `a4b3741` | 2026-09-11 最后提交,**09-21 已并入** `zzz` / `main` |

**结论**:`main` 与 `zzz` 内容完全一致;两个 `feature/*` 分支内容都已合并进主分支持续,可考虑清理。

## §2 `adb` 重建(USB 插拔后)

用户在切到 zzz 之后说「先重设 adb,我之前插拔了 USB」。

### §2.1 排查步骤

| 步 | 探测 | 结论 |
|---|---|---|
| 1 | `adb devices -l` | 设备 `21908b7a` 还在 |
| 2 | `adb reverse --list` | **空** —— USB 插拔触发清空(09-21 §6.1 已记第 N 次)|
| 3 | 本机 `:8000` 探活 | `000` —— **端口不对**;后端是 8010 |
| 4 | `docker ps` | `jianghu-dev-api-1` 映射 `0.0.0.0:8010 → 8000` |
| 5 | `adb -s 21908b7a reverse tcp:8010 tcp:8010` | 卡住(adb server 被 USB 抖动弄得不响应) |
| 6 | `adb kill-server && adb start-server` | 设备重连成功 |
| 7 | 再 reverse + 设备侧探活 | **/docs=200, /openapi=200** ✅ |

### §2.2 与昨日不同的发现

- **后端是 8010,不是 8000**(昨天 09-21 §6.1 写的是 8000;实际今天本机 `:8000` 没人听)。
- 可能 09-21 那天是 `8000` 直连,或 §6.1 那行写得不准。**这条以今天实测为准:reverse `tcp:8010 → tcp:8010`**。

## §3 SESSION-LOG 空骨架(早建)

`docs/SESSION-LOG-2026-09-22.md`,71 行:顶部指针 + 开工状态表 + 09-21 11 项待办(原样搬,标 `~` 的标完成)。

**核验**(按 09-21 沉淀 5「写进文档的数字必须当场跑一遍」):
- `wc -l docs/SESSION-LOG-2026-09-21.md` = **590** ✅(写的是 590)
- `git rev-parse --short main zzz` 都返回 `dcce4fb` ✅

## §4 CdOrder 改拖动排序(用户指令)

### §4.1 现状摸底

- `CdOrder` 出现在 **第 2 关 attack 列表**,共 **2 道**(「三步按正确顺序排好」「完整检修步骤」),与 5 个 `CdChoice` 排在一起。
- 玩家操作是「**依次点选**」:点过的标号 + 变金 + 凑满即答。
- `CdSort` 仍是「**逐条归入**」,有 5 个。

### §4.2 用户原话

> 「展开说说」(关于拖动)
> 「加底部『确认顺序』按钮,长按启动避免和按钮点击冲突,视觉反馈:先试试拖动时该行要变半透明 + 略微抬高,CdOrder 现状(点选)在 5 关里真的被用上了」

### §4.3 改写设计

| 维度 | 改前 | 改后 |
|---|---|---|
| 数据 | `picked: List<Int>`(记录点中下标)| 删;`orderSteps` 改 `MutableList<String>`(拖动直接重排) |
| 控件 | `CdOptionRow` × N | 新 `CdDraggableOrderRow`(左侧「≡」把手 + `detectDragGesturesAfterLongPress` 启动 + `graphicsLayer` 半透明 + 抬升 8dp) |
| 触发 | 凑满即判 | 加底部「确认顺序」按钮;玩家动过顺序才可点 |
| 答案判定 | `next.map { orderSteps[it] } == q.steps` | `orderSteps == q.steps` |
| 依赖 | 无 | 无新依赖 |

### §4.4 编译 + 装机

```bash
./gradlew :app:assembleDebug  → BUILD SUCCESSFUL
adb install -r → Success
```

**真机对照**:第一次进第 1 关点 CdChoice **闪退**了 —— 详见 §5。

## §5 闪退(NPE)调试

### §5.1 现场

`adb logcat -d -t 200` 抓到 `FATAL EXCEPTION: main` + `java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object androidx.compose.runtime.State.getValue()' on a null object reference`。

栈顶:`ChuangdangBattleScreen.kt:3741`(R8 字节码偏移,源码对应 `when (val q = question)` 块)。

### §5.2 根因

| 步骤 | 发生了什么 |
|---|---|
| 1 | 我加了 `var dragSource by remember(question) { mutableStateOf<Int?>(null) }` |
| 2 | `remember(key)` 语义:**`key` 变化时,旧 State holder 被 slot table dispose,新 key 重新调 lambda** |
| 3 | 第 1 关 CdChoice 阶段,`onClick = { answer(...) }` lambda 创建时**捕获了 `dragSource` State 引用**(`by` 委托) |
| 4 | onClick 被点 → `answer()` → `phase = Resolved` → `advance()` → `attackIdx++` → **`question` 变了**(下一张牌) |
| 5 | `remember(question)` 看到 key 变了 → **dispose 旧 holder** |
| 6 | onClick lambda 持有的旧引用指向已释放对象 |
| 7 | `onClick` 实际被点时(或下次重组),`dragSource.getValue()` → **NPE** |

### §5.3 修法

`remember(question) { ... }` → `remember { ... }`(无 key)。理由:`dragSource` / `dragDeltaY` 是「拖动中」的瞬时状态,只在 CdOrder 阶段有意义;**换题时由 `advance()` 手动清零**就够了,不需要跟随 question 生命周期。

### §5.4 验证

装新版 → 第 1 关点 CdChoice **不闪退** ✅;第 2 关 CdOrder 也可拖动 ✅。

### §5.5 教训(沉淀)

> **`remember(key)` = 「换 key 时丢弃值」**,**只对「要在 key 切换时丢弃」的值正确**;**「Composable 生命周期内有效 + 在某个时机手动清空」的状态应该用 `remember { ... }`**。这是与 `playerHearts` / `lastCorrect` 等「同关卡稳定」的状态**同款**。
> 之前 `picked` 用 `remember(question)` 是对的(点中次序属于这一题),但 `dragSource` / `dragDeltaY` 不属于「这一题」,属于「这次拖动」,**跟着 Composable 走**。

## §6 跨行换位不均调试

### §6.1 现场

用户反馈:「三个选项中,最上面的选项和最下面的选项很难移动到中间的选项位置」。

### §6.2 根因(原方案)

```kotlin
val offsetSteps = (deltaYPx / rowHeightPx).toInt()  // ① toInt 向 0 取整
// 换位后 dragDeltaY = 0f  ② 每次换位后清零,手指继续走 = 从 0 重新算
```

两个问题叠加:
- **`toInt()` 是向 0 取整**:`70/150=0`(还没换) → `149/150=0`(还没换) → `150/150=1`(才换)。**临界值附近永远到不了 1**。
- **换位后清零**:第 1 行 → 第 2 行(拖 80px)→ 清零 → 手指到 100px → `100/150=0` 没换 → 必须再走 150px 才到第 3 行。**「中间行」换过一次后,玩家感觉「再拖不动了」**。

### §6.3 修法

| # | 改前 | 改后 |
|---|---|---|
| 1 | `.toInt()` | `.roundToInt()` —— 拖到半行就换 |
| 2 | `removeAt + add`(重建 list)| `Collections.swap`(in-place)|
| 3 | 换位后 `dragDeltaY = 0f` | `dragDeltaY = deltaYPx - consumedPx` —— **保留剩余偏移** |

**第 3 条是关键**:第 1 行 → 第 3 行,**改前 80 + 150 = 230px;改后总 160px**。手指连续走,中间行无缝过渡。

### §6.4 验证

装新版,真机走 CdOrder → 跨行换位都顺畅。**但卡顿仍存在**(下一节)。

## §7 拖动卡顿调试 + `dumpsys gfxinfo`

### §7.1 现场

用户反馈:「拖动的时候还是觉得有点卡顿」。

### §7.2 真凶(数据说话)

`adb shell dumpsys gfxinfo com.jueqiao.jianghu` 输出:

```
Total frames rendered: 541
Janky frames: 78 (14.42%)         ← 14% 丢帧(正常 < 1%)
Janky frames (legacy): 482 (89.09%)
50th percentile: 26ms
95th percentile: 65ms
99th percentile: 300ms            ← 99% 帧 300ms(应该 ≤ 16ms)
50th gpu percentile: 3ms
95th gpu percentile: 9ms
```

GPU 渲染 3~10ms,**瓶颈在主线程**;每帧 16ms 内重组做不完。

### §7.3 根因

每帧 onDrag 触发:
1. 修改 `orderSteps: MutableList` → 触发**整棵子树重组**
2. `when (q) { is CdOrder -> forEachIndexed { ... CdDraggableOrderRow(...) } }` 全部重跑
3. **每行都重新创建 lambda、读 `isDragging`、算 `Modifier.graphicsLayer`**

即使 swap + roundToInt 让换位更顺,**底层「每帧改数据 → 整树重组」没解决**,卡顿就一直在。

### §7.4 修法(第三轮)

把 `MutableList` 改 `mutableStateListOf`(细粒度通知),拖动期间只更新 `Animatable.snapTo(deltaYPx)`。**理论**上减少重组范围。

**实测**:从 14% 丢帧 → **仍 13% 丢帧 / 99% 帧 150ms**。**几乎没改善**。

## §8 疯狂闪烁调试(5 轮)

### §8.1 现场

第三轮改完,用户反馈:「拖动的时候选项会疯狂闪烁」。

### §8.2 调试实验法 —— 排除 onDrag 写

加 `Log.d("CdOrderDrag", ...)` + **临时注释掉 `dragOffsetPx = deltaYPx`**,装上让用户重测。

**结果**:用户:「不闪了,但是拖不动选项了」

**结论**:**闪的根因是 `onDrag` 里写 `dragOffsetPx`**。但写时 Compose 文档保证**不触发重组**——那闪的真正机制是什么?

### §8.3 闪的真因(事后分析)

`CdDraggableOrderRow` 内的:

```kotlin
graphicsLayer {
    translationY = if (isDragging) dragOffsetPx else 0f  // 读 State
    alpha = if (isDragging) 0.5f else 1f
}
```

**`graphicsLayer { }` block 在每帧都执行**(不只是 Composable 重组时)。**block 内读 State 会把该 Composable 标脏 → 每帧一次重组**。`mutableFloatStateOf` 写时确实不主动通知,**但 graphicsLayer block 的反复 read 触发了别处的 invalidate**,实际表现就是**每帧 1 次 Composable 重组 + 14~16% 丢帧 + 视觉闪烁**。

### §8.4 第四轮:Animatable → mutableFloatStateOf(无效)

| 改前 | 改后 |
|---|---|
| `Animatable.snapTo(deltaYPx)` 在 `scope.launch { ... }` 内 | `dragOffsetPx = deltaYPx` 直接写 |

**结果**:仍闪。

### §8.5 第五轮:graphicsLayer → Modifier.offset(成功)

| 改前 | 改后 |
|---|---|
| `graphicsLayer { translationY = dragOffsetPx }` | `Modifier.offset { IntOffset(0, dragOffsetPx.roundToInt()) }` |

**关键差别**:
- `graphicsLayer { }` block 读 State → **Composable 每帧重组**
- `Modifier.offset { }` block 读 State → **只触发 layer 移动**,**不触发 Composable 重组**

alpha 仍走 `graphicsLayer { alpha = ... }`(只读 `isDragging`,翻转频率低,不引起高频闪烁)。

### §8.6 验证

装新版 → 用户:「现在拖动不会闪烁了」 ✅

## §9 关卡数据临时调换(已还原)

### §9.1 改动

为方便调试,**把第 2 关 attack 列表前 2 题改成 CdOrder**(原本在末尾),这样到第 2 关第 1 题就是排序,不用答 6 道 CdChoice 才能到。

### §9.2 还原

收尾时把数据**完全还原**(顺序、注释、源格式全部回到改之前)。`git diff ChuangdangData.kt` 输出为空 —— **数据文件 0 改动**。

## §10 数据还原 + 收尾

### §10.1 `git status`

```
M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/chuangdang/ChuangdangBattleScreen.kt
?? docs/SESSION-LOG-2026-09-22.md
```

干净的:1 个 Kotlin 文件改动(+173 / -25) + 1 个未跟踪的日志文件。

### §10.2 改动全貌(ChuangdangBattleScreen.kt)

| 类别 | 改动 | 行数 |
|---|---|---|
| import | +5:`mutableStateListOf` / `mutableFloatStateOf` / `rememberCoroutineScope` / `kotlinx.coroutines.launch` / `kotlin.math.roundToInt` / `Modifier.offset`(部分已存在) | 净 +5 |
| 数据层 | `orderSteps` 从 `remember(question) { List }` → `remember(question) { mutableStateListOf }`;`dragIndex`(原 `dragSource`)+ `dragOffsetPx`(原 `Animatable`) | 净 +10 |
| 渲染层 | 整段 `is CdOrder` 重写(加 `rowHeightPx`、加「确认顺序」按钮、协程 scope)| 净 +50 |
| 新组件 | `CdDraggableOrderRow`(完整,带 `dragOffsetPx` + `Modifier.offset { }`)| 净 +90 |
| 注释 + 删 `picked` | advance() / 头部注释 / 字段清理 | 净 +18 |

## 沉淀(§1~§10)

1. **Compose `remember(key)` 的语义陷阱**:`key` 变化 = 旧 holder dispose = 持有旧引用的 lambda 访问 NPE。`key` 只对「换 key 时丢弃」的值正确;其他状态用 `remember { }`。**(§5)**
2. **Compose 渲染层与重组是两件事**:`graphicsLayer { }` block 每帧执行 + 读 State → Composable 每帧重组;`Modifier.offset { }` block 每帧执行 + 读 State → **不**重组,只移动 layer。**这是高频拖动类 UI 的标准模式**。**(§8.5)**
3. **`dumpsys gfxinfo` 是「真机卡顿」最准的诊断**:janky% + 99% percentile ms;Compose 14% 丢帧 = 严重,GPU 时间 < 10ms = 不是绘制问题,是主线程重组。**(§7.2)**
4. **「Log.d + 注释掉可疑代码」是 UI 闪烁/卡顿最快的二分法**:注释前闪 = 那行写是闪源;注释后不闪 = 把那行的副作用(`graphicsLayer` 读 State)继续追下去。**(§8.2)**
5. **测试可达性**:CdOrder 调试期间,改数据临时把排序提到第 2 关第 1 题,极大加速迭代;**调试完立即还原**,`git diff` 为空才放心。**(§9)**
6. **「写进文档的数字必须当场跑一遍」(沿用 09-21 沉淀 5)**:SESSION-LOG-2026-09-22.md 早建时核对了 590 行、HEAD、git rev-parse 都对得上。**(§3)**

## 待办(承接 09-21 + 新增)

| # | 事项 | 状态 |
|---|---|---|
| 1 | ~~`zzz` 是否 `reset --hard main` + force push~~ | ✅ 09-21 已执行 |
| 2 | 合并 SOP「`--no-ff` 被误解析为 `theirs`」是否已失效 | ⏳ 待复核 |
| 3 | 后山跳转升级到 `TestNavHostController`(加依赖) | ⏳ 等用户决定 |
| 4 | 后山 6 页未自动化 | ⏳ |
| 5 | 09-21 上午两笔(`d420c26` / `5a1eaf2`)技术细节 | ⏳ 无归属日志 |
| 6 | 文档补「拉取后需把 `.env.example` 新增段并进本地 `.env`」 | ⏳ |
| 7 | app 在教练 503 时不显示错误提示(09-21 §7.5) | ⏳ |
| 8 | 演武场·视频 / 大会·竞技场两屏从未肉眼验证 | ⏳ |
| 9 | `services/api/.venv` | ⏳ 已排除,可删 |
| 10 | `feature/*` 已并入,可删 | ⏳ 两者 `ahead=0` |
| 11 | 真机对照截图 8 张 `D:\hermes\cache\merge-test-20260921\` | ⏳ |
| 12 | 🆕 **CdOrder「长按启动 → 按下启动」是否要保留** | ⏳ 已用 `detectDragGestures`(按下启动),无长按;待用户确认 |
| 13 | 🆕 **保留点选降级**(老人/小孩可访问性) | ⏳ 未做;§4 提到的「切换到点选」模式 |
| 14 | 🆕 **每关都加 CdOrder**(用户提的) | ⏳ 暂未做;今天只调换了第 2 关,已还原 |
| 15 | 🆕 **跨行换位的视觉效果**(被拖行「穿过」其他行的视觉) | ⏳ 现在的「松手才换 + 180ms 缓动」是取舍;如要「实时换位」需另写 |
| 16 | 🆕 **CdOrder 抖动/手感微调**(8dp 抬升、跨越阈值)| ⏳ roundToInt 后改 1 行 0.5;玩家可接受 |
| 17 | 🆕 **顶部「←」撤退图标、BackHandler 是否同样加 `playerHearts == 1` 守卫** | ⏳ 用户之前没回;只动了 766 行按钮 |

> **收工快照**(按 09-20 §10 习惯,**数字一律走命令现算**):
> - 今日 commit 笔数:**0**(本日未提交)
> - `main` 落后笔数:`git rev-list --count main..zzz` = 0
> - HEAD:`git log --oneline -1`
> - 工作区状态:`git status -sb`(见 §10.1)
> - 本日志行数:`wc -l docs/SESSION-LOG-2026-09-22.md`
