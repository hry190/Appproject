package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R

/**
 * 大会页 — 简单版(用 hygu.png 作全屏背景 + 左上返回按钮)。
 */
@Composable
fun DahuiScreen(
    onBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(hygu.png)
        Image(
            painter = painterResource(R.drawable.img_dahui_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 熊猫角色(Group 127.png,X=150, Y=597, 241×285)
        Image(
            painter = painterResource(R.drawable.img_dahui_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 150.dp, y = 597.dp)
                .size(width = 241.dp, height = 285.dp),
            contentScale = ContentScale.Fit,
        )

        // 对话气泡(Rectangle 186.png,X=56, Y=563, 158×80)
        Image(
            painter = painterResource(R.drawable.img_dahui_speech_bubble),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 56.dp, y = 563.dp)
                .size(width = 158.dp, height = 80.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 左上角返回按钮
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 76.dp)
                .size(32.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
