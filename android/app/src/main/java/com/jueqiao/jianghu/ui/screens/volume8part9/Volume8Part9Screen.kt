package com.jueqiao.jianghu.ui.screens.volume8part9

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
 * 第八卷-9 页 — 第八卷-8 → 点击"皮影戏之每一步奖励"标题跳转目标。
 *
 * 布局(z-order 由下到上,**4 层,单图布局**—Vol-1~8 多次出现的单图先例,沿用 4 层 z-order):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-8(256)→ Vol-8-9(255) 恢复交替)
 *   - 标题文本"皮影戏之每一步奖励"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **9 字**(全中文无标点)W=302 沿用 9 字规约(与 Vol-8-7/8-8 同款,跨页同标题)
 *   - 图1(image 530.png,X=18, Y=135, **W=355, H=278**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.278)=278)
 *
 * **本屏无图 2**(用户字面 2026-09-13 只指定 image 530;沿用 Vol-5-9/6-6/6-15 等单图先例直接采用 4 层 z-order,如有出入随时改回 5 层)
 *
 * 坐标说明:
 *   - image 530 实测 1020×798(横图,比率 1.278);自然 W=355 H=278,渲染比 1.277 与原图差 0.10%,几乎完美
 *   - 图1 Y=135+278=413,在书框 Y=88-872 范围内(余量 459dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 530.png(已复制为 res/drawable-nodpi/img_volume8part9_image_530.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-10 创建时按历次约定回填 onOpenVolume8Part10)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-8标题" → 实际意图"第八卷-8 的标题"(缺少" 的"),无歧义
 */
@Composable
fun Volume8Part9Screen(
    onBack: () -> Unit = {},
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
            // 标题"皮影戏之每一步奖励"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 9 字 W=302(沿用 9 字规约,与 Vol-8-7/8-8 同款)。
            // 本屏暂无后继页,故未接 clickable(等 Vol-8-10 创建时按历次约定回填 onOpenVolume8Part10)。
            Text(
                text = "皮影戏之每一步奖励",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp),
            )

            // 图1(image 530.png,X=18, Y=135, W=355, H=278)— 在书框之上、上部(单图屏)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 278.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part9_image_530),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}