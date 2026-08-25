package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.theme.YaHei

// 卡片配色(米色卡 + 错题 bar)
private val TextBlack     = Color(0xFF000000)
private val SectionCream  = Color(0xFFEBDCC3) // 秘籍/作品 section 底色
private val ErrorBarColor = Color(0xFF8E9A75) // 错题 section 底色
private val ErrorBarAlpha = 0.66f

// 主卡片尺寸(相对设计稿的固定宽高)
private val CardWidth  = 372.dp
private val CardHeight = 1000.dp
private val CardCorner = 14.dp
private val CardTopPad = 70.dp

// 集中文字样式:都是黑色 + YaHei,因为这是米色纸卡上的字
private val TitleStyle = TextStyle(
    fontFamily    = YaHei,
    fontSize      = 24.sp,
    letterSpacing = 24.sp, // 拉开"行"和"囊"之间的视觉距离
)
private val HeaderStyle    = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextBlack)
private val NameStyle      = TextStyle(fontFamily = YaHei, fontSize = 16.sp, color = TextBlack)
private val CaptionStyle   = TextStyle(fontFamily = YaHei, fontSize = 12.sp, color = TextBlack)
private val StatStyle      = TextStyle(fontFamily = YaHei, fontSize = 14.sp, color = TextBlack)
private val BookTitleStyle = TextStyle(fontFamily = YaHei, fontSize = 10.sp, color = TextBlack)

/**
 * 行囊页 — 完整页面化的"我的行囊"。
 *
 * 整体结构:
 *   竹林背景(全屏)
 *   ├─ 顶部右侧 4 个快捷图标(作品 / 进度 / 任务 / 设置)
 *   ├─ 中央米色卡(Top 70dp,宽 372,高 1000,圆角 14)
 *   │   ├─ 关闭按钮 (左上 ✕)
 *   │   ├─ 标题 "行 囊"
 *   │   ├─ 用户信息(头像 / 姓名 / 班级 / ID / 见习弟子徽章 / 勋章)
 *   │   ├─ 统计行(签到 / 累计修行 / 已通关试炼)
 *   │   ├─ 获得的秘籍(可折叠)── 3 本书
 *   │   ├─ 我的错题(可折叠)── 只露标题 bar
 *   │   └─ 我的作品(可折叠)── 2 张作品缩略图
 *   └─ 右下角大熊猫角色(叠在卡片之上)
 *
 * 点击"修炼/造物/武林大会"等快捷入口由 NavHost 注入。
 */
@Composable
fun LuggageScreen(
    onBack: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onOpenDahui: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
) {
    var booksOpen  by remember { mutableStateOf(true) }
    var errorsOpen by remember { mutableStateOf(false) }
    var worksOpen  by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 1. 竹林背景
        Image(
            painter            = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop,
        )

        // 2. 顶部右侧 4 个快捷图标
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(iconRes = R.drawable.img_icon_works,    label = "作品", onClick = { /* TODO: 跳转作品页 */ })
            QuickActionItem(iconRes = R.drawable.img_icon_progress, label = "进度", onClick = onOpenProgress)
            QuickActionItem(iconRes = R.drawable.img_icon_task,     label = "任务", onClick = { /* TODO: 跳转任务页 */ })
            QuickActionItem(iconRes = R.drawable.img_icon_settings, label = "设置", onClick = { /* TODO: 跳转设置页 */ })
        }

        // 3. 主卡片(米色纸)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = CardTopPad)
                .size(width = CardWidth, height = CardHeight)
                .clip(RoundedCornerShape(CardCorner)),
        ) {
            // 3.1 卡片底图
            Image(
                painter            = painterResource(R.drawable.img_luggage_card_bg),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop,
            )

            // 3.2 关闭按钮(左上 ✕)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 15.dp, y = 58.dp)
                    .size(15.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter            = painterResource(R.drawable.ic_close),
                    contentDescription = "关闭",
                    modifier           = Modifier.size(15.dp),
                )
            }

            // 3.3 标题 "行 囊"
            Text(
                text     = "行 囊",
                style    = TitleStyle,
                color    = TextBlack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 150.dp, y = 56.dp),
            )

            // 3.4 用户信息区
            UserInfo(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 80.dp),
            )

            // 3.5 统计行
            Text(
                text     = "签到：3天       累计修行：7天       已通关试炼:5个",
                style    = StatStyle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 165.dp),
            )

            // 3.6 获得的秘籍(可折叠)
            ExpandableSection(
                title        = "获得的秘籍：",
                expanded     = booksOpen,
                onToggle     = { booksOpen = !booksOpen },
                background   = SectionCream,
                cornerRadius = 6.dp,
                modifier     = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 203.dp)
                    .size(width = 332.dp, height = 250.dp),
            ) {
                Column(modifier = Modifier.offset(y = 26.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier              = Modifier.padding(horizontal = 7.dp),
                    ) {
                        BookItem(R.drawable.img_book_1, "《拆招心法》")
                        BookItem(R.drawable.img_book_2, "《识机真诀》")
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    BookItem(R.drawable.img_book_3, "《百炼识物诀》")
                }
            }

            // 3.7 我的错题(只露标题 bar)
            SectionBar(
                title        = "我的错题：",
                expanded     = errorsOpen,
                onToggle     = { errorsOpen = !errorsOpen },
                background   = ErrorBarColor.copy(alpha = ErrorBarAlpha),
                cornerRadius = 8.dp,
                modifier     = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 463.dp)
                    .size(width = 332.dp, height = 35.dp),
            )

            // 3.8 我的作品(可折叠)
            ExpandableSection(
                title        = "我的作品：",
                expanded     = worksOpen,
                onToggle     = { worksOpen = !worksOpen },
                background   = SectionCream,
                cornerRadius = 6.dp,
                modifier     = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 508.dp)
                    .size(width = 332.dp, height = 145.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    modifier              = Modifier
                        .offset(y = 26.dp)
                        .padding(horizontal = 3.dp),
                ) {
                    WorkItem(R.drawable.img_work_1, w = 72.dp, h = 95.dp)
                    WorkItem(R.drawable.img_work_2, w = 86.dp, h = 92.dp)
                }
            }

            // 3.9 大熊猫角色(右下,叠在卡片之上)
            Image(
                painter            = painterResource(R.drawable.img_luggage_panda),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 285.dp, y = 472.dp)
                    .size(width = 112.dp, height = 233.dp),
                contentScale       = ContentScale.Fit,
            )
        }
    }
}

/** 头像 + 姓名/班级/ID + 见习弟子徽章 + 勋章 */
@Composable
private fun UserInfo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 332.dp, height = 90.dp)) {
        // 头像圆框
        Image(
            painter            = painterResource(R.drawable.img_avatar_ring),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.TopStart)
                .size(width = 76.dp, height = 72.dp),
            contentScale       = ContentScale.Fit,
        )
        // 头像(熊猫脸,叠在圆框中央)
        Image(
            painter            = painterResource(R.drawable.img_avatar),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.TopStart)
                .offset(x = 9.dp, y = 6.dp)
                .size(width = 60.dp, height = 60.dp),
            contentScale       = ContentScale.Crop,
        )
        // 姓名
        Text(
            text     = "哈哈哈",
            style    = NameStyle,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 86.dp, y = 6.dp),
        )
        // 班级(姓名右侧)
        Text(
            text     = "五（三）班",
            style    = CaptionStyle,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 154.dp, y = 9.dp),
        )
        // ID
        Text(
            text     = "ID：1326528988",
            style    = CaptionStyle,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 86.dp, y = 31.dp),
        )
        // 见习弟子 徽章(图 + 文字)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 78.dp, y = 56.dp)
                .size(width = 83.dp, height = 21.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter            = painterResource(R.drawable.img_level_badge_bg),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.FillBounds,
            )
            Text(
                text  = "见习弟子",
                style = CaptionStyle,
            )
        }
        // 勋章 + 圆点
        Text(
            text     = "勋章：",
            style    = CaptionStyle,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 172.dp, y = 56.dp),
        )
        Image(
            painter            = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "已获得勋章",
            modifier           = Modifier
                .align(Alignment.TopStart)
                .offset(x = 204.dp, y = 55.dp)
                .size(20.dp),
        )
    }
}

/** 折叠区:可带内容的卡片版本(秘籍/作品用) */
@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    background: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(background)
            .clickable(onClick = onToggle),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = title, style = HeaderStyle)
                Chevron(expanded = expanded)
            }
            if (expanded) content()
        }
    }
}

/** 折叠区:只有标题 bar 的版本(错题用) */
@Composable
private fun SectionBar(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    background: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(background)
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = HeaderStyle)
            Chevron(expanded = expanded)
        }
    }
}

/** chevron-down 图标,根据 expanded 状态旋转 0°/180° */
@Composable
private fun Chevron(expanded: Boolean) {
    Image(
        painter            = painterResource(R.drawable.ic_chevron_down),
        contentDescription = if (expanded) "收起" else "展开",
        modifier           = Modifier
            .size(16.dp)
            .rotate(if (expanded) 180f else 0f),
    )
}

/** 单本秘籍:小图 + 标题 */
@Composable
private fun BookItem(imageRes: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter            = painterResource(imageRes),
            contentDescription = name,
            modifier           = Modifier.size(width = 47.dp, height = 42.dp),
            contentScale       = ContentScale.Crop,
        )
        Text(text = name, style = BookTitleStyle)
    }
}

/** 单个作品缩略图 */
@Composable
private fun WorkItem(imageRes: Int, w: Dp, h: Dp) {
    Image(
        painter            = painterResource(imageRes),
        contentDescription = null,
        modifier           = Modifier.size(width = w, height = h),
        contentScale       = ContentScale.Crop,
    )
}
