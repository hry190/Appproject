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
import com.jueqiao.jianghu.luggage.LuggageViewModel
import com.jueqiao.jianghu.luggage.LuggageViewModelFactory
import com.jueqiao.jianghu.luggage.PrivacySettingsPatchDto
import com.jueqiao.jianghu.luggage.RetrySessionDto
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.chatresult.ChatResultScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
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
import com.jueqiao.jianghu.ui.screens.yanwuchang.YanwuchangScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.YanwuchangVideoScreen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen
import com.jueqiao.jianghu.ui.screens.gongfang.GongfangScreen
import com.jueqiao.jianghu.ui.screens.gunlun1.Gunlun1Screen
import com.jueqiao.jianghu.ui.screens.gunlun2.Gunlun2Screen
import com.jueqiao.jianghu.ui.screens.gunlun3.Gunlun3Screen
import com.jueqiao.jianghu.ui.screens.gunlun4.Gunlun4Screen
import com.jueqiao.jianghu.ui.screens.gunlun5.Gunlun5Screen
import com.jueqiao.jianghu.ui.screens.shengtu.ShengtuScreen
import com.jueqiao.jianghu.ui.screens.picture.PictureScreen
import com.jueqiao.jianghu.ui.screens.yaosu.YaosuScreen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan.ChuangzuodanganScreen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan2.Chuangzuodangan2Screen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan3.Chuangzuodangan3Screen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan4.Chuangzuodangan4Screen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan5.Chuangzuodangan5Screen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan6.Chuangzuodangan6Screen
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
    val luggageFactory = remember(application) {
        LuggageViewModelFactory(application.luggageRepository)
    }
    val luggageViewModel: LuggageViewModel = viewModel(factory = luggageFactory)
    val luggageState by luggageViewModel.uiState.collectAsStateWithLifecycle()
    val luggageDetailState by luggageViewModel.detailState.collectAsStateWithLifecycle()
    val transitionScope = rememberCoroutineScope()
    val loginContentAlpha = remember { Animatable(1f) }
    val mistPhase = remember { Animatable(0f) }
    var loginTransitionRunning by remember { mutableStateOf(false) }
    var animateHomeQuickActionsOnNextEntry by remember { mutableStateOf(false) }

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
                    authViewModel.login(phone, password) { response ->
                        val destination = if (response.nextAction == "SHOW_GUIDE") {
                            Routes.Home
                        } else {
                            Routes.Home1
                        }
                        animateHomeQuickActionsOnNextEntry = true
                        startLoginTransition(destination)
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
            Home1Screen(
                animateQuickActionsEntrance = animateHomeQuickActionsOnNextEntry,
                quickActionsEntranceReady = !loginTransitionRunning,
                onQuickActionsEntranceConsumed = {
                    animateHomeQuickActionsOnNextEntry = false
                },
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
                onOpenGunlun1  = { navController.navigate(Routes.Gunlun1) },
            )
        }
        composable(Routes.Gunlun1) {
            Gunlun1Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun2 = { navController.navigate(Routes.Gunlun2) },
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
        composable(Routes.Dahui) {
            DahuiScreen(
                onBack = { navController.popBackStack() },
                onOpenYanwuchang = { navController.navigate(Routes.Yanwuchang) },
            )
        }
        // 演武场首页(从大会页"演武场"文字进入) + 演武场视频(从演武场"作品"进入)
        composable(Routes.Yanwuchang) {
            YanwuchangScreen(
                onBack = { navController.popBackStack() },
                onOpenYanwuchangVideo = { navController.navigate(Routes.YanwuchangVideo) },
            )
        }
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                onBack = { navController.popBackStack() },
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

        composable(
            Routes.ShengtuProjectPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            val bundle = luggageDetailState.creationDetail
                ?.takeIf { it.project.id == projectId }
            val latestVersion = bundle?.versions?.maxByOrNull { it.versionNumber }
            val initialPrompt = latestVersion?.layers
                ?.firstOrNull { it.kind == "TEXT" && !it.textContent.isNullOrBlank() }
                ?.textContent
                ?: bundle?.project?.description
            ShengtuScreen(
                projectId = projectId,
                projectTitle = bundle?.project?.title,
                currentVersionNumber = latestVersion?.versionNumber,
                initialPrompt = initialPrompt,
                onBack = { navController.popBackStack() },
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
                onOpenChuangzuodangan2 = { navController.navigate(Routes.Chuangzuodangan2) },
                onOpenChuangzuodangan3 = { navController.navigate(Routes.Chuangzuodangan3) },
                onOpenChuangzuodangan5 = { navController.navigate(Routes.Chuangzuodangan5) },
                onOpenChuangzuodangan6 = { navController.navigate(Routes.Chuangzuodangan6) },
            )
        }

        composable(Routes.Chuangzuodangan2) {
            Chuangzuodangan2Screen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
                onOpenChuangzuodangan3 = { navController.navigate(Routes.Chuangzuodangan3) },
                onOpenChuangzuodangan5 = { navController.navigate(Routes.Chuangzuodangan5) },
                onOpenChuangzuodangan6 = { navController.navigate(Routes.Chuangzuodangan6) },
            )
        }

        composable(Routes.Chuangzuodangan3) {
            Chuangzuodangan3Screen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
                onOpenChuangzuodangan4 = { navController.navigate(Routes.Chuangzuodangan4) },
            )
        }

        composable(Routes.Chuangzuodangan4) {
            Chuangzuodangan4Screen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
            )
        }

        composable(Routes.Chuangzuodangan5) {
            Chuangzuodangan5Screen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
            )
        }
        composable(Routes.Chuangzuodangan6) {
            Chuangzuodangan6Screen(
                onBack = { navController.popBackStack() },
                onCreateWork = { /* TODO: 后续 */ },
            )
        }

        // 行囊页(Figma 设计) — 点击首页1的"行囊"按钮跳转
        composable(Routes.Luggage) {
            LaunchedEffect(Unit) { luggageViewModel.refresh() }
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
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                onOpenWorks        = { navController.navigate(Routes.Zaowu) },
                onOpenTask         = { navController.navigate(Routes.Challenge) },
                onOpenLuggage      = { navController.navigate(Routes.Luggage) },
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

        // 首页挑战页(Figma 设计) — 点击"任务"展开栏的"挑战"文本跳转
        composable(Routes.Challenge) {
            ChallengeScreen(
                onBack        = { navController.popBackStack() },
                onOpenZaowu   = { navController.navigate(Routes.Zaowu) },
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
