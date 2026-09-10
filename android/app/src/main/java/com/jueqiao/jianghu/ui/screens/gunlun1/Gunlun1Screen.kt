package com.jueqiao.jianghu.ui.screens.gunlun1

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * 滚轮1 页 — 修炼页 → "前往后院"按钮跳转目标。
 *
 * 重构后:用 StandardGunlunScaffold 复用背景 + 熊猫(不可点击) + 返回按钮。
 * 本文件只剩页独有的元素:后山按钮(可点击跳后山1页) + 修炼按钮(可点击跳学习1页) + 跳转气泡。
 */
@Composable
fun Gunlun1Screen(
    onBack: () -> Unit = {},
    onOpenGunlun2: () -> Unit = {},
    onOpenLearning1: () -> Unit = {},
    onOpenHoushan1: () -> Unit = {},
) {
    // 熊猫在滚轮1 不可点击(叶子页),不传 onPandaClick
    StandardGunlunScaffold(onBack = onBack) {
        // 未标题-1 50.png — "后山"按钮(背景图像 + 文字,点击跳转到后山1页 = Houshan1Screen)
        //   (X=247, Y=165, W=55, H≈117.12 按 PNG 比例 85x181 算出)
        Box(
            modifier = Modifier
                .offset(x = 247.dp, y = 165.dp)
                .size(width = 55.dp, height = 117.12.dp)
                .clickable(onClick = onOpenHoushan1),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_1_50),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // "后\n山" 竖排文字(在未标题-1 50.png 上,X=247, Y=165, 字号 16, 白色, YaHei)— 左移 3dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = (-3).dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "后",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
                Text(
                    text = "山",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
            }
        }

        // Rectangle 86.png 气泡(D:\图\Rectangle 86.png)— X=23, Y=324, W=148, H=84
        //   点击气泡跳转到滚轮2
        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 324.dp)
                .size(width = 148.dp, height = 84.dp)
                .clickable(onClick = onOpenGunlun2),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gunlun1_rect86),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡上的文本(14sp, 黑色)
            Text(
                text = "你还未习得秘籍，不妨前往后山试炼，寻觅机缘",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxSize(),
            )
        }

        // 未标题-150.png — "修炼"按钮(背景图像 + 文字,点击跳转到学习1页 = HoushanScreen)
        //   (X=35, Y=548, W=55, H=117)
        Box(
            modifier = Modifier
                .offset(x = 35.dp, y = 548.dp)
                .size(width = 55.dp, height = 117.dp)
                .clickable(onClick = onOpenLearning1),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gunlun1_untitled_150),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // "修\n炼" 竖排文字(在未标题-150.png 上,X=35, Y=548, 字号 16, 白色, YaHei)— 左移 5dp(先左移10,再右移5)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = (-5).dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "修",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
                Text(
                    text = "炼",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp),
                )
            }
        }
    }
}
