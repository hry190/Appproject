package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.YaHei

private val HomeGuideMessages = listOf(
    "嗨，欢迎来到机巧江湖！",
    "在我身后，\n有三个奇妙去处哦！",
    "修炼场：完成互动试炼，\n解锁神秘秘籍。",
    "大会：和同伴互评，\n一起锻炼思考能力。",
    "作品创作：借助 AI，\n把灵感变成作品。",
    "对了，还有行囊，\n可以随时查看你的收获。",
    "准备好了吗？\n我们一起开始冒险吧！",
)

/**
 * Home guide — tap-through chat with 阿砚 before entering the formal home screen.
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
                // Panda mascot. Only the two existing poses cross-fade; its layout stays fixed.
                Crossfade(
                    targetState = chatStep >= 2,
                    animationSpec = tween(durationMillis = 320),
                    label = "home guide panda pose",
                    modifier = Modifier
                        .offset(
                            x = HomePandaLayout.X,
                            y = HomePandaLayout.Y + statusBarTop,
                        )
                        .size(
                            width = HomePandaLayout.Width,
                            height = HomePandaLayout.Height,
                        ),
                ) { isPresenting ->
                    Image(
                        painter = painterResource(
                            if (isPresenting) {
                                R.drawable.img_home_panda_2
                            } else {
                                R.drawable.img_home_panda_1
                            },
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }

                // 4 Banners (only when chatStep >= 3 and not in modal/luggage)
                // BannerRow removed — no decorative banners on home page

                // One compact bubble at a time; the full-screen guide layer advances the dialogue.
                AnimatedContent(
                    targetState = chatStep,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(
                            y = HomePandaLayout.Y + statusBarTop - 116.dp,
                        )
                        .size(width = 280.dp, height = 118.dp),
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (fadeIn(tween(durationMillis = 220)) +
                            slideInVertically(tween(durationMillis = 220)) { height -> height / 10 })
                            .togetherWith(fadeOut(tween(durationMillis = 140)))
                    },
                    label = "home guide bubble",
                ) { step ->
                    if (step in 1..HomeGuideMessages.size) {
                        HomeGuideBubble(
                            text = HomeGuideMessages[step - 1],
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // The entire guide advances the dialogue; the bubble is visual only.
                if (chatStep in 0..HomeGuideMessages.size) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "继续引导",
                            ) {
                                when {
                                    chatStep == 0 -> chatStep = 1
                                    chatStep < HomeGuideMessages.size -> chatStep += 1
                                    else -> onOpenHome1()
                                }
                            },
                    )
                }

                }
            }
        }
    }
}

@Composable
private fun HomeGuideBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.img_home_guide_bubble_left_tail),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            // The source canvas carries transparent breathing room above and below the artwork.
            // Cropping to the visible frame preserves its intended wide scroll-like proportion.
            contentScale = ContentScale.Crop,
            alpha = 0.90f,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 46.dp,
                    vertical = 29.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color(0xFF2F3C25),
                    textAlign = TextAlign.Center,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}
