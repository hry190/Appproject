package com.jueqiao.jianghu.ui.screens.unfinished

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R

/**
 * 未完待续 页。
 *
 * 布局:
 *   - 全屏背景图(image 129.png)
 *   - 图像 image 134.png(X=-1, Y=40, W=954, H=784)— 书框
 *   - 图像 Android Compact - 124.png(全屏,书框上层)
 *   - 熊猫图像 image 307.png(X=-7, Y=563, W=250, H=330)
 *   - "未完待续"图像(未 完 待 续.png,X=158, Y=168, W=96, H=464,Figma 滤镜渲染)
 */
@Composable
fun UnfinishedScreen(
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

        // "未完待续"图像(未 完 待 续.png,X=158, Y=168, W=96, H=464,Figma 滤镜渲染)— 点击跳转到滚轮1
        Image(
            painter = painterResource(R.drawable.img_unfinished_text),
            contentDescription = "未完待续",
            modifier = Modifier
                .offset(x = 152.dp, y = 140.dp)
                .size(width = 96.dp, height = 464.dp)
                .clickable(onClick = onOpenGunlun1),
            contentScale = ContentScale.FillBounds,
        )
    }
}
