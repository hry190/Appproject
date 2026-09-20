package com.jueqiao.jianghu.ui.screens.volume10part3

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
 * 第十卷-3 页 — 第十卷-2 → 点击"少取才安全"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-10-2(256)→ Vol-10-3(255) 交替恢复)
 *   - 标题文本" 少取才安全"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **4 字 + 1 前导空格** W=213 沿用 5 字规约(沿用 Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-9-1「长句先切成符」同款 5 字区域真机基线;**用户字面前导空格按字面保留,1 个空格** — 沿用 §50.3 Vol-9-5 模式,KDoc 留痕待用户指令决定是否删除 — **与 §64 Vol-10-1/10-2 修订后无空格版"少取才安全"不一致,本屏再次按字面带空格创建**)
 *   - 图1(image 472.png,X=18, Y=135, **W=355, H=310**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.147 自动 fit W=355 H=round(355/1.147)=310)
 *   - 图2(image 72.png,X=18, Y=478, **W=355, H=305**)— 中下部(应用新规则:横图 ratio 1.162 自动 fit W=355 H=round(355/1.162)=305)
 *
 * 坐标说明:
 *   - image 472 实测 1046×912(横图,比率 1.147);自然 W=355 H=310,渲染比 1.145 与原图差 0.17%,几乎完美
 *   - image 72 实测 1067×918(横图,比率 1.162);自然 W=355 H=305,渲染比 1.164 与原图差 0.17%,几乎完美
 *   - 图1 Y=135+310=445,图2 Y=478+305=783(均在书框 Y=88-872 范围内,余量 427/89dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 472.png(已复制为 res/drawable-nodpi/img_volume10part3_image_472.png)
 *   - 图2:设计稿 image 72.png(已复制为 res/drawable-nodpi/img_volume10part3_image_72.png)
 *
 * 本屏跳转目标:点击" 少取才安全"标题 → Vol-10-4(创建于 2026-09-13,本屏兑现 §65.4 KDoc 承诺,回填 onOpenVolume10Part4)。
 */
@Composable
fun Volume10Part3Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part4: () -> Unit = {},
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
            // 标题" 少取才安全"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 4 字 + 1 前导空格 W=213(沿用 5 字规约)。
            // 用户字面前导空格按字面保留 1 个空格 — 沿用 §50.3 Vol-9-5 模式,KDoc 留痕待用户指令决定是否删除。
            // 点击跳 Vol-10-4(创建于 2026-09-13)。
            Text(
                text = " 少取才安全",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part4),
            )

            // 图1(image 472.png,X=18, Y=135, W=355, H=310)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 310.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part3_image_472),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 72.png,X=18, Y=478, W=355, H=305)— 在书框之上、中下部(横图)。
            // Y=478+305=783,在书框 Y=88-872 范围内(余量 89dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 305.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part3_image_72),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}