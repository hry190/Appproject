package com.jueqiao.jianghu.ui.screens.xiulian

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * 修炼页 — 基于 Figma 节点 301-1242。
 * 布局:竹林背景 + 左上返回 + 中央"修 炼"竖排标签 + 左侧熊猫 +
 *      中央对话气泡 + 底部 4 个菜单(学习/试炼/秘籍/试炼)。
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
        // 竹林背景
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
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

        // 左上角:返回按钮(白色圆形)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 16.dp, y = 50.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5E8D4).copy(alpha = 0.85f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            // ↩ 返回箭头(可以用 ic_back_arrow 资源)
            Text(
                text = "←",
                color = Color.Black,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            )
        }

        // 中央:竖排"修 炼"标签(右上)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (-50).dp, y = 110.dp)
                .size(width = 55.dp, height = 100.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_decor_xiulian),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 竖排文字"修 炼"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "修",
                    color = Color(0xFFF4E6CF),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xCC141E14),
                            blurRadius = 6f,
                        ),
                    ),
                )
                Text(
                    text = "炼",
                    color = Color(0xFFF4E6CF),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xCC141E14),
                            blurRadius = 6f,
                        ),
                    ),
                )
            }
        }

        // 中间:对话气泡(空白,等待填充)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 0.dp, y = 250.dp)
                .size(width = 220.dp, height = 80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFAF0DA).copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center,
        ) {
            // TODO: 填入对话内容
        }

        // 左侧:熊猫角色
        Image(
            painter = painterResource(R.drawable.img_home1_panda),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 80.dp)
                .size(width = 200.dp, height = 276.dp),
            contentScale = ContentScale.Fit,
        )

        // 底部:4 个菜单项
        // 布局:左下"秘籍"、右中"学习"、右下"试炼"、右下角小圆按钮
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 左:秘籍
            Text(
                text = "秘籍",
                color = Color(0xFFF4E6CF),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xCC141E14),
                        blurRadius = 8f,
                    ),
                ),
                modifier = Modifier
                    .clickable(onClick = onOpenLuggage)
                    .padding(8.dp),
            )
            // 中:学习
            Text(
                text = "学习",
                color = Color(0xFFF4E6CF),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xCC141E14),
                        blurRadius = 8f,
                    ),
                ),
                modifier = Modifier
                    .clickable { /* TODO: 学习 */ }
                    .padding(8.dp),
            )
            // 右:试炼
            Text(
                text = "试炼",
                color = Color(0xFFF4E6CF),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xCC141E14),
                        blurRadius = 8f,
                    ),
                ),
                modifier = Modifier
                    .clickable(onClick = onOpenDahui)
                    .padding(8.dp),
            )
        }
    }
}
