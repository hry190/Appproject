package com.jueqiao.jianghu.ui.screens.volume3part3

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
 * 第三卷-3 页 — 第三卷-2 → 点击"万物形成符"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(交替模式:Vol-3-1(255)→ Vol-3-2(256)→ Vol-3-3(255))
 *   - 标题文本"万物形成符"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-3-1/3-2 同位置同样式
 *   - 图1(image 323.png,X=18, Y=260, W=352, H=352)— 居中(只 1 张图)
 *
 * 坐标说明:
 *   - 用户原始设计稿给的是 X=28/Y=155/W=352/H=203
 *   - image 323 实测 699×726(接近正方形,比率 0.963);H=203 会把图纵向压扁到 28% 高度,与原图严重不符
 *   - image 323 与 Vol-3-1/3-2 的图(1.137 横向矩形)版式不同,不能套用 352×327。改按"近原图比例"用 352×352(用户 2026-09-11 确认)
 *   - 居中放置:Y 起点 = (书框中部 - 图一半)≈ 260,使图视觉上落在书框正中央
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 323.png(已复制为 res/drawable-nodpi/img_volume3part3_image_323.png)
 */
@Composable
fun Volume3Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part4: () -> Unit = {},
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
            // 标题"万物形成符"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-3-1/3-2 同款,点击跳第三卷-4。
            Text(
                text = "万物形成符",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part4),
            )

            // 图1(image 323.png,X=18, Y=260, W=352, H=352)— 在书框之上、居中。
            // Y=260+352=612,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 260.dp)
                    .size(width = 352.dp, height = 352.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part3_image_323),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}