package com.jueqiao.jianghu.ui.screens.volume6part9

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
 * 第六卷-9 页 — 第六卷-8 → 点击"有名归类，无名成群"标题跳转目标。**Vol-6-9 标题点击跳 Vol-6-10**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-6-8(256)→ Vol-6-9(255) 恢复交替)
 *   - 标题文本"有名归类，无名成群"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **9 字**(含 1 个中文`,`)W=302 沿用 Vol-3-5 同款真机测过宽度(与 Vol-6-7/6-8 同款,跨页同标题)
 *   - 图1(image 422.png,X=18, Y=135, W=355, **H=289**)— 上部(**用户字面 H=311 → 自然 H=289** 按宽度调整规则自动重算,采用自然高度 355/1.230 消除畸变)
 *   - 图2(image 423.png,X=18, Y=478, W=355, **H=293**)— 中下部(**用户字面 H=321 → 自然 H=293** 按宽度调整规则自动重算,采用自然高度 355/1.210 消除畸变;沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 422 实测 1044×849(横向矩形,比率 1.230);**实际 H=289**(用户 2026-09-12 字面 H=311→ 自然 H=289),渲染比 1.228 与原图差 0.2%,几乎完美
 *   - image 423 实测 1049×867(横向矩形,比率 1.210);**实际 H=293**(用户 2026-09-12 字面 H=321→ 自然 H=293),渲染比 1.212 与原图差 0.2%,几乎完美
 *   - 图2 Y=478+293=771,在书框 Y=88-872 范围内(余量 101dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 422.png(已复制为 res/drawable-nodpi/img_volume6part9_image_422.png)
 *   - 图2:D:\图\image 423.png(已复制为 res/drawable-nodpi/img_volume6part9_image_423.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-6-10 创建时按历次约定回填 onOpenVolume6Part10)。
 */
@Composable
fun Volume6Part9Screen(
    onBack: () -> Unit = {},
    onOpenVolume6Part10: () -> Unit = {},
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
            // 标题"有名归类，无名成群"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 9 字 W=302,点击跳第六卷-10。
            Text(
                text = "有名归类，无名成群",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume6Part10),
            )

            // 图1(image 422.png,X=18, Y=135, W=355, H=289)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 289.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part9_image_422),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 423.png,X=18, Y=478, W=355, H=293)— 在书框之上、中下部。
            // Y=478+293=771,在书框 Y=88-872 范围内(余量 101dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 293.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part9_image_423),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}