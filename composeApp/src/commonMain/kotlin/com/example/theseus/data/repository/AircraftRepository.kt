package com.example.theseus.data.repository

import com.example.theseus.database.TheseusDatabase
import com.example.theseus.domain.model.Aircraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
class AircraftRepository(private val database: TheseusDatabase) {

    fun getAllAircraft(): Flow<List<Aircraft>> {
        return database.theseusQueries.selectAllAircraft()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun getAircraftById(id: String): Aircraft? {
        return database.theseusQueries.selectAircraftById(id)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    suspend fun insertAircraft(aircraft: Aircraft) {
        database.theseusQueries.insertAircraft(
            id = aircraft.id,
            registration = aircraft.registration,
            make = aircraft.make,
            model = aircraft.model,
            serialNumber = aircraft.serialNumber,
            yearOfManufacture = aircraft.yearOfManufacture?.toLong(),
            totalHours = aircraft.totalHours,
            totalCycles = aircraft.totalCycles.toLong(),
            createdAt = aircraft.createdAt.toEpochMilliseconds(),
            updatedAt = aircraft.updatedAt.toEpochMilliseconds()
        )
    }

    suspend fun updateAircraft(aircraft: Aircraft) {
        database.theseusQueries.updateAircraft(
            registration = aircraft.registration,
            make = aircraft.make,
            model = aircraft.model,
            serialNumber = aircraft.serialNumber,
            yearOfManufacture = aircraft.yearOfManufacture?.toLong(),
            totalHours = aircraft.totalHours,
            totalCycles = aircraft.totalCycles.toLong(),
            updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            id = aircraft.id
        )
    }

    suspend fun deleteAircraft(id: String) {
        database.theseusQueries.deleteAircraft(id)
    }

    private fun com.example.theseus.database.Aircraft.toDomain(): Aircraft {
        return Aircraft(
            id = id,
            registration = registration,
            make = make,
            model = model,
            serialNumber = serialNumber,
            yearOfManufacture = yearOfManufacture?.toInt(),
            totalHours = totalHours,
            totalCycles = totalCycles.toInt(),
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }
}