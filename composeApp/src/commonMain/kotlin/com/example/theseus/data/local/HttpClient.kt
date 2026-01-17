package com.example.theseus.data.local

interface NativeHttpClient {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
}

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val isSuccess: Boolean = statusCode in 200..299
)
expect fun getNativeHttpClient(): NativeHttpClient