package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.components.SpeechBubble
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Home screen — chat with 阿砚 + 4 banners + 4 quick actions + overlays.
 * Mirrors RN (tabs)/index.tsx.
 */
@Composable
fun HomeScreen(
    onOpenHome1: () -> Unit = {},
    onOpenLuggage: () -> Unit = {},
) {
    var chatStep by remember { mutableStateOf(0) }
    var taskExpanded by remember { mutableStateOf(false) }
    var progressOpen by remember { mutableStateOf(false) }
    var dailyOpen by remember { mutableStateOf(false) }
    var dailyStep by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Canvas (412 x 810)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(AuthDimens.homeCanvasH)) {
                // Background
                Image(
                    painter = painterResource(R.drawable.img_home_bg),
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
                // 4 Quick actions (top right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 71.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_works,
                        label = "作品",
                        onClick = { /* TODO */ },
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_progress,
                        label = "进度",
                        onClick = { progressOpen = true },
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_task,
                        label = "任务",
                        onClick = { taskExpanded = !taskExpanded },
                        showDot = true,
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_settings,
                        label = "设置",
                        onClick = { /* TODO */ },
                    )
                }

                // Task dropdown panel
                if (taskExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-46).dp, y = 130.dp)
                            .size(width = 40.dp, height = 36.dp)
                            .background(Color.Transparent),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_task_panel_bg),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().alpha(0.8f),
                            contentScale = ContentScale.FillBounds,
                        )
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "挑战",
                                color = AuthPalette.TextDark,
                                style = TextStyle(
                                    fontFamily = YaHei,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_chevron_down),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                }

                // Panda mascot
                val pandaRes = when {
                    dailyOpen -> R.drawable.img_home_panda_daily
                    chatStep >= 2 -> R.drawable.img_home_panda_2
                    else -> R.drawable.img_home_panda_1
                }
                Image(
                    painter = painterResource(pandaRes),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 107.dp, y = 473.dp)
                        .size(width = 200.dp, height = 276.dp),
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

                // Daily-question bubble
                if (dailyOpen) {
                    Box(
                        modifier = Modifier
                            .offset(x = 80.dp, y = 280.dp)
                            .size(width = 250.dp, height = 110.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable {
                                if (dailyStep == 1) dailyStep = 2 else dailyOpen = false
                            }
                            .padding(12.dp),
                    ) {
                        Text(
                            text = if (dailyStep == 1)
                                "...去找找秘籍，看看有没有答案"
                            else
                                "生活问题推荐:\n机器人为什么会认错物体?",
                            color = AuthPalette.TextDark,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                    }
                }

                // Tappable empty area for chatStep advance
                if (chatStep < 3 && !dailyOpen) {
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

                // Overlays
                if (progressOpen) {
                    ProgressModal(
                        onClose = { progressOpen = false },
                        onOpenDaily = { dailyOpen = true; progressOpen = false },
                        onOpenLuggage = onOpenLuggage,
                    )
                }
                }
            }
        }
    }
}