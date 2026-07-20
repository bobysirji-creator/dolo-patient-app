package com.dolo.patient.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrototypeSessionManagerTest {
    @Test fun `refreshes an expired access token and persists rotation`() {
        val expired = PrototypeTokenBundle("a".repeat(43), "2020-01-01T00:00:00Z", "r".repeat(43), "2099-01-01T00:00:00Z")
        val renewed = PrototypeTokenBundle("b".repeat(43), "2099-01-01T00:00:00Z", "s".repeat(43), "2099-02-01T00:00:00Z")
        val store = MemoryTokenStore(expired)
        val api = FakeApi(PrototypeAuthResult.Success(renewed))
        assertEquals(renewed.accessToken, PrototypeSessionManager(store, api).accessToken())
        assertEquals(renewed, store.tokens)
        assertEquals(expired.refreshToken, api.refreshToken)
    }

    @Test fun `clears tokens when refresh fails`() {
        val expired = PrototypeTokenBundle("a".repeat(43), "2020-01-01T00:00:00Z", "r".repeat(43), "2099-01-01T00:00:00Z")
        val store = MemoryTokenStore(expired)
        assertNull(PrototypeSessionManager(store, FakeApi(PrototypeAuthResult.Failure("offline"))).accessToken())
        assertNull(store.tokens)
    }

    private class MemoryTokenStore(var tokens: PrototypeTokenBundle?) : SecureTokenStore {
        override fun read() = tokens
        override fun save(tokens: PrototypeTokenBundle) { this.tokens = tokens }
        override fun clear() { tokens = null }
    }
    private class FakeApi(private val result: PrototypeAuthResult<PrototypeTokenBundle>) : PrototypeAuthApi {
        var refreshToken: String? = null
        override fun createDemoSession() = result
        override fun refresh(refreshToken: String): PrototypeAuthResult<PrototypeTokenBundle> { this.refreshToken = refreshToken; return result }
        override fun logout(accessToken: String) {}
    }
}