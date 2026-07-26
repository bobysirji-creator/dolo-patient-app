package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PrototypeAuthJsonTest {
    private val json = """{"accessToken":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","accessExpiresAt":"2026-07-20T08:15:00Z","refreshToken":"rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr","refreshExpiresAt":"2026-08-19T08:00:00Z","identity":{"seededDummy":true}}"""
    private val readinessJson = """{"enrollment":{"stage":"FOUNDATION_ONLY","demoPatientLogin":"ENABLED","productionPatientEnrollment":"DISABLED","otpUsage":"AUTHENTICATION_ONLY","otpProvider":"DISABLED","profileEnrollment":"DISABLED","familyEnrollment":"DISABLED","doloIdIssuance":"RESERVED","reason":"OTP_PROVIDER_NOT_CONFIGURED"},"authoritative":true,"privacy":"NO_PHONE_OR_PROFILE_ACCEPTED","providers":"DISABLED"}"""

    @Test fun acceptsOnlyFailClosedEnrollmentReadiness() {
        val readiness = PrototypeAuthJson.parseEnrollmentReadiness(readinessJson)
        assertEquals("DISABLED", readiness.productionPatientEnrollment)
        assertEquals("DISABLED", readiness.otpProvider)
        assertEquals("RESERVED", readiness.doloIdIssuance)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsEnabledProductionEnrollment() {
        PrototypeAuthJson.parseEnrollmentReadiness(
            readinessJson.replace(
                "\"productionPatientEnrollment\":\"DISABLED\"",
                "\"productionPatientEnrollment\":\"ENABLED\""
            )
        )
    }

    @Test fun acceptsOnlySeededDummyTokenResponse() {
        val tokens = PrototypeAuthJson.parseTokenResponse(json)
        assertEquals(43, tokens.accessToken.length)
        assertTrue(PrototypeAuthJson.hasUsableAccess(tokens, Instant.parse("2026-07-20T08:10:00Z")))
        assertFalse(PrototypeAuthJson.hasUsableAccess(tokens, Instant.parse("2026-07-20T09:00:00Z")))
        assertTrue(PrototypeAuthJson.hasUsableRefresh(tokens, Instant.parse("2026-07-21T00:00:00Z")))
        assertFalse(PrototypeAuthJson.hasUsableRefresh(tokens, Instant.parse("2026-09-01T00:00:00Z")))
    }
    @Test fun storedTokenRoundTripPreservesOpaqueValues() {
        val original = PrototypeAuthJson.parseTokenResponse(json)
        assertEquals(original, PrototypeAuthJson.parseStoredTokens(PrototypeAuthJson.encodeStoredTokens(original)))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsNonDummyIdentity() {
        PrototypeAuthJson.parseTokenResponse(json.replace("true", "false"))
    }
}
