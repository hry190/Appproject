package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 首页挑战页 — 点击首页1的"任务"展开栏里的"挑战"文本进入。
 * 基于 Figma 设计 node-id=342-2392 实现。
 * 布局:背景竹林 + 4 个快捷图标 + 滚动条"最新挑战"卡片 + 熊猫 + 对话气泡"聪明的你,一起来完成挑战吧"。
 */
@Composable
fun ChallengeScreen(
    onBack: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onBack),  // 点击空白处返回首页1
    ) {
        // 背景(竹林)
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 顶部右侧 4 个快捷图标(从左到右:作品/进度/任务/设置)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(
                iconRes = R.drawable.img_icon_works,
                label = "作品",
                onClick = { /* TODO */ },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_progress,
                label = "进度",
                onClick = onOpenProgress,
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_task,
                label = "任务",
                onClick = { /* TODO */ },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_settings,
                label = "设置",
                onClick = onOpenSettings,
            )
        }

        // 滚动条卡片:左=20, top=165, w=372, h=304
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 165.dp)
                .size(width = 372.dp, height = 304.dp)
                .clip(RoundedCornerShape(bottomStart = 100.dp, bottomEnd = 100.dp)),
        ) {
            // 弹窗背景图(u.png)
            Image(
                painter = painterResource(R.drawable.img_challenge_text_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )

            // "最新挑战"标题(相对卡片定位)
            Text(
                text = "最新挑战",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 147.dp, top = 38.dp),
            )

            // "完成" + 描述(点状分隔)
            Text(
                text = "完成\n。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 14.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 54.dp, top = 87.dp),
            )
        }

        // 对话气泡"聪明的你,一起来完成挑战吧"(Compose 自绘:圆角矩形+小三角尾巴)
        Box(
            modifier = Modifier
                .offset(x = 121.dp, y = 477.dp)
                .size(width = 135.dp, height = 74.dp),
        ) {
            // 1. 尾巴(小三角,指向左下,放在最底层)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 0.dp)
                    .size(width = 12.dp, height = 10.dp)
                    .background(Color(0xFFF5E8D4).copy(alpha = 0.92f))
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 12.dp,
                    )),
            )
            // 2. 气泡主体(圆角矩形,坐在尾巴上面)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)  // 留出尾巴空间
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5E8D4).copy(alpha = 0.92f)),
            )
            // 3. 文本(最上层)
            Text(
                text = "聪明的你,\n一起来完成挑战吧",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 14.sp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }

        // 熊猫角色(58.png,X=217, Y=520)
        Image(
            painter = painterResource(R.drawable.img_challenge_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 217.dp, y = 520.dp)
                .size(width = 200.dp, height = 334.dp),
            contentScale = ContentScale.Fit,
        )
    }
}