package com.dolo.patient.ui.booking

import androidx.annotation.DrawableRes
import java.time.LocalDate
import java.time.LocalTime

data class DoctorBookingSummaryUiModel(
    val id: String,
    val name: String,
    val qualifications: String,
    val specialty: String,
    val rating: Double,
    val reviewCount: Int,
    val experienceYears: Int,
    val primaryClinic: String,
    val distanceKm: Double?,
    val consultationFee: Int,
    @DrawableRes val imageRes: Int?,
    val isVerified: Boolean = true,
    val isFavourite: Boolean = false
)

data class AppointmentVisitorUiModel(
    val id: String,
    val name: String,
    val relationLabel: String,
    val initials: String,
    val profileImageUrl: String? = null,
    val isSelf: Boolean = false,
    val phone: String? = null,
    val age: Int? = null,
    val isProfileComplete: Boolean = true
)

data class ClinicOptionUiModel(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isAvailable: Boolean = true
)

data class AppointmentDateUiModel(
    val date: LocalDate,
    val isAvailable: Boolean,
    val isSelected: Boolean = false
)

enum class WalkInSessionType { MORNING, EVENING }

data class WalkInSessionUiModel(
    val id: String,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val reportingTime: LocalTime?,
    val availableTokens: Int,
    val totalTokens: Int,
    val type: WalkInSessionType,
    val isAvailable: Boolean
)

data class AppointmentFeeUiModel(
    val consultationFee: Int,
    val serviceCharge: Int,
    val discount: Int = 0,
    val tax: Int = 0
) {
    val totalPayable: Int
        get() = (consultationFee + serviceCharge + tax - discount).coerceAtLeast(0)
}

data class AppointmentBookingOptions(
    val clinics: List<ClinicOptionUiModel>,
    val dates: List<AppointmentDateUiModel>,
    val sessions: List<WalkInSessionUiModel>,
    val fees: AppointmentFeeUiModel
)

data class CreateWalkInAppointmentRequest(
    val doctorId: String,
    val patientProfileId: String,
    val patientName: String,
    val clinicId: String,
    val appointmentDate: LocalDate,
    val sessionId: String,
    val sessionType: WalkInSessionType
)

data class AppointmentBookingResult(
    val appointmentId: String,
    val tokenNumber: Int,
    val status: String
)

enum class BookingValidationField {
    VISITOR,
    CLINIC,
    DATE,
    SESSION,
    FAMILY_PROFILE
}

data class AppointmentBookingUiState(
    val doctor: DoctorBookingSummaryUiModel? = null,
    val visitors: List<AppointmentVisitorUiModel> = emptyList(),
    val selectedVisitorId: String? = null,
    val clinics: List<ClinicOptionUiModel> = emptyList(),
    val selectedClinicId: String? = null,
    val dates: List<AppointmentDateUiModel> = emptyList(),
    val selectedDate: LocalDate? = null,
    val sessions: List<WalkInSessionUiModel> = emptyList(),
    val selectedSessionId: String? = null,
    val fees: AppointmentFeeUiModel? = null,
    val notificationCount: Int = 0,
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val errorMessage: String? = null,
    val validationField: BookingValidationField? = null,
    val pendingRequest: CreateWalkInAppointmentRequest? = null,
    val bookingResult: AppointmentBookingResult? = null
) {
    val selectedVisitor: AppointmentVisitorUiModel?
        get() = visitors.firstOrNull { it.id == selectedVisitorId }
    val selectedClinic: ClinicOptionUiModel?
        get() = clinics.firstOrNull { it.id == selectedClinicId }
    val selectedSession: WalkInSessionUiModel?
        get() = sessions.firstOrNull { it.id == selectedSessionId }
    val hasRequiredSelections: Boolean
        get() = selectedVisitor != null && selectedClinic != null && selectedDate != null && selectedSession != null
    val canConfirm: Boolean
        get() = hasRequiredSelections && selectedVisitor?.isProfileComplete == true && !isLoading && !isBooking
}

sealed interface AppointmentBookingUiEvent {
    data class VisitorSelected(val visitorId: String) : AppointmentBookingUiEvent
    data class ClinicSelected(val clinicId: String) : AppointmentBookingUiEvent
    data class DateSelected(val date: LocalDate) : AppointmentBookingUiEvent
    data class SessionSelected(val sessionId: String) : AppointmentBookingUiEvent
    data object ConfirmBookingClicked : AppointmentBookingUiEvent
    data object ChangePatientClicked : AppointmentBookingUiEvent
    data object ServiceChargeInfoClicked : AppointmentBookingUiEvent
    data object SaveDoctorClicked : AppointmentBookingUiEvent
    data object MoreDatesClicked : AppointmentBookingUiEvent
    data object BackClicked : AppointmentBookingUiEvent
    data object NotificationsClicked : AppointmentBookingUiEvent
    data object Retry : AppointmentBookingUiEvent
}

object AppointmentBookingReducer {
    fun reduce(state: AppointmentBookingUiState, event: AppointmentBookingUiEvent): AppointmentBookingUiState = when (event) {
        is AppointmentBookingUiEvent.VisitorSelected -> {
            val visitor = state.visitors.firstOrNull { it.id == event.visitorId }
            if (visitor == null) state else state.copy(
                selectedVisitorId = visitor.id,
                validationField = if (visitor.isProfileComplete) null else BookingValidationField.FAMILY_PROFILE,
                errorMessage = if (visitor.isProfileComplete) null else "The selected family member profile is incomplete"
            )
        }
        is AppointmentBookingUiEvent.ClinicSelected -> {
            val clinic = state.clinics.firstOrNull { it.id == event.clinicId && it.isAvailable }
            if (clinic == null) state else state.copy(selectedClinicId = clinic.id, validationField = null, errorMessage = null)
        }
        is AppointmentBookingUiEvent.DateSelected -> {
            val date = state.dates.firstOrNull { it.date == event.date && it.isAvailable && !it.date.isBefore(LocalDate.now()) }
            if (date == null) state else state.copy(selectedDate = date.date, validationField = null, errorMessage = null)
        }
        is AppointmentBookingUiEvent.SessionSelected -> {
            val session = state.sessions.firstOrNull { it.id == event.sessionId }
            when {
                session == null -> state
                !session.isAvailable -> state.copy(
                    validationField = BookingValidationField.SESSION,
                    errorMessage = "This session is no longer available"
                )
                else -> state.copy(selectedSessionId = session.id, validationField = null, errorMessage = null)
            }
        }
        AppointmentBookingUiEvent.SaveDoctorClicked -> state.copy(
            doctor = state.doctor?.copy(isFavourite = !state.doctor.isFavourite)
        )
        else -> state
    }
}

object AppointmentBookingLogic {
    fun validate(state: AppointmentBookingUiState): Pair<BookingValidationField, String>? = when {
        state.selectedVisitor == null -> BookingValidationField.VISITOR to "Please select who is visiting"
        state.selectedVisitor?.isProfileComplete != true -> BookingValidationField.FAMILY_PROFILE to "The selected family member profile is incomplete"
        state.selectedClinic == null -> BookingValidationField.CLINIC to "Please select a clinic"
        state.selectedDate == null -> BookingValidationField.DATE to "Please select a date"
        state.selectedSession == null -> BookingValidationField.SESSION to "Please select a walk-in session"
        state.selectedSession?.isAvailable != true -> BookingValidationField.SESSION to "This session is no longer available"
        else -> null
    }

    fun createRequest(state: AppointmentBookingUiState): CreateWalkInAppointmentRequest? {
        if (validate(state) != null) return null
        val doctor = state.doctor ?: return null
        val visitor = state.selectedVisitor ?: return null
        val clinic = state.selectedClinic ?: return null
        val date = state.selectedDate ?: return null
        val session = state.selectedSession ?: return null
        return CreateWalkInAppointmentRequest(
            doctorId = doctor.id,
            patientProfileId = visitor.id,
            patientName = visitor.name,
            clinicId = clinic.id,
            appointmentDate = date,
            sessionId = session.id,
            sessionType = session.type
        )
    }

    fun formatInr(amount: Int): String = "\u20B9" + amount.coerceAtLeast(0)
}

interface AppointmentBookingRepository {
    suspend fun getBookingOptions(doctorId: String): Result<AppointmentBookingOptions>
    suspend fun createWalkInAppointment(request: CreateWalkInAppointmentRequest): Result<AppointmentBookingResult>
}