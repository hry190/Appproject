package com.jueqiao.jianghu.ui.screens.volume8part10

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
 * 第八卷-10 页 — 第八卷-9 → 点击"皮影戏之奖励塑形"标题跳转目标。
 *
 * 布局(z-order 由下到上,**7 层,含 3 张图**—Vol-8 首次 3 图布局):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-9(255)→ Vol-8-10(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"皮影戏之奖励塑形"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **8 字**(全中文无标点)W=192 沿用 6-8 字规约(8 字无独立规约,KDoc 标注异常,真机可微调;新标题系列)
 *   - 图1(image 531.png,X=18, Y=135, **W=355, H=208**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.709)=208)
 *   - 图2(image 532.png,X=18, Y=369, **W=358, H=142**)— 中部(应用新规则:**W=358 非常用 355**,H=round(358/2.525)=142)
 *   - 图3(image 533.png,X=18, Y=545, **W=358, H=308**)— 下部(应用新规则:W=358 非常用 355,H=round(358/1.162)=308)
 *
 * 坐标说明:
 *   - image 531 实测 1056×618(横图,比率 1.709);自然 W=355 H=208,渲染比 1.707 与原图差 0.12%,几乎完美
 *   - image 532 实测 1068×423(极扁横图,比率 2.525);自然 W=358 H=142,渲染比 2.521 与原图差 0.15%,几乎完美
 *   - image 533 实测 1056×909(近正方形,比率 1.162);自然 W=358 H=308,渲染比 1.162 与原图差 0.05%,几乎完美
 *   - 图1 Y=135+208=343,图 2 Y=369+142=511,图 3 Y=545+308=853(均在书框 Y=88-872 范围内,图 3 余量 19dp 较紧)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 531.png(已复制为 res/drawable-nodpi/img_volume8part10_image_531.png)
 *   - 图2:设计稿 image 532.png(已复制为 res/drawable-nodpi/img_volume8part10_image_532.png)
 *   - 图3:设计稿 image 533.png(已复制为 res/drawable-nodpi/img_volume8part10_image_533.png)
 *
 * 点击跳 Vol-8-11(Vol-8-11 创建时回填 callback 与 .clickable)。
 */
@Composable
fun Volume8Part10Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part11: () -> Unit = {},
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
            // 标题"皮影戏之奖励塑形"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 8 字 W=192,点击跳第八卷-11。
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
                    .clickable(onClick = onOpenVolume8Part11),
            )

            // 图1(image 531.png,X=18, Y=135, W=355, H=208)— 在书框之上、上部。
            // Y=135+208=343,在书框 Y=88-872 范围内(余量 529dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 125.dp)
                    .size(width = 355.dp, height = 208.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part10_image_531),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 532.png,X=18, Y=369, W=358, H=142)— 在书框之上、中部(用户字面 W=358 非常用 355)。
            // Y=369+142=511,在书框 Y=88-872 范围内(余量 361dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 345.dp)
                    .size(width = 358.dp, height = 142.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part10_image_532),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 533.png,X=18, Y=545, W=358, H=308)— 在书框之上、下部(用户字面 W=358 非常用 355)。
            // Y=545+308=853,在书框 Y=88-872 范围内(余量 19dp,较紧)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 505.dp)
                    .size(width = 358.dp, height = 308.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part10_image_533),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}