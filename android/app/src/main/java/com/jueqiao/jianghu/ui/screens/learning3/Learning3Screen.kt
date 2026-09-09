package com.jueqiao.jianghu.ui.screens.learning3

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
 * 学习3 页 — 学习2 → 点击"尝试回答"标签跳转目标。
 *
 * 布局(与学习2 视觉一致的"学习页 chrome"+ 学习2 顶部文本,不含底部标签):
 *   - 全屏背景图(Android Compact - 109.png)
 *   - 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)
 *   - 中央卷轴(image 174.png,X 轴居中, Y=124, W=407, H=563)
 *   - 顶部文本(生活问题推荐:\n机器人为什么会认错物体?,字号 24,黑色,X=60, Y=190, W=292, H=64)
 *   - 答题文本(我的回答\n。。。。。。。。。。(4 行占位点),字号 20,黑色,X=70, Y=396, W=270, H=130)
 *   - 答题插图(image 217.png,X=262, Y=417, W=146, H=214)— **点击跳"待解锁"页**
 *   - 底部图像(未标题-2-恢复的 10.png,X=4, Y=614, W=404, H=237)
 *   - "回答正确"标签(Group 280.png,在底部图像上层,X=117, Y=698, W=199, H=86)
 *   - "回答正确"文本(字号 24,黑色,X=44, Y=644, W=96, H=32)— 在底部图像上层、Group 280 上方
 *
 * 与学习2 的差别:**不含**"尝试回答"和"查看秘籍"两个标签(学习2 进入学习3 的入口已消耗)。
 *
 * TODO: 学习1 / 学习2 / 学习3 顶部 4 个元素(背景 + 返回按钮 + 卷轴 + 顶部文本)100% 重复,
 * 已是 [docs/CODE-AUDIT-2026-09-09.md](../CODE-AUDIT-2026-09-09.md) §"Massive duplication
 * across gunlun1-12 screens" 同类问题。建议抽 `LearningPageChrome(content)` 帮助函数,
 * 出现第 4 个学习页(学习4?)时再统一抽。当前保留重复。
 */
@Composable
fun Learning3Screen(
    onBack: () -> Unit = {},
    onOpenPendingUnlock: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(Android Compact - 109.png)
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
            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18)— 点击回到学习2
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

            // 中央卷轴(image 174.png,X 轴居中, Y=124, W=407, H=563)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 124.dp)
                    .size(width = 407.dp, height = 563.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_image_174),
                    contentDescription = "中央卷轴",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 顶部文本(2 行,字号 24,黑色,X=60, Y=190, W=292, H=64)
            Text(
                text = "生活问题推荐:\n机器人为什么会认错物体?",
                color = Color.Black,
                fontSize = 24.sp,
                fontFamily = YaHei,
                modifier = Modifier
                    .offset(x = 60.dp, y = 190.dp)
                    .size(width = 292.dp, height = 64.dp),
            )

            // 答题文本(5 行,字号 20,黑色,X=70, Y=396, W=270, H=130)。
            // 第 1 行是标题"我的回答",后 4 行用占位点提示用户输入区。
            // H=130 与 fontSize=20 × 5 行 ≈ 130 紧贴,渲染溢出时建议扩到 H=140。
            Text(
                text = "我的回答\n。。。。。。。。。。。。\n。。。。。。。。。。。。\n。。。。。。。。。。。。\n。。。。。。。。",
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = YaHei,
                modifier = Modifier
                    .offset(x = 70.dp, y = 396.dp)
                    .size(width = 270.dp, height = 130.dp),
            )

            // 答题插图(image 217.png,X=262, Y=417, W=146, H=214)— **点击跳"待解锁"页**。
            // 位置在答题文本(X=70–340)右侧;Y=417–631 与底部图像(Y=614–851)在 Y=614–631 范围重叠 17dp。
            // 写在答题文本之后、底部图像之前 → 视觉上位于答题文本上方、底部图像下方。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 262.dp, y = 417.dp)
                    .size(width = 146.dp, height = 214.dp)
                    .clickable(onClick = onOpenPendingUnlock),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_image_217),
                    contentDescription = "答题插图",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 底部图像(未标题-2-恢复的 10.png,X=4, Y=614, W=404, H=237)。
            // Y=614 落在卷轴 Y=124–687 范围内(覆盖卷轴底部 73dp),H=237 延伸到 Y=851。
            // 暂作静态展示,后续如需可点击加 .clickable。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 4.dp, y = 614.dp)
                    .size(width = 404.dp, height = 237.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_untitled_2_recovered_10),
                    contentDescription = "未标题-2-恢复的 10",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // "回答正确"标签(Group 280.png,在底部图像上层,X=117, Y=698, W=199, H=86)。
            // 写在底部图像 Box 之后 → z-order 在其上。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 117.dp, y = 698.dp)
                    .size(width = 199.dp, height = 86.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_learning_group_280),
                    contentDescription = "回答正确标签",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // "回答正确"文本(字号 24,黑色,X=44, Y=644, W=96, H=32)。
            // Y=644 在 Group 280 之上(Y=698),与底部图像上层叠合。
            Text(
                text = "回答正确",
                color = Color.Black,
                fontSize = 24.sp,
                fontFamily = YaHei,
                modifier = Modifier
                    .offset(x = 44.dp, y = 644.dp)
                    .size(width = 96.dp, height = 32.dp),
            )
        }
    }
}