package com.jueqiao.jianghu.ui.screens.picture

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.screens.settings.CreationDraftStore
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 图片页面 — 从 ShengtuScreen 复制了除"正在生成图片..."文字、加载 1.png、Rectangle 228 之外的全部元素。
 * 后续接生图结果展示时,把图片渲染等内容加在这里。
 */
@Composable
fun PictureScreen(
    onBack: () -> Unit = {},
    onCreateWork: () -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
) {
    // 拦截系统返回键 — 行为与点击左上角"返回"按钮一致
    BackHandler(enabled = true) {
        onBack()
    }

    // 输入框焦点管理
    val rect227FocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val draftStore = remember(context) { CreationDraftStore(context) }
    var rect227Text by rememberSaveable {
        mutableStateOf(draftStore.read(CreationDraftStore.Keys.PicturePrompt))
    }

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
            // 返回按钮(从 ChatResultScreen 复用:X=20, Y=41, 点击区 32×32)
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
                    .size(width = 127.dp, height = 46.dp)
                    .clickable(onClick = onOpenChuangzuodangan),
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
                    .size(width = 76.dp, height = 25.dp)
                    .clickable(onClick = onOpenChuangzuodangan),
            )

            // Group 212.png(W=372, H=679)— 主内容区,水平居中 + 垂直上移 2
            Image(
                painter = painterResource(R.drawable.img_shengtu_group212),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-2).dp)
                    .size(width = 372.dp, height = 679.dp),
                contentScale = ContentScale.Fit,
            )

            // Group 253.png(X=291, Y=125, W=65, H=23)
            Image(
                painter = painterResource(R.drawable.img_shengtu_group253),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 291.dp, y = 125.dp)
                    .size(width = 65.dp, height = 23.dp),
                contentScale = ContentScale.Fit,
            )

            // image 38.png(W=300, H=450)— 原 Rectangle 228 位置,水平居中 + Y=152(@1x,放 drawable/)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 152.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_picture_image38),
                    contentDescription = null,
                    modifier = Modifier.size(width = 300.dp, height = 450.dp),
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

            // 未标题-1 41.png(X=310, Y=605, W=92, H=143)
            Image(
                painter = painterResource(R.drawable.img_shengtu_untitled41),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 310.dp, y = 605.dp)
                    .size(width = 92.dp, height = 143.dp),
                contentScale = ContentScale.Fit,
            )

            val focusRequester = rect227FocusRequester

            // Rectangle 227.png(X=38, Y=708, W=256, H=20)— 真输入框
            Box(
                modifier = Modifier
                    .offset(x = 38.dp, y = 708.dp)
                    .size(width = 256.dp, height = 20.dp)
                    .background(Color.Transparent),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_rect227),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
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
                BasicTextField(
                    value = rect227Text,
                    onValueChange = {
                        rect227Text = it
                        draftStore.saveIfEnabled(CreationDraftStore.Keys.PicturePrompt, it)
                    },
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Black),
                    textStyle = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 10.sp,
                        color = Color.Transparent,
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
                    .clickable {
                        draftStore.clear(CreationDraftStore.Keys.PicturePrompt)
                        onCreateWork()
                    },
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
