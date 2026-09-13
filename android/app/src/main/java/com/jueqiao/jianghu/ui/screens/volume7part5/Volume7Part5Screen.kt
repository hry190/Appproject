package com.jueqiao.jianghu.ui.screens.volume7part5

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
 * 第七卷-5 页 — 第七卷-4 → 点击"皮影戏之权重从错误中学"标题跳转目标。**Vol-7-5 标题点击跳 Vol-7-6**。
 *
 * 布局(z-order 由下到上,**6 层,含 3 张图**):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-12 指定"复制第一卷-2";Vol-7-4(255)→ Vol-7-5(256) 恢复交替)
 *   - 标题文本"皮影戏之权重从错误中学"(字号 24,bold,黑色,X=110, Y=67, **W=360, H=32**)**)** **11 字**(全中文无标点)W=360 沿用估算 ~33/字宽度(与 Vol-7-4 同款,跨页同标题)
 *   - 图1(image 445.png,X=18, Y=135, **W=355, H=202**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.754)=202)
 *   - 图2(image 446.png,X=18, Y=381, **W=355, H=172**)— 中部(应用新规则:横图 W=355 H=round(355/2.063)=172)
 *   - 图3(image 447.png,X=18, Y=606, **W=355, H=188**)— 下部(应用新规则:横图 W=355 H=round(355/1.890)=188,书框底 872 - 606 - 188 = 78dp 余量)
 *
 * 坐标说明:
 *   - image 445 实测 1068×609(横向矩形,比率 1.754);自然 W=355 H=202,渲染比 1.757 与原图差 0.21%,几乎完美
 *   - image 446 实测 1077×522(横向矩形,比率 2.063);自然 W=355 H=172,渲染比 2.064 与原图差 0.04%,几乎完美
 *   - image 447 实测 1083×573(横向矩形,比率 1.890);自然 W=355 H=188,渲染比 1.888 与原图差 0.09%,几乎完美
 *   - 图1 Y=135+202=337,图2 Y=381+172=553,图3 Y=606+188=794(均在书框 Y=88-872 内)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 445.png(已复制为 res/drawable-nodpi/img_volume7part5_image_445.png)
 *   - 图2:D:\图\image 446.png(已复制为 res/drawable-nodpi/img_volume7part5_image_446.png)
 *   - 图3:D:\图\image 447.png(已复制为 res/drawable-nodpi/img_volume7part5_image_447.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-7-6 创建时按历次约定回填 onOpenVolume7Part6)。
 */
@Composable
fun Volume7Part5Screen(
    onBack: () -> Unit = {},
    onOpenVolume7Part6: () -> Unit = {},
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
            // 标题"皮影戏之权重从错误中学"(字号 24,bold,黑色,X=110, Y=67, W=360, H=32)— 11 字 W=360(沿用估算 ~33/字宽度,无独立规约),点击跳第七卷-6。
            Text(
                text = "皮影戏之权重从错误中学",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 360.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume7Part6),
            )

            // 图1(image 445.png,X=18, Y=135, W=355, H=202)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 202.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part5_image_445),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 446.png,X=18, Y=381, W=355, H=172)— 在书框之上、中部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 381.dp)
                    .size(width = 355.dp, height = 172.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part5_image_446),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 447.png,X=18, Y=606, W=355, H=188)— 在书框之上、下部(书框底 872 余量 78dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 606.dp)
                    .size(width = 355.dp, height = 188.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part5_image_447),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}