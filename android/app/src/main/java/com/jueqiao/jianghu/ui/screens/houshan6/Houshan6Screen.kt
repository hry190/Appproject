package com.jueqiao.jianghu.ui.screens.houshan6

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
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 后山6 页 — 后山5 页 dolly 推进而来;点击"返回"按钮回到后山5;整屏点击 noop(**当前是终点页**)。
 *
 * 2026-09-18 §35 新建:用户指令"后山6复用后山4页面的素材和动画"。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【与后山4 的关系】**素材 / 动画 100% 复用,行为按"终点页"重新设计**
 *
 *   复用(逐项与 Houshan4Screen.kt 一致):
 *     - 全屏背景图  img_shilian_bg.png(后山4 用的那张"后山页背景.png")
 *     - 云雾层      HoushanMistLayer()(默认变体,**不**用 Houshan3 变体 —— 与后山4 同)
 *     - 6 朵 ACI    58 / FCB左下 / 60 / FCB中下 / 62 / 57(周期 21.4/11/8/10/9/13 s,与后山4 同)
 *     - 6 朵老云    58 / 61 / 56 / 57 / 60 / 60b(§21f 同款)
 *     - 熊猫        img_shilian_panda,X=184 Y=621 W=210 H=192(§22 同款动画)
 *     - 4 个标签    万象谱 / 寻径迷踪步 / 百炼识物诀 / 分门辨类掌(位置与后山4 完全一致)
 *                   跳转目标沿用 §32 的"文字→卷"映射:3/4/5/6 卷-1
 *     - 返回按钮    Return.png,X=30 Y=60 W=18 H=18
 *     - **不加** Rectangle156 气泡(§18 "过场页不应有信息气泡")
 *
 *   改造(与后山4 不同之处):
 *     - ❌ **没有 dolly-in 过渡**:后山6 当前是**终点页**,整屏点击 noop
 *       (这与后山5 在 §33 的状态相同 —— 后山5 后来因"要跳后山6"才补上 dolly)
 *     - ❌ 没有 `isTransitioning` 状态 / 三个景深平面 / graphicsLayer 变换
 *     - ❌ 没有 chromeFade(返回按钮不需要淡出)
 *     - 标签 clickable **不带 `enabled = !isTransitioning`**(无过渡 → 无需防误触)
 * ══════════════════════════════════════════════════════════════════════════
 * 【标签跳转目标】§35 接好 —— 沿用 §32 的"文字→卷"映射(与后山4 一致)
 *
 *   - 标签1 万象谱     → 第三卷-1(`onOpenVolume3Part1`)
 *   - 标签2 寻径迷踪步 → 第四卷-1(`onOpenVolume4Part1`)
 *   - 标签3 百炼识物诀 → 第五卷-1(`onOpenVolume5Part1`)
 *   - 标签4 分门辨类掌 → 第六卷-1(`onOpenVolume6Part1`)
 *
 * 布局(与后山4 一致):
 *   - 全屏背景图(img_shilian_bg.png)
 *   - 6 朵 ACI 动画云 + 6 朵老云
 *   - 熊猫图像(X=184, Y=621, W=210, H=192)
 *   - 标签1 万象谱    (X=-13,  Y=570, W=106, H=210 §24b)
 *   - 标签2 寻径迷踪步(X=168, Y=345, W=74,  H=150 §24b)
 *   - 标签3 百炼识物诀(X=113, Y=322, W=50,  H=105 §24b)
 *   - 标签4 分门辨类掌(X=151, Y=248, W=30,  H=70  §24b)
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
/**
 * 后山6 页的所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 *
 * 为什么用 data class 而不是 5 个独立 lambda 参数:
 *   §3 实测:多 lambda 签名的 composable,slot table 在某种条件下把第 1 个 slot 记成 null
 *           而非 `Composer.Empty`,被 Compose 当成"有效值"复用 → clickable.onClick = null →
 *           invoke() 时 NPE 闪退。
 *   方向 B(包成 data class):参数从"5 个 lambda"变成"1 个非 lambda",slot 结构根本不同,
 *                           bug 触发条件消失,理论上根治。
 *   字段命名沿用 §32 的"文字→卷"语义 —— 不绑定标签文案,文案再改只动 NavHost 一处。
 */
data class Houshan6Actions(
    val onBack: () -> Unit = {},
    val onOpenVolume3Part1: () -> Unit = {},
    val onOpenVolume4Part1: () -> Unit = {},
    val onOpenVolume5Part1: () -> Unit = {},
    val onOpenVolume6Part1: () -> Unit = {},
)

@Composable
fun Houshan6Screen(
    actions: Houshan6Actions = Houshan6Actions(),
) {
    // §3 + §35:用 data class 包成 1 个参数,bug 触发条件(slot 0 = lambda)消失,无需 safeXxx 兜底
    // (旧版的 if (xxx == null) ({}) else xxx 5 行兜底已删)

    BackHandler(enabled = true) { actions.onBack() }

    // 熊猫上下浮 + 呼吸缩放(沿用 §22 后山 2 / §21 后山 1 / §35 后山6 的同款动画参数)
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

    // ── 6 个动画云元素的进度:与后山 1/2/4 完全同一套(周期 21.4/11/8/10/9/13 s)──
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(与后山 1/2/4 同一套;§21f 用户指令"不考虑间距了")──
    val o58Progress = rememberCloudProgress(4_700, "old58")
    val o61Progress = rememberCloudProgress(9_700, "old61")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o57Progress = rememberCloudProgress(8_200, "old57")
    val o60Progress = rememberCloudProgress(4_900, "old60")
    val o60bProgress = rememberCloudProgress(10_300, "old60b")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // §35:后山6 当前是终点页 → 整屏 clickable = noop(与后山5 在 §33 的同款做法)。
            //   若日后要接后山7,这里改成 `.clickable { startDollyIn() }` 并补三景深平面。
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        // 全屏背景图(后山页背景.png —— 与后山 1/2/4 同款)
        Image(
            painter = painterResource(R.drawable.img_shilian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 云雾层(程序化水墨云海,持续循环)— 用默认变体(Houshan1/2/4 同款)
            // 后山 3/5 用的是 Houshan3 变体(因为它们的背景图是另一张画作);后山 6 复用后山 4 的素材 → 用默认变体
            HoushanMistLayer()

            // ══ §21 动画云元素竖排(6 个,与后山 4 同一套)══════════════════════
            // 与后山 1/2/4 完全同一套 y 序列、间隔、×0.75 尺寸
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_58),
                contentDescription = "云朵58",
                xOffset = -27f, yOffset = 8f,
                widthDp = 180f, heightDp = 101.5f,
                progress = c58Progress, phase = 0.00f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 200f, amplitudeY = 10f,
                baseAlpha = 0.50f, alphaAmp = 0.25f,
                tint = CloudTintCool,
            )
            FocusCloudBand(
                xOffset = 0f, yOffset = 178.5f,
                widthDp = 180f, heightDp = 90f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 10f,
                baseAlpha = 0.50f, alphaAmp = 0.30f,
                periodMs = 11_000,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = "云朵60",
                xOffset = 0f, yOffset = 330.5f,
                widthDp = 210f, heightDp = 81f,
                progress = c60Progress, phase = 0.35f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 8f,
                baseAlpha = 0.50f, alphaAmp = 0.25f,
            )
            FocusCloudBand(
                xOffset = 0f, yOffset = 477.5f,
                widthDp = 240f, heightDp = 82.5f,
                motion = CloudMotion.DriftWrapLeft,
                amplitudeX = 0f, amplitudeY = 10f,
                baseAlpha = 0.50f, alphaAmp = 0.30f,
                periodMs = 10_000,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan3_cloud_62),
                contentDescription = "云朵62",
                xOffset = 83f, yOffset = 620f,
                widthDp = 180f, heightDp = 73.5f,
                progress = c62Progress, phase = 0.70f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 200f, amplitudeY = 8f,
                baseAlpha = 0.50f, alphaAmp = 0.25f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_57),
                contentDescription = "云朵57",
                xOffset = 146f, yOffset = 754.5f,
                widthDp = 100f, heightDp = 90f,
                progress = c57Progress, phase = 0.71f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 200f, amplitudeY = 10f,
                baseAlpha = 0.50f, alphaAmp = 0.25f,
                tint = CloudTintCool,
            )

            // ══ §21f 6 朵老云(与后山 1/2/4 同一套)══════════════════════════════
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_58),
                contentDescription = "云朵58",
                xOffset = -70f, yOffset = 320f,
                widthDp = 455f, heightDp = 259f,
                progress = o58Progress, phase = 0.00f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 90f, amplitudeY = 14f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_61),
                contentDescription = "云朵61",
                xOffset = 0f, yOffset = 304f,
                widthDp = 355f, heightDp = 213f,
                progress = o61Progress, phase = 0.18f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 12f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_56),
                contentDescription = "云朵56",
                xOffset = 196f, yOffset = 595f,
                widthDp = 335f, heightDp = 297f,
                progress = o56Progress, phase = 0.42f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 70f, amplitudeY = 16f,
                baseAlpha = 1f, alphaAmp = 0f,
                pulseBase = 0f, pulseAmp = 0f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_57),
                contentDescription = "云朵57",
                xOffset = 0f, yOffset = 570f,
                widthDp = 225f, heightDp = 191f,
                progress = o57Progress, phase = 0.63f,
                motion = CloudMotion.DriftWrapLeft,
                amplitudeX = 0f, amplitudeY = 12f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = "云朵60",
                xOffset = -21f, yOffset = 570f,
                widthDp = 355f, heightDp = 137f,
                progress = o60Progress, phase = 0.27f,
                motion = CloudMotion.Oscillate,
                amplitudeX = 90f, amplitudeY = 10f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = "云朵60b",
                xOffset = 0f, yOffset = 770f,
                widthDp = 355f, heightDp = 137f,
                progress = o60bProgress, phase = 0.81f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 10f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            // 熊猫图像(沿用 §22 后山 2 / §21 后山 1 / §35 后山 6 的同款)— X=184, Y=621, W=210, H=192
            Image(
                painter = painterResource(R.drawable.img_shilian_panda),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 184.dp, y = (621f + pandaDy).dp)
                    .size(width = 210.dp, height = 192.dp)
                    .graphicsLayer(
                        scaleX = pandaScale,
                        scaleY = pandaScale,
                    ),
                contentScale = ContentScale.FillBounds,
            )

            // "标签1" 图像(万象谱,X=-13, Y=570, W=106, H=210 §24b)— §35:点击 → **第三卷-1**
            Box(
                modifier = Modifier
                    .offset(x = -13.dp, y = 570.dp)
                    .size(width = 106.dp, height = 210.dp)
                    .clickable(onClick = actions.onOpenVolume3Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 24.sp),
                    modifier = Modifier
                        .offset(x = 46.dp, y = 68.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 48.dp, y = 30.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签2" 图像(寻径迷踪步,X=168, Y=345, W=74, H=150 §24b) — §35:点击 → **第四卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 168.dp, y = 345.dp)
                    .size(width = 74.dp, height = 150.dp)
                    .clickable(onClick = actions.onOpenVolume4Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 15.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 40.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 20.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签3" 图像(百炼识物诀,X=113, Y=322, W=50, H=105 §24b) — §35:点击 → **第五卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 113.dp, y = 322.dp)
                    .size(width = 50.dp, height = 105.dp)
                    .clickable(onClick = actions.onOpenVolume5Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "百\n炼\n识\n物\n诀",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 27.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                    modifier = Modifier
                        .offset(x = 23.dp, y = 18.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(分门辨类掌,X=151, Y=248, W=30, H=70 §24b) — §35:点击 → **第六卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 151.dp, y = 248.dp)
                    .size(width = 30.dp, height = 70.dp)
                    .clickable(onClick = actions.onOpenVolume6Part1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "分\n门\n辨\n类\n掌",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp, lineHeight = 6.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 23.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 11.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到后山5 页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = actions.onBack),
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
