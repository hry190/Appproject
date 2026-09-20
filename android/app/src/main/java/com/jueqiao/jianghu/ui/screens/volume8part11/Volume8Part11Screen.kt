package com.jueqiao.jianghu.ui.screens.volume8part11

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
 * 第八卷-11 页 — 第八卷-10 → 点击"皮影戏之奖励塑形"标题跳转目标。
 *
 * 布局(z-order 由下到上,**4 层,单图布局**—Vol-8 第二个单图屏,V1=Vol-8-9):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-8-10(255)→ Vol-8-11(256) 恢复交替)
 *   - 标题文本"皮影戏之奖励塑形"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **8 字**(全中文无标点)W=192 沿用 6-8 字规约(与 Vol-8-10 同款 8 字规约,跨页同标题)
 *   - 图1(image 534.png,X=18, Y=135, **W=355, H=168**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/2.114)=168)
 *
 * **本屏无图 2**(用户字面 2026-09-13 只指定 image 534;沿用 Vol-5-9/6-6/6-15/8-9 单图先例直接采用 4 层 z-order,如有出入随时改回 5 层)
 *
 * 坐标说明:
 *   - image 534 实测 1053×498(极扁横图,比率 2.114);自然 W=355 H=168,渲染比 2.114 与原图差 0%,完全匹配
 *   - 图1 Y=135+168=303,在书框 Y=88-872 范围内(余量 569dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 534.png(已复制为 res/drawable-nodpi/img_volume8part11_image_534.png)
 *
 * 点击跳 Vol-8-12(Vol-8-12 创建时回填 callback 与 .clickable)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-10标题" → 实际意图"第八卷-10 的标题"(缺少" 的"),无歧义
 */
@Composable
fun Volume8Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part12: () -> Unit = {},
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
            // 标题"皮影戏之奖励塑形"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 8 字 W=192(沿用 6-8 字规约,与 Vol-8-10 同款)。
            // 标题"皮影戏之奖励塑形"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 点击跳 Vol-8-12。
            Text(
                text = "皮影戏之奖励塑形",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part12),
            )

            // 图1(image 534.png,X=18, Y=135, W=355, H=168)— 在书框之上、上部(单图屏)。
            // Y=135+168=303,在书框 Y=88-872 范围内(余量 569dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 168.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part11_image_534),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}