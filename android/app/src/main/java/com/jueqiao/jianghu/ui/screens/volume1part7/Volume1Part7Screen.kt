package com.jueqiao.jianghu.ui.screens.volume1part7

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
 * 第一卷-7 页 — 第一卷-6 → 点击"感知-推理-行动闭环"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**
 *   - 标题文本"艺精是全能吗"(字号 24,bold,黑色,X=134, Y=67, W=144, H=32)— 顶层
 *   - 图1(image 243.png,X=20, Y=125, W=355, H=200)— 上半区域
 *   - 图2(group.png,X=20, Y=333, W=355, H=231)— 中部区域
 *   - 图3(image 245.png,X=20, Y=570, W=353, H=247)— 下半区域(贴近书框底沿 Y=851)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 243.png(已复制为 res/drawable-nodpi/img_volume1part7_image_243.png)
 *   - 图2:D:\图\group.png(已复制为 res/drawable-nodpi/img_volume1part7_group.png)
 *   - 图3:D:\图\image 245.png(已复制为 res/drawable-nodpi/img_volume1part7_image_245.png)
 */
@Composable
fun Volume1Part7Screen(
    onBack: () -> Unit = {},
    onOpenVolume1Part8: () -> Unit = {},
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
            // 标题"艺精是全能吗"(字号 24,bold,黑色,X=134, Y=67, W=144, H=32)— 6 字 24sp 接近 W=144 极限,点击跳第一卷-8。
            Text(
                text = "艺精是全能吗",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 134.dp, y = 67.dp)
                    .size(width = 144.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume1Part8),
            )

            // 图1(image 243.png,X=20, Y=125, W=355, H=200)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 125.dp)
                    .size(width = 355.dp, height = 222.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part7_image_243),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(group.png,X=20, Y=333, W=355, H=231)— 在书框之上、中部区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 333.dp)
                    .size(width = 355.dp, height = 231.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part7_group),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 245.png,X=24, Y=609, W=383, H=247)— 在书框之上、下半区域。
            // Y=609+247=856,接近书框底沿 Y=851,在 nav 安全区(通常 852dp 以下)内可能裁 5dp。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 570.dp)
                    .size(width = 353.dp, height = 229.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part7_image_245),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}