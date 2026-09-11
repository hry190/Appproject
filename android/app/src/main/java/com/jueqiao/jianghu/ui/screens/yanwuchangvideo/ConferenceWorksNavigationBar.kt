package com.jueqiao.jianghu.ui.screens.yanwuchangvideo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei

private val NavigationJade = Color(0xFF0F5140)
private val NavigationInactive = Color(0xFF7D9180)
private val NavigationGold = Color(0xFFD2AE50)
private val NavigationLeaf = Color(0xFF9DB69B)

@Composable
fun ConferenceWorksNavigationBar(
    selectedWorks: Boolean,
    navigationBarInset: Dp,
    onOpenWorks: () -> Unit,
    onOpenMy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentHeight = 64.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(contentHeight + navigationBarInset)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF1F3DE), Color(0xFFF4F0DB)),
                ),
            )
            .drawBehind {
                fun leaf(x: Float, y: Float, scale: Float, mirror: Boolean = false) {
                    val direction = if (mirror) -1f else 1f
                    val path = Path().apply {
                        moveTo(x, y)
                        cubicTo(
                            x + direction * 18f * scale,
                            y - 19f * scale,
                            x + direction * 34f * scale,
                            y - 13f * scale,
                            x + direction * 43f * scale,
                            y,
                        )
                        cubicTo(
                            x + direction * 31f * scale,
                            y + 7f * scale,
                            x + direction * 14f * scale,
                            y + 7f * scale,
                            x,
                            y,
                        )
                    }
                    drawPath(path, NavigationLeaf.copy(alpha = 0.12f))
                }

                repeat(4) { index ->
                    leaf(0f, 21f + index * 17f, 0.85f + index * 0.08f)
                    leaf(size.width, 18f + index * 18f, 0.85f + index * 0.08f, mirror = true)
                }
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NavigationGold.copy(alpha = 0.82f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeight)
                .padding(horizontal = 38.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationItem(
                label = "作品",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                selected = selectedWorks,
                onClick = onOpenWorks,
                modifier = Modifier.weight(1f),
            )
            NavigationItem(
                label = "我的",
                icon = Icons.Outlined.Pets,
                selected = !selectedWorks,
                onClick = onOpenMy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxHeight().clickable(onClick = onClick)) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .width(22.dp)
                    .height(4.dp)
                    .background(NavigationGold, RoundedCornerShape(50)),
            )
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) NavigationJade else NavigationInactive,
                modifier = Modifier.size(if (selected) 29.dp else 26.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = label,
                color = if (selected) NavigationJade else NavigationInactive,
                fontFamily = YaHei,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (selected) 16.sp else 15.sp,
            )
        }
    }
}
