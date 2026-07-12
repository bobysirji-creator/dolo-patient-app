package com.dolo.patient.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.auth.AuthStep
import com.dolo.patient.auth.AuthViewModel
import com.dolo.patient.data.Appointment
import com.dolo.patient.data.DummyData
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.Session
import com.dolo.patient.ui.components.*
import com.dolo.patient.ui.theme.*

private val bg = Modifier.fillMaxSize().background(DoloBackground)
private val wash = Brush.linearGradient(listOf(Color(0xFFE9FAFA), Color.White))

@Composable fun SplashScreen(onContinue:()->Unit){Box(bg.background(wash).padding(28.dp)){Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){BrandLogo();Spacer(Modifier.height(30.dp));CircleIcon(Icons.Outlined.MedicalServices,150.dp);Spacer(Modifier.height(24.dp));Text("Book. Track. Visit.",style=MaterialTheme.typography.headlineMedium);Text("Worry less.",color=DoloTeal,fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(42.dp));PrimaryButton("Get started",onContinue)}}}

@Composable fun LoginScreen(auth:AuthViewModel,onLogin:()->Unit){
 val state=auth.uiState;LaunchedEffect(state.step){if(state.step==AuthStep.AUTHENTICATED)onLogin()}
 LazyColumn(bg.background(wash).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  item{Spacer(Modifier.height(32.dp));BrandLogo()}
  item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(if(state.step==AuthStep.OTP)"Verify OTP" else "Welcome Back!",style=MaterialTheme.typography.headlineMedium);Text(if(state.step==AuthStep.OTP)"Code sent to +91 "+state.phone else "Login to continue your health journey",color=DoloMuted)};CircleIcon(Icons.Outlined.HealthAndSafety,112.dp)}}
  item{Surface(Modifier.shadow(12.dp,RoundedCornerShape(28.dp)),shape=RoundedCornerShape(28.dp),color=Color.White){Column(Modifier.padding(24.dp)){
   Text(if(state.step==AuthStep.OTP)"Enter verification code" else "Login with Mobile Number",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(20.dp))
   if(state.step==AuthStep.PHONE){OutlinedTextField(state.phone,auth::updatePhone,Modifier.fillMaxWidth(),label={Text("Enter mobile number")},leadingIcon={Icon(Icons.Outlined.Phone,null)},prefix={Text("+91  ")},singleLine=true,isError=state.error!=null,shape=RoundedCornerShape(18.dp),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone));Spacer(Modifier.height(18.dp));PrimaryButton("Send OTP",auth::requestOtp,state.phone.length==10&&!state.isLoading)}
   else{OutlinedTextField(state.otp,auth::updateOtp,Modifier.fillMaxWidth(),label={Text("6-digit OTP")},leadingIcon={Icon(Icons.Outlined.Lock,null)},singleLine=true,isError=state.error!=null,shape=RoundedCornerShape(18.dp),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));Text("Demo OTP: 123456",color=DoloTeal,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=12.dp));PrimaryButton("Verify & Continue",auth::verifyOtp,state.otp.length==6&&!state.isLoading);TextButton(auth::editPhone,Modifier.align(Alignment.CenterHorizontally)){Text("Change mobile number")}}
   state.error?.let{Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(top=10.dp))};if(state.isLoading)LinearProgressIndicator(Modifier.fillMaxWidth().padding(top=12.dp));Spacer(Modifier.height(20.dp));Row(Modifier.align(Alignment.CenterHorizontally)){Icon(Icons.Outlined.VerifiedUser,null,tint=DoloTeal);Spacer(Modifier.width(8.dp));Text("Secure  •  Fast  •  Hassle-free",color=DoloMuted)}
  }}}
  item{Text("New to DO-LO?  Create an account",Modifier.fillMaxWidth().padding(18.dp),textAlign=TextAlign.Center,color=DoloTeal,fontWeight=FontWeight.Bold)}
 }
}

@Composable fun HomeScreen(onCategories:()->Unit,onDoctor:(String)->Unit,onLogout:()->Unit,active:Appointment?=null){
 Scaffold(containerColor=DoloBackground,bottomBar={DoloBottomBar()}){pad->LazyColumn(Modifier.padding(pad).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  item{Row(verticalAlignment=Alignment.CenterVertically){IconButton(onCategories){Icon(Icons.Outlined.Menu,"Menu")};Spacer(Modifier.weight(1f));BrandLogo();Spacer(Modifier.weight(1f));IconButton(onLogout){Icon(Icons.Outlined.Logout,"Log out")}}}
  item{Row(verticalAlignment=Alignment.Bottom){Column(Modifier.weight(1f)){Text("Welcome,",style=MaterialTheme.typography.titleLarge);Text("Rahul Sharma 👋",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal)};CircleIcon(Icons.Outlined.Chair,82.dp)}}
  item{SearchBar(onClick=onCategories)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){MetricCard("Your token",(active?.token?:0).toString(),Modifier.weight(1f));MetricCard("In process","12",Modifier.weight(1f),Color(0xFF1769D2))}}
  item{WaitCard()}
  item{Row{Text("Your Favorite Doctors",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.weight(1f));Text("View all",color=DoloTeal)}}
  items(DummyData.doctors){DoctorCard(it){onDoctor(it.id)}}
 }}
}

@Composable fun CategoriesScreen(onBack:()->Unit,onSelect:(String)->Unit){
 Scaffold(containerColor=DoloBackground,bottomBar={DoloBottomBar()}){pad->Column(Modifier.padding(pad).padding(horizontal=20.dp)){Spacer(Modifier.height(12.dp));ScreenTitle("Categories",onBack);Spacer(Modifier.height(22.dp));Text("Doctor Categories",style=MaterialTheme.typography.headlineMedium);Text("Find the right specialist for your health",color=DoloMuted);Spacer(Modifier.height(18.dp));SearchBar("Search category..."){};Spacer(Modifier.height(16.dp));LazyVerticalGrid(GridCells.Fixed(2),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)){items(DummyData.categories){c->Surface(Modifier.height(160.dp).shadow(6.dp,RoundedCornerShape(24.dp)).clickable{onSelect(c.name)},shape=RoundedCornerShape(24.dp),color=Color.White){Column(Modifier.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){Surface(shape=CircleShape,color=DoloSurfaceAlt,modifier=Modifier.size(70.dp)){Box(contentAlignment=Alignment.Center){Text(c.symbol,fontSize=32.sp,color=DoloTeal)}};Text(c.name,textAlign=TextAlign.Center,fontWeight=FontWeight.Bold);Text((40+c.name.length*3).toString()+" Doctors",color=DoloTeal,fontSize=12.sp)}}}}}}
}

@Composable fun DoctorListScreen(category:String,onBack:()->Unit,onBook:(String)->Unit){
 val doctors=DummyData.doctors.filter{it.specialty==category}.ifEmpty{DummyData.doctors}
 Scaffold(containerColor=DoloBackground,bottomBar={DoloBottomBar()}){pad->LazyColumn(Modifier.padding(pad).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Spacer(Modifier.height(12.dp));ScreenTitle(category,onBack);Spacer(Modifier.height(20.dp));Text(category,style=MaterialTheme.typography.headlineMedium);Text("Trusted specialists near you",color=DoloMuted)}
  item{SearchBar("Search "+category.lowercase()+", clinics..."){}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){AssistChip({},label={Text("Sort")});AssistChip({},label={Text("Available")});AssistChip({},label={Text("Fees")})}}
  items(doctors){DoctorCard(it){onBook(it.id)}};item{Spacer(Modifier.height(10.dp))}
 }}
}

@Composable fun DoctorCard(doctor:Doctor,onBook:()->Unit){Surface(Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(24.dp)),shape=RoundedCornerShape(24.dp),color=Color.White){Column(Modifier.padding(17.dp)){Row(verticalAlignment=Alignment.CenterVertically){CircleIcon(Icons.Outlined.MedicalServices,74.dp);Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text(doctor.name,style=MaterialTheme.typography.titleLarge);Text(doctor.specialty,color=DoloMuted);Text("★ "+doctor.rating+"  •  "+doctor.experienceYears+"+ Years",fontSize=12.sp);Text("⌖ "+doctor.clinic,color=DoloMuted,fontSize=12.sp)};Column(horizontalAlignment=Alignment.End){Text("Available",color=DoloTeal,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("₹"+doctor.consultationFee,fontWeight=FontWeight.Bold,fontSize=18.sp)}};Spacer(Modifier.height(13.dp));PrimaryButton("Book Now",onBook)}}}

@Composable fun BookingScreen(doctorId:String,onBack:()->Unit,onConfirm:(String,Session)->Unit){
 val doctor=DummyData.doctors.firstOrNull{it.id==doctorId}?:DummyData.doctors.first();var session by remember{mutableStateOf(Session.MORNING)}
 LazyColumn(bg.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(17.dp)){item{Spacer(Modifier.height(12.dp));ScreenTitle("Book Appointment",onBack);Text("Book Appointment",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.padding(top=18.dp))};item{DoctorCard(doctor){}}
 item{Step("1","Select Clinic");SelectCard("Heart Care Clinic","Sector 45 • 2.3 km away")}
 item{Step("2","Select Date");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Mon\n19","Tue\n20","Wed\n21","Thu\n22").forEachIndexed{i,d->Surface(shape=RoundedCornerShape(15.dp),color=if(i==0)DoloTeal else Color.White,shadowElevation=3.dp){Text(d,Modifier.padding(14.dp),textAlign=TextAlign.Center,color=if(i==0)Color.White else DoloNavy,fontWeight=FontWeight.Bold)}}}}
 item{Step("3","Select Session (Walk-in)");Text("Patients are seen in token order.",color=DoloMuted);Spacer(Modifier.height(10.dp));SessionCard("Morning Session","09:00 AM – 01:00 PM","30 Tokens Available",Icons.Outlined.LightMode,session==Session.MORNING){session=Session.MORNING};Spacer(Modifier.height(10.dp));SessionCard("Evening Session","05:00 PM – 09:00 PM","22 Tokens Available",Icons.Outlined.DarkMode,session==Session.EVENING){session=Session.EVENING}}
 item{Step("4","Patient Details");SelectCard("Rahul Sharma","+91 98765 43210")}
 item{Surface(shape=RoundedCornerShape(22.dp),color=DoloSurfaceAlt){Column(Modifier.fillMaxWidth().padding(18.dp)){Fee("Consultation Fee","₹"+doctor.consultationFee);Fee("DO-LO Service Charge","₹20");HorizontalDivider(Modifier.padding(vertical=9.dp));Fee("Total Payable","₹"+(doctor.consultationFee+20),true)}}}
 item{PrimaryButton("Confirm Booking",onClick={onConfirm(doctor.id,session)});Spacer(Modifier.height(20.dp))}
 }
}

@Composable fun ConfirmationScreen(doctorId:String,session:String,appointment:Appointment?=null,onDone:()->Unit){
 val doctor=DummyData.doctors.firstOrNull{it.id==doctorId}?:DummyData.doctors.first()
 LazyColumn(bg.background(wash).padding(horizontal=20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){item{Spacer(Modifier.height(22.dp));BrandLogo();Spacer(Modifier.height(18.dp));Surface(shape=CircleShape,color=DoloMint,modifier=Modifier.size(86.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Check,null,tint=Color.White,modifier=Modifier.size(48.dp))}};Text("Booking Confirmed!",style=MaterialTheme.typography.headlineMedium);Text("Your walk-in appointment is successfully booked.",color=DoloMuted,textAlign=TextAlign.Center)}
 item{Surface(Modifier.fillMaxWidth().shadow(10.dp,RoundedCornerShape(26.dp)),shape=RoundedCornerShape(26.dp),color=Color.White){Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.fillMaxWidth().background(DoloTeal).padding(13.dp),contentAlignment=Alignment.Center){Text("YOUR TOKEN NUMBER",color=Color.White,fontWeight=FontWeight.Bold)};Text((appointment?.token?:18).toString(),fontSize=90.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal);Text("Keep this number safe",color=DoloTeal);HorizontalDivider(Modifier.padding(14.dp));Detail(Icons.Outlined.CalendarMonth,"Date",appointment?.date?:"Today");Detail(Icons.Outlined.Schedule,"Session",session.lowercase().replaceFirstChar(Char::uppercase)+" Session");Detail(Icons.Outlined.MedicalServices,"Doctor",appointment?.doctorName?:doctor.name);Detail(Icons.Outlined.LocationOn,"Clinic",appointment?.clinic?:doctor.clinic)}}}
 item{WaitCard()}
 item{Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFFFF7E8)){Text("Important Instructions\n\n• Reach 10-15 minutes before your turn.\n• Your token may be skipped if absent.\n• One reschedule is allowed.",Modifier.fillMaxWidth().padding(18.dp),color=DoloNavy)}}
 item{PrimaryButton("Back to Home",onDone);Spacer(Modifier.height(24.dp))}
 }
}

@Composable private fun CircleIcon(icon:ImageVector,size:androidx.compose.ui.unit.Dp){Surface(shape=CircleShape,color=DoloSurfaceAlt,modifier=Modifier.size(size)){Box(contentAlignment=Alignment.Center){Icon(icon,null,tint=DoloTeal,modifier=Modifier.size(size/2))}}}
@Composable private fun WaitCard(){Surface(Modifier.fillMaxWidth().shadow(7.dp,RoundedCornerShape(24.dp)),shape=RoundedCornerShape(24.dp),color=DoloSurfaceAlt){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){CircleIcon(Icons.Outlined.Schedule,72.dp);Spacer(Modifier.width(15.dp));Column{Text("Approx. Time for Your Turn");Text("35-40 mins",fontSize=27.sp,fontWeight=FontWeight.ExtraBold,color=DoloTeal);Text("5 patients ahead of you",color=DoloMuted)}}}}
@Composable private fun Step(n:String,t:String){Row(verticalAlignment=Alignment.CenterVertically){Surface(shape=CircleShape,color=DoloTeal){Text(n,Modifier.padding(horizontal=11.dp,vertical=7.dp),color=Color.White,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(11.dp));Text(t,style=MaterialTheme.typography.titleLarge)}}
@Composable private fun SelectCard(t:String,s:String){Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color.White,border=BorderStroke(1.dp,DoloTeal)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.LocationOn,null,tint=DoloTeal);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(t,fontWeight=FontWeight.Bold);Text(s,color=DoloMuted)};Icon(Icons.Outlined.CheckCircle,null,tint=DoloTeal)}}}
@Composable private fun SessionCard(t:String,time:String,tokens:String,icon:ImageVector,selected:Boolean,onClick:()->Unit){Surface(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(20.dp),color=if(selected)DoloSurfaceAlt else Color.White,border=BorderStroke(1.dp,if(selected)DoloTeal else DoloBorder)){Row(Modifier.padding(17.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=DoloTeal,modifier=Modifier.size(40.dp));Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(t,fontWeight=FontWeight.Bold);Text(time,color=DoloMuted);Text(tokens,color=DoloTeal,fontSize=12.sp)};if(selected)Icon(Icons.Outlined.CheckCircle,null,tint=DoloTeal)}}}
@Composable private fun Fee(l:String,v:String,bold:Boolean=false){Row{Text(l,fontWeight=if(bold)FontWeight.Bold else FontWeight.Normal);Spacer(Modifier.weight(1f));Text(v,color=if(bold)DoloTeal else DoloNavy,fontWeight=if(bold)FontWeight.ExtraBold else FontWeight.Normal,fontSize=if(bold)22.sp else 14.sp)}}
@Composable private fun Detail(i:ImageVector,l:String,v:String){Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically){CircleIcon(i,48.dp);Spacer(Modifier.width(12.dp));Column{Text(l,color=DoloMuted,fontSize=12.sp);Text(v,fontWeight=FontWeight.Bold)}}}
