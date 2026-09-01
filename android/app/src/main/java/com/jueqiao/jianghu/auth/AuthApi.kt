package com.jueqiao.jianghu.auth

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthApi(
    baseUrl: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build(),
    private val gson: Gson = Gson(),
) {
    private val root = baseUrl.trimEnd('/')
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun requestVerificationCode(
        phone: String,
        purpose: VerificationPurpose,
    ): VerificationCodeAccepted = post(
        path = "/v1/auth/verification-codes",
        payload = VerificationCodeRequest(
            phone = phone,
            purpose = purpose.name,
            clientRequestId = UUID.randomUUID().toString(),
        ),
        responseType = VerificationCodeAccepted::class.java,
    )

    suspend fun login(phone: String, password: String): AuthResponseDto = post(
        path = "/v1/auth/login/password",
        payload = PasswordLoginRequest(phone = phone, password = password),
        responseType = AuthResponseDto::class.java,
    )

    suspend fun register(request: RegisterRequest): AuthResponseDto = post(
        path = "/v1/auth/register",
        payload = request,
        responseType = AuthResponseDto::class.java,
    )

    suspend fun verifyGuardianConsent(
        request: GuardianConsentRequest,
    ): GuardianConsentResponse = post(
        path = "/v1/auth/guardian-consents/verify",
        payload = request,
        responseType = GuardianConsentResponse::class.java,
    )

    suspend fun resetPassword(request: PasswordResetRequest): PasswordResetResponse = post(
        path = "/v1/auth/password/reset",
        payload = request,
        responseType = PasswordResetResponse::class.java,
    )

    suspend fun refresh(refreshToken: String): TokenPairDto = post(
        path = "/v1/auth/token/refresh",
        payload = RefreshRequest(refreshToken),
        responseType = TokenPairDto::class.java,
    )

    suspend fun currentUser(accessToken: String): UserDto = get(
        path = "/v1/auth/me",
        accessToken = accessToken,
        responseType = UserDto::class.java,
    )

    suspend fun logout(refreshToken: String) {
        requestNoContent(
            Request.Builder()
                .apiUrl("/v1/auth/logout")
                .post(gson.toJson(LogoutRequest(refreshToken)).toRequestBody(jsonMediaType))
                .build()
        )
    }

    suspend fun logoutAll(accessToken: String) {
        requestNoContentOrJson(
            Request.Builder()
                .apiUrl("/v1/auth/logout-all")
                .authorized(accessToken)
                .post(ByteArray(0).toRequestBody(null))
                .build()
        )
    }

    suspend fun getSettings(accessToken: String): UserSettingsDto = get(
        path = "/v1/settings/preferences",
        accessToken = accessToken,
        responseType = UserSettingsDto::class.java,
    )

    suspend fun updateSettings(
        accessToken: String,
        patch: UserSettingsPatchDto,
    ): UserSettingsDto = requestJson(
        request = Request.Builder()
            .apiUrl("/v1/settings/preferences")
            .authorized(accessToken)
            .patch(gson.toJson(patch).toRequestBody(jsonMediaType))
            .build(),
        responseType = UserSettingsDto::class.java,
    )

    suspend fun submitFeedback(
        accessToken: String,
        message: String,
    ): FeedbackResponseDto = requestJson(
        request = Request.Builder()
            .apiUrl("/v1/support/feedback")
            .authorized(accessToken)
            .post(gson.toJson(FeedbackRequestDto(message = message)).toRequestBody(jsonMediaType))
            .build(),
        responseType = FeedbackResponseDto::class.java,
    )

    suspend fun getBlacklist(accessToken: String): List<BlacklistEntryDto> = get(
        path = "/v1/settings/blacklist",
        accessToken = accessToken,
        responseType = TypeToken.getParameterized(
            List::class.java,
            BlacklistEntryDto::class.java,
        ).type,
    )

    suspend fun removeFromBlacklist(accessToken: String, userId: String) {
        requestNoContent(
            Request.Builder()
                .apiUrl("/v1/settings/blacklist/$userId")
                .authorized(accessToken)
                .delete()
                .build()
        )
    }

    suspend fun getSessions(accessToken: String): List<SessionDto> = get(
        path = "/v1/account/sessions",
        accessToken = accessToken,
        responseType = TypeToken.getParameterized(
            List::class.java,
            SessionDto::class.java,
        ).type,
    )

    private suspend fun <T : Any> post(
        path: String,
        payload: Any,
        responseType: Class<T>,
    ): T = requestJson(
        request = Request.Builder()
            .apiUrl(path)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        responseType = responseType,
    )

    private suspend fun <T : Any> get(
        path: String,
        accessToken: String,
        responseType: Type,
    ): T = requestJson(
        request = Request.Builder()
            .apiUrl(path)
            .authorized(accessToken)
            .get()
            .build(),
        responseType = responseType,
    )

    private suspend fun <T : Any> requestJson(
        request: Request,
        responseType: Type,
    ): T = withContext(Dispatchers.IO) {
        val response = try {
            client.newCall(request).execute()
        } catch (error: IOException) {
            throw AuthApiException(
                statusCode = 0,
                code = "NETWORK_UNAVAILABLE",
                message = "暂时无法连接江湖驿站，请检查网络后重试",
            )
        }
        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) throw parseApiError(it.code, body)
            try {
                @Suppress("UNCHECKED_CAST")
                val parsed = gson.fromJson<Any>(body, responseType) as T?
                parsed ?: throw IllegalStateException("Empty response body")
            } catch (error: RuntimeException) {
                Log.e("AuthApi", "Unable to parse successful response for ${request.url.encodedPath}", error)
                throw AuthApiException(
                    statusCode = it.code,
                    code = "INVALID_SERVER_RESPONSE",
                    message = "驿站返回异常，请稍后重试",
                    requestId = it.header("X-Request-ID"),
                )
            }
        }
    }

    private suspend fun requestNoContent(request: Request) = withContext(Dispatchers.IO) {
        val response = execute(request)
        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) throw parseApiError(it.code, body)
        }
    }

    private suspend fun requestNoContentOrJson(request: Request) = withContext(Dispatchers.IO) {
        val response = execute(request)
        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) throw parseApiError(it.code, body)
        }
    }

    private fun execute(request: Request) = try {
        client.newCall(request).execute()
    } catch (error: IOException) {
        throw AuthApiException(
            statusCode = 0,
            code = "NETWORK_UNAVAILABLE",
            message = "暂时无法连接江湖驿站，请检查网络后重试",
        )
    }

    private fun Request.Builder.apiUrl(path: String): Request.Builder =
        url(root + path)
            .header("Accept", "application/json")
            .header("X-Request-ID", UUID.randomUUID().toString())

    private fun Request.Builder.authorized(accessToken: String): Request.Builder =
        header("Authorization", "Bearer $accessToken")

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
