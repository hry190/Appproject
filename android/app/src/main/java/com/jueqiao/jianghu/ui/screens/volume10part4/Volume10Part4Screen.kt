package com.jueqiao.jianghu.ui.screens.volume10part4

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
 * 第十卷-4 页 — 第十卷-3 → 点击" 少取才安全"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-10-3(255)→ Vol-10-4(255) **连续两屏异常**,字面"复制第一卷-1"=255 优先于交替模式)
 *   - 标题文本"偏见从何而来"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(沿用 Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-9-1「长句先切成符」同款 5 字真机基线)— **首次 Vol-10 系列 5 字标题**
 *   - 图1(image 473.png,X=18, Y=135, **W=355, H=321**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.104 自动 fit W=355 H=round(355/1.104)=321)
 *   - 图2(image 43.png,X=18, Y=478, **W=355, H=340**)— 中下部(应用新规则:横图 ratio 1.044 自动 fit W=355 H=round(355/1.044)=340)
 *
 * 坐标说明:
 *   - image 473 实测 1047×948(横图,比率 1.104);自然 W=355 H=321,渲染比 1.104 与原图差 0%,完全匹配
 *   - image 43 实测 1068×1023(横图,比率 1.044);自然 W=355 H=340,渲染比 1.044 与原图差 0%,完全匹配
 *   - 图1 Y=135+321=456,图2 Y=478+340=818(均在书框 Y=88-872 范围内,余量 416/54dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 473.png(已复制为 res/drawable-nodpi/img_volume10part4_image_473.png)
 *   - 图2:设计稿 image 43.png(已复制为 res/drawable-nodpi/img_volume10part4_image_43.png)
 *
 * 本屏跳转目标:点击"偏见从何而来"标题 → Vol-10-5(创建于 2026-09-13,本屏兑现 §66.3 KDoc 承诺,回填 onOpenVolume10Part5)。
 */
@Composable
fun Volume10Part4Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part5: () -> Unit = {},
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
            // 标题"偏见从何而来"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约)。
            // 点击跳 Vol-10-5(创建于 2026-09-13)。
            Text(
                text = "偏见从何而来",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part5),
            )

            // 图1(image 473.png,X=18, Y=135, W=355, H=321)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 321.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part4_image_473),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 43.png,X=18, Y=478, W=355, H=340)— 在书框之上、中下部(横图)。
            // Y=478+340=818,在书框 Y=88-872 范围内(余量 54dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 340.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part4_image_43),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}