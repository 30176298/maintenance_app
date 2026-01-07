package com.example.theseus.data.local

interface NativeHttpClient {
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): String
}

expect fun getNativeHttpClient(): NativeHttpClient