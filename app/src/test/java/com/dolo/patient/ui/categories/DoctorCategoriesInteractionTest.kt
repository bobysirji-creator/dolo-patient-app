package com.dolo.patient.ui.categories

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorCategoriesInteractionTest {
    @Test
    fun availableCategorySelection_returnsDestinationMetadata() {
        val cardiology = DoctorCategoryCatalog.categories.first { it.id == "cardiology" }
        val selected = DoctorCategorySelection.resolve(DoctorCategoryCatalog.categories, cardiology.id)

        assertNotNull(selected)
        assertEquals(
            DoctorListCategoryArguments("cardiology", "Cardiology"),
            selected!!.toDoctorListArguments()
        )
    }

    @Test
    fun disabledCategorySelection_isBlocked() {
        val disabled = DoctorCategoryCatalog.categories.first().copy(isAvailable = false)
        assertNull(DoctorCategorySelection.resolve(listOf(disabled), disabled.id))
    }

    @Test
    fun retryEvent_reloadsAfterRepositoryFailure() {
        var attempts = 0
        val repository = object : DoctorCategoryRepository {
            override suspend fun getCategories(): List<DoctorCategoryUiModel> {
                attempts += 1
                if (attempts == 1) error("offline")
                return DoctorCategoryCatalog.categories.take(1)
            }
        }
        val viewModel = DoctorCategoriesViewModel(repository, Dispatchers.Unconfined)

        assertEquals("offline", viewModel.uiState.value.errorMessage)
        viewModel.onEvent(DoctorCategoriesUiEvent.Retry)

        assertEquals(2, attempts)
        assertTrue(viewModel.uiState.value.errorMessage == null)
        assertEquals("general-physician", viewModel.uiState.value.visibleCategories.single().id)
    }
}
