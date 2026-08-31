package com.jueqiao.jianghu.auth

import com.jueqiao.jianghu.BuildConfig
import java.util.UUID

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: EncryptedTokenStore,
) {
    @Volatile
    private var accessToken: String? = null

    fun hasStoredSession(): Boolean = tokenStore.readRefreshToken() != null

    suspend fun restoreSession(): Boolean {
        val refreshToken = tokenStore.readRefreshToken() ?: return false
        return try {
            saveTokens(api.refresh(refreshToken))
            true
        } catch (error: AuthApiException) {
            if (error.statusCode == 401) clearSession()
            false
        }
    }

    suspend fun login(phone: String, password: String): AuthResponseDto {
        return api.login(phone, password).also { saveTokens(it.tokens) }
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

    fun clearSession() {
        accessToken = null
        tokenStore.clear()
    }

    private fun saveTokens(tokens: TokenPairDto) {
        accessToken = tokens.accessToken
        tokenStore.saveRefreshToken(tokens.refreshToken)
    }
}
