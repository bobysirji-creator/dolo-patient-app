package com.dolo.patient.data.model

data class DoctorCategory(val id: String, val name: String, val symbol: String)

data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val clinic: String,
    val experienceYears: Int,
    val rating: Double,
    val consultationFee: Int,
    val morningAvailable: Boolean = true,
    val eveningAvailable: Boolean = true,
)

enum class Session { MORNING, EVENING }

data class AppointmentDraft(
    val doctor: Doctor,
    val session: Session,
    val dateLabel: String,
)

