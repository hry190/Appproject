package com.jueqiao.jianghu.ui.screens.gongfang

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 工坊页 — 极简骨架版。
 * 后续内容(搜索框 / 教练气泡 / 作品列表 等)由用户在此基础上自行添加。
 */
@Composable
fun GongfangScreen(
    onBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(作品创作.png)— 延伸到屏幕底部
        Image(
            painter = painterResource(R.drawable.img_gongfang_bg),
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
        // 返回按钮(Return.png,与作品创作页一致:X=20, Y=76, W=32, H=32,内部图标 24×24)
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 76.dp)
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

        // 未标题-2 23.png(X=57, Y=64, W=160, H=58)
        Image(
            painter = painterResource(R.drawable.img_gongfang_23),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 57.dp, y = 64.dp)
                .size(width = 160.dp, height = 58.dp),
            contentScale = ContentScale.Fit,
        )

        // 未标题-2 24.png(X=265, Y=70, W=127, H=46)
        Image(
            painter = painterResource(R.drawable.img_gongfang_24),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 265.dp, y = 70.dp)
                .size(width = 127.dp, height = 46.dp),
            contentScale = ContentScale.Fit,
        )

        // "教练辅助" 标签(X=87, Y=81, W=71, H=18,字号 14)
        Text(
            text = "教练辅助",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 93.dp, y = 81.dp)
                .size(width = 71.dp, height = 18.dp),
        )

        // "创作档案" 标签(X=287, Y=81, W=76, H=15,字号 14)
        Text(
            text = "创作档案",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 287.dp, y = 81.dp)
                .size(width = 76.dp, height = 25.dp),
        )

        // 框.png(屏幕水平居中,垂直位置可调:W=372, H=462, opacity 100%, corner radius 8)
        Image(
            painter = painterResource(R.drawable.img_gongfang_frame),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp)   // ← 改这个值:负数=上移,正数=下移;0=完全居中
                .size(width = 372.dp, height = 462.dp)
                .alpha(1.0f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
        )

        // Rectangle 218.png(X=40, Y=155, W=332, H=42)
        Image(
            painter = painterResource(R.drawable.img_gongfang_rect218),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 40.dp, y = 155.dp)
                .size(width = 332.dp, height = 42.dp),
            contentScale = ContentScale.Fit,
        )

        // Rectangle 219.png(X=306, Y=163.5, W=60, H=24.5)
        Image(
            painter = painterResource(R.drawable.img_gongfang_rect219),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 306.dp, y = 163.5.dp)
                .size(width = 60.dp, height = 24.5.dp),
            contentScale = ContentScale.Fit,
        )

        // "确定" 按钮文字(X=322, Y=167.5, W=29, H=17.66,绘制在 Rectangle 219 之上)
        Text(
            text = "确定",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 322.dp, y = 167.dp)
                .size(width = 29.dp, height = 17.66.dp),
        )

        // "请输入一个主题或一句话描述" 占位文字(X=50, Y=167, W=190, H=18)
        Text(
            text = "请输入一个主题或一句话描述",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier
                .offset(x = 50.dp, y = 165.dp)
                .size(width = 190.dp, height = 18.dp),
        )

        // 熊猫角色(未标题-1 45.png, X=291, Y=442, W=112, H=167)
        Image(
            painter = painterResource(R.drawable.img_gongfang_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 285.dp, y = 422.dp)
                .size(width = 112.dp, height = 167.dp),
            contentScale = ContentScale.Fit,
        )

        // 186.png 气泡(X=133, Y=394, W=178, H=96)
        Box(
            modifier = Modifier
                .offset(x = 133.dp, y = 394.dp)
                .size(width = 178.dp, height = 96.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_186),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // 气泡文本(字号 14)
            Text(
                text = "可以在这里和教练\n对话哦，但是教练\n只会分析拆解你的\n问题，不会帮你完成创作",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        // 2.png 底部"创建作品"大按钮(X=20, Y=829, W=372, H=47)
        Image(
            painter = painterResource(R.drawable.img_zaowu_2),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 20.dp, y = 759.dp)
                .size(width = 372.dp, height = 47.dp),
            contentScale = ContentScale.Fit,
        )

        // "创建作品" 按钮文字(X=166, Y=840, W=80, H=20,字号 20,白色)
        Text(
            text = "创建作品",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .offset(x = 166.dp, y = 765.dp)
                .size(width = 80.dp, height = 30.dp),
        )
        }
    }
}