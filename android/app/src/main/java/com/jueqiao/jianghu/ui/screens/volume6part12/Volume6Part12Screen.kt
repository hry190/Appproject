package com.jueqiao.jianghu.ui.screens.volume6part12

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
 * 第六卷-12 页 — 第六卷-11 → 点击"门槛一动，错法不同"标题跳转目标。**Vol-6-12 标题点击跳 Vol-6-13**。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-12 指定"复制第一卷-1";Vol-6-11(256)→ Vol-6-12(255) 恢复交替)
 *   - 标题文本"门槛一动，错法不同"(字号 24,bold,黑色,X=110, Y=67, **W=302, H=32**)— **9 字**(含 1 个中文`,`)W=302 沿用 Vol-3-5 同款真机测过宽度(与 Vol-6-11 同款,跨页同标题)
 *   - 图1(image 130.png,X=18, Y=135, W=355, **H=288**)— 上部(**用户字面 H=311 → 自然 H=288** 按宽度调整规则自动重算,采用自然高度 355/1.232 消除畸变)
 *   - 图2(image 7.png,X=18, **Y=458**, **W=264, H=342**)— 下部(**用户 2026-09-12 真机手动调整**:W=355→264, H=321→342, Y=478→458;W×H 自然比例 264/342=0.772 与原图一致,失 0%;图片缩窄且上移 20dp 后 fit 书框,余量 72dp)
 *   - **尺寸决策演变史**:用户原 H=321 渲染比 1.106 畸变 43% → AskUserQuestion 后改 H=460(跳出书框) → 用户真机再次手动调为 W=264 H=342 Y=458(按比例缩到 fit)
 *
 * 坐标说明:
 *   - image 130 实测 1050×852(横向矩形,比率 1.232);**实际 H=288**(用户 2026-09-12 字面 H=311→ 自然 H=288),渲染比 1.233 与原图差 0.1%,几乎完美
 *   - image 7 实测 792×1026(竖向矩形,比率 0.772,小宽高大);**实际 W=264 H=342 Y=458**(用户 2026-09-12 字面 W=355 H=321 Y=478→ 真机手动改为 W=264 H=342 Y=458 按比例缩),渲染比 264/342=0.772 与原图差 0%,完全匹配
 *   - 图2 Y=458+342=800,在书框 Y=88-872 范围内(余量 72dp;之前"跳出书框"方案已撤回)
 *   - 图1 Y=135+288=423,在书框 Y=88-872 范围内(余量 449dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 130.png(已复制为 res/drawable-nodpi/img_volume6part12_image_130.png)
 *   - 图2:D:\图\image 7.png(已复制为 res/drawable-nodpi/img_volume6part12_image_7.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-6-13 创建时按历次约定回填 onOpenVolume6Part13)。
 *
 * 用户指令笔误留痕(2026-09-12):
 *   - 用户写"第六卷-8-12" → 实际意图"第六卷-12"(按上下文推断,无歧义)
 */
@Composable
fun Volume6Part12Screen(
    onBack: () -> Unit = {},
    onOpenVolume6Part13: () -> Unit = {},
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
            // 标题"门槛一动，错法不同"(字号 24,bold,黑色,X=110, Y=67, W=302, H=32)— 9 字 W=302,点击跳第六卷-13。
            Text(
                text = "门槛一动，错法不同",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = 110.dp, y = 67.dp)
                    .size(width = 302.dp, height = 32.dp)
                    .clickable(onClick = onOpenVolume6Part13),
            )

            // 图1(image 130.png,X=18, Y=135, W=355, H=288)— 在书框之上、上部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 288.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part12_image_130),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 7.png,X=18, Y=458, W=264, H=342)— 在书框之内、中下部(真机手动调整,fit书框)。
            // Y=458+342=800,在书框 Y=88-872 范围内(余量 72dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 458.dp)
                    .size(width = 264.dp, height = 342.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume6part12_image_7),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}