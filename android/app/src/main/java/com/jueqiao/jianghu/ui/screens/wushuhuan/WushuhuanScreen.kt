package com.jueqiao.jianghu.ui.screens.wushuhuan

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningBookDto
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import com.jueqiao.jianghu.luggage.ManualDetailBundle
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

private data class BookSlot(
    val volumeNo: Int,
    @DrawableRes val imageRes: Int,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
    val rotation: Float = 0f,
)

private val bookSlots = listOf(
    BookSlot(1, R.drawable.img_wushuhuan_book_01, 135.dp, 221.dp, 155.dp, 147.dp),
    BookSlot(2, R.drawable.img_wushuhuan_book_02, 8.dp, 205.dp, 96.dp, 96.dp),
    // 原稿把第 3 册裁出屏幕 21dp，导致实际可触范围不足 48dp；向内收齐以保证整本可点。
    BookSlot(3, R.drawable.img_wushuhuan_book_03, 0.dp, 130.29.dp, 66.29.dp, 69.dp, -11.03f),
    BookSlot(4, R.drawable.img_wushuhuan_book_04, 50.dp, 87.dp, 64.dp, 66.6.dp),
    BookSlot(5, R.drawable.img_wushuhuan_book_05, 123.4.dp, 66.dp, 55.6.dp, 57.9.dp),
    BookSlot(6, R.drawable.img_wushuhuan_book_06, 198.dp, 69.dp, 42.62.dp, 38.41.dp),
    BookSlot(7, R.drawable.img_wushuhuan_book_07, 258.15.dp, 68.5.dp, 54.78.dp, 51.85.dp),
    BookSlot(8, R.drawable.img_wushuhuan_book_08, 311.04.dp, 92.dp, 58.dp, 57.5.dp),
    BookSlot(9, R.drawable.img_wushuhuan_book_09, 357.dp, 136.32.dp, 66.dp, 69.dp),
    BookSlot(10, R.drawable.img_wushuhuan_book_10, 321.dp, 205.dp, 93.dp, 92.dp),
)

private const val BookCount = 10
private const val UnseenBookSaturation = 0.78f
private val unseenBookColorFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(UnseenBookSaturation) },
)

internal fun bookVisualAlpha(distanceFromCenter: Float, unseen: Boolean): Float {
    if (distanceFromCenter <= 0.08f) return 1f
    val distanceFraction = (distanceFromCenter / (BookCount / 2f)).coerceIn(0f, 1f)
    val positionAlpha = lerp(start = 1f, end = 0.28f, fraction = distanceFraction)
    val stateMultiplier = if (unseen) 0.84f else 1f
    return (positionAlpha * stateMultiplier).coerceIn(0.22f, 1f)
}

internal fun bookVisualSaturation(unseen: Boolean): Float =
    if (unseen) UnseenBookSaturation else 1f

internal fun nearestRingTarget(
    currentPosition: Float,
    volumeIndex: Int,
    itemCount: Int = BookCount,
): Float {
    require(itemCount > 0)
    val normalizedIndex = floorMod(volumeIndex, itemCount)
    val currentCycle = floor(currentPosition / itemCount).toInt()
    return ((currentCycle - 1)..(currentCycle + 1))
        .map { cycle -> normalizedIndex + cycle * itemCount.toFloat() }
        .minBy { candidate -> abs(candidate - currentPosition) }
}

internal fun volumeForRingPosition(
    position: Float,
    itemCount: Int = BookCount,
): Int {
    require(itemCount > 0)
    return floorMod(position.roundToInt(), itemCount) + 1
}

private fun floorMod(value: Int, divisor: Int): Int = ((value % divisor) + divisor) % divisor

private fun positiveModulo(value: Float, divisor: Int): Float {
    val remainder = value % divisor
    return if (remainder < 0f) remainder + divisor else remainder
}

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp =
    start + (end - start) * fraction

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction

private data class InterpolatedBookSlot(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
    val rotation: Float,
    val distanceFromCenter: Float,
)

private fun interpolatedSlot(bookIndex: Int, ringPosition: Float): InterpolatedBookSlot {
    val logicalSlot = positiveModulo(bookIndex - ringPosition, BookCount)
    val startIndex = floor(logicalSlot).toInt()
    val endIndex = (startIndex + 1) % BookCount
    val fraction = logicalSlot - startIndex
    val start = bookSlots[startIndex]
    val end = bookSlots[endIndex]
    return InterpolatedBookSlot(
        x = lerp(start.x, end.x, fraction),
        y = lerp(start.y, end.y, fraction),
        width = lerp(start.width, end.width, fraction),
        height = lerp(start.height, end.height, fraction),
        rotation = lerp(start.rotation, end.rotation, fraction),
        distanceFromCenter = minOf(logicalSlot, BookCount - logicalSlot),
    )
}

/**
 * 悟书环主场景。
 *
 * 旧 Gunlun1—15 的逐页展示由这里的选中状态取代。十本可见书分别代表十卷，
 * 每卷展示接口返回的推荐招式或第一招，状态直接复用行囊学习概览。
 */
@Composable
fun WushuhuanScreen(
    onBack: () -> Unit,
    learningOverview: LearningOverviewDto?,
    manualDetail: ManualDetailBundle?,
    isLoading: Boolean,
    loadMessage: String?,
    onOpenHoushan: (lessonId: String?) -> Unit,
    onLoadManualDetail: (String) -> Unit,
    onOpenReader: (volumeNo: Int, manualId: String, continueToTrial: Boolean) -> Unit,
    onOpenTrial: (String) -> Unit,
    onUseInCreation: (String) -> Unit,
) {
    val recommended = learningOverview?.books?.firstOrNull {
        it.manualPageId == learningOverview.recommendedLessonId
    }
    val booksByVolume = learningOverview?.books
        .orEmpty()
        .groupBy { it.volumeNo }
        .mapValues { (_, books) ->
            // 一册里可能有多招。回到悟书环时优先展示真实进度最高、最近更新的
            // 那一招，避免刚由服务端确认“习得”却仍被本册第一页的“未闻”盖住。
            books.maxWithOrNull(
                compareBy<LearningBookDto> { learningStateRank(it.state) }
                    .thenBy { it.updatedAt.orEmpty() }
                    .thenBy { -it.pageNo },
            )
        }

    var selectedVolume by rememberSaveable { mutableIntStateOf(1) }
    var userSelectedBook by rememberSaveable { mutableStateOf(false) }
    var detailBookId by rememberSaveable { mutableStateOf<String?>(null) }
    var ringPosition by rememberSaveable { mutableFloatStateOf(0f) }
    var ringAnimationJob by remember { mutableStateOf<Job?>(null) }
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val coroutineScope = rememberCoroutineScope()
    val pixelsPerSlot = with(LocalDensity.current) { 72.dp.toPx() }

    LaunchedEffect(recommended?.volumeNo, userSelectedBook) {
        if (!userSelectedBook) {
            selectedVolume = recommended?.volumeNo ?: 1
            ringPosition = (selectedVolume - 1).toFloat()
        }
    }

    val selectedBook = booksByVolume[selectedVolume]
    val selectedLessonId = selectedBook?.manualPageId
        ?: learningOverview?.backMountain?.lessonId
    val detailBook = learningOverview?.books?.firstOrNull { it.manualPageId == detailBookId }

    fun selectBook(volumeNo: Int) {
        userSelectedBook = true
        val target = nearestRingTarget(ringPosition, volumeNo - 1)
        val alreadyCentered = abs(target - ringPosition) < 0.08f &&
            ringAnimationJob?.isActive != true

        if (alreadyCentered) {
            selectedVolume = volumeNo
            val book = booksByVolume[volumeNo]
            if (book != null) {
                detailBookId = book.manualPageId
            }
            return
        }

        ringAnimationJob?.cancel()
        if (!animationsEnabled) {
            ringPosition = positiveModulo(target, BookCount)
            selectedVolume = volumeNo
            return
        }
        ringAnimationJob = coroutineScope.launch {
            val animation = Animatable(ringPosition)
            animation.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            ) {
                ringPosition = value
            }
            ringPosition = positiveModulo(target, BookCount)
            selectedVolume = volumeNo
        }
    }

    fun settleRingAfterDrag(velocityX: Float) {
        userSelectedBook = true
        ringAnimationJob?.cancel()
        val projectedSlots = (-velocityX / pixelsPerSlot * 0.12f).coerceIn(-2.25f, 2.25f)
        val projectedPosition = ringPosition + projectedSlots
        val target = projectedPosition.roundToInt().toFloat()
        val targetVolume = volumeForRingPosition(target)

        if (!animationsEnabled) {
            ringPosition = positiveModulo(target, BookCount)
            selectedVolume = targetVolume
            return
        }
        ringAnimationJob = coroutineScope.launch {
            val animation = Animatable(ringPosition)
            if (abs(projectedPosition - ringPosition) > 0.02f) {
                animation.animateTo(
                    targetValue = projectedPosition,
                    animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
                ) {
                    ringPosition = value
                }
            }
            animation.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            ) {
                ringPosition = value
            }
            ringPosition = positiveModulo(target, BookCount)
            selectedVolume = targetVolume
        }
    }

    fun moveRingByOne(step: Int): Boolean {
        val volumeNo = volumeForRingPosition(ringPosition.roundToInt() + step.toFloat())
        selectBook(volumeNo)
        return true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StandardGunlunScaffold(onBack = onBack) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // 原稿按 422dp 宽度定位。十本书沿原有十个槽位循环移动，
                // 所以背景、熊猫和书阵整体构图都不会被重新排版。
                val sceneScale = (maxWidth.value / 422f).coerceAtMost(1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(374.dp)
                        .testTag("wushuhuan_ring")
                        .semantics {
                            contentDescription = "悟书环，左右滑动查看秘籍"
                            customActions = listOf(
                                CustomAccessibilityAction("上一册秘籍") {
                                    moveRingByOne(-1)
                                },
                                CustomAccessibilityAction("下一册秘籍") {
                                    moveRingByOne(1)
                                },
                            )
                        }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                ringPosition -= delta / pixelsPerSlot
                            },
                            onDragStarted = {
                                ringAnimationJob?.cancel()
                                ringAnimationJob = null
                                detailBookId = null
                            },
                            onDragStopped = { velocity -> settleRingAfterDrag(velocity) },
                        ),
                ) {
                    bookSlots.forEachIndexed { bookIndex, bookSlot ->
                        val visualSlot = interpolatedSlot(bookIndex, ringPosition)
                        SelectableBook(
                            bookSlot = bookSlot,
                            visualSlot = visualSlot,
                            book = booksByVolume[bookSlot.volumeNo],
                            selected = bookSlot.volumeNo == selectedVolume &&
                                visualSlot.distanceFromCenter < 0.08f,
                            sceneScale = sceneScale,
                            onClick = { selectBook(bookSlot.volumeNo) },
                        )
                    }
                }

                val canOpenContextualHoushan = selectedBook == null || selectedBook.state == "UNSEEN"
                // 统一复用项目现有纸笺气泡；未闻时它仍是唯一的“前往后山”动作。
                HomeGuideBubble(
                    text = statusCopy(
                        selectedVolume = selectedVolume,
                        book = selectedBook,
                        isLoading = isLoading,
                        loadMessage = loadMessage,
                    ),
                    tailPointsRight = false,
                    horizontalPadding = 14.dp,
                    verticalPadding = 14.dp,
                    modifier = Modifier
                        .offset(x = (maxWidth - 166.dp).coerceAtLeast(0.dp), y = 374.dp)
                        .size(width = 166.dp, height = 122.dp)
                        .testTag("wushuhuan_status_bubble")
                        .then(
                            if (canOpenContextualHoushan) {
                                Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "前往后山问道",
                                    ) {
                                    userSelectedBook = true
                                    onOpenHoushan(selectedLessonId)
                                }
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "前往后山问道"
                                    }
                            } else {
                                Modifier.semantics(mergeDescendants = true) {}
                            },
                        ),
                )
            }
        }

        if (detailBook != null) {
            AncientManualBookOverlay(
                selectedBook = detailBook,
                volumeBooks = learningOverview?.books.orEmpty().filter {
                    it.volumeNo == detailBook.volumeNo
                },
                detail = manualDetail,
                loading = isLoading,
                message = loadMessage,
                onLoadManualDetail = onLoadManualDetail,
                onDismiss = { detailBookId = null },
                onOpenReader = { book ->
                    onOpenReader(book.volumeNo, book.manualPageId, false)
                },
                onOpenTrial = onOpenTrial,
                onUseInCreation = onUseInCreation,
            )
        }

        // 古籍容器自行处理“内容页 → 五招目录 → 合上书”；未打开古籍时才退出悟书环。
        BackHandler(enabled = detailBookId == null, onBack = onBack)
    }
}

private fun learningStateRank(state: String): Int = when (state) {
    "TEACHING" -> 4
    "MASTERED" -> 3
    "LEARNED" -> 2
    "DISCOVERED" -> 1
    else -> 0
}

@Composable
private fun SelectableBook(
    bookSlot: BookSlot,
    visualSlot: InterpolatedBookSlot,
    book: LearningBookDto?,
    selected: Boolean,
    sceneScale: Float,
    onClick: () -> Unit,
) {
    val focus = (1f - visualSlot.distanceFromCenter / 1.8f).coerceIn(0f, 1f)
    val unseen = book?.state == "UNSEEN" || book == null
    val visualAlpha = bookVisualAlpha(
        distanceFromCenter = visualSlot.distanceFromCenter,
        unseen = unseen,
    )
    val visualScale = 1f + 0.07f * focus
    val imageWidth = visualSlot.width * sceneScale
    val imageHeight = visualSlot.height * sceneScale
    val imageX = visualSlot.x * sceneScale
    val imageY = visualSlot.y * sceneScale
    val touchWidth = if (imageWidth < 48.dp) 48.dp else imageWidth
    val touchHeight = if (imageHeight < 48.dp) 48.dp else imageHeight
    val description = buildString {
        append("第${bookSlot.volumeNo}卷")
        book?.let {
            append(displayVolumeTitle(it))
            append("，")
            append(if (it.reviewDue) "待温习，原状态${it.stateLabel}" else it.stateLabel)
        } ?: append("，暂无数据")
        when {
            selected && book != null && book.state != "UNSEEN" ->
                append("，已在正中，轻触查看详情")
            selected && book != null -> append("，已在正中，轻触查看引导")
            selected -> append("，已在正中，暂无数据")
            else -> append("，轻触转到正中")
        }
    }

    Box(
        modifier = Modifier
            .offset(
                x = imageX - (touchWidth - imageWidth) / 2,
                y = imageY - (touchHeight - imageHeight) / 2,
            )
            .size(touchWidth, touchHeight)
            .zIndex(20f - visualSlot.distanceFromCenter)
            .testTag("wushuhuan_book_${bookSlot.volumeNo}")
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(bookSlot.imageRes),
            contentDescription = null,
            colorFilter = if (unseen) unseenBookColorFilter else null,
            modifier = Modifier
                .size(imageWidth, imageHeight)
                .rotate(visualSlot.rotation)
                .graphicsLayer {
                    alpha = visualAlpha
                    scaleX = visualScale
                    scaleY = visualScale
                },
            contentScale = ContentScale.FillBounds,
        )
    }
}

private fun statusCopy(
    selectedVolume: Int,
    book: LearningBookDto?,
    isLoading: Boolean,
    loadMessage: String?,
): String = when {
    isLoading && book == null -> "正在查看你的秘籍……"
    book == null && !loadMessage.isNullOrBlank() -> "秘籍状态暂未读到\n稍后再试"
    book == null -> "第${selectedVolume}卷 · 暂无秘籍\n轻触气泡去后山"
    book.state == "UNSEEN" -> "第${book.volumeNo}卷${displayVolumeTitle(book)} · 未闻\n轻触气泡去后山"
    book.reviewDue -> "第${book.volumeNo}卷${displayVolumeTitle(book)} · 待温习\n轻触正中秘籍"
    else -> "第${book.volumeNo}卷${displayVolumeTitle(book)} · ${book.stateLabel}\n轻触正中秘籍"
}

private fun displayVolumeTitle(book: LearningBookDto): String {
    val raw = book.volumeTitle.trim()
    return Regex("《[^》]+》").find(raw)?.value
        ?: raw.takeIf { it.isNotBlank() }?.let { "《$it》" }
        ?: "《未命名秘籍》"
}
