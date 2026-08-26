package com.jueqiao.jianghu.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
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

@Composable
fun JianghuNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.Home1,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onTap = {
                    navController.navigate(Routes.Home1) {
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
                onOpenDahui     = { navController.navigate(Routes.Dahui) },
                onOpenSettings  = { navController.navigate(Routes.Settings) },
                onOpenChallenge = { navController.navigate(Routes.Challenge) },
            )
        }

        composable(Routes.Xiulian)  {
            XiulianScreen(
                onBack         = { navController.popBackStack() },
                onOpenZaowu    = { navController.navigate(Routes.Zaowu) },
                onOpenDahui    = { navController.navigate(Routes.Dahui) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenProgress = { /* TODO:进度弹窗或页面 */ },
                onOpenTask     = { /* TODO:任务页 */ },
            )
        }
        composable(Routes.Zaowu)    { ZaowuScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.Dahui)    { DahuiScreen(onBack = { navController.popBackStack() }) }

        // 行囊页(Figma 设计) — 点击首页1的"行囊"按钮跳转
        composable(Routes.Luggage) {
            LuggageScreen(
                onBack         = { navController.popBackStack() },
                onOpenZaowu    = { navController.navigate(Routes.Zaowu) },
                onOpenDahui    = { navController.navigate(Routes.Dahui) },
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
                onOpenDahui   = { navController.navigate(Routes.Dahui) },
                onOpenProgress = { /* TODO:进度弹窗或页面 */ },
            )
        }
    }
}
