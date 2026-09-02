package com.jueqiao.jianghu.ui.screens.yanwuchangvideo

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 演武场视频首页 + 4 个学科分类页 — 统一 Composable
 *
 * 顶部 5 个 Tab + 搜索图标与返回键同一行(横向中轴 Y≈92 对齐):
 *   - 默认态:14sp,容器 29×21dp,无背景
 *   - 选中态(当前页):20sp,容器 56×28dp(4 个学科)/ 40×21dp(推荐),
 *     文字背景图(62×47dp,横向放置)
 *
 * 行为:
 *   - 5 个页面(推荐/艺术/科学/数学/语文)布局与内容完全相同
 *   - 区别仅在 [selectedCategory] 参数:哪个 Tab 高亮
 *   - "推荐" Tab 默认即选中态(主入口页)
 *   - 4 个学科 Tab 互相跳转;"推荐" 回到主入口
 *
 * @param onBack             左上角返回 / 系统返回键回调
 * @param onOpenComment      点击"评论"图标 — 跳转至演武场视频评论1页
 * @param onOpenMy           点击底部导航栏"我的"图标 — 跳转至演武场视频"我的"页
 *
 * 注意:5 个 Tab 共享同一个 Composable 实例,Tab 切换通过内部状态完成,
 *      不调用 NavController.navigate,避免页面重建和明显的切换动画。
 *      这也意味着 isLiked/isFavorited 等状态在 Tab 切换时不会丢失。
 */
@Composable
fun YanwuchangVideoScreen(
    onBack: () -> Unit = {},
    onOpenComment: () -> Unit = {},
    onOpenMy: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到演武场首页)
    BackHandler(enabled = true) {
        onBack()
    }

    // 顶部 5 个 Tab 的选中状态 — 在 Composable 内部维护,避免 Tab 切换时重建页面
    //   可选值:"推荐" / "艺术" / "科学" / "数学" / "语文",默认 "推荐"
    var selectedCategory by remember { mutableStateOf("推荐") }

    // 点赞状态:false = 空心(原 img_yanwuchang_video_like),true = 实心(ic_like_filled)
    // 计数:点赞后 +1(500 → 501)
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(500) }

    // 收藏状态:false = 仅空心星,true = 空心星 + Vector 600 中心叠加(视觉实心)
    // 计数:收藏后 +1(500 → 501)
    var isFavorited by remember { mutableStateOf(false) }
    var favoriteCount by remember { mutableStateOf(500) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 24.png, 412×917, 圆角 5dp)
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ===== 左上角返回按钮(32×32dp 容器,内含 24×24dp 图标) =====
            //   - 容器中心 Y=92(图标 24dp 居中:76+4=80 顶,80+24=104 底,中心 92)
            //   - 5 个 Tab 与搜索图标横向中轴线对齐 Y=92(详见下方 CategoryTab 注释)
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

            // ===== 顶部 5 个 Tab(均与返回键同一行 / 横向中轴 Y=92) =====
            // 位置布局(以设计稿 412 宽为基准):
            //   - "艺术"   X= 69
            //   - "科学"   X=134
            //   - "数学"   X=199
            //   - "语文"   X=264
            //   - "推荐"   X=329  (29dp 宽,右沿 358;56dp 宽右沿 385,距搜索 X=383 留 2dp 间距)
            //   - 搜索图标 X=383
            // 选中态 4 个学科 + "推荐" Tab 背景位置:背景宽 62 vs 文字容器宽 56,
            // 左右各 (62-56)/2 = 3dp 内边距,所有 Tab 文字容器尺寸统一为 56×28dp
            // 以保证 20sp 文字完整显示并在背景图内垂直居中

            // "艺术" Tab
            CategoryTab(
                text       = "艺术",
                x          = 69.dp,
                isSelected = selectedCategory == "艺术",
                onClick    = { selectedCategory = "艺术" },
            )
            // "科学" Tab
            CategoryTab(
                text       = "科学",
                x          = 134.dp,
                isSelected = selectedCategory == "科学",
                onClick    = { selectedCategory = "科学" },
            )
            // "数学" Tab
            CategoryTab(
                text       = "数学",
                x          = 199.dp,
                isSelected = selectedCategory == "数学",
                onClick    = { selectedCategory = "数学" },
            )
            // "语文" Tab
            CategoryTab(
                text       = "语文",
                x          = 264.dp,
                isSelected = selectedCategory == "语文",
                onClick    = { selectedCategory = "语文" },
            )
            // "推荐" Tab — 与 4 个学科 Tab 共用同一渲染逻辑(默认态 / 选中态)
            //   - 选中态:文字 56×28dp, 20sp,带 62×47dp 背景
            //   - 默认态:文字 29×21, 14sp,无背景
            //   - X=322:让背景图(宽 62)右沿 381 与搜索图标(383)留 2dp 间距
            CategoryTab(
                text        = "推荐",
                x           = 322.dp,
                isSelected  = selectedCategory == "推荐",
                onClick     = { selectedCategory = "推荐" },
                isRecommend = true,
            )

            // 搜索图标(19×19dp, Y=83 让 19dp 容器中心 Y=92.5,与返回键图标中心 Y=92 对齐)
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_search),
                contentDescription = "搜索",
                modifier = Modifier
                    .offset(x = 383.dp, y = 83.dp)
                    .size(width = 19.dp, height = 19.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            )
        }

        // ===== 底部导航栏(412×85dp,竖向渐变 #8A9E7E → #81A879)=====
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 412.dp, height = 85.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    ),
            )
            // "作品"按钮
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_works),
                contentDescription = "作品",
                modifier = Modifier
                    .offset(x = 90.dp, y = 0.dp)
                    .size(width = 58.93.dp, height = 54.49.dp),
                contentScale = ContentScale.Fit,
            )
            // "我的"按钮(爪子图标 + 文字),点击进入演武场视频"我的"页
            //   容器位置 (296, 8), 28×52dp 命中范围
            //   内部用 Box 局部坐标系 — 图标 (0, 0) 27×26,文字 (0, 26) 28×18
            Box(
                modifier = Modifier
                    .offset(x = 296.dp, y = 8.dp)
                    .size(width = 28.dp, height = 44.dp)
                    .clickable(onClick = onOpenMy),
            ) {
                // "我的"图标(爪子)
                Image(
                    painter = painterResource(R.drawable.ic_yanwuchang_video_me_paw),
                    contentDescription = "我的",
                    modifier = Modifier
                        .size(width = 27.dp, height = 26.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
                )
                // "我的"文字
                Text(
                    text  = "我的",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize   = 14.sp,
                    ),
                    modifier = Modifier
                        .offset(y = 26.dp)
                        .size(width = 28.dp, height = 18.dp),
                )
            }
        }

        // ===== 视频内容(5 个页面完全相同)=====
        // 用户名 / 标题
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
        // 头像
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
        // 头像嘴部装饰线 — Vector 593 / 594 / 595
        //   中心点按 Figma 坐标直接放置(以设计稿 412dp 宽为基准)
        //   尺寸取 PNG 原生像素(dp);填充白色 #FFFFFF,不透明度 100%
        //   Vector 595 横向放置
        // Vector 593: 中心 (379, 456), 原生 3×8dp
        Image(
            painter = painterResource(R.drawable.vector_593),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (379 - 3 / 2).dp, y = (456 - 8 / 2).dp)
                .size(width = 3.dp, height = 8.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
        )
        // Vector 594: 中心 (395, 454), 原生 3×10dp
        Image(
            painter = painterResource(R.drawable.vector_594),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (395 - 3 / 2).dp, y = (454 - 10 / 2).dp)
                .size(width = 3.dp, height = 10.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
        )
        // Vector 595: 位置 (382, 471), 原生 16×5dp,横向放置
        //   注:X:382, Y:471 表示线条左上角定位(非中心点)
        Image(
            painter = painterResource(R.drawable.vector_595),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 382.dp, y = 471.dp)
                .size(width = 16.dp, height = 5.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 点赞 — 点击切换空心/实心,数字同步 +1/-1
        //   容器固定 30×28dp;空心和实心都使用 ColorFilter.tint(#81A084) 保持视觉一致
        //   实心时整体 100% 不透明,填充色统一为 #81A084
        Image(
            painter = painterResource(
                if (isLiked) R.drawable.ic_like_filled
                else R.drawable.ic_like_outline
            ),
            contentDescription = if (isLiked) "已点赞" else "点赞",
            modifier = Modifier
                .offset(x = 374.dp, y = 500.dp)
                .size(width = 30.dp, height = 28.dp)
                .clickable {
                    isLiked = !isLiked
                    likeCount += if (isLiked) 1 else -1
                },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 点赞 — 已点赞(实心)时,在图标周围显示 4 根装饰线
        //   容器 6×7.98dp,corner radius 3(原 PNG 预渲染)
        //   填充 #EEC4B9,不透明度 100%,各自带旋转角度
        if (isLiked) {
            // Rectangle 199: 位置 (368, 505),旋转 -49.73°
            Image(
                painter = painterResource(R.drawable.rectangle_199),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 368.dp, y = 505.dp)
                    .size(width = 6.dp, height = 7.98.dp)
                    .rotate(-49.73f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFEEC4B9), BlendMode.SrcIn),
                alpha = 1f,
            )
            // Rectangle 200: 位置 (376, 497),旋转 -32.48°
            Image(
                painter = painterResource(R.drawable.rectangle_200),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 376.dp, y = 497.dp)
                    .size(width = 6.dp, height = 7.98.dp)
                    .rotate(-32.48f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFEEC4B9), BlendMode.SrcIn),
                alpha = 1f,
            )
            // Rectangle 201: 位置 (394, 497),旋转 -35.96°
            Image(
                painter = painterResource(R.drawable.rectangle_201),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 394.dp, y = 497.dp)
                    .size(width = 6.dp, height = 7.98.dp)
                    .rotate(-35.96f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFEEC4B9), BlendMode.SrcIn),
                alpha = 1f,
            )
            // Rectangle 202: 位置 (401, 506),旋转 -69.34°
            Image(
                painter = painterResource(R.drawable.rectangle_202),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 401.dp, y = 506.dp)
                    .size(width = 6.dp, height = 7.98.dp)
                    .rotate(-69.34f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFEEC4B9), BlendMode.SrcIn),
                alpha = 1f,
            )
        }
        // 点赞 — 数字 (500 / 501, 22×16dp 容器, 12sp 白色, 不透明度 100%)
        //   使用 Compose Text 而非 PNG,字号/位置/尺寸完全由 style 决定,数字变化时零缩放
        Text(
            text  = likeCount.toString(),
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 532.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 小爱心 + 数字 已移除(避免与主点赞按钮重复)
        // 评论 — 点击跳转到演武场视频评论1页
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_comment),
            contentDescription = "评论",
            modifier = Modifier
                .offset(x = 374.dp, y = 571.dp)
                .size(width = 31.dp, height = 31.dp)
                .clickable(onClick = onOpenComment),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        // 评论 — 数字 "500" (Text 渲染, 12sp 白色, 22×16dp 容器, 不透明度 100%)
        Text(
            text  = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 603.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 收藏 — 点击在图标中央叠加 Vector 600 标记,数字同步 +1/-1
        //   容器固定 34×34dp;始终显示空心星 img_yanwuchang_video_favorite(#81A084)
        //   已收藏时,在星图标中心(381, 649.5)叠加 Vector 600(16×15dp)使其视觉上"实心"
        //   不透明度 100%,填充色统一 #81A084
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_favorite),
            contentDescription = if (isFavorited) "已收藏" else "收藏",
            modifier = Modifier
                .offset(x = 372.dp, y = 640.dp)
                .size(width = 34.dp, height = 34.dp)
                .clickable {
                    isFavorited = !isFavorited
                    favoriteCount += if (isFavorited) 1 else -1
                },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 收藏 — 已收藏时,Vector 600 标记叠加在图标中心(16×15dp,容器内居中)
        if (isFavorited) {
            Image(
                painter = painterResource(R.drawable.vector_600),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 381.dp, y = 649.5.dp)
                    .size(width = 16.dp, height = 15.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
                alpha = 1f,
            )
        }
        // 收藏 — 数字 (500 / 501, 22×16dp 容器, 12sp 白色, 不透明度 100%)
        //   使用 Compose Text 而非 PNG,字号/位置/尺寸完全由 style 决定,数字变化时零缩放
        Text(
            text  = favoriteCount.toString(),
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 674.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 分享
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
        // 分享 — 数字 "500" (Text 渲染, 12sp 白色, 22×16dp 容器, 不透明度 100%)
        Text(
            text  = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 745.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        // 中心装饰圆形 + 播放按钮
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
        // 视频进度条
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

/**
 * 顶部 Tab — 4 个学科 + "推荐" 共用此渲染逻辑
 *
 * 布局统一以中轴 Y=92dp 为锚点(与左上角返回键、搜索图标同一行)
 *
 * 选中态(当前页):
 *   - 4 个学科 + "推荐":20sp / 56×28dp 文字容器,垂直居中于 62×47dp 背景图
 *   - 背景图 62×47dp,Y=68.5 (中心 92),完全包住 56×28dp 文字
 *   - 文字 Y=78 (中心 92),在背景图内垂直居中
 *   - "推荐" 文字容器宽度同样 56dp(与其他学科一致),保证 20sp 完整显示
 *
 * 默认态:
 *   - 4 个学科 + "推荐":14sp / 29×21dp 文字容器,Y=81.5 (中心 92)
 *   - 无背景
 *
 * @param text         Tab 文字
 * @param x            Tab 文字容器左边缘 X 坐标
 * @param isSelected   是否为当前选中页
 * @param onClick      点击切换回调
 * @param isRecommend  是否是"推荐" Tab(目前仅影响默认态/选中态的视觉差异,尺寸统一)
 */
@Composable
private fun CategoryTab(
    text:        String,
    x:           androidx.compose.ui.unit.Dp,
    isSelected:  Boolean,
    onClick:     () -> Unit,
    isRecommend: Boolean = false,
) {
    if (isSelected) {
        // 选中态:文字背景 `img_yanwuchang_video_recommend_bg.png` 横向放置 62×47dp
        //   - 背景中心 Y=92,Y_top=68.5,Y_bottom=115.5
        //   - 文字宽 56,左侧内边距 6dp(背景向左偏 3dp,呈现"背景图包围文字"且整体略偏左的视觉效果)
        //   - 右侧内边距 (62-56)-6 = 0dp
        val padX = 6.dp
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_recommend_bg),
            contentDescription = null,
            modifier = Modifier
                .offset(x = x - padX, y = 68.5.dp)
                .size(width = 62.dp, height = 47.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.7f,
        )
        // 选中态文字:20sp,容器 56×28dp
        //   - 文字垂直居中于背景图(背景中心 Y=92,文字高 28,Y_top=78)
        //   - 5 个 Tab 文字容器尺寸完全一致,确保"推荐" 20sp 文字完整显示
        Text(
            text  = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .offset(x = x, y = 78.dp)
                .size(width = 56.dp, height = 28.dp)
                .clickable(onClick = onClick),
        )
    } else {
        // 默认态:14sp,容器 29×21dp,Y=81.5(容器中心 Y=92,与返回键/搜索键同中轴)
        Text(
            text  = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = x, y = 81.5.dp)
                .size(width = 29.dp, height = 21.dp)
                .clickable(onClick = onClick),
        )
    }
}
