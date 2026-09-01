package com.jueqiao.jianghu.auth

import com.jueqiao.jianghu.BuildConfig
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: EncryptedTokenStore,
) {
    @Volatile
    private var accessToken: String? = null
    private val refreshMutex = Mutex()
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    fun hasStoredSession(): Boolean = tokenStore.readRefreshToken() != null

    suspend fun restoreSession(): Boolean {
        val refreshToken = tokenStore.readRefreshToken() ?: return false
        return try {
            saveTokens(api.refresh(refreshToken))
            _currentUser.value = api.currentUser(requireAccessToken())
            true
        } catch (error: AuthApiException) {
            if (error.statusCode == 401) clearSession()
            false
        }
    }

    suspend fun login(phone: String, password: String): AuthResponseDto {
        return api.login(phone, password).also {
            saveTokens(it.tokens)
            _currentUser.value = it.user
        }
    }

    suspend fun register(
        phone: String,
        verificationCode: String,
        password: String,
        ageBand: AgeBand,
        guardianConsentToken: String?,
    ): AuthResponseDto {
        val response = api.register(
            RegisterRequest(
                phone = phone,
                verificationCode = verificationCode,
                password = password,
                ageBand = ageBand.apiValue,
                termsVersion = BuildConfig.TERMS_VERSION,
                privacyVersion = BuildConfig.PRIVACY_VERSION,
                guardianConsentToken = guardianConsentToken,
                clientRequestId = UUID.randomUUID().toString(),
            )
        )
        saveTokens(response.tokens)
        _currentUser.value = response.user
        return response
    }

    suspend fun requestVerificationCode(
        phone: String,
        purpose: VerificationPurpose,
    ): VerificationCodeAccepted = api.requestVerificationCode(phone, purpose)

    suspend fun verifyGuardianConsent(
        childPhone: String,
        guardianPhone: String,
        verificationCode: String,
    ): GuardianConsentResponse = api.verifyGuardianConsent(
        GuardianConsentRequest(
            childPhone = childPhone,
            guardianPhone = guardianPhone,
            verificationCode = verificationCode,
            termsVersion = BuildConfig.TERMS_VERSION,
            privacyVersion = BuildConfig.PRIVACY_VERSION,
        )
    )

    suspend fun resetPassword(
        phone: String,
        verificationCode: String,
        newPassword: String,
    ): PasswordResetResponse {
        val response = api.resetPassword(
            PasswordResetRequest(phone, verificationCode, newPassword)
        )
        clearSession()
        return response
    }

    suspend fun refreshCurrentUser(): UserDto = authorized { token ->
        api.currentUser(token).also { _currentUser.value = it }
    }

    suspend fun getSettings(): UserSettingsDto = authorized(api::getSettings)

    suspend fun updateSettings(patch: UserSettingsPatchDto): UserSettingsDto =
        authorized { token -> api.updateSettings(token, patch) }

    suspend fun submitFeedback(message: String): FeedbackResponseDto =
        authorized { token -> api.submitFeedback(token, message) }

    suspend fun getBlacklist(): List<BlacklistEntryDto> =
        authorized(api::getBlacklist)

    suspend fun removeFromBlacklist(userId: String) {
        authorized<Unit> { token -> api.removeFromBlacklist(token, userId) }
    }

    suspend fun getSessions(): List<SessionDto> = authorized(api::getSessions)

    suspend fun logoutCurrent() {
        val refreshToken = tokenStore.readRefreshToken()
        try {
            if (refreshToken != null) api.logout(refreshToken)
        } finally {
            clearSession()
        }
    }

    suspend fun logoutAll() {
        try {
            authorized<Unit>(api::logoutAll)
        } finally {
            clearSession()
        }
    }

    fun clearSession() {
        accessToken = null
        _currentUser.value = null
        tokenStore.clear()
    }

    private fun saveTokens(tokens: TokenPairDto) {
        accessToken = tokens.accessToken
        tokenStore.saveRefreshToken(tokens.refreshToken)
    }

    private fun requireAccessToken(): String = accessToken ?: throw AuthApiException(
        statusCode = 401,
        code = "AUTHENTICATION_REQUIRED",
        message = "请先登录",
    )

    private suspend fun <T> authorized(block: suspend (String) -> T): T {
        var token = accessToken
        if (token == null) {
            token = refreshAccessToken()
        }
        return try {
            block(token)
        } catch (error: AuthApiException) {
            if (error.statusCode != 401) throw error
            if (accessToken == token) accessToken = null
            block(refreshAccessToken())
        }
    }

    private suspend fun refreshAccessToken(): String = refreshMutex.withLock {
        accessToken?.let { return@withLock it }
        val refreshToken = tokenStore.readRefreshToken() ?: throw AuthApiException(
            statusCode = 401,
            code = "AUTHENTICATION_REQUIRED",
            message = "请先登录",
        )
        try {
            saveTokens(api.refresh(refreshToken))
            requireAccessToken()
        } catch (error: AuthApiException) {
            if (error.statusCode == 401) clearSession()
            throw error
        }
    }
}
