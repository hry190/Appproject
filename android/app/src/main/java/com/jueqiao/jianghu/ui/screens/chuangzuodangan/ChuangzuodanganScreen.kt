package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.CreationDetailBundle
import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.components.NoRippleIndication
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class ArchiveIntroStage { Quiet, Opening, Open, Closing }

private enum class ArchiveGuideStage { AwaitingFirstTap, BubbleVisible, Ready }

private val ArchiveInk = Color(0xFF3E6847)
private val ArchiveMuted = Color(0xFF63765D)
private val ArchivePanel = Color(0xDDF4EEDB)
private val ArchivePanelSelected = Color(0xE4E5EFCB)
private val ArchivePanelBorder = Color(0x856A865D)

enum class CreationArchiveEntry { Works, OriginalRecords, VersionRecords, CoachRecords }

private data class ArchiveWork(
    val projectId: String,
    val title: String,
    val stage: String,
    val updatedAt: String,
    val description: String?,
    val versionNumber: Int?,
)

/** 每个记录入口拥有自己的花朵、雾气、熊猫、文案和关闭热区。 */
private data class ArchiveDetailSpec(
    val cloudRes: Int,
    val cloudY: Float,
    val lotusRes: Int,
    val lotusX: Float,
    val lotusY: Float,
    val lotusWidth: Float,
    val lotusHeight: Float,
    val smokeRes: Int?,
    val smokeX: Float,
    val smokeY: Float,
    val smokeWidth: Float,
    val smokeHeight: Float,
    val pandaRes: Int,
    val pandaX: Float,
    val pandaY: Float,
    val pandaWidth: Float,
    val pandaHeight: Float,
    val textX: Float,
    val textY: Float,
    val textWidth: Float,
    val textHeight: Float,
    val bubbleX: Float,
    val bubbleY: Float,
    val bubbleWidth: Float,
    val bubbleHeight: Float,
)

/** 选择作品是父级承接层，只包含自己的雾气和作品卡片，不引用三朵记录荷花。 */
private val archiveWorkPickerCloud = R.drawable.img_chuangzuodangan6_rect24

private fun archiveDetailSpec(entry: CreationArchiveEntry): ArchiveDetailSpec? = when (entry) {
    CreationArchiveEntry.Works -> null
    CreationArchiveEntry.OriginalRecords -> ArchiveDetailSpec(
        cloudRes = R.drawable.img_chuangzuodangan6_rect24,
        cloudY = 208f,
        lotusRes = R.drawable.img_chuangzuodangan6_image54,
        lotusX = -8f,
        lotusY = 233f,
        lotusWidth = 132f,
        lotusHeight = 106f,
        smokeRes = R.drawable.img_chuangzuodangan6_image57,
        smokeX = 10f,
        smokeY = 203f,
        smokeWidth = 282f,
        smokeHeight = 173f,
        pandaRes = R.drawable.img_chuangzuodangan4_image62,
        pandaX = 174f,
        pandaY = 646f,
        pandaWidth = 204f,
        pandaHeight = 219f,
        textX = 96f,
        textY = 380f,
        textWidth = 230f,
        textHeight = 110f,
        bubbleX = 250f,
        bubbleY = 540f,
        bubbleWidth = 170f,
        bubbleHeight = 104f,
    )
    CreationArchiveEntry.VersionRecords -> ArchiveDetailSpec(
        cloudRes = R.drawable.img_chuangzuodangan5_rect25,
        cloudY = 118f,
        lotusRes = R.drawable.img_chuangzuodangan5_image52,
        lotusX = 278f,
        lotusY = 365f,
        lotusWidth = 124f,
        lotusHeight = 100f,
        smokeRes = R.drawable.img_chuangzuodangan5_image59,
        smokeX = 124f,
        smokeY = 369f,
        smokeWidth = 276f,
        smokeHeight = 102f,
        pandaRes = R.drawable.img_chuangzuodangan4_image62,
        pandaX = 174f,
        pandaY = 646f,
        pandaWidth = 204f,
        pandaHeight = 219f,
        textX = 96f,
        textY = 250f,
        textWidth = 235f,
        textHeight = 120f,
        bubbleX = 24f,
        bubbleY = 540f,
        bubbleWidth = 170f,
        bubbleHeight = 104f,
    )
    CreationArchiveEntry.CoachRecords -> ArchiveDetailSpec(
        cloudRes = R.drawable.img_chuangzuodangan3_rect245,
        cloudY = 55f,
        lotusRes = R.drawable.img_chuangzuodangan3_image52,
        lotusX = 8f,
        lotusY = 556f,
        lotusWidth = 214f,
        lotusHeight = 172f,
        smokeRes = R.drawable.img_chuangzuodangan3_image61,
        smokeX = 35f,
        smokeY = 434f,
        smokeWidth = 193f,
        smokeHeight = 203f,
        pandaRes = R.drawable.img_chuangzuodangan3_image64,
        pandaX = 204f,
        pandaY = 667f,
        pandaWidth = 200f,
        pandaHeight = 222f,
        textX = 117f,
        textY = 346f,
        textWidth = 189f,
        textHeight = 90f,
        bubbleX = 24f,
        bubbleY = 560f,
        bubbleWidth = 170f,
        bubbleHeight = 104f,
    )
}

@Composable
fun ChuangzuodanganScreen(
    onBack: () -> Unit = {},
    guideSessionKey: String = "guest",
    onOpenCreationDesk: () -> Unit = {},
    onContinueWork: (String) -> Unit = {},
    onWithdrawWork: (String) -> Unit = {},
    onDeleteWork: (String) -> Unit = {},
    onAppealWork: (String, String, String) -> Unit = { _, _, _ -> },
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
    onRetryArchiveDetail: () -> Unit = {},
) {
    val context = LocalContext.current
    val guidePreferences = remember(context) {
        context.getSharedPreferences("creation_archive_guides", android.content.Context.MODE_PRIVATE)
    }
    val guidePreferenceKey = remember(guideSessionKey) { "archive_intro:$guideSessionKey" }
    var guideCompleted by rememberSaveable(guideSessionKey) {
        mutableStateOf(guidePreferences.getBoolean(guidePreferenceKey, false))
    }
    val creationTabSource = remember { MutableInteractionSource() }
    val creationTabPressed by creationTabSource.collectIsPressedAsState()
    val creationTabScale by animateFloatAsState(
        targetValue = if (creationTabPressed) .96f else 1f,
        animationSpec = tween(140),
        label = "创作台叶签按压",
    )
    var introStage by remember { mutableStateOf(ArchiveIntroStage.Quiet) }
    var guideStage by remember(guideSessionKey) {
        mutableStateOf(
            if (guideCompleted) ArchiveGuideStage.Ready else ArchiveGuideStage.AwaitingFirstTap
        )
    }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var activeEntry by remember { mutableStateOf<CreationArchiveEntry?>(null) }
    var showMoreActions by rememberSaveable { mutableStateOf(false) }
    var transitionLocked by remember { mutableStateOf(false) }
    val transitionScope = rememberCoroutineScope()
    val transitionProgress = remember { Animatable(0f) }
    val guideTapSource = remember { MutableInteractionSource() }
    val mistDrift by rememberInfiniteTransition(label = "档案云烟漂移").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "档案云烟横向漂移",
    )
    val archiveWorks = remember(recentWorks) {
        recentWorks
            .filter { it.status == "ACTIVE" }
            .distinctBy { it.id }
            .map { project ->
                ArchiveWork(
                    projectId = project.id,
                    title = project.title,
                    stage = archiveProjectStatusLabel(project),
                    updatedAt = project.updatedAt.take(10),
                    description = project.description,
                    versionNumber = project.currentVersionNumber,
                )
            }
    }
    val selectedWork = archiveWorks
        .firstOrNull { it.projectId == selectedProjectId }
        ?.let { work ->
            val appealPending = archiveDetail
                ?.takeIf { it.project.id == work.projectId }
                ?.moderationAppeals
                ?.any { it.status == "PENDING" } == true
            if (appealPending) work.copy(stage = "申诉处理中") else work
        }
    val detailSpec = activeEntry?.let(::archiveDetailSpec)
    val detailProgress = transitionProgress.value
    val flowerProgress = smoothProgress(segmentProgress(detailProgress, .02f, .48f))
    val mistProgress = smoothProgress(segmentProgress(detailProgress, .30f, .72f))
    val contentProgress = smoothProgress(segmentProgress(detailProgress, .58f, .88f))
    val pandaProgress = smoothProgress(segmentProgress(detailProgress, .42f, .66f))
    val guideBubbleProgress by animateFloatAsState(
        targetValue = if (guideStage == ArchiveGuideStage.BubbleVisible) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "档案引导气泡",
    )
    val guideTextProgress by animateFloatAsState(
        targetValue = if (guideStage == ArchiveGuideStage.Ready) 1f else 0f,
        animationSpec = tween(620, easing = FastOutSlowInEasing),
        label = "档案入口文字",
    )
    val childTextProgress = guideTextProgress * if (selectedWork == null) 0f else 1f
    // 使用详情开合进度恢复主页面内容，避免 Closing -> Quiet 时文字突然跳出。
    val baseEntryOpacity = 1f - smoothProgress(segmentProgress(detailProgress, .08f, .52f))
    val worksLabelProgress = guideTextProgress * staggerProgress(baseEntryOpacity, 0f, .42f)
    val originalLabelProgress = childTextProgress * staggerProgress(baseEntryOpacity, .12f, .58f)
    val versionLabelProgress = childTextProgress * staggerProgress(baseEntryOpacity, .28f, .76f)
    val coachLabelProgress = childTextProgress * staggerProgress(baseEntryOpacity, .44f, .94f)

    fun resetGuide() {
        guideStage = if (guideCompleted) ArchiveGuideStage.Ready else ArchiveGuideStage.AwaitingFirstTap
        selectedProjectId = null
        activeEntry = null
    }

    fun closeArchiveOrBack() {
        when (introStage) {
            ArchiveIntroStage.Open -> {
                if (transitionLocked) return
                transitionLocked = true
                introStage = ArchiveIntroStage.Closing
                transitionScope.launch {
                    transitionProgress.animateTo(0f, tween(1_900, easing = FastOutSlowInEasing))
                    activeEntry = null
                    introStage = ArchiveIntroStage.Quiet
                    transitionLocked = false
                }
            }
            ArchiveIntroStage.Quiet -> {
                resetGuide()
                onBack()
            }
            ArchiveIntroStage.Opening, ArchiveIntroStage.Closing -> Unit
        }
    }

    fun openArchiveEntry(entry: CreationArchiveEntry) {
        if (
            transitionLocked ||
            introStage != ArchiveIntroStage.Quiet ||
            guideStage != ArchiveGuideStage.Ready ||
            (entry != CreationArchiveEntry.Works && selectedWork == null)
        ) return
        activeEntry = entry
        transitionLocked = true
        introStage = ArchiveIntroStage.Opening
        transitionScope.launch {
            transitionProgress.snapTo(0f)
            transitionProgress.animateTo(1f, tween(2_800, easing = FastOutSlowInEasing))
            introStage = ArchiveIntroStage.Open
            transitionLocked = false
        }
    }

    fun chooseArchiveWork(work: ArchiveWork) {
        if (
            transitionLocked ||
            introStage != ArchiveIntroStage.Open ||
            activeEntry != CreationArchiveEntry.Works
        ) return
        selectedProjectId = work.projectId
        onArchiveWorkSelected(work.projectId)
        transitionLocked = true
        introStage = ArchiveIntroStage.Closing
        transitionScope.launch {
            transitionProgress.animateTo(0f, tween(1_300, easing = FastOutSlowInEasing))
            activeEntry = null
            introStage = ArchiveIntroStage.Quiet
            transitionLocked = false
        }
    }

    BackHandler(enabled = true) {
        if (showMoreActions) showMoreActions = false else closeArchiveOrBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ArchiveWaterBackground(Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 新账号首次轻触完成引导；之后再次进入直接可操作。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = guideTapSource,
                        indication = null,
                        enabled = introStage == ArchiveIntroStage.Quiet &&
                            guideStage != ArchiveGuideStage.Ready,
                    ) {
                        guideStage = ArchiveGuideStage.Ready
                        guideCompleted = true
                        guidePreferences.edit().putBoolean(guidePreferenceKey, true).apply()
                    },
            )

            Box(
                modifier = Modifier
                    .offset(20.dp, 41.dp)
                    .size(48.dp)
                    .clickable(onClick = ::closeArchiveOrBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset((-45).dp, 23.dp)
                    .size(160.dp, 58.dp)
                    .graphicsLayer {
                        scaleX = creationTabScale
                        scaleY = creationTabScale
                    }
                    .clickable(
                        interactionSource = creationTabSource,
                        indication = null,
                        onClick = {
                            resetGuide()
                            onOpenCreationDesk()
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_24),
                    contentDescription = "打开创作台",
                    modifier = Modifier.size(132.dp, 48.dp),
                )
                Text("创作台", color = ArchiveInk, fontFamily = YaHei, fontSize = 16.sp)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(103.dp, 23.dp)
                    .size(160.dp, 58.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_23),
                    contentDescription = "当前页面：创作档案",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    "创作档案",
                    color = ArchiveInk,
                    fontFamily = YaHei,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            ArchiveNaturalCues(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = .82f - .12f * flowerProgress },
            )

            ArchiveArcLabel(
                text = "选择作品查看",
                centerX = 45f,
                centerY = 193f,
                radius = 50f,
                startAngle = -90f,
                sweepAngle = 90f,
                fontSize = 12.sp,
                color = ArchiveInk,
                rotationOffset = 180f,
                alpha = worksLabelProgress,
            )
            ArchiveArcLabel(
                text = "原创记录",
                centerX = 61f,
                centerY = 294f,
                radius = 58f,
                startAngle = 90f,
                sweepAngle = -54f,
                fontSize = 16.sp,
                color = ArchiveInk,
                alpha = originalLabelProgress,
            )
            ArchiveArcLabel(
                text = "修改版本记录",
                centerX = 358f,
                centerY = 388f,
                radius = 64f,
                startAngle = 175f,
                sweepAngle = -90f,
                fontSize = 16.sp,
                color = ArchiveInk,
                alpha = versionLabelProgress,
            )
            ArchiveArcLabel(
                text = "创作教练记录",
                centerX = 145f,
                centerY = 650f,
                radius = 64f,
                startAngle = 160f,
                sweepAngle = -105f,
                fontSize = 16.sp,
                color = ArchiveInk,
                alpha = coachLabelProgress,
            )

            selectedWork?.let { work ->
                ArchiveCurrentWorkBadge(
                    work = work,
                    onContinue = { onContinueWork(work.projectId) },
                    onMore = { showMoreActions = true },
                    continuing = continuingProjectId == work.projectId,
                    continueMessage = continueMessage,
                    modifier = Modifier
                        // 保留左右安全边距，避免雾气牌的边缘被屏幕裁切。
                        .offset(116.dp, 94.dp)
                        .size(282.dp, 154.dp)
                        .graphicsLayer {
                            alpha = childTextProgress * staggerProgress(baseEntryOpacity, .05f, .50f)
                        }
                        .zIndex(15f),
                )
            }

            val entriesEnabled = introStage == ArchiveIntroStage.Quiet &&
                guideStage == ArchiveGuideStage.Ready
            ArchiveEntryHotspot(
                x = 0.dp,
                y = 143.dp,
                width = 120.dp,
                height = 110.dp,
                enabled = entriesEnabled,
                feedbackCenterX = 53.dp,
                feedbackCenterY = 54.dp,
                feedbackRadius = 58.dp,
                actionLabel = "选择作品",
            ) { openArchiveEntry(CreationArchiveEntry.Works) }
            ArchiveChildEntryHotspots(
                enabled = entriesEnabled && selectedWork != null,
                actionLabel = "查看原创记录",
                onClick = { openArchiveEntry(CreationArchiveEntry.OriginalRecords) },
                flowerX = 34.dp,
                flowerY = 255.dp,
                flowerWidth = 90.dp,
                flowerHeight = 92.dp,
                textX = 18.dp,
                textY = 333.dp,
                textWidth = 117.dp,
                textHeight = 96.dp,
            )
            ArchiveChildEntryHotspots(
                enabled = entriesEnabled && selectedWork != null,
                actionLabel = "查看修改版本记录",
                onClick = { openArchiveEntry(CreationArchiveEntry.VersionRecords) },
                // 右侧花苞在原画中的实际中心约为 (350dp, 402dp)。
                flowerX = 300.dp,
                flowerY = 352.dp,
                flowerWidth = 100.dp,
                flowerHeight = 100.dp,
                textX = 226.dp,
                textY = 384.dp,
                textWidth = 125.dp,
                textHeight = 130.dp,
            )
            ArchiveChildEntryHotspots(
                enabled = entriesEnabled && selectedWork != null,
                actionLabel = "查看创作教练记录",
                onClick = { openArchiveEntry(CreationArchiveEntry.CoachRecords) },
                flowerX = 80.dp,
                flowerY = 588.dp,
                flowerWidth = 130.dp,
                flowerHeight = 105.dp,
                textX = 3.dp,
                textY = 666.dp,
                textWidth = 128.dp,
                textHeight = 162.dp,
            )

            Image(
                painter = painterResource(R.drawable.img_chuangzuodangan_untitled172),
                contentDescription = "创作档案引导熊猫",
                modifier = Modifier
                    .offset(201.dp, 584.dp)
                    .size(212.dp, 245.dp)
                    .graphicsLayer { alpha = 1f - pandaProgress },
                contentScale = ContentScale.Fit,
            )

            if (activeEntry == CreationArchiveEntry.Works) {
                // 选择作品专用承接层：只有自己的雾气和后端作品卡片。
                Image(
                    painter = painterResource(archiveWorkPickerCloud),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(654.dp)
                        .offset(y = 142.dp)
                        .graphicsLayer {
                            alpha = mistProgress * .92f
                            scaleX = .94f + .06f * mistProgress
                            scaleY = .94f + .06f * mistProgress
                            translationX = (mistDrift - .5f) * 7.dp.toPx()
                        }
                        .zIndex(20f),
                    contentScale = ContentScale.FillBounds,
                )
                ArchiveWorkPickerContent(
                    works = archiveWorks,
                    selectedProjectId = selectedProjectId,
                    loading = recentWorksLoading,
                    message = recentWorksMessage,
                    onRetry = onRetryRecentWorks,
                    modifier = Modifier
                        .offset(86.dp, 344.dp)
                        .size(250.dp, 360.dp)
                        .graphicsLayer { alpha = contentProgress }
                        .zIndex(21f),
                    onSelect = ::chooseArchiveWork,
                )
                // 作品选择页保留右下角熊猫承接画面，雾气展开后再淡入，避免与列表抢层。
                Image(
                    painter = painterResource(R.drawable.img_chuangzuodangan4_image62),
                    contentDescription = "作品选择页熊猫",
                    modifier = Modifier
                        .offset(204.dp, 667.dp)
                        .size(200.dp, 222.dp)
                        .graphicsLayer {
                            alpha = contentProgress * .98f
                            val scale = .94f + .06f * contentProgress
                            scaleX = scale
                            scaleY = scale
                        }
                        .zIndex(23f),
                    contentScale = ContentScale.Fit,
                )
            }

            detailSpec?.let { spec ->
                // 固定层级：荷花先开，云雾覆盖在荷花之上，再出现烟丝与后端文字。
                Image(
                    painter = painterResource(spec.lotusRes),
                    contentDescription = "关闭当前档案记录",
                    modifier = Modifier
                        .offset(spec.lotusX.dp, spec.lotusY.dp)
                        .size(spec.lotusWidth.dp, spec.lotusHeight.dp)
                        .graphicsLayer {
                            alpha = flowerProgress
                            scaleX = .18f + .82f * flowerProgress
                            scaleY = .24f + .76f * flowerProgress
                            transformOrigin = TransformOrigin(.5f, .82f)
                            translationY = (1f - flowerProgress) * 10.dp.toPx()
                            rotationZ = -2.5f * (1f - flowerProgress)
                        }
                        .zIndex(30f),
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(spec.cloudRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = spec.cloudY.dp)
                        .graphicsLayer {
                            alpha = mistProgress * .88f
                            scaleX = .92f + .08f * mistProgress
                            scaleY = .92f + .08f * mistProgress
                            translationX = (mistDrift - .5f) * 8.dp.toPx()
                        }
                        .zIndex(31f),
                    contentScale = ContentScale.FillWidth,
                )
                spec.smokeRes?.let { smokeRes ->
                    Image(
                        painter = painterResource(smokeRes),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(
                                (spec.smokeX + mistDrift * 10f).dp,
                                (spec.smokeY + (1f - mistDrift) * 5f).dp,
                            )
                            .size(spec.smokeWidth.dp, spec.smokeHeight.dp)
                            .graphicsLayer {
                                alpha = mistProgress * .78f
                                scaleX = .94f + .06f * mistProgress
                                scaleY = .94f + .06f * mistProgress
                            }
                            .zIndex(32f),
                        contentScale = ContentScale.Fit,
                    )
                }
                ArchiveDetailContent(
                    entry = activeEntry ?: CreationArchiveEntry.OriginalRecords,
                    work = selectedWork,
                    detail = archiveDetail?.takeIf { it.project.id == selectedProjectId },
                    loading = archiveDetailLoading && archiveDetailProjectId == selectedProjectId,
                    message = archiveDetailMessage.takeIf { archiveDetailProjectId == selectedProjectId },
                    onRetry = onRetryArchiveDetail,
                    modifier = Modifier
                        .offset(spec.textX.dp, spec.textY.dp)
                        .size(spec.textWidth.dp, spec.textHeight.dp)
                        .graphicsLayer { alpha = contentProgress }
                        .zIndex(33f),
                )
                Image(
                    painter = painterResource(spec.pandaRes),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(spec.pandaX.dp, spec.pandaY.dp)
                        .size(spec.pandaWidth.dp, spec.pandaHeight.dp)
                        .graphicsLayer {
                            alpha = pandaProgress
                            translationY = (1f - pandaProgress) * 8.dp.toPx()
                        }
                        .zIndex(34f),
                    contentScale = ContentScale.Fit,
                )
                ArchiveEntryHotspot(
                    x = spec.lotusX.dp,
                    y = spec.lotusY.dp,
                    width = spec.lotusWidth.dp,
                    height = spec.lotusHeight.dp,
                    enabled = introStage == ArchiveIntroStage.Open,
                    feedbackCenterX = (spec.lotusWidth * .5f).dp,
                    feedbackCenterY = (spec.lotusHeight * .5f).dp,
                    feedbackRadius = (minOf(spec.lotusWidth, spec.lotusHeight) * .48f).dp,
                    zIndex = 36f,
                    actionLabel = "关闭当前档案记录",
                ) { closeArchiveOrBack() }
            }

            HomeGuideBubble(
                text = "点击花苞查看\n经历哦！",
                tailPointsRight = true,
                modifier = Modifier
                    .offset(46.dp, 532.dp)
                    .size(210.dp, 112.dp)
                    .graphicsLayer {
                        alpha = if (introStage == ArchiveIntroStage.Quiet) guideBubbleProgress else 0f
                        val scale = .94f + .06f * guideBubbleProgress
                        scaleX = scale
                        scaleY = scale
                    }
                    .zIndex(40f),
            )
        }
    }

    if (showMoreActions && selectedWork != null) {
        ArchiveMoreActionsSheet(
            work = selectedWork,
            detail = archiveDetail?.takeIf { it.project.id == selectedWork.projectId },
            loading = archiveDetailLoading,
            message = archiveDetailMessage,
            onDismiss = { showMoreActions = false },
            onRetry = onRetryArchiveDetail,
            onWithdraw = {
                showMoreActions = false
                onWithdrawWork(selectedWork.projectId)
            },
            onDelete = {
                showMoreActions = false
                onDeleteWork(selectedWork.projectId)
            },
            onAppeal = { caseId, reason ->
                showMoreActions = false
                onAppealWork(selectedWork.projectId, caseId, reason)
            },
        )
    }
}

@Composable
private fun BoxScope.ArchiveChildEntryHotspots(
    enabled: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
    flowerX: Dp,
    flowerY: Dp,
    flowerWidth: Dp,
    flowerHeight: Dp,
    textX: Dp,
    textY: Dp,
    textWidth: Dp,
    textHeight: Dp,
) {
    // 花苞与环绕文字共用一个连续热区，用户从任意位置按下都触发同一反馈与动作。
    val groupX = minOf(flowerX, textX)
    val groupY = minOf(flowerY, textY)
    val groupRight = maxOf(flowerX + flowerWidth, textX + textWidth)
    val groupBottom = maxOf(flowerY + flowerHeight, textY + textHeight)
    ArchiveEntryHotspot(
        x = groupX,
        y = groupY,
        width = groupRight - groupX,
        height = groupBottom - groupY,
        enabled = enabled,
        feedbackCenterX = flowerX - groupX + flowerWidth / 2f,
        feedbackCenterY = flowerY - groupY + flowerHeight / 2f,
        feedbackRadius = minOf(flowerWidth, flowerHeight) * .62f,
        actionLabel = actionLabel,
        onClick = onClick,
    )
}

@Composable
private fun ArchiveArcLabel(
    text: String,
    centerX: Float,
    centerY: Float,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    fontSize: TextUnit,
    color: Color,
    rotationOffset: Float = 0f,
    alpha: Float,
) {
    val denominator = (text.length - 1).coerceAtLeast(1).toFloat()
    text.forEachIndexed { index, char ->
        val t = index / denominator
        val angle = (startAngle + sweepAngle * t) * PI / 180.0
        val charAlpha = staggerProgress(
            value = alpha,
            start = t * .34f,
            end = .66f + t * .34f,
        )
        Text(
            text = char.toString(),
            color = color,
            style = TextStyle(
                fontFamily = YaHei,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier
                .offset(
                    x = (centerX + radius * cos(angle)).dp,
                    y = (centerY + radius * sin(angle)).dp,
                )
                .rotate(startAngle + sweepAngle * t - 90f + rotationOffset)
                .graphicsLayer {
                    this.alpha = charAlpha
                    translationY = (1f - charAlpha) * 3.dp.toPx()
                },
        )
    }
}

@Composable
private fun ArchiveDetailContent(
    entry: CreationArchiveEntry,
    work: ArchiveWork?,
    detail: CreationDetailBundle?,
    loading: Boolean,
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            loading -> ArchiveDetailMessage("正在收拢这幅作品的创作轨迹…", Modifier.fillMaxSize())
            message != null && detail == null -> ArchiveDetailError(message, onRetry)
            detail == null -> ArchiveDetailMessage("这幅作品还没有可展示的记录", Modifier.fillMaxSize())
            else -> {
                val projectTitle = detail.project.title.ifBlank { work?.title ?: "当前作品" }
                val lines = when (entry) {
                    CreationArchiveEntry.OriginalRecords -> {
                        val description = detail.project.description
                            ?: detail.method?.goal
                            ?: work?.description
                            ?: "暂无作品说明"
                        listOf(
                            projectTitle,
                            "创作目标：$description",
                            "当前阶段：${archiveProjectStatusLabel(detail.project)}",
                        )
                    }
                    CreationArchiveEntry.VersionRecords -> {
                        val versions = detail.versions
                            .sortedBy { it.versionNumber }
                            .takeLast(4)
                            .map { version ->
                                "第 ${version.versionNumber} 版 · ${version.changeSummary.ifBlank { "已保存版本" }}"
                            }
                        listOf(projectTitle) + versions.ifEmpty { listOf("暂无版本记录") }
                    }
                    CreationArchiveEntry.CoachRecords -> {
                        val calls = detail.toolCalls
                            .sortedBy { it.proposedAt }
                            .takeLast(4)
                            .map { call ->
                                "${call.proposedAt.take(16)} · ${call.effectSummary.ifBlank { call.promptSummary }}"
                            }
                        listOf(projectTitle) + calls.ifEmpty { listOf("暂无创作教练记录") }
                    }
                    CreationArchiveEntry.Works -> emptyList()
                }
                Text(
                    text = (lines + "再次点击荷花即可关闭").joinToString("\n"),
                    color = Color(0xFF263A2D),
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 21.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ArchiveDetailMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text, color = Color(0xFF4E6754), fontFamily = YaHei, fontSize = 14.sp)
    }
}

@Composable
private fun ArchiveDetailError(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = Color(0xFF8C4D3D), fontFamily = YaHei, fontSize = 13.sp)
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(40.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xCC6F963F))
                .clickable(onClick = onRetry),
            contentAlignment = Alignment.Center,
        ) {
            Text("再试一次", color = Color.White, fontFamily = YaHei, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ArchiveWorkPickerContent(
    works: List<ArchiveWork>,
    selectedProjectId: String?,
    loading: Boolean,
    message: String?,
    onRetry: () -> Unit,
    onSelect: (ArchiveWork) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("选择一幅作品", color = Color(0xFF33412F), fontFamily = YaHei, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (loading) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(ArchivePanel, RoundedCornerShape(18.dp))
                    .border(1.dp, ArchivePanelBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ArchiveInk,
                        strokeWidth = 2.dp,
                    )
                    Text("正在收集作品…", color = ArchiveMuted, fontFamily = YaHei, fontSize = 11.sp)
                }
            }
        } else if (message != null) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(ArchivePanel, RoundedCornerShape(18.dp))
                    .border(1.dp, ArchivePanelBorder, RoundedCornerShape(18.dp))
                    .clickable(onClick = onRetry),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(message, color = Color(0xFF8C4D3D), fontFamily = YaHei, fontSize = 11.sp)
                    Text("点击重试", color = ArchiveInk, fontFamily = YaHei, fontSize = 11.sp)
                }
            }
        } else if (works.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(ArchivePanel, RoundedCornerShape(18.dp))
                    .border(1.dp, ArchivePanelBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无已保存作品", color = ArchiveInk, fontFamily = YaHei, fontSize = 13.sp)
                    Text("请先在创作台保存作品", color = ArchiveMuted, fontFamily = YaHei, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(250.dp),
            ) {
                items(works, key = { it.projectId }) { work ->
                    val selected = work.projectId == selectedProjectId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Color(0x52D7E5B9) else Color.Transparent)
                            .semantics {
                                contentDescription = "选择作品：${work.title}"
                                role = Role.Button
                            }
                            .clickable(onClick = { onSelect(work) })
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    ) {
                        Column {
                            Text(work.title, color = ArchiveInk, fontFamily = YaHei, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = buildString {
                                    append(work.stage)
                                    work.versionNumber?.let { append(" · 第 $it 版") }
                                    if (work.updatedAt.isNotBlank()) append(" · ${work.updatedAt}")
                                },
                                color = ArchiveMuted,
                                fontFamily = YaHei,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                    }
                    if (work != works.last()) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 18.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x4D5D7751)),
                        )
                    }
                }
            }
        }
    }
}

/** 当前作品承接牌使用完整雾气素材，让作品信息位于雾气可视中心。 */
@Composable
private fun ArchiveCurrentWorkBadge(
    work: ArchiveWork,
    onContinue: () -> Unit,
    onMore: () -> Unit,
    continuing: Boolean,
    continueMessage: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            // 使用与详情页相同的雾气素材，当前作品承接牌不再使用气泡图形。
            painter = painterResource(R.drawable.img_chuangzuodangan_current_work_cloud),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 6.dp),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                continueMessage?.takeIf { it.isNotBlank() } ?: "当前作品",
                color = Color(0xFF5D7751),
                fontFamily = YaHei,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Text(
                work.title,
                color = Color(0xFF294F35),
                fontFamily = YaHei,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = buildString {
                    append(work.stage)
                    work.versionNumber?.let { append(" · 第 $it 版") }
                },
                color = ArchiveMuted,
                fontFamily = YaHei,
                fontSize = 10.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onContinue,
                    enabled = !continuing,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Text(if (continuing) "创建中…" else "继续创作", fontFamily = YaHei, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onMore,
                    enabled = !continuing,
                    modifier = Modifier.width(72.dp).height(48.dp),
                ) {
                    Text("更多", fontFamily = YaHei, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveMoreActionsSheet(
    work: ArchiveWork,
    detail: CreationDetailBundle?,
    loading: Boolean,
    message: String?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit,
    onAppeal: (String, String) -> Unit,
) {
    var confirmingDelete by rememberSaveable(work.projectId) { mutableStateOf(false) }
    var confirmingWithdraw by rememberSaveable(work.projectId) { mutableStateOf(false) }
    var appealReason by rememberSaveable(work.projectId) { mutableStateOf("") }
    val publication = detail?.project?.latestPublication
    val moderationCase = detail?.moderationCase
    val latestAppeal = detail?.moderationAppeals?.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF9F5E8),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = work.title,
                color = ArchiveInk,
                fontFamily = YaHei,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "这里只放不常用的作品管理操作。",
                color = ArchiveMuted,
                fontFamily = YaHei,
                fontSize = 12.sp,
            )
            when {
                loading -> Row(
                    modifier = Modifier.height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("正在确认可用操作…", fontFamily = YaHei, fontSize = 13.sp)
                }
                detail == null -> OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(message ?: "重新加载作品状态", fontFamily = YaHei)
                }
                else -> {
                    message?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = ArchiveInk, fontFamily = YaHei, fontSize = 12.sp)
                    }
                    if (publication?.status == "PUBLISHED") {
                        if (!confirmingWithdraw) {
                            OutlinedButton(
                                onClick = { confirmingWithdraw = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                Text("撤回已发布作品", fontFamily = YaHei)
                            }
                        } else {
                            Text(
                                "撤回后将从班级或大会中移除，作品和版本仍会保留。",
                                color = Color(0xFF8C3F35),
                                fontFamily = YaHei,
                                fontSize = 12.sp,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { confirmingWithdraw = false },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                ) { Text("取消", fontFamily = YaHei) }
                                Button(
                                    onClick = onWithdraw,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                ) { Text("确认撤回", fontFamily = YaHei) }
                            }
                        }
                    }
                    if (latestAppeal?.status == "PENDING") {
                        Text(
                            "申诉已提交，正在等待复核。无需重复提交。",
                            color = ArchiveInk,
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                        )
                    } else if (moderationCase?.canAppeal == true) {
                        TextField(
                            value = appealReason,
                            onValueChange = { appealReason = it.take(500) },
                            label = { Text("申诉说明", fontFamily = YaHei) },
                            supportingText = { Text("请说明希望复核的原因", fontFamily = YaHei) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedButton(
                            onClick = { onAppeal(moderationCase.id, appealReason.trim()) },
                            enabled = appealReason.trim().length >= 10,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text("提交申诉", fontFamily = YaHei)
                        }
                    }
                    if (!confirmingDelete && !confirmingWithdraw) {
                        OutlinedButton(
                            onClick = { confirmingDelete = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text("删除作品", fontFamily = YaHei, color = Color(0xFF8C3F35))
                        }
                    } else {
                        Text(
                            "删除后作品与已发布内容都会撤下，且不能恢复。",
                            color = Color(0xFF8C3F35),
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { confirmingDelete = false },
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) { Text("取消", fontFamily = YaHei) }
                            Button(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) { Text("确认删除", fontFamily = YaHei) }
                        }
                    }
                    if (
                        publication?.status != "PUBLISHED" &&
                        moderationCase?.canAppeal != true && latestAppeal == null &&
                        !confirmingDelete
                    ) {
                        Text(
                            "当前没有可撤回或可申诉的提交。",
                            color = ArchiveMuted,
                            fontFamily = YaHei,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ArchiveEntryHotspot(
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    enabled: Boolean,
    feedbackCenterX: Dp,
    feedbackCenterY: Dp,
    feedbackRadius: Dp,
    zIndex: Float = 10f,
    actionLabel: String,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (pressed) 110 else 280,
            easing = FastOutSlowInEasing,
        ),
        label = "档案入口自然按压反馈",
    )
    Box(
        modifier = Modifier
            .offset(x, y)
            .size(width, height)
            .zIndex(zIndex)
            .semantics {
                contentDescription = actionLabel
                role = Role.Button
            }
            .drawBehind {
                if (pressProgress > 0f) {
                    val center = Offset(feedbackCenterX.toPx(), feedbackCenterY.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFF6C7).copy(alpha = .30f * pressProgress),
                                Color(0xFFDCE9AE).copy(alpha = .18f * pressProgress),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = feedbackRadius.toPx(),
                        ),
                        center = center,
                        radius = feedbackRadius.toPx(),
                    )
                }
            }
            .clickable(
                interactionSource = source,
                indication = NoRippleIndication,
                enabled = enabled,
                onClick = onClick,
            ),
    )
}

private fun archiveProjectStatusLabel(project: CreationProjectDto): String {
    val status = project.displayStatus.ifBlank {
        project.latestPublication?.status ?: project.currentStage
    }
    return when (status) {
        "IDEATION" -> "构思阶段"
        "DRAFT" -> "草图阶段"
        "PRODUCTION" -> "制作阶段"
        "TEST" -> "测试阶段"
        "SEAL" -> "封卷阶段"
        "PENDING_CHECK", "PENDING_REVIEW", "SUBMITTED" -> "检查中"
        "PENDING_HUMAN_REVIEW" -> "老师检查中"
        "PUBLISHED" -> "已发布"
        "RETURNED" -> "需要修改"
        "RESTRICTED" -> "暂时不能发布"
        "WITHDRAWN" -> "已撤回"
        "REJECTED" -> "未通过检查"
        "ACTIVE" -> workflowStageLabelForArchive(project.currentStage)
        else -> workflowStageLabelForArchive(project.currentStage)
    }
}

private fun workflowStageLabelForArchive(stage: String): String = when (stage) {
    "IDEATION" -> "构思阶段"
    "DRAFT" -> "草图阶段"
    "PRODUCTION" -> "制作阶段"
    "TEST" -> "测试阶段"
    "SEAL" -> "封卷阶段"
    else -> "创作中"
}

@Composable
private fun ArchiveNaturalCues(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = 1.dp.toPx()
        listOf(
            Offset(66f * unit, 335f * unit),
            Offset(82f * unit, 341f * unit),
            Offset(94f * unit, 347f * unit),
        ).forEach { center ->
            drawCircle(Color(0xFFDFF2D2).copy(alpha = .42f), 5f * unit, center)
            drawCircle(Color.White.copy(alpha = .62f), 1.25f * unit, Offset(center.x - 1.4f * unit, center.y - 1.8f * unit))
        }
        val body = Offset(327f * unit, 344f * unit)
        drawLine(Color(0xFF745A35).copy(alpha = .48f), Offset(body.x, body.y - 10f * unit), Offset(body.x, body.y + 10f * unit), 1.2f * unit)
        drawCircle(Color(0xFF8B6D3E).copy(alpha = .52f), 2f * unit, body)
    }
}

private fun segmentProgress(value: Float, start: Float, end: Float): Float =
    if (end <= start) 1f else ((value - start) / (end - start)).coerceIn(0f, 1f)

private fun staggerProgress(value: Float, start: Float, end: Float): Float =
    smoothProgress(segmentProgress(value, start, end))

private fun smoothProgress(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}
