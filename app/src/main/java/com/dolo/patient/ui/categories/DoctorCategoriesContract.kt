package com.dolo.patient.ui.categories

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dolo.patient.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DoctorCategoryUiModel(
    val id: String,
    val name: String,
    val doctorCount: Int,
    @DrawableRes val imageRes: Int,
    val contentDescription: String,
    val keywords: Set<String> = emptySet(),
    val isAvailable: Boolean = true,
    val remoteIconUrl: String? = null,
    val displayOrder: Int = 0,
    val isFeatured: Boolean = false,
    val nearbyDoctorCount: Int? = null,
    val onlineConsultationAvailable: Boolean = false
)

data class DoctorCategoriesUiState(
    val query: String = "",
    val categories: List<DoctorCategoryUiModel> = emptyList(),
    val visibleCategories: List<DoctorCategoryUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && categories.isEmpty()
    val hasNoSearchResults: Boolean
        get() = !isLoading && errorMessage == null && categories.isNotEmpty() && visibleCategories.isEmpty()
}

sealed interface DoctorCategoriesUiEvent {
    data class SearchChanged(val query: String) : DoctorCategoriesUiEvent
    data object ClearSearch : DoctorCategoriesUiEvent
    data object Retry : DoctorCategoriesUiEvent
    data class CategorySelected(val categoryId: String) : DoctorCategoriesUiEvent
}

interface DoctorCategoryRepository {
    suspend fun getCategories(): List<DoctorCategoryUiModel>
}

class FakeDoctorCategoryRepository(
    private val categories: List<DoctorCategoryUiModel> = DoctorCategoryCatalog.categories,
    private val loadDelayMillis: Long = 180,
    private val failureMessage: String? = null
) : DoctorCategoryRepository {
    override suspend fun getCategories(): List<DoctorCategoryUiModel> {
        if (loadDelayMillis > 0) delay(loadDelayMillis)
        failureMessage?.let { error(it) }
        return categories
    }
}

object DoctorCategorySearch {
    fun filter(
        categories: List<DoctorCategoryUiModel>,
        query: String
    ): List<DoctorCategoryUiModel> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        if (terms.isEmpty()) return categories
        return categories.filter { category ->
            val searchableText = buildString {
                append(category.name.lowercase())
                append(' ')
                append(category.keywords.joinToString(" ").lowercase())
            }
            terms.all(searchableText::contains)
        }
    }
}

object DoctorCategoryCatalog {
    val categories = listOf(
        DoctorCategoryUiModel("general-physician", "General Physician", 125, R.drawable.category_general, "General physician medical illustration", setOf("general doctor", "family doctor", "primary care", "fever", "common illness")),
        DoctorCategoryUiModel("pediatrics", "Pediatrics", 98, R.drawable.category_child, "Pediatrics medical illustration", setOf("child", "children", "kids", "baby", "pediatrician")),
        DoctorCategoryUiModel("dermatology", "Dermatology", 76, R.drawable.category_skin, "Dermatology medical illustration", setOf("skin", "hair", "nails", "dermatologist")),
        DoctorCategoryUiModel("gynecology", "Gynecology", 82, R.drawable.category_gyn, "Gynecology medical illustration", setOf("women", "pregnancy", "female health", "gynecologist")),
        DoctorCategoryUiModel("orthopedics", "Orthopedics", 68, R.drawable.category_ortho, "Orthopedics medical illustration", setOf("bones", "joints", "fracture", "ortho")),
        DoctorCategoryUiModel("cardiology", "Cardiology", 64, R.drawable.category_cardio, "Cardiology medical illustration", setOf("heart", "cardiologist", "blood pressure")),
        DoctorCategoryUiModel("ent", "ENT", 58, R.drawable.category_ent, "Ear nose and throat medical illustration", setOf("ear", "nose", "throat", "otolaryngology")),
        DoctorCategoryUiModel("ophthalmology", "Ophthalmology", 52, R.drawable.category_eye, "Ophthalmology medical illustration", setOf("eye", "eyes", "vision", "ophthalmologist")),
        DoctorCategoryUiModel("dentistry", "Dentistry", 60, R.drawable.category_dental, "Dentistry medical illustration", setOf("teeth", "tooth", "oral", "dentist")),
        DoctorCategoryUiModel("psychiatry", "Psychiatry", 45, R.drawable.category_mental, "Psychiatry medical illustration", setOf("mental health", "mind", "anxiety", "depression", "psychiatrist")),
        DoctorCategoryUiModel("gastroenterology", "Gastroenterology", 46, R.drawable.category_gastro, "Digestive system medical illustration", setOf("stomach", "digestion", "gut", "liver", "gastro")),
        DoctorCategoryUiModel("neurology", "Neurology", 40, R.drawable.category_neuro, "Neurology medical illustration", setOf("brain", "nerves", "neurologist", "headache")),
        DoctorCategoryUiModel("pulmonology", "Pulmonology", 38, R.drawable.category_pulmonology, "Lungs medical illustration", setOf("lungs", "breathing", "respiratory", "chest")),
        DoctorCategoryUiModel("urology", "Urology", 32, R.drawable.category_urology, "Urinary system medical illustration", setOf("kidney", "urinary", "bladder", "urologist")),
        DoctorCategoryUiModel("endocrinology", "Endocrinology", 28, R.drawable.category_endocrinology, "Thyroid medical illustration", setOf("thyroid", "diabetes", "hormones", "endocrinologist")),
        DoctorCategoryUiModel("oncology", "Oncology", 30, R.drawable.category_oncology, "Cancer care medical illustration", setOf("cancer", "tumor", "oncologist"))
    )
}

data class DoctorListCategoryArguments(
    val categoryId: String,
    val categoryName: String
)

fun DoctorCategoryUiModel.toDoctorListArguments() = DoctorListCategoryArguments(id, name)

object DoctorCategorySelection {
    fun resolve(categories: List<DoctorCategoryUiModel>, categoryId: String): DoctorCategoryUiModel? =
        categories.firstOrNull { it.id == categoryId }?.takeIf { it.isAvailable }
}
class DoctorCategoriesViewModel(
    private val repository: DoctorCategoryRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    var uiState = androidx.compose.runtime.mutableStateOf(DoctorCategoriesUiState())
        private set

    init {
        loadCategories()
    }

    fun onEvent(event: DoctorCategoriesUiEvent) {
        when (event) {
            is DoctorCategoriesUiEvent.SearchChanged -> updateSearch(event.query)
            DoctorCategoriesUiEvent.ClearSearch -> updateSearch("")
            DoctorCategoriesUiEvent.Retry -> loadCategories()
            is DoctorCategoriesUiEvent.CategorySelected -> Unit
        }
    }

    fun findCategory(categoryId: String): DoctorCategoryUiModel? =
        DoctorCategorySelection.resolve(uiState.value.categories, categoryId)

    private fun updateSearch(query: String) {
        val state = uiState.value
        uiState.value = state.copy(
            query = query,
            visibleCategories = DoctorCategorySearch.filter(state.categories, query)
        )
    }

    private fun loadCategories() {
        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(dispatcher) {
            runCatching { repository.getCategories() }
                .onSuccess { categories ->
                    uiState.value = uiState.value.copy(
                        categories = categories,
                        visibleCategories = DoctorCategorySearch.filter(categories, uiState.value.query),
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(
                        categories = emptyList(),
                        visibleCategories = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "We could not load doctor categories."
                    )
                }
        }
    }
}

class DoctorCategoriesViewModelFactory(
    private val repository: DoctorCategoryRepository = FakeDoctorCategoryRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DoctorCategoriesViewModel(repository) as T
}
