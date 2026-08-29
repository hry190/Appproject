package com.jueqiao.jianghu.ui.screens.shengtu

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 生图页面 — 从 ChatResultScreen 复制了返回按钮 + 顶部装饰图/标签 + 底部"创建作品"按钮。
 * 背景图改为 img_shengtu_bg.png(原 D:\图\创作.png)。
 * 后续接生图逻辑时,把图片展示 / 生成进度等加在这里。
 */
@Composable
fun ShengtuScreen(
    onBack: () -> Unit = {},
    onCreateWork: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致
    BackHandler(enabled = true) {
        android.util.Log.d("Shengtu", "BackHandler triggered")
        onBack()
    }

    // 输入框焦点管理
    val rect227FocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景(创作.png)
        Image(
            painter = painterResource(R.drawable.img_shengtu_bg),
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
            // 返回按钮(从 ChatResultScreen 复用:X=20, Y=41, 点击区 32×32)— 上移 10)
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 41.dp)
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

            // 未标题-2 23.png(教练辅助装饰,从 ChatResultScreen 复用)
            Image(
                painter = painterResource(R.drawable.img_gongfang_23),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 57.dp, y = 29.dp)
                    .size(width = 160.dp, height = 58.dp),
                contentScale = ContentScale.Fit,
            )

            // 未标题-2 24.png(创作档案装饰,从 ChatResultScreen 复用)
            Image(
                painter = painterResource(R.drawable.img_gongfang_24),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 265.dp, y = 35.dp)
                    .size(width = 127.dp, height = 46.dp),
                contentScale = ContentScale.Fit,
            )

            // "教练辅助" 标签
            Text(
                text = "教练辅助",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 93.dp, y = 46.dp)
                    .size(width = 71.dp, height = 18.dp),
            )

            // "创作档案" 标签
            Text(
                text = "创作档案",
                color = Color.Black,
                style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                modifier = Modifier
                    .offset(x = 287.dp, y = 46.dp)
                    .size(width = 76.dp, height = 25.dp),
            )

            // Group 212.png(W=372, H=679)— 主内容区,水平居中 + 垂直上移 25(原 Center 上移)
            Image(
                painter = painterResource(R.drawable.img_shengtu_group212),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-2).dp)
                    .size(width = 372.dp, height = 679.dp),
                contentScale = ContentScale.Fit,
            )

            // Group 253.png(X=291, Y=125, W=65, H=23)— @1x,放 drawable-xxhdpi/
            Image(
                painter = painterResource(R.drawable.img_shengtu_group253),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 291.dp, y = 125.dp)
                    .size(width = 65.dp, height = 23.dp),
                contentScale = ContentScale.Fit,
            )

            // Rectangle 228.png(W=300, H=450)— X 轴居中(@2x,放 drawable-xxhdpi/)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 152.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_rect228),
                    contentDescription = null,
                    modifier = Modifier.size(width = 300.dp, height = 450.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // 加载 1.png(W=63, H=63)— 水平居中,Y=345(@1x 63×63,放 drawable-xxhdpi/)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 345.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_loading1),
                    contentDescription = null,
                    modifier = Modifier.size(width = 63.dp, height = 63.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // Rectangle 231.png(X=56, Y=605, W=193, H=30) + 文字 "帮我绘画一只在做手表的技巧熊猫"
            Box(
                modifier = Modifier
                    .offset(x = 56.dp, y = 605.dp)
                    .size(width = 193.dp, height = 30.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_rect231),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "帮我绘画一只在做手表的技巧熊猫",
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Normal, // Regular
                        fontSize = 12.sp,
                    ),
                    modifier = Modifier
                        .size(width = 180.dp, height = 16.dp),
                )
            }
            // 未标题-1 41.png(X=310, Y=605, W=92, H=143)— @2x,放 drawable-xxhdpi/(实际 184×286)
            Image(
                painter = painterResource(R.drawable.img_shengtu_untitled41),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 300.dp, y = 605.dp)
                    .size(width = 92.dp, height = 143.dp),
                contentScale = ContentScale.Fit,
            )

            // 9 个点:15字 × 12sp ≈ 180dp,Box 放宽让文字自然展开
            Text(
                text = "正在生成图片.........",
                color = Color(0xFF50553D),
                style = TextStyle(
                    fontFamily = YaHei,        // YaHei(项目通用字体)
                    fontWeight = FontWeight.Normal, // Regular
                    fontSize = 12.sp,
                ),
                modifier = Modifier
                    .offset(x = 56.dp, y = 130.dp),
                // 不强制 size:让 Text 自己按文字宽度展开,避免被裁
            )

            // 局部状态:输入框文字
            var rect227Text by remember { mutableStateOf("") }
            // 别名,FocusRequester 在外层声明了
            val focusRequester = rect227FocusRequester

            // Rectangle 227.png(X=38, Y=708, W=256, H=20)—真输入框
            // 采用 BasicTextField + Text 叠加结构,
            // 避开 Material3 TextField 的 padding/placeholder 边訧问题
            Box(
                modifier = Modifier
                    .offset(x = 38.dp, y = 708.dp)
                    .size(width = 256.dp, height = 20.dp)
                    .background(Color.Transparent),
            ) {
                // 底层:背景图
                Image(
                    painter = painterResource(R.drawable.img_shengtu_rect227),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                // 中层:placeholder 文本 (空值时显示)
                if (rect227Text.isEmpty()) {
                    Text(
                        text = "请输入创作的作品内容",
                        color = Color(0xFFA3A3A3),
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp),
                    )
                } else {
                    // 中层:输入文本 (有值时显示)
                    Text(
                        text = rect227Text,
                        color = Color.Black,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp),
                    )
                }
                // 顶层:BasicTextField 接管输入 + 弹键盘
                BasicTextField(
                    value = rect227Text,
                    onValueChange = { rect227Text = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Black),
                    textStyle = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 10.sp,
                        color = Color.Transparent, // 文本透明,由中层 Text 渲染
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .padding(start = 8.dp),
                )
            }
            // "保存作品" 按钮(Group 196.png + "保存作品" 文字,屏幕水平居中,下移 393)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 393.dp)
                    .size(width = 372.dp, height = 55.dp)
                    .clickable(onClick = onCreateWork),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_group196),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "保存作品",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
    }
}