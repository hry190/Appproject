package com.jueqiao.jianghu.ui.screens.volume8part3

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
 * 第八卷-3 页 — 第八卷-2 → 点击"皮影戏之状态、行动、奖励"标题跳转目标。**Vol-8-3 标题点击跳 Vol-8-4**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-2(256)→ Vol-8-3(255) 恢复交替)
 *   - 标题文本"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X=110, Y=67, **W=400, H=32**)— **12 字**(全中文含 2 个顿号「、」)W=400 沿用估算 ~33/字宽度(与 Vol-8-1/8-2 同款 12 字规约,跨页同标题)
 *   - 图1(image 468.png,X=18, Y=135, **W=380, H=394**)— 上部(应用新规则 fit-to-natural-bounds:近正方形 ratio 0.963,自动 fit H=394 W=round(394×0.963)=380)
 *   - 图2(image 469.png,X=18, Y=478, **W=355, H=135**)— 中下部(应用新规则:横图 W=355 H=round(355/2.625)=135)
 *
 * 坐标说明:
 *   - image 468 实测 1049×1089(近正方形,比率 0.963);自然 W=380 H=394,渲染比 0.964 与原图差 0.12%,几乎完美
 *   - image 469 实测 1071×408(极扁横图,比率 2.625);自然 W=355 H=135,渲染比 2.630 与原图差 0.18%,几乎完美
 *   - 图1 Y=135+394=529,图 2 Y=478+135=613(均在书框 Y=88-872 范围内,余量 343/259dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 468.png(已复制为 res/drawable-nodpi/img_volume8part3_image_468.png)
 *   - 图2:D:\图\image 469.png(已复制为 res/drawable-nodpi/img_volume8part3_image_469.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-4 创建时按历次约定回填 onOpenVolume8Part4)。
 */
@Composable
fun Volume8Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part4: () -> Unit = {},
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
            // 标题"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=400, H=32)— 12 字 W=400,点击跳第八卷-4。
            Text(
                text = "皮影戏之状态、行动、奖励",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part4),
            )

            // 图1(image 468.png,X=18, Y=135, W=380, H=394)— 在书框之上、上部(近正方形,占书框大半高)。
            // Y=135+394=529,在书框 Y=88-872 范围内(余量 343dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 354.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part3_image_468),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 469.png,X=18, Y=478, W=355, H=135)— 在书框之上、中下部(极扁)。
            // Y=478+135=613,在书框 Y=88-872 范围内(余量 259dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 538.dp)
                    .size(width = 355.dp, height = 135.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part3_image_469),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}