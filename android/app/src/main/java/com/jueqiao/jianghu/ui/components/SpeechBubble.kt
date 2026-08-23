package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Speech bubble used by home chat with 阿砚.
 * - Rounded rectangle body in cream
 * - Triangle tail at bottom-left
 *
 * Tail size scales with the bubble height.
 */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    bubbleColor: Color = Color(0xFFFFFFFF),
    cornerRadius: Dp = 12.dp,
    tailSize: Dp = 10.dp,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = tailSize)
                .clip(RoundedCornerShape(cornerRadius))
                .background(bubbleColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
        }
        // Tail triangle at bottom-left
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp),
        ) {
            val w = tailSize.toPx()
            val h = tailSize.toPx()
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(0f, h)
                close()
            }
            drawPath(path, color = bubbleColor, style = Fill)
        }
    }
}