package com.jueqiao.jianghu.ui.screens.wushuhuan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningBookDto
import com.jueqiao.jianghu.luggage.ManualDetailBundle
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val PaperLight = Color(0xFFD9D0BB)
private val PaperBase = Color(0xFFC9C1AE)
private val PaperShade = Color(0xFF9F9788)
private val PaperCrease = Color(0xFF817B70)
private val PaperSpine = Color(0xFF4B4943)
private val PaperEdge = Color(0xFF343634)
private val Ink = Color(0xFF292A25)
private val MutedInk = Color(0xFF59594F)
private val JadeInk = Color(0xFF3E5C53)
private val Cinnabar = Color(0xFF87372F)
private val CoverGreen = Color(0xFF123D3E)
private val CoverShadow = Color(0xFF202522)
private val OldGold = Color(0xFFC2AA76)

private val AncientKai = FontFamily(
    Font(R.font.lxgw_wenkai_lite_regular, weight = FontWeight.Normal),
)

private data class BookPageKey(
    val lessonPageNo: Int? = null,
    val leafIndex: Int = 0,
)

private data class CatalogEntry(
    val pageNo: Int,
    val title: String,
    val coreLogic: String,
    val book: LearningBookDto?,
)

/**
 * A single-page codex overlay. The page curl is drawn as one continuous Bézier surface, never as
 * individually clipped strips, so sub-pixel gaps cannot expose white lines during a turn.
 */
@Composable
internal fun AncientManualBookOverlay(
    selectedBook: LearningBookDto,
    volumeBooks: List<LearningBookDto>,
    detail: ManualDetailBundle?,
    loading: Boolean,
    message: String?,
    onLoadManualDetail: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenReader: (LearningBookDto) -> Unit,
    onOpenTrial: (String) -> Unit,
    onUseInCreation: (String) -> Unit,
) {
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val scope = rememberCoroutineScope()
    val volumeNo = selectedBook.volumeNo
    val rawVolumeTitle = selectedBook.volumeTitle
        .removePrefix("卷${chineseNumber(volumeNo)}")
        .trim()
        .ifBlank { selectedBook.title }
    val volumeTitle = if (rawVolumeTitle.startsWith("《")) {
        rawVolumeTitle
    } else {
        "《$rawVolumeTitle》"
    }
    val catalog = remember(volumeNo, volumeBooks) {
        if (volumeNo == 1) {
            volumeOneLessons.map { lesson ->
                CatalogEntry(
                    pageNo = lesson.pageNo,
                    title = lesson.title,
                    coreLogic = lesson.coreLogic,
                    book = volumeBooks.firstOrNull { it.pageNo == lesson.pageNo },
                )
            }
        } else {
            volumeBooks.sortedBy { it.pageNo }.map { book ->
                CatalogEntry(book.pageNo, book.title, "查看本招已审核内容", book)
            }
        }
    }

    var currentLessonPageNo by rememberSaveable(volumeNo) { mutableStateOf<Int?>(null) }
    var currentLeafIndex by rememberSaveable(volumeNo) { mutableIntStateOf(0) }
    var pendingPage by remember { mutableStateOf<BookPageKey?>(null) }
    var predictionChoice by rememberSaveable(volumeNo) { mutableStateOf<String?>(null) }
    var predictionSubmitted by rememberSaveable(volumeNo) { mutableStateOf(false) }
    var turnForward by remember { mutableStateOf(true) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var pageWidthPx by remember { mutableFloatStateOf(1f) }
    var turnJob by remember { mutableStateOf<Job?>(null) }
    val turnProgress = remember { Animatable(0f) }
    val openProgress = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    val currentPage = BookPageKey(currentLessonPageNo, currentLeafIndex)
    val currentBook = currentLessonPageNo?.let { pageNo ->
        volumeBooks.firstOrNull { it.pageNo == pageNo }
    }
    val lessonContent = currentLessonPageNo?.let(::volumeOneLesson)
    val localLeaves = lessonContent?.leaves.orEmpty()
    val readableCount = currentBook?.let { readableLeafCount(it.state, localLeaves.size) } ?: 0
    val genericLeafCount = if (currentBook != null) 1 else 0
    val visibleLeafCount = if (volumeNo == 1) readableCount else genericLeafCount

    LaunchedEffect(Unit) {
        if (animationsEnabled) {
            openProgress.animateTo(1f, tween(620, easing = FastOutSlowInEasing))
        } else {
            openProgress.snapTo(1f)
        }
    }

    LaunchedEffect(currentBook?.manualPageId) {
        currentBook?.manualPageId?.let(onLoadManualDetail)
    }

    fun previousPageFor(page: BookPageKey): BookPageKey? = when {
        page.lessonPageNo == null -> null
        page.leafIndex > 0 -> page.copy(leafIndex = page.leafIndex - 1)
        // 第一叶向右拖只回弹；目录只能经由现有返回按钮或系统返回进入。
        else -> null
    }

    fun nextPageFor(page: BookPageKey): BookPageKey? = when {
        page.lessonPageNo == null -> catalog.firstOrNull { it.book != null }?.let {
            BookPageKey(lessonPageNo = it.pageNo, leafIndex = 0)
        }
        page.lessonPageNo == 1 && page.leafIndex == 0 && !predictionSubmitted -> null
        page.leafIndex + 1 < visibleLeafCount -> page.copy(leafIndex = page.leafIndex + 1)
        else -> null
    }

    fun commitPage(target: BookPageKey) {
        currentLessonPageNo = target.lessonPageNo
        currentLeafIndex = target.leafIndex
        pendingPage = null
        dragDistance = 0f
    }

    fun animateToPage(target: BookPageKey, forward: Boolean, start: Float = 0f) {
        if (turnJob?.isActive == true || target == currentPage) return
        if (!animationsEnabled) {
            commitPage(target)
            return
        }
        turnJob = scope.launch {
            try {
                pendingPage = target
                turnForward = forward
                turnProgress.snapTo(start.coerceIn(0f, 0.96f))
                turnProgress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                commitPage(target)
                turnProgress.snapTo(0f)
            } finally {
                turnJob = null
            }
        }
    }

    fun cancelTurn(start: Float, target: BookPageKey, forward: Boolean) {
        if (!animationsEnabled) {
            pendingPage = null
            dragDistance = 0f
            return
        }
        turnJob = scope.launch {
            try {
                pendingPage = target
                turnForward = forward
                turnProgress.snapTo(start.coerceIn(0f, 0.96f))
                turnProgress.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                pendingPage = null
                dragDistance = 0f
            } finally {
                turnJob = null
            }
        }
    }

    fun returnOrDismiss() {
        if (currentLessonPageNo != null || pendingPage?.lessonPageNo != null) {
            turnJob?.cancel()
            turnJob = null
            pendingPage = null
            dragDistance = 0f
            currentLessonPageNo = null
            currentLeafIndex = 0
            scope.launch { turnProgress.snapTo(0f) }
            return
        }
        onDismiss()
    }

    BackHandler(onBack = ::returnOrDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .testTag("ancient_manual_overlay"),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val bookWidth = minOf(maxWidth, 430.dp)
            val bookHeight = minOf(maxHeight, 780.dp)
            val gestureEnabled = openProgress.value >= 1f && turnJob?.isActive != true
            val touchSlopPx = LocalViewConfiguration.current.touchSlop
            val minimumTurnDistancePx = with(LocalDensity.current) { 52.dp.toPx() }
            val minimumTurnVelocityPx = with(LocalDensity.current) { 720.dp.toPx() }

            Box(
                modifier = Modifier
                    .size(bookWidth, bookHeight)
                    .background(CoverShadow, RoundedCornerShape(5.dp))
                    .padding(start = 13.dp, top = 9.dp, end = 9.dp, bottom = 13.dp)
                    .testTag("ancient_manual_book"),
            ) {
                LayeredPageStack()
                BookPageTurnSurface(
                    current = currentPage,
                    pending = pendingPage,
                    forward = turnForward,
                    progress = if (turnJob?.isActive == true) turnProgress.value else {
                        (abs(dragDistance) / pageWidthPx).coerceIn(0f, 0.82f)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { pageWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                        .pointerInput(gestureEnabled, currentPage, pageWidthPx) {
                            if (!gestureEnabled) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial,
                                )
                                val pointerId = down.id
                                val velocityTracker = VelocityTracker()
                                velocityTracker.addPosition(down.uptimeMillis, down.position)
                                var axis = ManualGestureAxis.Undecided
                                var releaseHandled = false

                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    val totalX = change.position.x - down.position.x
                                    val totalY = change.position.y - down.position.y

                                    if (axis == ManualGestureAxis.Undecided) {
                                        axis = resolveManualGestureAxis(
                                            totalX = totalX,
                                            totalY = totalY,
                                            touchSlopPx = touchSlopPx,
                                        )
                                    }
                                    if (axis == ManualGestureAxis.Vertical) {
                                        return@awaitEachGesture
                                    }
                                    if (axis == ManualGestureAxis.Horizontal) {
                                        change.consume()
                                        val target = if (totalX < 0f) {
                                            nextPageFor(currentPage)
                                        } else {
                                            previousPageFor(currentPage)
                                        }
                                        dragDistance = if (target == null) totalX * 0.18f else totalX
                                        pendingPage = target
                                        turnForward = totalX < 0f
                                    }

                                    if (!change.pressed) {
                                        releaseHandled = true
                                        if (axis == ManualGestureAxis.Horizontal) {
                                            val target = pendingPage
                                            val progress = (abs(dragDistance) / pageWidthPx)
                                                .coerceIn(0f, 0.82f)
                                            val velocityX = velocityTracker.calculateVelocity().x
                                            val shouldCommit = target != null &&
                                                shouldCommitManualPageTurn(
                                                    distancePx = dragDistance,
                                                    pageWidthPx = pageWidthPx,
                                                    velocityPxPerSecond = velocityX,
                                                    minimumDistancePx = minimumTurnDistancePx,
                                                    minimumVelocityPxPerSecond = minimumTurnVelocityPx,
                                                )
                                            when {
                                                target == null -> {
                                                    pendingPage = null
                                                    dragDistance = 0f
                                                }
                                                shouldCommit -> animateToPage(
                                                    target = target,
                                                    forward = turnForward,
                                                    start = progress,
                                                )
                                                else -> cancelTurn(progress, target, turnForward)
                                            }
                                        }
                                        break
                                    }
                                }

                                if (!releaseHandled && axis == ManualGestureAxis.Horizontal) {
                                    pendingPage = null
                                    dragDistance = 0f
                                }
                            }
                        },
                    pageContent = { page ->
                        AncientPaperPage {
                            if (page.lessonPageNo == null) {
                                CatalogPage(
                                    volumeNo = volumeNo,
                                    volumeTitle = volumeTitle,
                                    entries = catalog,
                                    enabled = turnJob?.isActive != true,
                                    onSelect = { entry ->
                                        val book = entry.book ?: return@CatalogPage
                                        animateToPage(
                                            BookPageKey(lessonPageNo = book.pageNo, leafIndex = 0),
                                            forward = true,
                                        )
                                    },
                                )
                            } else {
                                val book = volumeBooks.firstOrNull { it.pageNo == page.lessonPageNo }
                                val localLesson = volumeOneLesson(page.lessonPageNo)
                                when {
                                    book == null -> MissingLessonPage("本招状态暂时无法读取，请返回目录后重试。")
                                    volumeNo == 1 && localLesson != null -> {
                                        val leaf = localLesson.leaves.getOrNull(page.leafIndex)
                                        if (leaf == null) {
                                            MissingLessonPage("这一页尚未整理完成。")
                                        } else {
                                            KnowledgeLeafPage(
                                                volumeNo = volumeNo,
                                                lesson = localLesson,
                                                book = book,
                                                leaf = leaf,
                                                leafIndex = page.leafIndex,
                                                readableCount = readableLeafCount(
                                                    book.state,
                                                    localLesson.leaves.size,
                                                ),
                                                detail = detail?.takeIf {
                                                    it.manual.id == book.manualPageId
                                                },
                                                loading = loading,
                                                message = message,
                                                onOpenReader = { onOpenReader(book) },
                                                onOpenTrial = onOpenTrial,
                                                onUseInCreation = { onUseInCreation(book.manualPageId) },
                                                predictionChoice = predictionChoice,
                                                predictionSubmitted = predictionSubmitted,
                                                onPredictionChoice = {
                                                    if (!predictionSubmitted) predictionChoice = it
                                                },
                                                onSubmitPrediction = {
                                                    if (predictionChoice != null) predictionSubmitted = true
                                                },
                                            )
                                        }
                                    }
                                    else -> GenericManualPage(
                                        book = book,
                                        detail = detail?.takeIf {
                                            it.manual.id == book.manualPageId
                                        },
                                        loading = loading,
                                        message = message,
                                        onOpenReader = { onOpenReader(book) },
                                        onOpenTrial = onOpenTrial,
                                        onUseInCreation = { onUseInCreation(book.manualPageId) },
                                    )
                                }
                            }
                        }
                    },
                )

                if (openProgress.value < 1f) {
                    CodexCover(
                        volumeNo = volumeNo,
                        title = volumeTitle,
                        imageRes = when (volumeNo) {
                            1 -> R.drawable.img_wushuhuan_book_01
                            2 -> R.drawable.img_wushuhuan_book_02
                            3 -> R.drawable.img_wushuhuan_book_03
                            4 -> R.drawable.img_wushuhuan_book_04
                            5 -> R.drawable.img_wushuhuan_book_05
                            6 -> R.drawable.img_wushuhuan_book_06
                            7 -> R.drawable.img_wushuhuan_book_07
                            8 -> R.drawable.img_wushuhuan_book_08
                            9 -> R.drawable.img_wushuhuan_book_09
                            else -> R.drawable.img_wushuhuan_book_10
                        },
                        progress = openProgress.value,
                    )
                }

                if (openProgress.value >= 1f) {
                    CompactPageNavigation(
                        previousAvailable = previousPageFor(currentPage) != null,
                        nextAvailable = nextPageFor(currentPage) != null,
                        enabled = turnJob?.isActive != true,
                        onPrevious = {
                            previousPageFor(currentPage)?.let {
                                animateToPage(it, forward = false)
                            }
                        },
                        onNext = {
                            nextPageFor(currentPage)?.let {
                                animateToPage(it, forward = true)
                            }
                        },
                    )
                }
            }
        }

        ManualBackButton(
            icon = if (currentLessonPageNo == null) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
            description = if (currentLessonPageNo == null) "合上秘籍" else "返回秘籍目录",
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 14.dp, top = 16.dp),
            onClick = ::returnOrDismiss,
        )
    }
}

@Composable
private fun BookPageTurnSurface(
    current: BookPageKey,
    pending: BookPageKey?,
    forward: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    pageContent: @Composable (BookPageKey) -> Unit,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier) {
        if (pending != null) {
            key(pending) {
                Box(Modifier.fillMaxSize()) { pageContent(pending) }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val edge = curlEdge(size, clamped, forward)
                    clipPath(edge.remaining) { this@drawWithContent.drawContent() }
                },
        ) {
            key(current) {
                pageContent(current)
            }
        }
        if (pending != null && clamped > 0f) {
            ContinuousCurlOverlay(progress = clamped, forward = forward)
        }
    }
}

@Composable
private fun BoxScope.LayeredPageStack() {
    val layers = listOf(
        Triple(6.dp, 7.dp, Color(0xFF4A4842)),
        Triple(4.dp, 5.dp, Color(0xFF686359)),
        Triple(2.dp, 3.dp, Color(0xFF8A8274)),
    )
    layers.forEachIndexed { index, (x, y, color) ->
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = x, y = y)
                .graphicsLayer {
                    shape = AncientPaperShape
                    clip = true
                }
                .background(color)
                .drawWithCache {
                    onDrawBehind {
                        val seamAlpha = 0.2f + index * 0.07f
                        drawLine(
                            color = PaperLight.copy(alpha = seamAlpha),
                            start = Offset(size.width * 0.08f, size.height - 1.5f - index),
                            end = Offset(size.width * 0.98f, size.height - 2.2f - index),
                            strokeWidth = 0.7f,
                        )
                    }
                },
        )
    }
}

private data class CurlGeometry(
    val remaining: Path,
    val fold: Path,
    val shadow: Path,
    val edgeTop: Offset,
    val edgeBottom: Offset,
)

private fun curlEdge(size: Size, progress: Float, forward: Boolean): CurlGeometry {
    val p = progress.coerceIn(0f, 1f)
    val width = size.width
    val height = size.height
    val travel = width * p
    val topInset = travel * (0.82f + 0.18f * p)
    val bottomInset = travel
    val curve = width * (0.018f + 0.082f * sin(p * Math.PI).toFloat())
    val edgeTopX = (width - topInset).coerceIn(0f, width)
    val edgeBottomX = (width - bottomInset).coerceIn(0f, width)

    fun mirrorX(x: Float): Float = if (forward) x else width - x
    val top = Offset(mirrorX(edgeTopX), 0f)
    val bottom = Offset(mirrorX(edgeBottomX), height)
    val middleX = mirrorX(((edgeTopX + edgeBottomX) / 2f - curve).coerceIn(0f, width))

    val remaining = Path().apply {
        if (forward) {
            moveTo(0f, 0f)
            lineTo(top.x, top.y)
            cubicTo(
                top.x - curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo(0f, height)
        } else {
            moveTo(width, 0f)
            lineTo(top.x, top.y)
            cubicTo(
                top.x + curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo(width, height)
        }
        close()
    }

    val foldWidth = (
        width * (0.018f + 0.13f * sin(p * Math.PI).toFloat()) * (1f - 0.55f * p)
    ).coerceAtLeast(4f)
    val fold = Path().apply {
        moveTo(top.x, top.y)
        if (forward) {
            cubicTo(
                top.x - curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo((bottom.x + foldWidth).coerceAtMost(width), height)
            cubicTo(
                (middleX + foldWidth).coerceAtMost(width), height * 0.67f,
                (top.x + foldWidth).coerceAtMost(width), height * 0.26f,
                (top.x + foldWidth * 0.62f).coerceAtMost(width), 0f,
            )
        } else {
            cubicTo(
                top.x + curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo((bottom.x - foldWidth).coerceAtLeast(0f), height)
            cubicTo(
                (middleX - foldWidth).coerceAtLeast(0f), height * 0.67f,
                (top.x - foldWidth).coerceAtLeast(0f), height * 0.26f,
                (top.x - foldWidth * 0.62f).coerceAtLeast(0f), 0f,
            )
        }
        close()
    }
    val shadowWidth = width * (0.025f + 0.055f * sin(p * Math.PI).toFloat())
    val shadow = Path().apply {
        moveTo(top.x, top.y)
        if (forward) {
            cubicTo(
                top.x - curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo((bottom.x - shadowWidth).coerceAtLeast(0f), height)
            cubicTo(
                (middleX - shadowWidth).coerceAtLeast(0f), height * 0.68f,
                (top.x - curve - shadowWidth).coerceAtLeast(0f), height * 0.27f,
                (top.x - shadowWidth).coerceAtLeast(0f), 0f,
            )
        } else {
            cubicTo(
                top.x + curve, height * 0.27f,
                middleX, height * 0.68f,
                bottom.x, bottom.y,
            )
            lineTo((bottom.x + shadowWidth).coerceAtMost(width), height)
            cubicTo(
                (middleX + shadowWidth).coerceAtMost(width), height * 0.68f,
                (top.x + curve + shadowWidth).coerceAtMost(width), height * 0.27f,
                (top.x + shadowWidth).coerceAtMost(width), 0f,
            )
        }
        close()
    }
    return CurlGeometry(remaining, fold, shadow, top, bottom)
}

@Composable
private fun ContinuousCurlOverlay(progress: Float, forward: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        val geometry = curlEdge(size, progress, forward)
        val centerX = (geometry.edgeTop.x + geometry.edgeBottom.x) / 2f
        drawPath(
            path = geometry.shadow,
            brush = if (forward) {
                Brush.horizontalGradient(
                    listOf(Color.Transparent, PaperEdge.copy(alpha = 0.34f)),
                    startX = centerX - size.width * 0.11f,
                    endX = centerX,
                )
            } else {
                Brush.horizontalGradient(
                    listOf(PaperEdge.copy(alpha = 0.34f), Color.Transparent),
                    startX = centerX,
                    endX = centerX + size.width * 0.11f,
                )
            },
        )
        drawPath(
            path = geometry.fold,
            brush = if (forward) {
                Brush.horizontalGradient(
                    colors = listOf(PaperCrease, PaperLight, PaperSpine),
                    startX = centerX,
                    endX = (centerX + size.width * 0.17f).coerceAtMost(size.width),
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(PaperSpine, PaperLight, PaperCrease),
                    startX = (centerX - size.width * 0.17f).coerceAtLeast(0f),
                    endX = centerX,
                )
            },
        )
        drawLine(
            color = PaperEdge.copy(alpha = 0.52f),
            start = geometry.edgeTop,
            end = geometry.edgeBottom,
            strokeWidth = 1.dp.toPx(),
        )
    }
}

@Composable
private fun AncientPaperPage(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                shape = AncientPaperShape
                clip = true
            }
            .background(
                Brush.verticalGradient(
                    0f to PaperLight,
                    0.24f to PaperBase,
                    0.72f to Color(0xFFBDB5A2),
                    1f to PaperShade,
                ),
            )
            .drawWithCache {
                val horizontalFibers = List(34) { index ->
                    Path().apply {
                        val baseY = size.height * (index + 0.45f) / 34f
                        moveTo(0f, baseY)
                        cubicTo(
                            size.width * 0.28f,
                            baseY + sin(index * 1.31f) * 2.1f,
                            size.width * 0.67f,
                            baseY + cos(index * 0.83f) * 2.7f,
                            size.width,
                            baseY + sin(index * 1.77f) * 1.6f,
                        )
                    }
                }
                val shortFibers = List(26) { index ->
                    val startX = size.width * ((index * 37 % 101) / 101f)
                    val startY = size.height * ((index * 61 % 103) / 103f)
                    Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            startX + size.width * 0.035f,
                            startY - 2f,
                            startX + size.width * 0.07f,
                            startY + 3f,
                            startX + size.width * 0.11f,
                            startY + sin(index.toFloat()) * 2f,
                        )
                    }
                }
                onDrawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PaperLight.copy(alpha = 0.28f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.54f, size.height * 0.42f),
                            radius = maxOf(size.width, size.height) * 0.62f,
                        ),
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PaperCrease.copy(alpha = 0.11f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.18f, size.height * 0.78f),
                            radius = size.width * 0.52f,
                        ),
                    )
                    horizontalFibers.forEachIndexed { index, path ->
                        drawPath(
                            path = path,
                            color = PaperEdge.copy(alpha = if (index % 4 == 0) 0.055f else 0.032f),
                            style = Stroke(width = if (index % 5 == 0) 0.75f else 0.45f),
                        )
                    }
                    shortFibers.forEachIndexed { index, path ->
                        drawPath(
                            path = path,
                            color = if (index % 3 == 0) {
                                PaperLight.copy(alpha = 0.14f)
                            } else {
                                PaperCrease.copy(alpha = 0.065f)
                            },
                            style = Stroke(width = 0.7f),
                        )
                    }
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PaperEdge.copy(alpha = 0.34f),
                                PaperSpine.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Transparent,
                                PaperEdge.copy(alpha = 0.17f),
                            ),
                        ),
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PaperEdge.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Transparent,
                                PaperCrease.copy(alpha = 0.2f),
                            ),
                        ),
                    )
                }
            },
        content = content,
    )
}

private object AncientPaperShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val inset = with(density) { 2.5.dp.toPx() }
        val minStep = with(density) { 5.5.dp.toPx() }
        val path = Path().apply {
            fun topDepth(index: Int): Float = inset * (
                0.5f + 0.34f * sin(index * 1.71f) + 0.16f * cos(index * 0.57f)
            ).coerceIn(0.08f, 0.98f)

            fun sideDepth(index: Int): Float = inset * (
                0.48f + 0.3f * sin(index * 1.19f) + 0.2f * cos(index * 0.43f)
            ).coerceIn(0.06f, 0.98f)

            var x = inset * 1.4f
            var i = 0
            moveTo(x, topDepth(i))
            while (x < size.width - inset * 1.5f) {
                lineTo(x, topDepth(i))
                x += minStep * (0.78f + 0.34f * (sin(i * 0.91f) + 1f) / 2f)
                i++
            }
            var y = inset
            i = 0
            while (y < size.height - inset) {
                lineTo(size.width - sideDepth(i), y)
                y += minStep * (0.82f + 0.3f * (cos(i * 0.79f) + 1f) / 2f)
                i++
            }
            x = size.width - inset
            i = 0
            while (x > inset) {
                lineTo(x, size.height - topDepth(i + 17))
                x -= minStep * (0.76f + 0.38f * (sin(i * 1.07f) + 1f) / 2f)
                i++
            }
            y = size.height - inset
            i = 0
            while (y > inset) {
                lineTo(sideDepth(i + 11), y)
                y -= minStep * (0.8f + 0.32f * (cos(i * 0.67f) + 1f) / 2f)
                i++
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun CatalogPage(
    volumeNo: Int,
    volumeTitle: String,
    entries: List<CatalogEntry>,
    enabled: Boolean,
    onSelect: (CatalogEntry) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "第${chineseNumber(volumeNo)}卷",
            color = MutedInk,
            fontFamily = AncientKai,
            fontSize = 15.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = volumeTitle,
                color = Ink,
                fontFamily = AncientKai,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                modifier = Modifier.weight(1f),
            )
            Seal(text = "卷")
        }
        InkDivider()
        Text(
            text = "本卷五招",
            color = JadeInk,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
        entries.forEachIndexed { index, entry ->
            val stateText = entry.book?.let {
                if (it.reviewDue) "待温习 · 原状态${it.stateLabel}" else it.stateLabel
            } ?: "状态暂未读到"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .clickable(
                        enabled = enabled && entry.book != null,
                        role = Role.Button,
                        onClickLabel = "打开第${index + 1}招",
                    ) { onSelect(entry) }
                    .semantics(mergeDescendants = true) {
                        contentDescription = "第${index + 1}招，${entry.title}，$stateText"
                    }
                    .padding(vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chineseNumber(index + 1),
                        color = JadeInk,
                        fontFamily = AncientKai,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.width(30.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = entry.title,
                            color = Ink,
                            fontFamily = AncientKai,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 27.sp,
                        )
                        Text(
                            text = entry.coreLogic,
                            color = MutedInk,
                            fontFamily = AncientKai,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        )
                    }
                    Text(
                        text = stateText,
                        color = if (entry.book?.state == "UNSEEN") MutedInk else Cinnabar,
                        fontFamily = AncientKai,
                        fontSize = 15.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(max = 82.dp),
                    )
                }
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 30.dp),
                ) {
                    val divider = Path().apply {
                        moveTo(0f, 0.35f)
                        cubicTo(
                            size.width * 0.31f,
                            0.9f,
                            size.width * 0.67f,
                            -0.55f,
                            size.width,
                            0.2f,
                        )
                    }
                    drawPath(
                        path = divider,
                        color = PaperEdge.copy(alpha = 0.34f),
                        style = Stroke(width = 0.75.dp.toPx()),
                    )
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = "轻触一招展开；左右滑动可翻页",
            color = MutedInk,
            fontFamily = AncientKai,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KnowledgeLeafPage(
    volumeNo: Int,
    lesson: ManualLessonContent,
    book: LearningBookDto,
    leaf: ManualKnowledgeLeaf,
    leafIndex: Int,
    readableCount: Int,
    detail: ManualDetailBundle?,
    loading: Boolean,
    message: String?,
    onOpenReader: () -> Unit,
    onOpenTrial: (String) -> Unit,
    onUseInCreation: () -> Unit,
    predictionChoice: String?,
    predictionSubmitted: Boolean,
    onPredictionChoice: (String) -> Unit,
    onSubmitPrediction: () -> Unit,
) {
    val learned = book.state in setOf("LEARNED", "MASTERED", "TEACHING")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "第${chineseNumber(lesson.pageNo)}招",
                    color = MutedInk,
                    fontFamily = AncientKai,
                    fontSize = 14.sp,
                )
                Text(
                    text = lesson.title,
                    color = Ink,
            fontFamily = AncientKai,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 33.sp,
                )
            }
            Seal(text = stateSeal(book.state))
        }
        InkDivider()
        Text(
            text = leaf.section,
            color = JadeInk,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 27.sp,
        )
        Text(
            text = leaf.title,
            color = Ink,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 27.sp,
        )
        if (leaf.comicImages.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                leaf.comicImages.take(2).forEach { imageRes ->
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.08f)
                            .graphicsLayer {
                                shape = RoundedCornerShape(2.dp)
                                clip = true
                            }
                            .background(PaperShade),
                    )
                }
            }
        }
        leaf.paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                color = Ink,
                fontFamily = AncientKai,
                fontSize = 17.sp,
                lineHeight = 28.sp,
            )
        }
        when {
            lesson.pageNo == 1 && leafIndex == 0 -> PredictionChoiceBlock(
                selectedChoice = predictionChoice,
                submitted = predictionSubmitted,
                onSelect = onPredictionChoice,
                onSubmit = onSubmitPrediction,
            )
            lesson.pageNo == 1 && leafIndex == 1 -> PredictionRevealBlock(
                selectedChoice = predictionChoice,
            )
            lesson.pageNo == 1 && leafIndex == 6 -> CheckUnderstandingBlock(
                manualId = book.manualPageId,
            )
            leaf.bullets.isNotEmpty() -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                leaf.bullets.forEach { bullet ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "◆",
                            color = JadeInk,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
                        )
                        Text(
                            text = bullet,
                            color = Ink,
                    fontFamily = AncientKai,
                            fontSize = 16.sp,
                            lineHeight = 25.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            }
        }
        leaf.keyLine?.let { keyLine ->
            Text(
                text = "心法  $keyLine",
                color = Cinnabar,
            fontFamily = AncientKai,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 27.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        when (leaf.action) {
            ManualLeafAction.OPEN_COMIC -> InkAction("翻看本招漫画", onOpenReader)
            ManualLeafAction.OPEN_TRIAL -> {
                val trialId = detail?.manual?.trialId
                if (trialId != null) {
                    InkAction("进入真实试炼") { onOpenTrial(trialId) }
                } else if (loading) {
                    CircularProgressIndicator(
                        color = JadeInk,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    Text(
                        text = message ?: "本招试炼尚未接入，不会用静态页面模拟完成。",
                        color = MutedInk,
                    fontFamily = AncientKai,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                    )
                }
            }
            ManualLeafAction.USE_IN_CREATION -> if (learned) {
                InkAction("把它用起来", onUseInCreation)
            }
            null -> Unit
        }

        if (leafIndex == readableCount - 1 && readableCount < lesson.leaves.size) {
            InkDivider()
            Text(
                text = when (book.state) {
                    "UNSEEN" -> "先读完漫画并完成预测，本招会记录为偶得，再继续进入试炼。"
                    "DISCOVERED" -> "完成对应试炼并由服务端确认习得后，可以阅读后面的详细心法。"
                    else -> "学习状态暂时无法确认，当前不会提前解锁后续内容。"
                },
                color = MutedInk,
                fontFamily = AncientKai,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "第${leafIndex + 1}叶 · 共${lesson.leaves.size}叶 · 第${chineseNumber(volumeNo)}卷",
            color = MutedInk,
            fontFamily = AncientKai,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PredictionChoiceBlock(
    selectedChoice: String?,
    submitted: Boolean,
    onSelect: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "机关会自己行动，就一定是在学习吗？",
            color = Ink,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )
        AncientChoice(
            label = "甲  一定是",
            selected = selectedChoice == "A",
            enabled = !submitted,
            onClick = { onSelect("A") },
        )
        AncientChoice(
            label = "乙  不一定",
            selected = selectedChoice == "B",
            enabled = !submitted,
            onClick = { onSelect("B") },
        )
        if (submitted) {
            Text(
                text = "已经记下你的判断。翻到下一叶，再和漫画中的现象对照。",
                color = JadeInk,
                fontFamily = AncientKai,
                fontSize = 15.sp,
                lineHeight = 23.sp,
            )
        } else if (selectedChoice != null) {
            InkAction(label = "记下我的判断", onClick = onSubmit)
        } else {
            Text(
                text = "先选择自己的想法，提交前不会显示结论。",
                color = MutedInk,
                fontFamily = AncientKai,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun PredictionRevealBlock(selectedChoice: String?) {
    val originalChoice = when (selectedChoice) {
        "A" -> "甲：一定是"
        "B" -> "乙：不一定"
        else -> "没有找到已保存的选择"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("你的原来想法")
        Text(
            text = originalChoice,
            color = Cinnabar,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 26.sp,
        )
        SectionHeading("漫画中的实际现象")
        Text(
            text = "机关能够接收信号并作出动作，但漫画没有显示它会从新的样本或反馈中改变判断方法。只看见‘会动’，证据还不够。",
            color = Ink,
            fontFamily = AncientKai,
            fontSize = 17.sp,
            lineHeight = 28.sp,
        )
        SectionHeading("怎样调整判断")
        Text(
            text = if (selectedChoice == "B") {
                "你已经抓住了关键：先不被动作表面迷惑，再寻找样本、反馈和判断变化的证据。接下来要继续分清固定规则与从样本学习。"
            } else {
                "你注意到了机关能独立行动，这是很有用的观察。还可以再补问一步：它的条件是人提前写好的，还是会从样本与反馈中改变？"
            },
            color = Ink,
            fontFamily = AncientKai,
            fontSize = 17.sp,
            lineHeight = 28.sp,
        )
    }
}

@Composable
private fun CheckUnderstandingBlock(manualId: String) {
    var first by rememberSaveable(manualId, "check-1") { mutableStateOf<String?>(null) }
    var second by rememberSaveable(manualId, "check-2") { mutableStateOf<String?>(null) }
    var third by rememberSaveable(manualId, "check-3") { mutableStateOf<String?>(null) }
    var revealed by rememberSaveable(manualId, "check-revealed") { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        UnderstandingCase(
            title = "一、到固定时间响铃",
            selected = first,
            enabled = !revealed,
            onSelect = { first = it },
        )
        UnderstandingCase(
            title = "二、根据长期输入习惯改变候选词",
            selected = second,
            enabled = !revealed,
            onSelect = { second = it },
        )
        UnderstandingCase(
            title = "三、感应到障碍后自动停止的小车",
            selected = third,
            enabled = !revealed,
            onSelect = { third = it },
        )

        if (!revealed && first != null && second != null && third != null) {
            InkAction("查看解释") { revealed = true }
        } else if (!revealed) {
            Text(
                text = "三个案例都先作出判断，解释才会出现。这里不计奖励，也不会改变秘籍状态。",
                color = MutedInk,
                    fontFamily = AncientKai,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }

        if (revealed) {
            SectionHeading("逐项对照")
            ExplanationLine(
                title = "固定时间响铃",
                answer = "固定规则",
                selected = first,
                explanation = "时间条件由人提前设定，到点执行动作；一次新的响铃不会自己改写条件。",
            )
            ExplanationLine(
                title = "候选词长期改变",
                answer = "从样本学习",
                selected = second,
                explanation = "历史选择成为样本，并持续影响之后的排序，具备从反馈调整判断的证据。",
            )
            ExplanationLine(
                title = "遇到障碍自动停止",
                answer = "目前不能确定",
                selected = third,
                explanation = "它可能只检查距离门槛，也可能使用学习模型；只看停止动作，证据不足。",
            )
        }
    }
}

@Composable
private fun UnderstandingCase(
    title: String,
    selected: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = title,
            color = Ink,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        listOf("固定规则", "从样本学习", "目前不能确定").forEach { answer ->
            AncientChoice(
                label = answer,
                selected = selected == answer,
                enabled = enabled,
                onClick = { onSelect(answer) },
            )
        }
    }
}

@Composable
private fun ExplanationLine(
    title: String,
    answer: String,
    selected: String?,
    explanation: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$title · $answer",
            color = if (selected == answer) JadeInk else Cinnabar,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 25.sp,
        )
        Text(
            text = if (selected == answer) {
                "你的判断有对应证据。$explanation"
            } else {
                "你原来选择了“${selected ?: "未选择"}”。可以补看这条证据：$explanation"
            },
            color = Ink,
            fontFamily = AncientKai,
            fontSize = 16.sp,
            lineHeight = 25.sp,
        )
    }
}

@Composable
private fun AncientChoice(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        role = Role.RadioButton,
                        onClickLabel = "选择$label",
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$label，${if (selected) "已选择" else "未选择"}"
            }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                color = if (selected) Cinnabar.copy(alpha = 0.09f) else PaperLight.copy(alpha = 0.2f),
            )
            drawRect(
                color = if (selected) Cinnabar else PaperEdge.copy(alpha = 0.56f),
                style = Stroke(width = if (selected) 1.5.dp.toPx() else 1.dp.toPx()),
            )
        }
        Text(
            text = label,
            color = if (selected) Cinnabar else Ink,
            fontFamily = AncientKai,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 25.sp,
        )
    }
}

@Composable
private fun GenericManualPage(
    book: LearningBookDto,
    detail: ManualDetailBundle?,
    loading: Boolean,
    message: String?,
    onOpenReader: () -> Unit,
    onOpenTrial: (String) -> Unit,
    onUseInCreation: () -> Unit,
) {
    val learned = book.state in setOf("LEARNED", "MASTERED", "TEACHING")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("第${book.pageNo}招", color = MutedInk, fontFamily = AncientKai, fontSize = 14.sp)
        Text(
            book.title,
            color = Ink,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp,
        )
        InkDivider()
        when {
            detail != null -> {
                SectionHeading("这一招讲什么")
                Text(detail.manual.coreLogic, color = Ink, fontFamily = AncientKai, fontSize = 17.sp, lineHeight = 28.sp)
                Text(detail.manual.lifeHook, color = Ink, fontFamily = AncientKai, fontSize = 17.sp, lineHeight = 28.sp)
                Text(
                    "迁移练习  ${detail.manual.interactionEvidence}",
                    color = Cinnabar,
            fontFamily = AncientKai,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 27.sp,
                )
                InkAction("翻看漫画", onOpenReader)
                detail.manual.trialId?.let { id -> InkAction("进入真实试炼") { onOpenTrial(id) } }
                if (learned) InkAction("把它用起来", onUseInCreation)
            }
            loading -> CircularProgressIndicator(color = JadeInk, modifier = Modifier.size(30.dp))
            else -> MissingLessonPage(message ?: "这一招的内容暂时无法载入，请稍后重试。")
        }
    }
}

@Composable
private fun MissingLessonPage(message: String) {
    Box(Modifier.fillMaxSize().padding(34.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MutedInk,
            fontFamily = AncientKai,
            fontSize = 17.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        color = JadeInk,
            fontFamily = AncientKai,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
}

@Composable
private fun InkAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRoundRect(
                color = JadeInk.copy(alpha = 0.1f),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
            drawRoundRect(
                color = JadeInk,
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }
        Text(
            text = label,
            color = JadeInk,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
        )
    }
}

@Composable
private fun InkDivider() {
    Canvas(Modifier.fillMaxWidth().height(8.dp)) {
        drawLine(
            color = PaperEdge.copy(alpha = 0.7f),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

@Composable
private fun Seal(text: String) {
    Box(
        modifier = Modifier.size(38.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Cinnabar, style = Stroke(width = 1.6.dp.toPx()))
            drawRect(
                Cinnabar.copy(alpha = 0.7f),
                topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx()),
            )
        }
        Text(
            text = text,
            color = Cinnabar,
            fontFamily = AncientKai,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
        )
    }
}

@Composable
private fun CodexCover(
    volumeNo: Int,
    title: String,
    imageRes: Int,
    progress: Float,
) {
    val density = LocalDensity.current.density
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                rotationY = -112f * progress
                cameraDistance = 18f * density
                alpha = (1f - ((progress - 0.86f) / 0.14f).coerceIn(0f, 1f))
                shadowElevation = 12.dp.toPx()
            }
            .background(
                Brush.horizontalGradient(
                    listOf(CoverShadow, CoverGreen, Color(0xFF17494A), CoverShadow),
                ),
                RoundedCornerShape(3.dp),
            )
            .padding(14.dp),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(OldGold.copy(alpha = 0.9f), style = Stroke(width = 1.4.dp.toPx()))
            val inset = 7.dp.toPx()
            drawRect(
                OldGold.copy(alpha = 0.55f),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = 0.7.dp.toPx()),
            )
            repeat(18) { index ->
                val x = size.width * (index + 1f) / 19f
                drawLine(
                    color = OldGold.copy(alpha = 0.055f),
                    start = Offset(x, inset),
                    end = Offset(x + sin(index.toFloat()) * 5f, size.height - inset),
                    strokeWidth = 0.7f,
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = OldGold,
            fontFamily = AncientKai,
                fontWeight = FontWeight.Bold,
                fontSize = 31.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "第${chineseNumber(volumeNo)}卷",
                color = OldGold.copy(alpha = 0.82f),
                fontFamily = AncientKai,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.72f).aspectRatio(1f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (volumeNo == 1) "五招 · 能力与边界" else "五招秘籍",
                color = OldGold.copy(alpha = 0.86f),
            fontFamily = AncientKai,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun BoxScope.CompactPageNavigation(
    previousAvailable: Boolean,
    nextAvailable: Boolean,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (previousAvailable) {
        CompactPageButton(
            description = "上一页",
            align = Alignment.CenterStart,
            enabled = enabled,
            onClick = onPrevious,
        )
    }
    if (nextAvailable) {
        CompactPageButton(
            description = "下一页",
            align = Alignment.CenterEnd,
            enabled = enabled,
            onClick = onNext,
        )
    }
}

@Composable
private fun BoxScope.CompactPageButton(
    description: String,
    align: Alignment,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(align)
            .size(48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (align == Alignment.CenterStart) "‹" else "›",
            color = JadeInk.copy(alpha = 0.78f),
            fontFamily = AncientKai,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun ManualBackButton(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(Color.Black.copy(alpha = 0.34f), RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

private fun stateSeal(state: String): String = when (state) {
    "DISCOVERED" -> "得"
    "LEARNED" -> "习"
    "MASTERED" -> "悟"
    "TEACHING" -> "传"
    else -> "闻"
}

private fun chineseNumber(number: Int): String = when (number) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    7 -> "七"
    8 -> "八"
    9 -> "九"
    10 -> "十"
    else -> number.toString()
}
