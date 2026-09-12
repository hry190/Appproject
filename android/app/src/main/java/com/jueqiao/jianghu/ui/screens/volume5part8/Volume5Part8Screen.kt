package com.jueqiao.jianghu.ui.screens.volume5part8

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
 * 第五卷-8 页 — 第五卷-7 → 点击"训练，检验，测试"标题跳转目标。Vol-5-8 标题点击跳 Vol-5-9。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-12 指定"复制第一卷-2";Vol-5-7(256)→ Vol-5-8(256) 用户字面优先,连续两屏 256)
 *   - 标题文本"训练，检验，测试"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-5-7 同款(8 字 W=192 沿用 6-8 字规约)
 *   - 图1(image 398.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 399.png,X=18, Y=478, W=355, H=321)— 中下部
 *
 * 坐标说明:
 *   - image 398 实测 1070×969(横向矩形,比率 1.104);用户给 W=355 H=311 渲染比 1.141 与原图差 3.4%,可接受
 *   - image 399 实测 1065×927(横向矩形,比率 1.149);用户给 W=355 H=321 渲染比 1.106 与原图差 3.9%,可接受
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内安全(余量 73dp;用户 2026-09-12 真机 Y=318→478 调整)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 398.png(已复制为 res/drawable-nodpi/img_volume5part8_image_398.png)
 *   - 图2:D:\图\image 399.png(已复制为 res/drawable-nodpi/img_volume5part8_image_399.png)
 */
@Composable
fun Volume5Part8Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part9: () -> Unit = {},
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
            // 标题"训练，检验，测试"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-5-7 同款,点击跳第五卷-9。
            Text(
                text = "训练，检验，测试",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part9),
            )

            // 图1(image 398.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part8_image_398),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 399.png,X=18, Y=478, W=355, H=321)— 在书框之上、中下部。
            // Y=478+321=799,在书框 Y=88-872 范围内安全(余量 73dp;用户 2026-09-12 真机 Y=318→478 调整)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part8_image_399),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}