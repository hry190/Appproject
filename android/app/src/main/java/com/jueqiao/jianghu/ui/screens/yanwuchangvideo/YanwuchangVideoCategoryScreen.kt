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
 * 演武场视频 — 单个学科分类页(艺术 / 科学 / 数学 / 语文)
 *   - 布局与 [YanwuchangVideoScreen] 保持一致,只把顶部"推荐"标签换成当前学科名
 *   - 顶部 4 个 Tab 文字字号 20sp,容器 56×28dp,文字背景 `img_yanwuchang_video_recommend_bg.png`
 *     横向放置 62×47dp,跟随页面切换
 *   - 内容层(视频、点赞、评论等)与原 [YanwuchangVideoScreen] 一致
 *
 * @param category  当前页所属学科(艺术 / 科学 / 数学 / 语文),决定顶部 Tab 高亮
 * @param onBack    左上角返回 / 系统返回键回调
 * @param onOpenArt / Science / Math / Chinese    顶部 Tab 文字点击回调
 */
@Composable
fun YanwuchangVideoCategoryScreen(
    category:    String,
    onBack:      () -> Unit,
    onOpenArt:     () -> Unit,
    onOpenScience: () -> Unit,
    onOpenMath:    () -> Unit,
    onOpenChinese: () -> Unit,
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图
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

            // ===== 顶部 4 个 Tab =====
            // 布局规则:
            //   - 默认态:14sp,容器 29×21dp(位置不变)
            //   - 选中态(当前页所属学科):字号 20sp,容器 56×28dp
            //     + 文字背景 `img_yanwuchang_video_recommend_bg.png`(62×47dp,横向放置)
            //   - 4 个 Tab X 坐标固定(69/134/199/264),Y 固定 74
            //   - 选中态文字背景位置:以原"推荐"背景为参照,把背景对齐到当前 Tab 中心
            //     背景宽 62,容器宽 56,左右各留 3dp 内边距,偏移 = TabX - 3

            // "艺术" Tab
            CategoryTab(
                text         = "艺术",
                x            = 69.dp,
                isSelected   = category == "艺术",
                onClick      = onOpenArt,
            )
            // "科学" Tab
            CategoryTab(
                text         = "科学",
                x            = 134.dp,
                isSelected   = category == "科学",
                onClick      = onOpenScience,
            )
            // "数学" Tab
            CategoryTab(
                text         = "数学",
                x            = 199.dp,
                isSelected   = category == "数学",
                onClick      = onOpenMath,
            )
            // "语文" Tab
            CategoryTab(
                text         = "语文",
                x            = 264.dp,
                isSelected   = category == "语文",
                onClick      = onOpenChinese,
            )

            // 搜索图标
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

        // ===== 底部导航栏(与原页一致) =====
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
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_works),
                contentDescription = "作品",
                modifier = Modifier
                    .offset(x = 90.dp, y = 0.dp)
                    .size(width = 58.93.dp, height = 54.49.dp),
                contentScale = ContentScale.Fit,
            )
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_people),
                contentDescription = "我的",
                modifier = Modifier
                    .offset(x = 296.dp, y = 8.dp)
                    .size(width = 27.dp, height = 26.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            )
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

        // ===== 视频内容(用户名 / 标题 / 头像 / 点赞 / 评论 / 收藏 / 分享 / 进度条)=====
        // 与原 YanwuchangVideoScreen 完全一致,保持布局同步
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
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 532.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_comment),
            contentDescription = "评论",
            modifier = Modifier
                .offset(x = 374.dp, y = 571.dp)
                .size(width = 31.dp, height = 31.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 603.dp)
                .size(width = 22.dp, height = 16.dp),
        )
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_favorite),
            contentDescription = "收藏",
            modifier = Modifier
                .offset(x = 372.dp, y = 640.dp)
                .size(width = 34.dp, height = 34.dp),
            contentScale = ContentScale.Fit,
            alpha = 1f,
        )
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 674.dp)
                .size(width = 22.dp, height = 16.dp),
        )
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
        Text(
            text = "500",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 378.dp, y = 745.dp)
                .size(width = 22.dp, height = 16.dp),
        )
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
 * 顶部学科 Tab — 默认态 14sp / 29×21dp;选中态 20sp / 56×28dp + 背景图(62×47dp)
 *
 * @param text        学科文字
 * @param x           Tab 文字左边缘 X 坐标(与原 4 个 Tab 一致:69/134/199/264)
 * @param isSelected  是否为当前选中页(决定字号、容器尺寸、背景)
 * @param onClick     点击切换回调
 */
@Composable
private fun CategoryTab(
    text:       String,
    x:          androidx.compose.ui.unit.Dp,
    isSelected: Boolean,
    onClick:    () -> Unit,
) {
    if (isSelected) {
        // 选中态:文字背景 `img_yanwuchang_video_recommend_bg.png` 横向放置 62×47dp
        //   - 背景中心对齐文字中心;背景宽 62,文字容器宽 56,左右各 (62-56)/2 = 3dp 边距
        //   - Y 与原"推荐"背景保持一致(56dp),使背景底沿与文字底沿贴合
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_recommend_bg),
            contentDescription = null,
            modifier = Modifier
                .offset(x = x - 3.dp, y = 56.dp)
                .size(width = 62.dp, height = 47.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.7f,
        )
        // 选中态文字:20sp,容器 56×28dp,Y=66(让 28dp 容器底沿 Y=94 ≈ 原 14sp 容器底沿 Y=95)
        Text(
            text  = text,
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .offset(x = x, y = 66.dp)
                .size(width = 56.dp, height = 28.dp)
                .clickable(onClick = onClick),
        )
    } else {
        // 默认态:14sp,容器 29×21dp,Y=74(与原位置一致)
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
