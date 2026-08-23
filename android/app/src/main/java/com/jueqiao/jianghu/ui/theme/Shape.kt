package com.jueqiao.jianghu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material3 Shapes mapped to design radii.
 * - extraSmall = 4dp
 * - small      = 5dp  (Layout.radiusField)
 * - medium     = 10dp (Layout.radiusButton)
 * - extraLarge = 25dp (Layout.radiusCard)
 */
val JianghuShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(5.dp),
    medium     = RoundedCornerShape(10.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(25.dp),
)