package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class ArchiveIntroStage {
    Quiet,
    Opening,
    Open,
    Closing,
}

enum class CreationArchiveEntry {
    Works,
    OriginalRecords,
    VersionRecords,
    CoachRecords,
}

/**
 * 创作档案页面 — 背景为 D:\图\创作档案.png。
 * 顶部仅保留创作台与创作档案叶签；页面内返回创作台统一使用左侧叶签。
 * 系统返回键仍由 onBack 处理。
 */
@Composable
fun ChuangzuodanganScreen(
    onBack: () -> Unit = {},
    onOpenCreationDesk: () -> Unit = {},
) {
    val creationTabInteractionSource = remember { MutableInteractionSource() }
    val creationTabPressed by creationTabInteractionSource.collectIsPressedAsState()
    val creationTabScale by animateFloatAsState(
        targetValue = if (creationTabPressed) 0.96f else 1f,
        animationSpec = tween(140),
        label = "档案页创作台叶签按压",
    )
    var introStage by remember { mutableStateOf(ArchiveIntroStage.Quiet) }
    var activeEntry by remember { mutableStateOf<CreationArchiveEntry?>(null) }
    var transitionLocked by remember { mutableStateOf(false) }
    val transitionScope = rememberCoroutineScope()
    val transitionProgress = remember { Animatable(0f) }
    val mistDrift by rememberInfiniteTransition(label = "创作档案云烟漂移").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "云烟横向漂移",
    )

    val detailProgress = transitionProgress.value
    val flowerProgress = smoothProgress(segmentProgress(detailProgress, 0.02f, 0.48f))
    val mistProgress = smoothProgress(segmentProgress(detailProgress, 0.30f, 0.72f))
    val contentProgress = smoothProgress(segmentProgress(detailProgress, 0.58f, 0.88f))
    val pandaProgress = smoothProgress(segmentProgress(detailProgress, 0.42f, 0.66f))
    val bottomPatchProgress = smoothProgress(segmentProgress(detailProgress, 0.12f, 0.48f))

    // 详情态在当前荷塘页内收起；普通态再离开创作档案页。
    fun closeArchiveOrBack() {
        when (introStage) {
            ArchiveIntroStage.Open -> {
                if (transitionLocked) return
                transitionLocked = true
                introStage = ArchiveIntroStage.Closing
                transitionScope.launch {
                    transitionProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(1_900, easing = FastOutSlowInEasing),
                    )
                    activeEntry = null
                    introStage = ArchiveIntroStage.Quiet
                    transitionLocked = false
                }
            }

            ArchiveIntroStage.Quiet -> onBack()
            ArchiveIntroStage.Opening,
            ArchiveIntroStage.Closing,
            -> Unit
        }
    }

    // 拦截系统返回键 — 行为与页面左上角返回按钮一致
    BackHandler(enabled = true) {
        closeArchiveOrBack()
    }

    fun openArchiveEntry(entry: CreationArchiveEntry) {
        if (transitionLocked || introStage != ArchiveIntroStage.Quiet) return
        activeEntry = entry
        transitionLocked = true
        introStage = ArchiveIntroStage.Opening
        transitionScope.launch {
            transitionProgress.snapTo(0f)
            transitionProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2_800, easing = FastOutSlowInEasing),
            )
            introStage = ArchiveIntroStage.Open
            transitionLocked = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景始终保持同一张荷塘图，避免整页 Crossfade 带来的闪白。
        // 详情背景只在底部荷叶区域逐渐覆盖原荷苞，为盛开过程腾出干净的花心位置。
        Image(
            painter = painterResource(R.drawable.img_chuangzuodangan_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(R.drawable.img_chuangzuodangan3_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bottomPatchProgress }
                .drawWithContent {
                    clipRect(
                        left = 0f,
                        top = 520.dp.toPx(),
                        right = 230.dp.toPx(),
                        bottom = 770.dp.toPx(),
                    ) {
                        this@drawWithContent.drawContent()
                    }
                },
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 返回当前档案页（详情态关闭雾气，普通态返回创作台上一页）。
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 41.dp)
                    .size(32.dp)
                    .clickable(onClick = ::closeArchiveOrBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // 档案页交换叶签状态：创作台恢复普通态，创作档案使用高亮放大态。
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-45).dp, y = 23.dp)
                    .size(width = 160.dp, height = 58.dp)
                    .graphicsLayer {
                        scaleX = creationTabScale
                        scaleY = creationTabScale
                    }
                    .clickable(
                        interactionSource = creationTabInteractionSource,
                        indication = null,
                        onClick = onOpenCreationDesk,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_24),
                    contentDescription = null,
                    modifier = Modifier.size(width = 132.dp, height = 48.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "创作台",
                    color = Color(0xFF294A2E),
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 103.dp, y = 23.dp)
                    .size(width = 160.dp, height = 58.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_23),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "创作档案",
                    color = Color(0xFF294A2E),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            // 修改版本记录.png(X=226, Y=384.5, W=120.47, H=126.12)
// 弧形文字:6 字沿弧线排列,首字 51° 顺时针,每字向逆时针递减 10.2°,末字回 0°(整体 -51°)
val arcText = "修改版本记录"
val arcN = arcText.length
for (i in 0 until arcN) {
    val t = i.toFloat() / (arcN - 1).toFloat()
    val arcAngleRad = (200.0 - 90.0 * t) * PI / 180.0
    val charX = (276.0 + 60.235 + 60.5 * cos(arcAngleRad)).toFloat()
    val charY = (325.5 + 69.4 + 60.5 * sin(arcAngleRad)).toFloat()
    // 单字旋转:不跟弧度,首字 51° CW,末字 0°,每字向逆时针递减 51°/5 = 10.2°
    val rot = 51f * (arcN - 1 - i) / (arcN - 1).toFloat()

    Text(
        text = arcText[i].toString(),
        color = Color(0xFF437349),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
        ),
        modifier = Modifier
            .offset(
                x = charX.dp,
                y = charY.dp,
            )
            .rotate(rot)
            .graphicsLayer { alpha = 1f - 0.78f * flowerProgress },
    )
}

// 原创记录.png(X=18, Y=333, W=116.5, H=94.11)
// 圆心在"原"上方 50 单位;"原"保持在原位,其余三字绕圆心排布
// 旋转:首字 0°,末字 -45°,每字向逆时针递减 15°
// 颜色:从左到右 浅黄绿(#B8D878) → 深草绿(#5A8A3A),每字内水平渐变
val chuangyuanText = "原创记录"
val chuangyuanN = chuangyuanText.length
val chuangyuanBaseX = 48f + 12f          // 55 — "原" 的 X
val chuangyuanBaseY = 303f + 47.055f     // 350.055 — "原" 的 Y
val chuangyuanCenterX = chuangyuanBaseX  // 55 — 圆心 X(直接在"原"上方)
val chuangyuanCenterY = chuangyuanBaseY - 50f  // 300.055 — 圆心 Y
val chuangyuanRadius = 50f               // 半径(正好让"原"在弧底)
// 4 字角度分布(math 度):90°, 70°, 50°, 25°(从"原"顺时针往上排)
val chuangyuanAnglesDeg = listOf(90f, 70f, 50f, 25f)
val chuangyuanFirstRot = 0f
val chuangyuanLastRot = -45f
val chuangyuanGradientStart = Color(0xFFB8D878)  // 浅黄绿 light yellow-green
val chuangyuanGradientEnd   = Color(0xFF5A8A3A)  // 深草绿 dark grass green
for (i in 0 until chuangyuanN) {
    val t = i.toFloat() / (chuangyuanN - 1).toFloat()
    val arcAngleRad = chuangyuanAnglesDeg[i].toDouble() * PI / 180.0
    val charX = (chuangyuanCenterX + chuangyuanRadius * cos(arcAngleRad)).toFloat()
    val charY = (chuangyuanCenterY + chuangyuanRadius * sin(arcAngleRad)).toFloat()
    val rot = chuangyuanFirstRot + (chuangyuanLastRot - chuangyuanFirstRot) * t
    // 每字内部水平渐变:取该字在整体渐变中的"切片"(左 t 到右 t)
    val rightT = (i + 1).toFloat() / (chuangyuanN - 1).toFloat()
    fun lerpColor(start: Color, end: Color, tt: Float) = Color(
        red   = start.red   + (end.red   - start.red)   * tt,
        green = start.green + (end.green - start.green) * tt,
        blue  = start.blue  + (end.blue  - start.blue)  * tt,
        alpha = 1f,
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(
            lerpColor(chuangyuanGradientStart, chuangyuanGradientEnd, t),
            lerpColor(chuangyuanGradientStart, chuangyuanGradientEnd, rightT),
        ),
    )

    Text(
        text = chuangyuanText[i].toString(),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
            brush = brush,
        ),
        modifier = Modifier
            .offset(
                x = charX.dp,
                y = charY.dp,
            )
            .rotate(rot)
            .graphicsLayer { alpha = 1f - flowerProgress },
    )
}

// 选择作品查看.png(X=-2, Y=143, W=103, H=101)
// 6 字绕圆心排布,圆心在"选"下方 50 单位,首字 25° 顺时针,末字 80° 顺时针
val xuanzeText = "选择作品查看"
val xuanzeN = xuanzeText.length
val xuanzeBaseX = 45f                // "选" 的 X
val xuanzeBaseY = 137f               // "选" 的 Y
val xuanzeCenterX = xuanzeBaseX      // -2 — 圆心 X(直接在"选"正下方)
val xuanzeCenterY = xuanzeBaseY + 50f  // 193 — 圆心 Y
val xuanzeRadius = 50f               // 半径(让"选"在弧顶)
val xuanzeFirstRot = 25f             // 首字 25° CW
val xuanzeLastRot = 80f              // 末字 80° CW
val xuanzeColor = Color(0xFF62704E)
for (i in 0 until xuanzeN) {
    val t = i.toFloat() / (xuanzeN - 1).toFloat()
    // 弧度角:从 -90°(正上方,即"选"位置)扫到 0°(正右方),90° 总扫角
    val arcAngleDeg = -90f + 90f * t
    val arcAngleRad = arcAngleDeg.toDouble() * PI / 180.0
    val charX = (xuanzeCenterX + xuanzeRadius * cos(arcAngleRad)).toFloat()
    val charY = (xuanzeCenterY + xuanzeRadius * sin(arcAngleRad)).toFloat()
    val rot = xuanzeFirstRot + (xuanzeLastRot - xuanzeFirstRot) * t

    Text(
        text = xuanzeText[i].toString(),
        color = xuanzeColor,
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 12.sp,
        ),
        modifier = Modifier
            .offset(
                x = charX.dp,
                y = charY.dp,
            )
            .rotate(rot)
            .graphicsLayer { alpha = 1f },
    )
}

// AI教练辅助记录.png(X=3, Y=666, W=126.5, H=162.3)— 整组可点击跳 Chuangzuodangan3
// 8 字绕圆心排布,圆心在"助"上方 70 单位;首字 A/I 51° CW,"助" 0° 锚点,末字 -20°
// 颜色:前 3 字墨绿(#2E7D32),后 5 字浅绿(#81C784)
val aiText = "AI教练辅助记录"
val aiN = aiText.length
val aiBaseX = 114f                  // "助" 的 X
val aiBaseY = 735f                 // "助" 的 Y
val aiCenterX = aiBaseX            // 75 — 圆心 X(直接在"助"正上方)
val aiCenterY = aiBaseY - 70f      // 680 — 圆心 Y("助"上方 70)
val aiRadius = 70f                 // 半径(让"助"在弧底)
// 8 字角度分布:从 A(155° 左侧)→ 助(90° 底)→ 录(60° 右侧)
val aiAnglesDeg = listOf(155f, 145f, 135f, 120f, 105f, 90f, 75f, 60f)
// 旋转:前两字同 75°(原 51°),中间 4 字线性到 0°,后两字线性到 -20°
val aiRotations = listOf(75f, 75f, 56.25f, 37.5f, 18.75f, 0f, -10f, -20f)
val aiGradientStart = Color(0xFF4A5D3A)  // 墨绿 dark olive green
val aiGradientEnd = Color(0xFF3D8A4A)    // 翠绿 emerald green
for (i in 0 until aiN) {
    val arcAngleRad = aiAnglesDeg[i].toDouble() * PI / 180.0
    val charX = (aiCenterX + aiRadius * cos(arcAngleRad)).toFloat()
    val charY = (aiCenterY + aiRadius * sin(arcAngleRad)).toFloat()
    val rot = aiRotations[i]
    // 每字内部水平渐变:取该字在整体渐变中的"切片"(左 t 到右 t)
    val leftT = i.toFloat() / (aiN - 1).toFloat()
    val rightT = (i + 1).toFloat() / (aiN - 1).toFloat()
    fun lerpColor(start: Color, end: Color, t: Float) = Color(
        red   = start.red   + (end.red   - start.red)   * t,
        green = start.green + (end.green - start.green) * t,
        blue  = start.blue  + (end.blue  - start.blue)  * t,
        alpha = 1f,
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(lerpColor(aiGradientStart, aiGradientEnd, leftT),
                        lerpColor(aiGradientStart, aiGradientEnd, rightT)),
    )

    Text(
        text = aiText[i].toString(),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
            brush = brush,
        ),
        modifier = Modifier
            .offset(
                x = charX.dp,
                y = charY.dp,
            )
            .rotate(rot)
            .graphicsLayer { alpha = 1f },
    )
}

// 四个入口均在当前荷塘页面内展开：花苞盛开、雾气扩散、内容浮现。
// 只有安静态允许再次选择入口，避免动画期间误触。
val archiveEntriesEnabled = introStage == ArchiveIntroStage.Quiet
ArchiveEntryHotspot(
    x = 0.dp,
    y = 143.dp,
    width = 120.dp,
    height = 110.dp,
    enabled = archiveEntriesEnabled,
) { openArchiveEntry(CreationArchiveEntry.Works) }
ArchiveEntryHotspot(
    x = 18.dp,
    y = 333.dp,
    width = 117.dp,
    height = 96.dp,
    enabled = archiveEntriesEnabled,
) { openArchiveEntry(CreationArchiveEntry.OriginalRecords) }
ArchiveEntryHotspot(
    x = 226.dp,
    y = 384.dp,
    width = 125.dp,
    height = 130.dp,
    enabled = archiveEntriesEnabled,
) { openArchiveEntry(CreationArchiveEntry.VersionRecords) }
ArchiveEntryHotspot(
    x = 3.dp,
    y = 666.dp,
    width = 128.dp,
    height = 162.dp,
    enabled = archiveEntriesEnabled,
) { openArchiveEntry(CreationArchiveEntry.CoachRecords) }

// 详情内容始终叠加在同一张背景上，通过同一条进度曲线完成开合，避免页面闪动。
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan3_rect245),
    contentDescription = null,
    modifier = Modifier
        .fillMaxWidth()
        .offset(y = 55.dp)
        .graphicsLayer {
            alpha = mistProgress * 0.88f
            scaleX = 0.92f + 0.08f * mistProgress
            scaleY = 0.92f + 0.08f * mistProgress
            translationX = (mistDrift - 0.5f) * 8.dp.toPx()
        },
    contentScale = ContentScale.FillWidth,
)
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan3_image61),
    contentDescription = null,
    modifier = Modifier
        .offset(x = (35f + mistDrift * 10f).dp, y = (434f + (1f - mistDrift) * 5f).dp)
        .size(width = 193.dp, height = 203.dp)
        .graphicsLayer { alpha = mistProgress * 0.78f },
    contentScale = ContentScale.Fit,
)

val detailText = when (activeEntry) {
    CreationArchiveEntry.Works -> "《未命名作品》\n当前阶段：创作草稿\n点击荷花可继续编辑作品"
    CreationArchiveEntry.OriginalRecords -> "9点32分18秒，记录灵感来源\n9点36分42秒，确定创作方向\n9点41分05秒，补充关键细节"
    CreationArchiveEntry.VersionRecords -> "V1，完成主题草稿\nV2，调整主体构图\nV3，保留当前创作版本"
    CreationArchiveEntry.CoachRecords, null -> "9点39分36秒，提示确认主题\n9点45分32秒，提出构图建议\n9点50分01秒，引导修改细节"
}
Text(
    text = detailText,
    color = Color.Black,
    style = TextStyle(
        fontFamily = YaHei,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    modifier = Modifier
        .offset(x = 117.dp, y = (346f + (1f - contentProgress) * 12f).dp)
        .size(width = 189.dp, height = 72.dp)
        .graphicsLayer { alpha = contentProgress },
)

// 只有目标荷花在花苞位置逐渐展开，花心固定在荷叶上，避免缩放跳动。
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan3_image52),
    contentDescription = "打开创作档案",
    modifier = Modifier
        .offset(x = 8.dp, y = 556.dp)
        .size(width = 214.dp, height = 172.dp)
        .graphicsLayer {
            alpha = flowerProgress
            val bloomScale = 0.18f + 0.82f * flowerProgress
            scaleX = bloomScale
            scaleY = 0.24f + 0.76f * flowerProgress
            translationY = (1f - flowerProgress) * 10.dp.toPx()
            rotationZ = -2.5f * (1f - flowerProgress)
        },
    contentScale = ContentScale.Fit,
)

// 熊猫姿态也随荷花完成交接，开合过程不会出现两只熊猫重影。
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan_untitled172),
    contentDescription = "创作档案引导熊猫",
    modifier = Modifier
        .offset(x = 201.dp, y = 584.dp)
        .size(width = 212.dp, height = 245.dp)
        .graphicsLayer { alpha = 1f - pandaProgress },
    contentScale = ContentScale.Fit,
)
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan3_image64),
    contentDescription = null,
    modifier = Modifier
        .offset(x = 204.dp, y = 667.dp)
        .size(width = 200.dp, height = 222.dp)
        .graphicsLayer {
            alpha = pandaProgress
            translationY = (1f - pandaProgress) * 8.dp.toPx()
        },
    contentScale = ContentScale.Fit,
)

// 详情态气泡复用首页同款纸笺样式，提示用户再次点击荷花关闭。
HomeGuideBubble(
    text = "关闭页面请\n再次点击荷花\n即可",
    tailPointsRight = true,
    modifier = Modifier
        .offset(x = 260.dp, y = 622.dp)
        .size(width = 155.dp, height = 88.dp)
        .graphicsLayer {
            alpha = contentProgress
            val bubbleScale = 0.92f + 0.08f * contentProgress
            scaleX = bubbleScale
            scaleY = bubbleScale
        },
)

// 四个入口的点击区域复用同一套开花流程；打开后只允许点击荷花关闭。
ArchiveEntryHotspot(
    x = 0.dp,
    y = 535.dp,
    width = 230.dp,
    height = 220.dp,
    enabled = introStage == ArchiveIntroStage.Open,
) { closeArchiveOrBack() }
        }
    }
}

private fun segmentProgress(value: Float, start: Float, end: Float): Float {
    if (end <= start) return 1f
    return ((value - start) / (end - start)).coerceIn(0f, 1f)
}

private fun smoothProgress(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}

@Composable
private fun BoxScope.ArchiveEntryHotspot(
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                indication = null,
                onClick = onClick,
            ),
    )
}
