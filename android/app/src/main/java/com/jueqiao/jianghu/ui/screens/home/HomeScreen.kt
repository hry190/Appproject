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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.DecorBanner
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
    onOpenXiulian: () -> Unit,
    onOpenXingnang: () -> Unit,
    onOpenZaowu: () -> Unit,
    onOpenDahui: () -> Unit,
) {
    var chatStep by remember { mutableStateOf(0) }
    var taskExpanded by remember { mutableStateOf(false) }
    var progressOpen by remember { mutableStateOf(false) }
    var dailyOpen by remember { mutableStateOf(false) }
    var dailyStep by remember { mutableStateOf(1) }
    var luggageOpen by remember { mutableStateOf(false) }

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

                // 4 Quick actions (top right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 71.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_settings,
                        label = "设置",
                        onClick = { /* TODO */ },
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_task,
                        label = "任务",
                        onClick = { taskExpanded = !taskExpanded },
                        showDot = true,
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_progress,
                        label = "进度",
                        onClick = { progressOpen = true },
                    )
                    QuickActionItem(
                        iconRes = R.drawable.img_icon_works,
                        label = "作品",
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
                if (chatStep >= 3 && !progressOpen && !luggageOpen) {
                    BannerRow(
                        onOpenXingnang = { luggageOpen = true },
                        onOpenXiulian = onOpenXiulian,
                        onOpenDahui = onOpenDahui,
                        onOpenZaowu = onOpenZaowu,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(y = (-24).dp),
                    )
                }

                // Welcome speech bubble (chatStep 1..2)
                if (chatStep in 1..2) {
                    SpeechBubble(
                        text = if (chatStep == 1)
                            "hi，欢迎来到机巧江湖"
                        else
                            "在我身后有三个奇妙去处，先跟我聊聊你今天想做什么？",
                        modifier = Modifier
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
                                chatStep = (chatStep + 1).coerceAtMost(3)
                            },
                    )
                }

                // Overlays
                if (progressOpen) {
                    ProgressModal(
                        onClose = { progressOpen = false },
                        onOpenDaily = { dailyOpen = true; progressOpen = false },
                        onOpenLuggage = { luggageOpen = true; progressOpen = false },
                    )
                }
                if (luggageOpen) {
                    LuggagePage(onClose = { luggageOpen = false })
                }
            }
        }
    }
}

@Composable
private fun BannerRow(
    onOpenXingnang: () -> Unit,
    onOpenXiulian: () -> Unit,
    onOpenDahui: () -> Unit,
    onOpenZaowu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DecorBanner(
            imageRes = R.drawable.img_decor_xingnang,
            text = "行囊",
            onClick = onOpenXingnang,
        )
        DecorBanner(
            imageRes = R.drawable.img_decor_xiulian,
            text = "修炼",
            onClick = onOpenXiulian,
        )
        DecorBanner(
            imageRes = R.drawable.img_decor_dahui,
            text = "大会",
            onClick = onOpenDahui,
        )
        DecorBanner(
            imageRes = R.drawable.img_decor_zaowu,
            text = "造物",
            onClick = onOpenZaowu,
        )
    }
}