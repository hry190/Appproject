package com.jueqiao.jianghu.ui.screens.zaowu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
 * 作品创作页 — 简单版(用作品创作.png 作全屏背景 + 左上返回按钮)。
 */
@Composable
fun ZaowuScreen(
    onBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(作品创作.png)
        Image(
            painter = painterResource(R.drawable.img_zaowu_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 左上角返回按钮
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 76.dp)
                .size(32.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_zaowu_return),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // 角色插画(pp.png, X=230, Y=550, W=176, H=271)
        Image(
            painter = painterResource(R.drawable.img_zaowu_figure),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 230.dp, y = 500.dp)
                .size(width = 176.dp, height = 271.dp),
            contentScale = ContentScale.Fit,
        )

        // 未标题-1 51.png(X=61, Y=574, W=30, H=65)
        Image(
            painter = painterResource(R.drawable.img_zaowu_51),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 60.dp, y = 514.dp)
                .size(width = 36.dp, height = 65.dp),
            contentScale = ContentScale.Fit,
        )

        // Rectangle 186.png 作为气泡背景(X=149, Y=479, W=144, H=81)
        Box(
            modifier = Modifier
                .offset(x = 149.dp, y = 429.dp)
                .size(width = 144.dp, height = 81.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_zaowu_rect186),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡文本
            Text(
                text = "快去工坊里头看看吧，\n一起来设计属于自己的作品吧",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 11.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }

        // 标签文字"工\n坊"(X=61, Y=514, 字号 12) — 显示在图标之上
        Text(
            text = "工\n坊",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier.offset(x = 69.dp, y = 524.dp),
        )
    }
}
