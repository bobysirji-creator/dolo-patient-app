package com.dolo.patient.ui.login

import com.dolo.patient.auth.stage52bpPatientLegalDocumentPreview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalPreviewUiStateTest {
    @Test
    fun selectedDocumentIsResolvedOnlyFromTheVerifiedPreview() {
        val preview = stage52bpPatientLegalDocumentPreview()
        val selected = LoginUiState(
            legalDocumentPreview = preview,
            selectedLegalDocumentCategory = "PRIVACY"
        )

        assertEquals("PRIVACY", selected.selectedLegalDocument?.category)
        assertNull(selected.copy(selectedLegalDocumentCategory = "UNKNOWN").selectedLegalDocument)
    }

    @Test
    fun simulatedAcknowledgementIsExplicitlyMemoryOnlyUiState() {
        val state = LoginUiState(
            legalDocumentPreview = stage52bpPatientLegalDocumentPreview(),
            simulatedAcknowledgements = setOf("TERMS")
        )

        assertTrue("TERMS" in state.simulatedAcknowledgements)
        assertEquals("NONE", state.legalDocumentPreview?.acceptancePersistence)
        assertEquals("DISABLED", state.legalDocumentPreview?.patientConsentCollection)
    }
}
