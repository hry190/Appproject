package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 文件级常量 —— FocusCloudBand 的 drift/jitter 都用
private val TWO_PI = (2.0 * PI).toFloat()

// 冷青 #A9C3C0:§11 §23-§26 渐变云沿用的高频冷色,
// 在暖色背景(后山1/2 的试炼图)上"冷暖对立"提高对比度
private val FocusCloudColor = Color(0xFFA9C3C0)

/**
 * 后山 3 页共同的"聚焦前景飘带"组件 —— 09-16 §43 提取
 *
 * 程序化径向渐变 + canvas 非等比缩放,出图像水墨晕染;**不建 render layer**
 *
 * 4 层独立动画(共享同一个 progress):
 *  - 慢速 sin 漂移(periodMs 控制,典型 9-13s)
 *  - 高频抖动(jitter 1900ms / ±3 dp X / ±1.5 dp Y,§11-§15 迭代后稳定值)
 *  - 整体 alpha 脉动(baseAlpha ± alphaAmp)
 *  - 缩放呼吸(±12%)
 *
 * 调用方传位置 + 尺寸 + 周期;颜色固定为冷青(项目 §23-§26 沿用色)
 *
 * 后山3 用在中下 + 左下各一处;后山1/2 用同样的两个调用,位置参数适配各自拆招心法
 *
 * 性能:每帧零重组 —— 位置 layout 阶段读、alpha layer 阶段读、缩放 draw 阶段读
 */
@Composable
fun FocusCloudBand(
    xOffset: Float,
    yOffset: Float,
    widthDp: Float,
    heightDp: Float,
    amplitudeX: Float,
    amplitudeY: Float,
    baseAlpha: Float,
    alphaAmp: Float,
    periodMs: Int,
) {
    val progress by rememberInfiniteTransition(label = "focusCloudBand")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = periodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "focusCloudBandP",
        )
    // 09-16 §18 抖动参数:周期 1900 → 3000 ms (0.53 → 0.33 Hz,频率再小一些);
    // 振幅 ±3 → ±4.5 dp X / ±1.5 → ±2.5 dp Y (大幅增大,单次颤动更"重量感")
    val jitter by rememberInfiniteTransition(label = "fcJitter")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "fcJitterP",
        )
    Box(
        modifier = Modifier
            .offset {
                val a = progress * TWO_PI
                val jt = jitter * TWO_PI * 2f  // 2 cycles / 3000ms ≈ 0.33 Hz
                IntOffset(
                    (xOffset + sin(a) * amplitudeX + sin(jt) * 4.5f).dp.roundToPx(),
                    (yOffset + cos(a) * amplitudeY + cos(jt) * 2.5f).dp.roundToPx(),
                )
            }
            .size(width = widthDp.dp, height = heightDp.dp)
            .drawWithCache {
                val r = size.minDimension / 2f
                val brush = Brush.radialGradient(
                    colors = listOf(
                        FocusCloudColor,
                        FocusCloudColor.copy(alpha = 0.55f),
                        FocusCloudColor.copy(alpha = 0f),
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = r,
                )
                val stretchX = size.width / size.minDimension
                val stretchY = size.height / size.minDimension
                onDrawBehind {
                    val a = progress * TWO_PI
                    val alpha = (baseAlpha + sin(a + 0.41f) * alphaAmp).coerceIn(0f, 1f)
                    val breathe = 1f + cos(a) * 0.12f
                    scale(
                        scaleX = stretchX * breathe,
                        scaleY = stretchY * breathe,
                        pivot = center,
                    ) {
                        drawCircle(brush = brush, radius = r, center = center, alpha = alpha)
                    }
                }
            },
    )
}
