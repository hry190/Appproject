package com.jueqiao.jianghu.ui.screens.volume5part15

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
 * 第五卷-15 页 — 第五卷-14 → 点击"死记硬背不可行"标题跳转目标。**标题点击跳 Gunlun12(卷末闭环,仿 Vol-4-14 → Gunlun8 模式)**。
 *
 * 布局(z-order 由下到上,**4 层,无图 2**):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-5-14(256)→ Vol-5-15(255) 恢复交替)
 *   - 标题文本"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192 沿用 6-8 字规约(与 Vol-5-13/5-14 同款,跨页同标题)
 *   - 图1(image 407.png,X=18, Y=135, **W=355, H=351**)— 上部(H=391→351 用户 2026-09-12 真机调整)
 *
 * **本屏无图 2**(用户 2026-09-12 创建指令仅指定 image 407;沿用 Vol-5-9 单图先例直接采用 4 层 z-order,如有出入随时改回 5 层)
 *
 * 坐标说明:
 *   - image 407 实测 1049×1089(近正方形略竖,比率 0.963);**实际 H=351**(用户 2026-09-12 真机从 H=391 调到 351),渲染比 1.011 与原图差 5%,几乎完美
 *   - 图1 Y=135+351=486,在书框 Y=88-872 范围内(余量 386dp;H=391→351 用户真机调整)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 407.png(已复制为 res/drawable-nodpi/img_volume5part15_image_407.png)
 */
@Composable
fun Volume5Part15Screen(
    onBack: () -> Unit = {},
    onOpenGunlun12: () -> Unit = {},
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
            // 标题"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192,点击跳 Gunlun12(卷末闭环,仿 Vol-4-14 → Gunlun8)。
            Text(
                text = "死记硬背不可行",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenGunlun12),
            )

            // 图1(image 407.png,X=18, Y=135, W=355, H=351)— 在书框之上、上部(H=391→351 用户真机调整)。
            // **本屏无图 2**(沿用 Vol-5-9 单图先例,如有出入随时改回 5 层)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 351.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part15_image_407),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}