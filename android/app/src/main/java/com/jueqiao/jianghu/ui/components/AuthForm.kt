package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun AuthLineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuthDimens.inputH)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = AuthPalette.Placeholder,
                        style = TextStyle(fontFamily = YaHei, fontSize = 15.sp),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        color = if (enabled) AuthPalette.TextDark else AuthPalette.ActionGray,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = visualTransformation,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (trailing != null) trailing()
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(AuthPalette.InputBorder),
        )
    }
}

@Composable
fun VerificationCodeAction(
    countdown: Int,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(
            modifier = Modifier.height(20.dp).width(1.dp),
            color = AuthPalette.DividerGray,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when {
                loading -> "正在发送…"
                countdown > 0 -> "${countdown}秒后重发"
                else -> "获取验证码"
            },
            color = if (enabled && !loading && countdown == 0) {
                AuthPalette.TextDark
            } else {
                AuthPalette.ActionGray
            },
            style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
            modifier = Modifier
                .height(48.dp)
                .clickable(
                    enabled = enabled && !loading && countdown == 0,
                    onClick = onClick,
                )
                .padding(horizontal = 4.dp, vertical = 15.dp),
        )
    }
}

@Composable
fun AgreementRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    prefix: String = "我已阅读并同意",
    privacyLabel: String = "《隐私条款》",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            size = 18.dp,
        )
        Spacer(Modifier.width(8.dp))
        Text(prefix, color = AuthPalette.TextDark, style = agreementStyle)
        Text(
            "《用户协议》",
            color = AuthPalette.LinkOlive,
            style = agreementStyle,
            modifier = Modifier.clickable(onClick = onOpenAgreement),
        )
        Text("和", color = AuthPalette.TextDark, style = agreementStyle)
        Text(
            privacyLabel,
            color = AuthPalette.LinkOlive,
            style = agreementStyle,
            modifier = Modifier.clickable(onClick = onOpenPrivacy),
        )
    }
}

private val agreementStyle = TextStyle(fontFamily = YaHei, fontSize = 11.sp)
