package com.jueqiao.jianghu.ui.screens.xiulian

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val GuideBubbleEnterDurationMillis = 260
private const val GuideBubbleExitDurationMillis = 220
private const val XiulianEntranceDelayMillis = 550L
private const val XiulianEntranceDurationMillis = 680
private val XiulianEntranceOffset = 56.dp

private enum class XiulianGuideStage {
    AwaitingFirstTap,
    ShowingBubble,
    ShowingEntrance,
}

/**
 * 修炼页 — 基于 Figma 节点 301-1242。
 * 布局:xiulian.png 全屏背景 + Group 17.png 左侧装饰(35.84, 501, 138.16×245)。
 */
@Composable
fun XiulianScreen(
    onBack: () -> Unit = {},
    onOpenGunlun1: () -> Unit = {},
    learningOverview: LearningOverviewDto? = null,
) {
    var guideStage by rememberSaveable {
        mutableStateOf(XiulianGuideStage.AwaitingFirstTap)
    }
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleMovement = remember { Animatable(0f) }
    val xiulianAlpha = remember { Animatable(0f) }
    val xiulianMovement = remember { Animatable(0f) }
    val recommendation = learningOverview?.books?.firstOrNull {
        it.manualPageId == learningOverview.recommendedLessonId
    }

    LaunchedEffect(guideStage, animationsEnabled) {
        when (guideStage) {
            XiulianGuideStage.AwaitingFirstTap -> {
                bubbleAlpha.snapTo(0f)
                bubbleMovement.snapTo(0f)
                xiulianAlpha.snapTo(0f)
                xiulianMovement.snapTo(0f)
            }

            XiulianGuideStage.ShowingBubble -> {
                if (!animationsEnabled) {
                    bubbleAlpha.snapTo(1f)
                    bubbleMovement.snapTo(1f)
                } else {
                    coroutineScope {
                        launch {
                            bubbleAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = GuideBubbleEnterDurationMillis,
                                    easing = LinearEasing,
                                ),
                            )
                        }
                        launch {
                            bubbleMovement.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = GuideBubbleEnterDurationMillis,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }
                }
            }

            XiulianGuideStage.ShowingEntrance -> {
                if (!animationsEnabled) {
                    bubbleAlpha.snapTo(0f)
                    bubbleMovement.snapTo(0f)
                    xiulianAlpha.snapTo(1f)
                    xiulianMovement.snapTo(1f)
                } else {
                    coroutineScope {
                        launch {
                            bubbleAlpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = GuideBubbleExitDurationMillis,
                                    easing = LinearEasing,
                                ),
                            )
                        }
                        launch {
                            bubbleMovement.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = GuideBubbleExitDurationMillis,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }
                    delay(XiulianEntranceDelayMillis)
                    coroutineScope {
                        launch {
                            xiulianAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = XiulianEntranceDurationMillis,
                                    easing = LinearEasing,
                                ),
                            )
                        }
                        launch {
                            xiulianMovement.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = XiulianEntranceDurationMillis,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图：上方竹林随风轻摆，建筑与石阶保持稳定。
        XiulianWindBackground(modifier = Modifier.fillMaxSize())

        // 内容层(避开系统导航条)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            val guideBubbleWidth = minOf(280.dp, (maxWidth - 32.dp).coerceAtLeast(180.dp))
            val guideBubbleX = minOf(
                118.dp,
                (maxWidth - guideBubbleWidth - 16.dp).coerceAtLeast(16.dp),
            )
            // Group 17.png(左侧装饰,135.84, 501, 138.16×245)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group17),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 35.84.dp, y = 501.dp)
                .size(width = 138.16.dp, height = 245.dp),
            contentScale = ContentScale.Fit,
        )

        // 第一次点击显示气泡，第二次点击关闭气泡并展示原修炼入口。
        if (guideStage != XiulianGuideStage.ShowingEntrance) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = if (
                            guideStage == XiulianGuideStage.AwaitingFirstTap
                        ) {
                            "显示修炼引导"
                        } else {
                            "关闭引导并显示修炼入口"
                        }
                        role = Role.Button
                    }
                    .clickable {
                        when (guideStage) {
                            XiulianGuideStage.AwaitingFirstTap -> {
                                guideStage = XiulianGuideStage.ShowingBubble
                            }
                            XiulianGuideStage.ShowingBubble -> {
                                if (!animationsEnabled || bubbleAlpha.value >= 0.99f) {
                                    guideStage = XiulianGuideStage.ShowingEntrance
                                }
                            }
                            XiulianGuideStage.ShowingEntrance -> Unit
                        }
                    },
            )
        }

        // 与首页引导页统一使用小笺气泡样式。
        Box(
            modifier = Modifier
                .offset(x = guideBubbleX, y = 453.dp)
                .size(width = guideBubbleWidth, height = 118.dp),
        ) {
            HomeGuideBubble(
                text = recommendation?.let {
                    "下一招：${it.title}\n${learningOverview?.backMountain?.reason ?: "打开秘籍继续修炼"}"
                } ?: "这里便是修炼之地!研读秘籍、\n静心学习、参与试炼,一步步\n提升你的学识修为。",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = bubbleAlpha.value
                        translationY = (1f - bubbleMovement.value) * 10.dp.toPx()
                    },
            )
        }

        // 气泡退场后恢复原“修炼”组件；点击组件后才进入Gunlun1分流页。
        DecorButton(
            imageRes = R.drawable.img_xiulian_group128,
            text = "修炼",
            accessibilityLabel = "进入修炼页",
            x = 131.dp,
            y = 358.dp,
            width = 55.dp,
            height = 90.dp,
            entranceAlpha = xiulianAlpha.value,
            entranceTranslationY =
                (1f - xiulianMovement.value) * with(LocalDensity.current) {
                    XiulianEntranceOffset.toPx()
                },
            entranceEnabled =
                xiulianAlpha.value >= 0.99f && xiulianMovement.value >= 0.99f,
            onClick = onOpenGunlun1,
        )

        // 左上角:返回按钮(Return.png,点击回到首页1)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 42.dp)
                .size(48.dp)
                .semantics {
                    contentDescription = "返回首页"
                    role = Role.Button
                }
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_xiulian_return),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        }
    }
}
