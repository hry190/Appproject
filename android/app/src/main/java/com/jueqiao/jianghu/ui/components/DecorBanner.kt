package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Vertical-text decorative banner used on home page.
 * Mirrors RN DecorBanner (40x90 image + 3 vertical chars).
 */
@Composable
fun DecorBanner(
    imageRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFF4E6CF),
    onClick: (() -> Unit)? = null,
    width: Dp = 40.dp,
    height: Dp = 90.dp,
) {
    val mod = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Box(modifier = mod) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = text,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Render each character as its own Text rotated vertically.
            // We use a Column with single chars and small spacing.
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                text.forEach { ch ->
                    Text(
                        text = ch.toString(),
                        color = textColor,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xCC141E14),
                                blurRadius = 8f,
                            ),
                        ),
                    )
                }
            }
        }
    }
}