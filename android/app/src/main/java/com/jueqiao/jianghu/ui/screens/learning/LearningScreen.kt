package com.jueqiao.jianghu.ui.screens.learning

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R

/**
 * 学习1 页 — 滚轮1 → 点击"修炼"按钮跳转目标。
 *
 * 布局:
 *   - 全屏背景图(Android Compact - 109.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,与后山页面一致)
 *   - 中央竖向元素(Group 281.png,X 轴居中, Y=124, W=83.76, H=563)
 */
@Composable
fun LearningScreen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(Android Compact - 109.png)
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
            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,与后山页面一致)— 点击回到滚轮1 页
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

            // 中央竖向元素(Group 281.png,X 轴居中, Y=124, W=83.76, H=563)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 124.dp)
                    .size(width = 83.76.dp, height = 563.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_group_281),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
