package com.jueqiao.jianghu.ui.screens.yanwuchang

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 演武场首页 — 已按其他页 safe-area 模式适配。
 *
 * 结构(两段式):
 *   - 外层 Box:全屏背景(img_yanwuchang_bg)+ 内容层
 *   - 内层 Box:windowInsetsPadding(navigationBars) 避开系统导航条
 *
 * 元素定位(BoxScope 相对定位,与设计稿 412×917 对应):
 *   - 顶部行:align(TopStart)+offset(X=20, Y=41)— 与其他页一致
 *   - 中部按钮(武会/作品):align(Center)+offset 居中再偏
 *   - 底部元素(气泡/熊猫):align(BottomStart/BottomCenter)+offset(y=-X)
 *     让它们跟着实际 safe-area 高度走,避免在矮屏被裁
 *
 * 坐标系参考(412×917 设计稿,48dp 系统导航条,safe-area 高度 869):
 *   - 中心 Y=534.5(武会)/ 539(作品),safe-area 中心 434.5,offset +100 / +104.5
 *   - 气泡底沿 634,距 safe-area 底 235
 *   - 熊猫底沿 859,距 safe-area 底 10
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
        // ===== Tier 1: 全屏背景(室内家园要求 1.png, 412×917)=====
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ===== Tier 2: 内容层(避开系统导航条)=====
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ----- 左上角返回按钮(32×32dp 容器,内含 24×24dp 图标)-----
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 41.dp)
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

            // ----- 右侧"武会"按钮(图 + 竖排文字)— 相对屏幕中心 -----
            // 原 (X=269, Y=487, W=45, H=95),中心 (291.5, 534.5)
            // 相对 safe-area 中心 (206, 434.5) 的偏移:(+85.5, +100)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_un50),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 85.5.dp, y = 100.dp)
                    .size(width = 45.dp, height = 95.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.7f,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 81.64.dp, y = 90.285.dp)
                    .size(width = 19.28.dp, height = 49.57.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("武", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
                Text("会", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
            }

            // ----- 左侧"作品"按钮(图 + 竖排文字,可点击)— 相对屏幕中心 -----
            // 原 (X=23, Y=492, W=44, H=94),中心 (45, 539)
            // 相对 safe-area 中心 (206, 434.5) 的偏移:(-161, +104.5)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_un50_1),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -161.dp, y = 104.5.dp)
                    .size(width = 44.dp, height = 94.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.7f,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -165.465.dp, y = 96.02.dp)
                    .size(width = 19.07.dp, height = 49.04.dp)
                    .clickable(onClick = onOpenYanwuchangVideo),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("作", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
                Text("品", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
            }

            // ----- 底部装饰气泡(Rectangle 199.png)— 相对 safe-area 底部 -----
            // 原 (X=8, Y=564, W=132, H=70),底沿 634
            // safe-area 底部 869,距底 235
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 8.dp, y = -235.dp)
                    .size(width = 132.dp, height = 70.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_rect199),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = 0.62f,
                )
                // 气泡内文案(原 X=22, Y=568, W=104, H=54, 14px)
                //   相对气泡 Box:(22-8, 568-564) = (14, 4)
                Text(
                    text = "这里有作品和比拼可供选择哦，快去看看吧！",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 14.sp,
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

            // ----- 角色插画(熊猫)— 相对 safe-area 底部 -----
            // 原 (X=82, Y=599, W=141, H=260),底沿 859,底中 (152.5, 859)
            // safe-area 底部 869,距底 10;水平居中偏移 206-152.5 = 53.5(向左)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_panda),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = -53.5.dp, y = -10.dp)
                    .size(width = 141.dp, height = 260.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
