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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 后山1 页 — 滚轮1 → 点击"后山"按钮跳转目标。
 *
 * 布局:
 *   - 全屏背景图(试炼.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)
 *   - 熊猫图像(image 75.png,X=184, Y=621, W=210, H=192)
 *   - 标签1 图像(X=-13, Y=570, W=106, H=188)+ 文字"识机真决"(父 Box 内 X=46, Y=54, W=14, H=80)+ 文字"炼"(父 Box 内 X=48, Y=27, W=12, H=16)
 *   - 标签2 图像(X=168, Y=345, W=74, H=131)+ 文字"拆招心法"(父 Box 内 X=32, Y=34, W=14, H=80)+ 文字"炼"(父 Box 内 X=32, Y=17, W=12, H=16)
 *   - 标签3 图像(X=113, Y=322, W=50, H=88)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14)
 *   - 标签4 图像(X=151, Y=248, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14)
 *   - 气泡 Rectangle156.png(X=136, Y=508, W=177, H=107)+ 文字"御剑穿行..."
 */
@Composable
fun ShilianScreen(
    onBack: () -> Unit = {},
    onOpenShilian2: () -> Unit = {},
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
            // 熊猫图像(image 75.png,X=184, Y=621, W=210, H=192)
            Image(
                painter = painterResource(R.drawable.img_shilian_panda),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 184.dp, y = 621.dp)
                    .size(width = 210.dp, height = 192.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "标签1" 图像(未标题-1-恢复的-恢复的 4.png,X=-13, Y=570, W=106, H=188)
            Box(
                modifier = Modifier
                    .offset(x = -13.dp, y = 570.dp)
                    .size(width = 106.dp, height = 188.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "识机真决" 竖排文字(父 Box 内 X=46, Y=54, W=14, H=80, 字号 14, 黑色, YaHei)
                Text(
                    text = "识\n机\n真\n决",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 46.dp, y = 54.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=48, Y=27, W=12, H=16, 字号 12, 颜色 #385816, YaHei)
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 48.dp, y = 27.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=168, Y=345, W=74, H=131)
            Box(
                modifier = Modifier
                    .offset(x = 168.dp, y = 345.dp)
                    .size(width = 74.dp, height = 131.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "拆招心法" 竖排文字(父 Box 内 X=32, Y=34, W=14, H=80, 字号 12, 黑色, YaHei)
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 34.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=32, Y=17, W=12, H=16, 字号 10, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 32.dp, y = 17.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=113, Y=322, W=50, H=88)
            Box(
                modifier = Modifier
                    .offset(x = 113.dp, y = 322.dp)
                    .size(width = 50.dp, height = 88.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=151, Y=248, W=30, H=53.5)
            Box(
                modifier = Modifier
                    .offset(x = 151.dp, y = 248.dp)
                    .size(width = 30.dp, height = 53.5.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置与标签1 一致
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // Rectangle156.png 气泡(X=136, Y=508, W=177, H=107)— 点击跳转到试炼2 页
            Box(
                modifier = Modifier
                    .offset(x = 136.dp, y = 508.dp)
                    .size(width = 177.dp, height = 107.dp)
                    .clickable(onClick = onOpenShilian2),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_rect156),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // 气泡文本(字号 14, 黑色, YaHei)
                Text(
                    text = "御剑穿行云雾群山，\n每一座山峰皆是试炼。来，选一座山峰，开启你的修行试炼！",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到修炼页
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
        }
    }
}
