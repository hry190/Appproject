package com.jueqiao.jianghu.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.chatresult.ChatResultScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
import com.jueqiao.jianghu.ui.screens.yanwuchang.YanwuchangScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.YanwuchangVideoScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment1Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment2Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideomy.YanwuchangVideoMyScreen
import com.jueqiao.jianghu.ui.screens.forgot.ForgotScreen
import com.jueqiao.jianghu.ui.screens.home.ChallengeScreen
import com.jueqiao.jianghu.ui.screens.home.Home1Screen
import com.jueqiao.jianghu.ui.screens.home.HomeScreen
import com.jueqiao.jianghu.ui.screens.home.LuggageScreen
import com.jueqiao.jianghu.ui.screens.home.SettingsScreen
import com.jueqiao.jianghu.ui.screens.login.LoginScreen
import com.jueqiao.jianghu.ui.screens.privacy.PrivacyScreen
import com.jueqiao.jianghu.ui.screens.register.RegisterScreen
import com.jueqiao.jianghu.ui.screens.splash.SplashScreen
import com.jueqiao.jianghu.ui.screens.xiulian.XiulianScreen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen
import com.jueqiao.jianghu.ui.screens.gongfang.GongfangScreen
import com.jueqiao.jianghu.ui.screens.shengtu.ShengtuScreen
import com.jueqiao.jianghu.ui.screens.picture.PictureScreen
import com.jueqiao.jianghu.ui.screens.yaosu.YaosuScreen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan.ChuangzuodanganScreen

@Composable
fun JianghuNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.ChatResultPattern,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onTap = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onLogin         = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onOpenForgot    = { navController.navigate(Routes.Forgot) },
                onOpenRegister  = { navController.navigate(Routes.Register) },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = { navController.popBackStack() },
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onRegistered    = { navController.popBackStack(Routes.Login, inclusive = false) },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = { navController.popBackStack() },
            )
        }

        composable(Routes.Forgot) {
            ForgotScreen(
                onSubmitted     = { navController.popBackStack(Routes.Login, inclusive = false) },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Agreement) {
            AgreementScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Privacy) {
            PrivacyScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onOpenHome1   = { navController.navigate(Routes.Home1) },
                onOpenLuggage = { navController.navigate(Routes.Luggage) },
            )
        }

        composable(Routes.Home1) {
            Home1Screen(
                onOpenXiulian   = { navController.navigate(Routes.Xiulian) },
                onOpenLuggage   = { navController.navigate(Routes.Luggage) },
                onOpenZaowu     = { navController.navigate(Routes.Zaowu) },
                onOpenSettings  = { navController.navigate(Routes.Settings) },
                onOpenChallenge = { navController.navigate(Routes.Challenge) },
                onOpenDahui     = { navController.navigate(Routes.Dahui) },
                onPandaClick    = {
                    // 跳 Home 并重置状态,保证 chatStep 从 1 开始
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.Xiulian)  {
            XiulianScreen(
                onBack         = { navController.popBackStack() },
                onOpenLuggage  = { navController.navigate(Routes.Luggage) },
                onOpenZaowu    = { navController.navigate(Routes.Zaowu) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenTask     = { /* TODO:任务页 */ },
            )
        }
        composable(Routes.Zaowu)    {
            ZaowuScreen(
                onBack         = {
                    // 直接跳到首页1
                    navController.navigate(Routes.Home1) {
                        popUpTo(Routes.Zaowu) { inclusive = true }
                    }
                },
                onOpenGongfang = { navController.navigate(Routes.Gongfang) },
            )
        }
        composable(Routes.Dahui)    {
            DahuiScreen(
                onBack          = { navController.popBackStack() },
                onOpenYanwuchang = { navController.navigate(Routes.Yanwuchang) },
            )
        }
        composable(Routes.Yanwuchang) {
            YanwuchangScreen(
                onBack                  = { navController.popBackStack() },
                onOpenYanwuchangVideo   = { navController.navigate(Routes.YanwuchangVideo) },
            )
        }
        // 演武场视频首页 — 5 个 Tab(推荐/艺术/科学/数学/语文)在同一个 Composable 内
        //   切换 Tab 不调用 NavController.navigate,避免页面重建和明显的切换动画
        //   Tab 选中态、点赞/收藏状态都在屏幕内部 remember 中,跨 Tab 不丢失
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                onBack          = { navController.popBackStack() },
                onOpenComment   = { navController.navigate(Routes.YanwuchangVideoComment1) },
                onOpenMy        = { navController.navigate(Routes.YanwuchangVideoMy) },
            )
        }
        // 演武场视频 — "我的"页(点击演武场视频首页底部导航栏"我的"图标进入)
        composable(Routes.YanwuchangVideoMy) {
            YanwuchangVideoMyScreen(
                onBack = { navController.popBackStack() },
            )
        }
        // 演武场视频 — 评论1 页(背景图:室内家园要求 1.png)
        composable(Routes.YanwuchangVideoComment1) {
            YanwuchangVideoComment1Screen(
                onBack          = { navController.popBackStack() },
                onOpenExpanded  = { navController.navigate(Routes.YanwuchangVideoComment2) },
            )
        }
        // 演武场视频 — 评论2 页(点"放大缩小"图标;背景图:未标题-1 69.png)
        composable(Routes.YanwuchangVideoComment2) {
            YanwuchangVideoComment2Screen(
                onBackToHome    = {
                    // X 关闭:直接 popBackStack 到 YanwuchangVideo 页面(演武场视频首页)
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onBackToComment = {
                    // 返回箭头:popBackStack 回到 YanwuchangVideoComment1(小评论框页)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.Gongfang) {
            GongfangScreen(
                onBack   = {
                    // 直接跳到作品创作页
                    navController.navigate(Routes.Zaowu) {
                        popUpTo(Routes.Gongfang) { inclusive = true }
                    }
                },
                onSearch = { query ->
                    if (query.isNotBlank()) {
                        navController.navigate(Routes.chatResult(query))
                    }
                },
                onContinueWork = { workId ->
                    // TODO: 后续补 EditWorkScreen 时改成
                    //   navController.navigate(Routes.editWork(workId))
                },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(
            Routes.ChatResultPattern,
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                defaultValue = ""
            }),
        ) { entry ->
            val encoded = entry.arguments?.getString("query") ?: ""
            val query = java.net.URLDecoder.decode(encoded, Charsets.UTF_8.name())
            ChatResultScreen(
                query = query,
                onBack = {
                    // 直接跳到工坊页(避开 popBackStack 在 startDestination 上的不可靠行为)
                    navController.navigate(Routes.Gongfang) {
                        popUpTo(Routes.ChatResultPattern) { inclusive = true }
                    }
                },
                onSearch = { newQuery ->
                    if (newQuery.isNotBlank()) {
                        navController.navigate(Routes.chatResult(newQuery))
                    }
                },
                onContinueWork = { workId ->
                    // TODO: 后续接 EditWorkScreen
                },
                onCreateWork = { navController.navigate(Routes.Shengtu) },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(Routes.Shengtu) {
            ShengtuScreen(
                onBack      = {
                    android.util.Log.d("Shengtu", "onBack called, popping")
                    val popped = navController.popBackStack()
                    android.util.Log.d("Shengtu", "popBackStack returned: $popped")
                    if (!popped) {
                        android.util.Log.w("Shengtu", "Stack empty, navigating to ChatResult manually")
                        navController.navigate(Routes.chatResult("")) {
                            popUpTo(Routes.Shengtu) { inclusive = true }
                        }
                    }
                },
                onCreateWork = { navController.navigate(Routes.Picture) },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(Routes.Picture) {
            PictureScreen(
                onBack      = { navController.popBackStack() },
                onCreateWork = { navController.navigate(Routes.Yaosu) },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(Routes.Yaosu) {
            YaosuScreen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(Routes.Chuangzuodangan) {
            ChuangzuodanganScreen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
            )
        }

        // 行囊页(Figma 设计) — 点击首页1的"行囊"按钮跳转
        composable(Routes.Luggage) {
            LuggageScreen(
                onBack         = { navController.popBackStack() },
                onOpenZaowu    = { navController.navigate(Routes.Zaowu) },
            )
        }

        // 设置页(Figma 设计) — 点击首页1的"设置"图标跳转
        composable(Routes.Settings) {
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                onOpenWorks        = { /* TODO: 作品页 */ },
                onOpenProgress     = { /* TODO: 进度页/弹窗 */ },
                onOpenTask         = { /* TODO: 任务页 */ },
                onOpenAccount      = { /* TODO: 账号管理 */ },
                onOpenMessage      = { /* TODO: 消息设置 */ },
                onOpenGeneral      = { /* TODO: 通用设置 */ },
                onOpenSound        = { /* TODO: 声音调节 */ },
                onOpenBlacklist    = { /* TODO: 黑名单管理 */ },
                onOpenPrivacy      = { navController.navigate(Routes.Privacy) },
                onOpenAgreement    = { navController.navigate(Routes.Agreement) },
                onOpenCollection   = { /* TODO: 个人信息收集清单 */ },
                onOpenSharing      = { /* TODO: 第三方信息共享清单 */ },
                onOpenHelp         = { /* TODO: 帮助中心 */ },
                onOpenAbout        = { /* TODO: 关于 */ },
                onOpenDataRecovery = { /* TODO: 数据恢复 */ },
                onSwitchAccount    = { /* TODO: 切换账号 */ },
                onLogout           = { /* TODO: 退出登录 */ },
            )
        }

        // 首页挑战页(Figma 设计) — 点击"任务"展开栏的"挑战"文本跳转
        composable(Routes.Challenge) {
            ChallengeScreen(
                onBack        = { navController.popBackStack() },
                onOpenZaowu   = { navController.navigate(Routes.Zaowu) },
                onOpenProgress = { /* TODO:进度弹窗或页面 */ },
            )
        }
    }
}
