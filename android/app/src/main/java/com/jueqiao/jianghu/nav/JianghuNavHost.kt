package com.jueqiao.jianghu.nav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.graphics.TransformOrigin
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
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Actions           // §7 全量迁移,data class 防 §3 slot bug
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Screen
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Screen
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Screen
import com.jueqiao.jianghu.ui.screens.houshan4.Houshan4Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan4.Houshan4Screen            // §24 新增
import com.jueqiao.jianghu.ui.screens.houshan5.Houshan5Actions           // §5/§6 先行重构,data class 防 §3 slot bug
import com.jueqiao.jianghu.ui.screens.houshan5.Houshan5Screen            // §33 新增
import com.jueqiao.jianghu.ui.screens.houshan6.Houshan6Actions           // §5/§6 先行重构
import com.jueqiao.jianghu.ui.screens.houshan6.Houshan6Screen            // 2026-09-18 §4 新增
import com.jueqiao.jianghu.ui.screens.houshan7.Houshan7Actions           // 2026-09-18 §9 新增
import com.jueqiao.jianghu.ui.screens.houshan7.Houshan7Screen            // 2026-09-18 §9 新增
import com.jueqiao.jianghu.ui.screens.houshan8.Houshan8Actions           // 2026-09-18 §10 新增
import com.jueqiao.jianghu.ui.screens.houshan8.Houshan8Screen            // 2026-09-18 §10 新增
import com.jueqiao.jianghu.ui.screens.houshan9.Houshan9Actions           // 2026-09-18 §11 新增
import com.jueqiao.jianghu.ui.screens.houshan9.Houshan9Screen            // 2026-09-18 §11 新增
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
import com.jueqiao.jianghu.ui.screens.gunlun16.Gunlun16Screen
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
import com.jueqiao.jianghu.ui.screens.volume1part6.Volume1Part6Screen
import com.jueqiao.jianghu.ui.screens.volume1part7.Volume1Part7Screen
import com.jueqiao.jianghu.ui.screens.volume1part8.Volume1Part8Screen
import com.jueqiao.jianghu.ui.screens.volume1part9.Volume1Part9Screen
import com.jueqiao.jianghu.ui.screens.volume1part10.Volume1Part10Screen
import com.jueqiao.jianghu.ui.screens.volume1part11.Volume1Part11Screen
import com.jueqiao.jianghu.ui.screens.volume1part12.Volume1Part12Screen
import com.jueqiao.jianghu.ui.screens.volume1part13.Volume1Part13Screen
import com.jueqiao.jianghu.ui.screens.volume1part14.Volume1Part14Screen
import com.jueqiao.jianghu.ui.screens.volume2part1.Volume2Part1Screen
import com.jueqiao.jianghu.ui.screens.volume2part2.Volume2Part2Screen
import com.jueqiao.jianghu.ui.screens.volume2part3.Volume2Part3Screen
import com.jueqiao.jianghu.ui.screens.volume2part4.Volume2Part4Screen
import com.jueqiao.jianghu.ui.screens.volume2part5.Volume2Part5Screen
import com.jueqiao.jianghu.ui.screens.volume2part6.Volume2Part6Screen
import com.jueqiao.jianghu.ui.screens.volume2part7.Volume2Part7Screen
import com.jueqiao.jianghu.ui.screens.volume2part8.Volume2Part8Screen
import com.jueqiao.jianghu.ui.screens.volume2part9.Volume2Part9Screen
import com.jueqiao.jianghu.ui.screens.volume2part10.Volume2Part10Screen
import com.jueqiao.jianghu.ui.screens.volume2part11.Volume2Part11Screen
import com.jueqiao.jianghu.ui.screens.volume2part12.Volume2Part12Screen
import com.jueqiao.jianghu.ui.screens.volume2part13.Volume2Part13Screen
import com.jueqiao.jianghu.ui.screens.volume2part14.Volume2Part14Screen
import com.jueqiao.jianghu.ui.screens.volume2part15.Volume2Part15Screen
import com.jueqiao.jianghu.ui.screens.volume3part10.Volume3Part10Screen
import com.jueqiao.jianghu.ui.screens.volume3part11.Volume3Part11Screen
import com.jueqiao.jianghu.ui.screens.volume3part12.Volume3Part12Screen
import com.jueqiao.jianghu.ui.screens.volume3part13.Volume3Part13Screen
import com.jueqiao.jianghu.ui.screens.volume3part14.Volume3Part14Screen
import com.jueqiao.jianghu.ui.screens.volume4part1.Volume4Part1Screen
import com.jueqiao.jianghu.ui.screens.volume5part1.Volume5Part1Screen
import com.jueqiao.jianghu.ui.screens.volume5part2.Volume5Part2Screen
import com.jueqiao.jianghu.ui.screens.volume5part3.Volume5Part3Screen
import com.jueqiao.jianghu.ui.screens.volume5part4.Volume5Part4Screen
import com.jueqiao.jianghu.ui.screens.volume5part5.Volume5Part5Screen
import com.jueqiao.jianghu.ui.screens.volume5part6.Volume5Part6Screen
import com.jueqiao.jianghu.ui.screens.volume5part7.Volume5Part7Screen
import com.jueqiao.jianghu.ui.screens.volume5part8.Volume5Part8Screen
import com.jueqiao.jianghu.ui.screens.volume5part9.Volume5Part9Screen
import com.jueqiao.jianghu.ui.screens.volume5part10.Volume5Part10Screen
import com.jueqiao.jianghu.ui.screens.volume5part11.Volume5Part11Screen
import com.jueqiao.jianghu.ui.screens.volume5part12.Volume5Part12Screen
import com.jueqiao.jianghu.ui.screens.volume5part13.Volume5Part13Screen
import com.jueqiao.jianghu.ui.screens.volume5part14.Volume5Part14Screen
import com.jueqiao.jianghu.ui.screens.volume5part15.Volume5Part15Screen
import com.jueqiao.jianghu.ui.screens.volume6part1.Volume6Part1Screen
import com.jueqiao.jianghu.ui.screens.volume6part2.Volume6Part2Screen
import com.jueqiao.jianghu.ui.screens.volume6part3.Volume6Part3Screen
import com.jueqiao.jianghu.ui.screens.volume6part4.Volume6Part4Screen
import com.jueqiao.jianghu.ui.screens.volume6part5.Volume6Part5Screen
import com.jueqiao.jianghu.ui.screens.volume6part6.Volume6Part6Screen
import com.jueqiao.jianghu.ui.screens.volume6part7.Volume6Part7Screen
import com.jueqiao.jianghu.ui.screens.volume6part8.Volume6Part8Screen
import com.jueqiao.jianghu.ui.screens.volume6part9.Volume6Part9Screen
import com.jueqiao.jianghu.ui.screens.volume6part10.Volume6Part10Screen
import com.jueqiao.jianghu.ui.screens.volume6part11.Volume6Part11Screen
import com.jueqiao.jianghu.ui.screens.volume6part12.Volume6Part12Screen
import com.jueqiao.jianghu.ui.screens.volume6part13.Volume6Part13Screen
import com.jueqiao.jianghu.ui.screens.volume6part14.Volume6Part14Screen
import com.jueqiao.jianghu.ui.screens.volume6part15.Volume6Part15Screen
import com.jueqiao.jianghu.ui.screens.volume7part1.Volume7Part1Screen
import com.jueqiao.jianghu.ui.screens.volume7part2.Volume7Part2Screen
import com.jueqiao.jianghu.ui.screens.volume7part3.Volume7Part3Screen
import com.jueqiao.jianghu.ui.screens.volume7part4.Volume7Part4Screen
import com.jueqiao.jianghu.ui.screens.volume7part5.Volume7Part5Screen
import com.jueqiao.jianghu.ui.screens.volume7part6.Volume7Part6Screen
import com.jueqiao.jianghu.ui.screens.volume7part7.Volume7Part7Screen
import com.jueqiao.jianghu.ui.screens.volume7part8.Volume7Part8Screen
import com.jueqiao.jianghu.ui.screens.volume7part9.Volume7Part9Screen
import com.jueqiao.jianghu.ui.screens.volume7part10.Volume7Part10Screen
import com.jueqiao.jianghu.ui.screens.volume7part11.Volume7Part11Screen
import com.jueqiao.jianghu.ui.screens.volume7part12.Volume7Part12Screen
import com.jueqiao.jianghu.ui.screens.volume8part1.Volume8Part1Screen
import com.jueqiao.jianghu.ui.screens.volume8part2.Volume8Part2Screen
import com.jueqiao.jianghu.ui.screens.volume8part3.Volume8Part3Screen
import com.jueqiao.jianghu.ui.screens.volume8part4.Volume8Part4Screen
import com.jueqiao.jianghu.ui.screens.volume8part5.Volume8Part5Screen
import com.jueqiao.jianghu.ui.screens.volume8part6.Volume8Part6Screen
import com.jueqiao.jianghu.ui.screens.volume8part7.Volume8Part7Screen
import com.jueqiao.jianghu.ui.screens.volume8part8.Volume8Part8Screen
import com.jueqiao.jianghu.ui.screens.volume8part9.Volume8Part9Screen
import com.jueqiao.jianghu.ui.screens.volume8part10.Volume8Part10Screen
import com.jueqiao.jianghu.ui.screens.volume8part11.Volume8Part11Screen
import com.jueqiao.jianghu.ui.screens.volume8part12.Volume8Part12Screen
import com.jueqiao.jianghu.ui.screens.volume8part13.Volume8Part13Screen
import com.jueqiao.jianghu.ui.screens.volume8part14.Volume8Part14Screen
import com.jueqiao.jianghu.ui.screens.volume9part1.Volume9Part1Screen
import com.jueqiao.jianghu.ui.screens.volume9part2.Volume9Part2Screen
import com.jueqiao.jianghu.ui.screens.volume9part3.Volume9Part3Screen
import com.jueqiao.jianghu.ui.screens.volume9part4.Volume9Part4Screen
import com.jueqiao.jianghu.ui.screens.volume9part5.Volume9Part5Screen
import com.jueqiao.jianghu.ui.screens.volume9part6.Volume9Part6Screen
import com.jueqiao.jianghu.ui.screens.volume9part7.Volume9Part7Screen
import com.jueqiao.jianghu.ui.screens.volume9part8.Volume9Part8Screen
import com.jueqiao.jianghu.ui.screens.volume9part9.Volume9Part9Screen
import com.jueqiao.jianghu.ui.screens.volume9part10.Volume9Part10Screen
import com.jueqiao.jianghu.ui.screens.volume9part11.Volume9Part11Screen
import com.jueqiao.jianghu.ui.screens.volume9part12.Volume9Part12Screen
import com.jueqiao.jianghu.ui.screens.volume9part13.Volume9Part13Screen
import com.jueqiao.jianghu.ui.screens.volume9part14.Volume9Part14Screen
import com.jueqiao.jianghu.ui.screens.volume9part15.Volume9Part15Screen
import com.jueqiao.jianghu.ui.screens.volume10part1.Volume10Part1Screen
import com.jueqiao.jianghu.ui.screens.volume10part2.Volume10Part2Screen
import com.jueqiao.jianghu.ui.screens.volume10part3.Volume10Part3Screen
import com.jueqiao.jianghu.ui.screens.volume10part4.Volume10Part4Screen
import com.jueqiao.jianghu.ui.screens.volume10part5.Volume10Part5Screen
import com.jueqiao.jianghu.ui.screens.volume10part6.Volume10Part6Screen
import com.jueqiao.jianghu.ui.screens.volume10part7.Volume10Part7Screen
import com.jueqiao.jianghu.ui.screens.volume10part8.Volume10Part8Screen
import com.jueqiao.jianghu.ui.screens.volume10part9.Volume10Part9Screen
import com.jueqiao.jianghu.ui.screens.volume10part10.Volume10Part10Screen
import com.jueqiao.jianghu.ui.screens.volume10part11.Volume10Part11Screen
import com.jueqiao.jianghu.ui.screens.volume10part12.Volume10Part12Screen
import com.jueqiao.jianghu.ui.screens.volume10part13.Volume10Part13Screen
import com.jueqiao.jianghu.ui.screens.volume10part14.Volume10Part14Screen
import com.jueqiao.jianghu.ui.screens.volume4part2.Volume4Part2Screen
import com.jueqiao.jianghu.ui.screens.volume4part3.Volume4Part3Screen
import com.jueqiao.jianghu.ui.screens.volume4part4.Volume4Part4Screen
import com.jueqiao.jianghu.ui.screens.volume4part5.Volume4Part5Screen
import com.jueqiao.jianghu.ui.screens.volume4part6.Volume4Part6Screen
import com.jueqiao.jianghu.ui.screens.volume4part7.Volume4Part7Screen
import com.jueqiao.jianghu.ui.screens.volume4part8.Volume4Part8Screen
import com.jueqiao.jianghu.ui.screens.volume4part9.Volume4Part9Screen
import com.jueqiao.jianghu.ui.screens.volume4part10.Volume4Part10Screen
import com.jueqiao.jianghu.ui.screens.volume4part11.Volume4Part11Screen
import com.jueqiao.jianghu.ui.screens.volume4part12.Volume4Part12Screen
import com.jueqiao.jianghu.ui.screens.volume4part13.Volume4Part13Screen
import com.jueqiao.jianghu.ui.screens.volume4part14.Volume4Part14Screen
import com.jueqiao.jianghu.ui.screens.volume3part1.Volume3Part1Screen
import com.jueqiao.jianghu.ui.screens.volume3part2.Volume3Part2Screen
import com.jueqiao.jianghu.ui.screens.volume3part3.Volume3Part3Screen
import com.jueqiao.jianghu.ui.screens.volume3part4.Volume3Part4Screen
import com.jueqiao.jianghu.ui.screens.volume3part5.Volume3Part5Screen
import com.jueqiao.jianghu.ui.screens.volume3part6.Volume3Part6Screen
import com.jueqiao.jianghu.ui.screens.volume3part7.Volume3Part7Screen
import com.jueqiao.jianghu.ui.screens.volume3part8.Volume3Part8Screen
import com.jueqiao.jianghu.ui.screens.volume3part9.Volume3Part9Screen
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
                actions = Houshan1Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan2 = { navController.navigate(Routes.Shilian2) },
                    // §25:用户指令"点击标签识机真决改成无法跳转" → 不再传 onOpenVolume1
                    //   (反转 §22 的 `onOpenVolume1 = { navigate(Routes.Volume1) }`)。
                    //   后山2 的同名标签仍保留跳转,未受影响。
                ),
            )
        }
        // 后山2 → 后山3:沉浸式纵深推进(§36)。缩放由 Houshan2Screen 内部按景深分层完成,
        // 导航层只负责交叉淡入淡出,因此这里不做位移,避免与内部推进叠加成"跳页"。
        composable(
            route = Routes.Shilian2,
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian3) {
                    // 与 Houshan2 内部推进在 DOLLY_HANDOFF_MS 处交接,淡出稍长以覆盖后半程
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    // 其他去向(返回后山1、识机真决→第一卷-1)保持轻淡出,不引入硬切
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
        ) {
            Houshan2Screen(
                actions = Houshan2Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    onOpenHoushan3 = { navController.navigate(Routes.Shilian3) },   // §15 拆招心法   → 后山3
                    onOpenHoushan4 = { navController.navigate(Routes.Shilian4) },   // §15 万象谱     → 后山4
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },   // §15 寻径迷踪步 → 后山5
                    onOpenVolume1 = { navController.navigate(Routes.Volume1) },   // 2026-09-18 §14 恢复(Y 最大标签:识机真决 → 卷1)
                    // §28:拆招心法标签 → 第二卷-1
                    onOpenVolume2Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                    // §29:万象谱标签 → 第三卷-1
                    onOpenVolume3Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                    // §30:寻径迷踪步标签 → 第四卷-1
                    onOpenVolume4Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                ),
            )
        }
        composable(
            route = Routes.Shilian3,
            // 镜头减速停稳:由轻微放大回落到 1.00,灭点与后山2 的推进焦点一致
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            // §24:反转 §21o —— 后山 3 现在 dolly 到后山 4,而非未完待续;
            // 淡出与后山 2 → 后山 3 同款:稍长以覆盖 dolly 后半程,与 §24 Houshan3 内部推进在 DOLLY_HANDOFF_MS 处交接
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian4) {
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    // 其他去向(返回后山2 等)保持轻淡出,不引入硬切
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
            // 显式覆盖:不设则 popEnterTransition 会默认继承上面的 enterTransition,
            // 导致从"未完待续"返回时也播一次推进动画
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan3Screen(
                actions = Houshan3Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // §24:反转 §21o —— 不再跳转未完待续页,改为跳转新建的后山 4 页
                    onOpenHoushan4 = { navController.navigate(Routes.Shilian4) },   // §15 万象谱     → 后山4
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },   // §15 寻径迷踪步 → 后山5
                    // §31:3 个标签 → 按"文字 → 卷"映射(与后山2 §28~§30 同一张表)
                    onOpenVolume2Part1 = { navController.navigate(Routes.Volume2Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:拆招心法 → 卷2)
                    onOpenVolume3Part1 = {},   // 2026-09-18 §12 取消跳转(原:万象谱)
                    onOpenVolume4Part1 = {},   // 2026-09-18 §12 取消跳转(原:寻径迷踪步)
                ),
            )
        }
        // §24:后山 4 页 —— 复用后山 2 素材;§33 起从"终点页"变成"过场页"(dolly 到后山5)
        composable(
            route = Routes.Shilian4,
            // §24 镜头减速停稳(同 Shilian3 enterTransition,与后山 3 dolly 末态衔接)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            // §33:后山 4 现在 dolly 到后山 5,淡出与后山 2→3 / 后山 3→4 同款
            // (稍长以覆盖 dolly 后半程,与 Houshan4 内部推进在 DOLLY_HANDOFF_MS 处交接)
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian5) {
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    // 其他去向(返回后山3、4 个标签→各卷)保持轻淡出,不引入硬切
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan4Screen(
                actions = Houshan4Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // §33:点击标签以外任意位置 → dolly 推进到后山5
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },   // §15 寻径迷踪步 → 后山5
                    onOpenHoushan6 = { navController.navigate(Routes.Shilian6) },   // §15 百炼识物诀 → 后山6
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },   // §15 分门辨类掌 → 后山7
                    // §32:4 个标签的跳转目标(参数名已从 §24 的占位 onOpenTagN 改为语义名)
                    //   ⚠️ 后山4 的标签文字是 §24 改过的,映射表与后山2/3 不完全相同 —— 按文字对齐
                    onOpenVolume3Part1 = { navController.navigate(Routes.Volume3Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:万象谱 → 卷3)
                    onOpenVolume4Part1 = {},   // 2026-09-18 §12 取消跳转(原:寻径迷踪步)
                    onOpenVolume5Part1 = {},   // 2026-09-18 §12 取消跳转(原:百炼识物诀)
                    onOpenVolume6Part1 = {},   // 2026-09-18 §12 取消跳转(原:分门辨类掌)
                ),
            )
        }
        // §33:后山 5 页 —— 复用后山 3 的素材/动画,2026-09-18 §4 升级为过场页(整屏触发 dolly 到后山6)
        composable(
            route = Routes.Shilian5,
            // 镜头减速停稳(与后山 4 内部 dolly 末态衔接)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan5Screen(
                actions = Houshan5Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // 2026-09-18 §4:点击标签以外任意位置 → dolly 推进到后山6
                    onOpenHoushan6 = { navController.navigate(Routes.Shilian6) },   // §15 百炼识物诀 → 后山6
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },   // §15 分门辨类掌 → 后山7
                    // §34:3 个标签按"文字→卷"映射接好 —— 详见 Houshan5Screen 顶 KDoc
                    onOpenVolume4Part1 = { navController.navigate(Routes.Volume4Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:寻径迷踪步 → 卷4)
                    onOpenVolume5Part1 = {},   // 2026-09-18 §12 取消跳转(原:百炼识物诀 → 第五卷-1)
                    onOpenVolume6Part1 = {},   // 2026-09-18 §12 取消跳转(原:分门辨类掌 → 第六卷-1)
                ),
            )
        }
        // 2026-09-18 §4:后山 6 页 —— 复用后山 4 的素材/动画,§9 升级为过场页(整屏触发 dolly 到后山7)
        composable(
            route = Routes.Shilian6,
            // 镜头减速停稳(与后山 5 内部 dolly 末态衔接,与 Shilian5 同款)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan6Screen(
                actions = Houshan6Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // 2026-09-18 §9:点击标签以外任意位置 → dolly 推进到后山7
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },   // §15 分门辨类掌 → 后山7
                    onOpenHoushan8 = { navController.navigate(Routes.Shilian8) },   // §15 千层观心镜 → 后山8
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },   // §15 赏罚驭灵诀 → 后山9
                    // 2026-09-18 §8:4 个标签按 §32 的"文字→卷"映射接好(文案重命名 §4→§8)
                    onOpenVolume5Part1 = { navController.navigate(Routes.Volume5Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:百炼识物诀 → 卷5)
                    onOpenVolume6Part1 = {},   // 2026-09-18 §12 取消跳转(原:分门辨类掌 → 第六卷-1)
                    onOpenVolume7Part1 = {},   // 2026-09-18 §12 取消跳转(原:千层观心镜 → 第七卷-1)
                    onOpenVolume8Part1 = {},   // 2026-09-18 §12 取消跳转(原:赏罚驭灵诀 → 第八卷-1)
                ),
            )
        }
        // 2026-09-18 §9:后山 7 页 —— 复用后山 5 的素材/动画,§10 升级为过场页(整屏触发 dolly 到后山8)
        composable(
            route = Routes.Shilian7,
            // 镜头减速停稳(与后山 6 内部 dolly 末态衔接,与 Shilian5/6 同款)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan7Screen(
                actions = Houshan7Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // 2026-09-18 §10:点击标签以外任意位置 → dolly 推进到后山8
                    onOpenHoushan8 = { navController.navigate(Routes.Shilian8) },   // §15 千层观心镜 → 后山8
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },   // §15 赏罚驭灵诀 → 后山9
                    // 2026-09-18 §9:3 个标签按 §8 的"文字→卷"映射接好(分门辨类掌/千层观心镜/赏罚驭灵诀)
                    onOpenVolume6Part1 = { navController.navigate(Routes.Volume6Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:分门辨类掌 → 卷6)
                    onOpenVolume7Part1 = {},   // 2026-09-18 §12 取消跳转(原:千层观心镜 → 第七卷-1)
                    onOpenVolume8Part1 = {},   // 2026-09-18 §12 取消跳转(原:赏罚驭灵诀 → 第八卷-1)
                ),
            )
        }
        // 2026-09-18 §10:后山 8 页 —— 复用后山 6 的素材/动画,§11 升级为过场页(整屏触发 dolly 到后山9)
        composable(
            route = Routes.Shilian8,
            // 镜头减速停稳(与后山 7 内部 dolly 末态衔接,与 Shilian5/6/7 同款)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan8Screen(
                actions = Houshan8Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // 2026-09-18 §11:点击标签以外任意位置 → dolly 推进到后山9
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },   // §15 赏罚驭灵诀 → 后山9
                    // 2026-09-18 §10:4 个标签按 §32 的"文字→卷"映射接好(千层观心镜/赏罚驭灵诀/听言解意篇/正心守道录)
                    onOpenVolume7Part1 = { navController.navigate(Routes.Volume7Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:千层观心镜 → 卷7)
                    onOpenVolume8Part1 = {},   // 2026-09-18 §12 取消跳转(原:赏罚驭灵诀 → 第八卷-1)
                    onOpenVolume9Part1 = {},   // 2026-09-18 §12 取消跳转(原:听言解意篇 → 第九卷-1)
                    onOpenVolume10Part1 = {},   // 2026-09-18 §12 取消跳转(原:正心守道录 → 第十卷-1)
                ),
            )
        }
        // 2026-09-18 §11:后山 9 页 —— 复用后山 7 的素材/动画,当前是**终点页**(整屏 noop)
        composable(
            route = Routes.Shilian9,
            // 镜头减速停稳(与后山 8 内部 dolly 末态衔接,与 Shilian5~8 同款)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan9Screen(
                actions = Houshan9Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // 2026-09-18 §11:3 个标签按 §8 的"文字→卷"映射接好(赏罚驭灵诀/听言解意篇/正心守道录)
                    onOpenVolume8Part1 = { navController.navigate(Routes.Volume8Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:赏罚驭灵诀 → 卷8)
                    onOpenVolume9Part1 = {},   // 2026-09-18 §12 取消跳转(原:听言解意篇 → 第九卷-1)
                    onOpenVolume10Part1 = {},   // 2026-09-18 §12 取消跳转(原:正心守道录 → 第十卷-1)
                ),
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
            Volume1Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part6 = { navController.navigate(Routes.Volume1Part6) },
            )
        }
        composable(Routes.Volume1Part6) {
            Volume1Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part7 = { navController.navigate(Routes.Volume1Part7) },
            )
        }
        composable(Routes.Volume1Part7) {
            Volume1Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part8 = { navController.navigate(Routes.Volume1Part8) },
            )
        }
        composable(Routes.Volume1Part8) {
            Volume1Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part9 = { navController.navigate(Routes.Volume1Part9) },
            )
        }
        composable(Routes.Volume1Part9) {
            Volume1Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part10 = { navController.navigate(Routes.Volume1Part10) },
            )
        }
        composable(Routes.Volume1Part10) {
            Volume1Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part11 = { navController.navigate(Routes.Volume1Part11) },
            )
        }
        composable(Routes.Volume1Part11) {
            Volume1Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part12 = { navController.navigate(Routes.Volume1Part12) },
            )
        }
        composable(Routes.Volume1Part12) {
            Volume1Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part13 = { navController.navigate(Routes.Volume1Part13) },
            )
        }
        composable(Routes.Volume1Part13) {
            Volume1Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part14 = { navController.navigate(Routes.Volume1Part14) },
            )
        }
        composable(Routes.Volume1Part14) {
            Volume1Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun6 = { navController.navigate(Routes.Gunlun6) },
            )
        }
        composable(Routes.Volume2Part1) {
            Volume2Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part2 = { navController.navigate(Routes.Volume2Part2) },
            )
        }
        composable(Routes.Volume2Part2) {
            Volume2Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part3 = { navController.navigate(Routes.Volume2Part3) },
            )
        }
        composable(Routes.Volume2Part3) {
            Volume2Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part4 = { navController.navigate(Routes.Volume2Part4) },
            )
        }
        composable(Routes.Volume2Part4) {
            Volume2Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part5 = { navController.navigate(Routes.Volume2Part5) },
            )
        }
        composable(Routes.Volume2Part5) {
            Volume2Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part6 = { navController.navigate(Routes.Volume2Part6) },
            )
        }
        composable(Routes.Volume2Part6) {
            Volume2Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part7 = { navController.navigate(Routes.Volume2Part7) },
            )
        }
        composable(Routes.Volume2Part7) {
            Volume2Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part8 = { navController.navigate(Routes.Volume2Part8) },
            )
        }
        composable(Routes.Volume2Part8) {
            Volume2Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part9 = { navController.navigate(Routes.Volume2Part9) },
            )
        }
        composable(Routes.Volume2Part9) {
            Volume2Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part10 = { navController.navigate(Routes.Volume2Part10) },
            )
        }
        composable(Routes.Volume2Part10) {
            Volume2Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part11 = { navController.navigate(Routes.Volume2Part11) },
            )
        }
        composable(Routes.Volume2Part11) {
            Volume2Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part12 = { navController.navigate(Routes.Volume2Part12) },
            )
        }
        composable(Routes.Volume2Part12) {
            Volume2Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part13 = { navController.navigate(Routes.Volume2Part13) },
            )
        }
        composable(Routes.Volume2Part13) {
            Volume2Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part14 = { navController.navigate(Routes.Volume2Part14) },
            )
        }
        composable(Routes.Volume2Part14) {
            Volume2Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part15 = { navController.navigate(Routes.Volume2Part15) },
            )
        }
        composable(Routes.Volume2Part15) {
            Volume2Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun10 = { navController.navigate(Routes.Gunlun10) },
            )
        }
        composable(Routes.Volume3Part1) {
            Volume3Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part2 = { navController.navigate(Routes.Volume3Part2) },
            )
        }
        composable(Routes.Volume3Part2) {
            Volume3Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part3 = { navController.navigate(Routes.Volume3Part3) },
            )
        }
        composable(Routes.Volume3Part3) {
            Volume3Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part4 = { navController.navigate(Routes.Volume3Part4) },
            )
        }
        composable(Routes.Volume3Part4) {
            Volume3Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part5 = { navController.navigate(Routes.Volume3Part5) },
            )
        }
        composable(Routes.Volume3Part5) {
            Volume3Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part6 = { navController.navigate(Routes.Volume3Part6) },
            )
        }
        composable(Routes.Volume3Part6) {
            Volume3Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part7 = { navController.navigate(Routes.Volume3Part7) },
            )
        }
        composable(Routes.Volume3Part7) {
            Volume3Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part8 = { navController.navigate(Routes.Volume3Part8) },
            )
        }
        composable(Routes.Volume3Part8) {
            Volume3Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part9 = { navController.navigate(Routes.Volume3Part9) },
            )
        }
        composable(Routes.Volume3Part9) {
            Volume3Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part10 = { navController.navigate(Routes.Volume3Part10) },
            )
        }
        composable(Routes.Volume3Part10) {
            Volume3Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part11 = { navController.navigate(Routes.Volume3Part11) },
            )
        }
        composable(Routes.Volume3Part11) {
            Volume3Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part12 = { navController.navigate(Routes.Volume3Part12) },
            )
        }
        composable(Routes.Volume3Part12) {
            Volume3Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part13 = { navController.navigate(Routes.Volume3Part13) },
            )
        }
        composable(Routes.Volume3Part13) {
            Volume3Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part14 = { navController.navigate(Routes.Volume3Part14) },
            )
        }
        composable(Routes.Volume3Part14) {
            Volume3Part14Screen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Gunlun7) {
            Gunlun7Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun8 = { navController.navigate(Routes.Gunlun8) },
                onOpenVolume3Part1 = { navController.navigate(Routes.Volume3Part1) },
            )
        }
        composable(Routes.Gunlun8) {
            Gunlun8Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun9 = { navController.navigate(Routes.Gunlun9) },
                onOpenVolume4Part1 = { navController.navigate(Routes.Volume4Part1) },
            )
        }
        composable(Routes.Volume4Part1) {
            Volume4Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part2 = { navController.navigate(Routes.Volume4Part2) },
            )
        }
        composable(Routes.Volume4Part2) {
            Volume4Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part3 = { navController.navigate(Routes.Volume4Part3) },
            )
        }
        composable(Routes.Volume4Part3) {
            Volume4Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part4 = { navController.navigate(Routes.Volume4Part4) },
            )
        }
        composable(Routes.Volume4Part4) {
            Volume4Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part5 = { navController.navigate(Routes.Volume4Part5) },
            )
        }
        composable(Routes.Volume4Part5) {
            Volume4Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part6 = { navController.navigate(Routes.Volume4Part6) },
            )
        }
        composable(Routes.Volume4Part6) {
            Volume4Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part7 = { navController.navigate(Routes.Volume4Part7) },
            )
        }
        composable(Routes.Volume4Part7) {
            Volume4Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part8 = { navController.navigate(Routes.Volume4Part8) },
            )
        }
        composable(Routes.Volume4Part8) {
            Volume4Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part9 = { navController.navigate(Routes.Volume4Part9) },
            )
        }
        composable(Routes.Volume4Part9) {
            Volume4Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part10 = { navController.navigate(Routes.Volume4Part10) },
            )
        }
        composable(Routes.Volume4Part10) {
            Volume4Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part11 = { navController.navigate(Routes.Volume4Part11) },
            )
        }
        composable(Routes.Volume4Part11) {
            Volume4Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part12 = { navController.navigate(Routes.Volume4Part12) },
            )
        }
        composable(Routes.Volume4Part12) {
            Volume4Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part13 = { navController.navigate(Routes.Volume4Part13) },
            )
        }
        composable(Routes.Volume4Part13) {
            Volume4Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part14 = { navController.navigate(Routes.Volume4Part14) },
            )
        }
        composable(Routes.Volume4Part14) {
            Volume4Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun8 = { navController.navigate(Routes.Gunlun8) },
            )
        }
        composable(Routes.Gunlun9) {
            Gunlun9Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun10 = { navController.navigate(Routes.Gunlun10) },
                onOpenVolume9Part1 = { navController.navigate(Routes.Volume9Part1) },
            )
        }
        composable(Routes.Volume9Part1) {
            Volume9Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part2 = { navController.navigate(Routes.Volume9Part2) },
            )
        }
        composable(Routes.Volume9Part2) {
            Volume9Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part3 = { navController.navigate(Routes.Volume9Part3) },
            )
        }
        composable(Routes.Volume9Part3) {
            Volume9Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part4 = { navController.navigate(Routes.Volume9Part4) },
            )
        }
        composable(Routes.Volume9Part4) {
            Volume9Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part5 = { navController.navigate(Routes.Volume9Part5) },
            )
        }
        composable(Routes.Volume9Part5) {
            Volume9Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part6 = { navController.navigate(Routes.Volume9Part6) },
            )
        }
        composable(Routes.Volume9Part6) {
            Volume9Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part7 = { navController.navigate(Routes.Volume9Part7) },
            )
        }
        composable(Routes.Volume9Part7) {
            Volume9Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part8 = { navController.navigate(Routes.Volume9Part8) },
            )
        }
        composable(Routes.Volume9Part8) {
            Volume9Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part9 = { navController.navigate(Routes.Volume9Part9) },
            )
        }
        composable(Routes.Volume9Part9) {
            Volume9Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part10 = { navController.navigate(Routes.Volume9Part10) },
            )
        }
        composable(Routes.Volume9Part10) {
            Volume9Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part11 = { navController.navigate(Routes.Volume9Part11) },
            )
        }
        composable(Routes.Volume9Part11) {
            Volume9Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part12 = { navController.navigate(Routes.Volume9Part12) },
            )
        }
        composable(Routes.Volume9Part12) {
            Volume9Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part13 = { navController.navigate(Routes.Volume9Part13) },
            )
        }
        composable(Routes.Volume9Part13) {
            Volume9Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part14 = { navController.navigate(Routes.Volume9Part14) },
            )
        }
        composable(Routes.Volume9Part14) {
            Volume9Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part15 = { navController.navigate(Routes.Volume9Part15) },
            )
        }
        composable(Routes.Volume9Part15) {
            Volume9Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun9 = { navController.navigate(Routes.Gunlun9) },
            )
        }
        composable(Routes.Gunlun10) {
            Gunlun10Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun11 = { navController.navigate(Routes.Gunlun11) },
                onOpenVolume2Part1 = { navController.navigate(Routes.Volume2Part1) },
            )
        }
        composable(Routes.Gunlun11) {
            Gunlun11Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun12 = { navController.navigate(Routes.Gunlun12) },
                onOpenVolume8Part1 = { navController.navigate(Routes.Volume8Part1) },
            )
        }
        composable(Routes.Volume8Part1) {
            Volume8Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part2 = { navController.navigate(Routes.Volume8Part2) },
            )
        }
        composable(Routes.Volume8Part2) {
            Volume8Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part3 = { navController.navigate(Routes.Volume8Part3) },
            )
        }
        composable(Routes.Volume8Part3) {
            Volume8Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part4 = { navController.navigate(Routes.Volume8Part4) },
            )
        }
        composable(Routes.Volume8Part4) {
            Volume8Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part5 = { navController.navigate(Routes.Volume8Part5) },
            )
        }
        composable(Routes.Volume8Part5) {
            Volume8Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part6 = { navController.navigate(Routes.Volume8Part6) },
            )
        }
        composable(Routes.Volume8Part6) {
            Volume8Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part7 = { navController.navigate(Routes.Volume8Part7) },
            )
        }
        composable(Routes.Volume8Part7) {
            Volume8Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part8 = { navController.navigate(Routes.Volume8Part8) },
            )
        }
        composable(Routes.Volume8Part8) {
            Volume8Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part9 = { navController.navigate(Routes.Volume8Part9) },
            )
        }
        composable(Routes.Volume8Part9) {
            Volume8Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part10 = { navController.navigate(Routes.Volume8Part10) },
            )
        }
        composable(Routes.Volume8Part10) {
            Volume8Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part11 = { navController.navigate(Routes.Volume8Part11) },
            )
        }
        composable(Routes.Volume8Part11) {
            Volume8Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part12 = { navController.navigate(Routes.Volume8Part12) },
            )
        }
        composable(Routes.Volume8Part12) {
            Volume8Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part13 = { navController.navigate(Routes.Volume8Part13) },
            )
        }
        composable(Routes.Volume8Part13) {
            Volume8Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part14 = { navController.navigate(Routes.Volume8Part14) },
            )
        }
        composable(Routes.Volume8Part14) {
            Volume8Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun11 = { navController.navigate(Routes.Gunlun11) },
            )
        }
        composable(Routes.Gunlun12) {
            Gunlun12Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun13 = { navController.navigate(Routes.Gunlun13) },
                onOpenVolume5Part1 = { navController.navigate(Routes.Volume5Part1) },
            )
        }
        composable(Routes.Volume5Part1) {
            Volume5Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part2 = { navController.navigate(Routes.Volume5Part2) },
            )
        }
        composable(Routes.Volume5Part2) {
            Volume5Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part3 = { navController.navigate(Routes.Volume5Part3) },
            )
        }
        composable(Routes.Volume5Part3) {
            Volume5Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part4 = { navController.navigate(Routes.Volume5Part4) },
            )
        }
        composable(Routes.Volume5Part4) {
            Volume5Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part5 = { navController.navigate(Routes.Volume5Part5) },
            )
        }
        composable(Routes.Volume5Part5) {
            Volume5Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part6 = { navController.navigate(Routes.Volume5Part6) },
            )
        }
        composable(Routes.Volume5Part6) {
            Volume5Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part7 = { navController.navigate(Routes.Volume5Part7) },
            )
        }
        composable(Routes.Volume5Part7) {
            Volume5Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part8 = { navController.navigate(Routes.Volume5Part8) },
            )
        }
        composable(Routes.Volume5Part8) {
            Volume5Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part9 = { navController.navigate(Routes.Volume5Part9) },
            )
        }
        composable(Routes.Volume5Part9) {
            Volume5Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part10 = { navController.navigate(Routes.Volume5Part10) },
            )
        }
        composable(Routes.Volume5Part10) {
            Volume5Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part11 = { navController.navigate(Routes.Volume5Part11) },
            )
        }
        composable(Routes.Volume5Part11) {
            Volume5Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part12 = { navController.navigate(Routes.Volume5Part12) },
            )
        }
        composable(Routes.Volume5Part12) {
            Volume5Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part13 = { navController.navigate(Routes.Volume5Part13) },
            )
        }
        composable(Routes.Volume5Part13) {
            Volume5Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part14 = { navController.navigate(Routes.Volume5Part14) },
            )
        }
        composable(Routes.Volume5Part14) {
            Volume5Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part15 = { navController.navigate(Routes.Volume5Part15) },
            )
        }
        composable(Routes.Volume5Part15) {
            Volume5Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun12 = { navController.navigate(Routes.Gunlun12) },
            )
        }
        composable(Routes.Gunlun13) {
            Gunlun13Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun14) },
                onOpenVolume10Part1 = { navController.navigate(Routes.Volume10Part1) },
            )
        }
        composable(Routes.Volume10Part1) {
            Volume10Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part2 = { navController.navigate(Routes.Volume10Part2) },
            )
        }
        composable(Routes.Volume10Part2) {
            Volume10Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part3 = { navController.navigate(Routes.Volume10Part3) },
            )
        }
        composable(Routes.Volume10Part3) {
            Volume10Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part4 = { navController.navigate(Routes.Volume10Part4) },
            )
        }
        composable(Routes.Volume10Part4) {
            Volume10Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part5 = { navController.navigate(Routes.Volume10Part5) },
            )
        }
        composable(Routes.Volume10Part5) {
            Volume10Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part6 = { navController.navigate(Routes.Volume10Part6) },
            )
        }
        composable(Routes.Volume10Part6) {
            Volume10Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part7 = { navController.navigate(Routes.Volume10Part7) },
            )
        }
        composable(Routes.Volume10Part7) {
            Volume10Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part8 = { navController.navigate(Routes.Volume10Part8) },
            )
        }
        composable(Routes.Volume10Part8) {
            Volume10Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part9 = { navController.navigate(Routes.Volume10Part9) },
            )
        }
        composable(Routes.Volume10Part9) {
            Volume10Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part10 = { navController.navigate(Routes.Volume10Part10) },
            )
        }
        composable(Routes.Volume10Part10) {
            Volume10Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part11 = { navController.navigate(Routes.Volume10Part11) },
            )
        }
        composable(Routes.Volume10Part11) {
            Volume10Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part12 = { navController.navigate(Routes.Volume10Part12) },
            )
        }
        composable(Routes.Volume10Part12) {
            Volume10Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part13 = { navController.navigate(Routes.Volume10Part13) },
            )
        }
        composable(Routes.Volume10Part13) {
            Volume10Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part14 = { navController.navigate(Routes.Volume10Part14) },
            )
        }
        composable(Routes.Volume10Part14) {
            Volume10Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun13 = { navController.navigate(Routes.Gunlun13) },
            )
        }
        composable(Routes.Gunlun14) {
            Gunlun14Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun15) },
                onOpenVolume6Part1 = { navController.navigate(Routes.Volume6Part1) },
            )
        }
        composable(Routes.Volume6Part1) {
            Volume6Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part2 = { navController.navigate(Routes.Volume6Part2) },
            )
        }
        composable(Routes.Volume6Part2) {
            Volume6Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part3 = { navController.navigate(Routes.Volume6Part3) },
            )
        }
        composable(Routes.Volume6Part3) {
            Volume6Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part4 = { navController.navigate(Routes.Volume6Part4) },
            )
        }
        composable(Routes.Volume6Part4) {
            Volume6Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part5 = { navController.navigate(Routes.Volume6Part5) },
            )
        }
        composable(Routes.Volume6Part5) {
            Volume6Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part6 = { navController.navigate(Routes.Volume6Part6) },
            )
        }
        composable(Routes.Volume6Part6) {
            Volume6Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part7 = { navController.navigate(Routes.Volume6Part7) },
            )
        }
        composable(Routes.Volume6Part7) {
            Volume6Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part8 = { navController.navigate(Routes.Volume6Part8) },
            )
        }
        composable(Routes.Volume6Part8) {
            Volume6Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part9 = { navController.navigate(Routes.Volume6Part9) },
            )
        }
        composable(Routes.Volume6Part9) {
            Volume6Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part10 = { navController.navigate(Routes.Volume6Part10) },
            )
        }
        composable(Routes.Volume6Part10) {
            Volume6Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part11 = { navController.navigate(Routes.Volume6Part11) },
            )
        }
        composable(Routes.Volume6Part11) {
            Volume6Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part12 = { navController.navigate(Routes.Volume6Part12) },
            )
        }
        composable(Routes.Volume6Part12) {
            Volume6Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part13 = { navController.navigate(Routes.Volume6Part13) },
            )
        }
        composable(Routes.Volume6Part13) {
            Volume6Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part14 = { navController.navigate(Routes.Volume6Part14) },
            )
        }
        composable(Routes.Volume6Part14) {
            Volume6Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part15 = { navController.navigate(Routes.Volume6Part15) },
            )
        }
        composable(Routes.Volume6Part15) {
            Volume6Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun14 = { navController.navigate(Routes.Gunlun14) },
            )
        }
        composable(Routes.Gunlun15) {
            Gunlun15Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part1 = { navController.navigate(Routes.Volume7Part1) },
            )
        }
        composable(Routes.Gunlun16) {
            Gunlun16Screen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Volume7Part1) {
            Volume7Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part2 = { navController.navigate(Routes.Volume7Part2) },
            )
        }
        composable(Routes.Volume7Part2) {
            Volume7Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part3 = { navController.navigate(Routes.Volume7Part3) },
            )
        }
        composable(Routes.Volume7Part3) {
            Volume7Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part4 = { navController.navigate(Routes.Volume7Part4) },
            )
        }
        composable(Routes.Volume7Part4) {
            Volume7Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part5 = { navController.navigate(Routes.Volume7Part5) },
            )
        }
        composable(Routes.Volume7Part5) {
            Volume7Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part6 = { navController.navigate(Routes.Volume7Part6) },
            )
        }
        composable(Routes.Volume7Part6) {
            Volume7Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part7 = { navController.navigate(Routes.Volume7Part7) },
            )
        }
        composable(Routes.Volume7Part7) {
            Volume7Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part8 = { navController.navigate(Routes.Volume7Part8) },
            )
        }
        composable(Routes.Volume7Part8) {
            Volume7Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part9 = { navController.navigate(Routes.Volume7Part9) },
            )
        }
        composable(Routes.Volume7Part9) {
            Volume7Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part10 = { navController.navigate(Routes.Volume7Part10) },
            )
        }
        composable(Routes.Volume7Part10) {
            Volume7Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part11 = { navController.navigate(Routes.Volume7Part11) },
            )
        }
        composable(Routes.Volume7Part11) {
            Volume7Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part12 = { navController.navigate(Routes.Volume7Part12) },
            )
        }
        composable(Routes.Volume7Part12) {
            Volume7Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun16 = { navController.navigate(Routes.Gunlun16) },
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
                onSaveLearningCard = { form, afterSave ->
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
                            afterSave()
                        }
                    }
                },
                onSubmitMigrationEvidence = { manualPageIds, revisionReason, afterSave ->
                    latestVersion?.let { version ->
                        creationViewModel.submitMigrationEvidence(
                            projectId = projectId,
                            creationVersionId = version.id,
                            manualPageIds = manualPageIds,
                            revisionReason = revisionReason,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            afterSave()
                        }
                    }
                },
                onSaveProvenance = { form, afterSave ->
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
                            afterSave()
                        }
                    }
                },
                onSaveSealCheck = { form, afterSave ->
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
                            afterSave()
                        }
                    }
                },
                onSaveReflectionPackage = { sealForm, learningForm, afterSave ->
                    latestVersion?.let { version ->
                        creationViewModel.saveSealReflectionPackage(
                            projectId = projectId,
                            versionId = version.id,
                            workDescription = sealForm.workDescription,
                            learningReflection = sealForm.learningReflection,
                            nextImprovement = sealForm.nextImprovement,
                            identityPrivacyConfirmed = sealForm.identityPrivacyConfirmed,
                            contactPrivacyConfirmed = sealForm.contactPrivacyConfirmed,
                            portraitRightsConfirmed = sealForm.portraitRightsConfirmed,
                            sealRowVersion = bundle?.sealCheck?.rowVersion,
                            manualPageIds = learningForm.manualPageIds,
                            methodSummary = learningForm.methodSummary,
                            learningRowVersion = bundle?.learningCard?.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            afterSave()
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
                onWithdrawWork = { projectId ->
                    luggageViewModel.withdrawPublication(projectId) {
                        creationViewModel.loadRecentProjects()
                    }
                },
                onDeleteWork = { projectId ->
                    luggageViewModel.deleteCreationProject(projectId) {
                        creationViewModel.loadRecentProjects()
                        navController.navigate(Routes.Gongfang) {
                            popUpTo(Routes.Chuangzuodangan) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onAppealWork = { projectId, caseId, reason ->
                    luggageViewModel.createAppeal(projectId, caseId, reason)
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
                onRetryArchiveDetail = {
                    luggageDetailState.creationDetailProjectId?.let(luggageViewModel::loadCreationDetail)
                },
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
