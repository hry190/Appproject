package com.jueqiao.jianghu.ui.screens.gunlun2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮2 页 — 滚轮1 → "气泡"点击跳转目标。
 * 布局:滚轮.png 全屏背景 + 未标题-1-恢复的 5.png 居中内容面板 + 返回按钮。
 * 复制自 Gunlun1Screen.kt,删除了:气泡及其文本、后山按钮、修炼按钮(只在滚轮1需要)。
 * 复制自 screen-adaptation.md 模式 A (YanwuchangScreen 简化版)。
 */
@Composable
fun Gunlun2Screen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(滚轮.png)— 延伸到屏幕底部
        Image(
            painter = painterResource(R.drawable.img_gunlun1_bg),
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
            // 熊猫打坐图像(未标题-1-恢复的 5.png,X=70, Y=320, W=257, H=457)— 居中内容面板
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_1_recovered_5),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 70.dp, y = 320.dp)
                    .size(width = 257.dp, height = 457.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "秘籍" 图像(未标题-2-恢复的 1.png,X=135, Y=251, W=155, H=147)
            Image(
                painter = painterResource(R.drawable.img_gunlun2_untitled_2_recovered_1),
                contentDescription = "秘籍",
                modifier = Modifier
                    .offset(x = 135.dp, y = 251.dp)
                    .size(width = 155.dp, height = 147.dp),
                contentScale = ContentScale.FillBounds,
            )

            // 未解锁秘籍1 图像(未标题1.png,X=8, Y=235, W=96, H=96)
            Image(
                painter = painterResource(R.drawable.img_gunlun2_untitled_1),
                contentDescription = "未解锁秘籍1",
                modifier = Modifier
                    .offset(x = 8.dp, y = 235.dp)
                    .size(width = 96.dp, height = 96.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "介绍" 旋转图像 + 背景填充(Rectangle 251.png,X=283, Y=264, rotation 0.93°, W=23, H=91.5)
            //   外观:Opacity 100%, Corner radius 0
            //   填充色:#DDC686,Opacity 100%
            Box(
                modifier = Modifier
                    .offset(x = 283.dp, y = 264.dp)
                    .size(width = 23.dp, height = 91.5.dp)
                    .background(Color(0xFFDDC686))
                    .rotate(0.93f),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gunlun2_rect251),
                    contentDescription = "介绍",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "识\n机\n真\n决" 竖排文字(W=16, H=76,字号 14,lineHeight 133.5%=18.69sp,黑色,YaHei)
                Text(
                    text = "识\n机\n真\n决",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 14.sp,
                        lineHeight = 18.69.sp,
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 16.dp, height = 76.dp),
                )
            }

            // Vector.png 自定义返回按钮(X=20, Y=77, W=18, H=13)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 77.dp)
                    .size(width = 18.dp, height = 13.dp)
                    .clickable(onClick = onBack),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gunlun1_vector),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
