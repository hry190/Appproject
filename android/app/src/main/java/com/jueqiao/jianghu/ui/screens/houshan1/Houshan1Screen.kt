package com.jueqiao.jianghu.ui.screens.houshan1

import androidx.annotation.DrawableRes
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.rememberSystemAnimationsEnabled
import com.jueqiao.jianghu.luggage.BackMountainRecommendationDto
import com.jueqiao.jianghu.ui.theme.YaHei

private data class Peak(
    val volumeNo: Int,
    val name: String,
    val verticalName: String,
    @DrawableRes val imageRes: Int,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
    val fontSize: Int,
)

private val peaks = listOf(
    Peak(1, "识机真诀", "识\n机\n真\n诀", R.drawable.img_shilian_recovered_4, (-13).dp, 570.dp, 106.dp, 188.dp, 14),
    Peak(2, "拆招心法", "拆\n招\n心\n法", R.drawable.img_shilian_recovered_4, 168.dp, 345.dp, 74.dp, 131.dp, 12),
    Peak(3, "万象谱", "万\n象\n谱", R.drawable.img_shilian_recovered_4, 113.dp, 322.dp, 50.dp, 88.dp, 10),
    Peak(4, "寻径迷踪步", "寻\n径\n迷\n踪\n步", R.drawable.img_shilian_recovered_4, 151.dp, 248.dp, 30.dp, 53.5.dp, 5),
)

/** 后山问道主场景。四座现有山峰本身就是选择入口，不再依赖气泡跳转。 */
@Composable
fun Houshan1Screen(
    onBack: () -> Unit = {},
    targetLessonId: String? = null,
    targetVolumeNo: Int? = null,
    recommendation: BackMountainRecommendationDto? = null,
    onOpenFirstTrial: (String) -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    val recommendedVolume = recommendation?.volumeNo
        ?.takeIf { it in 1..4 }
        ?: 1
    val focusedVolume = targetVolumeNo?.takeIf { it in 1..4 } ?: recommendedVolume
    var selectedVolume by rememberSaveable { mutableIntStateOf(focusedVolume) }
    LaunchedEffect(targetLessonId, targetVolumeNo, recommendation?.lessonId, recommendation?.volumeNo) {
        selectedVolume = focusedVolume
    }
    val selectedPeak = peaks.first { it.volumeNo == selectedVolume }
    val firstTrialAvailable = recommendation?.let { it.volumeNo == 1 && it.available } ?: false
    val selectedCanOpen = selectedVolume == 1 && firstTrialAvailable
    val bubbleText = when {
        selectedCanOpen -> "${selectedPeak.name}已可问道。轻触山峰，看看机关为何会认错。"
        recommendation?.volumeNo == selectedVolume && recommendation.available ->
            "${selectedPeak.name}的问道内容正在接入，先从识机真决开始会更顺畅。"
        else -> "先完成前一招，会更容易理解${selectedPeak.name}。可先看看识机真决。"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_shilian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            val sceneScale = minOf(
                maxWidth.value / 411f,
                maxHeight.value / 820f,
                1f,
            )
            val pandaWidth = 210.dp * sceneScale
            Image(
                painter = painterResource(R.drawable.img_shilian_panda),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = (maxWidth - pandaWidth)
                            .coerceAtMost(184.dp * sceneScale)
                            .coerceAtLeast(0.dp),
                        y = 621.dp * sceneScale,
                    )
                    .size(width = pandaWidth, height = 192.dp * sceneScale),
                contentScale = ContentScale.FillBounds,
            )

            peaks.forEach { peak ->
                PeakTag(
                    peak = peak,
                    selected = peak.volumeNo == selectedVolume,
                    recommended = peak.volumeNo == recommendedVolume,
                    available = peak.volumeNo == 1 && firstTrialAvailable,
                    sceneScale = sceneScale,
                    onClick = {
                        selectedVolume = peak.volumeNo
                        if (peak.volumeNo == 1 && firstTrialAvailable) {
                            recommendation?.lessonId?.let(onOpenFirstTrial)
                        }
                    },
                )
            }

            // 现有气泡只反馈选择结果，不再是唯一的下一步入口。
            Box(
                modifier = Modifier
                    .offset(x = 136.dp * sceneScale, y = 508.dp * sceneScale)
                    .size(width = 177.dp, height = 107.dp)
                    .testTag("houshan_feedback_bubble"),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_rect156),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = bubbleText,
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                )
            }

            // 保持原图标坐标 30×60；只把外层触控区扩到 48dp。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 15.dp, y = 45.dp)
                    .size(48.dp)
                    .testTag("houshan_back")
                    .semantics {
                        contentDescription = "返回上一页"
                        role = Role.Button
                    }
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}

@Composable
private fun PeakTag(
    peak: Peak,
    selected: Boolean,
    recommended: Boolean,
    available: Boolean,
    sceneScale: Float,
    onClick: () -> Unit,
) {
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val selectionAnimation = if (animationsEnabled) tween<Float>(160) else snap()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = selectionAnimation,
        label = "peakScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.68f,
        animationSpec = selectionAnimation,
        label = "peakAlpha",
    )
    val imageWidth = peak.width * sceneScale
    val imageHeight = peak.height * sceneScale
    val imageX = peak.x * sceneScale
    val imageY = peak.y * sceneScale
    val touchWidth = if (imageWidth < 48.dp) 48.dp else imageWidth
    val touchHeight = if (imageHeight < 48.dp) 48.dp else imageHeight

    Box(
        modifier = Modifier
            .offset(
                x = imageX - (touchWidth - imageWidth) / 2,
                y = imageY - (touchHeight - imageHeight) / 2,
            )
            .size(touchWidth, touchHeight)
            .testTag("houshan_peak_${peak.volumeNo}")
            .semantics {
                contentDescription = buildString {
                    append(peak.name)
                    append(if (available) "，可进入" else "，尚未开放")
                    if (recommended) append("，当前推荐")
                    if (selected) append("，已选中")
                }
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(imageWidth, imageHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
        ) {
            Image(
                painter = painterResource(peak.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            if (recommended) {
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = (peak.fontSize.coerceAtLeast(8) - 2).sp,
                    ),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                )
            }
            Text(
                text = peak.verticalName,
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = peak.fontSize.sp),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
