package com.jueqiao.jianghu.ui.screens.volume9part2

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
 * 第九卷-2 页 — 第九卷-1 → 点击" 长句先切成符"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-9-1(255)→ Vol-9-2(256) 恢复交替)
 *   - 标题文本" 长句先切成符"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字 + 1 前导空格** W=192 沿用 6-8 字规约(与 Vol-9-1 同款 6 字标题;**用户字面前导空格按字面保留,真实意图可能是"新"地引导——"长句先切成符"**)
 *   - 图1(image 493.png,X=18, Y=135, **W=355, H=288**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.235)=288)
 *   - 图2(image 494.png,X=18, Y=478, **W=355, H=291**)— 中下部(应用新规则:横图 W=355 H=round(355/1.222)=291)
 *
 * 坐标说明:
 *   - image 493 实测 1026×831(横图,比率 1.235);自然 W=355 H=288,渲染比 1.233 与原图差 0.16%,几乎完美
 *   - image 494 实测 1074×879(横图,比率 1.222);自然 W=355 H=291,渲染比 1.222 与原图差 0.16%,几乎完美
 *   - 图2 Y=478+291=769,在书框 Y=88-872 范围内(余量 103dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 493.png(已复制为 res/drawable-nodpi/img_volume9part2_image_493.png)
 *   - 图2:设计稿 image 494.png(已复制为 res/drawable-nodpi/img_volume9part2_image_494.png)
 *
 * 点击跳 Vol-9-3(Vol-9-3 创建时回填 callback 与 .clickable)。
 */
@Composable
fun Volume9Part2Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part3: () -> Unit = {},
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
            // 标题" 长句先切成符"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 + 1 前导空格 W=192(沿用 6-8 字规约,与 Vol-9-1 同款;**用户字面前导空格按字面保留**)。
            // 标题" 长句先切成符"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 + 1 前导空格 W=192(沿用 6-8 字规约),点击跳 Vol-9-3。
            Text(
                text = " 长句先切成符",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume9Part3),
            )

            // 图1(image 493.png,X=18, Y=135, W=355, H=288)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 288.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part2_image_493),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 494.png,X=18, Y=478, W=355, H=291)— 在书框之上、中下部。
            // Y=478+291=769,在书框 Y=88-872 范围内(余量 103dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 291.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part2_image_494),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}