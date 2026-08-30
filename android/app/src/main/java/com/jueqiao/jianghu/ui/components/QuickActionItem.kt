package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Top-right quick-action button (settings/task/progress/works).
 * Used by HomeScreen. Mirrors RN QuickActionItem at iconX positions.
 */
@Composable
fun QuickActionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    showDot: Boolean = false,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(iconSize)) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(iconSize),
            )
            if (showDot) {
                Image(
                    painter = painterResource(com.jueqiao.jianghu.R.drawable.ic_dot_red),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = AuthPalette.TextDark,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
            ),
        )
    }
}