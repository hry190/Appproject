package com.jueqiao.jianghu.ui.screens.volume5part11

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
 * 第五卷-11 页 — 第五卷-10 → 点击"偏差的数据"标题跳转目标。Vol-5-11 标题点击跳 Vol-5-12。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-12 指定"复制第一卷-2";Vol-5-10(255)→ Vol-5-11(256) 恢复交替)
 *   - 标题文本"偏差的数据"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 5 字 W=213 沿用 5 字规约(与 Vol-5-10 同款,跨页同标题)
 *   - 图1(image 4.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 4001.png,X=18, Y=478, W=355, H=321)— 中下部(沿用 Vol-5-7/5-8/5-10 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 4 实测 1047×951(横向矩形,比率 1.101);用户给 W=355 H=311 渲染比 1.141 与原图差 3.6%,可接受
 *   - image 4001 实测 1071×962(横向矩形,比率 1.113);用户给 W=355 H=321 渲染比 1.106 与原图差 0.6%,几乎完美
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 4.png(已复制为 res/drawable-nodpi/img_volume5part11_image_4.png)
 *   - 图2:D:\图\image 4001.png(已复制为 res/drawable-nodpi/img_volume5part11_image_4001.png)
 */
@Composable
fun Volume5Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part12: () -> Unit = {},
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
            // 标题"偏差的数据"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 5 字 W=213,点击跳第五卷-12。
            Text(
                text = "偏差的数据",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part12),
            )

            // 图1(image 4.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part11_image_4),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 4001.png,X=18, Y=478, W=355, H=321)— 在书框之上、中下部。
            // Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part11_image_4001),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}