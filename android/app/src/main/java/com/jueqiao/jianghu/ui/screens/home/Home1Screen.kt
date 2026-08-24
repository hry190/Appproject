package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
 * 首页1 — 点击首页后跳转的次页。
 * 布局：背景竹林 + 熊猫 + 4 个装饰横幅按钮(行囊/修炼/大会/作品)。
 * 位置/尺寸与 mobile RN 一致:行囊(99,633,33,69)、修炼(83,274,33,69)、
 * 大会(176,381,30,65)、作品(338,421,33,69)。
 */
@Composable
fun Home1Screen(
    onOpenXiulian: () -> Unit = {},
    onOpenXingnang: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 顶部右侧 4 个快捷图标(设置/任务/进度/作品)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(
                iconRes = com.jueqiao.jianghu.R.drawable.img_icon_settings,
                label = "设置",
                onClick = { /* TODO */ },
            )
            QuickActionItem(
                iconRes = com.jueqiao.jianghu.R.drawable.img_icon_task,
                label = "任务",
                onClick = { /* TODO */ },
            )
            QuickActionItem(
                iconRes = com.jueqiao.jianghu.R.drawable.img_icon_progress,
                label = "进度",
                onClick = onOpenProgress,
            )
            QuickActionItem(
                iconRes = com.jueqiao.jianghu.R.drawable.img_icon_works,
                label = "作品",
                onClick = { /* TODO */ },
            )
        }

        // 熊猫(mobile 高清资源,同位置/尺寸)
        Image(
            painter = painterResource(R.drawable.img_home1_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 107.dp, y = 473.dp)
                .size(width = 200.dp, height = 276.dp),
            contentScale = ContentScale.Fit,
        )

        // 装饰横幅按钮:贴图 + 竖排文字(点击区为 Box)
        // 行囊 (1.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn1,
            text = "行囊",
            x = 119.dp, y = 610.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenXingnang,
        )

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

        // 作品 (4.png)
        DecorButton(
            imageRes = R.drawable.img_home1_btn4,
            text = "作品",
            x = 300.dp, y = 350.dp,
            width = 55.dp, height = 90.dp,
            onClick = onOpenZaowu,
        )
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
        // 上层竖排文字
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp),
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
