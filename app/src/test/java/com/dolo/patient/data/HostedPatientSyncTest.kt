package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HostedPatientSyncTest {
    private val snapshot = HostedSyncSnapshot(
        bootstrap = HostedBootstrap(
            profile = HostedProfile("patient-1", "Prototype Patient"),
            clinic = HostedClinic("clinic-1", "Prototype Clinic", "Mumbai", "Dr. Ananya Mehta", "General Medicine", 50000),
            sessions = emptyList()
        ),
        appointments = emptyList(),
        live = emptyList()
    )

    @Test
    fun parsesActiveHostedCommunications() {
        val communications = HostedCommunicationJson.parse(
            """{"communications":[{"id":"message-1","audience":"ALL_PATIENTS","kind":"ADMIN_BROADCAST","title":"Platform update","message":"Appointments are operating normally.","startsOn":"2026-07-21","endsOn":"2026-07-22"}]}"""
        )

        assertEquals(1, communications.size)
        assertEquals("ADMIN_BROADCAST", communications.single().kind)
        assertEquals("Platform update", communications.single().title)
    }
    @Test
    fun parsesExplicitDoctorUnavailableContract() {
        val error = HostedErrorJson.parse(
            """{"error":{"code":"DOCTOR_UNAVAILABLE","message":"This doctor is temporarily unavailable for appointments."}}""",
            409
        )

        assertEquals("DOCTOR_UNAVAILABLE", error.code)
        assertEquals("This doctor is temporarily unavailable for appointments.", error.message)
    }

    @Test
    fun unavailableDoctorClearsOnlyTheHostedSnapshot() {
        val result = HostedSyncStateReducer.failure(
            HostedSyncUiState(snapshot = snapshot, message = "Previously loaded"),
            HostedResult.Failure("This doctor is temporarily unavailable for appointments.", doctorUnavailable = true)
        )

        assertNull(result.snapshot)
        assertTrue(result.error)
        assertTrue(result.doctorUnavailable)
    }

    @Test
    fun ordinaryOfflineFailureRetainsThePreviousHostedSnapshot() {
        val result = HostedSyncStateReducer.failure(
            HostedSyncUiState(snapshot = snapshot, message = "Previously loaded"),
            HostedResult.Failure("Offline. Server data was not changed.")
        )

        assertSame(snapshot, result.snapshot)
        assertTrue(result.error)
        assertFalse(result.doctorUnavailable)
    }
}
