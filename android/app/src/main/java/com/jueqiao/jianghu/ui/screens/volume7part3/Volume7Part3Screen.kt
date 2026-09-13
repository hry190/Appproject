package com.jueqiao.jianghu.ui.screens.volume7part3

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
 * 第七卷-3 页 — 第七卷-2 → 点击"皮影戏之小节点会加权"标题跳转目标。**Vol-7-3 标题点击跳 Vol-7-4**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-7-2(256)→ Vol-7-3(255) 恢复交替)
 *   - 标题文本"皮影戏之小节点会加权"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **10 字**(含 1 个中文`,`)W=302 沿用 9 字规约(与 Vol-7-1/7-2 同款,跨页同标题)
 *   - 图1(image 440.png,X=18, Y=135, **W=355, H=332**)— 上部(**应用新规则 fit-to-natural-bounds**:横图 W=355 H=round(355/ratio)=332,完全忽略用户字面 H=311)
 *   - 图2(image 441.png,X=18, Y=478, **W=355, H=324**)— 中下部(**应用新规则 fit-to-natural-bounds**:横图 W=355 H=round(355/ratio)=324,完全忽略用户字面 H=321)
 *
 * 坐标说明:
 *   - image 440 实测 1020×954(近正方形,比率 1.069);**自然 W=355 H=332**(按 fit-to-natural-bounds),渲染比 1.069 与原图差 0.01%,几乎完美
 *   - image 441 实测 1053×960(横向矩形,比率 1.097);**自然 W=355 H=324**(按 fit-to-natural-bounds),渲染比 1.096 与原图差 0.11%,几乎完美
 *   - 图2 Y=478+324=802,在书框 Y=88-872 范围内(余量 70dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 440.png(已复制为 res/drawable-nodpi/img_volume7part3_image_440.png)
 *   - 图2:D:\图\image 441.png(已复制为 res/drawable-nodpi/img_volume7part3_image_441.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-7-4 创建时按历次约定回填 onOpenVolume7Part4)。
 */
@Composable
fun Volume7Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume7Part4: () -> Unit = {},
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
            // 标题"皮影戏之小节点会加权"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 10 字 W=302(沿用 9 字规约宽度,无独立规约),点击跳第七卷-4。
            Text(
                text = "皮影戏之小节点会加权",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume7Part4),
            )

            // 图1(image 440.png,X=18, Y=135, W=355, H=332)— 在书框之上、上部(fit-to-natural-bounds 自然尺寸)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 332.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part3_image_440),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 441.png,X=18, Y=478, W=355, H=324)— 在书框之上、中下部(fit-to-natural-bounds)。
            // Y=478+324=802,在书框 Y=88-872 范围内(余量 70dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 324.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part3_image_441),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}