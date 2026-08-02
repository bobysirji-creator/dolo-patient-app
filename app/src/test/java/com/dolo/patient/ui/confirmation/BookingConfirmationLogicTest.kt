package com.dolo.patient.ui.confirmation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingConfirmationLogicTest {
    @Test fun tokenAcceptsSupportedRange() {
        assertEquals(1, TokenUiModel(1).number)
        assertEquals(18, TokenUiModel(18).number)
        assertEquals(999, TokenUiModel(999).number)
    }

    @Test fun tokenRejectsUnsupportedRange() {
        assertTrue(runCatching { TokenUiModel(0) }.isFailure)
        assertTrue(runCatching { TokenUiModel(1000) }.isFailure)
    }

    @Test fun queueEstimateFormatsWaitAndPatientsAhead() {
        val estimate = QueueEstimateUiModel(35, 40, 5)
        assertEquals("35-40 mins", estimate.timeLabel)
        assertEquals(5, estimate.patientsAhead)
        assertEquals("It is your turn", QueueEstimateUiModel(0, 0, 0).timeLabel)
    }

    @Test fun confirmedStateRequiresValidToken() {
        val confirmed = BookingConfirmationUiState(isLoading = false, status = BookingStatus.CONFIRMED, token = TokenUiModel(18))
        assertTrue(confirmed.canShowConfirmation)
        assertFalse(confirmed.copy(token = null).canShowConfirmation)
        assertFalse(confirmed.copy(status = BookingStatus.PENDING).canShowConfirmation)
    }

    @Test fun shareSummaryContainsNonSensitiveBookingDetails() {
        val state = BookingConfirmationUiState(
            isLoading = false,
            status = BookingStatus.CONFIRMED,
            token = TokenUiModel(18),
            appointmentDate = "Mon, 03 Aug 2026",
            session = SessionConfirmationUiModel("Morning Session", "9:00 AM - 1:00 PM"),
            doctor = DoctorConfirmationUiModel("3", "Dr. Rohan Mehta", "Cardiologist", null),
            patientName = "Private Patient",
        )
        val summary = BookingConfirmationText.shareSummary(state)
        assertTrue(summary.contains("Token 18"))
        assertTrue(summary.contains("Dr. Rohan Mehta"))
        assertTrue(summary.contains("Morning Session"))
        assertFalse(summary.contains("Private Patient"))
    }
}