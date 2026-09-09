package com.jueqiao.jianghu.ui.screens.houshan3

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;点击标签2-4 之外的空白区域跳转未完待续页。
 *
 * 布局:
 *   - 全屏背景图(后山3 转换.png)
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 屏幕空白点击无效
 *   - 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)
 *   - 标签2 图像(X=124, Y=521, W=96, H=170)+ 文字"拆招心法"(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14)+ 文字"炼"(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12)
 *   - 标签3 图像(X=43, Y=390, W=51, H=91)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6)
 *   - 标签4 图像(X=105, Y=295, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4)
 *   - 云朵(Ellipse 58.png,X=-46, Y=476, W=331, H=92)
 */
@Composable
fun Houshan3Screen(
    onBack: () -> Unit = {},
    onOpenUnfinished: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(后山3 转换.png)
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)— 标签2/3/4 区域不消费点击(让父 Box 接收,触发跳未完待续)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .clickable(onClick = onOpenUnfinished),
        ) {
            // 云朵(Ellipse 58.png,X=-46, Y=476, W=331, H=92)— 在熊猫上层
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud),
                contentDescription = "云朵",
                modifier = Modifier
                    .offset(x = (-46).dp, y = 476.dp)
                    .size(width = 331.dp, height = 92.dp),
                contentScale = ContentScale.FillBounds,
            )

            // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 在云朵下层
            Image(
                painter = painterResource(R.drawable.img_shilian2_recovered_8),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 118.dp, y = 405.dp)
                    .size(width = 181.dp, height = 96.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=43, Y=390, W=51, H=91)
            Box(
                modifier = Modifier
                    .offset(x = 43.dp, y = 390.dp)
                    .size(width = 51.dp, height = 91.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6, 颜色 #385816, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=105, Y=295, W=30, H=53.5)
            Box(
                modifier = Modifier
                    .offset(x = 105.dp, y = 295.dp)
                    .size(width = 30.dp, height = 53.5.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=124, Y=521, W=96, H=170)
            Box(
                modifier = Modifier
                    .offset(x = 124.dp, y = 521.dp)
                    .size(width = 96.dp, height = 170.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "拆招心法" 竖排文字(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14, 黑色, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 48.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12, 颜色 #385816, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 25.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 点击回到后山2 页
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
