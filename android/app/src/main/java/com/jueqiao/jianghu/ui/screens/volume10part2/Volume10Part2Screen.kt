package com.jueqiao.jianghu.ui.screens.volume10part2

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
 * 第十卷-2 页 — 第十卷-1 → 点击"少取才安全"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-10-1(255)→ Vol-10-2(256) 奇偶交替恢复,非异常)
 *   - 标题文本"少取才安全"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **4 字** W=213 沿用 5 字规约(沿用 Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-6-4/6-5「问问近邻」同款真机基线;与 Vol-10-1 同款 4 字标题)— **标题变更史**:初版 6 字"会说不等于知道" → 修订 4 字"少取才安全" 2026-09-13 §64 用户指令
 *   - 图1(image 471.png,X=18, Y=135, **W=355, H=300**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.182 自动 fit W=355 H=round(355/1.182)=300)
 *   - 图2(image 41.png,X=18, Y=478, **W=355, H=282**)— 中下部(应用新规则:横图 ratio 1.261 自动 fit W=355 H=round(355/1.261)=282)
 *
 * 坐标说明:
 *   - image 471 实测 1046×885(横图,比率 1.182);自然 W=355 H=300,渲染比 1.183 与原图差 0.08%,几乎完美
 *   - image 41 实测 1059×840(横图,比率 1.261);自然 W=355 H=282,渲染比 1.259 与原图差 0.16%,几乎完美
 *   - 图1 Y=135+300=435,图2 Y=478+282=760(均在书框 Y=88-872 范围内,余量 437/112dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 471.png(已复制为 res/drawable-nodpi/img_volume10part2_image_471.png)
 *   - 图2:设计稿 image 41.png(已复制为 res/drawable-nodpi/img_volume10part2_image_41.png)
 *
 * 本屏跳转目标:点击"少取才安全"标题 → Vol-10-3(创建于 2026-09-13,本屏兑现 §63.3 KDoc 承诺,回填 onOpenVolume10Part3)。
 */
@Composable
fun Volume10Part2Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part3: () -> Unit = {},
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
            // 标题"少取才安全"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 4 字 W=213(沿用 5 字规约,与 Vol-10-1 同款;**标题变更史**:初版 6 字"会说不等于知道" → 修订 4 字"少取才安全" 2026-09-13 §64)。
            // 点击跳 Vol-10-3(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume10Part3),
            )

            // 图1(image 471.png,X=18, Y=135, W=355, H=300)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 300.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part2_image_471),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 41.png,X=18, Y=478, W=355, H=282)— 在书框之上、中下部(横图)。
            // Y=478+282=760,在书框 Y=88-872 范围内(余量 112dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 282.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part2_image_41),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}