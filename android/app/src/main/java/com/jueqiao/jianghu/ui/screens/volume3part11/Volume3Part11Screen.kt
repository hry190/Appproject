package com.jueqiao.jianghu.ui.screens.volume3part11

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * 第三卷-11 页 — 第三卷-10 → 点击"关系织成网"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-10(256)→ Vol-3-11(255) 交替)
 *   - 标题文本"关系织成网"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-9/3-10 同款(同标题跨页叙述)
 *   - 图1(image 338.png,X=18, Y=135, W=355, H=249)— 上部
 *   - 图2(image 380.png,X=18, Y=471, W=356, H=288)— 中部
 *
 * 坐标说明:
 *   - image 338 实测 713×500(横向矩形,比率 1.426);用户给 W=355 H=230 渲染比 1.543 拉宽 8%
 *   - image 380 实测 712×880(纵向矩形,比率 0.809);用户给 W=356 H=320 渲染比 1.113 拉宽 38%
 *   - 用户 2026-09-11 在 AskUserQuestion 后改为:图1 H=230→249(套原图比例,0% 畸变),图2 H=320→288(套原图比例,0% 畸变)
 *   - 图2 Y=471+288=759,在书框 Y=88-872 范围内安全
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 340.png(已复制为 res/drawable-nodpi/img_volume3part11_image_340.png;2026-09-11 由用户从原 image 338.png 替换)
 *   - 图2:D:\图\image 380.png(已复制为 res/drawable-nodpi/img_volume3part11_image_380.png)
 */
@Composable
fun Volume3Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part12: () -> Unit = {},
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
            // 标题"关系织成网"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-9/3-10 同款,点击跳第三卷-12。
            Text(
                text = "关系织成网",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part12),
            )

            // 图1(image 338.png,X=18, Y=135, W=355, H=249)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 236.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part11_image_340),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 380.png,X=18, Y=471, W=356, H=288)— 在书框之上、中部。
            // Y=471+288=759,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 411.dp)
                    .size(width = 356.dp, height = 440.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part11_image_380),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}