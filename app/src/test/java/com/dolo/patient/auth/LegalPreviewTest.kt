package com.dolo.patient.auth

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class LegalPreviewTest {
    @Test
    fun acceptsOnlyThreeIntegrityProtectedTestDrafts() {
        val parsed = PrototypeAuthJson.parseLegalDocumentPreview(validJson())

        assertEquals("52B-P", parsed.foundationVersion)
        assertTrue(parsed.testOnly)
        assertEquals(listOf("TERMS", "PRIVACY", "HEALTH_DATA"), parsed.documents.map { it.category })
        assertTrue(parsed.documents.all {
            it.lifecycle == "DRAFT_TEST_ONLY" &&
                it.approvalReference == null &&
                it.effectiveDate == null &&
                it.consentCollection == "DISABLED"
        })
        assertEquals("NONE", parsed.acceptancePersistence)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAContractPresentedAsAuthoritative() {
        PrototypeAuthJson.parseLegalDocumentPreview(
            validJson().replace("\"authoritative\":false", "\"authoritative\":true")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAChangedDocumentWhoseHashWasNotUpdated() {
        PrototypeAuthJson.parseLegalDocumentPreview(
            validJson().replace("Test draft content for TERMS.", "Changed content.")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsConsentCollectionActivation() {
        PrototypeAuthJson.parseLegalDocumentPreview(
            validJson().replaceFirst(
                "\"consentCollection\":\"DISABLED\"",
                "\"consentCollection\":\"ENABLED\""
            )
        )
    }

    private fun validJson(): String {
        val documents = JSONArray()
        listOf("TERMS", "PRIVACY", "HEALTH_DATA").forEach { category ->
            val content = "Test draft content for $category."
            documents.put(
                JSONObject()
                    .put("category", category)
                    .put("version", "TEST-${category.replace("_", "-")}-001")
                    .put("language", "en-IN")
                    .put("title", "Prototype $category document")
                    .put("lifecycle", "DRAFT_TEST_ONLY")
                    .put("effectiveDate", JSONObject.NULL)
                    .put("approvalReference", JSONObject.NULL)
                    .put("banner", "TEST DRAFT - NOT LEGALLY APPROVED")
                    .put("content", content)
                    .put("contentSha256", sha256(content))
                    .put("consentCollection", "DISABLED")
            )
        }
        return JSONObject()
            .put("authoritative", false)
            .put("testOnly", true)
            .put("privacy", "NO_PATIENT_INPUT_ACCEPTED")
            .put("providers", "DISABLED")
            .put(
                "preview",
                JSONObject()
                    .put("stage", "PROTOTYPE_LEGAL_DOCUMENT_PREVIEW_ONLY")
                    .put("foundationVersion", "52B-P")
                    .put("testOnly", true)
                    .put("legalApproval", "NOT_PROVIDED")
                    .put("productionPublication", "DISABLED")
                    .put("patientDocumentViewing", "PROTOTYPE_TEST_ONLY")
                    .put("patientConsentCollection", "DISABLED")
                    .put("acceptancePersistence", "NONE")
                    .put("privacy", "NO_PATIENT_INPUT_ACCEPTED")
                    .put("documents", documents)
            )
            .toString()
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
