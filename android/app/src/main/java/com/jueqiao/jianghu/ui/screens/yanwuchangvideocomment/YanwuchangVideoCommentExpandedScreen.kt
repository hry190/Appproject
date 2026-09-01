package com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 演武场视频评论 — 全屏展开页
 *
 * 点击评论页(小评论框)的"放大缩小"图标进入。视觉效果:小评论框向上扩展成全屏。
 *
 * 布局(Figma 设计稿 412×917 基准,参考 演武场视频评论.png):
 *   - 全屏背景:未标题-1 69.png(img_yanwuchang_video_comment_expanded_bg, 412×917)
 *   - 顶部 header (Y=66):
 *       左 X:Group 141.png @ (20, 66), 17×17dp — 点击切换到演武场视频首页
 *       左 箭头:Group 141.png @ (54, 66), 17×17dp — 点击评论框向下缩小,切回小评论框页
 *       中:"500 条评论" @ (161, 65), 96×17dp, 14px, letterSpacing 41%, #6D8470, 100%
 *   - 评论列表 (Y=110 起):可上下滑动
 *       每条评论:
 *         - 左侧:圆形头像(38dp,填充 #6D8470)
 *         - 中部:用户名(呀呀呀,13sp #6D8470) + 评论文本(14sp #333333)
 *         - 右侧:爱心(ic_like_outline/ic_like_filled)+ 数字(12sp #6D8470 / #999999)
 *         - 整行间距 24dp
 *   - 底部:Group 166.png 按钮(可点击,380×64dp 原图),代替原"互评"绿色按钮
 *
 * @param onBackToHome     X 点击 — 跳到演武场视频首页(直接 popBackStack 到 YanwuchangVideo)
 * @param onBackToComment  返回箭头点击 — 动画回到小评论框页(slide down 出场)
 */
@Composable
fun YanwuchangVideoCommentExpandedScreen(
    onBackToHome: () -> Unit = {},
    onBackToComment: () -> Unit = {},
) {
    // 示例评论(参考 演武场视频评论.png 的样式)
    val sampleText = "我最欣赏的，是机械冷硬的手掌之上，依旧愿意为一只蝴蝶停留的温柔。"
    val initialComments = remember {
        (1..8).map { i ->
            ExpandedCommentItem(
                id = i,
                username = "呀呀呀",
                text = sampleText,
                likeCount = 50,
            )
        }
    }
    var comments by remember { mutableStateOf(initialComments) }

    // 系统返回键拦截 — 默认走"评论框向下缩小"路径
    BackHandler(enabled = true) {
        onBackToComment()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景(未标题-1 69.png, 412×917,圆角 5dp,ContentScale.Crop)
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_comment_expanded_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ===== 顶部 X 关闭按钮(Group 141.png @ (20, 66), 17×17dp) =====
            //   点击 → onBackToHome → 跳到演武场视频首页
            //   触摸区域扩大到 44×44dp,符合 Material 最小点击规范
            Box(
                modifier = Modifier
                    .offset(x = 20.dp - (44.dp - 17.dp) / 2, y = 66.dp - (44.dp - 17.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable { onBackToHome() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment_group141),
                    contentDescription = "关闭",
                    modifier = Modifier.size(width = 17.dp, height = 17.dp),
                )
            }

            // ===== 顶部 返回箭头(放大缩小.png @ (54, 66), 17×17dp) =====
            //   点击 → onBackToComment → 评论框向下缩小回到小评论框页
            Box(
                modifier = Modifier
                    .offset(x = 54.dp - (44.dp - 17.dp) / 2, y = 66.dp - (44.dp - 17.dp) / 2)
                    .size(width = 44.dp, height = 44.dp)
                    .clickable { onBackToComment() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment_zoom),
                    contentDescription = "返回",
                    modifier = Modifier.size(width = 17.dp, height = 17.dp),
                )
            }

            // ===== "500 条评论" 标题 @ (161, 65), 96×17dp, 14sp, #6D8470 =====
            //   原 letterSpacing 0.41em(41%)会让 7 个字超出 96dp 宽度被裁掉"论"
            //   改为 0.25em(25%),既保持字距又保证所有字完整显示
            Text(
                text  = "500 条评论",
                color = Color(0xFF6D8470),
                style = TextStyle(
                    fontFamily   = YaHei,
                    fontSize     = 14.sp,
                    letterSpacing = 0.25.em,           // 25%(原 41% 太宽被裁)
                ),
                modifier = Modifier
                    .offset(x = 161.dp, y = 65.dp)
                    .size(width = 96.dp, height = 17.dp),
            )

            // ===== 评论列表 (Y=110 起,左右各 16dp 内边距) =====
            LazyColumn(
                modifier = Modifier
                    .offset(x = 0.dp, y = 110.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(comments, key = { it.id }) { comment ->
                    ExpandedCommentRow(
                        comment = comment,
                        onLikeToggle = {
                            comments = comments.map { c ->
                                if (c.id == comment.id) {
                                    c.copy(
                                        isLiked   = !c.isLiked,
                                        likeCount = c.likeCount + if (!c.isLiked) 1 else -1,
                                    )
                                } else c
                            }
                        },
                    )
                }
            }

            // ===== 底部 Group 166.png 按钮(替代原"互评"绿色按钮)=====
            //   原图 380×64dp,居中显示,整图可点击
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .clickable { /* TODO: 互评功能 */ },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment_group166),
                    contentDescription = "互评",
                    // 按原图宽高 380×64 等比缩放到屏幕内(留 16dp 边距)
                    modifier = Modifier.size(width = 380.dp, height = 64.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/**
 * 单条评论行 — 左侧圆形头像 + 中部文本 + 右侧爱心 + 数字
 */
@Composable
private fun ExpandedCommentRow(
    comment: ExpandedCommentItem,
    onLikeToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        // 左侧:圆形头像(38dp, #6D8470 灰绿色,符合 Figma 设计)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF6D8470)),
        )

        // 中部:用户名 + 评论文本(占满中间空间)
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text  = comment.username,
                color = Color(0xFF6D8470),
                style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
            )
            Text(
                text  = comment.text,
                color = Color(0xFF333333),
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // 右侧:Vector.png 点赞图标(13×11dp)
            //   - 未点赞:Vector.png 原色(无 ColorFilter)
            //   - 已点赞:ColorFilter.tint + BlendMode.SrcIn 把图标**已有像素**染成 #6D8470
            //     填充严格限制在图标自身轮廓内,不超出边线
            //   点击翻转 isLiked 同步数字 ±1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_yanwuchang_video_comment_like_vector),
                    contentDescription = if (comment.isLiked) "取消点赞" else "点赞",
                    modifier = Modifier
                        .size(width = 13.dp, height = 11.dp)
                        .clickable(onClick = onLikeToggle),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (comment.isLiked) {
                        // 已点赞:仅染色图标自身的描边像素,不会扩展到矩形外
                        ColorFilter.tint(Color(0xFF6D8470), BlendMode.SrcIn)
                    } else {
                        // 未点赞:原图无 tint
                        null
                    },
                    alpha = 1f,
                )
                Text(
                    text  = comment.likeCount.toString(),
                    color = if (comment.isLiked) Color(0xFF6D8470) else Color(0xFF999999),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
    }
}

/** 单条评论数据 */
private data class ExpandedCommentItem(
    val id: Int,
    val username: String,
    val text: String,
    val likeCount: Int,
    val isLiked: Boolean = false,
)