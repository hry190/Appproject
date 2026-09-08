package com.jueqiao.jianghu.distribution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jueqiao.jianghu.auth.AuthApiException
import com.jueqiao.jianghu.luggage.ClassroomDto
import com.jueqiao.jianghu.luggage.LuggageRepository
import com.jueqiao.jianghu.luggage.PublicationFeedItemDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val DISTRIBUTION_REQUEST_TIMEOUT_MS = 20_000L

data class DistributionState(
    val community: List<PublicationFeedItemDto> = emptyList(),
    val communityCursor: String? = null,
    val communityLoading: Boolean = false,
    val communityError: String? = null,
    val inbox: List<PublicationFeedItemDto> = emptyList(),
    val inboxCursor: String? = null,
    val inboxLoading: Boolean = false,
    val inboxError: String? = null,
    val classrooms: List<ClassroomDto> = emptyList(),
    val classroomLoading: Boolean = false,
    val classroomMessage: String? = null,
    val oneTimeJoinCode: String? = null,
)

class DistributionViewModel(
    private val repository: LuggageRepository,
) : ViewModel() {
    private val _state = kotlinx.coroutines.flow.MutableStateFlow(DistributionState())
    val state = _state.asStateFlow()

    fun loadCommunity(loadMore: Boolean = false) {
        if (_state.value.communityLoading) return
        val cursor = if (loadMore) _state.value.communityCursor else null
        if (loadMore && cursor == null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(communityLoading = true, communityError = null)
            try {
                val page = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.communityFeed(cursor)
                }
                _state.value = _state.value.copy(
                    community = if (loadMore) _state.value.community + page.items else page.items,
                    communityCursor = page.nextCursor,
                )
            } catch (_: TimeoutCancellationException) {
                _state.value = _state.value.copy(communityError = "知行流载入超时，请重试")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    communityError = error.messageForUser("知行流暂时无法载入"),
                )
            } finally {
                _state.value = _state.value.copy(communityLoading = false)
            }
        }
    }

    fun loadInbox(loadMore: Boolean = false) {
        if (_state.value.inboxLoading) return
        val cursor = if (loadMore) _state.value.inboxCursor else null
        if (loadMore && cursor == null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(inboxLoading = true, inboxError = null)
            try {
                val page = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.publicationInbox(cursor)
                }
                _state.value = _state.value.copy(
                    inbox = if (loadMore) _state.value.inbox + page.items else page.items,
                    inboxCursor = page.nextCursor,
                )
            } catch (_: TimeoutCancellationException) {
                _state.value = _state.value.copy(inboxError = "作品来信载入超时，请重试")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    inboxError = error.messageForUser("作品来信暂时无法载入"),
                )
            } finally {
                _state.value = _state.value.copy(inboxLoading = false)
            }
        }
    }

    fun loadClassrooms() {
        if (_state.value.classroomLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(classroomLoading = true, classroomMessage = null)
            try {
                val classrooms = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.classrooms()
                }
                _state.value = _state.value.copy(
                    classrooms = classrooms,
                )
            } catch (_: TimeoutCancellationException) {
                _state.value = _state.value.copy(classroomMessage = "班级信息载入超时，请重试")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    classroomMessage = error.messageForUser("班级信息暂时无法载入"),
                )
            } finally {
                _state.value = _state.value.copy(classroomLoading = false)
            }
        }
    }

    fun refreshInboxAndClassrooms() {
        loadInbox()
        loadClassrooms()
    }

    fun createClassroom(name: String) {
        val normalized = name.trim()
        if (normalized.isEmpty() || _state.value.classroomLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(classroomLoading = true, classroomMessage = null)
            try {
                val created = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.createClassroom(normalized)
                }
                val classrooms = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.classrooms()
                }
                _state.value = _state.value.copy(
                    classrooms = classrooms,
                    classroomMessage = "班级已创建；邀请码只显示这一次",
                    oneTimeJoinCode = created.joinCode,
                )
            } catch (_: TimeoutCancellationException) {
                _state.value = _state.value.copy(classroomMessage = "班级创建超时，请刷新确认结果")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    classroomMessage = error.messageForUser("班级创建失败"),
                )
            } finally {
                _state.value = _state.value.copy(classroomLoading = false)
            }
        }
    }

    fun joinClassroom(code: String) {
        val normalized = code.replace("-", "").replace(" ", "").uppercase()
        if (normalized.length < 6 || _state.value.classroomLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(classroomLoading = true, classroomMessage = null)
            try {
                val joined = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.joinClassroom(normalized)
                }
                val classrooms = withTimeout(DISTRIBUTION_REQUEST_TIMEOUT_MS) {
                    repository.classrooms()
                }
                _state.value = _state.value.copy(
                    classrooms = classrooms,
                    classroomMessage = "已加入 ${joined.name}",
                    oneTimeJoinCode = null,
                )
            } catch (_: TimeoutCancellationException) {
                _state.value = _state.value.copy(classroomMessage = "加入班级超时，请刷新确认结果")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    classroomMessage = error.messageForUser("加入班级失败"),
                )
            } finally {
                _state.value = _state.value.copy(classroomLoading = false)
            }
        }
    }

    fun clearOneTimeJoinCode() {
        _state.value = _state.value.copy(oneTimeJoinCode = null)
    }
}

private fun Exception.messageForUser(fallback: String): String =
    (this as? AuthApiException)?.message?.takeIf { it.isNotBlank() } ?: fallback

class DistributionViewModelFactory(
    private val repository: LuggageRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DistributionViewModel(repository) as T
}
