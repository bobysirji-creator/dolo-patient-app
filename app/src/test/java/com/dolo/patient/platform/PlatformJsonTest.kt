package com.dolo.patient.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformJsonTest {
    @Test
    fun parsesAcceptedHostedHealthAndCapabilities() {
        assertEquals(
            "0.1.0-stage15.6",
            PlatformJson.parseHealth(
                """{"status":"ok","version":"0.1.0-stage15.6"}"""
            )
        )

        val capabilities = PlatformJson.parseCapabilities(
            """{
                "stage":15.6,
                "transport":"HARDENED_AUDITED_PROVIDER_BOUNDARY",
                "databaseConnected":true,
                "authenticationEnabled":false,
                "providers":{"sms":false,"push":false,"maps":false,"payments":false}
            }""".trimIndent()
        )

        assertEquals("15.6", capabilities.stage)
        assertTrue(capabilities.databaseConnected)
        assertFalse(capabilities.authenticationEnabled)
        assertFalse(capabilities.smsEnabled)
        assertFalse(capabilities.pushEnabled)
        assertFalse(capabilities.mapsEnabled)
        assertFalse(capabilities.paymentsEnabled)
    }

    @Test
    fun parsesPublicClinicDiscoveryWithoutInventingClinicalDetails() {
        val clinics = PlatformJson.parseClinics(
            """{
                "clinics":[{
                    "id":"clinic-1",
                    "name":"DO-LO Test Clinic",
                    "timeZone":"Asia/Kolkata",
                    "city":"Mumbai",
                    "active":true,
                    "doctor":{"id":"doctor-1","name":"Dr Demo","specialty":"General Medicine"},
                    "consultationFeeMinor":50000
                }]
            }""".trimIndent()
        )

        assertEquals(1, clinics.size)
        assertEquals("DO-LO Test Clinic", clinics.single().name)
        assertEquals("Dr Demo", clinics.single().doctorName)
        assertEquals(50000, clinics.single().consultationFeeMinor)
    }

    @Test
    fun ignoresMalformedClinicRows() {
        val clinics = PlatformJson.parseClinics(
            """{"clinics":[{"name":"Missing identifiers"},{"id":"clinic-2","doctor":{}}]}"""
        )

        assertTrue(clinics.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonHttpsPlatformBaseUrl() {
        HttpPlatformApi("http://example.test")
    }
}