package com.jueqiao.jianghu.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningBookDto
import com.jueqiao.jianghu.luggage.LuggageResponseDto
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 修为只展示真实学习数据的简短概览；完整证据仍由现有行囊页面负责。
 * 卡片高度由内容和安全区共同决定，小窗口时正文滚动而不是缩小字号。
 */
@Composable
fun ProgressModal(
    onClose: () -> Unit,
    onOpenLuggage: () -> Unit,
    onOpenRecommendedManual: (String) -> Unit,
    snapshot: LuggageResponseDto?,
    recommendation: LearningBookDto?,
    loading: Boolean,
    message: String?,
) {
    BackHandler(onBack = onClose)
    val cardBackgroundPainter = painterResource(R.drawable.img_progress_modal_bg)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(onClose) { detectTapGestures { onClose() } },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val availableCardHeight = maxHeight
            val useScrollableViewport = maxHeight < 500.dp
            Box(
                modifier = Modifier
                    .widthIn(max = 372.dp)
                    .fillMaxWidth()
                    .heightIn(max = availableCardHeight)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .drawWithCache {
                        val source = cardBackgroundPainter.intrinsicSize
                        val target = if (source.width > 0f && source.height > 0f) {
                            val scale = maxOf(size.width / source.width, size.height / source.height)
                            Size(source.width * scale, source.height * scale)
                        } else {
                            size
                        }
                        val left = (size.width - target.width) / 2f
                        val top = (size.height - target.height) / 2f
                        onDrawBehind {
                            translate(left = left, top = top) {
                                with(cardBackgroundPainter) { draw(size = target) }
                            }
                        }
                    }
                    .pointerInput(Unit) { detectTapGestures { /* consume card taps */ } },
            ) {
                Image(
                    painter = painterResource(R.drawable.img_corner_decor),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(width = 156.dp, height = 144.dp)
                        .alpha(0.18f),
                    contentScale = ContentScale.Crop,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (useScrollableViewport) {
                                Modifier
                                    .heightIn(max = availableCardHeight)
                                    .verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "关闭修为概览"
                                    role = Role.Button
                                }
                                .clickable(onClick = onClose),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            text = "我的修为",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = YaHei,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = AuthPalette.TextDark,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
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
                        Text(
                            text = snapshot?.data?.profile?.nickname
                                ?.takeIf(String::isNotBlank)
                                ?: "学习记录",
                            color = AuthPalette.TextDark,
                            style = TextStyle(
                                fontFamily = YaHei,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    val data = snapshot?.data
                    when {
                        data != null &&
                            data.stats.week.practiceCount == 0 &&
                            data.manuals.obtained == 0 -> {
                            BodyText("完成一次修炼后，这里会出现学习记录")
                        }
                        data != null -> {
                            BodyText("本周修炼：${data.stats.week.practiceCount} 次")
                            Spacer(modifier = Modifier.height(6.dp))
                            BodyText("已获得秘籍：${data.manuals.obtained} 本")
                        }
                        loading -> BodyText("正在整理学习记录…")
                        !message.isNullOrBlank() -> BodyText("学习记录暂时未同步")
                        else -> BodyText("完成一次修炼后，这里会出现学习记录")
                    }

                    message?.takeIf(String::isNotBlank)?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        BodyText(it)
                    }

                    recommendation?.let { manual ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .semantics {
                                    contentDescription = "打开推荐秘籍：${manual.title}"
                                    role = Role.Button
                                }
                                .clickable {
                                    onOpenRecommendedManual(manual.manualPageId)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "推荐：${manual.title}",
                                color = AuthPalette.TextDark,
                                textDecoration = TextDecoration.Underline,
                                style = TextStyle(
                                    fontFamily = YaHei,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = "前往行囊查看完整学习记录"
                                role = Role.Button
                            }
                            .clickable(onClick = onOpenLuggage)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "详情请查看行囊",
                            color = AuthPalette.TextDark,
                            style = TextStyle(
                                fontFamily = YaHei,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            ),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        color = AuthPalette.TextDark,
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        ),
    )
}
