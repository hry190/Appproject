package com.jueqiao.jianghu.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.jueqiao.jianghu.auth.AgeBand
import com.jueqiao.jianghu.auth.AuthOperation
import com.jueqiao.jianghu.auth.VerificationPurpose
import com.jueqiao.jianghu.data.Validators
import com.jueqiao.jianghu.ui.components.AgreementRow
import com.jueqiao.jianghu.ui.components.AuthAssetIcon
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
fun RegisterScreen(
    onRequestCode: (
        phone: String,
        purpose: VerificationPurpose,
        onCooldown: (Int) -> Unit,
    ) -> Unit,
    onRegister: (
        phone: String,
        code: String,
        password: String,
        ageBand: AgeBand,
    ) -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit,
    operation: AuthOperation? = null,
    errorMessage: String? = null,
    onClearError: () -> Unit = {},
) {
    var ageValue by rememberSaveable { mutableStateOf<String?>(null) }
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var agreed by rememberSaveable { mutableStateOf(false) }
    var codeCountdown by rememberSaveable { mutableIntStateOf(0) }
    val ageBand = AgeBand.entries.firstOrNull { it.apiValue == ageValue }

    LaunchedEffect(codeCountdown) {
        if (codeCountdown > 0) {
            delay(1000)
            codeCountdown -= 1
        }
    }
    val isBusy = operation != null
    val requestingCode = operation == AuthOperation.RequestCode
    val mainReady = ageBand != null &&
        Validators.isPhone(phone) &&
        Validators.isCode(code) &&
        Validators.isPassword(password) &&
        agreed &&
        !isBusy

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 背景图全屏
        Image(
            painter = painterResource(R.drawable.img_auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.92f),
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
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "返回",
                    modifier = Modifier
                        .offset(x = 18.dp, y = 112.dp)
                        .size(32.dp)
                        .clickable(enabled = !isBusy, onClick = onBack),
                )
                Text(
                    text = "欢迎来到机巧江湖",
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 31.sp,
                    ),
                    modifier = Modifier.offset(x = 52.dp, y = 112.dp),
                )
                Image(
                    painter = painterResource(R.drawable.img_auth_mascot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-200).dp, y = 193.dp)
                        .size(width = 178.dp, height = 210.dp),
                    contentScale = ContentScale.Fit,
                )

                Box(
                    modifier = Modifier
                        .offset(y = 390.dp)
                                .size(width = 392.dp, height = 430.dp),
                ) {
                    CardFrame(modifier = Modifier.fillMaxSize(), height = 430.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 49.dp),
                    ) {
                        Text(
                            text = "填写少侠资料",
                            color = AuthPalette.ActionGray,
                            style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        )
                        Spacer(Modifier.height(4.dp))
                        AgeSelector(
                            selected = ageBand,
                            enabled = !isBusy,
                            onSelected = {
                                ageValue = it.apiValue
                                agreed = false
                                onClearError()
                            },
                        )
                        AuthLineField(
                            value = phone,
                            onValueChange = {
                                phone = it.filter(Char::isDigit).take(11)
                                onClearError()
                            },
                            placeholder = "请输入手机号",
                            keyboardType = KeyboardType.Phone,
                            enabled = !isBusy,
                            leading = {
                                PhoneIcon(
                                    modifier = Modifier.size(18.dp, 22.dp),
                                    contentDescription = "手机号",
                                )
                            },
                        )
                        AuthLineField(
                            value = code,
                            onValueChange = {
                                code = it.filter(Char::isDigit).take(6)
                                onClearError()
                            },
                            placeholder = "请输入验证码",
                            keyboardType = KeyboardType.Number,
                            enabled = !isBusy,
                            leading = {
                                PeopleSafeIcon(
                                    modifier = Modifier.size(20.dp, 22.dp),
                                    contentDescription = "验证码",
                                )
                            },
                            trailing = {
                                VerificationCodeAction(
                                    countdown = codeCountdown,
                                    loading = requestingCode,
                                    enabled = Validators.isPhone(phone),
                                    onClick = {
                                        onRequestCode(
                                            phone,
                                            VerificationPurpose.REGISTER,
                                        ) { codeCountdown = it }
                                    },
                                )
                            },
                        )
                        AuthLineField(
                            value = password,
                            onValueChange = {
                                password = it.take(64)
                                onClearError()
                            },
                            placeholder = "请输入密码，至少8位",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            enabled = !isBusy,
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
                        PasswordStrengthBar(password, Modifier.padding(horizontal = 8.dp))
                        AuthStatusBanner(errorMessage, Modifier.padding(top = 5.dp))
                        Spacer(Modifier.height(6.dp))
                        AgreementRow(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            onOpenAgreement = onOpenAgreement,
                            onOpenPrivacy = onOpenPrivacy,
                        )
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = if (operation == AuthOperation.Register) "正在注册…" else "注 册",
                            onClick = {
                                val selectedAge = ageBand ?: return@PrimaryButton
                                onRegister(phone, code, password, selectedAge)
                            },
                            enabled = mainReady,
                            loading = operation == AuthOperation.Register,
                            modifier = Modifier.width(310.dp).align(Alignment.CenterHorizontally),
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
private fun AgeSelector(
    selected: AgeBand?,
    enabled: Boolean,
    onSelected: (AgeBand) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AuthDimens.inputH)
                .padding(horizontal = 8.dp)
                .clickable(enabled = enabled) { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthAssetIcon(
                resource = R.drawable.ic_age_scroll,
                contentDescription = "年龄段",
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = selected?.label ?: "请选择年龄段",
                color = if (selected == null) AuthPalette.Placeholder else AuthPalette.TextDark,
                style = TextStyle(fontFamily = YaHei, fontSize = 15.sp),
                modifier = Modifier.weight(1f),
            )
            Image(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = "展开年龄段",
                modifier = Modifier.size(20.dp),
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(AuthPalette.InputBorder)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = AuthPalette.BgCream,
        ) {
            AgeBand.entries.forEach { age ->
                DropdownMenuItem(
                    text = {
                        Text(
                            age.label,
                            color = AuthPalette.TextDark,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        )
                    },
                    leadingIcon = {
                        AuthAssetIcon(
                            resource = R.drawable.ic_age_scroll,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(age)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun RegisterScreenPreview() {
    JianghuTheme {
        RegisterScreen(
            onRequestCode = { _, _, _ -> },
            onRegister = { _, _, _, _ -> },
            onOpenAgreement = {},
            onOpenPrivacy = {},
            onBack = {},
        )
    }
}
