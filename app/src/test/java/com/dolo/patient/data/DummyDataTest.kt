package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyDataTest {
    @Test
    fun catalogueHasTwelveIllustratedCategories() {
        assertEquals(12, DummyData.categories.size)
        assertEquals(12, DummyData.categories.map { it.id }.distinct().size)
        assertTrue(DummyData.categories.all { it.imageRes != 0 })
    }

    @Test
    fun everyCategoryHasAtLeastTwoDoctorsForTesting() {
        DummyData.categories.forEach { category ->
            val matchingDoctors = DummyData.doctors.filter { it.specialty == category.name }
            assertTrue(
                "Expected at least two doctors for ${category.name}",
                matchingDoctors.size >= 2
            )
        }
        assertTrue(DummyData.doctors.size >= DummyData.categories.size * 2)
        assertEquals(DummyData.doctors.size, DummyData.doctors.map { it.id }.distinct().size)
    }
}
