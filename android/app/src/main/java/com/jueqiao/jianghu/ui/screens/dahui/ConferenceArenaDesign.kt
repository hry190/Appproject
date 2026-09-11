package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.conference.ConferenceUiState
import com.jueqiao.jianghu.luggage.ConferenceLetterDto
import com.jueqiao.jianghu.luggage.ConferenceMatchDetailDto
import com.jueqiao.jianghu.luggage.ConferenceMatchQueueDto
import com.jueqiao.jianghu.luggage.ConferenceMatchQuestionDto
import com.jueqiao.jianghu.luggage.ConferenceMatchRecordDto
import com.jueqiao.jianghu.luggage.ConferenceMatchResultDto
import com.jueqiao.jianghu.luggage.ManualPageDto
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ArenaJade950 = Color(0xFF315D4D)
private val ArenaJade900 = Color(0xFF4F8069)
private val ArenaJade800 = Color(0xFF6F9D7E)
private val ArenaJade700 = Color(0xFF86AD8E)
private val ArenaJade500 = Color(0xFFA8C7A5)
private val ArenaJade300 = Color(0xFFDCE9D4)
private val ArenaPaper = Color(0xFFF6EEDB)
private val ArenaPaperLight = Color(0xFFFFFBF1)
private val ArenaPaperDeep = Color(0xFFE7D7B0)
private val ArenaGold = Color(0xFFD8BC72)
private val ArenaInk = Color(0xFF244B42)
private val ArenaInkSoft = Color(0xFF63756A)
private val ArenaCinnabar = Color(0xFF98584F)
private val ArenaModeShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 5.dp,
    bottomEnd = 16.dp,
    bottomStart = 5.dp,
)
private val ArenaCardShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 6.dp,
    bottomEnd = 20.dp,
    bottomStart = 6.dp,
)

@Composable
internal fun ConferenceArenaHubContent(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenLetters: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenMatch: () -> Unit,
    onOpenCreation: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    var showCraftTask by rememberSaveable { mutableStateOf(false) }

    ArenaShell(
        title = "竹峰武会",
        subtitle = if (showCraftTask) "同题共创" else "同门切磋",
        onBack = if (showCraftTask) ({ showCraftTask = false }) else onBack,
        onTopAction = onOpenMatch,
        topActionContentDescription = "选择入场方式",
        snackbarHost = snackbarHost,
        activeNav = ArenaNav.Arena,
        onArena = { showCraftTask = false },
        onRecord = onOpenRecords,
        onLetters = onOpenLetters,
    ) {
        if (showCraftTask) {
            ArenaCraftTask(
                modifier = Modifier.arenaScrollPadding(),
                onOpenCreation = onOpenCreation,
            )
        } else {
            ArenaLobby(
                state = state,
                modifier = Modifier.arenaScrollPadding(),
                onOpenKnowledge = onOpenMatch,
                onOpenCraft = { showCraftTask = true },
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun ArenaLobby(
    state: ConferenceUiState,
    modifier: Modifier,
    onOpenKnowledge: () -> Unit,
    onOpenCraft: () -> Unit,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ArenaCardShape,
                color = ArenaPaperLight.copy(alpha = 0.66f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArenaJade700.copy(alpha = 0.24f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "先选一局，再遇同门",
                        color = ArenaInk,
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = ArenaJade700.copy(alpha = 0.65f))
                        Text("◆", color = ArenaJade800, fontSize = 15.sp)
                        HorizontalDivider(Modifier.weight(1f), color = ArenaJade700.copy(alpha = 0.65f))
                    }
                    Text(
                        "题面确认后，再进入匹配池",
                        color = ArenaInkSoft,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        state.error?.let { message ->
            item { ArenaInlineMessage(message = message, actionLabel = "重试", onAction = onRefresh) }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HorizontalDivider(Modifier.weight(1f), color = ArenaInk.copy(alpha = 0.55f))
                Text(
                    "第一步 · 选择本局内容",
                    color = ArenaInk,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                HorizontalDivider(Modifier.weight(1f), color = ArenaInk.copy(alpha = 0.55f))
            }
        }
        item {
            ArenaModeCard(
                glyph = "知",
                title = "知识比拼",
                body = "3 至 5 道已学知识情境题",
                chips = listOf("写明理由", "赛后复盘"),
                onClick = onOpenKnowledge,
            )
        }
        item {
            ArenaModeCard(
                glyph = "创",
                title = "同题 AI 共创",
                body = "同一任务，各自创作后由 AI 辅助评审",
                chips = listOf("复用创作台", "过程可追溯"),
                onClick = onOpenCraft,
            )
        }
    }
}

@Composable
private fun ArenaCraftTask(modifier: Modifier, onOpenCreation: () -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 17.dp, end = 17.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item {
            ArenaMatchHeaderBase(
                centerLabel = "同题",
                leftCaption = "作品 A\n尚未起稿",
                rightCaption = "作品 B\n准备中",
                progress = 0.2f,
                labels = listOf("构思", "草图", "制作", "测试", "说明"),
                description = "共创五阶段，当前为构思阶段",
            )
        }
        item {
            ArenaPaperCard {
                ArenaCardHeading("共创擂台 · 任务 T-07", "同一题面")
                Text(
                    "设计一张识别 AI 谣言的竖版海报",
                    color = ArenaInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    "面向同龄学生，说明“看到生成内容时如何核对来源”。双方可用相同的受控图片工具，各自决定构图、文案与修改过程。",
                    color = ArenaInkSoft,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
                ArenaChips(listOf("图片类首发", "验证来源", "人机分工必填", "素材可追溯"))
                ArenaStageStrip(activeIndex = 0)
                ArenaEvidenceLine("服务端为双方分别创建真实 CreationProject。")
                ArenaEvidenceLine("两个项目绑定同一任务快照和同一评审量规。")
                ArenaEvidenceLine("封卷、来源与 AIGC 检查通过后才能交卷。")
                ArenaPrimaryButton("进入现有创作台", onClick = onOpenCreation)
            }
        }
        item {
            ArenaCoachNote("本页只负责读题和确认。点击后复用现有创作页面，不新造第二套比赛编辑器。")
        }
    }
}

@Composable
internal fun ConferenceArenaMatchContent(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onJoin: (String) -> Unit,
    onExit: () -> Unit,
    onReport: (String, String) -> Unit,
    onSubmitAnswer: (String, String, String, String) -> Unit,
    onCreateEvaluation: (String, String, Double, String, String, String) -> Unit,
    onCreateReflection: (String, String, String) -> Unit,
    onStartNewMatch: () -> Unit,
    onOpenCreation: () -> Unit,
    onOpenArena: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenLetters: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val detail = state.matchDetail
    val queue = state.matchQueue
    val result = state.matchResult
    var selectedManualId by rememberSaveable { mutableStateOf<String?>(null) }
    var enteredMatchId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedManual = state.matchManuals.firstOrNull { it.id == selectedManualId }
    val matchFound = result == null && detail != null && queue?.status == "MATCHED" &&
        enteredMatchId != detail.matchId
    val matchInProgress = queue?.status == "WAITING" || matchFound ||
        (detail != null && result == null && enteredMatchId == detail.matchId)
    val showBottomNav = !matchInProgress
    val unavailable: (String) -> Unit = { label ->
        scope.launch {
            snackbarHost.showSnackbar("$label 的定向邀请接口尚未开放；不会进入随机匹配池")
        }
    }
    val subtitle = when {
        result != null -> "判招复盘"
        matchFound -> "匹配成功"
        detail?.status == "ACTIVE" -> "知识比拼"
        detail?.status == "AWAITING_JUDGMENT" -> "等待评审"
        queue?.status == "WAITING" -> "实时匹配"
        selectedManual != null -> "选择入场方式"
        else -> "选择秘籍"
    }
    val handleTopAction: () -> Unit = if (matchInProgress) {
        onRefresh
    } else {
        {
            if (selectedManual == null) {
                scope.launch { snackbarHost.showSnackbar("请先选择一页秘籍，再邀请队友") }
            } else {
                unavailable("邀请队友")
            }
            Unit
        }
    }

    ArenaShell(
        title = "竹峰武会",
        subtitle = subtitle,
        onBack = if (selectedManual != null && !matchInProgress && result == null) {
            { selectedManualId = null }
        } else {
            onBack
        },
        onTopAction = handleTopAction,
        topActionContentDescription = if (matchInProgress) "刷新匹配状态" else "邀请队友",
        topActionIsRefresh = matchInProgress,
        snackbarHost = snackbarHost,
        activeNav = if (result != null) ArenaNav.Record else ArenaNav.Arena,
        onArena = onOpenArena,
        onRecord = onOpenRecords,
        onLetters = onOpenLetters,
        showBottomNav = showBottomNav,
    ) {
        LazyColumn(
            modifier = Modifier
                .arenaScrollPadding(showBottomNav)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.error?.let { message ->
                item { ArenaInlineMessage(message, "重试", onRefresh) }
            }
            when {
                result != null -> item {
                    ArenaResultCard(
                        result = result,
                        busy = state.loading,
                        onCreateEvaluation = { kind, score, summary, strength, improvement ->
                            onCreateEvaluation(result.matchId, kind, score, summary, strength, improvement)
                        },
                        onCreateReflection = { learned, improvement ->
                            onCreateReflection(result.matchId, learned, improvement)
                        },
                        onStartNewMatch = onStartNewMatch,
                        onOpenCreation = onOpenCreation,
                        onRequestReview = {
                            scope.launch { snackbarHost.showSnackbar("人工复核接口尚未开放，当前评审依据已完整保留") }
                        },
                    )
                }
                matchFound -> item {
                    ArenaMatchFoundCard(
                        queue = queue,
                        onEnter = { enteredMatchId = detail?.matchId },
                        onExit = onExit,
                    )
                }
                detail != null -> {
                    item { ArenaMatchHeader(detail, queue?.anonymousOpponent?.alias ?: "竹影同门") }
                    when (detail.status) {
                        "ACTIVE" -> {
                            val answeredIds = detail.myAnswers.mapTo(mutableSetOf()) { it.questionId }
                            val question = detail.questions.firstOrNull { it.id !in answeredIds }
                            if (question != null) {
                                item(key = question.id) {
                                    ArenaQuestionCard(
                                        question = question,
                                        total = detail.questions.size,
                                        enabled = !state.loading,
                                        onSubmit = { answer, reason ->
                                            onSubmitAnswer(detail.matchId, question.id, answer, reason)
                                        },
                                    )
                                }
                            } else {
                                item { ArenaCoachNote("本方题目已经完成，正在等同门交卷。") }
                            }
                        }
                        "AWAITING_JUDGMENT" -> item {
                            ArenaCoachNote("双方都已完成，AI 正在依据题面、回答和理由进行匿名评审。")
                        }
                    }
                    if (detail.status != "ENDED") {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onExit,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
                                ) { Text("退出切磋") }
                                OutlinedButton(
                                    onClick = { onReport(detail.matchId, "SAFETY_CONCERN") },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaCinnabar),
                                ) { Text("举报并结束") }
                            }
                        }
                    }
                }
                queue?.status == "WAITING" -> item {
                    ArenaWaitingCard(queue = queue, onExit = onExit)
                }
                selectedManual != null -> item {
                    ArenaEntryChoiceCard(
                        manual = selectedManual,
                        busy = state.loading,
                        onChangeManual = { selectedManualId = null },
                        onStartMatch = { onJoin(selectedManual.id) },
                        onFriendInvite = { unavailable("好友邀请") },
                        onClassmateInvite = { unavailable("班级同门") },
                        onPasscode = { unavailable("输入口令") },
                    )
                }
                else -> {
                    item { ArenaOrnamentLine("选择秘籍") }
                    item {
                        Text(
                            "选一页已经学过的秘籍，与修炼进度相近的同门完成三式比拼。",
                            color = ArenaPaperLight,
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                        )
                    }
                    if (state.loading && state.matchManuals.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ArenaJade900)
                            }
                        }
                    }
                    items(state.matchManuals, key = ManualPageDto::id) { manual ->
                        ArenaManualCard(manual = manual, onClick = { selectedManualId = manual.id })
                    }
                    if (!state.loading && state.matchManuals.isEmpty()) {
                        item { ArenaInlineMessage("暂无可用于切磋的已学秘籍", "刷新", onRefresh) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ConferenceArenaRecordsContent(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFilter: (String?, String?) -> Unit,
    onLoadMore: () -> Unit,
    onOpenMatch: (String) -> Unit,
    onOpenArena: () -> Unit,
    onOpenLetters: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val summary = state.matchRecordSummary
    ArenaShell(
        title = "竹峰武会",
        subtitle = "战绩复盘",
        onBack = onBack,
        onTopAction = onRefresh,
        topActionContentDescription = "刷新战绩",
        topActionIsRefresh = true,
        snackbarHost = snackbarHost,
        activeNav = ArenaNav.Record,
        onArena = onOpenArena,
        onRecord = {},
        onLetters = onOpenLetters,
    ) {
        LazyColumn(
            modifier = Modifier.arenaScrollPadding().fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ArenaRecordOverview(
                    total = summary?.total ?: 0,
                    wins = summary?.wins ?: 0,
                    ties = summary?.ties ?: 0,
                    pendingReflections = summary?.pendingReflections ?: 0,
                )
            }
            item {
                ArenaRecordFilters(
                    outcome = state.matchRecordsOutcome,
                    reflectionStatus = state.matchRecordsReflectionStatus,
                    onFilter = onFilter,
                )
            }
            state.error?.let { error ->
                item { ArenaInlineMessage(error, "重新载入", onRefresh) }
            }
            if (state.loading && state.matchRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ArenaPaperLight)
                    }
                }
            }
            items(state.matchRecords, key = ConferenceMatchRecordDto::matchId) { record ->
                ArenaRecordCard(record = record, onClick = { onOpenMatch(record.matchId) })
            }
            if (!state.loading && state.matchRecords.isEmpty() && state.error == null) {
                item { ArenaEmptyRecords(onOpenArena) }
            }
            if (state.matchRecordsHasMore) {
                item {
                    OutlinedButton(
                        onClick = onLoadMore,
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ArenaPaperLight,
                            disabledContentColor = ArenaPaperLight.copy(alpha = 0.55f),
                        ),
                    ) {
                        Text(if (state.loading) "载入中…" else "载入更多战绩", fontSize = 17.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArenaRecordOverview(
    total: Int,
    wins: Int,
    ties: Int,
    pendingReflections: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaCardShape,
        color = ArenaPaperLight.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.82f)),
        shadowElevation = 5.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ArenaCardHeading("我的切磋卷宗", "录")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ArenaRecordMetric("总切磋", total, Modifier.weight(1f))
                ArenaRecordMetric("胜出", wins, Modifier.weight(1f))
                ArenaRecordMetric("平局", ties, Modifier.weight(1f))
                ArenaRecordMetric("待复盘", pendingReflections, Modifier.weight(1f), emphasize = pendingReflections > 0)
            }
        }
    }
}

@Composable
private fun ArenaRecordMetric(
    label: String,
    value: Int,
    modifier: Modifier,
    emphasize: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (emphasize) ArenaPaperDeep.copy(alpha = 0.68f) else ArenaJade300.copy(alpha = 0.66f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(value.toString(), color = ArenaInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(label, color = ArenaInkSoft, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ArenaRecordFilters(
    outcome: String?,
    reflectionStatus: String?,
    onFilter: (String?, String?) -> Unit,
) {
    val active = when {
        reflectionStatus == "PENDING" -> "待复盘"
        outcome == "WIN" -> "胜出"
        outcome == "TIE" -> "平局"
        outcome == "LOSE" -> "惜败"
        else -> "全部"
    }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        ArenaDarkOrnamentLine("筛选战绩")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(listOf("全部", "胜出", "平局", "惜败", "待复盘")) { label ->
                val selected = active == label
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .semantics {
                            this.selected = selected
                            contentDescription = "$label${if (selected) "，已选中" else ""}"
                        }
                        .clickable {
                            when (label) {
                                "胜出" -> onFilter("WIN", null)
                                "平局" -> onFilter("TIE", null)
                                "惜败" -> onFilter("LOSE", null)
                                "待复盘" -> onFilter(null, "PENDING")
                                else -> onFilter(null, null)
                            }
                        },
                    shape = RoundedCornerShape(15.dp),
                    color = if (selected) ArenaGold else ArenaPaperLight.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) ArenaPaperLight else ArenaGold.copy(alpha = 0.72f),
                    ),
                ) {
                    Box(Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            color = ArenaInk,
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArenaRecordCard(record: ConferenceMatchRecordDto, onClick: () -> Unit) {
    val outcomeLabel = when (record.outcome) {
        "WIN" -> "胜出"
        "TIE" -> "平局"
        "LOSE" -> "惜败"
        else -> "本局已结束"
    }
    val outcomeGlyph = when (record.outcome) {
        "WIN" -> "胜"
        "TIE" -> "和"
        "LOSE" -> "惜"
        else -> "止"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = ArenaCardShape,
        color = ArenaPaperLight.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.75f)),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                ArenaRoundGlyph(outcomeGlyph, 62.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(record.manualTitle, color = ArenaInk, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "第 ${record.manualPageNo} 页 · ${formatArenaDate(record.endedAt ?: record.createdAt)}",
                        color = ArenaInkSoft,
                        fontSize = 15.sp,
                    )
                }
                Surface(shape = RoundedCornerShape(12.dp), color = ArenaJade300) {
                    Text(
                        outcomeLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = ArenaInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalDivider(color = ArenaGold.copy(alpha = 0.48f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("我的得分", color = ArenaInkSoft, fontSize = 14.sp)
                    Text(record.myScore?.roundToInt()?.toString() ?: "—", color = ArenaInk, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                }
                Text("：", color = ArenaJade900, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(record.anonymousOpponent.alias, color = ArenaInkSoft, fontSize = 14.sp)
                    Text(record.opponentScore?.roundToInt()?.toString() ?: "—", color = ArenaInk, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                }
            }
            ArenaChips(
                listOf(
                    when (record.judgmentStatus) {
                        "COMPLETED" -> "AI 评审完成"
                        "NOT_REQUIRED" -> "本局不计评审"
                        else -> "等待 AI 评审"
                    },
                    if (record.reflectionStatus == "COMPLETED") "已完成复盘" else "待填写复盘",
                    "匿名对局",
                ),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClick, modifier = Modifier.height(48.dp)) {
                    Text(
                        if (record.reflectionStatus == "COMPLETED") "查看复盘  ›" else "补写复盘  ›",
                        color = ArenaJade950,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArenaEmptyRecords(onOpenArena: () -> Unit) {
    ArenaPaperCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_panda),
                contentDescription = null,
                modifier = Modifier.size(width = 138.dp, height = 168.dp),
                contentScale = ContentScale.Fit,
            )
            Text("卷宗尚空", color = ArenaInk, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(
                "还没有完成的切磋，去武会完成第一局吧。",
                color = ArenaInkSoft,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
            )
            ArenaPrimaryButton("去武会", onClick = onOpenArena)
        }
    }
}

@Composable
internal fun ConferenceArenaLettersContent(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onMarkRead: (ConferenceLetterDto) -> Unit,
    onOpenTarget: (ConferenceLetterDto) -> Boolean,
    onOpenArena: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenPublicationInbox: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    var category by rememberSaveable { mutableStateOf("ALL") }
    var detailLetter by remember { mutableStateOf<ConferenceLetterDto?>(null) }
    val visibleLetters = remember(state.letters, category) {
        if (category == "ALL") state.letters else state.letters.filter { it.category == category }
    }

    ArenaShell(
        title = "大会书信",
        subtitle = "${state.unreadLetterCount} 封未读 · 武会通知与待办",
        onBack = onBack,
        onTopAction = onRefresh,
        topActionContentDescription = "刷新大会书信",
        topActionIsRefresh = true,
        snackbarHost = snackbarHost,
        activeNav = ArenaNav.Letters,
        onArena = onOpenArena,
        onRecord = onOpenRecords,
        onLetters = {},
    ) {
        LazyColumn(
            modifier = Modifier.arenaScrollPadding().fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ArenaPaperCard {
                    ArenaCardHeading("武会专属通知与待办", "信")
                    Text(
                        "比拼结果、匿名评审、同门互评与共创授权都会送到这里。",
                        color = ArenaInkSoft,
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                    )
                }
            }
            item {
                ArenaLetterFilters(category = category, onCategory = { category = it })
            }
            state.error?.let { error ->
                item { ArenaInlineMessage(error, "重新载入", onRefresh) }
            }
            if (state.loading && state.letters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ArenaPaperLight)
                    }
                }
            }
            items(visibleLetters, key = ConferenceLetterDto::id) { letter ->
                ArenaLetterCard(
                    letter = letter,
                    onClick = {
                        onMarkRead(letter)
                        if (letter.category == "SYSTEM") {
                            detailLetter = letter
                        } else if (!onOpenTarget(letter)) {
                            detailLetter = letter
                            scope.launch {
                                snackbarHost.showSnackbar("这封书信暂时没有可打开的目标，已为你保留正文")
                            }
                        }
                    },
                )
            }
            if (!state.loading && visibleLetters.isEmpty() && state.error == null) {
                item { ArenaEmptyLetters(onRefresh) }
            }
            item {
                TextButton(
                    onClick = onOpenPublicationInbox,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = ArenaPaperLight),
                ) {
                    Text("前往全部书信  ›", fontSize = 16.sp)
                }
            }
        }
    }

    detailLetter?.let { letter ->
        AlertDialog(
            onDismissRequest = { detailLetter = null },
            title = { Text(letter.title, color = ArenaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(letter.body, color = ArenaInkSoft, fontSize = 17.sp, lineHeight = 25.sp)
                    Text(formatArenaDateTime(letter.createdAt), color = ArenaInkSoft, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { detailLetter = null }, modifier = Modifier.height(48.dp)) {
                    Text("收起竹简", color = ArenaJade950, fontSize = 16.sp)
                }
            },
            containerColor = ArenaPaperLight,
        )
    }
}

@Composable
private fun ArenaLetterFilters(category: String, onCategory: (String) -> Unit) {
    val choices = listOf(
        "ALL" to "全部",
        "MATCH" to "比拼",
        "REVIEW" to "评语",
        "DERIVATIVE" to "共创授权",
        "SYSTEM" to "系统",
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        ArenaDarkOrnamentLine("分拣竹简")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(choices) { (value, label) ->
                val selected = category == value
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .semantics {
                            this.selected = selected
                            contentDescription = "$label${if (selected) "，已选中" else ""}"
                        }
                        .clickable { onCategory(value) },
                    shape = RoundedCornerShape(15.dp),
                    color = if (selected) ArenaGold else ArenaPaperLight.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.72f)),
                ) {
                    Box(Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            color = ArenaInk,
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArenaLetterCard(letter: ConferenceLetterDto, onClick: () -> Unit) {
    val categoryLabel = when (letter.category) {
        "MATCH" -> "比拼"
        "REVIEW" -> "评语"
        "DERIVATIVE" -> "共创授权"
        else -> "系统"
    }
    val glyph = when (letter.category) {
        "MATCH" -> "武"
        "REVIEW" -> "评"
        "DERIVATIVE" -> "创"
        else -> "告"
    }
    val needsAction = letter.actionType in setOf("DERIVATIVE_REQUEST", "DERIVATIVE_AUTHORIZATION")
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = ArenaCardShape,
        color = if (letter.isRead) ArenaPaperLight.copy(alpha = 0.94f) else ArenaJade300.copy(alpha = 0.97f),
        border = androidx.compose.foundation.BorderStroke(
            if (letter.isRead) 1.dp else 2.dp,
            if (letter.isRead) ArenaGold.copy(alpha = 0.58f) else ArenaGold,
        ),
        shadowElevation = if (letter.isRead) 2.dp else 5.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!letter.isRead) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(ArenaGold))
            }
            Row(
                modifier = Modifier.weight(1f).padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                ArenaRoundGlyph(glyph, 58.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        if (!letter.isRead) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(ArenaCinnabar))
                        }
                        Text(
                            letter.title,
                            color = ArenaInk,
                            fontSize = 19.sp,
                            fontWeight = if (letter.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            maxLines = 2,
                        )
                    }
                    Text(letter.body, color = ArenaInkSoft, fontSize = 16.sp, lineHeight = 23.sp, maxLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ArenaMiniTag(categoryLabel)
                        if (!letter.isRead) ArenaMiniTag("未读")
                        if (needsAction) ArenaMiniTag("待处理", emphasized = true)
                        Text(formatArenaDateTime(letter.createdAt), color = ArenaInkSoft, fontSize = 14.sp)
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "打开${letter.title}",
                    tint = ArenaJade900,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ArenaMiniTag(text: String, emphasized: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (emphasized) ArenaGold.copy(alpha = 0.8f) else ArenaPaperLight.copy(alpha = 0.76f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.7f)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = ArenaInk,
            fontSize = 14.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ArenaEmptyLetters(onRefresh: () -> Unit) {
    ArenaPaperCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_panda),
                contentDescription = null,
                modifier = Modifier.size(width = 126.dp, height = 154.dp),
                contentScale = ContentScale.Fit,
            )
            Text("竹简尚空", color = ArenaInk, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(
                "比拼结果、评审和共创邀请会送到这里。",
                color = ArenaInkSoft,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("刷新书信", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatArenaDate(value: String): String = value.substringBefore('T')

private fun formatArenaDateTime(value: String): String {
    val date = value.substringBefore('T')
    val time = value.substringAfter('T', "").take(5)
    return if (time.isBlank()) date else "$date  $time"
}

@Composable
private fun ArenaEntryChoiceCard(
    manual: ManualPageDto,
    busy: Boolean,
    onChangeManual: () -> Unit,
    onStartMatch: () -> Unit,
    onFriendInvite: () -> Unit,
    onClassmateInvite: () -> Unit,
    onPasscode: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ArenaCardShape,
            color = ArenaPaperLight.copy(alpha = 0.97f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.78f)),
            shadowElevation = 5.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(ArenaJade300)
                        .border(2.dp, ArenaGold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = ArenaJade950, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("已选择 · 知识比拼", color = ArenaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${manual.title} · 3 道情境题 · 需要写明理由",
                        color = ArenaInkSoft,
                        fontSize = 16.sp,
                        lineHeight = 23.sp,
                    )
                }
                TextButton(onClick = onChangeManual, modifier = Modifier.height(48.dp)) {
                    Text("更换内容", color = ArenaJade950, fontSize = 16.sp)
                }
            }
        }
        ArenaDarkOrnamentLine("第二步 · 选择入场方式")
        ArenaPaperCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ArenaRoundGlyph("剑", 72.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("同门匹配", color = ArenaInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "进入实时匹配池，按年龄与修炼阶段寻找对手",
                        color = ArenaInkSoft,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                    )
                    ArenaChips(listOf("同龄优先", "阶段相近"))
                }
            }
            ArenaPrimaryButton(
                text = if (busy) "正在进入匹配池…" else "开始匹配",
                enabled = !busy,
                onClick = onStartMatch,
            )
        }
        ArenaPaperCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ArenaRoundGlyph("邀", 68.dp)
                Column(Modifier.weight(1f)) {
                    Text("邀请队友", color = ArenaInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "指定熟人切磋，不进入随机匹配池",
                        color = ArenaInkSoft,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArenaQuickAction("好友邀请", "从好友中选择", Modifier.weight(1f), onFriendInvite)
                ArenaQuickAction("班级同门", "从已加入班级选择", Modifier.weight(1f), onClassmateInvite)
                ArenaQuickAction("输入口令", "加入已有房间", Modifier.weight(1f), onPasscode)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ArenaJade300.copy(alpha = 0.62f),
            ) {
                Text(
                    "ⓘ  班级同门属于定向邀请，与随机同门匹配不重复。",
                    modifier = Modifier.padding(13.dp),
                    color = ArenaInkSoft,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

@Composable
private fun ArenaWaitingCard(queue: ConferenceMatchQueueDto, onExit: () -> Unit) {
    val phaseStep = when (queue.phase) {
        "JOINED" -> 1
        "LOCKED" -> 3
        else -> 2
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ArenaSelectedMatchStrip(queue)
        ArenaPaperCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                ArenaDarkOrnamentLine("正在寻访同门")
                Text(
                    "基于你的年龄与修炼阶段，服务器正在筛选合适的对手…",
                    color = ArenaInkSoft,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ArenaLivePortrait(
                        isSelf = true,
                        name = "小枢",
                        state = "已进入匹配池",
                        modifier = Modifier.weight(1f),
                    )
                    Text("VS", color = ArenaJade900, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    ArenaLivePortrait(
                        isSelf = false,
                        name = "尚未锁定",
                        state = "等待服务器返回",
                        modifier = Modifier.weight(1f),
                    )
                }
                BambooProgress(
                    progress = phaseStep / 3f,
                    labels = listOf("当前阶段 $phaseStep / 3"),
                    description = "服务器匹配阶段 $phaseStep / 3",
                )
                ArenaQueueStageRow("已加入匹配池", "已提交年龄与修炼阶段", completed = true)
                ArenaQueueStageRow("正在筛选同龄与修炼阶段", "服务器正在匹配中…", active = true)
                ArenaQueueStageRow("锁定真实对手", "匹配成功后由服务器确认", completed = false)
                ArenaQueueStats(queue.poolSize, queue.waitSeconds)
                Text(
                    "ⓘ  匹配池人数、阶段与对手资料均由服务器实时返回",
                    color = ArenaInkSoft,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
                ) { Text("退出匹配", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ArenaMatchFoundCard(
    queue: ConferenceMatchQueueDto?,
    onEnter: () -> Unit,
    onExit: () -> Unit,
) {
    val opponent = queue?.anonymousOpponent
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (queue != null) ArenaSelectedMatchStrip(queue)
        ArenaPaperCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ArenaDarkOrnamentLine("同门已至")
                Text(
                    "服务器已找到修炼阶段相近的同门",
                    color = ArenaInkSoft,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ArenaMatchedPortrait(true, "小枢", "已准备", Modifier.weight(1f))
                    Text("VS", color = ArenaJade900, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    ArenaMatchedPortrait(
                        false,
                        opponent?.alias ?: "竹影同门",
                        listOfNotNull(opponent?.ageBandLabel, opponent?.stageLabel).joinToString(" · ").ifBlank { "同龄 · 同阶段" },
                        Modifier.weight(1f),
                    )
                }
                BambooProgress(
                    progress = 1f,
                    labels = listOf("匹配阶段 3 / 3"),
                    description = "服务器已锁定真实对手",
                )
                ArenaQueueStageRow("已加入匹配池", "匹配请求已接收", completed = true)
                ArenaQueueStageRow("同龄与修炼阶段已校验", "筛选条件已通过", completed = true)
                ArenaQueueStageRow("真实对手已锁定", "服务器已确认本次对局", completed = true)
                ArenaServerReceipt(queue)
                Text(
                    "ⓘ  对手身份、对局编号与匹配结果均来自本次服务器回执",
                    color = ArenaInkSoft,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
                ArenaPrimaryButton("进入比拼", onClick = onEnter)
                TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("退出本局", color = ArenaJade950, fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ArenaSelectedMatchStrip(queue: ConferenceMatchQueueDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaCardShape,
        color = ArenaPaperLight.copy(alpha = 0.97f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.78f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(ArenaJade300)
                    .border(2.dp, ArenaGold, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = ArenaJade950, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "知识比拼 · ${queue.manualTitle ?: "已选秘籍"}",
                    color = ArenaInk,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "3 道情境题 · 需要写明理由",
                    color = ArenaInkSoft,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun ArenaRoundGlyph(glyph: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(ArenaJade300)
            .border(2.dp, ArenaGold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, color = ArenaJade950, fontSize = 27.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ArenaDarkOrnamentLine(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = ArenaInk.copy(alpha = 0.56f))
        Text(text, color = ArenaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        HorizontalDivider(Modifier.weight(1f), color = ArenaInk.copy(alpha = 0.56f))
    }
}

@Composable
private fun ArenaLivePortrait(
    isSelf: Boolean,
    name: String,
    state: String,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier.size(92.dp).clip(CircleShape)
                .background(if (isSelf) ArenaJade300 else ArenaJade300.copy(alpha = 0.62f))
                .border(3.dp, ArenaGold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelf) {
                Image(
                    painterResource(R.drawable.img_yanwuchang_panda),
                    contentDescription = "我的熊猫头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(72.dp),
                    color = ArenaJade800,
                    strokeWidth = 3.dp,
                )
                Icon(
                    Icons.Outlined.Groups,
                    contentDescription = "等待服务器返回对手",
                    tint = ArenaJade900.copy(alpha = 0.68f),
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        Text(name, color = ArenaInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(state, color = ArenaInkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ArenaMatchedPortrait(
    isSelf: Boolean,
    name: String,
    state: String,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(ArenaJade300)
                .border(3.dp, ArenaGold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(if (isSelf) R.drawable.img_yanwuchang_panda else R.drawable.img_shilian_panda),
                contentDescription = if (isSelf) "我的熊猫头像" else "服务器分配的匿名同门头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (!isSelf) {
                Text(
                    "匿名保护",
                    modifier = Modifier.align(Alignment.BottomCenter).background(ArenaJade950.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = ArenaPaperLight,
                    fontSize = 15.sp,
                )
            }
        }
        Text(name, color = ArenaInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(state, color = ArenaInkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ArenaQueueStageRow(
    title: String,
    detail: String,
    completed: Boolean = false,
    active: Boolean = false,
) {
    val mark = when {
        completed -> "✓"
        active -> "…"
        else -> "○"
    }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
            .background(if (active) ArenaJade300.copy(alpha = 0.72f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(if (completed || active) ArenaJade900 else Color.Transparent)
                .border(2.dp, ArenaJade900, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(mark, color = if (completed || active) ArenaPaperLight else ArenaJade900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(title, modifier = Modifier.weight(1f), color = ArenaInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(detail, modifier = Modifier.weight(1f), color = ArenaInkSoft, fontSize = 15.sp, textAlign = TextAlign.End, lineHeight = 20.sp)
    }
}

@Composable
private fun ArenaQueueStats(poolSize: Int, waitSeconds: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ArenaJade300.copy(alpha = 0.62f),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("匹配池在线", color = ArenaInkSoft, fontSize = 16.sp)
                Text("$poolSize 人", color = ArenaInk, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.width(1.dp).height(54.dp).background(ArenaInk.copy(alpha = 0.2f)))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("已等待", color = ArenaInkSoft, fontSize = 16.sp)
                Text(formatMatchDuration(waitSeconds), color = ArenaInk, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ArenaServerReceipt(queue: ConferenceMatchQueueDto?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ArenaJade300.copy(alpha = 0.62f),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("对局编号", color = ArenaInkSoft, fontSize = 16.sp)
                Text(queue?.matchCode ?: "—", color = ArenaInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.width(1.dp).height(54.dp).background(ArenaInk.copy(alpha = 0.2f)))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("服务器确认", color = ArenaInkSoft, fontSize = 16.sp)
                Text(formatServerClock(queue?.serverTime), color = ArenaInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatMatchDuration(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safeSeconds / 60, safeSeconds % 60)
}

private fun formatServerClock(value: String?): String = value
    ?.substringAfter('T', "")
    ?.take(8)
    ?.takeIf { it.length == 8 }
    ?: "—"

@Composable
private fun ArenaMatchHeader(detail: ConferenceMatchDetailDto, opponentAlias: String) {
    val total = detail.myProgress.total.coerceAtLeast(1)
    ArenaMatchHeaderBase(
        centerLabel = "VS",
        leftCaption = "小枢\n已答 ${detail.myProgress.answered}/$total",
        rightCaption = "$opponentAlias\n已答 ${detail.opponentProgress.answered}/$total",
        progress = detail.myProgress.answered.toFloat() / total,
        labels = listOf("我的进度", "第 ${detail.myProgress.answered.coerceAtMost(total) + 1} 式 / $total", "同门进度"),
        description = "知识比拼答题进度",
    )
}

@Composable
private fun ArenaMatchHeaderBase(
    centerLabel: String,
    leftCaption: String,
    rightCaption: String,
    progress: Float,
    labels: List<String>,
    description: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaModeShape,
        color = ArenaJade300.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.7f)),
        shadowElevation = 5.dp,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArenaMiniPlayer(true, leftCaption, Modifier.weight(1f))
                Text(centerLabel, color = ArenaJade900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ArenaMiniPlayer(false, rightCaption, Modifier.weight(1f))
            }
            BambooProgress(progress, labels, description)
        }
    }
}

@Composable
private fun ArenaQuestionCard(
    question: ConferenceMatchQuestionDto,
    total: Int,
    enabled: Boolean,
    onSubmit: (String, String) -> Unit,
) {
    var answer by rememberSaveable(question.id) { mutableStateOf("") }
    var reason by rememberSaveable(question.id) { mutableStateOf("") }
    ArenaPaperCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                ArenaQuestionKindLabels[question.kind] ?: question.kind,
                color = ArenaJade900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("第 ${question.position} 式 / $total · 不倒计时", color = ArenaInkSoft, fontSize = 15.sp)
        }
        Text(
            question.prompt,
            color = ArenaInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            letterSpacing = 0.6.sp,
        )
        Text(
            "写出你的回答与判断依据。对方只会看到完成进度，不会看到你的答案。",
            color = ArenaInkSoft,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        ArenaAnswerField(answer, { answer = it.take(2_000) }, "你的回答", enabled, 3)
        ArenaAnswerField(reason, { reason = it.take(1_000) }, "理由与依据", enabled, 2)
        ArenaPrimaryButton(
            text = "确认回答并说明理由",
            enabled = enabled && answer.isNotBlank() && reason.isNotBlank(),
            onClick = { onSubmit(answer, reason) },
        )
    }
}

@Composable
private fun ArenaResultCard(
    result: ConferenceMatchResultDto,
    busy: Boolean,
    onCreateEvaluation: (String, Double, String, String, String) -> Unit,
    onCreateReflection: (String, String) -> Unit,
    onStartNewMatch: () -> Unit,
    onOpenCreation: () -> Unit,
    onRequestReview: () -> Unit,
) {
    val self = result.participants.firstOrNull { it.perspective == "SELF" }
    val opponent = result.participants.firstOrNull { it.perspective == "OPPONENT" }
    val aiEvaluation = self?.evaluations?.firstOrNull { it.kind == "AI" }
    val selfSubmitted = self?.evaluations?.any { it.kind == "SELF" } == true
    val peerSubmitted = opponent?.evaluations?.any { it.kind == "PEER" } == true
    var showEvaluation by rememberSaveable(result.matchId) { mutableStateOf(false) }
    var evaluationKind by rememberSaveable(result.matchId) { mutableStateOf(if (selfSubmitted) "PEER" else "SELF") }
    var score by rememberSaveable(result.matchId) { mutableStateOf("80") }
    var summary by rememberSaveable(result.matchId) { mutableStateOf("") }
    var strength by rememberSaveable(result.matchId) { mutableStateOf("") }
    var improvement by rememberSaveable(result.matchId) { mutableStateOf("") }
    var showReflection by rememberSaveable(result.matchId) { mutableStateOf(false) }
    var learned by rememberSaveable(result.matchId) { mutableStateOf("") }
    var nextImprovement by rememberSaveable(result.matchId) { mutableStateOf("") }

    ArenaPaperCard {
        HorizontalDivider(color = ArenaGold)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("◆   此招已成   ◆", color = ArenaJade950, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("AI 辅助判招 · 人工可复核", color = ArenaInkSoft, fontSize = 15.sp, letterSpacing = 1.sp)
        }
        HorizontalDivider(color = ArenaGold)
        Text(
            "先看这次练会了什么，再看分数。胜负只表示本局结果，不影响已有学习成果。",
            color = ArenaInkSoft,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ArenaScore(self?.score, "我的表现", Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(64.dp).background(ArenaJade900.copy(alpha = 0.2f)))
            ArenaScore(opponent?.score, "同门表现", Modifier.weight(1f))
        }
        aiEvaluation?.dimensionScores?.forEach { (label, value) ->
            ArenaRubricRow(label, value)
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ArenaJade300.copy(alpha = 0.62f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArenaJade700.copy(alpha = 0.35f)),
        ) {
            Text(
                buildString {
                    append("AI 判招依据\n")
                    append(aiEvaluation?.summary ?: ArenaOutcomeLabels[result.outcome] ?: result.outcome)
                    aiEvaluation?.strengths?.firstOrNull()?.let { append("\n做得好：$it") }
                    aiEvaluation?.improvements?.firstOrNull()?.let { append("\n下一步：$it") }
                },
                modifier = Modifier.padding(12.dp),
                color = ArenaInk,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    evaluationKind = if (!selfSubmitted) "SELF" else "PEER"
                    showEvaluation = true
                },
                enabled = !busy && (!selfSubmitted || !peerSubmitted),
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
            ) { Text(if (!selfSubmitted) "提交自评" else if (!peerSubmitted) "提交互评" else "评价已提交") }
            OutlinedButton(
                onClick = onRequestReview,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
            ) { Text("申请复核") }
        }
        if (showEvaluation) {
            ArenaAnswerField(score, { score = it.filter(Char::isDigit).take(3) }, "分数（0—100）", !busy, 1)
            ArenaAnswerField(summary, { summary = it.take(1_000) }, "评价小结", !busy, 2)
            ArenaAnswerField(strength, { strength = it.take(300) }, "做得好的地方（可选）", !busy, 1)
            ArenaAnswerField(improvement, { improvement = it.take(300) }, "可以改进的地方（可选）", !busy, 1)
            val numericScore = score.toDoubleOrNull()
            ArenaPrimaryButton(
                text = "提交${if (evaluationKind == "SELF") "自评" else "互评"}",
                enabled = !busy && summary.isNotBlank() && numericScore != null && numericScore in 0.0..100.0,
                onClick = {
                    onCreateEvaluation(evaluationKind, numericScore ?: 0.0, summary, strength, improvement)
                    showEvaluation = false
                },
            )
        }
        if (result.myReflection == null) {
            TextButton(
                onClick = { showReflection = !showReflection },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = ArenaJade900),
            ) { Text(if (showReflection) "收起本局复盘" else "填写本局复盘") }
            if (showReflection) {
                ArenaAnswerField(learned, { learned = it.take(1_000) }, "这次学到了什么", !busy, 2)
                ArenaAnswerField(nextImprovement, { nextImprovement = it.take(1_000) }, "下一次准备怎样改进", !busy, 2)
                ArenaPrimaryButton(
                    "保存复盘",
                    enabled = !busy && learned.isNotBlank() && nextImprovement.isNotBlank(),
                    onClick = { onCreateReflection(learned, nextImprovement) },
                )
            }
        } else {
            ArenaInlineMessage("我的复盘：${result.myReflection.learned}\n下一步：${result.myReflection.nextImprovement}")
        }
        ArenaPrimaryButton("按建议回创作台修订", onClick = onOpenCreation)
        OutlinedButton(
            onClick = onStartNewMatch,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaInk),
        ) { Text("再来一局") }
    }
}

@Composable
private fun ArenaShell(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onTopAction: () -> Unit,
    topActionContentDescription: String,
    snackbarHost: SnackbarHostState,
    activeNav: ArenaNav,
    onArena: () -> Unit,
    onRecord: () -> Unit,
    onLetters: () -> Unit,
    topActionIsRefresh: Boolean = false,
    showBottomNav: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(ArenaJade300)) {
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x38F9F6DA),
                        Color(0x1FD9E9C8),
                        Color(0x6645694E),
                    ),
                ),
            ),
        )
        content()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(82.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = ArenaInk)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        title,
                        color = ArenaInk,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp,
                    )
                    Text(subtitle, color = ArenaInk.copy(alpha = 0.76f), fontSize = 15.sp, letterSpacing = 1.1.sp)
                }
                IconButton(onClick = onTopAction, modifier = Modifier.size(48.dp)) {
                    Icon(
                        if (topActionIsRefresh) Icons.Outlined.Refresh else Icons.Outlined.Groups,
                        contentDescription = topActionContentDescription,
                        tint = ArenaInk,
                    )
                }
            }
        }
        if (showBottomNav) {
            ArenaBottomNav(
                active = activeNav,
                onArena = onArena,
                onRecord = onRecord,
                onLetters = onLetters,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                .padding(bottom = if (showBottomNav) 76.dp else 12.dp),
        )
    }
}

private fun Modifier.arenaScrollPadding(showBottomNav: Boolean = true): Modifier =
    statusBarsPadding().padding(top = 88.dp, bottom = if (showBottomNav) 72.dp else 12.dp)

private enum class ArenaNav { Arena, Record, Letters }

@Composable
private fun ArenaBottomNav(
    active: ArenaNav,
    onArena: () -> Unit,
    onRecord: () -> Unit,
    onLetters: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF7DCE9CD))
            .navigationBarsPadding()
            .height(72.dp)
            .border(width = 0.5.dp, color = ArenaGold.copy(alpha = 0.7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArenaNavItem("武会", Icons.Outlined.SportsKabaddi, active == ArenaNav.Arena, Modifier.weight(1f), onArena)
        ArenaNavItem("战绩复盘", Icons.Outlined.Bookmark, active == ArenaNav.Record, Modifier.weight(1f), onRecord)
        ArenaNavItem("大会书信", Icons.Outlined.MailOutline, active == ArenaNav.Letters, Modifier.weight(1f), onLetters)
    }
}

@Composable
private fun ArenaNavItem(text: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .semantics {
                this.selected = selected
                contentDescription = "$text${if (selected) "，当前页面" else ""}"
            }
            .clickable(onClick = onClick)
            .padding(top = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.width(18.dp).height(4.dp).clip(CircleShape)
                .background(if (selected) ArenaGold else Color.Transparent),
        )
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) ArenaJade950 else ArenaInk.copy(alpha = 0.68f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(text, color = if (selected) ArenaJade950 else ArenaInk.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ArenaOrnamentLine(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalDivider(Modifier.weight(1f), color = ArenaGold.copy(alpha = 0.8f))
        Text(text, color = ArenaPaperLight, fontSize = 16.sp, letterSpacing = 1.2.sp)
        HorizontalDivider(Modifier.weight(1f), color = ArenaGold.copy(alpha = 0.8f))
    }
}

@Composable
private fun ArenaJadeTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = ArenaModeShape,
        color = if (selected) Color(0xFFF08FB791) else Color(0xEBC7DAB7),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.8f)),
        shadowElevation = if (selected) 4.dp else 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = ArenaInk, fontSize = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.3.sp)
        }
    }
}

@Composable
private fun ArenaScrollCard(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ArenaPaperLight.copy(alpha = 0.97f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArenaPaperDeep),
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 21.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
        val rollerBrush = Brush.horizontalGradient(listOf(Color(0xFFB9A178), Color(0xFFEFE1BD), Color(0xFFAA8B60)))
        Box(Modifier.align(Alignment.CenterStart).width(15.dp).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(rollerBrush).border(1.dp, Color(0xFF9B8051), RoundedCornerShape(6.dp)))
        Box(Modifier.align(Alignment.CenterEnd).width(15.dp).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(rollerBrush).border(1.dp, Color(0xFF9B8051), RoundedCornerShape(6.dp)))
    }
}

@Composable
private fun ArenaPaperCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaCardShape,
        color = ArenaPaperLight.copy(alpha = 0.97f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaPaperDeep),
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun ArenaCardHeading(title: String, seal: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = ArenaInk, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(
            seal,
            modifier = Modifier.border(1.dp, ArenaCinnabar).padding(horizontal = 7.dp, vertical = 5.dp),
            color = ArenaCinnabar,
            fontSize = 15.sp,
        )
    }
    HorizontalDivider(color = ArenaPaperDeep)
}

@Composable
private fun ArenaVersusBoard(rightName: String, rightState: String, rightGlyph: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ArenaPortrait(true, "小枢", "已准备", "枢", Modifier.weight(1f))
        Text("VS", color = ArenaJade700, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        ArenaPortrait(false, rightName, rightState, rightGlyph, Modifier.weight(1f))
    }
}

@Composable
private fun ArenaPortrait(isSelf: Boolean, name: String, state: String, glyph: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(if (isSelf) ArenaJade500 else Color(0xFFA57572)).border(3.dp, ArenaGold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelf) {
                Image(painterResource(R.drawable.img_yanwuchang_panda), contentDescription = "我的熊猫头像", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(glyph, color = ArenaPaperLight, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(name, color = ArenaInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(state, color = ArenaInkSoft, fontSize = 15.sp)
    }
}

@Composable
private fun ArenaMiniPlayer(isSelf: Boolean, caption: String, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isSelf) Arrangement.Start else Arrangement.End,
    ) {
        if (isSelf) {
            ArenaMiniAvatar(true, "枢")
            Spacer(Modifier.width(7.dp))
        }
        Text(caption, color = ArenaInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp, textAlign = if (isSelf) TextAlign.Start else TextAlign.End)
        if (!isSelf) {
            Spacer(Modifier.width(7.dp))
            ArenaMiniAvatar(false, "砚")
        }
    }
}

@Composable
private fun ArenaMiniAvatar(isSelf: Boolean, glyph: String) {
    Box(
        Modifier.size(45.dp).clip(CircleShape).background(if (isSelf) ArenaJade500 else Color(0xFFA57572)).border(2.dp, ArenaGold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelf) Image(painterResource(R.drawable.img_yanwuchang_panda), "我的头像", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(glyph, color = ArenaPaperLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BambooProgress(progress: Float, labels: List<String>, description: String) {
    val normalized = progress.coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth().semantics { progressBarRangeInfo = ProgressBarRangeInfo(normalized, 0f..1f) }) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(69.dp)) {
            val barWidth = maxWidth
            Image(
                painterResource(R.drawable.img_conference_bamboo_progress),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.36f),
                contentScale = ContentScale.FillBounds,
            )
            Box(Modifier.fillMaxHeight().fillMaxWidth(normalized).clipToBounds()) {
                Image(
                    painterResource(R.drawable.img_conference_bamboo_progress),
                    contentDescription = description,
                    modifier = Modifier.width(barWidth).fillMaxHeight(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
        if (labels.size == 1) {
            Text(
                labels.first(),
                modifier = Modifier.fillMaxWidth(),
                color = ArenaInk,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(it, color = ArenaInkSoft, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ArenaSectionLabel(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = ArenaPaperLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(trailing, color = ArenaPaperLight.copy(alpha = 0.88f), fontSize = 15.sp)
    }
}

@Composable
private fun ArenaModeCard(glyph: String, title: String, body: String, chips: List<String>, onClick: () -> Unit) {
    val glyphShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaModeShape,
        color = ArenaPaper.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.72f)),
        shadowElevation = 6.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.width(72.dp).height(88.dp).clip(glyphShape).background(Brush.verticalGradient(listOf(Color(0xFFD5E5C6), Color(0xFF91B891)))).border(1.dp, ArenaGold, glyphShape),
                contentAlignment = Alignment.Center,
            ) { Text(glyph, color = ArenaInk, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f)) {
                Text(title, color = ArenaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(body, color = ArenaInkSoft, fontSize = 17.sp, lineHeight = 24.sp)
                ArenaChips(chips)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ArenaJade700, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun ArenaChips(chips: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 7.dp)) {
        chips.take(4).chunked(2).forEach { rowChips ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowChips.forEach { chip ->
                    Text(
                        chip,
                        modifier = Modifier.border(1.dp, ArenaJade700.copy(alpha = 0.38f), CircleShape)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        color = ArenaJade950,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArenaQuickAction(title: String, body: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(112.dp), shape = ArenaModeShape, color = Color(0xEBB4CFA8), border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.7f))) {
        Column(Modifier.padding(horizontal = 5.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = ArenaInk, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(body, color = ArenaInk.copy(alpha = 0.76f), fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ArenaManualCard(manual: ManualPageDto, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = ArenaModeShape, color = ArenaPaper.copy(alpha = 0.96f), border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold.copy(alpha = 0.75f)), shadowElevation = 5.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(ArenaJade500), contentAlignment = Alignment.Center) {
                Text(manual.pageNo.toString(), color = ArenaInk, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(manual.title, color = ArenaInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("第 ${manual.pageNo} 页 · ${manual.progressLabel}", color = ArenaInkSoft, fontSize = 16.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "加入切磋", tint = ArenaJade700)
        }
    }
}

@Composable
private fun ArenaCoachNote(text: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(start = 56.dp), shape = RoundedCornerShape(17.dp, 17.dp, 17.dp, 5.dp), color = ArenaPaper.copy(alpha = 0.93f), border = androidx.compose.foundation.BorderStroke(1.dp, ArenaPaperDeep.copy(alpha = 0.8f))) {
        Text(text, modifier = Modifier.padding(14.dp, 13.dp), color = ArenaInk, fontSize = 16.sp, lineHeight = 24.sp)
    }
}

@Composable
private fun ArenaInlineMessage(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = ArenaJade300.copy(alpha = 0.9f), border = androidx.compose.foundation.BorderStroke(1.dp, ArenaJade700.copy(alpha = 0.35f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = ArenaInk, fontSize = 16.sp, lineHeight = 24.sp)
            if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel, color = ArenaJade950) }
        }
    }
}

@Composable
private fun ArenaEvidenceLine(text: String) {
    Text("◇  $text", color = ArenaInkSoft, fontSize = 15.sp, lineHeight = 22.sp)
}

@Composable
private fun ArenaStageStrip(activeIndex: Int) {
    val stages = listOf("构思", "草图", "制作", "测试", "说明")
    Row(Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        stages.forEachIndexed { index, stage ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(13.dp).clip(ArenaModeShape).background(if (index <= activeIndex) ArenaJade700 else Color(0xFFD5DCC1)).border(0.5.dp, ArenaJade700.copy(alpha = 0.35f), ArenaModeShape))
                Text(stage, color = if (index == activeIndex) ArenaJade950 else ArenaInkSoft, fontSize = 15.sp, fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun ArenaAnswerField(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean, minLines: Int) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp, 4.dp, 12.dp, 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ArenaJade300.copy(alpha = 0.45f),
            unfocusedContainerColor = ArenaJade300.copy(alpha = 0.32f),
            focusedBorderColor = ArenaJade700,
            unfocusedBorderColor = ArenaJade700.copy(alpha = 0.35f),
            focusedTextColor = ArenaInk,
            unfocusedTextColor = ArenaInk,
            focusedLabelColor = ArenaJade900,
            unfocusedLabelColor = ArenaInkSoft,
        ),
    )
}

@Composable
private fun ArenaPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = ArenaModeShape,
        colors = ButtonDefaults.buttonColors(containerColor = ArenaJade500, contentColor = ArenaInk, disabledContainerColor = ArenaJade300, disabledContentColor = ArenaInkSoft),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaGold),
    ) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ArenaScore(score: Double?, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(score?.roundToInt()?.toString() ?: "—", color = ArenaJade950, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(label, color = ArenaInkSoft, fontSize = 15.sp)
    }
}

@Composable
private fun ArenaRubricRow(label: String, value: Double) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, modifier = Modifier.width(104.dp), color = ArenaInk, fontSize = 15.sp)
        Box(Modifier.weight(1f).height(7.dp).clip(CircleShape).background(Color(0xFFE2E5CA))) {
            Box(Modifier.fillMaxWidth((value / 100.0).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFF83AF8B), Color(0xFFB2CBA4)))))
        }
        Text(value.roundToInt().toString(), modifier = Modifier.width(34.dp), color = ArenaInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private val ArenaQuestionKindLabels = mapOf(
    "CORE_LOGIC" to "核心理解",
    "CASE_ANALYSIS" to "案例分析",
    "TRANSFER" to "迁移应用",
)

private val ArenaOutcomeLabels = mapOf(
    "WIN" to "本局胜出",
    "LOSE" to "本局惜败",
    "TIE" to "本局平局",
    "PENDING" to "等待结果",
    "ENDED_WITHOUT_RESULT" to "本局已结束，不计结果",
)
