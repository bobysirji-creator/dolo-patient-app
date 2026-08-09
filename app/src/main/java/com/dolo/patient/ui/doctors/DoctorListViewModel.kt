package com.dolo.patient.ui.doctors

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dolo.patient.R
import com.dolo.patient.data.DummyData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FakeDoctorListRepository(
    private val loadDelayMillis: Long = 160,
    private val failureMessage: String? = null
) : DoctorListRepository {
    override suspend fun getDoctorsByCategory(categoryId: String): Result<List<DoctorListItemUiModel>> =
        runCatching {
            if (loadDelayMillis > 0) delay(loadDelayMillis)
            failureMessage?.let { error(it) }
            if (categoryId.lowercase() in setOf("cardiology", "cardio")) {
                DoctorListCatalog.cardiologists
            } else {
                val specialty = specialtyForCategory(categoryId)
                DummyData.doctors
                    .filter { specialty == null || it.specialty.equals(specialty, ignoreCase = true) }
                    .mapIndexed { index, doctor ->
                        DoctorListItemUiModel(
                            id = doctor.id,
                            name = doctor.name,
                            qualifications = "MBBS, MD",
                            specialty = doctor.specialty,
                            imageRes = portraitFor(index),
                            rating = doctor.rating,
                            reviewCount = 42 + (doctor.id.toIntOrNull() ?: index) * 3,
                            experienceYears = doctor.experienceYears,
                            clinicName = doctor.clinic,
                            clinicAddress = "Nearby clinic",
                            distanceKm = 1.8 + index * 0.7,
                            consultationFee = doctor.consultationFee,
                            availability = DoctorAvailabilityUiModel(
                                if (doctor.morningAvailable || doctor.eveningAvailable) DoctorAvailabilityType.AVAILABLE_TODAY else DoctorAvailabilityType.NOT_AVAILABLE,
                                if (doctor.morningAvailable || doctor.eveningAvailable) "Available Today" else "Not Available"
                            )
                        )
                    }
            }
        }

    private fun specialtyForCategory(id: String): String? = when (id.lowercase()) {
        "general-physician", "general" -> "General Physician"
        "pediatrics", "child" -> "Pediatrician"
        "dermatology", "skin" -> "Dermatologist"
        "gynecology", "gyn" -> "Gynecologist"
        "orthopedics", "ortho" -> "Orthopedic"
        "cardiology", "cardio" -> "Cardiologist"
        "ent" -> "ENT Specialist"
        "ophthalmology", "eye" -> "Ophthalmologist"
        "dentistry", "dental" -> "Dentist"
        "psychiatry", "mental" -> "Psychiatrist"
        "neurology", "neuro" -> "Neurologist"
        "physio" -> "Physiotherapist"
        else -> null
    }

    private fun portraitFor(index: Int): Int = when (index % 4) {
        0 -> R.drawable.doctor_rohan
        1 -> R.drawable.doctor_anjali
        2 -> R.drawable.doctor_arjun
        else -> R.drawable.doctor_neha
    }
}

class DoctorListViewModel(
    private val categoryId: String,
    categoryName: String,
    private val repository: DoctorListRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    private val category = DoctorCategoryHeaders.resolve(categoryId, categoryName)
    private var localDoctors: List<DoctorListItemUiModel> = emptyList()
    private var hostedDoctors: List<DoctorListItemUiModel> = emptyList()

    private var hostedOnly = false
    var uiState = mutableStateOf(DoctorListUiState(category = category))
        private set

    init {
        loadDoctors()
    }

    fun onEvent(event: DoctorListUiEvent) {
        when (event) {
            is DoctorListUiEvent.SearchChanged -> update(query = event.value)
            DoctorListUiEvent.ClearSearch -> update(query = "")
            is DoctorListUiEvent.SortChanged -> update(sort = event.sort)
            is DoctorListUiEvent.FiltersChanged -> update(filters = event.filters)
            is DoctorListUiEvent.FavouriteClicked -> toggleFavourite(event.doctorId)
            DoctorListUiEvent.Retry -> loadDoctors()
            DoctorListUiEvent.Refresh -> loadDoctors(isRefresh = true)
            DoctorListUiEvent.ShowAllHostedClicked -> showAllHosted()
            else -> Unit
        }
    }

    fun syncFavourites(ids: Set<String>) {
        localDoctors = localDoctors.map { it.copy(isFavourite = it.id in ids) }
        hostedDoctors = hostedDoctors.map { it.copy(isFavourite = it.id in ids) }
        val updated = allDoctors().distinctBy { it.id }
        if (updated != uiState.value.doctors) {
            uiState.value = uiState.value.copy(doctors = updated)
            recompute()
        }
    }

    fun updateHostedDoctors(doctors: List<DoctorListItemUiModel>) {
        val favouriteIds = uiState.value.doctors.filter(DoctorListItemUiModel::isFavourite).mapTo(mutableSetOf(), DoctorListItemUiModel::id)
        val updated = doctors.map { it.copy(isFavourite = it.id in favouriteIds) }
        if (hostedDoctors == updated) return
        hostedDoctors = updated
        val merged = allDoctors().distinctBy { it.id }
        uiState.value = uiState.value.copy(doctors = merged)
        recompute()
    }

    fun activateNearbyMode() {
        if (uiState.value.isNearbyMode) return
        hostedOnly = true
        hostedDoctors = emptyList()
        uiState.value = uiState.value.copy(doctors = emptyList(), isNearbyMode = true)
        recompute()
    }

    private fun showAllHosted() {
        hostedDoctors = emptyList()
        uiState.value = uiState.value.copy(doctors = emptyList(), isNearbyMode = false)
        recompute()
    }

    private fun toggleFavourite(doctorId: String) {
        localDoctors = localDoctors.map { if (it.id == doctorId) it.copy(isFavourite = !it.isFavourite) else it }
        hostedDoctors = hostedDoctors.map { if (it.id == doctorId) it.copy(isFavourite = !it.isFavourite) else it }
        uiState.value = uiState.value.copy(doctors = allDoctors(), favouriteUpdateDoctorId = null)
        recompute()
    }

    private fun update(
        query: String = uiState.value.searchQuery,
        filters: DoctorFilterState = uiState.value.filters,
        sort: DoctorSortOption = uiState.value.selectedSort
    ) {
        uiState.value = uiState.value.copy(searchQuery = query, filters = filters, selectedSort = sort)
        recompute()
    }

    private fun recompute() {
        val current = uiState.value
        uiState.value = current.copy(
            filteredDoctors = DoctorListLogic.apply(current.doctors, current.searchQuery, current.filters, current.selectedSort)
        )
    }

    private fun loadDoctors(isRefresh: Boolean = false) {
        uiState.value = uiState.value.copy(
            isLoading = !isRefresh,
            isRefreshing = isRefresh,
            errorMessage = null
        )
        viewModelScope.launch(dispatcher) {
            repository.getDoctorsByCategory(categoryId)
                .onSuccess {
                    localDoctors = it
                    val all = allDoctors()
                    uiState.value = uiState.value.copy(doctors = all, isLoading = false, isRefreshing = false, errorMessage = null)
                    recompute()
                }
                .onFailure {
                    val current = uiState.value
                    uiState.value = current.copy(
                        doctors = if (isRefresh) current.doctors else emptyList(),
                        filteredDoctors = if (isRefresh) current.filteredDoctors else emptyList(),
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = if (isRefresh) null else (it.message ?: "We could not load Doctors.")
                    )
                }
        }
    }

    private fun allDoctors(): List<DoctorListItemUiModel> =
        if (hostedOnly) hostedDoctors else localDoctors + hostedDoctors
}

class DoctorListViewModelFactory(
    private val categoryId: String,
    private val categoryName: String,
    private val repository: DoctorListRepository = FakeDoctorListRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DoctorListViewModel(categoryId, categoryName, repository) as T
}