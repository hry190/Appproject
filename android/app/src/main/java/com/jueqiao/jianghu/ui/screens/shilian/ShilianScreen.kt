package com.jueqiao.jianghu.ui.screens.shilian

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
 * 试炼页 — 修炼页 → 点击"试炼"文本跳转目标。
 *
 * 布局:
 *   - 全屏背景图(试炼.png)
 *   - 左上角返回按钮(Return.png,X=3, Y=7, W=18, H=13)
 *   - 熊猫图像(image 75.png,X=194, Y=651, W=210, H=192)
 */
@Composable
fun ShilianScreen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(试炼.png)
        Image(
            painter = painterResource(R.drawable.img_shilian_bg),
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
            // 熊猫图像(image 75.png,X=194, Y=651, W=210, H=192)
            Image(
                painter = painterResource(R.drawable.img_shilian_panda),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 194.dp, y = 651.dp)
                    .size(width = 210.dp, height = 192.dp),
                contentScale = ContentScale.FillBounds,
            )

            // 左上角返回按钮(Return.png,X=3, Y=7, W=18, H=13)— 点击回到修炼页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 3.dp, y = 7.dp)
                    .size(width = 18.dp, height = 13.dp)
                    .clickable(onClick = onBack),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
