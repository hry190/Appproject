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
    onOpenGunlun1: () -> Unit = {},
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
            // Group 17.png(左侧装饰,35.84, 501, 138.16×245)
        Image(
            painter = painterResource(R.drawable.img_xiulian_group17),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 35.84.dp, y = 501.dp)
                .size(width = 138.16.dp, height = 245.dp),
            contentScale = ContentScale.Fit,
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

        // "试炼" 旋转标签(X=271, Y=814, rotation 26° 顺时针, W=43, H=22,字号 16,白色)
        Text(
            text = "试炼",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
            modifier = Modifier
                .offset(x = 271.dp, y = 814.dp)
                .size(width = 43.dp, height = 22.dp)
                .rotate(26f),
        )

        // Rectangle 18.png(X=120, Y=389, W=168, H=140)
        Image(
            painter = painterResource(R.drawable.img_xiulian_rectangle_18),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 120.dp, y = 389.dp)
                .size(width = 168.dp, height = 140.dp),
            contentScale = ContentScale.FillBounds,
        )

        // Rectangle 18.png 气泡内文字(X=138, Y=401, W=138, H=58,字号 14,黑色)
        Text(
            text = "后院竹静风清水淡，乃是绝佳修炼之地，随我前往吧",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 138.dp, y = 401.dp)
                .size(width = 138.dp, height = 58.dp),
        )

        // Vector 579.png 按钮(X=183.69, Y=482.22, W=89.6, H=20.56)— 点击跳滚轮1
        Image(
            painter = painterResource(R.drawable.img_xiulian_vector_579),
            contentDescription = "前往后院",
            modifier = Modifier
                .offset(x = 183.69.dp, y = 482.22.dp)
                .size(width = 89.6.dp, height = 25.6.dp)
                .clickable(onClick = onOpenGunlun1),
            contentScale = ContentScale.FillBounds,
        )
        // Vector 579.png 按钮文字(X=200.49, Y=484, W=56, H=17,字号 14,黑色,居中于按钮)
        Text(
            text = "前往后院",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 200.49.dp, y = 484.dp)
                .size(width = 56.dp, height = 25.dp),
        )

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
