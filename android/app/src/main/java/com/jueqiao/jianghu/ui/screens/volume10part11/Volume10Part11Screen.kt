package com.jueqiao.jianghu.ui.screens.volume10part11

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
 * 第十卷-11 页 — 第十卷-10 → 点击"眼见未必为实"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 256.png,X=0, Y=88, W=854, H=784)— 复用第一卷-2 书框素材(用户 2026-09-13 指定"复制第一卷-2";Vol-10-10(255)→ Vol-10-11(256) 奇偶交替恢复,非异常)
 *   - 标题文本"眼见未必为实"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-10-4/5/6「偏见从何而来」同款真机基线;与 Vol-10-10 同款 5 字标题,跨页同标题叙述)
 *   - 图1(image 483.png,X=18, Y=135, **W=355, H=202**)— 上部(应用新规则 fit-to-natural-bounds:扁横图 ratio 1.754 自动 fit W=355 H=round(355/1.754)=202)— **用户 2026-09-13 第二次指令:图 1/图 2 由 image 480/40 改为 image 483/484**(PNG 路径变更)
 *   - 图2(image 484.png,X=18, **Y=381**, W=355, H=249)— 中部(**Y=381 非常用 Y=478**,沿用 Vol-7-7/Vol-10-10 2 张图布局变体的中部 Y;**2 张图布局变体**与标准 2 张图布局不同)— (应用新规则:横图 ratio 1.424 自动 fit W=355 H=round(355/1.424)=249)
 *
 * 坐标说明:
 *   - image 483 实测 1068×609(扁横图,比率 1.754);自然 W=355 H=202,渲染比 1.761 与原图差 0.40%,几乎完美
 *   - image 484 实测 1098×771(横图,比率 1.424);自然 W=355 H=249,渲染比 1.424 与原图差 0%,完全匹配
 *   - 图1 Y=135+204=339,图2 Y=381+198=579(均在书框 Y=88-872 范围内,余量 533/293dp)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 256.png(复用第一卷-2 img_volume1part2_group_256.png 资源)
 *   - 图1:设计稿 image 483.png(已复制为 res/drawable-nodpi/img_volume10part11_image_483.png)
 *   - 图2:设计稿 image 484.png(已复制为 res/drawable-nodpi/img_volume10part11_image_484.png)
 *
 * 本屏跳转目标:点击"眼见未必为实"标题 → Vol-10-12(创建于 2026-09-13,本屏兑现 §73.4 KDoc 承诺,回填 onOpenVolume10Part12)。
 */
@Composable
fun Volume10Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part12: () -> Unit = {},
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
            // 标题"眼见未必为实"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约,与 Vol-10-10 同款跨页同标题叙述)。
            // 点击跳 Vol-10-12(创建于 2026-09-13)。
            Text(
                text = "眼见未必为实",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume10Part12),
            )

            // 图1(image 483.png,X=18, Y=135, W=355, H=204)— 在书框之上、上部(扁横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 204.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part11_image_483),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 484.png,X=18, Y=381, W=355, H=249)— 在书框之上、中部(扁横图,2 张图布局变体)— Y=381 沿用 Vol-7-7/Vol-10-10。
            // Y=381+249=630,在书框 Y=88-872 范围内(余量 242dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 381.dp)
                    .size(width = 355.dp, height = 198.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part11_image_484),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}