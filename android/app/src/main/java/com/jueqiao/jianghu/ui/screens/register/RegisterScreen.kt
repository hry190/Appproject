package com.jueqiao.jianghu.ui.screens.register

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.data.Validators
import com.jueqiao.jianghu.ui.components.AuthCheckbox
import com.jueqiao.jianghu.ui.components.CreamButton
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * Register screen — separate olive-themed design from login.
 * Mirrors RN register.tsx (3 password fields + agreement row).
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit,
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    val accountValid = account.length in 4..20
    val pwdValid = Validators.isPassword(password)
    val confirmValid = password == confirm && confirm.isNotEmpty()
    val canSubmit = accountValid && pwdValid && confirmValid && agreed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        // Background bamboo forest
        Image(
            painter = painterResource(R.drawable.img_register_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(44.dp).clickable(onClick = onBack))
                Image(
                    painter = painterResource(R.drawable.img_auth_panda_face),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "注册",
                    color = AuthPalette.TextDark,
                    style = TextStyle(
                        fontFamily = YaHei,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 5.sp,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "WELCOME!",
                modifier = Modifier.padding(start = 16.dp),
                color = AuthPalette.TextDark,
                style = TextStyle(
                    fontFamily = YaHei,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        color = AuthPalette.Olive,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    )
                    .padding(20.dp),
            ) {
                Column {
                    RegisterField(
                        label = "账号",
                        value = account,
                        onValueChange = { account = it.take(20) },
                        placeholder = "请输入账号 (4-20 位)",
                        keyboardType = KeyboardType.Text,
                        leading = { PersonGlyph() },
                    )
                    if (account.isNotEmpty() && !accountValid) {
                        Text(
                            text = "账号需 4-20 位",
                            color = AuthPalette.ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    RegisterField(
                        label = "设置密码",
                        value = password,
                        onValueChange = { password = it.take(24) },
                        placeholder = "请输入密码 (6-24 位)",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        leading = { ShieldGlyph() },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RegisterField(
                        label = "确定密码",
                        value = confirm,
                        onValueChange = { confirm = it.take(24) },
                        placeholder = "请再次输入密码",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        leading = { ShieldGlyph() },
                    )
                    if (confirm.isNotEmpty() && !confirmValid) {
                        Text(
                            text = "两次输入的密码不一致",
                            color = AuthPalette.ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CreamButton(
                        text = "注册",
                        onClick = onRegistered,
                        enabled = canSubmit,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AuthCheckbox(checked = agreed, onCheckedChange = { agreed = it })
                        Spacer(modifier = Modifier.size(8.dp))
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
                }
            }
        }
    }
}

@Composable
private fun PersonGlyph() {
    // Approximation of the inline PersonGlyph from RN register.tsx
    Image(
        painter = painterResource(R.drawable.img_auth_panda_face),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun ShieldGlyph() {
    Image(
        painter = painterResource(R.drawable.ic_people_safe),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
    )
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: @Composable () -> Unit,
) {
    Column {
        Text(
            text = label,
            color = AuthPalette.TextDark,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Spacer(modifier = Modifier.size(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = AuthPalette.Placeholder,
                            style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
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
            }
        }
    }
}