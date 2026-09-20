package com.jueqiao.jianghu.ui.screens.volume9part10

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
 * 第九卷-10 页 — 第九卷-9 → 点击"大模型核心"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-9(255)→ Vol-9-10(255) **连续两屏异常**,字面"复制第一卷-1"=255 优先于交替模式)
 *   - 标题文本"上下文决定答法"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **7 字** W=192 沿用 7 字规约(Vol-5-13/14/15「死记硬背不可行」7 字 W=192 真机测过宽度)
 *   - 图1(image 509.png,X=18, Y=135, **W=355, H=332**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.070 自动 fit W=355 H=round(355/1.070)=332)
 *   - 图2(image 510.png,X=18, Y=478, **W=355, H=328**)— 中下部(应用新规则:横图 ratio 1.082 自动 fit W=355 H=round(355/1.082)=328)
 *
 * 坐标说明:
 *   - image 509 实测 1008×942(横图,比率 1.070);自然 W=355 H=332,渲染比 1.069 与原图差 0.06%,几乎完美
 *   - image 510 实测 1035×957(横图,比率 1.082);自然 W=355 H=328,渲染比 1.082 与原图差 0%,完全匹配
 *   - 图1 Y=135+332=467,图2 Y=478+328=806(均在书框 Y=88-872 范围内,余量 405/66dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 509.png(已复制为 res/drawable-nodpi/img_volume9part10_image_509.png)
 *   - 图2:设计稿 image 510.png(已复制为 res/drawable-nodpi/img_volume9part10_image_510.png)
 *
 * 本屏跳转目标:点击"上下文决定答法"标题 → Vol-9-11(创建于 2026-09-13,本屏兑现 §54.3 KDoc 承诺,回填 onOpenVolume9Part11)。
 */
@Composable
fun Volume9Part10Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part11: () -> Unit = {},
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
            // 标题"上下文决定答法"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 7 字 W=192(沿用 7 字规约)。
            // 点击跳 Vol-9-11(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume9Part11),
            )

            // 图1(image 509.png,X=18, Y=135, W=355, H=332)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 332.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part10_image_509),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 510.png,X=18, Y=478, W=355, H=328)— 在书框之上、中下部(横图)。
            // Y=478+328=806,在书框 Y=88-872 范围内(余量 66dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 328.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part10_image_510),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}