package com.jueqiao.jianghu.ui.screens.gunlun1

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import com.jueqiao.jianghu.ui.screens.home.DecorButton
import com.jueqiao.jianghu.ui.screens.home.HomeGuideBubble
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val EntrancesDelayMillis = 550L
private const val EntrancesDurationMillis = 680
private val EntrancesOffset = 56.dp
// 8dp 在目标 420dpi 设备上可避免分层 offset 的独立取整改变原语义边界。
private val EntryTintPadding = 8.dp

private val AcquiredManualStates = setOf(
    "DISCOVERED",
    "LEARNED",
    "MASTERED",
    "TEACHING",
)

internal fun LearningOverviewDto.hasAcquiredManual(): Boolean =
    books.any { book -> book.state.trim().uppercase() in AcquiredManualStates }

/**
 * 修炼分流页：保留原湖面、打坐熊猫、气泡以及“修炼/后山”两个入口。
 * 页面专属的上移淡入状态只在这里维护，避免改变其他引导页的展开节奏。
 */
@Composable
fun Gunlun1Screen(
    onBack: () -> Unit = {},
    onOpenWushuhuan: () -> Unit = {},
    onOpenHoushan1: () -> Unit = {},
    learningOverview: LearningOverviewDto? = null,
) {
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val entrancesAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val entrancesMovement = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val entranceOffsetPx = with(LocalDensity.current) { EntrancesOffset.toPx() }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            entrancesAlpha.snapTo(1f)
            entrancesMovement.snapTo(1f)
            return@LaunchedEffect
        }
        entrancesAlpha.snapTo(0f)
        entrancesMovement.snapTo(0f)
        delay(EntrancesDelayMillis)
        coroutineScope {
            launch {
                entrancesAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = EntrancesDurationMillis,
                        easing = LinearEasing,
                    ),
                )
            }
            launch {
                entrancesMovement.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = EntrancesDurationMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }

    val entranceTranslationY =
        (1f - entrancesMovement.value) * entranceOffsetPx
    val entrancesEnabled =
        entrancesAlpha.value >= 0.99f && entrancesMovement.value >= 0.99f
    // null 表示真实数据尚未返回；此时不先闪现“未获得秘籍”的提示。
    val hasAcquiredManual = learningOverview?.hasAcquiredManual()

    // 熊猫在本页只承担场景引导，不是隐藏的页面推进热区。
    StandardGunlunScaffold(onBack = onBack) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // 只收拢绝对坐标，保留入口原尺寸，避免极小窗口将48dp触控区裁掉。
            val positionScale = minOf(
                maxWidth.value / 400f,
                maxHeight.value / 820f,
                1f,
            )

            GunlunEntryButton(
                imageRes = R.drawable.img_gunlun1_untitled_1_50,
                accessibilityLabel = when (hasAcquiredManual) {
                    false -> "前往后山试炼，寻找秘籍"
                    else -> "进入后山问道"
                },
                x = 247.dp * positionScale,
                y = 165.dp * positionScale,
                width = 55.dp,
                height = 117.12.dp,
                tint = when (hasAcquiredManual) {
                    false -> Color(0xFFC9A75D)
                    true -> Color(0xFF6F9184)
                    null -> null
                },
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY = entranceTranslationY,
                entranceEnabled = entrancesEnabled,
                onClick = onOpenHoushan1,
            )
            GunlunEntryLabel(
                text = "后山",
                x = 247.dp * positionScale,
                y = 165.dp * positionScale,
                width = 55.dp,
                height = 117.12.dp,
                textOffsetX = (-3).dp,
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY = entranceTranslationY,
            )

            // 只有后端已确认没有已获得秘籍时才显示，加载和首次未知态不闪烁空态。
            if (hasAcquiredManual == false) {
                GunlunEmptyManualGuide(
                    modifier = Modifier
                        .offset(x = 23.dp * positionScale, y = 324.dp * positionScale)
                        .size(width = 148.dp, height = 84.dp),
                )
            }

            GunlunEntryButton(
                imageRes = R.drawable.img_gunlun1_untitled_150,
                accessibilityLabel = when (hasAcquiredManual) {
                    true -> "进入悟书环，已有秘籍可以修习"
                    false -> "修炼，尚未获得秘籍"
                    null -> "修炼，正在读取秘籍状态"
                },
                x = 35.dp * positionScale,
                y = 548.dp * positionScale,
                width = 55.dp,
                height = 117.dp,
                tint = if (hasAcquiredManual == true) Color(0xFF4F8F72) else null,
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY = entranceTranslationY,
                entranceEnabled = entrancesEnabled,
                // 未获得或尚未确认状态时留在原场景，避免进入空白悟书环。
                onClick = {
                    if (hasAcquiredManual == true) onOpenWushuhuan()
                },
            )
            GunlunEntryLabel(
                text = "修炼",
                x = 35.dp * positionScale,
                y = 548.dp * positionScale,
                width = 55.dp,
                height = 117.dp,
                textOffsetX = (-5).dp,
                entranceAlpha = entrancesAlpha.value,
                entranceTranslationY = entranceTranslationY,
            )
        }
    }
}

/**
 * 页面私有着色适配层。入口仍使用共享 DecorButton，因此按压、流光和 440ms 回调节奏
 * 保持不变；着色只作用于本页离屏绘制结果，不会改变其他业务页面。
 */
@Composable
private fun GunlunEntryButton(
    imageRes: Int,
    accessibilityLabel: String,
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    tint: Color?,
    entranceAlpha: Float,
    entranceTranslationY: Float,
    entranceEnabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .offset(x = x - EntryTintPadding, y = y - EntryTintPadding)
            .size(
                width = width + EntryTintPadding * 2,
                height = height + EntryTintPadding * 2,
            )
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslationY
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                if (tint != null) {
                    drawRect(
                        color = tint.copy(alpha = 0.48f),
                        blendMode = BlendMode.SrcAtop,
                    )
                }
            },
    ) {
        DecorButton(
            imageRes = imageRes,
            text = "",
            accessibilityLabel = accessibilityLabel,
            x = EntryTintPadding,
            y = EntryTintPadding,
            width = width,
            height = height,
            entranceAlpha = 1f,
            entranceTranslationY = 0f,
            entranceEnabled = entranceEnabled,
            onClick = onClick,
        )
    }
}

@Composable
private fun GunlunEmptyManualGuide(modifier: Modifier = Modifier) {
    HomeGuideBubble(
        text = "还未获得秘籍，先去后山试炼，寻觅机缘吧。",
        modifier = modifier,
        tailPointsRight = true,
        horizontalPadding = 12.dp,
        verticalPadding = 8.dp,
    )
}

@Composable
private fun GunlunEntryLabel(
    text: String,
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    textOffsetX: Dp,
    entranceAlpha: Float,
    entranceTranslationY: Float,
) {
    Column(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslationY
            }
            .offset(x = textOffsetX)
            .clearAndSetSemantics { },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        text.forEach { character ->
            Text(
                text = character.toString(),
                color = Color.White,
                style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
            )
        }
    }
}
