package com.jueqiao.jianghu.ui.screens.volume9part8

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
 * 第九卷-8 页 — 第九卷-7 → 点击"大模型核心"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-9-7(255)→ Vol-9-8(256) 奇偶交替恢复,非异常)
 *   - 标题文本"大模型核心"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」同款真机测过宽度;与 Vol-9-7 同款 5 字标题)
 *   - 图1(image 505.png,X=18, Y=135, **W=355, H=348**)— 上部(应用新规则 fit-to-natural-bounds:近正方形 ratio 1.020 自动 fit W=355 H=round(355/1.020)=348)
 *   - 图2(image 506.png,X=18, Y=478, **W=355, H=218**)— 中下部(应用新规则:扁横图 ratio 1.628 自动 fit W=355 H=round(355/1.628)=218)
 *
 * 坐标说明:
 *   - image 505 实测 1068×1047(近正方形,比率 1.020);自然 W=355 H=348,渲染比 1.020 与原图差 0%,完全匹配
 *   - image 506 实测 1089×669(扁横图,比率 1.628);自然 W=355 H=218,渲染比 1.628 与原图差 0%,完全匹配
 *   - 图1 Y=135+348=483,图2 Y=478+218=696(均在书框 Y=88-872 范围内,余量 389/176dp)— 图2 是扁横图所以上下留白较多(176dp 余量)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:D:\图\image 505.png(已复制为 res/drawable-nodpi/img_volume9part8_image_505.png)
 *   - 图2:D:\图\image 506.png(已复制为 res/drawable-nodpi/img_volume9part8_image_506.png)
 *
 * 本屏跳转目标:点击"大模型核心"标题 → Vol-9-9(创建于 2026-09-13,本屏兑现 §52.3 KDoc 承诺,回填 onOpenVolume9Part9)。
 */
@Composable
fun Volume9Part8Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part9: () -> Unit = {},
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
            // 标题"大模型核心"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约)。
            // 点击跳 Vol-9-9(创建于 2026-09-13)。
            Text(
                text = "大模型核心",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume9Part9),
            )

            // 图1(image 505.png,X=18, Y=135, W=355, H=348)— 在书框之上、上部(近正方形横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 348.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part8_image_505),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 506.png,X=18, Y=478, W=355, H=218)— 在书框之上、中下部(扁横图)。
            // Y=478+218=696,在书框 Y=88-872 范围内(余量 176dp);扁横图自然 H 小,留白多。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 508.dp)
                    .size(width = 355.dp, height = 218.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part8_image_506),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}