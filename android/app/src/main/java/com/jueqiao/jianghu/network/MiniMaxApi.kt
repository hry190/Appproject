package com.jueqiao.jianghu.network

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jueqiao.jianghu.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object MiniMaxApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    fun chat(
        prompt: String,
        systemPrompt: String = "你是一个友好的AI助手",
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val body = mapOf(
            "model" to "MiniMax-M3",
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to prompt)
            ),
            "temperature" to 0.7,
            "max_completion_tokens" to 2048
        )
        val json = gson.toJson(body)
        val request = Request.Builder()
            .url("${BuildConfig.MINIMAX_BASE_URL}/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.MINIMAX_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val result = response.body?.string()
                val content = JsonParser.parseString(result)
                    .asJsonObject
                    .getAsJsonArray("choices")
                    .get(0).asJsonObject
                    .getAsJsonObject("message")
                    .get("content").asString
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResult(content)
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError("请求失败: ${e.message}")
                }
            }
        }.start()
    }
}
