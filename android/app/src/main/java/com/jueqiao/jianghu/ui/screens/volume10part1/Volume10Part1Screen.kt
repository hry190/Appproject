package com.jueqiao.jianghu.ui.screens.volume10part1

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
 * 第十卷-1 页 — 滚轮13 → 点击"已解锁秘籍9"图像跳转目标(新卷首屏)。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";第十卷首屏)
 *   - 标题文本"少取才安全"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **4 字** W=213 沿用 5 字规约(4 字按 5 个字符宽度估算,沿用 Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-6-4/6-5「问问近邻」、Vol-9-1「长句先切成符」同款真机基线;**标题变更史**:初版 4 字 + 1 前导空格" 少取才安全" → 修订 4 字无前导空格"少取才安全" 2026-09-13 §64 用户指令)
 *   - 图1(image 470.png,X=18, Y=135, **W=355, H=328**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.084)=328)
 *   - 图2(image 70.png,X=18, Y=478, **W=355, H=340**)— 中下部(应用新规则:横图 W=355 H=round(355/1.044)=340)
 *
 * 坐标说明:
 *   - image 470 实测 1047×966(横图,比率 1.084);自然 W=355 H=328,渲染比 1.083 与原图差 0.09%,几乎完美
 *   - image 70 实测 1068×1023(横图,比率 1.044);自然 W=355 H=340,渲染比 1.044 与原图差 0%,完全匹配
 *   - 图1 Y=135+328=463,图2 Y=478+340=818(均在书框 Y=88-872 范围内,余量 409/54dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 470.png(已复制为 res/drawable-nodpi/img_volume10part1_image_470.png)
 *   - 图2:D:\图\image 70.png(已复制为 res/drawable-nodpi/img_volume10part1_image_70.png)— **注意编号小,易与 image 7/70x 系列混淆;drawable 名独立为 img_volume10part1_image_70 避免冲突**
 *
 * 本屏跳转目标:点击"少取才安全"标题 → Vol-10-2(创建于 2026-09-13,本屏兑现 §62.4 KDoc 承诺,回填 onOpenVolume10Part2)。
 */
@Composable
fun Volume10Part1Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part2: () -> Unit = {},
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
            // 标题"少取才安全"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 4 字 W=213(沿用 5 字规约;**标题变更史**:初版 4 字 + 1 前导空格" 少取才安全" → 修订 4 字无前导空格"少取才安全" 2026-09-13 §64 用户指令删除前导空格)。
            // 点击跳 Vol-10-2(创建于 2026-09-13)。
            Text(
                text = "少取才安全",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part2),
            )

            // 图1(image 470.png,X=18, Y=135, W=355, H=328)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 117.dp)
                    .size(width = 355.dp, height = 328.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part1_image_470),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 70.png,X=18, Y=478, W=355, H=340)— 在书框之上、中下部(横图)。
            // Y=478+340=818,在书框 Y=88-872 范围内(余量 54dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 458.dp)
                    .size(width = 355.dp, height = 340.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part1_image_70),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}