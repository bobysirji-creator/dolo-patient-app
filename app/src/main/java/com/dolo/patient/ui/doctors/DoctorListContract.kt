package com.dolo.patient.ui.doctors

import androidx.annotation.DrawableRes
import com.dolo.patient.R

data class DoctorCategoryHeaderUiModel(
    val categoryId: String,
    val categoryName: String,
    val pluralDisplayName: String,
    val supportingText: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val illustrationRes: Int? = null
)

enum class DoctorAvailabilityType { AVAILABLE_NOW, AVAILABLE_SOON, AVAILABLE_TODAY, NOT_AVAILABLE }

data class DoctorAvailabilityUiModel(val type: DoctorAvailabilityType, val label: String)

data class DoctorListItemUiModel(
    val id: String,
    val name: String,
    val qualifications: String,
    val specialty: String,
    val imageUrl: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val rating: Double,
    val reviewCount: Int,
    val experienceYears: Int,
    val clinicName: String,
    val clinicAddress: String,
    val distanceKm: Double? = null,
    val consultationFee: Int,
    val availability: DoctorAvailabilityUiModel,
    val isVerified: Boolean = true,
    val isFavourite: Boolean = false,
    val isHostedProfile: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val accessibleSummary: String
        get() = name + ", " + specialty + ", rating " + rating + ", " + experienceYears +
            " years experience, consultation fee " + consultationFee + " rupees, " + availability.label.lowercase()
}

enum class DoctorSortOption(val label: String) {
    RECOMMENDED("Recommended"),
    DISTANCE("Distance"),
    RATING("Rating"),
    EXPERIENCE("Experience"),
    FEES_LOW_TO_HIGH("Fees: Low to High"),
    FEES_HIGH_TO_LOW("Fees: High to Low")
}

data class DoctorFilterState(
    val availability: DoctorAvailabilityType? = null,
    val minimumFee: Int? = null,
    val maximumFee: Int? = null,
    val minimumExperience: Int? = null
) {
    val isActive: Boolean
        get() = availability != null || minimumFee != null || maximumFee != null || minimumExperience != null
}

data class DoctorListUiState(
    val category: DoctorCategoryHeaderUiModel? = null,
    val searchQuery: String = "",
    val doctors: List<DoctorListItemUiModel> = emptyList(),
    val filteredDoctors: List<DoctorListItemUiModel> = emptyList(),
    val selectedSort: DoctorSortOption = DoctorSortOption.RECOMMENDED,
    val filters: DoctorFilterState = DoctorFilterState(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val favouriteUpdateDoctorId: String? = null,
    val isNearbyMode: Boolean = false
) {
    val hasNoSearchResults: Boolean get() = !isLoading && errorMessage == null && searchQuery.isNotBlank() && filteredDoctors.isEmpty()
    val hasNoDoctors: Boolean get() = !isLoading && errorMessage == null && searchQuery.isBlank() && doctors.isEmpty()
    val hasFilteredEmptyState: Boolean get() = !isLoading && errorMessage == null && searchQuery.isBlank() && doctors.isNotEmpty() && filteredDoctors.isEmpty()
}

sealed interface DoctorListUiEvent {
    data class SearchChanged(val value: String) : DoctorListUiEvent
    data class DoctorSelected(val doctorId: String) : DoctorListUiEvent
    data class FavouriteClicked(val doctorId: String) : DoctorListUiEvent
    data class BookNowClicked(val doctorId: String) : DoctorListUiEvent
    data class ClinicLocationClicked(val doctorId: String) : DoctorListUiEvent
    data class SortChanged(val sort: DoctorSortOption) : DoctorListUiEvent
    data class FiltersChanged(val filters: DoctorFilterState) : DoctorListUiEvent
    data object NearMeClicked : DoctorListUiEvent
    data object ShowAllHostedClicked : DoctorListUiEvent
    data object ClearSearch : DoctorListUiEvent
    data object Retry : DoctorListUiEvent
    data object Refresh : DoctorListUiEvent
    data object BackClicked : DoctorListUiEvent
    data object NotificationsClicked : DoctorListUiEvent
    data object RequestCallbackClicked : DoctorListUiEvent
}

object DoctorListLogic {
    fun apply(
        doctors: List<DoctorListItemUiModel>,
        query: String,
        filters: DoctorFilterState,
        sort: DoctorSortOption
    ): List<DoctorListItemUiModel> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        val filtered = doctors.filter { doctor ->
            val searchText = listOf(
                doctor.name,
                doctor.specialty,
                doctor.qualifications,
                doctor.clinicName,
                doctor.clinicAddress
            ).joinToString(" ").lowercase()
            terms.all(searchText::contains) &&
                (filters.availability == null || doctor.availability.type == filters.availability) &&
                (filters.minimumFee == null || doctor.consultationFee >= filters.minimumFee) &&
                (filters.maximumFee == null || doctor.consultationFee <= filters.maximumFee) &&
                (filters.minimumExperience == null || doctor.experienceYears >= filters.minimumExperience)
        }
        return when (sort) {
            DoctorSortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<DoctorListItemUiModel> {
                    it.availability.type == DoctorAvailabilityType.AVAILABLE_NOW
                }.thenByDescending { it.rating }
            )
            DoctorSortOption.DISTANCE -> filtered.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            DoctorSortOption.RATING -> filtered.sortedByDescending(DoctorListItemUiModel::rating)
            DoctorSortOption.EXPERIENCE -> filtered.sortedByDescending(DoctorListItemUiModel::experienceYears)
            DoctorSortOption.FEES_LOW_TO_HIGH -> filtered.sortedBy(DoctorListItemUiModel::consultationFee)
            DoctorSortOption.FEES_HIGH_TO_LOW -> filtered.sortedByDescending(DoctorListItemUiModel::consultationFee)
        }
    }
}

object DoctorCategoryHeaders {
    fun resolve(categoryId: String, categoryName: String): DoctorCategoryHeaderUiModel {
        val id = categoryId.ifBlank { categoryName.lowercase().replace(' ', '-') }
        val definition = when (id.lowercase()) {
            "all" -> HeaderDefinition("Doctors", "Trusted healthcare professionals near you", R.drawable.category_general)
            "cardiology", "cardio" -> HeaderDefinition("Cardiologists", "Trusted heart specialists near you", R.drawable.category_cardio)
            "dermatology", "skin" -> HeaderDefinition("Dermatologists", "Trusted skin and hair specialists near you", R.drawable.category_skin)
            "pediatrics", "child" -> HeaderDefinition("Pediatricians", "Trusted child health specialists near you", R.drawable.category_child)
            "orthopedics", "ortho" -> HeaderDefinition("Orthopedic Doctors", "Trusted bone and joint specialists near you", R.drawable.category_ortho)
            "general-physician", "general" -> HeaderDefinition("General Physicians", "Trusted primary-care Doctors near you", R.drawable.category_general)
            "gynecology", "gyn" -> HeaderDefinition("Gynecologists", "Trusted women's health specialists near you", R.drawable.category_gyn)
            "neurology", "neuro" -> HeaderDefinition("Neurologists", "Trusted brain and nerve specialists near you", R.drawable.category_neuro)
            "dentistry", "dental" -> HeaderDefinition("Dentists", "Trusted dental specialists near you", R.drawable.category_dental)
            "ent" -> HeaderDefinition("ENT Specialists", "Trusted ear, nose and throat specialists near you", R.drawable.category_ent)
            "ophthalmology", "eye" -> HeaderDefinition("Ophthalmologists", "Trusted eye specialists near you", R.drawable.category_eye)
            "psychiatry", "mental" -> HeaderDefinition("Psychiatrists", "Trusted mental-health specialists near you", R.drawable.category_mental)
            "gastroenterology" -> HeaderDefinition("Gastroenterologists", "Trusted digestive-health specialists near you", R.drawable.category_gastro)
            "pulmonology" -> HeaderDefinition("Pulmonologists", "Trusted lung specialists near you", R.drawable.category_pulmonology)
            "urology" -> HeaderDefinition("Urologists", "Trusted urinary-health specialists near you", R.drawable.category_urology)
            "endocrinology" -> HeaderDefinition("Endocrinologists", "Trusted hormone specialists near you", R.drawable.category_endocrinology)
            "oncology" -> HeaderDefinition("Oncologists", "Trusted cancer-care specialists near you", R.drawable.category_oncology)
            else -> HeaderDefinition(categoryName.ifBlank { "Doctors" }, "Trusted healthcare professionals near you", R.drawable.category_general)
        }
        return DoctorCategoryHeaderUiModel(
            categoryId = id,
            categoryName = categoryName,
            pluralDisplayName = definition.plural,
            supportingText = definition.supporting,
            iconRes = definition.drawable,
            illustrationRes = definition.drawable
        )
    }

    private data class HeaderDefinition(
        val plural: String,
        val supporting: String,
        @DrawableRes val drawable: Int
    )
}

interface DoctorListRepository {
    suspend fun getDoctorsByCategory(categoryId: String): Result<List<DoctorListItemUiModel>>
}

object DoctorListCatalog {
    val cardiologists = listOf(
        DoctorListItemUiModel("3", "Dr. Rohan Mehta", "MBBS, MD (Cardiology), DM", "Interventional Cardiologist", imageRes = R.drawable.doctor_rohan, rating = 4.8, reviewCount = 152, experienceYears = 12, clinicName = "Heart Care Clinic", clinicAddress = "Sector 45", distanceKm = 2.3, consultationFee = 700, availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.AVAILABLE_NOW, "Available Now")),
        DoctorListItemUiModel("4", "Dr. Anjali Verma", "MBBS, MD (Cardiology)", "Consultant Cardiologist", imageRes = R.drawable.doctor_anjali, rating = 4.7, reviewCount = 98, experienceYears = 9, clinicName = "Civic Heart Clinic", clinicAddress = "Sector 12", distanceKm = 3.1, consultationFee = 600, availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.AVAILABLE_NOW, "Available Now")),
        DoctorListItemUiModel("25", "Dr. Arjun Kapoor", "MBBS, MD (Cardiology), FACC", "Senior Cardiologist", imageRes = R.drawable.doctor_arjun, rating = 4.6, reviewCount = 86, experienceYears = 15, clinicName = "Life Line Hospital", clinicAddress = "Sector 44", distanceKm = 3.8, consultationFee = 750, availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.AVAILABLE_SOON, "Available in 20 minutes")),
        DoctorListItemUiModel("26", "Dr. Neha Singh", "MBBS, MD (Cardiology)", "Cardiologist", imageRes = R.drawable.doctor_neha, rating = 4.5, reviewCount = 74, experienceYears = 8, clinicName = "Wellness Heart Centre", clinicAddress = "Sector 29", distanceKm = 4.2, consultationFee = 550, availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.AVAILABLE_TODAY, "Available Today"))
    )
}