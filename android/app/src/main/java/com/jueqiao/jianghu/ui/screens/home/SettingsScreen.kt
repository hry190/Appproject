package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 设置页 — Figma node-id 413:3244。
 * 竹林背景 + 米色圆角卡片 + 13 个菜单项 + 底部两个药丸按钮（切换账号/退出登录）。
 * —— 方案 A 改动 (设置页-方案A-实验 分支) ——
 *   1. 顶部 ✕ 按钮:加大尺寸 + 加绿色圆形背景,Figma里是圆形按钮
 *   2. 标题"设 置"增加水平 padding 让居中更准
 *   3. 关闭按钮可点击区域扩大
 *   4. 卡片顶部 padding 微调
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    // 顶部 4 个快捷键
    onOpenWorks: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    onOpenTask: () -> Unit = {},
    // 菜单项
    onOpenAccount: () -> Unit = {},
    onOpenMessage: () -> Unit = {},
    onOpenGeneral: () -> Unit = {},
    onOpenSound: () -> Unit = {},
    onOpenBlacklist: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenAgreement: () -> Unit = {},
    onOpenCollection: () -> Unit = {},
    onOpenSharing: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenDataRecovery: () -> Unit = {},
    // 底部按钮
    onSwitchAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 竹林背景
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 顶部右侧 4 个快捷图标
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 71.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(iconRes = R.drawable.img_icon_works,    label = "作品", onClick = onOpenWorks)
            QuickActionItem(iconRes = R.drawable.img_icon_progress, label = "进度", onClick = onOpenProgress)
            QuickActionItem(iconRes = R.drawable.img_icon_task,     label = "任务", onClick = onOpenTask)
            QuickActionItem(iconRes = R.drawable.img_icon_settings, label = "设置", onClick = { /* 当前页 */ })
        }

        // 主卡片（米色半透明,顶部圆角,内容可滚动）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 118.dp)
                .size(width = 372.dp, height = 998.dp)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5E8D4).copy(alpha = 0.92f)),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // 顶部:关闭按钮 + 标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 关闭按钮(方案 A:加大尺寸 + 浅绿色圆形背景)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFAACC99).copy(alpha = 0.3f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "关闭",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "设 置",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 10.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontSize = 24.sp,
                            letterSpacing = 12.sp,
                        ),
                    )
                    // 占位与关闭按钮等宽(保证标题居中)
                    Spacer(modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 第一组:账号 / 消息 / 通用 / 声音
                SettingsMenuItem("账号管理", onClick = onOpenAccount)
                SettingsMenuItem("消息设置", onClick = onOpenMessage)
                SettingsMenuItem("通用设置", onClick = onOpenGeneral)
                SettingsMenuItem("声音调节", onClick = onOpenSound)

                // 分组标题
                Text(
                    text = "隐私",
                    color = Color(0xFF9A9882),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                    ),
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 5.dp),
                )

                // 第二组
                SettingsMenuItem("黑名单管理",         onClick = onOpenBlacklist)
                SettingsMenuItem("隐私政策",           onClick = onOpenPrivacy)
                SettingsMenuItem("用户协议",           onClick = onOpenAgreement)
                SettingsMenuItem("个人信息收集清单",   onClick = onOpenCollection)
                SettingsMenuItem("第三方信息共享清单", onClick = onOpenSharing)
                SettingsMenuItem("帮助中心",           onClick = onOpenHelp)
                SettingsMenuItem("关于", subtitle = "2.0", onClick = onOpenAbout)
                SettingsMenuItem("数据恢复",           onClick = onOpenDataRecovery)

                Spacer(modifier = Modifier.height(20.dp))

                // 底部两个药丸按钮
                PillButton("切换账号", onClick = onSwitchAccount)
                Spacer(modifier = Modifier.height(10.dp))
                PillButton("退出登录", onClick = onLogout, isDestructive = true)

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 16.sp,
                ),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFF9A9882),
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Image(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
            thickness = 0.5.dp,
            color = Color(0xFFD8D2C2),
        )
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 31.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF5A7A5A), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isDestructive) Color(0xFFD93F3F) else Color.Black,
            style = TextStyle(
                fontFamily = YaHei,
                fontSize = 20.sp,
                letterSpacing = 12.sp,
            ),
        )
    }
}
