package com.dolo.patient.ui.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpRulesTest {
    @Test
    fun digits_keepsOnlyFirstSixDigits() {
        assertEquals("123456", OtpRules.digits("12a345678"))
    }

    @Test
    fun updateDigit_buildsSequentialOtp() {
        var otp = ""
        "123456".forEachIndexed { index, digit ->
            otp = OtpRules.updateDigit(otp, index, digit.toString())
        }
        assertEquals("123456", otp)
    }

    @Test
    fun backspace_removesCurrentOrPreviousDigit() {
        assertEquals("12345", OtpRules.backspace("123456", 5))
        assertEquals("1234", OtpRules.backspace("12345", 5))
    }

    @Test
    fun countdown_formatsMinutesAndSeconds() {
        assertEquals("00:45", OtpRules.countdown(45))
        assertEquals("01:01", OtpRules.countdown(61))
        assertEquals("00:00", OtpRules.countdown(-1))
    }

    @Test
    fun state_enablesOnlyAllowedActions() {
        val complete = OtpVerificationUiState(otp = "123456")
        assertTrue(complete.canVerify)
        assertFalse(complete.canResend)

        val expired = complete.copy(secondsRemaining = 0, isExpired = true)
        assertFalse(expired.canVerify)
        assertTrue(expired.canResend)
    }
}
