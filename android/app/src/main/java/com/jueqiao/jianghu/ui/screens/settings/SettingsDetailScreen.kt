package com.jueqiao.jianghu.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jueqiao.jianghu.BuildConfig
import com.jueqiao.jianghu.JianghuApp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.auth.BlacklistEntryDto
import com.jueqiao.jianghu.auth.UserDto
import com.jueqiao.jianghu.ui.components.SettingsPaperSurface
import com.jueqiao.jianghu.ui.theme.YaHei

enum class SettingsPage(val title: String) {
    Account("账号管理"),
    Message("消息设置"),
    General("通用设置"),
    Sound("声音调节"),
    Blacklist("黑名单管理"),
    Collection("个人信息收集清单"),
    Sharing("第三方信息共享清单"),
    Help("帮助中心"),
    About("关于"),
    DataRecovery("数据恢复"),
}

private val Paper = Color(0xFFF5E8D4)
private val Ink = Color(0xFF25271E)
private val MutedInk = Color(0xFF747361)
private val Bamboo = Color(0xFF70986B)
private val PaleBamboo = Color(0xFFDDE8D3)
private val Divider = Color(0xFFD8D2C2)

@Composable
fun SettingsDetailScreen(
    page: SettingsPage,
    onBack: () -> Unit,
    onChangePassword: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenAgreement: () -> Unit = {},
) {
    val context = LocalContext.current
    val application = context.applicationContext as JianghuApp
    val factory = remember(application) {
        SettingsViewModelFactory(application.settingsRepository)
    }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val snapshot by settingsViewModel.snapshot.collectAsStateWithLifecycle()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by settingsViewModel.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val feedbackTone = remember(snapshot.soundEnabled, snapshot.effectVolume) {
        if (snapshot.soundEnabled) {
            ToneGenerator(
                AudioManager.STREAM_MUSIC,
                (snapshot.effectVolume.coerceIn(0f, 1f) * 35).toInt(),
            )
        } else {
            null
        }
    }
    DisposableEffect(feedbackTone) {
        onDispose { feedbackTone?.release() }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        settingsViewModel.setBoolean(SettingsPreferences.Keys.MessageEnabled, granted)
        settingsViewModel.showMessage(
            if (granted) "系统通知权限已开启" else "未获得系统通知权限，提醒保持关闭",
        )
    }

    LaunchedEffect(page) {
        when (page) {
            SettingsPage.Account -> settingsViewModel.loadAccount()
            SettingsPage.Blacklist -> settingsViewModel.loadBlacklist()
            else -> Unit
        }
    }

    LaunchedEffect(uiState.eventMessage) {
        uiState.eventMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            settingsViewModel.consumeEvent()
        }
    }

    fun updateBoolean(key: String, value: Boolean) {
        if (snapshot.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        feedbackTone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 24)
        val needsPermission = key == SettingsPreferences.Keys.MessageEnabled &&
            value &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            settingsViewModel.setBoolean(key, value)
        }
    }

    fun updateFloat(key: String, value: Float) {
        if (snapshot.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        feedbackTone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 40)
        settingsViewModel.setFloat(key, value)
    }

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
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                SettingsHeader(title = page.title, onBack = onBack)
                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        when (page) {
                            SettingsPage.Account -> AccountContent(
                                user = currentUser,
                                activeSessionCount = uiState.sessions.size,
                                isLoading = uiState.isLoadingSessions,
                                onChangePassword = onChangePassword,
                                onRefresh = settingsViewModel::loadAccount,
                            )
                            SettingsPage.Message -> MessageContent(
                                snapshot = snapshot,
                                notificationsAllowed = SettingsNotifications.notificationsAllowed(context),
                                onChanged = ::updateBoolean,
                            )
                            SettingsPage.General -> GeneralContent(snapshot, ::updateBoolean)
                            SettingsPage.Sound -> SoundContent(
                                snapshot = snapshot,
                                onBooleanChanged = ::updateBoolean,
                                onFloatChanged = ::updateFloat,
                            )
                            SettingsPage.Blacklist -> BlacklistContent(
                                entries = uiState.blacklist,
                                isLoading = uiState.isLoadingBlacklist,
                                onRefresh = { settingsViewModel.loadBlacklist(showMessage = true) },
                                onRemove = settingsViewModel::removeFromBlacklist,
                            )
                            SettingsPage.Collection -> CollectionContent()
                            SettingsPage.Sharing -> SharingContent()
                            SettingsPage.Help -> HelpContent(
                                isSubmitting = uiState.isSubmittingFeedback,
                                onSubmitFeedback = settingsViewModel::submitFeedback,
                            )
                            SettingsPage.About -> AboutContent(onOpenPrivacy, onOpenAgreement)
                            SettingsPage.DataRecovery -> DataRecoveryContent(
                                onRestored = settingsViewModel::restoreDefaults,
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
        )
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PaleBamboo),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "返回设置",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = title,
            color = Ink,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = if (title.length > 8) 20.sp else 23.sp,
            ),
        )
    }
}

@Composable
private fun AccountContent(
    user: UserDto?,
    activeSessionCount: Int,
    isLoading: Boolean,
    onChangePassword: () -> Unit,
    onRefresh: () -> Unit,
) {
    PageIntro(
        icon = Icons.Default.Security,
        title = "账号安全",
        description = "账号资料与登录设备来自安全服务，涉及身份修改时需要重新验证手机号。",
    )
    SettingValueRow("江湖称号", user?.nickname ?: "加载中")
    SettingValueRow("绑定手机号", user?.phoneMasked ?: "加载中")
    SettingValueRow("年龄身份", user?.ageBand.ageBandLabel())
    SettingValueRow("账号状态", user?.status.accountStatusLabel())
    SettingValueRow(
        "有效登录设备",
        if (isLoading) "查询中" else "${activeSessionCount.coerceAtLeast(1)} 台",
    )
    SettingActionRow("修改登录密码", "通过短信验证码重设", onClick = onChangePassword)
    SecondaryAction("刷新账号状态", Icons.Default.Refresh, onRefresh)
    InfoCard("安全提示", "请勿向他人透露短信验证码。切换账号或退出登录时，本机会清除通行令并通知服务端撤销当前会话。")
}

private fun String?.ageBandLabel(): String = when (this) {
    "UNDER_14" -> "未满14岁"
    "AGE_14_TO_17" -> "14–17岁"
    "ADULT" -> "18岁及以上"
    else -> "加载中"
}

private fun String?.accountStatusLabel(): String = when (this) {
    "ACTIVE" -> "正常"
    "LOCKED" -> "已锁定"
    "DELETED" -> "已注销"
    else -> "加载中"
}

@Composable
private fun MessageContent(
    snapshot: SettingsSnapshot,
    notificationsAllowed: Boolean,
    onChanged: (String, Boolean) -> Unit,
) {
    PageIntro(Icons.Default.Notifications, "通知偏好", "提醒设置会立即在本机生效，并在登录状态下同步到账号。")
    SettingValueRow("系统通知权限", if (notificationsAllowed) "已允许" else "未允许")
    if (!notificationsAllowed) {
        SecondaryAction("开启系统通知权限", Icons.Default.Notifications) {
            onChanged(SettingsPreferences.Keys.MessageEnabled, true)
        }
    }
    PreferenceSwitch(snapshot.messageEnabled, SettingsPreferences.Keys.MessageEnabled, "接收消息通知", "关闭后停止系统与应用内提醒", onChanged)
    PreferenceSwitch(snapshot.learningReminder, SettingsPreferences.Keys.LearningReminder, "学习提醒", "每天 20:00 提醒未完成的学习任务", onChanged)
    PreferenceSwitch(snapshot.workUpdates, SettingsPreferences.Keys.WorkUpdates, "作品状态", "生成完成与审核结果", onChanged)
    PreferenceSwitch(snapshot.serviceMessages, SettingsPreferences.Keys.ServiceMessages, "活动与系统消息", "版本更新、活动和安全通知", onChanged)
    PreferenceSwitch(snapshot.quietHours, SettingsPreferences.Keys.QuietHours, "夜间免打扰", "22:00 至次日 08:00 静默提醒", onChanged)
}

@Composable
private fun GeneralContent(
    snapshot: SettingsSnapshot,
    onChanged: (String, Boolean) -> Unit,
) {
    PageIntro(Icons.Default.CheckCircle, "使用偏好", "调整创作、下载与显示体验。")
    PreferenceSwitch(snapshot.autoSave, SettingsPreferences.Keys.AutoSave, "自动保存创作", "生图、图片与要素输入会保存为本机草稿", onChanged)
    PreferenceSwitch(snapshot.wifiOnly, SettingsPreferences.Keys.WifiOnly, "仅 Wi-Fi 下载素材", "素材下载服务将遵循此网络限制", onChanged)
    PreferenceSwitch(snapshot.hapticFeedback, SettingsPreferences.Keys.HapticFeedback, "触感反馈", "点击重要操作时提供轻微振动", onChanged)
    PreferenceSwitch(snapshot.largeText, SettingsPreferences.Keys.LargeText, "大字号模式", "即时增大全应用文字", onChanged)
}

@Composable
private fun SoundContent(
    snapshot: SettingsSnapshot,
    onBooleanChanged: (String, Boolean) -> Unit,
    onFloatChanged: (String, Float) -> Unit,
) {
    PageIntro(Icons.AutoMirrored.Filled.VolumeUp, "声音", "分别调节背景音乐和交互音效。")
    PreferenceSwitch(snapshot.soundEnabled, SettingsPreferences.Keys.SoundEnabled, "开启声音", "关闭后停止应用交互音效", onBooleanChanged)
    PreferenceSlider(snapshot.musicVolume, SettingsPreferences.Keys.MusicVolume, "背景音乐", onFloatChanged)
    PreferenceSlider(snapshot.effectVolume, SettingsPreferences.Keys.EffectVolume, "交互音效", onFloatChanged)
}

@Composable
private fun BlacklistContent(
    entries: List<BlacklistEntryDto>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onRemove: (String) -> Unit,
) {
    PageIntro(Icons.Default.Lock, "黑名单", "被拉黑的用户无法向你发送互动消息。")
    when {
        isLoading && entries.isEmpty() -> LoadingState("正在获取黑名单")
        entries.isEmpty() -> EmptyState("暂无已拉黑用户", "后续可在用户资料或互动记录中将对方加入黑名单。")
        else -> entries.forEach { entry ->
            BlacklistEntryRow(entry, onRemove)
        }
    }
    SecondaryAction("刷新名单", Icons.Default.Refresh, onRefresh)
}

@Composable
private fun BlacklistEntryRow(
    entry: BlacklistEntryDto,
    onRemove: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.38f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.nickname, color = Ink, style = TextStyle(fontFamily = YaHei, fontSize = 16.sp, fontWeight = FontWeight.Bold))
            Text("已限制互动", color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 12.sp))
        }
        TextButton(onClick = { onRemove(entry.userId) }) {
            Icon(Icons.Default.Delete, contentDescription = "移出黑名单", tint = Color(0xFFB84A42))
            Spacer(Modifier.size(4.dp))
            Text("移除", color = Color(0xFFB84A42), fontFamily = YaHei)
        }
    }
}

@Composable
private fun CollectionContent() {
    PageIntro(Icons.Default.Security, "收集范围说明", "仅在提供对应功能时处理必要信息；具体规则以隐私政策为准。")
    InformationEntry("手机号", "用于注册、登录、安全验证与找回密码", "用户主动提供")
    InformationEntry("年龄段", "用于未成年人保护和监护人同意流程", "注册时选择")
    InformationEntry("创作描述与作品", "用于完成用户主动发起的智能创作", "使用创作功能时产生")
    InformationEntry("设备与错误日志", "用于保障服务安全、定位崩溃和异常", "应用运行时产生")
}

@Composable
private fun SharingContent() {
    PageIntro(Icons.Default.Info, "共享范围说明", "只有在提供功能所必需时才会向服务提供方传输最少信息。")
    InformationEntry("AI 内容生成服务", "处理用户主动提交的创作描述并返回生成结果", "创作描述；仅在使用该功能时")
    InformationEntry("短信验证服务", "发送注册、登录安全与密码找回验证码", "手机号码与验证码发送状态")
    InfoCard("特别说明", "不会出售个人信息。正式上线前，服务提供方名称、处理规则与保存期限需以最终合规清单为准。")
}

@Composable
private fun HelpContent(
    isSubmitting: Boolean,
    onSubmitFeedback: (String, () -> Unit) -> Unit,
) {
    var expandedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showFeedback by rememberSaveable { mutableStateOf(false) }
    var feedbackText by rememberSaveable { mutableStateOf("") }
    val questions = listOf(
        "如何找回登录密码？" to "进入账号管理，选择“修改登录密码”，通过已绑定手机号的验证码完成重设。",
        "作品生成后在哪里查看？" to "从首页进入“作品创作”，可查看创作流程与已有作品。",
        "为什么没有收到提醒？" to "请先确认消息设置中的总开关和对应提醒已开启，并检查系统通知权限。",
        "如何恢复设置？" to "进入“数据恢复”，可将通知、通用和声音偏好恢复为默认值。",
    )
    PageIntro(Icons.Default.Info, "常见问题", "点击问题查看答案。")
    questions.forEachIndexed { index, (question, answer) ->
        val expanded = expandedIndex == index
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.4f))
                .clickable { expandedIndex = if (expanded) -1 else index }
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = question,
                    modifier = Modifier.weight(1f),
                    color = Ink,
                    style = TextStyle(fontFamily = YaHei, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起答案" else "展开答案",
                    tint = MutedInk,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Text(answer, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 22.sp))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    SecondaryAction("意见反馈", Icons.Default.Info) { showFeedback = true }

    if (showFeedback) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showFeedback = false },
            title = { Text("提交意见反馈", fontFamily = YaHei) },
            text = {
                Column {
                    Text(
                        "请描述遇到的问题或建议（至少10个字）。",
                        color = MutedInk,
                        style = TextStyle(fontFamily = YaHei, fontSize = 13.sp),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { if (it.length <= 1000) feedbackText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        enabled = !isSubmitting,
                        placeholder = { Text("例如：出现问题的页面、操作步骤和期望结果") },
                        supportingText = { Text("${feedbackText.length}/1000") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = feedbackText.trim().length >= 10 && !isSubmitting,
                    onClick = {
                        onSubmitFeedback(feedbackText.trim()) {
                            feedbackText = ""
                            showFeedback = false
                        }
                    },
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Bamboo,
                        )
                    } else {
                        Text("提交", fontFamily = YaHei, color = Bamboo)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = { showFeedback = false },
                ) {
                    Text("取消", fontFamily = YaHei, color = MutedInk)
                }
            },
            containerColor = Paper,
        )
    }
}

@Composable
private fun AboutContent(onOpenPrivacy: () -> Unit, onOpenAgreement: () -> Unit) {
    PageIntro(Icons.Default.Info, "机巧江湖", "让人工智能知识在探索、创作和故事中自然发生。")
    SettingValueRow("版本", BuildConfig.VERSION_NAME)
    SettingValueRow("构建", BuildConfig.VERSION_CODE.toString())
    SettingActionRow("隐私政策", "查看当前版本", onClick = onOpenPrivacy)
    SettingActionRow("用户协议", "查看当前版本", onClick = onOpenAgreement)
    InfoCard("版权信息", "© 2026 机巧江湖项目组。保留所有权利。")
}

@Composable
private fun DataRecoveryContent(onRestored: () -> Unit) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    PageIntro(Icons.Default.Refresh, "恢复个性化设置", "将消息、通用和声音设置恢复为初始状态。作品与学习记录不会被删除。")
    InfoCard("当前支持范围", "恢复后会重新安排学习提醒并同步账号偏好；本机创作草稿、作品与学习记录不会被删除。")
    PrimaryAction("恢复默认设置") { showConfirm = true }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("恢复默认设置？", fontFamily = YaHei) },
            text = { Text("通知、通用和声音偏好将恢复为默认值。", fontFamily = YaHei) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onRestored()
                    },
                ) { Text("确认恢复", fontFamily = YaHei, color = Bamboo) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("取消", fontFamily = YaHei, color = MutedInk)
                }
            },
            containerColor = Paper,
        )
    }
}

@Composable
private fun PageIntro(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PaleBamboo.copy(alpha = 0.75f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Bamboo, modifier = Modifier.size(30.dp))
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp))
            Spacer(Modifier.height(3.dp))
            Text(description, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 19.sp))
        }
    }
}

@Composable
private fun PreferenceSwitch(
    checked: Boolean,
    key: String,
    title: String,
    description: String,
    onChanged: (String, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onChanged(key, !checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontSize = 16.sp))
            Text(description, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 17.sp))
        }
        Switch(
            checked = checked,
            onCheckedChange = { onChanged(key, it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Bamboo,
                uncheckedTrackColor = Color(0xFFCAC6B8),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
    HorizontalDivider(color = Divider, thickness = 0.5.dp)
}

@Composable
private fun PreferenceSlider(
    value: Float,
    key: String,
    title: String,
    onChanged: (String, Float) -> Unit,
) {
    var sliderValue by remember(key) { mutableStateOf(value) }
    LaunchedEffect(value) { sliderValue = value }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), color = Ink, style = TextStyle(fontFamily = YaHei, fontSize = 16.sp))
            Text("${(sliderValue * 100).toInt()}%", color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onChanged(key, sliderValue) },
        )
    }
    HorizontalDivider(color = Divider, thickness = 0.5.dp)
}

@Composable
private fun SettingValueRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), color = Ink, style = TextStyle(fontFamily = YaHei, fontSize = 16.sp))
        Text(value, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
    }
    HorizontalDivider(color = Divider, thickness = 0.5.dp)
}

@Composable
private fun SettingActionRow(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontSize = 16.sp))
            Text(description, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 12.sp))
        }
        Image(
            painter = painterResource(R.drawable.img_chevron_right),
            contentDescription = "进入$title",
            modifier = Modifier.size(16.dp),
        )
    }
    HorizontalDivider(color = Divider, thickness = 0.5.dp)
}

@Composable
private fun InformationEntry(title: String, purpose: String, scope: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.38f))
            .padding(14.dp),
    ) {
        Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 16.sp))
        Spacer(Modifier.height(7.dp))
        Text("用途：$purpose", color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp))
        Spacer(Modifier.height(3.dp))
        Text("范围：$scope", color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp))
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8E9).copy(alpha = 0.7f))
            .padding(14.dp),
    ) {
        Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 15.sp))
        Spacer(Modifier.height(5.dp))
        Text(body, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp))
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Bamboo.copy(alpha = 0.6f), modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Ink, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Spacer(Modifier.height(6.dp))
        Text(description, color = MutedInk, textAlign = TextAlign.Center, style = TextStyle(fontFamily = YaHei, fontSize = 13.sp, lineHeight = 20.sp))
    }
}

@Composable
private fun LoadingState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(34.dp),
            color = Bamboo,
            strokeWidth = 3.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(text, color = MutedInk, style = TextStyle(fontFamily = YaHei, fontSize = 14.sp))
    }
}

@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Bamboo),
        shape = RoundedCornerShape(25.dp),
    ) {
        Text(text, color = Color.White, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 16.sp))
    }
}

@Composable
private fun SecondaryAction(text: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Bamboo)
        Spacer(Modifier.size(8.dp))
        Text(text, color = Bamboo, style = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 15.sp))
    }
}
