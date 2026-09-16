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
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
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

    // ── §21c 6 个动画云元素的进度:每个元素独立周期,互不整除 ──────────────────
    // 用户指令"一部分走单向+回绕、一部分保持 sin 摆动,用互质周期错开"。
    // 周期 7/8/9/13(三朵 ACI)+ FCB 的 10/11 → 六个数 lcm = 7×8×9×5×11×13 = 360360s ≈ **4.2 天**,
    // 远超单次使用时长,看不出规律性同步。
    // 平均速度校准(让"单向组"和"摆动组"看起来快慢一致):
    //   单向组 = (393+元素宽)/周期 → ACI58 573/7 = 82 dp/s、ACI60 603/8 = 75、ACI62 573/9 = 64
    //   摆动组 = 400/半周期     → FCB左下 400/5.5 = 73、FCB中下 400/5 = 80、ACI57 400/6.5 = 62
    val cloudTransition = rememberInfiniteTransition(label = "h1Clouds")
    val c58Progress = cloudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud58",
    )
    val c60Progress = cloudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud60",
    )
    val c62Progress = cloudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud62",
    )
    val c57Progress = cloudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud57",
    )

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
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 云朵 58 (Ellipse 58.png, X=-70, Y=320, W=455, H=259) — fit-to-natural-bounds, 横图源 844×474 (比 1.781)
            // §21 静态化:用户指令"老云保留为静态图层、去掉动画" → 去掉随机飘动;
            //      alpha 固定取原动画区间 0.5~1.0 的中点 0.75,保持与原来"平均观感"一致
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_58),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-70f).dp, y = 320f.dp)
                    .size(width = 455.dp, height = 259.dp),
                alpha = 0.75f,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵 61 (Ellipse 61.png, X=-50, Y=304, W=355, H=213) — fit-to-natural-bounds, 横图源 351×210 (比 1.671)
            // §21 静态化(同云朵 58)
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_61),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-50f).dp, y = 304f.dp)
                    .size(width = 355.dp, height = 213.dp),
                alpha = 0.75f,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵 56 (Ellipse 56.png, X=196, Y=595, W=335, H=297) — 用户原值, 比 1.128 ≈ PNG 1.083 (4% 偏差可接受)
            // §21 静态化:去掉 §8 的椭圆飘动;alpha 保持 1f 不变(§7 用户明确指令"100% 不透明")
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_56),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 196f.dp, y = 595f.dp)
                    .size(width = 335.dp, height = 297.dp),
                alpha = 1f,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵 57 (Ellipse 57.png, X=208, Y=570, W=225, H=191) — 用户原值, 比 1.178 ≈ PNG 1.115 (5.7% 偏差可接受);右下角云朵 (§9);X=248→208 用户真机调整
            // §21 静态化(同云朵 58)
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_57),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 208f.dp, y = 570f.dp)
                    .size(width = 225.dp, height = 191.dp),
                alpha = 0.75f,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵 60 (Ellipse 60.png, X=-21, Y=570, W=355, H=137) — fit-to-natural-bounds, 扁长横图源 911×353 (比 2.581) (§10)
            // §21 静态化(同云朵 58)
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-21f).dp, y = 570f.dp)
                    .size(width = 355.dp, height = 137.dp),
                alpha = 0.75f,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵 60 副本 (Ellipse 60.png, X=-21, Y=760, W=355, H=137) — 与 cloud60 同 PNG
            // §21 静态化(同云朵 58)
            Image(
                painter = painterResource(R.drawable.img_houshan1_cloud_60),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-21f).dp, y = 760f.dp)
                    .size(width = 355.dp, height = 137.dp),
                alpha = 0.75f,
                contentScale = ContentScale.FillBounds,
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
