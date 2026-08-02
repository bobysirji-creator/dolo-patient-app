package com.dolo.patient.ui.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.R
import com.dolo.patient.data.Appointment
import com.dolo.patient.data.QueueSnapshot
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

interface BookingConfirmationRepository { fun confirmation(appointmentId: String): BookingConfirmationUiState }

class LocalBookingConfirmationRepository(
    private val appointment: Appointment?,
    private val doctor: Doctor?,
    private val queue: QueueSnapshot?,
    private val notificationCount: Int,
) : BookingConfirmationRepository {
    override fun confirmation(appointmentId: String): BookingConfirmationUiState {
        val current = appointment?.takeIf { appointmentId.isBlank() || it.id == appointmentId }
            ?: return BookingConfirmationUiState(isLoading = false, status = BookingStatus.FAILED, appointmentId = appointmentId, notificationCount = notificationCount, errorMessage = "We could not load this booking confirmation.")
        val token = runCatching { TokenUiModel(current.token) }.getOrNull()
            ?: return BookingConfirmationUiState(isLoading = false, status = BookingStatus.FAILED, appointmentId = current.id, notificationCount = notificationCount, errorMessage = "Your token is not available yet. Please view Appointments and retry.")
        val session = if (current.session == Session.MORNING) SessionConfirmationUiModel("Morning Session", "9:00 AM - 1:00 PM") else SessionConfirmationUiModel("Evening Session", "5:00 PM - 8:00 PM")
        val clinicParts = current.clinic.split(",", limit = 2)
        return BookingConfirmationUiState(
            isLoading = false,
            status = BookingStatus.CONFIRMED,
            appointmentId = current.id,
            token = token,
            appointmentDate = formatDate(current.date),
            session = session,
            doctor = DoctorConfirmationUiModel(current.doctorId, current.doctorName, doctor?.specialty ?: "Doctor consultation", portraitFor(current.doctorId)),
            clinic = ClinicConfirmationUiModel(clinicParts.firstOrNull().orEmpty().ifBlank { current.clinic }, clinicParts.drop(1).joinToString(",").trim().ifBlank { "Clinic address available in Doctor profile" }),
            patientName = current.patientName,
            queueEstimate = queue?.let { QueueEstimateUiModel((it.estimatedMinutes - 5).coerceAtLeast(0), it.estimatedMinutes, it.patientsAhead) },
            notificationCount = notificationCount,
        )
    }

    private fun formatDate(value: String): String = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH)) }.getOrDefault(value)
    private fun portraitFor(id: String): Int = when (id) {
        "3" -> R.drawable.doctor_rohan
        "4" -> R.drawable.doctor_anjali
        "25" -> R.drawable.doctor_arjun
        "26" -> R.drawable.doctor_neha
        else -> if ((id.toIntOrNull() ?: 1) % 2 == 0) R.drawable.doctor_anjali else R.drawable.doctor_rohan
    }
}

class BookingConfirmationViewModel(private val appointmentId: String, private val repository: BookingConfirmationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(repository.confirmation(appointmentId))
    val uiState: StateFlow<BookingConfirmationUiState> = _uiState.asStateFlow()
    fun retry() { _uiState.value = repository.confirmation(appointmentId) }
}

class BookingConfirmationViewModelFactory(private val appointmentId: String, private val repository: BookingConfirmationRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BookingConfirmationViewModel(appointmentId, repository) as T
}
