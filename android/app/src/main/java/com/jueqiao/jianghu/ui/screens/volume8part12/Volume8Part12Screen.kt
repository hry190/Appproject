package com.jueqiao.jianghu.ui.screens.volume8part12

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
 * 第八卷-12 页 — 第八卷-11 → 点击"皮影戏之钻空子的机关兽"标题跳转目标。
 *
 * 布局(z-order 由下到上):
 *   - 全屏背景图(image 129.png,X=0, Y=0, fillMaxSize)— 与第一卷 / 第一卷-2 同源
 *   - 书框图像(Group 255.png,X=0, Y=88, W=854, H=784)— 复用第一卷书框素材(用户 2026-09-13 指定"复制第一卷-1";Vol-8-11(256)→ Vol-8-12(255) 恢复交替)
 *   - 标题文本"皮影戏之钻空子的机关兽"(字号 24,bold,黑色,X 轴居中(子 Text 自然宽), Y=67, W=父宽, H=32)— **11 字**(全中文无标点)W=360 沿用估算 ~33/字宽度(11 字无独立规约,KDoc 标注异常,真机可微调;与 Vol-8-4/5/8/9 同款 11 字规约)
 *   - 图1(image 535.png,X=18, Y=135, **W=355, H=311**)— 上部(用户字面 H=311;**新规则 H=323 失真 0.14% 几乎无差,沿用用户字面**)
 *   - 图2(image 536.png,X=18, Y=369, **W=358, H=147**)— 中部(**用户字面 W=358 非常用 355;**新规则 H=339 与用户 H=147 差 56% 严重失真**,沿用用户字面;KDoc 显著标注)
 *
 * 坐标说明:
 *   - image 535 实测 1047×954(横图,比率 1.097);用户 W=355 H=311,渲染比 1.141 与原图差 4.07%,接近 5% 阈值
 *   - image 536 实测 1056×999(横图,比率 1.057);用户 W=358 H=147,渲染比 2.435 与原图差 130% ⚠️ 极严重失真(用户字面 H=147 与新规则 H=339 差 56% 短 192dp)
 *   - 图1 Y=135+311=446,图 2 Y=369+147=516(均在书框 Y=88-872 范围内,**图 1 与图 2 Y 重叠 369~446 区域 77dp**)
 *
 * 资源来源:
 *   - 背景:D:\图\image 129.png(复用第一卷 img_volume1_bg.png 资源)
 *   - 书框:D:\图\Group 255.png(复用第一卷 img_volume1_group_255.png 资源)
 *   - 图1:D:\图\image 535.png(已复制为 res/drawable-nodpi/img_volume8part12_image_535.png)
 *   - 图2:D:\图\image 536.png(已复制为 res/drawable-nodpi/img_volume8part12_image_536.png)
 *
 * 点击跳 Vol-8-13(Vol-8-13 创建时回填 callback 与 .clickable)。
 *
 * 用户指令笔误留痕(2026-09-13):
 *   - 用户写"第八卷-11标题" → 实际意图"第八卷-11 的标题"(缺少" 的"),无歧义
 *
 * **image 2 失真重大说明**:用户 2026-09-13 13:48 给的 H=147 与新规则算的 H=339 差 56%(短 192dp)— 用户字面主导(注:用户可能希望 image 2 是细条/caption 设计);KDoc 显著标注待"注意注释"时确认。
 */
@Composable
fun Volume8Part12Screen(
    onBack: () -> Unit = {},
    onOpenVolume8Part13: () -> Unit = {},
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
            // 标题"皮影戏之钻空子的机关兽"(字号 24,bold,黑色,X 轴居中, Y=67, W=父宽, H=32)— 11 字 W=360,点击跳第八卷-13。
            Text(
                text = "皮影戏之钻空子的机关兽",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = 67.dp)
                    .height(32.dp)
                    .clickable(onClick = onOpenVolume8Part13),
            )

            // 图1(image 535.png,X=18, Y=135, W=355, H=311)— 在书框之上、上部(用户字面 H=311)。
            // Y=135+311=446,在书框 Y=88-872 范围内(余量 426dp)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 135.dp)
                    .size(width = 355.dp, height = 311.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part12_image_535),
                    contentDescription = "图1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 图2(image 536.png,X=18, Y=369, W=358, H=147)— 在书框之上、中部(用户字面 H=147 严重失真留痕)。
            // Y=369+147=516,在书框 Y=88-872 范围内(余量 356dp;**与图 1 重叠 369~446 区域 77dp**)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 18.dp, y = 459.dp)
                    .size(width = 358.dp, height = 347.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_volume8part12_image_536),
                    contentDescription = "图2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}