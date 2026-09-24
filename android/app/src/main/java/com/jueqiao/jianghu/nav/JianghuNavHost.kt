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
import com.jueqiao.jianghu.conference.ConferenceViewModel
import com.jueqiao.jianghu.conference.ConferenceViewModelFactory
import com.jueqiao.jianghu.luggage.LuggageViewModel
import com.jueqiao.jianghu.luggage.LuggageViewModelFactory
import com.jueqiao.jianghu.luggage.LuggageUiState
import com.jueqiao.jianghu.luggage.PrivacySettingsPatchDto
import com.jueqiao.jianghu.luggage.RetrySessionDto
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceCollectionsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceHubScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceLettersScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceMatchScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceMatchRecordsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceRequestsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceWorkScreen
import com.jueqiao.jianghu.ui.screens.forgot.ForgotScreen
import com.jueqiao.jianghu.ui.screens.home.Home1Screen
import com.jueqiao.jianghu.ui.screens.home.HomeScreen
import com.jueqiao.jianghu.ui.screens.home.LuggageScreen
import com.jueqiao.jianghu.ui.screens.home.SettingsScreen
import com.jueqiao.jianghu.ui.screens.luggage.BadgesScreen
import com.jueqiao.jianghu.ui.screens.luggage.CreationDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.ConferencePublishScreen
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
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Screen
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Screen
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Screen
import com.jueqiao.jianghu.ui.screens.manualreader.ManualReaderScreen
import com.jueqiao.jianghu.ui.screens.wushuhuan.WushuhuanScreen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen
import com.jueqiao.jianghu.ui.screens.gongfang.GongfangScreen
import com.jueqiao.jianghu.ui.screens.gongfang.toCreationResumeItem
import com.jueqiao.jianghu.ui.screens.shengtu.ShengtuScreen
import com.jueqiao.jianghu.ui.screens.unfinished.UnfinishedScreen
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

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun NavHostController.openBackMountainWendao(targetLessonId: String? = null) {
    navigateSingleTop(Routes.shilian(targetLessonId))
}

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
    val progressSnapshot = (luggageState as? LuggageUiState.Content)?.snapshot
    val progressLoading = luggageState is LuggageUiState.Loading ||
        (luggageState as? LuggageUiState.Content)?.refreshing == true ||
        luggageDetailState.loading
    val progressMessage = when (val state = luggageState) {
        is LuggageUiState.Content -> state.notice
        is LuggageUiState.Error -> state.message
        LuggageUiState.Loading -> null
    } ?: luggageDetailState.learningOverviewMessage
        ?: authState.errorMessage?.takeIf {
            authState.errorCode == "NETWORK_UNAVAILABLE" ||
                authState.errorCode == "LOCAL_AUTH_FAILURE"
        }
    val creationFactory = remember(application) {
        CreationViewModelFactory(application, application.luggageRepository)
    }
    val creationViewModel: CreationViewModel = viewModel(factory = creationFactory)
    val creationState by creationViewModel.state.collectAsStateWithLifecycle()
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
                contentAlpha    = { loginContentAlpha.value },
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onRequestCode   = { phone, purpose, onCooldown ->
                    authViewModel.requestCode(phone, purpose, onCooldown)
                },
                onRegister      = { phone, code, password, ageBand ->
                    authViewModel.register(
                        phone,
                        code,
                        password,
                        ageBand,
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
                onOpenXiulian   = { navController.navigateSingleTop(Routes.Gunlun1) },
                onOpenWendao    = { navController.openBackMountainWendao() },
                onOpenLuggage   = { navController.navigateSingleTop(Routes.Luggage) },
                // 每次从首页进入工坊都创建新的引导页实例，确保气泡与入口状态正确。
                onOpenZaowu     = { navController.navigateSingleTop(Routes.Zaowu) },
                onOpenSettings  = { navController.navigateSingleTop(Routes.Settings) },
                onOpenLetters = {
                    navController.navigateSingleTop(Routes.DahuiLetters)
                },
                // 首页“大会”先进入演武场选择页，再由“作品 / 比武”组件继续分流。
                onOpenDahui     = { navController.navigateSingleTop(Routes.Yanwuchang) },
                dahuiEnabled = conferenceState.conferenceEnabled != false,
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
                progressSnapshot = progressSnapshot,
                learningOverview = luggageDetailState.learningOverview,
                progressLoading = progressLoading,
                progressMessage = progressMessage,
                onRefreshProgress = {
                    authViewModel.clearFeedback()
                    luggageViewModel.loadProgressOverview()
                },
                onOpenRecommendedManual = { id ->
                    navController.navigateSingleTop(Routes.luggageManualDetail(id))
                },
            )
        }

        composable(Routes.Xiulian)  {
            LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
            XiulianScreen(
                onBack         = { navController.popBackStack() },
                onOpenGunlun1 = {
                    navController.navigate(Routes.Gunlun1) {
                        popUpTo(Routes.Xiulian) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                learningOverview = luggageDetailState.learningOverview,
            )
        }
        composable(
            route = Routes.ShilianPattern,
            arguments = listOf(
                navArgument("lessonId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
            val targetLessonId = backStackEntry.arguments?.getString("lessonId")
            Houshan1Screen(
                onBack = { navController.popBackStack() },
                targetLessonId = targetLessonId,
                targetVolumeNo = luggageDetailState.learningOverview?.books
                    ?.firstOrNull { it.manualPageId == targetLessonId }
                    ?.volumeNo,
                recommendation = luggageDetailState.learningOverview?.backMountain,
                onOpenFirstTrial = { manualId ->
                    val volumeNo = luggageDetailState.learningOverview?.books
                        ?.firstOrNull { it.manualPageId == manualId }
                        ?.volumeNo
                        ?: 1
                    navController.navigateSingleTop(
                        Routes.manualReader(volumeNo, manualId, continueToTrial = true)
                    )
                },
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
                onFinish = {
                    if (!navController.popBackStack(Routes.ShilianPattern, inclusive = false)) {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable(Routes.Unfinished) {
            UnfinishedScreen(
                onBack = { navController.popBackStack() },
                onOpenGunlun1 = {
                    navController.navigate(Routes.Wushuhuan) {
                        popUpTo(Routes.Gunlun1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        // 旧 Learning/PendingUnlock 静态分支已从导航图移除。
        // 预测、作答、服务端判分统一复用 LearningTrialScreen，避免自动判对与伪奖励。
        composable(Routes.Gunlun1) {
            // 首页会直接进入本页；概览尚未载入时在此补充请求。
            LaunchedEffect(Unit) {
                if (luggageDetailState.learningOverview == null && !luggageDetailState.loading) {
                    luggageViewModel.loadLearningOverview()
                }
            }
            Gunlun1Screen(
                onBack = { navController.popBackStack() },
                onOpenWushuhuan = {
                    navController.navigateSingleTop(Routes.Wushuhuan)
                },
                onOpenHoushan1 = {
                    navController.openBackMountainWendao()
                },
                learningOverview = luggageDetailState.learningOverview,
            )
        }
        // Gunlun2—15 仅保留为旧深链兼容别名；正常书环交互使用独立 Wushuhuan 路由。
        listOf(
            Routes.Wushuhuan,
            Routes.Gunlun2,
            Routes.Gunlun3,
            Routes.Gunlun4,
            Routes.Gunlun5,
            Routes.Gunlun6,
            Routes.Gunlun7,
            Routes.Gunlun8,
            Routes.Gunlun9,
            Routes.Gunlun10,
            Routes.Gunlun11,
            Routes.Gunlun12,
            Routes.Gunlun13,
            Routes.Gunlun14,
            Routes.Gunlun15,
        ).forEach { route ->
            composable(route) {
                LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
                WushuhuanScreen(
                    onBack = { navController.popBackStack() },
                    learningOverview = luggageDetailState.learningOverview,
                    manualDetail = luggageDetailState.manualDetail,
                    isLoading = luggageDetailState.loading,
                    loadMessage = luggageDetailState.message,
                    onOpenHoushan = { lessonId ->
                        navController.openBackMountainWendao(lessonId)
                    },
                    onLoadManualDetail = luggageViewModel::loadManualDetail,
                    onOpenReader = { volumeNo, manualId, continueToTrial ->
                        navController.navigateSingleTop(
                            Routes.manualReader(volumeNo, manualId, continueToTrial)
                        )
                    },
                    onOpenTrial = { trialId ->
                        navController.navigateSingleTop(
                            Routes.learningTrial(trialId, returnToWushuhuan = true)
                        )
                    },
                    onUseInCreation = { manualId ->
                        navController.navigateSingleTop(Routes.gongfang(manualId))
                    },
                )
            }
        }
        composable(
            route = Routes.ManualReaderPattern,
            arguments = listOf(
                navArgument("volumeNo") { type = NavType.IntType },
                navArgument("manualId") { type = NavType.StringType },
                navArgument("continueToTrial") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entry ->
            val volumeNo = entry.arguments?.getInt("volumeNo") ?: 1
            val manualId = entry.arguments?.getString("manualId").orEmpty()
            val continueToTrial = entry.arguments?.getBoolean("continueToTrial") ?: false
            LaunchedEffect(manualId) {
                if (manualId.isNotBlank()) luggageViewModel.loadManualDetail(manualId)
            }
            val detail = luggageDetailState.manualDetail
                ?.takeIf { it.manual.id == manualId }
            ManualReaderScreen(
                volumeNo = volumeNo,
                lessonPageNo = detail?.manual?.pageNo,
                isCompleting = luggageDetailState.loading,
                message = luggageDetailState.message,
                onBack = { navController.popBackStack() },
                onComplete = {
                    if (manualId.isBlank()) return@ManualReaderScreen
                    luggageViewModel.completeManualReading(manualId) {
                        val trialId = detail?.manual?.trialId
                        if (continueToTrial && trialId != null) {
                            navController.navigate(
                                Routes.learningTrial(trialId, returnToWushuhuan = true)
                            ) {
                                popUpTo(entry.destination.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
            )
        }
        composable(Routes.Zaowu)    {
            ZaowuScreen(
                guideSessionKey = currentUser?.id ?: "guest",
                onBack         = {
                    // 引导页的页面返回与系统返回都直接回首页，并移除本次引导实例。
                    navController.navigate(Routes.Home1) {
                        popUpTo(Routes.Home1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onOpenCreationDesk = {
                    navController.navigate(Routes.Gongfang) {
                        launchSingleTop = true
                    }
                },
                onOpenCreationArchive = {
                    navController.navigate(Routes.Chuangzuodangan) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Dahui) {
            DahuiScreen(
                onBack = { navController.popBackStack() },
                // 大会入口先展示引导页；演武场组件进入图1的演武场页面。
                onOpenArena = { navController.navigate(Routes.Yanwuchang) },
            )
        }
        composable(Routes.Yanwuchang) {
            YanwuchangScreen(
                onBack = { navController.popBackStack() },
                onOpenDahui = { navController.navigate(Routes.DahuiArena) },
                onOpenYanwuchangVideo = { navController.navigate(Routes.YanwuchangVideo) },
            )
        }
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoadFeed = { category -> conferenceViewModel.loadFeed(category = category) },
                onLoadWork = conferenceViewModel::loadWork,
                onAddLike = conferenceViewModel::addLike,
                onRemoveLike = conferenceViewModel::removeLike,
                onCreateReview = conferenceViewModel::createReview,
                onAddCollection = conferenceViewModel::addCollection,
                onRemoveCollection = conferenceViewModel::removeCollection,
                onCreateCoCreateRequest = conferenceViewModel::createDerivativeRequest,
                onOpenOwnerRequests = { navController.navigate(Routes.DahuiRequests) },
                onOpenMy = { navController.navigate(Routes.YanwuchangVideoMy) },
            )
        }
        composable(Routes.YanwuchangVideoMy) {
            YanwuchangVideoMyScreen(
                state = conferenceState,
                nickname = currentUser?.nickname.orEmpty(),
                userId = currentUser?.id.orEmpty(),
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMyShelf,
                onOpenWorks = {
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
                onOpenReviews = { navController.navigate(Routes.dahuiWork(it)) },
                onOpenBrowseRecord = {
                    navController.navigate(Routes.YanwuchangVideoBrowseRecord)
                },
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
                onLoad = { conferenceViewModel.loadMatch() },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenMatch = { navController.navigate(Routes.DahuiMatch) },
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
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
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
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
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiRecords) {
            ConferenceMatchRecordsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatchRecords,
                onOpenMatch = { navController.navigate(Routes.dahuiMatch(it)) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiLetters) {
            ConferenceLettersScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadLetters,
                onMarkRead = conferenceViewModel::readLetter,
                onOpenTarget = { letter ->
                    Routes.conferenceLetterDestination(
                        navigationTarget = letter.navigationTarget,
                        navigationId = letter.navigationId,
                        actionType = letter.actionType,
                        actionId = letter.actionId,
                    )?.let { destination ->
                        navController.navigate(destination) { launchSingleTop = true }
                        true
                    } ?: false
                },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(
            route = Routes.GongfangPattern,
            arguments = listOf(
                navArgument("sourceManualId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val sourceManualId = entry.arguments?.getString("sourceManualId")
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
            GongfangScreen(
                onBack   = {
                    creationViewModel.clearDerivative()
                    if (!sourceManualId.isNullOrBlank()) {
                        navController.popBackStack()
                    } else if (
                        navController.previousBackStackEntry?.destination?.route == Routes.Zaowu
                    ) {
                        navController.popBackStack()
                    } else {
                        // 非工坊入口继续沿用原逻辑，避免改变其他创作来源的返回路径。
                        navController.navigate(Routes.Home1) {
                            popUpTo(Routes.Home1) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                initialManualId = sourceManualId,
                onStartConversation = { idea, assets, manuals ->
                    creationViewModel.startConversation(idea, assets, manuals) { conversation ->
                        navController.navigate(Routes.shengtuProject(conversation.project.id))
                    }
                },
                onContinueWork = { workId ->
                    navController.navigate(Routes.shengtuProject(workId))
                },
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
                startingConversation = creationState.startingConversation,
                analysisMessage = creationState.analysisError,
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
            }
            val bundle = luggageDetailState.creationDetail
                ?.takeIf { it.project.id == projectId }
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
            ShengtuScreen(
                projectId = projectId,
                projectTitle = bundle?.project?.title,
                conversation = bundle?.conversation,
                activeGeneration = visibleGeneration,
                loading = luggageDetailState.loading && bundle == null,
                busy = creationState.conversationBusy &&
                    creationState.conversationProjectId == projectId,
                statusMessage = creationState.conversationMessage
                    ?.takeIf { creationState.conversationProjectId == projectId },
                errorMessage = creationState.conversationError
                    ?.takeIf { creationState.conversationProjectId == projectId },
                generationBusy = creationState.generationBusy &&
                    creationState.generationProjectId == projectId,
                generationMessage = creationState.generationMessage
                    ?.takeIf { creationState.generationProjectId == projectId },
                onSendMessage = { text ->
                    creationViewModel.sendConversationMessage(projectId, text) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onAcceptSuggestion = { message ->
                    bundle?.conversation?.let { current ->
                        creationViewModel.acceptConversationSuggestion(
                            projectId = projectId,
                            message = message,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSaveDraftAndGenerate = {
                    bundle?.conversation?.let { current ->
                        creationViewModel.saveConversationDraftAndGenerate(
                            projectId = projectId,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            luggageViewModel.refresh(force = true)
                        }
                    }
                },
                onSaveWork = {
                    bundle?.conversation?.let { current ->
                        creationViewModel.saveConversationResult(
                            projectId = projectId,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            creationViewModel.loadRecentProjects()
                        }
                    }
                },
                onRetryGeneration = { job ->
                    creationViewModel.retryImageGeneration(job) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                    }
                },
                onBack = { navController.popBackStack() },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
                onPublishToConference = {
                    navController.navigateSingleTop(Routes.conferencePublish(projectId))
                },
            )
        }

        composable(Routes.Chuangzuodangan) {
            LaunchedEffect(Unit) {
                creationViewModel.loadRecentProjects()
            }
            ChuangzuodanganScreen(
                onBack = { navController.popBackStack() },
                guideSessionKey = currentUser?.id.orEmpty().ifBlank { "guest" },
                onContinueWork = { projectId ->
                    creationViewModel.continueProject(projectId) {
                        luggageViewModel.loadCreationDetail(projectId)
                        navController.navigate(Routes.shengtuProject(projectId))
                    }
                },
                onPublishWork = { projectId ->
                    navController.navigateSingleTop(Routes.conferencePublish(projectId))
                },
                onDeleteWork = { projectId ->
                    luggageViewModel.deleteCreationProject(projectId) {
                        creationViewModel.removeDeletedProject(projectId)
                    }
                },
                recentWorks = creationState.recentProjects,
                recentWorksLoading = creationState.loadingRecent,
                recentWorksMessage = creationState.recentError,
                continuingProjectId = creationState.continuingProjectId,
                continueMessage = creationState.continueMessage,
                onRetryRecentWorks = creationViewModel::loadRecentProjects,
                archiveDetail = luggageDetailState.creationDetail,
                archiveDetailProjectId = luggageDetailState.creationDetailProjectId,
                archiveDetailLoading = luggageDetailState.loading,
                archiveDetailMessage = luggageDetailState.message,
                onArchiveWorkSelected = luggageViewModel::loadCreationDetail,
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
            arguments = listOf(
                navArgument("trialId") { type = NavType.StringType },
                navArgument("returnToWushuhuan") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entry ->
            val trialId = entry.arguments?.getString("trialId").orEmpty()
            val returnToWushuhuan =
                entry.arguments?.getBoolean("returnToWushuhuan") ?: false
            val completeTrial: () -> Unit = {
                luggageViewModel.loadLearningOverview()
                if (returnToWushuhuan) {
                    if (!navController.popBackStack(Routes.Wushuhuan, inclusive = false)) {
                        navController.navigate(Routes.Wushuhuan) {
                            popUpTo(entry.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    navController.popBackStack()
                }
            }
            LearningTrialScreen(
                trialId = trialId,
                state = luggageDetailState,
                onBack = {
                    if (returnToWushuhuan && luggageDetailState.trialResult?.passed == true) {
                        completeTrial()
                    } else {
                        navController.popBackStack()
                    }
                },
                onLoad = luggageViewModel::loadLearningTrial,
                onSubmit = { prediction, answer, explanation ->
                    luggageViewModel.submitLearningTrial(trialId, prediction, answer, explanation)
                },
                onRetry = luggageViewModel::retryLearningTrial,
                onComplete = completeTrial,
                completeLabel = if (returnToWushuhuan) "返回悟书环查看状态" else "返回秘籍",
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
                onPublish = { id ->
                    navController.navigateSingleTop(Routes.conferencePublish(id))
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

        composable(
            Routes.ConferencePublishPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            ConferencePublishScreen(
                projectId = projectId,
                state = luggageDetailState,
                publishBusy = creationState.workflowBusy &&
                    creationState.workflowMessageProjectId == projectId,
                publishMessage = creationState.workflowMessage
                    ?.takeIf { creationState.workflowMessageProjectId == projectId },
                publishFailed = creationState.workflowFailed &&
                    creationState.workflowMessageProjectId == projectId,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadCreationDetail,
                onSubmit = { bundle, draft ->
                    creationViewModel.publishToConference(bundle, draft) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                        creationViewModel.loadRecentProjects()
                        conferenceViewModel.invalidateFeed()
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

    }
        if (loginTransitionRunning) {
            LoginMistTransition(
                phase = { mistPhase.value },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun NavHostController.navigateConferenceRoot(route: String) {
    navigate(route) {
        popUpTo(Routes.DahuiArena) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
