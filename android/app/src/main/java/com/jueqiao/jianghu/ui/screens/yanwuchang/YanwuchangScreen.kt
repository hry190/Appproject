package com.jueqiao.jianghu.ui.screens.yanwuchang

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.ResponsiveDesignCanvas
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.screens.home.HomeWindBackground
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val GuideBubbleEnterDurationMillis = 260
private const val GuideBubbleExitDurationMillis = 220
private const val EntrancesDelayMillis = 550L
private const val EntrancesDurationMillis = 680
private val EntrancesOffset = 56.dp

private enum class YanwuchangGuideStage {
    AwaitingFirstTap,
    BubbleVisible,
    EntrancesVisible,
}

/**
 * 演武场首页 — 简单版(用 室内家园要求 1.png 作全屏背景 + 左上返回按钮)。
 * 背景图原始尺寸 412×917,使用 ContentScale.Crop 适配任意屏幕。
 */
@Composable
fun YanwuchangScreen(
    onBack: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenYanwuchangVideo: () -> Unit = {},
) {
    var guideStage by remember { mutableStateOf(YanwuchangGuideStage.AwaitingFirstTap) }
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleMovement = remember { Animatable(0f) }
    val entrancesAlpha = remember { Animatable(0f) }
    val entrancesMovement = remember { Animatable(0f) }
    val guideInteractionSource = remember { MutableInteractionSource() }
    val entrancesOffsetPx = with(LocalDensity.current) { EntrancesOffset.toPx() }

    LaunchedEffect(guideStage) {
        when (guideStage) {
            YanwuchangGuideStage.AwaitingFirstTap -> {
                bubbleAlpha.snapTo(0f)
                bubbleMovement.snapTo(0f)
                entrancesAlpha.snapTo(0f)
                entrancesMovement.snapTo(0f)
            }
            YanwuchangGuideStage.BubbleVisible -> coroutineScope {
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
            YanwuchangGuideStage.EntrancesVisible -> {
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
                delay(EntrancesDelayMillis)
                coroutineScope {
                    launch {
                        entrancesAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = EntrancesDurationMillis,
                                easing = LinearEasing,
                            ),
                        )
                    }
                    launch {
                        entrancesMovement.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = EntrancesDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    }
                }
            }
        }
    }

    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到大会页)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 复用首页同一套风动、光尘与 11 片落叶参数，仅替换背景和植被蒙版。
        HomeWindBackground(
            backgroundRes = R.drawable.img_yanwuchang_bg,
            windMaskRes = R.drawable.img_yanwuchang_wind_mask,
            modifier = Modifier.fillMaxSize(),
        )

        // 引导阶段由窗口级点击层接管：首次显示气泡；气泡完整出现后再次点击，
        // 气泡先退场，再让两个入口按首页节奏淡入并上移。
        // 该层放在响应式设计画布下方，既覆盖状态栏/导航栏内边距区域，
        // 又不会抢走画布内返回按钮的点击事件。
        if (guideStage != YanwuchangGuideStage.EntrancesVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = guideInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = if (
                            guideStage == YanwuchangGuideStage.AwaitingFirstTap
                        ) {
                            "显示演武场介绍"
                        } else {
                            "关闭介绍并显示入口"
                        },
                    ) {
                        when (guideStage) {
                            YanwuchangGuideStage.AwaitingFirstTap -> {
                                guideStage = YanwuchangGuideStage.BubbleVisible
                            }
                            YanwuchangGuideStage.BubbleVisible -> {
                                if (bubbleAlpha.value >= 0.99f) {
                                    guideStage = YanwuchangGuideStage.EntrancesVisible
                                }
                            }
                            YanwuchangGuideStage.EntrancesVisible -> Unit
                        }
                    },
            )
        }

        // 内容层(避开系统导航条)
        ResponsiveDesignCanvas(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 左上角返回按钮
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

            // 角色插画(未标题-1 - 副本 (3) 4.png, X=82, Y=599, W=141, H=260)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_panda),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 82.dp, y = 599.dp)
                    .size(width = 141.dp, height = 260.dp),
                contentScale = ContentScale.Fit,
            )

            // 两个入口统一使用首页的 55×90 规格、最终不透明度与点击反馈。
            DecorButton(
                imageRes = R.drawable.img_yanwuchang_un50,
                text = "大会",
                x = 264.dp,
                y = 489.dp,
                width = 55.dp,
                height = 90.dp,
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY =
                    (1f - entrancesMovement.value) * entrancesOffsetPx,
                entranceEnabled =
                    entrancesAlpha.value >= 0.99f && entrancesMovement.value >= 0.99f,
                onClick = onOpenDahui,
            )

            DecorButton(
                imageRes = R.drawable.img_yanwuchang_un50_1,
                text = "作品",
                x = 18.dp,
                y = 494.dp,
                width = 55.dp,
                height = 90.dp,
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY =
                    (1f - entrancesMovement.value) * entrancesOffsetPx,
                entranceEnabled =
                    entrancesAlpha.value >= 0.99f && entrancesMovement.value >= 0.99f,
                onClick = onOpenYanwuchangVideo,
            )

            // 与首页、大会引导页复用同一小笺气泡；只在第一次点击后显示。
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 500.dp)
                    .size(width = 224.dp, height = 108.dp)
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
                    text = "这里有作品和比拼可供选择哦，\n快去看看吧！",
                    tailPointsRight = true,
                    modifier = Modifier.fillMaxSize(),
                    horizontalPadding = 32.dp,
                    verticalPadding = 21.dp,
                )
            }
        }
    }
}
