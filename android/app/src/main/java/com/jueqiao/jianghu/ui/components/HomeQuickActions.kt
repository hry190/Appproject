package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/** 首页和三个引导状态共用的快捷入口布局参数。 */
object HomeQuickActionsLayout {
    val EndOffset = (-12).dp
    val TopOffset = 71.dp
}

/**
 * 作品、进度、任务、设置四个入口的唯一实现。
 *
 * 两个首页复用这一组件，确保图标大小、间距、文字与任务状态完全一致。
 */
@Composable
fun HomeQuickActions(
    taskExpanded: Boolean,
    onOpenWorks: () -> Unit,
    onOpenProgress: () -> Unit,
    onToggleTask: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickActionItem(
            iconRes = R.drawable.img_icon_works,
            label = "作品",
            onClick = onOpenWorks,
        )
        QuickActionItem(
            iconRes = R.drawable.img_icon_progress,
            label = "进度",
            onClick = onOpenProgress,
        )
        TaskQuickActionItem(
            expanded = taskExpanded,
            onClick = onToggleTask,
        )
        QuickActionItem(
            iconRes = R.drawable.img_icon_settings,
            label = "设置",
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun TaskQuickActionItem(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(22.dp)) {
            Image(
                painter = painterResource(R.drawable.img_icon_task),
                contentDescription = "任务",
                modifier = Modifier.size(22.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_dot_red),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "任务",
                color = AuthPalette.TextDark,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                ),
            )
            Image(
                painter = painterResource(
                    if (expanded) R.drawable.ic_chevron_down
                    else R.drawable.ic_chevron_right,
                ),
                contentDescription = if (expanded) "收起" else "展开",
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
