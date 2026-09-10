package com.jueqiao.jianghu.ui.screens.gunlun6

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
import com.jueqiao.jianghu.ui.components.WideHexagonShape
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮6 页 — 滚轮5 → 点击气泡跳转目标。
 *
 * 与滚轮5 的差异:
 *   - 删除 Rectangle16.png 气泡(L175-197 in Gunlun5)及其文本 "山水作伴,秘籍环绕..."
 *   - 保留:10 本书(1 秘籍 + 9 已解锁秘籍,部分素材专属)+ 介绍六边形
 *
 * 熊猫在滚轮6 中**可点击**,点击跳转到滚轮7。
 * 复制自 Gunlun5Screen.kt,删除 Rectangle16 气泡及文本。
 */
@Composable
fun Gunlun6Screen(
    onBack: () -> Unit = {},
    onOpenGunlun7: () -> Unit = {},
    onOpenVolume1: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
        onPandaClick = onOpenGunlun7,
    ) {
        // "秘籍" 图像(未标题-2-恢复的 1.png,X=135, Y=221, W=155, H=147)
        // ⚠ 位于"介绍"左侧(X=135 < X=283)— 用户提示这是"已解锁9"。
        // 保险方案:与下面 X=321 的"已解锁秘籍9"Image 一起 clickable → Volume1,避免歧义。
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2_recovered_1),
            contentDescription = "秘籍",
            modifier = Modifier
                .offset(x = 135.dp, y = 221.dp)
                .size(width = 155.dp, height = 147.dp)
                .clickable(onClick = onOpenVolume1),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍1 图像(未标题1.png,X=8, Y=205, W=96, H=96 — 素材换成"已解锁1" 未标题-2 30.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_30),
            contentDescription = "已解锁秘籍1",
            modifier = Modifier
                .offset(x = 8.dp, y = 205.dp)
                .size(width = 96.dp, height = 96.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍2 旋转图像(未标题-2 32.png,X=-21, Y=130.29, rotation -11.03° 顺时针, W=66.29, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_32),
            contentDescription = "已解锁秘籍2",
            modifier = Modifier
                .offset(x = (-21).dp, y = 130.29.dp)
                .size(width = 66.29.dp, height = 69.dp)
                .rotate(-11.03f),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍3 图像(未标题-232.png,X=50, Y=87, W=64, H=66.6)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_232),
            contentDescription = "已解锁秘籍3",
            modifier = Modifier
                .offset(x = 50.dp, y = 87.dp)
                .size(width = 64.dp, height = 66.6.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍4 图像(未标题-2 28.png,X=123.4, Y=66, W=55.6, H=57.9)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_28),
            contentDescription = "已解锁秘籍4",
            modifier = Modifier
                .offset(x = 123.4.dp, y = 66.dp)
                .size(width = 55.6.dp, height = 57.9.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍5 图像(未标题-2 24.png,X=198, Y=69, W=42.62, H=38.41)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_24),
            contentDescription = "已解锁秘籍5",
            modifier = Modifier
                .offset(x = 198.dp, y = 69.dp)
                .size(width = 42.62.dp, height = 38.41.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍6 图像(未标题-2-恢复的 14.png,X=258.15, Y=68.5, W=54.78, H=51.85)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_recovered_14),
            contentDescription = "已解锁秘籍6",
            modifier = Modifier
                .offset(x = 258.15.dp, y = 68.5.dp)
                .size(width = 54.78.dp, height = 51.85.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍7 图像(未标题-7.png,X=311.04, Y=92, W=58, H=57.5)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_31),
            contentDescription = "已解锁秘籍7",
            modifier = Modifier
                .offset(x = 311.04.dp, y = 92.dp)
                .size(width = 58.dp, height = 57.5.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍8 图像(未标题-8.png,X=357, Y=136.32, W=66, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_26),
            contentDescription = "已解锁秘籍8",
            modifier = Modifier
                .offset(x = 357.dp, y = 136.32.dp)
                .size(width = 66.dp, height = 69.dp),
            contentScale = ContentScale.FillBounds,
        )

        // 已解锁秘籍9 图像(未标题-9.png,X=321, Y=205, W=93, H=92)— 静态装饰,不可点击。
        Image(
            painter = painterResource(R.drawable.img_gunlun5_untitled_2_33),
            contentDescription = "已解锁秘籍9",
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
