package com.example.theseus.data.remote.dto

import kotlinx.serialization.Serializable
@Serializable
data class MaintenanceEventDto (
    val id: String,
    val aircraftId: String,
    val date: Long,
    val hoursAtMaintenance: Double,
    val cyclesAtMaintenance: Int,
    val maintenanceType: String,
    val description: String,
    val technicianName: String,
    val technicianCertification: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)