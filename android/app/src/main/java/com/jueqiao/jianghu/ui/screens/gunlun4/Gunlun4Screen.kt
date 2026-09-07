package com.jueqiao.jianghu.ui.screens.gunlun4

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.StandardGunlunScaffold

/**
 * 滚轮4 页 — 滚轮3 → 点击"熊猫"贴图跳转目标。
 *
 * 重构后:用 StandardGunlunScaffold 复用背景 + 熊猫(可点击) + 返回按钮。
 * 本文件只剩页独有的元素:10 本书(部分用 Gunlun4 专属 drawable)。
 *
 * 与滚轮3 的差异:
 *   - 删除 Rectangle 6 气泡及其文本
 *   - 未解锁秘籍9 用 img_gunlun4_untitled_2_33(其他秘籍用 Gunlun2 共享资源)
 */
@Composable
fun Gunlun4Screen(
    onBack: () -> Unit = {},
    onOpenGunlun5: () -> Unit = {},
) {
    StandardGunlunScaffold(
        onBack = onBack,
        onPandaClick = onOpenGunlun5,
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

        // "未解锁秘籍1" 旋转图像(占原"未解锁秘籍2"槽位,X=-21, Y=130.29, rotation -11.03°, W=66.29, H=69)
        Image(
            painter = painterResource(R.drawable.img_gunlun2_untitled_1),
            contentDescription = "未解锁秘籍1",
            modifier = Modifier
                .offset(x = (-21).dp, y = 130.29.dp)
                .size(width = 66.29.dp, height = 69.dp)
                .rotate(-11.03f),
            contentScale = ContentScale.FillBounds,
        )

        // "未解锁秘籍2" 图像(占原"未解锁秘籍3"槽位,X=50, Y=87, W=64, H=66.6 — 素材换了 未标题-2 38.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun4_untitled_2_38),
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

        // "未解锁秘籍6" 图像(占原"未解锁秘籍7"槽位,X=311.04, Y=92, W=58, H=57.5 — 素材换了 未标题-2-恢复的 16.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun4_untitled_2_recovered_16),
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

        // "未解锁秘籍9" 图像(完成循环轮换,占"秘籍"原槽位,X=135, Y=221, W=155, H=147 — 素材换了 未标题-2 33.png)
        Image(
            painter = painterResource(R.drawable.img_gunlun4_untitled_2_33),
            contentDescription = "未解锁秘籍9",
            modifier = Modifier
                .offset(x = 135.dp, y = 221.dp)
                .size(width = 155.dp, height = 147.dp),
            contentScale = ContentScale.FillBounds,
        )
    }
}
