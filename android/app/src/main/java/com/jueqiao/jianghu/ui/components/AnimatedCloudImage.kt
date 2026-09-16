package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 文件级常量
private val TWO_PI = (2.0 * PI).toFloat()

/**
 * 后山 3 页共同的"实图云朵 PNG 动画"组件 —— 09-16 §6 提取
 *
 * 与 [FocusCloudBand] 的程序化径向渐变不同:本函数用**现成云朵 PNG** 出图
 *
 * 5 层独立动画(共享同一个 progress,phase 偏移避免拍点重合):
 *  - 慢速 sin 漂移(位置 X/Y)
 *  - 整体 alpha 脉动(baseAlpha ± alphaAmp)
 *  - 缩放呼吸(graphicsLayer 隐含,无)
 *  - **白色脉冲高光**(§7) —— 在 PNG 上叠一层白色径向渐变
 *  - **高频抖动**(§11-§15) —— 1900ms / ±3 dp X / ±1.5 dp Y
 *
 * 调用方传位置 + 尺寸 + 周期 + 相位 + 振幅;颜色固定为 PNG 原色
 *
 * 后山3 用 3 朵(Ellipse 58/60/62)共享 1 个 progress;后山1/2 用同样 3 朵,位置适配各自拆招心法
 *
 * 性能:每帧零重组 —— 位置 layout 阶段读、整体 alpha layer 阶段读、
 * 白色脉冲 + 抖动都在 draw 阶段读
 */
@Composable
fun AnimatedCloudImage(
    painter: Painter,
    contentDescription: String?,
    xOffset: Float,
    yOffset: Float,
    widthDp: Float,
    heightDp: Float,
    progress: State<Float>,
    phase: Float,
    amplitudeX: Float,
    amplitudeY: Float,
    baseAlpha: Float,
    alphaAmp: Float,
) {
    // 09-16 §15 抖动参数:1900ms 周期 + ±3 dp X / ±1.5 dp Y(共享 FocusCloudBand 的硬编码)
    val jitter by rememberInfiniteTransition(label = "aciJitter")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "aciJitterP",
        )
    Box(
        modifier = Modifier
            .offset {
                val a = (progress.value + phase) * TWO_PI
                val jt = jitter * TWO_PI * 2f + phase * 7f  // 2 cycles / 1900ms × phase 偏移
                IntOffset(
                    (xOffset + sin(a) * amplitudeX + sin(jt) * 3f).dp.roundToPx(),
                    (yOffset + cos(a) * amplitudeY + cos(jt) * 1.5f).dp.roundToPx(),
                )
            }
            .size(width = widthDp.dp, height = heightDp.dp)
            .graphicsLayer {
                // 整体 alpha 脉动 —— 09-16 §8 范围 0.25~0.75
                val a = (progress.value + phase + 0.41f) * TWO_PI
                alpha = (baseAlpha + sin(a) * alphaAmp).coerceIn(0f, 1f)
            },
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        // §7 + §8 白色脉冲高光 —— 让云中心"发光/呼吸"
        // 参数:base 0.20,amp 0.16 → range 0.04~0.36,相位 +0.37 与位置/alpha 都错开
        Box(
            modifier = Modifier.fillMaxSize().drawWithCache {
                val r = size.minDimension / 2f
                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = r,
                )
                onDrawBehind {
                    val a = (progress.value + phase + 0.37f) * TWO_PI
                    val pulseAlpha = (0.20f + sin(a) * 0.16f).coerceIn(0f, 1f)
                    drawRect(brush = brush, alpha = pulseAlpha)
                }
            },
        )
    }
}
