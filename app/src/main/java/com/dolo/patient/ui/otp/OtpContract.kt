package com.dolo.patient.ui.otp

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.auth.AuthRepository
import com.dolo.patient.auth.PatientSession
import java.util.concurrent.Executors

interface OtpRepository {
    fun verifyOtp(phone: String, otp: String): Result<PatientSession>
    fun resendOtp(phone: String): Result<Unit>
}

class AuthOtpRepository(
    private val authRepository: AuthRepository
) : OtpRepository {
    override fun verifyOtp(phone: String, otp: String): Result<PatientSession> {
        return authRepository.verifyOtp(phone, otp)
    }

    override fun resendOtp(phone: String): Result<Unit> {
        return authRepository.requestOtp(phone)
    }
}

enum class OtpError {
    Incomplete,
    Invalid,
    Expired,
    Network,
    TooManyAttempts
}

sealed interface OtpVerificationUiEvent {
    data class DigitChanged(val index: Int, val value: String) : OtpVerificationUiEvent
    data class OtpPasted(val value: String) : OtpVerificationUiEvent
    data class Backspace(val index: Int) : OtpVerificationUiEvent
    data object Verify : OtpVerificationUiEvent
    data object Resend : OtpVerificationUiEvent
    data object EditNumber : OtpVerificationUiEvent
}

data class OtpVerificationUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val secondsRemaining: Int = OtpRules.EXPIRY_SECONDS,
    val isExpired: Boolean = false,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val error: OtpError? = null,
    val confirmationMessageVisible: Boolean = false,
    val failedAttempts: Int = 0,
    val verifiedSession: PatientSession? = null,
    val editRequested: Boolean = false
) {
    val isComplete: Boolean get() = otp.length == OtpRules.OTP_LENGTH
    val canVerify: Boolean get() = isComplete && !isVerifying && !isResending
    val canResend: Boolean get() = isExpired && !isVerifying && !isResending
}

object OtpRules {
    const val OTP_LENGTH = 6
    const val EXPIRY_SECONDS = 45
    const val MAX_FAILED_ATTEMPTS = 5

    fun digits(value: String): String = value.filter(Char::isDigit).take(OTP_LENGTH)

    fun updateDigit(current: String, index: Int, value: String): String {
        if (index !in 0 until OTP_LENGTH) return digits(current)
        val slots = digits(current).padEnd(OTP_LENGTH, ' ').toCharArray()
        val digit = value.filter(Char::isDigit).lastOrNull()
        slots[index] = digit ?: ' '
        return slots.concatToString().replace(" ", "")
    }

    fun backspace(current: String, index: Int): String {
        if (current.isEmpty()) return current
        val target = if (index in current.indices) index else (index - 1).coerceAtLeast(0)
        return current.removeRange(target, (target + 1).coerceAtMost(current.length))
    }

    fun countdown(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return "%02d:%02d".format(safe / 60, safe % 60)
    }
}

class OtpVerificationViewModel(
    private val repository: OtpRepository
) : ViewModel() {
    var uiState by mutableStateOf(OtpVerificationUiState())
        private set

    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (uiState.secondsRemaining <= 1) {
                uiState = uiState.copy(
                    secondsRemaining = 0,
                    isExpired = true
                )
                return
            }
            uiState = uiState.copy(secondsRemaining = uiState.secondsRemaining - 1)
            main.postDelayed(this, 1_000)
        }
    }

    fun start(phoneNumber: String) {
        main.removeCallbacks(ticker)
        uiState = OtpVerificationUiState(phoneNumber = phoneNumber)
        main.postDelayed(ticker, 1_000)
    }

    fun onEvent(event: OtpVerificationUiEvent) {
        when (event) {
            is OtpVerificationUiEvent.DigitChanged -> updateDigit(event.index, event.value)
            is OtpVerificationUiEvent.OtpPasted -> pasteOtp(event.value)
            is OtpVerificationUiEvent.Backspace -> backspace(event.index)
            OtpVerificationUiEvent.Verify -> verify()
            OtpVerificationUiEvent.Resend -> resend()
            OtpVerificationUiEvent.EditNumber -> uiState = uiState.copy(editRequested = true)
        }
    }

    private fun updateDigit(index: Int, value: String) {
        if (uiState.isVerifying || uiState.isResending) return
        if (value.filter(Char::isDigit).length > 1) {
            pasteOtp(value)
            return
        }
        uiState = uiState.copy(
            otp = OtpRules.updateDigit(uiState.otp, index, value),
            error = null,
            confirmationMessageVisible = false
        )
    }

    private fun pasteOtp(value: String) {
        if (uiState.isVerifying || uiState.isResending) return
        uiState = uiState.copy(
            otp = OtpRules.digits(value),
            error = null,
            confirmationMessageVisible = false
        )
    }

    private fun backspace(index: Int) {
        if (uiState.isVerifying || uiState.isResending) return
        uiState = uiState.copy(
            otp = OtpRules.backspace(uiState.otp, index),
            error = null
        )
    }

    private fun verify() {
        if (uiState.isVerifying || uiState.isResending) return
        if (uiState.isExpired) {
            uiState = uiState.copy(error = OtpError.Expired)
            return
        }
        if (!uiState.isComplete) {
            uiState = uiState.copy(error = OtpError.Incomplete)
            return
        }

        val phone = uiState.phoneNumber
        val otp = uiState.otp
        uiState = uiState.copy(isVerifying = true, error = null)
        executor.execute {
            val result = repository.verifyOtp(phone, otp)
            main.post {
                uiState = result.fold(
                    onSuccess = { session ->
                        main.removeCallbacks(ticker)
                        uiState.copy(
                            isVerifying = false,
                            verifiedSession = session
                        )
                    },
                    onFailure = { failure ->
                        val attempts = uiState.failedAttempts + 1
                        val tooMany = attempts >= OtpRules.MAX_FAILED_ATTEMPTS
                        uiState.copy(
                            isVerifying = false,
                            failedAttempts = attempts,
                            isExpired = tooMany,
                            error = when {
                                tooMany -> OtpError.TooManyAttempts
                                failure is IllegalArgumentException -> OtpError.Invalid
                                else -> OtpError.Network
                            }
                        )
                    }
                )
            }
        }
    }

    private fun resend() {
        if (!uiState.canResend) return
        val phone = uiState.phoneNumber
        uiState = uiState.copy(isResending = true, error = null)
        executor.execute {
            val result = repository.resendOtp(phone)
            main.post {
                result.fold(
                    onSuccess = {
                        main.removeCallbacks(ticker)
                        uiState = uiState.copy(
                            otp = "",
                            secondsRemaining = OtpRules.EXPIRY_SECONDS,
                            isExpired = false,
                            isResending = false,
                            error = null,
                            confirmationMessageVisible = true,
                            failedAttempts = 0
                        )
                        main.postDelayed(ticker, 1_000)
                    },
                    onFailure = {
                        uiState = uiState.copy(
                            isResending = false,
                            error = OtpError.Network
                        )
                    }
                )
            }
        }
    }

    fun consumeVerifiedSession() {
        uiState = uiState.copy(verifiedSession = null)
    }

    fun consumeEditRequest() {
        uiState = uiState.copy(editRequested = false)
    }

    override fun onCleared() {
        main.removeCallbacks(ticker)
        executor.shutdownNow()
        super.onCleared()
    }
}

class OtpVerificationViewModelFactory(
    private val repository: OtpRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OtpVerificationViewModel(repository) as T
    }
}
