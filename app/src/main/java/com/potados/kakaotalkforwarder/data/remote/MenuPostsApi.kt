package com.potados.kakaotalkforwarder.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import org.json.JSONObject

sealed interface ForwardResult {
    data class Success(val httpCode: Int) : ForwardResult
    data class Failure(val httpCode: Int, val message: String) : ForwardResult
    data class NetworkError(val message: String) : ForwardResult
}

class MenuPostsApi(private val client: OkHttpClient) {

    suspend fun post(url: String, bearerToken: String, menuText: String): ForwardResult {
        val body = JSONObject()
            .put("menuText", menuText)
            .put("source", SOURCE)
            .toString()
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $bearerToken")
            .post(body)
            .build()

        return runCatching {
            client.newCall(request).executeAsync().use { response ->
                val code = response.code
                if (response.isSuccessful) {
                    ForwardResult.Success(code)
                } else {
                    val preview = withContext(Dispatchers.IO) {
                        response.body?.string()?.take(ERROR_BODY_PREVIEW).orEmpty()
                    }
                    ForwardResult.Failure(code, preview.ifBlank { response.message })
                }
            }
        }.getOrElse { e ->
            ForwardResult.NetworkError(e.message ?: e::class.java.simpleName)
        }
    }

    private companion object {
        const val SOURCE = "kakaotalk"
        const val ERROR_BODY_PREVIEW = 300
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
