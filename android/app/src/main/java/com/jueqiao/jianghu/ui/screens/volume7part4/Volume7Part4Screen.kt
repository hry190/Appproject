package com.jueqiao.jianghu.ui.screens.volume7part4

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
 * 第七卷-4 页 — 第七卷-3 → 点击"皮影戏之权重从错误中学"标题跳转目标。**Vol-7-4 标题点击跳 Vol-7-5**。
 *
 * 布局(z-order 由下到上,**6 层,含 3 张图**—Vol-1~7 系列首次):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-7-3(255)→ Vol-7-4(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"皮影戏之权重从错误中学"(字号 24,bold,黑色,X=110, Y=67, **W=360, H=32**)— **11 字**(全中文无标点)W=360 沿用估算 ~33/字宽度(11 字无独立规约,KDoc 标注异常,真机可微调)
 *   - 图1(image 442.png,X=18, Y=135, **W=355, H=211**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.684)=211)
 *   - 图2(image 443.png,X=18, Y=381, **W=355, H=189**)— 中部(应用新规则:横图 W=355 H=round(355/1.874)=189)
 *   - 图3(image 444.png,X=18, Y=606, **W=355, H=180**)— 下部(应用新规则:横图 W=355 H=round(355/1.967)=180,书框底 872 - 606 - 180 = 86dp 余量)
 *
 * 坐标说明:
 *   - image 442 实测 1056×627(横向矩形,比率 1.684);自然 W=355 H=211,渲染比 1.682 与原图差 0.10%,几乎完美
 *   - image 443 实测 1068×570(横向矩形,比率 1.874);自然 W=355 H=189,渲染比 1.878 与原图差 0.25%,几乎完美
 *   - image 444 实测 1068×543(横向矩形,比率 1.967);自然 W=355 H=180,渲染比 1.972 与原图差 0.27%,几乎完美
 *   - 图1 Y=135+211=346,图2 Y=381+189=570,图3 Y=606+180=786(均在书框 Y=88-872 内)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 442.png(已复制为 res/drawable-nodpi/img_volume7part4_image_442.png)
 *   - 图2:设计稿 image 443.png(已复制为 res/drawable-nodpi/img_volume7part4_image_443.png)
 *   - 图3:设计稿 image 444.png(已复制为 res/drawable-nodpi/img_volume7part4_image_444.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-7-5 创建时按历次约定回填 onOpenVolume7Part5)。
 */
@Composable
fun Volume7Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume7Part5: () -> Unit = {},
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
            // 标题"皮影戏之权重从错误中学"(字号 24,bold,黑色,X=110, Y=67, W=360, H=32)— 11 字 W=360(沿用估算 ~33/字宽度,无独立规约),点击跳第七卷-5。
            Text(
                text = "皮影戏之权重从错误中学",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 360.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume7Part5),
            )

            // 图1(image 442.png,X=18, Y=135, W=355, H=211)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 211.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part4_image_442),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 443.png,X=18, Y=381, W=355, H=189)— 在书框之上、中部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 381.dp)
                    .size(width = 355.dp, height = 189.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part4_image_443),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 444.png,X=18, Y=606, W=355, H=180)— 在书框之上、下部(书框底 872 余量 86dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 606.dp)
                    .size(width = 355.dp, height = 180.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part4_image_444),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}