package com.jueqiao.jianghu.ui.screens.volume3part4

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
 * 第三卷-4 页 — 第三卷-3 → 点击"特征与信息是否有关"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-11 指定,与 Vol-3-3 同款;注:这使 Vol-3-3(255)→ Vol-3-4(255) 连续两屏同款,打破既有 Vol-2/3 交替模式)
 *   - 标题文本"特征与信息是否有关"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— **新标题系列**;7 字文本,W=192 与 Vol-1-2(7字"规则与学习的区别")同款
 *   - 图1(image 324.png,X=18, Y=135, W=352, H=229)— 上部
 *   - 图2(image 325.png,X=18, Y=351, W=356, H=214)— 中部
 *   - 图3(image 326.png,X=18, Y=576, W=350, H=296)— 下部(限高保书框底安全)
 *
 * 坐标说明:
 *   - 用户原始设计稿:图3 H=342 → 渲染下沿 Y=918,超出书框底 872 共 46dp
 *   - 用户确认改为 H=296 → 渲染下沿 Y=872,贴书框底不越界
 *   - 渲染畸变 ~21% 横向被拉(渲染比 1.182 vs 原图 1.429),但 Y=576+296=872 在书框 88-872 范围内安全
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 324.png(已复制为 res/drawable-nodpi/img_volume3part4_image_324.png)
 *   - 图2:D:\图\image 325.png(已复制为 res/drawable-nodpi/img_volume3part4_image_325.png)
 *   - 图3:D:\图\image 326.png(已复制为 res/drawable-nodpi/img_volume3part4_image_326.png)
 */
@Composable
fun Volume3Part4Screen(
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
            // 标题"特征与信息是否有关"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 新标题系列。
            // 本屏暂无后继页,故未接 clickable(等 Vol-3-5 创建时按历次约定回填 onOpenVolume3Part5)。
            Text(
                text = "特征与信息是否有关",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp),
            )

            // 图1(image 324.png,X=18, Y=135, W=352, H=229)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 352.dp, height = 229.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part4_image_324),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 325.png,X=18, Y=351, W=356, H=214)— 在书框之上、中部。
            // Y=351+214=565,在书框 Y=88-872 范围内安全。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 351.dp)
                    .size(width = 356.dp, height = 214.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part4_image_325),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 326.png,X=18, Y=576, W=350, H=296)— 在书框之上、下部(贴书框底)。
            // Y=576+296=872,刚好书框底,不越界。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 576.dp)
                    .size(width = 350.dp, height = 296.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume3part4_image_326),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}