package com.jueqiao.jianghu.ui.screens.volume9part3

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
 * 第九卷-3 页 — 第九卷-2 → 点击" 长句先切成符"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-2(256)→ Vol-9-3(255) 恢复交替)
 *   - 标题文本" 长句先切成符"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字 + 1 前导空格** W=192 沿用 6-8 字规约(与 Vol-9-1/9-2 同款 6 字标题;**用户字面前导空格按字面保留**)
 *   - 图1(image 495.png,X=18, Y=135, **W=355, H=333**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.067)=333)
 *   - 图2(image 496.png,X=18, Y=478, **W=355, H=341**)— 中下部(应用新规则:横图 W=355 H=round(355/1.040)=341)
 *
 * 坐标说明:
 *   - image 495 实测 1047×981(横图,比率 1.067);自然 W=355 H=333,渲染比 1.067 与原图差 0%,完全匹配
 *   - image 496 实测 1065×1024(近正方形,比率 1.040);自然 W=355 H=341,渲染比 1.040 与原图差 0.10%,几乎完美
 *   - 图2 Y=478+341=819,在书框 Y=88-872 范围内(余量 53dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 495.png(已复制为 res/drawable-nodpi/img_volume9part3_image_495.png)
 *   - 图2:设计稿 image 496.png(已复制为 res/drawable-nodpi/img_volume9part3_image_496.png)
 *
 * 点击跳 Vol-9-4(Vol-9-4 创建时回填 callback 与 .clickable)。
 */
@Composable
fun Volume9Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part4: () -> Unit = {},
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
            // 标题" 长句先切成符"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 + 1 前导空格 W=192(沿用 6-8 字规约,与 Vol-9-1/9-2 同款;**用户字面前导空格按字面保留**)。
            // 标题"语义也有远近"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 W=192(沿用 6-8 字规约),点击跳 Vol-9-4。
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
                    .clickable(onClick = onOpenVolume9Part4),
            )

            // 图1(image 495.png,X=18, Y=135, W=355, H=333)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 333.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part3_image_495),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 496.png,X=18, Y=478, W=355, H=341)— 在书框之上、中下部。
            // Y=478+341=819,在书框 Y=88-872 范围内(余量 53dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 341.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part3_image_496),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}