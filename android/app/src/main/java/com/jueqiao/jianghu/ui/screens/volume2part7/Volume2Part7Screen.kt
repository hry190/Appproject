package com.jueqiao.jianghu.ui.screens.volume2part7

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
 * 第二卷-7 页 — 第二卷-6 → 点击"明令还是自学"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**(与 Vol-2-5/6 同款;交替模式:Vol-2-1/3/4/5/6/7 都用 255,仅 Vol-2-2 用 256)
 *   - 标题文本"明令还是自学"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— **新切换**(从"输入-处理-输出"系列到"明令还是自学"系列)
 *   - 图1(image 291.png,X=28, Y=155, W=352, H=203)— 上部
 *   - 图2(image 292.png,X=24, Y=381, W=356, H=214)— 中部
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 291.png(已复制为 res/drawable-nodpi/img_volume2part7_image_291.png)
 *   - 图2:D:\图\image 292.png(已复制为 res/drawable-nodpi/img_volume2part7_image_292.png)
 */
@Composable
fun Volume2Part7Screen(
    onBack: () -> Unit = {},
    onOpenVolume2Part8: () -> Unit = {},
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
            // 标题"明令还是自学"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 第二卷第三种标题系列
            Text(
                text = "明令还是自学",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume2Part8),
            )

            // 图1(image 291.png,X=28, Y=155, W=352, H=203)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 28.dp, y = 155.dp)
                    .size(width = 352.dp, height = 203.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part7_image_291),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 289.png,X=24, Y=381, W=356, H=214)— 在书框之上、中部。
            // Y=381+214=595,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 381.dp)
                    .size(width = 356.dp, height = 214.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part7_image_292),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}