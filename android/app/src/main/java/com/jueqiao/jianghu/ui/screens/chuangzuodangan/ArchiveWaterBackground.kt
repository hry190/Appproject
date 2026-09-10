package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.jueqiao.jianghu.R
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * 荷塘背景的水面动效。
 *
 * 动画只采样水面遮罩，原图中的鱼群会和水面一起变形；鱼群位置通过
 * 柔和的局部强度图放大波动幅度，不再做抠图、移动或重新着色。
 */
@Composable
internal fun ArchiveWaterBackground(
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderArchiveWaterBackground(
            timeSeconds = timeSeconds,
            modifier = modifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.img_chuangzuodangan_bg),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderArchiveWaterBackground(
    timeSeconds: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgroundBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_chuangzuodangan_bg,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }
    val waterMaskBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_chuangzuodangan_water_mask,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }
    val fishFocusBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_chuangzuodangan_fish_focus,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }

    DisposableEffect(backgroundBitmap, waterMaskBitmap, fishFocusBitmap) {
        onDispose {
            backgroundBitmap.recycle()
            waterMaskBitmap.recycle()
            fishFocusBitmap.recycle()
        }
    }

    val bitmapShader = remember(backgroundBitmap) {
        BitmapShader(
            backgroundBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val waterMaskShader = remember(waterMaskBitmap) {
        BitmapShader(
            waterMaskBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val fishFocusShader = remember(fishFocusBitmap) {
        BitmapShader(
            fishFocusBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val shader = remember(bitmapShader, waterMaskShader, fishFocusShader) {
        RuntimeShader(ARCHIVE_WATER_SHADER).apply {
            setInputShader("content", bitmapShader)
            setInputShader("waterMask", waterMaskShader)
            setInputShader("fishFocus", fishFocusShader)
            setFloatUniform("u_resolution", 1f, 1f)
            setFloatUniform("u_image_scale", 1f)
            setFloatUniform("u_image_offset", 0f, 0f)
            setFloatUniform("u_time", 0f)
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
        shader.setFloatUniform("u_time", timeSeconds)
        drawRect(brush = shaderBrush)
    }
}

private const val ARCHIVE_WATER_SHADER = """
    uniform shader content;
    uniform shader waterMask;
    uniform shader fishFocus;
    uniform float2 u_resolution;
    uniform float u_image_scale;
    uniform float2 u_image_offset;
    uniform float u_time;

    half4 main(float2 fragCoord) {
        float2 imagePoint = (fragCoord - u_image_offset) / u_image_scale;
        float water = clamp(waterMask.eval(imagePoint).r, 0.0, 1.0);
        float focus = clamp(fishFocus.eval(imagePoint).r, 0.0, 1.0);
        float phase = u_time * 0.78;

        float waveA = sin(imagePoint.y * 0.027 + phase) +
            0.42 * sin((imagePoint.x + imagePoint.y) * 0.012 + phase * 0.65);
        float waveB = sin(imagePoint.x * 0.019 + phase * 0.72);
        float horizontalAmplitude = (1.5 + 10.0 * focus) * water;
        float verticalAmplitude = (0.6 + 4.0 * focus) * water;
        float2 sourceDelta = float2(
            horizontalAmplitude * waveA,
            verticalAmplitude * waveB
        );

        float2 movedImagePoint = imagePoint + sourceDelta;
        half4 color = content.eval(movedImagePoint);
        float shimmerAmount = (0.015 + 0.05 * focus) * water;
        float shimmer = 1.0 + shimmerAmount *
            sin(imagePoint.x * 0.026 + imagePoint.y * 0.013 + phase * 1.05);
        return half4(color.rgb * shimmer, color.a);
    }
"""
