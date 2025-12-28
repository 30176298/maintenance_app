package com.example.theseus.domain.model

import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
data class Aircraft(
    val id: String,
    val registration: String,
    val make: String,
    val model: String,
    val serialNumber: String,
    val yearOfManufacture: Int?,
    val totalHours: Double,
    val totalCycles: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            registration: String,
            make: String,
            model: String,
            serialNumber: String,
            yearOfManufacture: Int?,
            totalHours: Double = 0.0,
            totalCycles: Int = 0
        ): Aircraft {
            val now = kotlinx.datetime.Clock.System.now()
            return Aircraft(
                id = generateId(),
                registration = registration,
                make = make,
                model = model,
                serialNumber = serialNumber,
                yearOfManufacture = yearOfManufacture,
                totalHours = totalHours,
                totalCycles = totalCycles,
                createdAt = now,
                updatedAt = now
            )
        }

        private fun generateId(): String {
            return "aircraft_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
        }
    }
}