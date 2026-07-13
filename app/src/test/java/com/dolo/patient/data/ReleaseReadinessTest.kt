package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessTest {
    @Test
    fun queueStatusIsReadableForPatients() {
        assertEquals(
            "In consultation",
            ReleaseReadiness.readableStatus(AppointmentStatus.IN_CONSULTATION)
        )
    }

    @Test
    fun recentQueueDataIsNotStale() {
        assertFalse(
            ReleaseReadiness.isQueueStale(
                refreshedAt = 100_000L,
                now = 159_999L
            )
        )
    }

    @Test
    fun oldMissingOrFutureQueueDataIsStale() {
        assertTrue(
            ReleaseReadiness.isQueueStale(
                refreshedAt = 100_000L,
                now = 160_001L
            )
        )
        assertTrue(ReleaseReadiness.isQueueStale(refreshedAt = 0L, now = 1L))
        assertTrue(
            ReleaseReadiness.isQueueStale(
                refreshedAt = 200_000L,
                now = 100_000L
            )
        )
    }

    @Test
    fun patientTextIsSingleLineBoundedAndStorageSafe() {
        val sanitized = ReleaseReadiness.safeSingleLine(
            value = "  Maya|Sharma\nEmergency contact  ",
            maxLength = 18
        )

        assertEquals("Maya/Sharma Emerge", sanitized)
        assertFalse(sanitized.contains('|'))
        assertFalse(sanitized.contains('\n'))
    }

    @Test
    fun negativeMaximumProducesAnEmptyValue() {
        assertEquals(
            "",
            ReleaseReadiness.safeSingleLine("Patient", maxLength = -1)
        )
    }
}
