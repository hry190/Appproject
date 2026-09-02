package com.jueqiao.jianghu.ui.screens.gunlun1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮1 页 — 修炼页 → "前往后院"按钮跳转目标。
 * 布局:滚轮.png 全屏背景 + 未标题-1-恢复的 5.png 居中内容面板 + 返回按钮。
 * 复制自 screen-adaptation.md 模式 A (YanwuchangScreen 简化版)。
 */
@Composable
fun Gunlun1Screen(
    onBack: () -> Unit = {},
    onOpenGunlun2: () -> Unit = {},
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

            // 未标题-1 50.png — "后山"按钮的背景图像
//   (X=247, Y=165, W=55, H≈117.12 按 PNG 比例 85x181 算出)
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_1_50),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 247.dp, y = 165.dp)
                    .size(width = 55.dp, height = 117.12.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "后\n山" 竖排文字(在未标题-1 50.png 上,X=247, Y=165, 字号 16, 白色, YaHei)
            Column(
                modifier = Modifier
                    .offset(x = 242.dp, y = 165.dp)
                    .size(width = 55.dp, height = 117.12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "后",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
                Text(
                    text = "山",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
            }

            // Rectangle 86.png 气泡(D:\图\Rectangle 86.png)— X=23, Y=324, W=148, H=84
            //   点击气泡跳转到滚轮2
            Box(
                modifier = Modifier
                    .offset(x = 23.dp, y = 324.dp)
                    .size(width = 148.dp, height = 84.dp)
                    .clickable(onClick = onOpenGunlun2),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gunlun1_rect86),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // 气泡上的文本(14sp, 黑色)
                Text(
                    text = "你还未习得秘籍，不妨前往后山试炼，寻觅机缘",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxSize(),
                )
            }

            // 未标题-150.png — "修炼"按钮的背景图像
            //   (X=35, Y=548, W=55, H=117)
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_150),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 35.dp, y = 548.dp)
                    .size(width = 55.dp, height = 117.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "修\n炼" 竖排文字(在未标题-150.png 上,X=35, Y=548, 字号 16, 白色, YaHei)
            Column(
                modifier = Modifier
                    .offset(x = 31.dp, y = 548.dp)
                    .size(width = 55.dp, height = 117.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "修",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
                Text(
                    text = "炼",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
            }
        }
    }
}
