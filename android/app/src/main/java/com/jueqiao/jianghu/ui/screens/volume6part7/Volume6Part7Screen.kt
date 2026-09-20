package com.jueqiao.jianghu.ui.screens.volume6part7

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
 * 第六卷-7 页 — 第六卷-6 → 点击"问问近邻"标题跳转目标。**Vol-6-7 标题点击跳 Vol-6-8**。标题文本用户 2026-09-12 修订为"有名归类，无名成群"。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-6-6(255)→ Vol-6-7(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"有名归类，无名成群"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **9 字**(含 1 个中文`,`)W=302 沿用 Vol-3-5 同款真机测过宽度
 *   - 图1(image 420.png,X=18, Y=135, W=355, **H=330**)— 上部(H=311→330 用户 2026-09-12 真机调整,采用自然高度 355/1.077 消除畸变)
 *   - 图2(image 42.png,X=18, Y=478, W=355, **H=341**)— 中下部(H=321→341 用户 2026-09-12 真机调整,采用自然高度 355/1.041 消除畸变;沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 420 实测 1047×972(近正方形,比率 1.077);**实际 H=330**(用户 2026-09-12 真机从 H=311 调到 330),渲染比 1.076 与原图差 0.1%,几乎完美
 *   - image 42 实测 1068×1026(近正方形,比率 1.041);**实际 H=341**(用户 2026-09-12 真机从 H=321 调到 341),渲染比 1.041 与原图差 0%,完全匹配
 *   - 图2 Y=478+341=819,在书框 Y=88-872 范围内(余量 53dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 420.png(已复制为 res/drawable-nodpi/img_volume6part7_image_420.png)
 *   - 图2:设计稿 image 42.png(已复制为 res/drawable-nodpi/img_volume6part7_image_42.png)
 *
 * 标题变更史(2026-09-12):
 *   - 初版 4 字「问近邻」(疑似拼写错误)— Vol-6-7 创建时
 *   - 修订 4 字「问问近邻」 — Vol-6-4/6-5/6-6 跨页同标题叙述
 *   - **最终 9 字「有名归类，无名成群」** — 本次修订,W=213→302(9 字规约)
 */
@Composable
fun Volume6Part7Screen(
    onBack: () -> Unit = {},
    onOpenVolume6Part8: () -> Unit = {},
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
            // 标题"有名归类，无名成群"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 9 字 W=302,点击跳第六卷-8。
            Text(
                text = "有名归类，无名成群",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume6Part8),
            )

            // 图1(image 420.png,X=18, Y=135, W=355, H=330)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 330.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part7_image_420),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 42.png,X=18, Y=478, W=355, H=341)— 在书框之上、中下部。
            // Y=478+341=819,在书框 Y=88-872 范围内(余量 53dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 341.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part7_image_42),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}