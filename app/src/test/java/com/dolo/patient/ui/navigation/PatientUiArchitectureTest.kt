package com.dolo.patient.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientUiArchitectureTest {
    @Test fun `primary navigation stays compact and task focused`() {
        assertEquals(listOf("Home", "Book", "Appointments"), PatientUiArchitecture.primaryDestinations)
        assertFalse(PatientUiArchitecture.prototypeControlsOnPrimaryNavigation)
    }

    @Test fun `secondary features remain grouped and discoverable`() {
        val destinations = PatientUiArchitecture.accountGroups.flatMap { it.destinations }
        assertTrue("Profile & family" in destinations)
        assertTrue("Help & support" in destinations)
        assertTrue("App status & diagnostics" in destinations)
        assertFalse("Profile & family" in PatientUiArchitecture.primaryDestinations)
        assertFalse("History" in PatientUiArchitecture.primaryDestinations)
    }

    @Test fun `shared navigation accessibility policy remains bounded`() {
        assertTrue(PatientUiArchitecture.minimumInteractiveTargetDp >= 48)
        assertFalse(PatientUiArchitecture.directionalScreenMotionEnabled)
    }
}
