package com.jueqiao.jianghu.ui.screens.privacy

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
fun PrivacyScreen(onBack: () -> Unit) {
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
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        contentDescription = "返回",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onBack),
                    )
                    Text(
                        text = "隐私政策",
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
                    text = PRIVACY_POLICY_TEXT,
                    color = AuthPalette.TextDark.copy(alpha = 0.78f),
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 23.sp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private val PRIVACY_POLICY_TEXT = """
    更新日期：2026年8月
    生效日期：2026年8月

    一、我们处理的信息
    为提供注册登录、安全验证、未成年人保护、学习记录与智能创作功能，我们会在对应功能启用时处理必要信息，包括手机号、年龄段、用户主动提交的创作描述与作品，以及保障服务安全所需的设备与错误日志。

    二、信息的使用目的
    上述信息仅用于完成身份验证、保存学习与创作结果、提供内容生成服务、保障账号与系统安全，以及改进产品稳定性。未启用对应功能时，我们不会无故收集相关信息。

    三、第三方处理
    当你主动使用短信验证或智能创作功能时，完成该功能所必需的最少信息可能会传输给相应服务提供方。你可以在设置中的“第三方信息共享清单”查看类别与使用场景。

    四、保存与保护
    我们会采用访问控制、传输加密和最小权限等措施保护信息，并仅在实现处理目的所需的期限内保存。登录凭据会使用系统安全存储能力保存在本机。

    五、你的权利
    你可以在设置中管理通知、声音等偏好，查看个人信息收集和第三方共享清单，也可以通过退出登录清除本机登录状态。账号资料的查询、更正或删除能力将在账号服务正式上线时提供。

    六、未成年人保护
    对需要监护人同意的用户，我们会在注册流程中进行年龄段确认和监护人验证，并以适合未成年人的方式提供产品功能。

    说明：本页面为当前产品版本的功能说明。正式发布前应由项目运营与合规负责人结合实际服务提供方、保存期限和联系方式完成最终审定。
""".trimIndent()
