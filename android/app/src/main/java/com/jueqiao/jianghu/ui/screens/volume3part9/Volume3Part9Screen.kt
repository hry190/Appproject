package com.jueqiao.jianghu.ui.screens.volume3part9

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
 * 第三卷-9 页 — 第三卷-8 → 点击"关系织成网"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-8(255)→ Vol-3-9(255) 两连同款,打破既有交替)
 *   - 标题文本"关系织成网"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列(7 字,W=192 沿用 Vol-3-4/3-5/3-6/3-7/3-8 同款)
 *   - 图1(image 336.png,X=18, Y=135, W=349, H=322)— 上部
 *   - 图2(image 337.png,X=18, Y=471, W=346, H=322)— 中部
 *
 * 坐标说明:
 *   - image 336 实测 698×612(横向矩形,比率 1.141);用户给 W=349 H=322,渲染比 1.083 与原图差 5%,可接受
 *   - image 337 实测 672×604(横向矩形,比率 1.113);用户给 W=346 H=322,渲染比 1.074 与原图差 3.5%,可接受
 *   - 图坐标与 Vol-3-6 当前代码一致(同样的 349×322 / 346×322);但 Vol-3-6 用 image 330/331(分别为 698×612 / 712×706),
 *     本屏用 image 336/337(分别为 698×612 / 672×604) —— 图1 同像素族,图2 不同
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 336.png(已复制为 res/drawable-nodpi/img_volume3part9_image_336.png)
 *   - 图2:D:\图\image 337.png(已复制为 res/drawable-nodpi/img_volume3part9_image_337.png)
 */
@Composable
fun Volume3Part9Screen(
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
            // 标题"关系织成网"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列。
            // 本屏暂无后继页,故未接 clickable(等 Vol-3-10 创建时按历次约定回填 onOpenVolume3Part10)。
            Text(
                text = "关系织成网",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 336.png,X=18, Y=135, W=349, H=322)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 349.dp, height = 322.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part9_image_336),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 337.png,X=18, Y=471, W=346, H=322)— 在书框之上、中部。
            // Y=471+322=793,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 471.dp)
                    .size(width = 346.dp, height = 322.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part9_image_337),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}