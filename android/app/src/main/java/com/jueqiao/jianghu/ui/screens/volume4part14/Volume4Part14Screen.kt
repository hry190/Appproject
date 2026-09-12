package com.jueqiao.jianghu.ui.screens.volume4part14

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
 * 第四卷-14 页 — 第四卷-13 → 点击"路边则重算"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-4-13(256)→ Vol-4-14(255) 交替)
 *   - 标题文本"路边则重算"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-4-12/4-13 同款(同标题跨页叙述)
 *   - 图1(image 0.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 379.png,X=18, Y=318, W=355, H=321)— 中部
 *
 * 坐标说明:
 *   - image 0 实测 1049×1011(近正方形,比率 1.038);用户给 W=355 H=311 渲染比 1.141 与原图差 9.9%,图被拉宽(接近 10% 阈值)
 *   - image 379 实测 1044×1023(近正方形,比率 1.021);用户给 W=355 H=321 渲染比 1.106 与原图差 8.3%,图被轻微拉宽
 *   - 图2 Y=458+321=779,在书框 Y=88-872 范围内安全(余量 93dp)
 *   - 代码后续真机上调过:图2 Y=318→458(见行内注释)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 0.png(已复制为 res/drawable-nodpi/img_volume4part14_image_0.png)
 *   - 图2:D:\图\image 379.png(已复制为 res/drawable-nodpi/img_volume4part14_image_379.png)
 */
@Composable
fun Volume4Part14Screen(
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
            // 标题"路边则重算"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 与 Vol-4-12/4-13 同款。
            // 本屏暂无后继页,故未接 clickable(等 Vol-4-15 创建时按历次约定回填 onOpenVolume4Part15)。
            Text(
                text = "路边则重算",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 0.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part14_image_0),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 379.png,X=18, Y=458, W=355, H=321)— 在书框之上、中部。
            // Y=458+321=779,在书框 Y=88-872 范围内安全(余量 93dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 458.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume4part14_image_379),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}