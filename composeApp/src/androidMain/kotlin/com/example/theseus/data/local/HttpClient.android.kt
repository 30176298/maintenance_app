package com.example.theseus.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class AndroidHttpClient : NativeHttpClient {
    private val baseUrl = "http://10.0.2.2:5000"

    override suspend fun get(
        url: String,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("GET", url, null, headers)
    }

    override suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("POST", url, body, headers)
    }

    override suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("PUT", url, body, headers)
    }

    override suspend fun delete(
        url: String,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest("DELETE", url, null, headers)
    }

    private fun executeRequest(
        method: String,
        url: String,
        body: String?,
        headers: Map<String, String>
    ): HttpResponse {
        val fullURL = "$baseUrl$url"
        val connection = URL(fullURL).openConnection() as HttpURLConnection

        try {
            connection.apply {
                requestMethod = method
                setRequestProperty("Content-Type", "application/json")

                headers.forEach { (name, value) ->
                    setRequestProperty(name, value)
                }

                if (body != null && (method == "POST" || method == "PUT")) {
                    doOutput = true
                    outputStream.use { os ->
                        os.write(body.toByteArray(Charsets.UTF_8))
                        os.flush()
                    }
                }
            }

            val statusCode = connection.responseCode
            val responseBody = if (statusCode in 200..299) {
                connection.inputStream?.bufferedReader()?.use { it.readText() } ?: ""
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            return HttpResponse(
                statusCode = statusCode,
                body = responseBody,
                isSuccess = statusCode in 200..299
            )
        } finally {
            connection.disconnect()
        }
    }
}

actual fun getNativeHttpClient(): NativeHttpClient = AndroidHttpClient()


