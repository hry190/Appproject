package com.jueqiao.jianghu.ui.screens.volume1part8

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R

/**
 * 第一卷-8 页 — 第一卷-7 → 点击"艺精是全能吗"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— **复用第一卷-2 书框素材**(与第一卷 / 第一卷-3 / 第一卷-4 / 第一卷-7 的 Group 255 不同)
 *   - 标题文本"艺精是全能吗"(字号 24,bold,黑色,X=134, Y=67, W=144, H=32)— 顶层(从第一卷-7 复制)
 *   - 图1(image 246.png,X=20, Y=138, W=350, H=250)— 上半区域
 *   - 图2(98.png,X=20, Y=405, W=366, H=162)— 中部区域
 *   - 图3(roup.png,X=20, Y=580, W=363, H=191)— 下半区域(贴近书框底沿 Y=851)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 246.png(已复制为 res/drawable-nodpi/img_volume1part8_image_246.png)
 *   - 图2:D:\图\98.png(已复制为 res/drawable-nodpi/img_volume1part8_98.png)
 *   - 图3:D:\图\roup.png(已复制为 res/drawable-nodpi/img_volume1part8_roup.png)
 */
@Composable
fun Volume1Part8Screen(
    onBack: () -> Unit = {},
    onOpenVolume1Part9: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 129.png,与第一卷 / 第一卷-2 同源)
        Image(
            painter = painterResource(R.drawable.img_volume1_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 素材。
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = 88.dp)
                .size(width = 854.dp, height = 784.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_volume1part2_group_256),
                contentDescription = "书框",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 标题"艺精是全能吗"(字号 24,bold,黑色,X=134, Y=67, W=144, H=32)— 从第一卷-7 复制,点击跳第一卷-9。
            Text(
                text = "艺精是全能吗",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 134.dp, y = 67.dp)
                    .size(width = 144.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume1Part9),
            )

            // 图1(image 246.png,X=20, Y=138, W=350, H=250)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 138.dp)
                    .size(width = 350.dp, height = 250.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part8_image_246),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(98.png,X=20, Y=405, W=366, H=162)— 在书框之上、中部区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 405.dp)
                    .size(width = 366.dp, height = 162.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part8_98),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(roup.png,X=20, Y=580, W=363, H=191)— 在书框之上、下半区域。
            // Y=580+191=771,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 580.dp)
                    .size(width = 363.dp, height = 191.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part8_roup),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}