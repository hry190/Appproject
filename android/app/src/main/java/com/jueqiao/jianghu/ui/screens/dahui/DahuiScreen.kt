package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 大会页 — 简单版(用大会.png 作全屏背景 + 演武场竖排文字 + 对话气泡 + 返回按钮)。
 */
@Composable
fun DahuiScreen(
    onBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(大会.png)
        Image(
            painter = painterResource(R.drawable.img_dahui_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 演武场竖排文字标签(X=98, Y=510, W=15, H=45)
        Column(
            modifier = Modifier
                .offset(x = 89.dp, y = 480.dp)
                .size(width = 15.dp, height = 45.dp)
                .clickable { /* TODO: 演武场点击交互 */ },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("演", color = Color.White, style = TextStyle(fontSize = 11.sp))
            Text("武", color = Color.White, style = TextStyle(fontSize = 11.sp))
            Text("场", color = Color.White, style = TextStyle(fontSize = 11.sp))
        }

        // 对话气泡(Rectangle 186.png,X=56, Y=563, 158×80)
        Box(
            modifier = Modifier
                .offset(x = 56.dp, y = 563.dp)
                .size(width = 158.dp, height = 80.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_dahui_speech_bubble),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡文本
            Text(
                text = "前面就是演武场!准备好,\n就来一展你的本领吧。",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            )
        }

        // 左上角返回按钮
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 76.dp)
                .size(32.dp)
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
    }
}
