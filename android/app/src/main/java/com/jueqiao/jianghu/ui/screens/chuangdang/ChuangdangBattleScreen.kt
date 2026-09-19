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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlin.random.Random

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
    /** 退出战斗回到地图(主动撤退 / 战败 / 通关都走这里)。 */
    val onExit: () -> Unit = {},
)

private val BInk = Color(0xFF2E2A24)
private val BInkSoft = Color(0x992E2A24)
private val BCardBg = Color(0xF2FFFFFF)
private val BGold = Color(0xFFB8894A)
private val BCorrect = Color(0xFF3F6B3A)
private val BWrong = Color(0xFF9B3B2E)

@Composable
fun ChuangdangBattleScreen(
    stageIndex: Int,
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
    var selected by remember { mutableStateOf<Int?>(null) }
    var pendingDefense by remember { mutableStateOf(false) } // 进攻答错 → 结算后转入防御题
    var blocked by remember { mutableStateOf(false) }
    // 2026-09-19 §6:文档 §3.3 的**保底** —— "一次攻击被格挡后,下一次正确攻击必定命中,
    //   即使期间出现答错回合"。故该标志只在"兑现命中"时清除,答错/防御作答都不影响它。
    var pendingGuaranteedHit by remember { mutableStateOf(false) }
    var headline by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    // 2026-09-19 §6:中途返回时弹确认框(文档 §6.4"主动撤退:弹窗说明后果,确认后结束本次出发")。
    //   · 撤退 → 结束本次出发,该关回到「可挑战」(闯荡令不返还)
    //   · 暂离 → 保留出发与关卡进度,该关显示「继续」,稍后进入不重复扣令
    //   注意:胜负已分时不弹框 —— 胜利/战败走 advance() 直接退出(那里已处理 clearStage / endRun)。
    var showRetreatDialog by remember { mutableStateOf(false) }
    val requestExit: () -> Unit = { showRetreatDialog = true }
    BackHandler { requestExit() }

    val question = when (phase) {
        CdPhase.Defense -> stage.defense[defenseIdx % stage.defense.size]
        else -> stage.attack[attackIdx % stage.attack.size]
    }

    /** 作答 + 按 §3.2 的判定顺序结算。 */
    fun answer(choice: Int) {
        if (selected != null) return
        if (phase != CdPhase.Attack && phase != CdPhase.Defense) return
        selected = choice
        val isCorrect = choice == question.correctIndex

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
                detail = question.explanation
                pendingDefense = false
                phase = if (enemyHearts <= 0) CdPhase.Victory else CdPhase.Resolved
            } else {
                // ③ 进攻答错 → 怪物攻击,稍后给一次防御机会
                headline = "答错了,怪物反击!"
                detail = question.explanation
                pendingDefense = true
                phase = CdPhase.Resolved
            }
        } else {
            // ④ 防御作答
            if (isCorrect) {
                headline = "挡下了!"
                detail = question.explanation
            } else {
                playerHearts -= 1
                headline = "防御失败,-1 心"
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
                // 通关:记录进度;是否结束出发由地图/Boss 关决定(四关全通后还要打 Boss)
                ChuangdangStore.clearStage(stage.index)
                actions.onExit()
            }
            CdPhase.Defeat -> {
                ChuangdangStore.endRun()
                actions.onExit()
            }
            CdPhase.Resolved -> {
                selected = null
                blocked = false
                if (pendingDefense) {
                    defenseIdx += 1
                    pendingDefense = false
                    phase = CdPhase.Defense
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Image(
                        painter = painterResource(R.drawable.img_shilian2_recovered_8),
                        contentDescription = "熊猫",
                        modifier = Modifier.size(width = 104.dp, height = 56.dp),
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
                    // 2026-09-19 §6:敌人形象改为 Canvas 手绘(见 ChuangdangMonsters.kt),
                    //   取代原先的圆形文字徽记。
                    CdMonster(
                        glyph = stage.enemyGlyph,
                        modifier = Modifier.size(96.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "知识点:${stage.knowledge}",
                        color = BInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
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
                            else if (blocked || selected != question.correctIndex) BWrong
                            else BCorrect,
                            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = detail,
                            color = BInk,
                            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        )
                        if (phase == CdPhase.Victory || phase == CdPhase.Defeat) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (phase == CdPhase.Victory)
                                    "获得「${stage.title}」通关印记,回到地图继续下一关。"
                                else
                                    "本次出发结束。已通关的节点与印记保留,可补修后重新出发。",
                                color = BInkSoft,
                                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        CdPrimaryButton(
                            text = when (phase) {
                                CdPhase.Victory, CdPhase.Defeat -> "返回地图"
                                else -> if (pendingDefense) "迎击(防御作答)" else "继续"
                            },
                            onClick = { advance() },
                        )
                    }
                }
                else -> {
                    question.options.forEachIndexed { i, option ->
                        CdOptionRow(
                            text = option,
                            onClick = { answer(i) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }

        // ── 撤退确认框(2026-09-19 §6,对应文档 §6.4 的"弹窗说明后果")──────────
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
                        text = "撤退:结束本次出发,已消耗的闯荡令不返还 —— 本关将回到「可挑战」。",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "暂离:保留出发与关卡进度,本关显示「继续」,稍后可从地图继续,不重复扣令。",
                        color = BInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 撤退 —— 有代价:结束出发,该关回到"可挑战"
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
                                        actions.onExit()
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
                        Spacer(Modifier.size(10.dp))
                        // 暂离 —— 保留进度:不结束出发,该关显示"继续"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(BGold)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        showRetreatDialog = false
                                        actions.onExit()
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "暂离",
                                color = Color.White,
                                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            )
                        }
                    }
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

/** 选项行。 */
@Composable
private fun CdOptionRow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BCardBg)
            .border(1.dp, Color(0x332E2A24), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = BInk,
            style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
        )
    }
}

/** 主按钮。 */
@Composable
private fun CdPrimaryButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(BGold)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
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
