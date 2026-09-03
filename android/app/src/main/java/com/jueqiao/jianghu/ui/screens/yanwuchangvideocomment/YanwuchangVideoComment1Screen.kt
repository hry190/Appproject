package com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
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
 *   - 评论框背景:未标题-1 69 (3).png(img_yanwuchang_video_comment1_bg)
 *       位置 (0, 393),尺寸 417×523dp — 略宽于画布,右侧溢出 5dp(按设计保留)
 *   - 顶部 X 关闭按钮:Group 141 (2).png(img_yanwuchang_video_comment1_group141)
 *       位置 (17, 418),尺寸 17×17dp, 填色 #6D8470, 不透明度 100%
 *       点击 → 关闭评论1页,回到演武场视频首页(onBack)
 *   - 顶部 "放大缩小" 按钮:放大缩小 (2).png(img_yanwuchang_video_comment1_zoom)
 *       位置 (54, 418),尺寸 17×17dp, 填色 #6D8470, 不透明度 100%
 *       (源图属性:opacity 54%, position mixed, weight 1, end point mixed)
 *       点击 → 展开为全屏评论2页(onOpenExpanded)
 *   - "500 条评论" 标题(Text 渲染,与 Comment2 页风格一致)
 *       位置 (160, 443),96×18dp 容器,14sp,YaHei,填色 #6D8470,不透明度 100%
 *       letterSpacing 0.25em(原 41% 会裁字,改为 25% 保证 7 个字完整显示)
 *   - 4 个评论项装饰(Group 179.png,group_179_v2)
 *       位置:均 X=20,Y 依次为 493 / 593 / 693 / 793,尺寸 104×50dp
 *       垂直等距 100dp,位于评论框背景图(Y=393~916)内
 *   - 4 条评论文本("我最欣赏的...")
 *       第 1 条 位置 (81, 517),后 3 条 位置 (79, 617) / (79, 717) / (79, 817)
 *       尺寸 313×36dp,14sp,YaHei,填色 #000000,不透明度 100%
 *       位置在每个 Group 179 项内垂直居中
 *   - 4 个评论点赞图标 + 数字(各评论右侧)
 *       未点赞:Like (喜欢) (1).png(img_yanwuchang_video_comment1_like),17×16dp
 *       已点赞:Like (喜欢) (2).png(img_yanwuchang_video_comment1_like_filled),17×16dp
 *         (Y 序列 559/659/759/859,X 序列 358/356/356/356)
 *       点击 → 切换 空心(1).png / 实心(2).png
 *       触摸区域扩大到 44×44dp(符合 Material 最小点击规范)
 *       数字 "50": 15×16dp,12sp,YaHei,填色 #000000,不透明度 100%
 *         X 序列 377/375/375/375(爱心 X + 爱心宽 17 + 间距 2)
 *         Y 序列与爱心顶对齐
 *       点击爱心 → isLiked 翻转 同时 likeCount ±1(默认 50)
 *   - 底部 "互评" 按钮(背景图 + 文字):
 *       背景:未标题-2-恢复的 12 (1).png(img_yanwuchang_video_comment1_reply, 372×47)
 *         位置 (22, 834),尺寸 372×47dp
 *       文字:"互评" @ (188, 844),40×26dp 区域,20sp,YaHei,白色 #FFFFFF,不透明度 100%
 *         (源图属性:opacity 88%)
 *       整体可点击,目前为占位
 *
 * @param onBack         返回回调(预留,当前未使用)
 * @param onOpenExpanded 打开全屏展开页回调(预留,当前未使用)
 */
@Composable
fun YanwuchangVideoComment1Screen(
    onBack: () -> Unit = {},
    onOpenExpanded: () -> Unit = {},
) {
    // 4 条评论的点赞状态 + 点赞数(独立维护,互不影响)
    //   isLiked  false = 未点赞:空心 heart 原色
    //            true  = 已点赞:实心 heart (Like (喜欢) (2).png)
    //   likeCount 默认 50,点击爱心同步 ±1
    var isLiked1 by remember { mutableStateOf(false) }
    var isLiked2 by remember { mutableStateOf(false) }
    var isLiked3 by remember { mutableStateOf(false) }
    var isLiked4 by remember { mutableStateOf(false) }
    var likeCount1 by remember { mutableStateOf(50) }
    var likeCount2 by remember { mutableStateOf(50) }
    var likeCount3 by remember { mutableStateOf(50) }
    var likeCount4 by remember { mutableStateOf(50) }

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

            // 评论框背景(未标题-1 69 (3).png, 417×523dp)
            //   位置 (0, 393),尺寸 417×523dp
            //   图片宽 417 略大于画布 412,右侧溢出 5dp,按设计保留
            //   顶部 Y=393 略高于视频进度条 Y=399(进度条叠在新评论框之上,效果保留)
            //   高度 523 + Y 393 = 916,贴底(系统导航条之上)
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_comment1_bg),
                contentDescription = "评论框背景",
                modifier = Modifier
                    .offset(x = 0.dp, y = 393.dp)
                    .size(width = 417.dp, height = 523.dp),
                contentScale = ContentScale.Fit,
            )

            // ===== 顶部 X 关闭按钮(Group 141 (2).png @ (17, 418), 17×17dp) =====
            //   位于评论框背景图(Y=393 起)顶部偏上,Y=418 距评论框顶 25dp,水平居中布局
            //   触摸区域扩大到 44×44dp,符合 Material 最小点击规范
            //   填色 #6D8470,不透明度 100%
            //   点击 → 关闭评论1页,回到演武场视频首页(onBack)
            Box(
                modifier = Modifier
                    .offset(x = 17.dp - (44.dp - 17.dp) / 2, y = 418.dp - (44.dp - 17.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment1_group141),
                    contentDescription = "关闭",
                    modifier = Modifier.size(width = 17.dp, height = 17.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF6D8470), BlendMode.SrcIn),
                    alpha = 1f,
                )
            }

            // ===== 顶部 "放大缩小" 按钮(放大缩小 (2).png @ (54, 418), 17×17dp) =====
            //   位于 X 关闭按钮右侧 37dp,水平对齐同一行
            //   触摸区域扩大到 44×44dp,符合 Material 最小点击规范
            //   填色 #6D8470,不透明度 100%
            //   (源图本身属性:opacity 54%, position mixed, weight 1, end point mixed)
            //   点击 → 展开为全屏评论2页(onOpenExpanded)
            Box(
                modifier = Modifier
                    .offset(x = 54.dp - (44.dp - 17.dp) / 2, y = 418.dp - (44.dp - 17.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable { onOpenExpanded() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment1_zoom),
                    contentDescription = "放大缩小",
                    modifier = Modifier.size(width = 17.dp, height = 17.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF6D8470), BlendMode.SrcIn),
                    alpha = 1f,
                )
            }

            // ===== "500 条评论" 标题 @ (160, 443), 96×18dp, 14sp, #6D8470 =====
            //   位于 X 关闭 + 放大缩小 按钮右侧(同 Y=443 行,略低于图标中心 Y=418+8.5=426.5)
            //   letterSpacing 0.25em(原 41% 太宽会被裁字,改为 25% 保证 7 个字完整显示)
            //   与 Comment2 页同名标题风格完全一致
            Text(
                text  = "500 条评论",
                color = Color(0xFF6D8470),
                style = TextStyle(
                    fontFamily    = YaHei,
                    fontSize      = 14.sp,
                    letterSpacing = 0.25.em,
                ),
                modifier = Modifier
                    .offset(x = 160.dp, y = 443.dp)
                    .size(width = 96.dp, height = 18.dp),
            )

            // ===== 4 个评论项装饰(Group 179.png, 104×50dp)=====
            //   垂直等距 100dp 排列,X 统一为 20,均位于评论框背景图(Y=393~916)内
            //   Y 序列:493 / 593 / 693 / 793(最后一项底沿 793+50=843,距互评按钮 Y=834 重叠 9dp)
            //   原图为评论项的头部 / 内容区装饰条(头像+昵称 / 文本行等)
            //   使用私有 Composable CommentGroupItem 避免重复代码
            CommentGroupItem(y = 493.dp)
            CommentGroupItem(y = 593.dp)
            CommentGroupItem(y = 693.dp)
            CommentGroupItem(y = 793.dp)

            // ===== 4 条评论文本(313×36dp, 14sp, #000000)=====
            //   文本位于每个 Group 179 项内(Y=493/593/693/793):
            //   - 前 3 条:位置 81/79,61×9/61×7/61×7,H=50dp(完整显示所有行,无 maxLines 限制)
            //   - 第 4 条:位置 79, Y=817, H=20dp, maxLines=1 + Ellipsis
            //     (Y=817 + 20 = 837,被互评按钮 Y=834 遮挡 3dp,只保留顶部 1 行可见)
            //   使用私有 Composable CommentTextItem 避免重复代码
            CommentTextItem(
                x =  81.dp, y = 517.dp,
                text = "我最欣赏的，是机械冷硬的手掌之上，依旧愿意为一只蝴蝶停留的温柔。",
                height = 50.dp,
            )
            CommentTextItem(
                x =  79.dp, y = 617.dp,
                text = "我最欣赏的，是机械冷硬的手掌之上，依旧愿意为一只蝴蝶停留的温柔。",
                height = 50.dp,
            )
            CommentTextItem(
                x =  79.dp, y = 717.dp,
                text = "我最欣赏的，是机械冷硬的手掌之上，依旧愿意为一只蝴蝶停留的温柔。",
                height = 50.dp,
            )
            CommentTextItem(
                x =  79.dp, y = 817.dp,
                text = "我最欣赏的，是机械冷硬的手掌之上，依旧愿意为一只蝴蝶停留的温柔。",
                height = 20.dp,
                maxLines = 1,
            )

            // ===== 4 个评论点赞图标(17×16dp,可点击切换图标)=====
            //   位于每条评论文本右侧(贴近右沿 X=358/356)
            //   Y 序列:559 / 659 / 759 / 859(与文本行对齐)
            //   点击 → 切换 isLiked 状态,图标资源随之更换:
            //     - 未点赞:Like (喜欢) (1).png(img_yanwuchang_video_comment1_like)
            //     - 已点赞:Like (喜欢) (2).png(img_yanwuchang_video_comment1_like_filled)
            //   触摸区扩大到 44×44dp,符合 Material 最小点击规范
            //   数字 "50" 紧贴爱心右边 2dp:
            //     X = 爱心 X + 爱心宽 17 + 间距 2 = 爱心 X + 19
            //     Y = 爱心 Y(顶对齐,文字在 16dp 容器内自然垂直居中)
            //     字号 12sp,黑色 #000000,不透明度 100%
            // 项 1:爱心 X=358 / 数字 X=377
            //   点击 → isLiked1 翻转 + likeCount1 ±1
            Box(
                modifier = Modifier
                    .offset(x = 358.dp - (44.dp - 17.dp) / 2, y = 559.dp - (44.dp - 16.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable {
                        isLiked1 = !isLiked1
                        likeCount1 += if (isLiked1) 1 else -1
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (isLiked1) R.drawable.img_yanwuchang_video_comment1_like_filled
                        else           R.drawable.img_yanwuchang_video_comment1_like
                    ),
                    contentDescription = if (isLiked1) "取消点赞" else "点赞",
                    modifier = Modifier.size(width = 17.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
            }
            CommentLikeNumber(x = 377.dp, y = 559.dp, text = likeCount1.toString())
            // 项 2:爱心 X=356 / 数字 X=375
            //   点击 → isLiked2 翻转 + likeCount2 ±1
            Box(
                modifier = Modifier
                    .offset(x = 356.dp - (44.dp - 17.dp) / 2, y = 659.dp - (44.dp - 16.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable {
                        isLiked2 = !isLiked2
                        likeCount2 += if (isLiked2) 1 else -1
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (isLiked2) R.drawable.img_yanwuchang_video_comment1_like_filled
                        else           R.drawable.img_yanwuchang_video_comment1_like
                    ),
                    contentDescription = if (isLiked2) "取消点赞" else "点赞",
                    modifier = Modifier.size(width = 17.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
            }
            CommentLikeNumber(x = 375.dp, y = 659.dp, text = likeCount2.toString())
            // 项 3:爱心 X=356 / 数字 X=375
            //   点击 → isLiked3 翻转 + likeCount3 ±1
            Box(
                modifier = Modifier
                    .offset(x = 356.dp - (44.dp - 17.dp) / 2, y = 759.dp - (44.dp - 16.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable {
                        isLiked3 = !isLiked3
                        likeCount3 += if (isLiked3) 1 else -1
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment1_like),
                    contentDescription = if (isLiked3) "取消点赞" else "点赞",
                    modifier = Modifier.size(width = 17.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (isLiked3) ColorFilter.tint(Color(0xFFF97D7D), BlendMode.SrcIn) else null,
                    alpha = 1f,
                )
            }
            // 项 4:爱心 X=356 / 数字 X=375
            //   Y=859 超出了评论框背景图(Y=393~916)范围,位于互评按钮 Y=834 之下
            //   (互评按钮 H=47,Y=834~881,与项 4 爱心 Y=859~875 重叠 16dp)
            //   实际渲染时爱心可能与互评按钮装饰条交叠
            //   点击 → isLiked4 翻转 + likeCount4 ±1
            Box(
                modifier = Modifier
                    .offset(x = 356.dp - (44.dp - 17.dp) / 2, y = 859.dp - (44.dp - 16.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable {
                        isLiked4 = !isLiked4
                        likeCount4 += if (isLiked4) 1 else -1
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (isLiked4) R.drawable.img_yanwuchang_video_comment1_like_filled
                        else           R.drawable.img_yanwuchang_video_comment1_like
                    ),
                    contentDescription = if (isLiked4) "取消点赞" else "点赞",
                    modifier = Modifier.size(width = 17.dp, height = 16.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 1f,
                )
            }
            CommentLikeNumber(x = 375.dp, y = 859.dp, text = likeCount4.toString())

            // ===== 底部 "互评" 按钮(背景图 @ (22, 834) + 居中文字) =====
            //   背景:未标题-2-恢复的 12 (1).png(img_yanwuchang_video_comment1_reply, 372×47)
            //     位置 (22, 834),尺寸 372×47dp(源图属性:opacity 88%)
            //   文字:"互评" 居中叠加在背景图上
            //     位置 (188, 844),40×26dp 区域,20sp,YaHei,白色 #FFFFFF,不透明度 100%
            //     (X=188 = 22 + (372-40)/2 = 22 + 166 = 188,水平居中于背景)
            //     (Y=844 = 834 + (47-26)/2 = 834 + 10.5 ≈ 844,垂直居中于背景)
            //   整体可点击,目前为占位(TODO:接入互评功能)
            Box(
                modifier = Modifier
                    .offset(x = 22.dp, y = 834.dp)
                    .size(width = 372.dp, height = 47.dp)
                    .clickable { /* TODO: 互评功能 */ },
                contentAlignment = Alignment.Center,
            ) {
                // 底层:装饰横幅背景(372×47dp,贴原图原生比例)
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment1_reply),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                // 顶层:"互评" 文字(白色 #FFFFFF,20sp,YaHei,不透明度 100%)
                //   居中显示在背景图正中(由外层 Box 的 Alignment.Center 处理)
                Box(
                    modifier = Modifier.size(width = 40.dp, height = 26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "互评",
                        color = Color(0xFFFFFFFF),
                        style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * 单条评论点赞数字(50)
 *
 *  位于评论1页每个 Like (喜欢) 爱心右边 2dp,15×16dp 容器,12sp 字号
 *  YaHei 字体,黑色 #000000,不透明度 100%
 *  使用 Compose Text 而非 PNG:数字变化时(后续可能绑定状态)零缩放
 *  默认显示静态 "50",未来可改 [text] 参数支持动态数字
 *
 * @param x 元素左上角 X 坐标(dp)
 * @param y 元素左上角 Y 坐标(dp)
 */
@Composable
private fun CommentLikeNumber(
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    text: String = "50",
) {
    Text(
        text  = text,
        color = Color(0xFF000000),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize   = 12.sp,
        ),
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = 15.dp, height = 16.dp),
    )
}

/**
 * 单条评论文本项
 *
 *  位于评论1页的评论框背景图内,313×可变dp 容器,14sp 字号,黑色 #000000
 *  使用 private 默认 alpha=1f(不透明度 100%),无 ColorFilter(直接使用原黑色)
 *  与 CommentGroupItem(Group 179.png)叠加显示,文本位于 Group 179 项内
 *
 * @param x        元素左上角 X 坐标(dp)
 * @param y        元素左上角 Y 坐标(dp)
 * @param text     评论文本内容
 * @param height   容器高度(dp),默认 36dp
 * @param maxLines 最大行数(Int.MAX_VALUE 表示不限制),默认不限制
 * @param overflow 超出 maxLines 后的裁剪策略,默认 Clip
 */
@Composable
private fun CommentTextItem(
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    text: String,
    height: androidx.compose.ui.unit.Dp = 36.dp,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
) {
    Text(
        text  = text,
        color = Color(0xFF000000),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize   = 14.sp,
        ),
        maxLines = maxLines,
        overflow = overflow,
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = 313.dp, height = height),
    )
}

/**
 * 单条评论项装饰(Group 179.png)
 *
 *  位于评论1页的评论框背景图内,X 固定 20,Y 由调用方传入,尺寸 104×50dp
 *  使用项目内 drawable/group_179_v2.png(原 PNG Group 179.png,大小 1.4KB)
 *  未来可在此添加 onClick / 头像 / 文本 / 点赞等子组件
 *
 * @param y 元素左上角的 Y 坐标(dp)
 */
@Composable
private fun CommentGroupItem(y: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(R.drawable.group_179_v2),
        contentDescription = "评论项",
        modifier = Modifier
            .offset(x = 20.dp, y = y)
            .size(width = 104.dp, height = 50.dp),
        contentScale = ContentScale.Fit,
    )
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