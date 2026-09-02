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
 *   - "查看点赞"按钮:Group 199.png,位置 (286, 318),101×22dp
 *       圆角 10dp,Linear 渐变 #8A9E7E → #81A879 填充,不透明度 100%
 *       文字"查看点赞"(44×15, 11sp, 黑, 水平居中),叠在卡片上方
 *   - 页面底部"我的"页专用导航栏:位置 (0, 832),417×85dp(与"演武场视频首页"完全一致)
 *       左 — 图标 考研历史 (1) 1 (1).png(70×70, X=75, Y=823) + 文字"作品"(30×17, 14sp, 白)
 *       右 — 脸.png(58×49, X=265, Y=833)作为激活背景 + 图标 爪子 1.png(29×29, X=281, Y=838) + 文字"我的"(28×18, 14sp, 黑)  ← 当前激活
 *   - 左上角返回键(32×32dp 容器 / 24×24dp 图标):与演武场视频首页共用 ic_dahui_return,
 *       容器 (20, 76),与首页"返回"键完全对齐,方便从任意"我的"页回到演武场视频首页
 *
 * @param onBack       返回回调 — 点击左上角返回键 / 系统返回键 触发,回退到演武场视频首页
 * @param onOpenWorks  底部导航"作品"点击回调 — 回退到演武场视频首页(我的页 nav 中"作品"区域)
 * @param onOpenLikes  "查看点赞"按钮点击回调 — 打开点赞列表(目前无对应路由,仅占位)
 */
@Composable
fun YanwuchangVideoMyScreen(
    onBack: () -> Unit = {},
    onOpenWorks: () -> Unit = {},
    onOpenLikes: () -> Unit = {},
    onOpenBrowseRecord: () -> Unit = {},
    onOpenMyClass: () -> Unit = {},
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
            // "作品 / 收藏" 二选一:0 = 作品,1 = 收藏
            //   驱动下方 Vector 602 指示线在 X=60 / X=267 之间切换
            var selectedTab by remember { mutableStateOf(0) }
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

            // "查看点赞"按钮 — Group 199.png 容器(整体可点击)
            //   位置 (286, 318),尺寸 101×22dp
            //   圆角 10dp,填充 Linear 渐变 #8A9E7E → #81A879(与底部导航栏共用),不透明度 100%
            Box(
                modifier = Modifier
                    .offset(x = 286.dp, y = 318.dp)
                    .size(width = 101.dp, height = 22.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    )
                    .clickable(onClick = onOpenLikes),
            ) {
                // Group 199.png(原图 101×22,圆角矩形带装饰角)
                //   容器内 (0, 0),101×22dp,ContentScale.FillBounds 完整填充
                //   先绘制在底层,作为按钮底板
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_group_199),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = 1f,
                )
                // 文字"查看点赞"— 绝对 (314, 321) → 容器内 local (28, 3),44×15,11sp,黑色 #000000
                //   后绘制,叠在 Group 199.png 之上,确保文字可见
                //   水平居中于 44dp 宽容器(与按钮中轴线对齐)
                Text(
                    text  = "查看点赞",
                    color = Color(0xFF000000),
                    style = TextStyle(
                        fontSize = 11.sp,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 28.dp, y = 3.dp)
                        .size(width = 44.dp, height = 15.dp),
                )
            }

            // ===== "浏览记录" 条目(168×38dp 圆角卡片 + 25×25dp 图标 + 文字) =====
            //   绝对定位:父 (30, 374) 168×38, 圆角 10dp
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 374.dp)
                    .size(width = 168.dp, height = 38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    )
                    .clickable(onClick = onOpenBrowseRecord),
            ) {
                // 卡片背景图(可选,Rectangle 214 本身就是渐变胶囊;若需叠加纹理可放开)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_rectangle_214),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )

                // 脚印图标(25×25dp),位于卡片内 (34, 6) → 距卡片左边 34dp,上 6dp
                // 保留原图绿色,与"我的班级"图标颜色一致
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_browse),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 34.dp, y = 6.dp)
                        .size(width = 25.dp, height = 25.dp),
                )

                // 文字"浏览记录"(16sp, 黑色)
                // 容器宽 100dp 留足 4 个汉字(每字 ~16sp),不再受 64dp 截断;文字垂直居中
                Text(
                    text  = "浏览记录",
                    color = Color(0xFF000000),
                    style = TextStyle(
                        fontSize = 16.sp,
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .offset(x = 64.dp, y = 6.dp)
                        .size(width = 96.dp, height = 20.dp),
                )
            }

            // ===== "我的班级" 条目(168×38dp 圆角卡片 + 30×30dp 图标 + 文字) =====
            //   绝对定位:父 (219, 374) 168×38, 圆角 10dp
            Box(
                modifier = Modifier
                    .offset(x = 219.dp, y = 374.dp)
                    .size(width = 168.dp, height = 38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8A9E7E),
                                Color(0xFF81A879),
                            ),
                        ),
                    )
                    .clickable(onClick = onOpenMyClass),
            ) {
                // 卡片背景图(Rectangle 214 本身就是渐变胶囊;此处复用同款底图)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_rectangle_214),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )

                // 班级图标(30×30dp),位于卡片内 (37, 4) → 距卡片左边 37dp,上 4dp
                // 保留原图绿色,与"浏览记录"图标颜色一致
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_class),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 37.dp, y = 4.dp)
                        .size(width = 30.dp, height = 30.dp),
                )

                // 文字"我的班级"(16sp, 黑色)
                // 容器宽 100dp 留足 4 个汉字(每字 ~16sp),不再受 64dp 截断;文字垂直居中
                Text(
                    text  = "我的班级",
                    color = Color(0xFF000000),
                    style = TextStyle(
                        fontSize = 16.sp,
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .offset(x = 68.dp, y = 6.dp)
                        .size(width = 96.dp, height = 20.dp),
                )
            }

            // ===== "作品" 标题(40×26dp, 20sp, 黑色) =====
            //   绝对定位:(85, 432),点击后切换 selectedTab = 0,指示线回到 X=60
            Text(
                text  = "作品",
                color = Color(0xFF000000),
                style = TextStyle(
                    fontSize = 20.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 85.dp, y = 432.dp)
                    .size(width = 40.dp, height = 26.dp)
                    .clickable { selectedTab = 0 },
            )

            // ===== "作品" 下方高亮指示器(Vector 602, 90×3dp, #5C5C33, 圆头) =====
            //   绝对定位 X 随 selectedTab 切换:0 → X=60(作品),1 → X=267(收藏)
            //   Y 固定 463,size 固定 90×3dp
            //   端点圆头由原图圆角实现,不需额外修饰
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_indicator_602),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = if (selectedTab == 0) 60.dp else 267.dp, y = 463.dp)
                    .size(width = 90.dp, height = 3.dp),
            )

            // ===== "收藏" 标题(40×26dp, 20sp, 黑色) =====
            //   绝对定位:(292, 432),点击后切换 selectedTab = 1,指示线移到 X=267
            Text(
                text  = "收藏",
                color = Color(0xFF000000),
                style = TextStyle(
                    fontSize = 20.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 292.dp, y = 432.dp)
                    .size(width = 40.dp, height = 26.dp)
                    .clickable { selectedTab = 1 },
            )

            // ===== 全屏宽水平分割线(Vector 601, 412×2dp,#A4A495) =====
            //   绝对定位:父 (0, 463) 412×2 — Figma 标注 height 0 实际是 1~2px 的细线
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_divider_601),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 0.dp, y = 463.dp)
                    .size(width = 412.dp, height = 2.dp),
            )

            // ===== 分割线下方内容区(根据 selectedTab 切换"作品" / "收藏") =====
            //   区域:从 Y=465 一直到 Y=832(底部 nav 上沿),共 367dp 高、412dp 宽
            //   当前为占位实现,后续接入真实数据列表时直接替换 if/else 分支即可
            if (selectedTab == 0) {
                // TODO: 接入"作品"列表
                // ----- 单张作品缩略卡(image 17, 132×175dp) -----
                //   父容器 (13, 473) 132×175,背景 #000000 不透明度 38%
                //   image 17 本身不透明度 100%,叠在黑色 38% 背景上
                Box(
                    modifier = Modifier
                        .offset(x = 13.dp, y = 473.dp)
                        .size(width = 132.dp, height = 175.dp)
                        .background(color = Color(0xFF000000).copy(alpha = 0.38f)),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_yanwuchang_video_my_image_17),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // 圆形播放按钮底(Ellipse 30, 44×44dp,#D9D9D9 54% 不透明度)
                    //   父容器内绝对定位:(57-13, 538-473) = (44, 65) — 即在 132×175 卡内的 (44, 65) 处
                    //   容器中心 X = 13+57+22 = 92(对应父 (13,473) 132×175 中心)
                    Box(
                        modifier = Modifier
                            .offset(x = 44.dp, y = 65.dp)
                            .size(width = 44.dp, height = 44.dp)
                            .background(
                                color = Color(0xFFD9D9D9).copy(alpha = 0.54f),
                                shape = CircleShape,
                            ),
                    ) {
                        // 三角播放图标(Polygon 2, 31×29dp,#D9D9D9 100% 不透明度)
                        //   在 44×44 圆内的局部偏移:(64-57, 544-538) = (7, 6) → 居中显示
                        Image(
                            painter = painterResource(R.drawable.img_yanwuchang_video_my_polygon_2),
                            contentDescription = null,
                            modifier = Modifier
                                .offset(x = 7.dp, y = 6.dp)
                                .size(width = 31.dp, height = 29.dp),
                        )
                    }
                }
            } else {
                // TODO: 接入"收藏"列表
                // ----- "还没有收藏哦" 空状态文字 -----
                //   绝对定位:(146, 612) 120×26dp,20sp,#97B1B5(青灰色)@ 100%
                Text(
                    text  = "还没有收藏哦",
                    color = Color(0xFF97B1B5),
                    style = TextStyle(
                        fontSize = 20.sp,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 146.dp, y = 612.dp)
                        .size(width = 120.dp, height = 26.dp),
                )
            }

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
                // ===== 左侧"作品"(整体可点击 — 切换回"演武场视频首页") =====
                // 命中范围:绝对 (75, 823) 至 (155, 893) → 容器内 local (75, -9) 80×72dp
                //   图标(75, -9) 70×70 + 文字(98, 46) 30×17 都在此区域内
                Box(
                    modifier = Modifier
                        .offset(x = 75.dp, y = (-9).dp)
                        .size(width = 80.dp, height = 72.dp)
                        .clickable(onClick = onOpenWorks),
                ) {
                    // 图标 考研历史 (1) 1 (1).png — 容器内 (0, 0) 70×70dp
                    Image(
                        painter = painterResource(R.drawable.img_yanwuchang_video_my_works_2),
                        contentDescription = "作品",
                        modifier = Modifier
                            .size(width = 70.dp, height = 70.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 1f,
                    )
                    // 文字 "作品"— 容器内 (23, 55),30×17,14sp,白色
                    Text(
                        text  = "作品",
                        color = Color(0xFFFFFFFF),
                        style = TextStyle(
                            fontSize = 14.sp,
                        ),
                        modifier = Modifier
                            .offset(x = 23.dp, y = 55.dp)
                            .size(width = 30.dp, height = 17.dp),
                    )
                }

                // ===== 右侧"我的"(当前激活) =====
                // 脸.png — 绝对 (265, 833) → 容器内 local (265, 1),58×49dp
                //   位于"我的"图标和文字背后,作为激活高亮背景
                //   在 Box 子元素中先绘制(在图标/文字前),Compose 默认后绘制覆盖先绘制 → 脸.png 在底层
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_my_nav_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 265.dp, y = 1.dp)
                        .size(width = 58.dp, height = 49.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
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
