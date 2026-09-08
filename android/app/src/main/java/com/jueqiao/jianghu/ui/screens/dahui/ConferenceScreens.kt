package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jueqiao.jianghu.conference.ConferenceUiState
import com.jueqiao.jianghu.luggage.ConferenceDerivativeRequestDto
import com.jueqiao.jianghu.luggage.ConferenceLetterDto
import com.jueqiao.jianghu.luggage.ConferenceMatchQuestionDto
import com.jueqiao.jianghu.luggage.ConferenceMatchResultDto
import com.jueqiao.jianghu.luggage.ConferenceReviewDto
import com.jueqiao.jianghu.luggage.ConferenceWorkDto
import com.jueqiao.jianghu.luggage.ManualPageDto
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceHubScreen(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenWork: (String) -> Unit,
    onOpenCollections: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenLetters: () -> Unit,
    onOpenMatch: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("演武场") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCollections) {
                        Icon(Icons.Outlined.Bookmark, contentDescription = "我的收藏")
                    }
                    IconButton(onClick = onOpenRequests) {
                        Icon(Icons.Outlined.Inbox, contentDescription = "授权记录")
                    }
                    IconButton(onClick = onOpenLetters) {
                        Icon(Icons.Outlined.MailOutline, contentDescription = "大会书信")
                    }
                    IconButton(onClick = onOpenMatch) {
                        Icon(Icons.Outlined.SportsKabaddi, contentDescription = "匿名切磋")
                    }
                    IconButton(onClick = onLoad) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading && state.works.isEmpty() -> LoadingContent(padding)
            state.error != null && state.works.isEmpty() -> ErrorContent(
                padding = padding,
                message = state.error,
                onRetry = onLoad,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.error != null) {
                    item { InlineError(state.error, onLoad) }
                }
                items(state.works, key = ConferenceWorkDto::publicationId) { work ->
                    ConferenceWorkCard(work = work, onClick = { onOpenWork(work.publicationId) })
                }
                if (state.nextCursor != null) {
                    item {
                        OutlinedButton(
                            onClick = onLoadMore,
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("加载更多")
                        }
                    }
                }
                if (state.works.isEmpty()) {
                    item { EmptyContent("暂无可见作品") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceWorkScreen(
    publicationId: String,
    state: ConferenceUiState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onAddCollection: (String) -> Unit,
    onCreateReview: (String, String, String) -> Unit,
    onDecideReview: (ConferenceReviewDto, String, String?) -> Unit,
    onAdoptReview: (ConferenceReviewDto, String, String) -> Unit,
    onReportReview: (String, String, String?) -> Unit,
    onCreateDerivativeRequest: (String, String) -> Unit,
    onOpenRevision: (String) -> Unit,
    onMessageShown: () -> Unit,
) {
    LaunchedEffect(publicationId) { onLoad(publicationId) }
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHost.showSnackbar(it)
            onMessageShown()
        }
    }
    val work = state.work?.takeIf { it.publicationId == publicationId }
    val adoptionVersions = state.adoptionVersions.filter {
        work?.isOwner == true && it.projectId == work.projectId &&
            it.versionNumber > work.versionNumber
    }.sortedByDescending { it.versionNumber }
    var template by rememberSaveable { mutableStateOf("OBSERVATION") }
    var reviewContent by rememberSaveable { mutableStateOf("") }
    var requestedUse by rememberSaveable { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ConferenceReviewDto?>(null) }
    var authorReply by rememberSaveable { mutableStateOf("") }
    var adoptionTarget by remember { mutableStateOf<ConferenceReviewDto?>(null) }
    var adoptionVersionId by rememberSaveable { mutableStateOf("") }
    var adoptionSummary by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.reviews) {
        if (adoptionTarget != null && state.reviews.any {
                it.id == adoptionTarget?.id && it.adoptedInCreationVersionId != null
            }
        ) {
            adoptionTarget = null
        }
    }
    var reportTarget by remember { mutableStateOf<ConferenceReviewDto?>(null) }
    var reportReason by rememberSaveable { mutableStateOf("INAPPROPRIATE_CONTENT") }
    var reportDetails by rememberSaveable { mutableStateOf("") }
    replyTarget?.let { review ->
        AlertDialog(
            onDismissRequest = { replyTarget = null },
            title = { Text("回复评语") },
            text = {
                OutlinedTextField(
                    value = authorReply,
                    onValueChange = { authorReply = it },
                    label = { Text("回复内容") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDecideReview(review, "REPLY", authorReply)
                        replyTarget = null
                    },
                    enabled = authorReply.isNotBlank(),
                ) { Text("提交") }
            },
            dismissButton = {
                OutlinedButton(onClick = { replyTarget = null }) { Text("取消") }
            },
        )
    }
    adoptionTarget?.let { review ->
        AlertDialog(
            onDismissRequest = { if (!state.loading) adoptionTarget = null },
            title = { Text("登记改进版本") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("选择在参会版本之后、实际按建议改进的作品版本。")
                    if (adoptionVersions.isEmpty()) {
                        Text("还没有新版本，请先进入创作修订并保存。")
                        OutlinedButton(
                            onClick = {
                                adoptionTarget = null
                                work?.let { onOpenRevision(it.projectId) }
                            },
                            enabled = !state.loading,
                        ) { Text("去创作修订") }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(adoptionVersions) { version ->
                            FilterChip(
                                selected = adoptionVersionId == version.id,
                                onClick = { adoptionVersionId = version.id },
                                enabled = !state.loading,
                                label = { Text("第 ${version.versionNumber} 版") },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = adoptionSummary,
                        onValueChange = { adoptionSummary = it.take(500) },
                        label = { Text("说明这条建议带来了什么改进") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !state.loading,
                    )
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdoptReview(review, adoptionVersionId, adoptionSummary)
                    },
                    enabled = adoptionVersions.any { it.id == adoptionVersionId } &&
                        adoptionSummary.isNotBlank() && !state.loading,
                ) { Text("确认登记") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { adoptionTarget = null },
                    enabled = !state.loading,
                ) { Text("取消") }
            },
        )
    }
    reportTarget?.let { review ->
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("举报评语") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ReviewReportReasons) { reason ->
                            FilterChip(
                                selected = reportReason == reason,
                                onClick = { reportReason = reason },
                                label = { Text(ReviewReportReasonLabels[reason] ?: reason) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it.take(500) },
                        label = { Text("补充说明（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportReview(review.id, reportReason, reportDetails)
                        reportTarget = null
                    },
                ) { Text("提交") }
            },
            dismissButton = {
                OutlinedButton(onClick = { reportTarget = null }) { Text("取消") }
            },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作品详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onLoad(publicationId) }, enabled = !state.loading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        when {
            state.loading && work == null -> LoadingContent(padding)
            state.error != null && work == null -> ErrorContent(
                padding = padding,
                message = state.error,
                onRetry = { onLoad(publicationId) },
            )
            work != null -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { ConferenceWorkCard(work = work, onClick = {}) }
                state.error?.let { error ->
                    item { InlineError(error) { onLoad(publicationId) } }
                }
                if (work.isOwner) {
                    item {
                        OutlinedButton(
                            onClick = { onOpenRevision(work.projectId) },
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("去创作修订并生成新版本") }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { onAddCollection(publicationId) },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Bookmark, contentDescription = null)
                        Text("收藏")
                    }
                }
                if (!work.isOwner) {
                    item {
                        Text("评语", style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ReviewTemplates) { option ->
                                FilterChip(
                                    selected = template == option,
                                    onClick = { template = option },
                                    label = { Text(ReviewTemplateLabels[option] ?: option) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = reviewContent,
                            onValueChange = { reviewContent = it },
                            label = { Text("写下评语") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                        Button(
                            onClick = {
                                onCreateReview(publicationId, template, reviewContent)
                            },
                            enabled = reviewContent.isNotBlank() && !state.loading,
                        ) { Text("提交评语") }
                    }
                    item {
                        Text("同门改造", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = requestedUse,
                            onValueChange = { requestedUse = it },
                            label = { Text("说明用途") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                        Button(
                            onClick = {
                                onCreateDerivativeRequest(publicationId, requestedUse)
                            },
                            enabled = requestedUse.isNotBlank() && !state.loading,
                        ) { Text("申请授权") }
                    }
                }
                item { Text("同门评语", style = MaterialTheme.typography.titleMedium) }
                if (state.reviews.isEmpty()) {
                    item { EmptyContent("暂无评语") }
                } else {
                    items(state.reviews, key = { it.id }) { review ->
                        Column {
                            ListItem(
                                headlineContent = { Text(review.reviewerNickname) },
                                supportingContent = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(review.content)
                                        review.authorReply?.let { Text("作者回复：$it") }
                                        review.adoptionSummary?.let { Text("改进记录：$it") }
                                    }
                                },
                                trailingContent = { Text(ReviewTemplateLabels[review.template] ?: review.template) },
                            )
                            if (work.isOwner && review.status == "PENDING") {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = { onDecideReview(review, "ACCEPT", null) },
                                        enabled = !state.loading,
                                    ) { Text("采纳") }
                                    OutlinedButton(
                                        onClick = { onDecideReview(review, "THINK", null) },
                                        enabled = !state.loading,
                                    ) { Text("想想") }
                                    Button(onClick = { replyTarget = review }, enabled = !state.loading) { Text("回复") }
                                }
                            }
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (
                                    work.isOwner && review.template == "SUGGESTION" &&
                                    review.status == "ACCEPTED" &&
                                    review.moderationStatus == "VISIBLE" &&
                                    review.adoptedInCreationVersionId == null
                                ) {
                                    Button(enabled = !state.loading, onClick = {
                                        adoptionVersionId = adoptionVersions
                                            .firstOrNull()
                                            ?.id
                                            .orEmpty()
                                        adoptionSummary = ""
                                        adoptionTarget = review
                                    }) { Text("登记改进") }
                                }
                                if (review.moderationStatus == "VISIBLE") {
                                    OutlinedButton(onClick = {
                                        reportDetails = ""
                                        reportTarget = review
                                    }) { Text("举报") }
                                }
                            }
                        }
                    }
                }
            }
            else -> EmptyContent("作品不可见")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceCollectionsScreen(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onRemove: (String) -> Unit,
    onOpenWork: (String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onLoad) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading && state.collections.isEmpty()) {
            LoadingContent(padding)
        } else if (state.error != null && state.collections.isEmpty()) {
            ErrorContent(padding, state.error, onLoad)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.collections, key = { it.publicationId }) { collection ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ConferenceWorkCard(
                            work = collection.work,
                            onClick = { onOpenWork(collection.publicationId) },
                        )
                        OutlinedButton(
                            onClick = { onRemove(collection.publicationId) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("取消收藏") }
                    }
                }
                if (state.collections.isEmpty()) item { EmptyContent("暂无收藏") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceRequestsScreen(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onLoad: (String) -> Unit,
    onDecision: (String, Boolean, String?) -> Unit,
    onRevoke: (String) -> Unit,
    onOpenWork: (String) -> Unit,
    onCreateDerivative: (String, String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad(state.derivativeScope) }
    var rejectTarget by remember { mutableStateOf<ConferenceDerivativeRequestDto?>(null) }
    var rejectionNote by rememberSaveable { mutableStateOf("") }
    rejectTarget?.let { request ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("拒绝申请") },
            text = {
                OutlinedTextField(
                    value = rejectionNote,
                    onValueChange = { rejectionNote = it },
                    label = { Text("说明原因") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDecision(request.id, false, rejectionNote)
                        rejectTarget = null
                    },
                    enabled = rejectionNote.isNotBlank(),
                ) { Text("确认") }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectTarget = null }) { Text("取消") }
            },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("授权记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.derivativeScope == "REQUESTED",
                        onClick = { onLoad("REQUESTED") },
                        label = { Text("我的申请") },
                    )
                    FilterChip(
                        selected = state.derivativeScope == "RECEIVED",
                        onClick = { onLoad("RECEIVED") },
                        label = { Text("待我处理") },
                    )
                }
            }
            if (state.loading && state.derivativeRequests.isEmpty()) item { LoadingRow() }
            state.error?.let { error -> item { InlineError(error) { onLoad(state.derivativeScope) } } }
            items(state.derivativeRequests, key = { it.id }) { request ->
                DerivativeRequestCard(
                    request = request,
                    isReceiver = state.derivativeScope == "RECEIVED",
                    onOpenWork = { onOpenWork(request.sourcePublicationId) },
                    onApprove = { onDecision(request.id, true, null) },
                    onReject = { rejectTarget = request },
                    onRevoke = onRevoke,
                    onCreateDerivative = onCreateDerivative,
                )
            }
            if (!state.loading && state.derivativeRequests.isEmpty()) {
                item { EmptyContent("暂无授权记录") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceMatchScreen(
    state: ConferenceUiState,
    directMatchId: String? = null,
    onBack: () -> Unit,
    onLoad: (String?) -> Unit,
    onJoin: (String) -> Unit,
    onExit: () -> Unit,
    onReport: (String, String) -> Unit,
    onSubmitAnswer: (String, String, String, String) -> Unit,
    onCreateEvaluation: (String, String, Double, String, String, String) -> Unit,
    onCreateReflection: (String, String, String) -> Unit,
    onStartNewMatch: () -> Unit,
    onMessageShown: () -> Unit,
) {
    LaunchedEffect(directMatchId) { onLoad(directMatchId) }
    val shouldPoll = state.matchQueue?.status in setOf("WAITING", "MATCHED") ||
        state.matchDetail?.status == "AWAITING_JUDGMENT"
    LaunchedEffect(shouldPoll, directMatchId) {
        while (shouldPoll) {
            delay(3_000)
            onLoad(directMatchId)
        }
    }
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHost.showSnackbar(it)
            onMessageShown()
        }
    }
    var reportReason by rememberSaveable { mutableStateOf("SAFETY_CONCERN") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("匿名切磋") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onLoad(directMatchId) }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        val queue = state.matchQueue
        val detail = state.matchDetail
        val result = state.matchResult
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.loading && queue == null && detail == null) item { LoadingRow() }
            state.error?.let { error ->
                item { InlineError(error) { onLoad(directMatchId) } }
            }
            if (detail != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = MatchStatusLabels[detail.status] ?: detail.status,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "我方 ${detail.myProgress.answered}/${detail.myProgress.total} · " +
                                    "对方 ${detail.opponentProgress.answered}/${detail.opponentProgress.total}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = {
                                    detail.myProgress.answered.toFloat() /
                                        detail.myProgress.total.coerceAtLeast(1)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (detail.status != "ENDED") {
                                OutlinedButton(onClick = onExit) { Text("退出切磋") }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(MatchReasons) { reason ->
                                        FilterChip(
                                            selected = reportReason == reason,
                                            onClick = { reportReason = reason },
                                            label = { Text(MatchReasonLabels[reason] ?: reason) },
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onReport(detail.matchId, reportReason) },
                                ) {
                                    Text("举报并结束")
                                }
                            }
                        }
                    }
                }
                if (detail.status == "ACTIVE") {
                    items(detail.questions, key = ConferenceMatchQuestionDto::id) { question ->
                        MatchQuestionCard(
                            question = question,
                            existingAnswer = detail.myAnswers.firstOrNull {
                                it.questionId == question.id
                            }?.answer,
                            enabled = !state.loading,
                            onSubmit = { answer, reason ->
                                onSubmitAnswer(detail.matchId, question.id, answer, reason)
                            },
                        )
                    }
                } else if (detail.status == "AWAITING_JUDGMENT") {
                    item { EmptyContent("双方回答已完成，正在生成匿名评审结果…") }
                }
                if (result != null) {
                    item {
                        MatchResultCard(
                            result = result,
                            busy = state.loading,
                            onCreateEvaluation = { kind, score, summary, strength, improvement ->
                                onCreateEvaluation(
                                    result.matchId,
                                    kind,
                                    score,
                                    summary,
                                    strength,
                                    improvement,
                                )
                            },
                            onCreateReflection = { learned, improvement ->
                                onCreateReflection(result.matchId, learned, improvement)
                            },
                            onStartNewMatch = onStartNewMatch,
                        )
                    }
                }
            } else if (queue != null && queue.status == "WAITING") {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("正在等待同门", style = MaterialTheme.typography.titleMedium)
                            Text("匹配成功后会自动进入答题。", style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = onExit) { Text("退出排队") }
                        }
                    }
                }
            } else {
                item { Text("选择秘籍", style = MaterialTheme.typography.titleMedium) }
                items(state.matchManuals, key = ManualPageDto::id) { manual ->
                    ManualMatchRow(manual = manual, onClick = { onJoin(manual.id) })
                }
                if (!state.loading && state.matchManuals.isEmpty()) {
                    item { EmptyContent("暂无可选秘籍") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceLettersScreen(
    state: ConferenceUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onOpenLetter: (ConferenceLetterDto) -> Unit,
    onOpenPublicationInbox: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("大会书信（${state.unreadLetterCount} 未读）") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onLoad) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = onOpenPublicationInbox,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("查看作品与班级来信") }
            }
            if (state.loading && state.letters.isEmpty()) item { LoadingRow() }
            state.error?.let { error -> item { InlineError(error, onLoad) } }
            items(state.letters, key = ConferenceLetterDto::id) { letter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLetter(letter) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (letter.isRead) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(letter.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                ConferenceLetterCategoryLabels[letter.category] ?: letter.category,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Text(letter.body, style = MaterialTheme.typography.bodyMedium)
                        if (!letter.isRead) {
                            Text(
                                "未读",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            if (!state.loading && state.letters.isEmpty()) {
                item { EmptyContent("暂无大会书信") }
            }
        }
    }
}

@Composable
private fun ConferenceWorkCard(work: ConferenceWorkDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            work.previewUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(work.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    work.authorNickname,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                work.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                work.learningSummary?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text("第 ${work.versionNumber} 版", style = MaterialTheme.typography.labelMedium)
                if (work.relatedManuals.isNotEmpty()) {
                    Text(
                        "关联秘籍：${work.relatedManuals.joinToString { "${it.title}·${it.pageNo}" }}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                work.learningCard?.let { card ->
                    Text("创作方法：${card.methodSummary}", style = MaterialTheme.typography.bodySmall)
                }
                work.provenance?.let { provenance ->
                    Text(
                        if (provenance.aiAssistanceUsed) {
                            "创作说明：使用 AI 辅助，${provenance.humanContributionSummary}"
                        } else {
                            "创作说明：${provenance.humanContributionSummary}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun DerivativeRequestCard(
    request: ConferenceDerivativeRequestDto,
    isReceiver: Boolean,
    onOpenWork: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRevoke: (String) -> Unit,
    onCreateDerivative: (String, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(request.sourceTitle, style = MaterialTheme.typography.titleMedium)
            Text(
                request.sourceAuthorNickname,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(request.requestedUse, style = MaterialTheme.typography.bodyMedium)
            Text(request.status, style = MaterialTheme.typography.labelMedium)
            request.authorDecisionNote?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            request.authorization?.let { authorization ->
                Text("授权：${authorization.status}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenWork) { Text("查看作品") }
                if (isReceiver && request.status == "PENDING") {
                    Button(onClick = onApprove) { Text("同意") }
                    OutlinedButton(onClick = onReject) { Text("拒绝") }
                }
            }
            request.authorization?.takeIf { isReceiver && it.status == "ACTIVE" }?.let { authorization ->
                OutlinedButton(onClick = { onRevoke(authorization.id) }) { Text("撤回授权") }
            }
            request.authorization?.takeIf { !isReceiver && it.status == "ACTIVE" }?.let { authorization ->
                Button(onClick = { onCreateDerivative(authorization.id, request.sourceTitle) }) {
                    Text("开始改造")
                }
            }
        }
    }
}

@Composable
private fun MatchQuestionCard(
    question: ConferenceMatchQuestionDto,
    existingAnswer: String?,
    enabled: Boolean,
    onSubmit: (String, String) -> Unit,
) {
    var answer by rememberSaveable(question.id) { mutableStateOf("") }
    var reason by rememberSaveable(question.id) { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "第 ${question.position} 题 · ${MatchQuestionKindLabels[question.kind] ?: question.kind}",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(question.prompt, style = MaterialTheme.typography.titleMedium)
            if (existingAnswer != null) {
                Text("已答：$existingAnswer", style = MaterialTheme.typography.bodyMedium)
            } else {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it.take(2_000) },
                    label = { Text("你的回答") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it.take(1_000) },
                    label = { Text("理由与依据") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Button(
                    onClick = { onSubmit(answer, reason) },
                    enabled = enabled && answer.isNotBlank() && reason.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("提交本题") }
            }
        }
    }
}

@Composable
private fun MatchResultCard(
    result: ConferenceMatchResultDto,
    busy: Boolean,
    onCreateEvaluation: (String, Double, String, String, String) -> Unit,
    onCreateReflection: (String, String) -> Unit,
    onStartNewMatch: () -> Unit,
) {
    val selfParticipant = result.participants.firstOrNull { it.perspective == "SELF" }
    val opponentParticipant = result.participants.firstOrNull { it.perspective == "OPPONENT" }
    val selfSubmitted = selfParticipant?.evaluations?.any { it.kind == "SELF" } == true
    val peerSubmitted = opponentParticipant?.evaluations?.any { it.kind == "PEER" } == true
    var evaluationKind by rememberSaveable(result.matchId) {
        mutableStateOf(if (selfSubmitted) "PEER" else "SELF")
    }
    var score by rememberSaveable(result.matchId) { mutableStateOf("80") }
    var summary by rememberSaveable(result.matchId) { mutableStateOf("") }
    var strength by rememberSaveable(result.matchId) { mutableStateOf("") }
    var improvement by rememberSaveable(result.matchId) { mutableStateOf("") }
    var learned by rememberSaveable(result.matchId) { mutableStateOf("") }
    var nextImprovement by rememberSaveable(result.matchId) { mutableStateOf("") }
    val selectedAlreadySubmitted = if (evaluationKind == "SELF") selfSubmitted else peerSubmitted
    LaunchedEffect(selfSubmitted, peerSubmitted) {
        if (evaluationKind == "SELF" && selfSubmitted && !peerSubmitted) evaluationKind = "PEER"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("切磋结果", style = MaterialTheme.typography.titleLarge)
            Text(
                MatchOutcomeLabels[result.outcome] ?: result.outcome,
                style = MaterialTheme.typography.headlineSmall,
            )
            result.participants.forEach { participant ->
                val label = if (participant.perspective == "SELF") "我的表现" else "同门表现"
                Text(
                    "$label：${participant.score?.let { "${it.toInt()} 分" } ?: "暂无评分"}",
                    style = MaterialTheme.typography.titleMedium,
                )
                participant.evaluations.forEach { evaluation ->
                    Text(
                        "${MatchEvaluationKindLabels[evaluation.kind] ?: evaluation.kind}：${evaluation.summary}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (!selfSubmitted || !peerSubmitted) {
                Text("补充评价", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!selfSubmitted) {
                        FilterChip(
                            selected = evaluationKind == "SELF",
                            onClick = { evaluationKind = "SELF" },
                            label = { Text("自评") },
                        )
                    }
                    if (!peerSubmitted) {
                        FilterChip(
                            selected = evaluationKind == "PEER",
                            onClick = { evaluationKind = "PEER" },
                            label = { Text("互评") },
                        )
                    }
                }
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it.filter(Char::isDigit).take(3) },
                    label = { Text("分数（0—100）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it.take(1_000) },
                    label = { Text("评价小结") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = strength,
                    onValueChange = { strength = it.take(300) },
                    label = { Text("做得好的地方（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = improvement,
                    onValueChange = { improvement = it.take(300) },
                    label = { Text("可以改进的地方（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                )
                val numericScore = score.toDoubleOrNull()
                Button(
                    onClick = {
                        onCreateEvaluation(
                            evaluationKind,
                            numericScore ?: 0.0,
                            summary,
                            strength,
                            improvement,
                        )
                    },
                    enabled = !busy && !selectedAlreadySubmitted && summary.isNotBlank() &&
                        numericScore != null && numericScore in 0.0..100.0,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("提交${if (evaluationKind == "SELF") "自评" else "互评"}") }
            }

            if (result.myReflection == null) {
                Text("本局复盘", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = learned,
                    onValueChange = { learned = it.take(1_000) },
                    label = { Text("这次学到了什么") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = nextImprovement,
                    onValueChange = { nextImprovement = it.take(1_000) },
                    label = { Text("下一次准备怎样改进") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Button(
                    onClick = { onCreateReflection(learned, nextImprovement) },
                    enabled = !busy && learned.isNotBlank() && nextImprovement.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("保存复盘") }
            } else {
                Text("我的复盘：${result.myReflection.learned}")
                Text("下一步：${result.myReflection.nextImprovement}")
            }
            OutlinedButton(
                onClick = onStartNewMatch,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("开始新一局") }
        }
    }
}

@Composable
private fun ManualMatchRow(manual: ManualPageDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    ) {
        ListItem(
            headlineContent = { Text(manual.title) },
            supportingContent = { Text("第 ${manual.pageNo} 页 · ${manual.progressLabel}") },
            trailingContent = { Icon(Icons.Outlined.Groups, contentDescription = "加入切磋") },
        )
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }
}

@Composable
private fun ErrorContent(padding: PaddingValues, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message)
        OutlinedButton(onClick = onRetry) { Text("重试") }
    }
}

@Composable
private fun InlineError(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(message, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
private fun EmptyContent(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val ReviewTemplates = listOf("OBSERVATION", "LEARNING", "EVIDENCE", "SUGGESTION", "QUESTION")
private val ReviewTemplateLabels = mapOf(
    "OBSERVATION" to "观察",
    "LEARNING" to "收获",
    "EVIDENCE" to "依据",
    "SUGGESTION" to "建议",
    "QUESTION" to "提问",
)
private val ReviewReportReasons = listOf(
    "PERSONAL_INFO",
    "HARASSMENT",
    "INAPPROPRIATE_CONTENT",
    "OTHER",
)
private val ReviewReportReasonLabels = mapOf(
    "PERSONAL_INFO" to "个人信息",
    "HARASSMENT" to "骚扰",
    "INAPPROPRIATE_CONTENT" to "内容不当",
    "OTHER" to "其他",
)
private val MatchReasons = listOf(
    "INAPPROPRIATE_CONTENT",
    "HARASSMENT",
    "SAFETY_CONCERN",
    "OTHER",
)
private val MatchReasonLabels = mapOf(
    "INAPPROPRIATE_CONTENT" to "内容不当",
    "HARASSMENT" to "骚扰",
    "SAFETY_CONCERN" to "安全",
    "OTHER" to "其他",
)
private val MatchStatusLabels = mapOf(
    "ACTIVE" to "切磋进行中",
    "AWAITING_JUDGMENT" to "等待评审",
    "ENDED" to "切磋已结束",
)
private val MatchQuestionKindLabels = mapOf(
    "CORE_LOGIC" to "核心理解",
    "CASE_ANALYSIS" to "案例分析",
    "TRANSFER" to "迁移应用",
)
private val MatchOutcomeLabels = mapOf(
    "WIN" to "本局胜出",
    "LOSE" to "本局惜败",
    "TIE" to "本局平局",
    "PENDING" to "等待结果",
    "ENDED_WITHOUT_RESULT" to "本局已结束，不计结果",
)
private val MatchEvaluationKindLabels = mapOf(
    "AI" to "大会评审",
    "SELF" to "自评",
    "PEER" to "同门互评",
    "TEACHER" to "师评",
)
private val ConferenceLetterCategoryLabels = mapOf(
    "REVIEW" to "评语",
    "DERIVATIVE" to "授权",
    "MATCH" to "切磋",
    "SYSTEM" to "系统",
)
