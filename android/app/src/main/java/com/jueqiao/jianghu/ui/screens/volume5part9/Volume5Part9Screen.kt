package com.jueqiao.jianghu.ui.screens.volume5part9

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
 * 第五卷-9 页 — 第五卷-8 → 点击"训练，检验，测试"标题跳转目标。Vol-5-9 标题点击跳 Vol-5-10。
 *
 * 布局(z-order 由下到上,**4 层,无图 2**):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-5-8(256)→ Vol-5-9(255) 恢复交替)
 *   - 标题文本"训练，检验，测试"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 8 字 W=192 沿用 6-8 字规约
 *   - 图1(image 400.png,X=18, Y=135, W=355, H=311)— 上部
 *
 * **本屏无图 2**(用户 2026-09-12 创建指令仅指定 image 400,经 AskUserQuestion 确认"确认 Vol-5-9 只有图1,跳过图2")。
 *
 * 坐标说明:
 *   - image 400 实测 1032×909(横向矩形,比率 1.135);用户给 W=355 H=311 渲染比 1.141 与原图差 0.5%,几乎完美
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 400.png(已复制为 res/drawable-nodpi/img_volume5part9_image_400.png)
 */
@Composable
fun Volume5Part9Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part10: () -> Unit = {},
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
            // 标题"训练，检验，测试"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 8 字 W=192,点击跳第五卷-10。
            Text(
                text = "训练，检验，测试",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part10),
            )

            // 图1(image 400.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            // **本屏无图 2**(已通过 AskUserQuestion 与用户确认跳过)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part9_image_400),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}