package com.dolo.patient.data

import com.dolo.patient.data.model.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientDataTest {
 @Test fun tokenIsDeterministicAndAdvances(){val first=TokenGenerator.forBooking("1","2026-07-12",0);assertEquals(first+1,TokenGenerator.forBooking("1","2026-07-12",1));assertTrue(first>=10)}
 @Test fun appointmentRoundTrips(){val a=Appointment("1","2","Dr Test","Clinic","2026-07-12",Session.MORNING,18);assertEquals(a,AppointmentCodec.decode(AppointmentCodec.encode(a)))}
 @Test fun malformedAppointmentIsIgnored(){assertNull(AppointmentCodec.decode("broken"))}
}
