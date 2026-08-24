package com.jueqiao.jianghu.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Speech bubble used by home chat with 阿砚.
 * Rounded rectangle body in cream, or optional image background.
 */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    bubbleColor: Color = Color.Transparent,
    cornerRadius: Dp = 12.dp,
    @DrawableRes bubbleImageRes: Int? = null,
    imageScale: ContentScale = ContentScale.Crop,
    imageColorFilter: ColorFilter? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bubbleColor),
        contentAlignment = Alignment.Center,
    ) {
        // 图片背景层(可选)：铺满气泡,文字叠在上面
        if (bubbleImageRes != null) {
            Image(
                painter = painterResource(bubbleImageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = imageScale,
                colorFilter = imageColorFilter,
            )
        }
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF1A1A2E),
            ),
        )
    }
}