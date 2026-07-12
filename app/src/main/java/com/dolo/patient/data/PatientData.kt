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
import kotlin.math.absoluteValue

data class Appointment(val id:String,val doctorId:String,val doctorName:String,val clinic:String,val date:String,val session:Session,val token:Int,val status:String="BOOKED")
sealed interface ApiResult<out T>{data class Success<T>(val value:T):ApiResult<T>;data class Failure(val message:String,val retryable:Boolean=true):ApiResult<Nothing>}
interface PatientApi{suspend fun doctors(query:String?):ApiResult<List<Doctor>>;suspend fun appointments():ApiResult<List<Appointment>>;suspend fun book(doctorId:String,date:String,session:Session):ApiResult<Appointment>}
interface PatientRepository{fun doctors(query:String="",specialty:String?=null):List<Doctor>;fun appointments():List<Appointment>;fun activeAppointment():Appointment?;fun book(doctorId:String,session:Session):Appointment;fun favouriteDoctorIds():Set<String>;fun toggleFavourite(doctorId:String):Set<String>}
object TokenGenerator{fun forBooking(doctorId:String,date:String,existingCount:Int)=10+((doctorId.hashCode()+date.hashCode()).absoluteValue%20)+existingCount}
object AppointmentCodec{fun encode(v:Appointment)=listOf(v.id,v.doctorId,v.doctorName,v.clinic,v.date,v.session.name,v.token,v.status).joinToString("|");fun decode(v:String):Appointment?{val p=v.split("|");if(p.size!=8)return null;return runCatching{Appointment(p[0],p[1],p[2],p[3],p[4],Session.valueOf(p[5]),p[6].toInt(),p[7])}.getOrNull()}}
class LocalPatientRepository(private val preferences:SharedPreferences):PatientRepository{
 override fun doctors(query:String,specialty:String?):List<Doctor>{val q=query.trim();return DummyData.doctors.filter{(specialty.isNullOrBlank()||it.specialty==specialty)&&(q.isBlank()||it.name.contains(q,true)||it.specialty.contains(q,true)||it.clinic.contains(q,true))}}
 override fun appointments()=preferences.getStringSet(KEY_APPOINTMENTS,emptySet()).orEmpty().mapNotNull(AppointmentCodec::decode).sortedByDescending{it.id}
 override fun activeAppointment()=appointments().firstOrNull{it.status=="BOOKED"}
 override fun book(doctorId:String,session:Session):Appointment{val d=DummyData.doctors.first{it.id==doctorId};val date=LocalDate.now().toString();val a=Appointment(System.currentTimeMillis().toString(),d.id,d.name,d.clinic,date,session,TokenGenerator.forBooking(d.id,date,appointments().size));val saved=appointments().map(AppointmentCodec::encode).toMutableSet().apply{add(AppointmentCodec.encode(a))};preferences.edit().putStringSet(KEY_APPOINTMENTS,saved).apply();return a}
 override fun favouriteDoctorIds()=preferences.getStringSet(KEY_FAVOURITES,emptySet()).orEmpty().toSet()
 override fun toggleFavourite(doctorId:String):Set<String>{val ids=favouriteDoctorIds().toMutableSet();if(!ids.add(doctorId))ids.remove(doctorId);preferences.edit().putStringSet(KEY_FAVOURITES,ids).apply();return ids}
 companion object{private const val KEY_APPOINTMENTS="patient_appointments";private const val KEY_FAVOURITES="patient_favourites"}
}
data class PatientUiState(val query:String="",val specialty:String?=null,val doctors:List<Doctor> = DummyData.doctors,val appointments:List<Appointment> = emptyList(),val active:Appointment?=null,val favouriteIds:Set<String> = emptySet())
class PatientViewModel(private val repository:PatientRepository):ViewModel(){var uiState by mutableStateOf(PatientUiState(appointments=repository.appointments(),active=repository.activeAppointment(),favouriteIds=repository.favouriteDoctorIds()));private set;fun search(value:String,specialty:String?=uiState.specialty){uiState=uiState.copy(query=value,specialty=specialty,doctors=repository.doctors(value,specialty))};fun toggleFavourite(id:String){uiState=uiState.copy(favouriteIds=repository.toggleFavourite(id))};fun book(id:String,session:Session):Appointment{val a=repository.book(id,session);uiState=uiState.copy(appointments=repository.appointments(),active=a);return a}}
class PatientViewModelFactory(private val repository:PatientRepository):ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=PatientViewModel(repository) as T}
