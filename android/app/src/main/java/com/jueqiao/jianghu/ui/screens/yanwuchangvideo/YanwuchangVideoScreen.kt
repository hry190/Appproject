package com.jueqiao.jianghu.ui.screens.yanwuchangvideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 演武场视频首页 — 用 image 24.png 作全屏背景 + 左上返回按钮。
 * 背景图原始尺寸 412×917,使用 ContentScale.Crop 适配任意屏幕;
 * 整体圆角 5dp(对应设计稿 corner radius: 5)。
 */
@Composable
fun YanwuchangVideoScreen(
    onBack: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到演武场首页)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 24.png, 412×917,圆角 5dp)— 用 clip 切出圆角
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)— 只保留左上角返回按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 左上角返回按钮
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 76.dp)
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_dahui_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // 顶部导航栏标题(与返回键图像中心 Y=92 视觉对齐)
            //   - 14sp 项 Y=82:容器 21dp,文本中心约 Y=92.5,与返回键图像中心 Y=92 对齐
            Text("艺术", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.offset(x = 69.dp, y = 74.dp).size(width = 29.dp, height = 21.dp))
            Text("科学", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.offset(x = 134.dp, y = 74.dp).size(width = 29.dp, height = 21.dp))
            Text("数学", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.offset(x = 199.dp, y = 74.dp).size(width = 29.dp, height = 21.dp))
            Text("语文", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.offset(x = 264.dp, y = 74.dp).size(width = 29.dp, height = 21.dp))
            // "推荐"装饰背景徽章(未标题-1 50 (2).png, X=312, Y=57, 62×47 横向放置, 不透明度 70%)
            //   - Y=57(规格 Y=56 +1):背景底沿 57+47=104,刚好包含"推荐"文字底沿 76+28=104
            //   - 宽 62 vs 文字 50:左右各留 6dp 边距
            //   - 高 47 vs 文字 28:上留 19dp / 下贴齐
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_recommend_bg),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 312.dp, y = 56.dp)
                    .size(width = 62.dp, height = 47.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.7f,
            )
            // "推荐" 字号较大(20sp),容器扩到 50×28 容纳 2 个汉字;Y=76 上移一点以视觉减重
            Text("推荐", color = Color.White, style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
                modifier = Modifier.offset(x = 322.dp, y = 69.dp).size(width = 56.dp, height = 28.dp))

            // 搜索图标(Search.png, X=383, Y=83, 19×19,上移至文字横向中轴线 Y=92.5;
            //   填充 #FFFFFF, 不透明度 100%;spacing=-2 对 Image 无效)
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_search),
                contentDescription = "搜索",
                modifier = Modifier
                    .offset(x = 383.dp, y = 75.dp)
                    .size(width = 19.dp, height = 19.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            )
        }

        // 底部导航栏(整条贴屏幕物理底部,跨过系统导航条区域)— 412×85
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 412.dp, height = 85.dp),
        ) {
            // 竖向线性渐变 #8A9E7E(0%)→ #81A879(100%),不透明度 100%
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E), // 顶部:灰绿(stops 0%)
                                Color(0xFF81A879), // 底部:草绿(stops 100%)
                            ),
                        ),
                    ),
            )

            // "作品"按钮(Group 254.png, 原绝对坐标 X=90, Y=832 → 相对栏顶 (90, 0),
            //   58.93×54.49,weight=1 → Image 无 weight 属性,绝对定位下不影响布局)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_works),
                contentDescription = "作品",
                modifier = Modifier
                    .offset(x = 90.dp, y = 0.dp)
                    .size(width = 58.93.dp, height = 54.49.dp),
                contentScale = ContentScale.Fit,
            )

            // "我的"图标(原绝对坐标 X=296, Y=840 → 相对栏顶偏移 (296, 8),27×26, 填充 #FFFFFF)
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_people),
                contentDescription = "我的",
                modifier = Modifier
                    .offset(x = 296.dp, y = 8.dp)
                    .size(width = 27.dp, height = 26.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            )

            // "我的"文字(原绝对坐标 X=296, Y=866 → 相对栏顶偏移 (296, 34),28×18, 14px, #FFFFFF)
            Text(
                text  = "我的",
                color = Color.White,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize   = 14.sp,
                ),
                modifier = Modifier
                    .offset(x = 296.dp, y = 34.dp)
                    .size(width = 28.dp, height = 18.dp),
            )
        }

        // 视频用户名/标题(从设计稿预渲染 PNG,X=17/22, Y=745/788, 字体 24px/16px, 纯白/浅灰)
        //   - "@啊啊啊.png" → 97×32 容器,ColorFilter.tint 强制 #FFFFFF,确保跨设备色值一致
        //   - "AI概念与能力边界创作.png" → 160×21 容器,ColorFilter.tint 强制 #D0D0D0
        //   - 两图 alpha=1f(100% 不透明)
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_username),
            contentDescription = "用户名",
            modifier = Modifier
                .offset(x = 17.dp, y = 745.dp)
                .size(width = 97.dp, height = 32.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            alpha = 1f,
        )
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_title),
            contentDescription = "视频标题",
            modifier = Modifier
                .offset(x = 22.dp, y = 788.dp)
                .size(width = 160.dp, height = 21.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFD0D0D0), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 头像/作者标识(头像.png, X=367, Y=444, 40×40, 填充 #F6B1B1, 不透明度 100%)
        //   - 位于右栏操作列顶部,作为视频作者头像入口
        //   - X=367 + 宽 40 = 右沿 407,留 5dp 右边距(头像略大于操作图标,作为视觉锚点)
        //   - 与下方点赞按钮(40dp 高)Y=500 间距 16dp,形成头像 → 操作列的层级关系
        //   - 颜色 #F6B1B1 是暖粉色调,与右栏绿色调形成对比,凸显头像身份
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_avatar),
            contentDescription = "头像",
            modifier = Modifier
                .offset(x = 367.dp, y = 444.dp)
                .size(width = 40.dp, height = 40.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFF6B1B1), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 头像装饰线 #1(Vector 593 重写为 Canvas 矢量绘制,X=379,Y=456,weight=2,圆头圆尾,白)
        //   - 原 PNG 实际仅 3×8px,容器 0.08×6dp 会把 2dp 描边压缩到不可见,改用 Canvas
        //   - Canvas 容器:offset.x = 378(给 1dp 左内边距让描边居中于 X=379),size = 2×6dp
        //   - 描边起点/终点都在 Canvas 水平中心,垂直方向从顶到底,strokeWidth=2dp,StrokeCap.Round
        Canvas(
            modifier = Modifier
                .offset(x = 378.dp, y = 456.dp)
                .size(width = 2.dp, height = 6.dp),
        ) {
            drawLine(
                color       = Color.White,
                start       = Offset(size.width / 2f, 0f),
                end         = Offset(size.width / 2f, size.height),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round,
            )
        }
        // 头像装饰线 #2(Vector 594 重写为 Canvas 矢量绘制,X=395,Y=454,weight=2,圆头圆尾,白)
        //   - 原 PNG 仅 3×10px,容器 0.55×7.25dp 同样被压缩,改用 Canvas
        //   - Canvas 容器:offset.x = 394.45(让 2dp 描边居中于 X=395),size = 2×7.25dp
        Canvas(
            modifier = Modifier
                .offset(x = 394.45.dp, y = 454.dp)
                .size(width = 2.dp, height = 7.25.dp),
        ) {
            drawLine(
                color       = Color.White,
                start       = Offset(size.width / 2f, 0f),
                end         = Offset(size.width / 2f, size.height),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round,
            )
        }
        // 头像装饰线 #3(Vector 595 重写为 Canvas 矢量绘制,X=392,Y=471,weight=2,圆头圆尾,白)
        //   - 原 PNG 16×5px,容器 13.4×2.03dp 勉强够,但精度受限,改用 Canvas 更可靠
        //   - Canvas 容器:offset.y = 470.015(让 2dp 描边居中于 Y=471),size = 13.4×2dp
        //   - 水平方向:从 (0, size.height/2) 画到 (size.width, size.height/2)
        Canvas(
            modifier = Modifier
                .offset(x = 382.dp, y = 470.015.dp)
                .size(width = 13.4.dp, height = 2.dp),
        ) {
            drawLine(
                color       = Color.White,
                start       = Offset(0f, size.height / 2f),
                end         = Offset(size.width, size.height / 2f),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round,
            )
        }
        // 点赞按钮(Thumbs-up (赞).png, X=374, Y=500, 30×28, 填充 #81A084, 不透明度 100%)
        //   - 右栏操作列最上方,下方依次为评论(Y=571)、收藏(Y=640)、分享(Y=713)
        //   - X=374 + 宽 30 = 右沿 404,留 8dp 右边距
        //   - 颜色 #81A084 接近底部导航栏渐变底色 #81A879,作为强调色呼应底部栏
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_like),
            contentDescription = "点赞",
            modifier = Modifier
                .offset(x = 374.dp, y = 500.dp)
                .size(width = 30.dp, height = 28.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 点赞计数(500,X=378,Y=532,22×16,12sp,Regular/默认字重,#FFFFFF)
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 532.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 评论按钮(评论 1.png, X=374, Y=571, 31×31, 不透明度 100%)
        //   - 右栏操作列最上方,下方依次为收藏(Y=640)、分享(Y=713)
        //   - X=374 + 宽 31 = 右沿 405,留 7dp 右边距
        //   - 用户未指定填充色,沿用原图色
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_comment),
            contentDescription = "评论",
            modifier = Modifier
                .offset(x = 374.dp, y = 571.dp)
                .size(width = 31.dp, height = 31.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        // 评论计数(500,X=378,Y=603,22×16,12sp,Regular/默认字重,#FFFFFF)
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 603.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 收藏按钮(收藏 1.png, X=372, Y=640, 34×34, 不透明度 100%)
        //   - 位于右侧中部,与下方分享按钮(X=374)同列(X=372/374,误差 2dp,视觉上对齐)
        //   - X=372 + 宽 34 = 右沿 406,留 6dp 右边距
        //   - 用户未指定填充色,沿用原图色,不应用 ColorFilter.tint
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_favorite),
            contentDescription = "收藏",
            modifier = Modifier
                .offset(x = 372.dp, y = 640.dp)
                .size(width = 34.dp, height = 34.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        // 收藏计数(500,X=378,Y=674,22×16,12sp,Regular/默认字重,#FFFFFF)
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 674.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 分享按钮(Share-two (分享2).png, X=374, Y=713, 32×28, 填充 #7FA889, 不透明度 100%)
        //   - 位于用户名区域右上角(用户名 Y=745-777,share Y=713-741,垂直差 4dp,视觉对齐同一基线带)
        //   - X=374 + 宽 32 = 右沿 406,留 6dp 右边距
        //   - 颜色 #7FA889 是底部导航栏渐变中部色,作为强调色呼应底部栏
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_share),
            contentDescription = "分享",
            modifier = Modifier
                .offset(x = 374.dp, y = 713.dp)
                .size(width = 32.dp, height = 28.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFF7FA889), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 分享计数(500,X=378,Y=745,22×16,12sp,Regular/默认字重,#FFFFFF)
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 745.dp)
                .size(width = 22.dp, height = 16.dp),
        )

        // 中心装饰圆形(Ellipse 30.png, X=163, Y=395, 84×84, 填充 #D9D9D9, 不透明度 54%)
        //   - 84×84 等宽高,ContentScale.Fit 渲染为正圆
        //   - 原图为浅灰,这里额外加 ColorFilter.tint 强制指定色值,确保跨设备色值一致
        //   - alpha=0.54 应用 54% 不透明度
        Image(
            painter = painterResource(R.drawable.ellipse_30),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 163.dp, y = 395.dp)
                .size(width = 84.dp, height = 84.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFD9D9D9), BlendMode.SrcIn),
            alpha = 0.54f,
        )
        // 播放按钮(Polygon 3.png, X=181, Y=412, 51×49, count=3 三角形,corner radius=3,
        //   填充 #F6F6F6, 不透明度 100%)
        //   - 居中叠在 Ellipse 30 之上作为播放图标:ellipse 中心 (205, 437) ≈ polygon 中心 (206.5, 436.5)
        //   - 原图已带圆角端点(corner radius=3,圆角三角形),直接渲染
        Image(
            painter = painterResource(R.drawable.polygon_3),
            contentDescription = "播放",
            modifier = Modifier
                .offset(x = 181.dp, y = 412.dp)
                .size(width = 51.dp, height = 49.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFF6F6F6), BlendMode.SrcIn),
            alpha = 1f,
        )

        // 视频进度条(Group 177.png, X=20, Y=808, 372×8,weight=5,圆头/圆尾)
        //   - 原 Y=830 时进度条底沿 Y=838,与底部导航栏上沿 Y=832 重叠 6dp
        //   - 现上移至 Y=808,底沿 Y=816,与底部导航栏上沿留出 16dp 视觉间距(8dp 基线 ×2)
        //   - 原图已带圆头/圆尾(round strokeCap),直接渲染即可
        Image(
            painter = painterResource(R.drawable.group_177),
            contentDescription = "视频进度",
            modifier = Modifier
                .offset(x = 20.dp, y = 818.dp)
                .size(width = 372.dp, height = 8.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
