package com.example.theseus.data.remote

import com.example.theseus.data.local.NativeHttpClient
import com.example.theseus.data.remote.dto.AircraftDto
import com.example.theseus.data.remote.dto.MaintenanceEventDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TheseusApiClient(
    private val httpClient: NativeHttpClient,
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
    }

    private val defaultHeaders = mapOf(
        "X-API-Key" to apiKey
    )

    // Aircraft endpoints
    suspend fun listAircraft(): Result<List<AircraftDto>> = safeApiCall {
        val response = httpClient.get("/api/v1/aircraft", defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<List<AircraftDto>>(response.body)
    }

    suspend fun getAircraft(id: String): Result<AircraftDto> = safeApiCall {
        val response = httpClient.get("/api/v1/aircraft/$id", defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<AircraftDto>(response.body)
    }

    suspend fun createAircraft(aircraft: AircraftDto): Result<AircraftDto> = safeApiCall {
        val body = json.encodeToString(aircraft)
        val response = httpClient.post("/api/v1/aircraft", body, defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<AircraftDto>(response.body)
    }

    suspend fun updateAircraft(id: String, aircraft: AircraftDto): Result<AircraftDto> = safeApiCall {
        val body = json.encodeToString(aircraft)
        val response = httpClient.put("/api/v1/aircraft/$id", body, defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<AircraftDto>(response.body)
    }

    suspend fun deleteAircraft(id: String): Result<Unit> = safeApiCall {
        val response = httpClient.delete("/api/v1/aircraft/$id", defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
    }

    // Maintenance Event endpoints
    suspend fun listMaintenanceEvents(aircraftId: String? = null): Result<List<MaintenanceEventDto>> = safeApiCall {
        val url = if (aircraftId != null) {
            "/api/v1/events?aircraftId=$aircraftId"
        } else {
            "/api/v1/events"
        }
        val response = httpClient.get(url, defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<List<MaintenanceEventDto>>(response.body)
    }

    suspend fun getMaintenanceEvent(id: String): Result<MaintenanceEventDto> = safeApiCall {
        val response = httpClient.get("/api/v1/events/$id", defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<MaintenanceEventDto>(response.body)
    }

    suspend fun createMaintenanceEvent(event: MaintenanceEventDto): Result<MaintenanceEventDto> = safeApiCall {
        val body = json.encodeToString(event)
        val response = httpClient.post("/api/v1/events", body, defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<MaintenanceEventDto>(response.body)
    }

    suspend fun updateMaintenanceEvent(id: String, event: MaintenanceEventDto): Result<MaintenanceEventDto> = safeApiCall {
        val body = json.encodeToString(event)
        val response = httpClient.put("/api/v1/events/$id", body, defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
        json.decodeFromString<MaintenanceEventDto>(response.body)
    }

    suspend fun deleteMaintenanceEvent(id: String): Result<Unit> = safeApiCall {
        val response = httpClient.delete("/api/v1/events/$id", defaultHeaders)
        if (!response.isSuccess) {
            throw ApiException(response.statusCode, response.body)
        }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException(0, "Network error: ${e.message}"))
        }
    }
}

class ApiException(val statusCode: Int, message: String) : Exception(message)