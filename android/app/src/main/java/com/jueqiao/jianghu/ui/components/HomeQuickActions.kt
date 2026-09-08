package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 首页和三个引导状态共用的快捷入口布局参数。 */
object HomeQuickActionsLayout {
    val EndOffset = (-12).dp
    val TopOffset = 71.dp
}

/**
 * 问道、修为、书信、设置四个入口的唯一实现。
 *
 * 所有承载顶部快捷栏的页面复用这一组件，确保图标、间距、文字与书信封蜡提示完全一致。
 */
@Composable
fun HomeQuickActions(
    onOpenWendao: () -> Unit,
    onOpenCultivation: () -> Unit,
    onOpenLetters: () -> Unit,
    onOpenSettings: () -> Unit,
    hasUnreadLetters: Boolean = false,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UtilityActionItem(
            icon = UtilityIcon.Wendao,
            label = "问道",
            contentDescription = "问道，查看下一步学习方向",
            onClick = onOpenWendao,
        )
        UtilityActionItem(
            icon = UtilityIcon.Cultivation,
            label = "修为",
            contentDescription = "修为，查看学习与创作成长",
            onClick = onOpenCultivation,
        )
        UtilityActionItem(
            icon = UtilityIcon.Letter,
            label = "书信",
            contentDescription = "书信，查看评招、邀请与提醒",
            showSeal = hasUnreadLetters,
            onClick = onOpenLetters,
        )
        UtilityActionItem(
            icon = UtilityIcon.Settings,
            label = "设置",
            contentDescription = "设置",
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun UtilityActionItem(
    icon: UtilityIcon,
    label: String,
    contentDescription: String,
    showSeal: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isClickSelected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isActive = isPressed || isClickSelected
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isClickSelected -> 1.06f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "utilityActionScale",
    )
    val lift by animateFloatAsState(
        targetValue = if (isClickSelected) -1.5f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "utilityActionLift",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.11f
            isClickSelected -> 0.15f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "utilityActionHalo",
    )
    val haloScale by animateFloatAsState(
        targetValue = if (isClickSelected) 1.10f else 0.82f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "utilityActionHaloScale",
    )
    val foreground by animateColorAsState(
        targetValue = if (isActive) UtilityJadeHighlight else UtilityInk,
        animationSpec = tween(durationMillis = 150),
        label = "utilityActionTint",
    )

    Column(
        modifier = Modifier
            .width(30.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = lift
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!isClickSelected) {
                        isClickSelected = true
                        scope.launch {
                            delay(280)
                            isClickSelected = false
                        }
                        onClick()
                    }
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(23.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(27.dp)
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        alpha = haloAlpha
                    }
                    .background(UtilityJadeWash, CircleShape),
            )
            when (icon) {
                UtilityIcon.Wendao -> WendaoRingIcon(contentDescription, foreground)
                UtilityIcon.Cultivation -> CultivationTalismanIcon(contentDescription, foreground)
                UtilityIcon.Letter -> Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = contentDescription,
                    tint = foreground,
                    modifier = Modifier.size(23.dp),
                )
                UtilityIcon.Settings -> Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = contentDescription,
                    tint = foreground,
                    modifier = Modifier.size(23.dp),
                )
            }
            if (showSeal) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(SealRed, CircleShape)
                        .border(1.dp, Color(0xFFF4E8D2), CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = UtilityInk,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
            ),
        )
    }
}

private enum class UtilityIcon { Wendao, Cultivation, Letter, Settings }

/** 与首页预览一致的“问道环印”：外环、内环、路径和一点悟光组成。 */
@Composable
private fun WendaoRingIcon(contentDescription: String, tint: Color) {
    Canvas(
        modifier = Modifier
            .size(23.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val side = size.minDimension
        val scale = side / 24f
        fun point(x: Float, y: Float) = Offset(x * scale, y * scale)
        val stroke = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        drawArc(
            color = tint,
            startAngle = 30f,
            sweepAngle = 292f,
            useCenter = false,
            topLeft = point(2.5f, 2.5f),
            size = Size(19f * scale, 19f * scale),
            style = stroke,
        )
        drawArc(
            color = tint,
            startAngle = 205f,
            sweepAngle = 215f,
            useCenter = false,
            topLeft = point(6.2f, 6.1f),
            size = Size(11.6f * scale, 11.6f * scale),
            style = stroke,
        )
        drawLine(
            color = tint,
            start = point(8.9f, 13.4f),
            end = point(13.9f, 17.0f),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = tint,
            radius = 1.35.dp.toPx(),
            center = point(15.7f, 8.3f),
        )
    }
}

/** 与首页预览一致的“修为符册”：方形符框内嵌一枚四向悟印。 */
@Composable
private fun CultivationTalismanIcon(contentDescription: String, tint: Color) {
    Canvas(
        modifier = Modifier
            .size(23.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val side = size.minDimension
        val scale = side / 24f
        fun point(x: Float, y: Float) = Offset(x * scale, y * scale)
        val stroke = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        drawRoundRect(
            color = tint,
            topLeft = point(3.4f, 3.4f),
            size = Size(17.2f * scale, 17.2f * scale),
            cornerRadius = CornerRadius(2.1f * scale, 2.1f * scale),
            style = stroke,
        )
        drawLine(
            color = tint,
            start = point(12f, 7.0f),
            end = point(12f, 17f),
            strokeWidth = 1.55.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = point(7f, 12f),
            end = point(17f, 12f),
            strokeWidth = 1.55.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = point(8.6f, 8.6f),
            end = point(15.4f, 15.4f),
            strokeWidth = 1.35.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = point(15.4f, 8.6f),
            end = point(8.6f, 15.4f),
            strokeWidth = 1.35.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = UtilityInk,
            radius = 1.45.dp.toPx(),
            center = point(12f, 12f),
        )
    }
}

private val UtilityInk = Color(0xFF2C4D39)
private val UtilityJadeHighlight = Color(0xFF76965D)
private val UtilityJadeWash = Color(0xFFB8CCA0)
private val SealRed = Color(0xFFB74842)
