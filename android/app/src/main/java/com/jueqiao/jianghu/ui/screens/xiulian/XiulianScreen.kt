package com.jueqiao.jianghu.ui.screens.xiulian

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import com.jueqiao.jianghu.ui.components.HomeQuickActions
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.screens.home.ProgressModal
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
 * 布局:xiulian.png 全屏背景 + Group 17.png 左侧装饰(35.84, 501, 138.16×245) +
 *      顶部 4 个快捷图标。
 */
@Composable
fun XiulianScreen(
    onBack: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
    onOpenManuals: () -> Unit = onOpenLuggage,
    onOpenLearning: () -> Unit = onOpenLuggage,
    onOpenTrials: () -> Unit = onOpenLuggage,
    onOpenGunlun1: () -> Unit = {},
    onOpenRecommendedManual: (String) -> Unit = {},
    learningOverview: LearningOverviewDto? = null,
    onOpenWendao: () -> Unit = onBack,
    onOpenSettings: () -> Unit = {},
    onOpenLetters: () -> Unit = {},
    hasUnreadLetters: Boolean = false,
) {
    var progressOpen by remember { mutableStateOf(false) }
    var dailyOpen    by remember { mutableStateOf(false) }
    var dailyStep    by remember { androidx.compose.runtime.mutableIntStateOf(1) }
    var guideStage by remember { mutableStateOf(XiulianGuideStage.AwaitingFirstTap) }
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleMovement = remember { Animatable(0f) }
    val xiulianAlpha = remember { Animatable(0f) }
    val xiulianMovement = remember { Animatable(0f) }
    val recommendation = learningOverview?.books?.firstOrNull {
        it.manualPageId == learningOverview.recommendedLessonId
    }

    LaunchedEffect(guideStage) {
        when (guideStage) {
            XiulianGuideStage.AwaitingFirstTap -> Unit

            XiulianGuideStage.ShowingBubble -> coroutineScope {
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

            XiulianGuideStage.ShowingEntrance -> {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图：上方竹林随风轻摆，建筑与石阶保持稳定。
        XiulianWindBackground(modifier = Modifier.fillMaxSize())

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // Group 17.png(左侧装饰,135.84, 501, 138.16×245)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group17),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 35.84.dp, y = 501.dp)
                .size(width = 138.16.dp, height = 245.dp),
            contentScale = ContentScale.Fit,
        )

        // 第一次点击显示气泡，第二次点击关闭气泡并进入修炼入口阶段。
        if (guideStage != XiulianGuideStage.ShowingEntrance) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        when (guideStage) {
                            XiulianGuideStage.AwaitingFirstTap -> {
                                guideStage = XiulianGuideStage.ShowingBubble
                            }
                            XiulianGuideStage.ShowingBubble -> {
                                if (bubbleAlpha.value >= 0.99f) {
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
                .offset(x = 118.dp, y = 453.dp)
                .size(width = 280.dp, height = 118.dp),
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

        // 气泡退场后，修炼入口复刻首页四个一级入口的入场与点击效果。
        DecorButton(
            imageRes = R.drawable.img_xiulian_group128,
            text = "修炼",
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
                .offset(x = 16.dp, y = 50.dp)
                .size(32.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_xiulian_return),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
            )
        }

        // 所有页面共用的顶部快捷入口：问道、修为、书信、设置。
        HomeQuickActions(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            onOpenWendao = onOpenWendao,
            onOpenCultivation = { progressOpen = true },
            onOpenLetters = onOpenLetters,
            onOpenSettings = onOpenSettings,
            hasUnreadLetters = hasUnreadLetters,
        )
        }
    }

    // 学习进度弹窗由顶部“修为”入口触发。
    if (progressOpen) {
        ProgressModal(
            onClose       = { progressOpen = false },
            onOpenDaily   = { dailyOpen = true; progressOpen = false },
            onOpenLuggage = onOpenLuggage,
        )
    }

    // 每日问题气泡(支持 2 步切换,第3 次点击关闭)
    if (dailyOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    if (dailyStep == 1) dailyStep = 2 else dailyOpen = false
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (dailyStep == 1)
                    "...去找找秘籍，看看有没有答案"
                else
                    "生活问题推荐:\n机器人为什么会认错物体?",
                color = Color.Black,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(20.dp),
            )
        }
    }
}
