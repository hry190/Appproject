package com.jueqiao.jianghu.ui.screens.learning2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 学习2 页 — 学习1 → 点击 Group 281 卷轴跳转目标。
 *
 * 布局(与学习1 视觉一致 + 中央卷轴):
 *   - 全屏背景图(Android Compact - 109.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,与学习1 一致)
 *   - 中央卷轴(image 174.png,X 轴居中, Y=124, W=407, H=563)
 *   - 顶部文本(生活问题推荐:\n机器人为什么会认错物体?,字号 24,黑色,X=68, Y=190, W=292, H=64)
 *
 * 资源来源:
 *   - 背景 / 返回按钮与学习1 同源
 *   - 中央卷轴:D:\图\image 174.png(需复制为 res/drawable-nodpi/img_learning_image_174.png)
 */
@Composable
fun Learning2Screen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(Android Compact - 109.png,与学习1 一致)
        Image(
            painter = painterResource(R.drawable.img_houshan_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,与学习1 一致)— 点击回到学习1 页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = onBack),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 中央卷轴(image 174.png,X 轴居中, Y=124, W=407, H=563)。
            // 当前为静态展示元素,后续如需可点击跳学习3 再加 .clickable + onOpenLearning3。
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 124.dp)
                    .size(width = 407.dp, height = 563.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_image_174),
                    contentDescription = "中央卷轴",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 顶部文本(2 行,字号 24,黑色,X=68, Y=190, W=292, H=64)。
            // 字体用项目统一的 YaHei,与 houshan1 等屏保持一致。
            Text(
                text = "生活问题推荐:\n机器人为什么会认错物体?",
                color = Color.Black,
                fontSize = 24.sp,
                fontFamily = YaHei,
                modifier = Modifier
                    .offset(x = 68.dp, y = 190.dp)
                    .size(width = 292.dp, height = 64.dp),
            )
        }
    }
}