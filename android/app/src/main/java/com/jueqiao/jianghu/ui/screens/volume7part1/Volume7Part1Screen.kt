package com.jueqiao.jianghu.ui.screens.volume7part1

import androidx.activity.compose.BackHandler
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
 * 第七卷-1 页 — 滚轮15 → 点击"已解锁9"图像跳转目标(新卷首屏)。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";第七卷首屏)
 *   - 标题文本"皮影戏之小节点会加权"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **10 字**(含 1 个中文`,`)W=302 沿用 9 字规约(10 字无独立规约,KDoc 标注异常,真机可微调)
 *   - 图1(image 436.png,X=18, Y=135, **W=355, H=277**)— 上部(**应用 2026-09-12 第 2 次新规则 fit-to-natural-bounds**:横图 W=355 H=round(355/ratio)=277,完全忽略用户字面 H=311)
 *   - 图2(image 437.png,X=18, Y=478, **W=355, H=267**)— 中下部(**应用新规则 fit-to-natural-bounds**:横图 W=355 H=round(355/ratio)=267,完全忽略用户字面 H=321)
 *
 * 坐标说明:
 *   - image 436 实测 1047×816(横向矩形,比率 1.283);**自然 W=355 H=277**(按 fit-to-natural-bounds),渲染比 1.282 与原图差 0.12%,几乎完美
 *   - image 437 实测 1065×801(横向矩形,比率 1.330);**自然 W=355 H=267**(按 fit-to-natural-bounds),渲染比 1.330 与原图差 0%,完全匹配
 *   - 图2 Y=478+267=745,在书框 Y=88-872 范围内(余量 127dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 436.png(已复制为 res/drawable-nodpi/img_volume7part1_image_436.png)
 *   - 图2:D:\图\image 437.png(已复制为 res/drawable-nodpi/img_volume7part1_image_437.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-7-2 创建时按历次约定回填 onOpenVolume7Part2)。
 */
@Composable
fun Volume7Part1Screen(
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
            // 标题"皮影戏之小节点会加权"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 10 字 W=302(沿用 9 字规约宽度,无独立规约)。
            // 本屏暂无后继页,故未接 clickable(等 Vol-7-2 创建时按历次约定回填 onOpenVolume7Part2)。
            Text(
                text = "皮影戏之小节点会加权",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp),
            )

            // 图1(image 436.png,X=18, Y=135, W=355, H=277)— 在书框之上、上部(fit-to-natural-bounds 自然尺寸)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 277.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part1_image_436),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 437.png,X=18, Y=478, W=355, H=267)— 在书框之上、中下部(fit-to-natural-bounds)。
            // Y=478+267=745,在书框 Y=88-872 范围内(余量 127dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 267.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part1_image_437),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}