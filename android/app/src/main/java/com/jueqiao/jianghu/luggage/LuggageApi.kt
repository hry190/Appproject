package com.jueqiao.jianghu.luggage

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.jueqiao.jianghu.auth.ApiErrorEnvelope
import com.jueqiao.jianghu.auth.AuthApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.TimeUnit

class LuggageApi(
    baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        // The contest Uvicorn service closes idle HTTP/1.1 connections before
        // OkHttp's five-minute default. Retire them on the client first so a
        // student pausing on a task never sends the next write to a stale socket.
        .connectionPool(ConnectionPool(5, 10, TimeUnit.SECONDS))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build(),
    private val gson: Gson = Gson(),
) {
    private val root = baseUrl.trimEnd('/')
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getCapabilities(): CapabilitiesDto = requestJson(
        requestBuilder("/v1/meta/capabilities").get().build(),
        CapabilitiesDto::class.java,
    )

    suspend fun getLuggage(accessToken: String, etag: String?): LuggageHttpResult =
        withContext(Dispatchers.IO) {
            val builder = requestBuilder("/v1/me/luggage").authorized(accessToken).get()
            if (etag != null) builder.header("If-None-Match", etag)
            execute(builder.build()).use { response ->
                if (response.code == 304) return@withContext LuggageHttpResult.NotModified
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
                val parsed = parseJson<LuggageResponseDto>(
                    response,
                    body,
                    LuggageResponseDto::class.java,
                )
                LuggageHttpResult.Fresh(
                    body = parsed,
                    etag = response.header("ETag") ?: parsed.meta.etag,
                )
            }
        }

    suspend fun getBadges(accessToken: String): List<BadgeDto> = get(
        accessToken = accessToken,
        path = "/v1/profile/badges",
        type = TypeToken.getParameterized(List::class.java, BadgeDto::class.java).type,
    )

    suspend fun getEvidence(
        accessToken: String,
        category: String?,
        weekOnly: Boolean,
        cursor: String?,
        limit: Int = 20,
    ): EvidenceListDto = get(
        accessToken,
        "/v1/me/learning-evidence",
        EvidenceListDto::class.java,
        mapOf(
            "category" to category,
            "week_only" to weekOnly.toString(),
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getManuals(
        accessToken: String,
        volume: Int?,
        query: String?,
        state: String?,
        favoritesOnly: Boolean,
        cursor: String?,
        limit: Int = 20,
    ): ManualPageListDto = get(
        accessToken,
        "/v1/manuals",
        ManualPageListDto::class.java,
        mapOf(
            "volume" to volume?.toString(),
            "q" to query?.takeIf { it.isNotBlank() },
            "state" to state,
            "favorites_only" to favoritesOnly.toString(),
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getLearningOverview(accessToken: String): LearningOverviewDto = get(
        accessToken,
        "/v1/learning/overview",
        LearningOverviewDto::class.java,
    )

    suspend fun setManualFavorite(
        accessToken: String,
        manualId: String,
        favorite: Boolean,
    ) = withContext(Dispatchers.IO) {
        val builder = requestBuilder("/v1/manuals/$manualId/favorite")
            .authorized(accessToken)
        val request = if (favorite) {
            builder.put(ByteArray(0).toRequestBody(null)).build()
        } else {
            builder.delete().build()
        }
        execute(request).use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw parseApiError(response.code, body)
        }
    }

    suspend fun getManual(accessToken: String, manualId: String): ManualDetailDto = get(
        accessToken,
        "/v1/manuals/$manualId",
        ManualDetailDto::class.java,
    )

    suspend fun recordLessonRead(
        accessToken: String,
        lessonId: String,
        idempotencyKey: String,
    ): LessonReadEventAcceptedDto = requestJson(
        requestBuilder("/v1/lessons/$lessonId/read-events")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(ByteArray(0).toRequestBody(null))
            .build(),
        LessonReadEventAcceptedDto::class.java,
    )

    suspend fun getManualEvidence(accessToken: String, manualId: String): List<EvidenceItemDto> = get(
        accessToken = accessToken,
        path = "/v1/manuals/$manualId/evidence",
        type = TypeToken.getParameterized(List::class.java, EvidenceItemDto::class.java).type,
    )

    suspend fun getManualLearningHistory(
        accessToken: String,
        manualId: String,
    ): ManualLearningHistoryDto = get(
        accessToken,
        "/v1/manuals/$manualId/learning-history",
        ManualLearningHistoryDto::class.java,
    )

    suspend fun submitMigrationEvidence(
        accessToken: String,
        lessonId: String,
        payload: MigrationEvidenceCreateDto,
        idempotencyKey: String,
    ): MigrationEvidenceSubmittedDto = requestJson(
        requestBuilder("/v1/lessons/$lessonId/migration-evidence")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        MigrationEvidenceSubmittedDto::class.java,
    )

    suspend fun getMistakes(
        accessToken: String,
        status: String?,
        cursor: String?,
        limit: Int = 20,
    ): MistakeListDto = get(
        accessToken,
        "/v1/mistakes",
        MistakeListDto::class.java,
        mapOf(
            "status" to status,
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getMistake(accessToken: String, mistakeId: String): MistakeDetailDto = get(
        accessToken,
        "/v1/mistakes/$mistakeId",
        MistakeDetailDto::class.java,
    )

    suspend fun createRetrySession(
        accessToken: String,
        mistakeId: String,
    ): RetrySessionDto = requestJson(
        requestBuilder("/v1/mistakes/$mistakeId/retry-sessions")
            .authorized(accessToken)
            .post(ByteArray(0).toRequestBody(null))
            .build(),
        RetrySessionDto::class.java,
    )

    suspend fun getTrial(accessToken: String, trialId: String): TrialDto = get(
        accessToken,
        "/v1/trials/$trialId",
        TrialDto::class.java,
    )

    suspend fun submitRetryAttempt(
        accessToken: String,
        trialId: String,
        request: TrialAttemptRequestDto,
    ): TrialAttemptResultDto = requestJson(
        requestBuilder("/v1/trials/$trialId/attempts")
            .authorized(accessToken)
            .header("Idempotency-Key", "android-retry-${UUID.randomUUID()}")
            .post(gson.toJson(request).toRequestBody(jsonMediaType))
            .build(),
        TrialAttemptResultDto::class.java,
    )

    suspend fun getCreations(
        accessToken: String,
        status: String?,
        cursor: String?,
        limit: Int = 20,
    ): CreationProjectListDto = get(
        accessToken,
        "/v1/me/creation-projects",
        CreationProjectListDto::class.java,
        mapOf(
            "status" to status,
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getCreationProject(
        accessToken: String,
        projectId: String,
    ): CreationProjectDto = get(
        accessToken,
        "/v1/creation-projects/$projectId",
        CreationProjectDto::class.java,
    )

    suspend fun createCreationProject(
        accessToken: String,
        payload: CreationProjectCreateDto,
        idempotencyKey: String,
    ): CreationProjectDto = requestJson(
        requestBuilder("/v1/creation-projects")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationProjectDto::class.java,
    )

    suspend fun patchCreationProject(
        accessToken: String,
        projectId: String,
        payload: CreationProjectPatchDto,
    ): CreationProjectDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId")
            .authorized(accessToken)
            .patch(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationProjectDto::class.java,
    )

    suspend fun analyzeCreationIntent(
        accessToken: String,
        payload: CreationIntentAnalyzeDto,
    ): CreationIntentAnalysisDto = requestJson(
        requestBuilder("/v1/creation-intents:analyze")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationIntentAnalysisDto::class.java,
    )

    suspend fun startCreationConversation(
        accessToken: String,
        payload: CreationConversationStartDto,
        idempotencyKey: String,
    ): CreationConversationDto = requestJson(
        requestBuilder("/v1/creation-conversations:start")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun getCreationConversation(
        accessToken: String,
        projectId: String,
    ): CreationConversationDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/conversation",
        CreationConversationDto::class.java,
    )

    suspend fun resumeCreationConversation(
        accessToken: String,
        projectId: String,
    ): CreationConversationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/conversation:resume")
            .authorized(accessToken)
            .post(ByteArray(0).toRequestBody(null))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun addCreationConversationMessage(
        accessToken: String,
        projectId: String,
        payload: CreationConversationMessageCreateDto,
        idempotencyKey: String,
    ): CreationConversationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/conversation/messages")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun acceptCreationConversationSuggestion(
        accessToken: String,
        projectId: String,
        messageId: String,
        payload: CreationConversationActionDto,
    ): CreationConversationDto = requestJson(
        requestBuilder(
            "/v1/creation-projects/$projectId/conversation/suggestions/$messageId:accept"
        )
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun generateFromCreationConversation(
        accessToken: String,
        projectId: String,
        payload: CreationConversationGenerateDto,
        idempotencyKey: String,
    ): CreationConversationGenerationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/conversation:generate")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationGenerationDto::class.java,
    )

    suspend fun returnCreationConversation(
        accessToken: String,
        projectId: String,
        payload: CreationConversationActionDto,
    ): CreationConversationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/conversation:return")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun saveCreationConversationResult(
        accessToken: String,
        projectId: String,
        payload: CreationConversationActionDto,
    ): CreationConversationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/conversation:save-result")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationConversationDto::class.java,
    )

    suspend fun createMediaUploadIntent(
        accessToken: String,
        payload: MediaUploadIntentCreateDto,
    ): MediaUploadIntentDto = requestJson(
        requestBuilder("/v1/uploads/intents")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        MediaUploadIntentDto::class.java,
    )

    suspend fun putMediaObject(
        accessToken: String,
        upload: MediaUploadIntentDto,
        bytes: ByteArray,
        mimeType: String,
    ) = withContext(Dispatchers.IO) {
        val isDirectUpload = upload.uploadUrl.startsWith("http://") ||
            upload.uploadUrl.startsWith("https://")
        val builder = if (isDirectUpload) {
            Request.Builder().url(upload.uploadUrl)
        } else {
            requestBuilder("/v1/uploads/${upload.id}/object").authorized(accessToken)
        }
        builder
            .put(bytes.toRequestBody(mimeType.toMediaType()))
        upload.requiredHeaders.forEach { (name, value) -> builder.header(name, value) }
        execute(builder.build()).use { response ->
            if (!response.isSuccessful) {
                throw AuthApiException(
                    statusCode = response.code,
                    code = "MEDIA_OBJECT_UPLOAD_FAILED",
                    message = "草图未能传入素材库，请重试",
                )
            }
        }
    }

    suspend fun completeMediaUpload(
        accessToken: String,
        uploadId: String,
        payload: MediaUploadCompleteDto,
        idempotencyKey: String,
    ): MediaAssetDto = requestJson(
        requestBuilder("/v1/uploads/$uploadId/complete")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        MediaAssetDto::class.java,
    )

    suspend fun getMediaAsset(
        accessToken: String,
        assetId: String,
    ): MediaAssetDto = get(
        accessToken,
        "/v1/media-assets/$assetId",
        MediaAssetDto::class.java,
    )

    suspend fun getCreationMethod(
        accessToken: String,
        projectId: String,
    ): CreationMethodDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/method",
        CreationMethodDto::class.java,
    )

    suspend fun putCreationMethod(
        accessToken: String,
        projectId: String,
        payload: CreationMethodPutDto,
    ): CreationMethodDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/method")
            .authorized(accessToken)
            .put(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationMethodDto::class.java,
    )

    suspend fun transitionCreationStage(
        accessToken: String,
        projectId: String,
        payload: CreationStageTransitionDto,
    ): CreationStageTransitionResultDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/stage-transitions")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationStageTransitionResultDto::class.java,
    )

    suspend fun getCreationStageEvents(
        accessToken: String,
        projectId: String,
    ): CreationStageEventListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/stage-events",
        CreationStageEventListDto::class.java,
    )

    suspend fun proposeCreationToolCall(
        accessToken: String,
        projectId: String,
        payload: CreationToolCallProposeDto,
        idempotencyKey: String,
    ): CreationToolCallDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/tool-calls")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationToolCallDto::class.java,
    )

    suspend fun getCreationToolCalls(
        accessToken: String,
        projectId: String,
    ): CreationToolCallListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/tool-calls",
        CreationToolCallListDto::class.java,
    )

    suspend fun decideCreationToolCall(
        accessToken: String,
        toolCallId: String,
        payload: CreationToolCallDecisionDto,
        idempotencyKey: String,
    ): CreationToolCallDto = requestJson(
        requestBuilder("/v1/creation-tool-calls/$toolCallId/decision")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationToolCallDto::class.java,
    )

    suspend fun createCreationTestRecord(
        accessToken: String,
        projectId: String,
        payload: CreationTestRecordCreateDto,
    ): CreationTestRecordDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/test-records")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationTestRecordDto::class.java,
    )

    suspend fun getCreationTestRecords(
        accessToken: String,
        projectId: String,
    ): CreationTestRecordListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/test-records",
        CreationTestRecordListDto::class.java,
    )

    suspend fun resolveCreationTestIssue(
        accessToken: String,
        issueId: String,
        payload: CreationTestIssueResolveDto,
    ): CreationTestIssueDto = requestJson(
        requestBuilder("/v1/creation-test-issues/$issueId/resolve")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationTestIssueDto::class.java,
    )

    suspend fun deleteCreationProject(accessToken: String, projectId: String) =
        withContext(Dispatchers.IO) {
            execute(
                requestBuilder("/v1/creation-projects/$projectId")
                    .authorized(accessToken)
                    .delete()
                    .build()
            ).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
            }
        }

    suspend fun getCreationVersions(
        accessToken: String,
        projectId: String,
    ): CreationVersionListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/versions",
        CreationVersionListDto::class.java,
    )

    suspend fun createCreationVersion(
        accessToken: String,
        projectId: String,
        payload: CreationVersionCreateDto,
        idempotencyKey: String,
    ): CreationVersionDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/versions")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationVersionDto::class.java,
    )

    suspend fun getCreationVersionDiff(
        accessToken: String,
        versionId: String,
        baseVersionId: String? = null,
    ): CreationVersionDiffDto = get(
        accessToken,
        buildString {
            append("/v1/creation-versions/$versionId/diff")
            if (baseVersionId != null) append("?base_version_id=$baseVersionId")
        },
        CreationVersionDiffDto::class.java,
    )

    suspend fun createCreationExport(
        accessToken: String,
        versionId: String,
        payload: CreationExportCreateDto,
        idempotencyKey: String,
    ): CreationExportJobDto = requestJson(
        requestBuilder("/v1/creation-versions/$versionId/exports")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationExportJobDto::class.java,
    )

    suspend fun getCreationExports(
        accessToken: String,
        versionId: String,
    ): CreationExportJobListDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/exports",
        CreationExportJobListDto::class.java,
    )

    suspend fun getCreationExportJob(
        accessToken: String,
        jobId: String,
    ): CreationExportJobDto = get(
        accessToken,
        "/v1/creation-export-jobs/$jobId",
        CreationExportJobDto::class.java,
    )

    suspend fun createImageGeneration(
        accessToken: String,
        projectId: String,
        payload: ImageGenerationCreateDto,
        idempotencyKey: String,
    ): ImageGenerationJobDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/image-generations")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ImageGenerationJobDto::class.java,
    )

    suspend fun getImageGenerations(
        accessToken: String,
        projectId: String,
    ): ImageGenerationJobListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/image-generations",
        ImageGenerationJobListDto::class.java,
    )

    suspend fun getImageGenerationJob(
        accessToken: String,
        jobId: String,
    ): ImageGenerationJobDto = get(
        accessToken,
        "/v1/image-generation-jobs/$jobId",
        ImageGenerationJobDto::class.java,
    )

    suspend fun retryImageGeneration(
        accessToken: String,
        jobId: String,
        payload: ImageGenerationRetryDto,
        idempotencyKey: String,
    ): ImageGenerationJobDto = requestJson(
        requestBuilder("/v1/image-generation-jobs/$jobId/retry")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ImageGenerationJobDto::class.java,
    )

    suspend fun getLearningCard(
        accessToken: String,
        versionId: String,
    ): LearningCardDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/learning-card",
        LearningCardDto::class.java,
    )

    suspend fun putLearningCard(
        accessToken: String,
        versionId: String,
        payload: LearningCardPutDto,
    ): LearningCardDto = requestJson(
        requestBuilder("/v1/creation-versions/$versionId/learning-card")
            .authorized(accessToken)
            .put(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        LearningCardDto::class.java,
    )

    suspend fun getProvenance(
        accessToken: String,
        versionId: String,
    ): ProvenanceManifestDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/provenance-manifest",
        ProvenanceManifestDto::class.java,
    )

    suspend fun putProvenance(
        accessToken: String,
        versionId: String,
        payload: ProvenanceManifestPutDto,
    ): ProvenanceManifestDto = requestJson(
        requestBuilder("/v1/creation-versions/$versionId/provenance-manifest")
            .authorized(accessToken)
            .put(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ProvenanceManifestDto::class.java,
    )

    suspend fun getCreationSealCheck(
        accessToken: String,
        versionId: String,
    ): CreationSealCheckDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/seal-check",
        CreationSealCheckDto::class.java,
    )

    suspend fun putCreationSealCheck(
        accessToken: String,
        versionId: String,
        payload: CreationSealCheckPutDto,
    ): CreationSealCheckDto = requestJson(
        requestBuilder("/v1/creation-versions/$versionId/seal-check")
            .authorized(accessToken)
            .put(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationSealCheckDto::class.java,
    )

    suspend fun submitCreation(
        accessToken: String,
        projectId: String,
        payload: CreationSubmissionCreateDto,
        idempotencyKey: String,
    ): PublicationDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/submissions")
            .authorized(accessToken)
            .header("Idempotency-Key", idempotencyKey)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        PublicationDto::class.java,
    )

    suspend fun getConferenceCategorySuggestions(
        accessToken: String,
        projectId: String,
    ): ConferenceCategorySuggestionListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/conference-category-suggestions",
        ConferenceCategorySuggestionListDto::class.java,
    )

    suspend fun getModerationCase(
        accessToken: String,
        publicationId: String,
    ): ModerationCaseDto = get(
        accessToken,
        "/v1/publications/$publicationId/moderation-case",
        ModerationCaseDto::class.java,
    )

    suspend fun withdrawPublication(
        accessToken: String,
        publicationId: String,
        rowVersion: Int,
    ): ModerationCaseDto = requestJson(
        requestBuilder("/v1/publications/$publicationId/withdraw")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("row_version" to rowVersion)).toRequestBody(jsonMediaType))
            .build(),
        ModerationCaseDto::class.java,
    )

    suspend fun createAppeal(
        accessToken: String,
        caseId: String,
        reason: String,
    ): ModerationAppealDto = requestJson(
        requestBuilder("/v1/moderation-cases/$caseId/appeals")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("reason" to reason)).toRequestBody(jsonMediaType))
            .build(),
        ModerationAppealDto::class.java,
    )

    suspend fun getModerationAppeals(accessToken: String): List<ModerationAppealDto> = get(
        accessToken = accessToken,
        path = "/v1/me/moderation-appeals",
        type = TypeToken.getParameterized(List::class.java, ModerationAppealDto::class.java).type,
    )

    suspend fun getPrivacy(accessToken: String): PrivacySettingsDto = get(
        accessToken,
        "/v1/me/privacy-settings",
        PrivacySettingsDto::class.java,
    )

    suspend fun updatePrivacy(
        accessToken: String,
        patch: PrivacySettingsPatchDto,
    ): PrivacySettingsDto = requestJson(
        requestBuilder("/v1/me/privacy-settings")
            .authorized(accessToken)
            .patch(gson.toJson(patch).toRequestBody(jsonMediaType))
            .build(),
        PrivacySettingsDto::class.java,
    )

    suspend fun createDataRightsRequest(
        accessToken: String,
        requestType: String,
        reason: String,
    ): DataRightsRequestDto = requestJson(
        requestBuilder("/v1/account/data-rights-requests")
            .authorized(accessToken)
            .post(
                gson.toJson(mapOf("request_type" to requestType, "reason" to reason))
                    .toRequestBody(jsonMediaType)
            )
            .build(),
        DataRightsRequestDto::class.java,
    )

    suspend fun getAccountExport(accessToken: String): JsonObject = get(
        accessToken,
        "/v1/account/export",
        JsonObject::class.java,
    )

    suspend fun getCommunityFeed(
        accessToken: String,
        cursor: String? = null,
    ): PublicationFeedPageDto = get(
        accessToken,
        "/v1/community/feed",
        PublicationFeedPageDto::class.java,
        mapOf("cursor" to cursor, "limit" to "20"),
    )

    suspend fun getPublicationInbox(
        accessToken: String,
        cursor: String? = null,
    ): PublicationFeedPageDto = get(
        accessToken,
        "/v1/me/publication-inbox",
        PublicationFeedPageDto::class.java,
        mapOf("cursor" to cursor, "limit" to "20"),
    )

    suspend fun getClassrooms(accessToken: String): ClassroomListDto = get(
        accessToken,
        "/v1/me/classrooms",
        ClassroomListDto::class.java,
    )

    suspend fun createClassroom(
        accessToken: String,
        name: String,
    ): ClassroomCreatedDto = requestJson(
        requestBuilder("/v1/classrooms")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("name" to name)).toRequestBody(jsonMediaType))
            .build(),
        ClassroomCreatedDto::class.java,
    )

    suspend fun joinClassroom(
        accessToken: String,
        joinCode: String,
    ): ClassroomDto = requestJson(
        requestBuilder("/v1/classrooms:join")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("join_code" to joinCode)).toRequestBody(jsonMediaType))
            .build(),
        ClassroomDto::class.java,
    )

    suspend fun getConferenceFeed(
        accessToken: String,
        cursor: String? = null,
        category: String? = null,
    ): ConferenceFeedDto = get(
        accessToken,
        "/v1/conference/feed",
        ConferenceFeedDto::class.java,
        mapOf("cursor" to cursor, "category" to category),
    )

    suspend fun getMyConferenceWorks(accessToken: String): ConferenceFeedDto = get(
        accessToken,
        "/v1/conference/me/works",
        ConferenceFeedDto::class.java,
    )

    suspend fun getConferenceWork(
        accessToken: String,
        publicationId: String,
    ): ConferenceWorkDto = get(
        accessToken,
        "/v1/conference/publications/$publicationId",
        ConferenceWorkDto::class.java,
    )

    suspend fun addConferenceLike(
        accessToken: String,
        publicationId: String,
    ): ConferenceLikeDto = requestJson(
        requestBuilder("/v1/conference/likes/$publicationId")
            .authorized(accessToken)
            .put(ByteArray(0).toRequestBody(null))
            .build(),
        ConferenceLikeDto::class.java,
    )

    suspend fun removeConferenceLike(accessToken: String, publicationId: String) =
        withContext(Dispatchers.IO) {
            execute(
                requestBuilder("/v1/conference/likes/$publicationId")
                    .authorized(accessToken)
                    .delete()
                    .build(),
            ).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
            }
        }

    suspend fun getConferenceReviews(
        accessToken: String,
        publicationId: String,
    ): ConferenceReviewListDto = get(
        accessToken,
        "/v1/conference/publications/$publicationId/reviews",
        ConferenceReviewListDto::class.java,
    )

    suspend fun createConferenceReview(
        accessToken: String,
        publicationId: String,
        payload: ConferenceReviewCreateDto,
    ): ConferenceReviewDto = requestJson(
        requestBuilder("/v1/conference/publications/$publicationId/reviews")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceReviewDto::class.java,
    )

    suspend fun decideConferenceReview(
        accessToken: String,
        reviewId: String,
        payload: ConferenceReviewDecisionDto,
    ): ConferenceReviewDto = requestJson(
        requestBuilder("/v1/conference/reviews/$reviewId/decision")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceReviewDto::class.java,
    )

    suspend fun adoptConferenceReview(
        accessToken: String,
        reviewId: String,
        payload: ConferenceReviewAdoptionCreateDto,
    ): ConferenceReviewDto = requestJson(
        requestBuilder("/v1/conference/reviews/$reviewId/adoption")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceReviewDto::class.java,
    )

    suspend fun reportConferenceReview(
        accessToken: String,
        reviewId: String,
        payload: ConferenceReviewReportCreateDto,
    ): ConferenceReviewReportDto = requestJson(
        requestBuilder("/v1/conference/reviews/$reviewId/reports")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceReviewReportDto::class.java,
    )

    suspend fun addConferenceCollection(
        accessToken: String,
        publicationId: String,
    ): ConferenceCollectionDto = requestJson(
        requestBuilder("/v1/conference/collections/$publicationId")
            .authorized(accessToken)
            .put(ByteArray(0).toRequestBody(null))
            .build(),
        ConferenceCollectionDto::class.java,
    )

    suspend fun removeConferenceCollection(accessToken: String, publicationId: String) =
        withContext(Dispatchers.IO) {
            execute(
                requestBuilder("/v1/conference/collections/$publicationId")
                    .authorized(accessToken)
                    .delete()
                    .build(),
            ).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
            }
        }

    suspend fun getConferenceCollections(accessToken: String): ConferenceCollectionListDto = get(
        accessToken,
        "/v1/conference/me/collections",
        ConferenceCollectionListDto::class.java,
    )

    suspend fun createDerivativeRequest(
        accessToken: String,
        payload: ConferenceDerivativeRequestCreateDto,
    ): ConferenceDerivativeRequestDto = requestJson(
        requestBuilder("/v1/conference/derivative-requests")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceDerivativeRequestDto::class.java,
    )

    suspend fun getDerivativeRequests(
        accessToken: String,
        scope: String,
    ): ConferenceDerivativeRequestListDto = get(
        accessToken,
        "/v1/conference/me/derivative-requests",
        ConferenceDerivativeRequestListDto::class.java,
        mapOf("scope" to scope),
    )

    suspend fun decideDerivativeRequest(
        accessToken: String,
        requestId: String,
        payload: ConferenceDerivativeDecisionDto,
    ): ConferenceDerivativeRequestDto = requestJson(
        requestBuilder("/v1/conference/derivative-requests/$requestId/decision")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceDerivativeRequestDto::class.java,
    )

    suspend fun revokeDerivativeAuthorization(
        accessToken: String,
        authorizationId: String,
    ): ConferenceDerivativeAuthorizationDto = requestJson(
        requestBuilder("/v1/conference/derivative-authorizations/$authorizationId/revoke")
            .authorized(accessToken)
            .post(ByteArray(0).toRequestBody(null))
            .build(),
        ConferenceDerivativeAuthorizationDto::class.java,
    )

    suspend fun getConferenceMatchQueue(accessToken: String): ConferenceMatchQueueDto = get(
        accessToken,
        "/v1/conference/match-queue",
        ConferenceMatchQueueDto::class.java,
    )

    suspend fun joinConferenceMatch(
        accessToken: String,
        payload: ConferenceMatchJoinDto,
    ): ConferenceMatchQueueDto = requestJson(
        requestBuilder("/v1/conference/match-queue")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceMatchQueueDto::class.java,
    )

    suspend fun exitConferenceMatch(accessToken: String): ConferenceMatchQueueDto = requestJson(
        requestBuilder("/v1/conference/match-queue")
            .authorized(accessToken)
            .delete()
            .build(),
        ConferenceMatchQueueDto::class.java,
    )

    suspend fun getConferenceMatch(
        accessToken: String,
        matchId: String,
    ): ConferenceMatchDetailDto = get(
        accessToken,
        "/v1/conference/matches/$matchId",
        ConferenceMatchDetailDto::class.java,
    )

    suspend fun getConferenceMatchRecords(
        accessToken: String,
        outcome: String?,
        reflectionStatus: String?,
        page: Int,
        limit: Int,
    ): ConferenceMatchRecordListDto {
        val params = mutableMapOf(
            "page" to page.toString(),
            "limit" to limit.toString(),
        )
        outcome?.let { params["outcome"] = it }
        reflectionStatus?.let { params["reflection_status"] = it }
        return get(
            accessToken,
            "/v1/conference/matches",
            ConferenceMatchRecordListDto::class.java,
            params,
        )
    }

    suspend fun answerConferenceMatch(
        accessToken: String,
        matchId: String,
        payload: ConferenceMatchAnswerCreateDto,
    ): ConferenceMatchAnswerSubmittedDto = requestJson(
        requestBuilder("/v1/conference/matches/$matchId/answers")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceMatchAnswerSubmittedDto::class.java,
    )

    suspend fun getConferenceMatchResult(
        accessToken: String,
        matchId: String,
    ): ConferenceMatchResultDto = get(
        accessToken,
        "/v1/conference/matches/$matchId/result",
        ConferenceMatchResultDto::class.java,
    )

    suspend fun createConferenceMatchEvaluation(
        accessToken: String,
        matchId: String,
        payload: ConferenceMatchEvaluationCreateDto,
    ): ConferenceMatchEvaluationDto = requestJson(
        requestBuilder("/v1/conference/matches/$matchId/evaluations")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceMatchEvaluationDto::class.java,
    )

    suspend fun createConferenceMatchReflection(
        accessToken: String,
        matchId: String,
        payload: ConferenceMatchReflectionCreateDto,
    ): ConferenceMatchReflectionDto = requestJson(
        requestBuilder("/v1/conference/matches/$matchId/reflections")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceMatchReflectionDto::class.java,
    )

    suspend fun reportConferenceMatch(
        accessToken: String,
        matchId: String,
        payload: ConferenceMatchReportCreateDto,
    ): ConferenceMatchReportDto = requestJson(
        requestBuilder("/v1/conference/matches/$matchId/reports")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        ConferenceMatchReportDto::class.java,
    )

    suspend fun getConferenceLetters(accessToken: String): ConferenceLetterListDto = get(
        accessToken,
        "/v1/conference/letters",
        ConferenceLetterListDto::class.java,
        mapOf("limit" to "100"),
    )

    suspend fun readConferenceLetter(
        accessToken: String,
        letterId: String,
    ): ConferenceLetterDto = requestJson(
        requestBuilder("/v1/conference/letters/$letterId/read")
            .authorized(accessToken)
            .put(ByteArray(0).toRequestBody(null))
            .build(),
        ConferenceLetterDto::class.java,
    )

    private suspend fun <T : Any> get(
        accessToken: String,
        path: String,
        type: Type,
        params: Map<String, String?> = emptyMap(),
    ): T = requestJson(
        requestBuilder(path, params).authorized(accessToken).get().build(),
        type,
    )

    private suspend fun <T : Any> requestJson(request: Request, type: Type): T =
        withContext(Dispatchers.IO) {
            execute(request).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
                parseJson(response, body, type)
            }
        }

    private fun requestBuilder(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): Request.Builder {
        val url = (root + path).toHttpUrl().newBuilder().apply {
            params.forEach { (name, value) ->
                if (value != null) addQueryParameter(name, value)
            }
        }.build()
        return Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-Request-ID", UUID.randomUUID().toString())
    }

    private fun Request.Builder.authorized(accessToken: String): Request.Builder =
        header("Authorization", "Bearer $accessToken")

    private fun execute(request: Request): Response {
        return try {
            client.newCall(request).execute()
        } catch (firstError: IOException) {
            if (request.method == "GET") {
                // The demo server may close an idle HTTP/1.1 socket between screens.
                // Evict pooled sockets so the single safe GET retry is truly fresh.
                client.connectionPool.evictAll()
                android.util.Log.w(
                    "LuggageApi",
                    "Retrying GET on a fresh connection: ${request.url}",
                    firstError,
                )
                try {
                    client.newCall(
                        request.newBuilder().header("Connection", "close").build(),
                    ).execute()
                } catch (retryError: IOException) {
                    throw networkUnavailable(request, retryError)
                }
            } else {
                throw networkUnavailable(request, firstError)
            }
        }
    }

    private fun networkUnavailable(request: Request, error: IOException): AuthApiException {
        android.util.Log.e("LuggageApi", "Request failed: ${request.method} ${request.url}", error)
        return AuthApiException(
            statusCode = 0,
            code = "NETWORK_UNAVAILABLE",
            message = "暂时无法连接江湖驿站，请检查网络后重试",
        )
    }

    private fun <T : Any> parseJson(response: Response, body: String, type: Type): T = try {
        @Suppress("UNCHECKED_CAST")
        (gson.fromJson<Any>(body, type) as T?) ?: error("Empty response body")
    } catch (_: RuntimeException) {
        throw AuthApiException(
            statusCode = response.code,
            code = "INVALID_SERVER_RESPONSE",
            message = "服务有点忙，请稍后再试",
            requestId = response.header("X-Request-ID"),
        )
    }

    private fun parseApiError(statusCode: Int, rawBody: String): AuthApiException {
        val payload = runCatching {
            gson.fromJson(rawBody, ApiErrorEnvelope::class.java).error
        }.getOrNull()
        return AuthApiException(
            statusCode = statusCode,
            code = payload?.code ?: "HTTP_$statusCode",
            message = payload?.message ?: "操作未完成，请稍后重试",
            retryAfter = payload?.retryAfter,
            requestId = payload?.requestId,
        )
    }
}
