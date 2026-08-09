package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostedPrototypePaymentJsonTest {
    @Test
    fun parsesServerAuthoritativeSyntheticOutcome() {
        val result = HostedPrototypePaymentJson.parse("""{"authoritative":true,"syntheticOnly":true,"realMoneyMoved":false,"transaction":{"id":"10000000-0000-4000-8000-000000000001","syntheticPaymentId":"DLO-PAY-SIM-000123","scenario":"CAPTURE_SUCCESS","status":"CAPTURED_TEST_ONLY","amountMinor":2000,"currency":"INR","bookingEligible":true,"refundStatus":"NOT_REQUESTED","realMoneyMoved":false,"createdAt":"2026-08-09T10:00:00.000Z"}}""")
        assertEquals("DLO-PAY-SIM-000123", result.syntheticPaymentId)
        assertEquals(2000, result.amountMinor)
        assertTrue(result.bookingEligible)
        assertEquals(HostedPrototypePaymentKeys.preferenceKey("CAPTURE_SUCCESS"), HostedPrototypePaymentKeys.preferenceKey("CAPTURE_SUCCESS"))
    }

    @Test
    fun rejectsAnyRealMoneyMarker() {
        val result = runCatching { HostedPrototypePaymentJson.parse("""{"authoritative":true,"syntheticOnly":true,"realMoneyMoved":true,"transaction":{}}""") }
        assertTrue(result.isFailure)
    }

    @Test
    fun failedOutcomeCannotBecomeBookingEligible() {
        val result = HostedPrototypePaymentJson.parse("""{"authoritative":true,"syntheticOnly":true,"realMoneyMoved":false,"transaction":{"id":"10000000-0000-4000-8000-000000000002","syntheticPaymentId":"DLO-PAY-SIM-000124","scenario":"PAYMENT_FAILED","status":"FAILED_TEST_ONLY","amountMinor":2000,"currency":"INR","bookingEligible":false,"refundStatus":"NOT_APPLICABLE","realMoneyMoved":false,"createdAt":"2026-08-09T10:00:00.000Z"}}""")
        assertFalse(result.bookingEligible)
    }
    @Test
    fun parsesSyntheticRefundThatQualifiedAtCaptureTime() {
        val result = HostedPrototypePaymentJson.parse("""{"authoritative":true,"syntheticOnly":true,"realMoneyMoved":false,"transaction":{"id":"10000000-0000-4000-8000-000000000003","syntheticPaymentId":"DLO-PAY-SIM-000125","scenario":"REFUND_AFTER_CAPTURE","status":"REFUNDED_TEST_ONLY","amountMinor":2000,"currency":"INR","bookingEligible":true,"refundStatus":"REFUNDED_TEST_ONLY","realMoneyMoved":false,"createdAt":"2026-08-09T10:00:00.000Z"}}""")
        assertEquals("REFUNDED_TEST_ONLY", result.status)
        assertEquals("REFUNDED_TEST_ONLY", result.refundStatus)
        assertTrue(result.bookingEligible)
    }
}
