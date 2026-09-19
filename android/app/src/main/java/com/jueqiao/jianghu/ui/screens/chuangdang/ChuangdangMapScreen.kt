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

                CdNodeCard(
                    node = node,
                    done = done,
                    unlocked = unlocked,
                    isCurrent = isCurrent,
                    isBoss = isBoss,
                    onClick = {
                        when {
                            done -> {
                                // 已通关:重玩,不计入新的出发(不额外扣令)
                                actions.onEnterStage(node.index)
                            }
                            unlocked -> {
                                if (ChuangdangStore.beginRun()) {
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
                    .padding(bottom = 14.dp),
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
                                    actions.onEnterStage(ChuangdangStore.currentStage)
                                } else {
                                    tip = "闯荡令不足:每次正式出发需要 1 枚"
                                }
                            },
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (ChuangdangStore.runActive) "继续挑战 第 $current 关" else "消耗 1 枚 · 出发",
                        color = Color.White,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    )
                }
            }
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
    isBoss: Boolean,
    onClick: () -> Unit,
) {
    val edge = when {
        isCurrent -> CdActive
        done -> CdDone
        else -> CdCardEdge
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CdCardBg)
            .border(if (isCurrent || done) 1.5.dp else 1.dp, edge, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
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
