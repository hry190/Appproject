package com.jueqiao.jianghu.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

enum class AuthFeedbackKind {
    Error,
    Success,
    Notice,
}

@Composable
fun AuthStatusBanner(
    message: String?,
    modifier: Modifier = Modifier,
    kind: AuthFeedbackKind = AuthFeedbackKind.Error,
) {
    if (message.isNullOrBlank()) return

    val background = when (kind) {
        AuthFeedbackKind.Error -> AuthPalette.ErrorSoft
        AuthFeedbackKind.Success -> AuthPalette.SuccessSoft
        AuthFeedbackKind.Notice -> AuthPalette.WarningSoft
    }
    val foreground = when (kind) {
        AuthFeedbackKind.Error -> AuthPalette.ErrorRed
        AuthFeedbackKind.Success -> AuthPalette.SuccessGreen
        AuthFeedbackKind.Notice -> Color(0xFF77643B)
    }
    val icon = when (kind) {
        AuthFeedbackKind.Error -> R.drawable.ic_error_seal
        AuthFeedbackKind.Success -> R.drawable.ic_success_seal
        AuthFeedbackKind.Notice -> R.drawable.ic_guardian_shield
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            color = foreground,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
        )
    }
}

@Composable
fun PasswordVisibilityButton(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(48.dp).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (visible) R.drawable.ic_password_visible
                else R.drawable.ic_password_hidden
            ),
            contentDescription = if (visible) "隐藏密码" else "显示密码",
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun PasswordStrengthBar(
    password: String,
    modifier: Modifier = Modifier,
) {
    if (password.isEmpty()) return
    val score = listOf(
        password.length >= 8,
        password.any(Char::isLetter) && password.any(Char::isDigit),
        password.length >= 12 || password.any { !it.isLetterOrDigit() },
    ).count { it }
    val label = when (score) {
        3 -> "密码强度：较强"
        2 -> "密码强度：可用"
        else -> "密码至少8位，建议包含字母和数字"
    }
    val activeColor = when (score) {
        3 -> AuthPalette.SuccessGreen
        2 -> AuthPalette.LinkOlive
        else -> Color(0xFF9B7B55)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(
                        if (index < score) activeColor else AuthPalette.InputBorder,
                        RoundedCornerShape(2.dp),
                    )
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = activeColor,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
            ),
        )
    }
}

@Composable
fun AuthAssetIcon(
    @DrawableRes resource: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
