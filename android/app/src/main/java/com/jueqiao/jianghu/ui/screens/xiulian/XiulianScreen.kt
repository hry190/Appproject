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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.rotate
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
        // 全屏背景图(xiulian.png)— 延伸到屏幕底部
        Image(
            painter = painterResource(R.drawable.img_xiulian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // Group 17.png(左侧装饰,135.84, 501, 138.16×245)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group17),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 35.84.dp, y = 501.dp)
                .size(width = 138.16.dp, height = 245.dp),
            contentScale = ContentScale.Fit,
        )

        // 修炼按钮(未标题-1 50.png,X=141, Y=368, W=55, H=90)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group128),
            contentDescription = "修炼",
            modifier = Modifier
                .offset(x = 131.dp, y = 358.dp)
                .size(width = 55.dp, height = 90.dp),
            contentScale = ContentScale.Fit,
        )

        // "修\n炼" 标签(X=150, Y=378,字号 12) — 在图标之上
        Text(
            text = "修\n炼",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier.offset(x = 150.dp, y = 378.dp),
        )

        // "秘籍" 旋转标签(X=104.5, Y=785.5, rotation -23.36° 逆时针, W=48, H=25,字号 20,白色)
        Text(
            text = "秘籍",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .offset(x = 104.5.dp, y = 785.5.dp)
                .size(width = 48.dp, height = 25.dp)
                .rotate(-23.36f),
        )

        // "学习" 旋转标签(X=238, Y=691, rotation 15.3° 顺时针, W=48, H=25,字号 20,白色)
        Text(
            text = "学习",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .offset(x = 238.dp, y = 691.dp)
                .size(width = 48.dp, height = 25.dp)
                .rotate(15.3f),
        )

        // "试炼" 旋转标签(X=271, Y=814, rotation 26° 顺时针, W=43, H=18,字号 16,白色)
        Text(
            text = "试炼",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
            modifier = Modifier
                .offset(x = 271.dp, y = 814.dp)
                .size(width = 43.dp, height = 18.dp)
                .rotate(26f),
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
