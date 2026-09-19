package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 2026-09-19 §6 闯荡江湖 · 第五关(Boss)—— 制作任务 + 评审
 *
 * 依据策划方案 v2 §4「第五关的制作任务与 AI 评审」:
 *   §4.1 任务卡提前展示剧情目标、交付形式、必须体现的知识、评分标准与通关条件
 *   §4.2 提交内容包括作品与「我为什么这样设计」,覆盖五项必要条件
 *   §4.3 五维量表:知识与原理正确 30 / 任务完成度 25 / 个人解释与决策 20 / 测试与修正 15 / 表达清楚 10
 *   §4.3 通关门槛:总分 ≥ 70,且知识正确性 ≥ 18、个人解释 ≥ 12,**并满足必要条件**
 *        (不得把「模型给出答案」直接当作「答案必然正确」——必须给出不确定时的处理办法)
 *
 * 用户 2026-09-19 选择:**本地规则评分**(不调大模型)。评分实现见文件末尾的 [cdScoreBoss],
 * 用关键词 + 结构检查覆盖五个维度;文档提到"具体分数需结合样例校准",故此处只作演示级评分。
 */
data class ChuangdangBossActions(
    val onBack: () -> Unit = {},
)

private val BossInk = Color(0xFF2E2A24)
private val BossInkSoft = Color(0x992E2A24)
private val BossCardBg = Color(0xF2FFFFFF)
private val BossGold = Color(0xFFB8894A)
private val BossPass = Color(0xFF3F6B3A)
private val BossFail = Color(0xFF9B3B2E)

@Composable
fun ChuangdangBossScreen(
    actions: ChuangdangBossActions = ChuangdangBossActions(),
) {
    var draft by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CdBossResult?>(null) }
    var submittedText by remember { mutableStateOf("") }

    BackHandler { actions.onBack() }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 返回 ────────────────────────────────────────────────────
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

            // ── 标题 + Boss ─────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 2026-09-19 §6:与战斗页共用 Canvas 手绘的敌人形象(见 ChuangdangMonsters.kt)
                CdMonster(
                    glyph = CD_BOSS_GLYPH,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = "终局 · ${CD_BOSS_TITLE}",
                        color = BossInk,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    )
                    Text(
                        text = CD_BOSS_ENEMY,
                        color = BossInkSoft,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 场景 ────────────────────────────────────────────────────
            Text(
                text = CD_BOSS_SCENE,
                color = BossInk,
                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BossCardBg)
                    .padding(12.dp),
            )

            Spacer(Modifier.height(10.dp))

            // ── 任务卡(§4.1)──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BossCardBg)
                    .border(1.dp, BossGold, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Text(
                    text = "制作任务",
                    color = BossGold,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = CD_BOSS_TASK,
                    color = BossInk,
                    style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "你的设计卡需要覆盖以下五项:",
                    color = BossInkSoft,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                )
                Spacer(Modifier.height(4.dp))
                CD_BOSS_REQUIRED_POINTS.forEachIndexed { i, point ->
                    Text(
                        text = "${i + 1}. $point",
                        color = BossInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "评分标准(共 100 分):" + CD_BOSS_DIMENSIONS.joinToString(" · ") { "${it.name} ${it.maxScore}" },
                    color = BossInkSoft,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 16.sp),
                )
                Text(
                    text = "通关条件:总分 ≥ $CD_BOSS_PASS_SCORE,知识 ≥ $CD_BOSS_KNOWLEDGE_FLOOR," +
                        "个人解释 ≥ $CD_BOSS_EXPLAIN_FLOOR,且必须给出「不确定时如何处理」的办法。",
                    color = BossFail,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 16.sp),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 作品输入(§4.2)────────────────────────────────────────
            Text(
                text = "我的小机关设计卡",
                color = BossInk,
                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            )
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                textStyle = TextStyle(fontFamily = YaHei, fontSize = 13.sp, color = BossInk, lineHeight = 20.sp),
                cursorBrush = SolidColor(BossGold),
                modifier = Modifier
                    .fillMaxWidth()
                    // 2026-09-19 §10:按用户要求整体 +5dp(180 → 185),与第 1~4 关的选项框调整保持一致
                    .height(185.dp),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BossCardBg)
                            .border(1.dp, Color(0x332E2A24), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        if (draft.isEmpty()) {
                            Text(
                                text = "写下你的设计思路:它能做什么、不能保证什么、感知与判断怎么分工、" +
                                    "需要哪些样本、看不清时怎么办、举一个可能失败的例子……",
                                color = Color(0x662E2A24),
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                            )
                        }
                        inner()
                    }
                },
            )

            Spacer(Modifier.height(10.dp))

            // ── 提交 ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (draft.isBlank()) Color(0x55888888) else BossGold)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = draft.isNotBlank(),
                        onClick = {
                            val r = cdScoreBoss(draft)
                            result = r
                            submittedText = draft
                            ChuangdangStore.lastBossResult = r
                            if (r.passed) ChuangdangStore.clearBoss()
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "提交评审",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                )
            }

            // ── 评审结果(§4.3)────────────────────────────────────────
            result?.let { r ->
                Spacer(Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BossCardBg)
                        .border(1.5.dp, if (r.passed) BossPass else BossFail, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text(
                        text = if (r.passed) "机枢恢复清明" else "机枢的最后一道锁仍未打开",
                        color = if (r.passed) BossPass else BossFail,
                        style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${r.total}",
                            color = if (r.passed) BossPass else BossFail,
                            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 32.sp),
                        )
                        Text(
                            text = " 分 · 通关要求 ≥ $CD_BOSS_PASS_SCORE",
                            color = BossInkSoft,
                            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // 五维得分
                    CD_BOSS_DIMENSIONS.forEachIndexed { i, dim ->
                        val score = r.scores.getOrElse(i) { 0 }
                        Column(modifier = Modifier.padding(bottom = 7.dp)) {
                            Row {
                                Text(
                                    text = dim.name,
                                    color = BossInk,
                                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "$score / ${dim.maxScore}",
                                    color = BossInkSoft,
                                    style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0x1A2E2A24)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(score.toFloat() / dim.maxScore)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (r.passed) BossPass else BossGold),
                                )
                            }
                        }
                    }

                    if (r.advice.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "优先修改建议",
                            color = BossFail,
                            style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        )
                        Spacer(Modifier.height(3.dp))
                        r.advice.forEach { a ->
                            Text(
                                text = "· $a",
                                color = BossInk,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 19.sp),
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (r.passed) {
                        Text(
                            text = "设计卡成为机枢的新运行图,雾隐镇灯火重新点亮。获得「识机通关印」与「识机铜铃」。",
                            color = BossInkSoft,
                            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp, lineHeight = 17.sp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(BossPass)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = actions.onBack,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "返回江湖地图",
                                color = Color.White,
                                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            )
                        }
                    } else {
                        Text(
                            text = "草稿与评分已保存,修改免费 —— 改完再提交一次即可。",
                            color = BossInkSoft,
                            style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(BossGold)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        // 继续修改:把上次提交的内容放回输入框,直接改
                                        draft = submittedText
                                        result = null
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "继续修改",
                                color = Color.White,
                                style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * 本地规则评分 —— 对应策划方案 v2 §4.3 的五维量表。
 *
 * 说明:这是**演示级**规则评分(关键词 + 结构检查),不是真正的语义评价。
 * 文档已注明"具体分数需结合样例校准,不能只依据一次模型输出就认为量表可靠";
 * 日后接入真实评审服务时,替换本函数即可,页面侧无需改动。
 */
private fun cdScoreBoss(raw: String): CdBossResult {
    val t = raw.trim()
    val len = t.length
    fun has(vararg keys: String) = keys.any { t.contains(it) }

    // ① 知识与原理正确(30):能力边界 + 人工确认
    val boundary = has("不能保证", "不确定", "会出错", "可能出错", "能力边界", "边界", "局限", "做不到")
    val humanCheck = has("人工确认", "交给人", "由人", "人来判断", "人工", "确认", "复核", "人工复核")
    val knowledge = when {
        boundary && humanCheck -> 30
        boundary || humanCheck -> 18
        len > 0 -> 8
        else -> 0
    }

    // ② 任务完成度(25):五项要求,每项 5 分
    val pointChecks = listOf(
        has("能做", "可以识别", "能识别", "能做什么", "能认") && has("不能", "保证"),
        has("感知") && has("判断", "推理") && has("行动"),
        has("样本", "数据", "样例", "例子"),
        has("不确定", "看不清", "没学过", "陌生", "模糊", "拿不准") && humanCheck,
        has("失败", "出错") && has("验证", "测试", "修正", "改进", "办法"),
    )
    val completeness = pointChecks.count { it } * 5

    // ③ 个人解释与决策(20)
    val explainHits = listOf("因为", "所以", "我选择", "考虑到", "取舍", "优先", "原因", "权衡", "设计成", "决定")
        .count { t.contains(it) }
    val explanation = (explainHits * 5).coerceAtMost(20)

    // ④ 测试与修正(15)
    val failureCase = has("失败", "出错", "错误的情况", "错误情形", "误判")
    val verifyFix = has("验证", "测试", "修正", "改进", "调整", "迭代")
    val testFix = (if (failureCase) 8 else 0) + (if (verifyFix) 7 else 0)

    // ⑤ 表达清楚(10)
    val lineBreaks = t.count { it == '\n' }
    val clarity = ((if (len >= 120) 5 else if (len >= 60) 3 else 0) +
        (if (len >= 260) 3 else 0) +
        (if (lineBreaks >= 3 || has("1.", "一、", "①", "第一")) 2 else 0)).coerceAtMost(10)

    val scores = listOf(knowledge, completeness, explanation, testFix, clarity)
    val total = scores.sum()

    // §4.3 通关条件:总分达标 + 两项底线 + 必要条件(不确定时的处理办法)
    val passed = total >= CD_BOSS_PASS_SCORE &&
        knowledge >= CD_BOSS_KNOWLEDGE_FLOOR &&
        explanation >= CD_BOSS_EXPLAIN_FLOOR &&
        boundary && humanCheck

    val advice = buildList {
        if (!boundary) add("补一句「它不能保证什么」,把能力边界写清楚。")
        if (!humanCheck) add("补上「看不清 / 没学过时如何提示并交给人确认」的处理办法 —— 这是通关的必要条件。")
        if (!pointChecks[1]) add("分别说明感知、判断、行动各自负责什么。")
        if (!pointChecks[2]) add("说明需要哪些有代表性的样本。")
        if (!failureCase) add("举一个可能失败的具体例子。")
        if (!verifyFix) add("给出验证或修正的办法。")
        if (explanation < CD_BOSS_EXPLAIN_FLOOR) add("多写你自己的判断与取舍理由,例如「我选择…因为…」。")
        if (len < 120) add("说明再写详细一些,把设计思路讲完整。")
    }.take(3)

    return CdBossResult(scores = scores, total = total, passed = passed, advice = advice)
}
