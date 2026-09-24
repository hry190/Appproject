package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import com.jueqiao.jianghu.luggage.LuggageResponseDto
import com.jueqiao.jianghu.ui.components.EdgeToEdgeScreen
import com.jueqiao.jianghu.ui.components.HomeQuickActions
import com.jueqiao.jianghu.ui.components.HomeQuickActionsLayout
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object HomePandaLayout {
    val X = 107.dp
    val Y = 478.dp
    val Width = 200.dp
    val Height = 256.dp
}

private const val QuickActionsEntranceDelayMillis = 550L
private const val QuickActionsEntranceDurationMillis = 1_500
private val QuickActionsEntranceOffset = 56.dp
private const val PrimaryEntranceDurationMillis = 680
private const val PrimaryEntranceStaggerMillis = 280L
private const val DecorButtonSheenDurationMillis = 440
private enum class PrimaryHomeAction {
    Luggage,
    Wushuhuan,
    Dahui,
    Zaowu,
}

private class PrimaryActionEntrance(initialValue: Float) {
    val alpha = Animatable(initialValue)
    val movement = Animatable(initialValue)
}

private fun PrimaryActionEntrance.isInteractive(): Boolean =
    alpha.value >= 0.99f && movement.value >= 0.99f

private fun PrimaryActionEntrance.translationY(offsetPx: Float): Float =
    (1f - movement.value) * offsetPx

private val DecorButtonJadeOutlineOffsets = listOf(
    (-1.5).dp to 0.dp,
    1.5.dp to 0.dp,
    0.dp to (-1.5).dp,
    0.dp to 1.5.dp,
    (-1.1).dp to (-1.1).dp,
    1.1.dp to (-1.1).dp,
    (-1.1).dp to 1.1.dp,
    1.1.dp to 1.1.dp,
)
private val DecorButtonGoldOutlineOffsets = listOf(
    (-0.65).dp to 0.dp,
    0.65.dp to 0.dp,
    0.dp to (-0.65).dp,
    0.dp to 0.65.dp,
)

/**
 * 首页1 — 点击首页后跳转的次页。
 * 布局：背景竹林 + 熊猫 + 4 个装饰横幅按钮(行囊/修炼/大会/作品)。
 * 位置/尺寸(代码 L161-195,dp):
 *   行囊(119,610,55,90)、修炼(83,234,55,90)、
 *   大会(156,351,55,90)、作品(300,350,55,90)。
 */
@Composable
fun Home1Screen(
    onOpenXiulian: () -> Unit = {},
    onOpenWendao: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    dahuiEnabled: Boolean = true,
    onOpenSettings: () -> Unit = {},
    onOpenLetters: () -> Unit = {},
    hasUnreadLetters: Boolean = false,
    animateQuickActionsEntrance: Boolean = false,
    quickActionsEntranceReady: Boolean = true,
    onQuickActionsEntranceConsumed: () -> Unit = {},
    progressSnapshot: LuggageResponseDto? = null,
    learningOverview: LearningOverviewDto? = null,
    progressLoading: Boolean = false,
    progressMessage: String? = null,
    onRefreshProgress: () -> Unit = {},
    onOpenRecommendedManual: (String) -> Unit = {},
) {
    var progressOpen by remember { mutableStateOf(false) }
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val density = LocalDensity.current
    val statusBarTop = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }
    val shouldAnimateQuickActions = remember { animateQuickActionsEntrance }
    val quickActionsAlpha = remember {
        Animatable(if (shouldAnimateQuickActions) 0f else 1f)
    }
    val quickActionsMovement = remember {
        Animatable(if (shouldAnimateQuickActions) 0f else 1f)
    }
    val primaryEntranceOrder = remember {
        PrimaryHomeAction.entries.shuffled()
    }
    val primaryActionEntrances = remember {
        List(PrimaryHomeAction.entries.size) {
            PrimaryActionEntrance(if (shouldAnimateQuickActions) 0f else 1f)
        }
    }

    LaunchedEffect(shouldAnimateQuickActions, quickActionsEntranceReady, animationsEnabled) {
        if (shouldAnimateQuickActions && quickActionsEntranceReady) {
            onQuickActionsEntranceConsumed()
            if (!animationsEnabled) {
                quickActionsAlpha.snapTo(1f)
                quickActionsMovement.snapTo(1f)
                primaryActionEntrances.forEach { entrance ->
                    entrance.alpha.snapTo(1f)
                    entrance.movement.snapTo(1f)
                }
            } else {
                delay(QuickActionsEntranceDelayMillis)
                coroutineScope {
                    launch {
                        quickActionsAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = QuickActionsEntranceDurationMillis,
                                easing = LinearEasing,
                            ),
                        )
                    }
                    launch {
                        quickActionsMovement.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = QuickActionsEntranceDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    }
                    primaryEntranceOrder.forEachIndexed { sequenceIndex, action ->
                        val entrance = primaryActionEntrances[action.ordinal]
                        launch {
                            delay(sequenceIndex * PrimaryEntranceStaggerMillis)
                            coroutineScope {
                                launch {
                                    entrance.alpha.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = PrimaryEntranceDurationMillis,
                                            easing = LinearEasing,
                                        ),
                                    )
                                }
                                launch {
                                    entrance.movement.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = PrimaryEntranceDurationMillis,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val homeEntrancesInteractive =
        quickActionsAlpha.value >= 0.99f && quickActionsMovement.value >= 0.99f
    val quickActionsOffsetPx = with(density) {
        QuickActionsEntranceOffset.toPx()
    }
    val homeEntrancesAlpha = quickActionsAlpha.value
    val homeEntrancesTranslationY =
        (1f - quickActionsMovement.value) * quickActionsOffsetPx
    val luggageEntrance = primaryActionEntrances[PrimaryHomeAction.Luggage.ordinal]
    val wushuhuanEntrance = primaryActionEntrances[PrimaryHomeAction.Wushuhuan.ordinal]
    val dahuiEntrance = primaryActionEntrances[PrimaryHomeAction.Dahui.ordinal]
    val zaowuEntrance = primaryActionEntrances[PrimaryHomeAction.Zaowu.ordinal]

    EdgeToEdgeScreen(
        background = {
            HomeWindBackground(
                modifier = Modifier.fillMaxSize(),
            )
        },
    ) {
        // 首页专属快捷入口面板；抵消父容器已经添加的状态栏顶部内边距。
        HomeQuickActions(
            onOpenWendao = {
                if (homeEntrancesInteractive) onOpenWendao()
            },
            onOpenCultivation = {
                if (homeEntrancesInteractive) {
                    progressOpen = true
                    onRefreshProgress()
                }
            },
            onOpenLetters = {
                if (homeEntrancesInteractive) onOpenLetters()
            },
            onOpenSettings = {
                if (homeEntrancesInteractive) onOpenSettings()
            },
            hasUnreadLetters = hasUnreadLetters,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = HomeQuickActionsLayout.EndOffset,
                    y = HomeQuickActionsLayout.TopOffset - statusBarTop,
                )
                .graphicsLayer {
                    alpha = homeEntrancesAlpha
                    translationY = homeEntrancesTranslationY
                },
        )

        // 用户提供的无行囊叉腰熊猫；行囊继续使用下方独立组件。
        Box(
            modifier = Modifier
                .offset(x = HomePandaLayout.X, y = HomePandaLayout.Y)
                .size(width = HomePandaLayout.Width, height = HomePandaLayout.Height),
        ) {
            Image(
                painter = painterResource(R.drawable.img_home1_panda_waist),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        // 行囊与另外三个入口复用相同组件、宽高和文字样式。
        DecorButton(
            imageRes = R.drawable.img_home1_btn1,
            text = "行囊",
            x = 119.dp, y = 610.dp,
            width = 55.dp, height = 90.dp,
            entranceAlpha = luggageEntrance.alpha.value,
            entranceTranslationY = luggageEntrance.translationY(quickActionsOffsetPx),
            entranceEnabled = luggageEntrance.isInteractive(),
            onClick = onOpenLuggage,
        )

        // 其余装饰横幅按钮:贴图 + 竖排文字(点击区为 Box)

        // 修炼 (2.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn2,
            text = "修炼",
            accessibilityLabel = "进入修炼引导页",
            x = 83.dp, y = 234.dp,
            width = 55.dp, height = 90.dp,
            entranceAlpha = wushuhuanEntrance.alpha.value,
            entranceTranslationY = wushuhuanEntrance.translationY(quickActionsOffsetPx),
            entranceEnabled = wushuhuanEntrance.isInteractive(),
            onClick = onOpenXiulian,
        )

        // 大会 (3.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn3,
            text = "大会",
            x = 156.dp, y = 351.dp,
            width = 55.dp, height = 90.dp,
            entranceAlpha = dahuiEntrance.alpha.value * if (dahuiEnabled) 1f else 0.45f,
            entranceTranslationY = dahuiEntrance.translationY(quickActionsOffsetPx),
            entranceEnabled = dahuiEntrance.isInteractive() && dahuiEnabled,
            onClick = onOpenDahui,
        )

        // 工坊 (4.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn4,
            text = "工坊",
            accessibilityLabel = "进入工坊",
            x = 300.dp, y = 350.dp,
            width = 55.dp, height = 90.dp,
            entranceAlpha = zaowuEntrance.alpha.value,
            entranceTranslationY = zaowuEntrance.translationY(quickActionsOffsetPx),
            entranceEnabled = zaowuEntrance.isInteractive(),
            onClick = onOpenZaowu,
        )
    }

    // 修为弹窗由顶部“修为”入口触发。
    if (progressOpen) {
        val recommendation = learningOverview?.books?.firstOrNull {
            it.manualPageId == learningOverview.recommendedLessonId
        }
        ProgressModal(
            onClose = { progressOpen = false },
            onOpenLuggage = {
                progressOpen = false
                onOpenLuggage()
            },
            onOpenRecommendedManual = { id ->
                progressOpen = false
                onOpenRecommendedManual(id)
            },
            snapshot = progressSnapshot,
            recommendation = recommendation,
            loading = progressLoading,
            message = progressMessage,
        )
    }
}

/**
 * 装饰横幅按钮:贴图背景 + 竖排中文叠在上层。
 * 整个 Box 是点击区,文字会居中显示。
 */
@Composable
fun DecorButton(
    imageRes: Int,
    text: String,
    accessibilityLabel: String = text,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    entranceAlpha: Float = 1f,
    entranceTranslationY: Float = 0f,
    entranceEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    var isClickSelected by remember { mutableStateOf(false) }
    val sheenProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val isActive = animationsEnabled && (isHovered || isPressed || isClickSelected)

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isClickSelected -> 1.035f
            isHovered -> 1.025f
            else -> 1f
        },
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 140, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "decorButtonScale",
    )
    val lift by animateDpAsState(
        targetValue = when {
            isClickSelected -> 2.dp
            isHovered -> 1.dp
            else -> 0.dp
        },
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 160, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "decorButtonLift",
    )
    val outlineAlpha by animateFloatAsState(
        targetValue = when {
            isClickSelected -> 0.2f
            isPressed -> 0.16f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 140, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "decorButtonOutline",
    )
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.07f else 0f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 160, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "decorButtonHighlight",
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFFF7DC) else Color(0xFFF4E6CF),
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 160, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "decorButtonTextColor",
    )
    val density = LocalDensity.current
    val liftPx = with(density) { lift.toPx() }

    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslationY
            }
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .hoverable(
                interactionSource = interactionSource,
                enabled = entranceEnabled,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = entranceEnabled,
                onClick = {
                    if (entranceEnabled && !isClickSelected) {
                        isClickSelected = true
                        if (!animationsEnabled) {
                            onClick()
                            isClickSelected = false
                        } else {
                            scope.launch {
                                sheenProgress.snapTo(0f)
                                sheenProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = DecorButtonSheenDurationMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                                // 保留各引导页原有节奏：流光完整播放后再切换页面。
                                onClick()
                                isClickSelected = false
                                sheenProgress.snapTo(0f)
                            }
                        }
                    }
                },
        ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = -liftPx
            },
            contentAlignment = Alignment.Center,
        ) {
            // 用透明贴图的多方向轻微偏移形成轮廓线，不使用任何模糊图层。
            DecorButtonJadeOutlineOffsets.forEach { (offsetX, offsetY) ->
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = offsetX, y = offsetY)
                        .alpha(outlineAlpha),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(Color(0xFFA8C77A)),
                )
            }
            DecorButtonGoldOutlineOffsets.forEach { (offsetX, offsetY) ->
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = offsetX, y = offsetY)
                        .alpha(outlineAlpha * 0.8f),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(Color(0xFFF2D58A)),
                )
            }

            // 原图与斜向流光在独立离屏层合成，SrcAtop 将流光限制在图标 Alpha 内。
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        if (animationsEnabled && isClickSelected) {
                            val centerX = size.width * (-0.45f + 1.9f * sheenProgress.value)
                            val bandHalfWidth = size.width * 0.22f
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x00FFF1B8),
                                        Color(0x66FFF1B8),
                                        Color(0x18F2D58A),
                                        Color.Transparent,
                                    ),
                                    start = Offset(
                                        x = centerX - bandHalfWidth,
                                        y = size.height,
                                    ),
                                    end = Offset(
                                        x = centerX + bandHalfWidth,
                                        y = 0f,
                                    ),
                                ),
                                blendMode = BlendMode.SrcAtop,
                            )
                        }
                    },
                contentScale = ContentScale.FillBounds,
            )

            // 低透明度暖白叠层模拟高光，不改变原素材。
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(highlightAlpha),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(Color(0xFFFFF1B8)),
            )

            // 上层竖排文字(向左偏移 4dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .offset(x = (-4).dp)
                    .padding(vertical = 6.dp),
            ) {
                text.forEach { ch ->
                    Text(
                        text = ch.toString(),
                        color = textColor,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            shadow = Shadow(
                                color = if (isActive) {
                                    Color(0xFF405126)
                                } else {
                                    Color(0xCC141E14)
                                },
                                blurRadius = if (isActive) 6f else 8f,
                            ),
                        ),
                    )
                }
            }
        }
    }
}
