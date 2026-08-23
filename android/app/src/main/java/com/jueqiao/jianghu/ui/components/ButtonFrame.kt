package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Primary gradient button — replicates SVG_BTN from authSvgs.ts.
 *
 * Two-layer rounded rect: outer olive frame + inner green gradient.
 * Pressed state reduces alpha; disabled state halves alpha.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 50.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val alpha = when {
        !enabled -> 0.5f
        pressed  -> 0.85f
        else     -> 1f
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFAACC99), Color(0xFF546942)),
                ),
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF527F50), Color(0xFF92B57A)),
                ),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        )
    }
}

/**
 * Cream button used on register page (#F7ECDA with dark text).
 */
@Composable
fun CreamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 50.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val alpha = when {
        !enabled -> 0.5f
        pressed  -> 0.92f
        else     -> 1f
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .clip(RoundedCornerShape(10.dp))
            .background(AuthPalette.CreamBtn)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = AuthPalette.TextDark,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        )
    }
}