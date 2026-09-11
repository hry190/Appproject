package com.jueqiao.jianghu.ui.screens.volume3part7

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
 * 第三卷-7 页 — 第三卷-6 → 点击"远近藏在数中"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-11 指定"复制第一卷-2";Vol-3-6(255)→ Vol-3-7(256) 交替)
 *   - 标题文本"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-6 同款(同标题跨页叙述)
 *   - 图1(image 333.png,X=18, Y=135, W=355, H=349)— 上部
 *   - 图2(image 334.png,X=18, Y=491, W=355, H=300)— 中部
 *
 * 坐标说明:
 *   - image 333 实测 708×734(近正方形,比率 0.965);用户给 W=355 H=369,渲染比 0.962 与原图差 0.3%,几乎完美
 *   - image 334 实测 712×614(横向矩形,比率 1.160);用户给 W=362 H=308,渲染比 1.175 与原图差 1.3%,几乎完美
 *   - 用户给的坐标是本批次以来与素材比例最贴的一组(无需修订)
 *   - 代码后续真机上调过:图1 H=369→349;图2 Y=351→491, W=362→355, H=308→300(见行内注释)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 333.png(已复制为 res/drawable-nodpi/img_volume3part7_image_333.png)
 *   - 图2:D:\图\image 334.png(已复制为 res/drawable-nodpi/img_volume3part7_image_334.png)
 */
@Composable
fun Volume3Part7Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part8: () -> Unit = {},
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
            // 标题"远近藏在数中"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-6 同款,点击跳第三卷-8。
            Text(
                text = "远近藏在数中",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part8),
            )

            // 图1(image 333.png,X=18, Y=135, W=355, H=349)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 349.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part7_image_333),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 334.png,X=18, Y=491, W=355, H=300)— 在书框之上、中部。
            // Y=491+300=791,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 491.dp)
                    .size(width = 355.dp, height = 300.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part7_image_334),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}