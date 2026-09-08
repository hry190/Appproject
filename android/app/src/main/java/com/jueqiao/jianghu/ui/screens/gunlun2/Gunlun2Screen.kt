package com.jueqiao.jianghu.ui.screens.gunlun2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.WideHexagonShape
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮2 页 — 滚轮1 → "气泡"点击跳转目标。
 *
 * 重构后:用 StandardGunlunScaffold 复用背景 + 熊猫(可点击) + 返回按钮。
 * 本文件只剩页独有的元素:9 本书 + "介绍" 六边形标签 + 文本。
 *
 * 与 Gunlun1 相比:删除了"气泡"、"后山"按钮、"修炼"按钮。
 */
@Composable
fun Gunlun2Screen(
    onBack: () -> Unit = {},
    onOpenGunlun3: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
        onPandaClick = onOpenGunlun3,
    ) {
        // "秘籍" 图像(未标题-2-恢复的 1.png,X=135, Y=221, W=155, H=147)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2_recovered_1),
            contentDescription = "秘籍",
            modifier = Modifier
                .offset(x = 135.dp, y = 221.dp)
                .size(width = 155.dp, height = 147.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍1 图像(未标题1.png,X=8, Y=205, W=96, H=96 — 素材换成"已解锁1" 未标题-2 30.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2_30),
            contentDescription = "未解锁秘籍1",
            modifier = Modifier
                .offset(x = 8.dp, y = 205.dp)
                .size(width = 96.dp, height = 96.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍2 旋转图像(未标题-2.png,X=-21, Y=130.29, rotation -11.03° 顺时针, W=66.29, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2),
            contentDescription = "未解锁秘籍2",
            modifier = Modifier
                .offset(x = (-21).dp, y = 130.29.dp)
                .size(width = 66.29.dp, height = 69.dp)
                .rotate(-11.03f),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍3 图像(未标题-3.png,X=50, Y=87, W=64, H=66.6)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_3),
            contentDescription = "未解锁秘籍3",
            modifier = Modifier
                .offset(x = 50.dp, y = 87.dp)
                .size(width = 64.dp, height = 66.6.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍4 图像(未标题-4.png,X=123.4, Y=66, W=55.6, H=57.9)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_4),
            contentDescription = "未解锁秘籍4",
            modifier = Modifier
                .offset(x = 123.4.dp, y = 66.dp)
                .size(width = 55.6.dp, height = 57.9.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍5 图像(未标题-5.png,X=198, Y=69, W=42.62, H=38.41)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_5),
            contentDescription = "未解锁秘籍5",
            modifier = Modifier
                .offset(x = 198.dp, y = 69.dp)
                .size(width = 42.62.dp, height = 38.41.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍6 图像(未标题-6.png,X=258.15, Y=68.5, W=54.78, H=51.85)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_6),
            contentDescription = "未解锁秘籍6",
            modifier = Modifier
                .offset(x = 258.15.dp, y = 68.5.dp)
                .size(width = 54.78.dp, height = 51.85.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍7 图像(未标题-7.png,X=311.04, Y=92, W=58, H=57.5)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_7),
            contentDescription = "未解锁秘籍7",
            modifier = Modifier
                .offset(x = 311.04.dp, y = 92.dp)
                .size(width = 58.dp, height = 57.5.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍8 图像(未标题-8.png,X=357, Y=136.32, W=66, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_8),
            contentDescription = "未解锁秘籍8",
            modifier = Modifier
                .offset(x = 357.dp, y = 136.32.dp)
                .size(width = 66.dp, height = 69.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 未解锁秘籍9 图像(未标题-9.png,X=321, Y=205, W=93, H=92)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_9),
            contentDescription = "未解锁秘籍9",
            modifier = Modifier
                .offset(x = 321.dp, y = 205.dp)
                .size(width = 93.dp, height = 92.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "介绍" 旋转背景填充(X=283, Y=254, rotation 0.93°, W=23, H=115.5)
        //   外观:Opacity 100%, Corner radius 0
        //   填充色:#DDC686,Opacity 100%
        //   裁剪为六边形显示
        Box(
            modifier = Modifier
                .offset(x = 283.dp, y = 254.dp)
                .size(width = 23.dp, height = 115.5.dp)
                .clip(WideHexagonShape())
                .background(Color(0xFFDDC686))
                .rotate(0.93f),
        ) {
            // "识\n机\n真\n决" 竖排文字(W=16, H=76,字号 14,lineHeight 133.5%=18.69sp,黑色,YaHei)
            Text(
                text = "识\n机\n真\n决",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 14.sp,
                    lineHeight = 18.69.sp,
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 16.dp, height = 76.dp),
            )
        }
    }
}
