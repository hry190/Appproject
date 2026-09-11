package com.jueqiao.jianghu.ui.screens.volume4part3

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
 * 第四卷-3 页 — 第四卷-2 → 点击"穷尽还是剪枝"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-4-2(256)→ Vol-4-3(255) 交替)
 *   - 标题文本"穷尽还是剪枝"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列(6 字,W=192 沿用 Vol-3-4/3-5/3-6/3-7/3-8 同款)
 *   - 图1(image 355.png,X=18, Y=135, W=350, H=325)— 上部
 *   - 图2(image 356.png,X=18, Y=399, W=355, H=320)— 中部
 *
 * 坐标说明:
 *   - image 355 实测 698×612(横向矩形,比率 1.141);用户给 W=350 H=325 渲染比 1.077 与原图差 5.6%,可接受
 *   - image 356 实测 1068×960(近正方形,比率 1.113);用户给 W=355 H=320 渲染比 1.109 与原图差 0.3%,几乎完美
 *   - 图2 Y=399+320=719,在书框 Y=88-872 范围内安全(余量 153dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 355.png(已复制为 res/drawable-nodpi/img_volume4part3_image_355.png)
 *   - 图2:D:\图\image 356.png(已复制为 res/drawable-nodpi/img_volume4part3_image_356.png)
 */
@Composable
fun Volume4Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume4Part4: () -> Unit = {},
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
            // 标题"穷尽还是剪枝"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列,点击跳第四卷-4。
            Text(
                text = "穷尽还是剪枝",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume4Part4),
            )

            // 图1(image 355.png,X=18, Y=135, W=350, H=325)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 350.dp, height = 325.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part3_image_355),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 356.png,X=18, Y=399, W=355, H=320)— 在书框之上、中部。
            // Y=399+320=719,在书框 Y=88-872 范围内安全(余量 153dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 399.dp)
                    .size(width = 355.dp, height = 320.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part3_image_356),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}