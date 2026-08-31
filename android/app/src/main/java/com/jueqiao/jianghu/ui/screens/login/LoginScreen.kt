package com.jueqiao.jianghu.ui.screens.login

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
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
import com.jueqiao.jianghu.data.Validators
import com.jueqiao.jianghu.ui.components.AgreementRow
import com.jueqiao.jianghu.ui.components.AuthLineField
import com.jueqiao.jianghu.ui.components.AuthStatusBanner
import com.jueqiao.jianghu.ui.components.CardFrame
import com.jueqiao.jianghu.ui.components.KeyIcon
import com.jueqiao.jianghu.ui.components.PasswordVisibilityButton
import com.jueqiao.jianghu.ui.components.PhoneIcon
import com.jueqiao.jianghu.ui.components.PrimaryButton
import com.jueqiao.jianghu.ui.theme.AuthDimens
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.JianghuTheme
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun LoginScreen(
    onLogin: (phone: String, password: String) -> Unit,
    onOpenForgot: () -> Unit,
    onOpenRegister: () -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onClearError: () -> Unit = {},
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var agreed by rememberSaveable { mutableStateOf(false) }

    val phoneValid = Validators.isPhone(phone)
    val passwordValid = Validators.isPassword(password)
    val canSubmit = phoneValid && passwordValid && agreed && !isSubmitting

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景图全屏(铺到状态栏/导航栏后面)
        Image(
            painter = painterResource(R.drawable.img_auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.85f),
            contentScale = ContentScale.Crop,
        )
        // 内容层(避开系统导航栏)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "欢迎来到机巧江湖",
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 31.sp,
                        color = AuthPalette.TextDark,
                    ),
                    modifier = Modifier.offset(x = 20.dp, y = 112.dp),
                )
                Image(
                    painter = painterResource(R.drawable.img_auth_mascot),
                    contentDescription = "机巧江湖熊猫引路人",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 263.dp)
                        .size(width = 185.dp, height = 220.dp)
                        .graphicsLayer(scaleX = -1f),
                    contentScale = ContentScale.Fit,
                )

                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = 470.dp)
                        .size(width = 392.dp, height = 350.dp),
                ) {
                    CardFrame(modifier = Modifier.fillMaxSize(), height = 350.dp)
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
                            enabled = !isSubmitting,
                            leading = {
                                PhoneIcon(
                                    modifier = Modifier.size(18.dp, 22.dp),
                                    contentDescription = "手机号",
                                )
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                        AuthLineField(
                            value = password,
                            onValueChange = {
                                password = it.take(64)
                                onClearError()
                            },
                            placeholder = "请输入密码",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            enabled = !isSubmitting,
                            leading = {
                                KeyIcon(
                                    modifier = Modifier.size(22.dp),
                                    contentDescription = "密码",
                                )
                            },
                            trailing = {
                                PasswordVisibilityButton(
                                    visible = passwordVisible,
                                    onToggle = { passwordVisible = !passwordVisible },
                                )
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "忘记密码？找回密码",
                                color = AuthPalette.ActionGray,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                                modifier = Modifier.clickable(
                                    enabled = !isSubmitting,
                                    onClick = onOpenForgot,
                                ),
                            )
                        }
                        AuthStatusBanner(errorMessage)
                        Spacer(Modifier.height(if (errorMessage == null) 8.dp else 6.dp))
                        AgreementRow(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            onOpenAgreement = onOpenAgreement,
                            onOpenPrivacy = onOpenPrivacy,
                        )
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(
                            text = if (isSubmitting) "正在登录…" else "登 录",
                            onClick = { onLogin(phone, password) },
                            enabled = canSubmit,
                            loading = isSubmitting,
                            modifier = Modifier
                                .width(310.dp)
                                .align(Alignment.CenterHorizontally),
                            height = 44.dp,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                "没有账号？",
                                color = AuthPalette.ActionGray,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                            )
                            Text(
                                "立即注册",
                                color = AuthPalette.LinkOlive,
                                style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                                modifier = Modifier.clickable(
                                    enabled = !isSubmitting,
                                    onClick = onOpenRegister,
                                ),
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun LoginScreenPreview() {
    JianghuTheme {
        LoginScreen(
            onLogin = { _, _ -> },
            onOpenForgot = {},
            onOpenRegister = {},
            onOpenAgreement = {},
            onOpenPrivacy = {},
            onBack = {},
        )
    }
}
