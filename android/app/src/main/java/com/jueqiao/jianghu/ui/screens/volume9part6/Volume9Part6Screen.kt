package com.jueqiao.jianghu.ui.screens.volume9part6

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
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
 * 第九卷-6 页 — 第九卷-5 → 点击"语义也有远近"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-5(256)→ Vol-9-6(255) 恢复交替)
 *   - 标题文本"语义也有远近"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字** W=192 沿用 6-8 字规约(与 Vol-9-5 同款 6 字标题;无前导空格)
 *   - 图1(image 500.png,X=18, Y=135, **W=355, H=324**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.094)=324)
 *   - 图2(image 502.png,X=18, Y=478, **W=355, H=354**)— 中下部(应用新规则:近正方形 ratio 1.002,自动 fit H=354 W=355)
 *
 * 坐标说明:
 *   - image 500 实测 1047×957(横图,比率 1.094);自然 W=355 H=324,渲染比 1.096 与原图差 0.15%,几乎完美
 *   - image 502 实测 1067×1065(近正方形,比率 1.002);自然 W=355 H=354,渲染比 1.003 与原图差 0.09%,几乎完美
 *   - 图1 Y=135+324=459,图 2 Y=478+354=832(均在书框 Y=88-872 范围内,余量 413/40dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 500.png(已复制为 res/drawable-nodpi/img_volume9part6_image_500.png)
 *   - 图2:D:\图\image 502.png(已复制为 res/drawable-nodpi/img_volume9part6_image_502.png)— **用户字面跳过 image 501(直接 500→502)**,可能 image 501 缺失或为笔误;沿用字面
 *
 * 本屏跳转目标:点击"语义也有远近"标题 → Vol-9-7(创建于 2026-09-13,本屏兑现 §50.6 KDoc 承诺,回填 onOpenVolume9Part7)。
 */
@Composable
fun Volume9Part6Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part7: () -> Unit = {},
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
            // 标题"语义也有远近"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 W=192(沿用 6-8 字规约,与 Vol-9-5 同款)。
            // 点击跳 Vol-9-7(创建于 2026-09-13)。
            Text(
                text = "语义也有远近",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume9Part7),
            )

            // 图1(image 500.png,X=18, Y=130, W=355, H=324)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 130.dp)
                    .size(width = 355.dp, height = 324.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part6_image_500),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 502.png,X=18, Y=478, W=355, H=354)— 在书框之上、中下部(近正方形)。
            // Y=478+354=832,在书框 Y=88-872 范围内(余量 40dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 470.dp)
                    .size(width = 355.dp, height = 334.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part6_image_502),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}