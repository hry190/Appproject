package com.jueqiao.jianghu.ui.screens.xiulian

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.screens.home.ProgressModal
import com.jueqiao.jianghu.ui.theme.YaHei

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
    onOpenProgress: () -> Unit = {},
    onOpenTask: () -> Unit = {},
) {
    var progressOpen by remember { mutableStateOf(false) }
    var dailyOpen    by remember { mutableStateOf(false) }
    var dailyStep    by remember { androidx.compose.runtime.mutableIntStateOf(1) }

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

        // 6.png 作为气泡背景(118, 453, 175×79)
        Box(
            modifier = Modifier
                .offset(x = 118.dp, y = 453.dp)
                .size(width = 175.dp, height = 79.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_xiulian_6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡文本
            Text(
                text = "这里便是修炼之地!研读秘籍、\n静心学习、参与试炼,一步步\n提升你的学识修为。",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }

        // 左上角:返回按钮(Return.png,点击回到首页1)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 16.dp, y = 50.dp)
                .size(32.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_xiulian_return),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
            )
        }

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
                onClick = { progressOpen = true },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_task,
                label = "任务",
                onClick = onOpenTask,
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_settings,
                label = "设置",
                onClick = onOpenSettings,
            )
        }
    }

    // 学习进度弹窗(由"进度"图标触发)
    if (progressOpen) {
        ProgressModal(
            onClose       = { progressOpen = false },
            onOpenDaily   = { dailyOpen = true; progressOpen = false },
            onOpenLuggage = onOpenLuggage,
        )
    }

    // 每日问题气泡(支持 2 步切换,第3 次点击关闭)
    if (dailyOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    if (dailyStep == 1) dailyStep = 2 else dailyOpen = false
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (dailyStep == 1)
                    "...去找找秘籍，看看有没有答案"
                else
                    "生活问题推荐:\n机器人为什么会认错物体?",
                color = Color.Black,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(20.dp),
            )
        }
    }
}
