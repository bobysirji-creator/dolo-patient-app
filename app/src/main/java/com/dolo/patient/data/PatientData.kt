package com.dolo.patient.data

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.Session
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

object AppointmentStatus {
 const val BOOKED="BOOKED"; const val WAITING="WAITING"; const val IN_CONSULTATION="IN_CONSULTATION"
 const val COMPLETED="COMPLETED"; const val MISSED="MISSED"
}
data class Appointment(val id:String,val doctorId:String,val doctorName:String,val clinic:String,val date:String,val session:Session,val token:Int,val status:String=AppointmentStatus.BOOKED,val rescheduleUsed:Boolean=false)
data class QueueSnapshot(val appointmentId:String,val patientToken:Int,val currentToken:Int,val patientsAhead:Int,val estimatedMinutes:Int,val status:String,val refreshedAt:Long)

sealed interface ApiResult<out T>{data class Success<T>(val value:T):ApiResult<T>;data class Failure(val message:String,val retryable:Boolean=true):ApiResult<Nothing>}
interface PatientApi{suspend fun doctors(query:String?):ApiResult<List<Doctor>>;suspend fun appointments():ApiResult<List<Appointment>>;suspend fun book(doctorId:String,date:String,session:Session):ApiResult<Appointment>}
interface PatientRepository{
 fun doctors(query:String="",specialty:String?=null):List<Doctor>;fun appointments():List<Appointment>;fun activeAppointment():Appointment?
 fun book(doctorId:String,date:String,session:Session):Appointment;fun favouriteDoctorIds():Set<String>;fun toggleFavourite(doctorId:String):Set<String>
 fun queue(appointmentId:String):QueueSnapshot?;fun advanceQueue(appointmentId:String):QueueSnapshot?;fun markMissed(appointmentId:String):Appointment?
 fun canReschedule(appointment:Appointment):Boolean;fun reschedule(appointmentId:String):Appointment?
}
object TokenGenerator{fun forBooking(doctorId:String,date:String,existingCount:Int)=10+((doctorId.hashCode()+date.hashCode()).absoluteValue%20)+existingCount}
object QueueCalculator{
 const val AVERAGE_CONSULTATION_MINUTES=12
 fun snapshot(a:Appointment,currentToken:Int,refreshedAt:Long=System.currentTimeMillis()):QueueSnapshot{
  val current=currentToken.coerceIn(1,a.token);val ahead=(a.token-current-1).coerceAtLeast(0)
  val status=when{a.status==AppointmentStatus.MISSED->AppointmentStatus.MISSED;current>=a.token->AppointmentStatus.IN_CONSULTATION;else->AppointmentStatus.WAITING}
  return QueueSnapshot(a.id,a.token,current,ahead,ahead*AVERAGE_CONSULTATION_MINUTES,status,refreshedAt)
 }
}
object AppointmentCodec{
 fun encode(v:Appointment)=listOf(v.id,v.doctorId,v.doctorName,v.clinic,v.date,v.session.name,v.token,v.status,v.rescheduleUsed).joinToString("|")
 fun decode(value:String):Appointment?{val p=value.split("|");if(p.size !in 8..9)return null;return runCatching{Appointment(p[0],p[1],p[2],p[3],p[4],Session.valueOf(p[5]),p[6].toInt(),p[7],p.getOrNull(8)?.toBooleanStrictOrNull()?:false)}.getOrNull()}
}
class LocalPatientRepository(private val preferences:SharedPreferences):PatientRepository{
 override fun doctors(query:String,specialty:String?):List<Doctor>{val q=query.trim();return DummyData.doctors.filter{(specialty.isNullOrBlank()||it.specialty==specialty)&&(q.isBlank()||it.name.contains(q,true)||it.specialty.contains(q,true)||it.clinic.contains(q,true))}}
 override fun appointments()=preferences.getStringSet(KEY_APPOINTMENTS,emptySet()).orEmpty().mapNotNull(AppointmentCodec::decode).sortedByDescending{it.id}
 override fun activeAppointment()=appointments().firstOrNull{it.status in setOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION)}
 override fun book(doctorId:String,date:String,session:Session):Appointment{val doctor=DummyData.doctors.first{it.id==doctorId};val selected=runCatching{LocalDate.parse(date)}.getOrDefault(LocalDate.now()).let{if(it.isBefore(LocalDate.now()))LocalDate.now() else it}.toString();val a=Appointment(System.currentTimeMillis().toString(),doctor.id,doctor.name,doctor.clinic,selected,session,TokenGenerator.forBooking(doctor.id,selected,appointments().size));save(a);return a}
 override fun favouriteDoctorIds()=preferences.getStringSet(KEY_FAVOURITES,emptySet()).orEmpty().toSet()
 override fun toggleFavourite(doctorId:String):Set<String>{val ids=favouriteDoctorIds().toMutableSet();if(!ids.add(doctorId))ids.remove(doctorId);preferences.edit().putStringSet(KEY_FAVOURITES,ids).apply();return ids}
 override fun queue(appointmentId:String):QueueSnapshot?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;val initial=(a.token-5).coerceAtLeast(1);return QueueCalculator.snapshot(a,preferences.getInt(queueKey(appointmentId),initial))}
 override fun advanceQueue(appointmentId:String):QueueSnapshot?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;if(a.status==AppointmentStatus.MISSED)return queue(appointmentId);val next=((queue(appointmentId)?.currentToken?:1)+1).coerceAtMost(a.token);preferences.edit().putInt(queueKey(appointmentId),next).apply();save(a.copy(status=if(next>=a.token)AppointmentStatus.IN_CONSULTATION else AppointmentStatus.WAITING));return QueueCalculator.snapshot(a,next)}
 override fun markMissed(appointmentId:String):Appointment?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;return a.copy(status=AppointmentStatus.MISSED).also(::save)}
 override fun canReschedule(a:Appointment):Boolean{if(a.status!=AppointmentStatus.MISSED||a.rescheduleUsed)return false;return runCatching{ChronoUnit.DAYS.between(LocalDate.parse(a.date),LocalDate.now()) in 0..10}.getOrDefault(false)}
 override fun reschedule(appointmentId:String):Appointment?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;if(!canReschedule(a))return null;val date=LocalDate.now().plusDays(1).toString();val updated=a.copy(date=date,token=TokenGenerator.forBooking(a.doctorId,date,appointments().size),status=AppointmentStatus.BOOKED,rescheduleUsed=true);preferences.edit().remove(queueKey(appointmentId)).apply();save(updated);return updated}
 private fun save(a:Appointment){val values=appointments().filterNot{it.id==a.id}.map(AppointmentCodec::encode).toMutableSet().apply{add(AppointmentCodec.encode(a))};preferences.edit().putStringSet(KEY_APPOINTMENTS,values).apply()}
 private fun queueKey(id:String)="queue_current_"+id
 companion object{private const val KEY_APPOINTMENTS="patient_appointments";private const val KEY_FAVOURITES="patient_favourites"}
}
data class PatientUiState(val query:String="",val specialty:String?=null,val doctors:List<Doctor> = DummyData.doctors,val appointments:List<Appointment> = emptyList(),val active:Appointment?=null,val favouriteIds:Set<String> = emptySet(),val queue:QueueSnapshot?=null)
class PatientViewModel(private val repository:PatientRepository):ViewModel(){
 var uiState by mutableStateOf(loadState());private set
 fun search(value:String,specialty:String?=uiState.specialty){uiState=uiState.copy(query=value,specialty=specialty,doctors=repository.doctors(value,specialty))}
 fun toggleFavourite(id:String){uiState=uiState.copy(favouriteIds=repository.toggleFavourite(id))}
 fun book(id:String,date:String,session:Session):Appointment{val a=repository.book(id,date,session);refresh(a.id);return a}
 fun refreshQueue(id:String)=refresh(id)
 fun advanceQueue(id:String){repository.advanceQueue(id);refresh(id)}
 fun markMissed(id:String){repository.markMissed(id);refresh(id)}
 fun canReschedule(a:Appointment)=repository.canReschedule(a)
 fun reschedule(id:String):Boolean{val a=repository.reschedule(id)?:return false;refresh(a.id);return true}
 private fun refresh(id:String?=repository.activeAppointment()?.id){val active=repository.activeAppointment();uiState=uiState.copy(appointments=repository.appointments(),active=active,queue=(id?:active?.id)?.let(repository::queue))}
 private fun loadState():PatientUiState{val active=repository.activeAppointment();return PatientUiState(appointments=repository.appointments(),active=active,favouriteIds=repository.favouriteDoctorIds(),queue=active?.id?.let(repository::queue))}
}
class PatientViewModelFactory(private val repository:PatientRepository):ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=PatientViewModel(repository) as T}
