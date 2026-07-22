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

    @Test
    fun parsesSeededSelfAndFamilyProfiles() {
        val bootstrap = HostedBootstrapJson.parse(
            """{"authoritative":true,"profile":{"id":"self-1","displayName":"Prototype Patient","relationship":"SELF"},"profiles":[{"id":"self-1","displayName":"Prototype Patient","relationship":"SELF"},{"id":"family-1","displayName":"Prototype Family Member","relationship":"FAMILY"}],"clinic":{"id":"clinic-1","name":"Prototype Clinic","city":"Mumbai","consultationFeeMinor":50000,"doctor":{"name":"Dr. Ananya Mehta","specialty":"General Medicine"}},"sessions":[]}"""
        )

        assertEquals(listOf("SELF", "FAMILY"), bootstrap.profiles.map { it.relationship })
        assertEquals("Prototype Patient", bootstrap.profile.name)
    }

    @Test
    fun bookingRetryKeysAreIndependentForEachPatientProfile() {
        val self = HostedBookingKeys.preferenceKey("session-1", "self-1")
        val family = HostedBookingKeys.preferenceKey("session-1", "family-1")

        assertFalse(self == family)
        assertEquals(self, HostedBookingKeys.preferenceKey("session-1", "self-1"))
        assertEquals("hosted_booking_key_session-1", HostedBookingKeys.legacyPreferenceKey("session-1"))
    }
}
