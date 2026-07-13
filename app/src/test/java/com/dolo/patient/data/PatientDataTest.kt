package com.dolo.patient.data

import com.dolo.patient.data.model.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientDataTest {
    @Test
    fun tokenIsDeterministicAndAdvances() {
        val first = TokenGenerator.forBooking("1", "2026-07-12", 0)
        assertEquals(first + 1, TokenGenerator.forBooking("1", "2026-07-12", 1))
        assertTrue(first >= 10)
    }

    @Test
    fun appointmentRoundTripsWithSelectedPatient() {
        val appointment = Appointment(
            id = "1",
            doctorId = "2",
            doctorName = "Dr Test",
            clinicName = "Clinic",
            appointmentDate = "2026-07-12",
            session = Session.MORNING,
            tokenNumber = 18,
            rescheduleUsed = true,
            patientName = "Maya Sharma"
        )

        assertEquals(
            appointment,
            AppointmentCodec.decode(AppointmentCodec.encode(appointment))
        )
    }

    @Test
    fun legacyAppointmentDefaultsNewFields() {
        val legacy = "1|2|Dr Test|Clinic|2026-07-12|MORNING|18|BOOKED"
        val decoded = AppointmentCodec.decode(legacy)!!

        assertFalse(decoded.rescheduleUsed)
        assertEquals("Rahul Sharma", decoded.patientName)
    }

    @Test
    fun malformedAppointmentIsIgnored() {
        assertNull(AppointmentCodec.decode("broken"))
    }

    @Test
    fun queueCalculatesPatientsAheadAndEstimate() {
        val appointment = Appointment("1", "2", "Dr Test", "Clinic", "2026-07-12", Session.MORNING, 18)
        val queue = QueueCalculator.snapshot(appointment, 12, 100)

        assertEquals(5, queue.patientsAhead)
        assertEquals(60, queue.estimatedMinutes)
        assertEquals(AppointmentStatus.WAITING, queue.status)
    }

    @Test
    fun queueMarksTurnAtPatientToken() {
        val appointment = Appointment("1", "2", "Dr Test", "Clinic", "2026-07-12", Session.MORNING, 18)
        val queue = QueueCalculator.snapshot(appointment, 18, 100)

        assertEquals(0, queue.patientsAhead)
        assertEquals(AppointmentStatus.IN_CONSULTATION, queue.status)
    }

    @Test
    fun missedStatusOverridesQueueProgress() {
        val appointment = Appointment(
            "1",
            "2",
            "Dr Test",
            "Clinic",
            "2026-07-12",
            Session.MORNING,
            18,
            AppointmentStatus.MISSED
        )

        assertEquals(
            AppointmentStatus.MISSED,
            QueueCalculator.snapshot(appointment, 12).status
        )
    }
}
