package com.dolo.patient.data

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.patient.auth.PrototypeSessionManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

data class HostedProfile(val id:String,val name:String)
data class HostedClinic(val id:String,val name:String,val city:String,val doctorName:String,val specialty:String,val feeMinor:Int)
data class HostedSession(val id:String,val date:String,val name:String,val startsAt:String,val endsAt:String,val available:Int,val enabled:Boolean)
data class HostedAppointment(val id:String,val sessionId:String,val doctorName:String,val clinicName:String,val patientName:String,val date:String,val session:String,val token:Int,val status:String)
data class HostedLiveQueue(val appointmentId:String,val token:Int,val currentToken:Int?,val patientsAhead:Int?,val estimatedMinutes:Int?,val status:String,val countdownState:String)
data class HostedBootstrap(val profile:HostedProfile,val clinic:HostedClinic,val sessions:List<HostedSession>)
data class HostedSyncSnapshot(val bootstrap:HostedBootstrap,val appointments:List<HostedAppointment>,val live:List<HostedLiveQueue>)

sealed interface HostedResult<out T>{data class Success<T>(val value:T):HostedResult<T>;data class Failure(val message:String):HostedResult<Nothing>}
interface HostedPatientSyncApi{fun refresh():HostedResult<HostedSyncSnapshot>;fun book(sessionId:String,profileId:String):HostedResult<HostedSyncSnapshot>}

class HttpHostedPatientSyncApi(baseUrl:String,private val sessionManager:PrototypeSessionManager,private val preferences:SharedPreferences):HostedPatientSyncApi{
 private val base=baseUrl.trim().trimEnd('/')
 init{require(URL(base).protocol.equals("https",true)){"Hosted Patient synchronization requires HTTPS."}}
 override fun refresh():HostedResult<HostedSyncSnapshot> = guarded { load() }
 override fun book(sessionId:String,profileId:String):HostedResult<HostedSyncSnapshot> = guarded {
   val keyName="hosted_booking_key_"+sessionId
   val idempotency=preferences.getString(keyName,null)?:("android16c-"+UUID.randomUUID()).also{preferences.edit().putString(keyName,it).apply()}
   request("POST","/api/v1/appointments",JSONObject().put("clinicSessionId",sessionId).put("patientProfileId",profileId).toString(),mapOf("Idempotency-Key" to idempotency))
   load()
 }
 private fun load():HostedSyncSnapshot{
   val bootstrap=parseBootstrap(request("POST","/api/v1/patient/sync/bootstrap","{}"))
   val appointments=parseAppointments(request("GET","/api/v1/appointments"))
   val live=parseLive(request("GET","/api/v1/patient/live-appointments"))
   return HostedSyncSnapshot(bootstrap,appointments,live)
 }
 private fun <T> guarded(block:()->T):HostedResult<T> = runCatching(block).fold({HostedResult.Success(it)},{HostedResult.Failure(when(it){is java.net.UnknownHostException->"Offline. Server data was not changed.";is java.net.SocketTimeoutException->"Hosted prototype is waking up. Retry shortly.";else->it.message?.take(180)?:"Hosted synchronization failed."})})
 private fun request(method:String,path:String,body:String?=null,headers:Map<String,String> = emptyMap()):String{
   val token=sessionManager.accessToken()?:error("Hosted session expired. Log out and sign in again while online.")
   val c=(URL(base+path).openConnection() as HttpURLConnection).apply{requestMethod=method;connectTimeout=15_000;readTimeout=25_000;setRequestProperty("Accept","application/json");setRequestProperty("Authorization","Bearer $token");setRequestProperty("User-Agent","DO-LO-Patient-Android/Stage16C");headers.forEach { (key, value) -> setRequestProperty(key, value) };if(body!=null){doOutput=true;setRequestProperty("Content-Type","application/json")};useCaches=false}
   return try{if(body!=null)c.outputStream.use{it.write(body.toByteArray())};val status=c.responseCode;val text=(if(status in 200..299)c.inputStream else c.errorStream)?.bufferedReader()?.use{it.readText().take(524_288)}.orEmpty();if(status !in 200..299)error(runCatching{JSONObject(text).getJSONObject("error").getString("message")}.getOrDefault("Hosted API returned HTTP $status"));text}finally{c.disconnect()}
 }
 private fun parseBootstrap(json:String):HostedBootstrap{val r=JSONObject(json);require(r.optBoolean("authoritative"));val p=r.getJSONObject("profile");val c=r.getJSONObject("clinic");val d=c.getJSONObject("doctor");val a=r.getJSONArray("sessions");val sessions=buildList{for(i in 0 until a.length()){val s=a.getJSONObject(i);add(HostedSession(s.getString("id"),s.getString("serviceDate"),s.getString("name"),s.getString("startsAt"),s.getString("endsAt"),s.optInt("availableTokens"),s.optBoolean("bookingEnabled")))}};return HostedBootstrap(HostedProfile(p.getString("id"),p.getString("displayName")),HostedClinic(c.getString("id"),c.getString("name"),c.optString("city"),d.getString("name"),d.optString("specialty"),c.optInt("consultationFeeMinor")),sessions)}
 private fun parseAppointments(json:String):List<HostedAppointment>{val a=JSONObject(json).getJSONArray("appointments");return buildList{for(i in 0 until a.length()){val x=a.getJSONObject(i);add(HostedAppointment(x.getString("id"),x.getString("clinicSessionId"),"Dr. Ananya Mehta",x.getString("clinicName"),x.getString("patientName"),x.getString("serviceDate"),x.getString("session"),x.getInt("tokenNumber"),x.getString("status")))}}}
 private fun parseLive(json:String):List<HostedLiveQueue>{val a=JSONObject(json).getJSONArray("appointments");return buildList{for(i in 0 until a.length()){val x=a.getJSONObject(i);add(HostedLiveQueue(x.getString("appointmentId"),x.getInt("tokenNumber"),x.optIntOrNull("currentToken"),x.optIntOrNull("patientsAhead"),x.optIntOrNull("estimatedWaitMinutes"),x.getString("appointmentStatus"),x.optString("countdownState")))}}}
 private fun JSONObject.optIntOrNull(key:String):Int?=if(isNull(key)||!has(key))null else getInt(key)
}

data class HostedSyncUiState(val loading:Boolean=false,val snapshot:HostedSyncSnapshot?=null,val message:String="Connect to the seeded hosted identity to begin.",val error:Boolean=false)
class HostedPatientSyncViewModel(private val api:HostedPatientSyncApi):ViewModel(){var uiState by mutableStateOf(HostedSyncUiState());private set;private val executor=Executors.newSingleThreadExecutor();private val main=Handler(Looper.getMainLooper());fun refresh(){execute{api.refresh()}};fun book(sessionId:String,profileId:String){execute{api.book(sessionId,profileId)}};private fun execute(call:()->HostedResult<HostedSyncSnapshot>){if(uiState.loading)return;uiState=uiState.copy(loading=true,message="Synchronizing authoritative prototype data...",error=false);executor.execute{val r=call();main.post{uiState=when(r){is HostedResult.Success->HostedSyncUiState(snapshot=r.value,message="Server data is authoritative for this seeded dummy flow.");is HostedResult.Failure->uiState.copy(loading=false,message=r.message,error=true)}}}};override fun onCleared(){executor.shutdownNow();super.onCleared()}}
class HostedPatientSyncViewModelFactory(private val api:HostedPatientSyncApi):ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST")override fun <T:ViewModel> create(modelClass:Class<T>):T=HostedPatientSyncViewModel(api) as T}