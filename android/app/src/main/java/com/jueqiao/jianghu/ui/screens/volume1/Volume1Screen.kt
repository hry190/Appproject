package com.jueqiao.jianghu.ui.screens.volume1

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
 * 第一卷 页 — 滚轮6 → 点击"秘籍"槽(X=135)跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 横跨全屏略溢出
 *   - 图1(image 233.png,X=19, Y=143, W=361, H=279)— 上半区域
 *   - 图2(image 230.png,X=19, Y=462, W=363, H=353)— 下半区域
 *   - 标题文本"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 顶层
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(已复制为 res/drawable-nodpi/img_volume1_bg.png)
 *   - 书框:D:\图\Group 255.png(已复制为 res/drawable-nodpi/img_volume1_group_255.png)
 *   - 图1:D:\图\image 233.png(已复制为 res/drawable-nodpi/img_volume1_image_233.png)
 *   - 图2:D:\图\image 230.png(已复制为 res/drawable-nodpi/img_volume1_image_230.png)
 */
@Composable
fun Volume1Screen(
    onBack: () -> Unit = {},
    onOpenVolume1Part2: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 129.png)
        Image(
            painter = painterResource(R.drawable.img_volume1_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)。
        // 写在背景之后 → 视觉上覆盖背景;标题 Text 在它之后 → 写在书框之上。
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

        // 标题"规则与学习的区别"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 点击跳第一卷-2。
        Text(
            text = "规则与学习的区别",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(x = 110.dp, y = 67.dp)
                .size(width = 192.dp, height = 32.dp)
                .clickable(onClick = onOpenVolume1Part2),
        )

        // 图1(image 233.png,X=19, Y=143, W=361, H=279)— 在书框之上、上半区域。
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 19.dp, y = 143.dp)
                .size(width = 361.dp, height = 279.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_volume1_image_233),
                contentDescription = "图1",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }

        // 图2(image 230.png,X=19, Y=462, W=363, H=353)— 在书框之上、下半区域。
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 19.dp, y = 462.dp)
                .size(width = 363.dp, height = 353.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_volume1_image_230),
                contentDescription = "图2",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }
    }
}