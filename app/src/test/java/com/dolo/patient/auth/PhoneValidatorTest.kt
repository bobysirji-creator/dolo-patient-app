package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneValidatorTest {
    @Test fun acceptsTenDigits() {
        assertTrue(PhoneValidator.isValid("9876543210"))
    }

    @Test fun rejectsShortNumber() {
        assertFalse(PhoneValidator.isValid("12345"))
    }

    @Test fun normalizesCountryCodeAndFormatting() {
        assertEquals("9876543210", PhoneValidator.normalize("+91 98765-43210"))
    }
}
