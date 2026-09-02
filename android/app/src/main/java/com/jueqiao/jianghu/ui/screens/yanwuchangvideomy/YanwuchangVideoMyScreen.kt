package com.jueqiao.jianghu.ui.screens.yanwuchangvideomy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R

/**
 * 演武场视频 — "我的"页
 *
 * 点击演武场视频首页底部导航栏的"我的"图标(爪子)进入。
 *
 * 当前布局:
 *   - 全屏背景:室内家园要求 1 (2).png(img_yanwuchang_video_my_bg, 原图 412×382, 1:1 像素对应)
 *       用 Modifier.size(412.dp, 382.dp) 保持原图 1:1 像素对应,ContentScale.Fit 不裁剪不变形
 *       顶部贴齐画布(Alignment.TopCenter),图片下方留出空白画布底色
 *   - 亭台楼阁图:未标题-1 69 (2).png(img_yanwuchang_video_my_pavilion)
 *       位置 (0, 230),尺寸 417×687dp
 *   - 装饰圆角图标:Group 165.png(img_yanwuchang_video_my_group_165)
 *       位置 (23, 181),尺寸 72×72dp,背景填充 #A5C2AB,不透明度 100%
 *   - 获赞模块:数字 + "获赞"文字,Y=276 起始,57×64dp 区域内
 *       数字 1(24sp,黑)+ "获赞"(24sp,黑),均水平居中于 57dp 宽容器
 *   - 页面底部"我的"页专用导航栏:位置 (0, 832),417×85dp(与"演武场视频首页"完全一致)
 *       左 — 图标 考研历史 (1) 1.png(58.93×54.49, X=90, Y=0, 白) + 文字"作品"(30×17, 14sp, 白)
 *       右 — 图标 爪子 1.png(27×26, X=296, Y=8) + 文字"我的"(28×18, 14sp, 黑)  ← 当前激活
 *   - 左上角返回键(32×32dp 容器 / 24×24dp 图标):与演武场视频首页共用 ic_dahui_return,
 *       容器 (20, 76),与首页"返回"键完全对齐,方便从任意"我的"页回到演武场视频首页
 *
 * @param onBack  返回回调 — 点击左上角返回键 / 系统返回键 触发,回退到演武场视频首页
 */
@Composable
fun YanwuchangVideoMyScreen(
    onBack: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到演武场视频首页)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(室内家园要求 1 (2).png, 原图 412×382)
        //   1:1 像素对应 412.dp×382.dp,ContentScale.Fit 不裁剪不变形
        //   Alignment.TopCenter:水平居中、顶部贴齐画布顶
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_bg),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 412.dp, height = 382.dp),
                contentScale = ContentScale.Fit,
            )
        }

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 亭台楼阁图(未标题-1 69 (2).png)
            //   位置 (0, 230),尺寸 417×687dp
            //   (图片宽 417 略大于画布 412,右侧溢出 5dp,按设计保留)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_pavilion),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 0.dp, y = 230.dp)
                    .size(width = 417.dp, height = 687.dp),
                contentScale = ContentScale.Fit,
            )

            // 装饰圆形图标(Group 165.png)
            //   位置 (23, 168),尺寸 72×72dp(正方形,clip(CircleShape) 切圆形)
            //   原 Y=181,向上移 13dp 至 Y=168,与右侧"哈哈哈"姓名 (Y=170) + ID (Y=223) 文本块垂直居中
            //   背景填充 #A5C2AB,不透明度 100%
            Box(
                modifier = Modifier
                    .offset(x = 23.dp, y = 168.dp)
                    .size(width = 72.dp, height = 72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA5C2AB)),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_group_165),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
            }

            // 姓名"哈哈哈"(哈哈哈.png, 24px)
            //   位置 (104, 170),尺寸 72×32dp
            //   文字填充色 #FFFFFF,不透明度 100%
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_haha),
                contentDescription = "哈哈哈",
                modifier = Modifier
                    .offset(x = 104.dp, y = 170.dp)
                    .size(width = 72.dp, height = 32.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                alpha = 1f,
            )

            // ID"ID：1326528988"(ID：1326528988.png, 12px)
            //   位置 (111, 223),尺寸 96×16dp
            //   文字填充色 #FFFFFF,不透明度 100%
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_id),
                contentDescription = "ID：1326528988",
                modifier = Modifier
                    .offset(x = 111.dp, y = 223.dp)
                    .size(width = 96.dp, height = 16.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color(0xFFFFFFFF), BlendMode.SrcIn),
                alpha = 1f,
            )

            // 获赞数字 + "获赞"文字
            //   容器位置 (30, 276),尺寸 57×64dp
            //   内部:第 1 行 动态 Text 数字"1"(24sp) + 第 2 行 "获赞"(24sp)
            //   数字与文字共用同一 57dp 宽容器,textAlign = Center 让两者视觉上共用中轴线 X=58.5
            //   整体填充色 #000000,不透明度 100%
            //   likeCount 默认为 1,后续可由业务逻辑变化
            var likeCount by remember { mutableStateOf(1) }
            // 第 1 行 数字"1"(24sp,黑色,水平居中于 57dp 宽容器)
            Text(
                text  = likeCount.toString(),
                color = Color(0xFF000000),
                style = TextStyle(
                    fontSize = 24.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 30.dp, y = 276.dp)
                    .size(width = 57.dp, height = 32.dp),
            )
            // 第 2 行 "获赞"(24sp,黑色,水平居中于 57dp 宽容器 — 与"1"共用中轴线)
            Text(
                text  = "获赞",
                color = Color(0xFF000000),
                style = TextStyle(
                    fontSize = 24.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 30.dp, y = 308.dp)
                    .size(width = 57.dp, height = 32.dp),
            )

            // ===== 左上角返回按钮(32×32dp 容器,内含 24×24dp 图标) =====
            //   - 容器中心 Y=92(与演武场视频首页"返回"键完全对齐)
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

            // 页面底部"我的"页专用导航栏
            //   容器位置 (0, 832),尺寸 417×85dp(画布 412dp,水平各溢出 2.5dp,按设计保留)
            //   位置与"演武场视频首页"完全一致(首页 nav Y=832,高 85dp)
            //   背景:竖向 Linear 渐变 #8A9E7E → #81A879(与演武场视频首页底部导航栏共用),不透明度 100%
            //   内部元素(画布绝对坐标 — 用户指定):
            //     左侧(作品):
            //       图标 考研历史 (1) 1 (1).png  (X=75,  Y=823) 70×70dp(容器内 local Y = 823-832 = -9)
            //       文字 "作品"                  (X=98,  Y=878) 30×17dp 14sp 白色 #FFFFFF 100%(local Y = 46)
            //     右侧(我的,当前激活):
            //       图标 爪子 1.png              (X=281, Y=838) 29×29dp(local Y = 6)
            //       文字 "我的"                  (X=282, Y=863) 28×18dp 14sp 黑色 #000000 100%(local Y = 31)
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 832.dp)
                    .size(width = 417.dp, height = 85.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    ),
            ) {
                // ===== 左侧"作品" =====
                // 图标 考研历史 (1) 1 (1).png — 绝对 (75, 823) → 容器内 local (75, -9)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_works_2),
                    contentDescription = "作品",
                    modifier = Modifier
                        .offset(x = 75.dp, y = (-9).dp)
                        .size(width = 70.dp, height = 70.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
                // 文字 "作品"— 绝对 (98, 878) → 容器内 local (98, 46),30×17,14sp,白色
                Text(
                    text  = "作品",
                    color = Color(0xFFFFFFFF),
                    style = TextStyle(
                        fontSize = 14.sp,
                    ),
                    modifier = Modifier
                        .offset(x = 98.dp, y = 46.dp)
                        .size(width = 30.dp, height = 17.dp),
                )

                // ===== 右侧"我的"(当前激活) =====
                // 图标 爪子 1.png — 绝对 (281, 838) → 容器内 local (281, 6),29×29dp
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_paw),
                    contentDescription = "我的",
                    modifier = Modifier
                        .offset(x = 281.dp, y = 6.dp)
                        .size(width = 29.dp, height = 29.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
                // 文字 "我的"— 绝对 (282, 863) → 容器内 local (282, 31),28×18,14sp,黑色
                Text(
                    text  = "我的",
                    color = Color(0xFF000000),
                    style = TextStyle(
                        fontSize = 14.sp,
                    ),
                    modifier = Modifier
                        .offset(x = 282.dp, y = 31.dp)
                        .size(width = 28.dp, height = 18.dp),
                )
            }
        }
    }
}
