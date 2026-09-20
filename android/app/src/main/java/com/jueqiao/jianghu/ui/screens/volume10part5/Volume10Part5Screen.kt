package com.jueqiao.jianghu.ui.screens.volume10part5

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
 * 第十卷-5 页 — 第十卷-4 → 点击"偏见从何而来"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-10-4(255)→ Vol-10-5(256) 奇偶交替恢复,非异常)
 *   - 标题文本"偏见从何而来"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-9-1「长句先切成符」同款真机基线;与 Vol-10-4 同款 5 字标题,跨页同标题叙述)
 *   - 图1(image 475.png,X=18, Y=135, **W=355, H=340**)— 上部(应用新规则 fit-to-natural-bounds:横图 ratio 1.045 自动 fit W=355 H=round(355/1.045)=340)
 *   - 图2(image 474.png,X=18, Y=478, **W=355, H=314**)— 中下部(应用新规则:横图 ratio 1.132 自动 fit W=355 H=round(355/1.132)=314)
 *
 * 坐标说明:
 *   - image 475 实测 1047×1002(横图,比率 1.045);自然 W=355 H=340,渲染比 1.044 与原图差 0.10%,几乎完美
 *   - image 474 实测 1089×962(横图,比率 1.132);自然 W=355 H=314,渲染比 1.132 与原图差 0%,完全匹配
 *   - 图1 Y=135+340=475,图2 Y=478+314=792(均在书框 Y=88-872 范围内,余量 397/80dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 475.png(已复制为 res/drawable-nodpi/img_volume10part5_image_475.png)
 *   - 图2:设计稿 image 474.png(已复制为 res/drawable-nodpi/img_volume10part5_image_474.png)
 *
 * 本屏跳转目标:点击"偏见从何而来"标题 → Vol-10-6(创建于 2026-09-13,本屏兑现 §67.3 KDoc 承诺,回填 onOpenVolume10Part6)。
 */
@Composable
fun Volume10Part5Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part6: () -> Unit = {},
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
            // 标题"偏见从何而来"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约,与 Vol-10-4 同款跨页同标题叙述)。
            // 点击跳 Vol-10-6(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume10Part6),
            )

            // 图1(image 475.png,X=18, Y=135, W=355, H=340)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 340.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part5_image_475),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 474.png,X=18, Y=478, W=355, H=314)— 在书框之上、中下部(横图)。
            // Y=478+314=792,在书框 Y=88-872 范围内(余量 80dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 478.dp)
                    .size(width = 355.dp, height = 314.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part5_image_474),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}