package com.jueqiao.jianghu.ui.screens.volume3part14

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
 * 第三卷-14 页 — 第三卷-13 → 点击"多感和参"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定"复制第一卷-1";Vol-3-13(256)→ Vol-3-14(255) 交替)
 *   - 标题文本"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-12/3-13 同款(同标题跨页叙述)
 *   - 图1(image 346.png,X=18, Y=135, W=355, H=327)— 上部
 *   - 图2(image 36.png,X=18, Y=471, W=355, H=355)— 中部
 *
 * 坐标说明:
 *   - image 346 实测 1052×966(近正方形,比率 1.089);用户给 W=365 H=327 渲染比 1.116 与原图差 2.5%,几乎完美
 *   - image 36 实测 1073×1065(近正方形,比率 1.008);用户给 W=371 H=355 渲染比 1.045 与原图差 3.7%,几乎完美
 *   - 用户给的坐标与素材比例几乎完美(无需修订);2 张图都是近正方形,图2 W=371 比图1 W=365 略宽 6dp,版式对称
 *   - 图2 Y=471+355=826,在书框 Y=88-872 范围内安全(余量 46dp)
 *   - 代码后续真机上调过:图1 W=365→355;图2 W=371→355(见行内注释)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 346.png(已复制为 res/drawable-nodpi/img_volume3part14_image_346.png)
 *   - 图2:D:\图\image 36.png(已复制为 res/drawable-nodpi/img_volume3part14_image_36.png)
 */
@Composable
fun Volume3Part14Screen(
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
            // 标题"多感和参"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-3-12/3-13 同款。
            // 本屏暂无后继页,故未接 clickable(等 Vol-3-15 创建时按历次约定回填 onOpenVolume3Part15)。
            Text(
                text = "多感和参",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 346.png,X=18, Y=135, W=355, H=327)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 327.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part14_image_346),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 36.png,X=18, Y=471, W=355, H=355)— 在书框之上、中部。
            // Y=471+355=826,在书框 Y=88-872 范围内安全(余量 46dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 471.dp)
                    .size(width = 355.dp, height = 355.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part14_image_36),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}