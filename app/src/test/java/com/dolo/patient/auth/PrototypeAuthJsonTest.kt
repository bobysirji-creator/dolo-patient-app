package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PrototypeAuthJsonTest {
    private val json = """{"accessToken":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","accessExpiresAt":"2026-07-20T08:15:00Z","refreshToken":"rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr","refreshExpiresAt":"2026-08-19T08:00:00Z","identity":{"seededDummy":true}}"""
    private val identityJson = """{"identity":{"doloId":"DLO-PAT-000002","displayName":"Prototype Patient","role":"PATIENT","prototype":true},"authoritative":true,"privacy":"SELF_ONLY_NO_PHONE","productionEnrollment":"DISABLED"}"""
    private val readinessJson = """{
        "enrollment": {
            "stage": "FOUNDATION_ONLY",
            "foundationVersion": "49A",
            "demoPatientLogin": "ENABLED",
            "productionPatientEnrollment": "DISABLED",
            "enrollmentTransaction": "CONTRACT_READY_ACTIVATION_DISABLED",
            "otpUsage": "AUTHENTICATION_ONLY",
            "otpProvider": "DISABLED",
            "otpChallengeProtection": "DEDICATED_RATE_LIMIT_REQUIRED",
            "profileEnrollment": "DISABLED",
            "familyEnrollment": "DISABLED",
            "doloIdIssuance": "RESERVED",
            "doloIdAllocator": "ATOMIC_LOCATION_NEUTRAL_READY_DISABLED",
            "publicIdPolicy": {
                "format": "DLO-PAT-NNNNNN",
                "serverOwned": true,
                "editableByPatient": false,
                "derivedFromPhone": false,
                "locationEmbedded": false,
                "locationReason": "PRIVACY_STABILITY_AND_RELOCATION_SAFETY"
            },
            "requiredConsents": ["TERMS", "PRIVACY", "HEALTH_DATA"],
            "reason": "OTP_PROVIDER_NOT_CONFIGURED"
        },
        "authoritative": true,
        "privacy": "NO_PHONE_OR_PROFILE_ACCEPTED",
        "providers": "DISABLED"
    }"""

    @Test fun acceptsSelfOnlyServerOwnedIdentity() {
        val identity = PrototypeAuthJson.parseIdentityCard(identityJson)
        assertEquals("DLO-PAT-000002", identity.doloId)
        assertEquals("Prototype Patient", identity.displayName)
        assertTrue(identity.prototype)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsPhoneBearingOrUnsafeIdentityContract() {
        PrototypeAuthJson.parseIdentityCard(identityJson.replace("SELF_ONLY_NO_PHONE", "PHONE_INCLUDED"))
    }
    @Test fun acceptsOnlyFailClosedEnrollmentReadiness() {
        val readiness = PrototypeAuthJson.parseEnrollmentReadiness(readinessJson)
        assertEquals("DISABLED", readiness.productionPatientEnrollment)
        assertEquals("DISABLED", readiness.otpProvider)
        assertEquals("RESERVED", readiness.doloIdIssuance)
        assertEquals("49A", readiness.foundationVersion)
        assertEquals("DLO-PAT-NNNNNN", readiness.publicIdPolicy.format)
        assertFalse(readiness.publicIdPolicy.locationEmbedded)
        assertFalse(readiness.publicIdPolicy.derivedFromPhone)
        assertEquals(listOf("TERMS", "PRIVACY", "HEALTH_DATA"), readiness.requiredConsents)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsEnabledProductionEnrollment() {
        PrototypeAuthJson.parseEnrollmentReadiness(
            readinessJson.replace(
                "\"productionPatientEnrollment\": \"DISABLED\"",
                "\"productionPatientEnrollment\": \"ENABLED\""
            )
        )
    }


    @Test(expected = IllegalArgumentException::class)
    fun rejectsLocationBearingPublicIdPolicy() {
        PrototypeAuthJson.parseEnrollmentReadiness(
            readinessJson.replace(
                "\"locationEmbedded\": false",
                "\"locationEmbedded\": true"
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncompleteConsentContract() {
        PrototypeAuthJson.parseEnrollmentReadiness(
            readinessJson.replace(
                "[\"TERMS\", \"PRIVACY\", \"HEALTH_DATA\"]",
                "[\"TERMS\", \"PRIVACY\"]"
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
