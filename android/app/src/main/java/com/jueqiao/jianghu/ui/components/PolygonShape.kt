package com.jueqiao.jianghu.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * 垂直拉伸的六边形(pointy top + pointy bottom,vertical stretched)
 *
 * 顶点(以 size 为单位):
 *   - (0.5, 0)   顶部尖
 *   - (1, 0.25)  右上
 *   - (1, 0.75)  右下
 *   - (0.5, 1)   底部尖
 *   - (0, 0.75)  左下
 *   - (0, 0.25)  左上
 *
 * 用法:`Modifier.clip(HexagonShape())`,把 Box 内容裁剪成六边形。
 */
class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height * 0.25f)
            lineTo(size.width, size.height * 0.75f)
            lineTo(size.width * 0.5f, size.height)
            lineTo(0f, size.height * 0.75f)
            lineTo(0f, size.height * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}
