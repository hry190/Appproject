package com.jueqiao.jianghu.luggage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jueqiao.jianghu.auth.AuthApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LuggageUiState {
    data object Loading : LuggageUiState
    data class Content(
        val snapshot: LuggageResponseDto,
        val refreshing: Boolean = false,
        val notice: String? = null,
    ) : LuggageUiState
    data class Error(
        val message: String,
        val requestId: String? = null,
        val canRetry: Boolean = true,
    ) : LuggageUiState
}

data class LuggageDetailState(
    val loading: Boolean = false,
    val message: String? = null,
    val retryable: Boolean = false,
    val badges: List<BadgeDto> = emptyList(),
    val evidence: EvidenceListDto? = null,
    val manuals: ManualPageListDto? = null,
    val manualDetail: ManualDetailBundle? = null,
    val mistakes: MistakeListDto? = null,
    val mistakeDetail: MistakeDetailDto? = null,
    val creations: CreationProjectListDto? = null,
    val creationDetail: CreationDetailBundle? = null,
    val privacy: PrivacySettingsDto? = null,
    val trial: TrialDto? = null,
    val trialResult: TrialAttemptResultDto? = null,
    val accountExportSummary: String? = null,
    val dataRightsRequest: DataRightsRequestDto? = null,
)

class LuggageViewModel(private val repository: LuggageRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<LuggageUiState>(LuggageUiState.Loading)
    val uiState: StateFlow<LuggageUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(LuggageDetailState())
    val detailState: StateFlow<LuggageDetailState> = _detailState.asStateFlow()

    fun refresh(force: Boolean = false) {
        val previous = (_uiState.value as? LuggageUiState.Content)?.snapshot
            ?: repository.latestSnapshot()
        _uiState.value = previous?.let { LuggageUiState.Content(it, refreshing = true) }
            ?: LuggageUiState.Loading
        viewModelScope.launch {
            try {
                _uiState.value = LuggageUiState.Content(repository.refresh(force))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = error.userMessage("行囊暂时无法打开，请稍后重试")
                _uiState.value = previous?.let {
                    LuggageUiState.Content(it, notice = message)
                } ?: LuggageUiState.Error(
                    message = message,
                    requestId = (error as? AuthApiException)?.requestId,
                )
            }
        }
    }

    fun loadBadges() = loadDetail { copy(badges = repository.badges()) }

    fun loadEvidence(category: String? = null, weekOnly: Boolean = false) =
        loadDetail { copy(evidence = repository.evidence(category, weekOnly)) }

    fun loadMoreEvidence(category: String? = null, weekOnly: Boolean = false) {
        val current = _detailState.value.evidence ?: return
        val cursor = current.nextCursor ?: return
        loadDetail {
            val next = repository.evidence(category, weekOnly, cursor)
            copy(evidence = next.copy(items = current.items + next.items))
        }
    }

    fun loadManuals(
        volume: Int? = null,
        query: String? = null,
        state: String? = null,
        favoritesOnly: Boolean = false,
    ) = loadDetail {
        copy(manuals = repository.manuals(volume, query, state, favoritesOnly))
    }

    fun loadMoreManuals(
        volume: Int? = null,
        query: String? = null,
        state: String? = null,
        favoritesOnly: Boolean = false,
    ) {
        val current = _detailState.value.manuals ?: return
        val cursor = current.nextCursor ?: return
        loadDetail {
            val next = repository.manuals(volume, query, state, favoritesOnly, cursor)
            copy(manuals = next.copy(items = current.items + next.items))
        }
    }

    fun toggleManualFavorite(item: ManualPageDto) {
        viewModelScope.launch {
            try {
                repository.setManualFavorite(item.id, !item.isFavorite)
                val current = _detailState.value.manuals ?: return@launch
                _detailState.value = _detailState.value.copy(
                    manuals = current.copy(
                        items = current.items.map {
                            if (it.id == item.id) it.copy(isFavorite = !item.isFavorite) else it
                        }
                    )
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showDetailMessage(error.userMessage("收藏操作失败，请稍后重试"))
            }
        }
    }

    fun loadManualDetail(manualId: String) =
        loadDetail { copy(manualDetail = repository.manualDetail(manualId)) }

    fun loadMistakes(status: String? = null) =
        loadDetail { copy(mistakes = repository.mistakes(status)) }

    fun loadMistakeDetail(mistakeId: String) =
        loadDetail { copy(mistakeDetail = repository.mistakeDetail(mistakeId)) }

    fun loadMoreMistakes(status: String? = null) {
        val current = _detailState.value.mistakes ?: return
        val cursor = current.nextCursor ?: return
        loadDetail {
            val next = repository.mistakes(status, cursor)
            copy(mistakes = next.copy(items = current.items + next.items))
        }
    }

    fun retryMistake(mistakeId: String, onReady: (RetrySessionDto) -> Unit) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                val session = repository.createRetrySession(mistakeId)
                _detailState.value = _detailState.value.copy(loading = false, retryable = false)
                onReady(session)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("暂时无法开始重练，请稍后重试"),
                    retryable = true,
                )
            }
        }
    }

    fun refreshMistakeAfterRetry(mistakeId: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                val detail = repository.mistakeDetail(mistakeId)
                val list = repository.mistakes()
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    mistakeDetail = detail,
                    mistakes = list,
                    trialResult = null,
                    retryable = false,
                )
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("重练结果已提交，错题状态稍后可重新加载"),
                    retryable = true,
                )
            }
        }
    }

    fun loadRetryTrial(trialId: String) =
        loadDetail { copy(trial = repository.trial(trialId), trialResult = null) }

    fun submitRetry(
        session: RetrySessionDto,
        prediction: String?,
        answer: String,
        explanation: String,
    ) {
        val trial = _detailState.value.trial ?: return
        val propertyName = trial.currentVersion.answerSchema
            .getAsJsonObject("properties")
            ?.keySet()
            ?.firstOrNull()
            ?: "choice"
        loadDetail {
            copy(
                trialResult = repository.submitRetry(
                    session.trialId,
                    TrialAttemptRequestDto(
                        trialVersionId = session.trialVersionId,
                        predictionPayload = prediction?.let { mapOf(propertyName to it) },
                        answerPayload = mapOf(propertyName to answer),
                        explanation = explanation.takeIf { it.isNotBlank() },
                        remediationContextId = session.id,
                        clientRequestId = "android-retry-${java.util.UUID.randomUUID()}",
                    ),
                )
            )
        }
    }

    fun loadCreations(status: String? = null) =
        loadDetail { copy(creations = repository.creations(status)) }

    fun loadMoreCreations(status: String? = null) {
        val current = _detailState.value.creations ?: return
        val cursor = current.nextCursor ?: return
        loadDetail {
            val next = repository.creations(status, cursor)
            copy(creations = next.copy(items = current.items + next.items))
        }
    }

    fun loadCreationDetail(projectId: String) =
        loadDetail { copy(creationDetail = repository.creationDetail(projectId)) }

    fun withdrawPublication(projectId: String) {
        val publication = _detailState.value.creationDetail?.project?.latestPublication ?: return
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                repository.withdrawPublication(publication.id, publication.rowVersion)
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    creationDetail = repository.creationDetail(projectId),
                    message = "作品已撤回",
                    retryable = false,
                )
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("作品撤回失败，请稍后重试"),
                    retryable = true,
                )
            }
        }
    }

    fun deleteCreationProject(projectId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                repository.deleteCreationProject(projectId)
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    creationDetail = null,
                    message = "作品已删除",
                    retryable = false,
                )
                refresh(force = true)
                onDeleted()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("作品删除失败，请稍后重试"),
                    retryable = true,
                )
            }
        }
    }

    fun createAppeal(projectId: String, caseId: String, reason: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                repository.createAppeal(caseId, reason)
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    creationDetail = repository.creationDetail(projectId),
                    message = "申诉已提交",
                    retryable = false,
                )
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("申诉提交失败，请稍后重试"),
                    retryable = true,
                )
            }
        }
    }

    fun loadPrivacy() = loadDetail { copy(privacy = repository.privacy()) }

    fun updatePrivacy(transform: (PrivacySettingsDto) -> PrivacySettingsPatchDto) {
        val current = _detailState.value.privacy ?: return
        loadDetail { copy(privacy = repository.updatePrivacy(transform(current))) }
    }

    fun loadAccountExport() =
        loadDetail { copy(accountExportSummary = repository.accountExportSummary()) }

    fun requestAccountDeletion(reason: String) =
        loadDetail {
            copy(dataRightsRequest = repository.createDataDeletionRequest(reason))
        }

    fun consumeDetailMessage() {
        _detailState.value = _detailState.value.copy(message = null, retryable = false)
    }

    private fun loadDetail(block: suspend LuggageDetailState.() -> LuggageDetailState) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                loading = true,
                message = null,
                retryable = false,
            )
            try {
                _detailState.value = _detailState.value.block().copy(
                    loading = false,
                    retryable = false,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _detailState.value = _detailState.value.copy(
                    loading = false,
                    message = error.userMessage("内容加载失败，请稍后重试"),
                    retryable = true,
                )
            }
        }
    }

    private fun showDetailMessage(message: String) {
        _detailState.value = _detailState.value.copy(message = message, retryable = false)
    }

    private fun Exception.userMessage(fallback: String): String =
        (this as? AuthApiException)?.message ?: fallback
}

class LuggageViewModelFactory(
    private val repository: LuggageRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LuggageViewModel::class.java)) {
            return LuggageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
