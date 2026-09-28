package com.jueqiao.jianghu.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
 * 滚轮1-5 页面共用的脚手架:
 * - 全屏背景(img_gunlun1_bg)
 * - 熊猫打坐图像(X=70, Y=354, 257x457)— 下移以消化素材底部透明留白并贴近石台
 * - 左上角返回按钮与修炼页一致（24x24 图标位置不变，点击框扩为 48x48）
 *
 * 调用方只负责在 `content` 槽里放各页独有的元素(书本、介绍、气泡等)。
 *
 * @param onPandaClick 熊猫点击回调。null = 熊猫不可点击(用于滚轮1/2/5 这类叶子页)。
 *                    非 null 时熊猫加上 .clickable 修饰符,点击触发此回调。
 */
@Composable
fun StandardGunlunScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPandaClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(滚轮.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun1_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            val pandaScale = minOf(
                maxWidth.value / 400f,
                maxHeight.value / 820f,
                1f,
            )
            // 熊猫打坐图像
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_1_recovered_5),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 70.dp * pandaScale, y = 354.dp * pandaScale)
                    .size(width = 257.dp * pandaScale, height = 457.dp * pandaScale)
                    .let { if (onPandaClick != null) it.clickable(onClick = onPandaClick) else it },
                contentScale = ContentScale.FillBounds,
            )

            // 各页独有的元素(书本、介绍、气泡等)— 由调用方提供
            content()

            // 保留用户已调整的图标视觉中心，只将外层点击区扩为 48dp。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp, y = 42.dp)
                    .size(48.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_xiulian_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
