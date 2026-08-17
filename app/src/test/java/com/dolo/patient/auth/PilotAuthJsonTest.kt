package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotAuthJsonTest {
    @Test fun parsesInvitationOnlyReadiness() {
        val value = PrototypeAuthJson.parsePilotReadiness("""{"enabled":true,"mode":"INVITATION_ONLY","openRegistration":false,"prototypeIsolation":true,"bootstrapRequired":false,"activeAccounts":3}""")
        assertTrue(value.enabled)
        assertFalse(value.bootstrapRequired)
        assertEquals(3, value.activeAccounts)
    }

    @Test fun parsesNonDummyPatientSession() {
        val value = PrototypeAuthJson.parsePilotSessionResponse("""{"accessToken":"${"a".repeat(43)}","accessExpiresAt":"2026-08-17T10:00:00Z","refreshToken":"${"r".repeat(43)}","refreshExpiresAt":"2026-09-17T10:00:00Z","identity":{"doloId":"DLO-PAT-000002","displayName":"Pilot Patient","role":"PATIENT","seededDummy":false,"controlledPilot":true}}""")
        assertEquals("DLO-PAT-000002", value.identity.doloId)
        assertEquals("PATIENT", value.identity.role)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSeededIdentityAtPilotBoundary() {
        PrototypeAuthJson.parsePilotSessionResponse("""{"accessToken":"${"a".repeat(43)}","accessExpiresAt":"2026-08-17T10:00:00Z","refreshToken":"${"r".repeat(43)}","refreshExpiresAt":"2026-09-17T10:00:00Z","identity":{"doloId":"DLO-PAT-000001","displayName":"Demo","role":"PATIENT","seededDummy":true,"controlledPilot":false}}""")
    }
}