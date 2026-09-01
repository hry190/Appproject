package com.jueqiao.jianghu.ui.screens.home

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * Home1 的动态背景。
 *
 * 背景动画和落叶都位于内容层后方，因此熊猫、牌匾、快捷入口及其点击区域
 * 不会被移动或重新绘制。Android 13+ 使用像素级植被蒙版；旧版本保留静态
 * 背景，但仍显示光影和落叶。
 */
@Composable
internal fun HomeWindBackground(
    modifier: Modifier = Modifier,
) {
    var timeSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val startedAtNanos = withFrameNanos { it }
        while (currentCoroutineContext().isActive) {
            withFrameNanos { frameNanos ->
                timeSeconds = ((frameNanos - startedAtNanos) / 1_000_000_000f) % 600f
            }
        }
    }

    Box(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ShaderWindBackground(
                timeSeconds = timeSeconds,
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .graphicsLayer {
                        scaleX = 2f
                        scaleY = 2f
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
            )
        } else {
            StaticHomeBackground(modifier = Modifier.fillMaxSize())
        }

        WindAtmosphereLayer(
            timeSeconds = timeSeconds,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun StaticHomeBackground(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.img_home_bg),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderWindBackground(
    timeSeconds: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgroundBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_home_bg,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }
    val windMaskBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_home_wind_mask,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }
    DisposableEffect(backgroundBitmap, windMaskBitmap) {
        onDispose {
            backgroundBitmap.recycle()
            windMaskBitmap.recycle()
        }
    }

    val bitmapShader = remember(backgroundBitmap) {
        BitmapShader(
            backgroundBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val maskShader = remember(windMaskBitmap) {
        BitmapShader(
            windMaskBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val shader = remember(bitmapShader, maskShader) {
        RuntimeShader(HOME_WIND_SHADER).apply {
            setInputShader("content", bitmapShader)
            setInputShader("windMask", maskShader)
            setFloatUniform("u_resolution", 1f, 1f)
            setFloatUniform("u_image_scale", 1f)
            setFloatUniform("u_image_offset", 0f, 0f)
            setFloatUniform("u_left_phase", 0f)
            setFloatUniform("u_center_phase", 0f)
            setFloatUniform("u_right_phase", 0f)
            setFloatUniform("u_tip_phase", 0f)
        }
    }
    val shaderBrush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = modifier) {
        val width = size.width.coerceAtLeast(1f)
        val height = size.height.coerceAtLeast(1f)
        val imageScale = maxOf(
            width / backgroundBitmap.width,
            height / backgroundBitmap.height,
        )
        val offsetX = (width - backgroundBitmap.width * imageScale) * 0.5f
        val offsetY = (height - backgroundBitmap.height * imageScale) * 0.5f

        shader.setFloatUniform("u_resolution", width, height)
        shader.setFloatUniform("u_image_scale", imageScale)
        shader.setFloatUniform("u_image_offset", offsetX, offsetY)
        val breeze = sin(timeSeconds * 0.66f) + 0.22f * sin(timeSeconds * 0.29f + 1.2f)
        val delayedLeft = sin(timeSeconds * 0.61f - 0.35f) +
            0.16f * sin(timeSeconds * 0.24f + 0.8f)
        val delayedRight = sin(timeSeconds * 0.69f + 0.42f) +
            0.18f * sin(timeSeconds * 0.31f + 1.7f)
        val gust = 0.82f + 0.18f * sin(timeSeconds * 0.17f + 0.6f)
        shader.setFloatUniform("u_left_phase", delayedLeft * gust * HOME_WIND_STRENGTH)
        shader.setFloatUniform("u_center_phase", breeze * gust * HOME_WIND_STRENGTH)
        shader.setFloatUniform("u_right_phase", delayedRight * gust * HOME_WIND_STRENGTH)
        shader.setFloatUniform(
            "u_tip_phase",
            sin(timeSeconds * 0.93f) * HOME_WIND_STRENGTH,
        )
        drawRect(brush = shaderBrush)
    }
}

private data class FallingLeafSpec(
    val startX: Float,
    val duration: Float,
    val delay: Float,
    val drift: Float,
    val sway: Float,
    val widthDp: Float,
    val turn: Float,
    val endY: Float,
    val phase: Float,
    val depth: Float,
)

private val FallingLeaves = listOf(
    FallingLeafSpec(18f, 17.2f, 2.3f, 13f, 4.2f, 4.6f, 1f, 91f, 0.3f, 0.72f),
    FallingLeafSpec(76f, 21.5f, 8.4f, -11f, 5.1f, 5.4f, -1f, 84f, 1.7f, 0.84f),
    FallingLeafSpec(44f, 18.8f, 15.3f, 9f, 3.8f, 4.9f, 1f, 96f, 2.4f, 0.66f),
    FallingLeafSpec(87f, 23.8f, 3.8f, -16f, 4.6f, 4.3f, -1f, 78f, 0.9f, 0.58f),
    FallingLeafSpec(31f, 22.4f, 18f, 12f, 5.4f, 5.8f, -1f, 88f, 2.9f, 0.88f),
    FallingLeafSpec(62f, 19.7f, 6.2f, -8f, 4f, 4.8f, 1f, 93f, 1.2f, 0.70f),
    FallingLeafSpec(9f, 25.2f, 22f, 15f, 4.8f, 4.2f, 1f, 82f, 2.1f, 0.56f),
    FallingLeafSpec(52f, 20.9f, 2.6f, 11f, 4.4f, 5f, -1f, 90f, 0.6f, 0.78f),
    FallingLeafSpec(93f, 18.3f, 14.9f, -13f, 3.7f, 4.5f, 1f, 86f, 2.6f, 0.64f),
    FallingLeafSpec(25f, 24.1f, 9.3f, -7f, 5.2f, 5.6f, -1f, 95f, 1.4f, 0.90f),
    FallingLeafSpec(69f, 16.8f, 13.6f, 14f, 4.1f, 4.4f, 1f, 80f, 3f, 0.62f),
)

private val LeafColors = listOf(
    Color(0xFF495936),
    Color(0xFF6E7543),
    Color(0xFF81784A),
    Color(0xFF57663A),
)

@Composable
private fun WindAtmosphereLayer(
    timeSeconds: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val lightCenter = Offset(
            x = size.width * 0.68f + sin(timeSeconds * 0.19f) * size.width * 0.018f,
            y = size.height * 0.18f + cos(timeSeconds * 0.13f) * size.height * 0.004f,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFEDB0).copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                center = lightCenter,
                radius = size.width * 0.48f,
            ),
            radius = size.width * 0.48f,
            center = lightCenter,
        )

        repeat(10) { index ->
            val seed = (index * 0.61803398875f) % 1f
            val duration = 10f + (index % 5) * 2.7f
            val phase = ((timeSeconds + seed * duration) % duration) / duration
            val moteX = 0.44f + ((index * 29) % 43) / 100f +
                sin(timeSeconds * 0.45f + index) * 0.025f
            val moteY = (0.17f + ((index * 19) % 51) / 100f) - phase * 0.13f
            val alpha = sin(phase * PI).toFloat().coerceAtLeast(0f) * 0.24f
            drawCircle(
                color = Color(0xFFFFE7A3).copy(alpha = alpha),
                radius = (1.1f + (index % 3) * 0.35f).dp.toPx(),
                center = Offset(size.width * moteX, size.height * moteY),
            )
        }

        FallingLeaves.forEachIndexed { index, leaf ->
            val cycle = ((timeSeconds + leaf.delay) % leaf.duration) / leaf.duration
            if (cycle >= LEAF_ACTIVE_PORTION) return@forEachIndexed

            val progress = cycle / LEAF_ACTIVE_PORTION
            val fadeIn = (progress / 0.08f).coerceAtMost(1f)
            val fadeOut = ((1f - progress) / 0.13f).coerceAtMost(1f)
            val alpha = minOf(fadeIn, fadeOut).coerceAtLeast(0f) *
                (0.36f + leaf.depth * 0.46f)
            val fallCurve = progress * progress * 0.18f + progress * 0.82f
            val xPercent = leaf.startX + leaf.drift * progress * 1.2f +
                sin(progress * PI.toFloat() * 4.2f + leaf.phase) * leaf.sway * 1.2f +
                sin(timeSeconds * 0.36f + leaf.phase) * 1.2f
            val yPercent = -4f + fallCurve * (leaf.endY + 5f)
            val rotation = leaf.turn * progress * 310f +
                sin(progress * PI.toFloat() * 5f + leaf.phase) * 32f
            val flip = 0.34f +
                abs(cos(progress * PI.toFloat() * 5.4f + leaf.phase)) * 0.66f

            drawFallingLeaf(
                center = Offset(
                    x = size.width * xPercent / 100f,
                    y = size.height * yPercent / 100f,
                ),
                width = leaf.widthDp.dp.toPx(),
                rotation = rotation,
                horizontalScale = flip,
                color = LeafColors[index % LeafColors.size],
                alpha = alpha,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFallingLeaf(
    center: Offset,
    width: Float,
    rotation: Float,
    horizontalScale: Float,
    color: Color,
    alpha: Float,
) {
    val height = width * 2.8f
    val leafPath = Path().apply {
        moveTo(center.x, center.y - height * 0.5f)
        quadraticTo(
            center.x + width * 0.58f,
            center.y - height * 0.20f,
            center.x + width * 0.46f,
            center.y,
        )
        quadraticTo(
            center.x + width * 0.30f,
            center.y + height * 0.32f,
            center.x,
            center.y + height * 0.5f,
        )
        quadraticTo(
            center.x - width * 0.30f,
            center.y + height * 0.32f,
            center.x - width * 0.46f,
            center.y,
        )
        quadraticTo(
            center.x - width * 0.58f,
            center.y - height * 0.20f,
            center.x,
            center.y - height * 0.5f,
        )
        close()
    }

    rotate(degrees = rotation, pivot = center) {
        scale(scaleX = horizontalScale, scaleY = 1f, pivot = center) {
            drawPath(
                path = leafPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = alpha * 0.72f),
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.78f),
                    ),
                    start = Offset(center.x - width * 0.5f, center.y),
                    end = Offset(center.x + width * 0.5f, center.y),
                ),
            )
            drawLine(
                color = Color(0xFF303D29).copy(alpha = alpha * 0.42f),
                start = Offset(center.x, center.y - height * 0.38f),
                end = Offset(center.x, center.y + height * 0.39f),
                strokeWidth = (width * 0.09f).coerceAtLeast(0.45f),
            )
        }
    }
}

private const val HOME_WIND_STRENGTH = 1f
private const val LEAF_ACTIVE_PORTION = 0.90f

private const val HOME_WIND_SHADER = """
    uniform shader content;
    uniform shader windMask;
    uniform float2 u_resolution;
    uniform float u_image_scale;
    uniform float2 u_image_offset;
    uniform float u_left_phase;
    uniform float u_center_phase;
    uniform float u_right_phase;
    uniform float u_tip_phase;

    float2 rotateDelta(float2 point, float2 pivot, float angle) {
        float c = cos(angle);
        float s = sin(angle);
        float2 local = point - pivot;
        float2 rotated = float2(
            c * local.x - s * local.y,
            s * local.x + c * local.y
        );
        return rotated - local;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / u_resolution;
        float2 imagePoint = (fragCoord - u_image_offset) / u_image_scale;
        half3 encodedWeight = windMask.eval(imagePoint).rgb;
        float leftWeight = encodedWeight.r;
        float centerWeight = encodedWeight.g;
        float rightWeight = encodedWeight.b;
        float totalWeight = max(0.0001, leftWeight + centerWeight + rightWeight);
        float leafMask = clamp(totalWeight, 0.0, 1.0);
        float motionAmount = smoothstep(0.08, 0.68, leafMask);

        float foreground = smoothstep(0.49, 0.56, uv.y);
        float upperDepth = smoothstep(0.14, 0.34, uv.y) * (1.0 - foreground);
        float2 leftPivot = mix(
            mix(float2(0.12, 0.015), float2(0.22, 0.19), upperDepth),
            float2(0.00, 0.63),
            foreground
        );
        float2 rightPivot = mix(
            mix(float2(0.90, 0.025), float2(0.82, 0.19), upperDepth),
            float2(1.00, 0.62),
            foreground
        );
        float leftAmplitude = mix(mix(0.032, 0.038, upperDepth), 0.0085, foreground);
        float rightAmplitude = mix(mix(0.031, 0.037, upperDepth), 0.0080, foreground);

        float2 displacement = float2(0.0);
        displacement += rotateDelta(uv, leftPivot, u_left_phase * leftAmplitude) * leftWeight;
        displacement += rotateDelta(uv, float2(0.50, 0.015), u_center_phase * 0.027) * centerWeight;
        displacement += rotateDelta(uv, rightPivot, u_right_phase * rightAmplitude) * rightWeight;
        displacement = displacement / totalWeight * motionAmount;

        float tipLag = u_tip_phase * mix(0.0022, 0.00065, foreground);
        displacement += float2(tipLag, tipLag * 0.20) * motionAmount;

        float2 samplePoint = clamp(
            (uv + displacement) * u_resolution,
            float2(0.5),
            u_resolution - float2(0.5)
        );
        float2 movedImagePoint = (samplePoint - u_image_offset) / u_image_scale;
        return content.eval(movedImagePoint);
    }
"""
