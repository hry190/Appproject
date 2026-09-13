package com.jueqiao.jianghu.ui.screens.volume9part1

import androidx.activity.compose.BackHandler
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
 * 第九卷-1 页 — 滚轮9 → 点击"已解锁9"图像跳转目标(新卷首屏)。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";第九卷首屏)
 *   - 标题文本"长句先切成符"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字** W=192 沿用 6-8 字规约(6 字无独立规约,真机可微调;与 Vol-8 系列同款窄标题)
 *   - 图1(image 491.png,X=18, Y=135, **W=355, H=328**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.084)=328)
 *   - 图2(image 492.png,X=18, Y=478, **W=355, H=343**)— 中下部(应用新规则:横图 W=355 H=round(355/1.035)=343)
 *
 * 坐标说明:
 *   - image 491 实测 1047×966(横图,比率 1.084);自然 W=355 H=328,渲染比 1.084 与原图差 0.14%,几乎完美
 *   - image 492 实测 1059×1023(近正方形,比率 1.035);自然 W=355 H=343,渲染比 1.035 与原图差 0.02%,几乎完美
 *   - 图2 Y=478+343=821,在书框 Y=88-872 范围内(余量 51dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 491.png(已复制为 res/drawable-nodpi/img_volume9part1_image_491.png)
 *   - 图2:D:\图\image 492.png(已复制为 res/drawable-nodpi/img_volume9part1_image_492.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-9-2 创建时按历次约定回填 onOpenVolume9Part2)。
 */
@Composable
fun Volume9Part1Screen(
    onBack: () -> Unit = {},
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
            // 标题"长句先切成符"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 W=192(沿用 6-8 字规约)。
            // 本屏暂无后继页,故未接 clickable(等 Vol-9-2 创建时按历次约定回填 onOpenVolume9Part2)。
            Text(
                text = "长句先切成符",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp),
            )

            // 图1(image 491.png,X=18, Y=135, W=355, H=328)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 328.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part1_image_491),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 492.png,X=18, Y=478, W=355, H=343)— 在书框之上、中下部。
            // Y=478+343=821,在书框 Y=88-872 范围内(余量 51dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 343.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part1_image_492),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}