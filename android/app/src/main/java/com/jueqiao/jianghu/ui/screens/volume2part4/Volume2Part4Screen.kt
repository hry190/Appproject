package com.jueqiao.jianghu.ui.screens.volume2part4

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
 * 第二卷-4 页 — 第二卷-3 → 点击"输入-处理-输出"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— **复用第一卷书框素材**(目前 Vol-2-1/3/4 都用 255,Vol-2-2 是唯一用 Group 256 的)
 *   - 标题文本"输入-处理-输出"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— **新标题**(从"大题拆小招"系列切换)— 与第一卷-1 标题位置样式相同
 *   - 图1(image 279.png,X=24, Y=135, W=339, H=286)— 上半区域
 *   - 图2(image 282.png,X=14, Y=452, W=404, H=338)— 下半区域
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 279.png(已复制为 res/drawable-nodpi/img_volume2part4_image_279.png)
 *   - 图2:D:\图\image 282.png(已复制为 res/drawable-nodpi/img_volume2part4_image_282.png)
 */
@Composable
fun Volume2Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume2Part5: () -> Unit = {},
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
            // 标题"输入-处理-输出"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与第一卷-1 标题样式相同(从"大题拆小招"系列切换)
            Text(
                text = "输入-处理-输出",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume2Part5),
            )

            // 图1(image 279.png,X=20, Y=135, W=359, H=320)— 在书框之上、上半区域。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 135.dp)
                    .size(width = 339.dp, height = 286.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part4_image_279),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 282.png,X=20, Y=492, W=403, H=353)— 在书框之上、下半区域。
            // Y=492+353=845,在书框 Y=88-872 范围内安全(底部留 27dp 余量)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 14.dp, y = 452.dp)
                    .size(width = 354.dp, height = 338.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume2part4_image_282),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}