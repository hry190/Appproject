package com.jueqiao.jianghu.ui.screens.volume5part14

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
 * 第五卷-14 页 — 第五卷-13 → 点击"死记硬背不可行"标题跳转目标。Vol-5-14 标题点击跳 Vol-5-15。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-12 指定"复制第一卷-2";Vol-5-13(255)→ Vol-5-14(256) 恢复交替)
 *   - 标题文本"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192 沿用 6-8 字规约(与 Vol-5-13 同款,跨页同标题)
 *   - 图1(image 405.png,X=18, Y=135, **W=355, H=281**)— 上部(H=311→281 用户 2026-09-12 真机调整)
 *   - 图2(image 406.png,X=18, **Y=425, W=355, H=358**)— 中下部(Y=478→425 / W=365→355 / H=378→358 用户 2026-09-12 真机调整)
 *
 * 坐标说明:
 *   - image 405 实测 1070×846(横向矩形,比率 1.265);**实际 H=281**(用户 2026-09-12 真机从 H=311 调到 281),渲染比 1.264 与原图差 **0.1%**,几乎完美
 *   - image 406 实测 1083×1068(近正方形,比率 1.014);**实际 Y=425 W=355 H=358**(用户 2026-09-12 真机调过),渲染比 0.992 与原图差 2.2%,几乎完美
 *   - 图2 Y=425+358=783,在书框 Y=88-872 范围内(余量 89dp;Y=478→425 H=378→358 用户真机调整)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 405.png(已复制为 res/drawable-nodpi/img_volume5part14_image_405.png)
 *   - 图2:D:\图\image 406.png(已复制为 res/drawable-nodpi/img_volume5part14_image_406.png)
 */
@Composable
fun Volume5Part14Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part15: () -> Unit = {},
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
            // 标题"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192,点击跳第五卷-15。
            Text(
                text = "死记硬背不可行",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part15),
            )

            // 图1(image 405.png,X=18, Y=135, W=355, H=281)— 在书框之上、上部(H=311→281 用户真机调整)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 281.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part14_image_405),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 406.png,X=18, Y=425, W=355, H=358)— 在书框之上、中下部(Y=478→425 / W=365→355 / H=378→358 用户真机调整)。
            // Y=425+358=783,在书框 Y=88-872 范围内(余量 89dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 425.dp)
                    .size(width = 355.dp, height = 358.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part14_image_406),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}