package com.example.theseus.data.remote.dto

import kotlinx.serialization.Serializable
@Serializable
data class AircraftDto (
    val id: String,
    val registration: String,
    val make: String,
    val model: String,
    val serialNumber: String,
    val yearOfManufacture: Int? = null,
    val totalHours: Double = 0.0,
    val totalCycles: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)