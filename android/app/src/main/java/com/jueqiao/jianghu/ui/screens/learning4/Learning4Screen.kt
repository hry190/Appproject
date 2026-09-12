package com.jueqiao.jianghu.ui.screens.learning4

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 学习4 页 — 学习2 → 点击"查看秘籍"标签跳转目标。
 *
 * 布局(从 LearningScreen.kt 复制,**不含**卷轴图像 + 新增内容图 + 熊猫):
 *   - 全屏背景图(Android Compact - 109.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)
 *   - 内容图像(未标题-2-恢复的 1.png,X=20, Y=178, W=372, H=352.8)
 *   - 熊猫图像(image 540.png,X=190, Y=544, W=200, H=280)
 *   - 标题"识机真决"(字号 32,黑色,X=142, Y=46, W=128, H=43)
 *   - 气泡(86.png,X=69, Y=536, W=187, H=85)+ 气泡文本(字号 14,黑色,X=87, Y=545, W=158, H=57,
 *     行间距 133.5%,2 行)
 *
 * 与学习1 的差别:**不含**中央卷轴(Group 281.png);改用内容图 + 熊猫填充中部。
 *
 * TODO: 学习1 / 学习4 顶部 2 元素(背景 + 返回按钮)100% 重复。
 * 已是 [docs/CODE-AUDIT-2026-09-10.md](../CODE-AUDIT-2026-09-10.md) §"Learning2/Learning3 share
 * byte-identical page chrome" 同类问题,出现第 3 个学习页(学习5?)时再统一抽。当前保留重复。
 */
@Composable
fun Learning4Screen(
    onBack: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(Android Compact - 109.png,与学习1 一致)
        Image(
            painter = painterResource(R.drawable.img_houshan_bg),
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
            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,与学习1 一致)— 点击回到学习2
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

            // 标题"识机真诀"(字号 32,黑色,X=142, Y=46, W=128, H=43)。
            // 写在背景后,内容图、熊猫之前 → 视觉上不重叠。
            Text(
                text = "识机真诀",
                color = Color.Black,
                fontSize = 32.sp,
                fontFamily = YaHei,
                modifier = Modifier
                    .offset(x = 142.dp, y = 46.dp)
                    .size(width = 128.dp, height = 43.dp),
            )

            // 内容图像(未标题-2-恢复的 1.png,X=20, Y=178, W=372, H=352.8)。
            // 占满中部,左对齐(标准手机宽 ~412dp,留 20dp 右边距)。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 178.dp)
                    .size(width = 372.dp, height = 352.8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning4_untitled_2_recovered_1),
                    contentDescription = "内容图像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 熊猫图像(image 540.png,X=190, Y=544, W=200, H=280)。
            // Y=544–824 落在内容图 Y=178–530.8 之下,覆盖屏幕下半部。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 190.dp, y = 544.dp)
                    .size(width = 200.dp, height = 326.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning4_image_540),
                    contentDescription = "熊猫",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 气泡(86.png,X=69, Y=536, W=187, H=85)。
            // Y=536–621 与熊猫 Y=544–883 在 X=69–256 范围 Y=544–621 重叠(77dp),
            // 写在熊猫之后 → 视觉上覆盖熊猫左上。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 69.dp, y = 536.dp)
                    .size(width = 187.dp, height = 85.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning4_bubble_86),
                    contentDescription = "气泡",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 气泡文本"太棒啦!成功获得秘籍《识机真诀》,快来一起学习里面的知识"
            //   (字号 14,黑色,X=87, Y=545, W=158, H=57,行间距 133.5%,2 行)。
            // X=87–245 落在气泡 X=69–256 范围内 → 居中;
            // Y=545–602 落在气泡 Y=536–621 范围内 → 垂直居中(气泡内留 19dp 上 + 19dp 下 padding)。
            Text(
                text = "太棒啦!成功获得秘籍《识机真诀》,快来一起学习里面的知识",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = YaHei,
                lineHeight = 14.sp * 1.335f,
                modifier = Modifier
                    .offset(x = 87.dp, y = 545.dp)
                    .size(width = 158.dp, height = 57.dp),
            )
        }
    }
}