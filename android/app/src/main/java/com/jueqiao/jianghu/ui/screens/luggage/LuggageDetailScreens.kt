package com.jueqiao.jianghu.ui.screens.luggage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.CreationDetailBundle
import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.luggage.EvidenceItemDto
import com.jueqiao.jianghu.luggage.LuggageDetailState
import com.jueqiao.jianghu.luggage.ManualDetailBundle
import com.jueqiao.jianghu.luggage.ManualPageDto
import com.jueqiao.jianghu.luggage.MistakeDetailDto
import com.jueqiao.jianghu.luggage.MistakeItemDto
import com.jueqiao.jianghu.luggage.PrivacySettingsDto
import com.jueqiao.jianghu.luggage.RetrySessionDto
import com.jueqiao.jianghu.ui.components.SettingsPaperSurface
import com.jueqiao.jianghu.ui.theme.YaHei

private val DetailInk = Color(0xFF29261F)
private val DetailMuted = Color(0xFF6A655A)
private val DetailSage = Color(0xFF65775E)
private val DetailPanel = Color(0xFFF7F0E2)
private val DetailSelected = Color(0xFFD6DDC9)

@Composable
private fun LuggagePaperScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        SettingsPaperSurface(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        contentDescription = "返回",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onBack)
                            .padding(8.dp),
                    )
                    Text(
                        text = title,
                        fontFamily = YaHei,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(end = 32.dp),
                    )
                }
                content()
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun DetailStateBanner(
    state: LuggageDetailState,
    onRetry: (() -> Unit)? = null,
) {
    if (state.loading) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DetailSage)
            Text("正在加载…", modifier = Modifier.padding(start = 9.dp), color = DetailMuted)
        }
    }
    state.message?.let { message ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEEE5))
                .padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = message,
                color = Color(0xFF8C4D3D),
                fontFamily = YaHei,
                fontSize = 12.sp,
            )
            if (state.retryable && onRetry != null) {
                PaperButton("重新加载", onClick = onRetry)
            }
        }
    }
}

@Composable
private fun PaperButton(text: String, selected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) DetailSelected else DetailPanel)
            .border(1.dp, DetailSage.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = DetailInk,
            fontFamily = YaHei,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DetailPanel.copy(alpha = 0.92f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        content = content,
    )
}

@Composable
fun BadgesScreen(state: LuggageDetailState, onBack: () -> Unit, onLoad: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    LuggagePaperScreen("我的勋章", onBack) {
        DetailStateBanner(state, onLoad)
        if (!state.loading && state.badges.isEmpty()) {
            EmptyMessage("完成试炼和创作任务后，勋章会出现在这里")
        }
        state.badges.forEach { badge ->
            SectionCard {
                Text(badge.name, fontFamily = YaHei, fontWeight = FontWeight.Bold, color = DetailInk)
                Text(badge.description ?: "学习成就勋章", fontFamily = YaHei, fontSize = 13.sp, color = DetailMuted)
                Text("获得时间：${displayDate(badge.earnedAt)}", fontFamily = YaHei, fontSize = 11.sp, color = DetailMuted)
            }
        }
    }
}

@Composable
fun EvidenceScreen(
    title: String,
    weekOnly: Boolean,
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: (String?) -> Unit,
    onLoadMore: (String?) -> Unit,
    initialCategory: String? = null,
) {
    var category by remember { mutableStateOf(initialCategory) }
    LaunchedEffect(category, weekOnly) { onLoad(category) }
    LuggagePaperScreen(title, onBack) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            listOf(null to "全部", "WISDOM" to "悟性", "CRAFT" to "匠心", "CHIVALRY" to "侠义")
                .forEach { (value, label) ->
                    PaperButton(label, category == value) { category = value }
                }
        }
        DetailStateBanner(state) { onLoad(category) }
        val items = state.evidence?.items.orEmpty()
        if (!state.loading && items.isEmpty()) EmptyMessage("暂时还没有符合条件的学习证据")
        items.forEach { EvidenceRow(it) }
        if (state.evidence?.nextCursor != null) PaperButton("加载更多") { onLoadMore(category) }
    }
}

@Composable
private fun EvidenceRow(item: EvidenceItemDto) {
    SectionCard {
        Text(
            text = "${evidenceCategoryLabel(item.category)} · ${item.manualTitle ?: "综合成长"}",
            color = DetailSage,
            fontFamily = YaHei,
            fontWeight = FontWeight.SemiBold,
        )
        Text(item.summary, color = DetailInk, fontFamily = YaHei, fontSize = 13.sp)
        Text(
            "${evidenceStatusLabel(item.validationStatus)} · ${displayDate(item.createdAt)}",
            color = DetailMuted,
            fontFamily = YaHei,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun ManualsScreen(
    state: LuggageDetailState,
    initialState: String?,
    onBack: () -> Unit,
    onLoad: (Int?, String?, String?, Boolean) -> Unit,
    onLoadMore: (Int?, String?, String?, Boolean) -> Unit,
    onOpenManual: (String) -> Unit,
    onToggleFavorite: (ManualPageDto) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf(initialState) }
    var volume by remember { mutableStateOf<Int?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    val reload = { onLoad(volume, query, selectedState, favoritesOnly) }
    LaunchedEffect(selectedState, volume, favoritesOnly) { reload() }
    LuggagePaperScreen("全部秘籍", onBack) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("搜索秘籍") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Text("搜索", color = DetailSage, modifier = Modifier.clickable { reload() }.padding(8.dp)) },
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            listOf(null to "全部", "UNSEEN" to "未闻", "DISCOVERED" to "偶得", "LEARNED" to "习得", "MASTERED" to "悟得", "TEACHING" to "传习")
                .forEach { (value, label) -> PaperButton(label, selectedState == value) { selectedState = value } }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            PaperButton("十卷全部", volume == null) { volume = null }
            (1..10).forEach { number -> PaperButton("第${number}卷", volume == number) { volume = number } }
            PaperButton("仅收藏", favoritesOnly) { favoritesOnly = !favoritesOnly }
        }
        DetailStateBanner(state, reload)
        Text("共 ${state.manuals?.total ?: 0} 本", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
        val items = state.manuals?.items.orEmpty()
        if (!state.loading && items.isEmpty()) EmptyMessage("没有找到符合条件的秘籍")
        items.forEach { item ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f).clickable { onOpenManual(item.id) },
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("第${item.pageNo}页 · ${item.title}", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.SemiBold)
                        Text("第${item.volumeNo}卷 ${item.volumeTitle} · ${item.progressLabel}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
                        Text(item.coreLogic, color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    PaperButton(if (item.isFavorite) "已收藏" else "收藏", item.isFavorite) { onToggleFavorite(item) }
                }
            }
        }
        if (state.manuals?.nextCursor != null) {
            PaperButton("加载更多") { onLoadMore(volume, query, selectedState, favoritesOnly) }
        }
    }
}

@Composable
fun ManualDetailScreen(
    manualId: String,
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
) {
    LaunchedEffect(manualId) { onLoad(manualId) }
    LuggagePaperScreen("秘籍详情", onBack) {
        DetailStateBanner(state) { onLoad(manualId) }
        state.manualDetail?.let { ManualDetailContent(it) }
    }
}

@Composable
private fun ManualDetailContent(bundle: ManualDetailBundle) {
    val item = bundle.manual
    SectionCard {
        Text("《${item.title}》", color = DetailInk, fontFamily = YaHei, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("第${item.volumeNo}卷 · 当前${item.progressLabel}", color = DetailSage, fontFamily = YaHei)
        Text(item.coreLogic, color = DetailInk, fontFamily = YaHei, fontSize = 14.sp)
    }
    SectionCard {
        Text("生活连接", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Text(item.lifeHook, color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        Text("学习证据要求", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Text(item.interactionEvidence, color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
    }
    SectionCard {
        Text("达成条件", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        item.progressRequirements.forEach { requirement ->
            Text("${requirement.label}：${requirement.requirement}", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        }
    }
    SectionCard {
        Text("我的证明材料", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        if (bundle.evidence.isEmpty()) Text("尚无证明材料", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        bundle.evidence.forEach { evidence ->
            Text(
                "• ${evidence.summary} · ${evidence.createdAt.substringBefore('T')}",
                color = DetailMuted,
                fontFamily = YaHei,
                fontSize = 13.sp,
            )
        }
    }
    SectionCard {
        Text("晋级记录", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        if (bundle.history.transitions.isEmpty()) {
            Text("完成学习后，这里会记录每一次秘籍进阶。", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        }
        bundle.history.transitions.forEach { transition ->
            Text(
                "${manualStateLabel(transition.previousState)} → ${manualStateLabel(transition.currentState)} · ${transition.occurredAt.substringBefore('T')}",
                color = DetailSage,
                fontFamily = YaHei,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${learningEventLabel(transition.triggerEvent)} · " +
                    "${transition.evidenceSummary ?: "学习事件已由服务端确认"} · " +
                    "规则 ${transition.ruleVersion}",
                color = DetailMuted,
                fontFamily = YaHei,
                fontSize = 12.sp,
            )
        }
    }
}

private fun manualStateLabel(state: String): String = when (state) {
    "UNSEEN" -> "未闻"
    "DISCOVERED" -> "偶得"
    "LEARNED" -> "习得"
    "MASTERED" -> "悟得"
    "TEACHING" -> "传习"
    else -> state
}

private fun learningEventLabel(event: String): String = when (event) {
    "PREDICTION_COMPLETED" -> "完成有效预测"
    "TRIAL_PASSED" -> "试炼通过"
    "TRANSFER_EVIDENCE_APPROVED" -> "迁移证据已通过"
    "STRUCTURED_REVIEW_ACCEPTED" -> "评招已通过"
    else -> event
}

@Composable
fun MistakesScreen(
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onOpen: (String) -> Unit,
    onRetry: (String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    LuggagePaperScreen("我的错题", onBack) {
        DetailStateBanner(state, onLoad)
        val items = state.mistakes?.items.orEmpty()
        if (!state.loading && items.isEmpty()) EmptyMessage("暂时没有待巩固的错题")
        items.forEach { item -> MistakeRow(item, onOpen = { onOpen(item.id) }) { onRetry(item.id) } }
        if (state.mistakes?.nextCursor != null) PaperButton("加载更多", onClick = onLoadMore)
    }
}

@Composable
fun MistakeDetailScreen(
    mistakeId: String,
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onRetry: (String) -> Unit,
) {
    LaunchedEffect(mistakeId) { onLoad(mistakeId) }
    LuggagePaperScreen("错题详情", onBack) {
        DetailStateBanner(state) { onLoad(mistakeId) }
        state.mistakeDetail?.let { mistake ->
            SectionCard {
                Text(mistake.manualTitle, color = DetailInk, fontFamily = YaHei, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${mistakeStatusLabel(mistake.status)} · 已错 ${mistake.failureCount} 次", color = DetailSage, fontFamily = YaHei)
                Text(mistake.errorReasonSummary, color = DetailInk, fontFamily = YaHei, fontSize = 14.sp)
            }
            SectionCard {
                Text("当时的答案", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                Text(originalAnswerLabel(mistake), color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
                Text("关联知识点：${mistake.knowledgePointCode}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
            }
            SectionCard {
                Text("重练记录", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                if (mistake.remediationRecords.isEmpty()) {
                    Text("还没有重练记录。准备好后可以再试一次。", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
                }
                mistake.remediationRecords.forEach { record ->
                    Text(
                        "${if (record.result == "PASSED") "通过" else "未通过"} · ${record.occurredAt.substringBefore('T')}",
                        color = DetailSage,
                        fontFamily = YaHei,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    record.reflection?.takeIf { it.isNotBlank() }?.let { reflection ->
                        Text(reflection, color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
                    }
                }
                mistake.nextReviewAt?.let { nextReview ->
                    Text("下次巩固：${nextReview.substringBefore('T')}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
                }
                mistake.consolidatedAt?.let { consolidatedAt ->
                    Text("已巩固：${consolidatedAt.substringBefore('T')}", color = DetailSage, fontFamily = YaHei, fontSize = 12.sp)
                }
            }
            if (mistake.status != "CONSOLIDATED") PaperButton("再试一次") { onRetry(mistake.id) }
        }
    }
}

@Composable
fun RetryTrialScreen(
    session: RetrySessionDto,
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onSubmit: (String?, String, String) -> Unit,
    onComplete: () -> Unit,
) {
    var prediction by remember { mutableStateOf<String?>(null) }
    var answer by remember { mutableStateOf<String?>(null) }
    var explanation by remember { mutableStateOf("") }
    LaunchedEffect(session.trialId) { onLoad(session.trialId) }
    LuggagePaperScreen("错题重练", onBack) {
        DetailStateBanner(state) { onLoad(session.trialId) }
        val trial = state.trial
        if (trial != null) {
            val version = trial.currentVersion
            val property = version.answerSchema.getAsJsonObject("properties")
                ?.entrySet()?.firstOrNull()
            val options = property?.value?.asJsonObject?.getAsJsonArray("enum")
                ?.map { it.asString }.orEmpty()
            SectionCard {
                Text(trial.title, color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                Text(version.prompt, color = DetailInk, fontFamily = YaHei, fontSize = 14.sp)
            }
            if (version.predictionRequired) {
                SectionCard {
                    Text(version.predictionPrompt, color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                    AnswerOptions(options, prediction) { prediction = it }
                }
            }
            SectionCard {
                Text("正式答案", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                AnswerOptions(options, answer) { answer = it }
                if (version.explanationRequired) {
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        label = { Text("说明你的判断依据") },
                        supportingText = { Text("至少 ${version.minExplanationLength} 个字") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                val ready = answer != null &&
                    (!version.predictionRequired || prediction != null) &&
                    (!version.explanationRequired || explanation.trim().length >= version.minExplanationLength)
                PaperButton("提交重练") {
                    if (ready) onSubmit(prediction, answer.orEmpty(), explanation)
                }
                if (!ready) Text("请完成预测、答案和解释后再提交", color = DetailMuted, fontFamily = YaHei, fontSize = 11.sp)
            }
        }
        state.trialResult?.let { result ->
            SectionCard {
                Text(
                    if (result.passed) "重练通过" else "还需要再巩固",
                    color = if (result.passed) DetailSage else Color(0xFF8C4D3D),
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Bold,
                )
                Text("得分 ${result.score.toInt()}/${result.maxScore.toInt()}", color = DetailMuted, fontFamily = YaHei)
                PaperButton(if (result.passed) "返回错题" else "查看结果") { onComplete() }
            }
        }
    }
}

@Composable
private fun AnswerOptions(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        options.forEach { option ->
            PaperButton(option, selected == option) { onSelect(option) }
        }
    }
}

@Composable
private fun MistakeRow(item: MistakeItemDto, onOpen: () -> Unit, onRetry: () -> Unit) {
    SectionCard {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
            Text(item.manualTitle, color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
            Text(item.errorReasonSummary, color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("错误 ${item.failureCount} 次 · ${mistakeStatusLabel(item.status)}", color = DetailMuted, fontFamily = YaHei, fontSize = 11.sp, modifier = Modifier.weight(1f))
            if (item.status != "CONSOLIDATED") PaperButton("再试一次", onClick = onRetry)
        }
    }
}

private fun originalAnswerLabel(mistake: MistakeDetailDto): String = runCatching {
    mistake.originalAnswerPayload.asJsonObject.entrySet().joinToString("，") { entry ->
        "${entry.key}：${entry.value.asString}"
    }
}.getOrDefault(mistake.originalAnswerPayload.toString())

@Composable
fun CreationsScreen(
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onOpen: (String) -> Unit,
    onContinue: (String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    LuggagePaperScreen("我的作品", onBack) {
        DetailStateBanner(state, onLoad)
        val items = state.creations?.items.orEmpty()
        if (!state.loading && items.isEmpty()) EmptyMessage("还没有作品，去造物坊试试吧")
        items.forEach { project -> CreationRow(project, onOpen, onContinue) }
        if (state.creations?.nextCursor != null) PaperButton("加载更多", onClick = onLoadMore)
    }
}

@Composable
private fun CreationRow(
    project: CreationProjectDto,
    onOpen: (String) -> Unit,
    onContinue: (String) -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).clickable { onOpen(project.id) }) {
                Text(project.title, color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
                Text("${creationStatusLabel(project.displayStatus)} · 版本 ${project.currentVersionNumber ?: 0}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
                project.latestPublication?.returnReasonSummary?.let {
                    Text("退回原因：$it", color = Color(0xFF8C4D3D), fontFamily = YaHei, fontSize = 12.sp)
                }
            }
            if (project.status == "ACTIVE" && project.displayStatus != "PUBLISHED") {
                PaperButton("继续修订") { onContinue(project.id) }
            }
        }
    }
}

@Composable
fun CreationDetailScreen(
    projectId: String,
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onContinue: (String) -> Unit,
    onWithdraw: (String) -> Unit,
    onAppeal: (String, String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    LaunchedEffect(projectId) { onLoad(projectId) }
    LuggagePaperScreen("作品档案", onBack) {
        DetailStateBanner(state) { onLoad(projectId) }
        state.creationDetail?.let { CreationDetailContent(it, onContinue, onWithdraw, onAppeal, onDelete) }
    }
}

@Composable
private fun CreationDetailContent(
    bundle: CreationDetailBundle,
    onContinue: (String) -> Unit,
    onWithdraw: (String) -> Unit,
    onAppeal: (String, String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var appealReason by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    SectionCard {
        Text(bundle.project.title, color = DetailInk, fontFamily = YaHei, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("状态：${creationStatusLabel(bundle.project.displayStatus)}", color = DetailSage, fontFamily = YaHei)
        bundle.project.latestPublication?.returnReasonSummary?.let { Text("退回原因：$it", color = Color(0xFF8C4D3D), fontFamily = YaHei) }
        PaperButton("继续创作/修订") { onContinue(bundle.project.id) }
        if (bundle.project.latestPublication?.status == "PUBLISHED") {
            PaperButton("撤回已发布作品") { onWithdraw(bundle.project.id) }
        }
        if (!confirmDelete) {
            PaperButton("删除作品") { confirmDelete = true }
        } else {
            Text("删除后作品、版本和关联媒体将进入删除流程，此操作需要重新创作才能恢复。", color = Color(0xFF8C4D3D), fontFamily = YaHei, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaperButton("取消") { confirmDelete = false }
                PaperButton("确认删除") { onDelete(bundle.project.id) }
            }
        }
    }
    SectionCard {
        Text("版本记录", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        if (bundle.versions.isEmpty()) Text("尚未保存版本", color = DetailMuted)
        bundle.versions.sortedByDescending { it.versionNumber }.forEach {
            Text("V${it.versionNumber} · ${it.changeSummary} · ${displayDate(it.createdAt)}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
        }
    }
    SectionCard {
        Text("学习卡", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        val card = bundle.learningCard
        if (card == null) Text("尚未填写学习卡", color = DetailMuted) else {
            Text(card.methodSummary, color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
            Text("使用秘籍 ${card.manualPageIds.size} 招 · 未解决问题 ${card.unresolvedQuestions.size} 个", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
        }
    }
    SectionCard {
        Text("人机分工与来源谱", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        val provenance = bundle.provenance
        if (provenance == null) Text("尚未填写来源谱", color = DetailMuted) else {
            Text("我的贡献：${provenance.humanContributionSummary}", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
            Text("AI辅助：${if (provenance.aiAssistanceUsed) "是" else "否"} · AIGC标识：${if (provenance.aigcLabelDeclared) "已声明" else "未声明"}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
            Text("素材记录 ${provenance.items.size} 项 · 未解决授权 ${if (provenance.unresolvedRights) "有" else "无"}", color = DetailMuted, fontFamily = YaHei, fontSize = 12.sp)
        }
    }
    bundle.moderationCase?.let { moderation ->
        SectionCard {
            Text("审核与申诉", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
            moderation.publicReasonSummary?.let { Text("审核说明：$it", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp) }
            moderation.revisionSuggestion?.let { Text("修改建议：$it", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp) }
            if (moderation.canAppeal) {
                OutlinedTextField(
                    value = appealReason,
                    onValueChange = { appealReason = it },
                    label = { Text("申诉理由（至少10字）") },
                    modifier = Modifier.fillMaxWidth(),
                )
                PaperButton("提交审核申诉") {
                    if (appealReason.trim().length >= 10) {
                        onAppeal(bundle.project.id, moderation.id, appealReason.trim())
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySafetyScreen(
    state: LuggageDetailState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onVisibility: (String) -> Unit,
    onExport: () -> Unit,
    onRequestDeletion: (String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    LuggagePaperScreen("隐私与安全", onBack) {
        DetailStateBanner(state, onLoad)
        state.privacy?.let { settings ->
            PrivacyContent(
                settings = settings,
                exportSummary = state.accountExportSummary,
                deletionStatus = state.dataRightsRequest?.status,
                onToggle = onToggle,
                onVisibility = onVisibility,
                onExport = onExport,
                onRequestDeletion = onRequestDeletion,
            )
        }
    }
}

@Composable
private fun PrivacyContent(
    settings: PrivacySettingsDto,
    exportSummary: String?,
    deletionStatus: String?,
    onToggle: (String, Boolean) -> Unit,
    onVisibility: (String) -> Unit,
    onExport: () -> Unit,
    onRequestDeletion: (String) -> Unit,
) {
    var confirmDeletion by remember { mutableStateOf(false) }
    var deletionReason by remember { mutableStateOf("") }
    SectionCard {
        Text("作品默认可见范围", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("PRIVATE" to "仅自己", "GUARDIAN_ONLY" to "监护人", "CLASSROOM" to "班级", "COMMUNITY" to "社区")
                .forEach { (value, label) -> PaperButton(label, settings.defaultWorkVisibility == value) { onVisibility(value) } }
        }
    }
    PrivacySwitch("公开学习卡", settings.learningCardPublic) { onToggle("learning_card_public", it) }
    PrivacySwitch("导出作品保留 AIGC 标识", settings.aigcExportMarkEnabled) { onToggle("aigc_export_mark_enabled", it) }
    PrivacySwitch("允许通过资料发现我", settings.profileDiscoveryEnabled) { onToggle("profile_discovery_enabled", it) }
    SectionCard {
        Text("监护设置", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Text(if (settings.guardianControlsActive) "监护设置已启用，受限选项不可由学生绕过" else "当前未启用监护限制", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
    }
    SectionCard {
        Text("数据权利", color = DetailInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Text("作品撤回在作品档案中操作；账号数据导出和删除申请会留下审计记录。", color = DetailMuted, fontFamily = YaHei, fontSize = 13.sp)
        PaperButton("生成我的数据导出", onClick = onExport)
        exportSummary?.let { Text(it, color = DetailSage, fontFamily = YaHei, fontSize = 12.sp) }
        if (!confirmDeletion) {
            PaperButton("申请删除账号数据") { confirmDeletion = true }
        } else {
            Text("删除申请不会立即抹除数据，后台将先核验身份与监护要求。", color = Color(0xFF8C4D3D), fontFamily = YaHei, fontSize = 12.sp)
            OutlinedTextField(
                value = deletionReason,
                onValueChange = { deletionReason = it },
                label = { Text("申请原因（可选）") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaperButton("取消") { confirmDeletion = false }
                PaperButton("确认提交删除申请") {
                    onRequestDeletion(deletionReason.trim().ifBlank { "用户主动申请删除账号数据" })
                    confirmDeletion = false
                }
            }
        }
        deletionStatus?.let { Text("删除申请状态：$it", color = DetailSage, fontFamily = YaHei, fontSize = 12.sp) }
    }
}

@Composable
private fun PrivacySwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = DetailInk, fontFamily = YaHei, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(
        text = text,
        color = DetailMuted,
        fontFamily = YaHei,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
    )
}

private fun displayDate(value: String): String = value.take(10)

private fun evidenceCategoryLabel(value: String): String = when (value) {
    "WISDOM" -> "悟性"
    "CRAFT" -> "匠心"
    "CHIVALRY" -> "侠义"
    else -> value
}

private fun evidenceStatusLabel(value: String): String = when (value) {
    "VALID" -> "已验证"
    "PENDING_REVIEW" -> "待复核"
    "REVOKED" -> "已撤销"
    else -> value
}

private fun mistakeStatusLabel(value: String): String = when (value) {
    "TO_REVIEW" -> "待巩固"
    "PRACTICING" -> "重练中"
    "CONSOLIDATED" -> "已巩固"
    else -> value
}

private fun creationStatusLabel(value: String): String = when (value) {
    "DRAFT" -> "草稿"
    "PENDING_CHECK" -> "检查中"
    "PENDING_HUMAN_REVIEW" -> "待人工复核"
    "PUBLISHED" -> "已发布"
    "RETURNED" -> "已退回"
    "RESTRICTED" -> "受限"
    "WITHDRAWN" -> "已撤回"
    else -> value
}
