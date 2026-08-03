package com.dolo.patient.ui.login

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.auth.AuthRepository
import com.dolo.patient.auth.PatientEnrollmentReadiness
import com.dolo.patient.auth.PatientEnrollmentActivationRequirements
import com.dolo.patient.auth.PhoneValidator
import com.dolo.patient.auth.stage49aPatientEnrollmentReadiness
import com.dolo.patient.auth.stage50aPatientEnrollmentActivationRequirements
import java.util.concurrent.Executors

enum class LoginError {
    InvalidPhone,
    Network
}

enum class RegistrationReadinessStatus {
    Checking,
    ActivationGatesBlocked,
    Unavailable
}

fun registrationReadinessStatus(
    readiness: PatientEnrollmentReadiness?,
    requirements: PatientEnrollmentActivationRequirements?
): RegistrationReadinessStatus =
    if (
        readiness != null &&
        requirements != null &&
        readiness == stage49aPatientEnrollmentReadiness(readiness.demoPatientLogin) &&
        requirements == stage50aPatientEnrollmentActivationRequirements()
    ) {
        RegistrationReadinessStatus.ActivationGatesBlocked
    } else {
        RegistrationReadinessStatus.Unavailable
    }

fun activationGateLabel(key: String): String = when (key) {
    "MANAGED_OTP_PROVIDER" -> "Managed OTP provider"
    "DISTRIBUTED_ABUSE_PROTECTION" -> "Distributed abuse protection"
    "VERSIONED_LEGAL_CONSENTS" -> "Versioned Terms, Privacy and Health Data consent"
    "ACCOUNT_RECOVERY_AND_DUPLICATE_POLICY" -> "Account recovery and duplicate-account policy"
    "RETENTION_CORRECTION_AND_DELETION_POLICY" -> "Data retention, correction and deletion policy"
    "INDIA_PRODUCTION_SECURITY_REVIEW" -> "India production security review"
    "ATOMIC_ENROLLMENT_TRANSACTION_REVIEW" -> "Atomic enrollment transaction review"
    else -> "Unrecognized safety requirement"
}

data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val accountCreationNoticeVisible: Boolean = false,
    val registrationStatus: RegistrationReadinessStatus = RegistrationReadinessStatus.Checking,
    val enrollmentReadiness: PatientEnrollmentReadiness? = null,
    val activationRequirements: PatientEnrollmentActivationRequirements? = null,
    val otpRequestedFor: String? = null
) {
    val isPhoneValid: Boolean get() = PhoneValidator.isValid(phoneNumber)
    val canSendOtp: Boolean get() = isPhoneValid && !isLoading
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())


    private fun refreshEnrollmentReadiness() {
        uiState = uiState.copy(registrationStatus = RegistrationReadinessStatus.Checking)
        executor.execute {
            val readiness = repository.enrollmentReadiness().getOrNull()
            val requirements = repository.enrollmentActivationRequirements().getOrNull()
            main.post {
                val status = registrationReadinessStatus(readiness, requirements)
                uiState = uiState.copy(
                    registrationStatus = status,
                    enrollmentReadiness = readiness.takeIf {
                        status == RegistrationReadinessStatus.ActivationGatesBlocked
                    },
                    activationRequirements = requirements.takeIf {
                        status == RegistrationReadinessStatus.ActivationGatesBlocked
                    }
                )
            }
        }
    }

    fun updatePhoneNumber(value: String) {
        uiState = uiState.copy(
            phoneNumber = normalizePhoneInput(value),
            error = null,
            accountCreationNoticeVisible = false
        )
    }

    private fun normalizePhoneInput(value: String): String {
        val digits = value.filter(Char::isDigit)
        return if (digits.length > 10 && digits.startsWith("91")) {
            digits.drop(2).take(10)
        } else {
            digits.take(10)
        }
    }

    fun sendOtp() {
        if (uiState.isLoading) return
        if (!uiState.isPhoneValid) {
            uiState = uiState.copy(error = LoginError.InvalidPhone)
            return
        }

        val phone = uiState.phoneNumber
        uiState = uiState.copy(isLoading = true, error = null)
        executor.execute {
            val result = repository.requestOtp(phone)
            main.post {
                uiState = result.fold(
                    onSuccess = {
                        uiState.copy(
                            isLoading = false,
                            otpRequestedFor = phone
                        )
                    },
                    onFailure = { failure ->
                        uiState.copy(
                            isLoading = false,
                            error = if (failure is IllegalArgumentException) {
                                LoginError.InvalidPhone
                            } else {
                                LoginError.Network
                            }
                        )
                    }
                )
            }
        }
    }

    fun consumeOtpNavigation() {
        uiState = uiState.copy(otpRequestedFor = null)
    }

    fun showAccountCreationNotice() {
        uiState = uiState.copy(accountCreationNoticeVisible = true)
        refreshEnrollmentReadiness()
    }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}

class LoginViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repository) as T
    }
}
