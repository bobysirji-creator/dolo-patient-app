package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PrototypeAuthJsonTest {
    private val json = """{"accessToken":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","accessExpiresAt":"2026-07-20T08:15:00Z","refreshToken":"rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr","refreshExpiresAt":"2026-08-19T08:00:00Z","identity":{"seededDummy":true}}"""
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
