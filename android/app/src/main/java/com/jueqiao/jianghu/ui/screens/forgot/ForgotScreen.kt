package com.jueqiao.jianghu.ui.screens.forgot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.auth.AuthOperation
import com.jueqiao.jianghu.auth.VerificationPurpose
import com.jueqiao.jianghu.data.Validators
import com.jueqiao.jianghu.ui.components.AuthAssetIcon
import com.jueqiao.jianghu.ui.components.AuthFeedbackKind
import com.jueqiao.jianghu.ui.components.AuthLineField
import com.jueqiao.jianghu.ui.components.AuthStatusBanner
import com.jueqiao.jianghu.ui.components.CardFrame
import com.jueqiao.jianghu.ui.components.KeyIcon
import com.jueqiao.jianghu.ui.components.PasswordStrengthBar
import com.jueqiao.jianghu.ui.components.PasswordVisibilityButton
import com.jueqiao.jianghu.ui.components.PeopleSafeIcon
import com.jueqiao.jianghu.ui.components.PhoneIcon
import com.jueqiao.jianghu.ui.components.PrimaryButton
import com.jueqiao.jianghu.ui.components.VerificationCodeAction
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.JianghuTheme
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay

@Composable
fun ForgotScreen(
    onSubmitted: () -> Unit,
    onRequestCode: (phone: String, onCooldown: (Int) -> Unit) -> Unit,
    onResetPassword: (
        phone: String,
        code: String,
        newPassword: String,
        onSuccess: () -> Unit,
    ) -> Unit,
    onBack: () -> Unit,
    operation: AuthOperation? = null,
    errorMessage: String? = null,
    onClearError: () -> Unit = {},
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var countdown by rememberSaveable { mutableIntStateOf(0) }
    var resetSucceeded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown -= 1
        }
    }

    val busy = operation != null
    val matches = newPassword == confirmPassword && confirmPassword.isNotEmpty()
    val canSubmit = Validators.isPhone(phone) &&
        Validators.isCode(code) &&
        Validators.isPassword(newPassword) &&
        matches &&
        !busy

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(AuthDimens.canvasH)) {
                Image(
                    painter = painterResource(R.drawable.img_forgot_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.9f),
                    contentScale = ContentScale.Crop,
                )
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "返回",
                    modifier = Modifier
                        .offset(x = 16.dp, y = 56.dp)
                        .size(32.dp)
                        .clickable(enabled = !busy, onClick = onBack),
                )
                Text(
                    text = "忘记密码",
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                    modifier = Modifier.offset(x = 47.dp, y = 50.dp),
                )
                Image(
                    painter = painterResource(R.drawable.img_forgot_mascot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 110.dp)
                        .size(width = 200.dp, height = 240.dp),
                    contentScale = ContentScale.Fit,
                )

                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = 340.dp)
                        .size(width = 392.dp, height = 430.dp),
                ) {
                    CardFrame(modifier = Modifier.fillMaxSize(), height = 430.dp)
                    if (resetSucceeded) {
                        ResetSuccess(onReturnToLogin = onSubmitted)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 28.dp, vertical = 34.dp),
                        ) {
                            AuthLineField(
                                value = phone,
                                onValueChange = {
                                    phone = it.filter(Char::isDigit).take(11)
                                    onClearError()
                                },
                                placeholder = "请输入手机号",
                                keyboardType = KeyboardType.Phone,
                                enabled = !busy,
                                leading = {
                                    PhoneIcon(Modifier.size(18.dp, 22.dp), "手机号")
                                },
                            )
                            Spacer(Modifier.height(6.dp))
                            AuthLineField(
                                value = code,
                                onValueChange = {
                                    code = it.filter(Char::isDigit).take(6)
                                    onClearError()
                                },
                                placeholder = "请输入验证码",
                                keyboardType = KeyboardType.Number,
                                enabled = !busy,
                                leading = {
                                    PeopleSafeIcon(Modifier.size(20.dp, 22.dp), "验证码")
                                },
                                trailing = {
                                    VerificationCodeAction(
                                        countdown = countdown,
                                        loading = operation == AuthOperation.RequestCode,
                                        enabled = Validators.isPhone(phone),
                                        onClick = {
                                            onRequestCode(phone) { countdown = it }
                                        },
                                    )
                                },
                            )
                            Spacer(Modifier.height(6.dp))
                            AuthLineField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it.take(64)
                                    onClearError()
                                },
                                placeholder = "请输入新密码，至少8位",
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (newPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                enabled = !busy,
                                leading = { KeyIcon(Modifier.size(22.dp), "新密码") },
                                trailing = {
                                    PasswordVisibilityButton(
                                        visible = newPasswordVisible,
                                        onToggle = { newPasswordVisible = !newPasswordVisible },
                                    )
                                },
                            )
                            PasswordStrengthBar(newPassword, Modifier.padding(horizontal = 8.dp))
                            Spacer(Modifier.height(5.dp))
                            AuthLineField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it.take(64)
                                    onClearError()
                                },
                                placeholder = "请再次输入新密码",
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (confirmPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                enabled = !busy,
                                leading = { KeyIcon(Modifier.size(22.dp), "确认密码") },
                                trailing = {
                                    PasswordVisibilityButton(
                                        visible = confirmPasswordVisible,
                                        onToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                    )
                                },
                            )
                            val localError = if (
                                confirmPassword.isNotEmpty() && !matches
                            ) "两次输入的密码不一致" else errorMessage
                            AuthStatusBanner(localError, Modifier.padding(top = 7.dp))
                            Spacer(Modifier.height(14.dp))
                            PrimaryButton(
                                text = if (operation == AuthOperation.ResetPassword) {
                                    "正在修改…"
                                } else {
                                    "确定修改"
                                },
                                onClick = {
                                    onResetPassword(phone, code, newPassword) {
                                        resetSucceeded = true
                                        onClearError()
                                    }
                                },
                                enabled = canSubmit,
                                loading = operation == AuthOperation.ResetPassword,
                                modifier = Modifier
                                    .width(310.dp)
                                    .align(Alignment.CenterHorizontally),
                                height = 44.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetSuccess(onReturnToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 54.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuthAssetIcon(
            resource = R.drawable.ic_success_seal,
            contentDescription = "修改成功",
            modifier = Modifier.size(68.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            "密码修改成功",
            color = AuthPalette.SuccessGreen,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "旧设备的登录状态已经失效\n请使用新密码重新登录",
            color = AuthPalette.ActionGray,
            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
        )
        Spacer(Modifier.height(28.dp))
        AuthStatusBanner(
            message = "新的通行令已准备好",
            kind = AuthFeedbackKind.Success,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = "返回登录",
            onClick = onReturnToLogin,
            modifier = Modifier.width(280.dp),
            height = 44.dp,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ForgotScreenPreview() {
    JianghuTheme {
        ForgotScreen(
            onSubmitted = {},
            onRequestCode = { _, _ -> },
            onResetPassword = { _, _, _, _ -> },
            onBack = {},
        )
    }
}
