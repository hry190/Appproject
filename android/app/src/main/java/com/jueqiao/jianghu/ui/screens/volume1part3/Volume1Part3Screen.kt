package com.jueqiao.jianghu.ui.screens.volume1part3

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
 * 第一卷-3 页 — 第一卷-2 → 点击"规则与学习的区别"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**(与第一卷-2 的 Group 256 不同)
 *   - 图1(image 231.png,X=19, Y=155, W=361, H=279)— 上半区域
 *   - 图2(image 232.png,X=19, Y=489, W=363, H=353)— 下半区域
 *   - 标题文本"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 顶层
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 231.png(已复制为 res/drawable-nodpi/img_volume1part3_image_231.png)
 *   - 图2:D:\图\image 232.png(已复制为 res/drawable-nodpi/img_volume1part3_image_232.png)
 */
@Composable
fun Volume1Part3Screen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 129.png,与第一卷同源)
        Image(
            painter = painterResource(R.drawable.img_volume1_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷素材**。
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
            // 图1(image 231.png,X=19, Y=155, W=361, H=279)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 19.dp, y = 155.dp)
                    .size(width = 361.dp, height = 279.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part3_image_231),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 232.png,X=19, Y=489, W=363, H=353)— 在书框之上、下半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 19.dp, y = 489.dp)
                    .size(width = 363.dp, height = 353.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part3_image_232),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 标题"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与第一卷同位置同样式
            Text(
                text = "规则与学习的区别",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )
        }
    }
}