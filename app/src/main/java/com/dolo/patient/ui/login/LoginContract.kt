package com.dolo.patient.ui.login

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.auth.AuthRepository
import com.dolo.patient.auth.PhoneValidator
import java.util.concurrent.Executors

enum class LoginError {
    InvalidPhone,
    Network
}

data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val accountCreationNoticeVisible: Boolean = false,
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
