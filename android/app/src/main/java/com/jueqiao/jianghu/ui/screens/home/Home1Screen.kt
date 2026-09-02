package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.EdgeToEdgeScreen
import com.jueqiao.jianghu.ui.components.HomeQuickActions
import com.jueqiao.jianghu.ui.components.HomeQuickActionsLayout
import com.jueqiao.jianghu.ui.theme.YaHei

internal object HomePandaLayout {
    val X = 107.dp
    val Y = 478.dp
    val Width = 200.dp
    val Height = 256.dp
}

/**
 * 首页1 — 点击首页后跳转的次页。
 * 布局：背景竹林 + 熊猫 + 4 个装饰横幅按钮(行囊/修炼/大会/作品)。
 * 位置/尺寸(代码 L161-195,dp):
 *   行囊(119,610,55,90)、修炼(83,234,55,90)、
 *   大会(156,351,55,90)、作品(300,350,55,90)。
 */
@Composable
fun Home1Screen(
    onOpenXiulian: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenChallenge: () -> Unit = {},
    onPandaClick: () -> Unit = {},
) {
    // 进度弹窗相关状态
    var progressOpen by remember { mutableStateOf(false) }
    var dailyOpen by remember { mutableStateOf(false) }
    var dailyStep by remember { androidx.compose.runtime.mutableIntStateOf(1) }
    var taskExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val statusBarTop = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    EdgeToEdgeScreen(
        background = {
            HomeWindBackground(
                modifier = Modifier.fillMaxSize(),
            )
        },
    ) {
        // 与三个引导状态共用同一个快捷入口组件；抵消父容器已经添加的状态栏顶部内边距。
        HomeQuickActions(
            taskExpanded = taskExpanded,
            onOpenWorks = { /* TODO */ },
            onOpenProgress = { progressOpen = true },
            onToggleTask = { taskExpanded = !taskExpanded },
            onOpenSettings = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = HomeQuickActionsLayout.EndOffset,
                    y = HomeQuickActionsLayout.TopOffset - statusBarTop,
                ),
        )

        // 任务展开栏(Rectangle 187.png 背景 + "挑战"选项)
        if (taskExpanded) {
            // 1) Rectangle 187 背景(浅透明,仅看轮廓)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-65).dp, y = 100.dp)
                    .size(width = 60.dp, height = 36.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_task_dropdown_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.2f),
                    contentScale = ContentScale.FillBounds,
                )
            }
            // 2) "挑战"文本 + 右箭头(独立定位,和"任务"同一X轴)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-40).dp, y = 110.dp)
                    .clickable(onClick = onOpenChallenge),  // ← 点击跳转挑战页
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "挑战",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 9.sp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Image(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        // 用户提供的无行囊叉腰熊猫；行囊继续使用下方独立组件。
        Box(
            modifier = Modifier
                .offset(x = HomePandaLayout.X, y = HomePandaLayout.Y)
                .size(width = HomePandaLayout.Width, height = HomePandaLayout.Height),
        ) {
            Image(
                painter = painterResource(R.drawable.img_home1_panda_waist),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Box(
                modifier = Modifier
                    .offset(x = 25.dp)
                    .size(width = 150.dp, height = HomePandaLayout.Height)
                    .clickable(onClick = onPandaClick),
            )
        }

        // 行囊与另外三个入口复用相同组件、宽高和文字样式。
        DecorButton(
            imageRes = R.drawable.img_home1_btn1,
            text = "行囊",
            x = 119.dp, y = 610.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenLuggage,
        )

        // 其余装饰横幅按钮:贴图 + 竖排文字(点击区为 Box)

        // 修炼 (2.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn2,
            text = "修炼",
            x = 83.dp, y = 234.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenXiulian,
        )

        // 大会 (3.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn3,
            text = "大会",
            x = 156.dp, y = 351.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenDahui,
        )

        // 作品创作 (4.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn4,
            text = "作品创作",
            x = 300.dp, y = 350.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenZaowu,
        )
    }

    // 学习进度弹窗(由"进度"图标触发)
    if (progressOpen) {
        ProgressModal(
            onClose      = { progressOpen = false },
            onOpenDaily  = { dailyOpen = true; progressOpen = false },
            onOpenLuggage = onOpenLuggage, // 跳转新 Luggage 页
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

/**
 * 装饰横幅按钮:贴图背景 + 竖排中文叠在上层。
 * 整个 Box 是点击区,文字会居中显示。
 */
@Composable
private fun DecorButton(
    imageRes: Int,
    text: String,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 底层贴图
        Image(
            painter = painterResource(imageRes),
            contentDescription = text,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        // 上层竖排文字(向左偏移 8dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset(x = (-4).dp)
                .padding(vertical = 6.dp),
        ) {
            text.forEach { ch ->
                Text(
                    text = ch.toString(),
                    color = Color(0xFFF4E6CF), // 米黄
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        shadow = Shadow(
                            color = Color(0xCC141E14),
                            blurRadius = 8f,
                        ),
                    ),
                )
            }
        }
    }
}
