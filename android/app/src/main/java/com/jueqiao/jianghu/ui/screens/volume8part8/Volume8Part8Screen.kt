package com.jueqiao.jianghu.ui.screens.volume8part8

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
 * 第八卷-8 页 — 第八卷-7 → 点击"皮影戏之每一步奖励"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-8-7(255)→ Vol-8-8(256) 恢复交替)
 *   - 标题文本"皮影戏之每一步奖励"(字号 24,bold,黑色,**X 轴居中**(子 Text 自然宽), Y=67, W=父宽, H=32)— **9 字**(全中文无标点)W=302 沿用 9 字规约(与 Vol-8-7 同款 9 字规约,跨页同标题)
 *   - 图1(image 528.png,X=18, Y=135, **W=355, H=265**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.341)=265)
 *   - 图2(image 529.png,X=18, Y=478, **W=355, H=277**)— 中下部(应用新规则:横图 W=355 H=round(355/1.280)=277)
 *
 * 坐标说明:
 *   - image 528 实测 1026×765(横图,比率 1.341);自然 W=355 H=265,渲染比 1.340 与原图差 0.12%,几乎完美
 *   - image 529 实测 1089×851(横图,比率 1.280);自然 W=355 H=277,渲染比 1.282 与原图差 0.15%,几乎完美
 *   - 图2 Y=478+277=755,在书框 Y=88-872 范围内(余量 117dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 528.png(已复制为 res/drawable-nodpi/img_volume8part8_image_528.png)
 *   - 图2:D:\图\image 529.png(已复制为 res/drawable-nodpi/img_volume8part8_image_529.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-9 创建时按历次约定回填 onOpenVolume8Part9)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-7标题" → 实际意图"第八卷-7 的标题"(缺少" 的"),无歧义
 */
@Composable
fun Volume8Part8Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part9: () -> Unit = {},
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
            // 标题"皮影戏之每一步奖励"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 9 字 W=302,点击跳第八卷-9。
            Text(
                text = "皮影戏之每一步奖励",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part9),
            )

            // 图1(image 528.png,X=18, Y=135, W=355, H=265)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 265.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part8_image_528),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 529.png,X=18, Y=478, W=355, H=277)— 在书框之上、中下部。
            // Y=478+277=755,在书框 Y=88-872 范围内(余量 117dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 277.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part8_image_529),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}