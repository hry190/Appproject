package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlin.random.Random

/**
 * 交手时放哪一种一次性动画(2026-09-20 §8,§9 扩充为"双方都会动")。
 *
 * 设计约束(用户定的口径 + 项目既有教训):
 *   · **一次性**,不循环 —— 循环动画会让 `uiautomator dump` 等不到 idle,而"每帧配 dump"是像素回归的地基;
 *   · **不整屏震** —— 文字区域是这个项目最脆的地方(§10/§12/§19 三次修的都是文字),震屏会一起晃到它;
 *   · **不改交互节奏** —— 动画在结算面板底下播,不加"等动画放完才能点继续"的门。
 *
 * 命名从"谁的动作"来(§9 改的名):原先 Lunge / Block / TakeHit 只描述了熊猫那一侧,
 * 而 §9 之后**怪物也会主动扑击**,读名字要先知道"这是谁在动"才不会改错。
 */
private enum class CdFx {
    None,
    /** 进攻命中:熊猫前冲,怪物挨打后仰。 */
    PandaStrike,
    /** 进攻被格挡:熊猫只前顶一点点(不放"出手+受击",那会谎报战果)。 */
    PandaGraze,
    /** 怪物扑击:反击起手 / 防御题起手 —— **只有怪物动**,熊猫不动。 */
    MonsterJab,
    /** 防御答对:怪物扑上来**被挡回**,熊猫挡一下并被顶退一点。 */
    MonsterBlocked,
    /** 防御答错:怪物扑上来**命中**,熊猫受击后退。 */
    MonsterHits,
}

/**
 * 2026-09-19 §6 闯荡江湖 · 战斗页(第 1~4 关的三心攻防)
 *
 * 依据策划方案 v2 §3「前四关的三心攻防规则」:
 *   §3.1 对战画面与生命 —— 熊猫在左、怪物在右,双方头顶各三颗心;每关开始时双方恢复三颗心;
 *        一次有效攻击造成一颗心伤害;上一关剩余生命**不带入**下一关。
 *   §3.2 每回合的判定顺序(逐步实现如下):
 *        ① 怪物提出一个问题,玩家完成进攻作答
 *        ② 进攻答对 → 熊猫攻击;怪物未格挡则扣一颗心,格挡成功则本次不扣心
 *        ③ 进攻答错 → 怪物攻击,玩家获得一次防御作答机会
 *        ④ 防御答对 → 挡下攻击;防御答错 → 玩家扣一颗心
 *        ⑤ 展示关键解释,若双方仍有生命则进入下一回合
 *   §3.3 怪物格挡递减 —— 本场累计答对第 1/2/3/4 次,格挡率 30%/20%/10%/0%;防御题答对不计入。
 *
 * 用户指令:"先把页面做出来,先不接后端" —— 题目来自 [CD_STAGES] 内置题库,
 * 格挡用本地随机数,胜负只写 [ChuangdangStore]。
 *
 * @param stageIndex 关卡序号(1~4)
 */
data class ChuangdangBattleActions(
    /**
     * 退出战斗回到地图(主动撤退 / 战败 / 通关都走这里)。
     *
     * 2026-09-20:把原本只一个 onExit 拆成 4 个明确语义的回调 —— 因为
     * 「主动撤退后是否要继续战斗」需要细分(策划 §9 「普通关失败」给出三选项:
     * 前往补修 / 免费练习 / 重新出发)。练习模式与战败/胜利路径继续走 [onExit]。
     */
    val onExit: () -> Unit = {},
    /** 主动撤退后再点「继续练习」(策划 §9):回到本关、practice=true。 */
    val onPracticeSame: () -> Unit = {},
    /** 主动撤退后再点「重新出发」(策划 §9):从当前关重新开始正式战斗。 */
    val onRestartSame: () -> Unit = {},
    /** 主动撤退后再点「前往补修」(策划 §9):跳到修学/补修页面。 */
    val onGoReview: () -> Unit = {},
    /**
     * 胜利后点「继续挑战 下一关」:直跳到下一关(stage 1→2→3→4→Boss)。
     *
     * 策划 §6.1:「通过普通关后继续下一关不额外扣令」(runActive 期间,continue 不消耗)。
     * 第 5 关(Boss)之后无下一关,BossScreen 不会传这个回调 —— 字段可空,
     * UI 见 [CdPhase.Victory] 分支:**有回调**时显示「继续挑战 下一关」主按钮。
     */
    val onGoNext: (() -> Unit)? = null,
)

private val BInk = Color(0xFF2E2A24)
private val BInkSoft = Color(0x992E2A24)
private val BCardBg = Color(0xF2FFFFFF)
private val BGold = Color(0xFFB8894A)
private val BCorrect = Color(0xFF3F6B3A)
private val BWrong = Color(0xFF9B3B2E)

/**
 * 选项文本框的**最小高度** —— 五个关卡共用这一处。
 *
 * 想整体调高/调矮,只改这个数字即可(2026-09-19 用户要求:在 50dp 基础上 +5dp → 55dp)。
 * 注意:这只是**下限**,文字换行变多时卡片会自动长高(见 [CdOptionRow])。
 */
private val CD_OPTION_MIN_HEIGHT = 55.dp

@Composable
fun ChuangdangBattleScreen(
    stageIndex: Int,
    /**
     * 免费练习模式(文档 §2):**不消耗闯荡令、可看提示、通关不解锁正式节点**。
     * 由地图页的「免费练习」入口传入 true。
     */
    practiceMode: Boolean = false,
    actions: ChuangdangBattleActions = ChuangdangBattleActions(),
) {
    val stage = CD_STAGES.firstOrNull { it.index == stageIndex } ?: CD_STAGES.first()

    // ── 战斗状态(文档 §3.1/§3.2)────────────────────────────────
    var playerHearts by remember { mutableIntStateOf(CD_MAX_HEARTS) }
    var enemyHearts by remember { mutableIntStateOf(CD_MAX_HEARTS) }
    var correctCount by remember { mutableIntStateOf(0) }   // 本场累计答对进攻题次数(§3.3)
    var attackIdx by remember { mutableIntStateOf(0) }
    var defenseIdx by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(CdPhase.Attack) }
    /**
     * 本题是否已作答、以及答得对不对(null = 还没作答)。
     *
     * 2026-09-19 §15:原先这里是 `selected: Int?`(选项下标),只够点选题用。
     * 引入排序 / 分类后,"选了什么"不再是一个下标,故这里只记**判定结果**;
     * 各交互自己的中间状态由下方 `picked` / `assigned` 持有。
     */
    var lastCorrect by remember { mutableStateOf<Boolean?>(null) }
    var pendingDefense by remember { mutableStateOf(false) } // 进攻答错 → 结算后转入防御题
    var blocked by remember { mutableStateOf(false) }
    // 2026-09-19 §6:文档 §3.3 的**保底** —— "一次攻击被格挡后,下一次正确攻击必定命中,
    //   即使期间出现答错回合"。故该标志只在"兑现命中"时清除,答错/防御作答都不影响它。
    var pendingGuaranteedHit by remember { mutableStateOf(false) }
    var headline by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    // ── 2026-09-20 §8:交手动画(出手 / 格挡 / 受击);§9 起**怪物也会主动扑击** ──────────
    //   为什么用"种类 + 计数器"两个状态:同一种动画连续触发时(连打两下都是同一种),
    //   只改种类不会让 LaunchedEffect 重跑 —— 必须让 tick 变。
    var fxKind by remember { mutableStateOf(CdFx.None) }
    var fxTick by remember { mutableIntStateOf(0) }
    /**
     * 一次性冲量,取值不限于 [0,1]:**负半程用来表达"被顶回去/被挡回"**。
     *
     * 为什么一个 Animatable 就够(§9 的设计要点):扑击那一拍需要**双方同时**动 ——
     * 怪物扑上来(−12dp)又被挡回(+6dp),熊猫挡一下(+4dp)再被顶退(−2dp)。
     * 若给两路各一个 Animatable,就得并发跑两段动画(`launch` + `join`);
     * 而这两条曲线**形状相同、只差系数**:同一段 1 → −0.5 → 0 的冲量,
     * 熊猫取 +4dp/单位、怪物取 −12dp/单位,正好得到 +4→−2 与 −12→+6 —— 一次动画、两条曲线。
     */
    val fx = remember { Animatable(0f) }
    // ⚠️ 这里**不要**自己去读 `Settings.Global.ANIMATOR_DURATION_SCALE` 再乘一遍时长:
    //   Compose 的 `WindowRecomposer` 已经把系统这个倍率注入 `MotionDurationScale`,
    //   所有 tween 都会被框架自动缩放(源码:`WindowRecomposer_androidKt` 里读的正是
    //   `animator_duration_scale`)。手动再乘一次就是**乘两遍**。
    //   实测(§8):系统倍率 =10、代码里也 ×10 时,一条 120+170ms 的动作在真机上放了 **29 秒**
    //   (= 2.9s × 10),取证时被误判成"熊猫一直在漂"。所以这里只写本来的时长:
    //   · 系统倍率 = 1(默认)→ 就是下面写的毫秒数;
    //   · 系统倍率 = 0(开发者选项关掉动画)→ 框架把动画时长归一为 0,动作直接不播(符合无障碍预期)。
    LaunchedEffect(fxTick) {
        if (fxTick == 0) return@LaunchedEffect
        fx.snapTo(0f)
        when (fxKind) {
            // 出手:冲出去快、收回来略慢(怪物挨打后仰是**同一拍被动发生**的,见下面映射表)
            CdFx.PandaStrike -> {
                fx.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
                fx.animateTo(0f, tween(140, easing = LinearOutSlowInEasing))
            }
            // 被格挡:短促一顿,没有后半程
            CdFx.PandaGraze -> {
                fx.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
                fx.animateTo(0f, tween(110, easing = LinearOutSlowInEasing))
            }
            // 怪物扑击(反击起手 / 防御题起手):扑得快、收得慢一点
            CdFx.MonsterJab -> {
                fx.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
                fx.animateTo(0f, tween(200, easing = LinearOutSlowInEasing))
            }
            // 扑上来 → **被挡回**(负半程)→ 归位:熊猫在这里"顶了一下又被顶退"
            CdFx.MonsterBlocked -> {
                fx.animateTo(1f, tween(130, easing = FastOutSlowInEasing))
                fx.animateTo(-0.5f, tween(90, easing = FastOutSlowInEasing))
                fx.animateTo(0f, tween(180, easing = LinearOutSlowInEasing))
            }
            // 扑上来命中:熊猫被打退得快、晃回来更慢
            CdFx.MonsterHits -> {
                fx.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
                fx.animateTo(0f, tween(170, easing = LinearOutSlowInEasing))
            }
            CdFx.None -> return@LaunchedEffect
        }
    }
    // 位移/缩放映射:一次性动画只碰 offset / scale,**不动尺寸、不动文字、不整屏震**。
    //   系数 × 冲量 = 这一拍的位移;冲量走负半程时系数不变,符号自然翻转("被顶退/被挡回")。
    val pandaFxDp = when (fxKind) {
        CdFx.PandaStrike -> 14f * fx.value        // 出手前冲
        CdFx.PandaGraze -> 4f * fx.value          // 被格挡:只前顶一点点
        CdFx.MonsterJab -> 0f                     // 怪物扑击时熊猫不动
        CdFx.MonsterBlocked -> 4f * fx.value      // +4dp 顶住 → 负半程 −2dp 被顶退(用户 3a)
        CdFx.MonsterHits -> -10f * fx.value       // 受击后退(用户 3b:保持 -10dp)
        CdFx.None -> 0f
    }
    val monsterFxDp = when (fxKind) {
        CdFx.PandaStrike -> -8f * fx.value        // 挨打后仰(被动)
        CdFx.PandaGraze -> 0f                     // 没打中,怪物不动
        CdFx.MonsterJab -> -12f * fx.value        // 扑击(用户 2a)
        CdFx.MonsterBlocked -> -12f * fx.value    // 扑击 → 负半程 +6dp **被挡回**(用户 2a)
        CdFx.MonsterHits -> -12f * fx.value       // 扑击命中(用户 2a)
        CdFx.None -> 0f
    }
    val pandaScaleX = when (fxKind) {
        CdFx.PandaStrike -> 1f + 0.06f * fx.value
        CdFx.PandaGraze -> 1f + 0.02f * fx.value
        CdFx.MonsterJab -> 1f
        CdFx.MonsterBlocked -> 1f + 0.02f * fx.value
        CdFx.MonsterHits -> 1f + 0.02f * fx.value
        CdFx.None -> 1f
    }
    // 只有"挨实了"才压扁(压扁=受击的读法;挡下来不该压扁)
    val pandaScaleY = if (fxKind == CdFx.MonsterHits) 1f - 0.04f * fx.value else pandaScaleX
    // ⚠️ 只在"这一侧真的要动"时才挂 graphicsLayer。
    //   实测:常挂着这一层,静止时也会让熊猫边缘出现 1px 级抖动(两帧静止截图的差异 7.9k px,
    //   而同屏怪物是 **0** px)—— 那会让"素材渲染正确"这条既有结论变成"看起来对"。
    //   条件挂载后,静息态的绘制与加动画之前**逐像素一致**。
    //   §9 两点改动:
    //     · 判据从 `> 0f` 改成 `!= 0f` —— 被挡回那半程冲量是**负的**,用 `> 0f` 会提前摘图层;
    //     · 拆成**两侧各自**判断 —— 怪物扑击时熊猫本来就不动,那一拍熊猫不该挂图层
    //       (这样"1b/1c 时熊猫逐像素不动"才是一条能验证的结论,而不是"差不多没动")。
    val pandaActive = pandaFxDp != 0f || pandaScaleX != 1f
    val monsterActive = monsterFxDp != 0f

    // 2026-09-20:撤退流程拆成两层(策划 §6.4 + §9「普通关失败」三选项):
    //   1. 第一次触发 → [showRetreatDialog] = true,弹「要离开本次出发吗?」两按钮弹窗;
    //   2. 用户点「撤退」→ 关弹窗、endRun()、再开 [showRetreatResult] = true 显示撤离后结果;
    //   3. 用户在结果页选「继续练习 / 重新出发 / 前往补修 / 返回地图」,各走各自回调,然后 [onExit]。
    //   这样:策划 §6.4 说的「弹窗说明后果,确认后结束本次出发」与 §9 三选项**两步合一**,
    //   而不是把「暂离」做成显式按钮(策划原文并无此选项;切后台/关闭/断网 = 暂停是另一条独立规则)。
    //
    // 注意:胜负已分(Victory/Defeat)时不弹这一组 —— 它们走 advance() 直接退出结算面板。
    // 练习模式也没有「出发」可撤退,直接退出。
    var showRetreatDialog by remember { mutableStateOf(false) }
    var showRetreatResult by remember { mutableStateOf(false) }

    // 2026-09-19 §14:关前剧情(文档 §2「从当前未通关节点出发,阅读简短剧情并进入战斗」+
    //   §5 每关的「场景」文案)。数据一直在 CdStage.scene 里,只是此前没有任何 UI 读它。
    //   文档 §2 另要求「已读剧情可跳过」:故**首次**进入才拦在战斗前面,之后重进直接开打,
    //   但仍可从顶部栏「重看剧情」把面板调回来。
    var showIntro by remember { mutableStateOf(!ChuangdangStore.hasReadScene(stage.index)) }

    val requestExit: () -> Unit = {
        if (practiceMode) actions.onExit() else showRetreatDialog = true
    }
    // 返回键先关剧情浮层;从浮层退出不算"已读",下次进来仍会展示。
    BackHandler {
        if (showIntro) showIntro = false else requestExit()
    }

    // 文档 §3.4:每关准备同知识点的情境变体,避免练习和正式挑战机械重复同一题。
    //   进入关卡时把题库洗一次,本场按洗后的顺序取题 —— 重玩时题目与顺序都可能不同。
    val attackDeck = remember(stage.index) { stage.attack.shuffled() }
    val defenseDeck = remember(stage.index) { stage.defense.shuffled() }

    val question = when (phase) {
        CdPhase.Defense -> defenseDeck[defenseIdx % defenseDeck.size]
        else -> attackDeck[attackIdx % attackDeck.size]
    }

    // 2026-09-19 §6:选项顺序打散。
    //   原先进攻题的正确答案固定在索引 1、防御题固定在索引 0 —— 玩两关就能摸出套路,
    //   知识判断会退化成"记位置"。这里对每道题洗一次牌;
    //   用 remember(question) 保证同一题在重绘(结算前后)时不会重新打散,否则答案位置会跳。
    //   仅点选题需要;排序 / 分类题的打散在各自的交互组件里做(见 §15)。
    val (shownOptions, shownCorrectIndex) = remember(question) {
        if (question is CdChoice) {
            val indexed = question.options.mapIndexed { i, text -> i to text }
            val order = indexed.shuffled()
            order.map { it.second } to order.indexOfFirst { it.first == question.correctIndex }
        } else {
            emptyList<String>() to -1
        }
    }

    // ── 排序题(§15):打乱后的展示顺序 + 玩家已点中的次序 ──────────────────
    val orderSteps = remember(question) {
        if (question is CdOrder) question.steps.shuffled() else emptyList<String>()
    }
    /** 玩家依次点中的步骤在 [orderSteps] 中的下标 —— 这个列表的顺序就是作答顺序。 */
    var picked by remember(question) { mutableStateOf<List<Int>>(emptyList()) }

    // ── 分类题(§15):每条当前归入的类别(-1 = 还没归) ────────────────────
    //   注意要显式写 List<Int> —— 两个分支分别是 List<Int> 与 emptyList(),
    //   不标注的话会被推成 List<Nothing>,委托属性直接编译不过。
    var assigned by remember(question) {
        mutableStateOf<List<Int>>(
            if (question is CdSort) List(question.items.size) { -1 } else emptyList(),
        )
    }

    // 练习模式的"看提示"(文档 §2:免费练习可查看提示与秘籍)
    var showHint by remember(question) { mutableStateOf(false) }

    /**
     * 作答 + 按 §3.2 的判定顺序结算。
     *
     * 2026-09-19 §15:入参从"选了第几项"改成"这次答得对不对" —— 三种交互各自判定
     * (点选比下标、排序比次序、分类逐条比类别),状态机这层不需要知道是哪一种。
     */
    /**
     * 2026-09-20 §8/§9:触发一次交手动画。
     *
     * 为什么是"种类 + 计数器"两个状态(而不是只改种类):同一种动作**连续触发**时
     * (例如连打两下都是怪物扑击),只改种类不会让 `LaunchedEffect` 重跑 —— 必须让 tick 变。
     *
     * 用法口径(用户 1a/1b/1c):
     *   · 进攻命中 → `PandaStrike`;进攻被格挡 → `PandaGraze`;进攻**答错** → `MonsterJab`(怪物反击起手);
     *   · 防御题**出现** → `MonsterJab`(在 `advance()` 里,不与作答绑定);
     *   · 防御答对 → `MonsterBlocked`(扑上来被挡回);防御答错 → `MonsterHits`(扑上来命中)。
     */
    val cue: (CdFx) -> Unit = { kind ->
        fxKind = kind
        fxTick += 1
    }

    fun answer(isCorrect: Boolean) {
        if (lastCorrect != null) return
        if (phase != CdPhase.Attack && phase != CdPhase.Defense) return
        lastCorrect = isCorrect

        if (phase == CdPhase.Attack) {
            if (isCorrect) {
                // ② 进攻答对 → 结算伤害(文档 §3.3)
                correctCount += 1
                if (pendingGuaranteedHit) {
                    // 保底兑现:上一次被格挡过 → 这一次必定命中(不受中途答错影响)
                    pendingGuaranteedHit = false
                    blocked = false
                    enemyHearts -= 1
                    headline = "识破一式 · 必定命中!-1 心"
                } else {
                    blocked = Random.nextFloat() < cdBlockRate(correctCount)
                    if (blocked) {
                        // 记下这笔:下一次正确攻击必定命中(文档 §3.3 的保底)
                        pendingGuaranteedHit = true
                        headline = "被格挡了 · 已看穿破绽"
                    } else {
                        enemyHearts -= 1
                        headline = "命中!-1 心"
                    }
                }
                // 被格挡时不放"出手+受击"(那会谎报战果),只给熊猫一个极小的前顶示意
                cue(if (blocked) CdFx.PandaGraze else CdFx.PandaStrike)
                detail = question.explanation
                pendingDefense = false
                phase = if (enemyHearts <= 0) CdPhase.Victory else CdPhase.Resolved
            } else {
                // ③ 进攻答错 → 怪物攻击,稍后给一次防御机会
                headline = "答错了,怪物反击!"
                cue(CdFx.MonsterJab)   // §9 / 用户 1b:面板既然写着"怪物反击",怪物就得真的扑一下
                detail = question.explanation
                pendingDefense = true
                phase = CdPhase.Resolved
            }
        } else {
            // ④ 防御作答
            if (isCorrect) {
                headline = "挡下了!"
                cue(CdFx.MonsterBlocked)   // §9 / 用户 1a+2a+3a:怪物扑上来被挡回,熊猫顶一下再被顶退
                detail = question.explanation
            } else {
                playerHearts -= 1
                headline = "防御失败,-1 心"
                cue(CdFx.MonsterHits)   // §9:怪物扑上来命中,挨打的是熊猫(玩家掉心)
                detail = question.explanation
            }
            pendingDefense = false
            phase = if (playerHearts <= 0) CdPhase.Defeat else CdPhase.Resolved
        }
    }

    /** ⑤ 结算后继续:决定进入防御题还是下一回合。 */
    fun advance() {
        when (phase) {
            CdPhase.Victory -> {
                // 练习模式不解锁正式节点(文档 §2:"不解锁正式通关节点")
                if (!practiceMode) ChuangdangStore.clearStage(stage.index)
                actions.onExit()
            }
            CdPhase.Defeat -> {
                // 练习模式本来就没有开始出发,故不结束别人的出发状态
                if (!practiceMode) ChuangdangStore.endRun()
                actions.onExit()
            }
            CdPhase.Resolved -> {
                // 换题前把作答状态清干净(§15:三种交互各有中间状态,都要重置)
                lastCorrect = null
                picked = emptyList()
                assigned = if (question is CdSort) List(question.items.size) { -1 } else emptyList()
                blocked = false
                if (pendingDefense) {
                    defenseIdx += 1
                    pendingDefense = false
                    phase = CdPhase.Defense
                    // §9 / 用户 1c:防御题**出现**时,怪物先扑一次(蓄势)—— 与"作答结果"无关,
                    //   所以它不在 answer() 里,而在这次相位切换上。一次性动画在题目底下播完即止,
                    //   不加"动画放完才能作答"的门(§8 约束三)。
                    cue(CdFx.MonsterJab)
                } else {
                    attackIdx += 1
                    phase = CdPhase.Attack
                }
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 顶部栏 ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = requestExit,
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_return),
                        contentDescription = "撤退",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "第 ${stage.index} 关 · ${stage.title}",
                    color = BInk,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                )
                Spacer(Modifier.weight(1f))
                // 2026-09-19 §14:已读剧情的关不再拦在前面,但留一个"重看"入口(文档 §2「已读剧情可跳过」)
                Text(
                    text = "重看剧情",
                    color = BGold,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showIntro = true },
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── 对战区:左熊猫 / 右怪物,各带三颗心(§3.1)──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 玩家(熊猫)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CdHearts(alive = playerHearts, tint = Color(0xFFB5453A))
                    Spacer(Modifier.height(8.dp))
                    // 2026-09-20 §7:熊猫少侠改用**用户给的新形象** ——
                    //   设计稿 image 72.png(来自用户桌面)→ img_chuangdang_xiongmaoshaoxia。
                    //   ⚠️ 注意同名不同图:卷10-3 的注释里也有一个 `image 72.png`,那是**另一张**
                    //   (导出工具自动命名,不同批次会撞名);分辨看资源名与所在文件。
                    //   旧图 img_shilian2_recovered_8 是 362×192(**横构图 1.885**),槽位 104×56dp 与它几乎同比例;
                    //   新图是 306×584(**竖构图 0.524**,主体占满画布),沿用旧槽位只会按宽适配出 29×56dp 的一条细缝
                    //   —— 故槽位一起改成竖的:**52×96dp**,按高适配出 50.3×96dp(与右侧怪物同高 96dp,对战高度齐平)。
                    //   真机实测:位置 (53,329)、渲染 138×264px = 50.3×96dp、缩放扫描最优 1.00(比例正确)、
                    //   顶部离血条 8dp(与 Spacer 一致,无重叠)。
                    //   ⚠️ 旧图在**后山 3/5/7/9/11** 五页仍用作「熊猫」(带云动画),本次**没动**它们。
                    Image(
                        painter = painterResource(R.drawable.img_chuangdang_xiongmaoshaoxia),
                        contentDescription = "熊猫少侠",
                        // 2026-09-20 §8/§9:出手 / 格挡 / 受击 / 被顶退的位移与缩放都走 graphicsLayer ——
                        //   它**只影响绘制,不影响布局**(槽位 52×96dp 不变),所以不会把旁边的心/文字挤动。
                        //   且**只在熊猫这一侧真的要动时挂载**(见 pandaActive 的注释):静息态逐像素与加动画前一致。
                        modifier = Modifier
                            .size(width = 52.dp, height = 96.dp)
                            .then(
                                if (pandaActive) Modifier.graphicsLayer {
                                    translationX = pandaFxDp.dp.toPx()
                                    scaleX = pandaScaleX
                                    scaleY = pandaScaleY
                                } else Modifier,
                            ),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        text = "熊猫少侠",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Text(
                    text = "VS",
                    color = BGold,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                )

                // 敌人
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CdHearts(alive = enemyHearts, tint = Color(0xFF6B5B8A))
                    Spacer(Modifier.height(8.dp))
                    // 命中数驱动「战斗表现」(文档 §5):每打掉一颗心,怪物形象随之变化 ——
                    // 身上多一道爪痕 / 声波环多一圈 / 脚下棋格少一块 / 一只纸鹤脱离阵形
                    //
                    // 2026-09-19 §17:槽位由方形 96dp 改为 **132×96dp**。
                    //   素材长宽比不一(铜齿门卫 1:1、机关蝠 3:2),方形槽位会让横构图
                    //   只能按宽适配、显得很矮。给一个略宽的槽位后:
                    //     · 1:1 素材 → 按高适配,仍是 96×96,**外观不变**
                    //     · 3:2 素材 → 按宽适配,由 96×64 变成 132×88,**大了一圈**
                    //   手绘敌人按 min(w,h) 归一化,槽位变宽对它们**没有影响**。
                    CdMonster(
                        glyph = stage.enemyGlyph,
                        hitCount = CD_MAX_HEARTS - enemyHearts,
                        // 2026-09-20 §8/§9:挨打后仰、以及**主动扑击 / 被挡回**,都只挪绘制不挪布局
                        //   (命中叠加层跟着一起动);**只在怪物这一侧真的要动时挂载**,静息态不受影响。
                        modifier = Modifier
                            .size(width = 132.dp, height = 96.dp)
                            .then(
                                if (monsterActive) Modifier.graphicsLayer {
                                    translationX = monsterFxDp.dp.toPx()
                                } else Modifier,
                            ),
                    )
                    Text(
                        text = stage.enemyName,
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── 题干卡片 ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BCardBg)
                    .padding(14.dp),
            ) {
                // 2026-09-19 §10 修复:原来标题与「知识点」挤在一行 —— 知识点文字较长时
                //   会被压到边缘甚至裁掉。改为让知识点占满剩余宽度并允许换行,信息不丢。
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = when (phase) {
                            CdPhase.Defense -> "防御作答"
                            CdPhase.Victory -> "关卡通过"
                            CdPhase.Defeat -> "本次出发结束"
                            else -> "进攻作答"
                        },
                        color = when (phase) {
                            CdPhase.Victory -> BCorrect
                            CdPhase.Defeat -> BWrong
                            CdPhase.Defense -> BGold
                            else -> BInk
                        },
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "知识点:${stage.knowledge}",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 15.sp),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = question.prompt,
                    color = BInk,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 选项 或 结算 ────────────────────────────────────────────
            when (phase) {
                CdPhase.Resolved, CdPhase.Victory, CdPhase.Defeat -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BCardBg)
                            .padding(14.dp),
                    ) {
                        Text(
                            text = headline,
                            color = if (phase == CdPhase.Defeat) BWrong
                            else if (phase == CdPhase.Victory) BCorrect
                            else if (blocked || lastCorrect == false) BWrong
                            else BCorrect,
                            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = detail,
                            color = BInk,
                            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        )
                        if (phase == CdPhase.Victory) {
                            // 战后剧情(文档 §5 每关都有一句,用于衔接下一关)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stage.aftermath,
                                color = BInk,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (practiceMode)
                                    "练习模式:本关不解锁正式节点,也不发放首通奖励。"
                                else
                                    "获得「${stage.title}」通关印记,回到地图继续下一关。",
                                color = BInkSoft,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                            )
                        } else if (phase == CdPhase.Defeat) {
                            // 战败结算文案对齐文档 §9「普通关失败」
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (practiceMode)
                                    "练习模式不消耗闯荡令,可以直接再来一次。"
                                else
                                    "此战惜败,本次出发已结束。本次已消耗的闯荡令不再额外扣除;" +
                                        "已通关的节点与已获得的奖励均保留,可先补修再重新出发。",
                                color = BInkSoft,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp, lineHeight = 17.sp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        // 2026-09-21:胜利结算面板加「继续挑战 下一关」主按钮
                        //   · 策划 §6.1:普通关通过后继续下一关不额外扣令(已在 runActive 状态)——
                        //     这是「继续挑战」按钮的成本判据(玩家不必担心再扣 1 枚)
                        //   · 第 4 关胜利 → 第 5 关(Boss):见 advance() / onGoNext
                        //   · 第 5 关(Boss)胜利:NavHost 不传 onGoNext(无下一关),只显示「返回地图」
                        //   · 战败 / 练习模式:也不显示「继续挑战」(练习不连发 / 战败已 endRun)
                        if (phase == CdPhase.Victory && !practiceMode && actions.onGoNext != null) {
                            CdPrimaryButton(text = "继续挑战 下一关", onClick = actions.onGoNext!!)
                            Spacer(Modifier.height(8.dp))
                        }
                        CdPrimaryButton(
                            text = when (phase) {
                                CdPhase.Victory, CdPhase.Defeat -> "返回地图"
                                else -> if (pendingDefense) "迎击(防御作答)" else "继续"
                            },
                            onClick = { advance() },
                        )
                        // 2026-09-20:结算面板也提供「撤退」入口(用户 1 号指令,补全触发路径),
                        //   与顶部"撤退"按钮 + BackHandler 等价 —— 胜利/战败时 advance() 已自行
                        //   退出战斗,这里仅 Resolved 阶段显示,避免胜利/战败后弹两次。
                        if (phase == CdPhase.Resolved && !practiceMode) {
                            Spacer(Modifier.height(8.dp))
                            CdTextButton(
                                text = "撤退本次出发",
                                onClick = { showRetreatDialog = true },
                            )
                        }
                    }
                }
                else -> {
                    // 练习模式的「看提示」(文档 §2:免费练习可查看提示与秘籍)
                    if (practiceMode) {
                        if (showHint) {
                            Text(
                                text = "提示:本关考的是「${stage.knowledge}」。",
                                color = BGold,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp, lineHeight = 17.sp),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x22B8894A))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { showHint = true },
                                    )
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = "看提示",
                                    color = BGold,
                                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    // ── 三种知识交互(文档 §3,2026-09-19 §15)──────────────────
                    //   点选:直接点即作答;排序:依次点满即作答;分类:逐条归类后按确认。
                    when (val q = question) {
                        is CdChoice -> shownOptions.forEachIndexed { i, option ->
                            CdOptionRow(
                                text = option,
                                onClick = { answer(i == shownCorrectIndex) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        is CdOrder -> {
                            Text(
                                text = "按正确顺序依次点选(已选 ${picked.size}/${orderSteps.size})",
                                color = BInkSoft,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            orderSteps.forEachIndexed { i, step ->
                                val at = picked.indexOf(i)
                                CdOptionRow(
                                    text = if (at >= 0) "${at + 1}. $step" else step,
                                    // 已点过的步骤变灰但仍可看;点它不生效(不返工不扣分,因为排序
                                    // 判定的是"第一次点满的顺序",重来请用下方「重排」)。
                                    highlight = if (at >= 0) BGold else null,
                                    onClick = {
                                        if (at < 0 && picked.size < orderSteps.size) {
                                            val next = picked + i
                                            picked = next
                                            if (next.size == orderSteps.size) {
                                                // 判定:玩家点出的步骤序列是否等于数据里的正确顺序
                                                answer(next.map { orderSteps[it] } == q.steps)
                                            }
                                        }
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            if (picked.isNotEmpty() && picked.size < orderSteps.size) {
                                CdTextButton(text = "重排", onClick = { picked = emptyList() })
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        is CdSort -> {
                            Text(
                                text = "把每一条归入正确的类别",
                                color = BInkSoft,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            q.items.forEachIndexed { i, item ->
                                CdSortRow(
                                    text = item.text,
                                    buckets = q.buckets,
                                    chosen = assigned.getOrElse(i) { -1 },
                                    onPick = { b ->
                                        assigned = assigned.toMutableList().also { it[i] = b }
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            val allAssigned = assigned.isNotEmpty() && assigned.all { it >= 0 }
                            CdPrimaryButton(
                                text = if (allAssigned) "确认归类" else "还有 ${assigned.count { it < 0 }} 条未归类",
                                enabled = allAssigned,
                                onClick = {
                                    val ok = q.items.indices.all { assigned[it] == q.items[it].bucket }
                                    answer(ok)
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }

        // ── 撤退确认框(2026-09-20,对应策划 §6.4「弹窗说明后果,确认后结束本次出发」)─
        //   弹窗只两个按钮:「撤退」「取消」 —— 策划没有「暂离」这一选项;
        //   切后台/关闭/断网 = 暂停是另一条独立规则(策划 §6.4 末条)。
        //   点「撤退」后,关闭弹窗并 endRun(),然后弹 [showRetreatResult] 三选项面板。
        if (showRetreatDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC1A1712))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        // 点遮罩 = 取消,继续留在战斗中
                        onClick = { showRetreatDialog = false },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F3EA))
                        // 吃掉面板内部的点击,避免穿透到遮罩把弹框关掉
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { },
                        )
                        .padding(18.dp),
                ) {
                    Text(
                        text = "要离开本次出发吗?",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        // 2026-09-20:文案按策划 §6.4「失败不再额外扣」与 §9「普通关失败」对齐
                        text = "本次出发已消耗 1 枚闯荡令,撤退不返还;本关将回到「可挑战」。" +
                            "已通过的关卡与已获得的奖励保留。",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 取消 —— 留在战斗中
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFFEDE6D7))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showRetreatDialog = false },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "取消",
                                color = BInk,
                                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            )
                        }
                        Spacer(Modifier.size(10.dp))
                        // 撤退 —— 结束本次出发(策划 §6.4:闯荡令不返还)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(BWrong)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        showRetreatDialog = false
                                        ChuangdangStore.endRun()
                                        showRetreatResult = true
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "撤退",
                                color = Color.White,
                                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            )
                        }
                    }
                }
            }
        }

        // ── 撤退结果面板(策划 §9「普通关失败」三选项:补修 / 练习 / 重新出发)────────
        //   撤退确认后展示:让用户知道"本关没丢、可以怎么再战"。
        //   补修入口目前没有对应路由(修学/补修模块在"等后端的一批"里,见 SESSION-LOG-2026-09-20 待办 #9),
        //   所以这里先弹一个轻量提示 + 提供默认回到修学主页 —— 模块接上后只需把 onGoReview 换成真正的补修页跳转。
        if (showRetreatResult) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC1A1712))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        // 撤离面板必须显式选一个动作才能走;点遮罩 = 不响应(防止误关)
                        onClick = { },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F3EA))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { },
                        )
                        .padding(18.dp),
                ) {
                    Text(
                        text = "本次出发已结束",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        // 对照策划 §9「普通关失败」文案模板:此战惜败 → 这里用「本次主动撤退」
                        text = "本次已消耗 1 枚闯荡令,不再额外扣除;" +
                            "已通关的节点与已获得的奖励均保留。",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "本关知识点:${stage.knowledge}",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp, lineHeight = 17.sp),
                    )
                    Spacer(Modifier.height(16.dp))

                    // ── 三个主操作(策划 §9 三选项)──────────────────────────
                    CdRetreatActionButton(
                        text = "前往补修",
                        sub = "回修学页补这一关的知识点",
                        tint = BGold,
                        onClick = {
                            showRetreatResult = false
                            actions.onGoReview()
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    CdRetreatActionButton(
                        text = "免费练习",
                        sub = "不消耗闯荡令,把本关再打一次",
                        tint = Color(0xFF6B5B8A),
                        onClick = {
                            showRetreatResult = false
                            actions.onPracticeSame()
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    CdRetreatActionButton(
                        text = "重新出发",
                        sub = "消耗 1 枚闯荡令,本关重新开始",
                        tint = BCorrect,
                        // 余额不足时按钮置灰,提示文字说明恢复方式
                        enabled = ChuangdangStore.tokens > 0,
                        sub2 = if (ChuangdangStore.tokens <= 0)
                            "闯荡令不足,${ChuangdangStore.nextRestoreText()}"
                        else null,
                        onClick = {
                            showRetreatResult = false
                            actions.onRestartSame()
                        },
                    )
                    Spacer(Modifier.height(14.dp))
                    // 收尾的二级动作:返回地图
                    Text(
                        text = "返回地图",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    showRetreatResult = false
                                    actions.onExit()
                                },
                            )
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // ── 关前剧情(2026-09-19 §14,文档 §2/§5)────────────────────────
        //   首次进入某关时拦在战斗前面;已读过的关不再自动弹出(见 showIntro 的初始化),
        //   但顶部栏「重看剧情」可随时调回来。点面板外不关闭 —— 剧情需要显式确认。
        if (showIntro) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC1A1712))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F3EA))
                        .padding(18.dp),
                ) {
                    Text(
                        text = "第 ${stage.index} 关 · ${stage.title}",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "敌对:${stage.enemyName}",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stage.scene,
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 21.sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "本关知识点:${stage.knowledge}",
                        color = BGold,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    )
                    Spacer(Modifier.height(16.dp))
                    CdPrimaryButton(
                        text = if (practiceMode) "开始练习" else "进入战斗",
                        onClick = {
                            // 标记已读:之后重进本关不再拦路(文档 §2「已读剧情可跳过」)
                            ChuangdangStore.markSceneRead(stage.index)
                            showIntro = false
                        },
                    )
                }
            }
        }
    }
}

/** 三颗心的显示(§3.1:一次有效攻击造成一颗心伤害)。 */
@Composable
private fun CdHearts(alive: Int, tint: Color) {
    Row {
        repeat(CD_MAX_HEARTS) { i ->
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(if (i < alive) tint else Color(0x33888888))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape),
            )
        }
    }
}

/**
 * 选项行。
 *
 * 2026-09-19 §10 修复:原先是 `.height(50.dp)` 固定高度 —— 选项文字较长换行到 3 行时
 * (13sp × 1.45 × 3 ≈ 57dp)会被 `clip` 裁掉,看起来像"文字被遮挡"。
 * 改为 `heightIn(min = ...)` 让高度随内容增长,并补上垂直内边距与显式行高。
 *
 * 最小高度由 [CD_OPTION_MIN_HEIGHT] 统一控制(五关共用,当前 55dp)。
 */
@Composable
private fun CdOptionRow(
    text: String,
    onClick: () -> Unit,
    /** 非 null 时给整行加底色(排序题用来标"已选中的第 N 步",2026-09-19 §15)。 */
    highlight: Color? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CD_OPTION_MIN_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight != null) Color(0x22B8894A) else BCardBg)
            .border(
                if (highlight != null) 1.5.dp else 1.dp,
                highlight ?: Color(0x332E2A24),
                RoundedCornerShape(12.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = BInk,
            style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp),
        )
    }
}

/**
 * **分类题的一行**(2026-09-19 §15,文档 §3 的第三种交互):
 * 左边是要归类的条目,右边一排类别小按钮;当前选中项高亮。
 */
@Composable
private fun CdSortRow(
    text: String,
    buckets: List<String>,
    chosen: Int,
    onPick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CD_OPTION_MIN_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(BCardBg)
            .border(1.dp, Color(0x332E2A24), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Text(
            text = text,
            color = BInk,
            style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp),
        )
        Spacer(Modifier.height(8.dp))
        Row {
            buckets.forEachIndexed { b, name ->
                val on = b == chosen
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (on) BGold else Color(0x142E2A24))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onPick(b) },
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = name,
                        color = if (on) Color.White else BInkSoft,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}

/** 次级文字按钮(排序题的「重排」用)。 */
@Composable
private fun CdTextButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, BGold, RoundedCornerShape(11.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            color = BGold,
            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp),
        )
    }
}

/** 主按钮。[enabled] 为 false 时置灰且不可点(分类题未归完时用)。 */
@Composable
private fun CdPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (enabled) BGold else Color(0x55888888))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        )
    }
}

/**
 * 撤退结果面板里的"主操作"按钮(策划 §9 三选项)。
 *
 * 区别于 [CdPrimaryButton]:这里要展示两行(主标 + 说明),让用户在三选项里
 * 一眼看出区别;且**禁用时只把背景与说明文字改暗,不让按钮整体消失**
 * (策划 §9 末段:"「重新出发」旁展示所需一枚令和当前余额,不足时给出恢复时间及可完成的补修入口")。
 */
@Composable
private fun CdRetreatActionButton(
    text: String,
    sub: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    /** 余额不足时的次级说明(放在 sub 下面);null = 不显示。 */
    sub2: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) tint else Color(0x55888888))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = sub,
            color = Color(0xE6FFFFFF),
            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp, lineHeight = 16.sp),
        )
        if (sub2 != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = sub2,
                color = Color(0xCCFFFFFF),
                style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 14.sp),
            )
        }
    }
}
