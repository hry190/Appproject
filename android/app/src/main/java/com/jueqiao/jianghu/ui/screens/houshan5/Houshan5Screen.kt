package com.jueqiao.jianghu.ui.screens.houshan5

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.AnimatedCloudImage
import com.jueqiao.jianghu.ui.components.CloudMotion
import com.jueqiao.jianghu.ui.components.CloudTintCool
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.components.HoushanMistVariant
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 后山5 页 — 后山4 页 dolly 推进而来;点击"返回"按钮回到后山4;整屏点击 noop(**当前是终点页**)。
 *
 * 2026-09-17 §33 新建:用户指令"创建后山5页面...后山5复用后山3页面的素材和动画"。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【与后山3 的关系】**素材 / 动画 100% 复用,行为按"终点页"重新设计**
 *
 *   复用(逐项与 Houshan3Screen.kt 一致):
 *     - 全屏背景图  img_shilian2_bg.png(后山3 用的那张"试炼转换.png")
 *     - 云雾层      HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
 *     - 5 朵 ACI    58 / FCB左下 / 60 / FCB中下 / 62(周期 21.4/11/8/10/9 s,与后山3 同)
 *     - 5 朵老云    old / 56 / 58 / 57 / 5(§21f 同款)
 *     - 熊猫        img_shilian2_recovered_8,X=118 Y=405 W=181 H=96
 *                   + "上下浮 ±10dp/4s + 呼吸缩放 0.95~1.05/3s"动画
 *     - 3 个标签    万象谱 / 寻径迷踪步 / 拆招心法(位置与后山3 完全一致)
 *     - 返回按钮    Return.png,X=30 Y=60 W=18 H=18
 *
 *   改造(与后山3 不同之处):
 *     - ❌ **没有 dolly-in 过渡**:后山5 当前是**终点页**,整屏点击 noop
 *       (这与后山4 在 §24~§32 期间的状态相同 —— 后山4 后来因"要跳后山5"才补上 dolly)
 *     - ❌ 没有 `isTransitioning` 状态 / 三个景深平面 / graphicsLayer 变换
 *     - ❌ 没有 chromeFade(返回按钮不需要淡出)
 *     - 标签 clickable **不带 `enabled = !isTransitioning`**(无过渡 → 无需防误触)
 * ══════════════════════════════════════════════════════════════════════════
 * 【标签跳转目标】**尚未配置**(沿用 §24 对后山4 的做法)
 *
 *   - 用户指令只说了"复用素材和动画",**没提标签跳转** → 3 个 callback 默认空函数
 *   - 参数名已按 §31 的"文字→卷"表取**语义名**,一旦用户给出目标,
 *     只改 JianghuNavHost 的 composable(Routes.Shilian5) 一处即可,**不必碰本文件**
 *   - 参考:后山3 的同名标签映射是 拆招心法→卷2 · 万象谱→卷3 · 寻径迷踪步→卷4(§31)
 *
 * 布局(与后山3 一致):
 *   - 全屏背景图(img_shilian2_bg.png)
 *   - 5 朵 ACI 动画云 + 5 朵老云
 *   - 熊猫图像(X=118, Y=405, W=181, H=96)
 *   - 标签2 拆招心法(X=124, Y=521, W=96, H=170)
 *   - 标签3 万象谱(X=43, Y=390, W=51, H=91)
 *   - 标签4 寻径迷踪步(X=105, Y=295, W=30, H=53.5)
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
@Composable
fun Houshan5Screen(
    onBack: () -> Unit = {},
    // §33:3 个标签的跳转目标**尚未配置**(用户只说"复用素材和动画"),默认都是 noop。
    //   参数名按 §31 的"文字→卷"表取语义名,便于日后在 NavHost 里一行接好。
    onOpenVolume2Part1: () -> Unit = {},  // (预留)拆招心法   → 第二卷-1?
    onOpenVolume3Part1: () -> Unit = {},  // (预留)万象谱     → 第三卷-1?
    onOpenVolume4Part1: () -> Unit = {},  // (预留)寻径迷踪步 → 第四卷-1?
) {
    BackHandler(enabled = true) { onBack() }

    // 熊猫上下浮 + 呼吸缩放(与后山1/2/3 同款参数)
    val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
    val pandaScale by pandaTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaScale",
    )
    val pandaDy by pandaTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaDy",
    )

    // §21 5 朵 ACI 动画云进度(与后山3 同一套周期)
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")

    // §21f 5 朵老云动画进度(与后山3 同一套)
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // §33:后山5 当前是终点页 → 整屏 clickable = noop(与后山4 在 §24~§32 期间同款)。
            //   若日后要接后山6,这里改成 `.clickable { startDollyIn() }` 并补三景深平面。
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        // 全屏背景图(试炼转换.png —— 与后山3 同一张)
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 云雾层(程序化水墨云海,持续循环)— 用 Houshan3 变体(与后山3 同)
        HoushanMistLayer(variant = HoushanMistVariant.Houshan3)

        // ══ §21 动画云元素竖排(5 个,与后山3 完全同一套)════════════════════════
        // y 序列(顶边):ACI58 8 · FCB左下 178.5 · ACI60 330.5 · FCB中下 477.5 · ACI62 620
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_58),
            contentDescription = "云朵58",
            xOffset = -60f,
            yOffset = 8f,
            widthDp = 180f,
            heightDp = 101.5f,
            progress = c58Progress,
            phase = 0.00f,
            motion = CloudMotion.Oscillate,
            amplitudeX = 200f,
            amplitudeY = 10f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
            tint = CloudTintCool,
        )
        FocusCloudBand(
            xOffset = 0f,
            yOffset = 178.5f,
            widthDp = 180f,
            heightDp = 90f,
            motion = CloudMotion.DriftWrap,
            amplitudeX = 0f,
            amplitudeY = 10f,
            baseAlpha = 0.50f,
            alphaAmp = 0.30f,
            periodMs = 11_000,
        )
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_60),
            contentDescription = "云朵60",
            xOffset = 0f,
            yOffset = 330.5f,
            widthDp = 210f,
            heightDp = 81f,
            progress = c60Progress,
            phase = 0.35f,
            motion = CloudMotion.DriftWrap,
            amplitudeX = 0f,
            amplitudeY = 8f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )
        FocusCloudBand(
            xOffset = 0f,
            yOffset = 477.5f,
            widthDp = 240f,
            heightDp = 82.5f,
            motion = CloudMotion.DriftWrapLeft,
            amplitudeX = 0f,
            amplitudeY = 10f,
            baseAlpha = 0.50f,
            alphaAmp = 0.30f,
            periodMs = 10_000,
        )
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan3_cloud_62),
            contentDescription = "云朵62",
            xOffset = 50f,
            yOffset = 620f,
            widthDp = 180f,
            heightDp = 73.5f,
            progress = c62Progress,
            phase = 0.70f,
            motion = CloudMotion.Oscillate,
            amplitudeX = 200f,
            amplitudeY = 8f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ══ §21f 5 朵老云(与后山3 完全同一套)════════════════════════════════
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_shilian3_cloud_old),
                contentDescription = "旧云朵",
                xOffset = -46f, yOffset = 476f,
                widthDp = 331f, heightDp = 92f,
                progress = oOldProgress, phase = 0.00f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 80f, amplitudeY = 10f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_shilian3_cloud_56),
                contentDescription = "云朵56",
                xOffset = 263f, yOffset = 755f,
                widthDp = 335f, heightDp = 297f,
                progress = o56Progress, phase = 0.42f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 70f, amplitudeY = 16f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_shilian3_cloud_58),
                contentDescription = "云朵58",
                xOffset = 0f, yOffset = 170f,
                widthDp = 331f, heightDp = 92f,
                progress = o58Progress, phase = 0.18f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 10f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_shilian3_cloud_57),
                contentDescription = "云朵57",
                xOffset = 0f, yOffset = 500f,
                widthDp = 225f, heightDp = 191f,
                progress = o57Progress, phase = 0.63f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 12f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_shilian3_cloud_5),
                contentDescription = "云朵5",
                xOffset = 0f, yOffset = 308f,
                widthDp = 225f, heightDp = 191f,
                progress = o5Progress, phase = 0.81f,
                motion = CloudMotion.DriftWrapLeft,
                amplitudeX = 0f, amplitudeY = 12f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 与后山3 同
            Image(
                painter = painterResource(R.drawable.img_shilian2_recovered_8),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 118.dp, y = (405f + pandaDy).dp)
                    .size(width = 181.dp, height = 96.dp)
                    .graphicsLayer(
                        scaleX = pandaScale,
                        scaleY = pandaScale,
                    ),
                contentScale = ContentScale.FillBounds,
            )

            // "标签3" 图像(万象谱,X=43, Y=390, W=51, H=91)— §33 目标待配
            Box(
                modifier = Modifier
                    .offset(x = 43.dp, y = 390.dp)
                    .size(width = 51.dp, height = 91.dp)
                    .clickable(onClick = onOpenVolume3Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(寻径迷踪步,X=105, Y=295, W=30, H=53.5)— §33 目标待配
            Box(
                modifier = Modifier
                    .offset(x = 105.dp, y = 295.dp)
                    .size(width = 30.dp, height = 53.5.dp)
                    .clickable(onClick = onOpenVolume4Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签2" 图像(拆招心法,X=124, Y=521, W=96, H=170)— §33 目标待配
            Box(
                modifier = Modifier
                    .offset(x = 124.dp, y = 521.dp)
                    .size(width = 96.dp, height = 170.dp)
                    .clickable(onClick = onOpenVolume2Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 48.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 25.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到后山4 页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = onBack),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
