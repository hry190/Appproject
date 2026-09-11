package com.jueqiao.jianghu.ui.screens.volume3part12

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
 * 第三卷-12 页 — 第三卷-11 → 点击"多感和参"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-11(255)→ Vol-3-12(255) 两连同款,打破既有交替)
 *   - 标题文本"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列(4 字短标题)
 *   - 图1(image 341.png,X=18, Y=135, W=355, H=333)— 上部
 *   - 图2(image 342.png,X=18, Y=481, W=355, H=320)— 中部
 *
 * 坐标说明:
 *   - image 341 实测 1032×975(近正方形,比率 1.058);用户给 W=355 H=333 渲染比 1.066 与原图差 0.7%,几乎完美
 *   - image 342 实测 1068×975(近正方形,比率 1.095);用户给 W=371 H=340 渲染比 1.091 与原图差 0.4%,几乎完美
 *   - 用户给的坐标是本批次以来与素材比例最贴的一组(无需修订);2 张图都是近正方形(1.058 / 1.095),且渲染后图2 W=371 略大于图1 W=355(宽 16dp,版式对称性可接受)
 *   - 图2 Y=471+340=811,在书框 Y=88-872 范围内安全(余量 61dp)
 *   - 代码后续真机上调过:图2 Y=471→481, W=371→355, H=340→320(见行内注释)
 *
 * 注:链路为 Vol-3-9 → 3-10 → 3-11 → 3-12(标准递增)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 341.png(已复制为 res/drawable-nodpi/img_volume3part12_image_341.png)
 *   - 图2:D:\图\image 342.png(已复制为 res/drawable-nodpi/img_volume3part12_image_342.png)
 */
@Composable
fun Volume3Part12Screen(
    onBack: () -> Unit = {},
    onOpenVolume3Part13: () -> Unit = {},
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
            // 标题"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列,点击跳第三卷-13。
            Text(
                text = "多感和参",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume3Part13),
            )

            // 图1(image 341.png,X=18, Y=135, W=355, H=333)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 333.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part12_image_341),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 342.png,X=18, Y=481, W=355, H=320)— 在书框之上、中部。
            // Y=481+320=801,在书框 Y=88-872 范围内安全(余量 71dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 481.dp)
                    .size(width = 355.dp, height = 320.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part12_image_342),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}