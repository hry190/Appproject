package com.jueqiao.jianghu.ui.screens.yanwuchang

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 演武场首页 — 简单版(用 室内家园要求 1.png 作全屏背景 + 左上返回按钮)。
 * 背景图原始尺寸 412×917,使用 ContentScale.Crop 适配任意屏幕。
 */
@Composable
fun YanwuchangScreen(
    onBack: () -> Unit = {},
    onOpenYanwuchangVideo: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到大会页)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(室内家园要求 1.png, 412×917)— 延伸到屏幕底部
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_bg),
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
            // 左上角返回按钮
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 76.dp)
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_dahui_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // 角色插画(未标题-1 - 副本 (3) 4.png, X=82, Y=599, W=141, H=260)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_panda),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 82.dp, y = 599.dp)
                    .size(width = 141.dp, height = 260.dp),
                contentScale = ContentScale.Fit,
            )

            // 右侧按钮(未标题-1 50.png, X=269, Y=487, 45×95, 不透明度 70%)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_un50),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 269.dp, y = 487.dp)
                    .size(width = 45.dp, height = 95.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.7f,
            )

            // 右侧图标内"武会"竖排文字(X=278, Y=500, 19.28×49.57, 14px, #FFFFFF, 不透明度 100%)
            Column(
                modifier = Modifier
                    .offset(x = 278.dp, y = 500.dp)
                    .size(width = 19.28.dp, height = 49.57.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("武", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
                Text("会", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
            }

            // 左侧按钮(未标题-1 50 (1).png, X=23, Y=492, 44×94, 不透明度 70%)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_un50_1),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 23.dp, y = 492.dp)
                    .size(width = 44.dp, height = 94.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.7f,
            )

            // 左侧图标内"作品"竖排文字(X=31, Y=506, 19.07×49.04, 14px, #FFFFFF, 不透明度 100%)
            //   点击进入演武场视频首页
            Column(
                modifier = Modifier
                    .offset(x = 31.dp, y = 506.dp)
                    .size(width = 19.07.dp, height = 49.04.dp)
                    .clickable(onClick = onOpenYanwuchangVideo),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("作", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
                Text("品", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
            }

            // 底部装饰气泡(与"大会"页面气泡同款填充色:
            //   R.drawable.img_dahui_speech_bubble,
            //   X=8, Y=564, 132×70, 不透明度 100%, ContentScale.FillBounds
            //   原本使用的 img_yanwuchang_rect199(米色玻璃 62%)与大会气泡颜色不一致,
            //   故改为与大会同源的图片,保持视觉统一)
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 564.dp)
                    .size(width = 132.dp, height = 70.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_dahui_speech_bubble),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = 1f,
                )
                // 气泡内文案(Text, 相对气泡 Box 偏移 (14, 4), 104×54, 14px, #000000, 不透明度 100%, weight=1 → Regular)
                //   - lineHeight = 16.sp:3 行 ≈ 48dp,稳妥落在 54dp 容器内
                //   - softWrap = true + overflow = Visible:不裁字、允许自动换行不超界
                //   - 容器内左右各留 2dp padding,字符不会贴边
                Text(
                    text = "这里有作品和比拼可供选择哦，快去看看吧！",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize   = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = Modifier
                        .offset(x = 14.dp, y = 4.dp)
                        .size(width = 104.dp, height = 54.dp)
                        .padding(horizontal = 2.dp),
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }
}
