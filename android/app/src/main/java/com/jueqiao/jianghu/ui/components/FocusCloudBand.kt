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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 文件级常量 —— FocusCloudBand 的 drift/jitter 都用
private val TWO_PI = (2.0 * PI).toFloat()

/**
 * 云元素的**横向运动模式**(2026-09-16 §21c/§21d,用户指令)。
 *
 * - [Oscillate] —— `x = xOffset + sin(2π·进度) × amplitudeX`,在基准点左右**往复摆动**,
 *   峰到峰 = 2×amplitudeX(200dp 即一屏宽),半周期 = 单程。
 * - [DriftWrap] —— **单向 左 → 右**:从屏幕左侧外走到右侧外,出屏后回绕到左侧重新进入。
 * - [DriftWrapLeft] —— **单向 右 → 左**:从屏幕右侧外走到左侧外,出屏后回绕到右侧重新进入。
 *   两个单向模式的回绕跨度都是 `屏宽 + 元素宽` → 元素在回绕瞬间**已经完全出屏**,看不到"跳回"。
 *   **代价**:单向元素约 **20%~38%** 的时间完全不在屏上(元素越宽占比越高)。
 *   **注意**:单向模式下 `xOffset` 与 `amplitudeX` **都不使用**(横向位置完全由回绕决定)。
 */
enum class CloudMotion {
    /** 左右往复摆动(sin),永远在屏上 */
    Oscillate,

    /** 单向 左 → 右,出屏后从左侧回绕 */
    DriftWrap,

    /** 单向 右 → 左,出屏后从右侧回绕(§21d) */
    DriftWrapLeft,
}

// 冷青 #A9C3C0:§11 §23-§26 渐变云沿用的高频冷色,
// 在暖色背景(后山1/2 的试炼图)上"冷暖对立"提高对比度
private val FocusCloudColor = Color(0xFFA9C3C0)

/**
 * 冷青 `#A9C3C0` —— 项目里沿用的高频冷色(§11 雾层、§23-§26 渐变云),用来给云素材做 tint(§21h/§21m)。
 *
 * **用法是按"元素所在竖带的背景亮度"分别决定,不是一刀切**(§21m)。判据:
 * `对比度 = α × |素材亮度 − 背景亮度|`,α 取 `baseAlpha` 的典型值 0.5。
 * `#A9C3C0` 的亮度是 **187**,所以:
 *
 * | 所在带背景亮度 | 该用哪个 | 理由 |
 * |---|---|---|
 * | **≥ 230(浅天空 / 浅底部)** | **tint** | 白云(250~255)在那里对比度只有 4~11;冷青(187)能到 24~30 |
 * | **≈ 190(中间调山体)** | **保持白** | 冷青 187 ≈ 山体 192,对比度只剩 2.5;白云反而有 31.5 |
 * | **≈ 170(深山谷)** | **保持白** | 白云对比度 43.7,冷青只有 9.7 |
 *
 * **云是横向移动的,竖向位置固定** → 每朵永远待在同一个高度带里,所以"按带分配 tint"是稳定的。
 *
 * 反例记录:§21h 曾给竖排栈的 3 朵(浅天空 + 中山体 + 深山谷)统一加 tint,
 * 实测中部两朵**反而更糊**(2.5 vs 31.5、9.7 vs 43.7),§21m 据此改成分带。
 */
val CloudTintCool = Color(0xFFA9C3C0)

/**
 * 后山 3 页共同的"聚焦前景飘带"组件 —— 09-16 §43 提取
 *
 * 程序化径向渐变 + canvas 非等比缩放,出图像水墨晕染;**不建 render layer**
 *
 * 4 层独立动画(共享同一个 progress):
 *  - 慢速横向位移 —— **两种模式见 [CloudMotion]**:Oscillate(sin 往复)/ DriftWrap(单向+回绕,2026-09-16 §21c)
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
    motion: CloudMotion = CloudMotion.Oscillate,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
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
                // §21c/§21d 横向三种模式:Oscillate = sin 往复;DriftWrap = 单向向右;DriftWrapLeft = 单向向左
                val span = screenWidthDp + widthDp
                val x = when (motion) {
                    CloudMotion.Oscillate -> xOffset + sin(a) * amplitudeX
                    // 左→右:p=0 时整朵在左屏外,p=1 时整朵在右屏外 → 回绕瞬间不可见,无"跳回"痕迹
                    CloudMotion.DriftWrap -> -widthDp + progress * span
                    // 右→左:反向线性递减,两端同样都在屏外
                    CloudMotion.DriftWrapLeft -> screenWidthDp - progress * span
                }
                IntOffset(
                    (x + sin(jt) * 4.5f).dp.roundToPx(),
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
