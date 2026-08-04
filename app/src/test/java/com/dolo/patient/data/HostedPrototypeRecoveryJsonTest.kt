package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HostedPrototypeRecoveryJsonTest {
    private val valid="""{"authoritative":true,"simulationOnly":true,"accountChange":false,"cases":[{"id":"90000000-0000-4000-8000-000000000053","patientDoloId":"DLO-PAT-000001","caseType":"LOST_DEVICE","status":"OPEN","outcome":"PENDING_TEST_ONLY","createdAt":"2026-08-04T12:00:00Z","updatedAt":"2026-08-04T12:00:00Z","events":[{"sequence":"1","actorRole":"PATIENT","action":"PATIENT_SIMULATION_CREATED","outcome":"PENDING_TEST_ONLY","occurredAt":"2026-08-04T12:00:00Z"}]}]}"""
    @Test fun parsesNoChangeSimulation(){val item=HostedPrototypeRecoveryJson.parse(valid).single();assertEquals("DLO-PAT-000001",item.patientDoloId);assertEquals("LOST_DEVICE",item.caseType);assertEquals(1,item.events.size)}
    @Test fun rejectsAccountChange(){assertThrows(IllegalArgumentException::class.java){HostedPrototypeRecoveryJson.parse(valid.replace("\"accountChange\":false","\"accountChange\":true"))}}
    @Test fun rejectsPhoneBearingPayload(){assertThrows(IllegalArgumentException::class.java){HostedPrototypeRecoveryJson.parse(valid.replace("\"caseType\":\"LOST_DEVICE\"","\"phone\":\"9999999999\",\"caseType\":\"LOST_DEVICE\""))}}
}
