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

data class PublicIdentityCard(val doloId:String,val displayName:String,val role:String,val prototype:Boolean)
data class PilotReadiness(val enabled:Boolean,val bootstrapRequired:Boolean,val activeAccounts:Int)
data class PilotIdentity(val doloId:String,val displayName:String,val role:String)
data class PilotSessionResult(val tokens:PrototypeTokenBundle,val identity:PilotIdentity)

data class PatientPublicIdPolicy(
    val format: String,
    val serverOwned: Boolean,
    val editableByPatient: Boolean,
    val derivedFromPhone: Boolean,
    val locationEmbedded: Boolean,
    val locationReason: String
)

data class PatientEnrollmentReadiness(
    val stage: String,
    val foundationVersion: String,
    val demoPatientLogin: String,
    val productionPatientEnrollment: String,
    val enrollmentTransaction: String,
    val otpUsage: String,
    val otpProvider: String,
    val otpChallengeProtection: String,
    val profileEnrollment: String,
    val familyEnrollment: String,
    val doloIdIssuance: String,
    val doloIdAllocator: String,
    val publicIdPolicy: PatientPublicIdPolicy,
    val requiredConsents: List<String>,
    val reason: String
)

fun stage49aPatientEnrollmentReadiness(demoPatientLogin: String = "ENABLED") =
    PatientEnrollmentReadiness(
        stage = "FOUNDATION_ONLY",
        foundationVersion = "49A",
        demoPatientLogin = demoPatientLogin,
        productionPatientEnrollment = "DISABLED",
        enrollmentTransaction = "CONTRACT_READY_ACTIVATION_DISABLED",
        otpUsage = "AUTHENTICATION_ONLY",
        otpProvider = "DISABLED",
        otpChallengeProtection = "DEDICATED_RATE_LIMIT_REQUIRED",
        profileEnrollment = "DISABLED",
        familyEnrollment = "DISABLED",
        doloIdIssuance = "RESERVED",
        doloIdAllocator = "ATOMIC_LOCATION_NEUTRAL_READY_DISABLED",
        publicIdPolicy = PatientPublicIdPolicy(
            format = "DLO-PAT-NNNNNN",
            serverOwned = true,
            editableByPatient = false,
            derivedFromPhone = false,
            locationEmbedded = false,
            locationReason = "PRIVACY_STABILITY_AND_RELOCATION_SAFETY"
        ),
        requiredConsents = listOf("TERMS", "PRIVACY", "HEALTH_DATA"),
        reason = "OTP_PROVIDER_NOT_CONFIGURED"
    )

data class PatientEnrollmentActivationGate(
    val key: String,
    val status: String,
    val evidence: String
)

data class PatientEnrollmentActivationRequirements(
    val stage: String,
    val foundationVersion: String,
    val activationDecision: String,
    val productionPatientEnrollment: String,
    val realPatientDataAcceptance: String,
    val otpUsage: String,
    val otpProvider: String,
    val distributedAbuseProtection: String,
    val publicIdIssuance: String,
    val enrollmentTransaction: String,
    val gates: List<PatientEnrollmentActivationGate>,
    val nextReview: String
)

private val PATIENT_ENROLLMENT_ACTIVATION_GATE_KEYS = listOf(
    "MANAGED_OTP_PROVIDER",
    "DISTRIBUTED_ABUSE_PROTECTION",
    "VERSIONED_LEGAL_CONSENTS",
    "ACCOUNT_RECOVERY_AND_DUPLICATE_POLICY",
    "RETENTION_CORRECTION_AND_DELETION_POLICY",
    "INDIA_PRODUCTION_SECURITY_REVIEW",
    "ATOMIC_ENROLLMENT_TRANSACTION_REVIEW"
)

fun stage50aPatientEnrollmentActivationRequirements() =
    PatientEnrollmentActivationRequirements(
        stage = "ACTIVATION_GATES_ONLY",
        foundationVersion = "50A",
        activationDecision = "BLOCKED",
        productionPatientEnrollment = "DISABLED",
        realPatientDataAcceptance = "DISABLED",
        otpUsage = "AUTHENTICATION_ONLY",
        otpProvider = "DISABLED",
        distributedAbuseProtection = "REQUIRED_NOT_CONFIGURED",
        publicIdIssuance = "RESERVED",
        enrollmentTransaction = "NOT_CALLABLE",
        gates = PATIENT_ENROLLMENT_ACTIVATION_GATE_KEYS.map {
            PatientEnrollmentActivationGate(it, "BLOCKED", "NOT_APPROVED")
        },
        nextReview = "SECURITY_LEGAL_PROVIDER_AND_OPERATIONS_APPROVAL_REQUIRED"
    )
data class PatientEnrollmentConsentRequirement(
    val category: String,
    val version: String,
    val lifecycle: String,
    val language: String,
    val content: String,
    val collection: String
)

data class PatientEnrollmentConsentCatalog(
    val stage: String,
    val foundationVersion: String,
    val activationGate: String,
    val activationGateStatus: String,
    val legalReview: String,
    val patientConsentCollection: String,
    val requirements: List<PatientEnrollmentConsentRequirement>,
    val privacy: String,
    val reason: String
)

fun stage51aPatientEnrollmentConsentCatalog() =
    PatientEnrollmentConsentCatalog(
        stage = "CONSENT_CATALOG_FOUNDATION_ONLY",
        foundationVersion = "51A",
        activationGate = "VERSIONED_LEGAL_CONSENTS",
        activationGateStatus = "BLOCKED",
        legalReview = "REQUIRED",
        patientConsentCollection = "DISABLED",
        requirements = listOf("TERMS", "PRIVACY", "HEALTH_DATA").map {
            PatientEnrollmentConsentRequirement(
                category = it,
                version = "RESERVED",
                lifecycle = "RESERVED",
                language = "en-IN",
                content = "NOT_PUBLISHED",
                collection = "DISABLED"
            )
        },
        privacy = "NO_PATIENT_CONSENT_ACCEPTED",
        reason = "APPROVED_DOCUMENT_VERSIONS_NOT_CONFIGURED"
    )
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
    fun pilotReadiness():PrototypeAuthResult<PilotReadiness>
    fun pilotLogin(doloId:String,credential:String):PrototypeAuthResult<PilotSessionResult>
    fun activatePilot(inviteCode:String,credential:String):PrototypeAuthResult<PilotSessionResult>
    fun enrollmentReadiness(): PrototypeAuthResult<PatientEnrollmentReadiness>
    fun enrollmentActivationRequirements(): PrototypeAuthResult<PatientEnrollmentActivationRequirements>
    fun enrollmentConsentCatalog(): PrototypeAuthResult<PatientEnrollmentConsentCatalog>
    fun legalDocumentPreview(): PrototypeAuthResult<PatientLegalDocumentPreview>
    fun identityCard(accessToken:String): PrototypeAuthResult<PublicIdentityCard>
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

    override fun pilotReadiness():PrototypeAuthResult<PilotReadiness> = runCatching { PrototypeAuthJson.parsePilotReadiness(get("/api/v1/auth/pilot/readiness")) }.fold({PrototypeAuthResult.Success(it)},{PrototypeAuthResult.Failure("Controlled pilot is temporarily unavailable.")})
    override fun pilotLogin(doloId:String,credential:String)=pilotCall("/api/v1/auth/pilot/sessions",JSONObject().put("doloId",doloId.trim().uppercase()).put("credential",credential).put("deviceLabel","DO-LO Patient Android").toString())
    override fun activatePilot(inviteCode:String,credential:String)=pilotCall("/api/v1/auth/pilot/activate",JSONObject().put("inviteCode",inviteCode.trim()).put("expectedRole","PATIENT").put("credential",credential).put("deviceLabel","DO-LO Patient Android").toString())

    override fun enrollmentReadiness():PrototypeAuthResult<PatientEnrollmentReadiness> = runCatching { PrototypeAuthJson.parseEnrollmentReadiness(get("/api/v1/auth/patient-enrollment/readiness")) }.fold({PrototypeAuthResult.Success(it)},{PrototypeAuthResult.Failure("Production registration status is temporarily unavailable.")})

    override fun enrollmentActivationRequirements(): PrototypeAuthResult<PatientEnrollmentActivationRequirements> = runCatching {
        PrototypeAuthJson.parseEnrollmentActivationRequirements(get("/api/v1/auth/patient-enrollment/requirements"))
    }.fold(
        { PrototypeAuthResult.Success(it) },
        { PrototypeAuthResult.Failure("Production registration requirements are temporarily unavailable.") }
    )
    override fun enrollmentConsentCatalog(): PrototypeAuthResult<PatientEnrollmentConsentCatalog> = runCatching {
        PrototypeAuthJson.parseEnrollmentConsentCatalog(
            get("/api/v1/auth/patient-enrollment/consent-requirements")
        )
    }.fold(
        { PrototypeAuthResult.Success(it) },
        { PrototypeAuthResult.Failure("Production consent catalog is temporarily unavailable.") }
    )

    override fun legalDocumentPreview():PrototypeAuthResult<PatientLegalDocumentPreview> = runCatching { PrototypeAuthJson.parseLegalDocumentPreview(get("/api/v1/auth/patient-enrollment/legal-document-preview")) }.fold({PrototypeAuthResult.Success(it)},{PrototypeAuthResult.Failure("Test legal-document preview is temporarily unavailable.")})

    override fun identityCard(accessToken:String):PrototypeAuthResult<PublicIdentityCard> = runCatching { PrototypeAuthJson.parseIdentityCard(get("/api/v1/auth/identity-card",accessToken)) }.fold({PrototypeAuthResult.Success(it)},{PrototypeAuthResult.Failure("Hosted DO-LO identity is temporarily unavailable.")})

    override fun createDemoSession() = call(
        "/api/v1/auth/prototype/sessions",
        JSONObject().put("identity", "patient-demo").put("otp", FakeAuthRepository.DEMO_OTP).put("deviceLabel", "DO-LO Patient Android").toString()
    )

    override fun refresh(refreshToken: String) = call(
        "/api/v1/auth/refresh",
        JSONObject().put("refreshToken", refreshToken).toString()
    )

    override fun logout(accessToken: String) { runCatching { post("/api/v1/auth/logout", "{}", accessToken) } }

    private fun pilotCall(path:String,body:String):PrototypeAuthResult<PilotSessionResult> = runCatching { PrototypeAuthJson.parsePilotSessionResponse(post(path,body)) }.fold(
        {PrototypeAuthResult.Success(it)},
        {PrototypeAuthResult.Failure(when(it){is java.net.SocketTimeoutException->"Pilot service timed out.";is java.net.UnknownHostException->"No network connection.";else->it.message?.take(160)?:"Pilot sign-in failed."})}
    )

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

    private fun get(path:String,bearer:String?=null):String{
        val connection=(URL(baseUrl+path).openConnection() as HttpURLConnection).apply{requestMethod="GET";connectTimeout=connectTimeoutMillis;readTimeout=readTimeoutMillis;setRequestProperty("Accept","application/json");setRequestProperty("User-Agent","DO-LO-Patient-Android/Stage63PB");bearer?.let{setRequestProperty("Authorization","Bearer $it")};useCaches=false}
        return try{val status=connection.responseCode;val response=(if(status in 200..299)connection.inputStream else connection.errorStream)?.bufferedReader(Charsets.UTF_8)?.use(::readBounded).orEmpty();if(status !in 200..299)error("Enrollment readiness returned HTTP $status");response}finally{connection.disconnect()}
    }

    private fun post(path: String, body: String, bearer: String? = null): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = connectTimeoutMillis; readTimeout = readTimeoutMillis
            setRequestProperty("Content-Type", "application/json"); setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "DO-LO-Patient-Android/Stage63PB")
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
    fun parsePilotReadiness(json:String):PilotReadiness { val root=JSONObject(json);require(root.getString("mode")=="INVITATION_ONLY"&&!root.getBoolean("openRegistration")&&root.getBoolean("prototypeIsolation"));return PilotReadiness(root.getBoolean("enabled"),root.getBoolean("bootstrapRequired"),root.getInt("activeAccounts")) }
    fun parsePilotSessionResponse(json:String):PilotSessionResult { val root=JSONObject(json);val item=root.getJSONObject("identity");require(item.optBoolean("controlledPilot")&&!item.optBoolean("seededDummy"));val identity=PilotIdentity(item.getString("doloId"),item.getString("displayName"),item.getString("role"));require(identity.doloId.matches(Regex("^DLO-(PAT|DOC|ADM)-[0-9]{6}$"))&&identity.displayName.isNotBlank());return PilotSessionResult(bundle(root),identity) }
    fun parseLegalDocumentPreview(json:String):PatientLegalDocumentPreview = parsePatientLegalDocumentPreview(json)

    fun parseIdentityCard(json:String):PublicIdentityCard{val root=JSONObject(json);require(root.optBoolean("authoritative")&&root.getString("privacy")=="SELF_ONLY_NO_PHONE"&&root.getString("productionEnrollment")=="DISABLED");val item=root.getJSONObject("identity");val result=PublicIdentityCard(item.getString("doloId"),item.getString("displayName"),item.getString("role"),item.getBoolean("prototype"));require(result.doloId.matches(Regex("^DLO-(PAT|DOC|AST|ADM)-[0-9]{6}$"))&&result.displayName.isNotBlank()&&result.displayName.length<=120&&result.role in setOf("PATIENT","DOCTOR","ASSISTANT","ADMIN"));return result}

    fun parseEnrollmentReadiness(json: String): PatientEnrollmentReadiness {
        val root = JSONObject(json)
        require(
            root.optBoolean("authoritative") &&
                root.getString("privacy") == "NO_PHONE_OR_PROFILE_ACCEPTED" &&
                root.getString("providers") == "DISABLED"
        )
        val item = root.getJSONObject("enrollment")
        val policy = item.getJSONObject("publicIdPolicy")
        val consents = item.getJSONArray("requiredConsents")
        val result = PatientEnrollmentReadiness(
            stage = item.getString("stage"),
            foundationVersion = item.getString("foundationVersion"),
            demoPatientLogin = item.getString("demoPatientLogin"),
            productionPatientEnrollment = item.getString("productionPatientEnrollment"),
            enrollmentTransaction = item.getString("enrollmentTransaction"),
            otpUsage = item.getString("otpUsage"),
            otpProvider = item.getString("otpProvider"),
            otpChallengeProtection = item.getString("otpChallengeProtection"),
            profileEnrollment = item.getString("profileEnrollment"),
            familyEnrollment = item.getString("familyEnrollment"),
            doloIdIssuance = item.getString("doloIdIssuance"),
            doloIdAllocator = item.getString("doloIdAllocator"),
            publicIdPolicy = PatientPublicIdPolicy(
                format = policy.getString("format"),
                serverOwned = policy.getBoolean("serverOwned"),
                editableByPatient = policy.getBoolean("editableByPatient"),
                derivedFromPhone = policy.getBoolean("derivedFromPhone"),
                locationEmbedded = policy.getBoolean("locationEmbedded"),
                locationReason = policy.getString("locationReason")
            ),
            requiredConsents = (0 until consents.length()).map(consents::getString),
            reason = item.getString("reason")
        )
        require(
            result == stage49aPatientEnrollmentReadiness(result.demoPatientLogin) &&
                result.demoPatientLogin in setOf("ENABLED", "DISABLED")
        )
        return result
    }

    fun parseEnrollmentActivationRequirements(json: String): PatientEnrollmentActivationRequirements {
        val root = JSONObject(json)
        require(
            root.optBoolean("authoritative") &&
                root.getString("privacy") == "NO_PATIENT_INPUT_ACCEPTED" &&
                root.getString("providers") == "DISABLED"
        )
        val item = root.getJSONObject("requirements")
        val gates = item.getJSONArray("gates")
        val result = PatientEnrollmentActivationRequirements(
            stage = item.getString("stage"),
            foundationVersion = item.getString("foundationVersion"),
            activationDecision = item.getString("activationDecision"),
            productionPatientEnrollment = item.getString("productionPatientEnrollment"),
            realPatientDataAcceptance = item.getString("realPatientDataAcceptance"),
            otpUsage = item.getString("otpUsage"),
            otpProvider = item.getString("otpProvider"),
            distributedAbuseProtection = item.getString("distributedAbuseProtection"),
            publicIdIssuance = item.getString("publicIdIssuance"),
            enrollmentTransaction = item.getString("enrollmentTransaction"),
            gates = (0 until gates.length()).map { index ->
                gates.getJSONObject(index).let { gate ->
                    PatientEnrollmentActivationGate(
                        key = gate.getString("key"),
                        status = gate.getString("status"),
                        evidence = gate.getString("evidence")
                    )
                }
            },
            nextReview = item.getString("nextReview")
        )
        require(result == stage50aPatientEnrollmentActivationRequirements())
        return result
    }
    fun parseEnrollmentConsentCatalog(json: String): PatientEnrollmentConsentCatalog {
        val root = JSONObject(json)
        require(
            root.optBoolean("authoritative") &&
                root.getString("privacy") == "NO_PATIENT_INPUT_ACCEPTED" &&
                root.getString("providers") == "DISABLED"
        )
        val item = root.getJSONObject("catalog")
        val requirements = item.getJSONArray("requirements")
        val result = PatientEnrollmentConsentCatalog(
            stage = item.getString("stage"),
            foundationVersion = item.getString("foundationVersion"),
            activationGate = item.getString("activationGate"),
            activationGateStatus = item.getString("activationGateStatus"),
            legalReview = item.getString("legalReview"),
            patientConsentCollection = item.getString("patientConsentCollection"),
            requirements = (0 until requirements.length()).map { index ->
                requirements.getJSONObject(index).let { requirement ->
                    PatientEnrollmentConsentRequirement(
                        category = requirement.getString("category"),
                        version = requirement.getString("version"),
                        lifecycle = requirement.getString("lifecycle"),
                        language = requirement.getString("language"),
                        content = requirement.getString("content"),
                        collection = requirement.getString("collection")
                    )
                }
            },
            privacy = item.getString("privacy"),
            reason = item.getString("reason")
        )
        require(result == stage51aPatientEnrollmentConsentCatalog())
        return result
    }
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
