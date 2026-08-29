package com.jueqiao.jianghu.ui.screens.gongfang

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    onSearch: (String) -> Unit = {},
    onContinueWork: (String) -> Unit = {},
) {
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
                .offset(x = 20.dp, y = 51.dp)
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
                .offset(x = 57.dp, y = 29.dp)
                .size(width = 160.dp, height = 58.dp),
            contentScale = ContentScale.Fit,
        )

        // 未标题-2 24.png(X=265, Y=70, W=127, H=46)
        Image(
            painter = painterResource(R.drawable.img_gongfang_24),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 265.dp, y = 35.dp)
                .size(width = 127.dp, height = 46.dp),
            contentScale = ContentScale.Fit,
        )

        // "教练辅助" 标签(X=87, Y=81, W=71, H=18,字号 14)
        Text(
            text = "教练辅助",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 93.dp, y = 46.dp)
                .size(width = 71.dp, height = 18.dp),
        )

        // "创作档案" 标签(X=287, Y=81, W=76, H=15,字号 14)
        Text(
            text = "创作档案",
            color = Color.Black,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            modifier = Modifier
                .offset(x = 287.dp, y = 46.dp)
                .size(width = 76.dp, height = 25.dp),
        )

        // 框.png(屏幕水平居中,垂直位置可调:W=372, H=462, opacity 100%, corner radius 8)
        Image(
            painter = painterResource(R.drawable.img_gongfang_frame),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-115).dp)   // ← 改这个值:负数=上移,正数=下移;0=完全居中
                .size(width = 392.dp, height = 432.dp)
                .alpha(1.0f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
        )

        // Rectangle 218.png(X=40, Y=120, W=332, H=42)— 搜索条背景(水平居中）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_rect218),
                contentDescription = null,
                modifier = Modifier.size(width = 332.dp, height = 42.dp),
                contentScale = ContentScale.FillBounds,
            )
        }

        // Rectangle 219.png(X=306, Y=128.5, W=60, H=24.5) — 点击触发搜索
        Box(
            modifier = Modifier
                .offset(x = 306.dp, y = 128.5.dp)
                .size(width = 60.dp, height = 24.5.dp)
                .clickable {
                    // 点击确定:清焦点 + 收键盘 + 触发搜索
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSearch(inputText)
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_rect219),
                contentDescription = "确定",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            // "确定" 按钮文字(在 Rectangle 219 之上)
            Text(
                text = "确定",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
            )
        }

            // "请输入一个主题或一句话描述" 输入框(居中于搜索条背景)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 116.dp),  // 输入框下移10 → Y=98+10=108, 居中于 Y=120-162 背景内
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 221.dp, height = 52.dp)  // 2/3 原宽,避免被"确定"按钮点到
                        .offset(x = (-30).dp),  // 向左移动 30dp
                    contentAlignment = Alignment.CenterStart,
                ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "请输入一个主题或一句话描述",
                            color = Color.White,
                            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                    ),
                    textStyle = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 12.sp,
                        color = Color.White,
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // 熊猫角色(未标题-1 45.png, X=291, Y=442, W=112, H=167)
        Image(
            painter = painterResource(R.drawable.img_gongfang_panda),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 285.dp, y = 377.dp)
                .size(width = 112.dp, height = 167.dp),
            contentScale = ContentScale.Fit,
        )

        // 186.png 气泡(X=133, Y=349, W=178, H=100)— 上移 20
        Box(
            modifier = Modifier
                .offset(x = 133.dp, y = 349.dp)
                .size(width = 178.dp, height = 100.dp),
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

        // 2.png 底部"创建作品"大按钮(屏幕水平居中,下移 40)
        Image(
            painter = painterResource(R.drawable.img_zaowu_2),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 383.dp)
                .size(width = 372.dp, height = 47.dp),
            contentScale = ContentScale.Fit,
        )

        // "创建作品" 按钮文字(与 2.png 同步居中,下移 40)
        Text(
            text = "创建作品",
            color = Color.White,
            style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 380.dp)
                .size(width = 80.dp, height = 30.dp),
        )

        // 创作作品.png"创作作品"列表(X=20, Y=552, W=374, H=217) — 上移 30
        // 列表包含:创作作品/所有作品 标题 + 3 行作品(含"继续创作"按钮)
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 542.dp)
                .size(width = 374.dp, height = 217.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_works),
                contentDescription = "创作作品",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            WorkContinueHotspot(
                x = 280.dp, y = 57.dp, width = 80.dp, height = 16.dp,
                onClick = { onContinueWork("panda_ai") },
            )
            WorkContinueHotspot(
                x = 280.dp, y = 114.dp, width = 80.dp, height = 16.dp,
                onClick = { onContinueWork("untitled") },
            )
            WorkContinueHotspot(
                x = 280.dp, y = 171.5.dp, width = 80.dp, height = 16.dp,
                onClick = { onContinueWork("poster") },
            )
        }
        }
    }
}

/**
 * 透明点击热区:盖在"继续创作"按钮图片上,触发 onContinueWork(workId)
 * 图实际渲染 374×206(ContentScale.Fit + 5.5dp 上下 padding)
 *   Row 1 按钮 ≈ 图内 (295, 22)~(348, 48) → 容器 (295, 27.5)
 *   Row 2 按钮 ≈ 图内 (295, 75)~(348,101) → 容器 (295, 80.5)
 *   Row 3 按钮 ≈ 图内 (295,128)~(348,154) → 容器 (295,133.5)
 * 热区各向外扩 ~10dp 方便点击
 */
@Composable
private fun BoxScope.WorkContinueHotspot(
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .clickable(onClick = onClick),
    )
}
