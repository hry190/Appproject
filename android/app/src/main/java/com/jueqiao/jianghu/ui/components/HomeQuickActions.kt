package com.jueqiao.jianghu.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 首页快捷入口的布局参数。 */
object HomeQuickActionsLayout {
    val EndOffset = (-12).dp
    val TopOffset = 71.dp
}

/**
 * 首页专属的快捷入口面板，将问道、修为、书信、设置收纳在一个可折叠容器中。
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
    val context = LocalContext.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences(
            QuickActionsPreferencesName,
            Context.MODE_PRIVATE,
        )
    }
    var expanded by rememberSaveable {
        mutableStateOf(preferences.getBoolean(QuickActionsExpandedPreference, true))
    }
    LaunchedEffect(expanded) {
        preferences.edit()
            .putBoolean(QuickActionsExpandedPreference, expanded)
            .apply()
    }
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val transitionDuration = if (animationsEnabled) 220 else 0
    val panelShape = RoundedCornerShape(24.dp)

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .clip(panelShape)
            .background(QuickActionsPanelBackground)
            .border(1.dp, QuickActionsPanelBorder, panelShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(
                expandFrom = Alignment.End,
                animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(transitionDuration)),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End,
                animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(transitionDuration)),
        ) {
            androidx.compose.foundation.layout.Row {
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

        QuickActionsToggle(
            expanded = expanded,
            showSeal = !expanded && hasUnreadLetters,
            animationsEnabled = animationsEnabled,
            onClick = { expanded = !expanded },
        )
    }
}

@Composable
private fun QuickActionsToggle(
    expanded: Boolean,
    showSeal: Boolean,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 220, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "quickActionsToggleRotation",
    )

    Column(
        modifier = Modifier
            .width(40.dp)
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = if (expanded) "收起首页快捷入口" else "展开首页快捷入口"
                role = Role.Button
            }
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(23.dp)) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(18.dp)
                    .graphicsLayer { rotationZ = rotation },
            ) {
                val strokeWidth = 1.8.dp.toPx()
                drawLine(
                    color = UtilityInk,
                    start = Offset(size.width * 0.36f, size.height * 0.22f),
                    end = Offset(size.width * 0.68f, size.height * 0.5f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = UtilityInk,
                    start = Offset(size.width * 0.68f, size.height * 0.5f),
                    end = Offset(size.width * 0.36f, size.height * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
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
            text = if (expanded) "收起" else "展开",
            color = UtilityInk,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
            maxLines = 1,
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
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isClickSelected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isActive = animationsEnabled && (isPressed || isClickSelected)
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isClickSelected -> 1.06f
            else -> 1f
        },
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 150, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "utilityActionScale",
    )
    val lift by animateFloatAsState(
        targetValue = if (isClickSelected) -1.5f else 0f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "utilityActionLift",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.11f
            isClickSelected -> 0.15f
            else -> 0f
        },
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 150, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "utilityActionHalo",
    )
    val haloScale by animateFloatAsState(
        targetValue = if (isClickSelected) 1.10f else 0.82f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "utilityActionHaloScale",
    )
    val foreground by animateColorAsState(
        targetValue = if (isActive) UtilityJadeHighlight else UtilityInk,
        animationSpec = if (animationsEnabled) tween(durationMillis = 150) else snap(),
        label = "utilityActionTint",
    )

    Column(
        modifier = Modifier
            .width(48.dp)
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = lift
            }
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
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
        verticalArrangement = Arrangement.Center,
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
                UtilityIcon.Wendao -> WendaoRingIcon(foreground)
                UtilityIcon.Cultivation -> CultivationTalismanIcon(foreground)
                UtilityIcon.Letter -> Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(23.dp),
                )
                UtilityIcon.Settings -> Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
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
                fontSize = 15.sp,
            ),
            maxLines = 1,
        )
    }
}

private enum class UtilityIcon { Wendao, Cultivation, Letter, Settings }

/** 与首页预览一致的“问道环印”：外环、内环、路径和一点悟光组成。 */
@Composable
private fun WendaoRingIcon(tint: Color) {
    Canvas(
        modifier = Modifier.size(23.dp),
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
private fun CultivationTalismanIcon(tint: Color) {
    Canvas(
        modifier = Modifier.size(23.dp),
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
private val QuickActionsPanelBackground = Color(0xE6F3EEDC)
private val QuickActionsPanelBorder = Color(0x66748D68)
private val SealRed = Color(0xFFB74842)
private const val QuickActionsPreferencesName = "home_quick_actions"
private const val QuickActionsExpandedPreference = "expanded"
