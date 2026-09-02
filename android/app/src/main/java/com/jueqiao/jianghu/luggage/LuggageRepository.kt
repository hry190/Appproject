package com.jueqiao.jianghu.luggage

import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.AuthApiException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LuggageRepository(
    private val authRepository: AuthRepository,
    private val api: LuggageApi,
) {
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

    suspend fun creationDetail(projectId: String): CreationDetailBundle = authorized { token ->
        val project = api.getCreationProject(token, projectId)
        val versions = api.getCreationVersions(token, projectId).items
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
        val moderationCase = project.latestPublication?.let { publication ->
            runCatching { api.getModerationCase(token, publication.id) }
                .getOrElse { error ->
                    if (error is AuthApiException && error.statusCode == 404) null else throw error
                }
        }
        CreationDetailBundle(project, versions, learningCard, provenance, moderationCase)
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

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.withAccessToken(block)
}
