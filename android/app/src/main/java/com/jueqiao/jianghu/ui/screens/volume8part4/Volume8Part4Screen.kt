package com.jueqiao.jianghu.ui.screens.volume8part4

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
 * 第八卷-4 页 — 第八卷-3 → 点击"皮影戏之探新还是用熟"标题跳转目标。**Vol-8-4 标题点击跳 Vol-8-5**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-3(255)→ Vol-8-4(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"皮影戏之探新还是用熟"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **10 字**(全中文无标点)W=302 沿用 9 字规约(10 字无独立规约,KDoc 标注异常,真机可微调;新标题系列)
 *   - 图1(image 520.png,X=18, Y=135, **W=355, H=291**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.220)=291)
 *   - 图2(image 521.png,X=18, Y=478, **W=355, H=307**)— 中下部(应用新规则:横图 W=355 H=round(355/1.156)=307)
 *
 * 坐标说明:
 *   - image 520 实测 1047×858(横图,比率 1.220);自然 W=355 H=291,渲染比 1.220 与原图差 0.03%,几乎完美
 *   - image 521 实测 1068×924(横图,比率 1.156);自然 W=355 H=307,渲染比 1.156 与原图差 0.04%,几乎完美
 *   - 图2 Y=478+307=785,在书框 Y=88-872 范围内(余量 87dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 520.png(已复制为 res/drawable-nodpi/img_volume8part4_image_520.png)
 *   - 图2:D:\图\image 521.png(已复制为 res/drawable-nodpi/img_volume8part4_image_521.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-5 创建时按历次约定回填 onOpenVolume8Part5)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-3标题" → 实际意图"第八卷-3 的标题"(缺少" 的"),无歧义
 */
@Composable
fun Volume8Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part5: () -> Unit = {},
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
            // 标题"皮影戏之探新还是用熟"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=302, H=32)— 10 字 W=302,点击跳第八卷-5。
            Text(
                text = "皮影戏之探新还是用熟",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part5),
            )

            // 图1(image 520.png,X=18, Y=135, W=355, H=291)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 291.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part4_image_520),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 521.png,X=18, Y=478, W=355, H=307)— 在书框之上、中下部。
            // Y=478+307=785,在书框 Y=88-872 范围内(余量 87dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 307.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part4_image_521),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}