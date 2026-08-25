package com.jueqiao.jianghu.ui.screens.xiulian

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem

/**
 * 修炼页 — 基于 Figma 节点 301-1242。
 * 布局:xiulian.png 全屏背景 + Group 17.png 左侧装饰(35.84, 501, 138.16×245) +
 *      顶部 4 个快捷图标。
 */
@Composable
fun XiulianScreen(
    onBack: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(xiulian.png)
        Image(
            painter = painterResource(R.drawable.img_xiulian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Group 17.png(左侧装饰,135.84, 501, 138.16×245)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group17),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 35.84.dp, y = 501.dp)
                .size(width = 138.16.dp, height = 245.dp),
            contentScale = ContentScale.Fit,
        )

        // 顶部右侧 4 个快捷图标
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(
                iconRes = R.drawable.img_icon_works,
                label = "作品",
                onClick = onOpenZaowu,
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_progress,
                label = "进度",
                onClick = { /* TODO: 进度 */ },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_task,
                label = "任务",
                onClick = { /* TODO: 任务 */ },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_settings,
                label = "设置",
                onClick = onOpenSettings,
            )
        }
    }
}
