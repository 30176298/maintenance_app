package com.example.theseus.data.repository

import com.example.theseus.database.TheseusDatabase
import com.example.theseus.domain.model.MaintenanceEvent
import com.example.theseus.domain.model.MaintenanceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
class MaintenanceRepository(private val database: TheseusDatabase) {

    fun getAllMaintenanceEvents(): Flow<List<MaintenanceEvent>> {
        return database.theseusQueries.selectAllMaintenanceEvents()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    fun getMaintenanceEventsByAircraftId(aircraftId: String): Flow<List<MaintenanceEvent>> {
        return database.theseusQueries.selectMaintenanceEventsByAircraftId(aircraftId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun getMaintenanceEventById(id: String): MaintenanceEvent? {
        return database.theseusQueries.selectMaintenanceEventById(id)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    suspend fun insertMaintenanceEvent(event: MaintenanceEvent) {
        database.theseusQueries.insertMaintenanceEvent(
            id = event.id,
            aircraftId = event.aircraftId,
            date = event.date.toEpochMilliseconds(),
            hoursAtMaintenance = event.hoursAtMaintenance,
            cyclesAtMaintenance = event.cyclesAtMaintenance.toLong(),
            maintenanceType = event.maintenanceType.name,
            description = event.description,
            technicianName = event.technicianName,
            technicianCertification = event.technicianCertification,
            createdAt = event.createdAt.toEpochMilliseconds(),
            updatedAt = event.updatedAt.toEpochMilliseconds()
        )
    }

    suspend fun updateMaintenanceEvent(event: MaintenanceEvent) {
        database.theseusQueries.updateMaintenanceEvent(
            date = event.date.toEpochMilliseconds(),
            hoursAtMaintenance = event.hoursAtMaintenance,
            cyclesAtMaintenance = event.cyclesAtMaintenance.toLong(),
            maintenanceType = event.maintenanceType.name,
            description = event.description,
            technicianName = event.technicianName,
            technicianCertification = event.technicianCertification,
            updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            id = event.id
        )
    }

    suspend fun deleteMaintenanceEvent(id: String) {
        database.theseusQueries.deleteMaintenanceEvent(id)
    }

    suspend fun getLatestMaintenanceByAircraftId(aircraftId: String): MaintenanceEvent? {
        return database.theseusQueries.selectLatestMaintenanceByAircraftId(aircraftId)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    suspend fun getMaintenanceCountByAircraftId(aircraftId: String): Long {
        return database.theseusQueries.countMaintenanceEventsByAircraftId(aircraftId)
            .executeAsOne()
    }

    private fun com.example.theseus.database.MaintenanceEvent.toDomain(): MaintenanceEvent {
        return MaintenanceEvent(
            id = id,
            aircraftId = aircraftId,
            date = Instant.fromEpochMilliseconds(date),
            hoursAtMaintenance = hoursAtMaintenance,
            cyclesAtMaintenance = cyclesAtMaintenance.toInt(),
            maintenanceType = MaintenanceType.valueOf(maintenanceType),
            description = description,
            technicianName = technicianName,
            technicianCertification = technicianCertification,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }
}
