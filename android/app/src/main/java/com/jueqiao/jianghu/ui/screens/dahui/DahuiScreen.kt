package com.jueqiao.jianghu.ui.screens.dahui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val GuideBubbleEnterDurationMillis = 260
private const val GuideBubbleExitDurationMillis = 220
private const val DahuiEntranceDelayMillis = 550L
private const val DahuiEntranceDurationMillis = 680
private val DahuiEntranceOffset = 56.dp

private enum class DahuiGuideStage {
    AwaitingFirstTap,
    BubbleVisible,
    Ready,
}

/**
 * 大会引导页：第一次点击页面显示提示气泡，点击气泡后显示“大会”入口。
 * 入口尺寸与点击反馈复用首页 DecorButton。
 */
@Composable
fun DahuiScreen(
    onBack: () -> Unit = {},
    onOpenArena: () -> Unit = {},
) {
    var guideStage by remember { mutableStateOf(DahuiGuideStage.AwaitingFirstTap) }
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleMovement = remember { Animatable(0f) }
    val entranceAlpha = remember { Animatable(0f) }
    val entranceMovement = remember { Animatable(0f) }
    val guideInteractionSource = remember { MutableInteractionSource() }
    val entranceOffsetPx = with(LocalDensity.current) { DahuiEntranceOffset.toPx() }

    LaunchedEffect(guideStage) {
        when (guideStage) {
            DahuiGuideStage.AwaitingFirstTap -> {
                bubbleAlpha.snapTo(0f)
                bubbleMovement.snapTo(0f)
                entranceAlpha.snapTo(0f)
                entranceMovement.snapTo(0f)
            }
            DahuiGuideStage.BubbleVisible -> {
                coroutineScope {
                    launch {
                        bubbleAlpha.animateTo(
                            1f,
                            tween(GuideBubbleEnterDurationMillis, easing = LinearEasing),
                        )
                    }
                    launch {
                        bubbleMovement.animateTo(
                            1f,
                            tween(GuideBubbleEnterDurationMillis, easing = FastOutSlowInEasing),
                        )
                    }
                }
            }
            DahuiGuideStage.Ready -> {
                coroutineScope {
                    launch {
                        bubbleAlpha.animateTo(
                            0f,
                            tween(GuideBubbleExitDurationMillis, easing = LinearEasing),
                        )
                    }
                    launch {
                        bubbleMovement.animateTo(
                            0f,
                            tween(GuideBubbleExitDurationMillis, easing = FastOutSlowInEasing),
                        )
                    }
                }
                delay(DahuiEntranceDelayMillis)
                coroutineScope {
                    launch {
                        entranceAlpha.animateTo(
                            1f,
                            tween(DahuiEntranceDurationMillis, easing = LinearEasing),
                        )
                    }
                    launch {
                        entranceMovement.animateTo(
                            1f,
                            tween(DahuiEntranceDurationMillis, easing = FastOutSlowInEasing),
                        )
                    }
                }
            }
        }
    }

    BackHandler(enabled = true, onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_dahui_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 引导阶段整页可点：第一次点击显示气泡，第二次点击关闭气泡并展示入口。
            if (guideStage != DahuiGuideStage.Ready) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = guideInteractionSource,
                            indication = null,
                        ) {
                            guideStage = when (guideStage) {
                                DahuiGuideStage.AwaitingFirstTap -> DahuiGuideStage.BubbleVisible
                                DahuiGuideStage.BubbleVisible -> DahuiGuideStage.Ready
                                DahuiGuideStage.Ready -> DahuiGuideStage.Ready
                            }
                        },
                )
            }

            // 熊猫始终保留在引导页上，只有气泡按引导步骤显隐。
            Image(
                painter = painterResource(R.drawable.img_dahui_group127),
                contentDescription = "大会引导熊猫",
                modifier = Modifier
                    .offset(x = 150.dp, y = 597.dp)
                    .size(width = 241.dp, height = 285.dp),
                contentScale = ContentScale.Fit,
            )

            // 气泡只负责展示；第二次点击屏幕任意位置都可让它退场。
            Box(
                modifier = Modifier
                    .offset(x = 56.dp, y = 563.dp)
                    .size(width = 158.dp, height = 80.dp)
                    .graphicsLayer {
                        alpha = bubbleAlpha.value
                        val progress = bubbleMovement.value
                        translationY = (1f - progress) * 10.dp.toPx()
                        val scale = 0.94f + 0.06f * progress
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                HomeGuideBubble(
                    text = "前面就是演武场!准备好,\n就来一展你的本领吧。",
                    tailPointsRight = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 气泡消失后出现首页同款“大会”跳转组件。
            DecorButton(
                imageRes = R.drawable.img_dahui_o,
                text = "大会",
                x = 72.5.dp,
                y = 458.dp,
                width = 55.dp,
                height = 90.dp,
                entranceAlpha = entranceAlpha.value,
                entranceTranslationY =
                    (1f - entranceMovement.value) * entranceOffsetPx,
                entranceEnabled =
                    entranceAlpha.value >= 0.99f && entranceMovement.value >= 0.99f,
                onClick = onOpenArena,
            )

            // 返回按钮保持独立可点，不参与引导推进。
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 76.dp)
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_dahui_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
