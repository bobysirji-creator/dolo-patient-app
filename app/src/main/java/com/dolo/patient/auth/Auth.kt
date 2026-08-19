package com.dolo.patient.auth

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.concurrent.Executors
import kotlin.concurrent.thread

object PhoneValidator {
    fun normalize(value: String): String = value.filter(Char::isDigit).takeLast(10)
    fun isValid(value: String): Boolean = normalize(value).length == 10
}

enum class PatientAuthMode(val label: String) { CONTROLLED_PILOT("Controlled pilot"), HOSTED_PROTOTYPE("Hosted prototype"), LOCAL_FALLBACK("Local fallback") }
data class PatientSession(val phone: String, val mode: PatientAuthMode = PatientAuthMode.LOCAL_FALLBACK, val notice: String? = null,val doloId:String?=null,val displayName:String?=null)

interface AuthRepository {
    fun currentSession(): PatientSession?
    fun pilotReadiness():Result<PilotReadiness>
    fun pilotLogin(doloId:String,credential:String):Result<PatientSession>
    fun activatePilot(inviteCode:String,credential:String):Result<PatientSession>
    fun enrollmentReadiness(): Result<PatientEnrollmentReadiness>
    fun enrollmentActivationRequirements(): Result<PatientEnrollmentActivationRequirements>
    fun enrollmentConsentCatalog(): Result<PatientEnrollmentConsentCatalog>
    fun legalDocumentPreview(): Result<PatientLegalDocumentPreview>
    fun identityCard(): Result<PublicIdentityCard>
    fun requestOtp(phone: String): Result<Unit>
    fun verifyOtp(phone: String, otp: String): Result<PatientSession>
    fun logout()
}

class FakeAuthRepository(private val preferences: SharedPreferences) : AuthRepository {
    override fun currentSession(): PatientSession? = preferences.getString(KEY_PHONE, null)?.let(::PatientSession)
    override fun pilotReadiness()=Result.failure<PilotReadiness>(IllegalStateException("Controlled pilot is unavailable in local mode."))
    override fun pilotLogin(doloId:String,credential:String)=Result.failure<PatientSession>(IllegalStateException("Controlled pilot is unavailable in local mode."))
    override fun activatePilot(inviteCode:String,credential:String)=Result.failure<PatientSession>(IllegalStateException("Controlled pilot is unavailable in local mode."))
    override fun enrollmentReadiness()=Result.success(stage49aPatientEnrollmentReadiness())
    override fun enrollmentActivationRequirements() =
        Result.success(stage50aPatientEnrollmentActivationRequirements())
    override fun enrollmentConsentCatalog() =
        Result.success(stage51aPatientEnrollmentConsentCatalog())
    override fun legalDocumentPreview() = Result.success(stage52bpPatientLegalDocumentPreview())
    override fun identityCard()=Result.failure<PublicIdentityCard>(IllegalStateException("Local-only demo profile has no hosted DO-LO identity."))
    override fun requestOtp(phone: String): Result<Unit> = if (PhoneValidator.isValid(phone)) Result.success(Unit) else Result.failure(IllegalArgumentException("Enter a valid 10-digit mobile number"))
    override fun verifyOtp(phone: String, otp: String): Result<PatientSession> {
        if (otp != DEMO_OTP) return Result.failure(IllegalArgumentException("Incorrect OTP. Use 123456 for this demo."))
        val session = PatientSession(PhoneValidator.normalize(phone))
        preferences.edit().putString(KEY_PHONE, session.phone).apply()
        return Result.success(session)
    }
    override fun logout() { preferences.edit().remove(KEY_PHONE).apply() }
    companion object { const val DEMO_OTP = "123456"; const val KEY_PHONE = "patient_phone" }
}

class PrototypeAuthRepository(
    private val preferences: SharedPreferences,
    private val tokenStore: SecureTokenStore,
    private val api: PrototypeAuthApi,
    private val sessionManager: PrototypeSessionManager = PrototypeSessionManager(tokenStore, api)
) : AuthRepository {
    override fun currentSession(): PatientSession? {
        val tokens=tokenStore.read()
        val hosted=tokens?.let { PrototypeAuthJson.hasUsableRefresh(it) }==true
        val mode=preferences.getString(KEY_AUTH_MODE,null)?.let{runCatching{PatientAuthMode.valueOf(it)}.getOrNull()}
        if(mode==PatientAuthMode.CONTROLLED_PILOT&&hosted)return PatientSession("",mode,"Connected to the invitation-only pilot.",preferences.getString(KEY_PILOT_DOLO_ID,null),preferences.getString(KEY_PILOT_NAME,null))
        val phone = preferences.getString(FakeAuthRepository.KEY_PHONE, null) ?: return null
        return PatientSession(phone,if(hosted)PatientAuthMode.HOSTED_PROTOTYPE else PatientAuthMode.LOCAL_FALLBACK)
    }
    override fun pilotReadiness():Result<PilotReadiness> = when(val result=api.pilotReadiness()){is PrototypeAuthResult.Success->Result.success(result.value);is PrototypeAuthResult.Failure->Result.failure(IllegalStateException(result.message))}
    override fun pilotLogin(doloId:String,credential:String):Result<PatientSession> = pilotSession(api.pilotLogin(doloId,credential))
    override fun activatePilot(inviteCode:String,credential:String):Result<PatientSession> = pilotSession(api.activatePilot(inviteCode,credential))
    private fun pilotSession(result:PrototypeAuthResult<PilotSessionResult>):Result<PatientSession> = when(result){
        is PrototypeAuthResult.Failure->Result.failure(IllegalStateException(result.message))
        is PrototypeAuthResult.Success->runCatching{
            require(result.value.identity.role=="PATIENT"){"This invitation does not belong to the Patient App."}
            tokenStore.save(result.value.tokens)
            preferences.edit().putString(KEY_AUTH_MODE,PatientAuthMode.CONTROLLED_PILOT.name).putString(KEY_PILOT_DOLO_ID,result.value.identity.doloId).putString(KEY_PILOT_NAME,result.value.identity.displayName).remove(FakeAuthRepository.KEY_PHONE).commit()
            PatientSession("",PatientAuthMode.CONTROLLED_PILOT,"Connected to the invitation-only pilot.",result.value.identity.doloId,result.value.identity.displayName)
        }
    }
    override fun enrollmentReadiness():Result<PatientEnrollmentReadiness> = when(val result=api.enrollmentReadiness()){is PrototypeAuthResult.Success->Result.success(result.value);is PrototypeAuthResult.Failure->Result.failure(IllegalStateException(result.message))}
    override fun enrollmentActivationRequirements(): Result<PatientEnrollmentActivationRequirements> =
        when (val result = api.enrollmentActivationRequirements()) {
            is PrototypeAuthResult.Success -> Result.success(result.value)
            is PrototypeAuthResult.Failure -> Result.failure(IllegalStateException(result.message))
        }
    override fun enrollmentConsentCatalog(): Result<PatientEnrollmentConsentCatalog> =
        when (val result = api.enrollmentConsentCatalog()) {
            is PrototypeAuthResult.Success -> Result.success(result.value)
            is PrototypeAuthResult.Failure -> Result.failure(IllegalStateException(result.message))
        }
    override fun legalDocumentPreview():Result<PatientLegalDocumentPreview> = when(val result=api.legalDocumentPreview()){is PrototypeAuthResult.Success->Result.success(result.value);is PrototypeAuthResult.Failure->Result.failure(IllegalStateException(result.message))}
    override fun identityCard():Result<PublicIdentityCard>{val token=sessionManager.accessToken()?:return Result.failure(IllegalStateException("Hosted session unavailable."));return when(val result=api.identityCard(token)){is PrototypeAuthResult.Success->Result.success(result.value);is PrototypeAuthResult.Failure->Result.failure(IllegalStateException(result.message))}}
    override fun requestOtp(phone: String): Result<Unit> = if (PhoneValidator.isValid(phone)) Result.success(Unit) else Result.failure(IllegalArgumentException("Enter a valid 10-digit mobile number"))
    override fun verifyOtp(phone: String, otp: String): Result<PatientSession> {
        if (otp != FakeAuthRepository.DEMO_OTP) return Result.failure(IllegalArgumentException("Incorrect OTP. Use 123456 for this demo."))
        val normalized = PhoneValidator.normalize(phone)
        val session = when (val result = api.createDemoSession()) {
            is PrototypeAuthResult.Success -> runCatching {
                tokenStore.save(result.value)
                PatientSession(normalized, PatientAuthMode.HOSTED_PROTOTYPE, "Connected to the seeded hosted identity.")
            }.getOrElse {
                tokenStore.clear()
                PatientSession(normalized, PatientAuthMode.LOCAL_FALLBACK, "Secure token storage was unavailable; local demo mode was used.")
            }
            is PrototypeAuthResult.Failure -> {
                tokenStore.clear()
                PatientSession(normalized, PatientAuthMode.LOCAL_FALLBACK, result.message)
            }
        }
        preferences.edit().putString(FakeAuthRepository.KEY_PHONE, normalized).putString(KEY_AUTH_MODE,session.mode.name).remove(KEY_PILOT_DOLO_ID).remove(KEY_PILOT_NAME).apply()
        return Result.success(session)
    }
    override fun logout() {
        val tokens = tokenStore.read()
        tokenStore.clear()
        preferences.edit().remove(FakeAuthRepository.KEY_PHONE).remove(KEY_AUTH_MODE).remove(KEY_PILOT_DOLO_ID).remove(KEY_PILOT_NAME).apply()
        if (tokens != null) thread(name = "dolo-prototype-logout", isDaemon = true) {
            if (PrototypeAuthJson.hasUsableAccess(tokens)) {
                api.logout(tokens.accessToken)
            } else if (PrototypeAuthJson.hasUsableRefresh(tokens)) {
                val refreshed = api.refresh(tokens.refreshToken)
                if (refreshed is PrototypeAuthResult.Success) api.logout(refreshed.value.accessToken)
            }
        }
    }
    private companion object{const val KEY_AUTH_MODE="patient_auth_mode_v2";const val KEY_PILOT_DOLO_ID="pilot_dolo_id";const val KEY_PILOT_NAME="pilot_display_name"}
}

enum class AuthStep { PHONE, OTP, AUTHENTICATED }
data class AuthUiState(val phone: String = "", val otp: String = "", val step: AuthStep = AuthStep.PHONE, val isLoading: Boolean = false, val error: String? = null, val session: PatientSession? = null,val enrollment:PatientEnrollmentReadiness?=null,val enrollmentMessage:String="Checking production registration status...",val identityCard:PublicIdentityCard?=null,val identityMessage:String="Hosted DO-LO identity unavailable in local mode.",val pilotDialog:Boolean=false,val pilotActivation:Boolean=false,val pilotDoloId:String="",val pilotInviteCode:String="",val pilotCredential:String="",val pilotReadiness:PilotReadiness?=null)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(repository.currentSession()?.let { AuthUiState(phone = it.phone, step = AuthStep.AUTHENTICATED, session = it) } ?: AuthUiState())
        private set
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    init{refreshEnrollmentReadiness();if(uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT))refreshIdentityCard()}
    private fun refreshEnrollmentReadiness(){executor.execute{val result=repository.enrollmentReadiness();main.post{uiState=result.fold({uiState.copy(enrollment=it,enrollmentMessage=if(it.productionPatientEnrollment=="DISABLED")"Real Patient registration is not enabled yet." else "Production registration available.")},{uiState.copy(enrollmentMessage=it.message?:"Registration status unavailable.")})}}}
    fun refreshIdentityCard(){executor.execute{val result=repository.identityCard();main.post{uiState=result.fold({uiState.copy(identityCard=it,identityMessage="Server-owned identity verified.")},{uiState.copy(identityCard=null,identityMessage=it.message?:"Hosted identity unavailable.")})}}}
    fun openPilot(activation:Boolean=false){uiState=uiState.copy(pilotDialog=true,pilotActivation=activation,error=null);executor.execute{val result=repository.pilotReadiness();main.post{uiState=result.fold({uiState.copy(pilotReadiness=it,error=if(it.enabled)null else "Controlled pilot is not enabled on this server.")},{uiState.copy(error=it.message)})}}}
    fun closePilot(){uiState=uiState.copy(pilotDialog=false,pilotCredential="",error=null)}
    fun updatePilotDoloId(value:String){uiState=uiState.copy(pilotDoloId=value.uppercase().filter{it.isLetterOrDigit()||it=='-'}.take(18),error=null)}
    fun updatePilotInviteCode(value:String){uiState=uiState.copy(pilotInviteCode=value.trim().take(32),error=null)}
    fun updatePilotCredential(value:String){uiState=uiState.copy(pilotCredential=value.take(128),error=null)}
    fun submitPilot(){if(uiState.isLoading)return;uiState=uiState.copy(isLoading=true,error=null);executor.execute{val result=if(uiState.pilotActivation)repository.activatePilot(uiState.pilotInviteCode,uiState.pilotCredential)else repository.pilotLogin(uiState.pilotDoloId,uiState.pilotCredential);main.post{result.onSuccess{uiState=uiState.copy(step=AuthStep.AUTHENTICATED,isLoading=false,pilotDialog=false,session=it,pilotCredential="");refreshIdentityCard()}.onFailure{uiState=uiState.copy(isLoading=false,error=it.message)}}}}

    fun updatePhone(value: String) { uiState = uiState.copy(phone = PhoneValidator.normalize(value), error = null) }
    fun beginOtp(phone: String) {
        uiState = uiState.copy(
            phone = PhoneValidator.normalize(phone),
            otp = "",
            step = AuthStep.OTP,
            isLoading = false,
            error = null
        )
    }
    fun updateOtp(value: String) { uiState = uiState.copy(otp = value.filter(Char::isDigit).take(6), error = null) }
    fun requestOtp() {
        uiState = uiState.copy(isLoading = true, error = null)
        repository.requestOtp(uiState.phone).onSuccess { uiState = uiState.copy(step = AuthStep.OTP, isLoading = false) }.onFailure { uiState = uiState.copy(isLoading = false, error = it.message) }
    }
    fun verifyOtp() {
        if (uiState.isLoading) return
        val phone = uiState.phone; val otp = uiState.otp
        uiState = uiState.copy(isLoading = true, error = null)
        executor.execute {
            val result = repository.verifyOtp(phone, otp)
            main.post { result.onSuccess { uiState = uiState.copy(step = AuthStep.AUTHENTICATED, isLoading = false, session = it);if(it.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT))refreshIdentityCard() }.onFailure { uiState = uiState.copy(isLoading = false, error = it.message) } }
        }
    }
    fun completeAuthentication(session: PatientSession) {
        uiState = uiState.copy(
            step = AuthStep.AUTHENTICATED,
            isLoading = false,
            error = null,
            session = session
        )
        if (session.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT)) refreshIdentityCard()
    }
    fun editPhone() { uiState = uiState.copy(step = AuthStep.PHONE, otp = "", error = null) }
    fun logout() { repository.logout(); uiState = AuthUiState();refreshEnrollmentReadiness() }
    override fun onCleared() { executor.shutdownNow(); super.onCleared() }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
}
