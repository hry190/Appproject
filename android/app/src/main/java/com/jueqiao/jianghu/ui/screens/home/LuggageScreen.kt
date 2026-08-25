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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 行囊页 (首页弹窗) — 完整页面化的"我的行囊"。
 * 基于 Figma 设计 node-id=397-2400 实现。
 * 4 个快捷图标（设置/任务/进度/作品）+ 关闭按钮 + 用户信息卡片 + 三个可折叠区域。
 */
@Composable
fun LuggageScreen(
    onBack: () -> Unit = {},
    onOpenXiulian: () -> Unit = {},
    onOpenXingnang: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
) {
    var booksOpen by remember { mutableStateOf(true) }
    var errorsOpen by remember { mutableStateOf(false) }
    var worksOpen by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景(竹林)
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 顶部右侧 4 个快捷图标(从左到右:作品/进度/任务/设置)
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
                onClick = onOpenProgress,
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_task,
                label = "任务",
                onClick = { /* TODO */ },
            )
            QuickActionItem(
                iconRes = R.drawable.img_icon_settings,
                label = "设置",
                onClick = { /* TODO */ },
            )
        }

        // 主卡片背景(水平居中,顶部 117dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
                .size(width = 372.dp, height = 1000.dp)
                .clip(RoundedCornerShape(14.dp)),
        ) {
            // 卡片底图
            Image(
                painter = painterResource(R.drawable.img_luggage_card_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // 关闭按钮 (卡片左上角 ✕ — 下移避开图片顶部透明区)
            Box(
                modifier = Modifier
                    .offset(x = 15.dp, y = 58.dp)
                    .size(15.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "关闭",
                    modifier = Modifier.size(15.dp),
                )
            }

            // 标题 "行 囊" (顶部居中 — 下移避开图片顶部透明区)
            Text(
                text = "行 囊",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 24.sp,
                    letterSpacing = 24.sp,
                ),
                modifier = Modifier.offset(x = 150.dp, y = 56.dp),
            )

            // ===== 用户信息区 (顶部) — 全部下移 30dp,给标题和按钮留空间 =====
            // 头像圆框
            Image(
                painter = painterResource(R.drawable.img_avatar_ring),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 20.dp, y = 80.dp)
                    .size(width = 76.dp, height = 72.dp),
                contentScale = ContentScale.Fit,
            )
            // 头像(熊猫脸)
            Image(
                painter = painterResource(R.drawable.img_avatar),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 29.dp, y = 86.dp)
                    .size(width = 60.dp, height = 60.dp),
                contentScale = ContentScale.Crop,
            )
            // 姓名
            Text(
                text = "哈哈哈",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                modifier = Modifier.offset(x = 106.dp, y = 86.dp),
            )
            // 班级
            Text(
                text = "五（三）班",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                modifier = Modifier.offset(x = 174.dp, y = 89.dp),
            )
            // ID
            Text(
                text = "ID：1326528988",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                modifier = Modifier.offset(x = 106.dp, y = 111.dp),
            )
            // 见习弟子 徽章背景(terw.png)
            Box(
                modifier = Modifier
                    .offset(x = 98.dp, y = 136.dp)
                    .size(width = 83.dp, height = 21.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_level_badge_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "见习弟子",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                )
            }
            // 勋章文字
            Text(
                text = "勋章：",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                modifier = Modifier.offset(x = 192.dp, y = 136.dp),
            )
            // 勋章图标(原占位图) — 下移 30dp
            Image(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = "已获得勋章",
                modifier = Modifier
                    .offset(x = 224.dp, y = 135.dp)
                    .size(20.dp),
            )

            // ===== 统计行 — 下移 30dp =====
            Text(
                text = "签到：3天       累计修行：7天       已通关试炼:5个",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.offset(x = 20.dp, y = 165.dp),
            )

            // ===== 1. 获得的秘籍 — 下移 30dp =====
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 203.dp)
                    .size(width = 332.dp, height = 250.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEBDCC3))
                    .clickable { booksOpen = !booksOpen },
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "获得的秘籍：",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = YaHei,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_down),
                            contentDescription = if (booksOpen) "收起" else "展开",
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = 309.dp),
                        )
                    }
                    if (booksOpen) {
                        // 3 本秘籍
                        Column(modifier = Modifier.offset(y = 26.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(horizontal = 7.dp),
                            ) {
                                BookItem(R.drawable.img_book_1, "《拆招心法》")
                                BookItem(R.drawable.img_book_2, "《识机真诀》")
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                            BookItem(R.drawable.img_book_3, "《百炼识物诀》")
                        }
                    }
                }
            }

            // ===== 2. 我的错题 — 下移 30dp =====
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 463.dp)
                    .size(width = 332.dp, height = 35.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF8E9A75).copy(alpha = 0.66f))
                    .clickable { errorsOpen = !errorsOpen },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "我的错题：",
                        color = Color.Black,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        ),
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = if (errorsOpen) "收起" else "展开",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // ===== 3. 我的作品 — 下移 30dp =====
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 508.dp)
                    .size(width = 332.dp, height = 145.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEBDCC3))
                    .clickable { worksOpen = !worksOpen },
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "我的作品：",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = YaHei,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_down),
                            contentDescription = if (worksOpen) "收起" else "展开",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    if (worksOpen) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            modifier = Modifier
                                .offset(y = 26.dp)
                                .padding(horizontal = 3.dp),
                        ) {
                            WorkItem(R.drawable.img_work_1, w = 72.dp, h = 95.dp)
                            WorkItem(R.drawable.img_work_2, w = 86.dp, h = 92.dp)
                        }
                    }
                }
            }

            // 熊猫角色(右下角,相对卡片定位) — 下移 30dp
            Image(
                painter = painterResource(R.drawable.img_luggage_panda),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 285.dp, y = 472.dp)
                    .size(width = 112.dp, height = 233.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun BookItem(imageRes: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = name,
            modifier = Modifier.size(width = 47.dp, height = 42.dp),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = name,
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
        )
    }
}

@Composable
private fun WorkItem(imageRes: Int, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier.size(width = w, height = h),
        contentScale = ContentScale.Crop,
    )
}