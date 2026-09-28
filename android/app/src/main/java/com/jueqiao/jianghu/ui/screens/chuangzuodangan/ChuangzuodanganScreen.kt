package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.CreationConversationDto
import com.jueqiao.jianghu.luggage.CreationDetailBundle
import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.ui.components.CreationSectionHeader
import com.jueqiao.jianghu.ui.theme.YaHei

private val ArchiveInk = Color(0xFF3E6847)
private val ArchiveMuted = Color(0xFF63765D)
private val ArchivePaper = Color(0xFFF9F5E8)
private val ArchiveError = Color(0xFF8C3F35)

private data class ArchiveLotus(
    val category: ArchiveCategory,
    val x: Int, val y: Int, val width: Int, val height: Int,
    val openImage: Int,
    val labelX: Int, val labelY: Int, val labelWidth: Int,
)

private val ArchiveLotuses = listOf(
    ArchiveLotus(ArchiveCategory.Graphic, 18, 255, 120, 156,
        R.drawable.img_chuangzuodangan6_image54, 28, 340, 104),
    ArchiveLotus(ArchiveCategory.Video, 264, 352, 136, 140,
        R.drawable.img_chuangzuodangan5_image52, 273, 415, 104),
    ArchiveLotus(ArchiveCategory.Game, 3, 588, 207, 205,
        R.drawable.img_chuangzuodangan3_image52, 52, 695, 156),
)

@Composable
fun ChuangzuodanganScreen(
    onBack: () -> Unit = {},
    guideSessionKey: String = "guest",
    onContinueWork: (String) -> Unit = {},
    onPublishWork: (String) -> Unit = {},
    onDeleteWork: (String) -> Unit = {},
    recentWorks: List<CreationProjectDto> = emptyList(),
    recentWorksLoading: Boolean = false,
    recentWorksMessage: String? = null,
    continuingProjectId: String? = null,
    continueMessage: String? = null,
    onRetryRecentWorks: () -> Unit = {},
    archiveDetail: CreationDetailBundle? = null,
    archiveDetailProjectId: String? = null,
    archiveDetailLoading: Boolean = false,
    archiveDetailMessage: String? = null,
    onArchiveWorkSelected: (String) -> Unit = {},
) {
    var categoryName by rememberSaveable(guideSessionKey) { mutableStateOf<String?>(null) }
    var selectedProjectId by rememberSaveable(guideSessionKey) { mutableStateOf<String?>(null) }
    var showMore by rememberSaveable(guideSessionKey) { mutableStateOf(false) }
    var showHistory by rememberSaveable(guideSessionKey) { mutableStateOf(false) }
    var continueAttemptId by rememberSaveable(guideSessionKey) { mutableStateOf<String?>(null) }
    var localMessage by rememberSaveable(guideSessionKey) { mutableStateOf<String?>(null) }
    val category = ArchiveCategory.entries.firstOrNull { it.name == categoryName }
    val works = category?.projects(recentWorks).orEmpty()
    val selectedWork = works.firstOrNull { it.id == selectedProjectId }
    val detail = archiveDetail?.takeIf { it.project.id == selectedWork?.id }
    val detailLoading = archiveDetailLoading && archiveDetailProjectId == selectedWork?.id
    val detailMessage = archiveDetailMessage.takeIf { archiveDetailProjectId == selectedWork?.id }
    val listState = rememberLazyListState()
    val openProgress by animateFloatAsState(
        targetValue = if (category == null) 0f else 1f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "荷花与云雾展开",
    )

    fun clearSelection() {
        selectedProjectId = null
        showMore = false
        showHistory = false
        continueAttemptId = null
        localMessage = null
    }

    fun back() {
        if (continuingProjectId != null && continuingProjectId == selectedProjectId) return
        when {
            showHistory -> { showHistory = false; showMore = true }
            showMore -> showMore = false
            selectedProjectId != null -> clearSelection()
            category != null -> { clearSelection(); categoryName = null }
            else -> onBack()
        }
    }

    // Restore the exact selected work after returning from the editor or publication page.
    LaunchedEffect(selectedProjectId) {
        selectedProjectId?.let(onArchiveWorkSelected)
    }
    LaunchedEffect(categoryName) { listState.scrollToItem(0) }
    LaunchedEffect(selectedProjectId, selectedWork?.id, recentWorksLoading) {
        if (selectedProjectId != null && selectedWork == null && !recentWorksLoading) clearSelection()
    }
    BackHandler(onBack = ::back)

    Box(Modifier.fillMaxSize()) {
        ArchiveWaterBackground(Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            Image(
                painter = painterResource(R.drawable.img_chuangzuodangan_untitled172),
                contentDescription = null,
                modifier = Modifier.offset(201.dp, 584.dp).size(212.dp, 245.dp),
                contentScale = ContentScale.Fit,
            )
            // All three labels are composed on the very first frame, with identical visibility.
            // The former global work-picker leaf has no text, hit target or guide gate.
            if (category == null) {
                ArchiveLotuses.forEach { lotus ->
                    ArchiveLotusLabel(lotus)
                    Box(
                        Modifier.offset(lotus.x.dp, lotus.y.dp).size(lotus.width.dp, lotus.height.dp)
                            .semantics { contentDescription = "浏览${lotus.category.label}作品" }
                            .clickable(role = Role.Button) {
                                clearSelection()
                                categoryName = lotus.category.name
                            },
                    )
                }
            } else {
                val lotus = ArchiveLotuses.first { it.category == category }
                Image(
                    painter = painterResource(lotus.openImage),
                    contentDescription = null,
                    modifier = Modifier.offset(lotus.x.dp, lotus.y.dp)
                        .size(lotus.width.dp, (lotus.width * .8f).dp)
                        .graphicsLayer {
                            alpha = openProgress
                            scaleX = .25f + .75f * openProgress
                            scaleY = .25f + .75f * openProgress
                            transformOrigin = TransformOrigin(.5f, .85f)
                        },
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(R.drawable.img_chuangzuodangan6_rect24),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(top = 118.dp, bottom = 105.dp)
                        .graphicsLayer { alpha = openProgress * .96f },
                    contentScale = ContentScale.FillBounds,
                )
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 42.dp).padding(top = 180.dp, bottom = 160.dp)
                        .graphicsLayer { alpha = openProgress }
                        .background(Color(0x5CF5F2DF), RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (selectedWork == null) {
                        ArchiveWorkPickerContent(
                            category = category,
                            works = works,
                            loading = recentWorksLoading,
                            message = recentWorksMessage,
                            listState = listState,
                            onRetry = onRetryRecentWorks,
                            onBack = { clearSelection(); categoryName = null },
                            onSelect = { work ->
                                clearSelection()
                                selectedProjectId = work.id
                            },
                        )
                    } else {
                        ArchiveCurrentWorkContent(
                            work = detail?.project ?: selectedWork,
                            category = category,
                            continuing = continuingProjectId == selectedWork.id,
                            enabled = !detailLoading && continuingProjectId == null,
                            message = if (detailLoading) "正在读取作品信息…" else
                                localMessage ?: continueMessage.takeIf { continueAttemptId == selectedWork.id },
                            onBack = ::clearSelection,
                            onContinue = {
                                if (category == ArchiveCategory.Graphic) {
                                    continueAttemptId = selectedWork.id
                                    onContinueWork(selectedWork.id)
                                } else {
                                    // The matching editors are not released. Never route these to the image editor.
                                    localMessage = "${category.label}创作功能尚未上线"
                                }
                            },
                            onMore = { showMore = true },
                        )
                    }
                }
            }
            CreationSectionHeader(
                title = "创作档案", onBack = ::back,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    if (showMore && selectedWork != null) {
        ArchiveMoreActionsSheet(
            work = detail?.project ?: selectedWork,
            detail = detail,
            loading = detailLoading,
            message = detailMessage,
            onDismiss = { showMore = false },
            onRetry = { onArchiveWorkSelected(selectedWork.id) },
            onPublish = { showMore = false; onPublishWork(selectedWork.id) },
            onDelete = { onDeleteWork(selectedWork.id) },
            onHistory = { showMore = false; showHistory = true },
        )
    }
    if (showHistory && selectedWork != null) {
        ArchiveHistorySheet(
            work = selectedWork,
            conversation = detail?.conversation?.takeIf { it.project.id == selectedWork.id },
            loading = detailLoading,
            message = detailMessage,
            onRetry = { onArchiveWorkSelected(selectedWork.id) },
            onDismiss = { showHistory = false; showMore = true },
        )
    }
}

@Composable
private fun ArchiveLotusLabel(lotus: ArchiveLotus) {
    Box(
        modifier = Modifier
            .offset(lotus.labelX.dp, lotus.labelY.dp)
            .size(lotus.labelWidth.dp, 52.dp)
            .drawBehind {
                // A feathered paper wash quiets the leaf veins without a hard badge edge.
                scale(scaleX = size.width / size.height, scaleY = 1f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to ArchivePaper.copy(alpha = 0.96f),
                            0.58f to ArchivePaper.copy(alpha = 0.91f),
                            1f to ArchivePaper.copy(alpha = 0f),
                            center = center,
                            radius = size.height / 2,
                        ),
                        radius = size.height / 2,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = lotus.category.label,
            color = Color(0xFF203D2B),
            fontFamily = YaHei,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = TextStyle(
                shadow = Shadow(ArchivePaper, Offset.Zero, blurRadius = 5f),
            ),
        )
    }
}

@Composable
private fun ArchiveWorkPickerContent(
    category: ArchiveCategory,
    works: List<CreationProjectDto>,
    loading: Boolean,
    message: String?,
    listState: LazyListState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onSelect: (CreationProjectDto) -> Unit,
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(category.pickerTitle, color = ArchiveInk, fontFamily = YaHei,
            fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Box(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp)) {
            when {
                loading -> ArchiveLoading("正在收集作品…")
                message != null -> ArchiveNotice(message, onRetry)
                works.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无相关作品", color = ArchiveMuted, fontFamily = YaHei, fontSize = 16.sp)
                }
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(works, key = { it.id }) { work ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .semantics { contentDescription = "选择作品：${work.title}" }
                                .clickable(role = Role.Button, onClick = { onSelect(work) })
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(work.title, color = ArchiveInk, fontFamily = YaHei,
                                fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(archiveWorkMetadata(work), color = ArchiveMuted,
                                fontFamily = YaHei, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                        HorizontalDivider(color = ArchiveInk.copy(alpha = .22f))
                    }
                }
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("返回荷塘", color = ArchiveInk, fontFamily = YaHei)
        }
    }
}

private fun archiveWorkMetadata(work: CreationProjectDto): String = buildList {
    work.latestPublication?.status?.let { add(archivePublicationLabel(it)) }
    work.currentVersionNumber?.let { add("第 $it 版") }
    work.updatedAt.takeIf { it.isNotBlank() }?.let { add(it.take(10)) }
}.joinToString(" · ")

@Composable
private fun ArchiveCurrentWorkContent(
    work: CreationProjectDto,
    category: ArchiveCategory,
    continuing: Boolean,
    enabled: Boolean,
    message: String?,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onMore: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("当前${category.label}作品", color = ArchiveMuted, fontFamily = YaHei, fontSize = 12.sp)
        Text(work.title, color = ArchiveInk, fontFamily = YaHei, fontWeight = FontWeight.Bold,
            fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
        Text(archiveWorkMetadata(work), color = ArchiveMuted, fontFamily = YaHei,
            fontSize = 12.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onContinue, enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8A759), contentColor = Color(0xFF433714)),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) { Text(if (continuing) "恢复中…" else "继续创作", fontFamily = YaHei, fontSize = 14.sp) }
            OutlinedButton(
                onClick = onMore, enabled = enabled,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) { Text("查看更多", fontFamily = YaHei, fontSize = 14.sp, color = ArchiveInk) }
        }
        message?.let {
            Text(it, color = ArchiveInk, fontFamily = YaHei, fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp), textAlign = TextAlign.Center)
        }
        TextButton(onClick = onBack, enabled = !continuing,
            modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
            Text("返回${category.label}作品列表", color = ArchiveInk, fontFamily = YaHei)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveMoreActionsSheet(
    work: CreationProjectDto,
    detail: CreationDetailBundle?,
    loading: Boolean,
    message: String?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onHistory: () -> Unit,
) {
    var confirmingDelete by rememberSaveable(work.id) { mutableStateOf(false) }
    val currentVersionId = detail?.versions?.firstOrNull {
        it.versionNumber == work.currentVersionNumber
    }?.id
    val canPublish = detail != null && archiveCanPublish(work, currentVersionId)
    val publication = work.latestPublication
    ModalBottomSheet(
        onDismissRequest = onDismiss, containerColor = ArchivePaper,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(work.title, color = ArchiveInk, fontFamily = YaHei, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = ArchiveInk, strokeWidth = 2.dp)
                    Text("正在处理，请稍候…", color = ArchiveMuted, fontFamily = YaHei)
                }
            }
            message?.let { Text(it, color = ArchiveError, fontFamily = YaHei, fontSize = 13.sp) }
            if (detail == null && !loading) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("重新加载作品状态", fontFamily = YaHei)
                }
            }
            Button(
                onClick = onPublish, enabled = canPublish && !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(if (!canPublish && publication != null) archivePublicationLabel(publication.status)
                    else "发布到大会", fontFamily = YaHei)
            }
            if (detail != null && publication == null && !canPublish) {
                Text("作品完成并保存后，可发布到大会", color = ArchiveMuted, fontFamily = YaHei, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onHistory, enabled = !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("查看历史长对话", color = ArchiveInk, fontFamily = YaHei)
            }
            OutlinedButton(onClick = { confirmingDelete = true }, enabled = !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("删除作品", color = ArchiveError, fontFamily = YaHei)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("返回作品", color = ArchiveInk, fontFamily = YaHei)
            }
        }
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false }, containerColor = ArchivePaper,
            title = { Text("删除作品？", fontFamily = YaHei, color = ArchiveInk) },
            text = { Text("确认删除「${work.title}」？删除后无法恢复，已发布内容也会撤下。", fontFamily = YaHei) },
            confirmButton = {
                TextButton(onClick = { confirmingDelete = false; onDelete() }, enabled = !loading) {
                    Text("确认删除", color = ArchiveError, fontFamily = YaHei)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("取消", fontFamily = YaHei) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveHistorySheet(
    work: CreationProjectDto,
    conversation: CreationConversationDto?,
    loading: Boolean,
    message: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, containerColor = ArchivePaper,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.88f).padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)) {
            Text("历史长对话", color = ArchiveInk, fontFamily = YaHei, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(work.title, color = ArchiveMuted, fontFamily = YaHei, modifier = Modifier.padding(vertical = 10.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loading -> ArchiveLoading("正在读取历史对话…")
                    conversation == null -> ArchiveNotice(message ?: "暂无历史对话记录", onRetry)
                    else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (conversation.initialIdea.isNotBlank()) {
                            item(key = "initial-idea") {
                                ArchiveConversationText("最初的创作想法", conversation.initialIdea, conversation.startedAt)
                            }
                        }
                        if (conversation.attachmentNames.isNotEmpty() || conversation.manualTitles.isNotEmpty()) {
                            item(key = "context") {
                                ArchiveConversationText("创作参考", buildList {
                                    if (conversation.attachmentNames.isNotEmpty()) add("带入内容：${conversation.attachmentNames.joinToString("、")}")
                                    if (conversation.manualTitles.isNotEmpty()) add("参考秘籍：${conversation.manualTitles.joinToString("、")}")
                                }.joinToString("\n"), "")
                            }
                        }
                        items(conversation.messages.filter { it.projectId == work.id }, key = { it.id }) { item ->
                            val speaker = when (item.role) {
                                "STUDENT" -> "我"
                                "COACH" -> when (item.decision) {
                                    "ACCEPTED" -> "创作教练 · 已采纳"
                                    "REPLACED" -> "创作教练 · 已调整"
                                    else -> "创作教练"
                                }
                                else -> "创作记录"
                            }
                            ArchiveConversationText(speaker, item.content, item.createdAt)
                        }
                        item(key = "history-end") {
                            Text("已展示全部历史对话", color = ArchiveMuted, fontFamily = YaHei,
                                fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("返回作品操作", color = ArchiveInk, fontFamily = YaHei)
            }
        }
    }
}

@Composable
private fun ArchiveConversationText(speaker: String, content: String, timestamp: String) {
    Column(Modifier.fillMaxWidth().background(Color(0x667D995E), RoundedCornerShape(16.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(speaker, color = ArchiveInk, fontFamily = YaHei, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        SelectionContainer {
            Text(content, color = Color(0xFF293D2D), fontFamily = YaHei, fontSize = 15.sp, lineHeight = 23.sp)
        }
        if (timestamp.isNotBlank()) Text(timestamp.take(19).replace('T', ' '), color = ArchiveMuted,
            fontFamily = YaHei, fontSize = 11.sp)
    }
}

@Composable
private fun ArchiveLoading(message: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(Modifier.size(24.dp), color = ArchiveInk, strokeWidth = 2.dp)
        Text(message, color = ArchiveMuted, fontFamily = YaHei, fontSize = 13.sp,
            modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ArchiveNotice(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(message, color = ArchiveMuted, fontFamily = YaHei, fontSize = 14.sp, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("重新加载", color = ArchiveInk, fontFamily = YaHei)
        }
    }
}
