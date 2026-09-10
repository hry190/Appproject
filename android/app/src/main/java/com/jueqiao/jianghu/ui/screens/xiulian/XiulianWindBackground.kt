package com.jueqiao.jianghu.ui.screens.xiulian

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
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.jueqiao.jianghu.R
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * 修炼页背景光斑动效：底图保持静止，只对原图中已有的地面光斑做局部扭曲和缩放。
 * 这样光斑像阳光穿过竹叶后投射到地面，建筑、石阶、熊猫和气泡不会整体移动。
 */
@Composable
internal fun XiulianWindBackground(
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
        Image(
            painter = painterResource(R.drawable.img_xiulian_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            XiulianLightSpotShaderLayer(
                timeSeconds = timeSeconds,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun XiulianLightSpotShaderLayer(
    timeSeconds: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgroundBitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_xiulian_bg,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }
    DisposableEffect(backgroundBitmap) {
        onDispose { backgroundBitmap.recycle() }
    }

    val bitmapShader = remember(backgroundBitmap) {
        BitmapShader(
            backgroundBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
    }
    val shader = remember(bitmapShader) {
        RuntimeShader(XIULIAN_LIGHT_SPOT_SHADER).apply {
            setInputShader("content", bitmapShader)
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

private const val XIULIAN_LIGHT_SPOT_SHADER = """
    uniform shader content;
    uniform float2 u_resolution;
    uniform float u_image_scale;
    uniform float2 u_image_offset;
    uniform float u_time;

    float spotWeight(float2 uv, float2 center, float2 radius) {
        float2 distance = (uv - center) / radius;
        // Cheap skew keeps the mask organic without adding many trig calls per
        // pixel; the animated warp below supplies the remaining motion.
        float skew = clamp(1.0
            + 0.16 * distance.x
            + 0.10 * distance.y
            + 0.08 * distance.x * distance.y, 0.72, 1.28);
        float normalizedDistance = length(distance) * skew;
        float radial = 1.0 - smoothstep(0.18, 1.08, normalizedDistance);
        float2 lobeOffset = float2(-0.20, 0.14);
        float lobe = 1.0 - smoothstep(
            0.12,
            1.02,
            length(distance - lobeOffset)
        );
        return clamp(radial * 0.72 + lobe * 0.24, 0.0, 1.0);
    }

    float2 spotWarp(
        float2 uv,
        float2 center,
        float2 radius,
        float speed,
        float phase
    ) {
        float2 distance = uv - center;
        float normalizedDistance = length(distance / radius);
        float edgeFalloff = 1.0 - smoothstep(0.0, 1.0, normalizedDistance);
        // Keep the animated patch compact: it breathes slightly instead of
        // expanding into the large circular bokeh seen in the source image.
        float scale = 0.70 + sin(u_time * speed + phase) * 0.08;
        float2 drift = float2(
            sin(u_time * speed * 0.72 + phase) * 0.0065,
            cos(u_time * speed * 0.58 + phase) * 0.0035
        ) * edgeFalloff;
        return center + distance / scale + drift;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / u_resolution;

        // 这些坐标对应修炼页原图底部已经存在的黄色、绿色光斑。
        float2 c0 = float2(0.016, 0.884);
        float2 c1 = float2(0.122, 0.851);
        float2 c2 = float2(0.142, 0.904);
        float2 c3 = float2(0.206, 0.906);
        float2 c4 = float2(0.298, 0.937);
        float2 c5 = float2(0.340, 0.858);
        float2 c6 = float2(0.392, 0.862);
        float2 c7 = float2(0.435, 0.861);
        float2 c8 = float2(0.29, 0.09);
        float2 c9 = float2(0.52, 0.14);
        float2 c10 = float2(0.78, 0.08);
        float2 c11 = float2(0.42, 0.27);
        float2 c12 = float2(0.67, 0.30);
        float2 c13 = float2(0.16, 0.30);
        float2 c14 = float2(0.88, 0.37);

        float2 r0 = float2(0.036, 0.030);
        float2 r1 = float2(0.052, 0.038);
        float2 r2 = float2(0.046, 0.040);
        float2 r3 = float2(0.036, 0.031);
        float2 r4 = float2(0.042, 0.035);
        float2 r5 = float2(0.053, 0.039);
        float2 r6 = float2(0.033, 0.029);
        float2 r7 = float2(0.046, 0.034);
        float2 r8 = float2(0.095, 0.065);
        float2 r9 = float2(0.085, 0.065);
        float2 r10 = float2(0.095, 0.070);
        float2 r11 = float2(0.085, 0.075);
        float2 r12 = float2(0.090, 0.080);
        float2 r13 = float2(0.080, 0.070);
        float2 r14 = float2(0.080, 0.065);

        // 台阶和石路上原图已有的细碎暖色光斑，数量更多、尺寸更小。
        float2 c15 = float2(0.52, 0.590);
        float2 c16 = float2(0.59, 0.590);
        float2 c17 = float2(0.67, 0.600);
        float2 c18 = float2(0.78, 0.610);
        float2 c19 = float2(0.44, 0.630);
        float2 c20 = float2(0.54, 0.650);
        float2 c21 = float2(0.64, 0.660);
        float2 c22 = float2(0.72, 0.670);
        float2 c23 = float2(0.31, 0.690);
        float2 c24 = float2(0.42, 0.710);
        float2 c25 = float2(0.54, 0.720);
        float2 c26 = float2(0.67, 0.730);
        float2 c27 = float2(0.80, 0.750);
        float2 c28 = float2(0.28, 0.770);
        float2 c29 = float2(0.46, 0.790);
        float2 c30 = float2(0.62, 0.800);

        float2 r15 = float2(0.030, 0.012);
        float2 r16 = float2(0.026, 0.011);
        float2 r17 = float2(0.024, 0.011);
        float2 r18 = float2(0.026, 0.012);
        float2 r19 = float2(0.025, 0.010);
        float2 r20 = float2(0.032, 0.011);
        float2 r21 = float2(0.028, 0.012);
        float2 r22 = float2(0.026, 0.010);
        float2 r23 = float2(0.026, 0.012);
        float2 r24 = float2(0.030, 0.011);
        float2 r25 = float2(0.025, 0.010);
        float2 r26 = float2(0.030, 0.012);
        float2 r27 = float2(0.028, 0.011);
        float2 r28 = float2(0.024, 0.010);
        float2 r29 = float2(0.028, 0.011);
        float2 r30 = float2(0.024, 0.010);

        float w0 = spotWeight(uv, c0, r0);
        float w1 = spotWeight(uv, c1, r1);
        float w2 = spotWeight(uv, c2, r2);
        float w3 = spotWeight(uv, c3, r3);
        float w4 = spotWeight(uv, c4, r4);
        float w5 = spotWeight(uv, c5, r5);
        float w6 = spotWeight(uv, c6, r6);
        float w7 = spotWeight(uv, c7, r7);
        // Compact masks drive the bright center; the wider masks above are
        // retained only as a soft transition for the baked source texture.
        float k0 = spotWeight(uv, c0, r0 * 0.66);
        float k1 = spotWeight(uv, c1, r1 * 0.66);
        float k2 = spotWeight(uv, c2, r2 * 0.66);
        float k3 = spotWeight(uv, c3, r3 * 0.66);
        float k4 = spotWeight(uv, c4, r4 * 0.66);
        float k5 = spotWeight(uv, c5, r5 * 0.66);
        float k6 = spotWeight(uv, c6, r6 * 0.66);
        float k7 = spotWeight(uv, c7, r7 * 0.66);
        float w8 = spotWeight(uv, c8, r8);
        float w9 = spotWeight(uv, c9, r9);
        float w10 = spotWeight(uv, c10, r10);
        float w11 = spotWeight(uv, c11, r11);
        float w12 = spotWeight(uv, c12, r12);
        float w13 = spotWeight(uv, c13, r13);
        float w14 = spotWeight(uv, c14, r14);
        float w15 = spotWeight(uv, c15, r15);
        float w16 = spotWeight(uv, c16, r16);
        float w17 = spotWeight(uv, c17, r17);
        float w18 = spotWeight(uv, c18, r18);
        float w19 = spotWeight(uv, c19, r19);
        float w20 = spotWeight(uv, c20, r20);
        float w21 = spotWeight(uv, c21, r21);
        float w22 = spotWeight(uv, c22, r22);
        float w23 = spotWeight(uv, c23, r23);
        float w24 = spotWeight(uv, c24, r24);
        float w25 = spotWeight(uv, c25, r25);
        float w26 = spotWeight(uv, c26, r26);
        float w27 = spotWeight(uv, c27, r27);
        float w28 = spotWeight(uv, c28, r28);
        float w29 = spotWeight(uv, c29, r29);
        float w30 = spotWeight(uv, c30, r30);

        float2 warpedUv = uv;
        warpedUv = mix(warpedUv, spotWarp(uv, c0, r0 * 0.66, 1.38, 0.2), w0 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c1, r1 * 0.66, 1.14, 1.3), w1 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c2, r2 * 0.66, 1.64, 2.5), w2 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c3, r3 * 0.66, 1.26, 3.6), w3 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c4, r4 * 0.66, 1.76, 4.5), w4 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c5, r5 * 0.66, 1.08, 5.0), w5 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c6, r6 * 0.66, 1.52, 5.8), w6 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c7, r7 * 0.66, 1.20, 6.4), w7 * 0.68);
        warpedUv = mix(warpedUv, spotWarp(uv, c15, r15, 1.32, 0.7), w15 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c16, r16, 1.74, 1.8), w16 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c17, r17, 1.48, 2.9), w17 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c18, r18, 1.92, 4.0), w18 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c19, r19, 1.22, 5.1), w19 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c20, r20, 1.58, 0.4), w20 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c21, r21, 1.36, 1.5), w21 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c22, r22, 1.80, 2.6), w22 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c23, r23, 1.12, 3.7), w23 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c24, r24, 1.66, 4.8), w24 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c25, r25, 1.30, 5.9), w25 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c26, r26, 1.96, 0.9), w26 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c27, r27, 1.42, 2.0), w27 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c28, r28, 1.78, 3.1), w28 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c29, r29, 1.20, 4.2), w29 * 0.84);
        warpedUv = mix(warpedUv, spotWarp(uv, c30, r30, 1.62, 5.3), w30 * 0.84);

        float2 imagePoint = (fragCoord - u_image_offset) / u_image_scale;
        half4 original = content.eval(imagePoint);

        float2 movedSamplePoint = clamp(
            warpedUv * u_resolution,
            float2(0.5),
            u_resolution - float2(0.5)
        );
        float2 movedImagePoint = (movedSamplePoint - u_image_offset) / u_image_scale;
        half4 moved = content.eval(movedImagePoint);

        // 只让原图中足够明亮的光斑像素参与叠加，周围地面不会被拖动。
        float luma = dot(original.rgb, float3(0.299, 0.587, 0.114));
        float brightSpot = smoothstep(0.38, 0.72, luma);
        float outerGround = 0.0;
        outerGround = max(outerGround, w0);
        outerGround = max(outerGround, w1);
        outerGround = max(outerGround, w2);
        outerGround = max(outerGround, w3);
        outerGround = max(outerGround, w4);
        outerGround = max(outerGround, w5);
        outerGround = max(outerGround, w6);
        outerGround = max(outerGround, w7);

        // The source artwork contains a few baked round highlights. Sample a
        // wider local neighborhood to flatten their rims before drawing the
        // compact dapple back in, while retaining the ground's own texture.
        float2 groundStep = float2(
            u_resolution.x * 0.085,
            u_resolution.y * 0.060
        ) / u_image_scale;
        half3 neighborhood = (
            content.eval(imagePoint + float2(groundStep.x, 0.0)).rgb +
            content.eval(imagePoint - float2(groundStep.x, 0.0)).rgb +
            content.eval(imagePoint + float2(0.0, groundStep.y)).rgb +
            content.eval(imagePoint - float2(0.0, groundStep.y)).rgb
        ) * 0.25;
        float neighborhoodLuma = dot(neighborhood, float3(0.299, 0.587, 0.114));
        float rimCorrection = smoothstep(0.04, 0.28, luma - neighborhoodLuma)
            * outerGround * 0.78;
        half3 cleanedGround = mix(
            original.rgb,
            neighborhood,
            clamp(rimCorrection, 0.0, 0.86)
        );

        float compactGround = 0.0;
        compactGround = max(compactGround, k0);
        compactGround = max(compactGround, k1);
        compactGround = max(compactGround, k2);
        compactGround = max(compactGround, k3);
        compactGround = max(compactGround, k4);
        compactGround = max(compactGround, k5);
        compactGround = max(compactGround, k6);
        compactGround = max(compactGround, k7);
        float localSpot = compactGround;
        localSpot = max(localSpot, w15);
        localSpot = max(localSpot, w16);
        localSpot = max(localSpot, w17);
        localSpot = max(localSpot, w18);
        localSpot = max(localSpot, w19);
        localSpot = max(localSpot, w20);
        localSpot = max(localSpot, w21);
        localSpot = max(localSpot, w22);
        localSpot = max(localSpot, w23);
        localSpot = max(localSpot, w24);
        localSpot = max(localSpot, w25);
        localSpot = max(localSpot, w26);
        localSpot = max(localSpot, w27);
        localSpot = max(localSpot, w28);
        localSpot = max(localSpot, w29);
        localSpot = max(localSpot, w30);
        // 把边缘权重压低，避免光斑移动后出现不自然的描边或环状光圈。
        float spotCore = pow(clamp(compactGround, 0.0, 1.0), 2.55);
        float spotPulse = 0.78 + 0.44 * (0.5 + 0.5 * sin(u_time * 1.18 + dot(uv, float2(23.0, 17.0))));
        float movementAlpha = clamp(spotCore * brightSpot * spotPulse * 1.16, 0.0, 0.96);

        // 竹林高亮处偶尔出现小而亮的叶片反光，外圈只保留宽而柔的光晕。
        float upperHighlight = 1.0 - smoothstep(0.40, 0.78, uv.y);
        float brightArea = smoothstep(0.54, 0.86, luma) * upperHighlight;
        float sparkle0 = pow(max(sin(u_time * 0.90 + 0.4), 0.0), 20.0);
        float sparkle1 = pow(max(sin(u_time * 0.68 + 2.0), 0.0), 22.0);
        float sparkle2 = pow(max(sin(u_time * 1.05 + 4.1), 0.0), 20.0);
        float sparkle3 = pow(max(sin(u_time * 0.76 + 5.5), 0.0), 22.0);
        float sparkle4 = pow(max(sin(u_time * 0.92 + 1.2), 0.0), 20.0);
        float sparkle5 = pow(max(sin(u_time * 0.61 + 3.4), 0.0), 22.0);
        float sparkle6 = pow(max(sin(u_time * 0.86 + 5.0), 0.0), 20.0);
        float core8 = spotWeight(uv, c8, r8 * 0.22);
        float core9 = spotWeight(uv, c9, r9 * 0.22);
        float core10 = spotWeight(uv, c10, r10 * 0.22);
        float core11 = spotWeight(uv, c11, r11 * 0.22);
        float core12 = spotWeight(uv, c12, r12 * 0.22);
        float core13 = spotWeight(uv, c13, r13 * 0.22);
        float core14 = spotWeight(uv, c14, r14 * 0.22);
        float sparkleHalo = max(
            max(max(w8 * sparkle0, w9 * sparkle1), max(w10 * sparkle2, w11 * sparkle3)),
            max(max(w12 * sparkle4, w13 * sparkle5), w14 * sparkle6)
        );
        float sparkleCore = max(
            max(max(core8 * sparkle0, core9 * sparkle1), max(core10 * sparkle2, core11 * sparkle3)),
            max(max(core12 * sparkle4, core13 * sparkle5), core14 * sparkle6)
        );
        float haloFlash = sparkleHalo * brightArea * 0.16;
        float coreFlash = sparkleCore * brightArea * 1.00;
        half3 warmMoved = mix(moved.rgb, float3(0.84, 0.72, 0.52), 0.28);
        half3 motionColor = mix(cleanedGround, moved.rgb, movementAlpha);
        float warmAmount = smoothstep(0.48, 0.90, localSpot) * brightSpot * 0.46;
        motionColor = mix(motionColor, warmMoved, warmAmount);

        // Warm edge-to-center gradient: low-saturation gold at the edge and a
        // small, brighter core. The angular masks above keep the silhouette
        // organic instead of rendering a regular circle.
        float dappleGradient = clamp(compactGround * brightSpot, 0.0, 1.0);
        float dappleCenter = pow(dappleGradient, 3.20);
        float dappleTexture = 0.92 + 0.08 * sin(
            uv.x * 42.0 + uv.y * 31.0 + u_time * 1.10
        );
        half3 dappleEdge = mix(
            motionColor,
            float3(0.76, 0.70, 0.55),
            (0.24 + 0.08 * dappleTexture) * spotPulse
        );
        half3 dappleTarget = mix(
            dappleEdge,
            float3(0.99, 0.88, 0.68),
            0.42 * dappleCenter
        );
        motionColor = mix(
            motionColor,
            dappleTarget,
            clamp(dappleGradient * 0.68, 0.0, 0.82)
        );
        half3 shimmerColor = motionColor
            + float3(0.14, 0.075, 0.014) * haloFlash
            + float3(0.72, 0.460, 0.085) * coreFlash;

        // 直接输出完整底图，再在局部替换光斑，避免透明叠加产生描边和环状光圈。
        return half4(shimmerColor, 1.0);
    }
"""
