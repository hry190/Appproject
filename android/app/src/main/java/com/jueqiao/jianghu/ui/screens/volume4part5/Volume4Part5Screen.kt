package com.jueqiao.jianghu.ui.screens.volume4part5

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
 * 第四卷-5 页 — 第四卷-4 → 点击"穷尽还是剪枝"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-4-4(256)→ Vol-4-5(255) 交替)
 *   - 标题文本"穷尽还是剪枝"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-4-3/4-4 同款(同标题跨页叙述)
 *   - 图1(image 359.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 360.png,X=18, Y=478, W=361, H=321)— 中部
 *
 * 坐标说明:
 *   - image 359 实测 1049×966(近正方形,比率 1.086);用户给 W=355 H=311 渲染比 1.141 与原图差 5%,可接受
 *   - image 360 实测 1064×978(近正方形,比率 1.088);用户给 W=361 H=321 渲染比 1.125 与原图差 3.4%,可接受
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内安全(余量 73dp)
 *   - 代码后续真机上调过:图2 Y=318→478(见行内注释)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 359.png(已复制为 res/drawable-nodpi/img_volume4part5_image_359.png)
 *   - 图2:D:\图\image 360.png(已复制为 res/drawable-nodpi/img_volume4part5_image_360.png)
 */
@Composable
fun Volume4Part5Screen(
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
            // 标题"穷尽还是剪枝"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-4-3/4-4 同款。
            // 本屏暂无后继页,故未接 clickable(等 Vol-4-6 创建时按历次约定回填 onOpenVolume4Part6)。
            Text(
                text = "穷尽还是剪枝",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 359.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part5_image_359),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 360.png,X=18, Y=478, W=361, H=321)— 在书框之上、中部。
            // Y=478+321=799,在书框 Y=88-872 范围内安全(余量 73dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 361.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part5_image_360),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}