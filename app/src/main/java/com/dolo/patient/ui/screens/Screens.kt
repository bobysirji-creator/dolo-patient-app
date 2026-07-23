package com.dolo.patient.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.dolo.patient.data.model.Session
import com.dolo.patient.integrations.*
import com.dolo.patient.ui.components.*
import com.dolo.patient.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private val page=Modifier.fillMaxSize().background(DoloBackground)

@Composable fun SplashScreen(onContinue:()->Unit){Box(page.padding(28.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){BrandLogo();Spacer(Modifier.height(28.dp));Icon(Icons.Outlined.HealthAndSafety,null,tint=DoloTeal,modifier=Modifier.size(130.dp));Text("Book. Track. Visit.",style=MaterialTheme.typography.headlineMedium);Text("Worry less.",color=DoloTeal,fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(34.dp));PrimaryButton("Get started",onContinue)}}}
@Composable fun LoginScreen(auth:AuthViewModel,onLogin:()->Unit){val s=auth.uiState;LaunchedEffect(s.step){if(s.step==AuthStep.AUTHENTICATED)onLogin()};Column(page.padding(24.dp),verticalArrangement=Arrangement.Center){BrandLogo();Spacer(Modifier.height(30.dp));Text(if(s.step==AuthStep.OTP)"Verify OTP" else "Welcome Back!",style=MaterialTheme.typography.headlineMedium);Text(if(s.step==AuthStep.OTP)"Code sent to +91 "+s.phone else "Login using your mobile number",color=DoloMuted);Spacer(Modifier.height(20.dp));if(s.step==AuthStep.PHONE){OutlinedTextField(s.phone,auth::updatePhone,Modifier.fillMaxWidth(),label={Text("Mobile number")},prefix={Text("+91 ")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),singleLine=true);Spacer(Modifier.height(16.dp));PrimaryButton("Send OTP",auth::requestOtp,s.phone.length==10)}else{OutlinedTextField(s.otp,auth::updateOtp,Modifier.fillMaxWidth(),label={Text("6-digit OTP")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true);Text("Demo OTP: 123456",color=DoloTeal,modifier=Modifier.padding(vertical=12.dp));PrimaryButton(if(s.isLoading)"Connecting..." else "Verify & Continue",auth::verifyOtp,s.otp.length==6&&!s.isLoading);TextButton(auth::editPhone){Text("Change mobile number")}};s.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}}

@Composable fun HomeScreen(onCategories:()->Unit,onDoctor:(String)->Unit,onHistory:()->Unit,onFavourites:()->Unit,onQueue:(String)->Unit,onProfile:()->Unit,onNotifications:()->Unit,onSupport:()->Unit,onLogout:()->Unit,state:PatientUiState,onSearch:(String)->Unit,onRefreshQueues:()->Unit,authStatus:String,hostedState:HostedSyncUiState?=null,onRefreshHosted:()->Unit={},onHostedSync:()->Unit={}){
 var q by remember{mutableStateOf("")}
 var nowMillis by remember{mutableStateOf(System.currentTimeMillis())}
 val activeAppointments=state.appointments.filter{it.status in listOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION)}
 LaunchedEffect(Unit){while(true){nowMillis=System.currentTimeMillis();delay(1000)}}
 LaunchedEffect(Unit){while(true){delay(ReleaseReadiness.QUEUE_REFRESH_INTERVAL_MILLIS);onRefreshQueues()}}
 Scaffold(containerColor=DoloBackground,bottomBar={DoloBottomBar(selected=PatientBottomDestination.HOME,onHome={},onAppointments=onHistory,onBook=onCategories)}){p->
  LazyColumn(Modifier.padding(p).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
   item{Row(verticalAlignment=Alignment.CenterVertically){BrandLogo();Spacer(Modifier.weight(1f));IconButton(onNotifications){BadgedBox({if(state.notifications.any{!it.isRead})Badge()}){Icon(Icons.Outlined.Notifications,"Notifications")}};IconButton(onProfile){Icon(Icons.Outlined.Person,"Profile")};IconButton(onLogout){Icon(Icons.Outlined.Logout,"Logout")}}}
   item{Column{Text(state.profile.name+" ("+state.profile.city+")",fontSize=26.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal);Text("Identity: "+authStatus,color=if(authStatus=="Hosted prototype")DoloTeal else DoloMuted,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}}
   item{OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),placeholder={Text("Search doctor, specialty or clinic")},leadingIcon={Icon(Icons.Outlined.Search,null)},trailingIcon={IconButton({onSearch(q)}){Icon(Icons.Outlined.ArrowForward,null)}},singleLine=true,shape=RoundedCornerShape(18.dp))}
   hostedState?.let{hosted->
    val snapshot=hosted.snapshot
    val hostedAppointments=snapshot?.let { HostedHomePresentation.activeAppointments(it) }.orEmpty()
    val hostedUpdates=snapshot?.let { HostedHomePresentation.homeCommunications(it) }.orEmpty()
    item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Hosted appointments",style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f));TextButton(onHostedSync){Text("View all")}}}
    if(hosted.error)item{Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.errorContainer),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp)){Text("Hosted refresh needs attention",fontWeight=FontWeight.Bold);Text(hosted.message,fontSize=12.sp);TextButton(onRefreshHosted,enabled=!hosted.loading){Text("Retry")}}}}
    items(hostedUpdates,key={"hosted-home-update-${it.id}"}){update->HostedHomeUpdateCard(update,onHostedSync)}
    if(snapshot==null&&!hosted.error)item{EmptyCard(if(hosted.loading)"Loading hosted appointments..." else "Hosted appointments are not loaded yet.")}
    else if(hostedAppointments.isEmpty())item{EmptyCard("No active hosted appointment. Completed and missed visits remain in hosted history.")}
    else items(hostedAppointments,key={"hosted-home-${it.id}"}){appointment->HostedHomeAppointmentCard(appointment,snapshot?.let{HostedHomePresentation.liveQueue(it,appointment.id)},onHostedSync)}
   }
   item{Text(if(activeAppointments.size==1)"Local Test Appointment" else "Local Test Appointments",style=MaterialTheme.typography.titleLarge)}
   if(activeAppointments.isEmpty())item{EmptyCard("Your active appointment and live queue will appear here.")}
   else items(activeAppointments,key={it.id}){appointment->HomeAppointmentQueueCard(appointment,state.queues[appointment.id],nowMillis){onQueue(appointment.id)}}
   item{PrimaryButton("Browse doctor categories",onCategories)}
   item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){TextButton(onHistory){Text("History")};TextButton(onFavourites){Text("Favourites")};TextButton(onSupport){Text("Help")}}}
   item{Text("Favourite Doctors",style=MaterialTheme.typography.titleLarge)}
   val favs=DummyData.doctors.filter{it.id in state.favouriteIds}
   if(favs.isEmpty())item{EmptyCard("Tap the heart on a doctor to save them.")}else items(favs){DoctorCard(it,true,{onDoctor(it.id)},{})}
  }
 }
}
@Composable
fun CategoriesScreen(onBack:()->Unit,onSelect:(String)->Unit){
 Column(page.padding(20.dp)){
  ScreenTitle("Categories",onBack)
  Spacer(Modifier.height(18.dp))
  Text("Find the right specialist",style=MaterialTheme.typography.headlineMedium)
  Text(DummyData.categories.size.toString()+" specialties available",color=DoloMuted)
  Spacer(Modifier.height(16.dp))
  LazyVerticalGrid(GridCells.Fixed(2),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   items(DummyData.categories,key={it.id}){category->CategoryCard(category,onSelect)}
  }
 }
}

@Composable
private fun CategoryCard(category:com.dolo.patient.data.model.DoctorCategory,onSelect:(String)->Unit){
 val transition=rememberInfiniteTransition(label="category-"+category.id)
 val lift by transition.animateFloat(initialValue=0f,targetValue=-7f,animationSpec=infiniteRepeatable(animation=tween(1500),repeatMode=RepeatMode.Reverse),label="category-art-lift")
 Card(
  modifier=Modifier.height(184.dp).shadow(10.dp,RoundedCornerShape(24.dp)).clickable{onSelect(category.name)},
  colors=CardDefaults.cardColors(containerColor=Color(0xFFF8FCFF)),
  elevation=CardDefaults.cardElevation(defaultElevation=5.dp,pressedElevation=2.dp),
  shape=RoundedCornerShape(24.dp)
 ){
  Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){
   Image(painter=painterResource(category.imageRes),contentDescription=category.name+" illustration",modifier=Modifier.fillMaxWidth().height(118.dp).graphicsLayer{translationY=lift}.clip(RoundedCornerShape(18.dp)),contentScale=ContentScale.Crop)
   Text(category.name,textAlign=TextAlign.Center,fontWeight=FontWeight.ExtraBold,color=DoloNavy,modifier=Modifier.padding(bottom=5.dp))
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
        containerColor = DoloBackground,
        bottomBar = { DoloBottomBar(selected = PatientBottomDestination.BOOK, onHome = onHome, onAppointments = onAppointments, onBook = onBook) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                item { Text("Hosted doctors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(hostedClinics, key = { "hosted-${it.id}" }) { clinic ->
                    HostedDoctorCard(clinic) { onHostedDoctor(clinic.id) }
                }
            }
            if (state.doctors.isNotEmpty()) {
                if (hostedClinics.isNotEmpty()) item { Text("Local test catalogue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
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
    Card(
        modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp)).clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F7F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.White, shadowElevation = 6.dp, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Outlined.CloudDone, null, tint = DoloTeal, modifier = Modifier.padding(15.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(clinic.doctorName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DoloNavy)
                    Text(clinic.specialty, color = DoloTeal, fontWeight = FontWeight.SemiBold)
                    Text("${clinic.name}, ${clinic.city}", color = DoloMuted, fontSize = 12.sp)
                }
            }
            Text("Hosted availability controlled by DO-LO Admin", color = DoloTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(if(clinic.publishedReviewCount>0) "★ ${"%.1f".format(clinic.publishedRatingAverage ?: 0.0)} from ${clinic.publishedReviewCount} published review${if(clinic.publishedReviewCount==1)"" else "s"}" else "No published Patient reviews yet",color=DoloMuted,fontSize=12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Consultation fee at clinic: INR ${clinic.consultationFeeMinor / 100}", fontWeight = FontWeight.Bold, color = DoloNavy)
                Spacer(Modifier.weight(1f))
                Button(onClick = onOpen) { Text("View & Book") }
            }
        }
    }
}

@Composable
fun HostedDoctorDetailsScreen(
    clinic: PlatformClinic?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onBook: () -> Unit
) {
    LazyColumn(page.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScreenTitle("Doctor Profile", onBack) }
        if (clinic == null) {
            item { EmptyCard("This approved hosted Doctor profile is unavailable. Refresh discovery and try again.") }
            item { PrimaryButton("Refresh hosted doctors", onRefresh) }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F7F1)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color.White, shadowElevation = 6.dp, modifier = Modifier.size(88.dp)) {
                            Icon(Icons.Outlined.MedicalServices, null, tint = DoloTeal, modifier = Modifier.padding(22.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(clinic.doctorName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DoloNavy, textAlign = TextAlign.Center)
                        Text(clinic.specialty, color = DoloTeal, fontWeight = FontWeight.Bold)
                        Text("Approved by DO-LO Admin", color = DoloTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item { InfoCard("Qualification and experience", listOfNotNull(clinic.qualification.takeIf { it.isNotBlank() }, clinic.experienceYears.takeIf { it > 0 }?.let { "$it years of experience" }).ifEmpty { listOf("Approved details not provided") }.joinToString("\n")) }
            item { InfoCard("Registration", clinic.registrationNumber.ifBlank { "Approved registration detail not provided" }) }
            item { InfoCard("About", clinic.about.ifBlank { "Approved profile description not provided" }) }
            item { InfoCard("Clinic", "${clinic.name}\n${clinic.city}\nConsultation fee paid at clinic: INR ${clinic.consultationFeeMinor / 100}") }
            item { InfoCard("Published Patient reviews", if(clinic.publishedReviewCount>0) "★ ${"%.1f".format(clinic.publishedRatingAverage ?: 0.0)} / 5" + System.lineSeparator() + "${clinic.publishedReviewCount} review${if(clinic.publishedReviewCount==1)"" else "s"} published after Admin moderation" else "No published Patient reviews yet") }
            item { Text("Only the currently approved profile is shown. Pending or rejected Doctor edits are never displayed here.", color = DoloMuted, fontSize = 12.sp) }
            item { PrimaryButton("Book hosted appointment", onBook) }
        }
    }
}

@Composable
fun DoctorCard(d:Doctor,favourite:Boolean,onOpen:()->Unit,onFavourite:()->Unit){
 Card(
  modifier=Modifier.fillMaxWidth().shadow(10.dp,RoundedCornerShape(24.dp)).clickable(onClick=onOpen),
  colors=CardDefaults.cardColors(containerColor=Color(0xFFEEF6FF)),
  elevation=CardDefaults.cardElevation(defaultElevation=6.dp,pressedElevation=2.dp),
  shape=RoundedCornerShape(24.dp)
 ){
  Column(Modifier.padding(16.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Surface(shape=CircleShape,color=Color.White,shadowElevation=6.dp,modifier=Modifier.size(64.dp)){Icon(Icons.Outlined.MedicalServices,null,tint=DoloTeal,modifier=Modifier.padding(15.dp))}
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)){Text(d.name,fontWeight=FontWeight.ExtraBold,fontSize=18.sp,color=DoloNavy);Text(d.specialty,color=DoloTeal,fontWeight=FontWeight.SemiBold);Text("★ "+d.rating+"  •  "+d.experienceYears+"+ years",fontSize=12.sp);Text(d.clinic,color=DoloMuted,fontSize=12.sp)}
    IconButton(onFavourite){Icon(if(favourite)Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,"Favourite",tint=if(favourite)Color(0xFFE94F64) else DoloTeal)}
   }
   Spacer(Modifier.height(12.dp))
   Row(verticalAlignment=Alignment.CenterVertically){
    Text("₹"+d.consultationFee,fontWeight=FontWeight.ExtraBold,fontSize=18.sp,color=DoloNavy)
    Spacer(Modifier.weight(1f))
    Button(onClick=onOpen,elevation=ButtonDefaults.buttonElevation(defaultElevation=8.dp,pressedElevation=2.dp)){Text("View & Book")}
   }
  }
 }
}
@Composable fun DoctorDetailsScreen(id:String,favourite:Boolean,reviews:List<DoctorReview>,onBack:()->Unit,onFavourite:()->Unit,onBook:()->Unit){val d=DummyData.doctors.firstOrNull{it.id==id}?:DummyData.doctors.first();LazyColumn(page.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){item{ScreenTitle("Doctor Details",onBack)};item{DoctorCard(d,favourite,{},onFavourite)};item{InfoCard("About","Experienced "+d.specialty.lowercase()+" focused on clear guidance and patient-friendly care.")};item{InfoCard("Clinic",d.clinic+"\nWalk-in sessions: Morning and Evening")};item{InfoCard("Patient reviews","★ "+d.rating+" / 5\n"+reviews.count{it.doctorId==d.id}+" verified DO-LO reviews")};item{PrimaryButton("Book Walk-in Appointment",onBook)}}}
@Composable fun FavouritesScreen(state:PatientUiState,onBack:()->Unit,onDoctor:(String)->Unit,onFavourite:(String)->Unit){val ds=DummyData.doctors.filter{it.id in state.favouriteIds};LazyColumn(page.padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{ScreenTitle("Favourite Doctors",onBack)};if(ds.isEmpty())item{EmptyCard("You have not saved any doctors yet.")}else items(ds){DoctorCard(it,true,{onDoctor(it.id)},{onFavourite(it.id)})}}}
@Composable fun AppointmentHistoryScreen(list:List<Appointment>,onBack:()->Unit,onQueue:(String)->Unit,onReschedule:(String)->Unit,onReview:(String,String)->Unit,canReschedule:(Appointment)->Boolean,canReview:(Appointment)->Boolean,onHome:()->Unit,onBook:()->Unit){
 Scaffold(containerColor=DoloBackground,bottomBar={DoloBottomBar(selected=PatientBottomDestination.APPOINTMENTS,onHome=onHome,onAppointments={},onBook=onBook)}){p->
 LazyColumn(Modifier.padding(p).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{ScreenTitle("Appointment History",onBack)}
  if(list.isEmpty())item{EmptyCard("Your booked appointments will appear here.")}
  else items(list){a->
   Card(Modifier.fillMaxWidth().shadow(8.dp,RoundedCornerShape(20.dp)),elevation=CardDefaults.cardElevation(defaultElevation=5.dp),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp)){
    Row{Text(a.doctorName,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));AssistChip({},label={Text(a.status.replace("_"," "))})}
    Text(a.patientName+" • "+a.clinic,color=DoloMuted);Text(displayDate(a.date)+" • "+a.session.name.lowercase().replaceFirstChar(Char::uppercase)+" session")
    StatusTimeline(a.status);Text("Token "+a.token,color=DoloTeal,fontSize=22.sp,fontWeight=FontWeight.Bold)
    if(a.status in listOf(AppointmentStatus.BOOKED,AppointmentStatus.WAITING,AppointmentStatus.IN_CONSULTATION)){TextButton({onQueue(a.id)}){Text("Track live queue")}}
    if(canReschedule(a)){Button(onClick={onReschedule(a.id)},elevation=ButtonDefaults.buttonElevation(defaultElevation=8.dp,pressedElevation=2.dp)){Text("Reschedule once")}}
    if(canReview(a)){Button(onClick={onReview(a.doctorId,a.id)},elevation=ButtonDefaults.buttonElevation(defaultElevation=8.dp,pressedElevation=2.dp)){Text("Rate consultation")}}
   }}
  }
 }
}
}

@Composable fun BookingScreen(doctorId:String,state:PatientUiState,onBack:()->Unit,onConfirm:(String,String,Session,String)->Unit){
 val d=DummyData.doctors.firstOrNull{it.id==doctorId}?:DummyData.doctors.first()
 val dates=remember{(0L..2L).map{LocalDate.now().plusDays(it)}};var selectedDate by remember{mutableStateOf(dates.first())};var session by remember{mutableStateOf(Session.MORNING)}
 val patients=listOf(state.profile.name)+state.family.map{it.name};var patientName by remember{mutableStateOf(patients.first())}
 LazyColumn(page.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{ScreenTitle("Book Appointment",onBack)};item{InfoCard(d.name,d.specialty+"\n"+d.clinic)}
  item{Text("Who is this appointment for?",style=MaterialTheme.typography.titleLarge)}
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){patients.forEach{name->FilterChip(name==patientName,{patientName=name},{Text(name)})}}}
  item{Text("Choose appointment date",style=MaterialTheme.typography.titleLarge)}
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){dates.forEach{date->DateChoice(date,date==selectedDate,{selectedDate=date},Modifier.weight(1f))}}}
  item{InfoCard("Selected appointment",patientName+"\n"+selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")))}
  item{Text("Choose walk-in session",style=MaterialTheme.typography.titleLarge)}
  item{SessionChoice("Morning","09:00 AM – 01:00 PM",session==Session.MORNING){session=Session.MORNING}}
  item{SessionChoice("Evening","05:00 PM – 09:00 PM",session==Session.EVENING){session=Session.EVENING}}
  item{InfoCard("Payment summary","Consultation ₹"+d.consultationFee+"\nService charge ₹20\nTotal ₹"+(d.consultationFee+20))}
  item{PrimaryButton("Confirm Booking",onClick={onConfirm(d.id,selectedDate.toString(),session,patientName)})}
 }
}
@Composable fun ConfirmationScreen(doctorId:String,session:String,appointment:Appointment?=null,onQueue:()->Unit,onDone:()->Unit){
 val d=DummyData.doctors.firstOrNull{it.id==doctorId}?:DummyData.doctors.first()
 Column(page.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
  Icon(Icons.Outlined.CheckCircle,null,tint=DoloTeal,modifier=Modifier.size(80.dp));Text("Booking Confirmed!",style=MaterialTheme.typography.headlineMedium)
  Text("YOUR TOKEN NUMBER",modifier=Modifier.padding(top=24.dp));Text((appointment?.token?:0).toString(),fontSize=88.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal)
  InfoCard(appointment?.doctorName?:d.name,(appointment?.patientName?:"Patient")+"\n"+(appointment?.clinic?:d.clinic)+"\n"+displayDate(appointment?.date?:"Today")+" • "+session.lowercase().replaceFirstChar(Char::uppercase)+" session")
  Spacer(Modifier.height(20.dp));PrimaryButton("Track Live Queue",onQueue);Spacer(Modifier.height(10.dp));OutlinedButton(onDone,Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(20.dp))){Text("Back to Home")}
 }
}

@Composable fun LiveQueueScreen(state:PatientUiState,appointmentId:String,onBack:()->Unit,onRefresh:()->Unit,onOffline:()->Unit,onAdvance:()->Unit,onMissed:()->Unit,onComplete:()->Unit,onReschedule:()->Unit,canReschedule:(Appointment)->Boolean){
 val appointment=state.appointments.firstOrNull{it.id==appointmentId}?:state.active
 val queue=state.queues[appointmentId]?:state.queue?.takeIf{it.appointmentId==appointmentId}
 var nowMillis by remember{mutableStateOf(System.currentTimeMillis())}
 LaunchedEffect(appointmentId){while(true){onRefresh();delay(ReleaseReadiness.QUEUE_REFRESH_INTERVAL_MILLIS)}}
 LaunchedEffect(appointmentId,queue?.currentTokenStartedAt){while(true){nowMillis=System.currentTimeMillis();delay(1000)}}
 LazyColumn(page.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{ScreenTitle("Live Queue",onBack)}
  if(appointment==null)item{EmptyCard("Appointment not found.")}
  else{
   item{InfoCard(appointment.doctorName,"Patient: "+appointment.patientName+"\n"+appointment.clinic+"\nAppointment date: "+displayDate(appointment.date)+"\nToken "+appointment.token+" - "+appointment.session.name.lowercase().replaceFirstChar(Char::uppercase)+" session")}
   item{Card(Modifier.fillMaxWidth().shadow(9.dp,RoundedCornerShape(24.dp)),colors=CardDefaults.cardColors(containerColor=DoloSurfaceAlt),elevation=CardDefaults.cardElevation(defaultElevation=5.dp),shape=RoundedCornerShape(24.dp)){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Text("CURRENTLY IN CONSULTATION",color=DoloMuted,fontWeight=FontWeight.Bold);Text((queue?.currentToken?:0).toString(),fontSize=58.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal)
    HorizontalDivider(Modifier.padding(vertical=12.dp));Row(Modifier.fillMaxWidth()){QueueMetric("Your token",appointment.token.toString(),Modifier.weight(1f),accent=Color(0xFFE94F64));QueueMetric("Patients ahead",(queue?.patientsAhead?:0).toString(),Modifier.weight(1f));QueueMetric("Countdown",QueueCountdown.format(QueueCountdown.remainingSeconds(queue,nowMillis)),Modifier.weight(1f))}
   }}}
   item{QueueConnectionBanner(state.syncStatus,queue?.refreshedAt?:0,onRefresh)}
   item{InfoCard("Queue status",ReleaseReadiness.readableStatus(queue?.status?:appointment.status)+"\nAverage consultation: "+QueueCalculator.AVERAGE_CONSULTATION_MINUTES+" minutes\nThe current consultation is included in your estimated wait.")}
   if(appointment.status!=AppointmentStatus.MISSED){
    item{PrimaryButton("Refresh Queue",onRefresh)}
    item{OutlinedButton(onOffline,Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(20.dp))){Text("Demo: show offline state")}}
    item{OutlinedButton(onAdvance,Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(20.dp))){Text("Demo: advance one token")}}
    if(appointment.status==AppointmentStatus.IN_CONSULTATION)item{PrimaryButton("Demo: complete consultation",onComplete)}
    item{TextButton(onMissed,Modifier.fillMaxWidth()){Text("Demo: mark appointment missed",color=MaterialTheme.colorScheme.error)}}
   }else{
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
                DoloSurfaceAlt
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
                    DoloTeal
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(message, color = DoloMuted, fontSize = 13.sp)
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
            Text(if (update.audience == "ALL_PATIENTS") "DO-LO update" else "Doctor update", color = DoloTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(update.title, fontWeight = FontWeight.ExtraBold)
            Text(update.message, maxLines = 2, color = DoloMuted, fontSize = 13.sp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAFBF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CloudDone, null, tint = DoloTeal, modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(appointment.doctorName, fontWeight = FontWeight.ExtraBold)
                    Text("Patient: ${appointment.patientName}", color = DoloMuted)
                    Text("${appointment.date} - ${appointment.session.lowercase().replaceFirstChar(Char::uppercase)}", fontSize = 12.sp, color = DoloMuted)
                }
                Surface(color = DoloTeal.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) { Text("HOSTED", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = DoloTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth()) {
                QueueMetric("Your token", appointment.token.toString(), Modifier.weight(1f), Color(0xFFE94F64))
                QueueMetric("Current token", live?.currentToken?.toString() ?: "--", Modifier.weight(1f))
                QueueMetric("Patients ahead", live?.patientsAhead?.toString() ?: "--", Modifier.weight(1f))
            }
            Surface(color = DoloBackground, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Timer, null, tint = DoloTeal)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Authoritative estimated wait", fontSize = 12.sp, color = DoloMuted)
                        Text(live?.estimatedMinutes?.let { "$it min" } ?: "Not available", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DoloTeal)
                    }
                    Text(live?.countdownState?.replace('_', ' ') ?: appointment.status.replace('_', ' '), fontSize = 11.sp, color = DoloMuted)
                }
            }
            Text("Clinic fee: ${appointment.clinicFeeStatus} • Tap for receipt, booking and reschedule details", fontSize = 12.sp, color = DoloMuted)
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
 Card(Modifier.fillMaxWidth().shadow(10.dp,RoundedCornerShape(24.dp)).clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=Color(0xFFF1F8FF)),elevation=CardDefaults.cardElevation(defaultElevation=6.dp,pressedElevation=2.dp),shape=RoundedCornerShape(24.dp)){
  Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Icon(Icons.Outlined.MedicalServices,null,tint=DoloTeal,modifier=Modifier.size(42.dp));Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)){Text(appointment.doctorName,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("Patient: "+appointment.patientName,color=DoloMuted);Text(displayDate(appointment.date)+" - "+appointment.session.name.lowercase().replaceFirstChar(Char::uppercase),fontSize=12.sp,color=DoloMuted)}
    Icon(Icons.Outlined.ArrowForward,null,tint=DoloTeal)
   }
   HorizontalDivider()
   Row(Modifier.fillMaxWidth()){
    QueueMetric("Your token",appointment.token.toString(),Modifier.weight(1f),accent=Color(0xFFE94F64))
    QueueMetric("In consultation",(queue?.currentToken?:0).toString(),Modifier.weight(1f))
    QueueMetric("Patients ahead",(queue?.patientsAhead?:0).toString(),Modifier.weight(1f))
   }
   Surface(color=DoloBackground,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Timer,null,tint=DoloTeal);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Estimated turn countdown",fontSize=12.sp,color=DoloMuted);Text(countdown,fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal)};Text("Avg. 12 min",fontSize=12.sp,color=DoloMuted)}
   }
   Text("Includes the consultation currently in progress.",fontSize=12.sp,color=DoloMuted)
  }
 }
}

@Composable private fun QueueMetric(label:String,value:String,modifier:Modifier=Modifier,accent:Color=DoloTeal){Column(modifier,horizontalAlignment=Alignment.CenterHorizontally){Text(value,fontSize=22.sp,fontWeight=FontWeight.ExtraBold,color=accent);Text(label,fontSize=11.sp,color=DoloMuted,textAlign=TextAlign.Center)}}

@Composable
fun ProfileScreen(
    state: PatientUiState,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onAddFamily: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(state.profile.name) }
    var phone by remember { mutableStateOf(state.profile.phone) }
    var city by remember { mutableStateOf(state.profile.city) }
    var familyName by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    LazyColumn(
        modifier = page.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Patient Profile", onBack) }
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
        item { PrimaryButton("Save Profile", { onSave(name, phone, city) }) }
        item { Text("Family Members", style = MaterialTheme.typography.titleLarge) }
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
            Button(
                onClick = {
                    if (familyName.isNotBlank() && relation.isNotBlank()) {
                        onAddFamily(familyName, relation, age.toIntOrNull() ?: 0)
                        familyName = ""
                        relation = ""
                        age = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
            ) { Text("Add Family Member") }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: PatientUiState,
    onBack: () -> Unit,
    onMarkRead: () -> Unit
) {
    LaunchedEffect(Unit) { onMarkRead() }
    LazyColumn(
        modifier = page.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Notifications", onBack) }
        if (state.notifications.isEmpty()) {
            item { EmptyCard("Queue and appointment updates will appear here.") }
        } else {
            items(items = state.notifications, key = { notification -> notification.id }) { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(18.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notification.isRead) Color.White else DoloSurfaceAlt
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(notification.title, fontWeight = FontWeight.Bold)
                        Text(notification.message, color = DoloMuted)
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
        modifier = page.padding(20.dp),
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
                            tint = DoloTeal
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
    onBack: () -> Unit,
    onIntegrations: () -> Unit
) {
    LazyColumn(
        modifier = page.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Help & Support", onBack) }
        item {
            InfoCard(
                "How does the live queue work?",
                "Your token, patients ahead and estimated wait refresh while this screen is open."
            )
        }
        item {
            InfoCard(
                "What if I miss my turn?",
                "A missed appointment can be rescheduled once within 10 days."
            )
        }
        item {
            InfoCard(
                "Need more help?",
                "Complaint and chat provider hooks are reserved for a later stage."
            )
        }
        item {
            OutlinedButton(
                onClick = onIntegrations,
                modifier = Modifier.fillMaxWidth().shadow(7.dp, RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Integration readiness")
            }
        }
        item {
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().shadow(7.dp, RoundedCornerShape(20.dp))) {
                Text("Create Support Request (Coming Soon)")
            }
        }
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
        modifier = page.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Integration Readiness", onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = DoloSurfaceAlt),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (platform.status == PlatformConnectionStatus.CONNECTED) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (platform.status == PlatformConnectionStatus.CONNECTED) DoloTeal else DoloMuted
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("DO-LO hosted prototype", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(platform.message, color = DoloMuted, fontSize = 13.sp)
                    if (platform.status == PlatformConnectionStatus.CONNECTED) {

                        val capability = platform.capabilities
                        Text(
                            "Version ${platform.serviceVersion} • Stage ${capability?.stage ?: "unknown"}",
                            color = DoloNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Database: ${if (capability?.databaseConnected == true) "connected" else "unavailable"} • Hosted clinics: ${platform.clinics.size}",
                            color = DoloMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            "Prototype identity: ${if (capability?.authenticationEnabled == true) "enabled" else "disabled"} | Patient sync: ${capability?.patientSynchronization ?: "DISABLED"}",
                            color = if (capability?.authenticationEnabled == true) DoloTeal else DoloMuted,
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
                "Safe Stage 16C boundary",
                "Only the separate seeded dummy flow can now create server-authoritative bookings. Your entered phone and existing local profile, family, bookings and queue data are never uploaded."
            )
        }
        item {
            Button(onClick = onHostedSync, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CloudDone, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open hosted prototype sync")
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
                InfoCard("Hosted clinic discovery", clinicText)
            }
        }
        items(
            items = IntegrationRegistry.patientCapabilities,
            key = { capability -> capability.type.name }
        ) { capability ->
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = DoloSurfaceAlt, shape = CircleShape) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = DoloTeal,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(capability.title, fontWeight = FontWeight.Bold)
                        Text(capability.description, color = DoloMuted, fontSize = 13.sp)
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
                "Provider credentials are not stored in this app. Server appointments and live synchronization remain disabled until the separate Stage 16C migration is tested.",
                color = DoloMuted,
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
                    tint = if (index <= currentIndex) DoloTeal else DoloMuted
                )
                Text(
                    text = step.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable private fun DateChoice(date:LocalDate,selected:Boolean,onClick:()->Unit,modifier:Modifier=Modifier){Card(modifier.shadow(7.dp,RoundedCornerShape(16.dp)).clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=if(selected)DoloTeal else Color.White),elevation=CardDefaults.cardElevation(defaultElevation=4.dp),shape=RoundedCornerShape(16.dp)){Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(date.format(DateTimeFormatter.ofPattern("EEE")),color=if(selected)Color.White else DoloMuted,fontSize=12.sp);Text(date.dayOfMonth.toString(),color=if(selected)Color.White else DoloNavy,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(date.format(DateTimeFormatter.ofPattern("MMM")),color=if(selected)Color.White else DoloMuted,fontSize=12.sp)}}}
private fun displayDate(value:String):String=runCatching{LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))}.getOrDefault(value)

@Composable private fun SessionChoice(title:String,time:String,selected:Boolean,onClick:()->Unit){Card(Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(18.dp)).clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=if(selected)DoloSurfaceAlt else Color.White),elevation=CardDefaults.cardElevation(defaultElevation=4.dp),shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(title=="Morning")Icons.Outlined.LightMode else Icons.Outlined.DarkMode,null,tint=DoloTeal);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(title+" Session",fontWeight=FontWeight.Bold);Text(time,color=DoloMuted)};if(selected)Icon(Icons.Outlined.CheckCircle,null,tint=DoloTeal)}}}
@Composable private fun InfoCard(title:String,text:String){Card(Modifier.fillMaxWidth().shadow(8.dp,RoundedCornerShape(20.dp)),colors=CardDefaults.cardColors(containerColor=Color(0xFFF8FCFF)),elevation=CardDefaults.cardElevation(defaultElevation=5.dp),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp);Spacer(Modifier.height(6.dp));Text(text,color=DoloMuted)}}}
@Composable private fun EmptyCard(text:String){Card(Modifier.fillMaxWidth().shadow(8.dp,RoundedCornerShape(20.dp)),colors=CardDefaults.cardColors(containerColor=DoloSurfaceAlt),elevation=CardDefaults.cardElevation(defaultElevation=5.dp),shape=RoundedCornerShape(20.dp)){Text(text,Modifier.padding(22.dp),textAlign=TextAlign.Center,color=DoloMuted)}}
