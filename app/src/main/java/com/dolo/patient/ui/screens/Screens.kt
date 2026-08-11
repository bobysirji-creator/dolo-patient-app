package com.dolo.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.platform.PlatformConnectionState
import com.dolo.patient.platform.PlatformConnectionStatus
import com.dolo.patient.platform.PlatformDiscovery
import com.dolo.patient.platform.PlatformClinic
import com.dolo.patient.auth.AuthStep
import com.dolo.patient.auth.AuthViewModel
import com.dolo.patient.data.*
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.integrations.*
import com.dolo.patient.ui.components.*
import com.dolo.patient.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable private fun pageModifier()=Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)

@Composable
fun SplashScreen(onContinue:()->Unit){
 Box(pageModifier().safeDrawingPadding().padding(24.dp),contentAlignment=Alignment.Center){
  Column(Modifier.widthIn(max=420.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){
   BrandLogo()
   Surface(shape=RoundedCornerShape(32.dp),color=MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.size(164.dp)){Icon(Icons.Outlined.HealthAndSafety,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(38.dp))}
   Text("Healthcare without the waiting room",style=MaterialTheme.typography.headlineLarge,textAlign=TextAlign.Center)
   Text("Book a walk-in visit, follow the live queue, and arrive closer to your turn.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
   Spacer(Modifier.height(6.dp))
   PrimaryButton("Continue",onContinue)
  }
 }
}
@Composable
fun OtpVerificationScreen(auth:AuthViewModel,onLogin:()->Unit){
 val state=auth.uiState
 LaunchedEffect(state.step){if(state.step==AuthStep.AUTHENTICATED)onLogin()}
 Box(pageModifier().safeDrawingPadding().imePadding().padding(horizontal=22.dp),contentAlignment=Alignment.Center){
  LazyColumn(Modifier.widthIn(max=440.dp),contentPadding=PaddingValues(vertical=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
   item{BrandLogo();Spacer(Modifier.height(24.dp));Text("Enter verification code",style=MaterialTheme.typography.headlineLarge);Text("Use the six-digit demo code to continue.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=6.dp))}
   item{DoloCard{OutlinedTextField(state.otp,auth::updateOtp,Modifier.fillMaxWidth(),label={Text("Verification code")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true,shape=RoundedCornerShape(16.dp));Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Info,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(9.dp));Text("Demo code: 123456",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)}};PrimaryButton(if(state.isLoading)"Signing in..." else "Verify and sign in",auth::verifyOtp,state.otp.length==6&&!state.isLoading,loading=state.isLoading);TextButton(auth::editPhone,modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Use a different number")}}}
   state.error?.let{item{Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.errorContainer){Text(it,Modifier.padding(14.dp),color=MaterialTheme.colorScheme.onErrorContainer)}}}
  }
 }
}
@Composable
fun HomeScreen(onCategories:()->Unit,onDoctor:(String)->Unit,onHistory:()->Unit,onFavourites:()->Unit,onQueue:(String)->Unit,onProfile:()->Unit,onNotifications:()->Unit,onSupport:()->Unit,onLogout:()->Unit,state:PatientUiState,onSearch:(String)->Unit,onRefreshQueues:()->Unit,authStatus:String,hostedState:HostedSyncUiState?=null,onRefreshHosted:()->Unit={},onHostedSync:()->Unit={}){
 var query by remember{mutableStateOf("")}
 var menuOpen by remember{mutableStateOf(false)}
 var nowMillis by remember{mutableStateOf(System.currentTimeMillis())}
 val activeAppointments=state.appointments.filter{it.status in listOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION)}
 val hostedAppointments=hostedState?.snapshot?.let{HostedHomePresentation.activeAppointments(it)}.orEmpty()
 val hostedUpdates=hostedState?.snapshot?.let{HostedHomePresentation.homeCommunications(it)}.orEmpty()
 val favourites=DummyData.doctors.filter{it.id in state.favouriteIds}
 LaunchedEffect(Unit){while(true){nowMillis=System.currentTimeMillis();delay(1000)}}
 LaunchedEffect(Unit){while(true){delay(ReleaseReadiness.QUEUE_REFRESH_INTERVAL_MILLIS);onRefreshQueues()}}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={DoloBottomBar(PatientBottomDestination.HOME,{},{onHistory()},{onCategories()})}){padding->
  LazyColumn(Modifier.padding(padding).fillMaxSize(),contentPadding=PaddingValues(horizontal=18.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){BrandLogo();Spacer(Modifier.weight(1f));IconButton(onNotifications){BadgedBox({if(state.notifications.any{!it.isRead}||hostedState?.snapshot?.notifications?.any{!it.read}==true)Badge(containerColor=DoloCoral)}){Icon(Icons.Outlined.Notifications,"Notifications",tint=MaterialTheme.colorScheme.onSurface)}};Box{IconButton({menuOpen=true}){Icon(Icons.Outlined.AccountCircle,"Account menu",tint=MaterialTheme.colorScheme.onSurface)};DropdownMenu(menuOpen,{menuOpen=false}){DropdownMenuItem({Text("Profile & family")},{menuOpen=false;onProfile()},leadingIcon={Icon(Icons.Outlined.Person,null)});DropdownMenuItem({Text("Favourites")},{menuOpen=false;onFavourites()},leadingIcon={Icon(Icons.Outlined.FavoriteBorder,null)});DropdownMenuItem({Text("Help & support")},{menuOpen=false;onSupport()},leadingIcon={Icon(Icons.Outlined.HelpOutline,null)});HorizontalDivider();DropdownMenuItem({Text("Sign out")},{menuOpen=false;onLogout()},leadingIcon={Icon(Icons.Outlined.Logout,null)})}}}}
   item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Bottom){Column(Modifier.weight(1f)){Text("Hello, ${state.profile.name}",style=MaterialTheme.typography.headlineMedium);Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.LocationOn,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(16.dp));Text(state.profile.city.ifBlank{"Location not set"},style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}};if(authStatus=="Hosted prototype")StatusBadge("Connected",DoloSuccess)}}
   item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Doctor, specialty or clinic")},leadingIcon={Icon(Icons.Outlined.Search,null,tint=MaterialTheme.colorScheme.primary)},trailingIcon={IconButton({onSearch(query)}){Icon(Icons.Outlined.ArrowForward,"Search")}},singleLine=true,shape=RoundedCornerShape(18.dp),colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=MaterialTheme.colorScheme.surface,focusedContainerColor=MaterialTheme.colorScheme.surface,unfocusedBorderColor=MaterialTheme.colorScheme.outlineVariant))}
   item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){QuickAction("Appointments",Icons.Outlined.CalendarMonth,onHistory,Modifier.weight(1f));QuickAction("Saved doctors",Icons.Outlined.FavoriteBorder,onFavourites,Modifier.weight(1f));QuickAction("Get help",Icons.Outlined.SupportAgent,onSupport,Modifier.weight(1f))}}
   if(hostedUpdates.isNotEmpty()){item{SectionHeader("Updates")};items(hostedUpdates,key={"home-update-${it.id}"}){HostedHomeUpdateCard(it,onHostedSync)}}
   item{SectionHeader("Your appointments","View all",onHistory)}
   if(hostedState?.error==true)item{DoloCard(containerColor=MaterialTheme.colorScheme.errorContainer){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.CloudOff,null,tint=MaterialTheme.colorScheme.error);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Could not refresh appointments",fontWeight=FontWeight.Bold);Text(hostedState.message,style=MaterialTheme.typography.bodySmall)}};TextButton(onRefreshHosted,enabled=!hostedState.loading){Text("Try again")}}}
   items(hostedAppointments,key={"hosted-home-${it.id}"}){appointment->HostedHomeAppointmentCard(appointment,hostedState?.snapshot?.let{HostedHomePresentation.liveQueue(it,appointment.id)},onHostedSync)}
   items(activeAppointments,key={it.id}){appointment->HomeAppointmentQueueCard(appointment,state.queues[appointment.id],nowMillis){onQueue(appointment.id)}}
   if(hostedAppointments.isEmpty()&&activeAppointments.isEmpty())item{DoloCard(containerColor=MaterialTheme.colorScheme.surfaceVariant){Row(verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(15.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.size(48.dp)){Icon(Icons.Outlined.EventAvailable,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(11.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("No upcoming appointment",style=MaterialTheme.typography.titleMedium);Text("Book a doctor and your token will appear here.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}};PrimaryButton("Book an appointment",onCategories)}}
   item{SectionHeader("Find care")}
   item{DoloCard(containerColor=DoloNavy){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Find the right doctor",style=MaterialTheme.typography.titleLarge,color=Color.White);Text("Browse specialties and compare available clinics.",style=MaterialTheme.typography.bodyMedium,color=Color.White.copy(alpha=.78f))};Spacer(Modifier.width(10.dp));Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.primary,modifier=Modifier.size(52.dp).clickable(onClick=onCategories)){Icon(Icons.Outlined.MedicalServices,"Browse doctors",tint=MaterialTheme.colorScheme.onPrimary,modifier=Modifier.padding(13.dp))}}}}
   item{SectionHeader("Favourite doctors",if(favourites.isNotEmpty())"View all" else null,if(favourites.isNotEmpty())onFavourites else null)}
   if(favourites.isEmpty())item{EmptyCard("Save doctors you trust for faster booking next time.")}else items(favourites.take(2),key={it.id}){DoctorCard(it,true,{onDoctor(it.id)},{})}
   item{Spacer(Modifier.height(4.dp))}
  }
 }
}
@Composable
fun DoctorListScreen(
    category: String,
    onBack: () -> Unit,
    state: PatientUiState,
    platform: PlatformConnectionState,
    onSearch: (String) -> Unit,
    onDoctor: (String) -> Unit,
    onHostedDoctor: (String) -> Unit,
    onRefreshHosted: () -> Unit,
    onFavourite: (String) -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit
) {
    var query by remember(category) { mutableStateOf(if (category == "All") state.query else "") }
    LaunchedEffect(category) {
        onSearch(query)
        onRefreshHosted()
    }
    val hostedClinics = if (platform.status == PlatformConnectionStatus.CONNECTED) {
        platform.clinics.filter { PlatformDiscovery.matches(it, category, query) }
    } else {
        emptyList()
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DoloBottomBar(selected = PatientBottomDestination.BOOK, onHome = onHome, onAppointments = onAppointments, onBook = onBook) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal=18.dp,vertical=12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenTitle(if (category == "All") "Search Doctors" else category, onBack) }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; onSearch(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search doctors or clinics") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
            }
            if (hostedClinics.isNotEmpty()) {
                item { Text("Available doctors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(hostedClinics, key = { "hosted-${it.id}" }) { clinic ->
                    HostedDoctorCard(clinic) { onHostedDoctor(clinic.id) }
                }
            }
            if (state.doctors.isNotEmpty()) {
                if (hostedClinics.isNotEmpty()) item { Text("More doctors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(state.doctors, key = { it.id }) { doctor ->
                    DoctorCard(doctor, doctor.id in state.favouriteIds, { onDoctor(doctor.id) }, { onFavourite(doctor.id) })
                }
            }
            if (state.doctors.isEmpty() && hostedClinics.isEmpty()) {
                item { EmptyCard("No doctors match your search.") }
            }
        }
    }
}

@Composable
private fun HostedDoctorCard(clinic: PlatformClinic, onOpen: () -> Unit) {
 DoloCard(Modifier.clickable(onClick=onOpen),containerColor=MaterialTheme.colorScheme.primaryContainer){
  Row(verticalAlignment=Alignment.CenterVertically){
   Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.size(62.dp)){Icon(Icons.Outlined.MedicalServices,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(15.dp))}
   Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(clinic.doctorName,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(clinic.specialty,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelLarge);Text("${clinic.name}, ${clinic.city}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)};StatusBadge("Available",DoloSuccess)
  }
  HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
  Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Star,null,tint=DoloWarning,modifier=Modifier.size(18.dp));Text(if(clinic.publishedReviewCount>0)" ${"%.1f".format(clinic.publishedRatingAverage?:0.0)}" else " New",fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("₹${clinic.consultationFeeMinor/100}",fontWeight=FontWeight.ExtraBold);Spacer(Modifier.width(10.dp));Icon(Icons.Outlined.ArrowForward,"Open profile",tint=MaterialTheme.colorScheme.primary)}
 }
}
@Composable
fun HostedDoctorDetailsScreen(
    clinic: PlatformClinic?,
    announcements: List<HostedCommunication>,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onBook: () -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBrowse: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DoloBottomBar(PatientBottomDestination.BOOK, onHome, onAppointments, onBrowse) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScreenTitle("Doctor Profile", onBack) }
            if (clinic == null) {
                item { EmptyCard("This approved hosted Doctor profile is unavailable. Refresh discovery and try again.") }
                item { PrimaryButton("Refresh hosted doctors", onRefresh) }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(88.dp)) {
                                Icon(Icons.Outlined.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(22.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(clinic.doctorName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                            Text(clinic.specialty, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Verified DO-LO profile", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item { InfoCard("Qualification and experience", listOfNotNull(clinic.qualification.takeIf { it.isNotBlank() }, clinic.experienceYears.takeIf { it > 0 }?.let { "$it years of experience" }).ifEmpty { listOf("Approved details not provided") }.joinToString("\n")) }
                item { InfoCard("Registration", clinic.registrationNumber.ifBlank { "Approved registration detail not provided" }) }
                item { InfoCard("About", clinic.about.ifBlank { "Approved profile description not provided" }) }
                item { InfoCard("Clinic", "${clinic.name}\n${clinic.city}\nConsultation fee paid at clinic: INR ${clinic.consultationFeeMinor / 100}") }
                item { Text("Doctor announcements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) }
                if (announcements.isEmpty()) item { InfoCard("Current update", "No active announcement from this Doctor.") }
                else items(announcements, key = { "doctor-announcement-${it.id}" }) { announcement ->
                    InfoCard(announcement.title, announcement.message + "\nActive ${announcement.startsOn} to ${announcement.endsOn}")
                }
                item { InfoCard("Published Patient reviews", if (clinic.publishedReviewCount > 0) "★ ${"%.1f".format(clinic.publishedRatingAverage ?: 0.0)} / 5\n${clinic.publishedReviewCount} review${if (clinic.publishedReviewCount == 1) "" else "s"} published after Admin moderation" else "No published Patient reviews yet") }
                item { Text("Profile information is reviewed before it becomes visible to Patients.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                item { PrimaryButton("Book appointment", onBook) }
            }
        }
    }
}
@Composable
fun DoctorCard(d:Doctor,favourite:Boolean,onOpen:()->Unit,onFavourite:()->Unit){
 DoloCard(Modifier.clickable(onClick=onOpen)){
  Row(verticalAlignment=Alignment.CenterVertically){
   Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.size(62.dp)){Icon(Icons.Outlined.MedicalServices,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(15.dp))}
   Spacer(Modifier.width(12.dp))
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){Text(d.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(d.specialty,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelLarge);Text(d.clinic,color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall,maxLines=1)}
   IconButton(onFavourite){Icon(if(favourite)Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,"Favourite",tint=if(favourite)DoloCoral else MaterialTheme.colorScheme.onSurfaceVariant)}
  }
  HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
  Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Star,null,tint=DoloWarning,modifier=Modifier.size(18.dp));Text(" ${d.rating}",fontWeight=FontWeight.Bold);Text("  •  ${d.experienceYears}+ years",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.weight(1f));Text("₹${d.consultationFee}",fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.onSurface);Spacer(Modifier.width(10.dp));Icon(Icons.Outlined.ArrowForward,"Open profile",tint=MaterialTheme.colorScheme.primary)}
 }
}
@Composable
fun DoctorDetailsScreen(
    id: String,
    favourite: Boolean,
    reviews: List<DoctorReview>,
    onBack: () -> Unit,
    onFavourite: () -> Unit,
    onBook: () -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBrowse: () -> Unit
) {
    val doctor = DummyData.doctors.firstOrNull { it.id == id } ?: DummyData.doctors.first()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DoloBottomBar(PatientBottomDestination.BOOK, onHome, onAppointments, onBrowse) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScreenTitle("Doctor Details", onBack) }
            item { DoctorCard(doctor, favourite, {}, onFavourite) }
            item { InfoCard("About", "Experienced ${doctor.specialty.lowercase()} focused on clear guidance and patient-friendly care.") }
            item { InfoCard("Clinic", doctor.clinic + "\nWalk-in sessions: Morning and Evening") }
            item { InfoCard("Patient reviews", "★ ${doctor.rating} / 5\n${reviews.count { it.doctorId == doctor.id }} verified DO-LO reviews") }
            item { PrimaryButton("Book Walk-in Appointment", onBook) }
        }
    }
}
@Composable fun FavouritesScreen(state:PatientUiState,onBack:()->Unit,onDoctor:(String)->Unit,onFavourite:(String)->Unit){val ds=DummyData.doctors.filter{it.id in state.favouriteIds};LazyColumn(pageModifier().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{ScreenTitle("Favourite Doctors",onBack)};if(ds.isEmpty())item{EmptyCard("You have not saved any doctors yet.")}else items(ds){DoctorCard(it,true,{onDoctor(it.id)},{onFavourite(it.id)})}}}
@Composable
fun AppointmentHistoryScreen(list:List<Appointment>,onBack:()->Unit,onQueue:(String)->Unit,onReschedule:(String)->Unit,onReview:(String,String)->Unit,canReschedule:(Appointment)->Boolean,canReview:(Appointment)->Boolean,onHome:()->Unit,onBook:()->Unit){
 var filter by remember{mutableStateOf("UPCOMING")}
 val visible=list.filter{appointment->when(filter){"UPCOMING"->appointment.status !in listOf(AppointmentStatus.COMPLETED,AppointmentStatus.MISSED);"PAST"->appointment.status in listOf(AppointmentStatus.COMPLETED,AppointmentStatus.MISSED);else->true}}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={DoloBottomBar(PatientBottomDestination.APPOINTMENTS,onHome,{},onBook)}){padding->
  LazyColumn(Modifier.padding(padding).fillMaxSize(),contentPadding=PaddingValues(horizontal=18.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{ScreenTitle("Appointments",onBack)}
   item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("UPCOMING" to "Upcoming","PAST" to "Past","ALL" to "All").forEach{(value,label)->FilterChip(selected=filter==value,onClick={filter=value},label={Text(label)},modifier=Modifier.weight(1f),colors=FilterChipDefaults.filterChipColors(selectedContainerColor=MaterialTheme.colorScheme.primaryContainer,selectedLabelColor=MaterialTheme.colorScheme.onPrimaryContainer,selectedLeadingIconColor=MaterialTheme.colorScheme.primary))}}}
   if(visible.isEmpty())item{DoloCard(containerColor=MaterialTheme.colorScheme.surfaceVariant){Icon(Icons.Outlined.EventNote,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.align(Alignment.CenterHorizontally).size(38.dp));Text(if(filter=="UPCOMING")"No upcoming appointments" else "No appointments in this section",style=MaterialTheme.typography.titleMedium,modifier=Modifier.align(Alignment.CenterHorizontally));if(filter=="UPCOMING")PrimaryButton("Book an appointment",onBook)}}
   else items(visible,key={it.id}){appointment->
    DoloCard{
     Row(verticalAlignment=Alignment.Top){Column(Modifier.weight(1f)){Text(appointment.doctorName,style=MaterialTheme.typography.titleMedium);Text(appointment.patientName+" • "+appointment.clinic,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};StatusBadge(ReleaseReadiness.readableStatus(appointment.status),when(appointment.status){AppointmentStatus.COMPLETED->DoloSuccess;AppointmentStatus.MISSED->DoloCoral;else->MaterialTheme.colorScheme.primary})}
     Row(verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceVariant){Text("Token ${appointment.token}",Modifier.padding(horizontal=11.dp,vertical=7.dp),fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.primary)};Spacer(Modifier.width(10.dp));Column{Text(displayDate(appointment.date),style=MaterialTheme.typography.bodyMedium);Text(appointment.session.name.lowercase().replaceFirstChar(Char::uppercase)+" session",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
     StatusTimeline(appointment.status)
     if(appointment.status in listOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION))PrimaryButton("Track live queue",{onQueue(appointment.id)})
     if(canReschedule(appointment))SecondaryButton("Reschedule appointment",{onReschedule(appointment.id)},icon=Icons.Outlined.EventRepeat)
     if(canReview(appointment))SecondaryButton("Rate consultation",{onReview(appointment.doctorId,appointment.id)},icon=Icons.Outlined.StarOutline)
    }
   }
  }
 }
}
@Composable fun LiveQueueScreen(state:PatientUiState,appointmentId:String,onBack:()->Unit,onRefresh:()->Unit,onOffline:()->Unit,onAdvance:()->Unit,onMissed:()->Unit,onComplete:()->Unit,onReschedule:()->Unit,canReschedule:(Appointment)->Boolean){
 val appointment=state.appointments.firstOrNull{it.id==appointmentId}?:state.active
 val queue=state.queues[appointmentId]?:state.queue?.takeIf{it.appointmentId==appointmentId}
 var nowMillis by remember{mutableStateOf(System.currentTimeMillis())}
 var showTestTools by remember{mutableStateOf(false)}
 LaunchedEffect(appointmentId){while(true){onRefresh();delay(ReleaseReadiness.QUEUE_REFRESH_INTERVAL_MILLIS)}}
 LaunchedEffect(appointmentId,queue?.currentTokenStartedAt){while(true){nowMillis=System.currentTimeMillis();delay(1000)}}
 LazyColumn(pageModifier().padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{ScreenTitle("Live Queue",onBack)}
  if(appointment==null)item{EmptyCard("Appointment not found.")}
  else{
   item{InfoCard(appointment.doctorName,"Patient: "+appointment.patientName+"\n"+appointment.clinic+"\nAppointment date: "+displayDate(appointment.date)+"\nToken "+appointment.token+" - "+appointment.session.name.lowercase().replaceFirstChar(Char::uppercase)+" session")}
   item{Card(Modifier.fillMaxWidth().shadow(9.dp,RoundedCornerShape(24.dp)),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant),elevation=CardDefaults.cardElevation(defaultElevation=5.dp),shape=RoundedCornerShape(24.dp)){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Text("CURRENTLY IN CONSULTATION",color=MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=FontWeight.Bold);Text((queue?.currentToken?:0).toString(),fontSize=58.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.primary)
    HorizontalDivider(Modifier.padding(vertical=12.dp));Row(Modifier.fillMaxWidth()){QueueMetric("Your token",appointment.token.toString(),Modifier.weight(1f),accent=Color(0xFFE94F64));QueueMetric("Patients ahead",(queue?.patientsAhead?:0).toString(),Modifier.weight(1f));QueueMetric("Countdown",QueueCountdown.format(QueueCountdown.remainingSeconds(queue,nowMillis)),Modifier.weight(1f))}
   }}}
   item{QueueConnectionBanner(state.syncStatus,queue?.refreshedAt?:0,onRefresh)}
   item{InfoCard("Queue status",ReleaseReadiness.readableStatus(queue?.status?:appointment.status)+"\nAverage consultation: "+QueueCalculator.AVERAGE_CONSULTATION_MINUTES+" minutes\nThe current consultation is included in your estimated wait.")}
   if(appointment.status!=AppointmentStatus.MISSED){
    item{PrimaryButton("Refresh queue",onRefresh)}
    item{TextButton({showTestTools=!showTestTools},Modifier.fillMaxWidth()){Icon(Icons.Outlined.Build,null,modifier=Modifier.size(17.dp));Spacer(Modifier.width(7.dp));Text(if(showTestTools)"Hide prototype controls" else "Prototype test controls")}}
    if(showTestTools)item{DoloCard(containerColor=MaterialTheme.colorScheme.surfaceVariant){Text("Testing only",style=MaterialTheme.typography.titleSmall);Text("These controls simulate queue events and are kept outside the normal Patient journey.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);SecondaryButton("Show offline state",onOffline,icon=Icons.Outlined.CloudOff);SecondaryButton("Advance one token",onAdvance,icon=Icons.Outlined.SkipNext);if(appointment.status==AppointmentStatus.IN_CONSULTATION)PrimaryButton("Complete consultation",onComplete);TextButton(onMissed,Modifier.fillMaxWidth()){Text("Mark appointment missed",color=MaterialTheme.colorScheme.error)}}}   }else{
    item{InfoCard("Appointment missed","You can reschedule once within 10 days.\nNew appointment date: "+LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")))}
    if(canReschedule(appointment))item{PrimaryButton("Reschedule for "+LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd MMM")),onReschedule)}
    else item{EmptyCard("This appointment is no longer eligible for rescheduling.")}
   }
  }
 }
}

@Composable
private fun QueueConnectionBanner(
    syncStatus: String,
    refreshedAt: Long,
    onRetry: () -> Unit
) {
    val offline = syncStatus == SyncStatus.OFFLINE
    val stale = !offline && ReleaseReadiness.isQueueStale(refreshedAt)
    val title = when {
        offline -> "You are offline"
        stale -> "Queue update may be stale"
        else -> "Queue is up to date"
    }
    val message = when {
        offline -> "Showing the last saved queue. Reconnect and retry before travelling."
        stale -> "The last queue update is over one minute old. Refresh before relying on the estimate."
        else -> "Automatically refreshes every 15 seconds while this screen is open."
    }
    val icon = when {
        offline -> Icons.Outlined.Warning
        stale -> Icons.Outlined.Schedule
        else -> Icons.Outlined.CheckCircle
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (offline || stale) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (offline || stale) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            if (offline || stale) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun HostedHomeUpdateCard(update: HostedCommunication, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().shadow(7.dp, RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (update.audience == "ALL_PATIENTS") "DO-LO update" else "Doctor update", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(update.title, fontWeight = FontWeight.ExtraBold)
            Text(update.message, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HostedHomeAppointmentCard(
    appointment: HostedAppointment,
    live: HostedLiveQueue?,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CloudDone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(appointment.doctorName, fontWeight = FontWeight.ExtraBold)
                    Text("Patient: ${appointment.patientName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${appointment.date} - ${appointment.session.lowercase().replaceFirstChar(Char::uppercase)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) { Text("HOSTED", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth()) {
                QueueMetric("Your token", appointment.token.toString(), Modifier.weight(1f), Color(0xFFE94F64))
                QueueMetric("Current token", live?.currentToken?.toString() ?: "--", Modifier.weight(1f))
                QueueMetric("Patients ahead", live?.patientsAhead?.toString() ?: "--", Modifier.weight(1f))
            }
            Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Timer, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Authoritative estimated wait", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(live?.estimatedMinutes?.let { "$it min" } ?: "Not available", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(live?.countdownState?.replace('_', ' ') ?: appointment.status.replace('_', ' '), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Clinic fee: ${appointment.clinicFeeStatus} • Tap for receipt, booking and reschedule details", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomeAppointmentQueueCard(
 appointment:Appointment,
 queue:QueueSnapshot?,
 nowMillis:Long,
 onClick:()->Unit
){
 val countdown=QueueCountdown.format(QueueCountdown.remainingSeconds(queue,nowMillis))
 Card(Modifier.fillMaxWidth().shadow(10.dp,RoundedCornerShape(24.dp)).clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer),elevation=CardDefaults.cardElevation(defaultElevation=6.dp,pressedElevation=2.dp),shape=RoundedCornerShape(24.dp)){
  Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Icon(Icons.Outlined.MedicalServices,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(42.dp));Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)){Text(appointment.doctorName,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("Patient: "+appointment.patientName,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(displayDate(appointment.date)+" - "+appointment.session.name.lowercase().replaceFirstChar(Char::uppercase),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    Icon(Icons.Outlined.ArrowForward,null,tint=MaterialTheme.colorScheme.primary)
   }
   HorizontalDivider()
   Row(Modifier.fillMaxWidth()){
    QueueMetric("Your token",appointment.token.toString(),Modifier.weight(1f),accent=Color(0xFFE94F64))
    QueueMetric("In consultation",(queue?.currentToken?:0).toString(),Modifier.weight(1f))
    QueueMetric("Patients ahead",(queue?.patientsAhead?:0).toString(),Modifier.weight(1f))
   }
   Surface(color=MaterialTheme.colorScheme.background,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Timer,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Estimated turn countdown",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(countdown,fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.primary)};Text("Avg. 12 min",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   }
   Text("Includes the consultation currently in progress.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}

@Composable private fun QueueMetric(label:String,value:String,modifier:Modifier=Modifier,accent:Color=Color.Unspecified){val resolvedAccent=if(accent==Color.Unspecified)MaterialTheme.colorScheme.primary else accent;Column(modifier,horizontalAlignment=Alignment.CenterHorizontally){Text(value,fontSize=22.sp,fontWeight=FontWeight.ExtraBold,color=resolvedAccent);Text(label,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)}}

@Composable
fun ProfileScreen(
    state: PatientUiState,
    identityCard: com.dolo.patient.auth.PublicIdentityCard?,
    identityMessage: String,
    onRefreshIdentity: () -> Unit,
    onBack: () -> Unit,
    onSave: (String, String, String, PatientGender) -> Unit,
    onAddFamily: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(state.profile.name) }
    var phone by remember { mutableStateOf(state.profile.phone) }
    var city by remember { mutableStateOf(state.profile.city) }
    var gender by remember { mutableStateOf(state.profile.gender) }
    var familyName by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    LazyColumn(
        modifier = pageModifier().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Profile & family", onBack) }
        item {
            Card(Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant), shape=RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.Badge, null, tint=MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("Your DO-LO ID", fontWeight=FontWeight.Bold) }
                    if (identityCard != null) {
                        Text(identityCard.doloId, fontSize=22.sp, fontWeight=FontWeight.ExtraBold, color=MaterialTheme.colorScheme.primary)
                        Text(identityCard.displayName + " | " + identityCard.role, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Use this ID when contacting DO-LO support. It is not your mobile number.", fontSize=12.sp, color=MaterialTheme.colorScheme.onSurfaceVariant)
                    } else Text(identityMessage, color=MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick=onRefreshIdentity, modifier=Modifier.fillMaxWidth()) { Text("Refresh DO-LO ID") }
                }
            }
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { value -> name = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") }
            )
        }
        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { value -> phone = value.filter { char -> char.isDigit() }.take(10) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile number") }
            )
        }
        item {
            OutlinedTextField(
                value = city,
                onValueChange = { value -> city = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("City") }
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Patient avatar", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PatientGender.entries.forEach { option ->
                        FilterChip(
                            selected = gender == option,
                            onClick = { gender = option },
                            label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                            leadingIcon = { Icon(if (option == PatientGender.FEMALE) Icons.Outlined.Woman else Icons.Outlined.Man, null) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
        item { PrimaryButton("Save changes", { onSave(name, phone, city, gender) }) }
        item { Text("Family members", style = MaterialTheme.typography.titleLarge) }
        if (state.family.isEmpty()) {
            item { EmptyCard("Add a family member to book appointments for them.") }
        } else {
            items(items = state.family, key = { member -> member.id }) { member ->
                InfoCard(member.name, member.relation + " • " + member.age + " years")
            }
        }
        item {
            OutlinedTextField(
                value = familyName,
                onValueChange = { value -> familyName = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Family member name") }
            )
        }
        item {
            OutlinedTextField(
                value = relation,
                onValueChange = { value -> relation = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Relation") }
            )
        }
        item {
            OutlinedTextField(
                value = age,
                onValueChange = { value -> age = value.filter { char -> char.isDigit() }.take(3) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Age") }
            )
        }
        item {
            OutlinedButton(
                onClick = {
                    if (familyName.isNotBlank() && relation.isNotBlank()) {
                        onAddFamily(familyName, relation, age.toIntOrNull() ?: 0)
                        familyName = ""
                        relation = ""
                        age = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Icon(Icons.Outlined.PersonAdd, null); Spacer(Modifier.width(8.dp)); Text("Add family member") }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: PatientUiState,
    hostedState: HostedSyncUiState?,
    onBack: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkHostedRead: (String) -> Unit
) {
    val hostedNotifications=hostedState?.snapshot?.notifications.orEmpty()
    val newestHostedCursor=hostedNotifications.maxByOrNull{runCatching{it.cursor.toLong()}.getOrDefault(0L)}?.cursor
    var requestedHostedCursor by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(newestHostedCursor,hostedState?.loading) { onMarkRead();if(hostedState?.loading!=true&&newestHostedCursor!=null&&requestedHostedCursor!=newestHostedCursor){requestedHostedCursor=newestHostedCursor;onMarkHostedRead(newestHostedCursor)} }
    LazyColumn(
        modifier = pageModifier().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Notifications", onBack) }
        if (hostedNotifications.isNotEmpty()) {
            item { Text("Hosted appointment updates",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold) }
            items(items=hostedNotifications,key={"hosted-${it.cursor}"}){notification->
                Card(modifier=Modifier.fillMaxWidth().shadow(8.dp,RoundedCornerShape(18.dp)),colors=CardDefaults.cardColors(containerColor=if(notification.read)MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant),shape=RoundedCornerShape(18.dp)){
                    Column(Modifier.padding(16.dp)){Text(notification.title,fontWeight=FontWeight.Bold);Text(notification.message,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${notification.patientName} - Token ${notification.tokenNumber}",fontSize=12.sp,color=MaterialTheme.colorScheme.primary)}
                }
            }
        }
        if (state.notifications.isEmpty() && hostedNotifications.isEmpty()) {
            item { EmptyCard("Queue and appointment updates will appear here.") }
        } else {
            items(items = state.notifications, key = { notification -> notification.id }) { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(18.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(notification.title, fontWeight = FontWeight.Bold)
                        Text(notification.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewScreen(
    state: PatientUiState,
    doctorId: String,
    appointmentId: String,
    onBack: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    val doctor = DummyData.doctors.firstOrNull { it.id == doctorId }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LazyColumn(
        modifier = pageModifier().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Rate Consultation", onBack) }
        item {
            InfoCard(
                doctor?.name ?: "Doctor",
                "Only completed consultations can receive a verified review."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = star.toString(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = comment,
                onValueChange = { value -> comment = value.take(300) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Share your experience") },
                minLines = 4
            )
        }
        item {
            PrimaryButton(
                label = "Submit Verified Review",
                onClick = { onSubmit(rating, comment) },
                enabled = comment.isNotBlank()
            )
        }
        if (state.reviews.any { review -> review.appointmentId == appointmentId }) {
            item { InfoCard("Review submitted", "Thank you for your feedback.") }
        }
    }
}

@Composable
fun SupportScreen(
    onBack:()->Unit,
    onIntegrations:()->Unit,
    hosted:HostedSyncUiState,
    onRefresh:()->Unit,
    onSubmit:(String,String,String)->Unit
){
    var category by remember{mutableStateOf("APP")};var subject by remember{mutableStateOf("")};var message by remember{mutableStateOf("")}
    val categories=listOf("APPOINTMENT","DOCTOR","BILLING","APP","OTHER")
    LaunchedEffect(Unit){if(hosted.snapshot==null)onRefresh()}
    LazyColumn(modifier=pageModifier().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{ScreenTitle("Help & support",onBack)}
        item{InfoCard("How does the live queue work?","Your token, patients ahead and estimated wait refresh while the hosted screen is open.")}
        item{InfoCard("Private in-app support","Your requests are visible only to your current DO-LO account and the support team.")}
        item{OutlinedButton(onClick=onIntegrations,modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.Settings,null);Spacer(Modifier.width(8.dp));Text("App status & diagnostics")}}
        item{Text("Create support request",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
        items(categories.chunked(3)){row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){row.forEach{value->FilterChip(selected=category==value,onClick={category=value},label={Text(value.lowercase().replaceFirstChar { it.uppercase() })},modifier=Modifier.weight(1f))};repeat(3-row.size){Spacer(Modifier.weight(1f))}}}
        item{OutlinedTextField(subject,{subject=it.take(120)},label={Text("Subject (8-120 characters)")},modifier=Modifier.fillMaxWidth(),enabled=!hosted.loading)}
        item{OutlinedTextField(message,{message=it.take(1000)},label={Text("Describe the issue (20-1000 characters)")},modifier=Modifier.fillMaxWidth(),minLines=4,enabled=!hosted.loading)}
        item{PrimaryButton(if(hosted.loading)"Sending..." else "Submit support request",{onSubmit(category,subject,message)},enabled=!hosted.loading&&subject.trim().length in 8..120&&message.trim().length in 20..1000)}
        item{Text(hosted.message,color=if(hosted.error)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Your requests",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);TextButton(onRefresh,enabled=!hosted.loading){Text("Refresh")}}}
        val requests=hosted.snapshot?.supportRequests.orEmpty()
        if(requests.isEmpty())item{EmptyCard("No hosted support requests yet.")}else items(requests,key={it.id}){request->Card(Modifier.fillMaxWidth().shadow(6.dp,RoundedCornerShape(20.dp)),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(request.category,fontWeight=FontWeight.Bold);Text(request.status,color=if(request.status=="RESOLVED")MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,fontWeight=FontWeight.Bold)};Text(request.subject,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(request.message);if(request.adminNote.isNotBlank())InfoCard("DO-LO Admin response",request.adminNote);Text("Updated ${request.updatedAt}",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)}}}
    }
}

@Composable
fun IntegrationStatusScreen(
    onBack: () -> Unit,
    platform: PlatformConnectionState,
    onRefreshPlatform: () -> Unit,
    onHostedSync: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (platform.status == PlatformConnectionStatus.NOT_CHECKED) onRefreshPlatform()
    }
    LazyColumn(
        modifier = pageModifier().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("App status & diagnostics", onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (platform.status == PlatformConnectionStatus.CONNECTED) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (platform.status == PlatformConnectionStatus.CONNECTED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("DO-LO service connection", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(platform.message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    if (platform.status == PlatformConnectionStatus.CONNECTED) {

                        val capability = platform.capabilities
                        Text(
                            "Service version ${platform.serviceVersion}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Database: ${if (capability?.databaseConnected == true) "connected" else "unavailable"} • Hosted clinics: ${platform.clinics.size}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Text(
                            "Secure sign-in: ${if (capability?.authenticationEnabled == true) "available" else "unavailable"} • Patient data connection: ${capability?.patientSynchronization ?: "DISABLED"}",
                            color = if (capability?.authenticationEnabled == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onRefreshPlatform,
                        enabled = platform.status != PlatformConnectionStatus.CONNECTING,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (platform.status == PlatformConnectionStatus.CONNECTING) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Checking...")
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Check connection")
                        }
                    }
                }
            }
        }
        item {
            InfoCard(
                "Data safety",
                "Your locally saved profile, family, favourites and test appointments stay on this device. Connected bookings use the separate hosted test account."
            )
        }
        item {
            Button(onClick = onHostedSync, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CloudDone, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open connected care")
            }
        }
        if (platform.status == PlatformConnectionStatus.CONNECTED) {
            item {
                val clinicText = if (platform.clinics.isEmpty()) {
                    "No hosted clinics are published yet. The tested local doctor catalogue remains available."
                } else {
                    platform.clinics.take(4).joinToString("\n") { clinic ->
                        "${clinic.name}, ${clinic.city} — ${clinic.doctorName} (${clinic.specialty})"
                    }
                }
                InfoCard("Connected clinic discovery", clinicText)
            }
        }
        items(
            items = IntegrationRegistry.patientCapabilities,
            key = { capability -> capability.type.name }
        ) { capability ->
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(capability.title, fontWeight = FontWeight.Bold)
                        Text(capability.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Status: Disabled",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        item {
            Text(
                "SMS, maps, payments and push providers are not enabled in this build. No provider credentials are stored in the app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}
@Composable
private fun StatusTimeline(status: String) {
    val steps = listOf(
        AppointmentStatus.BOOKED,
        AppointmentStatus.WAITING,
        AppointmentStatus.IN_CONSULTATION,
        AppointmentStatus.COMPLETED
    )
    val currentIndex = steps.indexOf(status).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (index <= currentIndex) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (index <= currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = step.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase),
                    fontSize = 9.sp
                )
            }
        }
    }
}

private fun displayDate(value:String):String=runCatching{LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))}.getOrDefault(value)

@Composable
private fun InfoCard(title:String,text:String){DoloCard{Text(title,style=MaterialTheme.typography.titleMedium);Text(text,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}

@Composable
private fun EmptyCard(text:String){Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceVariant,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Text(text,Modifier.padding(18.dp),textAlign=TextAlign.Center,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
