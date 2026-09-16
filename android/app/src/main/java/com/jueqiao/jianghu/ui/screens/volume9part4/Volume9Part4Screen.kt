package com.jueqiao.jianghu.ui.screens.volume9part4

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
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
 * 第九卷-4 页 — 第九卷-3 → 点击"语义也有远近"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-3(255)→ Vol-9-4(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"语义也有远近"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字** W=192 沿用 6-8 字规约
 *   - 图1(image 497.png,X=18, Y=135, **W=355, H=354**)— 上部(应用新规则 fit-to-natural-bounds:近正方形 ratio 1.003,自动 fit H=354 W=355)
 *   - 图2(image 49.png,X=18, Y=478, **W=355, H=356**)— 中下部(应用新规则:近正方形 ratio 0.997,自动 fit H=356 W=355)
 *
 * 坐标说明:
 *   - image 497 实测 969×966(近正方形,比率 1.003);自然 W=355 H=354,渲染比 1.003 与原图差 0.03%,几乎完美
 *   - image 49 实测 1044×1047(近正方形,比率 0.997);自然 W=355 H=356,渲染比 0.997 与原图差 0.01%,几乎完美
 *   - 图1 Y=135+354=489,图 2 Y=478+356=834(均在书框 Y=88-872 范围内,余量 383/38dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 497.png(已复制为 res/drawable-nodpi/img_volume9part4_image_497.png)
 *   - 图2:D:\图\image 49.png(已复制为 res/drawable-nodpi/img_volume9part4_image_49.png)
 *
 * 点击跳 Vol-9-5(Vol-9-5 创建时回填 callback 与 .clickable)。
 */
@Composable
fun Volume9Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part5: () -> Unit = {},
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
            // 标题"语义也有远近"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 W=192(沿用 6-8 字规约)。
            // 本屏暂无后继页,故未接 clickable(等 Vol-9-5 创建时按历次约定回填 onOpenVolume9Part5)。
            Text(
                text = "语义也有远近",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume9Part5),
            )

            // 图1(image 497.png,X=18, Y=118, W=355, H=324)— 在书框之上、上部(近正方形)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 118.dp)
                    .size(width = 355.dp, height = 324.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part4_image_497),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 49.png,X=18, Y=478, W=355, H=356)— 在书框之上、中下部(近正方形)。
            // Y=478+356=834,在书框 Y=88-872 范围内(余量 38dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 448.dp)
                    .size(width = 355.dp, height = 366.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part4_image_49),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}