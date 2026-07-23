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
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.Executors

data class HostedProfile(val id: String, val name: String, val relationship: String = "SELF")
data class HostedClinic(val id: String, val name: String, val city: String, val doctorName: String, val specialty: String, val feeMinor: Int)
data class HostedSession(val id: String, val date: String, val name: String, val startsAt: String, val endsAt: String, val available: Int, val enabled: Boolean)
data class HostedAppointment(val id: String, val sessionId: String, val doctorName: String, val clinicName: String, val patientName: String, val date: String, val session: String, val token: Int, val status: String, val clinicFeeStatus: String = "PENDING", val clinicFeeAmountMinor: Int = 0, val receiptNumber: String? = null, val rescheduleUsed: Boolean = false, val rescheduledFromAppointmentId: String? = null)
data class HostedLiveQueue(val appointmentId: String, val token: Int, val currentToken: Int?, val patientsAhead: Int?, val estimatedMinutes: Int?, val status: String, val countdownState: String)
data class HostedCommunication(
    val id: String,
    val audience: String,
    val kind: String,
    val title: String,
    val message: String,
    val startsOn: String,
    val endsOn: String,
    val clinicId: String? = null
)
data class HostedReview(val id: String, val appointmentId: String, val patientName: String, val doctorName: String, val clinicName: String, val rating: Int, val comment: String, val status: String, val submittedAt: String)
data class HostedSupportRequest(val id:String,val category:String,val subject:String,val message:String,val status:String,val adminNote:String,val submittedAt:String,val updatedAt:String)
data class HostedNotification(val cursor:String,val appointmentId:String,val patientName:String,val tokenNumber:Int,val kind:String,val title:String,val message:String,val occurredAt:String,val read:Boolean)
data class HostedPreferences(val appointmentServiceUpdates:Boolean,val healthInformation:Boolean,val promotionalMessages:Boolean,val inAppMessages:Boolean,val preferredLanguage:String,val consentVersion:String,val consentedAt:String?,val smsUsage:String,val healthSegmentationBasis:String)
data class HostedBootstrap(val profile: HostedProfile, val clinic: HostedClinic, val sessions: List<HostedSession>, val profiles: List<HostedProfile> = listOf(profile), val rescheduleWindowDays: Int = 10, val rescheduleSessions: List<HostedSession> = sessions)
data class HostedSyncSnapshot(val bootstrap: HostedBootstrap, val appointments: List<HostedAppointment>, val live: List<HostedLiveQueue>, val communications: List<HostedCommunication> = emptyList(), val reviews: List<HostedReview> = emptyList(), val supportRequests: List<HostedSupportRequest> = emptyList(), val notifications: List<HostedNotification> = emptyList(), val preferences: HostedPreferences? = null)
data class HostedServerError(val code: String, val message: String)

object HostedHomePresentation {
    private val terminalStatuses = setOf("COMPLETED", "ABSENT", "RESCHEDULED", "EXPIRED")

    fun activeAppointments(snapshot: HostedSyncSnapshot): List<HostedAppointment> =
        snapshot.appointments.filter { it.status !in terminalStatuses }.sortedWith(
            compareBy<HostedAppointment> { it.date }.thenBy { it.session }.thenBy { it.token }
        )

    fun liveQueue(snapshot: HostedSyncSnapshot, appointmentId: String): HostedLiveQueue? =
        snapshot.live.firstOrNull { it.appointmentId == appointmentId }

    fun homeCommunications(snapshot: HostedSyncSnapshot): List<HostedCommunication> =
        snapshot.communications.filter { it.audience == "ALL_PATIENTS" }.take(3)
}

sealed interface HostedResult<out T> {
    data class Success<T>(val value: T) : HostedResult<T>
    data class Failure(val message: String, val doctorUnavailable: Boolean = false) : HostedResult<Nothing>
}

object HostedBookingKeys {
    const val SEEDED_PRIMARY_PROFILE_ID = "10000000-0000-0000-0000-000000000016"
    fun preferenceKey(sessionId: String, profileId: String): String = "hosted_booking_key_${sessionId}_$profileId"
    fun legacyPreferenceKey(sessionId: String): String = "hosted_booking_key_$sessionId"
}

object HostedReceiptPresentation {
    fun text(appointment: HostedAppointment): String = when (appointment.clinicFeeStatus) {
        "PAID" -> {
            val amount = if (appointment.clinicFeeAmountMinor > 0) "INR ${appointment.clinicFeeAmountMinor / 100} " else ""
            "Clinic consultation fee: ${amount}paid directly at clinic\nReceipt: ${appointment.receiptNumber ?: "Pending reference"}\nNot an online DO-LO payment."
        }
        "WAIVED" -> "Clinic consultation fee: Waived at clinic\nReceipt: ${appointment.receiptNumber ?: "Pending reference"}\nNot an online DO-LO payment."
        else -> "Clinic consultation fee: Pending at clinic\nReceipt will be generated after clinic confirmation."
    }
}

object HostedSupportKeys { fun preferenceKey(subject:String,message:String):String="hosted_support_key_${subject.hashCode()}_${message.hashCode()}" }

object HostedReviewKeys {
    fun preferenceKey(appointmentId: String): String = "hosted_review_key_$appointmentId"
}

object HostedRescheduleKeys {
    fun preferenceKey(appointmentId: String, targetSessionId: String): String =
        "hosted_reschedule_key_${appointmentId}_$targetSessionId"
}

object HostedReschedulePolicy {
    fun eligibleSessions(appointment: HostedAppointment, sessions: List<HostedSession>, windowDays: Int): List<HostedSession> {
        if (appointment.status != "ABSENT" || appointment.rescheduleUsed || windowDays !in 1..30) return emptyList()
        val originalDate = runCatching { LocalDate.parse(appointment.date) }.getOrNull() ?: return emptyList()
        val lastDate = originalDate.plusDays(windowDays.toLong())
        return sessions.filter { session ->
            val targetDate = runCatching { LocalDate.parse(session.date) }.getOrNull()
            session.enabled && session.id != appointment.sessionId && targetDate != null && !targetDate.isBefore(originalDate) && !targetDate.isAfter(lastDate)
        }
    }
}

interface HostedPatientSyncApi {
    fun refresh(): HostedResult<HostedSyncSnapshot>
    fun book(sessionId: String, profileId: String): HostedResult<HostedSyncSnapshot>
    fun reschedule(appointmentId: String, targetSessionId: String): HostedResult<HostedSyncSnapshot>
    fun submitReview(appointmentId: String, rating: Int, comment: String): HostedResult<HostedSyncSnapshot>
    fun submitSupportRequest(category:String,subject:String,message:String):HostedResult<HostedSyncSnapshot>
    fun markNotificationsRead(readThroughCursor:String):HostedResult<HostedSyncSnapshot>
    fun updatePreferences(preferences:HostedPreferences):HostedResult<HostedSyncSnapshot>
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
                        endsOn = item.getString("endsOn"),
                        clinicId = item.optNullableString("clinicId")
                    )
                )
            }
        }
    }
}

object HostedReviewJson {
    fun parse(json: String): List<HostedReview> {
        val reviews = JSONObject(json).getJSONArray("reviews")
        return buildList {
            for (index in 0 until reviews.length()) {
                val item = reviews.getJSONObject(index)
                add(HostedReview(item.getString("id"), item.getString("appointmentId"), item.getString("patientName"), item.getString("doctorName"), item.getString("clinicName"), item.getInt("rating"), item.optString("comment"), item.getString("status"), item.getString("submittedAt")))
            }
        }
    }
}
object HostedPreferencesJson {
    fun parse(json: String): HostedPreferences {
        val item = JSONObject(json).getJSONObject("preferences")
        return HostedPreferences(
            appointmentServiceUpdates = item.getBoolean("appointmentServiceUpdates"),
            healthInformation = item.getBoolean("healthInformation"),
            promotionalMessages = item.getBoolean("promotionalMessages"),
            inAppMessages = item.getBoolean("inAppMessages"),
            preferredLanguage = item.getString("preferredLanguage"),
            consentVersion = item.getString("consentVersion"),
            consentedAt = item.optNullableString("consentedAt"),
            smsUsage = item.getString("smsUsage"),
            healthSegmentationBasis = item.getString("healthSegmentationBasis")
        )
    }
}
object HostedNotificationJson {
    fun parse(json:String):List<HostedNotification>{val root=JSONObject(json);require(root.optBoolean("authoritative"));val items=root.getJSONArray("notifications");return buildList{for(index in 0 until items.length()){val item=items.getJSONObject(index);add(HostedNotification(item.getString("cursor"),item.getString("appointmentId"),item.getString("patientName"),item.getInt("tokenNumber"),item.getString("kind"),item.getString("title"),item.getString("message"),item.getString("occurredAt"),item.getBoolean("read")))}}}
}object HostedSupportJson {
    fun parse(json:String):List<HostedSupportRequest>{val root=JSONObject(json);require(root.optBoolean("authoritative"));val items=root.getJSONArray("supportRequests");return buildList{for(index in 0 until items.length()){val item=items.getJSONObject(index);val status=item.getString("status");require(status in setOf("OPEN","IN_PROGRESS","RESOLVED","CLOSED"));add(HostedSupportRequest(item.getString("id"),item.getString("category"),item.getString("subject"),item.getString("message"),status,item.optString("adminNote"),item.getString("submittedAt"),item.getString("updatedAt")))}}}
}object HostedAppointmentJson {
    fun parse(json: String): List<HostedAppointment> {
        val appointments = JSONObject(json).getJSONArray("appointments")
        return buildList {
            for (index in 0 until appointments.length()) {
                val item = appointments.getJSONObject(index)
                fun nullableString(key: String): String? = if (item.isNull(key) || !item.has(key)) null else item.getString(key)
                add(
                    HostedAppointment(
                        item.getString("id"), item.getString("clinicSessionId"), "Dr. Ananya Mehta",
                        item.getString("clinicName"), item.getString("patientName"), item.getString("serviceDate"),
                        item.getString("session"), item.getInt("tokenNumber"), item.getString("status"),
                        item.optString("clinicFeeStatus", "PENDING"), item.optInt("clinicFeeAmountMinor"),
                        nullableString("receiptNumber"), item.optBoolean("rescheduleUsed"),
                        nullableString("rescheduledFromAppointmentId")
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
        fun parseSessions(key: String): List<HostedSession> {
            val values = root.getJSONArray(key)
            return buildList {
                for (index in 0 until values.length()) {
                    val session = values.getJSONObject(index)
                    add(HostedSession(session.getString("id"), session.getString("serviceDate"), session.getString("name"), session.getString("startsAt"), session.getString("endsAt"), session.optInt("availableTokens"), session.optBoolean("bookingEnabled")))
                }
            }
        }
        val sessions = parseSessions("sessions")
        val rescheduleSessions = if (root.has("rescheduleSessions")) parseSessions("rescheduleSessions") else sessions
        val primaryProfile = profiles.firstOrNull { it.id == primary.getString("id") }
            ?: HostedProfile(primary.getString("id"), primary.getString("displayName"), primary.optString("relationship", "SELF"))
        return HostedBootstrap(
            primaryProfile,
            HostedClinic(clinic.getString("id"), clinic.getString("name"), clinic.optString("city"), doctor.getString("name"), doctor.optString("specialty"), clinic.optInt("consultationFeeMinor")),
            sessions,
            profiles,
            root.optInt("rescheduleWindowDays", 10),
            rescheduleSessions
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

    override fun reschedule(appointmentId: String, targetSessionId: String): HostedResult<HostedSyncSnapshot> = guarded {
        val keyName = HostedRescheduleKeys.preferenceKey(appointmentId, targetSessionId)
        val idempotency = preferences.getString(keyName, null)
            ?: ("android22a-" + UUID.randomUUID()).also { preferences.edit().putString(keyName, it).apply() }
        request(
            "POST",
            "/api/v1/appointments/$appointmentId/reschedule",
            JSONObject().put("targetClinicSessionId", targetSessionId).toString(),
            mapOf("Idempotency-Key" to idempotency)
        )
        load()
    }

    override fun submitReview(appointmentId: String, rating: Int, comment: String): HostedResult<HostedSyncSnapshot> = guarded {
        require(rating in 1..5 && comment.trim().length <= 500) { "Review must use 1 to 5 stars and at most 500 characters." }
        val keyName = HostedReviewKeys.preferenceKey(appointmentId)
        val idempotency = preferences.getString(keyName, null)
            ?: ("android25b-" + UUID.randomUUID()).also { preferences.edit().putString(keyName, it).apply() }
        request("POST", "/api/v1/appointments/$appointmentId/review", JSONObject().put("rating", rating).put("comment", comment.trim()).toString(), mapOf("Idempotency-Key" to idempotency))
        load()
    }
    override fun submitSupportRequest(category:String,subject:String,message:String):HostedResult<HostedSyncSnapshot> = guarded {
        require(category in setOf("APPOINTMENT","DOCTOR","BILLING","APP","OTHER")&&subject.trim().length in 8..120&&message.trim().length in 20..1000){"Complete the support category, subject and message."}
        val keyName=HostedSupportKeys.preferenceKey(subject.trim(),message.trim());val idempotency=preferences.getString(keyName,null)?: ("android26b-"+UUID.randomUUID()).also{preferences.edit().putString(keyName,it).apply()}
        request("POST","/api/v1/patient/support-requests",JSONObject().put("category",category).put("subject",subject.trim()).put("message",message.trim()).toString(),mapOf("Idempotency-Key" to idempotency));load()
    }
    override fun updatePreferences(preferences:HostedPreferences):HostedResult<HostedSyncSnapshot> = guarded {
        request("PUT","/api/v1/patient/preferences",JSONObject().put("appointmentServiceUpdates",preferences.appointmentServiceUpdates).put("healthInformation",preferences.healthInformation).put("promotionalMessages",preferences.promotionalMessages).put("inAppMessages",preferences.inAppMessages).put("preferredLanguage",preferences.preferredLanguage).put("consentVersion","2026-07").toString());load()
    }    override fun markNotificationsRead(readThroughCursor:String):HostedResult<HostedSyncSnapshot> = guarded {
        require(readThroughCursor.matches(Regex("^(0|[1-9][0-9]{0,18})$"))){"Invalid notification cursor."}
        request("PUT","/api/v1/patient/notifications",JSONObject().put("readThroughCursor",readThroughCursor).toString());load()
    }
    private fun load(): HostedSyncSnapshot {
        val bootstrap = HostedBootstrapJson.parse(request("POST", "/api/v1/patient/sync/bootstrap", "{}"))
        val appointments = HostedAppointmentJson.parse(request("GET", "/api/v1/appointments"))
        val live = parseLive(request("GET", "/api/v1/patient/live-appointments"))
        val communications = HostedCommunicationJson.parse(request("GET", "/api/v1/patient/communications?clinicId=${bootstrap.clinic.id}"))
        val reviews = HostedReviewJson.parse(request("GET", "/api/v1/patient/reviews"))
        val supportRequests = HostedSupportJson.parse(request("GET", "/api/v1/patient/support-requests"))
        val notifications = HostedNotificationJson.parse(request("GET", "/api/v1/patient/notifications?after=0&limit=100"))
        val communicationPreferences = HostedPreferencesJson.parse(request("GET", "/api/v1/patient/preferences"))
        return HostedSyncSnapshot(bootstrap, appointments, live, communications, reviews, supportRequests, notifications, communicationPreferences)
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
            setRequestProperty("User-Agent", "DO-LO-Patient-Android/Stage31B")
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

private fun JSONObject.optNullableString(key: String): String? =
    if (!has(key) || isNull(key)) null else getString(key).takeIf { it.isNotBlank() }

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
    fun reschedule(appointmentId: String, targetSessionId: String) { execute { api.reschedule(appointmentId, targetSessionId) } }
    fun submitReview(appointmentId: String, rating: Int, comment: String) { execute { api.submitReview(appointmentId, rating, comment) } }
    fun submitSupportRequest(category:String,subject:String,message:String){execute{api.submitSupportRequest(category,subject,message)}}
    fun markHostedNotificationsRead(cursor:String){execute{api.markNotificationsRead(cursor)}}
    fun updatePreferences(preferences:HostedPreferences){execute{api.updatePreferences(preferences)}}

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
