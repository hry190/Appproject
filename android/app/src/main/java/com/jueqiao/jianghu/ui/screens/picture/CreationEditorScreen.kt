package com.jueqiao.jianghu.ui.screens.picture

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jueqiao.jianghu.luggage.CreationLayerDto
import com.jueqiao.jianghu.luggage.CreationExportJobDto
import com.jueqiao.jianghu.luggage.CreationVersionDiffDto
import com.jueqiao.jianghu.luggage.CreationVersionDto
import com.jueqiao.jianghu.luggage.MediaAssetDto
import com.jueqiao.jianghu.ui.theme.InkBg
import com.jueqiao.jianghu.ui.theme.InkBgElement
import com.jueqiao.jianghu.ui.theme.InkBamboo
import java.util.UUID
import kotlin.math.roundToInt

// Keep the editor on the same paper / bamboo palette as the existing app screens.
private val EditorInk = InkBamboo
private val EditorSurface = InkBg
private val EditorCanvas = InkBgElement

@Composable
fun CreationEditorScreen(
    projectTitle: String?,
    version: CreationVersionDto?,
    assets: Map<String, MediaAssetDto>,
    loading: Boolean,
    saving: Boolean,
    message: String?,
    diff: CreationVersionDiffDto?,
    exportBusy: Boolean,
    exportJob: CreationExportJobDto?,
    exportMessage: String?,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onSave: (CreationVersionDto, List<CreationLayerDto>, String) -> Unit,
    onCompare: (CreationVersionDto) -> Unit,
    onExport: (CreationVersionDto, String, Int) -> Unit,
    onDismissDiff: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var layers by remember { mutableStateOf<List<CreationLayerDto>>(emptyList()) }
    var originalLayers by remember { mutableStateOf<List<CreationLayerDto>>(emptyList()) }
    var selectedLayerId by remember { mutableStateOf<String?>(null) }
    var undoStack by remember { mutableStateOf<List<List<CreationLayerDto>>>(emptyList()) }
    var redoStack by remember { mutableStateOf<List<List<CreationLayerDto>>>(emptyList()) }
    var modificationReason by remember { mutableStateOf("调整图层位置与排版") }
    var exportFormat by remember { mutableStateOf("PNG") }
    var exportScale by remember { mutableStateOf(1) }
    var downloadNotice by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(version?.id) {
        val incoming = version?.layers.orEmpty().sortedBy { it.zIndex }
        layers = incoming
        originalLayers = incoming
        selectedLayerId = incoming.lastOrNull()?.layerId
        undoStack = emptyList()
        redoStack = emptyList()
    }

    val latestLayers by rememberUpdatedState(layers)
    val latestSelectedId by rememberUpdatedState(selectedLayerId)

    fun commit(next: List<CreationLayerDto>) {
        if (next == layers) return
        undoStack = (undoStack + listOf(layers)).takeLast(30)
        redoStack = emptyList()
        layers = next
    }

    fun updateSelected(transform: (CreationLayerDto) -> CreationLayerDto) {
        val selected = selectedLayerId ?: return
        commit(layers.map { if (it.layerId == selected) transform(it) else it })
    }

    fun moveSelected(delta: Int) {
        val selected = selectedLayerId ?: return
        val ordered = layers.sortedBy { it.zIndex }.toMutableList()
        val index = ordered.indexOfFirst { it.layerId == selected }
        val target = (index + delta).coerceIn(0, ordered.lastIndex)
        if (index < 0 || index == target) return
        val moving = ordered.removeAt(index)
        ordered.add(target, moving)
        commit(ordered.mapIndexed { z, layer -> layer.copy(zIndex = z) })
    }

    val selectedLayer = layers.firstOrNull { it.layerId == selectedLayerId }
    val selectedWasChanged = selectedLayer?.let { selected ->
        originalLayers.firstOrNull { it.layerId == selected.layerId } != selected
    } == true
    val dirty = layers != originalLayers
    val hasInvalidText = layers.any { it.kind == "TEXT" && it.textContent.isNullOrBlank() }

    Scaffold(
        containerColor = InkBg,
        topBar = {
            Surface(color = EditorInk.copy(alpha = 0.96f), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = projectTitle ?: "作品编辑器",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = version?.let { "当前 V${it.versionNumber} · 保存将生成 V${it.versionNumber + 1}" }
                                ?: "载入画布中",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconButton(onClick = onReload, enabled = !loading && !saving) {
                        Icon(Icons.Default.Refresh, "重新载入", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = EditorSurface) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    OutlinedTextField(
                        value = modificationReason,
                        onValueChange = { modificationReason = it.take(500) },
                        label = { Text("本次修改说明") },
                        supportingText = { Text("旧版本不会被覆盖") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { version?.let { onSave(it, layers, modificationReason) } },
                        enabled = version != null && dirty && !saving && layers.isNotEmpty() &&
                            !hasInvalidText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                hasInvalidText -> "请填写空白文字图层"
                                dirty -> "保存为新版本"
                                else -> "请先调整画布"
                            }
                        )
                    }
                }
            }
        },
    ) { padding ->
        when {
            loading && version == null -> LoadingEditor(Modifier.padding(padding))
            version == null -> EmptyEditor(message, onReload, Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!message.isNullOrBlank()) {
                    item {
                        Surface(
                            color = EditorSurface.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(12.dp),
                                color = EditorInk,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                item {
                    EditorToolbar(
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                        canCompare = version.parentVersionId != null,
                        onUndo = {
                            undoStack.lastOrNull()?.let { previous ->
                                redoStack = (redoStack + listOf(layers)).takeLast(30)
                                layers = previous
                                undoStack = undoStack.dropLast(1)
                            }
                        },
                        onRedo = {
                            redoStack.lastOrNull()?.let { next ->
                                undoStack = (undoStack + listOf(layers)).takeLast(30)
                                layers = next
                                redoStack = redoStack.dropLast(1)
                            }
                        },
                        onAddText = {
                            val id = "text:${UUID.randomUUID()}"
                            commit(
                                layers + CreationLayerDto(
                                    layerId = id,
                                    kind = "TEXT",
                                    name = "文字 ${layers.count { it.kind == "TEXT" } + 1}",
                                    zIndex = layers.size,
                                    visible = true,
                                    assetId = null,
                                    textContent = "新文字",
                                    aigc = false,
                                    fontSize = 64,
                                    textColor = "#294B35",
                                )
                            )
                            selectedLayerId = id
                        },
                        onCompare = { onCompare(version) },
                    )
                }
                item {
                    EditorCanvas(
                        version = version,
                        layers = layers,
                        assets = assets,
                        selectedLayerId = selectedLayerId,
                        onGestureStart = {
                            undoStack = (undoStack + listOf(latestLayers)).takeLast(30)
                            redoStack = emptyList()
                        },
                        onTransform = { panX, panY, zoom, rotation ->
                            val selected = latestSelectedId ?: return@EditorCanvas
                            layers = latestLayers.map { layer ->
                                if (layer.layerId != selected) layer else layer.copy(
                                    offsetX = (layer.offsetX + panX).coerceIn(-10000f, 10000f),
                                    offsetY = (layer.offsetY + panY).coerceIn(-10000f, 10000f),
                                    scale = (layer.scale * zoom).coerceIn(0.1f, 10f),
                                    rotationDegrees = (
                                        layer.rotationDegrees + rotation
                                    ).coerceIn(-3600f, 3600f),
                                )
                            }
                        },
                    )
                }
                item {
                    ExportPanel(
                        version = version,
                        dirty = dirty,
                        busy = exportBusy,
                        job = exportJob?.takeIf { it.creationVersionId == version.id },
                        message = downloadNotice ?: exportMessage,
                        format = exportFormat,
                        outputScale = exportScale,
                        onFormatChange = { exportFormat = it },
                        onScaleChange = { exportScale = it },
                        onGenerate = {
                            downloadNotice = null
                            onExport(version, exportFormat, exportScale)
                        },
                        onDownload = { job ->
                            downloadNotice = enqueueExportDownload(context, job)
                        },
                    )
                }
                item {
                    LayerStrip(
                        layers = layers,
                        selectedLayerId = selectedLayerId,
                        onSelect = { selectedLayerId = it },
                    )
                }
                selectedLayer?.let { layer ->
                    item {
                        LayerInspector(
                            layer = layer,
                            aiModified = layer.aigc && selectedWasChanged,
                            canDelete = layers.size > 1,
                            onUpdate = ::updateSelected,
                            onMoveDown = { moveSelected(-1) },
                            onMoveUp = { moveSelected(1) },
                            onDelete = {
                                if (layers.size > 1) {
                                    commit(
                                        layers.filterNot { it.layerId == layer.layerId }
                                            .sortedBy { it.zIndex }
                                            .mapIndexed { index, item -> item.copy(zIndex = index) }
                                    )
                                    selectedLayerId = layers
                                        .filterNot { it.layerId == layer.layerId }
                                        .lastOrNull()?.layerId
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    diff?.let { VersionDiffDialog(it, onDismissDiff) }
}

@Composable
private fun ExportPanel(
    version: CreationVersionDto,
    dirty: Boolean,
    busy: Boolean,
    job: CreationExportJobDto?,
    message: String?,
    format: String,
    outputScale: Int,
    onFormatChange: (String) -> Unit,
    onScaleChange: (Int) -> Unit,
    onGenerate: () -> Unit,
    onDownload: (CreationExportJobDto) -> Unit,
) {
    Surface(color = EditorSurface, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = EditorInk)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("预览与导出", color = EditorInk, fontWeight = FontWeight.Bold)
                    Text(
                        "由服务器合成 V${version.versionNumber} 的全部可见图层",
                        color = EditorInk.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PNG", "JPEG").forEach { option ->
                    FilterChip(
                        selected = format == option,
                        onClick = { onFormatChange(option) },
                        label = { Text(if (option == "PNG") "PNG · 保留透明" else "JPEG · 米白底") },
                        enabled = !busy,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "标准 1×", 2 to "高清 2×").forEach { (scale, label) ->
                    FilterChip(
                        selected = outputScale == scale,
                        onClick = { onScaleChange(scale) },
                        label = { Text(label) },
                        enabled = !busy,
                    )
                }
            }
            if (busy) {
                LinearProgressIndicator(
                    progress = { (job?.progressPercent ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!message.isNullOrBlank()) {
                Text(message, color = EditorInk.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
            }
            if (dirty) {
                Text(
                    "画布有未保存修改。请先保存为新版本，再生成导出文件。",
                    color = Color(0xFF9A5A10),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(
                onClick = onGenerate,
                enabled = !dirty && !busy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (busy) "正在生成导出文件" else "生成 ${outputScale}× $format 文件")
            }
            val output = job?.outputAsset
            if (job?.status == "COMPLETED" && output?.originalUrl != null) {
                AsyncImage(
                    model = output.originalUrl,
                    contentDescription = "V${job.versionNumber} 导出预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 360.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EditorCanvas),
                    contentScale = ContentScale.Fit,
                )
                OutlinedButton(
                    onClick = { onDownload(job) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("下载到设备")
                }
                Text(
                    "下载地址为限时签名链接；失效后重新进入本页即可刷新。",
                    color = EditorInk.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private fun enqueueExportDownload(context: Context, job: CreationExportJobDto): String {
    val output = job.outputAsset ?: return "导出文件尚未准备好"
    val url = output.originalUrl ?: return "下载地址已失效，请重新进入编辑器"
    return runCatching {
        val extension = if (job.format == "JPEG") "jpg" else "png"
        val filename = "机巧江湖-V${job.versionNumber}-${job.outputScale}x.$extension"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setDescription("作品导出文件")
            .setMimeType(output.actualMime ?: if (job.format == "JPEG") "image/jpeg" else "image/png")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        } else {
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        "已加入系统下载队列：$filename"
    }.getOrElse { "无法加入下载队列，请刷新下载地址后重试" }
}

@Composable
private fun EditorCanvas(
    version: CreationVersionDto,
    layers: List<CreationLayerDto>,
    assets: Map<String, MediaAssetDto>,
    selectedLayerId: String?,
    onGestureStart: () -> Unit,
    onTransform: (panX: Float, panY: Float, zoom: Float, rotation: Float) -> Unit,
) {
    Surface(
        color = EditorSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = EditorInk)
                Spacer(Modifier.width(8.dp))
                Text("画布", fontWeight = FontWeight.SemiBold, color = EditorInk)
                Spacer(Modifier.weight(1f))
                Text(
                    "单指拖动 · 双指缩放/旋转",
                    style = MaterialTheme.typography.labelSmall,
                    color = EditorInk.copy(alpha = 0.72f),
                )
            }
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 520.dp)
                    .aspectRatio(version.canvasWidth.toFloat() / version.canvasHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EditorCanvas)
                    .border(1.dp, EditorInk.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .pointerInput(selectedLayerId, version.canvasWidth, version.canvasHeight) {
                        if (selectedLayerId == null) return@pointerInput
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            onGestureStart()
                            do {
                                val event = awaitPointerEvent()
                                val pan = event.calculatePan()
                                val zoom = event.calculateZoom()
                                val rotation = event.calculateRotation()
                                if (pan.x != 0f || pan.y != 0f || zoom != 1f || rotation != 0f) {
                                    val logicalPanX = pan.x * version.canvasWidth / size.width
                                    val logicalPanY = pan.y * version.canvasHeight / size.height
                                    onTransform(logicalPanX, logicalPanY, zoom, rotation)
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            ) {
                val canvasWidthDp = maxWidth
                layers.sortedBy { it.zIndex }.filter { it.visible }.forEach { layer ->
                    val selected = layer.layerId == selectedLayerId
                    val borderModifier = if (selected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = layer.offsetX * size.width / version.canvasWidth
                                translationY = layer.offsetY * size.height / version.canvasHeight
                                scaleX = layer.scale
                                scaleY = layer.scale
                                rotationZ = layer.rotationDegrees
                                alpha = layer.opacity
                            }
                            .then(borderModifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (layer.kind) {
                            "TEXT" -> {
                                val displaySize = (
                                    (layer.fontSize ?: 64) * canvasWidthDp.value / version.canvasWidth
                                ).coerceIn(10f, 96f)
                                Text(
                                    text = layer.textContent.orEmpty(),
                                    color = parseEditorColor(layer.textColor),
                                    fontSize = displaySize.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            else -> {
                                val url = layer.assetId?.let { assets[it]?.originalUrl }
                                AsyncImage(
                                    model = url,
                                    contentDescription = layer.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val cropScale = 1f / (1f - 2f * layer.cropInset)
                                            scaleX = cropScale
                                            scaleY = cropScale
                                        },
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    canCompare: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAddText: () -> Unit,
    onCompare: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onUndo, enabled = canUndo, contentPadding = PaddingValues(12.dp)) {
            Icon(Icons.AutoMirrored.Filled.Undo, "撤销")
            Spacer(Modifier.width(5.dp))
            Text("撤销")
        }
        OutlinedButton(onClick = onRedo, enabled = canRedo, contentPadding = PaddingValues(12.dp)) {
            Icon(Icons.AutoMirrored.Filled.Redo, "重做")
            Spacer(Modifier.width(5.dp))
            Text("重做")
        }
        FilledTonalButton(onClick = onAddText) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(5.dp))
            Text("加文字")
        }
        OutlinedButton(onClick = onCompare, enabled = canCompare) {
            Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null)
            Spacer(Modifier.width(5.dp))
            Text("与上一版比较")
        }
    }
}

@Composable
private fun LayerStrip(
    layers: List<CreationLayerDto>,
    selectedLayerId: String?,
    onSelect: (String) -> Unit,
) {
    Surface(color = EditorSurface, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("图层（从下到上）", color = EditorInk, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                layers.sortedBy { it.zIndex }.forEach { layer ->
                    AssistChip(
                        onClick = { onSelect(layer.layerId) },
                        label = {
                            Text(
                                buildString {
                                    if (!layer.visible) append("隐藏 · ")
                                    if (layer.aigc) append("AI · ")
                                    append(layer.name)
                                }
                            )
                        },
                        leadingIcon = {
                            if (layer.layerId == selectedLayerId) {
                                Icon(Icons.Default.Layers, null, modifier = Modifier.size(18.dp))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerInspector(
    layer: CreationLayerDto,
    aiModified: Boolean,
    canDelete: Boolean,
    onUpdate: ((CreationLayerDto) -> CreationLayerDto) -> Unit,
    onMoveDown: () -> Unit,
    onMoveUp: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(color = EditorSurface, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(layer.name, color = EditorInk, fontWeight = FontWeight.Bold)
                    Text(
                        if (layer.aigc) {
                            if (aiModified) "AI 生成 · 已人工调整（保存时自动写入来源谱）"
                            else "AI 生成 · 尚未人工调整"
                        } else {
                            "${layer.kind} · 第 ${layer.zIndex + 1} 层"
                        },
                        color = if (aiModified) Color(0xFF9A5A10) else EditorInk.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                IconButton(onClick = { onUpdate { it.copy(visible = !it.visible) } }) {
                    Icon(
                        if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        if (layer.visible) "隐藏图层" else "显示图层",
                        tint = EditorInk,
                    )
                }
                IconButton(onClick = onMoveDown) {
                    Icon(Icons.Default.ArrowDownward, "下移一层", tint = EditorInk)
                }
                IconButton(onClick = onMoveUp) {
                    Icon(Icons.Default.ArrowUpward, "上移一层", tint = EditorInk)
                }
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(Icons.Default.Delete, "删除图层", tint = Color(0xFF9C3D35))
                }
            }

            if (layer.kind == "TEXT") {
                OutlinedTextField(
                    value = layer.textContent.orEmpty(),
                    onValueChange = { value ->
                        onUpdate { it.copy(textContent = value.take(5000)) }
                    },
                    label = { Text("文字内容") },
                    isError = layer.textContent.isNullOrBlank(),
                    supportingText = {
                        if (layer.textContent.isNullOrBlank()) Text("文字不能为空")
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                ValueStepper(
                    label = "字号",
                    value = "${layer.fontSize ?: 64}",
                    onDecrease = { onUpdate { it.copy(fontSize = ((it.fontSize ?: 64) - 4).coerceAtLeast(8)) } },
                    onIncrease = { onUpdate { it.copy(fontSize = ((it.fontSize ?: 64) + 4).coerceAtMost(512)) } },
                )
            }

            Text("透明度 ${(layer.opacity * 100).roundToInt()}%", color = EditorInk)
            Slider(
                value = layer.opacity,
                onValueChange = { value -> onUpdate { it.copy(opacity = value) } },
                valueRange = 0f..1f,
            )
            ValueStepper(
                label = "缩放",
                value = "${(layer.scale * 100).roundToInt()}%",
                onDecrease = { onUpdate { it.copy(scale = (it.scale - 0.1f).coerceAtLeast(0.1f)) } },
                onIncrease = { onUpdate { it.copy(scale = (it.scale + 0.1f).coerceAtMost(10f)) } },
            )
            ValueStepper(
                label = "旋转",
                value = "${layer.rotationDegrees.roundToInt()}°",
                onDecrease = { onUpdate { it.copy(rotationDegrees = it.rotationDegrees - 15f) } },
                onIncrease = { onUpdate { it.copy(rotationDegrees = it.rotationDegrees + 15f) } },
            )
            if (layer.kind != "TEXT") {
                Text("中心裁剪 ${(layer.cropInset * 200).roundToInt()}%", color = EditorInk)
                Slider(
                    value = layer.cropInset,
                    onValueChange = { value -> onUpdate { it.copy(cropInset = value) } },
                    valueRange = 0f..0.45f,
                )
            }
            OutlinedButton(
                onClick = {
                    onUpdate {
                        it.copy(
                            offsetX = 0f,
                            offsetY = 0f,
                            scale = 1f,
                            rotationDegrees = 0f,
                            opacity = 1f,
                            cropInset = 0f,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("重置所选图层变换")
            }
        }
    }
}

@Composable
private fun ValueStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = EditorInk, modifier = Modifier.width(64.dp))
        OutlinedButton(onClick = onDecrease, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) {
            Text("−", fontSize = 22.sp)
        }
        Text(value, color = EditorInk, modifier = Modifier.width(86.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        OutlinedButton(onClick = onIncrease, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) {
            Text("+", fontSize = 22.sp)
        }
    }
}

@Composable
private fun VersionDiffDialog(diff: CreationVersionDiffDto, onDismiss: () -> Unit) {
    val total = diff.addedLayers.size + diff.removedLayers.size + diff.modifiedLayers.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("V${diff.baseVersionNumber} → V${diff.targetVersionNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("共 $total 个图层发生变化${if (diff.canvasChanged) "，画布尺寸也已变化" else ""}。")
                HorizontalDivider()
                diff.addedLayers.forEach { Text("新增：${it.name}") }
                diff.removedLayers.forEach { Text("删除：${it.name}") }
                diff.modifiedLayers.forEach {
                    Text("修改：${it.name}（${it.changedFields.joinToString("、") { field -> fieldLabel(field) }}）")
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("知道了") } },
    )
}

@Composable
private fun LoadingEditor(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = EditorInk)
            Spacer(Modifier.height(12.dp))
            Text("正在载入图层与安全素材…", color = EditorInk)
        }
    }
}

@Composable
private fun EmptyEditor(message: String?, onReload: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message ?: "暂时没有可编辑版本", color = EditorInk)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onReload) { Text("重新载入") }
        }
    }
}

private fun parseEditorColor(value: String?): Color = runCatching {
    Color(android.graphics.Color.parseColor(value ?: "#294B35"))
}.getOrDefault(EditorInk)

private fun fieldLabel(field: String): String = when (field) {
    "offset_x", "offset_y" -> "位置"
    "scale" -> "缩放"
    "rotation_degrees" -> "旋转"
    "opacity" -> "透明度"
    "crop_inset" -> "裁剪"
    "text_content" -> "文字"
    "font_size" -> "字号"
    "z_index" -> "层级"
    "visible" -> "显示状态"
    else -> field
}
