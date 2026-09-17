package com.jueqiao.jianghu.ui.screens.houshan4

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
 * 后山4 页 — 后山3 页 → 点击"返回"按钮回到后山3;整屏点击 noop(终点页,无下一页)。
 *
 * 2026-09-17 §24 新建:用户指令"复用后山 2 页面的素材和动画",反转 §21n 之前"后山 3 是终点"
 * 的隐含假设(后山 3 现在跳后山 4,Unfinished 不再是从后山 3 出发的目标)。
 *
 * 复用:
 *   - 全屏背景图(img_shilian_bg.png,与后山 1/2 同款)
 *   - 云雾层(HoushanMistLayer 默认变体,与后山 1/2 同款,**不**用 Houshan3 变体)
 *   - 6 朵 ACI 动画云(58/60/62 + 2 FocusCloudBand + 57),周期 / 振幅 / 模式与后山 1/2 完全一致
 *   - 6 朵老云动画(58/61/56/57/60/60b),§21f 同款
 *   - 4 个标签(万象谱 / 寻径迷踪步 / 百炼识物诀 / 分门辨类掌),位置与后山 1/2 完全一致
 *   - 熊猫图像(img_shilian_panda,X=184, Y=621, W=210, H=192),§22 加回后山 2 的同款
 *     "上下浮 ±10dp/4s + 呼吸缩放 0.95~1.05/3s"动画
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18)
 *
 * 改造:
 *   - **去掉 dolly-in 过渡动画**:后山 4 是终点页,不是过场页;点击整屏 noop,没有下一页可去
 *   - **整屏 clickable = noop**:整屏 Box 仍带 clickable(与后山 1/2/3 视觉/结构对称),
 *     但 onClick 是空函数,给用户"点击不响应"的终点语义
 *   - **去掉 Rectangle156 气泡**:后山 2 是过场页也没复制气泡,后山 4 同样不加
 *
 * §24 标签可点击 → §32 已配置跳转目标:
 *   - §24 用户指令"标签可以点击,但还没有设置好可以跳转的页面" → 4 个 callback 用占位名
 *     `onOpenTagN` 且默认空函数,只等 NavHost 填目标
 *   - §32 用户给出全部目标 → **参数名改为语义名**(`onOpenVolume3Part1` 等),
 *     与后山2(§28~§30)/ 后山3(§31)命名一致,一眼看出"哪个标签去哪个卷"
 *   - 4 个标签都是 `.clickable(enabled = true, onClick = onOpenXXX)`;因为后山4 是终点页
 *     (无 dolly 过渡),所以**没有** `!isTransitioning` 门槛(与后山2/3 不同)
 *
 * ✅ §32 起后山 4 的 **4 个标签全部可点击**,目标映射(注意与后山2/3 的文字→卷表**不同**):
 *    万象谱 → 第三卷-1 · 寻径迷踪步 → 第四卷-1 · 百炼识物诀 → 第五卷-1 · 分门辨类掌 → 第六卷-1
 *    ⚠️ 后山4 的标签文字是 §24 改过的(识机真决→万象谱 等),所以映射表**不能照抄后山2**:
 *       "万象谱"在后山2 是第三卷,在后山4 也是第三卷(巧合一致);
 *       但"分门辨类掌"是后山4 独有的文字,对应第六卷。
 *
 * 布局(与后山 2 一致):
 *   - 全屏背景图(img_shilian_bg.png)
 *   - 6 朵 ACI 动画云(58/60/62 + 2 FocusCloudBand + 57)
 *   - 6 朵老云(58/61/56/57/60/60b)
 *   - 标签1 图像(X=-13, Y=570, W=106, H=210 §24b)+ 文字"万象谱"(原后山2"识机真决",§24 改名)+ 文字"炼"
 *     §32:点击 → **第三卷-1**(`onOpenVolume3Part1`)
 *   - 标签2 图像(X=168, Y=345, W=74, H=150 §24b)+ 文字"寻径迷踪步"(原后山2"拆招心法",§24 改名)+ 文字"炼"
 *     §32:点击 → **第四卷-1**(`onOpenVolume4Part1`)
 *   - 标签3 图像(X=113, Y=322, W=50, H=105 §24b)+ 文字"百炼识物诀"(原后山2"万象谱",§24 改名)+ 文字"炼"
 *     §32:点击 → **第五卷-1**(`onOpenVolume5Part1`)
 *   - 标签4 图像(X=151, Y=248, W=30, H=70 §24b)+ 文字"分门辨类掌"(原后山2"寻径迷踪步",§24 改名)+ 文字"炼"
 *     §32:点击 → **第六卷-1**(`onOpenVolume6Part1`)
 *   - 熊猫图像(X=184, Y=621, W=210, H=192)
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
@Composable
fun Houshan4Screen(
    onBack: () -> Unit = {},
    // §32:4 个标签的跳转目标已配置 —— 参数名从 §24 的占位 `onOpenTagN` 改为**语义名**,
    //   与后山2(§28~§30)/ 后山3(§31)的命名一致,一眼能看出"哪个标签去哪个卷"。
    //   代价:动了本文件(§24 曾说"不必再改本文件"),但语义名比"标签1/2/3/4"更抗漂移。
    onOpenVolume3Part1: () -> Unit = {},  // 标签1 万象谱     → 第三卷-1
    onOpenVolume4Part1: () -> Unit = {},  // 标签2 寻径迷踪步 → 第四卷-1
    onOpenVolume5Part1: () -> Unit = {},  // 标签3 百炼识物诀 → 第五卷-1
    onOpenVolume6Part1: () -> Unit = {},  // 标签4 分门辨类掌 → 第六卷-1
) {
    BackHandler(enabled = true) { onBack() }

    // 熊猫上下浮 + 呼吸缩放(沿用 §22 后山 2 / §21 后山 1 的同款动画参数)
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

    // ── 6 个动画云元素的进度:与后山 1/2 完全同一套(周期 21.4/11/8/10/9/13 s)──
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(与后山 1/2 同一套;§21f 用户指令"不考虑间距了")──
    val o58Progress = rememberCloudProgress(4_700, "old58")
    val o61Progress = rememberCloudProgress(9_700, "old61")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o57Progress = rememberCloudProgress(8_200, "old57")
    val o60Progress = rememberCloudProgress(4_900, "old60")
    val o60bProgress = rememberCloudProgress(10_300, "old60b")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(后山页背景.png,与后山 1/2 同款)
        Image(
            painter = painterResource(R.drawable.img_shilian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 云雾层(程序化水墨云海,持续循环)— 用默认变体(Houshan1/2 同款)
        // 后山 3 用的是 Houshan3 变体(因为它的背景图是另一张画作);后山 4 用默认变体
        HoushanMistLayer()

        // ══ §21 动画云元素竖排(6 个)════════════════════════════════════════════
        // 与后山 1/2 完全同一套 y 序列、间隔(69/62/66/60/61)、×0.75 尺寸
        // 详见 Houshan2Screen.kt §21 ~ §21n 的几何推导 + 模式分配
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

        // 内容层(避开系统导航条)— 整屏 clickable 但 onClick noop(终点页 §24)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},  // §24 整屏 noop —— 后山 4 是终点,无下一页
                ),
        ) {
            // ══ §21f 6 朵老云(重新动画;与后山 1/2 同一套;用户指令"不考虑间距了")══════
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

            // 熊猫图像(沿用 §22 后山 2 / §21 后山 1 的同款)— X=184, Y=621, W=210, H=192
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

            // "标签1" 图像(万象谱,X=-13, Y=570, W=106, H=210 §24b)— §32:点击 → **第三卷-1**
            Box(
                modifier = Modifier
                    .offset(x = -13.dp, y = 570.dp)
                    .size(width = 106.dp, height = 210.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenVolume3Part1,
                    ),
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

            // "标签2" 图像(寻径迷踪步,X=168, Y=345, W=74, H=150 §24b) — §32:点击 → **第四卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 168.dp, y = 345.dp)
                    .size(width = 74.dp, height = 150.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenVolume4Part1,
                    ),
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

            // "标签3" 图像(百炼识物诀,X=113, Y=322, W=50, H=105 §24b) — §32:点击 → **第五卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 113.dp, y = 322.dp)
                    .size(width = 50.dp, height = 105.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenVolume5Part1,
                    ),
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

            // "标签4" 图像(分门辨类掌,X=151, Y=248, W=30, H=70 §24b) — §32:点击 → **第六卷-1**
            Box(
                modifier = Modifier
                    .offset(x = 151.dp, y = 248.dp)
                    .size(width = 30.dp, height = 70.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenVolume6Part1,
                    ),
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

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到后山3 页
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