package com.jueqiao.jianghu.ui.screens.volume3part13

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
 * 第三卷-13 页 — 第三卷-12 → 点击"多感和参"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-11 指定"复制第一卷-2";Vol-3-12(255)→ Vol-3-13(256) 交替)
 *   - 标题文本"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-12 同款(同标题跨页叙述)
 *   - 图1(image 345.png,X=18, Y=135, W=375, H=343)— 上部
 *   - 图2(image 343.png,X=18, Y=471, W=361, H=330)— 中部
 *
 * 坐标说明:
 *   - image 345 实测 1071×996(近正方形,比率 1.075);用户给 W=375 H=343 渲染比 1.093 与原图差 1.7%,几乎完美
 *   - image 343 实测 1050×962(近正方形,比率 1.091);用户给 W=361 H=330 渲染比 1.094 与原图差 0.3%,几乎完美
 *   - 用户给的坐标与素材比例几乎完美(无需修订);2 张图都是近正方形,图1 W=375 比图2 W=361 略宽 14dp,版式对称性可接受
 *   - 图2 Y=471+330=801,在书框 Y=88-872 范围内安全(余量 71dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 345.png(已复制为 res/drawable-nodpi/img_volume3part13_image_345.png)
 *   - 图2:D:\图\image 343.png(已复制为 res/drawable-nodpi/img_volume3part13_image_343.png)
 */
@Composable
fun Volume3Part13Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part14: () -> Unit = {},
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
            // 标题"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-12 同款,点击跳第三卷-14。
            Text(
                text = "多感和参",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part14),
            )

            // 图1(image 345.png,X=18, Y=135, W=375, H=343)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 375.dp, height = 343.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part13_image_345),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 343.png,X=18, Y=471, W=361, H=330)— 在书框之上、中部。
            // Y=471+330=801,在书框 Y=88-872 范围内安全(余量 71dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 471.dp)
                    .size(width = 361.dp, height = 330.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part13_image_343),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}