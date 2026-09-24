package com.jueqiao.jianghu.ui.screens.luggage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.creation.ConferencePublishDraft
import com.jueqiao.jianghu.luggage.CreationDetailBundle
import com.jueqiao.jianghu.luggage.LuggageDetailState
import com.jueqiao.jianghu.ui.components.LuggagePageScaffold
import com.jueqiao.jianghu.ui.theme.YaHei

private val PublishInk = Color(0xFF2D3F31)
private val PublishMuted = Color(0xFF647065)
private val PublishGreen = Color(0xFF6F963F)
private val PublishPaper = Color(0xEAF7F1E2)
private val PublishError = Color(0xFF8C3F35)

private data class ConferenceCategoryOption(
    val value: String,
    val label: String,
)

private val ConferenceCategories = listOf(
    ConferenceCategoryOption("ART", "艺术创作"),
    ConferenceCategoryOption("SCIENCE", "科学探索"),
    ConferenceCategoryOption("MATH", "数学思维"),
    ConferenceCategoryOption("LANGUAGE", "语言表达"),
)

/** 保存作品之后的明确投稿页；不把“保存到个人档案”和“公开发布”混成同一次操作。 */
@Composable
fun ConferencePublishScreen(
    projectId: String,
    state: LuggageDetailState,
    publishBusy: Boolean,
    publishMessage: String?,
    publishFailed: Boolean,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onSubmit: (CreationDetailBundle, ConferencePublishDraft) -> Unit,
) {
    LaunchedEffect(projectId) { onLoad(projectId) }
    val bundle = state.creationDetail?.takeIf { it.project.id == projectId }
    val currentVersion = bundle?.versions?.firstOrNull {
        it.versionNumber == bundle.project.currentVersionNumber
    } ?: bundle?.versions?.maxByOrNull { it.versionNumber }
    val currentPublication = bundle?.project?.latestPublication
        ?.takeIf { it.creationVersionId == currentVersion?.id }

    var initialized by rememberSaveable(projectId) { mutableStateOf(false) }
    var category by rememberSaveable(projectId) { mutableStateOf("") }
    var workDescription by rememberSaveable(projectId) { mutableStateOf("") }
    var learningReflection by rememberSaveable(projectId) { mutableStateOf("") }
    var nextImprovement by rememberSaveable(projectId) { mutableStateOf("") }
    var humanContribution by rememberSaveable(projectId) { mutableStateOf("") }

    LaunchedEffect(bundle?.project?.id) {
        if (bundle != null && !initialized) {
            category = bundle.project.latestPublication?.conferenceCategory.orEmpty()
            workDescription = bundle.sealCheck?.workDescription.orEmpty()
            learningReflection = bundle.sealCheck?.learningReflection.orEmpty()
            nextImprovement = bundle.sealCheck?.nextImprovement.orEmpty()
            humanContribution = bundle.provenance?.humanContributionSummary.orEmpty()
            initialized = true
        }
    }

    val aiAssisted = bundle?.provenance?.aiAssistanceUsed == true ||
        currentVersion?.layers?.any { it.aigc || it.kind == "AI_GENERATED" } == true
    val aiSourceReady = !aiAssisted ||
        bundle?.provenance?.items?.any { it.itemType == "AI_CONTRIBUTION" } == true ||
        bundle?.imageGenerations?.items?.any { it.outputVersionId == currentVersion?.id } == true
    val rightsReady = bundle?.provenance?.unresolvedRights != true
    val formReady = category.isNotBlank() &&
        workDescription.trim().length >= 2 &&
        learningReflection.trim().length >= 2 &&
        nextImprovement.trim().length >= 2 &&
        humanContribution.trim().length >= 2 &&
        rightsReady && aiSourceReady
    val missingRequirements = buildList {
        if (currentVersion == null) add("先保存一个完整的作品版本")
        if (bundle?.project?.currentStage != null && bundle.project.currentStage != "SEAL") {
            add("等待作品完成安全与完整性检查")
        }
        if (category.isBlank()) add("选择一个大会分类")
        if (workDescription.trim().length < 2) add("作品介绍至少填写 2 个字")
        if (learningReflection.trim().length < 2) add("学习收获至少填写 2 个字")
        if (nextImprovement.trim().length < 2) add("下次改进至少填写 2 个字")
        if (humanContribution.trim().length < 2) add("本人贡献至少填写 2 个字")
        if (!rightsReady) add("先解决素材授权问题")
        if (!aiSourceReady) add("等待 AI 来源记录准备完成")
    }
    val canSubmit = bundle != null && currentVersion != null &&
        bundle.project.currentStage == "SEAL" && currentPublication == null && formReady &&
        !publishBusy

    LuggagePageScaffold(title = "发布到大会", onBack = onBack) {
        when {
            state.loading && bundle == null -> PublishLoading("正在准备投稿信息…")
            bundle == null -> PublishNotice(
                state.message ?: "作品档案暂时无法载入",
                isError = true,
            )
            currentPublication != null -> PublicationStatusCard(currentPublication.status)
            else -> {
                Text(
                    text = bundle.project.title,
                    color = PublishInk,
                    fontFamily = YaHei,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "投稿后先进入内容审核，审核通过才会出现在大会作品浏览中。",
                    color = PublishMuted,
                    fontFamily = YaHei,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
                if (bundle.project.currentStage != "SEAL") {
                    PublishNotice("作品仍在完成安全与完整性检查，请稍后刷新再投稿。", true)
                }
                if (!rightsReady) {
                    PublishNotice("作品仍有未解决的素材授权问题，请先继续修订来源记录。", true)
                }

                PublishSection("选择大会分类") {
                    Text(
                        text = "必选，请选择 1 项。",
                        color = if (category.isBlank()) PublishError else PublishMuted,
                        fontFamily = YaHei,
                        fontSize = 12.sp,
                    )
                    ConferenceCategories.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { option ->
                                FilterChip(
                                    selected = category == option.value,
                                    onClick = { category = option.value },
                                    enabled = !publishBusy,
                                    label = { Text(option.label, fontFamily = YaHei) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                )
                            }
                        }
                    }
                }

                PublishSection("投稿说明") {
                    Text(
                        text = "以下 4 项均为必填，每项至少填写 2 个字。",
                        color = PublishMuted,
                        fontFamily = YaHei,
                        fontSize = 12.sp,
                    )
                    PublishTextField(
                        value = workDescription,
                        onValueChange = { workDescription = it.take(800) },
                        label = "这件作品讲了什么",
                    )
                    PublishTextField(
                        value = learningReflection,
                        onValueChange = { learningReflection = it.take(800) },
                        label = "这次创作学到了什么",
                    )
                    PublishTextField(
                        value = nextImprovement,
                        onValueChange = { nextImprovement = it.take(800) },
                        label = "下一次还想改进什么",
                    )
                    PublishTextField(
                        value = humanContribution,
                        onValueChange = { humanContribution = it.take(1000) },
                        label = "哪些构思、选择或修改是我完成的",
                    )
                }

                (publishMessage ?: state.message)?.takeIf { it.isNotBlank() }?.let {
                    PublishNotice(it, isError = publishFailed || state.retryable)
                }
                PublishNotice(
                    text = if (missingRequirements.isEmpty()) {
                        "信息已完整，可以确认投稿。"
                    } else {
                        "还需完成：${missingRequirements.joinToString("；")}"
                    },
                    isError = missingRequirements.isNotEmpty(),
                )
                Button(
                    onClick = {
                        onSubmit(
                            bundle,
                            ConferencePublishDraft(
                                category = category,
                                workDescription = workDescription,
                                learningReflection = learningReflection,
                                nextImprovement = nextImprovement,
                                humanContributionSummary = humanContribution,
                            ),
                        )
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PublishGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFAABCA3),
                        disabledContentColor = Color.White.copy(alpha = .84f),
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = "确认投稿到大会" },
                ) {
                    if (publishBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("正在提交…", fontFamily = YaHei)
                    } else {
                        Text("确认投稿", fontFamily = YaHei, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PublishSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PublishPaper, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = PublishInk, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = PublishMuted.copy(alpha = .22f))
        content()
    }
}

@Composable
private fun PublishTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    val length = value.trim().length
    val hint = when {
        length == 0 -> "必填，至少 2 个字"
        length < 2 -> "还需 ${2 - length} 个字"
        else -> "已填写 $length 个字"
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = YaHei) },
        supportingText = {
            Text(
                text = hint,
                color = if (length < 2) PublishError else PublishMuted,
                fontFamily = YaHei,
            )
        },
        isError = length < 2,
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PublishLoading(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PublishGreen, strokeWidth = 2.dp)
        Text(text, color = PublishMuted, fontFamily = YaHei, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun PublishNotice(text: String, isError: Boolean) {
    Text(
        text = text,
        color = if (isError) PublishError else PublishGreen,
        fontFamily = YaHei,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(PublishPaper, RoundedCornerShape(14.dp))
            .padding(12.dp),
    )
}

@Composable
private fun PublicationStatusCard(status: String) {
    val (title, description) = when (status) {
        "PENDING_CHECK" -> "已经提交审核" to "审核通过后，作品会自动出现在大会作品浏览中，无需重复投稿。"
        "PUBLISHED" -> "已经发布到大会" to "作品目前已在大会作品页展示。"
        "RETURNED" -> "这版作品已被退回" to "请返回继续修订并保存新版本，然后再次投稿。"
        "WITHDRAWN" -> "这版作品已撤回" to "如需重新投稿，请先继续创作并保存一个新版本。"
        else -> "这版作品已经提交" to "请在作品档案中查看最新审核状态。"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PublishPaper, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = PublishInk, fontFamily = YaHei, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(description, color = PublishMuted, fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp)
    }
}
