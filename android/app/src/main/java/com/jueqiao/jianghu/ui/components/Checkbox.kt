package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.AuthPalette

/**
 * Checkbox — empty olive ring or filled with check.
 * Replicates the SVG checkbox used by agreement rows in login/register/forgot.
 */
@Composable
fun AuthCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.Transparent, CircleShape)
            .border(
                width = 1.dp,
                color = AuthPalette.LinkOlive,
                shape = CircleShape,
            )
            .semantics {
                contentDescription = "同意用户协议与隐私条款"
                stateDescription = if (checked) "已同意" else "未同意"
            }
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(size - 4.dp).graphicsLayer(scaleX = -1f),
            )
        }
    }
}
