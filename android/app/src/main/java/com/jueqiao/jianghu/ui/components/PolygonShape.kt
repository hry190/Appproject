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

/**
 * 宽顶/宽底的六边形 — 上下顶角角度更大(更钝),侧边更靠顶/底。
 *
 * 用于滚轮8 的"介绍"区域:把尖顶六边形改成几乎平顶/平底,只在最上/最下留一个小尖。
 *
 * 顶点(以 size 为单位):
 *   - (0.5, 0)        顶部尖
 *   - (1, 0.05)       右上(非常靠近顶部)
 *   - (1, 0.95)       右下(非常靠近底部)
 *   - (0.5, 1)        底部尖
 *   - (0, 0.95)       左下
 *   - (0, 0.05)       左上
 *
 * 顶角(内角)≈ 168°,比 HexagonShape 的 ≈127° 大约多 40°。
 */
class WideHexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height * 0.05f)
            lineTo(size.width, size.height * 0.95f)
            lineTo(size.width * 0.5f, size.height)
            lineTo(0f, size.height * 0.95f)
            lineTo(0f, size.height * 0.05f)
            close()
        }
        return Outline.Generic(path)
    }
}
