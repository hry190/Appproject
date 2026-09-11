package com.jueqiao.jianghu.ui.screens.volume2part14

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
 * 第二卷-14 页 — 第二卷-13 → 点击"以尺量标"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— **复用第一卷-2 书框素材**(与 Vol-2-8/11 同款;交替模式:Vol-2-2/8/11/14 用 256,其他 Vol-2 用 255)
 *   - 标题文本"以尺量标"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-2-13 同款
 *   - 图1(image 303.png,X=18, Y=155, W=352, H=303)— 上部
 *   - 图2(image 304.png,X=18, Y=481, W=356, H=314)— 下半区域
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 303.png(已复制为 res/drawable-nodpi/img_volume2part14_image_303.png)
 *   - 图2:D:\图\image 304.png(已复制为 res/drawable-nodpi/img_volume2part14_image_304.png)
 */
@Composable
fun Volume2Part14Screen(
    onBack: () -> Unit = {},
    onOpenVolume2Part15: () -> Unit = {},
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
            // 标题"以尺量标"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-2-13 同款
            Text(
                text = "以尺量标",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume2Part15),
            )

            // 图1(image 303.png,X=18, Y=155, W=352, H=303)— 上部
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 155.dp)
                    .size(width = 352.dp, height = 303.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part14_image_303),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 304.png,X=18, Y=481, W=356, H=314)— 下半区域
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 481.dp)
                    .size(width = 356.dp, height = 314.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part14_image_304),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
