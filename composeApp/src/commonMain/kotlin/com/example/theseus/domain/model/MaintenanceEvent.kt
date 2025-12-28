package com.example.theseus.domain.model

import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
data class MaintenanceEvent(
    val id: String,
    val aircraftId: String,
    val date: Instant,
    val hoursAtMaintenance: Double,
    val cyclesAtMaintenance: Int,
    val maintenanceType: MaintenanceType,
    val description: String,
    val technicianName: String,
    val technicianCertification: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            aircraftId: String,
            date: Instant,
            hoursAtMaintenance: Double,
            cyclesAtMaintenance: Int,
            maintenanceType: MaintenanceType,
            description: String,
            technicianName: String,
            technicianCertification: String?
        ): MaintenanceEvent {
            val now = kotlinx.datetime.Clock.System.now()
            return MaintenanceEvent(
                id = generateId(),
                aircraftId = aircraftId,
                date = date,
                hoursAtMaintenance = hoursAtMaintenance,
                cyclesAtMaintenance = cyclesAtMaintenance,
                maintenanceType = maintenanceType,
                description = description,
                technicianName = technicianName,
                technicianCertification = technicianCertification,
                createdAt = now,
                updatedAt = now
            )
        }

        private fun generateId(): String {
            return "maint_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
        }
    }
}

enum class MaintenanceType {
    INSPECTION,
    REPAIR,
    MODIFICATION,
    ROUTINE_SERVICE,
    COMPONENT_REPLACEMENT,
    OTHER;

    fun displayName(): String = when(this) {
        INSPECTION -> "Inspection"
        REPAIR -> "Repair"
        MODIFICATION -> "Modification"
        ROUTINE_SERVICE -> "Routine Service"
        COMPONENT_REPLACEMENT -> "Component Replacement"
        OTHER -> "Other"
    }
}
