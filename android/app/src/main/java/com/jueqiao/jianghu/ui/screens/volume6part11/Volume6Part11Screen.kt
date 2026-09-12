package com.jueqiao.jianghu.ui.screens.volume6part11

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
 * 第六卷-11 页 — 第六卷-10 → 点击"门槛一动，错法不同"标题跳转目标。**Vol-6-11 标题点击跳 Vol-6-12**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-10 书框素材(用户 2026-09-12 指定"复制第一卷-10";Vol-1-10 用 Group 256;Vol-6-10(255)→ Vol-6-11(256) 恢复交替)
 *   - 标题文本"门槛一动，错法不同"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **9 字**(含 1 个中文`,`)W=302 沿用 Vol-3-5 同款真机测过宽度(新标题系列)
 *   - 图1(image 427.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 428.png,X=18, Y=478, W=355, H=321)— 中下部(沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 427 实测 1047×951(横向矩形,比率 1.101);用户给 W=355 H=311 渲染比 1.141 与原图差 3.6%,可接受(按宽度调整规则阈值 5% 内)
 *   - image 428 实测 363×321(横向矩形,比率 1.131,小图);用户给 W=355 H=321 渲染比 1.106 与原图差 2.2%,几乎完美(小图略放大,无视觉问题)
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp;沿用 Vol-5-7+ 真机调整过的 Y=478)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-10 img_volume1part10_group_256 资源,与第一卷-2 img_volume1part2_group_256 同源 PNG)
 *   - 图1:D:\图\image 427.png(已复制为 res/drawable-nodpi/img_volume6part11_image_427.png)
 *   - 图2:D:\图\image 428.png(已复制为 res/drawable-nodpi/img_volume6part11_image_428.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-6-12 创建时按历次约定回填 onOpenVolume6Part12)。
 *
 * 用户指令笔误留痕(2026-09-12):
 *   - 用户写"第六卷-8-11" → 实际意图"第六卷-11"(按上下文推断,无歧义)
 *   - 用户写"复制第一卷-10页面" → 实际为第一卷-10(非第一卷-1 或第一卷-2),书框意外不是 255 而是 256(Vol-1-10 用 Group 256)
 */
@Composable
fun Volume6Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume6Part12: () -> Unit = {},
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

        // 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-10 素材(Vol-1-10 用 Group 256)。
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
            // 标题"门槛一动，错法不同"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 9 字 W=302,点击跳第六卷-12。
            Text(
                text = "门槛一动，错法不同",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume6Part12),
            )

            // 图1(image 427.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part11_image_427),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 428.png,X=18, Y=478, W=355, H=321)— 在书框之上、中下部。
            // Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp;沿用 Vol-5-7+ 真机调整过的 Y=478)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part11_image_428),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}