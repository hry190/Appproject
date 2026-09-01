package com.jueqiao.jianghu.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

internal const val LoginComponentsFadeMillis = 850
internal const val MistGatherMillis = 2_500
internal const val MistCoveredHoldMillis = 1_000L
internal const val MistDisperseMillis = 1_250

private data class MistBandSpec(
    val topFraction: Float,
    val heightFraction: Float,
    val startXFraction: Float,
    val coveredXFraction: Float,
    val endXFraction: Float,
    val startY: Dp,
    val coveredY: Dp,
    val endY: Dp,
    val delay: Float,
    val tone: Color,
    val blur: Dp,
)

private val MistBands = listOf(
    MistBandSpec(
        topFraction = 0.03f,
        heightFraction = 0.27f,
        startXFraction = 0.10f,
        coveredXFraction = -0.03f,
        endXFraction = -0.18f,
        startY = 28.dp,
        coveredY = (-8).dp,
        endY = (-58).dp,
        delay = 0.10f,
        tone = Color(0xFFC2CEC5),
        blur = 32.dp,
    ),
    MistBandSpec(
        topFraction = 0.22f,
        heightFraction = 0.25f,
        startXFraction = -0.11f,
        coveredXFraction = 0.02f,
        endXFraction = 0.16f,
        startY = 34.dp,
        coveredY = (-10).dp,
        endY = (-70).dp,
        delay = 0.02f,
        tone = Color(0xFFAEBEB2),
        blur = 36.dp,
    ),
    MistBandSpec(
        topFraction = 0.39f,
        heightFraction = 0.30f,
        startXFraction = 0.12f,
        coveredXFraction = -0.02f,
        endXFraction = -0.17f,
        startY = 42.dp,
        coveredY = (-12).dp,
        endY = (-76).dp,
        delay = 0.15f,
        tone = Color(0xFFBECAC1),
        blur = 30.dp,
    ),
    MistBandSpec(
        topFraction = 0.55f,
        heightFraction = 0.29f,
        startXFraction = -0.12f,
        coveredXFraction = 0.03f,
        endXFraction = 0.18f,
        startY = 48.dp,
        coveredY = (-14).dp,
        endY = (-86).dp,
        delay = 0.06f,
        tone = Color(0xFFA8BAAD),
        blur = 34.dp,
    ),
    MistBandSpec(
        topFraction = 0.73f,
        heightFraction = 0.33f,
        startXFraction = -0.07f,
        coveredXFraction = 0.01f,
        endXFraction = 0.13f,
        startY = 64.dp,
        coveredY = (-18).dp,
        endY = (-108).dp,
        delay = 0f,
        tone = Color(0xFFB7C5BA),
        blur = 28.dp,
    ),
)

/**
 * Full-window mountain mist shared by the login and destination screens.
 * Phase 0..1 gathers the mist; phase 1..2 disperses it while continuing the drift.
 */
@Composable
internal fun LoginMistTransition(
    phase: Float,
    modifier: Modifier = Modifier,
) {
    if (phase <= 0f || phase >= 2f) return

    val coverage = if (phase <= 1f) phase else 2f - phase
    val softenedCoverage = coverage.coerceIn(0f, 1f).pow(0.72f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.93f * softenedCoverage)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF9FB1A4),
                            Color(0xFFB9C7BD),
                            Color(0xFFA4B7A9),
                        )
                    )
                ),
        )

        MistBands.forEachIndexed { index, spec ->
            val bandCoverage = if (phase <= 1f) {
                ((phase - spec.delay) / (1f - spec.delay)).coerceIn(0f, 1f)
            } else {
                (2f - phase).coerceIn(0f, 1f)
            }.pow(0.74f)

            val localPhase = phase.coerceIn(0f, 2f)
            val horizontalFraction = if (localPhase <= 1f) {
                lerp(spec.startXFraction, spec.coveredXFraction, localPhase)
            } else {
                lerp(spec.coveredXFraction, spec.endXFraction, localPhase - 1f)
            }
            val verticalOffset = if (localPhase <= 1f) {
                lerp(spec.startY, spec.coveredY, localPhase)
            } else {
                lerp(spec.coveredY, spec.endY, localPhase - 1f)
            }

            MistBand(
                tone = spec.tone,
                blur = spec.blur,
                modifier = Modifier
                    .offset(
                        x = maxWidth * (-0.42f + horizontalFraction),
                        y = maxHeight * spec.topFraction + verticalOffset,
                    )
                    .width(maxWidth * 1.84f)
                    .height(maxHeight * spec.heightFraction)
                    .graphicsLayer(alpha = (0.92f + index * 0.012f) * bandCoverage),
            )
        }
    }
}

@Composable
private fun MistBand(
    tone: Color,
    blur: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 20.dp)
                .blur(blur + 10.dp, BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x73506457),
                            Color(0x3F435548),
                            Color.Transparent,
                        )
                    ),
                    RoundedCornerShape(percent = 50),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blur, BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        listOf(
                            tone.copy(alpha = 0.96f),
                            tone.copy(alpha = 0.78f),
                            tone.copy(alpha = 0.34f),
                            Color.Transparent,
                        )
                    ),
                    RoundedCornerShape(percent = 50),
                ),
        )
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp =
    start + (end - start) * fraction.coerceIn(0f, 1f)
