package com.jueqiao.jianghu.ui.screens.volume10part10

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
 * 第十卷-10 页 — 第十卷-9 → 点击"借招也要署名"标题跳转目标。
 *
 * 布局(z-order 由下到上,**7 层,3 张图布局**—Vol-7-4/5 多次出现的 3 张图布局,沿用 Y=135/381/606):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-10-9(255)→ Vol-10-10(255) **连续两屏异常**,字面"复制第一卷-1"=255 优先于交替模式)
 *   - 标题文本"眼见未必为实"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」、Vol-10-4/5/6「偏见从何而来」同款 5 字真机基线)— **首次 Vol-10 系列 5 字新标题**
 *   - 图1(image 480.png,X=18, Y=135, **W=355, H=204**)— 上部(应用新规则 fit-to-natural-bounds:扁横图 ratio 1.743 自动 fit W=355 H=round(355/1.743)=204)
 *   - 图2(image 40.png,X=18, **Y=381**, W=355, H=198)— 中部(Y=381 非常用 Y=478,沿用 Vol-7-4/5 3 张图布局的中部 Y)— (应用新规则:扁横图 ratio 1.789 自动 fit W=355 H=round(355/1.789)=198)
 *   - 图3(image 482.png,X=18, Y=606, W=355, H=211)— 下部(应用新规则:扁横图 ratio 1.679 自动 fit W=355 H=round(355/1.679)=211)
 *
 * 坐标说明:
 *   - image 480 实测 1056×606(扁横图,比率 1.743);自然 W=355 H=204,渲染比 1.740 与原图差 0.17%,几乎完美
 *   - image 40 实测 1068×597(扁横图,比率 1.789);自然 W=355 H=198,渲染比 1.788 与原图差 0.06%,几乎完美
 *   - image 482 实测 1068×636(扁横图,比率 1.679);自然 W=355 H=198 H=211,渲染比 1.682 与原图差 0.18%,几乎完美
 *   - 图1 Y=135+204=339,图2 Y=381+198=579,图3 Y=606+211=817(均在书框 Y=88-872 范围内,余量 533/293/55dp)— 3 张扁横图紧凑堆叠
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 480.png(已复制为 res/drawable-nodpi/img_volume10part10_image_480.png)
 *   - 图2:D:\图\image 40.png(已复制为 res/drawable-nodpi/img_volume10part10_image_40.png)
 *   - 图3:D:\图\image 482.png(已复制为 res/drawable-nodpi/img_volume10part10_image_482.png)
 *
 * 本屏跳转目标:点击"眼见未必为实"标题 → Vol-10-11(创建于 2026-09-13,本屏兑现 §72.3 KDoc 承诺,回填 onOpenVolume10Part11)。
 */
@Composable
fun Volume10Part10Screen(
    onBack: () -> Unit = {},
    onOpenVolume10Part11: () -> Unit = {},
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
            // 标题"眼见未必为实"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约)。
            // 点击跳 Vol-10-11(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume10Part11),
            )

            // 图1(image 480.png,X=18, Y=135, W=355, H=204)— 在书框之上、上部(扁横图,3 张图布局)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 204.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part10_image_480),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 40.png,X=18, Y=381, W=355, H=198)— 在书框之上、中部(扁横图,3 张图布局)— Y=381 沿用 Vol-7-4/5。
            // Y=381+198=579,在书框 Y=88-872 范围内(余量 293dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 381.dp)
                    .size(width = 355.dp, height = 198.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part10_image_40),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 482.png,X=18, Y=606, W=355, H=211)— 在书框之上、下部(扁横图,3 张图布局)。
            // Y=606+211=817,在书框 Y=88-872 范围内(余量 55dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 606.dp)
                    .size(width = 355.dp, height = 211.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume10part10_image_482),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}