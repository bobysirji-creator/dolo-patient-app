package com.dolo.patient.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dolo.patient.auth.*
import com.dolo.patient.data.*
import com.dolo.patient.platform.*
import com.dolo.patient.ui.screens.*

object Routes{const val Splash="splash";const val Login="login";const val Home="home";const val Categories="categories";const val Doctors="doctors/{category}";const val DoctorDetails="doctor/{doctorId}";const val Booking="booking/{doctorId}";const val Confirmation="confirmation/{doctorId}/{session}";const val History="history";const val Favourites="favourites";const val Queue="queue/{appointmentId}";const val Profile="profile";const val Notifications="notifications";const val Support="support";const val Integrations="integrations";const val HostedSync="hosted-sync";const val HostedDoctorDetails="hosted-doctor/{clinicId}";const val Review="review/{doctorId}/{appointmentId}"}
@Composable fun DoloPatientApp(authRepository:AuthRepository,patientRepository:PatientRepository,platformApi:PlatformApi,hostedSyncApi:HostedPatientSyncApi){
 val nav=rememberNavController();val auth:AuthViewModel=viewModel(factory=AuthViewModelFactory(authRepository));val patient:PatientViewModel=viewModel(factory=PatientViewModelFactory(patientRepository));val platform:PlatformConnectionViewModel=viewModel(factory=PlatformConnectionViewModelFactory(platformApi));val hosted:HostedPatientSyncViewModel=viewModel(factory=HostedPatientSyncViewModelFactory(hostedSyncApi))
 NavHost(nav,startDestination=Routes.Splash){
  composable(Routes.Splash){SplashScreen{nav.navigate(if(auth.uiState.step==AuthStep.AUTHENTICATED)Routes.Home else Routes.Login){popUpTo(Routes.Splash){inclusive=true}}}}
  composable(Routes.Login){LoginScreen(auth){nav.navigate(Routes.Home){popUpTo(Routes.Login){inclusive=true}}}}
  composable(Routes.Home){HomeScreen(onCategories={nav.navigate(Routes.Categories)},onDoctor={nav.navigate("doctor/"+it)},onHistory={nav.navigate(Routes.History)},onFavourites={nav.navigate(Routes.Favourites)},onQueue={nav.navigate("queue/"+it)},onProfile={nav.navigate(Routes.Profile)},onNotifications={nav.navigate(Routes.Notifications)},onSupport={nav.navigate(Routes.Support)},onLogout={auth.logout();nav.navigate(Routes.Login){popUpTo(Routes.Home){inclusive=true}}},state=patient.uiState,onSearch={patient.search(it);nav.navigate("doctors/All")},onRefreshQueues=patient::refreshAllQueues,authStatus=auth.uiState.session?.mode?.label ?: "Local fallback")}
  composable(Routes.Categories){CategoriesScreen(nav::popBackStack){nav.navigate("doctors/"+it)}}
  composable(Routes.Doctors,arguments=listOf(navArgument("category"){type=NavType.StringType})){e->val c=e.arguments?.getString("category").orEmpty();DoctorListScreen(c,nav::popBackStack,patient.uiState,platform.uiState,{patient.search(it,if(c=="All")null else c)},{nav.navigate("doctor/"+it)},{nav.navigate("hosted-doctor/"+it)},platform::refresh,patient::toggleFavourite,{nav.navigate(Routes.Home){launchSingleTop=true}},{nav.navigate(Routes.History){launchSingleTop=true}},{nav.navigate(Routes.Categories){launchSingleTop=true}})}
  composable(Routes.HostedDoctorDetails,arguments=listOf(navArgument("clinicId"){type=NavType.StringType})){e->val id=e.arguments?.getString("clinicId").orEmpty();HostedDoctorDetailsScreen(platform.uiState.clinics.firstOrNull{it.id==id},nav::popBackStack,platform::refresh){nav.navigate(Routes.HostedSync)}}
  composable(Routes.DoctorDetails,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->val id=e.arguments?.getString("doctorId").orEmpty();DoctorDetailsScreen(id,patient.uiState.favouriteIds.contains(id),patient.uiState.reviews,nav::popBackStack,{patient.toggleFavourite(id)},{nav.navigate("booking/"+id)})}
  composable(Routes.Favourites){FavouritesScreen(patient.uiState,nav::popBackStack,{nav.navigate("doctor/"+it)},patient::toggleFavourite)}
  composable(Routes.History){AppointmentHistoryScreen(patient.uiState.appointments,nav::popBackStack,{nav.navigate("queue/"+it)},{patient.reschedule(it)},{doctorId,appointmentId->nav.navigate("review/"+doctorId+"/"+appointmentId)},patient::canReschedule,patient::canReview,{nav.navigate(Routes.Home){launchSingleTop=true}},{nav.navigate(Routes.Categories){launchSingleTop=true}})}
  composable(Routes.Queue,arguments=listOf(navArgument("appointmentId"){type=NavType.StringType})){e->val id=e.arguments?.getString("appointmentId").orEmpty();LiveQueueScreen(patient.uiState,id,nav::popBackStack,{patient.refreshQueue(id)},{patient.refreshQueue(id,false)},{patient.advanceQueue(id)},{patient.markMissed(id)},{patient.completeAppointment(id)},{patient.reschedule(id)},patient::canReschedule)}
  composable(Routes.Profile){ProfileScreen(patient.uiState,nav::popBackStack,{name,phone,city->patient.updateProfile(name,phone,city)},{name,relation,age->patient.addFamilyMember(name,relation,age)})}
  composable(Routes.Notifications){NotificationsScreen(patient.uiState,nav::popBackStack,patient::markNotificationsRead)}
  composable(Routes.Support){SupportScreen(nav::popBackStack){nav.navigate(Routes.Integrations)}}
  composable(Routes.Integrations){IntegrationStatusScreen(nav::popBackStack,platform.uiState,platform::refresh){nav.navigate(Routes.HostedSync)}}
  composable(Routes.HostedSync){HostedSyncScreen(nav::popBackStack,hosted)}
  composable(Routes.Review,arguments=listOf(navArgument("doctorId"){type=NavType.StringType},navArgument("appointmentId"){type=NavType.StringType})){e->val doctorId=e.arguments?.getString("doctorId").orEmpty();val appointmentId=e.arguments?.getString("appointmentId").orEmpty();ReviewScreen(patient.uiState,doctorId,appointmentId,nav::popBackStack){rating,comment->patient.addReview(appointmentId,rating,comment)}}
  composable(Routes.Booking,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->BookingScreen(e.arguments?.getString("doctorId").orEmpty(),patient.uiState,nav::popBackStack){id,date,s,patientName->patient.book(id,date,s,patientName);nav.navigate("confirmation/"+id+"/"+s.name)}}
  composable(Routes.Confirmation){e->ConfirmationScreen(e.arguments?.getString("doctorId").orEmpty(),e.arguments?.getString("session").orEmpty(),patient.uiState.active,{patient.uiState.active?.id?.let{nav.navigate("queue/"+it)}}){nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}}}
 }
}
