package com.jueqiao.jianghu.ui.screens.volume5part6

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
 * 第五卷-6 页 — 第五卷-5 → 点击"标签不能重复和乱贴"标题跳转目标。Vol-5-6 标题点击跳 Vol-5-7。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-5-5(256)→ Vol-5-6(255) 交替)
 *   - 标题文本"标签不能重复和乱贴"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 与 Vol-5-4/5-5 同款(同标题跨页叙述,9 字 W=302)
 *   - 图1(image 393.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 394.png,X=18, Y=318, W=355, H=321)— 中部
 *
 * 坐标说明:
 *   - image 393 实测 1046×954(横向矩形,比率 1.096);用户给 W=355 H=311 渲染比 1.141 与原图差 4.1%,几乎完美
 *   - image 394 实测 1067×984(横向矩形,比率 1.084);用户给 W=355 H=321 渲染比 1.106 与原图差 2%,几乎完美
 *   - 图2 Y=318+321=639,在书框 Y=88-872 范围内安全(余量 233dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 393.png(已复制为 res/drawable-nodpi/img_volume5part6_image_393.png)
 *   - 图2:D:\图\image 394.png(已复制为 res/drawable-nodpi/img_volume5part6_image_394.png)
 */
@Composable
fun Volume5Part6Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part7: () -> Unit = {},
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
            // 标题"标签不能重复和乱贴"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 与 Vol-5-4/5-5 同款,点击跳第五卷-7。
            Text(
                text = "标签不能重复和乱贴",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part7),
            )

            // 图1(image 393.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part6_image_393),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 394.png,X=18, Y=318, W=355, H=321)— 在书框之上、中部。
            // Y=318+321=639,在书框 Y=88-872 范围内安全(余量 233dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part6_image_394),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}