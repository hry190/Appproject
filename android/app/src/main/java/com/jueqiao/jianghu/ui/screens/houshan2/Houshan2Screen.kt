package com.jueqiao.jianghu.ui.screens.houshan2

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 后山2 → 后山3 沉浸式纵深推进(dolly-in)参数 ──────────────────────────────
// 总时长落在用户要求的 0.8~1.2s 区间;ease-in-out 用 FastOutSlowInEasing
// (cubic-bezier 0.4, 0.0, 0.2, 1.0,即标准缓入缓出)。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山3 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心,与后山3 熊猫/标签所在高度对齐
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山2 页 — 后山1 页 → 点击整屏纵深推进过渡到后山3 页。
 *
 * 2026-09-15 §18 重写:基于 Houshan1Screen.kt 复制,保留全部 6 朵云动画 + 4 个标签 +
 * 返回按钮,去掉熊猫 (img_shilian_panda) 和 Rectangle156 气泡及文字。
 *
 * 2026-09-15 §36 加"沉浸式过渡动画"(用户指令):点击整屏不再是硬切,而是相机穿行山间:
 *   1. 三个景深平面绕同一灭点 [FOCAL_X, FOCAL_Y] 以不同幅度放大 —
 *      背景山体 ×1.34(向用户靠近)、云雾 ×1.09 并淡出(相对后移)、标签 ×1.34 并淡出
 *      (与山体同速,不产生相对滑动;"跳动"即由此避免)
 *   2. 推进到半程 (DOLLY_HANDOFF_MS) 调 onOpenHoushan3(),后山3 由
 *      JianghuNavHost 的 enterTransition 交叉淡入 + 轻微回落(1.10 → 1.00),
 *      读作镜头减速停稳,因此全程无闪切
 *   3. 返回按钮只淡出不缩放(属 UI chrome,不应随景深放大)
 *
 * 布局:
 *   - 全屏背景图 (img_shilian_bg.png)
 *   - 6 朵云 (58/61/56/57/60/60b) —— §21 起改为**静态图层**(用户指令"老云保留为静态、去掉动画")
 *   - §21 动画云元素 6 个(竖排,间隔 60~69dp,前 5 个 ×0.75):ACI58 y=8 · FCB左下 y=178.5 · ACI60 y=330.5
 *     · FCB中下 y=477.5 · ACI62 y=620 · **ACI57 y=754.5(100×90,§21b 实图云 PNG)**
 *     第 6 个底边 844.5dp,距导航栏上沿 857dp 留 12.5dp
 *   - 标签1 图像 (X=-13, Y=570, W=106, H=188) + 文字"识机真决" + 文字"炼"
 *   - 标签2 图像 (X=168, Y=345, W=74, H=131) + 文字"拆招心法" + 文字"炼"
 *   - 标签3 图像 (X=113, Y=322, W=50, H=88) + 文字"万象谱" + 文字"炼"
 *   - 标签4 图像 (X=151, Y=248, W=30, H=53.5) + 文字"寻径迷踪步" + 文字"炼"
 *   - 左上角返回按钮 (Return.png, X=30, Y=60, W=18, H=18)
 *
 * 删除元素:
 *   - 熊猫 (img_shilian_panda) — 用户指令 §18
 *   - Rectangle156.png 气泡 + 文字"御剑穿行云雾群山..." — 用户指令 §18
 */
@Composable
fun Houshan2Screen(
    onBack: () -> Unit = {},
    onOpenHoushan3: () -> Unit = {},
    onOpenVolume1: () -> Unit = {},  // 识机真决标签跳转第一卷-1 (§22)
) {
    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 过渡期间禁用返回手势,避免动画途中被中断而露出半程画面
    BackHandler(enabled = !isTransitioning) { onBack() }

    // 熊猫已按 §18 去掉;§21 起 6 朵老云也去掉动画,改为静态图层

    // ── §21c 6 个动画云元素的进度:每个元素独立周期(与后山1/后山3 完全同一套)──
    // 单向组 7/8/9s + 摆动组 10/11/13s → 六个数 lcm ≈ 4.2 天,看不出规律性同步
    val cloudTransition = rememberInfiniteTransition(label = "h2Clouds")
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

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (§36) ──────────────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 整屏点击:启动纵深推进,并在半程把控制权交给导航(后山3 交叉淡入)
    val startDollyIn: () -> Unit = {
        if (!isTransitioning) {
            isTransitioning = true
            scope.launch {
                dolly.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = DOLLY_DURATION_MS, easing = FastOutSlowInEasing),
                )
            }
            scope.launch {
                delay(DOLLY_HANDOFF_MS)
                onOpenHoushan3()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { startDollyIn() },
    ) {
        // ── 景深平面 1:背景山体(推进最多 → "向用户靠近")────────────────────
        // 单独一层 fillMaxSize 包裹,使其自身边界 = 屏幕,transformOrigin 才能
        // 表达屏幕空间灭点;缩放后仍 ContentScale.Crop 铺满,不会露边。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    transformOrigin = focal
                },
        ) {
            // 全屏背景图 (后山页背景.png)
            Image(
                painter = painterResource(R.drawable.img_shilian_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cloudScale
                        scaleY = cloudScale
                        transformOrigin = focal
                        alpha = cloudFade
                    },
            ) {
                // 云雾层(程序化水墨云海,持续循环)— 属大气中层 (§37)
                // 放在景深平面 2 之内:过渡推进时与云雾一起"相对后移 + 淡出",层次一致
                HoushanMistLayer()

                // ══ §21 动画云元素竖排(6 个)══════════════════════════════════════
                // 与后山1/后山3 完全同一套 y 序列、同一套间隔(69/62/66/60/61)、同一套 ×0.75 尺寸,
                // 满足"三页效果统一"。用户指令:间隔 60~90dp 定死一组;最底部(导航栏上方)必须有
                // 一个动画素材 → 第 6 个 = ACI57(§21b 按用户指令从 FocusCloudBand 改为实图云 PNG),
                // 底边 844.5dp,距 857dp 留 12.5dp。
                // amplitudeY 收到 8~10dp(原 42/36dp 会把 60~69dp 的间隔上下吃光);amplitudeX 仍 200dp。
                // 放在景深平面 2 内 → §36 过渡推进时随云雾一起 scaleX/Y + 淡出,层次一致,不会"留在屏上不动"。
                // §21c/§21d 横向模式分配 —— **按"观感可见性"分配**(与后山1/后山3 一致):
                //   三朵 ACI 的 PNG 是 250/255/237 的近白/纯白,浅底上几乎不可见;
                //   两条 FocusCloudBand 是冷青 #A9C3C0,浅底上对比明显 → 把**看得见的**放进单向组。
                //   #1 ACI58 摆动(7s) · #2 FCB左下 **单向→右**(11s) · #3 ACI60 **单向→右**(8s)
                //   #4 FCB中下 **单向←左**(10s) · #5 ACI62 摆动(9s) · #6 ACI57 摆动(13s,底部,永远在屏上)
                // §21d/§21e:3 个单向 = **2 个向右(FCB左下 + ACI60)+ 1 个向左(FCB中下)**,
                //   保证两个方向各有一个看得见的元素。
                // 单向模式忽略 xOffset / amplitudeX,故两者传 0f。
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
                // 第 6 个 = 最底部素材(§21b):用户指令从 FocusCloudBand 换成 AnimatedCloudImage(实图云 PNG)。
                // 底边 844.5dp,距导航栏上沿(857dp)留 12.5dp。选 cloud_57 而非扁长的 cloud_60:
                // cloud_60 实测纯白 (255,255,255),叠在近纯白的底部背景上像素差为 0(等于没加);
                // cloud_57 实测 MeanLum 221 / 72% 像素带色调 → 真正看得见(详见 §21 素材亮度表)。
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

                // 云朵 58 (X=-70, Y=320, W=455, H=259)
                // §21 静态化:用户指令"老云保留为静态图层、去掉动画";alpha 取原动画区间 0.5~1.0 的中点 0.75
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-70f).dp, y = 320f.dp)
                        .size(width = 455.dp, height = 259.dp),
                    alpha = 0.75f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 61 (X=-50, Y=304, W=355, H=213)— §21 静态化
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_61),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-50f).dp, y = 304f.dp)
                        .size(width = 355.dp, height = 213.dp),
                    alpha = 0.75f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 56 (X=196, Y=595, W=335, H=297)— §21 静态化;alpha 保持 1f(§7 用户指令 100% 不透明)
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_56),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 196f.dp, y = 595f.dp)
                        .size(width = 335.dp, height = 297.dp),
                    alpha = 1f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 57 (X=208, Y=570, W=225, H=191)— §21 静态化
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_57),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 208f.dp, y = 570f.dp)
                        .size(width = 225.dp, height = 191.dp),
                    alpha = 0.75f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 60 (X=-21, Y=570, W=355, H=137)— §21 静态化
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-21f).dp, y = 570f.dp)
                        .size(width = 355.dp, height = 137.dp),
                    alpha = 0.75f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 60b (X=-21, Y=760, W=355, H=137)— §21 静态化
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-21f).dp, y = 760f.dp)
                        .size(width = 355.dp, height = 137.dp),
                    alpha = 0.75f,
                    contentScale = ContentScale.FillBounds,
                )
            }

            // ── 景深平面 3:标签(与山体同速推进 + 淡出 → 不相对滑动)──────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = labelScale
                        scaleY = labelScale
                        transformOrigin = focal
                        alpha = labelFade
                    },
            ) {
                // "标签1" 图像 (未标题-1-恢复的-恢复的 4.png, X=-13, Y=570, W=106, H=188) — 点击跳转第一卷-1 (§22)
                Box(
                    modifier = Modifier
                        .offset(x = -13.dp, y = 570.dp)
                        .size(width = 106.dp, height = 188.dp)
                        .clickable(enabled = !isTransitioning, onClick = onOpenVolume1),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签1",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    // "识机真决" 竖排文字
                    Text(
                        text = "识\n机\n真\n决",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        modifier = Modifier
                            .offset(x = 46.dp, y = 54.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    // "炼" 文字
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 48.dp, y = 27.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签2" 图像 (X=168, Y=345, W=74, H=131)
                Box(
                    modifier = Modifier
                        .offset(x = 168.dp, y = 345.dp)
                        .size(width = 74.dp, height = 131.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§19)
                        ),
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
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 34.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 17.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签3" 图像 (X=113, Y=322, W=50, H=88)
                Box(
                    modifier = Modifier
                        .offset(x = 113.dp, y = 322.dp)
                        .size(width = 50.dp, height = 88.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§19)
                        ),
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
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 22.dp, y = 12.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像 (X=151, Y=248, W=30, H=53.5)
                Box(
                    modifier = Modifier
                        .offset(x = 151.dp, y = 248.dp)
                        .size(width = 30.dp, height = 53.5.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§19)
                        ),
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
            }

            // 左上角返回按钮 (Return.png, X=30, Y=60, W=18, H=18)— 点击回到后山1 页
            // 只淡出不缩放:UI chrome 不参与景深,否则会随山体放大而"跳动"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .graphicsLayer { alpha = chromeFade }
                    .clickable(enabled = !isTransitioning, onClick = onBack),
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
