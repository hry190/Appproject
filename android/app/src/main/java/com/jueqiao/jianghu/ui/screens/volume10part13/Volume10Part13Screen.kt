package com.jueqiao.jianghu.ui.screens.volume10part13

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
 * 第十卷-13 页 — 第十卷-12 → 点击"人作主，机助力"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-10-12(255)→ Vol-10-13(256) 交替恢复,非异常)
 *   - 标题文本"人作主，机助力"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字 + 1 中文逗号 = 7 字符** W=192 沿用 7 字规约(中文逗号算字符位,与 Vol-5-7「训练，检验，测试」8 字含 2 个中文逗号 W=192 同款;真机基线 Vol-5-13/14/15「死记硬背不可行」7 字 W=192)— 与 Vol-10-12 同款 7 字符标题,跨页同标题叙述
 *   - 图1(image 486.png,X=18, Y=135, **W=355, H=252**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.411 自动 fit W=355 H=round(355/1.411)=252)
 *   - 图2(image 487.png,X=18, Y=478, **W=355, H=307**)— 中下部(应用新规则:横图 ratio 1.157 自动 fit W=355 H=round(355/1.157)=307)
 *
 * 坐标说明:
 *   - image 486 实测 1020×723(横图,比率 1.411);自然 W=355 H=252,渲染比 1.409 与原图差 0.14%,几乎完美
 *   - image 487 实测 1076×930(横图,比率 1.157);自然 W=355 H=307,渲染比 1.156 与原图差 0.09%,几乎完美
 *   - 图1 Y=135+252=387,图2 Y=478+307=785(均在书框 Y=88-872 范围内,余量 485/87dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 486.png(已复制为 res/drawable-nodpi/img_volume10part13_image_486.png)
 *   - 图2:设计稿 image 487.png(已复制为 res/drawable-nodpi/img_volume10part13_image_487.png)
 *
 * 本屏跳转目标:点击"人作主，机助力"标题 → Vol-10-14(创建于 2026-09-13,本屏兑现 §75.3 KDoc 承诺,回填 onOpenVolume10Part14)。
 */
@Composable
fun Volume10Part13Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part14: () -> Unit = {},
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
            // 标题"人作主，机助力"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 7 字符 (6 字 + 1 中文逗号) W=192(沿用 7 字规约,与 Vol-10-12 同款跨页同标题叙述)。
            // 点击跳 Vol-10-14(创建于 2026-09-13)。
            Text(
                text = "人作主，机助力",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part14),
            )

            // 图1(image 486.png,X=18, Y=135, W=355, H=252)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 252.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part13_image_486),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 487.png,X=18, Y=478, W=355, H=307)— 在书框之上、中下部(横图)。
            // Y=478+307=785,在书框 Y=88-872 范围内(余量 87dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 307.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part13_image_487),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}