package com.dolo.patient.ui.confirmation

import androidx.annotation.DrawableRes

enum class BookingStatus { CONFIRMED, PENDING, FAILED }

data class TokenUiModel(val number: Int) {
    init { require(number in 1..999) { "Token number must be between 1 and 999." } }
}

data class QueueEstimateUiModel(val minimumMinutes: Int, val maximumMinutes: Int, val patientsAhead: Int) {
    init { require(minimumMinutes >= 0); require(maximumMinutes >= minimumMinutes); require(patientsAhead >= 0) }
    val timeLabel: String get() = when {
        maximumMinutes == 0 -> "It is your turn"
        minimumMinutes == maximumMinutes -> "About $maximumMinutes mins"
        else -> "$minimumMinutes-$maximumMinutes mins"
    }
}

data class SessionConfirmationUiModel(val title: String, val timeRange: String)
data class DoctorConfirmationUiModel(val id: String, val name: String, val specialty: String, @DrawableRes val imageRes: Int?)
data class ClinicConfirmationUiModel(val name: String, val address: String)

data class BookingConfirmationUiState(
    val isLoading: Boolean = false,
    val status: BookingStatus = BookingStatus.PENDING,
    val appointmentId: String = "",
    val token: TokenUiModel? = null,
    val appointmentDate: String = "",
    val session: SessionConfirmationUiModel? = null,
    val doctor: DoctorConfirmationUiModel? = null,
    val clinic: ClinicConfirmationUiModel? = null,
    val patientName: String = "",
    val queueEstimate: QueueEstimateUiModel? = null,
    val notificationCount: Int = 0,
    val isSharing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canShowConfirmation: Boolean get() = !isLoading && status == BookingStatus.CONFIRMED && token != null
}

sealed interface BookingConfirmationUiEvent {
    data object BackToHomeClicked : BookingConfirmationUiEvent
    data object AddToCalendarClicked : BookingConfirmationUiEvent
    data object ShareBookingClicked : BookingConfirmationUiEvent
    data object DownloadOrSaveClicked : BookingConfirmationUiEvent
    data object ViewMapClicked : BookingConfirmationUiEvent
    data object DoctorClicked : BookingConfirmationUiEvent
    data object NotificationsClicked : BookingConfirmationUiEvent
    data object Retry : BookingConfirmationUiEvent
}
object BookingConfirmationText {
    fun shareSummary(state: BookingConfirmationUiState): String {
        val token = state.token ?: return "DO-LO appointment confirmation"
        return buildString {
            append("DO-LO appointment confirmed. Token ${token.number}")
            state.doctor?.let { append(" with ${it.name}") }
            if (state.appointmentDate.isNotBlank()) append(" on ${state.appointmentDate}")
            state.session?.let { append(" (${it.title})") }
            append(". Consultation fee is payable at the clinic.")
        }
    }
}
