package com.jueqiao.jianghu.ui.screens.yanwuchangvideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
 * 演武场视频首页 + 4 个学科分类页 — 统一 Composable(已按其他页 safe-area 模式适配)
 *
 * 结构(三段式):
 *   - Tier 1(外层 Box):全屏背景(img_yanwuchang_video_bg)
 *   - Tier 2(内层 Box):windowInsetsPadding(navigationBars) 避开系统导航条
 *     - 顶部行:返回 + 5 Tabs + 搜索(横向中轴 Y=92)
 *     - 视频内容:中心播放按钮 / 右侧操作列 / 用户名+标题 / 进度条
 *   - Tier 3(外层 Box 子节点):底部独立导航条(作品/我的)
 *     - Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
 *     - 内容(渐变背景 + 按钮)总高 85dp,加上 navigationBarsPadding 抵消系统导航条
 *
 * 元素定位(BoxScope 相对定位,与设计稿 412×917 对应,safe-area 869dp):
 *   - 顶部行:绝对 offset(X=20, Y=41~95),与其他页一致
 *   - 中心播放:align(Center)+offset 微调
 *   - 右侧操作列:align(CenterEnd)+offset,Box 内部按设计稿坐标转相对定位
 *   - 用户名/标题/进度:align(BottomStart)+offset(y=-X)— 让出底部 85dp 给独立导航条
 *
 * 5 个 Tab / 选中态 / 默认态 / 搜索 等行为不变,见 [CategoryTab]。
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

    // 点赞状态(参考 cai 分支:false = 空心, true = 实心 + 计数 +1)
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(500) }

    // 收藏状态(参考 cai 分支:false = 仅空心星, true = 中心叠加 Vector 600 标记 + 计数 +1)
    var isFavorited by remember { mutableStateOf(false) }
    var favoriteCount by remember { mutableStateOf(500) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ===== Tier 1: 全屏背景图(image 24.png, 412×917, 圆角 5dp)=====
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop,
        )

        // ===== Tier 2: 内容层(避开系统导航条)=====
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ----- 左上角返回按钮(32×32dp 容器,内含 24×24dp 图标)-----
            //   容器中心 Y=92(图标 24dp 居中:76+4=80 顶,80+24=104 底,中心 92)
            //   5 个 Tab 与搜索图标横向中轴线对齐 Y=92
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

            // ----- 顶部 5 个 Tab(均与返回键同一行 / 横向中轴 Y=92)-----
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
            // "推荐" Tab — 与 4 个学科 Tab 共用同一渲染逻辑
            CategoryTab(
                text         = "推荐",
                x            = 329.dp,
                isSelected   = selectedCategory == "推荐",
                onClick      = onOpenRecommend,
                isRecommend  = true,
            )

            // ----- 搜索图标(19×19dp, Y=83 让 19dp 容器中心 Y=92.5)-----
            Image(
                painter = painterResource(R.drawable.ic_yanwuchang_video_search),
                contentDescription = "搜索",
                modifier = Modifier
                    .offset(x = 383.dp, y = 83.dp)
                    .size(width = 19.dp, height = 19.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
            )

            // ===== 视频内容(5 个页面完全相同)=====
            // ----- 中心装饰圆 + 播放按钮(相对屏幕中心)-----
            //   原 (X=163, Y=395, 84×84),中心 (205, 437)
            //   safe-area 中心 (206, 434.5),偏移 (-1, +2.5)
            Image(
                painter = painterResource(R.drawable.ellipse_30),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -1.dp, y = 2.5.dp)
                    .size(width = 84.dp, height = 84.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFD9D9D9), BlendMode.SrcIn),
                alpha = 0.54f,
            )
            //   原 (X=181, Y=412, 51×49),中心 (206.5, 436.5)
            //   偏移 (+0.5, +2)
            Image(
                painter = painterResource(R.drawable.polygon_3),
                contentDescription = "播放",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 0.5.dp, y = 2.dp)
                    .size(width = 51.dp, height = 49.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFF6F6F6), BlendMode.SrcIn),
                alpha = 1f,
            )

            // ----- 右侧操作列(头像 + 点赞/评论/收藏/分享 + 嘴部装饰)— 相对 safe-area 中心 -----
            //   原设计稿(412×917, Y=444~761,整体上挪 80dp 让出底部 85dp 导航条)
            //   Box 总高 317,中心 Y=522.5;safe-area 中心 434.5,纵向偏移 +88
            //   X:列最右 X=407(parentW=412 - 5),Box 宽 40,左沿 367
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = -5.dp, y = 88.dp)
                    .size(width = 40.dp, height = 317.dp),
            ) {
                // 头像(原 X=367, Y=444, 40×40)— Box 内 (0, 0)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_avatar),
                    contentDescription = "头像",
                    modifier = Modifier
                        .offset(x = 0.dp, y = 0.dp)
                        .size(width = 40.dp, height = 40.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFF6B1B1), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 嘴部装饰线 Vector 593(原中心 379,456)— Box 内 (10.5, 8)
                Image(
                    painter = painterResource(R.drawable.vector_593),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 10.5.dp, y = 8.dp)
                        .size(width = 3.dp, height = 8.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 嘴部装饰线 Vector 594(原中心 395,454)— Box 内 (26.5, 5)
                Image(
                    painter = painterResource(R.drawable.vector_594),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 26.5.dp, y = 5.dp)
                        .size(width = 3.dp, height = 10.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 嘴部装饰线 Vector 595(原 X=382, Y=471)— Box 内 (15, 27)
                Image(
                    painter = painterResource(R.drawable.vector_595),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 15.dp, y = 27.dp)
                        .size(width = 16.dp, height = 5.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 点赞图标(原 X=374, Y=500, 30×28)— Box 内 (7, 56)
                //   空心 = ic_like_outline;已点赞 = ic_like_filled;点击切换状态,计数 +1 / -1
                Image(
                    painter = painterResource(
                        if (isLiked) R.drawable.ic_like_filled
                        else R.drawable.ic_like_outline
                    ),
                    contentDescription = if (isLiked) "已点赞" else "点赞",
                    modifier = Modifier
                        .offset(x = 7.dp, y = 56.dp)
                        .size(width = 30.dp, height = 28.dp)
                        .clickable {
                            isLiked = !isLiked
                            likeCount += if (isLiked) 1 else -1
                        },
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 点赞数(原 X=378, Y=532, 22×16)— Box 内 (11, 88)
                //   Compose Text 驱动(初值 500,点赞后 501),数字变化零缩放
                Text(
                    text  = likeCount.toString(),
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 11.dp, y = 88.dp)
                        .size(width = 22.dp, height = 16.dp),
                )
                // 评论图标(原 X=374, Y=571, 31×31)— Box 内 (7, 127)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment),
                    contentDescription = "评论",
                    modifier = Modifier
                        .offset(x = 7.dp, y = 127.dp)
                        .size(width = 31.dp, height = 31.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
                // 评论数(原 X=378, Y=603, 22×16)— Box 内 (11, 159)
                Image(
                    painter = painterResource(R.drawable.text_500),
                    contentDescription = "评论数",
                    modifier = Modifier
                        .offset(x = 11.dp, y = 159.dp)
                        .size(width = 22.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 收藏图标(原 X=372, Y=640, 34×34)— Box 内 (5, 196)
                //   点击切换状态,已收藏时计数 +1(500 → 501)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_favorite),
                    contentDescription = if (isFavorited) "已收藏" else "收藏",
                    modifier = Modifier
                        .offset(x = 5.dp, y = 196.dp)
                        .size(width = 34.dp, height = 34.dp)
                        .clickable {
                            isFavorited = !isFavorited
                            favoriteCount += if (isFavorited) 1 else -1
                        },
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 收藏 — 已收藏时,Vector 600 标记叠加在图标中心(16×15dp)— Box 内 (14, 205.5)
                //   收藏图标在 Box 内 (5, 196) size 34×34,中心 (22, 213)
                //   叠加层 size 16×15,中心对齐图标中心 → top-left (14, 205.5)
                if (isFavorited) {
                    Image(
                        painter = painterResource(R.drawable.vector_600),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = 14.dp, y = 205.5.dp)
                            .size(width = 16.dp, height = 15.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFF81A084), BlendMode.SrcIn),
                        alpha = 1f,
                    )
                }
                // 收藏数(原 X=378, Y=674, 22×16)— Box 内 (11, 230)
                //   Compose Text 驱动(初值 500,收藏后 501),数字变化零缩放
                Text(
                    text  = favoriteCount.toString(),
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 11.dp, y = 230.dp)
                        .size(width = 22.dp, height = 16.dp),
                )
                // 分享图标(原 X=374, Y=713, 32×28)— Box 内 (7, 269)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_share),
                    contentDescription = "分享",
                    modifier = Modifier
                        .offset(x = 7.dp, y = 269.dp)
                        .size(width = 32.dp, height = 28.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF7FA889), BlendMode.SrcIn),
                    alpha = 1f,
                )
                // 分享数(原 X=378, Y=745, 22×16)— Box 内 (11, 301)
                Image(
                    painter = painterResource(R.drawable.text_500),
                    contentDescription = "分享数",
                    modifier = Modifier
                        .offset(x = 11.dp, y = 301.dp)
                        .size(width = 22.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                    alpha = 1f,
                )
            }

            // ----- 用户名 / 视频标题(相对 safe-area 底部,上挪 80dp 让出底部条)-----
            //   原 (X=17, Y=745, 97×32) → Y=665, 底沿 697,距 safe-area 底(869)172dp
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_username),
                contentDescription = "用户名",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 17.dp, y = -172.dp)
                    .size(width = 97.dp, height = 32.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
                alpha = 1f,
            )
            //   原 (X=22, Y=788, 160×21) → Y=708, 底沿 729,距 safe-area 底 140dp
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_title),
                contentDescription = "视频标题",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 22.dp, y = -140.dp)
                    .size(width = 160.dp, height = 21.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFD0D0D0), BlendMode.SrcIn),
                alpha = 1f,
            )
            // ----- 视频进度条(相对 safe-area 底部)-----
            //   原 (X=20, Y=818, 372×8) → Y=738, 底沿 746,距 safe-area 底 123dp
            Image(
                painter = painterResource(R.drawable.group_177),
                contentDescription = "视频进度",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 20.dp, y = -123.dp)
                    .size(width = 372.dp, height = 8.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // ===== Tier 3: 底部独立导航条(作品 + 我的)— 不在 safe-area 内,独立绘制 =====
        //   Modifier.align(Alignment.BottomCenter) 吸在外层 Box 底部
        //   .fillMaxWidth() 宽度跟随屏幕(原硬编码 412dp,改为响应式)
        //   .wrapContentHeight() 高度由内容决定(85dp 渐变背景)
        //   .navigationBarsPadding() 底部加 nav bar 高度 padding,让"作品/我的"内容浮在系统导航条上方
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding(),
        ) {
            // 渐变背景(竖向 #8A9E7E → #81A879,固定 85dp 高)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    ),
            ) {
                // "作品"按钮(X=90, Y=0, 58.93×54.49)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_works),
                    contentDescription = "作品",
                    modifier = Modifier
                        .offset(x = 90.dp, y = 0.dp)
                        .size(width = 58.93.dp, height = 54.49.dp),
                    contentScale = ContentScale.Fit,
                )
                // "我的"图标(X=296, Y=8, 27×26)
                Image(
                    painter = painterResource(R.drawable.ic_yanwuchang_video_people),
                    contentDescription = "我的",
                    modifier = Modifier
                        .offset(x = 296.dp, y = 8.dp)
                        .size(width = 27.dp, height = 26.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn),
                )
                // "我的"文字(X=296, Y=34, 28×18, 14sp White)
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
        }
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
