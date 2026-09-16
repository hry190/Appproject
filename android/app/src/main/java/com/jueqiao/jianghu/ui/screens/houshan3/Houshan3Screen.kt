package com.jueqiao.jianghu.ui.screens.houshan3

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.AnimatedCloudImage
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.components.HoushanMistVariant
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.isActive

// 文件级常量 TWO_PI 已移到 ui/components/FocusCloudBand.kt 和 AnimatedCloudImage.kt
// 这里不再需要 TWO_PI 声明

/**
 * 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;点击标签2-4 之外的空白区域跳转未完待续页。
 *
 * 布局:
 *   - 全屏背景图(试炼转换.png — §10 替换)
 *   - 云雾层(程序化水墨云海,持续循环 — HoushanMistVariant.Houshan3 位置表)(§37/§38)
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 屏幕空白点击无效
 *   - 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— **上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s (§44)**
 *   - 标签2 图像(X=124, Y=521, W=96, H=170)+ 文字"拆招心法"(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14)+ 文字"炼"(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12)
 *   - 标签3 图像(X=43, Y=390, W=51, H=91)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6)
 *   - 标签4 图像(X=105, Y=295, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4)
 *   - 云朵(Ellipse 58.png,X=-46, Y=476, W=331, H=92)
 */
@Composable
fun Houshan3Screen(
    onBack: () -> Unit = {},
    onOpenUnfinished: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    // Ellipse 56 渐变色循环:Animatable<Float> 在 [0,1] 插值,4s 来回 (§23/24/25/26 修复)
    // 0 = A9C3C0, 1 = White;在 graphicsLayer 块内合成 Color
    val tintProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            tintProgress.animateTo(1f, animationSpec = tween(durationMillis = 4000, easing = LinearEasing))
            tintProgress.animateTo(0f, animationSpec = tween(durationMillis = 4000, easing = LinearEasing))
        }
    }

    // 2 朵渐变云朵(56/58)位置 + 透明度随机飘动(沿用 §13 rememberCloudFloat 模式)(§28)
    val (cloud56Dx, cloud56Dy, cloud56Alpha) = rememberCloudFloat()
    val (cloud58Dx, cloud58Dy, cloud58Alpha) = rememberCloudFloat()
    val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()  // §29 加 Ellipse 57
    val (cloud5Dx, cloud5Dy, cloud5Alpha) = rememberCloudFloat()  // §33 加 Ellipse 5(实际与 Ellipse 57 同图)

    // 熊猫上下浮 + 呼吸缩放 (§44,与后山1 §21 同款动画)
    // Scale 0.95~1.05 / 3s + Y ±10 dp / 4s,都用 LinearEasing + RepeatMode.Reverse → 来回无缝
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

    // 拆招心法下方三朵云动画 (§6)— 用 Ellipse 58/60/62 三张素材
    // 资源:58/60 复用现有 img_houshan1_cloud_58/60(同一张图,fit 版本 — 见 09-15 §4);
    // 62 是新导入:D:\图\Ellipse 62.png → drawable-nodpi/img_houshan3_cloud_62.png
    // 三朵共用 1 个 rememberInfiniteTransition,各 animateFloat 取**互质周期** 7/8/9s(§19)
    //   §19:用户要"横向 4 秒跑完屏宽"→ 周期 = 2 × 4s = 8s(半周期 = 单程),
    //        取 7/8/9s 三个值让往返时间 3.5/4/4.5s,都是"大概 4 秒"但互不同步
    //        (7/8/9 两两互质:gcd(7,8)=gcd(7,9)=gcd(8,9)=1)
    val cloudTransition = rememberInfiniteTransition(label = "h3CloudBands")
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(试炼转换.png,§10 从"后山3 转换.png"替换;长宽比 0.449 一致,ContentScale.Crop 适配)
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 云雾层(程序化水墨云海,持续循环)— 背景之上、所有内容之下 (§37/§38)
        // 用 Houshan3 变体:背景 img_shilian2_bg 与后山1/2 的 img_shilian_bg 是两张不同画作,
        // 山峰位置不同,故坐标表单独调(远山 / 熊猫所在山谷 / 前景山脚);周期与配色与后山1/2 完全共用
        // 本层无 clickable/pointerInput → 不拦截触摸,故不影响整屏跳转与标签点击;
        // 且整层位于熊猫、标签之下,不会遮挡御剑飞行的视觉焦点
        HoushanMistLayer(variant = HoushanMistVariant.Houshan3)

        // 拆招心法下方聚焦前景飘带 (§43)— 用户反馈该区域"没动画/不够明显"
        // 比起 HoushanMistLayer 的均匀细雾团,这一个更大、更浓,让"下方有东西在缓慢飘动"一眼可辨;
        // 位于内容层之下,不会遮挡标签或熊猫
        // §45:周期 19000 → 11000 ms(1.73× 更快,与 HoushanMistLayer 13/17/19 互质,观感更明显)
        // §9:透明度大扩:baseAlpha 0.32→0.50,alphaAmp 0.10→0.30 → range 0.20~0.80(峰谷差 0.60,6×于原 0.10)
        // §19:横向 4 秒跑完屏宽 → amplitudeX 60→200(= 2×200 = 400dp ≈ 屏宽 412dp),
        //      周期 17→8s(半周期 4s = 单程)
        FocusCloudBand(
            xOffset = 12f,
            yOffset = 740f,
            widthDp = 320f,
            heightDp = 110f,
            amplitudeX = 200f,
            amplitudeY = 42f,
            baseAlpha = 0.50f,
            alphaAmp = 0.30f,
            periodMs = 8_000,
        )

        // 拆招心法**左下方**聚焦前景飘带 (§44)— 用户反馈"左下方没动画"
        // x_offset = -30, w = 240 → 覆盖 x=-30..210,圆心 x=90(明显在标签中心 x=172 的左侧)
        // 标签 x=124..220 在飘带的右端下方,飘带在标签之下层,标签的米色不透明图版遮住右端,
        // 实际视觉可见的就是"标签左侧"那一段 — 严格满足"左下方"
        // §45:周期 23000 → 9000 ms(2.56× 更快,与 §43 的 11s 互质)→ 两条飘带节奏不同步,观感更自然
        // §9:透明度大扩:baseAlpha 0.30→0.50,alphaAmp 0.10→0.30 → range 0.20~0.80(与 §43 同步)
        // §19:横向 amplitudeX 52→200,周期 14→9s(单程 4.5s)
        FocusCloudBand(
            xOffset = -30f,
            yOffset = 740f,
            widthDp = 240f,
            heightDp = 120f,
            amplitudeX = 200f,
            amplitudeY = 36f,
            baseAlpha = 0.50f,
            alphaAmp = 0.30f,
            periodMs = 9_000,
        )

        // 拆招心法下方三朵云(§6)— Ellipse 58/60/62 实图素材,带漂移 + 聚散
        // 绘制顺序 = 背景 → 大雾层 → 2 朵 focus 飘带 → **3 朵云** → 内容:
        // 云是**不透明 PNG**,放在 focus 飘带**之前**(之下)→ 飘带不挡云,但云的软边看起来"自带雾气"也合理
        // 位置三角形:
        //   58 中心 (60, 770)  — 标签左下,横椭圆
        //   60 中心 (260, 740) — 标签右下,扁长条
        //   62 中心 (170, 820) — 标签正下,中等扁长
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_58),
            contentDescription = "云朵58",
            xOffset = -60f,
            yOffset = 703f,
            widthDp = 240f,
            heightDp = 135f,
            progress = c58Progress,
            phase = 0.13f,
            amplitudeX = 200f,   // §19:38 → 200(2×200=400dp ≈ 屏宽)
            amplitudeY = 15f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_60),
            contentDescription = "云朵60",
            xOffset = 120f,
            yOffset = 686f,
            widthDp = 280f,
            heightDp = 108f,
            progress = c60Progress,
            phase = 0.31f,
            amplitudeX = 200f,   // §19:45 → 200
            amplitudeY = 8f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan3_cloud_62),
            contentDescription = "云朵62",
            xOffset = 50f,
            yOffset = 771f,
            widthDp = 240f,
            heightDp = 98f,
            progress = c62Progress,
            phase = 0.71f,
            amplitudeX = 200f,   // §19:40 → 200
            amplitudeY = 10f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )

        // 内容层(避开系统导航条)— 整屏 clickable,但 3 个标签 Box 自带消费事件 clickable (§20),点击标签不会冒泡触发跳转
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .clickable(onClick = onOpenUnfinished),
        ) {
            // 旧云朵(8f5a28c 基线,重命名为 _old 避免与 Ellipse 58.png 命名冲突 §35)— 资源已 mv → img_shilian3_cloud_old.png
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_old),
                contentDescription = "云朵",
                modifier = Modifier
                    .offset(x = (-46).dp, y = 476.dp)
                    .size(width = 331.dp, height = 92.dp),
                contentScale = ContentScale.FillBounds,
            )

            // 渐变云朵(Ellipse 56.png, X=263, Y=755, W=335, H=297) — tint A9C3C0 ↔ 白色 + 位置 + 透明度随机 (§23/26/28)
            // 注意:Modifier.graphicsLayer 不支持 colorFilter,改用 Image 自己的 colorFilter 参数
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_56),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (263f + cloud56Dx).dp, y = (755f + cloud56Dy).dp)
                    .size(width = 335.dp, height = 297.dp),
                colorFilter = ColorFilter.tint(
                    Color(
                        red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
                        green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
                        blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
                    ),
                    BlendMode.Modulate,
                ),
                alpha = cloud56Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 渐变云朵(Ellipse 58.png, X=78, Y=170, W=331, H=92) — 与 Ellipse 56 共用 tintProgress 同步循环 + 各自位置/透明度独立随机 (§27/28)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_58),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (78f + cloud58Dx).dp, y = (170f + cloud58Dy).dp)
                    .size(width = 331.dp, height = 92.dp),
                colorFilter = ColorFilter.tint(
                    Color(
                        red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
                        green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
                        blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
                    ),
                    BlendMode.Modulate,
                ),
                alpha = cloud58Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵(Ellipse 57.png, X=-25, Y=500, W=225, H=191) — 原色显示,仅位置 + 透明度随机 (§29/§30)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_57),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp)
                    .size(width = 225.dp, height = 191.dp),
                alpha = cloud57Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵(Ellipse 5.png, X=187, Y=308, W=225, H=191) — 原色显示,位置 + 透明度随机(§33)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_5),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (187f + cloud5Dx).dp, y = (308f + cloud5Dy).dp)
                    .size(width = 225.dp, height = 191.dp),
                alpha = cloud5Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 在云朵下层
            // §44 加:同后山1 §21 的"上下浮 + 呼吸缩放"动画(Y=405+pandaDy,graphicsLayer 缩放)
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

            // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=43, Y=390, W=51, H=91)
            Box(
                modifier = Modifier
                    .offset(x = 43.dp, y = 390.dp)
                    .size(width = 51.dp, height = 91.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6, 颜色 #385816, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=105, Y=295, W=30, H=53.5)
            Box(
                modifier = Modifier
                    .offset(x = 105.dp, y = 295.dp)
                    .size(width = 30.dp, height = 53.5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=124, Y=521, W=96, H=170)
            Box(
                modifier = Modifier
                    .offset(x = 124.dp, y = 521.dp)
                    .size(width = 96.dp, height = 170.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "拆招心法" 竖排文字(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14, 黑色, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 48.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12, 颜色 #385816, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 25.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 点击回到后山2 页
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

/**
 * 云朵随机飘动 helper:3 个独立 LaunchedEffect 协程并行,
 * 每次随机选目标值 + 随机 delay,产生 X/Y/Alpha 三维自然飘动。
 *
 * 复制自 Houshan1Screen.kt §13 — 因为 helper 是 private fun,不能跨文件共享。
 * TODO:后续可抽出到 ui/util/CloudFloat.kt 共享(同 §28 设计考虑)。
 */
@Composable
private fun rememberCloudFloat(
    maxX: Float = 100f,
    maxY: Float = 15f,
    alphaMin: Float = 0.5f,
    alphaMax: Float = 1f,
    xDuration: IntRange = 1500..3000,
    xDelay: LongRange = 500L..1500L,
): Triple<Float, Float, Float> {
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            x.animateTo(
                targetValue = Random.nextFloat() * 2f * maxX - maxX,
                animationSpec = tween(
                    durationMillis = Random.nextInt(xDuration.first, xDuration.last + 1),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(xDelay.first, xDelay.last + 1))
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            y.animateTo(
                targetValue = Random.nextFloat() * 2f * maxY - maxY,
                animationSpec = tween(
                    durationMillis = Random.nextInt(1000, 2000),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(300, 800))
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            alpha.animateTo(
                targetValue = alphaMin + Random.nextFloat() * (alphaMax - alphaMin),
                animationSpec = tween(
                    durationMillis = Random.nextInt(1500, 3000),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(500, 1200))
        }
    }
    return Triple(x.value, y.value, alpha.value)
}
