package com.jueqiao.jianghu.ui.screens.volume7part11

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
 * 第七卷-11 页 — 第七卷-10 → 点击"皮影戏之误差逆流改招"标题跳转目标。**Vol-7-11 标题点击跳 Vol-7-12**。
 *
 * 布局(z-order 由下到上,**7 层,含 4 张图**—Vol-1~7 系列首次 4 图布局):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-7-10(255)→ Vol-7-11(255) 用户字面优先,连续两屏 255)
 *   - 标题文本"皮影戏之误差逆流改招"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **10 字**(全中文无标点)W=302 沿用 9 字规约(10 字无独立规约,KDoc 标注异常,真机可微调)
 *   - 图1(image 458.png,X=18, Y=135, **W=355, H=129**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/2.750)=129)
 *   - 图2(image 459.png,X=18, Y=295, **W=355, H=173**)— 中上(应用新规则:横图 W=355 H=round(355/2.058)=173)
 *   - 图3(image 460.png,X=18, Y=497, **W=355, H=178**)— **2026-09-13 用户修正**:原误用 image 459 重用(笔误),已改为 image 460(应用新规则:横图 W=355 H=round(355/2.000)=178)
 *   - 图四(image 461.png,X=18, Y=690, **W=355, H=153**)— **2026-09-13 用户新增** image 461(应用新规则:横图 W=355 H=round(355/2.316)=153)
 *
 * 坐标说明:
 *   - image 458 实测 1056×384(极扁横图,比率 2.750);自然 W=355 H=129,渲染比 2.752 与原图差 0.07%,几乎完美
 *   - image 459 实测 1068×519(横图,比率 2.058);自然 W=355 H=173,渲染比 2.052 与原图差 0.28%,几乎完美
 *   - image 460 实测 1068×534(横图,比率 2.000);自然 W=355 H=178,渲染比 1.994 与原图差 0.28%,几乎完美
 *   - image 461 实测 1056×456(横图,比率 2.316);自然 W=355 H=153,渲染比 2.320 与原图差 0.19%,几乎完美
 *   - 图1 Y=135+129=264,图2 Y=295+173=468,图3 Y=497+178=675,图4 Y=690+153=843(均在书框 Y=88-872 内)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 458.png(已复制为 res/drawable-nodpi/img_volume7part11_image_458.png)
 *   - 图2:设计稿 image 459.png(已复制为 res/drawable-nodpi/img_volume7part11_image_459.png)
 *   - 图3:设计稿 image 460.png(已复制为 res/drawable-nodpi/img_volume7part11_image_460.png)— 2026-09-13 用户修正
 *   - 图4:设计稿 image 461.png(已复制为 res/drawable-nodpi/img_volume7part11_image_461.png)— 2026-09-13 用户新增
 *
 * **本屏无后继页,故未接 clickable**(等 Vol-7-12 创建时按历次约定回填 onOpenVolume7Part12)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第七卷-10标题" → 实际意图"第七卷-10 的标题"(缺少" 的"),无歧义
 *   - 用户初版写"图3...image 459.png"→ 2026-09-13 后续修正:图3 改 image 460,图4 改 image 461(消除原"image 459 三次复用"现象)— 留痕
 */
@Composable
fun Volume7Part11Screen(
    onBack: () -> Unit = {},
    onOpenVolume7Part12: () -> Unit = {},
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
            // 标题"皮影戏之误差逆流改招"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 10 字 W=302(沿用 9 字规约),点击跳第七卷-12。
            Text(
                text = "皮影戏之误差逆流改招",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume7Part12),
            )

            // 图1(image 458.png,X=18, Y=135, W=355, H=129)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 129.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part11_image_458),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 459.png,X=18, Y=295, W=355, H=173)— 在书框之上、中上。
            // Y=295+173=468,与图 1 间距 31dp。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 295.dp)
                    .size(width = 355.dp, height = 173.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part11_image_459),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图3(image 460.png,X=18, Y=497, W=355, H=178)— 2026-09-13 用户修正(原误用 image 459)。
            // Y=497+178=675,与图 2 间距 29dp。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 497.dp)
                    .size(width = 355.dp, height = 178.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part11_image_460),
                    contentDescription = "图3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图四(image 461.png,X=18, Y=690, W=355, H=153)— 2026-09-13 用户新增。
            // Y=690+153=843,在书框 Y=88-872 范围内(余量 29dp,比原 9dp 宽裕)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 690.dp)
                    .size(width = 355.dp, height = 153.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume7part11_image_461),
                    contentDescription = "图四",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}