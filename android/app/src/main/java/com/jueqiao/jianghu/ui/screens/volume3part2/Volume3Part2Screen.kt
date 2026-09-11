package com.jueqiao.jianghu.ui.screens.volume3part2

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
 * 第三卷-2 页 — 第三卷-1 → 点击"万物形成符"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(交替模式:Vol-1-2 / Vol-2-2 / Vol-2-8 / Vol-2-11 / Vol-2-14 / Vol-3-2 用 256,其他屏用 255)
 *   - 标题文本"万物形成符"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-3-1 同位置同样式
 *   - 图1(image 320.png,X=18, Y=135, W=352, H=327)— 上部
 *   - 图2(image 321.png,X=18, Y=361, W=356, H=214)— 中部
 *
 * 坐标说明:
 *   - 用户原始设计稿给的是 X=28/Y=155/W=352/H=203(图1)、X=24/Y=381/W=356/H=214(图2)
 *   - 图1 H=203 与素材真实宽高比不符(image 320 为 696×612,比率 1.137;H=203 会渲染成比率 1.734,
 *     纵向压扁到约 66%)。image 320 与 Vol-3-1 图1 素材 image 316(698×612)近乎孪生,
 *     故沿用 Vol-3-1 已实调的值 352×327(用户 2026-09-11 确认按此版)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 320.png(已复制为 res/drawable-nodpi/img_volume3part2_image_320.png)
 *   - 图2:D:\图\image 321.png(已复制为 res/drawable-nodpi/img_volume3part2_image_321.png)
 */
@Composable
fun Volume3Part2Screen(
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
            // 标题"万物形成符"(字号 24,bold,黑色,X=110, Y=67, W=213, H=32)— 与 Vol-3-1 同款。
            // 本屏暂无后继页,故未接 clickable(等 Vol-3-3 创建时按历次约定回填 onOpenVolume3Part3)。
            Text(
                text = "万物形成符",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 213.dp, height = 32.dp),
            )

            // 图1(image 320.png,X=18, Y=135, W=352, H=327)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 352.dp, height = 327.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part2_image_320),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 321.png,X=18, Y=361, W=356, H=214)— 在书框之上、中部。
            // Y=361+214=575,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 361.dp)
                    .size(width = 356.dp, height = 214.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part2_image_321),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
