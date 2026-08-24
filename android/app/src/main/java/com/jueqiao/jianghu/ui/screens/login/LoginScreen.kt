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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * Login screen — dual mode (password / SMS code), 412x800 fixed canvas.
 * Mirrors RN login.tsx structure.
 */
@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onOpenForgot: () -> Unit,
    onOpenRegister: () -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit,
) {
    var mode by remember { mutableStateOf("password") }
    var phone by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var registerPwd by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Countdown timer
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown -= 1
        }
    }

    val phoneValid = Validators.isPhone(phone)
    val secretValid = when (mode) {
        "password" -> Validators.isPassword(secret)
        "code"     -> Validators.isCode(secret) && Validators.isPassword(registerPwd)
        else       -> false
    }
    val canSubmit = phoneValid && secretValid && agreed

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
                // Background paper image
                Image(
                    painter = painterResource(R.drawable.img_auth_bg_paper),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.85f),
                    contentScale = ContentScale.Crop,
                )
                // Mascot (mirrored)
                Image(
                    painter = painterResource(R.drawable.img_auth_mascot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 233.dp)
                        .size(width = 200.dp, height = 240.dp)
                        .graphicsLayerScaleX(-1f),
                    contentScale = ContentScale.Fit,
                )

                // Welcome text
                Column(
                    modifier = Modifier
                        .offset(x = 20.dp, y = 110.dp),
                ) {
                    Text(
                        text = "WELCOME",
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = AuthPalette.TextDark,
                        ),
                    )
                    Text(
                        text = "欢迎来到机巧江湖",
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = AuthPalette.TextDark,
                        ),
                    )
                }

                // Login card
                Box(
                    modifier = Modifier
                        .offset(x = 20.dp, y = 455.dp)
                        .size(width = AuthDimens.canvasW - 40.dp, height = 380.dp),
                ) {
                    CardFrame(
                        modifier = Modifier.fillMaxSize(),
                        height = 380.dp,
                    )
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Mode toggle tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            ModeTab(
                                label = "登录",
                                selected = mode == "password",
                                onClick = { mode = "password"; secret = "" },
                            )
                            ModeTab(
                                label = "注册",
                                selected = mode == "code",
                                onClick = { mode = "code"; secret = "" },
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Phone
                        AuthTextField(
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
                        // Secret input
                        AuthTextField(
                            value = secret,
                            onValueChange = { secret = it },
                            placeholder = if (mode == "password") "请输入密码 (6-24 位)" else "请输入验证码",
                            keyboardType = if (mode == "password") KeyboardType.Password else KeyboardType.Number,
                            visualTransformation = if (mode == "password") PasswordVisualTransformation() else VisualTransformation.None,
                            leading = {
                                if (mode == "password") KeyIcon(modifier = Modifier.size(22.dp))
                                else PeopleSafeIcon(modifier = Modifier.size(20.dp, 22.dp))
                            },
                            trailing = if (mode == "code") {
                                {
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
                                }
                            } else null,
                        )
                        // Forgot link (password mode only)
                        if (mode == "password") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    text = "忘记密码",
                                    color = AuthPalette.ActionGray,
                                    style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
                                    modifier = Modifier.clickable { onOpenForgot() },
                                )
                            }
                        }
                        // Register password (code mode only)
                        if (mode == "code") {
                            Spacer(modifier = Modifier.height(8.dp))
                            AuthTextField(
                                value = registerPwd,
                                onValueChange = { registerPwd = it.take(24) },
                                placeholder = "请设置密码 (6-24 位)",
                                keyboardType = KeyboardType.Password,
                                visualTransformation = PasswordVisualTransformation(),
                                leading = { KeyIcon(modifier = Modifier.size(22.dp)) },
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Agreement row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AuthCheckbox(checked = agreed, onCheckedChange = { agreed = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            AgreementText(
                                onOpenAgreement = onOpenAgreement,
                                onOpenPrivacy = onOpenPrivacy,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = if (mode == "password") "登录" else "注册",
                            onClick = onLogin,
                            enabled = canSubmit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(96.dp)
            .background(
                color = if (selected) AuthPalette.LinkOlive else Color.Transparent,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AuthPalette.TextDark,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AuthDimens.inputH)
            .background(Color.Transparent)
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
                BasicTextField(
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
        // Underline
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
private fun AgreementText(
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

/** Horizontal flip helper for the mascot. */
private fun Modifier.graphicsLayerScaleX(scaleX: Float): Modifier =
    this.graphicsLayer(scaleX = scaleX)