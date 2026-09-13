package com.jueqiao.jianghu.ui.screens.volume8part1

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
 * 第八卷-1 页 — 滚轮11 → 点击"已解锁9"图像跳转目标(新卷首屏)。**Vol-8-1 标题点击跳 Vol-8-2**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";第八卷首屏)
 *   - 标题文本"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X=110, Y=67, **W=400, H=32**)— **12 字**(全中文含 2 个顿号「、」,顿号算字符)W=400 沿用估算 ~33/字宽度(**12 字无独立规约,KDoc 标注异常,真机可微调**;Vol-1~8 标题最长历史)
 *   - 图1(image 464.png,X=18, Y=135, **W=355, H=227**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.565)=227)
 *   - 图2(image 465.png,X=18, Y=478, **W=382, H=394**)— 中下部(应用新规则:近正方形 ratio 0.970,自动 fit H=394 W=round(394×0.970)=382 — 0% 畸变)
 *
 * 坐标说明:
 *   - image 464 实测 1047×669(横图,比率 1.565);自然 W=355 H=227,渲染比 1.564 与原图差 0.07%,几乎完美
 *   - image 465 实测 1065×1098(近正方形,比率 0.970);自然 W=382 H=394,渲染比 0.970 与原图差 0%,完全匹配
 *   - 图2 Y=478+394=872,正好到书框底 — 极紧但 fit
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 464.png(已复制为 res/drawable-nodpi/img_volume8part1_image_464.png)
 *   - 图2:D:\图\image 465.png(已复制为 res/drawable-nodpi/img_volume8part1_image_465.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-2 创建时按历次约定回填 onOpenVolume8Part2)。
 */
@Composable
fun Volume8Part1Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part2: () -> Unit = {},
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
            // 标题"皮影戏之状态、行动、奖励"(字号 24,bold,黑色,X=110, Y=67, W=400, H=32)— 12 字 W=400(沿用估算 ~33/字宽度,12 字无独立规约),点击跳第八卷-2。
            Text(
                text = "皮影戏之状态、行动、奖励",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 400.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume8Part2),
            )

            // 图1(image 464.png,X=18, Y=135, W=355, H=227)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 227.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part1_image_464),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 465.png,X=18, Y=478, W=382, H=394)— 在书框之上、中下部(刚好到书框底 872,极紧)。
            // Y=478+394=872,在书框 Y=88-872 范围内(余量 0dp,贴底)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 382.dp, height = 394.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part1_image_465),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}