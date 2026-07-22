package com.dolo.patient.data

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.auth.PrototypeSessionManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

data class HostedProfile(val id: String, val name: String, val relationship: String = "SELF")
data class HostedClinic(val id: String, val name: String, val city: String, val doctorName: String, val specialty: String, val feeMinor: Int)
data class HostedSession(val id: String, val date: String, val name: String, val startsAt: String, val endsAt: String, val available: Int, val enabled: Boolean)
data class HostedAppointment(val id: String, val sessionId: String, val doctorName: String, val clinicName: String, val patientName: String, val date: String, val session: String, val token: Int, val status: String)
data class HostedLiveQueue(val appointmentId: String, val token: Int, val currentToken: Int?, val patientsAhead: Int?, val estimatedMinutes: Int?, val status: String, val countdownState: String)
data class HostedCommunication(val id: String, val audience: String, val kind: String, val title: String, val message: String, val startsOn: String, val endsOn: String)
data class HostedBootstrap(val profile: HostedProfile, val clinic: HostedClinic, val sessions: List<HostedSession>, val profiles: List<HostedProfile> = listOf(profile))
data class HostedSyncSnapshot(val bootstrap: HostedBootstrap, val appointments: List<HostedAppointment>, val live: List<HostedLiveQueue>, val communications: List<HostedCommunication> = emptyList())
data class HostedServerError(val code: String, val message: String)

sealed interface HostedResult<out T> {
    data class Success<T>(val value: T) : HostedResult<T>
    data class Failure(val message: String, val doctorUnavailable: Boolean = false) : HostedResult<Nothing>
}

object HostedBookingKeys {
    const val SEEDED_PRIMARY_PROFILE_ID = "10000000-0000-0000-0000-000000000016"
    fun preferenceKey(sessionId: String, profileId: String): String = "hosted_booking_key_${sessionId}_$profileId"
    fun legacyPreferenceKey(sessionId: String): String = "hosted_booking_key_$sessionId"
}

interface HostedPatientSyncApi {
    fun refresh(): HostedResult<HostedSyncSnapshot>
    fun book(sessionId: String, profileId: String): HostedResult<HostedSyncSnapshot>
}

object HostedErrorJson {
    fun parse(json: String, status: Int): HostedServerError = runCatching {
        val error = JSONObject(json).getJSONObject("error")
        HostedServerError(
            code = error.optString("code", "HTTP_$status"),
            message = error.optString("message", "Hosted API returned HTTP $status")
        )
    }.getOrDefault(HostedServerError("HTTP_$status", "Hosted API returned HTTP $status"))
}

object HostedCommunicationJson {
    fun parse(json: String): List<HostedCommunication> {
        val items = JSONObject(json).getJSONArray("communications")
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    HostedCommunication(
                        id = item.getString("id"),
                        audience = item.getString("audience"),
                        kind = item.getString("kind"),
                        title = item.getString("title"),
                        message = item.getString("message"),
                        startsOn = item.getString("startsOn"),
                        endsOn = item.getString("endsOn")
                    )
                )
            }
        }
    }
}

object HostedBootstrapJson {
    fun parse(json: String): HostedBootstrap {
        val root = JSONObject(json)
        require(root.optBoolean("authoritative"))
        val primary = root.getJSONObject("profile")
        val clinic = root.getJSONObject("clinic")
        val doctor = clinic.getJSONObject("doctor")
        val profileArray = root.optJSONArray("profiles")
        val profiles = if (profileArray == null) {
            listOf(HostedProfile(primary.getString("id"), primary.getString("displayName"), primary.optString("relationship", "SELF")))
        } else {
            buildList {
                for (index in 0 until profileArray.length()) {
                    val profile = profileArray.getJSONObject(index)
                    add(HostedProfile(profile.getString("id"), profile.getString("displayName"), profile.optString("relationship", "FAMILY")))
                }
            }
        }
        val sessionArray = root.getJSONArray("sessions")
        val sessions = buildList {
            for (index in 0 until sessionArray.length()) {
                val session = sessionArray.getJSONObject(index)
                add(HostedSession(session.getString("id"), session.getString("serviceDate"), session.getString("name"), session.getString("startsAt"), session.getString("endsAt"), session.optInt("availableTokens"), session.optBoolean("bookingEnabled")))
            }
        }
        val primaryProfile = profiles.firstOrNull { it.id == primary.getString("id") }
            ?: HostedProfile(primary.getString("id"), primary.getString("displayName"), primary.optString("relationship", "SELF"))
        return HostedBootstrap(
            primaryProfile,
            HostedClinic(clinic.getString("id"), clinic.getString("name"), clinic.optString("city"), doctor.getString("name"), doctor.optString("specialty"), clinic.optInt("consultationFeeMinor")),
            sessions,
            profiles
        )
    }
}
private class HostedRequestException(
    val code: String,
    message: String
) : Exception(message)

class HttpHostedPatientSyncApi(
    baseUrl: String,
    private val sessionManager: PrototypeSessionManager,
    private val preferences: SharedPreferences
) : HostedPatientSyncApi {
    private val base = baseUrl.trim().trimEnd('/')

    init {
        require(URL(base).protocol.equals("https", true)) { "Hosted Patient synchronization requires HTTPS." }
    }

    override fun refresh(): HostedResult<HostedSyncSnapshot> = guarded { load() }

    override fun book(sessionId: String, profileId: String): HostedResult<HostedSyncSnapshot> = guarded {
        val keyName = HostedBookingKeys.preferenceKey(sessionId, profileId)
        val legacy = if (profileId == HostedBookingKeys.SEEDED_PRIMARY_PROFILE_ID) {
            preferences.getString(HostedBookingKeys.legacyPreferenceKey(sessionId), null)
        } else null
        val idempotency = preferences.getString(keyName, null)
            ?: legacy?.also { preferences.edit().putString(keyName, it).apply() }
            ?: ("android21b-" + UUID.randomUUID()).also { preferences.edit().putString(keyName, it).apply() }
        request(
            "POST",
            "/api/v1/appointments",
            JSONObject().put("clinicSessionId", sessionId).put("patientProfileId", profileId).toString(),
            mapOf("Idempotency-Key" to idempotency)
        )
        load()
    }

    private fun load(): HostedSyncSnapshot {
        val bootstrap = HostedBootstrapJson.parse(request("POST", "/api/v1/patient/sync/bootstrap", "{}"))
        val appointments = parseAppointments(request("GET", "/api/v1/appointments"))
        val live = parseLive(request("GET", "/api/v1/patient/live-appointments"))
        val communications = HostedCommunicationJson.parse(request("GET", "/api/v1/patient/communications?clinicId=${bootstrap.clinic.id}"))
        return HostedSyncSnapshot(bootstrap, appointments, live, communications)
    }

    private fun <T> guarded(block: () -> T): HostedResult<T> = runCatching(block).fold(
        onSuccess = { HostedResult.Success(it) },
        onFailure = { error ->
            when (error) {
                is java.net.UnknownHostException -> HostedResult.Failure("Offline. Server data was not changed.")
                is java.net.SocketTimeoutException -> HostedResult.Failure("Hosted prototype is waking up. Retry shortly.")
                is HostedRequestException -> HostedResult.Failure(
                    message = error.message?.take(180) ?: "Hosted synchronization failed.",
                    doctorUnavailable = error.code == "DOCTOR_UNAVAILABLE"
                )
                else -> HostedResult.Failure(error.message?.take(180) ?: "Hosted synchronization failed.")
            }
        }
    )

    private fun request(
        method: String,
        path: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): String {
        val token = sessionManager.accessToken() ?: error("Hosted session expired. Log out and sign in again while online.")
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 25_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("User-Agent", "DO-LO-Patient-Android/Stage21B")
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            useCaches = false
        }
        return try {
            if (body != null) connection.outputStream.use { it.write(body.toByteArray()) }
            val status = connection.responseCode
            val text = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText().take(524_288) }.orEmpty()
            if (status !in 200..299) {
                val serverError = HostedErrorJson.parse(text, status)
                throw HostedRequestException(serverError.code, serverError.message)
            }
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAppointments(json: String): List<HostedAppointment> {
        val appointments = JSONObject(json).getJSONArray("appointments")
        return buildList {
            for (index in 0 until appointments.length()) {
                val item = appointments.getJSONObject(index)
                add(HostedAppointment(item.getString("id"), item.getString("clinicSessionId"), "Dr. Ananya Mehta", item.getString("clinicName"), item.getString("patientName"), item.getString("serviceDate"), item.getString("session"), item.getInt("tokenNumber"), item.getString("status")))
            }
        }
    }

    private fun parseLive(json: String): List<HostedLiveQueue> {
        val appointments = JSONObject(json).getJSONArray("appointments")
        return buildList {
            for (index in 0 until appointments.length()) {
                val item = appointments.getJSONObject(index)
                add(HostedLiveQueue(item.getString("appointmentId"), item.getInt("tokenNumber"), item.optIntOrNull("currentToken"), item.optIntOrNull("patientsAhead"), item.optIntOrNull("estimatedWaitMinutes"), item.getString("appointmentStatus"), item.optString("countdownState")))
            }
        }
    }

    private fun JSONObject.optIntOrNull(key: String): Int? = if (isNull(key) || !has(key)) null else getInt(key)
}

data class HostedSyncUiState(
    val loading: Boolean = false,
    val snapshot: HostedSyncSnapshot? = null,
    val message: String = "Connect to the seeded hosted identity to begin.",
    val error: Boolean = false,
    val doctorUnavailable: Boolean = false
)

object HostedSyncStateReducer {
    fun failure(previous: HostedSyncUiState, failure: HostedResult.Failure): HostedSyncUiState =
        if (failure.doctorUnavailable) {
            HostedSyncUiState(message = failure.message, error = true, doctorUnavailable = true)
        } else {
            previous.copy(loading = false, message = failure.message, error = true)
        }
}

class HostedPatientSyncViewModel(private val api: HostedPatientSyncApi) : ViewModel() {
    var uiState by mutableStateOf(HostedSyncUiState())
        private set
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    fun refresh() { execute { api.refresh() } }
    fun book(sessionId: String, profileId: String) { execute { api.book(sessionId, profileId) } }

    private fun execute(call: () -> HostedResult<HostedSyncSnapshot>) {
        if (uiState.loading) return
        uiState = uiState.copy(loading = true, message = "Synchronizing authoritative prototype data...", error = false)
        executor.execute {
            val result = call()
            main.post {
                uiState = when (result) {
                    is HostedResult.Success -> HostedSyncUiState(snapshot = result.value, message = "Server data is authoritative for this seeded dummy flow.")
                    is HostedResult.Failure -> HostedSyncStateReducer.failure(uiState, result)
                }
            }
        }
    }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}

class HostedPatientSyncViewModelFactory(private val api: HostedPatientSyncApi) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HostedPatientSyncViewModel(api) as T
}
