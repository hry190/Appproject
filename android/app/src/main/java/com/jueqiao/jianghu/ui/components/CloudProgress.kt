package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * 后山 1/2/3 三页共用的"**0→1 线性回绕进度**" —— 2026-09-16 §21j 从三个 Screen 提出来。
 *
 * ## 为什么需要它
 * §21c 给 6 个动画云元素各配一个独立周期、§21f 又给 6~7 朵老云各配一个,导致三个 Screen 里
 * 出现了 **29 处逐字相同的 `animateFloat(...)` 声明**(每处 8 行,合计约 240 行)。
 * 抽成一个函数后,每处只剩一行。
 * 这符合项目自己的抽象阈值(§17 沉淀:**"重复 ≥3 次 + 跨 ≥2 个文件 = 抽"**)——
 * 本案例是 **29 次 × 3 个文件**,远远越线。
 *
 * ## 实现说明
 * - 每次调用**自己建一个** [rememberInfiniteTransition]。
 *   `HoushanMistLayer` 的私有 `rememberMistProgress` 就是同款写法,已验证无性能问题
 *   (InfiniteTransition 本身很轻,真正逐帧跑的是里面的 animateFloat)。
 * - `LinearEasing` + `RepeatMode.Restart` → **匀速、无缓动**。
 *   所有派生量都取 `sin/cos(2π × (进度 + 相位))`,回绕瞬间 `progress` 从 1 跳回 0 时
 *   `sin(2π)` = `sin(0)` → **画面不产生跳变**(见 `FocusCloudBand` / `AnimatedCloudImage` 的 KDoc)。
 * - 周期务必取**互质**值:几个元素同周期会同频同相,"看起来像一个元素"。
 *
 * @param periodMs 一个完整周期的毫秒数。**调小 = 调快。**
 * @param label 给 Android Studio Layout Inspector 看的名字,用元素名即可(如 `"c58"`)。
 * @return 0→1 循环的进度值,可直接传给 `FocusCloudBand` / `AnimatedCloudImage` 的 `progress` 参数。
 */
@Composable
fun rememberCloudProgress(periodMs: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "$label-progress",
    )
