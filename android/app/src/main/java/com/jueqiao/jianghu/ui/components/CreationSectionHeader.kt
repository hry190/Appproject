package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.YaHei

private val CreationHeaderInk = Color(0xFF294A2E)
private val CreationHeaderGold = Color(0xB8D7B55A)
private val CreationHeaderShape = RoundedCornerShape(20.dp)

/** 仅供工坊一级页面使用的单标题栏；后续创作页继续使用原双叶签组件。 */
@Composable
fun CreationSectionHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(72.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .size(48.dp)
                .semantics {
                    contentDescription = "返回"
                    role = Role.Button
                }
                .clickable(
                    interactionSource = backInteractionSource,
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_gongfang_return),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 150.dp, height = 48.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CreationHeaderShape,
                    ambientColor = Color(0x33294A2E),
                    spotColor = Color(0x44294A2E),
                )
                .clip(CreationHeaderShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF2F4F0DB),
                            Color(0xE6DDE9CC),
                        ),
                    ),
                )
                .border(1.dp, CreationHeaderGold, CreationHeaderShape)
                .semantics { heading() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = CreationHeaderInk,
                fontFamily = YaHei,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
