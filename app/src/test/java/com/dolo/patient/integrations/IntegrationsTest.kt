package com.dolo.patient.integrations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrationsTest {
    @Test
    fun allCapabilitiesAreDisabledUntilAProviderIsConfigured() {
        assertEquals(4, IntegrationRegistry.patientCapabilities.size)
        assertTrue(
            IntegrationRegistry.patientCapabilities.all { capability ->
                capability.mode == IntegrationMode.DISABLED
            }
        )
        assertTrue(
            IntegrationRegistry.patientCapabilities.all { capability ->
                capability.providerName == null
            }
        )
    }

    @Test
    fun capabilityLookupReturnsTheRequestedIntegration() {
        val capability = IntegrationRegistry.capability(IntegrationType.SMS)

        assertEquals(IntegrationType.SMS, capability.type)
        assertEquals("SMS messages", capability.title)
    }

    @Test
    fun disabledMapsProviderNeverBuildsALiveNavigationLink() {
        val result = DisabledMapsProvider.navigationUri(
            latitude = 28.6139,
            longitude = 77.2090,
            clinicLabel = "Demo Clinic"
        )

        assertTrue(result is ProviderResult.Unavailable)
        assertEquals(
            "MAPS_DISABLED",
            (result as ProviderResult.Unavailable).code
        )
    }

    @Test
    fun disabledPaymentsNeverCreateAnOrder() {
        val result = DisabledPaymentProvider.createOrder(
            appointmentId = "appointment-1",
            amountPaise = 50000
        )

        assertTrue(result is ProviderResult.Unavailable)
        assertEquals(
            "PAYMENTS_DISABLED",
            (result as ProviderResult.Unavailable).code
        )
    }
}
