package com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import kotlin.math.PI

/**
 * 演武场视频评论1页 — 点击演武场视频首页的"评论"图标进入
 *
 * 当前布局(Figma 设计稿 412×917 基准,简化版):
 *   - 全屏背景:室内家园要求 1.png(img_yanwuchang_bg, 412×917, ContentScale.Crop)
 *   - 视频缩略图:image 24.png(img_yanwuchang_video_comment_image24)
 *       位置 (107, 56),尺寸 198×347dp
 *   - 中心装饰圆:Ellipse 30.png
 *       位置 (187, 194),尺寸 35×35dp;填充 #D9D9D9,不透明度 54%
 *   - 播放三角形:Polygon 3.png
 *       位置 (195, 201),尺寸 20×20dp;等边三角形,填充 #F6F6F6,不透明度 100%
 *   - 视频进度条:Group 177.png(原图 377×8,宽高比 47.13:1)
 *       位置 (116, 399),尺寸 180×4dp — 水平居中于缩略图,贴视频底沿
 *   - 评论框背景:已删除,等用户重新加入图片
 *
 * @param onBack         返回回调(预留,当前未使用)
 * @param onOpenExpanded 打开全屏展开页回调(预留,当前未使用)
 */
@Composable
fun YanwuchangVideoComment1Screen(
    onBack: () -> Unit = {},
    onOpenExpanded: () -> Unit = {},
) {
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
                .fillMaxSize(),
        ) {
            // 中心装饰圆形(Ellipse 30.png, X=187, Y=194, 35×35dp)
            //   填充 #D9D9D9,不透明度 54%
            //   叠在视频缩略图正中,作为视频缩略图的"播放"提示装饰
            Image(
                painter = painterResource(R.drawable.ellipse_30),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 187.dp, y = 194.dp)
                    .size(width = 35.dp, height = 35.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFD9D9D9), BlendMode.SrcIn),
                alpha = 0.54f,
            )

            // 播放三角形(Polygon 3.png, X=195, Y=201, 20×20dp)
            //   等边三角形,corner radius 3,count 3(3 边 / 3 角)
            //   填充 #F6F6F6,不透明度 100%
            //   居中叠在 Ellipse 30 之上
            Image(
                painter = painterResource(R.drawable.polygon_3),
                contentDescription = "播放",
                modifier = Modifier
                    .offset(x = 195.dp, y = 201.dp)
                    .size(width = 20.dp, height = 20.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFF6F6F6), BlendMode.SrcIn),
                alpha = 1f,
            )

            // 视频缩略图(image 24.png)— 位置 (107, 56),尺寸 198×347dp
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment_image24),
                contentDescription = "视频缩略图",
                modifier = Modifier
                    .offset(x = 107.dp, y = 56.dp)
                    .size(width = 198.dp, height = 347.dp),
                contentScale = ContentScale.Crop,
            )

            // 视频进度条(Group 177.png)— 叠在缩略图底部边缘
            //   原图 377×8px(宽高比 47.13:1)
            //   缩略图宽 198dp,进度条宽 180dp(左右各 9dp 内边距,水平居中)
            //   高度按宽高比 180 / 47.13 ≈ 3.82dp → 取 4dp
            //   X = 107 + 9 = 116dp(水平居中)
            //   Y = 缩略图 Y + 缩略图高 - 4dp = 56 + 347 - 4 = 399dp
            //   (进度条 Y=399-403,贴视频底沿 4dp)
            Image(
                painter = painterResource(R.drawable.group_177),
                contentDescription = "视频进度",
                modifier = Modifier
                    .offset(x = 116.dp, y = 399.dp)
                    .size(width = 180.dp, height = 4.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

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