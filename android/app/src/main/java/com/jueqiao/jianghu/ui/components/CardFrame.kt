package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Card frame used by login & forgot — replicates buildCardFrameSvg(H).
 * Cream fill #F2E6D1 with dark-yellow stroke #E7DCAE (double border).
 */
@Composable
fun CardFrame(
    modifier: Modifier = Modifier,
    height: Dp,
    cornerRadius: Dp = 22.dp,
    strokeWidth: Dp = 3.dp,
    fillColor: Color = Color(0xFFF2E6D1),
    strokeColor: Color = Color(0xFFE7DCAE),
) {
    Canvas(modifier = modifier.height(height)) {
        val radiusPx = cornerRadius.toPx()
        val strokePx = strokeWidth.toPx()
        val outerRect = RoundRect(
            left = strokePx / 2,
            top = strokePx / 2,
            right = size.width - strokePx / 2,
            bottom = size.height - strokePx / 2,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
        )
        val outerPath = Path().apply { addRoundRect(outerRect) }
        drawPath(outerPath, color = fillColor)
        drawPath(outerPath, color = strokeColor, style = Stroke(width = strokePx))

        // Inner stroke (slightly inset)
        val inset = 6.dp.toPx()
        val innerRect = RoundRect(
            left = inset,
            top = inset,
            right = size.width - inset,
            bottom = size.height - inset,
            cornerRadius = CornerRadius(radiusPx - inset / 2, radiusPx - inset / 2),
        )
        val innerPath = Path().apply { addRoundRect(innerRect) }
        drawPath(innerPath, color = strokeColor, style = Stroke(width = 1.5.dp.toPx()))
    }
}