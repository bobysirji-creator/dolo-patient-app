package com.dolo.patient.auth

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class PrototypeTokenBundle(
    val accessToken: String,
    val accessExpiresAt: String,
    val refreshToken: String,
    val refreshExpiresAt: String
)

interface SecureTokenStore {
    fun read(): PrototypeTokenBundle?
    fun save(tokens: PrototypeTokenBundle)
    fun clear()
}

class AndroidKeystoreTokenStore(private val preferences: SharedPreferences) : SecureTokenStore {
    override fun read(): PrototypeTokenBundle? = runCatching {
        val iv = preferences.getString(KEY_IV, null) ?: return null
        val ciphertext = preferences.getString(KEY_CIPHERTEXT, null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, Base64.getDecoder().decode(iv)))
        PrototypeAuthJson.parseStoredTokens(String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), Charsets.UTF_8))
    }.getOrElse { clear(); null }

    override fun save(tokens: PrototypeTokenBundle) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(PrototypeAuthJson.encodeStoredTokens(tokens).toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString(KEY_IV, Base64.getEncoder().encodeToString(cipher.iv))
            .putString(KEY_CIPHERTEXT, Base64.getEncoder().encodeToString(encrypted))
            .apply()
    }

    override fun clear() { preferences.edit().remove(KEY_IV).remove(KEY_CIPHERTEXT).apply() }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build())
        return generator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "dolo_patient_prototype_tokens_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_IV = "prototype_token_iv"
        const val KEY_CIPHERTEXT = "prototype_token_ciphertext"
    }
}

sealed interface PrototypeAuthResult<out T> {
    data class Success<T>(val value: T) : PrototypeAuthResult<T>
    data class Failure(val message: String) : PrototypeAuthResult<Nothing>
}

class PrototypeSessionManager(private val store: SecureTokenStore, private val api: PrototypeAuthApi) {
    @Synchronized fun accessToken(): String? {
        val current = store.read() ?: return null
        if (PrototypeAuthJson.hasUsableAccess(current)) return current.accessToken
        if (!PrototypeAuthJson.hasUsableRefresh(current)) { store.clear(); return null }
        return when (val refreshed = api.refresh(current.refreshToken)) {
            is PrototypeAuthResult.Success -> { store.save(refreshed.value); refreshed.value.accessToken }
            is PrototypeAuthResult.Failure -> { store.clear(); null }
        }
    }
}

interface PrototypeAuthApi {
    fun createDemoSession(): PrototypeAuthResult<PrototypeTokenBundle>
    fun refresh(refreshToken: String): PrototypeAuthResult<PrototypeTokenBundle>
    fun logout(accessToken: String)
}

class HttpPrototypeAuthApi(
    baseUrl: String,
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 25_000
) : PrototypeAuthApi {
    private val baseUrl = baseUrl.trim().trimEnd('/')
    init { require(URL(this.baseUrl).protocol.equals("https", true)) { "Prototype auth requires HTTPS." } }

    override fun createDemoSession() = call(
        "/api/v1/auth/prototype/sessions",
        JSONObject().put("identity", "patient-demo").put("otp", FakeAuthRepository.DEMO_OTP).put("deviceLabel", "DO-LO Patient Android").toString()
    )

    override fun refresh(refreshToken: String) = call(
        "/api/v1/auth/refresh",
        JSONObject().put("refreshToken", refreshToken).toString()
    )

    override fun logout(accessToken: String) { runCatching { post("/api/v1/auth/logout", "{}", accessToken) } }

    private fun call(path: String, body: String): PrototypeAuthResult<PrototypeTokenBundle> = runCatching {
        PrototypeAuthJson.parseTokenResponse(post(path, body))
    }.fold(
        { PrototypeAuthResult.Success(it) },
        { PrototypeAuthResult.Failure(when (it) {
            is java.net.SocketTimeoutException -> "Hosted identity timed out; local demo mode was used."
            is java.net.UnknownHostException -> "No network connection; local demo mode was used."
            else -> "Hosted identity unavailable; local demo mode was used."
        }) }
    )

    private fun post(path: String, body: String, bearer: String? = null): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = connectTimeoutMillis; readTimeout = readTimeoutMillis
            setRequestProperty("Content-Type", "application/json"); setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "DO-LO-Patient-Android/Stage16B")
            bearer?.let { setRequestProperty("Authorization", "Bearer $it") }
            useCaches = false
        }
        return try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use(::readBounded).orEmpty()
            if (status !in 200..299) error("Hosted identity returned HTTP $status")
            response
        } finally { connection.disconnect() }
    }

    private fun readBounded(reader: java.io.BufferedReader): String {
        val output = StringBuilder()
        val buffer = CharArray(8_192)
        while (true) {
            val count = reader.read(buffer)
            if (count < 0) return output.toString()
            require(output.length + count <= 262_144) { "Hosted identity response was unexpectedly large." }
            output.append(buffer, 0, count)
        }
    }
}

object PrototypeAuthJson {
    fun parseTokenResponse(json: String): PrototypeTokenBundle {
        val root = JSONObject(json)
        require(root.optJSONObject("identity")?.optBoolean("seededDummy") == true) { "Not a seeded dummy identity." }
        return bundle(root)
    }
    fun encodeStoredTokens(tokens: PrototypeTokenBundle): String = JSONObject()
        .put("accessToken", tokens.accessToken).put("accessExpiresAt", tokens.accessExpiresAt)
        .put("refreshToken", tokens.refreshToken).put("refreshExpiresAt", tokens.refreshExpiresAt).toString()
    fun parseStoredTokens(json: String): PrototypeTokenBundle = bundle(JSONObject(json))
    fun hasUsableAccess(tokens: PrototypeTokenBundle, now: Instant = Instant.now()): Boolean =
        runCatching { Instant.parse(tokens.accessExpiresAt).isAfter(now) }.getOrDefault(false)
    fun hasUsableRefresh(tokens: PrototypeTokenBundle, now: Instant = Instant.now()): Boolean =
        runCatching { Instant.parse(tokens.refreshExpiresAt).isAfter(now) }.getOrDefault(false)
    private fun bundle(root: JSONObject): PrototypeTokenBundle {
        val result = PrototypeTokenBundle(
            root.getString("accessToken"), root.getString("accessExpiresAt"),
            root.getString("refreshToken"), root.getString("refreshExpiresAt")
        )
        require(result.accessToken.length >= 32 && result.refreshToken.length >= 32) { "Invalid opaque token response." }
        Instant.parse(result.accessExpiresAt); Instant.parse(result.refreshExpiresAt)
        return result
    }
}
