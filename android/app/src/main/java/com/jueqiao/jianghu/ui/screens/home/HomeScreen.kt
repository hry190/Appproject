package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.SpeechBubble
import com.jueqiao.jianghu.ui.theme.AuthDimens

/**
 * Home guide — three-step chat with 阿砚 before entering the formal home screen.
 * Mirrors RN (tabs)/index.tsx.
 */
@Composable
fun HomeScreen(
    onOpenHome1: () -> Unit = {},
) {
    var chatStep by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val statusBarTop = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景独立铺满整个窗口，包含透明系统导航栏后方区域。
        HomeWindBackground(modifier = Modifier.fillMaxSize())

        // 固定设计画布只承载引导内容；较矮设备仍可滚动查看。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(AuthDimens.homeCanvasH)) {
                // 内容层(避开系统导航条)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                ) {
                // Panda mascot
                val pandaRes = if (chatStep >= 2) {
                    R.drawable.img_home_panda_2
                } else {
                    R.drawable.img_home_panda_1
                }
                Image(
                    painter = painterResource(pandaRes),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(
                            x = HomePandaLayout.X,
                            y = HomePandaLayout.Y + statusBarTop,
                        )
                        .size(
                            width = HomePandaLayout.Width,
                            height = HomePandaLayout.Height,
                        ),
                    contentScale = ContentScale.Fit,
                )

                // 4 Banners (only when chatStep >= 3 and not in modal/luggage)
                // BannerRow removed — no decorative banners on home page

                // Welcome speech bubble (chatStep 1..2)
                if (chatStep in 1..2) {
                    // 更激进的滤镜：只保留非常浅的米色（亮度 > 0.85），
                    // 把深米色"箭头"、黑色描边等都变透明
                    val removeBlackFilter = ColorFilter.colorMatrix(
                        ColorMatrix(
                            floatArrayOf(
                                1f, 0f, 0f, 0f, 0f,         // R 保持
                                0f, 1f, 0f, 0f, 0f,         // G 保持
                                0f, 0f, 1f, 0f, 0f,         // B 保持
                                0f, 0f, 0f, 0f, 0.85f,      // A = 0.85（几乎全透明，仅最浅色保留）
                            )
                        )
                    )
                    SpeechBubble(
                        text = if (chatStep == 1)
                            "hi，欢迎来到机巧江湖"
                        else
                            "在我身后有三个奇妙去处哦！\n修炼场，可以完成互动试炼，解锁神秘秘籍；\n大会，同伴互评空间，锻炼思考能力；\n作品创作，辅助学生进行 AI 创作；\n哦差点忘了，行囊，可以查看收获的成果哦。\n聪明的你，已经迫不及待准备出发了吧，我们一起开始冒险吧！",
                        bubbleImageRes = if (chatStep == 1)
                            R.drawable.img_bubble_chat1_bg
                        else
                            R.drawable.img_bubble_chat2_bg,
                        bubbleColor = if (chatStep == 2) Color(0xFFC3BCA5) else Color.Transparent,
                        cornerRadius = if (chatStep == 1) 24.dp else 40.dp,
                        imageColorFilter = if (chatStep == 2) removeBlackFilter else null,
                        fontSize = 12.sp,
                        modifier = if (chatStep == 2)
                            Modifier
                                .offset(x = 16.dp, y = 250.dp)
                                .size(width = 320.dp, height = 170.dp)
                        else
                            Modifier
                                .offset(x = 29.dp, y = 363.dp)
                                .size(width = 195.dp, height = 74.dp),
                    )
                }

                // Tappable empty area for chatStep advance
                if (chatStep < 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (chatStep < 2) {
                                    chatStep = (chatStep + 1).coerceAtMost(2)
                                } else {
                                    // 聊天气泡播完(chatStep == 2),点击跳转 Home1
                                    onOpenHome1()
                                }
                            },
                    )
                }

                }
            }
        }
    }
}
