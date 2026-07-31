package com.dolo.patient.ui.home

import androidx.annotation.DrawableRes

enum class QueueStatus { ACTIVE, UPCOMING, PAUSED, COMPLETED }

data class QueueSummaryUiModel(
    val appointmentId: String,
    val patientName: String,
    val patientToken: String,
    val currentToken: String,
    val estimatedWaitMinutes: IntRange?,
    val bookingTime: String,
    val doctorName: String,
    val clinicName: String,
    val session: String,
    val queueStatus: QueueStatus,
    val isHosted: Boolean = false
)

enum class BroadcastActionType { DOCTOR_PROFILE, CLINIC, CATEGORY, APPOINTMENT, EXTERNAL_LINK, INFORMATION }

data class BroadcastUiModel(
    val id: String,
    val title: String,
    val message: String,
    val buttonText: String? = null,
    val imageUrl: String? = null,
    val actionType: BroadcastActionType = BroadcastActionType.INFORMATION,
    val actionValue: String? = null,
    val dismissible: Boolean = true
)

data class FavoriteDoctorUiModel(
    val id: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val reviewCount: Int,
    val distanceKm: Double?,
    @DrawableRes val imageRes: Int
)

data class PatientHomeUiState(
    val patientName: String = "",
    val patientCity: String = "",
    val queues: List<QueueSummaryUiModel> = emptyList(),
    val broadcasts: List<BroadcastUiModel> = emptyList(),
    val favoriteDoctors: List<FavoriteDoctorUiModel> = emptyList(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = false,
    val broadcastLoading: Boolean = false,
    val errorMessage: String? = null
) { val queueSummary: QueueSummaryUiModel? get() = queues.firstOrNull() }

sealed interface PatientHomeUiEvent {
    data object OpenMenu : PatientHomeUiEvent
    data object OpenNotifications : PatientHomeUiEvent
    data object SearchDoctors : PatientHomeUiEvent
    data object FindDoctorsNearMe : PatientHomeUiEvent
    data object ViewAllQueues : PatientHomeUiEvent
    data class OpenQueue(val appointmentId: String) : PatientHomeUiEvent
    data class DismissBroadcast(val id: String) : PatientHomeUiEvent
    data class OpenBroadcast(val broadcast: BroadcastUiModel) : PatientHomeUiEvent
    data object ViewFavoriteDoctors : PatientHomeUiEvent
    data class OpenDoctor(val doctorId: String) : PatientHomeUiEvent
    data class BookAgain(val doctorId: String) : PatientHomeUiEvent
    data object OpenAppointments : PatientHomeUiEvent
    data object OpenBook : PatientHomeUiEvent
    data object OpenHistory : PatientHomeUiEvent
    data object OpenProfile : PatientHomeUiEvent
}