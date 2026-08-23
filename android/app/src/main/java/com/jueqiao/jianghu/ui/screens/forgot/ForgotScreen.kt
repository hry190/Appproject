package com.jueqiao.jianghu.ui.screens.forgot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.data.Validators
import com.jueqiao.jianghu.ui.components.AuthCheckbox
import com.jueqiao.jianghu.ui.components.CardFrame
import com.jueqiao.jianghu.ui.components.KeyIcon
import com.jueqiao.jianghu.ui.components.PeopleSafeIcon
import com.jueqiao.jianghu.ui.components.PhoneIcon
import com.jueqiao.jianghu.ui.components.PrimaryButton
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay

/**
 * Forgot password screen — phone + code + new pwd + confirm pwd.
 * Mirrors RN forgot.tsx.
 */
@Composable
fun ForgotScreen(
    onSubmitted: () -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown -= 1
        }
    }

    val phoneValid = Validators.isPhone(phone)
    val codeValid = Validators.isCode(code)
    val newPwdValid = Validators.isPassword(newPwd)
    val confirmValid = newPwd == confirmPwd && newPwd.isNotEmpty()
    val canSubmit = phoneValid && codeValid && newPwdValid && confirmValid && agreed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(AuthDimens.canvasH)) {
                // Background
                Image(
                    painter = painterResource(R.drawable.img_forgot_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.85f),
                    contentScale = ContentScale.Crop,
                )
                // Back arrow
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "返回",
                    modifier = Modifier
                        .offset(x = 16.dp, y = 56.dp)
                        .size(32.dp)
                        .clickable(onClick = onBack),
                )
                // Title
                Text(
                    text = "忘记密码",
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                    modifier = Modifier.offset(x = 47.dp, y = 110.dp),
                )
                // Mascot
                Image(
                    painter = painterResource(R.drawable.img_forgot_mascot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 80.dp)
                        .size(width = 200.dp, height = 240.dp),
                    contentScale = ContentScale.Fit,
                )

                // Card
                Box(
                    modifier = Modifier
                        .offset(x = 20.dp, y = 349.dp)
                        .size(width = AuthDimens.canvasW - 40.dp, height = 400.dp),
                ) {
                    CardFrame(modifier = Modifier.fillMaxSize(), height = 400.dp)
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        ForgotField(
                            value = phone,
                            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11); phoneError = null },
                            placeholder = "请输入手机号",
                            keyboardType = KeyboardType.Phone,
                            leading = { PhoneIcon(modifier = Modifier.size(18.dp, 22.dp)) },
                        )
                        if (phoneError != null) {
                            Text(
                                text = phoneError!!,
                                color = AuthPalette.ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ForgotField(
                            value = code,
                            onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                            placeholder = "请输入验证码",
                            keyboardType = KeyboardType.Number,
                            leading = { PeopleSafeIcon(modifier = Modifier.size(20.dp, 22.dp)) },
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HorizontalDivider(
                                        modifier = Modifier.height(20.dp).width(1.dp),
                                        color = AuthPalette.DividerGray,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (countdown > 0) "${countdown}s 后重发" else "获取验证码",
                                        color = if (countdown > 0) AuthPalette.ActionGray else AuthPalette.TextDark,
                                        style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
                                        modifier = Modifier.clickable(enabled = phoneValid && countdown == 0) {
                                            if (phoneValid) countdown = 60
                                        },
                                    )
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ForgotField(
                            value = newPwd,
                            onValueChange = { newPwd = it.take(24) },
                            placeholder = "请输入新密码 (6-24 位)",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation(),
                            leading = { KeyIcon(modifier = Modifier.size(22.dp)) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ForgotField(
                            value = confirmPwd,
                            onValueChange = { confirmPwd = it.take(24) },
                            placeholder = "请再次输入新密码",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation(),
                            leading = { KeyIcon(modifier = Modifier.size(22.dp)) },
                        )
                        if (confirmPwd.isNotEmpty() && !confirmValid) {
                            Text(
                                text = "两次输入的密码不一致",
                                color = AuthPalette.ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AuthCheckbox(checked = agreed, onCheckedChange = { agreed = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            ForgotAgreementText(
                                onOpenAgreement = onOpenAgreement,
                                onOpenPrivacy = onOpenPrivacy,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "确定修改",
                            onClick = onSubmitted,
                            enabled = canSubmit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForgotField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AuthDimens.inputH)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = AuthPalette.Placeholder,
                        style = TextStyle(fontFamily = YaHei, fontSize = 15.sp),
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = YaHei,
                        fontSize = 16.sp,
                        color = AuthPalette.TextDark,
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = visualTransformation,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (trailing != null) trailing()
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(AuthPalette.InputBorder),
        )
    }
}

@Composable
private fun ForgotAgreementText(
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    Row {
        Text(
            text = "我已阅读并同意",
            color = AuthPalette.TextDark,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
        )
        Text(
            text = "《用户协议》",
            color = AuthPalette.LinkOlive,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier.clickable { onOpenAgreement() },
        )
        Text(
            text = "和",
            color = AuthPalette.TextDark,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
        )
        Text(
            text = "《隐私条款》",
            color = AuthPalette.LinkOlive,
            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
            modifier = Modifier.clickable { onOpenPrivacy() },
        )
    }
}