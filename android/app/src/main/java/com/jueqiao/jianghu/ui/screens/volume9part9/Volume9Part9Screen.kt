package com.jueqiao.jianghu.ui.screens.volume9part9

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
 * 第九卷-9 页 — 第九卷-8 → 点击"大模型核心"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-8(256)→ Vol-9-9(255) 奇偶交替恢复,非异常)
 *   - 标题文本"大模型核心"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」同款真机测过宽度;与 Vol-9-7/9-8 同款 5 字标题)
 *   - 图1(image 507.png,X=18, Y=135, **W=355, H=202**)— 上部(应用新规则 fit-to-natural-bounds:扁横图 ratio 1.754 自动 fit W=355 H=round(355/1.754)=202)
 *   - 图2(image 508.png,X=18, Y=478, **W=355, H=352**)— 中下部(应用新规则:近正方形 ratio 1.009 自动 fit W=355 H=round(355/1.009)=352)
 *
 * 坐标说明:
 *   - image 507 实测 1047×597(扁横图,比率 1.754);自然 W=355 H=202,渲染比 1.757 与原图差 0.16%,几乎完美
 *   - image 508 实测 1059×1050(近正方形,比率 1.009);自然 W=355 H=352,渲染比 1.009 与原图差 0%,完全匹配
 *   - 图1 Y=135+202=337,图2 Y=478+352=830(均在书框 Y=88-872 范围内,余量 535/42dp)— 图1 是扁横图所以上方余量较多(535dp),图2 余量 42dp
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 507.png(已复制为 res/drawable-nodpi/img_volume9part9_image_507.png)
 *   - 图2:D:\图\image 508.png(已复制为 res/drawable-nodpi/img_volume9part9_image_508.png)
 *
 * 本屏跳转目标:点击"大模型核心"标题 → Vol-9-10(创建于 2026-09-13,本屏兑现 §53.3 KDoc 承诺,回填 onOpenVolume9Part10)。
 */
@Composable
fun Volume9Part9Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part10: () -> Unit = {},
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
            // 标题"大模型核心"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约,与 Vol-9-7/9-8 同款)。
            // 点击跳 Vol-9-10(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume9Part10),
            )

            // 图1(image 507.png,X=18, Y=135, W=355, H=202)— 在书框之上、上部(扁横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 202.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part9_image_507),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 508.png,X=18, Y=478, W=355, H=352)— 在书框之上、中下部(近正方形)。
            // Y=478+352=830,在书框 Y=88-872 范围内(余量 42dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 378.dp)
                    .size(width = 355.dp, height = 352.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part9_image_508),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}