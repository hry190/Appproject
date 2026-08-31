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
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.YanwuchangVideoCategoryScreen
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
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                onBack = { navController.popBackStack() },
                onOpenArt     = { navController.navigate(Routes.YanwuchangVideoArt) },
                onOpenScience = { navController.navigate(Routes.YanwuchangVideoScience) },
                onOpenMath    = { navController.navigate(Routes.YanwuchangVideoMath) },
                onOpenChinese = { navController.navigate(Routes.YanwuchangVideoChinese) },
            )
        }
        // 4 个学科分类页 — 内容/布局与 YanwuchangVideoScreen 一致,仅顶部"推荐"标签替换为当前学科名
        //   - 顶部 Tab 选中态:字号 20sp,容器 56×28dp,文字背景图 62×47dp
        //   - 4 个 Tab 互相跳转,左上角 / 系统返回键回退到 YanwuchangVideoScreen
        composable(Routes.YanwuchangVideoArt) {
            YanwuchangVideoCategoryScreen(
                category    = "艺术",
                onBack      = { navController.popBackStack() },
                onOpenArt     = { navController.navigate(Routes.YanwuchangVideoArt)     { launchSingleTop = true } },
                onOpenScience = { navController.navigate(Routes.YanwuchangVideoScience) { launchSingleTop = true } },
                onOpenMath    = { navController.navigate(Routes.YanwuchangVideoMath)    { launchSingleTop = true } },
                onOpenChinese = { navController.navigate(Routes.YanwuchangVideoChinese) { launchSingleTop = true } },
            )
        }
        composable(Routes.YanwuchangVideoScience) {
            YanwuchangVideoCategoryScreen(
                category    = "科学",
                onBack      = { navController.popBackStack() },
                onOpenArt     = { navController.navigate(Routes.YanwuchangVideoArt)     { launchSingleTop = true } },
                onOpenScience = { navController.navigate(Routes.YanwuchangVideoScience) { launchSingleTop = true } },
                onOpenMath    = { navController.navigate(Routes.YanwuchangVideoMath)    { launchSingleTop = true } },
                onOpenChinese = { navController.navigate(Routes.YanwuchangVideoChinese) { launchSingleTop = true } },
            )
        }
        composable(Routes.YanwuchangVideoMath) {
            YanwuchangVideoCategoryScreen(
                category    = "数学",
                onBack      = { navController.popBackStack() },
                onOpenArt     = { navController.navigate(Routes.YanwuchangVideoArt)     { launchSingleTop = true } },
                onOpenScience = { navController.navigate(Routes.YanwuchangVideoScience) { launchSingleTop = true } },
                onOpenMath    = { navController.navigate(Routes.YanwuchangVideoMath)    { launchSingleTop = true } },
                onOpenChinese = { navController.navigate(Routes.YanwuchangVideoChinese) { launchSingleTop = true } },
            )
        }
        composable(Routes.YanwuchangVideoChinese) {
            YanwuchangVideoCategoryScreen(
                category    = "语文",
                onBack      = { navController.popBackStack() },
                onOpenArt     = { navController.navigate(Routes.YanwuchangVideoArt)     { launchSingleTop = true } },
                onOpenScience = { navController.navigate(Routes.YanwuchangVideoScience) { launchSingleTop = true } },
                onOpenMath    = { navController.navigate(Routes.YanwuchangVideoMath)    { launchSingleTop = true } },
                onOpenChinese = { navController.navigate(Routes.YanwuchangVideoChinese) { launchSingleTop = true } },
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
