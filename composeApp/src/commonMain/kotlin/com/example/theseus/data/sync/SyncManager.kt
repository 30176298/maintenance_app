package com.example.theseus.data.sync

import com.example.theseus.data.remote.TheseusApiClient
import com.example.theseus.data.remote.dto.AircraftDto
import com.example.theseus.data.remote.dto.MaintenanceEventDto
import com.example.theseus.database.TheseusDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class SyncManager(
    private val database: TheseusDatabase,
    private val apiClient: TheseusApiClient
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    //Perform full sync
    suspend fun sync(): SyncResult {
        if (_syncState.value is SyncState.Syncing) {
            return SyncResult.AlreadySyncing
        }

        _syncState.value = SyncState.Syncing(0)
        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            //Push local
            val pushResult = pushLocalChanges()
            if (pushResult !is PushResult.Success) {
                _syncState.value = SyncState.Error("Failed to push local changes")
                return SyncResult.Failed("Push failed: ${(pushResult as? PushResult.Failed)?.message}")
            }

            //Pull remote
            val pullResult = pullRemoteChanges()
            if (pullResult !is PullResult.Success) {
                _syncState.value = SyncState.Error("Failed to pull remote changes")
                return SyncResult.Failed("Pull failed: ${(pullResult as? PullResult.Failed)?.message}")
            }

            //Update status
            val endTime = Clock.System.now().toEpochMilliseconds()
            database.theseusQueries.upsertSyncStatus(
                key = "last_sync",
                lastSuccessfulSync = endTime,
                lastAttemptedSync = endTime,
                isPending = 0
            )

            _lastSyncTime.value = endTime
            _syncState.value = SyncState.Success(endTime)

            SyncResult.Success(
                pushedAircraft = pushResult.aircraftCount,
                pushedEvents = pushResult.eventsCount,
                pulledAircraft = pullResult.aircraftCount,
                pulledEvents = pullResult.eventsCount
            )
        } catch (e: Exception) {
            val endTime = Clock.System.now().toEpochMilliseconds()
            database.theseusQueries.upsertSyncStatus(
                key = "last_sync",
                lastSuccessfulSync = _lastSyncTime.value,
                lastAttemptedSync = endTime,
                isPending = 1
            )

            _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            SyncResult.Failed(e.message ?: "Unknown error")
        }
    }

    private suspend fun pushLocalChanges(): PushResult {
        var aircraftCount = 0
        var eventsCount = 0

        try {

            val dirtyAircraft = database.theseusQueries.selectDirtyAircraft().executeAsList()
            for (dbAircraft in dirtyAircraft) {
                val dto = AircraftDto(
                    id = dbAircraft.id,
                    registration = dbAircraft.registration,
                    make = dbAircraft.make,
                    model = dbAircraft.model,
                    serialNumber = dbAircraft.serialNumber,
                    yearOfManufacture = dbAircraft.yearOfManufacture?.toInt(),
                    totalHours = dbAircraft.totalHours,
                    totalCycles = dbAircraft.totalCycles.toInt(),
                    createdAt = dbAircraft.createdAt,
                    updatedAt = dbAircraft.updatedAt
                )

                val result = if (dbAircraft.lastSyncedAt == null) {
                    apiClient.createAircraft(dto)
                } else {
                    apiClient.updateAircraft(dbAircraft.id, dto)
                }

                result.onSuccess {
                    database.theseusQueries.markAircraftSynced(
                        lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                        id = dbAircraft.id
                    )
                    aircraftCount++
                }.onFailure {
                    return PushResult.Failed("Failed to sync aircraft ${dbAircraft.registration}: ${it.message}")
                }
            }


            val deletedAircraft = database.theseusQueries.selectDeletedAircraft().executeAsList()
            for (dbAircraft in deletedAircraft) {
                apiClient.deleteAircraft(dbAircraft.id).onSuccess {
                    database.theseusQueries.deleteAircraft(dbAircraft.id)
                }
            }


            val dirtyEvents = database.theseusQueries.selectDirtyMaintenanceEvents().executeAsList()
            for (dbEvent in dirtyEvents) {
                val dto = MaintenanceEventDto(
                    id = dbEvent.id,
                    aircraftId = dbEvent.aircraftId,
                    date = dbEvent.date,
                    hoursAtMaintenance = dbEvent.hoursAtMaintenance,
                    cyclesAtMaintenance = dbEvent.cyclesAtMaintenance.toInt(),
                    maintenanceType = dbEvent.maintenanceType,
                    description = dbEvent.description,
                    technicianName = dbEvent.technicianName,
                    technicianCertification = dbEvent.technicianCertification,
                    createdAt = dbEvent.createdAt,
                    updatedAt = dbEvent.updatedAt
                )

                val result = if (dbEvent.lastSyncedAt == null) {
                    apiClient.createMaintenanceEvent(dto)
                } else {
                    apiClient.updateMaintenanceEvent(dbEvent.id, dto)
                }

                result.onSuccess {
                    database.theseusQueries.markMaintenanceEventSynced(
                        lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                        id = dbEvent.id
                    )
                    eventsCount++
                }.onFailure {
                    return PushResult.Failed("Failed to sync maintenance event: ${it.message}")
                }
            }


            val deletedEvents = database.theseusQueries.selectDeletedMaintenanceEvents().executeAsList()
            for (dbEvent in deletedEvents) {
                apiClient.deleteMaintenanceEvent(dbEvent.id).onSuccess {
                    database.theseusQueries.deleteMaintenanceEvent(dbEvent.id)
                }
            }

            return PushResult.Success(aircraftCount, eventsCount)
        } catch (e: Exception) {
            return PushResult.Failed(e.message ?: "Unknown push error")
        }
    }

    private suspend fun pullRemoteChanges(): PullResult {
        var aircraftCount = 0
        var eventsCount = 0

        try {

            val aircraftResult = apiClient.listAircraft()
            aircraftResult.onSuccess { remoteAircraft ->
                for (dto in remoteAircraft) {
                    val existing = database.theseusQueries.selectAircraftById(dto.id).executeAsOneOrNull()

                    if (existing == null) {

                        database.theseusQueries.insertAircraft(
                            id = dto.id,
                            registration = dto.registration,
                            make = dto.make,
                            model = dto.model,
                            serialNumber = dto.serialNumber,
                            yearOfManufacture = dto.yearOfManufacture?.toLong(),
                            totalHours = dto.totalHours,
                            totalCycles = dto.totalCycles.toLong(),
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            isDirty = 0
                        )
                        database.theseusQueries.markAircraftSynced(
                            lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                            id = dto.id
                        )
                        aircraftCount++
                    } else if ((existing.isDirty == 0L) && (dto.updatedAt > existing.updatedAt)) {
                        // Server has newer version  - only update if local is clean
                        database.theseusQueries.updateAircraft(
                            registration = dto.registration,
                            make = dto.make,
                            model = dto.model,
                            serialNumber = dto.serialNumber,
                            yearOfManufacture = dto.yearOfManufacture?.toLong(),
                            totalHours = dto.totalHours,
                            totalCycles = dto.totalCycles.toLong(),
                            updatedAt = dto.updatedAt,
                            id = dto.id
                        )
                        database.theseusQueries.markAircraftSynced(
                            lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                            id = dto.id
                        )
                        aircraftCount++
                    }
                }
            }.onFailure {
                return PullResult.Failed("Failed to fetch aircraft: ${it.message}")
            }


            val eventsResult = apiClient.listMaintenanceEvents()
            eventsResult.onSuccess { remoteEvents ->
                for (dto in remoteEvents) {
                    val existing = database.theseusQueries.selectMaintenanceEventById(dto.id).executeAsOneOrNull()

                    if (existing == null) {

                        database.theseusQueries.insertMaintenanceEvent(
                            id = dto.id,
                            aircraftId = dto.aircraftId,
                            date = dto.date,
                            hoursAtMaintenance = dto.hoursAtMaintenance,
                            cyclesAtMaintenance = dto.cyclesAtMaintenance.toLong(),
                            maintenanceType = dto.maintenanceType,
                            description = dto.description,
                            technicianName = dto.technicianName,
                            technicianCertification = dto.technicianCertification,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            isDirty = 0
                        )
                        database.theseusQueries.markMaintenanceEventSynced(
                            lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                            id = dto.id
                        )
                        eventsCount++
                    } else if ((existing.isDirty == 0L) && dto.updatedAt > existing.updatedAt) {
                        // Server has newer version - only update if local is clean
                        database.theseusQueries.updateMaintenanceEvent(
                            date = dto.date,
                            hoursAtMaintenance = dto.hoursAtMaintenance,
                            cyclesAtMaintenance = dto.cyclesAtMaintenance.toLong(),
                            maintenanceType = dto.maintenanceType,
                            description = dto.description,
                            technicianName = dto.technicianName,
                            technicianCertification = dto.technicianCertification,
                            updatedAt = dto.updatedAt,
                            id = dto.id
                        )
                        database.theseusQueries.markMaintenanceEventSynced(
                            lastSyncedAt = Clock.System.now().toEpochMilliseconds(),
                            id = dto.id
                        )
                        eventsCount++
                    }
                }
            }.onFailure {
                return PullResult.Failed("Failed to fetch maintenance events: ${it.message}")
            }

            return PullResult.Success(aircraftCount, eventsCount)
        } catch (e: Exception) {
            return PullResult.Failed(e.message ?: "Unknown pull error")
        }
    }

    fun loadLastSyncTime() {
        val status = database.theseusQueries.selectSyncStatus("last_sync").executeAsOneOrNull()
        _lastSyncTime.value = status?.lastSuccessfulSync
    }
}

sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(val progress: Int) : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

sealed class SyncResult {
    data class Success(
        val pushedAircraft: Int,
        val pushedEvents: Int,
        val pulledAircraft: Int,
        val pulledEvents: Int
    ) : SyncResult()
    data class Failed(val message: String) : SyncResult()
    data object AlreadySyncing : SyncResult()
}

private sealed class PushResult {
    data class Success(val aircraftCount: Int, val eventsCount: Int) : PushResult()
    data class Failed(val message: String) : PushResult()
}

private sealed class PullResult {
    data class Success(val aircraftCount: Int, val eventsCount: Int) : PullResult()
    data class Failed(val message: String) : PullResult()
}