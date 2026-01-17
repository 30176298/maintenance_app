package com.example.theseus.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class JvmHttpClient : NativeHttpClient {
    private val baseUrl = "http://127.0.0.1:5000"

    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    override suspend fun get(
        url: String,
        headers: Map<String, String>
    ): com.example.theseus.data.local.HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("GET", url, null, headers)
    }

    override suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>
    ): com.example.theseus.data.local.HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("POST", url, body, headers)
    }

    override suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String>
    ): com.example.theseus.data.local.HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("PUT", url, body, headers)
    }

    override suspend fun delete(
        url: String,
        headers: Map<String, String>
    ): com.example.theseus.data.local.HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("DELETE", url, null, headers)
    }

    private fun executeRequest(
        method: String,
        url: String,
        body: String?,
        headers: Map<String, String>
    ): com.example.theseus.data.local.HttpResponse {
        val fullURL = "$baseUrl$url"
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(fullURL))
            .header("Content-Type", "application/json")

        headers.forEach { (name, value) ->
            builder.header(name, value)
        }

        when (method) {
            "GET" -> builder.GET()
            "POST" -> builder.POST(
                body?.let { HttpRequest.BodyPublishers.ofString(it) }
                    ?: HttpRequest.BodyPublishers.noBody()
            )
            "PUT" -> builder.PUT(
                body?.let { HttpRequest.BodyPublishers.ofString(it) }
                    ?: HttpRequest.BodyPublishers.noBody()
            )
            "DELETE" -> builder.DELETE()
        }

        val request = builder.build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return com.example.theseus.data.local.HttpResponse(
            statusCode = response.statusCode(),
            body = response.body(),
            isSuccess = response.statusCode() in 200..299
        )
    }
}

actual fun getNativeHttpClient(): NativeHttpClient = JvmHttpClient()