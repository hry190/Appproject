package com.jueqiao.jianghu.ui.screens.chuangzuodangan6

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
 * 创作档案6页面 — 从 Chuangzuodangan5Screen 复制,背景换为创作档案6.png。
 */
@Composable
fun Chuangzuodangan6Screen(
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
        // 全屏背景(D:\图\创作档案6.png)
        Image(
            painter = painterResource(R.drawable.img_chuangzuodangan6_bg),
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



// 选择作品查看.png(X=-2, Y=143, W=103, H=101) — 改为\"《熊猫AI绘画》\"(8 字)
// 8 字绕圆心排布,圆心在原\"选\"下方 50 单位,首字 25° 顺时针,末字 80° 顺时针(同原"选择作品查看")
val xuanzeText = "《熊猫AI绘画》"
val xuanzeN = xuanzeText.length
val xuanzeBaseX = 45f                // "选" 的 X
val xuanzeBaseY = 137f               // "选" 的 Y
val xuanzeCenterX = xuanzeBaseX      // -2 — 圆心 X(直接在"选"正下方)
val xuanzeCenterY = xuanzeBaseY + 50f  // 193 — 圆心 Y
val xuanzeRadius = 50f               // 半径(让"选"在弧顶)
val xuanzeFirstRot = 25f             // 首字 25° CW
val xuanzeLastRot = 80f              // 末字 80° CW
val xuanzeColor = Color.Black
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



// image 54.png(X=12, Y=274, W=132, H=106)— 莲花(D:\图\image 54.png)
// image 57.png(X=16, Y=243, W=282, H=173)— 飘雾(D:\图\image 57.png)
Image(
    painter = painterResource(R.drawable.img_chuangzuodangan6_image57),
    contentDescription = null,
    modifier = Modifier
        .offset(x = 10.dp, y = 203.dp)
        .size(width = 282.dp, height = 173.dp),
    contentScale = ContentScale.Fit,
)

Image(
    painter = painterResource(R.drawable.img_chuangzuodangan6_image54),
    contentDescription = null,
    modifier = Modifier
        .offset(x = -8.dp, y = 233.dp)
        .size(width = 132.dp, height = 106.dp),
    contentScale = ContentScale.Fit,
)

// Rectangle 24.png — 雾气(D:\图\Rectangle 24.png)
// 与其他页一致模式:fillMaxWidth 容器 + Image 用 ContentScale.FillWidth
//   本页 Y = 345.32, 文本绝对 Y = 480(内 Box offset 134.68)
Box(
    modifier = Modifier
        .fillMaxWidth()
        .offset(y = 345.32.dp),
) {
    Image(
        painter = painterResource(R.drawable.img_chuangzuodangan6_rect24),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
    )
    // 雾气上的文本(X居中, Y=480 绝对 = 345.32 + 134.68, 14sp, 黑色)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 134.68.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "时间:9点41分25秒\n内容:绘画一只在做手表的技巧熊猫",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
        )
    }
}

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
