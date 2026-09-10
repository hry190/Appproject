package com.jueqiao.jianghu.nav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.JianghuApp
import com.jueqiao.jianghu.auth.AuthOperation
import com.jueqiao.jianghu.auth.AuthViewModel
import com.jueqiao.jianghu.auth.AuthViewModelFactory
import com.jueqiao.jianghu.auth.VerificationPurpose
import com.jueqiao.jianghu.creation.CreationViewModel
import com.jueqiao.jianghu.creation.CreationViewModelFactory
import com.jueqiao.jianghu.distribution.DistributionViewModel
import com.jueqiao.jianghu.distribution.DistributionViewModelFactory
import com.jueqiao.jianghu.conference.ConferenceViewModel
import com.jueqiao.jianghu.conference.ConferenceViewModelFactory
import com.jueqiao.jianghu.luggage.LuggageViewModel
import com.jueqiao.jianghu.luggage.LuggageViewModelFactory
import com.jueqiao.jianghu.luggage.PrivacySettingsPatchDto
import com.jueqiao.jianghu.luggage.RetrySessionDto
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceCollectionsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceHubScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceLettersScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceMatchScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceRequestsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceWorkScreen
import com.jueqiao.jianghu.ui.screens.forgot.ForgotScreen
import com.jueqiao.jianghu.ui.screens.home.ChallengeScreen
import com.jueqiao.jianghu.ui.screens.home.Home1Screen
import com.jueqiao.jianghu.ui.screens.home.HomeScreen
import com.jueqiao.jianghu.ui.screens.home.LuggageScreen
import com.jueqiao.jianghu.ui.screens.home.SettingsScreen
import com.jueqiao.jianghu.ui.screens.luggage.BadgesScreen
import com.jueqiao.jianghu.ui.screens.luggage.CreationDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.CreationsScreen
import com.jueqiao.jianghu.ui.screens.luggage.EvidenceScreen
import com.jueqiao.jianghu.ui.screens.luggage.ManualDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.ManualsScreen
import com.jueqiao.jianghu.ui.screens.luggage.LearningTrialScreen
import com.jueqiao.jianghu.ui.screens.luggage.MistakeDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.MistakesScreen
import com.jueqiao.jianghu.ui.screens.luggage.PrivacySafetyScreen
import com.jueqiao.jianghu.ui.screens.luggage.RetryTrialScreen
import com.jueqiao.jianghu.ui.screens.settings.SettingsDetailScreen
import com.jueqiao.jianghu.ui.screens.settings.SettingsPage
import com.jueqiao.jianghu.ui.screens.login.LoginComponentsFadeMillis
import com.jueqiao.jianghu.ui.screens.login.LoginMistTransition
import com.jueqiao.jianghu.ui.screens.login.LoginScreen
import com.jueqiao.jianghu.ui.screens.login.MistCoveredHoldMillis
import com.jueqiao.jianghu.ui.screens.login.MistDisperseMillis
import com.jueqiao.jianghu.ui.screens.login.MistGatherMillis
import com.jueqiao.jianghu.ui.screens.privacy.PrivacyScreen
import com.jueqiao.jianghu.ui.screens.register.RegisterScreen
import com.jueqiao.jianghu.ui.screens.splash.SplashScreen
import com.jueqiao.jianghu.ui.screens.xiulian.XiulianScreen
import com.jueqiao.jianghu.ui.screens.gunlun1.Gunlun1Screen
import com.jueqiao.jianghu.ui.screens.gunlun2.Gunlun2Screen
import com.jueqiao.jianghu.ui.screens.gunlun3.Gunlun3Screen
import com.jueqiao.jianghu.ui.screens.gunlun4.Gunlun4Screen
import com.jueqiao.jianghu.ui.screens.gunlun5.Gunlun5Screen
import com.jueqiao.jianghu.ui.screens.gunlun6.Gunlun6Screen
import com.jueqiao.jianghu.ui.screens.gunlun7.Gunlun7Screen
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Screen
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Screen
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Screen
import com.jueqiao.jianghu.ui.screens.learning.LearningScreen
import com.jueqiao.jianghu.ui.screens.learning2.Learning2Screen
import com.jueqiao.jianghu.ui.screens.learning3.Learning3Screen
import com.jueqiao.jianghu.ui.screens.learning4.Learning4Screen
import com.jueqiao.jianghu.ui.screens.pendingunlock.PendingUnlockScreen
import com.jueqiao.jianghu.ui.screens.gunlun8.Gunlun8Screen
import com.jueqiao.jianghu.ui.screens.gunlun9.Gunlun9Screen
import com.jueqiao.jianghu.ui.screens.gunlun10.Gunlun10Screen
import com.jueqiao.jianghu.ui.screens.gunlun11.Gunlun11Screen
import com.jueqiao.jianghu.ui.screens.gunlun12.Gunlun12Screen
import com.jueqiao.jianghu.ui.screens.gunlun13.Gunlun13Screen
import com.jueqiao.jianghu.ui.screens.gunlun14.Gunlun14Screen
import com.jueqiao.jianghu.ui.screens.gunlun15.Gunlun15Screen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen
import com.jueqiao.jianghu.ui.screens.gongfang.GongfangScreen
import com.jueqiao.jianghu.ui.screens.gongfang.toCreationMethodDraftDto
import com.jueqiao.jianghu.ui.screens.gongfang.toCreationMethodPlan
import com.jueqiao.jianghu.ui.screens.gongfang.toCreationResumeItem
import com.jueqiao.jianghu.ui.screens.shengtu.ShengtuScreen
import com.jueqiao.jianghu.ui.screens.unfinished.UnfinishedScreen
import com.jueqiao.jianghu.ui.screens.volume1.Volume1Screen
import com.jueqiao.jianghu.ui.screens.volume1part2.Volume1Part2Screen
import com.jueqiao.jianghu.ui.screens.volume1part3.Volume1Part3Screen
import com.jueqiao.jianghu.ui.screens.volume1part4.Volume1Part4Screen
import com.jueqiao.jianghu.ui.screens.volume1part5.Volume1Part5Screen
import com.jueqiao.jianghu.ui.screens.picture.CreationEditorScreen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan.ChuangzuodanganScreen
import com.jueqiao.jianghu.ui.screens.yanwuchang.YanwuchangScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.YanwuchangVideoScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideobrowserecord.YanwuchangVideoBrowseRecordScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment1Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment2Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideomy.YanwuchangVideoMyScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JianghuNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val application = LocalContext.current.applicationContext as JianghuApp
    val authFactory = remember(application) {
        AuthViewModelFactory(application.authRepository)
    }
    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val luggageFactory = remember(application) {
        LuggageViewModelFactory(application.luggageRepository)
    }
    val luggageViewModel: LuggageViewModel = viewModel(factory = luggageFactory)
    val luggageState by luggageViewModel.uiState.collectAsStateWithLifecycle()
    val luggageDetailState by luggageViewModel.detailState.collectAsStateWithLifecycle()
    val creationFactory = remember(application) {
        CreationViewModelFactory(application, application.luggageRepository)
    }
    val creationViewModel: CreationViewModel = viewModel(factory = creationFactory)
    val creationState by creationViewModel.state.collectAsStateWithLifecycle()
    val distributionFactory = remember(application) {
        DistributionViewModelFactory(application.luggageRepository)
    }
    val distributionViewModel: DistributionViewModel = viewModel(factory = distributionFactory)
    val distributionState by distributionViewModel.state.collectAsStateWithLifecycle()
    val conferenceFactory = remember(application) {
        ConferenceViewModelFactory(application.luggageRepository)
    }
    val conferenceViewModel: ConferenceViewModel = viewModel(factory = conferenceFactory)
    val conferenceState by conferenceViewModel.state.collectAsStateWithLifecycle()
    val transitionScope = rememberCoroutineScope()
    val loginContentAlpha = remember { Animatable(1f) }
    val mistPhase = remember { Animatable(0f) }
    var loginTransitionRunning by remember { mutableStateOf(false) }
    var animateHomeQuickActionsOnNextEntry by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.id) {
        conferenceViewModel.reset()
        creationViewModel.clearDerivative()
        conferenceViewModel.loadCapabilities()
        if (currentUser != null) conferenceViewModel.syncLetters()
    }

    val startLoginTransition: (String) -> Unit = { destination ->
        if (!loginTransitionRunning) {
            loginTransitionRunning = true
            transitionScope.launch {
                loginContentAlpha.snapTo(1f)
                mistPhase.snapTo(0f)
                loginContentAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = LoginComponentsFadeMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
                delay(80)
                mistPhase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = MistGatherMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
                delay(MistCoveredHoldMillis)

                navController.navigate(destination) {
                    popUpTo(Routes.Login) { inclusive = true }
                    launchSingleTop = true
                }
                delay(50)
                mistPhase.animateTo(
                    targetValue = 2f,
                    animationSpec = tween(
                        durationMillis = MistDisperseMillis,
                        easing = LinearOutSlowInEasing,
                    ),
                )
                mistPhase.snapTo(0f)
                loginContentAlpha.snapTo(1f)
                loginTransitionRunning = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController    = navController,
        startDestination = Routes.Splash,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onTap = {
                    authViewModel.bootstrap { authenticated ->
                        navController.navigate(
                            if (authenticated) Routes.Home1 else Routes.Login
                        ) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onLogin         = { phone, password ->
                    authViewModel.login(phone, password) {
                        animateHomeQuickActionsOnNextEntry = true
                        startLoginTransition(Routes.Home)
                    }
                },
                onOpenForgot    = {
                    authViewModel.clearFeedback()
                    navController.navigate(Routes.Forgot)
                },
                onOpenRegister  = {
                    authViewModel.clearFeedback()
                    navController.navigate(Routes.Register)
                },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                isSubmitting    = authState.operation == AuthOperation.Login,
                isTransitioning = loginTransitionRunning,
                contentAlpha    = loginContentAlpha.value,
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onRequestCode   = { phone, purpose, onCooldown ->
                    authViewModel.requestCode(phone, purpose, onCooldown)
                },
                onVerifyGuardian = { childPhone, guardianPhone, code, onVerified ->
                    authViewModel.verifyGuardianConsent(
                        childPhone,
                        guardianPhone,
                        code,
                        onVerified,
                    )
                },
                onRegister      = { phone, code, password, ageBand, guardianToken ->
                    authViewModel.register(
                        phone,
                        code,
                        password,
                        ageBand,
                        guardianToken,
                    ) {
                        animateHomeQuickActionsOnNextEntry = true
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = { navController.popBackStack() },
                operation       = authState.operation,
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Forgot) {
            ForgotScreen(
                onSubmitted     = { navController.popBackStack(Routes.Login, inclusive = false) },
                onRequestCode   = { phone, onCooldown ->
                    authViewModel.requestCode(
                        phone,
                        VerificationPurpose.RESET_PASSWORD,
                        onCooldown,
                    )
                },
                onResetPassword = { phone, code, newPassword, onSuccess ->
                    authViewModel.resetPassword(phone, code, newPassword, onSuccess)
                },
                onBack          = {
                    authViewModel.clearFeedback()
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
                operation       = authState.operation,
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
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
                onOpenHome1 = {
                    // 引导结束进入首页时，重新请求一次八个入口的进场动效。
                    // 从各功能页返回 Home1 不会走此分支，因此仍保持直接显示。
                    animateHomeQuickActionsOnNextEntry = true
                    navController.navigate(Routes.Home1)
                },
            )
        }

        composable(Routes.Home1) {
            LaunchedEffect(Unit) { conferenceViewModel.syncLetters() }
            Home1Screen(
                animateQuickActionsEntrance = animateHomeQuickActionsOnNextEntry,
                quickActionsEntranceReady = !loginTransitionRunning,
                onQuickActionsEntranceConsumed = {
                    animateHomeQuickActionsOnNextEntry = false
                },
                onOpenXiulian   = { navController.navigate(Routes.Xiulian) },
                onOpenLuggage   = { navController.navigate(Routes.Luggage) },
                // 每次从首页进入作品创作都创建新的引导页实例，确保气泡与工坊入口重置。
                onOpenZaowu     = { navController.navigate(Routes.Zaowu) },
                onOpenSettings  = { navController.navigate(Routes.Settings) },
                onOpenChallenge = {
                    navController.navigate(
                        if (conferenceState.conferenceEnabled == false) {
                            Routes.Challenge
                        } else {
                            Routes.DahuiLetters
                        },
                    )
                },
                onOpenDahui     = { navController.navigate(Routes.Dahui) },
                dahuiEnabled = conferenceState.conferenceEnabled != false,
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
            )
        }

        composable(Routes.Xiulian)  {
            LaunchedEffect(Unit) { conferenceViewModel.syncLetters() }
            LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
            XiulianScreen(
                onBack         = { navController.popBackStack() },
                onOpenLuggage  = { navController.navigate(Routes.Luggage) },
                onOpenManuals  = { navController.navigate(Routes.luggageManuals(null)) },
                onOpenLearning = { navController.navigate(Routes.LuggageGrowth) },
                onOpenTrials   = { navController.navigate(Routes.Shilian) },
                onOpenGunlun1  = { navController.navigate(Routes.Gunlun1) },
                onOpenRecommendedManual = { id -> navController.navigate(Routes.luggageManualDetail(id)) },
                learningOverview = luggageDetailState.learningOverview,
                onOpenWendao   = { navController.navigate(Routes.Home1) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenLetters  = {
                    navController.navigate(
                        if (conferenceState.conferenceEnabled == false) Routes.Challenge
                        else Routes.DahuiLetters,
                    )
                },
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
            )
        }
        composable(Routes.Shilian) {
            Houshan1Screen(
                onBack = { navController.popBackStack() },
                onOpenHoushan2 = { navController.navigate(Routes.Shilian2) },
            )
        }
        composable(Routes.Shilian2) {
            Houshan2Screen(
                onBack = { navController.popBackStack() },
                onOpenHoushan3 = { navController.navigate(Routes.Shilian3) },
            )
        }
        composable(Routes.Shilian3) {
            Houshan3Screen(
                onBack = { navController.popBackStack() },
                onOpenUnfinished = { navController.navigate(Routes.Unfinished) },
            )
        }
        composable(Routes.Unfinished) {
            UnfinishedScreen(
                onBack = { navController.popBackStack() },
                onOpenGunlun1 = {
                    navController.navigate(Routes.Gunlun1) {
                        popUpTo(Routes.Gunlun1) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Houshan) {
            LearningScreen(
                onBack = { navController.popBackStack() },
                onOpenLearning2 = { navController.navigate(Routes.Learning2) },
            )
        }
        composable(Routes.Learning2) {
            Learning2Screen(
                onBack = { navController.popBackStack() },
                onOpenLearning3 = { navController.navigate(Routes.Learning3) },
                onOpenLearning4 = { navController.navigate(Routes.Learning4) },
            )
        }
        composable(Routes.Learning3) {
            Learning3Screen(
                onBack = { navController.popBackStack() },
                onOpenPendingUnlock = { navController.navigate(Routes.PendingUnlock) },
            )
        }
        composable(Routes.Learning4) {
            Learning4Screen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Learning3) {
            Learning3Screen(
                onBack = { navController.popBackStack() },
                onOpenPendingUnlock = { navController.navigate(Routes.PendingUnlock) },
            )
        }
        composable(Routes.PendingUnlock) {
            PendingUnlockScreen(
                onBack = { navController.popBackStack() },
                onOpenGunlun1 = { navController.navigate(Routes.Gunlun1) },
            )
        }
        composable(Routes.Gunlun1) {
            Gunlun1Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun2 = { navController.navigate(Routes.Gunlun2) },
                // 滚轮1 的"修\n炼"按钮 → 学习1 页 (LearningScreen)
                onOpenLearning1 = { navController.navigate(Routes.Houshan) },
                // 滚轮1 的"后\n山"按钮 → 后山1 页 (Houshan1Screen, 原 ShilianScreen)
                onOpenHoushan1 = { navController.navigate(Routes.Shilian) },
            )
        }
        composable(Routes.Gunlun2) {
            Gunlun2Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun3 = { navController.navigate(Routes.Gunlun3) },
            )
        }
        composable(Routes.Gunlun3) {
            Gunlun3Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun4 = { navController.navigate(Routes.Gunlun4) },
            )
        }
        composable(Routes.Gunlun4) {
            Gunlun4Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun5 = { navController.navigate(Routes.Gunlun5) },
            )
        }
        composable(Routes.Gunlun5) {
            Gunlun5Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun6 = { navController.navigate(Routes.Gunlun6) },
            )
        }
        composable(Routes.Gunlun6) {
            Gunlun6Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun7 = { navController.navigate(Routes.Gunlun7) },
                onOpenVolume1 = { navController.navigate(Routes.Volume1) },
            )
        }
        composable(Routes.Volume1) {
            Volume1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part2 = { navController.navigate(Routes.Volume1Part2) },
            )
        }
        composable(Routes.Volume1Part2) {
            Volume1Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part3 = { navController.navigate(Routes.Volume1Part3) },
            )
        }
        composable(Routes.Volume1Part3) {
            Volume1Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part4 = { navController.navigate(Routes.Volume1Part4) },
            )
        }
        composable(Routes.Volume1Part4) {
            Volume1Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part5 = { navController.navigate(Routes.Volume1Part5) },
            )
        }
        composable(Routes.Volume1Part5) {
            Volume1Part5Screen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Gunlun7) {
            Gunlun7Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun8 = { navController.navigate(Routes.Gunlun8) },
            )
        }
        composable(Routes.Gunlun8) {
            Gunlun8Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun9 = { navController.navigate(Routes.Gunlun9) },
            )
        }
        composable(Routes.Gunlun9) {
            Gunlun9Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun10 = { navController.navigate(Routes.Gunlun10) },
            )
        }
        composable(Routes.Gunlun10) {
            Gunlun10Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun11 = { navController.navigate(Routes.Gunlun11) },
            )
        }
        composable(Routes.Gunlun11) {
            Gunlun11Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun12 = { navController.navigate(Routes.Gunlun12) },
            )
        }
        composable(Routes.Gunlun12) {
            Gunlun12Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun13 = { navController.navigate(Routes.Gunlun13) },
            )
        }
        composable(Routes.Gunlun13) {
            Gunlun13Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun14) },
            )
        }
        composable(Routes.Gunlun14) {
            Gunlun14Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun15) },
            )
        }
        composable(Routes.Gunlun15) {
            Gunlun15Screen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Zaowu)    {
            ZaowuScreen(
                onBack         = {
                    // 引导页的页面返回与系统返回都直接回首页，并移除本次引导实例。
                    navController.navigate(Routes.Home1) {
                        popUpTo(Routes.Home1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onOpenGongfang = {
                    // 进入创作台时移除引导页，保证任何返回路径都不会重新露出引导页。
                    navController.navigate(Routes.Gongfang) {
                        popUpTo(Routes.Zaowu) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Dahui) {
            DahuiScreen(
                onBack = { navController.popBackStack() },
                onOpenArena = { navController.navigate(Routes.Yanwuchang) },
            )
        }
        composable(Routes.Yanwuchang) {
            YanwuchangScreen(
                onBack = { navController.popBackStack() },
                onOpenWuhui = { navController.navigate(Routes.DahuiArena) },
                onOpenYanwuchangVideo = { navController.navigate(Routes.YanwuchangVideo) },
            )
        }
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                onBack = { navController.popBackStack() },
                onOpenComment = { navController.navigate(Routes.YanwuchangVideoComment) },
                onOpenMy = { navController.navigate(Routes.YanwuchangVideoMy) },
            )
        }
        composable(Routes.YanwuchangVideoMy) {
            YanwuchangVideoMyScreen(
                onBack = { navController.popBackStack() },
                onOpenWorks = {
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onOpenLikes = { /* No likes-list endpoint yet. */ },
                onOpenBrowseRecord = {
                    navController.navigate(Routes.YanwuchangVideoBrowseRecord)
                },
                onOpenMyClass = { /* No class-detail destination yet. */ },
            )
        }
        composable(Routes.YanwuchangVideoBrowseRecord) {
            YanwuchangVideoBrowseRecordScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.YanwuchangVideoComment) {
            YanwuchangVideoComment1Screen(
                onBack = { navController.popBackStack() },
                onOpenExpanded = {
                    navController.navigate(Routes.YanwuchangVideoCommentExpanded)
                },
            )
        }
        composable(Routes.YanwuchangVideoCommentExpanded) {
            YanwuchangVideoComment2Screen(
                onBackToHome = {
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onBackToComment = { navController.popBackStack() },
            )
        }
        composable(Routes.DahuiArena) {
            ConferenceHubScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadFeed,
                onLoadMore = { conferenceViewModel.loadFeed(append = true) },
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
                onOpenCollections = { navController.navigate(Routes.DahuiCollections) },
                onOpenRequests = { navController.navigate(Routes.DahuiRequests) },
                onOpenLetters = { navController.navigate(Routes.DahuiLetters) },
                onOpenMatch = { navController.navigate(Routes.DahuiMatch) },
            )
        }
        composable(
            Routes.DahuiWorkPattern,
            arguments = listOf(navArgument("publicationId") { type = NavType.StringType }),
        ) { entry ->
            val publicationId = entry.arguments?.getString("publicationId").orEmpty()
            ConferenceWorkScreen(
                publicationId = publicationId,
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadWork,
                onAddCollection = conferenceViewModel::addCollection,
                onCreateReview = conferenceViewModel::createReview,
                onDecideReview = conferenceViewModel::decideReview,
                onAdoptReview = conferenceViewModel::adoptReview,
                onReportReview = conferenceViewModel::reportReview,
                onCreateDerivativeRequest = conferenceViewModel::createDerivativeRequest,
                onOpenRevision = { projectId ->
                    navController.navigate(Routes.creationEditorProject(projectId))
                },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiCollections) {
            ConferenceCollectionsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadCollections,
                onRemove = conferenceViewModel::removeCollection,
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
            )
        }
        composable(Routes.DahuiRequests) {
            ConferenceRequestsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadDerivativeRequests,
                onDecision = conferenceViewModel::decideDerivativeRequest,
                onRevoke = conferenceViewModel::revokeDerivativeAuthorization,
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
                onCreateDerivative = { authorizationId, sourceTitle ->
                    creationViewModel.beginDerivative(authorizationId, sourceTitle)
                    navController.navigate(Routes.Gongfang)
                },
            )
        }
        composable(Routes.DahuiMatch) {
            ConferenceMatchScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatch,
                onJoin = conferenceViewModel::joinMatch,
                onExit = conferenceViewModel::exitMatch,
                onReport = { matchId, reason -> conferenceViewModel.reportMatch(matchId, reason) },
                onSubmitAnswer = conferenceViewModel::submitMatchAnswer,
                onCreateEvaluation = conferenceViewModel::createMatchEvaluation,
                onCreateReflection = conferenceViewModel::createMatchReflection,
                onStartNewMatch = conferenceViewModel::clearCompletedMatch,
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(
            Routes.DahuiMatchPattern,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
        ) { entry ->
            ConferenceMatchScreen(
                state = conferenceState,
                directMatchId = entry.arguments?.getString("matchId"),
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatch,
                onJoin = conferenceViewModel::joinMatch,
                onExit = conferenceViewModel::exitMatch,
                onReport = { matchId, reason -> conferenceViewModel.reportMatch(matchId, reason) },
                onSubmitAnswer = conferenceViewModel::submitMatchAnswer,
                onCreateEvaluation = conferenceViewModel::createMatchEvaluation,
                onCreateReflection = conferenceViewModel::createMatchReflection,
                onStartNewMatch = {
                    conferenceViewModel.clearCompletedMatch()
                    navController.navigate(Routes.DahuiMatch) { launchSingleTop = true }
                },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiLetters) {
            ConferenceLettersScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadLetters,
                onOpenLetter = { letter ->
                    conferenceViewModel.readLetter(letter)
                    when (letter.navigationTarget) {
                        "CONFERENCE_WORK" -> letter.navigationId?.let {
                            navController.navigate(Routes.dahuiWork(it))
                        }
                        "CONFERENCE_MATCH" -> letter.navigationId?.let {
                            navController.navigate(Routes.dahuiMatch(it))
                        }
                        "DERIVATIVE_REQUESTS" ->
                            navController.navigate(Routes.DahuiRequests)
                        else -> when (letter.actionType) {
                            "CONFERENCE_MATCH" -> letter.actionId?.let {
                                navController.navigate(Routes.dahuiMatch(it))
                            }
                            "DERIVATIVE_REQUEST", "DERIVATIVE_AUTHORIZATION" ->
                                navController.navigate(Routes.DahuiRequests)
                            "CONFERENCE_REVIEW" -> navController.navigate(Routes.DahuiArena)
                        }
                    }
                },
                onOpenPublicationInbox = { navController.navigate(Routes.Challenge) },
            )
        }
        composable(Routes.Gongfang) {
            val recentWorks = creationState.recentProjects
                .asSequence()
                .mapNotNull { it.toCreationResumeItem() }
                .distinctBy { it.projectId }
                .take(2)
                .toList()
            val openCreationArchive = {
                navController.navigate(Routes.Chuangzuodangan) {
                    launchSingleTop = true
                }
            }
            val analysisNotice = when {
                creationState.intentAnalysis?.safetyFlags?.contains(
                    "POSSIBLE_PERSONAL_INFORMATION"
                ) == true -> "提示：请检查并移除电话、住址、学校全名等个人信息。"
                creationState.intentAnalysis?.safetyFlags?.contains(
                    "MVP_MEDIA_FALLBACK"
                ) == true -> "当前版本先按图文作品落地；互动、视频能力将在后续阶段接入。"
                creationState.intentAnalysis?.questions?.isNotEmpty() == true ->
                    creationState.intentAnalysis?.questions?.first()
                else -> null
            }
            GongfangScreen(
                onBack   = {
                    creationViewModel.clearDerivative()
                    // 创作台的页面返回与系统返回都直接回首页，同时移除引导页与创作台。
                    navController.navigate(Routes.Home1) {
                        popUpTo(Routes.Home1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                // 新版创作流程在工坊页内展开，不再跳转到旧的搜索结果页。
                onAnalyzeIntent = creationViewModel::analyzeIntent,
                onContinueWork = { workId ->
                    navController.navigate(Routes.shengtuProject(workId))
                },
                onOpenChuangzuodangan = openCreationArchive,
                onOpenAllWorks = openCreationArchive,
                recentWorks = recentWorks,
                recentWorksLoading = creationState.loadingRecent,
                recentWorksMessage = creationState.recentError,
                derivativeSourceTitle = creationState.derivativeSourceTitle,
                onClearDerivative = creationViewModel::clearDerivative,
                onLoadRecentWorks = creationViewModel::loadRecentProjects,
                manualSources = creationState.manualSources,
                manualSourcesLoading = creationState.loadingManualSources,
                manualSourcesMessage = creationState.manualSourceError,
                onLoadCreationSources = creationViewModel::loadCreationSources,
                uploadingSketch = creationState.uploadingSketch,
                sketchSourceAssetId = creationState.sketchSource?.assetId,
                sketchSourceMessage = creationState.sketchSourceMessage,
                onUploadSketch = creationViewModel::uploadSketch,
                analyzingIntent = creationState.analyzingIntent,
                creationMethod = creationState.intentAnalysis
                    ?.methodDraft
                    ?.toCreationMethodPlan(),
                analysisMessage = creationState.analysisError,
                creatingProject = creationState.creatingProject,
                createProjectMessage = creationState.createError ?: analysisNotice,
                onConfirmNewProject = { title, idea, method ->
                    creationViewModel.createProject(
                        title,
                        idea,
                        method.toCreationMethodDraftDto(),
                    ) { project ->
                        navController.navigate(Routes.shengtuProject(project.id))
                    }
                },
            )
        }

        composable(
            Routes.ShengtuProjectPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            LaunchedEffect(projectId) {
                luggageViewModel.loadCreationDetail(projectId)
                creationViewModel.loadCreationSources()
                distributionViewModel.loadClassrooms()
            }
            val bundle = luggageDetailState.creationDetail
                ?.takeIf { it.project.id == projectId }
            val latestVersion = bundle?.versions?.maxByOrNull { it.versionNumber }
            val currentPublication = bundle?.project?.latestPublication
                ?.takeIf { it.creationVersionId == latestVersion?.id }
            val bundleGeneration = bundle?.imageGenerations?.items?.firstOrNull()
            val visibleGeneration = creationState.generationJob
                ?.takeIf { it.projectId == projectId }
                ?: bundleGeneration
            LaunchedEffect(projectId, bundleGeneration?.id, bundleGeneration?.status) {
                bundleGeneration?.let { job ->
                    creationViewModel.resumeImageGeneration(job) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                    }
                }
            }
            val initialPrompt = latestVersion?.layers
                ?.firstOrNull { it.kind == "TEXT" && !it.textContent.isNullOrBlank() }
                ?.textContent
                ?: bundle?.project?.description
            ShengtuScreen(
                projectId = projectId,
                projectTitle = bundle?.project?.title,
                currentVersionNumber = latestVersion?.versionNumber,
                currentVersionId = latestVersion?.id,
                currentStage = bundle?.project?.currentStage,
                toolCalls = bundle?.toolCalls.orEmpty(),
                testRecords = bundle?.testRecords.orEmpty(),
                manualSources = creationState.manualSources,
                methodManualPageIds = bundle?.method?.manualPageIds.orEmpty(),
                initialMethodSummary = bundle?.method?.let { method ->
                    buildString {
                        append(method.name)
                        append("：")
                        append(method.goal)
                        if (method.steps.isNotEmpty()) {
                            append("\n")
                            append(method.steps.joinToString(" → "))
                        }
                    }
                },
                learningCard = bundle?.learningCard,
                provenance = bundle?.provenance,
                sealCheck = bundle?.sealCheck,
                imageGenerations = bundle?.imageGenerations,
                activeGeneration = visibleGeneration,
                publicationStatus = currentPublication?.status,
                publicationId = currentPublication?.id,
                publicationVisibility = currentPublication?.visibility,
                classrooms = distributionState.classrooms,
                initialPrompt = initialPrompt,
                savingDraft = creationState.savingDraft,
                draftSaveMessage = creationState.draftSaveMessage
                    ?.takeIf { creationState.draftMessageProjectId == projectId },
                workflowBusy = creationState.workflowBusy,
                workflowMessage = creationState.workflowMessage
                    ?.takeIf { creationState.workflowMessageProjectId == projectId },
                generationBusy = creationState.generationBusy &&
                    creationState.generationProjectId == projectId,
                generationMessage = creationState.generationMessage
                    ?.takeIf { creationState.generationProjectId == projectId },
                onSaveDraft = { prompt ->
                    creationViewModel.saveTextDraft(
                        projectId = projectId,
                        parentVersionId = latestVersion?.id,
                        prompt = prompt,
                    ) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onRequestCoach = { prompt ->
                    creationViewModel.requestCoachReview(projectId, prompt) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onDecideCoach = { call, approve ->
                    creationViewModel.decideCoachReview(call, approve) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onRequestImageGeneration = { prompt, size, quality ->
                    val project = bundle?.project
                    val version = latestVersion
                    if (project != null && version != null) {
                        creationViewModel.requestImageGeneration(
                            projectId = projectId,
                            parentVersionId = version.id,
                            expectedProjectRevision = project.rowVersion,
                            prompt = prompt,
                            size = size,
                            quality = quality,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            luggageViewModel.refresh(force = true)
                            conferenceViewModel.invalidateFeed()
                        }
                    }
                },
                onRetryImageGeneration = { job ->
                    creationViewModel.retryImageGeneration(job) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                    }
                },
                onEnterTest = {
                    bundle?.project?.let { project ->
                        creationViewModel.moveCreationStage(
                            project = project,
                            toStage = "TEST",
                            reason = "当前版本制作完成，开始真实场景测试",
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onRecordTest = { scenario, result, notes, finding ->
                    latestVersion?.let { version ->
                        creationViewModel.recordCreationTest(
                            projectId = projectId,
                            creationVersionId = version.id,
                            scenario = scenario,
                            result = result,
                            notes = notes,
                            finding = finding,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onResolveIssue = { issue, resolution ->
                    creationViewModel.resolveCreationTestIssue(issue, resolution) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onEnterSeal = {
                    bundle?.project?.let { project ->
                        creationViewModel.moveCreationStage(
                            project = project,
                            toStage = "SEAL",
                            reason = "当前版本复测通过，且测试问题已经全部关闭",
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSaveLearningCard = { form ->
                    latestVersion?.let { version ->
                        creationViewModel.saveLearningCard(
                            projectId = projectId,
                            versionId = version.id,
                            manualPageIds = form.manualPageIds,
                            methodSummary = form.methodSummary,
                            unresolvedQuestionsText = form.unresolvedQuestions,
                            questionsConfirmed = form.questionsConfirmed,
                            rowVersion = bundle?.learningCard?.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSubmitMigrationEvidence = { manualPageIds, revisionReason ->
                    latestVersion?.let { version ->
                        creationViewModel.submitMigrationEvidence(
                            projectId = projectId,
                            creationVersionId = version.id,
                            manualPageIds = manualPageIds,
                            revisionReason = revisionReason,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSaveProvenance = { form ->
                    latestVersion?.let { version ->
                        creationViewModel.saveProvenance(
                            projectId = projectId,
                            versionId = version.id,
                            humanSummary = form.humanSummary,
                            aiUsed = form.aiUsed,
                            aiSummary = form.aiSummary,
                            aiProvider = form.aiProvider,
                            aiModel = form.aiModel,
                            aiAction = form.aiAction,
                            promptSummary = form.promptSummary,
                            aiResultModified = form.aiResultModified,
                            aigcLabelDeclared = form.aigcLabelDeclared,
                            externalSourceUrl = form.externalSourceUrl,
                            externalSourceAuthor = form.externalSourceAuthor,
                            externalLicense = form.externalLicense,
                            unresolvedRights = form.unresolvedRights,
                            rowVersion = bundle?.provenance?.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSaveSealCheck = { form ->
                    latestVersion?.let { version ->
                        creationViewModel.saveSealCheck(
                            projectId = projectId,
                            versionId = version.id,
                            workDescription = form.workDescription,
                            learningReflection = form.learningReflection,
                            nextImprovement = form.nextImprovement,
                            identityPrivacyConfirmed = form.identityPrivacyConfirmed,
                            contactPrivacyConfirmed = form.contactPrivacyConfirmed,
                            portraitRightsConfirmed = form.portraitRightsConfirmed,
                            rowVersion = bundle?.sealCheck?.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSubmit = { visibility, classroomId ->
                    latestVersion?.let { version ->
                        creationViewModel.submitCreation(
                            projectId = projectId,
                            versionId = version.id,
                            visibility = visibility,
                            targetClassroomId = classroomId,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            luggageViewModel.refresh(force = true)
                        }
                    }
                },
                onOpenPublishedWork = { publicationId ->
                    navController.navigate(Routes.dahuiWork(publicationId))
                },
                onCreateWork = {
                    if (latestVersion != null) {
                        navController.navigate(Routes.creationEditorProject(projectId))
                    }
                },
                onBack = { navController.popBackStack() },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
            )
        }

        composable(Routes.Chuangzuodangan) {
            ChuangzuodanganScreen(
                onBack = { navController.popBackStack() },
                onOpenCreationDesk = {
                    val returnedToDesk = navController.popBackStack(
                        route = Routes.Gongfang,
                        inclusive = false,
                    )
                    if (!returnedToDesk) {
                        navController.navigate(Routes.Gongfang) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }


        // 行囊页(Figma 设计) — 点击首页1的"行囊"按钮跳转
        composable(Routes.Luggage) {
            LaunchedEffect(Unit) { luggageViewModel.refresh() }
            LaunchedEffect(Unit) { conferenceViewModel.syncLetters() }
            LuggageScreen(
                uiState = luggageState,
                onBack = { navController.popBackStack() },
                onOpenZaowu = { navController.navigate(Routes.Zaowu) },
                onRefresh = { luggageViewModel.refresh(force = true) },
                onOpenBadges = { navController.navigate(Routes.LuggageBadges) },
                onOpenGrowth = { navController.navigate(Routes.LuggageGrowth) },
                onOpenManuals = { state -> navController.navigate(Routes.luggageManuals(state)) },
                onOpenManual = { id -> navController.navigate(Routes.luggageManualDetail(id)) },
                onOpenMistakes = { navController.navigate(Routes.LuggageMistakes) },
                onRetryMistake = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
                onOpenCreations = { navController.navigate(Routes.LuggageCreations) },
                onOpenCreation = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onContinueCreation = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onOpenEvidence = { navController.navigate(Routes.LuggageEvidence) },
                onOpenPrivacy = { navController.navigate(Routes.LuggagePrivacySafety) },
                onOpenLetters = {
                    navController.navigate(
                        if (conferenceState.conferenceEnabled == false) Routes.Challenge
                        else Routes.DahuiLetters,
                    )
                },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
            )
        }

        composable(
            Routes.CreationEditorProjectPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            LaunchedEffect(projectId) {
                creationViewModel.loadEditor(projectId)
            }
            val belongsToProject = creationState.editorProjectId == projectId
            val currentVersion = creationState.editorVersions
                .takeIf { belongsToProject }
                ?.maxByOrNull { it.versionNumber }
            CreationEditorScreen(
                projectTitle = creationState.editorProject
                    ?.takeIf { belongsToProject }
                    ?.title,
                version = currentVersion,
                assets = creationState.editorAssets.takeIf { belongsToProject }.orEmpty(),
                loading = creationState.editorLoading && belongsToProject,
                saving = creationState.editorSaving && belongsToProject,
                message = creationState.editorMessage.takeIf { belongsToProject },
                diff = creationState.editorDiff.takeIf { belongsToProject },
                exportBusy = creationState.editorExportBusy && belongsToProject,
                exportJob = creationState.editorExportJob.takeIf { belongsToProject },
                exportMessage = creationState.editorExportMessage.takeIf { belongsToProject },
                onBack = { navController.popBackStack() },
                onReload = { creationViewModel.loadEditor(projectId) },
                onSave = { parent, layers, reason ->
                    creationViewModel.saveEditorVersion(
                        projectId = projectId,
                        parent = parent,
                        layers = layers,
                        modificationReason = reason,
                    ) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                        conferenceState.work
                            ?.takeIf { it.projectId == projectId }
                            ?.let { conferenceViewModel.loadWork(it.publicationId) }
                    }
                },
                onCompare = creationViewModel::compareEditorWithPrevious,
                onExport = creationViewModel::exportEditorVersion,
                onDismissDiff = creationViewModel::clearEditorDiff,
            )
        }

        composable(Routes.LuggageBadges) {
            BadgesScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadBadges,
            )
        }

        composable(Routes.LuggageGrowth) {
            EvidenceScreen(
                title = "本周成长记录",
                weekOnly = true,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { category -> luggageViewModel.loadEvidence(category, weekOnly = true) },
                onLoadMore = { category -> luggageViewModel.loadMoreEvidence(category, weekOnly = true) },
            )
        }

        composable(Routes.LuggageEvidence) {
            EvidenceScreen(
                title = "学习证据",
                weekOnly = false,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { category -> luggageViewModel.loadEvidence(category) },
                onLoadMore = { category -> luggageViewModel.loadMoreEvidence(category) },
            )
        }

        composable(
            Routes.LuggageManualsPattern,
            arguments = listOf(navArgument("state") { type = NavType.StringType }),
        ) { entry ->
            val initialState = entry.arguments?.getString("state")?.takeUnless { it == "ALL" }
            ManualsScreen(
                state = luggageDetailState,
                initialState = initialState,
                onBack = { navController.popBackStack() },
                onLoad = { volume, query, state, favorites ->
                    luggageViewModel.loadManuals(volume, query, state, favorites)
                },
                onLoadMore = { volume, query, state, favorites ->
                    luggageViewModel.loadMoreManuals(volume, query, state, favorites)
                },
                onOpenManual = { id -> navController.navigate(Routes.luggageManualDetail(id)) },
                onToggleFavorite = luggageViewModel::toggleManualFavorite,
            )
        }

        composable(
            Routes.LuggageManualDetailPattern,
            arguments = listOf(navArgument("manualId") { type = NavType.StringType }),
        ) { entry ->
            val manualId = entry.arguments?.getString("manualId").orEmpty()
            ManualDetailScreen(
                manualId = manualId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadManualDetail,
                onOpenTrial = { trialId -> navController.navigate(Routes.learningTrial(trialId)) },
            )
        }

        composable(
            Routes.LearningTrialPattern,
            arguments = listOf(navArgument("trialId") { type = NavType.StringType }),
        ) { entry ->
            val trialId = entry.arguments?.getString("trialId").orEmpty()
            LearningTrialScreen(
                trialId = trialId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadLearningTrial,
                onSubmit = { prediction, answer, explanation ->
                    luggageViewModel.submitLearningTrial(trialId, prediction, answer, explanation)
                },
                onComplete = { navController.popBackStack() },
            )
        }

        composable(Routes.LuggageMistakes) {
            MistakesScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { luggageViewModel.loadMistakes() },
                onLoadMore = { luggageViewModel.loadMoreMistakes() },
                onOpen = { id -> navController.navigate(Routes.luggageMistakeDetail(id)) },
                onRetry = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
            )
        }

        composable(
            Routes.LuggageMistakeDetailPattern,
            arguments = listOf(navArgument("mistakeId") { type = NavType.StringType }),
        ) { entry ->
            val mistakeId = entry.arguments?.getString("mistakeId").orEmpty()
            MistakeDetailScreen(
                mistakeId = mistakeId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadMistakeDetail,
                onRetry = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
            )
        }

        composable(Routes.LuggageCreations) {
            CreationsScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { luggageViewModel.loadCreations() },
                onLoadMore = { luggageViewModel.loadMoreCreations() },
                onOpen = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onContinue = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
            )
        }

        composable(
            Routes.LuggageCreationDetailPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            CreationDetailScreen(
                projectId = projectId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadCreationDetail,
                onContinue = {
                    navController.navigate(Routes.shengtuProject(it))
                },
                onWithdraw = luggageViewModel::withdrawPublication,
                onAppeal = luggageViewModel::createAppeal,
                onDelete = { id ->
                    luggageViewModel.deleteCreationProject(id) {
                        navController.popBackStack()
                    }
                },
            )
        }

        composable(Routes.LuggagePrivacySafety) {
            PrivacySafetyScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadPrivacy,
                onToggle = { key, value ->
                    luggageViewModel.updatePrivacy { current ->
                        when (key) {
                            "learning_card_public" -> PrivacySettingsPatchDto(
                                learningCardPublic = value,
                                rowVersion = current.rowVersion,
                            )
                            "aigc_export_mark_enabled" -> PrivacySettingsPatchDto(
                                aigcExportMarkEnabled = value,
                                rowVersion = current.rowVersion,
                            )
                            else -> PrivacySettingsPatchDto(
                                profileDiscoveryEnabled = value,
                                rowVersion = current.rowVersion,
                            )
                        }
                    }
                },
                onVisibility = { visibility ->
                    luggageViewModel.updatePrivacy { current ->
                        PrivacySettingsPatchDto(
                            defaultWorkVisibility = visibility,
                            rowVersion = current.rowVersion,
                        )
                    }
                },
                onExport = luggageViewModel::loadAccountExport,
                onRequestDeletion = luggageViewModel::requestAccountDeletion,
            )
        }

        composable(
            Routes.RetryTrialPattern,
            arguments = listOf(
                navArgument("mistakeId") { type = NavType.StringType },
                navArgument("trialId") { type = NavType.StringType },
                navArgument("versionId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType },
            ),
        ) { entry ->
            val session = RetrySessionDto(
                id = entry.arguments?.getString("sessionId").orEmpty(),
                mistakeId = entry.arguments?.getString("mistakeId").orEmpty(),
                trialId = entry.arguments?.getString("trialId").orEmpty(),
                trialVersionId = entry.arguments?.getString("versionId").orEmpty(),
                expiresAt = "",
                submitUrl = "",
            )
            RetryTrialScreen(
                session = session,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadRetryTrial,
                onSubmit = { prediction, answer, explanation ->
                    luggageViewModel.submitRetry(session, prediction, answer, explanation)
                },
                onComplete = {
                    luggageViewModel.refreshMistakeAfterRetry(session.mistakeId)
                    navController.popBackStack()
                },
            )
        }

        // 设置页(Figma 设计) — 点击首页1的"设置"图标跳转
        composable(Routes.Settings) {
            LaunchedEffect(Unit) { conferenceViewModel.syncLetters() }
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                onOpenWendao       = { navController.navigate(Routes.Home1) },
                onOpenLetters      = {
                    navController.navigate(
                        if (conferenceState.conferenceEnabled == false) Routes.Challenge
                        else Routes.DahuiLetters,
                    )
                },
                onOpenLuggage      = { navController.navigate(Routes.Luggage) },
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
                onOpenAccount      = { navController.navigate(Routes.SettingsAccount) },
                onOpenMessage      = { navController.navigate(Routes.SettingsMessage) },
                onOpenGeneral      = { navController.navigate(Routes.SettingsGeneral) },
                onOpenSound        = { navController.navigate(Routes.SettingsSound) },
                onOpenBlacklist    = { navController.navigate(Routes.SettingsBlacklist) },
                onOpenPrivacy      = { navController.navigate(Routes.Privacy) },
                onOpenAgreement    = { navController.navigate(Routes.Agreement) },
                onOpenCollection   = { navController.navigate(Routes.SettingsCollection) },
                onOpenSharing      = { navController.navigate(Routes.SettingsSharing) },
                onOpenHelp         = { navController.navigate(Routes.SettingsHelp) },
                onOpenAbout        = { navController.navigate(Routes.SettingsAbout) },
                onOpenDataRecovery = { navController.navigate(Routes.SettingsDataRecovery) },
                onSwitchAccount    = {
                    authViewModel.logout {
                        navController.navigate(Routes.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLogout           = {
                    authViewModel.logout {
                        navController.navigate(Routes.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(Routes.SettingsAccount) {
            SettingsDetailScreen(
                page = SettingsPage.Account,
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(Routes.Forgot) },
            )
        }
        composable(Routes.SettingsMessage) {
            SettingsDetailScreen(SettingsPage.Message, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsGeneral) {
            SettingsDetailScreen(SettingsPage.General, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsSound) {
            SettingsDetailScreen(SettingsPage.Sound, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsBlacklist) {
            SettingsDetailScreen(SettingsPage.Blacklist, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsCollection) {
            SettingsDetailScreen(SettingsPage.Collection, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsSharing) {
            SettingsDetailScreen(SettingsPage.Sharing, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsHelp) {
            SettingsDetailScreen(SettingsPage.Help, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsAbout) {
            SettingsDetailScreen(
                page = SettingsPage.About,
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.Privacy) },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
            )
        }
        composable(Routes.SettingsDataRecovery) {
            SettingsDetailScreen(SettingsPage.DataRecovery, onBack = { navController.popBackStack() })
        }

        // 书信中的切磋来信可进入挑战页。
        composable(Routes.Challenge) {
            LaunchedEffect(Unit) { distributionViewModel.refreshInboxAndClassrooms() }
            ChallengeScreen(
                inbox = distributionState.inbox,
                inboxLoading = distributionState.inboxLoading,
                inboxError = distributionState.inboxError,
                canLoadMore = distributionState.inboxCursor != null,
                classrooms = distributionState.classrooms,
                classroomLoading = distributionState.classroomLoading,
                classroomMessage = distributionState.classroomMessage,
                oneTimeJoinCode = distributionState.oneTimeJoinCode,
                isAdult = currentUser?.ageBand == "ADULT",
                onRefresh = distributionViewModel::refreshInboxAndClassrooms,
                onLoadMore = { distributionViewModel.loadInbox(loadMore = true) },
                onCreateClassroom = distributionViewModel::createClassroom,
                onJoinClassroom = distributionViewModel::joinClassroom,
                onDismissJoinCode = distributionViewModel::clearOneTimeJoinCode,
                onBack = { navController.popBackStack() },
                onOpenWendao = { navController.navigate(Routes.Xiulian) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenProgress = { /* TODO:进度弹窗或页面 */ },
            )
        }
    }
        LoginMistTransition(
            phase = mistPhase.value,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
