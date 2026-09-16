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
import androidx.compose.runtime.LaunchedEffect
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
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
 *   - 6 朵云 (58/61/56/57/60/60b) 带随机飘动
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

    // 云朵 56 椭圆飘动:7s 一圈,半径 ±40 dp
    val cloud56Transition = rememberInfiniteTransition(label = "cloud56Float")
    val cloud56Angle by cloud56Transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
        ),
        label = "cloud56Angle",
    )
    val cloud56Dx = (sin(cloud56Angle).toFloat() * 40f)
    val cloud56Dy = (cos(cloud56Angle).toFloat() * 40f)

    // 4 朵云随机飘动 (58/61/57/60):统一 rememberCloudFloat() helper
    val (cloud58Dx, cloud58Dy, cloud58Alpha) = rememberCloudFloat()
    val (cloud61Dx, cloud61Dy, cloud61Alpha) = rememberCloudFloat()
    val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()
    val (cloud60Dx, cloud60Dy, cloud60Alpha) = rememberCloudFloat(
        xDuration = 4000..6000,
        xDelay = 1000L..2000L,
    )
    val (cloud60bDx, cloud60bDy, cloud60bAlpha) = rememberCloudFloat(
        xDuration = 4000..6000,
        xDelay = 1000L..2000L,
    )

    // ── 后山2 与后山3 共享的拆招心法下方动画(§44/§6 §11 §15)────────────────
    // 共享 1 个 rememberInfiniteTransition 给 3 朵 AnimatedCloudImage,与后山3 一致
    val transition = rememberInfiniteTransition(label = "h2CloudBands")
    val cloudProgress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud58",
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

                // ── 后山2 与后山3 共享的拆招心法下方动画(§44/§6 §11 §15)────────────
                // 位置基于后山2 拆招心法 center X=205(后山1 同位置,后山3 是 172,横向偏移 +33dp),
                // Y 基于后山2 拆招心法底边 476dp(后山3 是 691dp,上移 -215dp)。
                // 放在景深平面 2 内 → 过渡推进时随云朵一起淡出,层次一致
                FocusCloudBand(
                    // 中下:拆招心法正下方,底边 +49dp gap
                    xOffset = 45f, yOffset = 525f,
                    widthDp = 320f, heightDp = 110f,
                    // §20:周期 8→10s(单程 5s),amplitudeX 保持 200
                    amplitudeX = 200f, amplitudeY = 42f,
                    baseAlpha = 0.50f, alphaAmp = 0.30f,
                    periodMs = 10_000,
                )
                FocusCloudBand(
                    // 左下:在标签 1(识机真决)附近,后山2 还有这个标签
                    xOffset = -30f, yOffset = 740f,
                    widthDp = 240f, heightDp = 120f,
                    // §20:周期 9→11s
                    amplitudeX = 200f, amplitudeY = 36f,
                    baseAlpha = 0.50f, alphaAmp = 0.30f,
                    periodMs = 11_000,
                )
                AnimatedCloudImage(
                    // 云 58:左下角,横椭圆
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -27f, yOffset = 488f,
                    widthDp = 240f, heightDp = 135f,
                    progress = cloudProgress, phase = 0.13f,
                    // §19:振幅加大
                    amplitudeX = 200f, amplitudeY = 15f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                )
                AnimatedCloudImage(
                    // 云 60:右侧,扁长
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60",
                    xOffset = 153f, yOffset = 471f,
                    widthDp = 280f, heightDp = 108f,
                    progress = cloudProgress, phase = 0.31f,
                    // §19:振幅加大
                    amplitudeX = 200f, amplitudeY = 8f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                )
                AnimatedCloudImage(
                    // 云 62:正下方,中等扁长
                    painter = painterResource(R.drawable.img_houshan3_cloud_62),
                    contentDescription = "云朵62",
                    xOffset = 83f, yOffset = 556f,
                    widthDp = 240f, heightDp = 98f,
                    progress = cloudProgress, phase = 0.71f,
                    // §19:振幅加大
                    amplitudeX = 200f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                )

                // 云朵 58
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-70f + cloud58Dx).dp, y = (320f + cloud58Dy).dp)
                        .size(width = 455.dp, height = 259.dp),
                    alpha = cloud58Alpha,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 61
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_61),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-50f + cloud61Dx).dp, y = (304f + cloud61Dy).dp)
                        .size(width = 355.dp, height = 213.dp),
                    alpha = cloud61Alpha,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 56 (alpha=1f 100% 不透明,椭圆飘动)
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_56),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (196f + cloud56Dx).dp, y = (595f + cloud56Dy).dp)
                        .size(width = 335.dp, height = 297.dp),
                    alpha = 1f,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 57
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_57),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (208f + cloud57Dx).dp, y = (570f + cloud57Dy).dp)
                        .size(width = 225.dp, height = 191.dp),
                    alpha = cloud57Alpha,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 60
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-21f + cloud60Dx).dp, y = (570f + cloud60Dy).dp)
                        .size(width = 355.dp, height = 137.dp),
                    alpha = cloud60Alpha,
                    contentScale = ContentScale.FillBounds,
                )

                // 云朵 60b (副本,Y=690)
                Image(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-21f + cloud60bDx).dp, y = (770f + cloud60bDy).dp)
                        .size(width = 355.dp, height = 137.dp),
                    alpha = cloud60bAlpha,
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

/**
 * 云朵随机飘动 helper:3 个独立 LaunchedEffect 协程并行,
 * 每次随机选目标值 + 随机 delay,产生 X/Y/Alpha 三维自然飘动。
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
