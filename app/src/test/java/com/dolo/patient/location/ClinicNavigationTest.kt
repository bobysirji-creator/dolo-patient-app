package com.dolo.patient.location

import org.junit.Assert.assertEquals
import org.junit.Test

class ClinicNavigationTest {
    @Test
    fun buildsApiKeyFreeExternalDirectionsUrl() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=18.938800,72.835400&travelmode=driving",
            ClinicNavigation.directionsUrl(18.9388, 72.8354)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidCoordinates() {
        ClinicNavigation.directionsUrl(91.0, 72.0)
    }
}
