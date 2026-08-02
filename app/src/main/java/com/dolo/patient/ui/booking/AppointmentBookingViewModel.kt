package com.dolo.patient.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dolo.patient.R
import com.dolo.patient.data.DummyData
import com.dolo.patient.data.FamilyMember
import com.dolo.patient.data.PatientProfile
import com.dolo.patient.data.PatientUiState
import com.dolo.patient.data.model.Doctor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FakeAppointmentBookingRepository(
    private val doctor: Doctor,
    private val serviceCharge: Int = 20,
    private val loadDelayMillis: Long = 120,
    private val bookingDelayMillis: Long = 450,
    private val failureMessage: String? = null
) : AppointmentBookingRepository {
    override suspend fun getBookingOptions(doctorId: String): Result<AppointmentBookingOptions> = runCatching {
        if (loadDelayMillis > 0) delay(loadDelayMillis)
        failureMessage?.let { error(it) }
        val today = LocalDate.now()
        AppointmentBookingOptions(
            clinics = clinicOptions(doctor),
            dates = (0L..6L).map { offset ->
                val date = today.plusDays(offset)
                AppointmentDateUiModel(date, isAvailable = date.dayOfWeek != DayOfWeek.SUNDAY)
            },
            sessions = listOf(
                WalkInSessionUiModel(
                    id = "morning",
                    name = "Morning Session",
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(13, 0),
                    reportingTime = LocalTime.of(8, 30),
                    availableTokens = if (doctor.morningAvailable) 30 else 0,
                    totalTokens = 40,
                    type = WalkInSessionType.MORNING,
                    isAvailable = doctor.morningAvailable
                ),
                WalkInSessionUiModel(
                    id = "evening",
                    name = "Evening Session",
                    startTime = LocalTime.of(17, 0),
                    endTime = LocalTime.of(21, 0),
                    reportingTime = LocalTime.of(16, 30),
                    availableTokens = if (doctor.eveningAvailable) 22 else 0,
                    totalTokens = 35,
                    type = WalkInSessionType.EVENING,
                    isAvailable = doctor.eveningAvailable
                )
            ),
            fees = AppointmentFeeUiModel(doctor.consultationFee, serviceCharge)
        )
    }

    override suspend fun createWalkInAppointment(request: CreateWalkInAppointmentRequest): Result<AppointmentBookingResult> = runCatching {
        if (bookingDelayMillis > 0) delay(bookingDelayMillis)
        failureMessage?.let { error(it) }
        AppointmentBookingResult(
            appointmentId = "local-${System.currentTimeMillis()}",
            tokenNumber = 0,
            status = "READY_FOR_LOCAL_COMMIT"
        )
    }

    private fun clinicOptions(doctor: Doctor): List<ClinicOptionUiModel> {
        val primaryName = doctor.clinic.substringBefore(',').trim()
        val primaryAddress = doctor.clinic.substringAfter(',', "Sector 45").trim()
        return listOf(
            ClinicOptionUiModel("primary", primaryName, "$primaryAddress, Gurugram, Haryana", 2.3),
            ClinicOptionUiModel("life-line", "Life Line Hospital", "Sector 44, Main Road, Gurugram, Haryana", 3.8),
            ClinicOptionUiModel("city-heart", "City Heart Center", "Sushant Lok, Phase 1, Gurugram, Haryana", 4.1),
            ClinicOptionUiModel("medanta", "Medanta - The Medicity", "Sector 38, Gurugram, Haryana", 5.2)
        )
    }
}

class AppointmentBookingViewModel(
    private val doctorId: String,
    initialPatientState: PatientUiState,
    private val repository: AppointmentBookingRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    private val doctor = DummyData.doctors.firstOrNull { it.id == doctorId } ?: DummyData.doctors.first()
    private val _uiState = MutableStateFlow(
        AppointmentBookingUiState(
            doctor = doctor.toSummary(initialPatientState.favouriteIds),
            visitors = visitors(initialPatientState.profile, initialPatientState.family),
            selectedVisitorId = "self",
            notificationCount = initialPatientState.notifications.count { !it.isRead },
            isLoading = false
        )
    )
    val uiState: StateFlow<AppointmentBookingUiState> = _uiState.asStateFlow()

    init {
        loadOptions()
    }

    fun onEvent(event: AppointmentBookingUiEvent) {
        when (event) {
            AppointmentBookingUiEvent.ConfirmBookingClicked -> confirmBooking()
            AppointmentBookingUiEvent.Retry -> loadOptions()
            else -> _uiState.update { AppointmentBookingReducer.reduce(it, event) }
        }
    }

    fun syncPatientState(state: PatientUiState) {
        val updatedVisitors = visitors(state.profile, state.family)
        _uiState.update { current ->
            val selected = current.selectedVisitorId?.takeIf { id -> updatedVisitors.any { it.id == id } } ?: "self"
            current.copy(
                doctor = current.doctor?.copy(isFavourite = doctor.id in state.favouriteIds),
                visitors = updatedVisitors,
                selectedVisitorId = selected,
                notificationCount = state.notifications.count { !it.isRead }
            )
        }
    }

    fun acknowledgeNavigation() {
        _uiState.update { it.copy(pendingRequest = null, bookingResult = null, isBooking = false) }
    }

    private fun loadOptions() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch(dispatcher) {
            repository.getBookingOptions(doctor.id)
                .onSuccess { options ->
                    val selectedDate = options.dates.firstOrNull { it.isAvailable }?.date
                    val selectedSession = options.sessions.firstOrNull { it.isAvailable }?.id
                    _uiState.update {
                        it.copy(
                            clinics = options.clinics,
                            selectedClinicId = options.clinics.firstOrNull { clinic -> clinic.isAvailable }?.id,
                            dates = options.dates,
                            selectedDate = selectedDate,
                            sessions = options.sessions,
                            selectedSessionId = selectedSession,
                            fees = options.fees,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.message ?: "Unable to load booking options. Please try again")
                    }
                }
        }
    }

    private fun confirmBooking() {
        val current = _uiState.value
        if (current.isBooking) return
        val validation = AppointmentBookingLogic.validate(current)
        if (validation != null) {
            _uiState.update { it.copy(validationField = validation.first, errorMessage = validation.second) }
            return
        }
        val request = AppointmentBookingLogic.createRequest(current) ?: return
        _uiState.update { it.copy(isBooking = true, errorMessage = null, validationField = null, pendingRequest = request) }
        viewModelScope.launch(dispatcher) {
            repository.createWalkInAppointment(request)
                .onSuccess { result -> _uiState.update { it.copy(isBooking = false, bookingResult = result) } }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isBooking = false,
                            pendingRequest = null,
                            errorMessage = failure.message ?: "Unable to complete booking. Please try again"
                        )
                    }
                }
        }
    }

    private fun Doctor.toSummary(favourites: Set<String>): DoctorBookingSummaryUiModel = DoctorBookingSummaryUiModel(
        id = id,
        name = name,
        qualifications = if (id == "3") "MBBS, MD (Cardiology), DM" else "MBBS, MD",
        specialty = specialty,
        rating = rating,
        reviewCount = if (id == "3") 152 else 42 + (id.toIntOrNull() ?: 0) * 3,
        experienceYears = experienceYears,
        primaryClinic = clinic,
        distanceKm = 2.3,
        consultationFee = consultationFee,
        imageRes = portraitFor(id),
        isFavourite = id in favourites
    )

    private fun portraitFor(id: String): Int = when (id) {
        "3" -> R.drawable.doctor_rohan
        "4" -> R.drawable.doctor_anjali
        "25" -> R.drawable.doctor_arjun
        "26" -> R.drawable.doctor_neha
        else -> if ((id.toIntOrNull() ?: 0) % 2 == 0) R.drawable.doctor_anjali else R.drawable.doctor_rohan
    }

    private fun visitors(profile: PatientProfile, family: List<FamilyMember>): List<AppointmentVisitorUiModel> = buildList {
        add(
            AppointmentVisitorUiModel(
                id = "self",
                name = profile.name.ifBlank { "Patient" },
                relationLabel = "Self",
                initials = initials(profile.name),
                isSelf = true,
                phone = profile.phone,
                isProfileComplete = profile.name.isNotBlank() && profile.phone.filter(Char::isDigit).length == 10
            )
        )
        family.forEach { member ->
            add(
                AppointmentVisitorUiModel(
                    id = member.id,
                    name = member.name,
                    relationLabel = member.relation.ifBlank { "Family Member" },
                    initials = initials(member.name),
                    age = member.age,
                    isProfileComplete = member.name.isNotBlank() && member.relation.isNotBlank() && member.age > 0
                )
            )
        }
    }

    private fun initials(name: String): String = name.trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "PT" }
}

class AppointmentBookingViewModelFactory(
    private val doctorId: String,
    private val patientState: PatientUiState,
    private val repository: AppointmentBookingRepository = FakeAppointmentBookingRepository(
        DummyData.doctors.firstOrNull { it.id == doctorId } ?: DummyData.doctors.first()
    )
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppointmentBookingViewModel(doctorId, patientState, repository) as T
}