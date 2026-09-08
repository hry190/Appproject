package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Hosts a fixed-size design canvas without cropping it on narrow or short devices.
 *
 * The imported Yanwuchang screens were authored against a 412 x 917 dp design.
 * This wrapper keeps their coordinates intact while uniformly shrinking the
 * foreground when the available window is smaller. Large windows keep the
 * original scale and center the design instead of stretching raster assets.
 */
@Composable
fun ResponsiveDesignCanvas(
    modifier: Modifier = Modifier,
    designWidth: Dp = 412.dp,
    designHeight: Dp = 917.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = calculateDesignCanvasScale(
            availableWidthDp = maxWidth.value,
            availableHeightDp = maxHeight.value,
            designWidthDp = designWidth.value,
            designHeightDp = designHeight.value,
        )
        val scaledWidth = designWidth * scale
        val scaledHeight = designHeight * scale

        Box(
            modifier = Modifier
                .size(width = scaledWidth, height = scaledHeight)
                .align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(width = designWidth, height = designHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
                content = content,
            )
        }
    }
}

internal fun calculateDesignCanvasScale(
    availableWidthDp: Float,
    availableHeightDp: Float,
    designWidthDp: Float = 412f,
    designHeightDp: Float = 917f,
): Float {
    if (
        availableWidthDp <= 0f ||
        availableHeightDp <= 0f ||
        designWidthDp <= 0f ||
        designHeightDp <= 0f
    ) {
        return 0f
    }
    return min(
        availableWidthDp / designWidthDp,
        availableHeightDp / designHeightDp,
    ).coerceAtMost(1f)
}
