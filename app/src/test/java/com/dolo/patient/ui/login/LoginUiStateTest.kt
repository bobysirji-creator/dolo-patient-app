package com.dolo.patient.ui.login

import com.dolo.patient.auth.stage49aPatientEnrollmentReadiness
import com.dolo.patient.auth.stage50aPatientEnrollmentActivationRequirements
import org.junit.Assert.assertEquals
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

    @Test
    fun acceptedStage49AFoundationAndStage50AGatesRemainVisiblyDisabled() {
        val readiness = stage49aPatientEnrollmentReadiness()

        assertEquals(
            RegistrationReadinessStatus.ActivationGatesBlocked,
            registrationReadinessStatus(
                readiness,
                stage50aPatientEnrollmentActivationRequirements()
            )
        )
        assertEquals("DLO-PAT-NNNNNN", readiness.publicIdPolicy.format)
        assertFalse(readiness.publicIdPolicy.locationEmbedded)
    }

    @Test
    fun missingAuthoritativeReadinessFailsClosed() {
        assertEquals(
            RegistrationReadinessStatus.Unavailable,
            registrationReadinessStatus(null, null)
        )
    }

    @Test
    fun unsafeOrIncompleteActivationRequirementsFailClosed() {
        val unsafe = stage50aPatientEnrollmentActivationRequirements().copy(
            activationDecision = "READY"
        )
        assertEquals(
            RegistrationReadinessStatus.Unavailable,
            registrationReadinessStatus(stage49aPatientEnrollmentReadiness(), unsafe)
        )
    }

    @Test
    fun allSevenActivationGatesHavePatientFriendlyLabels() {
        val requirements = stage50aPatientEnrollmentActivationRequirements()
        assertEquals(7, requirements.gates.size)
        assertTrue(
            requirements.gates.all {
                activationGateLabel(it.key) != "Unrecognized safety requirement"
            }
        )
    }
}
