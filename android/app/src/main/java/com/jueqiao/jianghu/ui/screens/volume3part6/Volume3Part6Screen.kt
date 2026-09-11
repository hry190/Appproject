package com.jueqiao.jianghu.ui.screens.volume3part6

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
 * 第三卷-6 页 — 第三卷-5 → 点击"远近藏在数中"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-5(256)→ Vol-3-6(255) 交替回去)
 *   - 标题文本"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列(6 字,W=192 取 Vol-1-2/Vol-3-4/3-5 的同尺寸同款)
 *   - 图1(image 330.png,X=18, Y=135, W=349, H=322)— 上部
 *   - 图2(image 331.png,X=18, Y=351, W=446, H=422)— 中部(宽图,版式上比图1 更宽,与系列其他屏不同)
 *
 * 坐标说明:
 *   - 用户原始设计稿给的是 X=18/Y=135/W=352/H=229(图1)、X=18/Y=351/W=356/H=214(图2)
 *   - image 330 实测 698×612(横向矩形,比率 1.141),W=352/H=229 渲染比 1.538 偏宽 35%
 *   - image 331 实测 712×706(近正方形,比率 1.008),W=356/H=214 渲染比 1.664 偏宽 65%
 *   - 用户 2026-09-11 在 AskUserQuestion 后修正:图1 改为 349×322(畸变 5%),图2 改为 446×422(畸变 5%)
 *   - 图2 W=446 远大于图1 W=349 和系列其他屏(同卷其他图 W=352/356/350),版式上不对称 —— 用户明确选择宽图风格
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 330.png(已复制为 res/drawable-nodpi/img_volume3part6_image_330.png)
 *   - 图2:D:\图\image 331.png(已复制为 res/drawable-nodpi/img_volume3part6_image_331.png)
 */
@Composable
fun Volume3Part6Screen(
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
            // 标题"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列。
            // 本屏暂无后继页,故未接 clickable(等 Vol-3-7 创建时按历次约定回填 onOpenVolume3Part7)。
            Text(
                text = "远近藏在数中",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 330.png,X=18, Y=135, W=349, H=322)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 349.dp, height = 322.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part6_image_330),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 331.png,X=18, Y=351, W=446, H=422)— 在书框之上、中部。
            // Y=351+422=773,在书框 Y=88-872 范围内安全。宽图(版式比图1 宽 97dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 351.dp)
                    .size(width = 446.dp, height = 422.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part6_image_331),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}