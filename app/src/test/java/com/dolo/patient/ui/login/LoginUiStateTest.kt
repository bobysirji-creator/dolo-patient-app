package com.dolo.patient.ui.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUiStateTest {
    @Test
    fun `ten digit number enables mocked OTP request`() {
        val state = LoginUiState(phoneNumber = "9876543210")

        assertTrue(state.isPhoneValid)
        assertTrue(state.canSendOtp)
    }

    @Test
    fun `short number remains invalid`() {
        val state = LoginUiState(phoneNumber = "98765")

        assertFalse(state.isPhoneValid)
        assertFalse(state.canSendOtp)
    }

    @Test
    fun `loading prevents duplicate OTP request`() {
        val state = LoginUiState(
            phoneNumber = "9876543210",
            isLoading = true
        )

        assertTrue(state.isPhoneValid)
        assertFalse(state.canSendOtp)
    }
}