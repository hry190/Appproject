package com.jueqiao.jianghu.ui.screens.yanwuchangvideobrowserecord

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import java.util.Calendar

/**
 * 演武场视频 — "浏览记录"页
 *
 *  - 进入路径:演武场视频 → 我的 → 点击"浏览记录"卡片
 *  - 退出路径:点击左上角返回键 / 系统返回手势
 *  - 页面背景:image 43.png(412×917,满屏),不透明度 80%
 *  - 顶部居中:页面标题"浏览记录"(24sp, Bold, 白色)
 *  - 顶部右侧:实时日期日历图标(背景图上叠"今日几号"文字)
 *  - 页面中部:渐变光柱(Modifier.blur(4.6.dp) 模拟 Figma "Uniform Blur 4.6")
 */
@Composable
fun YanwuchangVideoBrowseRecordScreen(
    onBack: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(回退到"演武场视频我的"页)
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(image 43.png, 412×917,1:1 像素对应画布)
        //   满铺整个 412×917 画布,ContentScale.FillBounds 让图填满容器
        //   alpha = 0.8f 实现 Figma 标注的"背景不透明度 80%"
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.8f,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ===== 中部竖向光柱(渐变 + Layer Blur 模拟) =====
            //   原图 57×745,显示尺寸 47×735dp
            //   Figma 标注 "start Progressive 0 / end Progressive 4.6 / Uniform Blur 4.6"
            //   Compose 用 Modifier.blur(4.6.dp) 近似实现"均匀模糊 4.6",得到接近 Figma 视觉效果
            //   绝对定位:(181, 109)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_glow),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 181.dp, y = 109.dp)
                    .size(width = 47.dp, height = 735.dp)
                    .blur(radius = 4.6.dp),
                contentScale = ContentScale.FillBounds,
            )

            // ===== 左上角返回按钮(32×32dp 容器,内含 24×24dp 图标) =====
            //   容器中心 Y=92(与"演武场视频我的"页返回键完全对齐)
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

            // ===== 页面标题"浏览记录"(24sp, Bold, 白色) =====
            //   绝对定位:(158, 70) 96×32dp
            Text(
                text  = "浏览记录",
                color = Color(0xFFFFFFFF),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 158.dp, y = 70.dp)
                    .size(width = 96.dp, height = 32.dp),
            )

            // ===== 右上角实时日期日历(原图外框 + 实时文字) =====
            //   容器 (350, 70) 42×42dp
            //   - 底层:原日历图(img_yanwuchang_video_browse_record_calendar)提供外框/圆角/装饰
            //   - 中层:与原图同色色块(深绿 14dp / 浅绿 28dp)覆盖原字,把"四月/19"彻底抹掉
            //   - 顶层:实时绘制的"X月"白字 + "日期"深绿字
            //   效果:日历框视觉与原图一致,数字永远跟随系统日期
            val (monthCn, dayOfMonth) = currentMonthDay()
            Box(
                modifier = Modifier
                    .offset(x = 350.dp, y = 70.dp)
                    .size(width = 42.dp, height = 42.dp),
            ) {
                // 底层:原日历图(完整外框 + 装饰)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_calendar),
                    contentDescription = "日历",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                // 中层 1:覆盖原"X月"区域 — 深绿渐变 14dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4F8C5A),
                                    Color(0xFF5C9A66),
                                ),
                            ),
                        ),
                )
                // 中层 2:覆盖原"日期"区域 — 浅绿渐变 28dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFC7E1C2),
                                    Color(0xFFB5D5B0),
                                ),
                            ),
                        ),
                )
                // 顶层:"X月" 实时白字
                Text(
                    text  = monthCn,
                    color = Color(0xFFFFFFFF),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .align(Alignment.TopCenter),
                )
                // 顶层:"日期" 实时深绿字
                Text(
                    text  = dayOfMonth,
                    color = Color(0xFF1F8A3F),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.BottomCenter),
                )
            }

            // ===== 第一张"浏览记录"卡片 =====
            //   整体 6 层结构,严格按 Figma z-order 叠加:
            //     1) Rectangle 237 卡片底(20, 134) 165×130,圆角 11 — 奶油色 #FFFAE6 99% 已是原图自带
            //     2) Rectangle 243 装饰线(26, 138) 154×121,圆角 11 — 渐变 #Linear(原图自带)
            //     3) image 25 缩略图(36, 145) 61×93,黑色 24% 底 + image 100%
            //     4) Rectangle 235 按钮底(106, 235) 70×20,圆角 4 — 渐变 #8C9E7F→#A9B496(原图)
            //     5) Rectangle 236 按钮内描边(108, 237) 65×15,圆角 4 — 渐变 #B1B57F→#3B572F(原图)
            //     6) "查看" 文字(131, 238) 20×13,10sp 白色 @ 100%
            //   卡片整体不可点(后续如需点击跳视频,把 clickable 加在最外层 Box 即可)
            // ---- 第 1 层:Rectangle 237 卡片底 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_bg),
                contentDescription = "浏览记录卡片",
                modifier = Modifier
                    .offset(x = 20.dp, y = 134.dp)
                    .size(width = 165.dp, height = 130.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 2 层:Rectangle 243 装饰线 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_decor),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 26.dp, y = 138.dp)
                    .size(width = 154.dp, height = 121.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 3 层:缩略图(image 25, 黑色 24% 底 + image 100%) ----
            //   容器用 Box 装"底色 + image 满铺"两层,模拟 Figma "container 24% / image 100%"
            Box(
                modifier = Modifier
                    .offset(x = 36.dp, y = 145.dp)
                    .size(width = 61.dp, height = 93.dp)
                    .background(color = Color(0xFF000000).copy(alpha = 0.24f)),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_thumb),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            // ---- 第 4 层:Rectangle 235 按钮底(渐变绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_bg),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 106.dp, y = 235.dp)
                    .size(width = 70.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 5 层:Rectangle 236 按钮内描边(深绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_line),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 108.dp, y = 237.dp)
                    .size(width = 65.dp, height = 15.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 6 层:"查看" 文字 ----
            Text(
                text  = "查看",
                color = Color(0xFFFFFFFF),
                style = TextStyle(
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 131.dp, y = 238.dp)
                    .size(width = 20.dp, height = 13.dp),
            )

            // ===== 第二张"浏览记录"卡片(右侧) =====
            //   6 层结构,严格按 Figma z-order 叠加,坐标与第一张卡相对偏移 (X+206~+208, Y+208~+212)
            //     1) Rectangle 237 卡片底(226, 342) 165×130,圆角 11
            //     2) Rectangle 243 装饰线(232, 346) 154×121,圆角 11
            //     3) 视频 (2).png 缩略图(241, 357) 64×99,圆角 5,黑色 27% 底 + image 100%
            //     4) Rectangle 235 按钮底(312, 443) 70×20,圆角 4
            //     5) Rectangle 236 按钮内描边(315, 445) 65×15,圆角 4
            //     6) "查看" 文字(337, 446) 20×13,10sp 白色 @ 100%
            // ---- 第 1 层:Rectangle 237 卡片底 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_bg),
                contentDescription = "浏览记录卡片 2",
                modifier = Modifier
                    .offset(x = 226.dp, y = 342.dp)
                    .size(width = 165.dp, height = 130.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 2 层:Rectangle 243 装饰线 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_decor),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 232.dp, y = 346.dp)
                    .size(width = 154.dp, height = 121.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 3 层:视频缩略图(视频 (2).png, 黑色 27% 底 + image 100%) ----
            Box(
                modifier = Modifier
                    .offset(x = 241.dp, y = 357.dp)
                    .size(width = 64.dp, height = 99.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color = Color(0xFF000000).copy(alpha = 0.27f)),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_video_thumb),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            // ---- 第 3a 层:卡 2 缩略图中心播放按钮(圆形 + 三角) ----
            //   严格按用户给的 Figma 坐标:圆 (262, 359) 23×23,三角 (265, 398) 16×15
            //   注:圆与三角垂直不对称(差 39dp),可能为设计特殊状态(按下/暂停帧)
            //   复用卡 3 的播放按钮 drawable,水平都居中于卡 2 缩略图 X=273
            //   圆 X 中心 = 262+11.5 = 273.5;三角 X 中心 = 265+8 = 273 — 都 ≈ 缩略图中心 273
            //   圆 Y 中心 = 359+11.5 = 370.5(偏上);三角 Y 中心 = 398+7.5 = 405.5(偏下)
            Box(
                modifier = Modifier
                    .offset(x = 262.dp, y = 359.dp)
                    .size(width = 23.dp, height = 23.dp)
                    .background(
                        color = Color(0xFFD9D9D9).copy(alpha = 0.54f),
                        shape = CircleShape,
                    ),
            )
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_play_triangle),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 265.dp, y = 398.dp)
                    .size(width = 16.dp, height = 15.dp),
            )
            // ---- 第 4 层:Rectangle 235 按钮底(渐变绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_bg),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 312.dp, y = 443.dp)
                    .size(width = 70.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 5 层:Rectangle 236 按钮内描边(深绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_line),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 315.dp, y = 445.dp)
                    .size(width = 65.dp, height = 15.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 6 层:"查看" 文字 ----
            Text(
                text  = "查看",
                color = Color(0xFFFFFFFF),
                style = TextStyle(
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 337.dp, y = 446.dp)
                    .size(width = 20.dp, height = 13.dp),
            )

            // ===== 第三张"浏览记录"卡片(左列第三行) =====
            //   6 层结构,严格按 Figma z-order 叠加,坐标与第一张卡 Y 偏移 +413
            //     1) Rectangle 237 卡片底(20, 547) 165×130,圆角 11
            //     2) Rectangle 243 装饰线(26, 551) 154×121,圆角 11
            //     3) image 17 (1).png 缩略图(33, 558) 64×99,圆角 5,黑色 27% 底 + image 100%
            //     4) Rectangle 235 按钮底(106, 648) 70×20,圆角 4
            //     5) Rectangle 236 按钮内描边(108, 650) 65×15,圆角 4
            //     6) "查看" 文字(131, 651) 20×13,10sp 白色 @ 100%
            // ---- 第 1 层:Rectangle 237 卡片底 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_bg),
                contentDescription = "浏览记录卡片 3",
                modifier = Modifier
                    .offset(x = 20.dp, y = 547.dp)
                    .size(width = 165.dp, height = 130.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 2 层:Rectangle 243 装饰线 ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_card_decor),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 26.dp, y = 551.dp)
                    .size(width = 154.dp, height = 121.dp)
                    .clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 3 层:缩略图(image 17 (1).png, 黑色 27% 底 + image 100%) ----
            Box(
                modifier = Modifier
                    .offset(x = 33.dp, y = 558.dp)
                    .size(width = 64.dp, height = 99.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color = Color(0xFF000000).copy(alpha = 0.27f)),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_thumb2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            // ---- 第 3a 层:缩略图中心播放按钮(圆形 + 三角) ----
            //   圆形 (56, 592) 23×23dp — 浅灰 #D9D9D9 @ 54% 不透明度,半透叠加在缩略图上
            //   三角 (60, 596) 16×15dp — 浅灰 #D9D9D9 @ 100% 不透明度
            //   视觉:与"演武场视频我的"页作品卡播放按钮同款风格
            //   中心对齐:23/2 = 11.5dp 圆心,三角 (16,15) 在圆内偏移 (4, 4) 居中
            Box(
                modifier = Modifier
                    .offset(x = 56.dp, y = 592.dp)
                    .size(width = 23.dp, height = 23.dp)
                    .background(
                        color = Color(0xFFD9D9D9).copy(alpha = 0.54f),
                        shape = CircleShape,
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_play_triangle),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(width = 16.dp, height = 15.dp),
                )
            }
            // ---- 第 4 层:Rectangle 235 按钮底(渐变绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_bg),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 106.dp, y = 648.dp)
                    .size(width = 70.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 5 层:Rectangle 236 按钮内描边(深绿) ----
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_browse_record_btn_line),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 108.dp, y = 650.dp)
                    .size(width = 65.dp, height = 15.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit,
            )
            // ---- 第 6 层:"查看" 文字 ----
            Text(
                text  = "查看",
                color = Color(0xFFFFFFFF),
                style = TextStyle(
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 131.dp, y = 651.dp)
                    .size(width = 20.dp, height = 13.dp),
            )
        }
    }
}

/**
 * 读取当前"几月几日",返回中文月份("一月"…"十二月")+ 两位日("01"–"31")
 * - 每次 Composable 重组时重新计算,反映当下日期
 * - 如需在跨日时自动刷新,可在外部包一层 `LaunchedEffect(Unit) { while(true) { delay(...); ... } }`
 */
private fun currentMonthDay(): Pair<String, String> {
    val cal = Calendar.getInstance()
    val month = cal.get(Calendar.MONTH) + 1  // Calendar.MONTH 0-based
    val day   = cal.get(Calendar.DAY_OF_MONTH)
    val monthCn = when (month) {
        1  -> "一月"
        2  -> "二月"
        3  -> "三月"
        4  -> "四月"
        5  -> "五月"
        6  -> "六月"
        7  -> "七月"
        8  -> "八月"
        9  -> "九月"
        10 -> "十月"
        11 -> "十一月"
        12 -> "十二月"
        else -> "${month}月"
    }
    val dayStr = if (day < 10) "0$day" else day.toString()
    return monthCn to dayStr
}
