package com.jueqiao.jianghu.ui.screens.houshan3

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.isActive

/**
 * 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;点击标签2-4 之外的空白区域跳转未完待续页。
 *
 * 布局:
 *   - 全屏背景图(后山3 转换.png)
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 屏幕空白点击无效
 *   - 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)
 *   - 标签2 图像(X=124, Y=521, W=96, H=170)+ 文字"拆招心法"(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14)+ 文字"炼"(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12)
 *   - 标签3 图像(X=43, Y=390, W=51, H=91)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6)
 *   - 标签4 图像(X=105, Y=295, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4)
 *   - 云朵(Ellipse 58.png,X=-46, Y=476, W=331, H=92)
 */
@Composable
fun Houshan3Screen(
    onBack: () -> Unit = {},
    onOpenUnfinished: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    // Ellipse 56 渐变色循环:Animatable<Float> 在 [0,1] 插值,4s 来回 (§23/24/25/26 修复)
    // 0 = A9C3C0, 1 = White;在 graphicsLayer 块内合成 Color
    val tintProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            tintProgress.animateTo(1f, animationSpec = tween(durationMillis = 4000, easing = LinearEasing))
            tintProgress.animateTo(0f, animationSpec = tween(durationMillis = 4000, easing = LinearEasing))
        }
    }

    // 2 朵渐变云朵(56/58)位置 + 透明度随机飘动(沿用 §13 rememberCloudFloat 模式)(§28)
    val (cloud56Dx, cloud56Dy, cloud56Alpha) = rememberCloudFloat()
    val (cloud58Dx, cloud58Dy, cloud58Alpha) = rememberCloudFloat()
    val (cloud57Dx, cloud57Dy, cloud57Alpha) = rememberCloudFloat()  // §29 加 Ellipse 57
    val (cloud5Dx, cloud5Dy, cloud5Alpha) = rememberCloudFloat()  // §33 加 Ellipse 5(实际与 Ellipse 57 同图)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(后山3 转换.png)
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)— 整屏 clickable,但 3 个标签 Box 自带消费事件 clickable (§20),点击标签不会冒泡触发跳转
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .clickable(onClick = onOpenUnfinished),
        ) {
            // 旧云朵(8f5a28c 基线,重命名为 _old 避免与 Ellipse 58.png 命名冲突 §35)— 资源已 mv → img_shilian3_cloud_old.png
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_old),
                contentDescription = "云朵",
                modifier = Modifier
                    .offset(x = (-46).dp, y = 476.dp)
                    .size(width = 331.dp, height = 92.dp),
                contentScale = ContentScale.FillBounds,
            )

            // 渐变云朵(Ellipse 56.png, X=263, Y=755, W=335, H=297) — tint A9C3C0 ↔ 白色 + 位置 + 透明度随机 (§23/26/28)
            // 注意:Modifier.graphicsLayer 不支持 colorFilter,改用 Image 自己的 colorFilter 参数
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_56),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (263f + cloud56Dx).dp, y = (755f + cloud56Dy).dp)
                    .size(width = 335.dp, height = 297.dp),
                colorFilter = ColorFilter.tint(
                    Color(
                        red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
                        green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
                        blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
                    ),
                    BlendMode.Modulate,
                ),
                alpha = cloud56Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 渐变云朵(Ellipse 58.png, X=78, Y=170, W=331, H=92) — 与 Ellipse 56 共用 tintProgress 同步循环 + 各自位置/透明度独立随机 (§27/28)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_58),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (78f + cloud58Dx).dp, y = (170f + cloud58Dy).dp)
                    .size(width = 331.dp, height = 92.dp),
                colorFilter = ColorFilter.tint(
                    Color(
                        red = 0xA9 + ((0xFF - 0xA9) * tintProgress.value).toInt(),
                        green = 0xC3 + ((0xFF - 0xC3) * tintProgress.value).toInt(),
                        blue = 0xC0 + ((0xFF - 0xC0) * tintProgress.value).toInt(),
                    ),
                    BlendMode.Modulate,
                ),
                alpha = cloud58Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵(Ellipse 57.png, X=-25, Y=500, W=225, H=191) — 原色显示,仅位置 + 透明度随机 (§29/§30)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_57),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (-25f + cloud57Dx).dp, y = (500f + cloud57Dy).dp)
                    .size(width = 225.dp, height = 191.dp),
                alpha = cloud57Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 云朵(Ellipse 5.png, X=187, Y=308, W=225, H=191) — 原色显示,位置 + 透明度随机(§33)
            Image(
                painter = painterResource(R.drawable.img_shilian3_cloud_5),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (187f + cloud5Dx).dp, y = (308f + cloud5Dy).dp)
                    .size(width = 225.dp, height = 191.dp),
                alpha = cloud5Alpha,
                contentScale = ContentScale.FillBounds,
            )

            // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 在云朵下层
            Image(
                painter = painterResource(R.drawable.img_shilian2_recovered_8),
                contentDescription = "熊猫",
                modifier = Modifier
                    .offset(x = 118.dp, y = 405.dp)
                    .size(width = 181.dp, height = 96.dp),
                contentScale = ContentScale.FillBounds,
            )

            // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=43, Y=390, W=51, H=91)
            Box(
                modifier = Modifier
                    .offset(x = 43.dp, y = 390.dp)
                    .size(width = 51.dp, height = 91.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签3",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "万\n象\n谱",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                    modifier = Modifier
                        .offset(x = 20.5.dp, y = 25.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6, 颜色 #385816, YaHei)— 相对位置参照后山2 标签3
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                    modifier = Modifier
                        .offset(x = 22.dp, y = 12.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=105, Y=295, W=30, H=53.5)
            Box(
                modifier = Modifier
                    .offset(x = 105.dp, y = 295.dp)
                    .size(width = 30.dp, height = 53.5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签4",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "寻\n径\n迷\n踪\n步",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 14.dp)
                        .size(width = 12.dp, height = 60.dp),
                )
                // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置参照后山2 标签4
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                    modifier = Modifier
                        .offset(x = 13.5.dp, y = 7.dp)
                        .size(width = 10.dp, height = 14.dp),
                )
            }

            // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=124, Y=521, W=96, H=170)
            Box(
                modifier = Modifier
                    .offset(x = 124.dp, y = 521.dp)
                    .size(width = 96.dp, height = 170.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // "拆招心法" 竖排文字(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14, 黑色, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "拆\n招\n心\n法",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 48.dp)
                        .size(width = 14.dp, height = 80.dp),
                )
                // "炼" 文字(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12, 颜色 #385816, YaHei)— 相对位置参照后山2 标签2
                Text(
                    text = "炼",
                    color = Color(0xFF385816),
                    style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    modifier = Modifier
                        .offset(x = 43.dp, y = 25.dp)
                        .size(width = 12.dp, height = 16.dp),
                )
            }

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 点击回到后山2 页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = onBack),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}

/**
 * 云朵随机飘动 helper:3 个独立 LaunchedEffect 协程并行,
 * 每次随机选目标值 + 随机 delay,产生 X/Y/Alpha 三维自然飘动。
 *
 * 复制自 Houshan1Screen.kt §13 — 因为 helper 是 private fun,不能跨文件共享。
 * TODO:后续可抽出到 ui/util/CloudFloat.kt 共享(同 §28 设计考虑)。
 */
@Composable
private fun rememberCloudFloat(
    maxX: Float = 100f,
    maxY: Float = 15f,
    alphaMin: Float = 0.5f,
    alphaMax: Float = 1f,
    xDuration: IntRange = 1500..3000,
    xDelay: LongRange = 500L..1500L,
): Triple<Float, Float, Float> {
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            x.animateTo(
                targetValue = Random.nextFloat() * 2f * maxX - maxX,
                animationSpec = tween(
                    durationMillis = Random.nextInt(xDuration.first, xDuration.last + 1),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(xDelay.first, xDelay.last + 1))
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            y.animateTo(
                targetValue = Random.nextFloat() * 2f * maxY - maxY,
                animationSpec = tween(
                    durationMillis = Random.nextInt(1000, 2000),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(300, 800))
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            alpha.animateTo(
                targetValue = alphaMin + Random.nextFloat() * (alphaMax - alphaMin),
                animationSpec = tween(
                    durationMillis = Random.nextInt(1500, 3000),
                    easing = LinearEasing,
                ),
            )
            delay(Random.nextLong(500, 1200))
        }
    }
    return Triple(x.value, y.value, alpha.value)
}
