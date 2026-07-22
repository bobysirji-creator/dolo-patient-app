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
            """{"authoritative":true,"profile":{"id":"self-1","displayName":"Prototype Patient","relationship":"SELF"},"profiles":[{"id":"self-1","displayName":"Prototype Patient","relationship":"SELF"},{"id":"family-1","displayName":"Prototype Family Member","relationship":"FAMILY"}],"clinic":{"id":"clinic-1","name":"Prototype Clinic","city":"Mumbai","consultationFeeMinor":50000,"doctor":{"name":"Dr. Ananya Mehta","specialty":"General Medicine"}},"sessions":[],"rescheduleWindowDays":10,"rescheduleSessions":[{"id":"later-1","serviceDate":"2026-07-24","name":"MORNING","startsAt":"09:00:00","endsAt":"11:00:00","availableTokens":5,"bookingEnabled":true}]}"""
        )

        assertEquals(listOf("SELF", "FAMILY"), bootstrap.profiles.map { it.relationship })
        assertEquals("Prototype Patient", bootstrap.profile.name)
        assertEquals(10, bootstrap.rescheduleWindowDays)
        assertEquals(listOf("later-1"), bootstrap.rescheduleSessions.map { it.id })
    }

    @Test
    fun bookingRetryKeysAreIndependentForEachPatientProfile() {
        val self = HostedBookingKeys.preferenceKey("session-1", "self-1")
        val family = HostedBookingKeys.preferenceKey("session-1", "family-1")

        assertFalse(self == family)
        assertEquals(self, HostedBookingKeys.preferenceKey("session-1", "self-1"))
        assertEquals("hosted_booking_key_session-1", HostedBookingKeys.legacyPreferenceKey("session-1"))
    }
    @Test
    fun missedAppointmentOffersOnlyLaterEnabledReplacementSessions() {
        val appointment = HostedAppointment("appointment-1", "session-old", "Doctor", "Clinic", "Patient", "2026-07-22", "MORNING", 3, "ABSENT")
        val sessions = listOf(
            HostedSession("session-old", "2026-07-22", "MORNING", "09:00", "11:00", 2, true),
            HostedSession("session-closed", "2026-07-23", "MORNING", "09:00", "11:00", 2, false),
            HostedSession("session-new", "2026-07-23", "EVENING", "17:00", "19:00", 2, true),
            HostedSession("session-too-late", "2026-08-10", "MORNING", "09:00", "11:00", 2, true)
        )

        assertEquals(listOf("session-new"), HostedReschedulePolicy.eligibleSessions(appointment, sessions, 10).map { it.id })
        assertTrue(HostedReschedulePolicy.eligibleSessions(appointment.copy(rescheduleUsed = true), sessions, 10).isEmpty())
        assertTrue(HostedReschedulePolicy.eligibleSessions(appointment.copy(status = "COMPLETED"), sessions, 10).isEmpty())
    }

    @Test
    fun rescheduleRetryKeyIsStableForOneAppointmentAndTarget() {
        assertEquals(
            HostedRescheduleKeys.preferenceKey("appointment-1", "session-2"),
            HostedRescheduleKeys.preferenceKey("appointment-1", "session-2")
        )
        assertFalse(
            HostedRescheduleKeys.preferenceKey("appointment-1", "session-2") ==
                HostedRescheduleKeys.preferenceKey("appointment-1", "session-3")
        )
    }
    @Test
    fun parsesClinicReceiptFieldsFromHostedHistory() {
        val appointment = HostedAppointmentJson.parse(
            """{"appointments":[{"id":"appointment-1","clinicSessionId":"session-1","clinicName":"Prototype Clinic","patientName":"Prototype Patient","serviceDate":"2026-07-22","session":"MORNING","tokenNumber":4,"status":"COMPLETED","clinicFeeStatus":"PAID","clinicFeeAmountMinor":50000,"receiptNumber":"DLO-20260722-0004","rescheduleUsed":false,"rescheduledFromAppointmentId":null}]}"""
        ).single()

        assertEquals("PAID", appointment.clinicFeeStatus)
        assertEquals(50000, appointment.clinicFeeAmountMinor)
        assertEquals("DLO-20260722-0004", appointment.receiptNumber)
    }

    @Test
    fun receiptPresentationSeparatesClinicPaymentFromDoloPayments() {
        val pending = HostedAppointment("a1", "s1", "Doctor", "Clinic", "Patient", "2026-07-22", "MORNING", 1, "BOOKED")
        val paid = pending.copy(clinicFeeStatus = "PAID", clinicFeeAmountMinor = 50000, receiptNumber = "DLO-1")
        val waived = pending.copy(clinicFeeStatus = "WAIVED", receiptNumber = "DLO-2")

        assertTrue(HostedReceiptPresentation.text(pending).contains("Pending at clinic"))
        assertTrue(HostedReceiptPresentation.text(paid).contains("INR 500 paid directly at clinic"))
        assertTrue(HostedReceiptPresentation.text(paid).contains("Not an online DO-LO payment"))
        assertTrue(HostedReceiptPresentation.text(waived).contains("Waived at clinic"))
    }
    @Test
    fun hostedHomeShowsOnlyActiveAppointmentsInStableOrder() {
        val later = HostedAppointment("a2", "s2", "Doctor", "Clinic", "Family", "2026-07-24", "EVENING", 2, "BOOKED")
        val earlier = HostedAppointment("a1", "s1", "Doctor", "Clinic", "Patient", "2026-07-23", "MORNING", 4, "WAITING")
        val completed = HostedAppointment("a3", "s1", "Doctor", "Clinic", "Patient", "2026-07-22", "MORNING", 1, "COMPLETED")
        val live = HostedLiveQueue("a1", 4, 2, 1, 12, "WAITING", "COUNTING_DOWN")
        val update = HostedCommunication("c1", "ALL_PATIENTS", "ADMIN_BROADCAST", "Clinic update", "Open normally", "2026-07-22", "2026-07-23")
        val doctorUpdate = HostedCommunication("c2", "CLINIC_PATIENTS", "DOCTOR_AVAILABILITY", "Doctor update", "Running late", "2026-07-22", "2026-07-23")
        val homeSnapshot = snapshot.copy(
            appointments = listOf(later, completed, earlier),
            live = listOf(live),
            communications = listOf(update, doctorUpdate)
        )

        assertEquals(listOf("a1", "a2"), HostedHomePresentation.activeAppointments(homeSnapshot).map { it.id })
        assertEquals(live, HostedHomePresentation.liveQueue(homeSnapshot, "a1"))
        assertEquals(listOf(doctorUpdate, update), HostedHomePresentation.homeCommunications(homeSnapshot))
    }

    @Test
    fun parsesPendingHostedReviewAndKeepsOneRetryKeyPerAppointment() {
        val review = HostedReviewJson.parse(
            """{"reviews":[{"id":"review-1","appointmentId":"appointment-1","patientName":"Prototype Patient","doctorName":"Dr. Ananya Mehta","clinicName":"Prototype Clinic","rating":5,"comment":"Very helpful.","status":"PENDING","submittedAt":"2026-07-22T08:00:00.000Z"}]}"""
        ).single()

        assertEquals(5, review.rating)
        assertEquals("PENDING", review.status)
        assertEquals("appointment-1", review.appointmentId)
        assertEquals(
            HostedReviewKeys.preferenceKey("appointment-1"),
            HostedReviewKeys.preferenceKey("appointment-1")
        )
        assertFalse(HostedReviewKeys.preferenceKey("appointment-1") == HostedReviewKeys.preferenceKey("appointment-2"))
    }}
