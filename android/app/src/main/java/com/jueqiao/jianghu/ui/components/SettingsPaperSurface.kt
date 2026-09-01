package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R

/** Shared antique-paper sheet with an adaptive, hand-torn silhouette. */
@Composable
fun SettingsPaperSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(2.dp, DeckledPaperShape, clip = false)
            .clip(DeckledPaperShape)
            .border(
                width = 0.6.dp,
                color = Color(0xFF8B7048).copy(alpha = 0.28f),
                shape = DeckledPaperShape,
            ),
    ) {
        Image(
            painter = painterResource(R.drawable.img_settings_paper_panel),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        content()
    }
}

/**
 * A deterministic deckled-paper edge. Edge depth is based on the short side so
 * the torn contour stays subtle on both the tall main sheet and shorter panels.
 */
private object DeckledPaperShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height
        val edge = size.minDimension * 0.018f

        fun x(fraction: Float) = width * fraction

        val path = Path().apply {
            // Top edge: two shallow missing-paper notches and an uneven lip.
            moveTo(edge * 2.8f, edge * 0.78f)
            lineTo(x(0.09f), edge * 0.62f)
            lineTo(x(0.14f), edge * 0.95f)
            lineTo(x(0.19f), edge * 0.66f)
            lineTo(x(0.235f), edge * 0.9f)
            lineTo(x(0.255f), edge * 1.7f)
            lineTo(x(0.276f), edge * 0.72f)
            lineTo(x(0.34f), edge * 0.9f)
            lineTo(x(0.405f), edge * 0.62f)
            lineTo(x(0.47f), edge * 0.92f)
            lineTo(x(0.535f), edge * 0.68f)
            lineTo(x(0.60f), edge * 0.96f)
            lineTo(x(0.665f), edge * 0.66f)
            lineTo(x(0.705f), edge * 0.86f)
            lineTo(x(0.725f), edge * 1.62f)
            lineTo(x(0.748f), edge * 0.72f)
            lineTo(x(0.82f), edge * 0.92f)
            lineTo(x(0.89f), edge * 0.65f)
            lineTo(width - edge * 2.5f, edge * 0.9f)
            quadraticTo(width - edge * 0.8f, edge, width - edge * 0.7f, edge * 3.0f)

            // Right edge: small alternating tears, kept clear of content.
            lineTo(width - edge * 0.55f, height * 0.09f)
            lineTo(width - edge * 0.95f, height * 0.15f)
            lineTo(width - edge * 0.58f, height * 0.21f)
            lineTo(width - edge * 1.08f, height * 0.27f)
            lineTo(width - edge * 0.62f, height * 0.34f)
            lineTo(width - edge * 1.02f, height * 0.41f)
            lineTo(width - edge * 0.55f, height * 0.48f)
            lineTo(width - edge * 0.98f, height * 0.55f)
            lineTo(width - edge * 0.58f, height * 0.62f)
            lineTo(width - edge * 1.08f, height * 0.69f)
            lineTo(width - edge * 0.6f, height * 0.76f)
            lineTo(width - edge * 0.96f, height * 0.83f)
            lineTo(width - edge * 0.6f, height - edge * 3.0f)
            quadraticTo(width - edge * 0.7f, height - edge, width - edge * 2.8f, height - edge * 0.72f)

            // Bottom edge: a slightly frayed, asymmetrical finish.
            lineTo(x(0.91f), height - edge * 0.55f)
            lineTo(x(0.86f), height - edge * 0.95f)
            lineTo(x(0.81f), height - edge * 0.6f)
            lineTo(x(0.755f), height - edge * 1.04f)
            lineTo(x(0.70f), height - edge * 0.58f)
            lineTo(x(0.645f), height - edge * 0.98f)
            lineTo(x(0.59f), height - edge * 0.55f)
            lineTo(x(0.535f), height - edge * 1.05f)
            lineTo(x(0.48f), height - edge * 0.6f)
            lineTo(x(0.425f), height - edge * 0.96f)
            lineTo(x(0.37f), height - edge * 0.56f)
            lineTo(x(0.315f), height - edge * 1.08f)
            lineTo(x(0.26f), height - edge * 0.58f)
            lineTo(x(0.205f), height - edge * 0.98f)
            lineTo(x(0.15f), height - edge * 0.56f)
            lineTo(x(0.095f), height - edge * 0.92f)
            lineTo(edge * 2.7f, height - edge * 0.7f)
            quadraticTo(edge * 0.75f, height - edge, edge * 0.66f, height - edge * 3.0f)

            // Left edge: a different rhythm from the right keeps it handmade.
            lineTo(edge * 0.52f, height * 0.90f)
            lineTo(edge * 1.02f, height * 0.84f)
            lineTo(edge * 0.58f, height * 0.78f)
            lineTo(edge * 1.08f, height * 0.71f)
            lineTo(edge * 0.5f, height * 0.64f)
            lineTo(edge * 0.98f, height * 0.57f)
            lineTo(edge * 0.56f, height * 0.50f)
            lineTo(edge * 1.1f, height * 0.43f)
            lineTo(edge * 0.52f, height * 0.36f)
            lineTo(edge * 0.96f, height * 0.29f)
            lineTo(edge * 0.54f, height * 0.22f)
            lineTo(edge * 1.06f, height * 0.15f)
            lineTo(edge * 0.6f, height * 0.08f)
            lineTo(edge * 0.72f, edge * 3.0f)
            quadraticTo(edge * 0.85f, edge, edge * 2.8f, edge * 0.78f)
            close()
        }

        return Outline.Generic(path)
    }
}
