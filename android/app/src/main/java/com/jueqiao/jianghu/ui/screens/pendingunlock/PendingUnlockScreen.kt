package com.jueqiao.jianghu.ui.screens.pendingunlock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 待解锁 页 — 学习3 → 点击答题插图(image 217.png)跳转目标。
 *
 * 布局(从 UnfinishedScreen 复制,**不含**"未完待续"文字图像):
 *   - 全屏背景图(image 129.png)
 *   - 图像 image 134.png(X=-1, Y=40, W=954, H=784)— 书框
 *   - 图像 Android Compact - 124.png(全屏,书框上层)
 *   - 熊猫图像 image 307.png(X=-7, Y=563, W=250, H=330)
 *   - "待解锁"文字图像(待解锁.png,X=158, Y=150, W=96, H=338)— 中央纵向书名样式,**点击跳滚轮1**
 *   - "前往解锁"按钮(未标题-2 23.png,X=222, Y=725, W=160, H=58)
 *   - "前往解锁"文本(字号 16,色 #605718,X=265, Y=738, W=81, H=21)— 在按钮上层
 *
 * 与 UnfinishedScreen 的差别:**不含** "未完待续" 文字图像(img_unfinished_text)。
 * 不复制该图像是因为它已被学习3 → 待解锁流程替代;待解锁屏上没有可点击的文字引导,
 * 故原 UnfinishedScreen 上的 onOpenGunlun1 入口也一并移除。
 *
 * TODO: UnfinishedScreen + PendingUnlockScreen 4 个元素 100% 重复;
 * 已是 [docs/CODE-AUDIT-2026-09-09.md](../CODE-AUDIT-2026-09-09.md) §"Massive duplication
 * across gunlun1-12 screens" 同类问题。建议抽 `BookFrameScaffold(content)` 帮助函数,
 * 出现第 3 个类似屏再统一抽。当前保留重复。
 */
@Composable
fun PendingUnlockScreen(
    onBack: () -> Unit = {},
    onOpenGunlun1: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 129.png)
        Image(
            painter = painterResource(R.drawable.img_unfinished_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 图像 image 134.png(X=-1, Y=40, W=954, H=784)— 书框
        Image(
            painter = painterResource(R.drawable.img_unfinished_image134),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (-1).dp, y = 40.dp)
                .size(width = 954.dp, height = 784.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 图像 Android Compact - 124.png(全屏,在书框上层)
        Image(
            painter = painterResource(R.drawable.img_unfinished_compact124),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        // 熊猫图像 image 307.png(X=-7, Y=563, W=250, H=330)
        Image(
            painter = painterResource(R.drawable.img_unfinished_image307),
            contentDescription = "熊猫",
            modifier = Modifier
                .offset(x = (-7).dp, y = 560.dp)
                .size(width = 250.dp, height = 330.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "待解锁"文字图像(待解锁.png,X=158, Y=150, W=96, H=338)— 中央纵向。
        // Y=150–488 在书框 Y=40–824 范围内,不与熊猫(Y=560–890)Y 重叠。
        // **点击跳滚轮1** — 沿用 UnfinishedScreen "未完待续" 文字图像的入口模式。
        Image(
            painter = painterResource(R.drawable.img_pendingunlock_text),
            contentDescription = "待解锁",
            modifier = Modifier
                .offset(x = 158.dp, y = 150.dp)
                .size(width = 96.dp, height = 338.dp)
                .clickable(onClick = onOpenGunlun1),
            contentScale = ContentScale.FillBounds,
        )

        // "前往解锁"按钮(未标题-2 23.png,X=222, Y=725, W=160, H=58)。
        // Y=725–783 在书框 Y=40–824 范围内;与熊猫(Y=560–890)在 X=222–243 范围 Y=725–783 重叠,
        // 写在熊猫之后 → 视觉上覆盖熊猫右下角。
        Image(
            painter = painterResource(R.drawable.img_pendingunlock_button),
            contentDescription = "前往解锁按钮",
            modifier = Modifier
                .offset(x = 222.dp, y = 725.dp)
                .size(width = 160.dp, height = 58.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "前往解锁"文本(字号 16,色 #605718,X=265, Y=738, W=81, H=21)。
        // Y=738–759 在按钮(Y=725–783)Y 范围内 → 视觉上叠在按钮上;
        // X=265–346 在按钮(X=222–382)X 范围内 → 居中。
        Text(
            text = "前往解锁",
            color = Color(0xFF605718),
            fontSize = 16.sp,
            fontFamily = YaHei,
            modifier = Modifier
                .offset(x = 265.dp, y = 738.dp)
                .size(width = 81.dp, height = 21.dp),
        )
    }
}