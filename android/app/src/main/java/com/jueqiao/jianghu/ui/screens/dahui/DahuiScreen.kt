package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 大会页 — 基于 Figma 节点 301-1185。
 * 布局:古风竹林背景 + 左上返回 + 中间古装插画 + 熊猫 + 演武场标签 + 石头装饰 + 气泡+文字。
 */
@Composable
fun DahuiScreen(
    onBack: () -> Unit = {},
    onOpenXiulian: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D2)),
    ) {
        // 全屏背景(古风竹林小院)
        Image(
            painter = painterResource(R.drawable.img_dahui_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 左上角返回按钮
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 76.dp)
                .size(24.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // 中间古装女性插画(32, 323, 259×332)
        Image(
            painter = painterResource(R.drawable.img_dahui_figure),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 32.dp, y = 323.dp)
                .size(width = 259.dp, height = 332.dp),
            contentScale = ContentScale.Fit,
        )

        // 演武场标签(90, 498, 30×65,中间有"演武场"字)
        Image(
            painter = painterResource(R.drawable.img_dahui_yanwu_tag),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 90.dp, y = 498.dp)
                .size(width = 30.dp, height = 65.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 对话气泡(56, 563, 158×79.58,Compose 自绘)
        Box(
            modifier = Modifier
                .offset(x = 56.dp, y = 563.dp)
                .size(width = 158.dp, height = 80.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFFFF).copy(alpha = 0.92f)),
            )
            // 气泡文字
            Text(
                text = "前面就是演武场!准备好,\n就来一展你的本领吧。",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }

        // 熊猫(157.16, 597, 168.88×273.72)
        Image(
            painter = painterResource(R.drawable.img_dahui_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 157.16.dp, y = 597.dp)
                .size(width = 168.88.dp, height = 273.72.dp),
            contentScale = ContentScale.Fit,
        )

        // 石头装饰(370, 854, 41×25)
        Image(
            painter = painterResource(R.drawable.img_dahui_stone),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 370.dp, y = 854.dp)
                .size(width = 41.dp, height = 25.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
