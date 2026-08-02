package com.dolo.patient.ui.booking

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppointmentBookingLogicTest {
    private val today = LocalDate.now()
    private val self = AppointmentVisitorUiModel("self", "Rahul Sharma", "Self", "RS", isSelf = true)
    private val family = AppointmentVisitorUiModel("family", "Aarav Sharma", "Child", "AS", age = 9)
    private val clinic = ClinicOptionUiModel("clinic", "Heart Care Clinic", "Sector 45", 2.3)
    private val morning = WalkInSessionUiModel("morning", "Morning", LocalTime.of(9, 0), LocalTime.of(13, 0), LocalTime.of(8, 30), 30, 40, WalkInSessionType.MORNING, true)
    private val doctor = DoctorBookingSummaryUiModel("3", "Dr. Rohan", "MBBS, MD", "Cardiology", 4.8, 152, 12, "Heart Care", 2.3, 700, null)

    private fun state() = AppointmentBookingUiState(
        doctor = doctor, visitors = listOf(self, family), selectedVisitorId = self.id,
        clinics = listOf(clinic), selectedClinicId = clinic.id,
        dates = listOf(AppointmentDateUiModel(today, true)), selectedDate = today,
        sessions = listOf(morning), selectedSessionId = morning.id,
        fees = AppointmentFeeUiModel(700, 20)
    )

    @Test fun selectingFamilyReplacesSelfAndUpdatesDetails() {
        val updated = AppointmentBookingReducer.reduce(state(), AppointmentBookingUiEvent.VisitorSelected(family.id))
        assertEquals(family.id, updated.selectedVisitorId)
        assertEquals("Aarav Sharma", updated.selectedVisitor?.name)
    }

    @Test fun clinicDateAndSessionSelectionsAreApplied() {
        val otherClinic = clinic.copy(id = "other")
        val tomorrow = today.plusDays(1)
        val evening = morning.copy(id = "evening", type = WalkInSessionType.EVENING)
        var current = state().copy(clinics = listOf(clinic, otherClinic), dates = listOf(AppointmentDateUiModel(tomorrow, true)), sessions = listOf(morning, evening))
        current = AppointmentBookingReducer.reduce(current, AppointmentBookingUiEvent.ClinicSelected(otherClinic.id))
        current = AppointmentBookingReducer.reduce(current, AppointmentBookingUiEvent.DateSelected(tomorrow))
        current = AppointmentBookingReducer.reduce(current, AppointmentBookingUiEvent.SessionSelected(evening.id))
        assertEquals(otherClinic.id, current.selectedClinicId)
        assertEquals(tomorrow, current.selectedDate)
        assertEquals(evening.id, current.selectedSessionId)
    }

    @Test fun disabledSessionCannotBeSelected() {
        val full = morning.copy(id = "full", isAvailable = false)
        val updated = AppointmentBookingReducer.reduce(state().copy(sessions = listOf(full), selectedSessionId = null), AppointmentBookingUiEvent.SessionSelected(full.id))
        assertNull(updated.selectedSessionId)
        assertEquals(BookingValidationField.SESSION, updated.validationField)
    }

    @Test fun feeTotalIsCalculatedOutsideComposable() {
        assertEquals(20, AppointmentFeeUiModel(700, 20).totalPayable)
        assertEquals(0, AppointmentFeeUiModel(700, 20, discount = 50).totalPayable)
    }

    @Test fun validationRequiresEverySelection() {
        assertEquals(BookingValidationField.VISITOR, AppointmentBookingLogic.validate(state().copy(selectedVisitorId = null))?.first)
        assertEquals(BookingValidationField.CLINIC, AppointmentBookingLogic.validate(state().copy(selectedClinicId = null))?.first)
        assertEquals(BookingValidationField.DATE, AppointmentBookingLogic.validate(state().copy(selectedDate = null))?.first)
        assertEquals(BookingValidationField.SESSION, AppointmentBookingLogic.validate(state().copy(selectedSessionId = null))?.first)
    }

    @Test fun requestContainsSelectedFamilyProfileAndSession() {
        val request = AppointmentBookingLogic.createRequest(state().copy(selectedVisitorId = family.id))!!
        assertEquals(family.id, request.patientProfileId)
        assertEquals("Aarav Sharma", request.patientName)
        assertEquals(WalkInSessionType.MORNING, request.sessionType)
    }

    @Test fun bookingStatePreventsRepeatedSubmission() {
        assertTrue(state().canConfirm)
        assertFalse(state().copy(isBooking = true).canConfirm)
    }
}