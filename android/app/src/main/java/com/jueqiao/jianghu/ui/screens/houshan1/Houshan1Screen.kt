package com.jueqiao.jianghu.ui.screens.houshan1

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
 * 后山1 页 — 滚轮1 → 点击"后山"按钮跳转目标。
 *
 * 布局:
 *   - 全屏背景图(后山页背景.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)
 *   - 熊猫图像(image 75.png,X=184, Y=621, W=210, H=192)
 *   - 标签1 图像(X=-13, Y=570, W=106, H=188)+ 文字"识机真决"(父 Box 内 X=46, Y=54, W=14, H=80)+ 文字"炼"(父 Box 内 X=48, Y=27, W=12, H=16)
 *   - 标签2 图像(X=168, Y=345, W=74, H=131)+ 文字"拆招心法"(父 Box 内 X=32, Y=34, W=14, H=80)+ 文字"炼"(父 Box 内 X=32, Y=17, W=12, H=16)
 *   - 标签3 图像(X=113, Y=322, W=50, H=88)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14)
 *   - 标签4 图像(X=151, Y=248, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14)
 *   - 气泡 Rectangle156.png(X=136, Y=508, W=177, H=107)+ 文字"御剑穿行..."
 *
 * §21 动画云元素(6 个,竖排,间隔 60~69dp,前 5 个 ×0.75 缩放):
 *   ACI58 y=8 · FCB左下 y=178.5 · ACI60 y=330.5 · FCB中下 y=477.5 · ACI62 y=620 · ACI57 y=754.5(100×90)
 *   第 6 个 底边 844.5dp,距导航栏上沿(857dp)12.5dp —— 满足"最底部必须有一个动画素材";
 *   §21b 按用户指令从 FocusCloudBand 改为实图云 PNG(cloud_57,有色调、在近白底上可见)。
 *   6 朵老云(58/61/56/57/60/60b)同 §21 去掉动画,改为静态图层。
 */
@Composable
fun Houshan1Screen(
    onBack: () -> Unit = {},
    onOpenHoushan2: () -> Unit = {},
    onOpenVolume1: () -> Unit = {},  // 识机真决标签跳转第一卷-1 (§22)
) {
    BackHandler(enabled = true) { onBack() }

    // 熊猫上下浮 + 呼吸缩放:Scale 0.95~1.05 / 3s, Y ±10 dp / 4s (§21)
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

    // ── 6 个动画云元素的进度:每个元素独立周期,互不整除 ──────────────────────
    // 用户指令"一部分走单向+回绕、一部分保持 sin 摆动,用互质周期错开"(§21c)。
    // §21k 用户反馈"最顶部的云速度太快了" → #1 ACI58 从 7s 改到 10.7s;
    //   §21l 用户复反馈"速度还是快了,速度调成一半" → 再减半到 **21.4s**(严格 ×2 周期 = ÷2 速度)。
    //   起点:§21c 的初始分配让它成了全页最快(4×200/7 = **114.3 dp/s**);
    //   §21h 给它加了冷青 tint 之后它变清楚了,**速度问题才暴露出来**。
    //   两轮:114.3 →(§21k 10.7s)74.8 →(§21l 21.4s)**37.4 dp/s**。
    //   ⚠️ 现在它变成**全页最慢**(次慢是 #2 FCB左下 52.1),而最快的仍是 #5 ACI62(88.9,是它的 2.4 倍);
    //      若之后觉得"和其它云不一致",下一个该调的是 ACI62(9s → 更大周期)。
    // 当前六个周期(上→下):21.4 / 11 / 8 / 10 / 9 / 13 s
    //   平均速度(上→下):37.4 / 52.1 / 75.4 / 63.3 / 88.9 / 61.5 dp/s
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(用户指令"把 6 朵静态老云的动画也做出来,不考虑间距了")──
    // §21 曾按用户指令把它们静态化;§21f 撤销该决定,并复用 §21c 的 CloudMotion 三模式
    // (不再用当初那套"朝随机目标点游走"的 rememberCloudFloat —— 那需要每朵 3 个协程,
    //  且方向/速度不可预期;CloudMotion 是确定性的,和另外 6 个动画元素同一种语言)。
    // **间距约束已按用户指令放弃**,故这里不参与"60~90dp 间隔"的排布。
    // §21i 用户指令"老云的移动速度要向其他的云朵一致" → 周期按"平均速度对齐"重算:
    //   目标 = 竖排栈 6 个的平均速度 **75.9 dp/s**(栈内 52~114);
    //   摆动模式 平均速度 = 4A/T,单向模式 = (屏宽+元素宽)/T。
    //   结果:58 4.7s→76.6、61 9.7s→77.1、56 3.7s→75.7、57 8.2s→75.4、60 4.9s→73.5、60b 10.3s→72.6 dp/s
    //   —— 全部落在 76±5% 内;且 47/97/37/82/49/103(×10ms)两两互质,不会规律性同步。
    // 尺寸最大的两朵(58: 455×259、56: 335×297)用 Oscillate 留在原地,避免大块云飘出屏幕;
    // 扁长的 3 朵(61/57/60b)用单向回绕,与 §21e 的"2 右 1 左"错开 → 老云是 2 右 1 左。
    // 白色脉冲关闭(pulseBase/pulseAmp = 0f):它们是画好的水彩云,不该再叠一层白光。
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
        // 全屏背景图(后山页背景.png)
        Image(
            painter = painterResource(R.drawable.img_shilian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 云雾层(程序化水墨云海,持续循环)— 背景之上、所有内容之下 (§37)
        // 本层无 clickable/pointerInput → 不拦截触摸;且下方山峰/标签/文字/熊猫/气泡
        // 均为不透明图版,故不会降低任何文字的对比度
        HoushanMistLayer()

        // ══ §21 动画云元素竖排(6 个)════════════════════════════════════════════
        // 用户指令:云朵素材"每两个间隔随机在 60~90dp",避免素材太密集;定死一组,不做每帧随机。
        //          最底部(导航栏上方)必须有一个动画素材 → 第 6 个即新增的底部 FocusCloudBand。
        //
        // 几何推导:素材 ×0.75 等比缩放后前 5 个高度合计 428.5dp;可用高度 = 873 − 16(导航栏)= 857dp,
        //   故 5 个间隔有空间落在 60~90 区间内 → 实际取 69/62/66/60/61。
        //   (6 个 × 90dp 间隔 = 962dp > 857dp,排不下;见 SESSION-LOG §21 的取舍表)
        //
        // y 序列(顶边)/ 高度 / 间隔:
        //   ACI58    y=8      h=101.5
        //   FCB左下  y=178.5  h=90     ← 间隔 69
        //   ACI60    y=330.5  h=81     ← 间隔 62
        //   FCB中下  y=477.5  h=82.5   ← 间隔 66
        //   ACI62    y=620    h=73.5   ← 间隔 60
        //   ACI57(底) y=754.5 h=90    ← 间隔 61;底边 844.5,距导航栏上沿 857dp 留 12.5dp
        //
        // amplitudeY 收到 8~10dp:原来的 42/36dp 上下漂移会把 60~69dp 的间隔整个吃掉(两朵各飘
        //   42/36dp 时会直接叠在一起);amplitudeX 仍 200dp(§20"约 5 秒跑完屏宽"不变)。
        // 绘制顺序 = 背景 → 雾层 → 这 6 个 → 内容层(标签/熊猫),故不会遮挡任何可点区域。
        // §21c/§21d 横向模式分配 —— **按"观感可见性"分配,不是按位置交替**:
        //   实测(§21 素材亮度表)三朵 ACI 的 PNG 是 250 / 255 / 237 的**近白/纯白**,叠在浅色背景上几乎不可见;
        //   而两条 FocusCloudBand 是**冷青 #A9C3C0**,在浅底上对比明显(§21 实测变化量 21~30%)。
        //   所以把**看得见的冷青飘带放进"单向"组**,单向运动才真的看得出来:
        //   #1 ACI58 摆动(7s) · #2 FCB左下 **单向→右**(11s) · #3 ACI60 **单向→右**(8s)
        //   #4 FCB中下 **单向←左**(10s) · #5 ACI62 摆动(9s) · #6 ACI57 摆动(13s,底部,永远在屏上)
        // §21d/§21e 用户指令 → 3 个单向最终是 **2 个向右 + 1 个向左**:
        //   向右 = FCB左下 + ACI60;向左 = FCB中下。
        //   这样**两个方向各有一个看得见的元素**(FCB左下 最明显;FCB中下 次之),
        //   若把"唯一向左"留给近白不可见的 ACI60,那"左"这个方向在真机上就等于消失了。
        // 单向模式忽略 xOffset / amplitudeX(横向位置完全由回绕决定),故这两项传 0f。
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_58),
            contentDescription = "云朵58",
            xOffset = -27f, yOffset = 8f,
            widthDp = 180f, heightDp = 101.5f,
            progress = c58Progress, phase = 0.00f,
            motion = CloudMotion.Oscillate,
            amplitudeX = 200f, amplitudeY = 10f,
            baseAlpha = 0.50f, alphaAmp = 0.25f,
            // §21h:cloud_58 平均亮度 250、带色像素 0% → 叠浅底看不见,用冷青 tint 提对比
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
        // 第 6 个 = 最底部素材:底边 844.5dp,距导航栏上沿(857dp)留 12.5dp → 满足"最底部必须有一个动画素材"
        // §21b 用户指令:从 FocusCloudBand(程序化冷青雾)换成 AnimatedCloudImage(**实图云 PNG**)。
        // 为什么用 img_houshan1_cloud_57 而不是形状更贴的扁长 cloud_60:
        //   实测 cloud_60 是**纯白 (255,255,255)**,而底部背景接近纯白 → 白云叠白底,像素差为 0,等于没加;
        //   cloud_57 实测 MeanLum 221、72% 像素带色调,是项目里少数"有颜色"的云(详见 §21 的素材亮度表)。
        //   代价:cloud_57 比例 1.12(近方),在"间隔 ≥60dp + 底边 ≤844.5dp"的约束下只能做 100×90。
        // §21c 运动模式:**保持 sin 摆动,不用单向回绕** —— 单向模式会有约 20% 时间完全出屏,
        //   会破坏"最底部一定要有一个动画素材"这条硬要求(用户明确选择保持摆动,永远在屏上)。
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_57),
            contentDescription = "云朵57",
            xOffset = 146f, yOffset = 754.5f,
            widthDp = 100f, heightDp = 90f,
            progress = c57Progress, phase = 0.71f,
            motion = CloudMotion.Oscillate,
            amplitudeX = 200f, amplitudeY = 10f,
            baseAlpha = 0.50f, alphaAmp = 0.25f,
            // §21m:按各朵**所在竖带的实测背景亮度**决定 tint。ACI57 在浅底部(234),
            //   素材 cloud_57 亮度 221 → 对比度只有 6.3;Modulate 冷青后约 162 → 对比度 35.8
            tint = CloudTintCool,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ══ §21f 6 朵老云(重新动画;间距约束已放弃)══════════════════════════
            // 位置/尺寸沿用 §21 静态化时的原值;alpha 恢复成老动画区间 0.75±0.25(= 原 0.5~1.0);
            // 云 56 按 §7 用户明确指令保持 100% 不透明(alphaAmp = 0)。
            // 动画云朵 58 (455×259, 最大) — Oscillate 留在原地
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

            // 动画云朵 61 (355×213) — 单向 → 右
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

            // 动画云朵 56 (335×297, 最高) — Oscillate 留在原地;alpha 恒定 1f(§7 用户指令)
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

            // 动画云朵 57 (225×191) — 单向 ← 左
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

            // 动画云朵 60 (355×137, 扁长) — Oscillate 留在原地
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

            // 动画云朵 60 副本 (355×137) — 单向 → 右(与 §21e 的 FCB中下 ← 左 相反,错开)
            AnimatedCloudImage(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = "云朵60b",
                xOffset = 0f, yOffset = 760f,
                widthDp = 355f, heightDp = 137f,
                progress = o60bProgress, phase = 0.81f,
                motion = CloudMotion.DriftWrap,
                amplitudeX = 0f, amplitudeY = 10f,
                baseAlpha = 0.75f, alphaAmp = 0.25f,
                pulseBase = 0f, pulseAmp = 0f,
            )

            // 熊猫图像(image 75.png,X=184, Y=621, W=210, H=192) — 上下浮 ±10 / 4s + 呼吸缩放 0.95~1.05 / 3s (§21)
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

            // "标签1" 图像(未标题-1-恢复的-恢复的 4.png,X=-13, Y=570, W=106, H=188) — 点击跳转第一卷-1 (§22)
            Box(
                modifier = Modifier
                    .offset(x = -13.dp, y = 570.dp)
                    .size(width = 106.dp, height = 188.dp)
                    .clickable(onClick = onOpenVolume1),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "识机真决" 竖排文字(父 Box 内 X=46, Y=54, W=14, H=80, 字号 14, 黑色, YaHei)
                Text(
                    text = "识\n机\n真\n决",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 46.dp, y = 54.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=48, Y=27, W=12, H=16, 字号 12, 颜色 #385816, YaHei)
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 48.dp, y = 27.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=168, Y=345, W=74, H=131)
            Box(
                modifier = Modifier
                    .offset(x = 168.dp, y = 345.dp)
                    .size(width = 74.dp, height = 131.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "拆招心法" 竖排文字(父 Box 内 X=32, Y=34, W=14, H=80, 字号 12, 黑色, YaHei)
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 34.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=32, Y=17, W=12, H=16, 字号 10, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 17.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=113, Y=322, W=50, H=88)
            Box(
                modifier = Modifier
                    .offset(x = 113.dp, y = 322.dp)
                    .size(width = 50.dp, height = 88.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=151, Y=248, W=30, H=53.5)
            Box(
                modifier = Modifier
                    .offset(x = 151.dp, y = 248.dp)
                    .size(width = 30.dp, height = 53.5.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // Rectangle156.png 气泡(X=136, Y=508, W=177, H=107)— 点击跳转到后山2 页
            Box(
                modifier = Modifier
                    .offset(x = 136.dp, y = 508.dp)
                    .size(width = 177.dp, height = 107.dp)
                    .clickable(onClick = onOpenHoushan2),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_rect156),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // 气泡文本(字号 14, 黑色, YaHei)
                Text(
                    text = "御剑穿行云雾群山，\n每一座山峰皆是试炼。来，选一座山峰，开启你的修行试炼！",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到修炼页
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
