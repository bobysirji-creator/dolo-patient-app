package com.dolo.patient.auth

import org.json.JSONObject
import java.security.MessageDigest

data class PatientLegalPreviewDocument(
    val category: String,
    val version: String,
    val language: String,
    val title: String,
    val lifecycle: String,
    val effectiveDate: String?,
    val approvalReference: String?,
    val banner: String,
    val content: String,
    val contentSha256: String,
    val consentCollection: String
)

data class PatientLegalDocumentPreview(
    val stage: String,
    val foundationVersion: String,
    val testOnly: Boolean,
    val legalApproval: String,
    val productionPublication: String,
    val patientDocumentViewing: String,
    val patientConsentCollection: String,
    val acceptancePersistence: String,
    val privacy: String,
    val documents: List<PatientLegalPreviewDocument>
)

private val previewCategories = listOf("TERMS", "PRIVACY", "HEALTH_DATA")

fun stage52bpPatientLegalDocumentPreview(): PatientLegalDocumentPreview {
    val titles = listOf(
        "Prototype Terms of Use",
        "Prototype Privacy Notice",
        "Prototype Health Data Notice"
    )
    return PatientLegalDocumentPreview(
        stage = "PROTOTYPE_LEGAL_DOCUMENT_PREVIEW_ONLY",
        foundationVersion = "52B-P",
        testOnly = true,
        legalApproval = "NOT_PROVIDED",
        productionPublication = "DISABLED",
        patientDocumentViewing = "PROTOTYPE_TEST_ONLY",
        patientConsentCollection = "DISABLED",
        acceptancePersistence = "NONE",
        privacy = "NO_PATIENT_INPUT_ACCEPTED",
        documents = previewCategories.mapIndexed { index, category ->
            val content = "Test-only local preview for ${titles[index]}. No legal consent is collected."
            PatientLegalPreviewDocument(
                category = category,
                version = "TEST-${category.replace("_", "-")}-001",
                language = "en-IN",
                title = titles[index],
                lifecycle = "DRAFT_TEST_ONLY",
                effectiveDate = null,
                approvalReference = null,
                banner = "TEST DRAFT - NOT LEGALLY APPROVED",
                content = content,
                contentSha256 = sha256(content),
                consentCollection = "DISABLED"
            )
        }
    )
}

internal fun parsePatientLegalDocumentPreview(json: String): PatientLegalDocumentPreview {
    val root = JSONObject(json)
    require(
        !root.optBoolean("authoritative", true) &&
            root.optBoolean("testOnly") &&
            root.getString("privacy") == "NO_PATIENT_INPUT_ACCEPTED" &&
            root.getString("providers") == "DISABLED"
    )
    val item = root.getJSONObject("preview")
    val items = item.getJSONArray("documents")
    val documents = (0 until items.length()).map { index ->
        val document = items.getJSONObject(index)
        PatientLegalPreviewDocument(
            category = document.getString("category"),
            version = document.getString("version"),
            language = document.getString("language"),
            title = document.getString("title"),
            lifecycle = document.getString("lifecycle"),
            effectiveDate = document.optString("effectiveDate").takeIf { !document.isNull("effectiveDate") },
            approvalReference = document.optString("approvalReference").takeIf { !document.isNull("approvalReference") },
            banner = document.getString("banner"),
            content = document.getString("content"),
            contentSha256 = document.getString("contentSha256"),
            consentCollection = document.getString("consentCollection")
        )
    }
    val result = PatientLegalDocumentPreview(
        stage = item.getString("stage"),
        foundationVersion = item.getString("foundationVersion"),
        testOnly = item.getBoolean("testOnly"),
        legalApproval = item.getString("legalApproval"),
        productionPublication = item.getString("productionPublication"),
        patientDocumentViewing = item.getString("patientDocumentViewing"),
        patientConsentCollection = item.getString("patientConsentCollection"),
        acceptancePersistence = item.getString("acceptancePersistence"),
        privacy = item.getString("privacy"),
        documents = documents
    )
    require(
        result.stage == "PROTOTYPE_LEGAL_DOCUMENT_PREVIEW_ONLY" &&
            result.foundationVersion == "52B-P" &&
            result.testOnly &&
            result.legalApproval == "NOT_PROVIDED" &&
            result.productionPublication == "DISABLED" &&
            result.patientDocumentViewing == "PROTOTYPE_TEST_ONLY" &&
            result.patientConsentCollection == "DISABLED" &&
            result.acceptancePersistence == "NONE" &&
            result.privacy == "NO_PATIENT_INPUT_ACCEPTED" &&
            result.documents.map { it.category } == previewCategories
    )
    result.documents.forEach { document ->
        require(
            document.version.matches(Regex("^TEST-[A-Z-]+-001$")) &&
                document.language == "en-IN" &&
                document.title.isNotBlank() &&
                document.title.length <= 120 &&
                document.lifecycle == "DRAFT_TEST_ONLY" &&
                document.effectiveDate == null &&
                document.approvalReference == null &&
                document.banner == "TEST DRAFT - NOT LEGALLY APPROVED" &&
                document.content.isNotBlank() &&
                document.content.length <= 4_000 &&
                document.contentSha256 == sha256(document.content) &&
                document.consentCollection == "DISABLED"
        )
    }
    return result
}

private fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
