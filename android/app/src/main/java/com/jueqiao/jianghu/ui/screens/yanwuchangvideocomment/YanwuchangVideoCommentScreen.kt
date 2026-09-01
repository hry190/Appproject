package com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment

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
import kotlin.math.PI

/**
 * 演武场视频评论页 — 点击演武场视频首页的"评论"图标进入
 *
 * 布局(Figma 设计稿 412×917 基准):
 *   - 全屏背景:室内家园要求 1.png(img_yanwuchang_bg, 412×917, ContentScale.Crop)
 *   - 视频缩略图:视频.png(img_yanwuchang_video_comment_thumb)
 *       位置 (107, 56),尺寸 198×347dp
 *       外围装饰边框:count=3, weight=5dp, round line-cap, mixed corner radius
 *       (顶左/底右为圆角,顶右/底左为直角)
 *   - 评论框:评论框.png(img_yanwuchang_video_comment_box)
 *       位置 (0, 394),尺寸 417×523dp
 *   - 顶部两个图标(可点击):
 *       Group 141.png @ (17, 419), 17×17dp
 *       放大缩小.png @ (54, 419), 17×17dp
 *   - 评论区(Y=440 起到评论框底 Y=917):可上下滑动的评论列表
 *       每条评论右侧爱心可点击:
 *         - 未点赞:空心 ic_like_outline
 *         - 已点赞:实心 ic_like_filled,填充色 #6D8470, 不透明度 100%
 *         - 数字 +1 / -1 同步
 *   - 无返回键
 */
@Composable
fun YanwuchangVideoCommentScreen() {
    // 评论数据 — 待接入后端接口
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }

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
            // 视频缩略图(视频.png, X=107, Y=56, 198×347dp)
            //   外围加一圈装饰虚线边框(count=3, weight=5, round cap, mixed corner radius)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment_thumb),
                contentDescription = "视频缩略图",
                modifier = Modifier
                    .offset(x = 107.dp, y = 56.dp)
                    .size(width = 198.dp, height = 347.dp)
                    .dashedBorderMixedCorners(
                        color        = Color(0xFF81A084),
                        width        = 5.dp,
                        cornerRadius = 12.dp,
                    ),
                contentScale = ContentScale.Crop,
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
            //   顶部留 46dp 空间给上方两个图标(17dp 图标 + 4dp 上间距 + 余量)
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

            // Group 141.png(X=17, Y=419, 17×17dp)— 可点击
            //   暂未指定行为,留 TODO;后续可关联输入/搜索等功能
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment_group141),
                contentDescription = "评论输入",
                modifier = Modifier
                    .offset(x = 17.dp, y = 419.dp)
                    .size(width = 17.dp, height = 17.dp)
                    .clickable { /* TODO: 输入 / 搜索 */ },
            )

            // 放大缩小.png(X=54, Y=419, 17×17dp)— 可点击
            //   暂未指定行为,留 TODO;后续可关联视频缩略图的展开切换
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment_zoom),
                contentDescription = "放大缩小",
                modifier = Modifier
                    .offset(x = 54.dp, y = 419.dp)
                    .size(width = 17.dp, height = 17.dp)
                    .clickable { /* TODO: 视频缩略图展开 / 收起 */ },
            )
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
        // 右侧:爱心 + 点赞数(垂直居中)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Image(
                painter = painterResource(
                    if (comment.isLiked) R.drawable.ic_like_filled
                    else R.drawable.ic_like_outline
                ),
                contentDescription = if (comment.isLiked) "取消点赞" else "点赞",
                modifier = Modifier
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = onLikeToggle),
                contentScale = ContentScale.Fit,
                // 已点赞时填充 #6D8470;未态保留原色(灰)
                colorFilter = if (comment.isLiked) {
                    ColorFilter.tint(Color(0xFF6D8470), BlendMode.SrcIn)
                } else {
                    ColorFilter.tint(Color(0xFF999999), BlendMode.SrcIn)
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
 *
 * 实现思路:
 *   1) 用 Path 画出矩形外周(顶左圆角、底右圆角,顶右/底左直角)
 *   2) PathEffect.dashPathEffect 把整圈切成 dash + gap 重复,目标 dash 数 = dashCount
 *   3) 用 Stroke 描边,line cap = Round
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

        // 1) 外周路径(顶左 + 底右 圆角,顶右 + 底左 直角)
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

        // 2) 估算路径长度(直线 + 两段 1/4 圆弧)
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