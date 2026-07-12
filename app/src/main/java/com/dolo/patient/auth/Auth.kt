package com.dolo.patient.auth

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object PhoneValidator {
    fun normalize(value: String): String = value.filter(Char::isDigit).takeLast(10)
    fun isValid(value: String): Boolean = normalize(value).length == 10
}

data class PatientSession(val phone: String)

interface AuthRepository {
    fun currentSession(): PatientSession?
    fun requestOtp(phone: String): Result<Unit>
    fun verifyOtp(phone: String, otp: String): Result<PatientSession>
    fun logout()
}

class FakeAuthRepository(private val preferences: SharedPreferences) : AuthRepository {
    override fun currentSession(): PatientSession? = preferences.getString(KEY_PHONE, null)?.let(::PatientSession)
    override fun requestOtp(phone: String): Result<Unit> =
        if (PhoneValidator.isValid(phone)) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Enter a valid 10-digit mobile number"))

    override fun verifyOtp(phone: String, otp: String): Result<PatientSession> {
        if (otp != DEMO_OTP) return Result.failure(IllegalArgumentException("Incorrect OTP. Use 123456 for this demo."))
        val session = PatientSession(PhoneValidator.normalize(phone))
        preferences.edit().putString(KEY_PHONE, session.phone).apply()
        return Result.success(session)
    }

    override fun logout() { preferences.edit().remove(KEY_PHONE).apply() }

    companion object {
        const val DEMO_OTP = "123456"
        private const val KEY_PHONE = "patient_phone"
    }
}

enum class AuthStep { PHONE, OTP, AUTHENTICATED }

data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val step: AuthStep = AuthStep.PHONE,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(repository.currentSession()?.let {
        AuthUiState(phone = it.phone, step = AuthStep.AUTHENTICATED)
    } ?: AuthUiState())
        private set

    fun updatePhone(value: String) { uiState = uiState.copy(phone = PhoneValidator.normalize(value), error = null) }
    fun updateOtp(value: String) { uiState = uiState.copy(otp = value.filter(Char::isDigit).take(6), error = null) }

    fun requestOtp() {
        uiState = uiState.copy(isLoading = true, error = null)
        repository.requestOtp(uiState.phone)
            .onSuccess { uiState = uiState.copy(step = AuthStep.OTP, isLoading = false) }
            .onFailure { uiState = uiState.copy(isLoading = false, error = it.message) }
    }

    fun verifyOtp() {
        uiState = uiState.copy(isLoading = true, error = null)
        repository.verifyOtp(uiState.phone, uiState.otp)
            .onSuccess { uiState = uiState.copy(step = AuthStep.AUTHENTICATED, isLoading = false, error = null) }
            .onFailure { uiState = uiState.copy(isLoading = false, error = it.message) }
    }

    fun editPhone() { uiState = uiState.copy(step = AuthStep.PHONE, otp = "", error = null) }
    fun logout() { repository.logout(); uiState = AuthUiState() }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
}
