package com.jueqiao.jianghu.ui.screens.volume7part6

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
 * 第七卷-6 页 — 第七卷-5 → 点击"皮影戏之层层见不同"标题跳转目标。**Vol-7-6 标题点击跳 Vol-7-7**。
 *
 * 布局(z-order 由下到上,**5 层,回到 2 图布局**—Vol-7-4/7-5 是 3 图,Vol-7-6 是 2 图):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-7-5(256)→ Vol-7-6(255) 恢复交替)
 *   - 标题文本"皮影戏之层层见不同"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **9 字**W=302 沿用 9 字规约(新标题系列,无独立规约)
 *   - 图1(image 448.png,X=18, Y=135, **W=355, H=281**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.264)=281)
 *   - 图2(image 449.png,X=18, Y=478, **W=355, H=265**)— 中下部(应用新规则:横图 W=355 H=round(355/1.338)=265)
 *
 * 坐标说明:
 *   - image 448 实测 1047×828(横向矩形,比率 1.264);自然 W=355 H=281,渲染比 1.263 与原图差 0.09%,几乎完美
 *   - image 449 实测 1068×798(横向矩形,比率 1.338);自然 W=355 H=265,渲染比 1.340 与原图差 0.10%,几乎完美
 *   - 图2 Y=478+265=743,在书框 Y=88-872 范围内(余量 129dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 448.png(已复制为 res/drawable-nodpi/img_volume7part6_image_448.png)
 *   - 图2:设计稿 image 449.png(已复制为 res/drawable-nodpi/img_volume7part6_image_449.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-7-7 创建时按历次约定回填 onOpenVolume7Part7)。
 */
@Composable
fun Volume7Part6Screen(
    onBack: () -> Unit = {},
    onOpenVolume7Part7: () -> Unit = {},
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
            // 标题"皮影戏之层层见不同"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 9 字 W=302(沿用 9 字规约),点击跳第七卷-7。
            Text(
                text = "皮影戏之层层见不同",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume7Part7),
            )

            // 图1(image 448.png,X=18, Y=135, W=355, H=281)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 281.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part6_image_448),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 449.png,X=18, Y=478, W=355, H=265)— 在书框之上、中下部。
            // Y=478+265=743,在书框 Y=88-872 范围内(余量 129dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 265.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part6_image_449),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}