package com.jueqiao.jianghu.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

enum class AuthOperation {
    Bootstrapping,
    Login,
    RequestCode,
    GuardianConsent,
    Register,
    ResetPassword,
}

data class AuthUiState(
    val operation: AuthOperation? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val requestId: String? = null,
) {
    val isBusy: Boolean get() = operation != null
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearFeedback() {
        if (_uiState.value.errorMessage != null) _uiState.value = AuthUiState()
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
            onComplete()
        }
    }

    fun bootstrap(onComplete: (Boolean) -> Unit) {
        if (_uiState.value.operation != null) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(operation = AuthOperation.Bootstrapping)
            try {
                val authenticated = repository.restoreSession()
                _uiState.value = AuthUiState()
                onComplete(authenticated)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setUnexpectedError()
                onComplete(false)
            }
        }
    }

    fun login(phone: String, password: String, onSuccess: () -> Unit) {
        launch(AuthOperation.Login, onSuccess) { repository.login(phone, password) }
    }

    fun requestCode(
        phone: String,
        purpose: VerificationPurpose,
        onSuccess: (Int) -> Unit,
    ) {
        if (_uiState.value.operation != null) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(operation = AuthOperation.RequestCode)
            try {
                val response = repository.requestVerificationCode(phone, purpose)
                _uiState.value = AuthUiState()
                onSuccess(response.retryAfter)
            } catch (error: AuthApiException) {
                error.retryAfter?.let(onSuccess)
                setError(error)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setUnexpectedError()
            }
        }
    }

    fun verifyGuardianConsent(
        childPhone: String,
        guardianPhone: String,
        verificationCode: String,
        onSuccess: (String) -> Unit,
    ) {
        if (_uiState.value.operation != null) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(operation = AuthOperation.GuardianConsent)
            try {
                val response = repository.verifyGuardianConsent(
                    childPhone,
                    guardianPhone,
                    verificationCode,
                )
                _uiState.value = AuthUiState()
                onSuccess(response.guardianConsentToken)
            } catch (error: AuthApiException) {
                setError(error)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setUnexpectedError()
            }
        }
    }

    fun register(
        phone: String,
        verificationCode: String,
        password: String,
        ageBand: AgeBand,
        guardianConsentToken: String?,
        onSuccess: () -> Unit,
    ) {
        launch(AuthOperation.Register, onSuccess) {
            repository.register(
                phone,
                verificationCode,
                password,
                ageBand,
                guardianConsentToken,
            )
        }
    }

    fun resetPassword(
        phone: String,
        verificationCode: String,
        newPassword: String,
        onSuccess: () -> Unit,
    ) {
        launch(AuthOperation.ResetPassword, onSuccess) {
            repository.resetPassword(phone, verificationCode, newPassword)
        }
    }

    private fun launch(
        operation: AuthOperation,
        onSuccess: () -> Unit,
        block: suspend () -> Any,
    ) {
        if (_uiState.value.operation != null) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(operation = operation)
            try {
                block()
                _uiState.value = AuthUiState()
                onSuccess()
            } catch (error: AuthApiException) {
                setError(error)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setUnexpectedError()
            }
        }
    }

    private fun setError(error: AuthApiException) {
        _uiState.value = AuthUiState(
            errorMessage = error.message,
            errorCode = error.code,
            requestId = error.requestId,
        )
    }

    private fun setUnexpectedError() {
        _uiState.value = AuthUiState(
            errorMessage = "本机通行令处理失败，请稍后重试",
            errorCode = "LOCAL_AUTH_FAILURE",
        )
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
