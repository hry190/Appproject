package com.jueqiao.jianghu.ui.screens.gongfang

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.CreationWorkspaceTopBar
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.theme.YaHei

private val Ink = Color(0xFF294A2E)
private val MutedInk = Color(0xFF5E695B)
private val GlassBorder = Color(0xB8F5F0D9)
private val PaleGreen = Color(0xFFD4EDB9)
private val Jade = Color(0xFF397D61)
private val JadeDark = Color(0xFF244F3D)
private val DormantBamboo = Color(0xFFAAA77E)
private val LivingBamboo = Color(0xFF6F963F)

private data class CreationStage(val label: String, val icon: ImageVector)

private val CreationStages = listOf(
    CreationStage("构思", Icons.Outlined.Lightbulb),
    CreationStage("草图/脚本", Icons.Outlined.Description),
    CreationStage("制作", Icons.Outlined.Settings),
    CreationStage("测试", Icons.Outlined.TrackChanges),
    CreationStage("说明", Icons.AutoMirrored.Outlined.MenuBook),
)

/**
 * 作品创作页。入口态只呈现创作入口与最近作品；开始后才显示工法详情和竹节进度链。
 * 背景、熊猫、顶部叶签与气泡均直接复用原资源。
 */
@Composable
fun GongfangScreen(
    onBack: () -> Unit = {},
    onAnalyzeIntent: (String, List<String>, List<String>) -> Unit = { _, _, _ -> },
    onContinueWork: (String) -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
    onOpenAllWorks: () -> Unit = {},
    recentWorks: List<CreationResumeItem> = emptyList(),
    recentWorksLoading: Boolean = false,
    recentWorksMessage: String? = null,
    derivativeSourceTitle: String? = null,
    onClearDerivative: () -> Unit = {},
    onLoadRecentWorks: () -> Unit = {},
    manualSources: List<CreationManualOption> = emptyList(),
    manualSourcesLoading: Boolean = false,
    manualSourcesMessage: String? = null,
    onLoadCreationSources: () -> Unit = {},
    uploadingSketch: Boolean = false,
    sketchSourceAssetId: String? = null,
    sketchSourceMessage: String? = null,
    onUploadSketch: (String) -> Unit = {},
    analyzingIntent: Boolean = false,
    creationMethod: CreationMethodPlan? = null,
    analysisMessage: String? = null,
    creatingProject: Boolean = false,
    createProjectMessage: String? = null,
    onConfirmNewProject: (String, String, CreationMethodPlan) -> Unit = { _, _, _ -> },
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var projectTitle by rememberSaveable { mutableStateOf("") }
    var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedManualId by rememberSaveable { mutableStateOf<String?>(null) }
    var showManualPicker by rememberSaveable { mutableStateOf(false) }
    var sourceMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var creationStarted by rememberSaveable { mutableStateOf(false) }
    var currentStage by rememberSaveable { mutableIntStateOf(0) }
    var maxReachedStage by rememberSaveable { mutableIntStateOf(0) }
    var isSealed by rememberSaveable { mutableStateOf(false) }
    var editedMethod by remember { mutableStateOf<CreationMethodPlan?>(null) }
    var receivedMethod by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sketchPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            selectedSource = "上传草图"
            selectedManualId = null
            sourceMenuExpanded = false
            onUploadSketch(uri.toString())
        }
    }
    val selectedManual = manualSources.firstOrNull { it.id == selectedManualId }

    LaunchedEffect(Unit) {
        onLoadRecentWorks()
        onLoadCreationSources()
    }

    fun showCreationPlan(idea: String) {
        val cleanedIdea = idea.trim()
        if (cleanedIdea.isEmpty()) return
        inputText = cleanedIdea
        projectTitle = deriveProjectTitle(cleanedIdea)
        currentStage = 0
        maxReachedStage = 0
        creationStarted = true
        sourceMenuExpanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(creationMethod) {
        if (creationMethod != null) {
            receivedMethod = true
            editedMethod = creationMethod
            if (!creationStarted) showCreationPlan(inputText)
        } else if (receivedMethod) {
            receivedMethod = false
            editedMethod = null
            creationStarted = false
            inputText = ""
            projectTitle = ""
            selectedSource = null
            selectedManualId = null
        }
    }

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler {
        if (imeVisible) {
            focusManager.clearFocus()
            keyboardController?.hide()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_gongfang_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            CreationWorkspaceTopBar(onBack, onOpenChuangzuodangan)

            if (!creationStarted) {
                StartCreationContent(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    sourceMenuExpanded = sourceMenuExpanded,
                    onToggleSourceMenu = { sourceMenuExpanded = !sourceMenuExpanded },
                    selectedSource = selectedSource,
                    onSelectSource = {
                        when (it) {
                            "上传草图" -> sketchPicker.launch(
                                arrayOf("image/jpeg", "image/png", "image/webp")
                            )
                            "带入秘籍" -> {
                                showManualPicker = true
                                sourceMenuExpanded = false
                            }
                        }
                    },
                    sourceStatus = when {
                        derivativeSourceTitle != null -> "已选择改造来源：《$derivativeSourceTitle》"
                        uploadingSketch -> "草图上传与安全检查中…"
                        selectedSource == "上传草图" -> sketchSourceMessage
                        selectedSource == "带入秘籍" && selectedManual != null ->
                            "已带入秘籍：《${selectedManual.title}》· ${selectedManual.stateLabel}"
                        else -> sketchSourceMessage
                    },
                    derivativeSourceSelected = derivativeSourceTitle != null,
                    onClearDerivative = onClearDerivative,
                    sourceReady = when (selectedSource) {
                        "上传草图" -> sketchSourceAssetId != null && !uploadingSketch
                        "带入秘籍" -> selectedManual != null
                        else -> true
                    },
                    onStart = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onAnalyzeIntent(
                            inputText.trim(),
                            if (selectedSource == "上传草图") {
                                listOfNotNull(sketchSourceAssetId)
                            } else {
                                emptyList()
                            },
                            if (selectedSource == "带入秘籍") {
                                listOfNotNull(selectedManualId)
                            } else {
                                emptyList()
                            },
                        )
                    },
                    onContinue = { workId, _, _ -> onContinueWork(workId) },
                    onOpenAllWorks = onOpenAllWorks,
                    recentWorks = recentWorks,
                    recentWorksLoading = recentWorksLoading,
                    recentWorksMessage = recentWorksMessage,
                    onRetryRecentWorks = onLoadRecentWorks,
                    analyzingIntent = analyzingIntent,
                    analysisMessage = analysisMessage,
                )
            } else {
                StartedCreationContent(
                    title = projectTitle,
                    idea = inputText,
                    source = selectedSource,
                    method = editedMethod ?: creationMethod,
                    currentStage = currentStage,
                    maxReachedStage = maxReachedStage,
                    isSealed = isSealed,
                    onMethodChanged = { editedMethod = it },
                    onStageSelected = { currentStage = it },
                    onNextStage = {
                        if (currentStage < CreationStages.lastIndex) {
                            currentStage += 1
                            maxReachedStage = maxOf(maxReachedStage, currentStage)
                        } else {
                            isSealed = true
                        }
                    },
                    creatingProject = creatingProject,
                    createProjectMessage = createProjectMessage,
                    onConfirmNewProject = { method ->
                        onConfirmNewProject(projectTitle, inputText, method)
                    },
                )
            }
        }
    }

    if (showManualPicker) {
        ManualSourceDialog(
            items = manualSources,
            loading = manualSourcesLoading,
            message = manualSourcesMessage,
            selectedId = selectedManualId,
            onDismiss = { showManualPicker = false },
            onSelect = { item ->
                selectedManualId = item.id
                selectedSource = "带入秘籍"
                showManualPicker = false
            },
            onRetry = onLoadCreationSources,
        )
    }
}

@Composable
private fun StartCreationContent(
    inputText: String,
    onInputChange: (String) -> Unit,
    sourceMenuExpanded: Boolean,
    onToggleSourceMenu: () -> Unit,
    selectedSource: String?,
    onSelectSource: (String) -> Unit,
    sourceStatus: String?,
    derivativeSourceSelected: Boolean,
    onClearDerivative: () -> Unit,
    sourceReady: Boolean,
    onStart: () -> Unit,
    onContinue: (String, String, Int) -> Unit,
    onOpenAllWorks: () -> Unit,
    recentWorks: List<CreationResumeItem>,
    recentWorksLoading: Boolean,
    recentWorksMessage: String?,
    onRetryRecentWorks: () -> Unit,
    analyzingIntent: Boolean,
    analysisMessage: String?,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        GlassPanel(modifier = Modifier.fillMaxWidth().height(330.dp)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "你今天想做什么？",
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(17.dp))
                CreationIdeaField(
                    inputText,
                    onInputChange,
                    onToggleSourceMenu,
                    onStart,
                    analyzingIntent,
                    sourceReady,
                )
                analysisMessage?.let { message ->
                    Text(
                        message,
                        color = Color(0xFF8C4D3D),
                        fontFamily = YaHei,
                        fontSize = 11.sp,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(top = 7.dp, start = 8.dp),
                    )
                }
                AnimatedVisibility(visible = sourceMenuExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        SourceChip("上传草图", Icons.Outlined.UploadFile, selectedSource, Modifier.weight(1f), true, onSelectSource)
                        SourceChip("带入秘籍", Icons.AutoMirrored.Outlined.MenuBook, selectedSource, Modifier.weight(1f), true, onSelectSource)
                        SourceChip("同门灵感", Icons.Outlined.Groups, selectedSource, Modifier.weight(1f), false, onSelectSource)
                    }
                }
                if (!sourceMenuExpanded || sourceStatus != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            sourceStatus ?: "点“＋”可上传草图或带入已学秘籍；同门灵感将在获得许可后开放",
                            color = if (sourceStatus?.contains("失败") == true) {
                                Color(0xFF8C4D3D)
                            } else {
                                MutedInk
                            },
                            fontFamily = YaHei,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (derivativeSourceSelected) {
                            TextButton(onClick = onClearDerivative) { Text("取消改造") }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().height(118.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End,
                ) {
                    CoachBubble("我会先帮你\n理清步骤。", Modifier.width(165.dp).padding(bottom = 18.dp))
                    Image(
                        painter = painterResource(R.drawable.img_gongfang_panda),
                        contentDescription = "创作教练熊猫",
                        modifier = Modifier.size(94.dp, 140.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        ContinueCreationPanel(
            recentWorks = recentWorks,
            loading = recentWorksLoading,
            message = recentWorksMessage,
            onContinue = onContinue,
            onOpenAllWorks = onOpenAllWorks,
            onRetry = onRetryRecentWorks,
        )
    }
}

@Composable
private fun CreationIdeaField(
    value: String,
    onValueChange: (String) -> Unit,
    onToggleSourceMenu: () -> Unit,
    onStart: () -> Unit,
    analyzingIntent: Boolean,
    sourceReady: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(5.dp, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xEFFBF8EA))
            .border(1.dp, Color(0x80A8AA86), RoundedCornerShape(30.dp))
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onToggleSourceMenu),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.AddCircleOutline, "添加参考素材", tint = MutedInk, modifier = Modifier.size(30.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontFamily = YaHei, fontSize = 17.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onStart() }),
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text("说一个想法", color = Color(0xFF92958A), fontFamily = YaHei, fontSize = 17.sp)
                    }
                    inner()
                }
            },
        )
        JadeButton(
            if (analyzingIntent) "整理中…" else "开始",
            Modifier.width(82.dp),
            enabled = value.trim().length >= 2 && !analyzingIntent && sourceReady,
            onClick = onStart,
        )
    }
}

@Composable
private fun ManualSourceDialog(
    items: List<CreationManualOption>,
    loading: Boolean,
    message: String?,
    selectedId: String?,
    onDismiss: () -> Unit,
    onSelect: (CreationManualOption) -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("带入已学秘籍", color = Ink, fontFamily = YaHei, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "只显示达到“习得、悟得、传习”的秘籍，确认后会写入工法来源。",
                    color = MutedInk,
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                )
                when {
                    loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Jade,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("正在读取行囊…", color = MutedInk, fontFamily = YaHei)
                    }
                    message != null -> Text(
                        "$message，点这里重试",
                        color = Color(0xFF8C4D3D),
                        fontFamily = YaHei,
                        modifier = Modifier.clickable(onClick = onRetry),
                    )
                    items.isEmpty() -> Text(
                        "还没有可带入的秘籍。先在修炼中完成一次试炼，达到“习得”后再来。",
                        color = MutedInk,
                        fontFamily = YaHei,
                    )
                    else -> items.forEach { item ->
                        val active = item.id == selectedId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) Color(0xC8CDE8B1) else Color(0x9CFFFDF2))
                                .border(
                                    1.dp,
                                    if (active) LivingBamboo else Color(0x80A8AA86),
                                    RoundedCornerShape(14.dp),
                                )
                                .clickable { onSelect(item) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "《${item.title}》",
                                color = Ink,
                                fontFamily = YaHei,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(item.stateLabel, color = MutedInk, fontFamily = YaHei, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Text(
                "关闭",
                color = JadeDark,
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
        containerColor = Color(0xFFF8F5E8),
    )
}

@Composable
private fun SourceChip(
    label: String,
    icon: ImageVector,
    selected: String?,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: (String) -> Unit,
) {
    val active = selected == label
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(
                when {
                    active -> Color(0xC8CDE8B1)
                    enabled -> Color(0x9CFFFDF2)
                    else -> Color(0x70E3E1D6)
                }
            )
            .border(1.dp, if (active) LivingBamboo else Color(0x80A8AA86), RoundedCornerShape(21.dp))
            .clickable(enabled = enabled) { onClick(label) }
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (enabled) Ink else Color(0xFF8B8C82), modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(3.dp))
        Text(
            label,
            color = if (enabled) Ink else Color(0xFF8B8C82),
            fontFamily = YaHei,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ContinueCreationPanel(
    recentWorks: List<CreationResumeItem>,
    loading: Boolean,
    message: String?,
    onContinue: (String, String, Int) -> Unit,
    onOpenAllWorks: () -> Unit,
    onRetry: () -> Unit,
) {
    GlassPanel(modifier = Modifier.fillMaxWidth().height(190.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 13.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "继续创作",
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "全部作品  ›",
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 15.sp,
                    modifier = Modifier.height(48.dp).clickable(onClick = onOpenAllWorks).padding(top = 12.dp),
                )
            }
            when {
                recentWorks.isNotEmpty() -> {
                    recentWorks.take(2).forEach { work ->
                        ContinueWorkRow(work.title, work.stageLabel) {
                            onContinue(work.projectId, work.title, work.stageIndex)
                        }
                    }
                }
                loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(92.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Jade,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("正在载入最近作品…", color = MutedInk, fontFamily = YaHei, fontSize = 13.sp)
                    }
                }
                message != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(92.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            message,
                            color = MutedInk,
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                            maxLines = 2,
                            modifier = Modifier.weight(1f),
                        )
                        PaleButton("重试", Modifier.width(72.dp), onRetry)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(92.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("还没有进行中的作品，先说一个想法吧。", color = MutedInk, fontFamily = YaHei, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWorkRow(title: String, stage: String, onContinue: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = Color(0xFF30362F),
            fontFamily = YaHei,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(stage, color = MutedInk, fontFamily = YaHei, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
        PaleButton("继续", Modifier.width(72.dp), onContinue)
    }
}

@Composable
private fun StartedCreationContent(
    title: String,
    idea: String,
    source: String?,
    method: CreationMethodPlan?,
    currentStage: Int,
    maxReachedStage: Int,
    isSealed: Boolean,
    onMethodChanged: (CreationMethodPlan) -> Unit,
    onStageSelected: (Int) -> Unit,
    onNextStage: () -> Unit,
    creatingProject: Boolean,
    createProjectMessage: String?,
    onConfirmNewProject: (CreationMethodPlan) -> Unit,
) {
    var editingMethod by rememberSaveable { mutableStateOf(false) }
    val methodReady = method != null &&
        method.name.trim().length >= 2 &&
        method.goal.trim().length >= 2 &&
        method.format.trim().length >= 2 &&
        method.audience.isNotEmpty() &&
        method.steps.any { it.trim().isNotEmpty() }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth().height(410.dp).padding(horizontal = 28.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(21.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Eco, null, tint = Ink, modifier = Modifier.size(27.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "创作工法",
                        color = Ink,
                        fontFamily = YaHei,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(17.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xA8FFFDF2))
                        .border(1.dp, Color(0x66A8AA86), RoundedCornerShape(22.dp))
                        .padding(horizontal = 17.dp, vertical = 15.dp),
                ) {
                    Text(
                        title,
                        color = Ink,
                        fontFamily = YaHei,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    method?.let { plan ->
                        Text(
                            "推荐：${creationMediaTypeLabel(plan.recommendedMediaType)} · 使用辅助功能前会请你确认",
                            color = MutedInk,
                            fontFamily = YaHei,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (source != null) {
                        Text(
                            "已带入：$source",
                            color = MutedInk,
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(0x90DDEAC9))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (editingMethod && method != null) {
                        EditableInfoRow(Icons.Outlined.TrackChanges, "目标", method.goal) {
                            onMethodChanged(method.copy(goal = it))
                        }
                        EditableInfoRow(Icons.Outlined.ChatBubbleOutline, "形式", method.format) {
                            onMethodChanged(method.copy(format = it))
                        }
                        val stepIndex = (currentStage + 1).coerceAtMost(method.steps.lastIndex)
                        EditableInfoRow(
                            Icons.Outlined.PlayCircleOutline,
                            "下一步",
                            method.steps.getOrNull(stepIndex).orEmpty(),
                        ) { value ->
                            val revisedSteps = method.steps.toMutableList().apply {
                                if (isEmpty()) add(value) else this[stepIndex] = value
                            }
                            onMethodChanged(method.copy(steps = revisedSteps))
                        }
                    } else {
                        InfoRow(Icons.Outlined.TrackChanges, "目标", method?.goal ?: conciseGoal(idea))
                        InfoRow(Icons.Outlined.ChatBubbleOutline, "形式", method?.format ?: inferCreationFormat(idea))
                        InfoRow(
                            Icons.Outlined.PlayCircleOutline,
                            "下一步",
                            method?.steps?.getOrNull(currentStage + 1) ?: stageInstruction(currentStage),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PaleButton(
                        if (editingMethod) "完成调整" else "调整工法",
                        Modifier.weight(0.78f),
                    ) { editingMethod = !editingMethod }
                    JadeButton(
                        text = if (creatingProject) {
                            "正在创建…"
                        } else if (currentStage < CreationStages.lastIndex) {
                            "进入${CreationStages[currentStage + 1].label}"
                        } else if (isSealed) {
                            "作品已封卷"
                        } else {
                            "完成并封卷"
                        },
                        modifier = Modifier.weight(1.42f),
                        enabled = !creatingProject && !isSealed && methodReady,
                        onClick = if (currentStage == 0) {
                            { if (method != null) onConfirmNewProject(method) }
                        } else {
                            onNextStage
                        },
                    )
                }
                createProjectMessage?.let { message ->
                    Text(
                        message,
                        color = Color(0xFF8C4D3D),
                        fontFamily = YaHei,
                        fontSize = 11.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
                if (!methodReady && createProjectMessage == null) {
                    Text(
                        "请补全目标、形式和下一步后再进入草图/脚本。",
                        color = Color(0xFF8C4D3D),
                        fontFamily = YaHei,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(135.dp).padding(horizontal = 52.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoachBubble(coachMessage(currentStage), Modifier.width(170.dp))
            Image(
                painter = painterResource(R.drawable.img_gongfang_panda),
                contentDescription = "创作教练熊猫",
                modifier = Modifier.size(82.dp, 122.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(Modifier.weight(1f))
        CreationProgressChain(currentStage, maxReachedStage, onStageSelected)
    }
}

@Composable
private fun EditableInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            "$label：",
            color = Ink,
            fontFamily = YaHei,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xB8F5F2DE))
                .border(1.dp, Color(0x66739A63), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            "$label：",
            color = Ink,
            fontFamily = YaHei,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            value,
            color = Color(0xFF3F4B3E),
            fontFamily = YaHei,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CoachBubble(text: String, modifier: Modifier = Modifier) {
    HomeGuideBubble(
        text = text,
        modifier = modifier.height(78.dp),
        tailPointsRight = true,
        horizontalPadding = 24.dp,
        verticalPadding = 16.dp,
    )
}

@Composable
private fun CreationProgressChain(
    currentStage: Int,
    maxReachedStage: Int,
    onStageSelected: (Int) -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = maxReachedStage.toFloat(),
        animationSpec = tween(750),
        label = "竹节生长进度",
    )
    GlassPanel(
        modifier = Modifier.fillMaxWidth().height(112.dp).padding(horizontal = 12.dp, vertical = 5.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CreationStages.forEachIndexed { index, stage ->
                StageNode(
                    stage = stage,
                    active = index == currentStage,
                    reached = index <= maxReachedStage,
                    modifier = Modifier.width(if (index == 1) 72.dp else 58.dp),
                    onClick = { if (index <= maxReachedStage) onStageSelected(index) },
                )
                if (index < CreationStages.lastIndex) {
                    BambooProgressLine(
                        fill = (progress - index).coerceIn(0f, 1f),
                        modifier = Modifier.weight(1f).height(58.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StageNode(
    stage: CreationStage,
    active: Boolean,
    reached: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable(enabled = reached, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .shadow(if (active) 9.dp else 2.dp, CircleShape)
                .clip(CircleShape)
                .background(if (active) Color(0xE7E8F4CE) else Color(0xD9F6F2DF))
                .border(if (active) 2.dp else 1.dp, if (active) LivingBamboo else DormantBamboo, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                stage.icon,
                stage.label,
                tint = if (reached) Ink else Color(0xFF7A7B68),
                modifier = Modifier.size(29.dp),
            )
        }
        Text(
            stage.label,
            color = if (active) Ink else Color(0xFF525A4B),
            fontFamily = YaHei,
            fontSize = if (stage.label.length > 2) 12.sp else 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun BambooProgressLine(fill: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val y = 27.dp.toPx()
        val startX = -2.dp.toPx()
        val endX = size.width + 2.dp.toPx()
        drawLine(
            color = DormantBamboo,
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )
        val livingEnd = startX + (endX - startX) * fill
        if (fill > 0f) {
            drawLine(
                color = LivingBamboo,
                start = Offset(startX, y),
                end = Offset(livingEnd, y),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        listOf(0.35f, 0.72f).forEach { node ->
            val x = startX + (endX - startX) * node
            drawLine(
                color = Color(0xFFD8D5AC),
                start = Offset(x, y - 4.dp.toPx()),
                end = Offset(x, y + 4.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
            val leafGrowth = ((fill - node) / 0.2f).coerceIn(0f, 1f)
            if (leafGrowth > 0f) {
                drawBambooLeaf(Offset(x - 1.dp.toPx(), y - 2.dp.toPx()), -1f, leafGrowth)
                drawBambooLeaf(Offset(x + 1.dp.toPx(), y + 2.dp.toPx()), 1f, leafGrowth)
            }
        }
    }
}

private fun DrawScope.drawBambooLeaf(origin: Offset, direction: Float, growth: Float) {
    val length = 13.dp.toPx() * growth
    val width = 5.dp.toPx() * growth
    val tip = Offset(origin.x + direction * length, origin.y - direction * length * 0.72f)
    val path = Path().apply {
        moveTo(origin.x, origin.y)
        quadraticTo(origin.x + direction * length * 0.45f, origin.y - width, tip.x, tip.y)
        quadraticTo(
            origin.x + direction * length * 0.55f,
            origin.y + width * 0.25f,
            origin.x,
            origin.y,
        )
        close()
    }
    drawPath(path, LivingBamboo)
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Color(0xAEEFF0DB))
            .border(1.2.dp, GlassBorder, shape),
        content = content,
    )
}

@Composable
private fun JadeButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(5.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) Jade else Color(0xFF7D8C81))
            .border(1.5.dp, Color(0xFFD4D7A5), RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color(0xFFFFF9E5),
            fontFamily = YaHei,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun PaleButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(46.dp)
            .shadow(4.dp, RoundedCornerShape(23.dp))
            .clip(RoundedCornerShape(23.dp))
            .background(PaleGreen)
            .border(1.dp, Color(0xC8F9F4DD), RoundedCornerShape(23.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = JadeDark,
            fontFamily = YaHei,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private fun deriveProjectTitle(idea: String): String =
    idea.trim().replace("\n", " ").take(16).ifBlank { "新的机关创意" }

private fun inferCreationFormat(idea: String): String = when {
    idea.contains("游戏") -> "互动问答小游戏"
    idea.contains("画") || idea.contains("海报") -> "画面与图文作品"
    idea.contains("故事") || idea.contains("脚本") -> "故事脚本"
    else -> "互动机关作品"
}

private fun creationMediaTypeLabel(mediaType: String): String = when (mediaType) {
    "COMIC" -> "漫画/绘本"
    "MIXED_MEDIA" -> "综合媒介"
    else -> "图文作品"
}

private fun conciseGoal(idea: String): String =
    if (idea.isBlank()) "把灵感整理成可完成的作品" else idea.replace("\n", " ").take(22)

private fun stageInstruction(stage: Int): String = when (stage) {
    0 -> "确定玩法与表达目标"
    1 -> "整理画面、流程与脚本"
    2 -> "完成素材与交互制作"
    3 -> "试玩并修正问题"
    else -> "补充说明并完成归档"
}

private fun coachMessage(stage: Int): String = when (stage) {
    0 -> "先确定玩法，\n再开始做画面。"
    1 -> "把流程排清楚，\n制作会更顺。"
    2 -> "按草图逐步制作，\n别忘了保存。"
    3 -> "邀请同门试玩，\n记录卡住的地方。"
    else -> "补齐作品说明，\n就可以封卷啦。"
}
