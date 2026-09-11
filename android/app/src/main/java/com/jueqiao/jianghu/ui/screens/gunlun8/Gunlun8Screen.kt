package com.jueqiao.jianghu.ui.screens.gunlun8

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.components.WideHexagonShape
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮8 页 — 滚轮7 → 点击"熊猫"贴图跳转目标。
 *
 * 重构后:用 StandardGunlunScaffold 复用背景 + 熊猫(可点击) + 返回按钮。
 * 本文件只剩页独有的元素:10 本书 + 介绍。
 *
 * 与滚轮7 的差异:
 *   - "秘籍"图像换成 未标题-2 33.png
 * 熊猫在滚轮8 中**可点击**,点击跳转到滚轮9。
 * 复制自 Gunlun7Screen.kt,替换秘籍图像。
 */
@Composable
fun Gunlun8Screen(
    onBack: () -> Unit = {},
    onOpenGunlun9: () -> Unit = {},
    onOpenVolume4Part1: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
        onPandaClick = onOpenGunlun9,
    ) {
        // "秘籍" 图像(占原"已解锁秘籍1"槽位,X=8, Y=205, W=96, H=96 — 素材换了 未标题-2 33.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_33),
            contentDescription = "秘籍",
            modifier = Modifier
                .offset(x = 8.dp, y = 205.dp)
                .size(width = 96.dp, height = 96.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍1" 旋转图像(占原"已解锁秘籍2"槽位,X=-21, Y=130.29, rotation -11.03°, W=66.29, H=69 — 素材换了 未标题-2-恢复的 1.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_recovered_1),
            contentDescription = "已解锁秘籍1",
            modifier = Modifier
                .offset(x = (-21).dp, y = 130.29.dp)
                .size(width = 66.29.dp, height = 69.dp)
                .rotate(-11.03f),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍2" 图像(占原"已解锁秘籍3"槽位,X=50, Y=87, W=64, H=66.6 — 素材换了 未标题-2 44.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_44),
            contentDescription = "已解锁秘籍2",
            modifier = Modifier
                .offset(x = 50.dp, y = 87.dp)
                .size(width = 64.dp, height = 66.6.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍3" 图像(占原"已解锁秘籍4"槽位,X=123.4, Y=66, W=55.6, H=57.9 — 素材换了 未标题-2 38.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_38),
            contentDescription = "已解锁秘籍3",
            modifier = Modifier
                .offset(x = 123.4.dp, y = 66.dp)
                .size(width = 55.6.dp, height = 57.9.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍4" 图像(占原"已解锁秘籍5"槽位,X=198, Y=69, W=42.62, H=38.41 — 素材换了 未标题-232.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_232),
            contentDescription = "已解锁秘籍4",
            modifier = Modifier
                .offset(x = 198.dp, y = 69.dp)
                .size(width = 42.62.dp, height = 38.41.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍5" 图像(占原"已解锁秘籍6"槽位,X=258.15, Y=68.5, W=54.78, H=51.85 — 素材换了 未标题-2 28.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_28),
            contentDescription = "已解锁秘籍5",
            modifier = Modifier
                .offset(x = 258.15.dp, y = 68.5.dp)
                .size(width = 54.78.dp, height = 51.85.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍6" 图像(占原"已解锁秘籍7"槽位,X=311.04, Y=92, W=58, H=57.5 — 素材换了 未标题-2 24.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_24),
            contentDescription = "已解锁秘籍6",
            modifier = Modifier
                .offset(x = 311.04.dp, y = 92.dp)
                .size(width = 58.dp, height = 57.5.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍7" 图像(占原"已解锁秘籍8"槽位,X=357, Y=136.32, W=66, H=69 — 素材换了 未标题-2-恢复的 14.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_recovered_14),
            contentDescription = "已解锁秘籍7",
            modifier = Modifier
                .offset(x = 357.dp, y = 136.32.dp)
                .size(width = 66.dp, height = 69.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍8" 图像(占原"已解锁秘籍9"槽位,X=321, Y=205, W=93, H=92 — 素材换了 未标题-2 41.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_41),
            contentDescription = "已解锁秘籍8",
            modifier = Modifier
                .offset(x = 321.dp, y = 205.dp)
                .size(width = 93.dp, height = 92.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "已解锁秘籍9" 图像(完成循环轮换,占"秘籍"原槽位,X=135, Y=221, W=155, H=147 — 素材换了 未标题-2 26.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun8_untitled_2_26),
            contentDescription = "已解锁秘籍9",
            modifier = Modifier
                .offset(x = 135.dp, y = 221.dp)
                .size(width = 155.dp, height = 147.dp)
                .clickable(onClick = onOpenVolume4Part1),
            contentScale = ContentScale.FillBounds,
        )

        // "介绍" 旋转背景填充(X=283, Y=254, rotation 0.93°, W=23, H=115.5)
        //   填充色:#2A350E,Opacity 80%
        //   裁剪为六边形显示
        Box(
            modifier = Modifier
                .offset(x = 283.dp, y = 254.dp)
                .size(width = 23.dp, height = 115.5.dp)
                .clip(WideHexagonShape())
                .background(Color(0xCC2A350E))
                .rotate(0.93f),
        ) {
            // "寻\n径\n迷\n踪\n步" 竖排文字(W=16, H=96,字号 14,颜色 #DDC686,YaHei)
            Text(
                text = "寻\n径\n迷\n踪\n步",
                color = Color(0xFFDDC686),
                style = TextStyle(
                    fontFamily = YaHei,
                    fontSize = 14.sp,
                    lineHeight = 18.69.sp,
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 16.dp, height = 96.dp),
            )
        }
    }
}