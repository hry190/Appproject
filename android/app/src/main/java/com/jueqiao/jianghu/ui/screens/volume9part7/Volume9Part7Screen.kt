package com.jueqiao.jianghu.ui.screens.volume9part7

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
 * 第九卷-7 页 — 第九卷-6 → 点击"语义也有远近"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-9-6(255)→ Vol-9-7(255) 连续两屏异常,字面"复制第一卷-1"=255 优先于交替模式)
 *   - 标题文本"大模型核心"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **5 字** W=213 沿用 5 字规约(Vol-5-10/11/12「偏差的数据」、Vol-6-1「相似要有尺」同款真机测过宽度)
 *   - 图1(image 503.png,X=18, Y=135, **W=355, H=340**)— 上部(应用新规则 fit-to-natural-bounds:横图 W=355 H=round(355/1.043)=340)
 *   - 图2(image 504.png,X=18, Y=478, **W=384, H=394**)— 中下部(应用新规则:近正方形 ratio 0.975 自动 fit,H=394 max W=round(394×0.975)=384,与 Vol-8-1 image 465 ratio 0.970 → W=382 同模式)
 *
 * 坐标说明:
 *   - image 503 实测 1008×966(横图,比率 1.043);自然 W=355 H=340,渲染比 1.044 与原图差 0.10%,几乎完美
 *   - image 504 实测 1035×1062(近正方形,比率 0.975);自然 W=384 H=394,渲染比 0.975 与原图差 0%,完全匹配
 *   - 图1 Y=135+340=475,图2 Y=478+394=872,**Y=872 正好顶到书框底 Y=88-872(余量 0dp)** — 紧贴设计约束,近正方形自动 fit 的可接受代价(参照 Vol-8-1 image 465 同模式)
 *   - 图1 W=355,图2 W=384 — 宽度不一致(19dp 差),由新规则按各自 PNG 比例 fit 自然形成,布局上 image 2 偏右突出 X=18+384=402(父 Box 宽 854 余量 452)
 *
 * 资源来源:
 *   - 背景:设计稿 image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:设计稿 Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:设计稿 image 503.png(已复制为 res/drawable-nodpi/img_volume9part7_image_503.png)
 *   - 图2:设计稿 image 504.png(已复制为 res/drawable-nodpi/img_volume9part7_image_504.png)
 *
 * 本屏跳转目标:点击"大模型核心"标题 → Vol-9-8(创建于 2026-09-13,本屏兑现 §51.3 KDoc 承诺,回填 onOpenVolume9Part8)。
 */
@Composable
fun Volume9Part7Screen(
    onBack: () -> Unit = {},
    onOpenVolume9Part8: () -> Unit = {},
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
            // 标题"大模型核心"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 5 字 W=213(沿用 5 字规约)。
            // 点击跳 Vol-9-8(创建于 2026-09-13)。
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
                    .clickable(onClick = onOpenVolume9Part8),
            )

            // 图1(image 503.png,X=18, Y=120, W=355, H=340)— 在书框之上、上部(横图)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 120.dp)
                    .size(width = 355.dp, height = 340.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part7_image_503),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 504.png,X=18, Y=478, W=384, H=394)— 在书框之上、中下部(近正方形)。
            // Y=478+394=872 正好顶到书框底(余量 0dp);W=384 超出标准 max_W=355(近正方形自动 fit 的可接受代价)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 465.dp)
                    .size(width = 354.dp, height = 380.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume9part7_image_504),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}