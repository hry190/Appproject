package com.jueqiao.jianghu.ui.screens.gunlun3

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 滚轮3 页 — 滚轮2 → 点击"熊猫"贴图跳转目标。
 *
 * 重构后:用 StandardGunlunScaffold 复用背景 + 熊猫(可点击) + 返回按钮。
 * 本文件只剩页独有的元素:10 张轮换位置的书 + Rectangle 6 气泡 + 文本。
 *
 * 10 本书的链式轮换 + Rectangle 6 气泡(删除了滚轮2 的"介绍")。
 */
@Composable
fun Gunlun3Screen(
    onBack: () -> Unit = {},
    onOpenGunlun4: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
        onPandaClick = onOpenGunlun4,
    ) {
        // "秘籍" 图像(占原"未解锁秘籍1"槽位,X=8, Y=205, W=96, H=96)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2_recovered_1),
            contentDescription = "秘籍",
            modifier = Modifier
                .offset(x = 8.dp, y = 205.dp)
                .size(width = 96.dp, height = 96.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍1" 旋转图像(占原"未解锁秘籍2"槽位,X=-31, Y=130.29, rotation -11.03°, W=66.29, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_1),
            contentDescription = "未解锁秘籍1",
            modifier = Modifier
                .offset(x = (-31).dp, y = 130.29.dp)
                .size(width = 66.29.dp, height = 69.dp)
                .rotate(-11.03f),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍2" 图像(占原"未解锁秘籍3"槽位,X=50, Y=87, W=64, H=66.6)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_2),
            contentDescription = "未解锁秘籍2",
            modifier = Modifier
                .offset(x = 50.dp, y = 87.dp)
                .size(width = 64.dp, height = 66.6.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍3" 图像(占原"未解锁秘籍4"槽位,X=123.4, Y=66, W=55.6, H=57.9)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_3),
            contentDescription = "未解锁秘籍3",
            modifier = Modifier
                .offset(x = 123.4.dp, y = 66.dp)
                .size(width = 55.6.dp, height = 57.9.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍4" 图像(占原"未解锁秘籍5"槽位,X=198, Y=69, W=42.62, H=38.41)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_4),
            contentDescription = "未解锁秘籍4",
            modifier = Modifier
                .offset(x = 198.dp, y = 69.dp)
                .size(width = 42.62.dp, height = 38.41.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍5" 图像(占原"未解锁秘籍6"槽位,X=258.15, Y=68.5, W=54.78, H=51.85)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_5),
            contentDescription = "未解锁秘籍5",
            modifier = Modifier
                .offset(x = 258.15.dp, y = 68.5.dp)
                .size(width = 54.78.dp, height = 51.85.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍6" 图像(占原"未解锁秘籍7"槽位,X=311.04, Y=92, W=58, H=57.5)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_6),
            contentDescription = "未解锁秘籍6",
            modifier = Modifier
                .offset(x = 311.04.dp, y = 92.dp)
                .size(width = 58.dp, height = 57.5.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍7" 图像(占原"未解锁秘籍8"槽位,X=357, Y=136.32, W=66, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_7),
            contentDescription = "未解锁秘籍7",
            modifier = Modifier
                .offset(x = 357.dp, y = 136.32.dp)
                .size(width = 66.dp, height = 69.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍8" 图像(占原"未解锁秘籍9"槽位,X=321, Y=205, W=93, H=92)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_8),
            contentDescription = "未解锁秘籍8",
            modifier = Modifier
                .offset(x = 321.dp, y = 205.dp)
                .size(width = 93.dp, height = 92.dp),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍9" 图像(完成循环轮换,占"秘籍"原槽位,X=135, Y=221, W=155, H=147)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_9),
            contentDescription = "未解锁秘籍9",
            modifier = Modifier
                .offset(x = 135.dp, y = 221.dp)
                .size(width = 155.dp, height = 147.dp),
            contentScale = ContentScale.FillBounds,
        )

        // Rectangle 6.png(X=20, Y=364, W=148, H=84)
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 364.dp)
                .size(width = 148.dp, height = 84.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gunlun3_rect6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡上的文本(14sp, 黑色)
            Text(
                text = "你还未习得这本秘籍哦，需前往后山试炼方能参悟。",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxSize(),
            )
        }
    }
}
