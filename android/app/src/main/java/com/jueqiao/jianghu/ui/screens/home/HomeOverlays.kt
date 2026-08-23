package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Progress modal — 我的学习进度 (372x660 centered card).
 */
@Composable
fun ProgressModal(
    onClose: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenLuggage: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 80.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .height(660.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .clickable(enabled = false) { /* swallow */ },
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "关闭",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onClose),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "我的学习进度",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AuthPalette.TextDark,
                    ),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp)) {
                        Image(
                            painter = painterResource(R.drawable.img_avatar_ring),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Image(
                            painter = painterResource(R.drawable.img_avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "名字:阿砚",
                            color = AuthPalette.TextDark,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                        Text(
                            text = "等级:见习弟子",
                            color = AuthPalette.TextDark,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "每日生活问题推荐:",
                    color = AuthPalette.TextDark,
                    textDecoration = TextDecoration.Underline,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier.clickable(onClick = onOpenDaily),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(1.dp, AuthPalette.DividerGray, RoundedCornerShape(8.dp)),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(1.dp, AuthPalette.DividerGray, RoundedCornerShape(8.dp)),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenLuggage),
                    ) {
                        Text(
                            text = "详情请查看行囊",
                            color = AuthPalette.TextDark,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(x = 20.dp, y = 30.dp)
                        .size(width = 223.dp, height = 244.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_corner_decor),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/**
 * Luggage page overlay — personal growth data.
 */
@Composable
fun LuggagePage(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.6f),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "关闭",
                    modifier = Modifier.size(28.dp).clickable(onClick = onClose),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "行囊",
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(670.dp)
                    .background(
                        color = Color(0xFFFAF0DA),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // User header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(70.dp)) {
                            Image(
                                painter = painterResource(R.drawable.img_avatar_ring),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Image(
                                painter = painterResource(R.drawable.img_avatar),
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp).fillMaxSize(),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "哈哈哈",
                                color = AuthPalette.TextDark,
                                style = TextStyle(
                                    fontFamily = YaHei,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                ),
                            )
                            Text(
                                text = "五（三）班  1326528988",
                                color = AuthPalette.TextDark,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 22.dp)
                                .background(Color(0xFFAACC99), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "见习弟子",
                                color = Color.White,
                                style = TextStyle(
                                    fontFamily = YaHei,
                                    fontSize = 10.sp,
                                ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "签到:3天  累计修行:7天  已通关试炼:5个",
                        color = AuthPalette.TextDark,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LuggageSection(title = "获得的秘籍", defaultOpen = true) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BookItem(R.drawable.img_book_1, "百炼识物诀")
                            BookItem(R.drawable.img_book_2, "四格漫画入门")
                            BookItem(R.drawable.img_book_3, "互评达人手册")
                        }
                    }
                    LuggageSection(title = "我的错题", defaultOpen = false) {
                        Text(
                            text = "暂无错题",
                            color = AuthPalette.TextDark.copy(alpha = 0.6f),
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                    }
                    LuggageSection(title = "我的作品", defaultOpen = false) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BookItem(R.drawable.img_work_1, "《我家乡的秋叶》")
                            BookItem(R.drawable.img_work_2, "《神奇的树叶》")
                        }
                    }

                    // Luggage panda
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_luggage_panda),
                            contentDescription = null,
                            modifier = Modifier.size(width = 100.dp, height = 130.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookItem(imageRes: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = name,
            modifier = Modifier.size(width = 60.dp, height = 80.dp),
        )
        Text(
            text = name,
            color = AuthPalette.TextDark,
            style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
        )
    }
}

@Composable
private fun LuggageSection(
    title: String,
    defaultOpen: Boolean = false,
    content: @Composable () -> Unit,
) {
    var open by remember { mutableStateOf(defaultOpen) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFFEBDCC3), RoundedCornerShape(8.dp))
            .clickable { open = !open }
            .padding(12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                )
                Image(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = if (open) "收起" else "展开",
                    modifier = Modifier
                        .size(14.dp)
                        .alpha(if (open) 1f else 0.6f),
                )
            }
            if (open) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}