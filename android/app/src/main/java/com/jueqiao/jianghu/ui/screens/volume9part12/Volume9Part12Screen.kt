package com.jueqiao.jianghu.ui.screens.volume9part12

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
 * 第九卷-12 页 — 第九卷-11 → 点击"上下文决定答法"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-11(256)→ Vol-9-12(255) 奇偶交替恢复,非异常)
 *   - 标题文本"上下文决定答法"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **7 字** W=192 沿用 7 字规约(Vol-5-13/14/15「死记硬背不可行」7 字 W=192 真机测过宽度;与 Vol-9-10/9-11 同款 7 字标题)
 *   - 图1(image 513.png,X=18, Y=135, **W=355, H=298**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.191 自动 fit W=355 H=round(355/1.191)=298)
 *   - 图2(image 514.png,X=18, Y=478, **W=355, H=345**)— 中下部(应用新规则:近正方形 ratio 1.029 自动 fit W=355 H=round(355/1.029)=345)
 *
 * 坐标说明:
 *   - image 513 实测 1047×879(横图,比率 1.191);自然 W=355 H=298,渲染比 1.191 与原图差 0%,完全匹配
 *   - image 514 实测 1074×1044(近正方形,比率 1.029);自然 W=355 H=345,渲染比 1.029 与原图差 0%,完全匹配
 *   - 图1 Y=135+298=433,图2 Y=478+345=823(均在书框 Y=88-872 范围内,余量 439/49dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 513.png(已复制为 res/drawable-nodpi/img_volume9part12_image_513.png)
 *   - 图2:设计稿 image 514.png(已复制为 res/drawable-nodpi/img_volume9part12_image_514.png)
 *
 * 本屏跳转目标:点击"上下文决定答法"标题 → Vol-9-13(创建于 2026-09-13,本屏兑现 §56.3 KDoc 承诺,回填 onOpenVolume9Part13)。
 */
@Composable
fun Volume9Part12Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part13: () -> Unit = {},
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
            // 标题"上下文决定答法"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 7 字 W=192(沿用 7 字规约,与 Vol-9-10/9-11 同款)。
            // 点击跳 Vol-9-13(创建于 2026-09-13)。
            Text(
                text = "上下文决定答法",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume9Part13),
            )

            // 图1(image 513.png,X=18, Y=135, W=355, H=298)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 298.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part12_image_513),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 514.png,X=18, Y=478, W=355, H=345)— 在书框之上、中下部(近正方形)。
            // Y=478+345=823,在书框 Y=88-872 范围内(余量 49dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 458.dp)
                    .size(width = 355.dp, height = 345.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part12_image_514),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}