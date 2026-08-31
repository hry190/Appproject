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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * @param selectedCategory   当前页所属 Tab 名称("推荐" / "艺术" / "科学" / "数学" / "语文")
 * @param onBack             左上角返回 / 系统返回键回调
 * @param onOpenArt          顶部"艺术" Tab 点击回调
 * @param onOpenScience      顶部"科学" Tab 点击回调
 * @param onOpenMath         顶部"数学" Tab 点击回调
 * @param onOpenChinese      顶部"语文" Tab 点击回调
 * @param onOpenRecommend    顶部"推荐" Tab 点击回调
 */
@Composable
fun YanwuchangVideoScreen(
    selectedCategory: String = "推荐",
    onBack:          () -> Unit = {},
    onOpenArt:       () -> Unit = {},
    onOpenScience:   () -> Unit = {},
    onOpenMath:      () -> Unit = {},
    onOpenChinese:   () -> Unit = {},
    onOpenRecommend: () -> Unit = {},
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
            // 选中态 4 个学科 Tab 背景位置:背景宽 62 vs 文字容器宽 56,左右各 (62-56)/2 = 3dp 内边距
            // "推荐" 选中态:文字容器宽 40,背景宽 62,左右各 (62-40)/2 = 11dp 内边距

            // "艺术" Tab
            CategoryTab(
                text         = "艺术",
                x            = 69.dp,
                isSelected   = selectedCategory == "艺术",
                onClick      = onOpenArt,
            )
            // "科学" Tab
            CategoryTab(
                text         = "科学",
                x            = 134.dp,
                isSelected   = selectedCategory == "科学",
                onClick      = onOpenScience,
            )
            // "数学" Tab
            CategoryTab(
                text         = "数学",
                x            = 199.dp,
                isSelected   = selectedCategory == "数学",
                onClick      = onOpenMath,
            )
            // "语文" Tab
            CategoryTab(
                text         = "语文",
                x            = 264.dp,
                isSelected   = selectedCategory == "语文",
                onClick      = onOpenChinese,
            )
            // "推荐" Tab — 与 4 个学科 Tab 共用同一渲染逻辑(默认态 / 选中态)
            //   - 选中态:文字 40×21, 20sp,带 62×47dp 背景
            //   - 默认态:文字 29×21, 14sp,无背景
            CategoryTab(
                text         = "推荐",
                x            = 329.dp,
                isSelected   = selectedCategory == "推荐",
                onClick      = onOpenRecommend,
                isRecommend  = true,
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
            // "我的"图标
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_people),
                contentDescription = "我的",
                modifier = Modifier
                    .offset(x = 296.dp, y = 8.dp)
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
                    .offset(x = 296.dp, y = 34.dp)
                    .size(width = 28.dp, height = 18.dp),
            )
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
        // 点赞
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
        // 点赞 — 数字 "500" (500.png, 22×16dp, #FFFFFF, 不透明度 100%)
        Image(
            painter = painterResource(R.drawable.text_500),
            contentDescription = "点赞数",
            modifier = Modifier
                .offset(x = 378.dp, y = 532.dp)
                .size(width = 22.dp, height = 16.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 评论
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_comment),
            contentDescription = "评论",
            modifier = Modifier
                .offset(x = 374.dp, y = 571.dp)
                .size(width = 31.dp, height = 31.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        // 评论 — 数字 "500" (500.png, 22×16dp, #FFFFFF, 不透明度 100%)
        Image(
            painter = painterResource(R.drawable.text_500),
            contentDescription = "评论数",
            modifier = Modifier
                .offset(x = 378.dp, y = 603.dp)
                .size(width = 22.dp, height = 16.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
        )
        // 收藏
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_favorite),
            contentDescription = "收藏",
            modifier = Modifier
                .offset(x = 372.dp, y = 640.dp)
                .size(width = 34.dp, height = 34.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        // 收藏 — 数字 "500" (500.png, 22×16dp, #FFFFFF, 不透明度 100%)
        Image(
            painter = painterResource(R.drawable.text_500),
            contentDescription = "收藏数",
            modifier = Modifier
                .offset(x = 378.dp, y = 674.dp)
                .size(width = 22.dp, height = 16.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
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
        // 分享 — 数字 "500" (500.png, 22×16dp, #FFFFFF, 不透明度 100%)
        Image(
            painter = painterResource(R.drawable.text_500),
            contentDescription = "分享数",
            modifier = Modifier
                .offset(x = 378.dp, y = 745.dp)
                .size(width = 22.dp, height = 16.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
            alpha = 1f,
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
 * 选中态(当前页):
 *   - 4 个学科:20sp / 56×28dp,背景 62×47dp
 *   - "推荐"  :20sp / 40×21dp,背景 62×47dp(用户要求文字完全包含在图标内)
 *
 * 默认态:
 *   - 4 个学科 + "推荐":14sp / 29×21dp,无背景
 *
 * @param text         Tab 文字
 * @param x            Tab 文字容器左边缘 X 坐标
 * @param isSelected   是否为当前选中页
 * @param onClick      点击切换回调
 * @param isRecommend  是否是"推荐" Tab(决定选中态容器尺寸:40×21 vs 56×28)
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
        //   - 背景中心对齐文字中心;背景宽 62
        //   - 4 个学科:文字宽 56,左右各 (62-56)/2 = 3dp 内边距,偏移 = x - 3
        //   - "推荐"  :文字宽 40,左右各 (62-40)/2 = 11dp 内边距,偏移 = x - 11
        val padX = if (isRecommend) 11.dp else 3.dp
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_recommend_bg),
            contentDescription = null,
            modifier = Modifier
                .offset(x = x - padX, y = 56.dp)
                .size(width = 62.dp, height = 47.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.7f,
        )
        // 选中态文字:20sp
        //   - 4 个学科:容器 56×28dp,Y=66(底沿 94 ≈ 返回键图标中心 92 + 2dp)
        //   - "推荐"  :容器 40×21dp,Y=69(底沿 90,贴合背景底沿 56+47=103 内偏上,文字视觉居中于背景)
        if (isRecommend) {
            Text(
                text  = text,
                color = Color.White,
                style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
                modifier = Modifier
                    .offset(x = x, y = 69.dp)
                    .size(width = 40.dp, height = 21.dp)
                    .clickable(onClick = onClick),
            )
        } else {
            Text(
                text  = text,
                color = Color.White,
                style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
                modifier = Modifier
                    .offset(x = x, y = 66.dp)
                    .size(width = 56.dp, height = 28.dp)
                    .clickable(onClick = onClick),
            )
        }
    } else {
        // 默认态:14sp,容器 29×21dp,Y=74(容器底沿 Y=95 ≈ 返回键图标中心 92 + 3dp,视觉对齐)
        Text(
            text  = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = x, y = 74.dp)
                .size(width = 29.dp, height = 21.dp)
                .clickable(onClick = onClick),
        )
    }
}
