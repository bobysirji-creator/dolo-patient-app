package com.dolo.patient.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HostedSupportJsonTest {
 @Test fun parsesAuthoritativePatientSupportHistory(){val requests=HostedSupportJson.parse("""{"authoritative":true,"supportRequests":[{"id":"80000000-0000-4000-8000-000000000026","category":"APP","subject":"Hosted login problem","message":"The hosted login is not opening correctly.","status":"RESOLVED","adminNote":"Please sign in again while online.","submittedAt":"2026-07-23T08:00:00Z","updatedAt":"2026-07-23T08:05:00Z"}]}""");assertEquals(1,requests.size);assertEquals("RESOLVED",requests.single().status);assertEquals("Please sign in again while online.",requests.single().adminNote)}
 @Test(expected=IllegalArgumentException::class) fun rejectsUnknownStatus(){HostedSupportJson.parse("""{"authoritative":true,"supportRequests":[{"id":"1","category":"APP","subject":"Subject text","message":"A sufficiently long support request message.","status":"DELETED","adminNote":"","submittedAt":"2026-07-23T08:00:00Z","updatedAt":"2026-07-23T08:00:00Z"}]}""")}
}