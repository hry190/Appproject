package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

private val CreationTopBarInk = Color(0xFF294A2E)

/** 创作台及具体作品页共用的顶部导航，保证叶签、文字与返回入口完全一致。 */
@Composable
fun CreationWorkspaceTopBar(
    onBack: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    val backInteractionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier.fillMaxWidth().height(105.dp)) {
        Box(
            modifier = Modifier
                .offset(18.dp, 34.dp)
                .size(48.dp)
                .clickable(
                    interactionSource = backInteractionSource,
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_return),
                contentDescription = "返回",
                modifier = Modifier.size(26.dp),
            )
        }
        CreationWorkspaceTopTab(
            label = "创作台",
            active = true,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = (-45).dp, y = 23.dp),
        )
        CreationWorkspaceTopTab(
            label = "创作档案",
            active = false,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = 103.dp, y = 23.dp),
            onClick = onOpenArchive,
        )
    }
}

@Composable
private fun CreationWorkspaceTopTab(
    label: String,
    active: Boolean,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(140),
        label = "顶部叶签按压",
    )
    Box(
        modifier = modifier
            .size(width = 160.dp, height = 58.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (active) R.drawable.img_gongfang_23 else R.drawable.img_gongfang_24,
            ),
            contentDescription = null,
            modifier = Modifier.size(
                width = if (active) 160.dp else 132.dp,
                height = if (active) 58.dp else 48.dp,
            ),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = label,
            color = CreationTopBarInk,
            fontFamily = YaHei,
            fontSize = if (active) 18.sp else 16.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}
