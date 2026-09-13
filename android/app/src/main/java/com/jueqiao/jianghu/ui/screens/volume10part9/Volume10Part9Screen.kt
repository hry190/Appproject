package com.jueqiao.jianghu.ui.screens.volume10part9

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
 * 第十卷-9 页 — 第十卷-8 → 点击"借招也要署名"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-10-8(256)→ Vol-10-9(255) 交替,非异常)
 *   - 标题文本"借招也要署名"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **6 字** W=192 沿用 6-8 字规约(Vol-9-1「长句先切成符」、Vol-9-4/5/6「语义也有远近」、Vol-9-13/14/15「会说不等于知道」同款真机基线;与 Vol-10-7/10-8 同款 6 字标题,跨页同标题叙述)
 *   - 图1(image 479.png,X=18, Y=135, **W=355, H=320**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.108 自动 fit W=355 H=round(355/1.108)=320)
 *   - 图2(image 9.png,X=18, Y=478, **W=355, H=341**)— 中下部(应用新规则:横图 ratio 1.042 自动 fit W=355 H=round(355/1.042)=341)— **注意编号小,易与 image 90/99 系列混淆;drawable 名独立为 img_volume10part9_image_9 避免冲突**
 *
 * 坐标说明:
 *   - image 479 实测 1047×945(横图,比率 1.108);自然 W=355 H=320,渲染比 1.109 与原图差 0.09%,几乎完美
 *   - image 9 实测 1067×1024(横图,比率 1.042);自然 W=355 H=341,渲染比 1.042 与原图差 0%,完全匹配
 *   - 图1 Y=135+320=455,图2 Y=478+341=819(均在书框 Y=88-872 范围内,余量 417/53dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 479.png(已复制为 res/drawable-nodpi/img_volume10part9_image_479.png)
 *   - 图2:D:\图\image 9.png(已复制为 res/drawable-nodpi/img_volume10part9_image_9.png)
 *
 * 本屏跳转目标:点击"借招也要署名"标题 → Vol-10-10(创建于 2026-09-13,本屏兑现 §71.3 KDoc 承诺,回填 onOpenVolume10Part10)。
 */
@Composable
fun Volume10Part9Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part10: () -> Unit = {},
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
            // 标题"借招也要署名"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 6 字 W=192(沿用 6-8 字规约,与 Vol-10-7/10-8 同款跨页同标题叙述)。
            // 点击跳 Vol-10-10(创建于 2026-09-13)。
            Text(
                text = "借招也要署名",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part10),
            )

            // 图1(image 479.png,X=18, Y=135, W=355, H=320)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 320.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part9_image_479),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 9.png,X=18, Y=478, W=355, H=341)— 在书框之上、中下部(横图)。
            // Y=478+341=819,在书框 Y=88-872 范围内(余量 53dp);编号小易混淆,drawable 名独立。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 341.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part9_image_9),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}