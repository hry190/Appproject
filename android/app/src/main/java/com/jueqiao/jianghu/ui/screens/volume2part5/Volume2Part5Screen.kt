package com.jueqiao.jianghu.ui.screens.volume2part5

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
 * 第二卷-5 页 — 第二卷-4 → 点击"输入-处理-输出"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**(与第一卷 / 第一卷-3 / 第一卷-4 / 第一卷-7 / 第一卷-9 / 第一卷-11 / 第一卷-12 / 第一卷-14 / 第二卷-1 / 第二卷-3 / 第二卷-4 同款;唯一用 Group 256 的屏是 第二卷-2)
 *   - 标题文本"输入-处理-输出"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与第二卷-4 同款(沿用"输入-处理-输出"系列)
 *   - 图1(image 281.png,X=28, Y=155, W=352, H=203)— 上部
 *   - 图2(image 283.png,X=24, Y=381, W=356, H=214)— 中部
 *   - 图3(image 284.png,X=24, Y=609, W=356, H=217)— 下部
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 281.png(已复制为 res/drawable-nodpi/img_volume2part4_image_281.png)— **复用 Vol-2-4 资源**(不重复复制)
 *   - 图2:D:\图\image 283.png(已复制为 res/drawable-nodpi/img_volume2part4_image_283.png)— **复用 Vol-2-4 资源**
 *   - 图3:D:\图\image 284.png(已复制为 res/drawable-nodpi/img_volume2part4_image_284.png)— **复用 Vol-2-4 资源**
 */
@Composable
fun Volume2Part5Screen(
    onBack: () -> Unit = {},
    onOpenVolume2Part6: () -> Unit = {},
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

        // 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷素材。
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = 88.dp)
                .size(width = 854.dp, height = 784.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_volume1_group_255),
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
            // 标题"输入-处理-输出"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与第二卷-4 同款
            Text(
                text = "输入-处理-输出",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume2Part6),
            )

            // 图1(image 281.png,X=28, Y=155, W=352, H=203)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 28.dp, y = 155.dp)
                    .size(width = 352.dp, height = 203.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part4_image_281),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 283.png,X=24, Y=381, W=356, H=214)— 在书框之上、中部。
            // Y=381+214=595,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 381.dp)
                    .size(width = 356.dp, height = 214.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part4_image_283),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 284.png,X=24, Y=609, W=356, H=217)— 在书框之上、下部。
            // Y=609+217=826,在书框 Y=88-872 范围内安全(底部留 46dp 余量)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 609.dp)
                    .size(width = 356.dp, height = 202.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part4_image_284),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}