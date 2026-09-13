package com.jueqiao.jianghu.ui.screens.volume8part2

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
 * 第八卷-2 页 — 第八卷-1 → 点击"皮影戏之状态、行动、奖励"标题跳转目标。**Vol-8-2 标题点击跳 Vol-8-3**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-8-1(255)→ Vol-8-2(256) 恢复交替)
 *   - 标题文本"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X=110, Y=67, **W=400, H=32**)— **12 字**(全中文含 2 个顿号「、」)W=400 沿用估算 ~33/字宽度(与 Vol-8-1 同款 12 字规约,跨页同标题)
 *   - 图1(image 466.png,X=18, Y=135, **W=355, H=241**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.474)=241)
 *   - 图2(image 467.png,X=18, Y=478, **W=355, H=256**)— 中下部(应用新规则:横图 W=355 H=round(355/1.385)=256)
 *
 * 坐标说明:
 *   - image 466 实测 1017×690(横图,比率 1.474);自然 W=355 H=241,渲染比 1.473 与原图差 0.06%,几乎完美
 *   - image 467 实测 1089×786(横图,比率 1.385);自然 W=355 H=256,渲染比 1.387 与原图差 0.09%,几乎完美
 *   - 图2 Y=478+256=734,在书框 Y=88-872 范围内(余量 138dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 466.png(已复制为 res/drawable-nodpi/img_volume8part2_image_466.png)
 *   - 图2:D:\图\image 467.png(已复制为 res/drawable-nodpi/img_volume8part2_image_467.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-3 创建时按历次约定回填 onOpenVolume8Part3)。
 */
@Composable
fun Volume8Part2Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part3: () -> Unit = {},
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
            // 标题"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=400, H=32)— 12 字 W=400(沿用估算 ~33/字宽度,12 字无独立规约),点击跳第八卷-3。
            Text(
                text = "皮影戏之状态、行动、奖励",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part3),
            )

            // 图1(image 466.png,X=18, Y=135, W=355, H=241)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 241.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part2_image_466),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 467.png,X=18, Y=478, W=355, H=256)— 在书框之上、中下部。
            // Y=478+256=734,在书框 Y=88-872 范围内(余量 138dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 256.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part2_image_467),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}