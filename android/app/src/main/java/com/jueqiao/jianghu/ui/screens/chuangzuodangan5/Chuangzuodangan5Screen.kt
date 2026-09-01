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

// 选择作品查看.png(X=-2, Y=143, W=103, H=101) — 改为\"《熊猫AI绘画》\"(8 字)
// 位置 7 槽位(AI A-I 间距压缩 50%) + 旋转 5 槽位(《/熊、AI、画/》 各共享)
// 90° 弧 6 间隔均分(其中 AI 之间 0.5 间距)
val xuanzeText = "《熊猫AI绘画》"
val xuanzeN = xuanzeText.length
// 位置映射(8 字 → 7 槽位,AI A-I 间距压缩):
//   i=0 《→ 0, i=1 熊 → 1, i=2 猫 → 2, i=3 A → 3,
//   i=4 I → 3.5(A 与 I 半间距), i=5 绘 → 4, i=6 画 → 5, i=7 》→ 6
val xuanzePosSlots = floatArrayOf(0f, 1f, 2f, 3f, 3.5f, 4f, 5f, 6f)
val xuanzePosN = 7
// 旋转映射(8 字 → 5 槽位):《/熊、AI、画/》 各共享一个旋转角度
//   i=0 《 → rot 0(同 熊)
//   i=1 熊 → rot 0(同 《)
//   i=2 猫 → rot 1
//   i=3 A  → rot 2(同 I)
//   i=4 I  → rot 2(同 A)
//   i=5 绘 → rot 3
//   i=6 画 → rot 4(同 》)
//   i=7 》→ rot 4
val xuanzeRotSlots = intArrayOf(0, 0, 1, 2, 2, 3, 4, 4)
val xuanzeRotN = 5
val xuanzeBaseX = 45f                // "选" 的 X
val xuanzeBaseY = 137f               // "选" 的 Y
val xuanzeCenterX = xuanzeBaseX      // -2 — 圆心 X(直接在"选"正下方)
val xuanzeCenterY = xuanzeBaseY + 50f  // 193 — 圆心 Y
val xuanzeRadius = 50f               // 半径(让"选"在弧顶)
val xuanzeFirstRot = 25f             // 首字 25° CW
val xuanzeLastRot = 80f              // 末字 80° CW
val xuanzeColor = Color.Black
for (i in 0 until xuanzeN) {
    // 弧度角:从 -90°(正上方,即"选"位置)扫到 0°(正右方),90° 总扫角
    val tPos = xuanzePosSlots[i] / (xuanzePosN - 1).toFloat()
    val tRot = xuanzeRotSlots[i].toFloat() / (xuanzeRotN - 1).toFloat()
    val arcAngleDeg = -90f + 90f * tPos
    val arcAngleRad = arcAngleDeg.toDouble() * PI / 180.0
    val charX = (xuanzeCenterX + xuanzeRadius * cos(arcAngleRad)).toFloat()
    val charY = (xuanzeCenterY + xuanzeRadius * sin(arcAngleRad)).toFloat()
    val rot = xuanzeFirstRot + (xuanzeLastRot - xuanzeFirstRot) * tRot

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
        .offset(y = 111.dp),
) {
    Image(
        painter = painterResource(R.drawable.img_chuangzuodangan5_rect25),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
    )
    // 雾气上的文本(X居中, Y=274 绝对 = 171 + 103, 14sp, 黑色)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 103.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "草稿1草稿1草稿1草稿1\n草稿1草稿1草稿1",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier.size(width = 145.dp, height = 40.dp),
        )
    }
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
