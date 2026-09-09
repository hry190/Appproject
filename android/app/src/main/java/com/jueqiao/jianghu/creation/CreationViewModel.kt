package com.jueqiao.jianghu.creation

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jueqiao.jianghu.auth.AuthApiException
import com.jueqiao.jianghu.luggage.CreationIntentAnalysisDto
import com.jueqiao.jianghu.luggage.CreationIntentAnalyzeDto
import com.jueqiao.jianghu.luggage.CreationExportCreateDto
import com.jueqiao.jianghu.luggage.CreationExportJobDto
import com.jueqiao.jianghu.luggage.CreationLayerDto
import com.jueqiao.jianghu.luggage.CreationMethodPutDto
import com.jueqiao.jianghu.luggage.CreationMethodDraftDto
import com.jueqiao.jianghu.luggage.CreationProjectCreateDto
import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.luggage.CreationSealCheckPutDto
import com.jueqiao.jianghu.luggage.CreationStageTransitionDto
import com.jueqiao.jianghu.luggage.CreationSubmissionCreateDto
import com.jueqiao.jianghu.luggage.CreationTestFindingCreateDto
import com.jueqiao.jianghu.luggage.CreationTestIssueDto
import com.jueqiao.jianghu.luggage.CreationTestIssueResolveDto
import com.jueqiao.jianghu.luggage.CreationTestRecordCreateDto
import com.jueqiao.jianghu.luggage.CreationToolCallDecisionDto
import com.jueqiao.jianghu.luggage.CreationToolCallDto
import com.jueqiao.jianghu.luggage.CreationToolCallProposeDto
import com.jueqiao.jianghu.luggage.CreationVersionCreateDto
import com.jueqiao.jianghu.luggage.CreationVersionDiffDto
import com.jueqiao.jianghu.luggage.CreationVersionDto
import com.jueqiao.jianghu.luggage.ImageGenerationCreateDto
import com.jueqiao.jianghu.luggage.ImageGenerationJobDto
import com.jueqiao.jianghu.luggage.ImageGenerationRetryDto
import com.jueqiao.jianghu.luggage.LearningCardPutDto
import com.jueqiao.jianghu.luggage.LuggageRepository
import com.jueqiao.jianghu.luggage.MediaAssetDto
import com.jueqiao.jianghu.luggage.MigrationEvidenceCreateDto
import com.jueqiao.jianghu.luggage.ProvenanceItemInputDto
import com.jueqiao.jianghu.luggage.ProvenanceManifestPutDto
import com.jueqiao.jianghu.luggage.PublicationDto
import com.jueqiao.jianghu.ui.screens.gongfang.CreationManualOption
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CreationSketchSource(
    val assetId: String,
    val filename: String,
)

data class CreationDeskState(
    val recentProjects: List<CreationProjectDto> = emptyList(),
    val derivativeAuthorizationId: String? = null,
    val derivativeSourceTitle: String? = null,
    val loadingRecent: Boolean = false,
    val recentError: String? = null,
    val analyzingIntent: Boolean = false,
    val intentAnalysis: CreationIntentAnalysisDto? = null,
    val analysisError: String? = null,
    val manualSources: List<CreationManualOption> = emptyList(),
    val loadingManualSources: Boolean = false,
    val manualSourceError: String? = null,
    val uploadingSketch: Boolean = false,
    val sketchSource: CreationSketchSource? = null,
    val sketchSourceMessage: String? = null,
    val creatingProject: Boolean = false,
    val createError: String? = null,
    val savingDraft: Boolean = false,
    val draftSaveMessage: String? = null,
    val draftMessageProjectId: String? = null,
    val workflowBusy: Boolean = false,
    val workflowMessage: String? = null,
    val workflowMessageProjectId: String? = null,
    val generationBusy: Boolean = false,
    val generationJob: ImageGenerationJobDto? = null,
    val generationMessage: String? = null,
    val generationProjectId: String? = null,
    val editorLoading: Boolean = false,
    val editorSaving: Boolean = false,
    val editorProjectId: String? = null,
    val editorProject: CreationProjectDto? = null,
    val editorVersions: List<CreationVersionDto> = emptyList(),
    val editorAssets: Map<String, MediaAssetDto> = emptyMap(),
    val editorDiff: CreationVersionDiffDto? = null,
    val editorMessage: String? = null,
    val editorExportBusy: Boolean = false,
    val editorExportVersionId: String? = null,
    val editorExportJob: CreationExportJobDto? = null,
    val editorExportMessage: String? = null,
)

class CreationViewModel(
    application: Application,
    private val repository: LuggageRepository,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(CreationDeskState())
    val state: StateFlow<CreationDeskState> = _state.asStateFlow()

    fun beginDerivative(authorizationId: String, sourceTitle: String) {
        pendingCommit = null
        _state.value = _state.value.copy(
            derivativeAuthorizationId = authorizationId,
            derivativeSourceTitle = sourceTitle,
            intentAnalysis = null,
            analysisError = null,
            createError = null,
        )
    }

    fun clearDerivative() {
        pendingCommit = null
        _state.value = _state.value.copy(
            derivativeAuthorizationId = null,
            derivativeSourceTitle = null,
            createError = null,
        )
    }
    private var pendingCommit: PendingCommit? = null
    private var pendingDraftSave: PendingCommit? = null
    private var pendingWorkflowCommit: PendingCommit? = null
    private var pendingGenerationCommit: PendingCommit? = null
    private var pendingEditorCommit: PendingCommit? = null
    private var pendingExportCommit: PendingCommit? = null

    fun loadEditor(projectId: String) {
        if (_state.value.editorLoading && _state.value.editorProjectId == projectId) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                editorLoading = true,
                editorProjectId = projectId,
                editorMessage = null,
                editorDiff = null,
                editorExportBusy = false,
                editorExportVersionId = null,
                editorExportJob = null,
                editorExportMessage = null,
            )
            try {
                val detail = repository.creationDetail(projectId)
                val current = detail.versions.maxByOrNull { it.versionNumber }
                    ?: error("作品还没有可编辑版本")
                val assetIds = current.layers.mapNotNull { it.assetId }.distinct()
                val assets = coroutineScope {
                    assetIds.map { assetId ->
                        async { repository.mediaAsset(assetId) }
                    }.awaitAll().associateBy { it.id }
                }
                val latestExport = runCatching {
                    repository.creationExports(current.id).items.firstOrNull()
                }.getOrNull()
                _state.value = _state.value.copy(
                    editorLoading = false,
                    editorProject = detail.project,
                    editorVersions = detail.versions.sortedByDescending { it.versionNumber },
                    editorAssets = assets,
                    editorExportVersionId = current.id,
                    editorExportJob = latestExport,
                    editorExportMessage = latestExport?.let(::exportStatusMessage),
                )
                if (latestExport?.status in EXPORT_ACTIVE) {
                    monitorCreationExport(latestExport!!)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    editorLoading = false,
                    editorMessage = error.userMessage("画布暂时无法载入，请稍后重试"),
                )
            }
        }
    }

    fun compareEditorWithPrevious(version: CreationVersionDto) {
        if (version.parentVersionId == null) {
            _state.value = _state.value.copy(editorMessage = "V1 是首个版本，没有上一版可比较")
            return
        }
        viewModelScope.launch {
            try {
                val diff = repository.compareCreationVersions(version.id)
                _state.value = _state.value.copy(editorDiff = diff, editorMessage = null)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    editorMessage = error.userMessage("版本差异暂时无法载入"),
                )
            }
        }
    }

    fun clearEditorDiff() {
        _state.value = _state.value.copy(editorDiff = null)
    }

    fun saveEditorVersion(
        projectId: String,
        parent: CreationVersionDto,
        layers: List<CreationLayerDto>,
        modificationReason: String,
        onSaved: (CreationVersionDto) -> Unit,
    ) {
        if (_state.value.editorSaving) return
        if (layers.isEmpty()) {
            _state.value = _state.value.copy(editorMessage = "画布至少需要保留一个图层")
            return
        }
        val normalizedLayers = layers.sortedBy { it.zIndex }.mapIndexed { index, layer ->
            layer.copy(zIndex = index)
        }
        val reason = modificationReason.trim().ifEmpty { "在画布编辑器中调整作品" }
        val signature = "$projectId|${parent.id}|$reason|${normalizedLayers.hashCode()}"
        val commit = pendingEditorCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-editor-${UUID.randomUUID()}")
                .also { pendingEditorCommit = it }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                editorSaving = true,
                editorMessage = "正在保存不可变新版本…",
                editorDiff = null,
            )
            try {
                val saved = repository.createCreationVersion(
                    projectId = projectId,
                    payload = CreationVersionCreateDto(
                        parentVersionId = parent.id,
                        layers = normalizedLayers,
                        canvasWidth = parent.canvasWidth,
                        canvasHeight = parent.canvasHeight,
                        previewAssetId = parent.previewAssetId,
                        changeSummary = "画布编辑：$reason",
                        modificationReason = reason,
                    ),
                    idempotencyKey = commit.idempotencyKey,
                )
                val project = repository.creationProject(projectId)
                _state.value = _state.value.copy(
                    editorSaving = false,
                    editorProject = project,
                    editorVersions = listOf(saved) + _state.value.editorVersions
                        .filterNot { it.id == saved.id },
                    editorMessage = "已保存为 V${saved.versionNumber}；旧版本仍可查看，AI 修改标记已同步",
                    editorExportBusy = false,
                    editorExportVersionId = saved.id,
                    editorExportJob = null,
                    editorExportMessage = "新版本尚未生成导出文件",
                )
                pendingEditorCommit = null
                onSaved(saved)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    editorSaving = false,
                    editorMessage = error.userMessage("保存失败，本页编辑仍然保留"),
                )
            }
        }
    }

    fun exportEditorVersion(
        version: CreationVersionDto,
        format: String,
        outputScale: Int,
    ) {
        if (_state.value.editorExportBusy) return
        val normalizedFormat = format.takeIf { it == "PNG" || it == "JPEG" } ?: "PNG"
        val normalizedScale = outputScale.coerceIn(1, 2)
        val signature = "${version.id}|$normalizedFormat|$normalizedScale"
        val commit = pendingExportCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-export-${UUID.randomUUID()}")
                .also { pendingExportCommit = it }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                editorExportBusy = true,
                editorExportVersionId = version.id,
                editorExportJob = null,
                editorExportMessage = "正在提交 V${version.versionNumber} 导出任务…",
            )
            try {
                val job = repository.createCreationExport(
                    versionId = version.id,
                    payload = CreationExportCreateDto(
                        format = normalizedFormat,
                        outputScale = normalizedScale,
                    ),
                    idempotencyKey = commit.idempotencyKey,
                )
                monitorCreationExport(job)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    editorExportBusy = false,
                    editorExportMessage = error.userMessage("导出任务提交失败，请稍后重试"),
                )
            }
        }
    }

    private suspend fun monitorCreationExport(initial: CreationExportJobDto) {
        var job = initial
        repeat(90) {
            if (_state.value.editorExportVersionId != job.creationVersionId) return
            _state.value = _state.value.copy(
                editorExportBusy = job.status in EXPORT_ACTIVE,
                editorExportJob = job,
                editorExportMessage = exportStatusMessage(job),
            )
            if (job.status !in EXPORT_ACTIVE) {
                pendingExportCommit = null
                return
            }
            delay(1_000)
            job = repository.creationExportJob(job.id)
        }
        _state.value = _state.value.copy(
            editorExportBusy = false,
            editorExportJob = job,
            editorExportMessage = "导出仍在后台处理，可稍后重新进入编辑器查看",
        )
    }

    private fun exportStatusMessage(job: CreationExportJobDto): String = when (job.status) {
        "QUEUED" -> "导出任务已排队（${job.progressPercent}%）"
        "RENDERING" -> "服务端正在合成全部图层（${job.progressPercent}%）"
        "SAFETY_CHECK" -> "合成完成，正在进行媒体安全检查（${job.progressPercent}%）"
        "COMPLETED" -> "V${job.versionNumber} 的 ${job.outputScale}× ${job.format} 已生成，可预览或下载"
        "REJECTED" -> job.errorSummary ?: "导出文件未通过安全检查"
        "FAILED" -> job.errorSummary ?: "导出失败，请重新生成"
        else -> "导出状态：${job.status}"
    }

    fun loadCreationSources() {
        if (_state.value.loadingManualSources) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loadingManualSources = true,
                manualSourceError = null,
            )
            try {
                val manuals = repository.learnedManualSources().map {
                    CreationManualOption(it.id, it.title, it.progressLabel)
                }
                _state.value = _state.value.copy(
                    loadingManualSources = false,
                    manualSources = manuals,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    loadingManualSources = false,
                    manualSourceError = error.userMessage("已学秘籍暂时无法载入"),
                )
            }
        }
    }

    fun uploadSketch(uriValue: String) {
        if (_state.value.uploadingSketch) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                uploadingSketch = true,
                sketchSource = null,
                sketchSourceMessage = "草图上传中，将先进行格式与安全检查…",
            )
            try {
                val file = readSketch(Uri.parse(uriValue))
                var asset = repository.uploadCreationSketch(
                    filename = file.filename,
                    mimeType = file.mimeType,
                    bytes = file.bytes,
                    sha256 = file.sha256,
                )
                repeat(30) {
                    when (asset.status) {
                        "READY" -> {
                            _state.value = _state.value.copy(
                                uploadingSketch = false,
                                sketchSource = CreationSketchSource(asset.id, file.filename),
                                sketchSourceMessage = "已带入草图：${file.filename}（安全检查通过）",
                            )
                            return@launch
                        }
                        "REJECTED" -> error(
                            asset.rejectionSummary ?: "草图未通过格式或安全检查"
                        )
                    }
                    delay(1_000)
                    asset = repository.mediaAsset(asset.id)
                }
                error("草图已上传，安全检查仍在进行，请稍后重新选择")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    uploadingSketch = false,
                    sketchSource = null,
                    sketchSourceMessage = error.userMessage(
                        error.message ?: "草图上传失败，请重新选择"
                    ),
                )
            }
        }
    }

    fun loadRecentProjects() {
        if (_state.value.loadingRecent) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingRecent = true, recentError = null)
            try {
                val result = repository.creations()
                _state.value = _state.value.copy(
                    recentProjects = result.items.distinctBy { it.id },
                    loadingRecent = false,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    loadingRecent = false,
                    recentError = error.userMessage("最近作品暂时无法载入，请稍后重试"),
                )
            }
        }
    }

    fun createProject(
        title: String,
        description: String,
        methodDraft: CreationMethodDraftDto,
        onCreated: (CreationProjectDto) -> Unit,
    ) {
        if (_state.value.creatingProject) return
        val analysis = _state.value.intentAnalysis ?: run {
            _state.value = _state.value.copy(createError = "请先让创作教练整理工法")
            return
        }
        val draft = methodDraft
        val signature = "${analysis.analysisId}|${title.trim()}|${description.trim()}"
        val commit = pendingCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-project-${UUID.randomUUID()}")
                .also { pendingCommit = it }
        viewModelScope.launch {
            _state.value = _state.value.copy(creatingProject = true, createError = null)
            try {
                var project = repository.createCreationProject(
                    payload = CreationProjectCreateDto(
                        title = title.trim(),
                        description = description.trim().takeIf { it.isNotEmpty() },
                        intentId = analysis.intentId,
                        derivativeAuthorizationId = _state.value.derivativeAuthorizationId,
                        mediaType = draft.recommendedMediaType,
                    ),
                    idempotencyKey = commit.idempotencyKey,
                )
                if (project.currentStage == "IDEATION") {
                    val method = try {
                        repository.getCreationMethod(project.id)
                    } catch (error: AuthApiException) {
                        if (error.statusCode != 404) throw error
                        repository.putCreationMethod(
                            projectId = project.id,
                            payload = CreationMethodPutDto(
                                name = draft.name,
                                goal = draft.goal,
                                audience = draft.audience,
                                format = draft.format,
                                steps = draft.steps,
                                resourceLinks = draft.resourceLinks,
                                sourceAssetIds = draft.sourceAssetIds,
                                manualPageIds = draft.manualPageIds,
                                expectedRevision = project.rowVersion,
                            ),
                        )
                    }
                    val transition = repository.transitionCreationStage(
                        projectId = project.id,
                        payload = CreationStageTransitionDto(
                            fromStage = "IDEATION",
                            toStage = "DRAFT",
                            reason = "用户确认创作工法并进入草图阶段",
                            expectedRevision = method.projectRevision,
                        ),
                    )
                    project = project.copy(
                        currentStage = transition.currentStage,
                        rowVersion = transition.projectRevision,
                    )
                }
                _state.value = _state.value.copy(
                    creatingProject = false,
                    intentAnalysis = null,
                    derivativeAuthorizationId = null,
                    derivativeSourceTitle = null,
                    sketchSource = null,
                    sketchSourceMessage = null,
                    recentProjects = listOf(project) + _state.value.recentProjects
                        .filterNot { it.id == project.id },
                )
                pendingCommit = null
                onCreated(project)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    creatingProject = false,
                    createError = error.userMessage("项目创建失败，你的想法仍保留在本页"),
                )
            }
        }
    }

    fun analyzeIntent(
        idea: String,
        attachmentAssetIds: List<String>,
        manualPageIds: List<String>,
    ) {
        val text = idea.trim()
        if (text.length < 2 || _state.value.analyzingIntent) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                analyzingIntent = true,
                intentAnalysis = null,
                analysisError = null,
                createError = null,
            )
            try {
                val analysis = repository.analyzeCreationIntent(
                    CreationIntentAnalyzeDto(
                        text = text,
                        attachmentAssetIds = attachmentAssetIds,
                        manualPageIds = manualPageIds,
                    )
                )
                pendingCommit = null
                _state.value = _state.value.copy(
                    analyzingIntent = false,
                    intentAnalysis = analysis,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    analyzingIntent = false,
                    analysisError = error.userMessage("暂时无法整理工法，你的想法仍保留在本页"),
                )
            }
        }
    }

    fun clearIntentAnalysis() {
        pendingCommit = null
        _state.value = _state.value.copy(
            intentAnalysis = null,
            analysisError = null,
            createError = null,
        )
    }

    fun clearCreateError() {
        _state.value = _state.value.copy(createError = null)
    }

    fun requestImageGeneration(
        projectId: String,
        parentVersionId: String,
        expectedProjectRevision: Int,
        prompt: String,
        size: String,
        quality: String,
        onComplete: () -> Unit,
    ) {
        val normalized = prompt.trim()
        if (normalized.length < 2 || _state.value.generationBusy) return
        val signature = "generation|$projectId|$parentVersionId|$size|$quality|$normalized"
        val commit = pendingGenerationCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-generation-${UUID.randomUUID()}")
                .also { pendingGenerationCommit = it }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                generationBusy = true,
                generationMessage = "生成任务正在排队…",
                generationProjectId = projectId,
            )
            try {
                val queued = repository.createImageGeneration(
                    projectId = projectId,
                    payload = ImageGenerationCreateDto(
                        parentVersionId = parentVersionId,
                        prompt = normalized,
                        size = size,
                        quality = quality,
                        expectedProjectRevision = expectedProjectRevision,
                    ),
                    idempotencyKey = commit.idempotencyKey,
                )
                pendingGenerationCommit = null
                val result = awaitImageGeneration(queued)
                finishImageGeneration(result, onComplete)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    generationBusy = false,
                    generationMessage = error.userMessage("图片生成暂时失败，请稍后重试"),
                    generationProjectId = projectId,
                )
            }
        }
    }

    fun retryImageGeneration(
        job: ImageGenerationJobDto,
        onComplete: () -> Unit,
    ) {
        if (_state.value.generationBusy || !job.retryable) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                generationBusy = true,
                generationJob = job,
                generationMessage = "正在重新排队…",
                generationProjectId = job.projectId,
            )
            try {
                val queued = repository.retryImageGeneration(
                    jobId = job.id,
                    payload = ImageGenerationRetryDto(job.rowVersion),
                    idempotencyKey = "android-generation-retry-${UUID.randomUUID()}",
                )
                val result = awaitImageGeneration(queued)
                finishImageGeneration(result, onComplete)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    generationBusy = false,
                    generationMessage = error.userMessage("重新生成失败，请稍后重试"),
                )
            }
        }
    }

    fun resumeImageGeneration(
        job: ImageGenerationJobDto,
        onComplete: () -> Unit,
    ) {
        if (job.status !in IMAGE_GENERATION_ACTIVE || _state.value.generationBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                generationBusy = true,
                generationJob = job,
                generationMessage = imageGenerationStatusMessage(job),
                generationProjectId = job.projectId,
            )
            try {
                val result = awaitImageGeneration(job)
                finishImageGeneration(result, onComplete)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    generationBusy = false,
                    generationMessage = error.userMessage("生成状态暂时无法刷新"),
                )
            }
        }
    }

    private suspend fun awaitImageGeneration(initial: ImageGenerationJobDto): ImageGenerationJobDto {
        var current = initial
        _state.value = _state.value.copy(
            generationJob = current,
            generationMessage = imageGenerationStatusMessage(current),
        )
        for (attempt in 0 until 240) {
            if (current.status !in IMAGE_GENERATION_ACTIVE) return current
            delay(1_000)
            current = repository.imageGenerationJob(current.id)
            _state.value = _state.value.copy(
                generationJob = current,
                generationMessage = imageGenerationStatusMessage(current),
            )
        }
        error("生成仍在后台进行，可稍后回到本页查看")
    }

    private fun finishImageGeneration(
        result: ImageGenerationJobDto,
        onComplete: () -> Unit,
    ) {
        _state.value = _state.value.copy(
            generationBusy = false,
            generationJob = result,
            generationMessage = imageGenerationStatusMessage(result),
            generationProjectId = result.projectId,
        )
        onComplete()
    }

    private fun imageGenerationStatusMessage(job: ImageGenerationJobDto): String = when (job.status) {
        "QUEUED" -> "生成任务已排队"
        "RUNNING" -> "正在生成画面…"
        "SAFETY_CHECK" -> "画面已生成，正在进行媒体安全检查…"
        "VERSIONING" -> "安全检查通过，正在写入新版本…"
        "COMPLETED" -> "生成完成，已写入新的不可变版本"
        "REJECTED" -> job.errorSummary ?: "生成内容未通过安全检查"
        else -> job.errorSummary ?: "图片生成失败"
    }

    fun requestCoachReview(
        projectId: String,
        prompt: String,
        onComplete: (CreationToolCallDto) -> Unit,
    ) {
        val normalized = prompt.trim()
        if (normalized.length < 2 || _state.value.workflowBusy) return
        val signature = "coach-proposal|$projectId|$normalized"
        val commit = pendingWorkflowCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-coach-${UUID.randomUUID()}")
                .also { pendingWorkflowCommit = it }
        runWorkflow(projectId, "正在准备调用确认…") {
            val call = repository.proposeCreationToolCall(
                projectId = projectId,
                payload = CreationToolCallProposeDto(prompt = normalized),
                idempotencyKey = commit.idempotencyKey,
            )
            pendingWorkflowCommit = null
            onComplete(call)
            "请先核对输入、影响和数据去向，再决定是否使用建议"
        }
    }

    fun decideCoachReview(
        call: CreationToolCallDto,
        approve: Boolean,
        onComplete: (CreationToolCallDto) -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        val signature = "coach-decision|${call.id}|$approve|${call.rowVersion}"
        val commit = pendingWorkflowCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-coach-decision-${UUID.randomUUID()}")
                .also { pendingWorkflowCommit = it }
        runWorkflow(call.projectId, if (approve) "正在生成文字建议…" else "正在取消本次建议…") {
            val updated = repository.decideCreationToolCall(
                toolCallId = call.id,
                payload = CreationToolCallDecisionDto(
                    approve = approve,
                    reason = if (approve) null else "用户在调用确认页选择暂不使用",
                    expectedRevision = call.rowVersion,
                ),
                idempotencyKey = commit.idempotencyKey,
            )
            pendingWorkflowCommit = null
            onComplete(updated)
            if (approve) "创作教练已返回文字建议，作品内容没有被自动修改" else "已取消，本次工具没有执行"
        }
    }

    fun moveCreationStage(
        project: CreationProjectDto,
        toStage: String,
        reason: String,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        runWorkflow(project.id, "正在更新创作阶段…") {
            val transition = repository.transitionCreationStage(
                projectId = project.id,
                payload = CreationStageTransitionDto(
                    fromStage = project.currentStage,
                    toStage = toStage,
                    reason = reason,
                    expectedRevision = project.rowVersion,
                ),
            )
            val updatedProject = project.copy(
                currentStage = transition.currentStage,
                rowVersion = transition.projectRevision,
            )
            _state.value = _state.value.copy(
                recentProjects = listOf(updatedProject) + _state.value.recentProjects
                    .filterNot { it.id == updatedProject.id },
            )
            onComplete()
            if (toStage == "TEST") "已进入测试：请记录一次真实检查" else "复测通过，已进入作品说明"
        }
    }

    fun recordCreationTest(
        projectId: String,
        creationVersionId: String,
        scenario: String,
        result: String,
        notes: String,
        finding: String,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy || scenario.trim().length < 2) return
        runWorkflow(projectId, "正在保存测试记录…") {
            repository.createCreationTestRecord(
                projectId = projectId,
                payload = CreationTestRecordCreateDto(
                    creationVersionId = creationVersionId,
                    scenario = scenario.trim(),
                    result = result,
                    notes = notes.trim(),
                    findings = finding.trim().takeIf { it.isNotEmpty() }?.let {
                        listOf(
                            CreationTestFindingCreateDto(
                                severity = if (result == "BLOCKED") "BLOCKING" else "IMPORTANT",
                                description = it,
                            )
                        )
                    }.orEmpty(),
                ),
            )
            onComplete()
            if (result == "PASSED") "测试已通过，可以在问题清零后进入说明" else "测试已记录，请完成问题整改后再复测"
        }
    }

    fun resolveCreationTestIssue(
        issue: CreationTestIssueDto,
        resolutionSummary: String,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy || resolutionSummary.trim().length < 2) return
        runWorkflow(issue.projectId, "正在保存整改说明…") {
            repository.resolveCreationTestIssue(
                issueId = issue.id,
                payload = CreationTestIssueResolveDto(
                    resolutionSummary = resolutionSummary.trim(),
                    expectedRevision = issue.rowVersion,
                ),
            )
            onComplete()
            "问题已关闭；仍需再提交一次通过的复测记录"
        }
    }

    fun saveLearningCard(
        projectId: String,
        versionId: String,
        manualPageIds: List<String>,
        methodSummary: String,
        unresolvedQuestionsText: String,
        questionsConfirmed: Boolean,
        rowVersion: Int?,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        runWorkflow(projectId, "正在保存学习说明…") {
            repository.putLearningCard(
                versionId = versionId,
                payload = LearningCardPutDto(
                    manualPageIds = manualPageIds.distinct(),
                    methodSummary = methodSummary.trim(),
                    unresolvedQuestions = unresolvedQuestionsText.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .take(20)
                        .toList(),
                    questionsConfirmed = questionsConfirmed,
                    rowVersion = rowVersion,
                ),
            )
            onComplete()
            "学习说明已保存"
        }
    }

    fun submitMigrationEvidence(
        projectId: String,
        creationVersionId: String,
        manualPageIds: List<String>,
        revisionReason: String,
        onComplete: () -> Unit,
    ) {
        val lessons = manualPageIds.distinct()
        val reason = revisionReason.trim()
        if (_state.value.workflowBusy || lessons.isEmpty() || reason.isEmpty()) return
        runWorkflow(projectId, "正在提交迁移证据…") {
            lessons.forEach { lessonId ->
                val keySeed = "$creationVersionId|$lessonId|$reason"
                repository.submitMigrationEvidence(
                    lessonId = lessonId,
                    payload = MigrationEvidenceCreateDto(
                        creationVersionId = creationVersionId,
                        usedLessons = lessons,
                        revisionReason = reason,
                    ),
                    idempotencyKey = "android-migration-${UUID.nameUUIDFromBytes(keySeed.toByteArray())}",
                )
            }
            onComplete()
            "迁移证据已提交，等待审核确认"
        }
    }

    @Suppress("LongParameterList")
    fun saveProvenance(
        projectId: String,
        versionId: String,
        humanSummary: String,
        aiUsed: Boolean,
        aiSummary: String,
        aiProvider: String,
        aiModel: String,
        aiAction: String,
        promptSummary: String,
        aiResultModified: Boolean,
        aigcLabelDeclared: Boolean,
        externalSourceUrl: String,
        externalSourceAuthor: String,
        externalLicense: String,
        unresolvedRights: Boolean,
        rowVersion: Int?,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        runWorkflow(projectId, "正在保存来源与人机分工…") {
            val items = mutableListOf<ProvenanceItemInputDto>()
            if (humanSummary.isNotBlank()) {
                items += ProvenanceItemInputDto(
                    itemType = "HUMAN_CONTRIBUTION",
                    contributionType = "构思与制作",
                    description = humanSummary.trim(),
                    licenseType = "ORIGINAL",
                )
            }
            if (aiUsed && aiSummary.isNotBlank()) {
                items += ProvenanceItemInputDto(
                    itemType = "AI_CONTRIBUTION",
                    contributionType = aiAction.trim().ifEmpty { "辅助创作" },
                    description = aiSummary.trim(),
                    licenseType = "NOT_APPLICABLE",
                    aiProvider = aiProvider.trim(),
                    aiModel = aiModel.trim(),
                    aiToolAction = aiAction.trim(),
                    promptSummary = promptSummary.trim(),
                    userModified = aiResultModified,
                )
            }
            if (externalSourceUrl.isNotBlank()) {
                items += ProvenanceItemInputDto(
                    itemType = "EXTERNAL_MATERIAL",
                    contributionType = "参考素材",
                    description = "创作中使用或参考的外部素材",
                    sourceUrl = externalSourceUrl.trim(),
                    sourceAuthor = externalSourceAuthor.trim().takeIf { it.isNotEmpty() },
                    licenseType = externalLicense,
                )
            }
            repository.putProvenance(
                versionId = versionId,
                payload = ProvenanceManifestPutDto(
                    humanContributionSummary = humanSummary.trim(),
                    aiAssistanceUsed = aiUsed,
                    aiContributionSummary = aiSummary.trim().takeIf { aiUsed },
                    aigcLabelDeclared = aigcLabelDeclared,
                    unresolvedRights = unresolvedRights,
                    items = items,
                    rowVersion = rowVersion,
                ),
            )
            onComplete()
            "来源与人机分工已保存"
        }
    }

    @Suppress("LongParameterList")
    fun saveSealCheck(
        projectId: String,
        versionId: String,
        workDescription: String,
        learningReflection: String,
        nextImprovement: String,
        identityPrivacyConfirmed: Boolean,
        contactPrivacyConfirmed: Boolean,
        portraitRightsConfirmed: Boolean,
        rowVersion: Int?,
        onComplete: () -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        runWorkflow(projectId, "正在保存作品说明与隐私自查…") {
            repository.putCreationSealCheck(
                versionId = versionId,
                payload = CreationSealCheckPutDto(
                    workDescription = workDescription.trim(),
                    learningReflection = learningReflection.trim(),
                    nextImprovement = nextImprovement.trim(),
                    identityPrivacyConfirmed = identityPrivacyConfirmed,
                    contactPrivacyConfirmed = contactPrivacyConfirmed,
                    portraitRightsConfirmed = portraitRightsConfirmed,
                    rowVersion = rowVersion,
                ),
            )
            onComplete()
            "作品说明与隐私自查已保存"
        }
    }

    fun submitCreation(
        projectId: String,
        versionId: String,
        visibility: String,
        targetClassroomId: String? = null,
        onComplete: (PublicationDto) -> Unit,
    ) {
        if (_state.value.workflowBusy) return
        val signature = "submission|$projectId|$versionId|$visibility|${targetClassroomId.orEmpty()}"
        val commit = pendingWorkflowCommit
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-submission-${UUID.randomUUID()}")
                .also { pendingWorkflowCommit = it }
        runWorkflow(projectId, "正在提交安全检查…") {
            val publication = repository.submitCreation(
                projectId = projectId,
                payload = CreationSubmissionCreateDto(
                    creationVersionId = versionId,
                    visibility = visibility,
                    targetClassroomId = targetClassroomId,
                ),
                idempotencyKey = commit.idempotencyKey,
            )
            pendingWorkflowCommit = null
            onComplete(publication)
            "已提交检查；通过前不会公开展示"
        }
    }

    private fun runWorkflow(
        projectId: String,
        pendingMessage: String,
        action: suspend () -> String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                workflowBusy = true,
                workflowMessage = pendingMessage,
                workflowMessageProjectId = projectId,
            )
            try {
                val message = action()
                _state.value = _state.value.copy(
                    workflowBusy = false,
                    workflowMessage = message,
                    workflowMessageProjectId = projectId,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    workflowBusy = false,
                    workflowMessage = error.userMessage("创作流程暂时无法更新，请稍后重试"),
                    workflowMessageProjectId = projectId,
                )
            }
        }
    }

    fun saveTextDraft(
        projectId: String,
        parentVersionId: String?,
        prompt: String,
        onSaved: (CreationVersionDto) -> Unit,
    ) {
        val text = prompt.trim()
        if (text.isEmpty() || _state.value.savingDraft) return
        val signature = "$projectId|${parentVersionId.orEmpty()}|$text"
        val save = pendingDraftSave
            ?.takeIf { it.signature == signature }
            ?: PendingCommit(signature, "android-version-${UUID.randomUUID()}")
                .also { pendingDraftSave = it }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                savingDraft = true,
                draftSaveMessage = null,
                draftMessageProjectId = projectId,
            )
            try {
                val version = repository.createCreationVersion(
                    projectId = projectId,
                    payload = CreationVersionCreateDto(
                        parentVersionId = parentVersionId,
                        layers = listOf(
                            CreationLayerDto(
                                layerId = "script-${save.idempotencyKey}",
                                kind = "TEXT",
                                name = "草图脚本",
                                zIndex = 0,
                                visible = true,
                                assetId = null,
                                textContent = text,
                                aigc = false,
                            )
                        ),
                        canvasWidth = 1080,
                        canvasHeight = 1920,
                        changeSummary = if (parentVersionId == null) {
                            "保存第一版草图/脚本"
                        } else {
                            "更新草图/脚本"
                        },
                    ),
                    idempotencyKey = save.idempotencyKey,
                )
                var project = repository.creationProject(projectId)
                if (project.currentStage == "DRAFT") {
                    val transition = repository.transitionCreationStage(
                        projectId = projectId,
                        payload = CreationStageTransitionDto(
                            fromStage = "DRAFT",
                            toStage = "PRODUCTION",
                            reason = "草图或脚本已保存，进入制作阶段",
                            expectedRevision = project.rowVersion,
                        ),
                    )
                    project = project.copy(
                        currentStage = transition.currentStage,
                        rowVersion = transition.projectRevision,
                    )
                }
                _state.value = _state.value.copy(
                    savingDraft = false,
                    draftSaveMessage = "已保存为 V${version.versionNumber}，可继续修改",
                    draftMessageProjectId = projectId,
                    recentProjects = listOf(project) + _state.value.recentProjects
                        .filterNot { it.id == project.id },
                )
                pendingDraftSave = null
                onSaved(version)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    savingDraft = false,
                    draftSaveMessage = error.userMessage("草图保存失败，本机输入仍然保留"),
                    draftMessageProjectId = projectId,
                )
            }
        }
    }

    private fun Exception.userMessage(fallback: String): String =
        (this as? AuthApiException)?.message ?: fallback

    private suspend fun readSketch(uri: Uri): SelectedSketch = withContext(Dispatchers.IO) {
        val resolver = getApplication<Application>().contentResolver
        var filename = "创作草图"
        var declaredSize: Long? = null
        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) filename = cursor.getString(nameIndex) ?: filename
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    declaredSize = cursor.getLong(sizeIndex)
                }
            }
        }
        val mimeType = resolver.getType(uri)?.lowercase()
            ?: error("无法识别草图格式，请选择 JPEG、PNG 或 WebP")
        if (mimeType !in setOf("image/jpeg", "image/png", "image/webp")) {
            error("目前只支持 JPEG、PNG 或 WebP 草图")
        }
        if ((declaredSize ?: 0L) > MAX_SKETCH_BYTES) {
            error("草图不能超过 20 MB")
        }
        val output = ByteArrayOutputStream()
        resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(16 * 1024)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > MAX_SKETCH_BYTES) error("草图不能超过 20 MB")
                output.write(buffer, 0, count)
            }
        } ?: error("无法读取所选草图")
        val bytes = output.toByteArray()
        if (bytes.isEmpty()) error("草图文件为空")
        val sha256 = MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
        SelectedSketch(filename.take(180), mimeType, bytes, sha256)
    }

    private data class PendingCommit(
        val signature: String,
        val idempotencyKey: String,
    )

    private data class SelectedSketch(
        val filename: String,
        val mimeType: String,
        val bytes: ByteArray,
        val sha256: String,
    )

    private companion object {
        const val MAX_SKETCH_BYTES = 20L * 1024 * 1024
        val IMAGE_GENERATION_ACTIVE = setOf("QUEUED", "RUNNING", "SAFETY_CHECK", "VERSIONING")
        val EXPORT_ACTIVE = setOf("QUEUED", "RENDERING", "SAFETY_CHECK")
    }
}

class CreationViewModelFactory(
    private val application: Application,
    private val repository: LuggageRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreationViewModel::class.java)) {
            return CreationViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
