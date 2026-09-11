package com.jueqiao.jianghu.ui.screens.volume1part13

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
 * 第一卷-13 页 — 第一卷-12 → 点击"规则与学习的区别"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— **复用第一卷-2 书框素材**(与第一卷 / 第一卷-3 / 第一卷-4 / 第一卷-7 / 第一卷-9 / 第一卷-11 / 第一卷-12 的 Group 255 不同)
 *   - 标题文本"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— **从第一卷-2 复制**(与 Vol-12 的 W=213 不同)
 *   - 图1(image 267.png,X=20, Y=135, W=359, H=321)— 上半区域
 *   - 图2(image 268.png,X=20, Y=458, W=324, H=377)— 下半区域(贴近书框底沿)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 267.png(已复制为 res/drawable-nodpi/img_volume1part13_image_267.png)
 *   - 图2:D:\图\image 268.png(已复制为 res/drawable-nodpi/img_volume1part13_image_268.png)
 */
@Composable
fun Volume1Part13Screen(
    onBack: () -> Unit = {},
    onOpenVolume1Part14: () -> Unit = {},
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
            // 标题"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 从第一卷-2 复制(与 Vol-12 的 W=213 不同)
            Text(
                text = "规则与学习的区别",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume1Part14),
            )

            // 图1(image 267.png,X=20, Y=135, W=359, H=321)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 135.dp)
                    .size(width = 350.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part13_image_267),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 268.png,X=20, Y=458, W=324, H=377)— 在书框之上、下半区域。
            // Y=458+377=835,在书框 Y=88-872 范围内安全(底部留 37dp 余量)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 458.dp)
                    .size(width = 355.dp, height = 320.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part13_image_268),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}