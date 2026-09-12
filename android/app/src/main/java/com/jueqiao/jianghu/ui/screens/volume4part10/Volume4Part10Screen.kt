package com.jueqiao.jianghu.ui.screens.volume4part10

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
 * 第四卷-10 页 — 第四卷-9 → 点击"线索做向导"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-12 指定"复制第一卷-2";Vol-4-9(255)→ Vol-4-10(256) 交替)
 *   - 标题文本"线索做向导"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-4-9 同款(同标题跨页叙述)
 *   - 图1(image 371.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 372.png,X=18, Y=318, W=361, H=321)— 中部
 *
 * 坐标说明:
 *   - image 371 实测 1046×927(横向矩形,比率 1.128);用户给 W=355 H=311 渲染比 1.141 与原图差 1.1%,几乎完美
 *   - image 372 实测 1089×915(横向矩形,比率 1.190);用户给 W=361 H=321 渲染比 1.125 与原图差 5.5%,可接受
 *   - 图2 Y=318+321=639,在书框 Y=88-872 范围内安全(余量 233dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 371.png(已复制为 res/drawable-nodpi/img_volume4part10_image_371.png)
 *   - 图2:D:\图\image 372.png(已复制为 res/drawable-nodpi/img_volume4part10_image_372.png)
 */
@Composable
fun Volume4Part10Screen(
    onBack: () -> Unit = {},
    onOpenVolume4Part11: () -> Unit = {},
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
            // 标题"线索做向导"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-4-9 同款,点击跳第四卷-11。
            Text(
                text = "线索做向导",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume4Part11),
            )

            // 图1(image 371.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part10_image_371),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 372.png,X=18, Y=318, W=361, H=321)— 在书框之上、中部。
            // Y=318+321=639,在书框 Y=88-872 范围内安全(余量 233dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 468.dp)
                    .size(width = 361.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part10_image_372),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}