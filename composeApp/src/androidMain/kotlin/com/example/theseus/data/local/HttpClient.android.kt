package com.example.theseus.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class AndroidHttpClient : NativeHttpClient {
    override suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>
    ): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")

                headers.forEach { (name, value) ->
                    setRequestProperty(name, value)
                }
            }

            connection.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val status = connection.responseCode
            if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("HTTP $status: $errorMsg")
            }
        } finally {
            connection.disconnect() // Essential for resource cleanup
        }
    }
}
actual fun getNativeHttpClient(): NativeHttpClient = AndroidHttpClient()

