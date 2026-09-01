package com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlin.math.PI

/**
 * 演武场视频评论页 — 点击演武场视频首页的"评论"图标进入
 *
 * 布局(Figma 设计稿 412×917 基准):
 *   - 全屏背景:室内家园要求 1.png(img_yanwuchang_bg, 412×917, ContentScale.Crop)
 *   - 视频缩略图:image 24.png(img_yanwuchang_video_comment_image24, 198×347)
 *       位置 (107, 56),尺寸 198×347dp
 *       关闭动画中会从 198×347 放大到 412×917 (铺满全屏)
 *   - 评论框:评论框.png(img_yanwuchang_video_comment_box)
 *       位置 (0, 394),尺寸 417×523dp — 关闭动画中向下缩消失
 *   - 顶部两个图标(可点击):
 *       Group 141.png @ (17, 419), 17×17dp — 点击关闭本页(下缩动画)
 *       放大缩小.png @ (54, 419), 17×17dp
 *   - 评论区(Y=440 起到评论框底 Y=917):可上下滑动的评论列表
 *       每条评论右侧爱心可点击:
 *         - 未点赞:空心 ic_like_outline
 *         - 已点赞:实心 ic_like_filled,填充色 #6D8470, 不透明度 100%
 *         - 数字 +1 / -1 同步
 *   - 无可见返回键(点 Group 141 触发下缩关闭动画)
 *
 * 关闭动画:
 *   1. 点击 Group 141 → isDismissing = true
 *   2. 评论框 / 列表 / 装饰圆 / 顶部图标 = AnimatedVisibility 向下 fadeOut + slideOutVertically
 *   3. 视频缩略图同时从 (107,56)/198×347 动画到 (0,0)/412×917(放大成视频首页全屏)
 *   4. 动画结束后(450ms)调用 onBack() 真正 popBackStack 到演武场视频首页
 *
 * @param onBack 动画结束后调用 — 触发 NavController.popBackStack()
 */
@Composable
fun YanwuchangVideoCommentScreen(
    onBack: () -> Unit = {},
    onOpenExpanded: () -> Unit = {},
) {
    // 评论数据 — 待接入后端接口
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }

    // 关闭动画状态
    var isDismissing by remember { mutableStateOf(false) }

    // 视频缩略图放大动画:从 (107, 56)/198×347 → (0, 0)/412×917
    val videoOffsetX by animateDpAsState(
        targetValue = if (isDismissing) 0.dp else 107.dp,
        animationSpec = tween(durationMillis = 450),
        label = "videoOffsetX",
    )
    val videoOffsetY by animateDpAsState(
        targetValue = if (isDismissing) 0.dp else 56.dp,
        animationSpec = tween(durationMillis = 450),
        label = "videoOffsetY",
    )
    val videoWidth by animateDpAsState(
        targetValue = if (isDismissing) 412.dp else 198.dp,
        animationSpec = tween(durationMillis = 450),
        label = "videoWidth",
    )
    val videoHeight by animateDpAsState(
        targetValue = if (isDismissing) 917.dp else 347.dp,
        animationSpec = tween(durationMillis = 450),
        label = "videoHeight",
    )

    // 动画结束后真正返回
    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            delay(450)
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(室内家园要求 1.png, 412×917)
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
            // 视频缩略图(image 24.png)— 默认 (107,56)/198×347;关闭时放大成全屏
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment_image24),
                contentDescription = "视频缩略图",
                modifier = Modifier
                    .offset(x = videoOffsetX, y = videoOffsetY)
                    .size(width = videoWidth, height = videoHeight),
                contentScale = ContentScale.Crop,
            )

            // === 关闭时被隐藏的全部评论 UI(评论框 / 列表 / 装饰圆 / 顶部图标) ===
            //   用 Box 而非 Column:保持各元素用 offset() 绝对定位的原始布局
            AnimatedVisibility(
                visible = !isDismissing,
                exit = slideOutVertically(
                    targetOffsetY = { it },        // 从当前位置滑到底部外
                    animationSpec = tween(450),
                ) + fadeOut(animationSpec = tween(450)),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 中心装饰圆形(Ellipse 30.png, X=187.93, Y=194.63, 35×35dp)
                    //   填充 #D9D9D9,不透明度 54%
                    Image(
                        painter = painterResource(R.drawable.ellipse_30),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = 187.93.dp, y = 194.63.dp)
                            .size(width = 35.dp, height = 35.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFFD9D9D9), BlendMode.SrcIn),
                        alpha = 0.54f,
                    )

                    // 播放三角形(Polygon 3.png, X=195, Y=201, 21×20dp)
                    //   等边三角形,corner radius 3,count 3(3 边 / 3 角)
                    //   填充 #F6F6F6,不透明度 100%
                    Image(
                        painter = painterResource(R.drawable.polygon_3),
                        contentDescription = "播放",
                        modifier = Modifier
                            .offset(x = 195.dp, y = 201.dp)
                            .size(width = 21.dp, height = 20.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFFF6F6F6), BlendMode.SrcIn),
                        alpha = 1f,
                    )

                    // 评论框背景(评论框.png, X=0, Y=394, 417×523dp)
                    Image(
                        painter = painterResource(R.drawable.img_yanwuchang_video_comment_box),
                        contentDescription = "评论框",
                        modifier = Modifier
                            .offset(x = 0.dp, y = 394.dp)
                            .size(width = 417.dp, height = 523.dp),
                        contentScale = ContentScale.Fit,
                    )

                    // 评论列表(Y=440 起到评论框底 Y=917,宽 417dp,可上下滑动)
                    LazyColumn(
                        modifier = Modifier
                            .offset(x = 0.dp, y = 440.dp)
                            .size(width = 417.dp, height = 477.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(comments, key = { it.id }) { comment ->
                            CommentRow(
                                comment = comment,
                                onLikeToggle = {
                                    comments = comments.map { c ->
                                        if (c.id == comment.id) {
                                            c.copy(
                                                isLiked   = !c.isLiked,
                                                likeCount = c.likeCount + if (!c.isLiked) 1 else -1,
                                            )
                                        } else c
                                    }
                                },
                            )
                        }
                    }

                    // Group 141.png(X=17, Y=419, 17×17dp)— 可点击(触摸区域扩大到 44×44dp 居中包裹图标,符合 Material 最小点击规范)
                    //   点击 → 触发 isDismissing → 评论框下缩消失 + 视频图放大
                    Box(
                        modifier = Modifier
                            .offset(x = 17.dp - (44.dp - 17.dp) / 2, y = 419.dp - (44.dp - 17.dp) / 2)
                            .size(width = 44.dp, height = 44.dp)
                            .clickable { isDismissing = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_yanwuchang_video_comment_group141),
                            contentDescription = "关闭评论",
                            modifier = Modifier.size(width = 17.dp, height = 17.dp),
                        )
                    }

                    // 放大缩小.png(X=54, Y=419, 17×17dp)— 可点击(触摸区域扩大)
                    //   点击 → 触发 onOpenExpanded → 跳到全屏展开页(评论框向上扩展动画)
                    Box(
                        modifier = Modifier
                            .offset(x = 54.dp - (44.dp - 17.dp) / 2, y = 419.dp - (44.dp - 17.dp) / 2)
                            .size(width = 44.dp, height = 44.dp)
                            .clickable { onOpenExpanded() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_yanwuchang_video_comment_zoom),
                            contentDescription = "放大缩小",
                            modifier = Modifier.size(width = 17.dp, height = 17.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单条评论行 — 左侧用户名 + 评论文本,右侧爱心 + 点赞数
 *
 * 爱心交互:
 *   - 未点赞 → 已点赞:实心 ic_like_filled + 填充 #6D8470 + 数字 +1
 *   - 已点赞 → 未点赞:空心 ic_like_outline + 数字 -1
 */
@Composable
private fun CommentRow(
    comment: CommentItem,
    onLikeToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧:用户名 + 评论文本
        Column(
            modifier = Modifier.weight(1f),
        ) {
            // 用户名
            Text(
                text  = comment.username,
                color = Color(0xFF6D8470),
                style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
            )
            // 评论内容
            Text(
                text  = comment.text,
                color = Color(0xFF333333),
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        // 右侧:Vector.png 点赞图标(13×11dp)
            //   - 未点赞:Vector.png 原色(无 ColorFilter)
            //   - 已点赞:ColorFilter.tint + BlendMode.SrcIn 把图标**已有像素**染成 #6D8470
            //     填充严格限制在图标自身轮廓内,不超出边线
            //   点击翻转 isLiked 同步数字 ±1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment_like_vector),
                    contentDescription = if (comment.isLiked) "取消点赞" else "点赞",
                    modifier = Modifier
                        .size(width = 13.dp, height = 11.dp)
                        .clickable(onClick = onLikeToggle),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (comment.isLiked) {
                        // 已点赞:仅染色图标自身的描边像素,不会扩展到矩形外
                        ColorFilter.tint(Color(0xFF6D8470), BlendMode.SrcIn)
                    } else {
                        // 未点赞:原图无 tint
                        null
                    },
                    alpha = 1f,
                )
                Text(
                    text  = comment.likeCount.toString(),
                    color = if (comment.isLiked) Color(0xFF6D8470) else Color(0xFF999999),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
    }
}

/** 单条评论数据(可序列化版本 — 后续接接口时按需调整) */
private data class CommentItem(
    val id: Int,
    val username: String,
    val text: String,
    val likeCount: Int,
    val isLiked: Boolean = false,
)

/**
 * 自定义 Modifier — 在元素四周绘制一圈虚线边框
 *
 *  - cornerRadius:圆角半径(只对"圆"角生效)
 *  - "mixed corner radius" 按对角分布:顶左 + 底右为圆角,顶右 + 底左为直角
 *  - dashCount:虚线段数(全周长内可见的 dash 数;实际 dash + gap = 2 * dashCount 段)
 *  - width:线宽(dp)
 *  - color:线色
 *  - cap:线帽(默认 Round,与 Figma start/end point: round 一致)
 */
private fun Modifier.dashedBorderMixedCorners(
    color: Color,
    width: Dp,
    cornerRadius: Dp,
    dashCount: Int = 3,
): Modifier = this.then(
    Modifier.drawWithCache {
        val widthPx = width.toPx()
        val rPx = cornerRadius.toPx()
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(rPx, 0f)
            lineTo(w, 0f)
            lineTo(w, h - rPx)
            arcTo(
                rect = Rect(w - rPx, h - rPx, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            lineTo(0f, h)
            lineTo(0f, rPx)
            arcTo(
                rect = Rect(0f, 0f, rPx, rPx),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            close()
        }

        val straightLen = 2 * (w + h) - 4 * rPx
        val arcLen = (PI / 2).toFloat() * rPx * 2f
        val totalLen = straightLen + arcLen
        val segmentLen = totalLen / (2f * dashCount)
        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(segmentLen, segmentLen),
            phase = 0f,
        )

        onDrawWithContent {
            drawContent()
            drawPath(
                path     = path,
                color    = color,
                style    = Stroke(
                    width     = widthPx,
                    pathEffect = pathEffect,
                    cap       = StrokeCap.Round,
                ),
            )
        }
    }
)