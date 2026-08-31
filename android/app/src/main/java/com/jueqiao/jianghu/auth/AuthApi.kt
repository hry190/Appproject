package com.jueqiao.jianghu.auth

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
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

    private suspend fun <T : Any> post(
        path: String,
        payload: Any,
        responseType: Class<T>,
    ): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(root + path)
            .header("Accept", "application/json")
            .header("X-Request-ID", UUID.randomUUID().toString())
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build()
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
                gson.fromJson(body, responseType) ?: throw IllegalStateException(
                    "Empty response body"
                )
            } catch (error: RuntimeException) {
                throw AuthApiException(
                    statusCode = it.code,
                    code = "INVALID_SERVER_RESPONSE",
                    message = "驿站返回异常，请稍后重试",
                    requestId = it.header("X-Request-ID"),
                )
            }
        }
    }

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
