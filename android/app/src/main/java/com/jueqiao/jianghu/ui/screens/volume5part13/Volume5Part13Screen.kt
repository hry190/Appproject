package com.jueqiao.jianghu.ui.screens.volume5part13

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
 * 第五卷-13 页 — 第五卷-12 → 点击"偏差的数据"标题跳转目标。Vol-5-13 标题点击跳 Vol-5-14。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-5-12(255)→ Vol-5-13(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192 沿用 6-8 字规约
 *   - 图1(image 402.png,X=18, Y=135, W=355, H=311)— 上部
 *   - 图2(image 403.png,X=18, Y=478, W=355, H=321)— 中下部(沿用 Vol-5-7~5-12 真机调整过的 Y=478)
 *
 * 坐标说明:
 *   - image 402 实测 1047×918(横向矩形,比率 1.141);用户给 W=355 H=311 渲染比 1.141 与原图差 0%,**完全匹配**
 *   - image 403 实测 1062×804(近正方形,比率 1.321);用户给 W=355 H=321 渲染比 1.106 与原图差 **16.3%**,**严重失真**(image 被竖向拉长 16%)— ⚠️ 用户可考虑:换图 / 改 H=268(自然高度 355/1.321)/ 接受失真。已记录备查。
 *   - 图2 Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 402.png(已复制为 res/drawable-nodpi/img_volume5part13_image_402.png)
 *   - 图2:D:\图\image 403.png(已复制为 res/drawable-nodpi/img_volume5part13_image_403.png)
 */
@Composable
fun Volume5Part13Screen(
    onBack: () -> Unit = {},
    onOpenVolume5Part14: () -> Unit = {},
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
            // 标题"死记硬背不可行"(字号 24,bold,黑色,X=110, Y=67, W=192, H=32)— 7 字 W=192,点击跳第五卷-14。
            Text(
                text = "死记硬背不可行",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 192.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume5Part14),
            )

            // 图1(image 402.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 306.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part13_image_402),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 403.png,X=18, Y=478, W=355, H=321)— 在书框之上、中下部。
            // Y=478+321=799,在书框 Y=88-872 范围内(余量 73dp)。
            // ⚠️ 该图原图近正方形(比率 1.321),按 H=321 渲染会有 16.3% 竖向拉伸,用户真机可观察决定是否调整。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 268.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume5part13_image_403),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}