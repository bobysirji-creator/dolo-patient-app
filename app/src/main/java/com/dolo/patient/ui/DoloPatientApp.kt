package com.dolo.patient.ui

import android.net.Uri

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dolo.patient.auth.*
import com.dolo.patient.data.*
import com.dolo.patient.platform.*
import com.dolo.patient.data.model.Session
import com.dolo.patient.ui.booking.AppointmentBookingRoute
import com.dolo.patient.ui.booking.WalkInSessionType
import com.dolo.patient.ui.categories.DoctorCategoriesRoute
import com.dolo.patient.ui.confirmation.BookingConfirmationRoute
import com.dolo.patient.ui.home.AllQueuesScreen
import com.dolo.patient.ui.home.PatientHomeRoute
import com.dolo.patient.ui.doctors.DoctorListRoute
import com.dolo.patient.ui.login.LoginRoute
import com.dolo.patient.ui.login.LoginViewModel
import com.dolo.patient.ui.login.LoginViewModelFactory
import com.dolo.patient.ui.login.PilotAccessDialog
import com.dolo.patient.ui.otp.AuthOtpRepository
import com.dolo.patient.ui.otp.OtpVerificationRoute
import com.dolo.patient.ui.otp.OtpVerificationViewModel
import com.dolo.patient.ui.otp.OtpVerificationViewModelFactory
import com.dolo.patient.ui.screens.*
import kotlinx.coroutines.delay

object Routes{const val Splash="splash";const val Login="login";const val Home="home";const val Categories="categories";const val Doctors="doctors/{category}";const val CategoryDoctors="doctors/{categoryId}/{categoryName}";const val DoctorDetails="doctor/{doctorId}";const val Booking="booking/{doctorId}";const val Confirmation="confirmation/{appointmentId}";const val History="history";const val Favourites="favourites";const val Queue="queue/{appointmentId}";const val Profile="profile";const val Notifications="notifications";const val Support="support";const val Integrations="integrations";const val HostedSync="hosted-sync";const val AllQueues="all-queues";const val HostedDoctorDetails="hosted-doctor/{clinicId}";const val HostedBooking="hosted-booking/{clinicId}";const val Review="review/{doctorId}/{appointmentId}"}
@Composable fun DoloPatientApp(authRepository:AuthRepository,patientRepository:PatientRepository,platformApi:PlatformApi,hostedSyncApi:HostedPatientSyncApi,darkModeEnabled:Boolean=false,initialNotificationDestination:String?=null,onNotificationDestinationHandled:()->Unit={},onDarkModeChange:(Boolean)->Unit={}){
 val nav=rememberNavController();val auth:AuthViewModel=viewModel(factory=AuthViewModelFactory(authRepository));val login:LoginViewModel=viewModel(factory=LoginViewModelFactory(authRepository));val otp:OtpVerificationViewModel=viewModel(factory=OtpVerificationViewModelFactory(AuthOtpRepository(authRepository)));val patient:PatientViewModel=viewModel(factory=PatientViewModelFactory(patientRepository));val platform:PlatformConnectionViewModel=viewModel(factory=PlatformConnectionViewModelFactory(platformApi));val hosted:HostedPatientSyncViewModel=viewModel(factory=HostedPatientSyncViewModelFactory(hostedSyncApi))
 NavHost(
  nav,
  startDestination=Routes.Splash,
  enterTransition={fadeIn(tween(140))},
  exitTransition={fadeOut(tween(120))},
  popEnterTransition={fadeIn(tween(160))},
  popExitTransition={fadeOut(tween(120))}
 ){
  composable(Routes.Splash){SplashScreen{nav.navigate(if(auth.uiState.step==AuthStep.AUTHENTICATED)Routes.Home else Routes.Login){popUpTo(Routes.Splash){inclusive=true}}}}
  composable(Routes.Login){
   when(auth.uiState.step){
    AuthStep.PHONE->{
     LoginRoute(login,auth::beginOtp){auth.openPilot(false)}
     if(auth.uiState.pilotDialog)PilotAccessDialog(auth.uiState,{auth.openPilot(it)},auth::updatePilotDoloId,auth::updatePilotInviteCode,auth::updatePilotCredential,auth::submitPilot,auth::closePilot)
    }
    AuthStep.OTP->OtpVerificationRoute(
     viewModel=otp,
     phoneNumber=auth.uiState.phone,
     onEditNumber=auth::editPhone,
     onVerified={session->
      auth.completeAuthentication(session)
      nav.navigate(Routes.Home){popUpTo(Routes.Login){inclusive=true}}
     }
    )
    AuthStep.AUTHENTICATED->androidx.compose.runtime.LaunchedEffect(Unit){nav.navigate(Routes.Home){popUpTo(Routes.Login){inclusive=true}}}
   }
  }
  composable(Routes.Home){
   val hostedMode=auth.uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT)
   androidx.compose.runtime.LaunchedEffect(initialNotificationDestination){
    initialNotificationDestination?.let{destination->
     nav.navigate(destination){launchSingleTop=true}
     onNotificationDestinationHandled()
    }
   }
   androidx.compose.runtime.LaunchedEffect(hostedMode){if(hostedMode){while(true){hosted.refresh();delay(15_000)}}}
   PatientHomeRoute(patientState=patient.uiState,hostedState=if(hostedMode)hosted.uiState else null,darkModeEnabled=darkModeEnabled,onDarkModeChange=onDarkModeChange,onSearchDoctors={patient.search("");nav.navigate("doctors/All")},onNearMe={patient.search("");nav.navigate("doctors/All")},onAllQueues={nav.navigate(Routes.AllQueues)},onQueue={nav.navigate("queue/"+it)},onNotifications={nav.navigate(Routes.Notifications)},onFavorites={nav.navigate(Routes.Favourites)},onDoctor={nav.navigate("doctor/"+it)},onBookDoctor={nav.navigate("booking/"+it)},onAppointments={nav.navigate(Routes.History)},onBook={nav.navigate(Routes.Categories)},onHistory={nav.navigate(Routes.History)},onProfile={nav.navigate(Routes.Profile)},onSupport={nav.navigate(Routes.Support)},onLogout={hosted.clearAuthorityCache();auth.logout();nav.navigate(Routes.Login){popUpTo(Routes.Home){inclusive=true}}},onRefreshQueues=patient::refreshAllQueues,onRefreshHosted=hosted::refresh,onHostedSync={nav.navigate(Routes.HostedSync)})
  }
  composable(Routes.AllQueues){AllQueuesScreen(patientState=patient.uiState,hostedState=if(auth.uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT))hosted.uiState else null,onBack=nav::popBackStack,onQueue={nav.navigate("queue/"+it)})}
  composable(Routes.Categories){DoctorCategoriesRoute(notificationCount=patient.uiState.notifications.count{!it.isRead},onBack=nav::popBackStack,onNotifications={nav.navigate(Routes.Notifications)},onCategorySelected={category->nav.navigate("doctors/${Uri.encode(category.id)}/${Uri.encode(category.name)}")},onHome={nav.returnToHome()},onAppointments={nav.openPrimary(Routes.History)},onBook={},onHistory={nav.openPrimary(Routes.History)},onProfile={nav.openPrimary(Routes.Profile)})}
  composable(Routes.Doctors,arguments=listOf(navArgument("category"){type=NavType.StringType})){e->val c=e.arguments?.getString("category").orEmpty();DoctorListRoute(c,c,patient.uiState,platform.uiState,nav::popBackStack,{nav.navigate(Routes.Notifications)},{nav.navigate("doctor/"+it)},{nav.navigate("hosted-doctor/"+it)},{nav.navigate("booking/"+it)},platform::refresh,platform::findNearby,patient::toggleFavourite,{nav.returnToHome()},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Categories)},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Profile)})}
  composable(Routes.CategoryDoctors,arguments=listOf(navArgument("categoryId"){type=NavType.StringType},navArgument("categoryName"){type=NavType.StringType})){e->val id=e.arguments?.getString("categoryId").orEmpty();val name=e.arguments?.getString("categoryName").orEmpty();DoctorListRoute(id,name,patient.uiState,platform.uiState,nav::popBackStack,{nav.navigate(Routes.Notifications)},{nav.navigate("doctor/"+it)},{nav.navigate("hosted-doctor/"+it)},{nav.navigate("booking/"+it)},platform::refresh,platform::findNearby,patient::toggleFavourite,{nav.returnToHome()},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Categories)},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Profile)})}
  composable(Routes.HostedDoctorDetails,arguments=listOf(navArgument("clinicId"){type=NavType.StringType})){e->val id=e.arguments?.getString("clinicId").orEmpty();HostedDoctorDetailsScreen(platform.uiState.clinics.firstOrNull{it.id==id},hosted.uiState.snapshot?.communications.orEmpty().filter{it.audience=="DOCTOR_PROFILE"&&it.clinicId==id},nav::popBackStack,platform::refresh,{nav.navigate("hosted-booking/"+id)},{nav.returnToHome()},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Categories)})}
  composable(Routes.HostedBooking,arguments=listOf(navArgument("clinicId"){type=NavType.StringType})){e->val id=e.arguments?.getString("clinicId").orEmpty();HostedBookingScreen(id,hosted,nav::popBackStack){nav.openPrimary(Routes.History)}}
  composable(Routes.DoctorDetails,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->val id=e.arguments?.getString("doctorId").orEmpty();DoctorDetailsScreen(id,patient.uiState.favouriteIds.contains(id),patient.uiState.reviews,nav::popBackStack,{patient.toggleFavourite(id)},{nav.navigate("booking/"+id)},{nav.returnToHome()},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Categories)})}
  composable(Routes.Favourites){FavouritesScreen(patient.uiState,nav::popBackStack,{nav.navigate("doctor/"+it)},patient::toggleFavourite)}
  composable(Routes.History){
   val hostedMode=auth.uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT)
   if(hostedMode) HostedAppointmentHistoryScreen(hosted.uiState,nav::popBackStack,{nav.navigate("queue/"+it)},hosted::refresh,{nav.returnToHome()},{nav.openPrimary(Routes.Categories)})
   else AppointmentHistoryScreen(patient.uiState.appointments,nav::popBackStack,{nav.navigate("queue/"+it)},{patient.reschedule(it)},{doctorId,appointmentId->nav.navigate("review/"+doctorId+"/"+appointmentId)},patient::canReschedule,patient::canReview,{nav.returnToHome()},{nav.openPrimary(Routes.Categories)})
  }
  composable(Routes.Queue,arguments=listOf(navArgument("appointmentId"){type=NavType.StringType})){e->
   val id=e.arguments?.getString("appointmentId").orEmpty()
   val hostedMode=auth.uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT)
   if(hostedMode && hosted.uiState.snapshot?.appointments?.any{it.id==id}==true) HostedLiveQueueScreen(hosted.uiState,id,nav::popBackStack,hosted::refresh)
   else LiveQueueScreen(patient.uiState,id,nav::popBackStack,{patient.refreshQueue(id)},{patient.refreshQueue(id,false)},{patient.advanceQueue(id)},{patient.markMissed(id)},{patient.completeAppointment(id)},{patient.reschedule(id)},patient::canReschedule)
  }
  composable(Routes.Profile){ProfileScreen(patient.uiState,auth.uiState.identityCard,auth.uiState.identityMessage,auth::refreshIdentityCard,nav::popBackStack,{name,phone,city,gender->patient.updateProfile(name,phone,city,gender)},{name,relation,age->patient.addFamilyMember(name,relation,age)})}
  composable(Routes.Notifications){NotificationsScreen(patient.uiState,if(auth.uiState.session?.mode in setOf(PatientAuthMode.HOSTED_PROTOTYPE,PatientAuthMode.CONTROLLED_PILOT))hosted.uiState else null,nav::popBackStack,patient::markNotificationsRead,hosted::markHostedNotificationsRead)}
  composable(Routes.Support){SupportScreen(nav::popBackStack,{nav.navigate(Routes.Integrations)},hosted.uiState,hosted::refresh,hosted::submitSupportRequest)}
  composable(Routes.Integrations){IntegrationStatusScreen(nav::popBackStack,platform.uiState,platform::refresh){nav.navigate(Routes.HostedSync)}}
  composable(Routes.HostedSync){HostedSyncScreen(nav::popBackStack,hosted)}
  composable(Routes.Review,arguments=listOf(navArgument("doctorId"){type=NavType.StringType},navArgument("appointmentId"){type=NavType.StringType})){e->val doctorId=e.arguments?.getString("doctorId").orEmpty();val appointmentId=e.arguments?.getString("appointmentId").orEmpty();ReviewScreen(patient.uiState,doctorId,appointmentId,nav::popBackStack){rating,comment->patient.addReview(appointmentId,rating,comment)}}
  composable(Routes.Booking,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->val id=e.arguments?.getString("doctorId").orEmpty();AppointmentBookingRoute(id,patient.uiState,nav::popBackStack,{nav.navigate(Routes.Notifications)},patient::toggleFavourite,{request->val session=if(request.sessionType==WalkInSessionType.MORNING)Session.MORNING else Session.EVENING;val appointment=patient.book(request.doctorId,request.appointmentDate.toString(),session,request.patientName);nav.navigate("confirmation/"+appointment.id)},{nav.returnToHome()},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Categories)},{nav.openPrimary(Routes.History)},{nav.openPrimary(Routes.Profile)})}
  composable(Routes.Confirmation,arguments=listOf(navArgument("appointmentId"){type=NavType.StringType})){e->val appointmentId=e.arguments?.getString("appointmentId").orEmpty();val appointment=patient.uiState.appointments.firstOrNull{it.id==appointmentId}?:patient.uiState.active;BookingConfirmationRoute(appointment,patient.uiState.queues[appointmentId]?:patient.uiState.queue?.takeIf{it.appointmentId==appointmentId},patient.uiState.notifications.count{!it.isRead},{nav.navigate(Routes.Notifications)},{nav.navigate("doctor/"+it)},{},{nav.openPrimary(Routes.History)}){nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}}}
 }
}

private fun NavHostController.returnToHome(){
 if(!popBackStack(Routes.Home,inclusive=false)){
  navigate(Routes.Home){launchSingleTop=true}
 }
}
private fun NavHostController.openPrimary(route:String){
 navigate(route){
  popUpTo(Routes.Home){saveState=true}
  launchSingleTop=true
  restoreState=true
 }
}
