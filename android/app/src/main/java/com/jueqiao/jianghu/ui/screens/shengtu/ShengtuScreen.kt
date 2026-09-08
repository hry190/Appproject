package com.jueqiao.jianghu.ui.screens.shengtu

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
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
    onSaveLearningCard: (LearningCardForm) -> Unit = {},
    onSubmitMigrationEvidence: (List<String>, String) -> Unit = { _, _ -> },
    onSaveProvenance: (ProvenanceForm) -> Unit = {},
    onSaveSealCheck: (SealCheckForm) -> Unit = {},
    onSubmit: (String, String?) -> Unit = { _, _ -> },
    onOpenPublishedWork: (String) -> Unit = {},
    onCreateWork: () -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致
    BackHandler(enabled = true) {
        android.util.Log.d("Shengtu", "BackHandler triggered")
        onBack()
    }

    // 输入框焦点管理
    val rect227FocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val draftStore = remember(context) { CreationDraftStore(context) }
    val draftKey = remember(projectId) {
        if (projectId == null) CreationDraftStore.Keys.ImagePrompt
        else "${CreationDraftStore.Keys.ImagePrompt}:$projectId"
    }
    var rect227Text by rememberSaveable(projectId) {
        mutableStateOf(
            draftStore.read(draftKey).ifBlank { initialPrompt.orEmpty() }
        )
    }
    var draftNotice by rememberSaveable(projectId) { mutableStateOf<String?>(null) }
    var showWorkflowDialog by rememberSaveable(projectId) { mutableStateOf(false) }

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

            // 卷轴标题区单独留出固定宽度，发布按钮永远不会覆盖标题。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 125.dp),
            ) {
                Text(
                    text = if (projectId != null) {
                        "正在为《${projectTitle ?: "未命名作品"}》准备创作草稿"
                    } else {
                        "正在生成图片"
                    },
                    color = Color(0xFF596756),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 42.dp, y = 2.dp)
                        .width(220.dp),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 291.dp, y = 0.dp)
                        .size(width = 65.dp, height = 30.dp)
                        .semantics { contentDescription = "打开创作流程" }
                        .clickable(enabled = projectId != null) {
                            showWorkflowDialog = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shengtu_group253),
                        contentDescription = null,
                        modifier = Modifier.size(width = 65.dp, height = 23.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            // Agent 思考状态卡：提供明确的当前动作、进度和后续步骤。
            Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 152.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Surface(
                    color = Color(0xFFF9F4E6).copy(alpha = .96f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(width = 300.dp, height = 370.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB9B993).copy(alpha = .72f),
                            shape = RoundedCornerShape(20.dp),
                        ),
                    shadowElevation = 5.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color(0xFF789D79),
                                        shape = RoundedCornerShape(12.dp),
                                    ),
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
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "CREATIVE AGENT",
                                    color = Color(0xFF91A08A),
                                    fontFamily = YaHei,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    text = "正在把灵感整理成草稿",
                                    color = Color(0xFF3E5B43),
                                    fontFamily = YaHei,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = "我会先理解你的想法，再给出可继续编辑的创作方向。",
                            color = Color(0xFF76816F),
                            fontFamily = YaHei,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Surface(
                            color = Color(0xFFEEF3E3),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                            .padding(top = 14.dp),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("思考中", color = Color(0xFF5B765F), fontFamily = YaHei, fontSize = 12.sp)
                                    Text("56%", color = Color(0xFF8DA28D), fontFamily = YaHei, fontSize = 12.sp)
                                }
                                LinearProgressIndicator(
                                    progress = { .56f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 7.dp)
                                        .height(6.dp),
                                    color = Color(0xFF7EA67D),
                                    trackColor = Color(0xFFDCE7D1),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 10.dp),
                                ) {
                                    Text("•••", color = Color(0xFF6F9874), fontFamily = YaHei, fontSize = 14.sp)
                                    Text(
                                        "正在梳理主题、角色与画面关系",
                                        color = Color(0xFF6F9874),
                                        fontFamily = YaHei,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 5.dp),
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AgentStep("理解创作意图", done = true)
                            AgentStep("规划作品结构")
                            AgentStep("生成首版草稿")
                        }
                        Text(
                            text = "完成后你可以直接修改文字、继续对话，或保存为草稿。",
                            color = Color(0xFF929980),
                            fontFamily = YaHei,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }

            // Rectangle 231.png(X=56, Y=605, W=193, H=30) + 文字 "帮我绘画一只在做手表的技巧熊猫"
            Box(
                modifier = Modifier
                    .offset(x = 56.dp, y = 605.dp)
                    .size(width = 193.dp, height = 30.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_rect231),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "帮我绘画一只在做手表的技巧熊猫",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Normal, // Regular
                        fontSize = 12.sp,
                    ),
                    modifier = Modifier
                        .size(width = 180.dp, height = 16.dp),
                )
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
                        if (rect227Text.isEmpty()) {
                            Text(
                                text = "继续告诉 Agent 你想怎么修改……",
                                color = Color(0xFF94A48F),
                                fontFamily = YaHei,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp),
                            )
                        } else {
                            Text(
                                text = rect227Text,
                                color = Color(0xFF3D4F3F),
                                fontFamily = YaHei,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp),
                            )
                        }
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
                                fontSize = 10.sp,
                                color = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .padding(horizontal = 12.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF83AA7D), RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("↗", color = Color.White, fontFamily = YaHei, fontSize = 16.sp)
                    }
                }
            }
            Text(
                text = "输入内容会保存在当前创作草稿中",
                color = Color(0xFF99A08D),
                fontFamily = YaHei,
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 724.dp)
                    .padding(horizontal = 45.dp),
            )
            // 行囊项目保存为服务端不可覆盖版本；无项目 ID 的旧入口仍沿用原流程。
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 393.dp)
                    .size(width = 372.dp, height = 55.dp)
                    .clickable(enabled = rect227Text.isNotBlank() && !savingDraft) {
                        if (projectId != null) {
                            draftStore.save(draftKey, rect227Text)
                            onSaveDraft(rect227Text)
                        } else {
                            draftStore.clear(draftKey)
                            onCreateWork()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_group196),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = when {
                        savingDraft -> "保存中…"
                        projectId != null -> "保存草图/脚本"
                        else -> "保存作品"
                    },
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
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

    if (showWorkflowDialog && projectId != null) {
        CreationWorkflowDialog(
            projectId = projectId,
            currentStage = currentStage ?: "DRAFT",
            currentVersionId = currentVersionId,
            currentPrompt = rect227Text,
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
            onSubmit = onSubmit,
            onOpenPublishedWork = onOpenPublishedWork,
            onOpenEditor = onCreateWork,
        )
    }
}

@Composable
private fun AgentStep(label: String, done: Boolean = false) {
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
            fontSize = 12.sp,
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
    onSaveLearningCard: (LearningCardForm) -> Unit,
    onSubmitMigrationEvidence: (List<String>, String) -> Unit,
    onSaveProvenance: (ProvenanceForm) -> Unit,
    onSaveSealCheck: (SealCheckForm) -> Unit,
    onSubmit: (String, String?) -> Unit,
    onOpenPublishedWork: (String) -> Unit,
    onOpenEditor: () -> Unit,
) {
    var coachPrompt by rememberSaveable(projectId) {
        mutableStateOf(currentPrompt.ifBlank { "请检查我的主题是否清楚，并给出文字修改建议" })
    }
    var generationPrompt by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf(currentPrompt)
    }
    var generationSize by rememberSaveable(projectId) { mutableStateOf("SQUARE") }
    var generationQuality by rememberSaveable(projectId) { mutableStateOf("MEDIUM") }
    var reviewingGeneration by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf(false)
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
                    text = "创作流程 · ${workflowStageLabel(currentStage)}",
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

                when (currentStage) {
                    "PRODUCTION" -> ProductionWorkflowContent(
                        generationPrompt = generationPrompt,
                        onGenerationPromptChange = {
                            generationPrompt = it.take(2000)
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
                        onResultChange = { testResult = it },
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
    Text("1. 可选：生成一层画面", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Text(
        "生成是明确的工具调用，不是自主 Agent。只有你在确认页点击确认后才会执行。",
        fontFamily = YaHei,
        fontSize = 12.sp,
        color = Color(0xFF526354),
    )
    generationCapability?.let { capability ->
        WorkflowFact("今日额度", "已用 ${capability.dailyUsed}/${capability.dailyLimit}，剩余 ${capability.dailyRemaining}")
        WorkflowFact(
            "运行方式",
            if (capability.providerRef == "development") {
                "本地开发预览 · ${capability.modelRef}"
            } else {
                "${capability.providerRef} · ${capability.modelRef}"
            },
        )
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
                    AsyncImage(
                        model = url,
                        contentDescription = "生成结果预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                if (job.status == "COMPLETED") {
                    Text(
                        "已写入新版本；来源谱已自动标记 AIGC，提交前仍需补充本人修改说明。",
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
    if (!generationActive) {
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
                    WorkflowFact("将产生", "一张 $generationSize / $generationQuality 图片，并作为 AI 图层写入新版本")
                    WorkflowFact(
                        "数据去向",
                        if (generationCapability?.externalDataShared == true) {
                            "提示词会发送给 ${generationCapability.providerRef}；不会发送作品原图"
                        } else {
                            "提示词不离开本服务"
                        },
                    )
                    WorkflowFact(
                        "后续检查",
                        "哈希、病毒、格式、像素、元数据和内容安全检查；失败不会写入版本",
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

    HorizontalDivider(color = Color(0xFFD7D4BE))
    Text("2. 可选：获取创作教练建议", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    if (pendingCall == null) {
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
            Text(if (latestCompletedCall == null) "准备建议并查看确认页" else "再次获取建议", fontFamily = YaHei)
        }
    } else {
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
        HorizontalDivider(color = Color(0xFFD7D4BE))
        Text("最近一次建议", fontFamily = YaHei, fontWeight = FontWeight.Bold)
        WorkflowNotice(output.get("summary")?.asString ?: "建议已生成")
        output.get("suggested_prompt")?.asString?.let {
            WorkflowFact("可参考的表达", it)
        }
        output.get("checklist")?.asJsonArray?.forEach { item ->
            Text("• ${item.asString}", fontFamily = YaHei, fontSize = 13.sp)
        }
        Text(
            "执行器：${latestCompletedCall.executorRef ?: "规则教练"}；未自动修改作品",
            fontFamily = YaHei,
            fontSize = 11.sp,
            color = Color(0xFF667166),
        )
    }

    HorizontalDivider(color = Color(0xFFD7D4BE))
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

    HorizontalDivider(color = Color(0xFFD7D4BE))
    Text("4. 完成当前版本后进入测试", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Button(
        onClick = onEnterTest,
        enabled = !busy && !generationBusy && !generationActive && currentVersionId != null && pendingCall == null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("进入测试", fontFamily = YaHei)
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
    latestTest?.let {
        WorkflowFact(
            "当前版本最近结果",
            if (it.result == "PASSED") "通过" else if (it.result == "BLOCKED") "受阻" else "需要修改",
        )
    }
    Text("记录一次真实测试", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    TextField(
        value = scenario,
        onValueChange = onScenarioChange,
        label = { Text("测试场景", fontFamily = YaHei) },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = result == "NEEDS_REVISION",
            onClick = { onResultChange("NEEDS_REVISION") },
            label = { Text("需要修改", fontFamily = YaHei) },
            enabled = !busy,
        )
        FilterChip(
            selected = result == "PASSED",
            onClick = { onResultChange("PASSED") },
            label = { Text("通过", fontFamily = YaHei) },
            enabled = !busy,
        )
        FilterChip(
            selected = result == "BLOCKED",
            onClick = { onResultChange("BLOCKED") },
            label = { Text("受阻", fontFamily = YaHei) },
            enabled = !busy,
        )
    }
    TextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("观察记录（可选）", fontFamily = YaHei) },
        modifier = Modifier.fillMaxWidth(),
    )
    TextField(
        value = finding,
        onValueChange = onFindingChange,
        label = { Text(if (result == "PASSED") "仍需留意的问题（可选）" else "发现的问题", fontFamily = YaHei) },
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = { onRecordTest(scenario, result, notes, finding) },
        enabled = !busy && currentVersionId != null && scenario.trim().length >= 2 &&
            (result == "PASSED" || finding.trim().length >= 2),
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("保存测试记录", fontFamily = YaHei)
    }

    if (openIssues.isNotEmpty()) {
        HorizontalDivider(color = Color(0xFFD7D4BE))
        Text("待整改问题（${openIssues.size}）", fontFamily = YaHei, fontWeight = FontWeight.Bold)
        openIssues.forEach { issue ->
            var issueResolution by rememberSaveable(issue.id) { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD4B88A), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(issue.description, fontFamily = YaHei, fontSize = 13.sp)
                TextField(
                    value = issueResolution,
                    onValueChange = { issueResolution = it.take(500) },
                    label = { Text("我是怎样处理的", fontFamily = YaHei) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = { onResolveIssue(issue, issueResolution) },
                    enabled = !busy && issueResolution.trim().length >= 2,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("标记为已处理", fontFamily = YaHei)
                }
            }
        }
    }

    HorizontalDivider(color = Color(0xFFD7D4BE))
    val canSeal = latestTest?.result == "PASSED" && openIssues.isEmpty()
    Button(
        onClick = onEnterSeal,
        enabled = !busy && canSeal,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text("进入作品说明", fontFamily = YaHei)
    }
    if (!canSeal) {
        Text(
            "进入说明需要：当前版本最近一次复测通过，且所有问题已经关闭。",
            fontFamily = YaHei,
            fontSize = 12.sp,
            color = Color(0xFF8A5A22),
        )
    }
}

@Composable
private fun SealWorkflowContent(
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
    onSaveLearningCard: (LearningCardForm) -> Unit,
    onSubmitMigrationEvidence: (List<String>, String) -> Unit,
    onSaveProvenance: (ProvenanceForm) -> Unit,
    onSaveSealCheck: (SealCheckForm) -> Unit,
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
    var unresolvedQuestions by rememberSaveable(projectId, currentVersionId, learningCard?.rowVersion) {
        mutableStateOf(learningCard?.unresolvedQuestions?.joinToString("\n").orEmpty())
    }
    var questionsConfirmed by rememberSaveable(projectId, currentVersionId, learningCard?.rowVersion) {
        mutableStateOf(learningCard?.questionsConfirmed ?: false)
    }
    var migrationReason by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf("")
    }
    val savedAiItem = provenance?.items?.firstOrNull { it.itemType == "AI_CONTRIBUTION" }
    val savedExternalItem = provenance?.items?.firstOrNull { it.itemType == "EXTERNAL_MATERIAL" }
    var humanSummary by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.humanContributionSummary.orEmpty())
    }
    var aiUsed by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.aiAssistanceUsed ?: false)
    }
    var aiSummary by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(provenance?.aiContributionSummary.orEmpty())
    }
    var aiProvider by rememberSaveable(projectId, currentVersionId, provenance?.rowVersion) {
        mutableStateOf(savedAiItem?.aiProvider.orEmpty())
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
        mutableStateOf<String?>(null)
    }
    var selectedClassroomId by rememberSaveable(projectId, currentVersionId) {
        mutableStateOf<String?>(null)
    }
    val formEnabled = publicationStatus == null && !busy

    Text("封卷检查", fontFamily = YaHei, fontWeight = FontWeight.Bold)
    Text(
        "所有内容固定在当前版本；提交后学习卡、来源谱和封卷说明将锁定。",
        fontFamily = YaHei,
        fontSize = 12.sp,
        color = Color(0xFF667166),
    )

    ModuleTitle("1. 作品说明与隐私", sealCheck?.status)
    TextField(
        value = workDescription,
        onValueChange = { workDescription = it.take(3000) },
        label = { Text("这件作品是什么、希望表达什么", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
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
    CheckRow("我已检查：作品中没有真实姓名、学校或可识别身份信息", identityChecked, formEnabled) {
        identityChecked = it
    }
    CheckRow("我已检查：作品中没有手机号、地址、账号等联系方式", contactChecked, formEnabled) {
        contactChecked = it
    }
    CheckRow("我已确认：没有敏感肖像，或已经取得相关使用许可", portraitChecked, formEnabled) {
        portraitChecked = it
    }
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
            )
        },
        enabled = formEnabled && currentVersionId != null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("保存作品说明与隐私自查", fontFamily = YaHei) }

    HorizontalDivider(color = Color(0xFFD7D4BE))
    ModuleTitle("2. 学习说明", learningCard?.status)
    Text(
        "秘籍是可选项；自由创作即使不选秘籍也可以封卷。",
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
        value = methodSummary,
        onValueChange = { methodSummary = it.take(3000) },
        label = { Text("我用了什么方法或秘籍招式", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    TextField(
        value = unresolvedQuestions,
        onValueChange = { unresolvedQuestions = it.take(3000) },
        label = { Text("还没解决的问题（每行一个，可不填）", fontFamily = YaHei) },
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    CheckRow("我确认已如实记录尚未解决的问题", questionsConfirmed, formEnabled) {
        questionsConfirmed = it
    }
    Button(
        onClick = {
            onSaveLearningCard(
                LearningCardForm(
                    selectedManualIds,
                    methodSummary,
                    unresolvedQuestions,
                    questionsConfirmed,
                )
            )
        },
        enabled = formEnabled && currentVersionId != null,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("保存学习说明", fontFamily = YaHei) }
    TextField(
        value = migrationReason,
        onValueChange = { migrationReason = it.take(500) },
        label = { Text("这次作品如何使用了所选秘籍（迁移理由）", fontFamily = YaHei) },
        minLines = 2,
        enabled = formEnabled,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = { onSubmitMigrationEvidence(selectedManualIds, migrationReason) },
        enabled = formEnabled && currentVersionId != null && selectedManualIds.isNotEmpty() && migrationReason.trim().isNotEmpty(),
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("提交迁移证据（等待审核）", fontFamily = YaHei) }

    HorizontalDivider(color = Color(0xFFD7D4BE))
    ModuleTitle("3. 来源与人机分工", provenance?.status)
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
        TextField(
            value = aiSummary,
            onValueChange = { aiSummary = it.take(3000) },
            label = { Text("AI 帮助了什么", fontFamily = YaHei) },
            minLines = 2,
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = aiProvider,
            onValueChange = { aiProvider = it.take(80) },
            label = { Text("服务提供方", fontFamily = YaHei) },
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = aiModel,
            onValueChange = { aiModel = it.take(120) },
            label = { Text("模型或工具名称", fontFamily = YaHei) },
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = aiAction,
            onValueChange = { aiAction = it.take(120) },
            label = { Text("调用动作，例如生成底图", fontFamily = YaHei) },
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = promptSummary,
            onValueChange = { promptSummary = it.take(500) },
            label = { Text("提示词摘要", fontFamily = YaHei) },
            enabled = formEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        CheckRow("我对 AI 结果作过选择或修改", aiResultModified, formEnabled) { aiResultModified = it }
        CheckRow("我同意作品卡显示 AIGC 标识", aigcLabelDeclared, formEnabled) {
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
            listOf("CC0", "CC_BY", "CC_BY_SA", "PUBLIC_DOMAIN").forEach { license ->
                FilterChip(
                    selected = externalLicense == license,
                    onClick = { externalLicense = license },
                    label = { Text(license.replace('_', '-'), fontSize = 10.sp) },
                    enabled = formEnabled,
                )
            }
        }
    }
    CheckRow("仍有素材授权问题没有解决", unresolvedRights, formEnabled) { unresolvedRights = it }
    val aiFieldsComplete = !aiUsed || (
        aiSummary.trim().length >= 2 && aiProvider.isNotBlank() && aiModel.isNotBlank() &&
            aiAction.isNotBlank() && promptSummary.isNotBlank() && aigcLabelDeclared
        )
    val externalSourceValid = externalUrl.isBlank() ||
        externalUrl.startsWith("http://") || externalUrl.startsWith("https://")
    if (aiUsed && !aiFieldsComplete) {
        Text(
            "可以先保存草稿；提交前需补齐 AI 作用、提供方、工具名称、动作、提示摘要和 AIGC 标识。",
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
                    aiProvider,
                    aiModel,
                    aiAction,
                    promptSummary,
                    aiResultModified,
                    aigcLabelDeclared,
                    externalUrl,
                    externalAuthor,
                    externalLicense,
                    unresolvedRights,
                )
            )
        },
        enabled = formEnabled && currentVersionId != null && externalSourceValid,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) { Text("保存来源与人机分工", fontFamily = YaHei) }

    HorizontalDivider(color = Color(0xFFD7D4BE))
    ModuleTitle("4. 保存或发布", publicationStatus)
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
    } else {
        val packageComplete = sealCheck?.status == "COMPLETE" &&
            learningCard?.status == "COMPLETE" && provenance?.status == "COMPLETE"
        WorkflowNotice("仅自己保存：不点击提交即可，已保存内容会留在个人创作档案中。")
        if (!packageComplete) {
            Text(
                "提交前请让上面三个模块都显示“已完成”。",
                fontFamily = YaHei,
                fontSize = 12.sp,
                color = Color(0xFF8A5A22),
            )
        }
        listOf(
            "GUARDIAN_ONLY" to "交给家长查看",
            "CLASSROOM" to "提交教师查看（${classrooms.count { it.canSubmit }} 个可选班级）",
            "COMMUNITY" to "发布到知行流",
        ).forEach { (visibility, label) ->
            OutlinedButton(
                onClick = { pendingVisibility = visibility },
                enabled = !busy && packageComplete,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text(label, fontFamily = YaHei) }
        }
        pendingVisibility?.let { visibility ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD4B88A), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("提交确认", fontFamily = YaHei, fontWeight = FontWeight.Bold)
                WorkflowFact("去向", publicationVisibilityLabel(visibility))
                if (visibility == "CLASSROOM") {
                    val availableClassrooms = classrooms.filter { it.canSubmit }
                    if (availableClassrooms.isEmpty()) {
                        Text(
                            "尚未加入班级，请先到“书信”页面输入教师提供的邀请码。",
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                            color = Color(0xFF8C4D3D),
                        )
                    } else {
                        Text("选择投递班级", fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        availableClassrooms.forEach { classroom ->
                            FilterChip(
                                selected = selectedClassroomId == classroom.id,
                                onClick = { selectedClassroomId = classroom.id },
                                label = { Text("${classroom.name} · ${classroom.teacherNickname}", fontFamily = YaHei) },
                                enabled = !busy,
                            )
                        }
                    }
                }
                WorkflowFact("提交后", "先进入内容与隐私检查；通过前不会公开展示")
                Text(
                    "学习卡、来源谱和封卷说明将锁定。需要修改时应保存新版本并重新测试。",
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                    color = Color(0xFF667166),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { pendingVisibility = null },
                        enabled = !busy,
                        modifier = Modifier.height(48.dp),
                    ) { Text("返回检查", fontFamily = YaHei) }
                    Button(
                        onClick = {
                            onSubmit(
                                visibility,
                                selectedClassroomId.takeIf { visibility == "CLASSROOM" },
                            )
                        },
                        enabled = !busy && (visibility != "CLASSROOM" || selectedClassroomId != null),
                        modifier = Modifier.height(48.dp),
                    ) { Text("确认提交", fontFamily = YaHei) }
                }
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
