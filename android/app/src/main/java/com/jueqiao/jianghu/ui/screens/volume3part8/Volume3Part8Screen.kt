package com.jueqiao.jianghu.ui.screens.volume3part8

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
 * 第三卷-8 页 — 第三卷-7 → 点击"远近藏在数中"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-7(256)→ Vol-3-8(255) 交替)
 *   - 标题文本"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-6/3-7 同款(同标题跨页叙述)
 *   - 图1(image 335.png,X=18, Y=135, W=342, H=303)— 上部(只 1 张图)
 *
 * 坐标说明:
 *   - image 335 实测 684×606(横向矩形,比率 1.129);用户给 W=342 H=303,渲染比 1.129 与原图差 0%,几乎完美
 *   - 注:image 335 与原 image 330(698×612,Vol-3-6 同源)同族,版式一致(都是 1.13 左右的横向矩形);
 *     本屏只是把图资源换成 image 335,坐标不动
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 335.png(已复制为 res/drawable-nodpi/img_volume3part8_image_335.png)
 */
@Composable
fun Volume3Part8Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part9: () -> Unit = {},
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
            // 标题"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-6/3-7 同款,点击跳第三卷-9。
            Text(
                text = "远近藏在数中",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part9),
            )

            // 图1(image 335.png,X=18, Y=135, W=342, H=303)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 342.dp, height = 303.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part8_image_335),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}