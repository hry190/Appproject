package com.jueqiao.jianghu.ui.screens.agreement

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.SettingsPaperSurface
import com.jueqiao.jianghu.ui.theme.AuthPalette
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun AgreementScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        SettingsPaperSurface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Header
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        contentDescription = "返回",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onBack),
                    )
                    Text(
                        text = "用户协议",
                        color = AuthPalette.TextDark,
                        style = TextStyle(
                            fontFamily = YaHei,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        ),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = USER_AGREEMENT_TEXT,
                    color = AuthPalette.TextDark.copy(alpha = 0.78f),
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 23.sp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private val USER_AGREEMENT_TEXT = """
    更新日期：2026年8月
    生效日期：2026年8月

    一、协议范围
    本协议适用于“机巧江湖”应用提供的学习、探索、作品创作和账号服务。使用应用前，请阅读并理解本协议及隐私政策。

    二、账号与安全
    用户应使用本人可正常接收验证码的手机号完成注册和验证，并妥善保管账号凭据。发现异常登录或账号被冒用时，应及时修改密码并退出可疑设备。

    三、使用规则
    用户不得利用本应用制作、传播违法违规、侵犯他人权益或危害未成年人身心健康的内容，不得干扰服务运行、绕过安全措施或批量滥用智能创作能力。

    四、智能生成内容
    智能生成结果可能存在不准确或不完整的情况。用户应在学习、展示或发布前自行核验，并确保使用方式不侵犯第三方知识产权、肖像权和隐私权。

    五、未成年人使用
    未成年人应在监护人指导下使用本应用。达到需要监护人同意的年龄条件时，必须完成应用提供的监护人验证流程。

    六、服务变更
    为改善体验或保障安全，我们可能更新功能、维护服务或调整规则。影响用户重要权益的变更将在应用内以适当方式说明。

    七、责任与联系
    因设备、网络或不可抗力导致的暂时中断，我们会尽力恢复服务。正式发布前，项目方应补充有效的运营主体、客服与争议处理联系方式，并完成协议审定。
""".trimIndent()
