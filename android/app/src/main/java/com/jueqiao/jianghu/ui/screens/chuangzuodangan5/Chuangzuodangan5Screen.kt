package com.jueqiao.jianghu.ui.screens.chuangzuodangan5

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 创作档案5页面 — 从 Chuangzuodangan4Screen 复制,但移除莲花(image 52)、飘雾(image 61)、雾气(Rectangle 245 + 文本)。
 */
@Composable
fun Chuangzuodangan5Screen(
    onBack: () -> Unit = {},
    onCreateWork: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致
    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景(D:\图\创作档案5.png)
        Image(
            painter = painterResource(R.drawable.img_chuangzuodangan5_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 返回按钮(从 ChatResultScreen 复用:X=20, Y=41, 点击区 32×32)
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 41.dp)
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_gongfang_return),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // 未标题-2 23.png(教练辅助装饰)
            Image(
                painter = painterResource(R.drawable.img_gongfang_23),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 57.dp, y = 29.dp)
                    .size(width = 160.dp, height = 58.dp),
                contentScale = ContentScale.Fit,
            )

            // 未标题-2 24.png(创作档案装饰) — 此页面不做可点击,避免自跳死循环
            Image(
                painter = painterResource(R.drawable.img_gongfang_24),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 265.dp, y = 35.dp)
                    .size(width = 127.dp, height = 46.dp),
                contentScale = ContentScale.Fit,
            )

            // "教练辅助" 标签
            Text(
                text = "教练辅助",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 93.dp, y = 46.dp)
                    .size(width = 71.dp, height = 18.dp),
            )

            // "创作档案" 标签 — 此页面不做可点击,避免自跳死循环
            Text(
                text = "创作档案",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 287.dp, y = 46.dp)
                    .size(width = 76.dp, height = 25.dp),
            )

            // 修改版本记录.png(X=226, Y=384.5, W=120.47, H=126.12)
// 弧形文字:6 字沿弧线排列,首字 51° 顺时针,每字向逆时针递减 10.2°,末字回 0°(整体 -51°)
val arcText = "修改版本记录"
val arcN = arcText.length
for (i in 0 until arcN) {
    val t = i.toFloat() / (arcN - 1).toFloat()
    val arcAngleRad = (200.0 - 90.0 * t) * PI / 180.0
    val charX = (276.0 + 60.235 + 60.5 * cos(arcAngleRad)).toFloat()
    val charY = (325.5 + 69.4 + 60.5 * sin(arcAngleRad)).toFloat()
    // 单字旋转:不跟弧度,首字 51° CW,末字 0°,每字向逆时针递减 51°/5 = 10.2°
    val rot = 51f * (arcN - 1 - i) / (arcN - 1).toFloat()

    Text(
        text = arcText[i].toString(),
        color = Color(0xFF437349),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
        ),
        modifier = Modifier
            .offset(x = charX.dp, y = charY.dp)
            .rotate(rot),
    )
}

// 原创记录.png(X=18, Y=333, W=116.5, H=94.11)
// 圆心在"原"上方 50 单位;"原"保持在原位,其余三字绕圆心排布
// 旋转:首字 0°,末字 -45°,每字向逆时针递减 15°
// 颜色:从左到右 浅黄绿(#B8D878) → 深草绿(#5A8A3A),每字内水平渐变
val chuangyuanText = "原创记录"
val chuangyuanN = chuangyuanText.length
val chuangyuanBaseX = 48f + 12f          // 55 — "原" 的 X
val chuangyuanBaseY = 303f + 47.055f     // 350.055 — "原" 的 Y
val chuangyuanCenterX = chuangyuanBaseX  // 55 — 圆心 X(直接在"原"上方)
val chuangyuanCenterY = chuangyuanBaseY - 50f  // 300.055 — 圆心 Y
val chuangyuanRadius = 50f               // 半径(正好让"原"在弧底)
// 4 字角度分布(math 度):90°, 70°, 50°, 25°(从"原"顺时针往上排)
val chuangyuanAnglesDeg = listOf(90f, 70f, 50f, 25f)
val chuangyuanFirstRot = 0f
val chuangyuanLastRot = -45f
val chuangyuanGradientStart = Color(0xFFB8D878)  // 浅黄绿 light yellow-green
val chuangyuanGradientEnd   = Color(0xFF5A8A3A)  // 深草绿 dark grass green
for (i in 0 until chuangyuanN) {
    val t = i.toFloat() / (chuangyuanN - 1).toFloat()
    val arcAngleRad = chuangyuanAnglesDeg[i].toDouble() * PI / 180.0
    val charX = (chuangyuanCenterX + chuangyuanRadius * cos(arcAngleRad)).toFloat()
    val charY = (chuangyuanCenterY + chuangyuanRadius * sin(arcAngleRad)).toFloat()
    val rot = chuangyuanFirstRot + (chuangyuanLastRot - chuangyuanFirstRot) * t
    // 每字内部水平渐变:取该字在整体渐变中的"切片"(左 t 到右 t)
    val rightT = (i + 1).toFloat() / (chuangyuanN - 1).toFloat()
    fun lerpColor(start: Color, end: Color, tt: Float) = Color(
        red   = start.red   + (end.red   - start.red)   * tt,
        green = start.green + (end.green - start.green) * tt,
        blue  = start.blue  + (end.blue  - start.blue)  * tt,
        alpha = 1f,
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(
            lerpColor(chuangyuanGradientStart, chuangyuanGradientEnd, t),
            lerpColor(chuangyuanGradientStart, chuangyuanGradientEnd, rightT),
        ),
    )

    Text(
        text = chuangyuanText[i].toString(),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
            brush = brush,
        ),
        modifier = Modifier
            .offset(x = charX.dp, y = charY.dp)
            .rotate(rot),
    )
}

// 选择作品查看.png(X=-2, Y=143, W=103, H=101)
// 6 字绕圆心排布,圆心在"选"下方 50 单位,首字 25° 顺时针,末字 80° 顺时针
val xuanzeText = "选择作品查看"
val xuanzeN = xuanzeText.length
val xuanzeBaseX = 45f                // "选" 的 X
val xuanzeBaseY = 137f               // "选" 的 Y
val xuanzeCenterX = xuanzeBaseX      // -2 — 圆心 X(直接在"选"正下方)
val xuanzeCenterY = xuanzeBaseY + 50f  // 193 — 圆心 Y
val xuanzeRadius = 50f               // 半径(让"选"在弧顶)
val xuanzeFirstRot = 25f             // 首字 25° CW
val xuanzeLastRot = 80f              // 末字 80° CW
val xuanzeColor = Color(0xFF62704E)
for (i in 0 until xuanzeN) {
    val t = i.toFloat() / (xuanzeN - 1).toFloat()
    // 弧度角:从 -90°(正上方,即"选"位置)扫到 0°(正右方),90° 总扫角
    val arcAngleDeg = -90f + 90f * t
    val arcAngleRad = arcAngleDeg.toDouble() * PI / 180.0
    val charX = (xuanzeCenterX + xuanzeRadius * cos(arcAngleRad)).toFloat()
    val charY = (xuanzeCenterY + xuanzeRadius * sin(arcAngleRad)).toFloat()
    val rot = xuanzeFirstRot + (xuanzeLastRot - xuanzeFirstRot) * t

    Text(
        text = xuanzeText[i].toString(),
        color = xuanzeColor,
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 12.sp,
        ),
        modifier = Modifier
            .offset(x = charX.dp, y = charY.dp)
            .rotate(rot),
    )
}

// AI教练辅助记录.png(X=3, Y=666, W=126.5, H=162.3)
// 8 字绕圆心排布,圆心在"助"上方 70 单位;首字 A/I 51° CW,"助" 0° 锚点,末字 -20°
// 颜色:前 3 字墨绿(#2E7D32),后 5 字浅绿(#81C784)
val aiText = "AI教练辅助记录"
val aiN = aiText.length
val aiBaseX = 114f                  // "助" 的 X
val aiBaseY = 735f                 // "助" 的 Y
val aiCenterX = aiBaseX            // 75 — 圆心 X(直接在"助"正上方)
val aiCenterY = aiBaseY - 70f      // 680 — 圆心 Y("助"上方 70)
val aiRadius = 70f                 // 半径(让"助"在弧底)
// 8 字角度分布:从 A(155° 左侧)→ 助(90° 底)→ 录(60° 右侧)
val aiAnglesDeg = listOf(155f, 145f, 135f, 120f, 105f, 90f, 75f, 60f)
// 旋转:前两字同 75°(原 51°),中间 4 字线性到 0°,后两字线性到 -20°
val aiRotations = listOf(75f, 75f, 56.25f, 37.5f, 18.75f, 0f, -10f, -20f)
val aiGradientStart = Color(0xFF4A5D3A)  // 墨绿 dark olive green
val aiGradientEnd = Color(0xFF3D8A4A)    // 翠绿 emerald green
for (i in 0 until aiN) {
    val arcAngleRad = aiAnglesDeg[i].toDouble() * PI / 180.0
    val charX = (aiCenterX + aiRadius * cos(arcAngleRad)).toFloat()
    val charY = (aiCenterY + aiRadius * sin(arcAngleRad)).toFloat()
    val rot = aiRotations[i]
    // 每字内部水平渐变:取该字在整体渐变中的"切片"(左 t 到右 t)
    val leftT = i.toFloat() / (aiN - 1).toFloat()
    val rightT = (i + 1).toFloat() / (aiN - 1).toFloat()
    fun lerpColor(start: Color, end: Color, t: Float) = Color(
        red   = start.red   + (end.red   - start.red)   * t,
        green = start.green + (end.green - start.green) * t,
        blue  = start.blue  + (end.blue  - start.blue)  * t,
        alpha = 1f,
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(lerpColor(aiGradientStart, aiGradientEnd, leftT),
                        lerpColor(aiGradientStart, aiGradientEnd, rightT)),
    )

    Text(
        text = aiText[i].toString(),
        style = TextStyle(
            fontFamily = YaHei,
            fontSize = 16.sp,
            brush = brush,
        ),
        modifier = Modifier
            .offset(x = charX.dp, y = charY.dp)
            .rotate(rot),
    )
}

// image 52.png(X=278, Y=365, W=124, H=100)— 莲花,逆时针旋转 10°
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan5_image52),
    contentDescription = null,
    modifier = Modifier
        .offset(x = 278.dp, y = 365.dp)
        .size(width = 124.dp, height = 100.dp)
        .rotate(-10f),
    contentScale = ContentScale.Fit,
)

// Rectangle 25.png — 雾气(D:\图\Rectangle 25.png)
// 与其他页一致模式:fillMaxWidth 容器 + Image 用 ContentScale.FillWidth
//   本页 Y = 171(其他页多在 55,本屏需要下移)
Box(
    modifier = Modifier
        .fillMaxWidth()
        .offset(y = 171.dp),
) {
    Image(
        painter = painterResource(R.drawable.img_chuangzuodangan5_rect25),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
    )
}

// image 59.png(X=124, Y=369, W=276, H=102)
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan5_image59),
    contentDescription = null,
    modifier = Modifier
        .offset(x = 124.dp, y = 369.dp)
        .size(width = 276.dp, height = 102.dp),
    contentScale = ContentScale.Fit,
)

// image 62.png(X=204, Y=687, W=204, H=219)— 熊猫图
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan4_image62),
    contentDescription = null,
    modifier = Modifier
        .offset(x = 174.dp, y = 646.dp)
        .size(width = 204.dp, height = 219.dp),
    contentScale = ContentScale.Fit,
)
        }
    }
}
