package com.jueqiao.jianghu.ui.screens.yanwuchangvideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import coil.compose.AsyncImage
import com.jueqiao.jianghu.conference.ConferenceUiState
import com.jueqiao.jianghu.luggage.ConferenceReviewDto
import com.jueqiao.jianghu.luggage.ConferenceWorkDto
import com.jueqiao.jianghu.ui.theme.YaHei
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val WorkJade = Color(0xFF315F49)
private val WorkJadeDeep = Color(0xFF173A2E)
private val WorkJadeSoft = Color(0xFF91B69A)
private val WorkParchment = Color(0xFFF3E7C9)
private val WorkGold = Color(0xFFC6A45B)
private val WorkPageBackground = Color(0xFFF2EBD8)

private data class WorkCategory(val label: String, val apiValue: String?)
private data class MediaSeekRequest(val requestId: Long, val positionMs: Int)

private val WorkCategories = listOf(
    WorkCategory("艺术", "ART"),
    WorkCategory("科学", "SCIENCE"),
    WorkCategory("数学", "MATH"),
    WorkCategory("语文", "LANGUAGE"),
    WorkCategory("推荐", null),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YanwuchangVideoScreen(
    state: ConferenceUiState,
    onBack: () -> Unit = {},
    onLoadFeed: (String?) -> Unit = {},
    onLoadWork: (String) -> Unit = {},
    onAddLike: (String) -> Unit = {},
    onRemoveLike: (String) -> Unit = {},
    onCreateReview: (String, String, String) -> Unit = { _, _, _ -> },
    onAddCollection: (String) -> Unit = {},
    onRemoveCollection: (String) -> Unit = {},
    onCreateCoCreateRequest: (String, String) -> Unit = { _, _ -> },
    onOpenOwnerRequests: () -> Unit = {},
    onOpenMy: () -> Unit = {},
) {
    BackHandler(enabled = true, onBack = onBack)

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var showReviews by rememberSaveable { mutableStateOf(false) }
    var showCoCreate by rememberSaveable { mutableStateOf(false) }
    var reviewTemplate by rememberSaveable { mutableStateOf("OBSERVATION") }
    var reviewDraft by rememberSaveable { mutableStateOf("") }
    var coCreateDraft by rememberSaveable { mutableStateOf("") }

    val selectedCategory = WorkCategories.firstOrNull {
        it.apiValue == state.selectedCategory
    } ?: WorkCategories.last()
    val works = state.works
    val listedWork = works.getOrNull(currentIndex)
    val work = state.work?.takeIf { it.publicationId == listedWork?.publicationId } ?: listedWork
    var playbackPositionMs by remember(work?.publicationId) { mutableIntStateOf(0) }
    var playbackDurationMs by remember(work?.publicationId) {
        mutableIntStateOf(work?.previewDurationMs ?: 0)
    }
    var seekRequest by remember(work?.publicationId) { mutableStateOf<MediaSeekRequest?>(null) }

    LaunchedEffect(works.size) {
        if (currentIndex > works.lastIndex) {
            currentIndex = works.lastIndex.coerceAtLeast(0)
        }
    }

    LaunchedEffect(work?.publicationId) {
        isPlaying = false
    }

    LaunchedEffect(selectedCategory.apiValue) {
        currentIndex = 0
        onLoadFeed(selectedCategory.apiValue)
        while (true) {
            delay(15_000)
            onLoadFeed(selectedCategory.apiValue)
        }
    }

    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(modifier = Modifier.fillMaxSize().background(WorkPageBackground)) {
        Spacer(modifier = Modifier.height(statusBarInset + 8.dp))
        WorksTopBar(
            selectedCategory = selectedCategory,
            onBack = onBack,
            onCategorySelected = { onLoadFeed(it.apiValue) },
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(WorkPageBackground),
        ) {
            if (work != null) {
                FullScreenWorkMedia(
                    work = work,
                    isPlaying = isPlaying,
                    seekRequest = seekRequest,
                    onPlaybackProgress = { positionMs, durationMs ->
                        playbackPositionMs = positionMs.coerceAtLeast(0)
                        playbackDurationMs = durationMs.coerceAtLeast(0)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color(0x10000000),
                                0.52f to Color.Transparent,
                                0.76f to Color(0x26000000),
                                1f to Color(0xD8172B20),
                            ),
                        ),
                    ),
                )
                if (works.isNotEmpty()) {
                    Text(
                        text = "${currentIndex + 1} / ${works.size}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = YaHei,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 20.dp, end = 20.dp)
                            .clickable {
                                currentIndex = if (currentIndex >= works.lastIndex) 0 else currentIndex + 1
                            },
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.74f))
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停作品" else "播放作品",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                WorkActions(
                    work = work,
                    enabled = !state.loading,
                    onLike = {
                        if (work.isLiked) onRemoveLike(work.publicationId)
                        else onAddLike(work.publicationId)
                    },
                    onComment = {
                        onLoadWork(work.publicationId)
                        showReviews = true
                    },
                    onFavorite = {
                        if (work.isCollected) onRemoveCollection(work.publicationId)
                        else onAddCollection(work.publicationId)
                    },
                    onCoCreate = {
                        if (work.isOwner) onOpenOwnerRequests() else showCoCreate = true
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 48.dp, end = 9.dp),
                )
                WorkInformation(
                    work = work,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 78.dp, bottom = 42.dp),
                )
                if (work.previewMimeType?.startsWith("video/") == true) {
                    MediaProgress(
                        positionMs = playbackPositionMs,
                        durationMs = playbackDurationMs,
                        onSeekFraction = { fraction ->
                            if (playbackDurationMs > 0) {
                                val targetMs = (playbackDurationMs * fraction).toInt()
                                playbackPositionMs = targetMs
                                seekRequest = MediaSeekRequest(System.nanoTime(), targetMs)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                    )
                }
            } else if (!state.loading) {
                EmptyFeed(selectedCategory.label, Modifier.align(Alignment.Center))
            }

            if (state.loading && work == null) {
                CircularProgressIndicator(color = WorkJade, modifier = Modifier.align(Alignment.Center))
            }
            state.error?.let { message ->
                Text(
                    text = message,
                    color = Color.White,
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 18.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC6E2D2D))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
        ConferenceWorksNavigationBar(
            selectedWorks = true,
            navigationBarInset = navigationBarInset,
            onOpenWorks = {},
            onOpenMy = onOpenMy,
        )
    }

    if (showReviews && work != null) {
        ModalBottomSheet(
            onDismissRequest = { showReviews = false },
            containerColor = Color(0xFFF7EFD9),
        ) {
            ReviewSheet(
                work = work,
                reviews = state.reviews,
                selectedTemplate = reviewTemplate,
                draft = reviewDraft,
                loading = state.loading,
                onTemplateChange = { reviewTemplate = it },
                onDraftChange = { reviewDraft = it.take(500) },
                onSubmit = {
                    onCreateReview(work.publicationId, reviewTemplate, reviewDraft)
                    reviewDraft = ""
                },
            )
        }
    }

    if (showCoCreate && work != null) {
        ModalBottomSheet(
            onDismissRequest = { showCoCreate = false },
            containerColor = Color(0xFFF7EFD9),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp)) {
                Text("申请共创", fontFamily = YaHei, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = WorkJadeDeep)
                Spacer(Modifier.height(6.dp))
                Text(
                    "说明你希望怎样基于《${work.title}》继续创作，原作者同意后才会建立授权。",
                    fontFamily = YaHei,
                    fontSize = 13.sp,
                    color = Color(0xFF52645A),
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = coCreateDraft,
                    onValueChange = { coCreateDraft = it.take(1000) },
                    label = { Text("共创计划", fontFamily = YaHei) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        onCreateCoCreateRequest(work.publicationId, coCreateDraft)
                        coCreateDraft = ""
                        showCoCreate = false
                    },
                    enabled = coCreateDraft.trim().isNotEmpty() && !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("提交申请", fontFamily = YaHei) }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FullScreenWorkMedia(
    work: ConferenceWorkDto,
    isPlaying: Boolean,
    seekRequest: MediaSeekRequest?,
    onPlaybackProgress: (positionMs: Int, durationMs: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val url = work.previewUrl
    val isVideo = url != null && work.previewMimeType?.startsWith("video/") == true
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isVideo) {
            val context = LocalContext.current
            var localPath by remember(work.publicationId) { mutableStateOf<String?>(null) }
            var downloadFinished by remember(work.publicationId) { mutableStateOf(false) }
            LaunchedEffect(url, work.publicationId) {
                localPath = withContext(Dispatchers.IO) {
                    val target = File(context.cacheDir, "conference-${work.publicationId}.mp4")
                    if (target.isFile && target.length() > 0) return@withContext target.absolutePath
                    val temporary = File(context.cacheDir, "${target.name}.part")
                    runCatching {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.connectTimeout = 10_000
                        connection.readTimeout = 20_000
                        connection.instanceFollowRedirects = true
                        try {
                            require(connection.responseCode in 200..299)
                            connection.inputStream.use { input ->
                                temporary.outputStream().use { output -> input.copyTo(output) }
                            }
                            require(temporary.length() > 0)
                            if (target.exists()) target.delete()
                            require(temporary.renameTo(target))
                            target.absolutePath
                        } finally {
                            connection.disconnect()
                        }
                    }.getOrElse {
                        temporary.delete()
                        null
                    }
                }
                downloadFinished = true
            }
            val playablePath = localPath
            if (playablePath != null) {
                LocalVideoSurface(
                    sourcePath = playablePath,
                    isPlaying = isPlaying,
                    seekRequest = seekRequest,
                    onPlaybackProgress = onPlaybackProgress,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (!downloadFinished) {
                CircularProgressIndicator(
                    color = WorkJade,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp),
                )
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = work.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                placeholder = ColorPainter(WorkPageBackground),
                error = ColorPainter(WorkPageBackground),
                fallback = ColorPainter(WorkPageBackground),
            )
        }
    }
}

@Composable
private fun LocalVideoSurface(
    sourcePath: String,
    isPlaying: Boolean,
    seekRequest: MediaSeekRequest?,
    onPlaybackProgress: (positionMs: Int, durationMs: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val player = remember(sourcePath) { MediaPlayer() }
    var prepared by remember(sourcePath) { mutableStateOf(false) }
    var surface by remember(sourcePath) { mutableStateOf<Surface?>(null) }
    var videoAspectRatio by remember(sourcePath) { mutableFloatStateOf(9f / 16f) }
    val latestOnPlaybackProgress by rememberUpdatedState(onPlaybackProgress)
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val availableAspectRatio = if (maxHeight > 0.dp) maxWidth.value / maxHeight.value else videoAspectRatio
        val fittedModifier = if (videoAspectRatio >= availableAspectRatio) {
            Modifier.fillMaxWidth().aspectRatio(videoAspectRatio)
        } else {
            Modifier.fillMaxHeight().aspectRatio(videoAspectRatio)
        }
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    isOpaque = false
                    alpha = 0f
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                            val videoSurface = Surface(texture)
                            surface = videoSurface
                            player.setSurface(videoSurface)
                            player.isLooping = true
                            player.setOnPreparedListener { readyPlayer ->
                                readyPlayer.setVideoScalingMode(
                                    MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT,
                                )
                                if (readyPlayer.videoWidth > 0 && readyPlayer.videoHeight > 0) {
                                    videoAspectRatio = readyPlayer.videoWidth.toFloat() / readyPlayer.videoHeight.toFloat()
                                }
                                readyPlayer.seekTo(1)
                                prepared = true
                                latestOnPlaybackProgress(readyPlayer.currentPosition, readyPlayer.duration)
                            }
                            player.setOnErrorListener { _, _, _ ->
                                prepared = false
                                true
                            }
                            player.setDataSource(sourcePath)
                            player.prepareAsync()
                        }

                        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = Unit

                        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                            surface?.release()
                            surface = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                    }
                }
            },
            update = { texture -> texture.alpha = if (prepared) 1f else 0f },
            modifier = fittedModifier,
        )
    }
    LaunchedEffect(isPlaying, prepared) {
        if (!prepared) return@LaunchedEffect
        if (isPlaying && !player.isPlaying) player.start()
        if (!isPlaying && player.isPlaying) player.pause()
    }
    LaunchedEffect(seekRequest?.requestId, prepared) {
        val targetMs = seekRequest?.positionMs ?: return@LaunchedEffect
        if (prepared) {
            runCatching { player.seekTo(targetMs) }
        }
    }
    LaunchedEffect(player, prepared) {
        while (prepared) {
            runCatching {
                latestOnPlaybackProgress(player.currentPosition, player.duration)
            }
            delay(200)
        }
    }
    DisposableEffect(player) {
        onDispose {
            player.setOnPreparedListener(null)
            player.setOnErrorListener(null)
            player.release()
            surface?.release()
            surface = null
        }
    }
}

@Composable
private fun WorksTopBar(
    selectedCategory: WorkCategory,
    onBack: () -> Unit,
    onCategorySelected: (WorkCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xDDEAF0DB))
            .border(1.dp, WorkGold.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(44.dp).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = WorkJade,
                modifier = Modifier.size(27.dp),
            )
        }
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            WorkCategories.forEach { category ->
                WorkCategoryTab(
                    text = category.label,
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Search, "搜索", tint = WorkJadeDeep, modifier = Modifier.size(27.dp))
        }
    }
}

@Composable
private fun WorkCategoryTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) WorkJadeSoft.copy(alpha = 0.24f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp)
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WorkGold),
            )
        }
        Text(
            text = text,
            color = WorkJadeDeep,
            fontFamily = YaHei,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (selected) 16.sp else 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun WorkActions(
    work: ConferenceWorkDto,
    enabled: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onFavorite: () -> Unit,
    onCoCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(58.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WorkAction(
            if (work.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            work.likeCount,
            "点赞",
            enabled,
            onLike,
            selected = work.isLiked,
        )
        WorkAction(Icons.Outlined.ChatBubbleOutline, work.reviewCount, "点评", enabled, onComment)
        WorkAction(
            if (work.isCollected) Icons.Filled.Star else Icons.Outlined.StarBorder,
            work.collectionCount,
            "收藏",
            enabled,
            onFavorite,
            selected = work.isCollected,
        )
        WorkAction(
            Icons.Outlined.Groups,
            if (work.coCreateRequestStatus == null) 0 else 1,
            if (work.coCreateRequestStatus == "PENDING") "已申请" else "共创",
            enabled && work.coCreateRequestStatus != "PENDING",
            onCoCreate,
        )
    }
}

@Composable
private fun WorkAction(
    icon: ImageVector,
    count: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = if (selected) 0.32f else 0.2f))
                .border(1.dp, WorkGold.copy(alpha = 0.76f), CircleShape)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, label, tint = if (selected) Color(0xFFFFDA76) else Color.White, modifier = Modifier.size(27.dp)) }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = count.toString(),
            color = Color.White,
            fontFamily = YaHei,
            fontSize = 11.sp,
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.65f),
            fontFamily = YaHei,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun WorkInformation(work: ConferenceWorkDto, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("@${work.authorNickname}", color = Color.White, fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(work.title, color = Color.White, fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        work.description?.takeIf { it.isNotBlank() }?.let { description ->
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.94f),
                fontFamily = YaHei,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            WorkTag(categoryLabel(work.conferenceCategory))
            WorkTag(mediaLabel(work.mediaType))
            if (work.aiAssisted) WorkTag("AI辅助")
        }
        work.provenance?.let { provenance ->
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = provenance.humanContributionSummary,
                color = Color.White.copy(alpha = 0.74f),
                fontFamily = YaHei,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WorkTag(text: String) {
    Text(
        text = text,
        color = Color(0xFFD9F0DD),
        fontFamily = YaHei,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, WorkJadeSoft, RoundedCornerShape(50))
            .background(Color(0x66355D46))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun EmptyFeed(category: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "暂无${if (category == "推荐") "" else category}作品",
            color = WorkJadeDeep,
            fontFamily = YaHei,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text("作品通过审核后会实时出现在这里", color = WorkJade.copy(alpha = 0.72f), fontFamily = YaHei, fontSize = 12.sp)
    }
}

@Composable
private fun ReviewSheet(
    work: ConferenceWorkDto,
    reviews: List<ConferenceReviewDto>,
    selectedTemplate: String,
    draft: String,
    loading: Boolean,
    onTemplateChange: (String) -> Unit,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val templates = listOf(
        "OBSERVATION" to "我发现",
        "LEARNING" to "我学到",
        "EVIDENCE" to "有依据",
        "SUGGESTION" to "我建议",
        "QUESTION" to "我想问",
    )
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
        Text("《${work.title}》的点评", fontFamily = YaHei, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = WorkJadeDeep)
        Spacer(Modifier.height(10.dp))
        if (reviews.isEmpty()) {
            Text("还没有点评", fontFamily = YaHei, fontSize = 13.sp, color = Color(0xFF66736B))
        } else {
            reviews.take(4).forEach { review ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(10.dp),
                ) {
                    Text("${review.reviewerNickname} · ${templateLabel(review.template)}", fontFamily = YaHei, fontSize = 12.sp, color = WorkJade)
                    Text(review.content, fontFamily = YaHei, fontSize = 14.sp, color = Color(0xFF27362E))
                }
            }
        }
        if (!work.isOwner) {
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                templates.forEach { (value, label) ->
                    Text(
                        text = label,
                        fontFamily = YaHei,
                        fontSize = 12.sp,
                        color = if (selectedTemplate == value) Color.White else WorkJade,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selectedTemplate == value) WorkJade else Color.Transparent)
                            .border(1.dp, WorkJadeSoft, RoundedCornerShape(18.dp))
                            .clickable { onTemplateChange(value) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                label = { Text("写下具体、友善的点评", fontFamily = YaHei) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onSubmit,
                enabled = draft.trim().isNotEmpty() && !loading,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("提交点评", fontFamily = YaHei) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MediaProgress(
    positionMs: Int,
    durationMs: Int,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0) {
        positionMs.toFloat().div(durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Box(
        modifier = modifier
            .height(32.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0 && size.width > 0) {
                        onSeekFraction((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.45f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFD56E)),
            )
        }
    }
}

private fun categoryLabel(category: String?): String = when (category) {
    "ART" -> "艺术"
    "SCIENCE" -> "科学"
    "MATH" -> "数学"
    "LANGUAGE" -> "语文"
    else -> "作品"
}

private fun mediaLabel(mediaType: String): String = when (mediaType) {
    "VIDEO" -> "视频"
    "ILLUSTRATION" -> "插画"
    "COMIC" -> "漫画"
    "MIXED_MEDIA" -> "综合创作"
    else -> "创作"
}

private fun templateLabel(template: String): String = when (template) {
    "OBSERVATION" -> "我发现"
    "LEARNING" -> "我学到"
    "EVIDENCE" -> "有依据"
    "SUGGESTION" -> "我建议"
    "QUESTION" -> "我想问"
    else -> "点评"
}
