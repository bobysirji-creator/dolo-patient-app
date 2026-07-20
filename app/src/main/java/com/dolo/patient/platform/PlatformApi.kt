package com.dolo.patient.platform

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

enum class PlatformConnectionStatus { NOT_CHECKED, CONNECTING, CONNECTED, OFFLINE }

data class PlatformCapabilities(
    val stage: String,
    val transport: String,
    val databaseConnected: Boolean,
    val authenticationEnabled: Boolean,
    val smsEnabled: Boolean,
    val pushEnabled: Boolean,
    val mapsEnabled: Boolean,
    val paymentsEnabled: Boolean
)

data class PlatformClinic(
    val id: String,
    val name: String,
    val city: String,
    val timeZone: String,
    val doctorId: String,
    val doctorName: String,
    val specialty: String,
    val consultationFeeMinor: Int
)

data class PlatformSnapshot(
    val serviceVersion: String,
    val capabilities: PlatformCapabilities,
    val clinics: List<PlatformClinic>
)

data class PlatformConnectionState(
    val status: PlatformConnectionStatus = PlatformConnectionStatus.NOT_CHECKED,
    val serviceVersion: String = "",
    val capabilities: PlatformCapabilities? = null,
    val clinics: List<PlatformClinic> = emptyList(),
    val message: String = "Connection has not been checked yet.",
    val checkedAtMillis: Long? = null
)

sealed interface PlatformResult<out T> {
    data class Success<T>(val value: T) : PlatformResult<T>
    data class Failure(val message: String) : PlatformResult<Nothing>
}

interface PlatformApi {
    fun fetchPublicSnapshot(): PlatformResult<PlatformSnapshot>
}

class HttpPlatformApi(
    baseUrl: String,
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 25_000
) : PlatformApi {
    private val baseUrl = baseUrl.trim().trimEnd('/')

    init {
        require(URL(this.baseUrl).protocol.equals("https", ignoreCase = true)) {
            "The platform API must use HTTPS."
        }
    }

    override fun fetchPublicSnapshot(): PlatformResult<PlatformSnapshot> = runCatching {
        val health = PlatformJson.parseHealth(get("/health"))
        val capabilities = PlatformJson.parseCapabilities(get("/api/v1/meta/capabilities"))
        val clinics = PlatformJson.parseClinics(get("/api/v1/clinics"))
        PlatformSnapshot(health, capabilities, clinics)
    }.fold(
        onSuccess = { PlatformResult.Success(it) },
        onFailure = { error ->
            PlatformResult.Failure(
                when (error) {
                    is java.net.SocketTimeoutException -> "The hosted service took too long to respond. Retry after a moment."
                    is java.net.UnknownHostException -> "No network connection is available. Local demo data is still safe."
                    else -> error.message?.take(180) ?: "The hosted service could not be reached."
                }
            )
        }
    )

    private fun get(path: String): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "DO-LO-Patient-Android/Stage16A")
            useCaches = false
        }
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                val output = StringBuilder()
                val buffer = CharArray(8_192)
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    if (output.length + count > MAX_RESPONSE_CHARS) {
                        error("The hosted response was unexpectedly large.")
                    }
                    output.append(buffer, 0, count)
                }
                output.toString()
            }.orEmpty()
            if (status !in 200..299) error("The hosted service returned HTTP $status.")
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_RESPONSE_CHARS = 524_288
    }
}

object PlatformJson {
    fun parseHealth(json: String): String {
        val root = JSONObject(json)
        require(root.optString("status") == "ok") { "The hosted health check is not ready." }
        return root.optString("version", "unknown")
    }

    fun parseCapabilities(json: String): PlatformCapabilities {
        val root = JSONObject(json)
        val providers = root.optJSONObject("providers") ?: JSONObject()
        return PlatformCapabilities(
            stage = root.opt("stage")?.toString() ?: "unknown",
            transport = root.optString("transport", "unknown"),
            databaseConnected = root.optBoolean("databaseConnected", false),
            authenticationEnabled = root.optBoolean("authenticationEnabled", false),
            smsEnabled = providers.optBoolean("sms", false),
            pushEnabled = providers.optBoolean("push", false),
            mapsEnabled = providers.optBoolean("maps", false),
            paymentsEnabled = providers.optBoolean("payments", false)
        )
    }

    fun parseClinics(json: String): List<PlatformClinic> {
        val clinics = JSONObject(json).optJSONArray("clinics") ?: return emptyList()
        return buildList {
            for (index in 0 until clinics.length()) {
                val clinic = clinics.optJSONObject(index) ?: continue
                val doctor = clinic.optJSONObject("doctor") ?: continue
                val id = clinic.optString("id")
                val doctorId = doctor.optString("id")
                if (id.isBlank() || doctorId.isBlank()) continue
                add(
                    PlatformClinic(
                        id = id,
                        name = clinic.optString("name", "Clinic"),
                        city = clinic.optString("city"),
                        timeZone = clinic.optString("timeZone"),
                        doctorId = doctorId,
                        doctorName = doctor.optString("name", "Doctor"),
                        specialty = doctor.optString("specialty", "General Medicine"),
                        consultationFeeMinor = clinic.optInt("consultationFeeMinor", 0).coerceAtLeast(0)
                    )
                )
            }
        }
    }
}

class PlatformConnectionViewModel(private val api: PlatformApi) : ViewModel() {
    var uiState by mutableStateOf(PlatformConnectionState())
        private set

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun refresh() {
        if (uiState.status == PlatformConnectionStatus.CONNECTING) return
        uiState = uiState.copy(
            status = PlatformConnectionStatus.CONNECTING,
            message = "Checking the hosted prototype safely..."
        )
        executor.execute {
            val result = api.fetchPublicSnapshot()
            mainHandler.post {
                uiState = when (result) {
                    is PlatformResult.Success -> PlatformConnectionState(
                        status = PlatformConnectionStatus.CONNECTED,
                        serviceVersion = result.value.serviceVersion,
                        capabilities = result.value.capabilities,
                        clinics = result.value.clinics,
                        message = "Connected using public, read-only endpoints.",
                        checkedAtMillis = System.currentTimeMillis()
                    )
                    is PlatformResult.Failure -> PlatformConnectionState(
                        status = PlatformConnectionStatus.OFFLINE,
                        message = result.message,
                        checkedAtMillis = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}

class PlatformConnectionViewModelFactory(
    private val api: PlatformApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlatformConnectionViewModel(api) as T
}