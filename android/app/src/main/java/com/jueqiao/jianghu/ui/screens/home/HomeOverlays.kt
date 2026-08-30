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
import androidx.compose.ui.draw.clip
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
                .padding(top = 130.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .height(660.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable(enabled = false) { /* swallow */ },
        ) {
            // 背景图(image 13.png — 372x579)
            Image(
                painter = painterResource(R.drawable.img_progress_modal_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
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
            }
            // 竹叶贴图：放在 Column 之外，渲染在最上层
            Box(
                modifier = Modifier
                    .offset(x = 169.dp, y = 435.dp)
                    .size(width = 262.dp, height = 244.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_corner_decor),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.3f),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter,
                )
            }
        }
    }
}

