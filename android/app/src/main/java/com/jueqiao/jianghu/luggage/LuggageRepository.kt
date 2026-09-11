package com.jueqiao.jianghu.luggage

import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.AuthApiException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class LuggageRepository(
    private val authRepository: AuthRepository,
    private val api: LuggageApi,
) {
    suspend fun capabilities(): CapabilitiesDto = api.getCapabilities()

    private val refreshMutex = Mutex()
    private var snapshot: LuggageResponseDto? = null
    private var etag: String? = null
    private var cachedUserId: String? = null

    suspend fun refresh(force: Boolean = false): LuggageResponseDto = refreshMutex.withLock {
        val userId = authRepository.currentUser.value?.id
        if (cachedUserId != null && cachedUserId != userId) clear()
        val result = authRepository.withAccessToken { token ->
            api.getLuggage(token, if (force) null else etag)
        }
        when (result) {
            is LuggageHttpResult.Fresh -> {
                snapshot = result.body
                etag = result.etag
                cachedUserId = userId
                result.body
            }
            LuggageHttpResult.NotModified -> snapshot ?: run {
                val fresh = authRepository.withAccessToken { token ->
                    api.getLuggage(token, null)
                } as LuggageHttpResult.Fresh
                snapshot = fresh.body
                etag = fresh.etag
                cachedUserId = userId
                fresh.body
            }
        }
    }

    fun latestSnapshot(): LuggageResponseDto? = snapshot

    fun clear() {
        snapshot = null
        etag = null
        cachedUserId = null
    }

    suspend fun badges(): List<BadgeDto> = authorized(api::getBadges)

    suspend fun evidence(
        category: String? = null,
        weekOnly: Boolean = false,
        cursor: String? = null,
    ): EvidenceListDto = authorized { token ->
        api.getEvidence(token, category, weekOnly, cursor)
    }

    suspend fun manuals(
        volume: Int? = null,
        query: String? = null,
        state: String? = null,
        favoritesOnly: Boolean = false,
        cursor: String? = null,
    ): ManualPageListDto = authorized { token ->
        api.getManuals(token, volume, query, state, favoritesOnly, cursor)
    }

    suspend fun learningOverview(): LearningOverviewDto = authorized { token ->
        api.getLearningOverview(token)
    }

    suspend fun setManualFavorite(manualId: String, favorite: Boolean) {
        authorized<Unit> { token -> api.setManualFavorite(token, manualId, favorite) }
    }

    suspend fun manualDetail(manualId: String): ManualDetailBundle = authorized { token ->
        ManualDetailBundle(
            manual = api.getManual(token, manualId),
            history = api.getManualLearningHistory(token, manualId),
            evidence = api.getManualEvidence(token, manualId),
        )
    }

    suspend fun submitMigrationEvidence(
        lessonId: String,
        payload: MigrationEvidenceCreateDto,
        idempotencyKey: String,
    ): MigrationEvidenceSubmittedDto = authorized { token ->
        api.submitMigrationEvidence(token, lessonId, payload, idempotencyKey)
    }

    suspend fun recordLessonRead(manualId: String): LessonReadEventAcceptedDto =
        authorized { token ->
            api.recordLessonRead(
                accessToken = token,
                lessonId = manualId,
                idempotencyKey = "android-read-${java.util.UUID.randomUUID()}",
            )
        }

    suspend fun mistakes(status: String? = null, cursor: String? = null): MistakeListDto =
        authorized { token -> api.getMistakes(token, status, cursor) }

    suspend fun mistakeDetail(mistakeId: String): MistakeDetailDto =
        authorized { token -> api.getMistake(token, mistakeId) }

    suspend fun createRetrySession(mistakeId: String): RetrySessionDto =
        authorized { token -> api.createRetrySession(token, mistakeId) }

    suspend fun trial(trialId: String): TrialDto =
        authorized { token -> api.getTrial(token, trialId) }

    suspend fun submitRetry(
        trialId: String,
        request: TrialAttemptRequestDto,
    ): TrialAttemptResultDto = authorized { token ->
        api.submitRetryAttempt(token, trialId, request)
    }

    suspend fun creations(status: String? = null, cursor: String? = null): CreationProjectListDto =
        authorized { token -> api.getCreations(token, status, cursor) }

    suspend fun creationProject(projectId: String): CreationProjectDto =
        authorized { token -> api.getCreationProject(token, projectId) }

    suspend fun createCreationProject(
        payload: CreationProjectCreateDto,
        idempotencyKey: String,
    ): CreationProjectDto = authorized { token ->
        api.createCreationProject(token, payload, idempotencyKey)
    }

    suspend fun patchCreationProject(
        projectId: String,
        payload: CreationProjectPatchDto,
    ): CreationProjectDto = authorized { token ->
        api.patchCreationProject(token, projectId, payload)
    }

    suspend fun analyzeCreationIntent(
        payload: CreationIntentAnalyzeDto,
    ): CreationIntentAnalysisDto = authorized { token ->
        api.analyzeCreationIntent(token, payload)
    }

    suspend fun startCreationConversation(
        payload: CreationConversationStartDto,
        idempotencyKey: String,
    ): CreationConversationDto = authorized { token ->
        api.startCreationConversation(token, payload, idempotencyKey)
    }

    suspend fun creationConversation(projectId: String): CreationConversationDto =
        authorized { token -> api.getCreationConversation(token, projectId) }

    suspend fun resumeCreationConversation(projectId: String): CreationConversationDto =
        authorized { token -> api.resumeCreationConversation(token, projectId) }

    suspend fun addCreationConversationMessage(
        projectId: String,
        text: String,
        idempotencyKey: String,
    ): CreationConversationDto = authorized { token ->
        api.addCreationConversationMessage(
            token,
            projectId,
            CreationConversationMessageCreateDto(text),
            idempotencyKey,
        )
    }

    suspend fun acceptCreationConversationSuggestion(
        projectId: String,
        messageId: String,
        expectedRevision: Int,
    ): CreationConversationDto = authorized { token ->
        api.acceptCreationConversationSuggestion(
            token,
            projectId,
            messageId,
            CreationConversationActionDto(expectedRevision),
        )
    }

    suspend fun generateFromCreationConversation(
        projectId: String,
        expectedRevision: Int,
        idempotencyKey: String,
    ): CreationConversationGenerationDto = authorized { token ->
        api.generateFromCreationConversation(
            token,
            projectId,
            CreationConversationGenerateDto(expectedRevision),
            idempotencyKey,
        )
    }

    suspend fun returnCreationConversation(
        projectId: String,
        expectedRevision: Int,
    ): CreationConversationDto = authorized { token ->
        api.returnCreationConversation(
            token,
            projectId,
            CreationConversationActionDto(expectedRevision),
        )
    }

    suspend fun saveCreationConversationResult(
        projectId: String,
        expectedRevision: Int,
    ): CreationConversationDto = authorized { token ->
        api.saveCreationConversationResult(
            token,
            projectId,
            CreationConversationActionDto(expectedRevision),
        )
    }

    suspend fun learnedManualSources(): List<ManualPageDto> = authorized { token ->
        listOf("LEARNED", "MASTERED", "TEACHING")
            .flatMap { state ->
                api.getManuals(
                    accessToken = token,
                    volume = null,
                    query = null,
                    state = state,
                    favoritesOnly = false,
                    cursor = null,
                    limit = 50,
                ).items
            }
            .distinctBy { it.id }
            .sortedWith(compareBy(ManualPageDto::volumeNo, ManualPageDto::pageNo))
    }

    suspend fun uploadCreationSketch(
        filename: String,
        mimeType: String,
        bytes: ByteArray,
        sha256: String,
    ): MediaAssetDto = authorized { token ->
        val upload = api.createMediaUploadIntent(
            token,
            MediaUploadIntentCreateDto(
                filename = filename,
                declaredMime = mimeType,
                byteSize = bytes.size,
                sha256 = sha256,
            ),
        )
        api.putMediaObject(token, upload, bytes, mimeType)
        api.completeMediaUpload(
            accessToken = token,
            uploadId = upload.id,
            payload = MediaUploadCompleteDto(bytes.size, sha256),
            idempotencyKey = "android-upload-${UUID.randomUUID()}",
        )
    }

    suspend fun mediaAsset(assetId: String): MediaAssetDto =
        authorized { token -> api.getMediaAsset(token, assetId) }

    suspend fun getCreationMethod(projectId: String): CreationMethodDto =
        authorized { token -> api.getCreationMethod(token, projectId) }

    suspend fun putCreationMethod(
        projectId: String,
        payload: CreationMethodPutDto,
    ): CreationMethodDto = authorized { token ->
        api.putCreationMethod(token, projectId, payload)
    }

    suspend fun transitionCreationStage(
        projectId: String,
        payload: CreationStageTransitionDto,
    ): CreationStageTransitionResultDto = authorized { token ->
        api.transitionCreationStage(token, projectId, payload)
    }

    suspend fun proposeCreationToolCall(
        projectId: String,
        payload: CreationToolCallProposeDto,
        idempotencyKey: String,
    ): CreationToolCallDto = authorized { token ->
        api.proposeCreationToolCall(token, projectId, payload, idempotencyKey)
    }

    suspend fun decideCreationToolCall(
        toolCallId: String,
        payload: CreationToolCallDecisionDto,
        idempotencyKey: String,
    ): CreationToolCallDto = authorized { token ->
        api.decideCreationToolCall(token, toolCallId, payload, idempotencyKey)
    }

    suspend fun createCreationTestRecord(
        projectId: String,
        payload: CreationTestRecordCreateDto,
    ): CreationTestRecordDto = authorized { token ->
        api.createCreationTestRecord(token, projectId, payload)
    }

    suspend fun resolveCreationTestIssue(
        issueId: String,
        payload: CreationTestIssueResolveDto,
    ): CreationTestIssueDto = authorized { token ->
        api.resolveCreationTestIssue(token, issueId, payload)
    }

    suspend fun createCreationVersion(
        projectId: String,
        payload: CreationVersionCreateDto,
        idempotencyKey: String,
    ): CreationVersionDto = authorized { token ->
        api.createCreationVersion(token, projectId, payload, idempotencyKey)
    }

    suspend fun compareCreationVersions(
        versionId: String,
        baseVersionId: String? = null,
    ): CreationVersionDiffDto = authorized { token ->
        api.getCreationVersionDiff(token, versionId, baseVersionId)
    }

    suspend fun createCreationExport(
        versionId: String,
        payload: CreationExportCreateDto,
        idempotencyKey: String,
    ): CreationExportJobDto = authorized { token ->
        api.createCreationExport(token, versionId, payload, idempotencyKey)
    }

    suspend fun creationExports(versionId: String): CreationExportJobListDto =
        authorized { token -> api.getCreationExports(token, versionId) }

    suspend fun creationExportJob(jobId: String): CreationExportJobDto =
        authorized { token -> api.getCreationExportJob(token, jobId) }

    suspend fun createImageGeneration(
        projectId: String,
        payload: ImageGenerationCreateDto,
        idempotencyKey: String,
    ): ImageGenerationJobDto = authorized { token ->
        api.createImageGeneration(token, projectId, payload, idempotencyKey)
    }

    suspend fun imageGenerationJob(jobId: String): ImageGenerationJobDto =
        authorized { token -> api.getImageGenerationJob(token, jobId) }

    suspend fun retryImageGeneration(
        jobId: String,
        payload: ImageGenerationRetryDto,
        idempotencyKey: String,
    ): ImageGenerationJobDto = authorized { token ->
        api.retryImageGeneration(token, jobId, payload, idempotencyKey)
    }

    suspend fun putLearningCard(
        versionId: String,
        payload: LearningCardPutDto,
    ): LearningCardDto = authorized { token ->
        api.putLearningCard(token, versionId, payload)
    }

    suspend fun putProvenance(
        versionId: String,
        payload: ProvenanceManifestPutDto,
    ): ProvenanceManifestDto = authorized { token ->
        api.putProvenance(token, versionId, payload)
    }

    suspend fun putCreationSealCheck(
        versionId: String,
        payload: CreationSealCheckPutDto,
    ): CreationSealCheckDto = authorized { token ->
        api.putCreationSealCheck(token, versionId, payload)
    }

    suspend fun submitCreation(
        projectId: String,
        payload: CreationSubmissionCreateDto,
        idempotencyKey: String,
    ): PublicationDto = authorized { token ->
        api.submitCreation(token, projectId, payload, idempotencyKey)
    }

    suspend fun conferenceCategorySuggestions(
        projectId: String,
    ): List<ConferenceCategorySuggestionDto> = authorized { token ->
        api.getConferenceCategorySuggestions(token, projectId).items
    }

    suspend fun creationDetail(projectId: String): CreationDetailBundle = authorized { token ->
        val project = api.getCreationProject(token, projectId)
        val conversation = api.resumeCreationConversation(token, projectId)
        val versions = api.getCreationVersions(token, projectId).items
        val method = runCatching { api.getCreationMethod(token, projectId) }
            .getOrElse { error ->
                if (error is AuthApiException && error.statusCode == 404) null else throw error
            }
        val stageEvents = api.getCreationStageEvents(token, projectId).items
        val toolCalls = api.getCreationToolCalls(token, projectId).items
        val testRecords = api.getCreationTestRecords(token, projectId).items
        val imageGenerations = api.getImageGenerations(token, projectId)
        val latestVersion = versions.maxByOrNull { it.versionNumber }
        val learningCard = latestVersion?.let { version ->
            runCatching { api.getLearningCard(token, version.id) }
                .getOrElse { error ->
                    if (error is AuthApiException && error.statusCode == 404) null else throw error
                }
        }
        val provenance = latestVersion?.let { version ->
            runCatching { api.getProvenance(token, version.id) }
                .getOrElse { error ->
                    if (error is AuthApiException && error.statusCode == 404) null else throw error
                }
        }
        val sealCheck = latestVersion?.let { version ->
            runCatching { api.getCreationSealCheck(token, version.id) }
                .getOrElse { error ->
                    if (error is AuthApiException && error.statusCode == 404) null else throw error
                }
        }
        val moderationCase = project.latestPublication?.let { publication ->
            runCatching { api.getModerationCase(token, publication.id) }
                .getOrElse { error ->
                    if (error is AuthApiException && error.statusCode == 404) null else throw error
                }
        }
        val moderationAppeals = moderationCase?.let { case ->
            api.getModerationAppeals(token).filter { it.moderationCaseId == case.id }
        }.orEmpty()
        CreationDetailBundle(
            project,
            conversation,
            versions,
            method,
            stageEvents,
            toolCalls,
            testRecords,
            imageGenerations,
            learningCard,
            provenance,
            sealCheck,
            moderationCase,
            moderationAppeals,
        )
    }

    suspend fun deleteCreationProject(projectId: String) {
        authorized<Unit> { token -> api.deleteCreationProject(token, projectId) }
    }

    suspend fun withdrawPublication(publicationId: String, rowVersion: Int) {
        authorized { token -> api.withdrawPublication(token, publicationId, rowVersion) }
    }

    suspend fun createAppeal(caseId: String, reason: String) {
        authorized { token -> api.createAppeal(token, caseId, reason) }
    }

    suspend fun privacy(): PrivacySettingsDto = authorized(api::getPrivacy)

    suspend fun updatePrivacy(patch: PrivacySettingsPatchDto): PrivacySettingsDto =
        authorized { token -> api.updatePrivacy(token, patch) }

    suspend fun createDataDeletionRequest(reason: String): DataRightsRequestDto =
        authorized { token ->
            api.createDataRightsRequest(token, "ACCOUNT_DELETION", reason)
        }

    suspend fun accountExportSummary(): String = authorized { token ->
        val payload = api.getAccountExport(token)
        val creations = payload.getAsJsonArray("creations")?.size() ?: 0
        val media = payload.getAsJsonArray("media_assets")?.size() ?: 0
        val appeals = payload.getAsJsonArray("moderation_appeals")?.size() ?: 0
        "导出已生成：作品 $creations 件、媒体 $media 项、申诉 $appeals 条"
    }

    suspend fun communityFeed(cursor: String? = null): PublicationFeedPageDto =
        authorized { token -> api.getCommunityFeed(token, cursor) }

    suspend fun publicationInbox(cursor: String? = null): PublicationFeedPageDto =
        authorized { token -> api.getPublicationInbox(token, cursor) }

    suspend fun classrooms(): List<ClassroomDto> =
        authorized { token -> api.getClassrooms(token).items }

    suspend fun createClassroom(name: String): ClassroomCreatedDto =
        authorized { token -> api.createClassroom(token, name) }

    suspend fun joinClassroom(joinCode: String): ClassroomDto =
        authorized { token -> api.joinClassroom(token, joinCode) }

    suspend fun conferenceFeed(
        cursor: String? = null,
        category: String? = null,
    ): ConferenceFeedDto = authorized { token ->
        api.getConferenceFeed(token, cursor, category)
    }

    suspend fun myConferenceWorks(): ConferenceFeedDto =
        authorized(api::getMyConferenceWorks)

    suspend fun conferenceWork(publicationId: String): ConferenceWorkDto =
        authorized { token -> api.getConferenceWork(token, publicationId) }

    suspend fun addConferenceLike(publicationId: String): ConferenceLikeDto =
        authorized { token -> api.addConferenceLike(token, publicationId) }

    suspend fun removeConferenceLike(publicationId: String) {
        authorized<Unit> { token -> api.removeConferenceLike(token, publicationId) }
    }

    suspend fun conferenceReviews(publicationId: String): ConferenceReviewListDto =
        authorized { token -> api.getConferenceReviews(token, publicationId) }

    suspend fun createConferenceReview(
        publicationId: String,
        template: String,
        content: String,
    ): ConferenceReviewDto = authorized { token ->
        api.createConferenceReview(token, publicationId, ConferenceReviewCreateDto(template, content))
    }

    suspend fun decideConferenceReview(
        reviewId: String,
        action: String,
        rowVersion: Int,
        reply: String? = null,
    ): ConferenceReviewDto = authorized { token ->
        api.decideConferenceReview(
            token,
            reviewId,
            ConferenceReviewDecisionDto(action, reply, rowVersion),
        )
    }

    suspend fun adoptConferenceReview(
        reviewId: String,
        creationVersionId: String,
        summary: String,
        rowVersion: Int,
    ): ConferenceReviewDto = authorized { token ->
        api.adoptConferenceReview(
            token,
            reviewId,
            ConferenceReviewAdoptionCreateDto(creationVersionId, summary, rowVersion),
        )
    }

    suspend fun reportConferenceReview(
        reviewId: String,
        reason: String,
        details: String? = null,
    ): ConferenceReviewReportDto = authorized { token ->
        api.reportConferenceReview(token, reviewId, ConferenceReviewReportCreateDto(reason, details))
    }

    suspend fun creationVersions(projectId: String): List<CreationVersionDto> = authorized { token ->
        api.getCreationVersions(token, projectId).items
    }

    suspend fun addConferenceCollection(publicationId: String): ConferenceCollectionDto =
        authorized { token -> api.addConferenceCollection(token, publicationId) }

    suspend fun removeConferenceCollection(publicationId: String) {
        authorized<Unit> { token -> api.removeConferenceCollection(token, publicationId) }
    }

    suspend fun conferenceCollections(): ConferenceCollectionListDto =
        authorized(api::getConferenceCollections)

    suspend fun createDerivativeRequest(
        sourcePublicationId: String,
        requestedUse: String,
    ): ConferenceDerivativeRequestDto = authorized { token ->
        api.createDerivativeRequest(
            token,
            ConferenceDerivativeRequestCreateDto(sourcePublicationId, requestedUse),
        )
    }

    suspend fun derivativeRequests(scope: String): ConferenceDerivativeRequestListDto =
        authorized { token -> api.getDerivativeRequests(token, scope) }

    suspend fun decideDerivativeRequest(
        requestId: String,
        decision: String,
        note: String? = null,
    ): ConferenceDerivativeRequestDto = authorized { token ->
        api.decideDerivativeRequest(token, requestId, ConferenceDerivativeDecisionDto(decision, note))
    }

    suspend fun revokeDerivativeAuthorization(
        authorizationId: String,
    ): ConferenceDerivativeAuthorizationDto = authorized { token ->
        api.revokeDerivativeAuthorization(token, authorizationId)
    }

    suspend fun conferenceMatchQueue(): ConferenceMatchQueueDto =
        authorized(api::getConferenceMatchQueue)

    suspend fun joinConferenceMatch(manualPageId: String): ConferenceMatchQueueDto =
        authorized { token -> api.joinConferenceMatch(token, ConferenceMatchJoinDto(manualPageId)) }

    suspend fun exitConferenceMatch(): ConferenceMatchQueueDto =
        authorized(api::exitConferenceMatch)

    suspend fun conferenceMatch(matchId: String): ConferenceMatchDetailDto = authorized { token ->
        api.getConferenceMatch(token, matchId)
    }

    suspend fun conferenceMatchRecords(
        outcome: String?,
        reflectionStatus: String?,
        page: Int,
        limit: Int,
    ): ConferenceMatchRecordListDto = authorized { token ->
        api.getConferenceMatchRecords(token, outcome, reflectionStatus, page, limit)
    }

    suspend fun answerConferenceMatch(
        matchId: String,
        questionId: String,
        answer: String,
        reason: String,
    ): ConferenceMatchAnswerSubmittedDto = authorized { token ->
        api.answerConferenceMatch(
            token,
            matchId,
            ConferenceMatchAnswerCreateDto(questionId, answer, reason),
        )
    }

    suspend fun conferenceMatchResult(matchId: String): ConferenceMatchResultDto = authorized { token ->
        api.getConferenceMatchResult(token, matchId)
    }

    suspend fun createConferenceMatchEvaluation(
        matchId: String,
        kind: String,
        score: Double,
        summary: String,
        strengths: List<String>,
        improvements: List<String>,
    ): ConferenceMatchEvaluationDto = authorized { token ->
        api.createConferenceMatchEvaluation(
            token,
            matchId,
            ConferenceMatchEvaluationCreateDto(
                kind = kind,
                score = score,
                summary = summary,
                strengths = strengths,
                improvements = improvements,
            ),
        )
    }

    suspend fun createConferenceMatchReflection(
        matchId: String,
        learned: String,
        nextImprovement: String,
    ): ConferenceMatchReflectionDto = authorized { token ->
        api.createConferenceMatchReflection(
            token,
            matchId,
            ConferenceMatchReflectionCreateDto(learned, nextImprovement),
        )
    }

    suspend fun reportConferenceMatch(
        matchId: String,
        reason: String,
        details: String? = null,
    ): ConferenceMatchReportDto = authorized { token ->
            api.reportConferenceMatch(token, matchId, ConferenceMatchReportCreateDto(reason, details))
    }

    suspend fun conferenceLetters(): ConferenceLetterListDto = authorized(api::getConferenceLetters)

    suspend fun readConferenceLetter(letterId: String): ConferenceLetterDto = authorized { token ->
        api.readConferenceLetter(token, letterId)
    }

    suspend fun conferenceMatchManuals(): List<ManualPageDto> = authorized { token ->
        api.getManuals(
            accessToken = token,
            volume = null,
            query = null,
            state = null,
            favoritesOnly = false,
            cursor = null,
            limit = 50,
        ).items
    }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.withAccessToken(block)
}
