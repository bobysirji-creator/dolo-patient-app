package com.dolo.patient.ui.categories

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorCategorySearchTest {
    private val categories = DoctorCategoryCatalog.categories

    @Test
    fun blankQuery_returnsEveryCategoryInOriginalOrder() {
        assertEquals(categories, DoctorCategorySearch.filter(categories, "   "))
    }

    @Test
    fun categoryNameQuery_isCaseInsensitive() {
        val result = DoctorCategorySearch.filter(categories, "CARDIO")
        assertEquals(listOf("cardiology"), result.map { it.id })
    }

    @Test
    fun healthNeedAlias_findsTheRelevantSpecialty() {
        assertEquals(listOf("orthopedics"), DoctorCategorySearch.filter(categories, "bones").map { it.id })
        assertEquals(listOf("pulmonology"), DoctorCategorySearch.filter(categories, "breathing").map { it.id })
        assertEquals(listOf("endocrinology"), DoctorCategorySearch.filter(categories, "diabetes").map { it.id })
    }

    @Test
    fun multipleTerms_mustAllMatchTheSameCategory() {
        val result = DoctorCategorySearch.filter(categories, "female health")
        assertEquals(listOf("gynecology"), result.map { it.id })
    }

    @Test
    fun unknownQuery_returnsNoResults() {
        assertTrue(DoctorCategorySearch.filter(categories, "not-a-specialty").isEmpty())
    }

    @Test
    fun catalogue_hasStableUniqueIdsAndRequiredCounts() {
        assertEquals(16, categories.size)
        assertEquals(16, categories.map { it.id }.toSet().size)
        assertEquals(125, categories.first { it.id == "general-physician" }.doctorCount)
        assertEquals(30, categories.first { it.id == "oncology" }.doctorCount)
    }
}
