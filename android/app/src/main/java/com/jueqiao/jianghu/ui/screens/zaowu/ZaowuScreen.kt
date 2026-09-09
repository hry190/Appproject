package com.jueqiao.jianghu.ui.screens.zaowu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val GuideBubbleEnterDurationMillis = 260
private const val GuideBubbleExitDurationMillis = 220
private const val WorkshopEntranceDelayMillis = 550L
private const val WorkshopEntranceDurationMillis = 680
private val WorkshopEntranceOffset = 56.dp

/**
 * 作品创作页 — 简单版(用作品创作.png 作全屏背景 + 左上返回按钮)。
 */
@Composable
fun ZaowuScreen(
    onBack: () -> Unit = {},
    onOpenGongfang: () -> Unit = {},
) {
    // 引导顺序：小笺出现并停留，用户点击全屏后才淡出并展示工坊入口。
    val guideDismissed = remember { mutableStateOf(false) }
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleMovement = remember { Animatable(0f) }
    val workshopAlpha = remember { Animatable(0f) }
    val workshopMovement = remember { Animatable(0f) }
    val density = LocalDensity.current
    val workshopEntranceOffsetPx = with(density) { WorkshopEntranceOffset.toPx() }

    LaunchedEffect(guideDismissed.value) {
        if (!guideDismissed.value) {
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
            return@LaunchedEffect
        }
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
        delay(WorkshopEntranceDelayMillis)
        coroutineScope {
            launch {
                workshopAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = WorkshopEntranceDurationMillis,
                        easing = LinearEasing,
                    ),
                )
            }
            launch {
                workshopMovement.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = WorkshopEntranceDurationMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }

    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(跳首页1)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 移除原图中会遮蔽地砖的厚重阴影，熊猫脚下改由独立接触阴影处理。
        Image(
            painter = painterResource(R.drawable.img_zaowu_bg_shadow_v2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 天窗丁达尔动效：雾光沿既有光束方向缓慢呼吸，小颗粒浮尘随气流漂移。
        // 整层不接收点击，并位于角色与引导 UI 下方。
        DirectionalTyndallEffect()

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
        // 贴脚的半透明接触阴影：保留石板纹理，不再铺一块大面积黑雾。
        PandaContactShadow(
            modifier = Modifier
                .offset(x = 245.dp, y = 781.dp)
                .size(width = 155.dp, height = 30.dp),
        )

        // 气泡停留期间，整页任意空白区域均可推进引导；返回按钮在更上层，保持独立可点。
        if (!guideDismissed.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (bubbleAlpha.value >= 0.99f) {
                            guideDismissed.value = true
                        }
                    },
            )
        }

        // 左上角返回按钮
        Box(
            modifier = Modifier
                .offset(x = 18.dp, y = 34.dp)
                .size(48.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_zaowu_return),
                contentDescription = "返回",
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // 角色插画：下移后让双脚压入背景阴影上缘，消除“悬空”观感。
        Image(
            painter = painterResource(R.drawable.img_zaowu_figure),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 230.dp, y = 528.dp)
                .size(width = 176.dp, height = 271.dp),
            contentScale = ContentScale.Fit,
        )

        // 与开局引导页复用同款小笺气泡与文字排版，尾巴自然指向熊猫。
        HomeGuideBubble(
            text = "快去工坊里头看看吧，\n一起来设计属于自己的作品吧",
            tailPointsRight = true,
            modifier = Modifier
                .offset(x = 130.dp, y = 398.dp)
                .size(width = 280.dp, height = 118.dp)
                .graphicsLayer {
                    alpha = bubbleAlpha.value
                    translationY = (1f - bubbleMovement.value) * 10.dp.toPx()
                },
        )

        // 气泡退场后，工坊入口复刻首页四个一级入口的淡入与上移入场节奏。
        DecorButton(
            imageRes = R.drawable.img_zaowu_51,
            text = "工坊",
            x = 55.dp,
            y = 510.dp,
            width = 55.dp,
            height = 90.dp,
            entranceAlpha = workshopAlpha.value,
            entranceTranslationY =
                (1f - workshopMovement.value) * workshopEntranceOffsetPx,
            entranceEnabled =
                workshopAlpha.value >= 0.99f && workshopMovement.value >= 0.99f,
            onClick = onOpenGongfang,
        )
        }
    }
}

private data class DustParticle(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val phase: Float,
    val opacity: Float,
    val driftXDp: Float,
    val driftYDp: Float,
)

@Composable
private fun DirectionalTyndallEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "zaowu-tyndall")
    val fogProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "zaowu-directional-fog",
    ).value
    val dustProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "zaowu-floating-dust",
    ).value
    val dustParticles = remember {
        val random = Random(91_573)
        List(38) {
            DustParticle(
                xFraction = random.nextFloat() * 0.84f + 0.08f,
                yFraction = random.nextFloat() * 0.82f + 0.10f,
                radiusDp = random.nextFloat() * 0.70f + 0.42f,
                phase = random.nextFloat(),
                opacity = random.nextFloat() * 0.27f + 0.23f,
                driftXDp = random.nextFloat() * 18f + 16f,
                driftYDp = random.nextFloat() * 30f + 32f,
            )
        }
    }
    val density = LocalDensity.current
    val resources = LocalContext.current.resources
    val fogImage = remember(resources) {
        ImageBitmap.imageResource(resources, R.drawable.img_zaowu_directional_fog_v2)
    }
    val fogAngle = fogProgress * 2f * PI.toFloat()
    val fogPulse = (sin(fogAngle - 0.4f) + 1f) * 0.5f
    val fogPrimaryX = with(density) { (sin(fogAngle) * 7f).dp.toPx() }
    val fogPrimaryY = with(density) { (cos(fogAngle) * 30f).dp.toPx() }
    val fogSecondaryX = with(density) { (sin(fogAngle + 1.8f) * 5.5f).dp.toPx() }
    val fogSecondaryY = with(density) { (cos(fogAngle + 1.8f) * 23f).dp.toPx() }
    val fogPrimaryAlpha = 0.145f + fogPulse * 0.070f
    val fogSecondaryAlpha = 0.070f + (1f - fogPulse) * 0.050f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(510.dp)
            .clipToBounds(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width.roundToInt()
            val canvasHeight = size.height.roundToInt()
            drawImage(
                image = fogImage,
                dstOffset = IntOffset(
                    x = fogPrimaryX.roundToInt(),
                    y = fogPrimaryY.roundToInt(),
                ),
                dstSize = IntSize(canvasWidth, canvasHeight),
                alpha = fogPrimaryAlpha,
                blendMode = BlendMode.Screen,
                filterQuality = FilterQuality.Medium,
            )

            val secondaryWidth = (canvasWidth * 1.035f).roundToInt()
            val secondaryHeight = (canvasHeight * 1.025f).roundToInt()
            drawImage(
                image = fogImage,
                dstOffset = IntOffset(
                    x = (fogSecondaryX - (secondaryWidth - canvasWidth) / 2f).roundToInt(),
                    y = (fogSecondaryY - (secondaryHeight - canvasHeight) / 2f).roundToInt(),
                ),
                dstSize = IntSize(secondaryWidth, secondaryHeight),
                alpha = fogSecondaryAlpha,
                blendMode = BlendMode.Screen,
                filterQuality = FilterQuality.Medium,
            )

            dustParticles.forEach { particle ->
                val progress = (dustProgress + particle.phase) % 1f
                // 粒子循环衔接处保持透明，避免从下方瞬间跳回时产生闪烁。
                val visibility = sin(PI.toFloat() * progress)
                    .coerceAtLeast(0f)
                    .pow(0.72f)
                val center = Offset(
                    x = size.width * particle.xFraction +
                        with(density) {
                            (
                                (progress - 0.5f) * particle.driftXDp +
                                    sin((progress + particle.phase) * 2f * PI.toFloat()) * 5f
                                ).dp.toPx()
                        },
                    y = size.height * particle.yFraction +
                        with(density) {
                            (
                                (progress - 0.5f) * particle.driftYDp +
                                    cos((progress + particle.phase) * 2f * PI.toFloat()) * 2.5f
                                ).dp.toPx()
                        },
                )
                val radius = with(density) { particle.radiusDp.dp.toPx() }
                val alpha = particle.opacity * visibility

                drawCircle(
                    color = Color(0xFFFFF4DC),
                    radius = radius * 1.65f,
                    center = center,
                    alpha = alpha * 0.24f,
                )
                drawCircle(
                    color = Color(0xFFFFF8E8),
                    radius = radius,
                    center = center,
                    alpha = alpha,
                )
            }
        }
    }
}

@Composable
private fun PandaContactShadow(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // 三层低透明椭圆提供柔和外缘；石板纹理会从所有层中清晰透出。
        drawOval(
            color = Color(0x1838322A),
            topLeft = Offset(0f, 0f),
            size = size,
        )
        drawOval(
            color = Color(0x2838322A),
            topLeft = Offset(size.width * 0.06f, size.height * 0.20f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.88f, size.height * 0.62f),
        )
        drawOval(
            color = Color(0x36352F27),
            topLeft = Offset(size.width * 0.18f, size.height * 0.38f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.34f),
        )

        // 两处略深的脚底接触阴影，避免角色看起来悬空。
        drawOval(
            color = Color(0x5A312C25),
            topLeft = Offset(size.width * 0.08f, size.height * 0.53f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.30f),
        )
        drawOval(
            color = Color(0x5A312C25),
            topLeft = Offset(size.width * 0.54f, size.height * 0.53f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.30f),
        )
    }
}
