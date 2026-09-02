package com.jueqiao.jianghu.ui.screens.chatresult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 聊天结果页 — 删掉气泡+文本+熊猫,其他元素全部复用原位。
 * 未来要插回气泡/熊猫时,把 SpeechBubble 区块和 Panda 区块取消注释即可。
 */
@Composable
fun ChatResultScreen(
    query: String,
    onBack: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onContinueWork: (String) -> Unit = {},
    onCreateWork: () -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致(跳工坊页)
    BackHandler(enabled = true) {
        onBack()
    }

    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景
        Image(
            painter = painterResource(R.drawable.img_gongfang_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 内容层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // 返回按钮
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 55.dp)
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

            // 未标题-2 23.png(X=57, Y=45, W=160, H=58)
            Image(
                painter = painterResource(R.drawable.img_gongfang_23),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 57.dp, y = 45.dp)
                    .size(width = 160.dp, height = 58.dp),
                contentScale = ContentScale.Fit,
            )

            // 未标题-2 24.png(X=240, Y=45, W=157, H=58)
            Image(
                painter = painterResource(R.drawable.img_gongfang_24),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 240.dp, y = 45.dp)
                    .size(width = 157.dp, height = 58.dp)
                    .clickable(onClick = onOpenChuangzuodangan),
                contentScale = ContentScale.Fit,
            )

            // 教练辅助 标签
            Text(
                text = "教练辅助",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 93.dp, y = 58.dp)
                    .size(width = 71.dp, height = 18.dp),
            )

            // 创作档案 标签
            Text(
                text = "创作档案",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 287.dp, y = 58.dp)
                    .size(width = 71.dp, height = 18.dp)
                    .clickable(onClick = onOpenChuangzuodangan),
            )

            // 中央卡片框
            Image(
                painter = painterResource(R.drawable.img_gongfang_frame),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-115).dp)
                    .size(width = 398.dp, height = 432.dp)
                    .alpha(1.0f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )

            // 搜索条背景
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

            // 确定按钮 — 点击清焦点 + 收键盘 + 触发搜索
            Box(
                modifier = Modifier
                    .offset(x = 306.dp, y = 128.5.dp)
                    .size(width = 60.dp, height = 24.5.dp)
                    .clickable {
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
                Text(
                    text = "确定",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                )
            }

            // 输入框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 116.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 221.dp, height = 52.dp)
                        .offset(x = (-30).dp),
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

            // (气泡 + 文本) 已删 — 待用户补素材后插回
            // (熊猫 + 教练) 已删 — 待用户补素材后插回

            // Group 258 图标(X=333, Y=198, W=38.91, H=37)
            Image(
                painter = painterResource(R.drawable.group_258),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 333.dp, y = 198.dp)
                    .size(width = 38.91.dp, height = 37.dp),
                contentScale = ContentScale.Fit,
            )

            // Rectangle 220.png(X=160, Y=201, W=171, H=31)
            Box(
                modifier = Modifier
                    .offset(x = 160.dp, y = 201.dp)
                    .size(width = 171.dp, height = 31.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_chatresult_rect220),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "帮我提供一些机械熊猫生成要素。",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }

            // Group 213 图标(X=40, Y=247, W=30.35, H=25.5)
            Image(
                painter = painterResource(R.drawable.group_213),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 40.dp, y = 247.dp)
                    .size(width = 30.35.dp, height = 25.5.dp),
                contentScale = ContentScale.Fit,
            )

            // Rectangle 221.png(X=77, Y=244, W=265, H=31)
            Box(
                modifier = Modifier
                    .offset(x = 77.dp, y = 244.dp)
                    .size(width = 265.dp, height = 31.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_chatresult_rect221),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "半机械武侠熊猫，古铜机甲，墨绿劲装，发光眼，立绘",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            // 创作作品列表
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

            // 创建作品按钮 — 点击跳转到 ShengtuScreen(生图页)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 393.dp)
                    .size(width = 372.dp, height = 47.dp)
                    .clickable(onClick = onCreateWork),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_zaowu_2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "创建作品",
                    color = Color.White,
                    style = TextStyle(fontFamily = YaHei, fontSize = 20.sp),
                )
            }
        }
    }
}

/**
 * 透明点击热区(同 GongfangScreen)
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
