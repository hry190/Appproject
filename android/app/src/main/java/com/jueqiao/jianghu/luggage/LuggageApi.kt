package com.jueqiao.jianghu.luggage

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.jueqiao.jianghu.auth.ApiErrorEnvelope
import com.jueqiao.jianghu.auth.AuthApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.TimeUnit

class LuggageApi(
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

    suspend fun getLuggage(accessToken: String, etag: String?): LuggageHttpResult =
        withContext(Dispatchers.IO) {
            val builder = requestBuilder("/v1/me/luggage").authorized(accessToken).get()
            if (etag != null) builder.header("If-None-Match", etag)
            execute(builder.build()).use { response ->
                if (response.code == 304) return@withContext LuggageHttpResult.NotModified
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
                val parsed = parseJson<LuggageResponseDto>(
                    response,
                    body,
                    LuggageResponseDto::class.java,
                )
                LuggageHttpResult.Fresh(
                    body = parsed,
                    etag = response.header("ETag") ?: parsed.meta.etag,
                )
            }
        }

    suspend fun getBadges(accessToken: String): List<BadgeDto> = get(
        accessToken = accessToken,
        path = "/v1/profile/badges",
        type = TypeToken.getParameterized(List::class.java, BadgeDto::class.java).type,
    )

    suspend fun getEvidence(
        accessToken: String,
        category: String?,
        weekOnly: Boolean,
        cursor: String?,
        limit: Int = 20,
    ): EvidenceListDto = get(
        accessToken,
        "/v1/me/learning-evidence",
        EvidenceListDto::class.java,
        mapOf(
            "category" to category,
            "week_only" to weekOnly.toString(),
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getManuals(
        accessToken: String,
        volume: Int?,
        query: String?,
        state: String?,
        favoritesOnly: Boolean,
        cursor: String?,
        limit: Int = 20,
    ): ManualPageListDto = get(
        accessToken,
        "/v1/manuals",
        ManualPageListDto::class.java,
        mapOf(
            "volume" to volume?.toString(),
            "q" to query?.takeIf { it.isNotBlank() },
            "state" to state,
            "favorites_only" to favoritesOnly.toString(),
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun setManualFavorite(
        accessToken: String,
        manualId: String,
        favorite: Boolean,
    ) = withContext(Dispatchers.IO) {
        val builder = requestBuilder("/v1/manuals/$manualId/favorite")
            .authorized(accessToken)
        val request = if (favorite) {
            builder.put(ByteArray(0).toRequestBody(null)).build()
        } else {
            builder.delete().build()
        }
        execute(request).use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw parseApiError(response.code, body)
        }
    }

    suspend fun getManual(accessToken: String, manualId: String): ManualDetailDto = get(
        accessToken,
        "/v1/manuals/$manualId",
        ManualDetailDto::class.java,
    )

    suspend fun getManualEvidence(accessToken: String, manualId: String): List<EvidenceItemDto> = get(
        accessToken = accessToken,
        path = "/v1/manuals/$manualId/evidence",
        type = TypeToken.getParameterized(List::class.java, EvidenceItemDto::class.java).type,
    )

    suspend fun getManualLearningHistory(
        accessToken: String,
        manualId: String,
    ): ManualLearningHistoryDto = get(
        accessToken,
        "/v1/manuals/$manualId/learning-history",
        ManualLearningHistoryDto::class.java,
    )

    suspend fun getMistakes(
        accessToken: String,
        status: String?,
        cursor: String?,
        limit: Int = 20,
    ): MistakeListDto = get(
        accessToken,
        "/v1/mistakes",
        MistakeListDto::class.java,
        mapOf(
            "status" to status,
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getMistake(accessToken: String, mistakeId: String): MistakeDetailDto = get(
        accessToken,
        "/v1/mistakes/$mistakeId",
        MistakeDetailDto::class.java,
    )

    suspend fun createRetrySession(
        accessToken: String,
        mistakeId: String,
    ): RetrySessionDto = requestJson(
        requestBuilder("/v1/mistakes/$mistakeId/retry-sessions")
            .authorized(accessToken)
            .post(ByteArray(0).toRequestBody(null))
            .build(),
        RetrySessionDto::class.java,
    )

    suspend fun getTrial(accessToken: String, trialId: String): TrialDto = get(
        accessToken,
        "/v1/trials/$trialId",
        TrialDto::class.java,
    )

    suspend fun submitRetryAttempt(
        accessToken: String,
        trialId: String,
        request: TrialAttemptRequestDto,
    ): TrialAttemptResultDto = requestJson(
        requestBuilder("/v1/trials/$trialId/attempts")
            .authorized(accessToken)
            .header("Idempotency-Key", "android-retry-${UUID.randomUUID()}")
            .post(gson.toJson(request).toRequestBody(jsonMediaType))
            .build(),
        TrialAttemptResultDto::class.java,
    )

    suspend fun getCreations(
        accessToken: String,
        status: String?,
        cursor: String?,
        limit: Int = 20,
    ): CreationProjectListDto = get(
        accessToken,
        "/v1/me/creation-projects",
        CreationProjectListDto::class.java,
        mapOf(
            "status" to status,
            "cursor" to cursor,
            "limit" to limit.toString(),
        ),
    )

    suspend fun getCreationProject(
        accessToken: String,
        projectId: String,
    ): CreationProjectDto = get(
        accessToken,
        "/v1/creation-projects/$projectId",
        CreationProjectDto::class.java,
    )

    suspend fun deleteCreationProject(accessToken: String, projectId: String) =
        withContext(Dispatchers.IO) {
            execute(
                requestBuilder("/v1/creation-projects/$projectId")
                    .authorized(accessToken)
                    .delete()
                    .build()
            ).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
            }
        }

    suspend fun getCreationVersions(
        accessToken: String,
        projectId: String,
    ): CreationVersionListDto = get(
        accessToken,
        "/v1/creation-projects/$projectId/versions",
        CreationVersionListDto::class.java,
    )

    suspend fun createCreationVersion(
        accessToken: String,
        projectId: String,
        payload: CreationVersionCreateDto,
    ): CreationVersionDto = requestJson(
        requestBuilder("/v1/creation-projects/$projectId/versions")
            .authorized(accessToken)
            .post(gson.toJson(payload).toRequestBody(jsonMediaType))
            .build(),
        CreationVersionDto::class.java,
    )

    suspend fun getLearningCard(
        accessToken: String,
        versionId: String,
    ): LearningCardDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/learning-card",
        LearningCardDto::class.java,
    )

    suspend fun getProvenance(
        accessToken: String,
        versionId: String,
    ): ProvenanceManifestDto = get(
        accessToken,
        "/v1/creation-versions/$versionId/provenance-manifest",
        ProvenanceManifestDto::class.java,
    )

    suspend fun getModerationCase(
        accessToken: String,
        publicationId: String,
    ): ModerationCaseDto = get(
        accessToken,
        "/v1/publications/$publicationId/moderation-case",
        ModerationCaseDto::class.java,
    )

    suspend fun withdrawPublication(
        accessToken: String,
        publicationId: String,
        rowVersion: Int,
    ): ModerationCaseDto = requestJson(
        requestBuilder("/v1/publications/$publicationId/withdraw")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("row_version" to rowVersion)).toRequestBody(jsonMediaType))
            .build(),
        ModerationCaseDto::class.java,
    )

    suspend fun createAppeal(
        accessToken: String,
        caseId: String,
        reason: String,
    ): ModerationAppealDto = requestJson(
        requestBuilder("/v1/moderation-cases/$caseId/appeals")
            .authorized(accessToken)
            .post(gson.toJson(mapOf("reason" to reason)).toRequestBody(jsonMediaType))
            .build(),
        ModerationAppealDto::class.java,
    )

    suspend fun getPrivacy(accessToken: String): PrivacySettingsDto = get(
        accessToken,
        "/v1/me/privacy-settings",
        PrivacySettingsDto::class.java,
    )

    suspend fun updatePrivacy(
        accessToken: String,
        patch: PrivacySettingsPatchDto,
    ): PrivacySettingsDto = requestJson(
        requestBuilder("/v1/me/privacy-settings")
            .authorized(accessToken)
            .patch(gson.toJson(patch).toRequestBody(jsonMediaType))
            .build(),
        PrivacySettingsDto::class.java,
    )

    suspend fun createDataRightsRequest(
        accessToken: String,
        requestType: String,
        reason: String,
    ): DataRightsRequestDto = requestJson(
        requestBuilder("/v1/account/data-rights-requests")
            .authorized(accessToken)
            .post(
                gson.toJson(mapOf("request_type" to requestType, "reason" to reason))
                    .toRequestBody(jsonMediaType)
            )
            .build(),
        DataRightsRequestDto::class.java,
    )

    suspend fun getAccountExport(accessToken: String): JsonObject = get(
        accessToken,
        "/v1/account/export",
        JsonObject::class.java,
    )

    private suspend fun <T : Any> get(
        accessToken: String,
        path: String,
        type: Type,
        params: Map<String, String?> = emptyMap(),
    ): T = requestJson(
        requestBuilder(path, params).authorized(accessToken).get().build(),
        type,
    )

    private suspend fun <T : Any> requestJson(request: Request, type: Type): T =
        withContext(Dispatchers.IO) {
            execute(request).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw parseApiError(response.code, body)
                parseJson(response, body, type)
            }
        }

    private fun requestBuilder(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): Request.Builder {
        val url = (root + path).toHttpUrl().newBuilder().apply {
            params.forEach { (name, value) ->
                if (value != null) addQueryParameter(name, value)
            }
        }.build()
        return Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-Request-ID", UUID.randomUUID().toString())
    }

    private fun Request.Builder.authorized(accessToken: String): Request.Builder =
        header("Authorization", "Bearer $accessToken")

    private fun execute(request: Request): Response = try {
        client.newCall(request).execute()
    } catch (_: IOException) {
        throw AuthApiException(
            statusCode = 0,
            code = "NETWORK_UNAVAILABLE",
            message = "暂时无法连接江湖驿站，请检查网络后重试",
        )
    }

    private fun <T : Any> parseJson(response: Response, body: String, type: Type): T = try {
        @Suppress("UNCHECKED_CAST")
        (gson.fromJson<Any>(body, type) as T?) ?: error("Empty response body")
    } catch (_: RuntimeException) {
        throw AuthApiException(
            statusCode = response.code,
            code = "INVALID_SERVER_RESPONSE",
            message = "驿站返回异常，请稍后重试",
            requestId = response.header("X-Request-ID"),
        )
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
