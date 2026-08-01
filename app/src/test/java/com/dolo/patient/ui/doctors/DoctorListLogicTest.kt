package com.dolo.patient.ui.doctors

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorListLogicTest {
    private val doctors = DoctorListCatalog.cardiologists

    @Test
    fun search_matches_name_clinic_and_qualification() {
        assertEquals(listOf("3"), DoctorListLogic.apply(doctors, "Rohan", DoctorFilterState(), DoctorSortOption.RECOMMENDED).map { it.id })
        assertEquals(listOf("4"), DoctorListLogic.apply(doctors, "Civic", DoctorFilterState(), DoctorSortOption.RECOMMENDED).map { it.id })
        assertEquals(listOf("25"), DoctorListLogic.apply(doctors, "FACC", DoctorFilterState(), DoctorSortOption.RECOMMENDED).map { it.id })
    }

    @Test
    fun sorting_supports_rating_distance_and_fee_orders() {
        assertEquals("3", DoctorListLogic.apply(doctors, "", DoctorFilterState(), DoctorSortOption.RATING).first().id)
        assertEquals("3", DoctorListLogic.apply(doctors, "", DoctorFilterState(), DoctorSortOption.DISTANCE).first().id)
        assertEquals("26", DoctorListLogic.apply(doctors, "", DoctorFilterState(), DoctorSortOption.FEES_LOW_TO_HIGH).first().id)
        assertEquals("25", DoctorListLogic.apply(doctors, "", DoctorFilterState(), DoctorSortOption.FEES_HIGH_TO_LOW).first().id)
        assertEquals("25", DoctorListLogic.apply(doctors, "", DoctorFilterState(), DoctorSortOption.EXPERIENCE).first().id)
    }

    @Test
    fun availability_fee_and_experience_filters_combine() {
        val filters = DoctorFilterState(
            availability = DoctorAvailabilityType.AVAILABLE_NOW,
            minimumFee = 500,
            maximumFee = 650,
            minimumExperience = 5
        )
        assertEquals(listOf("4"), DoctorListLogic.apply(doctors, "", filters, DoctorSortOption.RECOMMENDED).map { it.id })
    }

    @Test
    fun category_arguments_resolve_dynamic_header() {
        val header = DoctorCategoryHeaders.resolve("cardiology", "Cardiology")
        assertEquals("Cardiologists", header.pluralDisplayName)
        assertTrue(header.supportingText.contains("heart"))
        assertEquals("cardiology", header.categoryId)
    }

    @Test
    fun favourite_selection_booking_and_callback_events_preserve_intent() {
        val favourite = DoctorListUiEvent.FavouriteClicked("3")
        val selected = DoctorListUiEvent.DoctorSelected("4")
        val booking = DoctorListUiEvent.BookNowClicked("25")
        assertEquals("3", favourite.doctorId)
        assertEquals("4", selected.doctorId)
        assertEquals("25", booking.doctorId)
        assertEquals(DoctorListUiEvent.RequestCallbackClicked, DoctorListUiEvent.RequestCallbackClicked)
        assertFalse(doctors.first().isFavourite)
        assertTrue(doctors.first().copy(isFavourite = true).isFavourite)
    }
}