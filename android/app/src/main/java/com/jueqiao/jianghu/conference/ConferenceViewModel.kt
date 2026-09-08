package com.jueqiao.jianghu.conference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jueqiao.jianghu.auth.AuthApiException
import com.jueqiao.jianghu.luggage.ConferenceCollectionDto
import com.jueqiao.jianghu.luggage.ConferenceDerivativeRequestDto
import com.jueqiao.jianghu.luggage.ConferenceLetterDto
import com.jueqiao.jianghu.luggage.ConferenceMatchDetailDto
import com.jueqiao.jianghu.luggage.ConferenceMatchQueueDto
import com.jueqiao.jianghu.luggage.ConferenceMatchResultDto
import com.jueqiao.jianghu.luggage.ConferenceReviewDto
import com.jueqiao.jianghu.luggage.ConferenceWorkDto
import com.jueqiao.jianghu.luggage.CreationVersionDto
import com.jueqiao.jianghu.luggage.LuggageRepository
import com.jueqiao.jianghu.luggage.ManualPageDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConferenceUiState(
    val conferenceEnabled: Boolean? = null,
    val capabilitiesError: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val works: List<ConferenceWorkDto> = emptyList(),
    val nextCursor: String? = null,
    val work: ConferenceWorkDto? = null,
    val reviews: List<ConferenceReviewDto> = emptyList(),
    val adoptionVersions: List<CreationVersionDto> = emptyList(),
    val collections: List<ConferenceCollectionDto> = emptyList(),
    val derivativeRequests: List<ConferenceDerivativeRequestDto> = emptyList(),
    val derivativeScope: String = "REQUESTED",
    val matchQueue: ConferenceMatchQueueDto? = null,
    val matchManuals: List<ManualPageDto> = emptyList(),
    val matchDetail: ConferenceMatchDetailDto? = null,
    val matchResult: ConferenceMatchResultDto? = null,
    val letters: List<ConferenceLetterDto> = emptyList(),
    val unreadLetterCount: Int = 0,
)

class ConferenceViewModel(
    private val repository: LuggageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConferenceUiState())
    val state: StateFlow<ConferenceUiState> = _state.asStateFlow()
    private var activeRequest: Job? = null
    private var capabilitiesJob: Job? = null
    private var letterSyncJob: Job? = null

    fun reset() {
        activeRequest?.cancel()
        capabilitiesJob?.cancel()
        letterSyncJob?.cancel()
        activeRequest = null
        capabilitiesJob = null
        letterSyncJob = null
        _state.value = ConferenceUiState()
    }

    fun loadCapabilities() {
        if (capabilitiesJob?.isActive == true) return
        capabilitiesJob = viewModelScope.launch {
            try {
                val capabilities = repository.capabilities()
                _state.value = _state.value.copy(
                    conferenceEnabled = capabilities.conference,
                    capabilitiesError = null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    capabilitiesError = (error as? AuthApiException)?.message
                        ?: "功能开关暂时无法载入",
                )
            }
        }
    }

    fun syncLetters() {
        if (letterSyncJob?.isActive == true) return
        letterSyncJob = viewModelScope.launch {
            try {
                val page = repository.conferenceLetters()
                _state.value = _state.value.copy(
                    letters = page.items,
                    unreadLetterCount = page.unreadCount,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 首页的未读提示是辅助信息；同步失败不遮挡主功能页面。
            }
        }
    }

    fun invalidateFeed() {
        _state.value = _state.value.copy(works = emptyList(), nextCursor = null)
    }

    fun loadFeed(append: Boolean = false) {
        val current = _state.value
        val cursor = if (append) current.nextCursor ?: return else null
        request("大会作品暂时无法载入，请稍后重试") {
            val page = repository.conferenceFeed(cursor)
            _state.value = _state.value.copy(
                works = if (append) current.works + page.items else page.items,
                nextCursor = page.nextCursor,
            )
        }
    }

    fun loadWork(publicationId: String) = request("作品详情暂时无法载入，请稍后重试") {
        val work = repository.conferenceWork(publicationId)
        val reviews = repository.conferenceReviews(publicationId).items
        val versions = if (work.isOwner) repository.creationVersions(work.projectId) else emptyList()
        _state.value = _state.value.copy(
            work = work,
            reviews = reviews,
            adoptionVersions = versions,
        )
    }

    fun createReview(publicationId: String, template: String, content: String) {
        val text = content.trim()
        if (text.isEmpty()) return
        request("评语提交失败，请稍后重试") {
            repository.createConferenceReview(publicationId, template, text)
            _state.value = _state.value.copy(
                reviews = repository.conferenceReviews(publicationId).items,
                message = "评语已提交",
            )
        }
    }

    fun decideReview(review: ConferenceReviewDto, action: String, reply: String? = null) =
        request("评语处理失败，请稍后重试") {
            val updated = repository.decideConferenceReview(
                reviewId = review.id,
                action = action,
                rowVersion = review.rowVersion,
                reply = reply?.trim()?.takeIf { it.isNotEmpty() },
            )
            _state.value = _state.value.copy(
                reviews = _state.value.reviews.map { item ->
                    if (item.id == updated.id) updated else item
                },
                message = "评语已处理",
            )
        }

    fun adoptReview(review: ConferenceReviewDto, creationVersionId: String, summary: String) {
        val text = summary.trim()
        if (text.isEmpty()) return
        request("采纳记录写入失败，请稍后重试") {
            val updated = repository.adoptConferenceReview(
                reviewId = review.id,
                creationVersionId = creationVersionId,
                summary = text,
                rowVersion = review.rowVersion,
            )
            _state.value = _state.value.copy(
                reviews = _state.value.reviews.map { if (it.id == updated.id) updated else it },
                message = "已记录这条评语带来的改进",
            )
        }
    }

    fun reportReview(reviewId: String, reason: String, details: String? = null) =
        request("举报提交失败，请稍后重试") {
            repository.reportConferenceReview(
                reviewId,
                reason,
                details?.trim()?.takeIf { it.isNotEmpty() },
            )
            _state.value = _state.value.copy(message = "举报已提交")
        }

    fun addCollection(publicationId: String) = request("收藏操作失败，请稍后重试") {
        repository.addConferenceCollection(publicationId)
        _state.value = _state.value.copy(message = "已收藏")
    }

    fun removeCollection(publicationId: String) = request("取消收藏失败，请稍后重试") {
        repository.removeConferenceCollection(publicationId)
        _state.value = _state.value.copy(
            collections = _state.value.collections.filterNot { it.publicationId == publicationId },
            message = "已取消收藏",
        )
    }

    fun loadCollections() = request("收藏暂时无法载入，请稍后重试") {
        _state.value = _state.value.copy(collections = repository.conferenceCollections().items)
    }

    fun createDerivativeRequest(publicationId: String, requestedUse: String) {
        val text = requestedUse.trim()
        if (text.isEmpty()) return
        request("授权申请提交失败，请稍后重试") {
            repository.createDerivativeRequest(publicationId, text)
            _state.value = _state.value.copy(message = "授权申请已提交")
        }
    }

    fun loadDerivativeRequests(scope: String = _state.value.derivativeScope) =
        request("授权记录暂时无法载入，请稍后重试") {
            _state.value = _state.value.copy(
                derivativeScope = scope,
                derivativeRequests = repository.derivativeRequests(scope).items,
            )
        }

    fun decideDerivativeRequest(requestId: String, approve: Boolean, note: String? = null) =
        request("授权处理失败，请稍后重试") {
            repository.decideDerivativeRequest(
                requestId = requestId,
                decision = if (approve) "APPROVE" else "REJECT",
                note = note?.trim()?.takeIf { it.isNotEmpty() },
            )
            _state.value = _state.value.copy(
                derivativeRequests = repository.derivativeRequests(_state.value.derivativeScope).items,
                message = if (approve) "已同意授权" else "已拒绝申请",
            )
        }

    fun revokeDerivativeAuthorization(authorizationId: String) =
        request("撤回授权失败，请稍后重试") {
            repository.revokeDerivativeAuthorization(authorizationId)
            _state.value = _state.value.copy(
                derivativeRequests = repository.derivativeRequests(_state.value.derivativeScope).items,
                message = "授权已撤回",
            )
        }

    fun loadMatch(matchId: String? = null) = request("切磋状态暂时无法载入，请稍后重试") {
        val snapshot = _state.value
        val queue = repository.conferenceMatchQueue()
        val resolvedMatchId = matchId ?: queue.matchId ?: snapshot.matchDetail?.matchId
        val detail = if (resolvedMatchId != null) {
            repository.conferenceMatch(resolvedMatchId)
        } else {
            null
        }
        val result = if (detail?.status == "ENDED") {
            repository.conferenceMatchResult(detail.matchId)
        } else {
            snapshot.matchResult?.takeIf { it.matchId == resolvedMatchId }
        }
        val manuals = if (
            queue.status in setOf("IDLE", "EXITED") && snapshot.matchManuals.isEmpty()
        ) {
            repository.conferenceMatchManuals()
        } else {
            snapshot.matchManuals
        }
        _state.value = _state.value.copy(
            matchQueue = queue,
            matchManuals = manuals,
            matchDetail = detail,
            matchResult = result,
        )
    }

    fun joinMatch(manualPageId: String) = request("暂时无法加入切磋，请稍后重试") {
        val queue = repository.joinConferenceMatch(manualPageId)
        _state.value = _state.value.copy(
            matchQueue = queue,
            matchDetail = if (queue.matchId != null) {
                repository.conferenceMatch(queue.matchId)
            } else {
                null
            },
            matchResult = null,
            message = "已更新切磋状态",
        )
    }

    fun exitMatch() = request("暂时无法退出切磋，请稍后重试") {
        _state.value = _state.value.copy(
            matchQueue = repository.exitConferenceMatch(),
            matchDetail = null,
            matchResult = null,
            message = "已退出切磋",
        )
    }

    fun clearCompletedMatch() {
        _state.value = _state.value.copy(matchDetail = null, matchResult = null)
    }

    fun submitMatchAnswer(matchId: String, questionId: String, answer: String, reason: String) {
        val answerText = answer.trim()
        val reasonText = reason.trim()
        if (answerText.isEmpty() || reasonText.isEmpty()) return
        request("回答提交失败，请稍后重试") {
            val submitted = repository.answerConferenceMatch(
                matchId,
                questionId,
                answerText,
                reasonText,
            )
            val detail = repository.conferenceMatch(matchId)
            _state.value = _state.value.copy(
                matchDetail = detail,
                matchResult = if (submitted.matchStatus == "ENDED") {
                    repository.conferenceMatchResult(matchId)
                } else {
                    null
                },
                message = if (submitted.myProgress.complete) "回答已完成，等待评审" else "回答已提交",
            )
        }
    }

    fun createMatchEvaluation(
        matchId: String,
        kind: String,
        score: Double,
        summary: String,
        strength: String,
        improvement: String,
    ) {
        val text = summary.trim()
        if (text.isEmpty()) return
        request("评价提交失败，请稍后重试") {
            repository.createConferenceMatchEvaluation(
                matchId = matchId,
                kind = kind,
                score = score.coerceIn(0.0, 100.0),
                summary = text,
                strengths = listOfNotNull(strength.trim().takeIf { it.isNotEmpty() }),
                improvements = listOfNotNull(improvement.trim().takeIf { it.isNotEmpty() }),
            )
            _state.value = _state.value.copy(
                matchResult = repository.conferenceMatchResult(matchId),
                message = "评价已提交",
            )
        }
    }

    fun createMatchReflection(matchId: String, learned: String, nextImprovement: String) {
        val learnedText = learned.trim()
        val improvementText = nextImprovement.trim()
        if (learnedText.isEmpty() || improvementText.isEmpty()) return
        request("复盘提交失败，请稍后重试") {
            repository.createConferenceMatchReflection(matchId, learnedText, improvementText)
            _state.value = _state.value.copy(
                matchResult = repository.conferenceMatchResult(matchId),
                message = "复盘已保存",
            )
        }
    }

    fun reportMatch(matchId: String, reason: String, details: String? = null) =
        request("举报提交失败，请稍后重试") {
            repository.reportConferenceMatch(matchId, reason, details)
            val detail = repository.conferenceMatch(matchId)
            _state.value = _state.value.copy(
                matchQueue = repository.conferenceMatchQueue(),
                matchDetail = detail,
                matchResult = if (detail.status == "ENDED") {
                    repository.conferenceMatchResult(matchId)
                } else {
                    null
                },
                message = "举报已提交，切磋已结束",
            )
        }

    fun loadLetters() = request("大会书信暂时无法载入，请稍后重试") {
        val page = repository.conferenceLetters()
        _state.value = _state.value.copy(
            letters = page.items,
            unreadLetterCount = page.unreadCount,
        )
    }

    fun readLetter(letter: ConferenceLetterDto) {
        if (letter.isRead) return
        _state.value = _state.value.copy(
            letters = _state.value.letters.map {
                if (it.id == letter.id) it.copy(isRead = true) else it
            },
            unreadLetterCount = (_state.value.unreadLetterCount - 1).coerceAtLeast(0),
        )
        viewModelScope.launch {
            try {
                val updated = repository.readConferenceLetter(letter.id)
                if (_state.value.letters.any { it.id == letter.id }) {
                    _state.value = _state.value.copy(
                        letters = _state.value.letters.map {
                            if (it.id == updated.id) updated else it
                        },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (_state.value.letters.any { it.id == letter.id }) {
                    _state.value = _state.value.copy(
                        letters = _state.value.letters.map {
                            if (it.id == letter.id) letter else it
                        },
                        unreadLetterCount = _state.value.unreadLetterCount + 1,
                        error = (error as? AuthApiException)?.message ?: "书信状态更新失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun request(fallback: String, action: suspend () -> Unit) {
        if (_state.value.loading) return
        activeRequest = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, message = null)
            try {
                action()
                _state.value = _state.value.copy(loading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = (error as? AuthApiException)?.message ?: fallback,
                )
            }
        }
    }
}

class ConferenceViewModelFactory(
    private val repository: LuggageRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConferenceViewModel::class.java)) {
            return ConferenceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
