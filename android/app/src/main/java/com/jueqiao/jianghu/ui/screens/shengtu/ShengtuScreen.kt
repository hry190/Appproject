package com.jueqiao.jianghu.ui.screens.shengtu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.CreationTestIssueDto
import com.jueqiao.jianghu.luggage.CreationTestRecordDto
import com.jueqiao.jianghu.luggage.CreationToolCallDto
import com.jueqiao.jianghu.luggage.CreationSealCheckDto
import com.jueqiao.jianghu.luggage.ClassroomDto
import com.jueqiao.jianghu.luggage.ImageGenerationJobDto
import com.jueqiao.jianghu.luggage.ImageGenerationJobListDto
import com.jueqiao.jianghu.luggage.LearningCardDto
import com.jueqiao.jianghu.luggage.ProvenanceManifestDto
import com.jueqiao.jianghu.ui.components.CreationWorkspaceTopBar
import com.jueqiao.jianghu.ui.screens.gongfang.CreationManualOption
import com.jueqiao.jianghu.ui.screens.settings.CreationDraftStore
import com.jueqiao.jianghu.ui.theme.YaHei
import coil.compose.AsyncImage
import kotlin.math.roundToInt

/**
 * 作品制作与封卷流程页，沿用工坊的返回、顶部标签和创作档案入口。
 * 背景图使用 img_shengtu_bg.png，并承载草稿、生成、测试和提交状态。
 */
data class LearningCardForm(
    val manualPageIds: List<String>,
    val methodSummary: String,
    val unresolvedQuestions: String,
    val questionsConfirmed: Boolean,
)

data class ProvenanceForm(
    val humanSummary: String,
    val aiUsed: Boolean,
    val aiSummary: String,
    val aiProvider: String,
    val aiModel: String,
    val aiAction: String,
    val promptSummary: String,
    val aiResultModified: Boolean,
    val aigcLabelDeclared: Boolean,
    val externalSourceUrl: String,
    val externalSourceAuthor: String,
    val externalLicense: String,
    val unresolvedRights: Boolean,
)

data class SealCheckForm(
    val workDescription: String,
    val learningReflection: String,
    val nextImprovement: String,
    val identityPrivacyConfirmed: Boolean,
    val contactPrivacyConfirmed: Boolean,
    val portraitRightsConfirmed: Boolean,
)

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
)

private fun coachReplyText(call: CreationToolCallDto): String? {
    val output = call.outputSnapshot ?: return null
    val summary = output.get("summary")?.asString?.trim().orEmpty()
    val suggestedPrompt = output.get("suggested_prompt")?.asString?.trim().orEmpty()
    val checklist = output.get("checklist")?.asJsonArray
        ?.mapNotNull { it.asString?.trim()?.takeIf(String::isNotBlank) }
        .orEmpty()
    return buildString {
        if (summary.isNotBlank()) append(summary)
        if (suggestedPrompt.isNotBlank()) {
            if (isNotEmpty()) append("\n")
            append("可参考：").append(suggestedPrompt)
        }
        if (checklist.isNotEmpty()) {
            if (isNotEmpty()) append("\n")
            append(checklist.joinToString(prefix = "• ", separator = "\n• "))
        }
    }.trim().takeIf(String::isNotBlank)
}

@Composable
fun ShengtuScreen(
    onBack: () -> Unit = {},
    projectId: String? = null,
    projectTitle: String? = null,
    currentVersionNumber: Int? = null,
    currentVersionId: String? = null,
    currentStage: String? = null,
    toolCalls: List<CreationToolCallDto> = emptyList(),
    testRecords: List<CreationTestRecordDto> = emptyList(),
    manualSources: List<CreationManualOption> = emptyList(),
    methodManualPageIds: List<String> = emptyList(),
    initialMethodSummary: String? = null,
    learningCard: LearningCardDto? = null,
    provenance: ProvenanceManifestDto? = null,
    sealCheck: CreationSealCheckDto? = null,
    imageGenerations: ImageGenerationJobListDto? = null,
    activeGeneration: ImageGenerationJobDto? = null,
    publicationStatus: String? = null,
    publicationId: String? = null,
    publicationVisibility: String? = null,
    classrooms: List<ClassroomDto> = emptyList(),
    initialPrompt: String? = null,
    savingDraft: Boolean = false,
    draftSaveMessage: String? = null,
    workflowBusy: Boolean = false,
    workflowMessage: String? = null,
    generationBusy: Boolean = false,
    generationMessage: String? = null,
    onSaveDraft: (String) -> Unit = {},
    onRequestCoach: (String) -> Unit = {},
    onDecideCoach: (CreationToolCallDto, Boolean) -> Unit = { _, _ -> },
    onRequestImageGeneration: (String, String, String) -> Unit = { _, _, _ -> },
    onRetryImageGeneration: (ImageGenerationJobDto) -> Unit = {},
    onEnterTest: () -> Unit = {},
    onRecordTest: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onResolveIssue: (CreationTestIssueDto, String) -> Unit = { _, _ -> },
    onEnterSeal: () -> Unit = {},
    onSaveLearningCard: (LearningCardForm, () -> Unit) -> Unit = { _, _ -> },
    onSubmitMigrationEvidence: (List<String>, String, () -> Unit) -> Unit = { _, _, _ -> },
    onSaveProvenance: (ProvenanceForm, () -> Unit) -> Unit = { _, _ -> },
    onSaveSealCheck: (SealCheckForm, () -> Unit) -> Unit = { _, _ -> },
    onSaveReflectionPackage: (SealCheckForm, LearningCardForm, () -> Unit) -> Unit = { _, _, _ -> },
    onSubmit: (String, String?) -> Unit = { _, _ -> },
    onOpenPublishedWork: (String) -> Unit = {},
    onCreateWork: () -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
) {
    val screenFocusManager = LocalFocusManager.current
    val screenKeyboardController = LocalSoftwareKeyboardController.current
    val screenImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = true) {
        if (screenImeVisible) {
            screenFocusManager.clearFocus()
            screenKeyboardController?.hide()
        } else {
            onBack()
        }
    }

    // 输入框焦点管理
    val rect227FocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val draftStore = remember(context) { CreationDraftStore(context) }
    val coachAvailable = currentStage == "PRODUCTION" && publicationStatus == null
    val draftKey = remember(projectId, currentStage) {
        val base = if (coachAvailable) {
            CreationDraftStore.Keys.CoachPrompt
        } else {
            CreationDraftStore.Keys.CreationTextDraft
        }
        if (projectId == null) base else "$base:$projectId"
    }
    var rect227Text by rememberSaveable(projectId, currentStage) {
        mutableStateOf(
            draftStore.read(draftKey).ifBlank {
                initialPrompt.takeIf { currentStage == "DRAFT" || currentStage == "IDEATION" }.orEmpty()
            }
        )
    }
    var draftNotice by rememberSaveable(projectId) { mutableStateOf<String?>(null) }
    var showWorkflowDialog by rememberSaveable(projectId) { mutableStateOf(false) }
    var chatMessages by remember(projectId) { mutableStateOf(emptyList<ChatMessage>()) }
    var chatSending by rememberSaveable(projectId) { mutableStateOf(false) }
    val pendingCoachCall = toolCalls.firstOrNull {
        it.creationVersionId == currentVersionId && it.status == "PROPOSED"
    }
    val latestCompletedCoachCall = toolCalls.firstOrNull {
        it.creationVersionId == currentVersionId && it.status == "COMPLETED"
    }
    val latestGeneration = activeGeneration ?: imageGenerations?.items?.firstOrNull()
    val generationActive = latestGeneration?.status in setOf(
        "QUEUED", "RUNNING", "SAFETY_CHECK", "VERSIONING"
    )
    val agentProcessing = chatSending || workflowBusy || generationBusy || generationActive
    val sealPackageComplete = sealCheck?.status == "COMPLETE" &&
        learningCard?.status == "COMPLETE" && provenance?.status == "COMPLETE"
    val mainActionText = when {
        projectId == null -> "开始创作"
        publicationStatus != null -> "查看提交状态"
        currentStage == null -> "正在载入…"
        currentStage == "DRAFT" || currentStage == "IDEATION" -> "保存并继续"
        currentStage == "SEAL" && sealPackageComplete -> "提交作品"
        else -> "下一步"
    }

    LaunchedEffect(workflowBusy, workflowMessage, pendingCoachCall, latestCompletedCoachCall) {
        if (!workflowBusy && (workflowMessage != null || pendingCoachCall != null || latestCompletedCoachCall != null)) {
            chatSending = false
        }
    }
    val sendMessage = {
        val message = rect227Text.trim()
        if (coachAvailable && projectId != null && message.length >= 2 && !workflowBusy && !generationBusy) {
            chatMessages = chatMessages + ChatMessage(text = message, fromUser = true)
            rect227Text = ""
            draftStore.clear(draftKey)
            chatSending = true
            onRequestCoach(message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景(创作.png)
        Image(
            painter = painterResource(R.drawable.img_shengtu_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            CreationWorkspaceTopBar(
                onBack = onBack,
                onOpenArchive = onOpenChuangzuodangan,
            )

            // Group 212.png(W=372, H=679)— 主内容区,水平居中 + 垂直上移 25(原 Center 上移)
            Image(
                painter = painterResource(R.drawable.img_shengtu_group212),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-2).dp)
                    .size(width = 372.dp, height = 679.dp),
                contentScale = ContentScale.Fit,
            )

            // 对话工作区：只有用户发出消息后，Agent 处理中的进度卡才会出现。
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 150.dp)
                    .width(300.dp)
                    .height(500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (chatMessages.isEmpty() && !agentProcessing && pendingCoachCall == null && workflowMessage.isNullOrBlank()) {
                    ChatBubble(
                        text = "可以告诉我想怎么改，我会先理解你的想法，再给出修改方向。",
                        fromUser = false,
                    )
                }
                chatMessages.forEach { message ->
                    ChatBubble(text = message.text, fromUser = message.fromUser)
                }
                if (agentProcessing) {
                    AgentThinkingPanel(
                        workflowBusy = workflowBusy || chatSending,
                        generationBusy = generationBusy || generationActive,
                        generationProgress = latestGeneration?.progressPercent,
                        currentStage = currentStage,
                        statusMessage = if (generationBusy || generationActive) {
                            generationMessage
                        } else {
                            workflowMessage
                        },
                    )
                }
                pendingCoachCall?.let { call ->
                    CoachProposalCard(
                        effectSummary = call.effectSummary,
                        onAccept = { onDecideCoach(call, true) },
                        onReject = { onDecideCoach(call, false) },
                        enabled = !workflowBusy,
                    )
                }
                if (!workflowBusy) {
                    workflowMessage?.takeIf { it.isNotBlank() }?.let {
                        ChatBubble(text = it, fromUser = false)
                    }
                    latestCompletedCoachCall?.let { call ->
                        (coachReplyText(call) ?: call.effectSummary)
                            .takeIf { it.isNotBlank() }
                            ?.let {
                                ChatBubble(text = it, fromUser = false)
                            }
                    }
                }
            }

            // 未标题-1 41.png(X=310, Y=605, W=92, H=143)— @2x,放 drawable-xxhdpi/(实际 184×286)
            Image(
                painter = painterResource(R.drawable.img_shengtu_untitled41),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 300.dp, y = 530.dp)
                    .size(width = 92.dp, height = 143.dp),
                contentScale = ContentScale.Fit,
            )

            // 别名,FocusRequester 在外层声明了
            val focusRequester = rect227FocusRequester

            // 输入框固定在卷轴内容区内，和底部保存操作保持安全间距。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 684.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.width(294.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .background(
                                color = Color(0xFFE4EFD3),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .border(1.dp, Color(0xFF82A575).copy(alpha = .46f), RoundedCornerShape(18.dp)),
                    ) {
                        BasicTextField(
                            value = rect227Text,
                            onValueChange = {
                                rect227Text = it
                                draftStore.saveIfEnabled(draftKey, it)
                            },
                            singleLine = true,
                            cursorBrush = SolidColor(Color.Black),
                            textStyle = TextStyle(
                                fontFamily = YaHei,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF3D4F3F),
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .semantics { contentDescription = "创作修改要求" }
                                .padding(horizontal = 12.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (rect227Text.isEmpty()) {
                                        Text(
                                            text = when {
                                                coachAvailable -> "告诉创作教练你想怎么修改……"
                                                currentStage == "DRAFT" || currentStage == "IDEATION" -> "写下这一版的草图或脚本……"
                                                else -> "当前步骤请使用下方“下一步”继续"
                                            },
                                            color = Color(0xFF94A48F),
                                            fontFamily = YaHei,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "发送给创作教练"
                                role = Role.Button
                            }
                            .clickable(
                                enabled = coachAvailable && projectId != null && rect227Text.trim().length >= 2 &&
                                    !workflowBusy && !generationBusy,
                                onClick = sendMessage,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF83AA7D), RoundedCornerShape(17.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "↑",
                                color = Color.White,
                                fontFamily = YaHei,
                                fontSize = 19.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
            }
            // 底部只保留一个随阶段变化的主动作，避免学生在按钮之间猜测。
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 393.dp)
                    .width(372.dp)
                    .height(55.dp),
            ) {
                ScrollActionButton(
                    text = if (savingDraft) "保存中…" else mainActionText,
                    modifier = Modifier.fillMaxSize(),
                    enabled = !savingDraft && !workflowBusy && (
                        (projectId != null && currentStage != null) ||
                            (projectId == null && (rect227Text.isNotBlank() || chatMessages.any { it.fromUser }))
                    ),
                    onClick = {
                        val promptToSave = rect227Text.ifBlank {
                            chatMessages.lastOrNull { it.fromUser }?.text.orEmpty()
                        }
                        when {
                            projectId == null -> {
                                draftStore.clear(draftKey)
                                onCreateWork()
                            }
                            currentStage == "DRAFT" || currentStage == "IDEATION" -> {
                                draftStore.save(draftKey, promptToSave)
                                onSaveDraft(promptToSave)
                            }
                            else -> showWorkflowDialog = true
                        }
                    },
                )
            }

            (draftSaveMessage ?: draftNotice)?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFF8C4D3D),
                    style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 350.dp),
                )
            }
        }
    }

    if (showWorkflowDialog && projectId != null && currentStage != null) {
        CreationWorkflowDialog(
            projectId = projectId,
            currentStage = currentStage,
            currentVersionId = currentVersionId,
            currentPrompt = rect227Text.ifBlank {
                chatMessages.lastOrNull { it.fromUser }?.text.orEmpty()
            },
            toolCalls = toolCalls,
            testRecords = testRecords,
            manualSources = manualSources,
            methodManualPageIds = methodManualPageIds,
            initialMethodSummary = initialMethodSummary,
            learningCard = learningCard,
            provenance = provenance,
            sealCheck = sealCheck,
            imageGenerations = imageGenerations,
            activeGeneration = activeGeneration,
            publicationStatus = publicationStatus,
            publicationId = publicationId,
            publicationVisibility = publicationVisibility,
            classrooms = classrooms,
            busy = workflowBusy,
            message = workflowMessage,
            generationBusy = generationBusy,
            generationMessage = generationMessage,
            onDismiss = { if (!workflowBusy) showWorkflowDialog = false },
            onRequestCoach = onRequestCoach,
            onDecideCoach = onDecideCoach,
            onRequestImageGeneration = onRequestImageGeneration,
            onRetryImageGeneration = onRetryImageGeneration,
            onEnterTest = onEnterTest,
            onRecordTest = onRecordTest,
            onResolveIssue = onResolveIssue,
            onEnterSeal = onEnterSeal,
            onSaveLearningCard = onSaveLearningCard,
            onSubmitMigrationEvidence = onSubmitMigrationEvidence,
            onSaveProvenance = onSaveProvenance,
            onSaveSealCheck = onSaveSealCheck,
            onSaveReflectionPackage = onSaveReflectionPackage,
            onSubmit = onSubmit,
            onOpenPublishedWork = onOpenPublishedWork,
            onOpenEditor = onCreateWork,
        )
    }
}

@Composable
private fun ScrollActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val scrollAsset = remember(resources) {
        ImageBitmap.imageResource(resources, R.drawable.img_shengtu_group196)
    }
    Box(
        modifier = modifier
            .semantics {
                contentDescription = text
                role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 372×55 卷轴图按左右 30、上下 12 的九宫格切片绘制，
            // 这样发布按钮缩窄后仍保留和保存按钮相同的卷轴端与双层边线。
            val sourceCap = (scrollAsset.width * 30f / 372f).roundToInt()
            val destinationCap = (size.height * 30f / 55f)
                .roundToInt()
                .coerceAtMost((size.width / 2f).roundToInt())
            val canvasWidth = size.width.roundToInt()
            val canvasHeight = size.height.roundToInt()
            val sourceMiddle = (scrollAsset.width - sourceCap * 2).coerceAtLeast(1)
            val destinationMiddle = (canvasWidth - destinationCap * 2).coerceAtLeast(1)
            drawImage(
                image = scrollAsset,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(sourceCap, scrollAsset.height),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(destinationCap, canvasHeight),
                filterQuality = FilterQuality.Medium,
            )
            drawImage(
                image = scrollAsset,
                srcOffset = IntOffset(sourceCap, 0),
                srcSize = IntSize(sourceMiddle, scrollAsset.height),
                dstOffset = IntOffset(destinationCap, 0),
                dstSize = IntSize(destinationMiddle, canvasHeight),
                filterQuality = FilterQuality.Medium,
            )
            drawImage(
                image = scrollAsset,
                srcOffset = IntOffset(scrollAsset.width - sourceCap, 0),
                srcSize = IntSize(sourceCap, scrollAsset.height),
                dstOffset = IntOffset(destinationCap + destinationMiddle, 0),
                dstSize = IntSize(destinationCap, canvasHeight),
                filterQuality = FilterQuality.Medium,
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontFamily = YaHei,
            fontSize = if (text.startsWith("发布")) 18.sp else 16.sp,
            fontWeight = if (text.startsWith("发布")) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChatBubble(
    text: String,
    fromUser: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (fromUser) Color(0xFFE4EFD3) else Color(0xFFF9F4E6).copy(alpha = .96f),
            shape = if (fromUser) {
                RoundedCornerShape(16.dp, 6.dp, 16.dp, 16.dp)
            } else {
                RoundedCornerShape(6.dp, 16.dp, 16.dp, 16.dp)
            },
            modifier = Modifier
                .widthIn(max = 276.dp)
                .border(
                    width = 1.dp,
                    color = if (fromUser) Color(0xFFB7CAA8) else Color(0xFFD9D4BE),
                    shape = if (fromUser) {
                        RoundedCornerShape(16.dp, 6.dp, 16.dp, 16.dp)
                    } else {
                        RoundedCornerShape(6.dp, 16.dp, 16.dp, 16.dp)
                    },
                ),
        ) {
            Text(
                text = text,
                color = if (fromUser) Color(0xFF4F644E) else Color(0xFF58685A),
                fontFamily = YaHei,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun AgentThinkingPanel(
    workflowBusy: Boolean,
    generationBusy: Boolean,
    generationProgress: Int?,
    currentStage: String?,
    statusMessage: String?,
) {
    val actualProgress = generationProgress?.coerceIn(0, 100)
    val isGeneration = generationBusy && actualProgress != null
    val progressStep = when {
        !isGeneration -> 0
        actualProgress >= 80 -> 2
        actualProgress >= 40 -> 1
        else -> 0
    }
    val detail = statusMessage?.trim()?.takeIf { it.isNotBlank() } ?: when {
        isGeneration -> "正在生成并检查画面"
        currentStage == "TEST" -> "正在读取测试反馈"
        currentStage == "SEAL" -> "正在整理发布材料"
        else -> "正在理解你的修改意见"
    }
    Surface(
        color = Color(0xFFF9F4E6).copy(alpha = .96f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFB9B993).copy(alpha = .72f),
                shape = RoundedCornerShape(20.dp),
            ),
        shadowElevation = 5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF789D79), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "机巧",
                        color = Color.White,
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
                Text(
                    text = "正在把灵感整理成草稿",
                    color = Color(0xFF3E5B43),
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Text(
                text = if (isGeneration) "正在根据你的意见生成并校验新版本。" else "我会先理解你的想法，再给出可继续编辑的创作方向。",
                color = Color(0xFF76816F),
                fontFamily = YaHei,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
            Surface(
                color = Color(0xFFEEF3E3),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("处理中", color = Color(0xFF5B765F), fontFamily = YaHei, fontSize = 13.sp)
                        actualProgress?.let {
                            Text("$it%", color = Color(0xFF8DA28D), fontFamily = YaHei, fontSize = 13.sp)
                        }
                    }
                    if (actualProgress != null) {
                        LinearProgressIndicator(
                            progress = { actualProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp)
                                .height(6.dp),
                            color = Color(0xFF7EA67D),
                            trackColor = Color(0xFFDCE7D1),
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp)
                                .height(6.dp),
                            color = Color(0xFF7EA67D),
                            trackColor = Color(0xFFDCE7D1),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text("•••", color = Color(0xFF6F9874), fontFamily = YaHei, fontSize = 14.sp)
                        Text(
                            text = detail,
                            color = Color(0xFF6F9874),
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 5.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AgentStep("理解创作意图", done = isGeneration || progressStep > 0, current = progressStep == 0)
                AgentStep("规划作品结构", done = isGeneration && progressStep > 1, current = progressStep == 1)
                AgentStep("生成首版草稿", done = isGeneration && actualProgress >= 100, current = progressStep == 2)
            }
        }
    }
}

@Composable
private fun CoachProposalCard(
    effectSummary: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        color = Color(0xFFFFF3D7),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2D2A9), RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = "创作教练建议，请确认",
                color = Color(0xFF765A2C),
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Text(
                text = effectSummary.ifBlank { "我已整理出一份修改建议。" },
                color = Color(0xFF655B49),
                fontFamily = YaHei,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).height(42.dp),
                ) { Text("暂不采用", fontFamily = YaHei, fontSize = 12.sp) }
                Button(
                    onClick = onAccept,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).height(42.dp),
                ) { Text("采用建议", fontFamily = YaHei, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun AgentStep(label: String, done: Boolean = false, current: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    color = if (done) Color(0xFF7EA67D) else Color.Transparent,
                    shape = androidx.compose.foundation.shape.CircleShape,
                )
                .border(
                    width = 1.dp,
                    color = if (done) Color(0xFF7EA67D) else Color(0xFFB9C9AE),
                    shape = androidx.compose.foundation.shape.CircleShape,
                )
                .then(
                    if (current) {
                        Modifier.border(
                            width = 3.dp,
                            color = Color(0x227EA67D),
                            shape = androidx.compose.foundation.shape.CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (done) "✓" else "·",
                color = if (done) Color.White else Color(0xFF7EA67D),
                fontFamily = YaHei,
                fontSize = if (done) 12.sp else 16.sp,
            )
        }
        Text(
            text = label,
            color = if (done) Color(0xFF526B56) else Color(0xFF7D8878),
            fontFamily = YaHei,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

@Composable
private fun CreationWorkflowDialog(
    projectId: String,
    currentStage: String,
    currentVersionId: String?,
    currentPrompt: String,
    toolCalls: List<CreationToolCallDto>,
    testRecords: List<CreationTestRecordDto>,
    manualSources: List<CreationManualOption>,
    methodManualPageIds: List<String>,
    initialMethodSummary: String?,
    learningCard: LearningCardDto?,
    provenance: ProvenanceManifestDto?,
    sealCheck: CreationSealCheckDto?,
    imageGenerations: ImageGenerationJobListDto?,
    activeGeneration: ImageGenerationJobDto?,
    publicationStatus: String?,
    publicationId: String?,
    publicationVisibility: String?,
    classrooms: List<ClassroomDto>,
    busy: Boolean,
    message: String?,
    generationBusy: Boolean,
    generationMessage: String?,
    onDismiss: () -> Unit,
    onRequestCoach: (String) -> Unit,
    onDecideCoach: (CreationToolCallDto, Boolean) -> Unit,
    onRequestImageGeneration: (String, String, String) -> Unit,
    onRetryImageGeneration: (ImageGenerationJobDto) -> Unit,
    onEnterTest: () -> Unit,
    onRecordTest: (String, String, String, String) -> Unit,
    onResolveIssue: (CreationTestIssueDto, String) -> Unit,
    onEnterSeal: () -> Unit,
    onSaveLearningCard: (LearningCardForm, () -> Unit) -> Unit,
    onSubmitMigrationEvidence: (List<String>, String, () -> Unit) -> Unit,
    onSaveProvenance: (ProvenanceForm, () -> Unit) -> Unit,
    onSaveSealCheck: (SealCheckForm, () -> Unit) -> Unit,
    onSaveReflectionPackage: (SealCheckForm, LearningCardForm, () -> Unit) -> Unit,
    onSubmit: (String, String?) -> Unit,
    onOpenPublishedWork: (String) -> Unit,
    onOpenEditor: () -> Unit,
) {
    val workflowContext = LocalContext.current
    val workflowDraftStore = remember(workflowContext) { CreationDraftStore(workflowContext) }
    val generationDraftKey = remember(projectId) {
        "${CreationDraftStore.Keys.GenerationPrompt}:$projectId"
    }
    var coachPrompt by rememberSaveable(projectId) {
        mutableStateOf(currentPrompt.ifBlank { "请检查我的主题是否清楚，并给出文字修改建议" })
    }
    var generationPrompt by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf(workflowDraftStore.read(generationDraftKey))
    }
    var generationSize by rememberSaveable(projectId) { mutableStateOf("SQUARE") }
    var generationQuality by rememberSaveable(projectId) { mutableStateOf("MEDIUM") }
    var reviewingGeneration by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf(false)
    }
    var productionStep by rememberSaveable(projectId, currentVersionId) { mutableStateOf(0) }
    val savedSealStep = when {
        sealCheck?.status == "COMPLETE" && learningCard?.status == "COMPLETE" &&
            provenance?.status == "COMPLETE" -> 3
        provenance?.status == "COMPLETE" -> 3
        learningCard?.status == "COMPLETE" && !sealCheck?.workDescription.isNullOrBlank() -> 2
        !sealCheck?.workDescription.isNullOrBlank() -> 1
        else -> 0
    }
    var sealStep by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf(savedSealStep)
    }
    LaunchedEffect(savedSealStep) {
        if (savedSealStep > sealStep) sealStep = savedSealStep
    }
    var testScenario by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf("请一位同学在不看说明的情况下，说出画面的主体和主题")
    }
    var testResult by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf("NEEDS_REVISION")
    }
    var testNotes by rememberSaveable(projectId, currentVersionId) { mutableStateOf("") }
    var testFinding by rememberSaveable(projectId, currentVersionId) { mutableStateOf("") }
    val recordsForVersion = testRecords.filter { it.creationVersionId == currentVersionId }
    val latestTest = recordsForVersion.firstOrNull()
    val openIssues = recordsForVersion.flatMap { it.issues }.filter { it.status == "OPEN" }
    val pendingCall = toolCalls.firstOrNull {
        it.creationVersionId == currentVersionId && it.status == "PROPOSED"
    }
    val latestCompletedCall = toolCalls.firstOrNull {
        it.creationVersionId == currentVersionId && it.status == "COMPLETED"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF9F5E8),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (publicationStatus == null) {
                        "创作流程 · ${workflowStageLabel(currentStage)}"
                    } else {
                        "作品提交状态"
                    },
                    color = Color(0xFF294E36),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "每一步都由你确认；建议不会自动改动画面。",
                    color = Color(0xFF526354),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = YaHei),
                )
                if (busy) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Text(message ?: "正在处理…", fontFamily = YaHei, fontSize = 13.sp)
                    }
                } else if (!message.isNullOrBlank()) {
                    WorkflowNotice(message)
                }
                HorizontalDivider(color = Color(0xFFD7D4BE))

                if (publicationStatus != null) {
                    SubmissionStatusContent(
                        status = publicationStatus,
                        visibility = publicationVisibility,
                        publicationId = publicationId,
                        busy = busy,
                        onOpenPublishedWork = onOpenPublishedWork,
                    )
                } else when (currentStage) {
                    "PRODUCTION" -> ProductionWorkflowContent(
                        step = productionStep,
                        onStepChange = { productionStep = it.coerceIn(0, 3) },
                        generationPrompt = generationPrompt,
                        onGenerationPromptChange = {
                            generationPrompt = it.take(2000)
                            workflowDraftStore.saveIfEnabled(generationDraftKey, generationPrompt)
                            reviewingGeneration = false
                        },
                        generationSize = generationSize,
                        onGenerationSizeChange = {
                            generationSize = it
                            reviewingGeneration = false
                        },
                        generationQuality = generationQuality,
                        onGenerationQualityChange = {
                            generationQuality = it
                            reviewingGeneration = false
                        },
                        reviewingGeneration = reviewingGeneration,
                        onReviewGeneration = { reviewingGeneration = true },
                        onCancelGenerationReview = { reviewingGeneration = false },
                        generationCapability = imageGenerations,
                        latestGeneration = activeGeneration ?: imageGenerations?.items?.firstOrNull(),
                        generationBusy = generationBusy,
                        generationMessage = generationMessage,
                        onRequestImageGeneration = { prompt, size, quality ->
                            reviewingGeneration = false
                            workflowDraftStore.clear(generationDraftKey)
                            onRequestImageGeneration(prompt, size, quality)
                        },
                        onRetryImageGeneration = onRetryImageGeneration,
                        coachPrompt = coachPrompt,
                        onCoachPromptChange = { coachPrompt = it.take(1000) },
                        pendingCall = pendingCall,
                        latestCompletedCall = latestCompletedCall,
                        currentVersionId = currentVersionId,
                        busy = busy,
                        onRequestCoach = onRequestCoach,
                        onDecideCoach = onDecideCoach,
                        onOpenEditor = onOpenEditor,
                        onEnterTest = onEnterTest,
                    )
                    "TEST" -> TestWorkflowContent(
                        currentVersionId = currentVersionId,
                        latestTest = latestTest,
                        openIssues = openIssues,
                        scenario = testScenario,
                        onScenarioChange = { testScenario = it.take(500) },
                        result = testResult,
                        onResultChange = {
                            testResult = it
                            if (it == "PASSED") testFinding = ""
                        },
                        notes = testNotes,
                        onNotesChange = { testNotes = it.take(2000) },
                        finding = testFinding,
                        onFindingChange = { testFinding = it.take(500) },
                        busy = busy,
                        onRecordTest = onRecordTest,
                        onResolveIssue = onResolveIssue,
                        onEnterSeal = onEnterSeal,
                    )
                    "SEAL" -> SealWorkflowContent(
                        step = sealStep,
                        onStepChange = { sealStep = it.coerceIn(0, 3) },
                        projectId = projectId,
                        currentVersionId = currentVersionId,
                        manualSources = manualSources,
                        methodManualPageIds = methodManualPageIds,
                        initialMethodSummary = initialMethodSummary,
                        learningCard = learningCard,
                        provenance = provenance,
                        sealCheck = sealCheck,
                        publicationStatus = publicationStatus,
                        publicationId = publicationId,
                        publicationVisibility = publicationVisibility,
                        classrooms = classrooms,
                        busy = busy,
                        onSaveLearningCard = onSaveLearningCard,
                        onSubmitMigrationEvidence = onSubmitMigrationEvidence,
                        onSaveProvenance = onSaveProvenance,
                        onSaveSealCheck = onSaveSealCheck,
                        onSaveReflectionPackage = onSaveReflectionPackage,
                        onSubmit = onSubmit,
                        onOpenPublishedWork = onOpenPublishedWork,
                    )
                    else -> {
                        WorkflowNotice("先在下方保存一版草图或脚本；保存成功后会自动进入制作阶段。")
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("关闭", fontFamily = YaHei)
                }
            }
        }
    }
}

@Composable
private fun ProductionWorkflowContent(
    step: Int,
    onStepChange: (Int) -> Unit,
    generationPrompt: String,
    onGenerationPromptChange: (String) -> Unit,
    generationSize: String,
    onGenerationSizeChange: (String) -> Unit,
    generationQuality: String,
    onGenerationQualityChange: (String) -> Unit,
    reviewingGeneration: Boolean,
    onReviewGeneration: () -> Unit,
    onCancelGenerationReview: () -> Unit,
    generationCapability: ImageGenerationJobListDto?,
    latestGeneration: ImageGenerationJobDto?,
    generationBusy: Boolean,
    generationMessage: String?,
    onRequestImageGeneration: (String, String, String) -> Unit,
    onRetryImageGeneration: (ImageGenerationJobDto) -> Unit,
    coachPrompt: String,
    onCoachPromptChange: (String) -> Unit,
    pendingCall: CreationToolCallDto?,
    latestCompletedCall: CreationToolCallDto?,
    currentVersionId: String?,
    busy: Boolean,
    onRequestCoach: (String) -> Unit,
    onDecideCoach: (CreationToolCallDto, Boolean) -> Unit,
    onOpenEditor: () -> Unit,
    onEnterTest: () -> Unit,
) {
    val generationActive = latestGeneration?.status in setOf(
        "QUEUED", "RUNNING", "SAFETY_CHECK", "VERSIONING"
    )
    WorkflowStepHeader(
        title = "制作阶段",
        step = step,
        total = 4,
        task = listOf("生成画面", "教练确认", "画布排版", "进入测试")[step.coerceIn(0, 3)],
    )
    when (step.coerceIn(0, 3)) {
    0 -> {
    Text("1. 可选：生成一层画面", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Text(
        "只有你核对内容并确认后，系统才会开始生成画面。",
        fontFamily = YaHei,
        fontSize = 12.sp,
        color = Color(0xFF526354),
    )
    generationCapability?.let { capability ->
        WorkflowFact("今日额度", "已用 ${capability.dailyUsed}/${capability.dailyLimit}，剩余 ${capability.dailyRemaining}")
        WorkflowFact("生成服务", if (capability.enabled) "可以使用" else "暂不可用")
    }
    latestGeneration?.let { job ->
        Surface(
            color = Color(0xFFEAF2DE),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    generationStatusLabel(job.status),
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31543B),
                )
                LinearProgressIndicator(
                    progress = { job.progressPercent.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    generationMessage ?: job.errorSummary ?: "进度 ${job.progressPercent}%",
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                )
                job.outputAsset?.originalUrl?.let { url ->
                    var previewFailed by remember(url) { mutableStateOf(false) }
                    var previewAttempt by remember(url) { mutableStateOf(0) }
                    AsyncImage(
                        // A URL fragment changes Coil's request key without changing the
                        // signed HTTP request that reaches the contest backend.
                        model = "$url#preview-attempt=$previewAttempt",
                        contentDescription = "生成结果预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop,
                        onSuccess = { previewFailed = false },
                        onError = { previewFailed = true },
                    )
                    if (previewFailed) {
                        Text(
                            "图片暂时没有加载出来，作品版本已经安全保存。",
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                            color = Color(0xFF8C4D3D),
                        )
                        OutlinedButton(
                            onClick = {
                                previewFailed = false
                                previewAttempt += 1
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) { Text("重新加载", fontFamily = YaHei) }
                    }
                }
                if (job.status == "COMPLETED") {
                    Text(
                        "已保存为新版本；作品卡会注明智能工具参与，提交前还需补充本人修改说明。",
                        fontFamily = YaHei,
                        fontSize = 11.sp,
                        color = Color(0xFF526354),
                    )
                }
                if (job.status == "FAILED" && job.retryable) {
                    OutlinedButton(
                        onClick = { onRetryImageGeneration(job) },
                        enabled = !generationBusy,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) { Text("重试本次生成", fontFamily = YaHei) }
                }
            }
        }
    }
    if (!generationActive && latestGeneration?.status != "COMPLETED") {
        TextField(
            value = generationPrompt,
            onValueChange = onGenerationPromptChange,
            label = { Text("描述希望生成的画面", fontFamily = YaHei) },
            supportingText = { Text("不要填写姓名、电话、邮箱、网址或证件号", fontFamily = YaHei) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("画幅", fontFamily = YaHei, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("SQUARE" to "方形", "PORTRAIT" to "竖版", "LANDSCAPE" to "横版").forEach { (value, label) ->
                FilterChip(
                    selected = generationSize == value,
                    onClick = { onGenerationSizeChange(value) },
                    label = { Text(label, fontFamily = YaHei, fontSize = 11.sp) },
                )
            }
        }
        Text("质量", fontFamily = YaHei, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("LOW" to "快速", "MEDIUM" to "标准", "HIGH" to "精细").forEach { (value, label) ->
                FilterChip(
                    selected = generationQuality == value,
                    onClick = { onGenerationQualityChange(value) },
                    label = { Text(label, fontFamily = YaHei, fontSize = 11.sp) },
                )
            }
        }
        if (!reviewingGeneration) {
            Button(
                onClick = onReviewGeneration,
                enabled = !busy && !generationBusy && currentVersionId != null &&
                    generationPrompt.trim().length >= 2 &&
                    generationCapability?.enabled == true &&
                    generationCapability.dailyRemaining > 0,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("核对生成内容", fontFamily = YaHei) }
        } else {
            Surface(
                color = Color(0xFFFFF3D7),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("生成确认", fontFamily = YaHei, fontWeight = FontWeight.Bold)
                    WorkflowFact("将读取", generationPrompt.trim())
                    WorkflowFact(
                        "将产生",
                        "一张${generationSizeLabel(generationSize)}、${generationQualityLabel(generationQuality)}的图片，并保存到新版本",
                    )
                    WorkflowFact(
                        "数据去向",
                        if (generationCapability?.externalDataShared == true) {
                            "只发送这段画面描述，不会发送作品原图"
                        } else {
                            "提示词不离开本服务"
                        },
                    )
                    WorkflowFact(
                        "后续检查",
                        "系统会检查图片格式和内容安全；未通过时不会写入作品",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onCancelGenerationReview,
                            enabled = !generationBusy,
                            modifier = Modifier.height(48.dp),
                        ) { Text("返回修改", fontFamily = YaHei) }
                        Button(
                            onClick = {
                                onRequestImageGeneration(
                                    generationPrompt.trim(), generationSize, generationQuality
                                )
                            },
                            enabled = !generationBusy,
                            modifier = Modifier.height(48.dp),
                        ) { Text("确认生成", fontFamily = YaHei) }
                    }
                }
            }
        }
    }
    if (latestGeneration?.status == "COMPLETED") {
        Button(
            onClick = { onStepChange(1) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("下一项", fontFamily = YaHei) }
    } else if (!generationActive && !reviewingGeneration) {
        OutlinedButton(
            onClick = { onStepChange(1) },
            enabled = !busy && !generationBusy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("暂不生成", fontFamily = YaHei) }
    }
    }

    1 -> {
    Text("2. 可选：获取创作教练建议", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    if (pendingCall == null && latestCompletedCall == null) {
        TextField(
            value = coachPrompt,
            onValueChange = onCoachPromptChange,
            label = { Text("希望教练检查什么", fontFamily = YaHei) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onRequestCoach(coachPrompt) },
            enabled = !busy && currentVersionId != null && coachPrompt.trim().length >= 2,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("准备建议并查看确认页", fontFamily = YaHei)
        }
    } else if (pendingCall != null) {
        Text(
            text = "调用确认",
            fontFamily = YaHei,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A5A22),
        )
        WorkflowFact("将读取", pendingCall.inputSnapshot.get("prompt")?.asString.orEmpty())
        WorkflowFact("将产生", pendingCall.effectSummary)
        WorkflowFact(
            "数据去向",
            if (pendingCall.externalDataShared) "会发送到外部服务" else "不会向外部服务发送数据",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { onDecideCoach(pendingCall, false) },
                enabled = !busy,
                modifier = Modifier.height(48.dp),
            ) { Text("暂不使用", fontFamily = YaHei) }
            Button(
                onClick = { onDecideCoach(pendingCall, true) },
                enabled = !busy,
                modifier = Modifier.height(48.dp),
            ) { Text("确认并获取", fontFamily = YaHei) }
        }
    }

    latestCompletedCall?.outputSnapshot?.let { output ->
        Text("最近一次建议", fontFamily = YaHei, fontWeight = FontWeight.Bold)
        WorkflowNotice(output.get("summary")?.asString ?: "建议已生成")
        output.get("suggested_prompt")?.asString?.let {
            WorkflowFact("可参考的表达", it)
        }
        output.get("checklist")?.asJsonArray?.forEach { item ->
            Text("• ${item.asString}", fontFamily = YaHei, fontSize = 13.sp)
        }
        Text("建议只供参考，作品不会被自动修改。", fontFamily = YaHei, fontSize = 11.sp, color = Color(0xFF667166))
    }
    if (latestCompletedCall != null) {
        Button(
            onClick = { onStepChange(2) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("下一项", fontFamily = YaHei) }
    } else if (pendingCall == null) {
        OutlinedButton(
            onClick = { onStepChange(2) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("暂不询问", fontFamily = YaHei) }
    }
    }

    2 -> {
    Text("3. 进入画布完成排版", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Text(
        "拖动、缩放、旋转、裁剪与文字调整都会保存为新版本；旧版本不会被覆盖。",
        fontFamily = YaHei,
        fontSize = 12.sp,
        color = Color(0xFF526354),
    )
    Button(
        onClick = onOpenEditor,
        enabled = !busy && !generationBusy && !generationActive && currentVersionId != null && pendingCall == null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("进入画布编辑器", fontFamily = YaHei)
    }
    OutlinedButton(
        onClick = { onStepChange(3) },
        enabled = !busy && currentVersionId != null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("排版已完成", fontFamily = YaHei) }
    }

    else -> {
    Text("4. 完成当前版本后进入测试", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Button(
        onClick = onEnterTest,
        enabled = !busy && !generationBusy && !generationActive && currentVersionId != null && pendingCall == null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("进入测试", fontFamily = YaHei)
    }
    }
    }
}

@Composable
private fun SubmissionStatusContent(
    status: String,
    visibility: String?,
    publicationId: String?,
    busy: Boolean,
    onOpenPublishedWork: (String) -> Unit,
) {
    WorkflowStepHeader("提交结果", 0, 1, publicationStatusLabel(status))
    WorkflowNotice(
        when (status) {
            "PENDING_CHECK" -> "作品正在检查，通过前不会公开展示。"
            "PENDING_HUMAN_REVIEW" -> "作品正在由老师进一步检查。"
            "PUBLISHED" -> "作品已经通过并发布。当前范围：${publicationVisibilityLabel(visibility)}。"
            "RETURNED" -> "作品需要修改后重新提交。请到创作档案点“继续创作”。"
            "RESTRICTED" -> "作品暂时不能发布，可以在创作档案的“更多”中申诉。"
            "WITHDRAWN" -> "作品已经撤回，作品和版本仍会保留。"
            else -> "当前范围：${publicationVisibilityLabel(visibility)}。"
        }
    )
    if (status == "PUBLISHED" && visibility == "COMMUNITY" && publicationId != null) {
        Button(
            onClick = { onOpenPublishedWork(publicationId) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("查看已发布作品", fontFamily = YaHei)
        }
    }
}

@Composable
private fun TestWorkflowContent(
    currentVersionId: String?,
    latestTest: CreationTestRecordDto?,
    openIssues: List<CreationTestIssueDto>,
    scenario: String,
    onScenarioChange: (String) -> Unit,
    result: String,
    onResultChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    finding: String,
    onFindingChange: (String) -> Unit,
    busy: Boolean,
    onRecordTest: (String, String, String, String) -> Unit,
    onResolveIssue: (CreationTestIssueDto, String) -> Unit,
    onEnterSeal: () -> Unit,
) {
    val canSeal = latestTest?.result == "PASSED" && openIssues.isEmpty()
    WorkflowStepHeader("测试阶段", 0, 1, when {
        openIssues.isNotEmpty() -> "处理发现的问题"
        canSeal -> "确认测试结果"
        else -> "记录一次真实测试"
    })
    when {
        openIssues.isNotEmpty() -> {
            val issue = openIssues.first()
            var issueResolution by rememberSaveable(issue.id) { mutableStateOf("") }
            Text("还需处理 ${openIssues.size} 个问题", fontFamily = YaHei, fontWeight = FontWeight.Bold)
            Text(issue.description, fontFamily = YaHei, fontSize = 13.sp)
            TextField(
                value = issueResolution,
                onValueChange = { issueResolution = it.take(500) },
                label = { Text("我是怎样处理的", fontFamily = YaHei) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onResolveIssue(issue, issueResolution) },
                enabled = !busy && issueResolution.trim().length >= 2,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("保存处理结果", fontFamily = YaHei) }
        }
        canSeal -> {
            WorkflowNotice("这次测试已经通过，可以开始填写作品说明。")
            Button(
                onClick = onEnterSeal,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("进入封卷", fontFamily = YaHei) }
        }
        else -> {
            latestTest?.let {
                WorkflowFact(
                    "上次结果",
                    if (it.result == "BLOCKED") "暂时受阻" else "需要修改后再测",
                )
            }
            TextField(
                value = scenario,
                onValueChange = onScenarioChange,
                label = { Text("怎样测试", fontFamily = YaHei) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "NEEDS_REVISION" to "需要修改",
                    "PASSED" to "通过",
                    "BLOCKED" to "暂时受阻",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = result == value,
                        onClick = { onResultChange(value) },
                        label = { Text(label, fontFamily = YaHei, fontSize = 11.sp) },
                        enabled = !busy,
                    )
                }
            }
            TextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("观察到了什么（可选）", fontFamily = YaHei) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (result != "PASSED") {
                TextField(
                    value = finding,
                    onValueChange = onFindingChange,
                    label = { Text("发现的问题", fontFamily = YaHei) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = { onRecordTest(scenario, result, notes, finding) },
                enabled = !busy && currentVersionId != null && scenario.trim().length >= 2 &&
                    (result == "PASSED" || finding.trim().length >= 2),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("保存测试结果", fontFamily = YaHei) }
        }
    }
}

@Composable
private fun SealWorkflowContent(
    step: Int,
    onStepChange: (Int) -> Unit,
    projectId: String,
    currentVersionId: String?,
    manualSources: List<CreationManualOption>,
    methodManualPageIds: List<String>,
    initialMethodSummary: String?,
    learningCard: LearningCardDto?,
    provenance: ProvenanceManifestDto?,
    sealCheck: CreationSealCheckDto?,
    publicationStatus: String?,
    publicationId: String?,
    publicationVisibility: String?,
    classrooms: List<ClassroomDto>,
    busy: Boolean,
    onSaveLearningCard: (LearningCardForm, () -> Unit) -> Unit,
    onSubmitMigrationEvidence: (List<String>, String, () -> Unit) -> Unit,
    onSaveProvenance: (ProvenanceForm, () -> Unit) -> Unit,
    onSaveSealCheck: (SealCheckForm, () -> Unit) -> Unit,
    onSaveReflectionPackage: (SealCheckForm, LearningCardForm, () -> Unit) -> Unit,
    onSubmit: (String, String?) -> Unit,
    onOpenPublishedWork: (String) -> Unit,
) {
    var workDescription by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.workDescription.orEmpty())
    }
    var learningReflection by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.learningReflection.orEmpty())
    }
    var nextImprovement by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.nextImprovement.orEmpty())
    }
    var identityChecked by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.identityPrivacyConfirmed ?: false)
    }
    var contactChecked by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.contactPrivacyConfirmed ?: false)
    }
    var portraitChecked by rememberSaveable(projectId, currentVersionId, sealCheck?.rowVersion) {
        mutableStateOf(sealCheck?.portraitRightsConfirmed ?: false)
    }
    var selectedManualIds by rememberSaveable(projectId, currentVersionId, learningCard?.rowVersion) {
        mutableStateOf(learningCard?.manualPageIds ?: methodManualPageIds)
    }
    var methodSummary by rememberSaveable(projectId, currentVersionId, learningCard?.rowVersion) {
        mutableStateOf(learningCard?.methodSummary ?: initialMethodSummary.orEmpty())
    }
    val savedAiItem = provenance?.items?.firstOrNull { it.itemType == "AI_CONTRIBUTION" }
    val savedExternalItem = provenance?.items?.firstOrNull { it.itemType == "EXTERNAL_MATERIAL" }
    var humanSummary by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.humanContributionSummary.orEmpty())
    }
    var aiUsed by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.aiAssistanceUsed ?: false)
    }
    var showAiDetails by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(false)
    }
    var aiSummary by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.aiContributionSummary.orEmpty())
    }
    var aiProvider by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(
            savedAiItem?.aiProvider
                ?.takeUnless { it.equals("development", ignoreCase = true) }
                .orEmpty()
        )
    }
    var aiModel by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedAiItem?.aiModel.orEmpty())
    }
    var aiAction by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedAiItem?.aiToolAction.orEmpty())
    }
    var promptSummary by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedAiItem?.promptSummary.orEmpty())
    }
    var aiResultModified by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedAiItem?.userModified ?: false)
    }
    var aigcLabelDeclared by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.aigcLabelDeclared ?: false)
    }
    var externalUrl by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedExternalItem?.sourceUrl.orEmpty())
    }
    var externalAuthor by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedExternalItem?.sourceAuthor.orEmpty())
    }
    var externalLicense by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedExternalItem?.licenseType ?: "CC_BY")
    }
    var unresolvedRights by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.unresolvedRights ?: false)
    }
    var pendingVisibility by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf<String?>(
            if (classrooms.any { it.canSubmit }) "CLASSROOM" else "GUARDIAN_ONLY"
        )
    }
    var selectedClassroomId by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(classrooms) {
        val available = classrooms.filter { it.canSubmit }
        if (selectedClassroomId == null && available.size == 1) {
            selectedClassroomId = available.single().id
        }
    }
    val formEnabled = publicationStatus == null && !busy

    WorkflowStepHeader(
        title = "封卷准备",
        step = step,
        total = 4,
        task = listOf("作品说明", "学习收获", "来源确认", "隐私与投递")[step.coerceIn(0, 3)],
    )
    when (step.coerceIn(0, 3)) {
    0 -> {
    ModuleTitle("作品说明", sealCheck?.status)
    TextField(
        value = workDescription,
        onValueChange = { workDescription = it.take(3000) },
        label = { Text("这件作品是什么、希望表达什么", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = { onStepChange(1) },
        enabled = formEnabled && workDescription.trim().length >= 2,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("下一项", fontFamily = YaHei) }
    }

    1 -> {
    ModuleTitle("学习收获", learningCard?.status)
    Text(
        "写下这次学到的内容和下一次想改的地方；秘籍可以不选。",
        fontFamily = YaHei,
        fontSize = 12.sp,
        color = Color(0xFF667166),
    )
    if (manualSources.isEmpty()) {
        Text("暂无已学秘籍，可直接填写创作方法。", fontFamily = YaHei, fontSize = 12.sp)
    } else {
        manualSources.forEach { manual ->
            CheckRow(
                text = "${manual.title} · ${manual.stateLabel}",
                checked = manual.id in selectedManualIds,
                enabled = formEnabled,
            ) { selected ->
                selectedManualIds = if (selected) {
                    (selectedManualIds + manual.id).distinct()
                } else {
                    selectedManualIds - manual.id
                }
            }
        }
    }
    TextField(
        value = learningReflection,
        onValueChange = { learningReflection = it.take(3000) },
        label = { Text("这次学会了什么", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    TextField(
        value = nextImprovement,
        onValueChange = { nextImprovement = it.take(3000) },
        label = { Text("下一次还想改什么", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = {
            onSaveReflectionPackage(
                SealCheckForm(
                    workDescription,
                    learningReflection,
                    nextImprovement,
                    identityChecked,
                    contactChecked,
                    portraitChecked,
                ),
                LearningCardForm(
                    selectedManualIds,
                    methodSummary.ifBlank { learningReflection },
                    unresolvedQuestions = "",
                    questionsConfirmed = true,
                ),
            ) { onStepChange(2) }
        },
        enabled = formEnabled && currentVersionId != null && learningReflection.trim().length >= 2 &&
            nextImprovement.trim().length >= 2,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("保存并继续", fontFamily = YaHei)
    }
    }

    2 -> {
    ModuleTitle("来源确认", provenance?.status)
    TextField(
        value = humanSummary,
        onValueChange = { humanSummary = it.take(3000) },
        label = { Text("我亲自完成了什么", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    CheckRow("创作中使用了生成式 AI", aiUsed, formEnabled) {
        aiUsed = it
        if (!it) aigcLabelDeclared = false
    }
    if (aiUsed) {
        Text(
            "如果是在本应用里生成的图片，系统会自动记录来源；只有使用其他工具时，才需要补充详细信息。",
            fontFamily = YaHei,
            fontSize = 12.sp,
            color = Color(0xFF667166),
        )
        TextField(
            value = aiSummary,
            onValueChange = { aiSummary = it.take(3000) },
            label = { Text("AI 帮助了什么", fontFamily = YaHei) },
            minLines = 2,
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { showAiDetails = !showAiDetails },
            enabled = formEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(
                if (showAiDetails) "收起其他工具信息" else "补充其他工具信息（可选）",
                fontFamily = YaHei,
            )
        }
        if (showAiDetails) {
            TextField(
                value = aiProvider,
                onValueChange = { aiProvider = it.take(80) },
                label = { Text("使用的工具或平台", fontFamily = YaHei) },
                enabled = formEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = aiAction,
                onValueChange = { aiAction = it.take(120) },
                label = { Text("它帮你完成了什么", fontFamily = YaHei) },
                enabled = formEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = promptSummary,
                onValueChange = { promptSummary = it.take(500) },
                label = { Text("给工具的要求（可简要填写）", fontFamily = YaHei) },
                enabled = formEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        CheckRow("我对 AI 结果作过选择或修改", aiResultModified, formEnabled) { aiResultModified = it }
        CheckRow("我同意作品卡注明智能工具参与", aigcLabelDeclared, formEnabled) {
            aigcLabelDeclared = it
        }
    }
    TextField(
        value = externalUrl,
        onValueChange = { externalUrl = it.take(500) },
        label = { Text("外部素材来源网址（没有可不填）", fontFamily = YaHei) },
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    if (externalUrl.isNotBlank()) {
        TextField(
            value = externalAuthor,
            onValueChange = { externalAuthor = it.take(100) },
            label = { Text("原作者或来源机构", fontFamily = YaHei) },
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("素材许可", fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "CC0" to "自由使用",
                "CC_BY" to "注明作者",
                "CC_BY_SA" to "注明作者并同样共享",
                "PUBLIC_DOMAIN" to "公共领域",
            ).forEach { (license, label) ->
                FilterChip(
                    selected = externalLicense == license,
                    onClick = { externalLicense = license },
                    label = { Text(label, fontFamily = YaHei, fontSize = 10.sp) },
                    enabled = formEnabled,
                )
            }
        }
    }
    CheckRow("仍有素材授权问题没有解决", unresolvedRights, formEnabled) { unresolvedRights = it }
    val aiFieldsComplete = !aiUsed || (aiSummary.trim().length >= 2 && aigcLabelDeclared)
    val externalSourceValid = externalUrl.isBlank() ||
        externalUrl.startsWith("http://") || externalUrl.startsWith("https://")
    if (aiUsed && !aiFieldsComplete) {
        Text(
            "请说明智能工具帮了什么，并确认作品卡会显示相应标识。",
            fontFamily = YaHei,
            fontSize = 12.sp,
            color = Color(0xFF8A5A22),
        )
    }
    if (!externalSourceValid) {
        Text(
            "来源网址需要以 http:// 或 https:// 开头。",
            fontFamily = YaHei,
            fontSize = 12.sp,
            color = Color(0xFF8C4D3D),
        )
    }
    if (unresolvedRights) {
        Text(
            "授权问题会随草稿保存，但解决前不能提交。",
            fontFamily = YaHei,
            fontSize = 12.sp,
            color = Color(0xFF8A5A22),
        )
    }
    Button(
        onClick = {
            onSaveProvenance(
                ProvenanceForm(
                    humanSummary,
                    aiUsed,
                    aiSummary,
                    aiProvider.ifBlank { "其他生成工具" },
                    aiModel.ifBlank { "未说明版本" },
                    aiAction.ifBlank { "辅助生成或整理内容" },
                    promptSummary.ifBlank { aiSummary.trim() },
                    aiResultModified,
                    aigcLabelDeclared,
                    externalUrl,
                    externalAuthor,
                    externalLicense,
                    unresolvedRights,
                )
            ) { onStepChange(3) }
        },
        enabled = formEnabled && currentVersionId != null && humanSummary.trim().length >= 2 &&
            aiFieldsComplete && externalSourceValid && !unresolvedRights,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("保存并继续", fontFamily = YaHei) }
    }

    else -> {
    ModuleTitle("隐私与投递", publicationStatus ?: sealCheck?.status)
    val packageComplete = sealCheck?.status == "COMPLETE" &&
        learningCard?.status == "COMPLETE" && provenance?.status == "COMPLETE"
    if (publicationStatus == null && !packageComplete) {
        CheckRow("作品中没有真实姓名、学校或可识别身份信息", identityChecked, formEnabled) {
            identityChecked = it
        }
        CheckRow("作品中没有手机号、地址、账号等联系方式", contactChecked, formEnabled) {
            contactChecked = it
        }
        CheckRow("没有敏感肖像，或已经取得相关使用许可", portraitChecked, formEnabled) {
            portraitChecked = it
        }
    }
    if (publicationStatus != null) {
        WorkflowNotice(
            "当前提交：${publicationStatusLabel(publicationStatus)} · " +
                publicationVisibilityLabel(publicationVisibility)
        )
        if (publicationStatus == "RETURNED") {
            Text(
                "该版本已退回。请保存新版本，系统会回到制作阶段，并要求重新测试。",
                fontFamily = YaHei,
                fontSize = 12.sp,
                color = Color(0xFF8C4D3D),
            )
        }
        if (publicationStatus == "PUBLISHED" && publicationVisibility == "COMMUNITY" && publicationId != null) {
            OutlinedButton(
                onClick = { onOpenPublishedWork(publicationId) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text("打开大会作品，查看评招与采纳记录", fontFamily = YaHei)
            }
        }
    } else if (!packageComplete) {
        WorkflowNotice("先保存隐私确认；系统会同时检查作品说明、学习收获和来源记录。")
        Button(
            onClick = {
                onSaveSealCheck(
                    SealCheckForm(
                        workDescription,
                        learningReflection,
                        nextImprovement,
                        identityChecked,
                        contactChecked,
                        portraitChecked,
                    )
                ) {}
            },
            enabled = formEnabled && currentVersionId != null && identityChecked && contactChecked &&
                portraitChecked,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("保存并检查", fontFamily = YaHei) }
    } else {
        Text("选择投递位置", fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "GUARDIAN_ONLY" to "家长",
                "CLASSROOM" to "班级",
                "COMMUNITY" to "知行流",
            ).forEach { (visibility, label) ->
                FilterChip(
                    selected = pendingVisibility == visibility,
                    onClick = { pendingVisibility = visibility },
                    label = { Text(label, fontFamily = YaHei) },
                    enabled = !busy && (visibility != "CLASSROOM" || classrooms.any { it.canSubmit }),
                )
            }
        }
        if (pendingVisibility == "CLASSROOM") {
            val availableClassrooms = classrooms.filter { it.canSubmit }
            Text("选择班级", fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            availableClassrooms.forEach { classroom ->
                FilterChip(
                    selected = selectedClassroomId == classroom.id,
                    onClick = { selectedClassroomId = classroom.id },
                    label = { Text("${classroom.name} · ${classroom.teacherNickname}", fontFamily = YaHei) },
                    enabled = !busy,
                )
            }
        }
        WorkflowNotice("提交后会先进行内容与隐私检查，通过前不会公开展示。")
        Button(
            onClick = {
                pendingVisibility?.let { visibility ->
                    onSubmit(
                        visibility,
                        selectedClassroomId.takeIf { visibility == "CLASSROOM" },
                    )
                }
            },
            enabled = !busy && pendingVisibility != null &&
                (pendingVisibility != "CLASSROOM" || selectedClassroomId != null),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("提交作品", fontFamily = YaHei) }
    }
    }
    }
}

@Composable
private fun CheckRow(
    text: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Text(text, fontFamily = YaHei, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ModuleTitle(title: String, status: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        Text(
            text = when (status) {
                "COMPLETE" -> "已完成"
                "LOCKED" -> "已锁定"
                "PENDING_CHECK" -> "检查中"
                "PENDING_HUMAN_REVIEW" -> "人工复核"
                "PUBLISHED" -> "已发布"
                "RETURNED" -> "已退回"
                else -> "待完善"
            },
            fontFamily = YaHei,
            fontSize = 11.sp,
            color = if (status in setOf("COMPLETE", "LOCKED", "PUBLISHED")) {
                Color(0xFF426A47)
            } else {
                Color(0xFF8A5A22)
            },
        )
    }
}

@Composable
private fun WorkflowStepHeader(title: String, step: Int, total: Int, task: String) {
    val safeStep = step.coerceIn(0, total - 1)
    Text(
        "$title · ${safeStep + 1}/$total",
        color = Color(0xFF294E36),
        fontFamily = YaHei,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
    LinearProgressIndicator(
        progress = { (safeStep + 1).toFloat() / total.toFloat() },
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        task,
        color = Color(0xFF526354),
        fontFamily = YaHei,
        fontSize = 13.sp,
    )
}

private fun publicationStatusLabel(status: String): String = when (status) {
    "PENDING_CHECK" -> "自动检查中"
    "PENDING_HUMAN_REVIEW" -> "等待人工复核"
    "PUBLISHED" -> "已通过并发布"
    "RETURNED" -> "已退回修改"
    "RESTRICTED" -> "限制展示"
    "WITHDRAWN" -> "已撤回"
    else -> status
}

private fun publicationVisibilityLabel(visibility: String?): String = when (visibility) {
    "PRIVATE" -> "仅自己"
    "GUARDIAN_ONLY" -> "家长可见"
    "CLASSROOM" -> "教师/班级可见"
    "COMMUNITY" -> "知行流"
    else -> "未选择"
}

private fun generationSizeLabel(size: String): String = when (size) {
    "PORTRAIT" -> "竖版"
    "LANDSCAPE" -> "横版"
    else -> "方形"
}

private fun generationQualityLabel(quality: String): String = when (quality) {
    "LOW" -> "快速"
    "HIGH" -> "精细"
    else -> "标准"
}

@Composable
private fun WorkflowFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(value, fontFamily = YaHei, fontSize = 13.sp, color = Color(0xFF3F4D41))
    }
}

@Composable
private fun WorkflowNotice(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE7EEDB), RoundedCornerShape(12.dp))
            .padding(12.dp),
        color = Color(0xFF38513C),
        fontFamily = YaHei,
        fontSize = 13.sp,
    )
}

private fun workflowStageLabel(stage: String?): String = when (stage) {
    "IDEATION" -> "构思"
    "DRAFT" -> "草图"
    "PRODUCTION" -> "制作"
    "TEST" -> "测试"
    "SEAL" -> "说明"
    else -> "流程"
}

private fun generationStatusLabel(status: String): String = when (status) {
    "QUEUED" -> "等待生成"
    "RUNNING" -> "正在生成"
    "SAFETY_CHECK" -> "安全检查中"
    "VERSIONING" -> "正在写入版本"
    "COMPLETED" -> "生成完成"
    "REJECTED" -> "内容未通过检查"
    else -> "生成失败"
}
