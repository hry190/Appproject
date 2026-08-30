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

            // 底部装饰气泡(Rectangle 199.png, X=8, Y=564, 132×70,
            //   区域填充 #F4E6CF, 不透明度 62%,玻璃态。
            //   Figma 里的 glass 效果(light -45°/80%, refraction 80, depth 20,
            //   dispersion 50, frost 4)在 Compose 里没有直接对应,源 PNG 已自带
            //   米色玻璃气泡外观,这里只应用位置 / 尺寸 / 整体不透明度。)
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 564.dp)
                    .size(width = 132.dp, height = 70.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_rect199),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = 0.62f,
                )
                // 气泡内文案(Text, X=22, Y=568, 104×54, 14px, #000000, 不透明度 100%, weight=1 → Regular)
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
                        .offset(x = 14.dp, y = 4.dp)  // 22-8=14, 568-564=4(相对气泡 Box)
                        .size(width = 104.dp, height = 54.dp)
                        .padding(horizontal = 2.dp),
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }
}
