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
import com.dolo.patient.ui.screens.*

object Routes{const val Splash="splash";const val Login="login";const val Home="home";const val Categories="categories";const val Doctors="doctors/{category}";const val DoctorDetails="doctor/{doctorId}";const val Booking="booking/{doctorId}";const val Confirmation="confirmation/{doctorId}/{session}";const val History="history";const val Favourites="favourites";const val Queue="queue/{appointmentId}"}
@Composable fun DoloPatientApp(authRepository:AuthRepository,patientRepository:PatientRepository){
 val nav=rememberNavController();val auth:AuthViewModel=viewModel(factory=AuthViewModelFactory(authRepository));val patient:PatientViewModel=viewModel(factory=PatientViewModelFactory(patientRepository))
 NavHost(nav,startDestination=Routes.Splash){
  composable(Routes.Splash){SplashScreen{nav.navigate(if(auth.uiState.step==AuthStep.AUTHENTICATED)Routes.Home else Routes.Login){popUpTo(Routes.Splash){inclusive=true}}}}
  composable(Routes.Login){LoginScreen(auth){nav.navigate(Routes.Home){popUpTo(Routes.Login){inclusive=true}}}}
  composable(Routes.Home){HomeScreen(onCategories={nav.navigate(Routes.Categories)},onDoctor={nav.navigate("doctor/"+it)},onHistory={nav.navigate(Routes.History)},onFavourites={nav.navigate(Routes.Favourites)},onQueue={patient.uiState.active?.id?.let{nav.navigate("queue/"+it)}},onLogout={auth.logout();nav.navigate(Routes.Login){popUpTo(Routes.Home){inclusive=true}}},state=patient.uiState,onSearch={patient.search(it);nav.navigate("doctors/All")})}
  composable(Routes.Categories){CategoriesScreen(nav::popBackStack){nav.navigate("doctors/"+it)}}
  composable(Routes.Doctors,arguments=listOf(navArgument("category"){type=NavType.StringType})){e->val c=e.arguments?.getString("category").orEmpty();DoctorListScreen(c,nav::popBackStack,patient.uiState,{patient.search(it,if(c=="All")null else c)},{nav.navigate("doctor/"+it)},patient::toggleFavourite)}
  composable(Routes.DoctorDetails,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->val id=e.arguments?.getString("doctorId").orEmpty();DoctorDetailsScreen(id,patient.uiState.favouriteIds.contains(id),nav::popBackStack,{patient.toggleFavourite(id)},{nav.navigate("booking/"+id)})}
  composable(Routes.Favourites){FavouritesScreen(patient.uiState,nav::popBackStack,{nav.navigate("doctor/"+it)},patient::toggleFavourite)}
  composable(Routes.History){AppointmentHistoryScreen(patient.uiState.appointments,nav::popBackStack,{nav.navigate("queue/"+it)},{patient.reschedule(it)},patient::canReschedule)}
  composable(Routes.Queue,arguments=listOf(navArgument("appointmentId"){type=NavType.StringType})){e->val id=e.arguments?.getString("appointmentId").orEmpty();LiveQueueScreen(patient.uiState,id,nav::popBackStack,{patient.refreshQueue(id)},{patient.advanceQueue(id)},{patient.markMissed(id)},{patient.reschedule(id)},patient::canReschedule)}
  composable(Routes.Booking,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->BookingScreen(e.arguments?.getString("doctorId").orEmpty(),nav::popBackStack){id,date,s->patient.book(id,date,s);nav.navigate("confirmation/"+id+"/"+s.name)}}
  composable(Routes.Confirmation){e->ConfirmationScreen(e.arguments?.getString("doctorId").orEmpty(),e.arguments?.getString("session").orEmpty(),patient.uiState.active,{patient.uiState.active?.id?.let{nav.navigate("queue/"+it)}}){nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}}}
 }
}
