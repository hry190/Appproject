package com.jueqiao.jianghu.ui.screens.volume1part9

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
 * 第一卷-9 页 — 第一卷-8 → 点击"艺精是全能吗"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**(与第一卷-2 / 第一卷-8 的 Group 256 不同)
 *   - 标题文本"依赖数据与经验"(字号 24,bold,黑色,X=104, Y=67, W=170, H=33)— 6 字 24sp 接近 W=170 极限
 *   - 图1(image 250.png,X=24, Y=155, W=419, H=293)— 上半区域
 *   - 图2(p.png,X=24, Y=518, W=356, H=327)— 下半区域(贴近书框底沿)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 250.png(已复制为 res/drawable-nodpi/img_volume1part9_image_250.png)
 *   - 图2:D:\图\p.png(已复制为 res/drawable-nodpi/img_volume1part9_p.png)
 */
@Composable
fun Volume1Part9Screen(
    onBack: () -> Unit = {},
    onOpenVolume1Part10: () -> Unit = {},
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
            // 标题"依赖数据与经验"(字号 24,bold,黑色,X=104, Y=67, W=170, H=33)— 6 字 24sp 接近 W=170 极限,点击跳第一卷-10。
            Text(
                text = "依赖数据与经验",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 104.dp, y = 67.dp)
                    .size(width = 170.dp, height = 33.dp)
                    .clickable(onClick = onOpenVolume1Part10),
            )

            // 图1(image 250.png,X=24, Y=155, W=419, H=293)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 135.dp)
                    .size(width = 359.dp, height = 292.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part9_image_250),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(p.png,X=24, Y=518, W=356, H=327)— 在书框之上、下半区域。
            // Y=518+327=845,在书框 Y=88-872 范围内安全(底部留 27dp 余量)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 478.dp)
                    .size(width = 356.dp, height = 327.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume1part9_p),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}