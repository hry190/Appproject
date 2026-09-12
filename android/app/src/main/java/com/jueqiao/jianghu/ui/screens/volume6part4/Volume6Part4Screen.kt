package com.jueqiao.jianghu.ui.screens.volume6part4

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
 * 第六卷-4 页 — 第六卷-3 → 点击"相似要有尺"标题跳转目标。Vol-6-4 标题点击跳 Vol-6-5。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-6-3(255)→ Vol-6-4(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"问问近邻"(字号 24,bold,黑色,X=110, Y=67, **W=213, H=32**)— **4 字** W=213 沿用 5 字规约(更窄标题在历次卷未列规约,借用 5 字宽度)
 *   - 图1(image 415.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 416.png,X=18, Y=478, W=355, H=321)— 中下部(沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 415 实测 1047×918(横向矩形,比率 1.141);用户给 W=355 H=311 渲染比 1.141 与原图差 0%,**完全匹配**
 *   - image 416 实测 1068×966(横向矩形,比率 1.106);用户给 W=355 H=321 渲染比 1.106 与原图差 0%,**完全匹配**
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp;沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 415.png(已复制为 res/drawable-nodpi/img_volume6part4_image_415.png)
 *   - 图2:D:\图\image 416.png(已复制为 res/drawable-nodpi/img_volume6part4_image_416.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-6-5 创建时按历次约定回填 onOpenVolume6Part5)。
 */
@Composable
fun Volume6Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume6Part5: () -> Unit = {},
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
            // 标题"问问近邻"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 4 字 W=213,点击跳第六卷-5。
            Text(
                text = "问问近邻",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume6Part5),
            )

            // 图1(image 415.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part4_image_415),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 416.png,X=18, Y=478, W=355, H=321)— 在书框之上、中下部。
            // Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp;沿用 Vol-5-7+ 真机调整过的 Y=478)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part4_image_416),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}