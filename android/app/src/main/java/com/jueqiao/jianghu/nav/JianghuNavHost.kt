package com.jueqiao.jianghu.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
import com.jueqiao.jianghu.ui.screens.forgot.ForgotScreen
import com.jueqiao.jianghu.ui.screens.home.HomeScreen
import com.jueqiao.jianghu.ui.screens.login.LoginScreen
import com.jueqiao.jianghu.ui.screens.privacy.PrivacyScreen
import com.jueqiao.jianghu.ui.screens.register.RegisterScreen
import com.jueqiao.jianghu.ui.screens.splash.SplashScreen
import com.jueqiao.jianghu.ui.screens.xingnang.XingnangScreen
import com.jueqiao.jianghu.ui.screens.xiulian.XiulianScreen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen

@Composable
fun JianghuNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.Login,
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
                onOpenXiulian  = { navController.navigate(Routes.Xiulian) },
                onOpenXingnang = { navController.navigate(Routes.Xingnang) },
                onOpenZaowu    = { navController.navigate(Routes.Zaowu) },
                onOpenDahui    = { navController.navigate(Routes.Dahui) },
            )
        }

        composable(Routes.Xiulian)  { XiulianScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.Xingnang) { XingnangScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.Zaowu)    { ZaowuScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.Dahui)    { DahuiScreen(onBack = { navController.popBackStack() }) }
    }
}