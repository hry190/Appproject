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

/**
 * 作品创作入口。这里只收集最初想法和参考内容，开始后直接进入教练对话。
 * 背景、熊猫、顶部叶签与气泡均直接复用原资源。
 */
@Composable
fun GongfangScreen(
    onBack: () -> Unit = {},
    onStartConversation: (String, List<String>, List<String>) -> Unit = { _, _, _ -> },
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
    startingConversation: Boolean = false,
    analysisMessage: String? = null,
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedManualId by rememberSaveable { mutableStateOf<String?>(null) }
    var showManualPicker by rememberSaveable { mutableStateOf(false) }
    var sourceMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sketchPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            selectedSource = "上传草图"
            sourceMenuExpanded = false
            onUploadSketch(uri.toString())
        }
    }
    val selectedManual = manualSources.firstOrNull { it.id == selectedManualId }

    LaunchedEffect(Unit) {
        onLoadRecentWorks()
        onLoadCreationSources()
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
                        sketchSourceAssetId != null && selectedManual != null ->
                            "已带入草图和秘籍《${selectedManual.title}》"
                        sketchSourceAssetId != null -> sketchSourceMessage
                        selectedManual != null -> "已带入秘籍：《${selectedManual.title}》"
                        else -> sketchSourceMessage
                    },
                    derivativeSourceSelected = derivativeSourceTitle != null,
                    onClearDerivative = onClearDerivative,
                    sourceReady = !uploadingSketch &&
                        (selectedSource != "上传草图" || sketchSourceAssetId != null),
                    onStart = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onStartConversation(
                            inputText.trim(),
                            listOfNotNull(sketchSourceAssetId),
                            listOfNotNull(selectedManualId),
                        )
                    },
                    onContinue = { workId, _, _ -> onContinueWork(workId) },
                    onOpenAllWorks = onOpenAllWorks,
                    recentWorks = recentWorks,
                    recentWorksLoading = recentWorksLoading,
                    recentWorksMessage = recentWorksMessage,
                    onRetryRecentWorks = onLoadRecentWorks,
                    analyzingIntent = startingConversation,
                    analysisMessage = analysisMessage,
                )
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
                    CoachBubble("我会先听懂你的想法，\n再和你一起商量。", Modifier.width(165.dp).padding(bottom = 18.dp))
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
            if (analyzingIntent) "正在进入…" else "开始",
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
                    "只显示你已经学会的秘籍，选中后教练会在交流时一起参考。",
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
