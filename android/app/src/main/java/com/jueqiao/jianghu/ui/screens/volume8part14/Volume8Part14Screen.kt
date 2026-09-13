package com.jueqiao.jianghu.ui.screens.volume8part14

import androidx.activity.compose.BackHandler
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
 * 第八卷-14 页 — 第八卷-13 → 点击"皮影戏之钻空子的机关兽"标题跳转目标。
 *
 * 布局(z-order 由下到上,**4 层,单图布局**—Vol-8 第 3 个单图屏,V1=Vol-8-9,V2=Vol-8-11):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-13(256)→ Vol-8-14(255) 恢复交替)
 *   - 标题文本"皮影戏之钻空子的机关兽"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **11 字**(全中文无标点)W=360 沿用估算 ~33/字宽度(11 字无独立规约,KDoc 标注异常,真机可微调;与 Vol-8-12/8-13 同款 11 字规约,跨页同标题)
 *   - 图1(image 539.png,X=18, Y=135, **W=355, H=300**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.183)=300,0% 失真)
 *
 * **本屏无图 2**(用户字面 2026-09-13 只指定 image 539;沿用 Vol-5-9/6-6/6-15/8-9/8-11 单图先例直接采用 4 层 z-order,如有出入随时改回 5 层)
 *
 * 坐标说明:
 *   - image 539 实测 1026×867(横图,比率 1.183);自然 W=355 H=300,渲染比 1.183 与原图差 0%,完全匹配
 *   - 图1 Y=135+300=435,在书框 Y=88-872 范围内(余量 437dp)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 539.png(已复制为 res/drawable-nodpi/img_volume8part14_image_539.png)
 *
 * 本屏暂无后继页,故未接 clickable(等 Vol-8-15 创建时按历次约定回填 onOpenVolume8Part15)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-13标题" → 实际意图"第八卷-13 的标题"(缺少" 的"),无歧义
 */
@Composable
fun Volume8Part14Screen(
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
            // 标题"皮影戏之钻空子的机关兽"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 11 字 W=360(沿用估算 ~33/字宽度,无独立规约,与 Vol-8-12/8-13 同款)。
            // 本屏暂无后继页,故未接 clickable(等 Vol-8-15 创建时按历次约定回填 onOpenVolume8Part15)。
            Text(
                text = "皮影戏之钻空子的机关兽",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp),
            )

            // 图1(image 539.png,X=18, Y=135, W=355, H=300)— 在书框之上、上部(单图屏)。
            // Y=135+300=435,在书框 Y=88-872 范围内(余量 437dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 300.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part14_image_539),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}