package com.dolo.patient.data

import com.dolo.patient.data.model.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientDataTest {
 @Test fun tokenIsDeterministicAndAdvances(){val first=TokenGenerator.forBooking("1","2026-07-12",0);assertEquals(first+1,TokenGenerator.forBooking("1","2026-07-12",1));assertTrue(first>=10)}
 @Test fun appointmentRoundTrips(){val a=Appointment("1","2","Dr Test","Clinic","2026-07-12",Session.MORNING,18,rescheduleUsed=true);assertEquals(a,AppointmentCodec.decode(AppointmentCodec.encode(a)))}
 @Test fun legacyAppointmentDefaultsToUnusedReschedule(){val legacy="1|2|Dr Test|Clinic|2026-07-12|MORNING|18|BOOKED";assertFalse(AppointmentCodec.decode(legacy)!!.rescheduleUsed)}
 @Test fun malformedAppointmentIsIgnored(){assertNull(AppointmentCodec.decode("broken"))}
 @Test fun queueCalculatesPatientsAheadAndEstimate(){val a=Appointment("1","2","Dr Test","Clinic","2026-07-12",Session.MORNING,18);val q=QueueCalculator.snapshot(a,12,100);assertEquals(5,q.patientsAhead);assertEquals(60,q.estimatedMinutes);assertEquals(AppointmentStatus.WAITING,q.status)}
 @Test fun queueMarksTurnAtPatientToken(){val a=Appointment("1","2","Dr Test","Clinic","2026-07-12",Session.MORNING,18);val q=QueueCalculator.snapshot(a,18,100);assertEquals(0,q.patientsAhead);assertEquals(AppointmentStatus.IN_CONSULTATION,q.status)}
 @Test fun missedStatusOverridesQueueProgress(){val a=Appointment("1","2","Dr Test","Clinic","2026-07-12",Session.MORNING,18,AppointmentStatus.MISSED);assertEquals(AppointmentStatus.MISSED,QueueCalculator.snapshot(a,12).status)}
}
