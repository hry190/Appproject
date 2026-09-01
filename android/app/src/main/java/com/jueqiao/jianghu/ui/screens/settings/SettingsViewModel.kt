package com.jueqiao.jianghu.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jueqiao.jianghu.auth.AuthApiException
import com.jueqiao.jianghu.auth.BlacklistEntryDto
import com.jueqiao.jianghu.auth.SessionDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isSyncing: Boolean = false,
    val isSubmittingFeedback: Boolean = false,
    val isLoadingBlacklist: Boolean = false,
    val isLoadingSessions: Boolean = false,
    val blacklist: List<BlacklistEntryDto> = emptyList(),
    val sessions: List<SessionDto> = emptyList(),
    val eventMessage: String? = null,
)

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {
    val snapshot: StateFlow<SettingsSnapshot> = repository.snapshots.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.currentSnapshot(),
    )
    val currentUser = repository.currentUser

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        sync(silent = true)
    }

    fun sync(silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                repository.sync()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    eventMessage = if (silent) null else "设置已同步",
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    eventMessage = if (silent) null else localSavedMessage(error),
                )
            }
        }
    }

    fun setBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            try {
                repository.setBoolean(key, value)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(eventMessage = localSavedMessage(error))
            }
        }
    }

    fun setFloat(key: String, value: Float) {
        viewModelScope.launch {
            try {
                repository.setFloat(key, value)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(eventMessage = localSavedMessage(error))
            }
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            try {
                repository.restoreDefaults()
                _uiState.value = _uiState.value.copy(eventMessage = "个性化设置已恢复并同步")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    eventMessage = "已恢复本机设置；联网后将自动同步",
                )
            }
        }
    }

    fun submitFeedback(message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true)
            try {
                repository.submitFeedback(message)
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    eventMessage = "反馈已提交，我们会尽快处理",
                )
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    eventMessage = error.userMessage("反馈提交失败，请稍后重试"),
                )
            }
        }
    }

    fun loadBlacklist(showMessage: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingBlacklist = true)
            try {
                val entries = repository.getBlacklist()
                _uiState.value = _uiState.value.copy(
                    isLoadingBlacklist = false,
                    blacklist = entries,
                    eventMessage = if (showMessage) "黑名单已更新" else null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingBlacklist = false,
                    eventMessage = error.userMessage("黑名单加载失败，请稍后重试"),
                )
            }
        }
    }

    fun removeFromBlacklist(userId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromBlacklist(userId)
                _uiState.value = _uiState.value.copy(
                    blacklist = _uiState.value.blacklist.filterNot { it.userId == userId },
                    eventMessage = "已移出黑名单",
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    eventMessage = error.userMessage("操作失败，请稍后重试"),
                )
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSessions = true)
            try {
                _uiState.value = _uiState.value.copy(
                    isLoadingSessions = false,
                    sessions = repository.getSessions(),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingSessions = false,
                    eventMessage = error.userMessage("登录设备加载失败，请稍后重试"),
                )
            }
        }
    }

    fun loadAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSessions = true)
            try {
                repository.refreshCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isLoadingSessions = false,
                    sessions = repository.getSessions(),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingSessions = false,
                    eventMessage = error.userMessage("账号信息加载失败，请稍后重试"),
                )
            }
        }
    }

    fun consumeEvent() {
        if (_uiState.value.eventMessage != null) {
            _uiState.value = _uiState.value.copy(eventMessage = null)
        }
    }

    fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(eventMessage = message)
    }

    private fun localSavedMessage(error: Exception): String =
        if (error is AuthApiException && error.statusCode == 401) {
            "设置已保存在本机，请重新登录后同步"
        } else {
            "设置已保存在本机，联网后将自动同步"
        }

    private fun Exception.userMessage(fallback: String): String =
        (this as? AuthApiException)?.message ?: fallback
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
