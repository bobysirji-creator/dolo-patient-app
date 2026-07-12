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

data class Appointment(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val clinic: String,
    val date: String,
    val session: Session,
    val token: Int,
    val status: String = "BOOKED",
)

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val message: String, val retryable: Boolean = true) : ApiResult<Nothing>
}

interface PatientApi {
    suspend fun doctors(query: String?): ApiResult<List<Doctor>>
    suspend fun appointments(): ApiResult<List<Appointment>>
    suspend fun book(doctorId: String, date: String, session: Session): ApiResult<Appointment>
}

interface PatientRepository {
    fun doctors(query: String = ""): List<Doctor>
    fun appointments(): List<Appointment>
    fun activeAppointment(): Appointment?
    fun book(doctorId: String, session: Session): Appointment
}

object TokenGenerator {
    fun forBooking(doctorId: String, date: String, existingCount: Int): Int =
        10 + ((doctorId.hashCode() + date.hashCode()).absoluteValue % 20) + existingCount
}

object AppointmentCodec {
    fun encode(value: Appointment): String = listOf(value.id,value.doctorId,value.doctorName,value.clinic,value.date,value.session.name,value.token,value.status).joinToString("|")
    fun decode(value: String): Appointment? {
        val p=value.split("|"); if(p.size!=8)return null
        return runCatching { Appointment(p[0],p[1],p[2],p[3],p[4],Session.valueOf(p[5]),p[6].toInt(),p[7]) }.getOrNull()
    }
}

class LocalPatientRepository(private val preferences: SharedPreferences) : PatientRepository {
    override fun doctors(query: String): List<Doctor> {
        val q=query.trim()
        return if(q.isBlank()) DummyData.doctors else DummyData.doctors.filter {
            it.name.contains(q,true)||it.specialty.contains(q,true)||it.clinic.contains(q,true)
        }
    }
    override fun appointments(): List<Appointment> = preferences.getStringSet(KEY_APPOINTMENTS, emptySet()).orEmpty().mapNotNull(AppointmentCodec::decode).sortedByDescending { it.id }
    override fun activeAppointment(): Appointment? = appointments().firstOrNull { it.status=="BOOKED" }
    override fun book(doctorId: String, session: Session): Appointment {
        val doctor=DummyData.doctors.first { it.id==doctorId }; val date=LocalDate.now().toString()
        val appointment=Appointment(System.currentTimeMillis().toString(),doctor.id,doctor.name,doctor.clinic,date,session,TokenGenerator.forBooking(doctor.id,date,appointments().size))
        val updated=appointments().map(AppointmentCodec::encode).toMutableSet().apply { add(AppointmentCodec.encode(appointment)) }
        preferences.edit().putStringSet(KEY_APPOINTMENTS,updated).apply()
        return appointment
    }
    companion object { private const val KEY_APPOINTMENTS="patient_appointments" }
}

data class PatientUiState(val query:String="",val doctors:List<Doctor> = DummyData.doctors,val appointments:List<Appointment> = emptyList(),val active:Appointment?=null)

class PatientViewModel(private val repository: PatientRepository):ViewModel(){
    var uiState by mutableStateOf(PatientUiState(appointments=repository.appointments(),active=repository.activeAppointment()))
        private set
    fun search(value:String){uiState=uiState.copy(query=value,doctors=repository.doctors(value))}
    fun book(doctorId:String,session:Session):Appointment{val a=repository.book(doctorId,session);uiState=uiState.copy(appointments=repository.appointments(),active=a);return a}
}
class PatientViewModelFactory(private val repository:PatientRepository):ViewModelProvider.Factory{
 @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=PatientViewModel(repository) as T
}
