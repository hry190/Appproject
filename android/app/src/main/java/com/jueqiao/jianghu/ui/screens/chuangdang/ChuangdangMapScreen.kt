package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay

/**
 * 2026-09-19 §6 闯荡江湖 · 地图页 —— 【雾隐机关镇】
 *
 * 依据策划方案 v2:
 *   §2  首页入口与用户流程 —— 地图展示五个节点、已通关印记、当前关卡、Boss 剪影与闯荡令余额
 *   §2  一次完整旅程 —— 在地图查看事件、进度、消耗与奖励,再确认消耗闯荡令出发
 *   §6.1 一次正式出发 —— 支付一枚闯荡令,从当前未通关节点开始,可连续推进到 Boss
 *
 * 用户指令:"先把页面做出来,先不接后端" —— 进度读 [ChuangdangStore](进程内状态),
 * 素材复用后山系列的水墨背景,敌人形象用文字徽记占位(不引入新图)。
 *
 * 点击行为:
 *   | 点击位置            | 结果 |
 *   |--------------------|------|
 *   | 当前可挑战的节点     | 消耗一枚闯荡令并进入该关 |
 *   | 已通关的节点        | 进入重玩(不额外扣令,视为同一次出发)|
 *   | 未解锁的节点        | ❌ 无效,底部提示 |
 *   | 底部「出发」按钮     | 指定当前关出发 |
 *   | 左上角返回 / 系统返回 | 回首页1 |
 */
data class ChuangdangMapActions(
    val onBack: () -> Unit = {},
    /** 进入某一关:1~4 为普通关,5 为 Boss 关。 */
    val onEnterStage: (Int) -> Unit = {},
    /** 免费练习同一关(文档 §2:不消耗闯荡令、可看提示、不解锁正式节点)。 */
    val onPracticeStage: (Int) -> Unit = {},
)

/** 地图上的一个节点(普通关 1~4 与 Boss 关 5 统一描述)。 */
private data class CdMapNode(
    val index: Int,
    val title: String,
    val enemy: String,
    val subtitle: String,
    val glyph: String,
)

// 用 by lazy 而非直接初始化:本列表依赖**同包另一文件**的顶层 [CD_STAGES],
// 而 Kotlin 跨文件的顶层属性初始化顺序没有保证,直接引用可能拿到尚未初始化的空列表。
private val CD_MAP_NODES: List<CdMapNode> by lazy {
    CD_STAGES.map {
        CdMapNode(
            index = it.index,
            title = it.title,
            enemy = it.enemyName,
            subtitle = it.knowledge,
            glyph = it.enemyGlyph,
        )
    } + CdMapNode(
        index = CD_BOSS_INDEX,
        title = CD_BOSS_TITLE,
        enemy = CD_BOSS_ENEMY,
        subtitle = "制作任务 · 交付「小机关设计卡」",
        glyph = CD_BOSS_GLYPH,
    )
}

// 水墨风配色(背景是浅色山水,故用深墨文字 + 半透明白卡片)
private val CdInk = Color(0xFF2E2A24)
private val CdInkSoft = Color(0x992E2A24)
private val CdCardBg = Color(0xE8FFFFFF)
private val CdCardEdge = Color(0x332E2A24)
private val CdDone = Color(0xFF3F6B3A)
private val CdActive = Color(0xFF8A5A2B)
private val CdLocked = Color(0x552E2A24)
private val CdGold = Color(0xFFB8894A)

/**
 * 地图上单个关卡卡片的**最小高度** —— 六个节点(四关 + 终局 + 未解锁态)共用这一处。
 *
 * 2026-09-19 §10 修复:原先这里是 `.height(66.dp)` **固定高度**。
 * 66dp 是按**单行**副标题定的 —— 第 1 关的副标题只有一行,内容合计 110px,
 * 正好等于 40dp 徽记的 110px,上下各留 31px,看起来没问题。
 * 但第 2/3/4 关的副标题会折成两行,内容涨到 154px,上下余量只剩 **9px**;
 * 系统字体一放大就越过卡片下缘被 `clip` 裁掉 —— 实测 font_scale 1.15 时下留白 −1px、
 * 1.3 时 −5px(副标题第二行被切掉一截)。
 *
 * 改为 `heightIn(min = ...)` 后:单行仍渲染 66dp(外观零变化),两行时卡片自己长高。
 * 注意这只是**下限**,内容变多时卡片会自动增高(见 [CdNodeCard])。
 *
 * 同类修复见 ChuangdangBattleScreen 的 CD_OPTION_MIN_HEIGHT(选项框 50dp → 自适应)。
 */
private val CD_NODE_MIN_HEIGHT = 66.dp

@Composable
fun ChuangdangMapScreen(
    actions: ChuangdangMapActions = ChuangdangMapActions(),
) {
    BackHandler { actions.onBack() }

    // 读取进程内进度 —— mutableStateOf 变化会自动触发表格重组
    val tokens = ChuangdangStore.tokens
    val cleared = ChuangdangStore.clearedStages
    val bossCleared = ChuangdangStore.bossCleared
    val current = ChuangdangStore.currentStage
    var tip by remember { mutableStateOf<String?>(null) }
    // 2026-09-19 §6:秘籍副本选择面板(留下选择其他书本的入口)
    var showPicker by remember { mutableStateOf(false) }
    var pickerTip by remember { mutableStateOf<String?>(null) }

    // 2026-09-19 §6 追加:闯荡令的"下次恢复说明"(文档 §2 要求地图展示余额 + 下次恢复)。
    //   进入页面先按北京时间自然日结算一次(§6.2 的自然恢复),之后每 30 秒刷新倒计时。
    var restoreText by remember { mutableStateOf(ChuangdangStore.nextRestoreText()) }
    LaunchedEffect(Unit) {
        ChuangdangStore.settleDailyRestore()
        while (true) {
            restoreText = ChuangdangStore.nextRestoreText()
            delay(30_000L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景:复用后山系列的水墨山水图
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
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 返回按钮 ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = actions.onBack,
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 标题 + 切换副本(2026-09-19 §6:留下选择其他秘籍的入口)────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "闯荡江湖",
                        color = CdInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 28.sp),
                    )
                    Text(
                        text = "《${CD_MANUALS.first { it.available }.name}》· " +
                            "${CD_MANUALS.first { it.available }.dungeon}　五关",
                        color = CdInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    // 2026-09-19 §6:通关印记与配饰(文档 §7 的简版展示)
                    Text(
                        text = if (bossCleared) {
                            "通关印记 ${cleared.size}/4 · 已获「识机通关印」「识机铜铃」"
                        } else {
                            "通关印记 ${cleared.size}/4"
                        },
                        color = if (bossCleared) CdActive else CdInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, CdCardEdge, RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showPicker = true },
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "切换副本",
                        color = CdInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── 五个节点 ────────────────────────────────────────────────
            CD_MAP_NODES.forEach { node ->
                val isBoss = node.index == CD_BOSS_INDEX
                val done = if (isBoss) bossCleared else node.index in cleared
                val unlocked = ChuangdangStore.isUnlocked(node.index)
                val isCurrent = node.index == current && !done
                // 2026-09-19 §6:本次出发已进入过、但尚未通关的关卡 → 显示"继续"
                val resumable = !done && ChuangdangStore.startedStage == node.index

                CdNodeCard(
                    node = node,
                    done = done,
                    unlocked = unlocked,
                    isCurrent = isCurrent,
                    resumable = resumable,
                    isBoss = isBoss,
                    onClick = {
                        when {
                            done -> {
                                // 已通关:重玩,不计入新的出发(不额外扣令)
                                ChuangdangStore.markStageEntered(node.index)
                                actions.onEnterStage(node.index)
                            }
                            unlocked -> {
                                if (ChuangdangStore.beginRun()) {
                                    ChuangdangStore.markStageEntered(node.index)
                                    actions.onEnterStage(node.index)
                                } else {
                                    tip = "闯荡令不足:每次正式出发需要 1 枚"
                                }
                            }
                            else -> tip = "「${node.title}」尚未解锁,请先通过第 ${current} 关"
                        }
                    },
                )
                Spacer(Modifier.height(9.dp))
            }

            Spacer(Modifier.weight(1f))

            // 2026-09-19 §14:对齐文档 §9「出发前明确代价」——
            //   原文要求出发前把「消耗几枚 / 能从当前关继续推进 / 什么情况下结束 / 什么会保留」
            //   讲清楚;此前底部只有一排按钮,用户看不出代价是什么。
            //   runActive 时说明改为"继续推进不额外扣令",避免误以为每次进关都要再花一枚。
            Text(
                text = if (ChuangdangStore.runActive) {
                    "继续推进不额外扣令 · 可从第 $current 关一路打到 Boss"
                } else {
                    "本次出发消耗 1 枚 · 可从第 $current 关连续推进至 Boss"
                },
                color = CdActive,
                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                modifier = Modifier.padding(bottom = 2.dp),
            )
            Text(
                text = "失败或主动撤退即结束本次出发;前置通关记录与已获得奖励保留",
                color = CdInkSoft,
                style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 15.sp),
                modifier = Modifier.padding(bottom = 6.dp),
            )

            // ── 底部:闯荡令 + 出发 ──────────────────────────────────────
            tip?.let {
                Text(
                    text = it,
                    color = Color(0xFF9B3B2E),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 闯荡令余额
                Text(
                    text = "闯荡令",
                    color = CdInkSoft,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                )
                Spacer(Modifier.size(6.dp))
                repeat(CD_TOKENS_MAX) { i ->
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (i < tokens) CdGold else Color(0x33FFFFFF))
                            .border(1.dp, Color(0x66FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "令",
                            color = if (i < tokens) Color.White else Color(0x882E2A24),
                            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // 免费练习(文档 §2 + §9 的【先去练习】):不消耗闯荡令、可看提示、不解锁正式节点
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .border(1.dp, CdGold, RoundedCornerShape(21.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { actions.onPracticeStage(current) },
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "免费练习",
                        color = CdActive,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    )
                }

                Spacer(Modifier.size(8.dp))

                // 出发按钮
                val canStart = tokens > 0
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (canStart) CdGold else Color(0x55888888))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = canStart,
                            onClick = {
                                if (ChuangdangStore.beginRun()) {
                                    ChuangdangStore.markStageEntered(ChuangdangStore.currentStage)
                                    actions.onEnterStage(ChuangdangStore.currentStage)
                                } else {
                                    tip = "闯荡令不足:每次正式出发需要 1 枚"
                                }
                            },
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (ChuangdangStore.runActive) "继续挑战 第 $current 关" else "消耗 1 枚 · 出发",
                        color = Color.White,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    )
                }
            }

            // 2026-09-19 §6:除了余额,还要标注"下次恢复说明"(文档 §2)
            Text(
                text = restoreText,
                color = CdInkSoft,
                style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            )
        }

        // ── 秘籍副本选择面板(2026-09-19 §6)──────────────────────────────
        //   文档 §12 只保留其余九本的名称 / 场景 / Boss 概念(本期不做关卡、题目、动画与素材);
        //   §2 要求"若在界面露出,应标明'后续开放'" —— 故未开放条目灰显,点击只给提示、不跳转。
        if (showPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC1A1712))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showPicker = false; pickerTip = null },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.82f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F3EA))
                        // 吃掉面板内部的点击,避免穿透到遮罩(mask)把面板关掉
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { },
                        )
                        .padding(16.dp),
                ) {
                    Text(
                        text = "选择秘籍副本",
                        color = CdInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    )
                    Text(
                        text = "本期开放《识机真诀》五关;其余九本仅保留名称与场景概念。",
                        color = CdInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 3.dp),
                    )

                    pickerTip?.let {
                        Text(
                            text = it,
                            color = Color(0xFF9B3B2E),
                            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        CD_MANUALS.forEach { manual ->
                            CdManualRow(
                                manual = manual,
                                onClick = {
                                    if (manual.available) {
                                        showPicker = false
                                        pickerTip = null
                                    } else {
                                        pickerTip = "「${manual.name}」后续开放 —— 本期只做《识机真诀》。"
                                    }
                                },
                            )
                            Spacer(Modifier.height(7.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(CdGold)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showPicker = false; pickerTip = null },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "关闭",
                            color = Color.White,
                            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

/** 地图上的单个关卡卡片。 */
@Composable
private fun CdNodeCard(
    node: CdMapNode,
    done: Boolean,
    unlocked: Boolean,
    isCurrent: Boolean,
    /** 本次出发已进入过但尚未通关(2026-09-19 §6:显示"继续")。 */
    resumable: Boolean,
    isBoss: Boolean,
    onClick: () -> Unit,
) {
    val edge = when {
        isCurrent || resumable -> CdActive
        done -> CdDone
        else -> CdCardEdge
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CD_NODE_MIN_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(CdCardBg)
            .border(if (isCurrent || resumable || done) 1.5.dp else 1.dp, edge, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            // 2026-09-19 §19 修复:原先只有水平内边距,**没有垂直内边距**。
            //   两行副标题的卡片内容是 154px,而最小高度 66dp = 181.5px —— 撑到"刚好等于内容高度",
            //   上下 padding 实际变成 0:文字版面框距卡片下缘仅 **9px(3.3dp)**、墨迹仅 15px(5.5dp),
            //   而单行副标题的卡片有 37px(13.8dp)。字没被切,但**挤到看起来像被裁**(用户报障)。
            //   补 10dp 垂直内边距后:单行卡片仍由 heightIn(min) 保持 66dp(外观不变),
            //   两行卡片则长到 ~76dp,上下各留出 10dp。
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 序号 / 敌人徽记
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> Color(0x26B8894A)
                        done -> Color(0x263F6B3A)
                        else -> Color(0x142E2A24)
                    },
                )
                .border(1.dp, edge, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = node.glyph,
                color = when {
                    !unlocked -> CdLocked
                    isBoss -> CdGold
                    else -> CdInk
                },
                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 18.sp),
            )
        }

        Spacer(Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isBoss) "终局 · ${node.title}" else "第 ${node.index} 关 · ${node.title}",
                    color = if (unlocked) CdInk else CdLocked,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                )
            }
            Text(
                text = "${node.enemy} · ${node.subtitle}",
                color = if (unlocked) CdInkSoft else CdLocked,
                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // 状态标签
        val (label, labelColor) = when {
            done -> "已通关" to CdDone
            // 2026-09-19 §6:已进入过但未通关 → "继续"
            resumable -> "继续" to CdActive
            isCurrent -> "可挑战" to CdActive
            else -> "未解锁" to CdLocked
        }
        Text(
            text = label,
            color = labelColor,
            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
        )
    }
}

/** 秘籍副本选择面板里的一行(2026-09-19 §6)。 */
@Composable
private fun CdManualRow(manual: CdManual, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (manual.available) Color(0x26B8894A) else Color(0x0F2E2A24))
            .border(
                if (manual.available) 1.5.dp else 1.dp,
                if (manual.available) CdGold else CdCardEdge,
                RoundedCornerShape(11.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "《${manual.name}》",
                color = if (manual.available) CdInk else CdLocked,
                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            )
            Text(
                text = "${manual.dungeon} · ${manual.scene} · Boss:${manual.boss}",
                color = if (manual.available) CdInkSoft else CdLocked,
                style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = if (manual.available) "可挑战" else "后续开放",
            color = if (manual.available) CdActive else CdLocked,
            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 10.sp),
        )
    }
}
