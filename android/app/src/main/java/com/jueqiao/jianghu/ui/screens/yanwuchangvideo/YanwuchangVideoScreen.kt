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
    // 进度条(Group 177.png, X=20, Y=830, 372×8,圆角 5dp)
        //   - 底图为右侧灰条(#BEBEBE 渐变,整条 372×8)
        //   - 上叠左半进度(#FFFFFF,151×8,spacing=-2:右端伸出 2dp 覆盖接缝)
        // 渲染顺序:在底部导航栏之后绘制,确保不被遮挡
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_progress_bg),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 20.dp, y = 828.dp)
                .size(width = 372.dp, height = 8.dp)
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 828.dp)
                .size(width = 153.dp, height = 8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFFFFFFF)),
        )
    }
}
