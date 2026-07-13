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
data class Appointment(val id:String,val doctorId:String,val doctorName:String,val clinic:String,val date:String,val session:Session,val token:Int,val status:String=AppointmentStatus.BOOKED,val rescheduleUsed:Boolean=false,val patientName:String="Rahul Sharma")
data class QueueSnapshot(val appointmentId:String,val patientToken:Int,val currentToken:Int,val patientsAhead:Int,val estimatedMinutes:Int,val status:String,val refreshedAt:Long)
object SyncStatus{const val FRESH="FRESH";const val STALE="STALE";const val OFFLINE="OFFLINE";const val SYNCING="SYNCING"}
data class PatientProfile(val name:String="Rahul Sharma",val phone:String="9876543210",val city:String="New Delhi")
data class FamilyMember(val id:String,val name:String,val relation:String,val age:Int)
data class DoctorReview(val id:String,val doctorId:String,val appointmentId:String,val rating:Int,val comment:String,val createdAt:Long)
data class AppNotification(val id:String,val title:String,val message:String,val createdAt:Long,val isRead:Boolean=false)
interface QueueApi{ suspend fun queue(appointmentId:String):ApiResult<QueueSnapshot> }

sealed interface ApiResult<out T>{data class Success<T>(val value:T):ApiResult<T>;data class Failure(val message:String,val retryable:Boolean=true):ApiResult<Nothing>}
interface PatientApi{suspend fun doctors(query:String?):ApiResult<List<Doctor>>;suspend fun appointments():ApiResult<List<Appointment>>;suspend fun book(doctorId:String,date:String,session:Session):ApiResult<Appointment>}
interface PatientRepository{
 fun doctors(query:String="",specialty:String?=null):List<Doctor>;fun appointments():List<Appointment>;fun activeAppointment():Appointment?
 fun book(doctorId:String,date:String,session:Session,patientName:String):Appointment;fun favouriteDoctorIds():Set<String>;fun toggleFavourite(doctorId:String):Set<String>
 fun queue(appointmentId:String):QueueSnapshot?;fun advanceQueue(appointmentId:String):QueueSnapshot?;fun markMissed(appointmentId:String):Appointment?
 fun canReschedule(appointment:Appointment):Boolean;fun reschedule(appointmentId:String):Appointment?
 fun refreshQueue(appointmentId:String,online:Boolean=true):QueueSnapshot?;fun completeAppointment(appointmentId:String):Appointment?
 fun profile():PatientProfile;fun updateProfile(profile:PatientProfile):PatientProfile
 fun familyMembers():List<FamilyMember>;fun addFamilyMember(name:String,relation:String,age:Int):FamilyMember
 fun reviews():List<DoctorReview>;fun canReview(appointment:Appointment):Boolean;fun addReview(appointmentId:String,rating:Int,comment:String):DoctorReview?
 fun notifications():List<AppNotification>;fun markNotificationsRead()

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
 fun encode(v:Appointment)=listOf(v.id,v.doctorId,v.doctorName,v.clinic,v.date,v.session.name,v.token,v.status,v.rescheduleUsed,v.patientName).joinToString("|")
 fun decode(value:String):Appointment?{val p=value.split("|");if(p.size !in 8..10)return null;return runCatching{Appointment(p[0],p[1],p[2],p[3],p[4],Session.valueOf(p[5]),p[6].toInt(),p[7],p.getOrNull(8)?.toBooleanStrictOrNull()?:false,p.getOrNull(9)?:"Rahul Sharma")}.getOrNull()}
}
class LocalPatientRepository(private val preferences:SharedPreferences):PatientRepository{
 override fun doctors(query:String,specialty:String?):List<Doctor>{val q=query.trim();return DummyData.doctors.filter{(specialty.isNullOrBlank()||it.specialty==specialty)&&(q.isBlank()||it.name.contains(q,true)||it.specialty.contains(q,true)||it.clinic.contains(q,true))}}
 override fun appointments()=preferences.getStringSet(KEY_APPOINTMENTS,emptySet()).orEmpty().mapNotNull(AppointmentCodec::decode).sortedByDescending{it.id}
 override fun activeAppointment()=appointments().firstOrNull{it.status in setOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION)}
 override fun book(doctorId:String,date:String,session:Session,patientName:String):Appointment{val doctor=DummyData.doctors.first{it.id==doctorId};val selected=runCatching{LocalDate.parse(date)}.getOrDefault(LocalDate.now()).let{if(it.isBefore(LocalDate.now()))LocalDate.now() else it}.toString();val a=Appointment(System.currentTimeMillis().toString(),doctor.id,doctor.name,doctor.clinic,selected,session,TokenGenerator.forBooking(doctor.id,selected,appointments().size),patientName=patientName);save(a);notify("Appointment booked",doctor.name+" on "+selected+" for "+patientName);return a}
 override fun favouriteDoctorIds()=preferences.getStringSet(KEY_FAVOURITES,emptySet()).orEmpty().toSet()
 override fun toggleFavourite(doctorId:String):Set<String>{val ids=favouriteDoctorIds().toMutableSet();if(!ids.add(doctorId))ids.remove(doctorId);preferences.edit().putStringSet(KEY_FAVOURITES,ids).apply();return ids}
 override fun queue(appointmentId:String):QueueSnapshot?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;val initial=(a.token-5).coerceAtLeast(1);val refreshed=preferences.getLong(refreshKey(appointmentId),System.currentTimeMillis());return QueueCalculator.snapshot(a,preferences.getInt(queueKey(appointmentId),initial),refreshed)}
 override fun advanceQueue(appointmentId:String):QueueSnapshot?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;if(a.status==AppointmentStatus.MISSED)return queue(appointmentId);val next=((queue(appointmentId)?.currentToken?:1)+1).coerceAtMost(a.token);preferences.edit().putInt(queueKey(appointmentId),next).putLong(refreshKey(appointmentId),System.currentTimeMillis()).apply();val updated=a.copy(status=if(next>=a.token)AppointmentStatus.IN_CONSULTATION else AppointmentStatus.WAITING);save(updated);val snapshot=QueueCalculator.snapshot(updated,next);if(snapshot.patientsAhead<=2)notify("Your turn is approaching",snapshot.patientsAhead.toString()+" patients ahead");return snapshot}
 override fun markMissed(appointmentId:String):Appointment?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;return a.copy(status=AppointmentStatus.MISSED).also(::save)}
 override fun canReschedule(a:Appointment):Boolean{if(a.status!=AppointmentStatus.MISSED||a.rescheduleUsed)return false;return runCatching{ChronoUnit.DAYS.between(LocalDate.parse(a.date),LocalDate.now()) in 0..10}.getOrDefault(false)}
 override fun reschedule(appointmentId:String):Appointment?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;if(!canReschedule(a))return null;val date=LocalDate.now().plusDays(1).toString();val updated=a.copy(date=date,token=TokenGenerator.forBooking(a.doctorId,date,appointments().size),status=AppointmentStatus.BOOKED,rescheduleUsed=true);preferences.edit().remove(queueKey(appointmentId)).apply();save(updated);notify("Appointment rescheduled",a.doctorName+" on "+date);return updated}
 override fun refreshQueue(appointmentId:String,online:Boolean):QueueSnapshot?{if(!online)return queue(appointmentId);preferences.edit().putLong(refreshKey(appointmentId),System.currentTimeMillis()).apply();return queue(appointmentId)}
 override fun completeAppointment(appointmentId:String):Appointment?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;val updated=a.copy(status=AppointmentStatus.COMPLETED);save(updated);notify("Consultation completed","You can now review "+a.doctorName);return updated}
 override fun profile():PatientProfile=PatientProfile(preferences.getString(KEY_PROFILE_NAME,"Rahul Sharma")!!,preferences.getString(KEY_PROFILE_PHONE,"9876543210")!!,preferences.getString(KEY_PROFILE_CITY,"New Delhi")!!)
 override fun updateProfile(profile:PatientProfile):PatientProfile{preferences.edit().putString(KEY_PROFILE_NAME,safe(profile.name)).putString(KEY_PROFILE_PHONE,safe(profile.phone)).putString(KEY_PROFILE_CITY,safe(profile.city)).apply();return profile}
 override fun familyMembers():List<FamilyMember> =preferences.getStringSet(KEY_FAMILY,emptySet()).orEmpty().mapNotNull{v->val p=v.split("|");if(p.size!=4)null else p[3].toIntOrNull()?.let{FamilyMember(p[0],p[1],p[2],it)}}.sortedBy{it.name}
 override fun addFamilyMember(name:String,relation:String,age:Int):FamilyMember{val m=FamilyMember(System.currentTimeMillis().toString(),safe(name),safe(relation),age.coerceIn(0,120));val values=preferences.getStringSet(KEY_FAMILY,emptySet()).orEmpty().toMutableSet().apply{add(listOf(m.id,m.name,m.relation,m.age).joinToString("|"))};preferences.edit().putStringSet(KEY_FAMILY,values).apply();return m}
 override fun reviews():List<DoctorReview> =preferences.getStringSet(KEY_REVIEWS,emptySet()).orEmpty().mapNotNull{v->val p=v.split("|");if(p.size!=6)null else runCatching{DoctorReview(p[0],p[1],p[2],p[3].toInt(),p[4],p[5].toLong())}.getOrNull()}.sortedByDescending{it.createdAt}
 override fun canReview(a:Appointment)=a.status==AppointmentStatus.COMPLETED&&reviews().none{it.appointmentId==a.id}
 override fun addReview(appointmentId:String,rating:Int,comment:String):DoctorReview?{val a=appointments().firstOrNull{it.id==appointmentId}?:return null;if(!canReview(a))return null;val r=DoctorReview(System.currentTimeMillis().toString(),a.doctorId,a.id,rating.coerceIn(1,5),safe(comment),System.currentTimeMillis());val values=preferences.getStringSet(KEY_REVIEWS,emptySet()).orEmpty().toMutableSet().apply{add(listOf(r.id,r.doctorId,r.appointmentId,r.rating,r.comment,r.createdAt).joinToString("|"))};preferences.edit().putStringSet(KEY_REVIEWS,values).apply();notify("Review submitted","Thank you for rating "+a.doctorName);return r}
 override fun notifications():List<AppNotification> =preferences.getStringSet(KEY_NOTIFICATIONS,emptySet()).orEmpty().mapNotNull{v->val p=v.split("|");if(p.size!=5)null else runCatching{AppNotification(p[0],p[1],p[2],p[3].toLong(),p[4].toBoolean())}.getOrNull()}.sortedByDescending{it.createdAt}
 override fun markNotificationsRead(){val values=notifications().map{n->listOf(n.id,n.title,n.message,n.createdAt,true).joinToString("|")}.toSet();preferences.edit().putStringSet(KEY_NOTIFICATIONS,values).apply()}
 private fun notify(title:String,message:String){val n=AppNotification(System.currentTimeMillis().toString(),safe(title),safe(message),System.currentTimeMillis());val values=preferences.getStringSet(KEY_NOTIFICATIONS,emptySet()).orEmpty().toMutableSet().apply{add(listOf(n.id,n.title,n.message,n.createdAt,n.isRead).joinToString("|"))};preferences.edit().putStringSet(KEY_NOTIFICATIONS,values).apply()}
 private fun safe(value:String)=value.trim().replace("|","/")
 private fun refreshKey(id:String)="queue_refresh_"+id
 private fun save(a:Appointment){val values=appointments().filterNot{it.id==a.id}.map(AppointmentCodec::encode).toMutableSet().apply{add(AppointmentCodec.encode(a))};preferences.edit().putStringSet(KEY_APPOINTMENTS,values).apply()}
 private fun queueKey(id:String)="queue_current_"+id
 companion object{private const val KEY_APPOINTMENTS="patient_appointments";private const val KEY_FAVOURITES="patient_favourites";private const val KEY_PROFILE_NAME="profile_name";private const val KEY_PROFILE_PHONE="profile_phone";private const val KEY_PROFILE_CITY="profile_city";private const val KEY_FAMILY="family_members";private const val KEY_REVIEWS="doctor_reviews";private const val KEY_NOTIFICATIONS="app_notifications"}
}
data class PatientUiState(
    val query: String = "",
    val specialty: String? = null,
    val doctors: List<Doctor> = DummyData.doctors,
    val appointments: List<Appointment> = emptyList(),
    val active: Appointment? = null,
    val favouriteIds: Set<String> = emptySet(),
    val queue: QueueSnapshot? = null,
    val profile: PatientProfile = PatientProfile(),
    val family: List<FamilyMember> = emptyList(),
    val reviews: List<DoctorReview> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val syncStatus: String = SyncStatus.FRESH
)

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {
    var uiState by mutableStateOf(loadState())
        private set

    fun search(value: String, specialty: String? = uiState.specialty) {
        uiState = uiState.copy(
            query = value,
            specialty = specialty,
            doctors = repository.doctors(value, specialty)
        )
    }

    fun toggleFavourite(id: String) {
        uiState = uiState.copy(favouriteIds = repository.toggleFavourite(id))
    }

    fun book(id: String, date: String, session: Session, patientName: String): Appointment {
        val appointment = repository.book(id, date, session, patientName)
        refresh(appointment.id)
        return appointment
    }

    fun refreshQueue(id: String, online: Boolean = true) {
        uiState = uiState.copy(syncStatus = if (online) SyncStatus.SYNCING else SyncStatus.OFFLINE)
        val snapshot = repository.refreshQueue(id, online)
        refresh(id, snapshot, if (online) SyncStatus.FRESH else SyncStatus.OFFLINE)
    }

    fun advanceQueue(id: String) {
        repository.advanceQueue(id)
        refresh(id)
    }

    fun markMissed(id: String) {
        repository.markMissed(id)
        refresh(id)
    }

    fun completeAppointment(id: String) {
        repository.completeAppointment(id)
        refresh(id)
    }

    fun canReschedule(appointment: Appointment): Boolean = repository.canReschedule(appointment)

    fun reschedule(id: String): Boolean {
        val appointment = repository.reschedule(id) ?: return false
        refresh(appointment.id)
        return true
    }

    fun updateProfile(name: String, phone: String, city: String) {
        val profile = repository.updateProfile(PatientProfile(name, phone, city))
        uiState = uiState.copy(profile = profile)
    }

    fun addFamilyMember(name: String, relation: String, age: Int) {
        repository.addFamilyMember(name, relation, age)
        uiState = uiState.copy(family = repository.familyMembers())
    }

    fun canReview(appointment: Appointment): Boolean = repository.canReview(appointment)

    fun addReview(appointmentId: String, rating: Int, comment: String) {
        repository.addReview(appointmentId, rating, comment)
        uiState = uiState.copy(
            reviews = repository.reviews(),
            notifications = repository.notifications()
        )
    }

    fun markNotificationsRead() {
        repository.markNotificationsRead()
        uiState = uiState.copy(notifications = repository.notifications())
    }

    private fun refresh(
        id: String? = repository.activeAppointment()?.id,
        queueOverride: QueueSnapshot? = null,
        syncStatus: String = SyncStatus.FRESH
    ) {
        val active = repository.activeAppointment()
        val queueId = id ?: active?.id
        uiState = uiState.copy(
            appointments = repository.appointments(),
            active = active,
            queue = queueOverride ?: queueId?.let(repository::queue),
            profile = repository.profile(),
            family = repository.familyMembers(),
            reviews = repository.reviews(),
            notifications = repository.notifications(),
            syncStatus = syncStatus
        )
    }

    private fun loadState(): PatientUiState {
        val active = repository.activeAppointment()
        return PatientUiState(
            appointments = repository.appointments(),
            active = active,
            favouriteIds = repository.favouriteDoctorIds(),
            queue = active?.id?.let(repository::queue),
            profile = repository.profile(),
            family = repository.familyMembers(),
            reviews = repository.reviews(),
            notifications = repository.notifications()
        )
    }
}

class PatientViewModelFactory(
    private val repository: PatientRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PatientViewModel(repository) as T
}
