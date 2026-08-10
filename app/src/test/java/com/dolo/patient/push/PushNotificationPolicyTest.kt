package com.dolo.patient.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PushNotificationPolicyTest {
    @Test
    fun mapsOnlyBoundedAppointmentRoutes() {
        assertEquals("queue/abc-123", PushNotificationPolicy.destinationFor("/appointments/abc-123"))
        assertNull(PushNotificationPolicy.destinationFor("https://example.com"))
        assertNull(PushNotificationPolicy.destinationFor("/admin"))
        assertNull(PushNotificationPolicy.destinationFor("/appointments/a/b"))
    }

    @Test
    fun fingerprintsTokensWithoutRetainingTheirValue() {
        val token = "firebase-registration-token-for-test"
        val fingerprint = PushNotificationPolicy.tokenFingerprint(token)
        assertEquals(64, fingerprint.length)
        assertNotEquals(token, fingerprint)
        assertThrows(IllegalArgumentException::class.java) {
            PushNotificationPolicy.tokenFingerprint("short")
        }
    }

    @Test
    fun notificationIdsAreStablePerMessageAndDestination() {
        val first = PushNotificationPolicy.notificationId("message-1", "queue/a")
        assertEquals(first, PushNotificationPolicy.notificationId("message-1", "queue/a"))
        assertNotEquals(first, PushNotificationPolicy.notificationId("message-2", "queue/a"))
    }
}
