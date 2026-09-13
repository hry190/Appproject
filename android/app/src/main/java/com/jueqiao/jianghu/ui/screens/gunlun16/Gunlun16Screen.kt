package com.jueqiao.jianghu.ui.screens.gunlun16

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold

/**
 * 滚轮16 页 — Vol-7-12 标题点击跳转目标(第八卷入口卷)。
 *
 * 当前为最小可运行 stub — 仅 StandardGunlunScaffold + 文本占位
 * — 待第八卷首屏(Vol-8-1)创建后可扩展为与 Gunlun15 同款的10 本书 + 介绍布局
 *
 * 资源(暂无,待补):
 *   - 10 本书 + 介绍(主题待定)
 *
 * 相关:
 *   - Vol-7-12 标题点击 → Gunlun16(本屏,2026-09-13 新增闭环,仿 Vol-5-15 → Gunlun12 模式)
 */
@Composable
fun Gunlun16Screen(
    onBack: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
    ) {
        // 占位文本 — 标准滚轮 10 本书 + 介绍 布局待 Vol-8 续建时补
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "滚轮16(第八卷入口,占位)",
                color = Color.Gray,
                fontSize = 14.sp,
            )
        }
    }
}