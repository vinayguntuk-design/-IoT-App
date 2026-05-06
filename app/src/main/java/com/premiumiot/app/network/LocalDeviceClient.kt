package com.premiumiot.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class LocalDeviceClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(900, TimeUnit.MILLISECONDS)
        .readTimeout(1200, TimeUnit.MILLISECONDS)
        .build()

    suspend fun isReachable(host: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("http://$host/health")
                .get()
                .build()
            http.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    suspend fun publish(host: String, payload: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val body = payload.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$host/control")
                .post(body)
                .build()
            http.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }
}
