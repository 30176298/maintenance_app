package com.example.theseus.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class JvmNetworkClient : NativeHttpClient {
    // Reuse the client instance as recommended by JDK docs
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    override suspend fun post(url: String, body: String, headers: Map<String, String>): String = withContext(Dispatchers.IO) {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json") // Standard header
            .POST(HttpRequest.BodyPublishers.ofString(body))

        headers.forEach { (name, value) ->
            builder.header(name, value)
        }

        val request = builder.build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.body()

        if (response.statusCode() in 200..299) {
            response.body()
        } else {
            throw Exception("HTTP Error: ${response.statusCode()}")
        }
    }
}

actual fun getNativeHttpClient(): NativeHttpClient = JvmNetworkClient()